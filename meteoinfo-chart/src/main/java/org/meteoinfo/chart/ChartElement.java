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
package org.meteoinfo.chart;

import org.meteoinfo.common.PointF;
import org.meteoinfo.ui.event.ILocationChangedListener;
import org.meteoinfo.ui.event.ISizeChangedListener;
import org.meteoinfo.ui.event.LocationChangedEvent;
import org.meteoinfo.ui.event.SizeChangedEvent;

import java.awt.Color;
import java.awt.Rectangle;
import javax.swing.event.EventListenerList;
import org.meteoinfo.geometry.graphic.ResizeAbility;

/**
 *
 * @author yaqiang
 */
public abstract class ChartElement {
    // <editor-fold desc="Events">

    public void addLocationChangedListener(ILocationChangedListener listener) {
        this._listeners.add(ILocationChangedListener.class, listener);
    }

    public void removeLocationChangedListener(ILocationChangedListener listener) {
        this._listeners.remove(ILocationChangedListener.class, listener);
    }

    public void fireLocationChangedEvent() {
        fireLocationChangedEvent(new LocationChangedEvent(this));
    }

    private void fireLocationChangedEvent(LocationChangedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ILocationChangedListener.class) {
                ((ILocationChangedListener) listeners[i + 1]).locationChangedEvent(event);
            }
        }
    }

    public void addSizeChangedListener(ISizeChangedListener listener) {
        this._listeners.add(ISizeChangedListener.class, listener);
    }

    public void removeSizeChangedListener(ISizeChangedListener listener) {
        this._listeners.remove(ISizeChangedListener.class, listener);
    }

    public void fireSizeChangedEvent() {
        fireSizeChangedEvent(new SizeChangedEvent(this));
    }

    private void fireSizeChangedEvent(SizeChangedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ISizeChangedListener.class) {
                ((ISizeChangedListener) listeners[i + 1]).sizeChangedEvent(event);
            }
        }
    }
    // </editor-fold>
    // <editor-fold desc="Variables">
    private final EventListenerList _listeners = new EventListenerList();
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected Color _foreColor;
    protected Color _backColor;
    private boolean _selected;
    private ResizeAbility _resizeAbility;
    private boolean _visible = true;
    private boolean drawBackColor = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public ChartElement() {
        _foreColor = Color.black;
        _backColor = Color.white;
        _selected = false;
        _resizeAbility = ResizeAbility.NONE;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get if visible
     *
     * @return Boolean
     */
    public boolean isVisible() {
        return _visible;
    }

    /**
     * Set if visible
     *
     * @param istrue Boolean
     */
    public void setVisible(boolean istrue) {
        _visible = istrue;
    }

    /**
     * Get x
     *
     * @return x
     */
    public float getX() {
        return x;
    }

    /**
     * Set left
     *
     * @param left
     */
    public void setX(float left) {
        x = left;
        this.fireLocationChangedEvent();
    }

    /**
     * Get y
     *
     * @return Y
     */
    public float getY() {
        return y;
    }

    /**
     * Set y
     *
     * @param top Y
     */
    public void setY(float top) {
        y = top;
        this.fireLocationChangedEvent();
    }

    /**
     * Get width
     *
     * @return Width
     */
    public float getWidth() {
        return width;
    }

    /**
     * Set width
     *
     * @param width Width
     */
    public void setWidth(float width) {
        this.width = width;
        this.fireSizeChangedEvent();
    }

    /**
     * Get height
     *
     * @return Height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set height
     *
     * @param height Height
     */
    public void setHeight(float height) {
        this.height = height;
        this.fireSizeChangedEvent();
    }

    /**
     * Get right
     *
     * @return Right
     */
    public float getRight() {
        return x + width;
    }

    /**
     * Get bottom
     *
     * @return Bottom
     */
    public float getBottom() {
        return y + height;
    }

    /**
     * Get bounds rectangle
     *
     * @return Bounds rectangle
     */
    public Rectangle.Float getBounds() {
        return new Rectangle.Float(x, y, width, height);
    }

    /**
     * Get foreground color
     *
     * @return Foreground color
     */
    public Color getForeground() {
        return _foreColor;
    }

    /**
     * Set foreground color
     *
     * @param color Foreground color
     */
    public void setForeground(Color color) {
        _foreColor = color;
    }

    /**
     * Get background color
     *
     * @return Background color
     */
    public Color getBackground() {
        return _backColor;
    }

    /**
     * Set background color
     *
     * @param color Background color
     */
    public void setBackground(Color color) {
        _backColor = color;
    }

    /**
     * Get if is selected
     *
     * @return Boolean
     */
    public boolean isSelected() {
        return _selected;
    }

    /**
     * Set if is selected
     *
     * @param istrue Boolean
     */
    public void setSelected(boolean istrue) {
        _selected = istrue;
    }

    /**
     * Get resize ability
     *
     * @return Resize ability
     */
    public ResizeAbility getResizeAbility() {
        return _resizeAbility;
    }

    /**
     * Set resize ability
     *
     * @param ra Resize ability
     */
    public void setResizeAbility(ResizeAbility ra) {
        _resizeAbility = ra;
    }
    
    /**
     * Get is draw backcolor
     * @return Boolean
     */
    public boolean isDrawBackColor(){
        return drawBackColor;
    }
    
    /**
     * Set is draw backcolor
     * @param value Boolean
     */
    public void setDrawBackColor(boolean value){
        drawBackColor = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Move update method
     */
    public abstract void moveUpdate();

    /**
     * Resize update method
     */
    public abstract void resizeUpdate();

    /**
     * Page to screen
     *
     * @param pageX Page X
     * @param pageY Page Y
     * @param pageLocation Page location
     * @param zoom Zoom
     * @return Screen point
     */
    public PointF pageToScreen(float pageX, float pageY, PointF pageLocation, float zoom) {
        float x = pageX * zoom + pageLocation.X;
        float y = pageY * zoom + pageLocation.Y;
        return (new PointF(x, y));
    }
    // </editor-fold>
}
