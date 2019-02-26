/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot3d;

import org.meteoinfo.shape.ImageShape;

/**
 *
 * @author Yaqiang Wang
 */
public class ImageShape3D extends ImageShape {
    private double zValue;
    private String zdir;
    
    /**
     * Constructor
     */
    public ImageShape3D(){
        super();
        this.zValue = 0;
        this.zdir = "z";
    }
    
    /**
     * Get fixed z value
     * @return Fixed z value
     */
    public double getZValue(){
        return this.zValue;
    }
    
    /**
     * Set fixed z value
     * @param value Fixed z value
     */
    public void setZValue(double value){
        this.zValue = value;
    }
    
    /**
     * Get z direction - x, y or z
     * @return Z direction
     */
    public String getZDir(){
        return this.zdir;
    }
    
    /**
     * Set z direction - x, y or z
     * @param value Z direction
     */
    public void setZDir(String value){
        this.zdir = value;
    }
}
