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

import org.meteoinfo.geometry.Geometry;
import org.meteoinfo.geometry.GeometryType;
import org.meteoinfo.geometry.Point;

/**
 *
 * @author Yaqiang Wang
 */
public class WindBarb extends Point {
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
    public GeometryType getGeometryType(){
        return GeometryType.WIND_BARB;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get intersection shape
     * @param b Other shape
     * @return Intersection shape
     */
    @Override
    public Geometry intersection(Geometry b){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = b.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g3 = g1.intersection(g2);

        if (g3.getNumPoints() < 1)
            return null;
        else {
            return (WindBarb) this.clone();
        }
    }

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
        aWB.setCoordinate(this.getCoordinate());
        aWB.setValue(this.getValue());
        aWB.setLegendIndex(this.getLegendIndex());

        return aWB;
    }
    // </editor-fold>
}
