import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.PointGraphic;
import org.meteoinfo.chart.graphic.PointGraphicCollection;
import org.meteoinfo.chart.jogl.EarthGLPlot;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.geometry.legend.PointBreak;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Verifies that scatter / scatter3 point graphics are transformed from
 * geographic lon/lat/alt coordinates to earth centred cartesian coordinates
 * before they are rendered by EarthGLPlot.
 */
public class EarthScatterRenderTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final Color[] COLORS = new Color[]{
            new Color(220, 30, 30), new Color(30, 180, 30), new Color(30, 60, 220)};

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/earth-scatter-test");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }

        final EarthGLPlot plot = new EarthGLPlot();
        plot.setBackground(Color.WHITE);
        plot.setForeground(Color.BLACK);
        plot.setDrawBoundingBox(false);
        plot.setBoxed(false);
        plot.setDrawBase(false);
        plot.getGridLine().setDrawXLine(false);
        plot.getGridLine().setDrawYLine(false);
        plot.getGridLine().setDrawZLine(false);

        PointGraphicCollection points = new PointGraphicCollection();
        double[] lons = new double[]{0, 90, 180};
        for (int i = 0; i < lons.length; i++) {
            PointBreak pb = new PointBreak();
            pb.setColor(COLORS[i]);
            pb.setOutlineColor(Color.BLACK);
            pb.setSize(30);
            Point point = new Point(new Coordinate(lons[i], 0, 0));
            if (i == 2) {
                // The color-data path of PointGraphicCollection uses a plain
                // Graphic, not a PointGraphic.
                points.add(new Graphic(point, pb));
            } else {
                points.add(new PointGraphic(point, pb));
            }
        }
        plot.addGraphic((Graphic) points);

        Extent extent = plot.getDrawExtent();
        check(Math.abs(extent.maxX) > 1000 || Math.abs(extent.maxY) > 1000,
                "draw extent should be earth centred cartesian, was " + extent);

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);
        GLOffscreenAutoDrawable drawable = GLDrawableFactory.getFactory(profile)
                .createOffscreenAutoDrawable(null, caps, null, WIDTH, HEIGHT);
        drawable.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable d) {
                plot.init(d);
            }

            @Override
            public void dispose(GLAutoDrawable d) {
                plot.dispose(d);
            }

            @Override
            public void display(GLAutoDrawable d) {
                plot.display(d);
            }

            @Override
            public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
                plot.reshape(d, x, y, w, h);
            }
        });
        drawable.display();
        drawable.display();
        BufferedImage image = readImage(drawable);
        ImageIO.write(image, "png", new File(dir, "earth-scatter.png"));

        int[][] centers = new int[COLORS.length][];
        for (int i = 0; i < COLORS.length; i++) {
            centers[i] = centroid(image, COLORS[i]);
            check(centers[i] != null, "scatter point " + i + " was not drawn");
        }
        for (int i = 0; i < centers.length; i++) {
            for (int j = i + 1; j < centers.length; j++) {
                double distance = Math.hypot(centers[i][0] - centers[j][0],
                        centers[i][1] - centers[j][1]);
                check(distance > 80, "scatter points " + i + " and " + j
                        + " are too close, distance=" + distance);
            }
        }

        // Zoom in by shrinking the draw extent.  The points must remain
        // visible: the earth zoom only changes the camera scale.
        Extent zoom = extent.extend(-extent.getWidth() * 0.05, -extent.getHeight() * 0.05,
                -extent.getZLength() * 0.05);
        plot.setDrawExtent(zoom);
        drawable.display();
        drawable.display();
        BufferedImage zoomImage = readImage(drawable);
        ImageIO.write(zoomImage, "png", new File(dir, "earth-scatter-zoomed.png"));
        for (int i = 0; i < COLORS.length; i++) {
            check(centroid(zoomImage, COLORS[i]) != null,
                    "scatter point " + i + " disappeared after zooming in");
        }

        System.out.println("OK");
    }

    private static int[] centroid(BufferedImage image, Color color) {
        long sx = 0;
        long sy = 0;
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (Math.abs(r - color.getRed()) + Math.abs(g - color.getGreen())
                        + Math.abs(b - color.getBlue()) <= 30) {
                    sx += x;
                    sy += y;
                    count++;
                }
            }
        }
        if (count < 20) {
            return null;
        }
        return new int[]{(int) (sx / count), (int) (sy / count), count};
    }

    private static BufferedImage readImage(GLOffscreenAutoDrawable drawable) {
        drawable.getContext().makeCurrent();
        com.jogamp.opengl.GL2 gl = drawable.getGL().getGL2();
        int w = drawable.getSurfaceWidth();
        int h = drawable.getSurfaceHeight();
        java.nio.ByteBuffer buffer = com.jogamp.common.nio.Buffers.newDirectByteBuffer(w * h * 4);
        gl.glReadPixels(0, 0, w, h, com.jogamp.opengl.GL2.GL_RGBA,
                com.jogamp.opengl.GL2.GL_UNSIGNED_BYTE, buffer);
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
        return image;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
