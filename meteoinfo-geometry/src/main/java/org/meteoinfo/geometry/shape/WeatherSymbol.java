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

import org.locationtech.jts.geom.Geometry;

/**
 *
 * @author Yaqiang Wang
 */
public class WeatherSymbol extends PointShape {
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
    public ShapeTypes getShapeType(){
        return ShapeTypes.WEATHER_SYMBOL;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get intersection shape
     * @param b Other shape
     * @return Intersection shape
     */
    @Override
    public Shape intersection(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.intersection(g2);

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
        aWS.setPoint(this.getPoint());
        aWS.setValue(this.getValue());
        aWS.setLegendIndex(this.getLegendIndex());

        return aWS;
    }
    // </editor-fold>
}
