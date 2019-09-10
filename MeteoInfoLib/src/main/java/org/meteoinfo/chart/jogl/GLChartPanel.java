/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPopupMenu;
import org.meteoinfo.chart.IChartPanel;
import org.meteoinfo.chart.MouseMode;

/**
 *
 * @author Yaqiang Wang
 */
public class GLChartPanel extends GLJPanel implements IChartPanel {
    // <editor-fold desc="Variables">
    private Plot3DGL plot3DGL;
    private final Point mouseDownPoint = new Point(0, 0);
    private Point mouseLastPos = new Point(0, 0);
    private boolean dragMode = false;
    private JPopupMenu popupMenu;
    private MouseMode mouseMode;
    private float distanceX = 0.0f;
    private float distanceY = 0.0f;
    private FPSAnimator animator;
    // </editor-fold>
    // <editor-fold desc="Construction">
    /**
     * Constructor
     * @param pltGL Plot3DGL
     */
    public GLChartPanel(Plot3DGL pltGL) {
        super();
        
        init(pltGL);
    }
    
    /**
     * Constructor
     *
     * @param cap GLCapabilities
     * @param pltGL Plot3DGL
     */
    public GLChartPanel(GLCapabilities cap, Plot3DGL pltGL) {
        super(cap);

        init(pltGL);
    }
    
    private void init(Plot3DGL pltGL) {
        this.plot3DGL = pltGL;
        this.addGLEventListener(pltGL);
        
        this.setMouseMode(MouseMode.ROTATE);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //onMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //onMouseReleased(e);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                //onMouseMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }
        });
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                //onMouseWheelMoved(e);
            }
        });
    }
    // </editor-fold>
    // <editor-fold desc="Get set methods">
    /**
     * Get mouse mode
     *
     * @return Mouse mode
     */
    public MouseMode getMouseMode() {
        return this.mouseMode;
    }

    /**
     * Set mouse mode
     *
     * @param value Mouse mode
     */
    @Override
    public final void setMouseMode(MouseMode value) {
        this.mouseMode = value;
        Image image;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Cursor customCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        switch (this.mouseMode) {
            case SELECT:
                customCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                break;
            case ZOOM_IN:
                image = toolkit.getImage(this.getClass().getResource("/images/zoom_in_32x32x32.png"));
                customCursor = toolkit.createCustomCursor(image, new Point(8, 8), "Zoom In");
                break;
            case ZOOM_OUT:
                image = toolkit.getImage(this.getClass().getResource("/images/zoom_out_32x32x32.png"));
                customCursor = toolkit.createCustomCursor(image, new Point(8, 8), "Zoom In");
                break;
            case PAN:
                image = toolkit.getImage(this.getClass().getResource("/images/Pan_Open_32x32x32.png"));
                customCursor = toolkit.createCustomCursor(image, new Point(8, 8), "Pan");
                break;
            case IDENTIFER:
                image = toolkit.getImage(this.getClass().getResource("/images/identifer_32x32x32.png"));
                customCursor = toolkit.createCustomCursor(image, new Point(8, 8), "Identifer");
                break;
            case ROTATE:
                image = toolkit.getImage(this.getClass().getResource("/images/rotate.png"));
                customCursor = toolkit.createCustomCursor(image, new Point(8, 8), "Identifer");
                break;
        }
        this.setCursor(customCursor);
    }
    // </editor-fold>
    // <editor-fold desc="Events">
    void onMousePressed(MouseEvent e) {
        mouseDownPoint.x = e.getX();
        mouseDownPoint.y = e.getY();
        mouseLastPos = (Point) mouseDownPoint.clone();
    }

    void onMouseDragged(MouseEvent e) {
        this.dragMode = true;
        int x = e.getX();
        int y = e.getY();
        switch (this.mouseMode) {
            case ZOOM_IN:
            case SELECT:
                this.repaint();
                break;
            case ROTATE:
                if (e.isShiftDown()) {
                    float diffX = (float) (x - this.mouseLastPos.x) / 10.0f;
                    float diffY = (float) (this.mouseLastPos.y - y) / 10.0f;

                    distanceX += diffX;
                    distanceY += diffY;
                } else {
                    Dimension size = e.getComponent().getSize();

                    float thetaY = 360.0f * ((float) (x - this.mouseLastPos.x) / size.width);
                    float thetaX = 360.0f * ((float) (this.mouseLastPos.y - y) / size.height);

                    this.plot3DGL.setAngleX(this.plot3DGL.getAngleX() - thetaX);
                    this.plot3DGL.setAngleY(this.plot3DGL.getAngleY() + thetaY);
                }
                
                break;
        }
        mouseLastPos.x = x;
        mouseLastPos.y = y;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public void saveImage(String fn) {
        
    }
    
    /**
     * Zoom back to full extent
     */
    @Override
    public void onUndoZoomClick() {
        
    }
    
    /**
     * Paint graphics
     */
    @Override
    public void paintGraphics() {
        
    }
    
    /**
     * Start animator
     */
    public void animator_start() {
        animator = new FPSAnimator(this, 300, true);
        animator.start();
    }
    
    /**
     * Start animator
     */
    public void animator_stop() {
        animator.stop();
    }
    // </editor-fold>
}
