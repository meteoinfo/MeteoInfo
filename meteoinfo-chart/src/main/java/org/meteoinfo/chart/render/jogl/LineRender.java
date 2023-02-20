package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.GraphicCollection3D;
import org.meteoinfo.chart.graphic.cylinder.Cylinder;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.Polyline;
import org.meteoinfo.geometry.shape.PolylineZ;
import org.meteoinfo.geometry.shape.PolylineZShape;
import org.meteoinfo.math.Matrix4f;

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
    private float[] vertexPosition;
    private float[] vertexColor;
    private float lineWidth = 1.0f;
    private List<Integer> linePointNumbers;
    private boolean streamline = false;
    private IntBuffer vboCone;
    private float[] coneVertexPosition;
    private float[] coneVertexNormal;
    private float[] coneVertexColor;
    private int[] coneVertexIndices;
    private int sizeConePosition;
    private int sizeConeNormal;
    private int sizeConeColor;

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
        ColorBreak cb = graphics.getGraphicN(0).getLegend();
        PolylineBreak lineBreak;
        if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
            lineBreak = (PolylineBreak) ((ColorBreakCollection) cb).get(0);
        } else {
            lineBreak = (PolylineBreak) cb;
        }
        this.streamline = lineBreak instanceof StreamlineBreak;

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
        vertexPosition = new float[this.vertexNum * 3];
        int i = 0;
        linePointNumbers = new ArrayList<>();
        for (Graphic graphic : this.graphics.getGraphics()) {
            PolylineZShape shape = (PolylineZShape) graphic.getShape();
            for (Polyline line : shape.getPolylines()) {
                List<PointZ> ps = (List<PointZ>) line.getPointList();
                linePointNumbers.add(ps.size());
                for (PointZ p : ps) {
                    vertexPosition[i] = (float) p.X;
                    vertexPosition[i + 1] = (float) p.Y;
                    vertexPosition[i + 2] = (float) p.Z;
                    i += 3;
                }
            }
        }

        return vertexPosition;
    }

    private void updateVertexPosition() {
        vertexPosition = new float[this.vertexNum * 3];
        int i = 0;
        linePointNumbers = new ArrayList<>();
        for (Graphic graphic : this.graphics.getGraphics()) {
            PolylineZShape shape = (PolylineZShape) graphic.getShape();
            for (Polyline line : shape.getPolylines()) {
                List<PointZ> ps = (List<PointZ>) line.getPointList();
                linePointNumbers.add(ps.size());
                for (PointZ p : ps) {
                    vertexPosition[i] = transform.transform_x((float) p.X);
                    vertexPosition[i + 1] = transform.transform_y((float) p.Y);
                    vertexPosition[i + 2] = transform.transform_z((float) p.Z);
                    i += 3;
                }
            }
        }
    }

    private void updateConeVertex() {
        List<Vector3f> vertexPositionList = new ArrayList<>();
        List<Vector3f> vertexNormalList = new ArrayList<>();
        List<Vector4f> vertexColorList = new ArrayList<>();
        List<Integer> vertexIndices = new ArrayList<>();
        Cylinder cylinder = null;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PolylineZShape shape = (PolylineZShape) graphic.getShape();
            int pointNum = shape.getPointNum();
            List<PointZ> ps = (List<PointZ>) shape.getPoints();
            ColorBreak cb = graphic.getLegend();
            if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
                ColorBreakCollection cbc = (ColorBreakCollection) cb;
                StreamlineBreak slb = (StreamlineBreak) cbc.get(0);
                int interval = slb.getInterval();
                Vector3f v1, v2;
                for (int i = 1; i < pointNum; i++) {
                    if (i % interval == 0) {
                        PointZ p2 = ps.get(i);
                        PointZ p1 = ps.get(i - 1);
                        v1 = transform.transform((float) p1.X, (float) p1.Y, (float) p1.Z);
                        v2 = transform.transform((float) p2.X, (float) p2.Y, (float) p2.Z);
                        slb = (StreamlineBreak) cbc.get(i);
                        if (cylinder == null) {
                            cylinder = new Cylinder(slb.getArrowHeadWidth() * 0.02f,
                                    0, slb.getArrowHeadLength() * 0.02f, 8, 1, true);
                        }
                        Matrix4f matrix = new Matrix4f();
                        matrix.lookAt(v2.sub(v1, new Vector3f()));
                        matrix.translate(v2);
                        List<Vector3f> vertices = cylinder.getVertices();
                        int n = vertices.size();
                        int nAdded = vertexPositionList.size();
                        for (Vector3f v : vertices) {
                            vertexPositionList.add(matrix.mul(v));
                        }
                        List<Vector3f> normals = cylinder.getNormals();
                        vertexNormalList.addAll(normals);
                        float[] color = slb.getColor().getRGBComponents(null);
                        for (int j = 0; j < n; j++) {
                            vertexColorList.add(new Vector4f(color));
                        }
                        if (nAdded == 0) {
                            vertexIndices.addAll(cylinder.getIndices());
                        } else {
                            for (int idx : cylinder.getIndices()) {
                                vertexIndices.add(idx + nAdded);
                            }
                        }
                    }
                }
            } else {
                StreamlineBreak slb = (StreamlineBreak) cb;
                int interval = slb.getInterval();
                Vector3f v1, v2;
                for (int i = 1; i < pointNum; i++) {
                    if (i % interval == 0) {
                        PointZ p2 = ps.get(i);
                        PointZ p1 = ps.get(i - 1);
                        v1 = transform.transform((float) p1.X, (float) p1.Y, (float) p1.Z);
                        v2 = transform.transform((float) p2.X, (float) p2.Y, (float) p2.Z);
                        if (cylinder == null) {
                            cylinder = new Cylinder(slb.getArrowHeadWidth() * 0.02f,
                                    0, slb.getArrowHeadLength() * 0.02f, 8, 1, true);
                        }
                        Matrix4f matrix = new Matrix4f();
                        matrix.lookAt(v2.sub(v1, new Vector3f()));
                        matrix.translate(v2);
                        List<Vector3f> vertices = cylinder.getVertices();
                        int n = vertices.size();
                        int nAdded = vertexPositionList.size();
                        for (Vector3f v : vertices) {
                            vertexPositionList.add(matrix.mul(v));
                        }
                        List<Vector3f> normals = cylinder.getNormals();
                        vertexNormalList.addAll(normals);
                        /*for (Vector3f v : normals) {
                            vertexNormalList.add(matrix.mul(v));
                        }*/
                        float[] color = slb.getColor().getRGBComponents(null);
                        for (int j = 0; j < n; j++) {
                            vertexColorList.add(new Vector4f(color));
                        }
                        if (nAdded == 0) {
                            vertexIndices.addAll(cylinder.getIndices());
                        } else {
                            for (int idx : cylinder.getIndices()) {
                                vertexIndices.add(idx + nAdded);
                            }
                        }
                    }
                }
            }
        }

        int n = vertexPositionList.size();
        this.coneVertexPosition = new float[n * 3];
        this.coneVertexNormal = new float[n * 3];
        this.coneVertexColor = new float[n * 4];
        Vector3f v;
        Vector4f v4;
        for (int i = 0, j = 0, k = 0; i < n; i++, j+=3, k+=4) {
            v = vertexPositionList.get(i);
            coneVertexPosition[j] = v.x;
            coneVertexPosition[j + 1] = v.y;
            coneVertexPosition[j + 2] = v.z;
            v = vertexNormalList.get(i);
            coneVertexNormal[j] = v.x;
            coneVertexNormal[j + 1] = v.y;
            coneVertexNormal[j + 2] = v.z;
            v4 = vertexColorList.get(i);
            coneVertexColor[k] = v4.x;
            coneVertexColor[k + 1] = v4.y;
            coneVertexColor[k + 2] = v4.z;
            coneVertexColor[k + 3] = v4.w;
        }
        coneVertexIndices = vertexIndices.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && this.transform != null && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (updateBuffer) {
            if (this.vertexPosition == null) {
                this.getVertexPosition();
            }
            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPosition);
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

            if (this.streamline) {
                this.updateConeVertex();

                FloatBuffer coneVertexBuffer = GLBuffers.newDirectFloatBuffer(coneVertexPosition);
                sizeConePosition = coneVertexBuffer.capacity() * Float.BYTES;
                FloatBuffer coneNormalBuffer = GLBuffers.newDirectFloatBuffer(coneVertexNormal);
                sizeConeNormal = coneNormalBuffer.capacity() * Float.BYTES;
                FloatBuffer coneColorBuffer = GLBuffers.newDirectFloatBuffer(coneVertexColor);
                sizeConeColor = coneColorBuffer.capacity() * Float.BYTES;

                vboCone = GLBuffers.newDirectIntBuffer(2);
                gl.glGenBuffers(2, vboCone);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboCone.get(0));
                gl.glBufferData(GL.GL_ARRAY_BUFFER, sizeConePosition + sizeConeNormal + sizeConeColor,
                        null, GL.GL_STATIC_DRAW);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizeConePosition, coneVertexBuffer);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizeConePosition, sizeConeNormal, coneNormalBuffer);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizeConePosition + sizeConeNormal, sizeConeColor,
                        coneColorBuffer);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

                IntBuffer coneIndexBuffer = GLBuffers.newDirectIntBuffer(coneVertexIndices);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboCone.get(1));
                gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, coneIndexBuffer.capacity() * Integer.BYTES,
                        coneIndexBuffer, GL.GL_STATIC_DRAW);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
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

            if (this.streamline) {
                gl.glPushMatrix();
                FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
                gl.glLoadMatrixf(this.modelViewMatrixR.get(fb));

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboCone.get(0));
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboCone.get(1));

                // enable vertex arrays
                gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
                gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
                gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
                gl.glNormalPointer(GL.GL_FLOAT, 0, sizeConePosition);
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
                gl.glColorPointer(4, GL.GL_FLOAT, 0, sizeConePosition + sizeConeNormal);

                gl.glDrawElements(GL2.GL_TRIANGLES, coneVertexIndices.length, GL.GL_UNSIGNED_INT, 0);

                gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
                gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

                gl.glPopMatrix();
            }
        }
    }
}
