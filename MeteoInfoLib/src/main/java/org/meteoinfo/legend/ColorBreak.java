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
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Color break class
 *
 * @author Yaqiang Wang
 */
public class ColorBreak {
    // <editor-fold desc="Variables">

    protected BreakTypes breakType;
    protected Object startValue;
    protected Object endValue;
    protected Color color;
    protected String caption;
    protected boolean isNoData;
    protected boolean drawShape;
    protected String tag;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public ColorBreak() {
        breakType = BreakTypes.ColorBreak;
        color = Color.BLACK;
        isNoData = false;
        drawShape = true;
        startValue = 0;
        endValue = 0;
        caption = "";
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get break type
     *
     * @return Break type
     */
    public BreakTypes getBreakType() {
        return breakType;
    }

    /**
     * Set break type
     *
     * @param value Break type
     */
    public void setBreakType(BreakTypes value) {
        breakType = value;
    }

    /**
     * Get start value
     *
     * @return Start value
     */
    public Object getStartValue() {
        return startValue;
    }

    /**
     * Set start value
     *
     * @param value  Start value
     */
    public void setStartValue(Object value) {
        startValue = value;
    }

    /**
     * Get end value
     *
     * @return End value
     */
    public Object getEndValue() {
        return endValue;
    }

    /**
     * Set end value
     *
     * @param value End Value
     */
    public void setEndValue(Object value) {
        endValue = value;
    }

    /**
     * Get color
     *
     * @return Color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set color
     *
     * @param c Color
     */
    public void setColor(Color c) {
        color = c;
    }

    /**
     * Get caption
     *
     * @return Caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Set caption
     *
     * @param value Caption
     */
    public void setCaption(String value) {
        caption = value;
    }

    /**
     * Get if is undefine data
     *
     * @return boolean
     */
    public boolean isNoData() {
        return isNoData;
    }

    /**
     * Set if is undefine data
     *
     * @param isTrue boolean
     */
    public void setNoData(boolean isTrue) {
        isNoData = isTrue;
    }

    /**
     * Get if draw shape
     *
     * @return boolean
     */
    public boolean isDrawShape() {
        return drawShape;
    }

    /**
     * Set if draw shape
     *
     * @param isTrue boolean
     */
    public void setDrawShape(boolean isTrue) {
        drawShape = isTrue;
    }
    
    /**
     * Get tag
     * @return Tag 
     */
    public String getTag(){
        return this.tag;
    }
    
    /**
     * Set tag
     * @param value Tag
     */
    public void setTag(String value){
        this.tag = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get property object
     *
     * @return Custom property object
     */
    public Object getPropertyObject() {
        Map objAttr = new HashMap();
        objAttr.put("Color", "Color");
        return objAttr;
    }

    /**
     * Clone
     *
     * @return ColorBreak
     */
    @Override
    public Object clone() {
        //return MemberwiseClone();
        ColorBreak aCB = new ColorBreak();
        aCB.setCaption(caption);
        aCB.setColor(color);
        aCB.setDrawShape(drawShape);
        aCB.setEndValue(endValue);
        aCB.setNoData(isNoData);
        aCB.setStartValue(startValue);
        aCB.setTag(tag);

        return aCB;
    }

    /**
     * Export to XML document
     *
     * @param doc XML document
     * @param parent Parent XML element
     */
    public void exportToXML(Document doc, Element parent) {
        Element brk = doc.createElement("Break");
        Attr captionAttr = doc.createAttribute("Caption");
        Attr startValueAttr = doc.createAttribute("StartValue");
        Attr endValueAttr = doc.createAttribute("EndValue");
        Attr colorAttr = doc.createAttribute("Color");
        Attr isNoDataAttr = doc.createAttribute("IsNoData");
        Attr tagAttr = doc.createAttribute("Tag");

        captionAttr.setValue(caption);
        startValueAttr.setValue(String.valueOf(startValue));
        endValueAttr.setValue(String.valueOf(endValue));
        colorAttr.setValue(ColorUtil.toHexEncoding(color));
        isNoDataAttr.setValue(String.valueOf(isNoData));
        tagAttr.setValue(tag);

        brk.setAttributeNode(captionAttr);
        brk.setAttributeNode(startValueAttr);
        brk.setAttributeNode(endValueAttr);
        brk.setAttributeNode(colorAttr);
        brk.setAttributeNode(isNoDataAttr);
        brk.setAttributeNode(tagAttr);

        parent.appendChild(brk);
    }

    /**
     * Get value string
     *
     * @return value string
     */
    public String getValueString() {
        if (String.valueOf(startValue) == null ? String.valueOf(endValue) == null : String.valueOf(startValue).equals(String.valueOf(endValue))) {
            return String.valueOf(startValue);
        } else {
            return String.valueOf(startValue) + " - " + String.valueOf(endValue);
        }
    }
    // </editor-fold>
}
