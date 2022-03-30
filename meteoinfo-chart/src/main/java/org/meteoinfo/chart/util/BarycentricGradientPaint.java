package org.meteoinfo.chart.util;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;

/**
 * The BarycentricGradientPaint class provides a way to fill a {@link Shape}
 * with a triangular color gradient.
 * Colors are specified for the vertices of a triangle and interpolated within
 * the triangle according to barycentric coordinates.
 * Only areas of the filled shape that are intersecting with the triangle of
 * this paint are colored.
 * <p>
 * This paint supports the {@code ANTIALIASING} rendering hint
 * ({@link RenderingHints}) using a 4x multisampling approach.
 * When enabled, the edges of the triangle will appear anti-aliased.
 * It is recommended to disable AA when filling a triangle mesh (where triangles
 * are adjacent), since otherwise triangle edges become visible.
 * <p>
 * Note that it is not necessary to use a triangular {@link Shape} to render a
 * triangle. Instead, a rectangle can be used as well, since only the intersecting
 * area will be filled.
 *
 * @author hageldave
 */
public class BarycentricGradientPaint implements Paint {

    protected Point2D.Float p1;
    protected Point2D.Float p2;
    protected Point2D.Float p3;
    protected Color color1;
    protected Color color2;
    protected Color color3;


    /**
     * Creates a new {@link BarycentricGradientPaint} object with
     * specified triangle vertices and vertex colors.
     *
     * @param p1 vertex of triangle
     * @param p2 vertex of triangle
     * @param p3 vertex of triangle
     * @param color1 color of vertex
     * @param color2 color of vertex
     * @param color3 color of vertex
     */
    public BarycentricGradientPaint(Point2D p1, Point2D p2, Point2D p3, Color color1, Color color2, Color color3) {
        this.p1 = new Point2D.Float((float)p1.getX(), (float)p1.getY());
        this.p2 = new Point2D.Float((float)p2.getX(), (float)p2.getY());
        this.p3 = new Point2D.Float((float)p3.getX(), (float)p3.getY());
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    /**
     * Creates a new {@link BarycentricGradientPaint} object with
     * specified triangle vertices and vertex colors.
     *
     * @param x x-coordinates for the triangle vertices
     * @param y y-coordinates for the triangle vertices
     * @param color1 color of vertex
     * @param color2 color of vertex
     * @param color3 color of vertex
     */
    public BarycentricGradientPaint(float[] x, float[] y, Color color1, Color color2, Color color3) {
        this(x[0],y[0],x[1],y[1],x[2],y[2], color1,color2,color3);
    }

    /**
     * Creates a new {@link BarycentricGradientPaint} object with
     * specified triangle vertices and vertex colors.
     *
     * @param x1 x-coord of triangle vertex
     * @param y1 y-coord of triangle vertex
     * @param x2 x-coord of triangle vertex
     * @param y2 y-coord of triangle vertex
     * @param x3 x-coord of triangle vertex
     * @param y3 y-coord of triangle vertex
     * @param color1 color of vertex
     * @param color2 color of vertex
     * @param color3 color of vertex
     */
    public BarycentricGradientPaint(double x1, double y1, double x2, double y2, double x3, double y3, Color color1, Color color2, Color color3) {
        this((float)x1,(float)y1,(float)x2,(float)y2,(float)x3,(float)y3, color1,color2,color3);
    }

    /**
     * Creates a new {@link BarycentricGradientPaint} object with
     * specified triangle vertices and vertex colors.
     *
     * @param x1 x-coord of triangle vertex
     * @param y1 y-coord of triangle vertex
     * @param x2 x-coord of triangle vertex
     * @param y2 y-coord of triangle vertex
     * @param x3 x-coord of triangle vertex
     * @param y3 y-coord of triangle vertex
     * @param color1 color of vertex
     * @param color2 color of vertex
     * @param color3 color of vertex
     */
    public BarycentricGradientPaint(float x1, float y1, float x2, float y2, float x3, float y3, Color color1, Color color2, Color color3) {
        this.p1 = new Point2D.Float(x1, y1);
        this.p2 = new Point2D.Float(x2, y2);
        this.p3 = new Point2D.Float(x3, y3);
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }



    @Override
    public int getTransparency() {
        int a1 = color1.getAlpha();
        int a2 = color2.getAlpha();
        int a3 = color3.getAlpha();
        return (((a1 & a2 & a3) == 0xff) ? OPAQUE : TRANSLUCENT);
    }


    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
                                      AffineTransform xform, RenderingHints hints) {
        return new BarycentricGradientPaintContext(
                p1,p2,p3,
                color1,color2,color3,
                xform,
                hints.get(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON
        );
    }

    /**
     * This class is implements the {@link PaintContext} for
     * {@link BarycentricGradientPaint}.
     * <p>
     * The context operates solely in ARGB color space (blue on least
     * significant bits) with an integer packing {@link DirectColorModel}.
     * <p>
     * A cache for raster memory is implemented to avoid costly memory
     * allocations.
     *
     * @author hageldave
     */
    public static class BarycentricGradientPaintContext implements PaintContext {
        protected static final float[] MSAA_SAMPLES;

        static {
            MSAA_SAMPLES = new float[8];
            AffineTransform xform = new AffineTransform();
            xform.translate(.5, .5);
            xform.rotate(Math.PI*0.5*0.2);
            xform.scale(.5, .5);
            xform.translate(-.5, -.5);
            xform.transform(new float[] {0,0, 1,0, 0,1, 1,1}, 0, MSAA_SAMPLES, 0, 4);
        }

        protected final float x1,x2,x3,y1,y2,y3;
        protected final float x23,x13,y23,y13; //,x12,y12;
        protected final float denom;

        protected final int c1,c2,c3;
        protected final DirectColorModel cm = new DirectColorModel(32,
                0x00ff0000,       // Red
                0x0000ff00,       // Green
                0x000000ff,       // Blue
                0xff000000        // Alpha
        );
        protected final boolean antialiasing;
        protected WritableRaster saved;
        protected WeakReference<int[]> cache;

        public BarycentricGradientPaintContext(
                Point2D.Float p1, Point2D.Float p2, Point2D.Float p3,
                Color color1, Color color2, Color color3,
                AffineTransform xform, boolean antialiasing)
        {
            c1 = color1.getRGB();
            c2 = color2.getRGB();
            c3 = color3.getRGB();

            p1 = (Point2D.Float) xform.transform(p1, new Point2D.Float());
            p2 = (Point2D.Float) xform.transform(p2, new Point2D.Float());
            p3 = (Point2D.Float) xform.transform(p3, new Point2D.Float());
            // constants for barycentric coords
            x1=p1.x; x2=p2.x; x3=p3.x; y1=p1.y; y2=p2.y; y3=p3.y;
            x23=x2-x3; x13=x1-x3; y23=y2-y3; y13=y1-y3; // x12=x1-x2; y12=y1-y2;
            denom=1f/((y23*x13)-(x23*y13));

            this.antialiasing = antialiasing;
        }


        @Override
        public void dispose() {
            if(saved != null)
                cacheRaster(saved);
            saved = null;
        }

        @Override
        public ColorModel getColorModel() {
            return cm;
        }

        @Override
        public Raster getRaster(int xA, int yA, int w, int h) {
            WritableRaster rast = saved;
            if (rast == null) {
                rast = getCachedOrCreateRaster(w, h);
                saved = rast;
            } else if(rast.getWidth() != w || rast.getHeight() != h) {
                int[] data = dataFromRaster(rast);
                if(data.length < w*h) {
                    data = new int[w*h];
                }
                rast = createRaster(w, h, data);
                saved = rast;
            }

            // fill data array with interpolated colors (barycentric coords)
            int[] data = dataFromRaster(rast);
            if(antialiasing)
                fillRasterMSAA(xA, yA, w, h, data);
            else
                fillRaster(xA, yA, w, h, data);


            return rast;
        }

        protected void fillRaster(int xA, int yA, int w, int h, int[] data) {
            if(c1==c2&&c2==c3) {
                // all vertices same color
                for(int i=0; i<h; i++) {
                    float y = yA+i+.5f;
                    float ypart11 = -x23*(y-y3);
                    float ypart21 =  x13*(y-y3);

                    for(int j=0; j<w; j++) {
                        float x = xA+j+.5f;
                        // calculate barycentric coordinates for (x,y)
                        float l1 = ( y23*(x-x3)+ypart11)*denom;
                        float l2 = (-y13*(x-x3)+ypart21)*denom;
                        float l3 = 1f-l1-l2;
                        // determine color
                        int mix1;
                        if(l1<0||l2<0||l3<0) mix1 = 0;
                        else mix1 = c1;
                        data[i*w+j] = mix1;
                    }
                }
            } else {
                // vertices of different color
                for(int i=0; i<h; i++) {
                    float y = yA+i+.5f;
                    float ypart11 = -x23*(y-y3);
                    float ypart21 =  x13*(y-y3);

                    for(int j=0; j<w; j++) {
                        float x = xA+j+.5f;
                        // calculate barycentric coordinates for (x,y)
                        float l1 = ( y23*(x-x3)+ypart11)*denom;
                        float l2 = (-y13*(x-x3)+ypart21)*denom;
                        float l3 = 1f-l1-l2;
                        // determine color
                        int mix1;
                        if(l1<0||l2<0||l3<0) mix1 = 0;
                        else mix1 = mixColor3(c1, c2, c3, l1, l2, l3);
                        data[i*w+j] = mix1;
                    }
                }
            }
        }


        protected void fillRasterMSAA(int xA, int yA, int w, int h, int[] data) {
            final boolean monochrome = c1==c2&&c2==c3;
            for(int i=0; i<h; i++) {
                float y = yA+i+MSAA_SAMPLES[1];
                float ypart11 = -x23*(y-y3);
                float ypart21 =  x13*(y-y3);
                y = yA+i+MSAA_SAMPLES[3];
                float ypart12 = -x23*(y-y3);
                float ypart22 =  x13*(y-y3);
                y = yA+i+MSAA_SAMPLES[5];
                float ypart13 = -x23*(y-y3);
                float ypart23 =  x13*(y-y3);
                y = yA+i+MSAA_SAMPLES[7];
                float ypart14 = -x23*(y-y3);
                float ypart24 =  x13*(y-y3);

                for(int j=0; j<w; j++) {
                    float x = xA+j+MSAA_SAMPLES[0];
                    float xpart11 =  y23*(x-x3);
                    float xpart21 = -y13*(x-x3);
                    x = xA+j+MSAA_SAMPLES[2];
                    float xpart12 =  y23*(x-x3);
                    float xpart22 = -y13*(x-x3);
                    x = xA+j+MSAA_SAMPLES[4];
                    float xpart13 =  y23*(x-x3);
                    float xpart23 = -y13*(x-x3);
                    x = xA+j+MSAA_SAMPLES[6];
                    float xpart14 =  y23*(x-x3);
                    float xpart24 = -y13*(x-x3);

                    // calculate barycentric coordinates for the 4 sub pixel samples
                    float l11 = (xpart11+ypart11)*denom;
                    float l21 = (xpart21+ypart21)*denom;
                    float l31 = 1f-l11-l21;

                    float l12 = (xpart12+ypart12)*denom;
                    float l22 = (xpart22+ypart22)*denom;
                    float l32 = 1f-l12-l22;

                    float l13 = (xpart13+ypart13)*denom;
                    float l23 = (xpart23+ypart23)*denom;
                    float l33 = 1f-l13-l23;

                    float l14 = (xpart14+ypart14)*denom;
                    float l24 = (xpart24+ypart24)*denom;
                    float l34 = 1f-l14-l24;

                    // determine sample colors and weights (out of triangle samples have 0 weight)
                    int mix1,mix2,mix3,mix4;
                    float w1,w2,w3,w4;

                    if(l11<0||l21<0||l31<0) { mix1 = 0; w1=0f; }
                    else { mix1 = monochrome ? c1:mixColor3(c1, c2, c3, l11, l21, l31); w1=1f; }

                    if(l12<0||l22<0||l32<0) { mix2 = 0; w2=0f; }
                    else { mix2 = monochrome ? c1:mixColor3(c1, c2, c3, l12, l22, l32); w2=1f; }

                    if(l13<0||l23<0||l33<0) {mix3 = 0; w3=0f; }
                    else { mix3 = monochrome ? c1:mixColor3(c1, c2, c3, l13, l23, l33); w3=1f; }

                    if(l14<0||l24<0||l34<0) { mix4 = 0; w4=0f; }
                    else { mix4 = monochrome ? c1:mixColor3(c1, c2, c3, l14, l24, l34); w4=1f; }

                    int color = mixColor4(mix1, mix2, mix3, mix4, w1,w2,w3,w4);
                    data[i*w+j] = scaleColorAlpha(color,(w1+w2+w3+w4)*.25f);
                }
            }
        }

        protected WritableRaster getCachedOrCreateRaster(int w, int h) {
            if(cache != null) {
                int[] data = cache.get();
                if (data != null && data.length >= w*h)
                {
                    cache = null;
                    return createRaster(w, h, data);
                }
            }
            return createRaster(w, h, new int[w*h]);
        }

        protected void cacheRaster(WritableRaster ras) {
            int[] toCache = dataFromRaster(ras);
            if (cache != null) {
                int[] data = cache.get();
                if (data != null) {
                    if (toCache.length < data.length) {
                        return;
                    }
                }
            }
            cache = new WeakReference<>(toCache);
        }

        protected WritableRaster createRaster(int w, int h, int[] data) {
            DataBufferInt buffer = new DataBufferInt(data, w*h);
            WritableRaster raster = Raster.createPackedRaster(buffer, w, h, w, cm.getMasks(), null);
            return raster;
        }

        private static int[] dataFromRaster(WritableRaster wr) {
            return ((DataBufferInt)wr.getDataBuffer()).getData();
        }

        private static int mixColor3(int c1, int c2, int c3, float m1, float m2, float m3) {
            float normalize = 1f/(m1+m2+m3);
            float a = (a(c1)*m1 + a(c2)*m2 + a(c3)*m3)*normalize;
            float r = (r(c1)*m1 + r(c2)*m2 + r(c3)*m3)*normalize;
            float g = (g(c1)*m1 + g(c2)*m2 + g(c3)*m3)*normalize;
            float b = (b(c1)*m1 + b(c2)*m2 + b(c3)*m3)*normalize;
            return argb((int)a, (int)r, (int)g, (int)b);
        }

        private static int mixColor4(int c1, int c2, int c3, int c4, float m1, float m2, float m3, float m4) {
            float normalize = 1f/(m1+m2+m3+m4);
            float a = (a(c1)*m1 + a(c2)*m2 + a(c3)*m3 + a(c4)*m4)*normalize;
            float r = (r(c1)*m1 + r(c2)*m2 + r(c3)*m3 + r(c4)*m4)*normalize;
            float g = (g(c1)*m1 + g(c2)*m2 + g(c3)*m3 + g(c4)*m4)*normalize;
            float b = (b(c1)*m1 + b(c2)*m2 + b(c3)*m3 + b(c4)*m4)*normalize;
            return argb((int)a, (int)r, (int)g, (int)b);
        }

        private static int a(int argb) {
            return (argb >> 24) & 0xff;
        }

        private static int r(int argb) {
            return (argb >> 16) & 0xff;
        }

        private static int g(int argb) {
            return (argb >> 8) & 0xff;
        }

        private static int b(int argb) {
            return (argb) & 0xff;
        }

        private static int argb(final int a, final int r, final int g, final int b){
            return (a<<24)|(r<<16)|(g<<8)|b;
        }

        private static int scaleColorAlpha(int color, float m) {
            float normalize = 1f/255f;
            float af = a(color)*normalize*m;
            int a = (((int)(af*255f)) & 0xff) << 24;
            return (color&0x00ffffff)|a;
        }

    }

}
