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

import org.meteoinfo.common.util.JDateUtil;
import org.meteoinfo.data.dimarray.DimArray;
import org.meteoinfo.data.dimarray.DimensionType;
import org.meteoinfo.data.dimarray.Dimension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.meteoinfo.ndarray.*;

/**
 *
 * @author Yaqiang Wang
 */
public class Variable {
    // <editor-fold desc="Variables">

    public int Number;
    private  String name;
    private String shortName;
    private DataType dataType;
    protected int[] shape = new int[0];
    protected List<Dimension> dimensions = new ArrayList<>();
    protected List<Attribute> attributes = new ArrayList<>();
    protected double addOffset = 0;
    protected double scaleFactor = 1;
    protected double fillValue = Double.NaN;
    private int levelType;
    private List<Double> levels;
    private String units;
    private String description;
    private String hdfPath;
    private boolean isStation = false;
    private boolean isSwath = false;
    private int varId;
    private boolean dimVar = false;
    private boolean memberOfStructure = false;
    private List<Integer> levelIdxs = new ArrayList<>();
    private List<Integer> varInLevelIdxs = new ArrayList<>();
    private Array cachedData;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Variable() {
        this.name = "null";
        this.shortName = null;
        this.dataType = DataType.FLOAT;
        levels = new ArrayList<>();
        units = "null";
        description = "null";
    }

    /**
     * Constructor
     *
     * @param aNum Parameter number
     * @param aName The name
     * @param aDesc The description
     * @param aUnit The units
     */
    public Variable(int aNum, String aName, String aDesc, String aUnit) {
        Number = aNum;
        this.name = aName;
        this.units = aUnit;
        description = aDesc;
        levels = new ArrayList<>();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get name
     *
     * @return Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get short name
     *
     * @return Short name
     */
    public String getShortName() {
        return this.shortName == null ? this.name : this.shortName;
    }

    /**
     * Set short name
     *
     * @param value Short name
     */
    public void setShortName(String value) {
        this.shortName = value;
    }

    /**
     * Set name
     *
     * @param value Name
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Get data type
     *
     * @return Data type
     */
    public DataType getDataType() {
        return this.dataType;
    }

    /**
     * Set data type
     *
     * @param value Data type
     */
    public void setDataType(DataType value) {
        this.dataType = value;
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
     * Get dimensions by section
     * @param section The section
     * @return Result dimesions
     */
    public List<Dimension> getDimensions(Section section) {
        List<Dimension> dims = new ArrayList<>();
        for (int i = 0; i < section.getRank(); i++) {
            Range range = section.getRange(i);
            if (range.length() > 1) {
                Dimension dim = this.dimensions.get(i).extract(range);
                dims.add(dim);
            }
        }
        
        return dims;
    }

    /**
     * Get dimension
     *
     * @param index Dimension index
     * @return Dimension
     */
    public Dimension getDimension(int index) {
        return this.dimensions.get(index);
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
     * Get attributes
     *
     * @return Attributes
     */
    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    /**
     * Get attribute
     *
     * @param index Attribute index
     * @return Attribute
     */
    public Attribute getAttribute(int index) {
        return this.attributes.get(index);
    }

    /**
     * Get add offset value
     * @return Add offset value
     */
    public double getAddOffset() {
        return this.addOffset;
    }

    /**
     * Set add offset value
     * @param value Add offset value
     */
    public void setAddOffset(double value) {
        this.addOffset = value;
    }

    /**
     * Get scale factor value
     * @return Scale factor value
     */
    public double getScaleFactor() {
        return this.scaleFactor;
    }

    /**
     * Set scale factor value
     * @param value Scale factor value
     */
    public void setScaleFactor(double value) {
        this.scaleFactor = value;
    }

    /**
     * Get fill value
     * @return Fill value
     */
    public double getFillValue() {
        return this.fillValue;
    }

    /**
     * Set fill value
     * @param value Fill value
     */
    public void setFillValue(double value) {
        this.fillValue = value;
    }

    /**
     * Get level type
     *
     * @return Level type
     */
    public int getLevelType() {
        return levelType;
    }

    /**
     * Set level type
     *
     * @param value Level type
     */
    public void setLevelType(int value) {
        levelType = value;
    }

    /**
     * Get levels
     *
     * @return Levels
     */
    public List<Double> getLevels() {
        //return _levels;
        Dimension zDim = this.getZDimension();
        if (zDim == null) {
            return levels;
        } else {
            return zDim.getDimValueList();
        }
    }

    /**
     * Set levels
     *
     * @param value Levels
     */
    public void setLevels(List<Double> value) {
        levels = value;
        this.updateZDimension();
    }

    /**
     * Set units
     *
     * @return Units
     */
    public String getUnits() {
        return units;
    }

    /**
     * Set units
     *
     * @param value Units
     */
    public void setUnits(String value) {
        units = value;
    }

    /**
     * Get description
     *
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description
     *
     * @param value Description
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * Get dimension number
     *
     * @return Dimension number
     */
    public int getDimNumber() {
        return this.getDimensions().size();
    }

    /**
     * Get level number
     *
     * @return Level number
     */
    public int getLevelNum() {
        //return _levels.size();
        Dimension zDim = this.getZDimension();
        if (zDim == null) {
            return 0;
        } else {
            return zDim.getLength();
        }
    }

    /**
     * Get HDF path
     *
     * @return HDF path
     */
    public String getHDFPath() {
        return hdfPath;
    }

    /**
     * Set HDF path
     *
     * @param value HDF path
     */
    public void setHDFPath(String value) {
        hdfPath = value;
    }

    /**
     * Get X dimension
     *
     * @return X dimension
     */
    public Dimension getXDimension() {
        return getDimension(DimensionType.X);
    }

    /**
     * Set X dimension
     *
     * @param value X dimension
     */
    public void setXDimension(Dimension value) {
        setDimension(value, DimensionType.X);
    }

    /**
     * Get Y dimension
     *
     * @return Y dimension
     */
    public Dimension getYDimension() {
        return getDimension(DimensionType.Y);
    }

    /**
     * Set Y dimension
     *
     * @param value Y dimension
     */
    public void setYDimension(Dimension value) {
        setDimension(value, DimensionType.Y);
    }

    /**
     * Get Z dimension
     *
     * @return Z dimension
     */
    public Dimension getZDimension() {
        return getDimension(DimensionType.Z);
    }

    /**
     * Set Z dimension
     *
     * @param value Z dimension
     */
    public void setZDimension(Dimension value) {
        setDimension(value, DimensionType.Z);
    }

    /**
     * Get T dimension
     *
     * @return T dimension
     */
    public Dimension getTDimension() {
        return getDimension(DimensionType.T);
    }

    /**
     * Set T dimension
     *
     * @param value T dimension
     */
    public void setTDimension(Dimension value) {
        setDimension(value, DimensionType.T);
    }

    /**
     * Get dimension identifers
     *
     * @return Dimension identifers
     */
    public int[] getDimIds() {
        int[] dimids = new int[this.getDimensions().size()];
        for (int i = 0; i < this.getDimensions().size(); i++) {
            dimids[i] = ((Dimension) this.getDimension(i)).getDimId();
        }

        return dimids;
    }

    /**
     * Get if the variable is station data set
     *
     * @return Boolean
     */
    public boolean isStation() {
        return isStation;
    }

    /**
     * Set if the variable is station data set
     *
     * @param value Boolean
     */
    public void setStation(boolean value) {
        isStation = value;
    }

    /**
     * Get if the variable is swath data set
     *
     * @return Boolean
     */
    public boolean isSwath() {
        return isSwath;
    }

    /**
     * Set if the variable is swath data set
     *
     * @param value Boolean
     */
    public void setSwath(boolean value) {
        isSwath = value;
    }

    /**
     * Get if the variable is plottable (has both X and Y dimension)
     *
     * @return Boolean
     */
    public boolean isPlottable() {
        if (isStation) {
            return true;
        }
        if (this.getXDimension() == null) {
            return false;
        }
        if (this.getYDimension() == null) {
            return false;
        }

        return true;
    }

    /**
     * Get attribute number
     *
     * @return Attribute number
     */
    public int getAttNumber() {
        return this.getAttributes().size();
    }

    /**
     * Get variable identifer
     *
     * @return Variable identifer
     */
    public int getVarId() {
        return varId;
    }

    /**
     * Set variable identifer
     *
     * @param value Variable identifer
     */
    public void setVarId(int value) {
        varId = value;
    }

    /**
     * Get if the variable is dimension variable
     *
     * @return Boolean
     */
    public boolean isDimVar() {
        return dimVar;
    }

    /**
     * Set if the variable is dimension variable
     *
     * @param value Boolean
     */
    public void setDimVar(boolean value) {
        dimVar = value;
    }

    /**
     * Get if the variable is a member of a structure
     *
     * @return Is a member of a structure or not
     */
    public boolean isMemberOfStructure() {
        return this.memberOfStructure;
    }

    /**
     * Set if the variable is a member of a structure
     *
     * @param value Boolean
     */
    public void setMemberOfStructure(boolean value) {
        this.memberOfStructure = value;
    }

    /**
     * Get level index list - for ARL data
     *
     * @return Level index list
     */
    public List<Integer> getLevelIdxs() {
        return levelIdxs;
    }

    /**
     * Set level index list
     *
     * @param value Level index list
     */
    public void setLevelIdxs(List<Integer> value) {
        levelIdxs = value;
    }

    /**
     * Get variable index in level index list - for ARL data
     *
     * @return Variable index
     */
    public List<Integer> getVarInLevelIdxs() {
        return varInLevelIdxs;
    }

    /**
     * Set variable index in level index list - for ARL data
     *
     * @param value Variable index
     */
    public void setVarInLevelIdxs(List<Integer> value) {
        varInLevelIdxs = value;
    }

    /**
     * Get cached data array
     * @return Cached data array
     */
    public Array getCachedData() {
        return this.cachedData;
    }

    /**
     * Set cached data array
     * @param value Cached data array
     */
    public void setCachedData(Array value) {
        this.cachedData = value;
    }

    /**
     * Check has cached data or not
     * @return Has cached data or not
     */
    public boolean hasCachedData() {
        return this.cachedData != null;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Find an attribute by name
     * @param name Attribute name
     * @return Attribute
     */
    public Attribute findAttribute(String name) {
        for (Attribute a : this.attributes) {
            if (name.equals(a.getShortName())) {
                return a;
            }
        }
        return null;
    }

    /**
     * Find an attribute by name ignoring string case
     * @param name Attribute name
     * @return Attribute
     */
    public Attribute findAttributeIgnoreCase(String name) {
        for (Attribute a : this.attributes) {
            if (name.equalsIgnoreCase(a.getShortName())) {
                return a;
            }
        }
        return null;
    }

    /**
     * Clone
     *
     * @return Parameter object
     */
    @Override
    public Object clone() {
        Variable aPar = new Variable();
        aPar.Number = Number;
        aPar.setName(this.getName());
        aPar.setShortName(this.getShortName());
        aPar.setUnits(units);
        aPar.setDescription(description);
        aPar.setLevelType(levelType);

        aPar.getDimensions().addAll(this.getDimensions());
        aPar.setDimVar(dimVar);
        aPar.getLevels().addAll(levels);
        aPar.setVarId(varId);

        return aPar;
    }

    /**
     * Determine if two parameter are equal
     *
     * @param aVar The variable
     * @return If equal
     */
    public boolean equals(Variable aVar) {
        if (!this.getName().equals(aVar.getName())) {
            return false;
        }
        if (Number != aVar.Number) {
            return false;
        }
        if (!description.equals(aVar.getDescription())) {
            return false;
        }
        if (!units.equals(aVar.getUnits())) {
            return false;
        }

        return true;
    }

    /**
     * Determine if two parameter are totally equal
     *
     * @param aVar The variable
     * @return If equal
     */
    public boolean tEquals(Variable aVar) {
        if (!this.getName().equals(aVar.getName())) {
            return false;
        }
        if (Number != aVar.Number) {
            return false;
        }
        if (!description.equals(aVar.getDescription())) {
            return false;
        }
        if (!units.equals(aVar.getUnits())) {
            return false;
        }
        if (levelType != aVar.getLevelType()) {
            return false;
        }

        return true;
    }

    /**
     * Add a level
     *
     * @param levelValue Level value
     */
    public void addLevel(double levelValue) {
        if (!levels.contains(levelValue)) {
            levels.add(levelValue);
        }
    }

    /**
     * Get true level number
     *
     * @return True level number
     */
    public int getTrueLevelNumber() {
        if (getLevelNum() == 0) {
            return 1;
        } else {
            return getLevelNum();
        }
    }

    /**
     * Get dimension by type
     *
     * @param dimType Dimension type
     * @return Dimension
     */
    public Dimension getDimension(DimensionType dimType) {
        for (int i = 0; i < getDimNumber(); i++) {
            Dimension aDim = ((Dimension) this.getDimension(i));
            if (aDim.getDimType() == dimType) {
                return aDim;
            }
        }

        return null;
    }

    /**
     * Use when dimensions have changed, to recalculate the shape.
     */
    public void resetShape() {
        // if (immutable) throw new IllegalStateException("Cant modify");  LOOK allow this for unlimited dimension updating
        this.shape = new int[dimensions.size()];
        for (int i = 0; i < dimensions.size(); i++) {
            Dimension dim = dimensions.get(i);
            shape[i] = dim.getLength();
        }
    }

    /**
     * Set a dimension
     *
     * @param tstr Dimension type string
     * @param values Dimension values
     * @param reverse If is reverse
     */
    public void setDimension(String tstr, List<Number> values, boolean reverse) {
        DimensionType dType = DimensionType.OTHER;
        switch (tstr) {
            case "X":
                dType = DimensionType.X;
                break;
            case "Y":
                dType = DimensionType.Y;
                break;
            case "Z":
                dType = DimensionType.Z;
                break;
            case "T":
                dType = DimensionType.T;
                break;
        }
        Dimension dim = new Dimension("null", values.size(), dType);
        dim.setDimValues(values);
        dim.setReverse(reverse);
        this.setDimension(dim);
    }

    /**
     * Set a dimension
     *
     * @param tstr Dimension type string
     * @param values Dimension values
     * @param index Index
     * @param reverse If is reverse
     */
    public void setDimension(String tstr, List<Number> values, boolean reverse, int index) {
        DimensionType dType = DimensionType.OTHER;
        switch (tstr) {
            case "X":
                dType = DimensionType.X;
                break;
            case "Y":
                dType = DimensionType.Y;
                break;
            case "Z":
                dType = DimensionType.Z;
                break;
            case "T":
                dType = DimensionType.T;
                break;
        }
        Dimension dim = new Dimension("null", values.size(), dType);
        dim.setDimValues(values);
        dim.setReverse(reverse);
        this.setDimension(index, dim);
    }

    /**
     * Set dimension
     *
     * @param aDim The dimension
     */
    public void setDimension(Dimension aDim) {
        if (aDim == null) {
            return;
        }

        if (aDim.getDimType() == DimensionType.OTHER) {
            this.addDimension(aDim);
        } else {
            boolean hasDim = false;
            for (int i = 0; i < getDimNumber(); i++) {
                Dimension bDim = (Dimension) this.getDimension(i);
                if (bDim.getDimType() == aDim.getDimType()) {
                    this.setDimension(i, aDim);
                    hasDim = true;
                    break;
                }
            }

            if (!hasDim) {
                this.addDimension(aDim);
                this.resetShape();
            }
        }
    }

    /**
     * Set dimension
     *
     * @param aDim The dimension
     * @param idx Index
     */
    public void setDimension(int idx, Dimension aDim) {
        if (aDim == null) {
            return;
        }

        if (this.getDimNumber() > idx) {
            this.dimensions.set(idx, aDim);
        } else {
            this.dimensions.add(aDim);
        }
        this.resetShape();
    }

    /**
     * Set dimension by dimension type
     *
     * @param aDim The dimension
     * @param dimType Dimension type
     */
    public void setDimension(Dimension aDim, DimensionType dimType) {
        if (aDim.getDimType() == dimType) {
            setDimension(aDim);
        }
    }

    /**
     * Get index of a dimension
     *
     * @param aDim The dimension
     * @return Index
     */
    public int getDimIndex(Dimension aDim) {
        int idx = -1;
        for (int i = 0; i < getDimNumber(); i++) {
            if (aDim.equals(this.getDimension(i))) {
                idx = i;
                break;
            }
        }

        return idx;
    }

    /**
     * Get dimension length
     *
     * @param idx Dimension index
     * @return Dimension length
     */
    public int getDimLength(int idx) {
        return this.getDimension(idx).getLength();
    }

    /**
     * Determine if has Xtrack dimension
     *
     * @return Boolean
     */
    public boolean hasXtrackDimension() {
        boolean has = false;
        for (int i = 0; i < getDimNumber(); i++) {
            if (((Dimension) this.getDimension(i)).getDimType() == DimensionType.X_TRACK) {
                has = true;
                break;
            }
        }

        return has;
    }

    /**
     * Determine if the variable has a dimension
     *
     * @param dimId Dimension identifer
     * @return Boolean
     */
    public boolean hasDimension(int dimId) {
        for (int i = 0; i < this.getDimNumber(); i++) {
            Dimension aDim = (Dimension) this.getDimension(i);
            if (aDim.getDimId() == dimId) {
                return true;
            }
        }

        return false;
    }

    /**
     * If the variable has a null dimension
     *
     * @return Boolean
     */
    public boolean hasNullDimension() {
        for (int i = 0; i < this.getDimNumber(); i++) {
            Dimension aDim = (Dimension) this.getDimension(i);
            if (aDim == null) {
                return true;
            }
            if (aDim.getName() == null) {
                return true;
            }
            if (aDim.getName().equals("null")) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the dimensions equales with another variable
     *
     * @param var Another variable
     * @return Boolean
     */
    public boolean dimensionEquales(Variable var) {
        if (this.getDimNumber() != var.getDimNumber()) {
            return false;
        }
        for (int i = 0; i < this.getDimNumber(); i++) {
            Dimension adim = (Dimension) this.getDimension(i);
            Dimension bdim = (Dimension) var.getDimension(i);
            if (!adim.getShortName().equals(bdim.getShortName())) {
                return false;
            }
        }

        return true;
    }

    /**
     * If the dimensions size equales with another variable
     *
     * @param var Another variable
     * @return Boolean
     */
    public boolean dimensionSizeEquals(Variable var) {
        if (this.getDimNumber() != var.getDimNumber()) {
            return false;
        }

        for (int i = 0; i < this.getDimNumber(); i++) {
            Dimension adim = (Dimension) this.getDimension(i);
            Dimension bdim = (Dimension) var.getDimension(i);
            if (adim.getLength() != bdim.getLength()) {
                return false;
            }
        }

        return true;
    }

    /**
     * If the dimensions contains the diemsions of another variable
     *
     * @param var Another variable
     * @return Boolean
     */
    public boolean dimensionContains(Variable var) {
        if (this.getDimNumber() < var.getDimNumber()) {
            return false;
        }

        int sidx = 0;
        if (this.getDimNumber() > var.getDimNumber()) {
            sidx = this.getDimNumber() - var.getDimNumber();
        }
        for (int i = sidx; i < var.getDimNumber(); i++) {
            Dimension adim = (Dimension) this.getDimension(i);
            Dimension bdim = (Dimension) var.getDimension(i - sidx);
            if (adim.getLength() != bdim.getLength()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get level dimension for SWATH data variable
     *
     * @param var Variable
     * @return Dimension
     */
    public Dimension getLevelDimension(Variable var) {
        if (this.getDimNumber() > var.getDimNumber()) {
            for (int i = var.getDimNumber(); i < this.getDimNumber(); i++) {
                Dimension dim = (Dimension) this.getDimension(i);
                if (dim.getDimType() == DimensionType.OTHER) {
                    return dim;
                }
            }
        }

        return null;
    }

    /**
     * Get times
     *
     * @return Times
     */
    public List<LocalDateTime> getTimes() {
        Dimension tDim = this.getTDimension();
        if (tDim == null) {
            return null;
        }

        List<Double> values = tDim.getDimValueList();
        List<LocalDateTime> times = new ArrayList<>();
        for (Double v : values) {
            times.add(JDateUtil.fromOADate(v));
        }

        return times;
    }

    /**
     * Get attribute index by name, return -1 if the name not exist.
     *
     * @param attName Attribute name
     * @return Attribute index
     */
    public int getAttributeIndex(String attName) {
        int idx = -1;
        for (int i = 0; i < this.getAttributes().size(); i++) {
            if (this.getAttributes().get(i).getShortName().equalsIgnoreCase(attName)) {
                idx = i;
                break;
            }
        }

        return idx;
    }

    /**
     * Get attribute value string by name
     *
     * @param attName Attribute name
     * @return Attribute value string
     */
    public String getAttributeString(String attName) {
        String attStr = "";
        for (Attribute aAtt : this.getAttributes()) {
            if (aAtt.getShortName().equalsIgnoreCase(attName)) {
                attStr = aAtt.toString();
            }
        }

        return attStr;
    }

    /**
     * Add a dimension
     *
     * @param dim Dimension
     */
    public void addDimension(Dimension dim) {
        this.getDimensions().add(dim);
        this.resetShape();
    }

    /**
     * Add a dimension
     *
     * @param idx Index
     * @param dim Dimension
     */
    public void addDimension(int idx, Dimension dim) {
        this.getDimensions().add(idx, dim);
        this.resetShape();
    }

    /**
     * Add a dimension
     *
     * @param dType Dimension type
     * @param values Dimension values
     */
    public void addDimension(DimensionType dType, List<Number> values) {
        Dimension dim = new Dimension("null", values.size(), dType);
        dim.setDimValues(values);
        this.addDimension(dim);
    }

    /**
     * Add a dimension
     *
     * @param tstr Dimension type string
     * @param values Dimension values
     */
    public void addDimension(String tstr, List<Number> values) {
        DimensionType dType = DimensionType.OTHER;
        switch (tstr) {
            case "X":
                dType = DimensionType.X;
                break;
            case "Y":
                dType = DimensionType.Y;
                break;
            case "Z":
                dType = DimensionType.Z;
                break;
            case "T":
                dType = DimensionType.T;
                break;
        }
        Dimension dim = new Dimension("null", values.size(), dType);
        dim.setDimValues(values);
        this.addDimension(dim);
    }

    /**
     * Get stagger dimension index
     * @return Stagger dimension index
     */
    public int getStaggerDimIndex() {
        int i = 0;
        for (Dimension dim : this.dimensions) {
            if (dim.isStagger()) {
                return i;
            }
            i += 1;
        }

        return -1;
    }

    /**
     * Add an attribute
     *
     * @param attr Attribute
     */
    public void addAttribute(Attribute attr) {
        this.attributes.add(attr);
    }

    /**
     * Add attribute
     *
     * @param attName Attribute name
     * @param attValue Attribute value
     */
    public void addAttribute(String attName, List attValue) {
        Attribute aAtt = new Attribute(attName, attValue);;

        this.addAttribute(aAtt);
    }

    /**
     * Add attribute
     *
     * @param attName Attribute name
     * @param attValue Attribute value
     */
    public void addAttribute(String attName, String attValue) {
        Attribute aAtt = new Attribute(attName, attValue);

        this.addAttribute(aAtt);
    }

    /**
     * Add attribute
     *
     * @param attName Attribute name
     * @param attValue Attribute name
     */
    public void addAttribute(String attName, double attValue) {
        Attribute aAtt = new Attribute(attName, attValue);

        this.addAttribute(aAtt);
    }

    /**
     * Update z dimension from levels
     */
    public void updateZDimension() {
        if (levels.size() > 0) {
            Dimension zdim = new Dimension("null", 0, DimensionType.Z);
            zdim.setValues(levels);
            this.setZDimension(zdim);
        }
    }

    /**
     * Section dimensions
     * @param origin Origin
     * @param size Size
     * @param stride Stride
     * @return Section result dimensions
     * @throws InvalidRangeException
     */
    public List<Dimension> sectionDimensions(int[] origin, int[] size, int[] stride) throws InvalidRangeException {
        Section section = new Section(origin, size, stride);
        List<Dimension> dims = new ArrayList<>();
        for (int i = 0; i < section.getRank(); i++) {
            Range range = section.getRange(i);
            if (range.length() > 1) {
                Dimension dim = this.dimensions.get(i).extract(range);
                dims.add(dim);
            }
        }

        return dims;
    }

    /**
     * To string
     * @return String
     */
    @Override
    public String toString() {
        return this.name;
    }
    // </editor-fold>
}
