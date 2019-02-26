/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.data.Dataset;

/**
 *
 * @author yaqiang
 */
public abstract class Plot {
    
    /** The default outline stroke. */
    public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(0.5f,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    /** The default outline color. */
    public static final Paint DEFAULT_OUTLINE_PAINT = Color.gray;

    /** The default foreground alpha transparency. */
    public static final float DEFAULT_FOREGROUND_ALPHA = 1.0f;

    /** The default background alpha transparency. */
    public static final float DEFAULT_BACKGROUND_ALPHA = 1.0f;

    /** The default background color. */
    public static final Paint DEFAULT_BACKGROUND_PAINT = Color.white;

    /** The minimum width at which the plot should be drawn. */
    public static final int MINIMUM_WIDTH_TO_DRAW = 10;

    /** The minimum height at which the plot should be drawn. */
    public static final int MINIMUM_HEIGHT_TO_DRAW = 10;

    /** A default box shape for legend items. */
    public static final Shape DEFAULT_LEGEND_ITEM_BOX
            = new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0);

    /** A default circle shape for legend items. */
    public static final Shape DEFAULT_LEGEND_ITEM_CIRCLE
            = new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0);   
    
    protected Rectangle2D position = new Rectangle2D.Double(0.13, 0.11, 0.775, 0.815);        
    protected Rectangle2D outerPosition = new Rectangle2D.Double(0, 0, 1, 1);        
    private Rectangle2D outerPositionArea;
    private Margin tightInset = new Margin();
    
    /** If is sub plot. */
    public boolean isSubPlot = false;
    
    /** Column index as a sub plot. */
    public int columnIndex = 0;
    
    /** Row index as a sub plot. */
    public int rowIndex = 0;

    private boolean outerPosActive = true;
    private boolean sameShrink = false;      
    
    //units - normalized or pixels
    protected AxesUnits units = AxesUnits.NORMALIZED;
    
    protected boolean symbolAntialias = true;
    
    /**
     * Get units
     * @return Units
     */
    public AxesUnits getUnits(){
        return this.units;
    }
    
    /**
     * Set units
     * @param value Units
     */
    public void setUnits(AxesUnits value){
        this.units = value;
    }
    
    /**
     * Set units
     * @param value Units
     */
    public void setUnits(String value){
        this.units = AxesUnits.valueOf(value.toUpperCase());
    }
    
    /**
     * Get if Outer position active
     * @return Boolean
     */
    public boolean isOuterPosActive(){
        return this.outerPosActive;
    }
    
    /**
     * Set outer position active or not
     * @param value Boolean
     */
    public void setOuterPosActive(boolean value){
        this.outerPosActive = value;
    }
  
    /**
     * Get if same shrink
     * @return Boolean
     */
    public boolean isSameShrink(){
        return this.sameShrink;
    }
    
    /**
     * Set if same shrink
     * @param value Boolean
     */
    public void setSameShrink(boolean value){
        this.sameShrink = value;
    }    
        
    /**
     * Get position
     * @return Position
     */
    public Rectangle2D getPosition(){
        return this.position;
    }
    
    /**
     * Set position
     * @param value Position 
     */
    public void setPosition(Rectangle2D value){
        this.position = value;
    }
    
    /**
     * Set position
     * @param xmin Minimum x
     * @param ymin Minimum y
     * @param width Width
     * @param height Height
     */
    public void setPosition(double xmin, double ymin, double width, double height){
        this.position = new Rectangle2D.Double(xmin, ymin, width, height);
    }
    
    /**
     * Set position
     * @param pos Position list
     */
    public void setPosition(List<Number> pos){
        this.position = new Rectangle2D.Double(pos.get(0).doubleValue(), pos.get(1).doubleValue(), 
            pos.get(2).doubleValue(), pos.get(3).doubleValue());
    }
    
    /**
     * Update position
     * @param figureArea Figure area
     * @param outerArea Outer position area
     */
    public void updatePosition(Rectangle2D figureArea, Rectangle2D outerArea){
        double x = outerArea.getX() / figureArea.getWidth();
        double y = 1.0 - ((outerArea.getY() - figureArea.getY()) + outerArea.getHeight()) / figureArea.getHeight();
        double w = outerArea.getWidth() / figureArea.getWidth();
        double h = outerArea.getHeight() / figureArea.getHeight();
        this.setPosition(x, y, w, h);
    }
    
    /**
     * Update position
     * @param figureArea Figure areaa
     */
    public void updatePosition(Rectangle2D figureArea){
        double x = this.positionArea.getX() / figureArea.getWidth();
        double y = 1.0 - (this.positionArea.getY() + this.positionArea.getHeight()) / figureArea.getHeight();
        double w = this.positionArea.getWidth() / figureArea.getWidth();
        double h = this.positionArea.getHeight() / figureArea.getHeight();
        this.setPosition(x, y, w, h);
    }    
    
    /**
     * Get tight inset
     * @return Tight inset
     */
    public Margin getTightInset(){
        return this.tightInset;
    }
    
    /**
     * Set tight inset
     * @param value Tight inset
     */
    public void setTightInset(Margin value){
        this.tightInset = value;
    }        
    
    /**
     * Get outer position
     * @return Outer position
     */
    public Rectangle2D getOuterPosition(){
        return this.outerPosition;
    }
    
    /**
     * Set outer postion
     * @param value Outer position
     */
    public void setOuterPosition(Rectangle2D value){
        this.outerPosition = value;
    }
    
    /**
     * Set outer position
     * @param xmin Minimum x
     * @param ymin Minimum y
     * @param width Width
     * @param height Height
     */
    public void setOuterPosition(double xmin, double ymin, double width, double height){
        this.outerPosition = new Rectangle2D.Double(xmin, ymin, width, height);
    }
    
    /**
     * Set outer position
     * @param pos Outer position list
     */
    public void setOuterPosition(List<Number> pos){
        this.position = new Rectangle2D.Double(pos.get(0).doubleValue(), pos.get(1).doubleValue(), 
            pos.get(2).doubleValue(), pos.get(3).doubleValue());
    }
    
    /**
     * Get outer position area
     * @return Outer positoin area
     */
    public Rectangle2D getOuterPositionArea(){
        return this.outerPositionArea;
    }
    
    /**
     * Get outer position area
     * @param area Whole area
     * @return Outer position area
     */
    public abstract Rectangle2D getOuterPositionArea(Rectangle2D area);
    
    /**
     * Set outer position area
     * @param value Outer position area
     */
    public void setOuterPositionArea(Rectangle2D value){
        this.outerPositionArea = value;
    }
    
    /**
     * Get dataset
     * @return Dataset
     */
    public abstract Dataset getDataset();
    
    /**
     * Set dataset
     * @param dataset Dataset
     */
    public abstract void setDataset(Dataset dataset);
    
    /**
     * Get plot type
     * @return Plot type
     */
    public abstract PlotType getPlotType();
    
    /**
     * Get symbol antialias
     * @return Boolean
     */
    public boolean isSymbolAntialias() {
        return this.symbolAntialias;
    }
    
    /**
     * Set symbol antialias
     * @param value Boolean
     */
    public void setSymbolAntialias(boolean value) {
        this.symbolAntialias = value;
    }   
    
    /**
     * Draw graphics
     * @param g2 Graphics2D
     * @param area Graphics area
     */
    public abstract void draw(Graphics2D g2, Rectangle2D area);
    
    private Rectangle2D positionArea = new Rectangle2D.Double();
    
    /**
     * Get position area
     * @return position area
     */
    public Rectangle2D getPositionArea() {
        return this.positionArea;
    }
    
    /**
     * Get position area
     * @param zoom Zoom
     * @return Position area
     */
    public Rectangle2D getPositionArea(double zoom) {
        double w = this.positionArea.getWidth() * zoom;
        double h = this.positionArea.getHeight() * zoom;
        double xshift = (this.positionArea.getWidth() - w) / 2.0;
        double yshift = (this.positionArea.getHeight() - h) / 2.0;
        double x = this.positionArea.getX() + xshift;
        double y = this.positionArea.getY() + yshift;
        
        return new Rectangle2D.Double(x, y, w, h);
    }
    
    /**
     * Set position area
     * @param value Position area
     */
    public void setPositionArea(Rectangle2D value){
        this.positionArea = value;
    }
    
    private Rectangle2D graphArea = new Rectangle2D.Double();
    
    /**
     * Get graph area
     * @return Graph area
     */
    public Rectangle2D getGraphArea(){
        return graphArea;
    }
    
    /**
     * Set graph area
     * @param value Graph area
     */
    public void setGraphArea(Rectangle2D value){
        graphArea = value;
    }    
    
    /**
     * Get position area
     * @param area Figure area
     * @return Position area
     */
    public Rectangle2D getPositionArea(Rectangle2D area) {
        if (this.units == AxesUnits.NORMALIZED) {
            double x = area.getWidth() * this.getPosition().getX() + area.getX();
            double y = area.getHeight() * (1 - this.getPosition().getHeight() - this.getPosition().getY()) + area.getY();
            double w = area.getWidth() * this.getPosition().getWidth();
            double h = area.getHeight() * this.getPosition().getHeight();
            return new Rectangle2D.Double(x, y, w, h);
        } else {
            double x = this.position.getX() + area.getX();
            double y = area.getHeight() - this.position.getY() - this.position.getHeight();
            double w = this.position.getWidth();
            double h = this.position.getHeight();
            return new Rectangle2D.Double(x, y, w, h);
        }
    }
    
    /**
     * Get tight inset
     * @param g Graphics2D
     * @param positionArea Position area
     * @return Tight inset margin
     */
    public abstract Margin getTightInset(Graphics2D g, Rectangle2D positionArea);
        
    private double positionAreaZoom = 1.0;
    
    /**
     * Get position area zoom
     * @return Position area zoom
     */
    public double getPositionAreaZoom(){
        return this.positionAreaZoom;
    }
    
    /**
     * Set position area zoom
     * @param value Position area zoom
     */
    public void setPositionAreaZoom(double value){
        this.positionAreaZoom = value;
    }
    
    /**
     * Get plot shrink
     * @return Plot shrink
     */
    public Margin getPlotShrink(){
        Margin shrink = new Margin();
        if (this.tightInset.getLeft() + this.outerPositionArea.getX() > this.positionArea.getX()){
            shrink.setLeft(this.tightInset.getLeft() + this.outerPositionArea.getX() - this.positionArea.getX());
        }
        if (this.tightInset.getRight()+ this.positionArea.getX() + this.positionArea.getWidth() > 
                this.outerPositionArea.getX() + this.outerPositionArea.getWidth()){
            shrink.setRight(this.tightInset.getRight()+ this.positionArea.getX() + this.positionArea.getWidth() -
                (this.outerPositionArea.getX() + this.outerPositionArea.getWidth()));
        }  
        if (this.tightInset.getTop()+ this.outerPositionArea.getY()> this.positionArea.getY()){
            shrink.setTop(this.tightInset.getTop()+ this.outerPositionArea.getY()- this.positionArea.getY());
        }
        if (this.tightInset.getBottom()+ this.positionArea.getY()+ this.positionArea.getHeight()> 
                this.outerPositionArea.getY() + this.outerPositionArea.getHeight()){
            shrink.setBottom(this.tightInset.getBottom()+ this.positionArea.getY()+ this.positionArea.getHeight() -
                (this.outerPositionArea.getY() + this.outerPositionArea.getHeight()));
        }  
        
        return shrink;
    }
    
    /**
     * Set plot shrink
     * @param shrink Shrink
     */
    public void setPlotShrink(Margin shrink){
        if (this.positionArea == null)
            return;
        
        double x = this.positionArea.getX() + shrink.getLeft();
        double y = this.positionArea.getY() + shrink.getTop();
        double w = this.positionArea.getWidth() - (shrink.getLeft() + shrink.getRight());
        double h = this.positionArea.getHeight() - (shrink.getTop() + shrink.getBottom());
        
        this.positionArea = new Rectangle2D.Double(x, y, w, h);
    }
    
    /**
     * Update position area zoom
     * @return Position area zoom
     */
    public double updatePostionAreaZoom(){
        Rectangle2D tightInsetArea = this.tightInset.getArea(this.positionArea);
        double left = tightInsetArea.getX() - this.outerPositionArea.getX();
        double right = this.outerPositionArea.getX() + this.outerPositionArea.getWidth() - 
                (tightInsetArea.getX() + tightInsetArea.getWidth());
        double top = tightInsetArea.getY() - this.outerPositionArea.getY();
        double bottom = this.outerPositionArea.getY() + this.outerPositionArea.getHeight() - 
            (tightInsetArea.getY() + tightInsetArea.getHeight());
        double minh = Math.min(left, right);
        double minv = Math.min(top, bottom);
        double min = Math.min(minh, minv);
        double zoom = 1.0;
        double factor = 2;
        //if (this.isSubPlot)
        //    factor = 1.5;
        if (min < 0){
            double zoomh = 1.0;
            if (minh < 0)
                zoomh = (this.positionArea.getWidth() - Math.abs(minh) * factor) / this.positionArea.getWidth();
            double zoomv = 1.0;
            if (minv < 0)
                zoomv = (this.positionArea.getHeight() - Math.abs(minv) * factor) / this.positionArea.getHeight();
            zoom = Math.min(zoomh, zoomv);
            if (zoom < 0){
                zoom = 0.2;
            }
        }        
        
        return zoom;
    }
    
    /**
     * Update position area
     */
    public void updatePositionArea(){
        double w = this.outerPositionArea.getWidth() - this.tightInset.getLeft() - this.tightInset.getRight();
        double h = this.outerPositionArea.getHeight() - this.tightInset.getTop() - this.tightInset.getBottom();
        double x = this.outerPositionArea.getX() + this.tightInset.getLeft();
        double y = this.outerPositionArea.getY() + this.tightInset.getTop();
        
        this.positionArea = new Rectangle2D.Double(x, y, w, h);
    }
}
