/* Copyright 2016 - Yaqiang Wang,
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
package org.meteoinfo.chart.plot;

import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.axis.LogAxis;
import org.meteoinfo.chart.axis.TimeAxis;
import org.meteoinfo.chart.graphic.WebMapImage;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointD;
import org.meteoinfo.common.PointF;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.render.java2d.Draw;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.graphic.ImageGraphic;
import org.meteoinfo.geometry.graphic.Line2DGraphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.Polygon;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.geometry.geoprocess.GeometryUtil;
import org.meteoinfo.chart.shape.PolylineErrorShape;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.meteoinfo.render.java2d.Draw.getHatchImage;

/**
 *
 * @author Yaqiang Wang
 */
public class Plot2D extends AbstractPlot2D {

    // <editor-fold desc="Variables">
    protected GraphicCollection graphics;
    protected Graphic lastAddedGraphic;
    protected float barsWidth = 0.8f;

    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Plot2D() {
        super();
        this.graphics = new GraphicCollection();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get graphics
     *
     * @return Graphics
     */
    public GraphicCollection getGraphics() {
        return this.graphics;
    }

    /**
     * Get the number of graphics
     * @return The number of graphics
     */
    public int getGraphicNumber() {
        return this.graphics.size();
    }

    /**
     * Set graphics
     *
     * @param value Graphics
     */
    public void setGraphics(GraphicCollection value) {
        this.graphics = value;
    }
    
    /**
     * Get bars width (0 - 1), only used for automatic bar width.
     * @return Bars width
     */
    public float getBarsWidth(){
        return this.barsWidth;
    }
    
    /**
     * Set bars width (0 - 1), only used for automatic bar width.
     * @param value Bars width
     */
    public void setBarsWidth(float value){
        this.barsWidth = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a graphic
     *
     * @param g Graphic
     */
    public Graphic addGraphic(Graphic g) {
        this.graphics.add(g);
        this.lastAddedGraphic = g;
        return g;
    }

    /**
     * Add a graphic by index
     *
     * @param idx Index
     * @param g Graphic
     */
    public Graphic addGraphic(int idx, Graphic g) {
        this.graphics.add(idx, g);
        this.lastAddedGraphic = g;
        return g;
    }

    /**
     * Remove a graphic
     *
     * @param g Graphic
     */
    public void removeGraphic(Graphic g) {
        this.graphics.remove(g);
    }

    /**
     * Remove a graphic by index
     *
     * @param idx Index
     */
    public void removeGraphic(int idx) {
        this.graphics.remove(idx);
    }

    /**
     * Remove last graphic
     */
    public void removeLastGraphic() {
        this.graphics.remove(this.graphics.size() - 1);
    }

    /**
     * Remove last added graphic
     */
    public void removeLastAddedGraphic() {
        if (this.lastAddedGraphic != null) {
            this.graphics.remove(this.lastAddedGraphic);
        }
    }

    /**
     * Add graphic list
     *
     * @param gs Graphic list
     */
    public void addGraphics(List<Graphic> gs) {
        this.graphics.addAll(gs);
    }

    @Override
    protected void drawGraph(Graphics2D g, Rectangle2D area) {
        AffineTransform oldMatrix = g.getTransform();
        java.awt.Shape oldRegion = g.getClip();
        if (this.clip) {
            g.setClip(area);
        }
        g.translate(area.getX(), area.getY());

        plotGraphics(g, area);

        g.setTransform(oldMatrix);
        if (this.clip) {
            g.setClip(oldRegion);
        }
    }
    
    private boolean isPiePlot(){
        boolean isPie = false;
        int n = 0;
        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m).getGraphicN(0);
            ShapeTypes st = graphic.getShape().getShapeType();
            switch (st){
                case ARC:
                    isPie = true;
                    n += 1;
                    break;
            }
        }
        //return isPie && n == 1;
        return isPie;
    }
    
    void plotPie(Graphics2D g, Rectangle2D area){
        AffineTransform oldMatrix = g.getTransform();
        g.translate(area.getX(), area.getY());

        //Draw background
        if (this.background != null) {
            g.setColor(this.getBackground());
            g.fill(new Rectangle2D.Double(0, 0, area.getWidth(), area.getHeight()));
        }

        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            ColorBreak cb = graphic.getLegend();
            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                if (!graphic.isSingleLegend()) {
                    cb = gg.getLegend();
                }
                Shape shape = gg.getShape();
                switch (shape.getShapeType()) {
                    case POINT:
                    case POINT_M:
                    case POINT_Z:
                        this.drawPoint(g, (PointShape) shape, (PointBreak) cb, area);
                        break;
                    case TEXT:
                        this.drawText((ChartText)shape, g, area);
                        break;
                    case POLYLINE:
                    case POLYLINE_Z:
                        if (cb instanceof PointBreak) {
                            this.drawPolyline(g, (PolylineShape) shape, (PointBreak) cb, area);
                        } else {
                            this.drawPolyline(g, (PolylineShape) shape, (PolylineBreak) cb, area);
                        }
                        break;
                    case POLYGON:
                    case POLYGON_Z:
                        for (Polygon poly : ((PolygonShape) shape).getPolygons()) {
                            drawPolygon(g, poly, (PolygonBreak) cb, false, area);
                        }
                        break;
                    case RECTANGLE:
                        this.drawRectangle(g, (RectangleShape) shape, (PolygonBreak) cb, false, area);
                        break;
                    case ARC:
                        this.drawArc(g, (ArcShape) shape, (PolygonBreak) cb, area);
                        break;
                    case WIND_BARB:
                        this.drawWindBarb(g, (WindBarb) shape, (PointBreak) cb, area);
                        break;
                    case WIND_ARROW:
                        this.drawWindArrow(g, (WindArrow) shape, (ArrowBreak) cb, area);
                        break;
                    case IMAGE:
                        this.drawImage(g, gg, area);
                        break;
                }
            }
            if (graphic instanceof GraphicCollection) {
                GraphicCollection gc = (GraphicCollection) graphic;
                if (gc.getLabelSet().isDrawLabels()) {
                    this.drawLabels(g, gc, area);
                }
            }
        }

        g.setTransform(oldMatrix);
    }
    
    protected void plotGraphics(Graphics2D g, Rectangle2D area) {
        int barIdx = 0;
        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            if (graphic.isVisible()) {
                ColorBreak cb = graphic.getLegend();
                ShapeTypes shapeType = graphic.getGraphicN(0).getShape().getShapeType();
                switch (shapeType) {
                    case BAR:
                        this.drawBars(g, (GraphicCollection) graphic, barIdx, area);
                        barIdx += 1;
                        continue;
                }

                if (graphic.getExtent().intersects(this.drawExtent)) {
                    drawGraphics(g, graphic, area);
                }
            }
        }
    }

    protected void drawGraphics(Graphics2D g, Graphic graphic, Rectangle2D area) {
        if (this.antiAlias || graphic.isAntiAlias()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
        }

        java.awt.Shape oldClip = g.getClip();
        if (graphic.isClip()) {
            GeneralPath clipPath = getClipPath(graphic.getClipGraphic(), area);
            g.setClip(clipPath);
            if (oldClip != null) {
                g.clip(oldClip);
            }
        }

        ColorBreak cb = graphic.getLegend();
        if (graphic instanceof GraphicCollection) {
            if (((GraphicCollection) graphic).isAvoidCollision() && graphic.getShapeType().isPoint()) {
                List<Extent> extentList = new ArrayList<>();
                Extent maxExtent = new Extent();
                Extent ext = new Extent();
                PointD point;
                double[] xy;
                float size;
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    if (gg.getExtent().intersects(this.drawExtent)) {
                        if (!graphic.isSingleLegend()) {
                            cb = gg.getLegend();
                        }
                        PointShape shape = (PointShape) gg.getShape();
                        PointBreak pointBreak = (PointBreak) cb;
                        point = shape.getPoint();
                        xy = projToScreen(point.X, point.Y, area);
                        size = pointBreak.getSize() / 2;
                        ext.minX = xy[0] - size;
                        ext.maxX = xy[0] + size;
                        ext.minY = xy[1] - size;
                        ext.maxY = xy[1] + size;
                        if (extentList.isEmpty()) {
                            maxExtent = (Extent) ext.clone();
                            extentList.add((Extent) ext.clone());
                            drawGraphic(g, gg, cb, area);
                        } else if (!MIMath.isExtentCross(ext, maxExtent)) {
                            extentList.add((Extent) ext.clone());
                            maxExtent = MIMath.getLagerExtent(maxExtent, ext);
                            drawGraphic(g, gg, cb, area);
                        } else {
                            boolean ifDraw = true;
                            for (int j = 0; j < extentList.size(); j++) {
                                if (MIMath.isExtentCross(ext, extentList.get(j))) {
                                    ifDraw = false;
                                    break;
                                }
                            }
                            if (ifDraw) {
                                extentList.add((Extent) ext.clone());
                                maxExtent = MIMath.getLagerExtent(maxExtent, ext);
                                drawGraphic(g, gg, cb, area);
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    if (gg.getExtent().intersects(this.drawExtent)) {
                        if (!graphic.isSingleLegend()) {
                            cb = gg.getLegend();
                        }
                        drawGraphic(g, gg, cb, area);
                    }
                }
            }

            GraphicCollection gc = (GraphicCollection) graphic;
            if (gc.getLabelSet().isDrawLabels()) {
                this.drawLabels(g, gc, area);
            }
        } else {
            if (graphic.getExtent().intersects(this.drawExtent)) {
                drawGraphic(g, graphic, cb, area);
            }
        }

        if (graphic.isClip()) {
            g.setClip(oldClip);
        }
    }

    void drawGraphic(Graphics2D g, Graphic graphic, Rectangle2D area) {
        ColorBreak cb = graphic.getLegend();
        drawGraphic(g, graphic, cb, area);
    }

    protected void drawGraphic(Graphics2D g, Graphic graphic, ColorBreak cb, Rectangle2D area) {
        if (graphic instanceof Line2DGraphic) {
            this.drawLine2D(g, (Line2DGraphic) graphic, area);
            return;
        }

        Shape shape = graphic.getShape();
        switch (shape.getShapeType()) {
            case POINT:
            case POINT_M:
            case POINT_Z:
                this.drawPoint(g, (PointShape) shape, (PointBreak) cb, area);
                break;
            case TEXT:
                this.drawText((ChartText)shape, g, area);
                break;
            case POLYLINE:
            case POLYLINE_Z:
                if (shape instanceof CapPolylineShape){
                    this.drawCapPolyline(g, (CapPolylineShape) shape, (PolylineBreak) cb, area);
                } else {
                    switch (cb.getBreakType()){
                        case POINT_BREAK:
                            this.drawPolyline(g, (PolylineShape) shape, (PointBreak) cb, area);
                            break;
                        case POLYLINE_BREAK:
                            this.drawPolyline(g, (PolylineShape) shape, (PolylineBreak) cb, area);
                            break;
                        case COLOR_BREAK_COLLECTION:
                            this.drawPolyline(g, (PolylineShape) shape, (ColorBreakCollection) cb, area);
                            break;
                    }
                }
                break;
            case CURVE_LINE:
                this.drawCurveline(g, (CurveLineShape) shape, (PolylineBreak) cb, area);
                break;
            case POLYLINE_ERROR:
                if (cb instanceof PointBreak) {
                    this.drawPolylineError(g, (PolylineErrorShape) shape, (PointBreak) cb, area);
                } else {
                    this.drawPolylineError(g, (PolylineErrorShape) shape, (PolylineBreak) cb, area);
                }
                break;
            case POLYGON:
            case POLYGON_Z:
                for (Polygon poly : ((PolygonShape) shape).getPolygons()) {
                    drawPolygon(g, poly, (PolygonBreak) cb, false, area);
                }
                break;
            case RECTANGLE:
                this.drawRectangle(g, (RectangleShape) shape, (PolygonBreak) cb, false, area);
                break;
            case CIRCLE:
                this.drawCircle(g, (CircleShape) shape, (PolygonBreak) cb, false, area);
                break;
            case ELLIPSE:
                this.drawEllipse(g, (EllipseShape) shape, (PolygonBreak) cb, false, area);
                break;
            case ARC:
                this.drawArc(g, (ArcShape) shape, (PolygonBreak) cb, area);
                break;
            case WIND_BARB:
                this.drawWindBarb(g, (WindBarb) shape, (PointBreak) cb, area);
                break;
            case WIND_ARROW:
                this.drawWindArrow(g, (WindArrow) shape, (ArrowBreak) cb, area);
                break;
            case IMAGE:
                this.drawImage(g, graphic, area);
                break;
        }
    }

    protected GeneralPath getClipPath(Graphic graphic, Rectangle2D area) {
        GeneralPath clipPath = new GeneralPath();
        double[] xy;
        if (graphic.isCollection()) {
            for (PolygonShape aPGS : (List<PolygonShape>) ((GraphicCollection) graphic).getShapes()) {
                for (Polygon aPolygon : aPGS.getPolygons()) {
                    GeneralPath aPath = new GeneralPath();
                    PointD wPoint;
                    double[] sXY;
                    for (int i = 0; i < aPolygon.getOutLine().size(); i++) {
                        wPoint = aPolygon.getOutLine().get(i);
                        xy = projToScreen(wPoint.X, wPoint.Y, area);
                        if (i == 0) {
                            aPath.moveTo(xy[0], xy[1]);
                        } else {
                            aPath.lineTo(xy[0], xy[1]);
                        }
                    }
                    clipPath.append(aPath, false);
                }
            }
        } else {
            for (Polygon aPolygon : ((PolygonShape) graphic.getShape()).getPolygons()) {
                GeneralPath aPath = new GeneralPath();
                PointD wPoint;
                double[] sXY;
                for (int i = 0; i < aPolygon.getOutLine().size(); i++) {
                    wPoint = aPolygon.getOutLine().get(i);
                    xy = projToScreen(wPoint.X, wPoint.Y, area);
                    if (i == 0) {
                        aPath.moveTo(xy[0], xy[1]);
                    } else {
                        aPath.lineTo(xy[0], xy[1]);
                    }
                }
                clipPath.append(aPath, false);
            }
        }

        return clipPath;
    }

    void drawPoint(Graphics2D g, PointShape aPS, PointBreak aPB, Rectangle2D area) {
        PointD p = aPS.getPoint();
        double[] sXY = projToScreen(p.X, p.Y, area);
        PointF pf = new PointF((float) sXY[0], (float) sXY[1]);
        RenderingHints rend = g.getRenderingHints();
        boolean rc = false;
        if (this.symbolAntiAlias && rend.get(RenderingHints.KEY_ANTIALIASING) != RenderingHints.VALUE_ANTIALIAS_ON) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            rc = true;
        }
        Draw.drawPoint(pf, aPB, g);
        if (rc){
            g.setRenderingHints(rend);
        }
    }
    
    void drawText(ChartText text, Graphics2D g, Rectangle2D area) {
        float x, y;
        switch (text.getCoordinates()) {
            case AXES:
                x = (float) (area.getWidth() * text.getX());
                y = (float) (area.getHeight() * (1 - text.getY()));
                this.drawText(g, text, x, y);
                break;
            case FIGURE:
                x = (float) (area.getWidth() * text.getX());
                y = (float) (area.getHeight() * (1 - text.getY()));
                this.drawText(g, text, x, y);
                break;
            case DATA:
                double[] xy = this.projToScreen(text.getX(), text.getY(), area);
                x = (float) xy[0];
                y = (float) xy[1];
                this.drawText(g, text, x, y);
                break;
        }
    }
    
    void drawText(Graphics2D g, ChartText text, float x, float y) {
        g.setFont(text.getFont());
        g.setColor(text.getColor());
        switch (text.getYAlign()) {
            case TOP:
                y += g.getFontMetrics(g.getFont()).getAscent();
                break;
            case CENTER:
                y += g.getFontMetrics(g.getFont()).getAscent() / 2;
                break;
        }
        String s = text.getText();
        switch (text.getXAlign()) {
            case RIGHT:
                x = x - g.getFontMetrics(g.getFont()).stringWidth(s);
                break;
            case CENTER:
                x = x - g.getFontMetrics(g.getFont()).stringWidth(s) * 0.5f;
                break;
        }
        Draw.drawString(g, s, x, y, text.isUseExternalFont());
    }

    void drawWindBarb(Graphics2D g, WindBarb aPS, PointBreak aPB, Rectangle2D area) {
        PointD p = aPS.getPoint();
        double[] sXY = projToScreen(p.X, p.Y, area);
        PointF pf = new PointF((float) sXY[0], (float) sXY[1]);
        Draw.drawWindBarb(pf, aPS, aPB, g);
    }

    void drawWindArrow(Graphics2D g, WindArrow aPS, ArrowBreak aPB, Rectangle2D area) {
        PointD p = aPS.getPoint();
        double[] sXY = projToScreen(p.X, p.Y, area);
        PointF pf = new PointF((float) sXY[0], (float) sXY[1]);
        float zoom = aPB.getSize() / 10;
        Draw.drawArrow(pf, aPS, aPB, g, zoom);
    }

    void drawLine2D(Graphics2D g, Line2DGraphic line2D, Rectangle2D area) {
        if (line2D.isCurve()) {
            drawCurveline(g, (PolylineShape) line2D.getShape(), (PolylineBreak) line2D.getLegend(), area);
        } else {
            BreakTypes breakType = line2D.getLegend().getBreakType();
            switch (breakType) {
                case POINT_BREAK:
                    drawPolyline(g, (PolylineShape) line2D.getShape(), (PointBreak) line2D.getLegend(), area);
                    break;
                case COLOR_BREAK_COLLECTION:
                    drawPolyline(g, (PolylineShape) line2D.getShape(), (ColorBreakCollection) line2D.getLegend(), area);
                    break;
                default:
                    drawPolyline(g, (PolylineShape) line2D.getShape(), (PolylineBreak) line2D.getLegend(), area);
                    break;
            }
        }
    }

    void drawPolyline(Graphics2D g, PolylineShape aPLS, PointBreak aPB, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF p;
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                p = new PointF((float) sXY[0], (float) sXY[1]);
                Draw.drawPoint(p, aPB, g);
            }
        }
    }

    void drawPolyline(Graphics2D g, PolylineShape aPLS, PolylineBreak aPLB, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF[] points = new PointF[aline.getPointList().size()];
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                points[i] = new PointF((float) sXY[0], (float) sXY[1]);
            }
            Draw.drawPolyline(points, aPLB, g);
        }
    }
    
    void drawPolyline(Graphics2D g, PolylineShape aPLS, ColorBreakCollection cpc, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF[] points = new PointF[aline.getPointList().size()];
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                points[i] = new PointF((float) sXY[0], (float) sXY[1]);
            }
            if (cpc.get(0) instanceof StreamlineBreak) {
                Draw.drawStreamline(points, cpc, g);
            } else {
                Draw.drawPolyline(points, cpc, g);
            }
        }
    }
    
    void drawCapPolyline(Graphics2D g, CapPolylineShape aPLS, PolylineBreak aPLB, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF[] points = new PointF[aline.getPointList().size()];
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                points[i] = new PointF((float) sXY[0], (float) sXY[1]);
            }
            Draw.drawPolyline(points, aPLB, g);
            float capLen = aPLS.getCapLen();
            int idx = points.length - 1;
            if (aPLS.getCapAngle() == 0){
                PointF[] ps = new PointF[2];
                ps[0] = new PointF(points[0].X - capLen / 2, points[0].Y);
                ps[1] = new PointF(points[0].X + capLen / 2, points[0].Y);
                Draw.drawPolyline(ps, aPLB, g);
                ps = new PointF[2];
                ps[0] = new PointF(points[idx].X - capLen / 2, points[idx].Y);
                ps[1] = new PointF(points[idx].X + capLen / 2, points[idx].Y);
                Draw.drawPolyline(ps, aPLB, g);
            } else {
                PointF[] ps = new PointF[2];
                ps[0] = new PointF(points[0].X, points[0].Y - capLen / 2);
                ps[1] = new PointF(points[0].X, points[0].Y + capLen / 2);
                Draw.drawPolyline(ps, aPLB, g);
                ps = new PointF[2];
                ps[0] = new PointF(points[idx].X, points[idx].Y - capLen / 2);
                ps[1] = new PointF(points[idx].X, points[idx].Y + capLen / 2);
                Draw.drawPolyline(ps, aPLB, g);
            }
        }
    }
    
    void drawCurveline(Graphics2D g, PolylineShape aPLS, PolylineBreak aPLB, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF[] points = new PointF[aline.getPointList().size()];
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                points[i] = new PointF((float) sXY[0], (float) sXY[1]);
            }
            Draw.drawCurveLine(points, aPLB, g);
        }
    }

    void drawPolylineError(Graphics2D g, PolylineErrorShape aPLS, PointBreak aPB, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF p;
            double error;
            double elen = 6;
            g.setColor(aPB.getColor());
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                p = new PointF((float) sXY[0], (float) sXY[1]);
                if (aPLS.getYerror() != null) {
                    error = aPLS.getYerror(i);
                    error = this.projYLength(error, area);
                    g.draw(new Line2D.Double(p.X, p.Y - error, p.X, p.Y + error));
                    g.draw(new Line2D.Double(p.X - (elen * 0.5), p.Y - error, p.X + (elen * 0.5), p.Y - error));
                    g.draw(new Line2D.Double(p.X - (elen * 0.5), p.Y + error, p.X + (elen * 0.5), p.Y + error));
                }
                if (aPLS.getXerror() != null) {
                    error = aPLS.getXerror(i);
                    error = this.projXLength(error, area);
                    g.draw(new Line2D.Double(p.X - error, p.Y, p.X + error, p.Y));
                    g.draw(new Line2D.Double(p.X - error, p.Y - (elen * 0.5), p.X - error, p.Y + (elen * 0.5)));
                    g.draw(new Line2D.Double(p.X + error, p.Y - (elen * 0.5), p.X + error, p.Y + (elen * 0.5)));
                }
                Draw.drawPoint(p, aPB, g);
            }
        }
    }

    void drawPolylineError(Graphics2D g, PolylineErrorShape aPLS, PolylineBreak aPLB, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF[] points = new PointF[aline.getPointList().size()];
            PointF p;
            double error;
            double elen = 6;
            g.setColor(aPLB.getColor());
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                p = new PointF((float) sXY[0], (float) sXY[1]);
                points[i] = p;
                if (aPLS.getYerror() != null) {
                    error = aPLS.getYerror(i);
                    error = this.projYLength(error, area);
                    g.draw(new Line2D.Double(p.X, p.Y - error, p.X, p.Y + error));
                    g.draw(new Line2D.Double(p.X - (elen * 0.5), p.Y - error, p.X + (elen * 0.5), p.Y - error));
                    g.draw(new Line2D.Double(p.X - (elen * 0.5), p.Y + error, p.X + (elen * 0.5), p.Y + error));
                }
                if (aPLS.getXerror() != null) {
                    error = aPLS.getXerror(i);
                    error = this.projXLength(error, area);
                    g.draw(new Line2D.Double(p.X - error, p.Y, p.X + error, p.Y));
                    g.draw(new Line2D.Double(p.X - error, p.Y - (elen * 0.5), p.X - error, p.Y + (elen * 0.5)));
                    g.draw(new Line2D.Double(p.X + error, p.Y - (elen * 0.5), p.X + error, p.Y + (elen * 0.5)));
                }
            }
            Draw.drawPolyline(points, aPLB, g);
        }
    }

    void drawLabels(Graphics2D g, GraphicCollection graphics, Rectangle2D area) {
        Extent lExtent = graphics.getExtent();
        Extent drawExtent = this.getDrawExtent();
        if (!MIMath.isExtentCross(lExtent, drawExtent)) {
            return;
        }

        Font drawFont;
        List<Extent> extentList = new ArrayList<>();
        Extent maxExtent = new Extent();
        Extent aExtent;
        int i, j;
        List<Graphic> LabelPoints = graphics.getLabelPoints();
        String LabelStr;
        PointF aPoint = new PointF();

        for (i = 0; i < LabelPoints.size(); i++) {
            Graphic aLP = LabelPoints.get(i);
            PointShape aPS = (PointShape) aLP.getShape();
            LabelBreak aLB = (LabelBreak) aLP.getLegend();
            aPS.setVisible(true);
            LabelStr = aLB.getText();
            aPoint.X = (float) aPS.getPoint().X;
            aPoint.Y = (float) aPS.getPoint().Y;
            drawFont = aLB.getFont();
            if (aPoint.X < drawExtent.minX || aPoint.X > drawExtent.maxX
                    || aPoint.Y < drawExtent.minY || aPoint.Y > drawExtent.maxY) {
                continue;
            }
            double[] xy = projToScreen(aPoint.X, aPoint.Y, area);
            aPoint.X = (float) xy[0];
            aPoint.Y = (float) xy[1];
            FontMetrics metrics = g.getFontMetrics(drawFont);
            Dimension labSize = new Dimension(metrics.stringWidth(LabelStr), metrics.getHeight());
            switch (aLB.getAlignType()) {
                case CENTER:
                    aPoint.X = (float) xy[0] - labSize.width / 2;
                    break;
                case LEFT:
                    aPoint.X = (float) xy[0] - labSize.width;
                    break;
            }
            aPoint.Y += labSize.height / 2;
            aPoint.Y -= aLB.getYShift();
            aPoint.X += aLB.getXShift();

            AffineTransform tempTrans = g.getTransform();
            if (aLB.getAngle() != 0) {
                //AffineTransform myTrans = new AffineTransform();
                AffineTransform myTrans = (AffineTransform)tempTrans.clone();
                myTrans.translate(aPoint.X, aPoint.Y);
                myTrans.rotate(aLB.getAngle() * Math.PI / 180);
                g.setTransform(myTrans);
                aPoint.X = 0;
                aPoint.Y = 0;
            }

            boolean ifDraw = true;
            Rectangle rect = this.getGraphicRectangle(g, aLP, area);
            aExtent = new Extent();
            aExtent.minX = rect.x;
            aExtent.maxX = rect.x + rect.width;
            aExtent.minY = rect.y;
            aExtent.maxY = rect.y + rect.height;
            if (graphics.getLabelSet().isAvoidCollision()) {
                //Judge extent                                        
                if (extentList.isEmpty()) {
                    maxExtent = (Extent) aExtent.clone();
                    extentList.add(aExtent);
                } else if (!MIMath.isExtentCross(aExtent, maxExtent)) {
                    extentList.add(aExtent);
                    maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                } else {
                    for (j = 0; j < extentList.size(); j++) {
                        if (MIMath.isExtentCross(aExtent, extentList.get(j))) {
                            ifDraw = false;
                            break;
                        }
                    }
                    if (ifDraw) {
                        extentList.add(aExtent);
                        maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                    } else {
                        aPS.setVisible(false);
                    }
                }
            }

            if (ifDraw) {
                if (graphics.getLabelSet().isDrawShadow()) {
                    g.setColor(graphics.getLabelSet().getShadowColor());
                    g.fill(new Rectangle.Float((float) aExtent.minX, (float) aExtent.minY, labSize.width, labSize.height));
                }
                g.setFont(drawFont);
                //g.setColor(aLayer.getLabelSet().getLabelColor());
                g.setColor(aLP.getLegend().getColor());
                g.drawString(LabelStr, aPoint.X, aPoint.Y);

                //Draw selected rectangle
                if (aPS.isSelected()) {
                    float[] dashPattern = new float[]{2.0F, 1.0F};
                    g.setColor(Color.cyan);
                    g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                    g.draw(new Rectangle.Float((float) aExtent.minX, (float) aExtent.minY, labSize.width, labSize.height));
                }
            }

            if (aLB.getAngle() != 0) {
                g.setTransform(tempTrans);
            }
        }
    }

    /**
     * Get graphic rectangle
     *
     * @param g The graphics
     * @param aGraphic The graphic
     * @param area Area
     * @return Rectangle
     */
    public Rectangle getGraphicRectangle(Graphics2D g, Graphic aGraphic, Rectangle2D area) {
        Rectangle rect = new Rectangle();
        double[] sXY;
        float aX, aY;
        switch (aGraphic.getShape().getShapeType()) {
            case POINT:
            case POINT_M:
                PointShape aPS = (PointShape) aGraphic.getShape();
                sXY = projToScreen(aPS.getPoint().X, aPS.getPoint().Y, area);
                aX = (float) sXY[0];
                aY = (float) sXY[1];
                switch (aGraphic.getLegend().getBreakType()) {
                    case POINT_BREAK:
                        PointBreak aPB = (PointBreak) aGraphic.getLegend();
                        int buffer = (int) aPB.getSize() + 2;
                        rect.x = (int) aX - buffer / 2;
                        rect.y = (int) aY - buffer / 2;
                        rect.width = buffer;
                        rect.height = buffer;
                        break;
                    case LABEL_BREAK:
                        LabelBreak aLB = (LabelBreak) aGraphic.getLegend();
                        g.setFont(aLB.getFont());
                        //FontMetrics metrics = g.getFontMetrics(aLB.getFont());
                        //Dimension labSize = new Dimension(metrics.stringWidth(aLB.getText()), metrics.getHeight());
                        Dimension labSize = Draw.getStringDimension(aLB.getText(), g);
                        switch (aLB.getAlignType()) {
                            case CENTER:
                                aX = aX - labSize.width / 2;
                                break;
                            case LEFT:
                                aX = aX - labSize.width;
                                break;
                        }
                        aY -= aLB.getYShift();
                        aY -= labSize.height / 3;
                        rect.x = (int) aX;
                        rect.y = (int) aY;
                        rect.width = (int) labSize.width;
                        rect.height = (int) labSize.height;
                        break;
                }
                break;
            case POLYLINE:
            case POLYGON:
            case RECTANGLE:
            case CURVE_LINE:
            case ELLIPSE:
            case CIRCLE:
            case CURVE_POLYGON:
                List<PointD> newPList = (List<PointD>) aGraphic.getShape().getPoints();
                List<PointD> points = new ArrayList<>();
                for (PointD wPoint : newPList) {
                    sXY = projToScreen(wPoint.X, wPoint.Y, area);
                    aX = (float) sXY[0];
                    aY = (float) sXY[1];
                    points.add(new PointD(aX, aY));
                }
                Extent aExtent = GeometryUtil.getPointsExtent(points);
                rect.x = (int) aExtent.minX;
                rect.y = (int) aExtent.minY;
                rect.width = (int) (aExtent.maxX - aExtent.minX);
                rect.height = (int) (aExtent.maxY - aExtent.minY);
                break;
        }

        return rect;
    }

    List<PointF> drawPolygon(Graphics2D g, Polygon aPG, PolygonBreak aPGB,
                                     boolean isSelected, Rectangle2D area) {
        int len = aPG.getOutLine().size();
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, len);
        PointD wPoint;
        double[] sXY;
        List<PointF> rPoints = new ArrayList<>();
        for (int i = 0; i < aPG.getOutLine().size(); i++) {
            wPoint = aPG.getOutLine().get(i);
            sXY = projToScreen(wPoint.X, wPoint.Y, area);
            if (i == 0) {
                path.moveTo(sXY[0], sXY[1]);
            } else {
                path.lineTo(sXY[0], sXY[1]);
            }
            rPoints.add(new PointF((float) sXY[0], (float) sXY[1]));
        }

        List<PointD> newPList;
        if (aPG.hasHole()) {
            for (int h = 0; h < aPG.getHoleLines().size(); h++) {
                newPList = (List<PointD>)aPG.getHoleLines().get(h);
                for (int j = 0; j < newPList.size(); j++) {
                    wPoint = newPList.get(j);
                    sXY = projToScreen(wPoint.X, wPoint.Y, area);
                    if (j == 0) {
                        path.moveTo(sXY[0], sXY[1]);
                    } else {
                        path.lineTo(sXY[0], sXY[1]);
                    }
                }
            }
        }
        path.closePath();

        if (aPGB.isDrawFill()) {
            //int alpha = (int)((1 - (double)transparencyPerc / 100.0) * 255);
            //Color aColor = Color.FromArgb(alpha, aPGB.Color);
            Color aColor = aPGB.getColor();
            if (isSelected) {
                aColor = this.getSelectedColor();
            }
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(),
                        aPGB.getBackColor(), aPGB.getStyleLineWidth());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(path);
            } else {
                g.setColor(aColor);
                g.fill(path);
            }
        } else if (isSelected) {
            g.setColor(this.getSelectedColor());
            g.fill(path);
        }

        if (aPGB.isDrawOutline()) {
            BasicStroke pen = new BasicStroke(aPGB.getOutlineSize());
            g.setStroke(pen);
            g.setColor(aPGB.getOutlineColor());
            g.draw(path);
        }

        return rPoints;
    }
    
    void drawRectangle(Graphics2D g, RectangleShape rs, PolygonBreak aPGB,
            boolean isSelected, Rectangle2D area) {
        Extent extent = rs.getExtent();
        double[] xy = projToScreen(extent.minX, extent.minY + extent.getHeight(), area);
        double x = xy[0];
        double y = xy[1];
        xy = projToScreen(extent.maxX, extent.minY, area);
        double width = Math.abs(xy[0] - x);
        double height = Math.abs(xy[1] - y);
        RectangularShape rshape;
        if (rs.isRound())
            rshape = new RoundRectangle2D.Double(x, y, width, height, width * rs.getRoundX(), height * rs.getRoundY());
        else
            rshape = new Rectangle2D.Double(x, y, width, height);
        
        if (aPGB.isDrawFill()) {
            Color aColor = aPGB.getColor();
            if (isSelected) {
                aColor = this.getSelectedColor();
            }
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(),
                        aPGB.getBackColor(), aPGB.getStyleLineWidth());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(rshape);
            } else {
                g.setColor(aColor);
                g.fill(rshape);
            }
        } else if (isSelected) {
            g.setColor(this.getSelectedColor());
            g.fill(rshape);
        }

        if (aPGB.isDrawOutline()) {
            BasicStroke pen = new BasicStroke(aPGB.getOutlineSize());
            g.setStroke(pen);
            g.setColor(aPGB.getOutlineColor());
            g.draw(rshape);
        }
    }

    void drawCircle(Graphics2D g, CircleShape rs, PolygonBreak aPGB,
                               boolean isSelected, Rectangle2D area) {
        Extent extent = rs.getExtent();
        double[] sXY;
        sXY = projToScreen(extent.minX, extent.minY + extent.getHeight(), area);
        double x = sXY[0];
        double y = sXY[1];
        double width = this.projXLength(extent.getWidth(), area);
        java.awt.Shape shape = new Ellipse2D.Double(x, y, width, width);

        if (aPGB.isDrawFill()) {
            Color aColor = aPGB.getColor();
            if (isSelected) {
                aColor = this.getSelectedColor();
            }
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(),
                        aPGB.getBackColor(), aPGB.getStyleLineWidth());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(shape);
            } else {
                g.setColor(aColor);
                g.fill(shape);
            }
        } else if (isSelected) {
            g.setColor(this.getSelectedColor());
            g.fill(shape);
        }

        if (aPGB.isDrawOutline()) {
            BasicStroke pen = new BasicStroke(aPGB.getOutlineSize());
            g.setStroke(pen);
            g.setColor(aPGB.getOutlineColor());
            g.draw(shape);
        }
    }

    void drawEllipse(Graphics2D g, EllipseShape rs, PolygonBreak aPGB,
                            boolean isSelected, Rectangle2D area) {
        Extent extent = rs.getExtent();
        double[] sXY;
        sXY = projToScreen(extent.minX, extent.minY + extent.getHeight(), area);
        double x = sXY[0];
        double y = sXY[1];
        double width = this.projXLength(extent.getWidth(), area);
        double height = this.projYLength(extent.getHeight(), area);
        java.awt.Shape shape = new Ellipse2D.Double(x, y, width, height);

        AffineTransform atf = g.getTransform();
        if (rs.getAngle() != 0) {
            AffineTransform newATF = (AffineTransform) atf.clone();
            newATF.translate(x + width / 2, y + height / 2);
            newATF.rotate(Math.toRadians(rs.getAngle()));
            g.setTransform(newATF);
            shape = new Ellipse2D.Double(-width * 0.5, -height * 0.5, width, height);
        }

        if (aPGB.isDrawFill()) {
            Color aColor = aPGB.getColor();
            if (isSelected) {
                aColor = this.getSelectedColor();
            }
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(),
                        aPGB.getBackColor(), aPGB.getStyleLineWidth());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(shape);
            } else {
                g.setColor(aColor);
                g.fill(shape);
            }
        } else if (isSelected) {
            g.setColor(this.getSelectedColor());
            g.fill(shape);
        }

        if (aPGB.isDrawOutline()) {
            BasicStroke pen = new BasicStroke(aPGB.getOutlineSize());
            g.setStroke(pen);
            g.setColor(aPGB.getOutlineColor());
            g.draw(shape);
        }

        if (rs.getAngle() != 0) {
            g.setTransform(atf);
        }
    }
    
    void drawArc(Graphics2D g, ArcShape aShape, PolygonBreak aPGB,
            Rectangle2D area, float dist, float ex, Font labelFont, Color labelColor) {
        float startAngle = aShape.getStartAngle();
        float sweepAngle = aShape.getSweepAngle();
        float angle = startAngle + sweepAngle / 2;
        float space = 20;
        Rectangle2D rect = new Rectangle2D.Double(area.getX() + ex + space, area.getY() + ex + space, area.getWidth() - ex - space,
                area.getHeight() - ex - space);
        double dx = 0, dy = 0;
        if (aShape.getExplode() > 0) {
            dx = ex * Math.cos((360 - angle) * Math.PI / 180);
            dy = ex * Math.sin((360 - angle) * Math.PI / 180);
            rect.setRect(rect.getX() + dx, rect.getY() + dy, rect.getWidth(), rect.getHeight());
        }
        float sx = (float) (rect.getX() - area.getX());
        float sy = (float) (rect.getY() - area.getY());
        Draw.drawPie(new PointF(sx, sy), (float) rect.getWidth(), (float) rect.getHeight(), 
                startAngle, sweepAngle, aPGB, g);

        float x, y, w, h;
        PointF sPoint = new PointF((float) (rect.getWidth() * 0.5 + sx), (float) (rect.getHeight() * 0.5 + sy));
        String label = aPGB.getCaption();
        if (angle > 360) {
            angle = angle - 360;
        }
        float r = (float) (rect.getWidth() * 0.5) + dist;
        PointF lPoint = Draw.getPieLabelPoint(sPoint, r, angle);
        x = lPoint.X;
        y = lPoint.Y;
        Dimension dim = Draw.getStringDimension(label, g);
        h = dim.height;
        w = dim.width;
        if ((angle >= 0 && angle < 45)) {
            //x = x + dis;
            y = y - h;
        } else if (angle >= 45 && angle < 90) {
            //y = y - dis;
        } else if (angle >= 90 && angle < 135) {
            x = x - w;
            //y = y - dis;
        } else if (angle >= 135 && angle < 225) {
            x = x - w - 3;
            y = y + h / 2;
        } else if (angle >= 225 && angle < 270) {
            x = x - w / 2;
            y = y + h;
        } else if (angle >= 270 && angle < 315) {
            //x = x + dis;
            y = y + h;
        } else {
            //x = x + dis;
            y = y + h / 2;
        }
        g.setFont(labelFont);
        g.setColor(labelColor);
        //g.drawOval((int)(x - 3), (int)(y - 3), 6, 6);
        g.drawString(label, x, y);
    }

    void drawArc(Graphics2D g, ArcShape aShape, PolygonBreak aPGB,
            Rectangle2D area) {
        float startAngle = aShape.getStartAngle();
        float sweepAngle = aShape.getSweepAngle();
        Extent extent = aShape.getExtent();
        double[] sXY;
        sXY = projToScreen(extent.minX, extent.minY + extent.getHeight(), area);
        double x = sXY[0];
        double y = sXY[1];
        double width = this.projXLength(extent.getWidth(), area);
        double height = this.projYLength(extent.getHeight(), area);
        Float wedgeWidth = aShape.getWedgeWidth();
        if (wedgeWidth == null) {
            Draw.drawArc(new PointF((float)x, (float)y),
                (float) width, (float) height, startAngle, sweepAngle, aPGB, g, aShape.getClosure());
        } else {
            wedgeWidth = (float)this.projXLength(wedgeWidth, area);
            Draw.drawPie(new PointF((float)x, (float)y),
                (float) width, (float) height, startAngle, sweepAngle, aPGB, wedgeWidth, g);
        }
    }

    void drawBar(Graphics2D g, BarShape bar, BarBreak bb, float width, Rectangle2D area) {
        double[] xy;
        xy = this.projToScreen(0, 0, area);
        float y0 = (float) xy[1];
        width = (float) this.projXLength(width, area);
        xy = projToScreen(bar.getPoint().X, bar.getPoint().Y, area);
        double x = xy[0];
        double y = xy[1];
        float height;
        height = Math.abs((float) (y - y0));
        float yb = y0;
        if (y >= y0) {
            yb += height;
        }
        Draw.drawBar(new PointF((float) x, yb), width, height, bb, g, false, 5);
    }

    int getBarSeriesNum() {
        int n = 0;
        for (Graphic g : this.graphics.getGraphics()) {
            if (g.getGraphicN(0).getShape().getShapeType() == ShapeTypes.BAR) {
                n += 1;
            }
        }
        return n;
    }

    protected void drawBars(Graphics2D g, GraphicCollection bars, int barIdx, Rectangle2D area) {
        double[] xy;
        xy = this.projToScreen(0, 0, area);
        float y0 = (float) xy[1];
        int len = bars.getNumGraphics();
        PointF[] points = new PointF[len];
        for (int i = 0; i < len; i++) {
            BarShape bs = (BarShape) bars.getGraphicN(i).getShape();
            xy = this.projToScreen(bs.getPoint().X, bs.getPoint().Y, area);
            points[i] = new PointF((float) xy[0], (float) xy[1]);
        }
        float width;
        int barSeriesN = this.getBarSeriesNum();
        BarShape bs1 = (BarShape) bars.getGraphicN(0).getShape();
        if (bs1.isAutoWidth()) {
            if (len > 1) {
                width = (float) ((points[1].X - points[0].X) * this.barsWidth) / barSeriesN;
            } else {
                width = (float) (area.getWidth() / 10) / barSeriesN;
            }
            float height;
            BarBreak bb;
            for (int i = 0; i < len; i++) {
                BarShape bs = (BarShape) bars.getGraphicN(i).getShape();
                bb = (BarBreak) bars.getGraphicN(i).getLegend();
                height = Math.abs((float) (points[i].Y - y0));
                float yBottom = y0;
                if (bs.isDrawBottom()) {
                    xy = this.projToScreen(bs.getPoint().X, bs.getBottom(), area);
                    yBottom = (float) xy[1];
                }
                float yb = yBottom;
                if (points[i].Y >= y0) {
                    yb += height;
                }
                Draw.drawBar(new PointF(points[i].X - width * barSeriesN / 2
                        + barIdx * width, yb), width, height, bb, g, false, 5);
                if (bs.isDrawError()) {
                    PointF p = (PointF) points[i].clone();
                    p.Y -= y0 - yBottom;
                    double elen = 6;
                    double error = bs.getError();
                    error = this.projYLength(error, area);
                    double x = p.X - width * barSeriesN / 2
                            + barIdx * width + width / 2;
                    g.setColor(bb.getErrorColor());
                    g.draw(new Line2D.Double(x, p.Y - error, x, p.Y + error));
                    g.draw(new Line2D.Double(x - (elen * 0.5), p.Y - error, x + (elen * 0.5), p.Y - error));
                    g.draw(new Line2D.Double(x - (elen * 0.5), p.Y + error, x + (elen * 0.5), p.Y + error));
                }
            }
        } else {
            width = (float) this.projXLength(bs1.getWidth(), area);
            float height;
            BarBreak bb;
            for (int i = 0; i < len; i++) {
                BarShape bs = (BarShape) bars.getGraphicN(i).getShape();
                bb = (BarBreak) bars.getGraphicN(i).getLegend();
                height = Math.abs((float) (points[i].Y - y0));
                float yBottom = y0;
                if (bs.isDrawBottom()) {
                    xy = this.projToScreen(bs.getPoint().X, bs.getBottom(), area);
                    yBottom = (float) xy[1];
                }
                float yb = yBottom;
                if (points[i].Y >= y0) {
                    yb += height;
                }
                Draw.drawBar(new PointF(points[i].X, yb), width, height, bb, g, false, 5);
                if (bs.isDrawError()) {
                    PointF p = (PointF) points[i].clone();
                    p.Y -= y0 - yBottom;
                    double elen = 6;
                    double error = bs.getError();
                    error = this.projYLength(error, area);
                    double x = p.X + width / 2;
                    g.setColor(bb.getErrorColor());
                    g.draw(new Line2D.Double(x, p.Y - error, x, p.Y + error));
                    g.draw(new Line2D.Double(x - (elen * 0.5), p.Y - error, x + (elen * 0.5), p.Y - error));
                    g.draw(new Line2D.Double(x - (elen * 0.5), p.Y + error, x + (elen * 0.5), p.Y + error));
                }
            }
        }

        //Draw baseline
        boolean drawBaseline = true;
        if (drawBaseline) {
            g.setColor(Color.black);
            g.draw(new Line2D.Double(0, y0, area.getWidth(), y0));
        }
    }

    double getBarXInterval(int idx) {
        Graphic gg = this.graphics.get(idx);
        if (gg.getNumGraphics() == 1) {
            if (gg.getGraphicN(0).getShape().getPoints().get(0).X == 0) {
                return 1;
            } else {
                return gg.getGraphicN(0).getShape().getPoints().get(0).X / 10;
            }
        } else {
            return gg.getGraphicN(1).getShape().getPoints().get(0).X
                    - gg.getGraphicN(0).getShape().getPoints().get(0).X;
        }
    }

    private int getBarIndex() {
        int idx = -1;
        for (int i = 0; i < this.graphics.size(); i++) {
            if (this.graphics.get(i) instanceof WebMapImage) {
                continue;
            }
            if (this.graphics.get(i).getGraphicN(0).getShape().getShapeType() == ShapeTypes.BAR) {
                idx = i;
                break;
            }
        }
        return idx;
    }
    
    private int getImageIndex() {
        int idx = -1;
        for (int i = 0; i < this.graphics.size(); i++) {
            if (this.graphics.get(i) instanceof WebMapImage) {
                continue;
            }
            if (this.graphics.get(i).getGraphicN(0).getShape().getShapeType() == ShapeTypes.IMAGE) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    void drawImage(Graphics2D g, Graphic igraphic, Rectangle2D area) {
        ImageShape ishape = (ImageShape) igraphic.getShape();
        BufferedImage image = ishape.getImage();
        //double sx = ishape.getPoint().X, sy = ishape.getPoint().Y + image.getHeight();
        Extent extent = ishape.getExtent();
        double sx = extent.minX, sy = extent.maxY;
        double[] xy1 = this.projToScreen(sx, sy, area);
        double ex = extent.maxX, ey = extent.minY;
        //double[] xy2 = this.projToScreen(sx + image.getWidth(), ishape.getPoint().Y, area);
        double[] xy2 = this.projToScreen(ex, ey, area);
        int x = (int) xy1[0];
        int y = (int) xy1[1];
        int width = (int) (xy2[0] - xy1[0]);
        int height = (int) (xy2[1] - xy1[1]);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, ishape.getInterpolation());
        g.drawImage(image, x, y, width, height, null);
    }

    @Override
    protected Extent getAutoExtent() {
        this.graphics.updateExtent();
        Extent extent = (Extent)this.graphics.getExtent().clone();
        if (extent.minX == extent.maxX) {
            if (extent.minX == 0) {
                extent.minX = -1;
                extent.maxX = 1;
            } else {
                extent.minX = extent.minX - Math.abs(extent.minX);
                extent.maxX = extent.maxX + Math.abs(extent.minX);
            }
        }
        if (extent.minY == extent.maxY) {
            if (extent.minY == 0) {
                extent.minY = -1;
                extent.maxY = 1;
            } else {
                extent.minY = extent.minY - Math.abs(extent.minY);
                extent.maxY = extent.maxY + Math.abs(extent.maxY);
            }
        }        
        
        int imageIdx = this.getImageIndex();
        if (imageIdx >= 0){
            return extent;
        }

        int barIdx = this.getBarIndex();
        if (barIdx >= 0) {
            double dx = getBarXInterval(barIdx);
            extent.minX -= dx;
            extent.maxX += dx;
        }
        double[] xValues;
        if (this.getXAxis() instanceof TimeAxis) {
            xValues = (double[]) MIMath.getIntervalValues(extent.minX, extent.maxX, false).get(0);
            xValues[0] = extent.minX;
            xValues[xValues.length - 1] = extent.maxX;
        } else if (this.getXAxis() instanceof LogAxis) {
            xValues = (double[]) MIMath.getIntervalValues_Log(extent.minX, extent.maxX);
        } else {
            xValues = (double[]) MIMath.getIntervalValues(extent.minX, extent.maxX, true).get(0);
        }
        double[] yValues;
        if (this.getYAxis() instanceof LogAxis) {
            yValues = (double[]) MIMath.getIntervalValues_Log(extent.minY, extent.maxY);
        } else {
            yValues = (double[]) MIMath.getIntervalValues(extent.minY, extent.maxY, true).get(0);
        }
        if (this.getPlotOrientation() == PlotOrientation.VERTICAL) {
            return new Extent(xValues[0], xValues[xValues.length - 1], yValues[0], yValues[yValues.length - 1]);
        } else {
            return new Extent(yValues[0], yValues[yValues.length - 1], xValues[0], xValues[xValues.length - 1]);
        }
    }

    /**
     * Set auto extent
     */
    @Override
    public void setAutoExtent() {
        Extent extent = this.getAutoExtent();
        if (!this.fixDrawExtent)
            this.setDrawExtent(extent);
        this.setExtent((Extent) extent.clone());
    }

    @Override
    public void updateLegendScheme() {
        if (this.getLegend() == null) {
            this.setLegend(new ChartLegend(this.getLegendScheme()));
        } else {
            this.getLegend().setLegendScheme(this.getLegendScheme());
        }
    }

    @Override
    public Dataset getDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDataset(Dataset dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Get legend scheme
     *
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        LegendScheme ls = null;
        if (lastAddedGraphic != null) {
            if (lastAddedGraphic instanceof ImageGraphic) {
                ls = ((ImageGraphic) lastAddedGraphic).getLegendScheme();
            } else if (lastAddedGraphic instanceof GraphicCollection) {
                ls = ((GraphicCollection) lastAddedGraphic).getLegendScheme();
            }
        }

        if (ls == null) {
            int n = this.graphics.getNumGraphics();
            for (int i = n - 1; i >= 0; i--) {
                Graphic g = this.graphics.getGraphicN(i);
                if (g instanceof ImageGraphic) {
                    ls = ((ImageGraphic) g).getLegendScheme();
                    break;
                } else if (g instanceof GraphicCollection) {
                    ls = ((GraphicCollection) g).getLegendScheme();
                    break;
                }
            }
        }

        if (ls == null) {
            ShapeTypes stype = ShapeTypes.POLYLINE;
            ls = new LegendScheme(stype);
            for (Graphic g : this.graphics.getGraphics()) {
                if (g.getShapeType() == ShapeTypes.POLYLINE) {
                    ls.getLegendBreaks().add(g.getLegend());
                }
            }
        }
        return ls;
    }
    // </editor-fold>

}
