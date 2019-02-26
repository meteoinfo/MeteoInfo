/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.shape;

import org.meteoinfo.global.Extent3D;
import org.meteoinfo.global.PointD;
import org.meteoinfo.jts.geom.Coordinate;
import org.meteoinfo.jts.geom.Geometry;

/**
 *
 * @author yaqiang
 */
public class PointZShape extends PointShape {
    // <editor-fold desc="Variables">

    //private PointZ _point = new PointZ();
    //private double z;
    //private double m;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public PointZShape(){
        this.point = new PointZ();
        this.updateExtent((PointZ)this.point);
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public PointZShape(Geometry geometry) {
        Coordinate c = geometry.getCoordinate();
        this.setPoint(new PointZ(c.x, c.y, c.z, c.m));
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.PointZ;
    }

//    /**
//     * Get point
//     *
//     * @return Point
//     */
//    @Override
//    public PointZ getPoint() {
//        return _point;
//    }
//
//    /**
//     * Set point
//     *
//     * @param point Point
//     */
//    public void setPoint(PointZ point) {
//        _point = point;
//    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set point
     * 
     * @param aPoint point
     */
    @Override
    public void setPoint(PointD aPoint) {
        if (aPoint instanceof PointZ){
            this.point = aPoint;
        } else {
            this.point.X = aPoint.X;
            this.point.Y = aPoint.Y;  
        }
        this.updateExtent((PointZ)this.point);
    }
    
    /**
     * Set point
     * @param p PointZ
     */
    public void setPoint(PointZ p){
        this.point = p;
        this.updateExtent(p);
    }
    
    /**
     * Update extent
     * @param p PointZ
     */
    public void updateExtent(PointZ p){
        Extent3D aExtent = new Extent3D();
        aExtent.minX = p.X;
        aExtent.maxX = p.X;
        aExtent.minY = p.Y;
        aExtent.maxY = p.Y;
        aExtent.minZ = p.Z;
        aExtent.maxZ = p.Z;
        this.setExtent(aExtent);
    }
    
    /**
     * Get M value
     * @return M value
     */
    public double getM(){
        return ((PointZ)this.getPoint()).M;
    }
    
    /**
     * Get Z value
     * @return Z value
     */
    public double getZ(){
        return ((PointZ)this.getPoint()).Z;
    }
    
    /**
     * Clone
     *
     * @return PolylineZShape object
     */
    @Override
    public Object clone() {
        PointZShape aPS = new PointZShape();
        //aPS = (PointZShape)base.Clone();
        aPS.setPoint((PointZ)this.getPoint().clone());
        //aPS.Z = Z;
        //aPS.M = M;
        aPS.setValue(getValue());
        aPS.setVisible(this.isVisible());
        aPS.setSelected(this.isSelected());
        aPS.setLegendIndex(this.getLegendIndex());

        return aPS;
    }
    // </editor-fold>
}
