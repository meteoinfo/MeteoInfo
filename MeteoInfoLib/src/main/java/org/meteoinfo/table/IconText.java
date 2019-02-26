/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.table;

import javax.swing.ImageIcon;

/**
 *
 * @author yaqiang
 */
public class IconText {
    private final ImageIcon imageIcon;
    private final String text;
    
    /**
     * Constructor
     * @param icon ImageIcon
     * @param text Text
     */
    public IconText(ImageIcon icon, String text){
        this.imageIcon = icon;
        this.text = text;
    }
    
    /**
     * Get image icon
     * @return Image icon
     */
    public ImageIcon getImageIcon(){
        return this.imageIcon;
    }
    
    /**
     * Get text
     * @return Text
     */
    public String getText(){
        return this.text;
    }
    
    @Override
    public String toString(){
        return this.text;
    }
}
