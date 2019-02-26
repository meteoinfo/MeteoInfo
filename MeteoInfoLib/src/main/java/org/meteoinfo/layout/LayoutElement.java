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
package org.meteoinfo.layout;

import org.meteoinfo.global.event.ILocationChangedListener;
import org.meteoinfo.global.event.ISizeChangedListener;
import org.meteoinfo.global.event.LocationChangedEvent;
import org.meteoinfo.global.event.SizeChangedEvent;
import org.meteoinfo.global.PointF;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.event.EventListenerList;

/**
 *
 * @author yaqiang
 */
public abstract class LayoutElement {
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
    private int _left;
    private int _top;
    private int _width;
    private int _height;
    private ElementType _elementType;
    private Color _foreColor;
    private Color _backColor;
    private boolean _selected;
    private ResizeAbility _resizeAbility;
    private boolean _visible = true;
    private boolean drawBackColor = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public LayoutElement() {
        _foreColor = Color.black;
        _backColor = Color.white;
        _selected = false;
        _resizeAbility = ResizeAbility.None;        
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
     * Get left
     *
     * @return Left
     */
    public int getLeft() {
        return _left;
    }

    /**
     * Set left
     *
     * @param left
     */
    public void setLeft(int left) {
        _left = left;
        this.fireLocationChangedEvent();
    }

    /**
     * Get top
     *
     * @return Top
     */
    public int getTop() {
        return _top;
    }

    /**
     * Set top
     *
     * @param top Top
     */
    public void setTop(int top) {
        _top = top;
        this.fireLocationChangedEvent();
    }

    /**
     * Get width
     *
     * @return Width
     */
    public int getWidth() {
        return _width;
    }

    /**
     * Set width
     *
     * @param width Width
     */
    public void setWidth(int width) {
        _width = width;
        this.fireSizeChangedEvent();
    }

    /**
     * Get height
     *
     * @return Height
     */
    public int getHeight() {
        return _height;
    }

    /**
     * Set height
     *
     * @param height Height
     */
    public void setHeight(int height) {
        _height = height;
        this.fireSizeChangedEvent();
    }

    /**
     * Get right
     *
     * @return Right
     */
    public int getRight() {
        return _left + _width;
    }

    /**
     * Get bottom
     *
     * @return Bottom
     */
    public int getBottom() {
        return _top + _height;
    }

    /**
     * Get bounds rectangle
     *
     * @return Bounds rectangle
     */
    public Rectangle getBounds() {
        return new Rectangle(_left, _top, _width, _height);
    }

    /**
     * Get element type
     *
     * @return The element type
     */
    public ElementType getElementType() {
        return _elementType;
    }

    /**
     * Set element type
     *
     * @param type Element type
     */
    public void setElementType(ElementType type) {
        _elementType = type;
    }

    /**
     * Get foreground color
     *
     * @return Foreground color
     */
    public Color getForeColor() {
        return _foreColor;
    }

    /**
     * Set foreground color
     *
     * @param color Foreground color
     */
    public void setForeColor(Color color) {
        _foreColor = color;
    }

    /**
     * Get background color
     *
     * @return Background color
     */
    public Color getBackColor() {
        return _backColor;
    }

    /**
     * Set background color
     *
     * @param color Background color
     */
    public void setBackColor(Color color) {
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
     * Paint method
     *
     * @param g Graphics2D
     */
    public abstract void paint(Graphics2D g);

    /**
     * Paint on layout method
     *
     * @param g Grahpics2D
     * @param pageLocation Page location
     * @param zoom Zoom
     */
    public abstract void paintOnLayout(Graphics2D g, PointF pageLocation, float zoom);

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
