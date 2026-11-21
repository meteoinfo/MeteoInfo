package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.maplibre.earcut4j.Earcut;
import org.meteoinfo.chart.graphic.PuntalGraphic;
import org.meteoinfo.chart.graphic.sphere.Sphere;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.math.Matrix4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Render 3-D point graphics. Points can either be drawn as spheres or as flat
 * markers. Flat markers are billboards: their geometry is generated in
 * normalized device coordinates so that the marker symbol always faces the
 * camera and keeps a constant pixel size, exactly like the 2-D point symbols.
 * All {@link PointStyle} marker types are supported.
 */
public class PointRender extends JOGLGraphicRender {

    /** Number of floats per marker vertex: x, y, z, r, g, b, a. */
    private static final int MARKER_STRIDE = 7;

    private static final Map<PointStyle, MarkerShape> MARKER_SHAPES = createMarkerShapes();

    private PuntalGraphic graphics;
    private IntBuffer vbo;
    private Program program;
    private int pointNum;
    private int sizePosition;
    private int sizeColor;
    private int sizeNormal;
    private boolean sphere;
    private float sphereScale = 0.005f;
    private float[] vertexPosition;
    private float[] vertexNormal;
    private float[] vertexColor;
    private int[] vertexIndices;
    private List<Vector3f> objNormals;

    // Flat marker buffers. Positions and colors are interleaved.
    private IntBuffer markerVbo;
    private float[] markerFillVertex;
    private float[] markerLineVertex;
    private int markerFillVertexCount;
    private int markerLineVertexCount;
    private List<LineGroup> markerLineGroups = new ArrayList<>();
    private final float[] lastModelViewMatrix = new float[16];
    private final float[] lastProjectionMatrix = new float[16];
    private final int[] lastViewport = new int[4];
    private boolean markerDirty = true;
    private boolean dpiScaleDirty = false;
    private Extent clipExtent;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public PointRender(GL2 gl) {
        super(gl);

        if (useShader) {
            try {
                this.compileShaders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initVertexBuffer();
    }

    /**
     * Constructor
     *
     * @param gl The opengl pipeline
     * @param pointGraphics 3D point graphics
     */
    public PointRender(GL2 gl, PuntalGraphic pointGraphics) {
        this(gl);
        this.graphics = pointGraphics;
        this.sphere = this.graphics.isSphere();
        this.pointNum = ((Graphic) pointGraphics).getNumGraphics();
    }

    void updateVertex() {
        if (this.sphere) {
            this.updateSphereVertex();
        } else {
            this.markerDirty = true;
        }
    }

    @Override
    public void setDpiScale(float value) {
        if (Math.abs(this.dpiScale - value) > 1.0e-4f) {
            this.markerDirty = true;
            this.dpiScaleDirty = true;
            // Force the sphere geometry, whose radius depends on the DPI
            // scale, to be regenerated.
            this.vertexPosition = null;
        }
        super.setDpiScale(value);
    }

    /**
     * Set the extent used to clip the flat markers.
     *
     * @param value Clip extent, usually the axes extent of the plot
     */
    public void setClipExtent(Extent value) {
        if (value == null) {
            if (this.clipExtent != null) {
                this.clipExtent = null;
                this.markerDirty = true;
            }
            return;
        }
        if (this.clipExtent != null
                && this.clipExtent.minX == value.minX && this.clipExtent.maxX == value.maxX
                && this.clipExtent.minY == value.minY && this.clipExtent.maxY == value.maxY
                && this.clipExtent.minZ == value.minZ && this.clipExtent.maxZ == value.maxZ) {
            return;
        }
        this.clipExtent = (Extent) value.clone();
        this.markerDirty = true;
    }

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/surface/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/surface/surface.frag");
        program = new Program("surface", vertexShaderCode, fragmentShaderCode);
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(1);
    }

    private void updateSphereVertex() {
        List<Vector3f> vertexPositionList = new ArrayList<>();
        List<Vector3f> vertexNormalList = new ArrayList<>();
        List<Vector4f> vertexColorList = new ArrayList<>();
        List<Integer> vertexIndexList = new ArrayList<>();
        Vector3f vp;
        float size = -1;
        Sphere sphere = null;
        for (int i = 0; i < ((Graphic) this.graphics).getNumGraphics(); i++) {
            Graphic graphic = ((Graphic) this.graphics).getGraphicN(i);
            Point shape = (Point) graphic.getGeometry();
            Coordinate p = (Coordinate) shape.getCoordinate();
            PointBreak pb = (PointBreak) graphic.getLegendBreak();
            if (size != pb.getSize()) {
                size = pb.getSize();
                sphere = new Sphere(size * sphereScale * dpiScale, 36, 18);
            }
            vp = transform.transform((float) p.x, (float) p.y, (float) p.z);
            //vp = new Vector3f((float) p.X, (float) p.Y, (float) p.Z);
            Matrix4f matrix = new Matrix4f();
            matrix.translate(vp);
            List<Vector3f> vertices = sphere.getVertices();
            int n = vertices.size();
            int nAdded = vertexPositionList.size();
            for (Vector3f v : vertices) {
                vertexPositionList.add(matrix.mul(v));
            }
            List<Vector3f> normals = sphere.getNormals();
            if (vertexNormalList.isEmpty()) {
                this.objNormals = normals;
            }
            vertexNormalList.addAll(normals);
            float[] color = pb.getColor().getRGBComponents(null);
            for (int j = 0; j < n; j++) {
                vertexColorList.add(new Vector4f(color));
            }
            if (nAdded == 0) {
                vertexIndexList.addAll(sphere.getIndices());
            } else {
                for (int idx : sphere.getIndices()) {
                    vertexIndexList.add(idx + nAdded);
                }
            }
        }

        int n = vertexPositionList.size();
        this.vertexPosition = new float[n * 3];
        this.vertexNormal = new float[n * 3];
        this.vertexColor = new float[n * 4];
        Vector3f v;
        Vector4f v4;
        for (int i = 0, j = 0, k = 0; i < n; i++, j+=3, k+=4) {
            v = vertexPositionList.get(i);
            vertexPosition[j] = v.x;
            vertexPosition[j + 1] = v.y;
            vertexPosition[j + 2] = v.z;
            v = vertexNormalList.get(i);
            vertexNormal[j] = v.x;
            vertexNormal[j + 1] = v.y;
            vertexNormal[j + 2] = v.z;
            v4 = vertexColorList.get(i);
            vertexColor[k] = v4.x;
            vertexColor[k + 1] = v4.y;
            vertexColor[k + 2] = v4.z;
            vertexColor[k + 3] = v4.w;
        }
        vertexIndices = vertexIndexList.stream().mapToInt(Integer::intValue).toArray();
    }

    private void updateSphereVertexPosition() {
        List<Vector3f> vertexPositionList = new ArrayList<>();
        Vector3f vp;
        float size = -1;
        Sphere sphere = null;
        for (int i = 0; i < ((Graphic) this.graphics).getNumGraphics(); i++) {
            Graphic graphic = ((Graphic) this.graphics).getGraphicN(i);
            Point shape = (Point) graphic.getGeometry();
            Coordinate p = (Coordinate) shape.getCoordinate();
            PointBreak pb = (PointBreak) graphic.getLegendBreak();
            if (size != pb.getSize()) {
                size = pb.getSize();
                sphere = new Sphere(size * sphereScale * dpiScale, 36, 18);
            }
            vp = transform.transform((float) p.x, (float) p.y, (float) p.z);
            //vp = new Vector3f((float) p.X, (float) p.Y, (float) p.Z);
            Matrix4f matrix = new Matrix4f();
            matrix.translate(vp);
            List<Vector3f> vertices = sphere.getVertices();
            int n = vertices.size();
            for (Vector3f v : vertices) {
                vertexPositionList.add(matrix.mul(v));
            }
        }

        int n = vertexPositionList.size();
        this.vertexPosition = new float[n * 3];
        Vector3f v;
        for (int i = 0, j = 0, k = 0; i < n; i++, j+=3, k+=4) {
            v = vertexPositionList.get(i);
            vertexPosition[j] = v.x;
            vertexPosition[j + 1] = v.y;
            vertexPosition[j + 2] = v.z;
        }
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && !this.dpiScaleDirty && this.transform != null
                && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (!updateBuffer)
            return;

        if (!this.sphere) {
            this.markerDirty = true;
            this.dpiScaleDirty = false;
            return;
        }

        if (this.vertexPosition == null) {
            this.updateVertex();
        } else {
            this.updateSphereVertexPosition();
        }
        this.dpiScaleDirty = false;
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(this.vertexPosition);
        sizePosition = vertexBuffer.capacity() * Float.BYTES;

        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(vertexColor);
        sizeColor = colorBuffer.capacity() * Float.BYTES;

        FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(vertexNormal);
        sizeNormal = normalBuffer.capacity() * Float.BYTES;

        int totalSize = sizePosition + sizeColor + sizeNormal;

        vbo = GLBuffers.newDirectIntBuffer(2);
        gl.glGenBuffers(2, vbo);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, totalSize, null, GL.GL_STATIC_DRAW);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeColor, colorBuffer);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition + sizeColor, sizeNormal, normalBuffer);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        IntBuffer indexBuffer = GLBuffers.newDirectIntBuffer(this.vertexIndices);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * Integer.BYTES, indexBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    void setUniforms() {

    }

    @Override
    public void draw() {
        if (this.sphere) {
            drawSpherePoints();
        } else {
            drawMarkerPoints();
        }
    }

    /**
     * Draw points as spheres. The sphere geometry is generated in world space
     * and lit by the scene lighting.
     */
    private void drawSpherePoints() {
        if (useShader) {    //  not working now
            program.use(gl);
            setUniforms();

            gl.glUseProgram(0);
            return;
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));

        // enable vertex arrays
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
        gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        gl.glColorPointer(4, GL.GL_FLOAT, 0, sizePosition);

        gl.glPushMatrix();
        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glLoadMatrixf(this.modelViewMatrixR.get(fb));

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));

        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glNormalPointer(GL.GL_FLOAT, 0, sizePosition + sizeColor);

        gl.glDrawElements(GL2.GL_TRIANGLES, this.vertexIndices.length, GL.GL_UNSIGNED_INT, 0);

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

        gl.glPopMatrix();
    }

    /**
     * Draw points as flat marker symbols. Marker vertices are generated in
     * normalized device coordinates, so the symbols are always parallel to the
     * screen and keep a constant pixel size, independently of the camera
     * orientation and of the orthographic/perspective projection.
     */
    private void drawMarkerPoints() {
        if (this.graphics == null) {
            return;
        }

        if (((Graphic) this.graphics).getNumGraphics() != this.pointNum) {
            this.markerDirty = true;
        }

        if (markerDirty || isCameraChanged()) {
            buildMarkerVertex();
            uploadMarkerVertex();
            System.arraycopy(mvmatrix, 0, lastModelViewMatrix, 0, 16);
            System.arraycopy(projmatrix, 0, lastProjectionMatrix, 0, 16);
            System.arraycopy(viewport, 0, lastViewport, 0, 4);
            markerDirty = false;
        }

        if (markerFillVertexCount == 0 && markerLineVertexCount == 0) {
            return;
        }

        boolean lightEnabled = this.lighting.isEnable();
        if (lightEnabled) {
            this.lighting.stop(gl);
        }
        boolean cullEnabled = gl.glIsEnabled(GL.GL_CULL_FACE);
        if (cullEnabled) {
            gl.glDisable(GL.GL_CULL_FACE);
        }
        // Marker vertices are given in normalized device coordinates, so the
        // eye space clip planes of the plot do not apply to them. Disable the
        // clip planes while the markers are drawn and restore them afterwards.
        int[] clipPlanes = new int[]{GL2.GL_CLIP_PLANE0, GL2.GL_CLIP_PLANE1,
                GL2.GL_CLIP_PLANE2, GL2.GL_CLIP_PLANE3, GL2.GL_CLIP_PLANE4,
                GL2.GL_CLIP_PLANE5};
        boolean[] clipPlaneStates = new boolean[clipPlanes.length];
        for (int i = 0; i < clipPlanes.length; i++) {
            clipPlaneStates[i] = gl.glIsEnabled(clipPlanes[i]);
            if (clipPlaneStates[i]) {
                gl.glDisable(clipPlanes[i]);
            }
        }

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

        if (markerFillVertexCount > 0) {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, markerVbo.get(0));
            gl.glVertexPointer(3, GL.GL_FLOAT, MARKER_STRIDE * Float.BYTES, 0);
            gl.glColorPointer(4, GL.GL_FLOAT, MARKER_STRIDE * Float.BYTES, 3 * Float.BYTES);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, markerFillVertexCount);
        }

        if (markerLineVertexCount > 0) {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, markerVbo.get(1));
            gl.glVertexPointer(3, GL.GL_FLOAT, MARKER_STRIDE * Float.BYTES, 0);
            gl.glColorPointer(4, GL.GL_FLOAT, MARKER_STRIDE * Float.BYTES, 3 * Float.BYTES);
            for (LineGroup group : markerLineGroups) {
                gl.glLineWidth(group.width);
                gl.glDrawArrays(GL.GL_LINES, group.start, group.count);
            }
        }

        gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        for (int i = 0; i < clipPlanes.length; i++) {
            if (clipPlaneStates[i]) {
                gl.glEnable(clipPlanes[i]);
            }
        }
        if (cullEnabled) {
            gl.glEnable(GL.GL_CULL_FACE);
        }
        if (lightEnabled) {
            this.lighting.start(gl);
        }
    }

    /**
     * Whether the camera (or the viewport/projection) has changed since the
     * marker vertices were generated.
     *
     * @return boolean
     */
    private boolean isCameraChanged() {
        return !Arrays.equals(this.mvmatrix, this.lastModelViewMatrix)
                || !Arrays.equals(this.projmatrix, this.lastProjectionMatrix)
                || !Arrays.equals(this.viewport, this.lastViewport);
    }

    /**
     * Generate the normalized device coordinate marker geometry for all points.
     */
    private void buildMarkerVertex() {
        FloatBuilder fillBuilder = new FloatBuilder();
        FloatBuilder lineBuilder = new FloatBuilder();
        List<LineGroup> lineGroups = new ArrayList<>();

        List<Graphic> gs = new ArrayList<>();
        for (int i = 0; i < ((Graphic) this.graphics).getNumGraphics(); i++) {
            gs.add(((Graphic) this.graphics).getGraphicN(i));
        }
        this.pointNum = gs.size();
        Extent cullExtent = this.clipExtent;
        if (cullExtent == null && this.transform != null) {
            cullExtent = this.transform.getExtent();
        }
        for (Graphic graphic : gs) {
            if (!(graphic.getGeometry() instanceof Point)) {
                continue;
            }
            if (!(graphic.getLegendBreak() instanceof PointBreak)) {
                continue;
            }
            Point shape = (Point) graphic.getGeometry();
            Coordinate coordinate = shape.getCoordinate();
            if (coordinate == null || Double.isNaN(coordinate.x) || Double.isNaN(coordinate.y)) {
                continue;
            }
            PointBreak pb = (PointBreak) graphic.getLegendBreak();
            MarkerShape marker = getMarkerShape(pb.getStyle());

            double xValue = coordinate.x;
            double yValue = coordinate.y;
            double zValue = Double.isNaN(coordinate.z) ? 0.0 : coordinate.z;
            if (cullExtent != null && (xValue < cullExtent.minX || xValue > cullExtent.maxX
                    || yValue < cullExtent.minY || yValue > cullExtent.maxY
                    || zValue < cullExtent.minZ || zValue > cullExtent.maxZ)) {
                continue;
            }

            // The scene model view matrix already applies the plot transform
            // (center, scale, rotation), so the raw data coordinates are used
            // here, exactly like the other non-sphere renderers.
            Vector4f clip = this.viewProjMatrix.transform(new Vector4f(
                    (float) xValue,
                    (float) yValue,
                    (float) zValue,
                    1.0f));
            if (!(clip.w > 1.0e-6f)) {
                continue;
            }
            float ndcX = clip.x / clip.w;
            float ndcY = clip.y / clip.w;
            float ndcZ = clip.z / clip.w;
            if (Float.isNaN(ndcX) || Float.isNaN(ndcY) || Float.isNaN(ndcZ)
                    || ndcZ < -1.0f || ndcZ > 1.0f) {
                continue;
            }
            float markerSize = pb.getSize() * this.dpiScale;
            if (!(markerSize > 0) || Float.isNaN(markerSize) || Float.isInfinite(markerSize)) {
                continue;
            }

            // Positive angles are clockwise on screen, consistent with the 2-D
            // point symbol rendering.
            float angle = (float) Math.toRadians(-pb.getAngle());
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float[] fillColor = pb.getColor().getRGBComponents(null);
            float[] outlineColor = pb.getOutlineColor().getRGBComponents(null);
            float lineWidth = Math.max(1.0f, pb.getOutlineSize() * this.dpiScale);
            boolean drawFill = pb.isDrawFill();
            boolean drawOutline = pb.isDrawOutline();
            boolean drawLines = drawFill || drawOutline;

            if (drawFill) {
                for (int i = 0; i < marker.fillRings.size(); i++) {
                    float[] ring = marker.fillRings.get(i);
                    int[] indices = marker.fillIndices.get(i);
                    for (int index : indices) {
                        addMarkerVertex(fillBuilder, ndcX, ndcY, ndcZ, ring, index, markerSize,
                                cos, sin, fillColor);
                    }
                }
            }

            if (drawOutline) {
                for (float[] ring : marker.outlineRings) {
                    int start = lineBuilder.size();
                    int pointCount = ring.length / 2;
                    for (int i = 0; i < pointCount; i++) {
                        int next = (i + 1) % pointCount;
                        addMarkerVertex(lineBuilder, ndcX, ndcY, ndcZ, ring, i, markerSize,
                                cos, sin, outlineColor);
                        addMarkerVertex(lineBuilder, ndcX, ndcY, ndcZ, ring, next, markerSize,
                                cos, sin, outlineColor);
                    }
                    addLineGroup(lineGroups, start, lineBuilder.size(), lineWidth);
                }
            }

            if (drawLines) {
                for (float[] segment : marker.lineSegments) {
                    int start = lineBuilder.size();
                    addMarkerVertex(lineBuilder, ndcX, ndcY, ndcZ, segment, 0, markerSize,
                            cos, sin, fillColor);
                    addMarkerVertex(lineBuilder, ndcX, ndcY, ndcZ, segment, 1, markerSize,
                            cos, sin, fillColor);
                    addLineGroup(lineGroups, start, lineBuilder.size(), lineWidth);
                }
            }
        }

        this.markerFillVertex = fillBuilder.toArray();
        this.markerLineVertex = lineBuilder.toArray();
        this.markerFillVertexCount = this.markerFillVertex.length / MARKER_STRIDE;
        this.markerLineVertexCount = this.markerLineVertex.length / MARKER_STRIDE;
        this.markerLineGroups = lineGroups;
    }

    /**
     * Add one marker vertex to a float builder. Marker coordinates are
     * normalized to a one unit diameter symbol, so they are first converted to
     * pixels and then to normalized device coordinates. This makes the marker
     * size independent of the camera orientation and projection.
     *
     * @param builder Vertex builder
     * @param ndcX Marker center X in normalized device coordinates
     * @param ndcY Marker center Y in normalized device coordinates
     * @param ndcZ Marker center Z in normalized device coordinates
     * @param points Marker points (pairs of x/y values)
     * @param index Point index in the points array
     * @param size Marker size in pixels
     * @param cos Cosine of the marker rotation angle
     * @param sin Sine of the marker rotation angle
     * @param color RGBA color components
     */
    private void addMarkerVertex(FloatBuilder builder, float ndcX, float ndcY, float ndcZ,
                                 float[] points, int index, float size, float cos, float sin,
                                 float[] color) {
        float pixelX = (points[index * 2] * cos - points[index * 2 + 1] * sin) * size;
        float pixelY = (points[index * 2] * sin + points[index * 2 + 1] * cos) * size;
        int width = this.viewport[2] > 0 ? this.viewport[2] : 1;
        int height = this.viewport[3] > 0 ? this.viewport[3] : 1;
        builder.add(ndcX + pixelX * 2.0f / width)
                .add(ndcY + pixelY * 2.0f / height)
                .add(ndcZ)
                .add(color[0])
                .add(color[1])
                .add(color[2])
                .add(color[3]);
    }

    /**
     * Append a range of line vertices to the line group list. Neighboring
     * ranges with the same line width are merged into a single draw call.
     *
     * @param groups Line group list
     * @param start Start vertex index
     * @param end End vertex index (exclusive)
     * @param width Line width
     */
    private static void addLineGroup(List<LineGroup> groups, int start, int end, float width) {
        if (end <= start) {
            return;
        }
        if (!groups.isEmpty()) {
            LineGroup last = groups.get(groups.size() - 1);
            if (Float.compare(last.width, width) == 0 && last.start + last.count == start) {
                last.count += end - start;
                return;
            }
        }
        groups.add(new LineGroup(start, end - start, width));
    }

    /**
     * Upload the generated marker vertices to the GPU.
     */
    private void uploadMarkerVertex() {
        if (this.markerVbo == null) {
            this.markerVbo = GLBuffers.newDirectIntBuffer(2);
            gl.glGenBuffers(2, markerVbo);
        }
        uploadMarkerBuffer(markerVbo.get(0), this.markerFillVertex);
        uploadMarkerBuffer(markerVbo.get(1), this.markerLineVertex);
    }

    private void uploadMarkerBuffer(int bufferName, float[] vertices) {
        if (vertices == null || vertices.length == 0) {
            return;
        }
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferName);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertices);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) vertexBuffer.capacity() * Float.BYTES,
                vertexBuffer, GL.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Get the unit marker shape of a point style.
     *
     * @param style Point style
     * @return Marker shape
     */
    private static MarkerShape getMarkerShape(PointStyle style) {
        MarkerShape shape = style == null ? null : MARKER_SHAPES.get(style);
        if (shape == null) {
            shape = MARKER_SHAPES.get(PointStyle.CIRCLE);
        }
        return shape;
    }

    private static Map<PointStyle, MarkerShape> createMarkerShapes() {
        Map<PointStyle, MarkerShape> shapes = new EnumMap<>(PointStyle.class);

        MarkerShape shape = new MarkerShape();
        float[] circle = circleRing(0.5f, 32);
        shape.addFillRing(circle);
        shape.addOutlineRing(circle);
        shapes.put(PointStyle.CIRCLE, shape);

        shape = new MarkerShape();
        shape.addFillRing(circleRing(0.5f, 32));
        shape.addOutlineRing(circleRing(0.5f, 32));
        shape.addOutlineRing(circleRing(0.3f, 24));
        shapes.put(PointStyle.DOUBLE_CIRCLE, shape);

        shape = new MarkerShape();
        shape.addFillRing(new float[]{-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f});
        shape.addOutlineRing(new float[]{-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f});
        shapes.put(PointStyle.SQUARE, shape);

        shape = new MarkerShape();
        float[] diamond = new float[]{0.0f, -0.5f, 0.5f, 0.0f, 0.0f, 0.5f, -0.5f, 0.0f};
        shape.addFillRing(diamond);
        shape.addOutlineRing(diamond);
        shapes.put(PointStyle.DIAMOND, shape);

        shape = new MarkerShape();
        float[] upTriangle = new float[]{-0.4330127f, -0.25f, 0.4330127f, -0.25f, 0.0f, 0.5f};
        shape.addFillRing(upTriangle);
        shape.addOutlineRing(upTriangle);
        shapes.put(PointStyle.UP_TRIANGLE, shape);

        shape = new MarkerShape();
        float[] downTriangle = new float[]{-0.4330127f, 0.25f, 0.4330127f, 0.25f, 0.0f, -0.5f};
        shape.addFillRing(downTriangle);
        shape.addOutlineRing(downTriangle);
        shapes.put(PointStyle.DOWN_TRIANGLE, shape);

        shape = new MarkerShape();
        shape.addLineSegment(-0.5f, -0.5f, 0.5f, 0.5f);
        shape.addLineSegment(-0.5f, 0.5f, 0.5f, -0.5f);
        shapes.put(PointStyle.X_CROSS, shape);

        shape = new MarkerShape();
        shape.addLineSegment(0.0f, -0.5f, 0.0f, 0.5f);
        shape.addLineSegment(-0.5f, 0.0f, 0.5f, 0.0f);
        shapes.put(PointStyle.PLUS, shape);

        shape = new MarkerShape();
        shape.addLineSegment(-0.5f, 0.0f, 0.5f, 0.0f);
        shapes.put(PointStyle.MINUS, shape);

        shape = new MarkerShape();
        shape.addLineSegment(-0.5f, -0.5f, 0.5f, 0.5f);
        shape.addLineSegment(-0.5f, 0.5f, 0.5f, -0.5f);
        shape.addLineSegment(0.0f, -0.5f, 0.0f, 0.5f);
        shape.addLineSegment(-0.5f, 0.0f, 0.5f, 0.0f);
        shapes.put(PointStyle.STAR_LINES, shape);

        shape = new MarkerShape();
        float[] star = starRing(0.5f, 0.5f * 0.381966f);
        shape.addFillRing(star);
        shape.addOutlineRing(star);
        shapes.put(PointStyle.STAR, shape);

        shape = new MarkerShape();
        shape.addFillRing(starRing(0.4f, 0.4f * 0.381966f));
        shape.addOutlineRing(circleRing(0.5f, 32));
        shapes.put(PointStyle.CIRCLE_STAR, shape);

        shape = new MarkerShape();
        float[] pentagon = regularRing(0.5f, 5, Math.PI / 2);
        shape.addFillRing(pentagon);
        shape.addOutlineRing(pentagon);
        shapes.put(PointStyle.PENTAGON, shape);

        shape = new MarkerShape();
        float[] upSemiCircle = semiCircleRing(0.5f, true, 32);
        shape.addFillRing(upSemiCircle);
        shape.addOutlineRing(upSemiCircle);
        shapes.put(PointStyle.UP_SEMI_CIRCLE, shape);

        shape = new MarkerShape();
        float[] downSemiCircle = semiCircleRing(0.5f, false, 32);
        shape.addFillRing(downSemiCircle);
        shape.addOutlineRing(downSemiCircle);
        shapes.put(PointStyle.DOWN_SEMI_CIRCLE, shape);

        return shapes;
    }

    /**
     * Create a circle ring centered at the origin.
     *
     * @param radius Circle radius
     * @param segments Number of segments
     * @return Ring points
     */
    private static float[] circleRing(float radius, int segments) {
        float[] ring = new float[segments * 2];
        for (int i = 0; i < segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            ring[i * 2] = (float) (Math.cos(angle) * radius);
            ring[i * 2 + 1] = (float) (Math.sin(angle) * radius);
        }
        return ring;
    }

    /**
     * Create a regular polygon ring centered at the origin.
     *
     * @param radius Circumradius
     * @param points Point number
     * @param startAngle Start angle in radians
     * @return Ring points
     */
    private static float[] regularRing(float radius, int points, double startAngle) {
        float[] ring = new float[points * 2];
        for (int i = 0; i < points; i++) {
            double angle = startAngle + 2.0 * Math.PI * i / points;
            ring[i * 2] = (float) (Math.cos(angle) * radius);
            ring[i * 2 + 1] = (float) (Math.sin(angle) * radius);
        }
        return ring;
    }

    /**
     * Create a five point star ring.
     *
     * @param outerRadius Outer radius
     * @param innerRadius Inner radius
     * @return Ring points
     */
    private static float[] starRing(float outerRadius, float innerRadius) {
        float[] ring = new float[20];
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            float radius = i % 2 == 0 ? outerRadius : innerRadius;
            ring[i * 2] = (float) (Math.cos(angle) * radius);
            ring[i * 2 + 1] = (float) (Math.sin(angle) * radius);
        }
        return ring;
    }

    /**
     * Create an upper or lower semi-circle ring.
     *
     * @param radius Circle radius
     * @param up Whether the upper half is used
     * @param segments Number of segments
     * @return Ring points
     */
    private static float[] semiCircleRing(float radius, boolean up, int segments) {
        float[] ring = new float[(segments + 1) * 2];
        double start = up ? 0.0 : Math.PI;
        for (int i = 0; i <= segments; i++) {
            double angle = start + Math.PI * i / segments;
            ring[i * 2] = (float) (Math.cos(angle) * radius);
            ring[i * 2 + 1] = (float) (Math.sin(angle) * radius);
        }
        return ring;
    }

    /**
     * Triangulate a simple ring with the ear clipping algorithm.
     *
     * @param ring Ring points
     * @return Triangle vertex indices
     */
    private static int[] triangulate(float[] ring) {
        double[] coordinates = new double[ring.length];
        for (int i = 0; i < ring.length; i++) {
            coordinates[i] = ring[i];
        }
        List<Integer> indices = Earcut.earcut(coordinates, null, 2);
        int[] result = new int[indices.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = indices.get(i);
        }
        return result;
    }

    /**
     * Unit marker shape. Shape coordinates are normalized to a one unit
     * diameter symbol centered at the origin.
     */
    private static class MarkerShape {

        private final List<float[]> fillRings = new ArrayList<>();
        private final List<int[]> fillIndices = new ArrayList<>();
        private final List<float[]> outlineRings = new ArrayList<>();
        private final List<float[]> lineSegments = new ArrayList<>();

        private void addFillRing(float[] ring) {
            fillRings.add(ring);
            fillIndices.add(triangulate(ring));
        }

        private void addOutlineRing(float[] ring) {
            outlineRings.add(ring);
        }

        private void addLineSegment(float x1, float y1, float x2, float y2) {
            lineSegments.add(new float[]{x1, y1, x2, y2});
        }
    }

    /**
     * A contiguous range of line vertices sharing the same line width.
     */
    private static class LineGroup {

        private final int start;
        private int count;
        private final float width;

        private LineGroup(int start, int count, float width) {
            this.start = start;
            this.count = count;
            this.width = width;
        }
    }

    /**
     * Simple growable float array.
     */
    private static class FloatBuilder {

        private float[] data = new float[64];
        private int size;

        private FloatBuilder add(float value) {
            if (size == data.length) {
                data = Arrays.copyOf(data, data.length * 2);
            }
            data[size++] = value;
            return this;
        }

        private int size() {
            return size;
        }

        private float[] toArray() {
            return Arrays.copyOf(data, size);
        }
    }
}
