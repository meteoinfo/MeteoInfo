/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.shape.PointZ;

/**
 *
 * @author yaqiang
 */
public class IsosurfaceGraphics extends GraphicCollection3D {
    private List<PointZ[]> triangles = new ArrayList<>();
    
    /**
     * Constructor
     */
    public IsosurfaceGraphics() {
        this.allTriangle = true;
    }
    
    /**
     * Get triangles
     * @return Triangles
     */
    public List<PointZ[]> getTriangles() {
        return this.triangles;
    }
    
    /**
     * Set triangles
     * @param value Triangles 
     */
    public void setTriangles(List<PointZ[]> value) {
        this.triangles = value;
    }
    
    /**
     * Add a triangle
     * @param triangle Triangle 
     */
    public void addTriangle(PointZ[] triangle) {
        this.triangles.add(triangle);
        Extent3D extent = MIMath.getExtent(triangle);        
        if (this.triangles.size() == 1)
            this.setExtent(extent);
        else
            this.setExtent(MIMath.getLagerExtent(extent, this.getExtent()));
    }
}
