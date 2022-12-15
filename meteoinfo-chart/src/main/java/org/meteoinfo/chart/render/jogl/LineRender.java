package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.meteoinfo.chart.graphic.GraphicCollection3D;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.Polyline;
import org.meteoinfo.geometry.shape.PolylineZ;
import org.meteoinfo.geometry.shape.PolylineZShape;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.jogamp.opengl.GL.GL_TEXTURE_2D;

public class LineRender extends JOGLGraphicRender {

    private GraphicCollection3D graphics;
    private IntBuffer vbo;
    private Program program;
    private int vertexNum;
    private int sizePosition;
    private int sizeColor;
    private int sizeNormal;
    private float[] vertexColor;
    private float lineWidth = 1.0f;
    private List<Integer> linePointNumbers;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public LineRender(GL2 gl) {
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
    public LineRender(GL2 gl, GraphicCollection3D graphics) {
        this(gl);

        this.graphics = graphics;
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
        this.vertexNum = 0;
        for (Graphic graphic : this.graphics.getGraphics()) {
            this.vertexNum += graphic.getShape().getPointNum();
        }

        this.vertexColor = new float[this.vertexNum * 4];
        int i = 0;
        float[] color;
        for (Graphic graphic : this.graphics.getGraphics()) {
            int n = graphic.getShape().getPointNum();
            ColorBreak cb = graphic.getLegend();
            if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
                PolylineBreak lineBreak;
                for (int j = 0; j < n; j++) {
                    lineBreak = (PolylineBreak) ((ColorBreakCollection) cb).get(j);
                    this.lineWidth = lineBreak.getWidth();
                    color = lineBreak.getColor().getRGBComponents(null);
                    System.arraycopy(color, 0, vertexColor, i * 4, 4);
                    i++;
                }
            } else {
                this.lineWidth = ((PolylineBreak) cb).getWidth();
                color = cb.getColor().getRGBComponents(null);
                for (int j = 0; j < n; j++) {
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
            PolylineZShape shape = (PolylineZShape) graphic.getShape();
            for (Polyline line : shape.getPolylines()) {
                List<PointZ> ps = (List<PointZ>) line.getPointList();
                linePointNumbers.add(ps.size());
                for (PointZ p : ps) {
                    vertexData[i] = transform.transform_x((float) p.X);
                    vertexData[i + 1] = transform.transform_y((float) p.Y);
                    vertexData[i + 2] = transform.transform_z((float) p.Z);
                    i += 3;
                }
            }
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
            gl.glLineWidth(this.lineWidth * this.dpiScale);
            int sIdx = 0;
            for (int np : linePointNumbers) {
                gl.glDrawArrays(GL.GL_LINE_STRIP, sIdx, np);
                sIdx += np;
            }
            if (lightEnabled) {
                this.lighting.start(gl);
            }

            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }
}
