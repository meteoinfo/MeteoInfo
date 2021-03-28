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

 import org.meteoinfo.geometry.shape.Line;

 /**
 * Line style enum
 *
 * @author Yaqiang Wang
 */
public enum LineStyles {

    SOLID,
    DASH,
    DOT,
    DASH_DOT,
    DASH_DOT_DOT,
    ARROW_LINE,
    COLD_FRONT,
    WARM_FRONT,
    OCCLUDED_FRONT,
    STATIONARY_FRONT;

    public static LineStyles valueOfBack(String value) {
        switch (value.toUpperCase()) {
            case "DASHDOT":
                return LineStyles.DASH_DOT;
            case "DASHDOTDOT":
                return LineStyles.DASH_DOT_DOT;
            case "ARROWLINE":
                return LineStyles.ARROW_LINE;
            case "COLDFRONT":
                return LineStyles.COLD_FRONT;
            case "WARMFRONT":
                return LineStyles.WARM_FRONT;
            case "OCCLUDEDFRONT":
                return LineStyles.OCCLUDED_FRONT;
            case "STATIONARY_FRONT":
                return LineStyles.STATIONARY_FRONT;
            default:
                return LineStyles.valueOf(value.toUpperCase());
        }
    }
}
