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
public class ArrowBreak extends PointBreak {
    // <editor-fold desc="Variables">
    private float width;
    private float headWidth;
    private float headLength;
    private float overhang;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ArrowBreak() {
        super();
        this.outlineColor = null;
        this.width = 1;
        this.headWidth = this.width * 5;
        this.headLength = this.headWidth * 1.5f;
        this.overhang = 0;
    }
    
    /**
     * Constructor with a PointBreak
     * @param pb A PointBreak
     */
    public ArrowBreak(PointBreak pb) {
        this.caption = pb.caption;
        this.color = pb.color;
        this.drawShape = pb.drawShape;
        this.startValue = pb.startValue;
        this.endValue = pb.endValue;
        this.isNoData = pb.isNoData;
        this.markerType = pb.markerType;
        this.fontName = pb.fontName;
        this.charIndex = pb.charIndex;
        this.imagePath = pb.imagePath;
        this.outlineColor = pb.outlineColor;
        this.outlineSize = pb.outlineSize;
        this.size = pb.size;
        this.drawOutline = pb.drawOutline;
        this.drawFill = pb.drawFill;
        this.style = pb.style;
        this.angle = pb.angle;
        this.width = 1;
        this.headWidth = this.width * 5;
        this.headLength = this.headWidth * 1.5f;
        this.overhang = 0;
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
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
