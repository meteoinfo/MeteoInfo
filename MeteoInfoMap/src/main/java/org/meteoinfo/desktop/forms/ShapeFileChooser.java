/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.forms;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.SortedMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.meteoinfo.io.IOUtil;

/**
 *
 * @author Yaqiang Wang
 */
public class ShapeFileChooser extends JFileChooser {

    JComboBox encodingCB;
    JPanel panel;

    /**
     * Constructor
     */
    public ShapeFileChooser() {
        super();
        encodingCB = new JComboBox();
        SortedMap m = Charset.availableCharsets();
        DefaultComboBoxModel cbm = new DefaultComboBoxModel();
        cbm.addElement("System");
        Iterator ir = m.keySet().iterator();
        while (ir.hasNext()) {
            String n = (String) ir.next();
            Charset e = (Charset) m.get(n);
            cbm.addElement(e.displayName());
        }
        encodingCB.setModel(cbm);
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 10, 0, 0);
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(new JLabel("Encoding:"), constraints);
        constraints.gridy = 1;
        panel.add(encodingCB, constraints);    
        setAccessory(panel);
        //panel.setVisible(false);

        this.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                String prop = pce.getPropertyName();

                //If a file became selected, find out which one.
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
                    File file = (File) pce.getNewValue();
                    if (file != null && file.isFile()){
                        String fn = file.getAbsolutePath();
                        if (fn.toLowerCase().endsWith(".shp")) {
                            String encoding = IOUtil.encodingDetectShp(fn);
                            if (encoding.equals("ISO8859_1"))
                                encoding = "UTF-8";
                            encodingCB.setSelectedItem(encoding);
                            panel.setVisible(true);
                        } else {
                            panel.setVisible(false);
                        }
                        repaint();
                    }
                }
            }
        });
    }

    /**
     * Get encoding string
     *
     * @return Encoding string
     */
    public String getEncoding() {
        String encoding = this.encodingCB.getSelectedItem().toString();
        if (encoding.equals("System")) {
            encoding = Charset.defaultCharset().displayName();
        }
        return encoding;
    }
}
