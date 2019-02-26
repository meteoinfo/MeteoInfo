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
package org.meteoinfo.map;

import com.l2fprod.common.beans.BaseBeanInfo;

/**
 *
 * @author Yaqiang
 */
public class MapViewBeanInfo extends BaseBeanInfo {
    public MapViewBeanInfo(){
        super(MapView.class);                
        addProperty("antiAlias").setCategory("General").setDisplayName("AntiAlias");
        addProperty("background").setCategory("General").setDisplayName("Background");
        addProperty("foreground").setCategory("General").setDisplayName("Foreground");
        addProperty("xYScaleFactor").setCategory("General").setDisplayName("XYScaleFactor");
        addProperty("selectColor").setCategory("General").setDisplayName("Select Color");
        addProperty("multiGlobalDraw").setCategory("General").setDisplayName("MultiGlobalDraw");
        addProperty("pointAntiAlias").setCategory("General").setDisplayName("Point AntiAlias");
        addProperty("highSpeedWheelZoom").setCategory("General").setDisplayName("HighSpeedWheelZoom");
    }
}
