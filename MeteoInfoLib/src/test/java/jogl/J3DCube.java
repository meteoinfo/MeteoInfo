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
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.JFrame;

public class J3DCube implements GLEventListener {

    private GLU glu = new GLU();
    private float rotation = 0.0f;

    @Override
    public void display(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0f, 0f, -2.0f);

        gl.glRotatef(rotation, 1.0f, 1.0f, 1.0f);

        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(0f, 0f, 1f); //Blue color  
        //Top Quadrilateral  
        gl.glVertex3f(0.5f, 0.5f, -0.5f); //Upper Right  
        gl.glVertex3f(-0.5f, 0.5f, -0.5f); // Upper Left  
        gl.glVertex3f(-0.5f, 0.5f, 0.5f); // Bottom Left  
        gl.glVertex3f(0.5f, 0.5f, 0.5f); // Bottom Right  
        //Below Quadrilateral  
        gl.glColor3f(1f, 0f, 0f); //Red color  
        gl.glVertex3f(0.5f, -0.5f, 0.5f); // Upper Right   
        gl.glVertex3f(-0.5f, -0.5f, 0.5f); // Upper Left   
        gl.glVertex3f(-0.5f, -0.5f, -0.5f); // Bottom Left   
        gl.glVertex3f(0.5f, -0.5f, -0.5f); // Bottom Right   
        //Front Quadrilateral  
        gl.glColor3f(0f, 1f, 0f); //Green color  
        gl.glVertex3f(0.5f, 0.5f, 0.5f); // Upper Right   
        gl.glVertex3f(-0.5f, 0.5f, 0.5f); // Upper Left   
        gl.glVertex3f(-0.5f, -0.5f, 0.5f); // Bottom Left   
        gl.glVertex3f(0.5f, -0.5f, 0.5f); // Bottom Right  
        //Back Quadrilateral  
        gl.glColor3f(1f, 1f, 0f); //Yellow  
        gl.glVertex3f(0.5f, -0.5f, -0.5f); // Bottom Left   
        gl.glVertex3f(-0.5f, -0.5f, -0.5f); // Bottom Right   
        gl.glVertex3f(-0.5f, 0.5f, -0.5f); // Upper Right   
        gl.glVertex3f(0.5f, 0.5f, -0.5f); // Upper Left   
        //Left Quadrilateral  
        gl.glColor3f(1f, 0f, 1f); //Purple  
        gl.glVertex3f(-0.5f, 0.5f, 0.5f); // Upper Right  
        gl.glVertex3f(-0.5f, 0.5f, -0.5f); // Upper Left   
        gl.glVertex3f(-0.5f, -0.5f, -0.5f); // Bottom Left   
        gl.glVertex3f(-0.5f, -0.5f, 0.5f); // Bottom Right   
        //Right Quadrilateral  
        gl.glColor3f(0f, 1f, 1f); //Cyan  
        gl.glVertex3f(0.5f, 0.5f, -0.5f); // Upper Right   
        gl.glVertex3f(0.5f, 0.5f, 0.5f); // Upper Left   
        gl.glVertex3f(0.5f, -0.5f, 0.5f); // Bottom Left   
        gl.glVertex3f(0.5f, -0.5f, -0.5f); // Bottom Right   
        gl.glEnd();
        gl.glFlush();

        rotation += 0.6f;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void init(GLAutoDrawable drawable) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        final GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) {
            height = 1;
        }

        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(45.0f, h, 1.0, 20.0);
        glu.gluLookAt(0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public static void main(String[] args) {

        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        final GLJPanel gc = new GLJPanel(cap);
        J3DCube cube = new J3DCube();

        gc.addGLEventListener(cube);
        gc.setSize(400, 400);

        final JFrame frame = new JFrame(" 3D cube");
        frame.add(gc);
        frame.setSize(600, 500);
        frame.setVisible(true);

        final FPSAnimator animator = new FPSAnimator(gc, 200, true);
        animator.start();
    }

}
