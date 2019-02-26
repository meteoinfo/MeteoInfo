/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.legend;

/**
 *
 * @author Yaqiang Wang
 */
public class ArrowPolygonBreak extends PolygonBreak {
    // <editor-fold desc="Variables">
    private float width;
    private float headWidth;
    private float headLength;
    private float overhang;
    private boolean lengthIncludesHead;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ArrowPolygonBreak() {
        super();
        this.width = 0.001f;
        this.headWidth = this.width * 3;
        this.headLength = this.headWidth * 1.5f;
        this.overhang = 0;
        this.lengthIncludesHead = false;
    }
    
    /**
     * Constructor with a PolylgonBreak
     * @param pb A PolygonBreak
     */
    public ArrowPolygonBreak(PolygonBreak pb) {
        this.backColor = pb.backColor;
        this.breakType = pb.breakType;
        this.caption = pb.caption;
        this.color = pb.color;
        this.drawShape = pb.drawShape;
        this.startValue = pb.startValue;
        this.endValue = pb.endValue;
        this.isNoData = pb.isNoData;
        this.isMaskout = pb.isMaskout;
        this.outlineColor = pb.outlineColor;
        this.outlineSize = pb.outlineSize;
        this.drawOutline = pb.drawOutline;
        this.drawFill = pb.drawFill;
        this.style = pb.style;
        this.styleSize = pb.styleSize;
        this.width = 0.001f;
        this.headWidth = this.width * 3;
        this.headLength = this.headWidth * 1.5f;
        this.overhang = 0;
        this.lengthIncludesHead = false;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Arrow line width
     * @return Arrow line width
     */
    public float getWidth() {
        return this.width;
    }
    
    /**
     * Set arrow line width
     * @param value Arrow line width
     */
    public void setWidth(float value) {
        this.width = value;
    }
    
    /**
     * Get arrow head width
     * @return Arrow head width
     */
    public float getHeadWidth() {
        return this.headWidth;
    }
    
    /**
     * Set arrow head width
     * @param value Arrow head width
     */
    public void setHeadWidth(float value) {
        this.headWidth = value;
    }
    
    /**
     * Get arrow head length
     * @return Arrow head length
     */
    public float getHeadLength() {
        return this.headLength;
    }
    
    /**
     * Set arrow head length
     * @param value Arrow head length
     */
    public void setHeadLength(float value) {
        this.headLength = value;
    }
    
    /**
     * Get overhang
     * @return Overhang
     */
    public float getOverhang() {
        return this.overhang;
    }
    
    /**
     * Set overhang
     * @param value Overhang
     */
    public void setOverhang(float value) {
        this.overhang = value;
    }
    
    /**
     * Get length includes head or not
     * @return Boolean
     */
    public boolean isLengthIncludesHead() {
        return this.lengthIncludesHead;
    }
    
    /**
     * Set length includes head or not
     * @param value Boolean
     */
    public void setLengthIncludesHead(boolean value) {
        this.lengthIncludesHead = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
