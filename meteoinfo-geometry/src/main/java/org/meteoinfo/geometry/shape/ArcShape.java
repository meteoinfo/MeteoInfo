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

import org.meteoinfo.common.PointD;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class ArcShape extends EllipseShape {
    // <editor-fold desc="Variables">
    private float startAngle;
    private float sweepAngle;
    private float explode = 0;
    private Float wedgeWidth = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ArcShape() {

    }

    /**
     * Constructor
     * @param x Center x
     * @param y Center y
     * @param width Width
     * @param height Height
     */
    public ArcShape(double x, double y, double width, double height) {
        List<PointD> points = new ArrayList<>();
        points.add(new PointD(x - width * 0.5, y - height * 0.5));
        points.add(new PointD(x - width * 0.5, y + height * 0.5));
        points.add(new PointD(x + width * 0.5, y + height * 0.5));
        points.add(new PointD(x + width * 0.5, y - height * 0.5));
        super.setPoints(points);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.ARC;
    }
    
    /**
     * Get start angle
     * @return Start angle
     */
    public float getStartAngle(){
        return this.startAngle;
    }
    
    /**
     * Set start angle
     * @param value 
     */
    public void setStartAngle(float value){
        this.startAngle = value;
    }
    
    /**
     * Get sweep angle
     * @return Sweep angle
     */
    public float getSweepAngle(){
        return this.sweepAngle;
    }
    
    /**
     * Set sweep angle
     * @param value Sweep angle
     */
    public void setSweepAngle(float value){
        this.sweepAngle = value;
    }
    
    /**
     * Get explode
     * @return Explode
     */
    public float getExplode(){
        return this.explode;
    }
    
    /**
     * Set explode
     * @param value Explode 
     */
    public void setExplode(float value){
        this.explode = value;
    }
    
    /**
     * Get wedge width
     * @return Wedge width
     */
    public Float getWedgeWidth() {
        return this.wedgeWidth;
    }
    
    /**
     * Set wedge width
     * @param value Wedge width
     */
    public void setWedgeWidth(Float value) {
        this.wedgeWidth = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * If this shape contains a point
     * @param p Point
     * @return Contains a point or not
     */
    public boolean contains(PointD p){
        PointD center = this.getCenter();
        double a = this.getA();
        double b = this.getB();

        // checking the equation of
        // ellipse with the given point
        double r = (Math.pow((p.X - center.X), 2)
                / Math.pow(a, 2))
                + (Math.pow((p.Y - center.Y), 2)
                / Math.pow(b, 2));

        if (r > 1) {
            return false;
        }

        double angle = Math.toDegrees(Math.atan2(p.Y - center.Y, p.X - center.X));
        if (angle < 0) {
            angle += 360;
        }

        return angle >= this.startAngle && angle <= this.startAngle + this.sweepAngle;
    }

    /**
     * Clone
     *
     * @return RectangleShape object
     */
    @Override
    public Object clone() {
        ArcShape aPGS = new ArcShape();
        aPGS.setExtent(this.getExtent());
        aPGS.setPoints(new ArrayList<>(this.getPoints()));
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());

        return aPGS;
    }
    // </editor-fold>
}
