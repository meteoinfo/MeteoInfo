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
package org.meteoinfo.geometry;

/**
 * Shape type enum
 *
 * @author Yaqiang Wang
 */
public enum GeometryType {

    POINT(1),
    MULTI_POINT(2),
    LINESTRING(3),
    LINEARRING(13),
    MULTI_LINESTRING(4),
    POLYGON(5),
    MULTI_POLYGON(6),
    WIND_ARROW(41),
    WIND_BARB(42),
    WEATHER_SYMBOL(43),
    STATION_MODEL(44),
    IMAGE(99),
    RECTANGLE(51),
    CURVE_LINE(52),
    CURVE_POLYGON(53),
    ELLIPSE(54),
    CIRCLE(55),
    BAR(56),
    POLYLINE_ERROR(57),
    ARC(58),
    TEXT(59),
    TEXTURE(60),
    CUBIC(61),
    CYLINDER(62),
    CONE(63),
    SPHERE(64),
    GEOMETRY_COLLECTION(70);

    private final int value;

    /**
     * Get value
     * @return Value
     */
    public int getValue() {
        return value;
    }    

    GeometryType(int value) {
        this.value = value;
    }

    /**
     * Get value from ordinal
     *
     * @param ordinal Ordinal
     * @return ShapeType value
     */
    public static GeometryType valueOf(int ordinal) {
        switch(ordinal){
            case 1:
                return GeometryType.POINT;
            case 2:
                return GeometryType.MULTI_POINT;
            case 3:
                return GeometryType.LINESTRING;
            case 4:
                return GeometryType.MULTI_LINESTRING;
            case 13:
                return GeometryType.LINEARRING;
            case 5:
                return GeometryType.POLYGON;
            case 6:
                return GeometryType.MULTI_POLYGON;
            case 41:
                return GeometryType.WIND_ARROW;
            case 42:
                return GeometryType.WIND_BARB;
            case 43:
                return GeometryType.WEATHER_SYMBOL;
            case 44:
                return GeometryType.STATION_MODEL;
            case 51:
                return GeometryType.RECTANGLE;
            case 52:
                return GeometryType.CURVE_LINE;
            case 53:
                return GeometryType.CURVE_POLYGON;
            case 54:
                return GeometryType.ELLIPSE;
            case 55:
                return GeometryType.CIRCLE;
            case 56:
                return GeometryType.BAR;
            case 57:
                return GeometryType.POLYLINE_ERROR;
            case 58:
                return GeometryType.ARC;
            case 59:
                return GeometryType.TEXT;
            case 60:
                return GeometryType.TEXTURE;
            case 61:
                return GeometryType.CUBIC;
            case 62:
                return GeometryType.CYLINDER;
            case 63:
                return GeometryType.CONE;
            case 64:
                return GeometryType.SPHERE;
            case 70:
                return GeometryType.GEOMETRY_COLLECTION;
            case 99:
                return GeometryType.IMAGE;
            default:
                throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    /**
     * If is point
     * @return Boolean
     */
    public boolean isPoint(){
        switch(this){
            case POINT:
            case MULTI_POINT:
            case WIND_ARROW:
            case WIND_BARB:
            case WEATHER_SYMBOL:
            case STATION_MODEL:
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
            case LINESTRING:
            case LINEARRING:
            case MULTI_LINESTRING:
            case CURVE_LINE:
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
            case MULTI_POLYGON:
            case RECTANGLE:
            case CURVE_POLYGON:
            case ELLIPSE:
            case CIRCLE:
                return true;
        }
        return false;
    }
    
    /**
     * Check if this shape type has same legend type with other shape type
     * @param st Other shape type
     * @return Boolean
     */
    public boolean isSameLegendType(GeometryType st){
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
}
