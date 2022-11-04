package org.meteoinfo.chart.ui;

import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.geometry.colors.Normalize;
import org.meteoinfo.geometry.colors.OpacityTransferFunction;
import org.meteoinfo.geometry.colors.TransferFunction;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.ui.ColorComboBoxModel;
import org.meteoinfo.ui.ColorListCellRender;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class TransferFunctionPanel extends JPanel {
    private final EventListenerList listeners = new EventListenerList();
    private ColorMap colorMap;
    private ColorMap[] colorMaps;
    private JComboBox jComboBoxColorMap;
    private Array data;
    private double minData, maxData;
    private double minValue, maxValue;
    private Array histogram;
    private List<ControlPoint> colorControlPoints = new ArrayList<>();
    private List<OpacityControlPoint> opacityControlPoints = new ArrayList<>();
    private Color histogramColor;
    private Point mousePressPoint;
    private float xBorderGap;
    private float yBorderGap;
    private float colorMapHeight;
    private float histogramHeight;
    private float cmhistGap;
    private float pointSize;
    private ControlPoint selectedPoint;
    private boolean isDraggingColorCP = false;
    private boolean isDraggingOpacityCP = false;
    private boolean drawColorControlPoints = false;
    private boolean isoValue = false;

    /**
     * Constructor
     * @param data The data array
     * @param colorMap The color map
     */
    public TransferFunctionPanel(Array data, ColorMap colorMap) {
        this(data, colorMap, null);
    }

    /**
     * Constructor
     * @param data The data array
     * @param colorMap The color map
     * @param colorMaps The color maps
     */
    public TransferFunctionPanel(Array data, ColorMap colorMap, ColorMap[] colorMaps) {
        this(data, colorMap, colorMaps, null);
    }

    /**
     * Constructor
     * @param data The data array
     * @param colorMap The color map
     * @param colorMaps The color maps
     * @param opacity The opacity
     */
    public TransferFunctionPanel(Array data, ColorMap colorMap, ColorMap[] colorMaps, Float opacity) {
        this(data, colorMap, colorMaps, opacity, 4);
    }

    /**
     * Constructor
     * @param data The data array
     * @param colorMap The color map
     * @param colorMaps The color maps
     * @param opacity The opacity
     * @param opacityCPNumber Opacity control point number
     */
    public TransferFunctionPanel(Array data, ColorMap colorMap, ColorMap[] colorMaps, Float opacity, int opacityCPNumber) {
        super();
        this.setPreferredSize(new Dimension(200, 120));
        initComponents();

        if (colorMaps != null) {
            setColorMaps(colorMaps);
        }
        this.colorMap = colorMap;
        colorControlPoints.add(new ControlPoint(0.f));
        colorControlPoints.add(new ControlPoint(1.f));
        updateControlPoints(colorControlPoints);

        if (opacity == null) {
            if (opacityCPNumber > 1) {
                for (int i = 0; i < opacityCPNumber; i++) {
                    this.opacityControlPoints.add(new OpacityControlPoint(1.0f * i / (opacityCPNumber - 1)));
                }
            } else {
                this.opacityControlPoints.add(new OpacityControlPoint(0.5f));
            }
        } else {
            if (opacityCPNumber > 1) {
                for (int i = 0; i < opacityCPNumber; i++) {
                    this.opacityControlPoints.add(new OpacityControlPoint(1.0f * i / (opacityCPNumber - 1), opacity));
                }
            } else {
                this.opacityControlPoints.add(new OpacityControlPoint(0.5f, opacity));
            }
        }
        updateControlPoints(opacityControlPoints);

        this.histogramColor = new Color(0, 204, 204);
        this.xBorderGap = 4;
        this.yBorderGap = 4;
        this.colorMapHeight = 15;
        this.histogramHeight = 80;
        this.cmhistGap = 10;
        this.pointSize = 8;
        this.setBackground(new Color(40, 40, 40));
        setData(data);
        this.minValue = 0;
        this.maxValue = 1;

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePressPoint = new Point(e.getX(), e.getY());
                selectedPoint = mouseSelectPoint(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDraggingColorCP) {
                    updateControlPoints(colorControlPoints);
                    isDraggingColorCP = false;
                    fileTransferFunctionChangedEvent();
                } else if (isDraggingOpacityCP) {
                    updateControlPoints(opacityControlPoints);
                    isDraggingOpacityCP = false;
                    fileTransferFunctionChangedEvent();
                }
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }
        });
    }

    /**
     * Constructor for iso value
     * @param data The data array
     */
    public TransferFunctionPanel (Array data) {
        this(data, 1.0f);
    }

    /**
     * Constructor for iso value
     * @param data The data array
     * @param opacity The opacity
     */
    public TransferFunctionPanel (Array data, float opacity) {
        super();
        this.setPreferredSize(new Dimension(200, 120));
        initComponents();

        this.isoValue = true;
        this.drawColorControlPoints = true;

        colorControlPoints.add(new ControlPoint(0.5f));
        updateControlPoints(colorControlPoints);

        /*this.opacityControlPoints.add(new OpacityControlPoint(0.f, opacity));
        this.opacityControlPoints.add(new OpacityControlPoint(0.33f, opacity));
        this.opacityControlPoints.add(new OpacityControlPoint(0.67f, opacity));
        this.opacityControlPoints.add(new OpacityControlPoint(1.0f, opacity));
        updateControlPoints(opacityControlPoints);*/

        this.histogramColor = new Color(0, 204, 204);
        this.xBorderGap = 4;
        this.yBorderGap = 4;
        this.colorMapHeight = 15;
        this.histogramHeight = 80;
        this.cmhistGap = 10;
        this.pointSize = 8;
        this.setBackground(new Color(40, 40, 40));
        setData(data);
        this.minValue = 0;
        this.maxValue = 1;

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePressPoint = new Point(e.getX(), e.getY());
                selectedPoint = mouseSelectPoint(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDraggingColorCP) {
                    updateControlPoints(colorControlPoints);
                    isDraggingColorCP = false;
                    fileTransferFunctionChangedEvent();
                } else if (isDraggingOpacityCP) {
                    updateControlPoints(opacityControlPoints);
                    isDraggingOpacityCP = false;
                    fileTransferFunctionChangedEvent();
                }
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }
        });
    }

    private void initComponents() {
        this.jComboBoxColorMap = new JComboBox();
        this.add(jComboBoxColorMap, BorderLayout.SOUTH);
        this.jComboBoxColorMap.setVisible(false);
    }

    public void addTransferFunctionChangedListener(TransferFunctionChangedListener listener) {
        this.listeners.add(TransferFunctionChangedListener.class, listener);
    }

    public void removeTransferFunctionChangedListener(TransferFunctionChangedListener listener) {
        this.listeners.remove(TransferFunctionChangedListener.class, listener);
    }

    public void fileTransferFunctionChangedEvent() {
        Object[] listeners = this.listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == TransferFunctionChangedListener.class) {
                ((TransferFunctionChangedListener) listeners[i + 1]).transferFunctionChangedEvent(new TransferFunctionChangedEvent(this));
            }
        }
    }

    /**
     * Set data
     * @param data The data array
     */
    public void setData(Array data) {
        if (data != null) {
            this.data = data;
            this.minData = ArrayMath.min(data).doubleValue();
            this.maxData = ArrayMath.max(data).doubleValue();
            this.minValue = this.minData;
            this.maxValue = this.maxData;
            this.histogram = ArrayUtil.histogram(data, 256).get(0);
            this.histogram = ArrayUtil.toFloat(this.histogram);
            this.histogram = ArrayMath.div(this.histogram, ArrayMath.max(this.histogram));
            this.repaint();
        }
    }

    /**
     * Set minimum value
     * @param value Minimum value
     */
    public void setMinValue(double value) {
        this.minValue = value;
        this.repaint();
        fileTransferFunctionChangedEvent();
    }

    /**
     * Set maximum value
     * @param value Maximum value
     */
    public void setMaxValue(double value) {
        this.maxValue = value;
        this.repaint();
        fileTransferFunctionChangedEvent();
    }

    /**
     * Get color map
     * @return Color map
     */
    public ColorMap getColorMap() {
        return this.colorMap;
    }

    /**
     * Set color map
     * @param value Color map
     */
    public void setColorMap(ColorMap value) {
        this.colorMap = value;
    }

    /**
     * Set color maps
     * @param value Color maps
     */
    public void setColorMaps(ColorMap[] value) {
        this.colorMaps = value;

        ColorListCellRender render = new ColorListCellRender();
        render.setPreferredSize(new Dimension(62, 21));
        this.jComboBoxColorMap.setModel(new ColorComboBoxModel(colorMaps));
        this.jComboBoxColorMap.setRenderer(render);
        this.jComboBoxColorMap.setSelectedItem(this.colorMap);

        jComboBoxColorMap.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                onColormapChanged(e);
            }
        });
    }

    /**
     * Get selected control point
     * @return Selected control point
     */
    public ControlPoint getSelectedControlPoint() {
        return this.selectedPoint;
    }

    private void updateControlPoints(List<? extends ControlPoint> cps) {
        if (!this.isoValue) {
            if (cps.size() == 1) {
                ControlPoint cp = cps.get(0);
                cp.setMinRatio(0);
                cp.setMaxRatio(1);
            } else {
                for (int i = 0; i < cps.size(); i++) {
                    ControlPoint cp = cps.get(i);
                    if (i == 0) {
                        cp.setMaxRatio(cps.get(i + 1).getRatio());
                    } else if (i == cps.size() - 1) {
                        cp.setMinRatio(cps.get(i - 1).getRatio());
                    } else {
                        cp.setMinRatio(cps.get(i - 1).getRatio());
                        cp.setMaxRatio(cps.get(i + 1).getRatio());
                    }
                }
            }
        }
    }

    /**
     * Paint component
     *
     * @param g Graphics
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(this.getBackground());
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());

        int n;
        float x, y, w;
        float h = this.colorMapHeight;
        float width = this.getWidth() - xBorderGap * 2;

        //Draw color map
        if (!this.isoValue) {
            n = this.colorMap.getColorCount();
            w = width / n;
            if (w <= 0)
                w = 1;
            x = xBorderGap;
            y = this.getHeight() - h - yBorderGap;
            Color c;
            for (int i = 0; i < n; i++) {
                c = this.colorMap.getColor(i);
                g2.setColor(c);
                if (i == 0)
                    g2.fill(new Rectangle2D.Float(x, y, w, h));
                else
                    g2.fill(new Rectangle2D.Float(x - 1, y, w + 1, h));

                x += w;
            }
        }

        //Draw histogram
        float height = this.histogramHeight;
        if (this.data != null) {
            n = (int) this.histogram.getSize();
            int si = Math.round((float) ((minValue - minData) / (maxData - minData) * n));
            int ei = Math.round((float) ((maxValue - minData) / (maxData - minData) * n));
            x = xBorderGap;
            y = this.getHeight() - h - yBorderGap;
            y -= cmhistGap;
            g2.setColor(this.histogramColor);
            w = width / (ei - si);
            float v;
            for (int i = si; i < ei; i++) {
                v = this.histogram.getFloat(i);
                v = v * height;
                g2.fill(new Rectangle2D.Float(x, y - v, w, v));
                x += w;
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        //Draw color control points
        if (this.drawColorControlPoints) {
            y = this.getHeight() - h - yBorderGap;
            y = y + h / 2;
            for (int i = 0; i < this.colorControlPoints.size(); i++) {
                g2.setColor(Color.white);
                ControlPoint cp = this.colorControlPoints.get(i);
                x = xBorderGap + cp.getRatio() * width;
                Ellipse2D ellipse = new Ellipse2D.Float(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);
                cp.setLocation(x, y);
                g2.fill(ellipse);
                if (cp.isSelected()) {
                    g2.setColor(Color.black);
                    g2.fill(new Ellipse2D.Float(x - pointSize / 4, y - pointSize / 4, pointSize / 2, pointSize / 2));
                }
                if (this.isoValue) {
                    g2.setColor(Color.white);
                    float ty = this.getHeight() - h - yBorderGap - height;
                    g2.draw(new Line2D.Float(x, y, x, ty));
                }
            }
        }

        //Draw opacity control line and points
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, this.opacityControlPoints.size());
        y = this.getHeight() - h - yBorderGap;
        y -= cmhistGap;
        for (int i = 0; i < this.opacityControlPoints.size(); i++) {
            OpacityControlPoint ocp = this.opacityControlPoints.get(i);
            x = xBorderGap + ocp.getRatio() * width;
            float v = ocp.getOpacity() * height;
            if (i == 0) {
                path.moveTo(xBorderGap, y - v);
                path.lineTo(x, y - v);
                if (this.opacityControlPoints.size() == 1) {
                    path.lineTo(x, y - v);
                    path.lineTo(xBorderGap + width, y - v);
                }
            } else if (i == this.opacityControlPoints.size() - 1) {
                path.lineTo(x, y - v);
                path.lineTo(xBorderGap + width, y - v);
            } else {
                path.lineTo(x, y - v);
            }
            ocp.setLocation(x, y - v);
        }

        g2.setColor(Color.white);
        //g2.setStroke(new BasicStroke(2));
        g2.draw(path);
        for (OpacityControlPoint ocp : this.opacityControlPoints) {
            Point2D.Float point = ocp.getLocation();
            Ellipse2D ellipse = new Ellipse2D.Float(point.x - pointSize / 2, point.y - pointSize / 2, pointSize, pointSize);
            g2.setColor(Color.white);
            g2.fill(ellipse);
            if (ocp.isSelected()) {
                g2.setColor(Color.black);
                g2.fill(new Ellipse2D.Float(point.x - pointSize / 4, point.y - pointSize / 4, pointSize / 2, pointSize / 2));
            }
        }

        g2.dispose();
    }

    private Rectangle2D.Float getColorMapExtent() {
        return new Rectangle2D.Float(this.xBorderGap, this.getHeight() - this.yBorderGap - this.colorMapHeight,
                this.getWidth() - this.xBorderGap * 2, this.colorMapHeight);
    }

    private Rectangle2D.Float getHistogramExtent() {
        return new Rectangle2D.Float(this.xBorderGap,
                this.getHeight() - this.yBorderGap - this.colorMapHeight - this.cmhistGap - this.histogramHeight,
                this.getWidth() - this.xBorderGap * 2, this.histogramHeight);
    }

    public boolean mouseInColorMap(MouseEvent e) {
        float y = this.getHeight() - this.yBorderGap;
        return (e.getY() <= y) &&
                (e.getY() >= y - this.colorMapHeight);
    }

    public boolean mouseInHistogram(MouseEvent e) {
        float y = this.getHeight() - this.yBorderGap - this.colorMapHeight - this.cmhistGap;
        return (e.getY() <= y) &&
                (e.getY() >= y - this.histogramHeight);
    }

    private ControlPoint mouseSelectPoint(MouseEvent e) {
        for (ControlPoint cp : this.colorControlPoints) {
            cp.setSelected(false);
        }
        for (OpacityControlPoint ocp : this.opacityControlPoints) {
            ocp.setSelected(false);
        }

        /*if (mouseInColorMap(e)) {
            for (ControlPoint cp : this.colorControlPoints) {
                if (cp.isInPointExtent(e.getX(), e.getY(), this.pointSize + 5)) {
                    cp.setSelected(true);
                    return cp;
                }
            }
        } else if (mouseInHistogram(e)) {
            for (OpacityControlPoint ocp : this.opacityControlPoints) {
                if (ocp.isInPointExtent(e.getX(), e.getY(), this.pointSize + 5)) {
                    ocp.setSelected(true);
                    return ocp;
                }
            }
        }*/

        for (ControlPoint cp : this.colorControlPoints) {
            if (cp.isInPointExtent(e.getX(), e.getY(), this.pointSize + 5)) {
                cp.setSelected(true);
                return cp;
            }
        }

        for (OpacityControlPoint ocp : this.opacityControlPoints) {
            if (ocp.isInPointExtent(e.getX(), e.getY(), this.pointSize + 5)) {
                ocp.setSelected(true);
                return ocp;
            }
        }

        return null;
    }

    private void onMouseClicked(MouseEvent e) {
        selectedPoint = mouseSelectPoint(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            TransferFunctionPanel.this.repaint();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (mouseInColorMap(e)) {
                if (jComboBoxColorMap != null) {
                    jComboBoxColorMap.setLocation(this.getX(), this.getY() +
                            this.getHeight() - 20);
                    jComboBoxColorMap.setSize(this.getWidth(), 20);
                    jComboBoxColorMap.setVisible(true);
                    jComboBoxColorMap.showPopup();
                    jComboBoxColorMap.setVisible(false);
                }
            } else if (mouseInHistogram(e)) {
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem jMenuItemControlPoint = new JMenuItem();
                if (selectedPoint == null) {
                    jMenuItemControlPoint.setText("Add control point");
                    jMenuItemControlPoint.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            Rectangle2D.Float extent = getHistogramExtent();
                            float ratio = (e.getX() - extent.x) / extent.width;
                            float opacity = 1 - (e.getY() - extent.y) / extent.height;
                            OpacityControlPoint newOcp = new OpacityControlPoint(ratio);
                            newOcp.setOpacity(opacity);
                            newOcp.setSelected(true);
                            int i = 0;
                            for (OpacityControlPoint ocp : opacityControlPoints) {
                                if (ratio < ocp.getRatio()) {
                                    break;
                                }
                                i += 1;
                            }
                            if (i > 0) {
                                newOcp.setMinRatio(opacityControlPoints.get(i - 1).getMinRatio());
                            }
                            if (i < opacityControlPoints.size()) {
                                newOcp.setMaxRatio(opacityControlPoints.get(i).getMaxRatio());
                            }
                            opacityControlPoints.add(i, newOcp);
                            TransferFunctionPanel.this.repaint();
                        }
                    });
                } else {
                    if (opacityControlPoints.size() > 1) {
                        jMenuItemControlPoint.setText("Delete control point");
                        jMenuItemControlPoint.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                opacityControlPoints.remove(selectedPoint);
                                TransferFunctionPanel.this.repaint();
                            }
                        });
                    }
                }
                popupMenu.add(jMenuItemControlPoint);
                popupMenu.show(TransferFunctionPanel.this, e.getX(), e.getY());
            }
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (selectedPoint != null) {
            if (selectedPoint instanceof OpacityControlPoint) {
                Rectangle2D.Float extent = getHistogramExtent();
                selectedPoint.setRatio((e.getX() - extent.x) / extent.width);
                ((OpacityControlPoint) selectedPoint).setOpacity(1 - (e.getY() - extent.y) / extent.height);
                this.isDraggingOpacityCP = true;
            } else {
                Rectangle2D.Float extent = getColorMapExtent();
                selectedPoint.setRatio((e.getX() - extent.x) / extent.width);
                this.isDraggingColorCP = true;
            }
            this.repaint();
        }
    }

    private void onColormapChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            ColorMap colorMap = (ColorMap) this.jComboBoxColorMap.getSelectedItem();
            this.setColorMap(colorMap);
            this.updateUI();
            this.fileTransferFunctionChangedEvent();
        }
        this.jComboBoxColorMap.hidePopup();
        this.jComboBoxColorMap.setSize(this.jComboBoxColorMap.getWidth(), 0);
    }

    /**
     * Get selected opacity control point
     * @return Selected opacity control point
     */
    public OpacityControlPoint getSelectedOCP() {
        for (OpacityControlPoint ocp : this.opacityControlPoints) {
            if (ocp.isSelected()) {
                return ocp;
            }
        }
        return null;
    }

    /**
     * Get transfer function
     * @return Transfer function
     */
    public TransferFunction getTransferFunction() {
        int n = this.opacityControlPoints.size();
        float[] opacityNodes = new float[n];
        float[] opacityLevels = new float[n];
        for (int i = 0; i < n; i++) {
            OpacityControlPoint opc = opacityControlPoints.get(i);
            opacityNodes[i] = (float) ((opc.getValue(minValue, maxValue) - this.minData) / (this.maxData - this.minData));
            //opacityNodes[i] = opc.getRatio();
            opacityLevels[i] = opc.getOpacity();
        }

        OpacityTransferFunction opacityTransferFunction = new OpacityTransferFunction();
        opacityTransferFunction.setOpacityNodes(opacityNodes);
        opacityTransferFunction.setOpacityLevels(opacityLevels);

        Normalize normalize = new Normalize(minValue, maxValue, true);

        TransferFunction transferFunction = new TransferFunction(opacityTransferFunction, this.colorMap, normalize);

        return transferFunction;
    }
}
