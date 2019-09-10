/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogl;

import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import javax.swing.JFrame;

public class J3DBasic implements GLEventListener {

    private GLU glu = new GLU();

    @Override
    public void display(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();
        gl.glTranslatef(0f, 0f, -3f);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1f, 0f, 0);
        gl.glVertex3f(0f, 1f, 0);
        gl.glEnd();

        //3D  
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1f, 0f, -2f);
        gl.glVertex3f(0f, 1f, -2f);
        gl.glEnd();

        //top  
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1f, 0f, 0);
        gl.glVertex3f(-1f, 0f, -2f);
        gl.glEnd();

        //bottom  
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, 1f, 0);
        gl.glVertex3f(0f, 1f, -2f);
        gl.glEnd();
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        //method body  
    }

    @Override
    public void init(GLAutoDrawable arg0) {
        // method body  
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) {
            height = 1;
        }

        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(40.0f, h, 1.5, 18.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

    }

    public static void main(String[] args) {

        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        final GLCanvas gc = new GLCanvas(cap);
        J3DBasic b = new J3DBasic();
        gc.addGLEventListener(b);
        gc.setSize(600, 400);

        final JFrame frame = new JFrame("JOGL 3D");

        frame.getContentPane().add(gc);
        frame.setSize(frame.getContentPane().getPreferredSize());
        frame.setVisible(true);
    }
}
