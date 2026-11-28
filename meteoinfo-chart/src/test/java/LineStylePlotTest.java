import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.LineGraphic;
import org.meteoinfo.chart.graphic.LineGraphicCollection;
import org.meteoinfo.chart.jogl.Axes;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.legend.LineStyles;
import org.meteoinfo.geometry.legend.PolylineBreak;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration check of the line styles through a real Axes. The lines are
 * rendered in 3-D orthographic, 3-D perspective and 2-D views.
 */
public class LineStylePlotTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final LineStyles[] STYLES = new LineStyles[]{
            LineStyles.SOLID, LineStyles.DASH, LineStyles.DOT,
            LineStyles.DASH_DOT, LineStyles.DASH_DOT_DOT};

    private static final Color[] COLORS = new Color[]{
            new Color(200, 100, 0), new Color(220, 30, 30), new Color(30, 150, 30),
            new Color(30, 60, 220), new Color(180, 40, 180)};

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/line-style-plot-test");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }

        render(dir, "line-styles-2d.png", false, false, 0, 0, true);
        render(dir, "line-styles-3d.png", false, false, -25, 30, false);
        render(dir, "line-styles-perspective.png", true, false, -25, 30, false);
        System.out.println("OK");
    }

    private static void render(File dir, String name, boolean perspective, boolean force2D,
                               float angleX, float angleY, boolean checkPatterns) throws Exception {
        Axes plot = new Axes();
        plot.setBackground(Color.WHITE);
        plot.setForeground(Color.BLACK);
        plot.setDrawBoundingBox(false);
        plot.setBoxed(false);
        plot.setDrawBase(false);
        plot.getGridLine().setDrawXLine(false);
        plot.getGridLine().setDrawYLine(false);
        plot.getGridLine().setDrawZLine(false);
        plot.setDisplayZ(!checkPatterns);
        plot.setAngleX(angleX);
        plot.setAngleY(angleY);
        plot.setOrthographic(!perspective);
        if (force2D) {
            plot.set2D(true);
        }

        LineGraphicCollection lines = new LineGraphicCollection();
        for (int i = 0; i < STYLES.length; i++) {
            PolylineBreak pb = new PolylineBreak();
            pb.setStyle(STYLES[i]);
            pb.setColor(COLORS[i]);
            pb.setWidth(1.0f);
            LineString line = new LineString();
            List<Coordinate> coordinates = new ArrayList<>();
            double y = i + 0.5;
            coordinates.add(new Coordinate(-1, y, 0));
            coordinates.add(new Coordinate(1, y, 0));
            line.setCoordinates(coordinates);
            lines.add(new LineGraphic(line, pb));
        }
        lines.updateVertex();
        plot.addGraphic((Graphic) lines);
        plot.setDrawExtent(new Extent(-1, 1, 0, 5, -1, 1));
        if (!checkPatterns) {
            plot.rotateTo3D();
        }

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

        for (int i = 0; i < STYLES.length; i++) {
            int[] bounds = colorBounds(image, COLORS[i]);
            check(bounds != null, STYLES[i] + " was not drawn in " + name);
            int w = bounds[2] - bounds[0] + 1;
            check(w > WIDTH / 4, STYLES[i] + " line is too short in " + name + ": " + w);
            if (checkPatterns) {
                int y = (bounds[1] + bounds[3]) / 2;
                List<int[]> runs = runs(image, y, COLORS[i]);
                int onRuns = 0;
                int longest = 0;
                for (int[] run : runs) {
                    if (run[0] == 1) {
                        onRuns++;
                        longest = Math.max(longest, run[1]);
                    }
                }
                if (STYLES[i] == LineStyles.SOLID) {
                    check(onRuns == 1, "SOLID should be continuous but has " + onRuns + " runs");
                } else {
                    check(onRuns > 10, STYLES[i] + " should be dashed but has "
                            + onRuns + " runs");
                    check(longest < w, STYLES[i] + " should have gaps");
                }
            }
        }
    }

    private static List<int[]> runs(BufferedImage image, int y, Color color) {
        List<int[]> runs = new ArrayList<>();
        int current = -1;
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            int rgb = image.getRGB(x, y);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            int on = Math.abs(r - color.getRed()) + Math.abs(g - color.getGreen())
                    + Math.abs(b - color.getBlue()) <= 12 ? 1 : 0;
            if (on == current) {
                count++;
            } else {
                if (count > 0) {
                    runs.add(new int[]{current, count});
                }
                current = on;
                count = 1;
            }
        }
        if (count > 0) {
            runs.add(new int[]{current, count});
        }
        return runs;
    }

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
