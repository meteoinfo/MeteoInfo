package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL2;
import org.joml.Matrix4f;
import org.meteoinfo.chart.jogl.Lighting;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.render.GraphicRender;
import org.meteoinfo.geometry.graphic.Graphic;

import java.nio.IntBuffer;

public abstract class JOGLGraphicRender implements GraphicRender {

    protected GL2 gl;
    protected boolean orthographic = true;
    protected Transform transform;
    protected int viewport[] = new int[4];
    protected float mvmatrix[] = new float[16];
    protected float projmatrix[] = new float[16];
    protected Matrix4f viewProjMatrix = new Matrix4f();
    protected Matrix4f modelViewMatrix = new Matrix4f();
    protected Matrix4f projectionMatrix = new Matrix4f();
    protected Matrix4f modelViewMatrixR = new Matrix4f();
    protected boolean useShader = false;
    protected Lighting lighting = new Lighting();
    protected float dpiScale = 1.0f;

    /**
     * Constructor
     * @param graphic Graphic
     * @param gl The JOGL GL2 object
     */
    public JOGLGraphicRender(GL2 gl) {
        this.gl = gl;
        this.updateMatrix();
    }

    /**
     * Get whether orthographic projection
     * @return Whether orthographic projection
     */
    public boolean isOrthographic() {
        return this.orthographic;
    }

    /**
     * Set whether orthographic projection
     * @param value Whether orthographic projection
     */
    public void setOrthographic(boolean value) {
        this.orthographic = value;
    }

    /**
     * Get transform
     * @return Transform
     */
    public Transform getTransform() {
        return this.transform;
    }

    /**
     * Set transform
     * @param value Transform
     */
    public void setTransform(Transform value) {
        this.transform = value;
    }

    /**
     * Set transform
     * @param value Transform
     * @param alwaysUpdateBuffers Always update buffers or not
     */
    public void setTransform(Transform value, boolean alwaysUpdateBuffers) {
        this.transform = value;
    }

    /**
     * Get view port width
     * @return View port width
     */
    public int getWidth() {
        return this.viewport[2];
    }

    /**
     * Get view port height
     * @return View port height
     */
    public int getHeight() {
        return this.viewport[3];
    }

    /**
     * Set lighting
     * @param lighting Lighting
     */
    public void setLighting(Lighting lighting) {
        this.lighting = lighting;
    }

    /**
     * Get DPI scale
     * @return DPI scale
     */
    public float getDpiScale() {
        return this.dpiScale;
    }

    /**
     * Set DPI scale
     * @param value DPI scale
     */
    public void setDpiScale(float value) {
        this.dpiScale = value;
    }

    protected int getTextureID() {
        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGenTextures(1, intBuffer);
        return intBuffer.get(0);
    }

    protected Matrix4f toMatrix(float[] data) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.set(data);
        return matrix4f;
    }

    /**
     * Update matrix
     */
    public void updateMatrix() {
        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
        gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
        gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
        modelViewMatrix = toMatrix(mvmatrix);
        projectionMatrix = toMatrix(projmatrix);
        viewProjMatrix = projectionMatrix.
                mul(modelViewMatrix);
    }

    /**
     * Set rotate model view matrix
     * @param value Rotate model view matrix
     */
    public void setRotateModelView(Matrix4f value) {
        this.modelViewMatrixR = value;
    }
}
