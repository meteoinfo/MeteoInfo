/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.ChartWindArrow;
import org.meteoinfo.chart.LegendPosition;
import org.meteoinfo.chart.Location;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.chart.axis.Axis;
import org.meteoinfo.chart.axis.LogAxis;
import static org.meteoinfo.chart.plot.Plot.MINIMUM_HEIGHT_TO_DRAW;
import static org.meteoinfo.chart.plot.Plot.MINIMUM_WIDTH_TO_DRAW;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointF;

/**
 *
 * @author wyq
 */
public abstract class AbstractPlot2D extends Plot {

    // <editor-fold desc="Variables">
    protected Color background;
    //private boolean drawBackground;
    private Color selectColor = Color.yellow;
    private Extent extent;
    private Extent drawExtent;
    private final Map<Location, Axis> axis;
    private Location xAxisLocation;
    private Location yAxisLocation;
    private PlotOrientation orientation;
    private final GridLine gridLine;
    private boolean drawTopAxis;
    private boolean drawRightAxis;
    private boolean drawNeatLine;
    private ChartText title;
    private ChartText leftTitle;
    private ChartText rightTitle;
    private List<ChartLegend> legends;
    private List<ChartText> texts;
    private ChartWindArrow windArrow;
    private boolean autoAspect = true;
    private double aspect = 1;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public AbstractPlot2D() {
        super();
        this.background = null;
        //this.drawBackground = false;
        this.drawExtent = new Extent(0, 1, 0, 1);
        //this.xAxis = new Axis("X", true);
        //this.yAxis = new Axis("Y", false);
        this.axis = new HashMap<>();
        this.axis.put(Location.BOTTOM, new Axis("X", true, Location.BOTTOM));
        this.axis.put(Location.LEFT, new Axis("Y", false, Location.LEFT));
        this.axis.put(Location.TOP, new Axis("X", true, Location.TOP, false));
        this.axis.put(Location.RIGHT, new Axis("Y", false, Location.RIGHT, false));
        this.xAxisLocation = Location.BOTTOM;
        this.yAxisLocation = Location.RIGHT;
        this.orientation = PlotOrientation.VERTICAL;
        this.gridLine = new GridLine();
        this.drawTopAxis = true;
        this.drawRightAxis = true;
        this.drawNeatLine = false;
        this.legends = new ArrayList<>();
        this.texts = new ArrayList<>();
    }
    // </editor-fold>

    // <editor-fold desc="Get Set Methods">
    /**
     * Get title
     *
     * @return Title
     */
    public ChartText getTitle() {
        return this.title;
    }

    /**
     * Set title
     *
     * @param value Title
     */
    public void setTitle(ChartText value) {
        this.title = value;
    }

    /**
     * Set title
     *
     * @param text Title text
     */
    public void setTitle(String text) {
        if (this.title == null) {
            this.title = new ChartText(text);
        } else {
            this.title.setText(text);
        }
    }

    /**
     * Get selected color
     *
     * @return Selected color
     */
    public Color getSelectedColor() {
        return this.selectColor;
    }

    /**
     * Set selected color
     *
     * @param value Selected color
     */
    public void setSelectedColor(Color value) {
        this.selectColor = value;
    }

    /**
     * Get left sub title
     *
     * @return Left sub title
     */
    public ChartText getLeftTitle() {
        return leftTitle;
    }

    /**
     * Set left sub title
     *
     * @param value Left sub title
     */
    public void setLeftTitle(ChartText value) {
        leftTitle = value;
    }
    
    /**
     * Set left sub title
     *
     * @param text Title text
     */
    public void setLeftTitle(String text) {
        if (this.leftTitle == null) {
            this.leftTitle = new ChartText(text);
        } else {
            this.leftTitle.setText(text);
        }
    }
    
    /**
     * Get right sub title
     *
     * @return Right sub title
     */
    public ChartText getRightTitle() {
        return rightTitle;
    }

    /**
     * Set right sub title
     *
     * @param value Right sub title
     */
    public void setRightTitle(ChartText value) {
        rightTitle = value;
    }
    
    /**
     * Set right sub title
     *
     * @param text Title text
     */
    public void setRightTitle(String text) {
        if (this.rightTitle == null) {
            this.rightTitle = new ChartText(text);
        } else {
            this.rightTitle.setText(text);
        }
    }

    /**
     * Get legends
     *
     * @return Legends
     */
    public List<ChartLegend> getLegends() {
        return this.legends;
    }

    /**
     * Get chart legend
     *
     * @param idx Index
     * @return Chart legend
     */
    public ChartLegend getLegend(int idx) {
        if (this.legends.isEmpty()) {
            return null;
        } else {
            return this.legends.get(idx);
        }
    }

    /**
     * Get chart legend
     *
     * @return Chart legend
     */
    public ChartLegend getLegend() {
        if (this.legends.isEmpty()) {
            return null;
        } else {
            return this.legends.get(this.legends.size() - 1);
        }
    }

    /**
     * Set chart legend
     *
     * @param value Legend
     */
    public void setLegend(ChartLegend value) {
        this.legends.clear();
        this.legends.add(value);
    }

    /**
     * Set legends
     *
     * @param value Legends
     */
    public void setLegends(List<ChartLegend> value) {
        this.legends = value;
    }

//    /**
//     * Get if draw legend
//     *
//     * @return If draw legend
//     */
//    public boolean isDrawLegend() {
//        return this.drawLegend;
//    }
//
    /**
     * Set if draw legend
     *
     * @param value Boolean
     */
    public void setDrawLegend(boolean value) {
        //this.drawLegend = value;
        //this.updateLegendScheme();
    }

    /**
     * Get draw extent
     *
     * @return Draw extent
     */
    public Extent getDrawExtent() {
        return this.drawExtent;
    }

    /**
     * Set draw extent
     *
     * @param extent Extent
     */
    public void setDrawExtent(Extent extent) {
        this.drawExtent = extent;
        this.getAxis(Location.BOTTOM).setMinMaxValue(extent.minX, extent.maxX);
        this.getAxis(Location.TOP).setMinMaxValue(extent.minX, extent.maxX);
        this.getAxis(Location.LEFT).setMinMaxValue(extent.minY, extent.maxY);
        this.getAxis(Location.RIGHT).setMinMaxValue(extent.minY, extent.maxY);
    }

    /**
     * Set draw extent
     *
     * @param extent Extent
     */
    public void setDrawExtent1(Extent extent) {
        this.drawExtent = extent;
    }

    /**
     * Get extent
     *
     * @return Extent
     */
    public Extent getExtent() {
        return this.extent;
    }

    /**
     * Set extent
     *
     * @param extent Extent
     */
    public void setExtent(Extent extent) {
        this.extent = extent;
    }

    /**
     * Update draw extent
     */
    public void updateDrawExtent() {
        this.getAxis(Location.BOTTOM).setMinMaxValue(drawExtent.minX, drawExtent.maxX);
        this.getAxis(Location.TOP).setMinMaxValue(drawExtent.minX, drawExtent.maxX);
        this.getAxis(Location.LEFT).setMinMaxValue(drawExtent.minY, drawExtent.maxY);
        this.getAxis(Location.RIGHT).setMinMaxValue(drawExtent.minY, drawExtent.maxY);
    }

    /**
     * Get background
     *
     * @return Background
     */
    public Color getBackground() {
        return this.background;
    }

    /**
     * Set background
     *
     * @param value Background
     */
    public void setBackground(Color value) {
        this.background = value;
    }

//    /**
//     * Get if draw background
//     *
//     * @return Boolean
//     */
//    public boolean isDrawBackground() {
//        return this.drawBackground;
//    }

//    /**
//     * Set if draw background
//     *
//     * @param value Boolean
//     */
//    public void setDrawBackground(boolean value) {
//        this.drawBackground = value;
//    }

    @Override
    public PlotType getPlotType() {
        return PlotType.XY;
    }

    /**
     * Get bottom x axis
     *
     * @return Bottom x aixs
     */
    public Axis getXAxis() {
        return this.axis.get(Location.BOTTOM);
    }

    /**
     * Set x axis
     *
     * @param axis Axis
     * @throws java.lang.CloneNotSupportedException
     */
    public void setXAxis(Axis axis) throws CloneNotSupportedException {
        axis.setLocation(Location.BOTTOM);
        this.axis.put(Location.BOTTOM, axis);
        Axis topAxis = (Axis) axis.clone();
        topAxis.setLocation(Location.TOP);
        this.axis.put(Location.TOP, topAxis);
    }

    /**
     * Get left y axis
     *
     * @return Left y axis
     */
    public Axis getYAxis() {
        return this.axis.get(Location.LEFT);
    }

    /**
     * Set y axis
     *
     * @param axis Axis
     * @throws java.lang.CloneNotSupportedException
     */
    public void setYAxis(Axis axis) throws CloneNotSupportedException {
        axis.setLocation(Location.LEFT);
        this.axis.put(Location.LEFT, axis);
        Axis rightAxis = (Axis) axis.clone();
        rightAxis.setLocation(Location.RIGHT);
        this.axis.put(Location.RIGHT, rightAxis);
    }

    /**
     * Get axis
     *
     * @param loc Axis location
     * @return Axis
     */
    public Axis getAxis(Location loc) {
        return this.axis.get(loc);
    }

    /**
     * Get x axis location
     *
     * @return X axis location
     */
    public Location getXAxisLocation() {
        return this.xAxisLocation;
    }

    /**
     * Set x axis location
     *
     * @param value X axis location
     */
    public void setXAxisLocation(Location value) {
        this.xAxisLocation = value;
    }

    /**
     * Get y axis location
     *
     * @return Y axis location
     */
    public Location getYAxisLocation() {
        return this.yAxisLocation;
    }

    /**
     * Set y axis location
     *
     * @param value Y axis location
     */
    public void setYAxisLocation(Location value) {
        this.yAxisLocation = value;
    }

    /**
     * Get plot orientation
     *
     * @return Plot orientation
     */
    public PlotOrientation getPlotOrientation() {
        return this.orientation;
    }

    /**
     * Set plot orientation
     *
     * @param value Plot orientation
     */
    public void setPlotOrientation(PlotOrientation value) {
        this.orientation = value;
    }

    /**
     * Get grid line
     *
     * @return Grid line
     */
    public GridLine getGridLine() {
        return this.gridLine;
    }

    /**
     * get if draw top axis
     *
     * @return Boolean
     */
    public boolean isDrawTopAxis() {
        return this.drawTopAxis;
    }

    /**
     * Set if draw top right axis
     *
     * @param value Boolean
     */
    public void setDrawTopAxis(boolean value) {
        this.drawTopAxis = value;
    }

    /**
     * get if draw right axis
     *
     * @return Boolean
     */
    public boolean isDrawRightAxis() {
        return this.drawRightAxis;
    }

    /**
     * Set if draw right axis
     *
     * @param value Boolean
     */
    public void setDrawRightAxis(boolean value) {
        this.drawRightAxis = value;
    }

    /**
     * Get if draw neat line
     *
     * @return Boolean
     */
    public boolean isDrawNeatLine() {
        return this.drawNeatLine;
    }

    /**
     * Set if draw neat line
     *
     * @param value Boolean
     */
    public void setDrawNeatLine(boolean value) {
        this.drawNeatLine = value;
    }

    /**
     * Get texts
     *
     * @return Texts
     */
    public List<ChartText> getTexts() {
        return this.texts;
    }

    /**
     * Set texts
     *
     * @param value texts
     */
    public void setTexts(List<ChartText> value) {
        this.texts = value;
    }

    /**
     * Get wind arrow
     *
     * @return Wind arrow
     */
    public ChartWindArrow getWindArrow() {
        return this.windArrow;
    }

    /**
     * Set wind arrow
     *
     * @param value Wind arrow
     */
    public void setWindArrow(ChartWindArrow value) {
        this.windArrow = value;
    }

    /**
     * Get x axis is log or not
     *
     * @return Boolean
     */
    public boolean isLogX() {
        Axis xAxis = this.getXAxis();
        return xAxis instanceof LogAxis;
    }

    /**
     * Get y axis is log or not
     *
     * @return Boolean
     */
    public boolean isLogY() {
        Axis yAxis = this.getYAxis();
        return yAxis instanceof LogAxis;
    }

    /**
     * Get is auto aspect or not
     *
     * @return Boolean
     */
    public boolean isAutoAspect() {
        return this.autoAspect;
    }

    /**
     * Set is auto aspect or not
     *
     * @param value Boolean
     */
    public void setAutoAspect(boolean value) {
        this.autoAspect = value;
    }

    /**
     * Get aspect - scaling from data to plot units for x and y
     *
     * @return Aspect
     */
    public double getAspect() {
        return this.aspect;
    }

    /**
     * Set aspect
     *
     * @param value Aspect
     */
    public void setAspect(double value) {
        this.aspect = value;
    }

    /**
     * Get if y axis is reverse or not
     *
     * @return Boolean
     */
    public boolean isYReverse() {
        return this.getYAxis().isInverse();
    }

    /**
     * Get if x axis is reverse or not
     *
     * @return Boolean
     */
    public boolean isXReverse() {
        return this.getXAxis().isInverse();
    }

    // </editor-fold>
    // <editor-fold desc="Method">
    /**
     * Add a legend
     *
     * @param legend The legend
     */
    public void addLegend(ChartLegend legend) {
        this.legends.add(legend);
    }

    /**
     * Remove a legend
     *
     * @param legend The legend
     */
    public void removeLegend(ChartLegend legend) {
        this.legends.remove(legend);
    }

    /**
     * Remove a legend by index
     *
     * @param idx The legend index
     */
    public void removeLegend(int idx) {
        this.legends.remove(idx);
    }

    /**
     * Set axis
     *
     * @param axis The axis
     * @param loc Axis location
     */
    public void setAxis(Axis axis, Location loc) {
        this.axis.put(loc, axis);
    }

    /**
     * Set axis label font
     *
     * @param font Font
     */
    public void setAxisLabelFont(Font font) {
        for (Axis ax : this.axis.values()) {
            ax.setTickLabelFont(font);
        }
    }

    /**
     * Set all axis visible or not
     *
     * @param value Boolean
     */
    public void setAxisOn(boolean value) {
        for (Axis ax : this.axis.values()) {
            ax.setVisible(value);
        }
    }

    /**
     * Set axis tick line inside box or not
     *
     * @param isInside Inside box ot not
     */
    public void setInsideTick(boolean isInside) {
        this.getAxis(Location.LEFT).setInsideTick(isInside);
        this.getAxis(Location.RIGHT).setInsideTick(isInside);
        this.getAxis(Location.TOP).setInsideTick(isInside);
        this.getAxis(Location.BOTTOM).setInsideTick(isInside);
    }

    /**
     * Get is inside tick line or not
     *
     * @return Is inside or not
     */
    public boolean isInsideTick() {
        return this.getAxis(Location.BOTTOM).isInsideTick();
    }

    /**
     * Draw plot
     *
     * @param g Graphics2D
     * @param area Drawing area
     */
    @Override
    public void draw(Graphics2D g, Rectangle2D area) {
        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }

        Rectangle2D graphArea;
        graphArea = this.getPositionArea();
        this.setGraphArea(graphArea);

        //Draw title
        this.drawTitle(g, graphArea);
      
        if (graphArea.getWidth() < 10 || graphArea.getHeight() < 10) {
            return;
        }

        if (this.getGridLine().isTop()) {
            //Draw graph        
            this.drawGraph(g, graphArea);
            //Draw grid line
            this.drawGridLine(g, graphArea);
        } else {
            //Draw grid line
            this.drawGridLine(g, graphArea);
            //Draw graph        
            this.drawGraph(g, graphArea);
        }

        //Draw neat line
        if (this.drawNeatLine) {
            g.setStroke(new BasicStroke(1.0f));
            g.setColor(Color.black);
            g.draw(graphArea);
        }

        //Draw axis
        this.drawAxis(g, graphArea);

        //Draw text
        this.drawText(g, graphArea);

        //Draw legend
        this.drawLegend(g, area, graphArea);

        //Draw wind arrow - quiverkey
        if (this.getWindArrow() != null) {
            ChartWindArrow wa = this.getWindArrow();
            float x = (float) (area.getWidth() * wa.getX());
            float y = (float) (area.getHeight() * (1 - wa.getY()));
            wa.draw(g, x, y);
        }
    }

    /**
     * Get tight inset area
     *
     * @param g Graphics2D
     * @param positionArea Position area
     * @return Tight inset area
     */
    @Override
    public Margin getTightInset(Graphics2D g, Rectangle2D positionArea) {
        int left = 2, bottom = 2, right = 2, top = 5;

        top += this.getAxis(Location.TOP).getXAxisHeight(g);
        if (this.title != null) {
            top += this.title.getTrueDimension(g).height + 10;
        }        
        if (this.leftTitle != null){
            top += this.leftTitle.getDimension(g).height + 5;
        } else {
            if (this.rightTitle != null){
                top += this.rightTitle.getDimension(g).height + 5;
            }
        }

        if (!this.legends.isEmpty()) {
            ChartLegend legend = this.getLegend();
            Dimension dim = legend.getLegendDimension(g, new Dimension((int) positionArea.getWidth(),
                    (int) positionArea.getHeight()));
            switch (legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + 10;
                    break;
            }
        }

        //Get x axis space
        bottom += this.getXAxisHeight(g) + 5;

        //Get y axis space
        left += this.getYAxisWidth(g) + 5;

        //Set right space
        int radd = this.getAxis(Location.RIGHT).getYAxisWidth(g);
        if (this.getXAxis().isVisible()) {
            if (this.getXAxis().isDrawTickLabel()) {
                radd = Math.max(radd,this.getXAxis().getMaxLabelLength(g) / 2);
            }
        }
        right += radd;

        return new Margin(left, right, top, bottom);
    }

    /**
     * Get tight inset area
     *
     * @param g Graphics2D
     * @param positionArea Position area
     * @return Tight inset area
     */
    public Rectangle2D getTightInsetArea(Graphics2D g, Rectangle2D positionArea) {
        int left = 0, bottom = 0, right = 5, top = 5;
        int space = 1;

        if (this.title != null) {
            g.setFont(this.title.getFont());
            Dimension dim = Draw.getStringDimension(this.title.getText(), g);
            top += dim.getHeight() + 10;
        }

        if (!this.legends.isEmpty()) {
            ChartLegend legend = this.getLegend();
            Dimension dim = legend.getLegendDimension(g, new Dimension((int) positionArea.getWidth(),
                    (int) positionArea.getHeight()));
            switch (legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + 10;
                    break;
            }
        }

        //Get x axis space
        bottom += this.getXAxisHeight(g);

        //Get y axis space
        left += this.getYAxisWidth(g);

        //Set right space
        if (this.getXAxis().isVisible()) {
            if (this.getXAxis().isDrawTickLabel()) {
                right += this.getXAxis().getMaxLabelLength(g) / 2;
            }
        }

        double x = positionArea.getX() - left;
        double y = positionArea.getY() - top;
        double w = positionArea.getWidth() + left + right;
        double h = positionArea.getHeight() + top + bottom;

        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * Get position area
     *
     * @return Position area
     */
    @Override
    public Rectangle2D getPositionArea() {
        if (this.autoAspect) {
            return super.getPositionArea();
        } else {
            Rectangle2D plotArea = super.getPositionArea();
            double width = this.drawExtent.getWidth();
            double height = this.drawExtent.getHeight();
            if (width / height / aspect > plotArea.getWidth() / plotArea.getHeight()) {
                double h = plotArea.getWidth() * height * aspect / width;
                double delta = plotArea.getHeight() - h;
                plotArea.setRect(plotArea.getX(), plotArea.getY() + delta / 2, plotArea.getWidth(), h);
            } else {
                double w = width * plotArea.getHeight() / height / aspect;
                double delta = plotArea.getWidth() - w;
                plotArea.setRect(plotArea.getX() + delta / 2, plotArea.getY(), w, plotArea.getHeight());
            }

            return plotArea;
        }
    }
    
    /**
     * Get outer position area
     *
     * @param area Whole area
     * @return Position area
     */
    @Override
    public Rectangle2D getOuterPositionArea(Rectangle2D area) {
        Rectangle2D rect = this.getOuterPosition();
        double x = area.getWidth() * rect.getX() + area.getX();
        double y = area.getHeight() * (1 - rect.getHeight() - rect.getY()) + area.getY();
        double w = area.getWidth() * rect.getWidth();
        double h = area.getHeight() * rect.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * Get graphic area
     *
     * @param g Graphic2D
     * @param area Whole area
     * @return Graphic area
     */
    public Rectangle2D getGraphArea(Graphics2D g, Rectangle2D area) {
        int left = 5, bottom = 5, right = 5, top = 5;
        int space = 5;

        if (this.title != null) {
            top += this.title.getTrueDimension(g).height + 10;
        }

        if (!this.legends.isEmpty()) {
            ChartLegend legend = this.getLegend();
            Dimension dim = legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
            switch (legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + 10;
                    break;
            }
        }

        //Get x axis space
        bottom += this.getXAxisHeight(g);

        //Get y axis space
        left += this.getYAxisWidth(g);

        //Set right space
        if (this.getXAxis().isVisible()) {
            if (this.getXAxis().isDrawTickLabel()) {
                right += this.getXAxis().getMaxLabelLength(g) / 2;
            }
        }

        //Set area
        Rectangle2D plotArea = new Rectangle2D.Double(left, top,
                area.getWidth() - left - right, area.getHeight() - top - bottom);
        return plotArea;
    }    
    
    int getXAxisHeight(Graphics2D g) {
        return this.getXAxis().getXAxisHeight(g);
    }
    
    int getYAxisWidth(Graphics2D g) {
        return this.getYAxis().getYAxisWidth(g);
    }
    
    int getTopAxisHeight(Graphics2D g) {
        int height = this.getAxis(Location.TOP).getXAxisHeight(g);
        return height;
    }

    void drawTitle(Graphics2D g, Rectangle2D graphArea) {
        float x;
        float y = (float) graphArea.getY() - this.getTopAxisHeight(g);
        int sh = 0;
        if (leftTitle != null) {
            x = (float) graphArea.getX();
            y -= 5;
            leftTitle.draw(g, x, y);
            y += 5;
            sh = leftTitle.getDimension(g).height + 5;
        }
        if (rightTitle != null) {
            x = (float) (graphArea.getX() + graphArea.getWidth());
            y -= 5;
            rightTitle.draw(g, x, y);
            y += 5;
            sh = rightTitle.getDimension(g).height + 5;
        }
        if (title != null) {
            y -= sh;
            y -= 8;
            x = (float) (graphArea.getX() + graphArea.getWidth() / 2);
            title.draw(g, x, y);
        }        
    }

    void drawGridLine(Graphics2D g, Rectangle2D area) {
        if (!this.gridLine.isDrawXLine() && !this.gridLine.isDrawYLine()) {
            return;
        }

        double[] xy;
        double x, y;
        double miny = area.getY();
        double minx = area.getX();
        double maxx = area.getX() + area.getWidth();
        double maxy = area.getY() + area.getHeight();

        float[] dashPattern = Draw.getDashPattern(this.gridLine.getStyle());
        g.setColor(this.gridLine.getColor());
        g.setStroke(new BasicStroke(this.gridLine.getSize(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, dashPattern, 0.0f));

        //Draw x grid lines
        if (this.gridLine.isDrawXLine()) {
            this.getXAxis().updateTickLabels();
            //this.getXAxis().updateLabelGap(g, area);
            int n = 0;
            while (n < this.getXAxis().getTickValues().length) {
                double value = this.getXAxis().getTickValues()[n];
                if (value <= this.getXAxis().getMinValue() || value >= this.getXAxis().getMaxValue()) {
                    n += this.getXAxis().getTickLabelGap();
                    continue;
                }
                xy = this.projToScreen(value, this.drawExtent.minY, area);
                x = xy[0];
                if (this.getXAxis().isInverse()) {
                    x = area.getWidth() - x;
                }
                x += minx;
                g.draw(new Line2D.Double(x, maxy, x, miny));

                n += this.getXAxis().getTickLabelGap();
            }
        }

        //Draw y grid lines
        if (this.gridLine.isDrawYLine()) {
            this.getYAxis().updateTickLabels();
            //this.getYAxis().updateLabelGap(g, area);
            int n = 0;
            while (n < this.getYAxis().getTickValues().length) {
                double value = this.getYAxis().getTickValues()[n];
                if (value <= this.getYAxis().getMinValue() || value >= this.getYAxis().getMaxValue()) {
                    n += this.getYAxis().getTickLabelGap();
                    continue;
                }
                xy = this.projToScreen(this.drawExtent.minX, value, area);
                y = xy[1];
                if (this.getYAxis().isInverse()) {
                    y = area.getHeight() - y;
                }
                y += area.getY();
                g.draw(new Line2D.Double(minx, y, maxx, y));

                n += this.getYAxis().getTickLabelGap();
            }
        }
    }

    abstract void drawGraph(Graphics2D g, Rectangle2D area);

    void drawAxis(Graphics2D g, Rectangle2D area) {
        for (Location loc : this.axis.keySet()) {
            Axis ax = this.axis.get(loc);
            if (ax.isVisible()) {
                ax.updateLabelGap(g, area);
                ax.draw(g, area, this);
            }
        }
    }

    void drawText(Graphics2D g, Rectangle2D area) {
//        Iterator<ChartText> iter = this.getTexts().iterator();
//        while (iter.hasNext()){
//            drawPlotText(iter.next(), g, area);
//        }
        for (int i = 0; i < this.getTexts().size(); i++){
            drawPlotText(this.getTexts().get(i), g, area);
        }
    }

    void drawPlotText(ChartText text, Graphics2D g, Rectangle2D area) {
        float x, y;
        switch (text.getCoordinates()) {
            case AXES:
                AffineTransform oldMatrix = g.getTransform();
                Rectangle oldRegion = g.getClipBounds();
                g.setClip(area);
                g.translate(area.getX(), area.getY());
                x = (float) (area.getWidth() * text.getX());
                y = (float) (area.getHeight() * (1 - text.getY()));
                this.drawText(text, g, x, y);
                g.setTransform(oldMatrix);
                g.setClip(oldRegion);
                break;
            case FIGURE:
                x = (float) (area.getWidth() * text.getX());
                y = (float) (area.getHeight() * (1 - text.getY()));
                this.drawText(text, g, x, y);
                break;
            case DATA:
                oldMatrix = g.getTransform();
                oldRegion = g.getClipBounds();
                g.setClip(area);
                g.translate(area.getX(), area.getY());
                double[] xy = this.projToScreen(text.getX(), text.getY(), area);
                x = (float) xy[0];
                y = (float) xy[1];
                this.drawText(text, g, x, y);
                g.setTransform(oldMatrix);
                g.setClip(oldRegion);
                break;
        }
    }
    
    void drawText(ChartText text, Graphics2D g, float x, float y) {        
        g.setFont(text.getFont());        
        text.draw(g, x, y);
    }

    void drawLegend(Graphics2D g, Rectangle2D area, Rectangle2D graphArea) {
        if (!this.legends.isEmpty()) {
            Object rendering = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (ChartLegend legend : this.legends) {
                if (legend.isColorbar()) {
                    if (legend.getPlotOrientation() == PlotOrientation.VERTICAL) {
                        legend.setHeight((int) (graphArea.getHeight() * legend.getShrink()));
                        legend.setLegendHeight(legend.getHeight());
                    } else {
                        legend.setWidth((int) (graphArea.getWidth() * legend.getShrink()));
                        legend.setLegendWidth(legend.getWidth());
                    }
                }
                if (legend.getPosition() == LegendPosition.CUSTOM) {
                    legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
                    float x = (float) (area.getWidth() * legend.getX());
                    float y = (float) (area.getHeight() * (1 - (this.getLegend().getHeight() / area.getHeight())
                            - this.getLegend().getY()));
                    legend.draw(g, new PointF(x, y));
                } else {
                    this.drawLegendScheme(legend, g, graphArea);
                }
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rendering);
        }
    }

    void drawLegendScheme(ChartLegend legend, Graphics2D g, Rectangle2D area) {
        g.setFont(legend.getTickLabelFont());
        Dimension dim = legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
        float x = 0; 
        float y = 0;
        switch (legend.getPosition()) {
            case UPPER_CENTER_OUTSIDE:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) (area.getY() - this.getAxis(Location.TOP).getXAxisHeight(g) - dim.height - 5);
                break;
            case LOWER_CENTER_OUTSIDE:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) (area.getY() + area.getHeight() + this.getXAxisHeight(g) + 10);
                break;
            case LEFT_OUTSIDE:
                x = 10;
                y = (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case RIGHT_OUTSIDE:
                if (this.getAxis(Location.RIGHT).isDrawTickLabel() || this instanceof PolarPlot) {
                    x = (float) area.getX() + (float) area.getWidth() + (float) this.getTightInset().getRight();
                    x = x - dim.width;
                } else {
                    x = (float) area.getX() + (float) area.getWidth() + 10;
                }
                y = (float) area.getY() + (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case UPPER_CENTER:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) area.getY() + 10;
                break;
            case UPPER_RIGHT:
                x = (float) (area.getX() + area.getWidth()) - dim.width - 10;
                y = (float) area.getY() + 10;
                break;
            case LOWER_CENTER:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
            case LOWER_RIGHT:
                x = (float) (area.getX() + area.getWidth()) - dim.width - 10;
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
            case UPPER_LEFT:
                x = (float) area.getX() + 10;
                y = (float) area.getY() + 10;
                break;
            case LOWER_LEFT:
                x = (float) area.getX() + 10;
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
        }
        legend.draw(g, new PointF(x, y));
    }

    /**
     * Convert coordinate from map to screen
     *
     * @param projX Map X
     * @param projY Map Y
     * @param area Drawing area
     * @return Screen X/Y array
     */
    public double[] projToScreen(double projX, double projY, Rectangle2D area) {
        double width = drawExtent.getWidth();
        double height = drawExtent.getHeight();
        if (this.isLogY()) {
            height = Math.log10(drawExtent.maxY) - Math.log10(drawExtent.minY);
        }
        if (this.isLogX()) {
            width = Math.log10(drawExtent.maxX) - Math.log10(drawExtent.minX);
        }
        double scaleX = area.getWidth() / width;
        double scaleY = area.getHeight() / height;
        double screenX = (projX - drawExtent.minX) * scaleX;
        double screenY = (drawExtent.maxY - projY) * scaleY;
        if (this.isLogY()) {
            screenY = (Math.log10(drawExtent.maxY) - Math.log10(projY)) * scaleY;
        }
        if (this.isLogX()) {
            screenX = (Math.log10(projX) - Math.log10(drawExtent.minX)) * scaleX;
        }
        if (this.isYReverse()) {
            screenY = area.getHeight() - screenY;
        }
        if (this.isXReverse()) {
            screenX = area.getWidth() - screenX;
        }

        return new double[]{screenX, screenY};
    }

    /**
     * Convert data length to screen length in x direction
     *
     * @param len data length
     * @param area Drawing area
     * @return Screen length
     */
    public double projXLength(double len, Rectangle2D area) {
        double scaleX = area.getWidth() / drawExtent.getWidth();
        return len * scaleX;
    }

    /**
     * Convert data length to screen length in y direction
     *
     * @param len data length
     * @param area Drawing area
     * @return Screen length
     */
    public double projYLength(double len, Rectangle2D area) {
        double scaleY = area.getHeight() / drawExtent.getHeight();
        return len * scaleY;
    }

    /**
     * Convert coordinate from screen to map
     *
     * @param screenX Screen X
     * @param screenY Screen Y
     * @param area Area
     * @return Projected X/Y
     */
    public double[] screenToProj(double screenX, double screenY, Rectangle2D area) {
        double width = drawExtent.getWidth();
        double height = drawExtent.getHeight();
        if (this.isLogY()) {
            height = Math.log10(drawExtent.maxY) - Math.log10(drawExtent.minY);
        }
        if (this.isLogX()) {
            width = Math.log10(drawExtent.maxX) - Math.log10(drawExtent.minX);
        }
        if (this.isYReverse()) {
            screenY = area.getHeight() - screenY;
        }
        if (this.isXReverse()) {
            screenX = area.getWidth() - screenX;
        }
        double scaleX = area.getWidth() / width;
        double scaleY = area.getHeight() / height;
        double projX = screenX / scaleX + drawExtent.minX;
        double projY = drawExtent.maxY - screenY / scaleY;
        if (this.isLogY()) {
            projY = Math.pow(10, Math.log10(drawExtent.maxY) - screenY / scaleY);
        }
        if (this.isLogX()) {
            projX = Math.pow(10, screenX / scaleX + Math.log10(drawExtent.minX));
        }

        return new double[]{projX, projY};
    }

    abstract Extent getAutoExtent();

    public abstract void setAutoExtent();

    public abstract void updateLegendScheme();

    /**
     * Zoom to screen extent
     *
     * @param minX Minimum x
     * @param maxX Maximum x
     * @param minY Minimum y
     * @param maxY Maximum y
     */
    public void zoomToExtentScreen(double minX, double maxX, double minY, double maxY) {
        double[] pMin = screenToProj(minX, maxY, this.getGraphArea());
        double[] pMax = screenToProj(maxX, minY, this.getGraphArea());
        this.setDrawExtent(new Extent(pMin[0], pMax[0], pMin[1], pMax[1]));
    }

    /**
     * Add text
     *
     * @param text Chart text
     */
    public void addText(ChartText text) {
        this.getTexts().add(text);
    }
    // </editor-fold>
}
