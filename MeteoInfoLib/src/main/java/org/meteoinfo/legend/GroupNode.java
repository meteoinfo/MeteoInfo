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

import com.l2fprod.common.beans.BaseBeanInfo;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class GroupNode extends ItemNode {
    // <editor-fold desc="Variables">

    private int _groupHandle;
    private List<LayerNode> _layers;
    private int _checkStatus = 0;
    private MapFrame _mapFrame = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param name
     */
    public GroupNode(String name) {
        super();
        _groupHandle = -1;
        this.setText(name);
        _layers = new ArrayList<LayerNode>();
        this.setNodeType(NodeTypes.GroupNode);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get map frame
     *
     * @return The map frame
     */
    public MapFrame getMapFrame() {
        return _mapFrame;
    }

    /**
     * Set map frame
     *
     * @param mf The map frame
     */
    public void setMapFrame(MapFrame mf) {
        _mapFrame = mf;
    }

    /**
     * Get group handle
     *
     * @return The group handle
     */
    public int getGroupHandle() {
        return _groupHandle;
    }

    /**
     * Set group handle
     *
     * @param handle
     */
    public void setGroupHandle(int handle) {
        _groupHandle = handle;
    }

    /**
     * Get layer nodes
     *
     * @return Layer node list
     */
    public List<LayerNode> getLayers() {
        return this._layers;
    }

    /**
     * Get check status
     *
     * @return The check status
     */
    public int getCheckStatus() {
        return this._checkStatus;
    }

    /**
     * Set check status
     *
     * @param s
     */
    public void setCheckStatus(int s) {
        this._checkStatus = s;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Add a layer node
     *
     * @param aLayer The layer node
     */
    public void addLayer(LayerNode aLayer) {
        _layers.add(aLayer);
        aLayer.setGroupHandle(_groupHandle);
    }

    /**
     * Remove a layer node
     *
     * @param aLayer The layer node
     */
    public void removeLayer(LayerNode aLayer) {
        _layers.remove(aLayer);
        aLayer.setGroupHandle(-1);
    }

    /**
     * Insert a layer node
     *
     * @param index The index
     * @param aLayer The layer node
     */
    public void addLayer(int index, LayerNode aLayer) {
        _layers.add(index, aLayer);
        aLayer.setGroupHandle(_groupHandle);
    }

    /**
     * Get layer node index
     *
     * @param aLayer The layer node
     * @return The index
     */
    public int getLayerIndex(LayerNode aLayer) {
        return _layers.indexOf(aLayer);
    }

    /**
     * Insert layer node
     *
     * @param aLayer Layer node
     * @param index Index
     */
    public void insertLayer(LayerNode aLayer, int index) {
        _layers.add(index, aLayer);
        aLayer.setGroupHandle(_groupHandle);
    }

    /**
     * Update check status
     */
    public void updateCheckStatus() {
        boolean allChecked = true;
        boolean hasChecked = false;
        for (LayerNode aLN : _layers) {
            if (aLN.isChecked()) {
                hasChecked = true;
            } else {
                allChecked = false;
            }
        }

        if (allChecked) {
            _checkStatus = 1;
        } else if (hasChecked) {
            _checkStatus = 2;
        } else {
            _checkStatus = 0;
        }
    }

    @Override
    public int getDrawHeight() {
        if (this.isExpanded()) {
            return getExpandedHeight();
        } else {
            return this.getHeight();
        }
    }

    @Override
    public int getExpandedHeight() {
        int height = this.getHeight();
        for (LayerNode layerNode : _layers) {
            int lnHeight;
            if (layerNode.isExpanded()) {
                lnHeight = layerNode.getExpandedHeight();
            } else {
                lnHeight = layerNode.getHeight();
            }

            height += lnHeight + Constants.ITEM_PAD;
        }

        return height;
    }
    // </editor-fold>
    
    // <editor-fold desc="BeanInfo">
    public class GroupNodeBean {
        public GroupNodeBean(){
            
        }
        
        // <editor-fold desc="Get Set Methods">
        /**
         * Get text
         * @return Text
         */
        public String getText(){
            return GroupNode.this.getText();
        }
        
        /**
         * Set text
         * @param value Text
         */
        public void setText(String value){
            GroupNode.this.setText(value);
            GroupNode.this._mapFrame.getLegend().paintGraphics();
        }
        // </editor-fold>
    }
    
    public static class GroupNodeBeanBeanInfo extends BaseBeanInfo{
        public GroupNodeBeanBeanInfo(){
            super(GroupNodeBean.class);
            addProperty("text").setCategory("General").setDisplayName("Text");
        }
    }
    // </editor-fold>
}
