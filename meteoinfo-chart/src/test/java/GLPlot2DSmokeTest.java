import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.GraphicFactory;
import org.meteoinfo.chart.jogl.GLPlot;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.legend.PolylineBreak;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies automatic 2-D display of a GLPlot and switching to 3-D by rotation.
 */
public class GLPlot2DSmokeTest {

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/glplot2d-smoke");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }

        GLPlot plot = new GLPlot();
        plot.setBackground(Color.WHITE);
        plot.setForeground(Color.BLACK);

        List<Coordinate> coordinates = new ArrayList<>();
        double[] xs = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] ys = {1, 3, 2, 5, 4, 7, 6, 8, 7, 10, 9};
        for (int i = 0; i < xs.length; i++) {
            coordinates.add(new Coordinate(xs[i], ys[i]));
        }
        PolylineBreak lb = new PolylineBreak();
        lb.setColor(new Color(200, 30, 30));
        lb.setWidth(2.0f);
        org.meteoinfo.chart.graphic.GraphicCollection lines =
                new org.meteoinfo.chart.graphic.GraphicCollection();
        lines.add(new Graphic(new LineString(coordinates), lb));
        plot.addGraphic(lines);

        PointBreak pb = new PointBreak();
        pb.setColor(new Color(30, 60, 200));
        pb.setSize(8);
        org.meteoinfo.chart.graphic.GraphicCollection points =
                new org.meteoinfo.chart.graphic.GraphicCollection();
        for (int i = 0; i < xs.length; i += 2) {
            points.add(GraphicFactory.createPoint((float) xs[i], (float) ys[i], pb));
        }
        plot.addGraphic(points);
        // A single (non-collection) graphic must work in both 2-D and 3-D modes.
        plot.addGraphic(GraphicFactory.createPoint(5.0f, 5.0f, pb));

        check(plot.isAllGraphics2D(), "all graphics should be planar");
        check(plot.is2D(), "planar graphics should use 2-D mode by default");

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);
        GLOffscreenAutoDrawable drawable = GLDrawableFactory.getFactory(profile)
                .createOffscreenAutoDrawable(null, caps, null, 500, 350);
        //plot.rotateTo3D(); // diagnostic: compare with 3-D rendering
        drawable.addGLEventListener(new com.jogamp.opengl.GLEventListener() {
            @Override
            public void init(com.jogamp.opengl.GLAutoDrawable d) {
                plot.init(d);
            }

            @Override
            public void dispose(com.jogamp.opengl.GLAutoDrawable d) {
                plot.dispose(d);
            }

            @Override
            public void display(com.jogamp.opengl.GLAutoDrawable d) {
                plot.display(d);
            }

            @Override
            public void reshape(com.jogamp.opengl.GLAutoDrawable d, int x, int y, int w, int h) {
                plot.reshape(d, x, y, w, h);
            }
        });
        drawable.display();
        drawable.display();
        save(drawable, new File(dir, "glplot-2d.png"));

        // Rotate the camera: the same planar graphics should now show in 3-D.
        plot.setAngleX(-30);
        plot.setAngleY(35);
        check(!plot.is2D(), "rotation should switch to 3-D");
        drawable.display();
        save(drawable, new File(dir, "glplot-3d.png"));

        // Rotating close to the top view automatically returns to 2-D.
        plot.setAngleX(1);
        plot.setAngleY(1);
        check(plot.is2D(), "near-top view should switch to 2-D");
        drawable.display();
        save(drawable, new File(dir, "glplot-near-2d.png"));

        // Hysteresis: still 2-D just outside the entry tolerance, 3-D further out.
        plot.setAngleX(3);
        plot.setAngleY(3);
        check(plot.is2D(), "hysteresis should keep a near-top view in 2-D");
        plot.setAngleX(8);
        plot.setAngleY(8);
        check(!plot.is2D(), "rotating away from the top view should restore 3-D");

        // angleY at 0/90/180/270 and angleX at 0/-180 are all 2-D orientations.
        plot.setAngleX(0);
        plot.setAngleY(90);
        check(plot.is2D(), "angleY=90 should be treated as a 2-D view");
        plot.setAngleX(-179);
        plot.setAngleY(180);
        check(plot.is2D(), "angleX near -180 should be treated as a 2-D view");
        plot.setAngleX(0);
        plot.setAngleY(91);
        check(plot.is2D(), "angleY near 90 should be treated as a 2-D view");
        plot.setAngleX(0);
        plot.setAngleY(45);
        check(!plot.is2D(), "angleY=45 should remain a 3-D orientation");

        // A real 3-D graphic also becomes 2-D at the top view.
        GLPlot volumetric = new GLPlot();
        org.meteoinfo.chart.graphic.GraphicCollection points3d =
                new org.meteoinfo.chart.graphic.GraphicCollection();
        PointBreak pb3 = new PointBreak();
        pb3.setSize(6);
        points3d.add(GraphicFactory.createPoint(1.0f, 1.0f, pb3));
        org.meteoinfo.geometry.Point p0 =
                (org.meteoinfo.geometry.Point) points3d.getGraphicN(0).getGeometry();
        p0.getCoordinate().z = 2.0;
        p0.updateExtent();
        points3d.add(GraphicFactory.createPoint(2.0f, 2.0f, pb3));
        org.meteoinfo.geometry.Point p1 =
                (org.meteoinfo.geometry.Point) points3d.getGraphicN(1).getGeometry();
        p1.getCoordinate().z = 3.0;
        p1.updateExtent();
        // Give the collection a real Z extent.
        points3d.updateExtent();
        volumetric.addGraphic(points3d);
        check(!volumetric.is2D(), "3-D data should start in 3-D mode");
        volumetric.setAngleX(0);
        volumetric.setAngleY(0);
        check(volumetric.is2D(), "3-D data at the top view should use 2-D form");
        volumetric.setAngleX(-180);
        volumetric.setAngleY(90);
        check(volumetric.is2D(), "3-D data at angleX=-180/angleY=90 should use 2-D form");

        // Explicit switch back to 2-D.
        plot.set2D(true);
        check(plot.is2D(), "set2D(true) should restore 2-D mode");
        drawable.display();
        save(drawable, new File(dir, "glplot-2d-again.png"));

        System.out.println("OK");
    }

    private static void save(GLOffscreenAutoDrawable drawable, File file) throws Exception {
        drawable.getContext().makeCurrent();
        com.jogamp.opengl.GL2 gl = drawable.getGL().getGL2();
        int w = drawable.getSurfaceWidth();
        int h = drawable.getSurfaceHeight();
        java.nio.ByteBuffer buffer = com.jogamp.common.nio.Buffers.newDirectByteBuffer(w * h * 4);
        gl.glReadPixels(0, 0, w, h, com.jogamp.opengl.GL2.GL_RGBA,
                com.jogamp.opengl.GL2.GL_UNSIGNED_BYTE, buffer);
        int readError = gl.glGetError();
        if (readError != 0) {
            throw new IllegalStateException("glReadPixels failed: 0x"
                    + Integer.toHexString(readError));
        }
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = ((h - 1 - y) * w + x) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        ImageIO.write(image, "png", file);
        System.out.println(file.getAbsolutePath());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
