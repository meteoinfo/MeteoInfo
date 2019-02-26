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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.meteoinfo.global.util.DateUtil;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import ucar.ma2.Array;
import ucar.nc2.Attribute;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public abstract class DataInfo {

    // <editor-fold desc="Variables">

    private String _fileName;
    private List<Variable> _variables = new ArrayList<>();
    private List<Dimension> _dimensions = new ArrayList<>();
    private List<Attribute> attributes = new ArrayList<>();
    private Dimension _tDim = null;
    private Dimension _xDim = null;
    private Dimension _yDim = null;
    private Dimension _zDim = null;
    private boolean _xReverse = false;
    private boolean _yReverse = false;
    private boolean _isGlobal = false;
    private double _missingValue = -9999.0;
    private ProjectionInfo _projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
    protected MeteoDataType dataType;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get file name
     *
     * @return File name
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Set file name
     *
     * @param name File name
     */
    public void setFileName(String name) {
        _fileName = name;
    }

    /**
     * Get variables
     *
     * @return Variables
     */
    public List<Variable> getVariables() {
        return _variables;
    }

    /**
     * Set variables
     *
     * @param value Variables
     */
    public void setVariables(List<Variable> value) {
        _variables = value;
    }

    /**
     * Get plottable variables
     *
     * @return Plottable variables
     */
    public List<Variable> getPlottableVariables() {
        List<Variable> vars = new ArrayList<>();
        for (Variable var : _variables) {
            if (var.isPlottable()) {
                vars.add(var);
            }
        }

        return vars;
    }

    /**
     * Get dimensions
     *
     * @return Dimensions
     */
    public List<Dimension> getDimensions() {
        return this._dimensions;
    }

    /**
     * Set dimensions
     *
     * @param dims Dimensions
     */
    public void setDimensions(List<Dimension> dims) {
        this._dimensions = dims;
    }

    /**
     * Get variable number
     *
     * @return Variable number
     */
    public int getVariableNum() {
        return _variables.size();
    }

    /**
     * Get variable names
     *
     * @return Variable names
     */
    public List<String> getVariableNames() {
        List<String> names = new ArrayList<>();
        for (Variable var : _variables) {
            names.add(var.getName());
        }

        return names;
    }

    /**
     * Get times
     *
     * @return Times
     */
    public List<Date> getTimes() {
        List<Double> values = _tDim.getDimValue();
        List<Date> times = new ArrayList<>();
        for (Double v : values) {
            times.add(DateUtil.fromOADate(v));
        }

        return times;
    }

    /**
     * Get time
     *
     * @param timeIdx Time index
     * @return Time
     */
    public Date getTime(int timeIdx) {
        return DateUtil.fromOADate(_tDim.getDimValue().get(timeIdx));
    }
    
    /**
     * Get time doble value
     * @param timeIdx Time index
     * @return Time double value
     */
    public double getTimeValue(int timeIdx) {
        return _tDim.getDimValue().get(timeIdx);
    }

    /**
     * Get time value
     * @param time Time
     * @param baseDate Base time
     * @param tDelta Delta time
     * @return Time value
     */
    public static int getTimeValue(Date time, Date baseDate, String tDelta) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        long sl = cal.getTimeInMillis();
        long el, delta;
        int value = 0;
        cal.setTime(time);
        el = cal.getTimeInMillis();
        delta = el - sl;
        switch (tDelta.toLowerCase()) {
            case "seconds":
                value = (int) (delta / (1000));
                break;
            case "minutes":
                value = (int) (delta / (60 * 1000));
                break;
            case "hours":
                value = (int) (delta / (60 * 60 * 1000));
                break;
            case "days":
                value = (int) (delta / (24 * 60 * 60 * 1000));
                break;
        }

        return value;
    }

    /**
     * Get time values - Time delta values of base date
     *
     * @param baseDate Base date
     * @param tDelta Time delta type - days/hours/...
     * @return Time values
     */
    public List<Integer> getTimeValues(Date baseDate, String tDelta) {
        List<Date> times = this.getTimes();
        List<Integer> values = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        long sl = cal.getTimeInMillis();
        long el, delta;
        int value;
        for (Date time : times) {
            cal.setTime(time);
            el = cal.getTimeInMillis();
            delta = el - sl;
            if (tDelta.equalsIgnoreCase("hours")) {
                value = (int) (delta / (60 * 60 * 1000));
                values.add(value);
            } else if (tDelta.equalsIgnoreCase("days")) {
                value = (int) (delta / (24 * 60 * 60 * 1000));
                values.add(value);
            }
        }

        return values;
    }

    /**
     * Set times
     *
     * @param value Times
     */
    public void setTimes(List<Date> value) {
        List<Date> times = value;
        List<Double> values = new ArrayList<>();
        for (Date t : times) {
            values.add(DateUtil.toOADate(t));
        }
        _tDim.setValues(values);
    }

    /**
     * Get time number
     *
     * @return Time number
     */
    public int getTimeNum() {
        return _tDim.getLength();
    }

    /**
     * Get time dimension
     *
     * @return Time dimension
     */
    public Dimension getTimeDimension() {
        return _tDim;
    }

    /**
     * Set time dimension
     *
     * @param tDim Time dimension
     */
    public void setTimeDimension(Dimension tDim) {
        _tDim = tDim;
    }

    /**
     * Get x dimension
     *
     * @return X dimension
     */
    public Dimension getXDimension() {
        return _xDim;
    }

    /**
     * Set x dimension
     *
     * @param xDim X dimension
     */
    public void setXDimension(Dimension xDim) {
        _xDim = xDim;
    }

    /**
     * Get y dimension
     *
     * @return Y dimension
     */
    public Dimension getYDimension() {
        return _yDim;
    }

    /**
     * Set y dimension
     *
     * @param yDim Y dimension
     */
    public void setYDimension(Dimension yDim) {
        _yDim = yDim;
    }

    /**
     * Get z dimension
     *
     * @return Z dimension
     */
    public Dimension getZDimension() {
        return _zDim;
    }

    /**
     * Set z dimension
     *
     * @param zDim Z dimension
     */
    public void setZDimension(Dimension zDim) {
        _zDim = zDim;
    }

    /**
     * Get if x reversed
     *
     * @return Boolean
     */
    public boolean isXReverse() {
        return _xReverse;
    }

    /**
     * Set if x reversed
     *
     * @param value Boolean
     */
    public void setXReverse(boolean value) {
        _xReverse = value;
    }

    /**
     * Get if y reversed
     *
     * @return Boolean
     */
    public boolean isYReverse() {
        return _yReverse;
    }

    /**
     * Set if y reversed
     *
     * @param value Boolean
     */
    public void setYReverse(boolean value) {
        _yReverse = value;
    }

    /**
     * Get if is global data
     *
     * @return Boolean
     */
    public boolean isGlobal() {
        return _isGlobal;
    }

    /**
     * Set if is global data
     *
     * @param value
     */
    public void setGlobal(boolean value) {
        _isGlobal = value;
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
     * Get projection info
     *
     * @return Projection info
     */
    public ProjectionInfo getProjectionInfo() {
        return _projInfo;
    }

    /**
     * Set projection info
     *
     * @param projInfo Projection info
     */
    public void setProjectionInfo(ProjectionInfo projInfo) {
        _projInfo = projInfo;
    }

    /**
     * Get data type
     *
     * @return The data type
     */
    public MeteoDataType getDataType() {
        return dataType;
    }

    /**
     * Set data type
     *
     * @param value The data type
     */
    public void setDataType(MeteoDataType value) {
        dataType = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Read data info
     *
     * @param fileName File name
     */
    public abstract void readDataInfo(String fileName);
    
    /**
     * Read data info
     *
     * @param fileName File name
     * @param keepOpen Keep file opened or not
     */
    public void readDataInfo(String fileName, boolean keepOpen){    };

    /**
     * Generate data info text
     *
     * @return Data info text
     */
    public String generateInfoText() {
        String dataInfo;
        int i, j;
        ucar.nc2.Attribute aAttS;
        dataInfo = "File Name: " + this.getFileName();
        //dataInfo += System.getProperty("line.separator") + "File type: " + _fileTypeStr + " (" + _fileTypeId + ")";
        dataInfo += System.getProperty("line.separator") + "Dimensions: " + _dimensions.size();
        for (i = 0; i < _dimensions.size(); i++) {
            dataInfo += System.getProperty("line.separator") + "\t" + _dimensions.get(i).getShortName() + " = "
                    + String.valueOf(_dimensions.get(i).getLength()) + ";";
        }

        Dimension xdim = this.getXDimension();
        if (xdim != null) {
            dataInfo += System.getProperty("line.separator") + "X Dimension: Xmin = " + String.valueOf(xdim.getMinValue())
                    + "; Xmax = " + String.valueOf(xdim.getMaxValue()) + "; Xsize = "
                    + String.valueOf(xdim.getLength()) + "; Xdelta = " + String.valueOf(xdim.getDeltaValue());
        }
        Dimension ydim = this.getYDimension();
        if (ydim != null) {
            dataInfo += System.getProperty("line.separator") + "Y Dimension: Ymin = " + String.valueOf(ydim.getMinValue())
                    + "; Ymax = " + String.valueOf(ydim.getMaxValue()) + "; Ysize = "
                    + String.valueOf(ydim.getLength()) + "; Ydelta = " + String.valueOf(ydim.getDeltaValue());
        }

        dataInfo += System.getProperty("line.separator") + "Global Attributes: ";
        for (i = 0; i < this.attributes.size(); i++) {
            aAttS = this.attributes.get(i);
            dataInfo += System.getProperty("line.separator") + "\t: " + aAttS.toString();
        }

        dataInfo += System.getProperty("line.separator") + "Variations: " + _variables.size();
        for (i = 0; i < _variables.size(); i++) {
            dataInfo += System.getProperty("line.separator") + "\t" + _variables.get(i).getDataType().toString()
                    + " " + _variables.get(i).getShortName() + "(";
            List<ucar.nc2.Dimension> dims = _variables.get(i).getDimensions();
            for (j = 0; j < dims.size(); j++) {
                dataInfo += dims.get(j).getShortName() + ",";
            }
            dataInfo = dataInfo.substring(0, dataInfo.length() - 1);
            dataInfo += ");";
            List<ucar.nc2.Attribute> atts = _variables.get(i).getAttributes();
            for (j = 0; j < atts.size(); j++) {
                aAttS = atts.get(j);
                dataInfo += System.getProperty("line.separator") + "\t" + "\t" + _variables.get(i).getShortName()
                        + ": " + aAttS.toString();
            }
        }

        for (ucar.nc2.Dimension dim : _dimensions) {
            if (dim.isUnlimited()) {
                dataInfo += System.getProperty("line.separator") + "Unlimited dimension: " + dim.getShortName();
            }
            break;
        }

        return dataInfo;
    }
    
    /**
     * Read array data
     * @param varName Variable name
     * @return Array
     */
    public abstract Array read(String varName);

    /**
     * Read array data
     *
     * @param varName Variable name
     * @param origin Origin array
     * @param size Size array
     * @param stride Stride array
     * @return Array
     */
    public abstract Array read(String varName, int[] origin, int[] size, int[] stride);
    
    /**
     * Get global attributes
     * @return Global attributes
     */
    public abstract List<Attribute> getGlobalAttributes();

    /**
     * Get variable by name
     *
     * @param varName Variable name
     * @return The variable
     */
    public Variable getVariable(String varName) {
        for (Variable var : _variables) {
            if (var.getName().equalsIgnoreCase(varName)) {
                return var;
            }
        }

        return null;
    }

    /**
     * Add a variable
     *
     * @param var Variable
     */
    public void addVariable(Variable var) {
        this._variables.add(var);
    }

    /**
     * Add a dimension
     *
     * @param dim Dimension
     */
    public void addDimension(Dimension dim) {
        this._dimensions.add(dim);
    }
    
    /**
     * Add a global attribute
     * @param attr The attribute
     */
    public void addAttribute(Attribute attr){
        this.attributes.add(attr);
    }

    // </editor-fold>
}
