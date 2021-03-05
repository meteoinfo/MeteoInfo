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

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author yaqiang
 */
public class RowHeaderTableModel extends AbstractTableModel {
    
    // <editor-fold desc="Variables">
    private int _rowCount;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param rowCount Row count
     */
    public RowHeaderTableModel (int rowCount){
        _rowCount = rowCount;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public int getRowCount() {
        return _rowCount;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rowIndex;
    }
    
    @Override
    public String getColumnName(int col){
        return "";
    }
    
    @Override
    public boolean isCellEditable(int row, int column){
        return false;
    }
    
    public void setRowCount(int rowCount){
        this._rowCount = rowCount;
    }
    // </editor-fold>
}
