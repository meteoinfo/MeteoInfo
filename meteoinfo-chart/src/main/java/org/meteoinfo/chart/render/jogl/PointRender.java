package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.GraphicCollection3D;
import org.meteoinfo.chart.graphic.ParticleGraphics;
import org.meteoinfo.chart.graphic.sphere.Sphere;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.PointZShape;
import org.meteoinfo.geometry.shape.Polyline;
import org.meteoinfo.geometry.shape.PolylineZShape;
import org.meteoinfo.math.Matrix4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PointRender extends JOGLGraphicRender {

    private GraphicCollection3D graphics;
    private IntBuffer vbo;
    private Program program;
    private int pointNum;
    private int sizePosition;
    private int sizeColor;
    private int sizeNormal;
    private float pointSize;
    private boolean sphere;
    private float sphereScale = 0.005f;
    private float[] vertexPosition;
    private float[] vertexNormal;
    private float[] vertexColor;
    private int[] vertexIndices;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public PointRender(GL2 gl) {
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
     * @param gl The opengl pipeline
     * @param pointGraphics 3D point graphics
     */
    public PointRender(GL2 gl, GraphicCollection3D pointGraphics) {
        this(gl);

        this.graphics = pointGraphics;
        this.sphere = this.graphics.isSphere();
        if (this.graphics instanceof ParticleGraphics) {
            this.pointNum = ((ParticleGraphics) this.graphics).getPointNumber();
            this.pointSize = ((ParticleGraphics) this.graphics).getPointSize();
        } else {
            this.pointNum = pointGraphics.getNumGraphics();
            PointBreak pb = (PointBreak) this.graphics.getGraphicN(0).getLegend();
            this.pointSize = pb.getSize();
        }
    }

    void updateVertex() {
        if (this.sphere) {
            this.updateSphereVertex();
        } else {
            this.updateVertexPosition();
            this.updateVertexColor();
        }
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
        this.vertexColor = new float[this.pointNum * 4];
        int i = 0;
        if (this.graphics instanceof ParticleGraphics) {
            ParticleGraphics particles = (ParticleGraphics) graphics;
            for (Map.Entry<Integer, java.util.List> map : particles.getParticleList()) {
                for (ParticleGraphics.Particle p : (java.util.List<ParticleGraphics.Particle>)map.getValue()) {
                    System.arraycopy(p.rgba, 0, vertexColor, i * 4, 4);
                    i++;
                }
            }
        } else {
            float[] color;
            for (Graphic graphic : this.graphics.getGraphics()) {
                PointBreak pb = (PointBreak) graphic.getLegend();
                color = pb.getColor().getRGBComponents(null);
                System.arraycopy(color, 0, vertexColor, i * 4, 4);
                i++;
            }
        }
    }

    private void updateVertexPosition() {
        if (this.sphere) {
            this.updateSphereVertexPosition();
        } else {
            this.vertexPosition = new float[this.pointNum * 3];
            int i = 0;
            if (this.graphics instanceof ParticleGraphics) {
                ParticleGraphics particles = (ParticleGraphics) graphics;
                for (Map.Entry<Integer, java.util.List> map : particles.getParticleList()) {
                    for (ParticleGraphics.Particle p : (java.util.List<ParticleGraphics.Particle>)map.getValue()) {
                        vertexPosition[i] = transform.transform_x((float) p.x);
                        vertexPosition[i + 1] = transform.transform_y((float) p.y);
                        vertexPosition[i + 2] = transform.transform_z((float) p.z);
                        i += 3;
                    }
                }
            } else {
                for (Graphic graphic : this.graphics.getGraphics()) {
                    PointZShape shape = (PointZShape) graphic.getShape();
                    PointZ p = (PointZ) shape.getPoint();
                    vertexPosition[i] = transform.transform_x((float) p.X);
                    vertexPosition[i + 1] = transform.transform_y((float) p.Y);
                    vertexPosition[i + 2] = transform.transform_z((float) p.Z);
                    i += 3;
                }
            }
        }
    }

    private void updateSphereVertex() {
        List<Vector3f> vertexPositionList = new ArrayList<>();
        List<Vector3f> vertexNormalList = new ArrayList<>();
        List<Vector4f> vertexColorList = new ArrayList<>();
        List<Integer> vertexIndexList = new ArrayList<>();
        Vector3f vp;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PointZShape shape = (PointZShape) graphic.getShape();
            PointZ p = (PointZ) shape.getPoint();
            PointBreak pb = (PointBreak) graphic.getLegend();
            Sphere sphere = new Sphere(pb.getSize() * sphereScale * dpiScale, 36, 18);
            vp = transform.transform((float) p.X, (float) p.Y, (float) p.Z);
            Matrix4f matrix = new Matrix4f();
            matrix.translate(vp);
            List<Vector3f> vertices = sphere.getVertices();
            int n = vertices.size();
            int nAdded = vertexPositionList.size();
            for (Vector3f v : vertices) {
                vertexPositionList.add(matrix.mul(v));
            }
            List<Vector3f> normals = sphere.getNormals();
            vertexNormalList.addAll(normals);
            float[] color = pb.getColor().getRGBComponents(null);
            for (int j = 0; j < n; j++) {
                vertexColorList.add(new Vector4f(color));
            }
            if (nAdded == 0) {
                vertexIndexList.addAll(sphere.getIndices());
            } else {
                for (int idx : sphere.getIndices()) {
                    vertexIndexList.add(idx + nAdded);
                }
            }
        }

        int n = vertexPositionList.size();
        this.vertexPosition = new float[n * 3];
        this.vertexNormal = new float[n * 3];
        this.vertexColor = new float[n * 4];
        Vector3f v;
        Vector4f v4;
        for (int i = 0, j = 0, k = 0; i < n; i++, j+=3, k+=4) {
            v = vertexPositionList.get(i);
            vertexPosition[j] = v.x;
            vertexPosition[j + 1] = v.y;
            vertexPosition[j + 2] = v.z;
            v = vertexNormalList.get(i);
            vertexNormal[j] = v.x;
            vertexNormal[j + 1] = v.y;
            vertexNormal[j + 2] = v.z;
            v4 = vertexColorList.get(i);
            vertexColor[k] = v4.x;
            vertexColor[k + 1] = v4.y;
            vertexColor[k + 2] = v4.z;
            vertexColor[k + 3] = v4.w;
        }
        vertexIndices = vertexIndexList.stream().mapToInt(Integer::intValue).toArray();
    }

    private void updateSphereVertexPosition() {
        List<Vector3f> vertexPositionList = new ArrayList<>();
        Vector3f vp;
        for (Graphic graphic : this.graphics.getGraphics()) {
            PointZShape shape = (PointZShape) graphic.getShape();
            PointZ p = (PointZ) shape.getPoint();
            PointBreak pb = (PointBreak) graphic.getLegend();
            Sphere sphere = new Sphere(pb.getSize() * sphereScale * dpiScale, 36, 18);
            vp = transform.transform((float) p.X, (float) p.Y, (float) p.Z);
            Matrix4f matrix = new Matrix4f();
            matrix.translate(vp);
            List<Vector3f> vertices = sphere.getVertices();
            int n = vertices.size();
            int nAdded = vertexPositionList.size();
            for (Vector3f v : vertices) {
                vertexPositionList.add(matrix.mul(v));
            }
        }

        int n = vertexPositionList.size();
        this.vertexPosition = new float[n * 3];
        Vector3f v;
        for (int i = 0, j = 0, k = 0; i < n; i++, j+=3, k+=4) {
            v = vertexPositionList.get(i);
            vertexPosition[j] = v.x;
            vertexPosition[j + 1] = v.y;
            vertexPosition[j + 2] = v.z;
        }
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && this.transform != null && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (updateBuffer) {
            if (this.vertexPosition == null) {
                this.updateVertex();
            } else {
                //this.updateVertex();
                this.updateVertexPosition();
            }
            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(this.vertexPosition);
            sizePosition = vertexBuffer.capacity() * Float.BYTES;

            FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(vertexColor);
            sizeColor = colorBuffer.capacity() * Float.BYTES;

            if (this.sphere) {
                FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(vertexNormal);
                sizeNormal = normalBuffer.capacity() * Float.BYTES;

                int totalSize = sizePosition + sizeColor + sizeNormal;

                vbo = GLBuffers.newDirectIntBuffer(2);
                gl.glGenBuffers(2, vbo);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
                gl.glBufferData(GL.GL_ARRAY_BUFFER, totalSize, null, GL.GL_STATIC_DRAW);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeColor, colorBuffer);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition + sizeColor, sizeNormal, normalBuffer);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

                IntBuffer indexBuffer = GLBuffers.newDirectIntBuffer(this.vertexIndices);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));
                gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * Integer.BYTES, indexBuffer, GL.GL_STATIC_DRAW);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
            } else {
                int totalSize = sizePosition + sizeColor;

                gl.glGenBuffers(1, vbo);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
                gl.glBufferData(GL.GL_ARRAY_BUFFER, totalSize, null, GL.GL_STATIC_DRAW);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeColor, colorBuffer);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
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
            if (this.sphere) {
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));

                gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
                gl.glNormalPointer(GL.GL_FLOAT, 0, sizePosition + sizeColor);

                //gl.glEnable(GL.GL_CULL_FACE);
                gl.glDrawElements(GL2.GL_TRIANGLES, this.vertexIndices.length, GL.GL_UNSIGNED_INT, 0);

                gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
                gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
            } else {
                boolean lightEnabled = this.lighting.isEnable();
                if (lightEnabled) {
                    this.lighting.stop(gl);
                }
                gl.glPointSize(this.pointSize * this.dpiScale);
                gl.glDrawArrays(GL.GL_POINTS, 0, this.pointNum);

                if (lightEnabled) {
                    this.lighting.start(gl);
                }

                gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            }
        }
    }
}
