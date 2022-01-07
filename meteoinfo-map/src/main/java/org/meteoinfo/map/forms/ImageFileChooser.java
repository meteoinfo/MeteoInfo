/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.map.forms;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.meteoinfo.map.config.GenericFileFilter;

/**
 *
 * @author Yaqiang Wang
 */
public class ImageFileChooser extends JFileChooser {

    JComboBox dpiCB;
    JPanel panel;

    /**
     * Constructor
     */
    public ImageFileChooser() {
        super();
        dpiCB = new JComboBox();
        DefaultComboBoxModel cbm = new DefaultComboBoxModel();
        cbm.addElement("Default");
        cbm.addElement("150");
        cbm.addElement("300");
        cbm.addElement("600");
        cbm.addElement("900");
        cbm.addElement("1200");
        dpiCB.setModel(cbm);
        dpiCB.setEditable(true);
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 10, 0, 0);
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(new JLabel("DPI:"), constraints);
        constraints.gridy = 1;
        panel.add(dpiCB, constraints);    
        setAccessory(panel);
        //panel.setVisible(false);

        this.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                String prop = pce.getPropertyName();

                //If a file became selected, find out which one.
                if (JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(prop)) {
                    GenericFileFilter filter = (GenericFileFilter) pce.getNewValue();                    
                    if (filter != null){
                        switch (filter.getFileExtent().toLowerCase()) {
                            case "png":
                            case "jpg":
                            case "bmp":
                            case "gif":
                            case "tif":
                                panel.setVisible(true);
                                break;
                            default:
                                panel.setVisible(false);
                                break;
                        }
                        repaint();
                    }
                }
            }
        });
    }

    /**
     * Get DPI
     *
     * @return DPI
     */
    public Integer getDPI() {
        if (!this.dpiCB.isVisible())
            return null;
        
        String dpiStr = this.dpiCB.getSelectedItem().toString();
        if (dpiStr.equals("Default")) {
            return null;
        } else {
            return Integer.parseInt(dpiStr);
        }
    }
}
