package org.meteoinfo.chart;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.gl2.GLUT;
import org.meteoinfo.chart.jogl.Axes;
import org.meteoinfo.chart.jogl.Program;

import java.util.List;

/**
 * OpenGL renderer for a {@link Figure}.
 * <p>
 * The chart owns the model (plots, titles, layout and style); this class owns
 * the JOGL event handling and drawing state.  Keeping the renderer separate
 * makes it possible to use the same chart model with a different drawable or
 * rendering backend.
 */
public class FigureRenderer implements GLEventListener {

    private final Figure chart;
    private GLAutoDrawable drawable;
    private GL2 gl;
    private GLU glu;
    private final GLUT glut = new GLUT();
    private Axes.TessCallback tessCallback;
    private int width;
    private int height;

    /**
     * Constructor.
     *
     * @param chart Chart model to render
     */
    public FigureRenderer(Figure chart) {
        if (chart == null) {
            throw new IllegalArgumentException("chart must not be null");
        }
        this.chart = chart;
    }

    /**
     * Get the chart model.
     *
     * @return Chart model
     */
    public Figure getChart() {
        return this.chart;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        this.drawable = drawable;
        drawable.getGL().setSwapInterval(1);

        GL2 gl = drawable.getGL().getGL2();
        this.gl = gl;
        this.glu = GLU.createGLU(gl);

        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

        // JOGL specific tessellation callback.
        this.tessCallback = new Axes.TessCallback(gl, glu);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        Program.destroyAllPrograms(gl);
        this.chart.setAlwaysUpdateBuffers(true);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (!this.chart.containsGLPlot()) {
            return;
        }

        float[] rgba = this.chart.getBackground().getRGBComponents(null);
        this.gl.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        this.gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT
                | GL2.GL_STENCIL_BUFFER_BIT);

        this.gl.glShadeModel(GL2.GL_SMOOTH);
        this.gl.glEnable(GL2.GL_BLEND);
        this.gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        if (this.chart.isAntialias()) {
            if (this.chart.isSampleBuffers()) {
                this.gl.glEnable(GL2.GL_MULTISAMPLE);
            } else {
                this.gl.glEnable(GL2.GL_LINE_SMOOTH);
                this.gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
                this.gl.glEnable(GL2.GL_POINT_SMOOTH);
                this.gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
            }
        } else {
            if (this.chart.isSampleBuffers()) {
                this.gl.glDisable(GL2.GL_MULTISAMPLE);
            } else {
                this.gl.glDisable(GL2.GL_LINE_SMOOTH);
                this.gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_FASTEST);
                this.gl.glDisable(GL2.GL_POINT_SMOOTH);
                this.gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_FASTEST);
            }
        }

        List<Axes> glPlots = this.chart.getGLPlots();
        for (Axes glPlot : glPlots) {
            glPlot.reshape(drawable, 0, 0, this.width, this.height);
            // The chart has already cleared the shared color buffer.  Clearing
            // it again for a second overlapping axes would erase the first one.
            glPlot.setClearBuffer(false);
            glPlot.display(drawable);
        }

        if (this.chart.doScreenShot) {
            AWTGLReadBufferUtil readBufferUtil =
                    new AWTGLReadBufferUtil(drawable.getGLProfile(), false);
            this.chart.screenImage = readBufferUtil.readPixelsToBufferedImage(
                    drawable.getGL(), true);
            this.chart.doScreenShot = false;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;

        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(x, y, width, height);
    }

    /**
     * Get the current viewport width.
     *
     * @return Viewport width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get the current viewport height.
     *
     * @return Viewport height
     */
    public int getHeight() {
        return this.height;
    }
}
