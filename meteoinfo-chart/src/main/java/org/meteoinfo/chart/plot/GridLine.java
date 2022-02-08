/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import org.meteoinfo.geometry.legend.LineStyles;
import org.meteoinfo.geometry.legend.PolylineBreak;

import java.awt.*;

/**
 *
 * @author yaqiang
 */
public class GridLine {
    // <editor-fold desc="Variables">
    protected PolylineBreak lineBreak;
    protected boolean drawXLine;
    protected boolean drawYLine;
    protected boolean drawZLine;
    protected boolean top;
    // </editor-fold>    
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public GridLine(){
        this(false);
    }

    /**
     * Constructor
     * @param visible
     */
    public GridLine(boolean visible) {
        this.lineBreak = new PolylineBreak();
        this.lineBreak.setColor(Color.LIGHT_GRAY);
        this.lineBreak.setStyle(LineStyles.DASH);
        this.top = false;
        this.drawXLine = visible;
        this.drawYLine = visible;
        this.drawZLine = visible;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get color
     * @return Color
     */
    public Color getColor(){
        return this.lineBreak.getColor();
    }
    
    /**
     * Set color
     * @param value Color
     */
    public void setColor(Color value){
        this.lineBreak.setColor(value);
    }
    
    /**
     * Get size
     * @return Size
     */
    public float getSize(){
        return this.lineBreak.getWidth();
    }
    
    /**
     * Set size
     * @param value Size
     */
    public void setSize(float value) {
        this.lineBreak.setWidth(value);
    }
    
    /**
     * Get style
     * @return Style
     */
    public LineStyles getStyle(){
        return this.lineBreak.getStyle();
    }
    
    /**
     * Set style
     * @param value Style
     */
    public void setStyle(LineStyles value){
        this.lineBreak.setStyle(value);
    }
    
    /**
     * Get if draw x grid lines
     * @return Boolean
     */
    public boolean isDrawXLine(){
        return this.drawXLine;
    }
    
    /**
     * Set if draw x grid lines
     * @param value Boolean
     */
    public void setDrawXLine(boolean value){
        this.drawXLine = value;
    }
    
    /**
     * Get if draw y grid lines
     * @return Boolean
     */
    public boolean isDrawYLine(){
        return this.drawYLine;
    }
    
    /**
     * Set if draw y grid lines
     * @param value Boolean
     */
    public void setDrawYLine(boolean value){
        this.drawYLine = value;
    }

    /**
     * Get if draw z grid lines
     * @return Boolean
     */
    public boolean isDrawZLine(){
        return this.drawZLine;
    }

    /**
     * Set if draw z grid lines
     * @param value Boolean
     */
    public void setDrawZLine(boolean value){
        this.drawZLine = value;
    }
    
    /**
     * Return if the grid draw on the top of the graph
     * @return Boolean
     */
    public boolean isTop(){
        return this.top;
    }
    
    /**
     * Set if the grid draw on the top of the graph
     * @param value Boolean
     */
    public void setTop(boolean value){
        this.top = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
