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
package org.meteoinfo.geometry.legend;

/**
 * Point style enum
 *
 * @author Yaqiang Wang
 */
public enum PointStyle {

    CIRCLE,
    SQUARE,
    UP_TRIANGLE,
    DOWN_TRIANGLE,
    DIAMOND,
    X_CROSS,
    PLUS,
    MINUS,
    STAR,
    STAR_LINES,
    PENTAGON,
    UP_SEMI_CIRCLE,
    DOWN_SEMI_CIRCLE,
    DOUBLE_CIRCLE,
    CIRCLE_STAR;

    public static PointStyle valueOfBack(String value) {
        switch (value.toUpperCase()) {
            case "UPTRIANGLE":
                return PointStyle.UP_TRIANGLE;
            case "DOWNTRIANGLE":
                return PointStyle.DOWN_TRIANGLE;
            case "XCROSS":
                return PointStyle.X_CROSS;
            case "STAR_LINES":
                return PointStyle.STAR_LINES;
            case "UP_SEMI_CIRCLE":
                return PointStyle.UP_SEMI_CIRCLE;
            case "DOWN_SEMI_CIRCLE":
                return PointStyle.DOWN_SEMI_CIRCLE;
            default:
                return PointStyle.valueOf(value.toUpperCase());
        }
    }
}
