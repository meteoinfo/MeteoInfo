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
package org.meteoinfo.geoprocess.analysis;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.data.meteodata.GridDataSetting;

/**
 *
 * @author yaqiang
 */
public class InterpolationSetting {
    // <editor-fold desc="Variables">

    private GridDataSetting _gridDataPara = new GridDataSetting();
    private InterpolationMethods _gridInterMethod;
    private int _minPointNum;
    private double _radius;
    private double _missingValue = -9999.0;
    private List<Double> _radList;    //For cressman analysis
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public InterpolationSetting() {
        _gridDataPara.xNum = 50;
        _gridDataPara.yNum = 50;
        _gridInterMethod = InterpolationMethods.IDW_Radius;
        _minPointNum = 1;
        _radius = 1;
        _radList = new ArrayList<>();
        double[] values = new double[]{10, 7, 4, 2, 1};
        for (double v : values) {
            _radList.add(v);
        }
    }

    /**
     * Constructor
     *
     * @param minX Minimum x
     * @param maxX Maximum x
     * @param minY Minimum y
     * @param maxY Maximum y
     * @param xNum X number
     * @param yNum Y number
     * @param aInterMethod Interpolation method
     * @param radius Radius
     * @param minNum Minimum number
     */
    public InterpolationSetting(double minX, double maxX, double minY, double maxY, int xNum, int yNum,
            String aInterMethod, float radius, int minNum) {
        GridDataSetting aGDP = new GridDataSetting();
        aGDP.dataExtent.minX = minX;
        aGDP.dataExtent.maxX = maxX;
        aGDP.dataExtent.minY = minY;
        aGDP.dataExtent.maxY = maxY;
        aGDP.xNum = xNum;
        aGDP.yNum = yNum;
        _gridDataPara = aGDP;

        _gridInterMethod = InterpolationMethods.valueOf(aInterMethod);
        _radius = radius;
        _minPointNum = minNum;

        _radList = new ArrayList<>();
        double[] values = new double[]{10, 7, 4, 2, 1};
        for (double v : values) {
            _radList.add(v);
        }
    }

    /**
     * Constructor
     *
     * @param minX Minimum x
     * @param maxX Maximum x
     * @param minY Minimum y
     * @param maxY Maximum y
     * @param xNum X number
     * @param yNum Y number
     * @param aInterMethod Interpolation method
     * @param radList Radius list - Cressman
     */
    public InterpolationSetting(double minX, double maxX, double minY, double maxY, int xNum, int yNum,
            String aInterMethod, List<Double> radList) {
        GridDataSetting aGDP = new GridDataSetting();
        aGDP.dataExtent.minX = minX;
        aGDP.dataExtent.maxX = maxX;
        aGDP.dataExtent.minY = minY;
        aGDP.dataExtent.maxY = maxY;
        aGDP.xNum = xNum;
        aGDP.yNum = yNum;
        _gridDataPara = aGDP;

        _gridInterMethod = InterpolationMethods.valueOf(aInterMethod);
        _radList = radList;
        _minPointNum = 1;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get grid data setting
     *
     * @return Grid data setting
     */
    public GridDataSetting getGridDataSetting() {
        return _gridDataPara;
    }

    /**
     * Set grid data setting
     *
     * @param value Grid data setting
     */
    public void setGridDataSetting(GridDataSetting value) {
        _gridDataPara = value;
    }

    /**
     * Get interpolation method
     *
     * @return Interpolation method
     */
    public InterpolationMethods getInterpolationMethod() {
        return _gridInterMethod;
    }

    /**
     * Set interpolation method
     *
     * @param value Interpolation method
     */
    public void setInterpolationMethod(InterpolationMethods value) {
        _gridInterMethod = value;
    }

    /**
     * Get minimum point number
     *
     * @return Minimum point number
     */
    public int getMinPointNum() {
        return _minPointNum;
    }

    /**
     * Set minimum point number
     *
     * @param value Minimum point number
     */
    public void setMinPointNum(int value) {
        _minPointNum = value;
    }

    /**
     * Get search radius
     *
     * @return Radius
     */
    public double getRadius() {
        return _radius;
    }

    /**
     * Set search radius
     *
     * @param value Radius
     */
    public void setRadius(double value) {
        _radius = value;
    }

    /**
     * Get missing value
     *
     * @return Missing value
     */
    public double getMissingValue() {
        return _missingValue;
    }

    /**
     * Set missing value
     *
     * @param value Missing value
     */
    public void setMissingValue(double value) {
        _missingValue = value;
    }

    /**
     * Get radius list
     *
     * @return Radius list
     */
    public List<Double> getRadiusList() {
        return _radList;
    }

    /**
     * Set radius list
     *
     * @param value Radius list
     */
    public void setRadiusList(List<Double> value) {
        _radList = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    
        
        
    // </editor-fold>
}
