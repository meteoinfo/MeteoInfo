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

public class Rhombus implements GLEventListener {

    private GLU glu = new GLU();

    @Override
    public void display(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();
        gl.glTranslatef(0f, 0f, -2.5f);

        //drawing edge1.....
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-0.75f, 0f, 0);
        gl.glVertex3f(0f, -0.75f, 0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-0.75f, 0f, 3f); // 3 units into the window
        gl.glVertex3f(0f, -0.75f, 3f);
        gl.glEnd();

        //top
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-0.75f, 0f, 0);
        gl.glVertex3f(-0.75f, 0f, 3f);
        gl.glEnd();

        // bottom
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, -0.75f, 0);
        gl.glVertex3f(0f, -0.75f, 3f);
        gl.glEnd();

        // edge 2....
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, -0.75f, 0);
        gl.glVertex3f(0.75f, 0f, 0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, -0.75f, 3f);
        gl.glVertex3f(0.75f, 0f, 3f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, -0.75f, 0);
        gl.glVertex3f(0f, -0.75f, 3f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0.75f, 0f, 0);
        gl.glVertex3f(0.75f, 0f, 3f);
        gl.glEnd();

        //Edge 3.............
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0.0f, 0.75f, 0);
        gl.glVertex3f(-0.75f, 0f, 0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0.0f, 0.75f, 3f);
        gl.glVertex3f(-0.75f, 0f, 3f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0.0f, 0.75f, 0);
        gl.glVertex3f(0.0f, 0.75f, 3f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-0.75f, 0f, 0);
        gl.glVertex3f(-0.75f, 0f, 3f);
        gl.glEnd();

        //final edge
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0.75f, 0f, 0);
        gl.glVertex3f(0.0f, 0.75f, 0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0.75f, 0f, 3f);
        gl.glVertex3f(0.0f, 0.75f, 3f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0.75f, 0f, 0);
        gl.glVertex3f(0.75f, 0f, 3f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0.0f, 0.75f, 0);
        gl.glVertex3f(0.0f, 0.75f, 3f);
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

        // TODO Auto-generated method stub final
        GL2 gl = drawable.getGL().getGL2();
        if (height  <= 0)
            height = 1;

        final float h = (float) width / (float) height;
        gl.glViewport(3, 6, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(45.0f, h, 1.0, 20.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public static void main(String[] args) {

        //getting the capabilities object of GL2 profile
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        // The canvas
        final GLCanvas glcanvas = new GLCanvas(capabilities);
        Rhombus b = new Rhombus();
        glcanvas.addGLEventListener(b);
        glcanvas.setSize(400, 400);

        //creating frame
        final JFrame frame = new JFrame(" Rhombus 3d");

        //adding canvas to it
        frame.getContentPane().add(glcanvas);
        frame.setSize(frame.getContentPane().getPreferredSize());
        frame.setVisible(true);
    }//end of main

}//end of classimport javax.media.opengl.GL2;
