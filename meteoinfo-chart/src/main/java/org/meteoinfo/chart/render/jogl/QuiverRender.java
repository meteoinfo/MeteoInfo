package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
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
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.PolylineZShape;
import org.meteoinfo.geometry.shape.WindArrow3D;
import org.meteoinfo.math.Matrix4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class QuiverRender extends JOGLGraphicRender {

    private GraphicCollection3D graphics;
    private Program program;
    private IntBuffer vbo;
    private int quiverNumber;
    private float lineWidth;
    private float[] vertexPosition;
    private float[] vertexColor;
    private int sizePosition;
    private int sizeColor;
    private IntBuffer vboCone;
    private Cylinder arrow;
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
    public QuiverRender(GL2 gl) {
        super(gl);

        if (useShader) {
            try {
                this.compileShaders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructor
     * @param gl The JOGL GL2 object
     * @param graphics The quiver graphics
     */
    public QuiverRender(GL2 gl, GraphicCollection3D graphics) {
        this(gl);

        this.graphics = graphics;
        this.quiverNumber = graphics.getNumGraphics();
        PointBreak pb = (PointBreak) graphics.getGraphicN(0).getLegend();
        this.lineWidth = pb.getOutlineSize();
    }

    private void updateVertexArrays() {
        vertexPosition = new float[quiverNumber * 6];
        vertexColor = new float[quiverNumber * 8];
        List<Vector3f> vertexPositionList = new ArrayList<>();
        List<Vector3f> vertexNormalList = new ArrayList<>();
        List<Vector4f> vertexColorList = new ArrayList<>();
        List<Integer> vertexIndices = new ArrayList<>();
        Cylinder cylinder = null;
        for (int i = 0, pi = 0, ci = 0; i < quiverNumber; i++, pi+=6, ci+=8) {
            Graphic graphic = graphics.getGraphicN(i);
            WindArrow3D shape = (WindArrow3D) graphic.getShape();
            PointBreak pb = (PointBreak) graphic.getLegend();
            PointZ sp = (PointZ) shape.getPoint();
            PointZ ep = (PointZ) shape.getEndPoint();

            Vector3f v1 = transform.transform((float) sp.X, (float) sp.Y, (float) sp.Z);
            Vector3f v2 = transform.transform((float) ep.X, (float) ep.Y, (float) ep.Z);
            //Vector3f v1 = new Vector3f((float) sp.X, (float) sp.Y, (float) sp.Z);
            //Vector3f v2 = new Vector3f((float) ep.X, (float) ep.Y, (float) ep.Z);
            float[] color = pb.getColor().getRGBComponents(null);
            vertexPosition[pi] = v1.x;
            vertexPosition[pi + 1] = v1.y;
            vertexPosition[pi + 2] = v1.z;
            vertexPosition[pi + 3] = v2.x;
            vertexPosition[pi + 4] = v2.y;
            vertexPosition[pi + 5] = v2.z;
            System.arraycopy(color, 0, vertexColor, ci, 4);
            System.arraycopy(color, 0, vertexColor, ci + 4, 4);

            if (cylinder == null) {
                cylinder = new Cylinder(shape.getHeadWidth() * 0.02f,
                        0, shape.getHeadLength() * 0.02f, 8, 1, true);
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

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/surface/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/surface/surface.frag");
        program = new Program("surface", vertexShaderCode, fragmentShaderCode);
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && this.transform != null && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (updateBuffer) {
            this.updateVertexArrays();

            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPosition);
            sizePosition = vertexBuffer.capacity() * Float.BYTES;

            FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(vertexColor);
            sizeColor = colorBuffer.capacity() * Float.BYTES;
            int totalSize = sizePosition + sizeColor;

            vbo = GLBuffers.newDirectIntBuffer(1);
            gl.glGenBuffers(1, vbo);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
            gl.glBufferData(GL.GL_ARRAY_BUFFER, totalSize, null, GL.GL_STATIC_DRAW);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeColor, colorBuffer);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

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

    void setUniforms() {

    }

    @Override
    public void draw() {
        gl.glPushMatrix();
        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glLoadMatrixf(this.modelViewMatrixR.get(fb));

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
            gl.glDrawArrays(GL.GL_LINES, 0, quiverNumber * 2);
            if (lightEnabled) {
                this.lighting.start(gl);
            }

            // draw wind arrow
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

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

        gl.glPopMatrix();
    }
}
