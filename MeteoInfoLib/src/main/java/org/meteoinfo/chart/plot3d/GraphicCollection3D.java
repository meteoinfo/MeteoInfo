/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot3d;

import java.util.List;
import org.meteoinfo.shape.GraphicCollection;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicCollection3D extends GraphicCollection{
    
    private boolean fixZ;
    private double zValue;
    private String zdir;
    private List<Number> sePoint;
    
    /**
     * Constructor
     */
    public GraphicCollection3D(){
        super();
        fixZ = false;
        zdir = "z";
        sePoint = null;
    }
    
    /**
     * Get if is 3D
     * @return Boolean
     */
    @Override
    public boolean is3D(){
        return true;
    }
    
    /**
     * Get if is fixed z graphics
     * @return Boolean
     */
    public boolean isFixZ(){
        return this.fixZ;
    }
    
    /**
     * Set if is fixed z graphics
     * @param value Boolean
     */
    public void setFixZ(boolean value){
        this.fixZ = value;
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
    
    /**
     * Get start & end points [xstart, ystart, xend, yend]
     * @return Start & end points
     */
    public List<Number> getSEPoint(){
        return this.sePoint;
    }
    
    /**
     * Set start & end points
     * @param value Start & end points
     */
    public void setSEPoint(List<Number> value){
        this.sePoint = value;
    }
}
