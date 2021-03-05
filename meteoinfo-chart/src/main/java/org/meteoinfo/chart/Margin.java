/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.geom.Rectangle2D;

/**
 *
 * @author wyq
 */
public class Margin {
    // <editor-fold desc="Variables">
    private double left;
    private double right;
    private double top;
    private double bottom;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Margin(){
        
    }
    
    /**
     * Constructor
     * @param left Left
     * @param right Right
     * @param top Top
     * @param bottom Bottom
     */
    public Margin(double left, double right, double top, double bottom){
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get left
     * @return Left
     */
    public double getLeft(){
        return this.left;
    }
    
    /**
     * Set left
     * @param value Left
     */
    public void setLeft(double value){
        this.left = value;
    }
    
    /**
     * Get right
     * @return Right
     */
    public double getRight(){
        return this.right;
    }
    
    /**
     * Set right
     * @param value Right
     */
    public void setRight(double value){
        this.right = value;
    }
    
    /**
     * Get top
     * @return Top 
     */
    public double getTop(){
        return this.top;
    }
    
    /**
     * Set top
     * @param value Top
     */
    public void setTop(double value){
        this.top = value;
    }
    
    /**
     * Get bottom
     * @return Bottom
     */
    public double getBottom(){
        return this.bottom;
    }
    
    /**
     * Set bottom
     * @param value Bottom
     */
    public void setBottom(double value){
        this.bottom = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get margin area
     * @param inArea Inside area
     * @return Margin area
     */
    public Rectangle2D getArea(Rectangle2D inArea){
        double x = inArea.getX() - this.left;
        double y = inArea.getY() - this.top;
        double w = inArea.getWidth() + this.left + this.right;
        double h = inArea.getHeight() + this.top + this.bottom;
        
        return new Rectangle2D.Double(x, y, w, h);
    }
    
    /**
     * Extent
     * @param a Margin
     * @return Extented margin
     */
    public Margin extend(Margin a){
        Margin r = new Margin();
        r.setLeft(Math.max(this.left, a.left));
        r.setRight(Math.max(this.right, a.right));
        r.setTop(Math.max(this.top, a.top));
        r.setBottom(Math.max(this.bottom, a.bottom));
        
        return r;
    }
    // </editor-fold>
}
