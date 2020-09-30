/* Copyright 2012 - Yaqiang Wang,
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
package org.meteoinfo.desktop.forms;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.help.HelpSet;
import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.xml.parsers.ParserConfigurationException;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.meteoinfo.data.mapdata.webmap.WebMapProvider;
import org.meteoinfo.desktop.config.GenericFileFilter;
import org.meteoinfo.desktop.config.Options;
import org.meteoinfo.desktop.config.Plugin;
import org.meteoinfo.desktop.config.PluginCollection;
import org.meteoinfo.desktop.config.ProjectFile;
import org.meteoinfo.data.mapdata.FrmAttriData;
import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.FrmProperty;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointF;
import org.meteoinfo.global.event.ActiveMapFrameChangedEvent;
import org.meteoinfo.global.event.ElementSelectedEvent;
import org.meteoinfo.global.event.GraphicSelectedEvent;
import org.meteoinfo.global.event.IActiveMapFrameChangedListener;
import org.meteoinfo.global.event.IElementSelectedListener;
import org.meteoinfo.global.event.IGraphicSelectedListener;
import org.meteoinfo.global.event.INodeSelectedListener;
import org.meteoinfo.global.event.IShapeSelectedListener;
import org.meteoinfo.global.event.IUndoEditListener;
import org.meteoinfo.global.event.IZoomChangedListener;
import org.meteoinfo.global.event.NodeSelectedEvent;
import org.meteoinfo.global.event.ShapeSelectedEvent;
import org.meteoinfo.global.event.UndoEditEvent;
import org.meteoinfo.global.event.ZoomChangedEvent;
//import org.meteoinfo.help.Help;
import org.meteoinfo.layer.*;
import org.meteoinfo.ui.WrappingLayout;
import org.meteoinfo.layout.ElementType;
import org.meteoinfo.layout.FrmPageSet;
import org.meteoinfo.layout.LayoutGraphic;
import org.meteoinfo.layout.LayoutLegend;
import org.meteoinfo.layout.LayoutNorthArrow;
import org.meteoinfo.layout.LayoutScaleBar;
import org.meteoinfo.layout.MapLayout;
import org.meteoinfo.layout.MapLayoutUndoRedo;
import org.meteoinfo.layout.MouseMode;
import org.meteoinfo.legend.ItemNode;
import org.meteoinfo.legend.LayerNode;
import org.meteoinfo.legend.LayersLegend;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.legend.NodeTypes;
import org.meteoinfo.map.FeatureUndoableEdit;
import org.meteoinfo.map.MapView;
import org.meteoinfo.map.MapViewUndoRedo;
import org.meteoinfo.map.MaskOut;
import org.meteoinfo.map.MouseTools;
import org.meteoinfo.plugin.IApplication;
import org.meteoinfo.plugin.IPlugin;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.data.mapdata.ShapeFileManage;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.meteoinfo.shape.ShapeFactory;
import org.meteoinfo.shape.ShapeSelection;
import org.xml.sax.SAXException;

/**
 *
 * @author Yaqiang Wang
 */
public class FrmMain extends JFrame implements IApplication {
    // <editor-fold desc="Variables">

    private String _startupPath;
    private Options _options = new Options();
    private AbstractButton _currentTool = null;
    ResourceBundle bundle;
    ProjectFile _projectFile;
    //private boolean _isEditingVertices = false;
    private boolean _isLoading = false;
    private FrmMeteoData _frmMeteoData;
    //private String _currentDataFolder = "";
    private PluginCollection _plugins = new PluginCollection();
    private FlatSVGIcon _loadedPluginIcon;
    private FlatSVGIcon _unloadedPluginIcon;
    private final UndoManager undoManager = new UndoManager();
    private UndoManager zoomUndoManager = new UndoManager();
    private UndoManager currentUndoManager;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    
    public FrmMain() {
        initComponents();
    }

    public FrmMain(String startupPath, Options options) {
        //Locale.setDefault(Locale.ENGLISH);
        initComponents();
        
        this._startupPath = startupPath;
        this._options = options;

        currentUndoManager = undoManager;
        _mapDocument.addActiveMapFrameChangedListener(new IActiveMapFrameChangedListener() {
            @Override
            public void activeMapFrameChangedEvent(ActiveMapFrameChangedEvent event) {
                _mapView = _mapDocument.getActiveMapFrame().getMapView();
                setMapView();
                if (jTabbedPane_Main.getSelectedIndex() == 0) {
                    _mapView.paintLayers();
                }
            }
        });
        _mapDocument.addNodeSelectedListener(new INodeSelectedListener() {
            @Override
            public void nodeSelectedEvent(NodeSelectedEvent event) {
                ItemNode selNode = _mapDocument.getSelectedNode();
                switch (selNode.getNodeType()) {
                    case LayerNode:
                        MapLayer layer = ((LayerNode) selNode).getMapLayer();
                        if (layer.getLayerType() == LayerTypes.VectorLayer) {
                            if (!((VectorLayer) layer).isProjected()) {
                                jToolBar_Edit.setEnabled(true);
                                jButton_EditStartOrEnd.setEnabled(true);
                                jButton_EditStartOrEnd.setSelected(((LayerNode) selNode).isEditing());
                                if (jButton_EditStartOrEnd.isSelected()) {
                                    currentUndoManager = ((VectorLayer) layer).getUndoManager();
                                    refreshUndoRedo();
                                    jButton_EditNewFeature.setEnabled(true);
                                    if (((VectorLayer) layer).hasSelectedShapes()) {
                                        jButton_EditRemoveFeature.setEnabled(true);
                                        jButton_EditFeatureVertices.setEnabled(true);
                                    }
                                }
                            } else {
                                jToolBar_Edit.setEnabled(false);
                                for (Component c : jToolBar_Edit.getComponents()) {
                                    c.setEnabled(false);
                                }
                            }
                        } else {
                            jToolBar_Edit.setEnabled(false);
                            for (Component c : jToolBar_Edit.getComponents()) {
                                c.setEnabled(false);
                            }
                        }
                        break;
                    default:
                        jToolBar_Edit.setEnabled(false);
                        for (Component c : jToolBar_Edit.getComponents()) {
                            c.setEnabled(false);
                        }
                        break;
                }
            }
        });
        _mapLayout.addElementSelectedListener(new IElementSelectedListener() {
            @Override
            public void elementSelectedEvent(ElementSelectedEvent event) {
                if (_mapLayout.getSelectedElements().size() > 0) {
                    if (_mapLayout.getSelectedElements().get(0).getElementType() == ElementType.LayoutGraphic) {
                        switch (((LayoutGraphic) _mapLayout.getSelectedElements().get(0)).getGraphic().getShape().getShapeType()) {
                            case Polyline:
                            case CurveLine:
                            case Polygon:
                            case CurvePolygon:
                                jButton_EditVertices.setEnabled(true);
                                break;
                            default:
                                jButton_EditVertices.setEnabled(false);
                                break;
                        }
                    }
                } else {
                    jButton_EditVertices.setEnabled(false);
                }
            }
        });
        _mapLayout.addZoomChangedListener(new IZoomChangedListener() {
            @Override
            public void zoomChangedEvent(ZoomChangedEvent event) {
                jComboBox_PageZoom.setSelectedItem(String.valueOf((int) (_mapDocument.getMapLayout().getZoom() * 100)) + "%");
            }
        });
        _mapLayout.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                layout_MouseMoved(e);
            }
        });
        _mapLayout.addUndoEditListener(new IUndoEditListener() {
            @Override
            public void undoEditEvent(UndoEditEvent event, UndoableEdit undoEdit) {
                undoManager.addEdit(undoEdit);
                refreshUndoRedo();
            }
        });

        this.jPanel_MapTab.setLayout(new BorderLayout());
        _mapLayout.setFocusable(true);
        _mapLayout.requestFocusInWindow();

        _projectFile = new ProjectFile(this);
        this._mapDocument.getActiveMapFrame().setMapView(_mapView);
        this._mapDocument.setMapLayout(_mapLayout);
        //this._mapDocument.setIsLayoutView(false);
        _mapLayout.setLockViewUpdate(true);
        //this._options.setLegendFont(_mapDocument.getFont());

        BufferedImage image = null;
        try {
            image = ImageIO.read(this.getClass().getResource("/images/MeteoInfo_1_16x16x8.png"));
        } catch (IOException e) {
        }
        this.setIconImage(image);
        this.setTitle("MeteoInfoMap");
        this.jMenuItem_Layers.setSelected(true);
        this.jButton_SelectElement.doClick();

        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        String pluginPath;
        if (isDebug) {
            pluginPath = "D:/MyProgram/Java/MeteoInfoDev/plugins";
        } else {
            pluginPath = this._startupPath + File.separator + "plugins";
        }
        this._plugins.setPluginPath(pluginPath);
        this._plugins.setPluginConfigFile(pluginPath + File.separator + "plugins.xml");

//        //Help
//        HelpSet hs = getHelpSet("/org/meteoinfo/help/mi.hs");
//        HelpBroker hb = hs.createHelpBroker();
//        //Assign help to components
//        CSH.setHelpIDString(this.jMenuItem_Help, "top");
//        //Handle events
//        this.jMenuItem_Help.addActionListener(new CSH.DisplayHelpFromSource(hb));
        loadForm();
    }

    private void initComponents() {
        jPanel_MainToolBar = new javax.swing.JPanel();
        jToolBar_Base = new javax.swing.JToolBar();
        jSplitButton_AddLayer = new org.meteoinfo.ui.JSplitButton();
        jPopupMenu_AddLayer = new javax.swing.JPopupMenu();
        jMenuItem_AddLayer = new javax.swing.JMenuItem();
        jMenuItem_AddWebLayer = new javax.swing.JMenuItem();
        jButton_OpenData = new javax.swing.JButton();
        jButton_RemoveDataLayers = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton_SelectElement = new javax.swing.JToggleButton();
        jButton_ZoomIn = new javax.swing.JToggleButton();
        jButton_ZoomOut = new javax.swing.JToggleButton();
        jButton_Pan = new javax.swing.JToggleButton();
        jButton_FullExtent = new javax.swing.JButton();
        jButton_ZoomToLayer = new javax.swing.JButton();
        jButton_ZoomToExtent = new javax.swing.JButton();
        jButton_ZoomUndo = new javax.swing.JButton();
        jButton_ZoomRedo = new javax.swing.JButton();
        jButton_Identifer = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jSplitButton_SelectFeature = new org.meteoinfo.ui.JSplitToggleButton();
        jPopupMenu_SelectFeature = new javax.swing.JPopupMenu();
        jMenuItem_SelByRectangle = new javax.swing.JMenuItem();
        jMenuItem_SelByPolygon = new javax.swing.JMenuItem();
        jMenuItem_SelByLasso = new javax.swing.JMenuItem();
        jMenuItem_SelByCircle = new javax.swing.JMenuItem();
        jButton_Measurement = new javax.swing.JButton();
        jButton_LabelSet = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton_SavePicture = new javax.swing.JButton();
        jToolBar_Graphic = new javax.swing.JToolBar();
        jButton_NewLabel = new javax.swing.JToggleButton();
        jButton_NewPoint = new javax.swing.JToggleButton();
        jButton_NewPolyline = new javax.swing.JToggleButton();
        jButton_NewFreehand = new javax.swing.JToggleButton();
        jButton_NewCurve = new javax.swing.JToggleButton();
        jButton_NewPolygon = new javax.swing.JToggleButton();
        jButton_NewCurvePolygon = new javax.swing.JToggleButton();
        jButton_NewRectangle = new javax.swing.JToggleButton();
        jButton_NewCircle = new javax.swing.JToggleButton();
        jButton_NewEllipse = new javax.swing.JToggleButton();
        jButton_EditVertices = new javax.swing.JToggleButton();
        jToolBar_Layout = new javax.swing.JToolBar();
        jButton_PageSet = new javax.swing.JButton();
        jButton_PageZoomIn = new javax.swing.JButton();
        jButton_PageZoomOut = new javax.swing.JButton();
        jButton_FitToScreen = new javax.swing.JButton();
        jComboBox_PageZoom = new javax.swing.JComboBox();
        jToolBar_Edit = new javax.swing.JToolBar();
        jButton_EditStartOrEnd = new javax.swing.JToggleButton();
        jButton_EditSave = new javax.swing.JButton();
        //jSeparator19 = new javax.swing.JToolBar.Separator();
        jButton_EditTool = new javax.swing.JToggleButton();
        //jSeparator20 = new javax.swing.JToolBar.Separator();
        jButton_EditNewFeature = new javax.swing.JToggleButton();
        jButton_EditRemoveFeature = new javax.swing.JButton();
        jButton_EditFeatureVertices = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane_Main = new javax.swing.JTabbedPane();
        jPanel_MapTab = new javax.swing.JPanel();
        _mapView = new org.meteoinfo.map.MapView();
        jPanel_LayoutTab = new javax.swing.JPanel();
        _mapLayout = new org.meteoinfo.layout.MapLayout();
        _mapDocument = new org.meteoinfo.legend.LayersLegend();
        jPanel_Status = new javax.swing.JPanel();
        jLabel_Status = new javax.swing.JLabel();
        jLabel_Coordinate = new javax.swing.JLabel();
        jMenuBar_Main = new javax.swing.JMenuBar();
        jMenu_Project = new javax.swing.JMenu();
        jMenuItem_Open = new javax.swing.JMenuItem();
        jMenuItem_Save = new javax.swing.JMenuItem();
        jMenuItem_SaveAs = new javax.swing.JMenuItem();
        jMenu_Edit = new javax.swing.JMenu();
        jMenuItem_Undo = new javax.swing.JMenuItem();
        jMenuItem_Redo = new javax.swing.JMenuItem();
        jMenuItem_Cut = new javax.swing.JMenuItem();
        jMenuItem_Copy = new javax.swing.JMenuItem();
        jMenuItem_Paste = new javax.swing.JMenuItem();
        jMenuItem_NewLayer = new javax.swing.JMenuItem();
        jMenuItem_AddRing = new javax.swing.JMenuItem();
        jMenuItem_FillRing = new javax.swing.JMenuItem();
        jMenuItem_DeleteRing = new javax.swing.JMenuItem();
        jMenuItem_ReformFeature = new javax.swing.JMenuItem();
        jMenuItem_SplitFeature = new javax.swing.JMenuItem();
        jMenuItem_MergeFeature = new javax.swing.JMenuItem();
        jMenu_View = new javax.swing.JMenu();
        jMenuItem_Layers = new javax.swing.JMenuItem();
        jMenuItem_AttributeData = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_LayoutProperty = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_MapProperty = new javax.swing.JMenuItem();
        jMenuItem_MaskOut = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Projection = new javax.swing.JMenuItem();
        jMenu_Insert = new javax.swing.JMenu();
        jMenuItem_InsertMapFrame = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_InsertTitle = new javax.swing.JMenuItem();
        jMenuItem_InsertText = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_InsertLegend = new javax.swing.JMenuItem();
        jMenuItem_InsertScaleBar = new javax.swing.JMenuItem();
        jMenuItem_InsertNorthArrow = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_InsertWindArrow = new javax.swing.JMenuItem();
        jMenu_Selection = new javax.swing.JMenu();
        jMenuItem_SelByAttr = new javax.swing.JMenuItem();
        jMenuItem_SelByLocation = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_ClearSelection = new javax.swing.JMenuItem();
        jMenu_GeoProcessing = new javax.swing.JMenu();
        jMenuItem_Buffer = new javax.swing.JMenuItem();
        jMenuItem_Clipping = new javax.swing.JMenuItem();
        jMenuItem_Convexhull = new javax.swing.JMenuItem();
        jMenuItem_Intersection = new javax.swing.JMenuItem();
        jMenuItem_Difference = new javax.swing.JMenuItem();
        jMenuItem_SymDifference = new javax.swing.JMenuItem();
        jMenu_Tools = new javax.swing.JMenu();
        jMenuItem_Script = new javax.swing.JMenuItem();
        //jMenuItem_ScriptConsole = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Options = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_OutputMapData = new javax.swing.JMenuItem();
        jMenuItem_AddXYData = new javax.swing.JMenuItem();        
        jMenuItem_Animator = new javax.swing.JMenuItem();
        jMenu_NetCDFData = new javax.swing.JMenu();
        jMenuItem_JoinNCFiles = new javax.swing.JMenuItem();
        jMenu_Plugin = new javax.swing.JMenu();
        jMenuItem_PluginManager = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        jMenu_Help = new javax.swing.JMenu();
        jMenuItem_About = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Help = new javax.swing.JMenuItem();
        jProgressBar_Main = new javax.swing.JProgressBar();
        jLabel_ProgressBar = new javax.swing.JLabel();

        //Window listener
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }

            @Override
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        //Base tool bar
        jToolBar_Base.setFloatable(true);
        jToolBar_Base.setRollover(true);
        jToolBar_Base.setName(""); // NOI18N

        final java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("bundle/Bundle_FrmMain");

        //Split button
        //jSplitButton_AddLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Add_Layer.png")));
        jSplitButton_AddLayer.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/add-layer-plus.svg"));
        jSplitButton_AddLayer.setText("  ");
        jSplitButton_AddLayer.setToolTipText(bundle.getString("FrmMain.jMenuItem_AddLayer.toolTipText"));
        jSplitButton_AddLayer.setArrowColor(ColorUtil.parseToColor("#6E6E6E"));
        jSplitButton_AddLayer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!jSplitButton_AddLayer.isOnSplit()) {
                    jButton_AddLayerActionPerformed(evt);
                }
            }
        });
        //jMenuItem_AddLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_rectangle.png")));
        jMenuItem_AddLayer.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/add-layer-plus.svg"));
        jMenuItem_AddLayer.setText(bundle.getString("FrmMain.jMenuItem_AddLayer.text"));
        jMenuItem_AddLayer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jSplitButton_AddLayer.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/add-layer-plus.svg"));
                jSplitButton_AddLayer.setText("  ");
                jSplitButton_AddLayer.setToolTipText(bundle.getString("FrmMain.jMenuItem_AddLayer.toolTipText"));
                jButton_AddLayerActionPerformed(e);
            }
        });
        jPopupMenu_AddLayer.add(jMenuItem_AddLayer);
        jMenuItem_AddWebLayer.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/add-web-layer.svg"));
        jMenuItem_AddWebLayer.setText(bundle.getString("FrmMain.jMenuItem_AddWebLayer.text"));
        jMenuItem_AddWebLayer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jButton_AddWebLayerActionPerformed(e);
            }
        });
        jPopupMenu_AddLayer.add(jMenuItem_AddWebLayer);
        jSplitButton_AddLayer.setPopupMenu(jPopupMenu_AddLayer);
        jToolBar_Base.add(jSplitButton_AddLayer);

        //jButton_OpenData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Folder_1_16x16x8.png"))); // NOI18N
        jButton_OpenData.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-open.svg"));
        jButton_OpenData.setToolTipText(bundle.getString("FrmMain.jButton_OpenData.toolTipText")); // NOI18N
        jButton_OpenData.setFocusable(false);
        jButton_OpenData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_OpenData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_OpenData.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OpenDataActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_OpenData);

        //jButton_RemoveDataLayers.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_RemoveDataLayes.Image.png"))); // NOI18N
        jButton_RemoveDataLayers.setIcon(new FlatSVGIcon("org/meteoinfo/icons/delete.svg"));
        jButton_RemoveDataLayers.setToolTipText(bundle.getString("FrmMain.jButton_RemoveDataLayers.toolTipText")); // NOI18N
        jButton_RemoveDataLayers.setFocusable(false);
        jButton_RemoveDataLayers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_RemoveDataLayers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_RemoveDataLayers.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RemoveDataLayersActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_RemoveDataLayers);
        jToolBar_Base.add(jSeparator1);

        //jButton_SelectElement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Arrow.png"))); // NOI18N
        jButton_SelectElement.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select.svg"));
        jButton_SelectElement.setToolTipText(bundle.getString("FrmMain.jButton_SelectElement.toolTipText")); // NOI18N
        jButton_SelectElement.setFocusable(false);
        jButton_SelectElement.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SelectElement.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SelectElement.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SelectElementActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_SelectElement);

        //jButton_ZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomIn.Image.png"))); // NOI18N
        jButton_ZoomIn.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/zoom-in.svg"));
        jButton_ZoomIn.setToolTipText(bundle.getString("FrmMain.jButton_ZoomIn.toolTipText")); // NOI18N
        jButton_ZoomIn.setFocusable(false);
        jButton_ZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomIn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomInActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_ZoomIn);

        //jButton_ZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomOut.Image.png"))); // NOI18N
        jButton_ZoomOut.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/zoom-out.svg"));
        jButton_ZoomOut.setToolTipText(bundle.getString("FrmMain.jButton_ZoomOut.toolTipText")); // NOI18N
        jButton_ZoomOut.setFocusable(false);
        jButton_ZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomOut.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomOutActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_ZoomOut);

        //jButton_Pan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Pan.Image.png"))); // NOI18N
        //jButton_Pan.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/pan.svg"));
        jButton_Pan.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/hand.svg"));
        jButton_Pan.setToolTipText(bundle.getString("FrmMain.jButton_Pan.toolTipText")); // NOI18N
        jButton_Pan.setFocusable(false);
        jButton_Pan.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Pan.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Pan.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PanActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_Pan);

        //jButton_FullExtent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_FullExent.Image.png"))); // NOI18N
        jButton_FullExtent.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/full-extent.svg"));
        jButton_FullExtent.setToolTipText(bundle.getString("FrmMain.jButton_FullExtent.toolTipText")); // NOI18N
        jButton_FullExtent.setFocusable(false);
        jButton_FullExtent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_FullExtent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_FullExtent.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_FullExtentActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_FullExtent);

        //jButton_ZoomToLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomToLayer.Image.png"))); // NOI18N
        jButton_ZoomToLayer.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/zoom-layer.svg"));
        jButton_ZoomToLayer.setToolTipText(bundle.getString("FrmMain.jButton_ZoomToLayer.toolTipText")); // NOI18N
        jButton_ZoomToLayer.setFocusable(false);
        jButton_ZoomToLayer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomToLayer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomToLayer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomToLayerActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_ZoomToLayer);

        //jButton_ZoomToExtent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomToExtent.Image.png"))); // NOI18N
        jButton_ZoomToExtent.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/zoom-extent.svg"));
        jButton_ZoomToExtent.setToolTipText(bundle.getString("FrmMain.jButton_ZoomToExtent.toolTipText")); // NOI18N
        jButton_ZoomToExtent.setFocusable(false);
        jButton_ZoomToExtent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomToExtent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomToExtent.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomToExtentActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_ZoomToExtent);

        //jButton_ZoomUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_PreTime.Image.png"))); // NOI18N
        jButton_ZoomUndo.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/undo.svg"));
        jButton_ZoomUndo.setToolTipText(bundle.getString("FrmMain.jButton_ZoomUndo.toolTipText")); // NOI18N
        jButton_ZoomUndo.setFocusable(false);
        jButton_ZoomUndo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomUndo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomUndoActionPerformed(evt);
            }
        });
        jButton_ZoomUndo.setEnabled(false);
        jToolBar_Base.add(jButton_ZoomUndo);

        //jButton_ZoomRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NextTime.Image.png"))); // NOI18N
        jButton_ZoomRedo.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/redo.svg"));
        jButton_ZoomRedo.setToolTipText(bundle.getString("FrmMain.jButton_ZoomRedo.toolTipText")); // NOI18N
        jButton_ZoomRedo.setFocusable(false);
        jButton_ZoomRedo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomRedo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomRedo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomRedoActionPerformed(evt);
            }
        });
        jButton_ZoomRedo.setEnabled(false);
        jToolBar_Base.add(jButton_ZoomRedo);
        jToolBar_Base.add(new JSeparator());

        //jButton_Identifer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/information.png"))); // NOI18N
        jButton_Identifer.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/information.svg"));
        jButton_Identifer.setToolTipText(bundle.getString("FrmMain.jButton_Identifer.toolTipText")); // NOI18N
        jButton_Identifer.setFocusable(false);
        jButton_Identifer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Identifer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Identifer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_IdentiferActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_Identifer);
        jToolBar_Base.add(jSeparator2);

        //Split button
        //jSplitButton_SelectFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_rectangle.png")));
        jSplitButton_SelectFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-rectangle.svg"));
        jSplitButton_SelectFeature.setText("  ");
        jSplitButton_SelectFeature.setToolTipText(bundle.getString("FrmMain.jButton_SelectFeature.toolTipText_Rectangle"));
        jSplitButton_SelectFeature.setArrowColor(ColorUtil.parseToColor("#6E6E6E"));
        jSplitButton_SelectFeature.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String toolTipText = jSplitButton_SelectFeature.getToolTipText();
                if (toolTipText.equals(bundle.getString("FrmMain.jButton_SelectFeature.toolTipText_Polygon"))) {
                    jButton_SelByPolygonActionPerformed(evt);
                } else if (toolTipText.equals(bundle.getString("FrmMain.jButton_SelectFeature.toolTipText_Lasso"))) {
                    jButton_SelByLassoActionPerformed(evt);
                } else if (toolTipText.equals(bundle.getString("FrmMain.jButton_SelectFeature.toolTipText_Circle"))) {
                    jButton_SelByCircleActionPerformed(evt);
                } else {
                    jButton_SelByRectangleActionPerformed(evt);
                }

                setCurrentTool((JToggleButton) evt.getSource());
            }
        });
        //jMenuItem_SelByRectangle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_rectangle.png")));
        jMenuItem_SelByRectangle.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-rectangle.svg"));
        jMenuItem_SelByRectangle.setText(bundle.getString("FrmMain.jMenuItem_SelByRectangle.text"));
        jMenuItem_SelByRectangle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //jSplitButton_SelectFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_rectangle.png")));
                jSplitButton_SelectFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-rectangle.svg"));
                jSplitButton_SelectFeature.setText("  ");
                jSplitButton_SelectFeature.setToolTipText(bundle.getString("FrmMain.jButton_SelectFeature.toolTipText_Rectangle"));
                jButton_SelByRectangleActionPerformed(e);
                setCurrentTool(jSplitButton_SelectFeature);
            }
        });
        jPopupMenu_SelectFeature.add(jMenuItem_SelByRectangle);
        //jMenuItem_SelByPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_polygon.png")));
        jMenuItem_SelByPolygon.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-polygon.svg"));
        jMenuItem_SelByPolygon.setText(bundle.getString("FrmMain.jMenuItem_SelByPolygon.text"));
        jMenuItem_SelByPolygon.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //jSplitButton_SelectFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_polygon.png")));
                jSplitButton_SelectFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-polygon.svg"));
                jSplitButton_SelectFeature.setText("  ");
                jSplitButton_SelectFeature.setToolTipText(bundle.getString("FrmMain.jButton_SelectFeature.toolTipText_Polygon"));
                jButton_SelByPolygonActionPerformed(e);
                setCurrentTool(jSplitButton_SelectFeature);
            }
        });
        jPopupMenu_SelectFeature.add(jMenuItem_SelByPolygon);
        //jMenuItem_SelByLasso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_lasso.png")));
        jMenuItem_SelByLasso.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-lasso.svg"));
        jMenuItem_SelByLasso.setText(bundle.getString("FrmMain.jMenuItem_SelByLasso.text"));
        jMenuItem_SelByLasso.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //jSplitButton_SelectFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_lasso.png")));
                jSplitButton_SelectFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-lasso.svg"));
                jSplitButton_SelectFeature.setText("  ");
                jSplitButton_SelectFeature.setToolTipText(bundle.getString("FrmMain.jButton_SelectFeature.toolTipText_Lasso"));
                jButton_SelByLassoActionPerformed(e);
                setCurrentTool(jSplitButton_SelectFeature);
            }
        });
        jPopupMenu_SelectFeature.add(jMenuItem_SelByLasso);
        //jMenuItem_SelByCircle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_circle.png")));
        jMenuItem_SelByCircle.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-circle.svg"));
        jMenuItem_SelByCircle.setText(bundle.getString("FrmMain.jMenuItem_SelByCircle.text"));
        jMenuItem_SelByCircle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //jSplitButton_SelectFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select_circle.png")));
                jSplitButton_SelectFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-circle.svg"));
                jSplitButton_SelectFeature.setText("  ");
                jSplitButton_SelectFeature.setToolTipText(bundle.getString("FrmMain.jButton_SelectFeature.toolTipText_Circle"));
                jButton_SelByCircleActionPerformed(e);
                setCurrentTool(jSplitButton_SelectFeature);
            }
        });
        jPopupMenu_SelectFeature.add(jMenuItem_SelByCircle);
        jSplitButton_SelectFeature.setPopupMenu(jPopupMenu_SelectFeature);
        //jSplitButton_SelectFeature.add(jPopupMenu_SelectFeature);
        //jSplitButton_SelectFeature.setToolTipText(bundle.getString("FrmMain.jButton_SelectFeatures.toolTipText")); // NOI18N
        //jSplitButton_SelectFeature.setFocusable(false);
        //jSplitButton_SelectFeature.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        //jSplitButton_SelectFeature.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar_Base.add(jSplitButton_SelectFeature);

        //jButton_Measurement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Measurement.Image.png"))); // NOI18N
        jButton_Measurement.setIcon(new FlatSVGIcon("org/meteoinfo/icons/measurement.svg"));
        jButton_Measurement.setToolTipText(bundle.getString("FrmMain.jButton_Measurement.toolTipText")); // NOI18N
        jButton_Measurement.setFocusable(false);
        jButton_Measurement.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Measurement.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Measurement.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_MeasurementActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_Measurement);

        //jButton_LabelSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_LabelSet.Image.png"))); // NOI18N
        jButton_LabelSet.setIcon(new FlatSVGIcon("org/meteoinfo/icons/label.svg"));
        jButton_LabelSet.setToolTipText(bundle.getString("FrmMain.jButton_LabelSet.toolTipText")); // NOI18N
        jButton_LabelSet.setFocusable(false);
        jButton_LabelSet.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_LabelSet.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_LabelSet.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_LabelSetActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_LabelSet);
        jToolBar_Base.add(jSeparator3);

        //jButton_SavePicture.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Save_Image.png"))); // NOI18N
        jButton_SavePicture.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/figure-output.svg"));
        jButton_SavePicture.setToolTipText(bundle.getString("FrmMain.jButton_SavePicture.toolTipText")); // NOI18N
        jButton_SavePicture.setFocusable(false);
        jButton_SavePicture.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SavePicture.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SavePicture.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SavePictureActionPerformed(evt);
            }
        });
        jToolBar_Base.add(jButton_SavePicture);

        //Graphic tool bar
        jToolBar_Graphic.setFloatable(true);
        jToolBar_Graphic.setRollover(true);
        //jToolBar_Graphic.add(jSeparator4);

        //jButton_NewLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewLabel.Image.png"))); // NOI18N
        jButton_NewLabel.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-text.svg"));
        jButton_NewLabel.setToolTipText(bundle.getString("FrmMain.jButton_NewLabel.toolTipText")); // NOI18N
        jButton_NewLabel.setFocusable(false);
        jButton_NewLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewLabel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewLabelActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewLabel);

        //jButton_NewPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewPoint.Image.png"))); // NOI18N
        jButton_NewPoint.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-point.svg"));
        jButton_NewPoint.setToolTipText(bundle.getString("FrmMain.jButton_NewPoint.toolTipText")); // NOI18N
        jButton_NewPoint.setFocusable(false);
        jButton_NewPoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewPoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewPoint.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewPointActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewPoint);

        //jButton_NewPolyline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewPolyline.Image.png"))); // NOI18N
        jButton_NewPolyline.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-line.svg"));
        jButton_NewPolyline.setToolTipText(bundle.getString("FrmMain.jButton_NewPolyline.toolTipText")); // NOI18N
        jButton_NewPolyline.setFocusable(false);
        jButton_NewPolyline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewPolyline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewPolyline.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewPolylineActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewPolyline);

        //jButton_NewFreehand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewFreehand.Image.png"))); // NOI18N
        jButton_NewFreehand.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-line-hand.svg"));
        jButton_NewFreehand.setToolTipText(bundle.getString("FrmMain.jButton_NewFreehand.toolTipText")); // NOI18N
        jButton_NewFreehand.setFocusable(false);
        jButton_NewFreehand.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewFreehand.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewFreehand.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewFreehandActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewFreehand);

        //jButton_NewCurve.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewCurve.Image.png"))); // NOI18N
        jButton_NewCurve.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-curve.svg"));
        jButton_NewCurve.setToolTipText(bundle.getString("FrmMain.jButton_NewCurve.toolTipText")); // NOI18N
        jButton_NewCurve.setFocusable(false);
        jButton_NewCurve.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewCurve.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewCurve.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewCurveActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewCurve);

        //jButton_NewPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewPolygon.Image.png"))); // NOI18N
        jButton_NewPolygon.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-polygon.svg"));
        jButton_NewPolygon.setToolTipText(bundle.getString("FrmMain.jButton_NewPolygon.toolTipText")); // NOI18N
        jButton_NewPolygon.setFocusable(false);
        jButton_NewPolygon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewPolygon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewPolygon.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewPolygonActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewPolygon);

        //jButton_NewCurvePolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewCurvePolygon.Image.png"))); // NOI18N
        jButton_NewCurvePolygon.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-polygon-curve.svg"));
        jButton_NewCurvePolygon.setToolTipText(bundle.getString("FrmMain.jButton_NewCurvePolygon.toolTipText")); // NOI18N
        jButton_NewCurvePolygon.setFocusable(false);
        jButton_NewCurvePolygon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewCurvePolygon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewCurvePolygon.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewCurvePolygonActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewCurvePolygon);

        //jButton_NewRectangle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewRectangle.Image.png"))); // NOI18N
        jButton_NewRectangle.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-rectangle.svg"));
        jButton_NewRectangle.setToolTipText(bundle.getString("FrmMain.jButton_NewRectangle.toolTipText")); // NOI18N
        jButton_NewRectangle.setFocusable(false);
        jButton_NewRectangle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewRectangle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewRectangle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewRectangleActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewRectangle);

        //jButton_NewCircle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewCircle.Image.png"))); // NOI18N
        jButton_NewCircle.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-circle.svg"));
        jButton_NewCircle.setToolTipText(bundle.getString("FrmMain.jButton_NewCircle.toolTipText")); // NOI18N
        jButton_NewCircle.setFocusable(false);
        jButton_NewCircle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewCircle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewCircle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewCircleActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewCircle);

        //jButton_NewEllipse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewEllipse.Image.png"))); // NOI18N
        jButton_NewEllipse.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-ellipse.svg"));
        jButton_NewEllipse.setToolTipText(bundle.getString("FrmMain.jButton_NewEllipse.toolTipText")); // NOI18N
        jButton_NewEllipse.setFocusable(false);
        jButton_NewEllipse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewEllipse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewEllipse.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewEllipseActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_NewEllipse);

        //jButton_EditVertices.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_EditVertices.Image.png"))); // NOI18N
        jButton_EditVertices.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/edit-vertices.svg"));
        jButton_EditVertices.setToolTipText(bundle.getString("FrmMain.jButton_EditVertices.toolTipText")); // NOI18N
        jButton_EditVertices.setFocusable(false);
        jButton_EditVertices.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_EditVertices.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_EditVertices.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EditVerticesActionPerformed(evt);
            }
        });
        jToolBar_Graphic.add(jButton_EditVertices);

        //Layout tool bar
        jToolBar_Layout.setFloatable(true);
        jToolBar_Layout.setRollover(true);
        //jToolBar_Layout.add(jSeparator15);

        //jButton_PageSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/page_portrait.png"))); // NOI18N
        jButton_PageSet.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/page-setting.svg"));
        jButton_PageSet.setToolTipText(bundle.getString("FrmMain.jButton_PageSet.toolTipText")); // NOI18N
        jButton_PageSet.setFocusable(false);
        jButton_PageSet.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_PageSet.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_PageSet.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PageSetActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jButton_PageSet);

        //jButton_PageZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_PageZoomIn.Image.png"))); // NOI18N
        jButton_PageZoomIn.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/page-zoom-in.svg"));
        jButton_PageZoomIn.setToolTipText(bundle.getString("FrmMain.jButton_PageZoomIn.toolTipText")); // NOI18N
        jButton_PageZoomIn.setFocusable(false);
        jButton_PageZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_PageZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_PageZoomIn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PageZoomInActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jButton_PageZoomIn);

        //jButton_PageZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_PageZoomOut.Image.png"))); // NOI18N
        jButton_PageZoomOut.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/page-zoom-out.svg"));
        jButton_PageZoomOut.setToolTipText(bundle.getString("FrmMain.jButton_PageZoomOut.toolTipText")); // NOI18N
        jButton_PageZoomOut.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PageZoomOutActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jButton_PageZoomOut);

        //jButton_FitToScreen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ZoomFullMap.png"))); // NOI18N
        jButton_FitToScreen.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/page-zoom-window.svg"));
        jButton_FitToScreen.setToolTipText(bundle.getString("FrmMain.jButton_FitToScreen.toolTipText")); // NOI18N
        jButton_FitToScreen.setFocusable(false);
        jButton_FitToScreen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_FitToScreen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_FitToScreen.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_FitToScreenActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jButton_FitToScreen);

        jComboBox_PageZoom.setEditable(true);
        jComboBox_PageZoom.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        jComboBox_PageZoom.setMinimumSize(new java.awt.Dimension(60, 24));
        jComboBox_PageZoom.setPreferredSize(new java.awt.Dimension(80, 24));
        jComboBox_PageZoom.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_PageZoomActionPerformed(evt);
            }
        });
        jToolBar_Layout.add(jComboBox_PageZoom);

        //Edit tool bar
        jToolBar_Edit.setFloatable(true);
        jToolBar_Edit.setRollover(true);

        //jButton_EditStartOrEnd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit_16.png"))); // NOI18N
        jButton_EditStartOrEnd.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/edit-status.svg"));
        jButton_EditStartOrEnd.setToolTipText(bundle.getString("FrmMain.jButton_EditStartOrEnd.toolTipText")); // NOI18N
        jButton_EditStartOrEnd.setFocusable(false);
        jButton_EditStartOrEnd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_EditStartOrEnd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_EditStartOrEnd.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EditStartOrEndActionPerformed(evt);
            }
        });
        jToolBar_Edit.add(jButton_EditStartOrEnd);

        //jButton_EditSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_16.png"))); // NOI18N
        jButton_EditSave.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-save.svg"));
        jButton_EditSave.setToolTipText(bundle.getString("FrmMain.jButton_EditSave.toolTipText")); // NOI18N
        jButton_EditSave.setFocusable(false);
        jButton_EditSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_EditSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_EditSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EditSaveActionPerformed(evt);
            }
        });
        jButton_EditSave.setEnabled(false);
        jToolBar_Edit.add(jButton_EditSave);
        jToolBar_Edit.add(new javax.swing.JToolBar.Separator());

        //jButton_EditTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/location_arrow.png"))); // NOI18N
        jButton_EditTool.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/edit-tool.svg"));
        jButton_EditTool.setToolTipText(bundle.getString("FrmMain.jButton_EditTool.toolTipText")); // NOI18N
        jButton_EditTool.setFocusable(false);
        jButton_EditTool.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_EditTool.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_EditTool.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EditToolActionPerformed(evt);
            }
        });
        jButton_EditTool.setEnabled(false);
        jToolBar_Edit.add(jButton_EditTool);

        //jButton_EditNewFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new_document_16.png"))); // NOI18N
        jButton_EditNewFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/edit-add-feature.svg"));
        jButton_EditNewFeature.setToolTipText(bundle.getString("FrmMain.jButton_EditNewFeature.toolTipText")); // NOI18N
        jButton_EditNewFeature.setFocusable(false);
        jButton_EditNewFeature.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_EditNewFeature.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_EditNewFeature.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EditNewFeatureActionPerformed(evt);
            }
        });
        jButton_EditNewFeature.setEnabled(false);
        jToolBar_Edit.add(jButton_EditNewFeature);

        //jButton_EditRemoveFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_RemoveDataLayes.Image.png"))); // NOI18N
        jButton_EditRemoveFeature.setIcon(new FlatSVGIcon("org/meteoinfo/icons/delete.svg"));
        jButton_EditRemoveFeature.setToolTipText(bundle.getString("FrmMain.jButton_EditRemoveFeature.toolTipText")); // NOI18N
        jButton_EditRemoveFeature.setFocusable(false);
        jButton_EditRemoveFeature.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_EditRemoveFeature.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_EditRemoveFeature.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EditRemoveFeatureActionPerformed(evt);
            }
        });
        jButton_EditRemoveFeature.setEnabled(false);
        jToolBar_Edit.add(jButton_EditRemoveFeature);

        //jButton_EditFeatureVertices.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_EditVertices.Image.png"))); // NOI18N
        jButton_EditFeatureVertices.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/edit-vertices.svg"));
        jButton_EditFeatureVertices.setToolTipText(bundle.getString("FrmMain.jButton_EditFeatureVertices.toolTipText")); // NOI18N
        jButton_EditFeatureVertices.setFocusable(false);
        jButton_EditFeatureVertices.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_EditFeatureVertices.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_EditFeatureVertices.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EditFeatureVerticesActionPerformed(evt);
            }
        });
        jButton_EditFeatureVertices.setEnabled(false);
        jToolBar_Edit.add(jButton_EditFeatureVertices);

        //Add tool bars in the panel
        jPanel_MainToolBar.setLayout(new WrappingLayout(WrappingLayout.LEFT, 1, 1));
        jPanel_MainToolBar.add(jToolBar_Base);
        jPanel_MainToolBar.add(jToolBar_Graphic);
        jPanel_MainToolBar.add(jToolBar_Layout);
        jPanel_MainToolBar.add(jToolBar_Edit);

        //Split panel
        //jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(180);

        jTabbedPane_Main.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane_MainStateChanged(evt);
            }
        });

        _mapView.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                _mapViewComponentResized(evt);
            }
        });

        javax.swing.GroupLayout _mapViewLayout = new javax.swing.GroupLayout(_mapView);
        _mapView.setLayout(_mapViewLayout);
        _mapViewLayout.setHorizontalGroup(
                _mapViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 637, Short.MAX_VALUE));
        _mapViewLayout.setVerticalGroup(
                _mapViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 458, Short.MAX_VALUE));

        javax.swing.GroupLayout jPanel_MapTabLayout = new javax.swing.GroupLayout(jPanel_MapTab);
        jPanel_MapTab.setLayout(jPanel_MapTabLayout);
        jPanel_MapTabLayout.setHorizontalGroup(
                jPanel_MapTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(_mapView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        jPanel_MapTabLayout.setVerticalGroup(
                jPanel_MapTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(_mapView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        jTabbedPane_Main.addTab(bundle.getString("FrmMain.jPanel_MapTab.TabConstraints.tabTitle"), jPanel_MapTab); // NOI18N

        javax.swing.GroupLayout _mapLayoutLayout = new javax.swing.GroupLayout(_mapLayout);
        _mapLayout.setLayout(_mapLayoutLayout);
        _mapLayoutLayout.setHorizontalGroup(
                _mapLayoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 637, Short.MAX_VALUE));
        _mapLayoutLayout.setVerticalGroup(
                _mapLayoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 458, Short.MAX_VALUE));

        javax.swing.GroupLayout jPanel_LayoutTabLayout = new javax.swing.GroupLayout(jPanel_LayoutTab);
        jPanel_LayoutTab.setLayout(jPanel_LayoutTabLayout);
        jPanel_LayoutTabLayout.setHorizontalGroup(
                jPanel_LayoutTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(_mapLayout, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        jPanel_LayoutTabLayout.setVerticalGroup(
                jPanel_LayoutTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(_mapLayout, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        jTabbedPane_Main.addTab(bundle.getString("FrmMain.jPanel_LayoutTab.TabConstraints.tabTitle"), jPanel_LayoutTab); // NOI18N

        jSplitPane1.setRightComponent(jTabbedPane_Main);
        jSplitPane1.setLeftComponent(_mapDocument);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING));
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane1));

        //Status panel
        jLabel_Status.setText(bundle.getString("FrmMain.jLabel_Status.text")); // NOI18N
        jLabel_Coordinate.setText(bundle.getString("FrmMain.jLabel_Coordinate.text")); // NOI18N
        jLabel_ProgressBar.setVisible(false);
        jProgressBar_Main.setVisible(false);
        javax.swing.GroupLayout jPanel_StatusLayout = new javax.swing.GroupLayout(jPanel_Status);
        jPanel_Status.setLayout(jPanel_StatusLayout);
        jPanel_StatusLayout.setHorizontalGroup(
                jPanel_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel_StatusLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel_Status, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel_Coordinate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jProgressBar_Main, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel_ProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel_StatusLayout.setVerticalGroup(
                jPanel_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel_Status, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel_Coordinate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jProgressBar_Main, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel_ProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)));

        //Main menu bar
        //jMenuBar_Main.setFont(new java.awt.Font("微软雅黑", 0, 14)); // NOI18N

        //Project menu
        jMenu_Project.setText(bundle.getString("FrmMain.jMenu_Project.text")); // NOI18N
        jMenu_Project.setMnemonic(KeyEvent.VK_P);

        jMenuItem_Open.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_Open.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Folder_1_16x16x8.png"))); // NOI18N
        jMenuItem_Open.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-open.svg"));
        jMenuItem_Open.setText(bundle.getString("FrmMain.jMenuItem_Open.text")); // NOI18N
        jMenuItem_Open.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OpenActionPerformed(evt);
            }
        });
        jMenu_Project.add(jMenuItem_Open);

        jMenuItem_Save.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK));
        //jMenuItem_Save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Disk_1_16x16x8.png"))); // NOI18N
        jMenuItem_Save.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-save.svg"));
        jMenuItem_Save.setText(bundle.getString("FrmMain.jMenuItem_Save.text")); // NOI18N
        jMenuItem_Save.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SaveActionPerformed(evt);
            }
        });
        jMenu_Project.add(jMenuItem_Save);

        jMenuItem_SaveAs.setText(bundle.getString("FrmMain.jMenuItem_SaveAs.text")); // NOI18N
        jMenuItem_SaveAs.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SaveAsActionPerformed(evt);
            }
        });
        jMenu_Project.add(jMenuItem_SaveAs);

        jMenuBar_Main.add(jMenu_Project);

        //Edit menu
        jMenu_Edit.setText(bundle.getString("FrmMain.jMenu_Edit.text")); // NOI18N
        jMenu_Edit.setMnemonic(KeyEvent.VK_E);

        //jMenuItem_Undo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Undo.Image.png"))); // NOI18N
        jMenuItem_Undo.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/undo.svg"));
        jMenuItem_Undo.setText(bundle.getString("FrmMain.jMenuItem_Undo.text")); // NOI18N
        jMenuItem_Undo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_Undo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_UndoActionPerformed(evt);
            }
        });
        jMenuItem_Undo.setEnabled(false);
        jMenu_Edit.add(jMenuItem_Undo);

        //jMenuItem_Redo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Redo.Image.png"))); // NOI18N
        jMenuItem_Redo.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/redo.svg"));
        jMenuItem_Redo.setText(bundle.getString("FrmMain.jMenuItem_Redo.text")); // NOI18N
        jMenuItem_Redo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_Redo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_RedoActionPerformed(evt);
            }
        });
        jMenuItem_Redo.setEnabled(false);
        jMenu_Edit.add(jMenuItem_Redo);
        jMenu_Edit.add(new javax.swing.JPopupMenu.Separator());

        jMenu_Edit.setText(bundle.getString("FrmMain.jMenu_Edit.text")); // NOI18N
        jMenu_Edit.setMnemonic(KeyEvent.VK_E);

        //jMenuItem_Cut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSMI_EditCut.Image.png"))); // NOI18N
        jMenuItem_Cut.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/menu-cut.svg"));
        jMenuItem_Cut.setText(bundle.getString("FrmMain.jMenuItem_Cut.text")); // NOI18N
        jMenuItem_Cut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_Cut.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CutActionPerformed(evt);
            }
        });
        jMenuItem_Cut.setEnabled(false);
        jMenu_Edit.add(jMenuItem_Cut);

        //jMenuItem_Copy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/menuEditCopy.Image.png"))); // NOI18N
        jMenuItem_Copy.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/copy.svg"));
        jMenuItem_Copy.setText(bundle.getString("FrmMain.jMenuItem_Copy.text")); // NOI18N
        jMenuItem_Copy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_Copy.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CopyActionPerformed(evt);
            }
        });
        jMenuItem_Copy.setEnabled(false);
        jMenu_Edit.add(jMenuItem_Copy);

        //jMenuItem_Paste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pasteToolStripButton.Image.png"))); // NOI18N
        jMenuItem_Paste.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/menu-paste.svg"));
        jMenuItem_Paste.setText(bundle.getString("FrmMain.jMenuItem_Paste.text")); // NOI18N
        jMenuItem_Paste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_Paste.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_PasteActionPerformed(evt);
            }
        });
        jMenuItem_Paste.setEnabled(false);
        jMenu_Edit.add(jMenuItem_Paste);
        jMenu_Edit.add(new javax.swing.JPopupMenu.Separator());

        jMenuItem_NewLayer.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/new-layer.svg"));
        jMenuItem_NewLayer.setText(bundle.getString("FrmMain.jMenuItem_NewLayer.text")); // NOI18N
        jMenuItem_NewLayer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_NewLayerActionPerformed(evt);
            }
        });
        jMenu_Edit.add(jMenuItem_NewLayer);
        jMenu_Edit.add(new javax.swing.JPopupMenu.Separator());

        //jMenuItem_AddRing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ring_add.png"))); // NOI18N
        jMenuItem_AddRing.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/add-ring.svg"));
        jMenuItem_AddRing.setText(bundle.getString("FrmMain.jMenuItem_AddRing.text")); // NOI18N
        jMenuItem_AddRing.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AddRingActionPerformed(evt);
            }
        });
        jMenuItem_AddRing.setEnabled(false);
        jMenu_Edit.add(jMenuItem_AddRing);

        //jMenuItem_FillRing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ring.png"))); // NOI18N
        jMenuItem_FillRing.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/fill-ring.svg"));
        jMenuItem_FillRing.setText(bundle.getString("FrmMain.jMenuItem_FillRing.text")); // NOI18N
        jMenuItem_FillRing.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_FillRingActionPerformed(evt);
            }
        });
        jMenuItem_FillRing.setEnabled(false);
        jMenu_Edit.add(jMenuItem_FillRing);

        //jMenuItem_DeleteRing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ring_delete.png"))); // NOI18N
        jMenuItem_DeleteRing.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/delete-ring.svg"));
        jMenuItem_DeleteRing.setText(bundle.getString("FrmMain.jMenuItem_DeleteRing.text")); // NOI18N
        jMenuItem_DeleteRing.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_DeleteRingActionPerformed(evt);
            }
        });
        jMenuItem_DeleteRing.setEnabled(false);
        jMenu_Edit.add(jMenuItem_DeleteRing);
        
        //jMenuItem_ReformFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reform_edit.png"))); // NOI18N
        jMenuItem_ReformFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/reshape-feature.svg"));
        jMenuItem_ReformFeature.setText(bundle.getString("FrmMain.jMenuItem_ReformFeature.text")); // NOI18N
        jMenuItem_ReformFeature.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ReformFeatureActionPerformed(evt);
            }
        });
        jMenuItem_ReformFeature.setEnabled(false);
        jMenu_Edit.add(jMenuItem_ReformFeature);

        //jMenuItem_SplitFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/split.png"))); // NOI18N
        jMenuItem_SplitFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/split-feature.svg"));
        jMenuItem_SplitFeature.setText(bundle.getString("FrmMain.jMenuItem_SplitFeature.text")); // NOI18N
        jMenuItem_SplitFeature.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SplitFeatureActionPerformed(evt);
            }
        });
        jMenuItem_SplitFeature.setEnabled(false);
        jMenu_Edit.add(jMenuItem_SplitFeature);

        //jMenuItem_MergeFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/merge.png"))); // NOI18N
        jMenuItem_MergeFeature.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/merge-feature.svg"));
        jMenuItem_MergeFeature.setText(bundle.getString("FrmMain.jMenuItem_MergeFeature.text")); // NOI18N
        jMenuItem_MergeFeature.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_MergeFeatureActionPerformed(evt);
            }
        });
        jMenuItem_MergeFeature.setEnabled(false);
        jMenu_Edit.add(jMenuItem_MergeFeature);

        jMenuBar_Main.add(jMenu_Edit);

        //View menu
        jMenu_View.setText(bundle.getString("FrmMain.jMenu_View.text")); // NOI18N
        jMenu_View.setMnemonic(KeyEvent.VK_V);

        //jMenuItem_Layers.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Layers.png"))); // NOI18N
        jMenuItem_Layers.setIcon(new FlatSVGIcon("org/meteoinfo/icons/layers.svg"));
        jMenuItem_Layers.setText(bundle.getString("FrmMain.jMenuItem_Layers.text")); // NOI18N
        jMenuItem_Layers.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_LayersActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_Layers);

        //jMenuItem_AttributeData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSMI_AttriData.Image.png"))); // NOI18N
        jMenuItem_AttributeData.setIcon(new FlatSVGIcon("org/meteoinfo/icons/table.svg"));
        jMenuItem_AttributeData.setText(bundle.getString("FrmMain.jMenuItem_AttributeData.text")); // NOI18N
        jMenuItem_AttributeData.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AttributeDataActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_AttributeData);
        jMenu_View.add(jSeparator5);

        jMenuItem_LayoutProperty.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/layout-setting.svg"));
        jMenuItem_LayoutProperty.setText(bundle.getString("FrmMain.jMenuItem_LayoutProperty.text")); // NOI18N
        jMenuItem_LayoutProperty.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_LayoutPropertyActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_LayoutProperty);
        jMenu_View.add(jSeparator6);

        jMenuItem_MapProperty.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/map-setting.svg"));
        jMenuItem_MapProperty.setText(bundle.getString("FrmMain.jMenuItem_MapProperty.text")); // NOI18N
        jMenuItem_MapProperty.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_MapPropertyActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_MapProperty);

        jMenuItem_MaskOut.setText(bundle.getString("FrmMain.jMenuItem_MaskOut.text")); // NOI18N
        jMenuItem_MaskOut.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_MaskOutActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_MaskOut);
        jMenu_View.add(jSeparator7);

        jMenuItem_Projection.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/lon-lat.svg"));
        jMenuItem_Projection.setText(bundle.getString("FrmMain.jMenuItem_Projection.text")); // NOI18N
        jMenuItem_Projection.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ProjectionActionPerformed(evt);
            }
        });
        jMenu_View.add(jMenuItem_Projection);

        jMenuBar_Main.add(jMenu_View);

        jMenu_Insert.setText(bundle.getString("FrmMain.jMenu_Insert.text")); // NOI18N
        jMenu_Insert.setMnemonic(KeyEvent.VK_V);

        jMenuItem_InsertMapFrame.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/add-layer.svg"));
        jMenuItem_InsertMapFrame.setText(bundle.getString("FrmMain.jMenuItem_InsertMapFrame.text")); // NOI18N
        jMenuItem_InsertMapFrame.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertMapFrameActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertMapFrame);
        jMenu_Insert.add(jSeparator10);

        jMenuItem_InsertTitle.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/title.svg"));
        jMenuItem_InsertTitle.setText(bundle.getString("FrmMain.jMenuItem_InsertTitle.text")); // NOI18N
        jMenuItem_InsertTitle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertTitleActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertTitle);

        jMenuItem_InsertText.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/text.svg"));
        jMenuItem_InsertText.setText(bundle.getString("FrmMain.jMenuItem_InsertText.text")); // NOI18N
        jMenuItem_InsertText.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertTextActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertText);
        jMenu_Insert.add(jSeparator8);

        jMenuItem_InsertLegend.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/legend.svg"));
        //jMenuItem_InsertLegend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSMI_InsertLegend.Image.png"))); // NOI18N
        jMenuItem_InsertLegend.setText(bundle.getString("FrmMain.jMenuItem_InsertLegend.text")); // NOI18N
        jMenuItem_InsertLegend.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertLegendActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertLegend);

        jMenuItem_InsertScaleBar.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/scale-bar.svg"));
        jMenuItem_InsertScaleBar.setText(bundle.getString("FrmMain.jMenuItem_InsertScaleBar.text")); // NOI18N
        jMenuItem_InsertScaleBar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertScaleBarActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertScaleBar);

        jMenuItem_InsertNorthArrow.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/north-arrow.svg"));
        jMenuItem_InsertNorthArrow.setText(bundle.getString("FrmMain.jMenuItem_InsertNorthArrow.text")); // NOI18N
        jMenuItem_InsertNorthArrow.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertNorthArrowActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertNorthArrow);
        jMenu_Insert.add(jSeparator9);

        jMenuItem_InsertWindArrow.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/wind-arrow.svg"));
        jMenuItem_InsertWindArrow.setText(bundle.getString("FrmMain.jMenuItem_InsertWindArrow.text")); // NOI18N
        jMenuItem_InsertWindArrow.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertWindArrowActionPerformed(evt);
            }
        });
        jMenu_Insert.add(jMenuItem_InsertWindArrow);

        jMenuBar_Main.add(jMenu_Insert);

        jMenu_Selection.setText(bundle.getString("FrmMain.jMenu_Selection.text")); // NOI18N
        jMenu_Selection.setMnemonic(KeyEvent.VK_S);

        jMenuItem_SelByAttr.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-attr.svg"));
        jMenuItem_SelByAttr.setText(bundle.getString("FrmMain.jMenuItem_SelByAttr.text")); // NOI18N
        jMenuItem_SelByAttr.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SelByAttrActionPerformed(evt);
            }
        });
        jMenu_Selection.add(jMenuItem_SelByAttr);

        jMenuItem_SelByLocation.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/select-location.svg"));
        jMenuItem_SelByLocation.setText(bundle.getString("FrmMain.jMenuItem_SelByLocation.text")); // NOI18N
        jMenuItem_SelByLocation.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SelByLocationActionPerformed(evt);
            }
        });
        jMenu_Selection.add(jMenuItem_SelByLocation);
        jMenu_Selection.add(jSeparator11);

        jMenuItem_ClearSelection.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/cancel.svg"));
        jMenuItem_ClearSelection.setText(bundle.getString("FrmMain.jMenuItem_ClearSelection.text")); // NOI18N
        jMenuItem_ClearSelection.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ClearSelectionActionPerformed(evt);
            }
        });
        jMenu_Selection.add(jMenuItem_ClearSelection);

        jMenuBar_Main.add(jMenu_Selection);
        
        //GeoProcessing menu
        jMenu_GeoProcessing.setText(bundle.getString("FrmMain.jMenu_GeoProcessing.text"));
        jMenu_GeoProcessing.setMnemonic(KeyEvent.VK_G);
        
        jMenuItem_Buffer.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/buffer.svg"));
        jMenuItem_Buffer.setText(bundle.getString("FrmMain.jMenuItem_Buffer.text")); // NOI18N
        jMenuItem_Buffer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_BufferActionPerformed(evt);
            }
        });
        jMenu_GeoProcessing.add(jMenuItem_Buffer);

        jMenuItem_Clipping.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/clip.svg"));
        jMenuItem_Clipping.setText(bundle.getString("FrmMain.jMenuItem_Clipping.text")); // NOI18N
        jMenuItem_Clipping.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ClippingActionPerformed(evt);
            }
        });
        jMenu_GeoProcessing.add(jMenuItem_Clipping);
        
        jMenuItem_Convexhull.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/convexhull.svg"));
        jMenuItem_Convexhull.setText(bundle.getString("FrmMain.jMenuItem_Convexhull.text")); // NOI18N
        jMenuItem_Convexhull.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ConvexhullActionPerformed(evt);
            }
        });
        jMenu_GeoProcessing.add(jMenuItem_Convexhull);
        
        jMenuItem_Intersection.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/intersection.svg"));
        jMenuItem_Intersection.setText(bundle.getString("FrmMain.jMenuItem_Intersection.text")); // NOI18N
        jMenuItem_Intersection.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_IntersectionActionPerformed(evt);
            }
        });
        jMenu_GeoProcessing.add(jMenuItem_Intersection);

        jMenuItem_Difference.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/difference.svg"));
        jMenuItem_Difference.setText(bundle.getString("FrmMain.jMenuItem_Difference.text")); // NOI18N
        jMenuItem_Difference.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_DifferenceActionPerformed(evt);
            }
        });
        jMenu_GeoProcessing.add(jMenuItem_Difference);

        jMenuItem_SymDifference.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/system-difference.svg"));
        jMenuItem_SymDifference.setText(bundle.getString("FrmMain.jMenuItem_SymDifference.text")); // NOI18N
        jMenuItem_SymDifference.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SymDifferenceActionPerformed(evt);
            }
        });
        jMenu_GeoProcessing.add(jMenuItem_SymDifference);
        
        jMenuBar_Main.add(jMenu_GeoProcessing);

        //Tools menu
        jMenu_Tools.setText(bundle.getString("FrmMain.jMenu_Tools.text")); // NOI18N
        jMenu_Tools.setMnemonic(KeyEvent.VK_T);

        //jMenuItem_Script.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snake.png"))); // NOI18N
        jMenuItem_Script.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/editor.svg"));
        jMenuItem_Script.setText(bundle.getString("FrmMain.jMenuItem_Script.text")); // NOI18N
        jMenuItem_Script.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ScriptActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_Script);

        /*//jMenuItem_ScriptConsole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/console.png"))); // NOI18N
        jMenuItem_ScriptConsole.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/console.svg"));
        jMenuItem_ScriptConsole.setText(bundle.getString("FrmMain.jMenuItem_ScriptConsole.text")); // NOI18N
        jMenuItem_ScriptConsole.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ScriptConsoleActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_ScriptConsole);*/
        jMenu_Tools.add(jSeparator16);

        jMenuItem_Options.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/tools.svg"));
        jMenuItem_Options.setText(bundle.getString("FrmMain.jMenuItem_Options.text")); // NOI18N
        jMenuItem_Options.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OptionsActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_Options);
        jMenu_Tools.add(jSeparator17);

        jMenuItem_OutputMapData.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/map-out.svg"));
        jMenuItem_OutputMapData.setText(bundle.getString("FrmMain.jMenuItem_OutputMapData.text")); // NOI18N
        jMenuItem_OutputMapData.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OutputMapDataActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_OutputMapData);

        jMenuItem_AddXYData.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/chart-points.svg"));
        jMenuItem_AddXYData.setText(bundle.getString("FrmMain.jMenuItem_AddXYData.text")); // NOI18N
        jMenuItem_AddXYData.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AddXYDataActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_AddXYData);        

        jMenuItem_Animator.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/gif.svg"));
        jMenuItem_Animator.setText(bundle.getString("FrmMain.jMenuItem_Animator.text")); // NOI18N
        jMenuItem_Animator.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AnimatorActionPerformed(evt);
            }
        });
        jMenu_Tools.add(jMenuItem_Animator);

        jMenu_NetCDFData.setText(bundle.getString("FrmMain.jMenu_NetCDFData.text"));
        jMenuItem_JoinNCFiles.setText(bundle.getString("FrmMain.jMenuItem_JoinNCFiles.text")); // NOI18N
        jMenuItem_JoinNCFiles.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_JoinNCFilesActionPerformed(evt);
            }
        });
        jMenu_NetCDFData.add(jMenuItem_JoinNCFiles);
        jMenu_Tools.add(jMenu_NetCDFData);

        jMenuBar_Main.add(jMenu_Tools);

        jMenu_Plugin.setText(bundle.getString("FrmMain.jMenu_Plugin.text")); // NOI18N
        jMenu_Plugin.setMnemonic(KeyEvent.VK_L);

        jMenuItem_PluginManager.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/plugin-setting.svg"));
        //jMenuItem_PluginManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plugin_edit_green.png"))); // NOI18N
        jMenuItem_PluginManager.setText(bundle.getString("FrmMain.jMenuItem_PluginManager.text")); // NOI18N
        jMenuItem_PluginManager.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_PluginManagerActionPerformed(evt);
            }
        });
        jMenu_Plugin.add(jMenuItem_PluginManager);
        jMenu_Plugin.add(jSeparator18);

        jMenuBar_Main.add(jMenu_Plugin);

        jMenu_Help.setText(bundle.getString("FrmMain.jMenu_Help.text")); // NOI18N
        jMenu_Help.setMnemonic(KeyEvent.VK_H);

        jMenuItem_About.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/information.svg"));
        jMenuItem_About.setText(bundle.getString("FrmMain.jMenuItem_About.text")); // NOI18N
        jMenuItem_About.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AboutActionPerformed(evt);
            }
        });
        jMenu_Help.add(jMenuItem_About);
        jMenu_Help.add(jSeparator12);

        jMenuItem_Help.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/help.svg"));
        jMenuItem_Help.setText(bundle.getString("FrmMain.jMenuItem_Help.text")); // NOI18N
        //jMenuItem_Help.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help.png")));
        jMenuItem_Help.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_HelpActionPerformed(evt);
            }
        });
        jMenu_Help.add(jMenuItem_Help);

        jMenuBar_Main.add(jMenu_Help);

        setJMenuBar(jMenuBar_Main);

        //Add tool bar panel
        getContentPane().add(jPanel_MainToolBar, BorderLayout.NORTH);
        getContentPane().add(jPanel4, BorderLayout.CENTER);
        getContentPane().add(jPanel_Status, BorderLayout.SOUTH);
        pack();
    }

    private void loadForm() {
        _isLoading = true;

        //Set layout zoom combobox
        this.jComboBox_PageZoom.removeAllItems();
        String[] zooms = new String[]{"20%", "50%", "75%", "100%", "150%", "200%", "300%"};
        for (String zoom : zooms) {
            this.jComboBox_PageZoom.addItem(zoom);
        }
        this.jComboBox_PageZoom.setSelectedItem(String.valueOf((int) (_mapDocument.getMapLayout().getZoom() * 100)) + "%");
        //this._loadedPluginIcon = new ImageIcon(ImageIO.read(this.getClass().getResource("/images/plugin_green.png")));
        //this._unloadedPluginIcon = new ImageIcon(ImageIO.read(this.getClass().getResource("/images/plugin_unsel.png")));
        this._loadedPluginIcon = new FlatSVGIcon("org/meteoinfo/desktop/icons/plugin-loaded.svg");
        this._unloadedPluginIcon = new FlatSVGIcon("org/meteoinfo/desktop/icons/plugin.svg");

        this.loadDefaultPojectFile();
        //this.loadConfigureFile();
        //this._mapDocument.setFont(this._options.getLegendFont());
        this.setLocation(this._options.getMainFormLocation());
        this.setSize(this._options.getMainFormSize());
        try {
            this._plugins.loadConfigFile(this._plugins.getPluginConfigFile());
            this.loadPlugins(this._plugins);
        } catch (MalformedURLException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        _mapView = _mapDocument.getActiveMapFrame().getMapView();
        setMapView();
        //_mapView.setIsLayoutMap(false);
        //_mapView.zoomToExtent(_mapView.getViewExtent());

        this.jToolBar_Layout.setEnabled(false);
        for (Component c : this.jToolBar_Layout.getComponents()) {
            c.setEnabled(false);
        }
        this.jMenuItem_LayoutProperty.setEnabled(false);
        this.jMenuItem_InsertLegend.setEnabled(false);
        this.jMenuItem_InsertTitle.setEnabled(false);
        this.jMenuItem_InsertText.setEnabled(false);
        this.jMenuItem_InsertNorthArrow.setEnabled(false);
        this.jMenuItem_InsertScaleBar.setEnabled(false);
        this.jMenuItem_InsertWindArrow.setEnabled(false);

        _isLoading = false;
    }

    private void setMapView() {
        //Add map view 
        _mapView.setLockViewUpdate(true);
        this.jPanel_MapTab.removeAll();
        this.jPanel_MapTab.add(_mapView, BorderLayout.CENTER);
        _mapView.setLockViewUpdate(false);
        if (_currentTool != null) {
            _currentTool.doClick();
        }

        _mapView.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mapView_MouseMoved(e);
            }
        });
        _mapView.addGraphicSelectedListener(new IGraphicSelectedListener() {
            @Override
            public void graphicSelectedEvent(GraphicSelectedEvent event) {
                if (_mapView.getSelectedGraphics().size() > 0) {
                    switch (_mapView.getSelectedGraphics().get(0).getShape().getShapeType()) {
                        case Polyline:
                        case CurveLine:
                        case Polygon:
                        case CurvePolygon:
                            jButton_EditVertices.setEnabled(true);
                            break;
                        default:
                            jButton_EditVertices.setEnabled(false);
                            break;
                    }
                } else {
                    jButton_EditVertices.setEnabled(false);
                }
            }
        });
        _mapView.addShapeSelectedListener(new IShapeSelectedListener() {
            @Override
            public void shapeSelectedEvent(ShapeSelectedEvent event) {
                MapLayer selLayer = _mapView.getSelectedLayer();
                if (selLayer != null) {
                    if (selLayer.getLayerType() == LayerTypes.VectorLayer) {
                        if (((VectorLayer) selLayer).isEditing()) {
                            if (((VectorLayer) selLayer).hasSelectedShapes()) {
                                jMenuItem_Cut.setEnabled(true);
                                jMenuItem_Copy.setEnabled(true);
                                jMenuItem_Paste.setEnabled(true);
                                jMenuItem_MergeFeature.setEnabled(true);
                                jButton_EditRemoveFeature.setEnabled(true);
                                jButton_EditFeatureVertices.setEnabled(true);
                            } else {
                                jMenuItem_Cut.setEnabled(false);
                                jMenuItem_Copy.setEnabled(false);
                                jMenuItem_MergeFeature.setEnabled(false);
                                jButton_EditRemoveFeature.setEnabled(false);
                                jButton_EditFeatureVertices.setEnabled(false);
                            }
                            jButton_EditFeatureVertices.setSelected(false);
                        }
                    }
                }
            }
        });
        _mapView.addUndoEditListener(new IUndoEditListener() {
            @Override
            public void undoEditEvent(UndoEditEvent event, UndoableEdit undoEdit) {
                if (undoEdit.getClass().equals(MapViewUndoRedo.ZoomEdit.class)) {
                    zoomUndoManager.addEdit(undoEdit);
                    refreshZoomUndoRedo();
                } else if (undoEdit instanceof FeatureUndoableEdit) {
                    refreshUndoRedo();
                } else {
                    undoManager.addEdit(undoEdit);
                    refreshUndoRedo();
                }
            }
        });

        _mapView.setFocusable(true);
        _mapView.requestFocusInWindow();

//            tabControl1.TabPages[0].Controls.Clear();
//            tabControl1.TabPages[0].Controls.Add(_mapView);
//            _mapView.Dock = DockStyle.Fill;
//            _mapView.MouseMove += new MouseEventHandler(this.MapView_MouseMove);
//            _mapView.MouseDown += new MouseEventHandler(this.MapView_MouseDown);
//            _mapView.GraphicSeleted += new EventHandler(this.MapView_GraphicSelected);
    }

    private void refreshUndoRedo() {
        this.jMenuItem_Undo.setEnabled(currentUndoManager.canUndo());
        this.jMenuItem_Redo.setEnabled(currentUndoManager.canRedo());
        if (this.jButton_EditStartOrEnd.isSelected()) {
            this.jButton_EditSave.setEnabled(currentUndoManager.canUndo());
        }
    }

    private void refreshZoomUndoRedo() {
        this.jButton_ZoomUndo.setEnabled(zoomUndoManager.canUndo());
        this.jButton_ZoomRedo.setEnabled(zoomUndoManager.canRedo());
    }
    // </editor-fold>
    // <editor-fold desc="Events">

    private void frameResized(ComponentEvent evt) {
        validate();
    }

    private void mapView_MouseMoved(MouseEvent e) {
        double pXY[] = _mapDocument.getActiveMapFrame().getMapView().screenToProj((double) e.getX(), (double) e.getY());
        double projX = pXY[0];
        double projY = pXY[1];
        if (_mapDocument.getActiveMapFrame().getMapView().getProjection().isLonLatMap()) {
            this.jLabel_Coordinate.setText("Lon: " + String.format("%1$.2f", projX) + "; Lat: " + String.format("%1$.2f", projY));
        } else {
            this.jLabel_Coordinate.setText("X: " + String.format("%1$.1f", projX) + "; Y: " + String.format("%1$.1f", projY));
            String theText = this.jLabel_Coordinate.getText();
            if (_mapDocument.getActiveMapFrame().getMapView().getProjection().getProjInfo().getProjectionName() == ProjectionNames.Robinson) {
                return;
            }

            ProjectionInfo toProj = KnownCoordinateSystems.geographic.world.WGS1984;
            ProjectionInfo fromProj = _mapDocument.getActiveMapFrame().getMapView().getProjection().getProjInfo();
            double[][] points = new double[1][];
            points[0] = new double[]{projX, projY};
            //double[] Z = new double[1];
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                this.jLabel_Coordinate.setText(theText + " (Lon: " + String.format("%1$.2f", points[0][0]) + "; Lat: "
                        + String.format("%1$.2f", points[0][1]) + ")");
            } catch (Exception ex) {
                //this.TSSL_Coord.Text = "X: " + ProjX.ToString("0.0") + "; Y: " + ProjY.ToString("0.0"); 
            }
        }
    }

    private void layout_MouseMoved(MouseEvent e) {
        Point pageP = _mapDocument.getMapLayout().screenToPage(e.getX(), e.getY());
        for (MapFrame mf : _mapDocument.getMapFrames()) {
            Rectangle rect = mf.getLayoutBounds();
            if (MIMath.pointInRectangle(pageP, rect)) {
                double pXY[] = mf.getMapView().screenToProj((double) (pageP.x - rect.x), (double) (pageP.y - rect.y), _mapDocument.getMapLayout().getZoom());
                double projX = pXY[0];
                double projY = pXY[1];
                if (mf.getMapView().getProjection().isLonLatMap()) {
                    this.jLabel_Coordinate.setText("Lon: " + String.format("%1$.2f", projX) + "; Lat: " + String.format("%1$.2f", projY));
                } else {
                    this.jLabel_Coordinate.setText("X: " + String.format("%1$.1f", projX) + "; Y: " + String.format("%1$.1f", projY));
                    String theText = this.jLabel_Coordinate.getText();
                    if (mf.getMapView().getProjection().getProjInfo().getProjectionName() == ProjectionNames.Robinson) {
                        return;
                    }

                    ProjectionInfo toProj = KnownCoordinateSystems.geographic.world.WGS1984;
                    ProjectionInfo fromProj = mf.getMapView().getProjection().getProjInfo();
                    double[][] points = new double[1][];
                    points[0] = new double[]{projX, projY};
                    try {
                        Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                        this.jLabel_Coordinate.setText(theText + " (Lon: " + String.format("%1$.2f", points[0][0]) + "; Lat: "
                                + String.format("%1$.2f", points[0][1]) + ")");
                    } catch (Exception ex) {
                        //this.TSSL_Coord.Text = "X: " + ProjX.ToString("0.0") + "; Y: " + ProjY.ToString("0.0"); 
                    }
                }

                break;
            }
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get and set Methods">
    /**
     * Get application startup path
     *
     * @return Applicatin startup path
     */
    public String getStartupPath() {
        return this._startupPath;
    }

    /**
     * Get MapView object in the active map frame
     *
     * @return MapView object
     */
    @Override
    public MapView getMapView() {
        return this._mapDocument.getActiveMapFrame().getMapView();
    }

    /**
     * Get map layout
     *
     * @return Map layout
     */
    public MapLayout getMapLyout() {
        return this._mapLayout;
    }

    /**
     * Get map document (LayersLegend)
     *
     * @return The map document
     */
    @Override
    public LayersLegend getMapDocument() {
        return this._mapDocument;
    }

    /**
     * Get main menu bar
     *
     * @return Main menu bar
     */
    @Override
    public JMenuBar getMainMenuBar() {
        return this.jMenuBar_Main;
    }

    /**
     * Get plugin menu
     *
     * @return Plugin menu
     */
    @Override
    public JMenu getPluginMenu() {
        return this.jMenu_Plugin;
    }

    /**
     * Get tool bar panel
     *
     * @return Tool bar panel
     */
    @Override
    public JPanel getToolBarPanel() {
        return this.jPanel_MainToolBar;
    }

    /**
     * Get main progress bar
     *
     * @return The main progress bar
     */
    @Override
    public JProgressBar getProgressBar() {
        return this.jProgressBar_Main;
    }

    /**
     * Get progress bar label
     *
     * @return The progress bar label
     */
    @Override
    public JLabel getProgressBarLabel() {
        return this.jLabel_ProgressBar;
    }

    /**
     * Get jTabbedPane_Main
     *
     * @return jTabbedPane_Main
     */
    public JTabbedPane getMainTab() {
        return this.jTabbedPane_Main;
    }

    /**
     * Get meteo data form
     *
     * @return The meteo data form
     */
    public FrmMeteoData getMeteoDataset() {
        return this._frmMeteoData;
    }

    /**
     * Get options
     *
     * @return Options
     */
    public Options getOptions() {
        return this._options;
    }

    /**
     * Get legend font
     *
     * @return Legend font
     */
    public Font getLegendFont() {
        return _mapDocument.getFont();
    }

    /**
     * Set legend font
     *
     * @param font Legend font
     */
    public void setLegendFont(Font font) {
        _mapDocument.setFont(font);
        _options.setLegendFont(font);
        _mapDocument.paintGraphics();
    }

    /**
     * Get plugins
     *
     * @return Plugins
     */
    public PluginCollection getPlugins() {
        return _plugins;
    }

    /**
     * Get current tool
     *
     * @return The current tool
     */
    @Override
    public AbstractButton getCurrentTool() {
        return _currentTool;
    }

//    /**
//     * Get current data folder
//     * 
//     * @return Current data folder
//     */
//    public String getCurrentDataFolder(){
//        return _currentDataFolder;
//    }
//    
//    /**
//     * Set current data folder
//     * 
//     * @param folder Current data folder
//     */
//    public void setCurrentDataFolder(String folder){
//        _currentDataFolder = folder;
//    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Project">
    public final void loadDefaultPojectFile() {
        //Open default project file            
//        File directory = new File(".");        
//        String fn = null;
//        try {
//            fn = directory.getCanonicalPath();
//        } catch (IOException ex) {
//            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        fn = fn + File.separator + "default.mip";
        String fn = this._startupPath + File.separator + "default.mip";
        loadProjectFile(fn);
    }

    public final void loadConfigureFile() {
//        File directory = new File(".");
//        String fn = null;
//        try {
//            fn = directory.getCanonicalPath();
//        } catch (IOException ex) {
//            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
        String fn = this._startupPath + File.separator + "config.xml";
        if (new File(fn).exists()) {
            try {
                this._options.loadConfigFile(fn);
                this._mapDocument.setFont(this._options.getLegendFont());
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public final void saveConfigureFile() {
        String fn = this._options.getFileName();
        try {
            if (this._frmMeteoData == null) {
                this._options.setShowStartMeteoDataDlg(false);
            } else {
                this._options.setShowStartMeteoDataDlg(this._frmMeteoData.isVisible());
            }
            this._options.setMainFormLocation(this.getLocation());
            this._options.setMainFormSize(this.getSize());
            this._options.saveConfigFile(fn);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadProjectFile(String pFile) {
        if (new File(pFile).exists()) {
            try {
                _projectFile.loadProjFile(pFile);
                for (MapFrame mapFrame : this._mapDocument.getMapFrames()) {
                    mapFrame.getMapView().setDoubleBuffer(this._options.isDoubleBuffer());
                }
                this._mapDocument.getMapLayout().setDoubleBuffer(this._options.isDoubleBuffer());
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.setTitle("MeteoInfoMap - " + new File(pFile).getName());
        }
    }

    public Plugin readPlugin(String jarFileName) {
        try {
            Plugin plugin = new Plugin();
            plugin.setJarFileName(jarFileName);
            String className = GlobalUtil.getPluginClassName(jarFileName);
            if (className == null) {
                return null;
            } else {
                plugin.setClassName(className);
                URL url = new URL("file:" + plugin.getJarFileName());
                URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
                Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
                IPlugin instance = (IPlugin) clazz.newInstance();
                plugin.setPluginObject(instance);
                return plugin;
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<Plugin> readPlugins() throws MalformedURLException {
        List<Plugin> plugins = new ArrayList<>();
        String pluginPath = this._startupPath + File.separator + "plugins";
        if (new File(pluginPath).isDirectory()) {
            List<String> fileNames = GlobalUtil.getFiles(pluginPath, ".jar");
            for (String fn : fileNames) {
                Plugin plugin = readPlugin(fn);
                plugins.add(plugin);
            }
        }

        return plugins;
    }

    /**
     * Load plugins
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    public void loadPlugins() throws MalformedURLException, IOException {
        String pluginPath = this._startupPath + File.separator + "plugins";
        if (new File(pluginPath).isDirectory()) {
            List<String> fileNames = GlobalUtil.getFiles(pluginPath, ".jar");
            for (String fn : fileNames) {
                final Plugin plugin = this.readPlugin(fn);
                _plugins.add(plugin);
                URL url = new URL("file:" + plugin.getJarFileName());
                final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
                final JMenuItem pluginMI = new JMenuItem();
                pluginMI.setText(plugin.getName());
                pluginMI.setIcon(this._unloadedPluginIcon);
                pluginMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        FrmMain.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        try {
                            if (!plugin.isLoad()) {
                                Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
                                IPlugin instance = (IPlugin) clazz.newInstance();
                                instance.setApplication(FrmMain.this);
                                //instance.setName(plugin.getName());
                                plugin.setPluginObject(instance);
                                plugin.setLoad(true);
                                instance.load();
                                pluginMI.setSelected(true);
                                pluginMI.setIcon(FrmMain.this._loadedPluginIcon);
                            } else {
                                plugin.getPluginObject().unload();
                                plugin.setPluginObject(null);
                                plugin.setLoad(false);
                                pluginMI.setSelected(false);
                                pluginMI.setIcon(FrmMain.this._unloadedPluginIcon);
                            }
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InstantiationException ex) {
                            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        FrmMain.this.setCursor(Cursor.getDefaultCursor());
                    }
                });
                this.jMenu_Plugin.add(pluginMI);
            }
        }
    }

    public void loadPlugins(List<Plugin> plugins) throws MalformedURLException, IOException {
        if (plugins.size() > 0) {
            for (final Plugin plugin : plugins) {
                this.addPlugin(plugin);
            }
        }
    }

    private JMenuItem findPluginMenuItem(String name) {
        for (int i = 0; i < this.jMenu_Plugin.getItemCount(); i++) {
            JMenuItem mi = this.jMenu_Plugin.getItem(i);
            if (mi != null) {
                if (mi.getText().equals(name)) {
                    return mi;
                }
            }
        }

        return null;
    }

    /**
     * Remove a plugin
     *
     * @param plugin The plugin
     */
    public void removePlugin(Plugin plugin) {
        if (plugin.isLoad()) {
            unloadPlugin(plugin);
        }

        JMenuItem aMI = this.findPluginMenuItem(plugin.getName());
        if (aMI != null) {
            this.jMenu_Plugin.remove(aMI);
        }
    }

    public void addPlugin(final Plugin plugin) throws IOException {
        //this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));        
        URL url = new URL("file:" + plugin.getJarFileName());
        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
        final JMenuItem pluginMI = new JMenuItem();
        pluginMI.setText(plugin.getName());
        if (plugin.isLoad()) {
            try {
                Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
                IPlugin instance = (IPlugin) clazz.newInstance();
                instance.setApplication(FrmMain.this);
                instance.setName(plugin.getName());
                plugin.setPluginObject(instance);
                plugin.setLoad(true);
                instance.load();
                pluginMI.setSelected(true);
                pluginMI.setIcon(this._loadedPluginIcon);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            pluginMI.setIcon(this._unloadedPluginIcon);
        }
        pluginMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrmMain.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    if (!plugin.isLoad()) {
                        Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
                        IPlugin instance = (IPlugin) clazz.newInstance();
                        instance.setApplication(FrmMain.this);
                        instance.setName(plugin.getName());
                        plugin.setPluginObject(instance);
                        plugin.setLoad(true);
                        instance.load();
                        pluginMI.setSelected(true);
                        pluginMI.setIcon(FrmMain.this._loadedPluginIcon);
                    } else {
                        plugin.getPluginObject().unload();
                        //plugin.setPluginObject(null);
                        plugin.setLoad(false);
                        pluginMI.setSelected(false);
                        pluginMI.setIcon(FrmMain.this._unloadedPluginIcon);
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                }
                FrmMain.this.setCursor(Cursor.getDefaultCursor());
            }
        });
        this.jMenu_Plugin.add(pluginMI);
        //this.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Load plugin
     *
     * @param plugin Plugin
     */
    public void loadPlugin(Plugin plugin) {
        if (plugin.isLoad()) {
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        JMenuItem pluginMI = this.findPluginMenuItem(plugin.getName());
        URL url = null;
        try {
            url = new URL("file:" + plugin.getJarFileName());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
        try {
            Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
            IPlugin instance = (IPlugin) clazz.newInstance();
            instance.setApplication(FrmMain.this);
            instance.setName(plugin.getName());
            plugin.setPluginObject(instance);
            plugin.setLoad(true);
            instance.load();
            pluginMI.setSelected(true);
            pluginMI.setIcon(this._loadedPluginIcon);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void unloadPlugin(Plugin plugin) {
        if (!plugin.isLoad()) {
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        JMenuItem pluginMI = this.findPluginMenuItem(plugin.getName());
        plugin.getPluginObject().unload();
        plugin.setPluginObject(null);
        plugin.setLoad(false);
        pluginMI.setSelected(false);
        pluginMI.setIcon(this._unloadedPluginIcon);
        this.setCursor(Cursor.getDefaultCursor());
    }
    // </editor-fold>
    // <editor-fold desc="Menu">

    @Override
    public void setCurrentTool(AbstractButton currentTool) {
        if (_currentTool != null) {
            _currentTool.setSelected(false);
        }
        _currentTool = currentTool;
        _currentTool.setSelected(true);
        jLabel_Status.setText(_currentTool.getToolTipText());
    }

    private void _mapViewComponentResized(java.awt.event.ComponentEvent evt) {
        // TODO add your handling code here:
        //this.mapView1.zoomToExtent(this.mapView1.getViewExtent());
    }

    private void jTabbedPane_MainStateChanged(javax.swing.event.ChangeEvent evt) {
        // TODO add your handling code here:
        int selIndex = this.jTabbedPane_Main.getSelectedIndex();
        switch (selIndex) {
            case 0:    //MapView
                //_mapDocument.setIsLayoutView(false);

                this.jToolBar_Layout.setEnabled(false);
                for (Component c : this.jToolBar_Layout.getComponents()) {
                    c.setEnabled(false);
                }
                this.jMenuItem_LayoutProperty.setEnabled(false);
                this.jMenuItem_InsertLegend.setEnabled(false);
                this.jMenuItem_InsertTitle.setEnabled(false);
                this.jMenuItem_InsertText.setEnabled(false);
                this.jMenuItem_InsertNorthArrow.setEnabled(false);
                this.jMenuItem_InsertScaleBar.setEnabled(false);
                this.jMenuItem_InsertWindArrow.setEnabled(false);

                //_mapView.setIsLayoutMap(false);
                this._mapLayout.setLockViewUpdate(true);
                _mapView.zoomToExtent(_mapView.getViewExtent());
                break;
            case 1:    //MapLayout
                //_mapDocument.setIsLayoutView(true);

                this.jToolBar_Layout.setEnabled(true);
                for (Component c : this.jToolBar_Layout.getComponents()) {
                    c.setEnabled(true);
                }
                this.jMenuItem_LayoutProperty.setEnabled(true);
                this.jMenuItem_InsertLegend.setEnabled(true);
                this.jMenuItem_InsertTitle.setEnabled(true);
                this.jMenuItem_InsertText.setEnabled(true);
                this.jMenuItem_InsertNorthArrow.setEnabled(true);
                this.jMenuItem_InsertScaleBar.setEnabled(true);
                this.jMenuItem_InsertWindArrow.setEnabled(true);

                this._mapLayout.setLockViewUpdate(false);
                this._mapLayout.paintGraphics();
                break;
        }
    }

    private void jMenuItem_OpenActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        JFileChooser aDlg = new JFileChooser();
        String curDir = System.getProperty("user.dir");
        aDlg.setCurrentDirectory(new File(curDir));
        String[] fileExts = {"mip"};
        GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "MeteoInfo Project File (*.mip)");
        aDlg.setFileFilter(pFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            System.setProperty("user.dir", aFile.getParent());
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            openProjectFile(aFile.getAbsolutePath());
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void jMenuItem_SaveActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        String aFile = _projectFile.getFileName();
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            _projectFile.saveProjFile(aFile);
            this.setCursor(Cursor.getDefaultCursor());
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void jMenuItem_SaveAsActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        JFileChooser aDlg = new JFileChooser();
        String[] fileExts = {"mip"};
        GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "MeteoInfo Project File (*.mip)");
        aDlg.setFileFilter(pFileFilter);
        aDlg.setSelectedFile(new File(_projectFile.getFileName()));
        if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(this)) {
            File file = aDlg.getSelectedFile();
            System.setProperty("user.dir", file.getParent());
            String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
            String fileName = file.getAbsolutePath();
            if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                fileName = fileName + "." + extent;
            }
            file = new File(fileName);
            try {
                _projectFile.saveProjFile(file.getAbsolutePath());
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.setTitle("MeteoInfoMap - " + file.getName());
        }
    }

    private void jMenuItem_UndoActionPerformed(ActionEvent evt) {
        try {
            currentUndoManager.undo();
        } catch (CannotUndoException cre) {
        }
        this.refreshUndoRedo();
    }

    private void jMenuItem_RedoActionPerformed(ActionEvent evt) {
        try {
            currentUndoManager.redo();
        } catch (CannotRedoException cre) {
        }
        this.refreshUndoRedo();
    }

    private void jMenuItem_CutActionPerformed(ActionEvent evt) {
        VectorLayer layer = (VectorLayer) _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer();
        List<Shape> selShapes = (List<Shape>)layer.getSelectedShapes();

        UndoableEdit edit = (new MapViewUndoRedo()).new RemoveFeaturesEdit(_mapView, layer, selShapes);
        currentUndoManager.addEdit(edit);
        this.refreshUndoRedo();

        for (Shape shape : selShapes) {
            layer.editRemoveShape(shape);
        }
        this.jButton_EditRemoveFeature.setEnabled(false);
        _mapView.paintLayers();

        ShapeSelection shapeSelection = new ShapeSelection(selShapes);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(shapeSelection, null);
    }

    private void jMenuItem_CopyActionPerformed(ActionEvent evt) {
        VectorLayer layer = (VectorLayer) _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer();
        List<Shape> selShapes = (List<Shape>)layer.getSelectedShapes();
        List<Shape> r = new ArrayList<>();
        for (Shape s : selShapes) {
            r.add((Shape) s.clone());
        }
        ShapeSelection shapeSelection = new ShapeSelection(r);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(shapeSelection, null);
    }

    private void jMenuItem_PasteActionPerformed(ActionEvent evt) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = clipboard.getContents(null);
        if (t == null) {
            return;
        }

        VectorLayer layer = (VectorLayer) _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer();
        if (layer.isEditing()) {
            DataFlavor dataFlavors = new DataFlavor(org.meteoinfo.shape.Shape.class, "Shape Object");
            if (t.isDataFlavorSupported(dataFlavors)) {
                try {
                    List<Shape> shapes = (List<Shape>) t.getTransferData(dataFlavors);
                    for (Shape shape : shapes) {
                        layer.editAddShape(shape);
                    }
                    this._mapView.paintLayers();
                    UndoableEdit edit = (new MapViewUndoRedo()).new AddFeaturesEdit(this._mapView, layer, shapes);
                    currentUndoManager.addEdit(edit);
                    this.refreshUndoRedo();
                } catch (UnsupportedFlavorException | IOException ex) {
                } catch (Exception ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void jMenuItem_NewLayerActionPerformed(ActionEvent evt) {
        Object[] options = {"Point Layer", "Polyline Layer", "Polygon Layer"};
        String option = (String) JOptionPane.showInputDialog(this, "Select Layer Type:",
                "Select", JOptionPane.PLAIN_MESSAGE, null, options, "Point Layer");
        if (option != null) {
            ShapeTypes type = ShapeTypes.Point;
            if (option.equals("Polyline Layer")) {
                type = ShapeTypes.Polyline;
            } else if (option.equals("Polygon Layer")) {
                type = ShapeTypes.Polygon;
            }
            VectorLayer layer = new VectorLayer(type);
            layer.setLayerName("New " + option);
            layer.editAddField("ID", DataType.INT);
            layer.setProjInfo(this._mapDocument.getActiveMapFrame().getMapView().getProjection().getProjInfo());
            this._mapDocument.getActiveMapFrame().addLayer(layer);
            this._mapDocument.paintGraphics();
        }
    }

    private void jMenuItem_AddRingActionPerformed(ActionEvent evt) {
        _mapView.setMouseTool(MouseTools.Edit_AddRing);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_AddRing);
    }

    private void jMenuItem_FillRingActionPerformed(ActionEvent evt) {
        _mapView.setMouseTool(MouseTools.Edit_FillRing);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_FillRing);
    }

    private void jMenuItem_DeleteRingActionPerformed(ActionEvent evt) {
        _mapView.setMouseTool(MouseTools.Edit_DeleteRing);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_DeleteRing);
    }
    
    private void jMenuItem_ReformFeatureActionPerformed(ActionEvent evt) {
        _mapView.setMouseTool(MouseTools.Edit_ReformFeature);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_ReformFeature);
    }

    private void jMenuItem_SplitFeatureActionPerformed(ActionEvent evt) {
        _mapView.setMouseTool(MouseTools.Edit_SplitFeature);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_SplitFeature);
    }

    private void jMenuItem_MergeFeatureActionPerformed(ActionEvent evt) {
        VectorLayer layer = (VectorLayer) _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer();
        List<Shape> selShapes = (List<Shape>)layer.getSelectedShapes();
        if (selShapes.size() < 2) {
            JOptionPane.showMessageDialog(this, "Union option need at least two features are selected!");
            return;
        }

        List<Geometry> geos = new ArrayList<>();
        for (int i = 0; i < selShapes.size(); i++) {
            geos.add(selShapes.get(i).toGeometry());
        }
        Geometry mgeo = UnaryUnionOp.union(geos);
        Shape bShape = ShapeFactory.createShape(mgeo);
        
        for (Shape shape : selShapes) {
            layer.editRemoveShape(shape);
        }
        try {
            layer.editAddShape(bShape);
        } catch (Exception ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        _mapView.paintLayers();
        UndoableEdit edit = (new MapViewUndoRedo()).new UnionFeaturesEdit(this._mapView, layer, bShape, selShapes);
        currentUndoManager.addEdit(edit);
        this.refreshUndoRedo();
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        // TODO add your handling code here:
        //_mapView.setLockViewUpdate(true);
        //_mapDocument.setIsLayoutView(false);
        //_mapView.setIsLayoutMap(false);
        this._mapLayout.setLockViewUpdate(true);
        _mapView.zoomToExtent(_mapView.getViewExtent());
        //_mapView.setLockViewUpdate(false);
        this.zoomUndoManager = new UndoManager();

        //Open MeteoData form
        _frmMeteoData = new FrmMeteoData(this, false);
        //_frmMeteoData.setSize(500, 280);
        _frmMeteoData.setLocation(this.getX() + 10, this.getY() + this.getHeight() - _frmMeteoData.getHeight() - 40);
        _frmMeteoData.setVisible(this._options.isShowStartMeteoDataDlg());
//        if (this._options.isShowStartMeteoDataDlg()) {
//            _frmMeteoData = new FrmMeteoData(this, false);
//            //_frmMeteoData.setSize(500, 280);
//            _frmMeteoData.setLocation(this.getX() + 10, this.getY() + this.getHeight() - _frmMeteoData.getHeight() - 40);
//            _frmMeteoData.setVisible(this._options.isShowStartMeteoDataDlg());
//        }
    }

    private void jMenuItem_LayersActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (this.jMenuItem_Layers.isSelected()) {
            this.jSplitPane1.setDividerLocation(0);
            this.jSplitPane1.setDividerSize(0);
            this.jMenuItem_Layers.setSelected(false);
        } else {
            this.jSplitPane1.setDividerLocation(180);
            this.jSplitPane1.setDividerSize(5);
            this.jMenuItem_Layers.setSelected(true);
        }
    }

    private void jMenuItem_AttributeDataActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        //JOptionPane.showMessageDialog(null, "Under developing!");
        if (_mapDocument.getSelectedNode() == null) {
            return;
        }

        if (_mapDocument.getSelectedNode().getNodeType() == NodeTypes.LayerNode) {
            LayerNode aLN = (LayerNode) _mapDocument.getSelectedNode();
            MapLayer aLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
            if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
                if (((VectorLayer) aLayer).getShapeNum() > 0) {
                    FrmAttriData aFrmData = new FrmAttriData();
                    aFrmData.setLayer((VectorLayer) aLayer);
                    aFrmData.setLocationRelativeTo(this);
                    aFrmData.setVisible(true);
                }
            }
        }
    }

    private void jMenuItem_LayoutPropertyActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmProperty pFrm = new FrmProperty(this, true, false);
        pFrm.setObject(this._mapLayout);
        pFrm.setLocationRelativeTo(this);
        pFrm.setVisible(true);
    }

    private void jMenuItem_MapPropertyActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmProperty pFrm = new FrmProperty(this, true, false);
        pFrm.setObject(this._mapView);
        pFrm.setLocationRelativeTo(this);
        pFrm.setVisible(true);
    }

    private void jMenuItem_MaskOutActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmProperty pFrm = new FrmProperty(this, true, false);
        MaskOut maskout = this._mapView.getMaskOut();
        maskout.setMapView(_mapView);
        pFrm.setObject(maskout.new MaskOutBean());
        pFrm.setLocationRelativeTo(this);
        pFrm.setVisible(true);
    }

    private void jMenuItem_ProjectionActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmProjection frmProj = new FrmProjection(this, false);
        frmProj.setLocationRelativeTo(this);
        frmProj.setVisible(true);
    }

    private void jMenuItem_InsertMapFrameActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        MapFrame aMF = new MapFrame();
        aMF.setText(_mapDocument.getNewMapFrameName());
        aMF.getMapView().setDoubleBuffer(this._options.isDoubleBuffer());
        _mapDocument.addMapFrame(aMF);
        _mapDocument.paintGraphics();
    }

    private void jMenuItem_InsertTitleActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        LayoutGraphic text = _mapDocument.getMapLayout().addText("Map Title", _mapDocument.getMapLayout().getWidth() / 2, 20, 12);
        _mapDocument.getMapLayout().paintGraphics();
        UndoableEdit edit = (new MapLayoutUndoRedo()).new AddElementEdit(_mapDocument.getMapLayout(), text);
        undoManager.addEdit(edit);
        this.refreshUndoRedo();
    }

    private void jMenuItem_InsertTextActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        LayoutGraphic text = _mapDocument.getMapLayout().addText("Text", _mapDocument.getMapLayout().getWidth() / 2, 200);
        _mapDocument.getMapLayout().paintGraphics();
        UndoableEdit edit = (new MapLayoutUndoRedo()).new AddElementEdit(_mapDocument.getMapLayout(), text);
        undoManager.addEdit(edit);
        this.refreshUndoRedo();
    }

    private void jMenuItem_InsertLegendActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        LayoutLegend legend = _mapDocument.getMapLayout().addLegend(100, 100);
        _mapDocument.getMapLayout().paintGraphics();
        UndoableEdit edit = (new MapLayoutUndoRedo()).new AddElementEdit(_mapDocument.getMapLayout(), legend);
        undoManager.addEdit(edit);
        this.refreshUndoRedo();
    }

    private void jMenuItem_InsertScaleBarActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        LayoutScaleBar sb = _mapDocument.getMapLayout().addScaleBar(100, 100);
        _mapDocument.getMapLayout().paintGraphics();
        UndoableEdit edit = (new MapLayoutUndoRedo()).new AddElementEdit(_mapDocument.getMapLayout(), sb);
        undoManager.addEdit(edit);
        this.refreshUndoRedo();
    }

    private void jMenuItem_InsertNorthArrowActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        LayoutNorthArrow na = _mapDocument.getMapLayout().addNorthArrow(200, 100);
        _mapDocument.getMapLayout().paintGraphics();
        UndoableEdit edit = (new MapLayoutUndoRedo()).new AddElementEdit(_mapDocument.getMapLayout(), na);
        undoManager.addEdit(edit);
        this.refreshUndoRedo();
    }

    private void jMenuItem_InsertWindArrowActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:               
        LayoutGraphic wa = _mapDocument.getMapLayout().addWindArrow(100, 100);
        _mapDocument.getMapLayout().paintGraphics();
        UndoableEdit edit = (new MapLayoutUndoRedo()).new AddElementEdit(_mapDocument.getMapLayout(), wa);
        undoManager.addEdit(edit);
        this.refreshUndoRedo();
    }

    private void jMenuItem_ScriptActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        //JOptionPane.showMessageDialog(null, "Under developing!");
//        // Create an instance of the PythonInterpreter
//        PythonInterpreter interp = new PythonInterpreter();
//
//        // The exec() method executes strings of code
//        interp.exec("import sys");
//        interp.exec("print sys");
//
//        // Set variable values within the PythonInterpreter instance
//        //interp.set("a", new PyInteger(42));
//        interp.exec("a = 42");
//        interp.exec("print a");
//        interp.exec("x = 2+2");
//        interp.exec("b = 25" + "\n" + "print a + b");
//
//        // Obtain the value of an object from the PythonInterpreter and store it
//        // into a PyObject.
//        PyObject x = interp.get("x");
//        System.out.println("x: " + x);

        FrmTextEditor frmTE = new FrmTextEditor(this);
        frmTE.setTextFont(this._options.getTextFont());
        frmTE.setLocationRelativeTo(this);
        frmTE.setVisible(true);
    }

    private void jMenuItem_ScriptConsoleActionPerformed(java.awt.event.ActionEvent evt) {
        FrmConsole frmConsole = new FrmConsole(this);
        frmConsole.setTitle("Jython Console");
        frmConsole.setLocationRelativeTo(this);
        frmConsole.setVisible(true);
        frmConsole.InitializeConsole();
    }

    private void jMenuItem_SelByAttrActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmSelectByAttributes frmSel = new FrmSelectByAttributes(this, false);
        frmSel.setLocationRelativeTo(this);
        frmSel.setVisible(true);
    }

    private void jMenuItem_SelByLocationActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmSelectByLocation frmSel = new FrmSelectByLocation(this, false);
        frmSel.setLocationRelativeTo(this);
        frmSel.setVisible(true);
    }

    private void jMenuItem_ClearSelectionActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        MapLayer aLayer = _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer();
        if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
            ((VectorLayer) aLayer).clearSelectedShapes();
            for (Shape shape : ((VectorLayer) aLayer).getShapes()) {
                shape.setVisible(true);
            }
        }

        this.refreshMap();
    }

    private void jMenuItem_HelpActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
//        Help help = new Help();
//        help.setTitle("MeteoInfoMap - Help");
//        help.setIconImage("/images/MeteoInfo_1_16x16x8.png");
//        help.setSize(800, 700);
//        help.setLocationRelativeTo(this);
//        help.setVisible(true);
        try {
            URI uri = new URI("http://www.meteothink.org/docs/meteoinfo/desktop/index.html");
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            if (desktop != null) {
                desktop.browse(uri);
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(FrmAbout.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
        }
    }

/*    *//**
     * find the helpset file and create a HelpSet object
     *//*
    private HelpSet getHelpSet(String helpsetfile) {
        HelpSet hs = null;
        ClassLoader cl = this.getClass().getClassLoader();

        try {
            //URL hsURL = HelpSet.findHelpSet(cl, helpsetfile);   
            //URL hsURL = new URL("file:/" + helpsetfile);
            URL hsURL = this.getClass().getResource(helpsetfile);
            hs = new HelpSet(cl, hsURL);
        } catch (Exception ee) {
            System.out.println("HelpSet: " + ee.getMessage());
            System.out.println("HelpSet: " + helpsetfile + " not found");
        }
        return hs;
    }*/

    private void jMenuItem_AboutActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmAbout frmAbout = new FrmAbout(this, false);
        frmAbout.setLocationRelativeTo(this);
        frmAbout.setVisible(true);
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        // TODO add your handling code here:
        int result = JOptionPane.showConfirmDialog(this, "If save the project?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
        switch (result) {
            case JOptionPane.YES_OPTION:
                String aFile = _projectFile.getFileName();
                try {
                    _projectFile.saveProjFile(aFile);
                    this.saveConfigureFile();
                    _plugins.saveConfigFile();
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                }
                //this.dispose();
                System.exit(0);
            case JOptionPane.NO_OPTION:
                //this.dispose();
                try {
                    this.saveConfigureFile();
                    _plugins.saveConfigFile();
                } catch (Exception e) {
                }
                System.exit(0);
            case JOptionPane.CANCEL_OPTION:
                break;
            default:
                break;
        }
    }

    private void jComboBox_PageZoomActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (_isLoading) {
            return;
        }
        String zoomStr = this.jComboBox_PageZoom.getSelectedItem().toString().trim();
        if (zoomStr.endsWith("%")) {
            zoomStr = zoomStr.substring(0, zoomStr.length() - 1);
        }
        try {
            float zoom = Float.parseFloat(zoomStr);
            _mapDocument.getMapLayout().setZoom(zoom / 100);
            _mapDocument.getMapLayout().paintGraphics();
        } catch (Exception e) {
        }
    }

    private void jButton_FitToScreenActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        float zoomX = (float) _mapDocument.getMapLayout().getWidth() / _mapDocument.getMapLayout().getPageBounds().width;
        float zoomY = (float) _mapDocument.getMapLayout().getHeight() / _mapDocument.getMapLayout().getPageBounds().height;
        float zoom = Math.min(zoomX, zoomY);
        PointF aP = new PointF(0, 0);
        _mapDocument.getMapLayout().setPageLocation(aP);
        _mapDocument.getMapLayout().setZoom(zoom);
        _mapDocument.getMapLayout().paintGraphics();
    }

    private void jButton_PageZoomOutActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapLayout.setZoom(_mapLayout.getZoom() * 0.8F);
        _mapLayout.paintGraphics();
    }

    private void jButton_PageZoomInActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapLayout.setZoom(_mapLayout.getZoom() * 1.2F);
        _mapLayout.paintGraphics();
    }

    private void jButton_PageSetActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmPageSet aFrmPageSet = new FrmPageSet(this, true);
        aFrmPageSet.setMapLayout(_mapLayout);
        aFrmPageSet.setPaperSize(_mapDocument.getMapLayout().getPaperSize());
        aFrmPageSet.setLandscape(_mapDocument.getMapLayout().isLandscape());
        aFrmPageSet.setLocationRelativeTo(this);
        aFrmPageSet.setVisible(true);
    }

    private void jButton_EditVerticesActionPerformed(java.awt.event.ActionEvent evt) {
        if (jButton_EditVertices.isSelected()) {
            _mapView.setMouseTool(MouseTools.EditVertices);
            _mapDocument.getMapLayout().setMouseMode(MouseMode.EditVertices);
        } else {
            this.jButton_SelectElement.doClick();
        }

        if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
            _mapView.paintLayers();
        } else {
            _mapDocument.getMapLayout().paintGraphics();
        }
    }

    private void jButton_NewEllipseActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Ellipse);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Ellipse);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewCircleActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Circle);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Circle);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewRectangleActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Rectangle);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Rectangle);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewCurvePolygonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_CurvePolygon);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_CurvePolygon);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewPolygonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Polygon);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Polygon);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewCurveActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Curve);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Curve);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewFreehandActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Freehand);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Freehand);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewPolylineActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Polyline);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Polyline);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewPointActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Point);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Point);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_NewLabelActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.New_Label);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.New_Label);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_SavePictureActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        String path = System.getProperty("user.dir");
        File pathDir = new File(path);
        ImageFileChooser aDlg = new ImageFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        String[] fileExts = new String[]{"png"};
        GenericFileFilter pngFileFilter = new GenericFileFilter(fileExts, "Png Image (*.png)");
        aDlg.addChoosableFileFilter(pngFileFilter);
        fileExts = new String[]{"gif"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Gif Image (*.gif)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"jpg"};
        mapFileFilter = new GenericFileFilter(fileExts, "Jpeg Image (*.jpg)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"bmp"};
        mapFileFilter = new GenericFileFilter(fileExts, "Bitmap Image (*.bmp)");
        aDlg.addChoosableFileFilter(mapFileFilter);
//        fileExts = new String[]{"tif"};
//        mapFileFilter = new GenericFileFilter(fileExts, "Tiff Image (*.tif)");
//        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"eps"};
        mapFileFilter = new GenericFileFilter(fileExts, "EPS file (*.eps)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"pdf"};
        mapFileFilter = new GenericFileFilter(fileExts, "PDF file (*.pdf)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"emf"};
        mapFileFilter = new GenericFileFilter(fileExts, "EMF file (*.emf)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"ps"};
        mapFileFilter = new GenericFileFilter(fileExts, "Postscript file (*.ps)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        aDlg.setFileFilter(pngFileFilter);
        aDlg.setAcceptAllFileFilterUsed(false);
        if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            System.setProperty("user.dir", aFile.getParent());
            String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
            String fileName = aFile.getAbsolutePath();
            if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                fileName = fileName + "." + extent;
            }

            if (new File(fileName).exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this, "File exists! Overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (overwrite == JOptionPane.NO_OPTION) {
                    return;
                }
            }
                
            Integer dpi = aDlg.getDPI();

            if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
                try {
                    _mapDocument.getActiveMapFrame().getMapView().exportToPicture(fileName, dpi);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (PrintException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (this.jTabbedPane_Main.getSelectedIndex() == 1) {
                try {
                    _mapDocument.getMapLayout().exportToPicture(fileName, dpi);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (PrintException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void jButton_LabelSetActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (_mapDocument.getSelectedNode() == null) {
            return;
        }

        if (_mapDocument.getSelectedNode().getNodeType() == NodeTypes.LayerNode) {
            LayerNode aLN = (LayerNode) _mapDocument.getSelectedNode();
            MapLayer aMLayer = aLN.getMapFrame().getMapView().getLayerByHandle(aLN.getLayerHandle());
            if (aMLayer.getLayerType() == LayerTypes.VectorLayer) {
                VectorLayer aLayer = (VectorLayer) aMLayer;
                if (aLayer.getShapeNum() > 0) {
                    FrmLabelSet aFrmLabel = new FrmLabelSet(this, false, _mapDocument.getActiveMapFrame().getMapView());
                    aFrmLabel.setLayer(aLayer);
                    aFrmLabel.setLocationRelativeTo(this);
                    aFrmLabel.setVisible(true);
                }
            }
        }
    }

    private void jButton_MeasurementActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Measurement);
        //_mapDocument.getMapLayout().setMeasurementForm(_mapView.getMeasurementForm());
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Measurement);
        if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
            _mapView.showMeasurementForm();
        } else if (this.jTabbedPane_Main.getSelectedIndex() == 1) {
            _mapDocument.getMapLayout().showMeasurementForm();
        }
    }

    private void jButton_SelByRectangleActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.SelectFeatures_Rectangle);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_SelectFeatures_Rectangle);
    }

    private void jButton_SelByPolygonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.SelectFeatures_Polygon);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_SelectFeatures_Polygon);
    }

    private void jButton_SelByLassoActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.SelectFeatures_Lasso);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_SelectFeatures_Lasso);
    }

    private void jButton_SelByCircleActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.SelectFeatures_Circle);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_SelectFeatures_Circle);
    }

    private void jButton_IdentiferActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Identifer);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Identifer);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_ZoomToExtentActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmZoomToExtent frmZoom = new FrmZoomToExtent(this, true);
        frmZoom.setVisible(true);
    }

    private void jButton_ZoomToLayerActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (_mapDocument.getSelectedNode() == null) {
            return;
        }

        if (_mapDocument.getSelectedNode().getNodeType() == NodeTypes.LayerNode) {
            MapFrame aMF = _mapDocument.getCurrentMapFrame();
            MapLayer aLayer = ((LayerNode) _mapDocument.getSelectedNode()).getMapLayer();
            if (aLayer != null) {
                Extent oldExtent = (Extent) aMF.getMapView().getViewExtent().clone();
                aMF.getMapView().zoomToExtent(aLayer.getExtent());
                UndoableEdit edit = (new MapViewUndoRedo()).new ZoomEdit(aMF.getMapView(), oldExtent, (Extent) aMF.getMapView().getViewExtent().clone());
                zoomUndoManager.addEdit(edit);
                this.refreshZoomUndoRedo();
            }
        }
    }

    private void jButton_FullExtentActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        Extent oldExtent = (Extent) _mapView.getViewExtent().clone();
        _mapView.zoomToExtent(_mapView.getExtent());
        UndoableEdit edit = (new MapViewUndoRedo()).new ZoomEdit(_mapView, oldExtent, (Extent) _mapView.getViewExtent().clone());
        zoomUndoManager.addEdit(edit);
        this.refreshZoomUndoRedo();
    }

    private void jButton_ZoomUndoActionPerformed(ActionEvent evt) {
        try {
            zoomUndoManager.undo();
        } catch (CannotUndoException cre) {
        }
        this.refreshZoomUndoRedo();
    }

    private void jButton_ZoomRedoActionPerformed(ActionEvent evt) {
        try {
            zoomUndoManager.redo();
        } catch (CannotRedoException cre) {
        }
        this.refreshZoomUndoRedo();
    }

    private void jButton_PanActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Pan);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Pan);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_ZoomOutActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Zoom_Out);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_ZoomOut);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_ZoomInActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Zoom_In);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_ZoomIn);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_SelectElementActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.SelectElements);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Select);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_RemoveDataLayersActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _mapDocument.getActiveMapFrame().removeMeteoLayers();
    }

    private void jButton_OpenDataActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (_frmMeteoData == null) {
            _frmMeteoData = new FrmMeteoData(this, false);
            _frmMeteoData.setLocation(this.getX() + 10, this.getY() + this.getHeight() - _frmMeteoData.getHeight() - 40);
        }
        _frmMeteoData.setVisible(true);
    }

    private void jButton_AddLayerActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:        
        String path = System.getProperty("user.dir");
        File pathDir = new File(path);

        //JFileChooser aDlg = new JFileChooser();
        ShapeFileChooser aDlg = new ShapeFileChooser();
        //aDlg.setAcceptAllFileFilterUsed(false);
        aDlg.setCurrentDirectory(pathDir);
        String[] fileExts = new String[]{"shp", "bil", "bip", "bsq", "wmp", "bln", "gif", "jpg", "png", "tif", "asc"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Supported Formats");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"shp"};
        GenericFileFilter shpFileFilter = new GenericFileFilter(fileExts, "Shape File (*.shp)");
        aDlg.addChoosableFileFilter(shpFileFilter);
        aDlg.setFileFilter(mapFileFilter);
        aDlg.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File[] files = aDlg.getSelectedFiles();
            System.setProperty("user.dir", files[0].getParent());

            for (File aFile : files) {
                MapLayer aLayer = null;
                try {
                    String fn = aFile.getAbsolutePath();                    
                    String ext = GlobalUtil.getFileExtension(fn);
                    switch (ext.toLowerCase()){
                        case "shp":
                            String encoding = aDlg.getEncoding();
                            aLayer = ShapeFileManage.loadShapeFile(fn, encoding);
                            break;
                        default:
                            aLayer = MapDataManage.loadLayer(fn);
                            break;
                    }
                    
                    switch (ext.toLowerCase()){
                        case "bil":
                        case "bip":
                        case "bsq":                            
                            aLayer.setProjInfo(this._mapDocument.getActiveMapFrame().getMapView().getProjection().getProjInfo());
                            break;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (aLayer != null) {
                    ProjectionInfo dataProj = aLayer.getProjInfo();
                    ProjectionInfo mapViewProj = _mapDocument.getActiveMapFrame().getMapView().getProjection().getProjInfo();
                    if (!dataProj.equals(mapViewProj)) {
                        if (JOptionPane.showConfirmDialog(null, "Different projection! If project map view?", "Conform", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            _mapDocument.getActiveMapFrame().getMapView().projectLayers(dataProj);
                        }
                    }
                    this._mapDocument.getActiveMapFrame().addLayer(aLayer);
                }
            }
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void jButton_AddWebLayerActionPerformed(java.awt.event.ActionEvent evt) {
        WebMapLayer layer = new WebMapLayer();
        layer.setWebMapProvider(WebMapProvider.OpenStreetMap);
        //layer.setDefaultProvider(DefaultProviders.OpenStreetMapQuestSattelite);
        //layer.setDefaultProvider(DefaultProviders.ArcGISImage);
        ProjectionInfo proj = this.getMapView().getProjection().getProjInfo();
        if (proj.getProjectionName() != ProjectionNames.Mercator) {
            if (JOptionPane.showConfirmDialog(null, "Not mercator projection! If project?", "Conform", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                ProjectionInfo toProj = ProjectionInfo.factory("+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0");
                this.getMapView().projectLayers(toProj);
            }
        }

        this._mapDocument.getActiveMapFrame().addLayer(0, layer);
    }

    private void jButton_EditStartOrEndActionPerformed(ActionEvent evt) {
        ItemNode selNode = this._mapDocument.getSelectedNode();
        if (selNode.getNodeType() != NodeTypes.LayerNode) {
            return;
        }

        LayerNode selLayerNode = (LayerNode) selNode;
        MapLayer selMapLayer = selLayerNode.getMapLayer();
        if (selMapLayer == null) {
            return;
        }

        if (selMapLayer.getLayerType() == LayerTypes.VectorLayer) {
            VectorLayer layer = (VectorLayer) selMapLayer;
            if (selLayerNode.isEditing()) {
                if (currentUndoManager.canUndo()) {
                    int result = JOptionPane.showConfirmDialog(this, "If save edit?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (result != JOptionPane.CANCEL_OPTION) {
                        selLayerNode.setEditing(false);
                        this.jButton_EditStartOrEnd.setSelected(false);
                        this.jButton_EditTool.setEnabled(false);
                        this.jButton_EditSave.setEnabled(false);
                        this.jButton_EditNewFeature.setEnabled(false);
                        this.jButton_EditRemoveFeature.setEnabled(false);
                        this.jButton_EditFeatureVertices.setEnabled(false);

                        this.jMenuItem_AddRing.setEnabled(false);
                        this.jMenuItem_FillRing.setEnabled(false);
                        this.jMenuItem_SplitFeature.setEnabled(false);
                        this.jMenuItem_DeleteRing.setEnabled(false);
                        this.jMenuItem_ReformFeature.setEnabled(false);

                        if (result == JOptionPane.YES_OPTION) {
                            layer.saveFile();
                        } else if (result == JOptionPane.NO_OPTION) {
                            while (currentUndoManager.canUndo()) {
                                currentUndoManager.undo();
                            }
                        }
                        currentUndoManager.end();
                        currentUndoManager = undoManager;
                        this.refreshUndoRedo();
                    }
                } else {
                    selLayerNode.setEditing(false);
                    this.jButton_EditStartOrEnd.setSelected(false);
                    this.jButton_EditTool.setEnabled(false);
                    this.jButton_EditSave.setEnabled(false);
                    this.jButton_EditNewFeature.setEnabled(false);
                    this.jButton_EditRemoveFeature.setEnabled(false);
                    this.jButton_EditFeatureVertices.setEnabled(false);

                    this.jMenuItem_AddRing.setEnabled(false);
                    this.jMenuItem_FillRing.setEnabled(false);
                    this.jMenuItem_SplitFeature.setEnabled(false);
                    this.jMenuItem_DeleteRing.setEnabled(false);
                    this.jMenuItem_ReformFeature.setEnabled(false);

                    currentUndoManager = undoManager;
                    this.refreshUndoRedo();
                }
                this.jButton_SelectElement.doClick();
            } else {
                selLayerNode.setEditing(true);
                this.jButton_EditTool.setEnabled(true);
                this.jButton_EditNewFeature.setEnabled(true);
                if (layer.hasSelectedShapes()) {
                    this.jButton_EditRemoveFeature.setEnabled(true);
                    this.jButton_EditFeatureVertices.setEnabled(true);
                }
                this.jButton_EditStartOrEnd.setSelected(true);

                if (layer.getShapeType().isPolygon()) {
                    this.jMenuItem_AddRing.setEnabled(true);
                    this.jMenuItem_FillRing.setEnabled(true);
                    this.jMenuItem_SplitFeature.setEnabled(true);
                    this.jMenuItem_DeleteRing.setEnabled(true);
                    this.jMenuItem_ReformFeature.setEnabled(true);
                } else if (layer.getShapeType().isLine()) {
                    this.jMenuItem_SplitFeature.setEnabled(true);
                    this.jMenuItem_ReformFeature.setEnabled(true);
                }

                currentUndoManager = layer.getUndoManager();
                this.refreshUndoRedo();
                this.jButton_EditTool.doClick();
            }
            this._mapDocument.paintGraphics();
        }
    }

    private void jButton_EditSaveActionPerformed(ActionEvent evt) {
        VectorLayer layer = (VectorLayer) _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer();
        layer.saveFile();
    }

    private void jButton_EditToolActionPerformed(ActionEvent evt) {
        _mapView.setMouseTool(MouseTools.Edit_Tool);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_Tool);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_EditNewFeatureActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
        _mapView.setMouseTool(MouseTools.Edit_NewFeature);
        _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_NewFeature);

        setCurrentTool((JToggleButton) evt.getSource());
    }

    private void jButton_EditRemoveFeatureActionPerformed(ActionEvent evt) {
        VectorLayer layer = (VectorLayer) _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer();
        List<Shape> selShapes = (List<Shape>)layer.getSelectedShapes();

        UndoableEdit edit = (new MapViewUndoRedo()).new RemoveFeaturesEdit(_mapView, layer, selShapes);
        currentUndoManager.addEdit(edit);
        this.refreshUndoRedo();

        for (Shape shape : selShapes) {
            layer.editRemoveShape(shape);
        }
        this.jButton_EditRemoveFeature.setEnabled(false);
        _mapView.paintLayers();
    }

    private void jButton_EditFeatureVerticesActionPerformed(ActionEvent evt) {
        VectorLayer layer = (VectorLayer) _mapDocument.getActiveMapFrame().getMapView().getSelectedLayer();
        List<Shape> selShapes = (List<Shape>)layer.getSelectedShapes();
        if (selShapes.size() != 1) {
            JOptionPane.showMessageDialog(this, "Select one editable feature to edit.", "Edit Vertices", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (jButton_EditFeatureVertices.isSelected()) {
            layer.setEditingShape(layer.getSelectedShapes().get(0));
            _mapView.setMouseTool(MouseTools.Edit_FeatureVertices);
            _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_FeatureVertices);
        } else {
            layer.clearEditingShape();
            _mapView.setMouseTool(MouseTools.Edit_Tool);
            _mapDocument.getMapLayout().setMouseMode(MouseMode.Map_Edit_Tool);
        }

        if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
            _mapView.paintLayers();
        } else {
            _mapDocument.getMapLayout().paintGraphics();
        }
    }

    private void jMenuItem_OptionsActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmOptions frmOption = new FrmOptions(this, true);
        frmOption.setLocationRelativeTo(this);
        frmOption.setVisible(true);
    }

    private void jMenuItem_OutputMapDataActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmOutputMapData frm = new FrmOutputMapData(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    private void jMenuItem_AddXYDataActionPerformed(java.awt.event.ActionEvent evt) {
        FrmAddXYData frm = new FrmAddXYData(this, true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }
    
    private void jMenuItem_BufferActionPerformed(ActionEvent evt){
        FrmBuffer frm = new FrmBuffer(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    private void jMenuItem_ClippingActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmClipping frm = new FrmClipping(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }
    
    private void jMenuItem_ConvexhullActionPerformed(java.awt.event.ActionEvent evt) {
        FrmConvexhull frm = new FrmConvexhull(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }
    
    private void jMenuItem_IntersectionActionPerformed(java.awt.event.ActionEvent evt) {
        FrmIntersection frm = new FrmIntersection(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }
    
    private void jMenuItem_DifferenceActionPerformed(java.awt.event.ActionEvent evt) {
        FrmDifference frm = new FrmDifference(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }
    
    private void jMenuItem_SymDifferenceActionPerformed(java.awt.event.ActionEvent evt) {
        FrmSymDifference frm = new FrmSymDifference(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    private void jMenuItem_AnimatorActionPerformed(java.awt.event.ActionEvent evt) {
        FrmGifAnimator frm = new FrmGifAnimator(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    private void jMenuItem_JoinNCFilesActionPerformed(java.awt.event.ActionEvent evt) {
        FrmJoinNCFiles frm = new FrmJoinNCFiles(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    private void jMenuItem_PluginManagerActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmPluginManager frm = new FrmPluginManager(this, true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    /**
     * Open project file
     *
     * @param projFile project file path
     */
    @Override
    public void openProjectFile(String projFile) {
        for (MapFrame mf : _mapDocument.getMapFrames()) {
            if (mf.getMapView().getLayerNum() > 0) {
                mf.removeAllLayers();
            }
        }
        //Application.DoEvents();
        loadProjectFile(projFile);
        MapView mapView = _mapDocument.getActiveMapFrame().getMapView();
        mapView.setSize(_mapView.getSize());
        _mapView = mapView;
        setMapView();
        //setMapLayout();

        _mapDocument.paintGraphics();
        //_mapView.zoomToExtent(_mapView.getViewExtent());
        _mapView.paintLayers();
    }

    // </editor-fold>
    // <editor-fold desc="Others">
    /**
     * Refresh map document and map view / map layout
     */
    public void refresh() {
        this._mapDocument.paintGraphics();
        if (this.jTabbedPane_Main.getSelectedIndex() == 0) {
            this._mapView.paintLayers();
        } else {
            this._mapLayout.paintGraphics();
        }
    }

    /**
     * Refresh map view / map layer
     */
    public void refreshMap() {
        int selIndex = this.jTabbedPane_Main.getSelectedIndex();
        switch (selIndex) {
            case 0:    //MapView
                this._mapView.paintLayers();
                break;
            default:
                this._mapLayout.paintGraphics();
        }
    }
    // </editor-fold>
    // </editor-fold>

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //new frmMain().setVisible(true);
                FrmMain frame = new FrmMain(null, null);
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
    private org.meteoinfo.legend.LayersLegend _mapDocument;
    private org.meteoinfo.layout.MapLayout _mapLayout;
    private org.meteoinfo.map.MapView _mapView;
    private org.meteoinfo.ui.JSplitButton jSplitButton_AddLayer;
    private javax.swing.JPopupMenu jPopupMenu_AddLayer;
    private javax.swing.JMenuItem jMenuItem_AddLayer;
    private javax.swing.JMenuItem jMenuItem_AddWebLayer;
    private javax.swing.JToggleButton jButton_EditVertices;
    private javax.swing.JButton jButton_FitToScreen;
    private javax.swing.JButton jButton_FullExtent;
    private javax.swing.JToggleButton jButton_Identifer;
    private javax.swing.JButton jButton_LabelSet;
    private javax.swing.JButton jButton_Measurement;
    private javax.swing.JToggleButton jButton_NewCircle;
    private javax.swing.JToggleButton jButton_NewCurve;
    private javax.swing.JToggleButton jButton_NewCurvePolygon;
    private javax.swing.JToggleButton jButton_NewEllipse;
    private javax.swing.JToggleButton jButton_NewFreehand;
    private javax.swing.JToggleButton jButton_NewLabel;
    private javax.swing.JToggleButton jButton_NewPoint;
    private javax.swing.JToggleButton jButton_NewPolygon;
    private javax.swing.JToggleButton jButton_NewPolyline;
    private javax.swing.JToggleButton jButton_NewRectangle;
    private javax.swing.JButton jButton_OpenData;
    private javax.swing.JButton jButton_PageSet;
    private javax.swing.JButton jButton_PageZoomIn;
    private javax.swing.JButton jButton_PageZoomOut;
    private javax.swing.JToggleButton jButton_Pan;
    private javax.swing.JButton jButton_RemoveDataLayers;
    private javax.swing.JButton jButton_SavePicture;
    private javax.swing.JToggleButton jButton_SelectElement;
    private javax.swing.JToggleButton jButton_ZoomIn;
    private javax.swing.JToggleButton jButton_ZoomOut;
    private javax.swing.JButton jButton_ZoomToExtent;
    private javax.swing.JButton jButton_ZoomToLayer;
    private javax.swing.JButton jButton_ZoomUndo;
    private javax.swing.JButton jButton_ZoomRedo;
    private javax.swing.JToggleButton jButton_EditStartOrEnd;
    private javax.swing.JButton jButton_EditSave;
    private javax.swing.JToggleButton jButton_EditTool;
    private javax.swing.JToggleButton jButton_EditNewFeature;
    private javax.swing.JButton jButton_EditRemoveFeature;
    private javax.swing.JToggleButton jButton_EditFeatureVertices;
    private org.meteoinfo.ui.JSplitToggleButton jSplitButton_SelectFeature;
    private javax.swing.JPopupMenu jPopupMenu_SelectFeature;
    private javax.swing.JMenuItem jMenuItem_SelByRectangle;
    private javax.swing.JMenuItem jMenuItem_SelByPolygon;
    private javax.swing.JMenuItem jMenuItem_SelByLasso;
    private javax.swing.JMenuItem jMenuItem_SelByCircle;
    private javax.swing.JComboBox jComboBox_PageZoom;
    private javax.swing.JLabel jLabel_Coordinate;
    private javax.swing.JLabel jLabel_Status;
    private javax.swing.JLabel jLabel_ProgressBar;
    private javax.swing.JMenuBar jMenuBar_Main;
    private javax.swing.JMenuItem jMenuItem_About;
    private javax.swing.JMenuItem jMenuItem_AttributeData;
    private javax.swing.JMenuItem jMenuItem_ClearSelection;
    private javax.swing.JMenuItem jMenuItem_Help;
    private javax.swing.JMenuItem jMenuItem_InsertLegend;
    private javax.swing.JMenuItem jMenuItem_InsertMapFrame;
    private javax.swing.JMenuItem jMenuItem_InsertNorthArrow;
    private javax.swing.JMenuItem jMenuItem_InsertScaleBar;
    private javax.swing.JMenuItem jMenuItem_InsertText;
    private javax.swing.JMenuItem jMenuItem_InsertTitle;
    private javax.swing.JMenuItem jMenuItem_InsertWindArrow;
    private javax.swing.JMenuItem jMenuItem_Layers;
    private javax.swing.JMenuItem jMenuItem_LayoutProperty;
    private javax.swing.JMenuItem jMenuItem_MapProperty;
    private javax.swing.JMenuItem jMenuItem_MaskOut;
    private javax.swing.JMenuItem jMenuItem_Open;
    private javax.swing.JMenuItem jMenuItem_Options;
    private javax.swing.JMenuItem jMenuItem_OutputMapData;
    private javax.swing.JMenuItem jMenuItem_AddXYData;
    private javax.swing.JMenuItem jMenuItem_Animator;
    private javax.swing.JMenuItem jMenuItem_PluginManager;
    private javax.swing.JMenuItem jMenuItem_Projection;
    private javax.swing.JMenuItem jMenuItem_Save;
    private javax.swing.JMenuItem jMenuItem_SaveAs;
    private javax.swing.JMenuItem jMenuItem_Script;
    //private javax.swing.JMenuItem jMenuItem_ScriptConsole;
    private javax.swing.JMenu jMenu_NetCDFData;
    private javax.swing.JMenuItem jMenuItem_JoinNCFiles;
    private javax.swing.JMenuItem jMenuItem_SelByAttr;
    private javax.swing.JMenuItem jMenuItem_SelByLocation;
    private javax.swing.JMenuItem jMenuItem_Undo;
    private javax.swing.JMenuItem jMenuItem_Redo;
    private javax.swing.JMenuItem jMenuItem_Cut;
    private javax.swing.JMenuItem jMenuItem_Copy;
    private javax.swing.JMenuItem jMenuItem_Paste;
    private javax.swing.JMenuItem jMenuItem_NewLayer;
    private javax.swing.JMenuItem jMenuItem_AddRing;
    private javax.swing.JMenuItem jMenuItem_FillRing;
    private javax.swing.JMenuItem jMenuItem_DeleteRing;
    private javax.swing.JMenuItem jMenuItem_ReformFeature;
    private javax.swing.JMenuItem jMenuItem_SplitFeature;
    private javax.swing.JMenuItem jMenuItem_MergeFeature;
    private javax.swing.JMenu jMenu_Help;
    private javax.swing.JMenu jMenu_Insert;
    private javax.swing.JMenu jMenu_Plugin;
    private javax.swing.JMenu jMenu_Project;
    private javax.swing.JMenu jMenu_Selection;
    private javax.swing.JMenu jMenu_Tools;
    private javax.swing.JMenu jMenu_View;
    private javax.swing.JMenu jMenu_Edit;
    private javax.swing.JMenu jMenu_GeoProcessing;
    private javax.swing.JMenuItem jMenuItem_Buffer;
    private javax.swing.JMenuItem jMenuItem_Clipping;
    private javax.swing.JMenuItem jMenuItem_Convexhull;
    private javax.swing.JMenuItem jMenuItem_Intersection;
    private javax.swing.JMenuItem jMenuItem_Difference;
    private javax.swing.JMenuItem jMenuItem_SymDifference;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel_Status;
    private javax.swing.JPanel jPanel_LayoutTab;
    private javax.swing.JPanel jPanel_MainToolBar;
    private javax.swing.JPanel jPanel_MapTab;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    //private javax.swing.JToolBar.Separator jSeparator19;
    //private javax.swing.JToolBar.Separator jSeparator20;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane_Main;
    private javax.swing.JToolBar jToolBar_Base;
    private javax.swing.JToolBar jToolBar_Graphic;
    private javax.swing.JToolBar jToolBar_Layout;
    private javax.swing.JToolBar jToolBar_Edit;
    private javax.swing.JProgressBar jProgressBar_Main;
}
