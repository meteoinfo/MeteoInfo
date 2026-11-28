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
import org.meteoinfo.geometry.legend.PointStyle;
import org.meteoinfo.geometry.legend.PolylineBreak;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration check for line markers through a real Axes in 3-D and 2-D.
 */
public class LineMarkerPlotTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final Color LINE_COLOR = new Color(30, 60, 220);
    private static final Color MARKER_COLOR = new Color(220, 30, 30);

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/line-marker-plot-test");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }

        render(dir, "line-markers-3d.png", false, true, false);
        render(dir, "line-markers-perspective.png", true, true, false);
        render(dir, "line-markers-only-3d.png", false, false, false);
        render(dir, "line-markers-2d.png", false, true, true);
        System.out.println("OK");
    }

    private static void render(File dir, String name, boolean perspective, boolean drawLine,
                               boolean force2D) throws Exception {
        Axes plot = new Axes();
        plot.setBackground(Color.WHITE);
        plot.setForeground(Color.BLACK);
        plot.setDrawBoundingBox(false);
        plot.setBoxed(false);
        plot.setDrawBase(false);
        plot.getGridLine().setDrawXLine(false);
        plot.getGridLine().setDrawYLine(false);
        plot.getGridLine().setDrawZLine(false);
        plot.setAngleX(force2D ? 0 : -25);
        plot.setAngleY(force2D ? 0 : 30);
        plot.setOrthographic(!perspective);
        if (force2D) {
            plot.set2D(true);
        }

        PolylineBreak pb = new PolylineBreak();
        pb.setStyle(LineStyles.DASH);
        pb.setColor(LINE_COLOR);
        pb.setWidth(1.0f);
        pb.setDrawPolyline(drawLine);
        pb.setDrawSymbol(true);
        pb.setSymbolStyle(PointStyle.CIRCLE);
        pb.setSymbolSize(14);
        pb.setSymbolFillColor(MARKER_COLOR);
        pb.setSymbolColor(Color.BLACK);
        pb.setFillSymbol(true);
        pb.setSymbolInterval(1);

        LineString line = new LineString();
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            coordinates.add(new Coordinate(i, i * 0.5, force2D ? 0 : i * 0.4));
        }
        line.setCoordinates(coordinates);
        LineGraphicCollection lines = new LineGraphicCollection();
        lines.add(new LineGraphic(line, pb));
        lines.updateVertex();
        plot.addGraphic((Graphic) lines);
        plot.setDrawExtent(new Extent(-1, 6, -1, 4, -1, 3));
        if (!force2D) {
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

        int markerPixels = countColor(image, MARKER_COLOR, 30);
        int linePixels = countColor(image, LINE_COLOR, 30);
        check(markerPixels > 100, "markers were not drawn in " + name + ": " + markerPixels);
        if (drawLine) {
            check(linePixels > 50, "line was not drawn in " + name + ": " + linePixels);
        } else {
            check(linePixels < 20, "line should not be drawn in " + name + ": " + linePixels);
        }
    }

    private static int countColor(BufferedImage image, Color color, int tolerance) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (Math.abs(r - color.getRed()) + Math.abs(g - color.getGreen())
                        + Math.abs(b - color.getBlue()) <= tolerance) {
                    count++;
                }
            }
        }
        return count;
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
