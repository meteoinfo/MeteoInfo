/*
 * Copyright 2012 Yaqiang Wang,
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
package org.meteoinfo.geo.mapdata;

import org.meteoinfo.geometry.shape.ShapeTypes;

/**
 * Shape type enum
 *
 * @author Yaqiang Wang
 */
public enum ShapeFileType {

    POINT(1),
    POLYLINE(3),
    POLYGON(5),
    POINT_Z(11),
    POLYLINE_Z(13),
    POLYGON_Z(15),
    POINT_M(21),
    POLYLINE_M(23),
    POLYGON_M(25),
    WIND_ARROW(41),
    WIND_BARB(42),
    WEATHER_SYMBOL(43),
    STATION_MODEL(44),
    IMAGE(99);

    private final int value;

    /**
     * Get value
     * @return Value
     */
    public int getValue() {
        return value;
    }

    ShapeFileType(int value) {
        this.value = value;
    }

    /**
     * Get value from ordinal
     *
     * @param ordinal Ordinal
     * @return ShapeTypes value
     */
    public static ShapeFileType valueOf(int ordinal) {
        switch(ordinal){
            case 1:
                return ShapeFileType.POINT;
            case 3:
                return ShapeFileType.POLYLINE;
            case 5:
                return ShapeFileType.POLYGON;
            case 11:
                return ShapeFileType.POINT_Z;
            case 13:;
                return ShapeFileType.POLYLINE_Z;
            case 15:
                return ShapeFileType.POLYGON_Z;
            case 21:
                return ShapeFileType.POINT_M;
            case 23:
                return ShapeFileType.POLYLINE_M;
            case 25:
                return ShapeFileType.POLYGON_M;
            case 41:
                return ShapeFileType.WIND_ARROW;
            case 42:
                return ShapeFileType.WIND_BARB;
            case 43:
                return ShapeFileType.WEATHER_SYMBOL;
            case 44:
                return ShapeFileType.STATION_MODEL;
            case 99:
                return ShapeFileType.IMAGE;
            default:
                throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    /**
     * Convert to ShapeTypes
     * @return ShapeTypes
     */
    public ShapeTypes toShapeType() {
        switch (this) {
            case POINT:
            case POINT_M:
            case POINT_Z:
                return ShapeTypes.POINT;
            case POLYLINE:
            case POLYLINE_M:
            case POLYLINE_Z:
                return ShapeTypes.POLYLINE;
            case POLYGON:
            case POLYGON_M:
            case POLYGON_Z:
                return ShapeTypes.POLYGON;
        }
        return ShapeTypes.POINT;
    }

    /**
     * Convert from ShapeTypes
     * @param shapeType ShapeTypes
     * @return ShapeFileType
     */
    public static ShapeFileType fromShapeType(ShapeTypes shapeType) {
        switch (shapeType) {
            case POINT:
                return ShapeFileType.POINT;
            case POLYLINE:
                return ShapeFileType.POLYLINE;
            case POLYGON:
                return ShapeFileType.POLYGON;
            case WIND_ARROW:
                return ShapeFileType.WIND_ARROW;
            case WIND_BARB:
                return ShapeFileType.WIND_BARB;
            case WEATHER_SYMBOL:
                return  ShapeFileType.WEATHER_SYMBOL;
            case STATION_MODEL:
                return ShapeFileType.STATION_MODEL;
            case IMAGE:
                return ShapeFileType.IMAGE;
            default:
                return ShapeFileType.POINT;
        }
    }

    /**
     * If is point
     * @return Boolean
     */
    public boolean isPoint(){
        switch(this){
            case POINT:
            case POINT_M:
            case POINT_Z:
            case WIND_ARROW:
            case WIND_BARB:
            case WEATHER_SYMBOL:
                return true;                
        }
        return false;
    }
    
    /**
     * If is line
     * @return Boolean
     */
    public boolean isLine(){
        switch(this){
            case POLYLINE:
            case POLYLINE_Z:
            case POLYLINE_M:
                return true;
        }
        return false;
    }
    
    /**
     * If is polygon
     * @return Boolean
     */
    public boolean isPolygon(){
        switch(this){
            case POLYGON:
            case POLYGON_M:
            case POLYGON_Z:
                return true;
        }
        return false;
    }
    
    /**
     * Check if this shape type has same legend type with other shape type
     * @param st Other shape type
     * @return Boolean
     */
    public boolean isSameLegendType(ShapeFileType st){
        if (this == st){
            return true;
        } else {
            if (this.isLine() && st.isLine())
                return true;
            else if (this.isPoint() && st.isPoint())
                return true;
            else if (this.isPolygon() && st.isPolygon())
                return true;
            else
                return false;
        }
    }

    /**
     * Get if the shape has z coordinate
     * @return Boolean
     */
    public boolean isZ() {
        switch (this) {
            case POINT_Z:
            case POLYLINE_Z:
            case POLYGON_Z:
                return true;
        }
        return false;
    }
}
