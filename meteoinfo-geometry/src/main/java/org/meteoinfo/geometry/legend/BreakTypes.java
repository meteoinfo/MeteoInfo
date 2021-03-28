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
package org.meteoinfo.geometry.legend;

/**
 * Break type enum
 * 
 * @author Yaqiang Wang
 */
public enum BreakTypes {

    POINT_BREAK,
    POLYLINE_BREAK,
    POLYGON_BREAK,
    COLOR_BREAK,
    VECTOR_BREAK,
    LABEL_BREAK,
    CHART_BREAK,
    COLOR_BREAK_COLLECTION;

    public static BreakTypes valueOfBack(String value) {
        switch (value.toUpperCase()) {
            case "POINTBREAK":
                return BreakTypes.POINT_BREAK;
            case "POLYLINEBREAK":
                return BreakTypes.POLYLINE_BREAK;
            case "POLYGONBREAK":
                return BreakTypes.POLYGON_BREAK;
            case "COLORBREAK":
                return BreakTypes.COLOR_BREAK;
            case "VECTORBREAK":
                return BreakTypes.VECTOR_BREAK;
            case "LABELBREAK":
                return BreakTypes.LABEL_BREAK;
            case "CHARTBREAK":
                return BreakTypes.CHART_BREAK;
            case "COLORBREAKCOLLECTION":
                return BreakTypes.COLOR_BREAK_COLLECTION;
            default:
                return BreakTypes.valueOf(value.toUpperCase());
        }
    }
}
