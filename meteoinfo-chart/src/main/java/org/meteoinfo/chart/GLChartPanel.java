/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageWriteException;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.meteoinfo.chart.jogl.*;
import org.meteoinfo.chart.plot.*;
import org.meteoinfo.chart.plot3d.Projector;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.GenericFileFilter;
import org.meteoinfo.common.PointF;
import org.meteoinfo.data.mapdata.webmap.TileLoadListener;
import org.meteoinfo.geo.layer.LayerTypes;
import org.meteoinfo.geo.layer.MapLayer;
import org.meteoinfo.geo.layer.RasterLayer;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geo.mapview.FrmIdentifer;
import org.meteoinfo.geo.mapview.FrmIdentiferGrid;
import org.meteoinfo.geo.mapview.MapView;
import org.meteoinfo.image.ImageUtil;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.table.Field;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yaqiang
 */
public class GLChartPanel extends GLJPanel implements IChartPanel{

    // <editor-fold desc="Variables">
    private final EventListenerList listeners = new EventListenerList();
    private BufferedImage mapBitmap = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage tempImage = null;
    private boolean newPaint = false;
    private boolean doubleBuffer = true;
    private GLChart chart;
    private Plot currentPlot;
    private Dimension chartSize;
    private Point mouseDownPoint = new Point(0, 0);
    private Point mouseLastPos = new Point(0, 0);
    private boolean dragMode = false;
    private JPopupMenu popupMenu;
    private MouseMode mouseMode;
    private List<int[]> selectedPoints;
    private int xShift = 0;
    private int yShift = 0;
    private double paintScale = 1.0;
    private LocalDateTime lastMouseWheelTime;
    private Timer mouseWheelDetctionTimer;
    private boolean loading = false;
    private boolean zoomXY = false;
    // </editor-fold>

    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public GLChartPanel() {
        this(new GLChart());
    }

    /**
     * Constructor
     *
     * @param chart Chart
     */
    public GLChartPanel(GLChart chart) {
        super(createCapabilities(true, true, 4));

        //this.setBackground(Color.white);
        this.setBackground(Color.lightGray);
        this.setSize(200, 200);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onComponentResized(e);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onMouseReleased(e);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
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

        this.mouseWheelDetctionTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LocalDateTime now = LocalDateTime.now();
                if (Duration.between(lastMouseWheelTime, now).toMillis() > 200) {
                    xShift = 0;
                    yShift = 0;
                    paintScale = 1.0;
                    //paintGraphics();
                    repaintNew();
                    mouseWheelDetctionTimer.stop();
                }
            }
        });

        popupMenu = new JPopupMenu();
        JMenuItem undoZoom = new JMenuItem("Undo zoom");
        undoZoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onUndoZoomClick();
            }
        });
        popupMenu.add(undoZoom);
        popupMenu.addSeparator();

        JMenuItem saveFigure = new JMenuItem("Save figure");
        saveFigure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSaveFigureClick(e);
            }
        });
        popupMenu.add(saveFigure);

        this.mouseMode = MouseMode.DEFAULT;
        //this.setMouseMode(mouseMode.ZOOM_IN);

        this.chart = chart;
        if (this.chart != null) {
            this.chart.setParent(this);
            this.addGLEventListener(chart);
        }
    }

    /**
     * Constructor
     *
     * @param chart Chart
     * @param width Chart width
     * @param height Chart height
     */
    public GLChartPanel(GLChart chart, int width, int height) {
        this(chart);
        this.chartSize = new Dimension(width, height);
        this.setPreferredSize(chartSize);
    }

    /**
     * Create GLCapabilities
     * @param doubleBuffered Double buffered
     * @param sampleBuffers Sample buffers
     * @param numSamples Number samples
     * @return GLCapabilities
     */
    public static GLCapabilities createCapabilities(boolean doubleBuffered, boolean sampleBuffers, int numSamples) {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(profile);
        cap.setDoubleBuffered(doubleBuffered);
        cap.setSampleBuffers(sampleBuffers);
        cap.setNumSamples(numSamples);

        return cap;
    }

    // </editor-fold>
    // <editor-fold desc="Get set methods">

    /**
     * Get whether chart panel is loading
     * @return Is loading or not
     */
    public boolean isLoading() {
        return this.loading;
    }

    /**
     * Set whether chart panel is loading
     * @param loading Boolean
     */
    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    /**
     * Get chart
     *
     * @return Chart
     */
    public GLChart getChart() {
        return chart;
    }

    /**
     * Set chart
     *
     * @param value
     */
    public void setChart(GLChart value) {
        chart = value;
        if (this.chart != null) {
            chart.setParent(this);
        }
    }

    /**
     * Get if using off-screen image double buffering.
     * Using double buffering will be faster but lower view quality in
     * high dpi screen computer.
     *
     * @return Boolean
     */
    public boolean isDoubleBuffer() {
        return this.doubleBuffer;
    }

    /**
     * Set using off-screen image double buffering or not.
     * @param value Boolean
     */
    public void setDoubleBuffer(boolean value) {
        this.doubleBuffer = value;
    }

    /**
     * Get popup menu
     *
     * @return Popup menu
     */
    public JPopupMenu getPopupMenu() {
        return this.popupMenu;
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
    public void setMouseMode(MouseMode value) {
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
            case IDENTIFIER:
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

    /**
     * Get selected chart points
     *
     * @return Selected chart points
     */
    public List<int[]> getSelectedPoints() {
        return this.selectedPoints;
    }

    /**
     * Get whether only zoom XY
     *
     * @return Whether only zoom XY
     */
    public boolean isZoomXY() {
        return this.zoomXY;
    }

    /**
     * Set whether only zoom XY
     *
     * @param value Whether only zoom XY
     */
    public void setZoomXY(boolean value) {
        this.zoomXY = value;
    }
    // </editor-fold>

    // <editor-fold desc="Events">
    public void addPointSelectedListener(IPointSelectedListener listener) {
        this.listeners.add(IPointSelectedListener.class, listener);
    }

    public void removePointSelectedListener(IPointSelectedListener listener) {
        this.listeners.remove(IPointSelectedListener.class, listener);
    }

    public void firePointSelectedEvent() {
        firePointSelectedEvent(new PointSelectedEvent(this));
    }

    private void firePointSelectedEvent(PointSelectedEvent event) {
        Object[] listeners = this.listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IPointSelectedListener.class) {
                ((IPointSelectedListener) listeners[i + 1]).pointSelectedEvent(event);
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Method">
    /**
     * Get figure width
     *
     * @return Figure width
     */
    public int getFigureWidth() {
        int width;
        if (this.chartSize != null) {
            width = this.chartSize.width;
        } else {
            width = this.getWidth();
        }

        return width;
    }

    /**
     * Get Figure height
     *
     * @return Figure height
     */
    public int getFigureHeight() {
        int height;
        if (this.chartSize != null) {
            height = this.chartSize.height;
        } else {
            height = this.getHeight();
        }

        return height;
    }

    /**
     * Select a plot by point
     *
     * @param x X
     * @param y Y
     * @return Selected plot
     */
    public Plot selPlot(int x, int y) {
        if (this.chart == null) {
            return null;
        }

        int n = this.chart.getPlots().size();
        for (int i = n - 1; i >= 0; i--) {
            Plot plot = this.chart.getPlots().get(i);
            Rectangle2D rect;
            if (plot instanceof GLPlot) {
                //rect = plot.getPositionArea(new Rectangle2D.Double(0, 0, this.chart.width, this.chart.height));
                rect = plot.getPositionArea(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
            } else {
                rect = plot.getGraphArea();
            }
            if (rect.contains(x, y)) {
                return plot;
            }
        }
        return null;
    }

    /**
     * Paint component
     *
     * @param g Graphics
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (this.getWidth() < 5 || this.getHeight() < 5) {
            return;
        }

        //this.setBackground(Color.white);
        Graphics2D g2 = (Graphics2D) g;

        if (this.newPaint) {
            this.paintGraphics(g2);
        } else {
            AffineTransform mx = new AffineTransform();
            AffineTransformOp aop = new AffineTransformOp(mx, AffineTransformOp.TYPE_BICUBIC);
            g2.drawImage(mapBitmap, aop, 0, 0);
        }

        //Draw dynamic graphics
        if (this.dragMode) {
            switch (this.mouseMode) {
                case ZOOM_IN:
                case SELECT:
                    int aWidth = Math.abs(mouseLastPos.x - mouseDownPoint.x);
                    int aHeight = Math.abs(mouseLastPos.y - mouseDownPoint.y);
                    int aX = Math.min(mouseLastPos.x, mouseDownPoint.x);
                    int aY = Math.min(mouseLastPos.y, mouseDownPoint.y);
                    g2.setColor(this.getForeground());
                    float dash1[] = {2.0f};
                    g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
                    g2.draw(new Rectangle(aX, aY, aWidth, aHeight));
                    break;
            }
        }

        //Draw identifer shape
        if (this.currentPlot != null) {
            if (this.currentPlot instanceof MapPlot) {
                MapPlot plot = (MapPlot) this.currentPlot;
                if (plot.getMapView().isDrawIdentiferShape()) {
                    if (plot.getSelectedLayer() != null) {
                        if (plot.getSelectedLayer().getLayerType() == LayerTypes.VECTOR_LAYER) {
                            VectorLayer layer = (VectorLayer) plot.getSelectedLayer();
                            Rectangle2D rect = plot.getGraphArea();
                            Rectangle rr = new Rectangle((int) rect.getX(), (int) rect.getY(),
                                    (int) rect.getWidth(), (int) rect.getHeight());
                            plot.getMapView().drawIdShape(g2, layer.getShapes().get(layer.getIdentiferShape()), rr);
                        }
                    }
                }
            }
        }

        g2.dispose();
    }

    /**
     * New paint
     */
    public void repaintNew() {
        if (this.doubleBuffer) {
            this.paintGraphics();
        } else {
            this.newPaint = true;
            this.repaint();
            this.updateViewImage();
        }
    }

    private void repaintOld() {
        if (this.doubleBuffer) {
            this.repaint();
        } else {
            this.newPaint = false;
            this.repaint();
        }
    }

    private void updateViewImage() {
        if (this.getWidth() < 5 || this.getHeight() < 5) {
            return;
        }

        int width, height;
        if (this.chartSize != null) {
            height = this.chartSize.height;
            width = this.chartSize.width;
        } else {
            width = this.getWidth();
            height = this.getHeight();
        }

        this.mapBitmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = this.mapBitmap.createGraphics();
        this.print(g);
        g.dispose();
    }

    /**
     * Paint graphics
     */
    @Override
    public void paintGraphics() {
        if (this.getWidth() < 5 || this.getHeight() < 5) {
            return;
        }

        int width, height;
        if (this.chartSize != null) {
            height = this.chartSize.height;
            width = this.chartSize.width;
        } else {
            width = this.getWidth();
            height = this.getHeight();
        }

        this.mapBitmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        if (this.chart != null) {
            Graphics2D g = this.mapBitmap.createGraphics();
            Rectangle2D chartArea;
            if (this.chartSize == null) {
                chartArea = new Rectangle2D.Double(0.0, 0.0, this.mapBitmap.getWidth(), this.mapBitmap.getHeight());
            } else {
                chartArea = new Rectangle2D.Double(0.0, 0.0, this.chartSize.width, this.chartSize.height);
            }
            this.chart.draw(g, chartArea);
        }
        this.repaint();
    }

    public void paintGraphics(Graphics2D g) {
        if (this.chart != null) {
            Rectangle2D chartArea;
            if (this.chartSize == null) {
                chartArea = new Rectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight());
            } else {
                chartArea = new Rectangle2D.Double(0.0, 0.0, this.chartSize.width, this.chartSize.height);
            }
            this.chart.draw(g, chartArea);
        }
    }

    @Override
    public void paintGraphics(Graphics2D g, int width, int height) {
        if (this.chart != null) {
            Rectangle2D chartArea;
            chartArea = new Rectangle2D.Double(0.0, 0.0, width, height);
            this.chart.draw(g, chartArea);
        }
    }

    void onComponentResized(ComponentEvent e) {
        if (!loading) {
            if (this.getWidth() > 0 && this.getHeight() > 0) {
                if (this.chart != null) {
                    //this.paintGraphics();
                    this.chart.setAlwaysUpdateBuffers(true);
                    this.repaintNew();
                }
            }
        }
    }

    void onMousePressed(MouseEvent e) {
        mouseDownPoint.x = e.getX();
        mouseDownPoint.y = e.getY();
        mouseLastPos = (Point) mouseDownPoint.clone();
        switch (this.mouseMode) {
            case PAN:
                Plot plot = selPlot(e.getX(), e.getY());
                if (plot != null) {
                    Rectangle2D mapRect = plot.getGraphArea();
                    tempImage = new BufferedImage((int) mapRect.getWidth() - 2,
                            (int) mapRect.getHeight() - 2, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D tg = tempImage.createGraphics();
                    tg.setColor(Color.white);
                    tg.fill(mapRect);
                    tg.drawImage(this.mapBitmap, -(int) mapRect.getX() - 1, -(int) mapRect.getY() - 1, this);
                    tg.dispose();
                }
                break;
        }
    }

    void onMouseMoved(MouseEvent e) {
        this.dragMode = false;
//        switch (this.mouseMode) {
//            case PAN:
//                Plot plot = selPlot(e.getX(), e.getY());
//                if (plot != null) {
//                    Rectangle2D mapRect = plot.getGraphArea();
//                    tempImage = new BufferedImage((int) mapRect.getWidth() - 2,
//                            (int) mapRect.getHeight() - 2, BufferedImage.TYPE_INT_ARGB);
//                    Graphics2D tg = tempImage.createGraphics();
//                    tg.setColor(Color.white);
//                    tg.fill(mapRect);
//                    tg.drawImage(this.mapBitmap, -(int) mapRect.getX() - 1, -(int) mapRect.getY() - 1, this);
//                    tg.dispose();
//                }
//                break;
//        }
    }

    void onMouseReleased(MouseEvent e) {
        this.dragMode = false;
        Plot plot = selPlot(this.mouseDownPoint.x, this.mouseDownPoint.y);
        this.currentPlot = plot;
        switch (this.mouseMode) {
            case ZOOM_IN:
                if (Math.abs(mouseLastPos.x - mouseDownPoint.x) > 5) {
                    if (plot instanceof AbstractPlot2D) {
                        AbstractPlot2D xyplot = (AbstractPlot2D) plot;
                        if (xyplot instanceof MapPlot) {
                            MapPlot mapPlot = (MapPlot) xyplot;
                            Rectangle2D graphArea = plot.getGraphArea();
                            double[] xy1 = mapPlot.screenToProj(mouseDownPoint.x - graphArea.getX(), mouseDownPoint.y - graphArea.getY(), graphArea);
                            double[] xy2 = mapPlot.screenToProj(mouseLastPos.x - graphArea.getX(), mouseLastPos.y - graphArea.getY(), graphArea);
                            Extent extent = new Extent();
                            extent.minX = Math.min(xy1[0], xy2[0]);
                            extent.maxX = Math.max(xy1[0], xy2[0]);
                            extent.minY = Math.min(xy1[1], xy2[1]);
                            extent.maxY = Math.max(xy1[1], xy2[1]);
                            mapPlot.setDrawExtent(extent);
                            //this.paintGraphics();
                            this.repaintNew();
                        } else {
                            Rectangle2D graphArea = xyplot.getGraphArea();
                            double[] xy1 = xyplot.screenToProj(mouseDownPoint.x - graphArea.getX(), mouseDownPoint.y - graphArea.getY(), graphArea);
                            double[] xy2 = xyplot.screenToProj(mouseLastPos.x - graphArea.getX(), mouseLastPos.y - graphArea.getY(), graphArea);
                            Extent extent = new Extent();
                            extent.minX = Math.min(xy1[0], xy2[0]);
                            extent.maxX = Math.max(xy1[0], xy2[0]);
                            extent.minY = Math.min(xy1[1], xy2[1]);
                            extent.maxY = Math.max(xy1[1], xy2[1]);
                            if (xyplot.getXAxis().isInverse()) {
                                Extent drawExtent = xyplot.getDrawExtent();
                                double minx, maxx;
                                minx = drawExtent.getWidth() - (extent.maxX - drawExtent.minX) + drawExtent.minX;
                                maxx = drawExtent.getWidth() - (extent.minX - drawExtent.minX) + drawExtent.minX;
                                extent.minX = minx;
                                extent.maxX = maxx;
                            }
                            if (xyplot.getYAxis().isInverse()) {
                                Extent drawExtent = xyplot.getDrawExtent();
                                double miny, maxy;
                                miny = drawExtent.getHeight() - (extent.maxY - drawExtent.minY) + drawExtent.minY;
                                maxy = drawExtent.getHeight() - (extent.minY - drawExtent.minY) + drawExtent.minY;
                                extent.minY = miny;
                                extent.maxY = maxy;
                            }
                            xyplot.setDrawExtent(extent);
                            //this.paintGraphics();
                            this.repaintNew();
                        }
                    }
                }
                break;
            case ZOOM_OUT:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (plot instanceof AbstractPlot2D) {
                        AbstractPlot2D xyplot = (AbstractPlot2D) plot;
                        double zoom = 1.5;
                        Extent extent = xyplot.getDrawExtent();
                        double owidth = extent.getWidth();
                        double oheight = extent.getHeight();
                        double width = owidth * zoom;
                        double height = oheight * zoom;
                        double xshift = (owidth - width) * 0.5;
                        double yshift = (oheight - height) * 0.5;
                        extent.minX += xshift;
                        extent.maxX -= xshift;
                        extent.minY += yshift;
                        extent.maxY -= yshift;
                        xyplot.setDrawExtent(extent);
                        //this.paintGraphics();
                        this.repaintNew();
                    }
                }
                break;
            case SELECT:
                if (Math.abs(mouseLastPos.x - mouseDownPoint.x) > 5) {
                    if (plot instanceof AbstractPlot2D) {
                        AbstractPlot2D xyplot = (AbstractPlot2D) plot;
                        if (xyplot instanceof XY1DPlot) {
                            XY1DPlot plt = (XY1DPlot) xyplot;
                            Rectangle2D graphArea = plt.getGraphArea();
                            if (graphArea.contains(mouseDownPoint.x, mouseDownPoint.y) || graphArea.contains(mouseLastPos.x, mouseLastPos.y)) {
                                double[] xy1 = plt.screenToProj(mouseDownPoint.x - graphArea.getX(), mouseDownPoint.y - graphArea.getY(), graphArea);
                                double[] xy2 = plt.screenToProj(mouseLastPos.x - graphArea.getX(), mouseLastPos.y - graphArea.getY(), graphArea);
                                Extent extent = new Extent();
                                extent.minX = Math.min(xy1[0], xy2[0]);
                                extent.maxX = Math.max(xy1[0], xy2[0]);
                                extent.minY = Math.min(xy1[1], xy2[1]);
                                extent.maxY = Math.max(xy1[1], xy2[1]);
                                this.selectedPoints = plt.getDataset().selectPoints(extent);
                                this.firePointSelectedEvent();
                                //this.paintGraphics();
                                this.repaintNew();
                            }
                        }
                    }
                }
                break;
            case PAN:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (plot instanceof AbstractPlot2D) {
                        AbstractPlot2D xyplot = (AbstractPlot2D) plot;
                        double[] xy1 = xyplot.screenToProj(mouseDownPoint.x, mouseDownPoint.y);
                        double[] xy2 = xyplot.screenToProj(e.getX(), e.getY());
                        Extent extent = xyplot.getDrawExtent();
                        extent = extent.shift(xy1[0] - xy2[0], xy1[1] - xy2[1]);
                        xyplot.setDrawExtent(extent);
                        /*int deltaX = e.getX() - mouseDownPoint.x;
                        int deltaY = e.getY() - mouseDownPoint.y;
                        double minX = -deltaX;
                        double minY = -deltaY;
                        double maxX = xyplot.getGraphArea().getWidth() - deltaX;
                        double maxY = xyplot.getGraphArea().getHeight() - deltaY;
                        xyplot.zoomToExtentScreen(minX, maxX, minY, maxY);*/
                        //this.paintGraphics();
                        /*if (xyplot instanceof MapPlot) {
                            ((MapPlot) xyplot).getMapView().setFixMapScale(true);
                        }*/
                        this.repaintNew();
                    }
                }
                break;
        }
    }

    void onMouseDragged(MouseEvent e) {
        this.dragMode = true;
        int x = e.getX();
        int y = e.getY();
        Plot plot = selPlot(this.mouseDownPoint.x, this.mouseDownPoint.y);
        switch (this.mouseMode) {
            case ZOOM_IN:
            case SELECT:
                if (plot instanceof GLPlot) {
                    this.repaint();
                } else {
                    this.repaintOld();
                }
                break;
            case PAN:
                if (plot != null) {
                    if (plot instanceof GLPlot) {
                        GLPlot glPlot = (GLPlot) plot;
                        Dimension size = e.getComponent().getSize();
                        float dx = (float) (x - this.mouseLastPos.x) / size.width;
                        float dy = (float) (this.mouseLastPos.y - y) / size.height;
                        Extent3D extent = glPlot.getDrawExtent();
                        float rotation = glPlot.getAngleY();
                        if (rotation < 90 || rotation > 270) {
                            dx = -dx;
                            dy = -dy;
                        }
                        extent = extent.shift(extent.getWidth() * dx, extent.getHeight() * dy, 0);
                        glPlot.setDrawExtent(extent);
                        this.repaint();
                    } else {
                        Graphics2D g = (Graphics2D) this.getGraphics();
                        Rectangle2D mapRect = plot.getGraphArea();
                        g.setClip(mapRect);
                        g.setColor(Color.white);
                        int aX = e.getX() - mouseDownPoint.x;
                        int aY = e.getY() - mouseDownPoint.y;
                        if (aX > 0) {
                            if (mapRect.getX() >= 0) {
                                g.fillRect((int) mapRect.getX(), (int) mapRect.getY(), aX, (int) mapRect.getHeight());
                            } else {
                                g.fillRect(0, (int) mapRect.getY(), aX, (int) mapRect.getHeight());
                            }
                        } else if (mapRect.getX() <= this.getWidth()) {
                            g.fillRect((int) (mapRect.getX() + mapRect.getWidth() + aX), (int) mapRect.getY(), Math.abs(aX), (int) mapRect.getHeight());
                        } else {
                            g.fillRect(this.getWidth() + aX, (int) mapRect.getY(), Math.abs(aX), (int) mapRect.getHeight());
                        }
                        if (aY > 0) {
                            if (mapRect.getY() >= 0) {
                                g.fillRect((int) mapRect.getX(), (int) mapRect.getY(), (int) mapRect.getWidth(), aY);
                            } else {
                                g.fillRect((int) mapRect.getX(), 0, (int) mapRect.getWidth(), aY);
                            }
                        } else if (mapRect.getY() + mapRect.getHeight() <= this.getX() + this.getHeight()) {
                            g.fillRect((int) mapRect.getX(), (int) mapRect.getY() + (int) mapRect.getHeight() + aY, (int) mapRect.getWidth(), Math.abs(aY));
                        } else {
                            g.fillRect((int) mapRect.getX(), this.getY() + this.getHeight() + aY, (int) mapRect.getWidth(), Math.abs(aY));
                        }
                        int startX = (int) mapRect.getX() + aX;
                        int startY = (int) mapRect.getY() + aY;
                        g.drawImage(tempImage, startX, startY, this);
                        g.setColor(this.getForeground());
                        g.draw(mapRect);
                    }
                }
                break;
            case ROTATE:
                if (plot != null) {
                    if (plot instanceof Plot3D) {
                        Plot3D plot3d = (Plot3D) plot;
                        Projector projector = plot3d.getProjector();
                        float new_value = 0.0f;
                        // if (!thread.isAlive() || !data_available) {
                        if (e.isControlDown()) {
                            projector.set2D_xTranslation(projector.get2D_xTranslation() + (x - this.mouseLastPos.x));
                            projector.set2D_yTranslation(projector.get2D_yTranslation() + (y - this.mouseLastPos.y));
                        } else if (e.isShiftDown()) {
                            new_value = projector.getY2DScaling() + (y - this.mouseLastPos.y) * 0.5f;
                            if (new_value > 60.0f) {
                                new_value = 60.0f;
                            }
                            if (new_value < 2.0f) {
                                new_value = 2.0f;
                            }
                            projector.set2DScaling(new_value);
                        } else {
                            new_value = projector.getRotationAngle() + (x - this.mouseLastPos.x);
                            while (new_value > 360) {
                                new_value -= 360;
                            }
                            while (new_value < 0) {
                                new_value += 360;
                            }
                            projector.setRotationAngle(new_value);
                            new_value = projector.getElevationAngle() + (y - this.mouseLastPos.y);
                            if (new_value > 90) {
                                new_value = 90;
                            } else if (new_value < 0) {
                                new_value = 0;
                            }
                            projector.setElevationAngle(new_value);
                        }
                        this.repaintNew();
                    } else if (plot instanceof GLPlot) {
                        GLPlot glPlot = (GLPlot) plot;
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            if (e.isShiftDown()) {
                                Dimension size = e.getComponent().getSize();
                                float dx = (float) (x - this.mouseLastPos.x) / size.width;
                                float dy = (float) (this.mouseLastPos.y - y) / size.height;
                                Extent3D extent = glPlot.getDrawExtent();
                                float rotation = glPlot.getAngleY();
                                if (rotation < 90 || rotation > 270) {
                                    dx = -dx;
                                    dy = -dy;
                                }
                                extent = extent.shift(extent.getWidth() * dx, extent.getHeight() * dy, 0);
                                glPlot.setDrawExtent(extent);
                            } else {
                                Dimension size = e.getComponent().getSize();

                                float thetaY = 360.0f * ((float) (x - this.mouseLastPos.x) / size.width);
                                float thetaX = 180.0f * ((float) (this.mouseLastPos.y - y) / size.height);

                                if (glPlot instanceof EarthGLPlot) {
                                    float scale = glPlot.getScale();
                                    thetaY /= scale;
                                    thetaX /= scale;
                                }

                                float elevation = glPlot.getAngleX() - thetaX;
                                if (elevation > 0) {
                                    elevation = 0;
                                }
                                if (elevation < -180) {
                                    elevation = -180;
                                }
                                glPlot.setAngleX(elevation);

                                float rotation = glPlot.getAngleY() + thetaY;
                                if (rotation >= 360) {
                                    rotation -= 360;
                                }
                                if (rotation < 0) {
                                    rotation += 360;
                                }
                                glPlot.setAngleY(rotation);
                            }
                            this.repaint();
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            Dimension size = e.getComponent().getSize();
                            float shift = 360.0f * ((float) (this.mouseLastPos.x - x) / size.width);
                            if (glPlot instanceof EarthGLPlot) {
                                float scale = glPlot.getScale();
                                shift /= -scale;
                            }
                            float head = glPlot.getHeadAngle() - shift;
                            if (head >= 360) {
                                head -= 360;
                            } else if (head < 0) {
                                head += 360;
                            }
                            if (head < 1)
                                head = 0;
                            else if (head > 359)
                                head = 0;
                            glPlot.setHeadAngle(head);

                            if (glPlot instanceof EarthGLPlot) {
                                shift = 180.0f * ((float) (this.mouseLastPos.y - y) / size.height);
                                float pitch = glPlot.getPitchAngle() + shift;
                                if (pitch > 0) {
                                    pitch = 0;
                                }
                                if (pitch < -90) {
                                    pitch = -90;
                                }
                                glPlot.setPitchAngle(pitch);
                            }
                            this.repaint();
                        }
                    }
                }
                break;
        }
        mouseLastPos.x = x;
        mouseLastPos.y = y;
    }

    void onMouseClicked(MouseEvent e) {
        int clickTimes = e.getClickCount();
        if (clickTimes == 1) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                switch (this.mouseMode) {
                    case IDENTIFIER:
                        Plot plot = selPlot(e.getX(), e.getY());
                        if (plot == null) {
                            return;
                        }

                        if (!(plot instanceof MapPlot)) {
                            return;
                        }

                        this.currentPlot = plot;
                        MapPlot mplot = (MapPlot) plot;
                        final MapView mapView = mplot.getMapView();
                        MapLayer aMLayer = mplot.getSelectedLayer();
                        if (aMLayer == null) {
                            return;
                        }
                        if (aMLayer.getLayerType() == LayerTypes.IMAGE_LAYER) {
                            return;
                        }

                        Rectangle2D rect = mplot.getGraphArea();
                        PointF aPoint = new PointF(e.getX() - (float) rect.getX(), e.getY() - (float) rect.getY());
                        if (aMLayer.getLayerType() == LayerTypes.VECTOR_LAYER) {
                            VectorLayer aLayer = (VectorLayer) aMLayer;
                            List<Integer> selectedShapes = mapView.selectShapes(aLayer, aPoint, true, false);
                            if (selectedShapes.size() > 0) {
                                if (mapView.frmIdentifer == null) {
                                    mapView.frmIdentifer = new FrmIdentifer((JFrame) SwingUtilities.getWindowAncestor(this), false, mapView);
                                    mapView.frmIdentifer.addWindowListener(new WindowAdapter() {
                                        @Override
                                        public void windowClosed(WindowEvent e) {
                                            mapView.setDrawIdentiferShape(false);
                                            GLChartPanel.this.repaintOld();
                                        }
                                    });
                                }
                                String[] colNames = {"Field", "Value"};
                                String fieldStr, valueStr;
                                int shapeIdx = selectedShapes.get(0);
                                aLayer.setIdentiferShape(shapeIdx);
                                mapView._drawIdentiferShape = true;

                                Object[][] tData = new Object[aLayer.getFieldNumber() + 1][2];
                                fieldStr = "Index";
                                valueStr = String.valueOf(shapeIdx);
                                tData[0][0] = fieldStr;
                                tData[0][1] = valueStr;
                                Object value;
                                if (aLayer.getShapeNum() > 0) {
                                    for (int i = 0; i < aLayer.getFieldNumber(); i++) {
                                        Field field = aLayer.getField(i);
                                        fieldStr = field.getColumnName();
                                        value = aLayer.getCellValue(i, shapeIdx);
                                        if (value == null) {
                                            valueStr = "";
                                        } else if (field.getDataType() == DataType.DATE) {
                                            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                            valueStr = format.format((LocalDateTime) value);
                                        } else {
                                            valueStr = value.toString();
                                        }
                                        tData[i + 1][0] = fieldStr;
                                        tData[i + 1][1] = valueStr;
                                    }
                                }
                                DefaultTableModel dtm = new DefaultTableModel(tData, colNames) {
                                    @Override
                                    public boolean isCellEditable(int row, int column) {
                                        return false;
                                    }
                                };
                                mapView.frmIdentifer.getTable().setModel(dtm);
                                mapView.frmIdentifer.repaint();
                                if (!mapView.frmIdentifer.isVisible()) {
                                    //this._frmIdentifer.setLocation(e.getX(), e.getY());
                                    mapView.frmIdentifer.setLocationRelativeTo(this);
                                    mapView.frmIdentifer.setVisible(true);
                                }

                                mapView.setDrawIdentiferShape(true);
                                this.repaintOld();
                            }
                        } else if (aMLayer.getLayerType() == LayerTypes.RASTER_LAYER) {
                            RasterLayer aRLayer = (RasterLayer) aMLayer;
                            int[] ijIdx = mapView.selectGridCell(aRLayer, aPoint);
                            if (ijIdx != null) {
                                int iIdx = ijIdx[0];
                                int jIdx = ijIdx[1];
                                double aValue = aRLayer.getCellValue(iIdx, jIdx);
                                if (mapView._frmIdentiferGrid == null) {
                                    mapView._frmIdentiferGrid = new FrmIdentiferGrid((JFrame) SwingUtilities.getWindowAncestor(this), false);
                                }

                                mapView._frmIdentiferGrid.setIIndex(iIdx);
                                mapView._frmIdentiferGrid.setJIndex(jIdx);
                                mapView._frmIdentiferGrid.setCellValue(aValue);
                                if (!mapView._frmIdentiferGrid.isVisible()) {
                                    //this._frmIdentiferGrid.setLocation(e.getX(), e.getY());
                                    mapView._frmIdentiferGrid.setLocationRelativeTo(this);
                                    mapView._frmIdentiferGrid.setVisible(true);
                                }
                            }
                        }
                        break;
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                popupMenu.show(this, e.getX(), e.getY());
            }
        }
    }

    void onMouseWheelMoved(MouseWheelEvent e) {
        Plot plot = selPlot(e.getX(), e.getY());

        if (plot instanceof AbstractPlot2D) {
            double minX, maxX, minY, maxY, lonRan, latRan, zoomF;
            double mouseLon, mouseLat;
            Extent drawExtent = ((AbstractPlot2D) plot).getDrawExtent();
            lonRan = drawExtent.maxX - drawExtent.minX;
            latRan = drawExtent.maxY - drawExtent.minY;
            mouseLon = drawExtent.minX + lonRan / 2;
            mouseLat = drawExtent.minY + latRan / 2;

            zoomF = 1 + e.getWheelRotation() / 10.0f;

            minX = mouseLon - (lonRan / 2 * zoomF);
            maxX = mouseLon + (lonRan / 2 * zoomF);
            minY = mouseLat - (latRan / 2 * zoomF);
            maxY = mouseLat + (latRan / 2 * zoomF);
            switch (this.mouseMode) {
                case PAN:
                    if (plot instanceof MapPlot) {
                        MapPlot mplt = (MapPlot) plot;
                        Graphics2D g = (Graphics2D) this.getGraphics();
                        Rectangle2D mapRect = mplt.getGraphArea();

                        this.lastMouseWheelTime = LocalDateTime.now();
                        if (!this.mouseWheelDetctionTimer.isRunning()) {
                            this.mouseWheelDetctionTimer.start();
                            tempImage = new BufferedImage((int) mapRect.getWidth() - 2,
                                    (int) mapRect.getHeight() - 2, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D tg = tempImage.createGraphics();
                            tg.setColor(Color.white);
                            tg.fill(mapRect);
                            tg.drawImage(this.mapBitmap, -(int) mapRect.getX() - 1, -(int) mapRect.getY() - 1, this);
                            tg.dispose();
                        }

                        g.setClip(mapRect);
                        g.setColor(Color.white);
                        //g.clearRect((int)mapRect.getX(), (int)mapRect.getY(), (int)mapRect.getWidth(), (int)mapRect.getHeight());
                        paintScale = paintScale / zoomF;
                        float nWidth = (float) mapRect.getWidth() * (float) paintScale;
                        float nHeight = (float) mapRect.getHeight() * (float) paintScale;
                        float nx = ((float) mapRect.getWidth() - nWidth) / 2;
                        float ny = ((float) mapRect.getHeight() - nHeight) / 2;
                        if (nx > 0) {
                            g.fillRect((int) mapRect.getX(), (int) mapRect.getY(), (int) nx, (int) mapRect.getHeight());
                            g.fillRect((int) (mapRect.getMaxX() - nx), (int) mapRect.getY(), (int) nx, (int) mapRect.getHeight());
                        }
                        if (ny > 0) {
                            g.fillRect((int) mapRect.getX(), (int) mapRect.getY(), (int) mapRect.getWidth(), (int) ny);
                            g.fillRect((int) mapRect.getX(), (int) (mapRect.getMaxY() - ny), (int) mapRect.getWidth(), (int) ny);
                        }
                        g.drawImage(tempImage, (int) (mapRect.getX() + nx), (int) (mapRect.getY() + ny),
                                (int) nWidth, (int) nHeight, null);
                        g.setColor(this.getForeground());
                        g.draw(mapRect);
                        mplt.setDrawExtent(new Extent(minX, maxX, minY, maxY));
                    } else {
                        ((AbstractPlot2D) plot).setDrawExtent(new Extent(minX, maxX, minY, maxY));
                        //this.paintGraphics();
                        this.repaintNew();
                    }
                    break;
            }
        } else if (plot instanceof GLPlot) {
            GLPlot glPlot = (GLPlot) plot;
            Extent3D extent = glPlot.getDrawExtent();
            //float zoomF = 1 + e.getWheelRotation() / 10.0f;
            float zoomF = e.getWheelRotation() / 10.0f;
            double dx = extent.getWidth() * zoomF;
            double dy = extent.getHeight() * zoomF;
            if (glPlot instanceof EarthGLPlot) {
                double dz = extent.getZLength() * zoomF;
                extent = extent.extend(dx, dy, dz);
            } else {
                if (this.zoomXY) {
                    if (e.isShiftDown()) {
                        double dz = extent.getZLength() * zoomF;
                        extent = extent.extend(dx, dy, dz);
                    } else {
                        extent = extent.extend(dx, dy, 0);
                    }
                } else {
                    if (e.isShiftDown()) {
                        extent = extent.extend(dx, dy, 0);
                    } else {
                        double dz = extent.getZLength() * zoomF;
                        extent = extent.extend(dx, dy, dz);
                    }
                }
            }

            glPlot.setDrawExtent(extent);

            this.repaint();
        }
    }

    /**
     * Zoom back to full extent
     */
    @Override
    public void onUndoZoomClick() {
        Plot plot;
        if (this.currentPlot == null) {
            plot = this.chart.getPlots().get(0);
        } else {
            plot = this.currentPlot;
        }
        plot.setDrawExtent((Extent) plot.getExtent().clone());
        this.repaintNew();
    }

    private void onSaveFigureClick(ActionEvent e) {
        String path = System.getProperty("user.dir");
        File pathDir = new File(path);
        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        String[] fileExts = new String[]{"png"};
        GenericFileFilter pngFileFilter = new GenericFileFilter(fileExts, "Png Image (*.png)");
        aDlg.addChoosableFileFilter(pngFileFilter);
        fileExts = new String[]{"gif"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Gif Image (*.gif)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"jpg"};
        mapFileFilter = new GenericFileFilter(fileExts, "Jpeg Image (*.jpg)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"eps"};
        mapFileFilter = new GenericFileFilter(fileExts, "EPS file (*.eps)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"pdf"};
        mapFileFilter = new GenericFileFilter(fileExts, "PDF file (*.pdf)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"emf"};
        mapFileFilter = new GenericFileFilter(fileExts, "EMF file (*.emf)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        aDlg.setFileFilter(pngFileFilter);
        aDlg.setAcceptAllFileFilterUsed(false);
        if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            System.setProperty("user.dir", aFile.getParent());
            String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
            String fileName = aFile.getAbsolutePath();
            if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                fileName = fileName + "." + extent;
            }
            if (new File(fileName).exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this, "File exists! Overwrite it?");
                if (overwrite == JOptionPane.YES_OPTION) {
                    this.saveImage(fileName);
                }
            } else {
                this.saveImage(fileName);
            }
        }
    }

    /**
     * Save image to a picture file
     *
     * @param aFile File path
     */
    @Override
    public void saveImage(String aFile) {
        try {
            saveImageSleep(aFile, null);
        } catch (PrintException | IOException | InterruptedException | ImageWriteException ex) {
            Logger.getLogger(GLChartPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save image to a picture file
     *
     * @param aFile File path
     * @param sleep Sleep seconds for web map layer
     * @throws FileNotFoundException
     * @throws PrintException
     * @throws InterruptedException
     */
    public void saveImageSleep(String aFile, Integer sleep) throws FileNotFoundException, PrintException, IOException, InterruptedException, ImageWriteException {
        int w, h;
        if (this.chartSize == null) {
            w = this.getWidth();
            h = this.getHeight();
        } else {
            w = this.chartSize.width;
            h = this.chartSize.height;
        }
        this.saveImage(aFile, w, h, sleep);
    }
    
    /**
     * Save image to a picture file
     *
     * @param aFile File path
     * @param width Width
     * @param height Height
     * @param sleep Sleep seconds for web map layer
     * @throws FileNotFoundException
     * @throws PrintException
     * @throws InterruptedException
     */
    public void saveImage(String aFile, int width, int height, Integer sleep) throws FileNotFoundException, PrintException, IOException, InterruptedException, ImageWriteException {
        if (aFile.endsWith(".ps")) {
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
            String mimeType = "application/postscript";
            StreamPrintServiceFactory[] factories = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, mimeType);
            FileOutputStream out = new FileOutputStream(aFile);
            if (factories.length > 0) {
                PrintService service = factories[0].getPrintService(out);
                SimpleDoc doc = new SimpleDoc(new Printable() {
                    @Override
                    public int print(Graphics g, PageFormat pf, int page) {
                        if (page >= 1) {
                            return Printable.NO_SUCH_PAGE;
                        } else {
                            double sf1 = pf.getImageableWidth() / (getWidth() + 1);
                            double sf2 = pf.getImageableHeight() / (getHeight() + 1);
                            double s = Math.min(sf1, sf2);
                            Graphics2D g2 = (Graphics2D) g;
                            g2.translate((pf.getWidth() - pf.getImageableWidth()) / 2, (pf.getHeight() - pf.getImageableHeight()) / 2);
                            g2.scale(s, s);

                            paintGraphics(g2);
                            return Printable.PAGE_EXISTS;
                        }
                    }
                }, flavor, null);
                DocPrintJob job = service.createPrintJob();
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                job.print(doc, attributes);

                if (sleep != null) {
                    Thread.sleep(sleep * 1000);
                }

                out.close();
            }
        } else if (aFile.endsWith(".eps")) {
            Properties p = new Properties();
            p.setProperty("PageSize", "A5");
            VectorGraphics g = new PSGraphics2D(new File(aFile), new Dimension(width, height));
            //g.setProperties(p);
            g.startExport();
            //this.paintGraphics(g);
            this.paintGraphics(g, width, height);

            if (sleep != null) {
                Thread.sleep(sleep * 1000);
            }

            g.endExport();
            g.dispose();
        } else if (aFile.endsWith(".pdf")) {
            try {
                Document document = new Document(new com.itextpdf.text.Rectangle(width, height));
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(aFile));
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate pdfTemp = cb.createTemplate(width, height); 
                Graphics2D g2 = new PdfGraphics2D(pdfTemp, width, height, true);
                this.paintGraphics(g2, width, height);

                if (sleep != null) {
                    Thread.sleep(sleep * 1000);
                }

                g2.dispose(); 
                cb.addTemplate(pdfTemp, 0, 0);
                document.close();
            } catch (DocumentException | FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (aFile.endsWith(".emf")) {
            VectorGraphics g = new EMFGraphics2D(new File(aFile), new Dimension(width, height));
            //g.setProperties(p);
            g.startExport();
            //this.paintGraphics(g);
            this.paintGraphics(g, width, height);

            if (sleep != null) {
                Thread.sleep(sleep * 1000);
            }

            g.endExport();
            g.dispose();
        } else {
            ImageFormats imageFormat = ImageUtil.getImageFormat(aFile);
            int imageType = imageFormat == ImageFormats.JPEG ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
            BufferedImage image;
            if (this.chart.containsGLPlot()) {
                image = JOGLUtil.paintViewImage(this.chart, width, height);
            } else {
                image = new BufferedImage(width, height, imageType);
            }
            Graphics2D g = image.createGraphics();
            if (this.hasWebMap()) {
                for (Plot plot : this.chart.getPlots()) {
                    if (plot instanceof MapPlot) {
                        MapPlot mapPlot = (MapPlot) plot;
                        if (mapPlot.hasWebMapLayer()) {
                            TileLoadListener tileLoadListener = mapPlot.getTileLoadListener();
                            tileLoadListener.setGraphics2D(g);
                            tileLoadListener.setTransform(new AffineTransform());
                            tileLoadListener.setWidth(width);
                            tileLoadListener.setHeight(height);
                        }
                    }
                }
            }

            paintGraphics(g, width, height);

            if (sleep != null) {
                Thread.sleep(sleep * 1000);
            }

            ImageUtil.imageSave(image, aFile);

            if (this.hasWebMap()) {
                for (Plot plot : this.chart.getPlots()) {
                    if (plot instanceof MapPlot) {
                        MapPlot mapPlot = (MapPlot) plot;
                        if (mapPlot.hasWebMapLayer()) {
                            TileLoadListener tileLoadListener = mapPlot.getTileLoadListener();
                            tileLoadListener.setGraphics2D(null);
                            tileLoadListener.setTransform(null);
                            tileLoadListener.setWidth(this.getWidth());
                            tileLoadListener.setHeight(this.getHeight());
                        }
                    }
                }
            }
        }
    }

    /**
     * @param node
     * @param attributeName - name of child node to return
     * @return Node
     */
    private Node getAttributeByName(Node node, String attributeName) {
        if (node == null) {
            return null;
        }
        NamedNodeMap nnm = node.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            if (n.getNodeName().equals(attributeName)) {
                return n;
            }
        }
        return null; // no such attribute was found
    }

    public boolean saveImage_Jpeg(String file, int dpi) {
        int w, h;
        if (this.chartSize == null) {
            w = this.getWidth();
            h = this.getHeight();
        } else {
            w = this.chartSize.width;
            h = this.chartSize.height;
        }
        return this.saveImage_Jpeg(file, w, h, dpi);
    }

    public boolean saveImage_Jpeg(String file, int width, int height, int dpi) {
        double scaleFactor = dpi / 72.0;
        BufferedImage bufferedImage;
        if (this.chart.containsGLPlot()) {
            bufferedImage = JOGLUtil.paintViewImage(this.chart, width, height, dpi);
        } else {
            bufferedImage = new BufferedImage((int)(width * scaleFactor), (int)(height * scaleFactor), BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g = bufferedImage.createGraphics();
        AffineTransform at = g.getTransform();
        at.scale(scaleFactor, scaleFactor);
        g.setTransform(at);
        paintGraphics(g, width, height);

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
            return false;
        }
        g.dispose();

        return true;
    }

    /**
     * Save image
     *
     * @param fileName File name
     * @param dpi DPI
     * @throws IOException
     * @throws InterruptedException
     */
    public void saveImage(String fileName, int dpi) throws IOException, InterruptedException {
        saveImage(fileName, dpi, null);
    }

    /**
     * Save image
     *
     * @param fileName File name
     * @param dpi DPI
     * @param sleep Sleep seconds for web map layer
     * @throws IOException
     * @throws InterruptedException
     */
    public void saveImage(String fileName, int dpi, Integer sleep) throws IOException, InterruptedException {
        int width, height;
        if (this.chartSize != null) {
            height = this.chartSize.height;
            width = this.chartSize.width;
        } else {
            width = this.getWidth();
            height = this.getHeight();
        }
        
        this.saveImage(fileName, dpi, width, height, sleep);
    }

    /**
     * Save image
     *
     * @param fileName File name
     * @param dpi DPI
     * @param width Width
     * @param height Height
     * @param sleep Sleep seconds for web map layer
     * @throws IOException
     * @throws InterruptedException
     */
    public void saveImage(String fileName, int dpi, int width, int height, Integer sleep) throws IOException, InterruptedException {
        File output = new File(fileName);
        output.delete();

        ImageFormats imageFormat = ImageUtil.getImageFormat(fileName);
        double scaleFactor = dpi / 72.0;
        int imageType = imageFormat == ImageFormats.JPEG ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage image;
        if (this.chart.containsGLPlot()) {
            image = JOGLUtil.paintViewImage(this.chart, width, height, dpi);
        } else {
            image = new BufferedImage((int) (width * scaleFactor), (int) (height * scaleFactor), imageType);
        }
        Graphics2D g = image.createGraphics();
        AffineTransform at = g.getTransform();
        at.scale(scaleFactor, scaleFactor);
        g.setTransform(at);
        if (this.hasWebMap()) {
            for (Plot plot : this.chart.getPlots()) {
                if (plot instanceof MapPlot) {
                    MapPlot mapPlot = (MapPlot) plot;
                    if (mapPlot.hasWebMapLayer()) {
                        TileLoadListener tileLoadListener = mapPlot.getTileLoadListener();
                        tileLoadListener.setGraphics2D(g);
                        tileLoadListener.setTransform(at);
                        tileLoadListener.setWidth(width);
                        tileLoadListener.setHeight(height);
                    }
                }
            }
        }

        paintGraphics(g, width, height);
        //g.dispose();

        if (sleep != null) {
            Thread.sleep(sleep * 1000);
        }

        try {
            ImageUtil.imageSave(image, fileName, dpi);
        } catch (ImageWriteException e) {
            e.printStackTrace();
        }

        if (this.hasWebMap()) {
            for (Plot plot : this.chart.getPlots()) {
                if (plot instanceof MapPlot) {
                    MapPlot mapPlot = (MapPlot) plot;
                    if (mapPlot.hasWebMapLayer()) {
                        TileLoadListener tileLoadListener = mapPlot.getTileLoadListener();
                        tileLoadListener.setGraphics2D(null);
                        tileLoadListener.setTransform(null);
                        tileLoadListener.setWidth(this.getWidth());
                        tileLoadListener.setHeight(this.getHeight());
                    }
                }
            }
        }
    }

    /**
     * Get view image
     *
     * @return View image
     */
    public BufferedImage getViewImage() {
        return this.mapBitmap;
    }

    /**
     * Paint view image
     *
     * @return View image
     */
    public BufferedImage paintViewImage() {
        int w, h;
        if (this.chartSize == null) {
            w = this.getWidth();
            h = this.getHeight();
        } else {
            w = this.chartSize.width;
            h = this.chartSize.height;
        }
        return paintViewImage(w, h);
    }

    /**
     * Paint view image
     *
     * @param width Image width
     * @param height Image height
     * @return View image
     */
    public BufferedImage paintViewImage(int width, int height) {
        BufferedImage image;
        if (this.chart.containsGLPlot()) {
            image = JOGLUtil.paintViewImage(this.chart, width, height);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g = image.createGraphics();
        paintGraphics(g);

        return image;
    }
    
    /**
     * Paint view image
     *
     * @param dpi Image resolution
     * @return View image
     */
    public BufferedImage paintViewImage(int dpi) {
        int w, h;
        if (this.chartSize == null) {
            w = this.getWidth();
            h = this.getHeight();
        } else {
            w = this.chartSize.width;
            h = this.chartSize.height;
        }
        return paintViewImage(w, h, dpi);
    }

    /**
     * Paint view image
     *
     * @param width Image width
     * @param height Image height
     * @param dpi Image resolution
     * @return View image
     */
    public BufferedImage paintViewImage(int width, int height, int dpi) {
        double scaleFactor = dpi / 72.0;
        BufferedImage image;
        if (this.chart.containsGLPlot()) {
            image = JOGLUtil.paintViewImage(this.chart, width, height, dpi);
        } else {
            image = new BufferedImage((int)(width * scaleFactor), (int)(height * scaleFactor), BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g = image.createGraphics();
        AffineTransform at = g.getTransform();
        at.scale(scaleFactor, scaleFactor);
        g.setTransform(at);
        paintGraphics(g, width, height);

        return image;
    }

    /**
     * Check if chart panel has web map layer
     *
     * @return Boolean
     */
    public boolean hasWebMap() {
        if (this.chart != null) {
            return this.chart.hasWebMap();
        }

        return false;
    }
    // </editor-fold>
}
