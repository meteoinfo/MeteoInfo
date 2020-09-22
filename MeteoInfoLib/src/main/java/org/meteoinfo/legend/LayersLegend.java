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

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.event.ActiveMapFrameChangedEvent;
import org.meteoinfo.global.event.IActiveMapFrameChangedListener;
import org.meteoinfo.global.event.ILayersUpdatedListener;
import org.meteoinfo.global.event.IMapFramesUpdatedListener;
import org.meteoinfo.global.event.LayersUpdatedEvent;
import org.meteoinfo.global.event.MapFramesUpdatedEvent;
import org.meteoinfo.global.GenericFileFilter;
import org.meteoinfo.global.PointF;
import org.meteoinfo.layer.FrmLayerProperty;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.layout.MapLayout;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.undo.UndoableEdit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.meteoinfo.data.mapdata.FrmAttriData;
import org.meteoinfo.data.mapdata.webmap.WebMapProvider;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.FrmProperty;
import org.meteoinfo.global.event.INodeSelectedListener;
import org.meteoinfo.global.event.NodeSelectedEvent;
import org.meteoinfo.layer.FrmLabelSet;
import org.meteoinfo.layer.WebMapLayer;
import org.meteoinfo.map.MapView;
import org.meteoinfo.map.MapViewUndoRedo;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.ProjectionNames;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author yaqiang
 */
public class LayersLegend extends JPanel {

    class MousePos {

        public boolean inItem = false;
        public int curTop = 0;
        public boolean inCheckBox = false;
        public boolean inExpansionBox = false;
    }
    // <editor-fold desc="Variables">
    private EventListenerList _listeners = new EventListenerList();
    private BufferedImage _paintImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    public FrmLayerProperty frmLayerProp;
    private JScrollBar _vScrollBar;
    private MapLayout _mapLayout;
    private ItemNode _selectedNode;
    private Color _selectedBackColor = new Color(75,110,175);
    private Color _selectedForeColor = Color.white;
    private Point _mouseDownPos = new Point(0, 0);
    private ItemNode _dragNode = null;
    private MapFrame _currentMapFrame;
    private List<MapFrame> _mapFrames = new ArrayList<>();
    //private boolean _isLayoutView = false;
    private boolean _dragMode = false;
    private int _dragPosY;
    private LookAndFeel laf;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    public LayersLegend() {
        super();

        laf = UIManager.getLookAndFeel();
        this.setBackground(laf.getDefaults().getColor("List.background"));
        this._selectedBackColor = laf.getDefaults().getColor("List.selectionBackground");
        this._selectedForeColor = laf.getDefaults().getColor("List.selectionForeground");

        this.setLayout(new BorderLayout());
        this.setDoubleBuffered(true);
        initComponents();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    onMouseReleased(e);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(LayersLegend.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }
        });

        //this.setBackground(Color.white);
        //this.setFont(new Font("宋体", Font.PLAIN, 12));
        MapFrame mf = new MapFrame();
        mf.setActive(true);
        this.addMapFrame(mf);

        _mapLayout = null;
        frmLayerProp = null;
        //this.setBackground(Color.white);
        //this.setForeground(Color.black);
    }

    private void initComponents() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onComponentResized(e);
            }
        });

        _vScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        _vScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                onScrollValueChanged(e);
            }
        });
        this.add(_vScrollBar, BorderLayout.EAST);
        //this.vScrollBar.setPreferredSize(new Dimension(this.vScrollBar.getWidth(), this.getHeight()));
        this._vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);
        this._vScrollBar.setSize(this._vScrollBar.getWidth(), this.getHeight());
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Set font
     *
     * @param font Font
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);

    }

    /**
     * Get selected node
     *
     * @return The selected node
     */
    public ItemNode getSelectedNode() {
        return _selectedNode;
    }

    /**
     * Set selected node
     *
     * @param aNode Selected node
     */
    public void setSelectedNode(ItemNode aNode) {
        _selectedNode = aNode;
        this.fireNodeSelectedEvent();
    }

    /**
     * Get current map frame
     *
     * @return Current map frame
     */
    public MapFrame getCurrentMapFrame() {
        return getMapFrame(_selectedNode);
    }

    /**
     * Get active map frame
     *
     * @return Active map frame
     */
    public MapFrame getActiveMapFrame() {
        for (MapFrame mf : _mapFrames) {
            if (mf.isActive()) {
                return mf;
            }
        }

        return null;
    }

    /**
     * Get map frame list
     *
     * @return Map frame list
     */
    public List<MapFrame> getMapFrames() {
        return _mapFrames;
    }

    /**
     * Set map frame list
     *
     * @param mfs The map frame list
     */
    public void setMapFrames(List<MapFrame> mfs) {
        _mapFrames = mfs;
    }

    /**
     * Get map layout
     *
     * @return The map layout
     */
    public MapLayout getMapLayout() {
        return _mapLayout;
    }

    /**
     * Set map layout
     *
     * @param ml The map layout
     */
    public void setMapLayout(MapLayout ml) {
        _mapLayout = ml;
        _mapLayout.updateMapFrames(_mapFrames);
        _mapLayout.addActiveMapFrameChangedListener(new IActiveMapFrameChangedListener() {
            @Override
            public void activeMapFrameChangedEvent(ActiveMapFrameChangedEvent event) {
                setActiveMapFrame(_mapLayout.getActiveMapFrame());
            }
        });
        _mapLayout.addMapFramesUpdatedListener(new IMapFramesUpdatedListener() {
            @Override
            public void mapFramesUpdatedEvent(MapFramesUpdatedEvent event) {
                if (_mapLayout != null) {
                    _mapFrames = _mapLayout.getMapFrames();
                    paintGraphics();
                }
            }
        });
    }

//    /**
//     * Get if is layout view
//     *
//     * @return Boolean
//     */
//    public boolean isLayoutView() {
//        return _isLayoutView;
//    }
//
//    /**
//     * Set if is layout view
//     *
//     * @param istrue Boolean
//     */
//    public void setIsLayoutView(boolean istrue) {
//        _isLayoutView = istrue;
//        if (_isLayoutView) {
//            if (_mapLayout != null) {
//                for (MapFrame aMF : _mapFrames) {
//                    aMF.setIsFireMapViewUpdate(true);
//                }
//
//                if (_mapLayout.hasLegendElement()) {
//                    _mapLayout.getActiveLayoutMap().fireMapViewUpdatedEvent();
//                }
//            }
//        } else {
//            if (_mapLayout != null) {
//                for (MapFrame aMF : _mapFrames) {
//                    aMF.setIsFireMapViewUpdate(false);
//                }
//            }
//        }
//    }
    // </editor-fold>
    // <editor-fold desc="Events">
    public void addMapFramesUpdatedListener(IMapFramesUpdatedListener listener) {
        this._listeners.add(IMapFramesUpdatedListener.class, listener);
    }

    public void removeMapFramesUpdatedListener(IMapFramesUpdatedListener listener) {
        this._listeners.remove(IMapFramesUpdatedListener.class, listener);
    }

    public void fireMapFramesUpdatedEvent() {
        fireMapFramesUpdatedEvent(new MapFramesUpdatedEvent(this));
    }

    private void fireMapFramesUpdatedEvent(MapFramesUpdatedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IMapFramesUpdatedListener.class) {
                ((IMapFramesUpdatedListener) listeners[i + 1]).mapFramesUpdatedEvent(event);
            }
        }
    }

    public void addNodeSelectedListener(INodeSelectedListener listener) {
        this._listeners.add(INodeSelectedListener.class, listener);
    }

    public void removeNodeSelectedListener(INodeSelectedListener listener) {
        this._listeners.remove(INodeSelectedListener.class, listener);
    }

    public void fireNodeSelectedEvent() {
        fireNodeSelectedEvent(new NodeSelectedEvent(this));
    }

    private void fireNodeSelectedEvent(NodeSelectedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == INodeSelectedListener.class) {
                ((INodeSelectedListener) listeners[i + 1]).nodeSelectedEvent(event);
            }
        }
    }

    public void addActiveMapFrameChangedListener(IActiveMapFrameChangedListener listener) {
        this._listeners.add(IActiveMapFrameChangedListener.class, listener);
    }

    public void removeActiveMapFrameChangedListener(IActiveMapFrameChangedListener listener) {
        this._listeners.remove(IActiveMapFrameChangedListener.class, listener);
    }

    public void fireActiveMapFrameChangedEvent() {
        fireActiveMapFrameChangedEvent(new ActiveMapFrameChangedEvent(this));
    }

    private void fireActiveMapFrameChangedEvent(ActiveMapFrameChangedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IActiveMapFrameChangedListener.class) {
                ((IActiveMapFrameChangedListener) listeners[i + 1]).activeMapFrameChangedEvent(event);
            }
        }
        this.paintGraphics();
    }

    public void onScrollValueChanged(AdjustmentEvent e) {
        _vScrollBar.setValue(e.getValue());
        this.paintGraphics();
    }

    public void onComponentResized(ComponentEvent e) {
        this._vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);
        this._vScrollBar.setSize(this._vScrollBar.getWidth(), this.getHeight());
        this.paintGraphics();
    }

    public void onMouseClicked(MouseEvent e) {
        int clickTimes = e.getClickCount();
        if (clickTimes == 1) {
            MousePos mPos = new MousePos();
            ItemNode aNode = getNodeByPosition(e.getX(), e.getY(), mPos);
            if (aNode == null) {
                return;
            }

            selectNode(aNode);
            switch (aNode.getNodeType()) {
                case GroupNode:
                    onGroupMouseClicked(e);
                    break;
                case LayerNode:
                    onLayerMouseClicked(e);
                    break;
                case MapFrameNode:
                    onMapFrameMouseClicked(e);
                    break;
            }

            this.paintGraphics();
        } else if (clickTimes == 2) {
            MousePos mPos = new MousePos();
            ItemNode aNode = getNodeByPosition(e.getX(), e.getY(), mPos);
            if (aNode != null) {
                switch (aNode.getNodeType()) {
                    case LayerNode:
                        this.onPropertiesClick(null);
//                    LayerNode aLN = (LayerNode) aNode;
//                    MapLayer aLayer = aLN.getMapLayer();
//                    if (aLayer.getLayerType() == LayerTypes.WebMapLayer) {
//                        return;
//                    }
//                    
//                    if (frmLayerProp == null) {
//                        //frmLayerProp = new frmLayerProperty(aLayerObj, aLN.getMapFrame());
//                        frmLayerProp = new FrmLayerProperty((JFrame) SwingUtilities.getWindowAncestor(this), false);
//                        frmLayerProp.setMapLayer(aLayer);
//                        frmLayerProp.setMapFrame(this.getActiveMapFrame());
//                        //frmLayerProp.Legend = this;                        
//                        frmLayerProp.setLocationRelativeTo(this);
//                        frmLayerProp.setVisible(true);
//                        //frmLayerProp.setAlwaysOnTop(true);
//                    } else {
//                        frmLayerProp.setMapLayer(aLayer);
//                        frmLayerProp.setMapFrame(this.getActiveMapFrame());
//                        //frmLayerProp.Legend = this;
//                        frmLayerProp.setVisible(true);
//                    }
                        break;
                    case GroupNode:
                        FrmProperty pFrm = new FrmProperty((JFrame) SwingUtilities.getWindowAncestor(this), true, false);
                        pFrm.setObject(((GroupNode) aNode).new GroupNodeBean());
                        pFrm.setLocationRelativeTo(this);
                        pFrm.setVisible(true);
                        break;
                    case MapFrameNode:
                        pFrm = new FrmProperty((JFrame) SwingUtilities.getWindowAncestor(this), true, false);
                        pFrm.setObject(((MapFrame) aNode).new MapFrameBean());
                        pFrm.setLocationRelativeTo(this);
                        pFrm.setVisible(true);
                        break;
                }
            }
        }
    }

    public void onMousePressed(MouseEvent e) {
        _mouseDownPos.x = e.getX();
        _mouseDownPos.y = e.getY();

        MousePos mPos = new MousePos();
        mPos.curTop = 0;
        mPos.inItem = false;
        _dragNode = getNodeByPosition(e.getX(), e.getY(), mPos);
    }

    public void onMouseReleased(MouseEvent e) throws CloneNotSupportedException {
        _dragMode = false;
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (_dragNode != null) {
                MousePos mPos = new MousePos();
                mPos.inItem = false;
                mPos.curTop = 0;
                ItemNode aNode = getNodeByPosition(e.getX(), e.getY(), mPos);

                if (aNode != null && aNode != _dragNode) {
                    MapFrame fromMF = getMapFrame(_dragNode);
                    MapFrame mapFrame = getMapFrame(aNode);
                    if (fromMF == mapFrame) {
                        if (_dragNode.getNodeType() == NodeTypes.GroupNode) {
                            //Remove drag node
                            ((GroupNode) _dragNode).getMapFrame().removeNode(_dragNode);

                            //Add the node to new position
                            if (aNode.getNodeType() == NodeTypes.MapFrameNode) {
                                mapFrame.addNode(_dragNode);
                            } else {
                                int idx = mapFrame.getNodes().indexOf(aNode);
                                if (idx < 0) {
                                    idx = 0;
                                }
                                mapFrame.addNode(idx, _dragNode);
                            }
                        } else if (_dragNode.getNodeType() == NodeTypes.LayerNode) {
                            //Remove drag node
                            if (((LayerNode) _dragNode).getGroupHandle() >= 0) {
                                GroupNode sGroup = mapFrame.getGroupByHandle(((LayerNode) _dragNode).getGroupHandle());
                                sGroup.removeLayer((LayerNode) _dragNode);
                            } else {
                                mapFrame.removeNode(_dragNode);
                            }

                            //Add to new position
                            switch (aNode.getNodeType()) {
                                case MapFrameNode:
                                    mapFrame.addNode(_dragNode);
                                    break;
                                case GroupNode:
                                    ((GroupNode) aNode).addLayer((LayerNode) _dragNode);
                                    break;
                                case LayerNode:
                                    if (((LayerNode) aNode).getGroupHandle() >= 0) {
                                        GroupNode dGroup = mapFrame.getGroupByHandle(((LayerNode) aNode).getGroupHandle());
                                        dGroup.addLayer(dGroup.getLayerIndex((LayerNode) aNode), (LayerNode) _dragNode);
                                    } else {
                                        int idx = mapFrame.getNodes().indexOf(aNode);
                                        if (idx < 0) {
                                            idx = 0;
                                        }
                                        mapFrame.addNode(idx, _dragNode);
                                    }
                                    break;
                            }
                        }

                        mapFrame.reOrderMapViewLayers();
                    } else if (_dragNode.getNodeType() == NodeTypes.GroupNode) {
                        //Add the node to new position
                        GroupNode newGN = new GroupNode(_dragNode.getText());
                        for (LayerNode aLN : ((GroupNode) _dragNode).getLayers()) {
                            LayerNode bLN = (LayerNode) aLN.clone();
                            //if (!fromMF.MapView.Projection.IsLonLatMap)
                            //    bLN.MapLayer = (MapLayer)fromMF.MapView.GetGeoLayerFromHandle(bLN.LayerHandle).Clone();
                            newGN.addLayer(bLN);
                        }
                        if (aNode.getNodeType() == NodeTypes.MapFrameNode) {
                            mapFrame.addGroupNode(newGN);
                        } else {
                            int idx = mapFrame.getNodes().indexOf(aNode);
                            mapFrame.addGroupNode(idx, newGN);
                        }
                    } else if (_dragNode.getNodeType() == NodeTypes.LayerNode) {
                        //Add to new position
                        LayerNode aLN = (LayerNode) ((LayerNode) (_dragNode)).clone();
                        switch (aNode.getNodeType()) {
                            case MapFrameNode:
                                mapFrame.addLayerNode(aLN);
                                break;
                            case GroupNode:
                                mapFrame.addLayerNode(aLN, (GroupNode) aNode);
                                break;
                            case LayerNode:
                                if (((LayerNode) aNode).getGroupHandle() >= 0) {
                                    GroupNode dGroup = mapFrame.getGroupByHandle(((LayerNode) aNode).getGroupHandle());
                                    //dGroup.InsertLayer((LayerNode)_dragNode, dGroup.GetLayerIndex((LayerNode)aNode));
                                    mapFrame.addLayerNode(dGroup.getLayerIndex((LayerNode) aNode), aLN, dGroup);
                                } else {
                                    int idx = mapFrame.getNodes().indexOf(aNode);
                                    mapFrame.addLayerNode(idx, aLN);
                                }
                                break;
                        }
                    }
                }
                this.paintGraphics();
            }
        }
    }

    public void onMouseDragged(MouseEvent e) {
        _dragMode = true;
        if (_dragNode != null) {
            MousePos mPos = new MousePos();
            mPos.curTop = 0;
            mPos.inItem = false;
            ItemNode aNode = getNodeByPosition(e.getX(), e.getY(), mPos);
            if (aNode != null && aNode != _dragNode) {
                if (_dragNode.getNodeType() == NodeTypes.GroupNode && aNode.getNodeType() == NodeTypes.LayerNode) {
                    if (((LayerNode) aNode).getGroupHandle() != -1) {
                        return;
                    }
                }

                _dragPosY = mPos.curTop + aNode.getHeight();
                if (aNode.getNodeType() == NodeTypes.LayerNode) {
                    _dragPosY = mPos.curTop + aNode.getDrawHeight();
                }
            }
            this.repaint();
        }
    }

    private void onGroupMouseClicked(MouseEvent e) {
        //if (GroupMouseClick != null) GroupMouseClick(this, e);

        MousePos mPos = new MousePos();
        GroupNode aNode = (GroupNode) getNodeByPositionEx(e.getX(), e.getY(), mPos);
        if (mPos.inExpansionBox) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (aNode.isExpanded()) {
                    aNode.collapse();
                } else {
                    aNode.expand();
                }

                this.paintGraphics();
            }
        } else if (mPos.inCheckBox) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                switch (aNode.getCheckStatus()) {
                    case 0:
                    case 2:
                        aNode.setCheckStatus(1);
                        aNode.setChecked(true);
                        break;
                    default:
                        aNode.setCheckStatus(0);
                        aNode.setChecked(false);
                        break;
                }

                for (LayerNode aLN : aNode.getLayers()) {
                    aLN.setChecked(aNode.isChecked());
                    MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
                    aLayer.setVisible(aNode.isChecked());
                }

                this.paintGraphics();
                aNode.getMapFrame().fireLayersUpdatedEvent();
                aNode.getMapFrame().getMapView().paintLayers();
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            _currentMapFrame = getMapFrame(aNode);
            JPopupMenu mnuGroup = new JPopupMenu();
            JMenuItem newGroupMI = new JMenuItem("New Group");
            newGroupMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onAddGroupClick(e);
                }
            });
            mnuGroup.add(newGroupMI);
            JMenuItem removeGroupMI = new JMenuItem("Remove Group");
            removeGroupMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onRemoveGroupClick(e);
                }
            });
            mnuGroup.add(removeGroupMI);
            mnuGroup.show(this, e.getX(), e.getY());
        }
    }

    private void onLayerMouseClicked(MouseEvent e) {
        //if (LayerMouseClick != null) LayerMouseClick(this, e);

        MousePos mPos = new MousePos();
        ItemNode aNode = getNodeByPositionEx(e.getX(), e.getY(), mPos);
        final LayerNode aLN = (LayerNode) aNode;
        MapLayer aLayerObj = aLN.getMapLayer();
        if (mPos.inExpansionBox) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (aNode.isExpanded()) {
                    aNode.collapse();
                } else {
                    aNode.expand();
                }

                aLayerObj.setExpanded(aNode.isExpanded());
                //this.repaint();

                aLN.getMapFrame().fireLayersUpdatedEvent();
            }
        } else if (mPos.inCheckBox) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                aNode.setChecked(!aNode.isChecked());
                aLayerObj.setVisible(aNode.isChecked());
                //this.repaint();

                aLN.getMapFrame().fireLayersUpdatedEvent();
                aLN.getMapFrame().getMapView().paintLayers();
                //aLN.getMapFrame().fireMapViewUpdatedEvent();
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu mnuLayer = new JPopupMenu();

            //Remove/save layer
            JMenuItem removeLayerMI = new JMenuItem("Remove Layer");
            removeLayerMI.setIcon(new FlatSVGIcon("org/meteoinfo/icons/delete.svg"));
            removeLayerMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onRemoveLayerClick(e);
                }
            });
            mnuLayer.add(removeLayerMI);
            switch (aLayerObj.getLayerType()) {
                case VectorLayer:
                case RasterLayer:
                    if (!new File(aLayerObj.getFileName()).exists()) {
                        JMenuItem saveLayerMI = new JMenuItem("Save Layer");
                        saveLayerMI.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                onSaveLayerClick(e);
                            }
                        });
                        mnuLayer.add(saveLayerMI);
                    }
                    break;
            }
            mnuLayer.addSeparator();

            //Attribute table
            if (aLayerObj.getLayerType() == LayerTypes.VectorLayer) {
                JMenuItem attrTableMI = new JMenuItem("Attribute Table");
                attrTableMI.setIcon(new FlatSVGIcon("org/meteoinfo/icons/table.svg"));
                attrTableMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onAttrTableClick(e);
                    }
                });
                mnuLayer.add(attrTableMI);
                mnuLayer.addSeparator();
            }

            //Zoom to layer and Visible scale
            JMenuItem zoomToLayerMI = new JMenuItem("Zoom To Layer");
            zoomToLayerMI.setIcon(new FlatSVGIcon("org/meteoinfo/icons/find.svg"));
            zoomToLayerMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onZoomToLayerClick(e);
                }
            });
            mnuLayer.add(zoomToLayerMI);
            JMenu visScaleMenu = new JMenu("Visible Scale");
            JMenuItem minVisScaleMI = new JMenuItem("Set Minimum Scale");
            minVisScaleMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onMinVisScaleClick(e);
                }
            });
            visScaleMenu.add(minVisScaleMI);
            JMenuItem maxVisScaleMI = new JMenuItem("Set Maximum Scale");
            maxVisScaleMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onMaxVisScaleClick(e);
                }
            });
            visScaleMenu.add(maxVisScaleMI);
            JMenuItem removeVisScaleMI = new JMenuItem("Remove Visible Scale");
            removeVisScaleMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onRemoveVisScaleClick(e);
                }
            });
            if (aLayerObj.getVisibleScale().isVisibleScaleEnabled()) {
                removeVisScaleMI.setEnabled(true);
            } else {
                removeVisScaleMI.setEnabled(false);
            }
            visScaleMenu.add(removeVisScaleMI);
            mnuLayer.add(visScaleMenu);
            mnuLayer.addSeparator();

            //Label
            if (aLayerObj.getLayerType() == LayerTypes.VectorLayer) {
                JMenuItem labelMI = new JMenuItem("Label");
                labelMI.setIcon(new FlatSVGIcon("org/meteoinfo/icons/label.svg"));
                labelMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onLabelClick(e);
                    }
                });
                mnuLayer.add(labelMI);
                mnuLayer.addSeparator();

//                    //Edit and export features
//                    String editMIName = "Switch Edit Status";
//                    JMenuItem editMI = new JMenuItem(editMIName);
//                    editMI.addActionListener(new ActionListener() {
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            onEditClick(aLN);
//                        }
//                    });
//                    mnuLayer.add(editMI);
//                    mnuLayer.addSeparator();
            }

            //Properties
            JMenuItem propMI = new JMenuItem("Properties");
            propMI.setIcon(new FlatSVGIcon("org/meteoinfo/icons/properties.svg"));
            propMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onPropertiesClick(e);
                }
            });
            mnuLayer.add(propMI);

            mnuLayer.show(this, e.getX(), e.getY());
        }
    }

    private void onMapFrameMouseClicked(MouseEvent e) {
        //if (MapFrameMouseClick != null) MapFrameMouseClick(this, e);

        MousePos mPos = new MousePos();
        MapFrame aNode = (MapFrame) getNodeByPositionEx(e.getX(), e.getY(), mPos);
        _currentMapFrame = getMapFrame(aNode);
        if (mPos.inExpansionBox) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (aNode.isExpanded()) {
                    aNode.collapse();
                } else {
                    aNode.expand();
                }

                this.paintGraphics();
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu mnuMapFrame = new JPopupMenu();
//                JMenuItem newMapFrameMI = new JMenuItem("New Map Frame");
//                newMapFrameMI.addActionListener(new ActionListener(){
//
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        throw new UnsupportedOperationException("Not supported yet.");
//                    }
//                    
//                });
//                mnuMapFrame.add(newMapFrameMI);
            JMenuItem newGroupMI = new JMenuItem("New Group");
            newGroupMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onAddGroupClick(e);
                }
            });
            mnuMapFrame.add(newGroupMI);
            JMenuItem addLayerMI = new JMenuItem("Add Layer");
            addLayerMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onAddLayerClick(e);
                }
            });
            mnuMapFrame.add(addLayerMI);
            JMenuItem addWebLayerMI = new JMenuItem("Add Web Layer");
            addWebLayerMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onAddWebLayerClick(e);
                }
            });
            mnuMapFrame.add(addWebLayerMI);
            mnuMapFrame.add(new JSeparator());
            JMenuItem activeMI = new JMenuItem("Active");
            activeMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onMapFrameActiveClick(e);
                }
            });
            mnuMapFrame.add(activeMI);
            if (_mapFrames.size() > 1) {
                mnuMapFrame.add(new JSeparator());
                JMenuItem removeMapFrameMI = new JMenuItem("Remove Map Frame");
                removeMapFrameMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onRemoveMapFrameClick(e);
                    }
                });
                mnuMapFrame.add(removeMapFrameMI);
            }
            mnuMapFrame.show(this, e.getX(), e.getY());
        }
    }

    private void onAddLayerClick(ActionEvent e) {
        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(new File("D:\\GeoData\\WORLD"));
        String[] fileExts = {"shp", "wmp"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Supported Formats");
        aDlg.setFileFilter(mapFileFilter);
        fileExts = new String[]{"shp"};
        mapFileFilter = new GenericFileFilter(fileExts, "Shape File (*.shp)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            try {
                //MapLayer aLayer = _currentMapFrame.openLayer(aFile);
                MapLayer aLayer = MapDataManage.loadLayer(aFile.getAbsolutePath());
                _currentMapFrame.addLayer(aLayer);
            } catch (IOException ex) {
                Logger.getLogger(LayersLegend.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(LayersLegend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void onAddWebLayerClick(ActionEvent e) {
        WebMapLayer layer = new WebMapLayer();
        layer.setWebMapProvider(WebMapProvider.OpenStreetMap);
        //layer.setDefaultProvider(DefaultProviders.OpenStreetMapQuestSattelite);
        //layer.setDefaultProvider(DefaultProviders.ArcGISImage);
        ProjectionInfo proj = this._currentMapFrame.getMapView().getProjection().getProjInfo();
        if (proj.getProjectionName() != ProjectionNames.Mercator) {
            if (JOptionPane.showConfirmDialog(null, "Not mercator projection! If project?", "Conform", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                ProjectionInfo toProj = ProjectionInfo.factory("+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0");
                this._currentMapFrame.getMapView().projectLayers(toProj);
            }
        }

        _currentMapFrame.addLayer(0, layer);
    }

    private void onAddGroupClick(ActionEvent e) {
        _currentMapFrame.addNewGroup("New Group");
        this.paintGraphics();
    }

    private void onRemoveGroupClick(ActionEvent e) {
        if (_selectedNode.getNodeType() == NodeTypes.GroupNode) {
            GroupNode aGN = (GroupNode) _selectedNode;
            aGN.getMapFrame().removeGroup(aGN);
        }
        this.paintGraphics();
    }

    private void onRemoveLayerClick(ActionEvent e) {
        if (_selectedNode.getNodeType() == NodeTypes.LayerNode) {
            LayerNode aLN = (LayerNode) _selectedNode;
            aLN.getMapFrame().removeLayer(aLN);
        }

        this.paintGraphics();
    }

    private void onSaveLayerClick(ActionEvent e) {
        if (_selectedNode.getNodeType() == NodeTypes.LayerNode) {
            LayerNode aLN = (LayerNode) _selectedNode;
            MapLayer aLayer = aLN.getMapLayer();
            if (aLN.getMapFrame().getMapView().getProjection().getProjInfo().equals(aLayer.getProjInfo())) {
                aLayer.saveFile();
            } else {
                try {
                    MapLayer bLayer = (MapLayer) aLayer.clone();
                    bLayer.setProjInfo(aLN.getMapFrame().getMapView().getProjection().getProjInfo());
                    bLayer.saveFile();
                    aLayer.setFileName(bLayer.getFileName());
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(LayersLegend.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void onAttrTableClick(ActionEvent e) {
        LayerNode aLN = (LayerNode) _selectedNode;
        MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
        if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            FrmAttriData frm = new FrmAttriData();
            frm.setLayer((VectorLayer) aLayer);
            frm.setLocationRelativeTo(frame);
            frm.setVisible(true);
        }
    }

    private void onEditClick(LayerNode aLN) {
        aLN.setEditing(!aLN.isEditing());
        this.paintGraphics();
    }

    private void onPropertiesClick(ActionEvent e) {
        LayerNode aLN = (LayerNode) _selectedNode;
        MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
//        if (aLayer.getLayerType() == LayerTypes.WebMapLayer)
//            return;

        if (frmLayerProp == null) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frmLayerProp = new FrmLayerProperty(frame, false);
            frmLayerProp.setMapLayer(aLayer);
            frmLayerProp.setMapFrame(this.getCurrentMapFrame());
            //frmLayerProp.Legend = this;                        
            frmLayerProp.setLocationRelativeTo(frame);
            frmLayerProp.setVisible(true);
            //frmLayerProp.setAlwaysOnTop(true);
        } else {
            frmLayerProp.setMapLayer(aLayer);
            frmLayerProp.setMapFrame(this.getCurrentMapFrame());
            //frmLayerProp.Legend = this;
            frmLayerProp.setVisible(true);
        }
    }

    private void onZoomToLayerClick(ActionEvent e) {
        LayerNode aLN = (LayerNode) _selectedNode;
        MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
        MapView mapView = aLN.getMapFrame().getMapView();
        Extent oldExtent = (Extent) mapView.getViewExtent().clone();
        aLN.getMapFrame().getMapView().zoomToExtent(aLayer.getExtent());
        UndoableEdit edit = (new MapViewUndoRedo()).new ZoomEdit(mapView, oldExtent, (Extent) mapView.getViewExtent().clone());
        mapView.fireUndoEditEvent(edit);
    }

    private void onMinVisScaleClick(ActionEvent e) {
        LayerNode aLN = (LayerNode) _selectedNode;
        MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
        aLayer.getVisibleScale().setEnableMinVisScale(true);
        aLayer.getVisibleScale().setMinVisScale(aLN.getMapFrame().getMapView().getGeoScale());

        this.paintGraphics();
        aLN.getMapFrame().getMapView().paintLayers();
    }

    private void onMaxVisScaleClick(ActionEvent e) {
        LayerNode aLN = (LayerNode) _selectedNode;
        MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
        aLayer.getVisibleScale().setEnableMaxVisScale(true);
        aLayer.getVisibleScale().setMaxVisScale(aLN.getMapFrame().getMapView().getGeoScale());

        this.paintGraphics();
        aLN.getMapFrame().getMapView().paintLayers();
    }

    private void onRemoveVisScaleClick(ActionEvent e) {
        LayerNode aLN = (LayerNode) _selectedNode;
        MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
        aLayer.getVisibleScale().setEnableMinVisScale(false);
        aLayer.getVisibleScale().setEnableMaxVisScale(false);

        this.paintGraphics();
        aLN.getMapFrame().getMapView().paintLayers();
    }

    private void onLabelClick(ActionEvent e) {
        LayerNode aLN = (LayerNode) _selectedNode;
        MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
        if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
            VectorLayer layer = (VectorLayer) aLayer;
            if (layer.getShapeNum() > 0) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                FrmLabelSet aFrmLabel = new FrmLabelSet(frame, false, this.getActiveMapFrame().getMapView());
                aFrmLabel.setLayer(layer);
                aFrmLabel.setLocationRelativeTo(frame);
                aFrmLabel.setVisible(true);
            }
        }
    }

    private void onMapFrameActiveClick(ActionEvent e) {
        setActiveMapFrame(_currentMapFrame);
    }

    private void onRemoveMapFrameClick(ActionEvent e) {
        removeMapFrame(_currentMapFrame);
    }

    private ItemNode getNodeByPosition(int x, int y, MousePos mPos) {
        if (y < 0) {
            if (_vScrollBar.getValue() == 0) {
                return _mapFrames.get(0);
            } else {
                return null;
            }
        }

        ItemNode aIN = null;
        mPos.inItem = false;
        mPos.curTop = 0;
        if (_vScrollBar.isVisible()) {
            mPos.curTop = -_vScrollBar.getValue();
        }

        for (MapFrame mapFrame : _mapFrames) {
            if (y > mPos.curTop && y < mPos.curTop + mapFrame.getHeight()) {
                return mapFrame;
            }

            mPos.curTop += mapFrame.getHeight() + Constants.ITEM_PAD;
            for (int i = mapFrame.getNodes().size() - 1; i >= 0; i--) {
                if (mapFrame.getNodes().get(i).getNodeType() == NodeTypes.LayerNode) {
                    if (y > mPos.curTop && y < mPos.curTop + mapFrame.getNodes().get(i).getDrawHeight()) {
                        if (y < mPos.curTop + mapFrame.getNodes().get(i).getHeight()) {
                            mPos.inItem = true;
                        }

                        return mapFrame.getNodes().get(i);
                    }
                    mPos.curTop += mapFrame.getNodes().get(i).getDrawHeight() + Constants.ITEM_PAD;
                } else {
                    GroupNode gNode = (GroupNode) mapFrame.getNodes().get(i);
                    if (y > mPos.curTop && y < mPos.curTop + gNode.getHeight()) {
                        return gNode;
                    }

                    mPos.curTop += gNode.getHeight() + Constants.ITEM_PAD;
                    if (gNode.isExpanded()) {
                        for (int j = gNode.getLayers().size() - 1; j >= 0; j--) {
                            LayerNode aLN = (LayerNode) gNode.getLayers().get(j);
                            if (y > mPos.curTop && y < mPos.curTop + aLN.getDrawHeight()) {
                                if (y < mPos.curTop + mapFrame.getNodes().get(i).getHeight()) {
                                    mPos.inItem = true;
                                }

                                return aLN;
                            }
                            mPos.curTop += aLN.getDrawHeight() + Constants.ITEM_PAD;
                        }
                    }
                }
            }
        }

        if (aIN == null) {
            if (y < this.getHeight()) {
                List<ItemNode> nodes = _mapFrames.get(_mapFrames.size() - 1).getNodes();
                if (nodes != null && nodes.size() > 0) {
                    ItemNode selNode = _mapFrames.get(_mapFrames.size() - 1).getNodes().get(0);
                    mPos.curTop = mPos.curTop - selNode.getDrawHeight() - Constants.ITEM_PAD;
                    return selNode;
                }
            }
        }

        return aIN;
    }

    private ItemNode getNodeByPositionEx(int x, int y, MousePos mPos) {
        ItemNode aIN = getNodeByPosition(x, y, mPos);
        if (aIN != null) {
            int leftPad = Constants.MAPFRAME_LEFT_PAD;
            if (aIN.getNodeType() == NodeTypes.MapFrameNode) {
                if (x > leftPad && x < leftPad + Constants.EXPAND_BOX_SIZE) {
                    mPos.inExpansionBox = true;
                } else {
                    mPos.inExpansionBox = false;
                }

                if (x > leftPad + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD
                        && x < leftPad + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD + Constants.CHECK_BOX_SIZE) {
                    mPos.inCheckBox = true;
                } else {
                    mPos.inCheckBox = false;
                }
            } else if (aIN.getNodeType() == NodeTypes.GroupNode) {
                leftPad += Constants.ITEM_LEFT_PAD;
                if (x > leftPad && x < leftPad + Constants.EXPAND_BOX_SIZE) {
                    mPos.inExpansionBox = true;
                } else {
                    mPos.inExpansionBox = false;
                }

                if (x > leftPad + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD
                        && x < leftPad + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD + Constants.CHECK_BOX_SIZE) {
                    mPos.inCheckBox = true;
                } else {
                    mPos.inCheckBox = false;
                }
            } else if (aIN.getNodeType() == NodeTypes.LayerNode) {
                if (mPos.inItem) {
                    leftPad += Constants.ITEM_LEFT_PAD;
                    if (((LayerNode) aIN).getGroupHandle() >= 0) {
                        leftPad += Constants.ITEM_LEFT_PAD;
                    }
                    if (x > leftPad && x < leftPad + Constants.EXPAND_BOX_SIZE) {
                        mPos.inExpansionBox = true;
                    } else {
                        mPos.inExpansionBox = false;
                    }

                    if (x > leftPad + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD
                            && x < leftPad + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD + Constants.CHECK_BOX_SIZE) {
                        mPos.inCheckBox = true;
                    } else {
                        mPos.inCheckBox = false;
                    }
                }
            }
        }

        return aIN;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Set a map frame as active map frame
     *
     * @param mapFrame The map frame
     */
    public void setActiveMapFrame(MapFrame mapFrame) {
        if (mapFrame == null)
            return;

        for (MapFrame mf : _mapFrames) {
            mf.setActive(false);
        }

        mapFrame.setActive(true);
        this.fireActiveMapFrameChangedEvent();
    }

    /**
     * Get new map frame name
     *
     * @return New map frame name
     */
    public String getNewMapFrameName() {
        List<String> names = new ArrayList<>();
        for (MapFrame mf : _mapFrames) {
            if (mf.getText().contains("New Map Frame")) {
                names.add(mf.getText());
            }
        }

        String name = "New Map Frame";
        if (names.size() > 0) {
            for (int i = 1; i <= 100; i++) {
                name = "New Map Frame " + String.valueOf(i);
                if (!names.contains(name)) {
                    break;
                }
            }
        }

        return name;
    }

    // <editor-fold desc="Nodes">
    /**
     * Add a map frame
     *
     * @param mf The map frame
     */
    public final void addMapFrame(MapFrame mf) {
        mf.addLayersUpdatedListener(new ILayersUpdatedListener() {
            @Override
            public void layersUpdatedEvent(LayersUpdatedEvent luEvent) {
                paintGraphics();
            }
        });
        mf.setLegend(this);
        _mapFrames.add(mf);

        if (_mapLayout != null) {
            _mapLayout.updateMapFrames(_mapFrames);
            _mapLayout.paintGraphics();
//            if (_isLayoutView) {
//                for (MapFrame aMF : _mapFrames) {
//                    aMF.setIsFireMapViewUpdate(true);
//                }                
//            }
        }

        this.fireMapFramesUpdatedEvent();
    }

    /**
     * Remove a map frame
     *
     * @param mapFrame The map frame
     */
    public void removeMapFrame(MapFrame mapFrame) {
        _mapFrames.remove(mapFrame);
        if (mapFrame.isActive()) {
            _mapFrames.get(0).setActive(true);
            this.fireActiveMapFrameChangedEvent();
        }

        if (_mapLayout != null) {
            _mapLayout.updateMapFrames(_mapFrames);
            _mapLayout.paintGraphics();
//            if (_isLayoutView) {
//                for (MapFrame aMF : _mapFrames) {
//                    aMF.setIsFireMapViewUpdate(true);
//                } 
//            }
        }

        this.paintGraphics();
        this.fireMapFramesUpdatedEvent();
    }

    private MapFrame getMapFrame(ItemNode aNode) {
        MapFrame mf = null;
        switch (aNode.getNodeType()) {
            case MapFrameNode:
                mf = (MapFrame) aNode;
                break;
            case LayerNode:
                mf = ((LayerNode) aNode).getMapFrame();
                break;
            case GroupNode:
                mf = ((GroupNode) aNode).getMapFrame();
                break;
        }
        return mf;
    }

    /**
     * Get MapFrame by text
     *
     * @param text Text
     * @return MapFrame
     */
    public MapFrame getMapFrame(String text) {
        for (MapFrame mf : this._mapFrames) {
            if (mf.getText().equalsIgnoreCase(text)) {
                return mf;
            }
        }

        return null;
    }

    /**
     * Select item node
     *
     * @param aNode The item node
     */
    public void selectNode(ItemNode aNode) {
        _selectedNode = aNode;
        for (MapFrame mf : _mapFrames) {
            mf.setSelected(false);
            mf.unSelectNodes();
        }

        aNode.setSelected(true);
        MapFrame aMF = getMapFrame(aNode);
        if (aNode.getNodeType() == NodeTypes.LayerNode) {
            aMF.getMapView().setSelectedLayerHandle(((LayerNode) aNode).getLayerHandle());
        } else {
            aMF.getMapView().setSelectedLayerHandle(-1);
        }

        this.fireNodeSelectedEvent();
    }
    //</editor-fold>  
    // <editor-fold desc="Painting Methods">

    /**
     * Paint component
     *
     * @param g Graphics
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setColor(this.getBackground());
        g2.clearRect(0, 0, this.getWidth(), this.getHeight());
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());

//        AffineTransform mx = new AffineTransform();
//        AffineTransformOp aop = new AffineTransformOp(mx, AffineTransformOp.TYPE_BICUBIC);
//        //g2.drawImage(_paintImage, 0, 0, this.getBackground(), this);
//        g2.drawImage(_paintImage, aop, 0, 0);

        this.paintGraphics(g2);

        if (_dragMode) {
            //Draw drag line                                                
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(Constants.ITEM_LEFT_PAD, _dragPosY,
                    this.getWidth() - Constants.ITEM_RIGHT_PAD, _dragPosY);
        }
    }

    /**
     * Paint graphics
     */
    public void paintGraphics() {
        if (this.getWidth() < 10 || this.getHeight() < 10) {
            return;
        }
        this._paintImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = this._paintImage.createGraphics();
        int totalHeight = calcTotalDrawHeight();
        Rectangle rect;
        if (totalHeight > this.getHeight()) {
            int sHeight = totalHeight - this.getHeight() + 20;
            _vScrollBar.setMinimum(0);
            _vScrollBar.setMaximum(totalHeight);
            _vScrollBar.setVisibleAmount(totalHeight - sHeight);
            _vScrollBar.setUnitIncrement(totalHeight / 10);
            _vScrollBar.setBlockIncrement(totalHeight / 5);
            if (_vScrollBar.isVisible() == false) {
                _vScrollBar.setValue(0);
                _vScrollBar.setVisible(true);
            }

            //RecalcItemPositions();
            rect = new Rectangle(0, -_vScrollBar.getValue(), this.getWidth() - _vScrollBar.getWidth(), totalHeight);
        } else {
            _vScrollBar.setVisible(false);
            rect = new Rectangle(0, 0, this.getWidth(), this.getHeight());
        }
        rect.y += Constants.ITEM_PAD;

        //Draw map frame
//        g.setColor(this.getBackground());
//        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        for (MapFrame mapFrame : _mapFrames) {
            drawMapFrame(g, mapFrame, new Point(Constants.MAPFRAME_LEFT_PAD, rect.y));
            rect.y += mapFrame.getDrawHeight() + Constants.ITEM_PAD * 2;
        }

        this.repaint();
    }

    /**
     * Paint graphics
     */
    public void paintGraphics(Graphics2D g) {
        if (this.getWidth() < 10 || this.getHeight() < 10) {
            return;
        }

        int totalHeight = calcTotalDrawHeight();
        Rectangle rect;
        if (totalHeight > this.getHeight()) {
            int sHeight = totalHeight - this.getHeight() + 20;
            _vScrollBar.setMinimum(0);
            _vScrollBar.setMaximum(totalHeight);
            _vScrollBar.setVisibleAmount(totalHeight - sHeight);
            _vScrollBar.setUnitIncrement(totalHeight / 10);
            _vScrollBar.setBlockIncrement(totalHeight / 5);
            if (_vScrollBar.isVisible() == false) {
                _vScrollBar.setValue(0);
                _vScrollBar.setVisible(true);
            }
            rect = new Rectangle(0, -_vScrollBar.getValue(), this.getWidth() - _vScrollBar.getWidth(), totalHeight);
        } else {
            _vScrollBar.setVisible(false);
            rect = new Rectangle(0, 0, this.getWidth(), this.getHeight());
        }
        rect.y += Constants.ITEM_PAD;

        //Draw map frame
        for (MapFrame mapFrame : _mapFrames) {
            drawMapFrame(g, mapFrame, new Point(Constants.MAPFRAME_LEFT_PAD, rect.y));
            rect.y += mapFrame.getDrawHeight() + Constants.ITEM_PAD * 2;
        }
    }

    private int calcTotalDrawHeight() {
        int height = 0;
        for (MapFrame mapFrame : _mapFrames) {
            height += mapFrame.getDrawHeight() + Constants.ITEM_PAD * 2;
        }

        height -= Constants.ITEM_PAD * 2;

        return height;
    }

    private void drawMapFrame(Graphics2D g, MapFrame aMapFrame, Point sP) {
        if (aMapFrame.isSelected()) {
            Rectangle rect = new Rectangle(0, sP.y, this.getWidth(), aMapFrame.getHeight());
            g.setColor(_selectedBackColor);
            g.fill(rect);
            //g.setColor(Color.lightGray);
            //g.draw(rect);
        }

        g.setColor(this.getForeground());
        if (aMapFrame.getNodes().size() > 0) {
            drawExpansionBox(g, new Point(sP.x, sP.y + Constants.EXPAND_BOX_TOP_PAD), aMapFrame.isExpanded());
        }

        //Image icon;
        FlatSVGIcon icon = new FlatSVGIcon("org/meteoinfo/icons/layers.svg");
        icon.paintIcon(this, g, sP.x + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD, sP.y);

        Font newFont = this.getFont();
        if (aMapFrame.isActive()) {
            newFont = new Font(newFont.getFontName(), Font.BOLD, newFont.getSize());
        }
        g.setFont(newFont);
        g.drawString(aMapFrame.getText(), sP.x + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD
                + Constants.CHECK_BOX_SIZE + Constants.TEXT_LEFT_PAD * 2, sP.y + aMapFrame.getHeight() - 4);

        //Draw nodes
        if (aMapFrame.isExpanded()) {
            sP.x += Constants.ITEM_LEFT_PAD;
            sP.y += aMapFrame.getHeight() + Constants.ITEM_PAD;
            for (int i = aMapFrame.getNodes().size() - 1; i >= 0; i--) {
                ItemNode aTN = aMapFrame.getNodes().get(i);
                if (aTN.getNodeType() == NodeTypes.GroupNode) {
                    if (sP.y + aTN.getDrawHeight() < this.getY()) {
                        sP.y += aTN.getDrawHeight() + Constants.ITEM_PAD;
                        continue;
                    }
                    Point dP = new Point(sP.x, sP.y);
                    drawGroupNode(g, (GroupNode) aTN, dP);
                } else {
                    LayerNode aLN = (LayerNode) aTN;
                    if (sP.y + aLN.getDrawHeight() < this.getY()) {
                        sP.y += aLN.getDrawHeight() + Constants.ITEM_PAD;
                        continue;
                    }
                    Point dP = new Point(sP.x, sP.y);
                    drawLayerNode(g, (LayerNode) aTN, dP);
                }

                sP.y += aTN.getDrawHeight() + Constants.ITEM_PAD;
                if (sP.y >= this.getY() + this.getHeight()) {
                    break;
                }
            }
        }
    }

    private void drawGroupNode(Graphics2D g, GroupNode groupNode, Point sP) {
        //Draw group
        if (groupNode.isSelected()) {
            Rectangle rect = new Rectangle(0, sP.y, this.getWidth(), groupNode.getHeight());
            g.setColor(_selectedBackColor);
            g.fill(rect);
            //g.setColor(Color.lightGray);
            //g.draw(rect);
        }

        g.setColor(this.getForeground());
        if (groupNode.getLayers().size() > 0) {
            drawExpansionBox(g, new Point(sP.x, sP.y + Constants.EXPAND_BOX_TOP_PAD), groupNode.isExpanded());
        }

        groupNode.updateCheckStatus();
        drawCheckBox(g, new Point(sP.x + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD,
                sP.y + Constants.CHECK_TOP_PAD), groupNode.getCheckStatus());
        //Font newFont = new Font(this.Font, FontStyle.Bold);
        g.setFont(this.getFont());
        g.drawString(groupNode.getText(), sP.x + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD
                + Constants.CHECK_BOX_SIZE + Constants.TEXT_LEFT_PAD, sP.y + groupNode.getHeight() - 4);

        //Draw layer nodes
        if (groupNode.isExpanded()) {
            sP.y += Constants.ITEM_HEIGHT + Constants.ITEM_PAD;
            sP.x += Constants.ITEM_LEFT_PAD;
            for (int j = groupNode.getLayers().size() - 1; j >= 0; j--) {
                LayerNode layerNode = groupNode.getLayers().get(j);
                Point dP = new Point(sP.x, sP.y);
                drawLayerNode(g, layerNode, dP);
                sP.y += layerNode.getDrawHeight() + Constants.ITEM_PAD;
            }
        }
    }

    private void drawLayerNode(Graphics2D g, LayerNode layerNode, Point sP) {
        //Draw Layer
        g.setColor(this.getForeground());
        if (layerNode.isSelected()) {
            Rectangle rect = new Rectangle(0, sP.y, this.getWidth(), layerNode.getHeight());
            g.setColor(_selectedBackColor);
            g.fill(rect);
            //g.setColor(Color.lightGray);
            //g.draw(rect);
        }

        if (layerNode.isEditing()) {
            Rectangle rect = new Rectangle(0, sP.y, this.getWidth(), layerNode.getHeight());
            g.setColor(Color.red);
            g.draw(rect);
        }

        if (layerNode.isSelected()) {
            g.setColor(this._selectedForeColor);
        } else {
            g.setColor(this.getForeground());
        }
        if (layerNode.getLegendNodes().size() > 0) {
            drawExpansionBox(g, new Point(sP.x, sP.y + Constants.EXPAND_BOX_TOP_PAD), layerNode.isExpanded());
        }

        int checkStatus = 0;
        if (layerNode.isChecked()) {
            checkStatus = 1;
        }

        drawCheckBox(g, new Point(sP.x + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD,
                sP.y + Constants.CHECK_TOP_PAD), checkStatus);
        g.setFont(this.getFont());
        g.drawString(layerNode.getText(), sP.x + Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD
                + Constants.CHECK_BOX_SIZE + Constants.TEXT_LEFT_PAD, sP.y + layerNode.getHeight() - 4);

        //Draw legend nodes
        if (layerNode.isExpanded()) {
            sP.x += Constants.ITEM_LEFT_PAD;
            //sP.X += Constants.EXPAND_BOX_SIZE + Constants.CHECK_LEFT_PAD + Constants.CHECK_BOX_SIZE;
            sP.y = sP.y + layerNode.getHeight() + Constants.ITEM_PAD;
            for (LegendNode aLN : layerNode.getLegendNodes()) {
                Rectangle rect = new Rectangle(sP.x, sP.y, 40, aLN.getHeight());
                drawLegendNode(aLN, rect, g);
                sP.y = sP.y + aLN.getHeight() + Constants.ITEM_PAD;
            }
        }
    }

    private void drawLegendNode(LegendNode aLN, Rectangle rect, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //float aSize;
        PointF aP = new PointF(0, 0);
        float width, height;
        String caption = "";

        switch (aLN.getLegendBreak().getBreakType()) {
            case PointBreak:
                PointBreak aPB = (PointBreak) aLN.getLegendBreak();
                caption = aPB.getCaption();
                aP.X = rect.x + rect.width / 2;
                aP.Y = rect.y + rect.height / 2;
                //aSize = aPB.getSize();
                if (aPB.isDrawShape()) {
                    if (aPB.getMarkerType() == MarkerType.Character) {
//                            TextRenderingHint aTextRendering = g.TextRenderingHint;
//                            g.TextRenderingHint = TextRenderingHint.AntiAlias;
                        Draw.drawPoint(aP, aPB, g);
                        //g.TextRenderingHint = aTextRendering;
                    } else {
                        Draw.drawPoint(aP, aPB, g);
                    }
                }
                break;
            case PolylineBreak:
                PolylineBreak aPLB = (PolylineBreak) aLN.getLegendBreak();
                caption = aPLB.getCaption();
                aP.X = rect.x + rect.width / 2;
                aP.Y = rect.y + rect.height / 2;
                //aSize = aPLB.getSize();
                width = rect.width / 3 * 2;
                height = rect.height / 3 * 2;
                Draw.drawPolylineSymbol(aP, width, height, aPLB, g);
                break;
            case PolygonBreak:
                PolygonBreak aPGB = (PolygonBreak) aLN.getLegendBreak();
                caption = aPGB.getCaption();
                aP.X = rect.x + rect.width / 2;
                aP.Y = rect.y + rect.height / 2;
                width = rect.width / 3 * 2;
                height = rect.height / 5 * 4;
                if (aPGB.isDrawShape()) {
                    Draw.drawPolygonSymbol(aP, width, height, aPGB, g);
                }
                break;
            case ColorBreak:
                ColorBreak aCB = aLN.getLegendBreak();
                caption = aCB.getCaption();
                aP.X = rect.x + rect.width / 2;
                aP.Y = rect.y + rect.height / 2;
                width = rect.width / 3 * 2;
                height = rect.height / 3 * 2;
                Draw.drawPolygonSymbol(aP, aCB.getColor(), Color.black, width,
                        height, true, true, g);
                break;
            case ChartBreak:
                ChartBreak aChB = (ChartBreak) aLN.getLegendBreak();
                aP.X = rect.x;
                aP.Y = rect.y + rect.height - 5;
                switch (aChB.getChartType()) {
                    case BarChart:
                        Draw.drawBarChartSymbol(aP, aChB, g, true);
                        break;
                    case PieChart:
                        Draw.drawPieChartSymbol(aP, aChB, g, null);
                        break;
                }
                break;
        }

        int x = rect.x + rect.width + 5;
        int y = rect.y + rect.height * 3 / 4;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setFont(this.getFont());
        g.setColor(this.getForeground());
        g.drawString(caption, x, y);
    }

    private void drawExpansionBox(Graphics2D g, Point sP, boolean expanded) {
        int size = 8;
        int gap = 2;
        Rectangle rect = new Rectangle(sP.x, sP.y, size, size);
        //g.setColor(Color.gray);
        g.draw(rect);

        GeneralPath path = new GeneralPath();
        path.moveTo(sP.x + gap, sP.y + size / 2);
        path.lineTo(sP.x + size - gap, sP.y + size / 2);
        if (!expanded) {
            path.moveTo(sP.x + size / 2, sP.y + gap);
            path.lineTo(sP.x + size / 2, sP.y + size - gap);
        }

        //g.setColor(Color.black);
        g.draw(path);
    }

    private void drawCheckBox(Graphics2D g, Point sP, int checkStatus) {
        int size = 10;
        Rectangle rect = new Rectangle(sP.x, sP.y, size, size);
        //g.setColor(Color.gray);
        g.draw(rect);

        if (checkStatus == 2) {
            g.setColor(Color.lightGray);
            g.fill(rect);
            g.setColor(this.getForeground());
        }

        switch (checkStatus) {
            case 1:    //Checked
            case 2:    //Partly checked
                GeneralPath path = new GeneralPath();
                path.moveTo(sP.x + 2, sP.y + 6);
                path.lineTo(sP.x + 5, sP.y + 8);
                path.moveTo(sP.x + 5, sP.y + 8);
                path.lineTo(sP.x + 8, sP.y + 2);
                //g.setColor(Color.black);
                g.draw(path);
                break;
        }
    }
    // </editor-fold>

    // <editor-fold desc="XML import and export">
    /**
     * Export project XML content
     *
     * @param m_Doc XML document
     * @param parent Parent XML element
     * @param projectFilePath Project file path
     */
    public void exportProjectXML(Document m_Doc, Element parent, String projectFilePath) {
        Element mapFrames = m_Doc.createElement("MapFrames");
        for (MapFrame mf : _mapFrames) {
            mf.exportProjectXML(m_Doc, mapFrames, projectFilePath);
        }
        parent.appendChild(mapFrames);
    }

    /**
     * Import project XML content
     *
     * @param fileName XML file name
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public void importProjectXML(String fileName) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(fileName));

        Element root = doc.getDocumentElement();

        Properties property = System.getProperties();
        String path = System.getProperty("user.dir");
        property.setProperty("user.dir", new File(fileName).getAbsolutePath());
        String pPath = new File(fileName).getParent();

        this.getActiveMapFrame().getMapView().setLockViewUpdate(true);
        this.importProjectXML(pPath, root);
        this.getActiveMapFrame().getMapView().setLockViewUpdate(false);
        this.paintGraphics();
        this.getActiveMapFrame().getMapView().paintLayers();

        property.setProperty("user.dir", path);
    }

    /**
     * Import project XML content
     *
     * @param pPath Project file parent path
     * @param parent Parent XML element
     */
    public void importProjectXML(String pPath, Element parent) {
        _mapFrames.clear();
        Element mapFrames = (Element) parent.getElementsByTagName("MapFrames").item(0);
        if (mapFrames == null) {
            MapFrame mf = new MapFrame(this);
            mf.importProjectXML(pPath, parent);
            mf.addLayersUpdatedListener(new ILayersUpdatedListener() {
                @Override
                public void layersUpdatedEvent(LayersUpdatedEvent event) {
                    paintGraphics();
                }
            });
            mf.setActive(true);
            _mapFrames.add(mf);
        } else {
            NodeList mfNodes = mapFrames.getElementsByTagName("MapFrame");
            for (int i = 0; i < mfNodes.getLength(); i++) {
                Node mapFrame = mfNodes.item(i);
                MapFrame mf = new MapFrame(this);
                mf.importProjectXML(pPath, (Element) mapFrame);
                mf.addLayersUpdatedListener(new ILayersUpdatedListener() {
                    @Override
                    public void layersUpdatedEvent(LayersUpdatedEvent event) {
                        paintGraphics();
                    }
                });
                mf.setLegend(this);
                _mapFrames.add(mf);
            }
        }
    }
    
    // </editor-fold>
    // </editor-fold>
}
