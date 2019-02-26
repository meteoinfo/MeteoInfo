 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.legend;

import org.meteoinfo.global.colors.ColorUtil;
import java.awt.Color;
import java.util.HashMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author User
 */
public class PointBreak extends ColorBreak {
    // <editor-fold desc="Variables">

    protected MarkerType markerType;
    protected Color outlineColor;
    protected float outlineSize;
    protected float size;
    protected PointStyle style;
    protected boolean drawOutline;
    protected boolean drawFill;
    protected String fontName;
    protected int charIndex;
    protected String imagePath;
    protected float angle;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PointBreak() {
        super();
        this.breakType = BreakTypes.PointBreak;
        markerType = MarkerType.Simple;
        fontName = "Arial";
        charIndex = 0;
        outlineColor = Color.black;
        outlineSize = 1.0f;
        size = 6.0f;
        style = PointStyle.Circle;
        drawOutline = true;
        drawFill = true;
        angle = 0;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get marker type
     *
     * @return Marker type
     */
    public MarkerType getMarkerType() {
        return markerType;
    }

    /**
     * Set marker type
     *
     * @param value Marker type
     */
    public void setMarkerType(MarkerType value) {
        markerType = value;
    }

    /**
     * Get font name
     *
     * @return Font name string
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Set font name
     *
     * @param name Font name string
     */
    public void setFontName(String name) {
        fontName = name;
    }

    /**
     * Get character index
     *
     * @return Character index
     */
    public int getCharIndex() {
        return charIndex;
    }

    /**
     * Set character index
     *
     * @param idx Index
     */
    public void setCharIndex(int idx) {
        charIndex = idx;
    }

    /**
     * Get image file path
     *
     * @return Image file path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Set image file path
     *
     * @param path Image file path
     */
    public void setImagePath(String path) {
        imagePath = path;
    }

    /**
     * Get outline color
     *
     * @return Outline color
     */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
     * Set outline color
     *
     * @param c Color
     */
    public void setOutlineColor(Color c) {
        outlineColor = c;
    }
    
    /**
     * Get outline size
     * @return Outline size
     */
    public float getOutlineSize(){
        return this.outlineSize;
    }
    
    /**
     * Set outline size
     * @param value Outline size
     */
    public void setOutlineSize(float value){
        this.outlineSize = value;
    }

    /**
     * Get size
     *
     * @return Size
     */
    public float getSize() {
        return size;
    }

    /**
     * Set size
     *
     * @param value Size
     */
    public void setSize(float value) {
        size = value;
    }

    /**
     * Get point style
     *
     * @return Point style
     */
    public PointStyle getStyle() {
        return style;
    }

    /**
     * Set point style
     *
     * @param value Point style
     */
    public void setStyle(PointStyle value) {
        if (value != null)
            style = value;
    }

    /**
     * Get if draw outline
     *
     * @return Boolean
     */
    public boolean isDrawOutline() {
        return drawOutline;
    }

    /**
     * Set if draw outline
     *
     * @param isTrue Boolean
     */
    public void setDrawOutline(boolean isTrue) {
        drawOutline = isTrue;
    }

    /**
     * Get if draw fill
     *
     * @return Boolean
     */
    public boolean isDrawFill() {
        return drawFill;
    }

    /**
     * Set if draw fill
     *
     * @param isTrue Boolean
     */
    public void setDrawFill(boolean isTrue) {
        drawFill = isTrue;
    }

    /**
     * Get point angle
     *
     * @return Angle
     */
    public float getAngle() {
        return angle;
    }

    /**
     * Set point angle
     *
     * @param value Angle
     */
    public void setAngle(float value) {
        angle = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get property object
     *
     * @return Property object
     */
    @Override
    public Object getPropertyObject() {
        HashMap objAttr = new HashMap();
        objAttr.put("Color", "Color");
        objAttr.put("OutlineColor", "OutlineColor");
        objAttr.put("OutlineSize", "OutlineSize");
        objAttr.put("Size", "Size");
        objAttr.put("Style", "Style");
        objAttr.put("DrawOutline", "DrawOutline");
        objAttr.put("DrawFill", "DrawFill");
        objAttr.put("DrawPoint", "DrawPoint");
        objAttr.put("Angle", "Angle");
        return objAttr;
    }

    /**
     * Clone
     *
     * @return PointBreak
     */
    @Override
    public Object clone() {
        PointBreak aCB = new PointBreak();
        aCB.setCaption(this.getCaption());
        aCB.setColor(this.getColor());
        aCB.setDrawShape(this.isDrawShape());
        aCB.setEndValue(this.getEndValue());
        aCB.setNoData(this.isNoData());
        aCB.setStartValue(this.getStartValue());
        aCB.setMarkerType(markerType);
        aCB.setFontName(fontName);
        aCB.setCharIndex(charIndex);
        aCB.setImagePath(imagePath);
        aCB.setOutlineColor(outlineColor);
        aCB.setOutlineSize(this.outlineSize);
        aCB.setSize(size);
        aCB.setDrawOutline(drawOutline);
        aCB.setDrawFill(drawFill);
        aCB.setStyle(style);
        aCB.setAngle(angle);

        return aCB;
    }

    /**
     * Export to xml document
     *
     * @param doc xml document
     * @param parent parent xml element
     */
    @Override
    public void exportToXML(Document doc, Element parent) {
        Element brk = doc.createElement("Break");
        Attr captionAttr = doc.createAttribute("Caption");
        Attr startValueAttr = doc.createAttribute("StartValue");
        Attr endValueAttr = doc.createAttribute("EndValue");
        Attr colorAttr = doc.createAttribute("Color");
        Attr isNoDataAttr = doc.createAttribute("IsNoData");

        captionAttr.setValue(this.getCaption());
        startValueAttr.setValue(String.valueOf(this.getStartValue()));
        endValueAttr.setValue(String.valueOf(this.getEndValue()));
        colorAttr.setValue(ColorUtil.toHexEncoding(this.getColor()));
        isNoDataAttr.setValue(String.valueOf(this.isNoData()));

        brk.setAttributeNode(captionAttr);
        brk.setAttributeNode(startValueAttr);
        brk.setAttributeNode(endValueAttr);
        brk.setAttributeNode(colorAttr);
        brk.setAttributeNode(isNoDataAttr);

        parent.appendChild(brk);
    }
    // </editor-fold>
}
