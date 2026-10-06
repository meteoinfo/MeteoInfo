/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.GeometryUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class IsosurfaceGraphics extends GraphicCollection3D {
    private List<Coordinate[]> triangles = new ArrayList<>();
    
    /**
     * Constructor
     */
    public IsosurfaceGraphics() {
        super();
        this.allTriangle = true;
    }
    
    /**
     * Get triangles
     * @return Triangles
     */
    public List<Coordinate[]> getTriangles() {
        return this.triangles;
    }
    
    /**
     * Set triangles
     * @param value Triangles 
     */
    public void setTriangles(List<Coordinate[]> value) {
        this.triangles = value;
        updateExtent();
    }
    
    /**
     * Add a triangle
     * @param triangle Triangle 
     */
    public void addTriangle(Coordinate[] triangle) {
        this.triangles.add(triangle);
        Extent extent = GeometryUtil.getExtent(triangle);
        if (this.triangles.size() == 1)
            this.setExtent(extent);
        else
            this.setExtent(GeometryUtil.getLagerExtent(extent, this.getExtent()));
    }

    /**
     * Update extent
     */
    public void updateExtent() {
        Extent extent;
        for (int i = 0; i < this.triangles.size(); i++) {
            extent = GeometryUtil.getExtent(this.triangles.get(i));
            if (i == 0)
                this.setExtent(extent);
            else
                this.setExtent(GeometryUtil.getLagerExtent(extent, this.getExtent()));
        }
    }
}
