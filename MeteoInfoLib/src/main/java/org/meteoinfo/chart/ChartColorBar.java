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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.plot.XAlign;
import org.meteoinfo.chart.plot.YAlign;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.PointD;
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
    private float tickLength;
    private boolean tickVisible;
    private float tickWidth;
    private Color tickColor;
    private boolean drawMinLabel;
    private boolean drawMaxLabel;

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
        this.tickVisible = true;
        this.tickWidth = 1;
        this.tickColor = Color.black;
        this.drawMinLabel = false;
        this.drawMaxLabel = false;
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
    public float getTickLength() {
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

    /**
     * Get if is tick visible
     * @return Boolean
     */
    public boolean isTickVisible() {
        return this.tickVisible;
    }

    /**
     * Set if is tick visible
     * @param value Boolean
     */
    public void setTickVisible(boolean value) {
        this.tickVisible = value;
    }

    /**
     * Get tick line width
     * @return Tick line width
     */
    public float getTickWidth() {
        return this.tickWidth;
    }

    /**
     * Set tick line width
     * @param value Tick line width
     */
    public void setTickWidth(float value) {
        this.tickWidth = value;
    }

    /**
     * Get tick color
     * @return Tick color
     */
    public Color getTickColor() {
        return this.tickColor;
    }

    /**
     * Set tick color
     * @param value Tick color
     */
    public void setTickColor(Color value) {
        this.tickColor = value;
    }

    /**
     * Get if draw minimum value label
     *
     * @return Boolean
     */
    public boolean isDrawMinLabel() {
        return this.drawMinLabel;
    }

    /**
     * Set if draw minimum value label
     *
     * @param value Boolean
     */
    public void setDrawMinLabel(boolean value) {
        this.drawMinLabel = value;
    }

    /**
     * Get if draw maximum value label
     *
     * @return Boolean
     */
    public boolean isDrawMaxLabel() {
        return this.drawMaxLabel;
    }

    /**
     * Set if draw maximum value label
     *
     * @param value Boolean
     */
    public void setDrawMaxLabel(boolean value) {
        this.drawMaxLabel = value;
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

    private void drawTickLine(Graphics2D g, PointF sP, float tickLen, boolean vertical, float shift) {
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
    
    private void drawTickLine(Graphics2D g, PointD sP, float tickLen, boolean vertical, double shift) {
        if (vertical) {
            if (this.insideTick) {
                g.draw(new Line2D.Double(sP.X + shift, sP.Y, sP.X + shift, sP.Y - tickLen));
            } else {
                g.draw(new Line2D.Double(sP.X + shift, sP.Y, sP.X + shift, sP.Y + tickLen));
                sP.Y += tickLen;
            }
            sP.Y += 5;
        } else {
            if (this.insideTick) {
                g.draw(new Line2D.Double(sP.X - tickLen, sP.Y + shift, sP.X, sP.Y + shift));
            } else {
                g.draw(new Line2D.Double(sP.X, sP.Y + shift, sP.X + tickLen, sP.Y + shift));
                sP.X += tickLen;
            }
            sP.X += 5;
        }
    }

    private void drawHorizontalBarLegend(Graphics2D g, LegendScheme aLS) {
        PointD aP = new PointD(0, 0);
        PointD sP = new PointD(0, 0);
        boolean DrawShape = true, DrawFill = true, DrawOutline = false;
        Color FillColor = Color.red, OutlineColor = Color.black;
        String caption;

        int bNum = aLS.getBreakNum();
        if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
            bNum -= 1;
        }

        List<Integer> labelIdxs = new ArrayList<>();
        List<String> tLabels = new ArrayList<>();
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
            int tickIdx;
            for (int i = 0; i < bNum; i++) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                double v = Double.parseDouble(cb.getEndValue().toString());
                if (this.tickLocations.contains(v)) {
                    labelIdxs.add(i);
                    tickIdx = this.tickLocations.indexOf(v);
                    tLabels.add(this.tickLabels.get(tickIdx).getText());
                }
            }
        }

        this._hBarHeight = (double) this.legendWidth / this.aspect;
        _vBarWidth = (double) this.legendWidth / bNum;
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
        aP.Y = y_shift;
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

            if (DrawShape) {
                if (this.extendRect) {
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol(aP.X, aP.Y, _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol(aP.X, aP.Y, FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                } else {
                    double extendw = _vBarWidth;
                    if (this.autoExtendFrac) {
                        extendw = _hBarHeight;
                    }
                    if (i == 0) {
                        PointD[] Points = new PointD[4];
                        Points[0] = new PointD();
                        Points[0].X = _vBarWidth - extendw;
                        Points[0].Y = aP.Y + _hBarHeight * 0.5;
                        Points[1] = new PointD();
                        Points[1].X = _vBarWidth;
                        Points[1].Y = aP.Y;
                        Points[2] = new PointD();
                        Points[2].X = _vBarWidth;
                        Points[2].Y = aP.Y + _hBarHeight;
                        Points[3] = new PointD();
                        Points[3].X = _vBarWidth - extendw;
                        Points[3].Y = aP.Y + _hBarHeight * 0.5;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                            aPGB.setDrawOutline(false);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (i == bNum - 1) {
                        PointD[] Points = new PointD[4];
                        Points[0] = new PointD();
                        Points[0].X = i * _vBarWidth - 1.0f;
                        Points[0].Y = aP.Y + _hBarHeight;
                        Points[1] = new PointD();
                        Points[1].X = i * _vBarWidth - 1.0f;
                        Points[1].Y = aP.Y;
                        Points[2] = new PointD();
                        Points[2].X = i * _vBarWidth + extendw;
                        Points[2].Y = aP.Y + _hBarHeight * 0.5;
                        Points[3] = new PointD();
                        Points[3].X = i * _vBarWidth - 1.0f;
                        Points[3].Y = aP.Y + _hBarHeight;
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
                        Draw.drawPolygonSymbol(aP.X, aP.Y, _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol(aP.X, aP.Y, FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                }
            }
            aP.X += _vBarWidth;  
        }
        //Draw neatline
        g.setStroke(new BasicStroke(this.neatLineSize));
        g.setColor(this.neatLineColor);
        if (this.extendRect) {
            g.draw(new Rectangle.Double(0, y_shift, this._vBarWidth * bNum, this._hBarHeight));
        } else {
            double extendw = _vBarWidth;
            if (this.autoExtendFrac) {
                extendw = _hBarHeight;
            }
            Path2D p = new Path2D.Double();
            p.moveTo(_vBarWidth - extendw, this._hBarHeight / 2 + y_shift);
            p.lineTo(this._vBarWidth, y_shift);
            p.lineTo(this._vBarWidth * (bNum - 1), y_shift);
            p.lineTo(this._vBarWidth * (bNum - 1) + extendw, this._hBarHeight / 2 + y_shift);
            p.lineTo(this._vBarWidth * (bNum - 1), this._hBarHeight + y_shift);
            p.lineTo(this._vBarWidth, this._hBarHeight + y_shift);
            p.closePath();
            g.draw(p);
        }
        //Draw tick and label
        aP.X = -_vBarWidth / 2;
        float tickLen = this.tickLength;
        if (this.insideTick) {
            if (this._hBarHeight < tickLen) {
                tickLen = (int) this._hBarHeight;
            }
        }
        g.setStroke(new BasicStroke(this.tickWidth));
        g.setFont(tickLabelFont);
        g.setColor(this.tickColor);
        idx = 0;
        for (int i = 0; i < bNum; i++) {
            aP.X += _vBarWidth;
            aP.Y = _hBarHeight / 2 + y_shift;
            if (labelIdxs.contains(i)) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                if (this.autoTick) {
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = cb.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                    }
                } else {
                    caption = tLabels.get(idx);
                }
                
                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    sP.X = aP.X;
                    sP.Y = aP.Y + _hBarHeight / 2 + 5;
                    g.setColor(this.tickLabelColor);
                    if (this.tickLabelAngle == 0) {
                        Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                    } else if (this.tickLabelAngle < 45) {
                        Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                    } else {
                        Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
                    }
                } else {
                    sP.X = aP.X + _vBarWidth / 2;
                    sP.Y = aP.Y + _hBarHeight / 2;
                    PointD ssP = (PointD)sP.clone();
                    if (this.autoTick) {
                        if (i < bNum - 1) {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, true, 0);
                            g.setColor(this.tickLabelColor);
                            if (this.tickLabelAngle == 0) {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            } else if (this.tickLabelAngle < 45) {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                            } else {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
                            }
                            if (this.drawMinLabel && i == 0) {
                                g.setColor(this.tickColor);
                                this.drawTickLine(g, ssP, tickLen, true, -this._vBarWidth);
                                caption = DataConvert.removeTailingZeros(cb.getStartValue().toString());
                                g.setColor(this.tickLabelColor);
                                //Draw.drawString(g, ssP.X - this._vBarWidth, ssP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                                if (this.tickLabelAngle == 0) {
                                    Draw.drawString(g, ssP.X - this._vBarWidth, ssP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                                } else if (this.tickLabelAngle < 45) {
                                    Draw.drawString(g, ssP.X - this._vBarWidth, ssP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                                } else {
                                    Draw.drawString(g, ssP.X - this._vBarWidth, ssP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
                                }
                            }
                        } else if (this.drawMaxLabel) {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, true, 0);
                            g.setColor(this.tickLabelColor);
                            if (this.tickLabelAngle == 0) {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            } else if (this.tickLabelAngle < 45) {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                            } else {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
                            }
                        }
                    } else {
                        if (i == 0 && this.tickLocations.get(idx) == Double.parseDouble(cb.getStartValue().toString())) {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, true, -this._vBarWidth);
                            g.setColor(this.tickLabelColor);
                            //Draw.drawString(g, sP.X - this._vBarWidth, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            if (this.tickLabelAngle == 0) {
                                Draw.drawString(g, sP.X - this._vBarWidth, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            } else if (this.tickLabelAngle < 45) {
                                Draw.drawString(g, sP.X - this._vBarWidth, sP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                            } else {
                                Draw.drawString(g, sP.X - this._vBarWidth, sP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
                            }
                        } else {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, true, 0);
                            g.setColor(this.tickLabelColor);
                            if (this.tickLabelAngle == 0) {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            } else if (this.tickLabelAngle < 45) {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                            } else {
                                Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
                            }
                        }
                    }
                }
                idx += 1;
            }
        }

        //Draw label
        double sx, sy;
        if (this.label != null) {
            g.setFont(this.label.getFont());
            g.setColor(this.label.getColor());
            switch (this.labelLocation) {
                case "top":
                case "in":
                    sx = this.legendWidth * 0.5;
                    sy = 2;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.CENTER, YAlign.TOP, label.isUseExternalFont());
                    break;
                case "right":
                    sx = this.legendWidth + 5;
                    sy = this._hBarHeight * 0.5;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.LEFT, YAlign.CENTER, label.isUseExternalFont());
                    break;
                case "left":
                    sx = -5;
                    sy = this._hBarHeight * 0.5;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.RIGHT, YAlign.CENTER, label.isUseExternalFont());
                    break;
                default:
                    sx = this.legendWidth * 0.5;
                    sy = this.height - 2;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.CENTER, YAlign.BOTTOM, label.isUseExternalFont());
                    break;
            }
        }
    }

    private void drawVerticalBarLegend(Graphics2D g, LegendScheme aLS) {
        PointD aP = new PointD(0, 0);
        PointD sP = new PointD(0, 0);
        boolean DrawShape = true, DrawFill = true, DrawOutline = false;
        Color FillColor = Color.red, OutlineColor = Color.black;
        String caption;

        int bNum = aLS.getBreakNum();
        if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
            bNum -= 1;
        }

        List<Integer> labelIdxs = new ArrayList<>();
        List<String> tLabels = new ArrayList<>();
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
            int tickIdx;
            for (int i = 0; i < bNum; i++) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                double v = Double.parseDouble(cb.getEndValue().toString());
                if (this.tickLocations.contains(v)) {
                    labelIdxs.add(i);
                    tickIdx = this.tickLocations.indexOf(v);
                    tLabels.add(this.tickLabels.get(tickIdx).getText());
                }
                if (i == 0) {
                    v = Double.parseDouble(cb.getStartValue().toString());
                    if (this.tickLocations.contains(v)) {
                        labelIdxs.add(i);
                        tickIdx = this.tickLocations.indexOf(v);
                        tLabels.add(this.tickLabels.get(tickIdx).getText());
                    }
                }
            }
        }

        this._vBarWidth = (double) this.legendHeight / this.aspect;
        _hBarHeight = (double) this.legendHeight / bNum;
        aP.Y = this.legendHeight;
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
        aP.X = x_shift;
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
            
            aP.Y = aP.Y - _hBarHeight;

            if (DrawShape) {
                if (this.extendRect) {
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol(aP.X, aP.Y, _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol(aP.X, aP.Y, FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                } else if (i == 0) {
                    PointD[] Points = new PointD[4];
                    Points[0] = new PointD();
                    Points[0].X = aP.X + _vBarWidth * 0.5;
                    Points[0].Y = this.legendHeight;
                    Points[1] = new PointD();
                    Points[1].X = aP.X;
                    Points[1].Y = aP.Y;
                    Points[2] = new PointD();
                    Points[2].X = aP.X + _vBarWidth;
                    Points[2].Y = aP.Y;
                    Points[3] = new PointD();
                    Points[3].X = aP.X + _vBarWidth * 0.5;
                    Points[3].Y = this.legendHeight;
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygon(Points, aPGB, g);
                    } else {
                        Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                    }
                } else if (i == bNum - 1) {
                    PointD[] Points = new PointD[4];
                    Points[0] = new PointD();
                    Points[0].X = aP.X;
                    Points[0].Y = _hBarHeight;
                    Points[1] = new PointD();
                    Points[1].X = aP.X + _vBarWidth;
                    Points[1].Y = _hBarHeight;
                    Points[2] = new PointD();
                    Points[2].X = aP.X + _vBarWidth * 0.5;
                    Points[2].Y = 0;
                    Points[3] = new PointD();
                    Points[3].X = aP.X;
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
                    Draw.drawPolygonSymbol(aP.X, aP.Y, _vBarWidth, _hBarHeight, aPGB, g);
                } else {
                    Draw.drawPolygonSymbol(aP.X, aP.Y, FillColor, OutlineColor, _vBarWidth,
                            _hBarHeight, DrawFill, DrawOutline, g);
                }
            }
        }
        //Draw neatline
        g.setStroke(new BasicStroke(this.neatLineSize));
        g.setColor(this.neatLineColor);
        if (this.extendRect) {
            g.draw(new Rectangle.Double(x_shift, 0, this._vBarWidth, this._hBarHeight * bNum));
        } else {
            Path2D p = new Path2D.Double();
            p.moveTo(this._vBarWidth / 2 + x_shift, 0);
            p.lineTo(x_shift, this._hBarHeight);
            p.lineTo(x_shift, (this._hBarHeight * (bNum - 1)));
            p.lineTo(this._vBarWidth / 2 + x_shift, this._hBarHeight * bNum);
            p.lineTo(this._vBarWidth + x_shift, this._hBarHeight * (bNum - 1));
            p.lineTo(this._vBarWidth + x_shift, this._hBarHeight);
            p.closePath();
            g.draw(p);
        }
        //Draw ticks
        g.setStroke(new BasicStroke(this.tickWidth));
        aP.Y = this.legendHeight + _hBarHeight / 2;
        float tickLen = this.tickLength;
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
                if (this.autoTick) {
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = cb.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                    }
                } else {
                    caption = tLabels.get(idx);
                }

                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    sP.X = aP.X + _vBarWidth / 2 + 5;
                    sP.Y = aP.Y;
                    g.setColor(this.tickLabelColor);
                    Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                } else {
                    sP.X = aP.X + _vBarWidth / 2;
                    sP.Y = aP.Y - _hBarHeight / 2;
                    PointD ssP = (PointD)sP.clone();
                    if (this.autoTick) {
                        if (i < bNum - 1) {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, false, 0);
                            g.setColor(this.tickLabelColor);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                            if (this.drawMinLabel && i == 0) {
                                g.setColor(this.tickColor);
                                this.drawTickLine(g, ssP, tickLen, false, this._hBarHeight);
                                caption = DataConvert.removeTailingZeros(cb.getStartValue().toString());
                                g.setColor(this.tickLabelColor);
                                Draw.drawString(g, ssP.X, ssP.Y + this._hBarHeight, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                            }
                        } else if (this.drawMaxLabel) {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, false, 0);
                            g.setColor(this.tickLabelColor);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                        }
                    } else {
                        if (i == 0 && this.tickLocations.get(idx) == Double.parseDouble(cb.getStartValue().toString())) {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, false, this._hBarHeight);
                            g.setColor(this.tickLabelColor);
                            Draw.drawString(g, sP.X, sP.Y + this._hBarHeight, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                        } else {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, false, 0);
                            g.setColor(this.tickLabelColor);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                        }
                    }
                }
                idx += 1;
            }
        }
        //Draw label
        double sx, sy;
        if (this.label != null) {
            g.setFont(this.label.getFont());
            g.setColor(this.label.getColor());
            Dimension dim = Draw.getStringDimension(this.label.getText(), g);
            switch (this.labelLocation) {
                case "top":
                    sx = 0;
                    sy = -5;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.LEFT, YAlign.BOTTOM, label.isUseExternalFont());
                    break;
                case "bottom":
                    sx = 0;
                    sy = this.legendHeight + 5;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.LEFT, YAlign.TOP, label.isUseExternalFont());
                    break;
                case "left":
                case "in":
                    sx = 0;
                    sy = this.legendHeight * 0.5;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.CENTER, YAlign.TOP, 90, label.isUseExternalFont());
                    break;
                default:
                    sx = this.width - dim.height;
                    sy = this.legendHeight * 0.5;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.CENTER, YAlign.TOP, 90, label.isUseExternalFont());
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
