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
import org.meteoinfo.geometry.legend.PointStyle;
import org.meteoinfo.geometry.legend.PolylineBreak;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Smoke test for the point markers drawn on line vertices by LineRender.
 */
public class LineMarkerRenderTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int SYMBOL_SIZE = 16;

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "target/line-marker-test");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir);
        }

        BufferedImage image = render(new File(dir, "line-markers.png"));

        // Red circles are drawn at every vertex of the first line.
        for (float x = -0.8f; x <= 0.81f; x += 0.4f) {
            checkColor(image, x, 0.6f, new Color(220, 30, 30), "circle marker at x=" + x);
        }

        // Green squares use a two vertex interval.
        checkColor(image, -0.8f, 0.2f, new Color(30, 160, 30), "square marker at -0.8");
        checkColor(image, 0.0f, 0.2f, new Color(30, 160, 30), "square marker at 0.0");
        checkColor(image, 0.8f, 0.2f, new Color(30, 160, 30), "square marker at 0.8");
        checkColor(image, -0.4f, 0.2f, Color.BLACK, "line only at -0.4");
        checkColor(image, 0.4f, 0.2f, Color.BLACK, "line only at 0.4");

        // The third line has no polyline, only blue triangle markers.
        checkColor(image, -0.8f, -0.2f, new Color(30, 60, 220), "triangle marker at -0.8");
        checkColor(image, 0.0f, -0.2f, new Color(30, 60, 220), "triangle marker at 0.0");
        checkColor(image, -0.6f, -0.2f, Color.WHITE, "no polyline at -0.6");

        // The fourth line has hollow magenta circles.
        checkColor(image, 0.0f, -0.6f, Color.WHITE, "hollow marker center");
        checkColor(image, 0.0f, -0.6f + (SYMBOL_SIZE / 2.0f) * 2.0f / HEIGHT,
                Color.MAGENTA, "hollow marker outline");

        System.out.println("OK");
    }

    private static BufferedImage render(File file) throws Exception {
        final LineGraphicCollection collection = new LineGraphicCollection();
        float[] xs = new float[]{-0.8f, -0.4f, 0.0f, 0.4f, 0.8f};

        PolylineBreak circles = new PolylineBreak();
        circles.setStyle(LineStyles.DASH);
        circles.setColor(Color.BLACK);
        circles.setDrawSymbol(true);
        circles.setSymbolStyle(PointStyle.CIRCLE);
        circles.setSymbolSize(SYMBOL_SIZE);
        circles.setSymbolFillColor(new Color(220, 30, 30));
        circles.setSymbolColor(Color.BLACK);
        circles.setFillSymbol(true);
        circles.setSymbolInterval(1);
        collection.add(createLine(xs, 0.6f, circles));

        PolylineBreak squares = new PolylineBreak();
        squares.setStyle(LineStyles.SOLID);
        squares.setColor(Color.BLACK);
        squares.setDrawSymbol(true);
        squares.setSymbolStyle(PointStyle.SQUARE);
        squares.setSymbolSize(SYMBOL_SIZE);
        squares.setSymbolFillColor(new Color(30, 160, 30));
        squares.setSymbolColor(Color.BLACK);
        squares.setFillSymbol(true);
        squares.setSymbolInterval(2);
        collection.add(createLine(xs, 0.2f, squares));

        PolylineBreak triangles = new PolylineBreak();
        triangles.setStyle(LineStyles.SOLID);
        triangles.setColor(Color.BLACK);
        triangles.setDrawPolyline(false);
        triangles.setDrawSymbol(true);
        triangles.setSymbolStyle(PointStyle.UP_TRIANGLE);
        triangles.setSymbolSize(SYMBOL_SIZE);
        triangles.setSymbolFillColor(new Color(30, 60, 220));
        triangles.setSymbolColor(Color.BLACK);
        triangles.setFillSymbol(true);
        triangles.setSymbolInterval(1);
        collection.add(createLine(xs, -0.2f, triangles));

        PolylineBreak hollow = new PolylineBreak();
        hollow.setStyle(LineStyles.SOLID);
        hollow.setColor(Color.BLACK);
        hollow.setDrawPolyline(false);
        hollow.setDrawSymbol(true);
        hollow.setSymbolStyle(PointStyle.CIRCLE);
        hollow.setSymbolSize(SYMBOL_SIZE);
        hollow.setSymbolFillColor(Color.WHITE);
        hollow.setSymbolColor(Color.MAGENTA);
        hollow.setFillSymbol(false);
        hollow.setSymbolInterval(1);
        collection.add(createLine(xs, -0.6f, hollow));

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

    private static LineGraphic createLine(float[] xs, float y, PolylineBreak pb) {
        LineString line = new LineString();
        List<Coordinate> coordinates = new ArrayList<>();
        for (float x : xs) {
            coordinates.add(new Coordinate(x, y, 0.0));
        }
        line.setCoordinates(coordinates);
        return new LineGraphic(line, pb);
    }

    private static void checkColor(BufferedImage image, float ndcX, float ndcY, Color color,
                                   String message) {
        int x = Math.round((ndcX + 1.0f) * 0.5f * image.getWidth());
        int y = Math.round((1.0f - (ndcY + 1.0f) * 0.5f) * image.getHeight());
        x = Math.max(0, Math.min(image.getWidth() - 1, x));
        y = Math.max(0, Math.min(image.getHeight() - 1, y));
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int distance = Math.abs(r - color.getRed()) + Math.abs(g - color.getGreen())
                + Math.abs(b - color.getBlue());
        if (distance > 30) {
            throw new AssertionError(message + ": expected " + color + " but was "
                    + r + "," + g + "," + b);
        }
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
}
