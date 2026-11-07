package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.chart.graphic.ParticleGraphics;
import org.meteoinfo.chart.graphic.sphere.Sphere;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.math.Matrix4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParticleRender extends JOGLGraphicRender {

    private ParticleGraphics particles;
    private IntBuffer vbo;
    private Program program;
    private int pointNum;
    private int sizePosition;
    private int sizeColor;
    private int sizeNormal;
    private float pointSize;
    private float[] vertexPosition;
    private float[] vertexNormal;
    private float[] vertexColor;
    private int[] vertexIndices;
    private List<Vector3f> objNormals;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public ParticleRender(GL2 gl) {
        super(gl);

        if (useShader) {
            try {
                this.compileShaders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initVertexBuffer();
    }

    /**
     * Constructor
     *
     * @param gl The opengl pipeline
     * @param pointGraphics 3D point graphics
     */
    public ParticleRender(GL2 gl, ParticleGraphics particleGraphics) {
        this(gl);

        this.particles = particleGraphics;
        this.pointNum = this.particles.getPointNumber();
        this.pointSize = this.particles.getPointSize();
    }

    void updateVertex() {
        this.updateVertexPosition();
        this.updateVertexColor();
    }

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/surface/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/surface/surface.frag");
        program = new Program("surface", vertexShaderCode, fragmentShaderCode);
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(1);
    }

    private void updateVertexColor() {
        this.vertexColor = new float[this.pointNum * 4];
        int i = 0;
        for (Map.Entry<Integer, List> map : particles.getParticleList()) {
            for (ParticleGraphics.Particle p : (List<ParticleGraphics.Particle>)map.getValue()) {
                System.arraycopy(p.rgba, 0, vertexColor, i * 4, 4);
                i++;
            }
        }
    }

    private void updateVertexPosition() {
        this.vertexPosition = new float[this.pointNum * 3];
        int i = 0;
        for (Map.Entry<Integer, List> map : particles.getParticleList()) {
            for (ParticleGraphics.Particle p : (List<ParticleGraphics.Particle>)map.getValue()) {
                vertexPosition[i] = (float) p.x;
                vertexPosition[i + 1] = (float) p.y;
                vertexPosition[i + 2] = (float) p.z;
                i += 3;
            }
        }
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && this.transform != null && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (updateBuffer) {
            if (this.vertexPosition == null) {
                this.updateVertex();
            }

            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(this.vertexPosition);
            sizePosition = vertexBuffer.capacity() * Float.BYTES;

            FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(vertexColor);
            sizeColor = colorBuffer.capacity() * Float.BYTES;

            int totalSize = sizePosition + sizeColor;

            gl.glGenBuffers(1, vbo);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
            gl.glBufferData(GL.GL_ARRAY_BUFFER, totalSize, null, GL.GL_STATIC_DRAW);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeColor, colorBuffer);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }

    void setUniforms() {

    }

    @Override
    public void draw() {
        if (useShader) {    //  not working now
            program.use(gl);
            setUniforms();

            gl.glUseProgram(0);
        } else {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));

            // enable vertex arrays
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_FLOAT, 0, sizePosition);
            boolean lightEnabled = this.lighting.isEnable();
            if (lightEnabled) {
                this.lighting.stop(gl);
            }
            gl.glPointSize(this.pointSize * this.dpiScale);
            gl.glDrawArrays(GL.GL_POINTS, 0, this.pointNum);

            if (lightEnabled) {
                this.lighting.start(gl);
            }

            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }
}
