/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 * @author wyq
 */
public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer {

    private CheckTreeSelectionModel selectionModel;
    private TreeCellRenderer delegate;
//    private TristateCheckBox checkBox = new TristateCheckBox();   
    private JCheckBox checkBox = new JCheckBox();

    public CheckTreeCellRenderer(TreeCellRenderer delegate, CheckTreeSelectionModel selectionModel) {
        this.delegate = delegate;
        this.selectionModel = selectionModel;
        setLayout(new BorderLayout());
        setOpaque(false);
        checkBox.setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        ((DefaultTreeCellRenderer)this.delegate).setIcon(null);

        TreePath path = tree.getPathForRow(row);
        if (path != null) {
            System.out.println(path);
            if (selectionModel.isPathSelected(path, true)) {
                checkBox.setSelected(true);
            } else {
                //System.out.println(selectionModel.isPartiallySelected(path));
                checkBox.setSelected(selectionModel.isPartiallySelected(path) ? true : false);
            }
        }
        removeAll();
        add(checkBox, BorderLayout.WEST);
        add(renderer, BorderLayout.CENTER);
        return this;
    }
}
