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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.meteoinfo.common.*;
import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.geo.drawing.Draw;
import org.meteoinfo.geometry.colors.BoundaryNorm;
import org.meteoinfo.geometry.colors.ExtendType;
import org.meteoinfo.geometry.colors.LogNorm;
import org.meteoinfo.geometry.colors.Normalize;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.ShapeTypes;

/**
 *
 * @author Yaqiang Wang
 */
public class ChartColorBar extends ChartLegend {

    // <editor-fold desc="Variables">
    private List<Double> tickLocations = new ArrayList<>();
    private List<ChartText> tickLabels = new ArrayList<>();
    private boolean autoTick;
    private boolean insideTick;
    private float tickLength;
    private boolean tickVisible;
    private float tickWidth;
    private Color tickColor;
    private boolean drawMinLabel;
    private boolean drawMaxLabel;
    private ExtendType extendType;
    private boolean drawMinorTick;
    protected int minorTickNum;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     *
     * @param ls LegendScheme
     */
    public ChartColorBar(LegendScheme ls) {
        super(ls);

        this.autoTick = true;
        this.insideTick = false;
        this.tickLength = 5;
        this.tickVisible = true;
        this.tickWidth = 1;
        this.tickColor = Color.black;
        this.drawMinLabel = false;
        this.drawMaxLabel = false;
        this.extendType = ExtendType.NEITHER;
        this.drawMinorTick = false;
        this.minorTickNum = 5;
        this.setLegendScheme(ls);
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
        this.tickLabelColor = value;
    }

    /**
     * Set legend scheme
     *
     * @param value Legend scheme
     */
    @Override
    public void setLegendScheme(LegendScheme value) {
        this.legendScheme = value;
        Normalize normalize = this.legendScheme.getNormalize();
        if (normalize != null) {
            double min = normalize.getMinValue();
            double max = normalize.getMaxValue();
            double[] tickValues;
            if (normalize instanceof LogNorm) {
                tickValues = MIMath.getIntervalValues_Log(min, max);
                this.drawMinorTick = true;
            } else {
                tickValues = MIMath.getIntervalValues(min, max);
            }
            this.tickLocations = Arrays.stream(tickValues).boxed().collect(Collectors.toList());
            this.tickLabels = new ArrayList<>();
            if (normalize instanceof LogNorm) {
                for (double v : this.tickLocations) {
                    int e = (int) Math.floor(Math.log10(v));
                    this.tickLabels.add(new ChartText("$10^{" + String.valueOf(e) + "}$"));
                }
            } else {
                for (double v : this.tickLocations) {
                    this.tickLabels.add(new ChartText(DataConvert.removeTailingZeros(String.valueOf(v))));
                }
            }
        }
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

    /**
     * Get extend type
     * @return Extend type
     */
    public ExtendType getExtendType() {
        return this.extendType;
    }

    /**
     * Set extend type
     * @param value Extend type
     */
    public void setExtendType(ExtendType value) {
        this.extendType = value;
    }

    /**
     * Set extend type
     * @param value Extend type string
     */
    public void setExtendType(String value) {
        this.extendType = ExtendType.valueOf(value.toUpperCase());
    }

    /**
     * Get whether draw minor ticks
     * @return Whether draw minor ticks
     */
    public boolean isDrawMinorTick() {
        return this.drawMinorTick;
    }

    /**
     * Set whether draw minor ticks
     * @param value Whether draw minor ticks
     */
    public void setDrawMinorTick(boolean value) {
        this.drawMinorTick = value;
    }

    /**
     * Get minor tick number
     *
     * @return Minor tick number
     */
    public int getMinorTickNum() {
        return this.minorTickNum;
    }

    /**
     * Set minor tick number
     *
     * @param value Minor tick number
     */
    public void setMinorTickNum(int value) {
        this.minorTickNum = value;
    }

    // </editor-fold>
    // <editor-fold desc="Method">
    @Override
    protected int getTickWidth(Graphics2D g) {
        if (this.tickLabels.isEmpty()) {
            return super.getTickWidth(g);
        } else {
            float tWidth = 0;
            g.setFont(this.tickLabelFont);
            for (ChartText ct : this.tickLabels) {
                float labWidth = (float) Draw.getStringDimension(ct.getText(), this.tickLabelAngle, g).getWidth();
                if (tWidth < labWidth)
                    tWidth = labWidth;
            }
            return (int) tWidth;
        }
    }

    @Override
    protected int getTickHeight(Graphics2D g) {
        if (this.tickLabels.isEmpty()) {
            return super.getTickHeight(g);
        } else {
            float tHeight = 0;
            g.setFont(this.tickLabelFont);
            for (ChartText ct : this.tickLabels) {
                float labHeight = (float) Draw.getStringDimension(ct.getText(), 90 - Math.abs(this.tickLabelAngle), g).getWidth();
                if (tHeight < labHeight)
                    tHeight = labHeight;
            }
            return (int) tHeight;
        }
    }

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
                if (legendScheme.getColorMap() != null)
                    this.drawHorizontal(g, legendScheme);
                else
                    this.drawHorizontalBarLegend(g, legendScheme);
                break;
            case VERTICAL:
                if (legendScheme.getColorMap() != null)
                    this.drawVertical(g, legendScheme);
                else
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

    private void drawHorizontal(Graphics2D g, LegendScheme ls) {
        PointF aP = new PointF(0, 0);
        PointF sP = new PointF(0, 0);
        String caption;
        ColorMap colorMap = ls.getColorMap();
        Normalize normalize = ls.getNormalize();

        int bNum = colorMap.getColorCount();
        if (normalize instanceof BoundaryNorm) {
            bNum = ((BoundaryNorm) normalize).getNRegions();
        }

        this.barHeight = (float) this.legendWidth / this.aspect;
        float minMaxWidth = this.legendWidth;
        float x_shift = 0;
        switch (this.extendType) {
            case MIN:
                minMaxWidth -= barHeight;
                x_shift += barHeight;
                break;
            case MAX:
                minMaxWidth -= barHeight;
                break;
            case BOTH:
                minMaxWidth -= barHeight * 2;
                x_shift += barHeight;
                break;
        }
        barWidth = minMaxWidth / bNum;
        float y_shift = 0;
        if (this.label != null){
            switch (this.labelLocation){
                case "top":
                case "in":
                    y_shift = this.label.getDimension(g).height + 5;
                    break;
            }
        }

        //Draw color polygons
        aP.Y = y_shift;
        Color[] colors = colorMap.getColors(bNum);
        switch (this.extendType) {
            case MIN:
            case BOTH:
                g.setColor(colors[0]);
                Path2D p = new Path2D.Float();
                p.moveTo(aP.X, aP.Y + barHeight / 2);
                p.lineTo(aP.X + barHeight, aP.Y);
                p.lineTo(aP.X + barHeight, aP.Y + barHeight);
                p.lineTo(aP.X, aP.Y + barHeight / 2);
                p.closePath();
                g.fill(p);
                aP.X += barHeight;
                break;
        }
        for (int i = 0; i < bNum; i++) {
            g.setColor(colors[i]);
            Rectangle2D rect;
            if (i == bNum - 1)
                rect = new Rectangle2D.Float(aP.X - 1, aP.Y, barWidth + 2, barHeight);
            else
                rect = new Rectangle2D.Float(aP.X - 1, aP.Y, barWidth + 1, barHeight);
            g.fill(rect);
            aP.X += barWidth;
        }
        switch (this.extendType) {
            case MAX:
            case BOTH:
                g.setColor(colors[bNum - 1]);
                Path2D p = new Path2D.Float();
                p.moveTo(aP.X, aP.Y);
                p.lineTo(aP.X, aP.Y + barHeight);
                p.lineTo(aP.X + barHeight, aP.Y + barHeight / 2);
                p.lineTo(aP.X, aP.Y);
                p.closePath();
                g.fill(p);
                break;
        }

        //Draw neatline
        g.setStroke(new BasicStroke(this.neatLineSize));
        g.setColor(this.neatLineColor);
        switch (this.extendType) {
            case NEITHER:
                g.draw(new Rectangle.Double(0, y_shift, this.barWidth * bNum, this.barHeight));
                break;
            case BOTH:
                Path2D p = new Path2D.Float();
                p.moveTo(0, this.barHeight / 2 + y_shift);
                p.lineTo(this.barHeight, y_shift);
                p.lineTo(this.legendWidth - barHeight, y_shift);
                p.lineTo(this.legendWidth, this.barHeight / 2 + y_shift);
                p.lineTo(this.legendWidth - barHeight, this.barHeight + y_shift);
                p.lineTo(this.barHeight, this.barHeight + y_shift);
                p.closePath();
                g.draw(p);
                break;
            case MIN:
                p = new Path2D.Float();
                p.moveTo(0, this.barHeight / 2 + y_shift);
                p.lineTo(this.barHeight, y_shift);
                p.lineTo(this.legendWidth, y_shift);
                p.lineTo(this.legendWidth, this.barHeight + y_shift);
                p.lineTo(this.barHeight, this.barHeight + y_shift);
                p.lineTo(0, this.barHeight / 2 + y_shift);
                p.closePath();
                g.draw(p);
                break;
            case MAX:
                p = new Path2D.Float();
                p.moveTo(0, y_shift);
                p.lineTo(0, this.barHeight + y_shift);
                p.lineTo(this.legendWidth - barHeight, this.barHeight + y_shift);
                p.lineTo(this.legendWidth, this.barHeight / 2 + y_shift);
                p.lineTo(this.legendWidth - barHeight, y_shift);
                p.lineTo(0, y_shift);
                p.closePath();
                g.draw(p);
                break;
        }

        //Draw tick and label
        float tickLen = this.tickLength;
        if (this.insideTick) {
            if (this.barHeight < tickLen) {
                tickLen = (int) this.barHeight;
            }
        }
        g.setStroke(new BasicStroke(this.tickWidth));
        g.setFont(tickLabelFont);
        g.setColor(this.tickColor);
        aP.Y = barHeight + y_shift;
        for (int i = 0; i < this.tickLocations.size(); i++) {
            sP.X = x_shift + minMaxWidth * normalize.apply(this.tickLocations.get(i)).floatValue();
            sP.Y = aP.Y;
            g.setColor(this.tickColor);
            this.drawTickLine(g, sP, tickLen, true, 0);
            String label = this.tickLabels.get(i).getText();
            g.setColor(this.tickLabelColor);
            if (this.tickLabelAngle == 0) {
                Draw.drawString(g, sP.X, sP.Y, label, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
            } else if (this.tickLabelAngle < 45) {
                Draw.drawString(g, sP.X, sP.Y, label, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
            } else {
                Draw.drawString(g, sP.X, sP.Y, label, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
            }

            //Draw minor tick lines
            if (this.drawMinorTick) {
                if (i == this.tickLocations.size() - 1) {
                    continue;
                }
                float minorTickLen = tickLen - 2;
                double v1 = this.tickLocations.get(i);
                double v2 = this.tickLocations.get(i + 1);
                double step = (v2 - v1) / this.minorTickNum;
                double v;
                g.setColor(this.tickColor);
                for (int j = 1; j < this.minorTickNum; j++) {
                    v = v1 + step * j;
                    sP.X = x_shift + minMaxWidth * normalize.apply(v).floatValue();
                    sP.Y = aP.Y;
                    this.drawTickLine(g, sP, minorTickLen, true, 0);
                }
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
                    sy = this.barHeight * 0.5;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.LEFT, YAlign.CENTER, label.isUseExternalFont());
                    break;
                case "left":
                    sx = -5;
                    sy = this.barHeight * 0.5;
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
            if (aLS.getLegendType() == LegendType.UNIQUE_VALUE) {
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
                if (i == bNum - 1) {
                    if (cb.getStartValue().equals(cb.getEndValue()))
                        continue;
                }
                double v = Double.parseDouble(cb.getEndValue().toString());
                if (this.tickLocations.contains(v)) {
                    labelIdxs.add(i);
                    tickIdx = this.tickLocations.indexOf(v);
                    tLabels.add(this.tickLabels.get(tickIdx).getText());
                }
            }
        }

        this.barHeight = (float) this.legendWidth / this.aspect;
        barWidth = (float) this.legendWidth / bNum;
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
                case POINT:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPB.isDrawShape();
                    DrawFill = aPB.isDrawFill();
                    FillColor = aPB.getColor();
                    break;
                case POLYLINE:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPLB.isDrawPolyline();
                    FillColor = aPLB.getColor();
                    break;
                case POLYGON:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPGB.isDrawShape();
                    DrawFill = aPGB.isDrawFill();
                    FillColor = aPGB.getColor();
                    break;
                case IMAGE:
                    ColorBreak aCB = aLS.getLegendBreaks().get(idx);
                    DrawShape = true;
                    DrawFill = true;
                    FillColor = aCB.getColor();
                    break;
            }                      

            if (DrawShape) {
                if (this.extendRect) {
                    if (aLS.getShapeType() == ShapeTypes.POLYGON) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol(aP.X, aP.Y, barWidth, barHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol(aP.X, aP.Y, FillColor, OutlineColor, barWidth,
                                barHeight, DrawFill, DrawOutline, g);
                    }
                } else {
                    double extendw = barWidth;
                    if (this.autoExtendFrac) {
                        extendw = barHeight;
                    }
                    if (i == 0) {
                        PointD[] Points = new PointD[4];
                        Points[0] = new PointD();
                        Points[0].X = barWidth - extendw;
                        Points[0].Y = aP.Y + barHeight * 0.5;
                        Points[1] = new PointD();
                        Points[1].X = barWidth;
                        Points[1].Y = aP.Y;
                        Points[2] = new PointD();
                        Points[2].X = barWidth;
                        Points[2].Y = aP.Y + barHeight;
                        Points[3] = new PointD();
                        Points[3].X = barWidth - extendw;
                        Points[3].Y = aP.Y + barHeight * 0.5;
                        if (aLS.getShapeType() == ShapeTypes.POLYGON) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                            aPGB.setDrawOutline(false);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (i == bNum - 1) {
                        PointD[] Points = new PointD[4];
                        Points[0] = new PointD();
                        Points[0].X = i * barWidth - 1.0f;
                        Points[0].Y = aP.Y + barHeight;
                        Points[1] = new PointD();
                        Points[1].X = i * barWidth - 1.0f;
                        Points[1].Y = aP.Y;
                        Points[2] = new PointD();
                        Points[2].X = i * barWidth + extendw;
                        Points[2].Y = aP.Y + barHeight * 0.5;
                        Points[3] = new PointD();
                        Points[3].X = i * barWidth - 1.0f;
                        Points[3].Y = aP.Y + barHeight;
                        if (aLS.getShapeType() == ShapeTypes.POLYGON) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                            aPGB.setDrawOutline(false);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (aLS.getShapeType() == ShapeTypes.POLYGON) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol(aP.X, aP.Y, barWidth, barHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol(aP.X, aP.Y, FillColor, OutlineColor, barWidth,
                                barHeight, DrawFill, DrawOutline, g);
                    }
                }
            }
            aP.X += barWidth;
        }
        //Draw neatline
        g.setStroke(new BasicStroke(this.neatLineSize));
        g.setColor(this.neatLineColor);
        if (this.extendRect) {
            g.draw(new Rectangle.Double(0, y_shift, this.barWidth * bNum, this.barHeight));
        } else {
            double extendw = barWidth;
            if (this.autoExtendFrac) {
                extendw = barHeight;
            }
            Path2D p = new Path2D.Double();
            p.moveTo(barWidth - extendw, this.barHeight / 2 + y_shift);
            p.lineTo(this.barWidth, y_shift);
            p.lineTo(this.barWidth * (bNum - 1), y_shift);
            p.lineTo(this.barWidth * (bNum - 1) + extendw, this.barHeight / 2 + y_shift);
            p.lineTo(this.barWidth * (bNum - 1), this.barHeight + y_shift);
            p.lineTo(this.barWidth, this.barHeight + y_shift);
            p.closePath();
            g.draw(p);
        }
        //Draw tick and label
        aP.X = -barWidth / 2;
        float tickLen = this.tickLength;
        if (this.insideTick) {
            if (this.barHeight < tickLen) {
                tickLen = (int) this.barHeight;
            }
        }
        g.setStroke(new BasicStroke(this.tickWidth));
        g.setFont(tickLabelFont);
        g.setColor(this.tickColor);
        idx = 0;
        for (int i = 0; i < bNum; i++) {
            aP.X += barWidth;
            aP.Y = barHeight / 2 + y_shift;
            if (labelIdxs.contains(i)) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                if (this.autoTick) {
                    if (aLS.getLegendType() == LegendType.UNIQUE_VALUE) {
                        caption = cb.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                    }
                } else {
                    caption = tLabels.get(idx);
                }
                
                if (aLS.getLegendType() == LegendType.UNIQUE_VALUE) {
                    sP.X = aP.X;
                    sP.Y = aP.Y + barHeight / 2 + 5;
                    g.setColor(this.tickLabelColor);
                    if (this.tickLabelAngle == 0) {
                        Draw.drawString(g, sP.X, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                    } else if (this.tickLabelAngle < 45) {
                        Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                    } else {
                        Draw.drawString(g, sP.X, sP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
                    }
                } else {
                    sP.X = aP.X + barWidth / 2;
                    sP.Y = aP.Y + barHeight / 2;
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
                                this.drawTickLine(g, ssP, tickLen, true, -this.barWidth);
                                caption = DataConvert.removeTailingZeros(cb.getStartValue().toString());
                                g.setColor(this.tickLabelColor);
                                //Draw.drawString(g, ssP.X - this.barWidth, ssP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                                if (this.tickLabelAngle == 0) {
                                    Draw.drawString(g, ssP.X - this.barWidth, ssP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                                } else if (this.tickLabelAngle < 45) {
                                    Draw.drawString(g, ssP.X - this.barWidth, ssP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                                } else {
                                    Draw.drawString(g, ssP.X - this.barWidth, ssP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
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
                            this.drawTickLine(g, sP, tickLen, true, -this.barWidth);
                            g.setColor(this.tickLabelColor);
                            //Draw.drawString(g, sP.X - this.barWidth, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            if (this.tickLabelAngle == 0) {
                                Draw.drawString(g, sP.X - this.barWidth, sP.Y, caption, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                            } else if (this.tickLabelAngle < 45) {
                                Draw.drawString(g, sP.X - this.barWidth, sP.Y, caption, XAlign.RIGHT, YAlign.TOP, this.tickLabelAngle, true);
                            } else {
                                Draw.drawString(g, sP.X - this.barWidth, sP.Y, caption, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
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
                    sy = this.barHeight * 0.5;
                    Draw.drawString(g, sx, sy, label.getText(), XAlign.LEFT, YAlign.CENTER, label.isUseExternalFont());
                    break;
                case "left":
                    sx = -5;
                    sy = this.barHeight * 0.5;
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

    private void drawVertical(Graphics2D g, LegendScheme ls) {
        PointF aP = new PointF(0, 0);
        PointF sP = new PointF(0, 0);
        String caption;
        ColorMap colorMap = ls.getColorMap();
        Normalize normalize = ls.getNormalize();

        int bNum = colorMap.getColorCount();
        if (normalize instanceof BoundaryNorm) {
            bNum = ((BoundaryNorm) normalize).getNRegions();
        }

        this.barWidth = (float) this.legendHeight / this.aspect;
        float minMaxHeight = this.legendHeight;
        float y_shift = 0;
        switch (this.extendType) {
            case MIN:
                minMaxHeight -= barWidth;
                y_shift += barWidth;
                break;
            case MAX:
                minMaxHeight -= barWidth;
                break;
            case BOTH:
                minMaxHeight -= barWidth * 2;
                y_shift += barWidth;
                break;
        }
        barHeight = minMaxHeight / bNum;
        float x_shift = 0;
        if (this.label != null){
            switch (this.labelLocation){
                case "left":
                case "in":
                    x_shift = this.label.getDimension(g).height + 5;
                    break;
            }
        }

        //Draw color polygons
        aP.X = x_shift;
        aP.Y = legendHeight;
        Color[] colors = colorMap.getColors(bNum);
        switch (this.extendType) {
            case MIN:
            case BOTH:
                g.setColor(colors[0]);
                Path2D p = new Path2D.Float();
                p.moveTo(aP.X + barWidth / 2, aP.Y);
                p.lineTo(aP.X + barWidth, aP.Y - barWidth);
                p.lineTo(aP.X, aP.Y - barWidth);
                p.lineTo(aP.X + barWidth / 2, aP.Y);
                p.closePath();
                g.fill(p);
                aP.Y -= barWidth;
                break;
        }
        for (int i = 0; i < bNum; i++) {
            aP.Y -= barHeight;
            g.setColor(colors[i]);
            Rectangle2D rect;
            if (i == bNum - 1)
                rect = new Rectangle2D.Float(aP.X, aP.Y - 1, barWidth, barHeight + 2);
            else
                rect = new Rectangle2D.Float(aP.X, aP.Y, barWidth, barHeight + 1);
            g.fill(rect);
        }
        switch (this.extendType) {
            case MAX:
            case BOTH:
                g.setColor(colors[bNum - 1]);
                Path2D p = new Path2D.Float();
                p.moveTo(aP.X, aP.Y);
                p.lineTo(aP.X + barWidth, aP.Y);
                p.lineTo(aP.X + barWidth / 2, aP.Y - barWidth);
                p.lineTo(aP.X, aP.Y);
                p.closePath();
                g.fill(p);
                break;
        }

        //Draw neatline
        g.setStroke(new BasicStroke(this.neatLineSize));
        g.setColor(this.neatLineColor);
        switch (this.extendType) {
            case NEITHER:
                g.draw(new Rectangle.Double(x_shift, 0, this.barWidth, this.legendHeight));
                break;
            case BOTH:
                Path2D p = new Path2D.Double();
                p.moveTo(this.barWidth / 2 + x_shift, 0);
                p.lineTo(x_shift, this.barWidth);
                p.lineTo(x_shift, this.legendHeight - barWidth);
                p.lineTo(this.barWidth / 2 + x_shift, legendHeight);
                p.lineTo(this.barWidth + x_shift, legendHeight - barWidth);
                p.lineTo(this.barWidth + x_shift, this.barWidth);
                p.closePath();
                g.draw(p);
                break;
            case MIN:
                p = new Path2D.Double();
                p.moveTo(x_shift, 0);
                p.lineTo(this.barWidth + x_shift, 0);
                p.lineTo(this.barWidth + x_shift, this.legendHeight - barWidth);
                p.lineTo(this.barWidth / 2 + x_shift, legendHeight);
                p.lineTo(x_shift, legendHeight - barWidth);
                p.lineTo(x_shift, 0);
                p.closePath();
                g.draw(p);
                break;
            case MAX:
                p = new Path2D.Double();
                p.moveTo(this.barWidth / 2 + x_shift, 0);
                p.lineTo(x_shift, this.barWidth);
                p.lineTo(x_shift, this.legendHeight);
                p.lineTo(this.barWidth + x_shift, legendHeight);
                p.lineTo(this.barWidth + x_shift, this.barWidth);
                p.lineTo(this.barWidth / 2 + x_shift, 0);
                p.closePath();
                g.draw(p);
                break;
        }

        //Draw tick and label
        float tickLen = this.tickLength;
        if (this.insideTick) {
            if (this.barWidth < tickLen) {
                tickLen = (int) this.barWidth;
            }
        }
        g.setStroke(new BasicStroke(this.tickWidth));
        g.setFont(tickLabelFont);
        g.setColor(this.tickColor);
        aP.X = barWidth + x_shift;
        for (int i = 0; i < this.tickLocations.size(); i++) {
            sP.X = aP.X;
            sP.Y = this.legendHeight - y_shift - minMaxHeight * normalize.apply(this.tickLocations.get(i)).floatValue();
            g.setColor(this.tickColor);
            this.drawTickLine(g, sP, tickLen, false, 0);
            String label = this.tickLabels.get(i).getText();
            g.setColor(this.tickLabelColor);
            Draw.drawString(g, sP.X, sP.Y, label, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);

            //Draw minor tick lines
            if (this.drawMinorTick) {
                if (i == this.tickLocations.size() - 1) {
                    continue;
                }
                float minorTickLen = tickLen - 2;
                double v1 = this.tickLocations.get(i);
                double v2 = this.tickLocations.get(i + 1);
                double step = (v2 - v1) / this.minorTickNum;
                double v;
                g.setColor(this.tickColor);
                for (int j = 1; j < this.minorTickNum; j++) {
                    v = v1 + step * j;
                    sP.X = aP.X;
                    sP.Y = this.legendHeight - y_shift - minMaxHeight * normalize.apply(v).floatValue();
                    this.drawTickLine(g, sP, minorTickLen, false, 0);
                }
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
            if (aLS.getLegendType() == LegendType.UNIQUE_VALUE) {
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
                if (i == bNum - 1) {
                    if (cb.getStartValue().equals(cb.getEndValue()))
                        continue;
                }
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

        this.barWidth = (float) this.legendHeight / this.aspect;
        barHeight = (float) this.legendHeight / bNum;
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
                case POINT:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPB.isDrawShape();
                    DrawFill = aPB.isDrawFill();
                    FillColor = aPB.getColor();
                    break;
                case POLYLINE:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPLB.isDrawPolyline();
                    FillColor = aPLB.getColor();
                    break;
                case POLYGON:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPGB.isDrawShape();
                    DrawFill = aPGB.isDrawFill();
                    FillColor = aPGB.getColor();
                    break;
                case IMAGE:
                    ColorBreak aCB = aLS.getLegendBreaks().get(idx);
                    DrawShape = true;
                    DrawFill = true;
                    FillColor = aCB.getColor();
                    break;
            }
            
            aP.Y = aP.Y - barHeight;

            if (DrawShape) {
                if (this.extendRect) {
                    if (aLS.getShapeType() == ShapeTypes.POLYGON) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol(aP.X, aP.Y, barWidth, barHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol(aP.X, aP.Y, FillColor, OutlineColor, barWidth,
                                barHeight, DrawFill, DrawOutline, g);
                    }
                } else if (i == 0) {
                    PointD[] Points = new PointD[4];
                    Points[0] = new PointD();
                    Points[0].X = aP.X + barWidth * 0.5;
                    Points[0].Y = this.legendHeight;
                    Points[1] = new PointD();
                    Points[1].X = aP.X;
                    Points[1].Y = aP.Y;
                    Points[2] = new PointD();
                    Points[2].X = aP.X + barWidth;
                    Points[2].Y = aP.Y;
                    Points[3] = new PointD();
                    Points[3].X = aP.X + barWidth * 0.5;
                    Points[3].Y = this.legendHeight;
                    if (aLS.getShapeType() == ShapeTypes.POLYGON) {
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
                    Points[0].Y = barHeight;
                    Points[1] = new PointD();
                    Points[1].X = aP.X + barWidth;
                    Points[1].Y = barHeight;
                    Points[2] = new PointD();
                    Points[2].X = aP.X + barWidth * 0.5;
                    Points[2].Y = 0;
                    Points[3] = new PointD();
                    Points[3].X = aP.X;
                    Points[3].Y = barHeight;
                    if (aLS.getShapeType() == ShapeTypes.POLYGON) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygon(Points, aPGB, g);
                    } else {
                        Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                    }
                } else if (aLS.getShapeType() == ShapeTypes.POLYGON) {
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                    aPGB.setDrawOutline(false);
                    Draw.drawPolygonSymbol(aP.X, aP.Y, barWidth, barHeight, aPGB, g);
                } else {
                    Draw.drawPolygonSymbol(aP.X, aP.Y, FillColor, OutlineColor, barWidth,
                            barHeight, DrawFill, DrawOutline, g);
                }
            }
        }
        //Draw neatline
        g.setStroke(new BasicStroke(this.neatLineSize));
        g.setColor(this.neatLineColor);
        if (this.extendRect) {
            g.draw(new Rectangle.Double(x_shift, 0, this.barWidth, this.barHeight * bNum));
        } else {
            Path2D p = new Path2D.Double();
            p.moveTo(this.barWidth / 2 + x_shift, 0);
            p.lineTo(x_shift, this.barHeight);
            p.lineTo(x_shift, (this.barHeight * (bNum - 1)));
            p.lineTo(this.barWidth / 2 + x_shift, this.barHeight * bNum);
            p.lineTo(this.barWidth + x_shift, this.barHeight * (bNum - 1));
            p.lineTo(this.barWidth + x_shift, this.barHeight);
            p.closePath();
            g.draw(p);
        }
        //Draw ticks
        g.setStroke(new BasicStroke(this.tickWidth));
        aP.Y = this.legendHeight + barHeight / 2;
        float tickLen = this.tickLength;
        if (this.insideTick) {
            if (this.barWidth < tickLen) {
                tickLen = (int) this.barWidth;
            }
        }
        g.setFont(tickLabelFont);
        idx = 0;
        for (int i = 0; i < bNum; i++) {
            aP.X = barWidth / 2 + x_shift;
            aP.Y = aP.Y - barHeight;
            if (labelIdxs.contains(i)) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                if (this.autoTick) {
                    if (aLS.getLegendType() == LegendType.UNIQUE_VALUE) {
                        caption = cb.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                    }
                } else {
                    caption = tLabels.get(idx);
                }

                if (aLS.getLegendType() == LegendType.UNIQUE_VALUE) {
                    sP.X = aP.X + barWidth / 2 + 5;
                    sP.Y = aP.Y;
                    g.setColor(this.tickLabelColor);
                    Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                } else {
                    sP.X = aP.X + barWidth / 2;
                    sP.Y = aP.Y - barHeight / 2;
                    PointD ssP = (PointD)sP.clone();
                    if (this.autoTick) {
                        if (i < bNum - 1) {
                            g.setColor(this.tickColor);
                            this.drawTickLine(g, sP, tickLen, false, 0);
                            g.setColor(this.tickLabelColor);
                            Draw.drawString(g, sP.X, sP.Y, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                            if (this.drawMinLabel && i == 0) {
                                g.setColor(this.tickColor);
                                this.drawTickLine(g, ssP, tickLen, false, this.barHeight);
                                caption = DataConvert.removeTailingZeros(cb.getStartValue().toString());
                                g.setColor(this.tickLabelColor);
                                Draw.drawString(g, ssP.X, ssP.Y + this.barHeight, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
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
                            this.drawTickLine(g, sP, tickLen, false, this.barHeight);
                            g.setColor(this.tickLabelColor);
                            Draw.drawString(g, sP.X, sP.Y + this.barHeight, caption, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
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
                                this.width += dim.height + this.labelShift;
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
                                this.height += dim.height + this.labelShift;
                                break;
                        }
                    }
            }
        }

        return new Dimension(this.width, this.height);
    }
    // </editor-fold>
}
