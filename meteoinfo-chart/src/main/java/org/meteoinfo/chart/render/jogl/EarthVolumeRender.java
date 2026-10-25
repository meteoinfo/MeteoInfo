package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;
import org.meteoinfo.chart.graphic.VolumeGraphic;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.SphericalTransform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.Extent;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_3D;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_WRAP_R;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;

/**
 * Render for volume plot on the earth surface.
 * <p>
 * Unlike {@link VolumeRender}, which casts rays against an axis aligned bounding box,
 * the volume drawn on the earth is a spherical shell sector (longitude / latitude /
 * altitude). This renderer uploads a coarse shell sector mesh as the ray casting
 * carrier - the mesh only defines which screen pixels need ray casting, the actual
 * geometry is intersected analytically against the spherical shell in the fragment
 * shader, so the sampled values always follow the curved shell instead of the flat
 * box faces.
 * <p>
 * Only the back faces of the (closed) carrier mesh are drawn, which gives exactly one
 * fragment per pixel of the sector silhouette instead of one per shell layer, and the
 * depth of that fragment is taken from the first visible sample of the ray, so the
 * volume is still correctly occluded by the earth surface and other graphics.
 */
public class EarthVolumeRender extends JOGLGraphicRender {

    /**
     * Maximum / minimum number of ray marching samples
     */
    private static final int MAX_SAMPLE = 512;
    private static final int MIN_SAMPLE = 64;

    private VolumeGraphic volume;
    private int colorTexture;
    private int volumeTexture;
    private int normalsTexture;
    private Program program;
    private IntBuffer vbo;
    private int vertexNumber;
    private float radius = SphericalTransform.radius;

    // Uniform locations are cached per shader program. The uniforms are set directly
    // instead of through the static Program registry, because that registry is shared
    // by all shader programs and other renderers would otherwise overwrite the
    // uniforms of this program (their locations do not belong to this program).
    private final Map<String, Integer> uniformLocations = new HashMap<>();

    // Key of the geometry currently stored in the vertex buffer, so the shell mesh is
    // rebuilt when the radius or the geographic extent of the volume changes.
    private double[] meshKey;

    /**
     * Constructor
     *
     * @param gl JOGL GL2 object
     */
    public EarthVolumeRender(GL2 gl) {
        super(gl);

        useShader = true;
        initVertexBuffer();
    }

    /**
     * Constructor
     *
     * @param gl JOGL GL2 object
     * @param graphic Volume graphic
     */
    public EarthVolumeRender(GL2 gl, VolumeGraphic graphic) {
        this(gl);

        setVolume(graphic);
    }

    /**
     * Get earth radius used to build the shell
     *
     * @return Earth radius
     */
    public float getRadius() {
        return this.radius;
    }

    /**
     * Set earth radius used to build the shell - should be aligned with the earth surface
     *
     * @param value Earth radius
     */
    public void setRadius(float value) {
        this.radius = value;
    }

    /**
     * Get volume graphic
     *
     * @return Volume graphic
     */
    public VolumeGraphic getVolume() {
        return volume;
    }

    /**
     * Set volume graphic
     *
     * @param value Volume graphic
     */
    public void setVolume(VolumeGraphic value) {
        if (value == null) {
            return;
        }
        this.volume = value;
        setBufferData();
        this.bindingTextures();
        this.compileShaderProgram();
    }

    /**
     * Get ray casting type
     *
     * @return Ray casting type
     */
    public RayCastingType getRayCastingType() {
        return this.volume == null ? RayCastingType.MAX_VALUE : this.volume.getRayCastingType();
    }

    /**
     * Get brightness
     *
     * @return Brightness
     */
    public float getBrightness() {
        return this.volume == null ? 1.0f : this.volume.getBrightness();
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(1);
        gl.glGenBuffers(1, vbo);
    }

    /**
     * Get the geographic extent of the volume, fall back to the graphic extent.
     *
     * @return Geographic extent - longitude / latitude / altitude
     */
    private Extent geoExtent() {
        Extent geo = this.volume.getGeoExtent();
        if (geo == null) {
            geo = this.volume.getExtent();
        }
        return geo;
    }

    /**
     * Rebuild the shell mesh when the geometry that defines it has changed.
     */
    private void ensureBufferData() {
        Extent geo = geoExtent();
        double[] key = new double[]{geo.minX, geo.maxX, geo.minY, geo.maxY, geo.minZ, geo.maxZ,
                this.radius};
        if (this.meshKey != null && java.util.Arrays.equals(this.meshKey, key)) {
            return;
        }
        this.meshKey = key;
        setBufferData();
    }

    /**
     * Build the shell sector mesh that carries the ray casting
     */
    private void setBufferData() {
        float[] vertexBufferData = buildShellMesh();
        this.vertexNumber = vertexBufferData.length / 3;

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo.get(0));
        gl.glBufferData(GL_ARRAY_BUFFER, vertexBufferData.length * Float.BYTES,
                Buffers.newDirectFloatBuffer(vertexBufferData), GL_STATIC_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Build a triangle mesh of the lon/lat/alt shell sector in ECEF coordinates.
     * Both altitude surfaces and the four side walls are generated so that the mesh is
     * a closed volume. All faces are wound so their normals point outwards - the render
     * only draws back faces so that every pixel of the sector silhouette is rasterized
     * exactly once.
     *
     * @return Vertex positions - x,y,z per vertex
     */
    private float[] buildShellMesh() {
        Extent geo = geoExtent();
        double lonMin = geo.minX;
        double lonMax = geo.maxX;
        if (lonMax < lonMin) {
            lonMax += 360;
        }
        double latMin = geo.minY;
        double latMax = geo.maxY;
        double altMin = geo.minZ;
        double altMax = geo.maxZ;

        final int lonSegs = 64;
        final int latSegs = Math.max(2, Math.min(64, lonSegs / 2));
        final int altSegs = 8;

        int quads = 2 * lonSegs * latSegs
                + 2 * latSegs * altSegs
                + 2 * lonSegs * altSegs;
        float[] buf = new float[quads * 18];
        int p = 0;

        // Bottom (altMin) and top (altMax) surfaces - lon/lat grid
        for (int s = 0; s < 2; s++) {
            double alt = (s == 0) ? altMin : altMax;
            // Outward direction of the altitude surfaces is +/- the radial direction
            double outSign = (s == 0) ? -1.0 : 1.0;
            for (int j = 0; j < latSegs; j++) {
                for (int i = 0; i < lonSegs; i++) {
                    double lon0 = lonMin + (lonMax - lonMin) * i / lonSegs;
                    double lon1 = lonMin + (lonMax - lonMin) * (i + 1) / lonSegs;
                    double la0 = latMin + (latMax - latMin) * j / latSegs;
                    double la1 = latMin + (latMax - latMin) * (j + 1) / latSegs;
                    float[] a = vertex(lon0, la0, alt);
                    float[] b = vertex(lon1, la0, alt);
                    float[] c = vertex(lon1, la1, alt);
                    float[] d = vertex(lon0, la1, alt);
                    double clon = (lon0 + lon1) / 2;
                    double clat = (la0 + la1) / 2;
                    Vector3f out = radial(clon, clat);
                    p = writeQuad(buf, p, a, b, c, d, out.x * outSign, out.y * outSign, out.z * outSign);
                }
            }
        }

        // Walls at lon = lonMin and lon = lonMax - lat/alt grid. A sector spanning the
        // whole globe has no side wall there (both walls would be the same meridian and
        // would be rasterized twice, doubling the composite along that line).
        boolean globalLon = (lonMax - lonMin) >= 359.999;
        if (!globalLon) {
            for (int w = 0; w < 2; w++) {
                double lon = (w == 0) ? lonMin : lonMax;
                // Outward direction is -/+ the east (longitude increasing) direction
                double outSign = (w == 0) ? -1.0 : 1.0;
                Vector3f out = eastTangent(lon);
                for (int j = 0; j < latSegs; j++) {
                    for (int k = 0; k < altSegs; k++) {
                        double la0 = latMin + (latMax - latMin) * j / latSegs;
                        double la1 = latMin + (latMax - latMin) * (j + 1) / latSegs;
                        double al0 = altMin + (altMax - altMin) * k / altSegs;
                        double al1 = altMin + (altMax - altMin) * (k + 1) / altSegs;
                        float[] a = vertex(lon, la0, al0);
                        float[] b = vertex(lon, la1, al0);
                        float[] c = vertex(lon, la1, al1);
                        float[] d = vertex(lon, la0, al1);
                        p = writeQuad(buf, p, a, b, c, d, out.x * outSign, out.y * outSign, out.z * outSign);
                    }
                }
            }
        }

        // Walls at lat = latMin and lat = latMax - lon/alt grid
        for (int w = 0; w < 2; w++) {
            double lat = (w == 0) ? latMin : latMax;
            // A wall on a pole is degenerate (it collapses to a point) - skip it
            if ((w == 0 && lat <= -89.999) || (w == 1 && lat >= 89.999)) {
                continue;
            }
            // Outward direction is -/+ the north (latitude increasing) direction
            double outSign = (w == 0) ? -1.0 : 1.0;
            for (int i = 0; i < lonSegs; i++) {
                for (int k = 0; k < altSegs; k++) {
                    double lon0 = lonMin + (lonMax - lonMin) * i / lonSegs;
                    double lon1 = lonMin + (lonMax - lonMin) * (i + 1) / lonSegs;
                    double al0 = altMin + (altMax - altMin) * k / altSegs;
                    double al1 = altMin + (altMax - altMin) * (k + 1) / altSegs;
                    float[] a = vertex(lon0, lat, al0);
                    float[] b = vertex(lon1, lat, al0);
                    float[] c = vertex(lon1, lat, al1);
                    float[] d = vertex(lon0, lat, al1);
                    double clon = (lon0 + lon1) / 2;
                    Vector3f out = northTangent(clon, lat);
                    p = writeQuad(buf, p, a, b, c, d, out.x * outSign, out.y * outSign, out.z * outSign);
                }
            }
        }

        float[] result = new float[p];
        System.arraycopy(buf, 0, result, 0, p);
        return result;
    }

    /**
     * Write two triangles of a quad, wound so the face normal points along the given
     * outward direction.
     *
     * @param buf Vertex buffer
     * @param p Current buffer position
     * @param a First corner
     * @param b Second corner
     * @param c Third corner
     * @param d Fourth corner
     * @param ox Outward direction x
     * @param oy Outward direction y
     * @param oz Outward direction z
     * @return New buffer position
     */
    private int writeQuad(float[] buf, int p, float[] a, float[] b, float[] c, float[] d,
                          double ox, double oy, double oz) {
        double ux = b[0] - a[0];
        double uy = b[1] - a[1];
        double uz = b[2] - a[2];
        double vx = c[0] - a[0];
        double vy = c[1] - a[1];
        double vz = c[2] - a[2];
        double nx = uy * vz - uz * vy;
        double ny = uz * vx - ux * vz;
        double nz = ux * vy - uy * vx;
        if (nx * ox + ny * oy + nz * oz < 0) {
            // Flip the winding so the normal points outwards
            p = write(buf, p, a, c, b);
            return write(buf, p, a, d, c);
        }
        p = write(buf, p, a, b, c);
        return write(buf, p, a, c, d);
    }

    private int write(float[] buf, int p, float[] a, float[] b, float[] c) {
        buf[p] = a[0];
        buf[p + 1] = a[1];
        buf[p + 2] = a[2];
        buf[p + 3] = b[0];
        buf[p + 4] = b[1];
        buf[p + 5] = b[2];
        buf[p + 6] = c[0];
        buf[p + 7] = c[1];
        buf[p + 8] = c[2];
        return p + 9;
    }

    private float[] vertex(double lon, double lat, double alt) {
        double u = Math.toRadians(lon);
        double v = Math.toRadians(lat);
        double r = this.radius + alt;
        return new float[]{(float) (Math.cos(u) * Math.cos(v) * r),
                (float) (Math.sin(u) * Math.cos(v) * r),
                (float) (Math.sin(v) * r)};
    }

    /**
     * Radial (outward) unit vector at a longitude / latitude
     *
     * @param lon Longitude in degrees
     * @param lat Latitude in degrees
     * @return Unit vector
     */
    private static Vector3f radial(double lon, double lat) {
        double u = Math.toRadians(lon);
        double v = Math.toRadians(lat);
        return new Vector3f((float) (Math.cos(u) * Math.cos(v)), (float) (Math.sin(u) * Math.cos(v)),
                (float) Math.sin(v));
    }

    /**
     * East (longitude increasing) unit vector at a longitude
     *
     * @param lon Longitude in degrees
     * @return Unit vector
     */
    private static Vector3f eastTangent(double lon) {
        double u = Math.toRadians(lon);
        return new Vector3f((float) -Math.sin(u), (float) Math.cos(u), 0.f);
    }

    /**
     * North (latitude increasing) unit vector at a longitude / latitude
     *
     * @param lon Longitude in degrees
     * @param lat Latitude in degrees
     * @return Unit vector
     */
    private static Vector3f northTangent(double lon, double lat) {
        double u = Math.toRadians(lon);
        double v = Math.toRadians(lat);
        return new Vector3f((float) (-Math.cos(u) * Math.sin(v)), (float) (-Math.sin(u) * Math.sin(v)),
                (float) Math.cos(v));
    }

    @Override
    public void setTransform(org.meteoinfo.chart.jogl.Transform transform, boolean alwaysUpdateBuffers) {
        super.setTransform((org.meteoinfo.chart.jogl.Transform) transform.clone());

        if (alwaysUpdateBuffers) {
            // The GL context may have been recreated since the last frame, so the vertex
            // buffer object and the shader textures have to be generated again
            initVertexBuffer();
            this.meshKey = null;
            setBufferData();
            this.bindingTextures();
            this.compileShaderProgram();
        }
    }

    /**
     * Create the color / volume / normals textures. The previous textures of this
     * renderer are released first so repeated updates do not leak texture objects.
     */
    void bindingTextures() {
        this.releaseTextures();

        if (this.volume.getColors() == null && this.volume.getLegendScheme() != null) {
            this.volume.updateColors();
        }

        //ColorMap 2D texture
        this.colorTexture = getTextureID();
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, this.colorTexture);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, volume.getColorNum(), 1, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                ((Buffer) Buffers.newDirectByteBuffer(volume.getColors())).rewind());

        //Volume data 3D texture
        this.volumeTexture = getTextureID();
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_3D, this.volumeTexture);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        gl.glTexImage3D(
                GL_TEXTURE_3D,
                0,
                GL_LUMINANCE,
                volume.getWidth(),
                volume.getHeight(),
                volume.getDepth(),
                0,
                GL_LUMINANCE,
                GL_UNSIGNED_BYTE,
                ((Buffer) Buffers.newDirectByteBuffer(volume.getByteData())).rewind()
        );

        //Normals 3D texture
        if (this.getRayCastingType() == RayCastingType.SPECULAR) {
            this.normalsTexture = getTextureID();
            gl.glActiveTexture(GL_TEXTURE2);
            gl.glBindTexture(GL_TEXTURE_3D, this.normalsTexture);
            gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
            gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            gl.glTexImage3D(
                    GL_TEXTURE_3D,
                    0,
                    GL_RGB,
                    volume.getWidth(),
                    volume.getHeight(),
                    volume.getDepth(),
                    0,
                    GL_RGB,
                    GL_UNSIGNED_BYTE,
                    ((Buffer) Buffers.newDirectByteBuffer(volume.getNormals())).rewind()
            );
        }

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, 0);
        gl.glBindTexture(GL_TEXTURE_3D, 0);
    }

    /**
     * Release the texture objects of this renderer
     */
    private void releaseTextures() {
        if (this.colorTexture != 0) {
            gl.glDeleteTextures(1, new int[]{this.colorTexture}, 0);
            this.colorTexture = 0;
        }
        if (this.volumeTexture != 0) {
            gl.glDeleteTextures(1, new int[]{this.volumeTexture}, 0);
            this.volumeTexture = 0;
        }
        if (this.normalsTexture != 0) {
            gl.glDeleteTextures(1, new int[]{this.normalsTexture}, 0);
            this.normalsTexture = 0;
        }
    }

    private void compileShaderProgram() {
        try {
            String vertexShaderCode = Utils.loadResource("/shaders/volume/vertex.vert");
            String fragmentShaderCode = Utils.loadResource("/shaders/volume/earthVolume.frag");
            this.program = new Program("earthVolume", vertexShaderCode, fragmentShaderCode);
            this.uniformLocations.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update shaders
     */
    public void updateShaders() {
        if (program == null || program.getProgramId() == null) {
            this.bindingTextures();
            this.compileShaderProgram();
        }
    }

    int castTypeId() {
        switch (this.getRayCastingType()) {
            case BASIC:
                return 1;
            case SPECULAR:
                return 2;
            default:
                return 0;
        }
    }

    /**
     * Number of ray marching samples - two samples per data voxel along the longest
     * texture axis is enough for a smooth composite and keeps the shader cheap.
     *
     * @return Sample count
     */
    int sampleCount() {
        int n = Math.max(this.volume.getWidth(), Math.max(this.volume.getHeight(), this.volume.getDepth()));
        return Math.max(MIN_SAMPLE, Math.min(MAX_SAMPLE, n * 2));
    }

    /**
     * Get (and cache) the location of a uniform of this shader program.
     *
     * @param name Uniform name
     * @return Uniform location, -1 when the uniform is not used by the shader
     */
    private int uniformLocation(String name) {
        Integer loc = this.uniformLocations.get(name);
        if (loc == null) {
            loc = gl.glGetUniformLocation(this.program.getProgramId(), name);
            this.uniformLocations.put(name, loc);
        }
        return loc;
    }

    /**
     * Upload the uniforms of the sphere ray casting shader. The uniforms are set
     * directly with the locations of this program - the shared static registry of
     * {@link Program} cannot be used here because other renderers register their own
     * uniforms there (with locations of their own programs), which would overwrite
     * the shell definition uniforms of this shader.
     */
    void setUniforms() {
        Extent geo = geoExtent();
        double lonMin = geo.minX;
        double lonMax = geo.maxX;
        if (lonMax < lonMin) {
            lonMax += 360;
        }
        float[] aabbMin = this.volume.getAabbMin();
        float[] aabbMax = this.volume.getAabbMax();

        gl.glUniform1i(uniformLocation("orthographic"), this.orthographic ? 1 : 0);
        gl.glUniformMatrix4fv(uniformLocation("MVP"), 1, false,
                this.viewProjMatrix.get(Buffers.newDirectFloatBuffer(16)));
        gl.glUniformMatrix4fv(uniformLocation("iV"), 1, false,
                toMatrix(this.mvmatrix).invert().get(Buffers.newDirectFloatBuffer(16)));
        gl.glUniformMatrix4fv(uniformLocation("iP"), 1, false,
                toMatrix(this.projmatrix).invert().get(Buffers.newDirectFloatBuffer(16)));
        gl.glUniform2f(uniformLocation("viewSize"), this.getWidth(), this.getHeight());
        gl.glUniform2f(uniformLocation("viewShift"), this.viewport[0], this.viewport[1]);
        gl.glUniform1i(uniformLocation("depthSampleCount"), sampleCount());
        gl.glUniform1i(uniformLocation("tex"), 0);
        gl.glUniform1i(uniformLocation("colorMap"), 1);
        gl.glUniform1i(uniformLocation("normals"), 2);
        gl.glUniform1f(uniformLocation("brightness"), this.getBrightness());
        gl.glUniform1i(uniformLocation("castType"), castTypeId());
        gl.glUniform1f(uniformLocation("earthRadius"), this.radius);
        gl.glUniform1f(uniformLocation("altMin"), (float) geo.minZ);
        gl.glUniform1f(uniformLocation("altMax"), (float) geo.maxZ);
        gl.glUniform1f(uniformLocation("lonMin"), (float) Math.toRadians(lonMin));
        gl.glUniform1f(uniformLocation("lonMax"), (float) Math.toRadians(lonMax));
        gl.glUniform1f(uniformLocation("latMin"), (float) Math.toRadians(geo.minY));
        gl.glUniform1f(uniformLocation("latMax"), (float) Math.toRadians(geo.maxY));
        gl.glUniform3f(uniformLocation("aabbMin"), aabbMin[0], aabbMin[1], aabbMin[2]);
        gl.glUniform3f(uniformLocation("aabbMax"), aabbMax[0], aabbMax[1], aabbMax[2]);
    }

    @Override
    public void draw() {
        if (this.program == null) {
            this.compileShaderProgram();
        } else if (this.program.getProgramId() == null) {
            // The program was destroyed with the previous GL context - the cached
            // uniform locations do not belong to the new program object
            this.uniformLocations.clear();
        }
        try {
            ensureBufferData();

            program.use(gl);
            setUniforms();

            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo.get(0));

            // 1st attribute buffer : vertices
            gl.glEnableVertexAttribArray(0);
            gl.glVertexAttribPointer(
                    0,
                    3,
                    GL_FLOAT,
                    false,
                    3 * 4,
                    0
            );

            gl.glActiveTexture(GL_TEXTURE1);
            gl.glBindTexture(GL_TEXTURE_2D, this.colorTexture);
            gl.glActiveTexture(GL_TEXTURE0);
            gl.glBindTexture(GL_TEXTURE_3D, this.volumeTexture);
            if (this.getRayCastingType() == RayCastingType.SPECULAR) {
                gl.glActiveTexture(GL_TEXTURE2);
                gl.glBindTexture(GL_TEXTURE_3D, this.normalsTexture);
            }

            // Draw back faces only: the carrier mesh is a closed shell, so its back
            // faces cover the whole sector silhouette exactly once. The fragment
            // depth is computed from the ray marcher, so the volume is still correctly
            // occluded by the earth surface drawn before it.
            gl.glPushAttrib(GL2.GL_POLYGON_BIT | GL2.GL_DEPTH_BUFFER_BIT);
            gl.glEnable(GL2.GL_CULL_FACE);
            gl.glCullFace(GL2.GL_FRONT);

            gl.glDrawArrays(GL_TRIANGLES, 0, this.vertexNumber);

            gl.glPopAttrib();

            gl.glDisableVertexAttribArray(0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl.glActiveTexture(GL_TEXTURE0);
            gl.glBindTexture(GL_TEXTURE_2D, 0);
            gl.glBindTexture(GL_TEXTURE_3D, 0);
            gl.glUseProgram(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dispose
     */
    public void dispose() {
        gl.glDeleteBuffers(1, new int[]{vbo.get(0)}, 0);
        this.releaseTextures();
    }
}
