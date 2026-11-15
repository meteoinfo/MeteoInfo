import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GL2;
import org.joml.Matrix4f;
import org.meteoinfo.chart.graphic.LineGraphic;
import org.meteoinfo.chart.graphic.LineGraphicCollection;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.render.jogl.LineRender;
import org.meteoinfo.geometry.Coordinate;
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
 * Smoke test for the line styles drawn by LineRender.
 *
 * <p>Five horizontal lines with different line styles are rendered in an
 * offscreen OpenGL context. The pixel runs along each line are checked for the
 * expected dash/dot pattern.</p>
 */
public class LineStyleRenderTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;

    private static final LineStyles[] STYLES = new LineStyles[]{
            LineStyles.SOLID, LineStyles.DASH, LineStyles.DOT,
            LineStyles.DASH_DOT, LineStyles.DASH_DOT_DOT};

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/line-style-test");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }

        BufferedImage image = render(new File(dir, "line-styles.png"));

        for (int i = 0; i < STYLES.length; i++) {
            float y = 0.6f - i * 0.3f;
            List<int[]> runs = runs(image, y);
            int onRuns = 0;
            int onPixels = 0;
            int longest = 0;
            int shortest = Integer.MAX_VALUE;
            for (int[] run : runs) {
                if (run[0] == 1) {
                    onRuns++;
                    onPixels += run[1];
                    longest = Math.max(longest, run[1]);
                    shortest = Math.min(shortest, run[1]);
                }
            }

            switch (STYLES[i]) {
                case SOLID:
                    check(onRuns == 1, "SOLID should be a continuous line but has "
                            + onRuns + " runs");
                    check(longest > WIDTH / 2, "SOLID line is too short: " + longest);
                    break;
                case DASH:
                    check(onRuns > 20, "DASH should have many dashes but has " + onRuns);
                    check(longest <= 7, "DASH run is too long: " + longest);
                    break;
                case DOT:
                    check(onRuns > 60, "DOT should have many dots but has " + onRuns);
                    check(longest <= 3, "DOT run is too long: " + longest);
                    break;
                case DASH_DOT:
                    check(onRuns > 20, "DASH_DOT should have many segments but has " + onRuns);
                    check(longest >= 5 && shortest <= 2,
                            "DASH_DOT should mix long dashes and dots: " + shortest + "-" + longest);
                    break;
                case DASH_DOT_DOT:
                    check(onRuns > 20, "DASH_DOT_DOT should have many segments but has " + onRuns);
                    check(longest >= 8 && shortest <= 2,
                            "DASH_DOT_DOT should mix long dashes and dots: " + shortest + "-" + longest);
                    break;
                default:
                    break;
            }
            System.out.println(STYLES[i] + ": runs=" + onRuns + ", pixels=" + onPixels
                    + ", shortest=" + shortest + ", longest=" + longest);
        }

        System.out.println("OK");
    }

    private static BufferedImage render(File file) throws Exception {
        final LineGraphicCollection collection = new LineGraphicCollection();
        List<LineGraphic> lines = new ArrayList<>();
        for (int i = 0; i < STYLES.length; i++) {
            PolylineBreak pb = new PolylineBreak();
            pb.setStyle(STYLES[i]);
            pb.setColor(Color.BLACK);
            pb.setWidth(1.0f);
            LineString line = new LineString();
            List<Coordinate> coordinates = new ArrayList<>();
            float y = 0.6f - i * 0.3f;
            coordinates.add(new Coordinate(-0.8, y, 0.0));
            coordinates.add(new Coordinate(0.8, y, 0.0));
            line.setCoordinates(coordinates);
            lines.add(new LineGraphic(line, pb));
        }
        for (LineGraphic line : lines) {
            collection.add(line);
        }
        collection.updateVertex();

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);
        GLOffscreenAutoDrawable drawable = GLDrawableFactory.getFactory(profile)
                .createOffscreenAutoDrawable(null, caps, null, WIDTH, HEIGHT);
        drawable.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
                gl.glEnable(GL2.GL_DEPTH_TEST);
                gl.glDepthFunc(GL2.GL_LEQUAL);
                gl.glViewport(0, 0, WIDTH, HEIGHT);
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glOrthof(-1.0f, 1.0f, -1.0f, 1.0f, -2.0f, 2.0f);
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();
            }

            @Override
            public void dispose(GLAutoDrawable d) {
            }

            @Override
            public void display(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();
                LineRender render = new LineRender(gl, collection);
                render.setTransform(new Transform(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f), true);
                render.updateMatrix();
                render.setRotateModelView(new Matrix4f());
                render.draw();
                gl.glFlush();
            }

            @Override
            public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
            }
        });
        drawable.display();
        drawable.display();
        BufferedImage image = readImage(drawable);
        ImageIO.write(image, "png", file);
        System.out.println(file.getAbsolutePath());
        return image;
    }

    private static List<int[]> runs(BufferedImage image, float ndcY) {
        int y = Math.round((1.0f - (ndcY + 1.0f) * 0.5f) * image.getHeight());
        y = Math.max(0, Math.min(image.getHeight() - 1, y));
        List<int[]> runs = new ArrayList<>();
        int current = -1;
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            int rgb = image.getRGB(x, y) & 0xFFFFFF;
            int on = rgb == 0xFFFFFF ? 0 : 1;
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

    private static BufferedImage readImage(GLOffscreenAutoDrawable drawable) {
        drawable.getContext().makeCurrent();
        GL2 gl = drawable.getGL().getGL2();
        int w = drawable.getSurfaceWidth();
        int h = drawable.getSurfaceHeight();
        java.nio.ByteBuffer buffer = com.jogamp.common.nio.Buffers.newDirectByteBuffer(w * h * 4);
        gl.glReadPixels(0, 0, w, h, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, buffer);
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
