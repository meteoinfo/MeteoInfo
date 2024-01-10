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
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.data.DataMath;
import org.meteoinfo.geometry.shape.PointZ;

/**
 *
 * @author Yaqiang Wang
 * yaqiang.wang@gmail.com
 */
public class ChartText3D extends ChartText {
    private double z;
    private PointZ zdir = null;
    private boolean draw3D = false;
    
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
     * Get whether draw text at 3D location
     * @return Boolean
     */
    public boolean isDraw3D() {
        return this.draw3D;
    }

    /**
     * Set whether draw text at 3D location
     * @param value Boolean
     */
    public void setDraw3D(boolean value) {
        this.draw3D = value;
    }

    /**
     * Get point
     *
     * @return The point coordinates
     */
    public PointZ getPoint() {
        return new PointZ(x, y, z);
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
     * Set point
     *
     * @param point The point
     */
    public void setPoint(PointZ point) {
        setPoint(point.X, point.Y, point.Z);
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

    /**
     * Clone
     *
     * @return Cloned object
     */
    @Override
    public Object clone() {
        ChartText3D ct = new ChartText3D();
        ct.angle = this.angle;
        ct.background = this.background;
        ct.color = this.color;
        ct.coordinates = this.coordinates;
        ct.drawBackground = this.drawBackground;
        ct.drawNeatline = this.drawNeatline;
        ct.font = this.font;
        ct.gap = this.gap;
        ct.lineSpace = this.lineSpace;
        ct.neatLineColor = this.neatLineColor;
        ct.neatLineSize = this.neatLineSize;
        ct.text = this.text;
        ct.useExternalFont = this.useExternalFont;
        ct.x = this.x;
        ct.xAlign = this.xAlign;
        ct.y = this.y;
        ct.yAlign = this.yAlign;
        ct.z = this.z;
        ct.zdir = this.zdir;

        return ct;
    }
}
