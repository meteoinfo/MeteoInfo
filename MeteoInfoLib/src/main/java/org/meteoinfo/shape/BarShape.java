/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

/**
 *
 * @author Yaqiang Wang
 */
public class BarShape extends PointShape{
    // <editor-fold desc="Variables">
    private double width;
    private boolean autoWidth;
    private double error;
    private boolean drawError;
    private double bottom;
    private boolean drawBottom;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public BarShape(){
        super();
        this.autoWidth = true;
        this.drawError = false;
        this.drawBottom = false;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get width
     * @return Width
     */
    public double getWidth(){
        return this.width;
    }
    
    /**
     * Set width
     * @param value Width 
     */
    public void setWidth(double value){
        this.width = value;
    }
    
    /**
     * Get is auto width or not
     * @return Boolean
     */
    public boolean isAutoWidth(){
        return this.autoWidth;
    }
    
    /**
     * Set auto width or not
     * @param value Boolean
     */
    public void setAutoWidth(boolean value){
        this.autoWidth = value;
    }
    
    /**
     * Get error
     * @return Error
     */
    public double getError(){
        return this.error;
    }
    
    /**
     * Set error
     * @param value Error
     */
    public void setError(double value){
        this.error = value;
    }
    
    /**
     * Get is draw error or not
     * @return Boolean
     */
    public boolean isDrawError(){
        return this.drawError;
    }
    
    /**
     * Set if draw error or not
     * @param value Boolean
     */
    public void setDrawError(boolean value){
        this.drawError = value;
    }
    
    /**
     * Get bottom value
     * @return Bottom
     */
    public double getBottom(){
        return this.bottom;
    }
    
    /**
     * Set bottom value
     * @param value Bottom
     */
    public void setBottom(double value){
        this.bottom = value;
    }
    
    /**
     * Get is draw bottom or not
     * @return Boolean
     */
    public boolean isDrawBottom(){
        return this.drawBottom;
    }
    
    /**
     * Set is draw bottom or not
     * @param value Boolean
     */
    public void setDrawBottom(boolean value){
        this.drawBottom = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.Bar;
    }
    // </editor-fold>
}
