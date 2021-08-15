import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallback;
import org.meteoinfo.common.util.GlobalUtil;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import java.util.List;

/**
 * Filled contour tessellation demo
 */
public class Tessellation extends JFrame implements GLEventListener, KeyListener {

    private GLCapabilities caps;
    private GLJPanel canvas;
    private GLU glu;
    private int startList;
    private List<Polygon> polygons;


    public Tessellation() {
        super("tess");// name of file as title in window bar
        /*
         * display mode (single buffer and RGBA)
         */
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(false);
        System.out.println(caps.toString());
        canvas = new GLJPanel(caps);
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);

        getContentPane().add(canvas);
    }

    /*
     * Declare initial window size, position, and set frame's close behavior. Open
     * window with "hello" in its title bar. Call initialization routines.
     * Register callback function to display graphics. Enter main loop and process
     * events.
     */
    public void run() {
        try {
            readPolygonData("D:/Temp/test/tessellation_test.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSize(500, 500);
        setLocationRelativeTo(null); // center
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        canvas.requestFocusInWindow();
    }

    public static void main(String[] args) {
        new Tessellation().run();
    }

    void readPolygonData(String fileName) throws IOException {
        this.polygons = new ArrayList<>();
        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        String line = sr.readLine();
        line = sr.readLine().trim();
        int polygonNum = Integer.parseInt(line);
        for (int i = 0; i < polygonNum; i++) {
            Polygon polygon = new Polygon();
            line = sr.readLine();    //Polygon
            line = sr.readLine();    //Color
            line = sr.readLine().trim();
            Color color = new Color(Integer.parseInt(line));
            polygon.setColor(color);
            line = sr.readLine();    //Outline
            line = sr.readLine().trim();
            int n = Integer.parseInt(line);
            List<Point3D> points = new ArrayList<>();
            Point3D point;
            for (int j = 0; j < n; j++) {
                line = sr.readLine().trim();
                String[] data = line.split(",");
                point = new Point3D(Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                        Double.parseDouble(data[2]));
                points.add(point);
            }
            polygon.setOutline(points);
            line = sr.readLine().trim();    //Hole number
            int holeNum = Integer.parseInt(line);
            for (int j = 0; j < holeNum; j++) {
                line = sr.readLine();    //Hole
                line = sr.readLine().trim();
                n = Integer.parseInt(line);
                points = new ArrayList<>();
                for (int m = 0; m < n; m++) {
                    line = sr.readLine().trim();
                    String[] data = line.split(",");
                    point = new Point3D(Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                            Double.parseDouble(data[2]));
                    points.add(point);
                }
                polygon.addHole(points);
            }
            this.polygons.add(polygon);
        }
        sr.close();
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        //method body
    }

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();

    }

    public void display(GLAutoDrawable drawable) {
        displayTest1(drawable);
    }

    void displayTest(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        gl.glRotatef(-45.0f, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(45.0f, 0.0f, 0.0f, 1.0f);

        /*
         * jogl specific addition for tessellation
         */
        tessellCallBack tessCallback = new tessellCallBack(gl, glu);

        double rect[][] = new double[][]{ // [4][3] in C; reverse here
                {0.0, 50.0, 50.0},
                {0.0, 200.0, 50.0},
                {0.0, 200.0, 200.0},
                {0.0, 50.0, 200.0}};
        double tri[][] = new double[][]{// [3][3]
                {0.0, 75.0, 75.0},
                {0.0, 125.0, 175.0},
                {0.0, 175.0, 75.0}};
        double star[][] = new double[][]{// [5][6]; 6x5 in java
                {0.0, 250.0, 50.0, 1.0, 0.0, 1.0},
                {0.0, 325.0, 200.0, 1.0, 1.0, 0.0},
                {0.0, 400.0, 50.0, 0.0, 1.0, 1.0},
                {0.0, 250.0, 150.0, 1.0, 0.0, 0.0},
                {0.0, 400.0, 150.0, 0.0, 1.0, 0.0}};

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        startList = gl.glGenLists(2);

        GLUtessellator tobj = glu.gluNewTess();

        glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);// glVertex3dv);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);// beginCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);// endCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);// errorCallback);

        /* rectangle with triangular hole inside */
        gl.glNewList(startList, GL2.GL_COMPILE);
        gl.glShadeModel(GL2.GL_FLAT);
        glu.gluTessBeginPolygon(tobj, null);
        glu.gluTessBeginContour(tobj);
        glu.gluTessVertex(tobj, rect[0], 0, rect[0]);
        glu.gluTessVertex(tobj, rect[1], 0, rect[1]);
        glu.gluTessVertex(tobj, rect[2], 0, rect[2]);
        glu.gluTessVertex(tobj, rect[3], 0, rect[3]);
        glu.gluTessEndContour(tobj);
        glu.gluTessBeginContour(tobj);
        glu.gluTessVertex(tobj, tri[0], 0, tri[0]);
        glu.gluTessVertex(tobj, tri[1], 0, tri[1]);
        glu.gluTessVertex(tobj, tri[2], 0, tri[2]);
        glu.gluTessEndContour(tobj);
        glu.gluTessEndPolygon(tobj);
        gl.glEndList();

        glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);// vertexCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);// beginCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);// endCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);// errorCallback);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, tessCallback);// combineCallback);

        /* smooth shaded, self-intersecting star */
        gl.glNewList(startList + 1, GL2.GL_COMPILE);
        gl.glShadeModel(GL2.GL_SMOOTH);
        glu.gluTessProperty(tobj, //
                GLU.GLU_TESS_WINDING_RULE, //
                GLU.GLU_TESS_WINDING_POSITIVE);
        glu.gluTessBeginPolygon(tobj, null);
        glu.gluTessBeginContour(tobj);
        glu.gluTessVertex(tobj, star[0], 0, star[0]);
        glu.gluTessVertex(tobj, star[1], 0, star[1]);
        glu.gluTessVertex(tobj, star[2], 0, star[2]);
        glu.gluTessVertex(tobj, star[3], 0, star[3]);
        glu.gluTessVertex(tobj, star[4], 0, star[4]);
        glu.gluTessEndContour(tobj);
        glu.gluTessEndPolygon(tobj);
        gl.glEndList();
        glu.gluDeleteTess(tobj);

        gl.glCallList(startList);
        gl.glCallList(startList + 1);
        gl.glFlush();
    }

    void displayTest1(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        gl.glRotatef(-60.0f, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);

        /*
         * jogl specific addition for tessellation
         */
        tessellCallBack tessCallback = new tessellCallBack(gl, glu);

        startList = gl.glGenLists(this.polygons.size());

        GLUtessellator tobj = glu.gluNewTess();

        glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);// glVertex3dv
        glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);// beginCallback
        glu.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);// endCallback
        glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);// errorCallback
        //glu.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, tessCallback);// combineCallback;

        int i = 0;
        for (Polygon polygon : this.polygons) {
            gl.glNewList(startList + i, GL2.GL_COMPILE);
            //gl.glShadeModel(GL2.GL_FLAT);
            glu.gluTessBeginPolygon(tobj, null);
            float[] rgba = polygon.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            glu.gluTessBeginContour(tobj);
            for (Point3D p : polygon.getOutline()) {
                glu.gluTessVertex(tobj, p.toArray(), 0, p.toArray());
            }
            glu.gluTessEndContour(tobj);
            for (int j = 0; j < polygon.getHoleNumber(); j++) {
                glu.gluTessBeginContour(tobj);
                for (Point3D p : polygon.getHole(j)) {
                    glu.gluTessVertex(tobj, p.toArray(), 0, p.toArray());
                }
                glu.gluTessEndContour(tobj);
            }
            glu.gluTessEndPolygon(tobj);
            gl.glEndList();
            i = i + 1;
        }

        glu.gluDeleteTess(tobj);

        for (i = 0; i < this.polygons.size(); i++) {
            gl.glCallList(startList + i);
        }
        gl.glFlush();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final GL2 gl = drawable.getGL().getGL2();
        //gl.glTranslatef(0f, 0f, 5f);
        if (height <= 0) {
            height = 1;
        }

        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        //glu.gluPerspective(45.0f, h, 1.0, 20.0);
        float v = 200.0f;
        //gl.glOrthof(-180, 180, -v, v, -10, 10);
        gl.glOrthof(-v, v, 50, 80, -v, v);
        //glu.gluLookAt(0.0f, 0.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
                               boolean deviceChanged) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                System.exit(0);

            default:
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    /*
     * Tessellator callback implemenation with all the callback routines. YOu
     * could use GLUtesselatorCallBackAdapter instead. But
     */
    class tessellCallBack
            implements GLUtessellatorCallback {

        private GL2 gl;
        private GLU glu;

        public tessellCallBack(GL2 gl, GLU glu) {
            this.gl = gl;
            this.glu = glu;
        }

        public void begin(int type) {
            gl.glBegin(type);
        }

        public void end() {
            gl.glEnd();
        }

        public void vertex(Object vertexData) {
            double[] pointer;
            if (vertexData instanceof double[]) {
                pointer = (double[]) vertexData;
                if (pointer.length == 6) {
                    gl.glColor3dv(pointer, 3);
                }
                gl.glVertex3dv(pointer, 0);
            }

        }

        public void vertexData(Object vertexData, Object polygonData) {
        }

        /*
         * combineCallback is used to create a new vertex when edges intersect.
         * coordinate location is trivial to calculate, but weight[4] may be used to
         * average color, normal, or texture coordinate data. In this program, color
         * is weighted.
         */
        public void combine(double[] coords, Object[] data, //
                            float[] weight, Object[] outData) {
            double[] vertex = new double[6];
            int i;

            vertex[0] = coords[0];
            vertex[1] = coords[1];
            vertex[2] = coords[2];
            for (i = 3; i < 6/* 7OutOfBounds from C! */; i++) {
                vertex[i] = weight[0] //
                        * ((double[]) data[0])[i] + weight[1]
                        * ((double[]) data[1])[i] + weight[2]
                        * ((double[]) data[2])[i] + weight[3]
                        * ((double[]) data[3])[i];
            }
            outData[0] = vertex;
        }

        public void combineData(double[] coords, Object[] data, //
                                float[] weight, Object[] outData, Object polygonData) {
        }

        public void error(int errnum) {
            String estring;

            estring = glu.gluErrorString(errnum);
            System.err.println("Tessellation Error: " + estring);
            System.exit(0);
        }

        public void beginData(int type, Object polygonData) {
        }

        public void endData(Object polygonData) {
        }

        public void edgeFlag(boolean boundaryEdge) {
        }

        public void edgeFlagData(boolean boundaryEdge, Object polygonData) {
        }

        public void errorData(int errnum, Object polygonData) {
        }
    }// tessellCallBack

}// tess
