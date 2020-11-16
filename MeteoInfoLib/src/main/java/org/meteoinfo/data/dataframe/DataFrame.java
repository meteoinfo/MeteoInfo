/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.math.ArrayMath;
import org.meteoinfo.math.ArrayUtil;
import org.meteoinfo.data.dataframe.impl.Aggregation;
import org.meteoinfo.data.dataframe.impl.Function;
import org.meteoinfo.data.dataframe.impl.Grouping;
import org.meteoinfo.data.dataframe.impl.KeyFunction;
import org.meteoinfo.data.dataframe.impl.SortDirection;
import org.meteoinfo.data.dataframe.impl.Sorting;
import org.meteoinfo.data.dataframe.impl.Views;
import org.meteoinfo.data.dataframe.impl.WindowFunction;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.global.util.TypeUtils;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.Range;
import org.meteoinfo.ndarray.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class DataFrame implements Iterable {

    // <editor-fold desc="Variables">
    private Index index;
    private ColumnIndex columns;
    private Object data;    //Two dimension array or array list
    private boolean array2D = false;
    //private Range rowRange;
    //private Range colRange;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public DataFrame() {
        this.columns = new ColumnIndex();
    }

    /**
     * Constructor
     *
     * @param columns Columns
     */
    public DataFrame(ColumnIndex columns) {
        this.columns = columns;
    }

    /**
     * Constructor
     *
     * @param index Index
     */
    public DataFrame(Index index) {
        this();
        this.index = index;
    }

    /**
     * Constructor
     *
     * @param index Index
     */
    public DataFrame(List index) {
        this(Index.factory(index));
    }

    /**
     * Constructor
     *
     * @param data Data array
     * @param columns Columns
     * @param index Index
     */
    public DataFrame(Array data, Index index, ColumnIndex columns) {
        this(index, columns, data);
    }

    /**
     * Constructor
     *
     * @param data Data array
     * @param columns Columns
     * @param index Index
     */
    public DataFrame(Array data, Index index, List<String> columns) {
        this(index, columns, data);
    }

    /**
     * Constructor
     *
     * @param index Index
     * @param columns Columns
     * @param data Data
     */
    public DataFrame(Index index, ColumnIndex columns, Object data) {
        if (data instanceof Array) {
            if (((Array) data).getRank() == 1) {    //One dimension array
                if (columns.size() == 1) {
                    this.data = new ArrayList<>();
                    ((List) this.data).add(data);
                } else {
                    if (((Array) data).getSize() == columns.size()) {
                        this.data = ((Array) data).reshape(new int[]{1, columns.size()});
                        this.array2D = true;
                    }
                }
            } else {   //Two dimension array
                this.data = data;
                this.array2D = true;
            }
        } else {
            this.data = data;
        }

        this.columns = columns;
        this.index = index;
    }

    /**
     * Constructor
     *
     * @param index Index
     * @param columns Columns
     * @param data Data
     */
    public DataFrame(Index index, List<String> columns, Object data) {
        this.columns = new ColumnIndex();
        if (data instanceof Array) {
            List<DataType> dtypes = new ArrayList<>();
            if (((Array) data).getRank() == 1) {    //One dimension array
                this.data = new ArrayList<>();
                ((List) this.data).add(data);
                String colName;
                if (columns == null) {
                    colName = "C_1";
                } else {
                    colName = columns.get(0);
                }
                this.columns.add(Column.factory(colName, (Array) data));
            } else {   //Two dimension array
                this.data = data;
                this.array2D = true;
                int n = ((Array) data).getShape()[1];
                if (columns == null) {
                    columns = new ArrayList<>();
                    for (int i = 0; i < n; i++) {
                        columns.add("C_" + String.valueOf(i));
                    }
                }
                for (int i = 0; i < n; i++) {
                    dtypes.add(((Array) data).getDataType());
                    this.columns.add(new Column(columns.get(i), dtypes.get(i)));
                }
            }
        } else {
            this.data = data;
            int n = ((List) data).size();
            if (columns == null) {
                columns = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    columns.add("C_" + String.valueOf(i));
                }
            }
            for (int i = 0; i < n; i++) {
                this.columns.add(new Column(columns.get(i), ((List<Array>) data).get(i).getDataType()));
            }
        }

        this.index = index;
    }

    /**
     * Constructor
     *
     * @param data Data array
     * @param columns Columns
     * @param index Index
     */
    public DataFrame(Array data, List index, List<String> columns) {
        this(data, Index.factory(index), columns);
    }

    /**
     * Constructor
     *
     * @param data Data array list
     * @param columns Columns
     * @param index Index
     */
    public DataFrame(List<Array> data, Index index, ColumnIndex columns) {
        this(index, columns, data);
    }

    /**
     * Constructor
     *
     * @param data Data array list
     * @param columns Columns
     * @param index Index
     */
    public DataFrame(List<Array> data, Index index, List<String> columns) {
        this(index, columns, data);
    }

    /**
     * Constructor
     *
     * @param data Data array list
     * @param columns Columns
     * @param index Index
     */
    public DataFrame(List<Array> data, List index, List columns) {
        this(data, Index.factory(index), columns);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get data array
     *
     * @return Data array
     */
    public Object getData() {
        return this.data;
    }

    /**
     * Set data array
     *
     * @param value Data array
     */
    public void setData(Array value) {
        if (value.getRank() == 1) {    //One dimension array
            this.data = new ArrayList<>();
            ((List) this.data).add(value);
        } else {   //Two dimension array
            this.data = value;
            this.array2D = true;
        }
    }

    /**
     * Set data array
     *
     * @param value Data array
     */
    public void setData(List<Array> value) {
        this.data = value;
        this.array2D = false;
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
     * Get columns
     *
     * @return Columns
     */
    public ColumnIndex getColumns() {
        return this.columns;
    }

    /**
     * Get column names
     *
     * @return Column names
     */
    public List<String> getColumnNames() {
        return this.columns.getNames();
    }

    /**
     * Get column data types
     *
     * @return Column data types
     */
    public List<DataType> getColumnDataTypes() {
        return this.columns.getDataTypes();
    }

    /**
     * Set columns
     *
     * @param value Columns
     */
    public void setColumns(ColumnIndex value) {
        this.columns = value;
    }

    /**
     * Set column names
     *
     * @param colNames Column names
     */
    public void setColumns(List<String> colNames) {
        for (int i = 0; i < this.columns.size(); i++) {
            if (i < colNames.size()) {
                this.columns.get(i).setName(colNames.get(i));
            }
        }
    }

    /**
     * Get if is 2D array
     *
     * @return Boolean
     */
    public boolean isArray2D() {
        return this.array2D;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public Iterator iterator() {
        return iterrows();
    }

    public ListIterator<List<Object>> iterrows() {
        return new Views.ListView<>(this, true).listIterator();
    }

    /**
     * Update columns formats
     */
    public void updateColumnFormats() {
        if (this.array2D) {
            Column col = this.columns.get(0);
            col.updateFormat((Array) data);
            for (int i = 1; i < this.columns.size(); i++) {
                this.columns.get(i).setFormat(col.getFormat());
                this.columns.get(i).setFormatLen(col.getFormatLen());
            }
        } else {
            for (int i = 0; i < this.columns.size(); i++) {
                this.columns.get(i).updateFormat(((List<Array>) data).get(i));
            }
        }
    }

    /**
     * Get the number of columns
     *
     * @return The number of columns
     */
    public int size() {
        return this.columns.size();
    }

    /**
     * Get the number of rows
     *
     * @return The number of rows
     */
    public int length() {
        return this.index.size();
    }

    /**
     * Return {@code true} if the data frame contains no data.
     *
     * <pre> {@code
     * > DataFrame<Object> df = new DataFrame<>();
     * > df.isEmpty();
     * true }</pre>
     *
     * @return the number of columns
     */
    public boolean isEmpty() {
        return this.data == null ? true : length() == 0;
    }

    /**
     * Return a data frame column as a list.
     *
     * <pre> {@code
     * > DataFrame<Object> df = new DataFrame<>(
     * >         Collections.emptyList(),
     * >         Arrays.asList("name", "value"),
     * >         Arrays.asList(
     * >             Arrays.<Object>asList("alpha", "bravo", "charlie"),
     * >             Arrays.<Object>asList(1, 2, 3)
     * >         )
     * >     );
     * > df.col(1);
     * [1, 2, 3] }</pre>
     *
     * @param column the column index
     * @return the list of values
     */
    public List col(final Integer column) {
        return new Views.DataFrameListView<>(this, column, true);
    }

    /**
     * Return a data frame row as a list.
     *
     * <pre> {@code
     * > DataFrame<Object> df = new DataFrame<>(
     * >         Collections.emptyList(),
     * >         Collections.emptyList(),
     * >         Arrays.asList(
     * >             Arrays.<Object>asList("alpha", "bravo", "charlie"),
     * >             Arrays.<Object>asList(1, 2, 3)
     * >         )
     * >     );
     * > df.row(1);
     * [bravo, 2] }</pre>
     *
     * @param row the row index
     * @return the list of values
     */
    public List row(final Integer row) {
        return new Views.SeriesListView<>(this, row, false);
    }

    /**
     * Get row series
     *
     * @param row Row index
     * @return Series
     */
    public Series rowSeries(int row) {
        List rowData = new Views.SeriesListView<>(this, row, false);
        Array a;
        if (this.columns.isSameDataType()) {
            a = Array.factory(this.columns.get(0).dataType, new int[]{rowData.size()});
        } else {
            a = Array.factory(DataType.OBJECT, new int[]{rowData.size()});
        }
        for (int i = 0; i < rowData.size(); i++) {
            a.setObject(i, rowData.get(i));
        }
        Index idx = this.columns.asIndex();
        Series s = new Series(a, idx, "Index");
        return s;
    }

    /**
     * Get shape
     *
     * @return Shape
     */
    public int[] getShape() {
        int[] shape = new int[2];
        shape[0] = this.index.size();
        shape[1] = this.columns.size();
        return shape;
    }

    /**
     * Get value
     *
     * @param row Row object
     * @param col Column object
     * @return Value
     */
    public Object getValue(Object row, Column col) {
        return getValue(this.index.indexOf(row), this.columns.indexOf(col));
    }

    /**
     * Get value
     *
     * @param row Row index
     * @param col Column index
     * @return Value
     */
    public Object getValue(int row, int col) {
        if (this.array2D) {
            return ((Array) this.data).getObject(row * this.size() + col);
        } else {
            return ((Array) ((List) this.data).get(col)).getObject(row);
        }
    }

    /**
     * Get value
     *
     * @param row Row index
     * @param colName Column name
     * @return Value
     */
    public Object getValue(int row, String colName) {
        int col = this.columns.indexOfName(colName);
        if (col >= 0) {
            return getValue(row, col);
        } else {
            System.out.println("Column not exists: " + colName + "!");
            return null;
        }
    }

    /**
     * Set value
     *
     * @param row Row
     * @param col Column
     * @param v Value
     */
    public void setValue(Object row, Column col, Object v) {
        int ri = this.index.indexOf(row);
        this.setValue(ri, col, v);
    }

    /**
     * Set value
     *
     * @param row Row index
     * @param col Column index
     * @param v Value
     */
    public void setValue(int row, int col, Object v) {
        if (this.array2D) {
            ((Array) this.data).setObject(row * this.size() + col, v);
        } else {
            ((Array) ((List) this.data).get(col)).setObject(row, v);
        }
    }

    /**
     * Set value
     *
     * @param row Row index
     * @param colName Column name
     * @param v Value
     */
    public void setValue(int row, String colName, Object v) {
        int col = this.columns.indexOfName(colName);
        if (col >= 0) {
            setValue(row, col, v);
        } else {
            System.out.println("Column not exists: " + colName + "!");
        }
    }

    /**
     * Set value
     *
     * @param row Row index
     * @param column Column
     * @param v Value
     */
    public void setValue(int row, Column column, Object v) {
        int col = this.columns.indexOf(column);
        if (col >= 0) {
            setValue(row, col, v);
        } else {
            System.out.println("Column not exists: " + column.getName() + "!");
        }
    }

    /**
     * Get column data array
     *
     * @param col Column index
     * @return Column data array
     * @throws InvalidRangeException
     */
    public Array getColumnData(int col) throws InvalidRangeException {
        Array r;
        if (this.array2D) {
            Range rowRange = new Range(0, this.length() - 1, 1);
            Range colRange = new Range(col, col, 1);
            List<Range> ranges = new ArrayList<>();
            ranges.add(rowRange);
            ranges.add(colRange);
            r = ArrayMath.section((Array) this.data, ranges);
            r = r.copy();
        } else {
            r = (Array) ((List) this.data).get(col);
        }
        return r;
    }

    /**
     * Get column data array
     *
     * @param colName Column name
     * @return Column data array
     * @throws InvalidRangeException
     */
    public Array getColumnData(String colName) throws InvalidRangeException {
        int col = this.columns.getNames().indexOf(colName);
        if (col >= 0) {
            return getColumnData(col);
        } else {
            System.out.println("Column not exists: " + colName + "!");
            return null;
        }
    }

    /**
     * Add column data
     *
     * @param column Column
     */
    public void addColumn(Column column) {
        if (this.data == null) {
            this.columns.add(column);
        } else {
            Array array = Array.factory(column.getDataType(), new int[]{this.length()});
            try {
                this.addColumn(column, array);
            } catch (InvalidRangeException ex) {
                Logger.getLogger(DataFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Add column data
     *
     * @param column Column
     * @param a Column data array
     * @throws InvalidRangeException
     */
    public void addColumn(Column column, Array a) throws InvalidRangeException {
        DataType dt = a.getDataType();
        if (this.array2D) {
            DataType dt1 = this.columns.get(0).getDataType();
            if (dt1 != dt) {
                if (dt1.isNumeric() && dt.isNumeric()) {
                    if (dt1 == DataType.DOUBLE) {
                        a = ArrayUtil.toDouble(a);
                        dt = a.getDataType();
                    } else if (dt1 == DataType.FLOAT) {
                        a = ArrayUtil.toFloat(a);
                        dt = a.getDataType();
                    }
                }
            }
            if (dt1 == dt) {
                Array ra = Array.factory(dt, new int[]{this.length(), this.size() + 1});
                Range rowRange = new Range(0, this.length() - 1, 1);
                Range colRange = new Range(0, this.size() - 1, 1);
                List<Range> ranges = Arrays.asList(rowRange, colRange);
                ArrayMath.setSection(ra, ranges, (Array) this.data);
                colRange = new Range(this.size(), this.size(), 1);
                ranges = Arrays.asList(rowRange, colRange);
                ArrayMath.setSection(ra, ranges, a);
                this.data = ra;
            } else {
                this.dataToList();
                ((List<Array>) data).add(a);
            }
        } else {
            if (this.data == null) {
                this.data = new ArrayList<>();
            }
            ((List<Array>) data).add(a);
        }
        this.columns.add(column);
    }
    
    /**
     * Add column data
     *
     * @param column Column
     * @param s Column data series
     * @throws InvalidRangeException
     */
    public void addColumn(Column column, Series s) throws InvalidRangeException {
        Array a = s.getData();
        this.addColumn(column, a);
    }

    /**
     * Add column data
     *
     * @param loc Location
     * @param column Column
     * @param a Column data array
     * @throws InvalidRangeException
     */
    public void addColumn(int loc, Column column, Array a) throws InvalidRangeException {
        DataType dt = a.getDataType();
        if (this.array2D) {
            DataType dt1 = this.columns.get(0).getDataType();
            if (dt1 != dt) {
                if (dt1.isNumeric() && dt.isNumeric()) {
                    if (dt1 == DataType.DOUBLE) {
                        a = ArrayUtil.toDouble(a);
                        dt = a.getDataType();
                    } else if (dt1 == DataType.FLOAT) {
                        a = ArrayUtil.toFloat(a);
                        dt = a.getDataType();
                    }
                }
            }
            if (dt1 == dt) {
                Array ra = Array.factory(dt, new int[]{this.length(), this.size() + 1});
                Range rowRange = new Range(0, this.length() - 1, 1);
                List<Integer> colList = new ArrayList<>();
                for (int i = 0; i < this.size() + 1; i++) {
                    if (i != loc) {
                        colList.add(i);
                    }
                }
                List<Object> mranges = Arrays.asList(rowRange, colList);
                ra = ArrayMath.setSection_Mix(ra, mranges, (Array) this.data);
                Range colRange = new Range(loc, loc, 1);
                List<Range> ranges = Arrays.asList(rowRange, colRange);
                ra = ArrayMath.setSection(ra, ranges, a);
                this.data = ra;
            } else {
                this.dataToList();
                ((List<Array>) data).add(loc, a);
            }
        } else {
            if (this.data == null) {
                this.data = new ArrayList<>();
            }
            ((List<Array>) data).add(loc, a);
        }
        this.columns.add(loc, column);
    }

    /**
     * Add column data
     *
     * @param colName Column name
     * @param a Column data array
     * @throws InvalidRangeException
     */
    public void addColumn(String colName, Array a) throws InvalidRangeException {
        Column column = Column.factory(colName, a);
        addColumn(column, a);
    }

    /**
     * Add column data
     *
     * @param loc Location
     * @param colName Column name
     * @param a Column data array
     * @throws InvalidRangeException
     */
    public void addColumn(int loc, String colName, Array a) throws InvalidRangeException {
        Column column = Column.factory(colName, a);
        addColumn(loc, column, a);
    }

    /**
     * Add column data
     *
     * @param loc Location
     * @param colName Column name
     * @param o Column data object
     * @throws InvalidRangeException
     */
    public void addColumn(int loc, String colName, Object o) throws InvalidRangeException {
        DataType dt = ArrayMath.getDataType(o);
        Array ra = Array.factory(dt, new int[]{this.length()});
        for (int i = 0; i < ra.getSize(); i++) {
            ra.setObject(i, o);
        }
        Column column = Column.factory(colName, ra);
        addColumn(loc, column, ra);
    }

    /**
     * Set column name
     * @param idx Column index
     * @param colName Column name
     */
    public void setColumnName(int idx, String colName) {
        this.columns.get(idx).setName(colName);
    }

    /**
     * Set column data
     *
     * @param colName Column name
     * @param a Column data array
     * @throws InvalidRangeException
     */
    public void setColumn(String colName, Array a) throws InvalidRangeException {
        int col = this.columns.getNames().indexOf(colName);
        if (col >= 0) {
            if (this.array2D) {
                Range rowRange = new Range(0, this.length() - 1, 1);
                Range colRange = new Range(col, col, 1);
                List<Range> ranges = new ArrayList<>();
                ranges.add(rowRange);
                ranges.add(colRange);
                ArrayMath.setSection((Array) this.data, ranges, a);
            } else {
                this.columns.get(col).setDataType(a.getDataType());
                ((List<Array>) this.data).set(col, a);
            }
        } else {
            this.addColumn(colName, a);
        }
    }

    /**
     * Set column data
     *
     * @param colName Column name
     * @param a A value
     * @throws InvalidRangeException
     */
    public void setColumn(String colName, Object a) throws InvalidRangeException {
        DataType dt;
        int col = this.columns.getNames().indexOf(colName);
        if (col >= 0) {
            dt = this.columns.get(col).dataType;
        } else {
            dt = ArrayMath.getDataType(a);
        }
        Array ra = Array.factory(dt, new int[]{this.length()});
        for (int i = 0; i < ra.getSize(); i++) {
            ra.setObject(i, a);
        }
        this.setColumn(colName, ra);
    }

    /**
     * Append another data frame
     *
     * @param df Another data frame
     * @return Appended data frame
     */
    public DataFrame append(DataFrame df) {
        Index ridx = this.index.append(df.index);
        if (this.array2D && df.array2D) {
            Array ra = Array.factory(((Array) this.data).getDataType(),
                    new int[]{this.length() + df.length(), this.size()});
            int n = this.length() * this.size();
            for (int i = 0; i < ra.getSize(); i++) {
                if (i < n) {
                    ra.setObject(i, ((Array) this.data).getObject(i));
                } else {
                    ra.setObject(i, ((Array) df.data).getObject(i - n));
                }
            }
            DataFrame rdf = new DataFrame(ra, ridx, this.columns);
            return rdf;
        } else {
            List<Array> ra = new ArrayList<>();
            int n = this.length();
            for (int i = 0; i < this.size(); i++) {
                Array a = Array.factory(this.columns.get(i).getDataType(), new int[]{this.length() + df.length()});
                for (int j = 0; j < a.getSize(); j++) {
                    if (j < n) {
                        a.setObject(j, this.getValue(j, i));
                    } else {
                        a.setObject(j, df.getValue(j - n, i));
                    }
                }
                ra.add(a);
            }
            DataFrame rdf = new DataFrame(ra, ridx, this.columns);
            return rdf;
        }
    }

    /**
     * Convert array 2D to array list.
     */
    public void dataToList() {
        if (this.array2D) {
            List<Array> r = new ArrayList<>();
            for (int i = 0; i < this.size(); i++) {
                Array ra = Array.factory(((Array) this.data).getDataType(), new int[]{this.length()});
                for (int j = 0; j < this.length(); j++) {
                    ra.setObject(j, ((Array) data).getObject(j * this.size() + i));
                }
                r.add(ra);
            }
            this.data = r;
            this.array2D = false;
        }
    }

    /**
     * Data reshape
     *
     * @param nrow Number of rows
     * @param ncol Number of columns
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public void dataReshape(int nrow, int ncol) throws InvalidRangeException {
        if (this.array2D) {
            Array r = Array.factory(((Array) this.data).getDataType(), new int[]{nrow, ncol});
            Range rowRange = new Range(0, Math.min(nrow - 1, this.length() - 1), 1);
            Range colRange = new Range(0, Math.min(ncol - 1, this.size() - 1), 1);
            List<Range> ranges = Arrays.asList(rowRange, colRange);
            ArrayMath.setSection(r, ranges, (Array) this.data);
            this.data = r;
        } else {
            List<Array> r = new ArrayList<>();
            for (Array a : (List<Array>) this.data) {
                Array ra = Array.factory(a.getDataType(), new int[]{nrow});
                for (int i = 0; i < a.getSize(); i++) {
                    ra.setObject(i, a.getObject(i));
                }
                r.add(ra);
            }
            this.data = r;
        }
    }

    /**
     * Append row data
     *
     * @param name Index element
     * @param row Row data list
     */
    public void append(Object name, List row) {
        if (this.index == null) {
            List idx = new ArrayList<>();
            idx.add(name);
            this.index = Index.factory(idx);
        } else {
            this.index.add(name);
        }
        if (this.data == null) {
            this.data = new ArrayList<Array>();
            if (row.isEmpty()) {
                for (Column col : this.columns) {
                    ((ArrayList<Array>) this.data).add(Array.factory(col.dataType, new int[]{1}));
                }
            } else {
                int i = 0;
                for (Column col : this.columns) {
                    Array a = Array.factory(col.dataType, new int[]{1});
                    a.setObject(0, row.get(i));
                    ((ArrayList<Array>) this.data).add(a);
                    i += 1;
                }
            }
        } else {
            try {
                this.dataReshape(this.length() + 1, this.size());
            } catch (InvalidRangeException ex) {
                Logger.getLogger(DataFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!row.isEmpty()) {
                if (this.array2D) {
                    for (int i = 0; i < this.size(); i++) {
                        ((Array) this.data).setObject(this.length() * this.size() + i, row.get(i));
                    }
                } else {
                    for (int i = 0; i < this.size(); i++) {
                        ((List<Array>) this.data).get(i).setObject(this.length(), i < row.size() ? row.get(i) : null);
                    }
                }
            }
        }
    }
    
    /**
     * Append row data
     *
     * @param name Index element
     * @param row Row data array
     */
    public void append(Object name, Array row) {
        if (this.index == null) {
            List idx = new ArrayList<>();
            idx.add(name);
            this.index = Index.factory(idx);
        } else {
            this.index.add(name);
        }
        if (this.data == null) {
            this.data = new ArrayList<Array>();
            if (row == null) {
                for (Column col : this.columns) {
                    ((ArrayList<Array>) this.data).add(Array.factory(col.dataType, new int[]{1}));
                }
            } else {
                int i = 0;
                for (Column col : this.columns) {
                    Array a = Array.factory(col.dataType, new int[]{1});
                    a.setObject(0, row.getObject(i));
                    ((ArrayList<Array>) this.data).add(a);
                    i += 1;
                }
            }
        } else {
            try {
                this.dataReshape(this.length() + 1, this.size());
            } catch (InvalidRangeException ex) {
                Logger.getLogger(DataFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (row != null) {
                if (this.array2D) {
                    for (int i = 0; i < this.size(); i++) {
                        ((Array) this.data).setObject(this.length() * this.size() + i, row.getObject(i));
                    }
                } else {
                    for (int i = 0; i < this.size(); i++) {
                        ((List<Array>) this.data).get(i).setObject(this.length(), i < row.getSize() ? row.getObject(i) : null);
                    }
                }
            }
        }
    }
    
    /**
     * Append row data
     *
     * @param row Row data list
     */
    public void append(List row) {
        Object name = row.get(0);
        row.remove(0);
        this.append(name, row);
    }
    
    /**
     * Set row data
     * @param key Index key
     * @param row Row data
     */
    public void setRow(Object key, List row) {
        List<Integer> ii = this.index.indexAll(key);
        if (ii.isEmpty()) {
            this.append(key, row);
        } else {
            for (int i : ii) {
                if (this.array2D) {
                    Array a = (Array)this.data;
                    int[] shape = a.getShape();
                    org.meteoinfo.ndarray.Index2D idx = (org.meteoinfo.ndarray.Index2D)a.getIndex();
                    for (int j = 0; j < shape[1]; j++) {
                        idx.set(j, j);
                        a.setObject(idx, row.get(j));
                    }
                } else {
                    for (int j = 0; j < this.size(); j++) {
                        ((List<Array>) this.data).get(j).setObject(i, row.get(i));
                    }
                }
            }
        }
    }
    
    /**
     * Set row data
     * @param key Index key
     * @param row Row data
     */
    public void setRow(Object key, Array row) {
        List<Integer> ii = this.index.indexAll(key);
        if (ii.isEmpty()) {
            this.append(key, row);
        } else {
            for (int i : ii) {
                if (this.array2D) {
                    Array a = (Array)this.data;
                    int[] shape = a.getShape();
                    org.meteoinfo.ndarray.Index2D idx = (org.meteoinfo.ndarray.Index2D)a.getIndex();
                    for (int j = 0; j < shape[1]; j++) {
                        idx.set(j, j);
                        a.setObject(idx, row.getObject(j));
                    }
                } else {
                    for (int j = 0; j < this.size(); j++) {
                        ((List<Array>) this.data).get(j).setObject(i, row.getObject(i));
                    }
                }
            }
        }
    }

    /**
     * Create a new data frame by leaving out the specified columns.
     *
     * @param colNames Column names
     * @return a shallow copy of the data frame with the columns removed
     */
    public DataFrame drop(List<String> colNames) {
        return drop(columns.indexOfName(colNames).toArray(new Integer[colNames.size()]));
    }

    /**
     * Create a new data frame by leaving out the specified columns.
     *
     * @param cols the names of columns to be removed
     * @return a shallow copy of the data frame with the columns removed
     */
    public DataFrame drop(final Column... cols) {
        return drop(columns.indices(cols));
    }

    /**
     * Create a new data frame by leaving out the specified columns.
     *
     * @param cols the indices of the columns to be removed
     * @return a shallow copy of the data frame with the columns removed
     */
    public DataFrame drop(final Integer... cols) {
        final List<String> colnames = new ArrayList<>(columns.getNames());
        final List<String> todrop = new ArrayList<>(cols.length);
        for (final int col : cols) {
            todrop.add(colnames.get(col));
        }
        colnames.removeAll(todrop);

        if (this.array2D) {
            return null;
        } else {
            final List<Array> keep = new ArrayList<>(colnames.size());
            for (final String col : colnames) {
                keep.add(((List<Array>) this.data).get(this.columns.indexOfName(col)));
            }

            return new DataFrame(keep, index.getValues(), colnames);
        }
    }

    /**
     * Create a new data frame containing only the specified columns.
     *
     * <pre> {@code
     * > DataFrame<Object> df = new DataFrame<>("name", "value", "category");
     * > df.retain("name", "category").columns();
     * [name, category] }</pre>
     *
     * @param cols the columns to include in the new data frame
     * @return a new data frame containing only the specified columns
     */
    public DataFrame retain(final Object... cols) {
        return retain(columns.indices(cols));
    }

    /**
     * Create a new data frame containing only the specified columns.
     *
     * <pre> {@code
     *  DataFrame<Object> df = new DataFrame<>("name", "value", "category");
     *  df.retain(0, 2).columns();
     * [name, category] }</pre>
     *
     * @param cols the columns to include in the new data frame
     * @return a new data frame containing only the specified columns
     */
    public DataFrame retain(final Integer... cols) {
        final Set<Integer> keep = new HashSet<>(Arrays.asList(cols));
        final Integer[] todrop = new Integer[size() - keep.size()];
        for (int i = 0, c = 0; c < size(); c++) {
            if (!keep.contains(c)) {
                todrop[i++] = c;
            }
        }
        return drop(todrop);
    }

    /**
     * Return a data frame containing only columns with numeric data.
     *
     * <pre> {@code
     * > DataFrame<Object> df = new DataFrame<>("name", "value");
     * > df.append(Arrays.asList("one", 1));
     * > df.append(Arrays.asList("two", 2));
     * > df.numeric().columns();
     * [value] }</pre>
     *
     * @return a data frame containing only the numeric columns
     */
    public DataFrame numeric() {
//        final SparseBitSet numeric = Inspection.numeric(this);
//        final List keep = Selection.select(columns, numeric).getValues();
//        return retain(keep.toArray(new Object[keep.size()]));
        return null;
    }

    /**
     * Select by row and column ranges
     *
     * @param row Row index
     * @param colRange Column range
     * @return Selected data frame or series
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object select(int row, Range colRange) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i = colRange.first(); i <= colRange.last(); i += colRange.stride()) {
            cols.add((Column) this.columns.get(i).clone());
        }

        Object r;
        if (this.array2D) {
            List ranges = new ArrayList<>();
            ranges.add(new Range(row, row, 1));
            ranges.add(new Range(colRange.first(), colRange.last(), colRange.stride()));
            r = ArrayMath.section((Array) this.data, ranges);
        } else {
            r = new ArrayList<>();
            int rn = 1;
            for (int j = colRange.first(); j <= colRange.last(); j += colRange.stride()) {
                Array rr = Array.factory(this.columns.get(j).getDataType(), new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                rr.setObject(0, mr.getObject(row));
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = this.index.subIndex(row, row + 1, 1);
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df.rowSeries(0);
            }
        }
    }

    /**
     * Set values by row and column ranges
     *
     * @param row Row index
     * @param colRange Column range
     * @param value The value
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public void setValues(int row, Range colRange, Number value) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i = colRange.first(); i <= colRange.last(); i += colRange.stride()) {
            cols.add((Column) this.columns.get(i).clone());
        }

        if (this.array2D) {
            List ranges = new ArrayList<>();
            ranges.add(new Range(row, row, 1));
            ranges.add(new Range(colRange.first(), colRange.last(), colRange.stride()));
            ArrayMath.setSection((Array) this.data, ranges, value);
        } else {
            for (int j = colRange.first(); j <= colRange.last(); j += colRange.stride()) {
                ((List<Array>) this.data).get(j).setObject(row, value);
            }
        }
    }

    /**
     * Set values by row and column ranges
     *
     * @param row Row index
     * @param colRange Column range
     * @param value The value array
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public void setValues(int row, Range colRange, Array value) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i = colRange.first(); i <= colRange.last(); i += colRange.stride()) {
            cols.add((Column) this.columns.get(i).clone());
        }

        if (this.array2D) {
            List ranges = new ArrayList<>();
            ranges.add(new Range(row, row, 1));
            ranges.add(new Range(colRange.first(), colRange.last(), colRange.stride()));
            ArrayMath.setSection((Array) this.data, ranges, value);
        } else {
            int i = 0;
            for (int j = colRange.first(); j <= colRange.last(); j += colRange.stride()) {
                ((List<Array>) this.data).get(j).setObject(row, value.getObject(i));
                i += 1;
            }
        }
    }

    /**
     * Select by row and column ranges
     *
     * @param rowRange Row range
     * @param colRange Column range
     * @return Selected data frame or series
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object select(Range rowRange, Range colRange) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i = colRange.first(); i <= colRange.last(); i += colRange.stride()) {
            cols.add((Column) this.columns.get(i).clone());
        }

        Object r;
        if (this.array2D) {
            List ranges = new ArrayList<>();
            ranges.add(new Range(rowRange.first(), rowRange.last(), rowRange.stride()));
            ranges.add(new Range(colRange.first(), colRange.last(), colRange.stride()));
            r = ArrayMath.section((Array) this.data, ranges);
        } else {
            r = new ArrayList<>();
            int rn = rowRange.length();
            for (int j = colRange.first(); j <= colRange.last(); j += colRange.stride()) {
                Array rr = Array.factory(this.columns.get(j).getDataType(), new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                int idx = 0;
                for (int i = rowRange.first(); i <= rowRange.last(); i += rowRange.stride()) {
                    rr.setObject(idx, mr.getObject(i));
                    idx += 1;
                }
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = this.index.subIndex(rowRange.first(), rowRange.last() + 1, rowRange.stride());
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df;
            }
        }
    }

    /**
     * Select by row and column ranges
     *
     * @param rowRange Row range
     * @param colRange Column range
     * @return Selected data frame or series
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object select(Range rowRange, List<Integer> colRange) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i : colRange) {
            cols.add(this.columns.get(i));
        }

        Object r;
        if (this.array2D) {
            List ranges = new ArrayList<>();
            ranges.add(new Range(rowRange.first(), rowRange.last(), rowRange.stride()));
            ranges.add(colRange);
            r = ArrayMath.take((Array) this.data, ranges);
        } else {
            r = new ArrayList<>();
            int rn = rowRange.length();
            for (int j : colRange) {
                Array rr = Array.factory(this.columns.get(j).getDataType(), new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                int idx = 0;
                for (int i = rowRange.first(); i <= rowRange.last(); i += rowRange.stride()) {
                    rr.setObject(idx, mr.getObject(i));
                    idx += 1;
                }
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = this.index.subIndex(rowRange.first(), rowRange.last() + 1, rowRange.stride());
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df;
            }
        }
    }

    /**
     * Select by row ranges
     *
     * @param rowRange Row range
     * @return Selected data frame or series
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object select(List<Integer> rowRange) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i = 0; i < this.size(); i ++) {
            cols.add(this.columns.get(i));
        }

        Object r;
        if (this.array2D) {
            List ranges = new ArrayList<>();
            ranges.add(rowRange);
            ranges.add(new Range(0, cols.size() - 1, 1));
            r = ArrayMath.take((Array) this.data, ranges);
        } else {
            r = new ArrayList<>();
            int rn = rowRange.size();
            for (int j = 0; j < cols.size(); j ++) {
                Array rr = Array.factory(this.columns.get(j).getDataType(), new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                int idx = 0;
                for (int i : rowRange) {
                    rr.setObject(idx, mr.getObject(i));
                    idx += 1;
                }
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = this.index.subIndex(rowRange);
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df;
            }
        }
    }

    /**
     * Select by row and column ranges
     *
     * @param rowRange Row range
     * @param colRange Column range
     * @return Selected data frame or series
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object select(List<Integer> rowRange, Range colRange) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i = colRange.first(); i <= colRange.last(); i += colRange.stride()) {
            cols.add(this.columns.get(i));
        }

        Object r;
        if (this.array2D) {
            List ranges = new ArrayList<>();
            ranges.add(rowRange);
            ranges.add(new Range(colRange.first(), colRange.last(), colRange.stride()));
            r = ArrayMath.take((Array) this.data, ranges);
        } else {
            r = new ArrayList<>();
            int rn = rowRange.size();
            for (int j = colRange.first(); j <= colRange.last(); j += colRange.stride()) {
                Array rr = Array.factory(this.columns.get(j).getDataType(), new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                int idx = 0;
                for (int i : rowRange) {
                    rr.setObject(idx, mr.getObject(i));
                    idx += 1;
                }
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = this.index.subIndex(rowRange);
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df;
            }
        }
    }

    /**
     * Select by row and column ranges
     *
     * @param rowRangeArray Row range array
     * @param colRange Column range
     * @return Selected data frame or series
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object select(Array rowRangeArray, Range colRange) throws InvalidRangeException {
        rowRangeArray = rowRangeArray.copyIfView();

        ColumnIndex cols = new ColumnIndex();
        for (int i = colRange.first(); i <= colRange.last(); i += colRange.stride()) {
            cols.add(this.columns.get(i));
        }

        List<Integer> rowRange = new ArrayList<>();
        if (rowRangeArray.getDataType() == DataType.BOOLEAN) {
            for (int i = 0; i < rowRangeArray.getSize(); i++) {
                if (rowRangeArray.getBoolean(i)) {
                    rowRange.add(i);
                }
            }
        } else {
            for (int i = 0; i < rowRangeArray.getSize(); i++) {
                rowRange.add(rowRangeArray.getInt(i));
            }
        }

        Object r;
        if (this.array2D) {
            List ranges = new ArrayList<>();
            ranges.add(rowRange);
            ranges.add(new Range(colRange.first(), colRange.last(), colRange.stride()));
            r = ArrayMath.take((Array) this.data, ranges);
        } else {
            r = new ArrayList<>();
            int rn = rowRange.size();
            for (int j = colRange.first(); j <= colRange.last(); j += colRange.stride()) {
                Array rr = Array.factory(this.columns.get(j).getDataType(), new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                int idx = 0;
                for (int i : rowRange) {
                    rr.setObject(idx, mr.getObject(i));
                    idx += 1;
                }
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = this.index.subIndex(rowRange);
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df;
            }
        }
    }

    /**
     * Select by row and column ranges
     *
     * @param rowKeys Row keys
     * @param rowRange Row range
     * @param colRange Column range
     * @return Selected data frame or series
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object select(List rowKeys, List<Integer> rowRange, Range colRange) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i = colRange.first(); i <= colRange.last(); i += colRange.stride()) {
            cols.add(this.columns.get(i));
        }

        Object r;
        if (this.array2D) {
            int n = ((Array) data).getShape()[1];
            int rn = rowRange.size();
            int cn = colRange.length();
            DataType dtype = ((Array) data).getDataType();
            r = Array.factory(dtype, new int[]{rn, cn});
            String format = this.columns.get(0).getFormat();
            int idx, jj = 0;
            for (int j = colRange.first(); j <= colRange.last(); j += colRange.stride()) {
                int ii = 0;
                for (int i : rowRange) {
                    idx = ii * cn + jj;
                    if (i < 0) {
                        ((Array) r).setObject(idx, DataConvert.convertTo(null, dtype, format));
                    } else {
                        ((Array) r).setObject(idx, ((Array) data).getObject(i * n + j));
                    }
                    ii += 1;
                }
                jj += 1;
            }
        } else {
            r = new ArrayList<>();
            int rn = rowRange.size();
            for (int j = colRange.first(); j <= colRange.last(); j += colRange.stride()) {
                DataType dtype = this.columns.get(j).getDataType();
                String format = this.columns.get(j).getFormat();
                Array rr = Array.factory(dtype, new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                int idx = 0;
                for (int i : rowRange) {
                    if (i < 0) {
                        rr.setObject(idx, DataConvert.convertTo(null, dtype, format));
                    } else {
                        rr.setObject(idx, mr.getObject(i));
                    }
                    idx += 1;
                }
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = Index.factory(rowKeys);
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df;
            }
        }
    }

    /**
     * Select by row and column ranges
     *
     * @param rowKeys Row keys
     * @param rowRange Row range
     * @param colRange Column range
     * @return Selected data frame or series
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public Object select(List rowKeys, List<Integer> rowRange, List<Integer> colRange) throws InvalidRangeException {
        ColumnIndex cols = new ColumnIndex();
        for (int i : colRange) {
            cols.add(this.columns.get(i));
        }

        Object r;
        if (this.array2D) {
            int n = ((Array) data).getShape()[1];
            int rn = rowRange.size();
            int cn = colRange.size();
            DataType dtype = ((Array) data).getDataType();
            r = Array.factory(dtype, new int[]{rn, cn});
            String format = this.columns.get(0).getFormat();
            int idx, jj = 0;
            for (int j : colRange) {
                int ii = 0;
                for (int i : rowRange) {
                    idx = ii * cn + jj;
                    if (i < 0) {
                        ((Array) r).setObject(idx, DataConvert.convertTo(null, dtype, format));
                    } else {
                        ((Array) r).setObject(idx, ((Array) data).getObject(i * n + j));
                    }
                    ii += 1;
                }
                jj += 1;
            }
        } else {
            r = new ArrayList<>();
            int rn = rowRange.size();
            for (int j : colRange) {
                DataType dtype = this.columns.get(j).getDataType();
                String format = this.columns.get(j).getFormat();
                Array rr = Array.factory(dtype, new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                int idx = 0;
                for (int i : rowRange) {
                    if (i < 0) {
                        rr.setObject(idx, DataConvert.convertTo(null, dtype, format));
                    } else {
                        rr.setObject(idx, mr.getObject(i));
                    }
                    idx += 1;
                }
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = Index.factory(rowKeys);
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df;
            }
        }
    }

    /**
     * Select by row and column ranges
     *
     * @param row Row index
     * @param colRange Column range
     * @return Selected data frame or series
     */
    public Object select(int row, List<Integer> colRange) {
        ColumnIndex cols = new ColumnIndex();
        for (int i : colRange) {
            cols.add(this.columns.get(i));
        }

        Object r;
        if (this.array2D) {
            int n = ((Array) data).getShape()[1];
            int rn = 1;
            int cn = colRange.size();
            DataType dtype = ((Array) data).getDataType();
            r = Array.factory(dtype, new int[]{rn, cn});
            for (int j : colRange) {
                ((Array) r).setObject(0, ((Array) data).getObject(row * n + j));
            }
        } else {
            r = new ArrayList<>();
            int rn = 1;
            for (int j : colRange) {
                Array rr = Array.factory(this.columns.get(j).getDataType(), new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                rr.setObject(0, mr.getObject(row));
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = this.index.subIndex(row, row + 1, 1);
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df.rowSeries(0);
            }
        }
    }

    /**
     * Select by row and column ranges
     *
     * @param rowRange Row range
     * @param colRange Column range
     * @return Selected data frame or series
     */
    public Object select(List<Integer> rowRange, List<Integer> colRange) {
        ColumnIndex cols = new ColumnIndex();
        for (int i : colRange) {
            cols.add(this.columns.get(i));
        }

        Object r;
        if (this.array2D) {
            int n = ((Array) data).getShape()[1];
            int rn = rowRange.size();
            int cn = colRange.size();
            DataType dtype = ((Array) data).getDataType();
            r = Array.factory(dtype, new int[]{rn, cn});
            String format = this.columns.get(0).getFormat();
            int idx, jj = 0;
            for (int j : colRange) {
                int ii = 0;
                for (int i : rowRange) {
                    idx = ii * cn + jj;
                    if (i < 0) {
                        ((Array) r).setObject(idx, DataConvert.convertTo(null, dtype, format));
                    } else {
                        ((Array) r).setObject(idx, ((Array) data).getObject(i * n + j));
                    }
                    ii += 1;
                }
                jj += 1;
            }
        } else {
            r = new ArrayList<>();
            int rn = rowRange.size();
            for (int j : colRange) {
                Array rr = Array.factory(this.columns.get(j).getDataType(), new int[]{rn});
                Array mr = ((List<Array>) this.data).get(j);
                int idx = 0;
                for (int i : rowRange) {
                    rr.setObject(idx, mr.getObject(i));
                    idx += 1;
                }
                ((ArrayList) r).add(rr);
            }
            if (cols.size() == 1) {
                r = ((ArrayList) r).get(0);
            }
        }

        if (r == null) {
            return null;
        } else {
            Index rIndex = this.index.subIndex(rowRange);
            if (cols.size() == 1 && this.columns.size() > 1) {
                Series s = new Series((Array) r, rIndex, cols.get(0).getName());
                return s;
            } else {
                DataFrame df;
                if (r instanceof Array) {
                    df = new DataFrame((Array) r, rIndex, cols);
                } else {
                    df = new DataFrame((ArrayList) r, rIndex, cols);
                }
                return df;
            }
        }
    }

    /**
     * Transpose
     *
     * @return Transposed data frame
     */
    public DataFrame transpose() {
        DataFrame df = null;
        if (this.array2D) {
            Array ta = ArrayMath.transpose((Array) this.data, 0, 1);
            List tIndex = new ArrayList<>();
            for (Column col : this.columns) {
                tIndex.add(col.getName());
            }
            List<String> tColumns = new ArrayList<>();
            for (int i = 0; i < this.index.size(); i++) {
                tColumns.add(this.index.toString(i));
            }
            df = new DataFrame(ta, tIndex, tColumns);
        }

        return df;
    }

    private String toString(int start, int end) {
        this.updateColumnFormats();

        StringBuilder sb = new StringBuilder();
        String format = this.index.getNameFormat();
        sb.append(String.format(format, " "));
        for (Column col : this.columns.getValues()) {
            sb.append(" ");
            sb.append(String.format(col.getNameFormat(), col.getName()));
        }
        sb.append("\n");

        for (int r = start; r < end; r++) {
            sb.append(this.index.toString(r));
            for (int i = 0; i < this.size(); i++) {
                sb.append(" ");
                sb.append(this.columns.get(i).toString(this.getValue(r, i)));
            }
            sb.append("\n");
        }
        if (end < this.index.size()) {
            sb.append("...");
        }

        return sb.toString();
    }

    /**
     * Convert to string - head
     *
     * @param n Head row number
     * @return The string
     */
    public String head(int n) {
        int rn = this.index.size();
        if (n > rn) {
            n = rn;
        }
        return toString(0, n);
    }

    /**
     * Convert to string - tail
     *
     * @param n Tail row number
     * @return The string
     */
    public String tail(int n) {
        int rn = this.index.size();
        if (n > rn) {
            n = rn;
        }
        return toString(rn - n, rn);
    }

    @Override
    public String toString() {
        return head(100);
    }

    /**
     * Read data frame from ASCII file
     *
     * @param fileName File name
     * @param delimiter Delimiter
     * @param skipRows Number of lines to skip at begining of the file
     * @param formatSpec Format specifiers string
     * @param encoding Fle encoding
     * @param indexCol Column to be used as index
     * @param indexFormat Index format
     * @param names Column names
     * @param header Row number to use as the column names
     * @param skipFooter Number of lines at bottom of file to skip
     * @return DataFrame object
     * @throws java.io.FileNotFoundException
     */
    public static DataFrame readTable(String fileName, String delimiter, int skipRows, String formatSpec, String encoding,
            int indexCol, String indexFormat, List<String> names, Integer header, int skipFooter) throws FileNotFoundException, IOException, Exception {
        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
        if (skipRows > 0) {
            for (int i = 0; i < skipRows; i++) {
                sr.readLine();
            }
        }

        String title = sr.readLine().trim();
        if (encoding.equals("UTF8")) {
            if (title.startsWith("\uFEFF")) {
                title = title.substring(1);
            }
        }
        String[] titleArray1 = GlobalUtil.split(title, delimiter);
        List<String> titleArray = new ArrayList(Arrays.asList(titleArray1));
        if (indexCol >= 0) {
            titleArray.remove(indexCol);
        }

        if (titleArray.isEmpty()) {
            System.out.println("File Format Error!");
            sr.close();
            return null;
        }

        int colNum = titleArray.size();
        if (header == null) {
            for (int i = 0; i < colNum; i++) {
                titleArray.set(i, "Col_" + String.valueOf(i));
            }
        }

        //Get fields
        ColumnIndex cols = new ColumnIndex();
        Column col;
        List<List> values = new ArrayList<>();
        String[] colFormats;
        if (formatSpec == null) {
            colFormats = new String[colNum];
            for (int i = 0; i < colNum; i++) {
                colFormats[i] = "C";
            }
        } else {
            colFormats = formatSpec.split("%");
        }

        int idx = 0;
        boolean isBreak = false;
        for (String colFormat : colFormats) {
            if (colFormat.isEmpty()) {
                continue;
            }

            int num = 1;
            if (colFormat.length() > 1 && !colFormat.substring(0, 1).equals("{")) {
                int index = colFormat.indexOf("{");
                if (index < 0) {
                    index = colFormat.length() - 1;
                }
                num = Integer.parseInt(colFormat.substring(0, index));
                colFormat = colFormat.substring(index);
            }
            for (int i = 0; i < num; i++) {
                String colName = titleArray.get(idx).trim();
                if (colFormat.equals("C") || colFormat.equals("s")) //String
                {
                    col = new Column(colName, DataType.STRING);
                } else if (colFormat.equals("i")) //Integer
                {
                    col = new Column(colName, DataType.INT);
                } else if (colFormat.equals("f")) //Float
                {
                    col = new Column(colName, DataType.FLOAT);
                } else if (colFormat.equals("d")) //Double
                {
                    col = new Column(colName, DataType.DOUBLE);
                } else if (colFormat.equals("B")) //Boolean
                {
                    col = new Column(colName, DataType.BOOLEAN);
                } else if (colFormat.substring(0, 1).equals("{")) {    //Date
                    int eidx = colFormat.indexOf("}");
                    String formatStr = colFormat.substring(1, eidx);
                    col = new Column(colName, DataType.DATE);
                    col.setFormat(formatStr);
                } else {
                    col = new Column(colName, DataType.STRING);
                }
                cols.add(col);
                values.add(new ArrayList<>());
                idx += 1;
                if (idx == colNum) {
                    isBreak = true;
                    break;
                }
            }
            if (isBreak) {
                break;
            }
        }

        if (idx < colNum) {
            for (int i = idx; i < colNum; i++) {
                cols.add(new Column(titleArray.get(i), DataType.STRING));
                values.add(new ArrayList<>());
            }
        }

        String[] dataArray;
        List<String> indexValues = new ArrayList<>();
        String line;
        if (header == null) {
            line = title;
        } else {
            line = sr.readLine();
        }
        while (line != null) {
            line = line.trim();
            if (line.isEmpty()) {
                line = sr.readLine();
                continue;
            }
            dataArray = GlobalUtil.split(line, delimiter);
            int cn = 0;
            for (int i = 0; i < dataArray.length; i++) {
                if (cn < colNum) {
                    if (i == indexCol) {
                        indexValues.add(dataArray[i]);
                    } else {
                        values.get(cn).add(dataArray[i]);
                        cn++;
                    }
                } else {
                    break;
                }
            }
            if (cn < colNum) {
                for (int i = cn; i < colNum; i++) {
                    values.get(i).add("");
                }
            }

            line = sr.readLine();
        }
        sr.close();

        int rn = values.get(0).size();
        if (skipFooter > 0) {
            if (indexCol >= 0) {
                indexValues = indexValues.subList(0, rn - skipFooter);
            }
            for (int i = 0; i < colNum; i++) {
                values.set(i, values.get(i).subList(0, rn - skipFooter));
            }
        }
        rn = values.get(0).size();
        Index index;
        if (indexCol >= 0) {
            DataType idxDT;
            DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_DATE_TIME;
            if (indexFormat != null) {
                if (indexFormat.substring(0, 1).equals("%")) {
                    indexFormat = indexFormat.substring(1);
                }
                idxDT = DataConvert.getDataType(indexFormat);
                if (idxDT == DataType.DATE) {
                    indexFormat = DataConvert.getDateFormat(indexFormat);
                    dtFormatter = DateTimeFormatter.ofPattern(indexFormat);
                }
            } else {
                idxDT = DataConvert.detectDataType(indexValues, 10, null);
                if (idxDT == DataType.DATE) {
                    dtFormatter = TypeUtils.getDateTimeFormatter(indexValues.get(0));
                }
            }

            List indexData = new ArrayList<>();
            if (idxDT == DataType.DATE) {
                for (String s : indexValues) {
                    indexData.add(JDateUtil.parseDateTime(s, dtFormatter));
                }
                index = new DateTimeIndex(indexData);
                //((DateTimeIndex) index).setDateTimeFormatter(dtFormatter);
                if (indexFormat != null) {
                    index.setFormat(indexFormat);
                } else {
                    index.updateFormat();
                }
            } else {
                for (String s : indexValues) {
                    indexData.add(DataConvert.convertStringTo(s, idxDT, null));
                }
                index = Index.factory(indexData);
                index.updateFormat();
            }
        } else {
            index = new IntIndex(rn);
            index.updateFormat();
        }

        DataFrame df;
        if (cols.isSameDataType()) {
            Array data = Array.factory(cols.get(0).dataType, new int[]{rn, colNum});
            List vv;
            col = cols.get(0);
            for (int i = 0; i < colNum; i++) {
                vv = values.get(i);
                String v;
                for (int j = 0; j < vv.size(); j++) {
                    v = (String) vv.get(j);
                    data.setObject(j * colNum + i, col.convertStringTo(v));
                }
            }
            df = new DataFrame(data, index, cols);
        } else {
            List<Array> data = new ArrayList<>();
            Array a;
            List vv;
            for (int i = 0; i < colNum; i++) {
                vv = values.get(i);
                col = cols.get(i);
                DataType dt = col.getDataType();
                a = Array.factory(dt, new int[]{rn});
                String v;
                for (int j = 0; j < vv.size(); j++) {
                    v = (String) vv.get(j);
                    a.setObject(j, col.convertStringTo(v));
                }
                data.add(a);
            }

            df = new DataFrame(data, index, cols);
        }
        if (names != null) {
            df.setColumns(names);
        }

        return df;
    }

    /**
     * Read data frame from ASCII file
     *
     * @param fileName File name
     * @param delimiter Delimiter
     * @param skipRows Number of lines to skip at begining of the file
     * @param formatSpec Format specifiers string
     * @param encoding Fle encoding
     * @param indexCol Column to be used as index
     * @param indexFormat Index format
     * @param names Column names
     * @param header Row number to use as the column names
     * @param skipFooter Number of lines at bottom of file to skip
     * @param usecolsin Filter columns by column names or indices
     * @return DataFrame object
     * @throws java.io.FileNotFoundException
     */
    public static DataFrame readTable(String fileName, String delimiter, int skipRows, String formatSpec, String encoding,
            int indexCol, String indexFormat, List<String> names, Integer header, int skipFooter,
            List<Object> usecolsin) throws FileNotFoundException, IOException, Exception {
        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
        if (skipRows > 0) {
            for (int i = 0; i < skipRows; i++) {
                sr.readLine();
            }
        }

        String title = sr.readLine().trim();
        if (encoding.equals("UTF8")) {
            if (title.startsWith("\uFEFF")) {
                title = title.substring(1);
            }
        }
        String[] titleArray1 = GlobalUtil.split(title, delimiter);
        List<Integer> usecols = new ArrayList<>();
        if (usecolsin.get(0) instanceof Integer) {
            for (Object o : usecolsin) {
                usecols.add((Integer) o);
            }
        } else {
            int idx;
            List<String> titleArray2 = new ArrayList(Arrays.asList(titleArray1));
            for (Object o : usecolsin) {
                idx = titleArray2.indexOf((String) o);
                if (idx >= 0) {
                    usecols.add(idx);
                }
            }
        }
        List<String> titleArray = new ArrayList<>();
        for (int i = 0; i < titleArray1.length; i++) {
            if (i != indexCol && usecols.contains(i)) {
                titleArray.add(titleArray1[i]);
            }
        }

        if (titleArray.isEmpty()) {
            System.out.println("File Format Error!");
            sr.close();
            return null;
        }

        int colNum = titleArray.size();
        if (header == null) {
            for (int i = 0; i < colNum; i++) {
                titleArray.set(i, "Col_" + String.valueOf(i));
            }
        }

        //Get fields
        ColumnIndex cols = new ColumnIndex();
        Column col;
        List<List> values = new ArrayList<>();
        String[] colFormats;
        if (formatSpec == null) {
            colFormats = new String[colNum];
            for (int i = 0; i < colNum; i++) {
                colFormats[i] = "C";
            }
        } else {
            colFormats = formatSpec.split("%");
        }

        int idx = 0;
        boolean isBreak = false;
        for (String colFormat : colFormats) {
            if (colFormat.isEmpty()) {
                continue;
            }

            int num = 1;
            if (colFormat.length() > 1 && !colFormat.substring(0, 1).equals("{")) {
                int index = colFormat.indexOf("{");
                if (index < 0) {
                    index = colFormat.length() - 1;
                }
                num = Integer.parseInt(colFormat.substring(0, index));
                colFormat = colFormat.substring(index);
            }
            for (int i = 0; i < num; i++) {
                String colName = titleArray.get(idx).trim();
                if (colFormat.equals("C") || colFormat.equals("s")) //String
                {
                    col = new Column(colName, DataType.STRING);
                } else if (colFormat.equals("i")) //Integer
                {
                    col = new Column(colName, DataType.INT);
                } else if (colFormat.equals("f")) //Float
                {
                    col = new Column(colName, DataType.FLOAT);
                } else if (colFormat.equals("d")) //Double
                {
                    col = new Column(colName, DataType.DOUBLE);
                } else if (colFormat.equals("B")) //Boolean
                {
                    col = new Column(colName, DataType.BOOLEAN);
                } else if (colFormat.substring(0, 1).equals("{")) {    //Date
                    int eidx = colFormat.indexOf("}");
                    String formatStr = colFormat.substring(1, eidx);
                    col = new Column(colName, DataType.DATE);
                    col.setFormat(formatStr);
                } else {
                    col = new Column(colName, DataType.STRING);
                }
                cols.add(col);
                values.add(new ArrayList<>());
                idx += 1;
                if (idx == colNum) {
                    isBreak = true;
                    break;
                }
            }
            if (isBreak) {
                break;
            }
        }

        if (idx < colNum) {
            for (int i = idx; i < colNum; i++) {
                cols.add(new Column(titleArray.get(i), DataType.STRING));
                values.add(new ArrayList<>());
            }
        }

        String[] dataArray;
        List<String> indexValues = new ArrayList<>();
        String line;
        if (header == null) {
            line = title;
        } else {
            line = sr.readLine();
        }
        while (line != null) {
            line = line.trim();
            if (line.isEmpty()) {
                line = sr.readLine();
                continue;
            }
            dataArray = GlobalUtil.split(line, delimiter);
            int cn = 0;
            for (int i = 0; i < dataArray.length; i++) {
                if (cn < colNum) {
                    if (i == indexCol) {
                        indexValues.add(dataArray[i]);
                    } else if (usecols.contains(i)) {
                        values.get(cn).add(dataArray[i]);
                        cn++;
                    }
                } else {
                    break;
                }
            }
            if (cn < colNum) {
                for (int i = cn; i < colNum; i++) {
                    values.get(i).add("");
                }
            }

            line = sr.readLine();
        }
        sr.close();

        int rn = values.get(0).size();
        if (skipFooter > 0) {
            if (indexCol >= 0) {
                indexValues = indexValues.subList(0, rn - skipFooter);
            }
            for (int i = 0; i < colNum; i++) {
                values.set(i, values.get(i).subList(0, rn - skipFooter));
            }
        }
        rn = values.get(0).size();
        Index index;
        if (indexCol >= 0) {
            DataType idxDT;
            DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_DATE_TIME;
            if (indexFormat != null) {
                if (indexFormat.substring(0, 1).equals("%")) {
                    indexFormat = indexFormat.substring(1);
                }
                idxDT = DataConvert.getDataType(indexFormat);
                if (idxDT == DataType.DATE) {
                    indexFormat = DataConvert.getDateFormat(indexFormat);
                    dtFormatter = DateTimeFormatter.ofPattern(indexFormat);
                }
            } else {
                idxDT = DataConvert.detectDataType(indexValues, 10, null);
                if (idxDT == DataType.DATE) {
                    dtFormatter = TypeUtils.getDateTimeFormatter(indexValues.get(0));
                }
            }

            List indexData = new ArrayList<>();
            if (idxDT == DataType.DATE) {
                for (String s : indexValues) {
                    indexData.add(JDateUtil.parseDateTime(s, dtFormatter));
                }
                index = new DateTimeIndex(indexData);
                //((DateTimeIndex) index).setDateTimeFormatter(dtFormatter);
                if (indexFormat != null) {
                    index.format = indexFormat;
                } else {
                    index.updateFormat();
                }
            } else {
                for (String s : indexValues) {
                    indexData.add(DataConvert.convertStringTo(s, idxDT, null));
                }
                index = Index.factory(indexData);
                index.updateFormat();
            }
        } else {
            index = new IntIndex(rn);
            index.updateFormat();
        }

        DataFrame df;
        if (cols.isSameDataType()) {
            Array data = Array.factory(cols.get(0).dataType, new int[]{rn, colNum});
            List vv;
            col = cols.get(0);
            for (int i = 0; i < colNum; i++) {
                vv = values.get(i);
                String v;
                for (int j = 0; j < vv.size(); j++) {
                    v = (String) vv.get(j);
                    data.setObject(j * colNum + i, col.convertStringTo(v));
                }
            }
            df = new DataFrame(data, index, cols);
        } else {
            List<Array> data = new ArrayList<>();
            Array a;
            List vv;
            for (int i = 0; i < colNum; i++) {
                vv = values.get(i);
                col = cols.get(i);
                DataType dt = col.getDataType();
                a = Array.factory(dt, new int[]{rn});
                String v;
                for (int j = 0; j < vv.size(); j++) {
                    v = (String) vv.get(j);
                    a.setObject(j, col.convertStringTo(v));
                }
                data.add(a);
            }

            df = new DataFrame(data, index, cols);
        }
        if (names != null) {
            df.setColumns(names);
        }

        return df;
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
        for (int i = 0; i < this.size(); i++) {
            if (str.isEmpty()) {
                str = this.columns.get(i).getName();
            } else {
                str = str + delimiter + this.columns.get(i).getName();
            }
        }
        sw.write(str);

        String line, vstr;
        List<String> formats = new ArrayList<>();
        for (Column col : this.columns) {
            if (col.getDataType() == DataType.FLOAT || col.getDataType() == DataType.DOUBLE) {
                formats.add(floatFormat == null ? col.getFormat() : floatFormat);
            } else {
                formats.add(col.getFormat());
            }
        }
        for (int j = 0; j < this.length(); j++) {
            line = "";
            if (index) {
                line = this.index.toString(j, idxFormat).trim();
            }
            for (int i = 0; i < this.size(); i++) {
                if (formats.get(i) == null) {
                    vstr = this.getValue(j, i).toString();
                } else {
                    vstr = String.format(formats.get(i), this.getValue(j, i));
                }
                if (line.isEmpty()) {
                    line = vstr;
                } else {
                    line += delimiter + vstr;
                }
            }
            sw.newLine();
            sw.write(line);
        }
        sw.flush();
        sw.close();
    }

    public <V> DataFrame describe() {
        return Aggregation.describe(new Grouping().apply(this, new Aggregation.Describe<V>()));
    }

    public DataFrame sortBy(final List<String> cols, final List<Boolean> ascendings) {
        final Map<Integer, SortDirection> sortCols = new LinkedHashMap<>();
        for (int i = 0; i < cols.size(); i++) {
            String col = cols.get(i);
            boolean ascending = ascendings.get(i);
            final SortDirection dir = ascending
                    ? SortDirection.ASCENDING : SortDirection.DESCENDING;
            final int c = columns.indexOfName(col);
            sortCols.put(c, dir);
        }
        return Sorting.sort(this, sortCols);
    }

    public DataFrame sortByIndex(boolean ascending) {
        final SortDirection dir = ascending
                ? SortDirection.ASCENDING : SortDirection.DESCENDING;
        return Sorting.sortIndex(this, dir);
    }

    public DataFrame sortBy(final Object... cols) {
        final Map<Integer, SortDirection> sortCols = new LinkedHashMap<>();
        for (final Object col : cols) {
            final String str = col instanceof String ? String.class.cast(col) : "";
            final SortDirection dir = str.startsWith("-")
                    ? SortDirection.DESCENDING : SortDirection.ASCENDING;
            final int c = columns.indexOf(str.startsWith("-") ? str.substring(1) : col);
            sortCols.put(c, dir);
        }
        return Sorting.sort(this, sortCols);
    }

    public DataFrame sortBy(boolean ascending, final Integer... cols) {
        final Map<Integer, SortDirection> sortCols = new LinkedHashMap<>();
        for (final int c : cols) {
            final SortDirection dir = ascending
                    ? SortDirection.ASCENDING : SortDirection.DESCENDING;
            sortCols.put(Math.abs(c), dir);
        }
        return Sorting.sort(this, sortCols);
    }

    public <V> DataFrame sortBy(final Comparator<List<V>> comparator) {
        return Sorting.sort(this, comparator);
    }

    /**
     * Group the data frame rows using the specified key function.
     *
     * @param function the function to reduce rows to grouping keys
     * @return the grouping
     */
    public DataFrameGroupBy groupBy(final KeyFunction function) {
        return new DataFrameGroupBy(new Grouping(this, function), this);
    }

    /**
     * Group the data frame rows using columns
     *
     * @param columns The columns
     * @return The grouping
     */
    public DataFrameGroupBy groupBy(final Integer... columns) {
        return new DataFrameGroupBy(new Grouping(this, columns), this);
    }

    /**
     * Group the data frame rows using columns
     *
     * @param columns The columns
     * @return The grouping
     */
    public DataFrameGroupBy groupBy(final Object... columns) {
        Integer[] icols = this.columns.indices(columns);
        return groupBy(icols);
    }

    /**
     * Group the data frame rows using columns
     *
     * @param columns The columns
     * @return The grouping
     */
    public DataFrameGroupBy groupBy(final List<Object> columns) {
        Integer[] icols = this.columns.indices(columns);
        return groupBy(icols);
    }

    /**
     * Group the data frame rows using the specified key function.
     *
     * @param function the function to reduce rows to grouping keys
     * @return the grouping
     */
    public DataFrameGroupBy groupByIndex(final WindowFunction function) {
        ((DateTimeIndex) index).setResamplPeriod(function.getPeriod());
        return new DataFrameGroupBy(new Grouping(this, function), this);
    }

    /**
     * Group the data frame rows using the specified key function.
     *
     * @param pStr Period string
     * @return the grouping
     */
    public DataFrameGroupBy groupByIndex(final String pStr) {
        TemporalAmount period = JDateUtil.getPeriod(pStr);
        WindowFunction function = new WindowFunction(period);
        return groupByIndex(function);
    }

    /**
     * Apply a function
     *
     * @param <V>
     * @param function The function
     * @return Result data frame
     */
    public <V> DataFrame apply(final Function<?, ?> function) {
        DataFrame r = new Grouping().apply(this, function);
        if (r.index instanceof DateTimeIndex) {
            ((DateTimeIndex) r.getIndex()).setPeriod(((DateTimeIndex) this.index).getResamplePeriod());
        }
        return r;
    }

    /**
     * Compute the sum of the numeric columns.
     *
     * @return the new data frame
     */
    public DataFrame count() {
        DataFrame r = this.apply(new Aggregation.Count());
        return r;
    }

    /**
     * Compute the sum of the numeric columns.
     *
     * @return the new data frame
     */
    public DataFrame sum() {
        DataFrame r = this.apply(new Aggregation.Sum());
        return r;
    }

    /**
     * Compute the mean of the numeric columns.
     *
     * @return the new data frame
     */
    public DataFrame mean() {
        DataFrame r = this.apply(new Aggregation.Mean());
        return r;
    }

    /**
     * Compute the minimum of the numeric columns.
     *
     * @return the new data frame
     */
    public DataFrame min() {
        DataFrame r = this.apply(new Aggregation.Min());
        return r;
    }

    /**
     * Compute the Maximum of the numeric columns.
     *
     * @return the new data frame
     */
    public DataFrame max() {
        DataFrame r = this.apply(new Aggregation.Max());
        return r;
    }

    /**
     * Compute the median of the numeric columns.
     *
     * @return the new data frame
     */
    public DataFrame median() {
        DataFrame r = this.apply(new Aggregation.Median());
        return r;
    }
    
    /**
     * Compute the median of the numeric columns.
     *
     * @return the new data frame
     */
    public DataFrame stdDev() {
        DataFrame r = this.apply(new Aggregation.StdDev<>());
        return r;
    }

    @Override
    public Object clone() {
        Object rdata;
        if (this.array2D) {
            rdata = ((Array) this.data).copy();
        } else {
            rdata = new ArrayList<Array>();
            for (Array a : (List<Array>) this.data) {
                ((ArrayList<Array>) rdata).add(a.copy());
            }
        }
        Index rIndex = (Index) this.index.clone();
        ColumnIndex cols = (ColumnIndex) this.columns.clone();
        DataFrame df;
        if (rdata instanceof Array) {
            df = new DataFrame((Array) rdata, rIndex, cols);
        } else {
            df = new DataFrame((ArrayList) rdata, rIndex, cols);
        };
        return df;
    }
    // </editor-fold>
}
