/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.table;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author yaqiang
 */
public class IconRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object obj, boolean isSelected, boolean hasFocus, int row,
            int column) {
        IconText i = (IconText) obj;
        if (obj == i) {
            setIcon(i.getImageIcon());
            setText(i.getText());
        }
        setHorizontalAlignment(JLabel.LEFT);
        if (isSelected){
            this.setBackground(table.getSelectionBackground());
            this.setForeground(table.getSelectionForeground());
        } else {
            this.setBackground(table.getBackground());
            this.setForeground(table.getForeground());
        }
        return this;
    }
}
