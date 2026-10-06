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

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.common.PointZ;

/**
 * Point shape class
 * 
 * @author Yaqiang Wang
 */
public class PointShape extends Shape implements Cloneable{
    // <editor-fold desc="Variables">

    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PointShape(){
        this(new PointZ());
    }

    /**
     * Constructor
     * @param point The point
     */
    public PointShape(PointZ point) {
        this.setPoint(point);
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public PointShape(Geometry geometry) {
        Coordinate c = geometry.getCoordinate();
        this.setPoint(new PointZ(c.x, c.y, c.getZ()));
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.POINT;
    }
    
    /**
     * To geometry method
     * @param factory GeometryFactory
     * @return Geometry
     */
    @Override
    public Geometry toGeometry(GeometryFactory factory){
        PointZ point = this.getPoint();
        Coordinate c = new Coordinate(point.X, point.Y, point.Z);
        return factory.createPoint(c);
    };

    /**
     * Get point
     * 
     * @return point
     */
    public PointZ getPoint() {
        return this.points.get(0);
    }

    /**
     * Set point
     * 
     * @param point Point
     */
    public void setPoint(PointZ point) {
        this.points = new ArrayList<>();
        this.points.add(point);
        updateExtent();
    }

    /**
     * Get M value
     * @return M value
     */
    public double getM(){
        return (this.getPoint()).M;
    }

    /**
     * Get Z value
     * @return Z value
     */
    public double getZ(){
        return (this.getPoint()).Z;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     *
     * @return PolygonShape
     */
    @Override
    public Object clone() {
        PointShape ps = new PointShape();
        ps.setValue(this.getValue());
        ps.setPoint((PointZ) this.getPoint().clone());
        ps.setVisible(this.isVisible());
        ps.setSelected(this.isSelected());
        ps.setLegendIndex(this.getLegendIndex());
        
        return ps;
    }
    // </editor-fold>
}
