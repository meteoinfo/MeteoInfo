package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.MeshGraphic;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MeshRender extends JOGLGraphicRender {

    private MeshGraphic meshGraphic;
    private IntBuffer vbo;
    //private IntBuffer vboNormal;
    private Program program;
    private float[] vertexPosition;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public MeshRender(GL2 gl) {
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
     * @param meshGraphic The MeshGraphic
     */
    public MeshRender(GL2 gl, MeshGraphic meshGraphic) {
        this(gl);

        this.meshGraphic = meshGraphic;
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(1);
        //vboNormal = GLBuffers.newDirectIntBuffer(1);
    }

    @Override
    public void setTransform(Transform transform) {
        super.setTransform(transform);

        vertexPosition = meshGraphic.getVertexData(this.transform);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPosition);
        meshGraphic.calculateNormalVectors(vertexPosition);
        FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(meshGraphic.getVertexNormal());
        int sizePosition = vertexBuffer.capacity() * Float.BYTES;
        int sizeNormal = normalBuffer.capacity() * Float.BYTES;

        gl.glGenBuffers(1, vbo);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, sizePosition + sizeNormal,
                ByteBuffer.allocateDirect(sizePosition + sizeNormal).asFloatBuffer(), GL.GL_STATIC_DRAW);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, vertexBuffer.capacity() * Float.BYTES, vertexBuffer);
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES,
                normalBuffer.capacity() * Float.BYTES, normalBuffer);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
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
        float[] rgba = meshGraphic.getColor().getRGBComponents(null);
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

            //float[] rgba = meshGraphic.getColor().getRGBComponents(null);
            //gl.glColor4fv(rgba, 0);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));

            gl.glEnableVertexAttribArray(attribVertexPosition);
            gl.glEnableVertexAttribArray(attribVertexNormal);

            gl.glVertexAttribPointer(attribVertexPosition, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glVertexAttribPointer(attribVertexNormal, 3, GL.GL_FLOAT, false, 0, vertexPosition.length * Float.BYTES);

            gl.glDrawArrays(GL.GL_TRIANGLES, 0, meshGraphic.getVertexNumber());

            gl.glDisableVertexAttribArray(attribVertexPosition);
            gl.glDisableVertexAttribArray(attribVertexNormal);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

            gl.glUseProgram(0);
        } else {
            float[] rgba = meshGraphic.getColor().getRGBComponents(null);
            gl.glColor4fv(rgba, 0);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));

            // enable vertex arrays
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

            // before draw, specify vertex and index arrays with their offsets
            gl.glNormalPointer(GL.GL_FLOAT, 0, vertexPosition.length * Float.BYTES);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);

            gl.glDrawArrays(GL.GL_TRIANGLES, 0, meshGraphic.getVertexNumber());

            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }
}
