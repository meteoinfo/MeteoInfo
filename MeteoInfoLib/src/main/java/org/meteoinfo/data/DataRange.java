/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import org.meteoinfo.global.MIMath;

/**
 *
 * @author wyq
 */
public class DataRange {
    // <editor-fold desc="Variables">
    private double min;
    private double max;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public DataRange(){
        
    }
    
    /**
     * Constructor
     * @param min Minimum value
     * @param max Maximum value
     */
    public DataRange(double min, double max){
        this.min = min;
        this.max = max;
    }
    
    /**
     * Constructor
     * @param value Data value 
     */
    public DataRange(double value){
        this.min = value;
        this.max = value;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get minimum value
     * @return Minimum value
     */
    public double getMin(){
        return this.min;
    }
    
    /**
     * Set minimum value
     * @param value Minimum value
     */
    public void setMin(double value){
        this.min = value;
    }
    
    /**
     * Get maximum value
     * @return Maximum value
     */
    public double getMax(){
        return this.max;
    }
    
    /**
     * Set maximum value
     * @param value Maximum value
     */
    public void setMax(double value) {
        this.max = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get data range
     * @return Data range
     */
    public double getRange(){
        return this.max - this.min;
    }
    
    /**
     * Get if the data range is zero
     * @return Boolean
     */
    public boolean isFixed(){
        return MIMath.doubleEquals(max, min);
    }
    // </editor-fold>            
}
