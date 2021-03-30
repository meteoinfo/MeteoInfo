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
package org.meteoinfo.geo.layout;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public enum ScaleBarType {

    SCALE_LINE_1,
    SCALE_LINE_2,
    // Alternating scale bar
    ALTERNATING_BAR;

    public static ScaleBarType valueOfBack(String value) {
        switch (value.toUpperCase()) {
            case "SCALELINE_1":
                return ScaleBarType.SCALE_LINE_1;
            case "SCALELINE_2":
                return ScaleBarType.SCALE_LINE_2;
            default:
                return ScaleBarType.valueOf(value.toUpperCase());
        }
    }
}
