/* Copyright 2015 Yaqiang Wang,
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

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.GridDataSetting;
import org.meteoinfo.geoprocess.analysis.ResampleMethods;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.ProjectionUtil;
import org.meteoinfo.projection.Reproject;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author yaqiang
 */
public class GridArray {

    // <editor-fold desc="Variables">
    /**
     * Grid data
     */
    public Array data;
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
    public GridArray() {
        missingValue = -9999;
    }

    /**
     * Constructor
     *
     * @param aGridData The grid data
     */
    public GridArray(GridArray aGridData) {
        projInfo = aGridData.projInfo;
        xArray = aGridData.xArray.clone();
        yArray = aGridData.yArray.clone();
        missingValue = aGridData.missingValue;
        data = Array.factory(aGridData.data.getDataType(), aGridData.data.getShape());
        Array.arraycopy(aGridData.data, 0, data, 0, (int) aGridData.data.getSize());
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
    public GridArray(double xStart, double xDelt, int xNum, double yStart, double yDelt, int yNum) {
        xArray = new double[xNum];
        yArray = new double[yNum];
        for (int i = 0; i < xNum; i++) {
            xArray[i] = BigDecimalUtil.add(xStart, BigDecimalUtil.mul(xDelt, i));
        }
        for (int i = 0; i < yNum; i++) {
            yArray[i] = BigDecimalUtil.add(yStart, BigDecimalUtil.mul(yDelt, i));
        }

        missingValue = -9999;
        int[] shape = new int[]{yNum, xNum};
        data = Array.factory(DataType.DOUBLE, shape);
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
    public GridArray(Array array, List<Number> xdata, List<Number> ydata, double missingValue, ProjectionInfo projInfo) {
        int yn = ydata.size();
        int xn = xdata.size();
        this.data = array;
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
     * @param missingValue Missing value
     */
    public GridArray(Array array, Array xdata, Array ydata, Number missingValue) {
        int yn = (int) ydata.getSize();
        int xn = (int) xdata.getSize();
        this.data = array;

        this.xArray = new double[xn];
        this.yArray = new double[yn];
        for (int i = 0; i < xn; i++) {
            this.xArray[i] = xdata.getDouble(i);
        }
        for (int i = 0; i < yn; i++) {
            this.yArray[i] = ydata.getDouble(i);
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
        return BigDecimalUtil.sub(xArray[1], xArray[0]);
    }

    /**
     * Get yArray delt
     *
     * @return yArray delt
     */
    public double getYDelt() {
        return BigDecimalUtil.sub(yArray[1], yArray[0]);
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
        return (Number) data.getObject(i * this.getXNum() + j);
    }

    /**
     * Get double value
     *
     * @param i I index
     * @param j J index
     * @return Double value
     */
    public double getDoubleValue(int i, int j) {
        return data.getDouble(i * this.getXNum() + j);
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Convert data">    
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
        for (int i = 0; i < data.getSize(); i++) {
            if (java.lang.Double.isNaN(data.getDouble(i)) || MIMath.doubleEquals(data.getDouble(i), missingValue)) {
                hasUndef = true;
                continue;
            }

            if (vdNum == 0) {
                min = data.getDouble(i);
                max = min;
            } else {
                if (min > data.getDouble(i)) {
                    min = data.getDouble(i);
                }
                if (max < data.getDouble(i)) {
                    max = data.getDouble(i);
                }
            }
            vdNum += 1;
        }

        maxmin[0] = max;
        maxmin[1] = min;
        return hasUndef;
    }

    /**
     * Minimum
     *
     * @return Minimum value
     */
    public double min() {
        return ArrayMath.getMinimum(data, missingValue);
    }

    /**
     * Maximum
     *
     * @return Maximum value
     */
    public double max() {
        return ArrayMath.getMaximum(data, missingValue);
    }

    /**
     * Get if has NaN value
     *
     * @return Boolean
     */
    public boolean hasNaN() {
        boolean hasNaN = false;
        for (int i = 0; i < data.getSize(); i++) {
            if (java.lang.Double.isNaN(data.getDouble(i)) || MIMath.doubleEquals(data.getDouble(i), missingValue)) {
                hasNaN = true;
                break;
            }
        }
        return hasNaN;
    }

    /**
     * Project grid data
     *
     * @param toProj To projection
     * @return Projected grid data
     * @throws ucar.ma2.InvalidRangeException
     */
    public GridArray project(ProjectionInfo toProj) throws InvalidRangeException {
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
     * @throws ucar.ma2.InvalidRangeException
     */
    public GridArray project(ProjectionInfo fromProj, ProjectionInfo toProj) throws InvalidRangeException {
        //return project(fromProj, toProj, ResampleMethods.NearestNeighbor);
        List<Number> xx = new ArrayList<>();
        List<Number> yy = new ArrayList<>();
        for (Double x : this.xArray) {
            xx.add(x);
        }
        for (Double y : this.yArray) {
            yy.add(y);
        }
        Object[] r = ArrayUtil.reproject(data, xx, yy, fromProj, toProj);
        GridArray rdata = new GridArray((Array) r[0], (Array) r[1], (Array) r[2], missingValue);
        rdata.projInfo = toProj;

        return rdata;
    }

    /**
     * Project grid data
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param resampleMethod Interpolation method
     * @return Porjected grid data
     * @throws ucar.ma2.InvalidRangeException
     */
    public GridArray project(ProjectionInfo fromProj, ProjectionInfo toProj, ResampleMethods resampleMethod) throws InvalidRangeException {
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
        List<Number> x = new ArrayList<>();
        List<Number> y = new ArrayList<>();
        Array rx = Array.factory(DataType.DOUBLE, new int[]{xnum});
        Array ry = Array.factory(DataType.DOUBLE, new int[]{ynum});
        for (int i = 0; i < xnum; i++) {
            x.add(this.xArray[i]);
            rx.setDouble(i, aExtent.minX + i * xDelt);
        }

        for (int i = 0; i < ynum; i++) {
            y.add(this.yArray[i]);
            ry.setDouble(i, aExtent.minY + i * yDelt);
        }
        Array[] rxy = ArrayUtil.meshgrid(rx, ry);
        Array rrx = rxy[0];
        Array rry = rxy[1];

        Array r = ArrayUtil.reproject(data, x, y, rrx, rry, fromProj, toProj, missingValue, resampleMethod);
        GridArray rdata = new GridArray(r, rx, ry, missingValue);
        rdata.projInfo = toProj;

        return rdata;
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
            Object value;
            String aLine = "";
            for (int i = 0; i < this.getYNum(); i++) {
                for (int j = 0; j < this.getXNum(); j++) {
                    value = data.getObject(i * this.getXNum() + j);
                    if (j == 0) {
                        aLine = value.toString();
                    } else {
                        aLine = aLine + " " + value.toString();
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
        if (this.getXDelt() != this.getYDelt()){
            JOptionPane.showMessageDialog(null, "X cell size is not equal y cell size!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
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
            //sw.newLine();
            String value;
            String aLine = "";
            int xn = this.getXNum();
            int yn = this.getYNum();
            for (int i = 0; i < yn; i++) {
                for (int j = 0; j < xn; j++) {
                    value = data.getObject((yn - i - 1) * xn + j).toString();
                    if (j == 0) {
                        aLine = value;
                    } else {
                        aLine = aLine + " " + value;
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
                    outs.writeFloat((this.data.getFloat((yn - i - 1) * xn + j)));
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
     * Save as MICAPS 4 data file
     * @param aFile File path
     * @param description Description
     * @param aTime Data time
     * @param hours Hours
     * @param level Levels
     * @param smooth Smooth coefficient
     * @param boldValue Bold value
     * @param floatFormat Float format
     * @throws IOException 
     */
    public void saveAsMICAPS4File(String aFile, String description, Date aTime, int hours, int level,
            float smooth, float boldValue, String floatFormat) throws IOException {
        //Get contour values
        double[] CValues;
        double undef = 9999;
        double[] maxmin = new double[2];
        this.getMaxMinValue(maxmin);
        double min = maxmin[1];
        double max = maxmin[0];
        CValues = LegendManage.createContourValues(min, max);
        double cDelt = 0;
        if (CValues.length > 1)
            cDelt = CValues[1] - CValues[0];
        if (floatFormat == null) {
            int dNum = MIMath.getDecimalNum(min);
            floatFormat = "%." + String.valueOf(dNum) + "f";
        }

        //Write file
        SimpleDateFormat format = new SimpleDateFormat("yy MM dd HH");
        BufferedWriter sw = new BufferedWriter(new FileWriter(new File(aFile)));
        sw.write("diamond 4 " + description);
        sw.newLine();
        String aLine = format.format(aTime) + " " + String.valueOf(hours) + " " + String.valueOf(level);
        sw.write(aLine);
        sw.newLine();
        aLine = String.valueOf(this.getXDelt()) + " " + String.valueOf(this.getYDelt()) + " "
                + String.valueOf(this.xArray[0]) + " " + String.valueOf(this.xArray[this.getXNum() - 1])
                + " " + String.valueOf(this.yArray[0]) + " " + String.valueOf(this.yArray[this.getYNum() - 1]);
        sw.write(aLine);
        sw.newLine();
        aLine = String.valueOf(this.getXNum()) + " " + String.valueOf(this.getYNum()) + " "
                + String.valueOf(cDelt) + " " + String.valueOf(CValues[0]) + " "
                + String.valueOf(CValues[CValues.length - 1]) + " " + String.valueOf(smooth) + " "
                + String.valueOf(boldValue);
        sw.write(aLine);
        sw.newLine();
        double value;
        for (int i = 0; i < this.getYNum(); i++) {
            for (int j = 0; j < this.getXNum(); j++) {
                value = this.data.getDouble(i * this.getXNum() + j);
                if (Double.isNaN(value)) {
                    value = undef;
                } else if (MIMath.doubleEquals(value, this.missingValue)) {
                    value = undef;
                }

                if (j == 0) {
                    aLine = String.format(floatFormat, value);
                } else {
                    aLine = aLine + " " + String.format(floatFormat, value);
                }
            }
            sw.write(aLine);
            sw.newLine();
        }

        sw.close();
    }
    
    /**
     * Save as MICAPS 4 data file
     * @param aFile File path
     * @param description Description
     * @param aTime Data time
     * @param hours Hours
     * @param level Levels
     * @param smooth Smooth coefficient
     * @param boldValue Bold value
     * @param floatFormat Float format
     * @param projInfo Projection info
     * @throws IOException 
     */
    public void saveAsMICAPS4File(String aFile, String description, Date aTime, int hours, int level,
            float smooth, float boldValue, String floatFormat, ProjectionInfo projInfo) throws IOException {
        //Get contour values
        double[] CValues;
        double undef = 9999;

        double[] maxmin = new double[2];
        this.getMaxMinValue(maxmin);
        double min = maxmin[1];
        double max = maxmin[0];
        CValues = LegendManage.createContourValues(min, max);
        double cDelt = CValues[1] - CValues[0];
        if (floatFormat == null) {
            int dNum = MIMath.getDecimalNum(min);
            floatFormat = "%1$." + String.valueOf(dNum) + "f";
        }

        //Write file
        SimpleDateFormat format = new SimpleDateFormat("yy MM dd HH");
        BufferedWriter sw = new BufferedWriter(new FileWriter(new File(aFile)));
        sw.write("diamond 4 " + description);
        String aLine = format.format(aTime) + " " + String.valueOf(hours) + " " + String.valueOf(level);
        sw.write(aLine);
        sw.newLine();

        double eCValue = CValues[CValues.length - 1];
        double xdelt = this.getXDelt();
        double ydelt = this.getYDelt();
        double sLon = this.xArray[0];
        double eLon = this.xArray[this.getXNum() - 1];
        double sLat = this.yArray[0];
        double eLat = this.yArray[this.getYNum() - 1];
        if (!projInfo.isLonLat()) {
            switch (projInfo.getProjectionName()) {
                case Lambert_Conformal_Conic:
                    eCValue = -1;
                    break;
                case Mercator:
                    eCValue = -2;
                    break;
                case North_Polar_Stereographic_Azimuthal:
                    eCValue = -3;
                    break;
            }
            ProjectionInfo toProj = KnownCoordinateSystems.geographic.world.WGS1984;
            double[][] points = new double[2][];
            points[0] = new double[]{sLon, sLat};
            points[1] = new double[]{eLon, eLat};
            Reproject.reprojectPoints(points, projInfo, toProj, 0, 2);
            xdelt = points[1][0];
            ydelt = points[0][1];
            sLon = points[0][0];
            sLat = points[0][1];
            eLon = points[1][0];
            eLat = points[1][1];
        }
        aLine = String.valueOf(xdelt) + " " + String.valueOf(ydelt) + " " + String.valueOf(sLon)
                + " " + String.valueOf(eLon) + " " + String.valueOf(sLat) + " " + String.valueOf(eLat);
        sw.write(aLine);
        sw.newLine();
        aLine = String.valueOf(this.getXNum()) + " " + String.valueOf(this.getYNum()) + " "
                + String.valueOf(cDelt) + " " + String.valueOf(CValues[0]) + " "
                + String.valueOf(eCValue) + " " + String.valueOf(smooth) + " "
                + String.valueOf(boldValue);
        sw.write(aLine);
        sw.newLine();
        double value;
        for (int i = 0; i < this.getYNum(); i++) {
            for (int j = 0; j < this.getXNum(); j++) {
                value = this.data.getDouble(i * this.getXNum() + j);
                if (Double.isNaN(value)) {
                    value = undef;
                } else if (MIMath.doubleEquals(value, this.missingValue)) {
                    value = undef;
                }

                if (j == 0) {
                    aLine = String.format(floatFormat, value);
                } else {
                    aLine = aLine + " " + String.format(floatFormat, value);
                }
            }
            sw.write(aLine);
            sw.newLine();
        }

        sw.close();
    }

    /**
     * Clone
     *
     * @return Grid data object
     */
    @Override
    public Object clone() {
        GridArray newGriddata = new GridArray(this);

        return newGriddata;
    }
    // </editor-fold>
    // </editor-fold>
}
