/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geometry.legend;

import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.colors.ColorMap;

import java.awt.*;

/**
 *
 * @author yaqiang
 */
public class ColorScheme {
    // <editor-fold desc="Variables">
    private double[] values;
    private Color[] colors;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param min Minimum value
     * @param max Maximum value
     * @param n Level number
     * @param ct Color table
     */
    public ColorScheme(double min, double max, int n, ColorMap ct){
        this.values = MIMath.getIntervalValues(min, max, n);
        this.colors = ct.getColors(n);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get values
     * @return Values
     */
    public double[] getValues(){
        return this.values;
    }
    
    /**
     * Get colors
     * @return Colors
     */
    public Color[] getColors(){
        return this.colors;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
