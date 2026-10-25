import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import org.meteoinfo.chart.GLChart;
import org.meteoinfo.chart.MouseMode;
import org.meteoinfo.chart.graphic.GraphicFactory;
import org.meteoinfo.chart.graphic.VolumeGraphic;
import org.meteoinfo.chart.jogl.EarthGLPlot;
import org.meteoinfo.chart.render.jogl.RayCastingType;
import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.math.ArrayUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Volume plot on the earth surface demo / verification harness.
 * <p>
 * Creates a 3D scalar field defined on a longitude / latitude / altitude grid and draws
 * it as a volume shell above the earth surface of an {@link EarthGLPlot}, using the
 * spherical ray casting render (EarthVolumeRender) instead of the axis aligned box
 * casting of a plain 3D volume plot.
 * <p>
 * Usage:
 * <pre>
 *   java EarthVolumeTest                     - interactive window
 *   java EarthVolumeTest shots &lt;dir&gt;         - render verification images offscreen and exit
 *   java EarthVolumeTest diag &lt;dir&gt;          - render geometry alignment diagnostics and exit
 *   java EarthVolumeTest info                - print plot / extent diagnostics
 * </pre>
 */
public class EarthVolumeTest {

    /**
     * Blue -> green -> yellow -> red color map
     *
     * @return Color map
     */
    public static ColorMap warmColorMap() {
        List<Color> cs = new ArrayList<>();
        int n = 256;
        for (int i = 0; i < n; i++) {
            float t = (float) i / (n - 1);
            Color c;
            if (t < 0.4f) {
                float f = t / 0.4f;
                c = new Color(0, (int) (120 * f), (int) (255 - 100 * f));
            } else if (t < 0.7f) {
                float f = (t - 0.4f) / 0.3f;
                c = new Color((int) (255 * f), (int) (255 - 100 * f), 30);
            } else {
                float f = (t - 0.7f) / 0.3f;
                c = new Color(255, (int) (155 - 155 * f), (int) (30 - 30 * f));
            }
            cs.add(c);
        }
        return new ColorMap(cs);
    }

    /**
     * A warm air mass like scalar field, on a lon/lat/alt grid.
     *
     * @return Volume graphic
     */
    public static VolumeGraphic createVolume() {
        return createVolume(-25, 65, 0, 60, 200, 9000);
    }

    /**
     * Create a volume on a longitude / latitude / altitude grid.
     *
     * @param lon0 Minimum longitude
     * @param lon1 Maximum longitude
     * @param lat0 Minimum latitude
     * @param lat1 Maximum latitude
     * @param alt0 Minimum altitude
     * @param alt1 Maximum altitude
     * @return Volume graphic
     */
    public static VolumeGraphic createVolume(double lon0, double lon1, double lat0, double lat1,
                                             double alt0, double alt1) {
        int nx = 64;    // longitude points
        int ny = 48;    // latitude points
        int nz = 24;    // altitude levels

        Array xa = ArrayUtil.lineSpace(lon0, lon1, nx, true);
        Array ya = ArrayUtil.lineSpace(lat0, lat1, ny, true);
        Array za = ArrayUtil.lineSpace(alt0, alt1, nz, true);

        // value array shape: [alt, lat, lon]
        Array value = ArrayUtil.zeros(new int[]{nz, ny, nx}, DataType.FLOAT);
        double cLon = (lon0 + lon1) / 2;
        double cLat = (lat0 + lat1) / 2;
        double cAlt = alt0 + (alt1 - alt0) * 0.3;
        double sLon = (lon1 - lon0) / 6;
        double sLat = (lat1 - lat0) / 6;
        double sAlt = (alt1 - alt0) / 3.5;
        for (int k = 0; k < nz; k++) {
            double alt = za.getDouble(k);
            double dz = (alt - cAlt) / sAlt;
            for (int j = 0; j < ny; j++) {
                double lat = ya.getDouble(j);
                double dy = (lat - cLat) / sLat;
                for (int i = 0; i < nx; i++) {
                    double lon = xa.getDouble(i);
                    double dx = (lon - cLon) / sLon;
                    double g = Math.exp(-(dx * dx + dy * dy + dz * dz) / 2.0);
                    value.setDouble(k * ny * nx + j * nx + i, g * 100);
                }
            }
        }

        VolumeGraphic volume = (VolumeGraphic) GraphicFactory.volume(value, xa, ya, za, warmColorMap(), 0, 100,
                0f, 0.85f);

        // Opacity: hide low values, show the core of the air mass
        List<Number> nodes = Arrays.asList(0f, 0.5f, 0.75f, 1f);
        List<Number> levels = Arrays.asList(0f, 0.0f, 0.3f, 0.85f);
        volume.setOpacityTransferFunction(nodes, levels);
        volume.updateColors();
        volume.setRayCastingType(RayCastingType.BASIC);
        return volume;
    }

    /**
     * Create a constant value volume, used to check the geometry of the shell sector
     *
     * @param lon0 Minimum longitude
     * @param lon1 Maximum longitude
     * @param lat0 Minimum latitude
     * @param lat1 Maximum latitude
     * @param alt0 Minimum altitude
     * @param alt1 Maximum altitude
     * @param color Fill color
     * @param alpha Alpha value
     * @return Volume graphic
     */
    public static VolumeGraphic createConstantVolume(double lon0, double lon1, double lat0, double lat1,
                                                     double alt0, double alt1, Color color, float alpha) {
        Array xa = ArrayUtil.lineSpace(lon0, lon1, 4, true);
        Array ya = ArrayUtil.lineSpace(lat0, lat1, 4, true);
        Array za = ArrayUtil.lineSpace(alt0, alt1, 4, true);
        Array value = ArrayUtil.zeros(new int[]{4, 4, 4}, DataType.FLOAT);
        for (int i = 0; i < value.getSize(); i++) {
            value.setDouble(i, 50);
        }
        List<Color> cs = new ArrayList<>();
        cs.add(color);
        cs.add(color);
        VolumeGraphic volume = (VolumeGraphic) GraphicFactory.volume(value, xa, ya, za, new ColorMap(cs), 0, 100,
                alpha, alpha);
        volume.setRayCastingType(RayCastingType.MAX_VALUE);
        return volume;
    }

    private static String fmt(org.meteoinfo.geometry.Extent e) {
        if (e == null) {
            return "null";
        }
        return String.format("x[%.1f, %.1f] y[%.1f, %.1f] z[%.1f, %.1f]", e.minX, e.maxX, e.minY, e.maxY,
                e.minZ, e.maxZ);
    }

    private static void printInfo(EarthGLPlot plot) {
        System.out.println("radius = " + plot.getRadius());
        System.out.println("drawExtent  = " + fmt(plot.getDrawExtent()));
        System.out.println("graphicExtent = " + fmt(plot.getGraphicExtent()));
        org.meteoinfo.chart.graphic.GraphicCollection gs = plot.getGraphics();
        for (int i = 0; i < gs.getNumGraphics(); i++) {
            org.meteoinfo.chart.graphic.Graphic g = gs.get(i);
            System.out.println("  graphic[" + i + "] " + g.getClass().getSimpleName() + " extent=" + fmt(g.getExtent()));
            if (g instanceof VolumeGraphic) {
                VolumeGraphic v = (VolumeGraphic) g;
                System.out.println("    geo=" + fmt(v.getGeoExtent()) + " spherical=" + v.isSpherical());
            }
        }
    }

    /**
     * Create an offscreen GL drawable
     *
     * @param width Width
     * @param height Height
     * @return Drawable
     */
    private static GLAutoDrawable createDrawable(int width, int height) {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);
        caps.setSampleBuffers(true);
        caps.setNumSamples(4);
        return GLDrawableFactory.getFactory(profile)
                .createOffscreenAutoDrawable(null, caps, null, width, height);
    }

    /**
     * Render a plot offscreen and save the image
     *
     * @param chart Chart
     * @param drawable Drawable
     * @param dir Output directory
     * @param name File name
     */
    private static void save(GLChart chart, GLAutoDrawable drawable, File dir, String name) throws Exception {
        drawable.display();
        chart.setDoScreenShot(true);
        drawable.display();
        BufferedImage img = chart.getScreenImage();
        File file = new File(dir, name + ".png");
        ImageIO.write(img, "png", file);
        System.out.println("Saved " + file + " (" + img.getWidth() + "x" + img.getHeight() + ")");
    }

    /**
     * Render geometry alignment diagnostics: a constant volume sector should cover the
     * centre of the visible disk when the camera looks at the sector centre.
     *
     * @param outDir Output directory
     */
    private static void renderDiag(String outDir) throws Exception {
        File dir = new File(outDir);
        dir.mkdirs();
        GLAutoDrawable drawable = createDrawable(900, 700);

        // Camera looking at lon 20 / lat 20: lat = 90 + angleX, lon = 270 - angleY
        float angleX = -70.f;
        float angleY = 250.f;

        Object[][] scenes = new Object[][]{
                {"d1_global_const", false, -180, 180, -90, 90, 0, 300, Color.red, 0.7f},
                {"d2_sector_const", false, 0, 40, 0, 40, 0, 300, Color.red, 0.7f},
                {"d3_sector_surface", true, 0, 40, 0, 40, 0, 300, Color.red, 0.7f},
                {"d4_global_surface", true, -180, 180, -90, 90, 0, 300, Color.red, 0.7f},
        };

        for (Object[] scene : scenes) {
            EarthGLPlot plot = new EarthGLPlot();
            plot.setAngleX(angleX);
            plot.setAngleY(angleY);
            plot.setOrthographic(false);
            if ((Boolean) scene[1]) {
                plot.addGraphic(plot.earthSurface(60));
            }
            plot.addGraphic(createConstantVolume((Integer) scene[2], (Integer) scene[3],
                    (Integer) scene[4], (Integer) scene[5], (Integer) scene[6], (Integer) scene[7],
                    (Color) scene[8], (Float) scene[9]));
            plot.setAlwaysUpdateBuffers(true);

            GLChart chart = new GLChart();
            chart.addPlot(plot);
            drawable.addGLEventListener(chart);
            save(chart, drawable, dir, (String) scene[0]);
            drawable.removeGLEventListener(chart);
        }
        drawable.destroy();
        System.out.println("DONE");
        System.exit(0);
    }

    /**
     * Render verification scenes offscreen and save PNG files.
     *
     * @param outDir Output directory
     */
    private static void renderShots(String outDir) throws Exception {
        File dir = new File(outDir);
        dir.mkdirs();
        GLAutoDrawable drawable = createDrawable(900, 700);

        // Scene: name, earth surface, volume, lon0, lon1, lat0, lat1, alt0, alt1,
        //        angleX, angleY, orthographic, ray casting type
        Object[][] scenes = new Object[][]{
                {"01_surface_only", true, false, 0, 1, 0, 1, 0, 1, -70.f, 250.f, false, null},
                {"02_thin_basic", true, true, -60, 100, -20, 60, 0, 400, -70.f, 250.f, false, RayCastingType.BASIC},
                {"03_thin_max", true, true, -60, 100, -20, 60, 0, 400, -70.f, 250.f, false, RayCastingType.MAX_VALUE},
                {"04_thick_basic", true, true, -60, 100, -20, 60, 0, 3000, -70.f, 250.f, false, RayCastingType.BASIC},
                {"05_global", true, true, -180, 180, -90, 90, 0, 800, -70.f, 250.f, false, RayCastingType.BASIC},
                {"06_side", true, true, -60, 100, -20, 60, 0, 3000, -20.f, 200.f, false, RayCastingType.BASIC},
                {"07_ortho", true, true, -60, 100, -20, 60, 0, 3000, -70.f, 250.f, true, RayCastingType.BASIC},
                {"08_specular", true, true, -60, 100, -20, 60, 0, 3000, -70.f, 250.f, false, RayCastingType.SPECULAR},
                
                {"09_volume_only", false, true, -60, 100, -20, 60, 0, 3000, -70.f, 250.f, false, RayCastingType.BASIC},
                
                {"10_back_side", true, true, 150, 250, -30, 30, 0, 3000, -70.f, 250.f, false, RayCastingType.BASIC},
                
                {"11_thin_plume", true, true, -30, 60, 5, 55, 0, 12, -70.f, 250.f, false, RayCastingType.BASIC},
                {"12_limb", true, true, -60, 100, -20, 60, 0, 3000, -45.f, 160.f, false, RayCastingType.BASIC},
                {"13_limb_max", true, true, -60, 100, -20, 60, 0, 3000, -45.f, 160.f, false, RayCastingType.MAX_VALUE},
        };

        for (Object[] scene : scenes) {
            EarthGLPlot plot = new EarthGLPlot();
            plot.setAngleX((Float) scene[9]);
            plot.setAngleY((Float) scene[10]);
            plot.setOrthographic((Boolean) scene[11]);
            if ((Boolean) scene[1]) {
                plot.addGraphic(plot.earthSurface(60));
            }
            if ((Boolean) scene[2]) {
                VolumeGraphic volume = createVolume((Integer) scene[3], (Integer) scene[4],
                        (Integer) scene[5], (Integer) scene[6], (Integer) scene[7], (Integer) scene[8]);
                if (scene[12] != null) {
                    volume.setRayCastingType((RayCastingType) scene[12]);
                }
                plot.addGraphic(volume);
            }
            plot.setAlwaysUpdateBuffers(true);

            GLChart chart = new GLChart();
            chart.addPlot(plot);
            drawable.addGLEventListener(chart);
            save(chart, drawable, dir, (String) scene[0]);
            drawable.removeGLEventListener(chart);
        }
        drawable.destroy();
        System.out.println("DONE");
        System.exit(0);
    }

    /**
     * Measure the rendering time of the spherical volume plot
     *
     * @param frames Number of frames to render
     */
    private static void renderPerf(int frames) throws Exception {
        GLAutoDrawable drawable = createDrawable(900, 700);
        EarthGLPlot plot = new EarthGLPlot();
        plot.setAngleX(-70.f);
        plot.setAngleY(250.f);
        plot.setOrthographic(false);
        plot.addGraphic(plot.earthSurface(60));
        plot.addGraphic(createVolume(-60, 100, -20, 60, 0, 3000));
        plot.setAlwaysUpdateBuffers(true);

        GLChart chart = new GLChart();
        chart.addPlot(plot);
        drawable.addGLEventListener(chart);
        for (int i = 0; i < 5; i++) {
            drawable.display();
        }
        long t0 = System.nanoTime();
        for (int i = 0; i < frames; i++) {
            drawable.display();
        }
        long t1 = System.nanoTime();
        System.out.printf("Average frame time: %.1f ms (%.1f fps), %d frames%n",
                (t1 - t0) / 1e6 / frames, frames * 1e9 / (t1 - t0), frames);
        drawable.destroy();
        System.exit(0);
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("shots")) {
            renderShots(args.length > 1 ? args[1] : ".");
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("diag")) {
            renderDiag(args.length > 1 ? args[1] : ".");
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("perf")) {
            renderPerf(args.length > 1 ? Integer.parseInt(args[1]) : 60);
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            EarthGLPlot plot = new EarthGLPlot();
            plot.addGraphic(plot.earthSurface(60));
            plot.addGraphic(createVolume());
            printInfo(plot);
            System.exit(0);
        }

        // Interactive window
        javax.swing.SwingUtilities.invokeLater(() -> {
            EarthGLPlot plot = new EarthGLPlot();
            plot.initAngles();
            plot.addGraphic(plot.earthSurface(60));
            plot.addGraphic(createVolume());
            org.meteoinfo.chart.GLChartPanel panel = new org.meteoinfo.chart.GLChartPanel(new GLChart(plot));
            panel.setMouseMode(MouseMode.ROTATE);
            javax.swing.JFrame frame = new javax.swing.JFrame("Earth Volume Plot Test");
            frame.getContentPane().add(panel);
            frame.setSize(900, 700);
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
