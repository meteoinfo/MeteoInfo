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
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.JFrame;

public class JCuboid implements GLEventListener {

    private GLU glu = new GLU();
    private float rotation = 0.0f;

    @Override
    public void display(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0f, 0f, -5.0f);

        gl.glRotatef(rotation, 1.0f, 1.0f, 1.0f);

        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(1f, 0f, 0f); //red color  

        //Top  
        gl.glVertex3f(-1f, 0.8f, 0.5f);
        gl.glVertex3f(-0.5f, 0.8f, 0.5f);
        gl.glVertex3f(-1f, 0.5f, 0.5f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f);

        //Bottom  
        gl.glVertex3f(-1f, 0.8f, 0.9f);
        gl.glVertex3f(-1f, 0.5f, 0.9f);
        gl.glVertex3f(-0.5f, 0.5f, 0.9f);
        gl.glVertex3f(-0.5f, 0.8f, 0.9f);

        //Front  
        gl.glVertex3f(-0.5f, 0.8f, 0.5f);
        gl.glVertex3f(-1f, 0.8f, 0.5f);
        gl.glVertex3f(-1f, 0.8f, 0.9f);
        gl.glVertex3f(-0.5f, 0.8f, 0.9f);
        //Back  

        gl.glVertex3f(-0.5f, 0.5f, 0.5f);
        gl.glVertex3f(-1f, 0.5f, 0.5f);
        gl.glVertex3f(-1f, 0.5f, 0.9f);
        gl.glVertex3f(-0.5f, 0.5f, 0.9f);

        //Left  
        gl.glVertex3f(-0.5f, 0.8f, 0.9f);
        gl.glVertex3f(-0.5f, 0.8f, 0.5f);
        gl.glVertex3f(-0.5f, 0.5f, 0.9f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f);

        //Right  
        gl.glVertex3f(-1f, 0.8f, 0.9f);
        gl.glVertex3f(-1f, 0.8f, 0.5f);
        gl.glVertex3f(-1f, 0.5f, 0.5f);
        gl.glVertex3f(-1f, 0.5f, 0.9f);
        gl.glEnd();
        gl.glFlush();

        rotation -= 0.15f;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void init(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);

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
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public static void main(String[] args) {

        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        final GLCanvas gc = new GLCanvas(cap);
        JCuboid cuboid = new JCuboid();

        gc.addGLEventListener(cuboid);
        gc.setSize(100, 100);

        final JFrame frame = new JFrame(" JOGL Cuboid");
        frame.add(gc);
        frame.setSize(500, 400);
        frame.setVisible(true);
        final FPSAnimator animator = new FPSAnimator(gc, 300, true);

        animator.start();
    }
}
