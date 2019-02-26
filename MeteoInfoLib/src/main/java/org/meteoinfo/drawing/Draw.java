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
package org.meteoinfo.drawing;

import org.meteoinfo.legend.MarkerType;
import org.meteoinfo.legend.PointStyle;
import org.meteoinfo.geoprocess.Spline;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.BreakTypes;
import org.meteoinfo.legend.ChartBreak;
import org.meteoinfo.legend.LabelBreak;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.Polyline;
import org.meteoinfo.shape.WindArrow;
import org.meteoinfo.shape.WindBarb;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.meteoinfo.chart.plot.XAlign;
import org.meteoinfo.chart.plot.YAlign;
import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.legend.ArrowBreak;
import org.meteoinfo.legend.ArrowLineBreak;
import org.meteoinfo.legend.ColorBreakCollection;
import org.meteoinfo.legend.HatchStyle;
import org.meteoinfo.shape.EllipseShape;
import org.meteoinfo.shape.Polygon;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.StationModelShape;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

/**
 * Draw class with some drawing methods
 *
 * @author Yaqiang Wang
 */
public class Draw {

    // <editor-fold desc="String/LaTeX">
//    /**
//     * Determine if the string is a LaTeX string
//     *
//     * @param str String
//     * @return Boolean
//     */
//    public static boolean isLaTeX(String str) {
//        if (str.length() < 2) {
//            return false;
//        }
//
//        String str1 = str.substring(0, 1);
//        String str2 = str.substring(str.length() - 1);
//        return str1.equals("$") && str2.equals("$");
//    }
    /**
     * Get string type [NORMAL | LATEX | MIXING].
     *
     * @param str The string
     * @return String type
     */
    public static StringType getStringType(String str) {
        if (str.length() < 2 || !str.contains("$")) {
            return StringType.NORMAL;
        }

        if (str.contains("$")) {
            int n = str.length() - str.replace("$", "").length();
            int n1 = str.length() - str.replace("\\$", "").length();
            int n2 = n - n1;
            if (n2 < 2) {
                return StringType.NORMAL;
            } else if (n2 == 2) {
                if (str.startsWith("$") && str.endsWith("$") && !str.endsWith("\\$")) {
                    return StringType.LATEX;
                } else {
                    return StringType.MIXING;
                }
            } else {
                return StringType.MIXING;
            }
        } else {
            return StringType.NORMAL;
        }
    }

    /**
     * Split mixing string by $
     *
     * @param str The mixing string
     * @return String list
     */
    public static List<String> splitMixingString(String str) {
        List<String> strs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c == '$') {
                if (sb.length() == 0) {
                    sb.append(c);
                } else {
                    if (sb.substring(sb.length() - 1).equals("\\")) {
                        sb.append(c);
                    } else {
                        if (sb.substring(0, 1).equals("$")) {
                            sb.append(c);
                            strs.add(sb.toString());
                            sb = new StringBuilder();
                        } else {
                            strs.add(sb.toString());
                            sb = new StringBuilder();
                            sb.append(c);
                        }
                    }
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            strs.add(sb.toString());
        }

        return strs;
    }

    /**
     * Get string dimension
     *
     * @param str String
     * @param g Graphics2D
     * @param isLaTeX Is LaTeX or not
     * @return String dimension
     */
    public static Dimension getStringDimension(String str, Graphics2D g, boolean isLaTeX) {
        if (isLaTeX) {
            float size = g.getFont().getSize2D();
            // create a formula
            TeXFormula formula = new TeXFormula(str);

            // render the formla to an icon of the same size as the formula.
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_TEXT, size);

            // insert a border 
            //icon.setInsets(new Insets(5, 5, 5, 5));
            //return new Dimension(icon.getIconWidth(), icon.getIconHeight());
            int width = (int) icon.getTrueIconWidth() + 10;
            int height = (int) icon.getTrueIconHeight();
            //int height = icon.getIconHeight();
            return new Dimension(width, height);
        } else {
            FontMetrics metrics = g.getFontMetrics();
            //int height = (int) (metrics.getAscent() * 5.f / 6.f);
            int height = metrics.getAscent();
            //return new Dimension(metrics.stringWidth(str), metrics.getHeight());
            return new Dimension(metrics.stringWidth(str), height);
        }
    }

    /**
     * Get string dimension
     *
     * @param str String
     * @param angle Angle
     * @param g Graphics2D
     * @param isLaTeX Is LaTeX or not
     * @return String dimension
     */
    public static Dimension getStringDimension(String str, float angle, Graphics2D g, boolean isLaTeX) {
        float width, height;
        if (isLaTeX) {
            float size = g.getFont().getSize2D();
            // create a formula
            TeXFormula formula = new TeXFormula(str);

            // render the formla to an icon of the same size as the formula.
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_TEXT, size);

            // insert a border 
            //icon.setInsets(new Insets(5, 5, 5, 5));
            //return new Dimension(icon.getIconWidth(), icon.getIconHeight());
            width = (int) icon.getTrueIconWidth() + 10;
            height = (int) icon.getTrueIconHeight();
            //height = icon.getIconHeight();
        } else {
            FontMetrics metrics = g.getFontMetrics();
            width = metrics.stringWidth(str);
            height = metrics.getAscent();
        }
        float temp;
        if (angle == 90 || angle == -90) {
            temp = width;
            width = height;
            height = temp;
        } else {
            width = (float) (width * Math.cos(Math.toRadians(angle))) + (float) (height * Math.sin(Math.toRadians(angle)));
            height = (float) (width * Math.sin(Math.toRadians(angle))) + (float) (height * Math.cos(Math.toRadians(angle)));
        }
        return new Dimension((int) width, (int) height);
    }

    /**
     * Get string dimension
     *
     * @param str String
     * @param g Graphics2D
     * @return String dimension
     */
    public static Dimension getStringDimension(String str, Graphics2D g) {
        switch (getStringType(str)) {
            case LATEX:
                return getStringDimension(str, g, true);
            case MIXING:
                List<String> strs = splitMixingString(str);
                Dimension dim = new Dimension(0, 0);
                for (String s : strs) {
                    Dimension dim1 = getStringDimension(s, g, s.startsWith("$") && s.endsWith("$"));
                    dim.setSize(dim.getWidth() + dim1.getWidth(), Math.max(dim.getHeight(), dim1.getHeight()));
                }
                return dim;
            default:
                return getStringDimension(str, g, false);
        }
    }

    /**
     * Get string dimension
     *
     * @param str String
     * @param angle Angle
     * @param g Graphics2D
     * @return String dimension
     */
    public static Dimension getStringDimension(String str, float angle, Graphics2D g) {
        if (angle == 0) {
            return getStringDimension(str, g);
        } else {
            switch (getStringType(str)) {
                case LATEX:
                    return getStringDimension(str, angle, g, true);
                case MIXING:
                    List<String> strs = splitMixingString(str);
                    Dimension dim = new Dimension(0, 0);
                    for (String s : strs) {
                        Dimension dim1 = getStringDimension(s, angle, g, s.startsWith("$") && s.endsWith("$"));
                        dim.setSize(dim.getWidth() + dim1.getWidth(), Math.max(dim.getHeight(), dim1.getHeight()));
                    }
                    return dim;
                default:
                    return getStringDimension(str, angle, g, false);
            }
        }
    }

    /**
     * Draw string
     *
     * @param g Graphics2D
     * @param str String
     * @param x X
     * @param y Y
     */
    public static void drawString(Graphics2D g, String str, float x, float y) {
        drawString(g, str, x, y, true);
    }

    /**
     * Draw string
     *
     * @param g Graphics2D
     * @param str String
     * @param x X
     * @param y Y
     * @param useExternalFont If use external font
     */
    public static void drawString(Graphics2D g, String str, float x, float y, boolean useExternalFont) {
        switch (getStringType(str)) {
            case LATEX:
                drawLaTeX(g, str, x, y, useExternalFont);
                break;
            case MIXING:
                List<String> strs = splitMixingString(str);
                Dimension dim;
                for (String s : strs) {
                    if (s.startsWith("$") && s.endsWith("$")) {
                        drawLaTeX(g, s, x, y, useExternalFont);
                        dim = getStringDimension(s, g, true);
                        x += dim.width;
                    } else {
                        dim = getStringDimension(s, g, false);
                        g.drawString(s, x, y - (float) (dim.getHeight() * 0.2));
                        x += dim.width - 5;
                    }
                }
                break;
            default:
                FontMetrics fm = g.getFontMetrics();
                g.drawString(str, x, y - fm.getDescent());
                break;
        }
    }

//    /**
//     * Draw string
//     *
//     * @param g Graphics2D
//     * @param str String
//     * @param x X
//     * @param y Y
//     * @param isLaTeX If is LaTeX
//     */
//    public static void drawString(Graphics2D g, String str, float x, float y, boolean isLaTeX) {
//        if (isLaTeX) {            
//            drawLaTeX(g, str, x, y);
//        } else {
//            g.drawString(str, x, y);
//        }
//    }
    /**
     * Draw LaTeX string
     *
     * @param g Graphics2D
     * @param str String
     * @param x X
     * @param y Y
     * @param useExternalFont If use external font
     */
    public static void drawLaTeX(Graphics2D g, String str, float x, float y, boolean useExternalFont) {
        float size = g.getFont().getSize2D();
        drawLaTeX(g, str, size, x, y, useExternalFont);
    }

    /**
     * Draw LaTeX string
     *
     * @param g Graphics2D
     * @param str String
     * @param size Size
     * @param x X
     * @param y Y
     * @param useExternalFont If use external font
     */
    public static void drawLaTeX(Graphics2D g, String str, float size, float x, float y, boolean useExternalFont) {
        if (useExternalFont) {
            //Set font
            TeXFormula.registerExternalFont(Character.UnicodeBlock.BASIC_LATIN, g.getFont().getName());
        } else {
            TeXFormula.registerExternalFont(Character.UnicodeBlock.BASIC_LATIN, null, null);
        }

        // create a formula
        TeXFormula formula = new TeXFormula(str);

        // render the formla to an icon of the same size as the formula.
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_TEXT, size);

        // insert a border 
        icon.setInsets(new Insets(5, 5, 5, 5));
        icon.setForeground(g.getColor());
        y = y - icon.getIconHeight() + (icon.getIconHeight() - icon.getTrueIconHeight()) * 0.6f;
        //y = y - icon.getIconHeight() + size * 0.7f;
        //y = y - icon.getTrueIconHeight() * 1.f;
        Font font = g.getFont();
        icon.paintIcon(null, g, (int) x, (int) y);
        g.setFont(font);
    }

    // </editor-fold>
    // <editor-fold desc="Point">
    /**
     * Create wind barb from wind direction/speed
     *
     * @param windDir Wind direction
     * @param windSpeed Wind speed
     * @param value Value
     * @param size Size
     * @param sPoint Start point
     * @return WindBarb
     */
    public static WindBarb calWindBarb(float windDir, float windSpeed, double value,
            float size, PointD sPoint) {
        WindBarb aWB = new WindBarb();

        windSpeed += 1;
        aWB.windSpeed = windSpeed;
        aWB.angle = windDir;
        aWB.setValue(value);
        aWB.size = size;
        aWB.setPoint(sPoint);
        aWB.windSpeesLine.W20 = (int) (windSpeed / 20);
        aWB.windSpeesLine.W4 = (int) ((windSpeed - aWB.windSpeesLine.W20 * 20) / 4);
        aWB.windSpeesLine.W2 = (int) ((windSpeed - aWB.windSpeesLine.W20 * 20
                - aWB.windSpeesLine.W4 * 4) / 2);

        return aWB;
    }

    /**
     * Create station model shape
     *
     * @param windDir Wind direction
     * @param windSpeed Wind speed
     * @param value Value
     * @param size Size
     * @param sPoint Location point
     * @param weather Weather
     * @param temp Temperature
     * @param dewPoint Dew point
     * @param pressure Pressure
     * @param cloudCover Cloud cover
     * @return Station model shape
     */
    public static StationModelShape calStationModel(float windDir, float windSpeed, double value,
            float size, PointD sPoint, int weather, int temp, int dewPoint, int pressure, int cloudCover) {
        StationModelShape aSM = new StationModelShape();
        aSM.setPoint(sPoint);
        aSM.setValue(value);
        aSM.size = size;
        aSM.temperature = temp;
        aSM.dewPoint = dewPoint;
        aSM.pressure = pressure;
        aSM.windBarb = calWindBarb(windDir, windSpeed, value, size, sPoint);
        aSM.weatherSymbol.size = size / 4 * 3;
        //sPoint.X = sPoint.X - size / 2;
        PointD aPoint = new PointD(sPoint.X - size / 2, sPoint.Y);
        aSM.weatherSymbol.setPoint(aPoint);
        aSM.weatherSymbol.weather = weather;
        aSM.cloudCoverage.cloudCover = cloudCover;
        aSM.cloudCoverage.size = size / 4 * 3;
        aSM.cloudCoverage.sPoint = aPoint;

        return aSM;
    }

    /**
     * Draw wind arrow
     *
     * @param sP Start point
     * @param aArraw The arrow
     * @param g Graphics2D
     * @param zoom Zoom
     * @return Border rectangle
     */
    public static Rectangle2D getArrawBorder(PointF sP, WindArrow aArraw, Graphics2D g, double zoom) {
        PointF eP = new PointF(0, 0);
        //PointF eP1 = new PointF(0, 0);
        double len = aArraw.length;
        double angle = aArraw.angle + 180;
        if (angle >= 360) {
            angle -= 360;
        }

        len = len * zoom;

        eP.X = (int) (sP.X + len * Math.sin(angle * Math.PI / 180));
        eP.Y = (int) (sP.Y - len * Math.cos(angle * Math.PI / 180));

        if (angle == 90) {
            eP.Y = sP.Y;
        }

        return new Rectangle2D.Double(Math.min(sP.X, eP.X), Math.min(sP.Y, eP.Y),
                Math.abs(eP.X - sP.X), Math.abs(eP.Y - sP.Y));
    }

    /**
     * Draw arrow line
     *
     * @param points Line points
     * @param pb Legend
     * @param arrowSize ArrowSize
     * @param g Graphics2D
     */
    public static void drawArrow(PointF[] points, PointBreak pb, int arrowSize, Graphics2D g) {
        g.setColor(pb.getColor());
        g.setStroke(new BasicStroke(pb.getOutlineSize()));
        drawPolyline(points, g);

        int n = points.length;
        PointF aPoint = points[n - 2];
        PointF bPoint = points[n - 1];
        double U = bPoint.X - aPoint.X;
        double V = bPoint.Y - aPoint.Y;
        double angle = Math.atan((V) / (U)) * 180 / Math.PI;
        angle = angle + 90;
        if (U < 0) {
            angle = angle + 180;
        }

        if (angle >= 360) {
            angle = angle - 360;
        }

        Draw.drawArraw(g, bPoint, angle, arrowSize);
    }

    /**
     * Draw wind arrow
     *
     * @param aColor The color
     * @param sP Start point
     * @param aArraw The arrow
     * @param g Graphics2D
     * @param zoom Zoom
     * @return Border rectangle
     */
    public static Rectangle2D drawArraw(Color aColor, PointF sP, WindArrow aArraw, Graphics2D g, double zoom) {
        PointF eP = new PointF(0, 0);
        //PointF eP1 = new PointF(0, 0);
        double len = aArraw.length;
        double angle = aArraw.angle + 180;
        if (angle >= 360) {
            angle -= 360;
        }

        len = len * zoom;

        eP.X = (int) (sP.X + len * Math.sin(angle * Math.PI / 180));
        eP.Y = (int) (sP.Y - len * Math.cos(angle * Math.PI / 180));

        if (angle == 90) {
            eP.Y = sP.Y;
        }

        g.setColor(aColor);
        g.draw(new Line2D.Float(sP.X, sP.Y, eP.X, eP.Y));
        drawArraw(g, eP, angle);
        return new Rectangle2D.Double(Math.min(sP.X, eP.X), Math.min(sP.Y, eP.Y),
                Math.abs(eP.X - sP.X), Math.abs(eP.Y - sP.Y));

//        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
//        path.moveTo(sP.X, sP.Y);
//        path.lineTo(eP.X, eP.Y);                        
//
//        eP1.X = (int) (eP.X - aArraw.size * Math.sin((angle + 20.0) * Math.PI / 180));
//        eP1.Y = (int) (eP.Y + aArraw.size * Math.cos((angle + 20.0) * Math.PI / 180));
//        //g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
//        path.moveTo(eP1.X, eP1.Y);
//        path.lineTo(eP.X, eP.Y);
//        
//        eP1.X = (int) (eP.X - aArraw.size * Math.sin((angle - 20.0) * Math.PI / 180));
//        eP1.Y = (int) (eP.Y + aArraw.size * Math.cos((angle - 20.0) * Math.PI / 180));
//        //g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
//        path.lineTo(eP1.X, eP1.Y);
//        g.draw(path);
    }

    /**
     * Draw wind arrow
     *
     * @param sP Start point
     * @param aArraw The arrow
     * @param pb PointBreak
     * @param g Graphics2D
     * @param zoom Zoom
     * @return Border rectangle
     */
    public static Rectangle2D drawArraw(PointF sP, WindArrow aArraw, ArrowBreak pb, Graphics2D g, double zoom) {
        PointF eP = new PointF(0, 0);
        //PointF eP1 = new PointF(0, 0);
        double len = aArraw.length;
        double angle = aArraw.angle + 180;
        if (angle >= 360) {
            angle -= 360;
        }

        len = len * zoom;

        eP.X = (int) (sP.X + len * Math.sin(angle * Math.PI / 180));
        eP.Y = (int) (sP.Y - len * Math.cos(angle * Math.PI / 180));

        if (angle == 90) {
            eP.Y = sP.Y;
        }

        g.setColor(pb.getColor());
        float width = pb.getWidth();
        g.setStroke(new BasicStroke(width));
        g.draw(new Line2D.Float(sP.X, sP.Y, eP.X, eP.Y));
        float headWidth = pb.getHeadWidth();
        float headLength = pb.getHeadLength();
        drawArraw(g, eP, angle, headLength, headWidth, pb.getOverhang());
        return new Rectangle2D.Double(Math.min(sP.X, eP.X), Math.min(sP.Y, eP.Y),
                Math.abs(eP.X - sP.X), Math.abs(eP.Y - sP.Y));
    }

    /**
     * Draw arraw
     *
     * @param g Graphics2D
     * @param sP Start point
     * @param angle Angle
     */
    public static void drawArraw(Graphics2D g, PointF sP, double angle) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
        Rectangle.Float rect = new Rectangle.Float(-4, -4, 8, 8);
        PointF[] pt = new PointF[5];
        pt[0] = new PointF(rect.x, rect.y);
        pt[1] = new PointF(rect.x + rect.width, rect.y + (rect.height / 2));
        pt[2] = new PointF(rect.x, rect.y + rect.height);
        pt[3] = new PointF(rect.x + rect.width / 2, pt[1].Y);
        pt[4] = pt[0];
        path.moveTo(pt[0].X, pt[0].Y);
        for (int i = 1; i < 5; i++) {
            path.lineTo(pt[i].X, pt[i].Y);
        }

        AffineTransform tempTrans = g.getTransform();
        if (angle != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(tempTrans.getTranslateX() + sP.X, tempTrans.getTranslateY() + sP.Y);
            double angle1 = angle - 90;
            myTrans.rotate(angle1 * Math.PI / 180);
            g.setTransform(myTrans);
        }
        path.closePath();
        g.fill(path);

        if (angle != 0) {
            g.setTransform(tempTrans);
        }
    }

    /**
     * Draw arraw
     *
     * @param g Graphics2D
     * @param sP Start point
     * @param angle Angle
     * @param size Arrow size
     */
    public static void drawArraw(Graphics2D g, PointF sP, double angle, int size) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
        Rectangle.Float rect = new Rectangle.Float(-size, -size, size * 2, size * 2);
        PointF[] pt = new PointF[5];
        pt[0] = new PointF(rect.x, rect.y);
        pt[1] = new PointF(rect.x + rect.width, rect.y + (rect.height / 2));
        pt[2] = new PointF(rect.x, rect.y + rect.height);
        pt[3] = new PointF(rect.x + rect.width / 2, pt[1].Y);
        pt[4] = pt[0];
        path.moveTo(pt[0].X, pt[0].Y);
        for (int i = 1; i < 5; i++) {
            path.lineTo(pt[i].X, pt[i].Y);
        }

        AffineTransform tempTrans = g.getTransform();
        if (angle != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(tempTrans.getTranslateX() + sP.X, tempTrans.getTranslateY() + sP.Y);
            double angle1 = angle - 90;
            myTrans.rotate(angle1 * Math.PI / 180);
            g.setTransform(myTrans);
        }
        path.closePath();
        g.fill(path);

        if (angle != 0) {
            g.setTransform(tempTrans);
        }
    }

    /**
     * Draw arraw
     *
     * @param g Graphics2D
     * @param sP Start point
     * @param angle Angle
     * @param length Arrow length
     * @param width Arrow width
     * @param overhang Overhang
     */
    public static void drawArraw(Graphics2D g, PointF sP, double angle, float length, float width,
            float overhang) {
        PointF[] pt;
        float x = -length;
        float y = -width * 0.5f;
        //Rectangle.Float rect = new Rectangle.Float(x, y, length, width);
        if (overhang == 1) {
            pt = new PointF[3];
            pt[0] = new PointF(x, y);
            pt[1] = new PointF(x + length, y + (width / 2));
            pt[2] = new PointF(x, y + width);
        } else {
            x += length * (1 - overhang);
            pt = new PointF[5];
            pt[0] = new PointF(x, y);
            pt[1] = new PointF(x + length, y + (width / 2));
            pt[2] = new PointF(x, y + width);
            pt[3] = new PointF(x + length * overhang, pt[1].Y);
            pt[4] = pt[0];
        }
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, pt.length);
        path.moveTo(pt[0].X, pt[0].Y);
        for (int i = 1; i < pt.length; i++) {
            path.lineTo(pt[i].X, pt[i].Y);
        }

        AffineTransform tempTrans = g.getTransform();
        AffineTransform myTrans = new AffineTransform();
        myTrans.translate(tempTrans.getTranslateX() + sP.X, tempTrans.getTranslateY() + sP.Y);
        double angle1 = angle - 90;
        if (angle1 != 0) {
            myTrans.rotate(angle1 * Math.PI / 180);
        }
        g.setTransform(myTrans);
        if (overhang == 1) {
            g.draw(path);
        } else {
            path.closePath();
            g.fill(path);
        }

        g.setTransform(tempTrans);
    }

    /**
     * Draw arraw
     *
     * @param g Graphics2D
     * @param sP Start point
     * @param angle Angle
     * @param length Arrow length
     * @param width Arrow width
     * @param overhang Overhang
     * @param fillColor Arrow fill color
     * @param outlineColor Arrow outline color
     */
    public static void drawArraw(Graphics2D g, PointF sP, double angle, float length, float width,
            float overhang, Color fillColor, Color outlineColor) {
        PointF[] pt;
        float x = -length;
        float y = -width * 0.5f;
        //Rectangle.Float rect = new Rectangle.Float(x, y, length, width);
        if (overhang == 1) {
            pt = new PointF[3];
            pt[0] = new PointF(x, y);
            pt[1] = new PointF(x + length, 0);
            pt[2] = new PointF(x, y + width);
        } else {
            pt = new PointF[5];
            pt[0] = new PointF(x, y);
            pt[1] = new PointF(x + length, 0);
            pt[2] = new PointF(x, y + width);
            pt[3] = new PointF(x + length * overhang, pt[1].Y);
            pt[4] = pt[0];
        }
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, pt.length);
        path.moveTo(pt[0].X, pt[0].Y);
        for (int i = 1; i < pt.length; i++) {
            path.lineTo(pt[i].X, pt[i].Y);
        }

        AffineTransform tempTrans = g.getTransform();
        AffineTransform myTrans = new AffineTransform();
        myTrans.translate(tempTrans.getTranslateX() + sP.X, tempTrans.getTranslateY() + sP.Y);
        double angle1 = angle - 90;
        if (angle1 != 0) {
            myTrans.rotate(angle1 * Math.PI / 180);
        }
        g.setTransform(myTrans);
        if (overhang == 1) {
            g.setColor(fillColor);
            g.draw(path);
        } else {
            if (fillColor != null) {
                path.closePath();
                g.setColor(fillColor);
                g.fill(path);
            }
            if (outlineColor != null) {
                g.setColor(outlineColor);
                g.draw(path);
            }
        }

        g.setTransform(tempTrans);
    }

    /**
     * Draw wind barb
     *
     * @param aColor Color
     * @param sP Point
     * @param aWB WindBarb
     * @param g Grahics2D
     * @param size Size
     */
    public static void drawWindBarb(Color aColor, PointF sP, WindBarb aWB, Graphics2D g, float size) {
        PointF eP;
        PointF eP1;
        double len = size * 2;
        int i;

        double aLen = len;

        eP = new PointF();
        eP.X = (float) (sP.X + len * Math.sin(aWB.angle * Math.PI / 180));
        eP.Y = (float) (sP.Y - len * Math.cos(aWB.angle * Math.PI / 180));
        g.setColor(aColor);
        g.draw(new Line2D.Float(sP.X, sP.Y, eP.X, eP.Y));

        len = len / 2;
        if (aWB.windSpeesLine.W20 > 0) {
            for (i = 0; i < aWB.windSpeesLine.W20; i++) {
                eP1 = new PointF();
                eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 105) * Math.PI / 180));
                eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 105) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
                eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
                eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
            }
            eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
            eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
        }
        if (aWB.windSpeesLine.W4 > 0) {
            for (i = 0; i < aWB.windSpeesLine.W4; i++) {
                eP1 = new PointF();
                eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 120) * Math.PI / 180));
                eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 120) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
                eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
                eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
            }
        }
        if (aWB.windSpeesLine.W2 > 0) {
            len = len / 2;
            eP1 = new PointF();
            eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 120) * Math.PI / 180));
            eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 120) * Math.PI / 180));
            g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
        }
    }

    /**
     * Draw wind barb
     *
     * @param sP Point
     * @param aWB WindBarb
     * @param pb PointBreak
     * @param g Grahics2D
     */
    public static void drawWindBarb(PointF sP, WindBarb aWB, PointBreak pb, Graphics2D g) {
        PointF eP;
        PointF eP1;
        double len = pb.getSize() * 2;
        int i;

        double aLen = len;

        eP = new PointF();
        eP.X = (float) (sP.X + len * Math.sin(aWB.angle * Math.PI / 180));
        eP.Y = (float) (sP.Y - len * Math.cos(aWB.angle * Math.PI / 180));
        g.setColor(pb.getColor());
        g.setStroke(new BasicStroke(pb.getOutlineSize()));
        g.draw(new Line2D.Float(sP.X, sP.Y, eP.X, eP.Y));

        len = len / 2;
        if (aWB.windSpeesLine.W20 > 0) {
            for (i = 0; i < aWB.windSpeesLine.W20; i++) {
                eP1 = new PointF();
                eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 105) * Math.PI / 180));
                eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 105) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
                eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
                eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
            }
            eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
            eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
        }
        if (aWB.windSpeesLine.W4 > 0) {
            for (i = 0; i < aWB.windSpeesLine.W4; i++) {
                eP1 = new PointF();
                eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 120) * Math.PI / 180));
                eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 120) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
                eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
                eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
            }
        }
        if (aWB.windSpeesLine.W2 > 0) {
            len = len / 2;
            eP1 = new PointF();
            eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 120) * Math.PI / 180));
            eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 120) * Math.PI / 180));
            g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
        }
    }

    /**
     * Draw wind barb
     *
     * @param aColor Color
     * @param sP Point
     * @param aWB WindBarb
     * @param g Grahics2D
     * @param size Size
     * @param cut Cut
     */
    public static void drawWindBarb(Color aColor, PointF sP, WindBarb aWB, Graphics2D g, float size, float cut) {
        PointF eP;
        PointF eP1;
        double len = size * 2;
        int i;

        double aLen = len;

        eP = new PointF();
        eP.X = (float) (sP.X + len * Math.sin(aWB.angle * Math.PI / 180));
        eP.Y = (float) (sP.Y - len * Math.cos(aWB.angle * Math.PI / 180));
        PointF cutSP = new PointF(0, 0);
        cutSP.X = (float) (sP.X + cut * Math.sin(aWB.angle * Math.PI / 180));
        cutSP.Y = (float) (sP.Y - cut * Math.cos(aWB.angle * Math.PI / 180));
        g.setColor(aColor);
        g.draw(new Line2D.Float(cutSP.X, cutSP.Y, eP.X, eP.Y));

        len = len / 2;
        if (aWB.windSpeesLine.W20 > 0) {
            for (i = 0; i < aWB.windSpeesLine.W20; i++) {
                eP1 = new PointF();
                eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 105) * Math.PI / 180));
                eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 105) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
                eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
                eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
            }
            eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
            eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
        }
        if (aWB.windSpeesLine.W4 > 0) {
            for (i = 0; i < aWB.windSpeesLine.W4; i++) {
                eP1 = new PointF();
                eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 120) * Math.PI / 180));
                eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 120) * Math.PI / 180));
                g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
                eP.X = (float) (eP.X - aLen / 8 * Math.sin((aWB.angle) * Math.PI / 180));
                eP.Y = (float) (eP.Y + aLen / 8 * Math.cos((aWB.angle) * Math.PI / 180));
            }
        }
        if (aWB.windSpeesLine.W2 > 0) {
            len = len / 2;
            eP1 = new PointF();
            eP1.X = (float) (eP.X - len * Math.sin((aWB.angle - 120) * Math.PI / 180));
            eP1.Y = (float) (eP.Y + len * Math.cos((aWB.angle - 120) * Math.PI / 180));
            g.draw(new Line2D.Float(eP.X, eP.Y, eP1.X, eP1.Y));
        }
    }

    /**
     * Draw point
     *
     * @param aPS Point style
     * @param aP The point position
     * @param color The color
     * @param outlineColor Outline color
     * @param aSize size
     * @param drawOutline If draw outline
     * @param drawFill If draw fill
     * @param g Graphics2D
     */
    public static void drawPoint(PointStyle aPS, PointF aP, Color color, Color outlineColor,
            float aSize, Boolean drawOutline, Boolean drawFill, Graphics2D g) {
        PointBreak aPB = new PointBreak();
        aPB.setMarkerType(MarkerType.Simple);
        aPB.setStyle(aPS);
        aPB.setColor(color);
        aPB.setOutlineColor(outlineColor);
        aPB.setSize(aSize);
        aPB.setDrawOutline(drawOutline);
        aPB.setDrawFill(drawFill);

        drawPoint(aP, aPB, g);
    }

    /**
     * Draw point
     *
     * @param aP Position
     * @param aPB Point break
     * @param g Graphics
     */
    public static void drawPoint(PointF aP, PointBreak aPB, Graphics2D g) {
        Rectangle clip = g.getClipBounds();
        if (clip != null) {
            g.setClip(null);
            if (aP.X >= clip.x && aP.X <= clip.x + clip.width && aP.Y >= clip.y
                    && aP.Y <= clip.y + clip.height) {
                switch (aPB.getMarkerType()) {
                    case Simple:
                        drawPoint_Simple(aP, aPB, g);
                        break;
                    case Character:
                        drawPoint_Character(aP, aPB, g);
                        break;
                    case Image:
                        drawPoint_Image(aP, aPB, g);
                        break;
                }
            }
            g.setClip(clip);
        } else {
            switch (aPB.getMarkerType()) {
                case Simple:
                    drawPoint_Simple(aP, aPB, g);
                    break;
                case Character:
                    drawPoint_Character(aP, aPB, g);
                    break;
                case Image:
                    drawPoint_Image(aP, aPB, g);
                    break;
            }
        }
    }

    /**
     * Draw point
     *
     * @param aP Position
     * @param aPB Point break
     * @param g Graphics
     */
    public static void drawMapPoint(PointF aP, PointBreak aPB, Graphics2D g) {
        switch (aPB.getMarkerType()) {
            case Simple:
                drawPoint_Simple(aP, aPB, g);
                break;
            case Character:
                drawPoint_Character(aP, aPB, g);
                break;
            case Image:
                drawPoint_Image(aP, aPB, g);
                break;
        }
    }

    private static void drawPoint_Simple(PointF aP, PointBreak aPB, Graphics2D g) {
        AffineTransform tempTrans = g.getTransform();
        if (aPB.getAngle() != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(tempTrans.getTranslateX() + aP.X, tempTrans.getTranslateY() + aP.Y);
            myTrans.rotate(aPB.getAngle() * Math.PI / 180);
            g.setTransform(myTrans);
            aP.X = 0;
            aP.Y = 0;
        }

        g.setStroke(new BasicStroke(1.0f));
        int[] xPoints;
        int[] yPoints;
        float aSize = aPB.getSize();
        boolean drawFill = aPB.isDrawFill();
        boolean drawOutline = aPB.isDrawOutline();
        Color color = aPB.getColor();
        Color outlineColor = aPB.getOutlineColor();
        float outlineSize = aPB.getOutlineSize();

        GeneralPath path = new GeneralPath();

        switch (aPB.getStyle()) {
            case Circle:
                aP.X = aP.X - aSize / 2.f;
                aP.Y = aP.Y - aSize / 2.f;
                Ellipse2D ellipse = new Ellipse2D.Float(aP.X, aP.Y, aSize, aSize);
                if (drawFill) {
                    g.setColor(color);
                    g.fill(ellipse);
                    //g.fillOval((int) aP.X, (int) aP.Y, (int) aSize, (int) aSize);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(ellipse);
                    //g.drawOval((int) aP.X, (int) aP.Y, (int) aSize, (int) aSize);
                }
                break;
            case DOUBLE_CIRCLE:
                float x,
                 y;
                x = aP.X - aSize / 2.f;
                y = aP.Y - aSize / 2.f;
                ellipse = new Ellipse2D.Float(x, y, aSize, aSize);
                if (drawFill) {
                    g.setColor(color);
                    g.fill(ellipse);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(ellipse);
                    x = aP.X - aSize * 0.3f;
                    y = aP.Y - aSize * 0.3f;
                    ellipse = new Ellipse2D.Float(x, y, aSize * 0.6f, aSize * 0.6f);
                    g.draw(ellipse);
                }
                break;
            case Square:
                aP.X = aP.X - aSize / 2.f;
                aP.Y = aP.Y - aSize / 2.f;
                Rectangle2D rect = new Rectangle2D.Float(aP.X, aP.Y, aSize, aSize);
                if (drawFill) {
                    g.setColor(color);
                    g.fill(rect);
                    //g.fillRect((int) aP.X, (int) aP.Y, (int) aSize, (int) aSize);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(rect);
                    //g.drawRect((int) aP.X, (int) aP.Y, (int) aSize, (int) aSize);
                }
                break;
            case Diamond:
                xPoints = new int[4];
                yPoints = new int[4];
                xPoints[0] = (int) (aP.X - aSize / 2);
                yPoints[0] = (int) aP.Y;
                xPoints[1] = (int) aP.X;
                yPoints[1] = (int) (aP.Y - aSize / 2);
                xPoints[2] = (int) (aP.X + aSize / 2);
                yPoints[2] = (int) aP.Y;
                xPoints[3] = (int) aP.X;
                yPoints[3] = (int) (aP.Y + aSize / 2);
                if (drawFill) {
                    g.setColor(color);
                    g.fillPolygon(xPoints, yPoints, xPoints.length);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawPolygon(xPoints, yPoints, xPoints.length);
                }
                break;
            case UpTriangle:
                xPoints = new int[3];
                yPoints = new int[3];
                xPoints[0] = (int) aP.X;
                yPoints[0] = (int) (aP.Y - aSize / 2);
                xPoints[1] = (int) (aP.X + aSize / 4 * Math.sqrt(3));
                yPoints[1] = (int) (aP.Y + aSize / 4);
                xPoints[2] = (int) (aP.X - aSize / 4 * Math.sqrt(3));
                yPoints[2] = (int) (aP.Y + aSize / 4);
                if (drawFill) {
                    g.setColor(color);
                    g.fillPolygon(xPoints, yPoints, xPoints.length);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawPolygon(xPoints, yPoints, xPoints.length);
                }
                break;
            case DownTriangle:
                xPoints = new int[3];
                yPoints = new int[3];
                xPoints[0] = (int) aP.X;
                yPoints[0] = (int) (aP.Y + aSize / 2);
                xPoints[1] = (int) (aP.X - aSize / 4 * Math.sqrt(3));
                yPoints[1] = (int) (aP.Y - aSize / 4);
                xPoints[2] = (int) (aP.X + aSize / 4 * Math.sqrt(3));
                yPoints[2] = (int) (aP.Y - aSize / 4);
                if (drawFill) {
                    g.setColor(color);
                    g.fillPolygon(xPoints, yPoints, xPoints.length);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawPolygon(xPoints, yPoints, xPoints.length);
                }
                break;
            case XCross:
                path.moveTo(aP.X - aSize / 2, aP.Y - aSize / 2);
                path.lineTo(aP.X + aSize / 2, aP.Y + aSize / 2);
                path.moveTo(aP.X - aSize / 2, aP.Y + aSize / 2);
                path.lineTo(aP.X + aSize / 2, aP.Y - aSize / 2);
                path.closePath();
                if (drawFill || drawOutline) {
                    g.setColor(color);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(path);
                }
                break;
            case Plus:
                path.moveTo(aP.X, aP.Y - aSize / 2);
                path.lineTo(aP.X, aP.Y + aSize / 2);
                path.moveTo(aP.X - aSize / 2, aP.Y);
                path.lineTo(aP.X + aSize / 2, aP.Y);
                path.closePath();
                if (drawFill || drawOutline) {
                    g.setColor(color);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(path);
                }
                break;
            case Minus:
                path.moveTo(aP.X - aSize / 2, aP.Y);
                path.lineTo(aP.X + aSize / 2, aP.Y);
                path.closePath();
                if (drawFill || drawOutline) {
                    g.setColor(color);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(path);
                }
                break;
            case StarLines:
                path.moveTo(aP.X - aSize / 2, aP.Y - aSize / 2);
                path.lineTo(aP.X + aSize / 2, aP.Y + aSize / 2);
                path.moveTo(aP.X - aSize / 2, aP.Y + aSize / 2);
                path.lineTo(aP.X + aSize / 2, aP.Y - aSize / 2);
                path.moveTo(aP.X, aP.Y - aSize / 2);
                path.lineTo(aP.X, aP.Y + aSize / 2);
                path.moveTo(aP.X - aSize / 2, aP.Y);
                path.lineTo(aP.X + aSize / 2, aP.Y);
                path.closePath();
                if (drawFill || drawOutline) {
                    g.setColor(color);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(path);
                }
                break;
            case Star:
                float vRadius = aSize / 2;
                //Calculate 5 end points
                PointF[] vPoints = new PointF[5];
                double vAngle = 2.0 * Math.PI / 4 + Math.PI;
                for (int i = 0; i < vPoints.length; i++) {
                    vAngle += 2.0 * Math.PI / (double) vPoints.length;
                    vPoints[i] = new PointF(
                            (float) (Math.cos(vAngle) * vRadius) + aP.X,
                            (float) (Math.sin(vAngle) * vRadius) + aP.Y);
                }
                //Calculate 5 cross points
                PointF[] cPoints = new PointF[5];
                cPoints[0] = MIMath.getCrossPoint(vPoints[0], vPoints[2], vPoints[1], vPoints[4]);
                cPoints[1] = MIMath.getCrossPoint(vPoints[1], vPoints[3], vPoints[0], vPoints[2]);
                cPoints[2] = MIMath.getCrossPoint(vPoints[1], vPoints[3], vPoints[2], vPoints[4]);
                cPoints[3] = MIMath.getCrossPoint(vPoints[0], vPoints[3], vPoints[2], vPoints[4]);
                cPoints[4] = MIMath.getCrossPoint(vPoints[0], vPoints[3], vPoints[1], vPoints[4]);
                //New points
                xPoints = new int[10];
                yPoints = new int[10];
                for (int i = 0; i < 5; i++) {
                    xPoints[i * 2] = (int) vPoints[i].X;
                    yPoints[i * 2] = (int) vPoints[i].Y;
                    xPoints[i * 2 + 1] = (int) cPoints[i].X;
                    yPoints[i * 2 + 1] = (int) cPoints[i].Y;
                }
                if (drawFill) {
                    g.setColor(color);
                    g.fillPolygon(xPoints, yPoints, xPoints.length);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawPolygon(xPoints, yPoints, xPoints.length);
                }
                break;
            case CIRCLE_STAR:
                vRadius = aSize * 0.4f;
                //Calculate 5 end points
                vPoints = new PointF[5];
                vAngle = 2.0 * Math.PI / 4 + Math.PI;
                for (int i = 0; i < vPoints.length; i++) {
                    vAngle += 2.0 * Math.PI / (double) vPoints.length;
                    vPoints[i] = new PointF(
                            (float) (Math.cos(vAngle) * vRadius) + aP.X,
                            (float) (Math.sin(vAngle) * vRadius) + aP.Y);
                }
                //Calculate 5 cross points
                cPoints = new PointF[5];
                cPoints[0] = MIMath.getCrossPoint(vPoints[0], vPoints[2], vPoints[1], vPoints[4]);
                cPoints[1] = MIMath.getCrossPoint(vPoints[1], vPoints[3], vPoints[0], vPoints[2]);
                cPoints[2] = MIMath.getCrossPoint(vPoints[1], vPoints[3], vPoints[2], vPoints[4]);
                cPoints[3] = MIMath.getCrossPoint(vPoints[0], vPoints[3], vPoints[2], vPoints[4]);
                cPoints[4] = MIMath.getCrossPoint(vPoints[0], vPoints[3], vPoints[1], vPoints[4]);
                //New points
                xPoints = new int[10];
                yPoints = new int[10];
                for (int i = 0; i < 5; i++) {
                    xPoints[i * 2] = (int) vPoints[i].X;
                    yPoints[i * 2] = (int) vPoints[i].Y;
                    xPoints[i * 2 + 1] = (int) cPoints[i].X;
                    yPoints[i * 2 + 1] = (int) cPoints[i].Y;
                }
                if (drawFill) {
                    g.setColor(color);
                    g.fillPolygon(xPoints, yPoints, xPoints.length);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    //g.drawPolygon(xPoints, yPoints, xPoints.length);
                    x = aP.X - aSize * 0.5f;
                    y = aP.Y - aSize * 0.5f;
                    ellipse = new Ellipse2D.Float(x, y, aSize, aSize);
                    g.draw(ellipse);
                }
                break;
            case Pentagon:
                vRadius = aSize / 2;
                //Calculate 5 end points
                xPoints = new int[5];
                yPoints = new int[5];
                vAngle = 2.0 * Math.PI / 4 + Math.PI;
                for (int i = 0; i < 5; i++) {
                    vAngle += 2.0 * Math.PI / (double) 5;
                    xPoints[i] = (int) (Math.cos(vAngle) * vRadius + aP.X);
                    yPoints[i] = (int) (Math.sin(vAngle) * vRadius + aP.Y);
                }
                if (drawFill) {
                    g.setColor(color);
                    g.fillPolygon(xPoints, yPoints, xPoints.length);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawPolygon(xPoints, yPoints, xPoints.length);
                }
                break;
            case UpSemiCircle:
                aP.X = aP.X - aSize / 2;
                aP.Y = aP.Y - aSize / 2;
                if (drawFill) {
                    g.setColor(color);
                    g.fill(new Arc2D.Float(aP.X, aP.Y, aSize, aSize, 180, 180, Arc2D.CHORD));
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(new Arc2D.Float(aP.X, aP.Y, aSize, aSize, 180, 180, Arc2D.CHORD));
                }
                break;
            case DownSemiCircle:
                aP.X = aP.X - aSize / 2;
                aP.Y = aP.Y - aSize / 2;
                if (drawFill) {
                    g.setColor(color);
                    g.fill(new Arc2D.Float(aP.X, aP.Y, aSize, aSize, 0, 180, Arc2D.CHORD));
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.draw(new Arc2D.Float(aP.X, aP.Y, aSize, aSize, 0, 180, Arc2D.CHORD));
                }
                break;
        }

        if (aPB.getAngle() != 0) {
            g.setTransform(tempTrans);
        }
    }

    private static void drawPoint_Simple_Up(PointF aP, PointBreak aPB, Graphics2D g) {
        AffineTransform tempTrans = g.getTransform();
        if (aPB.getAngle() != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(tempTrans.getTranslateX() + aP.X, tempTrans.getTranslateY() + aP.Y);
            myTrans.rotate(aPB.getAngle() * Math.PI / 180);
            g.setTransform(myTrans);
            aP.X = 0;
            aP.Y = 0;
        }

        int[] xPoints;
        int[] yPoints;
        float aSize = aPB.getSize();
        boolean drawFill = aPB.isDrawFill();
        boolean drawOutline = aPB.isDrawOutline();
        Color color = aPB.getColor();
        Color outlineColor = aPB.getOutlineColor();
        float outlineSize = aPB.getOutlineSize();

        GeneralPath path = new GeneralPath();

        switch (aPB.getStyle()) {
            case Circle:
                aP.X = aP.X - aSize / 2;
                aP.Y = aP.Y - aSize;

                if (drawFill) {
                    g.setColor(color);
                    g.fillOval((int) aP.X, (int) aP.Y, (int) aSize, (int) aSize);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawOval((int) aP.X, (int) aP.Y, (int) aSize, (int) aSize);
                }
                break;
            case Square:
                aP.X = aP.X - aSize / 2;
                aP.Y = aP.Y - aSize;

                if (drawFill) {
                    g.setColor(color);
                    g.fillRect((int) aP.X, (int) aP.Y, (int) aSize, (int) aSize);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawRect((int) aP.X, (int) aP.Y, (int) aSize, (int) aSize);
                }
                break;
            case Diamond:
                xPoints = new int[4];
                yPoints = new int[4];
                xPoints[0] = (int) (aP.X - aSize / 2);
                yPoints[0] = (int) aP.Y;
                xPoints[1] = (int) aP.X;
                yPoints[1] = (int) (aP.Y - aSize / 2);
                xPoints[2] = (int) (aP.X + aSize / 2);
                yPoints[2] = (int) aP.Y;
                xPoints[3] = (int) aP.X;
                yPoints[3] = (int) (aP.Y + aSize / 2);
                if (drawFill) {
                    g.setColor(color);
                    g.fillPolygon(xPoints, yPoints, xPoints.length);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawPolygon(xPoints, yPoints, xPoints.length);
                }
            case UpTriangle:
                xPoints = new int[3];
                yPoints = new int[3];
                xPoints[0] = (int) aP.X;
                yPoints[0] = (int) (aP.Y - aSize * 3 / 4);
                xPoints[1] = (int) (aP.X + aSize / 4 * Math.sqrt(3));
                yPoints[1] = (int) (aP.Y);
                xPoints[2] = (int) (aP.X - aSize / 4 * Math.sqrt(3));
                yPoints[2] = (int) (aP.Y);
                if (drawFill) {
                    g.setColor(color);
                    g.fillPolygon(xPoints, yPoints, xPoints.length);
                }
                if (drawOutline) {
                    g.setColor(outlineColor);
                    g.setStroke(new BasicStroke(outlineSize));
                    g.drawPolygon(xPoints, yPoints, xPoints.length);
                }
                break;
        }

        if (aPB.getAngle() != 0) {
            g.setTransform(tempTrans);
        }
    }

    private static void drawPoint_Character(PointF aP, PointBreak aPB, Graphics2D g) {
        AffineTransform tempTrans = g.getTransform();
        if (aPB.getAngle() != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(aP.X, aP.Y);
            myTrans.rotate(aPB.getAngle() * Math.PI / 180);
            g.setTransform(myTrans);
            aP.X = 0;
            aP.Y = 0;
        }

        String text = String.valueOf((char) aPB.getCharIndex());
        Font wFont = new Font(aPB.getFontName(), Font.PLAIN, (int) aPB.getSize());
        g.setFont(wFont);
        FontMetrics metrics = g.getFontMetrics();
        PointF sPoint = (PointF) aP.clone();
        sPoint.X = sPoint.X - metrics.stringWidth(text) / 2;
        sPoint.Y = sPoint.Y + metrics.getHeight() / 4;
        //sPoint.X = sPoint.X - aPB.getSize() / 2;
        //sPoint.Y = sPoint.Y + aPB.getSize() / 2;        

        g.setColor(aPB.getColor());
        g.drawString(text, sPoint.X, sPoint.Y);

        if (aPB.getAngle() != 0) {
            g.setTransform(tempTrans);
        }
    }

    private static void drawPoint_Image(PointF aP, PointBreak aPB, Graphics2D g) {
        AffineTransform tempTrans = g.getTransform();
        if (aPB.getAngle() != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(aP.X, aP.Y);
            myTrans.rotate(aPB.getAngle() * Math.PI / 180);
            g.setTransform(myTrans);
            aP.X = 0;
            aP.Y = 0;
        }

        File imgFile = new File(aPB.getImagePath());
        if (!imgFile.exists()) {
            //String path = System.getProperty("user.dir");
            File directory = new File(".");
            String path = null;
            try {
                path = directory.getCanonicalPath();
            } catch (IOException ex) {
                Logger.getLogger(Draw.class.getName()).log(Level.SEVERE, null, ex);
            }
            path = path + File.separator + "Image";
            aPB.setImagePath(path + File.separator + imgFile.getName());
        }
        if (imgFile.exists()) {
            Image image = null;
            try {
                image = ImageIO.read(imgFile);
            } catch (IOException ex) {
                Logger.getLogger(Draw.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (image != null) {
                //((Bitmap)image).MakeTransparent(Color.White);
                int width = (int) aPB.getSize();
                int height = width * image.getHeight(null) / image.getWidth(null);
                PointF sPoint = aP;
                sPoint.X = sPoint.X - width / 2;
                sPoint.Y = sPoint.Y - height / 2;
                g.drawImage(image, (int) sPoint.X, (int) sPoint.Y, width, height, null);
            }
        }

        if (aPB.getAngle() != 0) {
            g.setTransform(tempTrans);
        }
    }

    /**
     * Draws string at the specified coordinates with the specified alignment.
     *
     * @param g graphics context to draw
     * @param x the x coordinate
     * @param y the y coordinate
     * @param s the string to draw
     * @param x_align the alignment in x direction
     * @param y_align the alignment in y direction
     * @param useExternalFont Use external font or not
     */
    public static void drawString(Graphics2D g, float x, float y, String s, XAlign x_align, YAlign y_align, boolean useExternalFont) {
        Dimension dim = Draw.getStringDimension(s, g);
        switch (y_align) {
            case TOP:
                y += dim.getHeight();
                break;
            case CENTER:
                y += dim.getHeight() / 2;
                break;
        }
        switch (x_align) {
            case LEFT:
                drawString(g, s, x, y, useExternalFont);
                break;
            case RIGHT:
                drawString(g, s, x - (float) dim.getWidth(), y, useExternalFont);
                break;
            case CENTER:
                drawString(g, s, x - (float) dim.getWidth() / 2, y, useExternalFont);
                break;
        }
    }

    /**
     * Draw out string
     *
     * @param g Graphics2D
     * @param x X location
     * @param y Y location
     * @param s String
     * @param x_align X align
     * @param y_align Y align
     * @param angle Angle
     * @param useExternalFont Use external font or not
     */
    public static void drawString(Graphics2D g, float x, float y, String s, XAlign x_align, YAlign y_align, float angle, boolean useExternalFont) {
        if (angle == 0) {
            drawString(g, x, y, s, x_align, y_align, useExternalFont);
        } else {
            AffineTransform tempTrans = g.getTransform();
            AffineTransform myTrans = transform(g, x, y, s, x_align, y_align, angle);
            g.setTransform(myTrans);
            Draw.drawString(g, s, 0, 0, useExternalFont);
            g.setTransform(tempTrans);
        }
    }

    /**
     * Graphics transform
     *
     * @param g Graphics2D
     * @param x X location
     * @param y Y location
     * @param s String
     * @param x_align X align
     * @param y_align Y align
     * @param angle Angle
     * @return AffineTransform
     */
    public static AffineTransform transform(Graphics2D g, float x, float y, String s, XAlign x_align, YAlign y_align, float angle) {
        Dimension dim = getStringDimension(s, g);
        AffineTransform tempTrans = g.getTransform();
        AffineTransform myTrans = new AffineTransform();
        switch (x_align) {
            case LEFT:
                switch (y_align) {
                    case CENTER:
                        if (angle == 90) {
                            x += (float) (dim.getHeight());
                            y += (float) (dim.getWidth() * 0.5);
                        } else if (angle == -90) {
                            y -= (float) (dim.getWidth() * 0.5);
                        } else if (angle > 0) {
                            x += (float) (dim.getHeight() * Math.abs(Math.sin(Math.toRadians(angle))));
                            y += (float) (dim.getHeight() * Math.cos(Math.toRadians(angle)) * 0.5);
                        } else {
                            y += (float) (dim.getHeight() * Math.cos(Math.toRadians(angle)) * 0.5);
                        }
                        break;
                }
                break;
            case CENTER:
                switch (y_align) {
                    case TOP:
                        if (angle == 90) {
                            x += (float) (dim.getHeight() * 0.5);
                            y += (float) (dim.getWidth());
                        } else if (angle == -90) {
                            x -= (float) (dim.getHeight() * 0.5);
                        } else if (angle > 0) {
                            x -= (float) (dim.getWidth() * Math.abs(Math.cos(Math.toRadians(angle))));
                            y += (float) (dim.getWidth() * Math.sin(Math.toRadians(angle))) + dim.getHeight();
                        } else {
                            //y += (float) (dim.getHeight() * Math.cos(Math.toRadians(angle)) * 0.5);
                            y += (float) (dim.getHeight() * Math.abs(Math.cos(Math.toRadians(angle))));
                        }
                        break;
                }
                break;
            case RIGHT:
                switch (y_align) {
                    case CENTER:
                        if (angle == 90) {
                            x -= (float) (dim.getHeight());
                            y += (float) (dim.getWidth() * 0.5);
                        } else if (angle == -90) {
                            x -= (float) (dim.getHeight());
                            y -= (float) (dim.getWidth() * 0.5);
                        } else if (angle > 0) {
                            x -= (float) (dim.getWidth() * Math.abs(Math.cos(Math.toRadians(angle))));
                            y += (float) (dim.getHeight() * Math.cos(Math.toRadians(angle)) * 0.5);
                        } else {
                            y += (float) (dim.getHeight() * Math.cos(Math.toRadians(angle)) * 0.5);
                        }
                        break;
                }
                break;
        }
        myTrans.translate(tempTrans.getTranslateX() + x, tempTrans.getTranslateY() + y);
        myTrans.rotate(-angle * Math.PI / 180);

        return myTrans;
    }

    /**
     * Draw label point
     *
     * @param aPoint The screen point
     * @param aLB The label break
     * @param g Graphics2D
     * @param rect The extent rectangle
     */
    public static void drawLabelPoint(PointF aPoint, LabelBreak aLB, Graphics2D g, Rectangle rect) {
        g.setColor(aLB.getColor());
        g.setFont(aLB.getFont());
        Dimension labSize = Draw.getStringDimension(aLB.getText(), g);
        //FontMetrics metrics = g.getFontMetrics(aLB.getFont());
        //Dimension labSize = new Dimension(metrics.stringWidth(aLB.getText()), metrics.getHeight());
        switch (aLB.getAlignType()) {
            case Center:
                aPoint.X = aPoint.X - (float) labSize.getWidth() / 2;
                break;
            case Left:
                aPoint.X = aPoint.X - (float) labSize.getWidth();
                break;
        }
        aLB.setYShift((float) labSize.getHeight() / 2);
        aPoint.Y -= aLB.getYShift();
        aPoint.X += aLB.getXShift();
        float inx = aPoint.X;
        float iny = aPoint.Y;

        AffineTransform tempTrans = g.getTransform();
        if (aLB.getAngle() != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(aPoint.X, aPoint.Y);
            myTrans.rotate(aLB.getAngle() * Math.PI / 180);
            g.setTransform(myTrans);
            aPoint.X = 0;
            aPoint.Y = 0;
        }

        //g.drawString(aLB.getText(), aPoint.X, aPoint.Y + metrics.getHeight() / 2);
        Draw.drawString(g, aLB.getText(), aPoint.X, aPoint.Y + labSize.height / 2);

        rect.x = (int) aPoint.X;
        rect.y = (int) aPoint.Y - labSize.height / 2;
        rect.width = (int) labSize.getWidth();
        rect.height = (int) labSize.getHeight();

        if (aLB.getAngle() != 0) {
            g.setTransform(tempTrans);
            rect.x = (int) inx;
            rect.y = (int) iny;
        }
    }

    /**
     * Draw label point
     *
     * @param x X
     * @param y Y
     * @param font Font
     * @param text Text
     * @param color Color
     * @param g Graphics2D
     * @param rect The extent rectangle
     * @param angle Angle
     */
    public static void drawLabelPoint(float x, float y, Font font, String text, Color color, float angle, Graphics2D g, Rectangle rect) {
        g.setColor(color);
        g.setFont(font);
        Dimension labSize = Draw.getStringDimension(text, g);
        //FontMetrics metrics = g.getFontMetrics(font);
        //Dimension labSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
        x = x - (float) labSize.getWidth() / 2;
        y -= (float) labSize.getHeight() / 2;

        float inx = x;
        float iny = y;

        AffineTransform tempTrans = g.getTransform();
        if (angle != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(x, y);
            myTrans.rotate(angle * Math.PI / 180);
            g.setTransform(myTrans);
            x = 0;
            y = 0;
        }

        //g.drawString(text, x, y + metrics.getHeight() / 2);
        Draw.drawString(g, text, x, y + labSize.height / 2);

        if (rect != null) {
            rect.x = (int) x;
            rect.y = (int) y - labSize.height / 2;
            rect.width = (int) labSize.getWidth();
            rect.height = (int) labSize.getHeight();
        }

        if (angle != 0) {
            g.setTransform(tempTrans);
            if (rect != null) {
                rect.x = (int) inx;
                rect.y = (int) iny;
            }
        }
    }

    /**
     * Draw label point
     *
     * @param x X
     * @param y Y
     * @param font Font
     * @param text Text
     * @param color Color
     * @param g Graphics2D
     * @param rect The extent rectangle
     * @param angle Angle
     * @param useExternalFont If use external font
     */
    public static void drawLabelPoint(float x, float y, Font font, String text, Color color, float angle,
            Graphics2D g, Rectangle rect, boolean useExternalFont) {
        g.setColor(color);
        g.setFont(font);
        Dimension labSize = Draw.getStringDimension(text, g);
        //FontMetrics metrics = g.getFontMetrics(font);
        //Dimension labSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
        x = x - (float) labSize.getWidth() / 2;
        y -= (float) labSize.getHeight() / 2;

        float inx = x;
        float iny = y;

        AffineTransform tempTrans = g.getTransform();
        if (angle != 0) {
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(x, y);
            myTrans.rotate(angle * Math.PI / 180);
            g.setTransform(myTrans);
            x = 0;
            y = 0;
        }

        //g.drawString(text, x, y + metrics.getHeight() / 2);
        Draw.drawString(g, text, x, y + labSize.height / 2, useExternalFont);

        if (rect != null) {
            rect.x = (int) x;
            rect.y = (int) y - labSize.height / 2;
            rect.width = (int) labSize.getWidth();
            rect.height = (int) labSize.getHeight();
        }

        if (angle != 0) {
            g.setTransform(tempTrans);
            if (rect != null) {
                rect.x = (int) inx;
                rect.y = (int) iny;
            }
        }
    }

    /**
     * Draw label point
     *
     * @param x X
     * @param y Y
     * @param font Font
     * @param text Text
     * @param color Color
     * @param g Graphics2D
     * @param angle Angle
     */
    public static void drawTickLabel(float x, float y, Font font, String text, Color color, float angle, Graphics2D g) {
        g.setColor(color);
        g.setFont(font);
        Dimension labSize = Draw.getStringDimension(text, g);
        if (angle == 0) {
            x = x - (float) labSize.getWidth() / 2;
            y -= (float) labSize.getHeight() / 2;
            Draw.drawString(g, text, x, y + labSize.height / 2);
        } else {
            AffineTransform tempTrans = g.getTransform();
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(tempTrans.getTranslateX() + x, tempTrans.getTranslateY() + y);
            myTrans.rotate(-angle * Math.PI / 180);
            g.setTransform(myTrans);
            if (angle == 90) {
                x = -(float) (labSize.getWidth() - 10);
                y = (float) (labSize.getHeight() / 3);
            } else {
                x = -(float) (labSize.getWidth() - 5);
                y = 0;
            }
            Draw.drawString(g, text, x, y);
            g.setTransform(tempTrans);
        }
    }

    /**
     * Draw label point
     *
     * @param x X
     * @param y Y
     * @param font Font
     * @param text Text
     * @param color Color
     * @param g Graphics2D
     * @param angle Angle
     */
    public static void drawTickLabel_Y(float x, float y, Font font, String text, Color color, float angle, Graphics2D g) {
        g.setColor(color);
        g.setFont(font);
        Dimension labSize = Draw.getStringDimension(text, g);
        if (angle == 0) {
            //x = x - (float) labSize.getWidth() / 2;
            //y -= (float) labSize.getHeight() / 2;
            Draw.drawString(g, text, x, y);
        } else {
            AffineTransform tempTrans = g.getTransform();
            AffineTransform myTrans = new AffineTransform();
            myTrans.translate(tempTrans.getTranslateX() + x, tempTrans.getTranslateY() + y);
            myTrans.rotate(-angle * Math.PI / 180);
            g.setTransform(myTrans);
            if (angle == 90) {
                x = -(float) (labSize.getWidth() - 10);
                y = (float) (labSize.getHeight() / 3);
            } else {
                x = -(float) (labSize.getWidth() - 5);
                y = 0;
            }
            Draw.drawString(g, text, x, y);
            g.setTransform(tempTrans);
        }
    }

    /**
     * Draw label point
     *
     * @param x X
     * @param y Y
     * @param font Font
     * @param text Text
     * @param color Color
     * @param g Graphics2D
     * @param angle Angle
     */
    public static void drawTickLabel_YRight(float x, float y, Font font, String text, Color color, float angle, Graphics2D g) {
        g.setColor(color);
        g.setFont(font);
        Dimension labSize = Draw.getStringDimension(text, g);
        if (angle == 0) {
            y += (float) labSize.getHeight() * 0.5f;
            Draw.drawString(g, text, x, y);
        } else {
            AffineTransform tempTrans = g.getTransform();
            AffineTransform myTrans = new AffineTransform();
            if (angle == 90) {
                x += (float) (labSize.getHeight());
                y += (float) (labSize.getWidth() * 0.5);
            } else if (angle == -90) {
                y -= (float) (labSize.getWidth() * 0.5);
            } else if (angle > 0) {
                x += (float) (labSize.getHeight() * Math.abs(Math.sin(Math.toRadians(angle))));
                y += (float) (labSize.getHeight() * Math.cos(Math.toRadians(angle)) * 0.5);
            } else {
                y += (float) (labSize.getHeight() * Math.cos(Math.toRadians(angle)) * 0.5);
            }
            myTrans.translate(tempTrans.getTranslateX() + x, tempTrans.getTranslateY() + y);
            myTrans.rotate(-angle * Math.PI / 180);
            g.setTransform(myTrans);
            Draw.drawString(g, text, 0, 0);
            g.setTransform(tempTrans);
        }
    }

//    /**
//     * Draw label point (270 degress)
//     *
//     * @param x X
//     * @param y Y
//     * @param font Font
//     * @param text Text
//     * @param color Color
//     * @param g Graphics2D
//     * @param rect The extent rectangle
//     */
//    public static void drawLabelPoint_270(float x, float y, Font font, String text, Color color, Graphics2D g, Rectangle rect) {
//        //FontMetrics metrics = g.getFontMetrics(font);
//        //Dimension labSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
//        g.setFont(font);
//        Dimension labSize = Draw.getStringDimension(text, g);
//
//        float inx = x;
//        float iny = y;
//
//        AffineTransform tempTrans = g.getTransform();
//        float angle = 270;
//        g.translate(x, y);
//        g.rotate(angle * Math.PI / 180);
//        x = 0;
//        y = 0;
//
//        g.setColor(color);
//        //g.setFont(font);
//        if (Draw.isLaTeX(text)) {
//            //Draw.drawLaTeX(g, text, x - labSize.width / 2, y - labSize.height);
//            Draw.drawLaTeX(g, text, x - labSize.width / 2, y + labSize.height / 2, true);
//        } else {
//            g.drawString(text, x - labSize.width / 2, y + labSize.height * 3 / 4);
//        }
//
//        if (rect != null) {
//            rect.x = (int) x;
//            rect.y = (int) y - labSize.height / 2;
//            rect.width = (int) labSize.getWidth();
//            rect.height = (int) labSize.getHeight();
//        }
//
//        g.setTransform(tempTrans);
//        if (rect != null) {
//            rect.x = (int) inx;
//            rect.y = (int) iny;
//        }
//    }
//    /**
//     * Draw label point (270 degress)
//     *
//     * @param x X
//     * @param y Y
//     * @param font Font
//     * @param text Text
//     * @param color Color
//     * @param g Graphics2D
//     * @param rect The extent rectangle
//     * @param useExternalFont If use external font
//     */
//    public static void drawLabelPoint_270(float x, float y, Font font, String text, Color color,
//            Graphics2D g, Rectangle rect, boolean useExternalFont) {
//        //FontMetrics metrics = g.getFontMetrics(font);
//        //Dimension labSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
//        g.setFont(font);
//        Dimension labSize = Draw.getStringDimension(text, g);
//
//        float inx = x;
//        float iny = y;
//
//        AffineTransform tempTrans = g.getTransform();
//        float angle = 270;
//        g.translate(x, y);
//        g.rotate(angle * Math.PI / 180);
//        x = 0;
//        y = 0;
//
//        g.setColor(color);
//        //g.setFont(font);
//        if (Draw.isLaTeX(text)) {
//            //Draw.drawLaTeX(g, text, x - labSize.width / 2, y - labSize.height);
//            Draw.drawLaTeX(g, text, x - labSize.width / 2, y + labSize.height / 2, useExternalFont);
//        } else {
//            g.drawString(text, x - labSize.width / 2, y + labSize.height * 3 / 4);
//        }
//
//        if (rect != null) {
//            rect.x = (int) x;
//            rect.y = (int) y - labSize.height / 2;
//            rect.width = (int) labSize.getWidth();
//            rect.height = (int) labSize.getHeight();
//        }
//
//        g.setTransform(tempTrans);
//        if (rect != null) {
//            rect.x = (int) inx;
//            rect.y = (int) iny;
//        }
//    }
    /**
     * Draw station model shape
     *
     * @param aColor Color
     * @param foreColor Foreground color
     * @param sP Start point
     * @param aSM Station model shape
     * @param g Graphics2D
     * @param size Size
     * @param cut Cut
     */
    public static void drawStationModel(Color aColor, Color foreColor, PointF sP, StationModelShape aSM, Graphics2D g,
            float size, float cut) {
        PointF sPoint = new PointF(0, 0);
        g.setColor(aColor);
        Font wFont;
        String text;

        //Draw cloud coverage     
        if (aSM.cloudCoverage.cloudCover >= 0 && aSM.cloudCoverage.cloudCover <= 9) {
            //Draw wind barb
            drawWindBarb(aColor, sP, aSM.windBarb, g, size, cut);
            text = String.valueOf((char) (aSM.cloudCoverage.cloudCover + 197));
            wFont = new Font("Weather", Font.PLAIN, (int) size);
            FontMetrics metrics = g.getFontMetrics(wFont);
            Dimension textSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
            sPoint.X = sP.X - (float) textSize.getWidth() / 2;
            sPoint.Y = sP.Y - (float) textSize.getHeight() / 2;
            g.setFont(wFont);
            g.drawString(text, sPoint.X, sPoint.Y + metrics.getHeight() * 3 / 4);
        } else {
            //Draw wind barb
            drawWindBarb(aColor, sP, aSM.windBarb, g, size);

            wFont = new Font("Arial", Font.PLAIN, (int) (size / 4 * 3));
            text = "M";
            FontMetrics metrics = g.getFontMetrics(wFont);
            Dimension textSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
            sPoint.X = sP.X - (float) textSize.getWidth() / 2;
            sPoint.Y = sP.Y - (float) textSize.getHeight() / 3 * 2;
            g.setFont(wFont);
            g.drawString(text, sPoint.X, sPoint.Y + metrics.getHeight() * 3 / 4);
            wFont = new Font("Weather", Font.PLAIN, (int) size);
            text = String.valueOf((char) 197);
            metrics = g.getFontMetrics(wFont);
            textSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
            sPoint.X = sP.X - (float) textSize.getWidth() / 2;
            sPoint.Y = sP.Y - (float) textSize.getHeight() / 2;
            g.setFont(wFont);
            g.drawString(text, sPoint.X, sPoint.Y + metrics.getHeight() * 3 / 4);
        }

        //Draw weather
        if (aSM.weatherSymbol.weather >= 4 && aSM.weatherSymbol.weather <= 99) {
            wFont = new Font("Weather", Font.PLAIN, (int) size);
            text = String.valueOf((char) (aSM.weatherSymbol.weather + 100));
            FontMetrics metrics = g.getFontMetrics(wFont);
            Dimension textSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
            sPoint.X = sP.X - (float) textSize.getHeight() - aSM.size / 2;
            sPoint.Y = sP.Y - (float) textSize.getHeight() / 2;
            text = String.valueOf((char) (aSM.weatherSymbol.weather + 28));
            if (aSM.weatherSymbol.weather == 99) {
                text = String.valueOf((char) (aSM.weatherSymbol.weather + 97));
            }
            g.setFont(wFont);
            g.drawString(text, sPoint.X, sPoint.Y + metrics.getHeight() * 3 / 4);
        }

        wFont = new Font("Arial", Font.PLAIN, (int) (size / 4 * 3));
        g.setFont(wFont);
        FontMetrics metrics = g.getFontMetrics(wFont);
        //Draw temperature
        if (Math.abs(aSM.temperature) < 1000) {
            g.setColor(Color.red);
            text = String.valueOf(aSM.temperature);
            Dimension textSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
            sPoint.X = sP.X - (float) textSize.getWidth() - size / 3;
            sPoint.Y = sP.Y - (float) textSize.getHeight() - size / 3;
            g.drawString(text, sPoint.X, sPoint.Y + metrics.getHeight() * 3 / 4);
        }

        //Draw dew point
        if (Math.abs(aSM.dewPoint) < 1000) {
            g.setColor(Color.green);
            text = String.valueOf(aSM.dewPoint);
            Dimension textSize = new Dimension(metrics.stringWidth(text), metrics.getHeight());
            sPoint.X = sP.X - (float) textSize.getWidth() - size / 3;
            sPoint.Y = sP.Y + size / 3;
            g.drawString(text, sPoint.X, sPoint.Y + metrics.getHeight() * 3 / 4);
        }

        //Draw pressure
        if (Math.abs(aSM.pressure) < 1000) {
            g.setColor(foreColor);
            text = String.format("%1$03d", aSM.pressure);
            sPoint.X = sP.X + size / 3;
            sPoint.Y = sP.Y - metrics.getHeight() - size / 3;
            g.drawString(text, sPoint.X, sPoint.Y + metrics.getHeight() * 3 / 4);
        }
    }

    // </editor-fold>
    // <editor-fold desc="Graphic">
    /**
     * Draw graphic
     *
     * @param points The points
     * @param aGraphic The graphic
     * @param g Graphics2D
     * @param isEditingVertice Is editing vertice
     */
    public static void drawGrahpic(PointF[] points, Graphic aGraphic, Graphics2D g, boolean isEditingVertice) {
        Rectangle rect = new Rectangle();
        Extent aExtent = MIMath.getPointFsExtent(points);
        rect.x = (int) aExtent.minX;
        rect.y = (int) aExtent.minY;
        rect.width = (int) aExtent.getWidth();
        rect.height = (int) aExtent.getHeight();

        switch (aGraphic.getShape().getShapeType()) {
            case Point:
                switch (aGraphic.getLegend().getBreakType()) {
                    case PointBreak:
                        drawPoint((PointF) points[0].clone(), (PointBreak) aGraphic.getLegend(), g);
                        int aSize = (int) ((PointBreak) aGraphic.getLegend()).getSize() / 2 + 2;
                        rect.x = (int) points[0].X - aSize;
                        rect.y = (int) points[0].Y - aSize;
                        rect.width = aSize * 2;
                        rect.height = aSize * 2;
                        break;
                    case LabelBreak:
                        drawLabelPoint((PointF) points[0].clone(), (LabelBreak) aGraphic.getLegend(), g, rect);
                        break;
                }
                break;
            case Polyline:
                if (aGraphic.getLegend().getBreakType() == BreakTypes.ColorBreakCollection) {
                    drawPolyline(points, (ColorBreakCollection) aGraphic.getLegend(), g);
                } else {
                    drawPolyline(points, (PolylineBreak) aGraphic.getLegend(), g);
                }
                break;
            case Polygon:
                PolygonShape pgs = (PolygonShape) aGraphic.getShape().clone();
                pgs.setPoints_keep(points);
                drawPolygonShape(pgs, (PolygonBreak) aGraphic.getLegend(), g);
                break;
            case Rectangle:
                drawPolygon(points, (PolygonBreak) aGraphic.getLegend(), g);
                break;
            case CurveLine:
                drawCurveLine(points, (PolylineBreak) aGraphic.getLegend(), g);
                break;
            case CurvePolygon:
                drawCurvePolygon(points, (PolygonBreak) aGraphic.getLegend(), g);
                break;
            case Circle:
                drawCircle(points, (PolygonBreak) aGraphic.getLegend(), g);
                break;
            case Ellipse:
                EllipseShape eshape = (EllipseShape) aGraphic.getShape();
                drawEllipse(points, eshape.getAngle(), (PolygonBreak) aGraphic.getLegend(), g);
                break;
        }

        //Draw selected rectangle
        if (aGraphic.getShape().isSelected()) {
            if (isEditingVertice) {
                drawSelectedVertices(g, points);
            } else {
                float[] dashPattern = new float[]{2.0F, 1.0F};
                g.setColor(Color.cyan);
                g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                g.draw(rect);
                switch (aGraphic.getShape().getShapeType()) {
                    case Point:
                        if (aGraphic.getLegend().getBreakType() == BreakTypes.PointBreak) {
                            drawSelectedCorners(g, rect);
                        }
                        break;
                    case Polyline:
                    case CurveLine:
                    case Polygon:
                    case Rectangle:
                    case Ellipse:
                    case CurvePolygon:
                        drawSelectedCorners(g, rect);
                        drawSelectedEdgeCenters(g, rect);
                        break;
                    case Circle:
                        drawSelectedCorners(g, rect);
                        break;
                }
            }
        }
    }

    /**
     * Draw polyline
     *
     * @param points Points list
     * @param g Graphics2D
     */
    public static void drawPolyline(List<PointF> points, Graphics2D g) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.size());
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) {
                path.moveTo(points.get(i).X, points.get(i).Y);
            } else {
                path.lineTo(points.get(i).X, points.get(i).Y);
            }
        }

        g.draw(path);
    }

    /**
     * Draw polyline
     *
     * @param points The points array
     * @param g Graphics2D
     */
    public static void drawPolyline(PointF[] points, Graphics2D g) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.length);
        for (int i = 0; i < points.length; i++) {
            if (i == 0) {
                path.moveTo(points[i].X, points[i].Y);
            } else {
                path.lineTo(points[i].X, points[i].Y);
            }
        }

        g.draw(path);
    }

    /**
     * Draw polyline
     *
     * @param points The points array
     * @param g Graphics2D
     * @param mvIdx Missing value index list
     */
    public static void drawPolyline(PointF[] points, Graphics2D g, List<Integer> mvIdx) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.length - mvIdx.size());
        boolean isNew = true;
        for (int i = 0; i < points.length; i++) {
            if (mvIdx.contains(i)) {
                isNew = true;
                continue;
            }
            if (isNew) {
                path.moveTo(points[i].X, points[i].Y);
                isNew = false;
            } else {
                path.lineTo(points[i].X, points[i].Y);
            }
        }

        g.draw(path);
    }

    /**
     * Fill polygon
     *
     * @param points The points array
     * @param g Graphics2D
     * @param aPGB Polygon break
     */
    public static void fillPolygon(PointF[] points, Graphics2D g, PolygonBreak aPGB) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.length);
        for (int i = 0; i < points.length; i++) {
            if (i == 0) {
                path.moveTo(points[i].X, points[i].Y);
            } else {
                path.lineTo(points[i].X, points[i].Y);
            }
        }
        path.closePath();

        if (aPGB != null) {
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(), aPGB.getBackColor());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(path);
            } else {
                g.fill(path);
            }
        } else {
            g.fill(path);
        }
    }

    /**
     * Draw polygon shape with screen coordinates
     *
     * @param pgs Polygon shape
     * @param pgb Polygon break
     * @param g Graphics2D
     */
    public static void drawPolygonShape(PolygonShape pgs, PolygonBreak pgb, Graphics2D g) {
        for (Polygon polygon : pgs.getPolygons()) {
            drawPolygon(polygon, pgb, g);
        }
    }

    /**
     * Draw polygon with screen coordinate
     *
     * @param aPG Polygon shape
     * @param aPGB Polygon break
     * @param g Graphics2D
     */
    public static void drawPolygon(Polygon aPG, PolygonBreak aPGB, Graphics2D g) {
        int len = aPG.getOutLine().size();
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, len);
        path.moveTo(0, 0);
        PointD wPoint;
        for (int i = 0; i < aPG.getOutLine().size(); i++) {
            wPoint = aPG.getOutLine().get(i);
            if (i == 0) {
                path.moveTo(wPoint.X, wPoint.Y);
            } else {
                path.lineTo(wPoint.X, wPoint.Y);
            }
        }

        List<PointD> newPList;
        if (aPG.hasHole()) {
            for (int h = 0; h < aPG.getHoleLines().size(); h++) {
                newPList = (List<PointD>) aPG.getHoleLines().get(h);
                for (int j = 0; j < newPList.size(); j++) {
                    wPoint = newPList.get(j);
                    if (j == 0) {
                        path.moveTo(wPoint.X, wPoint.Y);
                    } else {
                        path.lineTo(wPoint.X, wPoint.Y);
                    }
                }
            }
        }
        path.closePath();

        if (aPGB.isDrawFill()) {
            //int alpha = (int)((1 - (double)transparencyPerc / 100.0) * 255);
            //Color aColor = Color.FromArgb(alpha, aPGB.Color);
            Color aColor = aPGB.getColor();
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(), aPGB.getBackColor());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(path);
            } else {
                g.setColor(aColor);
                g.fill(path);
            }
        }

        if (aPGB.isDrawOutline()) {
            BasicStroke pen = new BasicStroke(aPGB.getOutlineSize());
            g.setStroke(pen);
            g.setColor(aPGB.getOutlineColor());
            g.draw(path);
        }
    }

    /**
     * Draw polygon
     *
     * @param points The points
     * @param aPGB The polygon break
     * @param g Graphics2D
     */
    public static void drawPolygon(PointF[] points, PolygonBreak aPGB, Graphics2D g) {
        if (aPGB.isDrawFill()) {
            g.setColor(aPGB.getColor());
            fillPolygon(points, g, aPGB);
        }
        if (aPGB.isDrawOutline()) {
            g.setColor(aPGB.getOutlineColor());
            g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
            drawPolyline(points, g);
        }
    }

    /**
     * Draw polygon
     *
     * @param points The points
     * @param aColor Fill oclor
     * @param outlineColor Outline color
     * @param drawFill
     * @param drawOutline
     * @param g
     */
    public static void drawPolygon(PointF[] points, Color aColor, Color outlineColor,
            boolean drawFill, boolean drawOutline, Graphics2D g) {
        if (drawFill) {
            g.setColor(aColor);
            fillPolygon(points, g, null);
        }
        if (drawOutline) {
            g.setColor(outlineColor);
            drawPolyline(points, g);
        }
    }

    /**
     * Get hatch style image
     *
     * @param style Hatch style
     * @param size
     * @param stripeColor Stripe color
     * @param backColor Background color
     * @return Hatch style image
     */
    public static BufferedImage getHatchImage(HatchStyle style, int size, Color stripeColor, Color backColor) {
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        int alpha = backColor.getAlpha();
        if (alpha > 0) {
            g2.setColor(backColor);
            g2.fillRect(0, 0, size, size);
        }
        g2.setColor(stripeColor);
        switch (style) {
            case HORIZONTAL:
                g2.drawLine(0, size / 2, size, size / 2);
                break;
            case VERTICAL:
                g2.drawLine(size / 2, 0, size / 2, size);
                break;
            case FORWARD_DIAGONAL:
                g2.drawLine(0, 0, size, size);
                break;
            case BACKWARD_DIAGONAL:
                //g2.drawLine(size, 0, 0, size);
                g2.draw(new Line2D.Float(0, size, size, 0));
                break;
            case CROSS:
                g2.drawLine(0, size / 2, size, size / 2);
                g2.drawLine(size / 2, 0, size / 2, size);
                break;
            case DIAGONAL_CROSS:
                g2.drawLine(0, 0, size, size);
                g2.drawLine(0, size, size, 0);
                break;
            case DOT:
                g2.fill(new Ellipse2D.Float(size / 2, size / 2, 2, 2));
                break;
        }
        return bi;
    }

    /**
     * Get dash pattern from LineStyle
     *
     * @param style The line style
     * @return Dash pattern array
     */
    public static float[] getDashPattern(LineStyles style) {
        float[] dashPattern = {4.0f};
        switch (style) {
            case SOLID:
                dashPattern = null;
                break;
            case DASH:
                dashPattern = new float[]{4.0f};
                break;
            case DOT:
                dashPattern = new float[]{2.0f};
                break;
            case DASHDOT:
                dashPattern = new float[]{10, 6, 2, 6};
                break;
            case DASHDOTDOT:
                dashPattern = new float[]{10, 6, 2, 6, 2, 6};
                break;
        }

        return dashPattern;
    }

    /**
     * Draw polyline
     *
     * @param points The points
     * @param alb The arrow line break
     * @param g Graphics2D
     */
    public static void drawPolyline(PointF[] points, ArrowLineBreak alb, Graphics2D g) {
        int n = points.length;
        PointF aPoint = points[n - 2];
        PointF bPoint = points[n - 1];
        double U = bPoint.X - aPoint.X;
        double V = bPoint.Y - aPoint.Y;
        double radian = Math.atan(V / U);
        double angle = radian * 180 / Math.PI;
        angle = angle + 90;
        if (U < 0) {
            angle = angle + 180;
        }
        if (angle >= 360) {
            angle = angle - 360;
        }
        double dx = alb.getArrowHeadLength() * Math.cos(radian) * (1 - alb.getArrowOverhang());
        double dy = alb.getArrowHeadLength() * Math.sin(radian) * (1 - alb.getArrowOverhang());
        if (angle > 180) {
            dx = -dx;
            dy = -dy;
        }
        points[n -  1] = new PointF(bPoint.X - (float)dx, bPoint.Y - (float)dy);

        g.setColor(alb.getColor());
        float[] dashPattern = getDashPattern(alb.getStyle());
        g.setStroke(new BasicStroke(alb.getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
        drawPolyline(points, g);

        //Draw symbol            
        if (alb.getDrawSymbol()) {
            Object rend = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle clip = g.getClipBounds();
            PointF p;
            if (clip != null) {
                g.setClip(null);
                for (int i = 0; i < points.length; i++) {
                    p = new PointF(points[i].X, points[i].Y);
                    if (p.X >= clip.x && p.X <= clip.x + clip.width && p.Y >= clip.y && p.Y <= clip.y + clip.height) {
                        if (i % alb.getSymbolInterval() == 0) {
                            drawPoint(alb.getSymbolStyle(), p, alb.getSymbolFillColor(), alb.getSymbolColor(),
                                    alb.getSymbolSize(), true, alb.isFillSymbol(), g);
                        }
                    }
                }
                g.setClip(clip);
            } else {
                for (int i = 0; i < points.length; i++) {
                    if (i % alb.getSymbolInterval() == 0) {
                        p = new PointF(points[i].X, points[i].Y);
                        drawPoint(alb.getSymbolStyle(), p, alb.getSymbolFillColor(), alb.getSymbolColor(),
                                alb.getSymbolSize(), true, alb.isFillSymbol(), g);
                    }
                }
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rend);
        }

        //Draw arrow        
        Draw.drawArraw(g, bPoint, angle, alb.getArrowHeadLength(), alb.getArrowHeadWidth(),
                alb.getArrowOverhang(), alb.getArrowFillColor(), alb.getArrowOutlineColor());
    }

    /**
     * Draw polyline
     *
     * @param points The points
     * @param aPLB The polyline break
     * @param g Graphics2D
     */
    public static void drawPolyline(PointF[] points, PolylineBreak aPLB, Graphics2D g) {
        if (aPLB instanceof ArrowLineBreak) {
            drawPolyline(points, (ArrowLineBreak)aPLB, g);
            return;
        }
        
        if (aPLB.isUsingDashStyle()) {
            g.setColor(aPLB.getColor());
            float[] dashPattern = getDashPattern(aPLB.getStyle());
            g.setStroke(new BasicStroke(aPLB.getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
            drawPolyline(points, g);

            //Draw symbol            
            if (aPLB.getDrawSymbol()) {
                Object rend = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle clip = g.getClipBounds();
                PointF p;
                if (clip != null) {
                    g.setClip(null);
                    for (int i = 0; i < points.length; i++) {
                        p = new PointF(points[i].X, points[i].Y);
                        if (p.X >= clip.x && p.X <= clip.x + clip.width && p.Y >= clip.y && p.Y <= clip.y + clip.height) {
                            if (i % aPLB.getSymbolInterval() == 0) {
                                drawPoint(aPLB.getSymbolStyle(), p, aPLB.getSymbolFillColor(), aPLB.getSymbolColor(),
                                        aPLB.getSymbolSize(), true, aPLB.isFillSymbol(), g);
                            }
                        }
                    }
                    g.setClip(clip);
                } else {
                    for (int i = 0; i < points.length; i++) {
                        if (i % aPLB.getSymbolInterval() == 0) {
                            p = new PointF(points[i].X, points[i].Y);
                            drawPoint(aPLB.getSymbolStyle(), p, aPLB.getSymbolFillColor(), aPLB.getSymbolColor(),
                                    aPLB.getSymbolSize(), true, aPLB.isFillSymbol(), g);
                        }
                    }
                }
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rend);
            }
        } else {
            Polyline aPLine = new Polyline();
            aPLine.setPoints(points);
            List<double[]> pos = aPLine.getPositions(30);
            float aSize = 16;
            int i;
            switch (aPLB.getStyle()) {
                case COLDFRONT:
                    if (pos != null) {
                        PointBreak aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(Color.blue);
                        aPB.setStyle(PointStyle.UpTriangle);
                        aPB.setOutlineColor(Color.blue);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 0; i < pos.size(); i++) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple_Up(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }
                    }

                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    drawPolyline(points, g);
                    break;
                case WARMFRONT:
                    if (pos != null) {
                        PointBreak aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(Color.red);
                        aPB.setStyle(PointStyle.UpSemiCircle);
                        aPB.setOutlineColor(Color.red);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 0; i < pos.size(); i++) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }
                    }

                    g.setColor(Color.red);
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    drawPolyline(points, g);
                    break;
                case OCCLUDEDFRONT:
                    Color aColor = new Color(255, 0, 255);
                    if (pos != null) {
                        PointBreak aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(aColor);
                        aPB.setStyle(PointStyle.UpTriangle);
                        aPB.setOutlineColor(aColor);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 0; i < pos.size(); i += 2) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple_Up(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }

                        aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(aColor);
                        aPB.setStyle(PointStyle.UpSemiCircle);
                        aPB.setOutlineColor(aColor);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 1; i < pos.size(); i += 2) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }
                    }

                    g.setColor(aColor);
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    drawPolyline(points, g);
                    break;
                case STATIONARYFRONT:
                    if (pos != null) {
                        PointBreak aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(Color.blue);
                        aPB.setStyle(PointStyle.UpTriangle);
                        aPB.setOutlineColor(Color.blue);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 0; i < pos.size(); i += 2) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple_Up(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }

                        aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(Color.red);
                        aPB.setStyle(PointStyle.DownSemiCircle);
                        aPB.setOutlineColor(Color.red);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 1; i < pos.size(); i += 2) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }
                    }

                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    drawPolyline(points, g);
                    break;
                case ARROWLINE:
                    g.setColor(aPLB.getColor());
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    //float[] dashPattern = getDashPattern(aPLB.getStyle());
                    //g.setStroke(new BasicStroke(aPLB.getSize(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                    drawPolyline(points, g);

                    int n = points.length;
                    PointF aPoint = points[n - 2];
                    PointF bPoint = points[n - 1];
                    double U = bPoint.X - aPoint.X;
                    double V = bPoint.Y - aPoint.Y;
                    double angle = Math.atan((V) / (U)) * 180 / Math.PI;
                    angle = angle + 90;
                    if (U < 0) {
                        angle = angle + 180;
                    }

                    if (angle >= 360) {
                        angle = angle - 360;
                    }

                    Draw.drawArraw(g, bPoint, angle, 8);
                    break;
            }
        }
    }

    /**
     * Draw polyline
     *
     * @param points The points
     * @param pbc The polyline break collection
     * @param g Graphics2D
     */
    public static void drawPolyline(PointF[] points, ColorBreakCollection pbc, Graphics2D g) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.length);
        PointF p;
        PolylineBreak aPLB;
        List<PointF> drawPs = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            p = points[i];
            if (i == 0) {
                path.moveTo(p.X, p.Y);
            } else {
                path.lineTo(p.X, p.Y);

                aPLB = (PolylineBreak) pbc.get(i);
                Color aColor = aPLB.getColor();
                Float size = aPLB.getWidth();
                float[] dashPattern = getDashPattern(aPLB.getStyle());
                BasicStroke pen = new BasicStroke(size, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f);
                g.setColor(aColor);
                g.setStroke(pen);
                g.draw(path);
                path.reset();
                path.moveTo(p.X, p.Y);
                //Draw symbol            
                if (aPLB.getDrawSymbol()) {
                    Object rend = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    for (int j = 0; j < drawPs.size(); j++) {
                        Draw.drawPoint(aPLB.getSymbolStyle(), p, aPLB.getSymbolFillColor(), aPLB.getSymbolColor(),
                                aPLB.getSymbolSize(), true, aPLB.isFillSymbol(), g);
                    }
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rend);
                }
            }
            drawPs.add(p);
        }
    }

    /**
     * Draw polyline
     *
     * @param points The points
     * @param aPLB The polyline break
     * @param g Graphics2D
     * @param mvIdx Missing value index list
     */
    public static void drawPolyline(PointF[] points, PolylineBreak aPLB, Graphics2D g, List<Integer> mvIdx) {
        if (aPLB.isUsingDashStyle()) {
            g.setColor(aPLB.getColor());
            float[] dashPattern = getDashPattern(aPLB.getStyle());
            g.setStroke(new BasicStroke(aPLB.getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
            if (mvIdx.size() > 0) {
                drawPolyline(points, g, mvIdx);
            } else {
                drawPolyline(points, g);
            }

            //Draw symbol            
            if (aPLB.getDrawSymbol()) {
                Object rend = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Rectangle clip = g.getClipBounds();
                PointF p;
                if (clip != null) {
                    g.setClip(null);
                    for (int i = 0; i < points.length; i++) {
                        p = new PointF(points[i].X, points[i].Y);
                        if (p.X >= clip.x && p.X <= clip.x + clip.width && p.Y >= clip.y && p.Y <= clip.y + clip.height) {
                            if (mvIdx.contains(i)) {
                                continue;
                            }
                            if (i % aPLB.getSymbolInterval() == 0) {
                                drawPoint(aPLB.getSymbolStyle(), p, aPLB.getSymbolFillColor(), aPLB.getSymbolColor(),
                                        aPLB.getSymbolSize(), true, aPLB.isFillSymbol(), g);
                            }
                        }
                    }
                    g.setClip(clip);
                } else {
                    for (int i = 0; i < points.length; i++) {
                        if (mvIdx.contains(i)) {
                            continue;
                        }

                        if (i % aPLB.getSymbolInterval() == 0) {
                            p = new PointF(points[i].X, points[i].Y);
                            drawPoint(aPLB.getSymbolStyle(), p, aPLB.getSymbolFillColor(), aPLB.getSymbolColor(),
                                    aPLB.getSymbolSize(), true, aPLB.isFillSymbol(), g);
                        }
                    }
                }
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rend);
            }
        } else {
            Polyline aPLine = new Polyline();
            aPLine.setPoints(points);
            List<double[]> pos = aPLine.getPositions(30);
            float aSize = 16;
            int i;
            switch (aPLB.getStyle()) {
                case COLDFRONT:
                    if (pos != null) {
                        PointBreak aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(Color.blue);
                        aPB.setStyle(PointStyle.UpTriangle);
                        aPB.setOutlineColor(Color.blue);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 0; i < pos.size(); i++) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple_Up(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }
                    }

                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    drawPolyline(points, g);
                    break;
                case WARMFRONT:
                    if (pos != null) {
                        PointBreak aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(Color.red);
                        aPB.setStyle(PointStyle.UpSemiCircle);
                        aPB.setOutlineColor(Color.red);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 0; i < pos.size(); i++) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }
                    }

                    g.setColor(Color.red);
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    drawPolyline(points, g);
                    break;
                case OCCLUDEDFRONT:
                    Color aColor = new Color(255, 0, 255);
                    if (pos != null) {
                        PointBreak aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(aColor);
                        aPB.setStyle(PointStyle.UpTriangle);
                        aPB.setOutlineColor(aColor);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 0; i < pos.size(); i += 2) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple_Up(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }

                        aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(aColor);
                        aPB.setStyle(PointStyle.UpSemiCircle);
                        aPB.setOutlineColor(aColor);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 1; i < pos.size(); i += 2) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }
                    }

                    g.setColor(aColor);
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    drawPolyline(points, g);
                    break;
                case STATIONARYFRONT:
                    if (pos != null) {
                        PointBreak aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(Color.blue);
                        aPB.setStyle(PointStyle.UpTriangle);
                        aPB.setOutlineColor(Color.blue);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 0; i < pos.size(); i += 2) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple_Up(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }

                        aPB = new PointBreak();
                        aPB.setSize(aSize);
                        aPB.setColor(Color.red);
                        aPB.setStyle(PointStyle.DownSemiCircle);
                        aPB.setOutlineColor(Color.red);
                        aPB.setDrawFill(true);
                        aPB.setDrawOutline(true);
                        for (i = 1; i < pos.size(); i += 2) {
                            aPB.setAngle((float) pos.get(i)[2]);
                            drawPoint_Simple(new PointF((float) pos.get(i)[0], (float) pos.get(i)[1]), aPB, g);
                        }
                    }

                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(aPLB.getWidth()));
                    drawPolyline(points, g);
                    break;
            }
        }
    }

    /**
     * Draw polyline symbol
     *
     * @param aP The point
     * @param width The width
     * @param height The height
     * @param aPLB The polyline break
     * @param g Graphics2D
     */
    public static void drawPolylineSymbol(PointF aP, float width, float height, PolylineBreak aPLB, Graphics2D g) {
        if (aPLB.isUsingDashStyle()) {
            PointF[] points = new PointF[4];
            PointF aPoint = new PointF(0, 0);
            aPoint.X = aP.X - width / 2;
            aPoint.Y = aP.Y + height / 2;
            points[0] = aPoint;
            aPoint = new PointF();
            aPoint.X = aP.X - width / 6;
            aPoint.Y = aP.Y - height / 2;
            points[1] = aPoint;
            aPoint = new PointF();
            aPoint.X = aP.X + width / 6;
            aPoint.Y = aP.Y + height / 2;
            points[2] = aPoint;
            aPoint = new PointF();
            aPoint.X = aP.X + width / 2;
            aPoint.Y = aP.Y - height / 2;
            points[3] = aPoint;

            g.setColor(aPLB.getColor());
            float[] dashPattern = getDashPattern(aPLB.getStyle());
            g.setStroke(new BasicStroke(aPLB.getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));

            if (aPLB.getDrawPolyline()) {
                drawPolyline(points, g);
            }
            g.setStroke(new BasicStroke());

            //Draw symbol
            if (aPLB.getDrawSymbol()) {
                drawPoint(aPLB.getSymbolStyle(), points[1], aPLB.getSymbolFillColor(), aPLB.getSymbolColor(), aPLB.getSymbolSize(), true, aPLB.isFillSymbol(), g);
                drawPoint(aPLB.getSymbolStyle(), points[2], aPLB.getSymbolFillColor(), aPLB.getSymbolColor(), aPLB.getSymbolSize(), true, aPLB.isFillSymbol(), g);
            }
        } else {
            PointF[] points = new PointF[2];
            PointF aPoint = new PointF(0, 0);
            aPoint.X = aP.X - width / 2;
            aPoint.Y = aP.Y;
            points[0] = aPoint;
            aPoint = new PointF();
            aPoint.X = aP.X + width / 2;
            aPoint.Y = aP.Y;
            points[1] = aPoint;
            float lineWidth = 2.0f;
            switch (aPLB.getStyle()) {
                case COLDFRONT:
                    PointBreak aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(Color.blue);
                    aPB.setStyle(PointStyle.UpTriangle);
                    aPB.setOutlineColor(Color.blue);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X - aPB.getSize() * 2 / 3, aP.Y - aPB.getSize() / 4), aPB, g);
                    drawPoint_Simple(new PointF(aP.X + aPB.getSize() * 2 / 3, aP.Y - aPB.getSize() / 4), aPB, g);

                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(lineWidth));
                    drawPolyline(points, g);
                    break;
                case WARMFRONT:
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(Color.red);
                    aPB.setStyle(PointStyle.UpSemiCircle);
                    aPB.setOutlineColor(Color.red);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X - aPB.getSize() * 2 / 3, aP.Y), aPB, g);
                    drawPoint_Simple(new PointF(aP.X + aPB.getSize() * 2 / 3, aP.Y), aPB, g);

                    g.setColor(Color.red);
                    g.setStroke(new BasicStroke(lineWidth));
                    drawPolyline(points, g);
                    break;
                case OCCLUDEDFRONT:
                    Color aColor = new Color(255, 0, 255);
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(aColor);
                    aPB.setStyle(PointStyle.UpTriangle);
                    aPB.setOutlineColor(aColor);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X - aPB.getSize() * 2 / 3, aP.Y - aPB.getSize() / 4), aPB, g);
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(aColor);
                    aPB.setStyle(PointStyle.UpSemiCircle);
                    aPB.setOutlineColor(aColor);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X + aPB.getSize() * 2 / 3, aP.Y), aPB, g);

                    g.setColor(aColor);
                    g.setStroke(new BasicStroke(lineWidth));
                    drawPolyline(points, g);
                    break;
                case STATIONARYFRONT:
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(Color.blue);
                    aPB.setStyle(PointStyle.UpTriangle);
                    aPB.setOutlineColor(Color.blue);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X - aPB.getSize() * 2 / 3, aP.Y - aPB.getSize() / 4), aPB, g);
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(Color.red);
                    aPB.setStyle(PointStyle.DownSemiCircle);
                    aPB.setOutlineColor(Color.red);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X + aPB.getSize() * 2 / 3, aP.Y), aPB, g);

                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(lineWidth));
                    drawPolyline(points, g);
                    break;
                case ARROWLINE:
                    g.setColor(aPLB.getColor());
                    g.setStroke(new BasicStroke(lineWidth));
                    //float[] dashPattern = getDashPattern(aPLB.getStyle());
                    //g.setStroke(new BasicStroke(aPLB.getSize(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                    drawPolyline(points, g);

                    int n = points.length;
                    aPoint = points[n - 2];
                    PointF bPoint = points[n - 1];
                    double U = bPoint.X - aPoint.X;
                    double V = bPoint.Y - aPoint.Y;
                    double angle = Math.atan((V) / (U)) * 180 / Math.PI;
                    angle = angle + 90;
                    if (U < 0) {
                        angle = angle + 180;
                    }

                    if (angle >= 360) {
                        angle = angle - 360;
                    }

                    Draw.drawArraw(g, bPoint, angle, 8);
                    break;
            }
        }
    }

    /**
     * Draw polyline symbol
     *
     * @param aP The point
     * @param width The width
     * @param height The height
     * @param aPLB The polyline break
     * @param g Graphics2D
     */
    public static void drawPolylineSymbol_S(PointF aP, float width, float height, PolylineBreak aPLB, Graphics2D g) {
        if (aPLB.isUsingDashStyle()) {
            PointF[] points = new PointF[2];
            PointF aPoint = new PointF(0, 0);
            aPoint.X = aP.X - width / 2;
            aPoint.Y = aP.Y;
            points[0] = aPoint;
            aPoint = new PointF();
            aPoint.X = aP.X + width / 2;
            aPoint.Y = aP.Y;
            points[1] = aPoint;

            g.setColor(aPLB.getColor());
            float[] dashPattern = getDashPattern(aPLB.getStyle());
            g.setStroke(new BasicStroke(aPLB.getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));

            if (aPLB.getDrawPolyline()) {
                drawPolyline(points, g);
            }
            g.setStroke(new BasicStroke());

            //Draw symbol
            if (aPLB.getDrawSymbol()) {
                drawPoint(aPLB.getSymbolStyle(), aP, aPLB.getSymbolFillColor(), aPLB.getSymbolColor(), aPLB.getSymbolSize(), true, aPLB.isFillSymbol(), g);
            }
        } else {
            PointF[] points = new PointF[2];
            PointF aPoint = new PointF(0, 0);
            aPoint.X = aP.X - width / 2;
            aPoint.Y = aP.Y;
            points[0] = aPoint;
            aPoint = new PointF();
            aPoint.X = aP.X + width / 2;
            aPoint.Y = aP.Y;
            points[1] = aPoint;
            float lineWidth = 2.0f;
            switch (aPLB.getStyle()) {
                case COLDFRONT:
                    PointBreak aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(Color.blue);
                    aPB.setStyle(PointStyle.UpTriangle);
                    aPB.setOutlineColor(Color.blue);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X - aPB.getSize() * 2 / 3, aP.Y - aPB.getSize() / 4), aPB, g);
                    drawPoint_Simple(new PointF(aP.X + aPB.getSize() * 2 / 3, aP.Y - aPB.getSize() / 4), aPB, g);

                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(lineWidth));
                    drawPolyline(points, g);
                    break;
                case WARMFRONT:
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(Color.red);
                    aPB.setStyle(PointStyle.UpSemiCircle);
                    aPB.setOutlineColor(Color.red);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X - aPB.getSize() * 2 / 3, aP.Y), aPB, g);
                    drawPoint_Simple(new PointF(aP.X + aPB.getSize() * 2 / 3, aP.Y), aPB, g);

                    g.setColor(Color.red);
                    g.setStroke(new BasicStroke(lineWidth));
                    drawPolyline(points, g);
                    break;
                case OCCLUDEDFRONT:
                    Color aColor = new Color(255, 0, 255);
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(aColor);
                    aPB.setStyle(PointStyle.UpTriangle);
                    aPB.setOutlineColor(aColor);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X - aPB.getSize() * 2 / 3, aP.Y - aPB.getSize() / 4), aPB, g);
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(aColor);
                    aPB.setStyle(PointStyle.UpSemiCircle);
                    aPB.setOutlineColor(aColor);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X + aPB.getSize() * 2 / 3, aP.Y), aPB, g);

                    g.setColor(aColor);
                    g.setStroke(new BasicStroke(lineWidth));
                    drawPolyline(points, g);
                    break;
                case STATIONARYFRONT:
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(Color.blue);
                    aPB.setStyle(PointStyle.UpTriangle);
                    aPB.setOutlineColor(Color.blue);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X - aPB.getSize() * 2 / 3, aP.Y - aPB.getSize() / 4), aPB, g);
                    aPB = new PointBreak();
                    aPB.setSize(14);
                    aPB.setColor(Color.red);
                    aPB.setStyle(PointStyle.DownSemiCircle);
                    aPB.setOutlineColor(Color.red);
                    aPB.setDrawFill(true);
                    aPB.setDrawOutline(true);
                    drawPoint_Simple(new PointF(aP.X + aPB.getSize() * 2 / 3, aP.Y), aPB, g);

                    g.setColor(Color.blue);
                    g.setStroke(new BasicStroke(lineWidth));
                    drawPolyline(points, g);
                    break;
                case ARROWLINE:
                    g.setColor(aPLB.getColor());
                    g.setStroke(new BasicStroke(lineWidth));
                    //float[] dashPattern = getDashPattern(aPLB.getStyle());
                    //g.setStroke(new BasicStroke(aPLB.getSize(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                    drawPolyline(points, g);

                    int n = points.length;
                    aPoint = points[n - 2];
                    PointF bPoint = points[n - 1];
                    double U = bPoint.X - aPoint.X;
                    double V = bPoint.Y - aPoint.Y;
                    double angle = Math.atan((V) / (U)) * 180 / Math.PI;
                    angle = angle + 90;
                    if (U < 0) {
                        angle = angle + 180;
                    }

                    if (angle >= 360) {
                        angle = angle - 360;
                    }

                    Draw.drawArraw(g, bPoint, angle, 8);
                    break;
            }
        }
    }

//    /**
//     * Draw polygon symbol
//     *
//     * @param aP The point
//     * @param width The width
//     * @param height The height
//     * @param aPGB The polygon break
//     * @param transparencyPerc Transparency percent
//     * @param g Graphics2D
//     */
//    public static void drawPolygonSymbol(PointF aP, float width, float height, PolygonBreak aPGB,
//            int transparencyPerc, Graphics2D g) {
//        int alpha = (int) ((1 - (double) transparencyPerc / 100.0) * 255);
//        Color c = aPGB.getColor();
//        Color aColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
////            Brush aBrush;
////            if (aPGB.UsingHatchStyle)
////                aBrush = new HatchBrush(aPGB.Style, aColor, aPGB.BackColor);
////            else
////                aBrush = new SolidBrush(aColor);
//
//        aP.X = aP.X - width / 2;
//        aP.Y = aP.Y - height / 2;
//        if (aPGB.getDrawFill()) {
//            g.setColor(aColor);
//            g.fill(new Rectangle.Float(aP.X, aP.Y, width, height));
//        }
//        if (aPGB.getDrawOutline()) {
//            g.setColor(aPGB.getOutlineColor());
//            g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
//            g.draw(new Rectangle.Float(aP.X, aP.Y, width, height));
//        }
//    }
    /**
     * Draw polygon symbol
     *
     * @param aP The point
     * @param width The width
     * @param height The height
     * @param aPGB The polygon break
     * @param g Graphics2D
     */
    public static void drawPolygonSymbol(PointF aP, float width, float height, PolygonBreak aPGB,
            Graphics2D g) {
        aP.X = aP.X - width / 2;
        aP.Y = aP.Y - height / 2;
        if (aPGB.isDrawFill()) {
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(), aPGB.getBackColor());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(new Rectangle.Float(aP.X, aP.Y, width, height));
            } else {
                g.setColor(aPGB.getColor());
                g.fill(new Rectangle.Float(aP.X, aP.Y, width, height));
            }
        }
        if (aPGB.isDrawOutline()) {
            g.setColor(aPGB.getOutlineColor());
            g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
            g.draw(new Rectangle.Float(aP.X, aP.Y, width, height));
        }
    }

    /**
     * Draw polygon symbol
     *
     * @param aP The point
     * @param aColor Fill color
     * @param outlineColor Outline color
     * @param width Width
     * @param height Height
     * @param drawFill If draw fill
     * @param drawOutline If draw outline
     * @param g Grahics2D
     */
    public static void drawPolygonSymbol(PointF aP, Color aColor, Color outlineColor,
            float width, float height, Boolean drawFill, Boolean drawOutline, Graphics2D g) {
        aP.X = aP.X - width / 2;
        aP.Y = aP.Y - height / 2;
        if (drawFill) {
            g.setColor(aColor);
            g.fill(new Rectangle.Float(aP.X, aP.Y, width, height));
        }
        if (drawOutline) {
            g.setColor(outlineColor);
            g.draw(new Rectangle.Float(aP.X, aP.Y, width, height));
        }
    }

    /**
     * Draw rectangle
     *
     * @param aPoint Start point
     * @param width Width
     * @param height Height
     * @param aPGB Polygon break
     * @param g Graphics2D
     */
    public static void drawRectangle(PointF aPoint, float width, float height, PolygonBreak aPGB, Graphics2D g) {
        Color aColor = aPGB.getColor();
        if (aPGB.isDrawFill()) {
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(), aPGB.getBackColor());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(new Rectangle.Float(aPoint.X, aPoint.Y, width, height));
            } else {
                g.setColor(aColor);
                g.fill(new Rectangle.Float(aPoint.X, aPoint.Y, width, height));
            }
        }
        if (aPGB.isDrawOutline()) {
            g.setColor(aPGB.getOutlineColor());
            g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
            g.draw(new Rectangle.Float(aPoint.X, aPoint.Y, width, height));
        }
    }

    /**
     * Draw pie
     *
     * @param aPoint Start point
     * @param width Width
     * @param height Height
     * @param startAngle Start angle
     * @param sweepAngle Sweep angle
     * @param aPGB Polygon break
     * @param g Graphics2D
     */
    public static void drawPie(PointF aPoint, float width, float height, float startAngle, float sweepAngle, PolygonBreak aPGB, Graphics2D g) {
        Color aColor = aPGB.getColor();
        if (aPGB.isDrawFill()) {
            g.setColor(aColor);
            g.fill(new Arc2D.Float(aPoint.X, aPoint.Y, width, height, startAngle, sweepAngle, Arc2D.PIE));
        }
        if (aPGB.isDrawOutline()) {
            g.setColor(aPGB.getOutlineColor());
            g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
            g.draw(new Arc2D.Float(aPoint.X, aPoint.Y, width, height, startAngle, sweepAngle, Arc2D.PIE));
        }
    }

    /**
     * Draw curve line
     *
     * @param points The points
     * @param aPLB The polyline break
     * @param g Graphics2D
     */
    public static void drawCurveLine(PointF[] points, PolylineBreak aPLB, Graphics2D g) {
        List<PointD> opoints = new ArrayList<>();
        int i;
        for (i = 0; i < points.length; i++) {
            opoints.add(new PointD(points[i].X, points[i].Y));
        }

        PointD[] rPoints = Spline.cardinalSpline((PointD[]) opoints.toArray(new PointD[opoints.size()]), 5);
        PointF[] dPoints = new PointF[rPoints.length];
        for (i = 0; i < dPoints.length; i++) {
            dPoints[i] = new PointF((float) rPoints[i].X, (float) rPoints[i].Y);
        }

        drawPolyline(dPoints, aPLB, g);
    }

    /**
     * Draw curve line
     *
     * @param points The points list
     * @param g Graphics2D
     */
    public static void drawCurveLine(List<PointF> points, Graphics2D g) {
        PointD[] opoints = new PointD[points.size()];
        int i;
        for (i = 0; i < points.size(); i++) {
            opoints[i] = new PointD(points.get(i).X, points.get(i).Y);
        }

        PointD[] rPoints = Spline.cardinalSpline(opoints, 5);
        PointF[] dPoints = new PointF[rPoints.length];
        for (i = 0; i < dPoints.length; i++) {
            dPoints[i] = new PointF((float) rPoints[i].X, (float) rPoints[i].Y);
        }

        drawPolyline(dPoints, g);
    }

    /**
     * Draw curve line
     *
     * @param points The points
     * @param g Graphics2D
     */
    public static void drawCurveLine(PointF[] points, Graphics2D g) {
        List<PointD> opoints = new ArrayList<>();
        int i;
        for (i = 0; i < points.length; i++) {
            opoints.add(new PointD(points[i].X, points[i].Y));
        }

        PointD[] rPoints = Spline.cardinalSpline((PointD[]) opoints.toArray(), 5);
        PointF[] dPoints = new PointF[rPoints.length];
        for (i = 0; i < dPoints.length; i++) {
            dPoints[i] = new PointF((float) rPoints[i].X, (float) rPoints[i].Y);
        }

        drawPolyline(dPoints, g);
    }

    /**
     * Draw curve polygon
     *
     * @param points The points
     * @param aPGB Polygon break
     * @param g Graphics2D
     */
    public static void drawCurvePolygon(PointF[] points, PolygonBreak aPGB, Graphics2D g) {
        List<PointD> opoints = new ArrayList<>();
        int i;
        for (i = 0; i < points.length; i++) {
            opoints.add(new PointD(points[i].X, points[i].Y));
        }

        PointD[] rPoints = Spline.cardinalSpline((PointD[]) opoints.toArray(new PointD[opoints.size()]), 5);
        PointF[] dPoints = new PointF[rPoints.length];
        for (i = 0; i < dPoints.length; i++) {
            dPoints[i] = new PointF((float) rPoints[i].X, (float) rPoints[i].Y);
        }

        drawPolygon(dPoints, aPGB, g);
    }

    /**
     * Draw circle
     *
     * @param points The points
     * @param aPGB The polygon break
     * @param g Graphics2D
     */
    public static void drawCircle(PointF[] points, PolygonBreak aPGB, Graphics2D g) {
        float radius = Math.abs(points[1].X - points[0].X);

        if (aPGB.isDrawFill()) {
            g.setColor(aPGB.getColor());
            g.fill(new Ellipse2D.Float(points[0].X, points[0].Y - radius, radius * 2, radius * 2));
        }
        if (aPGB.isDrawOutline()) {
            g.setColor(aPGB.getOutlineColor());
            g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
            g.draw(new Ellipse2D.Float(points[0].X, points[0].Y - radius, radius * 2, radius * 2));
        }
    }

    /**
     * Draw ellipse
     *
     * @param points The points
     * @param angle The angle
     * @param aPGB The polygon break
     * @param g Grahpics2D
     */
    public static void drawEllipse(PointF[] points, float angle, PolygonBreak aPGB, Graphics2D g) {
        float sx = Math.min(points[0].X, points[2].X);
        float sy = Math.min(points[0].Y, points[2].Y);
        float width = Math.abs(points[2].X - points[0].X);
        float height = Math.abs(points[2].Y - points[0].Y);

        if (angle != 0) {
            AffineTransform tempTrans = g.getTransform();
            AffineTransform myTrans = AffineTransform.getRotateInstance(Math.toRadians(angle),
                    sx + width / 2 + tempTrans.getTranslateX(), sy + height / 2 + tempTrans.getTranslateY());
            g.setTransform(myTrans);
            sx += tempTrans.getTranslateX();
            sy += tempTrans.getTranslateY();

            if (aPGB.isDrawFill()) {
                g.setColor(aPGB.getColor());
                g.fill(new Ellipse2D.Float(sx, sy, width, height));
            }
            if (aPGB.isDrawOutline()) {
                g.setColor(aPGB.getOutlineColor());
                g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
                g.draw(new Ellipse2D.Float(sx, sy, width, height));
            }

            g.setTransform(tempTrans);
        } else {

            if (aPGB.isDrawFill()) {
                g.setColor(aPGB.getColor());
                g.fill(new Ellipse2D.Float(sx, sy, width, height));
            }
            if (aPGB.isDrawOutline()) {
                g.setColor(aPGB.getOutlineColor());
                g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
                g.draw(new Ellipse2D.Float(sx, sy, width, height));
            }
        }
    }

    /**
     * Draw ellipse
     *
     * @param points The points
     * @param aPGB The polygon break
     * @param g Grahpics2D
     */
    public static void drawEllipse(PointF[] points, PolygonBreak aPGB, Graphics2D g) {
        float sx = Math.min(points[0].X, points[2].X);
        float sy = Math.min(points[0].Y, points[2].Y);
        float width = Math.abs(points[2].X - points[0].X);
        float height = Math.abs(points[2].Y - points[0].Y);

        if (aPGB.isDrawFill()) {
            g.setColor(aPGB.getColor());
            g.fill(new Ellipse2D.Float(sx, sy, width, height));
        }
        if (aPGB.isDrawOutline()) {
            g.setColor(aPGB.getOutlineColor());
            g.setStroke(new BasicStroke(aPGB.getOutlineSize()));
            g.draw(new Ellipse2D.Float(sx, sy, width, height));
        }
    }

    /**
     * Draw selected vertices rectangles
     *
     * @param g Graphics2D
     * @param points The points
     */
    public static void drawSelectedVertices(Graphics2D g, PointF[] points) {
        drawSelectedVertices(g, points, 6, Color.black, Color.cyan);
    }

    /**
     * Draw selected vertices rectangles
     *
     * @param g Graphics2D
     * @param points The points
     * @param size The size
     * @param outlineColor Outline coloe
     * @param fillColor Fill color
     */
    public static void drawSelectedVertices(Graphics2D g, PointF[] points, float size, Color outlineColor, Color fillColor) {
        Rectangle.Float rect = new Rectangle.Float(0, 0, size, size);

        for (PointF aPoint : points) {
            rect.x = aPoint.X - size / 2;
            rect.y = aPoint.Y - size / 2;
            g.setColor(fillColor);
            g.fill(rect);
            g.setColor(outlineColor);
            g.setStroke(new BasicStroke(1));
            g.draw(rect);
        }
    }

    /**
     * Draw selected vertice rectangles
     *
     * @param g Graphics2D
     * @param point The point
     * @param size The size
     * @param outlineColor Outline coloe
     * @param fillColor Fill color
     */
    public static void drawSelectedVertice(Graphics2D g, PointF point, float size, Color outlineColor, Color fillColor) {
        Rectangle.Float rect = new Rectangle.Float(0, 0, size, size);

        rect.x = point.X - size / 2;
        rect.y = point.Y - size / 2;
        g.setColor(fillColor);
        g.fill(rect);
        g.setColor(outlineColor);
        g.setStroke(new BasicStroke(1));
        g.draw(rect);
    }

    /**
     * Draw selected four corner rectangles
     *
     * @param g Graphics2D
     * @param gRect The rectangle
     */
    public static void drawSelectedCorners(Graphics2D g, Rectangle gRect) {
        int size = 6;
        Rectangle rect = new Rectangle(gRect.x - size / 2, gRect.y - size / 2, size, size);
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.y = gRect.y + gRect.height - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.x = gRect.x + gRect.width - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.y = gRect.y - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
    }

    /**
     * Draw selected four bouder edge center rectangles
     *
     * @param g Graphics2D
     * @param gRect The rectangle
     */
    public static void drawSelectedEdgeCenters(Graphics2D g, Rectangle gRect) {
        int size = 6;
        Rectangle rect = new Rectangle(gRect.x + gRect.width / 2 - size / 2, gRect.y - size / 2, size, size);
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.y = gRect.y + gRect.height - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.x = gRect.x - size / 2;
        rect.y = gRect.y + gRect.height / 2 - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
        rect.x = gRect.x + gRect.width - size / 2;
        g.setColor(Color.cyan);
        g.fill(rect);
        g.setColor(Color.black);
        g.draw(rect);
    }
    // </editor-fold>

    // <editor-fold desc="Chart">
    /**
     * Draw chart point
     *
     * @param aPoint Screen point
     * @param aCB Chart break
     * @param g Graphics2D
     */
    public static void drawChartPoint(PointF aPoint, ChartBreak aCB, Graphics2D g) {
        switch (aCB.getChartType()) {
            case BarChart:
                drawBarChartSymbol(aPoint, aCB, g);
                break;
            case PieChart:
                List<String> rStrs = null;
                if (aCB.isDrawLabel()) {
                    List<Float> ratios = aCB.getPieRatios();
                    rStrs = new ArrayList<>();
                    for (float r : ratios) {
                        rStrs.add(String.valueOf((int) (r * 100)) + "%");
                    }
                }
                drawPieChartSymbol(aPoint, aCB, g, rStrs);
                break;
        }

    }

    /**
     * Draw bar chart symbol
     *
     * @param aPoint Start point
     * @param aCB Chart break
     * @param g Graphics2D
     */
    public static void drawBarChartSymbol(PointF aPoint, ChartBreak aCB, Graphics2D g) {
        Font font = new Font("Arial", Font.PLAIN, 8);
        drawBarChartSymbol(aPoint, aCB, g, false, font);
    }

    /**
     * Draw bar chart symbol
     *
     * @param aPoint Start point
     * @param aCB Chart break
     * @param g Graphics2D
     * @param drawValue If draw value
     */
    public static void drawBarChartSymbol(PointF aPoint, ChartBreak aCB, Graphics2D g, boolean drawValue) {
        drawBarChartSymbol(aPoint, aCB, g, drawValue, g.getFont());
    }

    /**
     * Draw bar chart symbol
     *
     * @param sPoint Start point
     * @param aCB Chart break
     * @param g Graphics2D
     * @param drawValue If draw value
     * @param font Value font
     */
    public static void drawBarChartSymbol(PointF sPoint, ChartBreak aCB, Graphics2D g, boolean drawValue, Font font) {
        PointF aPoint = (PointF) sPoint.clone();
        List<Integer> heights = aCB.getBarHeights();
        float y = aPoint.Y;
        for (int i = 0; i < heights.size(); i++) {
            if (heights.get(i) <= 0) {
                aPoint.X += aCB.getBarWidth();
                continue;
            }

            aPoint.Y = y - heights.get(i);
            PolygonBreak aPGB = (PolygonBreak) aCB.getLegendScheme().getLegendBreaks().get(i);
            if (aCB.isView3D()) {
                Color aColor = ColorUtil.modifyBrightness(aPGB.getColor(), 0.5f);
                PointF[] points = new PointF[4];
                points[0] = new PointF(aPoint.X, aPoint.Y);
                points[1] = new PointF(aPoint.X + aCB.getBarWidth(), aPoint.Y);
                points[2] = new PointF(points[1].X + aCB.getThickness(), points[1].Y - aCB.getThickness());
                points[3] = new PointF(points[0].X + aCB.getThickness(), points[0].Y - aCB.getThickness());
                g.setColor(aColor);
                Draw.fillPolygon(points, g, aPGB);
                g.setColor(aPGB.getOutlineColor());
                Draw.drawPolyline(points, g);

                points[0] = new PointF(aPoint.X + aCB.getBarWidth(), aPoint.Y);
                points[1] = new PointF(aPoint.X + aCB.getBarWidth(), aPoint.Y + heights.get(i));
                points[2] = new PointF(points[1].X + aCB.getThickness(), points[1].Y - aCB.getThickness());
                points[3] = new PointF(points[0].X + aCB.getThickness(), points[0].Y - aCB.getThickness());
                g.setColor(aColor);
                Draw.fillPolygon(points, g, aPGB);
                g.setColor(aPGB.getOutlineColor());
                Draw.drawPolyline(points, g);
            }
            drawRectangle(aPoint, aCB.getBarWidth(), heights.get(i), aPGB, g);

            aPoint.X += aCB.getBarWidth();

            if (i == heights.size() - 1) {
                if (drawValue) {
                    //String vstr = String.valueOf(aCB.getChartData().get(i));
                    String formatStr = "%1$." + String.valueOf(aCB.getDecimalDigits()) + "f";
                    String vstr = String.format(formatStr, aCB.getChartData().get(i));
                    FontMetrics metrics = g.getFontMetrics(font);
                    Dimension labSize = new Dimension(metrics.stringWidth(vstr), metrics.getHeight());
                    aPoint.X += 5;
                    aPoint.Y = (float) (y - heights.get(i) / 2);
                    g.setColor(Color.black);
                    g.setFont(font);
                    g.drawString(vstr, aPoint.X, aPoint.Y + metrics.getHeight() / 2);
                }
            }
        }
    }

    /**
     * Draw bar chart symbol
     *
     * @param aPoint Start point
     * @param width Width
     * @param height Height
     * @param g Graphics2D
     * @param aPGB Polygon beak
     * @param isView3D Is view as 3D
     * @param thickness 3D thickness
     */
    public static void drawBar(PointF aPoint, int width, int height, PolygonBreak aPGB, Graphics2D g, boolean isView3D,
            int thickness) {
//        float y = aPoint.Y;
//        aPoint.Y = y - height;
//        if (isView3D) {
//            Color aColor = ColorUtil.modifyBrightness(aPGB.getColor(), 0.5f);
//            PointF[] points = new PointF[4];
//            points[0] = new PointF(aPoint.X, aPoint.Y);
//            points[1] = new PointF(aPoint.X + width, aPoint.Y);
//            points[2] = new PointF(points[1].X + thickness, points[1].Y - thickness);
//            points[3] = new PointF(points[0].X + thickness, points[0].Y - thickness);
//            g.setColor(aColor);
//            Draw.fillPolygon(points, g);
//            g.setColor(aPGB.getOutlineColor());
//            Draw.drawPolyline(points, g);
//
//            points[0] = new PointF(aPoint.X + width, aPoint.Y);
//            points[1] = new PointF(aPoint.X + width, aPoint.Y + height);
//            points[2] = new PointF(points[1].X + thickness, points[1].Y - thickness);
//            points[3] = new PointF(points[0].X + thickness, points[0].Y - thickness);
//            g.setColor(aColor);
//            Draw.fillPolygon(points, g);
//            g.setColor(aPGB.getOutlineColor());
//            Draw.drawPolyline(points, g);
//        }
//        drawRectangle(aPoint, width, height, aPGB, g);
        drawBar(aPoint, (float) width, (float) height, aPGB, g, isView3D, thickness);
    }

    /**
     * Draw bar chart symbol
     *
     * @param aPoint Start point
     * @param width Width
     * @param height Height
     * @param g Graphics2D
     * @param aPGB Polygon beak
     * @param isView3D Is view as 3D
     * @param thickness 3D thickness
     */
    public static void drawBar(PointF aPoint, float width, float height, PolygonBreak aPGB, Graphics2D g, boolean isView3D,
            int thickness) {
        float y = aPoint.Y;
        aPoint.Y = y - height;
        if (isView3D) {
            Color aColor = ColorUtil.modifyBrightness(aPGB.getColor(), 0.5f);
            PointF[] points = new PointF[4];
            points[0] = new PointF(aPoint.X, aPoint.Y);
            points[1] = new PointF(aPoint.X + width, aPoint.Y);
            points[2] = new PointF(points[1].X + thickness, points[1].Y - thickness);
            points[3] = new PointF(points[0].X + thickness, points[0].Y - thickness);
            g.setColor(aColor);
            Draw.fillPolygon(points, g, aPGB);
            g.setColor(aPGB.getOutlineColor());
            Draw.drawPolyline(points, g);

            points[0] = new PointF(aPoint.X + width, aPoint.Y);
            points[1] = new PointF(aPoint.X + width, aPoint.Y + height);
            points[2] = new PointF(points[1].X + thickness, points[1].Y - thickness);
            points[3] = new PointF(points[0].X + thickness, points[0].Y - thickness);
            g.setColor(aColor);
            Draw.fillPolygon(points, g, aPGB);
            g.setColor(aPGB.getOutlineColor());
            Draw.drawPolyline(points, g);
        }
        drawRectangle(aPoint, width, height, aPGB, g);
    }

    /**
     * Draw pie chart symbol
     *
     * @param aPoint Start point
     * @param aCB Chart break
     * @param g Graphics2D
     * @param labels Labels
     */
    public static void drawPieChartSymbol(PointF aPoint, ChartBreak aCB, Graphics2D g, List<String> labels) {
        int width = aCB.getWidth();
        int height = aCB.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        PointF sPoint = new PointF(aPoint.X + width / 2, aPoint.Y - height / 2);
        aPoint.Y -= height;
        List<List<Float>> angles = aCB.getPieAngles();
        float startAngle, sweepAngle;
        int i;
        if (aCB.isView3D()) {
            aPoint.Y = aPoint.Y + width / 6 - aCB.getThickness();
            for (i = 0; i < angles.size(); i++) {
                startAngle = angles.get(i).get(0);
                sweepAngle = angles.get(i).get(1);
                PolygonBreak aPGB = (PolygonBreak) aCB.getLegendScheme().getLegendBreaks().get(i);
                if (startAngle + sweepAngle > 180) {
                    PointF bPoint = new PointF(aPoint.X, aPoint.Y + aCB.getThickness());
                    Color aColor = ColorUtil.modifyBrightness(aPGB.getColor(), 0.5f);

                    g.setColor(aColor);
                    g.fill(new Arc2D.Float(bPoint.X, bPoint.Y, width, width * 2 / 3, startAngle, sweepAngle, Arc2D.PIE));
                    g.setColor(aPGB.getOutlineColor());
                    g.draw(new Arc2D.Float(bPoint.X, bPoint.Y, width, width * 2 / 3, startAngle, sweepAngle, Arc2D.PIE));
                }
            }
            float a = (float) width / 2;
            float b = (float) width / 3;
            float x0 = aPoint.X + a;
            float y0 = aPoint.Y + b;
            double sA, eA;
            for (i = 0; i < angles.size(); i++) {
                startAngle = angles.get(i).get(0);
                sweepAngle = angles.get(i).get(1);
                if (startAngle + sweepAngle > 180) {
                    sA = (360 - startAngle) / 180 * Math.PI;
                    eA = (360 - (startAngle + sweepAngle)) / 180 * Math.PI;
                    PolygonBreak aPGB = (PolygonBreak) aCB.getLegendScheme().getLegendBreaks().get(i);
                    PointF bPoint = MIMath.calEllipseCoordByAngle(x0, y0, a, b, eA);
                    PointF cPoint = new PointF(x0 - a, y0);
                    if (sA < Math.PI) {
                        cPoint = MIMath.calEllipseCoordByAngle(x0, y0, a, b, sA);
                    }

                    Color aColor = ColorUtil.modifyBrightness(aPGB.getColor(), 0.5f);
                    PointF[] points = new PointF[5];
                    points[0] = cPoint;
                    points[1] = new PointF(cPoint.X, cPoint.Y + aCB.getThickness());
                    points[2] = new PointF(bPoint.X, bPoint.Y + aCB.getThickness());
                    points[3] = bPoint;
                    points[4] = cPoint;
                    g.setColor(aColor);
                    Draw.fillPolygon(points, g, aPGB);
                    g.setColor(aPGB.getOutlineColor());
                    g.draw(new Line2D.Float(points[0].X, points[0].Y, points[1].X, points[1].Y));
                    g.draw(new Line2D.Float(points[2].X, points[2].Y, points[3].X, points[3].Y));
                }
            }
            for (i = 0; i < angles.size(); i++) {
                startAngle = angles.get(i).get(0);
                sweepAngle = angles.get(i).get(1);
                PolygonBreak aPGB = (PolygonBreak) aCB.getLegendScheme().getLegendBreaks().get(i);
                drawPie(aPoint, width, width * 2 / 3, startAngle, sweepAngle, aPGB, g);
            }
        } else {
            for (i = 0; i < angles.size(); i++) {
                startAngle = angles.get(i).get(0);
                sweepAngle = angles.get(i).get(1);
                PolygonBreak aPGB = (PolygonBreak) aCB.getLegendScheme().getLegendBreaks().get(i);
                drawPie(aPoint, width, width, startAngle, sweepAngle, aPGB, g);
            }
            if (labels != null) {
                FontMetrics metrics = g.getFontMetrics();
                float x, y, angle, w, h;
                for (i = 0; i < angles.size(); i++) {
                    String label = labels.get(i);
                    if (label.equals("0%")) {
                        continue;
                    }

                    startAngle = angles.get(i).get(0);
                    sweepAngle = angles.get(i).get(1);
                    angle = startAngle + sweepAngle / 2;
                    PointF lPoint = getPieLabelPoint(sPoint, width / 2, angle);
                    x = lPoint.X;
                    y = lPoint.Y;
                    h = metrics.getHeight();
                    w = metrics.stringWidth(label);
                    if ((angle >= 0 && angle < 45) || (angle >= 315 && angle <= 360)) {
                        x = x + 3;
                        y = y + h / 2;
                    } else if (angle >= 45 && angle < 90) {
                        y = y - 3;
                    } else if (angle >= 90 && angle < 135) {
                        x = x - w - 3;
                        y = y - 3;
                    } else if (angle >= 135 && angle < 225) {
                        x = x - w - 3;
                        y = y + h / 2;
                    } else if (angle >= 225 && angle < 270) {
                        x = x - w - 3;
                        y = y + h / 2;
                    } else {
                        y = y + h;
                    }
                    g.drawString(label, x, y);
                }
            }
        }
    }

    /**
     * Get pie wedge label point
     *
     * @param sPoint Center point
     * @param r Radius
     * @param angle Angle
     * @return Label point
     */
    public static PointF getPieLabelPoint(PointF sPoint, float r, float angle) {
        float x = (float) (sPoint.X + r * Math.cos(angle * Math.PI / 180));
        float y = (float) (sPoint.Y - r * Math.sin(angle * Math.PI / 180));
        return new PointF(x, y);
    }
    // </editor-fold>
}
