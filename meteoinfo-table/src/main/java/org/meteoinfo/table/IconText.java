/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.table;

import javax.swing.*;

/**
 *
 * @author yaqiang
 */
public class IconText {
    private final Icon icon;
    private final String text;
    private int sortIdx = 1;
    
    /**
     * Constructor
     * @param icon Icon
     * @param text Text
     */
    public IconText(Icon icon, String text){
        this.icon = icon;
        this.text = text;
    }

    /**
     * Constructor
     * @param icon Icon
     * @param text Text
     * @param sortIdx Sort index
     */
    public IconText(Icon icon, String text, int sortIdx ) {
        this(icon, text);
        this.sortIdx = sortIdx;
    }
    
    /**
     * Get icon
     * @return Icon
     */
    public Icon getImageIcon(){
        return this.icon;
    }
    
    /**
     * Get text
     * @return Text
     */
    public String getText(){
        return this.text;
    }

    /**
     * Get text with sort index ahead for sort comparing
     * @return Sort text
     */
    public String getSortText() {
        return String.valueOf(this.sortIdx) + this.text;
    }
    
    @Override
    public String toString(){
        return this.text;
    }
}
