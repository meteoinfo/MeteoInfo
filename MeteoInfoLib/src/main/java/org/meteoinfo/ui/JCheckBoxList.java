 /* Copyright 2012 - Yaqiang Wang,
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
package org.meteoinfo.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/*
 * Copyright (C) 2005 - 2007 JasperSoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 * 
 * Unless you have purchased a commercial license agreement from JasperSoft, the
 * following license terms apply:
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; and without the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see http://www.gnu.org/licenses/gpl.txt or write to:
 * 
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA USA
 * 02111-1307
 * 
 * 
 * 
 * 
 * CheckboxCellRenderer.java
 * 
 * Created on October 5, 2006, 10:03 AM
 * 
 */
/**
 *
 * @author yaqiang
 */
public class JCheckBoxList extends JList {

    public JCheckBoxList() {
        super();

        setModel(new DefaultListModel());
        setCellRenderer(new CheckboxCellRenderer());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());

                if (index != -1) {
                    Object obj = getModel().getElementAt(index);
                    if (obj instanceof JCheckBox) {
                        JCheckBox checkbox = (JCheckBox) obj;
                        if (e.getPoint().getX() < 20){
                            checkbox.setSelected(!checkbox.isSelected());
                        }
                        repaint();
                    }
                }
            }
        });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @SuppressWarnings("unchecked")
    public int[] getCheckedIdexes() {
        java.util.List list = new java.util.ArrayList();
        DefaultListModel dlm = (DefaultListModel) getModel();
        for (int i = 0; i < dlm.size(); ++i) {
            Object obj = getModel().getElementAt(i);
            if (obj instanceof JCheckBox) {
                JCheckBox checkbox = (JCheckBox) obj;
                if (checkbox.isSelected()) {
                    list.add(new Integer(i));
                }
            }
        }

        int[] indexes = new int[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            indexes[i] = ((Integer) list.get(i)).intValue();
        }

        return indexes;
    }

    @SuppressWarnings("unchecked")
    public java.util.List getCheckedItems() {
        java.util.List list = new java.util.ArrayList();
        DefaultListModel dlm = (DefaultListModel) getModel();
        for (int i = 0; i < dlm.size(); ++i) {
            Object obj = getModel().getElementAt(i);
            if (obj instanceof JCheckBox) {
                JCheckBox checkbox = (JCheckBox) obj;
                if (checkbox.isSelected()) {
                    list.add(checkbox);
                }
            }
        }
        return list;
    }
}
/**
 *
 * @author gtoffoli
 */
class CheckboxCellRenderer extends DefaultListCellRenderer {

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        if (value instanceof CheckBoxListEntry) {
            CheckBoxListEntry checkbox = (CheckBoxListEntry) value;
            checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            if (checkbox.isRed()) {
                checkbox.setForeground(Color.red);
            } else {
                checkbox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            }
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
                    : noFocusBorder);

            return checkbox;
        } else {
            return super.getListCellRendererComponent(list, value.getClass().getName(), index,
                    isSelected, cellHasFocus);
        }
    }
}
