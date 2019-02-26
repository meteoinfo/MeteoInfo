/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;

/**
 *
 * @author wyq
 */
public class VariableDockable extends DefaultSingleCDockable {
    
    private final VariableExplorer variableExplorer;
    
    /**
     * Constructor
     * @param id
     * @param title
     * @param actions 
     */
    public VariableDockable(String id, String title, CAction... actions) {
        super(id, title, actions);
        
        this.variableExplorer = new VariableExplorer(); 
        this.getContentPane().add(this.variableExplorer);
        //this.setCloseable(false);
    }
    
    /**
     * Get variable explorer
     * @return 
     */
    public VariableExplorer getVariableExplorer(){
        return this.variableExplorer;
    }
}
