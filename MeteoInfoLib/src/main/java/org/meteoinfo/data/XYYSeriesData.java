/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.List;
import ucar.ma2.Array;

/**
 *
 * @author Yaqiang Wang
 */
public class XYYSeriesData extends XYSeriesData {
    // <editor-fold desc="Variables">
    private double[] y2data;
    private List<Boolean> where;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XYYSeriesData(){
        super();
    }
    
     /**
     * Constructor
     * @param key Series key
     * @param xdata X data
     * @param ydata Y data
     * @param y2data Y2 data
     */
    public XYYSeriesData(String key, double[] xdata, double[] ydata, double[] y2data){
        super(key, xdata, ydata);
        this.y2data = y2data;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get Y2 data
     * @return Y2 data
     */
    public double[] getY2data(){
        return this.y2data;
    }
    
    /**
     * Set Y2 data
     * @param value Y2 data 
     */
    public void setY2data(double[] value){
        this.y2data = value;
    }
    
    /**
     * Set Y2 data
     * @param value Y2 data 
     */
    public void setY2data(List<Number> value){
        this.y2data = new double[value.size()];
        double v;
        for (int i = 0; i < value.size(); i++){
            v = value.get(i).doubleValue();
            if (Double.isNaN(v))
                y2data[i] = this.getMissingValue();
            else
                y2data[i] = v;
        }
    }
    
    /**
     * Set Y2 data
     * @param value Y2 data 
     */
    public void setY2data(Array value){
        this.y2data = new double[(int)value.getSize()];
        double v;
        for (int i = 0; i < y2data.length; i++){
            v = value.getDouble(i);
            if (Double.isNaN(v))
                y2data[i] = this.getMissingValue();
            else
                y2data[i] = v;
        }
    }
    
    /**
     * Get where boolean list
     * @return Where boolean list
     */
    public List<Boolean> getWhere(){
        return this.where;
    }
    
    /**
     * Set where boolean list
     * @param value Where boolean list
     */
    public void setWhere(List<Boolean> value){
        this.where = value;
    }
    
    /**
     * Set where boolean list
     * @param value Where boolean list
     */
    public void setWhere(Array value){
        this.where = new ArrayList<>();
        for (int i = 0; i < value.getSize(); i++){
            this.where.add(value.getInt(i) == 1);
        }
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
