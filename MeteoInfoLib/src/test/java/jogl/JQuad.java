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
import javax.swing.JFrame;

public class JQuad implements GLEventListener {

    @Override
    public void init(GLAutoDrawable arg0) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();

        gl.glBegin(GL2.GL_QUADS);

        gl.glVertex3f(0.0f, 0.5f, 0);
        gl.glVertex3f(-0.5f, 0f, 0);
        gl.glVertex3f(0f, -0.5f, 0);
        gl.glVertex3f(0.5f, 0f, 0);

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

        final GLCanvas gc = new GLCanvas(cap);
        JQuad jq = new JQuad();
        gc.addGLEventListener(jq);
        gc.setSize(400, 400);

        final JFrame frame = new JFrame("JOGL Primitive Quadrilateral");
        frame.add(gc);
        frame.setSize(500, 400);
        frame.setVisible(true);
    }
}
