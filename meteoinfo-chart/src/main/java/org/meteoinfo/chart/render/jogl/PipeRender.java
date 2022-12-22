package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;
import org.meteoinfo.chart.graphic.GraphicCollection3D;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.chart.graphic.pipe.Pipe;
import org.meteoinfo.chart.graphic.pipe.PipeShape;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.BreakTypes;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.ColorBreakCollection;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PipeRender extends JOGLGraphicRender{

    private GraphicCollection3D graphics;
    private IntBuffer vbo;
    private Program program;
    private int vertexNum;
    private int sizePosition;
    private int sizeColor;
    private int sizeNormal;
    private float[] vertexColor;
    private int[] vertexIndices;
    private List<Integer> linePointNumbers;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public PipeRender(GL2 gl) {
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
     * @param gl The JOGL GL2 object
     * @param graphics Linestring 3D graphics
     */
    public PipeRender(GL2 gl, GraphicCollection3D graphics) {
        this(gl);

        this.graphics = graphics;
        this.vertexNum = 0;
        for (Graphic graphic : this.graphics.getGraphics()) {
            this.vertexNum += ((PipeShape) graphic.getShape()).getVertexCount();
        }
    }

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/surface/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/surface/surface.frag");
        program = new Program("surface", vertexShaderCode, fragmentShaderCode);
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(2);
    }

    private void updateVertexColor() {
        this.vertexColor = new float[this.vertexNum * 4];
        int i = 0;
        float[] color;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PipeShape shape = (PipeShape) graphic.getShape();
            Pipe pipe = shape.getPipe();
            int n = pipe.getContourCount();
            int m = pipe.getContour().size();
            ColorBreak cb = graphic.getLegend();
            if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
                ColorBreak lineBreak;
                for (int j = 0; j < n; j++) {
                    lineBreak = ((ColorBreakCollection) cb).get(j);
                    color = lineBreak.getColor().getRGBComponents(null);
                    for (int k = 0; k < m; k++) {
                        System.arraycopy(color, 0, vertexColor, i * 4, 4);
                        i++;
                    }
                }
            } else {
                color = cb.getColor().getRGBComponents(null);
                for (int j = 0; j < n * m; j++) {
                    System.arraycopy(color, 0, vertexColor, i * 4, 4);
                    i++;
                }
            }
        }
    }

    private float[] getVertexPosition() {
        float[] vertexData = new float[this.vertexNum * 3];
        int i = 0;
        linePointNumbers = new ArrayList<>();
        for (Graphic graphic : this.graphics.getGraphics()) {
            PipeShape shape = (PipeShape) graphic.getShape();
            shape.transform(transform);
            Pipe pipe = shape.getPipe();
            linePointNumbers.add(pipe.getVertexCount());
            for (int j = 0; j < pipe.getContourCount(); j++) {
                for (Vector3f vector3f : shape.getPipe().getContour(j)) {
                    vertexData[i] = vector3f.x;
                    vertexData[i + 1] = vector3f.y;
                    vertexData[i + 2] = vector3f.z;
                    i += 3;
                }
            }
        }

        return vertexData;
    }

    private void updateVertexIndices() {
        int idxNum = 0;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PipeShape shape = (PipeShape) graphic.getShape();
            Pipe pipe = shape.getPipe();
            idxNum += (pipe.getContourCount() - 1) * (pipe.getContour().size() - 1) * 6;
        }

        vertexIndices = new int[idxNum];
        int i = 0;
        int b = 0;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PipeShape shape = (PipeShape) graphic.getShape();
            Pipe pipe = shape.getPipe();
            int n = pipe.getContour().size();
            for (int j = 0; j < pipe.getContourCount() - 1; j++) {
                Vector<Vector3f> c1 = pipe.getContour(j);
                Vector<Vector3f> c2 = pipe.getContour(j + 1);
                for (int k = 0; k < n - 1; k++) {
                    vertexIndices[i++] = b + j * n + k;
                    vertexIndices[i++] = b + j * n + k + 1;
                    vertexIndices[i++] = b + (j + 1) * n + k;

                    vertexIndices[i++] = b + j * n + k + 1;
                    vertexIndices[i++] = b + (j + 1) * n + k + 1;
                    vertexIndices[i++] = b + (j + 1) * n + k;
                }
            }
            b += pipe.getVertexCount();
        }
    }

    private float[] getVertexNormal() {
        float[] vertexNormal = new float[this.vertexNum * 3];
        int i = 0;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PipeShape shape = (PipeShape) graphic.getShape();
            Pipe pipe = shape.getPipe();
            for (int j = 0; j < pipe.getContourCount(); j++) {
                for (Vector3f vector3f : shape.getPipe().getNormal(j)) {
                    vertexNormal[i] = vector3f.x;
                    vertexNormal[i + 1] = vector3f.y;
                    vertexNormal[i + 2] = vector3f.z;
                    i += 3;
                }
            }
        }

        return vertexNormal;
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

            float[] vertexNormal = this.getVertexNormal();
            FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(vertexNormal);
            sizeNormal = normalBuffer.capacity() * Float.BYTES;

            if (vertexColor == null) {
                this.updateVertexColor();
            }
            FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(vertexColor);
            sizeColor = colorBuffer.capacity() * Float.BYTES;
            int totalSize = sizePosition + sizeNormal + sizeColor;

            gl.glGenBuffers(2, vbo);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
            gl.glBufferData(GL.GL_ARRAY_BUFFER, totalSize, null, GL.GL_STATIC_DRAW);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeNormal, normalBuffer);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition + sizeNormal, sizeColor, colorBuffer);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

            if (vertexIndices == null) {
                this.updateVertexIndices();
            }
            IntBuffer indexBuffer = GLBuffers.newDirectIntBuffer(vertexIndices);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));
            gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * Integer.BYTES, indexBuffer, GL.GL_STATIC_DRAW);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
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
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));

            // enable vertex arrays
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glNormalPointer(GL.GL_FLOAT, 0, sizePosition);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_FLOAT, 0, sizePosition + sizeNormal);

            gl.glDrawElements(GL2.GL_TRIANGLES, vertexIndices.length, GL.GL_UNSIGNED_INT, 0);

            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
}
