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
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.JFrame;

public class JPaddle implements GLEventListener {

    private float rotation = 0.0f;

    @Override
    public void display(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0f, 0f, -2.0f);
        gl.glRotatef(rotation, 1f, 0f, 0f);

        gl.glColor3f(1f, 0f, 0f);
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex3d(-0.5, 0.3, 0.8);
        gl.glVertex3d(0.5, 0.3, 0.8);
        gl.glVertex3d(0.8, 0.7, 0.8);
        gl.glVertex3d(-0.8, 0.7, 0.8);
        gl.glEnd();

        int paddles = 40;
        for (int i = 0; i < paddles; i++) {
            gl.glRotated(360.0 / paddles, 1, 0, 0);
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex3d(-0.5, 0.3, 0.8);
            gl.glVertex3d(0.5, 0.3, 0.8);
            gl.glVertex3d(0.8, 0.7, 0.8);
            gl.glVertex3d(-0.8, 0.7, 0.8);

            gl.glEnd();

        }

        rotation -= 0.2f;

    }

    public void reshape(GLAutoDrawable drawable, int x, int y,
            int width, int height) {

    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
            boolean deviceChanged) {
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {

    }

    @Override
    public void init(GLAutoDrawable arg0) {

    }

    public static void main(String[] args) {

        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        final GLCanvas gc = new GLCanvas(cap);
        JPaddle paddle = new JPaddle();

        gc.addGLEventListener(paddle);
        gc.setSize(400, 400);

        final JFrame frame = new JFrame("Motor Paddle");
        frame.add(gc);
        frame.setSize(600, 500);
        frame.setVisible(true);

        final FPSAnimator animator = new FPSAnimator(gc, 200, true);
        animator.start();
    }

}
