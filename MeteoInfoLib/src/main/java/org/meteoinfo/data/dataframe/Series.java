/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.math.ArrayMath;
import org.meteoinfo.data.dataframe.impl.Grouping;
import org.meteoinfo.data.dataframe.impl.KeyFunction;
import org.meteoinfo.data.dataframe.impl.TimeFunction;
import org.meteoinfo.data.dataframe.impl.TimeFunctions;
import org.meteoinfo.data.dataframe.impl.Views;
import org.meteoinfo.data.dataframe.impl.WindowFunction;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.Range;

/**
 *
 * @author Yaqiang Wang
 */
public class Series implements Iterable {

    // <editor-fold desc="Variables">
    private Index index;
    private Array data;    //One dimension array
    private String name;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     *
     * @param data Data array
     * @param index Index
     * @param name Name
     */
    public Series(Array data, Index index, String name) {
        this.data = data;
        this.index = index;
        this.name = name;
    }

    /**
     * Constructor
     *
     * @param data Data array
     * @param idxValue Index value
     * @param name Name
     */
    public Series(Array data, List idxValue, String name) {
        this(data, Index.factory(idxValue), name);
    }

    /**
     * Constructor
     *
     * @param data Data array
     * @param name name
     */
    public Series(Array data, String name) {
        this(data, new IntIndex((int) data.getSize()), name);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get data array
     *
     * @return Data array
     */
    public Array getData() {
        return this.data;
    }

    /**
     * Set data array
     *
     * @param value Data array
     */
    public void setData(Array value) {
        this.data = value;
    }

    /**
     * Get index
     *
     * @return Index
     */
    public Index getIndex() {
        return this.index;
    }

    /**
     * Set index
     *
     * @param value Index
     */
    public void setIndex(Index value) {
        this.index = value;
    }

    /**
     * Set index
     *
     * @param value Index value
     */
    public void setIndex(List value) {
        this.index = Index.factory(value);
    }

    /**
     * Get name
     *
     * @return Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set name
     *
     * @param value Name
     */
    public void setName(String value) {
        this.name = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get if the series contains no data
     *
     * @return Boolean
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Get a data value
     *
     * @param i Index
     * @return Data value
     */
    public Object getValue(int i) {
        return this.data.getObject(i);
    }

    /**
     * Get value by index
     *
     * @param idxValue Index value
     * @return Data value
     */
    public Object getValueByIndex(Object idxValue) {
        Object[] rii = this.index.getIndices(idxValue);
        List<Integer> ii = (List<Integer>) rii[0];
        List rIndex = (List) rii[1];
        if (ii.size() == 1) {
            if (ii.get(0) >= 0) {
                return this.data.getObject(ii.get(0));
            } else {
                return Double.NaN;
            }
        } else {
            Array ra = Array.factory(this.data.getDataType(), new int[]{ii.size()});
            for (int i = 0; i < ii.size(); i++) {
                if (ii.get(i) < 0) {
                    if (ra.getDataType().isNumeric()) {
                        ra.setObject(i, Double.NaN);
                    }
                } else {
                    ra.setObject(i, this.data.getObject(ii.get(i)));
                }
            }
            Index idx;
            if (this.index instanceof DateTimeIndex && !(idxValue instanceof LocalDateTime)) {
                List<LocalDateTime> values = new ArrayList<>();
                for (String v : (List<String>) rIndex) {
                    values.add(JDateUtil.getDateTime(v));
                }
                idx = Index.factory(values);
            } else {
                idx = Index.factory(rIndex);
            }
            idx.format = this.index.format;
            return new Series(ra, idx, this.name);
        }
    }

    /**
     * Set a data value
     *
     * @param i Index
     * @param v Data value
     */
    public void setValue(int i, Object v) {
        this.data.setObject(i, v);
    }

    /**
     * Set data values by another boolean series
     *
     * @param s Boolean series
     * @param v Data value
     */
    public void setValue(Series s, Object v) {
        for (int i = 0; i < this.size(); i++) {
            if ((boolean) s.getValue(i)) {
                this.data.setObject(i, v);
            }
        }
    }

    /**
     * Get values
     *
     * @param ii index values
     * @return Result series
     */
    public Series getValues(List<Integer> ii) {
        Array ra = Array.factory(this.data.getDataType(), new int[]{ii.size()});
        for (int i = 0; i < ii.size(); i++) {
            ra.setObject(i, this.data.getObject(ii.get(i)));
        }
        Index idx = this.index.subIndex(ii);
        return new Series(ra, idx, this.name);
    }

    /**
     * Get values
     *
     * @param range Range
     * @return Result series
     */
    public Series getValues(Range range) {
        Array ra = Array.factory(this.data.getDataType(), new int[]{range.length()});
        int i = 0;
        for (int ii = range.first(); ii <= range.last(); ii += range.stride()) {
            ra.setObject(i, this.data.getObject(ii));
            i += 1;
        }
        Index idx = this.index.subIndex(range.first(), range.last() + 1, range.stride());
        return new Series(ra, idx, this.name);
    }

    /**
     * Get values by index
     *
     * @param idxValues index values
     * @return Result series
     */
    public Series getValueByIndex(List idxValues) {
        Object[] rii = this.index.getIndices(idxValues);
        List<Integer> ii = (List<Integer>) rii[2];
        List rIndex = (List) rii[3];
        Array ra = Array.factory(this.data.getDataType(), new int[]{ii.size()});
        for (int i = 0; i < ii.size(); i++) {
            if (ii.get(i) < 0) {
                if (ra.getDataType().isNumeric()) {
                    ra.setObject(i, Double.NaN);
                }
            } else {
                ra.setObject(i, this.data.getObject(ii.get(i)));
            }
        }
        Index idx;
        if (this.index instanceof DateTimeIndex && (rIndex.get(0) instanceof String)) {
            List<LocalDateTime> values = new ArrayList<>();
            for (String v : (List<String>) rIndex) {
                values.add(JDateUtil.getDateTime(v));
            }
            idx = Index.factory(values);
        } else {
            idx = Index.factory(rIndex);
        }
        idx.format = this.index.format;
//        if (idx instanceof DateTimeIndex) {
//            ((DateTimeIndex) idx).setDateTimeFormatter(((DateTimeIndex) this.index).getDateTimeFormatter());
//        }
        return new Series(ra, idx, this.name);
    }
    
    /**
     * Get values by index
     *
     * @param idxValues index values
     * @return Result series
     */
    public Series getValueByIndex_bak(List idxValues) {
        Object[] rii = this.index.getIndices(idxValues);
        List<Integer> ii = (List<Integer>) rii[0];
        List rIndex = (List) rii[1];
        Array ra = Array.factory(this.data.getDataType(), new int[]{ii.size()});
        for (int i = 0; i < ii.size(); i++) {
            if (ii.get(i) < 0) {
                if (ra.getDataType().isNumeric()) {
                    ra.setObject(i, Double.NaN);
                }
            } else {
                ra.setObject(i, this.data.getObject(ii.get(i)));
            }
        }
        Index idx;
        if (this.index instanceof DateTimeIndex && (rIndex.get(0) instanceof String)) {
            List<LocalDateTime> values = new ArrayList<>();
            for (String v : (List<String>) rIndex) {
                values.add(JDateUtil.getDateTime(v));
            }
            idx = Index.factory(values);
        } else {
            idx = Index.factory(rIndex);
        }
        idx.format = this.index.format;
//        if (idx instanceof DateTimeIndex) {
//            ((DateTimeIndex) idx).setDateTimeFormatter(((DateTimeIndex) this.index).getDateTimeFormatter());
//        }
        return new Series(ra, idx, this.name);
    }

    /**
     * Get a index value
     *
     * @param i Index
     * @return Index value
     */
    public Object getIndexValue(int i) {
        return this.index.get(i);
    }

    @Override
    public Iterator iterator() {
        return iterrows();
    }

    public ListIterator<List<Object>> iterrows() {
        return new Views.ListView<>(this).listIterator();
    }

    /**
     * Get size
     *
     * @return Size
     */
    public int size() {
        return this.index.size();
    }

    /**
     * Group the series rows using the specified key function.
     *
     * @param function the function to reduce rows to grouping keys
     * @return the grouping
     */
    public SeriesGroupBy groupBy(final KeyFunction function) {
        return new SeriesGroupBy(new Grouping(this, function), this);
    }

    /**
     * Group the series rows using the specified key function.
     *
     * @return the grouping
     */
    public SeriesGroupBy groupBy() {
        return new SeriesGroupBy(new Grouping(this), this);
    }

    /**
     * Group the series rows using the specified time function.
     *
     * @param function the function to reduce rows to grouping keys
     * @return the grouping
     */
    public SeriesGroupBy groupBy(final TimeFunction function) {
        return new SeriesGroupBy(new Grouping(this, function), this);
    }

    /**
     * Group by time string - DateTimeIndex
     *
     * @param tStr Time string
     * @return The grouping
     */
    public SeriesGroupBy groupBy(String tStr) {
        TimeFunction function = TimeFunctions.factory(tStr);
        if (function == null) {
            return null;
        } else {
            return groupBy(function);
        }
    }

    /**
     * Group the series rows using the specified key function.
     *
     * @param function the function to reduce rows to grouping keys
     * @return the grouping
     */
    public SeriesGroupBy resample(final WindowFunction function) {
        ((DateTimeIndex) index).setResamplPeriod(function.getPeriod());
        return new SeriesGroupBy(new Grouping(this, function), this);
    }

    /**
     * Group the series rows using the specified key function.
     *
     * @param pStr Period string
     * @return the grouping
     */
    public SeriesGroupBy resample(final String pStr) {
        TemporalAmount period = JDateUtil.getPeriod(pStr);
        WindowFunction function = new WindowFunction(period);
        return resample(function);
    }

    /**
     * Compute the mean of the numeric columns for each group or the entire series
     * if the data is not grouped.
     *
     * @return Mean object
     */
    public Object mean() {
        return ArrayMath.mean(data);
    }

    /**
     * Compute the maximum of the numeric columns for each group or the entire
     * series if the data is not grouped.
     *
     * @return Maximum object
     */
    public Object max() {
        return ArrayMath.max(data);
    }

    /**
     * Compute the minimum of the numeric columns for each group or the entire
     * series if the data is not grouped.
     *
     * @return Minimum object
     */
    public Object min() {
        return ArrayMath.min(data);
    }
    
    /**
     * Compute the standard deviation of the numeric columns for each group or the entire
     * series if the data is not grouped.
     *
     * @return Minimum object
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object stdDev() throws InvalidRangeException {
        return ArrayMath.std(data);
    }

    /**
     * Equal
     *
     * @param v Value
     * @return Result series
     */
    public Series equal(Number v) {
        Array rdata = ArrayMath.equal(data, v);
        Series r = new Series(rdata, index, name);
        return r;
    }

    /**
     * Equal
     *
     * @param v Value
     * @return Result series
     */
    public Series equal(String v) {
        Array rdata = ArrayMath.equal(data, v);
        Series r = new Series(rdata, index, name);
        return r;
    }

    /**
     * Less then
     *
     * @param v Value
     * @return Result series
     */
    public Series lessThan(Number v) {
        Array rdata = ArrayMath.lessThan(data, v);
        Series r = new Series(rdata, index, name);
        return r;
    }

    /**
     * Less then or equal
     *
     * @param v Value
     * @return Result series
     */
    public Series lessThanOrEqual(Number v) {
        Array rdata = ArrayMath.lessThanOrEqual(data, v);
        Series r = new Series(rdata, index, name);
        return r;
    }

    /**
     * Greater then
     *
     * @param v Value
     * @return Result series
     */
    public Series greaterThan(Number v) {
        Array rdata = ArrayMath.greaterThan(data, v);
        Series r = new Series(rdata, index, name);
        return r;
    }

    /**
     * Greater then or equal
     *
     * @param v Value
     * @return Result series
     */
    public Series greaterThanOrEqual(Number v) {
        Array rdata = ArrayMath.greaterThanOrEqual(data, v);
        Series r = new Series(rdata, index, name);
        return r;
    }

    /**
     * Convert to string - head
     *
     * @param n Head row number
     * @return The string
     */
    public String head(int n) {
        StringBuilder sb = new StringBuilder();
        int rn = this.index.size();
        if (n > rn) {
            n = rn;
        }
        for (int r = 0; r < n; r++) {
            sb.append(this.index.toString(r));
            sb.append("  ");
            sb.append(this.data.getObject(r).toString());
            sb.append("\n");
        }
        if (n < rn) {
            sb.append("...");
        }

        return sb.toString();
    }

    /**
     * Convert to string - tail
     *
     * @param n Tail row number
     * @return The string
     */
    public String tail(int n) {
        StringBuilder sb = new StringBuilder();
        int rn = this.index.size();
        if (n > rn) {
            n = rn;
        }
        for (int r = rn - n; r < rn; r++) {
            sb.append(this.index.toString(r));
            sb.append("  ");
            sb.append(this.data.getObject(r).toString());
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return head(100);
    }

    /**
     * Save as CSV file
     *
     * @param fileName File name
     * @param delimiter Delimiter
     * @param dateFormat Date format string
     * @param floatFormat Float format string
     * @param index If write index
     * @throws java.io.IOException
     */
    public void saveCSV(String fileName, String delimiter, String dateFormat, String floatFormat,
                        boolean index) throws IOException {
        BufferedWriter sw = new BufferedWriter(new FileWriter(new File(fileName)));
        String str = "";
        String idxFormat = this.index.format;
        if (index) {
            str = this.index.getName();
            if (dateFormat != null && (this.index instanceof DateTimeIndex)) {
                idxFormat = dateFormat;
            }
        }
        if (str.isEmpty()) {
            str = this.name;
        } else {
            str = str + delimiter + this.name;
        }
        sw.write(str);

        String line, vstr;
        String format;
        DataType dtype = this.data.getDataType();
        Column col = new Column(this.name, dtype);
        if (dtype == DataType.FLOAT || dtype == DataType.DOUBLE) {
            format = floatFormat == null ? col.getFormat() : floatFormat;
        } else {
            format = col.getFormat();
        }
        for (int i = 0; i < this.data.getSize(); i++) {
            line = "";
            if (index) {
                line = this.index.toString(i, idxFormat).trim();
            }
            if (format == null) {
                vstr = this.getValue(i).toString();
            } else {
                vstr = String.format(format, this.getValue(i));
            }
            if (line.isEmpty()) {
                line = vstr;
            } else {
                line += delimiter + vstr;
            }
            sw.newLine();
            sw.write(line);
        }
        sw.flush();
        sw.close();
    }
    // </editor-fold>
}
