/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import java.util.List;
import ucar.ma2.Array;

/**
 *
 * @author Yaqiang Wang
 */
public class PolylineErrorShape extends PolylineShape {
    // <editor-fold desc="Variables">
    private double[] xerror;
    private double[] yerror;
    // </editor-fold>
    // <editor-fold desc="Constructor">
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
        this.xerror = new double[this.getPointNum()];
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
            xerror[i] = v;
        }
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
        this.yerror = new double[this.getPointNum()];
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
            yerror[i] = v;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.PolylineError;
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
     * Get x error
     * @param idx Index
     * @return X error
     */
    public double getXerror(int idx){
        return this.xerror[idx];
    }        
    
    /**
     * Update extent
     */
    public void updateExtent(){
        double min = 0, max = 0, v;
        if (this.xerror != null){
            for (int i = 0; i < this.xerror.length; i++){
                v = this.xerror[i];
                if (i == 0){
                    min = v;
                    max = v;
                } else {
                    if (v < min)
                        min = v;
                    else if (v > max)
                        max = v;
                }
            }
            this.getExtent().minX -= max;
            this.getExtent().maxX += max;
        }
        if (this.yerror != null){
            for (int i = 0; i < this.yerror.length; i++){
                v = this.yerror[i];
                if (i == 0){
                    min = v;
                    max = v;
                } else {
                    if (v < min)
                        min = v;
                    else if (v > max)
                        max = v;
                }
            }
            this.getExtent().minY -= max;
            this.getExtent().maxY += max;
        }
    }
    // </editor-fold>
}
