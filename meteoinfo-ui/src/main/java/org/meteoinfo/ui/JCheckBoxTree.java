/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.List;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 * @author wyq
 */
public class JCheckBoxTree extends JTree {

    /**
     * Constructor
     */
    public JCheckBoxTree() {
        super();
        
        this.setCellRenderer(new CheckBoxNodeRenderer());
        this.setCellEditor(new CheckBoxNodeEditor(this));
        this.setEditable(true);
    }
}

class CheckBoxNodeRenderer implements TreeCellRenderer {

    private JCheckBox leafRenderer = new JCheckBox();
    private DefaultTreeCellRenderer nonLeafRenderer = new DefaultTreeCellRenderer();
    Color selectionBorderColor, selectionForeground, selectionBackground,
            textForeground, textBackground;
    
    protected JCheckBox getLeafRenderer() {
        return leafRenderer;
    }
    
    public CheckBoxNodeRenderer() {
        Font fontValue;
        fontValue = UIManager.getFont("Tree.font");
        if (fontValue != null) {
            leafRenderer.setFont(fontValue);
        }
        Boolean booleanValue = (Boolean) UIManager
                .get("Tree.drawsFocusBorderAroundIcon");
        leafRenderer.setFocusPainted((booleanValue != null)
                && (booleanValue.booleanValue()));
        
        selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
        textForeground = UIManager.getColor("Tree.textForeground");
        textBackground = UIManager.getColor("Tree.textBackground");
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        
        Component returnValue;
        if (leaf) {
            
            String stringValue = tree.convertValueToText(value, selected,
                    expanded, leaf, row, false);
            leafRenderer.setText(stringValue);
            leafRenderer.setSelected(false);
            
            leafRenderer.setEnabled(tree.isEnabled());
            
            if (selected) {
                leafRenderer.setForeground(selectionForeground);
                leafRenderer.setBackground(selectionBackground);
            } else {
                leafRenderer.setForeground(textForeground);
                leafRenderer.setBackground(textBackground);
            }
            
            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                Object userObject = ((DefaultMutableTreeNode) value)
                        .getUserObject();
                if (userObject instanceof CheckBoxNode) {
                    CheckBoxNode node = (CheckBoxNode) userObject;
                    leafRenderer.setText(node.getText());
                    leafRenderer.setSelected(node.isSelected());
                }
            }
            returnValue = leafRenderer;
        } else {
            returnValue = nonLeafRenderer.getTreeCellRendererComponent(tree,
                    value, selected, expanded, leaf, row, hasFocus);
        }
        return returnValue;
    }
}

class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
    
    CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
    ChangeEvent changeEvent = null;
    JTree tree;
    
    public CheckBoxNodeEditor(JTree tree) {
        this.tree = tree;
    }
    
    @Override
    public Object getCellEditorValue() {
        JCheckBox checkbox = renderer.getLeafRenderer();
        CheckBoxNode checkBoxNode = new CheckBoxNode(checkbox.getText(),
                checkbox.isSelected());
        return checkBoxNode;
    }
    
    @Override
    public boolean isCellEditable(EventObject event) {
        boolean returnValue = false;
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            TreePath path = tree.getPathForLocation(mouseEvent.getX(),
                    mouseEvent.getY());
            if (path != null) {
                Object node = path.getLastPathComponent();
                if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                    Object userObject = treeNode.getUserObject();
                    returnValue = ((treeNode.isLeaf()) && (userObject instanceof CheckBoxNode));
                }
            }
        }
        return returnValue;
    }
    
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row) {
        
        Component editor = renderer.getTreeCellRendererComponent(tree, value,
                true, expanded, leaf, row, true);

        // editor always selected / focused
        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (stopCellEditing()) {
                    fireEditingStopped();
                }
            }
        };
        if (editor instanceof JCheckBox) {
            ((JCheckBox) editor).addItemListener(itemListener);
        }
        
        return editor;
    }
}

class CheckBoxNode {

    String text;
    boolean selected;
    
    public CheckBoxNode(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean newValue) {
        selected = newValue;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String newValue) {
        text = newValue;
    }
    
    @Override
    public String toString() {
        return getClass().getName() + "[" + text + "/" + selected + "]";
    }
}

class NamedList extends ArrayList {

    String name;
    
    public NamedList(String name) {
        this.name = name;
    }
    
    public NamedList(String name, Object elements[]) {
        this.name = name;
        for (int i = 0, n = elements.length; i < n; i++) {
            add(elements[i]);
        }
    }
    
    @Override
    public String toString() {
        return "[" + name + "]";
    }
}