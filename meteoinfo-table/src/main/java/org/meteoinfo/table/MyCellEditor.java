/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.table;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author yaqiang
 */
public class MyCellEditor extends DefaultCellEditor {

    public MyCellEditor() {
        super(new JTextField());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
            int row, int column) {
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
