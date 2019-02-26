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
package org.meteoinfo.table;

import org.meteoinfo.data.DataTypes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yaqiang Wang
 */
public class DataColumn {

    private boolean readOnly;
    private DataTable table;
    private String columnName;
    private String captionName;
    private int columnIndex;
    private DataTypes dataType;
    private String format;
    private boolean joined = false;
    //private String dataTypeName;

    /**
     * Constructor
     */
    public DataColumn() {
        this("default1");
    }

    /**
     * Constructor
     *
     * @param dataType Data type
     */
    public DataColumn(DataTypes dataType) {
        this("default1", dataType);
    }

    /**
     * Constructor
     *
     * @param columnName Column name
     */
    public DataColumn(String columnName) {
        this(columnName, DataTypes.Integer);
    }

    /**
     * Constructor
     *
     * @param columnName Column name
     * @param dataType Data type
     */
    public DataColumn(String columnName, DataTypes dataType) {
        this.dataType = dataType;
        this.columnName = columnName;
        if (this.dataType == DataTypes.Date){
            this.format = "YYYYMMddHH";
        }
    }

    /**
     * Constructor
     *
     * @param columnName Column name
     * @param dataType Data type
     * @param format Data format string
     */
    public DataColumn(String columnName, DataTypes dataType, String format) {
        this.dataType = dataType;
        this.columnName = columnName;
        this.format = format;
    }

    /**
     * Get column name
     *
     * @return Column name
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Set Column name
     *
     * @param columnName Column name
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Get caption name
     *
     * @return Caption name
     */
    public String getCaptionName() {
        return captionName;
    }

    /**
     * Set caption name
     *
     * @param captionName Caption name
     */
    public void setCaptionName(String captionName) {
        this.captionName = captionName;
    }

    /**
     * Get if is read only
     *
     * @return Boolean
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Set if is read only
     *
     * @param readOnly Boolean
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Get format string
     *
     * @return Format string
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * Set format string
     *
     * @param value Format string
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Get if is joined
     *
     * @return Boolean
     */
    public boolean isJoined() {
        return this.joined;
    }

    /**
     * Set if is joined
     *
     * @param value Boolean
     */
    public void setJoined(boolean value) {
        this.joined = value;
    }

    /**
     * Get data table
     *
     * @return The data table
     */
    public DataTable getTable() {
        return this.table;
    }

    /**
     * Set data table
     *
     * @param table The data table
     */
    public void setTable(DataTable table) {
        this.table = table;
    }

    /**
     * Set data type
     *
     * @param dataType Data type
     */
    public void setDataType(DataTypes dataType) {
        this.dataType = dataType;
        if (dataType == DataTypes.Date){
            this.format = "YYYYMMddHH";
        }
    }

    /**
     * Get data type
     *
     * @return The data type
     */
    public DataTypes getDataType() {
        return dataType;
    }

    /**
     * Set column index
     *
     * @param columnIndex Column index
     */
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * Get column index
     *
     * @return The column index
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Get data type name
     *
     * @return Data type name
     */
    public String getDataTypeName() {
        return dataType.toString();
    }

    /**
     * Convert input data to current data type
     *
     * @param value Object value
     * @return Result object
     */
    public Object convertTo(Object value) {
        if (value == null) {
            switch (this.dataType) {
                case Integer:
                    return Integer.MIN_VALUE;
                case Float:
                    return Float.NaN;
                case Double:
                    return Double.NaN;
                case Boolean:
                    return false;
                case String:
                    return "";
                default:
                    return value;
            }
        } else {
            switch (this.dataType) {
                case Integer:
                    if (!(value instanceof Integer)) {
                        String vStr = value.toString();
                        if (vStr.isEmpty())
                            return Integer.MIN_VALUE;
                        return Integer.valueOf(vStr);
                    }
                    break;
                case Double:
                    if (!(value instanceof Double)) {
                        String vStr = value.toString();
                        if (vStr.isEmpty() || vStr.equalsIgnoreCase("nan")) {
                            return Double.NaN;
                        } else {
                            return Double.valueOf(vStr);
                        }
                    }
                    break;
                case Float:
                    if (!(value instanceof Float)) {
                        String vStr = value.toString();
                        if (vStr.isEmpty() || vStr.equalsIgnoreCase("nan")) {
                            return Float.NaN;
                        } else {
                            try {
                                float v = Float.valueOf(vStr);
                                return v;
                            } catch (Exception e){
                                return Float.NaN;
                            }                            
                        }
                    }
                    break;
                case Boolean:
                    if (!(value instanceof Boolean)) {
                        String vStr = value.toString();
                        if (vStr.isEmpty())
                            return false;
                        return Boolean.valueOf(vStr);
                    }
                    break;
                case Date:
                    if (!(value instanceof Date)) {
                        String vStr = value.toString();
                        if (vStr.isEmpty()) {
                            return null;
                        }
                        SimpleDateFormat dformat = new SimpleDateFormat(this.format);
                        try {
                            return dformat.parse(vStr);
                        } catch (ParseException ex) {
                            Logger.getLogger(DataColumn.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;
            }
        }

        return value;
    }

    /**
     * Convert to string
     *
     * @return String
     */
    @Override
    public String toString() {
        return this.columnName;
    }

    /**
     * Clone
     *
     * @return Cloned DataColumn object
     */
    @Override
    public Object clone() {
        DataColumn col = new DataColumn();
        col.captionName = this.captionName;
        col.columnIndex = this.columnIndex;
        col.columnName = this.columnName;
        col.dataType = this.dataType;
        col.readOnly = this.readOnly;
        col.format = this.format;

        return col;
    }
}
