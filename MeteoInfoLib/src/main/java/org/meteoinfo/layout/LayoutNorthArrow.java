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
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.PointF;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Yaqiang Wang
 */
public class LayoutNorthArrow extends LayoutElement {
// <editor-fold desc="Variables">

    private LayoutMap _layoutMap;
    private boolean _antiAlias;
    private boolean _drawNeatLine;
    private Color _neatLineColor;
    private float _neatLineSize;
    private NorthArrowTypes _northArrowType;
    private float _angle;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param layoutMap The layout map
     */
    public LayoutNorthArrow(LayoutMap layoutMap) {
        super();
        this.setElementType(ElementType.LayoutNorthArraw);
        this.setResizeAbility(ResizeAbility.ResizeAll);

        this.setWidth(50);
        this.setHeight(50);

        _layoutMap = layoutMap;
        _antiAlias = true;
        _drawNeatLine = false;
        _neatLineColor = Color.black;
        _neatLineSize = 1;
        _northArrowType = NorthArrowTypes.NorthArrow1;
        _angle = 0;
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
     * Get angle
     *
     * @return Angle
     */
    public float getAngle() {
        return _angle;
    }

    /**
     * Set angle
     *
     * @param angle The angle
     */
    public void setAngle(float angle) {
        _angle = angle;
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
        if (_angle != 0) {
            g.rotate(_angle);
        }
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

        drawNorthArrow(g, zoom);

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

    private void drawNorthArrow(Graphics2D g, float zoom) {
        switch (_northArrowType) {
            case NorthArrow1:
                drawNorthArrow1(g, zoom);
                break;
        }
    }

    private void drawNorthArrow1(Graphics2D g, float zoom) {
        g.setColor(this.getForeColor());
        g.setStroke(new BasicStroke(zoom));

        //Draw N symbol
        PointF[] points = new PointF[4];
        int x = this.getWidth() / 2;
        int y = this.getHeight() / 6;
        int w = this.getWidth() / 6;
        int h = this.getHeight() / 4;
        points[0] = new PointF(x - w / 2, y + h / 2);
        points[1] = new PointF(x - w / 2, y - h / 2);
        points[2] = new PointF(x + w / 2, y + h / 2);
        points[3] = new PointF(x + w / 2, y - h / 2);
        Draw.drawPolyline(points, g);

        //Draw arrow
        w = this.getWidth() / 2;
        h = this.getHeight() * 2 / 3;
        points = new PointF[3];
        points[0] = new PointF(x - w / 2, this.getHeight());
        points[1] = new PointF(x, this.getHeight() - h / 2);
        points[2] = new PointF(x, this.getHeight() - h);
        Draw.fillPolygon(points, g, null);

        points = new PointF[4];
        points[0] = new PointF(x + w / 2, this.getHeight());
        points[1] = new PointF(x, this.getHeight() - h / 2);
        points[2] = new PointF(x, this.getHeight() - h);
        points[3] = points[0];
        Draw.drawPolyline(points, g);
    }

    @Override
    public void moveUpdate() {
    }

    @Override
    public void resizeUpdate() {
    }
    // </editor-fold>
    // <editor-fold desc="BeanInfo">

    public class LayoutNorthArrowBean {

        LayoutNorthArrowBean() {
        }
        // <editor-fold desc="Get Set Methods">

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
         * Get angle
         *
         * @return Angle
         */
        public float getAngle() {
            return _angle;
        }

        /**
         * Set angle
         *
         * @param angle The angle
         */
        public void setAngle(float angle) {
            _angle = angle;
        }
        
        /**
         * Get is draw backcolor
         * @return Boolean
         */
        public boolean isDrawBackColor(){
            return LayoutNorthArrow.this.isDrawBackColor();
        }
        
        /**
         * Set is draw backcolor
         * @param value Boolean
         */
        public void setDrawBackColor(boolean value){
            LayoutNorthArrow.this.setDrawBackColor(value);
        }
        
        /**
         * Get background color
         *
         * @return Background color
         */
        public Color getBackColor() {
            return LayoutNorthArrow.this.getBackColor();
        }

        /**
         * Set background color
         *
         * @param c Background color
         */
        public void setBackColor(Color c) {
            LayoutNorthArrow.this.setBackColor(c);
        }

        /**
         * Get foreground color
         *
         * @return Foreground color
         */
        public Color getForeColor() {
            return LayoutNorthArrow.this.getForeColor();
        }

        /**
         * Set foreground color
         *
         * @param c Foreground color
         */
        public void setForeColor(Color c) {
            LayoutNorthArrow.this.setForeColor(c);
        }
        
        /**
         * Get left
         *
         * @return Left
         */
        public int getLeft() {
            return LayoutNorthArrow.this.getLeft();
        }

        /**
         * Set left
         *
         * @param left Left
         */
        public void setLeft(int left) {
            LayoutNorthArrow.this.setLeft(left);
        }

        /**
         * Get top
         *
         * @return Top
         */
        public int getTop() {
            return LayoutNorthArrow.this.getTop();
        }

        /**
         * Set top
         *
         * @param top Top
         */
        public void setTop(int top) {
            LayoutNorthArrow.this.setTop(top);
        }
        
        /**
         * Get width
         *
         * @return Width
         */
        public int getWidth() {
            return LayoutNorthArrow.this.getWidth();
        }

        /**
         * Set width
         *
         * @param width Width
         */
        public void setWidth(int width) {
            LayoutNorthArrow.this.setWidth(width);
        }

        /**
         * Get height
         *
         * @return Height
         */
        public int getHeight() {
            return LayoutNorthArrow.this.getHeight();
        }

        /**
         * Set height
         *
         * @param height Height
         */
        public void setHeight(int height) {
            LayoutNorthArrow.this.setHeight(height);
        }
        // </editor-fold>
    }

    public static class LayoutNorthArrowBeanBeanInfo extends BaseBeanInfo {

        public LayoutNorthArrowBeanBeanInfo() {
            super(LayoutNorthArrowBean.class);
            addProperty("drawBackColor").setCategory("General").setDisplayName("Draw Background");
            addProperty("backColor").setCategory("General").setDisplayName("Background");
            addProperty("foreColor").setCategory("General").setDisplayName("Foreground");
            addProperty("drawNeatLine").setCategory("Neat Line").setDisplayName("Draw Neat Line");
            addProperty("neatLineColor").setCategory("Neat Line").setDisplayName("Neat Line Color");
            addProperty("neatLineSize").setCategory("Neat Line").setDisplayName("Neat Line Size");
            addProperty("angle").setCategory("Location").setDisplayName("Angle");
            addProperty("left").setCategory("Location").setDisplayName("Left");
            addProperty("top").setCategory("Location").setDisplayName("Top");
            addProperty("width").setCategory("Location").setDisplayName("Width");
            addProperty("height").setCategory("Location").setDisplayName("Height");
        }
    }
    // </editor-fold>
}
