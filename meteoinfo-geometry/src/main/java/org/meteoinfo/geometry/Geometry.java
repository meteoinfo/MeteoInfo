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
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.locationtech.jts.operation.union.UnaryUnionOp;

/**
 * Shape class
 *
 * @author Yaqiang Wang
 */
public abstract class Geometry implements Cloneable{
    // <editor-fold desc="Variables">

    //protected List<Coordinate> coordinates;
    protected boolean visible;
    protected boolean selected;
    private boolean editing;
    protected Extent extent;
    protected int legendIndex = 0;
    protected double value;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Geometry() {
        visible = true;
        editing = false;
        selected = false;
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public Geometry(org.locationtech.jts.geom.Geometry geometry) {};

    /**
     * Create a new Shape object
     * @param shapeType Shape type
     * @return Shape object
     */
    public static Geometry factory(GeometryType shapeType) {
        switch (shapeType) {
            case POINT:
                return new Point();
            case LINESTRING:
                return new MultiLineString();
            case POLYGON:
                return new MultiPolygon();
            default:
                throw new IllegalArgumentException("Shape type not supported");
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get shape type
     *
     * @return Shape type
     */
    public abstract GeometryType getGeometryType();

    /**
     * Get if visible
     *
     * @return is visible or not
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set visible
     *
     * @param isTrue True or not
     */
    public void setVisible(boolean isTrue) {
        visible = isTrue;
    }

    /**
     * Get coordinate
     * @return Coordinate
     */
    public abstract Coordinate getCoordinate();

    /**
     * Get coordinates
     * @return The coordinates
     */
    public abstract List<Coordinate> getCoordinates();

    public void setCoordinates(List<Coordinate> coordinates) {}

    /**
     * Get if the shape is selected
     *
     * @return Boolean
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set selected
     *
     * @param isTrue True or not
     */
    public void setSelected(boolean isTrue) {
        selected = isTrue;
    }

    /**
     * Get if editing
     *
     * @return Boolean
     */
    public boolean isEditing() {
        return editing;
    }

    /**
     * Set if editing
     *
     * @param value Boolean
     */
    public void setEditing(boolean value) {
        editing = value;
    }

    /**
     * Get extent
     *
     * @return extent Extent
     */
    public Extent getExtent() {
        if (extent == null) {
            this.updateExtent();
        }
        return extent;
    }

    /**
     * Set extent
     *
     * @param aExtent Extent
     */
    public void setExtent(Extent aExtent) {
        extent = aExtent;
    }

    /**
     * Get legend index
     *
     * @return Legend index
     */
    public int getLegendIndex() {
        return legendIndex;
    }

    /**
     * Set legend index
     *
     * @param value Legend index
     */
    public void setLegendIndex(int value) {
        legendIndex = value;
    }
    
    /**
     * Get value
     * @return Value
     */
    public double getValue(){
        return this.value;
    }
    
    /**
     * Set value
     * @param value Value
     */
    public void setValue(double value){
        this.value = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Update extent
     */
    public abstract void updateExtent();

    /**
     * Returns the number of {@link Geometry}s in a {@link GeometryCollection}
     * (or 1, if the geometry is not a collection).
     *
     * @return the number of geometries contained in this geometry
     */
    public int getNumGeometries() {
        return 1;
    }

    /**
     * Returns an element {@link Geometry} from a {@link GeometryCollection}
     * (or <code>this</code>, if the geometry is not a collection).
     *
     * @param n the index of the geometry element
     * @return the n'th geometry contained in this geometry
     */
    public Geometry getGeometryN(int n) {
        return this;
    }

    /**
     * Get point number
     * @return Point number
     */
    public abstract int getNumPoints();

    public int getNumParts() {
        return getNumGeometries();
    }

    /**
     * Tests whether the set of points covered by this <code>Geometry</code> is
     * empty.
     * <p>
     * Note this test is for topological emptiness,
     * not structural emptiness.
     * A collection containing only empty elements is reported as empty.
     * To check structural emptiness use {@link #getNumGeometries()}.
     *
     *@return <code>true</code> if this <code>Geometry</code> does not cover any points
     */
    public abstract boolean isEmpty();

    /**
     * Computes a new geometry which has all component coordinate sequences
     * in reverse order (opposite orientation) to this one.
     *
     * @return a reversed geometry
     */
    public abstract Geometry reverse();

    /**
     * Returns true if the array contains any non-empty <code>Geometry</code>s.
     *
     *@param  geometries  an array of <code>Geometry</code>s; no elements may be
     *      <code>null</code>
     *@return             <code>true</code> if any of the <code>Geometry</code>s
     *      <code>isEmpty</code> methods return <code>false</code>
     */
    protected static boolean hasNonEmptyElements(Geometry[] geometries) {
        for (int i = 0; i < geometries.length; i++) {
            if (!geometries[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Returns true if the array contains any <code>null</code> elements.
     *
     *@param  array  an array to validate
     *@return        <code>true</code> if any of <code>array</code>s elements are
     *      <code>null</code>
     */
    protected static boolean hasNullElements(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * To geometry method
     * @param factory GeometryFactory
     * @return Geometry
     */
    public abstract org.locationtech.jts.geom.Geometry toJTSGeometry(GeometryFactory factory);
    
    /**
     * To geometry method
     * @return Geometry
     */
    public org.locationtech.jts.geom.Geometry toJTSGeometry(){
        return toJTSGeometry(new GeometryFactory());
    };
    
    public static Geometry fromJTSGeometry(org.locationtech.jts.geom.Geometry geo){
        switch (geo.getGeometryType()){
            case "Point":
            case "MultiPoint":
                if (geo.getNumPoints() < 1)
                    return null;
                else if (geo.getNumPoints() == 1)
                    return new Point(geo);
                else {
                    Point[] pointShapes = new Point[geo.getNumPoints()];
                    for (int i = 0; i < geo.getNumPoints(); i++) {
                        pointShapes[i] = new Point(geo.getGeometryN(i));
                    }
                    return new MultiPoint(pointShapes);
                }
            case "LineString":
            case "MultiLineString":
                if (geo.getNumPoints() < 2)
                    return null;
                else
                    return new MultiLineString(geo);
            case "Polygon":
            case "MultiPolygon":
                if (geo.getNumPoints() < 3)
                    return null;
                else
                    return new MultiPolygon(geo);
            default:
                return null;
        }
    }
    
    /**
     * Get intersection shape
     * @param b Other shape
     * @return Intersection shape
     */
    public Geometry intersection(Geometry b){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = b.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g3 = g1.intersection(g2);
        return fromJTSGeometry(g3);
    }
    
    /**
     * Get union shape
     * @param b Other shape
     * @return Union shape
     */
    public Geometry union(Geometry b){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = b.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g3 = g1.union(g2);
        return fromJTSGeometry(g3);
    }
    
    /**
     * Get difference shape
     * @param b Other shape
     * @return Difference shape
     */
    public Geometry difference(Geometry b){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = b.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g3 = g1.difference(g2);
        return fromJTSGeometry(g3);
    }
    
    /**
     * Get system difference shape
     * @param b Other shape
     * @return System difference shape
     */
    public Geometry symDifference(Geometry b){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = b.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g3 = g1.symDifference(g2);
        return fromJTSGeometry(g3);
    }
    
    /**
     * Get buffer shape
     * @param distance Distance
     * @return Buffered shape
     */
    public Geometry buffer(double distance){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g3 = g1.buffer(distance);
        return fromJTSGeometry(g3);
    }
    
    /**
     * Get convexhull shape
     * @return Convexhull shape
     */
    public Geometry convexHull(){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g3 = g1.convexHull();
        return fromJTSGeometry(g3);
    }
    
    /**
     * Split shape
     * @param line Split line
     * @return Splitted shapes
     */
    public List<Geometry> split(Geometry line){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = line.toJTSGeometry();
        if (this.getGeometryType().isPolygon()){
            Polygonizer polygonizer = new Polygonizer();
            org.locationtech.jts.geom.Geometry polygons = g1.getBoundary().union(g2);
            polygonizer.add(polygons);
            List<org.locationtech.jts.geom.Geometry> polys = (List)polygonizer.getPolygons();
            List<Geometry> polyShapes = new ArrayList<>();
            for (int i = 0; i < polys.size(); i++){
                org.locationtech.jts.geom.Polygon poly = (org.locationtech.jts.geom.Polygon)polys.get(i);
                if (poly.getInteriorPoint().within(g1))
                    polyShapes.add(new MultiPolygon(poly));
            }
            return polyShapes;
        } else if (this.getGeometryType().isLine()){
            org.locationtech.jts.geom.Geometry ugeo = g1.union(g2);
            List<Geometry> lineShapes = new ArrayList<>();
            for (int i = 0; i < ugeo.getNumGeometries(); i++){
                org.locationtech.jts.geom.Geometry geo = ugeo.getGeometryN(i);
                if (geo.buffer(0.001).within(g1.buffer(0.0011)))
                    lineShapes.add(new MultiLineString(geo));
            }
            return lineShapes;
        }
        
        return null;
    }
    
    /**
     * Reform the shape by a line
     * @param line The line
     * @return Result shape
     */
    public Geometry reform(Geometry line){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = line.toJTSGeometry();
        if (this.getGeometryType().isPolygon()){
            Polygonizer polygonizer = new Polygonizer();
            org.locationtech.jts.geom.Geometry polygons = g1.getBoundary().union(g2);
            polygonizer.add(polygons);
            List<org.locationtech.jts.geom.Geometry> polys = (List)polygonizer.getPolygons();
            org.locationtech.jts.geom.Geometry mbgeo;
            org.locationtech.jts.geom.Polygon poly1 = (org.locationtech.jts.geom.Polygon)g1;
            if (poly1.getNumInteriorRing() == 0){
                mbgeo = CascadedPolygonUnion.union(polys);
            } else {
                GeometryFactory factory = new GeometryFactory();
                org.locationtech.jts.geom.Polygon shell = factory.createPolygon((LinearRing)poly1.getExteriorRing());
                List<org.locationtech.jts.geom.Geometry> npolys = new ArrayList<>();
                for (int i = 0; i < polys.size(); i++){
                    org.locationtech.jts.geom.Polygon poly = (org.locationtech.jts.geom.Polygon)polys.get(i);
                    if (poly.getInteriorPoint().within(g1))
                        npolys.add(poly);
                    else {
                        if (!poly.getInteriorPoint().within(shell))
                            npolys.add(poly);
                    }
                }
                mbgeo = CascadedPolygonUnion.union(npolys);
            }
            Geometry r = new MultiPolygon(mbgeo);
            return r;
        } else if (this.getGeometryType().isLine()){
            org.locationtech.jts.geom.Geometry ugeo = g1.union(g2);
            List<org.locationtech.jts.geom.Geometry> geos = new ArrayList<>();
            for (int i = 0; i < ugeo.getNumGeometries(); i++){
                org.locationtech.jts.geom.Geometry geo = ugeo.getGeometryN(i);
                org.locationtech.jts.geom.Coordinate c1 = geo.getCoordinates()[0];
                org.locationtech.jts.geom.Coordinate c2 = geo.getCoordinates()[geo.getNumPoints() - 1];
                if (c1.equals2D(g1.getCoordinates()[0]) ||
                    c1.equals2D(g1.getCoordinates()[g1.getNumPoints() - 1]) ||
                    c2.equals2D(g1.getCoordinates()[0]) ||
                    c2.equals2D(g1.getCoordinates()[g1.getNumPoints() - 1]))
                    geos.add(geo);
                else {
                    GeometryFactory factory = new GeometryFactory();
                    org.locationtech.jts.geom.Geometry p1 = factory.createPoint(c1);
                    org.locationtech.jts.geom.Geometry p2 = factory.createPoint(c2);
                    org.locationtech.jts.geom.Geometry buffer = g1.buffer(0.001);
                    if (p1.within(buffer) && p2.within(buffer)){
                        if (!geo.buffer(0.001).within(g1.buffer(0.0011)))
                            geos.add(geo);
                    }
                }
            }
            org.locationtech.jts.geom.Geometry geo = UnaryUnionOp.union(geos);
            return new MultiLineString(geo);
        }
        
        return null;
    }
    
    /**
     * Is shapes cross each other or not
     * @param other Other shape
     * @return Cross or not
     */
    public boolean crosses(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.crosses(g2);
    }
    
    /**
     * If this shape contains another one
     * @param other Other shape
     * @return Contains or not
     */
    public boolean contains(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.contains(g2);
    }
    
    /**
     * If this shape covered by another one
     * @param other Other shape
     * @return Covered by or not
     */
    public boolean coveredBy(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.coveredBy(g2);
    }
    
    /**
     * If this shape covers another one
     * @param other Other shape
     * @return Covers or not
     */
    public boolean covers(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.covers(g2);
    }
    
    /**
     * If this shape disjoint another one
     * @param other Other shape
     * @return Disjoint or not
     */
    public boolean disjoint(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.disjoint(g2);
    }
    
    /**
     * If this shape equals another one
     * @param other Other shape
     * @return Equals or not
     */
    public boolean equals(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.equals(g2);
    }
    
    /**
     * If this shape intersects another one
     * @param other Other shape
     * @return Intersects or not
     */
    public boolean intersects(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.intersects(g2);
    }
    
    /**
     * If this shape overlaps another one
     * @param other Other shape
     * @return Overlaps or not
     */
    public boolean overlaps(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.overlaps(g2);
    }
    
    /**
     * If this shape touches another one
     * @param other Other shape
     * @return Touches or not
     */
    public boolean touches(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.touches(g2);
    }
    
    /**
     * If this shape within another one
     * @param other Other shape
     * @return Within or not
     */
    public boolean within(Geometry other){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = other.toJTSGeometry();
        return g1.within(g2);
    }

    /**
     * Clone
     *
     * @return Shape object
     */
    @Override
    public Object clone() {
        Geometry o = null;
        try {
            o = (Geometry)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }
    
    /**
     * Clone value
     * @param other Other shape
     */
    public void cloneValue(Geometry other){};

    /**
     * Vertice edited update
     *
     * @param vIdx Vertice index
     * @param newX New X
     * @param newY New Y
     */
    public void moveVertice(int vIdx, double newX, double newY) {
        List<Coordinate> points = getCoordinates();
        if (this.getGeometryType().isPolygon()) {
            int last = points.size() - 1;
            if (vIdx == 0) {
                if (points.get(0).x == points.get(last).x && points.get(0).y == points.get(last).y) {
                    points.get(last).x = newX;
                    points.get(last).y = newY;
                }
            } else if (vIdx == last) {
                if (points.get(0).x == points.get(last).x && points.get(0).y == points.get(last).y) {
                    points.get(0).x = newX;
                    points.get(0).y = newY;
                }
            }
        }

        Coordinate aP = points.get(vIdx);
        aP.x = newX;
        aP.y = newY;
        setCoordinates(points);
    }

    /**
     * Move the shape
     * @param xShift X shift
     * @param yShift Y shift
     */
    public void move(double xShift, double yShift){
        List<Coordinate> points = this.getCoordinates();
        for (Coordinate aPoint : points) {
            aPoint.x += xShift;
            aPoint.y += yShift;
        }

        this.setCoordinates(points);
    }

    /**
     * Remove a vertice
     *
     * @param vIdx Vertice index
     */
    public void removeVertice(int vIdx) {
        List<Coordinate> coordinates = getCoordinates();
        coordinates.remove(vIdx);
        this.setCoordinates(coordinates);
        this.updateExtent();
    }

    /**
     * Add a vertice
     *
     * @param vIdx Vertice index
     * @param vertice The vertice
     */
    public void addVertice(int vIdx, Coordinate vertice) {
        List<Coordinate> coordinates = getCoordinates();
        coordinates.add(vIdx, vertice);
        this.setCoordinates(coordinates);
        this.updateExtent();
    }
    // </editor-fold>
}
