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

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.global.event.ActiveMapFrameChangedEvent;
import org.meteoinfo.global.event.ElementSelectedEvent;
import org.meteoinfo.global.event.IActiveMapFrameChangedListener;
import org.meteoinfo.global.event.IElementSelectedListener;
import org.meteoinfo.global.event.IMapFramesUpdatedListener;
import org.meteoinfo.global.event.IMapViewUpdatedListener;
import org.meteoinfo.global.event.IZoomChangedListener;
import org.meteoinfo.global.event.MapFramesUpdatedEvent;
import org.meteoinfo.global.event.MapViewUpdatedEvent;
import org.meteoinfo.global.event.ZoomChangedEvent;
import org.meteoinfo.global.FrmMeasurement;
import org.meteoinfo.global.FrmMeasurement.MeasureTypes;
import org.meteoinfo.global.FrmProperty;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.PointF;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.BreakTypes;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.FrmLabelSymbolSet;
import org.meteoinfo.legend.FrmPointSymbolSet;
import org.meteoinfo.legend.FrmPolygonSymbolSet;
import org.meteoinfo.legend.FrmPolylineSymbolSet;
import org.meteoinfo.legend.GridLabelPosition;
import org.meteoinfo.legend.LabelBreak;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.map.FrmIdentifer;
import org.meteoinfo.shape.CircleShape;
import org.meteoinfo.shape.CurveLineShape;
import org.meteoinfo.shape.CurvePolygonShape;
import org.meteoinfo.shape.EllipseShape;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.RectangleShape;
import org.meteoinfo.shape.ShapeTypes;
import org.meteoinfo.shape.WindArrow;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.UndoableEdit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.meteoinfo.data.mapdata.webmap.IWebMapPanel;
import org.meteoinfo.data.mapdata.webmap.TileLoadListener;
import org.meteoinfo.global.event.IUndoEditListener;
import org.meteoinfo.global.event.UndoEditEvent;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.image.ImageUtil;
import org.meteoinfo.layer.RasterLayer;
import org.meteoinfo.layer.WebMapLayer;
import org.meteoinfo.legend.VectorBreak;
import org.meteoinfo.map.FrmIdentiferGrid;
import org.meteoinfo.map.MapView;
import org.meteoinfo.shape.Shape;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author yaqiang
 */
public class MapLayout extends JPanel implements IWebMapPanel {

    // <editor-fold desc="Variables">
    private final EventListenerList _listeners = new EventListenerList();
    private final TileLoadListener tileLoadListener = new TileLoadListener(this);
    private FrmIdentifer frmIdentifier = null;
    private FrmIdentiferGrid frmIdentifierGrid = null;
    private FrmMeasurement _frmMeasure = null;
    private JScrollBar _vScrollBar;
    private JScrollBar _hScrollBar;
    private boolean _lockViewUpdate = false;
    private List<MapFrame> _mapFrames = new ArrayList<>();
    private final List<LayoutElement> _layoutElements = new ArrayList<>();
    private LayoutMap _currentLayoutMap;
    private BufferedImage _layoutBitmap = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage _tempImage = null;
    private boolean newPaint = false;
    private boolean doubleBuffer = true;
    private boolean _antiAlias = false;
    private FrmLabelSymbolSet _frmLabelSymbolSet = null;
    private FrmPointSymbolSet _frmPointSymbolSet = null;
    private FrmPolylineSymbolSet _frmPolylineSymbolSet = null;
    private FrmPolygonSymbolSet _frmPolygonSymbolSet = null;
    private Rectangle _pageBounds;
    private Color _pageForeColor = Color.black;
    private Color _pageBackColor = Color.white;
    private PaperSize _paperSize = new PaperSize();
    private final List<PaperSize> _paperSizeList = new ArrayList<>();
    private boolean _isLandscape;
    private float _zoom = 1.0f;
    private PointF _pageLocation = new PointF(0, 0);
    private PointBreak _defPointBreak = new PointBreak();
    private LabelBreak _defLabelBreak = new LabelBreak();
    private PolylineBreak _defPolylineBreak = new PolylineBreak();
    private PolygonBreak _defPolygonBreak = new PolygonBreak();
    private final int _xShift = 0;
    private final int _yShift = 0;
    private MouseMode _mouseMode = MouseMode.Default;
    private List<LayoutElement> _selectedElements = new ArrayList<>();
    private Rectangle _selectedRectangle = new Rectangle();
    private final Point _mouseDownPos = new Point(0, 0);
    private Point _mouseLastPos = new Point(0, 0);
    private final Point _mouseDownPoint = new Point(0, 0);
    private Edge _resizeSelectedEdge = Edge.None;
    private boolean _startNewGraphic = true;
    private List<PointF> _graphicPoints = new ArrayList<>();
    private final List<PointD> _editingVertices = new ArrayList<>();
    private int _editingVerticeIndex;
    private boolean _dragMode = false;
    // </editor-fold>

    // <editor-fold desc="Constructor">
    public MapLayout() {
        super();
        this.setLayout(new BorderLayout());
        initComponents();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onComponentResized(e);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    onMouseClicked(e);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(MapLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                    Logger.getLogger(MapLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }
        });
//        this.addMouseWheelListener(new MouseWheelListener() {
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                onMouseWheelMoved(e);
//            }
//        });
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                onKeyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        this.setBackground(Color.lightGray);
        this.setForeground(Color.black);
        //Set page        
        PaperSize aPS = new PaperSize("A4", 827, 1169);
        _paperSizeList.add(aPS);
        aPS = new PaperSize("Letter", 850, 1100);
        _paperSizeList.add(aPS);
        aPS = new PaperSize("A5", 583, 827);
        _paperSizeList.add(aPS);
        aPS = new PaperSize("Custom", 500, 750);
        _paperSizeList.add(aPS);
        _isLandscape = true;

        //Set default size
        _pageBounds = new Rectangle();
        _pageBounds.x = 0;
        _pageBounds.y = 0;
        _pageBounds.width = 730;
        _pageBounds.height = 480;
        _zoom = 1.0F;
        this.setPaperSize(aPS);

        //Add a default map frame
        MapFrame aMF = new MapFrame();
        aMF.setActive(true);
        aMF.setLayoutBounds(new Rectangle(40, 36, 606, 420));
        _mapFrames.add(aMF);
        LayoutMap layoutMap = new LayoutMap(aMF, this.tileLoadListener);
        this.addElement(layoutMap);

        _defPointBreak.setSize(10);
        _defLabelBreak.setText("Text");
        _defLabelBreak.setFont(new Font(GlobalUtil.getDefaultFontName(), Font.PLAIN, 12));
        _defPolylineBreak.setColor(Color.red);
        _defPolylineBreak.setWidth(2);
        _defPolygonBreak.setColor(new Color(104, 255, 104, 125));
    }

    private void initComponents() {
        _vScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        _vScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                onScrollValueChanged(e);
            }
        });
        this.add(_vScrollBar, BorderLayout.EAST);

        _hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        _hScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                onScrollValueChanged(e);
            }
        });
        this.add(_hScrollBar, BorderLayout.SOUTH);

        this._vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);
        if (this._hScrollBar.isVisible()) {
            this._vScrollBar.setSize(21, this.getHeight() - 21);
        } else {
            this._vScrollBar.setSize(21, this.getHeight());
        }

        this._hScrollBar.setLocation(0, this.getHeight() - this._hScrollBar.getHeight());
        if (this._vScrollBar.isVisible()) {
            this._hScrollBar.setSize(this.getWidth() - 21, 21);
        } else {
            this._hScrollBar.setSize(this.getWidth(), 21);
        }
    }
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
    }

    public void addElementSelectedListener(IElementSelectedListener listener) {
        this._listeners.add(IElementSelectedListener.class, listener);
    }

    public void removeElementSelectedListener(IElementSelectedListener listener) {
        this._listeners.remove(IElementSelectedListener.class, listener);
    }

    public void fireElementSelectedEvent() {
        fireElementSelectedEvent(new ElementSelectedEvent(this));
    }

    private void fireElementSelectedEvent(ElementSelectedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IElementSelectedListener.class) {
                ((IElementSelectedListener) listeners[i + 1]).elementSelectedEvent(event);
            }
        }
    }

    public void addZoomChangedListener(IZoomChangedListener listener) {
        this._listeners.add(IZoomChangedListener.class, listener);
    }

    public void removeZoomChangedListener(IZoomChangedListener listener) {
        this._listeners.remove(IZoomChangedListener.class, listener);
    }

    public void fireZoomChangedEvent() {
        fireZoomChangedEvent(new ZoomChangedEvent(this));
    }

    private void fireZoomChangedEvent(ZoomChangedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IZoomChangedListener.class) {
                ((IZoomChangedListener) listeners[i + 1]).zoomChangedEvent(event);
            }
        }
    }

    public void addUndoEditListener(IUndoEditListener listener) {
        this._listeners.add(IUndoEditListener.class, listener);
    }

    public void removeUndoEditListener(IUndoEditListener listener) {
        this._listeners.remove(IUndoEditListener.class, listener);
    }

    public void fireUndoEditEvent(UndoableEdit undoEdit) {
        fireUndoEditEvent(new UndoEditEvent(this), undoEdit);
    }

    private void fireUndoEditEvent(UndoEditEvent event, UndoableEdit undoEdit) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IUndoEditListener.class) {
                ((IUndoEditListener) listeners[i + 1]).undoEditEvent(event, undoEdit);
            }
        }
    }

    public void onScrollValueChanged(AdjustmentEvent e) {
        if (e.getSource() == _vScrollBar) {
            //_vScrollBar.setValue(e.getValue());            
            //this._yShift = - this._vScrollBar.getValue();
            int y = -e.getValue();
            if (y == 1) {
                y = 0;
            }
            this._pageLocation.Y = y;
        }
        if (e.getSource() == _hScrollBar) {
            //_hScrollBar.setValue(e.getValue());       
            //this._xShift = - this._hScrollBar.getValue();
            int x = -e.getValue();
            if (x == 1) {
                x = 0;
            }
            this._pageLocation.X = x;
        }
        //this.paintGraphics();
        this.repaintNew();
        //this.repaint();
    }

    void onComponentResized(ComponentEvent e) {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            this._vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);
            if (this._hScrollBar.isVisible()) {
                this._vScrollBar.setSize(this._vScrollBar.getWidth(), this.getHeight() - this._vScrollBar.getWidth());
            } else {
                this._vScrollBar.setSize(this._vScrollBar.getWidth(), this.getHeight());
            }

            this._hScrollBar.setLocation(0, this.getHeight() - this._hScrollBar.getHeight());
            if (this._vScrollBar.isVisible()) {
                this._hScrollBar.setSize(this.getWidth() - this._hScrollBar.getHeight(), this._hScrollBar.getHeight());
            } else {
                this._hScrollBar.setSize(this.getWidth(), this._hScrollBar.getHeight());
            }

            //this.paintGraphics();
            this.repaintNew();
        }
    }

    void onMousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            //int left = e.getX();
            //int top = e.getY();
            Point pageP = screenToPage(e.getX(), e.getY());
            Graphics2D g = (Graphics2D) this.getGraphics();
            LayoutMap aLM = getLayoutMap(pageP);
            if (aLM != null) {
                _currentLayoutMap = aLM;
            }

            switch (_mouseMode) {
                case Map_Pan:
                    Rectangle mapRect = pageToScreen(_currentLayoutMap.getBounds());
                    _tempImage = new BufferedImage(mapRect.width - 2,
                            mapRect.height - 2, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D tg = _tempImage.createGraphics();
                    tg.setColor(_currentLayoutMap.getMapFrame().getMapView().getBackground());
                    tg.fill(mapRect);
                    tg.drawImage(_layoutBitmap, -mapRect.x - 1, -mapRect.y - 1, this);
                    tg.dispose();
                    break;
                case Select:
                    List<LayoutElement> tempGraphics = selectElements(pageP, _selectedElements, 3);
                    if (tempGraphics.size() > 0) {
                        _selectedRectangle = (Rectangle) _selectedElements.get(0).getBounds().clone();
                        _selectedRectangle = pageToScreen(_selectedRectangle);
                        if (_resizeSelectedEdge == Edge.None) {
                            _mouseMode = MouseMode.MoveSelection;
                        } else {
                            _mouseMode = MouseMode.ResizeSelected;
                        }
                    } else {
                        _mouseMode = MouseMode.CreateSelection;
                    }

                    break;
                case New_Point:
                    PointShape aPS = new PointShape();
                    aPS.setPoint(new PointD(pageP.x, pageP.y));
                    Graphic aGraphic = new Graphic(aPS, (PointBreak) _defPointBreak.clone());
                    LayoutGraphic aLayoutGraphic = new LayoutGraphic(aGraphic, this);
                    addElement(aLayoutGraphic);
                    //this.paintGraphics();
                    this.repaintNew();
                    UndoableEdit edit = (new MapLayoutUndoRedo()).new AddElementEdit(this, aLayoutGraphic);
                    this.fireUndoEditEvent(edit);
                    break;
                case New_Label:
                    aPS = new PointShape();
                    aPS.setPoint(new PointD(pageP.x, pageP.y));
                    aGraphic = new Graphic(aPS, (LabelBreak) _defLabelBreak.clone());
                    aLayoutGraphic = new LayoutGraphic(aGraphic, this);
                    addElement(aLayoutGraphic);
                    //this.paintGraphics();
                    this.repaintNew();
                    edit = (new MapLayoutUndoRedo()).new AddElementEdit(this, aLayoutGraphic);
                    this.fireUndoEditEvent(edit);
                    break;
                case New_Polyline:
                case New_Polygon:
                case New_Curve:
                case New_CurvePolygon:
                case New_Freehand:
                case Map_SelectFeatures_Polygon:
                case Map_SelectFeatures_Lasso:
                    if (_startNewGraphic) {
                        _graphicPoints = new ArrayList<>();
                        _startNewGraphic = false;
                    }
                    _graphicPoints.add(new PointF(e.getX(), e.getY()));
                    break;
                case EditVertices:
                    if (_selectedElements.size() > 0) {
                        _editingVerticeIndex = selectEditVertices(pageP, ((LayoutGraphic) _selectedElements.get(0)).getGraphic().getShape(),
                                _editingVertices);
                        if (_editingVerticeIndex >= 0) {
                            _mouseMode = MouseMode.InEditingVertices;
                        }
                    }
                    break;
                case Map_Measurement:
                    if (_frmMeasure == null) {
                        break;
                    }
                    if (_frmMeasure.isVisible()) {
                        switch (_frmMeasure.getMeasureType()) {
                            case Length:
                            case Area:
                                if (_startNewGraphic) {
                                    _graphicPoints = new ArrayList<>();
                                    _startNewGraphic = false;
                                }
                                _frmMeasure.setPreviousValue(_frmMeasure.getTotalValue());
                                _graphicPoints.add(new PointF(e.getX(), e.getY()));
                                break;
                            case Feature:
                                MapLayer aMLayer = _currentLayoutMap.getMapFrame().getMapView().getSelectedLayer();
                                if (aMLayer != null) {
                                    if (aMLayer.getLayerType() == LayerTypes.VectorLayer) {
                                        VectorLayer aLayer = (VectorLayer) aMLayer;
                                        if (aLayer.getShapeType() != ShapeTypes.Point) {
                                            PointF mapP = pageToScreen(_currentLayoutMap.getLeft(), _currentLayoutMap.getTop());
                                            PointF aPoint = new PointF(e.getX() - mapP.X, e.getY() - mapP.Y);
                                            List<Integer> selectedShapes = _currentLayoutMap.getMapFrame().getMapView().selectShapes(aLayer, aPoint);
                                            if (selectedShapes.size() > 0) {
                                                int shapeIdx = selectedShapes.get(0);
                                                org.meteoinfo.shape.Shape aShape = aLayer.getShapes().get(shapeIdx);
                                                aLayer.setIdentiferShape(shapeIdx);
                                                _currentLayoutMap.getMapFrame().getMapView().setDrawIdentiferShape(true);
                                                //this.repaint();
                                                this.repaintOld();
                                                //_currentLayoutMap.getMapFrame().getMapView().drawIdShape((Graphics2D) this.getGraphics(), aLayer.getShapes().get(shapeIdx), rect);
                                                double value = 0.0;
                                                switch (aShape.getShapeType()) {
                                                    case Polyline:
                                                    case PolylineZ:
                                                        _frmMeasure.setArea(false);
                                                        if (_currentLayoutMap.getMapFrame().getMapView().getProjection().isLonLatMap()) {
                                                            value = GeoComputation.getDistance(((PolylineShape) aShape).getPoints(), true);
                                                        } else {
                                                            value = ((PolylineShape) aShape).getLength();
                                                            value *= _currentLayoutMap.getMapFrame().getMapView().getProjection().getProjInfo().getCoordinateReferenceSystem().getProjection().getFromMetres();
                                                        }
                                                        break;
                                                    case Polygon:
                                                    case PolygonM:
                                                    case PolygonZ:
                                                        _frmMeasure.setArea(true);
                                                        if (_currentLayoutMap.getMapFrame().getMapView().getProjection().isLonLatMap()) {
                                                            value = ((PolygonShape) aShape).getSphericalArea();
                                                        } else {
                                                            value = ((PolygonShape) aShape).getArea();
                                                        }
                                                        value *= _currentLayoutMap.getMapFrame().getMapView().getProjection().getProjInfo().getCoordinateReferenceSystem().getProjection().getFromMetres()
                                                                * _currentLayoutMap.getMapFrame().getMapView().getProjection().getProjInfo().getCoordinateReferenceSystem().getProjection().getFromMetres();
                                                        break;
                                                }
                                                _frmMeasure.setCurrentValue(value);
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                    }
                    break;
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            switch (_mouseMode) {
                case Map_Measurement:
                    if (_frmMeasure.isVisible()) {
                        switch (_frmMeasure.getMeasureType()) {
                            case Length:
                            case Area:
                                _startNewGraphic = true;
                                _frmMeasure.setTotalValue(0);
                                break;
                        }
                    }
                    break;
            }
        }

        _mouseDownPoint.x = e.getX();
        _mouseDownPoint.y = e.getY();
        _mouseLastPos = new Point(_mouseDownPoint.x, _mouseDownPoint.y);
    }

    void onMouseDragged(MouseEvent e) {
        _dragMode = true;
        int deltaX = e.getX() - _mouseLastPos.x;
        int deltaY = e.getY() - _mouseLastPos.y;
        _mouseLastPos.x = e.getX();
        _mouseLastPos.y = e.getY();

        Point pageP = screenToPage(e.getX(), e.getY());

        Graphics2D g = (Graphics2D) this.getGraphics();
        //Pen aPen = new Pen(Color.Red);
        //aPen.DashStyle = DashStyle.Dash;
        //Rectangle rect = new Rectangle();
        _vScrollBar.setCursor(Cursor.getDefaultCursor());
        _hScrollBar.setCursor(Cursor.getDefaultCursor());
        //this.setCursor(Cursor.getDefaultCursor());

        switch (_mouseMode) {
            case Map_ZoomIn:
                if (isInLayoutMaps(pageP)) {
                    this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().
                            getImage(this.getClass().getResource("/images/zoom_in_32x32x32.png")), new Point(8, 8), "Zoom In"));

                    //this.repaint();
                    this.repaintOld();
                }
                break;
            case Map_Pan:
                if (isInLayoutMaps(pageP)) {
                    this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().
                            getImage(this.getClass().getResource("/images/Pan_Open_32x32x32.png")), new Point(8, 8), "Pan"));

                    Rectangle mapRect = pageToScreen(_currentLayoutMap.getBounds());
                    g.setClip(mapRect);
                    Color aColor = _currentLayoutMap.getMapFrame().getBackColor();
                    if (aColor.getAlpha() == 255) {
                        aColor = Color.white;
                    }
                    g.setColor(aColor);
                    int aX = e.getX() - _mouseDownPoint.x;
                    int aY = e.getY() - _mouseDownPoint.y;
                    aX = (int) (aX / _zoom);
                    aY = (int) (aY / _zoom);
                    if (aX > 0) {
                        if (mapRect.x >= 0) {
                            g.fillRect(mapRect.x, mapRect.y, aX, mapRect.height);
                        } else {
                            g.fillRect(0, mapRect.y, aX, mapRect.height);
                        }
                    } else {
                        if (mapRect.x <= this.getWidth()) {
                            g.fillRect(mapRect.x + mapRect.width + aX, mapRect.y, Math.abs(aX), mapRect.height);
                        } else {
                            g.fillRect(this.getWidth() + aX, mapRect.y, Math.abs(aX), mapRect.height);
                        }
                    }
                    if (aY > 0) {
                        if (mapRect.y >= 0) {
                            g.fillRect(mapRect.x, mapRect.y, mapRect.width, aY);
                        } else {
                            g.fillRect(mapRect.x, 0, mapRect.width, aY);
                        }
                    } else {
                        if (mapRect.y + mapRect.height <= this.getX() + this.getHeight()) {
                            g.fillRect(mapRect.x, mapRect.y + mapRect.height + aY, mapRect.width, Math.abs(aY));
                        } else {
                            g.fillRect(mapRect.x, this.getY() + this.getHeight() + aY, mapRect.width, Math.abs(aY));
                        }
                    }
                    int startX = mapRect.x + aX;
                    int startY = mapRect.y + aY;
                    AffineTransformOp aop = new AffineTransformOp(new AffineTransform(), AffineTransformOp.TYPE_BILINEAR);
                    g.drawImage(_tempImage, aop, startX, startY);
                    //g.drawImage(_tempImage, startX, startY, this);
                    g.setColor(this.getForeground());
                    g.draw(mapRect);
                }
                break;
            case Map_SelectFeatures_Rectangle:
                //this.repaint();
                this.repaintOld();
                break;
            case MoveSelection:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                //this.repaint();
                this.repaintOld();
                break;
            case ResizeSelected:
                LayoutElement oElement = _selectedElements.get(0);
                switch (oElement.getResizeAbility()) {
                    case SameWidthHeight:
                        switch (_resizeSelectedEdge) {
                            case TopLeft:
                                _selectedRectangle.x += deltaX;
                                _selectedRectangle.y += deltaX;
                                _selectedRectangle.width -= deltaX;
                                _selectedRectangle.height -= deltaX;
                                break;
                            case BottomRight:
                                _selectedRectangle.width += deltaX;
                                _selectedRectangle.height += deltaX;
                                break;
                            case TopRight:
                                _selectedRectangle.y += deltaY;
                                _selectedRectangle.width -= deltaY;
                                _selectedRectangle.height -= deltaY;
                                break;
                            case BottomLeft:
                                _selectedRectangle.x += deltaX;
                                _selectedRectangle.width -= deltaX;
                                _selectedRectangle.height -= deltaX;
                                break;
                        }
                        break;
                    case ResizeAll:
                        switch (_resizeSelectedEdge) {
                            case TopLeft:
                                _selectedRectangle.x += deltaX;
                                _selectedRectangle.y += deltaY;
                                _selectedRectangle.width -= deltaX;
                                _selectedRectangle.height -= deltaY;
                                break;
                            case BottomRight:
                                _selectedRectangle.width += deltaX;
                                _selectedRectangle.height += deltaY;
                                break;
                            case Top:
                                _selectedRectangle.y += deltaY;
                                _selectedRectangle.height -= deltaY;
                                break;
                            case Bottom:
                                _selectedRectangle.height += deltaY;
                                break;
                            case TopRight:
                                _selectedRectangle.y += deltaY;
                                _selectedRectangle.width += deltaX;
                                _selectedRectangle.height -= deltaY;
                                break;
                            case BottomLeft:
                                _selectedRectangle.x += deltaX;
                                _selectedRectangle.width -= deltaX;
                                _selectedRectangle.height += deltaY;
                                break;
                            case Left:
                                _selectedRectangle.x += deltaX;
                                _selectedRectangle.width -= deltaX;
                                break;
                            case Right:
                                _selectedRectangle.width += deltaX;
                                break;
                        }
                        break;
                }
                //this.repaint();
                this.repaintOld();
                break;
            case New_Rectangle:
            case New_Ellipse:
            case New_Freehand:
            case New_Circle:
            case Map_SelectFeatures_Polygon:
            case Map_SelectFeatures_Lasso:
            case Map_SelectFeatures_Circle:
                //this.repaint();
                this.repaintOld();
                break;
            case InEditingVertices:
                //this.repaint();
                this.repaintOld();
                break;
        }
    }

    void onMouseMoved(MouseEvent e) {
        //int deltaX = e.getX() - _mouseLastPos.x;
        //int deltaY = e.getY() - _mouseLastPos.y;
        _mouseLastPos.x = e.getX();
        _mouseLastPos.y = e.getY();

        Point pageP = screenToPage(e.getX(), e.getY());

        Graphics2D g = (Graphics2D) this.getGraphics();
        //Pen aPen = new Pen(Color.Red);
        //aPen.DashStyle = DashStyle.Dash;
        //Rectangle rect = new Rectangle();
        _vScrollBar.setCursor(Cursor.getDefaultCursor());
        _hScrollBar.setCursor(Cursor.getDefaultCursor());
        //this.setCursor(Cursor.getDefaultCursor());

        switch (_mouseMode) {
            case Map_ZoomIn:
                if (isInLayoutMaps(pageP)) {
                    this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().
                            getImage(this.getClass().getResource("/images/zoom_in_32x32x32.png")), new Point(8, 8), "Zoom In"));
                }
                break;
            case Map_ZoomOut:
                if (isInLayoutMaps(pageP)) {
                    this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().
                            getImage(this.getClass().getResource("/images/zoom_out_32x32x32.png")), new Point(8, 8), "Zoom Out"));
                }
                break;
            case Map_Pan:
                if (isInLayoutMaps(pageP)) {
                    this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().
                            getImage(this.getClass().getResource("/images/Pan_Open_32x32x32.png")), new Point(8, 8), "Pan"));
                }
                break;
            case Map_Identifer:
                if (isInLayoutMaps(pageP)) {
                    this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().
                            getImage(this.getClass().getResource("/images/identifer_32x32x32.png")), new Point(8, 8), "Identifer"));
                }
                break;
            case Map_SelectFeatures_Rectangle:
                //case Map_SelectFeatures_Polygon:
                //case Map_SelectFeatures_Lasso:
                //case Map_SelectFeatures_Circle:
                if (isInLayoutMaps(pageP)) {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
                break;
            case Select:
                if (_selectedElements.size() > 0) {
                    List<LayoutElement> tempElements = selectElements(pageP, _selectedElements, 3);
                    if (tempElements.size() > 0) {
                        //Change mouse cursor    
                        Rectangle aRect = (Rectangle) _selectedElements.get(0).getBounds().clone();
                        _resizeSelectedEdge = intersectElementEdge(aRect, new PointF(pageP.x, pageP.y), 3F);
                        switch (_selectedElements.get(0).getResizeAbility()) {
                            case SameWidthHeight:
                                switch (_resizeSelectedEdge) {
                                    case TopLeft:
                                    case BottomRight:
                                        this.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                                        break;
                                    case TopRight:
                                    case BottomLeft:
                                        this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                                        break;
                                    default:
                                        this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                                        break;
                                }
                                break;
                            case ResizeAll:
                                switch (_resizeSelectedEdge) {
                                    case TopLeft:
                                    case BottomRight:
                                        this.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                                        break;
                                    case Top:
                                    case Bottom:
                                        this.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                                        break;
                                    case TopRight:
                                    case BottomLeft:
                                        this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                                        break;
                                    case Left:
                                    case Right:
                                        this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                                        break;
                                    case None:
                                        this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                                        break;
                                }
                                break;
                            default:
                                this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                                break;
                        }
                    } else {
                        this.setCursor(Cursor.getDefaultCursor());
                    }
                } else {
                    this.setCursor(Cursor.getDefaultCursor());
                }

                break;
            case MoveSelection:
                break;
            case ResizeSelected:
                break;
            case New_Polyline:
            case New_Polygon:
            case New_Curve:
            case New_CurvePolygon:
            case Map_SelectFeatures_Polygon:
                if (!_startNewGraphic) {
                    //this.repaint();
                    this.repaintOld();
                }
                break;
            case EditVertices:
                if (_selectedElements.size() > 0) {
                    _editingVerticeIndex = selectEditVertices(pageP, ((LayoutGraphic) _selectedElements.get(0)).getGraphic().getShape(),
                            _editingVertices);
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    if (_editingVerticeIndex >= 0) {
                        this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().
                                getImage(this.getClass().getResource("/images/VertexEdit_32x32x32.png")), new Point(8, 8), "Vertices edit"));
                    } else {
                        Image image = toolkit.getImage(this.getClass().getResource("/images/Edit_tool.png"));
                        this.setCursor(toolkit.createCustomCursor(image, new Point(2, 2), "Edit Tool"));
                    }
                }
                break;
            case Map_Measurement:
                if (isInLayoutMaps(pageP)) {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }

                if (_frmMeasure == null) {
                    break;
                }
                if (_frmMeasure.isVisible()) {
                    switch (_frmMeasure.getMeasureType()) {
                        case Length:
                        case Area:
                            if (!_startNewGraphic) {
                                //Draw graphic                                    
                                //g.SmoothingMode = SmoothingMode.AntiAlias;
                                //this.repaint();
                                this.repaintOld();
                                PointF[] fpoints = (PointF[]) _graphicPoints.toArray(new PointF[_graphicPoints.size()]);
                                PointF[] points = new PointF[fpoints.length + 1];
                                System.arraycopy(fpoints, 0, points, 0, fpoints.length);
                                points[_graphicPoints.size()] = new PointF(e.getX(), e.getY());

                                //Calculate             
                                PointF mapP = pageToScreen(_currentLayoutMap.getLeft(), _currentLayoutMap.getTop());
                                PointF aPoint = new PointF(e.getX() - mapP.X, e.getY() - mapP.Y);
                                float[] pXY = _currentLayoutMap.getMapFrame().getMapView().screenToProj(aPoint.X, aPoint.Y);
                                if (_frmMeasure.getMeasureType() == MeasureTypes.Length) {
                                    aPoint = new PointF(_mouseDownPoint.x - mapP.X, _mouseDownPoint.y - mapP.Y);
                                    float[] pPXY = _currentLayoutMap.getMapFrame().getMapView().screenToProj(aPoint.X, aPoint.Y);
                                    double dx = Math.abs(pXY[0] - pPXY[0]);
                                    double dy = Math.abs(pXY[1] - pPXY[1]);
                                    double dist;
                                    if (_currentLayoutMap.getMapFrame().getMapView().getProjection().isLonLatMap()) {
                                        double y = (pXY[1] + pPXY[1]) / 2;
                                        double factor = Math.cos(y * Math.PI / 180);
                                        dx *= factor;
                                        dist = Math.sqrt(dx * dx + dy * dy);
                                        dist = dist * 111319.5;
                                    } else {
                                        dist = Math.sqrt(dx * dx + dy * dy);
                                        dist *= _currentLayoutMap.getMapFrame().getMapView().getProjection().getProjInfo().getCoordinateReferenceSystem().getProjection().getFromMetres();
                                    }

                                    _frmMeasure.setCurrentValue(dist);
                                } else {
                                    List<PointD> mPoints = new ArrayList<>();
                                    for (int i = 0; i < points.length; i++) {
                                        aPoint = new PointF(points[i].X - mapP.X, points[i].Y - mapP.Y);
                                        pXY = _currentLayoutMap.getMapFrame().getMapView().screenToProj(aPoint.X, aPoint.Y);
                                        mPoints.add(new PointD(pXY[0], pXY[1]));
                                    }
                                    double area = GeoComputation.getArea(mPoints);
                                    if (_currentLayoutMap.getMapFrame().getMapView().getProjection().isLonLatMap()) {
                                        area = area * 111319.5 * 111319.5;
                                    } else {
                                        area *= _currentLayoutMap.getMapFrame().getMapView().getProjection().getProjInfo().getCoordinateReferenceSystem().getProjection().getFromMetres()
                                                * _currentLayoutMap.getMapFrame().getMapView().getProjection().getProjInfo().getCoordinateReferenceSystem().getProjection().getFromMetres();
                                    }
                                    _frmMeasure.setCurrentValue(area);
                                }
                            }
                            break;
                    }
                }
                break;
        }
    }

    void onMouseReleased(MouseEvent e) throws CloneNotSupportedException {
        _dragMode = false;
        double MinX, MaxX, MinY, MaxY, ZoomF;
        Point pageP = screenToPage(e.getX(), e.getY());
        switch (_mouseMode) {
            case Map_ZoomIn:
                MinX = Math.min(e.getX(), _mouseDownPoint.x);
                MinY = Math.min(e.getY(), _mouseDownPoint.y);
                MaxX = Math.max(e.getX(), _mouseDownPoint.x);
                MaxY = Math.max(e.getY(), _mouseDownPoint.y);
                if (MaxX - MinX < 5) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        ZoomF = 0.75;
                    } else {
                        ZoomF = 1.5;
                    }
                    MinX = pageP.x - (_currentLayoutMap.getWidth() / 2 * ZoomF);
                    MaxX = pageP.x + (_currentLayoutMap.getWidth() / 2 * ZoomF);
                    MinY = pageP.y - (_currentLayoutMap.getHeight() / 2 * ZoomF);
                    MaxY = pageP.y + (_currentLayoutMap.getHeight() / 2 * ZoomF);
                } else {
                    PointF minP = screenToPage((float) MinX, (float) MinY);
                    PointF maxP = screenToPage((float) MaxX, (float) MaxY);
                    MinX = minP.X;
                    MinY = minP.Y;
                    MaxX = maxP.X;
                    MaxY = maxP.Y;
                }

                MinX -= _currentLayoutMap.getLeft();
                MinY -= _currentLayoutMap.getTop();
                MaxX -= _currentLayoutMap.getLeft();
                MaxY -= _currentLayoutMap.getTop();
                if (MaxX - MinX > 0.001) {
                    _currentLayoutMap.getMapFrame().getMapView().zoomToExtentScreen(MinX, MaxX, MinY, MaxY, _zoom);
                }
                break;
            case Map_ZoomOut:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ZoomF = 1.5;
                } else {
                    ZoomF = 0.75;
                }
                MinX = pageP.x - (_currentLayoutMap.getWidth() / 2 * ZoomF);
                MaxX = pageP.x + (_currentLayoutMap.getWidth() / 2 * ZoomF);
                MinY = pageP.y - (_currentLayoutMap.getHeight() / 2 * ZoomF);
                MaxY = pageP.y + (_currentLayoutMap.getHeight() / 2 * ZoomF);

                MinX -= _currentLayoutMap.getLeft();
                MinY -= _currentLayoutMap.getTop();
                MaxX -= _currentLayoutMap.getLeft();
                MaxY -= _currentLayoutMap.getTop();
                if (MaxX - MinX > 0.001) {
                    _currentLayoutMap.getMapFrame().getMapView().zoomToExtentScreen(MinX, MaxX, MinY, MaxY, _zoom);
                }
                break;
            case Map_Pan:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int deltaX = e.getX() - _mouseDownPoint.x;
                    int deltaY = e.getY() - _mouseDownPoint.y;
                    deltaX = (int) (deltaX / _zoom);
                    deltaY = (int) (deltaY / _zoom);
                    MinX = -deltaX;
                    MinY = -deltaY;
                    MaxX = _currentLayoutMap.getWidth() - deltaX;
                    MaxY = _currentLayoutMap.getHeight() - deltaY;
                    _currentLayoutMap.getMapFrame().getMapView().zoomToExtentScreen(MinX, MaxX, MinY, MaxY, _zoom);
                }
                break;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            switch (_mouseMode) {
                case Map_SelectFeatures_Rectangle:
                    if (_currentLayoutMap.getMapFrame().getMapView().getSelectedLayerHandle() < 0) {
                        return;
                    }
                    MapLayer aMLayer = _currentLayoutMap.getMapFrame().getMapView().getSelectedLayer();
                    if (aMLayer == null) {
                        return;
                    }
                    if (aMLayer.getLayerType() != LayerTypes.VectorLayer) {
                        return;
                    }

                    VectorLayer aLayer = (VectorLayer) aMLayer;
                    PointF mapP = pageToScreen(_currentLayoutMap.getLeft(), _currentLayoutMap.getTop());
                    Point aPoint = new Point(e.getX() - (int) mapP.X, e.getY() - (int) mapP.Y);
                    Point bPoint = new Point(_mouseDownPoint.x - (int) mapP.X, _mouseDownPoint.y - (int) mapP.Y);
                    int minx = Math.min(bPoint.x, aPoint.x);
                    int miny = Math.min(bPoint.y, aPoint.y);
                    int width = Math.abs(aPoint.x - bPoint.x);
                    int height = Math.abs(aPoint.y - bPoint.y);
                    Rectangle.Float rect = new Rectangle.Float(minx, miny, width, height);
                    List<Integer> selectedShapes = _currentLayoutMap.getMapFrame().getMapView().selectShapes(aLayer, rect);
                    if (!(e.isControlDown() || e.isShiftDown())) {
                        aLayer.clearSelectedShapes();
                    }
                    if (selectedShapes.size() > 0) {
                        for (int shapeIdx : selectedShapes) {
                            Shape shape = aLayer.getShapes().get(shapeIdx);
                            shape.setSelected(!shape.isSelected());
                        }
                        _currentLayoutMap.getMapFrame().getMapView().fireShapeSelectedEvent();
                    }
                    //this.paintGraphics();
                    this.repaintNew();
                    break;
                case CreateSelection:
                    //Remove selected graphics
                    for (LayoutElement aElement : _selectedElements) {
                        aElement.setSelected(false);
                    }
                    _selectedElements.clear();

                    //Select elements
                    if (Math.abs(e.getX() - _mouseDownPoint.x) > 2 || Math.abs(e.getY() - _mouseDownPoint.y) > 2) {
                        //this.paintGraphics();
                        this.repaintNew();
                        return;
                    }

                    //Point mousePoint = new Point(_mouseDownPoint.X, _mouseDownPoint.Y);
                    _selectedElements = selectElements(pageP, _layoutElements, 0);
                    if (_selectedElements.size() > 0) {
                        for (int i = 0; i < _selectedElements.size() - 1; i++) {
                            _selectedElements.remove(_selectedElements.size() - 1);
                        }
                        _selectedElements.get(0).setSelected(true);
                        if (_selectedElements.get(0).getElementType() == ElementType.LayoutMap) {
                            setActiveMapFrame(((LayoutMap) _selectedElements.get(0)).getMapFrame());
                        }
                    }
                    this.fireElementSelectedEvent();

                    _mouseMode = MouseMode.Select;
                    //this.paintGraphics();
                    this.repaintNew();
                    break;
                case MoveSelection:
                    //Select elements
                    if (Math.abs(e.getX() - _mouseDownPoint.x) < 2 && Math.abs(e.getY() - _mouseDownPoint.y) < 2) {
                        LayoutElement aElement = _selectedElements.get(0);
                        _selectedElements = selectElements(pageP, _layoutElements, 0);
                        if (_selectedElements.size() > 1) {
                            aElement.setSelected(false);
                            int idx = _selectedElements.indexOf(aElement);
                            if (idx == 0) {
                                idx = _selectedElements.size() - 1;
                            } else {
                                idx -= 1;
                            }
                            if (idx < 0) {
                                idx = 0;
                            }
                            aElement = _selectedElements.get(idx);
                            _selectedElements.clear();
                            _selectedElements.add(aElement);
                            _selectedElements.get(0).setSelected(true);
                            if (_selectedElements.get(0).getElementType() == ElementType.LayoutMap) {
                                setActiveMapFrame(((LayoutMap) _selectedElements.get(0)).getMapFrame());
                            }
                        }
                        this.fireElementSelectedEvent();
                    } else {
                        int deltaX = (int) ((e.getX() - _mouseDownPoint.x) / _zoom);
                        int deltaY = (int) ((e.getY() - _mouseDownPoint.y) / _zoom);
                        for (LayoutElement aElement : _selectedElements) {
                            aElement.setLeft(aElement.getLeft() + deltaX);
                            aElement.setTop(aElement.getTop() + deltaY);
                            aElement.moveUpdate();
                        }
                        UndoableEdit edit = (new MapLayoutUndoRedo()).new MoveElementsEdit(this, _selectedElements, deltaX, deltaY);
                        this.fireUndoEditEvent(edit);
                    }

                    _mouseMode = MouseMode.Select;
                    //this.paintGraphics();
                    this.repaintNew();
                    break;
                case ResizeSelected:
                    _mouseMode = MouseMode.Select;
                    LayoutElement oElement = _selectedElements.get(0);
                    if (_selectedRectangle.width < 3) {
                        _selectedRectangle.width = 3;
                    }
                    if (_selectedRectangle.height < 3) {
                        _selectedRectangle.height = 3;
                    }

                    UndoableEdit edit = (new MapLayoutUndoRedo()).new ResizeElementEdit(this, oElement, _selectedRectangle);
                    this.fireUndoEditEvent(edit);
                    PointF minP = screenToPage((float) _selectedRectangle.x, (float) _selectedRectangle.y);
                    PointF maxP = screenToPage((float) _selectedRectangle.x + _selectedRectangle.width, _selectedRectangle.y + _selectedRectangle.height);
                    oElement.setLeft((int) minP.X);
                    oElement.setTop((int) minP.Y);
                    oElement.setWidth((int) (maxP.X - minP.X));
                    oElement.setHeight((int) (maxP.Y - minP.Y));
                    oElement.resizeUpdate();
                    //this.paintGraphics();
                    this.repaintNew();
                    break;
                case New_Rectangle:
                case New_Ellipse:
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (e.getX() - _mouseDownPoint.x < 2 || e.getY() - _mouseDownPoint.y < 2) {
                            return;
                        }

                        _startNewGraphic = true;
                        _graphicPoints = new ArrayList<>();
                        _graphicPoints.add(new PointF(_mouseDownPoint.x, _mouseDownPoint.y));
                        _graphicPoints.add(new PointF(_mouseDownPoint.x, e.getY()));
                        _graphicPoints.add(new PointF(e.getX(), e.getY()));
                        _graphicPoints.add(new PointF(e.getX(), _mouseDownPoint.y));
                        List<PointD> points = new ArrayList<>();
                        for (PointF cPoint : _graphicPoints) {
                            PointF dPoint = screenToPage(cPoint.X, cPoint.Y);
                            points.add(new PointD(dPoint.X, dPoint.Y));
                        }

                        Graphic aGraphic = null;
                        switch (_mouseMode) {
                            case New_Rectangle:
                                RectangleShape aPGS = new RectangleShape();
                                points.add((PointD) points.get(0).clone());
                                aPGS.setPoints(points);
                                aGraphic = new Graphic(aPGS, (PolygonBreak) _defPolygonBreak.clone());
                                break;
                            case New_Ellipse:
                                EllipseShape aES = new EllipseShape();
                                aES.setPoints(points);
                                aGraphic = new Graphic(aES, (PolygonBreak) _defPolygonBreak.clone());
                                break;
                        }

                        if (aGraphic != null) {
                            LayoutGraphic lg = new LayoutGraphic(aGraphic, this);
                            addElement(lg);
                            //this.paintGraphics();
                            this.repaintNew();
                            edit = (new MapLayoutUndoRedo()).new AddElementEdit(this, lg);
                            this.fireUndoEditEvent(edit);
                        } else {
                            //this.repaint();
                            this.repaintOld();
                        }
                    }
                    break;
                case New_Freehand:
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        _startNewGraphic = true;
                        if (_graphicPoints.size() < 2) {
                            break;
                        }

                        List<PointD> points = new ArrayList<>();
                        for (PointF cPoint : _graphicPoints) {
                            PointF dPoint = screenToPage(cPoint.X, cPoint.Y);
                            points.add(new PointD(dPoint.X, dPoint.Y));
                        }

                        PolylineShape aPLS = new PolylineShape();
                        aPLS.setPoints(points);
                        Graphic aGraphic = new Graphic(aPLS, (PolylineBreak) _defPolylineBreak.clone());
                        LayoutGraphic lg = new LayoutGraphic(aGraphic, this);
                        addElement(lg);
                        //this.paintGraphics();
                        this.repaintNew();
                        edit = (new MapLayoutUndoRedo()).new AddElementEdit(this, lg);
                        this.fireUndoEditEvent(edit);
                    }
                    break;
                case New_Circle:
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (e.getX() - _mouseDownPoint.x < 2 || e.getY() - _mouseDownPoint.y < 2) {
                            return;
                        }

                        float radius = (float) Math.sqrt(Math.pow(e.getX() - _mouseDownPoint.x, 2)
                                + Math.pow(e.getY() - _mouseDownPoint.y, 2));
                        _startNewGraphic = true;
                        _graphicPoints = new ArrayList<>();
                        _graphicPoints.add(new PointF(_mouseDownPoint.x - radius, _mouseDownPoint.y));
                        _graphicPoints.add(new PointF(_mouseDownPoint.x, _mouseDownPoint.y - radius));
                        _graphicPoints.add(new PointF(_mouseDownPoint.x + radius, _mouseDownPoint.y));
                        _graphicPoints.add(new PointF(_mouseDownPoint.x, _mouseDownPoint.y + radius));
                        List<PointD> points = new ArrayList<>();
                        for (PointF cPoint : _graphicPoints) {
                            PointF dPoint = screenToPage(cPoint.X, cPoint.Y);
                            points.add(new PointD(dPoint.X, dPoint.Y));
                        }

                        CircleShape aPGS = new CircleShape();
                        aPGS.setPoints(points);
                        Graphic aGraphic = new Graphic(aPGS, (PolygonBreak) _defPolygonBreak.clone());
                        LayoutGraphic lg = new LayoutGraphic(aGraphic, this);
                        addElement(lg);
                        //this.paintGraphics();
                        this.repaintNew();
                        edit = (new MapLayoutUndoRedo()).new AddElementEdit(this, lg);
                        this.fireUndoEditEvent(edit);
                    }
                    break;
                case InEditingVertices:
                    LayoutGraphic lg = (LayoutGraphic) _selectedElements.get(0);
                    edit = (new MapLayoutUndoRedo()).new MoveGraphicVerticeEdit(this, lg, _editingVerticeIndex,
                            pageP.x, pageP.y);
                    this.fireUndoEditEvent(edit);
                    lg.verticeEditUpdate(_editingVerticeIndex, pageP.x, pageP.y);
                    _mouseMode = MouseMode.EditVertices;
                    //this.paintGraphics();
                    this.repaintNew();
                    break;
            }
        }
    }

    void onMouseClicked(MouseEvent e) throws CloneNotSupportedException {
        int clickTimes = e.getClickCount();
        if (clickTimes == 1) {
            Point pageP = screenToPage(e.getX(), e.getY());
            if (e.getButton() == MouseEvent.BUTTON1) {
                switch (_mouseMode) {
                    case Map_Identifer:
                        MapLayer aMLayer = _currentLayoutMap.getMapFrame().getMapView().getSelectedLayer();
                        if (aMLayer == null) {
                            return;
                        }
                        if (aMLayer.getLayerType() == LayerTypes.ImageLayer) {
                            return;
                        }

                        PointF mapP = pageToScreen(_currentLayoutMap.getLeft(), _currentLayoutMap.getTop());
                        PointF aPoint = new PointF(e.getX() - mapP.X, e.getY() - mapP.Y);
                        if (aMLayer.getLayerType() == LayerTypes.VectorLayer) {
                            VectorLayer aLayer = (VectorLayer) aMLayer;
                            List<Integer> selectedShapes = _currentLayoutMap.getMapFrame().getMapView().selectShapes(aLayer, aPoint);
                            if (selectedShapes.size() > 0) {
                                if (frmIdentifier == null) {
                                    frmIdentifier = new FrmIdentifer((JFrame) SwingUtilities.getWindowAncestor(this), false, _currentLayoutMap.getMapFrame().getMapView());
                                    frmIdentifier.addWindowListener(new WindowAdapter() {
                                        @Override
                                        public void windowClosed(WindowEvent e) {
                                            _currentLayoutMap.getMapFrame().getMapView().setDrawIdentiferShape(false);
                                            //repaint();
                                            repaintOld();
                                        }
                                    });
                                }
                                frmIdentifier.setMapView(_currentLayoutMap.getMapFrame().getMapView());

                                String[] colNames = {"Field", "Value"};
                                String fieldStr, valueStr;
                                int shapeIdx = selectedShapes.get(0);
                                aLayer.setIdentiferShape(shapeIdx);
                                _currentLayoutMap.getMapFrame().getMapView().setDrawIdentiferShape(true);

                                Object[][] tData = new Object[aLayer.getFieldNumber() + 1][2];
                                fieldStr = "Index";
                                valueStr = String.valueOf(shapeIdx);
                                tData[0][0] = fieldStr;
                                tData[0][1] = valueStr;
                                if (aLayer.getShapeNum() > 0) {
                                    for (int i = 0; i < aLayer.getFieldNumber(); i++) {
                                        fieldStr = aLayer.getFieldName(i);
                                        valueStr = aLayer.getCellValue(i, shapeIdx).toString();
                                        tData[i + 1][0] = fieldStr;
                                        tData[i + 1][1] = valueStr;
                                    }
                                }
                                DefaultTableModel dtm = new javax.swing.table.DefaultTableModel(tData, colNames) {
                                    @Override
                                    public boolean isCellEditable(int row, int column) {
                                        return false;
                                    }
                                };
                                this.frmIdentifier.getTable().setModel(dtm);
                                this.frmIdentifier.repaint();
                                if (!this.frmIdentifier.isVisible()) {
                                    this.frmIdentifier.setLocation(e.getX(), e.getY());
                                    this.frmIdentifier.setVisible(true);
                                }

                                //this.repaint();
                                this.repaintOld();
                                //Rectangle rect = getElementViewExtent(_currentLayoutMap);
                                //_currentLayoutMap.getMapFrame().getMapView().drawIdShape(this.createGraphics(), aLayer.getShapes().get(shapeIdx), rect);
                            }
                        } else if (aMLayer.getLayerType() == LayerTypes.RasterLayer) {
                            RasterLayer aRLayer = (RasterLayer) aMLayer;
                            int[] ijIdx = _currentLayoutMap.getMapFrame().getMapView().selectGridCell(aRLayer, aPoint);
                            if (ijIdx != null) {
                                int iIdx = ijIdx[0];
                                int jIdx = ijIdx[1];
                                double aValue = aRLayer.getCellValue(iIdx, jIdx);
                                if (frmIdentifierGrid == null) {
                                    frmIdentifierGrid = new FrmIdentiferGrid((JFrame) SwingUtilities.getWindowAncestor(this), false);
                                }

                                frmIdentifierGrid.setIIndex(iIdx);
                                frmIdentifierGrid.setJIndex(jIdx);
                                frmIdentifierGrid.setCellValue(aValue);
                                if (!this.frmIdentifierGrid.isVisible()) {
                                    //this._frmIdentiferGrid.setLocation(e.getX(), e.getY());
                                    this.frmIdentifierGrid.setLocationRelativeTo(this);
                                    this.frmIdentifierGrid.setVisible(true);
                                }
                            }
                        }
                        break;
//                    case Map_SelectFeatures_Rectangle:
//                        aMLayer = _currentLayoutMap.getMapFrame().getMapView().getSelectedLayer();
//                        if (aMLayer == null) {
//                            return;
//                        }
//
//                        if (aMLayer.getLayerType() != LayerTypes.VectorLayer) {
//                            return;
//                        }
//
//                        VectorLayer aLayer = (VectorLayer) aMLayer;
//                        if (!e.isControlDown() && !e.isShiftDown()) {
//                            aLayer.clearSelectedShapes();
//                            //_currentLayoutMap.getMapFrame().getMapView().paintLayers();
//                        }
//
//                        mapP = pageToScreen(_currentLayoutMap.getLeft(), _currentLayoutMap.getTop());
//                        aPoint = new PointF(e.getX() - mapP.X, e.getY() - mapP.Y);
//                        List<Integer> selectedShapes = _currentLayoutMap.getMapFrame().getMapView().selectShapes(aLayer, aPoint);
//                        this._layoutBitmap = GlobalUtil.deepCopy(this._tempImage);
//                        if (selectedShapes.size() > 0) {
//                            int shapeIdx = selectedShapes.get(0);
//                            Shape selShape = aLayer.getShapes().get(shapeIdx);
//                            Rectangle rect = getElementViewExtent(_currentLayoutMap);
//                            if (!e.isControlDown() && !e.isShiftDown()) {
//                                selShape.setSelected(true);
//                                _currentLayoutMap.getMapFrame().getMapView().drawIdShape((Graphics2D) this._layoutBitmap.getGraphics(), selShape, rect);
//                            } else {
//                                selShape.setSelected(!selShape.isSelected());
//                                for (int sIdx : aLayer.getSelectedShapeIndexes()) {
//                                    _currentLayoutMap.getMapFrame().getMapView().drawIdShape((Graphics2D) this._layoutBitmap.getGraphics(), aLayer.getShapes().get(sIdx), rect);
//                                }
//                            }
//                            this.repaint();
//                        } else {
//                            if (!e.isControlDown() && !e.isShiftDown()) {
//                                this.repaint();
//                            }
//                        }
//                        break;
                }
            } else if (e.getButton() == MouseEvent.BUTTON3 && _mouseMode == MouseMode.Select) {
                if (_selectedElements.isEmpty()) {
                    return;
                }

                JPopupMenu jPopupMenu_Element = new JPopupMenu();
                JMenu jMenu_Order = new JMenu("Order");
                jPopupMenu_Element.add(jMenu_Order);

                JMenuItem jMenuItem_BTF = new JMenuItem("Bring to Front");
                jMenuItem_BTF.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onBringToFrontClick(e);
                    }
                });
                jMenu_Order.add(jMenuItem_BTF);

                JMenuItem jMenuItem_STB = new JMenuItem("Send to Back");
                jMenuItem_STB.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onSendToBackClick(e);
                    }
                });
                jMenu_Order.add(jMenuItem_STB);

                JMenuItem jMenuItem_BF = new JMenuItem("Bring Forward");
                jMenuItem_BF.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onBringForwardClick(e);
                    }
                });
                jMenu_Order.add(jMenuItem_BF);

                JMenuItem jMenuItem_SB = new JMenuItem("Send Backward");
                jMenuItem_SB.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onSendBackwardClick(e);
                    }
                });
                jMenu_Order.add(jMenuItem_SB);

                jPopupMenu_Element.add(new JSeparator());

                JMenuItem jMenuItem_Remove = new JMenuItem("Remove");
                jMenuItem_Remove.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onRemoveElementClick(e);
                    }
                });
                jPopupMenu_Element.add(jMenuItem_Remove);

                switch (_mouseMode) {
                    case Select:
                    case MoveSelection:
                    case ResizeSelected:
                        if (_selectedElements.size() > 0) {
                            LayoutElement aElement = _selectedElements.get(0);
                            if (MIMath.pointInRectangle(pageP, aElement.getBounds())) {
                                if (aElement.getElementType() == ElementType.LayoutGraphic) {
                                    Graphic aGraphic = ((LayoutGraphic) aElement).getGraphic();
                                    if (aGraphic.getLegend().getBreakType() == BreakTypes.PolylineBreak || aGraphic.getLegend().getBreakType() == BreakTypes.PolygonBreak) {
                                        JMenuItem jMenuItem_Reverse = new JMenuItem("Reverse");
                                        jMenuItem_Reverse.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                onReverseGraphicClick(e);
                                            }
                                        });
                                        jPopupMenu_Element.add(jMenuItem_Reverse);

                                        if (aGraphic.getShape().getShapeType() == ShapeTypes.Polyline || aGraphic.getShape().getShapeType() == ShapeTypes.Polygon) {
                                            jPopupMenu_Element.add(new JSeparator());
                                            JMenuItem jMenuItem_Smooth = new JMenuItem("Smooth Graphic");
                                            jMenuItem_Smooth.addActionListener(new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    onGraphicSmoothClick(e);
                                                }
                                            });
                                            jPopupMenu_Element.add(jMenuItem_Smooth);
                                        }

                                        if (aGraphic.getShape().getShapeType() == ShapeTypes.Ellipse) {
                                            JMenuItem jMenuItem_Angle = new JMenuItem("Set Angle");
                                            jMenuItem_Angle.addActionListener(new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    onGraphicAngleClick(e);
                                                }
                                            });
                                            jPopupMenu_Element.add(jMenuItem_Angle);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }

                jPopupMenu_Element.show(this, e.getX(), e.getY());
            }
        } else if (clickTimes == 2) {
            Point pageP = screenToPage(e.getX(), e.getY());
            switch (_mouseMode) {
                case Select:
                case MoveSelection:
                case ResizeSelected:
                    if (_selectedElements.isEmpty()) {
                        return;
                    }

                    LayoutElement aElement = _selectedElements.get(0);
                    _selectedElements = selectElements(pageP, _layoutElements, 0);
                    if (_selectedElements.size() > 1) {
                        aElement.setSelected(false);
                        int idx = _selectedElements.indexOf(aElement);
                        idx += 2;
                        if (idx > _selectedElements.size() - 1) {
                            idx = idx - _selectedElements.size();
                        }
                        aElement = _selectedElements.get(idx);
                        _selectedElements.clear();
                        _selectedElements.add(aElement);
                        _selectedElements.get(0).setSelected(true);
                    }
                    //this.paintGraphics();
                    this.repaintNew();

                    if (aElement.getElementType() == ElementType.LayoutGraphic) {
                        Graphic aGraphic = ((LayoutGraphic) aElement).getGraphic();
                        showSymbolSetForm(aGraphic);
                    } else {
                        FrmProperty aFrmProperty = new FrmProperty((JFrame) SwingUtilities.getWindowAncestor(this), true, false);
                        Object object = aElement;
                        switch (aElement.getElementType()) {
                            case LayoutLegend:
                                object = ((LayoutLegend) aElement).new LayoutLegendBean();
                                break;
                            case LayoutMap:
                                object = ((LayoutMap) aElement).new LayoutMapBean();
                                break;
                            case LayoutNorthArraw:
                                object = ((LayoutNorthArrow) aElement).new LayoutNorthArrowBean();
                                break;
                            case LayoutScaleBar:
                                object = ((LayoutScaleBar) aElement).new LayoutScaleBarBean();
                                break;
                        }
                        aFrmProperty.setObject(object);
                        aFrmProperty.setParent(this);
                        aFrmProperty.setLocationRelativeTo(this);
                        aFrmProperty.setVisible(true);
                    }
                    setMouseMode(MouseMode.Select);
                    this.fireElementSelectedEvent();
                    break;
                case New_Polyline:
                case New_Polygon:
                case New_Curve:
                case New_CurvePolygon:
                case New_Freehand:
                case Map_SelectFeatures_Polygon:
                    if (!_startNewGraphic) {
                        _startNewGraphic = true;
                        _graphicPoints.remove(_graphicPoints.size() - 1);

                        if (_mouseMode == MouseMode.Map_SelectFeatures_Polygon) {
                            PointF mapP = pageToScreen(_currentLayoutMap.getLeft(), _currentLayoutMap.getTop());
                            List<PointD> points = new ArrayList<>();
                            MapView currentMapView = _currentLayoutMap.getMapFrame().getMapView();
                            for (PointF aPoint : _graphicPoints) {
                                float[] pXY = currentMapView.screenToProj(aPoint.X - mapP.X, aPoint.Y - mapP.Y);
                                points.add(new PointD(pXY[0], pXY[1]));
                            }

                            MapLayer aMLayer = _currentLayoutMap.getMapFrame().getMapView().getSelectedLayer();
                            if (aMLayer == null) {
                                return;
                            }
                            if (aMLayer.getLayerType() != LayerTypes.VectorLayer) {
                                return;
                            }

                            PolygonShape aPGS = new PolygonShape();
                            points.add((PointD) points.get(0).clone());
                            aPGS.setPoints(points);
                            VectorLayer aLayer = (VectorLayer) aMLayer;
                            if (!e.isControlDown() && !e.isShiftDown()) {
                                aLayer.clearSelectedShapes();
                            }
                            aLayer.selectShapes(aPGS);
                            _currentLayoutMap.getMapFrame().getMapView().fireShapeSelectedEvent();
                        } else {
                            List<PointD> points = new ArrayList<>();
                            for (PointF aPoint : _graphicPoints) {
                                PointF bPoint = screenToPage(aPoint.X, aPoint.Y);
                                points.add(new PointD(bPoint.X, bPoint.Y));
                            }

                            Graphic aGraphic = null;
                            switch (_mouseMode) {
                                case New_Polyline:
                                case New_Freehand:
                                    PolylineShape aPLS = new PolylineShape();
                                    aPLS.setPoints(points);
                                    aGraphic = new Graphic(aPLS, (PolylineBreak) _defPolylineBreak.clone());
                                    break;
                                case New_Polygon:
                                    if (points.size() > 2) {
                                        PolygonShape aPGS = new PolygonShape();
                                        points.add((PointD) points.get(0).clone());
                                        aPGS.setPoints(points);
                                        aGraphic = new Graphic(aPGS, (PolygonBreak) _defPolygonBreak.clone());
                                    }
                                    break;
                                case New_Curve:
                                    CurveLineShape aCLS = new CurveLineShape();
                                    aCLS.setPoints(points);
                                    aGraphic = new Graphic(aCLS, (PolylineBreak) _defPolylineBreak.clone());
                                    break;
                                case New_CurvePolygon:
                                    if (points.size() > 2) {
                                        CurvePolygonShape aCPS = new CurvePolygonShape();
                                        points.add((PointD) points.get(0).clone());
                                        aCPS.setPoints(points);
                                        aGraphic = new Graphic(aCPS, (PolygonBreak) _defPolygonBreak.clone());
                                    }
                                    break;
                            }

                            if (aGraphic != null) {
                                LayoutGraphic lg = new LayoutGraphic(aGraphic, this);
                                addElement(lg);
                                //this.paintGraphics();
                                this.repaintNew();
                                UndoableEdit edit = (new MapLayoutUndoRedo()).new AddElementEdit(this, lg);
                                this.fireUndoEditEvent(edit);
                            } else {
                                this.repaint();
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void onRemoveElementClick(ActionEvent e) {
        this.onRemoveElementClick();
    }

    private void onBringToFrontClick(ActionEvent e) {
        LayoutElement aLE = _selectedElements.get(0);
        int idx = _layoutElements.indexOf(aLE);
        if (idx < _layoutElements.size() - 1) {
            _layoutElements.remove(aLE);
            _layoutElements.add(aLE);
            //this.paintGraphics();
            this.repaintNew();
        }
    }

    private void onSendToBackClick(ActionEvent e) {
        LayoutElement aLE = _selectedElements.get(0);
        int idx = _layoutElements.indexOf(aLE);
        if (idx > 0) {
            _layoutElements.remove(aLE);
            _layoutElements.add(0, aLE);
            //this.paintGraphics();
            this.repaintNew();
        }
    }

    private void onBringForwardClick(ActionEvent e) {
        LayoutElement aLE = _selectedElements.get(0);
        int idx = _layoutElements.indexOf(aLE);
        if (idx < _layoutElements.size() - 1) {
            _layoutElements.remove(aLE);
            _layoutElements.add(idx + 1, aLE);
            //this.paintGraphics();
            this.repaintNew();
        }
    }

    private void onSendBackwardClick(ActionEvent e) {
        LayoutElement aLE = _selectedElements.get(0);
        int idx = _layoutElements.indexOf(aLE);
        if (idx > 0) {
            _layoutElements.remove(aLE);
            _layoutElements.add(idx - 1, aLE);
            //this.paintGraphics();
            this.repaintNew();
        }
    }

    private void onReverseGraphicClick(ActionEvent e) {
        LayoutElement aElement = _selectedElements.get(0);
        Graphic aGraphic = ((LayoutGraphic) aElement).getGraphic();
        List<PointD> points = (List<PointD>) aGraphic.getShape().getPoints();
        Collections.reverse(points);
        aGraphic.getShape().setPoints(points);

        //this.paintGraphics();
        this.repaintNew();
    }

    private void onGraphicSmoothClick(ActionEvent e) {
        LayoutElement aElement = _selectedElements.get(0);
        Graphic aGraphic = ((LayoutGraphic) aElement).getGraphic();
        List<wcontour.global.PointD> pointList = new ArrayList<>();
        List<PointD> newPoints = new ArrayList<>();

        for (PointD aP : aGraphic.getShape().getPoints()) {
            pointList.add(new wcontour.global.PointD(aP.X, aP.Y));
        }

        if (aGraphic.getShape().getShapeType() == ShapeTypes.Polygon) {
            pointList.add(pointList.get(0));
        }

        pointList = wcontour.Contour.smoothPoints(pointList);
        for (wcontour.global.PointD aP : pointList) {
            newPoints.add(new PointD(aP.X, aP.Y));
        }
        aGraphic.getShape().setPoints(newPoints);
        ((LayoutGraphic) aElement).updateControlSize();
        //this.paintGraphics();
        this.repaintNew();
    }
    
    private void onGraphicAngleClick(ActionEvent e) {
        LayoutElement aElement = _selectedElements.get(0);
        Graphic aGraphic = ((LayoutGraphic) aElement).getGraphic();
        EllipseShape es = (EllipseShape)aGraphic.getShape();
        String angleStr = JOptionPane.showInputDialog(this, "Ellipse angle:", es.getAngle());
        if (angleStr != null){
            es.setAngle(Float.parseFloat(angleStr));
            //this.paintGraphics();
            this.repaintNew();
        }       
    }

    private void showSymbolSetForm(ColorBreak aCB) {
        switch (aCB.getBreakType()) {
            case PointBreak:
                PointBreak aPB = (PointBreak) aCB;

                if (_frmPointSymbolSet == null) {
                    _frmPointSymbolSet = new FrmPointSymbolSet((JFrame) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPointSymbolSet.setLocationRelativeTo(this);
                    _frmPointSymbolSet.setVisible(true);
                }
                _frmPointSymbolSet.setPointBreak(aPB);
                _frmPointSymbolSet.setVisible(true);
                break;
            case LabelBreak:
                LabelBreak aLB = (LabelBreak) aCB;

                if (_frmLabelSymbolSet == null) {
                    _frmLabelSymbolSet = new FrmLabelSymbolSet((JFrame) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmLabelSymbolSet.setLocationRelativeTo(this);
                    _frmLabelSymbolSet.setVisible(true);
                }
                _frmLabelSymbolSet.setLabelBreak(aLB);
                _frmLabelSymbolSet.setVisible(true);
                break;
            case PolylineBreak:
                PolylineBreak aPLB = (PolylineBreak) aCB;

                if (_frmPolylineSymbolSet == null) {
                    _frmPolylineSymbolSet = new FrmPolylineSymbolSet((JFrame) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPolylineSymbolSet.setLocationRelativeTo(this);
                    _frmPolylineSymbolSet.setVisible(true);
                }
                _frmPolylineSymbolSet.setPolylineBreak(aPLB);
                _frmPolylineSymbolSet.setVisible(true);
                break;
            case PolygonBreak:
                PolygonBreak aPGB = (PolygonBreak) aCB;

                if (_frmPolygonSymbolSet == null) {
                    _frmPolygonSymbolSet = new FrmPolygonSymbolSet((JFrame) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPolygonSymbolSet.setLocationRelativeTo(this);
                    _frmPolygonSymbolSet.setVisible(true);
                }
                _frmPolygonSymbolSet.setPolygonBreak(aPGB);
                _frmPolygonSymbolSet.setVisible(true);
                break;
        }
    }

    private void showSymbolSetForm(Graphic graphic) {
        Shape shape = graphic.getShape();
        ColorBreak aCB = graphic.getLegend();
        switch (aCB.getBreakType()) {
            case PointBreak:
                PointBreak aPB = (PointBreak) aCB;

                if (_frmPointSymbolSet == null) {
                    _frmPointSymbolSet = new FrmPointSymbolSet((JFrame) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPointSymbolSet.setLocationRelativeTo(this);
                    _frmPointSymbolSet.setVisible(true);
                }
                _frmPointSymbolSet.setPointBreak(aPB);
                _frmPointSymbolSet.setVisible(true);
                break;
            case LabelBreak:
                LabelBreak aLB = (LabelBreak) aCB;

                if (_frmLabelSymbolSet == null) {
                    _frmLabelSymbolSet = new FrmLabelSymbolSet((JFrame) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmLabelSymbolSet.setLocationRelativeTo(this);
                    _frmLabelSymbolSet.setVisible(true);
                }
                _frmLabelSymbolSet.setLabelBreak(aLB);
                _frmLabelSymbolSet.setVisible(true);
                break;
            case PolylineBreak:
                PolylineBreak aPLB = (PolylineBreak) aCB;

                if (_frmPolylineSymbolSet == null) {
                    _frmPolylineSymbolSet = new FrmPolylineSymbolSet((JFrame) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPolylineSymbolSet.setLocationRelativeTo(this);
                    _frmPolylineSymbolSet.setVisible(true);
                }
                _frmPolylineSymbolSet.setPolylineBreak(aPLB);
                _frmPolylineSymbolSet.setVisible(true);
                break;
            case PolygonBreak:
                PolygonBreak aPGB = (PolygonBreak) aCB;

                if (_frmPolygonSymbolSet == null) {
                    _frmPolygonSymbolSet = new FrmPolygonSymbolSet((JFrame) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPolygonSymbolSet.setLocationRelativeTo(this);
                    _frmPolygonSymbolSet.setVisible(true);
                }
                _frmPolygonSymbolSet.setPolygonBreak(aPGB);
                _frmPolygonSymbolSet.setVisible(true);
                break;
            case VectorBreak:
                WindArrow wa = (WindArrow) shape;
                //VectorBreak vb = (VectorBreak) aCB;
                Object[] lens = {5, 10, 15, 20, 25, 30};
                Object lenObj = JOptionPane.showInputDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Select wind speed:", "Selection", JOptionPane.PLAIN_MESSAGE, null, lens, (int) wa.length);
                if (lenObj != null) {
                    wa.length = Integer.parseInt(lenObj.toString());
                    //this.paintGraphics();
                    this.repaintNew();
                }
                break;
        }
    }

    void onKeyPressed(KeyEvent e) {
        if (_mouseMode == MouseMode.Select) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DELETE:
                    onRemoveElementClick();
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    int x = 0;
                    int y = 0;
                    int d = 5;
                    if (e.isControlDown()) {
                        d = 1;
                    }
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            x = -d;
                            break;
                        case KeyEvent.VK_RIGHT:
                            x = d;
                            break;
                        case KeyEvent.VK_UP:
                            y = -d;
                            break;
                        case KeyEvent.VK_DOWN:
                            y = d;
                            break;
                    }
                    for (int i = 0; i < _layoutElements.size(); i++) {
                        LayoutElement aElement = _layoutElements.get(i);
                        if (aElement.isSelected()) {
                            if (x != 0) {
                                aElement.setLeft(aElement.getLeft() + x);
                            }
                            if (y != 0) {
                                aElement.setTop(aElement.getTop() + y);
                            }
                            aElement.moveUpdate();
                        }
                    }
                    //this.paintGraphics();
                    this.repaintNew();
                    break;
            }
        }
    }

    private void onRemoveElementClick() {
        UndoableEdit edit = (new MapLayoutUndoRedo()).new RemoveElementsEdit(this, _selectedElements);
        this.fireUndoEditEvent(edit);
        for (LayoutElement element : _selectedElements) {
            removeElement(element);
        }

        _selectedElements.clear();
        _startNewGraphic = true;
        //paintGraphics();
        this.repaintNew();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get if using off screen image double buffering.
     * Using double buffering will be faster but lower view quality in
     * high dpi screen computer.
     *
     * @return Boolean
     */
    public boolean isDoubleBuffer() {
        return this.doubleBuffer;
    }

    /**
     * Set using off screen image double buffering or not.
     * @param value Boolean
     */
    public void setDoubleBuffer(boolean value) {
        this.doubleBuffer = value;
    }

    /**
     * Get if lock view update
     *
     * @return If lock view update
     */
    public boolean isLockViewUpdate() {
        return _lockViewUpdate;
    }

    /**
     * Set if lock view update
     *
     * @param istrue If lock view update
     */
    public void setLockViewUpdate(boolean istrue) {
        _lockViewUpdate = istrue;
    }

    /**
     * Get map frames
     *
     * @return Map frames
     */
    public List<MapFrame> getMapFrames() {
        return _mapFrames;
    }

    /**
     * Set map frames
     *
     * @param mfs Map frames
     */
    public void setMapFrames(List<MapFrame> mfs) {
        _mapFrames = mfs;
        _mapFrames = new ArrayList<>();
        for (MapFrame mf : mfs) {
            boolean isInsert = false;
            for (int i = 0; i < _mapFrames.size(); i++) {
                MapFrame amf = _mapFrames.get(i);
                if (mf.getOrder() < amf.getOrder()) {
                    _mapFrames.add(i, mf);
                    isInsert = true;
                    break;
                }
            }

            if (!isInsert) {
                _mapFrames.add(mf);
            }
        }
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
     * Get active layout map
     *
     * @return Active layout map
     */
    public LayoutMap getActiveLayoutMap() {
        LayoutMap aLM = null;
        for (LayoutMap lm : this.getLayoutMaps()) {
            if (lm.getMapFrame().isActive()) {
                aLM = lm;
                break;
            }
        }
        return aLM;
    }

    /**
     * Get if is landscape
     *
     * @return Boolean
     */
    public boolean isLandscape() {
        return _isLandscape;
    }

    /**
     * Set if is landscape
     *
     * @param istrue
     */
    public void setLandscape(boolean istrue) {
        _isLandscape = istrue;
        Rectangle.Float aRect = paperToScreen(new Rectangle.Float(0, 0, this.getPaperWidth(), this.getPaperHeight()));
        _pageBounds.width = (int) aRect.width;
        _pageBounds.height = (int) aRect.height;
    }

    /**
     * Get mouse mode
     *
     * @return The mouse mode
     */
    public MouseMode getMouseMode() {
        return _mouseMode;
    }

    /**
     * Set mouse mode
     *
     * @param mm The mouse mode
     */
    public void setMouseMode(MouseMode mm) {
        _mouseMode = mm;
        switch (_mouseMode) {
            case New_Label:
            case New_Point:
            case New_Polyline:
            case New_Polygon:
            case New_Rectangle:
            case New_Circle:
            case New_Curve:
            case New_CurvePolygon:
            case New_Ellipse:
            case New_Freehand:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case Map_Measurement:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case Map_SelectFeatures_Rectangle:
            case Map_SelectFeatures_Polygon:
            case Map_SelectFeatures_Lasso:
            case Map_SelectFeatures_Circle:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                this._tempImage = GlobalUtil.deepCopy(this._layoutBitmap);
                break;
        }
    }

    /**
     * Get if antialias
     *
     * @return Boolean
     */
    public boolean isAntiAlias() {
        return _antiAlias;
    }

    /**
     * Set if antialias
     *
     * @param istrue Boolean
     */
    public void setAntiAlias(boolean istrue) {
        _antiAlias = istrue;
    }

    /**
     * Get page foreground color
     *
     * @return Page foreground color
     */
    public Color getPageForeColor() {
        return this._pageForeColor;
    }

    /**
     * Set page foreground color
     *
     * @param c Page foreground color
     */
    public void setPageForeColor(Color c) {
        _pageForeColor = c;
    }

    /**
     * Get page background color
     *
     * @return Page background color
     */
    public Color getPageBackColor() {
        return _pageBackColor;
    }

    /**
     * Set page background color
     *
     * @param c Page background color
     */
    public void setPageBackColor(Color c) {
        _pageBackColor = c;

        if (c == Color.black) {
            _pageForeColor = Color.white;
        } else if (c == Color.white) {
            _pageForeColor = Color.black;
        }
    }

    /**
     * Get paper size
     *
     * @return Paper size
     */
    public PaperSize getPaperSize() {
        return _paperSize;
    }

    /**
     * Set paper size
     *
     * @param ps Paper size
     */
    public void setPaperSize(PaperSize ps) {
        _paperSize = ps;
        Rectangle.Float aRect = paperToScreen(new Rectangle.Float(0, 0, getPaperWidth(), getPaperHeight()));
        _pageBounds.width = (int) aRect.width;
        _pageBounds.height = (int) aRect.height;
    }

    /**
     * Set paper size
     *
     * @param width Width
     * @param height Height
     */
    public void setPaperSize(int width, int height) {
        PaperSize ps = new PaperSize("Custom", width, height);
        setPaperSize(ps);
    }

    /**
     * Get the width of the paper in 1/100 of an inch
     */
    private int getPaperWidth() {
        if (_isLandscape) {
            return _paperSize.getHeight();
        }
        return _paperSize.getWidth();
    }

    /**
     * Gets the heigh of the paper in 1/100 of an inch
     */
    private int getPaperHeight() {
        if (_isLandscape) {
            return _paperSize.getWidth();
        }
        return _paperSize.getHeight();
    }

    /**
     * Get layout map elements
     *
     * @return The layout map elements
     */
    public List<LayoutMap> getLayoutMaps() {
        List<LayoutMap> layoutMaps = new ArrayList<>();
        for (LayoutElement aLE : _layoutElements) {
            if (aLE.getElementType() == ElementType.LayoutMap) {
                layoutMaps.add((LayoutMap) aLE);
            }
        }
        return layoutMaps;
    }

    /**
     * Get selected elements
     *
     * @return Selected elements
     */
    public List<LayoutElement> getSelectedElements() {
        return _selectedElements;
    }

    /// <summary>
    /// Get or set page bounds
    /// </summary>
    /**
     * Get page bounds
     *
     * @return Page bounds
     */
    public Rectangle getPageBounds() {
        return _pageBounds;
    }

    /**
     * Set page bounds
     *
     * @param pb Page bounds
     */
    public void setPageBounds(Rectangle pb) {
        _pageBounds = pb;
    }

    /**
     * Get page location
     *
     * @return Page location
     */
    public PointF getPageLocation() {
        return _pageLocation;
    }

    /**
     * Set page location
     *
     * @param p Page location
     */
    public void setPageLocation(PointF p) {
        _pageLocation = p;
    }

    /**
     * Get zoom
     *
     * @return Zoom
     */
    public float getZoom() {
        return _zoom;
    }

    /**
     * Set zoom
     *
     * @param zoom Zoom
     */
    public void setZoom(float zoom) {
        _zoom = zoom;
        this.fireZoomChangedEvent();
    }

    /**
     * Get default point break
     *
     * @return Default point break
     */
    public PointBreak getDefPointBreak() {
        return _defPointBreak;
    }

    /**
     * Set default point break
     *
     * @param pb Default point break
     */
    public void setDefPointBreak(PointBreak pb) {
        _defPointBreak = pb;
    }

    /**
     * Get default label break
     *
     * @return Default label break
     */
    public LabelBreak getDefLabelBreak() {
        return _defLabelBreak;
    }

    /**
     * Set default label break
     *
     * @param lb Default label break
     */
    public void setDefLabelBreak(LabelBreak lb) {
        _defLabelBreak = lb;
    }

    /**
     * Get default polyline break
     *
     * @return Default polyline break
     */
    public PolylineBreak getDefPolylineBreak() {
        return _defPolylineBreak;
    }

    /**
     * Set default polyline break
     *
     * @param pb Default polyline break
     */
    public void setDefPolylineBreak(PolylineBreak pb) {
        _defPolylineBreak = pb;
    }

    /**
     * Get default polygon break
     *
     * @return Default polygon break
     */
    public PolygonBreak getDefPolygonBreak() {
        return _defPolygonBreak;
    }

    /**
     * Set default polygon break
     *
     * @param pb Default polygon break
     */
    public void setDefPolygonBreak(PolygonBreak pb) {
        _defPolygonBreak = pb;
    }

    /**
     * Get measurement form
     *
     * @return Measurement form
     */
    public FrmMeasurement getMeasurementForm() {
        return _frmMeasure;
    }

    /**
     * set measurement form
     *
     * @param form Measurement form
     */
    public void setMeasurementForm(FrmMeasurement form) {
        _frmMeasure = form;
    }

    /**
     * Get view image
     *
     * @return View image
     */
    public BufferedImage getViewImage() {
        BufferedImage aImage = new BufferedImage(_pageBounds.width, _pageBounds.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = aImage.createGraphics();
        paintGraphics(g);
        return aImage;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Paint methods">
    @Override
    public int getWebMapZoom(){
        WebMapLayer layer = this.getActiveMapFrame().getMapView().getWebMapLayer();
        if (layer != null){
            return layer.getZoom();
        }
        return 0;
    }
    
    @Override
    public void reDraw(){
        //this.paintGraphics();
        this.repaintNew();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(this.getBackground());
        g2.clearRect(0, 0, this.getWidth(), this.getHeight());
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (this.newPaint) {
            this.paintGraphicsAll(g2);
        } else {
            //g2.drawImage(this._layoutBitmap, _xShift, _yShift, this.getBackground(), this);
            AffineTransform mx = new AffineTransform();
            mx.translate((float) _xShift, (float) _yShift);
            AffineTransformOp aop = new AffineTransformOp(mx, AffineTransformOp.TYPE_BILINEAR);
            g2.drawImage(this._layoutBitmap, aop, 0, 0);
        }

        if (this._dragMode) {
            Rectangle rect = new Rectangle();
            float dash1[] = {2.0f};
            switch (this._mouseMode) {
                case Map_ZoomIn:
                    rect.width = Math.abs(_mouseLastPos.x - _mouseDownPoint.x);
                    rect.height = Math.abs(_mouseLastPos.y - _mouseDownPoint.y);
                    rect.x = Math.min(_mouseLastPos.x, _mouseDownPoint.x);
                    rect.y = Math.min(_mouseLastPos.y, _mouseDownPoint.y);
                    //g2.setColor(this.getForeground());
                    g2.setColor(Color.black);
                    g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
                    g2.draw(rect);
                    break;
                case MoveSelection:
                    rect.x = _selectedRectangle.x + _mouseLastPos.x - _mouseDownPoint.x;
                    rect.y = _selectedRectangle.y + _mouseLastPos.y - _mouseDownPoint.y;
                    rect.width = _selectedRectangle.width;
                    rect.height = _selectedRectangle.height;
                    g2.setColor(Color.red);
                    g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
                    g2.draw(rect);
                    break;
                case ResizeSelected:
                    g2.setColor(Color.red);
                    g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
                    g2.draw(_selectedRectangle);
                    break;
                case CreateSelection:
                case New_Rectangle:
                case New_Ellipse:
                case Map_SelectFeatures_Rectangle:
                    int sx = Math.min(_mouseDownPoint.x, _mouseLastPos.x);
                    int sy = Math.min(_mouseDownPoint.y, _mouseLastPos.y);
                    g2.setColor(this.getForeground());
                    g2.draw(new Rectangle(sx, sy, Math.abs(_mouseLastPos.x - _mouseDownPoint.x),
                            Math.abs(_mouseLastPos.y - _mouseDownPoint.y)));
                    break;
                case New_Freehand:
                case Map_SelectFeatures_Lasso:
                    List<PointF> points = new ArrayList<>(_graphicPoints);
                    points.add(new PointF(_mouseLastPos.x, _mouseLastPos.y));
                    g2.setColor(this.getForeground());
                    _graphicPoints.add(new PointF(_mouseLastPos.x, _mouseLastPos.y));
                    Draw.drawPolyline(points, g2);
                    break;
                case New_Circle:
                case Map_SelectFeatures_Circle:
                    int radius = (int) Math.sqrt(Math.pow(_mouseLastPos.x - _mouseDownPoint.x, 2)
                            + Math.pow(_mouseLastPos.y - _mouseDownPoint.y, 2));
                    g2.setColor(this.getForeground());
                    g2.drawLine(_mouseDownPoint.x, _mouseDownPoint.y, _mouseLastPos.x, _mouseLastPos.y);
                    g2.drawOval(_mouseDownPoint.x - radius, _mouseDownPoint.y - radius,
                            radius * 2, radius * 2);
                    break;
                case InEditingVertices:
                    PointF p1 = pageToScreen((float) _editingVertices.get(1).X, (float) _editingVertices.get(1).Y);
                    PointF p2 = pageToScreen((float) _editingVertices.get(2).X, (float) _editingVertices.get(2).Y);
                    g2.setColor(Color.black);
                    g2.drawLine((int) p1.X, (int) p1.Y, _mouseLastPos.x, _mouseLastPos.y);
                    if (_editingVertices.size() == 3) {
                        g2.drawLine((int) p2.X, (int) p2.Y, _mouseLastPos.x, _mouseLastPos.y);
                    }

                    Rectangle nRect = new Rectangle(_mouseLastPos.x - 3, _mouseLastPos.y - 3, 6, 6);
                    g2.setColor(Color.cyan);
                    g2.fill(nRect);
                    g2.setColor(Color.black);
                    g2.draw(nRect);
                    break;
            }
        }

        switch (this._mouseMode) {
            case New_Polyline:
            case New_Polygon:
            case New_Curve:
            case New_CurvePolygon:
            case Map_SelectFeatures_Polygon:
                if (!_startNewGraphic) {
                    List<PointF> points = new ArrayList<>(_graphicPoints);
                    points.add(new PointF(_mouseLastPos.x, _mouseLastPos.y));
                    g2.setColor(this.getForeground());
                    switch (_mouseMode) {
                        case New_Polyline:
                            Draw.drawPolyline(points, g2);
                            break;
                        case New_Polygon:
                            points.add(points.get(0));
                            Draw.drawPolyline(points, g2);
                            break;
                        case New_Curve:
                            Draw.drawCurveLine(points, g2);
                            break;
                        case New_CurvePolygon:
                            points.add(points.get(0));
                            Draw.drawCurveLine(points, g2);
                            break;
                    }
                }
                break;
            case Map_Measurement:
                if (!_startNewGraphic) {
                    //Draw graphic                                    
                    //g.SmoothingMode = SmoothingMode.AntiAlias;
                    PointF[] fpoints = (PointF[]) _graphicPoints.toArray(new PointF[_graphicPoints.size()]);
                    PointF[] points = new PointF[fpoints.length + 1];
                    System.arraycopy(fpoints, 0, points, 0, fpoints.length);
                    points[_graphicPoints.size()] = new PointF(_mouseLastPos.x, _mouseLastPos.y);

                    if (_frmMeasure.getMeasureType() == MeasureTypes.Length) {
                        g2.setColor(Color.red);
                        g2.setStroke(new BasicStroke(2));
                        Draw.drawPolyline(points, g2);
                    } else {
                        PointF[] ppoints = new PointF[points.length + 1];
                        System.arraycopy(points, 0, ppoints, 0, points.length);
                        ppoints[ppoints.length - 1] = _graphicPoints.get(0);
                        Color aColor = new Color(Color.blue.getRed(), Color.blue.getGreen(), Color.blue.getBlue(), 100);
                        g2.setColor(aColor);
                        PolygonBreak aPB = new PolygonBreak();
                        aPB.setColor(aColor);
                        Draw.drawPolygon(ppoints, aPB, g2);
                        g2.setColor(Color.red);
                        Draw.drawPolyline(ppoints, g2);
                    }
                }
                break;
        }

        if (this._currentLayoutMap != null) {
            if (this._currentLayoutMap.getMapFrame().getMapView().isDrawIdentiferShape()) {
                int selLayerHandle = this._currentLayoutMap.getMapFrame().getMapView().getSelectedLayerHandle();
                if (selLayerHandle >= 0) {
                    MapLayer aLayer = this._currentLayoutMap.getMapFrame().getMapView().getLayerByHandle(selLayerHandle);
                    if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
                        VectorLayer vLayer = (VectorLayer) aLayer;
                        Rectangle rect = getElementViewExtent(_currentLayoutMap);
                        this._currentLayoutMap.getMapFrame().getMapView().drawIdShape(g2, vLayer.getShapes().get(vLayer.getIdentiferShape()), rect);
                    }
                }
            }
        }
    }

    /**
     * New paint
     */
    public void repaintNew() {
        if (this.doubleBuffer) {
            this.newPaint = false;
            this.paintGraphics();
        } else {
            this.newPaint = true;
            this.repaint();
            this.updateViewImage();
        }
    }

    private void repaintOld() {
        if (this.doubleBuffer) {
            this.repaint();
        } else {
            this.newPaint = false;
            this.repaint();
        }
    }

    private void updateViewImage() {
        if (this.getWidth() < 5 || this.getHeight() < 5) {
            return;
        }

        int width = this.getWidth();
        int height = this.getHeight();

        this._layoutBitmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = this._layoutBitmap.createGraphics();
        this.print(g);
        g.dispose();
    }

    public void paintGraphicsAll(Graphics2D g) {
        if (this._lockViewUpdate) {
            return;
        }

        if (this.getWidth() < 10 || this.getHeight() < 10) {
            return;
        }

        if ((this._pageBounds.width < 2) || (this._pageBounds.height < 2)) {
            return;
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //Judge if show scroll bar
        int pageHeight = (int) (_pageBounds.height * _zoom);
        int pageWidth = (int) (_pageBounds.width * _zoom);
        if (pageHeight > this.getHeight()) {
            int sHeight = pageHeight - this.getHeight() + 40;
            _vScrollBar.setMinimum(0);
            _vScrollBar.setMaximum(pageHeight);
            _vScrollBar.setVisibleAmount(pageHeight - sHeight);
            _vScrollBar.setUnitIncrement(pageHeight / 10);
            _vScrollBar.setBlockIncrement(pageHeight / 5);
            if (_vScrollBar.getWidth() == 0) {
                _vScrollBar.setSize(21, this._vScrollBar.getHeight());
            }

            if (_vScrollBar.isVisible() == false) {
                _vScrollBar.setValue(0);
                _vScrollBar.setVisible(true);
            }
        } else {
            _pageBounds.y = 0;
            this._pageLocation.Y = 0;
            _vScrollBar.setVisible(false);
        }

        if (pageWidth > this.getWidth()) {
            int sWidth = pageWidth - this.getWidth() + 40;
            _hScrollBar.setMinimum(0);
            _hScrollBar.setMaximum(pageWidth);
            _hScrollBar.setVisibleAmount(pageWidth - sWidth);
            _hScrollBar.setUnitIncrement(pageWidth / 10);
            _hScrollBar.setBlockIncrement(pageWidth / 5);
            if (this._hScrollBar.getHeight() == 0) {
                this._hScrollBar.setSize(this._hScrollBar.getWidth(), 21);
            }

            if (_hScrollBar.isVisible() == false) {
                _hScrollBar.setValue(0);
                _hScrollBar.setVisible(true);
            }
        } else {
            _pageBounds.x = 0;
            this._pageLocation.X = 0;
            _hScrollBar.setVisible(false);
        }

        //Draw bound rectangle
        Rectangle.Float aRect = pageToScreen(_pageBounds.x, _pageBounds.y, _pageBounds.width, _pageBounds.height);
        g.setColor(_pageBackColor);
        g.fill(aRect);

        //Draw layout elements
        paintGraphicsOnLayout(g);
    }

    public void paintGraphics() {
        if (this._lockViewUpdate) {
            return;
        }

        if (this.getWidth() < 10 || this.getHeight() < 10) {
            return;
        }

        if ((this._pageBounds.width < 2) || (this._pageBounds.height < 2)) {
            return;
        }

        _layoutBitmap = new BufferedImage(this.getWidth(),
                this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = _layoutBitmap.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //Judge if show scroll bar
        int pageHeight = (int) (_pageBounds.height * _zoom);
        int pageWidth = (int) (_pageBounds.width * _zoom);
        if (pageHeight > this.getHeight()) {
            int sHeight = pageHeight - this.getHeight() + 40;
            _vScrollBar.setMinimum(0);
            _vScrollBar.setMaximum(pageHeight);
            _vScrollBar.setVisibleAmount(pageHeight - sHeight);
            _vScrollBar.setUnitIncrement(pageHeight / 10);
            _vScrollBar.setBlockIncrement(pageHeight / 5);
            if (_vScrollBar.getWidth() == 0) {
                _vScrollBar.setSize(21, this._vScrollBar.getHeight());
            }

            if (_vScrollBar.isVisible() == false) {
                _vScrollBar.setValue(0);
                _vScrollBar.setVisible(true);
            }
        } else {
            _pageBounds.y = 0;
            this._pageLocation.Y = 0;
            _vScrollBar.setVisible(false);
        }

        if (pageWidth > this.getWidth()) {
            int sWidth = pageWidth - this.getWidth() + 40;
            _hScrollBar.setMinimum(0);
            _hScrollBar.setMaximum(pageWidth);
            _hScrollBar.setVisibleAmount(pageWidth - sWidth);
            _hScrollBar.setUnitIncrement(pageWidth / 10);
            _hScrollBar.setBlockIncrement(pageWidth / 5);
            if (this._hScrollBar.getHeight() == 0) {
                this._hScrollBar.setSize(this._hScrollBar.getWidth(), 21);
            }

            if (_hScrollBar.isVisible() == false) {
                _hScrollBar.setValue(0);
                _hScrollBar.setVisible(true);
            }
        } else {
            _pageBounds.x = 0;
            this._pageLocation.X = 0;
            _hScrollBar.setVisible(false);
        }

        //Draw bound rectangle
        Rectangle.Float aRect = pageToScreen(_pageBounds.x, _pageBounds.y, _pageBounds.width, _pageBounds.height);
        g.setColor(_pageBackColor);
        g.fill(aRect);

        //Draw layout elements
        paintGraphicsOnLayout(g);

        g.dispose();
        this.repaint();
    }

    /**
     * Paint graphics on layout
     *
     * @param g Graphics2D
     */
    public void paintGraphicsOnLayout(Graphics2D g) {
        //g.SmoothingMode = _smoothingMode;
        //g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        for (LayoutElement aElement : _layoutElements) {
            if (!aElement.isVisible()) {
                continue;
            }

            aElement.paintOnLayout(g, _pageLocation, _zoom);
        }

        //Draws the selection rectangle around each selected item 
        for (LayoutElement aElement : _layoutElements) {
            if (!aElement.isVisible()) {
                continue;
            }

            if (aElement.isSelected()) {
                if (_mouseMode == MouseMode.EditVertices) {
                    LayoutGraphic aLG = (LayoutGraphic) aElement;
                    List<PointD> points = (List<PointD>) aLG.getGraphic().getShape().getPoints();
                    drawSelectedVertices(g, points);
                } else {
                    float[] dashPattern = new float[]{2.0F, 1.0F};
                    Rectangle aRect = pageToScreen(aElement.getBounds());
                    g.setColor(Color.cyan);
                    g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                    g.draw(aRect);

                    switch (aElement.getResizeAbility()) {
                        case SameWidthHeight:
                            drawSelectedConers(g, aElement);
                            break;
                        case ResizeAll:
                            drawSelectedConers(g, aElement);
                            drawSelectedEdgeCenters(g, aElement);
                            break;
                    }
                }
            }
        }
    }

    /**
     * Paint graphics
     *
     * @param g Graphics2D
     */
    public void paintGraphics(Graphics2D g) {
        g.setColor(this._pageBackColor);
        g.fillRect(0, 0, _pageBounds.width, _pageBounds.height);

        for (LayoutElement aElement : _layoutElements) {
            if (!aElement.isVisible()) {
                continue;
            }

            //aElement.Paint(g);
            aElement.paintOnLayout(g, new PointF(0, 0), 1);
        }
    }

    private void drawSelectedConers(Graphics2D g, LayoutElement aElement) {
        Rectangle elementRect = pageToScreen(aElement.getBounds());
        int size = 6;
        Rectangle rect = new Rectangle(elementRect.x - size / 2, elementRect.y - size / 2, size, size);
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.y = elementRect.y + elementRect.height - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.x = elementRect.x + elementRect.width - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.y = elementRect.y - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
    }

    private void drawSelectedEdgeCenters(Graphics2D g, LayoutElement aElement) {
        Rectangle elementRect = pageToScreen(aElement.getBounds());
        int size = 6;
        Rectangle rect = new Rectangle(elementRect.x + elementRect.width / 2 - size / 2, elementRect.y - size / 2, size, size);
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.y = elementRect.y + elementRect.height - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.x = elementRect.x - size / 2;
        rect.y = elementRect.y + elementRect.height / 2 - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.x = elementRect.x + elementRect.width - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
    }

    private void drawSelectedVertices(Graphics2D g, List<PointD> points) {
        int size = 6;
        Rectangle rect = new Rectangle(0, 0, size, size);

        for (PointD aPoint : points) {
            PointF aP = pageToScreen((float) aPoint.X, (float) aPoint.Y);
            rect.x = (int) aP.X - size / 2;
            rect.y = (int) aP.Y - size / 2;
            g.setColor(Color.cyan);
            g.fill(rect);
            g.setColor(Color.black);
            g.draw(rect);
        }
    }

    /**
     * Export to a picture file
     *
     * @param aFile File path
     * @throws java.io.FileNotFoundException
     * @throws javax.print.PrintException
     */
    public void exportToPicture(String aFile) throws FileNotFoundException, PrintException, IOException {
        if (aFile.endsWith(".ps")) {
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
            String mimeType = "application/postscript";
            StreamPrintServiceFactory[] factories = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, mimeType);
            FileOutputStream out = new FileOutputStream(aFile);
            if (factories.length > 0) {
                PrintService service = factories[0].getPrintService(out);
                SimpleDoc doc = new SimpleDoc(new Printable() {
                    @Override
                    public int print(Graphics g, PageFormat pf, int page) {
                        if (page >= 1) {
                            return Printable.NO_SUCH_PAGE;
                        } else {
                            double sf1 = pf.getImageableWidth() / (_pageBounds.width + 1);
                            double sf2 = pf.getImageableHeight() / (_pageBounds.height + 1);
                            double s = Math.min(sf1, sf2);
                            Graphics2D g2 = (Graphics2D) g;
                            g2.translate((pf.getWidth() - pf.getImageableWidth()) / 2, (pf.getHeight() - pf.getImageableHeight()) / 2);
                            g2.scale(s, s);

                            paintGraphics(g2);
                            return Printable.PAGE_EXISTS;
                        }
                    }
                }, flavor, null);
                DocPrintJob job = service.createPrintJob();
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                job.print(doc, attributes);
                out.close();
            }
        } else if (aFile.endsWith(".eps")) {
            int width = this.getPaperWidth();
            int height = this.getPaperHeight();
//            EPSGraphics2D g = new EPSGraphics2D(0.0, 0.0, width, height);
//            paintGraphics(g);
//            FileOutputStream file = new FileOutputStream(aFile);
//            try {
//                file.write(g.getBytes());
//            } finally {
//                file.close();
//                g.dispose();
//            }

            Properties p = new Properties();
            p.setProperty("PageSize", "A5");
            VectorGraphics g = new PSGraphics2D(new File(aFile), new Dimension(width, height));
            //g.setProperties(p);
            g.startExport();
            this.paintGraphics(g);
            g.endExport();
            g.dispose();
        } else if (aFile.endsWith(".pdf")) {
            int width = this.getPaperWidth();
            int height = this.getPaperHeight();
            try {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document(new com.itextpdf.text.Rectangle(width, height));
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(aFile));
                document.open();
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate pdfTemp = cb.createTemplate(width, height); 
                Graphics2D g2 = new PdfGraphics2D(pdfTemp, width, height, true);
                this.paintGraphics(g2);
                g2.dispose(); 
                cb.addTemplate(pdfTemp, 0, 0);
                document.close();
            } catch (DocumentException | FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (aFile.endsWith(".emf")) {
            int width = this.getPaperWidth();
            int height = this.getPaperHeight();
            VectorGraphics g = new EMFGraphics2D(new File(aFile), new Dimension(width, height));
            //g.setProperties(p);
            g.startExport();
            this.paintGraphics(g);
            g.endExport();
            g.dispose();
        } else {
            String extension = aFile.substring(aFile.lastIndexOf('.') + 1);
            BufferedImage aImage;
            if (extension.equalsIgnoreCase("bmp"))
                aImage = new BufferedImage(_pageBounds.width, _pageBounds.height, BufferedImage.TYPE_INT_RGB);
            else
                aImage = new BufferedImage(_pageBounds.width, _pageBounds.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = aImage.createGraphics();
            paintGraphics(g);            
            if (extension.equalsIgnoreCase("jpg")) {
                BufferedImage newImage = new BufferedImage(aImage.getWidth(), aImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                newImage.createGraphics().drawImage(aImage, 0, 0, Color.BLACK, null);
                ImageIO.write(newImage, extension, new File(aFile));
            } else {
                ImageIO.write(aImage, extension, new File(aFile));
            }
        }
    }
    
    /**
     * Export to a picture file
     *
     * @param fileName File path
     * @param dpi DPI
     * @throws java.io.FileNotFoundException
     * @throws javax.print.PrintException
     */
    public void exportToPicture(String fileName, Integer dpi) throws FileNotFoundException, PrintException, IOException {
        if (dpi == null) {
            exportToPicture(fileName);
        } else {
            File output = new File(fileName);
            output.delete();

            int width = _pageBounds.width;
            int height = _pageBounds.height;
            String formatName = fileName.substring(fileName.lastIndexOf('.') + 1);
            if (formatName.equals("jpg")) {
                formatName = "jpeg";
                saveImage_Jpeg(fileName, width, height, dpi);
                return;
            }

            double scaleFactor = dpi / 72.0;
            BufferedImage image = new BufferedImage((int)(width * scaleFactor), (int)(height * scaleFactor), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            AffineTransform at = g.getTransform();
            at.scale(scaleFactor, scaleFactor);
            g.setTransform(at);
            paintGraphics(g);
            for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
                ImageWriter writer = iw.next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
                IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
                if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                    continue;
                }

                ImageUtil.setDPI(metadata, dpi);

                final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
                try {
                    writer.setOutput(stream);
                    writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
                } finally {
                    stream.close();
                }
                break;
            }
            g.dispose();
        }
    }
    
    private boolean saveImage_Jpeg(String file, int width, int height, int dpi) {
        double scaleFactor = dpi / 72.0;
        BufferedImage bufferedImage = new BufferedImage((int)(width * scaleFactor), (int)(height * scaleFactor), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        AffineTransform at = g.getTransform();
        at.scale(scaleFactor, scaleFactor);
        g.setTransform(at);
        paintGraphics(g);

        try {
            // Image writer 
            ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(new File(file));
            imageWriter.setOutput(ios);

            // Compression
            JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(0.85f);

            // Metadata (dpi)
            IIOMetadata data = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(bufferedImage), jpegParams);
            Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", Integer.toString(dpi));
            jfif.setAttribute("Ydensity", Integer.toString(dpi));
            jfif.setAttribute("resUnits", "1"); // density is dots per inch	
            data.setFromTree("javax_imageio_jpeg_image_1.0", tree);

            // Write and clean up
            imageWriter.write(null, new IIOImage(bufferedImage, null, data), jpegParams);
            ios.close();
            imageWriter.dispose();
        } catch (Exception e) {
            return false;
        }
        g.dispose();

        return true;
    }
    
    // </editor-fold>
    // <editor-fold desc="Coordinate transfer">

    /**
     * Convert screen coordinate to page coordinate
     *
     * @param screenX Screen x
     * @param screenY Screen y
     * @return Page position
     */
    public Point screenToPage(int screenX, int screenY) {
        float x = (screenX - _pageLocation.X) / _zoom;
        float y = (screenY - _pageLocation.Y) / _zoom;
        return (new Point((int) x, (int) y));
    }

    /**
     * Convert screen coordinate to page coordinate
     *
     * @param screenX Screen x
     * @param screenY Screen y
     * @return Page position
     */
    public PointF screenToPage(float screenX, float screenY) {
        float x = (screenX - _pageLocation.X) / _zoom;
        float y = (screenY - _pageLocation.Y) / _zoom;
        return (new PointF(x, y));
    }

    private PointF pageToScreen(float pageX, float pageY) {
        float x = pageX * _zoom + _pageLocation.X;
        float y = pageY * _zoom + _pageLocation.Y;
        return (new PointF(x, y));
    }

    private Rectangle.Float pageToScreen(float pageX, float pageY, float pageW, float pageH) {
        PointF screenTL = pageToScreen(pageX, pageY);
        PointF screenBR = pageToScreen(pageX + pageW, pageY + pageH);
        return new Rectangle.Float(screenTL.X, screenTL.Y, screenBR.X - screenTL.X, screenBR.Y - screenTL.Y);
    }

    private Rectangle pageToScreen(Rectangle rect) {
        PointF screenTL = pageToScreen(rect.x, rect.y);
        PointF screenBR = pageToScreen(rect.x + rect.width, rect.y + rect.height);
        return new Rectangle((int) screenTL.X, (int) screenTL.Y, (int) (screenBR.X - screenTL.X), (int) (screenBR.Y - screenTL.Y));
    }

    private Rectangle.Float paperToScreen(Rectangle.Float paper) {
        return paperToScreen(paper.x, paper.y, paper.width, paper.height);
    }

    /**
     * Converts a rectangle in paper coordiants in 1/100 of an inch to screen
     * coordinants
     *
     * @param paperX Paper x
     * @param paperY Paper Y
     * @param paperW Paper width
     * @param paperH Paper height
     * @return Screen rectangle
     */
    private Rectangle.Float paperToScreen(float paperX, float paperY, float paperW, float paperH) {
        PointF screenTL = paperToScreen(paperX, paperY);
        PointF screenBR = paperToScreen(paperX + paperW, paperY + paperH);
        return new Rectangle.Float(screenTL.X, screenTL.Y, screenBR.X - screenTL.X, screenBR.Y - screenTL.Y);
    }

    /**
     * Converts between a point in paper coordinants in 1/100th of an inch to
     * screen coordinants
     *
     * @param paperX Paper x
     * @param paperY Paper y
     * @return Screen point
     */
    private PointF paperToScreen(float paperX, float paperY) {
        float screenX = (paperX / 100F * 96F * _zoom) + _pageLocation.X;
        float screenY = (paperY / 100F * 96F * _zoom) + _pageLocation.Y;
        return (new PointF(screenX, screenY));
    }
    // </editor-fold>
    // <editor-fold desc="Elements">

    /**
     * Update the order of the map frames
     */
    public void updateMapFrameOrder() {
        List<LayoutMap> lms = getLayoutMaps();
        for (int i = 0; i < lms.size(); i++) {
            lms.get(i).getMapFrame().setOrder(i);
        }
    }

    /**
     * Update map frames
     *
     * @param mapFrames The map frames
     */
    public void updateMapFrames(List<MapFrame> mapFrames) {
        for (MapFrame mf : mapFrames) {
            boolean isNew = true;
            for (MapFrame aMF : _mapFrames) {
                if (mf == aMF) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                LayoutMap aLM = new LayoutMap(mf, this.tileLoadListener);
                addElement(aLM);
            }
        }

        for (int i = 0; i < _mapFrames.size(); i++) {
            MapFrame mf = _mapFrames.get(i);
            boolean isNew = true;
            for (MapFrame aMF : mapFrames) {
                if (mf == aMF) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                LayoutMap aLM = getLayoutMap(mf);
                if (aLM != null) {
                    removeElement(aLM);
                    i -= 1;
                }
            }
        }

        this.setMapFrames(mapFrames);
    }

    /**
     * Add a layout element
     *
     * @param aElement The layout element
     */
    public void addElement(LayoutElement aElement) {
        _layoutElements.add(aElement);
        if (aElement.getElementType() == ElementType.LayoutMap) {
            final LayoutMap aLM = (LayoutMap) aElement;
            aLM.addMapViewUpdatedListener(new IMapViewUpdatedListener() {
                @Override
                public void mapViewUpdatedEvent(MapViewUpdatedEvent event) {
                    //if (aLM.getMapFrame().isFireMapViewUpdate()) {
                    //paintGraphics();
                    repaintNew();
                    //}
                }
            });
            if (aLM.getMapFrame().isActive()) {
                _currentLayoutMap = aLM;
            }
        }
    }

    /**
     * Remove a layout element
     *
     * @param aElement The layout element
     */
    public void removeElement(LayoutElement aElement) {
        switch (aElement.getElementType()) {
            case LayoutMap:
                if (this.getLayoutMaps().size() == 1) {
                    JOptionPane.showMessageDialog(this, "There is at least one layout map!");
                    return;
                }

                LayoutMap aLM = (LayoutMap) aElement;
                for (int i = 0; i < _layoutElements.size(); i++) {
                    LayoutElement aLE = _layoutElements.get(i);
                    switch (aLE.getElementType()) {
                        case LayoutLegend:
                            if (((LayoutLegend) aLE).getLayoutMap() == aLM) {
                                _layoutElements.remove(aLE);
                                i -= 1;
                            }
                            break;
                        case LayoutScaleBar:
                            if (((LayoutScaleBar) aLE).getLayoutMap() == aLM) {
                                _layoutElements.remove(aLE);
                                i -= 1;
                            }
                            break;
                        case LayoutNorthArraw:
                            if (((LayoutNorthArrow) aLE).getLayoutMap() == aLM) {
                                _layoutElements.remove(aLE);
                                i -= 1;
                            }
                            break;
                    }
                }
                _mapFrames.remove(aLM.getMapFrame());
                _layoutElements.remove(aElement);
                if (_mapFrames.size() > 0) {
                    setActiveMapFrame(_mapFrames.get(0));
                }
                this.fireMapFramesUpdatedEvent();
                break;
            default:
                _layoutElements.remove(aElement);
                break;
        }
//        if (this._selectedElements.contains(aElement)){
//            this._selectedElements.remove(aElement);
//        }
    }

    /**
     * Add a text label element
     *
     * @param text The text
     * @param x Center x
     * @param y Center y
     * @return Text layout graphic
     */
    public LayoutGraphic addText(String text, int x, int y) {
        return addText(text, x, y, _defLabelBreak.getFont().getFontName(), _defLabelBreak.getFont().getSize());
    }

    /**
     * Add a text label element
     *
     * @param text The text
     * @param x Center x
     * @param y Center y
     * @param fontSize Font size
     * @return Text layout graphic
     */
    public LayoutGraphic addText(String text, int x, int y, float fontSize) {
        return addText(text, x, y, _defLabelBreak.getFont().getName(), fontSize);
    }

    /**
     * Add a text label element
     *
     * @param text The text
     * @param x Center x
     * @param y Center y
     * @param fontName Font name
     * @param fontSize Font size
     * @return Text layout graphic
     */
    public LayoutGraphic addText(String text, int x, int y, String fontName, float fontSize) {
        PointShape aPS = new PointShape();
        aPS.setPoint(new PointD(x, y));
        LabelBreak aLB = (LabelBreak) _defLabelBreak.clone();
        aLB.setText(text);
        aLB.setFont(new Font(fontName, Font.PLAIN, (int) fontSize));
        Graphic aGraphic = new Graphic(aPS, aLB);
        LayoutGraphic aLayoutGraphic = new LayoutGraphic(aGraphic, this);
        addElement(aLayoutGraphic);

        return aLayoutGraphic;
    }

    public LayoutGraphic addWindArrow(int left, int top) {
        WindArrow aWindArraw = new WindArrow();
        //aWindArraw.setPoint(new PointD(left, top));
        aWindArraw.angle = 270;
        aWindArraw.length = 20;
        VectorBreak aVB = new VectorBreak();
        aVB.setColor(Color.black);
        LayoutGraphic wag = new LayoutGraphic(new Graphic(aWindArraw, aVB), this,
                this.getActiveLayoutMap());
        wag.setLeft(left);
        wag.setTop(top);
        addElement(wag);

        return wag;
    }

    /**
     * Add a layout legend
     *
     * @param left Left
     * @param top Top
     * @return Layout legend
     */
    public LayoutLegend addLegend(int left, int top) {
        LayoutMap aLM = getActiveLayoutMap();
        LayoutLegend aLL = new LayoutLegend(this, aLM);
        aLL.setLeft(left);
        aLL.setTop(top);
        if (aLM.getMapFrame().getMapView().getLayerNum() > 0) {
            for (MapLayer aLayer : aLM.getMapFrame().getMapView().getLayers()) {
                if (aLayer.getLayerType() != LayerTypes.ImageLayer) {
                    aLL.setLegendLayer(aLayer);
                }
            }
        }

        addElement(aLL);

        return aLL;
    }

    /**
     * Add a layout scale bar
     *
     * @param left Left
     * @param top Top
     * @return Layout scale bar
     */
    public LayoutScaleBar addScaleBar(int left, int top) {
        LayoutMap aLM = getActiveLayoutMap();
        LayoutScaleBar aLSB = new LayoutScaleBar(aLM);
        aLSB.setLeft(left);
        aLSB.setTop(top);
        addElement(aLSB);

        return aLSB;
    }

    /**
     * Add a layout north arrow
     *
     * @param left Left
     * @param top Top
     * @return Layout north arrow
     */
    public LayoutNorthArrow addNorthArrow(int left, int top) {
        LayoutMap aLM = getActiveLayoutMap();
        LayoutNorthArrow aLNA = new LayoutNorthArrow(aLM);
        aLNA.setLeft(left);
        aLNA.setTop(top);
        addElement(aLNA);

        return aLNA;
    }

    /**
     * Add a layout chart
     *
     * @param left Left
     * @param top Top
     * @return Layout chart
     */
    public LayoutChart addChart(int left, int top) {
        LayoutChart chart = new LayoutChart();
        chart.setLeft(left);
        chart.setTop(top);
        addElement(chart);

        return chart;
    }

    /**
     * Get layout graphic list
     *
     * @return Layout graphic list
     */
    public List<LayoutGraphic> getLayoutGraphics() {
        List<LayoutGraphic> graphics = new ArrayList<>();
        for (LayoutElement aLE : _layoutElements) {
            if (aLE.getElementType() == ElementType.LayoutGraphic) {
                graphics.add((LayoutGraphic) aLE);
            }
        }

        return graphics;
    }

    /**
     * Get text graphic list
     *
     * @return Text graphic list
     */
    public List<LayoutGraphic> getTexts() {
        List<LayoutGraphic> texts = new ArrayList<>();
        List<LayoutGraphic> graphics = getLayoutGraphics();
        for (LayoutGraphic aLG : graphics) {
            if (aLG.getGraphic().getLegend().getBreakType() == BreakTypes.LabelBreak) {
                texts.add(aLG);
            }
        }

        return texts;
    }

    /**
     * Get a text graphic by text string
     *
     * @param text Text string
     * @return Text graphic
     */
    public LayoutGraphic getText(String text) {
        List<LayoutGraphic> texts = getTexts();
        for (LayoutGraphic aLG : texts) {
            if (((LabelBreak) aLG.getGraphic().getLegend()).getText().equals(text)) {
                return aLG;
            }
        }

        return null;
    }

    /**
     * Get layout legend list
     *
     * @return Layout legend list
     */
    public List<LayoutLegend> getLegends() {
        List<LayoutLegend> legends = new ArrayList<>();
        for (LayoutElement aLE : _layoutElements) {
            if (aLE.getElementType() == ElementType.LayoutLegend) {
                legends.add((LayoutLegend) aLE);
            }
        }

        return legends;
    }

    /**
     * Set a map frame as active
     *
     * @param mapFrame The map frame
     */
    public void setActiveMapFrame(MapFrame mapFrame) {
        for (MapFrame mf : _mapFrames) {
            mf.setActive(false);
        }

        mapFrame.setActive(true);
        this.fireActiveMapFrameChangedEvent();
    }

    private LayoutMap getLayoutMap(MapFrame mapFrame) {
        LayoutMap aLM = null;
        for (LayoutMap lm : this.getLayoutMaps()) {
            if (lm.getMapFrame() == mapFrame) {
                aLM = lm;
                break;
            }
        }

        return aLM;
    }

    private LayoutMap getLayoutMap(Point aPoint) {
        for (int i = getLayoutMaps().size() - 1; i >= 0; i--) {
            LayoutMap aLM = getLayoutMaps().get(i);
            if (MIMath.pointInRectangle(aPoint, aLM.getBounds())) {
                return aLM;
            }
        }

        return null;
    }

    private int getLayoutMapIndex(LayoutMap aLM) {
        return getLayoutMaps().indexOf(aLM);
    }

    /**
     * If has legend element
     *
     * @return Boolean
     */
    public boolean hasLegendElement() {
        for (LayoutElement aLE : _layoutElements) {
            if (aLE.getElementType() == ElementType.LayoutLegend) {
                return true;
            }
        }
        return false;
    }

    private List<LayoutElement> selectElements(Point aPoint, List<LayoutElement> baseElements, int limit) {
        List<LayoutElement> selectedElements = new ArrayList<>();
        for (int i = baseElements.size() - 1; i >= 0; i--) {
            LayoutElement element = baseElements.get(i);
            if (element.isVisible()) {
                Rectangle rect = (Rectangle) element.getBounds().clone();
                if (rect.width < 5) {
                    rect.width = 5;
                    rect.x -= 2.5;
                }
                if (rect.height < 5) {
                    rect.height = 5;
                    rect.y -= 2.5;
                }
                rect.width += limit;
                rect.height += limit;
                if (MIMath.pointInRectangle(aPoint, rect)) {
                    selectedElements.add(element);
                }
            }
        }

        return selectedElements;
    }

    private Rectangle getElementViewExtent(LayoutElement aLE) {
        PointF aP = aLE.pageToScreen(aLE.getLeft(), aLE.getTop(), _pageLocation, _zoom);
        Rectangle rect = new Rectangle((int) aP.X, (int) aP.Y, (int) (aLE.getWidth() * _zoom), (int) (aLE.getHeight() * _zoom));

        return rect;
    }

    private int selectEditVertices(Point aPoint, org.meteoinfo.shape.Shape aShape, List<PointD> vertices) {
        List<PointD> points = (List<PointD>) aShape.getPoints();
        int buffer = 4;
        Rectangle rect = new Rectangle(aPoint.x - buffer / 2, aPoint.y - buffer / 2, buffer, buffer);
        vertices.clear();
        PointD aPD;
        int vIdx = -1;
        for (int i = 0; i < points.size(); i++) {
            if (MIMath.pointInRectangle(points.get(i), rect)) {
                vIdx = i;
                vertices.add(points.get(i));
                switch (aShape.getShapeType()) {
                    case Polyline:
                    case CurveLine:
                        if (i == 0) {
                            vertices.add(points.get(i + 1));
                        } else if (i == points.size() - 1) {
                            vertices.add(points.get(i - 1));
                        } else {
                            vertices.add(points.get(i - 1));
                            vertices.add(points.get(i + 1));
                        }
                        break;
                    default:
                        if (i == 0) {
                            vertices.add(points.get(i + 1));
                            aPD = points.get(points.size() - 1);
                            if (aPD.X == points.get(i).X && aPD.Y == points.get(i).Y) {
                                vertices.add(points.get(points.size() - 2));
                            } else {
                                vertices.add(aPD);
                            }
                        } else if (i == points.size() - 1) {
                            vertices.add(points.get(i - 1));
                            aPD = points.get(0);
                            if (aPD.X == points.get(i).X && aPD.Y == points.get(i).Y) {
                                vertices.add(points.get(1));
                            } else {
                                vertices.add(points.get(0));
                            }
                        } else {
                            vertices.add(points.get(i - 1));
                            vertices.add(points.get(i + 1));
                        }
                        break;
                }
                break;
//                if (aShape.getShapeType() == ShapeTypes.Polyline) {
//                    if (i == 0) {
//                        vertices.add(points.get(i + 1));
//                    } else if (i == points.size() - 1) {
//                        vertices.add(points.get(i - 1));
//                    } else {
//                        vertices.add(points.get(i - 1));
//                        vertices.add(points.get(i + 1));
//                    }
//                } else {
//                    if (i == 0) {
//                        vertices.add(points.get(i + 1));
//                        aPD = points.get(points.size() - 1);
//                        if (aPD.X == points.get(i).X && aPD.Y == points.get(i).Y) {
//                            vertices.add(points.get(points.size() - 2));
//                        } else {
//                            vertices.add(aPD);
//                        }
//                    } else if (i == points.size() - 1) {
//                        vertices.add(points.get(i - 1));
//                        aPD = points.get(0);
//                        if (aPD.X == points.get(i).X && aPD.Y == points.get(i).Y) {
//                            vertices.add(points.get(1));
//                        } else {
//                            vertices.add(points.get(0));
//                        }
//                    } else {
//                        vertices.add(points.get(i - 1));
//                        vertices.add(points.get(i + 1));
//                    }
//                }
            }
        }

        return vIdx;
    }

    private boolean isInLayoutMaps(Point aPoint) {
        for (LayoutMap aLM : getLayoutMaps()) {
            if (MIMath.pointInRectangle(aPoint, aLM.getBounds())) {
                return true;
            }
        }

        return false;
    }

    private static Edge intersectElementEdge(Rectangle screen, PointF pt, float limit) {
        Rectangle.Float ptRect = new Rectangle.Float(pt.X - limit, pt.Y - limit, 2F * limit, 2F * limit);
        if ((pt.X >= screen.x - limit && pt.X <= screen.x + limit) && (pt.Y >= screen.y - limit && pt.Y <= screen.y + limit)) {
            return Edge.TopLeft;
        }
        if ((pt.X >= screen.x + screen.width - limit && pt.X <= screen.x + screen.width + limit) && (pt.Y >= screen.y - limit && pt.Y <= screen.y + limit)) {
            return Edge.TopRight;
        }
        if ((pt.X >= screen.x + screen.width - limit && pt.X <= screen.x + screen.width + limit) && (pt.Y >= screen.y + screen.height - limit && pt.Y <= screen.y + screen.height + limit)) {
            return Edge.BottomRight;
        }
        if ((pt.X >= screen.x - limit && pt.X <= screen.x + limit) && (pt.Y >= screen.y + screen.height - limit && pt.Y <= screen.y + screen.height + limit)) {
            return Edge.BottomLeft;
        }
        if (ptRect.intersects(new Rectangle.Float(screen.x, screen.y, screen.width, 1F))) {
            return Edge.Top;
        }
        if (ptRect.intersects(new Rectangle.Float(screen.x, screen.y, 1F, screen.height))) {
            return Edge.Left;
        }
        if (ptRect.intersects(new Rectangle.Float(screen.x, screen.y + screen.height, screen.width, 1F))) {
            return Edge.Bottom;
        }
        if (ptRect.intersects(new Rectangle.Float(screen.x + screen.width, screen.y, 1F, screen.height))) {
            return Edge.Right;
        }
        return Edge.None;
    }
    // </editor-fold>

    // <editor-fold desc="Others">
    private void updatePageSet() {
        Rectangle.Float aRect = paperToScreen(new Rectangle.Float(0, 0, this.getPaperWidth(), this.getPaperHeight()));
        _pageBounds.width = (int) aRect.width;
        _pageBounds.height = (int) aRect.height;
    }

    /**
     * Show measurment form
     */
    public void showMeasurementForm() {
        if (_frmMeasure == null) {
            _frmMeasure = new FrmMeasurement((JFrame) SwingUtilities.getWindowAncestor(this), false);
            _frmMeasure.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    _currentLayoutMap.getMapFrame().getMapView().setDrawIdentiferShape(false);
                    //repaint();
                    repaintOld();
                }
            });
            _frmMeasure.setLocationRelativeTo(this);
            _frmMeasure.setVisible(true);
        } else if (!_frmMeasure.isVisible()) {
            _frmMeasure.setVisible(true);
        }
    }

    // </editor-fold>
    // <editor-fold desc="XML import and export">
    /**
     * Export project XML content
     *
     * @param m_Doc XML document
     * @param parent Parent XML element
     */
    public void exportProjectXML(Document m_Doc, Element parent) {
        exportLayout(m_Doc, parent);
    }

    private void exportLayout(Document m_Doc, Element parent) {
        Element layout = m_Doc.createElement("Layout");

        //Add attribute
        Attr BackColor = m_Doc.createAttribute("BackColor");
        Attr ForeColor = m_Doc.createAttribute("ForeColor");
        Attr SmoothingMode = m_Doc.createAttribute("SmoothingMode");
        Attr PaperSizeName = m_Doc.createAttribute("PaperSizeName");
        Attr PaperSizeWidth = m_Doc.createAttribute("PaperSizeWidth");
        Attr PaperSizeHeight = m_Doc.createAttribute("PaperSizeHeight");
        Attr Landscape = m_Doc.createAttribute("Landscape");

        BackColor.setValue(ColorUtil.toHexEncoding(_pageBackColor));
        ForeColor.setValue(ColorUtil.toHexEncoding(_pageForeColor));
        SmoothingMode.setValue(String.valueOf(_antiAlias));
        PaperSizeName.setValue(_paperSize.getName());
        PaperSizeWidth.setValue(String.valueOf(_paperSize.getWidth()));
        PaperSizeHeight.setValue(String.valueOf(_paperSize.getHeight()));
        Landscape.setValue(String.valueOf(_isLandscape));

        layout.setAttributeNode(BackColor);
        layout.setAttributeNode(ForeColor);
        layout.setAttributeNode(SmoothingMode);
        layout.setAttributeNode(PaperSizeName);
        layout.setAttributeNode(PaperSizeWidth);
        layout.setAttributeNode(PaperSizeHeight);
        layout.setAttributeNode(Landscape);

        parent.appendChild(layout);

        //Add layout elements
        addLayoutElements(m_Doc, layout);
    }

    private void addLayoutElements(Document doc, Element parent) {
        Element layoutElements = doc.createElement("LayoutElements");
        for (LayoutElement aElement : _layoutElements) {
            switch (aElement.getElementType()) {
                case LayoutMap:
                    addLayoutMapElement(doc, layoutElements, (LayoutMap) aElement);
                    break;
                case LayoutIllustration:
                    //AddIllustrationElement(ref doc, layoutElements, (LayoutIllustrationMap)aElement);
                    break;
                case LayoutLegend:
                    addLayoutLegendElement(doc, layoutElements, (LayoutLegend) aElement);
                    break;
                case LayoutGraphic:
                    addLayoutGraphicElement(doc, layoutElements, (LayoutGraphic) aElement);
                    break;
                case LayoutScaleBar:
                    addLayoutScaleBarElement(doc, layoutElements, (LayoutScaleBar) aElement);
                    break;
                case LayoutNorthArraw:
                    addLayoutNorthArrowElement(doc, layoutElements, (LayoutNorthArrow) aElement);
                    break;
            }
        }
        parent.appendChild(layoutElements);
    }

    private void addLayoutMapElement(Document m_Doc, Element parent, LayoutMap aMap) {
        Element layoutMap = m_Doc.createElement("LayoutMap");
        Attr elementType = m_Doc.createAttribute("ElementType");
        Attr left = m_Doc.createAttribute("Left");
        Attr top = m_Doc.createAttribute("Top");
        Attr width = m_Doc.createAttribute("Width");
        Attr height = m_Doc.createAttribute("Height");
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
        Attr gridLabelPosition = m_Doc.createAttribute("GridLabelPosition");
        Attr drawDegreeSymbol = m_Doc.createAttribute("DrawDegreeSymbol");
        Attr drawBackColor = m_Doc.createAttribute("DrawBackColor");

        elementType.setValue(aMap.getElementType().toString());
        left.setValue(String.valueOf(aMap.getLeft()));
        top.setValue(String.valueOf(aMap.getTop()));
        width.setValue(String.valueOf(aMap.getWidth()));
        height.setValue(String.valueOf(aMap.getHeight()));
        DrawMapNeatLine.setValue(String.valueOf(aMap.isDrawNeatLine()));
        MapNeatLineColor.setValue(ColorUtil.toHexEncoding(aMap.getNeatLineColor()));
        MapNeatLineSize.setValue(String.valueOf(aMap.getNeatLineSize()));
        GridLineColor.setValue(ColorUtil.toHexEncoding(aMap.getGridLineColor()));
        GridLineSize.setValue(String.valueOf(aMap.getGridLineSize()));
        GridLineStyle.setValue(aMap.getGridLineStyle().toString());
        DrawGridLine.setValue(String.valueOf(aMap.isDrawGridLine()));
        DrawGridLabel.setValue(String.valueOf(aMap.isDrawGridLabel()));
        GridFontName.setValue(aMap.getGridFont().getFontName());
        GridFontSize.setValue(String.valueOf(aMap.getGridFont().getSize()));
        GridXDelt.setValue(String.valueOf(aMap.getGridXDelt()));
        GridYDelt.setValue(String.valueOf(aMap.getGridYDelt()));
        GridXOrigin.setValue(String.valueOf(aMap.getGridXOrigin()));
        GridYOrigin.setValue(String.valueOf(aMap.getGridYOrigin()));
        gridLabelPosition.setValue(aMap.getGridLabelPosition().toString());
        drawDegreeSymbol.setValue(String.valueOf(aMap.isDrawDegreeSymbol()));
        drawBackColor.setValue(String.valueOf(aMap.isDrawBackColor()));

        layoutMap.setAttributeNode(elementType);
        layoutMap.setAttributeNode(left);
        layoutMap.setAttributeNode(top);
        layoutMap.setAttributeNode(width);
        layoutMap.setAttributeNode(height);
        layoutMap.setAttributeNode(DrawMapNeatLine);
        layoutMap.setAttributeNode(MapNeatLineColor);
        layoutMap.setAttributeNode(MapNeatLineSize);
        layoutMap.setAttributeNode(GridLineColor);
        layoutMap.setAttributeNode(GridLineSize);
        layoutMap.setAttributeNode(GridLineStyle);
        layoutMap.setAttributeNode(DrawGridLine);
        layoutMap.setAttributeNode(DrawGridLabel);
        layoutMap.setAttributeNode(GridFontName);
        layoutMap.setAttributeNode(GridFontSize);
        layoutMap.setAttributeNode(GridXDelt);
        layoutMap.setAttributeNode(GridYDelt);
        layoutMap.setAttributeNode(GridXOrigin);
        layoutMap.setAttributeNode(GridYOrigin);
        layoutMap.setAttributeNode(gridLabelPosition);
        layoutMap.setAttributeNode(drawDegreeSymbol);
        layoutMap.setAttributeNode(drawBackColor);

        parent.appendChild(layoutMap);
    }

    private void addLayoutLegendElement(Document doc, Element parent, LayoutLegend aLegend) {
        Element Legend = doc.createElement("LayoutLegend");
        Attr elementType = doc.createAttribute("ElementType");
        Attr layoutMapIndex = doc.createAttribute("LayoutMapIndex");
        Attr legendLayer = doc.createAttribute("LegendLayer");
        Attr LegendStyle = doc.createAttribute("LegendStyle");
        Attr layerUpdateType = doc.createAttribute("LayerUpdateType");
        Attr BackColor = doc.createAttribute("BackColor");
        Attr DrawNeatLine = doc.createAttribute("DrawNeatLine");
        Attr NeatLineColor = doc.createAttribute("NeatLineColor");
        Attr NeatLineSize = doc.createAttribute("NeatLineSize");
        Attr drawChartBreaks = doc.createAttribute("DrawChartBreaks");
        Attr Left = doc.createAttribute("Left");
        Attr Top = doc.createAttribute("Top");
        Attr Width = doc.createAttribute("Width");
        Attr Height = doc.createAttribute("Height");
        Attr FontName = doc.createAttribute("FontName");
        Attr FontSize = doc.createAttribute("FontSize");
        Attr colNum = doc.createAttribute("ColumnNumber");
        Attr drawBackColor = doc.createAttribute("DrawBackColor");
        Attr forceDrawOutline = doc.createAttribute("ForceDrawOutline");

        elementType.setValue(aLegend.getElementType().toString());
        layoutMapIndex.setValue(String.valueOf(getLayoutMapIndex(aLegend.getLayoutMap())));
        legendLayer.setValue(aLegend.getLayerName());
        LegendStyle.setValue(aLegend.getLegendStyle().toString());
        layerUpdateType.setValue(aLegend.getLayerUpdateType().toString());
        BackColor.setValue(ColorUtil.toHexEncoding(aLegend.getBackColor()));
        DrawNeatLine.setValue(String.valueOf(aLegend.isDrawNeatLine()));
        NeatLineColor.setValue(ColorUtil.toHexEncoding(aLegend.getNeatLineColor()));
        NeatLineSize.setValue(String.valueOf(aLegend.getNeatLineSize()));
        drawChartBreaks.setValue(String.valueOf(aLegend.isDrawChartBreaks()));
        Left.setValue(String.valueOf(aLegend.getLeft()));
        Top.setValue(String.valueOf(aLegend.getTop()));
        Width.setValue(String.valueOf(aLegend.getWidth()));
        Height.setValue(String.valueOf(aLegend.getHeight()));
        FontName.setValue(aLegend.getFont().getFontName());
        FontSize.setValue(String.valueOf(aLegend.getFont().getSize()));
        colNum.setValue(String.valueOf(aLegend.getColumnNumber()));
        drawBackColor.setValue(String.valueOf(aLegend.isDrawBackColor()));
        forceDrawOutline.setValue(String.valueOf(aLegend.isForceDrawOutline()));

        Legend.setAttributeNode(elementType);
        Legend.setAttributeNode(layoutMapIndex);
        Legend.setAttributeNode(legendLayer);
        Legend.setAttributeNode(LegendStyle);
        Legend.setAttributeNode(layerUpdateType);
        Legend.setAttributeNode(BackColor);
        Legend.setAttributeNode(DrawNeatLine);
        Legend.setAttributeNode(NeatLineColor);
        Legend.setAttributeNode(NeatLineSize);
        Legend.setAttributeNode(drawChartBreaks);
        Legend.setAttributeNode(Left);
        Legend.setAttributeNode(Top);
        Legend.setAttributeNode(Width);
        Legend.setAttributeNode(Height);
        Legend.setAttributeNode(FontName);
        Legend.setAttributeNode(FontSize);
        Legend.setAttributeNode(colNum);
        Legend.setAttributeNode(drawBackColor);
        Legend.setAttributeNode(forceDrawOutline);

        parent.appendChild(Legend);
    }

    private void addLayoutScaleBarElement(Document doc, Element parent, LayoutScaleBar aScaleBar) {
        Element scaleBar = doc.createElement("LayoutScaleBar");
        Attr elementType = doc.createAttribute("ElementType");
        Attr layoutMapIndex = doc.createAttribute("LayoutMapIndex");
        Attr scaleBarType = doc.createAttribute("ScaleBarType");
        Attr BackColor = doc.createAttribute("BackColor");
        Attr foreColor = doc.createAttribute("ForeColor");
        Attr DrawNeatLine = doc.createAttribute("DrawNeatLine");
        Attr NeatLineColor = doc.createAttribute("NeatLineColor");
        Attr NeatLineSize = doc.createAttribute("NeatLineSize");
        Attr Left = doc.createAttribute("Left");
        Attr Top = doc.createAttribute("Top");
        Attr Width = doc.createAttribute("Width");
        Attr Height = doc.createAttribute("Height");
        Attr FontName = doc.createAttribute("FontName");
        Attr FontSize = doc.createAttribute("FontSize");
        Attr drawScaleText = doc.createAttribute("DrawScaleText");
        Attr drawBackColor = doc.createAttribute("DrawBackColor");

        elementType.setValue(aScaleBar.getElementType().toString());
        layoutMapIndex.setValue(String.valueOf(getLayoutMapIndex(aScaleBar.getLayoutMap())));
        scaleBarType.setValue(aScaleBar.getScaleBarType().toString());
        BackColor.setValue(ColorUtil.toHexEncoding(aScaleBar.getBackColor()));
        foreColor.setValue(ColorUtil.toHexEncoding(aScaleBar.getForeColor()));
        DrawNeatLine.setValue(String.valueOf(aScaleBar.isDrawNeatLine()));
        NeatLineColor.setValue(ColorUtil.toHexEncoding(aScaleBar.getNeatLineColor()));
        NeatLineSize.setValue(String.valueOf(aScaleBar.getNeatLineSize()));
        Left.setValue(String.valueOf(aScaleBar.getLeft()));
        Top.setValue(String.valueOf(aScaleBar.getTop()));
        Width.setValue(String.valueOf(aScaleBar.getWidth()));
        Height.setValue(String.valueOf(aScaleBar.getHeight()));
        FontName.setValue(aScaleBar.getFont().getFontName());
        FontSize.setValue(String.valueOf(aScaleBar.getFont().getSize()));
        drawScaleText.setValue(String.valueOf(aScaleBar.isDrawScaleText()));
        drawBackColor.setValue(String.valueOf(aScaleBar.isDrawBackColor()));

        scaleBar.setAttributeNode(elementType);
        scaleBar.setAttributeNode(layoutMapIndex);
        scaleBar.setAttributeNode(scaleBarType);
        scaleBar.setAttributeNode(BackColor);
        scaleBar.setAttributeNode(foreColor);
        scaleBar.setAttributeNode(DrawNeatLine);
        scaleBar.setAttributeNode(NeatLineColor);
        scaleBar.setAttributeNode(NeatLineSize);
        scaleBar.setAttributeNode(Left);
        scaleBar.setAttributeNode(Top);
        scaleBar.setAttributeNode(Width);
        scaleBar.setAttributeNode(Height);
        scaleBar.setAttributeNode(FontName);
        scaleBar.setAttributeNode(FontSize);
        scaleBar.setAttributeNode(drawScaleText);
        scaleBar.setAttributeNode(drawBackColor);

        parent.appendChild(scaleBar);
    }

    private void addLayoutNorthArrowElement(Document doc, Element parent, LayoutNorthArrow aNorthArrow) {
        Element northArrow = doc.createElement("LayoutNorthArrow");
        Attr elementType = doc.createAttribute("ElementType");
        Attr layoutMapIndex = doc.createAttribute("LayoutMapIndex");
        Attr BackColor = doc.createAttribute("BackColor");
        Attr foreColor = doc.createAttribute("ForeColor");
        Attr DrawNeatLine = doc.createAttribute("DrawNeatLine");
        Attr NeatLineColor = doc.createAttribute("NeatLineColor");
        Attr NeatLineSize = doc.createAttribute("NeatLineSize");
        Attr Left = doc.createAttribute("Left");
        Attr Top = doc.createAttribute("Top");
        Attr Width = doc.createAttribute("Width");
        Attr Height = doc.createAttribute("Height");
        Attr angle = doc.createAttribute("Angle");
        Attr drawBackColor = doc.createAttribute("DrawBackColor");

        elementType.setValue(aNorthArrow.getElementType().toString());
        layoutMapIndex.setValue(String.valueOf(getLayoutMapIndex(aNorthArrow.getLayoutMap())));
        BackColor.setValue(ColorUtil.toHexEncoding(aNorthArrow.getBackColor()));
        foreColor.setValue(ColorUtil.toHexEncoding(aNorthArrow.getForeColor()));
        DrawNeatLine.setValue(String.valueOf(aNorthArrow.isDrawNeatLine()));
        NeatLineColor.setValue(ColorUtil.toHexEncoding(aNorthArrow.getNeatLineColor()));
        NeatLineSize.setValue(String.valueOf(aNorthArrow.getNeatLineSize()));
        Left.setValue(String.valueOf(aNorthArrow.getLeft()));
        Top.setValue(String.valueOf(aNorthArrow.getTop()));
        Width.setValue(String.valueOf(aNorthArrow.getWidth()));
        Height.setValue(String.valueOf(aNorthArrow.getHeight()));
        angle.setValue(String.valueOf(aNorthArrow.getAngle()));
        drawBackColor.setValue(String.valueOf(aNorthArrow.isDrawBackColor()));

        northArrow.setAttributeNode(elementType);
        northArrow.setAttributeNode(layoutMapIndex);
        northArrow.setAttributeNode(BackColor);
        northArrow.setAttributeNode(foreColor);
        northArrow.setAttributeNode(DrawNeatLine);
        northArrow.setAttributeNode(NeatLineColor);
        northArrow.setAttributeNode(NeatLineSize);
        northArrow.setAttributeNode(Left);
        northArrow.setAttributeNode(Top);
        northArrow.setAttributeNode(Width);
        northArrow.setAttributeNode(Height);
        northArrow.setAttributeNode(angle);
        northArrow.setAttributeNode(drawBackColor);

        parent.appendChild(northArrow);
    }

    private void addLayoutGraphicElement(Document doc, Element parent, LayoutGraphic aLayoutGraphic) {
        Element layoutGraphic = doc.createElement("LayoutGraphic");
        Attr elementType = doc.createAttribute("ElementType");
        Attr isTitle = doc.createAttribute("IsTitle");
        Attr left = doc.createAttribute("Left");
        Attr top = doc.createAttribute("Top");
        Attr width = doc.createAttribute("Width");
        Attr height = doc.createAttribute("Height");

        elementType.setValue(aLayoutGraphic.getElementType().toString());
        isTitle.setValue(String.valueOf(aLayoutGraphic.isTitle()));
        left.setValue(String.valueOf(aLayoutGraphic.getLeft()));
        top.setValue(String.valueOf(aLayoutGraphic.getTop()));
        width.setValue(String.valueOf(aLayoutGraphic.getWidth()));
        height.setValue(String.valueOf(aLayoutGraphic.getHeight()));

        layoutGraphic.setAttributeNode(elementType);
        layoutGraphic.setAttributeNode(isTitle);
        layoutGraphic.setAttributeNode(left);
        layoutGraphic.setAttributeNode(top);
        layoutGraphic.setAttributeNode(width);
        layoutGraphic.setAttributeNode(height);

        //Add graphic
        Graphic aGraphic = aLayoutGraphic.getGraphic();
        aGraphic.exportToXML(doc, layoutGraphic);

        //Append in parent
        parent.appendChild(layoutGraphic);
    }

    /**
     * Load project file
     *
     * @param aFile The project file
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public void loadProjectFile(String aFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(aFile);

        Element root = doc.getDocumentElement();

        Properties property = System.getProperties();
        String path = System.getProperty("user.dir");
        property.setProperty("user.dir", new File(aFile).getAbsolutePath());
        String pPath = new File(aFile).getParent();

        //Load map frames content
        List<MapFrame> mfs = new ArrayList<>();
        Element mapFrames = (Element) root.getElementsByTagName("MapFrames").item(0);
        if (mapFrames == null) {
            MapFrame mf = new MapFrame();
            mf.importProjectXML(pPath, root);
            mf.setActive(true);
            mfs.add(mf);
        } else {
            NodeList mfNodes = mapFrames.getElementsByTagName("MapFrame");
            for (int i = 0; i < mfNodes.getLength(); i++) {
                Node mapFrame = mfNodes.item(i);
                MapFrame mf = new MapFrame();
                mf.importProjectXML(pPath, (Element) mapFrame);
                mfs.add(mf);
            }
        }

        this.setMapFrames(mfs);
        //Load MapLayout content
        this.importProjectXML(root);

        property.setProperty("user.dir", path);
    }

    /**
     * Import project XML element
     *
     * @param parent Parent element
     */
    public void importProjectXML(Element parent) {
        loadLayout(parent);
    }

    private void loadLayout(Element parent) {
        Node layout = parent.getElementsByTagName("Layout").item(0);
        try {
            _pageBackColor = ColorUtil.parseToColor(layout.getAttributes().getNamedItem("BackColor").getNodeValue());
            _pageForeColor = ColorUtil.parseToColor(layout.getAttributes().getNamedItem("ForeColor").getNodeValue());
            this.setAntiAlias(Boolean.parseBoolean(layout.getAttributes().getNamedItem("SmoothingMode").getNodeValue()));
            _paperSize.setName(layout.getAttributes().getNamedItem("PaperSizeName").getNodeValue());
            _paperSize.setWidth(Integer.parseInt(layout.getAttributes().getNamedItem("PaperSizeWidth").getNodeValue()));
            _paperSize.setHeight(Integer.parseInt(layout.getAttributes().getNamedItem("PaperSizeHeight").getNodeValue()));
            _isLandscape = Boolean.parseBoolean(layout.getAttributes().getNamedItem("Landscape").getNodeValue());

            updatePageSet();

            loadLayoutElements((Element) layout);
        } catch (Exception e) {
        }
    }

    private void loadLayoutElements(Element parent) {
        _layoutElements.clear();
        _selectedElements.clear();

        Node layoutElements = parent.getElementsByTagName("LayoutElements").item(0);
        //Load layout maps
        NodeList layoutNodes = ((Element) layoutElements).getElementsByTagName("LayoutMap");
        for (int i = 0; i < layoutNodes.getLength(); i++) {
            Node elementNode = layoutNodes.item(i);
            MapFrame aMF;
            if (_mapFrames.size() > i) {
                aMF = _mapFrames.get(i);
            } else {
                aMF = new MapFrame();
            }

            LayoutMap aLM = new LayoutMap(aMF, this.tileLoadListener);
            loadLayoutMapElement(elementNode, aLM);
            addElement(aLM);
        }

        //Load other elements
        for (int i = 0; i < layoutElements.getChildNodes().getLength(); i++) {
            Node elementNode = layoutElements.getChildNodes().item(i);
            if (elementNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            ElementType aType = ElementType.valueOf(elementNode.getAttributes().getNamedItem("ElementType").getNodeValue());
            switch (aType) {
                case LayoutIllustration:
                    break;
                case LayoutLegend:
                    LayoutLegend aLL = loadLayoutLegendElement(elementNode);
                    addElement(aLL);
                    break;
                case LayoutGraphic:
                    LayoutGraphic aLG = loadLayoutGraphicElement(elementNode);
                    if (aLG.getGraphic().getShape().getShapeType() == ShapeTypes.WindArraw) {
                        ((WindArrow) aLG.getGraphic().getShape()).angle = 270;
                    }
                    addElement(aLG);
                    break;
                case LayoutScaleBar:
                    LayoutScaleBar aLSB = loadLayoutScaleBarElement(elementNode);
                    if (aLSB != null) {
                        addElement(aLSB);
                    }
                    break;
                case LayoutNorthArraw:
                    LayoutNorthArrow aLNA = loadLayoutNorthArrowElement(elementNode);
                    if (aLNA != null) {
                        addElement(aLNA);
                    }
                    break;
            }
        }
    }

    private void loadLayoutMapElement(Node layoutMap, LayoutMap aLM) {
        try {
            aLM.setLeft(Integer.parseInt(layoutMap.getAttributes().getNamedItem("Left").getNodeValue()));
            aLM.setTop(Integer.parseInt(layoutMap.getAttributes().getNamedItem("Top").getNodeValue()));
            aLM.setWidth(Integer.parseInt(layoutMap.getAttributes().getNamedItem("Width").getNodeValue()));
            aLM.setHeight(Integer.parseInt(layoutMap.getAttributes().getNamedItem("Height").getNodeValue()));
            aLM.setDrawNeatLine(Boolean.parseBoolean(layoutMap.getAttributes().getNamedItem("DrawNeatLine").getNodeValue()));
            aLM.setNeatLineColor(ColorUtil.parseToColor(layoutMap.getAttributes().getNamedItem("NeatLineColor").getNodeValue()));
            aLM.setNeatLineSize(Float.parseFloat(layoutMap.getAttributes().getNamedItem("NeatLineSize").getNodeValue()));
            aLM.setGridLineColor(ColorUtil.parseToColor(layoutMap.getAttributes().getNamedItem("GridLineColor").getNodeValue()));
            aLM.setGridLineSize(Float.parseFloat(layoutMap.getAttributes().getNamedItem("GridLineSize").getNodeValue()));
            aLM.setGridLineStyle(LineStyles.valueOf(layoutMap.getAttributes().getNamedItem("GridLineStyle").getNodeValue()));
            aLM.setDrawGridLine(Boolean.parseBoolean(layoutMap.getAttributes().getNamedItem("DrawGridLine").getNodeValue()));
            aLM.setDrawGridLabel(Boolean.parseBoolean(layoutMap.getAttributes().getNamedItem("DrawGridLabel").getNodeValue()));
            String fontName = layoutMap.getAttributes().getNamedItem("GridFontName").getNodeValue();
            float fontSize = Float.parseFloat(layoutMap.getAttributes().getNamedItem("GridFontSize").getNodeValue());
            aLM.setGridFont(new Font(fontName, Font.PLAIN, (int) fontSize));
            aLM.setGridXDelt(Double.parseDouble(layoutMap.getAttributes().getNamedItem("GridXDelt").getNodeValue()));
            aLM.setGridYDelt(Double.parseDouble(layoutMap.getAttributes().getNamedItem("GridYDelt").getNodeValue()));
            aLM.setGridXOrigin(Float.parseFloat(layoutMap.getAttributes().getNamedItem("GridXOrigin").getNodeValue()));
            aLM.setGridYOrigin(Float.parseFloat(layoutMap.getAttributes().getNamedItem("GridYOrigin").getNodeValue()));
            aLM.setGridLabelPosition(GridLabelPosition.valueOf(layoutMap.getAttributes().getNamedItem("GridLabelPosition").getNodeValue()));
            aLM.setDrawDegreeSymbol(Boolean.parseBoolean(layoutMap.getAttributes().getNamedItem("DrawDegreeSymbol").getNodeValue()));
            aLM.setDrawBackColor(Boolean.parseBoolean(layoutMap.getAttributes().getNamedItem("DrawBackColor").getNodeValue()));
        } catch (Exception e) {
        }
    }

    private LayoutLegend loadLayoutLegendElement(Node layoutLegend) {
        LayoutLegend aLL = null;
        try {
            int layoutMapIdx = Integer.parseInt(layoutLegend.getAttributes().getNamedItem("LayoutMapIndex").getNodeValue());
            String legendLayerName = layoutLegend.getAttributes().getNamedItem("LegendLayer").getNodeValue();
            LayoutMap aLM = this.getLayoutMaps().get(layoutMapIdx);
            MapLayer legendLayer = aLM.getMapFrame().getMapView().getLayer(legendLayerName);
            aLL = new LayoutLegend(this, aLM);
            aLL.setLegendLayer(legendLayer);
        } catch (Exception e) {
            aLL = new LayoutLegend(this, this.getLayoutMaps().get(0));
            aLL.setLegendLayer(aLL.getLayoutMap().getMapFrame().getMapView().getLayers().get(0));
        }

        try {
            aLL.setLegendStyle(LegendStyles.valueOf(layoutLegend.getAttributes().getNamedItem("LegendStyle").getNodeValue()));
            aLL.setBackColor(ColorUtil.parseToColor(layoutLegend.getAttributes().getNamedItem("BackColor").getNodeValue()));
            aLL.setDrawNeatLine(Boolean.parseBoolean(layoutLegend.getAttributes().getNamedItem("DrawNeatLine").getNodeValue()));
            aLL.setNeatLineColor(ColorUtil.parseToColor(layoutLegend.getAttributes().getNamedItem("NeatLineColor").getNodeValue()));
            aLL.setNeatLineSize(Float.parseFloat(layoutLegend.getAttributes().getNamedItem("NeatLineSize").getNodeValue()));
            aLL.setLeft(Integer.parseInt(layoutLegend.getAttributes().getNamedItem("Left").getNodeValue()));
            aLL.setTop(Integer.parseInt(layoutLegend.getAttributes().getNamedItem("Top").getNodeValue()));
            aLL.setWidth(Integer.parseInt(layoutLegend.getAttributes().getNamedItem("Width").getNodeValue()));
            aLL.setHeight(Integer.parseInt(layoutLegend.getAttributes().getNamedItem("Height").getNodeValue()));
            String fontName = layoutLegend.getAttributes().getNamedItem("FontName").getNodeValue();
            float fontSize = Float.parseFloat(layoutLegend.getAttributes().getNamedItem("FontSize").getNodeValue());
            aLL.setFont(new Font(fontName, Font.PLAIN, (int) fontSize));
            aLL.setLayerUpdateType(LayerUpdateTypes.valueOf(layoutLegend.getAttributes().getNamedItem("LayerUpdateType").getNodeValue()));
            aLL.setColumnNumber(Integer.parseInt(layoutLegend.getAttributes().getNamedItem("ColumnNumber").getNodeValue()));
            aLL.setDrawBackColor(Boolean.parseBoolean(layoutLegend.getAttributes().getNamedItem("DrawBackColor").getNodeValue()));
            aLL.setDrawChartBreaks(Boolean.parseBoolean(layoutLegend.getAttributes().getNamedItem("DrawChartBreaks").getNodeValue()));
            aLL.setForceDrawOutline(Boolean.parseBoolean(layoutLegend.getAttributes().getNamedItem("ForceDrawOutline").getNodeValue()));
        } catch (Exception e) {
        }

        return aLL;
    }

    private LayoutScaleBar loadLayoutScaleBarElement(Node layoutScaleBar) {
        LayoutScaleBar aLSB = null;
        try {
            int layoutMapIdx = Integer.parseInt(layoutScaleBar.getAttributes().getNamedItem("LayoutMapIndex").getNodeValue());
            LayoutMap aLM = this.getLayoutMaps().get(layoutMapIdx);
            aLSB = new LayoutScaleBar(aLM);
        } catch (Exception e) {
            aLSB = new LayoutScaleBar(this.getLayoutMaps().get(0));
        }

        try {
            aLSB.setScaleBarType(ScaleBarType.valueOf(layoutScaleBar.getAttributes().getNamedItem("ScaleBarType").getNodeValue()));
            aLSB.setBackColor(ColorUtil.parseToColor(layoutScaleBar.getAttributes().getNamedItem("BackColor").getNodeValue()));
            aLSB.setForeColor(ColorUtil.parseToColor(layoutScaleBar.getAttributes().getNamedItem("ForeColor").getNodeValue()));
            aLSB.setDrawNeatLine(Boolean.parseBoolean(layoutScaleBar.getAttributes().getNamedItem("DrawNeatLine").getNodeValue()));
            aLSB.setNeatLineColor(ColorUtil.parseToColor(layoutScaleBar.getAttributes().getNamedItem("NeatLineColor").getNodeValue()));
            aLSB.setNeatLineSize(Float.parseFloat(layoutScaleBar.getAttributes().getNamedItem("NeatLineSize").getNodeValue()));
            aLSB.setLeft(Integer.parseInt(layoutScaleBar.getAttributes().getNamedItem("Left").getNodeValue()));
            aLSB.setTop(Integer.parseInt(layoutScaleBar.getAttributes().getNamedItem("Top").getNodeValue()));
            aLSB.setWidth(Integer.parseInt(layoutScaleBar.getAttributes().getNamedItem("Width").getNodeValue()));
            aLSB.setHeight(Integer.parseInt(layoutScaleBar.getAttributes().getNamedItem("Height").getNodeValue()));
            String fontName = layoutScaleBar.getAttributes().getNamedItem("FontName").getNodeValue();
            float fontSize = Float.parseFloat(layoutScaleBar.getAttributes().getNamedItem("FontSize").getNodeValue());
            aLSB.setFont(new Font(fontName, Font.PLAIN, (int) fontSize));
            aLSB.setDrawScaleText(Boolean.parseBoolean(layoutScaleBar.getAttributes().getNamedItem("DrawScaleText").getNodeValue()));
            aLSB.setDrawBackColor(Boolean.parseBoolean(layoutScaleBar.getAttributes().getNamedItem("DrawBackColor").getNodeValue()));
        } catch (Exception e) {
        }

        return aLSB;
    }

    private LayoutNorthArrow loadLayoutNorthArrowElement(Node layoutNorthArrow) {
        LayoutNorthArrow aLNA = null;
        try {
            int layoutMapIdx = Integer.parseInt(layoutNorthArrow.getAttributes().getNamedItem("LayoutMapIndex").getNodeValue());
            LayoutMap aLM = this.getLayoutMaps().get(layoutMapIdx);
            aLNA = new LayoutNorthArrow(aLM);
        } catch (Exception e) {
            aLNA = new LayoutNorthArrow(this.getLayoutMaps().get(0));
        }

        try {
            aLNA.setBackColor(ColorUtil.parseToColor(layoutNorthArrow.getAttributes().getNamedItem("BackColor").getNodeValue()));
            aLNA.setForeColor(ColorUtil.parseToColor(layoutNorthArrow.getAttributes().getNamedItem("ForeColor").getNodeValue()));
            aLNA.setDrawNeatLine(Boolean.parseBoolean(layoutNorthArrow.getAttributes().getNamedItem("DrawNeatLine").getNodeValue()));
            aLNA.setNeatLineColor(ColorUtil.parseToColor(layoutNorthArrow.getAttributes().getNamedItem("NeatLineColor").getNodeValue()));
            aLNA.setNeatLineSize(Float.parseFloat(layoutNorthArrow.getAttributes().getNamedItem("NeatLineSize").getNodeValue()));
            aLNA.setLeft(Integer.parseInt(layoutNorthArrow.getAttributes().getNamedItem("Left").getNodeValue()));
            aLNA.setTop(Integer.parseInt(layoutNorthArrow.getAttributes().getNamedItem("Top").getNodeValue()));
            aLNA.setWidth(Integer.parseInt(layoutNorthArrow.getAttributes().getNamedItem("Width").getNodeValue()));
            aLNA.setHeight(Integer.parseInt(layoutNorthArrow.getAttributes().getNamedItem("Height").getNodeValue()));
            aLNA.setAngle(Float.parseFloat(layoutNorthArrow.getAttributes().getNamedItem("Angle").getNodeValue()));
            aLNA.setDrawBackColor(Boolean.parseBoolean(layoutNorthArrow.getAttributes().getNamedItem("DrawBackColor").getNodeValue()));
        } catch (Exception e) {
        }

        return aLNA;
    }

    private LayoutGraphic loadLayoutGraphicElement(Node layoutGraphic) {
        Graphic aGraphic = new Graphic();
        Node graphicNode = ((Element) layoutGraphic).getElementsByTagName("Graphic").item(0);
        aGraphic.importFromXML((Element) graphicNode);

        LayoutGraphic aLG;
        if (aGraphic.getShape().getShapeType() == ShapeTypes.WindArraw) {
            aLG = new LayoutGraphic(aGraphic, this, this.getActiveLayoutMap());
        } else {
            aLG = new LayoutGraphic(aGraphic, this);
        }

        aLG.setIsTitle(Boolean.parseBoolean(layoutGraphic.getAttributes().getNamedItem("IsTitle").getNodeValue()));

        return aLG;
    }
    // </editor-fold>
    // </editor-fold>
}
