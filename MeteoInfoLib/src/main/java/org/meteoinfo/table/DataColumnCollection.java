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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Yaqiang Wang
 */
public class DataColumnCollection extends ArrayList<DataColumn>{
    Map nameMap = new HashMap<>();
    
    /**
     * Constructor
     */
    public DataColumnCollection(){
        super();
    }
    
    /**
     * Constructor
     * @param dcs DataColumn list
     */
    public DataColumnCollection(List<DataColumn> dcs) {
        super();
        for (DataColumn dc : dcs) {
            this.add(dc);
        }
    }
    
    /**
     * Add a data column
     * @param aCol The data column
     * @return Boolean
     */
    @Override
    public boolean add(DataColumn aCol){
        boolean isTrue = super.add(aCol);
        nameMap.put(aCol.getColumnName(), aCol);
        
        return isTrue;
    }
    
    /**
     * Insert a data column
     * @param index The index
     * @param aCol The data column
     */
    @Override
    public void add(int index, DataColumn aCol){
        super.add(index, aCol);
        nameMap.put(aCol.getColumnName(), aCol);
    }
    
    /**
     * Add a data column
     * @param colName
     * @param dataType
     * @return The added data column
     */
    public DataColumn add(String colName, DataTypes dataType){
        DataColumn aCol = new DataColumn(colName, dataType);
        this.add(aCol);
        nameMap.put(aCol.getColumnName(), aCol);
        return aCol;
    }
    
    /**
     * Get data column by name
     * @param colName Column name
     * @return The data column
     */
    public DataColumn get(String colName){
        return (DataColumn)nameMap.get(colName);
    }
    
    /**
     * Rename column
     * @param column The column
     * @param name The new name
     */
    public void renameColumn(DataColumn column, String name){
        String oldName = column.getColumnName();
        column.setColumnName(name);
        nameMap.remove(oldName);
        nameMap.put(name, column);
    }
    
    /**
     * Get column names
     * @return Column names
     */
    public List<String> getColumnNames(){
        List<String> colNames = new ArrayList<>();
        for (DataColumn col : this){
            colNames.add(col.getColumnName());
        }
        return colNames;
    }
}
