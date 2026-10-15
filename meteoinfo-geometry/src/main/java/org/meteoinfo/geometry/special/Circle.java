 /* Copyright 2012 Yaqiang Wang,
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
package org.meteoinfo.geometry.special;

import java.util.ArrayList;
import java.util.List;

import org.meteoinfo.geometry.*;
import org.meteoinfo.geometry.geoprocess.GeoComputation;

 /**
 * Circle shape class
 * 
 * @author Yaqiang Wang
 */
public class Circle extends MultiPolygon {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Circle() {

    }
    
    /**
     * Constructor
     * @param x Center x
     * @param y Center y
     * @param radius Radius
     */
    public Circle(double x, double y, double radius) {
        List<Coordinate> points = new ArrayList<>();
        points.add(new Coordinate(x - radius, y));
        points.add(new Coordinate(x, y -radius));
        points.add(new Coordinate(x + radius, y));
        points.add(new Coordinate(x, y + radius));
        super.setCoordinates(points);
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    
    @Override
    public GeometryType getGeometryType(){
        return GeometryType.CIRCLE;
    }
    
    /**
     * Get circle center point
     * @return Center point
     */
    public Coordinate getCenter() {
        return this.getExtent().getCenterPoint();
    }
    
    /**
     * Get radius
     * @return Radius
     */
    public double getRadius() {
        return this.getExtent().getHeight() / 2;
    }
    
    /**
     * If this shape contains another one
     * @param other Other shape
     * @return Contains or not
     */
    @Override
    public boolean contains(Geometry other){
        if (other instanceof Point) {
            return this.contains(((Point)other).getCoordinate());
        } else {
            Coordinate center = this.getCenter();
            double radius = this.getRadius();
            boolean isIn = true;
            for (Coordinate p : other.getCoordinates()){
                if (GeoComputation.distance(p, center) > radius) {
                    isIn = false;
                    break;
                }
            }
            return isIn;
        }
    }
    
    /**
     * If this shape contains a point
     * @param p Point
     * @return Contains a point or not
     */
    public boolean contains(Coordinate p){
        Coordinate center = this.getCenter();
        double radius = this.getRadius();
        return GeoComputation.distance(p, center) <= radius;
    }

    /**
     * Clone
     * 
     * @return CircleShape
     */
    @Override
    public Object clone() {
        Circle aPGS = new Circle();
        aPGS.setExtent(this.getExtent());
        aPGS.setCoordinates(this.getCoordinates());
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());

        return aPGS;
    }
    // </editor-fold>
}
