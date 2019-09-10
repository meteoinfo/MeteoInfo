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

public class JRotation implements GLEventListener {

    public float rotation;

    @Override
    public void display(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Clear The Screen And The Depth Buffer   
        gl.glLoadIdentity();  // Reset The View       

        //triangle rotation        
        gl.glRotatef(rotation, 0.0f, 1.0f, 0.0f);

        gl.glBegin(GL2.GL_TRIANGLES);
        //Green Color  
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glVertex2d(0, 0.5);
        gl.glVertex2d(-0.5, -0.5);
        gl.glVertex2d(0.5, -0.5);

        gl.glEnd();

        gl.glFlush();
        //Assign the angle  
        rotation += 0.6f;
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
    }

    public static void main(String[] args) {

        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        final GLCanvas gc = new GLCanvas(cap);
        JRotation jr = new JRotation();
        gc.addGLEventListener(jr);
        gc.setSize(400, 400);

        final JFrame frame = new JFrame("JOGL Rotation");

        frame.add(gc);
        frame.setSize(500, 400);
        frame.setVisible(true);

        final FPSAnimator animator = new FPSAnimator(gc, 400, true);
        animator.start();

    }

}
