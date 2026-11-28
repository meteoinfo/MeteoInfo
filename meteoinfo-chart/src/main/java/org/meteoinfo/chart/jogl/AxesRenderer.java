package org.meteoinfo.chart.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.meteoinfo.chart.ChartColorBar;
import org.meteoinfo.chart.graphic.Graphic;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.nio.FloatBuffer;

/**
 * OpenGL renderer for a {@link Axes}.
 * <p>
 * The plot owns its data, axes, transforms and styling.  This renderer owns
 * the JOGL lifecycle, viewport/projection setup and the drawing pass order.
 * Keeping the two apart allows the same plot model to be rendered by another
 * backend without changing the plot class.
 */
public class AxesRenderer implements GLEventListener {

    private static final float MIN_2D_AXIS_MARGIN = 50.0f;

    private final Axes plot;

    /**
     * Constructor.
     *
     * @param plot Plot model to render
     */
    public AxesRenderer(Axes plot) {
        if (plot == null) {
            throw new IllegalArgumentException("plot must not be null");
        }
        this.plot = plot;
    }

    /**
     * Get the plot model.
     *
     * @return Plot model
     */
    public Axes getPlot() {
        return this.plot;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        this.plot.drawable = drawable;
        drawable.getGL().setSwapInterval(1);
        this.plot.updateSurfaceScale(drawable);

        GL2 gl = drawable.getGL().getGL2();
        this.plot.gl = gl;
        this.plot.glu = GLU.createGLU(gl);
        Font font = this.plot.xAxis.getTickLabelFont();
        this.plot.updateTextRender(font);

        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

        this.plot.tessCallback = new Axes.TessCallback(gl, this.plot.glu);
        this.plot.setPositionArea(new Rectangle2D.Double(0, 0, 1, 1));
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        Program.destroyAllPrograms(gl);
        this.plot.alwaysUpdateBuffers = true;
    }

    /**
     * Draw the plot.
     *
     * @param drawable GL drawable
     */
    public void display(GLAutoDrawable drawable) {
        this.plot.updateSurfaceScale(drawable);
        if (this.plot.is2D()) {
            boolean oldDisplayZ = this.plot.displayZ;
            this.plot.displayZ = false;
            this.plot.angleX = 0.f;
            this.plot.angleY = 0.f;
            this.display3D(drawable);
            this.plot.displayZ = oldDisplayZ;
        } else {
            this.display3D(drawable);
        }
    }

    /**
     * Draw the plot with the 3-D camera.
     *
     * @param drawable GL drawable
     */
    public void display3D(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glLoadIdentity();

        this.plot.updateTextRender(this.plot.xAxis.getTickLabelFont());

        this.plot.lighting.setPosition(gl);

        gl.glPushMatrix();

        this.plot.modelViewMatrix = new Matrix4f();
        this.plot.modelViewMatrix.rotate((float) Math.toRadians(this.plot.angleX), 1.0f, 0.0f, 0.0f);
        this.plot.modelViewMatrix.rotate((float) Math.toRadians(this.plot.angleY), 0.0f, 0.0f, 1.0f);
        if (this.plot.headAngle != 0) {
            this.plot.modelViewMatrix.rotate((float) Math.toRadians(this.plot.headAngle), 0.0f, 1.0f, 0.0f);
        }
        this.plot.modelViewMatrixR = new Matrix4f(this.plot.modelViewMatrix);

        Vector3f center = this.plot.transform.getCenter();
        Vector3f scale = this.plot.transform.getScale();
        if (this.plot.isXInverted()) {
            scale.x = -scale.x;
        }
        if (this.plot.isYInverted()) {
            scale.y = -scale.y;
        }
        if (this.plot.isZInverted()) {
            scale.z = -scale.z;
        }
        this.plot.modelViewMatrix.scale(scale);
        this.plot.modelViewMatrix.translate(center.negate());

        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glLoadMatrixf(this.plot.modelViewMatrix.get(fb));

        this.plot.updateMatrix(gl);

        if (this.plot.background != null) {
            float[] rgba = this.plot.background.getRGBComponents(null);
            gl.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        if (this.plot.isClearBuffer()) {
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT
                    | GL2.GL_STENCIL_BUFFER_BIT);
        } else {
            gl.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
        }

        if (this.plot.drawBase) {
            this.plot.drawBase(gl);
        }
        if (this.plot.boxed) {
            this.plot.drawBox(gl);
        }

        if (this.plot.clipPlane) {
            this.plot.enableClipPlane(gl);
        }

        this.plot.drawGridLine(gl);
        this.plot.setLight(gl);

        for (int m = 0; m < this.plot.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.plot.graphics.get(m);
            this.plot.drawGraphics(gl, graphic);
        }

        if (this.plot.clipPlane) {
            this.plot.disableClipPlane(gl);
        }
        if (this.plot.lighting.isEnable()) {
            this.plot.lighting.stop(gl);
        }
        if (this.plot.drawBoundingBox) {
            this.plot.drawBoundingBox(gl);
        }

        this.plot.drawAxis(gl);
        this.plot.drawAllZAxis(gl);

        gl.glPopMatrix();
        this.plot.updateMatrix(gl);
        if (!this.plot.legends.isEmpty()) {
            ChartColorBar legend = (ChartColorBar) this.plot.legends.get(0);
            this.plot.updateTextRender(legend.getTickLabelFont());
            if (legend.getLegendScheme().getColorMap() == null) {
                this.plot.drawLegend(gl, legend);
            } else {
                this.plot.drawColorbar(gl, legend);
            }
        }

        this.plot.drawTitle();
        this.plot.textRenderer.dispose();
        this.plot.textRenderer = null;

        gl.glFlush();

        if (this.plot.alwaysUpdateBuffers) {
            this.plot.alwaysUpdateBuffers = false;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.plot.updateSurfaceScale(drawable);
        Rectangle2D dataArea = this.plot.getPositionArea(
                new Rectangle2D.Double(0, 0, width, height));
        this.plot.setPositionArea(dataArea);
        this.plot.setGraphArea(dataArea);

        Rectangle2D renderArea = dataArea;
        float margin = MIN_2D_AXIS_MARGIN * this.plot.dpiScale;
        double left = Math.min(margin, dataArea.getX());
        double top = Math.min(margin, dataArea.getY());
        double right = Math.min(margin,
                width - dataArea.getX() - dataArea.getWidth());
        if (!this.plot.legends.isEmpty()) {
            right = Math.min(Math.max(right, 90.0 * this.plot.dpiScale),
                    width - dataArea.getX() - dataArea.getWidth());
        }
        double bottom = Math.min(margin,
                height - dataArea.getY() - dataArea.getHeight());
        renderArea = new Rectangle2D.Double(dataArea.getX() - left,
                dataArea.getY() - top,
                dataArea.getWidth() + left + right,
                dataArea.getHeight() + top + bottom);
        this.plot.width = (int) renderArea.getWidth();
        this.plot.height = (int) renderArea.getHeight();

        final GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) {
            height = 1;
        }

        gl.glViewport((int) renderArea.getX(),
                (int) (height - renderArea.getHeight() - renderArea.getY()),
                this.plot.width, this.plot.height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        if (this.plot.is2D()) {
            float dataWidth = (float) dataArea.getWidth();
            float dataHeight = (float) dataArea.getHeight();
            float xHalf = dataWidth > 0
                    ? (float) (renderArea.getWidth() / dataWidth) : 1.5f;
            float yHalf = dataHeight > 0
                    ? (float) (renderArea.getHeight() / dataHeight) : 1.5f;
            gl.glOrthof(-xHalf, xHalf, -yHalf, yHalf,
                    -this.plot.distance, this.plot.distance);
        } else if (this.plot.orthographic) {
            float aspect3D = this.plot.height == 0
                    ? 1.0f : (float) this.plot.width / (float) this.plot.height;
            float v = 1.9f;
            float xv = v * Math.max(1.0f, aspect3D);
            float yv = v * Math.max(1.0f, 1.0f / aspect3D);
            gl.glOrthof(-xv, xv, -yv, yv,
                    -this.plot.distance, this.plot.distance);
        } else {
            float near = 0.1f;
            float far = 1000.0f;
            float aspect3D = this.plot.height == 0
                    ? 1.0f : (float) this.plot.width / (float) this.plot.height;
            this.plot.glu.gluPerspective(this.plot.fieldOfView, aspect3D, near, far);
            float z = this.plot.distance * 45.f / this.plot.fieldOfView;
            this.plot.glu.gluLookAt(0.0f, 0.0f, z,
                    0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        }
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
}
