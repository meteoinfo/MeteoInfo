package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.meteoinfo.chart.graphic.PolygonGraphic;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Utils;

import java.nio.IntBuffer;

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

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/mesh/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/mesh/mesh.frag");
        program = new Program("mesh", vertexShaderCode, fragmentShaderCode);
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(2);
    }

    @Override
    public void draw() {

    }
}
