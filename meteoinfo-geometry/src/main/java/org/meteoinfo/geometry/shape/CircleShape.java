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
package org.meteoinfo.geometry.shape;

import java.util.ArrayList;
import java.util.List;

import org.meteoinfo.common.PointD;
import org.meteoinfo.common.PointZ;
import org.meteoinfo.geometry.geoprocess.GeoComputation;

 /**
 * Circle shape class
 * 
 * @author Yaqiang Wang
 */
public class CircleShape extends PolygonShape {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public CircleShape() {

    }
    
    /**
     * Constructor
     * @param x Center x
     * @param y Center y
     * @param radius Radius
     */
    public CircleShape(double x, double y, double radius) {
        List<PointZ> points = new ArrayList<>();
        points.add(new PointZ(x - radius, y));
        points.add(new PointZ(x, y -radius));
        points.add(new PointZ(x + radius, y));
        points.add(new PointZ(x, y + radius));
        super.setPoints(points);
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.CIRCLE;
    }
    
    /**
     * Get circle center point
     * @return Center point
     */
    public PointZ getCenter() {
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
    public boolean contains(Shape other){
        if (other instanceof PointShape) {
            return this.contains(((PointShape)other).getPoint());
        } else {
            PointZ center = this.getCenter();
            double radius = this.getRadius();
            boolean isIn = true;
            for (PointZ p : other.getPoints()){
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
    public boolean contains(PointZ p){
        PointZ center = this.getCenter();
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
        CircleShape aPGS = new CircleShape();
        aPGS.setExtent(this.getExtent());
        aPGS.setPoints(this.getPoints());
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());

        return aPGS;
    }
    // </editor-fold>
}
