/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.data.ArrayUtil;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.XYListDataset;
import org.meteoinfo.data.analysis.Statistics;
import org.meteoinfo.drawing.ContourDraw;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.legend.PointStyle;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.layer.ImageLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.ArrowBreak;
import org.meteoinfo.legend.ArrowLineBreak;
import org.meteoinfo.legend.ArrowPolygonBreak;
import org.meteoinfo.legend.BarBreak;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.ColorBreakCollection;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.ma.ArrayBoolean;
import org.meteoinfo.shape.ArcShape;
import org.meteoinfo.shape.BarShape;
import org.meteoinfo.shape.CapPolylineShape;
import org.meteoinfo.shape.CurveLineShape;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PointZShape;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.PolygonZShape;
import org.meteoinfo.shape.Polyline;
import org.meteoinfo.shape.PolylineErrorShape;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.PolylineZ;
import org.meteoinfo.shape.PolylineZShape;
import org.meteoinfo.shape.RectangleShape;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;
import org.meteoinfo.shape.WindArrow;
import org.meteoinfo.shape.WindArrow3D;
import org.meteoinfo.shape.WindBarb;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import wContour.Global.PolyLine;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicFactory {

    /**
     * Create LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cb Color break
     * @return LineString graphic
     */
    public static GraphicCollection createLineString(Array xdata, Array ydata, ColorBreak cb) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        List<PointD> points = new ArrayList<>();
        double x, y;
        for (int i = 0; i < xdata.getSize(); i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            if (Double.isNaN(y) || Double.isNaN(x)) {
                if (points.isEmpty()) {
                    continue;
                }
                if (points.size() == 1) {
                    points.add((PointD) points.get(0).clone());
                }
                pls = new PolylineShape();
                pls.setPoints(points);
                gc.add(new Graphic(pls, cb));
                points = new ArrayList<>();
            } else {
                points.add(new PointD(x, y));
            }
        }
        if (!points.isEmpty()) {
            if (points.size() == 1) {
                points.add((PointD) points.get(0).clone());
            }
            pls = new PolylineShape();
            pls.setPoints(points);
            gc.add(new Graphic(pls, cb));
        }

        return gc;
    }

    /**
     * Create LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cb Color break
     * @param iscurve Is curve line or not
     * @return LineString graphic
     */
    public static GraphicCollection createLineString(Array xdata, Array ydata, ColorBreak cb, boolean iscurve) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        List<PointD> points = new ArrayList<>();
        double x, y;
        if (xdata.getRank() == 1) {
            for (int i = 0; i < xdata.getSize(); i++) {
                x = xdata.getDouble(i);
                y = ydata.getDouble(i);
                if (Double.isNaN(y) || Double.isNaN(x)) {
                    if (points.isEmpty()) {
                        continue;
                    }
                    if (points.size() == 1) {
                        points.add((PointD) points.get(0).clone());
                    }
                    if (iscurve) {
                        pls = new CurveLineShape();
                    } else {
                        pls = new PolylineShape();
                    }
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                    points = new ArrayList<>();
                } else {
                    points.add(new PointD(x, y));
                }
            }
            if (!points.isEmpty()) {
                if (points.size() == 1) {
                    points.add((PointD) points.get(0).clone());
                }
                if (iscurve) {
                    pls = new CurveLineShape();
                } else {
                    pls = new PolylineShape();
                }
                pls.setPoints(points);
                gc.add(new Graphic(pls, cb));
            }
        } else {    //Two dimensions
            int[] shape = xdata.getShape();
            int yn = shape[0];
            int xn = shape[1];
            for (int j = 0; j < yn; j++) {
                points = new ArrayList<>();
                for (int i = 0; i < xn; i++) {
                    x = xdata.getDouble(j * xn + i);
                    y = ydata.getDouble(j * xn + i);
                    if (Double.isNaN(y) || Double.isNaN(x)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() == 1) {
                            points.add((PointD) points.get(0).clone());
                        }
                        if (iscurve) {
                            pls = new CurveLineShape();
                        } else {
                            pls = new PolylineShape();
                        }
                        pls.setPoints(points);
                        gc.add(new Graphic(pls, cb));
                        points = new ArrayList<>();
                    } else {
                        points.add(new PointD(x, y));
                    }
                }
                if (points.size() > 1) {
                    if (iscurve) {
                        pls = new CurveLineShape();
                    } else {
                        pls = new PolylineShape();
                    }
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                }
            }
        }

        return gc;
    }

    /**
     * Create LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cbs Color break list
     * @param iscurve Is curve line or not
     * @return LineString graphic
     */
    public static GraphicCollection createLineString(Array xdata, Array ydata, List<ColorBreak> cbs, boolean iscurve) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        List<PointD> points;
        double x, y;
        ColorBreak cb = cbs.get(0);
        if (xdata.getRank() == 1) {
            points = new ArrayList<>();
            ColorBreakCollection cbc = new ColorBreakCollection();
            for (int i = 0; i < xdata.getSize(); i++) {
                x = xdata.getDouble(i);
                y = ydata.getDouble(i);
                if (cbs.size() > i) {
                    cb = cbs.get(i);
                }
                if (Double.isNaN(y) || Double.isNaN(x)) {
                    if (points.isEmpty()) {
                        continue;
                    }
                    if (points.size() == 1) {
                        points.add((PointD) points.get(0).clone());
                    }
                    if (iscurve) {
                        pls = new CurveLineShape();
                    } else {
                        pls = new PolylineShape();
                    }
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cbc));
                    points = new ArrayList<>();
                    cbc = new ColorBreakCollection();
                } else {
                    points.add(new PointD(x, y));
                    cbc.add(cb);
                }
            }
            if (points.size() > 1) {
                if (iscurve) {
                    pls = new CurveLineShape();
                } else {
                    pls = new PolylineShape();
                }
                pls.setPoints(points);
                gc.add(new Graphic(pls, cbc));
            }
        } else {    //Two dimensions
            int[] shape = xdata.getShape();
            int yn = shape[0];
            int xn = shape[1];
            for (int j = 0; j < yn; j++) {
                points = new ArrayList<>();
                cb = cbs.get(j);
                for (int i = 0; i < xn; i++) {
                    x = xdata.getDouble(j * xn + i);
                    y = ydata.getDouble(j * xn + i);
                    if (Double.isNaN(y) || Double.isNaN(x)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() == 1) {
                            points.add((PointD) points.get(0).clone());
                        }
                        if (iscurve) {
                            pls = new CurveLineShape();
                        } else {
                            pls = new PolylineShape();
                        }
                        pls.setPoints(points);
                        gc.add(new Graphic(pls, cb));
                        points = new ArrayList<>();
                    } else {
                        points.add(new PointD(x, y));
                    }
                }
                if (points.size() > 1) {
                    if (iscurve) {
                        pls = new CurveLineShape();
                    } else {
                        pls = new PolylineShape();
                    }
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                }
            }
            gc.setSingleLegend(false);
        }

        return gc;
    }

    /**
     * Create LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param ls Legend scheme
     * @param iscurve Is curve line or not
     * @return LineString graphic
     */
    public static GraphicCollection createLineString(Array xdata, Array ydata, Array zdata, LegendScheme ls, boolean iscurve) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        List<PointD> points;
        ColorBreakCollection cbc;
        double x, y, z;
        ColorBreak cb;
        if (xdata.getRank() == 1) {
            points = new ArrayList<>();
            cbc = new ColorBreakCollection();
            for (int i = 0; i < xdata.getSize(); i++) {
                x = xdata.getDouble(i);
                y = ydata.getDouble(i);
                z = zdata.getDouble(i);
                cb = ls.findLegendBreak(z);
                if (Double.isNaN(y) || Double.isNaN(x)) {
                    if (points.isEmpty()) {
                        continue;
                    }
                    if (points.size() == 1) {
                        points.add((PointD) points.get(0).clone());
                    }
                    if (iscurve) {
                        pls = new CurveLineShape();
                    } else {
                        pls = new PolylineShape();
                    }
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cbc));
                    points = new ArrayList<>();
                    cbc = new ColorBreakCollection();
                } else {
                    points.add(new PointD(x, y));
                    cbc.add(cb);
                }
            }
            if (points.size() > 1) {
                if (iscurve) {
                    pls = new CurveLineShape();
                } else {
                    pls = new PolylineShape();
                }
                pls.setPoints(points);
                gc.add(new Graphic(pls, cbc));
            }
            gc.setLegendScheme(ls);
        } else {    //Two dimensions
            int[] shape = xdata.getShape();
            int yn = shape[0];
            int xn = shape[1];
            for (int j = 0; j < yn; j++) {
                points = new ArrayList<>();
                cbc = new ColorBreakCollection();
                for (int i = 0; i < xn; i++) {
                    x = xdata.getDouble(j * xn + i);
                    y = ydata.getDouble(j * xn + i);
                    z = zdata.getDouble(j * xn + i);
                    cb = ls.findLegendBreak(z);
                    if (Double.isNaN(y) || Double.isNaN(x)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() == 1) {
                            points.add((PointD) points.get(0).clone());
                        }
                        if (iscurve) {
                            pls = new CurveLineShape();
                        } else {
                            pls = new PolylineShape();
                        }
                        pls.setPoints(points);
                        gc.add(new Graphic(pls, cbc));
                        points = new ArrayList<>();
                        cbc = new ColorBreakCollection();
                    } else {
                        points.add(new PointD(x, y));
                        cbc.add(cb);
                    }
                }
                if (points.size() > 1) {
                    if (iscurve) {
                        pls = new CurveLineShape();
                    } else {
                        pls = new PolylineShape();
                    }
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cbc));
                }
            }
            gc.setLegendScheme(ls);
            gc.setSingleLegend(false);
        }

        return gc;
    }

    /**
     * Create LineString graphic
     *
     * @param data Y data array
     * @param cbs Color breaks
     * @return LineString graphic
     */
    public static GraphicCollection createLineString(XYListDataset data, List<ColorBreak> cbs) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        List<PointD> points;
        double x, y;
        for (int i = 0; i < data.getSeriesCount(); i++) {
            points = new ArrayList<>();
            for (int j = 0; j < data.getItemCount(i); j++) {
                x = data.getX(i, j);
                y = data.getY(i, j);
                points.add(new PointD(x, y));
            }
            pls = new PolylineShape();
            pls.setPoints(points);
            gc.add(new Graphic(pls, cbs.get(i)));
        }
        gc.setSingleLegend(false);

        return gc;
    }

    /**
     * Create 3D LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param cb Color break
     * @return LineString graphic
     */
    public static GraphicCollection createLineString3D(Array xdata, Array ydata, Array zdata, ColorBreak cb) {
        GraphicCollection3D gc = new GraphicCollection3D();
        PolylineZShape pls;
        List<PointZ> points = new ArrayList<>();
        double x, y, z = 0;
        boolean fixZ = false;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z = zdata.getDouble(0);
        }
        if (xdata.getRank() == 1) {
            for (int i = 0; i < xdata.getSize(); i++) {
                x = xdata.getDouble(i);
                y = ydata.getDouble(i);
                if (!fixZ) {
                    z = zdata.getDouble(i);
                }
                if (Double.isNaN(y) || Double.isNaN(x)) {
                    if (points.isEmpty()) {
                        continue;
                    }
                    if (points.size() == 1) {
                        points.add((PointZ) points.get(0).clone());
                    }
                    pls = new PolylineZShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                    points = new ArrayList<>();
                } else {
                    points.add(new PointZ(x, y, z));
                }
            }
            if (points.size() > 1) {
                pls = new PolylineZShape();
                pls.setPoints(points);
                gc.add(new Graphic(pls, cb));
            }
        } else {    //two dimensions
            int[] shape = xdata.getShape();
            int yn = shape[0];
            int xn = shape[1];
            for (int j = 0; j < yn; j++){
                for (int i = 0; i < xn; i++) {
                    x = xdata.getDouble(j * xn + i);
                    y = ydata.getDouble(j * xn + i);
                    if (!fixZ) {
                        z = zdata.getDouble(j * xn + i);
                    }
                    if (Double.isNaN(y) || Double.isNaN(x)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() == 1) {
                            points.add((PointZ) points.get(0).clone());
                        }
                        pls = new PolylineZShape();
                        pls.setPoints(points);
                        gc.add(new Graphic(pls, cb));
                        points = new ArrayList<>();
                    } else {
                        points.add(new PointZ(x, y, z));
                    }
                }
                if (points.size() > 1) {
                    pls = new PolylineZShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                    points = new ArrayList<>();
                }
            }
        }

        return gc;
    }
    
    /**
     * Create 3D LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param cbs Color break list
     * @return LineString graphic
     */
    public static GraphicCollection createLineString3D(Array xdata, Array ydata, Array zdata, List<ColorBreak> cbs) {
        GraphicCollection3D gc = new GraphicCollection3D();
        PolylineZShape pls;
        List<PointZ> points = new ArrayList<>();
        double x, y, z = 0;
        boolean fixZ = false;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z = zdata.getDouble(0);
        }
        ColorBreak cb;
        if (xdata.getRank() == 1) {
            cb = cbs.get(0);
            for (int i = 0; i < xdata.getSize(); i++) {
                x = xdata.getDouble(i);
                y = ydata.getDouble(i);
                if (!fixZ) {
                    z = zdata.getDouble(i);
                }
                if (Double.isNaN(y) || Double.isNaN(x)) {
                    if (points.isEmpty()) {
                        continue;
                    }
                    if (points.size() == 1) {
                        points.add((PointZ) points.get(0).clone());
                    }
                    pls = new PolylineZShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                    points = new ArrayList<>();
                } else {
                    points.add(new PointZ(x, y, z));
                }
            }
            if (points.size() > 1) {
                pls = new PolylineZShape();
                pls.setPoints(points);
                gc.add(new Graphic(pls, cb));
            }
        } else {    //two dimensions
            int[] shape = xdata.getShape();
            int yn = shape[0];
            int xn = shape[1];
            for (int j = 0; j < yn; j++){
                cb = cbs.get(j);
                for (int i = 0; i < xn; i++) {
                    x = xdata.getDouble(j * xn + i);
                    y = ydata.getDouble(j * xn + i);
                    if (!fixZ) {
                        z = zdata.getDouble(j * xn + i);
                    }
                    if (Double.isNaN(y) || Double.isNaN(x)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() == 1) {
                            points.add((PointZ) points.get(0).clone());
                        }
                        pls = new PolylineZShape();
                        pls.setPoints(points);
                        gc.add(new Graphic(pls, cb));
                        points = new ArrayList<>();
                    } else {
                        points.add(new PointZ(x, y, z));
                    }
                }
                if (points.size() > 1) {
                    pls = new PolylineZShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                    points = new ArrayList<>();
                }
            }
        }

        return gc;
    }

    /**
     * Create 3D LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param mdata M data array
     * @param ls Legend scheme
     * @return LineString graphic
     */
    public static GraphicCollection createLineString3D(Array xdata, Array ydata, Array zdata, Array mdata,
            LegendScheme ls) {
        GraphicCollection3D gc = new GraphicCollection3D();
        PolylineZShape pls;
        List<PointZ> points = new ArrayList<>();
        double x, y, z = 0, m;
        boolean fixZ = false;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z = zdata.getDouble(0);
        }
        ColorBreak cb;
        ColorBreakCollection cbs;
        if (xdata.getRank() == 1) {
            cbs = new ColorBreakCollection();
            for (int i = 0; i < xdata.getSize(); i++) {
                x = xdata.getDouble(i);
                y = ydata.getDouble(i);
                if (!fixZ) {
                    z = zdata.getDouble(i);
                }
                m = mdata.getDouble(i);
                cb = ls.findLegendBreak(m);
                if (Double.isNaN(y) || Double.isNaN(x)) {
                    if (points.isEmpty()) {
                        continue;
                    }
                    if (points.size() == 1) {
                        points.add((PointZ) points.get(0).clone());
                    }
                    pls = new PolylineZShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cbs));
                    points = new ArrayList<>();
                    cbs = new ColorBreakCollection();
                } else {
                    points.add(new PointZ(x, y, z));
                    cbs.add(cb);
                }
            }
            if (points.size() > 1) {            
                pls = new PolylineZShape();
                pls.setPoints(points);
                gc.add(new Graphic(pls, cbs));        
            }
        } else {    //two dimensions
            int[] shape = xdata.getShape();
            int yn = shape[0];
            int xn = shape[1];
            for (int j = 0; j < yn; j++){
                cbs = new ColorBreakCollection();
                for (int i = 0; i < xn; i++) {
                    x = xdata.getDouble(j * xn + i);
                    y = ydata.getDouble(j * xn + i);
                    if (!fixZ) {
                        z = zdata.getDouble(j * xn + i);
                    }
                    m = mdata.getDouble(j * xn + i);
                    cb = ls.findLegendBreak(m);
                    if (Double.isNaN(y) || Double.isNaN(x)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() == 1) {
                            points.add((PointZ) points.get(0).clone());
                        }
                        pls = new PolylineZShape();
                        pls.setPoints(points);
                        gc.add(new Graphic(pls, cbs));
                        points = new ArrayList<>();
                        cbs = new ColorBreakCollection();
                    } else {
                        points.add(new PointZ(x, y, z));
                        cbs.add(cb);
                    }
                }
                if (points.size() > 1) {            
                    pls = new PolylineZShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cbs));        
                    points = new ArrayList<>();
                }
            }
        }
        gc.setLegendScheme(ls);

        return gc;
    }

    /**
     * Create error LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param xErrorLeft X error array - left
     * @param xErrorRight X error array - right
     * @param yErrorBottom Y error array - bottom
     * @param yErrorUp Y error array - up
     * @param cb Color break
     * @param ecb Error bar color break
     * @param capSize The length of the error bar caps.
     * @return LineString graphics
     */
    public static GraphicCollection createErrorLineString(Array xdata, Array ydata, Array xErrorLeft,
            Array xErrorRight, Array yErrorBottom, Array yErrorUp, PolylineBreak cb, PolylineBreak ecb, float capSize) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        CapPolylineShape epls;
        List<PointD> points = new ArrayList<>();
        List<PointD> eps;
        double x, y, xerrL, xerrR, yerrB, yerrU;
        //Loop
        for (int i = 0; i < xdata.getSize(); i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            if (Double.isNaN(y) || Double.isNaN(x)) {
                if (points.isEmpty()) {
                    continue;
                }
                if (points.size() == 1) {
                    points.add((PointD) points.get(0).clone());
                }
                pls = new PolylineShape();
                pls.setPoints(points);
                gc.add(new Graphic(pls, cb));
                points = new ArrayList<>();
            } else {
                points.add(new PointD(x, y));
                if (yErrorBottom != null) {
                    yerrB = yErrorBottom.getDouble(i);
                    yerrU = yErrorUp.getDouble(i);
                    eps = new ArrayList<>();
                    eps.add(new PointD(x, y + yerrU));
                    eps.add(new PointD(x, y - yerrB));
                    epls = new CapPolylineShape();
                    epls.setCapLen(capSize);
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                }
                if (xErrorLeft != null) {
                    xerrL = xErrorLeft.getDouble(i);
                    xerrR = xErrorRight.getDouble(i);
                    eps = new ArrayList<>();
                    eps.add(new PointD(x - xerrL, y));
                    eps.add(new PointD(x + xerrR, y));
                    epls = new CapPolylineShape();
                    epls.setCapLen(capSize);
                    epls.setCapAngle(90);
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                }
            }
        }
        if (!points.isEmpty()) {
            if (points.size() == 1) {
                points.add((PointD) points.get(0).clone());
            }
            pls = new PolylineShape();
            pls.setPoints(points);
            gc.add(new Graphic(pls, cb));
        }
        gc.setSingleLegend(false);
        PolylineBreak lb = (PolylineBreak) ecb.clone();
        lb.setDrawSymbol(cb.getDrawSymbol());
        gc.setLegendBreak(lb);

        return gc;
    }

    /**
     * Create error LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param xErrorLeft X error array - left
     * @param xErrorRight X error array - right
     * @param yErrorBottom Y error array - bottom
     * @param yErrorUp Y error array - up
     * @param cb Color break
     * @param ecb Error bar color break
     * @param capSize The length of the error bar caps.
     * @return LineString graphics
     */
    public static GraphicCollection createErrorLineString_bak1(Array xdata, Array ydata, Array xErrorLeft,
            Array xErrorRight, Array yErrorBottom, Array yErrorUp, ColorBreak cb, ColorBreak ecb, Double capSize) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls, epls;
        List<PointD> points = new ArrayList<>();
        List<PointD> eps;
        double x, y, xerrL, xerrR, yerrB, yerrU;
        double width;
        if (capSize == null) {
            width = (ArrayMath.getMaximum(xdata) - ArrayMath.getMinimum(xdata)) / xdata.getSize() * 0.1;
        } else {
            width = capSize * 0.5;
        }
        //Loop
        for (int i = 0; i < xdata.getSize(); i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            if (Double.isNaN(y) || Double.isNaN(x)) {
                if (points.isEmpty()) {
                    continue;
                }
                if (points.size() == 1) {
                    points.add((PointD) points.get(0).clone());
                }
                pls = new PolylineShape();
                pls.setPoints(points);
                gc.add(new Graphic(pls, cb));
                points = new ArrayList<>();
            } else {
                points.add(new PointD(x, y));
                if (yErrorBottom != null) {
                    yerrB = yErrorBottom.getDouble(i);
                    yerrU = yErrorUp.getDouble(i);
                    eps = new ArrayList<>();
                    eps.add(new PointD(x, y + yerrU));
                    eps.add(new PointD(x, y - yerrB));
                    epls = new PolylineShape();
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                    eps = new ArrayList<>();
                    eps.add(new PointD(x - width, y + yerrU));
                    eps.add(new PointD(x + width, y + yerrU));
                    epls = new PolylineShape();
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                    eps = new ArrayList<>();
                    eps.add(new PointD(x - width, y - yerrB));
                    eps.add(new PointD(x + width, y - yerrB));
                    epls = new PolylineShape();
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                }
                if (xErrorLeft != null) {
                    xerrL = xErrorLeft.getDouble(i);
                    xerrR = xErrorRight.getDouble(i);
                    eps = new ArrayList<>();
                    eps.add(new PointD(x - xerrL, y));
                    eps.add(new PointD(x + xerrR, y));
                    epls = new PolylineShape();
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                    eps = new ArrayList<>();
                    eps.add(new PointD(x - xerrL, y - width));
                    eps.add(new PointD(x - xerrL, y + width));
                    epls = new PolylineShape();
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                    eps = new ArrayList<>();
                    eps.add(new PointD(x + xerrR, y - width));
                    eps.add(new PointD(x + xerrR, y + width));
                    epls = new PolylineShape();
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                }
            }
        }
        if (!points.isEmpty()) {
            if (points.size() == 1) {
                points.add((PointD) points.get(0).clone());
            }
            pls = new PolylineShape();
            pls.setPoints(points);
            gc.add(new Graphic(pls, cb));
        }
        gc.setSingleLegend(false);

        return gc;
    }

    /**
     * Create error LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param xError X error array
     * @param yError Y error array
     * @param cb Color break
     * @return LineString graphic
     */
    public static GraphicCollection createErrorLineString_bak(Array xdata, Array ydata, Array xError, Array yError, ColorBreak cb) {
        GraphicCollection gc = new GraphicCollection();
        PolylineErrorShape pls;
        List<PointD> points = new ArrayList<>();
        List<Number> xerrors = new ArrayList<>();
        List<Number> yerrors = new ArrayList<>();
        double x, y;
        for (int i = 0; i < xdata.getSize(); i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            if (Double.isNaN(y) || Double.isNaN(x)) {
                if (points.isEmpty()) {
                    continue;
                }
                if (points.size() == 1) {
                    points.add((PointD) points.get(0).clone());
                }
                pls = new PolylineErrorShape();
                pls.setPoints(points);
                if (xError != null) {
                    pls.setXerror(xerrors);
                }
                if (yError != null) {
                    pls.setYerror(yerrors);
                }
                pls.updateExtent();
                gc.add(new Graphic(pls, cb));
                points = new ArrayList<>();
                xerrors = new ArrayList<>();
                yerrors = new ArrayList<>();
            } else {
                points.add(new PointD(x, y));
                if (xError != null) {
                    xerrors.add(xError.getDouble(i));
                }
                if (yError != null) {
                    yerrors.add(yError.getDouble(i));
                }
            }
        }
        if (points.size() == 1) {
            points.add((PointD) points.get(0).clone());
        }
        pls = new PolylineErrorShape();
        pls.setPoints(points);
        if (xError != null) {
            pls.setXerror(xerrors);
        }
        if (yError != null) {
            pls.setYerror(yerrors);
        }
        pls.updateExtent();
        gc.add(new Graphic(pls, cb));

        return gc;
    }

    /**
     * Create step LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cb Color break
     * @param where Where - pre, post, mid
     * @return LineString graphic
     */
    public static GraphicCollection createStepLineString(Array xdata, Array ydata, ColorBreak cb, String where) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        List<PointD> points = new ArrayList<>();
        double x, x1, x2, y, y1, y2;
        switch (where) {
            case "mid":
                for (int i = 0; i < xdata.getSize() - 1; i++) {
                    x1 = xdata.getDouble(i);
                    x2 = xdata.getDouble(i + 1);
                    y1 = ydata.getDouble(i);
                    y2 = ydata.getDouble(i + 1);
                    if (Double.isNaN(y1) || Double.isNaN(x1) || Double.isNaN(x2)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() > 1) {
                            pls = new PolylineShape();
                            pls.setPoints(points);
                            gc.add(new Graphic(pls, cb));
                            points = new ArrayList<>();
                        }
                    } else {
                        x = x1 + (x2 - x1) * 0.5;
                        if (i == 0) {
                            points.add(new PointD(x1, y1));
                            points.add(new PointD(x, y1));
                            points.add(new PointD(x, y2));
                        } else if (i == xdata.getSize() - 2) {
                            points.add(new PointD(x, y1));
                            points.add(new PointD(x, y2));
                            points.add(new PointD(x2, y2));
                        } else {
                            points.add(new PointD(x, y1));
                            points.add(new PointD(x, y2));
                        }
                    }
                }
                if (points.size() > 1) {
                    pls = new PolylineShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                }
                break;
            case "post":
                for (int i = 0; i < xdata.getSize() - 1; i++) {
                    x1 = xdata.getDouble(i);
                    x2 = xdata.getDouble(i + 1);
                    y = ydata.getDouble(i);
                    if (Double.isNaN(y) || Double.isNaN(x1) || Double.isNaN(x2)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() > 1) {
                            pls = new PolylineShape();
                            pls.setPoints(points);
                            gc.add(new Graphic(pls, cb));
                            points = new ArrayList<>();
                        }
                    } else {
                        points.add(new PointD(x1, y));
                        points.add(new PointD(x2, y));
                        if (i == xdata.getSize() - 2) {
                            points.add(new PointD(x2, ydata.getDouble(i + 1)));
                        }
                    }
                }
                if (points.size() > 1) {
                    pls = new PolylineShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                }
                break;
            default:
                for (int i = 0; i < xdata.getSize() - 1; i++) {
                    x1 = xdata.getDouble(i);
                    x2 = xdata.getDouble(i + 1);
                    y = ydata.getDouble(i + 1);
                    if (Double.isNaN(y) || Double.isNaN(x1) || Double.isNaN(x2)) {
                        if (points.isEmpty()) {
                            continue;
                        }
                        if (points.size() > 1) {
                            pls = new PolylineShape();
                            pls.setPoints(points);
                            gc.add(new Graphic(pls, cb));
                            points = new ArrayList<>();
                        }
                    } else {
                        if (i == 0) {
                            points.add(new PointD(x1, ydata.getDouble(i)));
                        }
                        points.add(new PointD(x1, y));
                        points.add(new PointD(x2, y));
                    }
                }
                if (points.size() > 1) {
                    pls = new PolylineShape();
                    pls.setPoints(points);
                    gc.add(new Graphic(pls, cb));
                }
                break;
        }

        return gc;
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cb Color break
     * @return LineString graphic
     */
    public static GraphicCollection createGraphics(Array xdata, Array ydata, ColorBreak cb) {
        GraphicCollection graphics = new GraphicCollection();
        if (cb instanceof PolylineBreak) {
            graphics.add(createLineString(xdata, ydata, cb));
        } else {
            PointShape ps;
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointShape();
                ps.setPoint(new PointD(xdata.getDouble(i), ydata.getDouble(i)));
                graphics.add(new Graphic(ps, cb));
            }
        }
        return graphics;
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cbs Color breaks
     * @return LineString graphic
     */
    public static GraphicCollection createPoints(Array xdata, Array ydata, List<ColorBreak> cbs) {
        GraphicCollection graphics = new GraphicCollection();
        PointShape ps;
        if (cbs.size() == xdata.getSize()) {
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointShape();
                ps.setPoint(new PointD(xdata.getDouble(i), ydata.getDouble(i)));
                graphics.add(new Graphic(ps, cbs.get(i)));
            }
            graphics.setSingleLegend(false);
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.UniqueValue);
            ls.setShapeType(ShapeTypes.Point);
            graphics.setLegendScheme(ls);
        } else {
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointShape();
                ps.setPoint(new PointD(xdata.getDouble(i), ydata.getDouble(i)));
                graphics.add(new Graphic(ps, cbs.get(0)));
                LegendScheme ls = new LegendScheme();
                ls.setLegendBreaks(cbs);
                ls.setLegendType(LegendType.SingleSymbol);
                ls.setShapeType(ShapeTypes.Point);
                graphics.setLegendScheme(ls);
            }
        }
        return graphics;
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param ls Legend scheme
     * @return LineString graphic
     */
    public static GraphicCollection createPoints(Array xdata, Array ydata, Array zdata, LegendScheme ls) {
        GraphicCollection graphics = new GraphicCollection();
        PointShape ps;
        double z;
        ColorBreak cb;
        for (int i = 0; i < xdata.getSize(); i++) {
            ps = new PointShape();
            ps.setPoint(new PointD(xdata.getDouble(i), ydata.getDouble(i)));
            z = zdata.getDouble(i);
            cb = ls.findLegendBreak(z);
            graphics.add(new Graphic(ps, cb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);
        return graphics;
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param cb Color break
     * @return LineString graphic
     */
    public static GraphicCollection createPoints3D(Array xdata, Array ydata, Array zdata, ColorBreak cb) {
        List<ColorBreak> cbs = new ArrayList<>();
        cbs.add(cb);
        return createPoints3D(xdata, ydata, zdata, cbs);
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param cbs Color breaks
     * @return LineString graphic
     */
    public static GraphicCollection createPoints3D(Array xdata, Array ydata, Array zdata, List<ColorBreak> cbs) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        PointShape ps;
        boolean fixZ = false;
        double z = 0;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z = zdata.getDouble(0);
        }
        if (cbs.size() == xdata.getSize()) {
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointZShape();
                if (fixZ) {
                    ps.setPoint(new PointZ(xdata.getDouble(i), ydata.getDouble(i), z));
                } else {
                    ps.setPoint(new PointZ(xdata.getDouble(i), ydata.getDouble(i), zdata.getDouble(i)));
                }
                graphics.add(new Graphic(ps, cbs.get(i)));
            }
            graphics.setSingleLegend(false);
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.UniqueValue);
            ls.setShapeType(ShapeTypes.Point);
            graphics.setLegendScheme(ls);
        } else {
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointZShape();
                if (fixZ) {
                    ps.setPoint(new PointZ(xdata.getDouble(i), ydata.getDouble(i), z));
                } else {
                    ps.setPoint(new PointZ(xdata.getDouble(i), ydata.getDouble(i), zdata.getDouble(i)));
                }
                graphics.add(new Graphic(ps, cbs.get(0)));
                LegendScheme ls = new LegendScheme();
                ls.setLegendBreaks(cbs);
                ls.setLegendType(LegendType.SingleSymbol);
                ls.setShapeType(ShapeTypes.Point);
                graphics.setLegendScheme(ls);
            }
        }
        return graphics;
    }

    /**
     * Create 3D point graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param cdata C data array
     * @param ls Legend scheme
     * @return 3D point graphics
     */
    public static GraphicCollection createPoints3D(Array xdata, Array ydata, Array zdata, Array cdata, LegendScheme ls) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        PointShape ps;
        double c;
        ColorBreak cb;
        boolean fixZ = false;
        double z = 0;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z = zdata.getDouble(0);
        }
        for (int i = 0; i < xdata.getSize(); i++) {
            ps = new PointZShape();
            if (fixZ) {
                ps.setPoint(new PointZ(xdata.getDouble(i), ydata.getDouble(i), z));
            } else {
                ps.setPoint(new PointZ(xdata.getDouble(i), ydata.getDouble(i), zdata.getDouble(i)));
            }
            c = cdata.getDouble(i);
            cb = ls.findLegendBreak(c);
            graphics.add(new Graphic(ps, cb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);
        return graphics;
    }

    /**
     * Create 3D stem graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param cbs Color breaks
     * @param plb Stem line break
     * @param bottom Stem bottom
     * @param sameStemColor Same stem line and point color or not
     * @return Graphics
     */
    public static GraphicCollection[] createStems3D(Array xdata, Array ydata, Array zdata, List<ColorBreak> cbs,
            PolylineBreak plb, double bottom, boolean sameStemColor) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        GraphicCollection3D stemlines = new GraphicCollection3D();
        PointShape ps;
        boolean fixZ = false;
        double x, y, z, z0 = 0;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z0 = zdata.getDouble(0);
        }
        List<PointZ> pzs;
        PolylineZShape pls;
        if (cbs.size() == xdata.getSize()) {
            for (int i = 0; i < xdata.getSize(); i++) {
                x = xdata.getDouble(i);
                y = ydata.getDouble(i);
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                ps = new PointZShape();
                pzs = new ArrayList<>();
                if (fixZ) {
                    ps.setPoint(new PointZ(x, y, z0));
                    pzs.add(new PointZ(x, y, bottom));
                    pzs.add(new PointZ(x, y, z0));
                } else {
                    z = zdata.getDouble(i);
                    if (Double.isNaN(z)) {
                        continue;
                    }
                    ps.setPoint(new PointZ(x, y, z));
                    pzs.add(new PointZ(x, y, bottom));
                    pzs.add(new PointZ(x, y, z));
                }
                graphics.add(new Graphic(ps, cbs.get(i)));
                pls = new PolylineZShape();
                pls.setPoints(pzs);
                if (sameStemColor) {
                    PolylineBreak nplb = (PolylineBreak) plb.clone();
                    nplb.setColor(cbs.get(i).getColor());
                    stemlines.add(new Graphic(pls, nplb));
                } else {
                    stemlines.add(new Graphic(pls, plb));
                }
            }
            graphics.setSingleLegend(false);
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.UniqueValue);
            ls.setShapeType(ShapeTypes.Point);
            graphics.setLegendScheme(ls);
        } else {
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointZShape();
                pzs = new ArrayList<>();
                if (fixZ) {
                    ps.setPoint(new PointZ(xdata.getDouble(i), ydata.getDouble(i), z0));
                    pzs.add(new PointZ(xdata.getDouble(i), ydata.getDouble(i), bottom));
                    pzs.add(new PointZ(xdata.getDouble(i), ydata.getDouble(i), z0));
                } else {
                    ps.setPoint(new PointZ(xdata.getDouble(i), ydata.getDouble(i), zdata.getDouble(i)));
                    pzs.add(new PointZ(xdata.getDouble(i), ydata.getDouble(i), bottom));
                    pzs.add(new PointZ(xdata.getDouble(i), ydata.getDouble(i), zdata.getDouble(i)));
                }
                graphics.add(new Graphic(ps, cbs.get(0)));
                pls = new PolylineZShape();
                pls.setPoints(pzs);
                if (sameStemColor) {
                    PolylineBreak nplb = (PolylineBreak) plb.clone();
                    nplb.setColor(cbs.get(0).getColor());
                    stemlines.add(new Graphic(pls, nplb));
                } else {
                    stemlines.add(new Graphic(pls, plb));
                }
            }
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.SingleSymbol);
            ls.setShapeType(ShapeTypes.Point);
            graphics.setLegendScheme(ls);
        }
        return new GraphicCollection[]{stemlines, graphics};
    }

    /**
     * Create 3D stem graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param cdata C data array
     * @param ls Legend scheme
     * @param plb Stem line break
     * @param bottom Stem bottom
     * @param sameStemColor Same stem line and point color or not
     * @return 3D point graphics
     */
    public static GraphicCollection[] createStems3D(Array xdata, Array ydata, Array zdata, Array cdata, LegendScheme ls,
            PolylineBreak plb, double bottom, boolean sameStemColor) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        GraphicCollection3D stemlines = new GraphicCollection3D();
        PointShape ps;
        double c;
        ColorBreak cb;
        boolean fixZ = false;
        double x, y, z, z0 = 0;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z0 = zdata.getDouble(0);
        }
        List<PointZ> pzs;
        PolylineZShape pls;
        for (int i = 0; i < xdata.getSize(); i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            ps = new PointZShape();
            pzs = new ArrayList<>();
            if (fixZ) {
                ps.setPoint(new PointZ(x, y, z0));
                pzs.add(new PointZ(x, y, bottom));
                pzs.add(new PointZ(x, y, z0));
            } else {
                z = zdata.getDouble(i);
                if (Double.isNaN(z)) {
                    continue;
                }
                ps.setPoint(new PointZ(x, y, z));
                pzs.add(new PointZ(x, y, bottom));
                pzs.add(new PointZ(x, y, z));
            }
            c = cdata.getDouble(i);
            cb = ls.findLegendBreak(c);
            graphics.add(new Graphic(ps, cb));
            pls = new PolylineZShape();
            pls.setPoints(pzs);
            if (sameStemColor) {
                PolylineBreak nplb = (PolylineBreak) plb.clone();
                nplb.setColor(cb.getColor());
                stemlines.add(new Graphic(pls, nplb));
            } else {
                stemlines.add(new Graphic(pls, plb));
            }
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);
        return new GraphicCollection[]{stemlines, graphics};
    }

    /**
     * Add polygons
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param pgb PolygonBreak
     * @return Graphics
     */
    public static GraphicCollection createPolygons(Array xa, Array ya, PolygonBreak pgb) {
        GraphicCollection graphics = new GraphicCollection();
        double x, y;
        int n = (int) xa.getSize();
        PolygonShape pgs;
        PointD p;
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            x = xa.getDouble(i);
            y = ya.getDouble(i);
            if (Double.isNaN(x)) {
                if (points.size() > 2) {
                    pgs = new PolygonShape();
                    pgs.setPoints(points);
                    Graphic aGraphic = new Graphic(pgs, pgb);
                    graphics.add(aGraphic);
                }
                points = new ArrayList<>();
            } else {
                p = new PointD(x, y);
                points.add(p);
            }
        }
        if (points.size() > 2) {
            pgs = new PolygonShape();
            pgs.setPoints(points);
            Graphic aGraphic = new Graphic(pgs, pgb);
            graphics.add(aGraphic);
        }
        return graphics;
    }

    /**
     * Add wireframe polylines
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param pb Polyline break
     * @return Graphics
     */
    public static GraphicCollection createWireframe(Array xa, Array ya, Array za, PolylineBreak pb) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        int[] shape = xa.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        double z1, z2, z3, z4, z;
        int idx1, idx2, idx3, idx4;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                idx1 = i * colNum + j;
                idx2 = i * colNum + j + 1;
                idx3 = (i + 1) * colNum + j;
                idx4 = (i + 1) * colNum + j + 1;
                z1 = za.getDouble(idx1);
                z2 = za.getDouble(idx2);
                z3 = za.getDouble(idx3);
                z4 = za.getDouble(idx4);
                z = (z1 + z2 + z3 + z4) / 4.0;
                PolylineZShape ps = new PolylineZShape();
                List<PointZ> points = new ArrayList<>();
                points.add(new PointZ(xa.getDouble(idx1), ya.getDouble(idx1), z1));
                points.add(new PointZ(xa.getDouble(idx3), ya.getDouble(idx3), z3));
                points.add(new PointZ(xa.getDouble(idx4), ya.getDouble(idx4), z4));
                points.add(new PointZ(xa.getDouble(idx2), ya.getDouble(idx2), z2));
                points.add((PointZ) points.get(0).clone());
                ps.setPoints(points);
                Graphic graphic = new Graphic(ps, pb);
                graphics.add(graphic);
            }
        }

        return graphics;
    }

    /**
     * Add mesh polygons
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param ls Legend scheme
     * @return Graphics
     */
    public static GraphicCollection createMeshPolygons(Array xa, Array ya, Array za, LegendScheme ls) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        int[] shape = xa.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        double z1, z2, z3, z4, z;
        int idx1, idx2, idx3, idx4;
        PolygonBreak pb;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                idx1 = i * colNum + j;
                idx2 = i * colNum + j + 1;
                idx3 = (i + 1) * colNum + j;
                idx4 = (i + 1) * colNum + j + 1;
                z1 = za.getDouble(idx1);
                z2 = za.getDouble(idx2);
                z3 = za.getDouble(idx3);
                z4 = za.getDouble(idx4);
                z = (z1 + z2 + z3 + z4) / 4.0;
                PolygonZShape ps = new PolygonZShape();
                List<PointZ> points = new ArrayList<>();
                points.add(new PointZ(xa.getDouble(idx1), ya.getDouble(idx1), z1));
                points.add(new PointZ(xa.getDouble(idx3), ya.getDouble(idx3), z3));
                points.add(new PointZ(xa.getDouble(idx4), ya.getDouble(idx4), z4));
                points.add(new PointZ(xa.getDouble(idx2), ya.getDouble(idx2), z2));
                points.add((PointZ) points.get(0).clone());
                ps.setPoints(points);
                ps.lowValue = z;
                ps.highValue = ps.lowValue;
                pb = (PolygonBreak) ls.findLegendBreak(z);
                //pb.setDrawOutline(true);
                Graphic graphic = new Graphic(ps, pb);
                graphics.add(graphic);
            }
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);
        return graphics;
    }

    /**
     * Create 3D graphics from a VectorLayer.
     *
     * @param layer The layer
     * @param offset Offset of z axis.
     * @param xshift X shift - to shift the grahpics in x direction, normally
     * for map in 180 - 360 degree east
     * @return Graphics
     */
    public static GraphicCollection createGraphicsFromLayer(VectorLayer layer, double offset, double xshift) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZValue(offset);
        ShapeTypes shapeType = layer.getShapeType();
        LegendScheme ls = layer.getLegendScheme();
        PointZ pz;
        ColorBreak cb;
        switch (shapeType) {
            case Point:
                for (PointShape shape : (List<PointShape>) layer.getShapes()) {
                    PointZShape s = new PointZShape();
                    PointD pd = shape.getPoint();
                    pz = new PointZ(pd.X + xshift, pd.Y, offset);
                    s.setPoint(pz);
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    graphics.add(new Graphic(s, cb));
                }
                break;
            case Polyline:
                for (PolylineShape shape : (List<PolylineShape>) layer.getShapes()) {
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    for (Polyline pl : (List<Polyline>) shape.getPolylines()) {
                        PolylineZShape s = new PolylineZShape();
                        List<PointZ> plist = new ArrayList<>();
                        for (PointD pd : pl.getPointList()) {
                            pz = new PointZ(pd.X + xshift, pd.Y, offset);
                            plist.add(pz);
                        }
                        s.setPoints(plist);
                        graphics.add(new Graphic(s, cb));
                    }
                }
                break;
            case Polygon:
                for (PolygonShape shape : (List<PolygonShape>) layer.getShapes()) {
                    PolygonZShape s = new PolygonZShape();
                    List<PointZ> plist = new ArrayList<>();
                    for (PointD pd : shape.getPoints()) {
                        pz = new PointZ(pd.X + xshift, pd.Y, offset);
                        plist.add(pz);
                    }
                    s.setPartNum(shape.getPartNum());
                    s.setParts(shape.getParts());
                    s.setPoints(plist);
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    graphics.add(new Graphic(s, cb));
                }
                break;
            case PointZ:
            case PolylineZ:
            case PolygonZ:
                graphics.setFixZ(false);
                if (xshift == 0) {
                    for (Shape shape : layer.getShapes()) {

                        cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                        graphics.add(new Graphic(shape, cb));
                    }
                } else {
                    switch (shapeType) {
                        case PointZ:
                            for (PointZShape shape : (List<PointZShape>) layer.getShapes()) {
                                PointZShape s = new PointZShape();
                                PointZ pd = (PointZ) shape.getPoint();
                                pz = new PointZ(pd.X + xshift, pd.Y, pd.Z, pd.M);
                                s.setPoint(pz);
                                cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                                graphics.add(new Graphic(s, cb));
                            }
                            break;
                        case PolylineZ:
                            for (PolylineZShape shape : (List<PolylineZShape>) layer.getShapes()) {
                                cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                                for (PolylineZ pl : (List<PolylineZ>) shape.getPolylines()) {
                                    PolylineZShape s = new PolylineZShape();
                                    List<PointZ> plist = new ArrayList<>();
                                    for (PointZ pd : (List<PointZ>) pl.getPointList()) {
                                        pz = new PointZ(pd.X + xshift, pd.Y, pd.Z, pd.M);
                                        plist.add(pz);
                                    }
                                    s.setPoints(plist);
                                    graphics.add(new Graphic(s, cb));
                                }
                            }
                            break;
                        case PolygonZ:
                            for (PolygonZShape shape : (List<PolygonZShape>) layer.getShapes()) {
                                PolygonZShape s = new PolygonZShape();
                                List<PointZ> plist = new ArrayList<>();
                                for (PointZ pd : (List<PointZ>) shape.getPoints()) {
                                    pz = new PointZ(pd.X + xshift, pd.Y, pd.Z, pd.M);
                                    plist.add(pz);
                                }
                                s.setPartNum(shape.getPartNum());
                                s.setParts(shape.getParts());
                                s.setPoints(plist);
                                cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                                graphics.add(new Graphic(s, cb));
                            }
                            break;
                    }
                }
                break;
        }
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create rectangle graphic
     *
     * @param pos Rectangle position
     * @param curvature Curvature
     * @param pgb Polygon break
     * @return
     */
    public static Graphic createRectangle(List<Number> pos, List<Number> curvature, PolygonBreak pgb) {
        RectangleShape rect = new RectangleShape(pos.get(0).doubleValue(), pos.get(1).doubleValue(),
                pos.get(2).doubleValue(), pos.get(3).doubleValue());
        if (curvature != null) {
            rect.setRoundX(curvature.get(0).doubleValue());
            rect.setRoundY(curvature.get(1).doubleValue());
        }
        Graphic graphic = new Graphic(rect, pgb);
        return graphic;
    }

    /**
     * Create bar graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param autoWidth Is auto width or not
     * @param widths Width
     * @param drawError Is draw error or not
     * @param error Error
     * @param drawBottom Is draw bottom or not
     * @param bottom Bottom
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createBars(Array xdata, Array ydata, boolean autoWidth,
            Array widths, boolean drawError, Array error, boolean drawBottom, Array bottom,
            List<BarBreak> bbs) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) xdata.getSize();
        double x, y;
        BarBreak bb = bbs.get(0);
        PolylineBreak ebreak = new PolylineBreak();
        ebreak.setColor(bb.getErrorColor());
        ebreak.setWidth(bb.getErrorSize());
        double width = widths.getDouble(0);
        if (autoWidth && xdata.getSize() > 1) {
            width = (xdata.getDouble(1) - xdata.getDouble(0)) * width;
        }
        double bot = 0;
        if (drawBottom) {
            bot = bottom.getDouble(0);
        }
        double miny = 0;
        boolean baseLine = false;
        for (int i = 0; i < n; i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            // Add bar
            if (drawBottom) {
                if (bottom.getSize() > i) {
                    bot = bottom.getDouble(i);
                }
                miny = bot;
                y += miny;
            }
            if (y < miny) {
                baseLine = true;
            }
            if (widths.getSize() > 1 && widths.getSize() > i) {
                width = widths.getDouble(i);
            }
            List<PointD> pList = new ArrayList<>();
            pList.add(new PointD(x, miny));
            pList.add(new PointD(x, y));
            pList.add(new PointD(x + width, y));
            pList.add(new PointD(x + width, miny));
            pList.add(new PointD(x, miny));
            PolygonShape pgs = new PolygonShape();
            pgs.setPoints(pList);
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(pgs, bb));

            if (drawError) {
                //Add error line
                double e = error.getDouble(i);
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.5, y - e));
                pList.add(new PointD(x + width * 0.5, y + e));
                PolylineShape pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                //Add cap
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.25, y - e));
                pList.add(new PointD(x + width * 0.75, y - e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.25, y + e));
                pList.add(new PointD(x + width * 0.75, y + e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
            }
        }

        if (baseLine) {
            List<PointD> pList = new ArrayList<>();
            double x1 = xdata.getDouble(0);
            double x2 = xdata.getDouble((int) xdata.getSize() - 1);
            x1 -= (x2 - x1);
            x2 += (x2 - x1);
            pList.add(new PointD(x1, miny));
            pList.add(new PointD(x2, miny));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            ebreak = new PolylineBreak();
            ebreak.setColor(Color.black);
            graphics.add(new Graphic(pls, ebreak));
        }
        graphics.setSingleLegend(false);

        return graphics;
    }

    /**
     * Create horizontal bar graphics
     *
     * @param ydata Y data array
     * @param xdata X data array
     * @param autoHeight Is auto height or not
     * @param heights Heights
     * @param drawError Is draw error or not
     * @param error Error
     * @param drawLeft Is draw left or not
     * @param left Left
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createHBars(Array ydata, Array xdata, boolean autoHeight,
            Array heights, boolean drawError, Array error, boolean drawLeft, Array left,
            List<BarBreak> bbs) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) ydata.getSize();
        double x, y;
        BarBreak bb = bbs.get(0);
        PolylineBreak ebreak = new PolylineBreak();
        ebreak.setColor(bb.getErrorColor());
        ebreak.setWidth(bb.getErrorSize());
        double height = heights.getDouble(0);
        if (autoHeight && ydata.getSize() > 1) {
            height = (ydata.getDouble(1) - ydata.getDouble(0)) * height;
        }
        double bot = 0;
        if (drawLeft) {
            bot = left.getDouble(0);
        }
        double minx = 0;
        boolean baseLine = false;
        for (int i = 0; i < n; i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            // Add bar
            if (drawLeft) {
                if (left.getSize() > i) {
                    bot = left.getDouble(i);
                }
                minx = bot;
                x += minx;
            }
            if (x < minx) {
                baseLine = true;
            }
            if (heights.getSize() > 1 && heights.getSize() > i) {
                height = heights.getDouble(i);
            }
            List<PointD> pList = new ArrayList<>();
            pList.add(new PointD(minx, y));
            pList.add(new PointD(x, y));
            pList.add(new PointD(x, y + height));
            pList.add(new PointD(minx, y + height));
            pList.add(new PointD(minx, y));
            PolygonShape pgs = new PolygonShape();
            pgs.setPoints(pList);
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(pgs, bb));

            if (drawError) {
                //Add error line
                double e = error.getDouble(i);
                pList = new ArrayList<>();
                pList.add(new PointD(x - e, y + height * 0.5));
                pList.add(new PointD(x + e, y + height * 0.5));
                PolylineShape pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                //Add cap
                pList = new ArrayList<>();
                pList.add(new PointD(x - e, y + height * 0.25));
                pList.add(new PointD(x - e, y + height * 0.75));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                pList = new ArrayList<>();
                pList.add(new PointD(x + e, y + height * 0.25));
                pList.add(new PointD(x + e, y + height * 0.75));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
            }
        }

        if (baseLine) {
            List<PointD> pList = new ArrayList<>();
            double y1 = ydata.getDouble(0);
            double y2 = ydata.getDouble((int) ydata.getSize() - 1);
            y1 -= (y2 - y1);
            y2 += (y2 - y1);
            pList.add(new PointD(minx, y1));
            pList.add(new PointD(minx, y2));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            ebreak = new PolylineBreak();
            ebreak.setColor(Color.black);
            graphics.add(new Graphic(pls, ebreak));
        }
        graphics.setSingleLegend(false);

        return graphics;
    }

    /**
     * Create bar graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param autoWidth Is auto width or not
     * @param widths Width
     * @param drawError Is draw error or not
     * @param error Error
     * @param drawBottom Is draw bottom or not
     * @param bottom Bottom
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createBars1(Array xdata, Array ydata, boolean autoWidth,
            Array widths, boolean drawError, Array error, boolean drawBottom, Array bottom,
            List<BarBreak> bbs) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) xdata.getSize();
        double x, y;
        BarBreak bb = bbs.get(0);
        PolylineBreak ebreak = new PolylineBreak();
        ebreak.setColor(bb.getErrorColor());
        ebreak.setWidth(bb.getErrorSize());
        double width = widths.getDouble(0);
        if (autoWidth && xdata.getSize() > 1) {
            width = (xdata.getDouble(1) - xdata.getDouble(0)) * width;
        }
        double bot = 0;
        if (drawBottom) {
            bot = bottom.getDouble(0);
        }
        double miny = 0;
        boolean baseLine = false;
        for (int i = 0; i < n; i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            // Add bar
            if (drawBottom) {
                if (bottom.getSize() > i) {
                    bot = bottom.getDouble(i);
                }
                miny = bot;
                y += miny;
            }
            if (y < miny) {
                baseLine = true;
            }
            if (widths.getSize() > 1 && widths.getSize() > i) {
                width = widths.getDouble(i);
            }
            List<PointD> pList = new ArrayList<>();
            pList.add(new PointD(x, miny));
            for (double x1 = x; x1 < x + width; x1 += width / 100) {
                pList.add(new PointD(x1, y));
            }
            pList.add(new PointD(x + width, y));
            for (double x1 = x + width; x1 > x; x1 -= width / 20) {
                pList.add(new PointD(x1, miny));
            }
            pList.add(new PointD(x, miny));
            PolygonShape pgs = new PolygonShape();
            pgs.setPoints(pList);
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(pgs, bb));

            if (drawError) {
                //Add error line
                double e = error.getDouble(i);
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.5, y - e));
                pList.add(new PointD(x + width * 0.5, y + e));
                PolylineShape pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                //Add cap
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.25, y - e));
                pList.add(new PointD(x + width * 0.75, y - e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.25, y + e));
                pList.add(new PointD(x + width * 0.75, y + e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
            }
        }

        if (baseLine) {
            List<PointD> pList = new ArrayList<>();
            double x1 = xdata.getDouble(0);
            double x2 = xdata.getDouble((int) xdata.getSize() - 1);
            x1 -= (x2 - x1);
            x2 += (x2 - x1);
            pList.add(new PointD(x1, miny));
            pList.add(new PointD(x2, miny));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            ebreak = new PolylineBreak();
            ebreak.setColor(Color.black);
            graphics.add(new Graphic(pls, ebreak));
        }
        graphics.setSingleLegend(false);

        return graphics;
    }

    /**
     * Create bar graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param autoWidth Is auto width or not
     * @param width Width
     * @param drawError Is draw error or not
     * @param error Error
     * @param drawBottom Is draw bottom or not
     * @param bottom Bottom
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createBars_bak(Array xdata, Array ydata, boolean autoWidth,
            double width, boolean drawError, Array error, boolean drawBottom, Array bottom,
            List<BarBreak> bbs) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) xdata.getSize();
        double x, y;
        BarBreak bb = bbs.get(0);
        for (int i = 0; i < n; i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            BarShape bs = new BarShape();
            bs.setPoint(new PointD(x, y));
            bs.setAutoWidth(autoWidth);
            bs.setWidth(width);
            bs.setDrawError(drawError);
            if (drawError) {
                bs.setError(error.getDouble(i));
            }
            bs.setDrawBottom(drawBottom);
            if (drawBottom) {
                bs.setBottom(bottom.getDouble(i));
            }
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(bs, bb));
        }
        if (bbs.size() == 1) {
            graphics.setSingleLegend(true);
        } else {
            graphics.setSingleLegend(false);
        }

        return graphics;
    }

    /**
     * Create histogram bar graphics
     *
     * @param data The data array
     * @param bins Bins number
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createHistBars(Array data, int bins,
            List<BarBreak> bbs) {
        List<Array> r = ArrayUtil.histogram(data, bins);
        Array xdata = r.get(1);
        Array ydata = r.get(0);
        return createHistBars(data, xdata, ydata, bbs);
    }

    /**
     * Create histogram bar graphics
     *
     * @param data The data array
     * @param bins Bins array
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createHistBars(Array data, Array bins,
            List<BarBreak> bbs) {
        List<Array> r = ArrayUtil.histogram(data, bins);
        Array xdata = r.get(1);
        Array ydata = r.get(0);
        return createHistBars(data, xdata, ydata, bbs);
    }

    /**
     * Create histogram bar graphics
     *
     * @param data The data array
     * @param xdata X bins data
     * @param ydata Y bins data
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createHistBars(Array data, Array xdata, Array ydata,
            List<BarBreak> bbs) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) ydata.getSize();
        double x, y, width;
        BarBreak bb = bbs.get(0);
        for (int i = 0; i < n; i++) {
            x = (xdata.getDouble(i + 1) + xdata.getDouble(i)) * 0.5;
            width = xdata.getDouble(i + 1) - xdata.getDouble(i);
            y = ydata.getDouble(i);
            BarShape bs = new BarShape();
            bs.setPoint(new PointD(x, y));
            bs.setAutoWidth(false);
            bs.setWidth(width);
            bs.setDrawBottom(false);
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(bs, bb));
        }
        if (bbs.size() == 1) {
            graphics.setSingleLegend(true);
        } else {
            graphics.setSingleLegend(false);
        }

        return graphics;
    }

    /**
     * Create stem graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param plb Polyline break
     * @param pb Point break
     * @param bplb Baseline break
     * @param bottom Bottom
     * @return Bar graphics
     */
    public static GraphicCollection createStems(Array xdata, Array ydata, PolylineBreak plb, PointBreak pb,
            PolylineBreak bplb, double bottom) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) xdata.getSize();
        double x, y;
        double miny = bottom;
        boolean baseLine = false;
        for (int i = 0; i < n; i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            // Add stem
            if (y < miny) {
                baseLine = true;
            }
            List<PointD> pList = new ArrayList<>();
            pList.add(new PointD(x, miny));
            pList.add(new PointD(x, y));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            graphics.add(new Graphic(pls, plb));
            PointShape ps = new PointShape();
            ps.setPoint(new PointD(x, y));
            graphics.add(new Graphic(ps, pb));
        }

        if (baseLine) {
            List<PointD> pList = new ArrayList<>();
            double x1 = xdata.getDouble(0);
            double x2 = xdata.getDouble((int) xdata.getSize() - 1);
            pList.add(new PointD(x1, miny));
            pList.add(new PointD(x2, miny));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            graphics.add(new Graphic(pls, bplb));
        }
        graphics.setSingleLegend(false);

        return graphics;
    }

    /**
     * Create an image graphic
     *
     * @param image The image
     * @return Image graphic
     */
    public static Graphic createImage(BufferedImage image) {
        ImageShape ishape = new ImageShape();
        ishape.setPoint(new PointD(0, 0));
        ishape.setImage(image);
        ishape.setExtent(new Extent(0, image.getWidth(), 0, image.getHeight()));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create image
     *
     * @param gdata data array
     * @param extent Extent
     * @return Image graphic
     */
    public static Graphic createImage(Array gdata, List<Number> extent) {
        int width, height;
        width = gdata.getShape()[1];
        height = gdata.getShape()[0];
        Color undefColor = Color.white;
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Color color;
        Index index = gdata.getIndex();
        boolean isAlpha = gdata.getShape()[2] == 4;
        if (gdata.getDataType() == DataType.FLOAT || gdata.getDataType() == DataType.DOUBLE) {
            float r, g, b;
            if (isAlpha) {
                float a;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = gdata.getFloat(index.set(i, j, 0));
                        g = gdata.getFloat(index.set(i, j, 1));
                        b = gdata.getFloat(index.set(i, j, 2));
                        a = gdata.getFloat(index.set(i, j, 3));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = gdata.getFloat(index.set(i, j, 0));
                        g = gdata.getFloat(index.set(i, j, 1));
                        b = gdata.getFloat(index.set(i, j, 2));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        } else {
            int r, g, b;
            if (isAlpha) {
                int a;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = gdata.getInt(index.set(i, j, 0));
                        g = gdata.getInt(index.set(i, j, 1));
                        b = gdata.getInt(index.set(i, j, 2));
                        a = gdata.getInt(index.set(i, j, 3));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = gdata.getInt(index.set(i, j, 0));
                        g = gdata.getInt(index.set(i, j, 1));
                        b = gdata.getInt(index.set(i, j, 2));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        }

        ImageShape ishape = new ImageShape();
        double minx, maxx, miny, maxy;
        if (extent == null) {
            minx = 0;
            maxx = width;
            miny = 0;
            maxy = height;
        } else {
            minx = extent.get(0).doubleValue();
            maxx = extent.get(1).doubleValue();
            miny = extent.get(2).doubleValue();
            maxy = extent.get(3).doubleValue();
        }
        ishape.setPoint(new PointD(minx, miny));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(minx, maxx, miny, maxy));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create image by RGB data array
     *
     * @param data RGB data array list
     * @param extent Exent
     * @return Image graphic
     */
    public static Graphic createImage(List<Array> data, List<Number> extent) {
        int width, height;
        width = data.get(0).getShape()[1];
        height = data.get(0).getShape()[0];
        Color undefColor = Color.white;
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Color color;
        boolean isAlpha = data.size() == 4;
        Array rdata = data.get(0);
        Array gdata = data.get(1);
        Array bdata = data.get(2);
        Index rindex = rdata.getIndex();
        Index gindex = gdata.getIndex();
        Index bindex = bdata.getIndex();
        if (rdata.getDataType() == DataType.FLOAT || rdata.getDataType() == DataType.DOUBLE) {
            float r, g, b;
            if (isAlpha) {
                float a;
                Array adata = data.get(3);
                Index aindex = adata.getIndex();
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = rdata.getFloat(rindex.set(i, j));
                        g = gdata.getFloat(gindex.set(i, j));
                        b = bdata.getFloat(bindex.set(i, j));
                        a = adata.getFloat(aindex.set(i, j));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = rdata.getFloat(rindex.set(i, j));
                        g = gdata.getFloat(gindex.set(i, j));
                        b = bdata.getFloat(bindex.set(i, j));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        } else {
            int r, g, b;
            if (isAlpha) {
                int a;
                Array adata = data.get(3);
                Index aindex = adata.getIndex();
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = rdata.getInt(rindex.set(i, j));
                        g = gdata.getInt(gindex.set(i, j));
                        b = bdata.getInt(bindex.set(i, j));
                        a = adata.getInt(aindex.set(i, j));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = rdata.getInt(rindex.set(i, j));
                        g = gdata.getInt(gindex.set(i, j));
                        b = bdata.getInt(bindex.set(i, j));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        }

        ImageShape ishape = new ImageShape();
        double minx, maxx, miny, maxy;
        if (extent == null) {
            minx = 0;
            maxx = width;
            miny = 0;
            maxy = height;
        } else {
            minx = extent.get(0).doubleValue();
            maxx = extent.get(1).doubleValue();
            miny = extent.get(2).doubleValue();
            maxy = extent.get(3).doubleValue();
        }
        ishape.setPoint(new PointD(minx, miny));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(minx, maxx, miny, maxy));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create image by RGB data array
     *
     * @param x X data array
     * @param y Y data array
     * @param data RGB data array list
     * @param offset Offset in z axis
     * @param zdir Z direction - x, y or z
     * @param interpolation Interpolation
     * @return Graphics
     */
    public static GraphicCollection createImage(Array x, Array y, List<Array> data, double offset,
            String zdir, String interpolation) {
        Graphic gg = createImage(data, null);
        if (interpolation != null) {
            ((ImageShape) gg.getShape()).setInterpolation(interpolation);
        }
        Shape shape = gg.getShape();
        Extent extent = shape.getExtent();
        Extent3D ex3 = new Extent3D();
        switch (zdir.toLowerCase()) {
            case "x":
                ex3 = new Extent3D(offset, offset, extent.minX, extent.maxX, extent.minY, extent.maxY);
                break;
            case "y":
                ex3 = new Extent3D(extent.minX, extent.maxX, offset, offset, extent.minY, extent.maxY);
                break;
            case "z":
                ex3 = new Extent3D(extent.minX, extent.maxX, extent.minY, extent.maxY, offset, offset);
                break;
        }
        shape.setExtent(ex3);
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZDir(zdir);
        graphics.setZValue(offset);
        graphics.add(gg);
        return graphics;
    }

    /**
     * Create image
     *
     * @param gdata Grid data array
     * @param ls Legend scheme
     * @param extent Extent
     * @return Image graphic
     */
    public static Graphic createImage(Array gdata, LegendScheme ls, List<Number> extent) {
        int width, height, breakNum;
        width = gdata.getShape()[1];
        height = gdata.getShape()[0];
        breakNum = ls.getBreakNum();
        double[] breakValue = new double[breakNum];
        Color[] breakColor = new Color[breakNum];
        Color undefColor = Color.white;
        for (int i = 0; i < breakNum; i++) {
            breakValue[i] = Double.parseDouble(ls.getLegendBreaks().get(i).getEndValue().toString());
            breakColor[i] = ls.getLegendBreaks().get(i).getColor();
            if (ls.getLegendBreaks().get(i).isNoData()) {
                undefColor = ls.getLegendBreaks().get(i).getColor();
            }
        }
        Color defaultColor = breakColor[breakNum - 1];    //默认颜色为最后一个颜色
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //oneValue = gdata.data[i][j];
                oneValue = gdata.getDouble(i * width + j);
                if (Double.isNaN(oneValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    if (ls.getLegendType() == LegendType.GraduatedColor) {
                        //循环只到breakNum-1 是因为最后一个LegendBreaks的EndValue和StartValue是一样的
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue < breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    } else {
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue == breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    }
                }
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        ImageShape ishape = new ImageShape();
        double minx, maxx, miny, maxy;
        if (extent == null) {
            minx = 0;
            maxx = width;
            miny = 0;
            maxy = height;
        } else {
            minx = extent.get(0).doubleValue();
            maxx = extent.get(1).doubleValue();
            miny = extent.get(2).doubleValue();
            maxy = extent.get(3).doubleValue();
        }
        ishape.setPoint(new PointD(minx, miny));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(minx, maxx, miny, maxy));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create image
     *
     * @param gdata Grid data array
     * @param ls Legend scheme
     * @return Image graphic
     */
    public static Graphic createImage(GridArray gdata, LegendScheme ls) {
        int width, height, breakNum;
        width = gdata.getXNum();
        height = gdata.getYNum();
        breakNum = ls.getBreakNum();
        double[] breakValue = new double[breakNum];
        Color[] breakColor = new Color[breakNum];
        Color undefColor = Color.white;
        for (int i = 0; i < breakNum; i++) {
            breakValue[i] = Double.parseDouble(ls.getLegendBreaks().get(i).getEndValue().toString());
            breakColor[i] = ls.getLegendBreaks().get(i).getColor();
            if (ls.getLegendBreaks().get(i).isNoData()) {
                undefColor = ls.getLegendBreaks().get(i).getColor();
            }
        }
        Color defaultColor = breakColor[breakNum - 1];    //默认颜色为最后一个颜色
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //oneValue = gdata.data[i][j];
                oneValue = gdata.getDoubleValue(i, j);
                if (Double.isNaN(oneValue) || MIMath.doubleEquals(oneValue, gdata.missingValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    if (ls.getLegendType() == LegendType.GraduatedColor) {
                        //循环只到breakNum-1 是因为最后一个LegendBreaks的EndValue和StartValue是一样的
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue < breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    } else {
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue == breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    }
                }
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        ImageShape ishape = new ImageShape();
        double xdelta = BigDecimalUtil.mul(gdata.getXDelt(), 0.5);
        double xmin = BigDecimalUtil.sub(gdata.xArray[0], xdelta);
        double xmax = BigDecimalUtil.add(gdata.getXMax(), xdelta);
        double ydelta = BigDecimalUtil.mul(gdata.getYDelt(), 0.5);
        double ymin = BigDecimalUtil.sub(gdata.yArray[0], ydelta);
        double ymax = BigDecimalUtil.add(gdata.getYMax(), ydelta);
        ishape.setPoint(new PointD(xmin, ymin));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(xmin, xmax, ymin, ymax));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create image
     *
     * @param gdata Grid data array
     * @param ls Legend scheme
     * @param extent Extent
     * @return Image graphic
     */
    public static Graphic createImage(GridArray gdata, LegendScheme ls, List<Number> extent) {
        int width, height, breakNum;
        width = gdata.getXNum();
        height = gdata.getYNum();
        breakNum = ls.getBreakNum();
        double[] breakValue = new double[breakNum];
        Color[] breakColor = new Color[breakNum];
        Color undefColor = Color.white;
        for (int i = 0; i < breakNum; i++) {
            breakValue[i] = Double.parseDouble(ls.getLegendBreaks().get(i).getEndValue().toString());
            breakColor[i] = ls.getLegendBreaks().get(i).getColor();
            if (ls.getLegendBreaks().get(i).isNoData()) {
                undefColor = ls.getLegendBreaks().get(i).getColor();
            }
        }
        Color defaultColor = breakColor[breakNum - 1];    //默认颜色为最后一个颜色
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //oneValue = gdata.data[i][j];
                oneValue = gdata.getDoubleValue(i, j);
                if (Double.isNaN(oneValue) || MIMath.doubleEquals(oneValue, gdata.missingValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    if (ls.getLegendType() == LegendType.GraduatedColor) {
                        //循环只到breakNum-1 是因为最后一个LegendBreaks的EndValue和StartValue是一样的
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue < breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    } else {
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue == breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    }
                }
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        ImageShape ishape = new ImageShape();
        double xmin, xmax, ymin, ymax;
        if (extent == null) {
            double xdelta = BigDecimalUtil.mul(gdata.getXDelt(), 0.5);
            xmin = BigDecimalUtil.sub(gdata.xArray[0], xdelta);
            xmax = BigDecimalUtil.add(gdata.getXMax(), xdelta);
            double ydelta = BigDecimalUtil.mul(gdata.getYDelt(), 0.5);
            ymin = BigDecimalUtil.sub(gdata.yArray[0], ydelta);
            ymax = BigDecimalUtil.add(gdata.getYMax(), ydelta);
        } else {
            xmin = extent.get(0).doubleValue();
            xmax = extent.get(1).doubleValue();
            ymin = extent.get(2).doubleValue();
            ymax = extent.get(3).doubleValue();
        }
        ishape.setPoint(new PointD(xmin, ymin));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(xmin, xmax, ymin, ymax));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create image
     *
     * @param gdata Grid data array
     * @param ls Legend scheme
     * @param offset Offset of z axis
     * @param zdir Z direction - x, y or z
     * @param sePoint Start and end points [xstart, ystart, xend, yend]
     * @param interpolation Interpolation
     * @return Graphics
     */
    public static GraphicCollection createImage(GridArray gdata, LegendScheme ls, double offset,
            String zdir, List<Number> sePoint, String interpolation) {
        Graphic gg = createImage(gdata, ls);
        if (interpolation != null) {
            ((ImageShape) gg.getShape()).setInterpolation(interpolation);
        }
        Shape shape = gg.getShape();
        Extent extent = shape.getExtent();
        Extent3D ex3 = new Extent3D();
        switch (zdir.toLowerCase()) {
            case "x":
                ex3 = new Extent3D(offset, offset, extent.minX, extent.maxX, extent.minY, extent.maxY);
                break;
            case "y":
                ex3 = new Extent3D(extent.minX, extent.maxX, offset, offset, extent.minY, extent.maxY);
                break;
            case "xy":
                ex3 = new Extent3D(sePoint.get(0).doubleValue(), sePoint.get(2).doubleValue(),
                        sePoint.get(1).doubleValue(), sePoint.get(3).doubleValue(), extent.minY, extent.maxY);
                break;
            case "z":
                ex3 = new Extent3D(extent.minX, extent.maxX, extent.minY, extent.maxY, offset, offset);
                break;
        }
        shape.setExtent(ex3);
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZDir(zdir);
        graphics.setZValue(offset);
        graphics.setSEPoint(sePoint);
        graphics.add(gg);
        graphics.setLegendScheme(ls);
        graphics.setSingleLegend(false);
        return graphics;
    }

    /**
     * Create image
     *
     * @param layer Image layer
     * @param offset Offset of z axis
     * @param xshift X shift - to shift the grahpics in x direction, normally
     * for map in 180 - 360 degree east
     * @param interpolation Interpolation
     * @return Graphics
     */
    public static GraphicCollection createImage(ImageLayer layer, double offset, double xshift,
            String interpolation) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZDir("z");
        graphics.setZValue(offset);
        ImageShape ishape = new ImageShape();
        ishape.setImage(layer.getImage());
        Extent extent = layer.getExtent();
        Extent3D ex3 = new Extent3D(extent.minX + xshift, extent.maxX + xshift, extent.minY, extent.maxY, offset, offset);
        ishape.setExtent(ex3);
        Graphic gg = new Graphic(ishape, new ColorBreak());
        if (interpolation != null) {
            ((ImageShape) gg.getShape()).setInterpolation(interpolation);
        }
        graphics.add(gg);

        return graphics;
    }

    /**
     * Create contour lines
     *
     * @param gridData Grid data
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour lines
     */
    public static GraphicCollection createContourLines(GridData gridData, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.Polyline);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, S1);
        List<wContour.Global.PolyLine> ContourLines = (List<wContour.Global.PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = wContour.Contour.smoothLines(ContourLines);
        }

        wContour.Global.PolyLine aLine;
        double v;
        ColorBreak cbb = ls.findLegendBreak(0);
        GraphicCollection graphics = new GraphicCollection();
        for (int i = 0; i < ContourLines.size(); i++) {
            aLine = ContourLines.get(i);
            v = aLine.Value;

            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (int j = 0; j < aLine.PointList.size(); j++) {
                aPoint = new PointD();
                aPoint.X = aLine.PointList.get(j).X;
                aPoint.Y = aLine.PointList.get(j).Y;
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(v);
            aPolyline.setExtent(MIMath.getPointsExtent(pList));

            switch (ls.getLegendType()) {
                case UniqueValue:
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
                case GraduatedColor:
                    int blNum = 0;
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        blNum += 1;
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))
                                || (v > Double.parseDouble(cb.getStartValue().toString())
                                && v < Double.parseDouble(cb.getEndValue().toString()))
                                || (blNum == ls.getBreakNum() && v == Double.parseDouble(cb.getEndValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
            }
            graphics.add(new Graphic(aPolyline, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create contour lines
     *
     * @param gridData Grid data
     * @param offset Offset in z direction
     * @param zdir Z direction - x, y or z
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour lines
     */
    public static GraphicCollection createContourLines(GridData gridData, double offset,
            String zdir, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.Polyline);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, S1);
        List<wContour.Global.PolyLine> ContourLines = (List<wContour.Global.PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = wContour.Contour.smoothLines(ContourLines);
        }

        wContour.Global.PolyLine aLine;
        double v;
        ColorBreak cbb;
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZValue(offset);
        zdir = zdir.toLowerCase();
        graphics.setZDir(zdir);
        for (int i = 0; i < ContourLines.size(); i++) {
            aLine = ContourLines.get(i);
            v = aLine.Value;

            PolylineZShape aPolyline = new PolylineZShape();
            PointZ aPoint;
            List<PointZ> pList = new ArrayList<>();
            switch (zdir) {
                case "x":
                    for (int j = 0; j < aLine.PointList.size(); j++) {
                        aPoint = new PointZ();
                        aPoint.X = offset;
                        aPoint.Y = aLine.PointList.get(j).X;
                        aPoint.Z = aLine.PointList.get(j).Y;
                        pList.add(aPoint);
                    }
                    break;
                case "y":
                    for (int j = 0; j < aLine.PointList.size(); j++) {
                        aPoint = new PointZ();
                        aPoint.Y = offset;
                        aPoint.X = aLine.PointList.get(j).X;
                        aPoint.Z = aLine.PointList.get(j).Y;
                        pList.add(aPoint);
                    }
                    break;
                case "z":
                    for (int j = 0; j < aLine.PointList.size(); j++) {
                        aPoint = new PointZ();
                        aPoint.X = aLine.PointList.get(j).X;
                        aPoint.Y = aLine.PointList.get(j).Y;
                        aPoint.Z = offset;
                        pList.add(aPoint);
                    }
                    break;
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(v);
            aPolyline.setExtent(MIMath.getPointsExtent(pList));
            cbb = ls.findLegendBreak(v);
            graphics.add(new Graphic(aPolyline, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create contour lines
     *
     * @param gridData Grid data
     * @param offset Offset in z direction
     * @param zdir Z direction - x, y or z
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @param sePoint Start and end points [xstart, ystart, xend, yend]
     * @return Contour lines
     */
    public static GraphicCollection createContourLines(GridData gridData, double offset,
            String zdir, LegendScheme ls, boolean isSmooth, List<Number> sePoint) {
        ls = ls.convertTo(ShapeTypes.Polyline);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, S1);
        List<wContour.Global.PolyLine> ContourLines = (List<wContour.Global.PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = wContour.Contour.smoothLines(ContourLines);
        }

        wContour.Global.PolyLine aLine;
        double v;
        ColorBreak cbb;
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZValue(offset);
        graphics.setSEPoint(sePoint);
        zdir = zdir.toLowerCase();
        graphics.setZDir(zdir);
        double x, y, xs, xe, ys, ye;
        xs = sePoint.get(0).doubleValue();
        ys = sePoint.get(1).doubleValue();
        xe = sePoint.get(2).doubleValue();
        ye = sePoint.get(3).doubleValue();
        for (int i = 0; i < ContourLines.size(); i++) {
            aLine = ContourLines.get(i);
            v = aLine.Value;

            PolylineZShape aPolyline = new PolylineZShape();
            PointZ aPoint;
            List<PointZ> pList = new ArrayList<>();
            switch (zdir) {
                case "x":
                    for (int j = 0; j < aLine.PointList.size(); j++) {
                        aPoint = new PointZ();
                        aPoint.X = offset;
                        aPoint.Y = aLine.PointList.get(j).X;
                        aPoint.Z = aLine.PointList.get(j).Y;
                        pList.add(aPoint);
                    }
                    break;
                case "y":
                    for (int j = 0; j < aLine.PointList.size(); j++) {
                        aPoint = new PointZ();
                        aPoint.Y = offset;
                        aPoint.X = aLine.PointList.get(j).X;
                        aPoint.Z = aLine.PointList.get(j).Y;
                        pList.add(aPoint);
                    }
                    break;
                case "xy":
                    for (int j = 0; j < aLine.PointList.size(); j++) {
                        x = aLine.PointList.get(j).X;
                        y = aLine.PointList.get(j).Y;
                        aPoint = new PointZ();
                        aPoint.X = x;
                        aPoint.Y = ys + (ye - ys) * (x - xs) / (xe - xs);
                        aPoint.Z = y;
                        pList.add(aPoint);
                    }
                    break;
                case "z":
                    for (int j = 0; j < aLine.PointList.size(); j++) {
                        aPoint = new PointZ();
                        aPoint.X = aLine.PointList.get(j).X;
                        aPoint.Y = aLine.PointList.get(j).Y;
                        aPoint.Z = offset;
                        pList.add(aPoint);
                    }
                    break;
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(v);
            aPolyline.setExtent(MIMath.getPointsExtent(pList));
            cbb = ls.findLegendBreak(v);
            graphics.add(new Graphic(aPolyline, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create contour polygons
     *
     * @param gridData Grid data
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour polygons
     */
    public static GraphicCollection createContourPolygons(GridData gridData, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.Polygon);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        double minData;
        double maxData;
        double[] maxmin = new double[2];
        gridData.getMaxMinValue(maxmin);
        maxData = maxmin[0];
        minData = maxmin[1];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, S1);
        List<wContour.Global.PolyLine> contourLines = (List<wContour.Global.PolyLine>) cbs[0];
        List<wContour.Global.Border> borders = (List<wContour.Global.Border>) cbs[1];

        if (isSmooth) {
            contourLines = wContour.Contour.smoothLines(contourLines);
        }
        List<wContour.Global.Polygon> contourPolygons = ContourDraw.tracingPolygons(gridData.data, contourLines, borders, cValues);

        double v;
        ColorBreak cbb = ls.findLegendBreak(0);
        GraphicCollection graphics = new GraphicCollection();
        for (int i = 0; i < contourPolygons.size(); i++) {
            wContour.Global.Polygon poly = contourPolygons.get(i);
            v = poly.LowValue;
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                aPoint = new PointD();
                aPoint.X = pointList.X;
                aPoint.Y = pointList.Y;
                pList.add(aPoint);
            }
            if (!GeoComputation.isClockwise(pList)) {
                Collections.reverse(pList);
            }
            PolygonShape aPolygonShape = new PolygonShape();
            aPolygonShape.setPoints(pList);
            aPolygonShape.setExtent(MIMath.getPointsExtent(pList));
            aPolygonShape.lowValue = v;
            if (poly.HasHoles()) {
                for (PolyLine holeLine : poly.HoleLines) {
                    pList = new ArrayList<>();
                    for (wContour.Global.PointD pointList : holeLine.PointList) {
                        aPoint = new PointD();
                        aPoint.X = pointList.X;
                        aPoint.Y = pointList.Y;
                        pList.add(aPoint);
                    }
                    aPolygonShape.addHole(pList, 0);
                }
            }
            int valueIdx = Arrays.binarySearch(cValues, v);
            if (valueIdx < 0) {
                valueIdx = -valueIdx;
            }
            //int valueIdx = findIndex(cValues, v);            
            if (valueIdx == cValues.length - 1) {
                aPolygonShape.highValue = maxData;
            } else {
                aPolygonShape.highValue = cValues[valueIdx + 1];
            }
//            if (!aPolygon.IsBorder) {
//                if (!aPolygon.IsHighCenter) {
//                    aPolygonShape.highValue = aValue;
//                    if (valueIdx == 0) {
//                        aPolygonShape.lowValue = minData;
//                    } else {
//                        aPolygonShape.lowValue = cValues[valueIdx - 1];
//                    }
//                }
//            }
            if (!poly.IsHighCenter && poly.HighValue == poly.LowValue) {
                aPolygonShape.highValue = v;
                if (valueIdx == 0) {
                    aPolygonShape.lowValue = minData;
                } else {
                    aPolygonShape.lowValue = cValues[valueIdx - 1];
                }
            }

            v = aPolygonShape.lowValue;
            switch (ls.getLegendType()) {
                case UniqueValue:
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
                case GraduatedColor:
                    int blNum = 0;
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        blNum += 1;
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))
                                || (v > Double.parseDouble(cb.getStartValue().toString())
                                && v < Double.parseDouble(cb.getEndValue().toString()))
                                || (blNum == ls.getBreakNum() && v == Double.parseDouble(cb.getEndValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
            }
            graphics.add(new Graphic(aPolygonShape, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create 3D contour polygons
     *
     * @param gridData Grid data
     * @param offset Offset of z axis
     * @param zdir Z direction - x, y or z
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour polygons
     */
    public static GraphicCollection createContourPolygons(GridData gridData, double offset,
            String zdir, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.Polygon);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        double minData;
        double maxData;
        double[] maxmin = new double[2];
        gridData.getMaxMinValue(maxmin);
        maxData = maxmin[0];
        minData = maxmin[1];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, S1);
        List<wContour.Global.PolyLine> contourLines = (List<wContour.Global.PolyLine>) cbs[0];
        List<wContour.Global.Border> borders = (List<wContour.Global.Border>) cbs[1];

        if (isSmooth) {
            contourLines = wContour.Contour.smoothLines(contourLines);
        }
        List<wContour.Global.Polygon> contourPolygons = ContourDraw.tracingPolygons(gridData.data, contourLines, borders, cValues);

        double v;
        ColorBreak cbb;
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZValue(offset);
        zdir = zdir.toLowerCase();
        graphics.setZDir(zdir);
        for (int i = 0; i < contourPolygons.size(); i++) {
            wContour.Global.Polygon poly = contourPolygons.get(i);
            v = poly.LowValue;
            PointZ aPoint;
            List<PointZ> pList = new ArrayList<>();
            switch (zdir) {
                case "x":
                    for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.Y = pointList.X;
                        aPoint.Z = pointList.Y;
                        aPoint.X = offset;
                        pList.add(aPoint);
                    }
                    break;
                case "y":
                    for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.X = pointList.X;
                        aPoint.Z = pointList.Y;
                        aPoint.Y = offset;
                        pList.add(aPoint);
                    }
                    break;
                case "z":
                    for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.X = pointList.X;
                        aPoint.Y = pointList.Y;
                        aPoint.Z = offset;
                        pList.add(aPoint);
                    }
                    break;
            }

            if (!GeoComputation.isClockwise(pList)) {
                Collections.reverse(pList);
            }
            PolygonZShape aPolygonShape = new PolygonZShape();
            aPolygonShape.setPoints(pList);
            aPolygonShape.setExtent(MIMath.getPointsExtent(pList));
            aPolygonShape.lowValue = v;
            if (poly.HasHoles()) {
                switch (zdir) {
                    case "x":
                        for (PolyLine holeLine : poly.HoleLines) {
                            pList = new ArrayList<>();
                            for (wContour.Global.PointD pointList : holeLine.PointList) {
                                aPoint = new PointZ();
                                aPoint.Y = pointList.X;
                                aPoint.Z = pointList.Y;
                                aPoint.X = offset;
                                pList.add(aPoint);
                            }
                            aPolygonShape.addHole(pList, 0);
                        }
                        break;
                    case "y":
                        for (PolyLine holeLine : poly.HoleLines) {
                            pList = new ArrayList<>();
                            for (wContour.Global.PointD pointList : holeLine.PointList) {
                                aPoint = new PointZ();
                                aPoint.X = pointList.X;
                                aPoint.Z = pointList.Y;
                                aPoint.Y = offset;
                                pList.add(aPoint);
                            }
                            aPolygonShape.addHole(pList, 0);
                        }
                        break;
                    case "z":
                        for (PolyLine holeLine : poly.HoleLines) {
                            pList = new ArrayList<>();
                            for (wContour.Global.PointD pointList : holeLine.PointList) {
                                aPoint = new PointZ();
                                aPoint.X = pointList.X;
                                aPoint.Y = pointList.Y;
                                aPoint.Z = offset;
                                pList.add(aPoint);
                            }
                            aPolygonShape.addHole(pList, 0);
                        }
                        break;
                }
            }
            int valueIdx = Arrays.binarySearch(cValues, v);
            if (valueIdx < 0) {
                valueIdx = -valueIdx;
            }
            //int valueIdx = findIndex(cValues, v);           
            if (valueIdx == cValues.length - 1) {
                aPolygonShape.highValue = maxData;
            } else {
                aPolygonShape.highValue = cValues[valueIdx + 1];
            }
            if (!poly.IsHighCenter && poly.HighValue == poly.LowValue) {
                aPolygonShape.highValue = v;
                if (valueIdx == 0) {
                    aPolygonShape.lowValue = minData;
                } else {
                    aPolygonShape.lowValue = cValues[valueIdx - 1];
                }
            }

            v = aPolygonShape.lowValue;
            cbb = ls.findLegendBreak(v);
            graphics.add(new Graphic(aPolygonShape, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create 3D contour polygons
     *
     * @param gridData Grid data
     * @param offset Offset of z axis
     * @param zdir Z direction - x, y or z
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @param sePoint Start and end points [xstart, ystart, xend, yend]
     * @return Contour polygons
     */
    public static GraphicCollection createContourPolygons(GridData gridData, double offset,
            String zdir, LegendScheme ls, boolean isSmooth, List<Number> sePoint) {
        ls = ls.convertTo(ShapeTypes.Polygon);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        double minData;
        double maxData;
        double[] maxmin = new double[2];
        gridData.getMaxMinValue(maxmin);
        maxData = maxmin[0];
        minData = maxmin[1];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, S1);
        List<wContour.Global.PolyLine> contourLines = (List<wContour.Global.PolyLine>) cbs[0];
        List<wContour.Global.Border> borders = (List<wContour.Global.Border>) cbs[1];

        if (isSmooth) {
            contourLines = wContour.Contour.smoothLines(contourLines);
        }
        List<wContour.Global.Polygon> contourPolygons = ContourDraw.tracingPolygons(gridData.data, contourLines, borders, cValues);

        double v;
        ColorBreak cbb;
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZValue(offset);
        graphics.setSEPoint(sePoint);
        zdir = zdir.toLowerCase();
        graphics.setZDir(zdir);
        double x, y, xs, xe, ys, ye;
        xs = sePoint.get(0).doubleValue();
        ys = sePoint.get(1).doubleValue();
        xe = sePoint.get(2).doubleValue();
        ye = sePoint.get(3).doubleValue();
        for (int i = 0; i < contourPolygons.size(); i++) {
            wContour.Global.Polygon poly = contourPolygons.get(i);
            v = poly.LowValue;
            PointZ aPoint;
            List<PointZ> pList = new ArrayList<>();
            switch (zdir) {
                case "x":
                    for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.Y = pointList.X;
                        aPoint.Z = pointList.Y;
                        aPoint.X = offset;
                        pList.add(aPoint);
                    }
                    break;
                case "y":
                    for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.X = pointList.X;
                        aPoint.Z = pointList.Y;
                        aPoint.Y = offset;
                        pList.add(aPoint);
                    }
                    break;
                case "xy":
                    for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                        x = pointList.X;
                        y = pointList.Y;
                        aPoint = new PointZ();
                        aPoint.X = x;
                        aPoint.Y = ys + (ye - ys) * (x - xs) / (xe - xs);
                        aPoint.Z = y;
                        pList.add(aPoint);
                    }
                    break;
                case "z":
                    for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.X = pointList.X;
                        aPoint.Y = pointList.Y;
                        aPoint.Z = offset;
                        pList.add(aPoint);
                    }
                    break;
            }

            if (!GeoComputation.isClockwise(pList)) {
                Collections.reverse(pList);
            }
            PolygonZShape aPolygonShape = new PolygonZShape();
            aPolygonShape.setPoints(pList);
            aPolygonShape.setExtent(MIMath.getPointsExtent(pList));
            aPolygonShape.lowValue = v;
            if (poly.HasHoles()) {
                switch (zdir) {
                    case "x":
                        for (PolyLine holeLine : poly.HoleLines) {
                            pList = new ArrayList<>();
                            for (wContour.Global.PointD pointList : holeLine.PointList) {
                                aPoint = new PointZ();
                                aPoint.Y = pointList.X;
                                aPoint.Z = pointList.Y;
                                aPoint.X = offset;
                                pList.add(aPoint);
                            }
                            aPolygonShape.addHole(pList, 0);
                        }
                        break;
                    case "y":
                        for (PolyLine holeLine : poly.HoleLines) {
                            pList = new ArrayList<>();
                            for (wContour.Global.PointD pointList : holeLine.PointList) {
                                aPoint = new PointZ();
                                aPoint.X = pointList.X;
                                aPoint.Z = pointList.Y;
                                aPoint.Y = offset;
                                pList.add(aPoint);
                            }
                            aPolygonShape.addHole(pList, 0);
                        }
                        break;
                    case "z":
                        for (PolyLine holeLine : poly.HoleLines) {
                            pList = new ArrayList<>();
                            for (wContour.Global.PointD pointList : holeLine.PointList) {
                                aPoint = new PointZ();
                                aPoint.X = pointList.X;
                                aPoint.Y = pointList.Y;
                                aPoint.Z = offset;
                                pList.add(aPoint);
                            }
                            aPolygonShape.addHole(pList, 0);
                        }
                        break;
                }
            }
            int valueIdx = Arrays.binarySearch(cValues, v);
            if (valueIdx < 0) {
                valueIdx = -valueIdx;
            }
            //int valueIdx = findIndex(cValues, v);           
            if (valueIdx == cValues.length - 1) {
                aPolygonShape.highValue = maxData;
            } else {
                aPolygonShape.highValue = cValues[valueIdx + 1];
            }
            if (!poly.IsHighCenter && poly.HighValue == poly.LowValue) {
                aPolygonShape.highValue = v;
                if (valueIdx == 0) {
                    aPolygonShape.lowValue = minData;
                } else {
                    aPolygonShape.lowValue = cValues[valueIdx - 1];
                }
            }

            v = aPolygonShape.lowValue;
            cbb = ls.findLegendBreak(v);
            graphics.add(new Graphic(aPolygonShape, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create pseudocolor polygons
     *
     * @param x_s scatter X array - 2D
     * @param y_s scatter Y array - 2D
     * @param a scatter value array - 2D
     * @param ls Legend scheme
     * @return Mesh polygon layer
     */
    public static GraphicCollection createPColorPolygons(Array x_s, Array y_s, Array a, LegendScheme ls) {
        GraphicCollection gc = new GraphicCollection();

        int[] shape = x_s.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        double x1, x2, x3, x4, v;
        PolygonBreak pb;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                x1 = x_s.getDouble(i * colNum + j);
                x2 = x_s.getDouble(i * colNum + j + 1);
                x3 = x_s.getDouble((i + 1) * colNum + j);
                x4 = x_s.getDouble((i + 1) * colNum + j + 1);
                PolygonShape ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(x1, y_s.getDouble(i * colNum + j)));
                points.add(new PointD(x3, y_s.getDouble((i + 1) * colNum + j)));
                points.add(new PointD(x4, y_s.getDouble((i + 1) * colNum + j + 1)));
                points.add(new PointD(x2, y_s.getDouble(i * colNum + j + 1)));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                v = a.getDouble(i * colNum + j);
                pb = (PolygonBreak) ls.findLegendBreak(v);
                Graphic graphic = new Graphic(ps, pb);
                gc.add(graphic);
            }
        }

        gc.setSingleLegend(false);
        gc.setLegendScheme(ls);

        return gc;
    }

    /**
     * Create grid polygons
     *
     * @param x_s X array - 1D
     * @param y_s Y array - 1D
     * @param a scatter value array - 2D
     * @param ls Legend scheme
     * @return Grid polygons
     */
    public static GraphicCollection createGridPolygons(Array x_s, Array y_s, Array a, LegendScheme ls) {
        GraphicCollection gc = new GraphicCollection();

        int colNum = (int) x_s.getSize();
        int rowNum = (int) y_s.getSize();
        double x, x1 = 0, x2, y, y1 = 0, y2, xd, yd, v;
        PolygonBreak pb;
        for (int i = 0; i < rowNum; i++) {
            if (i == 0) {
                y1 = y_s.getDouble(i);
            }
            y = y_s.getDouble(i);
            if (i < rowNum - 1) {
                y2 = y_s.getDouble(i + 1);
                yd = y2 - y;
            } else {
                y2 = y_s.getDouble(i - 1);
                yd = y - y2;
            }
            if (i == 0) {
                y1 = y1 - yd * 0.5;
            }
            y2 = y + yd * 0.5;
            for (int j = 0; j < colNum; j++) {
                if (j == 0) {
                    x1 = x_s.getDouble(j);
                }
                x = x_s.getDouble(j);
                if (j < colNum - 1) {
                    x2 = x_s.getDouble(j + 1);
                    xd = x2 - x;
                } else {
                    x2 = x_s.getDouble(j - 1);
                    xd = x - x2;
                }
                x2 = x + xd * 0.5;
                PolygonShape ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(x1, y1));
                points.add(new PointD(x1, y2));
                points.add(new PointD(x2, y2));
                points.add(new PointD(x2, y1));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                v = a.getDouble(i * colNum + j);
                pb = (PolygonBreak) ls.findLegendBreak(v);
                Graphic graphic = new Graphic(ps, pb);
                gc.add(graphic);
                x1 = x2;
            }
            y1 = y2;
        }

        gc.setSingleLegend(false);
        gc.setLegendScheme(ls);

        return gc;
    }

    /**
     * Create fill between polygons
     *
     * @param xdata X data array
     * @param y1data Y1 data array
     * @param y2data Y2 data array
     * @param where Where data array
     * @param pb Polygon break
     * @return GraphicCollection
     */
    public static GraphicCollection createFillBetweenPolygons(Array xdata, Array y1data,
            Array y2data, Array where, PolygonBreak pb) {
        GraphicCollection gc = new GraphicCollection();
        int len = (int) xdata.getSize();
        if (where == null) {
            if (ArrayMath.containsNaN(y1data) || ArrayMath.containsNaN(y2data)) {
                where = new ArrayBoolean(new int[]{len});
                double v1, v2;
                for (int i = 0; i < len; i++) {
                    v1 = y1data.getDouble(i);
                    v2 = y2data.getDouble(i);
                    if (Double.isNaN(v1) || Double.isNaN(v2)) {
                        where.setBoolean(i, false);
                    } else {
                        where.setBoolean(i, true);
                    }
                }
            }
        }
        if (where == null) {
            PolygonShape pgs = new PolygonShape();
            List<PointD> points = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                points.add(new PointD(xdata.getDouble(i), y1data.getDouble(i)));
            }
            for (int i = len - 1; i >= 0; i--) {
                points.add(new PointD(xdata.getDouble(i), y2data.getDouble(i)));
            }
            pgs.setPoints(points);
            Graphic graphic = new Graphic(pgs, pb);
            gc.add(graphic);
        } else {
            boolean ob = false;
            List<List<Integer>> idxs = new ArrayList<>();
            List<Integer> idx = new ArrayList<>();
            for (int j = 0; j < len; j++) {
                if (where.getInt(j) == 1) {
                    idx.add(j);
                } else if (ob) {
                    idxs.add(idx);
                    idx = new ArrayList<>();
                }
                ob = where.getInt(j) == 1;
            }
            if (!idx.isEmpty()) {
                idxs.add(idx);
            }
            for (List<Integer> index : idxs) {
                int nn = index.size();
                if (nn >= 2) {
                    PolygonShape pgs = new PolygonShape();
                    List<PointD> points = new ArrayList<>();
                    int ii;
                    for (int j = 0; j < nn; j++) {
                        ii = index.get(j);
                        points.add(new PointD(xdata.getDouble(ii), y1data.getDouble(ii)));
                    }
                    for (int j = 0; j < nn; j++) {
                        ii = index.get(nn - j - 1);
                        points.add(new PointD(xdata.getDouble(ii), y2data.getDouble(ii)));
                    }
                    pgs.setPoints(points);
                    Graphic graphic = new Graphic(pgs, pb);
                    gc.add(graphic);
                }
            }
        }

        return gc;
    }

    /**
     * Create fill between polygons - X direction
     *
     * @param ydata Y data array
     * @param x1data X1 data array
     * @param x2data X2 data array
     * @param where Where data array
     * @param pb Polygon break
     * @return GraphicCollection
     */
    public static GraphicCollection createFillBetweenPolygonsX(Array ydata, Array x1data,
            Array x2data, Array where, PolygonBreak pb) {
        GraphicCollection gc = new GraphicCollection();
        int len = (int) ydata.getSize();
        if (where == null) {
            if (ArrayMath.containsNaN(x1data) || ArrayMath.containsNaN(x2data)) {
                where = new ArrayBoolean(new int[]{len});
                double v1, v2;
                for (int i = 0; i < len; i++) {
                    v1 = x1data.getDouble(i);
                    v2 = x2data.getDouble(i);
                    if (Double.isNaN(v1) || Double.isNaN(v2)) {
                        where.setBoolean(i, false);
                    } else {
                        where.setBoolean(i, true);
                    }
                }
            }
        }
        if (where == null) {
            PolygonShape pgs = new PolygonShape();
            List<PointD> points = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                points.add(new PointD(x1data.getDouble(i), ydata.getDouble(i)));
            }
            for (int i = len - 1; i >= 0; i--) {
                points.add(new PointD(x2data.getDouble(i), ydata.getDouble(i)));
            }
            pgs.setPoints(points);
            Graphic graphic = new Graphic(pgs, pb);
            gc.add(graphic);
        } else {
            boolean ob = false;
            List<List<Integer>> idxs = new ArrayList<>();
            List<Integer> idx = new ArrayList<>();
            for (int j = 0; j < len; j++) {
                if (where.getInt(j) == 1) {
                    idx.add(j);
                } else if (ob) {
                    idxs.add(idx);
                    idx = new ArrayList<>();
                }
                ob = where.getInt(j) == 1;
            }
            if (!idx.isEmpty()) {
                idxs.add(idx);
            }
            for (List<Integer> index : idxs) {
                int nn = index.size();
                if (nn >= 2) {
                    PolygonShape pgs = new PolygonShape();
                    List<PointD> points = new ArrayList<>();
                    int ii;
                    for (int j = 0; j < nn; j++) {
                        ii = index.get(j);
                        points.add(new PointD(x1data.getDouble(ii), ydata.getDouble(ii)));
                    }
                    for (int j = 0; j < nn; j++) {
                        ii = index.get(nn - j - 1);
                        points.add(new PointD(x2data.getDouble(ii), ydata.getDouble(ii)));
                    }
                    pgs.setPoints(points);
                    Graphic graphic = new Graphic(pgs, pb);
                    gc.add(graphic);
                }
            }
        }

        return gc;
    }

    /**
     * Create fill between polygons
     *
     * @param xdata X data array
     * @param y1data Y1 data array
     * @param y2data Y2 data array
     * @param where Where data array
     * @param pb Polygon break
     * @param offset Offset
     * @param zdir Zdir
     * @return GraphicCollection
     */
    public static GraphicCollection createFillBetweenPolygons(Array xdata, Array y1data,
            Array y2data, Array where, PolygonBreak pb, double offset, String zdir) {
        GraphicCollection3D gc = new GraphicCollection3D();
        gc.setFixZ(true);
        gc.setZValue(offset);
        gc.setZDir(zdir);
        int len = (int) xdata.getSize();
        if (where == null) {
            if (ArrayMath.containsNaN(y1data) || ArrayMath.containsNaN(y2data)) {
                where = new ArrayBoolean(new int[]{len});
                double v1, v2;
                for (int i = 0; i < len; i++) {
                    v1 = y1data.getDouble(i);
                    v2 = y2data.getDouble(i);
                    if (Double.isNaN(v1) || Double.isNaN(v2)) {
                        where.setBoolean(i, false);
                    } else {
                        where.setBoolean(i, true);
                    }
                }
            }
        }
        if (where == null) {
            PolygonZShape pgs = new PolygonZShape();
            List<PointZ> points = new ArrayList<>();
            switch (zdir) {
                case "x":
                    for (int i = 0; i < len; i++) {
                        points.add(new PointZ(offset, xdata.getDouble(i), y1data.getDouble(i)));
                    }
                    for (int i = len - 1; i >= 0; i--) {
                        points.add(new PointZ(offset, xdata.getDouble(i), y2data.getDouble(i)));
                    }
                    break;
                case "y":
                    for (int i = 0; i < len; i++) {
                        points.add(new PointZ(xdata.getDouble(i), offset, y1data.getDouble(i)));
                    }
                    for (int i = len - 1; i >= 0; i--) {
                        points.add(new PointZ(xdata.getDouble(i), offset, y2data.getDouble(i)));
                    }
                    break;
                case "z":
                    for (int i = 0; i < len; i++) {
                        points.add(new PointZ(xdata.getDouble(i), y1data.getDouble(i), offset));
                    }
                    for (int i = len - 1; i >= 0; i--) {
                        points.add(new PointZ(xdata.getDouble(i), y2data.getDouble(i), offset));
                    }
                    break;
            }
            pgs.setPoints(points);
            Graphic graphic = new Graphic(pgs, pb);
            gc.add(graphic);
        } else {
            boolean ob = false;
            List<List<Integer>> idxs = new ArrayList<>();
            List<Integer> idx = new ArrayList<>();
            for (int j = 0; j < len; j++) {
                if (where.getInt(j) == 1) {
                    idx.add(j);
                } else if (ob) {
                    idxs.add(idx);
                    idx = new ArrayList<>();
                }
                ob = where.getInt(j) == 1;
            }
            if (!idx.isEmpty()) {
                idxs.add(idx);
            }
            for (List<Integer> index : idxs) {
                int nn = index.size();
                if (nn >= 2) {
                    PolygonZShape pgs = new PolygonZShape();
                    List<PointZ> points = new ArrayList<>();
                    int ii;
                    switch (zdir) {
                        case "x":
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(j);
                                points.add(new PointZ(offset, xdata.getDouble(ii), y1data.getDouble(ii)));
                            }
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(nn - j - 1);
                                points.add(new PointZ(offset, xdata.getDouble(ii), y2data.getDouble(ii)));
                            }
                            break;
                        case "y":
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(j);
                                points.add(new PointZ(xdata.getDouble(ii), offset, y1data.getDouble(ii)));
                            }
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(nn - j - 1);
                                points.add(new PointZ(xdata.getDouble(ii), offset, y2data.getDouble(ii)));
                            }
                            break;
                        case "z":
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(j);
                                points.add(new PointZ(xdata.getDouble(ii), y1data.getDouble(ii), offset));
                            }
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(nn - j - 1);
                                points.add(new PointZ(xdata.getDouble(ii), y2data.getDouble(ii), offset));
                            }
                            break;
                    }
                    pgs.setPoints(points);
                    Graphic graphic = new Graphic(pgs, pb);
                    gc.add(graphic);
                }
            }
        }

        return gc;
    }

    /**
     * Create fill between polygons
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param y1data Y1 data array
     * @param y2data Y2 data array
     * @param where Where data array
     * @param pb Polygon break
     * @param offset Offset
     * @param zdir Zdir
     * @return GraphicCollection
     */
    public static GraphicCollection createFillBetweenPolygons(Array xdata, Array ydata, Array y1data,
            Array y2data, Array where, PolygonBreak pb, double offset, String zdir) {
        GraphicCollection3D gc = new GraphicCollection3D();
        gc.setFixZ(true);
        gc.setZValue(offset);
        gc.setZDir(zdir);
        int len = (int) xdata.getSize();
        if (where == null) {
            PolygonZShape pgs = new PolygonZShape();
            List<PointZ> points = new ArrayList<>();
            switch (zdir) {
                case "x":
                    for (int i = 0; i < len; i++) {
                        points.add(new PointZ(offset, xdata.getDouble(i), y1data.getDouble(i)));
                    }
                    for (int i = len - 1; i >= 0; i--) {
                        points.add(new PointZ(offset, xdata.getDouble(i), y2data.getDouble(i)));
                    }
                    break;
                case "y":
                    for (int i = 0; i < len; i++) {
                        points.add(new PointZ(xdata.getDouble(i), offset, y1data.getDouble(i)));
                    }
                    for (int i = len - 1; i >= 0; i--) {
                        points.add(new PointZ(xdata.getDouble(i), offset, y2data.getDouble(i)));
                    }
                    break;
                case "xy":
                    for (int i = 0; i < len; i++) {
                        points.add(new PointZ(xdata.getDouble(i), ydata.getDouble(i), y1data.getDouble(i)));
                    }
                    for (int i = len - 1; i >= 0; i--) {
                        points.add(new PointZ(xdata.getDouble(i), ydata.getDouble(i), y2data.getDouble(i)));
                    }
                    break;
                case "z":
                    for (int i = 0; i < len; i++) {
                        points.add(new PointZ(xdata.getDouble(i), y1data.getDouble(i), offset));
                    }
                    for (int i = len - 1; i >= 0; i--) {
                        points.add(new PointZ(xdata.getDouble(i), y2data.getDouble(i), offset));
                    }
                    break;
            }
            pgs.setPoints(points);
            Graphic graphic = new Graphic(pgs, pb);
            gc.add(graphic);
        } else {
            boolean ob = false;
            List<List<Integer>> idxs = new ArrayList<>();
            List<Integer> idx = new ArrayList<>();
            for (int j = 0; j < len; j++) {
                if (where.getInt(j) == 1) {
                    if (!ob) {
                        idx = new ArrayList<>();
                    }
                    idx.add(j);
                } else if (ob) {
                    idxs.add(idx);
                }
                ob = where.getInt(j) == 1;
            }
            for (List<Integer> index : idxs) {
                int nn = index.size();
                if (nn >= 2) {
                    PolygonZShape pgs = new PolygonZShape();
                    List<PointZ> points = new ArrayList<>();
                    int ii;
                    switch (zdir) {
                        case "x":
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(j);
                                points.add(new PointZ(offset, xdata.getDouble(ii), y1data.getDouble(ii)));
                            }
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(nn - j - 1);
                                points.add(new PointZ(offset, xdata.getDouble(ii), y2data.getDouble(ii)));
                            }
                            break;
                        case "y":
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(j);
                                points.add(new PointZ(xdata.getDouble(ii), offset, y1data.getDouble(ii)));
                            }
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(nn - j - 1);
                                points.add(new PointZ(xdata.getDouble(ii), offset, y2data.getDouble(ii)));
                            }
                            break;
                        case "xy":
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(j);
                                points.add(new PointZ(xdata.getDouble(ii), ydata.getDouble(ii), y1data.getDouble(ii)));
                            }
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(nn - j - 1);
                                points.add(new PointZ(xdata.getDouble(ii), ydata.getDouble(ii), y2data.getDouble(ii)));
                            }
                            break;
                        case "z":
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(j);
                                points.add(new PointZ(xdata.getDouble(ii), y1data.getDouble(ii), offset));
                            }
                            for (int j = 0; j < nn; j++) {
                                ii = index.get(nn - j - 1);
                                points.add(new PointZ(xdata.getDouble(ii), y2data.getDouble(ii), offset));
                            }
                            break;
                    }
                    pgs.setPoints(points);
                    Graphic graphic = new Graphic(pgs, pb);
                    gc.add(graphic);
                }
            }
        }

        return gc;
    }

    /**
     * Create wind barbs
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param udata U/WindDirection data array
     * @param vdata V/WindSpeed data array
     * @param cdata Colored data array
     * @param ls Legend scheme
     * @param isUV Is U/V or not
     * @return GraphicCollection
     */
    public static GraphicCollection createBarbs(Array xdata, Array ydata, Array udata, Array vdata,
            Array cdata, LegendScheme ls, boolean isUV) {
        GraphicCollection gc = new GraphicCollection();
        Array windDirData;
        Array windSpeedData;
        if (isUV) {
            Array[] wwData = ArrayMath.uv2ds(udata, vdata);
            windDirData = wwData[0];
            windSpeedData = wwData[1];
        } else {
            windDirData = udata;
            windSpeedData = vdata;
        }

        ShapeTypes sts = ls.getShapeType();
        ls = ls.convertTo(ShapeTypes.Point);
        if (sts != ShapeTypes.Point) {
            for (int i = 0; i < ls.getBreakNum(); i++) {
                ((PointBreak) ls.getLegendBreaks().get(i)).setSize(10);
            }
        }

        int i, j;
        WindBarb aWB;
        double windDir, windSpeed;
        PointD aPoint;
        ColorBreak cb;
        double v;
        int dn = (int) xdata.getSize();
        for (i = 0; i < dn; i++) {
            windDir = windDirData.getDouble(i);
            windSpeed = windSpeedData.getDouble(i);
            if (!Double.isNaN(windDir)) {
                if (!Double.isNaN(windSpeed)) {
                    aPoint = new PointD();
                    aPoint.X = xdata.getDouble(i);
                    aPoint.Y = ydata.getDouble(i);
                    aWB = Draw.calWindBarb((float) windDir, (float) windSpeed, 0, 10, aPoint);
                    if (cdata == null) {
                        cb = ls.getLegendBreaks().get(0);
                    } else {
                        v = cdata.getDouble(i);
                        aWB.setValue(v);
                        cb = ls.findLegendBreak(v);
                    }
                    Graphic graphic = new Graphic(aWB, cb);
                    gc.add(graphic);
                }
            }
        }

        gc.setLegendScheme(ls);
        if (cdata != null) {
            gc.setSingleLegend(false);
        }

        return gc;
    }
    
    /**
     * Create arrow polygon
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param dx The length of arrow along x direction
     * @param dy The length of arrow along y direction
     * @param ab The arrow polygon break
     * @return Arrow polygon graphic
     */
    public static Graphic createArrow(double x, double y, double dx, double dy, ArrowPolygonBreak ab) {
        double[] r = ArrayMath.uv2ds(dx, dy);
        double length = r[1];
        if (ab.isLengthIncludesHead()) {
            length = length - ab.getHeadLength();
        }
        
        AffineTransform atf = new AffineTransform();
        atf.translate(x, y);
        atf.rotate(dx, dy);
        
        float width = ab.getWidth();   
        float headLength = ab.getHeadLength();
        float overhang = ab.getOverhang();
        float lenShift = headLength * overhang;
        double[] srcPts = new double[8 * 2];
        srcPts[0] = 0;
        srcPts[1] = -width * 0.5;
        srcPts[2] = 0;
        srcPts[3] = width * 0.5;
        srcPts[4] = length + lenShift;
        srcPts[5] = width * 0.5;
        srcPts[6] = length;
        srcPts[7] = ab.getHeadWidth() * 0.5;
        srcPts[8] = length + ab.getHeadLength();
        srcPts[9] = 0;
        srcPts[10] = length;
        srcPts[11] = -ab.getHeadWidth() * 0.5;
        srcPts[12] = length + lenShift;
        srcPts[13] = -width * 0.5;
        srcPts[14] = 0;
        srcPts[15] = -width * 0.5;
        atf.transform(srcPts, 0, srcPts, 0, 8);
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < srcPts.length; i+=2) {
            points.add(new PointD(srcPts[i], srcPts[i + 1]));
        }
        PolygonShape pgs = new PolygonShape();
        pgs.setPoints(points);
        
        return new Graphic(pgs, ab);
    }
    
    /**
     * Create arrow line
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param dx The length of arrow along x direction
     * @param dy The length of arrow along y direction
     * @param ab The arrow line break
     * @return Arrow line graphic
     */
    public static Graphic createArrowLine(double x, double y, double dx, double dy, ArrowLineBreak ab) {
        List<PointD> points = new ArrayList<>();
        points.add(new PointD(x, y));
        points.add(new PointD(x + dx, y + dy));
        PolylineShape pls = new PolylineShape();
        pls.setPoints(points);
        
        return new Graphic(pls, ab);
    }
    
    /**
     * Create arrow line
     * 
     * @param x X coordinates
     * @param y Y coordinates
     * @param ab The arrow line break
     * @param iscurve Is curve or not
     * @return Arrow line graphic
     */
    public static Graphic createArrowLine(Array x, Array y, ArrowLineBreak ab, boolean iscurve) {
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x.getSize(); i++) {
            points.add(new PointD(x.getDouble(i), y.getDouble(i)));
        }
        PolylineShape pls;
        if (iscurve)
            pls = new CurveLineShape();
        else
            pls = new PolylineShape();
        pls.setPoints(points);
        
        return new Graphic(pls, ab);
    }

    /**
     * Create wind arrows
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param udata U/WindDirection data array
     * @param vdata V/WindSpeed data array
     * @param cdata Colored data array
     * @param ls Legend scheme
     * @param isUV Is U/V or not
     * @return GraphicCollection
     */
    public static GraphicCollection createArrows(Array xdata, Array ydata, Array udata, Array vdata,
            Array cdata, LegendScheme ls, boolean isUV) {
        GraphicCollection gc = new GraphicCollection();
        Array windDirData;
        Array windSpeedData;
        if (isUV) {
            Array[] wwData = ArrayMath.uv2ds(udata, vdata);
            windDirData = wwData[0];
            windSpeedData = wwData[1];
        } else {
            windDirData = udata;
            windSpeedData = vdata;
        }

        ShapeTypes sts = ls.getShapeType();
        ls = ls.convertTo(ShapeTypes.Point);
        if (sts != ShapeTypes.Point) {
            for (int i = 0; i < ls.getBreakNum(); i++) {
                ((PointBreak) ls.getLegendBreaks().get(i)).setSize(10);
            }
        }

        int i;
        WindArrow wa;
        double windDir, windSpeed;
        PointD aPoint;
        ColorBreak cb;
        double v;
        int dn = (int) xdata.getSize();
        float size = 6;
        for (i = 0; i < dn; i++) {
            windDir = windDirData.getDouble(i);
            windSpeed = windSpeedData.getDouble(i);
            if (!Double.isNaN(windDir)) {
                if (!Double.isNaN(windSpeed)) {
                    aPoint = new PointD();
                    aPoint.X = xdata.getDouble(i);
                    aPoint.Y = ydata.getDouble(i);
                    wa = new WindArrow();
                    wa.angle = windDir;
                    wa.length = (float) windSpeed;
                    wa.size = size;
                    wa.setPoint(aPoint);
                    if (cdata == null) {
                        cb = ls.getLegendBreaks().get(0);
                    } else {
                        v = cdata.getDouble(i);
                        wa.setValue(v);
                        cb = ls.findLegendBreak(v);
                    }
                    Graphic graphic = new Graphic(wa, cb);
                    gc.add(graphic);
                }
            }
        }

        gc.setLegendScheme(ls);
        if (cdata != null) {
            gc.setSingleLegend(false);
        }

        return gc;
    }
    
    /**
     * Create wind arrows
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param udata U data array
     * @param vdata V data array
     * @param wdata W data array
     * @param length The length of each wind arrow
     * @param cdata Colored data array
     * @param ls Legend scheme
     * @return GraphicCollection
     */
    public static GraphicCollection createArrows3D(Array xdata, Array ydata, Array zdata, Array udata, 
            Array vdata, Array wdata, float length, Array cdata, LegendScheme ls) {
        GraphicCollection gc = new GraphicCollection();
        ShapeTypes sts = ls.getShapeType();
        ls = ls.convertTo(ShapeTypes.Point);
        if (sts != ShapeTypes.Point) {
            for (int i = 0; i < ls.getBreakNum(); i++) {
                ((PointBreak) ls.getLegendBreaks().get(i)).setSize(10);
            }
        }

        int i;
        WindArrow3D wa;
        double u, v, w;
        PointZ aPoint;
        ColorBreak cb;
        double value;
        int dn = (int) xdata.getSize();
        for (i = 0; i < dn; i++) {
            u = udata.getDouble(i);
            v = vdata.getDouble(i);
            w = wdata.getDouble(i);
            if (!Double.isNaN(u) && !Double.isNaN(v) && !Double.isNaN(w)) {
                    aPoint = new PointZ();
                    aPoint.X = xdata.getDouble(i);
                    aPoint.Y = ydata.getDouble(i);
                    aPoint.Z = zdata.getDouble(i);
                    wa = new WindArrow3D();
                    wa.u = u;
                    wa.v = v;
                    wa.w = w;
                    wa.length = length;
                    wa.setPoint(aPoint);
                    if (cdata == null) {
                        cb = ls.getLegendBreaks().get(0);
                    } else {
                        value = cdata.getDouble(i);
                        wa.setValue(value);
                        cb = ls.findLegendBreak(value);
                    }
                    Graphic graphic = new Graphic(wa, cb);
                    gc.add(graphic);
            }
        }

        gc.setLegendScheme(ls);
        if (cdata != null) {
            gc.setSingleLegend(false);
        }

        return gc;
    }
    
//    /**
//     * Create annotate
//     * @param text The text
//     * @param x X coordinate
//     * @param y Y coordinate
//     * @param xText X coordinate of the text
//     * @param yText Y coordinate of the text
//     * @param ab Arrow line break
//     * @return Arrow line break and text
//     */
//    public static GraphicCollection createAnnotate(ChartText text, float x, float y, 
//            float xText, float yText, ArrowLineBreak ab) {
//        GraphicCollection gc = new GraphicCollection();
//        Graphic gg = createArrowLine(x, y, xText - x, xText - y, ab);
//        gc.add(gg)
//    }

    /**
     * Create pie arc polygons
     *
     * @param xdata X data array
     * @param colors Colors
     * @param labels Labels
     * @param startAngle Start angle
     * @param explode Explode
     * @param labelFont Label font
     * @param labelColor Label color
     * @param labelDis Label distance
     * @param autopct pct format
     * @param pctDis pct distance
     * @return GraphicCollection
     */
    public static GraphicCollection[] createPieArcs(Array xdata, List<Color> colors,
            List<String> labels, float startAngle, List<Number> explode, Font labelFont,
            Color labelColor, float labelDis, String autopct, float pctDis) {
        GraphicCollection gc = new GraphicCollection();
        GraphicCollection lgc = new GraphicCollection();
        GraphicCollection pgc = new GraphicCollection();
        double sum = ArrayMath.sum(xdata);
        double v;
        int n = (int) xdata.getSize();
        float sweepAngle, angle;
        float ex;
        double dx, dy, ldx, ldy, r = 1;
        String label, pct = null;
        LegendScheme ls = new LegendScheme(ShapeTypes.Polygon);
        for (int i = 0; i < n; i++) {
            v = xdata.getDouble(i);
            if (Double.isNaN(v)) {
                continue;
            }

            if (sum > 1) {
                v = v / sum;
            }
            sweepAngle = (float) (360.0 * v);
            ArcShape aShape = new ArcShape();
            aShape.setStartAngle(startAngle);
            aShape.setSweepAngle(sweepAngle);
            angle = startAngle + sweepAngle / 2;
            if (explode == null) {
                dx = 0;
                dy = 0;
            } else {
                ex = explode.get(i).floatValue();
                aShape.setExplode(ex);
                dx = r * ex * Math.cos(angle * Math.PI / 180);
                dy = r * ex * Math.sin(angle * Math.PI / 180);
            }
            List<PointD> points = new ArrayList<>();
            points.add(new PointD(-r + dx, -r + dy));
            points.add(new PointD(-r + dx, r + dy));
            points.add(new PointD(r + dx, r + dy));
            points.add(new PointD(r + dx, -r + dy));
            points.add(new PointD(dx, dy));
            aShape.setPoints(points);
            PolygonBreak pgb = new PolygonBreak();
            pgb.setColor(colors.get(i));
            if (labels == null) {
                if (autopct == null) {
                    label = "";
                } else {
                    label = String.format(autopct, v * 100);
                }
            } else {
                label = labels.get(i);
                if (autopct != null) {
                    pct = String.format(autopct, v * 100);
                }
            }
            pgb.setCaption(label);
            Graphic graphic = new Graphic(aShape, pgb);
            gc.add(graphic);
            ls.addLegendBreak(pgb);

            //Label text
            ChartText ps = new ChartText();
            ldx = dx + r * labelDis * Math.cos(angle * Math.PI / 180);
            ldy = dy + r * labelDis * Math.sin(angle * Math.PI / 180);
            ps.setPoint(ldx, ldy);
            ps.setText(label);
            ps.setFont(labelFont);
            ps.setColor(labelColor);
            if (angle > 90 && angle < 270) {
                ps.setXAlign(XAlign.RIGHT);
            }
            if (angle > 180 && angle < 360) {
                ps.setYAlign(YAlign.TOP);
            }
            if (angle == 0 || angle == 180) {
                ps.setYAlign(YAlign.CENTER);
            } else if (angle == 90 || angle == 270) {
                ps.setXAlign(XAlign.CENTER);
            }
            lgc.add(new Graphic(ps, new ColorBreak()));

            //pct text
            if (pct != null) {
                ps = new ChartText();
                ldx = dx + r * pctDis * Math.cos(angle * Math.PI / 180);
                ldy = dy + r * pctDis * Math.sin(angle * Math.PI / 180);
                ps.setPoint(ldx, ldy);
                ps.setText(pct);
                ps.setFont(labelFont);
                ps.setColor(labelColor);
                ps.setXAlign(XAlign.CENTER);
                ps.setYAlign(YAlign.CENTER);
                pgc.add(new Graphic(ps, new ColorBreak()));
            }

            startAngle += sweepAngle;
        }
        gc.setSingleLegend(false);
        gc.setLegendScheme(ls);
        gc.getLabelSet().setLabelFont(labelFont);
        gc.getLabelSet().setLabelColor(labelColor);
        dx = r * 0.1;
        if (labels != null || autopct != null) {
            Extent ext = gc.getExtent().extend(dx, dx);
            gc.setExtent(ext);
        }

        if (pct == null) {
            return new GraphicCollection[]{gc, lgc};
        } else {
            return new GraphicCollection[]{gc, lgc, pgc};
        }
    }

    /**
     * Create box graphics
     *
     * @param xdata X data array list
     * @param positions Box position list
     * @param widths Box width list
     * @param showcaps Show caps or not
     * @param showfliers Show fliers or not
     * @param showmeans Show means or not
     * @param boxBreak Box polygon break
     * @param medianBreak Meandian line break
     * @param whiskerBreak Whisker line break
     * @param capBreak Whisker cap line break
     * @param meanBreak Mean point break
     * @param flierBreak Flier point break
     * @return GraphicCollection
     */
    public static GraphicCollection createBox(List<Array> xdata, List<Number> positions, List<Number> widths,
            boolean showcaps, boolean showfliers, boolean showmeans, PolygonBreak boxBreak,
            PolylineBreak medianBreak, PolylineBreak whiskerBreak, PolylineBreak capBreak,
            ColorBreak meanBreak, PointBreak flierBreak) {
        GraphicCollection gc = new GraphicCollection();
        int n = xdata.size();
        if (positions == null) {
            positions = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                positions.add(i + 1);
            }
        }
        if (widths == null) {
            widths = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                widths.add(0.5);
            }
        }
        double v, width;
        if (boxBreak == null) {
            boxBreak = new PolygonBreak();
            boxBreak.setDrawFill(false);
            boxBreak.setOutlineColor(Color.blue);
        }
        if (medianBreak == null) {
            medianBreak = new PolylineBreak();
            medianBreak.setColor(Color.red);
        }
        if (whiskerBreak == null) {
            whiskerBreak = new PolylineBreak();
            whiskerBreak.setColor(Color.black);
            whiskerBreak.setStyle(LineStyles.DASH);
        }
        if (capBreak == null) {
            capBreak = new PolylineBreak();
            capBreak.setColor(Color.black);
        }
        if (flierBreak == null) {
            flierBreak = new PointBreak();
            flierBreak.setStyle(PointStyle.Plus);
        }
        if (meanBreak == null) {
            meanBreak = new PointBreak();
            ((PointBreak) meanBreak).setStyle(PointStyle.Square);
            ((PointBreak) meanBreak).setColor(Color.red);
            ((PointBreak) meanBreak).setOutlineColor(Color.black);
        }

        for (int i = 0; i < n; i++) {
            Array a = xdata.get(i);
            if (Double.isNaN(ArrayMath.min(a))) {
                continue;
            }

            v = positions.get(i).doubleValue();
            width = widths.get(i).doubleValue();
            //Add box polygon
            double q1 = Statistics.quantile(a, 1);
            double q3 = Statistics.quantile(a, 3);
            double median = Statistics.quantile(a, 2);
            double mind = ArrayMath.getMinimum(a);
            double maxd = ArrayMath.getMaximum(a);
            double mino = q1 - (q3 - q1) * 1.5;
            double maxo = q3 + (q3 - q1) * 1.5;
            List<PointD> pList = new ArrayList<>();
            pList.add(new PointD(v - width * 0.5, q1));
            pList.add(new PointD(v - width * 0.5, q3));
            pList.add(new PointD(v + width * 0.5, q3));
            pList.add(new PointD(v + width * 0.5, q1));
            pList.add(new PointD(v - width * 0.5, q1));
            PolygonShape pgs = new PolygonShape();
            pgs.setPoints(pList);
            gc.add(new Graphic(pgs, boxBreak));

            //Add meadian line
            pList = new ArrayList<>();
            pList.add(new PointD(v - width * 0.5, median));
            pList.add(new PointD(v + width * 0.5, median));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            gc.add(new Graphic(pls, medianBreak));

            //Add low whisker line
            double min = Math.max(mino, mind);
            pList = new ArrayList<>();
            pList.add(new PointD(v, q1));
            pList.add(new PointD(v, min));
            pls = new PolylineShape();
            pls.setPoints(pList);
            gc.add(new Graphic(pls, whiskerBreak));
            //Add cap
            if (showcaps) {
                pList = new ArrayList<>();
                pList.add(new PointD(v - width * 0.25, min));
                pList.add(new PointD(v + width * 0.25, min));
                pls = new PolylineShape();
                pls.setPoints(pList);
                gc.add(new Graphic(pls, capBreak));
            }
            //Add low fliers
            if (showfliers) {
                if (mino > mind) {
                    for (int j = 0; j < a.getSize(); j++) {
                        if (a.getDouble(j) < mino) {
                            PointShape ps = new PointShape();
                            ps.setPoint(new PointD(v, a.getDouble(j)));
                            gc.add(new Graphic(ps, flierBreak));
                        }
                    }
                }
            }

            //Add high whisker line
            double max = Math.min(maxo, maxd);
            pList = new ArrayList<>();
            pList.add(new PointD(v, q3));
            pList.add(new PointD(v, max));
            pls = new PolylineShape();
            pls.setPoints(pList);
            gc.add(new Graphic(pls, whiskerBreak));
            //Add cap
            if (showcaps) {
                pList = new ArrayList<>();
                pList.add(new PointD(v - width * 0.25, max));
                pList.add(new PointD(v + width * 0.25, max));
                pls = new PolylineShape();
                pls.setPoints(pList);
                gc.add(new Graphic(pls, capBreak));
            }
            //Add high fliers
            if (showfliers) {
                if (maxo < maxd) {
                    for (int j = 0; j < a.getSize(); j++) {
                        if (a.getDouble(j) > maxo) {
                            PointShape ps = new PointShape();
                            ps.setPoint(new PointD(v, a.getDouble(j)));
                            gc.add(new Graphic(ps, flierBreak));
                        }
                    }
                }
            }

            //Add mean line
            if (showmeans) {
                double mean = ArrayMath.mean(a);
                PointShape ps = new PointShape();
                ps.setPoint(new PointD(v, mean));
                gc.add(new Graphic(ps, meanBreak));
            }
        }
        gc.setSingleLegend(false);

        return gc;
    }

    /**
     * Convert graphics from polar to cartesian coordinate
     *
     * @param graphics Graphics
     */
    public static void polarToCartesian(GraphicCollection graphics) {
        for (int m = 0; m < graphics.getNumGraphics(); m++) {
            Graphic graphic = graphics.get(m);
            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                Shape shape = gg.getShape();
                List<PointD> points = new ArrayList<>();
                for (PointD p : shape.getPoints()) {
                    double[] xy = MIMath.polarToCartesian(p.X, p.Y);
                    points.add(new PointD(xy[0], xy[1]));
                }
                shape.setPoints(points);
            }
        }
        graphics.updateExtent();
    }

    private static int findIndex(double[] values, double v) {
        int idx = -1;
        for (int i = 0; i < values.length; i++) {
            if (i == values.length - 1) {
                if (v == values[i]) {
                    idx = i;
                    break;
                }
            } else {
                if (v == values[i] || v < values[i + 1]) {
                    idx = i;
                    break;
                }
            }
        }

        return idx;
    }

}
