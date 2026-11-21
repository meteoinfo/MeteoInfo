import org.meteoinfo.chart.GLChart;
import org.meteoinfo.chart.GLChartPanel;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.LineGraphic;
import org.meteoinfo.chart.graphic.LineGraphicCollection;
import org.meteoinfo.chart.graphic.PointGraphic;
import org.meteoinfo.chart.graphic.PointGraphicCollection;
import org.meteoinfo.chart.jogl.GLPlot;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.geometry.legend.LineStyles;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.legend.PolylineBreak;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * On-screen HiDPI check for GLPlot.
 *
 * <p>The test creates a 400x300 GLChartPanel and verifies that the plot's DPI
 * scale matches the actual surface/component pixel ratio of the JOGL panel. It
 * is skipped in headless environments.</p>
 */
public class GLPlotHiDPITest {

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/hidpi-test");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("SKIP: headless environment");
            return;
        }

        final BufferedImage[] imageHolder = new BufferedImage[1];
        final float[] result = new float[4];
        SwingUtilities.invokeAndWait(() -> {
            GLChart chart = new GLChart();
            GLPlot plot = new GLPlot();
            plot.setBackground(Color.WHITE);
            plot.setForeground(Color.BLACK);
            plot.setDrawBoundingBox(false);
            plot.setBoxed(false);
            plot.setDrawBase(false);
            plot.getGridLine().setDrawXLine(false);
            plot.getGridLine().setDrawYLine(false);
            plot.getGridLine().setDrawZLine(false);
            plot.setTitle("TEST");

            PolylineBreak pb = new PolylineBreak();
            pb.setColor(Color.BLUE);
            pb.setWidth(1.0f);
            pb.setStyle(LineStyles.SOLID);
            LineString line = new LineString();
            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(new Coordinate(-1, 0.25, 0));
            coordinates.add(new Coordinate(1, 0.25, 0));
            line.setCoordinates(coordinates);
            LineGraphicCollection lines = new LineGraphicCollection();
            lines.add(new LineGraphic(line, pb));
            lines.updateVertex();
            plot.addGraphic((Graphic) lines);

            PointBreak pointBreak = new PointBreak();
            pointBreak.setColor(new Color(30, 180, 30));
            pointBreak.setOutlineColor(Color.BLACK);
            pointBreak.setSize(10);
            PointGraphicCollection points = new PointGraphicCollection();
            points.add(new PointGraphic(new Point(new Coordinate(0, 0.5, 0)), pointBreak));
            plot.addGraphic((Graphic) points);
            plot.setDrawExtent(new Extent(-1, 1, 0, 1, 0, 1));
            chart.addPlot(plot);

            GLChartPanel panel = new GLChartPanel(chart, 400, 300);
            JFrame frame = new JFrame("GLPlot HiDPI test");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setVisible(true);
            panel.display();
            panel.display();

            result[0] = plot.getDpiScale();
            result[1] = (float) panel.getSurfaceWidth() / panel.getWidth();
            result[2] = panel.getSurfaceWidth();
            result[3] = panel.getSurfaceHeight();
            System.out.println("component=" + panel.getWidth() + "x" + panel.getHeight()
                    + " surface=" + (int) result[2] + "x" + (int) result[3]
                    + " dpiScale=" + result[0] + " expected=" + result[1]);

            imageHolder[0] = readImage(panel);
            frame.dispose();
        });

        BufferedImage image = imageHolder[0];
        ImageIO.write(image, "png", new File(dir, "glplot-hidpi.png"));

        float actual = result[0];
        float expected = result[1];
        if (Math.abs(actual - expected) > 0.01f) {
            throw new AssertionError("DPI scale mismatch: actual=" + actual
                    + " expected=" + expected);
        }
        if (actual < 1.0f) {
            throw new AssertionError("DPI scale should not be smaller than 1: " + actual);
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = -1, maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (Math.abs(r - 30) + Math.abs(g - 180) + Math.abs(b - 30) <= 30) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < 0) {
            throw new AssertionError("point marker was not drawn");
        }
        int markerHeight = maxY - minY + 1;
        float markerExpected = 10.0f * expected;
        if (markerHeight < markerExpected * 0.65f || markerHeight > markerExpected * 1.2f) {
            throw new AssertionError("point marker size should be scaled by DPI, height="
                    + markerHeight + " expected about " + markerExpected);
        }
        System.out.println("point marker height=" + markerHeight);

        if (expected > 1.5f) {
            int centerX = image.getWidth() / 2;
            int blueThickness = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(centerX, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (b > 150 && r < 100 && g < 100) {
                    blueThickness++;
                }
            }
            if (blueThickness < 2) {
                throw new AssertionError("line width should be scaled by DPI, thickness="
                        + blueThickness);
            }
            System.out.println("line thickness=" + blueThickness);
        }
        System.out.println("OK");
    }

    private static BufferedImage readImage(GLChartPanel panel) {
        int w = panel.getSurfaceWidth();
        int h = panel.getSurfaceHeight();
        panel.getContext().makeCurrent();
        com.jogamp.opengl.GL2 gl = panel.getGL().getGL2();
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
}
