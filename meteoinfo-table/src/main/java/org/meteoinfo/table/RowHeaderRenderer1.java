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

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author yaqiang
 */
public class RowHeaderRenderer1 extends JLabel implements TableCellRenderer, ListSelectionListener {

    // <editor-fold desc="Variables">
    private JTable _refTable;
    private JTable _tableShow;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param refTable Reference table
     * @param tableShow Show table
     */
    public RowHeaderRenderer1(JTable refTable, JTable tableShow) {
        this._refTable = refTable;
        this._tableShow = tableShow;
        ListSelectionModel listModel = _refTable.getSelectionModel();
        listModel.addListSelectionListener(this);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int rowCount = _refTable.getRowCount();
        ((RowHeaderTableModel) table.getModel()).setRowCount(rowCount);
        JTableHeader header = _refTable.getTableHeader();
        this.setOpaque(true);
        //setBorder(UIManager.getBorder("TableHeader.cellBorder"));//设置为TableHeader的边框类型     
        setHorizontalAlignment(CENTER);//让text居中显示     
        setBackground(header.getBackground());//设置背景色为TableHeader的背景色       
        if (isSelect(row)) //当选取单元格时,在row header上设置成选取颜色      
        {
            setForeground(Color.white);
            setBackground(Color.lightGray);
        } else {
            setForeground(header.getForeground());
        }
        setFont(header.getFont());
        setText(String.valueOf(rowCount - row - 1));
        return this;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        this._tableShow.repaint();
    }

    private boolean isSelect(int row) {
        int[] sel = _refTable.getSelectedRows();
        for (int i = 0; i < sel.length; i++) {
            if (sel[i] == row) {
                return true;
            }
        }
        return false;
    }
    // </editor-fold>
}
