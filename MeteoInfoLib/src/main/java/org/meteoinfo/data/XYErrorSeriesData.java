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
public class XYErrorSeriesData extends XYSeriesData {
    // <editor-fold desc="Variables">
    private double[] xerror;
    private double[] xerror_upper;
    private double[] yerror;
    private double[] yerror_upper;
    private double[] bottom;    //For stacked bar plot
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XYErrorSeriesData(){
        
    }
    
    /**
     * Constructor
     * @param key Series key
     * @param xdata X data
     * @param ydata Y data
     * @param yerror Y error
     */
    public XYErrorSeriesData(String key, double[] xdata, double[] ydata, double[] yerror){
        super(key, xdata, ydata);
        this.yerror = yerror;
    }
    
    /**
     * Constructor
     * @param key Series key
     * @param xdata X data
     * @param ydata Y data
     * @param xerror X error
     * @param yerror Y error
     */
    public XYErrorSeriesData(String key, double[] xdata, double[] ydata, double[] xerror, double yerror[]){
        super(key, xdata, ydata);
        this.xerror = xerror;
        this.yerror = yerror;
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get X error data
     * @return X error data
     */
    public double[] getXerror(){
        return this.xerror;
    }
    
    /**
     * Set X error data
     * @param value X error data
     */
    public void setXerror(double[] value){
        this.xerror = value;
    }
    
    /**
     * Set x error data
     * @param value X error data
     */
    public void setXerror(double value){
        this.xerror = new double[this.dataLength()];
        for (int i = 0; i < this.xerror.length; i++){
            this.xerror[i] = value;
        }
    }
    
    /**
     * Set X error data
     * @param value X error data 
     */
    public void setXerror(List<Number> value){
        this.xerror = new double[value.size()];
        double v;
        for (int i = 0; i < value.size(); i++){
            v = value.get(i).doubleValue();
            if (Double.isNaN(v))
                xerror[i] = this.getMissingValue();
            else
                xerror[i] = v;
        }
    }
    
    /**
     * Set X error data
     * @param value X error data 
     */
    public void setXerror(Array value){
        this.xerror = new double[(int)value.getSize()];
        double v;
        for (int i = 0; i < xerror.length; i++){
            v = value.getDouble(i);
            if (Double.isNaN(v))
                xerror[i] = this.getMissingValue();
            else
                xerror[i] = v;
        }
    }
    
    /**
     * Get X upper error data
     * @return X upper error data
     */
    public double[] getXerror_upper(){
        return this.xerror_upper;
    }
    
    /**
     * Set X upper error data
     * @param value X upper error data
     */
    public void setXerror_upper(double[] value){
        this.xerror_upper = value;
    }
    
    /**
     * Get Y error data
     * @return Y error data
     */
    public double[] getYerror(){
        return this.yerror;
    }
    
    /**
     * Set Y error data
     * @param value Y error data
     */
    public void setYerror(double[] value){
        this.yerror = value;
    }
    
    /**
     * Set y error data
     * @param value Y error data
     */
    public void setYerror(double value){
        this.yerror = new double[this.dataLength()];
        for (int i = 0; i < this.yerror.length; i++){
            this.yerror[i] = value;
        }
    }
    
    /**
     * Set Y error data
     * @param value Y error data 
     */
    public void setYerror(List<Number> value){
        this.yerror = new double[value.size()];
        double v;
        for (int i = 0; i < value.size(); i++){
            v = value.get(i).doubleValue();
            if (Double.isNaN(v))
                yerror[i] = this.getMissingValue();
            else
                yerror[i] = v;
        }
    }
    
    /**
     * Set Y error data
     * @param value Y error data 
     */
    public void setYerror(Array value){
        this.yerror = new double[(int)value.getSize()];
        double v;
        for (int i = 0; i < yerror.length; i++){
            v = value.getDouble(i);
            if (Double.isNaN(v))
                yerror[i] = this.getMissingValue();
            else
                yerror[i] = v;
        }
    }
    
    /**
     * Get Y upper error data
     * @return Y upper error data
     */
    public double[] getYerror_upper(){
        return this.yerror_upper;
    }
    
    /**
     * Set Y upper error data
     * @param value Y upper error data
     */
    public void setYerror_upper(double[] value){
        this.yerror_upper = value;
    }
    
    /**
     * Get Bottom data
     * @return Bottom data
     */
    public double[] getBottom(){
        return this.bottom;
    }
    
    /**
     * Set bottom
     * @param value Fixed bottom value 
     */
    public void setBottom(double value){
        this.bottom = new double[this.dataLength()];
        for (int i = 0; i < this.bottom.length; i++){
            this.bottom[i] = value;
        }
    }
    
    /**
     * Set bottom value
     * @param value Bottom value
     */
    public void setBottom(double[] value){
        this.bottom = value;
    }
    
    /**
     * Set bottom data
     * @param value Bottom data 
     */
    public void setBottom(List<Number> value){
        this.bottom = new double[value.size()];
        double v;
        for (int i = 0; i < value.size(); i++){
            v = value.get(i).doubleValue();
            if (Double.isNaN(v))
                bottom[i] = this.getMissingValue();
            else
                bottom[i] = v;
        }
    }
    
    /**
     * Set bottom data
     * @param value Bottom data 
     */
    public void setBottom(Array value){
        this.bottom = new double[(int)value.getSize()];
        double v;
        for (int i = 0; i < bottom.length; i++){
            v = value.getDouble(i);
            if (Double.isNaN(v))
                bottom[i] = this.getMissingValue();
            else
                bottom[i] = v;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get x error
     * @param idx Index
     * @return X error
     */
    public double getXerror(int idx){
        return this.xerror[idx];
    }
    
    /**
     * Get y error
     * @param idx Index
     * @return Y error
     */
    public double getYerror(int idx){
        return this.yerror[idx];
    }
    
    /**
     * Get x + error value
     * @param idx Index
     * @return X + error value
     */
    @Override
    public double getX_max(int idx){
        double v = this.getX(idx);
        if (this.xerror != null){
            v += this.xerror[idx];
        }
        return v;
    }
    
    /**
     * Get x - error value
     * @param idx Index
     * @return X - error value
     */
    @Override
    public double getX_min(int idx){
        double v = this.getX(idx);
        if (this.xerror != null){
            v -= this.xerror[idx];
        }
        return v;
    }
    
    /**
     * Get y + error value
     * @param idx Index
     * @return Y + error value
     */
    @Override
    public double getY_max(int idx){
        double v = this.getY(idx);
        if (this.yerror != null){
            v += this.yerror[idx];
        }
        return v;
    }
    
    /**
     * Get y - error value
     * @param idx Index
     * @return Y - error value
     */
    @Override
    public double getY_min(int idx){
        double v = this.getY(idx);
        if (this.yerror != null){
            v -= this.yerror[idx];
        }
        return v;
    }
    
    /**
     * Get a bottom value
     * @param idx Index
     * @return A bottom value
     */
    public double getBottom(int idx){
        return this.bottom[idx];
    }
    // </editor-fold>
}
