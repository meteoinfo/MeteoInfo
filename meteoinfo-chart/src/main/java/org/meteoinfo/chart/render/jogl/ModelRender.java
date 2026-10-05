package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.*;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.legend.PolygonBreak;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.GL_TEXTURE_2D;

public class ModelRender extends MeshRender {

    private ModelGraphic modelGraphic;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public ModelRender(GL2 gl) {
        super(gl);
    }

    /**
     * Constructor
     *
     * @param gl The opengl pipeline
     * @param modelGraphic The model graphic
     */
    public ModelRender(GL2 gl, ModelGraphic modelGraphic) {
        this(gl);

        this.modelGraphic = modelGraphic;
        this.meshGraphic = this.modelGraphic.getMeshGraphic();
        this.quad = meshGraphic instanceof QuadMeshGraphic ? true : false;
        this.setBufferData();
        if (meshGraphic.isUsingTexture()) {
            texture = AWTTextureIO.newTexture(gl.getGLProfile(), meshGraphic.getImage(), true);
            this.textureID = texture.getTextureObject(gl);
            bindingTextures();
        }
    }

    @Override
    public void draw() {
        gl.glPushMatrix();
        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        Matrix4f modelView = new Matrix4f(this.modelViewMatrixR);
        if (modelGraphic.getLocation() != null) {
            modelView.translate(this.transform.transform(modelGraphic.getLocation()));
        }
        if (modelGraphic.getScale() != 1) {
            modelView.scale(modelGraphic.getScale());
        }
        if (modelGraphic.getRotation() != null) {
            modelView.rotateXYZ(modelGraphic.getRotationRadians());
        }
        if (modelGraphic.getDirection() != null) {
            modelView.lookAlong(modelGraphic.getDirection(), new Vector3f(modelView.m30(), modelView.m31(),
                    modelView.m32() + 1));
        }
        gl.glLoadMatrixf(modelView.get(fb));

        super.draw();
    }
}
