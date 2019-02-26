/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class ColumnIndex extends Index<Column> {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    // </editor-fold>
    // <editor-fold desc="Methods">
    
    /**
     * Get column names
     * @return Column names
     */
    public List<String> getNames(){
        List<String> colNames = new ArrayList<>();
        for (Column col : this.getValues()){
            colNames.add(col.getName());
        }
        
        return colNames;
    }
    
    /**
     * Get Column data types
     * @return Column data types
     */
    public List<DataType> getDataTypes(){
        List<DataType> dTypes = new ArrayList<>();
        for (Column col : (List<Column>)this.getValues()){
            dTypes.add(col.getDataType());
        }
        
        return dTypes;
    }
    
    /**
     * Get column data formats
     * @return Column data formats
     */
    public List<String> getFormats(){
        List<String> formats = new ArrayList<>();
        for (Column col : (List<Column>)this.getValues()){
            formats.add(col.getFormat());
        }
        
        return formats;
    }
    
    /**
     * Index of column name
     * @param colName Column name
     * @return Index value
     */
    public int indexOfName(String colName) {
        return this.getNames().indexOf(colName);
    }
    
    /**
     * Index of column names
     * @param colNames Column names
     * @return Index list
     */
    public List<Integer> indexOfName(List<String> colNames) {
        List<Integer> r = new ArrayList<>();
        for (String colName : colNames){
            r.add(indexOfName(colName));
        }
        return r;
    }
    
    /**
     * Get indices
     * @param names Names
     * @return Indices
     */
    @Override
    public Integer[] indices(final Object[] names) {
        return indices(Arrays.asList(names));
    }

    /**
     * Get indices
     * @param names Names
     * @return Indices
     */
    @Override
    public Integer[] indices(final List<Object> names) {
        final int size = names.size();
        final Integer[] indices = new Integer[size];
        for (int i = 0; i < size; i++) {
            indices[i] = indexOfName(names.get(i).toString());
        }
        return indices;
    }
    
    /**
     * Check if the data types of all columns are same
     * @return Boolean
     */
    public boolean isSameDataType() {
        if (this.data.size() == 1)
            return true;
        
        DataType dt = this.data.get(0).dataType;
        for (int i = 1; i < this.data.size(); i++){
            if (dt != this.data.get(i).dataType){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Convert to string index
     * @return String index
     */
    public Index asIndex() {
        return Index.factory(this.getNames());
    }
    
    @Override
    public Object clone() {        
        ColumnIndex r = new ColumnIndex();
        for (Column col : this.data){
            r.add((Column)col.clone());
        }
        r.format = this.format;
        return r;
    }
    // </editor-fold>
}
