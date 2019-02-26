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

import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.shape.ShapeTypes;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class LayerNode extends ItemNode {
    // <editor-fold desc="Variables">

    private MapLayer _mapLayer = null;
    private int _groupHandle = -1;
    private final List<LegendNode> _legendNodes = new ArrayList<>();
    private MapFrame _mapFrame = null;
    private boolean editing = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param aLayer
     */
    public LayerNode(MapLayer aLayer) {
        super();
        _mapLayer = aLayer;
        this.setText(aLayer.getLayerName());
        this.setChecked(aLayer.isVisible());
        this.setNodeType(NodeTypes.LayerNode);
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
     * Get layer handle
     *
     * @return The layer handle
     */
    public int getLayerHandle() {
        return this._mapLayer.getHandle();
    }

    /**
     * Get map layer
     *
     * @return The map layer
     */
    public MapLayer getMapLayer() {
        return this._mapLayer;
    }

    /**
     * Set map layer
     *
     * @param layer The map layer
     */
    public void setMapLayer(MapLayer layer) {
        this._mapLayer = layer;
    }

    /**
     * Get the layer legend scheme
     *
     * @return The legend scheme
     */
    public LegendScheme getLegendScheme() {
        return this._mapLayer.getLegendScheme();
    }

    /**
     * Get the layer shape type
     *
     * @return The shape type
     */
    public ShapeTypes getShapeType() {
        return this._mapLayer.getShapeType();
    }

    /**
     * Get legend node list
     *
     * @return The legend node list
     */
    public List<LegendNode> getLegendNodes() {
        return this._legendNodes;
    }

    /**
     * Get group handle
     *
     * @return The group handle
     */
    public int getGroupHandle() {
        return this._groupHandle;
    }

    /**
     * Set group handle
     *
     * @param handle
     */
    public void setGroupHandle(int handle) {
        this._groupHandle = handle;
    }
    
    /**
     * Get if is editing
     * @return Boolean
     */
    public boolean isEditing(){
        return editing;
    }
    
    /**
     * Set if is editing
     * @param value Boolean
     */
    public void setEditing(boolean value){
        editing = value;
        if (this._mapLayer.getLayerType() == LayerTypes.VectorLayer)
            ((VectorLayer)this._mapLayer).setEditing(value);
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Update properties
     */
    public void update() {
        this.setText(_mapLayer.getLayerName());
        this.setChecked(_mapLayer.isVisible());
        updateLegendScheme(_mapLayer.getLegendScheme());
    }

    /**
     * Update legend nodes using a legend scheme
     *
     * @param aLS The legend scheme
     */
    public void updateLegendScheme(LegendScheme aLS) {
        if (aLS == null){
            this._legendNodes.clear();
            return;
        }
        
        switch (_mapLayer.getLayerType()) {
            case VectorLayer:
            case RasterLayer:
                _legendNodes.clear();
                LegendNode aTN;
                for (int i = 0; i < aLS.getBreakNum(); i++) {
                    if (aLS.getLegendBreaks().get(i).isDrawShape()) {
                        aTN = new LegendNode();
                        aTN.setShapeType(this.getShapeType());
                        aTN.setLegendBreak(aLS.getLegendBreaks().get(i));
                        _legendNodes.add(aTN);
                    }
                }

                if (_mapLayer.getLayerType() == LayerTypes.VectorLayer) {
                    VectorLayer aLayer = (VectorLayer) _mapLayer;
                    if (aLayer.getChartSet().isDrawCharts() && aLayer.getChartPoints().size() > 0) {
                        LegendNode aLN = new LegendNode();
                        aLN.setShapeType(ShapeTypes.Polygon);
                        ChartBreak aCB = ((ChartBreak) aLayer.getChartPoints().get(0).getLegend()).getSampleChartBreak();
                        aLN.setLegendBreak(aCB);
                        aLN.setHeight(((ChartBreak) aLN.getLegendBreak()).getHeight() + 10);
                        _legendNodes.add(aLN);
                        for (int i = 0; i < aLayer.getChartSet().getLegendScheme().getBreakNum(); i++) {
                            aLN = new LegendNode();
                            aLN.setShapeType(ShapeTypes.Polygon);
                            aLN.setLegendBreak(aLayer.getChartSet().getLegendScheme().getLegendBreaks().get(i));
                            _legendNodes.add(aLN);
                        }
                    }
                }
                break;
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
        for (LegendNode legNode : _legendNodes) {
            height += legNode.getHeight() + Constants.ITEM_PAD;
        }

        return height;
    }

    /**
     * Clone
     *
     * @return LayerNode object
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        LayerNode aLN = new LayerNode((MapLayer) _mapLayer.clone());
        if (this.isExpanded()) {
            aLN.expand();
        }

        if (_legendNodes.size() > 0) {
            for (LegendNode aLegNode : _legendNodes) {
                aLN.getLegendNodes().add((LegendNode) aLegNode.clone());
            }
        }

        return aLN;
    }
    // </editor-fold>
}
