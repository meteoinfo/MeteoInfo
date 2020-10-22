/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Color;

import org.locationtech.proj4j.datum.Grid;
import org.meteoinfo.legend.LineStyles;

/**
 *
 * @author yaqiang
 */
public class GridLine {
    // <editor-fold desc="Variables">
    private Color color;
    private float size;
    private LineStyles style;
    private boolean drawXLine;
    private boolean drawYLine;
    private boolean drawZLine;
    private boolean top;
    // </editor-fold>    
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public GridLine(){
        this.color = Color.LIGHT_GRAY;
        this.size = 1.0f;
        this.style = LineStyles.DASH;
        this.drawXLine = false;
        this.drawYLine = false;
        this.drawZLine = false;
        this.top = false;
    }

    /**
     * Constructor
     * @param visible
     */
    public GridLine(boolean visible) {
        this();
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
        return this.color;
    }
    
    /**
     * Set color
     * @param value Color
     */
    public void setColor(Color value){
        this.color = value;
    }
    
    /**
     * Get size
     * @return Size
     */
    public float getSize(){
        return this.size;        
    }
    
    /**
     * Set size
     * @param value Size
     */
    public void setSize(float value) {
        this.size = value;
    }
    
    /**
     * Get style
     * @return Style
     */
    public LineStyles getStyle(){
        return this.style;
    }
    
    /**
     * Set style
     * @param value Style
     */
    public void setStyle(LineStyles value){
        this.style = value;
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
