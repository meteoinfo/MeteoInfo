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
public class ArrowLineBreak extends PolylineBreak {
    // <editor-fold desc="Variables">
    private float arrowHeadWidth;
    private float arrowHeadLength;
    private float arrowOverhang;
    private Color ArrowFillColor;
    private Color ArrowOutlineColor;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ArrowLineBreak() {
        super();
        this.arrowHeadWidth = this.width * 5;
        this.arrowHeadLength = this.arrowHeadWidth * 1.5f;
        this.arrowOverhang = 0;
        this.ArrowFillColor = this.color;
        this.ArrowOutlineColor = null;
    }
    
    /**
     * Constructor
     * @param pb PolylineBreak
     */
    public ArrowLineBreak(PolylineBreak pb) {
        this.breakType = pb.breakType;
        this.caption = pb.caption;
        this.color = pb.color;
        this.drawPolyline = pb.drawPolyline;
        this.drawShape = pb.drawShape;
        this.drawSymbol = pb.drawSymbol;
        this.endValue = pb.endValue;
        this.fillSymbol = pb.fillSymbol;
        this.isNoData = pb.isNoData;
        this.startValue = pb.startValue;
        this.style = pb.style;
        this.symbolColor = pb.symbolColor;
        this.symbolFillColor = pb.symbolFillColor;
        this.symbolInterval = pb.symbolInterval;
        this.symbolSize = pb.symbolSize;
        this.symbolStyle = pb.symbolStyle;
        this.tag = pb.tag;
        this.width = pb.width;
        this.arrowHeadWidth = this.width * 5;
        this.arrowHeadLength = this.arrowHeadWidth * 1.5f;
        this.arrowOverhang = 0;
        this.ArrowFillColor = this.color;
        this.ArrowOutlineColor = null;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get arrow head width
     * @return Arrow head width
     */
    public float getArrowHeadWidth() {
        return this.arrowHeadWidth;
    }
    
    /**
     * Set arrow head width
     * @param value Arrow head width
     */
    public void setArrowHeadWidth(float value) {
        this.arrowHeadWidth = value;
    }
    
    /**
     * Get arrow head length
     * @return Arrow head length
     */
    public float getArrowHeadLength() {
        return this.arrowHeadLength;
    }
    
    /**
     * Set arrow head length
     * @param value Arrow head length
     */
    public void setArrowHeadLength(float value) {
        this.arrowHeadLength = value;
    }
    
    /**
     * Get arrow overhang
     * @return Arrow overhang
     */
    public float getArrowOverhang() {
        return this.arrowOverhang;
    }
    
    /**
     * Set arrow overhang
     * @param value Arrow overhang
     */
    public void setArrowOverhang(float value) {
        this.arrowOverhang = value;
    }
    
    /**
     * Get arrow fill color
     * @return Arrow fill color
     */
    public Color getArrowFillColor() {
        return this.ArrowFillColor;
    }
    
    /**
     * Set arrow fill color
     * @param value Arrow fill color
     */
    public void setArrowFillColor(Color value) {
        this.ArrowFillColor = value;
    }
    
    /**
     * Get arrow draw outline color
     * @return Boolean Arrow outline color
     */
    public Color getArrowOutlineColor() {
        return this.ArrowOutlineColor;
    }
    
    /**
     * Set arrow outline color
     * @param value Arrow outline color
     */
    public void setArrowOutlineColor(Color value) {
        this.ArrowOutlineColor = value;
    }
     
    // </editor-fold>
}
