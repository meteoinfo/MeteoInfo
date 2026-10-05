package org.meteoinfo.chart.jogl.tessellator;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;
import org.meteoinfo.common.PointZ;
import org.meteoinfo.geometry.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * A robust polygon tessellator using JOGL's GLU library.
 * Converts complex, concave, or holed polygons into OpenGL primitives (triangles).
 *
 * Key improvements for spherical rendering:
 * 1. Enabled GLU_TESS_COMBINE callback to handle self-intersecting and complex polygons.
 * 2. Removed unnecessary Thread.sleep blocking (GLU tessellation is synchronous).
 * 3. Added explicit normal vector calculation for correct 3D projection on curved surfaces.
 * 4. Reused GLUtessellator instance to prevent memory leaks and improve performance.
 */
public class PrimitiveTessellator {

    private final GLU glu = new GLU();
    // Reuse a single tessellator instance to avoid garbage collection overhead
    private final GLUtessellator tobj = glu.gluNewTess();
    private final TessellationCallback tessCallback = new TessellationCallback();

    /**
     * Tessellates a PolygonZ into a list of OpenGL Primitives (usually triangles).
     * This method is synchronous and blocks until the tessellation is complete.
     *
     * @param polygon The PolygonZ shape to tessellate (vertices should be in 3D ECEF space for globe rendering).
     * @return A list of Primitive objects containing the tessellated vertices.
     * @throws TesselationException If the GLU tessellator encounters an unrecoverable error.
     */
    public List<Primitive> getPrimitives(Polygon polygon) throws TesselationException {
        // Reset callback state before starting a new tessellation
        tessCallback.reset();

        // Perform the actual tessellation (this is synchronous in GLU)
        makePrimitives(polygon);

        // Check if the tessellation callback recorded any errors
        if (tessCallback.err == null) {
            return tessCallback.primitives;
        } else {
            throw tessCallback.err;
        }
    }

    /**
     * Configures the GLU tessellator and feeds the polygon contours (outer boundary and holes).
     *
     * @param polygon The polygon to process.
     */
    private void makePrimitives(Polygon polygon) {
        // Register all necessary callbacks
        glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);

        // CRITICAL: Enable COMBINE callback to handle self-intersections and complex merges
        glu.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, tessCallback);

        // Set tessellation properties
        // GLU_TESS_WINDING_ODD ensures standard polygon filling rules
        glu.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);

        // Calculate and set the normal vector for the polygon.
        // This tells GLU how to project the 3D vertices onto a 2D plane for triangulation.
        // For spherical polygons, computing an average normal from the first 3 non-collinear vertices is usually sufficient.
        double[] normal = computePolygonNormal(polygon);
        glu.gluTessNormal(tobj, normal[0], normal[1], normal[2]);

        // Begin polygon definition
        glu.gluTessBeginPolygon(tobj, null);

        // 1. Process the outer boundary
        glu.gluTessBeginContour(tobj);
        List<PointZ> outline = (List<PointZ>) polygon.getOutLine();
        for (int i = 0; i < outline.size(); i++) {
            PointZ p = outline.get(i);
            // Skip the last point if it's identical to the first point (closed loop redundancy)
            if (i == outline.size() - 1 && i > 0) {
                PointZ first = outline.get(0);
                if (p.X == first.X && p.Y == first.Y && p.Z == first.Z) {
                    continue;
                }
            }
            double[] v = p.toArray();
            glu.gluTessVertex(tobj, v, 0, v);
        }
        glu.gluTessEndContour(tobj);

        // 2. Process inner holes (if any)
        if (polygon.hasHole()) {
            for (int i = 0; i < polygon.getHoleLineNumber(); i++) {
                glu.gluTessBeginContour(tobj);
                List<PointZ> holeLine = (List<PointZ>) polygon.getHoleLine(i);
                for (int j = 0; j < holeLine.size(); j++) {
                    PointZ p = holeLine.get(j);
                    // Skip redundant closing point
                    if (j == holeLine.size() - 1 && j > 0) {
                        PointZ first = holeLine.get(0);
                        if (p.X == first.X && p.Y == first.Y && p.Z == first.Z) {
                            continue;
                        }
                    }
                    double[] v = p.toArray();
                    glu.gluTessVertex(tobj, v, 0, v);
                }
                glu.gluTessEndContour(tobj);
            }
        }

        // End polygon definition (triggers the callbacks synchronously)
        glu.gluTessEndPolygon(tobj);

        // Note: Do NOT call glu.gluDeleteTess(tobj) here if we want to reuse the instance!
    }

    /**
     * Computes an approximate normal vector for the polygon.
     * Uses Newell's method or simple cross product of the first 3 vertices.
     *
     * @param polygon The polygon to compute the normal for.
     * @return A normalized double array [nx, ny, nz].
     */
    private double[] computePolygonNormal(Polygon polygon) {
        List<PointZ> outline = (List<PointZ>) polygon.getOutLine();
        if (outline.size() < 3) {
            return new double[]{0, 0, 1}; // Fallback for degenerate polygons
        }

        // Use Newell's method for a more robust normal calculation on non-planar polygons
        double nx = 0, ny = 0, nz = 0;
        for (int i = 0; i < outline.size(); i++) {
            PointZ current = outline.get(i);
            PointZ next = outline.get((i + 1) % outline.size());

            nx += (current.Y - next.Y) * (current.Z + next.Z);
            ny += (current.Z - next.Z) * (current.X + next.X);
            nz += (current.X - next.X) * (current.Y + next.Y);
        }

        double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 1e-10) {
            return new double[]{nx / length, ny / length, nz / length};
        }

        // Fallback if Newell's method yields a zero vector
        return new double[]{0, 0, 1};
    }

    /**
     * Custom exception for tessellation errors.
     */
    public static class TesselationException extends Exception {
        private static final long serialVersionUID = 1L;

        public TesselationException(String message) {
            super(message);
        }
    }

    /**
     * Implementation of GLUtessellatorCallbackAdapter.
     * Captures the generated primitives and handles vertex combination for complex polygons.
     */
    class TessellationCallback extends GLUtessellatorCallbackAdapter {

        protected List<Primitive> primitives = new ArrayList<>();
        protected TesselationException err = null;

        // Keep track of dynamically created vertices to prevent garbage collection issues
        private final List<double[]> combinedVertices = new ArrayList<>();

        /**
         * Resets the callback state before a new tessellation run.
         */
        public void reset() {
            primitives.clear();
            err = null;
            combinedVertices.clear();
        }

        @Override
        public void begin(int type) {
            this.primitives.add(new Primitive(type));
        }

        @Override
        public void end() {
            // Tessellation of the current primitive is complete
        }

        @Override
        public void vertex(Object vertexData) {
            if (vertexData instanceof double[]) {
                double[] coords = (double[]) vertexData;
                // Create a PointZ from the 3D coordinates
                PointZ p = new PointZ(coords[0], coords[1], coords[2]);
                this.getLastPrimitive().vertices.add(p);
            }
        }

        /**
         * CRITICAL CALLBACK: Called when GLU needs to create a new vertex at an intersection.
         * Without this, complex or self-intersecting polygons will fail to tessellate.
         */
        @Override
        public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
            // Create a new vertex at the intersection coordinates
            double[] newVertex = new double[]{coords[0], coords[1], coords[2]};

            // Store the reference to prevent it from being garbage collected before GLU finishes
            combinedVertices.add(newVertex);

            // Pass the new vertex back to GLU
            outData[0] = newVertex;
        }

        @Override
        public void error(int errnum) {
            String errMsg = "GLU Tessellation Error: " + glu.gluErrorString(errnum);
            System.err.println(errMsg);
            this.err = new TesselationException(errMsg);
        }

        private Primitive getLastPrimitive() {
            if (primitives.isEmpty()) {
                // Fallback in case begin() wasn't called (shouldn't happen)
                primitives.add(new Primitive(com.jogamp.opengl.GL.GL_TRIANGLES));
            }
            return primitives.get(primitives.size() - 1);
        }
    }
}