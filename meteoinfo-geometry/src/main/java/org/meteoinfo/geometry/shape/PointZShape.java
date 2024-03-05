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
package org.meteoinfo.geometry.shape;

import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.Geometry;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.PointD;

import java.util.ArrayList;
import java.util.List;

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
        this(new PointZ());
    }

    /**
     * Constructor
     * @param point The PointD or PointZ object
     */
    public PointZShape(PointD point) {
        setPoint(point);
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public PointZShape(Geometry geometry) {
        CoordinateXYZM c = (CoordinateXYZM)geometry.getCoordinate();
        this.setPoint(new PointZ(c.x, c.y, c.getZ(), c.getM()));
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.POINT_Z;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set point
     * 
     * @param aPoint point
     */
    @Override
    public void setPoint(PointD point) {
        PointZ p;
        if (this.points == null) {
            p = new PointZ();
            this.points = new ArrayList<>();
            ((List<PointZ>)this.points).add(p);
        } else {
            p = (PointZ) this.getPoint();
        }
        p.X = point.X;
        p.Y = point.Y;
        if (point instanceof PointZ){
            p.Z = ((PointZ) point).Z;
        }

        this.updateExtent();
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
