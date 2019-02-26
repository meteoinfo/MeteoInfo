/* This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.plot.MapPlot;
import org.meteoinfo.chart.plot.Plot;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.PointF;

/**
 *
 * @author Yaqiang Wang
 * yaqiang.wang@gmail.com
 */
public class Chart {

    // <editor-fold desc="Variables">
    private List<Plot> plots;
    private int currentPlot;
    private int rowNum;
    private int columnNum;
    private ChartText title;
    private ChartText subTitle;
    private List<ChartText> texts;
    private ChartLegend legend;
    private Color background;
    //private boolean drawBackground;
    private boolean drawLegend;
    private Rectangle2D plotArea;
    private boolean antiAlias;
    private boolean symbolAntialias;
    private ChartPanel parent;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Chart() {
        this.drawLegend = false;
        this.background = Color.white;
        //this.drawBackground = true;
        this.antiAlias = false;
        this.symbolAntialias = true;
        this.rowNum = 1;
        this.columnNum = 1;
        this.plots = new ArrayList<>();
        this.currentPlot = -1;
        this.texts = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param parent ChartPanel parent
     */
    public Chart(ChartPanel parent) {
        this();
        this.parent = parent;
    }

    /**
     * Constructor
     *
     * @param plot Plot
     * @param parent ChartPanel
     */
    public Chart(Plot plot, ChartPanel parent) {
        this(parent);
        this.plots.add(plot);
    }

    /**
     * Constructor
     *
     * @param plot Plot
     */
    public Chart(Plot plot) {
        this(plot, null);
    }

    /**
     * Constructor
     *
     * @param title Title
     * @param plot Plot
     * @param parent ChartPanel
     */
    public Chart(String title, Plot plot, ChartPanel parent) {
        this(plot, parent);
        if (title == null) {
            this.title = null;
        } else {
            this.title = new ChartText(title);
        }
    }

    /**
     * Constructor
     *
     * @param title Title
     * @param plot Plot
     */
    public Chart(String title, Plot plot) {
        this(title, plot, null);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Set ChartPanel parent
     *
     * @param value ChartPanel
     */
    public void setParent(ChartPanel value) {
        this.parent = value;
        for (Plot plot : this.plots) {
            if (plot instanceof MapPlot) {
                ((MapPlot) plot).setParent(value);
            }
        }
    }

    /**
     * Get plot
     *
     * @return Plot
     */
    public List<Plot> getPlots() {
        return plots;
    }

    /**
     * Get current plot
     *
     * @return Current plot
     */
    public Plot getCurrentPlot() {
        if (this.plots.isEmpty()) {
            return null;
        }
        
        if (this.currentPlot < 0 || this.currentPlot >= this.plots.size()) {
            this.currentPlot = this.plots.size() - 1;
        }
        return this.plots.get(this.currentPlot);
    }

    /**
     * Set current plot
     *
     * @param value Current plot
     */
    public void setCurrentPlot(Plot value) {
        if (this.plots.isEmpty()) {
            this.addPlot(value);
            //this.currentPlot = 0;
        } else if (this.currentPlot == -1) {
            this.plots.add(value);
        } else {
            if (this.currentPlot >= this.plots.size()) {
                this.currentPlot = this.plots.size() - 1;
            }
            Plot plot = this.plots.get(this.currentPlot);
            value.isSubPlot = plot.isSubPlot;
            value.columnIndex = plot.columnIndex;
            value.rowIndex = plot.rowIndex;
            this.plots.set(this.currentPlot, value);
        }
    }

    /**
     * Set current plot index
     *
     * @param value Current plot index
     */
    public void setCurrentPlot(int value) {
        this.currentPlot = value;
    }

    /**
     * Get the first plot
     *
     * @return Plot
     */
    public Plot getPlot() {
        if (this.plots.isEmpty()) {
            return null;
        }

        return this.plots.get(0);
    }

    /**
     * Get row number of sub plots
     *
     * @return Row number of sub plots
     */
    public int getRowNum() {
        return this.rowNum;
    }

    /**
     * Set row number of sub plots
     *
     * @param value Row number of sub plots
     */
    public void setRowNum(int value) {
        this.rowNum = value;
    }

    /**
     * Get column number of sub plots
     *
     * @return Column number of sub plots
     */
    public int getColumnNum() {
        return this.columnNum;
    }

    /**
     * Set column number of sub plots
     *
     * @param value Column number of sub plots
     */
    public void setColumnNum(int value) {
        this.columnNum = value;
    }

    /**
     * Get title
     *
     * @return Title
     */
    public ChartText getTitle() {
        return title;
    }

    /**
     * Set title
     *
     * @param value Title
     */
    public void setTitle(ChartText value) {
        title = value;
    }

    /**
     * Get sub title
     *
     * @return Sub title
     */
    public ChartText getSubTitle() {
        return subTitle;
    }

    /**
     * Set sub title
     *
     * @param value Sub title
     */
    public void setSubTitle(ChartText value) {
        subTitle = value;
    }

    /**
     * Get background
     *
     * @return Background
     */
    public Color getBackground() {
        return this.background;
    }

    /**
     * Set background
     *
     * @param value Background
     */
    public void setBackground(Color value) {
        this.background = value;
    }

//    /**
//     * Get if draw background
//     *
//     * @return Boolean
//     */
//    public boolean isDrawBackground() {
//        return this.drawBackground;
//    }

//    /**
//     * Set if draw background
//     *
//     * @param value Boolean
//     */
//    public void setDrawBackground(boolean value) {
//        this.drawBackground = value;
//    }

    /**
     * Get chart legend
     *
     * @return Chart legend
     */
    public ChartLegend getLegend() {
        return this.legend;
    }

    /**
     * Get if draw legend
     *
     * @return If draw legend
     */
    public boolean isDrawLegend() {
        return this.drawLegend;
    }

    /**
     * Set if draw legend
     *
     * @param value Boolean
     */
    public void setDrawLegend(boolean value) {
        this.drawLegend = value;
    }

    /**
     * Get plot area
     *
     * @return Plot area
     */
    public Rectangle2D getPlotArea() {
        return this.plotArea;
    }

    /**
     * Get if is anti-alias
     *
     * @return Boolean
     */
    public boolean isAntiAlias() {
        return this.antiAlias;
    }

    /**
     * Set if is anti-alias
     *
     * @param value Boolean
     */
    public void setAntiAlias(boolean value) {
        this.antiAlias = value;
    }
    
    /**
     * Get symbol antialias
     * @return Boolean
     */
    public boolean isSymbolAntialias() {
        return this.symbolAntialias;
    }
    
    /**
     * Set symbol antialias
     * @param value Boolean
     */
    public void setSymbolAntialias(boolean value) {
        this.symbolAntialias = value;
    }   

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Draw plot
     *
     * @param g Graphics2D
     * @param area Drawing area
     */
    public void draw(Graphics2D g, Rectangle2D area) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
            //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
        }

        AffineTransform oldMatrix = g.getTransform();
        Rectangle oldRegion = g.getClipBounds();
        g.setClip(area);
        g.translate(area.getX(), area.getY());

        //Draw background
        if (this.background != null) {
            g.setColor(background);
            g.fill(new Rectangle2D.Double(0, 0, area.getWidth(), area.getHeight()));
        }

        //Draw title
        float y = 5;
        if (title != null) {
            g.setColor(title.getColor());
            g.setFont(title.getFont());
            float x = (float) area.getWidth() / 2;
            //y -= this.title.getHeight(g) - 5;
            //FontMetrics metrics = g.getFontMetrics(title.getFont());
            //x -= metrics.stringWidth(title.getText()) / 2;
            //y += metrics.getHeight();
            int i = 0;
            for (String text : title.getTexts()) {
                Dimension dim = Draw.getStringDimension(text, g);
                if (i == 0) {
                    y += dim.getHeight();
                }
                Draw.drawString(g, text, x - dim.width / 2, y);
                g.setFont(title.getFont());
                y += dim.height + title.getLineSpace();
                i += 1;
            }
            y += 5;
        }

        //Draw plot
        plotArea = this.getPlotArea(g, area);
        //plotArea = area;
        if (plotArea.getWidth() < 20 || plotArea.getHeight() < 20) {
            g.setTransform(oldMatrix);
            g.setClip(oldRegion);
            return;
        }

        if (this.plots.size() > 0) {
            //double zoom = this.getPositionAreaZoom(g, plotArea);
            //Margin tightInset = this.getPlotsTightInset(g, plotArea);
            Margin shrink = this.getPlotsShrink(g, plotArea);
            for (int i = 0; i < this.plots.size(); i++) {
                Plot plot = this.plots.get(i);
                plot.setSymbolAntialias(this.symbolAntialias);
                if (plot.isOuterPosActive()){
                    if (plot.isSubPlot || plot.isSameShrink()) {
                        plot.setPlotShrink(shrink);
                    } else {
                        //plot.setPlotShrink(this.getPlotShrink(g, area, plot));
                        plot.setPlotShrink(this.getPlotShrink(g, plotArea, plot));
                    }
                }
                if (plot instanceof MapPlot) {
                    ((MapPlot) plot).setAntialias(this.antiAlias);
                }
                plot.draw(g, plotArea);
            }
        }

        //Draw text
        drawText(g, area);

        //Draw legend
        if (this.drawLegend) {
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
            float x = 0;
            switch (this.legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    x = (float) area.getWidth() / 2 - dim.width / 2;
                    y += 5;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    x = (float) area.getWidth() / 2 - dim.width / 2;
                    y += plotArea.getHeight() + 5;
                    break;
                case LEFT_OUTSIDE:
                    x = 10;
                    y = (float) area.getHeight() / 2 - dim.height / 2;
                    break;
                case RIGHT_OUTSIDE:
                    x = (float) plotArea.getWidth() + 10;
                    y = (float) area.getHeight() / 2 - dim.height / 2;
                    break;
            }
            this.legend.draw(g, new PointF(x, y));
        }

        g.setTransform(oldMatrix);
        g.setClip(oldRegion);
    }

    void drawText(Graphics2D g, Rectangle2D area) {
        float x, y;
        for (ChartText text : this.texts) {
            x = (float) (area.getWidth() * text.getX());
            y = (float) (area.getHeight() * (1 - text.getY()));
            Dimension dim = Draw.getStringDimension(text.getText(), g);
            Rectangle.Double rect = new Rectangle.Double(x, y, dim.getWidth(), dim.getHeight());
            if (text.isFill()) {
                g.setColor(text.getBackground());
                g.fill(rect);
            }
            if (text.isDrawNeatline()) {
                g.setColor(text.getNeatlineColor());
                Stroke oldStroke = g.getStroke();
                g.setStroke(new BasicStroke(text.getNeatlineSize()));
                g.draw(rect);
                g.setStroke(oldStroke);
            }
            g.setFont(text.getFont());
            g.setColor(text.getColor());
            Draw.drawString(g, text.getText(), x, y);
        }
    }
    
    private Rectangle2D getPlotArea(Graphics2D g, Rectangle2D area) {
        Rectangle2D pArea = new Rectangle2D.Double();
        int edge = 0;
        int top = edge;
        int left = edge;
        int right = edge;
        int bottom = edge;
        if (this.title != null) {
            top += this.title.getTrueDimension(g).height + 12;
        }        
        pArea.setRect(left, top, area.getWidth() - left - right, area.getHeight() - top - bottom);

        return pArea;
    }

    private Rectangle2D getPlotArea_bak(Graphics2D g, Rectangle2D area) {
        Rectangle2D pArea = new Rectangle2D.Double();
        int edge = 2;
        int top = edge;
        int left = edge;
        int right = edge;
        int bottom = edge;
        if (this.title != null) {
            top += this.title.getTrueDimension(g).height + 10;
        }
        if (this.drawLegend) {
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
            switch (this.legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + 10;
                    break;
            }
        }
        pArea.setRect(left, top, area.getWidth() - left - right, area.getHeight() - top - bottom);

        return pArea;
    }

    private Margin getPlotShrink(Graphics2D g, Rectangle2D area, Plot plot) {
        Margin shrink;
        if (plot.isSubPlot) {
            double rowHeight = area.getHeight() / this.rowNum;
            double colWidth = area.getWidth() / this.columnNum;
            double x = area.getX() + plot.columnIndex * colWidth;
            double y = area.getY() + plot.rowIndex * rowHeight;
            Rectangle2D subPlotArea = new Rectangle2D.Double(x, y, colWidth, rowHeight);
            plot.setOuterPositionArea(subPlotArea);
            plot.updatePosition(area, subPlotArea);
            Rectangle2D positionArea = plot.getPositionArea(area);
            plot.setPositionArea(positionArea);
            Margin tightInset = plot.getTightInset(g, positionArea);
            plot.setTightInset(tightInset);
            shrink = plot.getPlotShrink();
        } else {
            plot.setOuterPositionArea(area);
            Rectangle2D positionArea = plot.getPositionArea(area);
            plot.setPositionArea(positionArea);
            Margin tightInset = plot.getTightInset(g, positionArea);
            plot.setTightInset(tightInset);
            shrink = plot.getPlotShrink();
            //shrink = tightInset;
        }

        return shrink;
    }

    private Margin getPlotsShrink(Graphics2D g, Rectangle2D area) {
        Margin pshrink = null, shrink;
        for (int i = 0; i < this.plots.size(); i++) {
            Plot plot = this.plots.get(i);
            plot.setOuterPositionArea(plot.getOuterPositionArea(area));
            Rectangle2D positionArea = plot.getPositionArea(area);
            plot.setPositionArea(positionArea);
            Margin tightInset = plot.getTightInset(g, positionArea);
            plot.setTightInset(tightInset);
            shrink = plot.getPlotShrink();
            if (i == 0) {
                pshrink = shrink;
            } else if (pshrink != null) {
                pshrink = pshrink.extend(shrink);
            }
        }

        return pshrink;
    }

    private Margin getPlotsShrink_bak(Graphics2D g, Rectangle2D area) {
        Margin pshrink = null, shrink;
        for (int i = 0; i < this.plots.size(); i++) {
            Plot plot = this.plots.get(i);
            if (plot.isSubPlot) {
                double rowHeight = area.getHeight() / this.rowNum;
                double colWidth = area.getWidth() / this.columnNum;
                double x = area.getX() + plot.columnIndex * colWidth;
                double y = area.getY() + plot.rowIndex * rowHeight;
                Rectangle2D subPlotArea = new Rectangle2D.Double(x, y, colWidth, rowHeight);
                plot.setOuterPositionArea(subPlotArea);
                plot.updatePosition(area, subPlotArea);
                Rectangle2D positionArea = plot.getPositionArea(area);
                plot.setPositionArea(positionArea);
                Margin tightInset = plot.getTightInset(g, positionArea);
                plot.setTightInset(tightInset);
                shrink = plot.getPlotShrink();
            } else {
                plot.setOuterPositionArea(area);
                Rectangle2D positionArea = plot.getPositionArea(area);
                plot.setPositionArea(positionArea);
                Margin tightInset = plot.getTightInset(g, positionArea);
                plot.setTightInset(tightInset);
                shrink = plot.getPlotShrink();
            }
            if (i == 0) {
                pshrink = shrink;
            } else if (pshrink != null) {
                pshrink = pshrink.extend(shrink);
            }
        }

        return pshrink;
    }

    private Margin getPlotsTightInset(Graphics2D g, Rectangle2D area) {
        int i = 0;
        Margin pti = null, tightInset;
        for (Plot plot : this.plots) {
            if (plot.isSubPlot) {
                double rowHeight = area.getHeight() / this.rowNum;
                double colWidth = area.getWidth() / this.columnNum;
                double x = area.getX() + plot.columnIndex * colWidth;
                double y = area.getY() + plot.rowIndex * rowHeight;
                Rectangle2D subPlotArea = new Rectangle2D.Double(x, y, colWidth, rowHeight);
                plot.setOuterPositionArea(subPlotArea);
                plot.updatePosition(area, subPlotArea);
                Rectangle2D positionArea = plot.getPositionArea();
                plot.setPositionArea(positionArea);
                tightInset = plot.getTightInset(g, positionArea);
            } else {
                plot.setOuterPositionArea(area);
                Rectangle2D positionArea = plot.getPositionArea();
                plot.setPositionArea(positionArea);
                tightInset = plot.getTightInset(g, positionArea);
            }
            if (i == 0) {
                pti = tightInset;
            } else if (pti != null) {
                pti = pti.extend(tightInset);
            }
            i += 1;
        }

        return pti;
    }

    private double getPositionAreaZoom(Graphics2D g, Rectangle2D area) {
        double zoom = 1.0;
        for (Plot plot : this.plots) {
            if (plot.isSubPlot) {
                double rowHeight = area.getHeight() / this.rowNum;
                double colWidth = area.getWidth() / this.columnNum;
                double x = area.getX() + plot.columnIndex * colWidth;
                double y = area.getY() + plot.rowIndex * rowHeight;
                Rectangle2D subPlotArea = new Rectangle2D.Double(x, y, colWidth, rowHeight);
                plot.setOuterPositionArea(subPlotArea);
                plot.updatePosition(area, subPlotArea);
                Rectangle2D positionArea = plot.getPositionArea();
                plot.setPositionArea(positionArea);
                Margin tightInset = plot.getTightInset(g, positionArea);
                plot.setTightInset(tightInset);
                double zoom1 = plot.updatePostionAreaZoom();
                if (zoom1 < zoom) {
                    zoom = zoom1;
                }
            } else {
                plot.setOuterPositionArea(area);
                Rectangle2D positionArea = plot.getPositionArea();
                plot.setPositionArea(positionArea);
                Margin tightInset = plot.getTightInset(g, positionArea);
                plot.setTightInset(tightInset);
                double zoom1 = plot.updatePostionAreaZoom();
                if (zoom1 < zoom) {
                    zoom = zoom1;
                }
            }
        }

        return zoom;
    }

    private Rectangle2D getSubPlotArea(Graphics2D g, Plot plot, Rectangle2D area) {
        if (plot.isSubPlot) {
            double rowHeight = area.getHeight() / this.rowNum;
            double colWidth = area.getWidth() / this.columnNum;
            double x = area.getX() + plot.columnIndex * colWidth;
            double y = area.getY() + plot.rowIndex * rowHeight;
            Rectangle2D subPlotArea = new Rectangle2D.Double(x, y, colWidth, rowHeight);
            plot.setOuterPositionArea(subPlotArea);
            plot.updatePosition(area, subPlotArea);
            Rectangle2D positionArea = plot.getPositionArea();
            plot.setPositionArea(positionArea);
            Margin tightInset = plot.getTightInset(g, positionArea);
            plot.setTightInset(tightInset);
            double zoom = plot.updatePostionAreaZoom();
            plot.setPositionAreaZoom(zoom);
            return subPlotArea;
        } else {
            plot.setOuterPositionArea(area);
            Rectangle2D positionArea = plot.getPositionArea();
            plot.setPositionArea(positionArea);
            Margin tightInset = plot.getTightInset(g, positionArea);
            plot.setTightInset(tightInset);
            double zoom = plot.updatePostionAreaZoom();
            plot.setPositionAreaZoom(zoom);
            //return tightInset.getArea(positionArea);
            return area;
        }
    }

    /**
     * Get graph area
     *
     * @return Get graph area
     */
    public Rectangle2D getGraphArea() {
        Rectangle2D rect = this.plots.get(0).getPositionArea();
        double left = rect.getX() + this.plotArea.getX();
        double top = rect.getY() + this.plotArea.getY();
        return new Rectangle2D.Double(left, top, rect.getWidth(), rect.getHeight());
    }

    /**
     * Find a plot by point
     *
     * @param x X
     * @param y Y
     * @return Plot
     */
    public Plot findPlot(int x, int y) {
        for (Plot plot : this.plots) {
            Rectangle2D area = plot.getPositionArea();
            if (area.contains(x, y)) {
                return plot;
            }
        }

        return null;
    }

    /**
     * Clear plots
     */
    public void clearPlots() {
        this.plots.clear();
    }

    /**
     * Clear texts
     */
    public void clearTexts() {
        this.texts.clear();
    }

    /**
     * Remove a plot
     *
     * @param plot The plot
     */
    public void removePlot(Plot plot) {
        this.plots.remove(plot);
    }

    /**
     * Add a plot
     *
     * @param plot Plot
     */
    public void addPlot(Plot plot) {
        if (plot instanceof MapPlot) {
            ((MapPlot) plot).setParent(parent);
        }
        this.plots.add(plot);
    }

    /**
     * Set plot
     *
     * @param plot Plot
     */
    public void setPlot(Plot plot) {
        if (plot instanceof MapPlot) {
            ((MapPlot) plot).setParent(parent);
        }
        this.plots.clear();
        this.plots.add(plot);
    }

    /**
     * Get plot by plot index
     *
     * @param plotIdx Plot index - begin with 1
     * @return Plot index
     */
    public Plot getPlot(int plotIdx) {
        for (Plot plot : this.plots) {
            int pIdx = plot.rowIndex * this.columnNum + plot.columnIndex + 1;
            if (pIdx == plotIdx) {
                return plot;
            }
        }

        if (plotIdx > 0 && plotIdx <= this.plots.size())
            return this.plots.get(plotIdx - 1);
        else
            return null;
    }
    
    /**
     * Get plot index
     * @param plot The plot
     * @return Plot index
     */
    public int getPlotIndex(Plot plot){
        return this.plots.indexOf(plot);
    }

    /**
     * Check if has web map layer
     *
     * @return Boolean
     */
    public boolean hasWebMap() {
        for (Plot plot : this.plots) {
            if (plot instanceof MapPlot) {
                MapPlot mp = (MapPlot) plot;
                if (mp.hasWebMapLayer()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Add text
     *
     * @param text Text
     */
    public void addText(ChartText text) {
        this.texts.add(text);
    }
    // </editor-fold>

}
