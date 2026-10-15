package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.meteoinfo.chart.graphic.PolygonGraphic;
import org.meteoinfo.chart.jogl.tessellator.TriangleTessellator;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.Polygon;
import org.meteoinfo.geometry.MultiPolygon;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * Pure Cartesian polygon renderer with VBO caching.
 * Performs EarCut tessellation only (no spherical subdivision).
 * Vertices are used as-is in 3D Euclidean space.
 */
public class PolygonRenderBack extends JOGLGraphicRender {

    private PolygonGraphic polygonGraphic;
    private int vboId;
    private int vertexCount;
    private boolean dirty = true;

    // Interleaved vertex stride: x,y,z, nx,ny,nz = 6 floats = 24 bytes
    private static final int STRIDE_BYTES = 24;
    private static final int NORMAL_OFFSET = 12;

    public PolygonRenderBack(GL2 gl) {
        super(gl);
        useShader = false;
    }

    public PolygonRenderBack(GL2 gl, PolygonGraphic polygonGraphic) {
        this(gl);
        this.polygonGraphic = polygonGraphic;
    }

    /** Mark VBO as needing rebuild. Safe to call from any thread. */
    public void invalidate() {
        this.dirty = true;
    }

    // ======================== VBO Build ========================

    private void buildVbo() {
        if (vboId != 0) {
            gl.glDeleteBuffers(1, new int[]{vboId}, 0);
            vboId = 0;
            vertexCount = 0;
        }

        List<Polygon> polygons = ((MultiPolygon) polygonGraphic.getShape()).getPolygons();
        if (polygons == null || polygons.isEmpty()) return;

        FloatBuffer buffer = GLBuffers.newDirectFloatBuffer(estimateCapacity(polygons));

        for (Polygon polygon : polygons) {
            List<Coordinate[]> triangles = TriangleTessellator.tessellate(polygon);
            for (Coordinate[] tri : triangles) {
                // Flat shading normal: compute once per triangle
                float[] normal = computeFaceNormal(tri[0], tri[1], tri[2]);
                emitVertex(buffer, tri[0], normal);
                emitVertex(buffer, tri[1], normal);
                emitVertex(buffer, tri[2], normal);
            }
        }

        buffer.flip();
        vertexCount = buffer.remaining() / 6;
        if (vertexCount == 0) return;

        int[] vbos = new int[1];
        gl.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, buffer.remaining() * 4L, buffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        dirty = false;
    }

    private int estimateCapacity(List<Polygon> polygons) {
        int totalVerts = 0;
        for (Polygon p : polygons) {
            totalVerts += p.getOutLine().size() * 3;
            if (p.hasHole()) {
                for (int i = 0; i < p.getNumInteriorRing(); i++) {
                    totalVerts += p.getHoleLine(i).size() * 3;
                }
            }
        }
        return Math.max(totalVerts * 6 * 2, 2048);
    }

    // ======================== Vertex Packing ========================

    /**
     * Computes flat-shading face normal via cross product.
     * Returns normalized float[3]. No sphere projection involved.
     */
    private float[] computeFaceNormal(Coordinate a, Coordinate b, Coordinate c) {
        double ux = b.x - a.x, uy = b.y - a.y, uz = b.z - a.z;
        double vx = c.x - a.x, vy = c.y - a.y, vz = c.z - a.z;

        double nx = uy * vz - uz * vy;
        double ny = uz * vx - ux * vz;
        double nz = ux * vy - uy * vx;

        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-15) return new float[]{0, 0, 1};

        float invLen = (float) (1.0 / len);
        return new float[]{(float) nx * invLen, (float) ny * invLen, (float) nz * invLen};
    }

    private void emitVertex(FloatBuffer buf, Coordinate p, float[] normal) {
        buf.put((float) p.x).put((float) p.y).put((float) p.z);
        buf.put(normal[0]).put(normal[1]).put(normal[2]);
    }

    // ======================== Draw ========================

    @Override
    public void draw() {
        if (polygonGraphic == null || !polygonGraphic.isVisible()) return;

        if (dirty || vboId == 0) buildVbo();
        if (vboId == 0 || vertexCount == 0) return;

        PolygonBreak pb = (PolygonBreak) polygonGraphic.getLegendBreak();

        // ---- Fill ----
        if (pb.isDrawFill() && pb.getColor().getAlpha() > 0) {
            float[] rgba = pb.getColor().getRGBComponents(null);

            gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
            gl.glPolygonOffset(-1.0f, -1.0f);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId);
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);

            gl.glVertexPointer(3, GL.GL_FLOAT, STRIDE_BYTES, 0);
            gl.glNormalPointer(GL.GL_FLOAT, STRIDE_BYTES, NORMAL_OFFSET);

            gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount);

            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
        }

        // ---- Outline ----
        if (pb.isDrawOutline()) {
            float[] outRgba = pb.getOutlineColor().getRGBComponents(null);
            gl.glColor4fv(outRgba, 0);
            gl.glLineWidth(pb.getOutlineSize() * this.dpiScale);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboId);
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL.GL_FLOAT, STRIDE_BYTES, 0);

            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount);
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }

    public void dispose() {
        if (vboId != 0) {
            gl.glDeleteBuffers(1, new int[]{vboId}, 0);
            vboId = 0;
            vertexCount = 0;
        }
    }
}