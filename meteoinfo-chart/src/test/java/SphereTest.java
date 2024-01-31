import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.sphere.Sphere;
import org.meteoinfo.math.Matrix4f;

import javax.swing.*;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class SphereTest implements GLEventListener {

    private List<Sphere> spheres;
    private IntBuffer vbo;
    private int sizePosition;
    private int sizeColor;
    private int sizeNormal;
    private float[] vertexPosition;
    private float[] vertexNormal;
    private float[] vertexColor;
    private int[] vertexIndices;
    private float rquad = 0.0f;

    public SphereTest() {
        vbo = GLBuffers.newDirectIntBuffer(2);
        createSpheres(3);
        updateSphereVertex();
    }

    void createSpheres(int n) {
        this.spheres = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            this.spheres.add(new Sphere(0.1f, 36, 18));
        }
    }

    private void updateSphereVertex() {
        List<Vector3f> vertexPositionList = new ArrayList<>();
        List<Vector3f> vertexNormalList = new ArrayList<>();
        List<Vector4f> vertexColorList = new ArrayList<>();
        List<Integer> vertexIndexList = new ArrayList<>();
        Vector3f vp;
        int idx = 0, sn = this.spheres.size();
        float max = 0.5f;
        float x, y, z;
        for (Sphere sphere : this.spheres) {
            z = max / sn * idx;
            x = z * (float) Math.sin(z * 20);
            y = z * (float) Math.sin(z * 20);
            vp = new Vector3f(x, y, z);
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
            float[] color = Color.RED.getRGBComponents(null);
            for (int j = 0; j < n; j++) {
                vertexColorList.add(new Vector4f(color));
            }
            if (nAdded == 0) {
                vertexIndexList.addAll(sphere.getIndices());
            } else {
                for (int ii : sphere.getIndices()) {
                    vertexIndexList.add(ii + nAdded);
                }
            }
            idx += 1;
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

    private void updateBuffer(GL2 gl) {
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(this.vertexPosition);
        sizePosition = vertexBuffer.capacity() * Float.BYTES;

        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(vertexColor);
        sizeColor = colorBuffer.capacity() * Float.BYTES;

        FloatBuffer normalBuffer = GLBuffers.newDirectFloatBuffer(vertexNormal);
        sizeNormal = normalBuffer.capacity() * Float.BYTES;

        int totalSize = sizePosition + sizeColor + sizeNormal;

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
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        updateBuffer(gl);

        gl.glShadeModel( GL2.GL_SMOOTH );
        gl.glClearColor( 0f, 0f, 0f, 0f );
        gl.glClearDepth( 1.0f );
        gl.glEnable( GL2.GL_DEPTH_TEST );
        gl.glDepthFunc( GL2.GL_LEQUAL );
        gl.glHint( GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST );
        gl.glEnable(GL2.GL_COLOR_MATERIAL);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT1);
        //gl.glEnable(GL2.GL_AUTO_NORMAL);
        //gl.glEnable(GLLightingFunc.GL_NORMALIZE);

        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.f}, 0);
        //gl.glLightfv(this.light, GL2.GL_AMBIENT, ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, new float[]{1.f, 1.f, 1.f, 1.f}, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, new float[]{1.f, 1.f, 1.f, 1.f}, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, new float[]{0.f, 0.f, 1.f, 0.f}, 0);

        //Material
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1.f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[]{1.f, 1.f, 1.f, 1.f}, 0);
        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        gl.glLoadIdentity();

        // Rotate The Cube On X, Y & Z
        gl.glRotatef(rquad, 1.0f, 1.0f, 1.0f);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));

        // enable vertex arrays
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
        gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        gl.glColorPointer(4, GL.GL_FLOAT, 0, sizePosition);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));

        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glNormalPointer(GL.GL_FLOAT, 0, sizePosition + sizeColor);

        gl.glEnable(GL.GL_CULL_FACE);
        gl.glDrawElements(GL2.GL_TRIANGLES, this.vertexIndices.length, GL.GL_UNSIGNED_INT, 0);

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

        rquad -= 0.15f;
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }

    public static void main(String[] args) {
        //getting the capabilities object of GL2 profile
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        // The canvas
        final GLJPanel glcanvas = new GLJPanel(capabilities);
        SphereTest sphereTest = new SphereTest();
        glcanvas.addGLEventListener(sphereTest);
        //creating frame
        final JFrame frame = new JFrame ("Sphere test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        //adding canvas to frame
        frame.getContentPane().add(glcanvas);
        frame.setVisible(true);

        final FPSAnimator animator = new FPSAnimator(glcanvas, 300, true);
        animator.start();
    }

}
