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
package org.meteoinfo.geometry;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;

/**
 * Point shape class
 * 
 * @author Yaqiang Wang
 */
public class Point extends Geometry implements Cloneable{
    // <editor-fold desc="Variables">
    protected Coordinate coordinate;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Point(){
        this(new Coordinate());
    }

    /**
     * Constructor
     * @param point The point
     */
    public Point(Coordinate point) {
        this.setCoordinate(point);
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public Point(org.locationtech.jts.geom.Geometry geometry) {
        org.locationtech.jts.geom.Coordinate c = geometry.getCoordinate();
        this.setCoordinate(new Coordinate(c.x, c.y, c.getZ()));
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    @Override
    public GeometryType getGeometryType(){
        return GeometryType.POINT;
    }
    
    /**
     * To geometry method
     * @param factory GeometryFactory
     * @return Geometry
     */
    @Override
    public org.locationtech.jts.geom.Geometry toJTSGeometry(GeometryFactory factory){
        Coordinate point = this.getCoordinate();
        org.locationtech.jts.geom.Coordinate c = new org.locationtech.jts.geom.Coordinate(point.x, point.y, point.z);
        return factory.createPoint(c);
    };

    /**
     * Get coordinate
     * 
     * @return Coordinate
     */
    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    @Override
    public List<Coordinate> getCoordinates() {
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(this.coordinate);
        return coordinates;
    }

    @Override
    public void updateExtent() {
        this.extent = new Extent(this.coordinate);
    }

    @Override
    public int getNumPoints() {
        return 1;
    }

    /**
     * Set coordinate
     * 
     * @param coordinate Coordinate
     */
    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
        updateExtent();
    }

    /**
     * Get x value
     * @return x value
     */
    public double getX(){
        return coordinate.x;
    }

    /**
     * Get y value
     * @return y value
     */
    public double getY(){
        return coordinate.y;
    }

    /**
     * Get M value
     * @return M value
     */
    public double getM(){
        return coordinate.m;
    }

    /**
     * Get Z value
     * @return Z value
     */
    public double getZ(){
        return coordinate.z;
    }

    @Override
    public boolean isEmpty() {
        return coordinate == null;
    }

    @Override
    public Geometry reverse() {
        return this;
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
        Point ps = new Point();
        ps.setValue(this.getValue());
        ps.setCoordinate((Coordinate) this.getCoordinate().clone());
        ps.setVisible(this.isVisible());
        ps.setSelected(this.isSelected());
        ps.setLegendIndex(this.getLegendIndex());
        
        return ps;
    }
    // </editor-fold>
}
