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
package org.meteoinfo.geometry.legend;

import java.awt.*;
import java.util.HashMap;

/**
 * Polyline break class
 *
 * @author Yaqiang Wang
 */
public class PolylineBreak extends ColorBreak {
    // <editor-fold desc="Variables">

    protected float width;
    protected LineStyles style;
    protected boolean drawPolyline;
    protected boolean drawSymbol;
    protected float symbolSize;
    protected PointStyle symbolStyle;
    protected Color symbolColor;
    protected Color symbolFillColor;
    protected boolean fillSymbol;
    protected int symbolInterval;
    protected float capSize;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PolylineBreak() {
        super();
        this.breakType = BreakTypes.POLYLINE_BREAK;
        width = 1.0f;
        style = LineStyles.SOLID;
        drawPolyline = true;
        drawSymbol = false;
        symbolSize = 8.0f;
        symbolStyle = PointStyle.UP_TRIANGLE;
        symbolColor = this.color;
        symbolFillColor = symbolColor;
        fillSymbol = false;
        symbolInterval = 1;
        capSize = 0;
    }

    /**
     * Create a PolylineBreak from a PointBreak
     * @param pb The PointBreak
     * @return The PolylineBreak
     */
    public static PolylineBreak of(PointBreak pb) {
        PolylineBreak plb = new PolylineBreak();
        plb.setDrawSymbol(true);
        plb.setFillSymbol(pb.drawFill);
        plb.setSymbolFillColor(pb.color);
        plb.setSymbolColor(pb.color);
        plb.setSymbolSize(pb.size);
        plb.setSymbolStyle(pb.style);

        return plb;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get line width
     *
     * @return Line width
     */
    public float getWidth() {
        return width;
    }

    /**
     * Set line width
     *
     * @param size Line width
     */
    public void setWidth(float size) {
        width = size;
    }
    
    /**
     * Get line width
     *
     * @return Line width
     */
    @Deprecated
    public float getSize() {
        return width;
    }

    /**
     * Set line width
     *
     * @param size Line width
     */
    @Deprecated
    public void setSize(float size) {
        width = size;
    }

    /**
     * Get line style
     *
     * @return Line style
     */
    public LineStyles getStyle() {
        return style;
    }

    /**
     * Set line style
     *
     * @param value Line style
     */
    public void setStyle(LineStyles value) {
        if (value != null) {
            style = value;
        }
    }

    /**
     * Get if draw polyline
     *
     * @return Boolean
     */
    public boolean isDrawPolyline() {
        return drawPolyline;
    }

    /**
     * Set if draw polyline
     *
     * @param isTrue Boolean
     */
    public void setDrawPolyline(boolean isTrue) {
        drawPolyline = isTrue;
    }

    /**
     * Get if draw symbol
     *
     * @return Boolean
     */
    public boolean isDrawSymbol() {
        return drawSymbol;
    }

    /**
     * Set if draw symbol
     *
     * @param isTrue
     */
    public void setDrawSymbol(boolean isTrue) {
        drawSymbol = isTrue;
    }

    /**
     * Get is draw symbol outline or not
     * @return Draw symbol outline or not
     */
    public boolean isDrawSymbolOutline() {
        return this.symbolColor != null;
    }

    /**
     * Get symbol size
     *
     * @return Symbol size
     */
    public float getSymbolSize() {
        return symbolSize;
    }

    /**
     * Set symbol size
     *
     * @param size Symbol size
     */
    public void setSymbolSize(float size) {
        symbolSize = size;
    }

    /**
     * Get symbol style
     *
     * @return Symbol style
     */
    public PointStyle getSymbolStyle() {
        return symbolStyle;
    }

    /**
     * Set symbol style
     *
     * @param style Symbol style
     */
    public void setSymbolStyle(PointStyle style) {
        if (style != null) {
            symbolStyle = style;
        }
    }

    /**
     * Get symbol color
     *
     * @return Symbol color
     */
    public Color getSymbolColor() {
        return symbolColor;
    }

    /**
     * Set symbol color
     *
     * @param c Symbol color
     */
    public void setSymbolColor(Color c) {
        symbolColor = c;
    }

    /**
     * Get symbol fill color
     *
     * @return Symbol fill color
     */
    public Color getSymbolFillColor() {
        return this.symbolFillColor;
    }

    /**
     * Set symbol fill color
     *
     * @param value Symbol fill color
     */
    public void setSymbolFillColor(Color value) {
        this.symbolFillColor = value;
    }

    /**
     * Get if fill symbol
     *
     * @return Boolean
     */
    public boolean isFillSymbol() {
        return this.fillSymbol;
    }

    /**
     * Set if fill symbol
     *
     * @param value Boolean
     */
    public void setFillSymbol(boolean value) {
        this.fillSymbol = value;
    }

    /**
     * Get symbol interval
     *
     * @return Symbol interval
     */
    public int getSymbolInterval() {
        return symbolInterval;
    }

    /**
     * Set symbol Interval
     *
     * @param interval Symbol interval
     */
    public void setSymbolInterval(int interval) {
        symbolInterval = interval;
    }

    /**
     * Get if using dash style
     *
     * @return Boolean
     */
    public boolean isUsingDashStyle() {
        switch (style) {
            case SOLID:
            case DASH:
            case DOT:
            case DASH_DOT:
            case DASH_DOT_DOT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get cap size
     * @return Cap size
     */
    public float getCapSize() {
        return this.capSize;
    }

    /**
     * Set cap size
     * @param value Cap size
     */
    public void setCapSize(float value) {
        this.capSize = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Set symbol by a PointBreak
     * @param pb The PointBreak
     */
    public void setSymbol(PointBreak pb) {
        this.drawSymbol = true;
        this.fillSymbol = pb.drawFill;
        this.symbolColor = pb.color;
        this.symbolFillColor = pb.color;
        this.symbolSize = pb.size;
        this.symbolStyle = pb.style;
    }

    /**
     * Get property object
     *
     * @return Custom property object
     */
    @Override
    public Object getPropertyObject() {
        HashMap objAttr = new HashMap();
        objAttr.put("Color", "Color");
        objAttr.put("Width", "Width");
        objAttr.put("Style", "Style");
        objAttr.put("DrawPolyline", "DrawPolyline");
        objAttr.put("DrawSymbol", "DrawSymbol");
        objAttr.put("SymbolSize", "SymbolSize");
        objAttr.put("SymbolStyle", "SymbolStyle");
        objAttr.put("SymbolColor", "SymbolColor");
        objAttr.put("SymbolInterval", "SymbolInterval");
        return objAttr;
    }

    /**
     * Clone
     *
     * @return PolylineBreak
     */
    @Override
    public Object clone() {
        PolylineBreak aCB = new PolylineBreak();
        aCB.setCaption(this.getCaption());
        aCB.setColor(this.getColor());
        aCB.setDrawShape(this.isDrawShape());
        aCB.setEndValue(this.getEndValue());
        aCB.setNoData(this.isNoData());
        aCB.setStartValue(this.getStartValue());
        aCB.setWidth(width);
        aCB.setStyle(style);
        aCB.setDrawPolyline(drawPolyline);
        aCB.setDrawSymbol(drawSymbol);
        aCB.setFillSymbol(fillSymbol);
        aCB.setSymbolSize(symbolSize);
        aCB.setSymbolColor(symbolColor);
        aCB.setSymbolFillColor(symbolFillColor);
        aCB.setSymbolStyle(symbolStyle);
        aCB.setSymbolInterval(symbolInterval);

        return aCB;
    }
    // </editor-fold>
}
