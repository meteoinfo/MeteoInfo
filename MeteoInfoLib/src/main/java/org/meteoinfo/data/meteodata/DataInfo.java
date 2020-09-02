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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public abstract class DataInfo {

    // <editor-fold desc="Variables">

    protected String fileName;
    protected List<Variable> variables = new ArrayList<>();
    protected List<Dimension> dimensions = new ArrayList<>();
    protected List<Attribute> attributes = new ArrayList<>();
    protected Dimension tDim = null;
    protected Dimension xDim = null;
    protected Dimension yDim = null;
    protected Dimension zDim = null;
    protected boolean xReverse = false;
    protected boolean yReverse = false;
    protected boolean isGlobal = false;
    protected double missingValue = -9999.0;
    protected ProjectionInfo projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
    protected MeteoDataType meteoDataType;

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
        return fileName;
    }

    /**
     * Set file name
     *
     * @param name File name
     */
    public void setFileName(String name) {
        fileName = name;
    }

    /**
     * Get variables
     *
     * @return Variables
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Set variables
     *
     * @param value Variables
     */
    public void setVariables(List<Variable> value) {
        variables = value;
    }

    /**
     * Get plottable variables
     *
     * @return Plottable variables
     */
    public List<Variable> getPlottableVariables() {
        List<Variable> vars = new ArrayList<>();
        for (Variable var : variables) {
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
        return this.dimensions;
    }

    /**
     * Set dimensions
     *
     * @param dims Dimensions
     */
    public void setDimensions(List<Dimension> dims) {
        this.dimensions = dims;
    }

    /**
     * Get variable number
     *
     * @return Variable number
     */
    public int getVariableNum() {
        return variables.size();
    }

    /**
     * Get variable names
     *
     * @return Variable names
     */
    public List<String> getVariableNames() {
        List<String> names = new ArrayList<>();
        for (Variable var : variables) {
            names.add(var.getName());
        }

        return names;
    }

    /**
     * Get times
     *
     * @return Times
     */
    public List<LocalDateTime> getTimes() {
        List<Double> values = tDim.getDimValue();
        List<LocalDateTime> times = new ArrayList<>();
        for (Double v : values) {
            times.add(JDateUtil.fromOADate(v));
        }

        return times;
    }

    /**
     * Get time
     *
     * @param timeIdx Time index
     * @return Time
     */
    public LocalDateTime getTime(int timeIdx) {
        return JDateUtil.fromOADate(tDim.getDimValue().get(timeIdx));
    }
    
    /**
     * Get time doble value
     * @param timeIdx Time index
     * @return Time double value
     */
    public double getTimeValue(int timeIdx) {
        return tDim.getDimValue().get(timeIdx);
    }

    /**
     * Get time value
     * @param time Time
     * @param baseDate Base time
     * @param tDelta Delta time
     * @return Time value
     */
    public static int getTimeValue(LocalDateTime time, LocalDateTime baseDate, String tDelta) {
        int value = 0;
        switch (tDelta.toLowerCase()) {
            case "seconds":
                value = (int)Duration.between(baseDate, time).getSeconds();
                break;
            case "minutes":
                value = (int)Duration.between(baseDate, time).toMinutes();
                break;
            case "hours":
                value = (int)Duration.between(baseDate, time).toHours();
                break;
            case "days":
                value = Period.between(baseDate.toLocalDate(), time.toLocalDate()).getDays();
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
    public List<Integer> getTimeValues(LocalDateTime baseDate, String tDelta) {
        List<LocalDateTime> times = this.getTimes();
        List<Integer> values = new ArrayList<>();
        for (LocalDateTime time : times) {
            if (tDelta.equalsIgnoreCase("hours")) {
                values.add((int)Duration.between(baseDate, time).toHours());
            } else if (tDelta.equalsIgnoreCase("days")) {
                values.add(Period.between(baseDate.toLocalDate(), time.toLocalDate()).getDays());
            }
        }

        return values;
    }

    /**
     * Set times
     *
     * @param value Times
     */
    public void setTimes(List<LocalDateTime> value) {
        List<Double> values = new ArrayList<>();
        for (LocalDateTime t : value) {
            values.add(JDateUtil.toOADate(t));
        }
        tDim.setValues(values);
    }

    /**
     * Get time number
     *
     * @return Time number
     */
    public int getTimeNum() {
        return tDim.getLength();
    }

    /**
     * Get time dimension
     *
     * @return Time dimension
     */
    public Dimension getTimeDimension() {
        return tDim;
    }

    /**
     * Set time dimension
     *
     * @param tDim Time dimension
     */
    public void setTimeDimension(Dimension tDim) {
        this.tDim = tDim;
    }

    /**
     * Get x dimension
     *
     * @return X dimension
     */
    public Dimension getXDimension() {
        return xDim;
    }

    /**
     * Set x dimension
     *
     * @param xDim X dimension
     */
    public void setXDimension(Dimension xDim) {
        this.xDim = xDim;
    }

    /**
     * Get y dimension
     *
     * @return Y dimension
     */
    public Dimension getYDimension() {
        return yDim;
    }

    /**
     * Set y dimension
     *
     * @param yDim Y dimension
     */
    public void setYDimension(Dimension yDim) {
        this.yDim = yDim;
    }

    /**
     * Get z dimension
     *
     * @return Z dimension
     */
    public Dimension getZDimension() {
        return zDim;
    }

    /**
     * Set z dimension
     *
     * @param zDim Z dimension
     */
    public void setZDimension(Dimension zDim) {
        this.zDim = zDim;
    }

    /**
     * Get if x reversed
     *
     * @return Boolean
     */
    public boolean isXReverse() {
        return xReverse;
    }

    /**
     * Set if x reversed
     *
     * @param value Boolean
     */
    public void setXReverse(boolean value) {
        xReverse = value;
    }

    /**
     * Get if y reversed
     *
     * @return Boolean
     */
    public boolean isYReverse() {
        return yReverse;
    }

    /**
     * Set if y reversed
     *
     * @param value Boolean
     */
    public void setYReverse(boolean value) {
        yReverse = value;
    }

    /**
     * Get if is global data
     *
     * @return Boolean
     */
    public boolean isGlobal() {
        return isGlobal;
    }

    /**
     * Set if is global data
     *
     * @param value
     */
    public void setGlobal(boolean value) {
        isGlobal = value;
    }

    /**
     * Get missing data
     *
     * @return Missing data
     */
    public double getMissingValue() {
        return missingValue;
    }

    /**
     * Set missing data
     *
     * @param value Missing data
     */
    public void setMissingValue(double value) {
        missingValue = value;
    }

    /**
     * Get projection info
     *
     * @return Projection info
     */
    public ProjectionInfo getProjectionInfo() {
        return projInfo;
    }

    /**
     * Set projection info
     *
     * @param value Projection info
     */
    public void setProjectionInfo(ProjectionInfo value) {
        this.projInfo = value;
    }

    /**
     * Get data type
     *
     * @return The data type
     */
    public MeteoDataType getDataType() {
        return meteoDataType;
    }

    /**
     * Set data type
     *
     * @param value The data type
     */
    public void setDataType(MeteoDataType value) {
        meteoDataType = value;
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
        Attribute aAttS;
        dataInfo = "File Name: " + this.getFileName();
        //dataInfo += System.getProperty("line.separator") + "File type: " + _fileTypeStr + " (" + _fileTypeId + ")";
        dataInfo += System.getProperty("line.separator") + "Dimensions: " + dimensions.size();
        for (i = 0; i < dimensions.size(); i++) {
            dataInfo += System.getProperty("line.separator") + "\t" + dimensions.get(i).getShortName() + " = "
                    + String.valueOf(dimensions.get(i).getLength()) + ";";
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

        dataInfo += System.getProperty("line.separator") + "Variations: " + variables.size();
        for (i = 0; i < variables.size(); i++) {
            dataInfo += System.getProperty("line.separator") + "\t" + variables.get(i).getDataType().toString()
                    + " " + variables.get(i).getShortName() + "(";
            List<Dimension> dims = variables.get(i).getDimensions();
            for (j = 0; j < dims.size(); j++) {
                dataInfo += dims.get(j).getShortName() + ",";
            }
            dataInfo = dataInfo.substring(0, dataInfo.length() - 1);
            dataInfo += ");";
            List<Attribute> atts = variables.get(i).getAttributes();
            for (j = 0; j < atts.size(); j++) {
                aAttS = atts.get(j);
                dataInfo += System.getProperty("line.separator") + "\t" + "\t" + variables.get(i).getShortName()
                        + ": " + aAttS.toString();
            }
        }

        for (Dimension dim : dimensions) {
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
        Variable v = null;
        for (Variable var : variables) {
            if (var.getName().equalsIgnoreCase(varName)) {
                v = var;
                break;
            }
        }

        if (v == null) {
            for (Variable var : variables) {
                if (var.getShortName().equalsIgnoreCase(varName)) {
                    v = var;
                    break;
                }
            }
        }

        return v;
    }

    /**
     * Get variable index
     * @param varName Variable name
     * @return Variable index
     */
    public int getVariableIndex(String varName) {
        int varIdx = -1;
        int i = 0;
        for (Variable var : variables) {
            if (var.getName().equalsIgnoreCase(varName)) {
                varIdx = i;
                break;
            }
            i ++;
        }

        if (varIdx < 0) {
            i = 0;
            for (Variable var : variables) {
                if (var.getShortName().equalsIgnoreCase(varName)) {
                    varIdx = i;
                    break;
                }
                i ++;
            }
        }

        return varIdx;
    }

    /**
     * Add a variable
     *
     * @param var Variable
     */
    public void addVariable(Variable var) {
        this.variables.add(var);
    }

    /**
     * Add a dimension
     *
     * @param dim Dimension
     */
    public void addDimension(Dimension dim) {
        this.dimensions.add(dim);
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
