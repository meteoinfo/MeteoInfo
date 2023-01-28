package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.meteoinfo.chart.graphic.GraphicCollection3D;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.PointZShape;
import org.meteoinfo.geometry.shape.Polyline;
import org.meteoinfo.geometry.shape.PolylineZShape;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class PointRender extends JOGLGraphicRender {

    private GraphicCollection3D graphics;
    private IntBuffer vbo;
    private Program program;
    private int vertexNum;
    private int sizePosition;
    private int sizeColor;
    private int sizeNormal;
    private float[] vertexColor;
    private float pointSize;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public PointRender(GL2 gl) {
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
    public PointRender(GL2 gl, GraphicCollection3D pointGraphics) {
        this(gl);

        this.graphics = pointGraphics;
        this.vertexNum = pointGraphics.getNumGraphics();
        PointBreak pb = (PointBreak) this.graphics.getGraphicN(0).getLegend();
        this.pointSize = pb.getSize();
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
        this.vertexColor = new float[this.vertexNum * 4];
        int i = 0;
        float[] color;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PointBreak pb = (PointBreak) graphic.getLegend();
            color = pb.getColor().getRGBComponents(null);
            System.arraycopy(color, 0, vertexColor, i * 4, 4);
            i++;
        }
    }

    private float[] getVertexPosition() {
        float[] vertexData = new float[this.vertexNum * 3];
        int i = 0;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PointZShape shape = (PointZShape) graphic.getShape();
            PointZ p = (PointZ) shape.getPoint();
            vertexData[i] = transform.transform_x((float) p.X);
            vertexData[i + 1] = transform.transform_y((float) p.Y);
            vertexData[i + 2] = transform.transform_z((float) p.Z);
            i += 3;
        }

        return vertexData;
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && this.transform != null && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (updateBuffer) {
            float[] vertexData = this.getVertexPosition();
            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
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
            gl.glDrawArrays(GL.GL_POINTS, 0, this.vertexNum);

            if (lightEnabled) {
                this.lighting.start(gl);
            }

            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }
}
