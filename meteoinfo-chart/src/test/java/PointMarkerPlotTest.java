import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.PointGraphic;
import org.meteoinfo.chart.graphic.PointGraphicCollection;
import org.meteoinfo.chart.jogl.Axes;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.legend.PointStyle;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Integration check for the marker rendering of PointRender through a real
 * Axes. Every PointStyle is rendered in orthographic 3-D, perspective 3-D
 * and 2-D views, and the resulting image is checked for the presence and the
 * pixel size of the markers.
 */
public class PointMarkerPlotTest {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;
    private static final float MARKER_SIZE = 18;

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/point-marker-plot-test");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }

        render(dir, "marker-plot-3d.png", false, false, -25, 30);
        render(dir, "marker-plot-perspective.png", true, false, -25, 30);
        render(dir, "marker-plot-2d.png", false, true, 0, 0);
        System.out.println("OK");
    }

    private static void render(File dir, String name, boolean perspective, boolean force2D,
                               float angleX, float angleY) throws Exception {
        Axes plot = new Axes();
        plot.setBackground(Color.WHITE);
        plot.setForeground(Color.BLACK);
        plot.setDrawBoundingBox(true);
        plot.setBoxed(true);
        plot.setDisplayZ(true);
        plot.setAngleX(angleX);
        plot.setAngleY(angleY);
        plot.setOrthographic(!perspective);
        if (force2D) {
            plot.set2D(true);
        }

        PointStyle[] styles = PointStyle.values();
        PointGraphicCollection points = new PointGraphicCollection();
        for (int i = 0; i < styles.length; i++) {
            int col = i % 5;
            int row = i / 5;
            PointBreak pb = new PointBreak();
            pb.setStyle(styles[i]);
            pb.setSize(MARKER_SIZE);
            pb.setColor(color(i));
            pb.setOutlineColor(Color.BLACK);
            pb.setOutlineSize(1);
            Point point = new Point(new Coordinate(col * 1.5, row * 1.5, row * 0.8));
            points.add(new PointGraphic(point, pb));
        }
        plot.addGraphic((Graphic) points);
        plot.setDrawExtent(new Extent(-1, 7, -1, 4, -1, 3));

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
        ImageIO.write(image, "png", new File(dir, name));

        for (int i = 0; i < styles.length; i++) {
            int[] bounds = colorBounds(image, color(i));
            check(bounds != null, styles[i] + " was not drawn in " + name);
            int w = bounds[2] - bounds[0] + 1;
            int h = bounds[3] - bounds[1] + 1;
            check(w >= 8, styles[i] + " is too small in " + name + ": " + w + "x" + h);
            if (styles[i] != PointStyle.MINUS) {
                int minHeight = styles[i] == PointStyle.UP_SEMI_CIRCLE
                        || styles[i] == PointStyle.DOWN_SEMI_CIRCLE ? 3 : 8;
                check(h >= minHeight,
                        styles[i] + " is too small in " + name + ": " + w + "x" + h);
            }
            if (styles[i] == PointStyle.CIRCLE || styles[i] == PointStyle.SQUARE) {
                // A square/round marker must not be stretched by the viewport aspect ratio.
                check(Math.abs(w - h) <= Math.max(w, h) * 0.35f,
                        styles[i] + " is distorted in " + name + ": " + w + "x" + h);
            }
        }
    }

    private static Color color(int index) {
        int col = index % 5;
        int row = index / 5;
        return new Color(30 + col * 40, 180 - row * 50, 80 + row * 60);
    }

    /**
     * Get the bounding box of all pixels close to the given color.
     *
     * @return {minX, minY, maxX, maxY} or null when the color is absent
     */
    private static int[] colorBounds(BufferedImage image, Color color) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (Math.abs(r - color.getRed()) + Math.abs(g - color.getGreen())
                        + Math.abs(b - color.getBlue()) <= 12) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < 0) {
            return null;
        }
        return new int[]{minX, minY, maxX, maxY};
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
