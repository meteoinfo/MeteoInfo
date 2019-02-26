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

import org.meteoinfo.global.event.ISizeChangedListener;
import org.meteoinfo.global.event.SizeChangedEvent;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import javax.swing.event.EventListenerList;
import org.meteoinfo.global.util.GlobalUtil;

/**
 * Label break class
 *
 * @author Yaqiang Wang
 */
public class LabelBreak extends ColorBreak {
    // <editor-fold desc="Variables">

    private EventListenerList _listeners = new EventListenerList();
    private String _text;
    private float _angle;
    private Font _font;
    private AlignType _alignType;
    private float _xShift;
    private float _yShift;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public LabelBreak() {
        super();
        this.setBreakType(BreakTypes.LabelBreak);
        _text = "";
        _angle = 0;
        this.setColor(Color.black);        
        _font = new Font(GlobalUtil.getDefaultFontName(), Font.PLAIN, 7);
        _alignType = AlignType.Center;
        _yShift = 0;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get text string
     *
     * @return Text string
     */
    public String getText() {
        return _text;
    }

    /**
     * Set text string and fire size changed event
     *
     * @param text Text string
     */
    public void setText(String text) {
        _text = text;
        this.fireSizeChangedEvent(new SizeChangedEvent(this));
    }

    /**
     * Get angle
     *
     * @return Angle
     */
    public float getAngle() {
        return _angle;
    }

    /**
     * Set angle
     *
     * @param angle Angle
     */
    public void setAngle(float angle) {
        _angle = angle;
    }

    /**
     * Get font
     *
     * @return Font
     */
    public Font getFont() {
        return _font;
    }

    /**
     * Set font and fire size changed event
     *
     * @param f Font
     */
    public void setFont(Font f) {
        _font = f;
        this.fireSizeChangedEvent(new SizeChangedEvent(this));
    }

    /**
     * Get align type
     *
     * @return Align type
     */
    public AlignType getAlignType() {
        return _alignType;
    }

    /**
     * Set align type
     *
     * @param at Align type
     */
    public void setAlignType(AlignType at) {
        _alignType = at;
    }

    /**
     * Get y shift
     *
     * @return Y shift
     */
    public float getYShift() {
        return _yShift;
    }

    /**
     * Set y shift
     *
     * @param yshift Y shift
     */
    public void setYShift(float yshift) {
        _yShift = yshift;
    }

    /**
     * Get x shift
     *
     * @return X shift
     */
    public float getXShift() {
        return _xShift;
    }

    /**
     * Set x shift
     *
     * @param xshift X shift
     */
    public void setXShift(float xshift) {
        _xShift = xshift;
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
        objAttr.put("Text", "Text");
        objAttr.put("Angle", "Angle");
        objAttr.put("Color", "Color");
        objAttr.put("Font", "Font");
        //objAttr.Add("AlignType", "AlignType");
        //objAttr.Add("YShift", "YShift");
        //CustomProperty cp = new CustomProperty(this, objAttr);
        return objAttr;
    }

    /**
     * Clone
     *
     * @return LabelBreak object
     */
    @Override
    public Object clone() {
        LabelBreak aCB = new LabelBreak();
        aCB.setCaption(this.getCaption());
        aCB.setColor(this.getColor());
        aCB.setDrawShape(this.isDrawShape());
        aCB.setEndValue(this.getEndValue());
        aCB.setNoData(this.isNoData());
        aCB.setStartValue(this.getStartValue());
        aCB.setAngle(_angle);
        aCB.setText(_text);
        aCB.setFont(_font);
        aCB.setAlignType(_alignType);
        aCB.setYShift(_yShift);
        aCB.setXShift(_xShift);

        return aCB;
    }
    // </editor-fold>
    // <editor-fold desc="Events">

    /**
     * Add size changed listener
     *
     * @param scl SizeChangedListener interface
     */
    public void addSizeChangedListener(ISizeChangedListener scl) {
        this._listeners.add(ISizeChangedListener.class, scl);
    }

    /**
     * Remove size changed listener
     *
     * @param scl SizeChangedListener interface
     */
    public void removeSizeChangedListener(ISizeChangedListener scl) {
        this._listeners.remove(ISizeChangedListener.class, scl);
    }

    /**
     * Fire size changed event
     *
     * @param event SizeChangedEvent
     */
    public void fireSizeChangedEvent(SizeChangedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ISizeChangedListener.class) {
                ((ISizeChangedListener) listeners[i + 1]).sizeChangedEvent(event);
            }
        }
    }
    // </editor-fold>
}