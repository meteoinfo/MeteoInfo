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
package org.meteoinfo.drawing;

import org.meteoinfo.global.MIMath;
import java.util.List;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class ContourDraw {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Tracing contour borders
     *
     * @param gridData Grid data
     * @param X X array
     * @param Y Y array
     * @param S1 Flat array
     * @param undef Undefine value
     * @return Borders
     */
    public static List<wContour.Global.Border> tracingBorders(double[][] gridData, double[] X, double[] Y, int[][] S1,
            double undef) {
        return wContour.Contour.tracingBorders(gridData, X, Y, S1, undef);
    }

//    /**
//     * Tracing contour lines with undefined data
//     *
//     * @param gridData Grid data
//     * @param cValues Values
//     * @param X X array
//     * @param Y Y array
//     * @param noData Undefine data
//     * @param borders Contour line borders
//     * @param S1 Flag array
//     * @return Traced contour lines
//     */
//    public static List<wContour.Global.PolyLine> tracingContourLines(double[][] gridData, double[] cValues, double[] X,
//            double[] Y, double noData, List<wContour.Global.Border> borders, int[][] S1) {
//        int nc = cValues.length;
//        return wContour.Contour.tracingContourLines(gridData, X, Y, nc, cValues, noData, borders, S1);
//    }
    
    /**
     * Tracing contour lines with undefined data
     *
     * @param gridData Grid data
     * @param cValues Values
     * @param X X array
     * @param Y Y array
     * @param noData Undefine data
     * @param S1 Flag array
     * @return Traced contour lines and borders
     */
    public static Object[] tracingContourLines(double[][] gridData, double[] cValues, double[] X,
            double[] Y, double noData, int[][] S1) {
        int nc = cValues.length;
        List<wContour.Global.Border> borders = wContour.Contour.tracingBorders(gridData, X, Y, S1, noData);
        List<wContour.Global.PolyLine> contourLines = wContour.Contour.tracingContourLines(gridData, X, Y, nc, cValues, noData, borders, S1);
        return new Object[]{contourLines, borders};
    }

    /**
     * Tracing shaded polygons with undefined data
     *
     * @param gridData Grid data
     * @param contourLines Contour lines
     * @param borders Border lines
     * @param cValues Values
     * @return Polygon list
     */
    public static List<wContour.Global.Polygon> tracingPolygons(double[][] gridData,
            List<wContour.Global.PolyLine> contourLines, List<wContour.Global.Border> borders, double[] cValues) {
        return wContour.Contour.tracingPolygons(gridData, contourLines, borders, cValues);
    }

    /**
     * Get max/min values from a station data
     *
     * @param S Discrete data
     * @param noData Missing value
     * @param minmax Min/Max data array
     * @return If has missing value
     */
    public static boolean getMinMaxValueFDiscreteData(double[][] S, double noData, double[] minmax) {
        int i, validNum;
        boolean isNodata = false;
        double min = 0.0, max = 0.0;

        validNum = 0;
        for (i = 0; i < S.length; i++) {
            if (!MIMath.doubleEquals(S[i][2], noData)) {
                validNum++;
                if (validNum == 1) {
                    min = S[i][2];
                    max = min;
                } else {
                    if (S[i][2] < min) {
                        min = S[i][2];
                    }
                    if (S[i][2] > max) {
                        max = S[i][2];
                    }
                }
            } else {
                isNodata = true;
            }

        }
        
        minmax[0] = min;
        minmax[1] = max;

        return isNodata;
    }
    // </editor-fold>
}
