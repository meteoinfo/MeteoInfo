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
 import org.meteoinfo.geometry.geoprocess.GeometryUtil;

 import java.util.ArrayList;
 import java.util.List;

 /**
  * Ellipse shape class
  *
  * @author Yaqiang Wang
  */
 public class EllipseShape extends PolygonShape {
     // <editor-fold desc="Variables">
     private float angle = 0.0f;
     // </editor-fold>
     // <editor-fold desc="Constructor">

     /**
      * Constructor
      */
     public EllipseShape() {
     }

     /**
      * Constructor
      * @param x Center x
      * @param y Center y
      * @param width Width
      * @param height Height
      */
     public EllipseShape(double x, double y, double width, double height) {
         List<PointD> points = new ArrayList<>();
         points.add(new PointD(x - width * 0.5, y));
         points.add(new PointD(x, y - height * 0.5));
         points.add(new PointD(x + width * 0.5, y));
         points.add(new PointD(x, y + height * 0.5));
         super.setPoints(points);
     }

     // </editor-fold>
     // <editor-fold desc="Get Set Methods">
     @Override
     public ShapeTypes getShapeType() {
         return ShapeTypes.ELLIPSE;
     }

     /**
      * Get center point
      * @return Center point
      */
     public PointD getCenter() {
         return this.getExtent().getCenterPoint();
     }

     /**
      * Get width
      * @return Width
      */
     public double getWidth() {
         return this.getExtent().getWidth();
     }

     /**
      * Get height
      * @return Height
      */
     public double getHeight() {
         return this.getExtent().getHeight();
     }

     /**
      * Get angle
      *
      * @return Angle
      */
     public float getAngle() {
         return this.angle;
     }

     /**
      * Set angle
      *
      * @param value Angle
      */
     public void setAngle(float value) {
         this.angle = value;
     }
     // </editor-fold>
     // <editor-fold desc="Methods">

     /**
      * Clone
      *
      * @return EllipseShape
      */
     @Override
     public Object clone() {
         EllipseShape aPGS = new EllipseShape();
         aPGS.setExtent(this.getExtent());
         aPGS.setPoints(this.getPoints());
         aPGS.setVisible(this.isVisible());
         aPGS.setSelected(this.isSelected());

         return aPGS;
     }
     // </editor-fold>
 }
