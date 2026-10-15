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

import org.meteoinfo.geometry.GeometryType;
import org.meteoinfo.geometry.Point;

/**
 *
 * @author Yaqiang
 */
public class StationModel extends Point {
    // <editor-fold desc="Variables">

    /// <summary>
    /// Wind barb
    /// </summary>
    public WindBarb windBarb = new WindBarb();
    /// <summary>
    /// Weather symbol
    /// </summary>
    public WeatherSymbol weatherSymbol = new WeatherSymbol();
    /// <summary>
    /// Cloud coverage
    /// </summary>
    public CloudCoverage cloudCoverage = new CloudCoverage();
    /// <summary>
    /// Temperature
    /// </summary>
    public int temperature;
    /// <summary>
    /// Dew point
    /// </summary>
    public int dewPoint;
    /// <summary>
    /// Pressure
    /// </summary>
    public int pressure;
    /// <summary>
    /// Size
    /// </summary>
    public float size;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public StationModel() {

    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public GeometryType getGeometryType(){
        return GeometryType.STATION_MODEL;
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
        StationModel aSM = new StationModel();
        aSM.size = size;
        aSM.pressure = pressure;
        aSM.dewPoint = dewPoint;
        aSM.temperature = temperature;
        aSM.cloudCoverage = cloudCoverage;
        aSM.weatherSymbol = (WeatherSymbol) weatherSymbol.clone();
        aSM.windBarb = (WindBarb) windBarb.clone();
        aSM.setCoordinate(this.getCoordinate());
        aSM.setValue(this.getValue());

        return aSM;
    }
    // </editor-fold>
}
