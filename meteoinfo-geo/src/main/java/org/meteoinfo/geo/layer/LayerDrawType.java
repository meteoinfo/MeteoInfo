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
package org.meteoinfo.geo.layer;

/**
 * Layer draw type enum
 *
 * @author Yaqiang Wang
 */
public enum LayerDrawType {

    MAP,
    SHADED,
    CONTOUR,
    GRID_FILL,
    GRID_POINT,
    VECTOR,
    STATION_POINT,
    BARB,
    WEATHER_SYMBOL,
    STATION_MODEL,
    IMAGE,
    RASTER,
    TRAJECTORY_LINE,
    TRAJECTORY_POINT,
    STREAMLINE;

    public static LayerDrawType valueOfBack(String value) {
        switch (value.toUpperCase()) {
            case "GRIDFILL":
                return LayerDrawType.GRID_FILL;
            case "GRIDPOINT":
                return LayerDrawType.GRID_POINT;
            case "STATIONPOINT":
                return LayerDrawType.STATION_POINT;
            case "WEATHERSYMBOL":
                return LayerDrawType.WEATHER_SYMBOL;
            case "STATIONMODEL":
                return LayerDrawType.STATION_MODEL;
            case "TRAJLINE":
                return LayerDrawType.TRAJECTORY_LINE;
            case "TRAJPOINT":
                return LayerDrawType.TRAJECTORY_POINT;
            default:
                return LayerDrawType.valueOf(value.toUpperCase());
        }
    }
}
