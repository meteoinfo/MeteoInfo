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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.meteoinfo.data.meteodata.GridDataSetting;
import org.meteoinfo.geoprocess.analysis.InterpolationSetting;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.ShapeTypes;
import ucar.ma2.Array;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class StationData {
    // <editor-fold desc="Variables">
    /// <summary>
    /// station data: longitude, latitude, value
    /// </summary>

    public double[][] data;
    /// <summary>
    /// Station identifer list
    /// </summary>
    public List<String> stations;
    /// <summary>
    /// Data extent
    /// </summary>
    public Extent dataExtent;
    /// <summary>
    /// Undef data
    /// </summary>
    public double missingValue;
    /**
     * Projection information
     */
    public ProjectionInfo projInfo = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public StationData() {
        data = new double[0][3];
        stations = new ArrayList<>();
        dataExtent = new Extent();
        missingValue = -9999;
    }

    /**
     * Constructor
     *
     * @param a Array data
     * @param x Array x
     * @param y Array y
     * @param missingv Missing value
     */
    public StationData(Array a, Array x, Array y, Number missingv) {
        int n = (int) a.getSize();
        this.missingValue = missingv.doubleValue();
        stations = new ArrayList<>();
        dataExtent = new Extent();
        data = new double[n][3];
        for (int i = 0; i < n; i++) {
            stations.add("s_" + String.valueOf(i + 1));
            data[i][0] = x.getDouble(i);
            data[i][1] = y.getDouble(i);
            data[i][2] = a.getDouble(i);
            if (Double.isNaN(data[i][2]))
                data[i][2] = missingv.doubleValue();
            //this.addData("s_" + String.valueOf(i + 1), x.getDouble(i), y.getDouble(i), a.getDouble(i));
        }
    }

    /**
     * Constructor
     *
     * @param aStData Station data
     */
    public StationData(StationData aStData) {
        projInfo = aStData.projInfo;
        stations = aStData.stations;
        dataExtent = aStData.dataExtent;
        missingValue = aStData.missingValue;
        data = new double[aStData.data.length][aStData.data[0].length];
        for (int i = 0; i < aStData.getStNum(); i++) {
            data[i][0] = aStData.data[i][0];
            data[i][1] = aStData.data[i][1];
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get station number
     *
     * @return Station number
     */
    public int getStNum() {
        return data.length;
    }

    /**
     * Get X coordinates array
     *
     * @return X array
     */
    public double[] getX() {
        double[] x = new double[getStNum()];
        for (int i = 0; i < getStNum(); i++) {
            x[i] = data[i][0];
        }

        return x;
    }
    
    /**
     * Get X coordinates array
     *
     * @return X array
     */
    public List<Double> getXList() {
        List<Double> x = new ArrayList<>();
        for (int i = 0; i < getStNum(); i++) {
            x.add(data[i][0]);
        }

        return x;
    }

    /**
     * Get Y coordinates array
     *
     * @return Y array
     */
    public double[] getY() {
        double[] y = new double[getStNum()];
        for (int i = 0; i < getStNum(); i++) {
            y[i] = data[i][1];
        }

        return y;
    }
    
    /**
     * Get Y coordinates array
     *
     * @return Y array
     */
    public List<Double> getYList() {
        List<Double> y = new ArrayList<>();
        for (int i = 0; i < getStNum(); i++) {
            y.add(data[i][1]);
        }

        return y;
    }

    /**
     * Set data array
     *
     * @param value Data array
     */
    public void setData(double[][] value) {
        data = value;
        this.updateExtent();
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    // <editor-fold desc="Operator">
    /**
     * Add operator with another station data
     *
     * @param bStData Station data
     * @return Result station data
     */
    public StationData add(StationData bStData) {
        if (!MIMath.isExtentCross(this.dataExtent, bStData.dataExtent)) {
            return null;
        }

        StationData cStData = new StationData();
        cStData.projInfo = bStData.projInfo;
        String aStid;
        int stIdx;
        double x, y;
        for (int i = 0; i < stations.size(); i++) {
            aStid = stations.get(i);
            if (aStid.equals("99999")) {
                continue;
            }

            double aValue = this.getValue(i);
            if (aValue == missingValue) {
                continue;
            }

            stIdx = bStData.stations.indexOf(aStid);
            if (stIdx >= 0) {
                double bValue = bStData.getValue(stIdx);
                if (bValue == bStData.missingValue) {
                    continue;
                }

                x = this.getX(i);
                y = this.getY(i);
                cStData.addData(aStid, x, y, aValue + bValue);
            }
        }

        return cStData;
    }

    /**
     * Add operator with a double value
     *
     * @param value The value
     * @return Result station data
     */
    public StationData add(double value) {
        StationData cStData = new StationData();
        cStData.projInfo = this.projInfo;
        String aStid;
        double x, y;
        for (int i = 0; i < stations.size(); i++) {
            aStid = stations.get(i);

            double aValue = this.getValue(i);
            x = this.getX(i);
            y = this.getY(i);
            if (MIMath.doubleEquals(aValue, missingValue)) {
                cStData.addData(aStid, x, y, aValue);
            } else {
                cStData.addData(aStid, x, y, aValue + value);
            }
        }

        return cStData;
    }

    /**
     * Subtract operator with another station data
     *
     * @param bStData Station data
     * @return Result station data
     */
    public StationData sub(StationData bStData) {
        if (!MIMath.isExtentCross(this.dataExtent, bStData.dataExtent)) {
            return null;
        }

        StationData cStData = new StationData();
        String aStid;
        int stIdx;
        double x, y;
        for (int i = 0; i < stations.size(); i++) {
            aStid = stations.get(i);
            if (aStid.equals("99999")) {
                continue;
            }

            double aValue = this.getValue(i);
            if (aValue == missingValue) {
                continue;
            }

            stIdx = bStData.stations.indexOf(aStid);
            if (stIdx >= 0) {
                double bValue = bStData.getValue(stIdx);
                if (bValue == bStData.missingValue) {
                    continue;
                }

                x = this.getX(i);
                y = this.getY(i);
                cStData.addData(aStid, x, y, aValue - bValue);
            }
        }

        return cStData;
    }

    /**
     * Subtract operator with a double value
     *
     * @param value The value
     * @return Result station data
     */
    public StationData sub(double value) {
        StationData cStData = new StationData();
        String aStid;
        double x, y;
        for (int i = 0; i < stations.size(); i++) {
            aStid = stations.get(i);

            double aValue = this.getValue(i);
            x = this.getX(i);
            y = this.getY(i);
            if (MIMath.doubleEquals(aValue, missingValue)) {
                cStData.addData(aStid, x, y, aValue);
            } else {
                cStData.addData(aStid, x, y, aValue - value);
            }
        }

        return cStData;
    }

    /**
     * multiply operator with another station data
     *
     * @param bStData Station data
     * @return Result station data
     */
    public StationData mul(StationData bStData) {
        if (!MIMath.isExtentCross(this.dataExtent, bStData.dataExtent)) {
            return null;
        }

        StationData cStData = new StationData();
        String aStid;
        int stIdx;
        double x, y;
        for (int i = 0; i < stations.size(); i++) {
            aStid = stations.get(i);
            if (aStid.equals("99999")) {
                continue;
            }

            double aValue = this.getValue(i);
            if (aValue == missingValue) {
                continue;
            }

            stIdx = bStData.stations.indexOf(aStid);
            if (stIdx >= 0) {
                double bValue = bStData.getValue(stIdx);
                if (bValue == bStData.missingValue) {
                    continue;
                }

                x = this.getX(i);
                y = this.getY(i);
                cStData.addData(aStid, x, y, aValue * bValue);
            }
        }

        return cStData;
    }

    /**
     * Multiply operator with a double value
     *
     * @param value The value
     * @return Result station data
     */
    public StationData mul(double value) {
        StationData cStData = new StationData();
        String aStid;
        double x, y;
        for (int i = 0; i < stations.size(); i++) {
            aStid = stations.get(i);

            double aValue = this.getValue(i);
            x = this.getX(i);
            y = this.getY(i);
            if (MIMath.doubleEquals(aValue, missingValue)) {
                cStData.addData(aStid, x, y, aValue);
            } else {
                cStData.addData(aStid, x, y, aValue * value);
            }
        }

        return cStData;
    }

    /**
     * Divide operator with another station data
     *
     * @param bStData Station data
     * @return Result station data
     */
    public StationData div(StationData bStData) {
        if (!MIMath.isExtentCross(this.dataExtent, bStData.dataExtent)) {
            return null;
        }

        StationData cStData = new StationData();
        String aStid;
        int stIdx;
        double x, y;
        for (int i = 0; i < stations.size(); i++) {
            aStid = stations.get(i);
            if (aStid.equals("99999")) {
                continue;
            }

            double aValue = this.getValue(i);
            if (aValue == missingValue) {
                continue;
            }

            stIdx = bStData.stations.indexOf(aStid);
            if (stIdx >= 0) {
                double bValue = bStData.getValue(stIdx);
                if (bValue == bStData.missingValue) {
                    continue;
                }

                x = this.getX(i);
                y = this.getY(i);
                cStData.addData(aStid, x, y, aValue / bValue);
            }
        }

        return cStData;
    }

    /**
     * Divide operator with a double value
     *
     * @param value The value
     * @return Result station data
     */
    public StationData div(double value) {
        StationData cStData = new StationData();
        String aStid;
        double x, y;
        for (int i = 0; i < stations.size(); i++) {
            aStid = stations.get(i);

            double aValue = this.getValue(i);
            x = this.getX(i);
            y = this.getY(i);
            if (MIMath.doubleEquals(aValue, missingValue)) {
                cStData.addData(aStid, x, y, aValue);
            } else {
                cStData.addData(aStid, x, y, aValue / value);
            }
        }

        return cStData;
    }

    // </editor-fold>
    // <editor-fold desc="Functions">

    /**
     * Calculate abstract station data
     *
     * @return Result station data
     */
    public StationData abs() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.abs(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate anti-cosine station data
     *
     * @return Result station data
     */
    public StationData acos() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.acos(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate anti-sine station data
     *
     * @return Result station data
     */
    public StationData asin() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.asin(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate anti-tangent station data
     *
     * @return Result station data
     */
    public StationData atan() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.atan(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate cosine station data
     *
     * @return Result station data
     */
    public StationData cos() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.cos(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate sine station data
     *
     * @return Result station data
     */
    public StationData sin() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.sin(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate tangent station data
     *
     * @return Result station data
     */
    public StationData tan() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.tan(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate e raised specific power value of station data
     *
     * @return Result station data
     */
    public StationData exp() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.exp(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate power station data
     *
     * @param p Power value
     * @return Result station data
     */
    public StationData pow(double p) {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.pow(getValue(i), p));
            }
        }

        return stationData;
    }

    /**
     * Calculate square root station data
     *
     * @return Result station data
     */
    public StationData sqrt() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.sqrt(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate logrithm station data
     *
     * @return Result station data
     */
    public StationData log() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.log(getValue(i)));
            }
        }

        return stationData;
    }

    /**
     * Calculate base 10 logrithm station data
     *
     * @return Result station data
     */
    public StationData log10() {
        StationData stationData = new StationData(this);
        for (int i = 0; i < stationData.getStNum(); i++) {
            if (MIMath.doubleEquals(getValue(i), missingValue)) {
                stationData.setValue(i, missingValue);
            } else {
                stationData.setValue(i, Math.log10(getValue(i)));
            }
        }

        return stationData;
    }
    // </editor-fold>
    // <editor-fold desc="Data">

    /**
     * Add a data
     *
     * @param id Data identifer
     * @param x X coordinate
     * @param y Y coordinate
     * @param value Value
     */
    public void addData(String id, double x, double y, double value) {
        stations.add(id);
        int newSize = data.length + 1;
        if (data.length == 0) {
            data = new double[1][3];
        } else {
            data = DataConvert.resizeArray2D(data, newSize);
        }
        data[newSize - 1][0] = x;
        data[newSize - 1][1] = y;
        data[newSize - 1][2] = value;

        if (newSize == 1) {
            dataExtent.minX = x;
            dataExtent.maxX = x;
            dataExtent.minY = y;
            dataExtent.maxY = y;
        } else {
            if (x < dataExtent.minX) {
                dataExtent.minX = x;
            }
            if (x > dataExtent.maxX) {
                dataExtent.maxX = x;
            }
            if (y < dataExtent.minY) {
                dataExtent.minY = y;
            }
            if (y > dataExtent.maxY) {
                dataExtent.maxY = y;
            }
        }
    }

    /**
     * Get station identifer by index
     *
     * @param idx Index
     * @return Station identifer
     */
    public String getStid(int idx) {
        return stations.get(idx);
    }

    /**
     * Set station identifer by index
     *
     * @param idx Index
     * @param value Station identifer
     */
    public void setStid(int idx, String value) {
        stations.set(idx, value);
    }

    /**
     * Get x coordinate by index
     *
     * @param idx Index
     * @return X coordinate
     */
    public double getX(int idx) {
        return data[idx][0];
    }

    /**
     * Get y coordinate by index
     *
     * @param idx Index
     * @return Y coordinate
     */
    public double getY(int idx) {
        return data[idx][1];
    }

    /**
     * Get data value by index
     *
     * @param idx Index
     * @return Data value
     */
    public double getValue(int idx) {
        return data[idx][2];
    }

    /**
     * Set data value by index
     *
     * @param idx Index
     * @param value Data value
     */
    public void setValue(int idx, double value) {
        data[idx][2] = value;
    }
    
    /**
     * Get values
     * @return Values
     */
    public List<Double> getValues(){
        List<Double> values = new ArrayList<>();
        double v;
        for (int i = 0; i <this.getStNum(); i++){
            v = this.getValue(i);
            if (MIMath.doubleEquals(v, this.missingValue)){
                values.add(Double.NaN);
            } else {
                values.add(v);
            }           
        }
        
        return values;
    }
    
    /**
     * Get valid values
     * @return Values
     */
    public List<Double> getValidValues(){
        List<Double> values = new ArrayList<>();
        double v;
        for (int i = 0; i <this.getStNum(); i++){
            v = this.getValue(i);
            if (!MIMath.doubleEquals(v, this.missingValue)){                
                values.add(v);
            }           
        }
        
        return values;
    }
    
    /**
     * Index of - by station identifer
     * @param stid Station identifer
     * @return Data index
     */
    public int indexOf(int stid){
        return this.stations.indexOf(stid);
    }

    /**
     * Save station data to a CVS file
     *
     * @param fileName File name
     * @param fieldName Field name
     */
    public void saveAsCSVFile(String fileName, String fieldName) {
        this.saveAsCSVFile(fileName, fieldName, false);
    }

    /**
     * Save station data to a CVS file
     *
     * @param fileName File name
     * @param fieldName Field name
     * @param saveMissingData If save missing data
     */
    public void saveAsCSVFile(String fileName, String fieldName, boolean saveMissingData) {
        BufferedWriter sw = null;
        try {
            sw = new BufferedWriter(new FileWriter(new File(fileName)));
            String aStr = "Stid,Longitude,Latitude," + fieldName;
            sw.write(aStr);
            for (int i = 0; i < this.getStNum(); i++) {
                if (!saveMissingData) {
                    if (MIMath.doubleEquals(this.getValue(i), missingValue)) {
                        continue;
                    }
                }

                aStr = stations.get(i) + "," + String.valueOf(this.getX(i)) + "," + String.valueOf(this.getY(i))
                        + "," + String.valueOf(this.getValue(i));
                sw.newLine();
                sw.write(aStr);
            }
            sw.flush();
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(StationData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Maskout station data
     *
     * @param polygonShape Mask polygon shape
     * @return Result station data
     */
    public StationData maskout(PolygonShape polygonShape) {
        StationData stData = new StationData();
        stData.projInfo = this.projInfo;
        stData.missingValue = this.missingValue;
        for (int i = 0; i < this.getStNum(); i++) {
            if (GeoComputation.pointInPolygon(polygonShape, new PointD(this.getX(i), this.getY(i)))) {
                stData.addData(this.getStid(i), this.getX(i), this.getY(i), this.getValue(i));
            }
        }

        return stData;
    }

    /**
     * Maskout station data
     *
     * @param polygonShapes Mask polygon shapes
     * @return Result station data
     */
    public StationData maskout(List<PolygonShape> polygonShapes) {
        StationData stData = new StationData();
        stData.projInfo = this.projInfo;
        stData.missingValue = this.missingValue;
        for (int i = 0; i < this.getStNum(); i++) {
            if (GeoComputation.pointInPolygons(polygonShapes, new PointD(this.getX(i), this.getY(i)))) {
                stData.addData(this.getStid(i), this.getX(i), this.getY(i), this.getValue(i));
            }
        }

        return stData;
    }

    /**
     * Maskout station data
     *
     * @param maskLayer Mask layer
     * @return Result station data
     */
    public StationData maskout(VectorLayer maskLayer) {
        if (maskLayer.getShapeType() != ShapeTypes.Polygon) {
            return this;
        }

        List<PolygonShape> polygons = (List<PolygonShape>) maskLayer.getShapes();
        return this.maskout(polygons);
    }

    /**
     * Maskin station data
     *
     * @param polygonShape Mask polygon shape
     * @return Result station data
     */
    public StationData maskin(PolygonShape polygonShape) {
        StationData stData = new StationData();
        stData.projInfo = this.projInfo;
        stData.missingValue = this.missingValue;
        for (int i = 0; i < this.getStNum(); i++) {
            if (!GeoComputation.pointInPolygon(polygonShape, new PointD(this.getX(i), this.getY(i)))) {
                stData.addData(this.getStid(i), this.getX(i), this.getY(i), this.getValue(i));
            }
        }

        return stData;
    }

    /**
     * Maskin station data
     *
     * @param polygonShapes Mask polygon shapes
     * @return Result station data
     */
    public StationData maskin(List<PolygonShape> polygonShapes) {
        StationData stData = new StationData();
        stData.projInfo = this.projInfo;
        stData.missingValue = this.missingValue;
        for (int i = 0; i < this.getStNum(); i++) {
            if (!GeoComputation.pointInPolygons(polygonShapes, new PointD(this.getX(i), this.getY(i)))) {
                stData.addData(this.getStid(i), this.getX(i), this.getY(i), this.getValue(i));
            }
        }

        return stData;
    }

    /**
     * Maskin station data
     *
     * @param maskLayer Mask layer
     * @return Result station data
     */
    public StationData maskin(VectorLayer maskLayer) {
        if (maskLayer.getShapeType() != ShapeTypes.Polygon) {
            return this;
        }

        List<PolygonShape> polygons = (List<PolygonShape>) maskLayer.getShapes();
        return this.maskin(polygons);
    }

    /**
     * Filter station data
     *
     * @param stations Station identifer list
     * @return Result station data
     */
    public StationData filter(List<String> stations) {
        StationData stData = new StationData();
        stData.projInfo = this.projInfo;
        stData.missingValue = this.missingValue;
        for (int i = 0; i < this.getStNum(); i++) {
            if (stations.contains(this.getStid(i))) {
                stData.addData(this.getStid(i), this.getX(i), this.getY(i), this.getValue(i));
            }
        }

        return stData;
    }

    /**
     * Join an other station data
     *
     * @param indata Other station data
     * @return Joined station data
     */
    public StationData join(StationData indata) {
        StationData stData = new StationData(this);
        for (int i = 0; i < this.getStNum(); i++) {
            stData.addData(this.getStid(i), this.getX(i), this.getY(i), this.getValue(i));
        }
        for (int i = 0; i < indata.getStNum(); i++) {
            if (!stData.stations.contains(indata.getStid(i))) {
                stData.addData(indata.getStid(i), indata.getX(i), indata.getY(i), indata.getValue(i));
            }
        }

        return stData;
    }

    /**
     * Project station data
     *
     * @param fromProj From projection info
     * @param toProj To projection info
     * @return Projected station data
     */
    public StationData project(ProjectionInfo fromProj, ProjectionInfo toProj) {
        int i;
        double x, y;
        StationData nsData = new StationData();
        nsData.missingValue = missingValue;

        double[][] points = new double[1][];
        for (i = 0; i < this.getStNum(); i++) {
            points[0] = new double[]{this.getX(i), this.getY(i)};
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                x = points[0][0];
                y = points[0][1];

                nsData.addData(this.getStid(i), x, y, this.getValue(i));
            } catch (Exception e) {
                i++;
            }
        }

        nsData.projInfo = toProj;
        return nsData;
    }
    // </editor-fold>    
    // <editor-fold desc="Update">

    /**
     * Get station identifer index
     *
     * @param stid Station identifer
     * @return Index
     */
    public int getStidIndex(String stid) {
        int idx = -1;
        for (int i = 0; i < this.getStNum(); i++) {
            if (this.getStid(i).trim().equals(stid.trim())) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    /**
     * Update data extent
     */
    public void updateExtent() {
        int stNum = this.getStNum();
        double minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        double lon, lat;
        for (int i = 0; i < stNum; i++) {
            lon = data[i][0];
            lat = data[i][1];
            if (i == 0) {
                minX = lon;
                maxX = minX;
                minY = lat;
                maxY = minY;
            } else {
                if (minX > lon) {
                    minX = lon;
                } else if (maxX < lon) {
                    maxX = lon;
                }
                if (minY > lat) {
                    minY = lat;
                } else if (maxY < lat) {
                    maxY = lat;
                }
            }
        }
        dataExtent.minX = minX;
        dataExtent.maxX = maxX;
        dataExtent.minY = minY;
        dataExtent.maxY = maxY;
    }

    // </editor-fold>
    // <editor-fold desc="Interpolation">
    /**
     * Interpolate to grid data
     *
     * @param interSet Interpolation setting
     * @return Grid data
     */
    public GridData interpolateData(InterpolationSetting interSet) {
        GridData aGridData = null;
        double[] X;
        double[] Y;
        List<double[]> values = createGridXY(interSet.getGridDataSetting());
        X = values.get(0);
        Y = values.get(1);
        switch (interSet.getInterpolationMethod()) {
            case IDW_Radius:
                this.filterData_Radius(interSet.getRadius(), interSet.getGridDataSetting().dataExtent);
                aGridData = interpolate_Radius(data,
                        X, Y, interSet.getMinPointNum(), interSet.getRadius(), missingValue);
                break;
            case IDW_Neighbors:
                this.filterData_Radius(interSet.getRadius(), interSet.getGridDataSetting().dataExtent);
                aGridData = interpolate_Neighbor(data, X, Y,
                        interSet.getMinPointNum(), missingValue);
                break;
            case Cressman:
                this.filterData_Radius(0, interSet.getGridDataSetting().dataExtent);
                aGridData = interpolate_Cressman(data, X, Y, interSet.getRadiusList(), missingValue);
                break;
            case AssignPointToGrid:
                this.filterData_Radius(0, interSet.getGridDataSetting().dataExtent);
                aGridData = interpolate_Assign(data, X, Y, missingValue);
                break;
        }

        return aGridData;
    }

    /**
     * Interpolate by IDW radius method
     *
     * @param S Station data array
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param minPNum Minimum point number
     * @param radius Radius
     * @param missingValue Missing value
     * @return Grid data
     */
    public GridData interpolate_Radius(double[][] S, double[] X, double[] Y,
            int minPNum, double radius, double missingValue) {
        double[][] dataArray;
        dataArray = wContour.Interpolate.interpolation_IDW_Radius(S, X, Y, minPNum, radius, missingValue);

        GridData gridData = new GridData();
        gridData.data = dataArray;
        gridData.missingValue = missingValue;
        gridData.xArray = X;
        gridData.yArray = Y;

        return gridData;
    }
    
    /**
     * Interpolate by IDW radius method
     *
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param minPNum Minimum point number
     * @param radius Radius
     * @param missingValue Missing value
     * @return Grid data
     */
    public GridData interpolate_Radius(List<Number> X, List<Number> Y,
            int minPNum, double radius, double missingValue) {
        double[] nX = new double[X.size()];
        double[] nY = new double[Y.size()];
        for (int i = 0; i < X.size(); i++){
            nX[i] = X.get(i).doubleValue();
        }
        for (int i = 0; i < Y.size(); i++){
            nY[i] = Y.get(i).doubleValue();
        }
        
        return this.interpolate_Radius(data, nX, nY, minPNum, radius, missingValue);
    }

    /**
     * Interpolate by IDW_Neighbor method
     *
     * @param S Station data array
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param pNum Point number
     * @param missingValue Missing value
     * @return Grid data
     */
    public GridData interpolate_Neighbor(double[][] S, double[] X, double[] Y, int pNum, double missingValue) {
        double[][] dataArray = wContour.Interpolate.interpolation_IDW_Neighbor(S, X, Y, pNum, missingValue);

        GridData gridData = new GridData();
        gridData.data = dataArray;
        gridData.missingValue = missingValue;
        gridData.xArray = X;
        gridData.yArray = Y;

        return gridData;
    }
    
    /**
     * Interpolate by IDW_Neighbor method
     *
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param pNum Point number
     * @param missingValue Missing value
     * @return Grid data
     */
    public GridData interpolate_Neighbor(List<Number> X, List<Number> Y, int pNum, double missingValue) {
        double[] nX = new double[X.size()];
        double[] nY = new double[Y.size()];
        for (int i = 0; i < X.size(); i++){
            nX[i] = X.get(i).doubleValue();
        }
        for (int i = 0; i < Y.size(); i++){
            nY[i] = Y.get(i).doubleValue();
        }
        
        return this.interpolate_Neighbor(data, nX, nY, pNum, missingValue);
    }

    /**
     * Interpolation by Cressman method
     *
     * @param S Station data array
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param radList Radius list
     * @param missingValue Missing value
     * @return Grid data
     */
    public GridData interpolate_Cressman(double[][] S, double[] X, double[] Y,
            List<Double> radList, double missingValue) {
        double[][] dataArray = wContour.Interpolate.cressman(S, X, Y, missingValue, radList);

        GridData gridData = new GridData();
        gridData.data = dataArray;
        gridData.missingValue = missingValue;
        gridData.xArray = X;
        gridData.yArray = Y;

        return gridData;
    }
    
    /**
     * Interpolation by Cressman method
     *
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param radList Radius list
     * @param missingValue Missing value
     * @return Grid data
     */
    public GridData interpolate_Cressman(List<Number> X, List<Number> Y,
            List<Number> radList, double missingValue) {
        double[] nX = new double[X.size()];
        double[] nY = new double[Y.size()];
        for (int i = 0; i < X.size(); i++){
            nX[i] = X.get(i).doubleValue();
        }
        for (int i = 0; i < Y.size(); i++){
            nY[i] = Y.get(i).doubleValue();
        }

        List<Double> rlist = new ArrayList<>();
        for (Number r : radList){
            rlist.add(r.doubleValue());
        }
        return this.interpolate_Cressman(data, nX, nY, rlist, missingValue);
    }

    /**
     * Interpolation by assign method
     *
     * @param S Station data array
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param missingValue Missing value
     * @return Grid data
     */
    public GridData interpolate_Assign(double[][] S, double[] X, double[] Y, double missingValue) {
        double[][] dataArray = wContour.Interpolate.assignPointToGrid(S, X, Y, missingValue);

        GridData gridData = new GridData();
        gridData.data = dataArray;
        gridData.missingValue = missingValue;
        gridData.xArray = X;
        gridData.yArray = Y;

        return gridData;
    }
    
    /**
     * Interpolation by assign method
     *
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param missingValue Missing value
     * @return Grid data
     */
    public GridData interpolate_Assign(List<Number> X, List<Number> Y, double missingValue) {
        double[] nX = new double[X.size()];
        double[] nY = new double[Y.size()];
        for (int i = 0; i < X.size(); i++){
            nX[i] = X.get(i).doubleValue();
        }
        for (int i = 0; i < Y.size(); i++){
            nY[i] = Y.get(i).doubleValue();
        }

        return this.interpolate_Assign(data, nX, nY, missingValue);
    }

    /**
     * Create grid X/Y coordinate
     *
     * @param gSet
     * @return X/Y coordinate array list
     */
    public List<double[]> createGridXY(GridDataSetting gSet) {
        double xDelt = (gSet.dataExtent.maxX - gSet.dataExtent.minX) / (double) (gSet.xNum - 1);
        double yDelt = (gSet.dataExtent.maxY - gSet.dataExtent.minY) / (double) (gSet.yNum - 1);

        return wContour.Interpolate.createGridXY_Delt(gSet.dataExtent.minX, gSet.dataExtent.minY,
                gSet.dataExtent.maxX, gSet.dataExtent.maxY, xDelt, yDelt);
    }

    /**
     * Filte station data by radius and extent
     *
     * @param radius Radius
     * @param aExtent Data extent
     */
    public void filterData_Radius(double radius, Extent aExtent) {
        double[][] discretedData;
        List<double[]> disDataList = new ArrayList<double[]>();
        List<String> nstations = new ArrayList<String>();
        int i;
        for (i = 0; i < this.getStNum(); i++) {
            if (MIMath.doubleEquals(data[i][2], missingValue)) {
                continue;
            }
            if (data[i][0] + radius < aExtent.minX || data[i][0] - radius > aExtent.maxX
                    || data[i][1] + radius < aExtent.minY || data[i][1] - radius > aExtent.maxY) {
                continue;
            } else {
                disDataList.add(new double[]{data[i][0], data[i][1], data[i][2]});
                nstations.add(this.stations.get(i));
            }
        }

        discretedData = new double[disDataList.size()][3];
        i = 0;
        for (double[] disData : disDataList) {
            discretedData[i][0] = disData[0];
            discretedData[i][1] = disData[1];
            discretedData[i][2] = disData[2];
            i += 1;
        }

        this.setData(discretedData);
        stations = nstations;
    }
    // </editor-fold>
    // <editor-fold desc="Statictics">

    /**
     * Get minimum value
     *
     * @return Minimum value
     */
    public double getMinValue() {
        return (double)this.getMinValueIndex()[0];
    }

    /**
     * Get maximum value
     *
     * @return Maximum value
     */
    public double getMaxValue() {
        return (double)this.getMaxValueIndex()[0];
    }
    
    /**
     * Get minimum value and index
     *
     * @return Minimum value and index
     */
    public Object[] getMinValueIndex() {
        double min = 0;
        int vdNum = 0;
        int idx = 0;
        for (int i = 0; i < this.getStNum(); i++) {
            if (MIMath.doubleEquals(data[i][2], missingValue)) {
                continue;
            }

            if (vdNum == 0) {
                min = data[i][2];
                idx = i;
            } else {
                if (min > data[i][2]) {
                    min = data[i][2];
                    idx = i;
                }
            }
            vdNum += 1;
        }

        return new Object[]{min, idx};
    }

    /**
     * Get maximum value and index
     *
     * @return Maximum value and index
     */
    public Object[] getMaxValueIndex() {
        double max = 0;
        int vdNum = 0;
        int idx = 0;
        for (int i = 0; i < this.getStNum(); i++) {
            if (MIMath.doubleEquals(data[i][2], missingValue)) {
                continue;
            }

            if (vdNum == 0) {
                max = data[i][2];
                idx = i;
            } else {
                if (max < data[i][2]) {
                    max = data[i][2];
                    idx = i;
                }
            }
            vdNum += 1;
        }

        return new Object[]{max, idx};
    }

    /**
     * Get maximum and minimum values
     *
     * @param maxmin Maximum and minimum value array
     * @return Has missing value or not
     */
    public boolean getMaxMinValue(double[] maxmin) {
        double max = 0;
        double min = 0;
        int vdNum = 0;
        boolean hasMissingValue = false;
        for (int i = 0; i < this.getStNum(); i++) {
            if (MIMath.doubleEquals(data[i][2], missingValue)) {
                hasMissingValue = true;
                continue;
            }

            if (vdNum == 0) {
                max = data[i][2];
                min = max;
            } else {
                if (max < data[i][2]) {
                    max = data[i][2];
                }
                if (min > data[i][2]) {
                    min = data[i][2];
                }
            }
            vdNum += 1;
        }

        maxmin[0] = max;
        maxmin[1] = min;
        return hasMissingValue;
    }

    /**
     * Calculate average value
     *
     * @return Average value
     */
    public double average() {
        double ave = 0;
        int vdNum = 0;
        double v;
        for (int i = 0; i < this.getStNum(); i++) {
            v = this.getValue(i);
            if (MIMath.doubleEquals(v, missingValue)) {
                continue;
            }

            ave += v;
            vdNum += 1;
        }

        ave = ave / vdNum;

        return ave;
    }
    
    /**
     * Calculate summary value
     *
     * @return Summary value
     */
    public double sum() {
        double sum = 0;
        double v;
        for (int i = 0; i < this.getStNum(); i++) {
            v = this.getValue(i);
            if (MIMath.doubleEquals(v, missingValue)) {
                continue;
            }

            sum += v;
        }

        return sum;
    }
    // </editor-fold>
    // </editor-fold>
}
