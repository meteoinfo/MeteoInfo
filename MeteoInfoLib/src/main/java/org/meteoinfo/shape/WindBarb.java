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
 *
 * @author Yaqiang Wang
 */
public class WindBarb extends PointShape {
    // <editor-fold desc="Variables">

    /// <summary>
    /// size
    /// </summary>
    public float size;
    /// <summary>
    /// angle
    /// </summary>
    public double angle;
    /// <summary>
    /// wind speed
    /// </summary>
    public float windSpeed;
    /// <summary>
    /// wind speed line
    /// </summary>
    public WindSpeedLine windSpeesLine = new WindSpeedLine();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public WindBarb() {

    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.WindBarb;
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
        WindBarb aWB = new WindBarb();
        aWB.size = size;
        aWB.windSpeed = windSpeed;
        aWB.angle = angle;
        aWB.windSpeesLine = windSpeesLine;
        aWB.setPoint(this.getPoint());
        aWB.setValue(this.getValue());

        return aWB;
    }
    // </editor-fold>
}
