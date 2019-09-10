/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogl;

import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

public class CubeSample2 implements GLEventListener, MouseListener, KeyListener {
	private static final char KEY_ESC = 0x1b;

	private final float[][] vertex = {
		{ 0.0f, 0.0f, 0.0f}, /* A */
		{ 1.0f, 0.0f, 0.0f}, /* B */
		{ 1.0f, 1.0f, 0.0f}, /* C */
		{ 0.0f, 1.0f, 0.0f}, /* D */
		{ 0.0f, 0.0f, 1.0f}, /* E */
		{ 1.0f, 0.0f, 1.0f}, /* F */
		{ 1.0f, 1.0f, 1.0f}, /* G */
		{ 0.0f, 1.0f, 1.0f} /* H */
	};

	private final int[][] edge = {
		{ 0, 1}, /* ア (A-B) */
		{ 1, 2}, /* イ (B-C) */
		{ 2, 3}, /* ウ (C-D) */
		{ 3, 0}, /* エ (D-A) */
		{ 4, 5}, /* オ (E-) */
		{ 5, 6}, /* カ (-G) */
		{ 6, 7}, /* キ (G-H) */
		{ 7, 4}, /* ク (H-E) */
		{ 0, 4}, /* ケ (A-E) */
		{ 1, 5}, /* コ (B-) */
		{ 2, 6}, /* サ (C-G) */
		{ 3, 7} /* シ (D-H) */
	};

	private final GLU glu;
	private final FPSAnimator animator;
	private final GLWindow glWindow;
	private boolean willAnimatorPause = false;

	public static void main(String[] args){
		new CubeSample2();
	}

	public CubeSample2() {
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		glu = new GLU();

		glWindow = GLWindow.create(caps);
		glWindow.setTitle("Cube demo (Newt)");
		glWindow.setSize(300, 300);
		glWindow.addGLEventListener(this);

		glWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDestroyed(WindowEvent arg0) {
				System.exit(0);
			}
		});
		glWindow.addMouseListener(this);
		glWindow.addKeyListener(this);
		//animator = new FPSAnimator(30);
		animator = new FPSAnimator(glWindow, 30, false);
		animator.add(glWindow);
		animator.start();
		animator.pause();
		glWindow.setVisible(true);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
//		animator = new FPSAnimator(drawable, 30, true);
//		animator.add(glWindow);
//		animator.start();
//		animator.pause();

		GL2 gl = drawable.getGL().getGL2();
		//背景を白く塗りつぶす.
		gl.glClearColor(1f, 1f, 1f, 1.0f);

		System.out.println("auto swap:" + drawable.getAutoSwapBufferMode());
		drawable.setAutoSwapBufferMode(false);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
//		gl.glViewport(0f, 0f, width, height); //(3) Jogl内部で実行済みなので,不要.(APIDOCに書いてある)
//		gl.glOrtho(-2.0, 2.0, -2.0, 2.0, -2.0, 2.0);
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(30.0, (double)width / (double)height, 1.0, 300.0);
//		gl.glTranslatef(0.0f, 0.0f, -5.0f);
		glu.gluLookAt(3.0f, 4.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
//		System.out.printf("x:%d, y:%d, w:%d, h:%d, %n", x, y, width, height);
		//これによりウィンドウをリサイズしても中の図形は大きさが維持される.
		//また,第3,第4引数を入れ替えることによりGLWindowの座標系(左上隅が原点)とデバイス座標系(左下隅が原点)の違いを吸収している.
//		gl.glOrthof(x, x + width, y + height, y, -1.0f, 1.0f); //(4)
//		gl.glOrthof(x/300f, (x + width)/300f, (y + height)/300f, y/300f, -1.0f, 1.0f); //(4)
//		gl.glOrthof(x/400f, (x + width)/400f, (y + height)/400f, y/400f, -1.0f, 1.0f); //(4)
//		gl.glOrthof(x/400f, (x + width)/400f, (y + height)/400f, y/400f, -1.0f, 1.0f); //(4)
//		gl.glOrthof(x/300f, (x/300) + width, (y/300) + height, y/300f, -1.0f, 1.0f); //(4)
//		gl.glOrthof(x , x + width/300f, y + height/300f, y, -1.0f, 1.0f); //(4)

		gl.glMatrixMode(GL_MODELVIEW);
//		gl.glLoadIdentity();
	}

	//回転角
	float r = 0;

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		gl.glLoadIdentity();

		// 視点位置と視線方向
//		glu.gluLookAt(3.0, 4.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

		// 図形の回転
		gl.glTranslatef(0.5f, 0.5f, 0.5f);
		gl.glRotatef(r, 0.0f, 1.0f, 0.0f);
		gl.glTranslatef(-0.5f, -0.5f, -0.5f);
		// 図形の描画
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		gl.glBegin(GL_LINES);
		for (int i = 0; i < 12; i++) {
			gl.glVertex3fv(vertex[edge[i][0]], 0);
			gl.glVertex3fv(vertex[edge[i][1]], 0);
		}
		gl.glEnd();

		//一周回ったら回転角を 0 に戻す
		if (r++ >= 360.0f) r = 0;
		System.out.println("anim:" + animator.isAnimating() + ", r:" + r);
		if(willAnimatorPause) {
			animator.pause();
			System.out.println("animoator paused:");
			willAnimatorPause = false;
		}
		drawable.swapBuffers();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		char keyChar = e.getKeyChar();
		if(keyChar == KEY_ESC || keyChar == 'q' || keyChar == 'Q') { //KeyEvent.VK_Qは大文字。小文字?
			glWindow.destroy();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		switch(e.getButton()) {
		case MouseEvent.BUTTON1:
			animator.resume();
			System.out.println("button 1, left click");
			break;
		case MouseEvent.BUTTON2:
			System.out.println("button 2");
			break;
		case MouseEvent.BUTTON3:
			System.out.println("button 3, right click");
			willAnimatorPause  = true;
			animator.resume();
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		animator.pause();
	}

	@Override
	public void mouseMoved(MouseEvent e) { }

	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseWheelMoved(MouseEvent e) {}

}
