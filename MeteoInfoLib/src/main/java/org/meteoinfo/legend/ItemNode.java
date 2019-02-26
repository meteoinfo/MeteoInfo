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

import java.awt.Color;

/**
 *
 * @author yaqiang
 */
public abstract class ItemNode {
    // <editor-fold desc="Variables">

    private LayersLegend _parentLegend = null;
    private int _top;
    private int _height = Constants.ITEM_HEIGHT;
    private boolean _isExpanded;
    private boolean _checked;
    private String _text;
    private Color _backColor;
    private Color _foreColor;
    private NodeTypes _nodeType;
    private boolean _selected;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public ItemNode() {        
        _isExpanded = false;
        _checked = true;
        _backColor = Color.white;
        _foreColor = Color.black;
        _selected = false;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get parent layers legend
     *
     * @return LayersLegend
     */
    public LayersLegend getParentLegend() {
        return _parentLegend;
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
    }

    /**
     * Get if is expanded
     *
     * @return Boolean
     */
    public boolean isExpanded() {
        return _isExpanded;
    }

    /**
     * Get if is checked
     *
     * @return Boolean
     */
    public boolean isChecked() {
        return _checked;
    }

    /**
     * Set if is checked
     *
     * @param checked Boolean
     */
    public void setChecked(boolean checked) {
        _checked = checked;
    }

    /**
     * Get text
     *
     * @return The text
     */
    public String getText() {
        return _text;
    }

    /**
     * Set text
     *
     * @param text The text
     */
    public void setText(String text) {
        _text = text;
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
     * @param backColor Background color
     */
    public void setBackColor(Color backColor) {
        _backColor = backColor;
    }

    /**
     * Get foreground color
     *
     * @return foreground color
     */
    public Color getForeColor() {
        return _foreColor;
    }

    /**
     * Set foreground color
     *
     * @param foreColor Foreground color
     */
    public void setForeColor(Color foreColor) {
        _foreColor = foreColor;
    }

    /**
     * Get node type
     *
     * @return The node type
     */
    public NodeTypes getNodeType() {
        return _nodeType;
    }

    /**
     * Set node type
     *
     * @param nodeType The node type
     */
    public void setNodeType(NodeTypes nodeType) {
        _nodeType = nodeType;
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
     * @param sel Boolean
     */
    public void setSelected(boolean sel) {
        _selected = sel;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set parent legend
     *
     * @param aLegend The layers legend
     */
    public void setParentLegend(LayersLegend aLegend) {
        _parentLegend = aLegend;
    }

    /**
     * Expand the node
     */
    public void expand() {
        _isExpanded = true;
    }

    /**
     * Collapse the node
     */
    public void collapse() {
        _isExpanded = false;
    }

//    /**
//     * Clone
//     *
//     * @return ItemNode object
//     */
//    @Override
//    public Object clone() {
//        ItemNode aNode = new ItemNode();
//        aNode.setHeight(_height);
//        aNode.setChecked(_checked);
//        aNode.setBackColor(_backColor);
//        aNode.setForeColor(_foreColor);
//        if (_isExpanded) {
//            aNode.expand();
//        } else {
//            aNode.collapse();
//        }
//
//        aNode.setText(_text);
//        aNode.setTop(_top);
//
//        return aNode;
//    }

    /**
     * Get expanded height
     *
     * @return The expanded height
     */
    public abstract int getExpandedHeight();

    /**
     * Get drawing height
     *
     * @return The drawing height
     */
    public abstract int getDrawHeight();
    // </editor-fold>
}
