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

import org.meteoinfo.global.Extent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class StationModelData {
    // <editor-fold desc="Variables">

    private List<StationModel> _data = new ArrayList<>();
    private Extent _dataExtent = new Extent();
    private double _missingValue = -9999.0;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get data
     *
     * @return Data
     */
    public List<StationModel> getData() {
        return _data;
    }

    /**
     * Set data
     *
     * @param value Data
     */
    public void setData(List<StationModel> value) {
        _data = value;
    }

    /**
     * Get data extent
     *
     * @return Data extent
     */
    public Extent getDataExtent() {
        return _dataExtent;
    }

    /**
     * Set data extent
     *
     * @param value Data extent
     */
    public void setDataExtent(Extent value) {
        _dataExtent = value;
    }

    /**
     * Get missing data
     *
     * @return Missing data
     */
    public double getMissingValue() {
        return _missingValue;
    }

    /**
     * Set missing data
     *
     * @param value Missing data
     */
    public void setMissingValue(double value) {
        _missingValue = value;
    }

    /**
     * Get data number
     *
     * @return Data number
     */
    public int getDataNum() {
        return _data.size();
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
