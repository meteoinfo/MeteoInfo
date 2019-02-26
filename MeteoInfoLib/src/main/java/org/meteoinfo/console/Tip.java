/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;

/**
 *
 * @author Yaqiang Wang
 */
public class Tip extends JWindow {
    // <editor-fold desc="Variables">
    public final static int MAX_HEIGHT = 300;
    private final static int MAX_WIDTH = 400;
    private final JTextArea textarea;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param frame JFrame 
     */
    public Tip(JFrame frame){
        super(frame);
        this.textarea = new JTextArea();
        this.textarea.setBackground(new Color(225, 255, 255));
        this.textarea.setEditable(false);
        JScrollPane jscrollpane = new JScrollPane(this.textarea);
        this.getContentPane().add(jscrollpane);
        this.setAlwaysOnTop(true);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set text
     * @param tip Tip text
     */
    public void setText(String tip){
        this.textarea.setText(tip);
        this.textarea.setCaretPosition(0);
        this.setSize(this.getPreferredSize());
    }
    
    @Override
    public Dimension getPreferredSize(){
        int MAGIC = 20;
        Dimension size = this.textarea.getPreferredScrollableViewportSize();
        int height = size.height + MAGIC;
        int width = size.width + MAGIC;
        if (height > Tip.MAX_HEIGHT)
            height = Tip.MAX_HEIGHT;
        if (width > Tip.MAX_WIDTH)
            width = Tip.MAX_WIDTH;
        return new Dimension(width, height);
    }
    
    /**
     * Show tip window
     * @param displayPoint Display point 
     */
    public void showTip(Point displayPoint){      
        this.setLocation(displayPoint);        
        this.setVisible(true);
        //this.show();
    }
    // </editor-fold>
}
