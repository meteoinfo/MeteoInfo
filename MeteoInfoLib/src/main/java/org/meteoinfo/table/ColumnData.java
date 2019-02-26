/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.table;

import org.meteoinfo.data.DataTypes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.meteoinfo.global.util.DateUtil;

/**
 *
 * @author yaqiang
 */
public class ColumnData {

    // <editor-fold desc="Variables">
    private DataColumn dataColumn;
    private List data;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     *
     * @param col The data column
     */
    public ColumnData(DataColumn col) {
        dataColumn = col;
        switch (col.getDataType()) {
            case Integer:
                data = new ArrayList<Integer>();
                break;
            case Float:
                data = new ArrayList<Float>();
                break;
            case Double:
                data = new ArrayList<Double>();
                break;
            case String:
                data = new ArrayList<String>();
                break;
            case Date:
                data = new ArrayList<Date>();
                break;
            case Boolean:
                data = new ArrayList<Boolean>();
                break;
        }
    }

    /**
     * Constructor
     *
     * @param colName Data column name
     * @param type Data type
     */
    public ColumnData(String colName, DataTypes type) {
        this(new DataColumn(colName, type));
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get data column
     *
     * @return Data column
     */
    public DataColumn getDataColumn() {
        return this.dataColumn;
    }

    /**
     * Set data column
     *
     * @param value Data column
     */
    public void setDataColumn(DataColumn value) {
        dataColumn = value;
    }

    /**
     * Get data list
     *
     * @return Data list
     */
    public List getData() {
        return data;
    }

    /**
     * Set data list
     *
     * @param value Data list
     */
    public void setData(List value) {
        data = value;
    }

    /**
     * Get data type
     *
     * @return Data type
     */
    public DataTypes getDataType() {
        return dataColumn.getDataType();
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get size of the data
     *
     * @return Size of the data
     */
    public int size() {
        return data.size();
    }

    /**
     * Add a object data
     *
     * @param value Data value
     */
    public void addData(Object value) {
        switch (dataColumn.getDataType()) {
            case Integer:
                this.addData((Integer) value);
                break;
            case Float:
                this.addData((Float) value);
                break;
            case Double:
                this.addData((Double) value);
                break;
            case String:
                this.addData((String) value);
                break;
            case Date:
                this.addData((Date) value);
                break;
            case Boolean:
                this.addData((Boolean) value);
                break;
        }
    }

    /**
     * Add a double data value
     *
     * @param value Data value
     */
    public void addData(Double value) {
        ((List<Double>) data).add(value);
    }

    /**
     * Add a float data value
     *
     * @param value Data value
     */
    public void addData(Float value) {
        ((List<Float>) data).add(value);
    }

    /**
     * Add a integer data value
     *
     * @param value Data value
     */
    public void addData(Integer value) {
        ((List<Integer>) data).add(value);
    }

    /**
     * Add a string data value
     *
     * @param value Data value
     */
    public void addData(String value) {
        ((List<String>) data).add(value);
    }

    /**
     * Add a date data value
     *
     * @param value Data value
     */
    public void addData(Date value) {
        ((List<Date>) data).add(value);
    }

    /**
     * Add a boolean data value
     *
     * @param value Data value
     */
    public void addData(boolean value) {
        ((List<Boolean>) data).add(value);
    }

    /**
     * Get data value
     *
     * @param idx Data index
     * @return Data value
     */
    public Object getValue(int idx) {
        switch (dataColumn.getDataType()) {
            case Integer:
                return (Integer) data.get(idx);
            case Float:
                return (Float) data.get(idx);
            case Double:
                return (Double) data.get(idx);
            case String:
                return (String) data.get(idx);
            case Date:
                return (Date) data.get(idx);
            case Boolean:
                return (Boolean) data.get(idx);
        }

        return null;
    }
    
    /**
     * Get string data list
     * @return String data list
     */
    public List<String> getDataStrings(){
        List<String> r = new ArrayList<>();
        for (Object v : data){
            r.add(v.toString());
        }
        
        return r;
    }

    /**
     * Get number data list
     *
     * @return Number data list
     */
    public List<Number> getDataValues() {
        if (dataColumn.getDataType() == DataTypes.Double) {
            return ((List<Number>) data);
        } else {
            List<Number> values = new ArrayList<>();
            switch (dataColumn.getDataType()) {
                case Integer:
                    for (int v : (List<Integer>) data) {
                        values.add(v);
                    }
                    break;
                case Float:
                    for (Object v : (List<Object>) data) {
                        if (v == null){
                            values.add(Float.NaN);
                        } else {
                            values.add((float)v);
                        }
                    }
                    break;
                case String:
                    for (String v : (List<String>)data){
                        if (v.isEmpty())
                            values.add(Double.NaN);
                        else
                            values.add(Double.parseDouble(v));
                    }
                    break;
                case Date:
                    for (Date v : (List<Date>)data){
                        values.add(DateUtil.toOADate(v));
                    }
                    break;
            }
            return values;
        }
    }
    
    /**
     * Get valid number data list
     * @return Number data list
     */
    public List<Number> getValidDataValues(){
        List<Number> values = this.getDataValues();
        for (int i = 0, len = values.size(); i < len; i++) { 
            if (Double.isNaN(values.get(i).doubleValue())) {                
                values.remove(i); 
                len--; 
                i--;  
            }
        } 
        
        return values;
    }

    /**
     * Contains function
     *
     * @param value The object value
     * @return Is contains or not
     */
    public boolean contains(Object value) {
        return data.contains(value);
    }        

    /**
     * Mean function
     *
     * @return Mean value
     */
    public Double mean() {
        double mean = 0.0;
        int n = 0;
        switch (dataColumn.getDataType()) {
            case Integer:
                for (int v : (List<Integer>) data) {
                    mean += v;
                    n++;
                }
                break;
            case Float:
                for (float v : (List<Float>) data) {
                    if (v != Float.NaN) {
                        mean += v;
                        n++;
                    }
                }
                break;
            case Double:
                for (double v : (List<Double>) data) {
                    if (v != Double.NaN) {
                        mean += v;
                        n++;
                    }
                }
                break;
        }

        if (n > 0) {
            mean = mean / n;
            return mean;
        } else {
            return Double.NaN;
        }
    }
    
    /**
     * Add function
     *
     * @param value The object value to add
     * @param colName New column data name
     * @return Result column data
     */
    public ColumnData add(Object value, String colName) {
        DataTypes thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataTypes inType = this.getDataType(value);
        if (!inType.isNumeric()){
            return null;
        }
        
        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case Integer:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Integer);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((float)(Integer) this.getValue(i) + (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) + (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) + (Double) value);
                        }
                        break;
                }
                break;
            case Float:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) + (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) + (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) + (Double) value);
                        }
                        break;
                }
                break;
            case Double:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) + (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) + (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) + (Double) value);
                        }
                        break;
                }
                break;            
        }

        return rColData;
    }

    /**
     * Add function
     *
     * @param colData Anorther column data
     * @param colName New column data name
     * @return Result column data
     */
    public ColumnData add(ColumnData colData, String colName) {
        DataTypes thisType = dataColumn.getDataType();
        if (thisType == DataTypes.Date || thisType == DataTypes.Boolean) {
            return null;
        }

        DataTypes inType = colData.getDataType();
        if (inType == DataTypes.Date || inType == DataTypes.Boolean) {
            return null;
        }

        if (thisType == DataTypes.String && inType != DataTypes.String) {
            return null;
        }

        if (inType == DataTypes.String && thisType != DataTypes.String) {
            return null;
        }

        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case Integer:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Integer);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) + (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) + (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) + (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case Float:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) + (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) + (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) + (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case Double:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) + (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) + (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) + (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case String:
                rColData = new ColumnData(colName, DataTypes.String);
                for (i = 0; i < data.size(); i++) {
                    if (i < colData.size()) {
                        rColData.addData((String) this.getValue(i) + (String) colData.getValue(i));
                    }
                }
                break;
        }

        return rColData;
    }
    
    /**
     * Subtract function
     *
     * @param value The object value to subtract
     * @param colName New column data name
     * @return Result column data
     */
    public ColumnData sub(Object value, String colName) {
        DataTypes thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataTypes inType = this.getDataType(value);
        if (!inType.isNumeric()){
            return null;
        }
        
        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case Integer:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Integer);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((float)(Integer) this.getValue(i) - (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) - (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) - (Double) value);
                        }
                        break;
                }
                break;
            case Float:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) - (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) - (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) - (Double) value);
                        }
                        break;
                }
                break;
            case Double:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) - (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) - (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) - (Double) value);
                        }
                        break;
                }
                break;            
        }

        return rColData;
    }
    
    /**
     * Subtract function
     *
     * @param colData Anorther column data
     * @param colName New column data name
     * @return Result column data
     */
    public ColumnData sub(ColumnData colData, String colName) {
        DataTypes thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataTypes inType = colData.getDataType();
        if (!inType.isNumeric()) {
            return null;
        }

        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case Integer:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Integer);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) - (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) - (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) - (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case Float:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) - (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) - (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) - (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case Double:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) - (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) - (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) - (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;            
        }

        return rColData;
    }
    
    /**
     * Multiply function
     *
     * @param value The object value to multiply
     * @param colName New column data name
     * @return Result column data
     */
    public ColumnData mul(Object value, String colName) {
        DataTypes thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataTypes inType = this.getDataType(value);
        if (!inType.isNumeric()){
            return null;
        }
        
        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case Integer:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Integer);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((float)(Integer) this.getValue(i) * (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) * (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) * (Double) value);
                        }
                        break;
                }
                break;
            case Float:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) * (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) * (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) * (Double) value);
                        }
                        break;
                }
                break;
            case Double:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) * (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) * (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) * (Double) value);
                        }
                        break;
                }
                break;            
        }

        return rColData;
    }
    
    /**
     * Multiply function
     *
     * @param colData Anorther column data
     * @param colName New column data name
     * @return Result column data
     */
    public ColumnData mul(ColumnData colData, String colName) {
        DataTypes thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataTypes inType = colData.getDataType();
        if (!inType.isNumeric()) {
            return null;
        }

        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case Integer:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Integer);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) * (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) * (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) * (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case Float:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) * (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) * (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) * (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case Double:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) * (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) * (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) * (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;            
        }

        return rColData;
    }
    
    /**
     * Divide function
     *
     * @param colData Anorther column data
     * @param colName New column data name
     * @return Result column data
     */
    public ColumnData div(ColumnData colData, String colName) {
        DataTypes thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataTypes inType = colData.getDataType();
        if (!inType.isNumeric()) {
            return null;
        }

        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case Integer:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((float)(Integer) this.getValue(i) / (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) / (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) / (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case Float:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) / (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) / (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) / (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case Double:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) / (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) / (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) / (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;            
        }

        return rColData;
    }
    
    /**
     * Divide function
     *
     * @param value The object value to divide
     * @param colName New column data name
     * @return Result column data
     */
    public ColumnData div(Object value, String colName) {
        DataTypes thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataTypes inType = this.getDataType(value);
        if (!inType.isNumeric()){
            return null;
        }
        
        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case Integer:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((float)(Integer) this.getValue(i) / (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) / (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) / (Double) value);
                        }
                        break;
                }
                break;
            case Float:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) / (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Float);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) / (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) / (Double) value);
                        }
                        break;
                }
                break;
            case Double:
                switch (inType) {
                    case Integer:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) / (Integer) value);
                        }
                        break;
                    case Float:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) / (Float) value);
                        }
                        break;
                    case Double:
                        rColData = new ColumnData(colName, DataTypes.Double);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) / (Double) value);
                        }
                        break;
                }
                break;            
        }

        return rColData;
    }
    
    private DataTypes getDataType(Object value){
        if (value instanceof Integer)
            return DataTypes.Integer;
        else if (value instanceof Float)
            return DataTypes.Float;
        else if (value instanceof Double)
            return DataTypes.Double;
        else if (value instanceof Boolean)
            return DataTypes.Boolean;
        else if (value instanceof String)
            return DataTypes.String;
        else if (value instanceof Date)
            return DataTypes.Date;
        else
            return null;
    }
    // </editor-fold>
}
