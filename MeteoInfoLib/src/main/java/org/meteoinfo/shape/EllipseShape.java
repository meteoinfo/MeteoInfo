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
package org.meteoinfo.shape;

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
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.Ellipse;
    }
    
    /**
     * Get angle
     * @return Angle
     */
    public float getAngle(){
        return this.angle;
    }
    
    /**
     * Set angle
     * @param value Angle
     */
    public void setAngle(float value){
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
