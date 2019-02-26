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

import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.event.ISelectedCellChangedListener;
import org.meteoinfo.global.event.SelectedCellChangedEvent;
import org.meteoinfo.global.PointF;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Yaqiang Wang
 */
public class SymbolControl extends JPanel {
    // <editor-fold desc="Variables">

    private EventListenerList _listeners = new EventListenerList();
    private ShapeTypes _shapeType;
    private MarkerType _markerType;
    private Dimension _cellSize;
    private int _symbolNumber;
    private int _colNumber;
    private int _rowNumber;
    private int _selectedCell;
    private List<Image> _imageList;
    private JScrollBar _vScrollBar;
    private Color _selColor = new Color(210, 255, 255);
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public SymbolControl() {
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
                onMouseClicked(e);
            }
        });

        _shapeType = ShapeTypes.Point;
        _markerType = MarkerType.Simple;
        _cellSize = new Dimension(25, 25);
        _symbolNumber = PointStyle.values().length;
        _colNumber = 10;
        _rowNumber = 26;
        _selectedCell = -1;
        _imageList = new ArrayList<>();
    }

    private void initComponents() {
        this.setPreferredSize(new Dimension(200, 100));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.white);

        _vScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        _vScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                onScrollValueChanged(e);
            }
        });
        this.add(_vScrollBar, BorderLayout.EAST);
        //this._vScrollBar.setSize(this._vScrollBar.getWidth(), this.getHeight());
        this._vScrollBar.setSize(20, this.getHeight());
        this._vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);
    }
    // </editor-fold>

    // <editor-fold desc="Events">
    public void addSelectedCellChangedListener(ISelectedCellChangedListener listener) {
        this._listeners.add(ISelectedCellChangedListener.class, listener);
    }

    public void removeSelectedCellChangedListener(ISelectedCellChangedListener listener) {
        this._listeners.remove(ISelectedCellChangedListener.class, listener);
    }

    public void fireSelectedCellChangedEvent() {
        fireSelectedCellChangedEvent(new SelectedCellChangedEvent(this));
    }

    private void fireSelectedCellChangedEvent(SelectedCellChangedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ISelectedCellChangedListener.class) {
                ((ISelectedCellChangedListener) listeners[i + 1]).selectedCellChangedEvent(event);
            }
        }
    }

    public void onScrollValueChanged(AdjustmentEvent e) {
        _vScrollBar.setValue(e.getValue());
        this.repaint();
    }

    public void onComponentResized(ComponentEvent e) {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            this.updateSize();
            this._vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);
            this._vScrollBar.setSize(this._vScrollBar.getWidth(), this.getHeight());
        }
        this.repaint();
    }

    public void onMouseClicked(MouseEvent e) {
        int col = e.getX() / _cellSize.width;
        int row = (e.getY() + _vScrollBar.getValue()) / _cellSize.height;
        int sel = row * _colNumber + col;
        if (sel < _symbolNumber) {
            _selectedCell = row * _colNumber + col;
            //((frmPointSymbolSet)this.ParentForm).PointBreak.CharIndex = _selectedCell;
            this.repaint();

            this.fireSelectedCellChangedEvent();
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get shape type
     *
     * @return The shape type
     */
    public ShapeTypes getShapeType() {
        return _shapeType;
    }

    /**
     * Set shape type
     *
     * @param st The shape type
     */
    public void setShapeType(ShapeTypes st) {
        _shapeType = st;
        switch (_shapeType) {
            case Point:
                setCellSize(new Dimension(25, 25));
                if (_markerType == MarkerType.Simple) {
                    setSymbolNumber(PointStyle.values().length);
                } else {
                    setSymbolNumber(256);
                }
                break;
            case Polyline:
                setCellSize(new Dimension(50, 40));
                setSymbolNumber(LineStyles.values().length);
                break;
            case Polygon:
                setCellSize(new Dimension(30, 30));
                setSymbolNumber(HatchStyle.values().length);
                break;
        }
    }

    /**
     * Get marker type
     *
     * @return Marker type
     */
    public MarkerType getMarkerType() {
        return _markerType;
    }

    /**
     * Set marker type
     *
     * @param mt
     */
    public void setMarkerType(MarkerType mt) {
        _markerType = mt;
        if (_selectedCell < 0 || _selectedCell >= _symbolNumber) {
            _selectedCell = 0;
        }
        this.repaint();
    }

    /**
     * Get cell size
     *
     * @return Cell size
     */
    public Dimension getCellSize() {
        return _cellSize;
    }

    /**
     * Set cell size
     *
     * @param size Cell size
     */
    public void setCellSize(Dimension size) {
        _cellSize = size;
        this.updateSize();
    }

    /**
     * Get selected cell index
     *
     * @return Selected cell index
     */
    public int getSelectedCell() {
        return _selectedCell;
    }

    /**
     * Set selected cell index
     *
     * @param idx Selected cell index
     */
    public void setSelectedCell(int idx) {
        _selectedCell = idx;
    }

    /**
     * Get symbol number
     *
     * @return The symbol number
     */
    public int getSymbolNumber() {
        return _symbolNumber;
    }

    /**
     * Set symbol number
     *
     * @param sn The symbol number
     */
    public void setSymbolNumber(int sn) {
        _symbolNumber = sn;
        _rowNumber = (int) Math.ceil((float) _symbolNumber / _colNumber);
    }

    /**
     * Get column number
     *
     * @return The column number
     */
    public int getColumnNumber() {
        return _colNumber;
    }

    /**
     * Set column number
     *
     * @param n The column number
     */
    public void setColumnNumber(int n) {
        _colNumber = n;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Set image list
     *
     * @param imageList The image list
     */
    public void setIamgeList(List<Image> imageList) {
        _imageList = imageList;
        setSymbolNumber(imageList.size());
    }

    private void updateSize() {
        if (this._vScrollBar.isVisible()) {
            this._colNumber = (this.getWidth() - this._vScrollBar.getWidth()) / this._cellSize.width;
        } else {
            this._colNumber = this.getWidth() / this._cellSize.width;
        }
        if (_colNumber == 0) {
            _colNumber = 1;
        }
        this._rowNumber = (int) Math.ceil((float) _symbolNumber / _colNumber);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintGraphics(g2);
        //g2.dispose();
    }

    private void paintGraphics(Graphics2D g) {
        int TotalHeight = calcTotalDrawHeight();
        Rectangle rect;
        if (TotalHeight > this.getHeight()) {
            _vScrollBar.setMinimum(0);
            _vScrollBar.setUnitIncrement(_cellSize.height);
            _vScrollBar.setBlockIncrement(this.getHeight());
            _vScrollBar.setMaximum(TotalHeight);

            if (_vScrollBar.isVisible() == false) {
                _vScrollBar.setValue(0);
                _vScrollBar.setVisible(true);
            }
            rect = new Rectangle(0, -_vScrollBar.getValue(), this.getWidth() - _vScrollBar.getWidth(), TotalHeight);
        } else {
            _vScrollBar.setVisible(false);
            rect = new Rectangle(0, 0, this.getWidth(), this.getHeight());
        }

        this.updateSize();
        drawCells(g, rect.y);
    }

    private void drawCells(Graphics2D g, int sHeight) {
        int hideRows = 0;
        switch (_shapeType) {
            case Point:
                switch (_markerType) {
                    case Character:
                        Font smallFont = new Font(this.getFont().getFamily(), Font.PLAIN, (int) (_cellSize.width * 0.8F));
                        for (int i = 0; i < _symbolNumber; i++) {
                            int row = i / _colNumber;
                            if (row > hideRows) {
                                sHeight += _cellSize.height;
                                hideRows = row;
                            }
                            if (sHeight + _cellSize.height < 0) {
                                continue;
                            }

                            int col = i % _colNumber;
                            if (i == _selectedCell) {
                                g.setColor(_selColor);
                                g.fill(new Rectangle(col * _cellSize.width, sHeight, _cellSize.width, _cellSize.height));
                            }

                            String text = String.valueOf((char) i);
                            g.setColor(Color.black);
                            g.setFont(smallFont);
                            g.drawString(text, col * _cellSize.width, sHeight + _cellSize.height);
                        }
                        break;
                    case Simple:
                        PointBreak aPB = new PointBreak();
                        aPB.setColor(Color.red);
                        aPB.setDrawOutline(true);
                        aPB.setSize(_cellSize.width * 0.8f);
                        for (int i = 0; i < _symbolNumber; i++) {
//                            if (i == PointStyle.values().length) {
//                                break;
//                            }

                            int row = i / _colNumber;
                            if (row > hideRows) {
                                sHeight += _cellSize.height;
                                hideRows = row;
                            }
                            if (sHeight + _cellSize.height < 0) {
                                continue;
                            }

                            int col = i % _colNumber;
                            if (i == _selectedCell) {
                                g.setColor(_selColor);
                                g.fill(new Rectangle(col * _cellSize.width, sHeight, _cellSize.width, _cellSize.height));
                            }

                            PointF sP = new PointF(col * _cellSize.width + _cellSize.width / 2,
                                    sHeight + _cellSize.height / 2);
                            aPB.setStyle(PointStyle.values()[i]);
                            Draw.drawPoint(sP, aPB, g);
                        }
                        break;
                    case Image:
                        float size = _cellSize.width * 0.8f;
                        for (int i = 0; i < _symbolNumber; i++) {
                            int row = i / _colNumber;
                            if (row > hideRows) {
                                sHeight += _cellSize.height;
                                hideRows = row;
                            }
                            if (sHeight + _cellSize.height < 0) {
                                continue;
                            }

                            int col = i % _colNumber;
                            if (i == _selectedCell) {
                                g.setColor(_selColor);
                                g.fill(new Rectangle(col * _cellSize.width, sHeight, _cellSize.width, _cellSize.height));
                            }

                            //((Bitmap)_imageList[i]).MakeTransparent(Color.White);
                            if (_imageList.size() > i) {
                                g.drawImage(_imageList.get(i), col * _cellSize.width, sHeight, (int) size, (int) size, null);
                            }
                        }
                        break;
                }
                break;
            case Polyline:
                PolylineBreak aPLB = new PolylineBreak();
                aPLB.setWidth(2);
                aPLB.setColor(Color.black);
                for (int i = 0; i < _symbolNumber; i++) {
                    int row = i / _colNumber;
                    if (row > hideRows) {
                        sHeight += _cellSize.height;
                        hideRows = row;
                    }
                    if (sHeight + _cellSize.height < 0) {
                        continue;
                    }

                    int col = i % _colNumber;
                    Rectangle rect = new Rectangle(col * _cellSize.width, sHeight + _cellSize.height / 4,
                            _cellSize.width, _cellSize.height / 2);
                    if (i == _selectedCell) {
                        g.setColor(_selColor);
                        g.fill(rect);
                    }

                    aPLB.setStyle(LineStyles.values()[i]);
                    Draw.drawPolylineSymbol(new PointF(rect.x + rect.width / 2, rect.y + rect.height / 2),
                            rect.width * 0.8f, rect.height * 0.8f, aPLB, g);
                }
                break;
            case Polygon:
                PolygonBreak aPGB = new PolygonBreak();
                aPGB.setColor(Color.red);
                aPGB.setOutlineColor(Color.black);
                for (int i = 0; i < _symbolNumber; i++) {
                    int row = i / _colNumber;
                    if (row > hideRows) {
                        sHeight += _cellSize.height;
                        hideRows = row;
                    }
                    if (sHeight + _cellSize.height < 0) {
                        continue;
                    }

                    int col = i % _colNumber;
                    Rectangle rect = new Rectangle(col * _cellSize.width, sHeight, _cellSize.width, _cellSize.height);
                    if (i == _selectedCell) {
                        g.setColor(_selColor);
                        g.fill(rect);
                    }

                    aPGB.setStyle(HatchStyle.values()[i]);
                    Draw.drawPolygonSymbol(new PointF(rect.x + rect.width / 2, rect.y + rect.height / 2),
                            rect.width * 0.8f, rect.height * 0.8f, aPGB, g);
                }
                break;
        }
    }

    private int calcTotalDrawHeight() {
        return _cellSize.height * _rowNumber;
    }
    // </editor-fold>
}
