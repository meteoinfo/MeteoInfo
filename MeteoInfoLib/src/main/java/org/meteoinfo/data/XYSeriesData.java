/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.MIMath;
import ucar.ma2.Array;

/**
 *
 * @author yaqiang
 */
public class XYSeriesData {
    // <editor-fold desc="Variables">
    private String key;
    private double[] xdata;
    private double[] ydata;
    private double missingValue = -9999.0;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XYSeriesData(){

    }
    
    /**
     * Constructor
     * @param key Key
     */
    public XYSeriesData(String key){
        this.key = key;
    }
    
    /**
     * Constructor
     * @param key Series key
     * @param xdata X data
     * @param ydata Y data
     */
    public XYSeriesData(String key, double[] xdata, double[] ydata){
        this.key = key;
        this.xdata = xdata;
        this.ydata = ydata;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get series key
     * @return Series key
     */
    public String getKey(){
        return key;
    }
    
    /**
     * Set series key
     * @param value Series key
     */
    public void setKey(String value){
        key = value;
    }     
    
    /**
     * Get X data
     * @return X data
     */
    public double[] getXdata(){
        return this.xdata;
    }
    
    /**
     * Set X data
     * @param value X data 
     */
    public void setXdata(double[] value){
        this.xdata = value;
    }
    
    /**
     * Set X data
     * @param value X data 
     */
    public void setXdata(List<Number> value){
        this.xdata = new double[value.size()];
        double v;
        for (int i = 0; i < value.size(); i++){
            v = value.get(i).doubleValue();
            if (Double.isNaN(v))
                xdata[i] = this.missingValue;
            else
                xdata[i] = v;
        }
    }
    
    /**
     * Set X data
     * @param value X data 
     */
    public void setXdata(Array value){
        this.xdata = new double[(int)value.getSize()];
        double v;
        for (int i = 0; i < xdata.length; i++){
            v = value.getDouble(i);
            if (Double.isNaN(v))
                xdata[i] = this.missingValue;
            else
                xdata[i] = v;
        }
    }
    
    /**
     * Get Y data
     * @return Y data
     */
    public double[] getYdata(){
        return this.ydata;
    }
    
    /**
     * Set Y data
     * @param value Y data 
     */
    public void setYdata(double[] value){
        this.ydata = value;
    }
    
    /**
     * Set Y data
     * @param value Y data 
     */
    public void setYdata(List<Number> value){
        this.ydata = new double[value.size()];
        double v;
        for (int i = 0; i < value.size(); i++){
            v = value.get(i).doubleValue();
            if (Double.isNaN(v))
                ydata[i] = this.missingValue;
            else
                ydata[i] = v;
        }
    }
    
    /**
     * Set Y data
     * @param value Y data 
     */
    public void setYdata(Array value){
        this.ydata = new double[(int)value.getSize()];
        double v;
        for (int i = 0; i < ydata.length; i++){
            v = value.getDouble(i);
            if (Double.isNaN(v))
                ydata[i] = this.missingValue;
            else
                ydata[i] = v;
        }
    }
    
    /**
     * Get missing value
     * @return Missing value
     */
    public double getMissingValue(){
        return this.missingValue;
    }
    
    /**
     * Set missing value
     * @param value Missing value
     */
    public void setMissingValue(double value){
        this.missingValue = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get data length
     * @return Data length
     */
    public int dataLength(){
        return this.xdata.length;
    }
    
    /**
     * Get x value
     * @param idx Index
     * @return X value
     */
    public double getX(int idx){
        return this.xdata[idx];
    }
    
    /**
     * Get x - error value
     * @param idx Index
     * @return X - error value
     */
    public double getX_min(int idx){
        return this.xdata[idx];
    }
    
    /**
     * Get x + error value
     * @param idx Index
     * @return X + error value
     */
    public double getX_max(int idx){
        return this.xdata[idx];
    }
    
    /**
     * Get y value
     * @param idx Index
     * @return Y value
     */
    public double getY(int idx){
        return this.ydata[idx];
    }
    
    /**
     * Get y - error value
     * @param idx Index
     * @return Y - error value
     */
    public double getY_min(int idx){
        return this.ydata[idx];
    }
    
    /**
     * Get y + error value
     * @param idx Index
     * @return Y + error value
     */
    public double getY_max(int idx){
        return this.ydata[idx];
    }
    
    /**
     * Get missing value index list
     * @return Missing value index list
     */
    public List<Integer> getMissingValueIndex(){
        List<Integer> mvidx = new ArrayList<>();
        for (int i = 0; i < xdata.length; i++){
            if (MIMath.doubleEquals(xdata[i], this.missingValue) || MIMath.doubleEquals(ydata[i], this.missingValue))
                mvidx.add(i);
        }
        
        return mvidx;
    }
    // </editor-fold>
}
