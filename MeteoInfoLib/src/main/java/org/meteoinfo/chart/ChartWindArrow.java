/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.PointF;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.ArrowBreak;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.WindArrow;

/**
 *
 * @author Yaqiang Wang
 */
public class ChartWindArrow {

    // <editor-fold desc="Variables">
    private final WindArrow windArrow;
    private ArrowBreak arrowBreak;
    private Font font;
    //private Color color;
    private Color labelColor;
    private float x;
    private float y;
    private String label;
    private int labelSep;
    private Object layer;
    private Color background;
    private boolean drawBackground;
    private boolean drawNeatline;
    private Color neatLineColor;
    private float neatLineSize;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ChartWindArrow() {
        this.windArrow = new WindArrow();
        this.windArrow.angle = 270;
        this.windArrow.length = 20;
        this.arrowBreak = new ArrowBreak();
        this.font = new Font("Arial", Font.PLAIN, 12);
        //this.color = Color.black;
        this.labelColor = Color.black;
        this.labelSep = 5;
        this.background = Color.white;
        this.drawBackground = false;
        this.drawNeatline = false;
        this.neatLineColor = Color.black;
        this.neatLineSize = 1.0f;
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get wind arrow
     *
     * @return Wind arrow
     */
    public WindArrow getWindArrow() {
        return this.windArrow;
    }
    
    /**
     * Get arrow break
     * @return Arrow break
     */
    public ArrowBreak getArrowBreak() {
        return this.arrowBreak;
    }
    
    /**
     * Set arrow break
     * @param value Arrow break
     */
    public void setArrowBreak(ArrowBreak value) {
        this.arrowBreak = value;
    }

    /**
     * Get length
     *
     * @return Length
     */
    public float getLength() {
        return this.windArrow.length;
    }

    /**
     * Set length
     *
     * @param value Length
     */
    public void setLength(float value) {
        this.windArrow.length = value;
        this.label = String.valueOf(value);
        this.label = DataConvert.removeTailingZeros(this.label);
    }

    /**
     * Get angle
     *
     * @return Angle
     */
    public double getAngle() {
        return this.windArrow.angle;
    }

    /**
     * Set angle
     *
     * @param value Angle
     */
    public void setAngle(double value) {
        this.windArrow.angle = value;
    }

    /**
     * Get layer
     *
     * @return Layer
     */
    public Object getLayer() {
        return this.layer;
    }

    /**
     * Set layer
     *
     * @param value Layer
     */
    public void setLayer(Object value) {
        this.layer = value;
    }

    /**
     * Get font
     *
     * @return Font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Set font
     *
     * @param value Font
     */
    public void setFont(Font value) {
        font = value;
    }

    /**
     * Get label color
     *
     * @return Label color
     */
    public Color getLabelColor() {
        return this.labelColor;
    }

    /**
     * Set label color
     *
     * @param value Label color
     */
    public void setLabelColor(Color value) {
        this.labelColor = value;
    }
    
    /**
     * Get the distance between arrow and label
     * @return Distance between arrow and label
     */
    public int getLabelSep(){
        return this.labelSep;
    }
    
    /**
     * Set the distance between arrow and label
     * @param value Distance between arrow and label
     */
    public void setLabelSep(int value) {
        this.labelSep = value;
    }

    /**
     * Get x
     *
     * @return X
     */
    public float getX() {
        return this.x;
    }

    /**
     * Set x
     *
     * @param value X
     */
    public void setX(float value) {
        this.x = value;
    }

    /**
     * Get y
     *
     * @return Y
     */
    public float getY() {
        return this.y;
    }

    /**
     * Set y
     *
     * @param value Y
     */
    public void setY(float value) {
        this.y = value;
    }

    /**
     * Get label
     *
     * @return Label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Set label
     *
     * @param value Label
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Get background color
     *
     * @return Background color
     */
    public Color getBackground() {
        return this.background;
    }

    /**
     * Set background color
     *
     * @param value Background color
     */
    public void setBackground(Color value) {
        this.background = value;
    }

    /**
     * Get if is fill background
     *
     * @return Boolean
     */
    public boolean isFill() {
        return this.drawBackground;
    }

    /**
     * Set fill background or not
     *
     * @param value Boolean
     */
    public void setFill(boolean value) {
        this.drawBackground = value;
    }

    /**
     * Get draw neatline or not
     *
     * @return Boolean
     */
    public boolean isDrawNeatline() {
        return this.drawNeatline;
    }

    /**
     * Set draw neatline or not
     *
     * @param value Boolean
     */
    public void setDrawNeatline(boolean value) {
        this.drawNeatline = value;
    }

    /**
     * Get neatline color
     *
     * @return Neatline color
     */
    public Color getNeatlineColor() {
        return this.neatLineColor;
    }

    /**
     * Set neatline color
     *
     * @param value Neatline color
     */
    public void setNeatlineColor(Color value) {
        this.neatLineColor = value;
    }

    /**
     * Get neatline size
     *
     * @return Neatline size
     */
    public float getNeatlineSize() {
        return this.neatLineSize;
    }

    /**
     * Set neatline size
     *
     * @param value Neatline size
     */
    public void setNeatlineSize(float value) {
        this.neatLineSize = value;
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
        Object rendering = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float zoom = 1.0f;
        if (this.layer != null) {
            if (this.layer instanceof VectorLayer) {
                zoom = ((VectorLayer) this.layer).getDrawingZoom();
            } else if (this.layer instanceof GraphicCollection) {
                zoom = ((GraphicCollection) this.layer).getArrowZoom();
            }
        }
        g.setFont(this.font);
        //String drawStr = this.label wa.getLabel();
        Dimension dim = Draw.getStringDimension(this.label, g);
        if (this.drawBackground || this.drawNeatline) {
            Rectangle2D rect = Draw.getArrawBorder(new PointF(x, y), this.windArrow, g, zoom);
            double gap = 5;
            double width = Math.max(rect.getWidth(), dim.getWidth());
            rect.setRect(rect.getX() - gap, rect.getY() - gap - 2, width + gap * 2,
                    rect.getHeight() + dim.height + this.labelSep + gap + 2);
            if (this.drawBackground) {
                g.setColor(this.background);
                g.fill(rect);
            }
            if (this.drawNeatline) {
                g.setColor(this.neatLineColor);
                g.draw(rect);
            }
        }
        //Draw.drawArraw(this.color, new PointF(x, y), this.windArrow, g, zoom);
        Draw.drawArraw(new PointF(x, y), windArrow, arrowBreak, g, zoom);
        g.setColor(this.labelColor);
        Draw.drawString(g, this.label, x, y + dim.height + this.labelSep);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rendering);
    }
    // </editor-fold>
}
