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
package org.meteoinfo.data;

import java.util.ArrayList;
import org.meteoinfo.global.MIMath;
import java.util.Arrays;
import java.util.List;
import org.meteoinfo.table.ColumnData;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public abstract class DataMath {
    // <editor-fold desc="Operator">

    /**
     * Tack add operator of two objects
     *
     * @param a Object a
     * @param b Object b
     * @return Result object
     */
    public static Object add(Object a, Object b) {
        if (a.getClass() == GridData.class) {
            if (b.getClass() == GridData.class) {
                return ((GridData) a).add((GridData) b);
            } else {
                return ((GridData) a).add(Double.parseDouble(b.toString()));
            }
        } else if (a.getClass() == StationData.class) {
            if (b.getClass() == StationData.class) {
                return ((StationData) a).add((StationData) b);
            } else {
                return ((StationData) a).add(Double.parseDouble(b.toString()));
            }
        } else if (b.getClass() == GridData.class) {
            return ((GridData) b).add(Double.parseDouble(a.toString()));
        } else if (b.getClass() == StationData.class) {
            return ((StationData) b).add(Double.parseDouble(a.toString()));
        } else {
            return Double.parseDouble(a.toString()) + Double.parseDouble(b.toString());
        }
    }

    /**
     * Take subtract operator of two objects
     *
     * @param a Object a
     * @param b Object b
     * @return Result object
     */
    public static Object sub(Object a, Object b) {
        if (a.getClass() == GridData.class) {
            if (b.getClass() == GridData.class) {
                return ((GridData) a).sub((GridData) b);
            } else {
                return ((GridData) a).sub(Double.parseDouble(b.toString()));
            }
        } else if (a.getClass() == StationData.class) {
            if (b.getClass() == StationData.class) {
                return ((StationData) a).sub((StationData) b);
            } else {
                return ((StationData) a).sub(Double.parseDouble(b.toString()));
            }
        } else if (b.getClass() == GridData.class) {
            return sub(Double.parseDouble(a.toString()), (GridData) b);
        } else if (b.getClass() == StationData.class) {
            return sub(Double.parseDouble(a.toString()), (StationData) b);
        } else {
            return Double.parseDouble(a.toString()) - Double.parseDouble(b.toString());
        }
    }

    /**
     * Subtract operation between a double value and a grid data
     *
     * @param value Double value
     * @param gridData Grid data
     * @return Result grid data
     */
    public static GridData sub(double value, GridData gridData) {
        int xNum = gridData.getXNum();
        int yNum = gridData.getYNum();

        GridData cGrid = new GridData(gridData);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(gridData.data[i][j], gridData.missingValue)) {
                    cGrid.data[i][j] = gridData.missingValue;
                } else {
                    cGrid.data[i][j] = value - gridData.data[i][j];
                }
            }
        }

        return cGrid;
    }

    /**
     * Subtract operator between a double value and a station data
     *
     * @param value The value
     * @param stData Station data
     * @return Result station data
     */
    public static StationData sub(double value, StationData stData) {
        StationData cStData = new StationData();
        String aStid;
        double x, y;
        for (int i = 0; i < stData.stations.size(); i++) {
            aStid = stData.stations.get(i);

            double aValue = stData.getValue(i);
            x = stData.getX(i);
            y = stData.getY(i);
            if (MIMath.doubleEquals(aValue, stData.missingValue)) {
                cStData.addData(aStid, x, y, aValue);
            } else {
                cStData.addData(aStid, x, y, value - aValue);
            }
        }

        return cStData;
    }

    /**
     * Take multiply operator of two objects
     *
     * @param a Object a
     * @param b Object b
     * @return Result object
     */
    public static Object mul(Object a, Object b) {
        if (a.getClass() == GridData.class) {
            if (b.getClass() == GridData.class) {
                return ((GridData) a).mul((GridData) b);
            } else {
                return ((GridData) a).mul(Double.parseDouble(b.toString()));
            }
        } else if (a.getClass() == StationData.class) {
            if (b.getClass() == StationData.class) {
                return ((StationData) a).mul((StationData) b);
            } else {
                return ((StationData) a).mul(Double.parseDouble(b.toString()));
            }
        } else if (b.getClass() == GridData.class) {
            return ((GridData) b).mul(Double.parseDouble(a.toString()));
        } else if (b.getClass() == StationData.class) {
            return ((StationData) b).mul(Double.parseDouble(a.toString()));
        } else {
            return Double.parseDouble(a.toString()) * Double.parseDouble(b.toString());
        }
    }

    /**
     * Take divide operator of two objects
     *
     * @param a Object a
     * @param b Object b
     * @return Result object
     */
    public static Object div(Object a, Object b) {
        if (a.getClass() == GridData.class) {
            if (b.getClass() == GridData.class) {
                return ((GridData) a).div((GridData) b);
            } else {
                return ((GridData) a).div(Double.parseDouble(b.toString()));
            }
        } else if (a.getClass() == StationData.class) {
            if (b.getClass() == StationData.class) {
                return ((StationData) a).div((StationData) b);
            } else {
                return ((StationData) a).div(Double.parseDouble(b.toString()));
            }
        } else if (b.getClass() == GridData.class) {
            return div_1(Double.parseDouble(a.toString()), (GridData) b);
        } else if (b.getClass() == StationData.class) {
            return div_1(Double.parseDouble(a.toString()), (StationData) b);
        } else {
            return Double.parseDouble(a.toString()) / Double.parseDouble(b.toString());
        }
    }

    /**
     * Divide operation between a double value and a grid data
     *
     * @param value Double value
     * @param gridData Grid data
     * @return Result grid data
     */
    public static GridData div_1(double value, GridData gridData) {
        int xNum = gridData.getXNum();
        int yNum = gridData.getYNum();

        GridData cGrid = new GridData(gridData);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(gridData.data[i][j], gridData.missingValue)) {
                    cGrid.data[i][j] = gridData.missingValue;
                } else if (gridData.data[i][j] == 0) {
                    cGrid.data[i][j] = gridData.missingValue;
                } else {
                    cGrid.data[i][j] = value / gridData.data[i][j];
                }
            }
        }

        return cGrid;
    }

    /**
     * Divide operator between a double value and a station data
     *
     * @param value The value
     * @param stData Station data
     * @return Result station data
     */
    public static StationData div_1(double value, StationData stData) {
        StationData cStData = new StationData();
        String aStid;
        double x, y;
        for (int i = 0; i < stData.stations.size(); i++) {
            aStid = stData.stations.get(i);

            double aValue = stData.getValue(i);
            x = stData.getX(i);
            y = stData.getY(i);
            if (aValue == 0 || MIMath.doubleEquals(aValue, stData.missingValue)) {
                cStData.addData(aStid, x, y, aValue);
            } else {
                cStData.addData(aStid, x, y, value / aValue);
            }
        }

        return cStData;
    }

    // </editor-fold>
    // <editor-fold desc="Wind U/V">
    /**
     * Get wind U/V from wind direction/speed
     *
     * @param windDir The wind direction
     * @param windSpeed The wind speed
     * @return Wind U/V
     */
    public static double[] getUVFromDS(double windDir, double windSpeed) {
        double dir = windDir + 180;
        if (dir > 360) {
            dir = dir - 360;
        }

        dir = dir * Math.PI / 180;
        double U = windSpeed * Math.sin(dir);
        double V = windSpeed * Math.cos(dir);

        return new double[]{U, V};
    }

    /**
     * Get wind U/V grid data from wind direction/speed grid data
     *
     * @param windDirData Wind directoin grid data
     * @param windSpeedData Wind speed grid data
     * @return U/V grid data
     */
    public static GridData[] getUVFromDS(GridData windDirData, GridData windSpeedData) {
        GridData uData = new GridData(windDirData);
        GridData vData = new GridData(windDirData);
        double[] uv;
        for (int i = 0; i < windDirData.getYNum(); i++) {
            for (int j = 0; j < windDirData.getXNum(); j++) {
                if (MIMath.doubleEquals(windDirData.data[i][j], windDirData.missingValue)
                        || MIMath.doubleEquals(windSpeedData.data[i][j], windSpeedData.missingValue)) {
                    uData.data[i][j] = uData.missingValue;
                    vData.data[i][j] = vData.missingValue;
                } else {
                    uv = getUVFromDS(windDirData.data[i][j], windSpeedData.data[i][j]);
                    uData.data[i][j] = uv[0];
                    vData.data[i][j] = uv[1];
                }
            }
        }

        return new GridData[]{uData, vData};
    }

    /**
     * Get wind U/V station data from wind direction/speed station data
     *
     * @param windDirData Wind direction station data
     * @param windSpeedData Wind speed station data
     * @return U/V station data
     */
    public static StationData[] getUVFromDS(StationData windDirData, StationData windSpeedData) {
        StationData uData = new StationData(windDirData);
        StationData vData = new StationData(windSpeedData);
        double[] uv;
        for (int i = 0; i < windDirData.getStNum(); i++) {
            if (MIMath.doubleEquals(windDirData.data[i][2], windDirData.missingValue)
                    || MIMath.doubleEquals(windSpeedData.data[i][2], windSpeedData.missingValue)) {
                uData.data[i][2] = uData.missingValue;
                vData.data[i][2] = vData.missingValue;
            } else {
                uv = getUVFromDS(windDirData.data[i][2], windSpeedData.data[i][2]);
                uData.data[i][2] = uv[0];
                vData.data[i][2] = uv[1];
            }
        }
        return new StationData[]{uData, vData};
    }

    /**
     * Get wind direction/speed from U/V
     *
     * @param U The U value
     * @param V The V value
     * @return Wind direction/speed array
     */
    public static double[] getDSFromUV(double U, double V) {
        double windSpeed = Math.sqrt(U * U + V * V);
        double windDir;
        if (windSpeed == 0) {
            windDir = 0;
        } else {
            windDir = Math.asin(U / windSpeed) * 180 / Math.PI;
            if (U < 0 && V < 0) {
                windDir = 180.0 - windDir;
            } else if (U > 0 && V < 0) {
                windDir = 180.0 - windDir;
            } else if (U < 0 && V > 0) {
                windDir = 360.0 + windDir;
            }
            windDir += 180;
            if (windDir >= 360) {
                windDir -= 360;
            }
        }

        return new double[]{windDir, windSpeed};
    }
    
    /**
     * Get wind direction/speed grid data from wind U/V grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @return Wind direction/speed grid data
     */
    public static GridData[] getDSFromUV(GridData uData, GridData vData) {
        GridData windDirData = new GridData(uData);
        GridData windSpeedData = new GridData(uData);
        double[] ds;
        for (int i = 0; i < uData.getYNum(); i++) {
            for (int j = 0; j < uData.getXNum(); j++) {
                if (MIMath.doubleEquals(uData.data[i][j], uData.missingValue)
                        || MIMath.doubleEquals(vData.data[i][j], vData.missingValue)) {
                    windDirData.data[i][j] = windDirData.missingValue;
                    windSpeedData.data[i][j] = windSpeedData.missingValue;
                } else {
                    ds = getDSFromUV(uData.data[i][j], vData.data[i][j]);
                    windDirData.data[i][j] = ds[0];
                    windSpeedData.data[i][j] = ds[1];
                }
            }
        }

        return new GridData[]{windDirData, windSpeedData};
    }

    /**
     * Get wind direction/speed station data from wind U/V station data
     *
     * @param uData U station data
     * @param vData V station data
     * @return Wind direction/speed station data
     */
    public static StationData[] getDSFromUV(StationData uData, StationData vData) {
        StationData windDirData = new StationData(uData);
        StationData windSpeedData = new StationData(vData);
        double[] ds;
        for (int i = 0; i < windDirData.getStNum(); i++) {
            if (MIMath.doubleEquals(uData.data[i][2], uData.missingValue)
                    || MIMath.doubleEquals(vData.data[i][2], vData.missingValue)) {
                windDirData.data[i][2] = windDirData.missingValue;
                windSpeedData.data[i][2] = windSpeedData.missingValue;
            } else {
                ds = getDSFromUV(uData.data[i][2], vData.data[i][2]);
                windDirData.data[i][2] = ds[0];
                windSpeedData.data[i][2] = ds[1];
            }
        }

        return new StationData[]{windDirData, windSpeedData};
    }
    
    /**
     * Get end point by start point, angle and length
     * @param x Start point x
     * @param y Start point y
     * @param angle Angle
     * @param len Length
     * @return End point x/y values;
     */
    public static double[] getEndPoint(double x, double y, double angle, double len){
        double[] r = getUVFromDS(angle, len);
        r[0] += x;
        r[1] += y;
        return r;
    }
    // </editor-fold>
    // <editor-fold desc="Fitting">

    /**
     * Summary the value array
     *
     * @param values Values
     * @return Summary
     */
    public static double sum(double[] values) {
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            sum = sum + values[i];
        }
        sum = sum / values.length;

        return sum;
    }

    // </editor-fold>
    // <editor-fold desc="Function">
    /**
     * Take abstract value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object abs(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).abs();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).abs();
        } else {
            return Math.abs(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take anti-sine value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object asin(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).asin();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).asin();
        } else {
            return Math.asin(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take anti-cosine value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object acos(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).acos();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).acos();
        } else {
            return Math.acos(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take anti-tangent value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object atan(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).atan();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).atan();
        } else {
            return Math.atan(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take sine value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object sin(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).sin();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).sin();
        } else {
            return Math.sin(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take cosine value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object cos(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).cos();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).cos();
        } else {
            return Math.cos(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take tangent value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object tan(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).tan();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).tan();
        } else {
            return Math.tan(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take e base power value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object exp(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).exp();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).exp();
        } else {
            return Math.exp(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take power value
     *
     * @param a Object a
     * @param p Power value
     * @return Result object
     */
    public static Object pow(Object a, double p) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).pow(p);
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).pow(p);
        } else {
            return Math.pow(Double.parseDouble(a.toString()), p);
        }
    }

    /**
     * Take logrithm value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object log(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).log();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).log();
        } else {
            return Math.log(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take 10 base logrithm value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object log10(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).log10();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).log10();
        } else {
            return Math.log10(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Take squre root value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object sqrt(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).sqrt();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).sqrt();
        } else {
            return Math.sqrt(Double.parseDouble(a.toString()));
        }
    }

    /**
     * Calculate average grid data from grid data list
     *
     * @param gDataList Grid data list
     * @return Averaged grid data
     */
    public static GridData average(List<GridData> gDataList) {
        GridData rData = gDataList.get(0);
        for (int i = 1; i < gDataList.size(); i++) {
            rData = rData.add(gDataList.get(i));
        }
        rData = rData.div(gDataList.size());

        return rData;
    }

    /**
     * Calculate average grid data from grid data list
     *
     * @param gDataList Grid data list
     * @param ignoreUndef If ignore missing data
     * @return Averaged grid data
     */
    public static GridData average(List<GridData> gDataList, boolean ignoreUndef) {
        if (ignoreUndef) {
            return average(gDataList);
        } else {
            GridData rData = new GridData(gDataList.get(0));
            GridData numData = new GridData(gDataList.get(0));
            rData.setValue(0);
            numData.setValue(0);
            for (int d = 0; d < gDataList.size(); d++) {
                GridData aGrid = gDataList.get(d);
                for (int i = 0; i < rData.getYNum(); i++) {
                    for (int j = 0; j < rData.getXNum(); j++) {
                        if (!MIMath.doubleEquals(aGrid.data[i][j], aGrid.missingValue)) {
                            rData.data[i][j] = aGrid.data[i][j] + rData.data[i][j];
                            numData.data[i][j] += 1;
                        }
                    }
                }
            }

            for (int i = 0; i < rData.getYNum(); i++) {
                for (int j = 0; j < rData.getXNum(); j++) {
                    if (rData.data[i][j] == 0) {
                        rData.data[i][j] = rData.missingValue;
                    }
                }
            }
            rData = rData.div(numData);

            return rData;
        }
    }

    /**
     * Calculate average grid data from grid data list
     *
     * @param gDataList Grid data list
     * @param ignoreUndef If ignore missing value
     * @param validNum Valid number
     * @return Averaged grid data
     */
    public static GridData average(List<GridData> gDataList, boolean ignoreUndef, int validNum) {
        if (ignoreUndef) {
            return average(gDataList);
        } else {
            GridData rData = new GridData(gDataList.get(0));
            GridData numData = new GridData(gDataList.get(0));
            rData.setValue(0);
            numData.setValue(0);
            for (int d = 0; d < gDataList.size(); d++) {
                GridData aGrid = gDataList.get(d);
                for (int i = 0; i < rData.getYNum(); i++) {
                    for (int j = 0; j < rData.getXNum(); j++) {
                        if (!MIMath.doubleEquals(aGrid.data[i][j], aGrid.missingValue)) {
                            rData.data[i][j] = aGrid.data[i][j] + rData.data[i][j];
                            numData.data[i][j] += 1;
                        }
                    }
                }
            }

            for (int i = 0; i < rData.getYNum(); i++) {
                for (int j = 0; j < rData.getXNum(); j++) {
                    if (rData.data[i][j] == 0) {
                        rData.data[i][j] = rData.missingValue;
                    }
                }
            }
            numData.replaceValue(validNum, 0, false);
            rData = rData.div(numData);

            return rData;
        }
    }

    /**
     * Compute the arithmetic mean station data
     * @param datalist station data list
     * @return Mean station data
     */
    public static StationData mean(List<StationData> datalist) {
        StationData cStData = new StationData();
        StationData stdata = datalist.get(0);
        cStData.projInfo = stdata.projInfo;
        String aStid;
        int stIdx;
        double x, y;
        for (int i = 0; i < stdata.stations.size(); i++) {
            aStid = stdata.stations.get(i);
            if (aStid.equals("99999")) {
                continue;
            }

            double aValue = stdata.getValue(i);
            if (aValue == stdata.missingValue) {
                continue;
            }
            double sum = aValue;
            int n = 1;
            for (int j = 1; j < datalist.size(); j++) {
                StationData sd = datalist.get(j);
                stIdx = sd.stations.indexOf(aStid);
                if (stIdx >= 0) {
                    double bValue = sd.getValue(stIdx);
                    if (bValue == sd.missingValue) {
                        continue;
                    }
                    sum += bValue;
                    n += 1;
                }
            }
            sum = sum / n;
            x = stdata.getX(i);
            y = stdata.getY(i);
            cStData.addData(aStid, x, y, sum);
        }

        return cStData;
    }

    // </editor-fold>
    // <editor-fold desc="Statistics">
    /**
     * Determine the least square trend equation - linear fitting
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept and slope
     */
    public static double[] leastSquareTrend(double[] xData, double[] yData) {
        int n = xData.length;
        double sumX = 0.0;
        double sumY = 0.0;
        double sumSquareX = 0.0;
        double sumXY = 0.0;
        for (int i = 0; i < n; i++) {
            sumX += xData[i];
            sumY += yData[i];
            sumSquareX += xData[i] * xData[i];
            sumXY += xData[i] * yData[i];
        }

        double a = (sumSquareX * sumY - sumX * sumXY) / (n * sumSquareX - sumX * sumX);
        double b = (n * sumXY - sumX * sumY) / (n * sumSquareX - sumX * sumX);

        return new double[]{a, b};
    }

    /**
     * Determine the least square trend equation - linear fitting
     *
     * @param dataList Grid data list
     * @param xData X data array
     * @return Result grid data - slop
     */
    public static GridData leastSquareTrend(List<GridData> dataList, double[] xData) {
        int n = dataList.size();
        double[] yData = new double[n];
        double missingValue = dataList.get(0).missingValue;
        GridData rData = new GridData(dataList.get(0));
        rData = rData.setValue(missingValue);
        double value;
        boolean ifcal;
        for (int i = 0; i < dataList.get(0).getYNum(); i++) {
            for (int j = 0; j < dataList.get(0).getXNum(); j++) {
                ifcal = true;
                for (int d = 0; d < n; d++) {
                    value = dataList.get(d).data[i][j];
                    if (MIMath.doubleEquals(value, dataList.get(d).missingValue)) {
                        ifcal = false;
                        break;
                    }
                    yData[d] = dataList.get(d).data[i][j];
                }
                if (ifcal) {
                    rData.data[i][j] = leastSquareTrend(xData, yData)[1];
                }
            }
        }

        return rData;
    }

    /**
     * Get correlation coefficient How well did the forecast values correspond
     * to the observed values? Range: -1 to 1. Perfect score: 1.
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Correlation coefficent
     */
    public static float getR(List<Double> xData, List<Double> yData) {
        int n = xData.size();
        double x_sum = 0;
        double y_sum = 0;
        for (int i = 0; i < n; i++) {
            x_sum += xData.get(i);
            y_sum += yData.get(i);
        }
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        for (int i = 0; i < n; i++) {
            sx_sum += xData.get(i) * xData.get(i);
            sy_sum += yData.get(i) * yData.get(i);
            xy_sum += xData.get(i) * yData.get(i);
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        return (float) r;
    }

    /**
     * Get correlation coefficient How well did the forecast values correspond
     * to the observed values? Range: -1 to 1. Perfect score: 1.
     *
     * @param xcData X data array
     * @param ycData Y data array
     * @return Correlation coefficent
     */
    public static float getR(ColumnData xcData, ColumnData ycData) {
        List<Number> xData = xcData.getDataValues();
        List<Number> yData = ycData.getDataValues();
        List<Double> xxData = new ArrayList<>();
        List<Double> yyData = new ArrayList<>();
        for (int i = 0; i < xcData.size(); i++) {
            if (Double.isNaN(xData.get(i).doubleValue()) || Double.isNaN(yData.get(i).doubleValue())) {
                continue;
            }
            xxData.add(xData.get(i).doubleValue());
            yyData.add(yData.get(i).doubleValue());
        }

        return getR(xxData, yyData);
    }

    /**
     * Mann-Kendall trend statistics
     *
     * @param ts Input data list
     * @return Result array - z (trend)/beta (change value per unit time)
     */
    public static double[] mann_Kendall_Trend(List<Double> ts) {
        double[] nts = new double[ts.size()];
        for (int i = 0; i < ts.size(); i++) {
            nts[i] = ts.get(i);
        }

        return mann_Kendall_Trend(nts);
    }

    /**
     * Mann-Kendall trend statistics
     *
     * @param ts Input data array
     * @return Result array - z (trend)/beta (change value per unit time)
     */
    public static double[] mann_Kendall_Trend(double[] ts) {
        int i, j, s = 0, k = 0;
        int n = ts.length;
        double[] differ = new double[n * (n - 1) / 2];
        double z, beta;

        //Calculate z
        for (i = 0; i < n - 1; i++) {
            for (j = i + 1; j < n; j++) {
                if (ts[j] > ts[i]) {
                    s = s + 1;
                } else if (ts[j] < ts[i]) {
                    s = s - 1;
                }
                differ[k] = (ts[j] - ts[i]) / (j - i);
                k += 1;
            }
        }

        double var = n * (n - 1) * (2 * n + 5) / 18;

        if (s > 0) {
            z = (double) (s - 1) / Math.sqrt(var);
        } else if (s == 0) {
            z = 0;
        } else {
            z = (double) (s + 1) / Math.sqrt(var);
        }

        //Calculate beta
        Arrays.sort(differ);
        if (k % 2 == 0) {
            beta = (differ[k / 2] + differ[k / 2 + 1]) / 2;
        } else {
            beta = differ[k / 2 + 1];
        }

        return new double[]{z, beta};
    }

    /**
     * Mann-Kendall trend statistics
     *
     * @param ts Input data array
     * @return Result array - z (trend)/beta (change value per unit time)
     */
    public static double[] mann_Kendall_Trend_1(double[] ts) {
        int i, j, s = 0, k = 0;
        int n = ts.length;
        int p[] = new int[n - 1];
        int psum = 0;
        double[] differ = new double[n * (n - 1) / 2];
        double beta;

        //Calculate z
        for (i = 0; i < n - 1; i++) {
            s = 0;
            for (j = i + 1; j < n; j++) {
                if (ts[j] > ts[i]) {
                    s = s + 1;
                }
                differ[k] = (ts[j] - ts[i]) / (j - i);
                k += 1;
            }
            p[i] = s;
            psum += s;
        }

        double t = 4.0 * psum / (n * (n - 1)) - 1.0;
        double var = 2.0 * (2.0 * n + 5.0) / (9.0 * n * (n - 1));

        double u = t / Math.sqrt(var);

        //Calculate beta
        Arrays.sort(differ);
        if (k % 2 == 0) {
            beta = (differ[k / 2] + differ[k / 2 + 1]) / 2;
        } else {
            beta = differ[k / 2 + 1];
        }

        return new double[]{u, beta};
    }
    // </editor-fold>
    // <editor-fold desc="Spatial">

    /**
     * Take magnitude value from U/V grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @return Magnitude grid data
     */
    public static GridData magnitude(GridData uData, GridData vData) {
        int xNum = uData.getXNum();
        int yNum = uData.getYNum();

        GridData cGrid = new GridData(uData);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (Math.abs(uData.data[i][j] / uData.missingValue - 1) < 0.01
                        || Math.abs(vData.data[i][j] / vData.missingValue - 1) < 0.01) {
                    cGrid.data[i][j] = uData.missingValue;
                } else {
                    cGrid.data[i][j] = Math.sqrt(uData.data[i][j] * uData.data[i][j] + vData.data[i][j]
                            * vData.data[i][j]);
                }
            }
        }

        return cGrid;
    }

    /**
     * Take magnitude value from U/V station data
     *
     * @param uData U station data
     * @param vData V station data
     * @return Magnitude station data
     */
    public static StationData magnitude(StationData uData, StationData vData) {
        if (!MIMath.isExtentCross(uData.dataExtent, vData.dataExtent)) {
            return null;
        }

        StationData cStData = new StationData();
        List<double[]> cData = new ArrayList<>();
        String aStid;
        int stIdx = -1;
        double minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        for (int i = 0; i < uData.stations.size(); i++) {
            aStid = uData.stations.get(i);
            if (aStid.equals("99999")) {
                continue;
            }

            double aValue = uData.data[2][i];
            if (aValue == uData.missingValue) {
                continue;
            }

            stIdx = vData.stations.indexOf(aStid);
            if (stIdx >= 0) {
                double bValue = vData.data[2][stIdx];
                if (bValue == vData.missingValue) {
                    continue;
                }

                cStData.stations.add(aStid);
                double[] theData = new double[3];
                theData[0] = uData.data[0][i];
                theData[1] = uData.data[1][i];
                theData[2] = Math.sqrt(aValue * aValue + bValue * bValue);
                cData.add(theData);

                if (cStData.stations.size() == 1) {
                    minX = theData[0];
                    maxX = minX;
                    minY = theData[1];
                    maxY = minY;
                } else {
                    if (minX > theData[0]) {
                        minX = theData[0];
                    } else if (maxX < theData[0]) {
                        maxX = theData[0];
                    }
                    if (minY > theData[1]) {
                        minY = theData[1];
                    } else if (maxY < theData[1]) {
                        maxY = theData[1];
                    }
                }
            }
        }
        cStData.dataExtent.minX = minX;
        cStData.dataExtent.maxX = maxX;
        cStData.dataExtent.minY = minY;
        cStData.dataExtent.maxY = maxY;
        cStData.data = new double[3][cData.size()];
        for (int i = 0; i < cData.size(); i++) {
            cStData.data[0][i] = cData.get(i)[0];
            cStData.data[1][i] = cData.get(i)[1];
            cStData.data[2][i] = cData.get(i)[2];
        }

        return cStData;
    }

    /**
     * Performs a centered difference operation on a grid data in the x or y
     * direction
     *
     * @param aData The grid data
     * @param isX If is x direction
     * @return Result grid data
     */
    public static GridData cdiff(GridData aData, boolean isX) {
        int xnum = aData.getXNum();
        int ynum = aData.getYNum();
        GridData bData = new GridData(aData);
        for (int i = 0; i < ynum; i++) {
            for (int j = 0; j < xnum; j++) {
                if (i == 0 || i == ynum - 1 || j == 0 || j == xnum - 1) {
                    bData.data[i][j] = aData.missingValue;
                } else {
                    double a, b;
                    if (isX) {
                        a = aData.data[i][j + 1];
                        b = aData.data[i][j - 1];
                    } else {
                        a = aData.data[i + 1][j];
                        b = aData.data[i - 1][j];
                    }
                    if (MIMath.doubleEquals(a, aData.missingValue) || MIMath.doubleEquals(b, aData.missingValue)) {
                        bData.data[i][j] = aData.missingValue;
                    } else {
                        bData.data[i][j] = a - b;
                    }
                }
            }
        }

        return bData;
    }

    /**
     * Calculates the vertical component of the curl (ie, vorticity)
     *
     * @param uData U component
     * @param vData V component
     * @return Curl
     */
    public static GridData hcurl(GridData uData, GridData vData) {
        GridData lonData = new GridData(uData);
        GridData latData = new GridData(vData);
        int i, j;
        for (i = 0; i < uData.getYNum(); i++) {
            for (j = 0; j < uData.getXNum(); j++) {
                lonData.data[i][j] = uData.xArray[j];
                latData.data[i][j] = uData.yArray[i];
            }
        }
        GridData dv = cdiff(vData, true);
        GridData dx = cdiff(lonData, true).mul(Math.PI / 180);
        GridData du = cdiff(uData.mul((GridData) cos(latData.mul(Math.PI / 180))), false);
        GridData dy = cdiff(latData, false).mul(Math.PI / 180);
        GridData gData = (dv.div(dx).sub(du.div(dy))).div(((GridData) (cos(latData.mul(Math.PI / 180)))).mul(6.37e6));

        return gData;
    }

    /**
     * Calculates the horizontal divergence using finite differencing
     *
     * @param uData U component
     * @param vData V component
     * @return Divergence
     */
    public static GridData hdivg(GridData uData, GridData vData) {
        GridData lonData = new GridData(uData);
        GridData latData = new GridData(uData);
        int i, j;
        for (i = 0; i < uData.getYNum(); i++) {
            for (j = 0; j < uData.getXNum(); j++) {
                lonData.data[i][j] = uData.xArray[j];
                latData.data[i][j] = uData.yArray[i];
            }
        }
        GridData du = cdiff(uData, true);
        GridData dx = cdiff(lonData, true).mul(Math.PI / 180);
        GridData dv = cdiff(vData.mul((GridData) cos(latData.mul(Math.PI / 180))), false);
        GridData dy = cdiff(latData, false).mul(Math.PI / 180);
        GridData gData = (du.div(dx).add(dv.div(dy))).div(((GridData) cos(latData.mul(Math.PI / 180))).mul(6.37e6));

        return gData;
    }
    // </editor-fold>
    // <editor-fold desc="Gaussian">

    /**
     * This function provides latitudes on a Gaussian grid from the number of
     * latitude lines
     *
     * @param nlat the number of latitude lines
     * @return The latitudes of hemisphere
     */
    public static Object[] gauss2Lats(int nlat) {
        double acon = 180.0 / Math.PI;

        // convergence criterion for iteration of cos latitude
        double xlim = 1.0e-7;

        // initialise arrays
        int i;
        //int iNum = 720;
        int iNum = nlat;
        double[] cosc = new double[iNum];
        double[] gwt = new double[iNum];
        double[] sinc = new double[iNum];
        double[] colat = new double[iNum];
        double[] wos2 = new double[iNum];
        for (i = 0; i < iNum; i++) {
            cosc[i] = 0.0;
            gwt[i] = 0.0;
            sinc[i] = 0.0;
            colat[i] = 0.0;
            wos2[i] = 0.0;
        }

        // the number of zeros between pole and equator
        int nzero = nlat / 2;

        // set first guess for cos(colat)
        for (i = 1; i <= nzero; i++) {
            cosc[i - 1] = Math.sin((i - 0.5) * Math.PI / nlat + Math.PI * 0.5);
        }

        // constants for determining the derivative of the polynomial
        int fi = nlat;
        double fi1 = fi + 1.0;
        double a = fi * fi1 / Math.sqrt(4.0 * fi1 * fi1 - 1.0);
        double b = fi1 * fi / Math.sqrt(4.0 * fi * fi - 1.0);

        //loop over latitudes, iterating the search for each root
        double c, d;
        for (i = 0; i < nzero; i++) {
            // determine the value of the ordinary Legendre polynomial for the current guess root
            double g = gord(nlat, cosc[i]);
            // determine the derivative of the polynomial at this point
            double gm = gord(nlat - 1, cosc[i]);
            double gp = gord(nlat + 1, cosc[i]);
            double gt = (cosc[i] * cosc[i] - 1.0) / (a * gp - b * gm);
            // update the estimate of the root
            double delta = g * gt;
            cosc[i] = cosc[i] - delta;

            // if convergence criterion has not been met, keep trying
            while (Math.abs(delta) > xlim) {
                g = gord(nlat, cosc[i]);
                gm = gord(nlat - 1, cosc[i]);
                gp = gord(nlat + 1, cosc[i]);
                gt = (cosc[i] * cosc[i] - 1.0) / (a * gp - b * gm);
                delta = g * gt;
                cosc[i] = cosc[i] - delta;
            }
            // determine the Gaussian weights
            c = 2.0 * (1.0 - cosc[i] * cosc[i]);
            d = gord(nlat - 1, cosc[i]);
            d = d * d * fi * fi;
            gwt[i] = c * (fi - 0.5) / d;
        }

        // determine the colatitudes and sin(colat) and weights over sin**2
        for (i = 0; i < nzero; i++) {
            colat[i] = Math.acos(cosc[i]);
            sinc[i] = Math.sin(colat[i]);
            wos2[i] = gwt[i] / (sinc[i] * sinc[i]);
        }

        // if nlat is odd, set values at the equator
        if (nlat % 2 != 0) {
            i = nzero;
            cosc[i] = 0.0;
            c = 2.0;
            d = gord(nlat - 1, cosc[i]);
            d = d * d * fi * fi;
            gwt[i] = c * (fi - 0.5) / d;
            colat[i] = Math.PI * 0.5;
            sinc[i] = 1.0;
            wos2[i] = gwt[i];
        }

        // determine the southern hemisphere values by symmetry
        for (i = nlat - nzero; i < nlat; i++) {
            int j = nlat - i - 1;
            cosc[i] = -cosc[j];
            gwt[i] = gwt[j];
            colat[i] = Math.PI - colat[j];
            sinc[i] = sinc[j];
            wos2[i] = wos2[j];
        }

        double ylat = -90.0;

        // calculate latitudes and latitude spacing
        double[] xlat = new double[nlat];
        double[] dlat = new double[nlat];
        for (i = 0; i < nzero; i++) {
            xlat[i] = -Math.acos(sinc[i]) * acon;
            dlat[i] = xlat[i] - ylat;
            ylat = xlat[i];
        }

        if (nlat % 2 != 0) {
            i = nzero;
            xlat[i] = 0;
            dlat[i] = xlat[i] - ylat;
        }

        for (i = nlat - nzero; i < nlat; i++) {
            xlat[i] = Math.acos(sinc[i]) * acon;
            dlat[i] = xlat[i] - ylat;
            ylat = xlat[i];
        }

        //// calculate latitudes and latitude spacing
        //double[] xlat = new double[nlat];
        //double[] dlat = new double[nlat];
        //for (i = 0; i < nlat; i++)
        //{
        //    xlat[i] = Math.Acos(sinc[i]) * acon;
        //    dlat[i] = xlat[i] - ylat;
        //    ylat = xlat[i];
        //}
        return new Object[]{xlat, dlat};
    }

    /**
     * Calculates the value of an ordinary Legendre polynomial at a latitude
     *
     * @param n The degree of the polynomial
     * @param x Cos(colatitude)
     * @return The value of the Legendre polynomial of degree n at latitude
     * asin(x)
     */
    private static double gord(int n, double x) {
        //determine the colatitude
        double colat = Math.acos(x);
        double c1 = Math.sqrt(2.0);

        for (int i = 1; i <= n; i++) {
            c1 = c1 * Math.sqrt(1.0 - 1.0 / (4 * i * i));
        }

        int fn = n;
        double ang = fn * colat;
        double s1 = 0.0;
        double c4 = 1.0;
        double a = -1.0;
        double b = 0.0;

        for (int k = 0; k <= n; k = k + 2) {
            if (k == n) {
                c4 = 0.5 * c4;
            }

            s1 = s1 + c4 * Math.cos(ang);
            a = a + 2.0;
            b = b + 1.0;
            int fk = k;
            ang = colat * (fn - fk - 2.0);
            c4 = (a * (fn - b + 1.0) / (b * (fn + fn - a))) * c4;
        }
        return s1 * c1;
    }
    // </editor-fold>
}
