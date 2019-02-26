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
 * Wind arraw class
 *
 * @author Yaqiang
 */
public class WindArrow extends PointShape {
    // <editor-fold desc="Variables">

    /**
     * Size
     */
    public float size = 6;
    /**
     * Length
     */
    public float length = 20;
    /**
     * Angle
     */
    public double angle = 270;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public WindArrow() {

    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.WindArraw;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     *
     * @return WindArraw object
     */
    @Override
    public Object clone() {
        WindArrow aWA = new WindArrow();
        aWA.size = size;
        aWA.length = length;
        aWA.angle = angle;
        aWA.setPoint(this.getPoint());
        aWA.setValue(this.getValue());
        
        return aWA;
    }
    // </editor-fold>
}
