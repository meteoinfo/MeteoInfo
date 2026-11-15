import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GL2;
import org.joml.Matrix4f;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.render.jogl.PointRender;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.legend.PointStyle;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Smoke test for the flat point markers drawn by PointRender.
 *
 * <p>The test renders one row of markers for every {@link PointStyle} in an
 * offscreen OpenGL context with a known projection, then verifies the pixel
 * layout of representative marker shapes.</p>
 */
public class PointMarkerRenderTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final float MARKER_SIZE = 26;

    private static final PointStyle[] STYLES = new PointStyle[]{
            PointStyle.CIRCLE, PointStyle.SQUARE, PointStyle.UP_TRIANGLE,
            PointStyle.DOWN_TRIANGLE, PointStyle.DIAMOND,
            PointStyle.X_CROSS, PointStyle.PLUS, PointStyle.MINUS,
            PointStyle.STAR, PointStyle.STAR_LINES,
            PointStyle.PENTAGON, PointStyle.UP_SEMI_CIRCLE,
            PointStyle.DOWN_SEMI_CIRCLE, PointStyle.DOUBLE_CIRCLE,
            PointStyle.CIRCLE_STAR};

    private static final Color[] COLORS = new Color[]{
            new Color(220, 30, 30), new Color(30, 160, 30), new Color(30, 60, 220),
            new Color(240, 150, 0), new Color(190, 30, 190),
            new Color(0, 150, 150), new Color(120, 80, 20), new Color(90, 90, 90),
            new Color(200, 0, 100), new Color(0, 90, 200),
            new Color(140, 0, 0), new Color(0, 140, 0), new Color(0, 0, 140),
            new Color(210, 120, 0), new Color(120, 0, 120)};

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/point-marker-test");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }

        BufferedImage image = renderGrid(new Matrix4f(), new File(dir, "markers-ortho.png"));

        // Every marker is filled, so an interior pixel must carry its fill color.
        for (int i = 0; i < STYLES.length; i++) {
            float dy = 0;
            if (STYLES[i] == PointStyle.UP_SEMI_CIRCLE) {
                dy = 6;     // the center lies on the diameter outline
            } else if (STYLES[i] == PointStyle.DOWN_SEMI_CIRCLE) {
                dy = -6;
            }
            checkOffset(image, i, 0, dy, COLORS[i], STYLES[i] + " center");
        }

        // A circle does not reach the corners of its bounding box, a square does.
        checkOffset(image, 1, 10, 10, COLORS[1], "square corner");
        checkOffset(image, 0, 10, 10, Color.WHITE, "circle corner");

        // Up triangle has its apex above the center and no fill below the base.
        checkOffset(image, 2, 0, 9, COLORS[2], "up triangle apex");
        checkOffset(image, 2, -8, -11, Color.WHITE, "up triangle lower left");
        checkOffset(image, 3, 0, -9, COLORS[3], "down triangle apex");
        checkOffset(image, 3, 8, 11, Color.WHITE, "down triangle upper right");

        // Diamond has horizontal points but not diagonal corners.
        checkOffset(image, 4, 9, 0, COLORS[4], "diamond right point");
        checkOffset(image, 4, 9, 9, Color.WHITE, "diamond corner");

        // Star is concave at the top-left diagonal, circle_star has a filled center.
        checkOffset(image, 14, 0, 0, COLORS[14], "circle star center");

        // The same geometry, viewed from a rotated camera, must still be built.
        Matrix4f rotation = new Matrix4f().rotate((float) Math.toRadians(30), 1, 0, 0)
                .rotate((float) Math.toRadians(25), 0, 1, 0);
        BufferedImage rotated = renderGrid(rotation, new File(dir, "markers-rotated.png"));
        check(!imagesEqual(image, rotated), "rotated camera must produce a different image");

        // Hollow marker: outline only.
        BufferedImage hollow = renderHollow(new File(dir, "marker-hollow.png"));
        checkBackground(hollow, 0.0f, 0.0f, "hollow circle center");
        checkNear(hollow, 0.0f, 0.1f, Color.RED, "hollow circle top outline");

        System.out.println("OK");
    }

    private static BufferedImage renderGrid(final Matrix4f modelView, File file) throws Exception {
        final GraphicCollection graphics = new GraphicCollection();
        for (int i = 0; i < STYLES.length; i++) {
            PointBreak pb = new PointBreak();
            pb.setStyle(STYLES[i]);
            pb.setSize(MARKER_SIZE);
            pb.setColor(COLORS[i]);
            pb.setOutlineColor(Color.BLACK);
            pb.setOutlineSize(1.0f);
            graphics.add(new Graphic(new Point(new Coordinate(gridX(i), gridY(i), 0.0)), pb));
        }
        return render(graphics, modelView, file);
    }

    private static BufferedImage renderHollow(File file) throws Exception {
        GraphicCollection graphics = new GraphicCollection();
        PointBreak pb = new PointBreak();
        pb.setStyle(PointStyle.CIRCLE);
        pb.setSize(40);
        pb.setColor(Color.BLUE);
        pb.setOutlineColor(Color.RED);
        pb.setOutlineSize(2.0f);
        pb.setDrawFill(false);
        pb.setDrawOutline(true);
        graphics.add(new Graphic(new Point(new Coordinate(0.0, 0.0, 0.0)), pb));
        return render(graphics, new Matrix4f(), file);
    }

    private static BufferedImage render(final GraphicCollection graphics, final Matrix4f modelView,
                                        File file) throws Exception {
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
                gl.glMultMatrixf(modelView.get(new float[16]), 0);

                PointRender render = new PointRender(gl, graphics);
                render.setTransform(new Transform(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f), true);
                render.updateMatrix();
                render.setRotateModelView(new Matrix4f(modelView));
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

    private static float gridX(int index) {
        int col = index % 5;
        return -0.8f + col * 0.4f;
    }

    private static float gridY(int index) {
        int row = index / 5;
        return 0.6f - row * 0.6f;
    }

    private static void checkOffset(BufferedImage image, int index, float dx, float dy,
                                    Color expected, String message) {
        float ndcX = gridX(index) + dx * 2.0f / image.getWidth();
        float ndcY = gridY(index) + dy * 2.0f / image.getHeight();
        checkNear(image, ndcX, ndcY, expected, message);
    }

    private static void checkNear(BufferedImage image, float ndcX, float ndcY, Color expected,
                                  String message) {
        int[] rgb = pixel(image, ndcX, ndcY);
        int er = expected.getRed();
        int eg = expected.getGreen();
        int eb = expected.getBlue();
        if (Math.abs(rgb[0] - er) + Math.abs(rgb[1] - eg) + Math.abs(rgb[2] - eb) > 30) {
            throw new AssertionError(message + ": expected " + er + "," + eg + "," + eb
                    + " but was " + rgb[0] + "," + rgb[1] + "," + rgb[2]);
        }
    }

    private static void checkBackground(BufferedImage image, float ndcX, float ndcY,
                                        String message) {
        checkNear(image, ndcX, ndcY, Color.WHITE, message);
    }

    private static int[] pixel(BufferedImage image, float ndcX, float ndcY) {
        int x = Math.round((ndcX + 1.0f) * 0.5f * image.getWidth());
        int y = Math.round((1.0f - (ndcY + 1.0f) * 0.5f) * image.getHeight());
        x = Math.max(0, Math.min(image.getWidth() - 1, x));
        y = Math.max(0, Math.min(image.getHeight() - 1, y));
        int rgb = image.getRGB(x, y);
        return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
    }

    private static boolean imagesEqual(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }
        List<Integer> differences = new ArrayList<>();
        for (int y = 0; y < a.getHeight(); y += 4) {
            for (int x = 0; x < a.getWidth(); x += 4) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    differences.add(1);
                    if (differences.size() > 20) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
