/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.plot.XAlign;
import org.meteoinfo.chart.plot.YAlign;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author Yaqiang Wang
 */
public class ChartColorBar extends ChartLegend {

    // <editor-fold desc="Variables">
    private List<Double> tickLocations;
    private List<ChartText> tickLabels;
    private boolean autoTick;
    private boolean insideTick;
    private int tickLength;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     *
     * @param ls LegendScheme
     */
    public ChartColorBar(LegendScheme ls) {
        super(ls);

        this.tickLocations = new ArrayList<>();
        this.tickLabels = new ArrayList<>();
        this.autoTick = true;
        this.insideTick = true;
        this.tickLength = 5;
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Tick locations
     *
     * @return Tick locations
     */
    public List<Double> getTickLocations() {
        return this.tickLocations;
    }

    /**
     * Set tick locations
     *
     * @param value Tick locations
     */
    public void setTickLocations(List<Number> value) {
        this.tickLocations.clear();
        this.tickLabels.clear();
        for (Number v : value) {
            this.tickLocations.add(v.doubleValue());
            this.tickLabels.add(new ChartText(DataConvert.removeTailingZeros(String.valueOf(v))));
        }
        this.autoTick = false;
    }

    /**
     * Set tick locations
     *
     * @param value Tick locations
     */
    public void setTickLocations(double[] value) {
        this.tickLocations.clear();
        this.tickLabels.clear();
        for (double v : value) {
            this.tickLocations.add(v);
            this.tickLabels.add(new ChartText(DataConvert.removeTailingZeros(String.valueOf(v))));
        }
        this.autoTick = false;
    }

    /**
     * Get tick labels
     *
     * @return Tick labels
     */
    public List<ChartText> getTickLabels() {
        return this.tickLabels;
    }

    /**
     * Get tick label text
     *
     * @return Tick label text
     */
    public List<String> getTickLabelText() {
        List<String> strs = new ArrayList<>();
        for (ChartText ct : this.tickLabels) {
            strs.add(ct.toString());
        }

        return strs;
    }

    /**
     * Set tick label text
     *
     * @param value Tick label text
     */
    public void setTickLabelText(List<String> value) {
        this.tickLabels = new ArrayList<>();
        for (String v : value) {
            this.tickLabels.add(new ChartText(v));
        }
        this.autoTick = false;
    }

    /**
     * Set tick labels.
     *
     * @param value Tick labels
     */
    public void setTickLabels(List<ChartText> value) {
        this.tickLabels = value;
    }

    /**
     * Set tick labels
     *
     * @param value Tick labels
     */
    public void setTickLabels_Number(List<Number> value) {
        this.tickLabels = new ArrayList<>();
        for (Number v : value) {
            this.tickLabels.add(new ChartText(v.toString()));
        }
        this.autoTick = false;
    }

    /**
     * Get if is auto tick labels
     *
     * @return Boolean
     */
    public boolean isAutoTick() {
        return this.autoTick;
    }

    /**
     * Set if auto tick labels
     *
     * @param value Boolean
     */
    public void setAutoTick(boolean value) {
        this.autoTick = value;
    }

    /**
     * Get tick length
     *
     * @return Tick length
     */
    public int getTickLength() {
        return this.tickLength;
    }

    /**
     * Set tick length
     *
     * @param value Tick length
     */
    public void setTickLength(int value) {
        this.tickLength = value;
    }

    /**
     * Get if is inside tick
     *
     * @return Boolean
     */
    public boolean isInsideTick() {
        return this.insideTick;
    }

    /**
     * Set if is inside tick
     *
     * @param value Boolean
     */
    public void setInsideTick(boolean value) {
        this.insideTick = value;
    }
    
    // </editor-fold>
    // <editor-fold desc="Method">
    /**
     * Draw legend
     *
     * @param g Graphics2D
     * @param point Start point
     */
    @Override
    public void draw(Graphics2D g, PointF point) {

        AffineTransform oldMatrix = g.getTransform();
        g.translate(point.X + this.xshift, point.Y + this.yshift);

        //Draw background color
        if (this.drawBackground) {
            g.setColor(this.background);
            g.fill(new Rectangle.Float(0, 0, this.width, this.height));
        }

        //Draw legend
        g.setStroke(new BasicStroke(1));
        switch (this.orientation) {
            case HORIZONTAL:
                this.drawHorizontalBarLegend(g, legendScheme);
                break;
            case VERTICAL:
                this.drawVerticalBarLegend(g, legendScheme);
                break;
        }

        //Draw neatline
        if (drawNeatLine) {
            Rectangle.Float mapRect = new Rectangle.Float(0, 0, this.width, this.height);
            g.setColor(neatLineColor);
            g.setStroke(new BasicStroke(neatLineSize));
            g.draw(mapRect);
        }

        g.setTransform(oldMatrix);
    }

    private void drawTickLine(Graphics2D g, PointF sP, int tickLen, boolean vertical, float shift) {
        if (vertical) {
            if (this.insideTick) {
                g.draw(new Line2D.Float(sP.X + shift, sP.Y, sP.X + shift, sP.Y - tickLen));
            } else {
                g.draw(new Line2D.Float(sP.X + shift, sP.Y, sP.X + shift, sP.Y + tickLen));
                sP.Y += tickLen;
            }
            sP.Y += 5;
        } else {
            if (this.insideTick) {
                g.draw(new Line2D.Float(sP.X - tickLen, sP.Y + shift, sP.X, sP.Y + shift));
            } else {
                g.draw(new Line2D.Float(sP.X, sP.Y + shift, sP.X + tickLen, sP.Y + shift));
                sP.X += tickLen;
            }
            sP.X += 5;
        }
    }

    private void drawHorizontalBarLegend(Graphics2D g, LegendScheme aLS) {
        PointF aP = new PointF(0, 0);
        PointF sP = new PointF(0, 0);
        boolean DrawShape = true, DrawFill = true, DrawOutline = false;
        Color FillColor = Color.red, OutlineColor = Color.black;
        String caption;

        int bNum = aLS.getBreakNum();
        if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
            bNum -= 1;
        }

        List<Integer> labelIdxs = new ArrayList<>();
        if (this.autoTick) {
            int tickGap = this.getTickGap(g);
            int sIdx = (bNum % tickGap) / 2;
            int labNum = bNum - 1;
            if (aLS.getLegendType() == LegendType.UniqueValue) {
                labNum += 1;
            } else if (this.drawMinLabel) {
                sIdx = 0;
                labNum = bNum;
            }
            while (sIdx < labNum) {
                labelIdxs.add(sIdx);
                sIdx += tickGap;
            }
        } else {
            for (int i = 0; i < bNum; i++) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                double v = Double.parseDouble(cb.getEndValue().toString());
                if (this.tickLocations.contains(v)) {
                    labelIdxs.add(i);
                } else {
                    if (i == 0){
                        v = Double.parseDouble(cb.getStartValue().toString());
                        if (this.tickLocations.contains(v))
                            labelIdxs.add(i);
                    }
                }
            }
        }

        this._hBarHeight = (float) this.legendWidth / this.aspect;
        _vBarWidth = (float) this.legendWidth / bNum;
        aP.X = -_vBarWidth / 2;
        float y_shift = 0;
        if (this.label != null){
            switch (this.labelLocation){
                case "top":
                case "in":
                    y_shift = this.label.getDimension(g).height + 5;
                    break;
            }
        }
        int idx;
        for (int i = 0; i < bNum; i++) {
            idx = i;
            switch (aLS.getShapeType()) {
                case Point:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPB.isDrawShape();
                    DrawFill = aPB.isDrawFill();
                    FillColor = aPB.getColor();
                    break;
                case Polyline:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPLB.getDrawPolyline();
                    FillColor = aPLB.getColor();
                    break;
                case Polygon:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPGB.isDrawShape();
                    DrawFill = aPGB.isDrawFill();
                    FillColor = aPGB.getColor();
                    break;
                case Image:
                    ColorBreak aCB = aLS.getLegendBreaks().get(idx);
                    DrawShape = true;
                    DrawFill = true;
                    FillColor = aCB.getColor();
                    break;
            }

            aP.X += _vBarWidth;
            aP.Y = _hBarHeight / 2 + y_shift;

            if (DrawShape) {
                if (this.extendRect) {
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol((PointF) aP.clone(), _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                } else {
                    float extendw = _vBarWidth;
                    if (this.autoExtendFrac) {
                        extendw = _hBarHeight;
                    }
                    if (i == 0) {
                        PointF[] Points = new PointF[4];
                        Points[0] = new PointF();
                        Points[0].X = _vBarWidth - extendw;
                        Points[0].Y = aP.Y;
                        Points[1] = new PointF();
                        Points[1].X = _vBarWidth;
                        Points[1].Y = y_shift;
                        Points[2] = new PointF();
                        Points[2].X = _vBarWidth;
                        Points[2].Y = _hBarHeight + y_shift;
                        Points[3] = new PointF();
                        Points[3].X = _vBarWidth - extendw;
                        Points[3].Y = aP.Y;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                            aPGB.setDrawOutline(false);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (i == bNum - 1) {
                        PointF[] Points = new PointF[4];
                        Points[0] = new PointF();
                        Points[0].X = i * _vBarWidth - 1.0f;
                        Points[0].Y = _hBarHeight + y_shift;
                        Points[1] = new PointF();
                        Points[1].X = i * _vBarWidth - 1.0f;
                        Points[1].Y = y_shift;
                        Points[2] = new PointF();
                        Points[2].X = i * _vBarWidth + extendw;
                        Points[2].Y = aP.Y;
                        Points[3] = new PointF();
                        Points[3].X = i * _vBarWidth - 1.0f;
                        Points[3].Y = _hBarHeight + y_shift;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                            aPGB.setDrawOutline(false);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol((PointF) aP.clone(), _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                }
            }
        }
        //Draw neatline
        g.setColor(Color.black);
        if (this.extendRect) {
            g.draw(new Rectangle.Float(0, y_shift, this._vBarWidth * bNum, this._hBarHeight));
        } else {
            float extendw = _vBarWidth;
            if (this.autoExtendFrac) {
                extendw = _hBarHeight;
            }
            Polygon p = new Polygon();
            p.addPoint((int) (_vBarWidth - extendw), (int) (this._hBarHeight / 2 + y_shift));
            p.addPoint((int) this._vBarWidth, (int)y_shift);
            p.addPoint((int) (this._vBarWidth * (bNum - 1)), (int)y_shift);
            p.addPoint((int) (this._vBarWidth * (bNum - 1) + extendw), (int) (this._hBarHeight / 2 + y_shift));
            p.addPoint((int) (this._vBarWidth * (bNum - 1)), (int) (this._hBarHeight + y_shift));
            p.addPoint((int) this._vBarWidth, (int) (this._hBarHeight + y_shift));
            g.drawPolygon(p);
        }
        //Draw tick and label
        aP.X = -_vBarWidth / 2;
        int tickLen = this.tickLength;
        if (this.insideTick) {
            if (this._hBarHeight < tickLen) {
                tickLen = (int) this._hBarHeight;
            }
        }
        g.setFont(tickLabelFont);
        g.setColor(Color.black);
        idx = 0;
        for (int i = 0; i < bNum; i++) {
            aP.X += _vBarWidth;
            aP.Y = _hBarHeight / 2 + y_shift;
            if (labelIdxs.contains(i)) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    caption = cb.getCaption();
                } else {
                    caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                    if (!this.autoTick) {
                        if (this.tickLabels.size() > idx) {
                            caption = this.tickLabels.get(idx).getText();
                        }
                    }
                }
                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    sP.X = aP.X;
                    sP.Y = aP.Y + _hBarHeight / 2 + 5;
                    Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                } else {
                    sP.X = aP.X + _vBarWidth / 2;
                    sP.Y = aP.Y + _hBarHeight / 2;
                    PointF ssP = (PointF)sP.clone();
                    if (this.autoTick) {
                        if (i < bNum - 1) {
                            this.drawTickLine(g, sP, tickLen, true, 0);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            if (this.drawMinLabel && i == 0) {
                                this.drawTickLine(g, ssP, tickLen, true, -this._vBarWidth);
                                caption = DataConvert.removeTailingZeros(cb.getStartValue().toString());
                                Draw.drawString(g, ssP.X - this._vBarWidth, ssP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            }
                        } else if (this.drawMaxLabel) {
                            this.drawTickLine(g, sP, tickLen, true, 0);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                        }
                    } else {
                        if (i == 0 && this.tickLocations.get(idx) == Double.parseDouble(cb.getStartValue().toString())) {
                            this.drawTickLine(g, sP, tickLen, true, -this._vBarWidth);
                            Draw.drawString(g, sP.X - this._vBarWidth, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                        } else {
                            this.drawTickLine(g, sP, tickLen, true, 0);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                        }
                    }
                }
                idx += 1;
            }
        }

        //Draw label
        if (this.label != null) {
            g.setFont(this.label.getFont());
            g.setColor(this.label.getColor());
            switch (this.labelLocation) {
                case "top":
                case "in":
                    x = this.legendWidth * 0.5f;
                    y = 2;
                    Draw.drawString(g, x, y, label.getText(), XAlign.CENTER, YAlign.TOP, label.isUseExternalFont());
                    break;
                case "right":
                    x = this.legendWidth + 5;
                    y = this._hBarHeight * 0.5f;
                    Draw.drawString(g, x, y, label.getText(), XAlign.LEFT, YAlign.CENTER, label.isUseExternalFont());
                    break;
                case "left":
                    x = -5;
                    y = this._hBarHeight * 0.5f;
                    Draw.drawString(g, x, y, label.getText(), XAlign.RIGHT, YAlign.CENTER, label.isUseExternalFont());
                    break;
                default:
                    x = this.legendWidth * 0.5f;
                    y = this.height - 2;
                    Draw.drawString(g, x, y, label.getText(), XAlign.CENTER, YAlign.BOTTOM, label.isUseExternalFont());
                    break;
            }
        }
    }

    private void drawVerticalBarLegend(Graphics2D g, LegendScheme aLS) {
        PointF aP = new PointF(0, 0);
        PointF sP = new PointF(0, 0);
        boolean DrawShape = true, DrawFill = true, DrawOutline = false;
        Color FillColor = Color.red, OutlineColor = Color.black;
        String caption;

        int bNum = aLS.getBreakNum();
        if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
            bNum -= 1;
        }

        List<Integer> labelIdxs = new ArrayList<>();
        if (this.autoTick) {
            int tickGap = this.getTickGap(g);
            int sIdx = (bNum % tickGap) / 2;
            int labNum = bNum - 1;
            if (aLS.getLegendType() == LegendType.UniqueValue) {
                labNum += 1;
            } else if (this.drawMinLabel) {
                sIdx = 0;
                labNum = bNum;
            }
            while (sIdx < labNum) {
                labelIdxs.add(sIdx);
                sIdx += tickGap;
            }
        } else {
            for (int i = 0; i < bNum; i++) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                double v = Double.parseDouble(cb.getEndValue().toString());
                if (this.tickLocations.contains(v)) {
                    labelIdxs.add(i);
                } else {
                    if (i == 0){
                        v = Double.parseDouble(cb.getStartValue().toString());
                        if (this.tickLocations.contains(v))
                            labelIdxs.add(i);
                    }
                }
            }
        }

        this._vBarWidth = (float) this.legendHeight / this.aspect;
        _hBarHeight = (float) this.legendHeight / bNum;
        aP.Y = this.legendHeight + _hBarHeight / 2;
        float x_shift = 0;
        if (this.label != null){
            switch (this.labelLocation){
                case "left":
                case "in":
                    x_shift = this.label.getDimension(g).height + 5;
                    break;
            }
        }
        int idx;
        for (int i = 0; i < bNum; i++) {
            idx = i;
            switch (aLS.getShapeType()) {
                case Point:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPB.isDrawShape();
                    DrawFill = aPB.isDrawFill();
                    FillColor = aPB.getColor();
                    break;
                case Polyline:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPLB.getDrawPolyline();
                    FillColor = aPLB.getColor();
                    break;
                case Polygon:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPGB.isDrawShape();
                    DrawFill = aPGB.isDrawFill();
                    FillColor = aPGB.getColor();
                    break;
                case Image:
                    ColorBreak aCB = aLS.getLegendBreaks().get(idx);
                    DrawShape = true;
                    DrawFill = true;
                    FillColor = aCB.getColor();
                    break;
            }

            aP.X = _vBarWidth / 2 + x_shift;
            aP.Y = aP.Y - _hBarHeight;

            if (DrawShape) {
                if (this.extendRect) {
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol((PointF) aP.clone(), _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                } else if (i == 0) {
                    PointF[] Points = new PointF[4];
                    Points[0] = new PointF();
                    Points[0].X = aP.X;
                    Points[0].Y = this.legendHeight;
                    Points[1] = new PointF();
                    Points[1].X = 0;
                    Points[1].Y = aP.Y - _hBarHeight / 2 - 1.0f;
                    Points[2] = new PointF();
                    Points[2].X = _vBarWidth;
                    Points[2].Y = aP.Y - _hBarHeight / 2 - 1.0f;
                    Points[3] = new PointF();
                    Points[3].X = aP.X;
                    Points[3].Y = this.legendHeight;
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygon(Points, aPGB, g);
                    } else {
                        Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                    }
                } else if (i == bNum - 1) {
                    PointF[] Points = new PointF[4];
                    Points[0] = new PointF();
                    Points[0].X = 0;
                    Points[0].Y = _hBarHeight;
                    Points[1] = new PointF();
                    Points[1].X = _vBarWidth;
                    Points[1].Y = _hBarHeight;
                    Points[2] = new PointF();
                    Points[2].X = aP.X;
                    Points[2].Y = 0;
                    Points[3] = new PointF();
                    Points[3].X = 0;
                    Points[3].Y = _hBarHeight;
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygon(Points, aPGB, g);
                    } else {
                        Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                    }
                } else if (aLS.getShapeType() == ShapeTypes.Polygon) {
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                    aPGB.setDrawOutline(false);
                    Draw.drawPolygonSymbol((PointF) aP.clone(), _vBarWidth, _hBarHeight, aPGB, g);
                } else {
                    Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, _vBarWidth,
                            _hBarHeight, DrawFill, DrawOutline, g);
                }
            }
        }
        //Draw neatline
        g.setColor(Color.black);
        if (this.extendRect) {
            g.draw(new Rectangle.Float(x_shift, 0, this._vBarWidth, this._hBarHeight * bNum));
        } else {
            Polygon p = new Polygon();
            p.addPoint((int) (this._vBarWidth / 2 + x_shift), 0);
            p.addPoint((int)x_shift, (int) this._hBarHeight);
            p.addPoint((int)x_shift, (int) (this._hBarHeight * (bNum - 1)));
            p.addPoint((int) (this._vBarWidth / 2 + x_shift), (int) (this._hBarHeight * bNum));
            p.addPoint((int) (this._vBarWidth + x_shift), (int) (this._hBarHeight * (bNum - 1)));
            p.addPoint((int) (this._vBarWidth + x_shift), (int) this._hBarHeight);
            g.drawPolygon(p);
        }
        //Draw ticks
        aP.Y = this.legendHeight + _hBarHeight / 2;
        int tickLen = this.tickLength;
        if (this.insideTick) {
            if (this._vBarWidth < tickLen) {
                tickLen = (int) this._vBarWidth;
            }
        }
        g.setFont(tickLabelFont);
        idx = 0;
        for (int i = 0; i < bNum; i++) {
            aP.X = _vBarWidth / 2 + x_shift;
            aP.Y = aP.Y - _hBarHeight;
            if (labelIdxs.contains(i)) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    caption = cb.getCaption();
                } else {
                    caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                    if (!this.autoTick) {
                        if (this.tickLabels.size() > idx) {
                            caption = this.tickLabels.get(idx).getText();
                        }
                    }
                }

                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    sP.X = aP.X + _vBarWidth / 2 + 5;
                    sP.Y = aP.Y;
                    g.setColor(Color.black);
                    Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                } else {
                    sP.X = aP.X + _vBarWidth / 2;
                    sP.Y = aP.Y - _hBarHeight / 2;
                    PointF ssP = (PointF)sP.clone();
                    if (this.autoTick) {
                        if (i < bNum - 1) {
                            this.drawTickLine(g, sP, tickLen, false, 0);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                            if (this.drawMinLabel && i == 0) {                                
                                this.drawTickLine(g, ssP, tickLen, false, this._hBarHeight);
                                caption = DataConvert.removeTailingZeros(cb.getStartValue().toString());
                                Draw.drawString(g, ssP.X, ssP.Y + this._hBarHeight, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                            }
                        } else if (this.drawMaxLabel) {
                            this.drawTickLine(g, sP, tickLen, false, 0);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                        }
                    } else {
                        if (i == 0 && this.tickLocations.get(idx) == Double.parseDouble(cb.getStartValue().toString())) {
                            this.drawTickLine(g, sP, tickLen, false, this._hBarHeight);
                            Draw.drawString(g, sP.X, sP.Y + this._hBarHeight, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                        } else {
                            this.drawTickLine(g, sP, tickLen, false, 0);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                        }
                    }
                }
                idx += 1;
            }
        }
        //Draw label
        if (this.label != null) {
            g.setFont(this.label.getFont());
            g.setColor(this.label.getColor());
            Dimension dim = Draw.getStringDimension(this.label.getText(), g);
            switch (this.labelLocation) {
                case "top":
                    x = 0;
                    y = -5;
                    Draw.drawString(g, x, y, label.getText(), XAlign.LEFT, YAlign.BOTTOM, label.isUseExternalFont());
                    break;
                case "bottom":
                    x = 0;
                    y = this.legendHeight + 5;
                    Draw.drawString(g, x, y, label.getText(), XAlign.LEFT, YAlign.TOP, label.isUseExternalFont());
                    break;
                case "left":
                case "in":
                    x = 0;
                    y = this.legendHeight * 0.5f;
                    Draw.drawString(g, x, y, label.getText(), XAlign.LEFT, YAlign.CENTER, 90, label.isUseExternalFont());
                    break;
                default:
                    x = this.width - dim.height;
                    y = this.legendHeight * 0.5f;
                    Draw.drawString(g, x, y, label.getText(), XAlign.LEFT, YAlign.CENTER, 90, label.isUseExternalFont());
                    break;
            }
        }
    }

    /**
     * Get legend dimension
     *
     * @param g Graphics2D
     * @param limitDim Limit dimension
     * @return Legend dimension
     */
    @Override
    public Dimension getLegendDimension(Graphics2D g, Dimension limitDim) {
        if (legendScheme != null) {
            switch (this.orientation) {
                case VERTICAL:
                    this.width = (int) (this.getTickWidth(g) + limitDim.height * this.shrink / this.aspect + 5);
                    if (!this.insideTick){
                        this.width += this.tickLength;
                    }
                    this.legendWidth = this.width;
                    this.legendHeight = this.height;
                    if (this.label != null) {
                        g.setFont(this.label.getFont());
                        Dimension dim = Draw.getStringDimension(label.getText(), g);
                        switch (this.labelLocation) {
                            case "top":
                            case "bottom":
                                //this.height += dim.height + 10;
                                this.width = Math.max(this.width, dim.width);
                                break;
                            default:
                                this.width += dim.height + 5;
                                break;
                        }                        
                    }
                    break;
                default:
                    g.setFont(this.tickLabelFont);
                    this.height = (int) (this.getTickHeight(g) + limitDim.width * this.shrink / this.aspect + 5);
                    if (!this.insideTick){
                        this.height += this.tickLength;
                    }
                    this.legendWidth = this.width;
                    this.legendHeight = this.height;
                    if (this.label != null) {
                        g.setFont(this.label.getFont());
                        Dimension dim = Draw.getStringDimension(label.getText(), g);
                        switch (this.labelLocation) {
                            case "right":
                            case "left":
                                //this.width += dim.width + 10;
                                break;
                            default:
                                this.height += dim.height + 5;
                                break;
                        }
                    }
            }
        }

        return new Dimension(this.width, this.height);
    }
    // </editor-fold>
}
