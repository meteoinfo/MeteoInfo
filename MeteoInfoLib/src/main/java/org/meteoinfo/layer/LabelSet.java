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
package org.meteoinfo.layer;

import org.meteoinfo.legend.AlignType;
import java.awt.Color;
import java.awt.Font;
import org.meteoinfo.global.util.GlobalUtil;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class LabelSet {
    // <editor-fold desc="Variables">

    private boolean _drawLabels;
    private String _fieldName;
    private Font _labelFont;
    private Color _labelColor;
    private boolean _drawShadow;
    private Color _shadowColor;
    private AlignType _labelAlignType;
    private int _xOffset;
    private int _yOffset;
    private boolean _avoidCollision;
    private boolean _colorByLegend;
    private boolean _dynamicContourLabel;
    private boolean _autoDecimal;
    private int _decimalDigits;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public LabelSet() {
        _drawLabels = false;
        _fieldName = null;
        _labelFont = new Font(GlobalUtil.getDefaultFontName(), Font.PLAIN, 12);
        _labelColor = Color.black;
        _drawShadow = false;
        _shadowColor = Color.white;
        _labelAlignType = AlignType.Center;
        _xOffset = 0;
        _yOffset = 0;
        _avoidCollision = true;
        _colorByLegend = false;
        _dynamicContourLabel = false;
        _autoDecimal = true;
        _decimalDigits = 2;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get if draw labels
     *
     * @return If draw labels
     */
    public boolean isDrawLabels() {
        return _drawLabels;
    }

    /**
     * Set if draw labels
     *
     * @param istrue If draw labels
     */
    public void setDrawLabels(boolean istrue) {
        _drawLabels = istrue;
    }

    /**
     * Get label field name
     *
     * @return Label field name
     */
    public String getFieldName() {
        return _fieldName;
    }

    /**
     * Set label field name
     *
     * @param name Label field name
     */
    public void setFieldName(String name) {
        _fieldName = name;
    }

    /**
     * Get label font
     *
     * @return Font
     */
    public Font getLabelFont() {
        return _labelFont;
    }

    /**
     * Set label font
     *
     * @param font Label font
     */
    public void setLabelFont(Font font) {
        _labelFont = font;
    }

    /**
     * Get label color
     *
     * @return Label color
     */
    public Color getLabelColor() {
        return _labelColor;
    }

    /**
     * Set label color
     *
     * @param color Label color
     */
    public void setLabelColor(Color color) {
        _labelColor = color;
    }

    /**
     * Get if show shadow
     *
     * @return If show shadow
     */
    public boolean isDrawShadow() {
        return _drawShadow;
    }

    /**
     * Set if show shadow
     *
     * @param istrue If show shadow
     */
    public void setDrawShadow(boolean istrue) {
        _drawShadow = istrue;
    }

    /**
     * Get shadow color
     *
     * @return Shadow color
     */
    public Color getShadowColor() {
        return _shadowColor;
    }

    /**
     * Set shadow color
     *
     * @param color Shadow color
     */
    public void setShadowColor(Color color) {
        _shadowColor = color;
    }

    /**
     * Get label align type
     *
     * @return Align type
     */
    public AlignType getLabelAlignType() {
        return _labelAlignType;
    }

    /**
     * Set label align type
     *
     * @param type Align type
     */
    public void setLabelAlignType(AlignType type) {
        _labelAlignType = type;
    }

    /**
     * Get x offset
     *
     * @return X offset
     */
    public int getXOffset() {
        return _xOffset;
    }

    /**
     * Set x offset
     *
     * @param value X offset
     */
    public void setXOffset(int value) {
        _xOffset = value;
    }

    /**
     * Get y offset
     *
     * @return Y offset
     */
    public int getYOffset() {
        return _yOffset;
    }

    /**
     * Set y offset
     *
     * @param value Y offset
     */
    public void setYOffset(int value) {
        _yOffset = value;
    }

    /**
     * Get if avoid collision
     *
     * @return If avoid collision
     */
    public boolean isAvoidCollision() {
        return _avoidCollision;
    }

    /**
     * Set if avoid collision
     *
     * @param istrue If avoid collision
     */
    public void setAvoidCollision(boolean istrue) {
        _avoidCollision = istrue;
    }

    /**
     * Get if set color by legend
     *
     * @return Boolean
     */
    public boolean isColorByLegend() {
        return _colorByLegend;
    }

    /**
     * Set if set color by legend
     *
     * @param istrue Boolean
     */
    public void setColorByLegend(boolean istrue) {
        _colorByLegend = istrue;
    }

    /**
     * Get if using dynamic contour label
     *
     * @return Boolean
     */
    public boolean isDynamicContourLabel() {
        return _dynamicContourLabel;
    }

    /**
     * Set if using dynamic contour label
     *
     * @param istrue Boolean
     */
    public void setDynamicContourLabel(boolean istrue) {
        _dynamicContourLabel = istrue;
    }

    /// <summary>
    /// Get or set if automatic set decimal digits
    /// </summary>
    /**
     * Get if automatic set decimal digits
     *
     * @return Boolean
     */
    public boolean isAutoDecimal() {
        return _autoDecimal;
    }

    /**
     * Set if automatic set decimal digits
     *
     * @param istrue Boolean
     */
    public void setAutoDecimal(boolean istrue) {
        _autoDecimal = istrue;
    }

    /**
     * Get decimal digits
     *
     * @return Decimal digits
     */
    public int getDecimalDigits() {
        return _decimalDigits;
    }

    /**
     * Set decimal digits
     *
     * @param value Decimal digits
     */
    public void setDecimalDigits(int value) {
        _decimalDigits = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
