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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.drawing.Draw;
import static org.meteoinfo.drawing.Draw.getDashPattern;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;

/**
 *
 * @author Yaqiang Wang
 */
public class PolarPlot extends Plot2D {

    // <editor-fold desc="Variables">
    private double radius;
    private double bottom = 0;
    private Font xTickFont = new Font("Arial", Font.PLAIN, 12);
    private Font yTickFont = new Font("Aria", Font.PLAIN, 12);
    private Color xTickColor = Color.black;
    private Color yTickColor = Color.black;
    private List<Double> xTickLocations;
    private List<String> xTickLabels;
    private boolean yTickAuto = true;
    private List<Double> yTickLocations;
    private List<String> yTickLabels;
    private float yTickLabelPos = 22.5f;
    private String yTickFormat = "";

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public PolarPlot() {
        super();
        this.setAutoAspect(false);
        GridLine gl = this.getGridLine();
        gl.setDrawXLine(true);
        gl.setDrawYLine(true);
        this.xTickLocations = new ArrayList<>();
        this.xTickLabels = new ArrayList<>();
        double angle = 0;
        while (angle < 360) {
            this.xTickLocations.add(angle);
            String label = DataConvert.removeTailingZeros(String.valueOf(angle)) + String.valueOf((char) 186);
            this.xTickLabels.add(label);
            angle += 45;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get max radius
     * @return Max radius
     */
    public double getRadius() {
        return this.radius;
    }
    
    /**
     * Set max radius
     * @param value Max radius
     */
    public void setRadius(double value){
        this.radius = value + this.bottom;
        super.setDrawExtent(new Extent(-this.radius, this.radius, -this.radius, this.radius));
    }

    /**
     * Get radius bottom - calm wind in center circle
     * @return Radius bottom
     */
    public double getBottom() {
        return this.bottom;
    }

    /**
     * Set radius bottom
     * @param value Radius bottom
     */
    public void setBottom(double value) {
        this.bottom = value;
    }
    
    /**
     * Get x tick font
     * @return X tick font
     */
    public Font getXTickFont(){
        return this.xTickFont;
    }
    
    /**
     * Set x tick font
     * @param value X tick font
     */
    public void setXTickFont(Font value){
        this.xTickFont = value;
    }
    
    /**
     * Get y tick font
     * @return Y tick font
     */
    public Font getYTickFont(){
        return this.yTickFont;
    }
    
    /**
     * Set y tick font
     * @param value Y tick font
     */
    public void setYTickFont(Font value){
        this.yTickFont = value;
    }

    /**
     * Get x tick label color
     * @return X tick label color
     */
    public Color getXTickColor() {return this.xTickColor;}

    /**
     * Set x tick label color
     * @param value X tick label color
     */
    public void setXTickColor(Color value) {this.xTickColor = value;}

    /**
     * Get y tick label color
     * @return Y tick label color
     */
    public Color getYTickColor() {return this.yTickColor;}

    /**
     * Set y tick label color
     * @param value Y tick label color
     */
    public void setYTickColor(Color value) {this.yTickColor = value;}
    
    /**
     * Get x tick locations
     * @return X tick locations
     */
    public List<Double> getXTickLocations(){
        return this.xTickLocations;
    }
    
    /**
     * Set x tick locations
     * @param value X tick locations
     */
    public void setXTickLocations(List<Number> value){
        this.xTickLocations = new ArrayList<>();
        for (Number v : value)
            this.xTickLocations.add(v.doubleValue());
        if (this.xTickLabels.size() != value.size()) {
            this.xTickLabels = new ArrayList<>();
            String label;
            for (Number v : value) {
                label = DataConvert.removeTailingZeros(String.valueOf(v.floatValue())) + String.valueOf((char) 186);
                this.xTickLabels.add(label);
            }
        }
    }
    
    /**
     * Get x tick labels
     * @return X tick labels
     */
    public List<String> getXTickLabels(){
        return this.xTickLabels;
    }
    
    /**
     * Set x tick labels
     * @param value X tick labels
     */
    public void setXTickLabels(List<String> value){
        this.xTickLabels = value;
    }
    
    /**
     * Get y tick locations
     * @return Y tick locations
     */
    public List<Double> getYTickLocations(){
        return this.yTickLocations;
    }
    
    /**
     * Set y tick locations
     * @param value Y tick locations
     */
    public void setYTickLocations(List<Number> value){
        this.yTickLocations = new ArrayList<>();
        for (Number v : value)
            this.yTickLocations.add(v.doubleValue());
        this.yTickAuto = false;
    }
    
    /**
     * Get y tick labels
     * @return Y tick labels
     */
    public List<String> getYTickLabels(){
        return this.yTickLabels;
    }
    
    /**
     * Set y tick labels
     * @param value Y tick labels
     */
    public void setYTickLabels(List<String> value){
        this.yTickLabels = value;
        this.yTickAuto = false;
    }
    
    /**
     * Get y tick label position
     * @return Y tick label position
     */
    public float getYTickLabelPos(){
        return this.yTickLabelPos;
    }
    
    /**
     * Set y tick label position
     * @param value Y tick label position
     */
    public void setYTickLabelPos(float value){
        this.yTickLabelPos = value;
    }
    
    /**
     * Get y tick format
     * @return Y tick format
     */
    public String getYTickFormat(){
        return this.yTickFormat;
    }
    
    /**
     * Set y tick format
     * @param value Y tick format
     */
    public void setYTickFormat(String value){
        this.yTickFormat = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a graphic
     *
     * @param g Graphic
     */
    @Override
    public void addGraphic(Graphic g) {
        GraphicFactory.polarToCartesian((GraphicCollection) g, this.bottom);
        super.addGraphic(g);
    }

    /**
     * Add a graphic by index
     *
     * @param idx Index
     * @param g Graphic
     */
    @Override
    public void addGraphic(int idx, Graphic g) {
        GraphicFactory.polarToCartesian((GraphicCollection) g, this.bottom);
        super.addGraphic(idx, g);
    }

    @Override
    Extent getAutoExtent() {
        Extent extent = this.getGraphics().getExtent();
        if (extent.minX == extent.maxX) {
            extent.minX = extent.minX - Math.abs(extent.minX);
            extent.maxX = extent.maxX + Math.abs(extent.minX);
        }
        if (extent.minY == extent.maxY) {
            extent.minY = extent.minY - Math.abs(extent.minY);
            extent.maxY = extent.maxY + Math.abs(extent.maxY);
        }

        return extent;
    }

    /**
     * Set draw extent
     *
     * @param extent Extent
     */
    @Override
    public void setDrawExtent(Extent extent) {
        double max = Math.abs(extent.minX);
        max = Math.max(max, Math.abs(extent.maxX));
        max = Math.max(max, Math.abs(extent.minY));
        max = Math.max(max, Math.abs(extent.maxY));
        double[] values = (double[]) MIMath.getIntervalValues(0, max, true).get(0);
        this.radius = values[values.length - 1] + this.bottom;
        super.setDrawExtent(new Extent(-this.radius, this.radius, -this.radius, this.radius));
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

        if (this.getTitle() != null) {
            top += this.getTitle().getTrueDimension(g).height + 15;
        }

        if (!this.getLegends().isEmpty()) {
            Dimension dim = this.getLegend().getLegendDimension(g, new Dimension((int) positionArea.getWidth(),
                    (int) positionArea.getHeight()));
            switch (this.getLegend().getPosition()) {
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

        int space = 5;
        if (this.xTickLabels != null && this.xTickLabels.size() > 0) {
            g.setFont(xTickFont);
            Dimension dim = Draw.getStringDimension(this.xTickLabels.get(0), g);
            bottom += dim.height;
            top += dim.height;
            left += dim.width + space;
            right += dim.width + space;
        }

        return new Margin(left, right, top, bottom);
    }
    
    /**
     * Get graphic area
     *
     * @param g Graphic2D
     * @param area Whole area
     * @return Graphic area
     */
    @Override
    public Rectangle2D getGraphArea(Graphics2D g, Rectangle2D area) {
        int left = 5, bottom = 5, right = 5, top = 5;
        int space = 10;

        if (this.getTitle() != null) {
            top += this.getTitle().getTrueDimension(g).height + 10;
        }

        if (!this.getLegends().isEmpty()) {
            Dimension dim = this.getLegend().getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
            switch (this.getLegend().getPosition()) {
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
        bottom += space;
        left += space;
        top += space;
        right += space;

        //Set area
        Rectangle2D plotArea = new Rectangle2D.Double(left, top,
                area.getWidth() - left - right, area.getHeight() - top - bottom);
        return plotArea;
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

        //Draw background
        if (this.background != null) {
            g.setColor(this.getBackground());
            //g.fill(graphArea);
            Ellipse2D ellipse=new Ellipse2D.Double();
            ellipse.setFrame(graphArea);
            g.fill(ellipse);
        }
        
        if (this.getGridLine().isTop()){
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
        this.drawGridLabel(g, graphArea);

        //Draw border circle
        this.drawBorder(g, graphArea);

        //Draw neat line
        if (this.isDrawNeatLine()) {
            g.setStroke(new BasicStroke(1.0f));
            g.setColor(Color.black);
            g.draw(graphArea);
        }

        //Draw text
        this.drawText(g, graphArea);

        //Draw legend
        this.drawLegend(g, area, graphArea);

    }
    
    @Override
    int getTopAxisHeight(Graphics2D g) {
        g.setFont(xTickFont);
        int height = Draw.getStringDimension("tick", g).height + 5;
        return height;
    }

    @Override
    void drawGraph(Graphics2D g, Rectangle2D area) {
        super.drawGraph(g, area);
    }

    @Override
    void drawGridLine(Graphics2D g, Rectangle2D area) {
        GridLine gridLine = this.getGridLine();
        if (!gridLine.isDrawXLine() && !gridLine.isDrawYLine()) {
            return;
        }

        double[] xy;
        double x, y;
        double miny = area.getY();
        double minx = area.getX();

        if (gridLine.getStyle() == LineStyles.SOLID) {
            g.setStroke(new BasicStroke(gridLine.getSize()));
        } else {
            float[] dashPattern = getDashPattern(gridLine.getStyle());
            g.setStroke(new BasicStroke(gridLine.getSize(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10.0f, dashPattern, 0.0f));
        }        

        xy = this.projToScreen(0, 0, area);
        double x0 = xy[0] + minx;
        double y0 = xy[1] + miny;
        //Draw straight grid lines
        if (gridLine.isDrawXLine()) {
            g.setFont(this.xTickFont);
            float shift = 5;
            for (int i = 0; i < this.xTickLocations.size(); i++) {
                double angle = this.xTickLocations.get(i);
                if (bottom != 0) {
                    xy = MIMath.polarToCartesian(Math.toRadians(angle), bottom);
                    xy = this.projToScreen(xy[0], xy[1], area);
                    x0 = xy[0] + minx;
                    y0 = xy[1] + miny;
                }
                xy = MIMath.polarToCartesian(Math.toRadians(angle), this.radius);
                xy = this.projToScreen(xy[0], xy[1], area);
                x = xy[0] + minx;
                y = xy[1] + miny;
                g.setColor(gridLine.getColor());
                g.draw(new Line2D.Double(x0, y0, x, y));
//                //Draw x tick label
//                String label = this.xTickLabels.get(i);
//                Dimension dim = Draw.getStringDimension(label, g);
//                float w = dim.width;
//                float h = dim.height;
//                if (angle == 0 || angle == 180){
//                    y = y + h * 0.5;
//                    if (angle == 0)
//                        x += shift;
//                    else {
//                        x -= w;
//                        x -= shift;
//                    }
//                } else if (angle == 90 || angle == 270) {
//                    x = x - w * 0.5;
//                    if (angle == 90)
//                        y -= shift;
//                    else {
//                        y += h;
//                        y += shift;
//                    }
//                } else if (angle > 0  && angle <= 45) {
//                    x += shift;
//                } else if (angle > 45 && angle < 90) {
//                    y -= shift;
//                } else if (angle > 90 && angle < 180) {
//                    x -= w;
//                    x -= shift;
//                } else if (angle > 180 && angle <= 225) {
//                    x -= w;
//                    x -= shift;
//                    y += h;
//                } else if (angle > 225 && angle < 270) {
//                    x -= w;
//                    x -= shift;
//                    y += h;
//                } else if (angle > 270) {
//                    x += shift;
//                    y += h;
//                }
//                g.setColor(Color.black);
//                g.drawString(label, (float) x, (float) y);
            }
        }

        //Draw y grid lines
        if (gridLine.isDrawYLine()) {
            g.setFont(this.yTickFont);
            if (this.yTickAuto)
                this.yTickLocations = this.getTickValues();
            
            for (int i = 0; i < this.yTickLocations.size(); i++) {
                double v = this.yTickLocations.get(i);
                if (v > 0 && v < this.radius) {
                    g.setColor(gridLine.getColor());
                    this.drawCircle(g, area, v + bottom);
                }
//                if (v > 0){
//                    g.setColor(Color.black);
//                    xy = MIMath.polarToCartesian(Math.toRadians(this.yTickLabelPos), v);
//                    xy = this.projToScreen(xy[0], xy[1], area);
//                    x = xy[0];
//                    y = xy[1];
//                    x += minx;
//                    y += miny;
//                    String label;
//                    if (this.yTickLabels != null)
//                        label = this.yTickLabels.get(i);
//                    else {
//                        if (this.yTickFormat.equals("%"))
//                            label = DataConvert.removeTailingZeros(String.valueOf(BigDecimalUtil.mul(v, 100))) + "%";
//                        else
//                            label = DataConvert.removeTailingZeros(String.valueOf(v));
//                    }
//                    g.drawString(label, (float)x, (float)y);
//                }
            }
        }
    }
    
    void drawGridLabel_bak(Graphics2D g, Rectangle2D area) {
        GridLine gridLine = this.getGridLine();
        if (!gridLine.isDrawXLine() && !gridLine.isDrawYLine()) {
            return;
        }

        double[] xy;
        double x, y;
        double miny = area.getY();
        double minx = area.getX();     

        //Draw straight grid lines
        if (gridLine.isDrawXLine()) {
            g.setFont(this.xTickFont);
            float shift = 5;
            for (int i = 0; i < this.xTickLocations.size(); i++) {
                double angle = this.xTickLocations.get(i);
                xy = MIMath.polarToCartesian(Math.toRadians(angle), this.radius);
                xy = this.projToScreen(xy[0], xy[1], area);
                x = xy[0];
                y = xy[1];
                x += minx;
                y += miny;
                //Draw x tick label
                String label = this.xTickLabels.get(i);
                Dimension dim = Draw.getStringDimension(label, g);
                float w = dim.width;
                float h = dim.height;
                if (angle == 0 || angle == 180){
                    y = y + h * 0.5;
                    if (angle == 0)
                        x += shift;
                    else {
                        x -= w;
                        x -= shift;
                    }
                } else if (angle == 90 || angle == 270) {
                    x = x - w * 0.5;
                    if (angle == 90)
                        y -= shift;
                    else {
                        y += h;
                        y += shift;
                    }
                } else if (angle > 0  && angle <= 45) {
                    x += shift;
                } else if (angle > 45 && angle < 90) {
                    y -= shift;
                } else if (angle > 90 && angle < 180) {
                    x -= w;
                    x -= shift;
                } else if (angle > 180 && angle <= 225) {
                    x -= w;
                    x -= shift;
                    y += h;
                } else if (angle > 225 && angle < 270) {
                    x -= w;
                    x -= shift;
                    y += h;
                } else if (angle > 270) {
                    x += shift;
                    y += h;
                }
                g.setColor(Color.black);
                g.drawString(label, (float) x, (float) y);
            }
        }

        //Draw y grid lines
        if (gridLine.isDrawYLine()) {
            g.setFont(this.yTickFont);
            if (this.yTickAuto)
                this.yTickLocations = this.getTickValues();
            
            for (int i = 0; i < this.yTickLocations.size(); i++) {
                double v = this.yTickLocations.get(i);
                if (v > 0){
                    g.setColor(Color.black);
                    xy = MIMath.polarToCartesian(Math.toRadians(this.yTickLabelPos), v);
                    xy = this.projToScreen(xy[0], xy[1], area);
                    x = xy[0];
                    y = xy[1];
                    x += minx;
                    y += miny;
                    String label;
                    if (this.yTickLabels != null)
                        label = this.yTickLabels.get(i);
                    else {
                        if (this.yTickFormat.equals("%"))
                            label = DataConvert.removeTailingZeros(String.valueOf(BigDecimalUtil.mul(v, 100))) + "%";
                        else
                            label = DataConvert.removeTailingZeros(String.valueOf(v));
                    }
                    g.drawString(label, (float)x, (float)y);
                }
            }
        }
    }
    
    void drawGridLabel(Graphics2D g, Rectangle2D area) {
        GridLine gridLine = this.getGridLine();
        if (!gridLine.isDrawXLine() && !gridLine.isDrawYLine()) {
            return;
        }

        double[] xy;
        double x, y;
        double miny = area.getY();
        double minx = area.getX();     

        //Draw x grid line labels
        if (gridLine.isDrawXLine()) {
            g.setFont(this.xTickFont);
            g.setColor(this.xTickColor);
            float shift = 5;
            for (int i = 0; i < this.xTickLocations.size(); i++) {
                double angle = this.xTickLocations.get(i);
                xy = MIMath.polarToCartesian(Math.toRadians(angle), this.radius);
                xy = this.projToScreen(xy[0], xy[1], area);
                x = xy[0];
                y = xy[1];
                x += minx;
                y += miny;
                //Draw x tick label
                String label = this.xTickLabels.get(i);
                Dimension dim = Draw.getStringDimension(label, g);
                float w = dim.width;
                float h = dim.height;
                if (angle == 0 || angle == 180){
                    if (angle == 0) {
                        x += shift;
                        Draw.drawString(g, (float)x, (float)y, label, XAlign.LEFT, YAlign.CENTER, false);
                    }
                    else {                        
                        x -= shift;
                        Draw.drawString(g, (float)x, (float)y, label, XAlign.RIGHT, YAlign.CENTER, false);
                    }
                } else if (angle == 90 || angle == 270) {
                    if (angle == 90) {
                        y -= shift;
                        Draw.drawString(g, (float)x, (float)y, label, XAlign.CENTER, YAlign.BOTTOM, false);
                    } else {                        
                        y += shift;
                        Draw.drawString(g, (float)x, (float)y, label, XAlign.CENTER, YAlign.TOP, false);
                    }
                } else if (angle > 0  && angle < 90) {
                    x += shift;
                    Draw.drawString(g, (float)x, (float)y, label, XAlign.LEFT, YAlign.BOTTOM, false);
                } else if (angle > 90 && angle < 180) {                    
                    x -= shift;
                    Draw.drawString(g, (float)x, (float)y, label, XAlign.RIGHT, YAlign.BOTTOM, false);
                } else if (angle > 180 && angle < 270) {
                    x -= shift;
                    Draw.drawString(g, (float)x, (float)y, label, XAlign.RIGHT, YAlign.TOP, false);
                } else if (angle > 270) {
                    x += shift;
                    Draw.drawString(g, (float)x, (float)y, label, XAlign.LEFT, YAlign.TOP, false);
                }                
            }
        }

        //Draw y grid lines
        if (gridLine.isDrawYLine()) {
            g.setFont(this.yTickFont);
            g.setColor(this.yTickColor);
            if (this.yTickAuto)
                this.yTickLocations = this.getTickValues();
            
            for (int i = 0; i < this.yTickLocations.size(); i++) {
                double v = this.yTickLocations.get(i);
                if (v > 0  && v < this.radius){
                    xy = MIMath.polarToCartesian(Math.toRadians(this.yTickLabelPos), v + bottom);
                    xy = this.projToScreen(xy[0], xy[1], area);
                    x = xy[0];
                    y = xy[1];
                    x += minx;
                    y += miny;
                    String label;
                    if (this.yTickLabels != null)
                        label = this.yTickLabels.get(i);
                    else {
                        if (this.yTickFormat.equals("%"))
                            label = DataConvert.removeTailingZeros(String.valueOf(BigDecimalUtil.mul(v, 100))) + "%";
                        else
                            label = DataConvert.removeTailingZeros(String.valueOf(v));
                    }
                    g.drawString(label, (float)x, (float)y);
                }
            }
        }
    }

    List<Double> getTickValues() {
        //List<Object> r = MIMath.getIntervalValues1(0, this.radius);
        double[] v =  MIMath.getIntervalValues(0, radius, 4);
        List<Double> vl = new ArrayList<>();
        for (double vv : v){
            vl.add(vv);
        }
        return vl;
    }

    void drawBorder(Graphics2D g, Rectangle2D area) {
        g.setColor(Color.black);
        g.setStroke(new BasicStroke(1.f));
        //this.drawCircle(g, area, radius);
        Ellipse2D ellipse=new Ellipse2D.Double();
        ellipse.setFrame(area);
        g.draw(ellipse);
    }

    void drawCircle(Graphics2D g, Rectangle2D area, double r) {
        double[] xy = this.projToScreen(0, 0, area);
        double x = xy[0] + area.getX();
        double y = xy[1] + area.getY();
        r = this.projXLength(r, area);
        g.draw(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
    }

    // </editor-fold>
}
