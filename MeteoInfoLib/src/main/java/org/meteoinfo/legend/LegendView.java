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
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointF;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

 /**
  *
  * @author Yaqiang Wang
  */
public class LegendView extends JPanel {
    // <editor-fold desc="Variables">

    private LegendScheme _legendScheme = null;
    private int _breakHeight;
    private int _symbolWidth;
    private int _valueWidth;
    private int _labelWidth;
    private FrmPointSymbolSet _frmPointSymbolSet;
    private FrmPolylineSymbolSet _frmPolylineSymbolSet;
    private FrmPolygonSymbolSet _frmPolygonSymbolSet;
    private FrmColorSymbolSet _frmColorSymbolSet;
    private List<Integer> _selectedRows = new ArrayList<>();
    private int _startRow = -1;
    private ColorBreak _curBreak = null;
    private JScrollBar _vScrollBar;
    private JTextField _textField;
    private LookAndFeel laf;
    private Color borderColor;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public LegendView() {
        super();

        laf = UIManager.getLookAndFeel();
        this.setBackground(laf.getDefaults().getColor("List.background"));
        this.borderColor = laf.getDefaults().getColor("List.dropLineColor");

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
        
        this._textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    LegendView.this.afterCellEdit();
                }
            }
        });
    }

    private void initComponents() {
        this.setPreferredSize(new Dimension(100, 200));
        this.setLayout(new BorderLayout());
        //this.setBackground(Color.white);

        _vScrollBar = new JScrollBar(JScrollBar.VERTICAL);        
        _vScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                onScrollValueChanged(e);
            }
        });
        this.add(_vScrollBar, BorderLayout.EAST);
        this._vScrollBar.setSize(20, this.getHeight());
        this._vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);        

        _textField = new JTextField();
        _textField.setVisible(false);
        this.add(_textField);

        _frmPointSymbolSet = null;
        _frmPolylineSymbolSet = null;
        _frmPolygonSymbolSet = null;
        _frmColorSymbolSet = null;
        _legendScheme = null;
        _breakHeight = 20;
        _symbolWidth = 60;
        _valueWidth = (this.getWidth() - _symbolWidth) / 2;
        _labelWidth = _valueWidth;
    }
    // </editor-fold>
    // <editor-fold desc="Events">

    public void onComponentResized(ComponentEvent e) {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            _valueWidth = (this.getWidth() - _symbolWidth) / 2;
            _labelWidth = _valueWidth;

            this._vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);
            this._vScrollBar.setSize(this._vScrollBar.getWidth(), this.getHeight());
        }

        this.repaint();
    }

    public void onScrollValueChanged(AdjustmentEvent e) {
        _vScrollBar.setValue(e.getValue());
        this.repaint();
    }

    public void onMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (this._textField.isVisible()) {
                    afterCellEdit();
                }

                int[] curTops = new int[1];
                curTops[0] = 0;
                int bIdx = findBreakIndexByPosition(e.getY(), curTops);
                if (bIdx >= 0) {
                    if (e.isControlDown()) {
                        if (_selectedRows.contains(bIdx)) {
                            _selectedRows.remove(bIdx);
                        } else {
                            _selectedRows.add(bIdx);
                            _startRow = bIdx;
                        }
                    } else if (e.isShiftDown()) {
                        _selectedRows.clear();
                        if (_startRow == -1) {
                            _selectedRows.add(bIdx);
                        } else {
                            if (bIdx > _startRow) {
                                for (int i = _startRow; i <= bIdx; i++) {
                                    _selectedRows.add(i);
                                }
                            } else {
                                for (int i = bIdx; i <= _startRow; i++) {
                                    _selectedRows.add(i);
                                }
                            }
                        }
                    } else {
                        _selectedRows.clear();
                        _selectedRows.add(bIdx);
                        _startRow = bIdx;
                    }

                    this.repaint();
                }
            }
        } else if (e.getClickCount() == 2) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (this._textField.isVisible()) {
                    afterCellEdit();
                }

                int[] curTops = new int[1];
                curTops[0] = 0;
                int bIdx = findBreakIndexByPosition(e.getY(), curTops);
                int curTop = curTops[0];
                if (bIdx >= 0) {
                    if (e.isControlDown()) {
                        if (_selectedRows.contains(bIdx)) {
                            _selectedRows.remove(bIdx);
                        } else {
                            _selectedRows.add(bIdx);
                            _startRow = bIdx;
                        }
                    } else if (e.isShiftDown()) {
                        _selectedRows.clear();
                        if (_startRow == -1) {
                            _selectedRows.add(bIdx);
                        } else {
                            if (bIdx > _startRow) {
                                for (int i = _startRow; i <= bIdx; i++) {
                                    _selectedRows.add(i);
                                }
                            } else {
                                for (int i = bIdx; i <= _startRow; i++) {
                                    _selectedRows.add(i);
                                }
                            }
                        }
                    } else {
                        _selectedRows.clear();
                        _selectedRows.add(bIdx);
                        _startRow = bIdx;
                    }

                    ColorBreak aCB = _legendScheme.getLegendBreaks().get(bIdx);
                    _curBreak = aCB;
                    if (ifInSymbol(e.getX())) {
                        showSymbolSetForm(aCB);
                    } else if (ifInValue(e.getX())) {
                        _textField.setLocation(_symbolWidth, curTop);
                        _textField.setSize(_valueWidth, _breakHeight);
                        _textField.setText(aCB.getValueString());
                        _textField.setName("Value");
                        _textField.setVisible(true);
                    } else if (ifInLabel(e.getX())) {
                        if (_legendScheme.getLegendType() == LegendType.SingleSymbol) {
                            _textField.setLocation(_symbolWidth, curTop);
                        } else {
                            _textField.setLocation(_symbolWidth + _valueWidth, curTop);
                        }
                        _textField.setSize(_valueWidth, _breakHeight);
                        _textField.setText(aCB.getCaption());
                        _textField.setName("Label");
                        _textField.setVisible(true);
                    }
                }
                this.repaint();
            }
        }
    }

    private ColorBreak findBreakByPosition(int y, int[] curTops) {
        ColorBreak aCB = null;
        int idx = findBreakIndexByPosition(y, curTops);
        if (idx >= 0) {
            aCB = _legendScheme.getLegendBreaks().get(idx);
        }

        return aCB;
    }

    private int findBreakIndexByPosition(int y, int[] curTops) {
        int idx = -1;
        int curTop = curTops[0];
        if (_vScrollBar.isVisible()) {
            curTop = curTop - _vScrollBar.getValue();
        }

        for (int i = 0; i < _legendScheme.getBreakNum(); i++) {
            curTop += _breakHeight;
            if (y > curTop && y < curTop + _breakHeight) {
                idx = i;
                break;
            }
        }

        curTops[0] = curTop;
        return idx;
    }

    private boolean ifInSymbol(int x) {
        if (x > 0 && x < _symbolWidth) {
            return true;
        } else {
            return false;
        }
    }

    private boolean ifInValue(int x) {
        if (_legendScheme.getLegendType() == LegendType.SingleSymbol) {
            return false;
        } else {
            if (x > _symbolWidth && x < _symbolWidth + _valueWidth) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean ifInLabel(int x) {
        if (_legendScheme.getLegendType() == LegendType.SingleSymbol) {
            if (x > _symbolWidth && x < _symbolWidth + _labelWidth) {
                return true;
            } else {
                return false;
            }
        } else {
            if (x > _symbolWidth + _valueWidth && x < _symbolWidth + _valueWidth + _labelWidth) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void showSymbolSetForm(ColorBreak aCB) {
        switch (_legendScheme.getBreakType()) {
            case PointBreak:
                PointBreak aPB = (PointBreak) aCB;

                if (_frmPointSymbolSet == null) {
                    _frmPointSymbolSet = new FrmPointSymbolSet((JDialog) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPointSymbolSet.setLocationRelativeTo(this);
                    _frmPointSymbolSet.setVisible(true);
                }
                _frmPointSymbolSet.setPointBreak(aPB);
                _frmPointSymbolSet.setVisible(true);
                break;
            case PolylineBreak:
                PolylineBreak aPLB = (PolylineBreak) aCB;

                if (_frmPolylineSymbolSet == null) {
                    _frmPolylineSymbolSet = new FrmPolylineSymbolSet((JDialog) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPolylineSymbolSet.setLocationRelativeTo(this);
                    _frmPolylineSymbolSet.setVisible(true);
                }
                _frmPolylineSymbolSet.setPolylineBreak(aPLB);
                _frmPolylineSymbolSet.setVisible(true);
                break;
            case PolygonBreak:
                PolygonBreak aPGB = (PolygonBreak) aCB;

                if (_frmPolygonSymbolSet == null) {
                    _frmPolygonSymbolSet = new FrmPolygonSymbolSet((JDialog) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmPolygonSymbolSet.setLocationRelativeTo(this);
                    _frmPolygonSymbolSet.setVisible(true);
                }
                _frmPolygonSymbolSet.setPolygonBreak(aPGB);
                _frmPolygonSymbolSet.setVisible(true);
                break;
            case ColorBreak:
                if (_frmColorSymbolSet == null) {
                    _frmColorSymbolSet = new FrmColorSymbolSet((JDialog) SwingUtilities.getWindowAncestor(this), false, this);
                    _frmColorSymbolSet.setLocationRelativeTo(this);
                    _frmColorSymbolSet.setVisible(true);
                }
                _frmColorSymbolSet.setColorBreak(aCB);
                _frmColorSymbolSet.setVisible(true);
                break;
        }
    }

    private void afterCellEdit() {
        if (_textField.getName().equals("Value")) {
            String aValue = _textField.getText().trim();
            double sValue, eValue;
            int aIdx;

            aIdx = aValue.indexOf("-");
            if (aIdx > 0) {
                if (aValue.substring(aIdx - 1, aIdx).equals("E")) {
                    aIdx = aValue.indexOf("-", aIdx + 1);
                }
                sValue = Double.parseDouble(aValue.substring(0, aIdx).trim());
                eValue = Double.parseDouble(aValue.substring(aIdx + 1).trim());
                aValue = aValue.substring(0, aIdx).trim() + " - " + aValue.substring(aIdx + 1).trim();
                _curBreak.setStartValue(sValue);
                _curBreak.setEndValue(eValue);
            } else if (aIdx == 0) {
                aIdx = aValue.substring(1).indexOf("-");
                if (aIdx > 0) {
                    aIdx += 1;
                    sValue = Double.parseDouble(aValue.substring(0, aIdx).trim());
                    eValue = Double.parseDouble(aValue.substring(aIdx + 1).trim());
                    aValue = aValue.substring(0, aIdx).trim() + " - " + aValue.substring(aIdx + 1).trim();
                } else {
                    sValue = Double.parseDouble(aValue);
                    eValue = sValue;
                }
                _curBreak.setStartValue(sValue);
                _curBreak.setEndValue(eValue);
            } else {
                if (MIMath.isNumeric(aValue)) {
                    sValue = Double.parseDouble(aValue);
                    eValue = sValue;
                }
                _curBreak.setStartValue(aValue);
                _curBreak.setEndValue(aValue);
            }

            _curBreak.setCaption(aValue);
        } else if (_textField.getName().equals("Label")) {
            String caption = _textField.getText().trim();
            _curBreak.setCaption(caption);
        }

        _textField.setVisible(false);
        this.repaint();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get legend scheme
     *
     * @return The legend scheme
     */
    public LegendScheme getLegendScheme() {
        return this._legendScheme;
    }

    /**
     * Set legend scheme
     *
     * @param ls The legend scheme
     */
    public void setLegendScheme(LegendScheme ls) {
        this._legendScheme = ls;
    }

    /**
     * Get selected rows
     *
     * @return The selected rows
     */
    public List<Integer> getSelectedRows() {
        return _selectedRows;
    }

    /**
     * Get break height
     *
     * @return Break height
     */
    public int getBreakHeight() {
        return this._breakHeight;
    }

    /**
     * Set break height
     *
     * @param height Break height
     */
    public void setBreakHeight(int height) {
        this._breakHeight = height;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Update legend scheme
     *
     * @param aLS The legend scheme
     */
    public void update(LegendScheme aLS) {
        _legendScheme = aLS;
        this.repaint();
    }

    public void setLegendBreak_Color(Color aColor) {        
        for (int rowIdx : _selectedRows) {
            _legendScheme.getLegendBreaks().get(rowIdx).setColor(aColor);
        }

        this.repaint();        
    }
    
    public void setLegendBreak_Color_Transparency(int alpha){
        for (int rowIdx : _selectedRows) {
            Color c = _legendScheme.getLegendBreaks().get(rowIdx).getColor();
            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
            _legendScheme.getLegendBreaks().get(rowIdx).setColor(c);
        }
        
        this.repaint();
    }

    /**
     * Set legend break outline color
     * @param aColor The color
     */ 
        public void setLegendBreak_OutlineColor(Color aColor)
        {
            for (int rowIdx : _selectedRows)
            {                
                switch (_legendScheme.getBreakType())
                {
                    case PointBreak:
                        PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPB.setOutlineColor(aColor);
                        //_legendScheme.LegendBreaks[rowIdx] = aPB;
                        break;
                    case PolygonBreak:
                        PolygonBreak aPGB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPGB.setOutlineColor(aColor);
                        //_legendScheme.LegendBreaks[rowIdx] = aPGB;
                        break;
                }
            }

            this.repaint();
        }

        /**
         * Set legend break outline size
         * @param outlineSize The size
         */
        public void setLegendBreak_OutlineSize(float outlineSize)
        {
            for (int rowIdx : _selectedRows)
            {                
                switch (_legendScheme.getBreakType())
                {
                    case PointBreak:
                        PointBreak pb = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        pb.setOutlineSize(outlineSize);
                        break;
                    case PolygonBreak:
                        PolygonBreak aPGB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPGB.setOutlineSize(outlineSize);
                        //_legendScheme.LegendBreaks[rowIdx] = aPGB;
                        break;
                }
            }

            this.repaint();
        }

        /**
         * Set legend break angle
         * @param angle The angle
         */
        public void setLegendBreak_Angle(float angle)
        {
            for (int rowIdx : _selectedRows)
            {                
                PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPB.setAngle(angle);
                //_legendScheme.LegendBreaks[rowIdx] = aPB;
            }

            this.repaint();
        }
        
        /**
         * Set legend break alpha
         * @param alpha Alpha value
         */
        public void setLegendBreak_Alpha(int alpha){
            for (int rowIdx : _selectedRows){
                ColorBreak aCB = _legendScheme.getLegendBreaks().get(rowIdx);
                Color c = aCB.getColor();
                aCB.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            }
            this.repaint();
        }

        /**
         * Set legend break size
         * @param aSize The size
         */
        public void setLegendBreak_Size(float aSize)
        {
            for (int rowIdx : _selectedRows)
            {                
                switch (_legendScheme.getBreakType())
                {
                    case PointBreak:
                        PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPB.setSize(aSize);
                        //_legendScheme.LegendBreaks[rowIdx] = aPB;
                        break;
                    case PolylineBreak:
                        PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPLB.setWidth(aSize);
                        //_legendScheme.LegendBreaks[rowIdx] = aPLB;
                        break;
                    case PolygonBreak:
                        PolygonBreak aPGB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPGB.setOutlineSize(aSize);
                        //_legendScheme.LegendBreaks[rowIdx] = aPGB;
                        break;
                }
            }
            
            this.repaint();
        }

        /**
         * Set legend break point style
         * @param aPS 
         */
        public void setLegendBreak_PointStyle(PointStyle aPS)
        {
            for (int rowIdx : _selectedRows)
            {                
                PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPB.setStyle(aPS);
                //_legendScheme.getLegendBreaks().get(rowIdx) = aPB;
            }

            this.repaint();
        }

        /**
         * Set legend break polyline style
         * @param style The polyline style
         */
        public void setLegendBreak_PolylineStyle(LineStyles style)
        {
            for (int rowIdx : _selectedRows)
            {                
                PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPLB.setStyle(style);
                //_legendScheme.LegendBreaks[rowIdx] = aPLB;
            }

            this.repaint();
        }
        
        /**
         * Set legend break polygon style
         * @param style The polygon style
         */
        public void setLegendBreak_PolygonStyle(HatchStyle style)
        {
            for (int rowIdx : _selectedRows)
            {                
                PolygonBreak aPLB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPLB.setStyle(style);
            }

            this.repaint();
        }

        /**
         * Set legend break if draw outline
         * @param drawOutLine If draw outline
         */
        public void setLegendBreak_DrawOutline(boolean drawOutLine)
        {
            for (int rowIdx : _selectedRows)
            {                
                switch (_legendScheme.getBreakType())
                {
                    case PointBreak:
                        PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPB.setDrawOutline(drawOutLine);
                        //_legendScheme.LegendBreaks[rowIdx] = aPB;
                        break;
                    case PolygonBreak:
                        PolygonBreak aPGB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPGB.setDrawOutline(drawOutLine);
                        //_legendScheme.LegendBreaks[rowIdx] = aPGB;
                        break;
                }
            }

            this.repaint();
        }

        /**
         * Set legend break if draw fill
         * @param drawFill If draw fill
         */
        public void setLegendBreak_DrawFill(boolean drawFill)
        {
            for (int rowIdx : _selectedRows)
            {                
                switch (_legendScheme.getBreakType())
                {
                    case PointBreak:
                        PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPB.setDrawFill(drawFill);
                        //_legendScheme.LegendBreaks[rowIdx] = aPB;
                        break;
                    case PolygonBreak:
                        PolygonBreak aPGB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPGB.setDrawFill(drawFill);
                        //_legendScheme.LegendBreaks[rowIdx] = aPGB;
                        break;
                }
            }

            this.repaint();
        }

        /**
         * Set legend break if draw shape
         * @param drawShape If draw shape
         */
        public void setLegendBreak_DrawShape(boolean drawShape)
        {
            for (int rowIdx : _selectedRows)
            {                
                switch (_legendScheme.getBreakType())
                {
                    case PointBreak:
                        PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPB.setDrawShape(drawShape);
                        //_legendScheme.LegendBreaks[rowIdx] = aPB;
                        break;
                    case PolylineBreak:
                        PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPLB.setDrawPolyline(drawShape);
                        //_legendScheme.LegendBreaks[rowIdx] = aPLB;
                        break;
                    case PolygonBreak:
                        PolygonBreak aPGB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                        aPGB.setDrawShape(drawShape);
                        //_legendScheme.LegendBreaks[rowIdx] = aPGB;
                        break;
                }
            }

            this.repaint();
        }

//        public void setLegendBreak_UsingHatchStyle(boolean usginHatchStyle)
//        {
//            for (int rowIdx : _selectedRows)
//            {                
//                PolygonBreak aPGB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
//                aPGB.setUsingHatchStyle(usginHatchStyle);
//                _legendScheme.LegendBreaks[rowIdx] = aPGB;
//            }
//
//            this.repaint();
//        }

//        public void setLegendBreak_HatchStyle(HatchStyle hatchStyle)
//        {
//            foreach (int rowIdx in _selectedRows)
//            {               
//                PolygonBreak aPGB = (PolygonBreak)_legendScheme.LegendBreaks[rowIdx];
//                aPGB.Style = hatchStyle;
//                _legendScheme.LegendBreaks[rowIdx] = aPGB;
//            }
//
//            this.Invalidate();
//        }

        /**
         * Set legend break marker type
         * @param markerType Marker type
         */
        public void setLegendBreak_MarkerType(MarkerType markerType)
        {
            for (int rowIdx : _selectedRows)
            {                
                PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPB.setMarkerType(markerType);
                //_legendScheme.LegendBreaks[rowIdx] = aPB;
            }
            
            this.repaint();
        }

        /**
         * Set legend break font name
         * @param fontName Font name
         */
        public void setLegendBreak_FontName(String fontName)
        {
            for (int rowIdx : _selectedRows)
            {                
                PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPB.setFontName(fontName);
                //_legendScheme.LegendBreaks[rowIdx] = aPB;
            }
            
            this.repaint();
        }

        /**
         * Set legend break image path
         * @param imagePath Image paht
         */
        public void setLegendBreak_Image(String imagePath)
        {
            for (int rowIdx : _selectedRows)
            {                
                PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPB.setImagePath(imagePath);
                //_legendScheme.LegendBreaks[rowIdx] = aPB;
            }
            
            this.repaint();
        }

        /**
         * Set legend brea marker index
         * @param markerIdx Marker index
         */
        public void setLegendBreak_MarkerIndex(int markerIdx)
        {
            for (int rowIdx : _selectedRows)
            {                
                PointBreak aPB = (PointBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                switch (aPB.getMarkerType())
                {
                    case Character:
                        aPB.setCharIndex(markerIdx);
                        break;
                    case Simple:
                        aPB.setStyle(PointStyle.values()[markerIdx]);
                        break;
                    case Image:

                        break;
                }
                //_legendScheme.LegendBreaks[rowIdx] = aPB;
            }

            this.repaint();
        }

        /**
         * Set legend break background color
         * @param backColor Background color
         */
        public void setLegendBreak_BackColor(Color backColor)
        {
            for (int rowIdx : _selectedRows)
            {                
                PolygonBreak aPGB = (PolygonBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPGB.setBackColor(backColor);
                //_legendScheme.LegendBreaks[rowIdx] = aPGB;
            }

            this.repaint();
        }

        /**
         * Set legend break if draw symbol
         * @param drawSymbol If draw symbol
         */
        public void setLegendBreak_DrawSymbol(boolean drawSymbol)
        {
            if (_selectedRows.size() > 1) {
                int i = 0;
                for (int rowIdx : _selectedRows)
                {                
                    PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                    aPLB.setDrawSymbol(drawSymbol);
                    aPLB.setSymbolStyle(PointStyle.values()[i]);
                    i ++;
                    if (i == PointStyle.values().length)
                        i = 0;
                }
            } else {
                PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(_selectedRows.get(0));
                aPLB.setDrawSymbol(drawSymbol);
            }

            this.repaint();
        }

        /**
         * Set legend break symbol size
         * @param symbolSize Symbol size
         */
        public void setLegendBreak_SymbolSize(float symbolSize)
        {
            for (int rowIdx : _selectedRows)
            {                
                PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPLB.setSymbolSize(symbolSize);
                //_legendScheme.LegendBreaks[rowIdx] = aPLB;
            }

            this.repaint();
        }

        /**
         * Set legend break symbol size
         * @param symbolStyle Symbol size
         */
        public void setLegendBreak_SymbolStyle(PointStyle symbolStyle)
        {
            for (int rowIdx : _selectedRows)
            {                
                PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPLB.setSymbolStyle(symbolStyle);
                //_legendScheme.LegendBreaks[rowIdx] = aPLB;
            }

            this.repaint();
        }

        /**
         * Set legend break symbol color
         * @param symbolColor Symbol color
         */
        public void setLegendBreak_SymbolColor(Color symbolColor)
        {
            for (int rowIdx : _selectedRows)
            {                
                PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPLB.setSymbolColor(symbolColor);
                //_legendScheme.LegendBreaks[rowIdx] = aPLB;
            }

            this.repaint();
        }

        /**
         * Set legend break symbol interval
         * @param symbolInterval Symbol interval
         */
        public void setLegendBreak_SymbolInterval(int symbolInterval)
        {
            for (int rowIdx : _selectedRows)
            {                
                PolylineBreak aPLB = (PolylineBreak)_legendScheme.getLegendBreaks().get(rowIdx);
                aPLB.setSymbolInterval(symbolInterval);
                //_legendScheme.LegendBreaks[rowIdx] = aPLB;
            }

            this.repaint();
        }

    // <editor-fold desc="Paint Methods">
    /**
     * Paint component
     *
     * @param g Graphics
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        if (_legendScheme != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            paintLegendScheme(g2);
        }
    }

    private void paintLegendScheme(Graphics2D g) {
        int TotalHeight = calcTotalDrawHeight();
        Rectangle rect;
        if (TotalHeight > this.getHeight()) {
            _vScrollBar.setLocation(this.getWidth() - this._vScrollBar.getWidth(), 0);
            _vScrollBar.setSize(this._vScrollBar.getWidth(), this.getHeight());
            _vScrollBar.setMinimum(0);
            _vScrollBar.setUnitIncrement(_breakHeight);
            _vScrollBar.setBlockIncrement(this.getHeight());
            _vScrollBar.setMaximum(TotalHeight);

            if (_vScrollBar.isVisible() == false) {
                _vScrollBar.setValue(0);
                _vScrollBar.setVisible(true);
            }
            rect = new Rectangle(0, -_vScrollBar.getValue(), this.getWidth() - _vScrollBar.getWidth(), TotalHeight);

            _valueWidth = (this.getWidth() - _vScrollBar.getWidth() - _symbolWidth) / 2;
            _labelWidth = _valueWidth;
        } else {
            _vScrollBar.setVisible(false);
            rect = new Rectangle(0, 0, this.getWidth(), this.getHeight());

            _valueWidth = (this.getWidth() - _symbolWidth) / 2;
            _labelWidth = _valueWidth;
        }

        //Draw breaks
        drawBreaks(g, rect.y);
        
        //Draw title            
        drawTitle(g);             
    }

    private void drawTitle(Graphics2D g) {
        //Symbol
        int sX = 0;
        //Font aFont = new Font("Arial", Font.PLAIN, 12);
        Font font = this.getFont();
        Color bColor = this.getBackground();
        g.setColor(bColor);
        g.fill(new Rectangle(sX, 0, _symbolWidth, _breakHeight));
        g.setColor(this.borderColor);
        g.draw(new Rectangle(0, 0, _symbolWidth, _breakHeight));
        String str = "Symbol";
        FontMetrics metrics = g.getFontMetrics(font);
        Dimension size = new Dimension(metrics.stringWidth(str), metrics.getHeight());
        int cx = _symbolWidth / 2;
        int cy = _breakHeight / 2;
        PointF aPoint = new PointF(cx - size.width / 2, cy + size.height / 3);
        g.setFont(font);
        g.setColor(this.getForeground());
        g.drawString(str, aPoint.X, aPoint.Y);

        if (_legendScheme.getLegendType() != LegendType.SingleSymbol) {
            //Value
            sX = _symbolWidth;
            g.setColor(bColor);
            g.fill(new Rectangle(sX, 0, _valueWidth, _breakHeight));
            g.setColor(this.borderColor);
            g.draw(new Rectangle(sX, 0, _valueWidth, _breakHeight));
            str = "Value";
            size = new Dimension(metrics.stringWidth(str), metrics.getHeight());
            cx = sX + _valueWidth / 2;
            aPoint = new PointF(cx - size.width / 2, cy + size.height / 3);
            g.setColor(this.getForeground());
            g.drawString(str, aPoint.X, aPoint.Y);

            //Label
            sX = _symbolWidth + _valueWidth;
        } else {
            sX = _symbolWidth;
        }

        //Label   
        g.setColor(bColor);
        g.fill(new Rectangle(sX, 0, _labelWidth, _breakHeight));
        g.setColor(this.borderColor);
        g.draw(new Rectangle(sX, 0, _labelWidth, _breakHeight));
        str = "Label";
        size = new Dimension(metrics.stringWidth(str), metrics.getHeight());
        cx = sX + _labelWidth / 2;
        aPoint = new PointF(cx - size.width / 2, cy + size.height / 3);
        g.setColor(this.getForeground());
        g.drawString(str, aPoint.X, aPoint.Y);
    }

    private void drawBreaks(Graphics2D g, int sHeight) {
        Point sP = new Point(0, sHeight + _breakHeight);
        for (int i = 0; i < _legendScheme.getBreakNum(); i++) {
            if (sP.y + _breakHeight > _breakHeight) {
                ColorBreak aCB = _legendScheme.getLegendBreaks().get(i);
                Rectangle rect = new Rectangle(sP.x, sP.y, _symbolWidth, _breakHeight);
                boolean selected = _selectedRows.contains(i);                
                drawBreakSymbol(aCB, rect, selected, g);                
                sP.y += _breakHeight;
            } else if (sP.y > this.getHeight()) {
                break;
            } else {
                sP.y += _breakHeight;                
            }
        }
    }

    private void drawBreakSymbol(ColorBreak aCB, Rectangle rect, boolean selected, Graphics2D g) {
        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float aSize;
        PointF aP = new PointF(0, 0);
        float width, height;
        aP.X = rect.x + rect.width / 2;
        aP.Y = rect.y + rect.height / 2;

        //Draw selected back color
        if (selected) {
            g.setColor(Color.lightGray);
            g.fill(new Rectangle(_symbolWidth, rect.y, _valueWidth + _labelWidth, rect.height));
        }

        //Draw symbol
        switch (aCB.getBreakType()) {
            case PointBreak:
                PointBreak aPB = (PointBreak) aCB;
                aSize = aPB.getSize();
                if (aPB.isDrawShape()) {
                    if (aPB.getMarkerType() == MarkerType.Character) {
                        Draw.drawPoint(aP, aPB, g);
                    } else {
                        Draw.drawPoint(aP, aPB, g);
                    }
                }
                break;
            case PolylineBreak:
                PolylineBreak aPLB = (PolylineBreak) aCB;
                aSize = aPLB.getWidth();
                width = rect.width / 3 * 2;
                height = rect.height / 3 * 2;
                Draw.drawPolylineSymbol(aP, width, height, aPLB, g);
                break;
            case PolygonBreak:
                PolygonBreak aPGB = (PolygonBreak) aCB;
                width = rect.width / 3 * 2;
                height = rect.height / 5 * 4;
                if (aPGB.isDrawShape()) {
                    Draw.drawPolygonSymbol(aP, width, height, aPGB, g);
                }                
                break;
            case ColorBreak:
                width = rect.width / 3 * 2;
                height = rect.height / 3 * 2;
                Draw.drawPolygonSymbol(aP, aCB.getColor(), Color.black, width,
                        height, true, true, g);
                break;
        }
        
        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        //Draw value and label
        int sX = _symbolWidth;
        //Font aFont = new Font("Simsun", Font.PLAIN, 13);
        Font font = this.getFont();
        String str = aCB.getCaption();
        FontMetrics metrics = g.getFontMetrics(font);
        Dimension size = new Dimension(metrics.stringWidth(str), metrics.getHeight());
        aP.X = sX;
        aP.Y = rect.y + rect.height * 3 / 4;

        g.setFont(font);
        g.setColor(this.getForeground());
        if (_legendScheme.getLegendType() == LegendType.SingleSymbol) {
            g.drawString(str, aP.X, aP.Y);
        } else {
            //Label
            aP.X += _valueWidth;
            g.drawString(str, aP.X, aP.Y);

            //Value
            if (String.valueOf(aCB.getStartValue()).equals(
                    String.valueOf(aCB.getEndValue()))) {
                str = String.valueOf(aCB.getStartValue());
            } else {
                str = String.valueOf(aCB.getStartValue()) + " - " + String.valueOf(aCB.getEndValue());
            }

            //size = new Dimension(metrics.stringWidth(str), metrics.getHeight());
            aP.X = sX;
            Rectangle clip = g.getClipBounds();  
            g.clipRect(sX, rect.y, this._valueWidth - 5, rect.height);  
            g.drawString(str, aP.X, aP.Y);
            g.setClip(clip.x, clip.y, clip.width, clip.height); 
        }
    }

    private int calcTotalDrawHeight() {
        return _breakHeight * (_legendScheme.getBreakNum() + 1);
    }
    // </editor-fold>
    // </editor-fold>
}
