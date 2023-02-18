package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.GraphicCollection3D;
import org.meteoinfo.chart.graphic.cylinder.Cylinder;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.chart.graphic.pipe.Pipe;
import org.meteoinfo.chart.graphic.pipe.PipeShape;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.PolylineZShape;
import org.meteoinfo.math.Matrix4f;

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

        ColorBreak cb = graphics.getGraphicN(0).getLegend();
        PolylineBreak lineBreak;
        if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
            lineBreak = (PolylineBreak) ((ColorBreakCollection) cb).get(0);
        } else {
            lineBreak = (PolylineBreak) cb;
        }
        this.streamline = lineBreak instanceof StreamlineBreak;
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
            //shape.transform(transform);
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
                List<Vector3f> c1 = pipe.getContour(j);
                List<Vector3f> c2 = pipe.getContour(j + 1);
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

    private void updateConeVertex() {
        List<Vector3f> vertexPositionList = new ArrayList<>();
        List<Vector3f> vertexNormalList = new ArrayList<>();
        List<Vector4f> vertexColorList = new ArrayList<>();
        List<Integer> vertexIndices = new ArrayList<>();
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
                        v1 = new Vector3f((float) p1.X, (float) p1.Y, (float) p1.Z);
                        v2 = new Vector3f((float) p2.X, (float) p2.Y, (float) p2.Z);
                        slb = (StreamlineBreak) cbc.get(i);
                        Cylinder cylinder = new Cylinder(slb.getArrowHeadWidth() * 0.02f,
                                0, slb.getArrowHeadLength() * 0.02f, 8, 1, true);
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
                        for (Vector3f v : normals) {
                            vertexNormalList.add(matrix.mul(v));
                        }
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
                        v1 = new Vector3f((float) p1.X, (float) p1.Y, (float) p1.Z);
                        v2 = new Vector3f((float) p2.X, (float) p2.Y, (float) p2.Z);
                        Cylinder cylinder = new Cylinder(slb.getArrowHeadWidth() * 0.02f,
                                0, slb.getArrowHeadLength() * 0.02f, 8, 1, true);
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
                        for (Vector3f v : normals) {
                            vertexNormalList.add(matrix.mul(v));
                        }
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

            if (this.streamline) {
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
            }
        }
    }
}
