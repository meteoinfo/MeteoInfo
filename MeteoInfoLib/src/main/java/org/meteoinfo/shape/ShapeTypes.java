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
package org.meteoinfo.shape;

/**
 * Shape type enum
 *
 * @author Yaqiang Wang
 */
public enum ShapeTypes {

    Point(1),
    Polyline(3),
    Polygon(5),
    PointZ(11),
    PolylineZ(13),
    PolygonZ(15),
    PointM(21),
    PolylineM(23),
    PolygonM(25),
    WindArraw(41),
    WindBarb(42),
    WeatherSymbol(43),
    StationModel(44),
    Image(99),
    Rectangle(51),
    CurveLine(52),
    CurvePolygon(53),
    Ellipse(54),
    Circle(55),
    Bar(56),
    PolylineError(57),
    ARC(58),
    TEXT(59);
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
//        if (ordinal < 0 || ordinal >= values().length) {
//            throw new IndexOutOfBoundsException("Invalid ordinal");
//        }
        switch(ordinal){
            case 1:
                return ShapeTypes.Point;
            case 3:
                return ShapeTypes.Polyline;
            case 5:
                return ShapeTypes.Polygon;
            case 11:
                return ShapeTypes.PointZ;
            case 13:;
                return ShapeTypes.PolylineZ;
            case 15:
                return ShapeTypes.PolygonZ;
            case 21:
                return ShapeTypes.PointM;
            case 23:
                return ShapeTypes.PolylineM;
            case 25:
                return ShapeTypes.PolygonM;
            case 41:
                return ShapeTypes.WindArraw;
            case 42:
                return ShapeTypes.WindBarb;
            case 43:
                return ShapeTypes.WeatherSymbol;
            case 44:
                return ShapeTypes.StationModel;
            case 51:
                return ShapeTypes.Rectangle;
            case 52:
                return ShapeTypes.CurveLine;
            case 53:
                return ShapeTypes.CurvePolygon;
            case 54:
                return ShapeTypes.Ellipse;
            case 55:
                return ShapeTypes.Circle;
            case 99:
                return ShapeTypes.Image;
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
            case Point:
            case PointM:
            case PointZ:
            case WindArraw:
            case WindBarb:
            case WeatherSymbol:
            case StationModel:
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
            case Polyline:
            case PolylineZ:
            case PolylineM:
            case CurveLine:            
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
            case Polygon:
            case PolygonM:
            case PolygonZ:
            case Rectangle:
            case CurvePolygon:
            case Ellipse:
            case Circle:
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
