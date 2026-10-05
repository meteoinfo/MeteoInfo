/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console;

import java.awt.*;

/**
 *
 * @author Yaqiang Wang
 */
public class ConsoleColors {
    private Color promptColor;
    private Color commandColor;
    private Color codeLinesColor;
    private Color errorColor;
    
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
        switch (lookFeel) {
            case "Darcula":
            case "FlatDarculaLaf":
            case "FlatDarkLaf":
            case "FlatMacDarkLaf":
                this.promptColor = new Color(255, 100, 100);
                this.commandColor = Color.WHITE;
                this.codeLinesColor = new Color(0, 153, 204);
                this.errorColor = new Color(255, 102, 0);
                break;
            default:
                this.promptColor = Color.RED;
                this.commandColor = Color.BLACK;
                this.codeLinesColor = Color.BLUE;
                this.errorColor = new Color(255, 102, 0);
                break;
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
    public void setPromptColor(Color value) {
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

    /**
     * Get error color
     * @return Error color
     */
    public Color getErrorColor() {
        return this.errorColor;
    }

    /**
     * Set error color
     * @param value Error color
     */
    public void setErrorColor(Color value) {
        this.errorColor = value;
    }
}
