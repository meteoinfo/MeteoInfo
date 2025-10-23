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
package org.meteoinfo.data.dimarray;

import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.ndarray.util.BigDecimalUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class Dimension {
    // <editor-fold desc="Variables">

    private String name;
    private DimensionType dimType;
    private Array dimValue;
    private int dimId;
    private boolean unlimited = false;
    private boolean variableLength = false;
    private boolean shared = true;
    private boolean reverse = false;
    private String unit = "null";
    private boolean stagger = false;

    /**
     * Constructor
     */
    public Dimension() {
        this("null", 1);
    }

    /**
     * Constructor
     * @param len Length
     */
    public Dimension(int len) {
        this("null", len);
    }

    /**
     * Constructor
     *
     * @param name Name
     * @param len Length
     */
    public Dimension(String name, int len) {
        this.name = name;
        dimType = DimensionType.OTHER;
        dimValue = ArrayUtil.arrayRange(0, len, 1);
    }

    /**
     * Constructor
     *
     * @param dimType Dimension type
     */
    public Dimension(DimensionType dimType) {
        this(dimType.toString(), 1, dimType);
    }

    /**
     * Constructor
     *
     * @param name Name
     * @param dimValue Dimension values
     * @param dimType Dimension type
     */
    public Dimension(String name, Array dimValue, DimensionType dimType) {
        this.name = name;
        this.dimType = dimType;
        this.dimValue = dimValue.copyIfView();
    }

    /**
     * Constructor
     *
     * @param name Name
     * @param dimValue Dimension values
     */
    public Dimension(String name, Array dimValue) {
        this(name, dimValue, DimensionType.OTHER);
    }

    /**
     * Constructor
     *
     * @param name Name
     * @param dimType Dimension type
     */
    public Dimension(String name, DimensionType dimType) {
        this.name = name;
        this.dimType = dimType;
    }

    /**
     * Constructor
     *
     * @param name Name
     * @param len Length
     * @param dimType Dimension type
     */
    public Dimension(String name, int len, DimensionType dimType) {
        this(name, len);
        this.dimType = dimType;
    }

    /**
     * Constructor
     *
     * @param name Name
     * @param len Length
     * @param dimType Dimension type
     * @param min Minimum value
     * @param delta Delta value
     */
    public Dimension(String name, int len, DimensionType dimType, double min, double delta) {
        this(name, len);
        this.dimType = dimType;
        dimValue = ArrayUtil.arrayRange1(min, len, delta);
    }

    /**
     * Constructor
     * @param dimension Other dimension
     */
    public Dimension(Dimension dimension) {
        this.name = dimension.getName();
        this.dimId = dimension.getDimId();
        this.unit = dimension.getUnit();
        this.dimType = dimension.getDimType();
        this.stagger = dimension.isStagger();
        this.unlimited = dimension.isUnlimited();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get short name
     * @return Short name
     */
    public String getShortName() {
        return this.name;
    }
    
    /**
     * Set short name
     * @param value Short name
     */
    public void setShortName(String value) {
        this.name = value;
    }
    
    /**
     * Get short name
     * @return Short name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Set short name
     * @param value Short name
     */
    public void setName(String value) {
        this.name = value;
    }
    
    /**
     * Get length
     * @return Length
     */
    public int getLength() {
        return (int) this.dimValue.getSize();
    }
   
    /**
     * Set dimension length
     *
     * @param value Dimension length
     */
    public void setLength(int value) {
        if (value <= 0)
            return;

        if (this.dimValue == null || this.dimValue.getSize() != value) {
            this.dimValue = ArrayUtil.arrayRange(0, value, 1);
        }
    }

    /**
     * Get dimension type
     *
     * @return Dimension type
     */
    public DimensionType getDimType() {
        return dimType;
    }

    /**
     * Set dimension type
     *
     * @param value Dimension type
     */
    public void setDimType(DimensionType value) {
        this.dimType = value;
    }

    /**
     * Get dimension values
     *
     * @return Dimension values
     */
    public Array getDimValue() {
        return dimValue;
    }

    /**
     * Set dimension values
     * @param value Dimension values
     */
    public void setDimValue(Array value) {
        this.dimValue = value.copyIfView();
    }
    
    /**
     * Get dimension value by index
     * @param idx index
     * @return Dimension value
     */
    public double getDimValue(int idx) {
        return this.dimValue.getDouble(idx);
    }

    /**
     * Get dimension value list
     * @return Value list
     */
    public List<Double> getDimValueList() {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < this.dimValue.getSize(); i++) {
            values.add(this.dimValue.getDouble(i));
        }

        return values;
    }

    /**
     * Get dimension identifier
     *
     * @return Dimension identifier
     */
    public int getDimId() {
        return dimId;
    }

    public void setDimId(int value) {
        dimId = value;
    }

    /**
     * Get if values are reverse (in descending order)
     *
     * @return Boolean
     */
    public boolean isReverse() {
        return this.reverse;
    }

    /**
     * Set if values are reverse
     *
     * @param value Boolean
     */
    public void setReverse(boolean value) {
        this.reverse = value;
    }
    
    /**
     * Get is unlimited or not
     * @return Boolean
     */
    public boolean isUnlimited() {
        return this.unlimited;
    }
    
    /**
     * Set unlimited or not
     * @param value Boolean
     */
    public void setUnlimited(boolean value) {
        this.unlimited = value;
    }
    
    /**
     * Get is shared or not
     * @return Boolean
     */
    public boolean isShared() {
        return this.shared;
    }
    
    /**
     * Set is shared or not
     * @param value Boolean
     */
    public void setShared(boolean value) {
        this.shared = value;
    }
    
    /**
     * Get is variable length or not
     * @return Boolean
     */
    public boolean isVariableLength() {
        return this.variableLength;
    }
    
    /**
     * Set is variable length or not
     * @param value 
     */
    public void setVariableLength(boolean value) {
        this.variableLength = value;
    }

    /**
     * Get unit string
     * @return Unit string
     */
    public String getUnit() {
        return this.unit;
    }

    /**
     * Set unit string
     * @param value Unit string
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Get whether is stagger dimension
     * @return Whether is stagger dimension
     */
    public boolean isStagger() {
        return this.stagger;
    }

    /**
     * Set whether is stagger dimension
     * @param value Whether is stagger dimension
     */
    public void setStagger(boolean value) {
        this.stagger = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Determine if two dimensions equals
     *
     * @param aDim The other dimension
     * @return If equals
     */
    public boolean equals(Dimension aDim) {
        if (!this.getShortName().equals(aDim.getShortName())) {
            return false;
        }
        if (dimType != aDim.getDimType()) {
            return false;
        }
        return this.getLength() == aDim.getLength();
    }

    /**
     * Get dimension value array
     *
     * @return Value array
     */
    public double[] getValues() {
        int len = this.getLength();
        double[] values = new double[len];
        for (int i = 0; i < len; i++) {
            values[i] = dimValue.getDouble(i);
        }

        return values;
    }

    /**
     * Set dimension values
     *
     * @param values Values
     */
    public void setValues(List<Double> values) {
        dimValue = ArrayUtil.array(values);
    }

    /**
     * Set dimension values
     *
     * @param values Values
     */
    public void setDimValues(List<Number> values) {
        dimValue = ArrayUtil.array(values);
    }

    /**
     * Set dimension value
     * @param v Dimension value
     */
    public void setValue(LocalDateTime v) {
        dimValue = Array.factory(DataType.DATE, new int[]{1});
        dimValue.setDate(0, v);
    }

    /**
     * Set dimension value
     * @param v Dimension value
     */
    public void setValue(double v) {
        dimValue = Array.factory(DataType.DOUBLE, new int[]{1});
        dimValue.setDouble(0, v);
    }

    /**
     * Set dimension values
     *
     * @param values Values
     */
    public void setValues(double[] values) {
        dimValue = Array.factory(DataType.DOUBLE, new int[]{values.length}, values);
    }

    /**
     * Set dimension values
     *
     * @param values Values
     */
    public void setValues(float[] values) {
        dimValue = Array.factory(DataType.FLOAT, new int[]{values.length}, values);
    }

    /**
     * Add a dimension value
     *
     * @param value The value
     *//*
    public void addValue(double value) {
        dimValue.add(value);
        this.setLength(dimValue.size());
    }*/

    /**
     * Get minimum dimension value
     *
     * @return Minimum dimension value
     */
    public double getMinValue() {
        return dimValue.getDouble(0);
    }

    /**
     * Get maximum dimension value
     *
     * @return Maximum dimension value
     */
    public double getMaxValue() {
        return dimValue.getDouble((int)dimValue.getSize() - 1);
    }

    /**
     * Get delta value
     *
     * @return Delta value
     */
    public double getDeltaValue() {
        if (dimValue.getSize() <= 1) {
            return 1;
        }

        return BigDecimalUtil.sub(dimValue.getDouble(1), dimValue.getDouble(0));
    }

    /**
     * Flip the dimension data array
     */
    public void flip() {
        this.dimValue = this.dimValue.flip(0).copy();
    }
    
    /**
     * Extract dimension
     * @param range The range
     * @return Result dimension
     */
    public Dimension extract(Range range) {
        List<Range> ranges = new ArrayList<>();
        ranges.add(range);
        try {
            Array a = this.dimValue.section(ranges);
            Dimension dim = new Dimension(this.getShortName(), a, this.dimType);
            dim.setDimId(this.dimId);
            dim.setUnit(this.unit);
            dim.setStagger(this.stagger);
            return dim;
        } catch (InvalidRangeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract dimension
     *
     * @param first First
     * @param last Last
     * @param stride Stride
     * @return Extracted dimension
     */
    public Dimension extract(int first, int last, int stride) {
        int n = (last - first) / stride + 1;
        Dimension dim = new Dimension(this.getShortName(), n, this.dimType);
        dim.setDimId(this.dimId);
        dim.setUnit(this.unit);
        dim.setStagger(this.stagger);
        //dim.setReverse(this.reverse);
        if (this.dimValue.getSize() > last) {
            List values = new ArrayList<>();
            if (first <= last) {
                if (stride > 0) {
                    for (int i = first; i <= last; i += stride) {
                        values.add(this.dimValue.getObject(i));
                    }
                } else {
                    for (int i = last; i >= first; i += stride) {
                        values.add(this.dimValue.getObject(i));
                    }
                }
            } else {
                if (stride > 0) {
                    for (int i = last; i <= first; i += stride) {
                        values.add(this.dimValue.getObject(i));
                    }
                } else {
                    for (int i = first; i >= last; i += stride) {
                        values.add(this.dimValue.getObject(i));
                    }
                }
            }
            Array array = Array.factory(this.dimValue.getDataType(), new int[]{values.size()});
            for (int i = 0; i < values.size(); i++) {
                array.setObject(i, values.get(i));
            }
            dim.setDimValue(array);
        }

        return dim;
    }

    /**
     * Extract dimension
     *
     * @param first First
     * @param last Last
     * @param stride Stride
     * @return Extracted dimension
     */
    public Dimension extract(double first, double last, double stride) {
        Dimension dim = new Dimension(this.getShortName(), this.getLength(), this.dimType);
        dim.setDimId(this.dimId);
        dim.setUnit(this.unit);
        dim.setStagger(this.stagger);
        List values = new ArrayList<>();
        int idx;
        for (double v = first; v <= last; v += stride) {
            idx = this.getValueIndex(v);
            values.add(this.dimValue.getObject(idx));
        }
        Array array = Array.factory(this.dimValue.getDataType(), new int[]{values.size()});
        for (int i = 0; i < values.size(); i++) {
            array.setObject(i, values.get(i));
        }
        dim.setDimValue(array);

        return dim;
    }
    
    /**
     * Extract dimension
     *
     * @param index Indices
     * @return Extracted dimension
     */
    public Dimension extract(List<Integer> index) {
        Dimension dim = new Dimension(this.getShortName(), this.getLength(), this.dimType);
        dim.setDimId(this.dimId);
        dim.setUnit(this.unit);
        dim.setStagger(this.stagger);
        Array array = Array.factory(this.dimValue.getDataType(), new int[]{index.size()});
        for (int i = 0; i < index.size(); i++) {
            array.setObject(i, this.dimValue.getObject(index.get(i)));
        }
        dim.setDimValue(array);

        return dim;
    }

    /**
     * Extract dimension
     *
     * @param index Indices
     * @return Extracted dimension
     */
    public Dimension extract(Array index) {
        Dimension dim = new Dimension(this.getShortName(), this.getLength(), this.dimType);
        dim.setDimId(this.dimId);
        dim.setUnit(this.unit);
        dim.setStagger(this.stagger);
        Array values = Array.factory(this.dimValue.getDataType(), new int[]{(int) index.getSize()});
        IndexIterator iter = index.getIndexIterator();
        IndexIterator iterV = values.getIndexIterator();
        while (iter.hasNext()) {
            iterV.setObjectNext(this.dimValue.getObject(iter.getIntNext()));
        }
        dim.setDimValue(values);

        return dim;
    }

    /**
     * Get value index
     *
     * @param v Value
     * @return Index
     */
    public int getValueIndex(double v) {
        int idx = ArrayMath.asList(this.dimValue).indexOf(v);
        if (idx < 0) {
            idx = this.getLength() - 1;
            if (getDeltaValue() > 0) {
                for (int i = 0; i < this.getLength(); i++) {
                    if (v <= this.dimValue.getDouble(i)) {
                        if (i == 0)
                            idx = 0;
                        else {
                            if (this.dimValue.getDouble(i) - v > v - this.dimValue.getDouble(i - 1))
                                idx = i - 1;
                            else
                                idx = i;
                        }
                        break;
                    }
                }
            } else {
                for (int i = 0; i < this.getLength(); i++) {
                    if (v >= this.dimValue.getDouble(i)) {
                        if (i == 0)
                            idx = 0;
                        else {
                            if (this.dimValue.getDouble(i - 1) - v > v - this.dimValue.getDouble(i))
                                idx = i;
                            else
                                idx = i - 1;
                        }
                        break;
                    }
                }
            }
        }

        return idx;
    }

    /**
     * Get whether the dimension values are ascending
     * @return Ascending or not
     */
    public boolean isAscending() {
        for (int i = 0; i < this.dimValue.getSize() - 1; i++) {
            if (dimValue.getDouble(i) >= dimValue.getDouble(i + 1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get whether the dimension values are descending
     * @return Descending or not
     */
    public boolean isDescending() {
        for (int i = 0; i < this.dimValue.getSize() - 1; i++) {
            if (dimValue.getDouble(i) <= dimValue.getDouble(i + 1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get whether the dimension values are ordered
     * @return Ordered or not
     */
    public boolean isOrdered() {
        return isAscending() || isDescending();
    }

    /**
     * Reverse the dimension values
     */
    public void reverse() {
        this.dimValue = this.dimValue.flip(0).copy();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(this.getShortName());
        sb.append("\n");
        sb.append("Min value: ").append(String.valueOf(this.getMinValue()));
        sb.append("\n");
        sb.append("Max value: ").append(String.valueOf(this.getMaxValue()));
        sb.append("\n");
        sb.append("Size: ").append(String.valueOf(this.getLength()));
        sb.append("\n");
        sb.append("Delta: ").append(String.valueOf(this.getDeltaValue()));
        sb.append("\n");
        sb.append("Unit: ").append(this.unit);

        return sb.toString();
    }
    // </editor-fold>
}
