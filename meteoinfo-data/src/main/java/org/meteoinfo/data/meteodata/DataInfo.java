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

import java.io.RandomAccessFile;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.google.errorprone.annotations.Var;
import org.meteoinfo.common.util.JDateUtil;
import org.meteoinfo.data.dimarray.DimArray;
import org.meteoinfo.data.dimarray.DimensionType;
import org.meteoinfo.data.meteodata.netcdf.NCUtil;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;

 /**
  * Template
  *
  * @author Yaqiang Wang
  */
 public abstract class DataInfo {

     // <editor-fold desc="Variables">

     protected String fileName;
     protected List<Variable> variables = new ArrayList<>();
     protected List<Variable> coordinates = new ArrayList<>();
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
      * Get data variables - excluding coordinate variables
      * @return Data variables
      */
     public List<Variable> getDataVariables() {
         List<Variable> dataVariables = new ArrayList<>();
         for (Variable variable : this.variables) {
             if (!variable.isDimVar()) {
                 dataVariables.add(variable);
             }
         }

         return dataVariables;
     }

     /**
      * Get coordinate variables
      * @return Coordinate variables
      */
     public List<Variable> getCoordinates() {
         return this.coordinates;
     }

     /**
      * Set coordinate variables
      * @param value Coordinate variables
      */
     public void setCoordinates(List<Variable> value) {
         this.coordinates = value;
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
      * Get data variable number
      * @return Data variable number
      */
     public int getDataVariableNum() {
         int i = 0;
         for (Variable var : variables) {
             if (!var.isDimVar()) {
                 i += 1;
             }
         }

         return i;
     }

     /**
      * Get coordinate variable number
      * @return Coordinate variable number
      */
     public int getCoordinateNum() {
         return this.coordinates.size();
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
      * Get coordinate names
      *
      * @return Coordinate names
      */
     public List<String> getCoordinateNames() {
         List<String> names = new ArrayList<>();
         for (Variable var : coordinates) {
             names.add(var.getName());
         }

         return names;
     }

     /**
      * Get times
      *
      * @return Times
      */
     public Array getTimes() {
         if (tDim == null) {
             return null;
         }

         return tDim.getDimValue();
     }

     /**
      * Get times
      *
      * @return Times
      */
     public List<LocalDateTime> getTimeList() {
         if (tDim == null) {
             return null;
         }

         List<LocalDateTime> times = new ArrayList<>();
         IndexIterator iter = tDim.getDimValue().getIndexIterator();
         while (iter.hasNext()) {
             times.add(iter.getDateNext());
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
         if (tDim == null)
             return null;

         return tDim.getDimValue().getDate(timeIdx);
     }

     /**
      * Get time double value
      * @param timeIdx Time index
      * @return Time double value
      */
     public double getTimeValue(int timeIdx) {
         if (tDim == null)
             return Double.NaN;

         return JDateUtil.toOADate(tDim.getDimValue().getDate(timeIdx));
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
         Array times = this.getTimes();
         List<Integer> values = new ArrayList<>();
         IndexIterator iter = times.getIndexIterator();
         while (iter.hasNext()){
             LocalDateTime time = iter.getDateNext();
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
         Array times = Array.factory(DataType.DATE, new int[]{value.size()});
         IndexIterator iter = times.getIndexIterator();
         for (LocalDateTime t : value) {
             iter.setDateNext(t);
         }

         if (tDim == null) {
             tDim = new Dimension(DimensionType.T);
         }

         tDim.setDimValue(times);
     }

     /**
      * Set times array
      * @param value Times array
      */
     public void setTimes(Array value) {
         tDim.setDimValue(value);
     }

     /**
      * Get time number
      *
      * @return Time number
      */
     public int getTimeNum() {
         if (tDim == null)
             return 0;

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
      * Get x coordinate variable name
      * @return X coordinate variable name
      */
     public String getXCoordVariableName() {
         if (this.projInfo.isLonLat()) {
             return "lon";
         } else {
             return "x";
         }
     }

     /**
      * Get y coordinate variable name
      * @return Y coordinate variable name
      */
     public String getYCoordVariableName() {
         if (this.projInfo.isLonLat()) {
             return "lat";
         } else {
             return "y";
         }
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

     public abstract boolean isValidFile(RandomAccessFile raf);

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
         Attribute aAttS;
         dataInfo = "File Name: " + this.getFileName();
         //dataInfo += System.getProperty("line.separator") + "File type: " + _fileTypeStr + " (" + _fileTypeId + ")";
         dataInfo += System.getProperty("line.separator") + "Dimensions: " + dimensions.size();
         for (Dimension dimension : dimensions) {
             dataInfo += System.getProperty("line.separator") + "\t" + dimension.getShortName() + " = "
                     + String.valueOf(dimension.getLength()) + ";";
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
         for (Attribute attribute : attributes) {
             dataInfo += System.getProperty("line.separator") + "\t: " + attribute.toString();
         }

         List<Variable> dataVariables = this.getDataVariables();
         dataInfo += System.getProperty("line.separator") + "Data Variables: " + dataVariables.size();
         for (Variable variable : dataVariables) {
             dataInfo += System.getProperty("line.separator") + "\t" + variable.getDataType().toString()
                     + " " + variable.getShortName() + "(";
             for (Dimension dim : variable.getDimensions()) {
                 dataInfo += dim.getShortName() + ",";
             }
             dataInfo = dataInfo.substring(0, dataInfo.length() - 1);
             dataInfo += ");";
             List<Attribute> atts = variable.getAttributes();
             for (int j = 0; j < atts.size(); j++) {
                 aAttS = atts.get(j);
                 dataInfo += System.getProperty("line.separator") + "\t" + "\t" + variable.getShortName()
                         + ": " + aAttS.toString();
             }
         }

         dataInfo += System.getProperty("line.separator") + "Coordinates: " + coordinates.size();
         for (Variable coord : coordinates) {
             dataInfo += System.getProperty("line.separator") + "\t" + coord.getDataType().toString()
                     + " " + coord.getShortName() + "(";
             for (Dimension dim : coord.getDimensions()) {
                 dataInfo += dim.getShortName() + ",";
             }
             dataInfo = dataInfo.substring(0, dataInfo.length() - 1);
             dataInfo += ");";
             for (Attribute attr : coord.getAttributes()) {
                 dataInfo += System.getProperty("line.separator") + "\t" + "\t" + coord.getShortName()
                         + ": " + attr.toString();
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
     public Array read(String varName) {
         Variable var = this.getVariable(varName);
         if (var != null) {
             if (var.hasCachedData()) {
                 return var.cachedData.copy();
             }
         }

         return realRead(varName);
     }

     /**
      * Read array data
      * @param varName Variable name
      * @return Array
      */
     public abstract Array realRead(String varName);

     /**
      * Read dimension array data
      * @param varName Variable name
      * @return Dimension array
      */
     public DimArray readDimArray(String varName) {
         Variable variable = this.getVariable(varName);
         if (variable == null) {
             System.out.println("The variable is not exist: " + varName);
             return null;
         }

         Array array = read(varName);

         return new DimArray(array, variable.getDimensions());
     }

     /**
      * Read array data
      *
      * @param varName Variable name
      * @param origin Origin array
      * @param size Size array
      * @param stride Stride array
      * @return Array
      */
     public Array read(String varName, int[] origin, int[] size, int[] stride) {
         Variable var = this.getVariable(varName);
         if (var == null) {
             System.out.println("The variable is not exist: " + varName);
             return null;
         }

         if (var.hasCachedData()) {
             boolean negStride = false;
             for (int s : stride) {
                 if (s < 0) {
                     negStride = true;
                     break;
                 }
             }
             List<Integer> flips = new ArrayList<>();
             if (negStride) {
                 int[] pStride = new int[stride.length];
                 for (int i = 0; i < stride.length; i++) {
                     pStride[i] = Math.abs(stride[i]);
                     if (stride[i] < 0) {
                         flips.add(i);
                     }
                 }
                 stride = pStride;
             }
             Section section = null;
             try {
                 section = new Section(origin, size, stride);
                 Array r = var.getCachedData().section(section.getRanges()).copy();
                 if (negStride) {
                     for (int i : flips) {
                         r = r.flip(i);
                     }
                     Array data = Array.factory(r.getDataType(), r.getShape());
                     MAMath.copy(data, r);
                     return data;
                 }
                 return r;
             } catch (InvalidRangeException e) {
                 throw new RuntimeException(e);
             }
         } else {
             return realRead(varName, origin, size, stride);
         }
     }

     /**
      * Read array data
      *
      * @param varName Variable name
      * @param origin Origin array
      * @param size Size array
      * @param stride Stride array
      * @return Array
      */
     public abstract Array realRead(String varName, int[] origin, int[] size, int[] stride);

     /**
      * Read dimension array data
      *
      * @param varName Variable name
      * @param origin Origin array
      * @param size Size array
      * @param stride Stride array
      * @return Dimension array
      */
     public DimArray readDimArray(String varName, int[] origin, int[] size, int[] stride) {
         Variable variable = this.getVariable(varName);
         if (variable == null) {
             System.out.println("The variable is not exist: " + varName);
             return null;
         }

         Array array = read(varName, origin, size, stride).reduce();
         ArrayMath.missingToNaN(array, this.missingValue);
         try {
             List<Dimension> dimensions = variable.sectionDimensions(origin, size, stride);
             return new DimArray(array, dimensions);
         } catch (InvalidRangeException e) {
             e.printStackTrace();
             return new DimArray(array);
         }
     }

     /**
      * Read dimension array data
      *
      * @param varName Variable name
      * @param ranges Range list
      * @return Dimension array
      */
     public DimArray readDimArray(String varName, List<Range> ranges) {
         int n = ranges.size();
         int[] origin = new int[n], size = new int[n], stride = new int[n];
         ArrayMath.rangesToSection(ranges, origin, size, stride);

         return readDimArray(varName, origin, size, stride);
     }

     /**
      * Get global attributes
      * @return Global attributes
      */
     public List<Attribute> getGlobalAttributes() {
         return this.attributes;
     };

     /**
      * Get variable by name
      *
      * @param varName Variable name
      * @return The variable
      */
     public Variable getVariable(String varName) {
         for (Variable var : variables) {
             if (var.getName().equalsIgnoreCase(varName)) {
                 return var;
             }
         }

         for (Variable var : variables) {
             if (var.getShortName().equalsIgnoreCase(varName)) {
                 return var;
             }
         }

         return null;
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
      * Add a coordinate variable
      * @param var Coordinate variable
      */
     public void addCoordinate(Variable var) {
         this.coordinates.add(var);
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

     /**
      * Find global attribute
      *
      * @param attName Attribute name
      * @return Global attribute
      */
     public Attribute findGlobalAttribute(String attName) {
         for (Attribute att : this.attributes) {
             if (att.getShortName().equalsIgnoreCase(attName)) {
                 return att;
             }
         }

         return null;
     }

     /**
      * Get the data is Radial (Radar) or not
      * @return Is Radial or not
      */
     public boolean isRadial() {
         Attribute ra = findGlobalAttribute("featureType");
         if (ra != null) {
             String va = ra.getStringValue();
             if (va.equalsIgnoreCase("RADIAL")) {
                 return true;
             }
         }

         return false;
     }

     // </editor-fold>
 }
