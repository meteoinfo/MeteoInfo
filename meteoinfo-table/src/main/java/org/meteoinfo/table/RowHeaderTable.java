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

import java.awt.Dimension;
import javax.swing.JTable;

/**
 *
 * @author yaqiang
 */
public class RowHeaderTable extends JTable {
    // <editor-fold desc="Variables">

    private JTable _refTable;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param refTable Reference table
     * @param columnWidth Column width
     */
    public RowHeaderTable(JTable refTable, int columnWidth) {
        this(refTable, columnWidth, false);
    }
    
    /**
     * Constructor
     * @param refTable Reference table
     * @param columnWidth Column width
     * @param reverseRender If reverseRender
     */
    public RowHeaderTable(JTable refTable, int columnWidth, boolean reverseRender){
        super(new RowHeaderTableModel(refTable.getRowCount()));
        this._refTable = refTable;
        //this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);//不可以调整列宽   
        if (_refTable.getRowCount() > 10000)
            columnWidth = 60;
        this.getColumnModel().getColumn(0).setPreferredWidth(columnWidth);
        if (reverseRender){
            this.setDefaultRenderer(Object.class, new RowHeaderRenderer1(_refTable, this));
        } else {
            this.setDefaultRenderer(Object.class, new RowHeaderRenderer(_refTable, this));//设置渲染器  
        }
        this.setPreferredScrollableViewportSize(new Dimension(columnWidth, 0));
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
