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

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import org.meteoinfo.global.event.ILayersUpdatedListener;
import org.meteoinfo.global.event.IMapViewUpdatedListener;
import org.meteoinfo.global.event.LayersUpdatedEvent;
import org.meteoinfo.global.event.MapViewUpdatedEvent;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.GridLabelPosition;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.map.GridLabel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.meteoinfo.data.mapdata.webmap.TileLoadListener;

/**
 *
 * @author yaqiang
 */
public class LayoutMap extends LayoutElement {
    // <editor-fold desc="Events">

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
    // </editor-fold>
    // <editor-fold desc="Variables">
    private EventListenerList _listeners = new EventListenerList();
    private MapFrame _mapFrame = null;
    private boolean _drawDegreeSymbol = false;
    private final TileLoadListener tileLoadListener;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param mapFrame MapFrame
     * @param tll TileLoadListener
     */
    public LayoutMap(MapFrame mapFrame, TileLoadListener tll) {
        super();
        this.setElementType(ElementType.LayoutMap);
        this.setResizeAbility(ResizeAbility.ResizeAll);
        this.setMapFrame(mapFrame);
        this.tileLoadListener = tll;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    public MapFrame getMapFrame() {
        return _mapFrame;
    }

    public void setMapFrame(MapFrame mf) {
        _mapFrame = mf;
        _mapFrame.addMapViewUpdatedListener(new IMapViewUpdatedListener() {
            @Override
            public void mapViewUpdatedEvent(MapViewUpdatedEvent event) {
                fireMapViewUpdatedEvent();
            }
        });

        _mapFrame.addLayersUpdatedListener(new ILayersUpdatedListener() {
            @Override
            public void layersUpdatedEvent(LayersUpdatedEvent event) {
                fireMapViewUpdatedEvent();
            }
        });
    }

    /**
     * Get left
     *
     * @return Left
     */
    @Override
    public int getLeft() {
        return _mapFrame.getLayoutBounds().x;
    }

    /**
     * Set left
     *
     * @param left Left
     */
    @Override
    public void setLeft(int left) {
        _mapFrame.setLayoutBounds(new Rectangle(left, _mapFrame.getLayoutBounds().y, _mapFrame.getLayoutBounds().width,
                _mapFrame.getLayoutBounds().height));
    }

    /**
     * Get top
     *
     * @return Top
     */
    @Override
    public int getTop() {
        return _mapFrame.getLayoutBounds().y;
    }

    /**
     * Set top
     *
     * @param top Top
     */
    @Override
    public void setTop(int top) {
        _mapFrame.setLayoutBounds(new Rectangle(_mapFrame.getLayoutBounds().x, top, _mapFrame.getLayoutBounds().width,
                _mapFrame.getLayoutBounds().height));
    }

    /**
     * Get width
     *
     * @return Width
     */
    @Override
    public int getWidth() {
        return _mapFrame.getLayoutBounds().width;
    }

    /**
     * Set width
     *
     * @param width Width
     */
    @Override
    public void setWidth(int width) {
        _mapFrame.setLayoutBounds(new Rectangle(_mapFrame.getLayoutBounds().x, _mapFrame.getLayoutBounds().y, width,
                _mapFrame.getLayoutBounds().height));
    }

    /**
     * Get height
     *
     * @return Height
     */
    @Override
    public int getHeight() {
        return _mapFrame.getLayoutBounds().height;
    }

    /**
     * Set height
     *
     * @param height Height
     */
    @Override
    public void setHeight(int height) {
        _mapFrame.setLayoutBounds(new Rectangle(_mapFrame.getLayoutBounds().x, _mapFrame.getLayoutBounds().y,
                _mapFrame.getLayoutBounds().width, height));
    }

    /**
     * Get bounds rectangle
     *
     * @return The bounds rectangle
     */
    @Override
    public Rectangle getBounds() {
        return _mapFrame.getLayoutBounds();
    }

    /**
     * Set bounds rectangle
     *
     * @param rect Bounds rectangle
     */
    public void setBounds(Rectangle rect) {
        _mapFrame.setLayoutBounds(rect);
    }

    /**
     * Get background color
     *
     * @return Background color
     */
    @Override
    public Color getBackColor() {
        return _mapFrame.getBackColor();
    }

    /**
     * Set background color
     *
     * @param color Background color
     */
    @Override
    public void setBackColor(Color color) {
        _mapFrame.setBackColor(color);
    }

    /**
     * Get foreground color
     *
     * @return Foreground color
     */
    @Override
    public Color getForeColor() {
        return _mapFrame.getForeColor();
    }

    /**
     * Set foreground color
     *
     * @param color
     */
    @Override
    public void setForeColor(Color color) {
        _mapFrame.setForeColor(color);
    }

    /**
     * Get if draw map view neat line
     *
     * @return Boolean
     */
    public boolean isDrawNeatLine() {
        return _mapFrame.isDrawNeatLine();
    }

    /**
     * Set if draw map view neat line
     *
     * @param istrue Boolean
     */
    public void setDrawNeatLine(boolean istrue) {
        _mapFrame.setDrawNeatLine(istrue);
    }

    /**
     * Get map view neat line color
     *
     * @return Neat line color
     */
    public Color getNeatLineColor() {
        return _mapFrame.getNeatLineColor();
    }

    /**
     * Set map view neat line color
     *
     * @param color Neat line color
     */
    public void setNeatLineColor(Color color) {
        _mapFrame.setNeatLineColor(color);
    }

    /**
     * Get map view neat line size
     *
     * @return Neat line size
     */
    public float getNeatLineSize() {
        return _mapFrame.getNeatLineSize();
    }

    /**
     * Set map view neat line size
     *
     * @param size Neat line size
     */
    public void setNeatLineSize(float size) {
        _mapFrame.setNeatLineSize(size);
    }

    /**
     * Get grid line color
     *
     * @return Grid line color
     */
    public Color getGridLineColor() {
        return _mapFrame.getGridLineColor();
    }

    /**
     * Set grid line color
     *
     * @param color Grid line color
     */
    public void setGridLineColor(Color color) {
        _mapFrame.setGridLineColor(color);
    }

    /**
     * Get grid line size
     *
     * @return Grid line size
     */
    public float getGridLineSize() {
        return _mapFrame.getGridLineSize();
    }

    /**
     * Set grid line size
     *
     * @param size Grid line size
     */
    public void setGridLineSize(float size) {
        _mapFrame.setGridLineSize(size);
    }

    /**
     * Get grid line style
     *
     * @return Grid line style
     */
    public LineStyles getGridLineStyle() {
        return _mapFrame.getGridLineStyle();
    }

    /**
     * Set grid line style
     *
     * @param style Grid line style
     */
    public void setGridLineStyle(LineStyles style) {
        _mapFrame.setGridLineStyle(style);
    }

    /**
     * Get if draw grid labels
     *
     * @return If draw grid labels
     */
    public boolean isDrawGridLabel() {
        return _mapFrame.isDrawGridLabel();
    }

    /**
     * Set if draw grid labels
     *
     * @param istrue Boolean
     */
    public void setDrawGridLabel(boolean istrue) {
        _mapFrame.setDrawGridLabel(istrue);
    }

    /**
     * Get if draw grid tick line inside
     *
     * @return Booelan
     */
    public boolean isInsideTickLine() {
        return _mapFrame.isInsideTickLine();
    }

    /**
     * Set if draw grid tick line inside
     *
     * @param istrue Boolean
     */
    public void setInsideTickLine(boolean istrue) {
        _mapFrame.setInsideTickLine(istrue);
    }

    /**
     * Get grid tick line length
     *
     * @return Grid tick line length
     */
    public int getTickLineLength() {
        return _mapFrame.getTickLineLength();
    }

    /**
     * Set grid tick line length
     *
     * @param value Tick line length value
     */
    public void setTickLineLength(int value) {
        _mapFrame.setTickLineLength(value);
    }

    /**
     * Get grid label shift
     *
     * @return Grid label shift
     */
    public int getGridLabelShift() {
        return _mapFrame.getGridLabelShift();
    }

    /**
     * Set grid label shift
     *
     * @param value Grid label shift
     */
    public void setGridLabelShift(int value) {
        _mapFrame.setGridLabelShift(value);
    }

    /// <summary>
    /// Get or set grid label position
    /**
     * Get grid label position
     *
     * @return Grid label position
     */
    public GridLabelPosition getGridLabelPosition() {
        return _mapFrame.getGridLabelPosition();
    }

    /**
     * Set grid label positiont
     *
     * @param value Grid label position
     */
    public void setGridLabelPosition(GridLabelPosition value) {
        _mapFrame.setGridLabelPosition(value);
    }

    /**
     * Get if draw grid line
     *
     * @return If draw grid line
     */
    public boolean isDrawGridLine() {
        return _mapFrame.isDrawGridLine();
    }

    /**
     * Set if draw grid line
     *
     * @param istrue If draw grid line
     */
    public void setDrawGridLine(boolean istrue) {
        _mapFrame.setDrawGridLine(istrue);
    }

    /**
     * Get if draw grid tick line
     *
     * @return Boolean
     */
    public boolean isDrawGridTickLine() {
        return _mapFrame.isDrawGridTickLine();
    }

    /**
     * Set if draw grid tick line
     *
     * @param istrue Boolean
     */
    public void setDrawGridTickLine(boolean istrue) {
        _mapFrame.setDrawGridTickLine(istrue);
    }

    /**
     * Get if draw degree symbol
     *
     * @return Boolean
     */
    public boolean isDrawDegreeSymbol() {
        return _drawDegreeSymbol;
    }

    /**
     * Set if draw degree symbol
     *
     * @param value Boolean
     */
    public void setDrawDegreeSymbol(boolean value) {
        _drawDegreeSymbol = value;
    }

    /**
     * Get grid label font
     *
     * @return Grid label font
     */
    public Font getGridFont() {
        return _mapFrame.getGridFont();
    }

    /**
     * Set grid label font
     *
     * @param font Grid label font
     */
    public void setGridFont(Font font) {
        _mapFrame.setGridFont(font);
    }

    /**
     * Get grid x delt
     *
     * @return Grid x delt
     */
    public double getGridXDelt() {
        return _mapFrame.getGridXDelt();
    }

    /**
     * Set grid x delt
     *
     * @param value The value
     */
    public void setGridXDelt(double value) {
        _mapFrame.setGridXDelt(value);
    }

    /**
     * Get grid y delt
     *
     * @return Grid y delt
     */
    public double getGridYDelt() {
        return _mapFrame.getGridYDelt();
    }

    /**
     * Set grid y delt
     *
     * @param value Grid y delt
     */
    public void setGridYDelt(double value) {
        _mapFrame.setGridYDelt(value);
    }

    /**
     * Get grid x origin
     *
     * @return Grid x origin
     */
    public float getGridXOrigin() {
        return _mapFrame.getGridXOrigin();
    }

    /**
     * Set grid x origin
     *
     * @param value Grid x origin
     */
    public void setGridXOrigin(float value) {
        _mapFrame.setGridXOrigin(value);
    }

    /**
     * Get grid y origin
     *
     * @return Grid y origin
     */
    public float getGridYOrigin() {
        return _mapFrame.getGridYOrigin();
    }

    /**
     * Set grid y origin
     *
     * @param value Grid y origin
     */
    public void setGridYOrigin(float value) {
        _mapFrame.setGridYOrigin(value);
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Zoom to exactly lon/lat extent
     *
     * @param aExtent The lon/lat extent
     */
    public void zoomToExtentLonLatEx(Extent aExtent) {
        if (!_mapFrame.getMapView().getProjection().isLonLatMap()) {
            aExtent = _mapFrame.getMapView().getProjection().getProjectedExtentFromLonLat(aExtent);
        }

        setSizeByExtent(aExtent);
        _mapFrame.getMapView().setViewExtent(aExtent);
    }

    private void setSizeByExtent(Extent aExtent) {
        double scaleFactor;

        double scaleX = this.getWidth() / (aExtent.maxX - aExtent.minX);
        double scaleY = this.getHeight() / (aExtent.maxY - aExtent.minY);
        if (_mapFrame.getMapView().getProjection().isLonLatMap()) {
            scaleFactor = _mapFrame.getMapView().getXYScaleFactor();
        } else {
            scaleFactor = 1;
        }

        if (scaleX > scaleY) {
            scaleX = scaleY / scaleFactor;
            this.setWidth((int) ((aExtent.maxX - aExtent.minX) * scaleX));
        } else {
            scaleY = scaleX * scaleFactor;
            this.setHeight((int) ((aExtent.maxY - aExtent.minY) * scaleY));
        }
    }

    /**
     * Paint method
     *
     * @param g Graphics2D
     */
    @Override
    public void paint(Graphics2D g) {
        if (_mapFrame != null) {
            g.setColor(_mapFrame.getMapView().getBackground());
            g.fill(_mapFrame.getLayoutBounds());

            _mapFrame.getMapView().paintGraphics(g, _mapFrame.getLayoutBounds(), this.tileLoadListener);

//                //Draw lon/lat grid labels
//                if (_mapFrame.getDrawGridLabel())
//                {
//                    List<Extent> extentList = new ArrayList<Extent>();
//                    Extent maxExtent = new Extent();
//                    Extent aExtent = new Extent();
//                    SizeF aSF = new SizeF();
//                    SolidBrush aBrush = new SolidBrush(this.ForeColor);
//                    Pen aPen = new Pen(_mapFrame.GridLineColor);
//                    aPen.Width = _mapFrame.GridLineSize;
//                    String drawStr;
//                    PointF sP = new PointF(0, 0);
//                    PointF eP = new PointF(0, 0);
//                    Font font = new Font(_mapFrame.getGridFont().Name, _mapFrame.getGridFont().Size, _mapFrame.getGridFont().Style);
//                    float labX, labY;
//                    int len = 5;
//                    int space = len + 2;
//                    for (int i = 0; i < _mapFrame.getMapView().getGridLabels().Count; i++)
//                    {
//                        GridLabel aGL = _mapFrame.getMapView().getGridLabels()[i];
//                        switch (_mapFrame.getGridLabelPosition())
//                        {
//                            case GridLabelPosition.LeftBottom:
//                                switch (aGL.LabDirection)
//                                {
//                                    case Direction.East:
//                                    case Direction.North:
//                                        continue;
//                                }
//                                break;
//                            case GridLabelPosition.LeftUp:
//                                switch (aGL.LabDirection)
//                                {
//                                    case Direction.East:
//                                    case Direction.South:
//                                        continue;
//                                }
//                                break;
//                            case GridLabelPosition.RightBottom:
//                                switch (aGL.LabDirection)
//                                {
//                                    case Direction.Weast:
//                                    case Direction.North:
//                                        continue;
//                                }
//                                break;
//                            case GridLabelPosition.RightUp:
//                                switch (aGL.LabDirection)
//                                {
//                                    case Direction.Weast:
//                                    case Direction.South:
//                                        continue;
//                                }
//                                break;
//                        }                        
//
//                        labX = (float)aGL.LabPoint.X;
//                        labY = (float)aGL.LabPoint.Y;
//                        labX = labX + this.Left;
//                        labY = labY + this.Top;
//                        sP.X = labX;
//                        sP.Y = labY;
//
//                        drawStr = aGL.LabString;
//                        aSF = g.MeasureString(drawStr, font);
//                        switch (aGL.LabDirection)
//                        {
//                            case Direction.South:                                
//                                labX = labX - aSF.Width / 2;
//                                labY = labY + space;
//                                eP.X = sP.X;
//                                eP.Y = sP.Y + len;
//                                break;
//                            case Direction.Weast:
//                                labX = labX - aSF.Width - space;
//                                labY = labY - aSF.Height / 2;
//                                eP.X = sP.X - len;
//                                eP.Y = sP.Y;
//                                break;
//                            case Direction.North:
//                                labX = labX - aSF.Width / 2;
//                                labY = labY - aSF.Height - space;
//                                eP.X = sP.X;
//                                eP.Y = sP.Y - len;
//                                break;
//                            case Direction.East:
//                                labX = labX + space;
//                                labY = labY - aSF.Height / 2;
//                                eP.X = sP.X + len;
//                                eP.Y = sP.Y;
//                                break;
//                        }
//
//                        bool ifDraw = true;
//                        float aSize = aSF.Width / 2;
//                        float bSize = aSF.Height / 2;
//                        aExtent.minX = labX;
//                        aExtent.maxX = labX + aSF.Width;
//                        aExtent.minY = labY - aSF.Height;
//                        aExtent.maxY = labY;
//
//                        //Judge extent                                        
//                        if (extentList.Count == 0)
//                        {
//                            maxExtent = aExtent;
//                            extentList.Add(aExtent);
//                        }
//                        else
//                        {
//                            if (!MIMath.IsExtentCross(aExtent, maxExtent))
//                            {
//                                extentList.Add(aExtent);
//                                maxExtent = MIMath.GetLagerExtent(maxExtent, aExtent);
//                            }
//                            else
//                            {
//                                for (int j = 0; j < extentList.Count; j++)
//                                {
//                                    if (MIMath.IsExtentCross(aExtent, extentList[j]))
//                                    {
//                                        ifDraw = false;
//                                        break;
//                                    }
//                                }
//                                if (ifDraw)
//                                {
//                                    extentList.Add(aExtent);
//                                    maxExtent = MIMath.GetLagerExtent(maxExtent, aExtent);
//                                }
//                            }
//                        }
//
//                        if (ifDraw)
//                        {
//                            g.DrawLine(aPen, sP, eP);
//                            g.DrawString(drawStr, font, aBrush, labX, labY);
//                        }
//                    }
//                }
            if (_mapFrame.isDrawNeatLine()) {
                g.setColor(_mapFrame.getNeatLineColor());
                g.setStroke(new BasicStroke(_mapFrame.getNeatLineSize()));
                g.draw(_mapFrame.getLayoutBounds());
            }
        }
    }

    @Override
    public void paintOnLayout(Graphics2D g, PointF pageLocation, float zoom) {
        if (_mapFrame != null) {
            PointF aP = pageToScreen(this.getLeft(), this.getTop(), pageLocation, zoom);
            Rectangle rect = new Rectangle((int) aP.X, (int) aP.Y, (int) (this.getWidth() * zoom), (int) (this.getHeight() * zoom));
            //g.setColor(_mapFrame.getMapView().getBackground());
            //g.fill(rect);

            _mapFrame.getMapView().paintGraphics(g, rect, this.tileLoadListener);

            //Draw lon/lat grid labels
            if (_mapFrame.isDrawGridLabel()) {
                List<Extent> extentList = new ArrayList<>();
                Extent maxExtent = new Extent();
                Extent aExtent;
                Dimension aSF;
                g.setColor(_mapFrame.getGridLineColor());
                g.setStroke(new BasicStroke(_mapFrame.getGridLineSize()));
                String drawStr;
                PointF sP = new PointF(0, 0);
                PointF eP = new PointF(0, 0);
                Font font = new Font(_mapFrame.getGridFont().getFontName(), _mapFrame.getGridFont().getStyle(), (int) (_mapFrame.getGridFont().getSize() * zoom));
                g.setFont(font);
                float labX, labY;
                int len = _mapFrame.getTickLineLength();
                int space = len + _mapFrame.getGridLabelShift();
                if (_mapFrame.isInsideTickLine()) {
                    space = _mapFrame.getGridLabelShift();
                }

                for (int i = 0; i < _mapFrame.getMapView().getGridLabels().size(); i++) {
                    GridLabel aGL = _mapFrame.getMapView().getGridLabels().get(i);
                    switch (_mapFrame.getGridLabelPosition()) {
                        case LEFT_BOTTOM:
                            switch (aGL.getLabDirection()) {
                                case East:
                                case North:
                                    continue;
                            }
                            break;
                        case LEFT_UP:
                            switch (aGL.getLabDirection()) {
                                case East:
                                case South:
                                    continue;
                            }
                            break;
                        case RIGHT_BOTTOM:
                            switch (aGL.getLabDirection()) {
                                case Weast:
                                case North:
                                    continue;
                            }
                            break;
                        case RIGHT_UP:
                            switch (aGL.getLabDirection()) {
                                case Weast:
                                case South:
                                    continue;
                            }
                            break;
                    }

                    labX = (float) aGL.getLabPoint().X;
                    labY = (float) aGL.getLabPoint().Y;
                    labX = labX + this.getLeft() * zoom + pageLocation.X;
                    labY = labY + this.getTop() * zoom + pageLocation.Y;
                    sP.X = labX;
                    sP.Y = labY;

                    drawStr = aGL.getLabString();
                    if (_drawDegreeSymbol) {
                        if (drawStr.endsWith("E") || drawStr.endsWith("W") || drawStr.endsWith("N") || drawStr.endsWith("S")) {
                            drawStr = drawStr.substring(0, drawStr.length() - 1) + String.valueOf((char) 186) + drawStr.substring(drawStr.length() - 1);
                        } else {
                            drawStr = drawStr + String.valueOf((char) 186);
                        }
                    }
                    FontMetrics metrics = g.getFontMetrics(font);
                    aSF = new Dimension(metrics.stringWidth(drawStr), metrics.getHeight());
                    switch (aGL.getLabDirection()) {
                        case South:
                            labX = labX - aSF.width / 2;
                            labY = labY + aSF.height * 3 / 4 + space;
                            eP.X = sP.X;
                            if (_mapFrame.isInsideTickLine()) {
                                eP.Y = sP.Y - len;
                            } else {
                                eP.Y = sP.Y + len;
                            }
                            break;
                        case Weast:
                            labX = labX - aSF.width - space;
                            labY = labY + aSF.height / 3;
                            eP.Y = sP.Y;
                            if (_mapFrame.isInsideTickLine()) {
                                eP.X = sP.X + len;
                            } else {
                                eP.X = sP.X - len;
                            }
                            break;
                        case North:
                            labX = labX - aSF.width / 2;
                            //labY = labY - aSF.height / 3 - space;
                            labY = labY - space;
                            eP.X = sP.X;
                            if (_mapFrame.isInsideTickLine()) {
                                eP.Y = sP.Y + len;
                            } else {
                                eP.Y = sP.Y - len;
                            }
                            break;
                        case East:
                            labX = labX + space;
                            labY = labY + aSF.height / 3;
                            eP.Y = sP.Y;
                            if (_mapFrame.isInsideTickLine()) {
                                eP.X = sP.X - len;
                            } else {
                                eP.X = sP.X + len;
                            }
                            break;
                    }

                    boolean ifDraw = true;
                    float aSize = aSF.width / 2;
                    float bSize = aSF.height / 2;
                    aExtent = new Extent();
                    aExtent.minX = labX;
                    aExtent.maxX = labX + aSF.width;
                    aExtent.minY = labY - aSF.height;
                    aExtent.maxY = labY;

                    //Judge extent                                        
                    if (extentList.isEmpty()) {
                        maxExtent = (Extent) aExtent.clone();
                        extentList.add((Extent) aExtent.clone());
                    } else {
                        if (!MIMath.isExtentCross(aExtent, maxExtent)) {
                            extentList.add((Extent) aExtent.clone());
                            maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                        } else {
                            for (int j = 0; j < extentList.size(); j++) {
                                if (MIMath.isExtentCross(aExtent, extentList.get(j))) {
                                    ifDraw = false;
                                    break;
                                }
                            }
                            if (ifDraw) {
                                extentList.add(aExtent);
                                maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                            }
                        }
                    }

                    if (ifDraw) {
                        g.setColor(_mapFrame.getGridLineColor());
                        g.draw(new Line2D.Float(sP.X, sP.Y, eP.X, eP.Y));
                        g.setColor(this.getForeColor());
                        g.drawString(drawStr, labX, labY);
                    }
                }
            }

            //Draw neat line
            if (_mapFrame.isDrawNeatLine()) {
                g.setColor(_mapFrame.getNeatLineColor());
                g.setStroke(new BasicStroke(_mapFrame.getNeatLineSize()));
                g.draw(rect);
            }
        }
    }

    @Override
    public void moveUpdate() {
    }

    @Override
    public void resizeUpdate() {
    }
    // </editor-fold>   
    // <editor-fold desc="BeanInfo">

    public class LayoutMapBean {

        LayoutMapBean() {
        }
        // <editor-fold desc="Get Set Methods">

        /**
         * Get left
         *
         * @return Left
         */
        public int getLeft() {
            return _mapFrame.getLayoutBounds().x;
        }

        /**
         * Set left
         *
         * @param left Left
         */
        public void setLeft(int left) {
            _mapFrame.setLayoutBounds(new Rectangle(left, _mapFrame.getLayoutBounds().y, _mapFrame.getLayoutBounds().width,
                    _mapFrame.getLayoutBounds().height));
        }

        /**
         * Get top
         *
         * @return Top
         */
        public int getTop() {
            return _mapFrame.getLayoutBounds().y;
        }

        /**
         * Set top
         *
         * @param top Top
         */
        public void setTop(int top) {
            _mapFrame.setLayoutBounds(new Rectangle(_mapFrame.getLayoutBounds().x, top, _mapFrame.getLayoutBounds().width,
                    _mapFrame.getLayoutBounds().height));
        }

        /**
         * Get width
         *
         * @return Width
         */
        public int getWidth() {
            return _mapFrame.getLayoutBounds().width;
        }

        /**
         * Set width
         *
         * @param width Width
         */
        public void setWidth(int width) {
            _mapFrame.setLayoutBounds(new Rectangle(_mapFrame.getLayoutBounds().x, _mapFrame.getLayoutBounds().y, width,
                    _mapFrame.getLayoutBounds().height));
        }

        /**
         * Get height
         *
         * @return Height
         */
        public int getHeight() {
            return _mapFrame.getLayoutBounds().height;
        }

        /**
         * Set height
         *
         * @param height Height
         */
        public void setHeight(int height) {
            _mapFrame.setLayoutBounds(new Rectangle(_mapFrame.getLayoutBounds().x, _mapFrame.getLayoutBounds().y,
                    _mapFrame.getLayoutBounds().width, height));
        }

        /**
         * Get bounds rectangle
         *
         * @return The bounds rectangle
         */
        public Rectangle getBounds() {
            return _mapFrame.getLayoutBounds();
        }

        /**
         * Set bounds rectangle
         *
         * @param rect Bounds rectangle
         */
        public void setBounds(Rectangle rect) {
            _mapFrame.setLayoutBounds(rect);
        }

        /**
         * Get is draw backcolor
         *
         * @return Boolean
         */
        public boolean isDrawBackColor() {
            return LayoutMap.this.isDrawBackColor();
        }

        /**
         * Set is draw backcolor
         *
         * @param value Boolean
         */
        public void setDrawBackColor(boolean value) {
            LayoutMap.this.setDrawBackColor(value);
        }

        /**
         * Get background color
         *
         * @return Background color
         */
        public Color getBackColor() {
            return _mapFrame.getBackColor();
        }

        /**
         * Set background color
         *
         * @param color Background color
         */
        public void setBackColor(Color color) {
            _mapFrame.setBackColor(color);
        }

        /**
         * Get foreground color
         *
         * @return Foreground color
         */
        public Color getForeColor() {
            return _mapFrame.getForeColor();
        }

        /**
         * Set foreground color
         *
         * @param color
         */
        public void setForeColor(Color color) {
            _mapFrame.setForeColor(color);
        }

        /**
         * Get if draw map view neat line
         *
         * @return Boolean
         */
        public boolean isDrawNeatLine() {
            return _mapFrame.isDrawNeatLine();
        }

        /**
         * Set if draw map view neat line
         *
         * @param istrue Boolean
         */
        public void setDrawNeatLine(boolean istrue) {
            _mapFrame.setDrawNeatLine(istrue);
        }

        /**
         * Get map view neat line color
         *
         * @return Neat line color
         */
        public Color getNeatLineColor() {
            return _mapFrame.getNeatLineColor();
        }

        /**
         * Set map view neat line color
         *
         * @param color Neat line color
         */
        public void setNeatLineColor(Color color) {
            _mapFrame.setNeatLineColor(color);
        }

        /**
         * Get map view neat line size
         *
         * @return Neat line size
         */
        public float getNeatLineSize() {
            return _mapFrame.getNeatLineSize();
        }

        /**
         * Set map view neat line size
         *
         * @param size Neat line size
         */
        public void setNeatLineSize(float size) {
            _mapFrame.setNeatLineSize(size);
        }

        /**
         * Get grid line color
         *
         * @return Grid line color
         */
        public Color getGridLineColor() {
            return _mapFrame.getGridLineColor();
        }

        /**
         * Set grid line color
         *
         * @param color Grid line color
         */
        public void setGridLineColor(Color color) {
            _mapFrame.setGridLineColor(color);
        }

        /**
         * Get grid line size
         *
         * @return Grid line size
         */
        public float getGridLineSize() {
            return _mapFrame.getGridLineSize();
        }

        /**
         * Set grid line size
         *
         * @param size Grid line size
         */
        public void setGridLineSize(float size) {
            _mapFrame.setGridLineSize(size);
        }

        /**
         * Get grid line style
         *
         * @return Grid line style
         */
        public String getGridLineStyle() {
            return _mapFrame.getGridLineStyle().toString();
        }

        /**
         * Set grid line style
         *
         * @param style Grid line style
         */
        public void setGridLineStyle(String style) {
            _mapFrame.setGridLineStyle(LineStyles.valueOf(style));
        }

        /**
         * Get if draw grid labels
         *
         * @return If draw grid labels
         */
        public boolean isDrawGridLabel() {
            return _mapFrame.isDrawGridLabel();
        }

        /**
         * Set if draw grid labels
         *
         * @param istrue Boolean
         */
        public void setDrawGridLabel(boolean istrue) {
            _mapFrame.setDrawGridLabel(istrue);
        }

        /**
         * Get if draw grid tick line inside
         *
         * @return Booelan
         */
        public boolean isInsideTickLine() {
            return _mapFrame.isInsideTickLine();
        }

        /**
         * Set if draw grid tick line inside
         *
         * @param istrue Boolean
         */
        public void setInsideTickLine(boolean istrue) {
            _mapFrame.setInsideTickLine(istrue);
        }

        /**
         * Get grid tick line length
         *
         * @return Grid tick line length
         */
        public int getTickLineLength() {
            return _mapFrame.getTickLineLength();
        }

        /**
         * Set grid tick line length
         *
         * @param value tick line length
         */
        public void setTickLineLength(int value) {
            _mapFrame.setTickLineLength(value);
        }

        /**
         * Get grid label shift
         *
         * @return Grid label shift
         */
        public int getGridLabelShift() {
            return _mapFrame.getGridLabelShift();
        }

        /**
         * Set grid label shift
         *
         * @param value Grid label shift
         */
        public void setGridLabelShift(int value) {
            _mapFrame.setGridLabelShift(value);
        }

        /// <summary>
        /// Get or set grid label position
        /**
         * Get grid label position
         *
         * @return Grid label position
         */
        public String getGridLabelPosition() {
            return _mapFrame.getGridLabelPosition().toString();
        }

        /**
         * Set grid label positiont
         *
         * @param value Grid label position
         */
        public void setGridLabelPosition(String value) {
            _mapFrame.setGridLabelPosition(GridLabelPosition.valueOf(value));
        }

        /**
         * Get if draw grid line
         *
         * @return If draw grid line
         */
        public boolean isDrawGridLine() {
            return _mapFrame.isDrawGridLine();
        }

        /**
         * Set if draw grid line
         *
         * @param istrue If draw grid line
         */
        public void setDrawGridLine(boolean istrue) {
            _mapFrame.setDrawGridLine(istrue);
        }

        /**
         * Get if draw grid tick line
         *
         * @return Boolean
         */
        public boolean isDrawGridTickLine() {
            return _mapFrame.isDrawGridTickLine();
        }

        /**
         * Set if draw grid tick line
         *
         * @param istrue Boolean
         */
        public void setDrawGridTickLine(boolean istrue) {
            _mapFrame.setDrawGridTickLine(istrue);
        }

        /**
         * Get if draw degree symbol
         *
         * @return Boolean
         */
        public boolean isDrawDegreeSymbol() {
            return _drawDegreeSymbol;
        }

        /**
         * Set if draw degree symbol
         *
         * @param value Boolean
         */
        public void setDrawDegreeSymbol(boolean value) {
            _drawDegreeSymbol = value;
        }

        /**
         * Get grid label font
         *
         * @return Grid label font
         */
        public Font getGridFont() {
            return _mapFrame.getGridFont();
        }

        /**
         * Set grid label font
         *
         * @param font Grid label font
         */
        public void setGridFont(Font font) {
            _mapFrame.setGridFont(font);
        }

        /**
         * Get grid x delt
         *
         * @return Grid x delt
         */
        public double getGridXDelt() {
            return _mapFrame.getGridXDelt();
        }

        /**
         * Set grid x delt
         *
         * @param value The value
         */
        public void setGridXDelt(double value) {
            _mapFrame.setGridXDelt(value);
        }

        /**
         * Get grid y delt
         *
         * @return Grid y delt
         */
        public double getGridYDelt() {
            return _mapFrame.getGridYDelt();
        }

        /**
         * Set grid y delt
         *
         * @param value Grid y delt
         */
        public void setGridYDelt(double value) {
            _mapFrame.setGridYDelt(value);
        }

        /**
         * Get grid x origin
         *
         * @return Grid x origin
         */
        public float getGridXOrigin() {
            return _mapFrame.getGridXOrigin();
        }

        /**
         * Set grid x origin
         *
         * @param value Grid x origin
         */
        public void setGridXOrigin(float value) {
            _mapFrame.setGridXOrigin(value);
        }

        /**
         * Get grid y origin
         *
         * @return Grid y origin
         */
        public float getGridYOrigin() {
            return _mapFrame.getGridYOrigin();
        }

        /**
         * Set grid y origin
         *
         * @param value Grid y origin
         */
        public void setGridYOrigin(float value) {
            _mapFrame.setGridYOrigin(value);
        }
        // </editor-fold>
    }

    public static class LayoutMapBeanBeanInfo extends BaseBeanInfo {

        public LayoutMapBeanBeanInfo() {
            super(LayoutMapBean.class);
            addProperty("drawBackColor").setCategory("General").setDisplayName("Draw Background");
            addProperty("backColor").setCategory("General").setDisplayName("Background");
            addProperty("foreColor").setCategory("General").setDisplayName("Foreground");
            addProperty("drawNeatLine").setCategory("Neat Line").setDisplayName("Draw Neat Line");
            addProperty("neatLineColor").setCategory("Neat Line").setDisplayName("Neat Line Color");
            addProperty("neatLineSize").setCategory("Neat Line").setDisplayName("Neat Line Size");
            addProperty("drawGridLine").setCategory("Grid Line").setDisplayName("Draw Grid Line");
            addProperty("drawGridLabel").setCategory("Grid Line").setDisplayName("Draw Grid Label");
            addProperty("gridXDelt").setCategory("Grid Line").setDisplayName("Grid X Interval");
            addProperty("gridYDelt").setCategory("Grid Line").setDisplayName("Grid Y Interval");
            addProperty("gridXOrigin").setCategory("Grid Line").setDisplayName("Grid X Origin");
            addProperty("gridYOrigin").setCategory("Grid Line").setDisplayName("Grid Y Origin");
            addProperty("gridFont").setCategory("Grid Line").setDisplayName("Grid Label Font");
            addProperty("gridLabelShift").setCategory("Grid Line").setDisplayName("Grid Label Shift");
            ExtendedPropertyDescriptor e = addProperty("gridLabelPosition");
            e.setCategory("Grid Line").setDisplayName("Grid Label Position");
            e.setPropertyEditorClass(GridLabelPositionEditor.class);
            addProperty("drawDegreeSymbol").setCategory("Grid Line").setDisplayName("Draw Degree Symbol");
            addProperty("gridLineColor").setCategory("Grid Line").setDisplayName("Grid Line Color");
            addProperty("gridLineSize").setCategory("Grid Line").setDisplayName("Grid Line Size");
            e = addProperty("gridLineStyle");
            e.setCategory("Grid Line").setDisplayName("Grid Line Style");
            e.setPropertyEditorClass(LineStyleEditor.class);
            addProperty("insideTickLine").setCategory("Grid Line").setDisplayName("Inside Tick Line");
            addProperty("tickLineLength").setCategory("Grid Line").setDisplayName("Tick Line Length");
            addProperty("left").setCategory("Location").setDisplayName("Left");
            addProperty("top").setCategory("Location").setDisplayName("Top");
            addProperty("width").setCategory("Location").setDisplayName("Width");
            addProperty("height").setCategory("Location").setDisplayName("Height");
        }
    }

    public static class GridLabelPositionEditor extends ComboBoxPropertyEditor {

        public GridLabelPositionEditor() {
            super();
            GridLabelPosition[] lutypes = GridLabelPosition.values();
            String[] types = new String[lutypes.length];
            int i = 0;
            for (GridLabelPosition type : lutypes) {
                types[i] = type.toString();
                i += 1;
            }
            setAvailableValues(types);
        }
    }

    public static class LineStyleEditor extends ComboBoxPropertyEditor {

        public LineStyleEditor() {
            super();
            LineStyles[] lutypes = LineStyles.values();
            String[] types = new String[lutypes.length];
            int i = 0;
            for (LineStyles type : lutypes) {
                types[i] = type.toString();
                i += 1;
            }
            setAvailableValues(types);
        }
    }
    // </editor-fold>
}
