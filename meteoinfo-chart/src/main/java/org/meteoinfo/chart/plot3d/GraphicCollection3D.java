/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot3d;

import org.meteoinfo.chart.graphic.GraphicCollection;

import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicCollection3D extends GraphicCollection{
    
    private boolean fixZ;
    private double zValue;
    private String zdir;
    private List<Number> sePoint;
    protected boolean allQuads;
    protected boolean allTriangle;
    protected boolean allConvexPolygon;
    protected boolean usingLight;
    
    /**
     * Constructor
     */
    public GraphicCollection3D(){
        super();
        fixZ = false;
        zdir = "z";
        sePoint = null;
        allQuads = false;
        allTriangle = false;
        allConvexPolygon = false;
        usingLight = true;
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
     * Get start and end points [xstart, ystart, xend, yend]
     * @return Start and end points
     */
    public List<Number> getSEPoint(){
        return this.sePoint;
    }
    
    /**
     * Set start and end points
     * @param value Start and end points
     */
    public void setSEPoint(List<Number> value){
        this.sePoint = value;
    }
    
    /**
     * Get is all quads or not
     * @return All quads or not
     */
    public boolean isAllQuads() {
        return this.allQuads;
    }
    
    /**
     * Set is all quads or not
     * @param value All quads or not
     */
    public void setAllQuads(boolean value) {
        this.allQuads = value;
    }
    
    /**
     * Get is all triangle or not
     * @return All triangle or not
     */
    public boolean isAllTriangle() {
        return this.allTriangle;
    }
    
    /**
     * Set is all triangle or not
     * @param value All triangle or not
     */
    public void setAllTriangle(boolean value) {
        this.allTriangle = value;
    }
    
    /**
     * Get is all convex polygon or not
     * @return All convex polygon or not
     */
    public boolean isAllConvexPolygon() {
        if (this.allConvexPolygon) {
            return true;
        } else {
            return this.allQuads || this.allTriangle;
        }
    }
    
    /**
     * Set is all convex polygon or not
     * @param value All convex polygon or not
     */
    public void setAllConvexPolygon(boolean value) {
        this.allConvexPolygon = value;
    }

    /**
     * Get using light or not
     * @return Boolean
     */
    public boolean isUsingLight() {
        return this.usingLight;
    }

    /**
     * Set using light or not
     * @param value Boolean
     */
    public void setUsingLight(boolean value) {
        this.usingLight = value;
    }
}
