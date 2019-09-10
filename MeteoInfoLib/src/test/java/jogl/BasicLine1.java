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

public class BasicLine1 implements GLEventListener {

    @Override
    public void init(GLAutoDrawable arg0) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        //gl.glTranslatef(0f, 0f, 2.0f);

        //Draw H  
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3d(-0.9, -0.9, -1);
        gl.glVertex3d(0.9, 0.9, -1);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(1f, 1f, -1f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-2f, 2f, -2f, 2f, 0.0f, 1.0f);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {

    }

    public static void main(String[] args) {

        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        final GLJPanel gc = new GLJPanel(cap);
        BasicLine1 bl = new BasicLine1();
        gc.addGLEventListener(bl);
        gc.setSize(400, 400);

        final JFrame frame = new JFrame("JOGL Line");
        frame.add(gc);
        frame.setSize(500, 400);
        frame.setVisible(true);
    }
}
