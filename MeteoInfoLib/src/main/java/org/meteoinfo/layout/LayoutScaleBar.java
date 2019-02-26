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
import org.meteoinfo.global.PointF;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

/**
 *
 * @author Yaqiang Wang
 */
public class LayoutScaleBar extends LayoutElement {
// <editor-fold desc="Variables">

    private LayoutMap _layoutMap;
    private boolean _antiAlias;
    private Font _font;
    private ScaleBarTypes _scaleBarType;
    private ScaleBarUnits _unit;
    private String _unitText;
    private int _numBreaks;
    private boolean _drawNeatLine;
    private Color _neatLineColor;
    private float _neatLineSize;
    private boolean _drawScaleText;
    private float _yShiftScale = 2.0f;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param layoutMap The layout map
     */
    public LayoutScaleBar(LayoutMap layoutMap) {
        super();
        this.setElementType(ElementType.LayoutScaleBar);
        this.setResizeAbility(ResizeAbility.ResizeAll);

        this.setWidth(200);
        this.setHeight(50);
        _layoutMap = layoutMap;
        _antiAlias = true;
        _scaleBarType = ScaleBarTypes.ScaleLine1;
        _drawNeatLine = false;
        _neatLineColor = Color.black;
        _neatLineSize = 1;
        _font = new Font("Arial", Font.PLAIN, 12);
        _unit = ScaleBarUnits.Kilometers;
        _unitText = "km";
        _numBreaks = 4;
        _drawScaleText = false;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get layout map
     *
     * @return The layout map
     */
    public LayoutMap getLayoutMap() {
        return _layoutMap;
    }

    /**
     * Get scale bar type
     *
     * @return Scale bar type
     */
    public ScaleBarTypes getScaleBarType() {
        return _scaleBarType;
    }

    /**
     * Set scale bar type
     *
     * @param type Scale bar type
     */
    public void setScaleBarType(ScaleBarTypes type) {
        _scaleBarType = type;
    }

    /**
     * Get if draw neat line
     *
     * @return If draw neat line
     */
    public boolean isDrawNeatLine() {
        return _drawNeatLine;
    }

    /**
     * Set if draw neat line
     *
     * @param istrue If draw neat line
     */
    public void setDrawNeatLine(boolean istrue) {
        _drawNeatLine = istrue;
    }

    /**
     * Get neat line color
     *
     * @return Neat line color
     */
    public Color getNeatLineColor() {
        return _neatLineColor;
    }

    /**
     * Set neat line color
     *
     * @param color Neat line color
     */
    public void setNeatLineColor(Color color) {
        _neatLineColor = color;
    }

    /**
     * Get neat line size
     *
     * @return Neat line size
     */
    public float getNeatLineSize() {
        return _neatLineSize;
    }

    /**
     * Set neat line size
     *
     * @param size Neat line size
     */
    public void setNeatLineSize(float size) {
        _neatLineSize = size;
    }

    /**
     * Get font
     *
     * @return The font
     */
    public Font getFont() {
        return _font;
    }

    /**
     * Set font
     *
     * @param font The font
     */
    public void setFont(Font font) {
        _font = font;
    }

    /**
     * Get break number
     *
     * @return The break number
     */
    public int getBreakNumber() {
        return _numBreaks;
    }

    /**
     * Set break number
     *
     * @param num Break number
     */
    public void setBreakNumber(int num) {
        _numBreaks = num;
    }

    /**
     * Get if draw scale text
     *
     * @return If draw scale text
     */
    public boolean isDrawScaleText() {
        return _drawScaleText;
    }

    /**
     * Set if draw scale text
     *
     * @param istrue If draw scale text
     */
    public void setDrawScaleText(boolean istrue) {
        _drawScaleText = istrue;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void paint(Graphics2D g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void paintOnLayout(Graphics2D g, PointF pageLocation, float zoom) {
        if (this.isVisible()) {
            paintGraphics(g, pageLocation, zoom);
        }
    }

    /**
     * Paint graphics
     *
     * @param g Graphics
     * @param pageLocation Page location
     * @param zoom Zoom
     */
    public void paintGraphics(Graphics2D g, PointF pageLocation, float zoom) {
        AffineTransform oldMatrix = g.getTransform();
        PointF aP = pageToScreen(this.getLeft(), this.getTop(), pageLocation, zoom);
        g.translate(aP.X, aP.Y);
        g.scale(zoom, zoom);
        if (_antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        //Draw background color
        if (this.isDrawBackColor()){
            g.setColor(this.getBackColor());
            g.fill(new Rectangle.Float(0, 0, this.getWidth() * zoom, this.getHeight() * zoom));
        }

        drawScaleBar(g, zoom);

        //Draw neatline
        if (_drawNeatLine) {
            Rectangle.Float mapRect = new Rectangle.Float(_neatLineSize - 1, _neatLineSize - 1,
                    (this.getWidth() - _neatLineSize) * zoom, (this.getHeight() - _neatLineSize) * zoom);
            g.setColor(_neatLineColor);
            g.setStroke(new BasicStroke(_neatLineSize));
            g.draw(mapRect);
        }

        g.setTransform(oldMatrix);
    }

    private void drawScaleBar(Graphics2D g, float zoom) {
        Font aFont = new Font(_font.getFontName(), _font.getStyle(), (int) (_font.getSize() * zoom));
        //Calculates the width of one break in greographic units
        FontMetrics metrics = g.getFontMetrics(aFont);
        float unitLegnth = metrics.stringWidth(_unitText) * 2;
        float widthNoUnit = (this.getWidth() * zoom - unitLegnth);
        long geoBreakWidth = (long) (getGeoWidth(widthNoUnit / _numBreaks));

        //If the geobreakWidth is less than 1 we return and don't draw anything
        if (geoBreakWidth < 1) {
            return;
        }

        double n = Math.pow(10, String.valueOf(geoBreakWidth).length() - 1);
        geoBreakWidth = (long) (Math.floor(geoBreakWidth / n) * n);

        long breakWidth = (long) (getWidth(geoBreakWidth));
        FontMetrics metrics1 = g.getFontMetrics(_font);
        float fontHeight = metrics1.getHeight() * zoom;
        float leftStart = metrics1.stringWidth(String.valueOf(Math.abs(geoBreakWidth))) / 2F;

        //Draw scale text
        double scale = geoBreakWidth * getConversionFactor(_unit) * 100 / (breakWidth / 96 * 2.539999918) * zoom;
        if (_drawScaleText) {
            g.setFont(aFont);
            g.setColor(this.getForeColor());
            g.drawString("1 : " + String.format("{0:0,0}", scale),
                    leftStart - (metrics.stringWidth(String.valueOf(Math.abs(0))) / 2), fontHeight * 2.5F);
        }

        //Draw scale bar
        switch (_scaleBarType) {
            case ScaleLine1:
                drawScaleLine1(g, zoom, aFont, breakWidth, geoBreakWidth);
                break;
            case ScaleLine2:
                drawScaleLine2(g, zoom, aFont, breakWidth, geoBreakWidth);
                break;
            case AlternatingBar:
                drawAlternatingBar(g, zoom, aFont, breakWidth, geoBreakWidth);
                break;
        }
    }

    private double getConversionFactor(ScaleBarUnits unit) {
        switch (unit) {
            case Kilometers:
                return 1000;
            default:
                return 1;
        }
    }

    private double getGeoWidth(double width) {
        double geoWidth = width / _layoutMap.getMapFrame().getMapView().getXScale() / getConversionFactor(_unit);
        if (_layoutMap.getMapFrame().getMapView().getProjection().isLonLatMap()) {
            geoWidth = geoWidth * getLonDistScale();
        }

        return geoWidth;
    }

    private double getWidth(double geoWidth) {
        double width = geoWidth * _layoutMap.getMapFrame().getMapView().getXScale() * getConversionFactor(_unit);
        if (_layoutMap.getMapFrame().getMapView().getProjection().isLonLatMap()) {
            width = width / getLonDistScale();
        }

        return width;
    }

    private double getLonDistScale() {
        //Get meters of one longitude degree
        double pY = (_layoutMap.getMapFrame().getMapView().getViewExtent().maxY + _layoutMap.getMapFrame().getMapView().getViewExtent().minY) / 2;
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

    private void drawScaleLine1(Graphics2D g, float zoom, Font aFont, long breakWidth, long geoBreakWidth) {
        FontMetrics metrics = g.getFontMetrics(_font);
        float fontHeight = metrics.getHeight() * zoom;
        float leftStart = metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth))) / 2F;
        int yShift = 10;

        g.setColor(this.getForeColor());
        g.setStroke(new BasicStroke(zoom));
        g.draw(new Line2D.Float(leftStart, fontHeight * 1.6f + yShift, leftStart + (breakWidth * _numBreaks), fontHeight * 1.6f + yShift));
        FontMetrics metrics1 = g.getFontMetrics(aFont);
        g.setFont(aFont);
        for (int i = 0; i <= _numBreaks; i++) {
            g.draw(new Line2D.Float(leftStart, fontHeight * 1.1f + yShift, leftStart, fontHeight * 1.6f + yShift));
            g.drawString(String.valueOf(Math.abs(geoBreakWidth * i)),
                    leftStart - (metrics1.stringWidth(String.valueOf(Math.abs(geoBreakWidth * i))) / 2), yShift * _yShiftScale);
            leftStart = leftStart + breakWidth;
        }
        g.drawString(_unitText, leftStart - breakWidth + (fontHeight / 2), fontHeight * 1.1f + yShift * _yShiftScale);
    }

    private void drawScaleLine2(Graphics2D g, float zoom, Font aFont, long breakWidth, long geoBreakWidth) {
        FontMetrics metrics = g.getFontMetrics(_font);
        float fontHeight = metrics.getHeight() * zoom;
        float leftStart = metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth))) / 2F;
        int yShift = 5;

        g.setColor(this.getForeColor());
        g.draw(new Line2D.Float(leftStart, fontHeight * 1.6f + yShift, leftStart + (breakWidth * _numBreaks), fontHeight * 1.6f + yShift));
        FontMetrics metrics1 = g.getFontMetrics(aFont);
        g.setFont(aFont);
        for (int i = 0; i <= _numBreaks; i++) {
            g.draw(new Line2D.Float(leftStart, fontHeight * 1.1f + yShift, leftStart, fontHeight + (fontHeight * 1.1f) + yShift));
            g.drawString(String.valueOf(Math.abs(geoBreakWidth * i)),
                    leftStart - (metrics1.stringWidth(String.valueOf(Math.abs(geoBreakWidth * i))) / 2), yShift * _yShiftScale);
            leftStart = leftStart + breakWidth;
        }
        g.drawString(_unitText, leftStart - breakWidth + (fontHeight / 2), fontHeight * 1.1f + yShift * _yShiftScale);
    }

    private void drawAlternatingBar(Graphics2D g, float zoom, Font aFont, long breakWidth, long geoBreakWidth) {
        FontMetrics metrics = g.getFontMetrics(_font);
        float fontHeight = metrics.getHeight() * zoom;
        float leftStart = metrics.stringWidth(String.valueOf(Math.abs(geoBreakWidth))) / 2F;
        int yShift = 5;
        float rHeight = fontHeight / 2;
        FontMetrics metrics1 = g.getFontMetrics(aFont);

        boolean isFill = false;
        for (int i = 0; i <= _numBreaks; i++) {
            if (i < _numBreaks) {
                if (isFill) {
                    g.setColor(this.getForeColor());
                    g.fill(new Rectangle.Float(leftStart, fontHeight * 1.1f + yShift, breakWidth, rHeight));
                } else {
                    g.setColor(this.getForeColor());
                    g.draw(new Rectangle.Float(leftStart, fontHeight * 1.1f + yShift, breakWidth, rHeight));
                }
            }

            g.setColor(this.getForeColor());
            g.setFont(aFont);
            g.drawString(String.valueOf(Math.abs(geoBreakWidth * i)),
                    leftStart - (metrics1.stringWidth(String.valueOf(Math.abs(geoBreakWidth * i))) / 2), yShift * _yShiftScale);
            leftStart = leftStart + breakWidth;
            isFill = !isFill;
        }
        g.setColor(this.getForeColor());
        g.setFont(aFont);
        g.drawString(_unitText, leftStart - breakWidth + (fontHeight / 2), fontHeight * 1.1f + yShift * _yShiftScale);
    }

    @Override
    public void moveUpdate() {
    }

    @Override
    public void resizeUpdate() {
    }
    // </editor-fold>
    // <editor-fold desc="BeanInfo">

    public class LayoutScaleBarBean {

        LayoutScaleBarBean() {
        }
        // <editor-fold desc="Get Set Methods">

        /**
         * Get scale bar type
         *
         * @return Scale bar type
         */
        public String getScaleBarType() {
            return _scaleBarType.toString();
        }

        /**
         * Set scale bar type
         *
         * @param type Scale bar type
         */
        public void setScaleBarType(String type) {
            _scaleBarType = ScaleBarTypes.valueOf(type);
        }

        /**
         * Get if draw neat line
         *
         * @return If draw neat line
         */
        public boolean isDrawNeatLine() {
            return _drawNeatLine;
        }

        /**
         * Set if draw neat line
         *
         * @param istrue If draw neat line
         */
        public void setDrawNeatLine(boolean istrue) {
            _drawNeatLine = istrue;
        }

        /**
         * Get neat line color
         *
         * @return Neat line color
         */
        public Color getNeatLineColor() {
            return _neatLineColor;
        }

        /**
         * Set neat line color
         *
         * @param color Neat line color
         */
        public void setNeatLineColor(Color color) {
            _neatLineColor = color;
        }

        /**
         * Get neat line size
         *
         * @return Neat line size
         */
        public float getNeatLineSize() {
            return _neatLineSize;
        }

        /**
         * Set neat line size
         *
         * @param size Neat line size
         */
        public void setNeatLineSize(float size) {
            _neatLineSize = size;
        }

        /**
         * Get font
         *
         * @return The font
         */
        public Font getFont() {
            return _font;
        }

        /**
         * Set font
         *
         * @param font The font
         */
        public void setFont(Font font) {
            _font = font;
        }

        /**
         * Get break number
         *
         * @return The break number
         */
        public int getBreakNumber() {
            return _numBreaks;
        }

        /**
         * Set break number
         *
         * @param num Break number
         */
        public void setBreakNumber(int num) {
            _numBreaks = num;
        }

        /**
         * Get if draw scale text
         *
         * @return If draw scale text
         */
        public boolean isDrawScaleText() {
            return _drawScaleText;
        }

        /**
         * Set if draw scale text
         *
         * @param istrue If draw scale text
         */
        public void setDrawScaleText(boolean istrue) {
            _drawScaleText = istrue;
        }
        
        /**
         * Get is draw backcolor
         * @return Boolean
         */
        public boolean isDrawBackColor(){
            return LayoutScaleBar.this.isDrawBackColor();
        }
        
        /**
         * Set is draw backcolor
         * @param value Boolean
         */
        public void setDrawBackColor(boolean value){
            LayoutScaleBar.this.setDrawBackColor(value);
        }
        
        /**
         * Get background color
         *
         * @return Background color
         */
        public Color getBackColor() {
            return LayoutScaleBar.this.getBackColor();
        }

        /**
         * Set background color
         *
         * @param c Background color
         */
        public void setBackColor(Color c) {
            LayoutScaleBar.this.setBackColor(c);
        }

        /**
         * Get foreground color
         *
         * @return Foreground color
         */
        public Color getForeColor() {
            return LayoutScaleBar.this.getForeColor();
        }

        /**
         * Set foreground color
         *
         * @param c Foreground color
         */
        public void setForeColor(Color c) {
            LayoutScaleBar.this.setForeColor(c);
        }
        
        /**
         * Get left
         *
         * @return Left
         */
        public int getLeft() {
            return LayoutScaleBar.this.getLeft();
        }

        /**
         * Set left
         *
         * @param left Left
         */
        public void setLeft(int left) {
            LayoutScaleBar.this.setLeft(left);
        }

        /**
         * Get top
         *
         * @return Top
         */
        public int getTop() {
            return LayoutScaleBar.this.getTop();
        }

        /**
         * Set top
         *
         * @param top Top
         */
        public void setTop(int top) {
            LayoutScaleBar.this.setTop(top);
        }
        
        /**
         * Get width
         *
         * @return Width
         */
        public int getWidth() {
            return LayoutScaleBar.this.getWidth();
        }

        /**
         * Set width
         *
         * @param width Width
         */
        public void setWidth(int width) {
            LayoutScaleBar.this.setWidth(width);
        }

        /**
         * Get height
         *
         * @return Height
         */
        public int getHeight() {
            return LayoutScaleBar.this.getHeight();
        }

        /**
         * Set height
         *
         * @param height Height
         */
        public void setHeight(int height) {
            LayoutScaleBar.this.setHeight(height);
        }
        // </editor-fold>
    }

    public static class LayoutScaleBarBeanBeanInfo extends BaseBeanInfo {

        public LayoutScaleBarBeanBeanInfo() {
            super(LayoutScaleBarBean.class);
            ExtendedPropertyDescriptor e = addProperty("scaleBarType");
            e.setCategory("General").setDisplayName("Scale Bar Type");
            e.setPropertyEditorClass(ScaleBarTypeEditor.class);
            addProperty("drawBackColor").setCategory("General").setDisplayName("Draw Background");
            addProperty("backColor").setCategory("General").setDisplayName("Background");
            addProperty("foreColor").setCategory("General").setDisplayName("Foreground");
            addProperty("font").setCategory("General").setDisplayName("Font");
            addProperty("drawScaleText").setCategory("General").setDisplayName("Draw Scale Text");
            addProperty("drawNeatLine").setCategory("Neat Line").setDisplayName("Draw Neat Line");
            addProperty("neatLineColor").setCategory("Neat Line").setDisplayName("Neat Line Color");
            addProperty("neatLineSize").setCategory("Neat Line").setDisplayName("Neat Line Size");
            addProperty("left").setCategory("Location").setDisplayName("Left");
            addProperty("top").setCategory("Location").setDisplayName("Top");
            addProperty("width").setCategory("Location").setDisplayName("Width");
            addProperty("height").setCategory("Location").setDisplayName("Height");
        }
    }

    public static class ScaleBarTypeEditor extends ComboBoxPropertyEditor {

        public ScaleBarTypeEditor() {
            super();
            ScaleBarTypes[] lutypes = ScaleBarTypes.values();
            String[] types = new String[lutypes.length];
            int i = 0;
            for (ScaleBarTypes type : lutypes) {
                types[i] = type.toString();
                i += 1;
            }
            setAvailableValues(types);
        }
    }
    // </editor-fold>
}
