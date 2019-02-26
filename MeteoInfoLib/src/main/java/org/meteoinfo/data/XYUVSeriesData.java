/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.List;
import ucar.ma2.Array;

/**
 *
 * @author wyq
 */
public class XYUVSeriesData extends XYSeriesData {
    // <editor-fold desc="Variables">
    private double[] udata;
    private double[] vdata;
    private boolean uv = true;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XYUVSeriesData(){
        super();
    }
    
     /**
     * Constructor
     * @param key Series key
     * @param xdata X data
     * @param ydata Y data
     * @param udata U data
     * @param vdata V data
     * @param isuv Is U/V or not
     */
    public XYUVSeriesData(String key, double[] xdata, double[] ydata, double[] udata, double[] vdata,
        boolean isuv){
        super(key, xdata, ydata);
        this.udata = udata;
        this.vdata = vdata;
        this.uv = isuv;
    }
    
    /**
     * Constructor
     * @param key Series key
     * @param xdata X data
     * @param ydata Y data
     * @param udata U data
     * @param vdata V data
     */
    public XYUVSeriesData(String key, double[] xdata, double[] ydata, double[] udata, double[] vdata){
        this(key, xdata, ydata, udata, vdata, true);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get U data
     * @return U data
     */
    public double[] getUdata(){
        return this.udata;
    }
    
    /**
     * Set U data
     * @param value U data 
     */
    public void setUdata(double[] value){
        this.udata = value;
    }
    
    /**
     * Set U data
     * @param value U data 
     */
    public void setUdata(List<Number> value){
        this.udata = new double[value.size()];
        double v;
        for (int i = 0; i < value.size(); i++){
            v = value.get(i).doubleValue();
            if (Double.isNaN(v))
                udata[i] = this.getMissingValue();
            else
                udata[i] = v;
        }
    }
    
    /**
     * Set U data
     * @param value U data 
     */
    public void setUdata(Array value){
        this.udata = new double[(int)value.getSize()];
        double v;
        for (int i = 0; i < udata.length; i++){
            v = value.getDouble(i);
            if (Double.isNaN(v))
                udata[i] = this.getMissingValue();
            else
                udata[i] = v;
        }
    }
    
    /**
     * Get V data
     * @return V data
     */
    public double[] getVdata(){
        return this.vdata;
    }
    
    /**
     * Set V data
     * @param value V data 
     */
    public void setVdata(double[] value){
        this.vdata = value;
    }
    
    /**
     * Set V data
     * @param value V data 
     */
    public void setVdata(List<Number> value){
        this.vdata = new double[value.size()];
        double v;
        for (int i = 0; i < value.size(); i++){
            v = value.get(i).doubleValue();
            if (Double.isNaN(v))
                vdata[i] = this.getMissingValue();
            else
                vdata[i] = v;
        }
    }
    
    /**
     * Set V data
     * @param value V data 
     */
    public void setVdata(Array value){
        this.vdata = new double[(int)value.getSize()];
        double v;
        for (int i = 0; i < vdata.length; i++){
            v = value.getDouble(i);
            if (Double.isNaN(v))
                vdata[i] = this.getMissingValue();
            else
                vdata[i] = v;
        }
    }
    
    /**
     * Get is U/V or not
     * @return Boolean
     */
    public boolean isUV(){
        return this.uv;
    }
    
    /**
     * Set is U/V or not
     * @param value Boolean
     */
    public void setUV(boolean value){
        this.uv = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
