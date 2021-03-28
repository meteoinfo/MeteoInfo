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
 * Layer type enum
 * 
 * @author Yaqiang Wang
 */
public enum LayerTypes {
    VECTOR_LAYER,
    IMAGE_LAYER,
    RASTER_LAYER,
    WEB_MAP_LAYER;

    public static LayerTypes valueOfBack(String value) {
        switch (value.toUpperCase()) {
            case "VECTORLAYER":
                return LayerTypes.VECTOR_LAYER;
            case "IMAGELAYER":
                return LayerTypes.IMAGE_LAYER;
            case "RASTERLAYER":
                return LayerTypes.RASTER_LAYER;
            case "WEBMAPLAYER":
                return LayerTypes.WEB_MAP_LAYER;
            default:
                return LayerTypes.valueOf(value.toUpperCase());
        }
    }
}
