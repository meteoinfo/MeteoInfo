package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import org.meteoinfo.chart.graphic.VolumeGraphics;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.graphic.Graphic;

import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_3D;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_WRAP_R;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;

/**
 * Render for volume plot
 */
public class VolumeRender extends JOGLGraphicRender {

    private VolumeGraphics volume;
    private int colorTexture;
    private int volumeTexture;
    private int normalsTexture;
    private Program program;

    /**
     * Constructor
     * @param gl JOGL GL2 object
     */
    public VolumeRender(GL2 gl) {
        super(gl);
    }

    /**
     * Constructor
     * @param gl JOGL GL2 object
     * @param rayCastingType Ray casting type
     */
    public VolumeRender(GL2 gl, RayCastingType rayCastingType) {
        super(gl);
    }

    /**
     * Constructor
     * @param gl JOGL GL2 object
     * @param graphic Volume graphic
     */
    public VolumeRender(GL2 gl, VolumeGraphics graphic) {
        this(gl);
        setVolume(graphic);
    }

    /**
     * Constructor
     * @param gl JOGL GL2 object
     * @param graphic Volume graphic
     * @param transform Transform
     */
    public VolumeRender(GL2 gl, VolumeGraphics graphic, Transform transform) {
        this(gl, graphic);
        this.transform = transform;
    }

    /**
     * Get volume graphic
     * @return Volume graphic
     */
    public VolumeGraphics getVolume() {
        return volume;
    }

    /**
     * Set volume graphic
     * @param value Volume graphic
     */
    public void setVolume(VolumeGraphics value) {
        this.volume = value;
        this.bindingTextures();
        try {
            this.compileShaders();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get ray casting type
     * @return
     */
    public RayCastingType getRayCastingType() {
        return this.volume == null ? RayCastingType.MAX_VALUE : this.volume.getRayCastingType();
    }

    /**
     * Get brightness
     * @return Brightness
     */
    public float getBrightness() {
        return this.volume == null ? 1.0f : this.volume.getBrightness();
    }

    void bindingTextures() {
        //ColorMap 2D texture
        this.colorTexture = getTextureID();
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, this.colorTexture);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        //gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, Buffers.newDirectByteBuffer(volume.getColors()));
        //gl.glActiveTexture(GL_TEXTURE1);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, volume.getColorNum(), 1, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                Buffers.newDirectByteBuffer(volume.getColors()).rewind());

        //Volume data 3D texture
        this.volumeTexture = getTextureID();
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_3D, this.volumeTexture);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        //gl.glActiveTexture(GL_TEXTURE0);
        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        gl.glTexImage3D(
                GL_TEXTURE_3D,  // target
                0,              // level
                GL_LUMINANCE,        // internal format
                volume.getWidth(),           // width
                volume.getHeight(),           // height
                volume.getDepth(),           // depth
                0,              // border
                GL_LUMINANCE,         // format
                GL_UNSIGNED_BYTE,       // type
                Buffers.newDirectByteBuffer(volume.getData()).rewind()           // pixel
        );

        //Normals 3D texture
        switch (this.getRayCastingType()) {
            case SPECULAR:
                this.normalsTexture = getTextureID();
                gl.glActiveTexture(GL_TEXTURE2);
                gl.glBindTexture(GL_TEXTURE_3D, this.normalsTexture);
                gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
                gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
                //gl.glActiveTexture(GL_TEXTURE2);
                gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
                gl.glTexImage3D(
                        GL_TEXTURE_3D,  // target
                        0,              // level
                        GL_RGB,        // internal format
                        volume.getWidth(),           // width
                        volume.getHeight(),           // height
                        volume.getDepth(),           // depth
                        0,              // border
                        GL_RGB,         // format
                        GL_UNSIGNED_BYTE,       // type
                        Buffers.newDirectByteBuffer(volume.getNormals()).rewind()           // pixel
                );
                break;
        }
    }

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/volume/vertex.vert");
        String fragmentShaderCode;
        switch (this.getRayCastingType()) {
            case SPECULAR:
                fragmentShaderCode = Utils.loadResource("/shaders/volume/specular.frag");
                break;
            default:
                fragmentShaderCode = Utils.loadResource("/shaders/volume/maxValue.frag");
                break;
        }
        program = new Program("volume", vertexShaderCode, fragmentShaderCode);
    }

    /**
     * Update shaders
     */
    public void updateShaders() {
        if (program == null) {
            this.bindingTextures();
            try {
                compileShaders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (program.getProgramId() == null) {
            this.bindingTextures();
            program.init(gl);
        }
    }

    void setUniforms() {
        program.allocateUniform(gl, "orthographic", (gl2, loc) -> {
            gl2.glUniform1i(loc, this.orthographic ? 1 : 0);
        });
        program.allocateUniform(gl, "MVP", (gl2, loc) -> {
            //gl2.glUniformMatrix4fv(loc, 1, false, this.camera.getViewProjectionMatrix().get(Buffers.newDirectFloatBuffer(16)));
            gl2.glUniformMatrix4fv(loc, 1, false, this.viewProjMatrix.get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "iV", (gl2, loc) -> {
            //gl2.glUniformMatrix4fv(loc, 1, false, this.camera.getViewMatrix().invert().get(Buffers.newDirectFloatBuffer(16)));
            gl2.glUniformMatrix4fv(loc, 1, false, toMatrix(this.mvmatrix).invert().get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "iP", (gl2, loc) -> {
            //gl2.glUniformMatrix4fv(loc, 1, false, this.camera.getProjectionMatrix().invert().get(Buffers.newDirectFloatBuffer(16)));
            gl2.glUniformMatrix4fv(loc, 1, false, toMatrix(this.projmatrix).invert().get(Buffers.newDirectFloatBuffer(16)));
        });
        program.allocateUniform(gl, "viewSize", (gl2, loc) -> {
            gl2.glUniform2f(loc, this.getWidth(), this.getHeight());
        });
        int sampleCount = 512;
        program.allocateUniform(gl, "depthSampleCount", (gl2, loc) -> {
            gl2.glUniform1i(loc, sampleCount);
        });
        program.allocateUniform(gl, "tex", (gl2, loc) -> {
            gl2.glUniform1i(loc, 0);
        });
        program.allocateUniform(gl, "colorMap", (gl2, loc) -> {
            gl2.glUniform1i(loc, 1);
        });
        if (this.getRayCastingType() == RayCastingType.SPECULAR) {
            program.allocateUniform(gl, "normals", (gl2, loc) -> {
                gl2.glUniform1i(loc, 2);
            });
            program.allocateUniform(gl, "brightness", (gl2, loc) -> {
                gl2.glUniform1f(loc, this.getBrightness());
            });
        }
        float[] aabbMin = volume.getAabbMin();
        float[] aabbMax = volume.getAabbMax();
        program.allocateUniform(gl, "aabbMin", (gl2, loc) -> {
            gl2.glUniform3f(loc, aabbMin[0], aabbMin[1], aabbMin[2]);
        });
        program.allocateUniform(gl, "aabbMax", (gl2, loc) -> {
            gl2.glUniform3f(loc, aabbMax[0], aabbMax[1], aabbMax[2]);
        });

        program.setUniforms(gl);
    }

    @Override
    public void draw() {
        try {
            //this.bindingTextures();
            //this.compileShaders();
            program.use(gl);
            setUniforms();
            //program.use(gl);
            //program.setUniforms(gl);

            IntBuffer intBuffer = IntBuffer.allocate(1);
            gl.glGenBuffers(1, intBuffer);
            int vertexBuffer = intBuffer.get(0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
            float[] vertexBufferData = volume.getVertexBufferData(this.transform);
            gl.glBufferData(GL_ARRAY_BUFFER, vertexBufferData.length * Float.BYTES, Buffers.newDirectFloatBuffer(vertexBufferData), GL_STATIC_DRAW);
            // 1st attribute buffer : vertices
            gl.glEnableVertexAttribArray(0);
            //gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
            gl.glVertexAttribPointer(
                    0,                  // attribute 0. No particular reason for 0, but must match the layout in the shader.
                    3,                  // size
                    GL_FLOAT,           // type
                    false,           // normalized?
                    3 * 4,                  // stride
                    0            // array buffer offset
            );

            // Draw the triangle !
            gl.glDisable(GL_DEPTH_TEST);
            gl.glDrawArrays(GL_TRIANGLES, 0, vertexBufferData.length / 3); // Starting from vertex 0; 3 vertices total -> 1 triangle
            gl.glDisableVertexAttribArray(0);

            //Program.destroyAllPrograms(gl);
            gl.glUseProgram(0);
            gl.glEnable(GL_DEPTH_TEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
