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

import java.io.BufferedReader;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.GridDataSetting;
import org.meteoinfo.geoprocess.analysis.ResampleMethods;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.ProjectionUtil;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.ShapeTypes;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;

/**
 *
 * @author yaqiang
 */
public class GridData {

    // <editor-fold desc="Variables">
    /**
     * Grid data
     */
    public double[][] data;
    /// <summary>
    /// x coordinate array
    /// </summary>
    public double[] xArray;
    /// <summary>
    /// y coordinate array
    /// </summary>
    public double[] yArray;
    /// <summary>
    /// Undef data
    /// </summary>
    public double missingValue;
    /**
     * Projection information
     */
    public ProjectionInfo projInfo = null;
    public String fieldName = "Data";
    private boolean _xStag = false;
    private boolean _yStag = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GridData() {
        missingValue = -9999;
    }

    /**
     * Constructor
     *
     * @param aGridData The grid data
     */
    public GridData(GridData aGridData) {
        projInfo = aGridData.projInfo;
        xArray = aGridData.xArray.clone();
        yArray = aGridData.yArray.clone();
        missingValue = aGridData.missingValue;
        data = new double[yArray.length][xArray.length];
    }

    /**
     * Constructor
     *
     * @param xStart xArray start
     * @param xDelt xArray delt
     * @param xNum xArray number
     * @param yStart yArray start
     * @param yDelt yArray delt
     * @param yNum yArray number
     */
    public GridData(double xStart, double xDelt, int xNum, double yStart, double yDelt, int yNum) {
        xArray = new double[xNum];
        yArray = new double[yNum];
        for (int i = 0; i < xNum; i++) {
            xArray[i] = BigDecimalUtil.add(xStart, BigDecimalUtil.mul(xDelt, i));
        }
        for (int i = 0; i < yNum; i++) {
            yArray[i] = BigDecimalUtil.add(yStart, BigDecimalUtil.mul(yDelt, i));
        }

        missingValue = -9999;
        data = new double[yNum][xNum];
    }

    /**
     * Constructor
     *
     * @param array Data array
     * @param xdata X data
     * @param ydata Y data
     * @param missingValue Missing value
     * @param projInfo Projection info
     */
    public GridData(Array array, List<Number> xdata, List<Number> ydata, double missingValue, ProjectionInfo projInfo) {
        int yn = ydata.size();
        int xn = xdata.size();
        this.data = new double[yn][xn];
        IndexIterator iter = array.getIndexIterator();
        int idx = 0;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (java.lang.Double.isNaN(val)) {
                data[idx / xn][idx % xn] = missingValue;
            } else {
                data[idx / xn][idx % xn] = val;
            }
            idx += 1;
        }

        this.xArray = new double[xn];
        this.yArray = new double[yn];
        for (int i = 0; i < xn; i++) {
            this.xArray[i] = xdata.get(i).doubleValue();
        }
        for (int i = 0; i < yn; i++) {
            this.yArray[i] = ydata.get(i).doubleValue();
        }

        this.missingValue = missingValue;
        this.projInfo = projInfo;
    }
    
    /**
     * Constructor
     *
     * @param array Data array
     * @param xdata X data
     * @param ydata Y data
     */
    public GridData(Array array, Array xdata, Array ydata) {
        this(array, xdata, ydata, -9999.0);
    }

    /**
     * Constructor
     *
     * @param array Data array
     * @param xdata X data
     * @param ydata Y data
     * @param missingValue Missing value
     */
    public GridData(Array array, Array xdata, Array ydata, Number missingValue) {
        int yn = (int) ydata.getSize();
        int xn = (int) xdata.getSize();
        this.data = new double[yn][xn];
        IndexIterator iter = array.getIndexIterator();
        int idx = 0;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (java.lang.Double.isNaN(val)) {
                data[idx / xn][idx % xn] = missingValue.doubleValue();
            } else {
                data[idx / xn][idx % xn] = val;
            }
            idx += 1;
        }

        this.xArray = new double[xn];
        this.yArray = new double[yn];
        iter = xdata.getIndexIterator();
        int i = 0;
        while(iter.hasNext()) {
            this.xArray[i] = iter.getDoubleNext();
            i++;
        }
        iter = ydata.getIndexIterator();
        i = 0;
        while(iter.hasNext()) {
            this.yArray[i] = iter.getDoubleNext();
            i++;
        }

        this.missingValue = missingValue.doubleValue();
        this.projInfo = KnownCoordinateSystems.geographic.world.WGS1984;;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get xArray number
     *
     * @return xArray number
     */
    public int getXNum() {
        return xArray.length;
    }

    /**
     * Get yArray number
     *
     * @return yArray number
     */
    public int getYNum() {
        return yArray.length;
    }

    /**
     * Get xArray delt
     *
     * @return xArray delt
     */
    public double getXDelt() {
        return xArray[1] - xArray[0];
    }

    /**
     * Get yArray delt
     *
     * @return yArray delt
     */
    public double getYDelt() {
        return yArray[1] - yArray[0];
    }

    /**
     * Get Extent
     *
     * @return Extent
     */
    public Extent getExtent() {
        Extent extent = new Extent();
        extent.minX = xArray[0];
        extent.maxX = xArray[xArray.length - 1];
        extent.minY = yArray[0];
        extent.maxY = yArray[yArray.length - 1];

        return extent;
    }

    /**
     * Get if the data is global
     *
     * @return If the data is global
     */
    public boolean isGlobal() {
        boolean isGlobal = false;
        if (MIMath.doubleEquals(xArray[getXNum() - 1] + getXDelt() - xArray[0], 360.0)) {
            isGlobal = true;
        }

        return isGlobal;
    }

    /**
     * Get if is x stagger
     *
     * @return Boolean
     */
    public boolean isXStagger() {
        return _xStag;
    }

    /**
     * Set if is x stagger
     *
     * @param value Boolean
     */
    public void setXStagger(boolean value) {
        _xStag = value;
    }

    /**
     * Get if is y stagger
     *
     * @return Boolean
     */
    public boolean isYStagger() {
        return _yStag;
    }

    /**
     * Set if is y stagger
     *
     * @param value Boolean
     */
    public void setYStagger(boolean value) {
        _yStag = value;
    }

    /**
     * Get value
     *
     * @param i I index
     * @param j J index
     * @return Value
     */
    public Number getValue(int i, int j) {
        return data[i][j];
    }

    /**
     * Get double value
     *
     * @param i I index
     * @param j J index
     * @return Double value
     */
    public double getDoubleValue(int i, int j) {
        return data[i][j];
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Operation">
    /**
     * Add operation with another grid data
     *
     * @param bGrid The grid data
     * @return Added grid data
     */
    public GridData add(GridData bGrid) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)
                        || MIMath.doubleEquals(bGrid.data[i][j], bGrid.missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = data[i][j] + bGrid.data[i][j];
                }
            }
        }

        return cGrid;
    }

    /**
     * Add operation with a double value
     *
     * @param value Double value
     * @return Added grid data
     */
    public GridData add(double value) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = data[i][j] + value;
                }
            }
        }

        return cGrid;
    }

    /**
     * Subtraction operation with another grid data
     *
     * @param bGrid The grid data
     * @return Subtracted grid data
     */
    public GridData sub(GridData bGrid) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)
                        || MIMath.doubleEquals(bGrid.data[i][j], bGrid.missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = data[i][j] - bGrid.data[i][j];
                }
            }
        }

        return cGrid;
    }

    /**
     * Subtraction operation with a double value
     *
     * @param value The double value
     * @return Subtracted grid data
     */
    public GridData sub(double value) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = data[i][j] - value;
                }
            }
        }

        return cGrid;
    }

    /**
     * Multiply operation with another grid data
     *
     * @param bGrid The grid data
     * @return Result grid data
     */
    public GridData mul(GridData bGrid) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)
                        || MIMath.doubleEquals(bGrid.data[i][j], bGrid.missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = data[i][j] * bGrid.data[i][j];
                }
            }
        }

        return cGrid;
    }

    /**
     * Multiply operation with a double value
     *
     * @param value Double value
     * @return Result grid data
     */
    public GridData mul(double value) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = data[i][j] * value;
                }
            }
        }

        return cGrid;
    }

    /**
     * Divide operation with another grid data
     *
     * @param bGrid The grid data
     * @return Result grid data
     */
    public GridData div(GridData bGrid) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)
                        || MIMath.doubleEquals(bGrid.data[i][j], bGrid.missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else if (bGrid.data[i][j] == 0) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = data[i][j] / bGrid.data[i][j];
                }
            }
        }

        return cGrid;
    }

    /**
     * Divide operation with a double value
     *
     * @param value Double value
     * @return Result grid data
     */
    public GridData div(double value) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = data[i][j] / value;
                }
            }
        }

        return cGrid;
    }

    /**
     * Regrid data with double linear interpolation method
     *
     * @param gridData Result grid data
     */
    public void regrid(GridData gridData) {
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                gridData.data[i][j] = toStation(gridData.xArray[j], gridData.yArray[i]);
            }
        }
    }

    /**
     * Interpolate grid data to a station point
     *
     * @param x X coordinate of the station
     * @param y Y coordinate of the station
     * @return Interpolated value
     */
    public double toStation(double x, double y) {
        double iValue = missingValue;
        if (x < xArray[0] || x > xArray[this.getXNum() - 1] || y < yArray[0] || y > yArray[this.getYNum() - 1]) {
            return iValue;
        }

        //Get x/y index
        double DX = this.getXDelt();
        double DY = this.getYDelt();
        int xIdx = 0, yIdx = 0;
        xIdx = (int) ((x - xArray[0]) / DX);
        yIdx = (int) ((y - yArray[0]) / DY);
        if (xIdx == this.getXNum() - 1) {
            xIdx = this.getXNum() - 2;
        }

        if (yIdx == this.getYNum() - 1) {
            yIdx = this.getYNum() - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        double a = data[i1][j1];
        double b = data[i1][j2];
        double c = data[i2][j1];
        double d = data[i2][j2];
        List<java.lang.Double> dList = new ArrayList<>();
        if (!MIMath.doubleEquals(a, missingValue)) {
            dList.add(a);
        }
        if (!MIMath.doubleEquals(b, missingValue)) {
            dList.add(b);
        }
        if (!MIMath.doubleEquals(c, missingValue)) {
            dList.add(c);
        }
        if (!MIMath.doubleEquals(d, missingValue)) {
            dList.add(d);
        }

        if (dList.isEmpty()) {
            return iValue;
        } else if (dList.size() == 1) {
            iValue = dList.get(0);
        } else if (dList.size() <= 3) {
            double aSum = 0;
            for (double dd : dList) {
                aSum += dd;
            }
            iValue = aSum / dList.size();
        } else {
            double x1val = a + (c - a) * (y - yArray[i1]) / DY;
            double x2val = b + (d - b) * (y - yArray[i1]) / DY;
            iValue = x1val + (x2val - x1val) * (x - xArray[j1]) / DX;
        }

        return iValue;
    }

    /**
     * Interpolate grid data to station points
     *
     * @param xlist X coordinate list
     * @param ylist Y coordinate list
     * @return Result data list
     */
    public List<java.lang.Double> toStation(List<Number> xlist, List<Number> ylist) {
        List<java.lang.Double> r = new ArrayList<>();
        double x, y, v;
        for (int i = 0; i < xlist.size(); i++) {
            x = xlist.get(i).doubleValue();
            y = ylist.get(i).doubleValue();
            v = this.toStation(x, y);
            r.add(v);
        }

        return r;
    }

    /**
     * Interpolate grid data to station data
     *
     * @param stData Station data
     * @return Interpolated station data
     */
    public StationData toStation(StationData stData) {
        StationData nstData = new StationData(stData);
        nstData.missingValue = this.missingValue;
        if (this.projInfo.equals(stData.projInfo)) {
            for (int i = 0; i < nstData.getStNum(); i++) {
                nstData.setValue(i, this.toStation(nstData.getX(i), nstData.getY(i)));
            }
        } else {
            nstData = this.project(projInfo, stData.projInfo, stData, ResampleMethods.Bilinear);
        }

        return nstData;
    }

    /**
     * Interpolate grid data to station data
     *
     * @param stData Station table data
     */
    public void toStation(StationTableData stData) {
        //DataTable dataTable = stData.dataTable;
        int lonIdx = stData.getLonIndex();
        int latIdx = stData.getLatIndex();
        double x, y;
        try {
            stData.addColumn(this.fieldName, DataType.FLOAT);
        } catch (Exception ex) {
            Logger.getLogger(GridData.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (this.projInfo.equals(stData.getProjectionInfo())) {
            for (int i = 0; i < stData.getRowCount(); i++) {
                x = java.lang.Double.parseDouble(stData.getValue(i, lonIdx).toString());
                y = java.lang.Double.parseDouble(stData.getValue(i, latIdx).toString());
                stData.setValue(i, fieldName, this.toStation(x, y));
            }
        } else {
            StationData sdata = new StationData();
            for (int i = 0; i < stData.getRowCount(); i++) {
                x = java.lang.Double.parseDouble(stData.getValue(i, lonIdx).toString());
                y = java.lang.Double.parseDouble(stData.getValue(i, latIdx).toString());
                sdata.addData("S_" + String.valueOf(i), x, y, 0);
            }
            sdata = this.project(projInfo, sdata.projInfo, sdata, ResampleMethods.Bilinear);
            for (int i = 0; i < stData.getRowCount(); i++) {
                stData.setValue(i, fieldName, sdata.getValue(i));
            }
        }
    }

    /**
     * Interpolate grid data to stations imported from station file
     *
     * @param inFile Input station file
     * @param outFile Output station file
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.FileNotFoundException
     */
    public void toStation(String inFile, String outFile) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        if (!new File(inFile).exists()) {
            return;
        }

        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "utf-8"));
        BufferedWriter sw = new BufferedWriter(new FileWriter(new File(outFile)));

        //Header
        String aLine = sr.readLine();
        aLine = aLine + ",data";
        sw.write(aLine);
        sw.newLine();

        //Data
        String[] dArray;
        String stId;
        double x, y;
        double value;
        aLine = sr.readLine();
        while (aLine != null) {
            aLine = aLine.trim();
            dArray = aLine.split(",");
            if (dArray.length < 3) {
                continue;
            }

            stId = dArray[0].trim();
            x = java.lang.Double.parseDouble(dArray[1].trim());
            y = java.lang.Double.parseDouble(dArray[2].trim());
            value = toStation(x, y);
            if (!java.lang.Double.isNaN(value)) {
                aLine = aLine + "," + String.valueOf(value);
                sw.write(aLine);
                sw.newLine();
            }
            aLine = sr.readLine();
        }

        sr.close();
        sw.close();
    }

    /**
     * Set grid values by anthor grid data - overlay
     *
     * @param bGrid The grid data
     * @return Result grid data
     */
    public GridData setValue(GridData bGrid) {
        Extent aExtent = this.getExtent();
        Extent bExtent = bGrid.getExtent();
        if (!MIMath.isExtentCross(aExtent, bExtent)) {
            return this;
        }

        int xNum = this.getXNum();
        int yNum = this.getYNum();
        double axdelt = this.getXDelt();
        double aydelt = this.getYDelt();
        int sXidx = 0;
        int eXidx = xNum - 1;
        int sYidx = 0;
        int eYidx = yNum - 1;
        if (aExtent.minX < bExtent.minX) {
            sXidx = (int) ((bExtent.minX - aExtent.minX) / axdelt);
        }
        if (aExtent.maxX > bExtent.maxX) {
            eXidx = (int) ((bExtent.maxX - aExtent.minX) / axdelt);
        }
        if (aExtent.minY < bExtent.minY) {
            sYidx = (int) ((bExtent.minY - aExtent.minY) / aydelt);
        }
        if (aExtent.maxY > bExtent.maxY) {
            eYidx = (int) ((bExtent.maxY - aExtent.minY) / aydelt);
        }

        GridData cGrid = (GridData) this.clone();
        int xidx, yidx;
        double bxdelt = bGrid.getXDelt();
        double bydelt = bGrid.getYDelt();
        for (int i = sYidx; i <= eYidx; i++) {
            for (int j = sXidx; j <= eXidx; j++) {
                xidx = (int) ((xArray[j] - bGrid.xArray[0]) / bxdelt);
                if (xidx < 0 || xidx >= bGrid.getXNum()) {
                    continue;
                }
                yidx = (int) ((yArray[i] - bGrid.yArray[0]) / bydelt);
                if (yidx < 0 || yidx >= bGrid.getYNum()) {
                    continue;
                }
                cGrid.data[i][j] = bGrid.data[yidx][xidx];
            }
        }

        return cGrid;
    }

    /**
     * Set grid values by anthor grid data - overlay
     *
     * @param bGrid The grid data
     * @param useMissingData if set missing data
     * @return Result grid data
     */
    public GridData setValue(GridData bGrid, boolean useMissingData) {
        Extent aExtent = this.getExtent();
        Extent bExtent = bGrid.getExtent();
        if (!MIMath.isExtentCross(aExtent, bExtent)) {
            return this;
        }

        int xNum = this.getXNum();
        int yNum = this.getYNum();
        double axdelt = this.getXDelt();
        double aydelt = this.getYDelt();
        int sXidx = 0;
        int eXidx = xNum - 1;
        int sYidx = 0;
        int eYidx = yNum - 1;
        if (aExtent.minX < bExtent.minX) {
            sXidx = (int) ((bExtent.minX - aExtent.minX) / axdelt);
        }
        if (aExtent.maxX > bExtent.maxX) {
            eXidx = (int) ((bExtent.maxX - aExtent.minX) / axdelt);
        }
        if (aExtent.minY < bExtent.minY) {
            sYidx = (int) ((bExtent.minY - aExtent.minY) / aydelt);
        }
        if (aExtent.maxY > bExtent.maxY) {
            eYidx = (int) ((bExtent.maxY - aExtent.minY) / aydelt);
        }

        GridData cGrid = (GridData) this.clone();
        int xidx, yidx;
        double bxdelt = bGrid.getXDelt();
        double bydelt = bGrid.getYDelt();
        for (int i = sYidx; i <= eYidx; i++) {
            for (int j = sXidx; j <= eXidx; j++) {
                xidx = (int) ((xArray[j] - bGrid.xArray[0]) / bxdelt);
                if (xidx < 0 || xidx >= bGrid.getXNum()) {
                    continue;
                }
                yidx = (int) ((yArray[i] - bGrid.yArray[0]) / bydelt);
                if (yidx < 0 || yidx >= bGrid.getYNum()) {
                    continue;
                }
                if (useMissingData) {
                    if (MIMath.doubleEquals(bGrid.data[yidx][xidx], bGrid.missingValue)) {
                        continue;
                    }
                }
                cGrid.data[i][j] = bGrid.data[yidx][xidx];
            }
        }

        return cGrid;
    }

    /**
     * Set constant value to all grid cells
     *
     * @param aValue The value
     * @return Result grid data
     */
    public GridData setValue(double aValue) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                cGrid.data[i][j] = aValue;
            }
        }

        return cGrid;
    }

    /**
     * Replace a certain grid data value by a new value
     *
     * @param aValue Old value
     * @param bValue New value
     */
    public void replaceValue(double aValue, double bValue) {
        for (int i = 0; i < getYNum(); i++) {
            for (int j = 0; j < getXNum(); j++) {
                if (data[i][j] == aValue) {
                    data[i][j] = bValue;
                }
            }
        }
    }

    /**
     * Replace grid data value by a threshold - the values bigger/smaller than
     * the threshold value will be replaced by the new value
     *
     * @param aValue Threshold value
     * @param bValue New value
     * @param bigger Bigger or smaller
     */
    public void replaceValue(double aValue, double bValue, boolean bigger) {
        for (int i = 0; i < getYNum(); i++) {
            for (int j = 0; j < getXNum(); j++) {
                if (bigger) {
                    if (data[i][j] > aValue) {
                        data[i][j] = bValue;
                    }
                } else if (data[i][j] < aValue) {
                    data[i][j] = bValue;
                }
            }
        }
    }

    /**
     * Merge grid values by anthor grid data - the two grids should have same
     * extent replace missing value with valid data
     *
     * @param bGrid The grid data
     * @return Result grid data
     */
    public GridData merge(GridData bGrid) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = (GridData) this.clone();
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)
                        && (!MIMath.doubleEquals(bGrid.data[i][j], bGrid.missingValue))) {
                    cGrid.data[i][j] = bGrid.data[i][j];
                }
            }
        }

        return cGrid;
    }

    /**
     * Maximum operation with another grid data
     *
     * @param bGrid The grid data
     * @return Maximum grid data
     */
    public GridData max(GridData bGrid) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)
                        || MIMath.doubleEquals(bGrid.data[i][j], bGrid.missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = Math.max(data[i][j], bGrid.data[i][j]);
                }
            }
        }

        return cGrid;
    }

    /**
     * Minimum operation with another grid data
     *
     * @param bGrid The grid data
     * @return Minimum grid data
     */
    public GridData min(GridData bGrid) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)
                        || MIMath.doubleEquals(bGrid.data[i][j], bGrid.missingValue)) {
                    cGrid.data[i][j] = missingValue;
                } else {
                    cGrid.data[i][j] = Math.min(data[i][j], bGrid.data[i][j]);
                }
            }
        }

        return cGrid;
    }

    /**
     * Calculate summary value
     *
     * @return Summary value
     */
    public double sum() {
        double sum = 0;
        for (int i = 0; i < this.getYNum(); i++) {
            for (int j = 0; j < this.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    continue;
                }

                sum += data[i][j];
            }
        }

        return sum;
    }

    /**
     * Calculate average value
     *
     * @return Average value
     */
    public double average() {
        double ave = 0;
        int vdNum = 0;
        for (int i = 0; i < this.getYNum(); i++) {
            for (int j = 0; j < this.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    continue;
                }

                ave += data[i][j];
                vdNum += 1;
            }
        }

        ave = ave / vdNum;

        return ave;
    }

    /**
     * Calculate average data array by another grid data and threshold values
     *
     * @param bGrid The grid data
     * @param tValues Threshold values
     * @return Average values
     */
    public double[] average(GridData bGrid, double[] tValues) {
        int vn = tValues.length;
        int n = vn + 1;
        double[] aves = new double[n];
        int[] vdNum = new int[n];
        int k;
        double v, bv;
        for (int i = 0; i < this.getYNum(); i++) {
            for (int j = 0; j < this.getXNum(); j++) {
                v = data[i][j];
                bv = bGrid.data[i][j];
                if (MIMath.doubleEquals(v, missingValue)) {
                    continue;
                }

                if (MIMath.doubleEquals(bv, bGrid.missingValue)) {
                    continue;
                }

                if (bv >= tValues[vn - 1]) {
                    k = vn;
                } else {
                    for (k = 0; k < vn; k++) {
                        if (bv < tValues[k]) {
                            break;
                        }
                    }
                }
                aves[k] += v;
                vdNum[k] += 1;
            }
        }

        for (k = 0; k < n; k++) {
            if (vdNum[k] > 0) {
                aves[k] = aves[k] / vdNum[k];
            } else {
                aves[k] = missingValue;
            }
        }

        return aves;
    }

    /**
     * Simple statistics of the grid data
     *
     * @return Result array - average, standard deviation
     */
    public double[] statistics() {
        double theMean, theSqDev, theSumSqDev, theVariance, theStdDev, theValue;

        theMean = average();
        theSumSqDev = 0;
        int vdNum = 0;
        for (int i = 0; i < this.getYNum(); i++) {
            for (int j = 0; j < this.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    continue;
                }

                theValue = data[i][j];
                theSqDev = (theValue - theMean) * (theValue - theMean);
                theSumSqDev = theSqDev + theSumSqDev;
                vdNum += 1;
            }
        }

        theVariance = theSumSqDev / (vdNum - 1);
        theStdDev = Math.sqrt(theVariance);
        double sem = theStdDev / Math.sqrt(vdNum);

        return new double[]{theMean, theStdDev, sem};
    }
    // </editor-fold>
    // <editor-fold desc="Functions">

    /**
     * Calculate abstract grid data
     *
     * @return Result grid data
     */
    public GridData abs() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.abs(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate anti-cosine grid data
     *
     * @return Result grid data
     */
    public GridData acos() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.acos(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate anti-sine grid data
     *
     * @return Result grid data
     */
    public GridData asin() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.asin(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate anti-tangent grid data
     *
     * @return Result grid data
     */
    public GridData atan() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.atan(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate cosine grid data
     *
     * @return Result grid data
     */
    public GridData cos() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.cos(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate sine grid data
     *
     * @return Result grid data
     */
    public GridData sin() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.sin(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate tangent grid data
     *
     * @return Result grid data
     */
    public GridData tan() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.abs(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate e raised specific power value of grid data
     *
     * @return Result grid data
     */
    public GridData exp() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.exp(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate power grid data
     *
     * @param p Power value
     * @return Result grid data
     */
    public GridData pow(double p) {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.pow(data[i][j], p);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate square root grid data
     *
     * @return Result grid data
     */
    public GridData sqrt() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.sqrt(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate logrithm grid data
     *
     * @return Result grid data
     */
    public GridData log() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.log(data[i][j]);
                }
            }
        }

        return gridData;
    }

    /**
     * Calculate base 10 logrithm grid data
     *
     * @return Result grid data
     */
    public GridData log10() {
        GridData gridData = new GridData(this);
        for (int i = 0; i < gridData.getYNum(); i++) {
            for (int j = 0; j < gridData.getXNum(); j++) {
                if (MIMath.doubleEquals(data[i][j], missingValue)) {
                    gridData.data[i][j] = missingValue;
                } else {
                    gridData.data[i][j] = Math.abs(data[i][j]);
                }
            }
        }

        return gridData;
    }
    // </editor-fold>
    // <editor-fold desc="Convert data">

    /**
     * Extend the grid data to global by add a new column data
     */
    public void extendToGlobal() {
        int yNum = getYNum();
        int xNum = getXNum();
        double[][] newGriddata = new double[yNum][xNum + 1];
        double[] newX = new double[xNum + 1];
        int i, j;
        for (i = 0; i < xNum; i++) {
            newX[i] = xArray[i];
        }

        newX[xNum] = newX[xNum - 1] + getXDelt();
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                newGriddata[i][j] = data[i][j];
            }
            newGriddata[i][xNum] = newGriddata[i][0];
        }

        data = newGriddata;
        xArray = newX;
    }

    /**
     * Get array from data
     *
     * @return Array
     */
    public Array getArray() {
        int yn = this.getYNum();
        int xn = this.getXNum();
        int[] shape = new int[]{yn, xn};
        Array r = Array.factory(DataType.DOUBLE, shape);
        for (int i = 0; i < yn; i++) {
            for (int j = 0; j < xn; j++) {
                r.setDouble(i * xn + j, this.data[i][j]);
            }
        }

        return r;
    }

    /**
     * Get dimensions
     *
     * @return Dimensions
     */
    public List<Dimension> getDimensions() {
        List<Dimension> dims = new ArrayList<>();
        Dimension ydim = new Dimension(DimensionType.Y);
        ydim.setValues(this.yArray);
        dims.add(ydim);
        Dimension xdim = new Dimension(DimensionType.X);
        xdim.setValues(this.xArray);
        dims.add(xdim);

        return dims;
    }

    // </editor-fold>
    // <editor-fold desc="Modify">
    /**
     * Extract grid data by extent
     *
     * @param extent Extent
     * @return Extracted grid data
     */
    public GridData extract(Extent extent) {
        return extract(extent.minX, extent.maxX, extent.minY, extent.maxY);
    }

    /**
     * Extract grid data by extent
     *
     * @param sX Start X
     * @param eX End X
     * @param sY Start Y
     * @param eY End Y
     * @return Grid data
     */
    public GridData extract(double sX, double eX, double sY, double eY) {
        if (eX <= sX || eY <= sY) {
            return null;
        }

        int xNum = this.getXNum();
        int yNum = this.getYNum();
        if (sX >= xArray[xNum - 2] || sY >= yArray[yNum - 2]) {
            return null;
        }

        GridData aGridData = new GridData();
        aGridData.projInfo = projInfo;
        aGridData.missingValue = missingValue;
        int sXidx = 0, eXidx = xNum - 1, sYidx = 0, eYidx = yNum - 1;

        //Get start x
        int i;
        if (sX <= xArray[0]) {
            sXidx = 0;
        } else {
            for (i = 0; i < xNum; i++) {
                if (sX <= xArray[i]) {
                    sXidx = i;
                    break;
                }
            }
        }

        //Get end x            
        if (eX >= xArray[xNum - 1]) {
            eXidx = xNum - 1;
        } else {
            for (i = sXidx; i < xNum; i++) {
                if (MIMath.doubleEquals(eX, xArray[i])) {
                    eXidx = i;
                    break;
                } else if (eX < xArray[i]) {
                    eXidx = i - 1;
                    break;
                }
            }
        }

        //Get star y            
        if (sY <= yArray[0]) {
            sYidx = 0;
        } else {
            for (i = 0; i < yNum; i++) {
                if (sY <= yArray[i]) {
                    sYidx = i;
                    break;
                }
            }
        }

        //Get end y            
        if (eY >= yArray[yNum - 1]) {
            eYidx = yNum - 1;
        } else {
            for (i = sYidx; i < yNum; i++) {
                if (MIMath.doubleEquals(eY, yArray[i])) {
                    eYidx = i;
                    break;
                } else if (eY < yArray[i]) {
                    eYidx = i - 1;
                    break;
                }
            }
        }

        int newXNum = eXidx - sXidx + 1;
        double[] newX = new double[newXNum];
        for (i = sXidx; i <= eXidx; i++) {
            newX[i - sXidx] = xArray[i];
        }

        int newYNum = eYidx - sYidx + 1;
        double[] newY = new double[newYNum];
        for (i = sYidx; i <= eYidx; i++) {
            newY[i - sYidx] = yArray[i];
        }

        aGridData.xArray = newX;
        aGridData.yArray = newY;

        double[][] newData = new double[newYNum][newXNum];
        for (i = sYidx; i <= eYidx; i++) {
            for (int j = sXidx; j <= eXidx; j++) {
                newData[i - sYidx][j - sXidx] = data[i][j];
            }
        }
        aGridData.data = newData;

        return aGridData;
    }

    /**
     * Extract grid data by extent index
     *
     * @param sXIdx Start x index
     * @param sYIdx Start y index
     * @param xNum X number
     * @param yNum Y number
     * @return Extracted grid data
     */
    public GridData extract(int sXIdx, int sYIdx, int xNum, int yNum) {
        GridData aGridData = new GridData();
        aGridData.projInfo = projInfo;
        aGridData.missingValue = missingValue;
        int eXIdx = sXIdx + xNum - 1, eYIdx = sYIdx + yNum - 1;
        double[] newX = new double[xNum];
        int i;
        for (i = sXIdx; i <= eXIdx; i++) {
            newX[i - sXIdx] = xArray[i];
        }

        double[] newY = new double[yNum];
        for (i = sYIdx; i <= eYIdx; i++) {
            newY[i - sYIdx] = yArray[i];
        }

        aGridData.xArray = newX;
        aGridData.yArray = newY;

        double[][] newData = new double[yNum][xNum];
        for (i = sYIdx; i <= eYIdx; i++) {
            for (int j = sXIdx; j <= eXIdx; j++) {
                newData[i - sYIdx][j - sXIdx] = data[i][j];
            }
        }
        aGridData.data = newData;

        return aGridData;
    }

    /**
     * Extract grid data by extent index
     *
     * @param sXIdx Start x index
     * @param eXIdx End x index
     * @param xstep X step
     * @param sYIdx Start y index
     * @param eYIdx End y index
     * @param ystep Y step
     * @return Extracted grid data
     */
    public GridData extract(int sXIdx, int eXIdx, int xstep, int sYIdx, int eYIdx, int ystep) {
        GridData aGridData = new GridData();
        aGridData.projInfo = projInfo;
        aGridData.missingValue = missingValue;
        int xNum = (eXIdx - sXIdx) / xstep;
        int yNum = (eYIdx - sYIdx) / ystep;
        double[] newX = new double[xNum];
        int i, idx = 0;
        for (i = sXIdx; i < eXIdx; i += xstep) {
            newX[idx] = xArray[i];
            idx += 1;
        }

        double[] newY = new double[yNum];
        idx = 0;
        for (i = sYIdx; i < eYIdx; i += ystep) {
            newY[idx] = yArray[i];
            idx += 1;
        }

        aGridData.xArray = newX;
        aGridData.yArray = newY;

        double[][] newData = new double[yNum][xNum];
        int yidx = 0;
        for (i = sYIdx; i < eYIdx; i += ystep) {
            int xidx = 0;
            for (int j = sXIdx; j < eXIdx; j += xstep) {
                newData[yidx][xidx] = data[i][j];
                xidx += 1;
            }
            yidx += 1;
        }
        aGridData.data = newData;

        return aGridData;
    }

    /**
     * Set missing value - bigger or smaller than the given value
     *
     * @param value The given value
     * @param isBigger Is bigger or not
     */
    public void setMissingValue(double value, boolean isBigger) {
        int xnum = this.getXNum();
        int ynum = this.getYNum();
        for (int i = 0; i < ynum; i++) {
            for (int j = 0; j < xnum; j++) {
                if (isBigger) {
                    if (this.data[i][j] > value) {
                        this.data[i][j] = this.missingValue;
                    }
                } else if (this.data[i][j] < value) {
                    this.data[i][j] = this.missingValue;
                }
            }
        }
    }

    /**
     * Skip the grid data by two dimension skip factor
     *
     * @param skipI Skip number in x coordinate
     * @param skipJ Skip number in y coordinate
     * @return Grid data
     */
    public GridData skip(int skipI, int skipJ) {
        int yNum = (getYNum() + skipI - 1) / skipI;
        int xNum = (getXNum() + skipJ - 1) / skipJ;
        int i, j, idxI, idxJ;

        GridData gdata = new GridData();
        gdata.missingValue = missingValue;
        gdata.xArray = new double[xNum];
        gdata.yArray = new double[yNum];
        gdata.data = new double[yNum][xNum];

        for (i = 0; i < yNum; i++) {
            idxI = i * skipI;
            gdata.yArray[i] = yArray[idxI];
        }
        for (j = 0; j < xNum; j++) {
            idxJ = j * skipJ;
            gdata.xArray[j] = xArray[idxJ];
        }
        for (i = 0; i < yNum; i++) {
            idxI = i * skipI;
            for (j = 0; j < xNum; j++) {
                idxJ = j * skipJ;
                gdata.data[i][j] = data[idxI][idxJ];
            }
        }

        return gdata;
    }

    /**
     * Get a cell value by X/Y coordinate - nearest cell
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return Cell value
     */
    public double getValue(double x, double y) {
        double iValue = missingValue;
        int xnum = this.getXNum();
        int ynum = this.getYNum();
        if (x < xArray[0] || x > xArray[xnum - 1] || y < yArray[0] || y > yArray[ynum - 1]) {
            return iValue;
        }

        //Get x/y index
        int xIdx = 0, yIdx = 0;
        xIdx = (int) ((x - xArray[0]) / this.getXDelt());
        yIdx = (int) ((y - yArray[0]) / this.getYDelt());
        if (xIdx == xnum - 1) {
            xIdx = xnum - 2;
        }

        if (yIdx == ynum - 1) {
            yIdx = ynum - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;

        if (x - xArray[j1] < xArray[j2] - x) {
            xIdx = j1;
            if (y - yArray[i1] < yArray[i2] - y) {
                yIdx = i1;
            } else {
                yIdx = i2;
            }
        } else {
            xIdx = j2;
            if (y - yArray[i1] < yArray[i2] - y) {
                yIdx = i1;
            } else {
                yIdx = i2;
            }
        }

        iValue = data[yIdx][xIdx];
        return iValue;
    }
    // </editor-fold>
    // <editor-fold desc="Gaussian grid">

    /**
     * Interpolate grid data to a station point
     *
     * @param x X coordinate of the station
     * @param y Y coordinate of the station
     * @return Interpolated value
     */
    public double toStation_Gaussian(double x, double y) {
        double iValue = missingValue;
        double xmin = this.getXMin();
        double xmax = this.getXMax();
        double ymin = this.getYMin();
        double ymax = this.getYMax();
        if (x < xmin || x > xmax || y < ymin || y > ymax) {
            return iValue;
        }

        //Get x/y index
        double xdelta = this.getXDelt();
        int xIdx = (int) ((x - xmin) / xdelta);
        int yIdx = 0;
        int i;
        int xnum = this.getXNum();
        int ynum = this.getYNum();

        if (xIdx == xnum - 1) {
            xIdx = xnum - 2;
        }

        for (i = 0; i < ynum - 1; i++) {
            if (y >= yArray[i] && y <= yArray[i + 1]) {
                yIdx = i;
                break;
            }
        }
        if (yIdx == ynum - 1) {
            yIdx = ynum - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        double a = data[i1][j1];
        double b = data[i1][j2];
        double c = data[i2][j1];
        double d = data[i2][j2];
        List<java.lang.Double> dList = new ArrayList<>();
        if (a != missingValue) {
            dList.add(a);
        }
        if (b != missingValue) {
            dList.add(b);
        }
        if (c != missingValue) {
            dList.add(c);
        }
        if (d != missingValue) {
            dList.add(d);
        }

        if (dList.isEmpty()) {
            return iValue;
        } else if (dList.size() == 1) {
            iValue = dList.get(0);
        } else if (dList.size() <= 3) {
            double aSum = 0;
            for (double dd : dList) {
                aSum += dd;
            }
            iValue = aSum / dList.size();
        } else {
            double ydelta = yArray[i2] - yArray[i1];
            double x1val = a + (c - a) * (y - yArray[i1]) / ydelta;
            double x2val = b + (d - b) * (y - yArray[i1]) / ydelta;
            iValue = x1val + (x2val - x1val) * (x - xArray[j1]) / xdelta;
        }

        return iValue;
    }

    /**
     * Convert Gassian grid to lat/lon grid
     */
    public void gassianToLatLon() {
        double ymin = this.getYMin();
        double ymax = this.getYMax();
        int xnum = this.getXNum();
        int ynum = this.getYNum();
        double delta = (ymax - ymin) / (ynum - 1);
        double[] newY = new double[ynum];
        for (int i = 0; i < ynum; i++) {
            newY[i] = ymin + i * delta;
        }

        double[][] newData = new double[ynum][xnum];
        for (int i = 0; i < ynum; i++) {
            for (int j = 0; j < xnum; j++) {
                newData[i][j] = toStation_Gaussian(xArray[j], newY[i]);
            }
        }

        this.yArray = newY;
        this.data = newData;
    }

    /**
     * Convert Gassian grid to lat/lon grid - only convert Y coordinate
     */
    public void GassianToLatLon_Simple() {
        double ymin = this.getYMin();
        double ymax = this.getYMax();
        int ynum = this.getYNum();
        double delta = (ymax - ymin) / (ynum - 1);
        double[] newY = new double[ynum];
        for (int i = 0; i < ynum; i++) {
            newY[i] = ymin + i * delta;
        }

        this.yArray = newY;
    }
    // </editor-fold>
    // <editor-fold desc="Others">

    /**
     * Get minimum x
     *
     * @return Minimum x
     */
    public double getXMin() {
        return xArray[0];
    }

    /**
     * Get maximum x
     *
     * @return Maximum x
     */
    public double getXMax() {
        return xArray[xArray.length - 1];
    }

    /**
     * Get minimum y
     *
     * @return Minimum y
     */
    public double getYMin() {
        return yArray[0];
    }

    /**
     * Get maximum y
     *
     * @return Maximum y
     */
    public double getYMax() {
        return yArray[yArray.length - 1];
    }

    /**
     * Get minimum x of the grid border
     *
     * @return Minimum x of the grid border
     */
    public double getBorderXMin() {
        return this.getXMin() - this.getXDelt() / 2;
    }

    /**
     * Get maximum x of the grid border
     *
     * @return Maximum x of the grid border
     */
    public double getBorderXMax() {
        return this.getXMax() + this.getXDelt() / 2;
    }

    /**
     * Get minimum y of the grid border
     *
     * @return Minimum y of the grid border
     */
    public double getBorderYMin() {
        return this.getYMin() - this.getYDelt() / 2;
    }

    /**
     * Get maximum y of the grid border
     *
     * @return Maximum y of the grid border
     */
    public double getBorderYMax() {
        return this.getYMax() + this.getYDelt() / 2;
    }

    /**
     * Get i/j index of a point in the grid
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return I/J index array
     */
    public int[] getIJIndex(double x, double y) {
        int xidx = -1;
        int yidx = -1;
        if (x >= this.getBorderXMin() && x <= this.getBorderXMax()) {
            if (y >= this.getBorderYMin() && y <= this.getBorderYMax()) {
                xidx = (int) ((x - this.getBorderXMin()) / this.getXDelt());
                yidx = (int) ((y - this.getBorderYMin()) / this.getYDelt());
            }
        }
        if (xidx >= this.getXNum() || yidx >= this.getYNum()) {
            xidx = -1;
            yidx = -1;
        }

        return new int[]{xidx, yidx};
    }

    /**
     * Save as Surfer ASCII data file
     *
     * @param aFile File path
     */
    public void saveAsSurferASCIIFile(String aFile) {
        try {
            BufferedWriter sw = new BufferedWriter(new FileWriter(new File(aFile)));
            sw.write("DSAA");
            sw.newLine();
            sw.write(String.valueOf(this.getXNum()) + " " + String.valueOf(this.getYNum()));
            sw.newLine();
            sw.write(String.valueOf(xArray[0]) + " " + String.valueOf(xArray[xArray.length - 1]));
            sw.newLine();
            sw.write(String.valueOf(yArray[0]) + " " + String.valueOf(yArray[yArray.length - 1]));
            sw.newLine();
            double[] maxmin = new double[2];
            getMaxMinValue(maxmin);
            sw.write(String.valueOf(maxmin[1]) + " " + String.valueOf(maxmin[0]));
            double value;
            String aLine = "";
            for (int i = 0; i < this.getYNum(); i++) {
                for (int j = 0; j < this.getXNum(); j++) {
                    if (MIMath.doubleEquals(data[i][j], missingValue)) {
                        value = -9999.0;
                    } else {
                        value = data[i][j];
                    }

                    if (j == 0) {
                        aLine = String.valueOf(value);
                    } else {
                        aLine = aLine + " " + String.valueOf(value);
                    }
                }
                sw.newLine();
                sw.write(aLine);
            }
            sw.flush();
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(GridData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save as ESRI ASCII data file
     *
     * @param aFile File path
     */
    public void saveAsESRIASCIIFile(String aFile) {
        try {
            BufferedWriter sw = new BufferedWriter(new FileWriter(new File(aFile)));
            sw.write("NCOLS " + String.valueOf(this.getXNum()));
            sw.newLine();
            sw.write("NROWS " + String.valueOf(this.getYNum()));
            sw.newLine();
            sw.write("XLLCENTER " + String.valueOf(xArray[0]));
            sw.newLine();
            sw.write("YLLCENTER " + String.valueOf(yArray[0]));
            sw.newLine();
            sw.write("CELLSIZE " + String.valueOf(this.getXDelt()));
            sw.newLine();
            sw.write("NODATA_VALUE " + String.valueOf(this.missingValue));
            sw.newLine();
            double value;
            String aLine = "";
            int xn = this.getXNum();
            int yn = this.getYNum();
            for (int i = 0; i < yn; i++) {
                for (int j = 0; j < xn; j++) {
                    value = data[yn - i - 1][j];
                    if (j == 0) {
                        aLine = String.valueOf(value);
                    } else {
                        aLine = aLine + " " + String.valueOf(value);
                    }
                }
                sw.newLine();
                sw.write(aLine);
            }
            sw.flush();
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(GridData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save as BIL data file
     *
     * @param fileName File path
     * @throws java.io.IOException
     */
    public void saveAsBILFile(String fileName) throws IOException {
        try {
            //Save data file
            DataOutputStream outs = new DataOutputStream(new FileOutputStream(new File(fileName)));
            int xn = this.getXNum();
            int yn = this.getYNum();
            for (int i = 0; i < yn; i++) {
                for (int j = 0; j < xn; j++) {
                    outs.writeFloat((float) data[yn - i - 1][j]);
                }
            }
            outs.close();

            //Save header file
            String hfn = fileName.replace(".bil", ".hdr");
            BufferedWriter sw = new BufferedWriter(new FileWriter(new File(hfn)));
            sw.write("nrows " + String.valueOf(this.getYNum()));
            sw.newLine();
            sw.write("ncols " + String.valueOf(this.getXNum()));
            sw.newLine();
            sw.write("nbands 1");
            sw.newLine();
            sw.write("nbits 32");
            sw.newLine();
            sw.write("pixeltype float");
            sw.newLine();
            sw.write("byteorder M");
            sw.newLine();
            sw.write("layout bil");
            sw.newLine();
            sw.write("ulxmap " + String.valueOf(xArray[0]));
            sw.newLine();
            sw.write("ulymap " + String.valueOf(yArray[yArray.length - 1]));
            sw.newLine();
            sw.write("xdim " + String.valueOf(this.getXDelt()));
            sw.newLine();
            sw.write("ydim " + String.valueOf(this.getYDelt()));
            sw.newLine();

            sw.flush();
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(GridData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save as station data file
     *
     * @param filePath File Path
     * @param fieldName Field name
     */
    public void saveAsStationData(String filePath, String fieldName) {
        try {
            BufferedWriter sw = new BufferedWriter(new FileWriter(new File(filePath)));
            sw.write("Id,X,Y," + fieldName);
            String aLine = "";
            int id = 1;
            for (int i = 0; i < this.getYNum(); i++) {
                for (int j = 0; j < this.getXNum(); j++) {
                    if (!MIMath.doubleEquals(data[i][j], missingValue)) {
                        aLine = String.valueOf(id) + "," + String.valueOf(xArray[j]) + "," + String.valueOf(yArray[i]) + "," + String.valueOf(data[i][j]);
                        sw.newLine();
                        sw.write(aLine);
                    }
                }
            }
            sw.flush();
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(GridData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get maximum and minimum values
     *
     * @param maxmin Max/Min array
     * @return If has undefine data
     */
    public boolean getMaxMinValue(double[] maxmin) {
        double max = 0;
        double min = 0;
        int vdNum = 0;
        boolean hasUndef = false;
        for (int i = 0; i < getYNum(); i++) {
            for (int j = 0; j < getXNum(); j++) {
                if (java.lang.Double.isNaN(data[i][j]) || MIMath.doubleEquals(data[i][j], missingValue)) {
                    hasUndef = true;
                    continue;
                }

                if (vdNum == 0) {
                    min = data[i][j];
                    max = min;
                } else {
                    if (min > data[i][j]) {
                        min = data[i][j];
                    }
                    if (max < data[i][j]) {
                        max = data[i][j];
                    }
                }
                vdNum += 1;
            }
        }

        maxmin[0] = max;
        maxmin[1] = min;
        return hasUndef;
    }

    /**
     * Get if has missing value
     *
     * @return Boolean
     */
    public boolean hasMissing() {
        boolean hasNaN = false;
        for (int i = 0; i < getYNum(); i++) {
            for (int j = 0; j < getXNum(); j++) {
                if (java.lang.Double.isNaN(data[i][j]) || MIMath.doubleEquals(data[i][j], missingValue)) {
                    hasNaN = true;
                    break;
                }
            }
        }
        return hasNaN;
    }

    /**
     * Set missing value
     * @param value Missing value
     */
    public void setMissingValue(double value) {
        if (java.lang.Double.isNaN(value)) {
            value = -9999.;
            int yn = this.getYNum();
            int xn = this.getXNum();
            for (int i = 0; i < yn; i++) {
                for (int j = 0; j < xn; j++) {
                    if (java.lang.Double.isNaN(this.data[i][j]))
                        this.data[i][j] = value;
                }
            }
        }
        this.missingValue = value;
    }


    /**
     * Get if has NaN value
     *
     * @return Boolean
     */
    public boolean hasNaN() {
        boolean hasNaN = false;
        for (int i = 0; i < getYNum(); i++) {
            for (int j = 0; j < getXNum(); j++) {
                if (java.lang.Double.isNaN(data[i][j])) {
                    hasNaN = true;
                    break;
                }
            }
        }
        return hasNaN;
    }

    /**
     * Get maximum and minimum values
     *
     * @return Max/Min array
     */
    public double[] getMaxMinValue() {
        double max = 0;
        double min = 0;
        int vdNum = 0;
        for (int i = 0; i < getYNum(); i++) {
            for (int j = 0; j < getXNum(); j++) {
                if (java.lang.Double.isNaN(data[i][j]) || MIMath.doubleEquals(data[i][j], missingValue)) {
                    continue;
                }

                if (vdNum == 0) {
                    min = data[i][j];
                    max = min;
                } else {
                    if (min > data[i][j]) {
                        min = data[i][j];
                    }
                    if (max < data[i][j]) {
                        max = data[i][j];
                    }
                }
                vdNum += 1;
            }
        }

        return new double[]{max, min};
    }

    /**
     * Get maximum value
     *
     * @return Maximum value
     */
    public double getMaxValue() {
        return this.getMaxMinValue()[0];
    }

    /**
     * Get minimum value
     *
     * @return Minimum value
     */
    public double getMinValue() {
        return this.getMaxMinValue()[1];
    }

    /**
     * Test unique values
     *
     * @return True if unique value number less then 20
     */
    public boolean testUniqueValues() {
        List<Number> values = new ArrayList<>();
        int vdNum = 0;
        for (int i = 0; i < getYNum(); i++) {
            for (int j = 0; j < getXNum(); j++) {
                if (MIMath.doubleEquals(this.getValue(i, j).doubleValue(), missingValue)) {
                    continue;
                }

                if (vdNum == 0) {
                    values.add(this.getValue(i, j));
                    vdNum += 1;
                } else if (!values.contains(this.getValue(i, j))) {
                    values.add(this.getValue(i, j));
                    vdNum += 1;
                }
                if (vdNum > 20) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get unique values
     *
     * @return Unique values
     */
    public List<Number> getUniqueValues() {
        List<Number> values = new ArrayList<>();
        int vdNum = 0;
        for (int i = 0; i < getYNum(); i++) {
            for (int j = 0; j < getXNum(); j++) {
                if (MIMath.doubleEquals(this.getValue(i, j).doubleValue(), missingValue)) {
                    continue;
                }

                if (vdNum == 0) {
                    values.add(this.getValue(i, j));
                } else if (!values.contains(this.getValue(i, j))) {
                    values.add(this.getValue(i, j));
                }
                vdNum += 1;
            }
        }

        return values;
    }

    /**
     * Get grid data setting
     *
     * @return Grid data setting
     */
    public GridDataSetting getGridDataSetting() {
        GridDataSetting gDataSet = new GridDataSetting();
        gDataSet.dataExtent = (Extent) this.getExtent().clone();
        gDataSet.xNum = this.getXNum();
        gDataSet.yNum = this.getYNum();
        return gDataSet;
    }

    /**
     * Mask out grid data with a polygon shape
     *
     * @param aPGS The polygon shape
     * @return Maskouted grid data
     */
    public GridData maskout(PolygonShape aPGS) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            if (yArray[i] >= aPGS.getExtent().minY && yArray[i] <= aPGS.getExtent().maxY) {
                for (int j = 0; j < xNum; j++) {
                    if (xArray[j] >= aPGS.getExtent().minX && xArray[j] <= aPGS.getExtent().maxX) {
                        if (GeoComputation.pointInPolygon(aPGS, new PointD(xArray[j], yArray[i]))) {
                            cGrid.data[i][j] = data[i][j];
                        } else {
                            cGrid.data[i][j] = missingValue;
                        }
                    } else {
                        cGrid.data[i][j] = missingValue;
                    }
                }
            } else {
                for (int j = 0; j < xNum; j++) {
                    cGrid.data[i][j] = missingValue;
                }
            }
        }

        return cGrid;
    }

    /**
     * Mask out grid data with polygon shapes
     *
     * @param polygons The polygon shapes
     * @return Maskouted grid data
     */
    public GridData maskout(List<PolygonShape> polygons) {
        int xNum = this.getXNum();
        int yNum = this.getYNum();

        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(xArray[j], yArray[i]))) {
                    cGrid.data[i][j] = data[i][j];
                } else {
                    cGrid.data[i][j] = missingValue;
                }
            }
        }

        return cGrid;
    }

    /**
     * Mask out grid data with a polygon layer
     *
     * @param maskLayer The polygon layer
     * @return Maskouted grid data
     */
    public GridData maskout(VectorLayer maskLayer) {
        if (maskLayer.getShapeType() != ShapeTypes.Polygon) {
            return this;
        }

        int xNum = this.getXNum();
        int yNum = this.getYNum();
        GridData cGrid = new GridData(this);
        for (int i = 0; i < yNum; i++) {
            if (yArray[i] >= maskLayer.getExtent().minY && yArray[i] <= maskLayer.getExtent().maxY) {
                for (int j = 0; j < xNum; j++) {
                    if (xArray[j] >= maskLayer.getExtent().minX && xArray[j] <= maskLayer.getExtent().maxX) {
                        if (GeoComputation.pointInPolygonLayer(maskLayer, new PointD(xArray[j], yArray[i]), false)) {
                            cGrid.data[i][j] = data[i][j];
                        } else {
                            cGrid.data[i][j] = missingValue;
                        }
                    } else {
                        cGrid.data[i][j] = missingValue;
                    }
                }
            } else {
                for (int j = 0; j < xNum; j++) {
                    cGrid.data[i][j] = missingValue;
                }
            }
        }

        return cGrid;
    }

    /**
     * Mask out grid data by a mask grid data
     *
     * @param maskGrid Mask grid data
     * @return Result grid data
     */
    public GridData maskout(GridData maskGrid) {
        GridData cGrid = new GridData(this);
        int xnum = this.getXNum();
        int ynum = this.getYNum();
        for (int i = 0; i < ynum; i++) {
            for (int j = 0; j < xnum; j++) {
                if (maskGrid.data[i][j] >= 0) {
                    cGrid.data[i][j] = data[i][j];
                } else {
                    cGrid.data[i][j] = missingValue;
                }
            }
        }

        return cGrid;
    }

    /**
     * Resample grid data
     *
     * @param toGridData The grid data
     * @param method The resample method
     * @return Result grid data
     */
    public GridData resample(GridData toGridData, ResampleMethods method) {
        GridData gridData;
        if (this.projInfo.equals(toGridData.projInfo)) {
            switch (method) {
                case NearestNeighbor:
                    gridData = resample_Neighbor(toGridData.xArray, toGridData.yArray);
                    break;
                default:
                    gridData = resample_Bilinear(toGridData.xArray, toGridData.yArray);
                    break;
            }
        } else {
            gridData = this.project(toGridData.projInfo, toGridData.xArray, toGridData.yArray, method);
        }

        gridData.projInfo = toGridData.projInfo;

        return gridData;
    }

    private GridData resample_Neighbor(double[] newX, double[] newY) {
        double[][] newdata = new double[newY.length][newX.length];
        int i, j, xIdx, yIdx;
        double x, y;

        double[][] points = new double[1][];
        for (i = 0; i < newY.length; i++) {
            y = newY[i];
            for (j = 0; j < newX.length; j++) {
                points[0] = new double[]{newX[j], newY[i]};
                x = newX[j];
                if (x < xArray[0] || x > xArray[xArray.length - 1]) {
                    newdata[i][j] = missingValue;
                } else if (y < yArray[0] || y > yArray[yArray.length - 1]) {
                    newdata[i][j] = missingValue;
                } else {
                    xIdx = (int) ((x - xArray[0]) / getXDelt());
                    yIdx = (int) ((y - yArray[0]) / getYDelt());
                    newdata[i][j] = data[yIdx][xIdx];
                }
            }
        }

        GridData gData = new GridData(this);
        gData.data = newdata;
        gData.xArray = newX;
        gData.yArray = newY;

        return gData;
    }

    private GridData resample_Bilinear(double[] newX, double[] newY) {
        //PointD[][] pos = new PointD[newY.length][newX.length];
        double[][] newdata = new double[newY.length][newX.length];
        int i, j;
        double x, y;

        double[][] points = new double[1][];
        for (i = 0; i < newY.length; i++) {
            y = newY[i];
            for (j = 0; j < newX.length; j++) {
                points[0] = new double[]{newX[j], newY[i]};
                x = newX[j];
                if (x < xArray[0] || x > xArray[xArray.length - 1]) {
                    newdata[i][j] = missingValue;
                } else if (y < yArray[0] || y > yArray[yArray.length - 1]) {
                    newdata[i][j] = missingValue;
                } else {
                    newdata[i][j] = this.toStation(x, y);
                }
            }
        }

        GridData gData = new GridData(this);
        gData.data = newdata;
        gData.xArray = newX;
        gData.yArray = newY;

        return gData;
    }

    /**
     * Interpolate grid data
     *
     * @return Result grid data
     */
    public GridData interpolate() {
        int nxNum = this.xArray.length * 2 - 1;
        int nyNum = this.yArray.length * 2 - 1;
        double[] newX = new double[nxNum];
        double[] newY = new double[nyNum];

        double[][] newData = interpolation_Grid(data, this.xArray, this.yArray, missingValue, newX, newY);
        int i;
        for (i = 0; i < nxNum; i++) {
            if (i % 2 == 0) {
                newX[i] = this.xArray[i / 2];
            } else {
                newX[i] = (this.xArray[(i - 1) / 2] + this.xArray[(i - 1) / 2 + 1]) / 2;
            }
        }
        for (i = 0; i < nyNum; i++) {
            if (i % 2 == 0) {
                newY[i] = this.yArray[i / 2];
            } else {
                newY[i] = (this.yArray[(i - 1) / 2] + this.yArray[(i - 1) / 2 + 1]) / 2;
            }
        }
        GridData gdata = new GridData();
        gdata.data = newData;
        gdata.xArray = newX;
        gdata.yArray = newY;
        gdata.missingValue = this.missingValue;

        return gdata;
    }

    /**
     * Interpolate from grid data
     *
     * @param GridData input grid data
     * @param X input x coordinates
     * @param Y input y coordinates
     * @param unDefData undefine data
     * @param nX output x coordinate
     * @param nY output y coordinate
     * @return output grid data
     */
    public double[][] interpolation_Grid(double[][] GridData, double[] X, double[] Y, double unDefData,
            double[] nX, double[] nY) {
        //int xNum = X.length;
        //int yNum = Y.length;
        int nxNum = X.length * 2 - 1;
        int nyNum = Y.length * 2 - 1;
        nX = new double[nxNum];
        nY = new double[nyNum];
        double[][] nGridData = new double[nyNum][nxNum];
        int i, j;
        double a, b, c, d;
        List<java.lang.Double> dList;
        for (i = 0; i < nxNum; i++) {
            if (i % 2 == 0) {
                nX[i] = X[i / 2];
            } else {
                nX[i] = (X[(i - 1) / 2] + X[(i - 1) / 2 + 1]) / 2;
            }
        }
        for (i = 0; i < nyNum; i++) {
            if (i % 2 == 0) {
                nY[i] = Y[i / 2];
            } else {
                nY[i] = (Y[(i - 1) / 2] + Y[(i - 1) / 2 + 1]) / 2;
            }
            for (j = 0; j < nxNum; j++) {
                if (i % 2 == 0 && j % 2 == 0) {
                    nGridData[i][j] = GridData[i / 2][j / 2];
                } else if (i % 2 == 0 && j % 2 != 0) {
                    a = GridData[i / 2][(j - 1) / 2];
                    b = GridData[i / 2][(j - 1) / 2 + 1];
                    dList = new ArrayList<>();
                    if (a != unDefData) {
                        dList.add(a);
                    }
                    if (b != unDefData) {
                        dList.add(b);
                    }

                    if (dList.isEmpty()) {
                        nGridData[i][j] = unDefData;
                    } else if (dList.size() == 1) {
                        nGridData[i][j] = dList.get(0);
                    } else {
                        nGridData[i][j] = (a + b) / 2;
                    }
                } else if (i % 2 != 0 && j % 2 == 0) {
                    a = GridData[(i - 1) / 2][j / 2];
                    b = GridData[(i - 1) / 2 + 1][j / 2];
                    dList = new ArrayList<>();
                    if (a != unDefData) {
                        dList.add(a);
                    }
                    if (b != unDefData) {
                        dList.add(b);
                    }

                    if (dList.isEmpty()) {
                        nGridData[i][j] = unDefData;
                    } else if (dList.size() == 1) {
                        nGridData[i][j] = dList.get(0);
                    } else {
                        nGridData[i][j] = (a + b) / 2;
                    }
                } else {
                    a = GridData[(i - 1) / 2][(j - 1) / 2];
                    b = GridData[(i - 1) / 2][(j - 1) / 2 + 1];
                    c = GridData[(i - 1) / 2 + 1][(j - 1) / 2 + 1];
                    d = GridData[(i - 1) / 2 + 1][(j - 1) / 2];
                    dList = new ArrayList<>();
                    if (a != unDefData) {
                        dList.add(a);
                    }
                    if (b != unDefData) {
                        dList.add(b);
                    }
                    if (c != unDefData) {
                        dList.add(c);
                    }
                    if (d != unDefData) {
                        dList.add(d);
                    }

                    if (dList.isEmpty()) {
                        nGridData[i][j] = unDefData;
                    } else if (dList.size() == 1) {
                        nGridData[i][j] = dList.get(0);
                    } else {
                        double aSum = 0;
                        for (double dd : dList) {
                            aSum += dd;
                        }
                        nGridData[i][j] = aSum / dList.size();
                    }
                }
            }
        }

        return nGridData;
    }

    /**
     * Interpolate grid data
     *
     * @return Result grid data
     */
    public GridData interpolate_old() {
        int nxNum = this.xArray.length * 2 - 1;
        int nyNum = this.yArray.length * 2 - 1;
        double[] newX = new double[nxNum];
        double[] newY = new double[nyNum];
        int i;

        for (i = 0; i < nxNum; i++) {
            if (i % 2 == 0) {
                newX[i] = this.xArray[i / 2];
            } else {
                newX[i] = (this.xArray[(i - 1) / 2] + this.xArray[(i - 1) / 2 + 1]) / 2;
            }
        }
        for (i = 0; i < nyNum; i++) {
            if (i % 2 == 0) {
                newY[i] = this.yArray[i / 2];
            } else {
                newY[i] = (this.yArray[(i - 1) / 2] + this.yArray[(i - 1) / 2 + 1]) / 2;
            }
        }

        return this.resample_Bilinear(newX, newY);
    }

    /**
     * Project grid data
     *
     * @param toProj To projection
     * @return Projected grid data
     */
    public GridData project(ProjectionInfo toProj) {
        if (this.projInfo == null) {
            return null;
        }

        return project(this.projInfo, toProj, ResampleMethods.NearestNeighbor);
    }

    /**
     * Project grid data
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @return Porjected grid data
     */
    public GridData project(ProjectionInfo fromProj, ProjectionInfo toProj) {
        return project(fromProj, toProj, ResampleMethods.NearestNeighbor);
    }

    /**
     * Project grid data
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param resampleMethod Interpolation method
     * @return Porjected grid data
     */
    public GridData project(ProjectionInfo fromProj, ProjectionInfo toProj, ResampleMethods resampleMethod) {
        Extent aExtent;
        int xnum = this.getXNum();
        int ynum = this.getYNum();
        if (this.isGlobal() || this.xArray[xnum - 1] - this.xArray[0] == 360) {
            aExtent = ProjectionUtil.getProjectionGlobalExtent(toProj);
        } else {
            aExtent = ProjectionUtil.getProjectionExtent(fromProj, toProj, this.xArray, this.yArray);
        }

        double xDelt = (aExtent.maxX - aExtent.minX) / (xnum - 1);
        double yDelt = (aExtent.maxY - aExtent.minY) / (ynum - 1);
        double[] newX = new double[xnum];
        double[] newY = new double[ynum];
        for (int i = 0; i < newX.length; i++) {
            newX[i] = aExtent.minX + i * xDelt;
        }

        for (int i = 0; i < newY.length; i++) {
            newY[i] = aExtent.minY + i * yDelt;
        }

        GridData pGridData = project(fromProj, toProj, newX, newY, resampleMethod);

        return pGridData;
    }

    /**
     * Project grid data
     *
     * @param toProj To projection info
     * @param newX New xArray coordinates
     * @param newY New yArray coordinates
     * @return Projected grid data
     */
    public GridData project(ProjectionInfo toProj, double[] newX, double[] newY) {
        if (this.projInfo == null) {
            return null;
        }

        return project_Neighbor(this.projInfo, toProj, newX, newY);
    }

    /**
     * Project grid data
     *
     * @param toProj To projection info
     * @param newX New xArray coordinates
     * @param newY New yArray coordinates
     * @param resampleMethod Interpolation method
     * @return Projected grid data
     */
    public GridData project(ProjectionInfo toProj, double[] newX, double[] newY,
            ResampleMethods resampleMethod) {
        if (this.projInfo == null) {
            return null;
        }

        return project(this.projInfo, toProj, newX, newY, resampleMethod);
    }

    /**
     * Project grid data
     *
     * @param fromProj From projection info
     * @param toProj To projection info
     * @param newX New xArray coordinates
     * @param newY New yArray coordinates
     * @param resampleMethod Interpolation method
     * @return Projected grid data
     */
    public GridData project(ProjectionInfo fromProj, ProjectionInfo toProj, double[] newX, double[] newY,
            ResampleMethods resampleMethod) {
        switch (resampleMethod) {
            case Bilinear:
                return project_Bilinear(fromProj, toProj, newX, newY);
            case NearestNeighbor:
                return project_Neighbor(fromProj, toProj, newX, newY);
            default:
                return project_Bilinear(fromProj, toProj, newX, newY);
        }
    }

    /**
     * Project grid data to station data
     *
     * @param fromProj From projection info
     * @param toProj To projection info
     * @param stData Station data
     * @param resampleMethod Interpolation method
     * @return Projected station data
     */
    public StationData project(ProjectionInfo fromProj, ProjectionInfo toProj, StationData stData,
            ResampleMethods resampleMethod) {
        switch (resampleMethod) {
            case Bilinear:
                return project_Bilinear(fromProj, toProj, stData);
            default:
                return project_Bilinear(fromProj, toProj, stData);
        }
    }

    private GridData project_Neighbor(ProjectionInfo fromProj, ProjectionInfo toProj, double[] newX, double[] newY) {
        double[][] newdata = new double[newY.length][newX.length];
        int i, j, xIdx, yIdx;
        double x, y;

        double[][] points = new double[1][];
        for (i = 0; i < newY.length; i++) {
            for (j = 0; j < newX.length; j++) {
                points[0] = new double[]{newX[j], newY[i]};
                try {
                    Reproject.reprojectPoints(points, toProj, fromProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];

                    if (x < xArray[0] || x > xArray[xArray.length - 1]) {
                        newdata[i][j] = missingValue;
                    } else if (y < yArray[0] || y > yArray[yArray.length - 1]) {
                        newdata[i][j] = missingValue;
                    } else {
                        xIdx = (int) ((x - xArray[0]) / getXDelt());
                        yIdx = (int) ((y - yArray[0]) / getYDelt());
                        newdata[i][j] = data[yIdx][xIdx];
                    }
                } catch (Exception e) {
                    newdata[i][j] = missingValue;
                    j++;
                }
            }
        }

        GridData gData = new GridData(this);
        gData.data = newdata;
        gData.xArray = newX;
        gData.yArray = newY;

        return gData;
    }

    private GridData project_Bilinear(ProjectionInfo fromProj, ProjectionInfo toProj, double[] newX, double[] newY) {
        //PointD[][] pos = new PointD[newY.length][newX.length];
        double[][] newdata = new double[newY.length][newX.length];
        int i, j;
        double x, y;

        double[][] points = new double[1][];
        for (i = 0; i < newY.length; i++) {
            for (j = 0; j < newX.length; j++) {
                points[0] = new double[]{newX[j], newY[i]};
                try {
                    Reproject.reprojectPoints(points, toProj, fromProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];

                    if (x < xArray[0] || x > xArray[xArray.length - 1]) {
                        newdata[i][j] = missingValue;
                    } else if (y < yArray[0] || y > yArray[yArray.length - 1]) {
                        newdata[i][j] = missingValue;
                    } else {
                        newdata[i][j] = this.toStation(x, y);
                    }
                } catch (Exception e) {
                    newdata[i][j] = missingValue;
                    j++;
                }
            }
        }

        GridData gData = new GridData(this);
        gData.data = newdata;
        gData.xArray = newX;
        gData.yArray = newY;

        return gData;
    }

    private StationData project_Bilinear(ProjectionInfo fromProj, ProjectionInfo toProj, StationData stData) {
        int i;
        double x, y;
        StationData nsData = new StationData(stData);
        nsData.missingValue = missingValue;

        double[][] points = new double[1][];
        for (i = 0; i < stData.getStNum(); i++) {
            points[0] = new double[]{stData.getX(i), stData.getY(i)};
            try {
                Reproject.reprojectPoints(points, toProj, fromProj, 0, 1);
                x = points[0][0];
                y = points[0][1];

                if (x < xArray[0] || x > xArray[xArray.length - 1]) {
                    nsData.setValue(i, missingValue);
                } else if (y < yArray[0] || y > yArray[yArray.length - 1]) {
                    nsData.setValue(i, missingValue);
                } else {
                    nsData.setValue(i, this.toStation(x, y));
                }
            } catch (Exception e) {
                nsData.setValue(i, missingValue);
                i++;
            }
        }

        return nsData;
    }

    /**
     * Aggregate the grid data to coarser resolution
     *
     * @param toGridData To grid data
     * @param isAverage If is average
     */
    public void aggregate(GridData toGridData, boolean isAverage) {
        int xnum = this.getXNum();
        int ynum = this.getYNum();
        int toXnum = toGridData.getXNum();
        int toYnum = toGridData.getYNum();
        int i, j, xIdx, yIdx;
        int[][] nums = new int[toYnum][toXnum];
        double x, y, y0;
        toGridData.setValue(0.0);
        if (this.projInfo.equals(toGridData.projInfo)) {
            for (i = 0; i < ynum; i++) {
                y = yArray[i];
                for (j = 0; j < xnum; j++) {
                    x = xArray[j];
                    int[] ij = toGridData.getIJIndex(x, y);
                    xIdx = ij[0];
                    yIdx = ij[1];
                    if (xIdx >= 0 && yIdx >= 0) {
                        toGridData.data[yIdx][xIdx] += this.data[i][j];
                        nums[yIdx][xIdx] += 1;
                    }
                }
            }
        } else {
            double[][] points = new double[1][];
            for (i = 0; i < ynum; i++) {
                y0 = yArray[i];
                for (j = 0; j < xnum; j++) {
                    x = xArray[j];
                    y = y0;
                    points[0] = new double[]{x, y};
                    try {
                        Reproject.reprojectPoints(points, this.projInfo, toGridData.projInfo, 0, 1);
                        x = points[0][0];
                        y = points[0][1];
                        if (java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y)) {
                            continue;
                        }

                        int[] ij = toGridData.getIJIndex(x, y);
                        xIdx = ij[0];
                        yIdx = ij[1];
                        if (xIdx >= 0 && yIdx >= 0) {
                            toGridData.data[yIdx][xIdx] += this.data[i][j];
                            nums[yIdx][xIdx] += 1;
                        }
                    } catch (Exception e) {
                        j++;
                        continue;
                    }
                }
            }
        }

        //Average
        if (isAverage) {
            for (i = 0; i < toYnum; i++) {
                for (j = 0; j < toXnum; j++) {
                    if (nums[i][j] == 0) {
                        toGridData.data[i][j] = toGridData.missingValue;
                    } else {
                        toGridData.data[i][j] = toGridData.data[i][j] / nums[i][j];
                    }
                }
            }
        }
    }

    /**
     * Un stag grid data through x dimension
     *
     * @return Un stagged grid data
     */
    public GridData unStagger_X() {
        int xn = this.getXNum();
        int yn = this.getYNum();
        double dx = this.getXDelt();
        int xn_us = xn - 1;
        int i, j;
        GridData usData = new GridData(this);
        usData.xArray = new double[xn_us];
        usData.data = new double[yn][xn_us];
        for (i = 0; i < yn; i++) {
            for (j = 0; j < xn_us; j++) {
                if (i == 0) {
                    usData.xArray[j] = xArray[j] + dx;
                }
                usData.data[i][j] = (this.data[i][j] + this.data[i][j + 1]) * 0.5;
            }
        }

        return usData;
    }

    /**
     * Un stag grid data through y dimension
     *
     * @return Un stagged grid data
     */
    public GridData unStagger_Y() {
        int xn = this.getXNum();
        int yn = this.getYNum();
        double dy = this.getYDelt();
        int yn_us = yn - 1;
        int i, j;
        GridData usData = new GridData(this);
        usData.yArray = new double[yn_us];
        usData.data = new double[yn_us][xn];
        for (i = 0; i < yn_us; i++) {
            for (j = 0; j < xn; j++) {
                if (j == 0) {
                    usData.yArray[i] = yArray[i] + dy;
                }
                usData.data[i][j] = (this.data[i][j] + this.data[i + 1][j]) * 0.5;
            }
        }

        return usData;
    }

    /**
     * Clone
     *
     * @return Grid data object
     */
    @Override
    public Object clone() {
        GridData newGriddata = new GridData(this);
        newGriddata.data = data.clone();

        return newGriddata;
    }

    /**
     * yArray reverse to the grid data
     */
    public void yReverse() {
        int xNum = getXNum();
        int yNum = getYNum();
        double[][] newdata = new double[yNum][xNum];
        for (int i = 0; i < yNum; i++) {
            System.arraycopy(data[i], 0, newdata[yNum - i - 1], 0, xNum);
        }
        data = newdata;
    }
    
    /**
     * xArray reverse to the grid data
     */
    public void xReverse() {
        int xNum = getXNum();
        int yNum = getYNum();
        double[][] newdata = new double[yNum][xNum];
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++){
                newdata[i][j] = data[i][xNum - j - 1];
            }
        }
        data = newdata;
    }

    // </editor-fold>
    // </editor-fold>
    // <editor-fold desc="Nested classes">
    /**
     * GridData.Integer class
     */
    public static class Byte extends GridData {

        private byte[][] data;
        private int missingValue;

        /**
         * Constructor
         *
         * @param yNum Y number
         * @param xNum X number
         */
        public Byte(int yNum, int xNum) {
            data = new byte[yNum][xNum];
            missingValue = -9999;
        }

        /**
         * Get missing value
         *
         * @return Missing value
         */
        public int getMissingValue() {
            return this.missingValue;
        }

        /**
         * Set missing value
         *
         * @param value Missing value
         */
        public void setMissingValue(int value) {
            this.missingValue = value;
        }

        /**
         * Get value
         *
         * @param i I index
         * @param j J index
         * @return Value
         */
        @Override
        public Number getValue(int i, int j) {
            return DataConvert.byte2Int(data[i][j]);
        }

        /**
         * Set value
         *
         * @param i I index
         * @param j J index
         * @param value Value
         */
        public void setValue(int i, int j, byte value) {
            data[i][j] = value;
        }

        /**
         * Get double value
         *
         * @param i I index
         * @param j J index
         * @return Double value
         */
        @Override
        public double getDoubleValue(int i, int j) {
            return getValue(i, j).doubleValue();
        }

        /**
         * Get maximum and minimum values
         *
         * @param maxmin Max/Min array
         * @return If has undefine data
         */
        @Override
        public boolean getMaxMinValue(double[] maxmin) {
            double max = 0;
            double min = 0;
            int vdNum = 0;
            boolean hasUndef = false;
            int v;
            for (int i = 0; i < getYNum(); i++) {
                for (int j = 0; j < getXNum(); j++) {
                    v = getValue(i, j).intValue();
                    if (v == missingValue) {
                        hasUndef = true;
                        continue;
                    }

                    if (vdNum == 0) {
                        min = v;
                        max = min;
                    } else {
                        if (min > v) {
                            min = v;
                        }
                        if (max < v) {
                            max = v;
                        }
                    }
                    vdNum += 1;
                }
            }

            maxmin[0] = max;
            maxmin[1] = min;
            return hasUndef;
        }

        /**
         * Convert to GridArray object
         *
         * @return GridArray object
         */
        @Override
        public GridArray toGridArray() {
            Array a = Array.factory(DataType.DOUBLE, new int[]{this.getYNum(), this.getXNum()});
            int idx = 0;
            for (int i = 0; i < this.getYNum(); i++) {
                for (int j = 0; j < this.getXNum(); j++) {
                    a.setDouble(idx, this.data[i][j]);
                    idx += 1;
                }
            }

            GridArray r = new GridArray();
            r.setData(a);
            r.xArray = this.xArray;
            r.yArray = this.yArray;
            r.projInfo = this.projInfo;
            r.missingValue = this.missingValue;
            return r;
        }
    }

    /**
     * GridData.Integer class
     */
    public static class Integer extends GridData {

        private final int[][] data;
        private int missingValue;

        /**
         * Constructor
         *
         * @param yNum Y number
         * @param xNum X number
         */
        public Integer(int yNum, int xNum) {
            data = new int[yNum][xNum];
            missingValue = -9999;
        }

        /**
         * Get missing value
         *
         * @return Missing value
         */
        public int getMissingValue() {
            return this.missingValue;
        }

        /**
         * Set missing value
         *
         * @param value Missing value
         */
        public void setMissingValue(int value) {
            this.missingValue = value;
        }

        /**
         * Get value
         *
         * @param i I index
         * @param j J index
         * @return Value
         */
        @Override
        public Number getValue(int i, int j) {
            return data[i][j];
        }

        /**
         * Set value
         *
         * @param i I index
         * @param j J index
         * @param value Value
         */
        public void setValue(int i, int j, int value) {
            data[i][j] = value;
        }

        /**
         * Get double value
         *
         * @param i I index
         * @param j J index
         * @return Double value
         */
        @Override
        public double getDoubleValue(int i, int j) {
            return data[i][j];
        }

        /**
         * Get maximum and minimum values
         *
         * @param maxmin Max/Min array
         * @return If has undefine data
         */
        @Override
        public boolean getMaxMinValue(double[] maxmin) {
            double max = 0;
            double min = 0;
            int vdNum = 0;
            boolean hasUndef = false;
            for (int i = 0; i < getYNum(); i++) {
                for (int j = 0; j < getXNum(); j++) {
                    if (data[i][j] == missingValue) {
                        hasUndef = true;
                        continue;
                    }

                    if (vdNum == 0) {
                        min = data[i][j];
                        max = min;
                    } else {
                        if (min > data[i][j]) {
                            min = data[i][j];
                        }
                        if (max < data[i][j]) {
                            max = data[i][j];
                        }
                    }
                    vdNum += 1;
                }
            }

            maxmin[0] = max;
            maxmin[1] = min;
            return hasUndef;
        }
    }

    /**
     * GridData.Double class
     */
    public static class Double extends GridData {

        private double[][] data;
        private double missingValue;

        /**
         * Constructor
         *
         * @param yNum Y number
         * @param xNum X number
         */
        public Double(int yNum, int xNum) {
            data = new double[yNum][xNum];
            missingValue = -9999.0;
        }

        /**
         * Get missing value
         *
         * @return Missing value
         */
        public double getMissingValue() {
            return this.missingValue;
        }

        /**
         * Set missing value
         *
         * @param value Missing value
         */
        public void setMissingValue(double value) {
            this.missingValue = value;
        }

        /**
         * Get value
         *
         * @param i I index
         * @param j J index
         * @return Value
         */
        @Override
        public Number getValue(int i, int j) {
            return data[i][j];
        }

        /**
         * Set value
         *
         * @param i I index
         * @param j J index
         * @param value Value
         */
        public void setValue(int i, int j, double value) {
            data[i][j] = value;
        }

        /**
         * Get double value
         *
         * @param i I index
         * @param j J index
         * @return Double value
         */
        @Override
        public double getDoubleValue(int i, int j) {
            return data[i][j];
        }
    }

    /**
     * GridData.Integer class
     */
    public static class Float extends GridData {

        private float[][] data;
        private float missingValue;

        /**
         * Constructor
         *
         * @param yNum Y number
         * @param xNum X number
         */
        public Float(int yNum, int xNum) {
            data = new float[yNum][xNum];
            missingValue = -9999;
        }

        /**
         * Get missing value
         *
         * @return Missing value
         */
        public float getMissingValue() {
            return this.missingValue;
        }

        /**
         * Set missing value
         *
         * @param value Missing value
         */
        public void setMissingValue(float value) {
            this.missingValue = value;
        }

        /**
         * Get value
         *
         * @param i I index
         * @param j J index
         * @return Value
         */
        @Override
        public Number getValue(int i, int j) {
            return data[i][j];
        }

        /**
         * Set value
         *
         * @param i I index
         * @param j J index
         * @param value Value
         */
        public void setValue(int i, int j, float value) {
            data[i][j] = value;
        }

        /**
         * Get double value
         *
         * @param i I index
         * @param j J index
         * @return Double value
         */
        @Override
        public double getDoubleValue(int i, int j) {
            return data[i][j];
        }
    }

    /**
     * Convert to GridArray object
     *
     * @return GridArray object
     */
    public GridArray toGridArray() {
        Array a = Array.factory(DataType.DOUBLE, new int[]{this.getYNum(), this.getXNum()});
        int idx = 0;
        for (int i = 0; i < this.getYNum(); i++) {
            for (int j = 0; j < this.getXNum(); j++) {
                a.setDouble(idx, this.getDoubleValue(i, j));
                idx += 1;
            }
        }

        GridArray r = new GridArray();
        r.setData(a);
        r.xArray = this.xArray;
        r.yArray = this.yArray;
        r.projInfo = this.projInfo;
        r.missingValue = this.missingValue;
        return r;
    }
    // </editor-fold>
}
