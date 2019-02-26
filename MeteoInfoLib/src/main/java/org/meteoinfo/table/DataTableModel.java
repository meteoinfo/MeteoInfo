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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author yaqiang
 */
public class DataTableModel extends AbstractTableModel {

    // <editor-fold desc="Variables">
    private DataTable _dataTable;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param dataTable Data table
     */
    public DataTableModel(DataTable dataTable){
        _dataTable = dataTable;        
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">    
    @Override
    public int getRowCount() {
        return _dataTable.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return _dataTable.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (_dataTable.getColumns().get(columnIndex).getDataType() == DataTypes.Date){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.format((Date)_dataTable.getValue(rowIndex, columnIndex));
        } else 
            return _dataTable.getValue(rowIndex, columnIndex);
    }    
    
    @Override
    public void setValueAt(Object value, int row, int column){
        _dataTable.setValue(row, column, value);
    }
    
    @Override
    public String getColumnName(int columnIndex){
        return _dataTable.getColumns().get(columnIndex).getColumnName();
    }
    
    @Override
    public boolean isCellEditable(int row, int column){
        return false;
    }
    
    public void addColumn(DataColumn col){
        this._dataTable.addColumn(col);
    }
    // </editor-fold>
}
