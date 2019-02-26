/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.legend;

import java.awt.Color;

/**
 *
 * @author Yaqiang Wang
 */
public class BarBreak extends PolygonBreak {
    // <editor-fold desc="Variables">
    private Color errorColor;
    private float errorSize;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public BarBreak(){
        super();
        errorColor = Color.black;
        errorSize = 1.0f;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get error color
     * @return Error color
     */
    public Color getErrorColor(){
        return this.errorColor;
    }
    
    /**
     * Set error color
     * @param value Error color
     */
    public void setErrorColor(Color value){
        this.errorColor = value;
    }
    
    /**
     * Get error size
     * @return Error size
     */
    public float getErrorSize(){
        return this.errorSize;
    }
    
    /**
     * Set error size
     * @param value Error size
     */
    public void setErrorSize(float value){
        this.errorSize = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
