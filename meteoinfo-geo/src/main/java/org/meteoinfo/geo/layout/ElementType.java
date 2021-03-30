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
 * @author yaqiang
 */
public enum ElementType {

    LAYOUT_MAP,
    LAYOUT_ILLUSTRATION,
    LAYOUT_LEGEND,
    LAYOUT_GRAPHIC,
    LAYOUT_SCALE_BAR,
    LAYOUT_NORTH_ARROW,
    LAYOUT_CHART;

    public static ElementType valueOfBack(String value) {
        switch (value.toUpperCase()) {
            case "LAYOUTMAP":
                return ElementType.LAYOUT_MAP;
            case "LAYOUTILLUSTRATION":
                return ElementType.LAYOUT_ILLUSTRATION;
            case "LAYOUTLEGEND":
                return ElementType.LAYOUT_LEGEND;
            case "LAYOUTGRAPHIC":
                return ElementType.LAYOUT_GRAPHIC;
            case "LAYOUTSCALEBAR":
                return ElementType.LAYOUT_SCALE_BAR;
            case "LAYOUTNORTHARRAW":
                return ElementType.LAYOUT_NORTH_ARROW;
            case "LAYOUTCHART":
                return ElementType.LAYOUT_CHART;
            default:
                return ElementType.valueOf(value.toUpperCase());
        }
    }
}
