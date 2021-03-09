/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.grib;

/**
 *
 * @author yaqiang
 */
public class GRIBData {

    /**
     * Get mean longitude between tow longitude
     *
     * @param lon1 Longitude 1
     * @param lon2 Longitude 2
     * @return Mean longitude
     */
    public static double getMeanLongitude(double lon1, double lon2) {
        double meanLon;
        if (lon1 < lon2) {
            meanLon = (lon1 + lon2) / 2;
        } else {
            meanLon = (lon1 + lon2 + 360) / 2;
            if (meanLon > 360) {
                meanLon -= 360;
            }
        }

        return meanLon;
    }
}
