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
 *
 * @author Yaqiang Wang
 */
public enum LayerUpdateTypes {

    NOT_UPDATE,
    FIRST_METEO_LAYER,
    LAST_ADDED_LAYER,
    FIRST_EXPANDED_LAYER;

    public static LayerUpdateTypes valueOfBack(String value) {
        switch (value.toUpperCase()) {
            case "NOTUPDATE":
                return LayerUpdateTypes.NOT_UPDATE;
            case "FIRSTMETEOLAYER":
                return LayerUpdateTypes.FIRST_METEO_LAYER;
            case "LASTADDEDLAYER":
                return LayerUpdateTypes.LAST_ADDED_LAYER;
            case "FIRSTEXPANDEDLAYER":
                return LayerUpdateTypes.FIRST_EXPANDED_LAYER;
            default:
                return LayerUpdateTypes.valueOf(value.toUpperCase());
        }
    }
}
