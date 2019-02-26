/* This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.chart;

import java.awt.Point;
import org.meteoinfo.chart.plot3d.Projector;
import org.meteoinfo.data.DataMath;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.shape.PointZ;

/**
 *
 * @author Yaqiang Wang
 * yaqiang.wang@gmail.com
 */
public class ChartText3D extends ChartText {
    private double z;
    private PointZ zdir = null;    
    
    /**
     * Get z coordinate value
     * @return Z coordinate value
     */
    public double getZ(){
        return this.z;
    }
    
    /**
     * Set z coordinate value
     * @param value Z coordinate value
     */
    public void setZ(double value){
        this.z = value;
    }
    
    /**
     * Get zdir point
     * @return ZDir point
     */
    public PointZ getZDir(){
        return zdir;
    }
    
    /**
     * Set zdir point
     * @param value ZDir point
     */
    public void setZDir(PointZ value){
        this.zdir = value;
    }
    
    /**
     * Set zdir point
     * @param x X coordinate value
     * @param y Y coordinate value
     * @param z Z coordinate value
     */
    public void setZDir(float x, float y, float z){
        if (x == 0 && y == 0 && z == 0)
            this.zdir = null;
        else
            this.zdir = new PointZ(x, y, z);
    }
    
    /**
     * Set zdir point
     * @param value ZDir point
     */
    public void setZDir(String value){
        float x1 = 0, y1 = 0, z1 = 0;
        switch(value.toLowerCase()){
            case "x":
                x1 = 1;
                break;
            case "y":
                y1 = 1;
                break;
            case "z":
                z1 = 1;
                break;
        }
        this.setZDir(x1, y1, z1);
    }
    
    /**
     * Set point
     * 
     * @param x X
     * @param y Y
     * @param z Z
     */
    public void setPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        Extent3D aExtent = new Extent3D();
        aExtent.minX = x;
        aExtent.maxX = x;
        aExtent.minY = y;
        aExtent.maxY = y;
        aExtent.minZ = z;
        aExtent.maxZ = z;
        this.setExtent(aExtent);
    }
    
    /**
     * Update angle
     * @param projector Projector
     */
    public void updateAngle(Projector projector){
        if (this.zdir == null)
            return;
        
        Point p0 = projector.project(0, 0, 0);
        Point p1 = projector.project((float)this.zdir.X, (float)this.zdir.Y, (float)this.zdir.Z);
        double[] value = DataMath.getDSFromUV(p1.x - p0.x, p1.y - p0.y);
        this.angle = (float)value[0];
    }
}
