/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author wyq
 */
public class VariableExplorer extends JPanel {
    private final JTable varTable;
    private final DefaultTableModel tmVars;
    
    /**
     * Constructor
     */
    public VariableExplorer(){
        super(new BorderLayout());
        
        this.tmVars = new DefaultTableModel();
        this.tmVars.addColumn("Name");
        this.tmVars.addColumn("Type");
        this.tmVars.addColumn("Size");
        this.tmVars.addColumn("Value");
        this.varTable = new JTable(this.tmVars);
        this.add(new JScrollPane(this.varTable), "Center");
        this.setBackground(Color.white);
    }
    
    /**
     * Add a variable
     * @param var Variable
     */
    public void addVariable(Object[] var){        
        this.tmVars.insertRow(0, var);
    }
    
    /**
     * Update variables
     * @param vars Variables
     */
    public void updateVariables(List<Object[]> vars) {
        this.tmVars.setRowCount(0);
        for (Object[] var : vars)
            this.addVariable(var);
    }
}
