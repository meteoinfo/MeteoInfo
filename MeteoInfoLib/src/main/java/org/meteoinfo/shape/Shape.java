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

import java.util.ArrayList;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointD;
import java.util.List;
import org.meteoinfo.jts.geom.Coordinate;
import org.meteoinfo.jts.geom.Geometry;
import org.meteoinfo.jts.geom.GeometryFactory;
import org.meteoinfo.jts.geom.LinearRing;
import org.meteoinfo.jts.operation.polygonize.Polygonizer;
import org.meteoinfo.jts.operation.union.CascadedPolygonUnion;
import org.meteoinfo.jts.operation.union.UnaryUnionOp;

/**
 * Shape class
 *
 * @author Yaqiang Wang
 */
public abstract class Shape implements Cloneable{
    // <editor-fold desc="Variables">

    //private ShapeTypes _shapeType;
    private boolean _visible;
    private boolean _selected;
    private boolean editing;
    private Extent _extent = new Extent();
    private int _legendIndex = 0;
    private double value;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Contructor
     */
    public Shape() {
        //_shapeType = ShapeTypes.Point;
        _visible = true;
        editing = false;
        _selected = false;
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public Shape(Geometry geometry) {};

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get shape type
     *
     * @return Shape type
     */
    public abstract ShapeTypes getShapeType();

    /**
     * Get if visible
     *
     * @return is visible or not
     */
    public boolean isVisible() {
        return _visible;
    }

    /**
     * Set visible
     *
     * @param isTrue True or not
     */
    public void setVisible(boolean isTrue) {
        _visible = isTrue;
    }

    /**
     * Get if the shape is selected
     *
     * @return Boolean
     */
    public boolean isSelected() {
        return _selected;
    }

    /**
     * Set selected
     *
     * @param isTrue True or not
     */
    public void setSelected(boolean isTrue) {
        _selected = isTrue;
    }

    /**
     * Get if is editing
     *
     * @return Boolean
     */
    public boolean isEditing() {
        return editing;
    }

    /**
     * Set if is editing
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
        return _extent;
    }

    /**
     * Set extent
     *
     * @param aExtent Extent
     */
    public void setExtent(Extent aExtent) {
        _extent = aExtent;
    }

    /**
     * Get legend index
     *
     * @return Legend index
     */
    public int getLegendIndex() {
        return _legendIndex;
    }

    /**
     * Set legend index
     *
     * @param value Legend index
     */
    public void setLegendIndex(int value) {
        _legendIndex = value;
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
     * Get points
     *
     * @return point list
     */
    public List<? extends PointD> getPoints() {
        return null;
    }

    /**
     * Set points
     *
     * @param points point list
     */
    public void setPoints(List<? extends PointD> points) {
    }
    
    /**
     * Add a vertice
     * @param vIdx Vertice index
     * @param vertice The vertice
     */
    public void addVertice(int vIdx, PointD vertice){        
    }
    
    /**
     * Remove a vertice
     * @param vIdx Vertice index
     */
    public void removeVerice(int vIdx){        
    }

    /**
     * Vertice edited update
     *
     * @param vIdx Vertice index
     * @param newX New X
     * @param newY New Y
     */
    public void moveVertice(int vIdx, double newX, double newY) {
        List<PointD> points = (List<PointD>) getPoints();
        if (this.getShapeType().isPolygon()) {
            int last = points.size() - 1;
            if (vIdx == 0) {
                if (points.get(0).X == points.get(last).X && points.get(0).Y == points.get(last).Y) {
                    points.get(last).X = newX;
                    points.get(last).Y = newY;
                }
            } else if (vIdx == last) {
                if (points.get(0).X == points.get(last).X && points.get(0).Y == points.get(last).Y) {
                    points.get(0).X = newX;
                    points.get(0).Y = newY;
                }
            }
        }

        PointD aP = points.get(vIdx);
        aP.X = newX;
        aP.Y = newY;
        //points.set(vIdx, aP);
        setPoints(points);
    }
    
    /**
     * Move the shape
     * @param xShift X shift
     * @param yShift Y shift
     */
    public void move(double xShift, double yShift){
        List<PointD> points = (List<PointD>) this.getPoints();
        for (PointD aPoint : points) {
            aPoint.X += xShift;
            aPoint.Y += yShift;
        }

        this.setPoints(points);
    }
    
    /**
     * Reverse points direction
     */
    public void reverse(){        
    }
    
    /**
     * To geometry method
     * @param factory GeometryFactory
     * @return Geometry
     */
    public abstract Geometry toGeometry(GeometryFactory factory);
    
    /**
     * To geometry method
     * @return Geometry
     */
    public Geometry toGeometry(){
        return toGeometry(new GeometryFactory());
    };
    
    public static Shape geometry2Shape(Geometry geo){
        switch (geo.getGeometryType()){
            case "Point":
            case "MultiPoint":
                if (geo.getNumPoints() < 1)
                    return null;
                else
                    return new PointShape(geo);
            case "LineString":
            case "MultiLineString":
                if (geo.getNumPoints() < 2)
                    return null;
                else
                    return new PolylineShape(geo);
            case "Polygon":
            case "MultiPolygon":
                if (geo.getNumPoints() < 3)
                    return null;
                else
                    return new PolygonShape(geo);
            default:
                return null;
        }
    }
    
    /**
     * Get intersection shape
     * @param b Other shape
     * @return Intersection shape
     */
    public Shape intersection(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.intersection(g2);
        return geometry2Shape(g3);
    }
    
    /**
     * Get union shape
     * @param b Other shape
     * @return Union shape
     */
    public Shape union(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.union(g2);
        return geometry2Shape(g3);
    }
    
    /**
     * Get difference shape
     * @param b Other shape
     * @return Difference shape
     */
    public Shape difference(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.difference(g2);
        return geometry2Shape(g3);
    }
    
    /**
     * Get system difference shape
     * @param b Other shape
     * @return System difference shape
     */
    public Shape symDifference(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.symDifference(g2);
        return geometry2Shape(g3);
    }
    
    /**
     * Get buffer shape
     * @param distance Distance
     * @return Buffered shape
     */
    public Shape buffer(double distance){
        Geometry g1 = this.toGeometry();
        Geometry g3 = g1.buffer(distance);
        return geometry2Shape(g3);
    }
    
    /**
     * Get convexhull shape
     * @return Convexhull shape
     */
    public Shape convexHull(){
        Geometry g1 = this.toGeometry();
        Geometry g3 = g1.convexHull();
        return geometry2Shape(g3);
    }
    
    /**
     * Split shape
     * @param line Split line
     * @return Splitted shapes
     */
    public List<Shape> split(Shape line){
        Geometry g1 = this.toGeometry();
        Geometry g2 = line.toGeometry();
        if (this.getShapeType().isPolygon()){
            Polygonizer polygonizer = new Polygonizer();
            Geometry polygons = g1.getBoundary().union(g2);
            polygonizer.add(polygons);
            List<Geometry> polys = (List)polygonizer.getPolygons();   
            List<Shape> polyShapes = new ArrayList<>();
            for (int i = 0; i < polys.size(); i++){
                org.meteoinfo.jts.geom.Polygon poly = (org.meteoinfo.jts.geom.Polygon)polys.get(i);
                if (poly.getInteriorPoint().within(g1))
                    polyShapes.add(new PolygonShape(poly));
            }
            return polyShapes;
        } else if (this.getShapeType().isLine()){
            Geometry ugeo = g1.union(g2);            
            List<Shape> lineShapes = new ArrayList<>();
            for (int i = 0; i < ugeo.getNumGeometries(); i++){
                Geometry geo = ugeo.getGeometryN(i);
                if (geo.buffer(0.001).within(g1.buffer(0.0011)))
                    lineShapes.add(new PolylineShape(geo));
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
    public Shape reform(Shape line){
        Geometry g1 = this.toGeometry();
        Geometry g2 = line.toGeometry();
        if (this.getShapeType().isPolygon()){
            Polygonizer polygonizer = new Polygonizer();
            Geometry polygons = g1.getBoundary().union(g2);
            polygonizer.add(polygons);
            List<Geometry> polys = (List)polygonizer.getPolygons();
            Geometry mbgeo;
            org.meteoinfo.jts.geom.Polygon poly1 = (org.meteoinfo.jts.geom.Polygon)g1;
            if (poly1.getNumInteriorRing() == 0){
                mbgeo = CascadedPolygonUnion.union(polys);
            } else {
                GeometryFactory factory = new GeometryFactory();
                org.meteoinfo.jts.geom.Polygon shell = factory.createPolygon((LinearRing)poly1.getExteriorRing());
                List<Geometry> npolys = new ArrayList<>();
                for (int i = 0; i < polys.size(); i++){
                    org.meteoinfo.jts.geom.Polygon poly = (org.meteoinfo.jts.geom.Polygon)polys.get(i);
                    if (poly.getInteriorPoint().within(g1))
                        npolys.add(poly);
                    else {
                        if (!poly.getInteriorPoint().within(shell))
                            npolys.add(poly);
                    }
                }
                mbgeo = CascadedPolygonUnion.union(npolys);
            }
            Shape r = new PolygonShape(mbgeo);
            return r;
        } else if (this.getShapeType().isLine()){
            Geometry ugeo = g1.union(g2);            
            List<Geometry> geos = new ArrayList<>();
            for (int i = 0; i < ugeo.getNumGeometries(); i++){
                Geometry geo = ugeo.getGeometryN(i);
                Coordinate c1 = geo.getCoordinates()[0];
                Coordinate c2 = geo.getCoordinates()[geo.getNumPoints() - 1];
                if (c1.equals2D(g1.getCoordinates()[0]) ||
                    c1.equals2D(g1.getCoordinates()[g1.getNumPoints() - 1]) ||
                    c2.equals2D(g1.getCoordinates()[0]) ||
                    c2.equals2D(g1.getCoordinates()[g1.getNumPoints() - 1]))
                    geos.add(geo);
                else {
                    GeometryFactory factory = new GeometryFactory();
                    Geometry p1 = factory.createPoint(c1);
                    Geometry p2 = factory.createPoint(c2);
                    Geometry buffer = g1.buffer(0.001);
                    if (p1.within(buffer) && p2.within(buffer)){
                        if (!geo.buffer(0.001).within(g1.buffer(0.0011)))
                            geos.add(geo);
                    }
                }
            }
            Geometry geo = UnaryUnionOp.union(geos);
            return new PolylineShape(geo);
        }
        
        return null;
    }
    
    /**
     * Is shapes cross each other or not
     * @param other Other shape
     * @return Cross or not
     */
    public boolean crosses(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.crosses(g2);
    }
    
    /**
     * If this shape contains another one
     * @param other Other shape
     * @return Contains or not
     */
    public boolean contains(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.contains(g2);
    }
    
    /**
     * If this shape covered by another one
     * @param other Other shape
     * @return Covered by or not
     */
    public boolean coveredBy(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.coveredBy(g2);
    }
    
    /**
     * If this shape covers another one
     * @param other Other shape
     * @return Covers or not
     */
    public boolean covers(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.covers(g2);
    }
    
    /**
     * If this shape disjoint another one
     * @param other Other shape
     * @return Disjoint or not
     */
    public boolean disjoint(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.disjoint(g2);
    }
    
    /**
     * If this shape equals another one
     * @param other Other shape
     * @return Equals or not
     */
    public boolean equals(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.equals(g2);
    }
    
    /**
     * If this shape intersects another one
     * @param other Other shape
     * @return Intersects or not
     */
    public boolean intersects(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.intersects(g2);
    }
    
    /**
     * If this shape overlaps another one
     * @param other Other shape
     * @return Overlaps or not
     */
    public boolean overlaps(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.overlaps(g2);
    }
    
    /**
     * If this shape touches another one
     * @param other Other shape
     * @return Touches or not
     */
    public boolean touches(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.touches(g2);
    }
    
    /**
     * If this shape within another one
     * @param other Other shape
     * @return Within or not
     */
    public boolean within(Shape other){
        Geometry g1 = this.toGeometry();
        Geometry g2 = other.toGeometry();
        return g1.within(g2);
    }

    /**
     * Clone
     *
     * @return Shape object
     */
    @Override
    public Object clone() {
        Shape o = null;
        try {
            o = (Shape)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }
    
    /**
     * Clone value
     * @param other Other shape
     */
    public void cloneValue(Shape other){};
    // </editor-fold>
}
