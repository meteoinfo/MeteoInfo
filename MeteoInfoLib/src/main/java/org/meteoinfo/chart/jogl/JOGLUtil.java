/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GL2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.meteoinfo.chart.jogl.mc.MarchingCubes;
import org.meteoinfo.chart.jogl.mc.CallbackMC;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.layer.ImageLayer;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.math.ArrayUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.PointZ;

/**
 *
 * @author yaqiang
 */
public class JOGLUtil {

    public static void diff3(final float[] a, final float[] b, final float[] c) {
        c[0] = a[0] - b[0];
        c[1] = a[1] - b[1];
        c[2] = a[2] - b[2];
    }

    public static void crossprod(final float[] v1, final float[] v2, final float[] prod) {
        final float[] p = new float[3];         /* in case prod == v1 or v2 */

        p[0] = v1[1] * v2[2] - v2[1] * v1[2];
        p[1] = v1[2] * v2[0] - v2[2] * v1[0];
        p[2] = v1[0] * v2[1] - v2[0] * v1[1];
        prod[0] = p[0];
        prod[1] = p[1];
        prod[2] = p[2];
    }

    public static void normalize(final float[] v) {
        float d;

        d = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (d == 0.0) {
            v[0] = d = 1.0f;
        }
        d = 1 / d;
        v[0] *= d;
        v[1] *= d;
        v[2] *= d;
    }

    public static float[] normalize(final float[] n1, final float[] n2, final float[] n3) {
        final float[] q0 = new float[3];
        final float[] q1 = new float[3];

        diff3(n1, n2, q0);
        diff3(n2, n3, q1);
        crossprod(q0, q1, q1);
        normalize(q1);

        return q1;
    }

    private static void recordItem(final GL2 gl, final float[] n1, final float[] n2, final float[] n3, final int shadeType) {
        final float[] q0 = new float[3];
        final float[] q1 = new float[3];

        diff3(n1, n2, q0);
        diff3(n2, n3, q1);
        crossprod(q0, q1, q1);
        normalize(q1);

        gl.glBegin(shadeType);
        gl.glNormal3fv(q1, 0);
        gl.glVertex3fv(n1, 0);
        gl.glVertex3fv(n2, 0);
        gl.glVertex3fv(n3, 0);
        gl.glEnd();
    }

    private static void subdivide(final GL2 gl, final float[] v0, final float[] v1, final float[] v2, final int shadeType) {
        int depth;
        final float[] w0 = new float[3];
        final float[] w1 = new float[3];
        final float[] w2 = new float[3];
        float l;
        int i, j, k, n;

        depth = 1;
        for (i = 0; i < depth; i++) {
            for (j = 0; i + j < depth; j++) {
                k = depth - i - j;
                for (n = 0; n < 3; n++) {
                    w0[n] = (i * v0[n] + j * v1[n] + k * v2[n]) / depth;
                    w1[n] = ((i + 1) * v0[n] + j * v1[n] + (k - 1) * v2[n])
                            / depth;
                    w2[n] = (i * v0[n] + (j + 1) * v1[n] + (k - 1) * v2[n])
                            / depth;
                }
                l = (float) Math.sqrt(w0[0] * w0[0] + w0[1] * w0[1] + w0[2] * w0[2]);
                w0[0] /= l;
                w0[1] /= l;
                w0[2] /= l;
                l = (float) Math.sqrt(w1[0] * w1[0] + w1[1] * w1[1] + w1[2] * w1[2]);
                w1[0] /= l;
                w1[1] /= l;
                w1[2] /= l;
                l = (float) Math.sqrt(w2[0] * w2[0] + w2[1] * w2[1] + w2[2] * w2[2]);
                w2[0] /= l;
                w2[1] /= l;
                w2[2] /= l;
                recordItem(gl, w1, w0, w2, shadeType);
            }
        }
    }

    public static void drawTriangle(final GL2 gl, final float[]x0, final float[] x1,
                                    final float[] x2) {
        //subdivide(gl, x0, x1, x2, GL2.GL_TRIANGLES);
        recordItem(gl, x0, x1, x2, GL2.GL_TRIANGLES);
    }

    /**
     * Get RGBA components from a legend break
     *
     * @param cb Legend break
     * @return RGBA float array
     */
    public static float[] getRGBA(ColorBreak cb) {
        return cb.getColor().getRGBComponents(null);
    }

    /**
     * Create Texture
     *
     * @param gl GL2
     * @param layer Image layer
     * @param offset Offset of z axis
     * @param xshift X shift - to shift the grahpics in x direction, normally
     * for map in 180 - 360 degree east
     * @param interpolation Interpolation
     * @return Graphics
     * @throws java.io.IOException
     */
    public static GraphicCollection createTexture(GL2 gl, ImageLayer layer, double offset, double xshift,
            String interpolation) throws IOException {
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZDir("z");
        graphics.setZValue(offset);
        TextureShape ishape = new TextureShape();
        ishape.setFileName(layer.getFileName());
        Extent extent = layer.getExtent();
        Extent3D ex3 = new Extent3D(extent.minX + xshift, extent.maxX + xshift, extent.minY, extent.maxY, offset, offset);
        List<PointZ> coords = new ArrayList<>();
        coords.add(new PointZ(extent.minX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.maxY, offset));
        coords.add(new PointZ(extent.minX + xshift, extent.maxY, offset));
        ishape.setExtent(ex3);
        ishape.setCoords(coords);
        Graphic gg = new Graphic(ishape, new ColorBreak());
        if (interpolation != null) {
            ((ImageShape) gg.getShape()).setInterpolation(interpolation);
        }
        graphics.add(gg);

        return graphics;
    }

    /**
     * Create surface graphics
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param ls Legend scheme
     * @return Surface graphics
     */
    public static SurfaceGraphics surface(Array xa, Array ya, Array za, LegendScheme ls) {
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        SurfaceGraphics graphics = new SurfaceGraphics();
        int[] shape = xa.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        int idx;
        PointZ[][] vertices = new PointZ[rowNum][colNum];
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                idx = i * colNum + j;
                vertices[i][j] = new PointZ(xa.getDouble(idx), ya.getDouble(idx), za.getDouble(idx), za.getDouble(idx));
            }
        }
        graphics.setVertices(vertices);
        graphics.setLegendScheme(ls);
        return graphics;
    }

    /**
     * Create slice graphics
     *
     * @param data Data array - 3D
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param za Z coordinate array - 1D
     * @param xSlice X slice list
     * @param ySlice Y slice list
     * @param zSlice Z slice list
     * @param ls Legend scheme
     * @return Surface graphics
     */
    public static List<SurfaceGraphics> slice(Array data, Array xa, Array ya, Array za, List<Number> xSlice,
                                        List<Number> ySlice, List<Number> zSlice, LegendScheme ls) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        List<SurfaceGraphics> sgs = new ArrayList<>();

        int dim1, dim2;
        double x, y, z;

        //X slices
        dim1 = (int)za.getSize();
        dim2 = (int)ya.getSize();
        for (int s = 0; s < xSlice.size(); s++) {
            x = xSlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 2, xa, x);
            if (r != null) {
                Index index = r.getIndex();
                SurfaceGraphics graphics = new SurfaceGraphics();
                PointZ[][] vertices = new PointZ[dim1][dim2];
                for (int i = 0; i < dim1; i++) {
                    z = za.getDouble(i);
                    for (int j = 0; j < dim2; j++) {
                        y = ya.getDouble(j);
                        vertices[i][j] = new PointZ(x, y, z, r.getDouble(index.set(i, j)));
                    }
                }
                graphics.setVertices(vertices);
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        //Y slices
        dim1 = (int)za.getSize();
        dim2 = (int)xa.getSize();
        for (int s = 0; s < ySlice.size(); s++) {
            y = ySlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 1, ya, y);
            if (r != null) {
                Index index = r.getIndex();
                SurfaceGraphics graphics = new SurfaceGraphics();
                PointZ[][] vertices = new PointZ[dim1][dim2];
                for (int i = 0; i < dim1; i++) {
                    z = za.getDouble(i);
                    for (int j = 0; j < dim2; j++) {
                        x = xa.getDouble(j);
                        vertices[i][j] = new PointZ(x, y, z, r.getDouble(index.set(i, j)));
                    }
                }
                graphics.setVertices(vertices);
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        //Z slices
        dim1 = (int)ya.getSize();
        dim2 = (int)xa.getSize();
        for (int s = 0; s < zSlice.size(); s++) {
            z = zSlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 0, za, z);
            if (r != null) {
                Index index = r.getIndex();
                SurfaceGraphics graphics = new SurfaceGraphics();
                PointZ[][] vertices = new PointZ[dim1][dim2];
                for (int i = 0; i < dim1; i++) {
                    y = ya.getDouble(i);
                    for (int j = 0; j < dim2; j++) {
                        x = xa.getDouble(j);
                        vertices[i][j] = new PointZ(x, y, z, r.getDouble(index.set(i, j)));
                    }
                }
                graphics.setVertices(vertices);
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        return sgs;
    }

    /**
     * Create isosurface graphics
     *
     * @param data 3d data array
     * @param x X coordinates
     * @param y Y coordinates
     * @param z Z coordinates
     * @param isoLevel iso level
     * @param pb Polygon break
     * @return Graphics
     */
    public static GraphicCollection isosurface(Array data, Array x, Array y, Array z,
            float isoLevel, PolygonBreak pb) {
        x = x.copyIfView();
        y = y.copyIfView();
        z = z.copyIfView();
        data = data.copyIfView();

        List<float[]> vertices = MarchingCubes.marchingCubes(data, x, y, z, isoLevel);
        IsosurfaceGraphics graphics = new IsosurfaceGraphics();
        graphics.setLegendBreak(pb);
        float[] v1, v2, v3;
        for (int i = 0; i < vertices.size(); i += 3) {
            PointZ[] points = new PointZ[3];
            v1 = vertices.get(i);
            v2 = vertices.get(i + 1);
            v3 = vertices.get(i + 2);
            points[0] = new PointZ(v1[0], v1[1], v1[2]);
            points[1] = new PointZ(v2[0], v2[1], v2[2]);
            points[2] = new PointZ(v3[0], v3[1], v3[2]);
            graphics.addTriangle(points);
        }

        return graphics;
    }

    /**
     * Create isosurface graphics
     *
     * @param data 3d data array
     * @param x X coordinates
     * @param y Y coordinates
     * @param z Z coordinates
     * @param isoLevel iso level
     * @param pb Polygon break
     * @param nThreads Thread number
     * @return Graphics
     */
    public static GraphicCollection isosurface(final Array data, final Array x, final Array y, final Array z,
            final float isoLevel, PolygonBreak pb, int nThreads) {
        // TIMER
        ArrayList<Thread> threads = new ArrayList<>();
        final ArrayList<ArrayList<float[]>> results = new ArrayList<>();

        // Thread work distribution
        int nz = (int) z.getSize();
        int remainder = nz % nThreads;
        int segment = nz / nThreads;

        // Z axis offset for vertice position calculation
        int zAxisOffset = 0;

        for (int i = 0; i < nThreads; i++) {
            // Distribute remainder among first (remainder) threads
            int segmentSize = (remainder-- > 0) ? segment + 1 : segment;

            // Padding needs to be added to correctly close the gaps between segments
            final int paddedSegmentSize = (i != nThreads - 1) ? segmentSize + 1 : segmentSize;

            // Finished callback
            final CallbackMC callback = new CallbackMC() {
                @Override
                public void run() {
                    results.add(getVertices());
                }
            };

            // Java...
            final int finalZAxisOffset = zAxisOffset;

            // Start the thread
            Thread t = new Thread() {
                public void run() {
                    MarchingCubes.marchingCubes(data, x, y, z, isoLevel, paddedSegmentSize, finalZAxisOffset, callback);
                }
            };

            threads.add(t);
            t.start();

            // Correct offsets for next iteration
            zAxisOffset += segmentSize;
        }

        // Join the threads
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        IsosurfaceGraphics graphics = new IsosurfaceGraphics();
        graphics.setLegendBreak(pb);
        float[] v1, v2, v3;
        for (List<float[]> vertices : results) {
            for (int i = 0; i < vertices.size(); i += 3) {
                PointZ[] points = new PointZ[3];
                v1 = vertices.get(i);
                v2 = vertices.get(i + 1);
                v3 = vertices.get(i + 2);
                points[0] = new PointZ(v1[0], v1[1], v1[2]);
                points[1] = new PointZ(v2[0], v2[1], v2[2]);
                points[2] = new PointZ(v3[0], v3[1], v3[2]);
                graphics.addTriangle(points);
            }
        }

        return graphics;
    }

    /**
     * Create particle graphics
     * @param data 3d data array
     * @param xa X coordinates
     * @param ya Y coordinates
     * @param za Z coordinates
     * @param ls LegendScheme
     * @param alphaMin Min alpha
     * @param alphaMax Max alpha
     * @param density Point density
     * @return Particles
     */
    public static GraphicCollection particles(Array data, Array xa, Array ya, Array za, LegendScheme ls,
                                              float alphaMin, float alphaMax, int density) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        ParticleGraphics graphics = new ParticleGraphics();
        ParticleGraphics.Particle particle;
        Random random = new Random();
        float dx = xa.getFloat(1) - xa.getFloat(0);
        float dy = ya.getFloat(1) - ya.getFloat(0);
        float dz = za.getFloat(1) - za.getFloat(0);
        int n = ls.getBreakNum();
        float[] alphas = new float[n];
        float dd = (alphaMax - alphaMin) / (n - 1);
        for (int i = 0; i < n; i++) {
            alphas[i] = alphaMin + i * dd;
        }
        double v;
        ColorBreak cb;
        float[] rgba;
        int level;
        double vMin = ls.getMinValue();
        double vMax = ls.getMaxValue();
        Index index = data.getIndex();
        for (int i = 0; i < za.getSize(); i++) {
            for (int j = 0; j < ya.getSize(); j++) {
                for (int k = 0; k < xa.getSize(); k++) {
                    index.set(i, j, k);
                    v = data.getDouble(index);
                    if (Double.isNaN(v)) {
                        continue;
                    }
                    if (v < vMin || v > vMax) {
                        continue;
                    }
                    level = ls.legendBreakIndex(v);
                    if (level >= 0) {
                        cb = ls.getLegendBreak(level);
                        rgba = cb.getColor().getRGBComponents(null);
                        rgba[3] = alphas[level];
                        for (int l = 0; l <= level * density; l++) {
                            particle = new ParticleGraphics.Particle();
                            particle.x = xa.getFloat(k) + (random.nextFloat() - 0.5f) * dx * 2;
                            particle.y = ya.getFloat(j) + (random.nextFloat() - 0.5f) * dy * 2;
                            particle.z = za.getFloat(i) + (random.nextFloat() - 0.5f) * dz * 2;
                            particle.rgba = rgba;
                            graphics.addParticle(level, particle);
                        }
                    }
                }
            }
        }

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int)xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int)ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int)za.getSize() - 1);
        graphics.setExtent(extent3D);
        graphics.setLegendScheme(ls);

        return graphics;
    }
}
