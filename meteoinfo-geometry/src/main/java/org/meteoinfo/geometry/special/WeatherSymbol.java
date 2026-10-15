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
public class WeatherSymbol extends Point {
    // <editor-fold desc="Variables">

    /// <summary>
    /// size
    /// </summary>
    public float size;
    /// <summary>
    /// Weather
    /// </summary>
    public int weather;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public WeatherSymbol() {

    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public GeometryType getGeometryType(){
        return GeometryType.WEATHER_SYMBOL;
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
            return (WeatherSymbol) this.clone();
        }
    }

    /**
     * Clone
     *
     * @return WindArraw object
     */
    @Override
    public Object clone() {
        WeatherSymbol aWS = new WeatherSymbol();
        aWS.size = size;
        aWS.weather = weather;
        aWS.setCoordinate(this.getCoordinate());
        aWS.setValue(this.getValue());
        aWS.setLegendIndex(this.getLegendIndex());

        return aWS;
    }
    // </editor-fold>
}
