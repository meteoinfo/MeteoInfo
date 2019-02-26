/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.meteoinfo.chart.plot.XAlign;
import org.meteoinfo.chart.plot.YAlign;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.Extent;
import org.meteoinfo.jts.geom.Geometry;
import org.meteoinfo.jts.geom.GeometryFactory;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author yaqiang
 */
public class ChartText extends Shape {

    // <editor-fold desc="Variables">
    protected double x;
    protected double y;
    private Font font;
    private List<String> text;
    private Color color;
    private int lineSpace;
    private CoordinateType coordinates;
    private Color background;
    private boolean drawBackground;
    private boolean drawNeatline;
    private Color neatLineColor;
    private float neatLineSize;
    private float gap;
    protected float angle;
    private XAlign xAlign;
    private YAlign yAlign;
    private boolean useExternalFont;

    // </editor-fold>    
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ChartText() {
        font = new Font("Arial", Font.PLAIN, 14);
        color = Color.black;
        lineSpace = 5;
        coordinates = CoordinateType.DATA;
        this.background = Color.white;
        this.drawBackground = false;
        this.drawNeatline = false;
        this.neatLineColor = Color.black;
        this.neatLineSize = 1.0f;
        this.gap = 3.0f;
        this.angle = 0.0f;
        this.xAlign = XAlign.LEFT;
        this.yAlign = YAlign.BOTTOM;
        this.useExternalFont = false;
    }

    /**
     * Constructor
     *
     * @param text Text
     */
    public ChartText(String text) {
        this();
        this.text = new ArrayList<>();
        String[] lines = text.split("\n");
        this.text.addAll(Arrays.asList(lines));
    }

    /**
     * Constructor
     *
     * @param text Text
     */
    public ChartText(List<String> text) {
        this();
        this.text = text;
    }

    /**
     * Constructor
     *
     * @param text Text
     * @param font Font
     */
    public ChartText(String text, Font font) {
        this();
        this.text = new ArrayList<>();
        String[] lines = text.split("\n");
        this.text.addAll(Arrays.asList(lines));
        this.font = font;
    }

    /**
     * Constructor
     *
     * @param text Text
     * @param font Font
     */
    public ChartText(List<String> text, Font font) {
        this();
        this.text = text;
        this.font = font;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get text
     *
     * @return Text
     */
    public String getText() {
        return text.get(0);
    }

    /**
     * Set text
     *
     * @param value Text
     */
    public void setText(String value) {
        text = new ArrayList<>();
        String[] lines = value.split("\n");
        this.text.addAll(Arrays.asList(lines));
    }

    /**
     * Get texts
     *
     * @return Text list
     */
    public List<String> getTexts() {
        return text;
    }

    /**
     * Set texts
     *
     * @param value Text list
     */
    public void setTexts(List<String> value) {
        text = value;
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
     * Get title color
     *
     * @return Title color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set title color
     *
     * @param value Title color
     */
    public void setColor(Color value) {
        this.color = value;
    }

    /**
     * Get x
     *
     * @return X
     */
    public double getX() {
        return this.x;
    }

    /**
     * Set x
     *
     * @param value X
     */
    public void setX(double value) {
        this.x = value;
    }

    /**
     * Get y
     *
     * @return Y
     */
    public double getY() {
        return this.y;
    }

    /**
     * Set y
     *
     * @param value Y
     */
    public void setY(double value) {
        this.y = value;
    }

    /**
     * Get line space
     *
     * @return Line space
     */
    public int getLineSpace() {
        return this.lineSpace;
    }

    /**
     * Set line space
     *
     * @param value Line space
     */
    public void setLineSpace(int value) {
        this.lineSpace = value;
    }

    /**
     * Get coordinates
     *
     * @return Coordinates
     */
    public CoordinateType getCoordinates() {
        return this.coordinates;
    }

    /**
     * Set coordinates
     *
     * @param value Coordinates
     */
    public void setCoordinates(CoordinateType value) {
        this.coordinates = value;
    }

    /**
     * Set coordinates
     *
     * @param value Coordinates
     */
    public void setCoordinates(String value) {
        switch (value) {
            case "axes":
                this.coordinates = CoordinateType.AXES;
                break;
            case "figure":
                this.coordinates = CoordinateType.FIGURE;
                break;
            case "data":
                this.coordinates = CoordinateType.DATA;
                break;
            case "inches":
                this.coordinates = CoordinateType.INCHES;
                break;
        }
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

    /**
     * Get gap
     *
     * @return Gap
     */
    public float getGap() {
        return this.gap;
    }

    /**
     * Set gap
     *
     * @param value Gap
     */
    public void setGap(float value) {
        this.gap = value;
    }

    /**
     * Get angle
     *
     * @return Angle
     */
    public float getAngle() {
        return this.angle;
    }

    /**
     * Set angle
     *
     * @param value Angle
     */
    public void setAngle(float value) {
        this.angle = value;
    }

    /**
     * Get x align
     *
     * @return X align
     */
    public XAlign getXAlign() {
        return this.xAlign;
    }

    /**
     * Set x align
     *
     * @param value X align
     */
    public void setXAlign(XAlign value) {
        this.xAlign = value;
    }

    /**
     * Set x align
     *
     * @param value X align string
     */
    public void setXAlign(String value) {
        this.xAlign = XAlign.valueOf(value.toUpperCase());
    }

    /**
     * Get y align
     *
     * @return Y align
     */
    public YAlign getYAlign() {
        return this.yAlign;
    }

    /**
     * Set y align
     *
     * @param value Y align
     */
    public void setYAlign(YAlign value) {
        this.yAlign = value;
    }

    /**
     * Set y align
     *
     * @param value Y align string
     */
    public void setYAlign(String value) {
        this.yAlign = YAlign.valueOf(value.toUpperCase());
    }

    /**
     * Get if use external font - only for LaTeX string
     *
     * @return Boolean
     */
    public boolean isUseExternalFont() {
        return this.useExternalFont;
    }

    /**
     * Set if use external font - only for LaTeX string
     *
     * @param value Boolean
     */
    public void setUseExternalFont(boolean value) {
        this.useExternalFont = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add text in new line
     *
     * @param value The text string
     */
    public void addText(String value) {
        this.text.add(value);
    }

    @Override
    public ShapeTypes getShapeType() {
        return ShapeTypes.TEXT;
    }

    /**
     * Get text line number
     *
     * @return Text line number
     */
    public int getLineNum() {
        return this.text.size();
    }

    /**
     * Get text dimension with angle
     * @param g Graphics2D
     * @return Dimension
     */
    public Dimension getTrueDimension(Graphics2D g) {
        Dimension dim = getDimension(g);
        if (this.angle != 0){
            int width = dim.width;
            int height = dim.height;                
            int temp;
            if (angle == 90 || angle == -90) {
                temp = width;
                width = height;
                height = temp;
            } else {
                width = (int) ((width * Math.cos(Math.toRadians(angle))) + (height * Math.sin(Math.toRadians(angle))));
                height = (int) ((width * Math.sin(Math.toRadians(angle))) + (height * Math.cos(Math.toRadians(angle))));
            }
            return new Dimension(width, height);
        } else {
            return dim;
        }                
    }
    
    /**
     * Get text dimension
     * @param g Graphics2D
     * @return Dimension
     */
    public Dimension getDimension(Graphics2D g) {
        g.setFont(font);
        int width = 0, height = 0;
        for (String line : this.text) {
            Dimension dim = Draw.getStringDimension(line, g);
            if (width < dim.width) {
                width = dim.width;
            }
            height += dim.height + this.lineSpace;
        }
        height -= this.lineSpace;

        return new Dimension(width, height);
    }

    /**
     * To geometry method
     *
     * @param factory GeometryFactory
     * @return Geometry
     */
    @Override
    public Geometry toGeometry(GeometryFactory factory) {
        return null;
    }

    /**
     * Set point
     *
     * @param x X
     * @param y Y
     */
    public void setPoint(double x, double y) {
        this.x = x;
        this.y = y;
        Extent aExtent = new Extent();
        aExtent.minX = x;
        aExtent.maxX = x;
        aExtent.minY = y;
        aExtent.maxY = y;
        this.setExtent(aExtent);
    }

    /**
     * To string
     *
     * @return String
     */
    @Override
    public String toString() {
        if (this.text.size() == 1) {
            return this.text.get(0);
        } else {
            String r = "";
            for (int i = 0; i < this.text.size(); i++) {
                if (i == 0) {
                    r = this.text.get(i);
                } else {
                    r = r + "\n" + this.text.get(i);
                }
            }
            return r;
        }
    }

    /**
     * Draw text
     *
     * @param g Graphics2D
     * @param x X
     * @param y Y
     */
    public void draw(Graphics2D g, float x, float y) {
        Dimension dim = this.getDimension(g);

        AffineTransform tempTrans = g.getTransform();
        if (this.angle != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(tempTrans.getTranslateX() + x, tempTrans.getTranslateY() + y);
            myTrans.rotate(-angle * Math.PI / 180);
            g.setTransform(myTrans);
            x = 0;
            y = 0;
        }

        Rectangle.Double rect = new Rectangle.Double(x, y - dim.getHeight(), dim.getWidth(), dim.getHeight());
        rect.setRect(rect.x - gap, rect.y - gap, rect.width + gap * 2,
                rect.height + gap * 2);
        if (this.drawBackground) {
            g.setColor(this.background);
            g.fill(rect);
        }
        if (this.drawNeatline) {
            g.setColor(this.neatLineColor);
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(neatLineSize));
            g.draw(rect);
            g.setStroke(oldStroke);
        }

        g.setColor(this.color);
        g.setFont(font);
        switch (this.yAlign) {
            case BOTTOM:
                y = y - dim.height;
                break;
            case CENTER:
                y = y - dim.height * 0.5f;
                break;
        }

        for (String str : this.text) {
            dim = Draw.getStringDimension(str, g);
            Draw.drawString(g, x, y, str, xAlign, YAlign.TOP, useExternalFont);
            y += dim.height;
            y += this.lineSpace;
        }

        if (this.angle != 0) {
            g.setTransform(tempTrans);
        }
    }
    
    /**
     * Clone
     * @return Cloned object
     */
    @Override
    public Object clone() {
        ChartText ct = new ChartText();
        ct.angle = this.angle;
        ct.background = this.background;
        ct.color = this.color;
        ct.coordinates = this.coordinates;
        ct.drawBackground = this.drawBackground;
        ct.drawNeatline = this.drawNeatline;
        ct.font = this.font;
        ct.gap = this.gap;
        ct.lineSpace = this.lineSpace;
        ct.neatLineColor = this.neatLineColor;
        ct.neatLineSize = this.neatLineSize;
        ct.text = this.text;
        ct.useExternalFont = this.useExternalFont;
        ct.x = this.x;
        ct.xAlign = this.xAlign;
        ct.y = this.y;
        ct.yAlign = this.yAlign;
        
        return ct;
    }
    // </editor-fold>
}
