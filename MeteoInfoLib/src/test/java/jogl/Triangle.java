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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class Triangle implements GLEventListener {

    @Override
    public void init(GLAutoDrawable arg0) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();

        //drawing the base
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glVertex3f(-0.50f, -0.50f, 0);
        gl.glVertex3f(0.50f, -0.50f, 0);
        gl.glVertex3f(0f, 0.50f, 0);
        gl.glVertex3f(-0.50f, -0.50f, 0);
        gl.glEnd();
        gl.glFlush();
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
        Triangle tr = new Triangle();
        gc.addGLEventListener(tr);
        gc.setSize(400, 400);

        final JFrame frame = new JFrame("JOGL Triangle");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowevent) {
                frame.dispose();
                System.exit(0);
            }
        });
        frame.add(gc);
        frame.setSize(500, 400);
        frame.setVisible(true);
    }
}
