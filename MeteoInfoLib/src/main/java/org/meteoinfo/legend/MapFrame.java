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
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.global.event.ILayersUpdatedListener;
import org.meteoinfo.global.event.ILayoutBoundsChangedListener;
import org.meteoinfo.global.event.IMapViewUpdatedListener;
import org.meteoinfo.global.event.IViewExtentChangedListener;
import org.meteoinfo.global.event.LayersUpdatedEvent;
import org.meteoinfo.global.event.LayoutBoundsChangedEvent;
import org.meteoinfo.global.event.MapViewUpdatedEvent;
import org.meteoinfo.global.event.ViewExtentChangedEvent;
import org.meteoinfo.layer.ImageLayer;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.map.MapView;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import static org.meteoinfo.layer.LayerTypes.WebMapLayer;
import org.meteoinfo.layer.RasterLayer;
import org.meteoinfo.layer.WebMapLayer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author yaqiang
 */
public class MapFrame extends ItemNode {
    // <editor-fold desc="Events">

    /**
     * Add layers updated event listener
     *
     * @param lul LayersUpdatedListener interface
     */
    public void addLayersUpdatedListener(ILayersUpdatedListener lul) {
        this._listeners.add(ILayersUpdatedListener.class, lul);
    }

    public void removeLayersUpdatedListener(ILayersUpdatedListener lul) {
        this._listeners.remove(ILayersUpdatedListener.class, lul);
    }

    public void fireLayersUpdatedEvent() {
        fireLayersUpdatedEvent(new LayersUpdatedEvent(this));
    }

    private void fireLayersUpdatedEvent(LayersUpdatedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ILayersUpdatedListener.class) {
                ((ILayersUpdatedListener) listeners[i + 1]).layersUpdatedEvent(event);
            }
        }
    }

    public void addMapViewUpdatedListener(IMapViewUpdatedListener listener) {
        this._listeners.add(IMapViewUpdatedListener.class, listener);
    }

    public void removeMapViewUpdatedListener(IMapViewUpdatedListener listener) {
        this._listeners.remove(IMapViewUpdatedListener.class, listener);
    }

    public void fireMapViewUpdatedEvent() {
        fireMapViewUpdatedEvent(new MapViewUpdatedEvent(this));
    }

    private void fireMapViewUpdatedEvent(MapViewUpdatedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IMapViewUpdatedListener.class) {
                ((IMapViewUpdatedListener) listeners[i + 1]).mapViewUpdatedEvent(event);
            }
        }
    }

    public void addLayoutBoundsChangedListener(ILayoutBoundsChangedListener listener) {
        this._listeners.add(ILayoutBoundsChangedListener.class, listener);
    }

    public void removeLayoutBoundsChangedListener(ILayoutBoundsChangedListener listener) {
        this._listeners.remove(ILayoutBoundsChangedListener.class, listener);
    }

    public void fireLayoutBoundsChangedEvent() {
        fireLayoutBoundsChangedEvent(new LayoutBoundsChangedEvent(this));
    }

    private void fireLayoutBoundsChangedEvent(LayoutBoundsChangedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ILayoutBoundsChangedListener.class) {
                ((ILayoutBoundsChangedListener) listeners[i + 1]).layoutBoundsChangedEvent(event);
            }
        }
    }
    // </editor-fold>
    // <editor-fold desc="Variables">
    private EventListenerList _listeners = new EventListenerList();
    private MapView _mapView = new MapView();
    private List<ItemNode> _nodes = new ArrayList<>();
    private int _selectedLayerHandle;
    private LayersLegend _legend;
    private boolean _active = false;
    private int _order;
    private boolean _drawNeatLine = true;
    private boolean _insideTickLine = false;
    private int _tickLineLength = 5;
    private int _gridLabelShift = 2;
    private Color _neatLineColor = Color.black;
    private float _neatLineSize = 1.0f;
    private boolean _drawGridLabel = true;
    private GridLabelPosition _gridLabelPosition = GridLabelPosition.LEFT_BOTTOM;
    private Font _gridFont = new Font("Arial", Font.PLAIN, 12);
    private Rectangle _layoutBounds;
    //private boolean _isFireMapViewUpdate = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MapFrame() {
        this.setText("New Map Frame");
        this.setNodeType(NodeTypes.MapFrameNode);
        this.expand();
        _layoutBounds = new Rectangle(100, 100, 300, 200);

        _mapView.addViewExtentChangedListener(new IViewExtentChangedListener() {
            @Override
            public void viewExtentChangedEvent(ViewExtentChangedEvent event) {
                fireMapViewUpdatedEvent();
            }
        });
//            _mapView.LayersUpdated += MapViewLayersUpdated;
//            _mapView.MapViewRedrawed += MapViewRedrawed;
//            _mapView.ProjectionChanged += new EventHandler(MapViewProjectionChanged);
    }

    /**
     * Constructor
     *
     * @param legend LayersLegend
     */
    public MapFrame(LayersLegend legend) {
        this();
        _legend = legend;

    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get map view
     *
     * @return The map view
     */
    public MapView getMapView() {
        return _mapView;
    }

    /**
     * Set map view
     * @param mapView The map view 
     */
    public void setMapView(MapView mapView) {
        _mapView = mapView;
        _mapView.addViewExtentChangedListener(new IViewExtentChangedListener() {
            @Override
            public void viewExtentChangedEvent(ViewExtentChangedEvent even) {
                //if (_isFireMapViewUpdate) {
                fireMapViewUpdatedEvent();
                //}
            }
        });

//        if (_mapView.getProjection().isLonLatMap()) {
//            _gridLabelPosition = GridLabelPosition.LEFT_BOTTOM;
//        } else {
//            _gridLabelPosition = GridLabelPosition.ALL;
//        }
    }

    /**
     * Get nodes
     *
     * @return The nodes
     */
    public List<ItemNode> getNodes() {
        return _nodes;
    }

    /**
     * Set nodes
     *
     * @param nodes The nodes
     */
    public void setNodes(List<ItemNode> nodes) {
        _nodes = nodes;
    }

    /**
     * Get selected layer handle
     *
     * @return The selected layer handle
     */
    public int getSelectedLayer() {
        return this._selectedLayerHandle;
    }

    /**
     * Set selected layer handle
     *
     * @param handle The selected layer handle
     */
    public void setSelectedLayer(int handle) {
        this._selectedLayerHandle = handle;
    }

    /**
     * Get layers legend
     *
     * @return LayersLegend
     */
    public LayersLegend getLegend() {
        return this._legend;
    }

    /**
     * Set layers legend
     *
     * @param legend
     */
    public void setLegend(LayersLegend legend) {
        this._legend = legend;
    }

    /**
     * Get if is active
     *
     * @return Boolean
     */
    public boolean isActive() {
        return _active;
    }

    /**
     * Set if is active
     *
     * @param istrue Boolean
     */
    public void setActive(boolean istrue) {
        _active = istrue;
    }

    /**
     * Get order
     *
     * @return The order
     */
    public int getOrder() {
        return _order;
    }

    /**
     * Set order
     *
     * @param order The order
     */
    public void setOrder(int order) {
        _order = order;
    }

    /**
     * Get if draw neat line
     *
     * @return Boolean
     */
    public boolean isDrawNeatLine() {
        return _drawNeatLine;
    }

    /**
     * Set if draw neat line
     *
     * @param istrue Boolean
     */
    public void setDrawNeatLine(boolean istrue) {
        _drawNeatLine = istrue;
    }

    /**
     * Get neat line color
     *
     * @return The neat line color
     */
    public Color getNeatLineColor() {
        return _neatLineColor;
    }

    /**
     * Set neat line color
     *
     * @param color The color
     */
    public void setNeatLineColor(Color color) {
        _neatLineColor = color;
    }

    /**
     * Get neat line size
     *
     * @return The neat line size
     */
    public float getNeatLineSize() {
        return _neatLineSize;
    }

    /**
     * Set neat line size
     *
     * @param size The size
     */
    public void setNeatLineSize(float size) {
        _neatLineSize = size;
    }

    /**
     * Get if draw grid label
     *
     * @return Boolean
     */
    public boolean isDrawGridLabel() {
        return _drawGridLabel;
    }

    /**
     * Set if draw grid label
     *
     * @param istrue Boolean
     */
    public void setDrawGridLabel(boolean istrue) {
        _drawGridLabel = istrue;
    }

    /**
     * Get if draw tick line inside of the border
     *
     * @return Boolean
     */
    public boolean isInsideTickLine() {
        return _insideTickLine;
    }

    /**
     * Set if draw tick line inside of the border
     *
     * @param istrue Boolean
     */
    public void setInsideTickLine(boolean istrue) {
        _insideTickLine = istrue;
    }

    /**
     * Get tick line length
     *
     * @return Tick line length
     */
    public int getTickLineLength() {
        return _tickLineLength;
    }

    /**
     * Set tick line length
     *
     * @param len The length
     */
    public void setTickLineLength(int len) {
        _tickLineLength = len;
    }

    /**
     * Get grid label shift
     *
     * @return Shift value
     */
    public int getGridLabelShift() {
        return _gridLabelShift;
    }

    /**
     * Set grid label shift
     *
     * @param shift Shift value
     */
    public void setGridLabelShift(int shift) {
        _gridLabelShift = shift;
    }

    /**
     * Get grid label position
     *
     * @return Grid label position
     */
    public GridLabelPosition getGridLabelPosition() {
        return _gridLabelPosition;
    }

    /**
     * Set grid label position
     *
     * @param pos Grid label position
     */
    public void setGridLabelPosition(GridLabelPosition pos) {
        _gridLabelPosition = pos;
    }
    
    /**
     * Set grid label position
     *
     * @param pos Grid label position
     */
    public void setGridLabelPosition(String pos) {
        GridLabelPosition glp = GridLabelPosition.valueOf(pos.toUpperCase());
        _gridLabelPosition = glp;
    }

    /**
     * Get grid label font
     *
     * @return Grid font
     */
    public Font getGridFont() {
        return _gridFont;
    }

    /**
     * Set grid label font
     *
     * @param font The font
     */
    public void setGridFont(Font font) {
        _gridFont = font;
    }

    /**
     * Get map frame name
     *
     * @return Map frmae name
     */
    public String getMapFrameName() {
        return this.getText();
    }

    /**
     * Set map frame name
     *
     * @param name The name
     */
    public void setMapFrameName(String name) {
        this.setText(name);
    }

    /**
     * Get map view background color
     *
     * @return Map view background color
     */
    @Override
    public Color getBackColor() {
        return _mapView.getBackground();
    }

    /**
     * Set map view background color
     *
     * @param color The color
     */
    @Override
    public void setBackColor(Color color) {
        _mapView.setBackground(color);
    }

    /**
     * Get map view foreground color
     *
     * @return Map view foreground color
     */
    @Override
    public Color getForeColor() {
        return _mapView.getForeground();
    }

    /**
     * Set map view foreground color
     *
     * @param color The color
     */
    @Override
    public void setForeColor(Color color) {
        _mapView.setForeground(color);
    }

    /**
     * Get grid line color
     *
     * @return Grid line color
     */
    public Color getGridLineColor() {
        return _mapView.getGridLineColor();
    }

    /**
     * Set grid line color
     *
     * @param color Grid line color
     */
    public void setGridLineColor(Color color) {
        _mapView.setGridLineColor(color);
    }

    /**
     * Get grid line size
     *
     * @return Grid line size
     */
    public float getGridLineSize() {
        return _mapView.getGridLineSize();
    }

    /**
     * Set grid line size
     *
     * @param size Grid line size
     */
    public void setGridLineSize(float size) {
        _mapView.setGridLineSize(size);
    }

    /**
     * Get grid line style
     *
     * @return Grid line style
     */
    public LineStyles getGridLineStyle() {
        return _mapView.getGridLineStyle();
    }

    /**
     * Set grid line style
     *
     * @param style Grid line style
     */
    public void setGridLineStyle(LineStyles style) {
        _mapView.setGridLineStyle(style);
    }

    /**
     * Get if draw grid line
     *
     * @return Boolean
     */
    public boolean isDrawGridLine() {
        return _mapView.isDrawGridLine();
    }

    /**
     * Set if draw grid line
     *
     * @param istrue Boolean
     */
    public void setDrawGridLine(boolean istrue) {
        _mapView.setDrawGridLine(istrue);
    }

    /**
     * Get if draw grid tick line
     *
     * @return Boolean
     */
    public boolean isDrawGridTickLine() {
        return _mapView.isDrawGridTickLine();
    }

    /**
     * Set if draw grid tick line
     *
     * @param istrue Boolean
     */
    public void setDrawGridTickLine(boolean istrue) {
        _mapView.setDrawGridTickLine(istrue);
    }

    /**
     * Get grid x delta
     *
     * @return Grid x delta
     */
    public double getGridXDelt() {
        return _mapView.getGridXDelt();
    }

    /**
     * Set grid x delta
     *
     * @param delta Grid x delta
     */
    public void setGridXDelt(double delta) {
        _mapView.setGridXDelt(delta);
    }

    /**
     * Get grid y delta
     *
     * @return Grid y delta
     */
    public double getGridYDelt() {
        return _mapView.getGridYDelt();
    }

    /**
     * Set grid y delta
     *
     * @param value Grid y delta
     */
    public void setGridYDelt(double value) {
        _mapView.setGridYDelt(value);
    }

    /**
     * Get grid x origin
     *
     * @return Grid x origin
     */
    public float getGridXOrigin() {
        return _mapView.getGridXOrigin();
    }

    /**
     * Set grid x origin
     *
     * @param value Grid x origin
     */
    public void setGridXOrigin(float value) {
        _mapView.setGridXOrigin(value);
    }

    /**
     * Get grid y origin
     *
     * @return Grid y origin
     */
    public float getGridYOrigin() {
        return _mapView.getGridYOrigin();
    }

    /**
     * Set grid y origin
     *
     * @param value Grid y origin
     */
    public void setGridYOrigin(float value) {
        _mapView.setGridYOrigin(value);
    }

    /**
     * Get layout bounds
     *
     * @return The layout bounds
     */
    public Rectangle getLayoutBounds() {
        return _layoutBounds;
    }

    /**
     * Set layout bounds
     *
     * @param lb Layout bounds
     */
    public void setLayoutBounds(Rectangle lb) {
        _layoutBounds = lb;
        this.fireLayoutBoundsChangedEvent();
    }

//    /**
//     * Get if fire map view upate event
//     *
//     * @return Boolean
//     */
//    public boolean isFireMapViewUpdate() {
//        return this._isFireMapViewUpdate;
//    }
//
//    /**
//     * Set if fire map view update event
//     *
//     * @param b Boolean
//     */
//    public void setIsFireMapViewUpdate(boolean b) {
//        this._isFireMapViewUpdate = b;
//    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Group">
    /**
     * Add a group node
     *
     * @param aGroup The group node
     * @return Group node handle
     */
    public int addGroup(GroupNode aGroup) {
        addNode(aGroup);

        return aGroup.getGroupHandle();
    }

    /**
     * Add a new group
     *
     * @param name Group name
     * @return Group handle
     */
    public int addNewGroup(String name) {
        GroupNode aGroup = new GroupNode(name);
        return (addGroup(aGroup));
    }

    /**
     * Rmove a group
     *
     * @param aGroup The group node
     */
    public void removeGroup(GroupNode aGroup) {
        MapFrame mapFrame = aGroup.getMapFrame();
        if (aGroup.getLayers().size() > 0) {
            if (JOptionPane.showConfirmDialog(null, "All layers of the group will be removed! Will you continue?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                int lNum = aGroup.getLayers().size();
                for (int i = 0; i < lNum; i++) {
                    LayerNode aLN = aGroup.getLayers().get(0);
                    removeLayer(aLN);
                }
                mapFrame.removeNode(aGroup);
            }
        } else {
            mapFrame.removeNode(aGroup);
        }
    }

    // </editor-fold>
    // <editor-fold desc="Nodes">
    /**
     * Add a layer
     *
     * @param aLayer The layer
     * @return The layer handle
     */
    public int addLayer(MapLayer aLayer) {
        LayerNode aLN = new LayerNode(aLayer);
        this.selectLayer(aLN);
        return addLayerNode(aLN);
    }

    /**
     * Add a layer
     *
     * @param index The index
     * @param layer The layer
     * @return The layer handle
     */
    public int addLayer(int index, MapLayer layer) {
        LayerNode ln = new LayerNode(layer);
        this.selectLayer(ln);
        return addLayerNode(index, ln);
    }

    /**
     * Add wind layer
     *
     * @param aLayer Wind layer
     * @param earthWind If wind relative to earth
     * @return Layer handle
     */
    public int addWindLayer(VectorLayer aLayer, boolean earthWind) {
        LayerNode aLN = new LayerNode(aLayer);
        this.selectLayer(aLN);
        return addLayerNode(aLN, earthWind);
    }

    /**
     * Add a layer in a group
     *
     * @param aLayer The layer
     * @param groupHandle The group handle
     * @return Layer handle
     */
    public int addLayer(MapLayer aLayer, int groupHandle) {
        LayerNode aLN = new LayerNode(aLayer);
        this.selectLayer(aLN);
        GroupNode aGroup = getGroupByHandle(groupHandle);

        if (aGroup == null) {
            return addLayerNode(aLN);
        } else {
            return addLayerNode(aLN, aGroup);
        }
    }

    /**
     * Insert polygon layer
     *
     * @param aLayer Polygon layer
     * @return Layer handle
     */
    public int insertPolygonLayer(MapLayer aLayer) {
        //_mapView.setLockViewUpdate(true);
        //int lIdx = _mapView.getPolygonLayerIdx() + 1;
        int lIdx = _mapView.getImageLayerIdx() + 1;
        int handle = addLayer(aLayer);
        if (lIdx < 0) {
            lIdx = 0;
        }
        moveLayer(handle, lIdx);
        _mapView.setLockViewUpdate(false);
        _mapView.paintLayers();

        return handle;
    }

    /**
     * Insert polyline layer
     *
     * @param aLayer Polyline layer
     * @return Layer handle
     */
    public int insertPolylineLayer(VectorLayer aLayer) {
        //_mapView.setLockViewUpdate(true);
        int lIdx = _mapView.getLineLayerIdx() + 1;
        int handle = addLayer(aLayer);
        if (lIdx < 0) {
            lIdx = 0;
        }
        moveLayer(handle, lIdx);
        _mapView.setLockViewUpdate(false);
        _mapView.paintLayers();

        return handle;
    }

    /**
     * Insert image layer
     *
     * @param aLayer Image layer
     * @return Layer handle
     */
    public int insertImageLayer(MapLayer aLayer) {
        //_mapView.setLockViewUpdate(true);
        int lIdx = _mapView.getImageLayerIdx() + 1;
        int handle = addLayer(aLayer);
        if (lIdx < 0) {
            lIdx = 0;
        }
        moveLayer(handle, lIdx);
        _mapView.setLockViewUpdate(false);
        _mapView.paintLayers();

        return handle;
    }

    /**
     * Move layer position
     *
     * @param handle Layer handle
     * @param lNewIdx Move to index
     */
    public void moveLayer(int handle, int lNewIdx) {
        int lPreIdx = _mapView.getLayerIdxFromHandle(handle);

        if (lPreIdx == lNewIdx) {
            return;
        }
        moveLayerNode(lPreIdx, lNewIdx);
        _mapView.moveLayer(lPreIdx, lNewIdx);

        _mapView.paintLayers();

        this.fireLayersUpdatedEvent();
    }

    /**
     * Move layer position
     *
     * @param layer The layer
     * @param lNewIdx Move to index
     */
    public void moveLayer(MapLayer layer, int lNewIdx) {
        moveLayer(layer.getHandle(), lNewIdx);
    }

    /**
     * Get all layer nodes
     *
     * @return Layer nodes
     */
    public List<LayerNode> getLayerNodes() {
        List<LayerNode> layerNodes = new ArrayList<>();
        for (ItemNode aIN : _nodes) {
            if (aIN.getNodeType() == NodeTypes.GroupNode) {
                for (LayerNode aLN : ((GroupNode) aIN).getLayers()) {
                    layerNodes.add(aLN);
                }
            } else {
                layerNodes.add((LayerNode) aIN);
            }
        }

        return layerNodes;
    }

    /**
     * Move layer node
     *
     * @param lPreIdx Previous index
     * @param lNewIdx New index
     */
    public void moveLayerNode(int lPreIdx, int lNewIdx) {
        List<LayerNode> layerNodes = getLayerNodes();
        LayerNode aTN = layerNodes.get(lPreIdx);

        _nodes.remove(aTN);
        LayerNode toLN = layerNodes.get(lNewIdx);
        if (toLN.getGroupHandle() >= 0) {
            GroupNode gNode = getGroupByHandle(toLN.getGroupHandle());
            gNode.insertLayer(aTN, gNode.getLayerIndex(toLN));
        } else {
            _nodes.add(_nodes.indexOf(toLN), aTN);
        }
    }

    /**
     * Add a layer node
     *
     * @param aLN The layer node
     * @return The layer handle
     */
    public int addLayerNode(LayerNode aLN) {
        aLN.setMapFrame(this);
        int handle = _mapView.addLayer(aLN.getMapLayer());

        //aLN.setMapLayer(_mapView.getLayerFromHandle(handle));
        addNode(aLN);
        if (aLN.getMapLayer().isVisible()) {
            aLN.setChecked(true);
        }
        aLN.updateLegendScheme(aLN.getLegendScheme());
        selectLayer(aLN);
        if (aLN.getMapLayer().isExpanded()) {
            aLN.expand();
        }

        this.fireLayersUpdatedEvent(new LayersUpdatedEvent(this));

        return handle;
    }

    /**
     * Add a layer node in a group node
     *
     * @param aLN a layer node
     * @param aGN The group node
     * @return Layer handle
     */
    public int addLayerNode(LayerNode aLN, GroupNode aGN) {
        aLN.setMapFrame(this);
        int handle = _mapView.addLayer(aLN.getMapLayer());

        aLN.setMapLayer(_mapView.getLayerByHandle(handle));
        aGN.addLayer(aLN);
        if (aLN.getMapLayer().isVisible()) {
            aLN.setChecked(true);
        }
        aLN.updateLegendScheme(aLN.getLegendScheme());
        selectLayer(aLN);
        if (aLN.getMapLayer().isExpanded()) {
            aLN.expand();
        }

        reOrderMapViewLayers();
        this.fireLayersUpdatedEvent();

        return handle;
    }

    private int addLayerNode(LayerNode aLN, boolean earthWind) {
        aLN.setMapFrame(this);
        int handle = _mapView.addWindLayer((VectorLayer) aLN.getMapLayer(), earthWind);

        addNode(aLN);
        if (aLN.getMapLayer().isVisible()) {
            aLN.setChecked(true);
        }
        aLN.updateLegendScheme(aLN.getLegendScheme());
        selectLayer(aLN);
        if (aLN.getMapLayer().isExpanded()) {
            aLN.expand();
        }

        this.fireLayersUpdatedEvent();

        return handle;
    }

    /**
     * Add layer node
     *
     * @param index The index
     * @param aLN A layer node
     * @return Layer node
     */
    public int addLayerNode(int index, LayerNode aLN) {
        aLN.setMapFrame(this);
        int handle = _mapView.addLayer(aLN.getMapLayer());

        aLN.setMapLayer(_mapView.getLayerByHandle(handle));
        addNode(index, aLN);
        if (aLN.getMapLayer().isVisible()) {
            aLN.setChecked(true);
        }
        aLN.updateLegendScheme(aLN.getLegendScheme());
        selectLayer(aLN);
        if (aLN.getMapLayer().isExpanded()) {
            aLN.expand();
        }

        reOrderMapViewLayers();
        this.fireLayersUpdatedEvent();

        return handle;
    }

    /**
     * Add a layer node in a group node
     *
     * @param index The index
     * @param aLN The layer node
     * @param aGN The group node
     * @return Layer handle
     */
    public int addLayerNode(int index, LayerNode aLN, GroupNode aGN) {
        aLN.setMapFrame(this);
        int handle = _mapView.addLayer(aLN.getMapLayer());

        aLN.setMapLayer(_mapView.getLayerByHandle(handle));
        aGN.addLayer(index, aLN);
        if (aLN.getMapLayer().isVisible()) {
            aLN.setChecked(true);
        }
        aLN.updateLegendScheme(aLN.getLegendScheme());
        selectLayer(aLN);
        if (aLN.getMapLayer().isExpanded()) {
            aLN.expand();
        }

        reOrderMapViewLayers();
        this.fireLayersUpdatedEvent();

        return handle;
    }

    /**
     * Add a group node
     *
     * @param aGN The group node
     */
    public void addGroupNode(GroupNode aGN) {
        addNode(aGN);
        for (LayerNode aLN : aGN.getLayers()) {
            _mapView.addLayer(aLN.getMapLayer());
        }

        reOrderMapViewLayers();
        this.fireLayersUpdatedEvent();
    }

    /**
     * Insert a group node
     *
     * @param index The index
     * @param aGN The group node
     */
    public void addGroupNode(int index, GroupNode aGN) {
        addNode(index, aGN);
        for (LayerNode aLN : aGN.getLayers()) {
            _mapView.addLayer(aLN.getMapLayer());
        }

        reOrderMapViewLayers();
        this.fireLayersUpdatedEvent();
    }

    /**
     * Add a node
     *
     * @param aNode The node
     */
    public void addNode(ItemNode aNode) {
        if (aNode.getNodeType() == NodeTypes.GroupNode) {
            ((GroupNode) aNode).setGroupHandle(getNewGroupHandle());
            ((GroupNode) aNode).setMapFrame(this);
        } else if (aNode.getNodeType() == NodeTypes.LayerNode) {
            ((LayerNode) aNode).setMapFrame(this);
        }

        _nodes.add(aNode);
    }

    /**
     * Insert a node
     *
     * @param idx The index
     * @param aNode The node
     */
    public void addNode(int idx, ItemNode aNode) {
        if (idx == -1) {
            return;
        }

        if (aNode.getNodeType() == NodeTypes.GroupNode) {
            ((GroupNode) aNode).setGroupHandle(getNewGroupHandle());
            ((GroupNode) aNode).setMapFrame(this);
        } else if (aNode.getNodeType() == NodeTypes.LayerNode) {
            ((LayerNode) aNode).setMapFrame(this);
        }

        _nodes.add(idx, aNode);
    }

    /**
     * Remove a node
     *
     * @param aNode The node
     */
    public void removeNode(ItemNode aNode) {
        _nodes.remove(aNode);
    }

    /**
     * Remove a layer by index
     *
     * @param lIdx The layer index
     */
    public void removeLayer(int lIdx) {
        MapLayer aLayer = _mapView.getLayers().get(lIdx);
        removeLayer(aLayer);
    }

    /**
     * Remove a layer by handle
     *
     * @param handle The layer handle
     */
    public void removeLayerByHandle(int handle) {
        int lIdx = _mapView.getLayerIdxFromHandle(handle);
        if (lIdx > -1) {
            LayerNode aLN = getLayerNodeByHandle(handle);
            if (aLN == null) {
                _mapView.removeLayer(lIdx);
                return;
            }

            if (aLN.getGroupHandle() >= 0) {
                GroupNode gNode = getGroupByHandle(aLN.getGroupHandle());
                gNode.removeLayer(aLN);
            } else {
                removeNode(aLN);
            }

            _mapView.removeLayer(lIdx);
            if (lIdx > 0) {
                int newHandle = _mapView.getLayerHandleFromIdx(lIdx - 1);
                selectLayerByHandle(newHandle);
            }

            _mapView.paintLayers();

            this.fireLayersUpdatedEvent();
        }
    }

    /**
     * Remove a layer
     *
     * @param aLayer The layer
     */
    public void removeLayer(MapLayer aLayer) {
        int handle = aLayer.getHandle();
        removeLayerByHandle(handle);
    }

    /**
     * Remove a layer node
     *
     * @param aLN The layer node
     */
    public void removeLayer(LayerNode aLN) {
        _mapView.removeLayerHandle(aLN.getLayerHandle());
        if (aLN.getGroupHandle() >= 0) {
            GroupNode gNode = getGroupByHandle(aLN.getGroupHandle());
            gNode.removeLayer(aLN);
        } else {
            removeNode(aLN);
        }

        _mapView.paintLayers();
        this.fireLayersUpdatedEvent();
    }

    /**
     * Remove meteorological data layers
     */
    public void removeMeteoLayers() {
        for (int i = 0; i < _mapView.getLayerNum(); i++) {
            if (i == _mapView.getLayerNum()) {
                break;
            }
            MapLayer aLayer = _mapView.getLayers().get(i);
            if (aLayer.getFileName().isEmpty()) {
                removeLayer(aLayer);
                i -= 1;
            }
        }
    }

    /**
     * Remove all layers
     */
    public void removeAllLayers() {
        int lNum = _mapView.getLayerNum();
        for (int i = 0; i < lNum; i++) {
            removeLayer(0);
        }
    }

    private int getNewGroupHandle() {
        int handle = 0;
        for (ItemNode aTN : _nodes) {
            if (aTN.getNodeType() == NodeTypes.GroupNode) {
                if (((GroupNode) aTN).getGroupHandle() > handle) {
                    handle = ((GroupNode) aTN).getGroupHandle();
                }
            }
        }
        handle += 1;

        return handle;
    }

    /**
     * Get group node by handle
     *
     * @param handle The hanle
     * @return group node
     */
    public GroupNode getGroupByHandle(int handle) {
        GroupNode aGroup = null;
        for (ItemNode aNode : _nodes) {
            if (aNode.getNodeType() == NodeTypes.GroupNode) {
                if (((GroupNode) aNode).getGroupHandle() == handle) {
                    aGroup = (GroupNode) aNode;
                    break;
                }
            }
        }

        return aGroup;
    }

    /**
     * Get group node by name
     *
     * @param name The node name
     * @return Group node
     */
    public GroupNode getGroupByName(String name) {
        GroupNode aGroup = null;
        for (ItemNode aTN : _nodes) {
            if (aTN.getNodeType() == NodeTypes.GroupNode) {
                if (((GroupNode) aTN).getText().equals(name)) {
                    aGroup = (GroupNode) aTN;
                    break;
                }
            }
        }

        return aGroup;
    }

    /**
     * Select layer by handle
     *
     * @param handle Layer handle
     */
    public void selectLayerByHandle(int handle) {
        LayerNode aLN = getLayerNodeByHandle(handle);

        selectLayer(aLN);
    }

    private void selectLayer(LayerNode aLN) {
//        if (_nodes.size() > 1) {
//            for (ItemNode aNode : _nodes) {
//                aNode.setBackColor(Color.white);
//                aNode.setForeColor(Color.black);
//                if (aNode.getNodeType() == NodeTypes.GroupNode) {
//                    for (LayerNode bNode : ((GroupNode) aNode).getLayers()) {
//                        bNode.setBackColor(Color.white);
//                        bNode.setForeColor(Color.black);
//                    }
//                }
//            }
//        }

        this.unSelectNodes();
        aLN.setSelected(true);
        this.setSelectedLayer(aLN.getLayerHandle());
        _mapView.setSelectedLayerHandle(this._selectedLayerHandle);
        if (_legend != null) {
            //_legend.selectNode(aLN);
            _legend.setSelectedNode(aLN);
        }
    }

    /**
     * Unselect all nodes
     */
    public void unSelectNodes() {
        for (ItemNode aNode : _nodes) {
            aNode.setSelected(false);
            if (aNode.getNodeType() == NodeTypes.GroupNode) {
                for (LayerNode lNode : ((GroupNode) aNode).getLayers()) {
                    lNode.setSelected(false);
                }
            }
        }
    }

    /**
     * Get layer node by handle
     *
     * @param handle The layer handle
     * @return The layer node
     */
    public LayerNode getLayerNodeByHandle(int handle) {
        LayerNode aLN = null;
        for (ItemNode aTN : _nodes) {
            if (aTN.getNodeType() == NodeTypes.LayerNode) {
                if (((LayerNode) aTN).getLayerHandle() == handle) {
                    aLN = (LayerNode) aTN;
                    break;
                }
            } else //Group node
            {
                boolean find = false;
                for (LayerNode bLN : ((GroupNode) aTN).getLayers()) {
                    if (bLN.getLayerHandle() == handle) {
                        aLN = bLN;
                        find = true;
                        break;
                    }
                }
                if (find) {
                    break;
                }
            }
        }

        return aLN;
    }

    /**
     * Get layer node by name
     *
     * @param lName The layer name
     * @return The layer node
     */
    public LayerNode getLayerNodeByName(String lName) {
        LayerNode aLN = null;
        for (ItemNode aTN : _nodes) {
            if (aTN.getNodeType() == NodeTypes.LayerNode) {
                if (((LayerNode) aTN).getText().equals(lName)) {
                    aLN = (LayerNode) aTN;
                    break;
                }
            } else //Group node
            {
                boolean find = false;
                for (LayerNode bLN : ((GroupNode) aTN).getLayers()) {
                    if (bLN.getText().equals(lName)) {
                        aLN = bLN;
                        find = true;
                        break;
                    }
                }
                if (find) {
                    break;
                }
            }
        }

        return aLN;
    }

    /**
     * Update layer node
     *
     * @param aLayer The layer
     */
    public void updateLayerNode(MapLayer aLayer) {
        updateLayerNode(aLayer.getHandle());
    }

    /**
     * Update layer node by handle
     *
     * @param handle The layer handle
     */
    public void updateLayerNode(int handle) {
        LayerNode aLN = getLayerNodeByHandle(handle);
        aLN.update();

        this.fireLayersUpdatedEvent(new LayersUpdatedEvent(this));
    }

    /**
     * Update layer node legend scheme
     *
     * @param handle The layer handle
     * @param aLS The legend scheme
     */
    public void UpdateLayerNodeLegendScheme(int handle, LegendScheme aLS) {
        LayerNode aLN = getLayerNodeByHandle(handle);
        aLN.updateLegendScheme(aLS);
    }

    /**
     * Reorder map view layers
     */
    public void reOrderMapViewLayers() {
        for (int i = _nodes.size() - 1; i >= 0; i--) {
            ItemNode aTN = _nodes.get(i);
            if (aTN.getNodeType() == NodeTypes.LayerNode) {
                for (int j = 0; j < _mapView.getLayerNum(); j++) {
                    if (_mapView.getLayers().get(j).getHandle() == ((LayerNode) aTN).getLayerHandle()) {
                        if (j > 0) {
                            _mapView.moveLayer(j, 0);
                        }
                        break;
                    }
                }
            } else {
                for (int l = ((GroupNode) aTN).getLayers().size() - 1; l >= 0; l--) {
                    LayerNode aLN = ((GroupNode) aTN).getLayers().get(l);
                    for (int j = 0; j < _mapView.getLayerNum(); j++) {
                        if (_mapView.getLayers().get(j).getHandle() == aLN.getLayerHandle()) {
                            if (j > 0) {
                                _mapView.moveLayer(j, 0);
                            }
                            break;
                        }
                    }
                }
            }
        }

        _mapView.paintLayers();
        this.fireMapViewUpdatedEvent();
    }

    @Override
    public int getExpandedHeight() {
        int height = this.getHeight();
        for (ItemNode aNode : _nodes) {
            height += aNode.getDrawHeight() + Constants.ITEM_PAD;
        }

        return height;
    }

    @Override
    public int getDrawHeight() {
        if (this.isExpanded()) {
            return getExpandedHeight();
        } else {
            return this.getHeight();
        }
    }

    // </editor-fold>
    // <editor-fold desc="XML import and export">
    /**
     * Export project XML content
     *
     * @param m_Doc XML document
     * @param parent parent XML element
     * @param projectFilePath Project file path
     */
    public void exportProjectXML(Document m_Doc, Element parent, String projectFilePath) {
        addMapFrameElement(m_Doc, parent, projectFilePath);
    }

    private void addMapFrameElement(Document m_Doc, Element parent, String projectFilePath) {
        Element mapFrame = m_Doc.createElement("MapFrame");
        Attr name = m_Doc.createAttribute("Name");
        Attr active = m_Doc.createAttribute("Active");
        Attr expanded = m_Doc.createAttribute("Expanded");
        Attr order = m_Doc.createAttribute("Order");
        Attr Left = m_Doc.createAttribute("Left");
        Attr Top = m_Doc.createAttribute("Top");
        Attr Width = m_Doc.createAttribute("Width");
        Attr Height = m_Doc.createAttribute("Height");
        Attr DrawMapNeatLine = m_Doc.createAttribute("DrawNeatLine");
        Attr MapNeatLineColor = m_Doc.createAttribute("NeatLineColor");
        Attr MapNeatLineSize = m_Doc.createAttribute("NeatLineSize");
        Attr GridLineColor = m_Doc.createAttribute("GridLineColor");
        Attr GridLineSize = m_Doc.createAttribute("GridLineSize");
        Attr GridLineStyle = m_Doc.createAttribute("GridLineStyle");
        Attr DrawGridLine = m_Doc.createAttribute("DrawGridLine");
        Attr DrawGridLabel = m_Doc.createAttribute("DrawGridLabel");
        Attr GridFontName = m_Doc.createAttribute("GridFontName");
        Attr GridFontSize = m_Doc.createAttribute("GridFontSize");
        Attr GridXDelt = m_Doc.createAttribute("GridXDelt");
        Attr GridYDelt = m_Doc.createAttribute("GridYDelt");
        Attr GridXOrigin = m_Doc.createAttribute("GridXOrigin");
        Attr GridYOrigin = m_Doc.createAttribute("GridYOrigin");

        name.setValue(this.getText());
        active.setValue(String.valueOf(this.isActive()));
        expanded.setValue(String.valueOf(this.isExpanded()));
        order.setValue(String.valueOf(_order));
        Left.setValue(String.valueOf(_layoutBounds.x));
        Top.setValue(String.valueOf(_layoutBounds.y));
        Width.setValue(String.valueOf(_layoutBounds.width));
        Height.setValue(String.valueOf(_layoutBounds.height));
        DrawMapNeatLine.setValue(String.valueOf(_drawNeatLine));
        MapNeatLineColor.setValue(ColorUtil.toHexEncoding(_neatLineColor));
        MapNeatLineSize.setValue(String.valueOf(_neatLineSize));
        GridLineColor.setValue(ColorUtil.toHexEncoding(this.getGridLineColor()));
        GridLineSize.setValue(String.valueOf(this.getGridLineSize()));
        GridLineStyle.setValue(this.getGridLineStyle().toString());
        DrawGridLine.setValue(String.valueOf(this.isDrawGridLine()));
        DrawGridLabel.setValue(String.valueOf(this.isDrawGridLabel()));
        GridFontName.setValue(this.getGridFont().getFontName());
        GridFontSize.setValue(String.valueOf(this.getGridFont().getSize()));
        GridXDelt.setValue(String.valueOf(this.getGridXDelt()));
        GridYDelt.setValue(String.valueOf(this.getGridYDelt()));
        GridXOrigin.setValue(String.valueOf(this.getGridXOrigin()));
        GridYOrigin.setValue(String.valueOf(this.getGridYOrigin()));

        mapFrame.setAttributeNode(name);
        mapFrame.setAttributeNode(active);
        mapFrame.setAttributeNode(expanded);
        mapFrame.setAttributeNode(order);
        mapFrame.setAttributeNode(Left);
        mapFrame.setAttributeNode(Top);
        mapFrame.setAttributeNode(Width);
        mapFrame.setAttributeNode(Height);
        mapFrame.setAttributeNode(DrawMapNeatLine);
        mapFrame.setAttributeNode(MapNeatLineColor);
        mapFrame.setAttributeNode(MapNeatLineSize);
        mapFrame.setAttributeNode(GridLineColor);
        mapFrame.setAttributeNode(GridLineSize);
        mapFrame.setAttributeNode(GridLineStyle);
        mapFrame.setAttributeNode(DrawGridLine);
        mapFrame.setAttributeNode(DrawGridLabel);
        mapFrame.setAttributeNode(GridFontName);
        mapFrame.setAttributeNode(GridFontSize);
        mapFrame.setAttributeNode(GridXDelt);
        mapFrame.setAttributeNode(GridYDelt);
        mapFrame.setAttributeNode(GridXOrigin);
        mapFrame.setAttributeNode(GridYOrigin);

        _mapView.exportExtentsElement(m_Doc, mapFrame);
        _mapView.exportMapPropElement(m_Doc, mapFrame);
        _mapView.exportGridLineElement(m_Doc, mapFrame);
        _mapView.exportMaskOutElement(m_Doc, mapFrame);
        _mapView.exportProjectionElement(m_Doc, mapFrame);
        addGroupLayerElement(m_Doc, mapFrame, projectFilePath);
        _mapView.exportGraphics(m_Doc, mapFrame, _mapView.getGraphicCollection().getGraphics());

        parent.appendChild(mapFrame);
    }

    private void addGroupLayerElement(Document m_Doc, Element parent, String projectFilePath) {
        Element GroupLayer = m_Doc.createElement("GroupLayer");
        for (int i = 0; i < _nodes.size(); i++) {
            ItemNode aTN = this.getNodes().get(i);
            if (aTN.getNodeType() == NodeTypes.LayerNode) {
                MapLayer aLayer = _mapView.getLayerByHandle(((LayerNode) aTN).getLayerHandle());
                addLayerElement(m_Doc, GroupLayer, aLayer, projectFilePath);
            } else {
                addGroupElement(m_Doc, GroupLayer, (GroupNode) aTN, projectFilePath);
            }
        }

        parent.appendChild(GroupLayer);
    }

    private void addGroupElement(Document m_Doc, Element parent, GroupNode aGN, String projectFilePath) {
        Element Group = m_Doc.createElement("Group");
        Attr GroupHandle = m_Doc.createAttribute("GroupHandle");
        Attr GroupName = m_Doc.createAttribute("GroupName");
        Attr Expanded = m_Doc.createAttribute("Expanded");

        GroupHandle.setValue(String.valueOf(aGN.getGroupHandle()));
        GroupName.setValue(aGN.getText());
        Expanded.setValue(String.valueOf(aGN.isExpanded()));

        Group.setAttributeNode(GroupHandle);
        Group.setAttributeNode(GroupName);
        Group.setAttributeNode(Expanded);

        for (LayerNode aLN : aGN.getLayers()) {
            MapLayer aLayer = _mapView.getLayerByHandle(aLN.getLayerHandle());
            addLayerElement(m_Doc, Group, aLayer, projectFilePath);
        }

        parent.appendChild(Group);
    }

    private void addLayerElement(Document m_Doc, Element parent, MapLayer aLayer, String projectFilePath) {
        File aFile = new File(aLayer.getFileName());
        if (aFile.isFile()) {
            switch (aLayer.getLayerType()) {
                case VectorLayer:
                    VectorLayer aVLayer = (VectorLayer) aLayer;
                    _mapView.exportVectorLayerElement(m_Doc, parent, aVLayer, projectFilePath);
                    break;
                case ImageLayer:
                    ImageLayer aILayer = (ImageLayer) aLayer;
                    _mapView.exportImageLayer(m_Doc, parent, aILayer, projectFilePath);
                    break;
                case RasterLayer:
                    RasterLayer aRLayer = (RasterLayer) aLayer;
                    _mapView.exportRasterLayer(m_Doc, parent, aRLayer, projectFilePath);
                    break;
            }
        } else {
            if (aLayer.getLayerType() == LayerTypes.WebMapLayer) {
                WebMapLayer wmLayer = (WebMapLayer) aLayer;
                _mapView.exportWebMapLayer(m_Doc, parent, wmLayer, projectFilePath);
            }
        }
    }

    /**
     * Import project XML content
     *
     * @param parent Parent XML element
     */
    public void importProjectXML(Element parent) {
        this.getNodes().clear();
        this.getMapView().removeAllLayers();

        try {
            this.setText(parent.getAttributes().getNamedItem("Name").getNodeValue());
            this.setActive(Boolean.parseBoolean(parent.getAttributes().getNamedItem("Active").getNodeValue()));

            boolean expanded = Boolean.parseBoolean(parent.getAttributes().getNamedItem("Expanded").getNodeValue());
            if (expanded) {
                this.expand();
            } else {
                this.collapse();
            }
        } catch (Exception e) {
        }

        try {
            _order = Integer.parseInt(parent.getAttributes().getNamedItem("Order").getNodeValue());
            int left = Integer.parseInt(parent.getAttributes().getNamedItem("Left").getNodeValue());
            int top = Integer.parseInt(parent.getAttributes().getNamedItem("Top").getNodeValue());
            int width = Integer.parseInt(parent.getAttributes().getNamedItem("Width").getNodeValue());
            int height = Integer.parseInt(parent.getAttributes().getNamedItem("Height").getNodeValue());
            _layoutBounds = new Rectangle(left, top, width, height);
            _drawNeatLine = Boolean.parseBoolean(parent.getAttributes().getNamedItem("DrawNeatLine").getNodeValue());
            _neatLineColor = ColorUtil.parseToColor(parent.getAttributes().getNamedItem("NeatLineColor").getNodeValue());
            _neatLineSize = Float.parseFloat(parent.getAttributes().getNamedItem("NeatLineSize").getNodeValue());
            this.setGridLineColor(ColorUtil.parseToColor(parent.getAttributes().getNamedItem("GridLineColor").getNodeValue()));
            this.setGridLineSize(Float.parseFloat(parent.getAttributes().getNamedItem("GridLineSize").getNodeValue()));
            this.setGridLineStyle(LineStyles.valueOf(parent.getAttributes().getNamedItem("GridLineStyle").getNodeValue()));
            this.setDrawGridLine(Boolean.parseBoolean(parent.getAttributes().getNamedItem("DrawGridLine").getNodeValue()));
            this.setDrawGridLabel(Boolean.parseBoolean(parent.getAttributes().getNamedItem("DrawGridLabel").getNodeValue()));
            String fontName = parent.getAttributes().getNamedItem("GridFontName").getNodeValue();
            float fontSize = Float.parseFloat(parent.getAttributes().getNamedItem("GridFontSize").getNodeValue());
            this.setGridFont(new Font(fontName, Font.PLAIN, (int) fontSize));
            this.setGridXDelt(Float.parseFloat(parent.getAttributes().getNamedItem("GridXDelt").getNodeValue()));
            this.setGridYDelt(Float.parseFloat(parent.getAttributes().getNamedItem("GridYDelt").getNodeValue()));
            this.setGridXOrigin(Float.parseFloat(parent.getAttributes().getNamedItem("GridXOrigin").getNodeValue()));
            this.setGridYOrigin(Float.parseFloat(parent.getAttributes().getNamedItem("GridYOrigin").getNodeValue()));
        } catch (Exception e) {
        }

        _mapView.setLockViewUpdate(true);
        _mapView.loadMapPropElement(parent);
        _mapView.loadGridLineElement(parent);
        _mapView.loadMaskOutElement(parent);
        _mapView.loadProjectionElement(parent);
        loadGroupLayer(parent);
        _mapView.loadGraphics(parent);
        _mapView.loadExtentsElement(parent);
        _mapView.setLockViewUpdate(false);
    }

    private void loadGroupLayer(Element parent) {
        Node theNode;
        if (parent.getElementsByTagName("GroupLayer").getLength() > 0) {
            theNode = parent.getElementsByTagName("GroupLayer").item(0);
        } else {
            theNode = parent.getElementsByTagName("Layers").item(0);
        }
        for (int i = 0; i < theNode.getChildNodes().getLength(); i++) {
            Node aGL = theNode.getChildNodes().item(i);
            if ("Group".equals(aGL.getNodeName())) {
                loadGroup(aGL);
            } else if ("Layer".equals(aGL.getNodeName())) {
                loadLayer(aGL, -1);
            }
        }
    }

    private void loadGroup(Node aGroup) {
        GroupNode aGN = new GroupNode(aGroup.getAttributes().getNamedItem("GroupName").getNodeValue());
        try {
            boolean expanded = Boolean.parseBoolean(aGroup.getAttributes().getNamedItem("Expanded").getNodeValue());
            if (expanded) {
                aGN.expand();
            } else {
                aGN.collapse();
            }
        } catch (Exception e) {
        } finally {
            addGroup(aGN);
            NodeList layerNodes = ((Element) aGroup).getElementsByTagName("Layer");
            for (int i = 0; i < layerNodes.getLength(); i++) {
                Node aLayerNode = layerNodes.item(i);
                loadLayer(aLayerNode, aGN.getGroupHandle());
            }
            aGN.updateCheckStatus();
        }
    }

    private void loadLayer(Node aLayer, int groupHnd) {
        try {
            LayerTypes aLayerType = LayerTypes.valueOf(aLayer.getAttributes().getNamedItem("LayerType").getNodeValue());

            switch (aLayerType) {
                case VectorLayer:
                    VectorLayer aVLayer = _mapView.loadVectorLayer(aLayer);
                    if (aVLayer != null) {
                        addLayer(aVLayer, groupHnd);
                    }
                    break;
                case ImageLayer:
                    ImageLayer aILayer = _mapView.loadImageLayer(aLayer);
                    if (aILayer != null) {
                        addLayer(aILayer, groupHnd);
                    }
                    break;
                case RasterLayer:
                    RasterLayer aRLayer = _mapView.loadRasterLayer(aLayer);
                    if (aRLayer != null) {
                        addLayer(aRLayer, groupHnd);
                    }
                    break;
                case WebMapLayer:
                    WebMapLayer wmLayer = _mapView.loadWebMapLayer(aLayer);
                    if (wmLayer != null) {
                        addLayer(wmLayer, groupHnd);
                    }
            }
        } catch (Exception ex) {
            Logger.getLogger(MapFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // </editor-fold>
    // </editor-fold>
    // <editor-fold desc="BeanInfo">

    public class MapFrameBean {

        public MapFrameBean() {

        }

        public String getText() {
            return MapFrame.this.getText();
        }

        public void setText(String value) {
            MapFrame.this.setText(value);
            MapFrame.this.getLegend().paintGraphics();
        }
    }

    public static class MapFrameBeanBeanInfo extends BaseBeanInfo {

        public MapFrameBeanBeanInfo() {
            super(MapFrameBean.class);
            addProperty("text").setCategory("General").setDisplayName("Text");
        }
    }
    // </editor-fold>
}
