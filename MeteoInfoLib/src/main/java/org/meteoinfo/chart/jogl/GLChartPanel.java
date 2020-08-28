/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;

import org.meteoinfo.chart.IChartPanel;
import org.meteoinfo.chart.MouseMode;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.image.ImageUtil;
import org.w3c.dom.Element;

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
     */
    public GLChartPanel() {
        super();
        this.setDoubleBuffered(true);
    }

    /**
     * Constructor
     *
     * @param pltGL Plot3DGL
     */
    public GLChartPanel(Plot3DGL pltGL) {
        this();

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

    /**
     * Factory
     * @param doubleBuffered Double buffered
     * @param sampleBuffers Sample buffers
     * @param numSamples Number samples
     * @param pltGL Plot3DGL
     * @return GLChartPanel
     */
    public static GLChartPanel factor(boolean doubleBuffered, boolean sampleBuffers,
                                      int numSamples, Plot3DGL pltGL) {
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(profile);
        cap.setDoubleBuffered(doubleBuffered);
        cap.setSampleBuffers(sampleBuffers);
        cap.setNumSamples(numSamples);

        return new GLChartPanel(cap, pltGL);
    }

    /**
     * Factory
     * @param pltGL Plot3DGL
     * @return GLChartPanel
     */
    public static GLChartPanel factory(Plot3DGL pltGL) {
        return factor(true, true, 4, pltGL);
    }

    /**
     * Factory
     * @return GLChartPanel
     */
    public static GLChartPanel factory() {
        return factor(true, true, 4, new Plot3DGL());
    }

    private void init(Plot3DGL pltGL) {
        this.plot3DGL = pltGL;
        this.addGLEventListener(pltGL);

        this.setMouseMode(MouseMode.ROTATE);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onComponentResized(e);
            }
        });

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
                onMouseWheelMoved(e);
            }
        });
    }

    // </editor-fold>
    // <editor-fold desc="Get set methods">
    /**
     * Get plot
     *
     * @return Plot
     */
    public Plot3DGL getPlot() {
        return this.plot3DGL;
    }

    /**
     * Set plot
     *
     * @param plot Plot
     */
    public void setPlot(Plot3DGL plot) {
        init(plot);
    }

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
    void onComponentResized(ComponentEvent e) {
        //this.repaint();
    }

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
            case PAN:
                Dimension size = e.getComponent().getSize();
                float dx = (float) (x - this.mouseLastPos.x) / size.width;
                float dy = (float) (this.mouseLastPos.y - y) / size.height;
                Extent3D extent = this.plot3DGL.getExtent();
                float rotation = this.plot3DGL.getAngleY();
                if (rotation <90 || rotation > 270) {
                    dx = -dx;
                    dy = -dy;
                }
                extent = extent.shift(extent.getWidth() * dx, extent.getHeight() * dy, 0);
                this.plot3DGL.setExtent(extent);
                this.repaint();
                break;
            case ROTATE:
                if (e.isShiftDown()) {
                    size = e.getComponent().getSize();
                    dx = (float) (x - this.mouseLastPos.x) / size.width;
                    dy = (float) (this.mouseLastPos.y - y) / size.height;
                    extent = this.plot3DGL.getExtent();
                    rotation = this.plot3DGL.getAngleY();
                    if (rotation <90 || rotation > 270) {
                        dx = -dx;
                        dy = -dy;
                    }
                    extent = extent.shift(extent.getWidth() * dx, extent.getHeight() * dy, 0);
                    this.plot3DGL.setExtent(extent);
                } else {
                    size = e.getComponent().getSize();

                    float thetaY = 360.0f * ((float) (x - this.mouseLastPos.x) / size.width);
                    float thetaX = 360.0f * ((float) (this.mouseLastPos.y - y) / size.height);

                    float elevation = this.plot3DGL.getAngleX() - thetaX;
                    if (elevation > 0) {
                        elevation = 0;
                    }
                    if (elevation < -180) {
                        elevation = -180;
                    }
                    this.plot3DGL.setAngleX(elevation);

                    rotation = this.plot3DGL.getAngleY() + thetaY;
                    if (rotation >= 360) {
                        rotation -= 360;
                    }
                    if (rotation < 0) {
                        rotation += 360;
                    }
                    this.plot3DGL.setAngleY(rotation);
                }
                this.repaint();
                break;
        }
        mouseLastPos.x = x;
        mouseLastPos.y = y;
    }

    void onMouseWheelMoved(MouseWheelEvent e) {
        Extent3D extent = this.plot3DGL.getExtent();
        //float zoomF = 1 + e.getWheelRotation() / 10.0f;
        float zoomF = e.getWheelRotation() / 10.0f;
        double dx = extent.getWidth() * zoomF;
        double dy = extent.getHeight() * zoomF;
        extent = extent.extend(dx, dy, 0);
        this.plot3DGL.setExtent(extent);
        /*this.plot3DGL.setScaleX(zoomF);
        this.plot3DGL.setScaleY(zoomF);
        this.plot3DGL.setScaleZ(zoomF);*/
        this.repaint();
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get GL2
     *
     * @return
     */
    public GL2 getGL2() {
        return this.getGL().getGL2();
    }
    
    /**
     * Paint view image
     *
     * @return View image
     */
    public BufferedImage paintViewImage() {
        this.plot3DGL.setDoScreenShot(true);
        this.display();
        return this.plot3DGL.getScreenImage();
    }

    /**
     * Paint view image
     *
     * @param width Image width
     * @param height Image height
     * @return View image
     */
    public BufferedImage paintViewImage(int width, int height) {
        final GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setHardwareAccelerated(true);
        caps.setDoubleBuffered(false);
        caps.setAlphaBits(8);
        caps.setRedBits(8);
        caps.setBlueBits(8);
        caps.setGreenBits(8);
        caps.setOnscreen(false);
        GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
        GLOffscreenAutoDrawable drawable = factory.createOffscreenAutoDrawable(null, caps, null,
                width, height);
        drawable.addGLEventListener(this.plot3DGL);
        this.plot3DGL.setDoScreenShot(true);
        drawable.display();

        BufferedImage image = this.plot3DGL.getScreenImage();
        drawable.destroy();

        return image;
    }

    /**
     * Paint view image
     *
     * @param width Image width
     * @param height Image height
     * @param dpi Image dpi
     * @return View image
     */
    public BufferedImage paintViewImage(int width, int height, int dpi) {
        double scaleFactor = dpi / 72.0;
        width = (int)(width * scaleFactor);
        height = (int)(height * scaleFactor);
        this.plot3DGL.setScale((float)scaleFactor);
        BufferedImage image = paintViewImage(width, height);
        this.plot3DGL.setScale(1);
        return image;
    }

    @Override
    public void saveImage(String fn) {
        try {
            saveImage(fn, null);
        } catch (InterruptedException ex) {
            Logger.getLogger(GLChartPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save image file
     * @param fn File path
     * @param sleep Sleep seconds
     * @throws InterruptedException
     */
    public void saveImage(String fn, Integer sleep) throws InterruptedException {
        BufferedImage image = this.paintViewImage();
        if (image != null) {
            String extension = fn.substring(fn.lastIndexOf('.') + 1);
            try {
                ImageIO.write(image, extension, new File(fn));
            } catch (IOException ex) {
                Logger.getLogger(GLChartPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Save image file
     * @param fn File path
     * @param width Image width
     * @param height Image height
     * @param sleep Sleep seconds
     * @throws InterruptedException
     */
    public void saveImage(String fn, int width, int height, Integer sleep) throws InterruptedException {
        BufferedImage image = this.paintViewImage(width, height);
        if (image != null) {
            String extension = fn.substring(fn.lastIndexOf('.') + 1);
            try {
                ImageIO.write(image, extension, new File(fn));
            } catch (IOException ex) {
                Logger.getLogger(GLChartPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.plot3DGL.reshape(this, 0, 0, this.getWidth(), this.getHeight());
    }

    /**
     * Save image file
     * @param fn File path
     * @param dpi Image dpi
     * @param width Image width
     * @param height Image height
     * @param sleep Sleep seconds
     * @throws InterruptedException
     */
    public void saveImage(String fn, int dpi, Integer sleep) throws InterruptedException, IOException {
        saveImage(fn, dpi, this.getWidth(), this.getHeight(), sleep);
    }

    /**
     * Save image file
     * @param fn File path
     * @param dpi Image dpi
     * @param width Image width
     * @param height Image height
     * @param sleep Sleep seconds
     * @throws InterruptedException
     */
    public void saveImage(String fn, int dpi, int width, int height, Integer sleep) throws InterruptedException, IOException {
        String formatName = fn.substring(fn.lastIndexOf('.') + 1);
        if (formatName.equals("jpg")) {
            formatName = "jpeg";
            saveImage_Jpeg(fn, width, height, dpi);
            return;
        }

        BufferedImage image = this.paintViewImage(width, height, dpi);

        if (image != null) {
            File output = new File(fn);
            output.delete();
            for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
                ImageWriter writer = iw.next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
                IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
                if (metadata == null) {
                    metadata = writer.getDefaultImageMetadata(typeSpecifier, null);
                }
                if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                    continue;
                }

                ImageUtil.setDPI(metadata, dpi);

                if (sleep != null) {
                    Thread.sleep(sleep * 1000);
                }
                final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
                try {
                    writer.setOutput(stream);
                    writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
                } finally {
                    stream.close();
                }
                break;
            }
        }

        this.plot3DGL.reshape(this, 0, 0, this.getWidth(), this.getHeight());
    }

    private void saveImage_Jpeg(String file, int width, int height, int dpi) {
        BufferedImage bufferedImage = this.paintViewImage(width, height, dpi);

        if (bufferedImage != null) {
            try {
                // Image writer
                ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(new File(file));
                imageWriter.setOutput(ios);

                // Compression
                JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
                jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
                jpegParams.setCompressionQuality(0.85f);

                // Metadata (dpi)
                IIOMetadata data = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(bufferedImage), jpegParams);
                Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
                Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
                jfif.setAttribute("Xdensity", Integer.toString(dpi));
                jfif.setAttribute("Ydensity", Integer.toString(dpi));
                jfif.setAttribute("resUnits", "1"); // density is dots per inch
                data.setFromTree("javax_imageio_jpeg_image_1.0", tree);

                // Write and clean up
                imageWriter.write(null, new IIOImage(bufferedImage, null, data), jpegParams);
                ios.close();
                imageWriter.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.plot3DGL.reshape(this, 0, 0, this.getWidth(), this.getHeight());
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
        this.repaint();
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
        if (animator != null) {
            animator.stop();
        }
    }
    // </editor-fold>
}
