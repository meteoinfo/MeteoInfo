package org.meteoinfo.chart.jogl.tessellator;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PolygonZ;

import java.util.ArrayList;
import java.util.List;

public class PrimitiveTessellator {
    private final GLU glu = new GLU();
    private final GLUtessellator tobj = glu.gluNewTess();
    private final TessellationCallback tessCallback = new TessellationCallback();

    private TesselationException err;

    /*
     * Either you kindly wait for your triangles.
     */

    private boolean done = false;

    /**
     * Tessellate a shape, transform it to a set of triangles. This method locks
     * until rendering is completed.
     *
     * @param shape
     *            The shape to tessellate.
     * @param hole
     *            Shape of the hole in the tessellation (optional).
     * @return A list of triangles that constitutes the shape.
     * @throws TesselationException
     *             Throws {@link TesselationException} if the tessellation was
     *             unsuccessful, most commonly due to ambiguous shapes
     */
    public List<Primitive> getPrimitives(PolygonZ polygon)
            throws TesselationException {

        makePrimitives(polygon);

        try {
            while (!done) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (err == null) {
            return tessCallback.primitives;
        } else
            throw err;
    }

    /*
     * Or you may implement a listener interface and handle an asynchronous
     * call.
     */

    private TessellatorListener listener;

    /**
     * Tessellates a shape that is, tiling of the shape in a pattern of
     * triangles that fills the shape with no overlaps and no gaps. Returns a
     * set of triangles to the given {@link TessellatorListener} whenever done.
     *
     * @param shape
     *            The shape to tessellate.
     * @param hole
     *            Shape of the hole in the tessellation (optional).
     * @param listener
     *            Instance of {@link TessellatorListener} that will be invoked
     *            whenever tessellation is done or fails.
     */
    public void getPrimitives(PolygonZ polygon,
                             TessellatorListener listener) {
        this.listener = listener;
        makePrimitives(polygon);
    }

    public interface TessellatorListener {
        public void onTesselationDone(List<Primitive> primitives);

        public void onTesselationError(TesselationException err);
    }

    public class TesselationException extends Exception {
        private static final long serialVersionUID = 1L;

        public TesselationException(String message) {
            super(message);
        }
    }

    /**
     * This method register an instance of {@link TessellationCallback} which
     * will produce triangles given an outer- and an optional inner- (hollow)
     * {@link Shape}. {@link TessellationCallback} will report status to callers
     * whenever done.
     *
     * @param shape
     *            The to shape to tessellate. This shape can not be a complex
     *            type, neither contain holes. In such case a
     *            {@link TesselationException} will be thrown.
     * @param hole
     *            A subsection of the shape that will not be included in the
     *            resulting triangles area. The hole's contour may not exceed
     *            the contour of the shape or a {@link TesselationException}
     *            will be thrown.
     *
     */
    private void makePrimitives(PolygonZ polygon) {
        glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);
        //glu.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, tessCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);

        glu.gluTessBeginPolygon(tobj, null);

        glu.gluTessBeginContour(tobj);

        double[] v;
        PointZ p;
        for (int i = 0; i < polygon.getOutLine().size() - 1; i++) {
            p = ((List<PointZ>) polygon.getOutLine()).get(i);
            v = p.toArray();
            glu.gluTessVertex(tobj, v, 0, v);
        }

        glu.gluTessEndContour(tobj);

        if (polygon.hasHole()) {
            for (int i = 0; i < polygon.getHoleLineNumber(); i++) {
                glu.gluTessBeginContour(tobj);
                for (int j = 0; j < polygon.getHoleLine(i).size() - 1; j++) {
                    p = ((List<PointZ>) polygon.getHoleLine(i)).get(j);
                    v = p.toArray();
                    glu.gluTessVertex(tobj, v, 0, v);
                }
                glu.gluTessEndContour(tobj);
            }
        }

        glu.gluTessEndPolygon(tobj);

        glu.gluDeleteTess(tobj);

    }

    /**
     * Implementation of {@link GLUtessellatorCallback} thats responsible for
     * creating {@link Triangle}s from {@link GLU}'s tessellation callbacks and
     * reporting back to {@link TessellatorListener}s whenever done or an error
     * occurs.
     *
     * @author nomis
     *
     */
    class TessellationCallback extends GLUtessellatorCallbackAdapter {

        protected List<Primitive> primitives = new ArrayList<>();

        private PointZ p1, p2, p3;

        private int geometricPrimitiveType;

        public void begin(int type) {
            this.primitives.add(new Primitive(type));
        }

        public void end() {
            sendSuccess();
        }

        public void vertex(Object vertexData) {
            PointZ coords = new PointZ((double[]) vertexData);
            this.getLastPrimitive().vertices.add(coords);
        }

        public void combine(double[] coords, Object[] data, float[] weight,
                            Object[] outData) {
            sendError("Self-intersecting polygons not supported");
        }

        public void error(int errnum) {
            sendError("Opengl error: " + glu.gluErrorString(errnum));
        }

        private Primitive getLastPrimitive() {
            return primitives.get(primitives.size() - 1);
        }
    }

    protected void sendSuccess() {
        done = true;
        err = null;
        if (listener != null) {
            listener.onTesselationDone(tessCallback.primitives);
        }
    }

    protected void sendError(String message) {
        done = true;
        err = new TesselationException(message);
        if (listener != null) {
            listener.onTesselationError(err);
        }

    }
}
