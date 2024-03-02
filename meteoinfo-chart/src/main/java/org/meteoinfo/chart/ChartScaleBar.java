/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import org.meteoinfo.chart.geo.MapPlot;
import org.meteoinfo.common.PointF;

/**
 *
 * @author Yaqiang Wang
 */
public class ChartScaleBar extends ChartElement {

    public enum ScaleBarType {
        SCALE_LINE_1,
        SCALE_LINE_2,
        // Alternating scale bar
        ALTERNATING_BAR;
    }

    public enum ScaleBarUnits {
        KILOMETERS,
        METERS,
        CENTIMETERS,
        MILLIMETERS,
        MILES,
        YARDS,
        FEET,
        INCHES
    }

    // <editor-fold desc="Variables">

    private MapPlot mapPlot;
    private float lineWidth;
    private Font font;
    private ScaleBarType scaleBarType;
    private ScaleBarUnits unit;
    private String unitText;
    private int numBreaks;
    private boolean drawNeatLine;
    private Color neatLineColor;
    private float neatLineSize;
    private boolean drawScaleText;
    private float yShiftScale = 2.0f;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param mapPlot The map plot
     */
    public ChartScaleBar(MapPlot mapPlot) {
        super();

        this.width = 200;
        this.height = 50;
        this.mapPlot = mapPlot;
        antiAlias = true;
        scaleBarType = ScaleBarType.SCALE_LINE_1;
        lineWidth = 1;
        drawNeatLine = false;
        neatLineColor = Color.black;
        neatLineSize = 1;
        font = new Font("Arial", Font.PLAIN, 12);
        unit = ScaleBarUnits.KILOMETERS;
        unitText = "km";
        numBreaks = 4;
        drawScaleText = false;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get map plot
     *
     * @return The map plot
     */
    public MapPlot getMapPlot() {
        return mapPlot;
    }
    
    /**
     * Get line width
     * @return Line width
     */
    public float getLineWidth() {
        return this.lineWidth;
    }
    
    /**
     * Set line width
     * @param value Line width
     */
    public void setLineWidth(float value) {
        this.lineWidth = value;
    }

    /**
     * Get scale bar type
     *
     * @return Scale bar type
     */
    public ScaleBarType getScaleBarType() {
        return scaleBarType;
    }

    /**
     * Set scale bar type
     *
     * @param type Scale bar type
     */
    public void setScaleBarType(ScaleBarType type) {
        scaleBarType = type;
    }

    /**
     * Get if draw neat line
     *
     * @return If draw neat line
     */
    public boolean isDrawNeatLine() {
        return drawNeatLine;
    }

    /**
     * Set if draw neat line
     *
     * @param istrue If draw neat line
     */
    public void setDrawNeatLine(boolean istrue) {
        drawNeatLine = istrue;
    }

    /**
     * Get neat line color
     *
     * @return Neat line color
     */
    public Color getNeatLineColor() {
        return neatLineColor;
    }

    /**
     * Set neat line color
     *
     * @param color Neat line color
     */
    public void setNeatLineColor(Color color) {
        neatLineColor = color;
    }

    /**
     * Get neat line size
     *
     * @return Neat line size
     */
    public float getNeatLineSize() {
        return neatLineSize;
    }

    /**
     * Set neat line size
     *
     * @param size Neat line size
     */
    public void setNeatLineSize(float size) {
        neatLineSize = size;
    }

    /**
     * Get font
     *
     * @return The font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Set font
     *
     * @param font The font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Get break number
     *
     * @return The break number
     */
    public int getBreakNumber() {
        return numBreaks;
    }

    /**
     * Set break number
     *
     * @param num Break number
     */
    public void setBreakNumber(int num) {
        numBreaks = num;
    }

    /**
     * Get if draw scale text
     *
     * @return If draw scale text
     */
    public boolean isDrawScaleText() {
        return drawScaleText;
    }

    /**
     * Set if draw scale text
     *
     * @param istrue If draw scale text
     */
    public void setDrawScaleText(boolean istrue) {
        drawScaleText = istrue;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Draw text
     *
     * @param g Graphics2D
     * @param x X
     * @param y Y
     */
    public void draw(Graphics2D g, float x, float y) {
        AffineTransform oldMatrix = g.getTransform();
        g.translate(x, y);
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        //Draw background color
        if (this.isDrawBackColor()){
            g.setColor(this.getBackground());
            g.fill(new Rectangle.Float(0, 0, this.getWidth(), this.getHeight()));
        }

        drawScaleBar(g);

        //Draw neatline
        if (drawNeatLine) {
            Rectangle.Float mapRect = new Rectangle.Float(neatLineSize - 1, neatLineSize - 1,
                    (this.getWidth() - neatLineSize), (this.getHeight() - neatLineSize));
            g.setColor(neatLineColor);
            g.setStroke(new BasicStroke(neatLineSize));
            g.draw(mapRect);
        }

        g.setTransform(oldMatrix);
    }

    /**
     * Paint graphics
     *
     * @param g Graphics
     * @param pageLocation Page location
     * @param zoom Zoom
     */
    public void paintGraphics(Graphics2D g, PointF pageLocation) {
        AffineTransform oldMatrix = g.getTransform();
        PointF aP = pageToScreen(this.getX(), this.getY(), pageLocation, 1);
        g.translate(aP.X, aP.Y);
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        //Draw background color
        if (this.isDrawBackColor()){
            g.setColor(this.getBackground());
            g.fill(new Rectangle.Float(0, 0, this.getWidth(), this.getHeight()));
        }

        drawScaleBar(g);

        //Draw neatline
        if (drawNeatLine) {
            Rectangle.Float mapRect = new Rectangle.Float(neatLineSize - 1, neatLineSize - 1,
                    (this.getWidth() - neatLineSize), (this.getHeight() - neatLineSize));
            g.setColor(neatLineColor);
            g.setStroke(new BasicStroke(neatLineSize));
            g.draw(mapRect);
        }

        g.setTransform(oldMatrix);
    }

    private void drawScaleBar(Graphics2D g) {
        //Calculates the width of one break in geographic units
        FontMetrics metrics = g.getFontMetrics(this.font);
        float unitLength = metrics.stringWidth(unitText) * 2;
        float widthNoUnit = (this.getWidth() - unitLength);
        long geoBreakWidth = (long) (getGeoWidth(widthNoUnit / numBreaks));

        //If the geoBreakWidth is less than 1 we return and don't draw anything
        if (geoBreakWidth < 1) {
            return;
        }

        double n = Math.pow(10, String.valueOf(geoBreakWidth).length() - 1);
        geoBreakWidth = (long) (Math.floor(geoBreakWidth / n) * n);

        long breakWidth = (long) (getWidth(geoBreakWidth));
        FontMetrics metrics1 = g.getFontMetrics(font);
        float fontHeight = metrics1.getHeight();
        float leftStart = metrics1.stringWidth(String.valueOf(Math.abs(geoBreakWidth))) / 2F;

        //Draw scale text
        double scale = geoBreakWidth * getConversionFactor(unit) * 100 / (breakWidth / 96 * 2.539999918);
        if (drawScaleText) {
            g.setFont(this.font);
            g.setColor(this.getForeground());
            g.drawString("1 : " + String.format("{0:0,0}", scale),
                    leftStart - (metrics.stringWidth(String.valueOf(Math.abs(0))) / 2), fontHeight * 2.5F);
        }

        //Draw scale bar
        switch (scaleBarType) {
            case SCALE_LINE_1:
                drawScaleLine1(g, breakWidth, geoBreakWidth);
                break;
            case SCALE_LINE_2:
                drawScaleLine2(g, breakWidth, geoBreakWidth);
                break;
            case ALTERNATING_BAR:
                drawAlternatingBar(g, breakWidth, geoBreakWidth);
                break;
        }
    }

    private double getConversionFactor(ScaleBarUnits unit) {
        switch (unit) {
            case KILOMETERS:
                return 1000;
            default:
                return 1;
        }
    }

    private double getGeoWidth(double width) {
        double geoWidth = width / mapPlot.getXScale() / getConversionFactor(unit);
        if (mapPlot.isLonLatMap()) {
            geoWidth = geoWidth * getLonDistScale();
        }

        return geoWidth;
    }

    private double getWidth(double geoWidth) {
        double width = geoWidth * mapPlot.getXScale() * getConversionFactor(unit);
        if (mapPlot.isLonLatMap()) {
            width = width / getLonDistScale();
        }

        return width;
    }

    private double getLonDistScale() {
        //Get meters of one longitude degree
        double pY = (mapPlot.getDrawExtent().maxY + mapPlot.getDrawExtent().minY) / 2;
        double ProjX = 0, ProjY = pY, pProjX = 1, pProjY = pY;
        double dx = Math.abs(ProjX - pProjX);
        double dy = Math.abs(ProjY - pProjY);
        double dist;
        double y = (ProjY + pProjY) / 2;
        double factor = Math.cos(y * Math.PI / 180);
        dx *= factor;
        dist = Math.sqrt(dx * dx + dy * dy);
        dist = dist * 111319.5;

        return dist;
    }

    private void drawScaleLine1(Graphics2D g, long breakWidth, long geoBreakWidth) {
        FontMetrics metrics = g.getFontMetrics(font);
        float fontHeight = metrics.getHeight();
        float leftStart = metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth))) / 2F;
        int yShift = 10;

        g.setColor(this.getForeground());
        g.setStroke(new BasicStroke(this.lineWidth));
        g.draw(new Line2D.Float(leftStart, fontHeight * 1.6f + yShift, leftStart + (breakWidth * numBreaks), fontHeight * 1.6f + yShift));
        g.setFont(this.font);
        for (int i = 0; i <= numBreaks; i++) {
            g.draw(new Line2D.Float(leftStart, fontHeight * 1.1f + yShift, leftStart, fontHeight * 1.6f + yShift));
            g.drawString(String.valueOf(Math.abs(geoBreakWidth * i)),
                    leftStart - (metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth * i))) / 2), yShift * yShiftScale);
            leftStart = leftStart + breakWidth;
        }
        g.drawString(unitText, leftStart - breakWidth + (fontHeight / 2), fontHeight * 1.1f + yShift * yShiftScale);
    }

    private void drawScaleLine2(Graphics2D g, long breakWidth, long geoBreakWidth) {
        FontMetrics metrics = g.getFontMetrics(font);
        float fontHeight = metrics.getHeight();
        float leftStart = metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth))) / 2F;
        int yShift = 5;

        g.setColor(this.getForeground());
        g.setStroke(new BasicStroke(this.lineWidth));
        g.draw(new Line2D.Float(leftStart, fontHeight * 1.6f + yShift, leftStart + (breakWidth * numBreaks), fontHeight * 1.6f + yShift));
        g.setFont(this.font);
        for (int i = 0; i <= numBreaks; i++) {
            g.draw(new Line2D.Float(leftStart, fontHeight * 1.1f + yShift, leftStart, fontHeight + (fontHeight * 1.1f) + yShift));
            g.drawString(String.valueOf(Math.abs(geoBreakWidth * i)),
                    leftStart - (metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth * i))) / 2), yShift * yShiftScale);
            leftStart = leftStart + breakWidth;
        }
        g.drawString(unitText, leftStart - breakWidth + (fontHeight / 2), fontHeight * 1.1f + yShift * yShiftScale);
    }

    private void drawAlternatingBar(Graphics2D g, long breakWidth, long geoBreakWidth) {
        FontMetrics metrics = g.getFontMetrics(font);
        float fontHeight = metrics.getHeight();
        float leftStart = metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth))) / 2F;
        int yShift = 5;
        float rHeight = fontHeight / 2;

        boolean isFill = false;
        g.setStroke(new BasicStroke(this.lineWidth));
        g.setColor(this.getForeground());
        g.setFont(this.font);
        for (int i = 0; i <= numBreaks; i++) {
            if (i < numBreaks) {
                if (isFill) {                    
                    g.fill(new Rectangle.Float(leftStart, fontHeight * 1.1f + yShift, breakWidth, rHeight));
                }
                g.draw(new Rectangle.Float(leftStart, fontHeight * 1.1f + yShift, breakWidth, rHeight));
            }            
            g.drawString(String.valueOf(Math.abs(geoBreakWidth * i)),
                    leftStart - (metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth * i))) / 2), yShift * yShiftScale);
            leftStart = leftStart + breakWidth;
            isFill = !isFill;
        }
        g.setColor(this.getForeground());
        g.drawString(unitText, leftStart - breakWidth + (fontHeight / 2), fontHeight * 1.1f + yShift * yShiftScale);
    }

    @Override
    public void moveUpdate() {
    }

    @Override
    public void resizeUpdate() {
    }
    // </editor-fold>
}
