import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.chart.jogl.EarthGLPlot;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.legend.StreamlineBreak;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies that a plain GraphicCollection of streamlines (line graphics) is
 * transformed and rendered by EarthGLPlot instead of being silently skipped.
 */
public class EarthStreamlineRenderTest {

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/earth-stream-test");
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
        plot.setAngleX(0);
        plot.setAngleY(0);
        plot.setHeadAngle(-90);
        plot.addGraphic(plot.earthSurface(50));

        GraphicCollection streamlines = new GraphicCollection();
        for (int k = 0; k < 4; k++) {
            List<Coordinate> coords = new ArrayList<>();
            for (int i = 0; i <= 30; i++) {
                double lon = -30 + 60.0 * i / 30;
                double lat = -15 + 10 * k;
                coords.add(new Coordinate(lon, lat, 0));
            }
            StreamlineBreak sb = new StreamlineBreak();
            sb.setColor(Color.RED);
            sb.setWidth(2);
            streamlines.add(new Graphic(new LineString(coords), sb));
        }
        plot.addGraphic(streamlines);
        for (int i = 0; i < plot.getGraphics().getNumGraphics(); i++) {
            Graphic g = plot.getGraphics().getGraphicN(i);
            System.out.println(i + " " + g.getClass() + " n=" + g.getNumGraphics());
        }
        plot.getLegendScheme();
        check(plot.getLegendScheme() != null
                && plot.getLegendScheme().getBreakNum() > 0, "empty legend scheme after streamplot");

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);
        GLOffscreenAutoDrawable drawable = GLDrawableFactory.getFactory(profile)
                .createOffscreenAutoDrawable(null, caps, null, 800, 600);
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
        ImageIO.write(image, "png", new File(dir, "earth-streamlines.png"));

        int red = countColor(image, new Color(255, 0, 0), 30);
        System.out.println("red=" + red);
        check(red > 40, "streamlines were not drawn");
        System.out.println("OK");
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
