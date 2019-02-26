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
package org.meteoinfo.data.meteodata;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class MeteoDataDrawSet {
    // <editor-fold desc="Variables">

    private String _weatherType = "All Weather";
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get weather type
     *
     * @return Weather type
     */
    public String getWeatherType() {
        return _weatherType;
    }

    /**
     * Set weather type
     *
     * @param weatherType Weather type
     */
    public void setWeatherType(String weatherType) {
        _weatherType = weatherType;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get weather list
     *
     * @param weatherType Weather type
     * @return Weather list
     */
    public static List<Integer> getWeatherTypes(String weatherType) {
        List<Integer> weatherList = new ArrayList<Integer>();
        int i;
        int[] weathers = new int[1];
        if (weatherType.equals("All Weather")) {
            weathers = new int[96];
            for (i = 4; i < 100; i++) {
                weathers[i - 4] = i;
            }
        } else if (weatherType.equals("SDS")) {
            weathers = new int[]{6, 7, 8, 9, 30, 31, 32, 33, 34, 35};
        } else if (weatherType.equals("SDS, Haze")) {
            weathers = new int[]{5, 6, 7, 8, 9, 30, 31, 32, 33, 34, 35};
        } else if (weatherType.equals("Smoke, Haze, Mist")) {
            weathers = new int[]{4, 5, 10};
        } else if (weatherType.equals("Smoke")) {
            weathers = new int[]{4};
        } else if (weatherType.equals("Haze")) {
            weathers = new int[]{5};
        } else if (weatherType.equals("Mist")) {
            weathers = new int[]{10};
        } else if (weatherType.equals("Fog")) {
            weathers = new int[10];
            for (i = 40; i < 50; i++) {
                weathers[i - 40] = i;
            }
        }


        for (int w : weathers) {
            weatherList.add(w);
        }

        return weatherList;
    }
    // </editor-fold>
}
