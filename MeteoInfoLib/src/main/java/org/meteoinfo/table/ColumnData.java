/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.table;

import org.meteoinfo.ndarray.DataType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.util.JDateUtil;

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
            case INT:
                data = new ArrayList<Integer>();
                break;
            case FLOAT:
                data = new ArrayList<Float>();
                break;
            case DOUBLE:
                data = new ArrayList<Double>();
                break;
            case STRING:
                data = new ArrayList<String>();
                break;
            case DATE:
                data = new ArrayList<LocalDateTime>();
                break;
            case BOOLEAN:
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
    public ColumnData(String colName, DataType type) {
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
    public DataType getDataType() {
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
            case INT:
                this.addData((Integer) value);
                break;
            case FLOAT:
                this.addData((Float) value);
                break;
            case DOUBLE:
                this.addData((Double) value);
                break;
            case STRING:
                this.addData((String) value);
                break;
            case DATE:
                this.addData((LocalDateTime) value);
                break;
            case BOOLEAN:
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
    public void addData(LocalDateTime value) {
        ((List<LocalDateTime>) data).add(value);
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
            case INT:
                return (Integer) data.get(idx);
            case FLOAT:
                return (Float) data.get(idx);
            case DOUBLE:
                return (Double) data.get(idx);
            case STRING:
                return (String) data.get(idx);
            case DATE:
                return (LocalDateTime) data.get(idx);
            case BOOLEAN:
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
        if (dataColumn.getDataType() == DataType.DOUBLE) {
            return ((List<Number>) data);
        } else {
            List<Number> values = new ArrayList<>();
            switch (dataColumn.getDataType()) {
                case INT:
                    for (int v : (List<Integer>) data) {
                        values.add(v);
                    }
                    break;
                case FLOAT:
                    for (Object v : (List<Object>) data) {
                        if (v == null){
                            values.add(Float.NaN);
                        } else {
                            values.add((float)v);
                        }
                    }
                    break;
                case STRING:
                    for (String v : (List<String>)data){
                        if (v.isEmpty())
                            values.add(Double.NaN);
                        else
                            values.add(Double.parseDouble(v));
                    }
                    break;
                case DATE:
                    for (LocalDateTime v : (List<LocalDateTime>)data){
                        values.add(JDateUtil.toOADate(v));
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
            case INT:
                for (int v : (List<Integer>) data) {
                    mean += v;
                    n++;
                }
                break;
            case FLOAT:
                for (float v : (List<Float>) data) {
                    if (v != Float.NaN) {
                        mean += v;
                        n++;
                    }
                }
                break;
            case DOUBLE:
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
        DataType thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataType inType = this.getDataType(value);
        if (!inType.isNumeric()){
            return null;
        }
        
        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case INT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.INT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((float)(Integer) this.getValue(i) + (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) + (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) + (Double) value);
                        }
                        break;
                }
                break;
            case FLOAT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) + (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) + (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) + (Double) value);
                        }
                        break;
                }
                break;
            case DOUBLE:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) + (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) + (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
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
        DataType thisType = dataColumn.getDataType();
        if (thisType == DataType.DATE || thisType == DataType.BOOLEAN) {
            return null;
        }

        DataType inType = colData.getDataType();
        if (inType == DataType.DATE || inType == DataType.BOOLEAN) {
            return null;
        }

        if (thisType == DataType.STRING && inType != DataType.STRING) {
            return null;
        }

        if (inType == DataType.STRING && thisType != DataType.STRING) {
            return null;
        }

        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case INT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.INT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) + (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) + (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) + (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case FLOAT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) + (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) + (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) + (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case DOUBLE:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) + (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) + (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) + (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case STRING:
                rColData = new ColumnData(colName, DataType.STRING);
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
        DataType thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataType inType = this.getDataType(value);
        if (!inType.isNumeric()){
            return null;
        }
        
        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case INT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.INT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((float)(Integer) this.getValue(i) - (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) - (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) - (Double) value);
                        }
                        break;
                }
                break;
            case FLOAT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) - (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) - (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) - (Double) value);
                        }
                        break;
                }
                break;
            case DOUBLE:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) - (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) - (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
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
        DataType thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataType inType = colData.getDataType();
        if (!inType.isNumeric()) {
            return null;
        }

        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case INT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.INT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) - (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) - (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) - (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case FLOAT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) - (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) - (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) - (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case DOUBLE:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) - (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) - (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
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
        DataType thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataType inType = this.getDataType(value);
        if (!inType.isNumeric()){
            return null;
        }
        
        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case INT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.INT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((float)(Integer) this.getValue(i) * (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) * (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) * (Double) value);
                        }
                        break;
                }
                break;
            case FLOAT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) * (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) * (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) * (Double) value);
                        }
                        break;
                }
                break;
            case DOUBLE:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) * (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) * (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
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
        DataType thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataType inType = colData.getDataType();
        if (!inType.isNumeric()) {
            return null;
        }

        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case INT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.INT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) * (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) * (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) * (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case FLOAT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) * (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) * (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) * (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case DOUBLE:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) * (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) * (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
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
        DataType thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataType inType = colData.getDataType();
        if (!inType.isNumeric()) {
            return null;
        }

        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case INT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((float)(Integer) this.getValue(i) / (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) / (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Integer) this.getValue(i) / (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case FLOAT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) / (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) / (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Float) this.getValue(i) / (Double) colData.getValue(i));
                            }
                        }
                        break;
                }
                break;
            case DOUBLE:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) / (Integer) colData.getValue(i));
                            }
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            if (i < colData.size()) {
                                rColData.addData((Double) this.getValue(i) / (Float) colData.getValue(i));
                            }
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
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
        DataType thisType = dataColumn.getDataType();
        if (!thisType.isNumeric()) {
            return null;
        }

        DataType inType = this.getDataType(value);
        if (!inType.isNumeric()){
            return null;
        }
        
        ColumnData rColData = null;
        int i;
        switch (thisType) {
            case INT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((float)(Integer) this.getValue(i) / (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) / (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Integer) this.getValue(i) / (Double) value);
                        }
                        break;
                }
                break;
            case FLOAT:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) / (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.FLOAT);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) / (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Float) this.getValue(i) / (Double) value);
                        }
                        break;
                }
                break;
            case DOUBLE:
                switch (inType) {
                    case INT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) / (Integer) value);
                        }
                        break;
                    case FLOAT:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) / (Float) value);
                        }
                        break;
                    case DOUBLE:
                        rColData = new ColumnData(colName, DataType.DOUBLE);
                        for (i = 0; i < data.size(); i++) {
                            rColData.addData((Double) this.getValue(i) / (Double) value);
                        }
                        break;
                }
                break;            
        }

        return rColData;
    }
    
    private DataType getDataType(Object value){
        if (value instanceof Integer)
            return DataType.INT;
        else if (value instanceof Float)
            return DataType.FLOAT;
        else if (value instanceof Double)
            return DataType.DOUBLE;
        else if (value instanceof Boolean)
            return DataType.BOOLEAN;
        else if (value instanceof String)
            return DataType.STRING;
        else if (value instanceof LocalDateTime)
            return DataType.DATE;
        else
            return null;
    }
    // </editor-fold>
}
