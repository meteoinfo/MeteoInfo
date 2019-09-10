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
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

public class BasicLine implements GLEventListener {

    @Override
    public void init(GLAutoDrawable arg0) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        //Draw H  
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(-0.8, 0.6);
        gl.glVertex2d(-0.8, -0.6);
        gl.glVertex2d(-0.8, 0.0);
        gl.glVertex2d(-0.4, 0.0);
        gl.glVertex2d(-0.4, 0.6);
        gl.glVertex2d(-0.4, -0.6);
        gl.glEnd();
//Draw W  
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(0.4, 0.6);
        gl.glVertex2d(0.4, -0.6);
        gl.glVertex2d(0.4, -0.6);
        gl.glVertex2d(0.6, 0);
        gl.glVertex2d(0.6, 0);
        gl.glVertex2d(0.8, -0.6);
        gl.glVertex2d(0.8, -0.6);
        gl.glVertex2d(0.8, 0.6);
        gl.glEnd();

    }

    @Override
    public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {

    }

    @Override
    public void dispose(GLAutoDrawable arg0) {

    }

    public static void main(String[] args) {

        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        final GLJPanel gc = new GLJPanel(cap);
        BasicLine bl = new BasicLine();
        gc.addGLEventListener(bl);
        gc.setSize(400, 400);

        final JFrame frame = new JFrame("JOGL Line");
        frame.add(gc);
        frame.setSize(500, 400);
        frame.setVisible(true);
    }
}
