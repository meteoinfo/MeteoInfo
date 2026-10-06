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
package org.meteoinfo.geometry.shape;

import java.util.Locale;

/**
 * Shape type enum
 *
 * @author Yaqiang Wang
 */
public enum ShapeTypes {

    POINT(1),
    POLYLINE(3),
    POLYGON(5),
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
    SHAPE_COLLECTION(70);

    private final int value;

    /**
     * Get value
     * @return Value
     */
    public int getValue() {
        return value;
    }    

    ShapeTypes(int value) {
        this.value = value;
    }

    /**
     * Get value from ordinal
     *
     * @param ordinal Ordinal
     * @return ShapeTypes value
     */
    public static ShapeTypes valueOf(int ordinal) {
        switch(ordinal){
            case 1:
                return ShapeTypes.POINT;
            case 3:
                return ShapeTypes.POLYLINE;
            case 5:
                return ShapeTypes.POLYGON;
            case 41:
                return ShapeTypes.WIND_ARROW;
            case 42:
                return ShapeTypes.WIND_BARB;
            case 43:
                return ShapeTypes.WEATHER_SYMBOL;
            case 44:
                return ShapeTypes.STATION_MODEL;
            case 51:
                return ShapeTypes.RECTANGLE;
            case 52:
                return ShapeTypes.CURVE_LINE;
            case 53:
                return ShapeTypes.CURVE_POLYGON;
            case 54:
                return ShapeTypes.ELLIPSE;
            case 55:
                return ShapeTypes.CIRCLE;
            case 99:
                return ShapeTypes.IMAGE;
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
            case POLYLINE:
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
    public boolean isSameLegendType(ShapeTypes st){
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
