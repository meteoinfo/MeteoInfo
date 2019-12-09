/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import java.awt.Color;

/**
 *
 * @author Yaqiang Wang
 */
public class ConsoleColors {
    private Color promptColor;
    private Color commandColor;
    private Color codeLinesColor;
    
    /**
     * Constructor
     */
    public ConsoleColors() {
        this("Nimbus");
    }
    
    /**
     * Constructor
     * @param lookFeel Look and feel
     */
    public ConsoleColors(String lookFeel) {
        if (lookFeel.equals("Darcula")) {
            this.promptColor = new Color(255, 100, 100);
            this.commandColor = Color.WHITE;
            this.codeLinesColor = new Color(0, 102, 102);
        } else {
            this.promptColor = Color.RED;
            this.commandColor = Color.BLACK;
            this.codeLinesColor = Color.BLUE;
        }
    }
    
    /**
     * Get prompt color
     * @return Prompt color
     */
    public Color getPromptColor() {
        return this.promptColor;
    }
    
    /**
     * Set prompt color
     * @param value Prompt color
     */
    public void setPrompColor(Color value) {
        this.promptColor = value;
    }
    
    /**
     * Get command color
     * @return Command color
     */
    public Color getCommandColor() {
        return this.commandColor;
    }
    
    /**
     * Set command color
     * @param value Command color
     */
    public void setCommandColor(Color value) {
        this.commandColor = value;
    }
    
    /**
     * Get code lines color
     * @return Code lines color
     */
    public Color getCodeLinesColor() {
        return this.codeLinesColor;
    }
    
    /**
     * Set code lines color
     * @param value Code lines color
     */
    public void setCodeLinesColor(Color value) {
        this.codeLinesColor = value;
    }
}
