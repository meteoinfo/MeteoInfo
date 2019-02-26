/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import javax.swing.JTextArea;

/**
 *
 * @author wyq
 */
public class OutputDockable extends DefaultSingleCDockable {

    private final JTextArea textArea;
    
    /**
     * Constructor
     * @param id
     * @param title
     * @param actions 
     */
    public OutputDockable(String id, String title, CAction... actions) {
        super(id, title, actions);
        
        textArea = new JTextArea();
        this.getContentPane().add(textArea);
    }
    
    /**
     * Get text area
     * @return Text area
     */
    public JTextArea getTextArea(){
        return this.textArea;
    }
}
