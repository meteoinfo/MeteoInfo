package org.meteoinfo.chart;

import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class SwingGLCanvasHiDPI {

    public static void main(String[] args) {
        // 开启 JOGL 全局 HiDPI 支持（配合 setSurfaceScale 使用效果更稳）
        System.setProperty("nativewindow.ws.HiDPI", "true");

        // 获取主屏幕缩放比
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        AffineTransform at = gd.getDefaultConfiguration().getDefaultTransform();
        float scaleX = (float) at.getScaleX();
        float scaleY = (float) at.getScaleY();

        // 创建 OpenGL 画布
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);

        // ★ 关键：设置表面缩放，让渲染目标使用物理像素
        ((ScalableSurface) canvas).setSurfaceScale(new float[]{scaleX, scaleY});

        // 添加渲染监听器
        canvas.addGLEventListener(new MyGLEventListener());

        // 构建界面（在 EDT 线程中）
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Swing + GLCanvas HiDPI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // GLCanvas 作为重量组件可以直接添加到 JFrame
            frame.add(canvas, BorderLayout.CENTER);
            frame.setSize(800, 600);   // 逻辑分辨率
            frame.setVisible(true);

            // 启动渲染循环（每秒 60 帧）
            new com.jogamp.opengl.util.FPSAnimator(canvas, 60).start();
        });
    }

    static class MyGLEventListener implements GLEventListener {
        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            GL2 gl = drawable.getGL().getGL2();
            // 必须使用物理像素尺寸设置视口
            int pw = drawable.getSurfaceWidth();
            int ph = drawable.getSurfaceHeight();
            gl.glViewport(0, 0, pw, ph);

            // 根据物理宽高调整投影矩阵（避免画面拉伸）
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrtho(0, pw, 0, ph, -1, 1);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            // 以物理坐标绘制，保证清晰度
            gl.glColor3f(1, 0, 0);
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glVertex2i(100, 100);
            gl.glVertex2i(500, 100);
            gl.glVertex2i(300, 500);
            gl.glEnd();
        }

        @Override public void init(GLAutoDrawable drawable) {}
        @Override public void dispose(GLAutoDrawable drawable) {}
    }
}
