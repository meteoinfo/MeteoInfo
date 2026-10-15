package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.PolygonGraphic;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.legend.PolygonBreak;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Pure Cartesian polygon renderer with VBO caching.
 * Performs EarCut tessellation only (no spherical subdivision).
 * Vertices are used as-is in 3D Euclidean space.
 */
public class PolygonRender extends JOGLGraphicRender {

    private PolygonGraphic polygonGraphic;
    private IntBuffer vbo;
    private Program program;
    private float[] vertexPosition;
    private int sizePosition;
    private int sizeNormal;
    private int sizeColor;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public PolygonRender(GL2 gl) {
        super(gl);

        useShader = false;
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
     * @param polygonGraphic The PolygonGraphic
     */
    public PolygonRender(GL2 gl, PolygonGraphic polygonGraphic) {
        this(gl);

        this.polygonGraphic = polygonGraphic;
        this.setBufferData();
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(1);
    }

    private void setBufferData() {
        if (vertexPosition == null) {
            vertexPosition = polygonGraphic.getVertexPosition();
        }
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPosition);
        if (polygonGraphic.getVertexNormal() == null) {
            polygonGraphic.calculateNormalVectors(vertexPosition);
        }
        FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(polygonGraphic.getVertexNormal());
        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(polygonGraphic.getVertexColor());
        sizePosition = vertexBuffer.capacity() * Float.BYTES;
        sizeNormal = normalBuffer.capacity() * Float.BYTES;
        sizeColor = colorBuffer.capacity() * Float.BYTES;

        gl.glGenBuffers(2, vbo);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, sizePosition + sizeNormal + sizeColor, null, GL.GL_STATIC_DRAW);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeNormal, normalBuffer);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition + sizeNormal, sizeColor, colorBuffer);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && this.transform != null && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (alwaysUpdateBuffers) {
            setBufferData();
        }
    }

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/mesh/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/mesh/mesh.frag");
        program = new Program("mesh", vertexShaderCode, fragmentShaderCode);
    }

    /**
     * Update shaders
     */
    public void updateShaders() {
        if (program == null) {
            try {
                compileShaders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void setUniforms() {
        program.allocateUniform(gl, "matrixModelViewProjection", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, this.viewProjMatrix.get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "matrixModelView", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, toMatrix(this.mvmatrix).get(Buffers.newDirectFloatBuffer(16)));
        });
        Matrix4f matrixNormal = toMatrix(this.mvmatrix);
        matrixNormal.setColumn(3, new Vector4f(0,0,0,1));
        program.allocateUniform(gl, "matrixNormal", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, matrixNormal.get(Buffers.newDirectFloatBuffer(16)));
        });
        float[] rgba = polygonGraphic.getLegendBreak().getColor().getRGBComponents(null);
        program.allocateUniform(gl, "color", (gl2, loc) -> {
            gl2.glUniform4fv(loc, 1, rgba, 0);
        });
        program.allocateUniform(gl, "lightPosition", (gl2, loc) -> {
            gl2.glUniform4fv(loc, 1, lighting.getPosition(), 0);
        });
        program.allocateUniform(gl, "lightAmbient", (gl2, loc) -> {
            gl2.glUniform4fv(loc, 1, lighting.getAmbient(), 0);
        });
        program.allocateUniform(gl, "lightDiffuse", (gl2, loc) -> {
            gl2.glUniform4fv(loc, 1, lighting.getDiffuse(), 0);
        });
        program.allocateUniform(gl, "lightSpecular", (gl2, loc) -> {
            gl2.glUniform4fv(loc, 1, lighting.getSpecular(), 0);
        });

        program.setUniforms(gl);
    }

    @Override
    public void draw() {

        if (useShader) {
            program.use(gl);
            setUniforms();

            int attribVertexPosition = gl.glGetAttribLocation(program.getProgramId(), "vertexPosition");
            int attribVertexNormal = gl.glGetAttribLocation(program.getProgramId(), "vertexNormal");

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));

            gl.glEnableVertexAttribArray(attribVertexPosition);
            gl.glEnableVertexAttribArray(attribVertexNormal);

            gl.glVertexAttribPointer(attribVertexPosition, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glVertexAttribPointer(attribVertexNormal, 3, GL.GL_FLOAT, false, 0, vertexPosition.length * Float.BYTES);

            gl.glDrawArrays(GL.GL_TRIANGLES, 0, polygonGraphic.getVertexNumber());

            gl.glDisableVertexAttribArray(attribVertexPosition);
            gl.glDisableVertexAttribArray(attribVertexNormal);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

            gl.glUseProgram(0);
        } else {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));

            // enable vertex arrays
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glNormalPointer(GL.GL_FLOAT, 0, sizePosition);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_FLOAT, 0, sizePosition + sizeNormal);

            PolygonBreak pb = (PolygonBreak) polygonGraphic.getLegendBreak();
            if (pb.isDrawFill()) {
                gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
                gl.glPolygonOffset(1.0f, 1.0f);
                if (polygonGraphic.isFaceInterp()) {
                    gl.glDrawArrays(GL2.GL_TRIANGLES, 0, polygonGraphic.getVertexNumber());
                } else {
                    gl.glShadeModel(GL2.GL_FLAT);
                    gl.glDrawArrays(GL2.GL_TRIANGLES, 0, polygonGraphic.getVertexNumber());
                    gl.glShadeModel(GL2.GL_SMOOTH);
                }
            }
            if (pb.isDrawOutline()) {
                boolean lightEnabled = this.lighting.isEnable();
                if (lightEnabled) {
                    this.lighting.stop(gl);
                }
                gl.glLineWidth(pb.getOutlineSize() * this.dpiScale);
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
                gl.glDrawArrays(GL.GL_TRIANGLES, 0, polygonGraphic.getVertexNumber());
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
                if (lightEnabled) {
                    this.lighting.start(gl);
                }
            }

            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
}