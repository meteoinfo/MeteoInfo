package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.meteoinfo.chart.graphic.MeshGraphic;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.legend.PolygonBreak;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;

public class MeshRender extends JOGLGraphicRender {

    private MeshGraphic meshGraphic;
    private IntBuffer vbo;
    private IntBuffer vao;
    private Program program;
    private int sizePosition;
    private int sizeNormal;
    private int sizeColorTexture;
    private int sizeIndices;
    private Texture texture;
    private int textureID;

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
     * @param meshGraphic The meshGraphic
     */
    public MeshRender(GL2 gl, MeshGraphic meshGraphic) {
        this(gl);

        this.meshGraphic = meshGraphic;
        if (meshGraphic.isUsingTexture()) {
            texture = AWTTextureIO.newTexture(gl.getGLProfile(), meshGraphic.getImage(), true);
            this.textureID = texture.getTextureObject(gl);
            bindingTextures();
        }
    }

    private void initVertexBuffer() {
        vao = GLBuffers.newDirectIntBuffer(1);
        vbo = GLBuffers.newDirectIntBuffer(2);
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && this.transform != null && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (updateBuffer) {
            float[] vertexData = meshGraphic.getVertexPosition(this.transform);
            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
            meshGraphic.calculateNormalVectors(vertexData);
            FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(meshGraphic.getVertexNormal());
            sizePosition = vertexBuffer.capacity() * Float.BYTES;
            sizeNormal = normalBuffer.capacity() * Float.BYTES;
            int totalSize = sizePosition + sizeNormal;

            FloatBuffer ctBuffer;
            if (meshGraphic.isUsingTexture()) {
                ctBuffer = GLBuffers.newDirectFloatBuffer(meshGraphic.getVertexTexture());
            } else {
                ctBuffer = GLBuffers.newDirectFloatBuffer(meshGraphic.getVertexColor());
            }
            sizeColorTexture = ctBuffer.capacity() * Float.BYTES;
            totalSize += sizeColorTexture;

            gl.glGenBuffers(2, vbo);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
            gl.glBufferData(GL.GL_ARRAY_BUFFER, totalSize,null, GL.GL_STATIC_DRAW);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeNormal, normalBuffer);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition + sizeNormal, sizeColorTexture, ctBuffer);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

            IntBuffer indexBuffer = GLBuffers.newDirectIntBuffer(meshGraphic.getVertexIndices());
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));
            gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * Integer.BYTES, indexBuffer, GL.GL_STATIC_DRAW);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        if (alwaysUpdateBuffers) {
            texture = AWTTextureIO.newTexture(gl.getGLProfile(), meshGraphic.getImage(), true);
            this.textureID = texture.getTextureObject(gl);
            this.bindingTextures();
        }
    }

    void bindingTextures() {
        gl.glBindTexture(GL_TEXTURE_2D, this.textureID);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/surface/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/surface/surface.frag");
        program = new Program("surface", vertexShaderCode, fragmentShaderCode);
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
        program.allocateUniform(gl, "MVP", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, this.viewProjMatrix.get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "MV", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, toMatrix(this.mvmatrix).get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "MVI", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, toMatrix(this.mvmatrix).invert().get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "transMatrix", (gl2, loc) -> {
            gl2.glUniformMatrix4fv(loc, 1, false, this.transform.getTransformMatrix().get(Buffers.newDirectFloatBuffer(16)));
        });
        float[] rgba = meshGraphic.getColor().getRGBComponents(null);
        program.allocateUniform(gl, "color", (gl2, loc) -> {
            gl2.glUniform4f(loc, rgba[0], rgba[1], rgba[2], rgba[3]);
        });
        program.allocateUniform(gl, "lightPosition", (gl2, loc) -> {
            gl2.glUniform4fv(loc, 0, lighting.getPosition(), 0);
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
            int attribVertexColor = gl.glGetAttribLocation(program.getProgramId(), "vertexColor");
            int attribVertexIndices = gl.glGetAttribLocation(program.getProgramId(), "vertexIndices");

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));

            gl.glEnableVertexAttribArray(attribVertexPosition);
            gl.glEnableVertexAttribArray(attribVertexNormal);
            gl.glEnableVertexAttribArray(attribVertexColor);

            gl.glVertexAttribPointer(attribVertexPosition, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glVertexAttribPointer(attribVertexNormal, 3, GL.GL_FLOAT, false, 0, sizePosition);
            gl.glVertexAttribPointer(attribVertexColor, 3, GL.GL_FLOAT, false, 0, sizePosition + sizeNormal);

            gl.glDrawArrays(GL.GL_TRIANGLES, 0, meshGraphic.getVertexNumber());

            gl.glDisableVertexAttribArray(0);
            gl.glDisableVertexAttribArray(1);

            gl.glUseProgram(0);
        } else {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));

            // enable vertex arrays
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glNormalPointer(GL.GL_FLOAT, 0, sizePosition);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);

            if (meshGraphic.isUsingTexture()) {
                gl.glEnable(GL2.GL_TEXTURE_2D);
                gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, sizePosition + sizeNormal);
            } else {
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
                gl.glColorPointer(4, GL.GL_FLOAT, 0, sizePosition + sizeNormal);
            }

            PolygonBreak pb = (PolygonBreak) meshGraphic.getLegendScheme().getLegendBreak(0);
            if (pb.isDrawFill()) {
                gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
                gl.glPolygonOffset(1.0f, 1.0f);

                if (meshGraphic.isUsingTexture()) {
                    gl.glColor3f(1.0f, 1.0f, 1.0f);
                    gl.glBindTexture(GL_TEXTURE_2D, this.textureID);
                }

                if (meshGraphic.isFaceInterp())
                    gl.glDrawElements(GL2.GL_QUADS, meshGraphic.getVertexIndices().length, GL.GL_UNSIGNED_INT, 0);
                else {
                    gl.glShadeModel(GL2.GL_FLAT);
                    gl.glDrawElements(GL2.GL_QUADS, meshGraphic.getVertexIndices().length, GL.GL_UNSIGNED_INT, 0);
                    gl.glShadeModel(GL2.GL_SMOOTH);
                }
            }
            if (pb.isDrawOutline()) {
                boolean lightEnabled = this.lighting.isEnable();
                if (lightEnabled) {
                    this.lighting.stop(gl);
                }
                gl.glLineWidth(pb.getOutlineSize() * this.dpiScale);
                if (!meshGraphic.isMesh()) {
                    gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
                    float[] rgba = pb.getOutlineColor().getRGBComponents(null);
                    gl.glColor4fv(rgba, 0);
                }
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
                gl.glDrawElements(GL2.GL_QUADS, meshGraphic.getVertexIndices().length, GL.GL_UNSIGNED_INT, 0);
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
                if (lightEnabled) {
                    this.lighting.start(gl);
                }
            }

            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            if (meshGraphic.isUsingTexture()) {
                gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                gl.glBindTexture(GL_TEXTURE_2D, 0);
            }
            else
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
}
