/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.graphic;

import org.apache.commons.lang3.ArrayUtils;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.ChartText3D;
import org.meteoinfo.chart.jogl.mc.CallbackMC;
import org.meteoinfo.chart.jogl.mc.MarchingCubes;
import org.meteoinfo.chart.jogl.pipe.PipeShape;
import org.meteoinfo.chart.shape.TextureShape;
import org.meteoinfo.common.*;
import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.XYListDataset;
import org.meteoinfo.data.analysis.Statistics;
import org.meteoinfo.geo.drawing.ContourDraw;
import org.meteoinfo.geo.drawing.Draw;
import org.meteoinfo.geo.layer.ImageLayer;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geo.legend.LegendManage;
import org.meteoinfo.geometry.colors.Normalize;
import org.meteoinfo.geometry.colors.OpacityTransferFunction;
import org.meteoinfo.geometry.colors.TransferFunction;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.graphic.ImageGraphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.geoprocess.GeometryUtil;
import org.meteoinfo.math.interpolate.InterpUtil;
import org.meteoinfo.math.interpolate.InterpolationMethod;
import org.meteoinfo.math.interpolate.RectNearestInterpolator3D;
import org.meteoinfo.math.meteo.MeteoMath;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.ndarray.util.BigDecimalUtil;
import org.meteoinfo.projection.ProjectionInfo;
import wcontour.Contour;
import wcontour.global.Point3D;
import wcontour.global.PolyLine;
import wcontour.global.PolyLine3D;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        if (xdata.getRank() == 1) {
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
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
                    x = xIter.getDoubleNext();
                    y = yIter.getDoubleNext();
                    //x = xdata.getDouble(j * xn + i);
                    //y = ydata.getDouble(j * xn + i);
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        ColorBreak cb;
        if (xdata.getRank() == 1) {
            points = new ArrayList<>();
            int i = 0;
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
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
                    cb = cbs.get(i);
                    gc.add(new Graphic(pls, cb));
                    points = new ArrayList<>();
                    i += 1;
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
                cb = cbs.get(i);
                gc.add(new Graphic(pls, cb));
            }
        } else {    //Two dimensions
            int[] shape = xdata.getShape();
            int yn = shape[0];
            int xn = shape[1];
            for (int j = 0; j < yn; j++) {
                points = new ArrayList<>();
                cb = cbs.get(j);
                for (int i = 0; i < xn; i++) {
                    x = xIter.getDoubleNext();
                    y = yIter.getDoubleNext();
                    //x = xdata.getDouble(j * xn + i);
                    //y = ydata.getDouble(j * xn + i);
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
        gc.setSingleLegend(false);

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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        ColorBreak cb;
        if (xdata.getRank() == 1) {
            points = new ArrayList<>();
            cbc = new ColorBreakCollection();
            while (xIter.hasNext()){
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                z = zIter.getDoubleNext();
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
                    x = xIter.getDoubleNext();
                    y = yIter.getDoubleNext();
                    z = zIter.getDoubleNext();
                    //x = xdata.getDouble(j * xn + i);
                    //y = ydata.getDouble(j * xn + i);
                    //z = zdata.getDouble(j * xn + i);
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        boolean fixZ = false;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z = zdata.getDouble(0);
        }
        if (xdata.getRank() == 1) {
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                if (!fixZ) {
                    z = zIter.getDoubleNext();
                }
                if (Double.isNaN(y) || Double.isNaN(x) || Double.isNaN(z)) {
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
            for (int j = 0; j < yn; j++) {
                for (int i = 0; i < xn; i++) {
                    x = xIter.getDoubleNext();
                    y = yIter.getDoubleNext();
                    //x = xdata.getDouble(j * xn + i);
                    //y = ydata.getDouble(j * xn + i);
                    if (!fixZ) {
                        z = zIter.getDoubleNext();
                        //z = zdata.getDouble(j * xn + i);
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        boolean fixZ = false;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z = zdata.getDouble(0);
        }
        ColorBreak cb;
        if (xdata.getRank() == 1) {
            int i = 0;
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                if (!fixZ) {
                    z = zIter.getDoubleNext();
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
                    cb = cbs.get(i);
                    gc.add(new Graphic(pls, cb));
                    points = new ArrayList<>();
                    i += 1;
                } else {
                    points.add(new PointZ(x, y, z));
                }
            }
            if (points.size() > 1) {
                pls = new PolylineZShape();
                pls.setPoints(points);
                cb = cbs.get(i);
                gc.add(new Graphic(pls, cb));
            }
        } else {    //two dimensions
            int[] shape = xdata.getShape();
            int yn = shape[0];
            int xn = shape[1];
            for (int j = 0; j < yn; j++) {
                cb = cbs.get(j);
                for (int i = 0; i < xn; i++) {
                    x = xIter.getDoubleNext();
                    y = yIter.getDoubleNext();
                    //x = xdata.getDouble(j * xn + i);
                    //y = ydata.getDouble(j * xn + i);
                    if (!fixZ) {
                        z = zIter.getDoubleNext();
                        //z = zdata.getDouble(j * xn + i);
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
     * LineString 3D to pipe graphics
     * @param lines LineStrings
     * @param radius Radius
     * @param steps Steps
     * @return Pipe graphics
     */
    public static GraphicCollection3D lineString3DToPipe(GraphicCollection3D lines, float radius,
                                                         int steps) {
        for (Graphic graphic : lines.getGraphics()) {
            PolylineZShape shape = (PolylineZShape) graphic.getShape();
            PipeShape pipeShape = new PipeShape(shape, radius, steps);
            graphic.setShape(pipeShape);
        }
        return lines;
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        IndexIterator mIter = mdata.getIndexIterator();
        boolean fixZ = false;
        if (zdata.getSize() == 1 && xdata.getSize() > 1) {
            fixZ = true;
            z = zdata.getDouble(0);
        }
        ColorBreak cb;
        ColorBreakCollection cbs;
        if (xdata.getRank() == 1) {
            cbs = new ColorBreakCollection();
            while (xIter.hasNext()){
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                if (!fixZ) {
                    z = zIter.getDoubleNext();
                }
                m = mIter.getDoubleNext();
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
            for (int j = 0; j < yn; j++) {
                cbs = new ColorBreakCollection();
                for (int i = 0; i < xn; i++) {
                    x = xIter.getDoubleNext();
                    y = yIter.getDoubleNext();
                    //x = xdata.getDouble(j * xn + i);
                    //y = ydata.getDouble(j * xn + i);
                    if (!fixZ) {
                        z = zIter.getDoubleNext();
                        //z = zdata.getDouble(j * xn + i);
                    }
                    m = mIter.getDoubleNext();
                    //m = mdata.getDouble(j * xn + i);
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
                                                          Array xErrorRight, Array yErrorBottom, Array yErrorUp, ColorBreak cb, PolylineBreak ecb, float capSize) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        CapPolylineShape epls;
        List<PointD> points = new ArrayList<>();
        List<PointD> eps;
        double x, y, xerrL, xerrR, yerrB, yerrU;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator xelIter = xErrorLeft == null ? null : xErrorLeft.getIndexIterator();
        IndexIterator xerIter = xErrorRight == null ? null : xErrorRight.getIndexIterator();
        IndexIterator yebIter = yErrorBottom == null ? null : yErrorBottom.getIndexIterator();
        IndexIterator yeuIter = yErrorUp == null ? null : yErrorUp.getIndexIterator();
        //Loop
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
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
                if (yebIter != null) {
                    yebIter.next();
                    yeuIter.next();
                }
                if (xelIter != null) {
                    xelIter.next();
                    xerIter.next();
                }
            } else {
                points.add(new PointD(x, y));
                if (yebIter != null) {
                    yerrB = yebIter.getDoubleNext();
                    yerrU = yeuIter.getDoubleNext();
                    eps = new ArrayList<>();
                    eps.add(new PointD(x, y + yerrU));
                    eps.add(new PointD(x, y - yerrB));
                    epls = new CapPolylineShape();
                    epls.setCapLen(capSize);
                    epls.setPoints(eps);
                    gc.add(new Graphic(epls, ecb));
                }
                if (xelIter != null) {
                    xerrL = xelIter.getDoubleNext();
                    xerrR = xerIter.getDoubleNext();
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
        if (cb instanceof PolylineBreak)
            lb.setDrawSymbol(((PolylineBreak)cb).isDrawSymbol());
        else {
            lb.setSymbol((PointBreak) cb);
            lb.setCaption(cb.getCaption());
        }
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator xelIter = xErrorLeft == null ? null : xErrorLeft.getIndexIterator();
        IndexIterator xerIter = xErrorRight == null ? null : xErrorRight.getIndexIterator();
        IndexIterator yebIter = yErrorBottom == null ? null : yErrorBottom.getIndexIterator();
        IndexIterator yeuIter = yErrorUp == null ? null : yErrorUp.getIndexIterator();
        double width;
        if (capSize == null) {
            width = (ArrayMath.getMaximum(xdata) - ArrayMath.getMinimum(xdata)) / xdata.getSize() * 0.1;
        } else {
            width = capSize * 0.5;
        }
        //Loop
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
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
                if (yebIter != null) {
                    yebIter.next();
                    yeuIter.next();
                }
                if (xelIter != null) {
                    xelIter.next();
                    xerIter.next();
                }
            } else {
                points.add(new PointD(x, y));
                if (yErrorBottom != null) {
                    yerrB = yebIter.getDoubleNext();
                    yerrU = yeuIter.getDoubleNext();
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
                    xerrL = xelIter.getDoubleNext();
                    xerrR = xerIter.getDoubleNext();
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
        if (!xdata.getIndexPrivate().isFastIterator()) {
            xdata = xdata.copy();
        }
        if (!ydata.getIndexPrivate().isFastIterator()) {
            ydata = ydata.copy();
        }
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
            IndexIterator xIter = xdata.getIndexIterator();
            IndexIterator yIter = ydata.getIndexIterator();
            while (xIter.hasNext()){
                ps = new PointShape();
                ps.setPoint(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
                graphics.add(new Graphic(ps, cb));
            }
        }
        return graphics;
    }

    /**
     * Create a point graphic
     * @param x X
     * @param y Y
     * @param pb Point legend break
     * @return Point graphic
     */
    public static Graphic createPoint(float x, float y, PointBreak pb) {
        PointShape ps = new PointShape();
        ps.setPoint(new PointD(x, y));
        return new Graphic(ps, pb);
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param pb Point legend break
     * @return Point graphics
     */
    public static GraphicCollection createPoints(Array xdata, Array ydata, PointBreak pb) {
        GraphicCollection graphics = new GraphicCollection();
        PointShape ps;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        while (xIter.hasNext()) {
            ps = new PointShape();
            ps.setPoint(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
            graphics.add(new Graphic(ps, pb));
        }
        return graphics;
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cbs Color breaks
     * @return Point graphics
     */
    public static GraphicCollection createPoints(Array xdata, Array ydata, List<ColorBreak> cbs) {
        GraphicCollection graphics = new GraphicCollection();
        PointShape ps;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        if (cbs.size() == xdata.getSize()) {
            int i = 0;
            while (xIter.hasNext()){
                ps = new PointShape();
                ps.setPoint(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
                graphics.add(new Graphic(ps, cbs.get(i)));
                i++;
            }
            graphics.setSingleLegend(false);
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.UNIQUE_VALUE);
            ls.setShapeType(ShapeTypes.POINT);
            graphics.setLegendScheme(ls);
        } else {
            while (xIter.hasNext()){
                ps = new PointShape();
                ps.setPoint(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
                graphics.add(new Graphic(ps, cbs.get(0)));
            }
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.SINGLE_SYMBOL);
            ls.setShapeType(ShapeTypes.POINT);
            graphics.setLegendScheme(ls);
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        while (xIter.hasNext()) {
            ps = new PointShape();
            ps.setPoint(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
            z = zIter.getDoubleNext();
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        if (cbs.size() == xdata.getSize()) {
            int i = 0;
            while (xIter.hasNext()) {
                ps = new PointZShape();
                if (fixZ) {
                    ps.setPoint(new PointZ(xIter.getDoubleNext(), yIter.getDoubleNext(), z));
                } else {
                    ps.setPoint(new PointZ(xIter.getDoubleNext(), yIter.getDoubleNext(), zIter.getDoubleNext()));
                }
                graphics.add(new Graphic(ps, cbs.get(i)));
                i++;
            }
            graphics.setSingleLegend(false);
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.UNIQUE_VALUE);
            ls.setShapeType(ShapeTypes.POINT);
            graphics.setLegendScheme(ls);
        } else {
            while (xIter.hasNext()) {
                ps = new PointZShape();
                if (fixZ) {
                    ps.setPoint(new PointZ(xIter.getDoubleNext(), yIter.getDoubleNext(), z));
                } else {
                    ps.setPoint(new PointZ(xIter.getDoubleNext(), yIter.getDoubleNext(), zIter.getDoubleNext()));
                }
                graphics.add(new Graphic(ps, cbs.get(0)));
                LegendScheme ls = new LegendScheme();
                ls.setLegendBreaks(cbs);
                ls.setLegendType(LegendType.SINGLE_SYMBOL);
                ls.setShapeType(ShapeTypes.POINT);
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        IndexIterator cIter = cdata.getIndexIterator();
        while (xIter.hasNext()) {
            ps = new PointZShape();
            if (fixZ) {
                ps.setPoint(new PointZ(xIter.getDoubleNext(), yIter.getDoubleNext(), z));
            } else {
                ps.setPoint(new PointZ(xIter.getDoubleNext(), yIter.getDoubleNext(), zIter.getDoubleNext()));
            }
            c = cIter.getDoubleNext();
            cb = ls.findLegendBreak(c);
            graphics.add(new Graphic(ps, cb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);
        return graphics;
    }

    /**
     * Create 3D text graphics
     *
     * @param xa X data array
     * @param ya Y data array
     * @param za Z data array
     * @param sa  data array
     * @param text Text 3d
     * @return 3D point graphics
     */
    public static GraphicCollection createTexts3D(Array xa, Array ya, Array za, Array sa, ChartText3D text) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        IndexIterator xIter = xa.getIndexIterator();
        IndexIterator yIter = ya.getIndexIterator();
        IndexIterator zIter = za.getIndexIterator();
        IndexIterator sIter = sa.getIndexIterator();
        double x, y, z;
        String s;
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            z = zIter.getDoubleNext();
            s = sIter.getStringNext();
            ChartText3D text3D = (ChartText3D) text.clone();
            text3D.setText(s);
            text3D.setPoint(x, y, z);
            graphics.add(new Graphic(text3D, null));
        }

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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        if (cbs.size() == xdata.getSize()) {
            int i = 0;
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
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
                    z = zIter.getDoubleNext();
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
                i++;
            }
            graphics.setSingleLegend(false);
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.UNIQUE_VALUE);
            ls.setShapeType(ShapeTypes.POINT);
            graphics.setLegendScheme(ls);
        } else {
            while (xIter.hasNext()) {
                ps = new PointZShape();
                pzs = new ArrayList<>();
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                if (fixZ) {
                    ps.setPoint(new PointZ(x, y, z0));
                    pzs.add(new PointZ(x, y, bottom));
                    pzs.add(new PointZ(x, y, z0));
                } else {
                    z = zIter.getDoubleNext();
                    ps.setPoint(new PointZ(x, y, z));
                    pzs.add(new PointZ(x, y, bottom));
                    pzs.add(new PointZ(x, y, z));
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
            ls.setLegendType(LegendType.SINGLE_SYMBOL);
            ls.setShapeType(ShapeTypes.POINT);
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        IndexIterator cIter = cdata.getIndexIterator();
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
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
                z = zIter.getDoubleNext();
                if (Double.isNaN(z)) {
                    continue;
                }
                ps.setPoint(new PointZ(x, y, z));
                pzs.add(new PointZ(x, y, bottom));
                pzs.add(new PointZ(x, y, z));
            }
            c = cIter.getDoubleNext();
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
     * Create a polygon
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param pgb PolygonBreak
     * @return Graphic
     */
    public static Graphic createPolygon(Array xa, Array ya, PolygonBreak pgb) {
        double x, y;
        int n = (int) xa.getSize();
        PolygonShape pgs;
        PointD p;
        List<PointD> points = new ArrayList<>();
        IndexIterator xIter = xa.getIndexIterator();
        IndexIterator yIter = ya.getIndexIterator();
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            p = new PointD(x, y);
            points.add(p);
        }
        if (points.size() > 2) {
            pgs = new PolygonShape();
            pgs.setPoints(points);
            Graphic graphic = new Graphic(pgs, pgb);
            return graphic;
        }
        return null;
    }

    /**
     * Create a polygon
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param pgb PolygonBreak
     * @return Graphic
     */
    public static Graphic createPolygon(Array xy, PolygonBreak pgb) {
        double x, y;
        int n = xy.getShape()[0];
        PolygonShape pgs;
        PointD p;
        List<PointD> points = new ArrayList<>();
        IndexIterator iter = xy.getIndexIterator();
        while (iter.hasNext()){
            x = iter.getDoubleNext();
            y = iter.getDoubleNext();
            p = new PointD(x, y);
            points.add(p);
        }
        if (points.size() > 2) {
            pgs = new PolygonShape();
            pgs.setPoints(points);
            Graphic graphic = new Graphic(pgs, pgb);
            return graphic;
        }
        return null;
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
        IndexIterator xIter = xa.getIndexIterator();
        IndexIterator yIter = ya.getIndexIterator();
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
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
        if (!xa.getIndexPrivate().isFastIterator())
            xa = xa.copy();
        if (!ya.getIndexPrivate().isFastIterator())
            ya = ya.copy();
        if (!za.getIndexPrivate().isFastIterator())
            za = za.copy();
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
     * Add wireframe polylines
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param ls Legend scheme
     * @return Graphics
     */
    public static GraphicCollection createWireframe(Array xa, Array ya, Array za, LegendScheme ls) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        int[] shape = xa.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        double z1, z2, z3, z4, z;
        int idx1, idx2, idx3, idx4;
        PolylineBreak pb;
        PolylineZShape ps;
        Graphic graphic;
        List<PointZ> points;
        if (!xa.getIndexPrivate().isFastIterator())
            xa = xa.copy();
        if (!ya.getIndexPrivate().isFastIterator())
            ya = ya.copy();
        if (!za.getIndexPrivate().isFastIterator())
            za = za.copy();
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
                ps = new PolylineZShape();
                points = new ArrayList<>();
                points.add(new PointZ(xa.getDouble(idx1), ya.getDouble(idx1), z1));
                points.add(new PointZ(xa.getDouble(idx3), ya.getDouble(idx3), z3));
                ps.setPoints(points);
                z = (z1 + z3) * 0.5;
                ps.setValue(z);
                pb = (PolylineBreak) ls.findLegendBreak(z);
                graphic = new Graphic(ps, pb);
                graphics.add(graphic);
                ps = new PolylineZShape();
                points = new ArrayList<>();
                points.add(new PointZ(xa.getDouble(idx3), ya.getDouble(idx3), z3));
                points.add(new PointZ(xa.getDouble(idx4), ya.getDouble(idx4), z4));
                ps.setPoints(points);
                z = (z3 + z4) * 0.5;
                ps.setValue(z);
                pb = (PolylineBreak) ls.findLegendBreak(z);
                graphic = new Graphic(ps, pb);
                graphics.add(graphic);
                ps = new PolylineZShape();
                points = new ArrayList<>();
                points.add(new PointZ(xa.getDouble(idx4), ya.getDouble(idx4), z4));
                points.add(new PointZ(xa.getDouble(idx2), ya.getDouble(idx2), z2));
                ps.setPoints(points);
                z = (z4 + z2) * 0.5;
                ps.setValue(z);
                pb = (PolylineBreak) ls.findLegendBreak(z);
                graphic = new Graphic(ps, pb);
                graphics.add(graphic);
                ps = new PolylineZShape();
                points = new ArrayList<>();
                points.add(new PointZ(xa.getDouble(idx2), ya.getDouble(idx2), z2));
                points.add(new PointZ(xa.getDouble(idx1), ya.getDouble(idx1), z1));
                ps.setPoints(points);
                z = (z1 + z2) * 0.5;
                ps.setValue(z);
                pb = (PolylineBreak) ls.findLegendBreak(z);
                graphic = new Graphic(ps, pb);
                graphics.add(graphic);
            }
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

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
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();
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
        graphics.setAllQuads(true);
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);
        return graphics;
    }

    /**
     * Create 3D graphics from a VectorLayer.
     *
     * @param layer The layer
     * @param offset Offset of z axis.
     * @param xshift X shift - to shift the graphics in x direction, normally
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
            case POINT:
                for (PointShape shape : (List<PointShape>) layer.getShapes()) {
                    PointZShape s = new PointZShape();
                    PointD pd = shape.getPoint();
                    pz = new PointZ(pd.X + xshift, pd.Y, offset);
                    s.setPoint(pz);
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    graphics.add(new Graphic(s, cb));
                }
                break;
            case POLYLINE:
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
            case POLYGON:
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
            case POINT_Z:
            case POLYLINE_Z:
            case POLYGON_Z:
                graphics.setFixZ(false);
                switch (shapeType) {
                    case POINT_Z:
                        for (PointZShape shape : (List<PointZShape>) layer.getShapes()) {
                            PointZShape s = new PointZShape();
                            PointZ pd = (PointZ) shape.getPoint();
                            pz = new PointZ(pd.X + xshift, pd.Y, pd.Z + offset, pd.M);
                            s.setPoint(pz);
                            cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                            graphics.add(new Graphic(s, cb));
                        }
                        break;
                    case POLYLINE_Z:
                        for (PolylineZShape shape : (List<PolylineZShape>) layer.getShapes()) {
                            cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                            for (PolylineZ pl : (List<PolylineZ>) shape.getPolylines()) {
                                PolylineZShape s = new PolylineZShape();
                                List<PointZ> plist = new ArrayList<>();
                                for (PointZ pd : (List<PointZ>) pl.getPointList()) {
                                    pz = new PointZ(pd.X + xshift, pd.Y, pd.Z + offset, pd.M);
                                    plist.add(pz);
                                }
                                s.setPoints(plist);
                                graphics.add(new Graphic(s, cb));
                            }
                        }
                        break;
                    case POLYGON_Z:
                        for (PolygonZShape shape : (List<PolygonZShape>) layer.getShapes()) {
                            PolygonZShape s = new PolygonZShape();
                            List<PointZ> plist = new ArrayList<>();
                            for (PointZ pd : (List<PointZ>) shape.getPoints()) {
                                pz = new PointZ(pd.X + xshift, pd.Y, pd.Z + offset, pd.M);
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
        float capeSize = bb.getCapSize();
        double width = widths.getDouble(0);
        if (autoWidth && xdata.getSize() > 1) {
            width = (xdata.getDouble(1) - xdata.getDouble(0)) * width;
        }
        double bot = 0;
        if (drawBottom) {
            bot = bottom.getDouble(0);
        }
        double miny = 0;
        //boolean baseLine = false;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        int i = 0;
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            // Add bar
            if (drawBottom) {
                if (bottom.getSize() > i) {
                    bot = bottom.getDouble(i);
                }
                miny = bot;
                y += miny;
            }
//            if (y < miny) {
//                baseLine = true;
//            }
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
                CapPolylineShape pls = new CapPolylineShape();
                pls.setPoints(pList);
                pls.setCapLen(capeSize);
                graphics.add(new Graphic(pls, ebreak));
                /*//Add cap
                double cape = capeSize == null ? width * 0.25 : capeSize * 0.5;
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.5 - cape, y - e));
                pList.add(new PointD(x + width * 0.5 + cape, y - e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.5 - cape, y + e));
                pList.add(new PointD(x + width * 0.5 + cape, y + e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));*/
            }
            i++;
        }

//        if (baseLine) {
//            List<PointD> pList = new ArrayList<>();
//            double x1 = xdata.getDouble(0);
//            double x2 = xdata.getDouble((int) xdata.getSize() - 1);
//            x1 -= (x2 - x1);
//            x2 += (x2 - x1);
//            pList.add(new PointD(x1, miny));
//            pList.add(new PointD(x2, miny));
//            PolylineShape pls = new PolylineShape();
//            pls.setPoints(pList);
//            ebreak = new PolylineBreak();
//            ebreak.setColor(Color.black);
//            graphics.add(new Graphic(pls, ebreak));
//        }
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
        //boolean baseLine = false;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        int i = 0;
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            // Add bar
            if (drawLeft) {
                if (left.getSize() > i) {
                    bot = left.getDouble(i);
                }
                minx = bot;
                x += minx;
            }
//            if (x < minx) {
//                baseLine = true;
//            }
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
            i++;
        }

//        if (baseLine) {
//            List<PointD> pList = new ArrayList<>();
//            double y1 = ydata.getDouble(0);
//            double y2 = ydata.getDouble((int) ydata.getSize() - 1);
//            y1 -= (y2 - y1);
//            y2 += (y2 - y1);
//            pList.add(new PointD(minx, y1));
//            pList.add(new PointD(minx, y2));
//            PolylineShape pls = new PolylineShape();
//            pls.setPoints(pList);
//            ebreak = new PolylineBreak();
//            ebreak.setColor(Color.black);
//            graphics.add(new Graphic(pls, ebreak));
//        }
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        int i = 0;
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
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
            i++;
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
     * Create 3D bar graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param autoWidth Is auto width or not
     * @param widths Width
     * @param bottom Bottom
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection3D createBars3D(Array xdata, Array ydata, Array zdata, boolean autoWidth,
            Array widths, Array bottom, List<BarBreak> bbs) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        int n = (int) xdata.getSize();
        double x, y, z;
        BarBreak bb = bbs.get(0);
        double width = widths.getDouble(0);
        if (autoWidth && xdata.getSize() > 1) {
            width = (xdata.getDouble(1) - xdata.getDouble(0)) * width;
        }
        double bot = 0;
        double minz = 0;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        int i = 0;
        double hw = width * 0.5;
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            z = zIter.getDoubleNext();
            if (!Double.isNaN(z)) {
                // Add bar
                if (widths.getSize() > 1 && widths.getSize() > i) {
                    width = widths.getDouble(i);
                    hw = width * 0.5;
                }
                List<PointZ> pList = new ArrayList<>();
                pList.add(new PointZ(x + hw, y + hw, minz));
                pList.add(new PointZ(x + hw, y - hw, minz));
                pList.add(new PointZ(x + hw, y + hw, z));
                pList.add(new PointZ(x + hw, y - hw, z));
                pList.add(new PointZ(x - hw, y + hw, minz));
                pList.add(new PointZ(x - hw, y - hw, minz));
                pList.add(new PointZ(x - hw, y + hw, z));
                pList.add(new PointZ(x - hw, y - hw, z));
                CubicShape cs = new CubicShape();
                cs.setPoints(pList);
                if (bbs.size() > i) {
                    bb = bbs.get(i);
                }
                graphics.add(new Graphic(cs, bb));

                i++;
            }
        }

        graphics.setSingleLegend(false);

        return graphics;
    }

    /**
     * Create 3D cylinder bar graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param autoWidth Is auto width or not
     * @param widths Width
     * @param bottom Bottom
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection3D createCylinderBars3D(Array xdata, Array ydata, Array zdata, boolean autoWidth,
                                                 Array widths, Array bottom, List<BarBreak> bbs) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        int n = (int) xdata.getSize();
        double x, y, z;
        BarBreak bb = bbs.get(0);
        double width = widths.getDouble(0);
        if (autoWidth && xdata.getSize() > 1) {
            width = (xdata.getDouble(1) - xdata.getDouble(0)) * width;
        }
        double bot = 0;
        double minz = 0;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        int i = 0;
        double hw = width * 0.5;
        while (xIter.hasNext()){
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            z = zIter.getDoubleNext();
            // Add bar
            if (widths.getSize() > 1 && widths.getSize() > i) {
                width = widths.getDouble(i);
                hw = width * 0.5;
            }
            List<PointZ> pList = new ArrayList<>();
            pList.add(new PointZ(x, y, minz));
            pList.add(new PointZ(x, y, z));
            CylinderShape cs = new CylinderShape(pList, hw);
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(cs, bb));

            i++;
        }

        graphics.setSingleLegend(false);

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
        if (!xdata.getIndexPrivate().isFastIterator())
            xdata = xdata.copy();
        if (!ydata.getIndexPrivate().isFastIterator())
            ydata = ydata.copy();
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
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
            Index xIdx = xdata.getIndex();
            xIdx.setCurrentCounter(0);
            double x1 = xdata.getDouble(xIdx);
            xIdx.setCurrentCounter((int)xdata.getSize() - 1);
            double x2 = xdata.getDouble(xIdx);
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
        return createImage(gdata, extent, false);
    }

    /**
     * Create image
     *
     * @param gdata data array
     * @param extent Extent
     * @param yReverse Y axis reverse or not
     * @return Image graphic
     */
    public static Graphic createImage(Array gdata, List<Number> extent, boolean yReverse) {
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
                        if (yReverse)
                            aImage.setRGB(j, i, color.getRGB());
                        else
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
                        if (yReverse)
                            aImage.setRGB(j, i, color.getRGB());
                        else
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
                        if (yReverse)
                            aImage.setRGB(j, i, color.getRGB());
                        else
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
                        if (yReverse)
                            aImage.setRGB(j, i, color.getRGB());
                        else
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
     * @param extent Extent
     * @return Image graphic
     */
    public static Graphic createImage(List<Array> data, List<Number> extent) {
        return createImage(data, extent, false);
    }

    /**
     * Create image by RGB data array
     *
     * @param data RGB data array list
     * @param extent Extent
     * @param yReverse Y coordinate reverse or not
     * @return Image graphic
     */
    public static Graphic createImage(List<Array> data, List<Number> extent, boolean yReverse) {
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
                        if (yReverse)
                            aImage.setRGB(j, i, color.getRGB());
                        else
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
                        if (yReverse)
                            aImage.setRGB(j, i, color.getRGB());
                        else
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
                        if (yReverse)
                            aImage.setRGB(j, i, color.getRGB());
                        else
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
                        if (yReverse)
                            aImage.setRGB(j, i, color.getRGB());
                        else
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
        ImageShape shape = (ImageShape)gg.getShape();
        Extent extent = shape.getExtent();
        Extent3D ex3 = new Extent3D();
        List<PointZ> coords = new ArrayList<>();
        switch (zdir.toLowerCase()) {
            case "x":
                ex3 = new Extent3D(offset, offset, extent.minX, extent.maxX, extent.minY, extent.maxY);
                coords.add(new PointZ(offset, extent.minX, extent.minY));
                coords.add(new PointZ(offset, extent.maxX, extent.minY));
                coords.add(new PointZ(offset, extent.maxX, extent.maxY));
                coords.add(new PointZ(offset, extent.minX, extent.maxY));
                break;
            case "y":
                ex3 = new Extent3D(extent.minX, extent.maxX, offset, offset, extent.minY, extent.maxY);
                coords.add(new PointZ(extent.minX, offset, extent.minY));
                coords.add(new PointZ(extent.maxX, offset, extent.minY));
                coords.add(new PointZ(extent.maxX, offset, extent.maxY));
                coords.add(new PointZ(extent.minX, offset, extent.maxY));
                break;
            case "z":
                ex3 = new Extent3D(extent.minX, extent.maxX, extent.minY, extent.maxY, offset, offset);
                coords.add(new PointZ(extent.minX, extent.minY, offset));
                coords.add(new PointZ(extent.maxX, extent.minY, offset));
                coords.add(new PointZ(extent.maxX, extent.maxY, offset));
                coords.add(new PointZ(extent.minX, extent.maxY, offset));
                break;
        }
        shape.setExtent(ex3);
        shape.setCoords(coords);
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
     * @return Image
     */
    public static BufferedImage createImage(Array gdata, LegendScheme ls) {
        gdata = gdata.copyIfView();

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
        Color defaultColor = breakColor[breakNum - 1];    //Last color
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        Index index = gdata.getIndex();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index.set(i, j);
                oneValue = gdata.getDouble(index);
                if (Double.isNaN(oneValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    if (ls.getLegendType() == LegendType.GRADUATED_COLOR) {
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
                //aImage.setRGB(j, height - i - 1, oneColor.getRGB());
                aImage.setRGB(j, i, oneColor.getRGB());
            }
        }

        return aImage;
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
        Color defaultColor = breakColor[breakNum - 1];    //Last color
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        Index index = gdata.getIndex();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index.set(i, j);
                oneValue = gdata.getDouble(index);
                if (Double.isNaN(oneValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    if (ls.getLegendType() == LegendType.GRADUATED_COLOR) {
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
        Color defaultColor = breakColor[breakNum - 1];    //Last color
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                oneValue = gdata.getDoubleValue(i, j);
                if (Double.isNaN(oneValue) || MIMath.doubleEquals(oneValue, gdata.missingValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    if (ls.getLegendType() == LegendType.GRADUATED_COLOR) {
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
        return new ImageGraphic(ishape, ls);
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
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        if (ls.getColorMap() == null) {
            breakNum = ls.getValidBreakNum();
            double[] breakValue = new double[breakNum];
            Color[] breakColor = new Color[breakNum];
            Color undefColor = Color.white;
            int idx = 0;
            for (ColorBreak cb : ls.getLegendBreaks()) {
                if (cb.isNoData()) {
                    undefColor = cb.getColor();
                } else {
                    breakValue[idx] = Double.parseDouble(cb.getEndValue().toString());
                    breakColor[idx] = cb.getColor();
                    idx += 1;
                }
            }
            Color defaultColor = breakColor[breakNum - 1];    //Last color
            double oneValue;
            Color oneColor;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    oneValue = gdata.getDoubleValue(i, j);
                    if (Double.isNaN(oneValue) || MIMath.doubleEquals(oneValue, gdata.missingValue)) {
                        oneColor = undefColor;
                    } else {
                        oneColor = defaultColor;
                        if (ls.getLegendType() == LegendType.GRADUATED_COLOR) {
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
        } else {
            ColorMap colorMap = ls.getColorMap();
            int n = colorMap.getColorCount();
            Normalize normalize = ls.getNormalize();
            double v;
            Color fillColor = colorMap.getFillColor();
            Color color;
            int idx;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    v = gdata.getDoubleValue(i, j);
                    if (Double.isNaN(v) || MIMath.doubleEquals(v, gdata.missingValue)) {
                        color = fillColor;
                    } else {
                        idx = (int) (normalize.apply(v).floatValue() * n);
                        if (idx < 0)
                            idx = 0;
                        else if (idx >= n)
                            idx = n - 1;
                        color = colorMap.getColor(idx);
                    }
                    aImage.setRGB(j, height - i - 1, color.getRGB());
                }
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
        return new ImageGraphic(ishape, ls);
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
        ImageShape shape = (ImageShape) gg.getShape();
        Extent extent = shape.getExtent();
        Extent3D ex3 = new Extent3D();
        List<PointZ> coords = new ArrayList<>();
        switch (zdir.toLowerCase()) {
            case "x":
                ex3 = new Extent3D(offset, offset, extent.minX, extent.maxX, extent.minY, extent.maxY);
                coords.add(new PointZ(offset, extent.minX, extent.minY));
                coords.add(new PointZ(offset, extent.maxX, extent.minY));
                coords.add(new PointZ(offset, extent.maxX, extent.maxY));
                coords.add(new PointZ(offset, extent.minX, extent.maxY));
                break;
            case "y":
                ex3 = new Extent3D(extent.minX, extent.maxX, offset, offset, extent.minY, extent.maxY);
                coords.add(new PointZ(extent.minX, offset, extent.minY));
                coords.add(new PointZ(extent.maxX, offset, extent.minY));
                coords.add(new PointZ(extent.maxX, offset, extent.maxY));
                coords.add(new PointZ(extent.minX, offset, extent.maxY));
                break;
            case "xy":
                ex3 = new Extent3D(sePoint.get(0).doubleValue(), sePoint.get(2).doubleValue(),
                        sePoint.get(1).doubleValue(), sePoint.get(3).doubleValue(), extent.minY, extent.maxY);
                coords.add(new PointZ(sePoint.get(0).doubleValue(), sePoint.get(1).doubleValue(), extent.minY));
                coords.add(new PointZ(sePoint.get(2).doubleValue(), sePoint.get(1).doubleValue(), extent.minY));
                coords.add(new PointZ(sePoint.get(2).doubleValue(), sePoint.get(3).doubleValue(), extent.maxY));
                coords.add(new PointZ(sePoint.get(0).doubleValue(), sePoint.get(3).doubleValue(), extent.maxY));
                break;
            case "z":
                ex3 = new Extent3D(extent.minX, extent.maxX, extent.minY, extent.maxY, offset, offset);
                coords.add(new PointZ(extent.minX, extent.minY, offset));
                coords.add(new PointZ(extent.maxX, extent.minY, offset));
                coords.add(new PointZ(extent.maxX, extent.maxY, offset));
                coords.add(new PointZ(extent.minX, extent.maxY, offset));
                break;
        }
        shape.setExtent(ex3);
        shape.setCoords(coords);
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
        List<PointZ> coords = new ArrayList<>();
        coords.add(new PointZ(extent.minX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.maxY, offset));
        coords.add(new PointZ(extent.minX + xshift, extent.maxY, offset));
        ishape.setExtent(ex3);
        ishape.setCoords(coords);
        Graphic gg = new Graphic(ishape, new ColorBreak());
        if (interpolation != null) {
            ((ImageShape) gg.getShape()).setInterpolation(interpolation);
        }
        graphics.add(gg);

        return graphics;
    }

    /**
     * Create Texture
     *
     * @param gl            GL2
     * @param layer         Image layer
     * @param offset        Offset of z axis
     * @param xshift        X shift - to shift the grahpics in x direction, normally
     *                      for map in 180 - 360 degree east
     * @param interpolation Interpolation
     * @return Graphics
     * @throws IOException
     */
    public static GraphicCollection createTexture(ImageLayer layer, double offset, double xshift,
                                                  String interpolation) throws IOException {
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZDir("z");
        graphics.setZValue(offset);
        TextureShape ishape = new TextureShape();
        ishape.setFileName(layer.getFileName());
        ishape.setImage(layer.getImage());
        Extent extent = layer.getExtent();
        Extent3D ex3 = new Extent3D(extent.minX + xshift, extent.maxX + xshift, extent.minY, extent.maxY, offset, offset);
        List<PointZ> coords = new ArrayList<>();
        coords.add(new PointZ(extent.minX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.maxY, offset));
        coords.add(new PointZ(extent.minX + xshift, extent.maxY, offset));
        ishape.setExtent(ex3);
        ishape.setCoords(coords);
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
     * @param xa X coordinate array - one dimension
     * @param ya Y coordinate array - one dimension
     * @param va Data array - two dimension
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour lines
     */
    public static GraphicCollection createContourLines(Array xa, Array ya, Array va, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.POLYLINE);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[] shape = va.getShape();
        int[][] S1 = new int[shape[0]][shape[1]];
        double[] x = (double[])ArrayUtil.copyToNDJavaArray_Double(xa);
        double[] y = (double[])ArrayUtil.copyToNDJavaArray_Double(ya);
        if (x[1] - x[0] < 0) {
            ArrayUtils.reverse(x);
            va = va.flip(1);
        }
        if (y[1] - y[0] < 0) {
            ArrayUtils.reverse(y);
            va = va.flip(0);
        }
        double missingValue = -9999.0;
        double[][] data = (double[][]) ArrayUtil.copyToNDJavaArray_Double(va, missingValue);
        Object[] cbs = ContourDraw.tracingContourLines(data,
                cValues, x, y, missingValue, S1);
        List<PolyLine> ContourLines = (List<PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = Contour.smoothLines(ContourLines);
        }

        PolyLine aLine;
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
            aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));

            switch (ls.getLegendType()) {
                case UNIQUE_VALUE:
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
                case GRADUATED_COLOR:
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
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour lines
     */
    public static GraphicCollection createContourLines(GridData gridData, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.POLYLINE);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[gridData.getYNum()][gridData.getXNum()];
        double[] x = gridData.getXArray();
        double[] y = gridData.getYArray();
        if (gridData.getXDelta() < 0) {
            ArrayUtils.reverse(x);
            gridData.xReverse();
        }
        if (gridData.getYDelta() < 0) {
            ArrayUtils.reverse(y);
            gridData.yReverse();
        }
        Object[] cbs = ContourDraw.tracingContourLines(gridData.getData(),
                cValues, x, y, gridData.getDoubleMissingValue(), S1);
        List<PolyLine> ContourLines = (List<PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = Contour.smoothLines(ContourLines);
        }

        PolyLine aLine;
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
            aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));

            switch (ls.getLegendType()) {
                case UNIQUE_VALUE:
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
                case GRADUATED_COLOR:
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
        ls = ls.convertTo(ShapeTypes.POLYLINE);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[gridData.getYNum()][gridData.getXNum()];
        double[] x = gridData.getXArray();
        double[] y = gridData.getYArray();
        if (gridData.getXDelta() < 0) {
            ArrayUtils.reverse(x);
            gridData.xReverse();
        }
        if (gridData.getYDelta() < 0) {
            ArrayUtils.reverse(y);
            gridData.yReverse();
        }
        Object[] cbs = ContourDraw.tracingContourLines(gridData.getData(),
                cValues, x, y, gridData.getDoubleMissingValue(), S1);
        List<PolyLine> ContourLines = (List<PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = Contour.smoothLines(ContourLines);
        }

        PolyLine aLine;
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
            aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));
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
        ls = ls.convertTo(ShapeTypes.POLYLINE);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[gridData.getYNum()][gridData.getXNum()];
        double[] xArray = gridData.getXArray();
        double[] yArray = gridData.getYArray();
        if (gridData.getXDelta() < 0) {
            ArrayUtils.reverse(xArray);
            gridData.xReverse();
        }
        if (gridData.getYDelta() < 0) {
            ArrayUtils.reverse(yArray);
            gridData.yReverse();
        }
        Object[] cbs = ContourDraw.tracingContourLines(gridData.getData(),
                cValues, xArray, yArray, gridData.getDoubleMissingValue(), S1);
        List<PolyLine> ContourLines = (List<PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = Contour.smoothLines(ContourLines);
        }

        PolyLine aLine;
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
            aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));
            cbb = ls.findLegendBreak(v);
            graphics.add(new Graphic(aPolyline, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create contour slice graphics
     *
     * @param data   Data array - 3D
     * @param xa     X coordinate array - 1D
     * @param ya     Y coordinate array - 1D
     * @param za     Z coordinate array - 1D
     * @param xSlice X slice list
     * @param ySlice Y slice list
     * @param zSlice Z slice list
     * @param ls     Legend scheme
     * @param isSmooth Smooth contour lines or not
     * @return Contour slice graphics
     */
    public static List<GraphicCollection3D> contourSlice(Array data, Array xa, Array ya, Array za, List<Number> xSlice,
                                              List<Number> ySlice, List<Number> zSlice, LegendScheme ls,
                                                         boolean isSmooth) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        List<GraphicCollection3D> sgs = new ArrayList<>();

        int dim1, dim2;
        double x, y, z;

        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];
        double[] xx = (double[])ArrayUtil.copyToNDJavaArray_Double(xa);
        double[] yy = (double[])ArrayUtil.copyToNDJavaArray_Double(ya);
        double[] zz = (double[])ArrayUtil.copyToNDJavaArray_Double(za);

        //X slices
        dim1 = (int) za.getSize();
        dim2 = (int) ya.getSize();
        for (int s = 0; s < xSlice.size(); s++) {
            x = xSlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 2, xa, x);
            if (r != null) {
                double[][] grid = (double[][]) ArrayUtil.copyToNDJavaArray_Double(r);
                int[][] S1 = new int[dim1][dim2];
                Object[] cbs = ContourDraw.tracingContourLines(grid,
                        cValues, yy, zz, Double.NaN, S1);
                List<PolyLine> contourLines = (List<PolyLine>) cbs[0];

                if (contourLines.isEmpty()) {
                    continue;
                }

                if (isSmooth) {
                    contourLines = Contour.smoothLines(contourLines);
                }

                GraphicCollection3D graphics = new GraphicCollection3D();
                ColorBreak cb;
                for (PolyLine line : contourLines) {
                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (wcontour.global.PointD p : line.PointList) {
                        points.add(new PointZ(x, p.X, p.Y));
                    }
                    shape.setPoints(points);
                    shape.setValue(line.Value);
                    //shape.setExtent(GeometryUtil.getPointsExtent(points));
                    cb = ls.findLegendBreak(line.Value);
                    graphics.add(new Graphic(shape, cb));
                }
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        //Y slices
        dim1 = (int) za.getSize();
        dim2 = (int) xa.getSize();
        for (int s = 0; s < ySlice.size(); s++) {
            y = ySlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 1, ya, y);
            if (r != null) {
                double[][] grid = (double[][]) ArrayUtil.copyToNDJavaArray_Double(r);
                int[][] S1 = new int[dim1][dim2];
                Object[] cbs = ContourDraw.tracingContourLines(grid,
                        cValues, xx, zz, Double.NaN, S1);
                List<PolyLine> contourLines = (List<PolyLine>) cbs[0];

                if (contourLines.isEmpty()) {
                    continue;
                }

                if (isSmooth) {
                    contourLines = Contour.smoothLines(contourLines);
                }

                GraphicCollection3D graphics = new GraphicCollection3D();
                ColorBreak cb;
                for (PolyLine line : contourLines) {
                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (wcontour.global.PointD p : line.PointList) {
                        points.add(new PointZ(p.X, y, p.Y));
                    }
                    shape.setPoints(points);
                    shape.setValue(line.Value);
                    //shape.setExtent(GeometryUtil.getPointsExtent(points));
                    cb = ls.findLegendBreak(line.Value);
                    graphics.add(new Graphic(shape, cb));
                }
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        //Z slices
        dim1 = (int) ya.getSize();
        dim2 = (int) xa.getSize();
        for (int s = 0; s < zSlice.size(); s++) {
            z = zSlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 0, za, z);
            if (r != null) {
                double[][] grid = (double[][]) ArrayUtil.copyToNDJavaArray_Double(r);
                int[][] S1 = new int[dim1][dim2];
                Object[] cbs = ContourDraw.tracingContourLines(grid,
                        cValues, xx, yy, Double.NaN, S1);
                List<PolyLine> contourLines = (List<PolyLine>) cbs[0];

                if (contourLines.isEmpty()) {
                    continue;
                }

                if (isSmooth) {
                    contourLines = Contour.smoothLines(contourLines);
                }

                GraphicCollection3D graphics = new GraphicCollection3D();
                ColorBreak cb;
                for (PolyLine line : contourLines) {
                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (wcontour.global.PointD p : line.PointList) {
                        points.add(new PointZ(p.X, p.Y, z));
                    }
                    shape.setPoints(points);
                    shape.setValue(line.Value);
                    //shape.setExtent(GeometryUtil.getPointsExtent(points));
                    cb = ls.findLegendBreak(line.Value);
                    graphics.add(new Graphic(shape, cb));
                }
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        return sgs;
    }

    /**
     * Create contour slice graphics
     *
     * @param data   Data array - 3D
     * @param xa     X coordinate array - 1D
     * @param ya     Y coordinate array - 1D
     * @param za     Z coordinate array - 1D
     * @param xySlice XY slice list
     * @param method Interpolation method
     * @param ls     Legend scheme
     * @param isSmooth Smooth contour lines or not
     * @return Contour slice graphics
     */
    public static GraphicCollection3D contourSlice(Array data, Array xa, Array ya, Array za, List<Number> xySlice,
                                                    InterpolationMethod method, LegendScheme ls, boolean isSmooth) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        int dim1, dim2;
        double x, y, z;

        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];
        double[] zz = (double[])ArrayUtil.copyToNDJavaArray_Double(za);
        double missingValue = -9999.0;

        //XY slices
        Array[] rxy = InterpUtil.sliceXY(xa, ya, za, data, xySlice, method);
        Array r = rxy[0];
        if (r != null) {
            Array xs = rxy[1];
            double[] xx = (double[])ArrayUtil.copyToNDJavaArray_Double(xs);
            int[] rShape = r.getShape();
            dim1 = rShape[0];
            dim2 = rShape[1];
            double x1 = xySlice.get(0).doubleValue();
            double y1 = xySlice.get(1).doubleValue();
            double x2 = xySlice.get(2).doubleValue();
            double y2 = xySlice.get(3).doubleValue();

            double minData = ArrayMath.getMinimum(r);
            double maxData = ArrayMath.getMaximum(r);
            double[][] grid = (double[][]) ArrayUtil.copyToNDJavaArray_Double(r, missingValue);
            int[][] S1 = new int[dim1][dim2];
            Object[] cbs = ContourDraw.tracingContourLines(grid,
                    cValues, xx, zz, missingValue, S1);
            List<PolyLine> contourLines = (List<PolyLine>) cbs[0];

            if (!contourLines.isEmpty()) {
                if (isSmooth) {
                    contourLines = Contour.smoothLines(contourLines);
                }

                GraphicCollection3D graphics = new GraphicCollection3D();
                ColorBreak cb;
                PointZ pz;
                for (PolyLine line : contourLines) {
                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (wcontour.global.PointD p : line.PointList) {
                        x = p.X;
                        y = p.Y;
                        pz = new PointZ();
                        pz.X = x;
                        pz.Y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
                        pz.Z = y;
                        points.add(pz);
                    }
                    shape.setPoints(points);
                    shape.setValue(line.Value);
                    cb = ls.findLegendBreak(line.Value);
                    graphics.add(new Graphic(shape, cb));
                }
                graphics.setLegendScheme(ls);
                return graphics;
            }
        }

        return null;
    }

    /**
     * Create contour polygon slice graphics
     *
     * @param data   Data array - 3D
     * @param xa     X coordinate array - 1D
     * @param ya     Y coordinate array - 1D
     * @param za     Z coordinate array - 1D
     * @param xSlice X slice list
     * @param ySlice Y slice list
     * @param zSlice Z slice list
     * @param ls     Legend scheme
     * @param isSmooth Smooth contour lines or not
     * @return Contour polygon slice graphics
     */
    public static List<GraphicCollection3D> contourfSlice(Array data, Array xa, Array ya, Array za, List<Number> xSlice,
                                                         List<Number> ySlice, List<Number> zSlice, LegendScheme ls,
                                                         boolean isSmooth) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        List<GraphicCollection3D> sgs = new ArrayList<>();

        int dim1, dim2;
        double x, y, z;

        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];
        double[] xx = (double[])ArrayUtil.copyToNDJavaArray_Double(xa);
        double[] yy = (double[])ArrayUtil.copyToNDJavaArray_Double(ya);
        double[] zz = (double[])ArrayUtil.copyToNDJavaArray_Double(za);
        double missingValue = -9999.0;

        //X slices
        dim1 = (int) za.getSize();
        dim2 = (int) ya.getSize();
        for (int s = 0; s < xSlice.size(); s++) {
            x = xSlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 2, xa, x);
            if (r != null) {
                double minData = ArrayMath.getMinimum(r);
                double maxData = ArrayMath.getMaximum(r);
                double[][] grid = (double[][]) ArrayUtil.copyToNDJavaArray_Double(r, missingValue);
                int[][] S1 = new int[dim1][dim2];
                Object[] cbs = ContourDraw.tracingContourLines(grid,
                        cValues, yy, zz, missingValue, S1);
                List<PolyLine> contourLines = (List<PolyLine>) cbs[0];

                if (contourLines.isEmpty()) {
                    continue;
                }

                List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

                if (isSmooth) {
                    contourLines = Contour.smoothLines(contourLines);
                }
                List<wcontour.global.Polygon> contourPolygons = ContourDraw.tracingPolygons(grid, contourLines, borders, cValues);

                GraphicCollection3D graphics = new GraphicCollection3D();
                ColorBreak cb;
                for (wcontour.global.Polygon polygon : contourPolygons) {
                    PolygonZShape shape = new PolygonZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (wcontour.global.PointD p : polygon.OutLine.PointList) {
                        points.add(new PointZ(x, p.X, p.Y));
                    }
                    if (!GeoComputation.isClockwise(points)) {
                        Collections.reverse(points);
                    }
                    shape.setPoints(points);
                    shape.lowValue = polygon.LowValue;
                    shape.highValue = polygon.HighValue;
                    //shape.setExtent(GeometryUtil.getPointsExtent(points));
                    if (polygon.HasHoles()) {
                        for (PolyLine holeLine : polygon.HoleLines) {
                            points = new ArrayList<>();
                            for (wcontour.global.PointD p : holeLine.PointList) {
                                points.add(new PointZ(x, p.X, p.Y));
                            }
                            shape.addHole(points, 0);
                        }
                    }
                    int valueIdx = Arrays.binarySearch(cValues, polygon.LowValue);
                    if (valueIdx < 0) {
                        valueIdx = -valueIdx;
                    }
                    //int valueIdx = findIndex(cValues, v);
                    if (valueIdx == cValues.length - 1) {
                        shape.highValue = maxData;
                    } else {
                        shape.highValue = cValues[valueIdx + 1];
                    }
                    if (!polygon.IsHighCenter && polygon.HighValue == polygon.LowValue) {
                        shape.highValue = polygon.LowValue;
                        if (valueIdx == 0) {
                            shape.lowValue = minData;
                        } else {
                            shape.lowValue = cValues[valueIdx - 1];
                        }
                    }
                    cb = ls.findLegendBreak(shape.lowValue);
                    graphics.add(new Graphic(shape, cb));
                }
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        //Y slices
        dim1 = (int) za.getSize();
        dim2 = (int) xa.getSize();
        for (int s = 0; s < ySlice.size(); s++) {
            y = ySlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 1, ya, y);
            if (r != null) {
                double minData = ArrayMath.getMinimum(r);
                double maxData = ArrayMath.getMaximum(r);
                double[][] grid = (double[][]) ArrayUtil.copyToNDJavaArray_Double(r, missingValue);
                int[][] S1 = new int[dim1][dim2];
                Object[] cbs = ContourDraw.tracingContourLines(grid,
                        cValues, xx, zz, missingValue, S1);
                List<PolyLine> contourLines = (List<PolyLine>) cbs[0];

                if (contourLines.isEmpty()) {
                    continue;
                }

                List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

                if (isSmooth) {
                    contourLines = Contour.smoothLines(contourLines);
                }
                List<wcontour.global.Polygon> contourPolygons = ContourDraw.tracingPolygons(grid, contourLines, borders, cValues);

                GraphicCollection3D graphics = new GraphicCollection3D();
                ColorBreak cb;
                for (wcontour.global.Polygon polygon : contourPolygons) {
                    PolygonZShape shape = new PolygonZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (wcontour.global.PointD p : polygon.OutLine.PointList) {
                        points.add(new PointZ(p.X, y, p.Y));
                    }
                    if (!GeoComputation.isClockwise(points)) {
                        Collections.reverse(points);
                    }
                    shape.setPoints(points);
                    shape.lowValue = polygon.LowValue;
                    shape.highValue = polygon.HighValue;
                    //shape.setExtent(GeometryUtil.getPointsExtent(points));
                    if (polygon.HasHoles()) {
                        for (PolyLine holeLine : polygon.HoleLines) {
                            points = new ArrayList<>();
                            for (wcontour.global.PointD p : holeLine.PointList) {
                                points.add(new PointZ(p.X, y, p.Y));
                            }
                            shape.addHole(points, 0);
                        }
                    }
                    int valueIdx = Arrays.binarySearch(cValues, polygon.LowValue);
                    if (valueIdx < 0) {
                        valueIdx = -valueIdx;
                    }
                    //int valueIdx = findIndex(cValues, v);
                    if (valueIdx == cValues.length - 1) {
                        shape.highValue = maxData;
                    } else {
                        shape.highValue = cValues[valueIdx + 1];
                    }
                    if (!polygon.IsHighCenter && polygon.HighValue == polygon.LowValue) {
                        shape.highValue = polygon.LowValue;
                        if (valueIdx == 0) {
                            shape.lowValue = minData;
                        } else {
                            shape.lowValue = cValues[valueIdx - 1];
                        }
                    }
                    cb = ls.findLegendBreak(shape.lowValue);
                    graphics.add(new Graphic(shape, cb));
                }
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        //Z slices
        dim1 = (int) ya.getSize();
        dim2 = (int) xa.getSize();
        for (int s = 0; s < zSlice.size(); s++) {
            z = zSlice.get(s).doubleValue();
            Array r = ArrayUtil.slice(data, 0, za, z);
            if (r != null) {
                double minData = ArrayMath.getMinimum(r);
                double maxData = ArrayMath.getMaximum(r);
                double[][] grid = (double[][]) ArrayUtil.copyToNDJavaArray_Double(r, missingValue);
                int[][] S1 = new int[dim1][dim2];
                Object[] cbs = ContourDraw.tracingContourLines(grid,
                        cValues, xx, yy, missingValue, S1);
                List<PolyLine> contourLines = (List<PolyLine>) cbs[0];

                if (contourLines.isEmpty()) {
                    continue;
                }

                List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

                if (isSmooth) {
                    contourLines = Contour.smoothLines(contourLines);
                }
                List<wcontour.global.Polygon> contourPolygons = ContourDraw.tracingPolygons(grid, contourLines, borders, cValues);

                GraphicCollection3D graphics = new GraphicCollection3D();
                ColorBreak cb;
                for (wcontour.global.Polygon polygon : contourPolygons) {
                    PolygonZShape shape = new PolygonZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (wcontour.global.PointD p : polygon.OutLine.PointList) {
                        points.add(new PointZ(p.X, p.Y, z));
                    }
                    if (!GeoComputation.isClockwise(points)) {
                        Collections.reverse(points);
                    }
                    shape.setPoints(points);
                    shape.lowValue = polygon.LowValue;
                    shape.highValue = polygon.HighValue;
                    //shape.setExtent(GeometryUtil.getPointsExtent(points));
                    if (polygon.HasHoles()) {
                        for (PolyLine holeLine : polygon.HoleLines) {
                            points = new ArrayList<>();
                            for (wcontour.global.PointD p : holeLine.PointList) {
                                points.add(new PointZ(p.X, p.Y, z));
                            }
                            shape.addHole(points, 0);
                        }
                    }
                    int valueIdx = Arrays.binarySearch(cValues, polygon.LowValue);
                    if (valueIdx < 0) {
                        valueIdx = -valueIdx;
                    }
                    //int valueIdx = findIndex(cValues, v);
                    if (valueIdx == cValues.length - 1) {
                        shape.highValue = maxData;
                    } else {
                        shape.highValue = cValues[valueIdx + 1];
                    }
                    if (!polygon.IsHighCenter && polygon.HighValue == polygon.LowValue) {
                        shape.highValue = polygon.LowValue;
                        if (valueIdx == 0) {
                            shape.lowValue = minData;
                        } else {
                            shape.lowValue = cValues[valueIdx - 1];
                        }
                    }
                    cb = ls.findLegendBreak(shape.lowValue);
                    graphics.add(new Graphic(shape, cb));
                }
                graphics.setLegendScheme(ls);
                sgs.add(graphics);
            }
        }

        return sgs;
    }

    /**
     * Create contour polygon slice graphics
     *
     * @param data   Data array - 3D
     * @param xa     X coordinate array - 1D
     * @param ya     Y coordinate array - 1D
     * @param za     Z coordinate array - 1D
     * @param xySlice XY slice list
     * @param method Interpolation method
     * @param ls     Legend scheme
     * @param isSmooth Smooth contour lines or not
     * @return Contour polygon slice graphics
     */
    public static GraphicCollection3D contourfSlice(Array data, Array xa, Array ya, Array za, List<Number> xySlice,
                                                          InterpolationMethod method, LegendScheme ls, boolean isSmooth) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        int dim1, dim2;
        double x, y, z;

        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];
        double[] zz = (double[])ArrayUtil.copyToNDJavaArray_Double(za);
        double missingValue = -9999.0;

        //XY slices
        Array[] rxy = InterpUtil.sliceXY(xa, ya, za, data, xySlice, method);
        Array r = rxy[0];
        if (r != null) {
            Array xs = rxy[1];
            double[] xx = (double[])ArrayUtil.copyToNDJavaArray_Double(xs);
            int[] rShape = r.getShape();
            dim1 = rShape[0];
            dim2 = rShape[1];
            double x1 = xySlice.get(0).doubleValue();
            double y1 = xySlice.get(1).doubleValue();
            double x2 = xySlice.get(2).doubleValue();
            double y2 = xySlice.get(3).doubleValue();

            double minData = ArrayMath.getMinimum(r);
            double maxData = ArrayMath.getMaximum(r);
            double[][] grid = (double[][]) ArrayUtil.copyToNDJavaArray_Double(r, missingValue);
            int[][] S1 = new int[dim1][dim2];
            Object[] cbs = ContourDraw.tracingContourLines(grid,
                    cValues, xx, zz, missingValue, S1);
            List<PolyLine> contourLines = (List<PolyLine>) cbs[0];

            if (!contourLines.isEmpty()) {
                List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

                if (isSmooth) {
                    contourLines = Contour.smoothLines(contourLines);
                }
                List<wcontour.global.Polygon> contourPolygons = ContourDraw.tracingPolygons(grid, contourLines, borders, cValues);

                GraphicCollection3D graphics = new GraphicCollection3D();
                ColorBreak cb;
                PointZ pz;
                for (wcontour.global.Polygon polygon : contourPolygons) {
                    PolygonZShape shape = new PolygonZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (wcontour.global.PointD p : polygon.OutLine.PointList) {
                        x = p.X;
                        y = p.Y;
                        pz = new PointZ();
                        pz.X = x;
                        pz.Y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
                        pz.Z = y;
                        points.add(pz);
                    }
                    if (!GeoComputation.isClockwise(points)) {
                        Collections.reverse(points);
                    }
                    shape.setPoints(points);
                    shape.lowValue = polygon.LowValue;
                    shape.highValue = polygon.HighValue;
                    //shape.setExtent(GeometryUtil.getPointsExtent(points));
                    if (polygon.HasHoles()) {
                        for (PolyLine holeLine : polygon.HoleLines) {
                            points = new ArrayList<>();
                            for (wcontour.global.PointD p : holeLine.PointList) {
                                x = p.X;
                                y = p.Y;
                                pz = new PointZ();
                                pz.X = x;
                                pz.Y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
                                pz.Z = y;
                                points.add(pz);
                            }
                            shape.addHole(points, 0);
                        }
                    }
                    int valueIdx = Arrays.binarySearch(cValues, polygon.LowValue);
                    if (valueIdx < 0) {
                        valueIdx = -valueIdx;
                    }
                    //int valueIdx = findIndex(cValues, v);
                    if (valueIdx == cValues.length - 1) {
                        shape.highValue = maxData;
                    } else {
                        shape.highValue = cValues[valueIdx + 1];
                    }
                    if (!polygon.IsHighCenter && polygon.HighValue == polygon.LowValue) {
                        shape.highValue = polygon.LowValue;
                        if (valueIdx == 0) {
                            shape.lowValue = minData;
                        } else {
                            shape.lowValue = cValues[valueIdx - 1];
                        }
                    }
                    cb = ls.findLegendBreak(shape.lowValue);
                    graphics.add(new Graphic(shape, cb));
                }
                graphics.setLegendScheme(ls);
                return graphics;
            }
        }

        return null;
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
        ls = ls.convertTo(ShapeTypes.POLYGON);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        double minData;
        double maxData;
        double[] maxmin = new double[2];
        gridData.getMaxMinValue(maxmin);
        maxData = maxmin[0];
        minData = maxmin[1];

        int[][] S1 = new int[gridData.getYNum()][gridData.getXNum()];
        double[] xArray = gridData.getXArray();
        double[] yArray = gridData.getYArray();
        if (gridData.getXDelta() < 0) {
            ArrayUtils.reverse(xArray);
            gridData.xReverse();
        }
        if (gridData.getYDelta() < 0) {
            ArrayUtils.reverse(yArray);
            gridData.yReverse();
        }
        Object[] cbs = ContourDraw.tracingContourLines(gridData.getData(),
                cValues, xArray, yArray, gridData.getDoubleMissingValue(), S1);
        List<PolyLine> contourLines = (List<PolyLine>) cbs[0];
        List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

        if (isSmooth) {
            contourLines = Contour.smoothLines(contourLines);
        }
        List<wcontour.global.Polygon> contourPolygons = ContourDraw.tracingPolygons(gridData.getData(), contourLines, borders, cValues);

        double v;
        ColorBreak cbb = ls.findLegendBreak(0);
        GraphicCollection graphics = new GraphicCollection();
        for (int i = 0; i < contourPolygons.size(); i++) {
            wcontour.global.Polygon poly = contourPolygons.get(i);
            v = poly.LowValue;
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
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
            aPolygonShape.setExtent(GeometryUtil.getPointsExtent(pList));
            aPolygonShape.lowValue = v;
            if (poly.HasHoles()) {
                for (PolyLine holeLine : poly.HoleLines) {
                    pList = new ArrayList<>();
                    for (wcontour.global.PointD pointList : holeLine.PointList) {
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
                case UNIQUE_VALUE:
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
                case GRADUATED_COLOR:
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
     * Create contour polygons
     *
     * @param xa X coordinate array - one dimension
     * @param ya Y coordinate array - one dimension
     * @param va Data array - two dimension
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour polygons
     */
    public static GraphicCollection createContourPolygons(Array xa, Array ya, Array va, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.POLYGON);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        double minData = ArrayMath.min(va).doubleValue();
        double maxData = ArrayMath.max(va).doubleValue();

        int[] shape = va.getShape();
        int[][] S1 = new int[shape[0]][shape[1]];
        double[] x = (double[])ArrayUtil.copyToNDJavaArray_Double(xa);
        double[] y = (double[])ArrayUtil.copyToNDJavaArray_Double(ya);
        if (x[1] - x[0] < 0) {
            ArrayUtils.reverse(x);
            va = va.flip(1);
        }
        if (y[1] - y[0] < 0) {
            ArrayUtils.reverse(y);
            va = va.flip(0);
        }
        double missingValue = -9999.0;
        double[][] data = (double[][]) ArrayUtil.copyToNDJavaArray_Double(va, missingValue);
        Object[] cbs = ContourDraw.tracingContourLines(data,
                cValues, x, y, missingValue, S1);
        List<PolyLine> contourLines = (List<PolyLine>) cbs[0];
        List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

        if (isSmooth) {
            contourLines = Contour.smoothLines(contourLines);
        }
        List<wcontour.global.Polygon> contourPolygons = ContourDraw.tracingPolygons(data, contourLines, borders, cValues);

        double v;
        ColorBreak cbb = ls.findLegendBreak(0);
        GraphicCollection graphics = new GraphicCollection();
        for (int i = 0; i < contourPolygons.size(); i++) {
            wcontour.global.Polygon poly = contourPolygons.get(i);
            v = poly.LowValue;
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
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
            aPolygonShape.setExtent(GeometryUtil.getPointsExtent(pList));
            aPolygonShape.lowValue = v;
            if (poly.HasHoles()) {
                for (PolyLine holeLine : poly.HoleLines) {
                    pList = new ArrayList<>();
                    for (wcontour.global.PointD pointList : holeLine.PointList) {
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
            switch (ls.getLegendType()) {
                case UNIQUE_VALUE:
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
                case GRADUATED_COLOR:
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
        ls = ls.convertTo(ShapeTypes.POLYGON);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        double minData;
        double maxData;
        double[] maxmin = new double[2];
        gridData.getMaxMinValue(maxmin);
        maxData = maxmin[0];
        minData = maxmin[1];

        int[][] S1 = new int[gridData.getYNum()][gridData.getXNum()];
        double[] xArray = gridData.getXArray();
        double[] yArray = gridData.getYArray();
        if (gridData.getXDelta() < 0) {
            ArrayUtils.reverse(xArray);
            gridData.xReverse();
        }
        if (gridData.getYDelta() < 0) {
            ArrayUtils.reverse(yArray);
            gridData.yReverse();
        }
        Object[] cbs = ContourDraw.tracingContourLines(gridData.getData(),
                cValues, xArray, yArray, gridData.getDoubleMissingValue(), S1);
        List<PolyLine> contourLines = (List<PolyLine>) cbs[0];
        List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

        if (isSmooth) {
            contourLines = Contour.smoothLines(contourLines);
        }
        List<wcontour.global.Polygon> contourPolygons = ContourDraw.tracingPolygons(gridData.getData(), contourLines, borders, cValues);

        double v;
        ColorBreak cbb;
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZValue(offset);
        zdir = zdir.toLowerCase();
        graphics.setZDir(zdir);
        for (int i = 0; i < contourPolygons.size(); i++) {
            wcontour.global.Polygon poly = contourPolygons.get(i);
            v = poly.LowValue;
            PointZ aPoint;
            List<PointZ> pList = new ArrayList<>();
            switch (zdir) {
                case "x":
                    for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.Y = pointList.X;
                        aPoint.Z = pointList.Y;
                        aPoint.X = offset;
                        pList.add(aPoint);
                    }
                    break;
                case "y":
                    for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.X = pointList.X;
                        aPoint.Z = pointList.Y;
                        aPoint.Y = offset;
                        pList.add(aPoint);
                    }
                    break;
                case "z":
                    for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
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
            aPolygonShape.setExtent(GeometryUtil.getPointsExtent(pList));
            aPolygonShape.lowValue = v;
            if (poly.HasHoles()) {
                switch (zdir) {
                    case "x":
                        for (PolyLine holeLine : poly.HoleLines) {
                            pList = new ArrayList<>();
                            for (wcontour.global.PointD pointList : holeLine.PointList) {
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
                            for (wcontour.global.PointD pointList : holeLine.PointList) {
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
                            for (wcontour.global.PointD pointList : holeLine.PointList) {
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
        ls = ls.convertTo(ShapeTypes.POLYGON);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        double minData;
        double maxData;
        double[] maxmin = new double[2];
        gridData.getMaxMinValue(maxmin);
        maxData = maxmin[0];
        minData = maxmin[1];

        int[][] S1 = new int[gridData.getYNum()][gridData.getXNum()];
        double[] xArray = gridData.getXArray();
        double[] yArray = gridData.getYArray();
        if (gridData.getXDelta() < 0) {
            ArrayUtils.reverse(xArray);
            gridData.xReverse();
        }
        if (gridData.getYDelta() < 0) {
            ArrayUtils.reverse(yArray);
            gridData.yReverse();
        }
        Object[] cbs = ContourDraw.tracingContourLines(gridData.getData(),
                cValues, xArray, yArray, gridData.getDoubleMissingValue(), S1);
        List<PolyLine> contourLines = (List<PolyLine>) cbs[0];
        List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

        if (isSmooth) {
            contourLines = Contour.smoothLines(contourLines);
        }
        List<wcontour.global.Polygon> contourPolygons = ContourDraw.tracingPolygons(gridData.getData(), contourLines, borders, cValues);

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
            wcontour.global.Polygon poly = contourPolygons.get(i);
            v = poly.LowValue;
            PointZ aPoint;
            List<PointZ> pList = new ArrayList<>();
            switch (zdir) {
                case "x":
                    for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.Y = pointList.X;
                        aPoint.Z = pointList.Y;
                        aPoint.X = offset;
                        pList.add(aPoint);
                    }
                    break;
                case "y":
                    for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
                        aPoint = new PointZ();
                        aPoint.X = pointList.X;
                        aPoint.Z = pointList.Y;
                        aPoint.Y = offset;
                        pList.add(aPoint);
                    }
                    break;
                case "xy":
                    for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
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
                    for (wcontour.global.PointD pointList : poly.OutLine.PointList) {
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
            aPolygonShape.setExtent(GeometryUtil.getPointsExtent(pList));
            aPolygonShape.lowValue = v;
            if (poly.HasHoles()) {
                switch (zdir) {
                    case "x":
                        for (PolyLine holeLine : poly.HoleLines) {
                            pList = new ArrayList<>();
                            for (wcontour.global.PointD pointList : holeLine.PointList) {
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
                            for (wcontour.global.PointD pointList : holeLine.PointList) {
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
                            for (wcontour.global.PointD pointList : holeLine.PointList) {
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
        if (!x_s.getIndexPrivate().isFastIterator())
            x_s = x_s.copy();
        if (!y_s.getIndexPrivate().isFastIterator())
            y_s = y_s.copy();
        if (!a.getIndexPrivate().isFastIterator())
            a = a.copy();
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

        if (!x_s.getIndexPrivate().isFastIterator())
            x_s = x_s.copy();
        if (!y_s.getIndexPrivate().isFastIterator())
            y_s = y_s.copy();
        if (!a.getIndexPrivate().isFastIterator())
            a = a.copy();

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
                if (j == 0) {
                    x1 = x1 - xd * 0.5;
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
        if (!xdata.getIndexPrivate().isFastIterator())
            xdata = xdata.copy();
        if (!y1data.getIndexPrivate().isFastIterator())
            y1data = y1data.copy();
        if (!y2data.getIndexPrivate().isFastIterator())
            y2data = y2data.copy();
        if (where == null) {
            if (ArrayMath.containsNaN(y1data) || ArrayMath.containsNaN(y2data)) {
                where = Array.factory(DataType.BOOLEAN, new int[]{len});
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
            if (!where.getIndexPrivate().isFastIterator())
                where = where.copy();
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
        if (!ydata.getIndexPrivate().isFastIterator())
            ydata = ydata.copy();
        if (!x1data.getIndexPrivate().isFastIterator())
            x1data = x1data.copy();
        if (!x2data.getIndexPrivate().isFastIterator())
            x2data = x2data.copy();
        if (where == null) {
            if (ArrayMath.containsNaN(x1data) || ArrayMath.containsNaN(x2data)) {
                where = Array.factory(DataType.BOOLEAN, new int[]{len});
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
            if (!where.getIndexPrivate().isFastIterator())
                where = where.copy();
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
        if (!xdata.getIndexPrivate().isFastIterator())
            xdata = xdata.copy();
        if (!y1data.getIndexPrivate().isFastIterator())
            y1data = y1data.copy();
        if (!y2data.getIndexPrivate().isFastIterator())
            y2data = y2data.copy();
        if (where == null) {
            if (ArrayMath.containsNaN(y1data) || ArrayMath.containsNaN(y2data)) {
                where = Array.factory(DataType.BOOLEAN, new int[]{len});
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
            if (!where.getIndexPrivate().isFastIterator())
                where = where.copy();
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
        if (!xdata.getIndexPrivate().isFastIterator())
            xdata = xdata.copy();
        if (!y1data.getIndexPrivate().isFastIterator())
            y1data = y1data.copy();
        if (!y2data.getIndexPrivate().isFastIterator())
            y2data = y2data.copy();
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
                    if (!ydata.getIndexPrivate().isFastIterator())
                        ydata = ydata.copy();
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
                            if (!ydata.getIndexPrivate().isFastIterator())
                                ydata = ydata.copy();
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
            Array[] wwData = MeteoMath.uv2ds(udata, vdata);
            windDirData = wwData[0];
            windSpeedData = wwData[1];
        } else {
            windDirData = udata;
            windSpeedData = vdata;
        }

        ShapeTypes sts = ls.getShapeType();
        ls = ls.convertTo(ShapeTypes.POINT);
        if (sts != ShapeTypes.POINT) {
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator wdIter = windDirData.getIndexIterator();
        IndexIterator wsIter = windSpeedData.getIndexIterator();
        IndexIterator cIter = cdata == null ? null : cdata.getIndexIterator();
        while (xIter.hasNext()){
            windDir = wdIter.getDoubleNext();
            windSpeed = wsIter.getDoubleNext();
            if (!Double.isNaN(windDir) && !Double.isNaN(windSpeed)) {
                aPoint = new PointD();
                aPoint.X = xIter.getDoubleNext();
                aPoint.Y = yIter.getDoubleNext();
                aWB = Draw.calWindBarb((float) windDir, (float) windSpeed, 0, 10, aPoint);
                if (cdata == null) {
                    cb = ls.getLegendBreaks().get(0);
                } else {
                    v = cIter.getDoubleNext();
                    aWB.setValue(v);
                    cb = ls.findLegendBreak(v);
                }
                Graphic graphic = new Graphic(aWB, cb);
                gc.add(graphic);
            } else {
                xIter.next();
                yIter.next();
                if (cIter != null)
                    cIter.next();
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
        double[] r = MeteoMath.uv2ds(dx, dy);
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
        for (int i = 0; i < srcPts.length; i += 2) {
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
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        while (xIter.hasNext()){
            points.add(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
        }
        PolylineShape pls;
        if (iscurve) {
            pls = new CurveLineShape();
        } else {
            pls = new PolylineShape();
        }
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
            Array[] wwData = MeteoMath.uv2ds(udata, vdata);
            windDirData = wwData[0];
            windSpeedData = wwData[1];
        } else {
            windDirData = udata;
            windSpeedData = vdata;
        }

        ShapeTypes sts = ls.getShapeType();
        ls = ls.convertTo(ShapeTypes.POINT);
        if (sts != ShapeTypes.POINT) {
            for (int i = 0; i < ls.getBreakNum(); i++) {
                ((PointBreak) ls.getLegendBreaks().get(i)).setSize(10);
            }
        }

        int i;
        WindArrow wa;
        double windDir, windSpeed;
        PointD aPoint;
        ColorBreak cb;
        double x, y, v = 0;
        float size = 6;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator wdIter = windDirData.getIndexIterator();
        IndexIterator wsIter = windSpeedData.getIndexIterator();
        IndexIterator cIter = cdata == null ? null : cdata.getIndexIterator();
        while (xIter.hasNext()){
            windDir = wdIter.getDoubleNext();
            windSpeed = wsIter.getDoubleNext();
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            if (cdata != null)
                v = cIter.getDoubleNext();
            if (!Double.isNaN(windDir) && !Double.isNaN(windSpeed)) {
                aPoint = new PointD();
                aPoint.X = x;
                aPoint.Y = y;
                wa = new WindArrow();
                wa.angle = windDir;
                wa.length = (float) windSpeed;
                wa.size = size;
                wa.setPoint(aPoint);
                if (cdata == null) {
                    cb = ls.getLegendBreaks().get(0);
                } else {
                    wa.setValue(v);
                    cb = ls.findLegendBreak(v);
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

    /**
     * Create wind arrows
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param udata U data array
     * @param vdata V data array
     * @param wdata W data array
     * @param scale The length scale of each wind arrow
     * @param headWidth The head width of the arrow
     * @param headLength The head length of the arrow
     * @param cdata Colored data array
     * @param ls Legend scheme
     * @return GraphicCollection
     */
    public static GraphicCollection3D createArrows3D(Array xdata, Array ydata, Array zdata, Array udata,
            Array vdata, Array wdata, float scale, float headWidth, float headLength, Array cdata, LegendScheme ls) {
        GraphicCollection3D gc = new GraphicCollection3D();
        ShapeTypes sts = ls.getShapeType();
        ls = ls.convertTo(ShapeTypes.POINT);
        if (sts != ShapeTypes.POINT) {
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
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        IndexIterator uIter = udata.getIndexIterator();
        IndexIterator vIter = vdata.getIndexIterator();
        IndexIterator wIter = wdata.getIndexIterator();
        IndexIterator cIter = cdata == null ? null : cdata.getIndexIterator();
        while (xIter.hasNext()){
            u = uIter.getDoubleNext();
            v = vIter.getDoubleNext();
            w = wIter.getDoubleNext();
            if (!Double.isNaN(u) && !Double.isNaN(v) && !Double.isNaN(w)) {
                aPoint = new PointZ();
                aPoint.X = xIter.getDoubleNext();
                aPoint.Y = yIter.getDoubleNext();
                aPoint.Z = zIter.getDoubleNext();
                wa = new WindArrow3D();
                wa.u = u;
                wa.v = v;
                wa.w = w;
                wa.scale = scale;
                wa.setHeadWith(headWidth);
                wa.setHeadLength(headLength);
                wa.setPoint(aPoint);
                if (cdata == null) {
                    cb = ls.getLegendBreaks().get(0);
                } else {
                    value = cIter.getDoubleNext();
                    wa.setValue(value);
                    cb = ls.findLegendBreak(value);
                }
                Graphic graphic = new Graphic(wa, cb);
                gc.add(graphic);
            } else {
                xIter.next();
                yIter.next();
                zIter.next();
                if (cdata != null)
                    cIter.next();
            }
        }

        gc.setLegendScheme(ls);
        if (cdata != null) {
            gc.setSingleLegend(false);
        }

        return gc;
    }

    /**
     * Create 3D streamlines
     * @param xdata X coordinate array
     * @param ydata Y coordinate array
     * @param zdata Z coordinate array
     * @param udata U wind component array
     * @param vdata V wind component array
     * @param wdata W wind component array
     * @param cdata Value array
     * @param density Streamline density
     * @param ls The legend scheme
     * @param minPoints Minimum point number of streamline
     * @param loopLimit Limit loop number
     * @return
     */
    public static GraphicCollection3D createStreamlines3D(Array xdata, Array ydata, Array zdata,
                                                          Array udata, Array vdata, Array wdata,
                                                          Array cdata, int density, LegendScheme ls,
                                                          int minPoints, int loopLimit) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        double[][][] u = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(udata);
        double[][][] v = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(vdata);
        double[][][] w = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(wdata);
        if (xdata.getRank() == 1) {
            double[] x = (double[]) ArrayUtil.copyToNDJavaArray_Double(xdata);
            double[] y = (double[]) ArrayUtil.copyToNDJavaArray_Double(ydata);
            double[] z = (double[]) ArrayUtil.copyToNDJavaArray_Double(zdata);

            if (cdata == null) {
                List<PolyLine3D> streamLines = Contour.tracingStreamline3D(u, v, w, null, x, y, z, density, loopLimit);
                ColorBreak cb = ls.getLegendBreak(0);
                for (PolyLine3D line : streamLines) {
                    if (line.PointList.size() < minPoints)
                        continue;

                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (Point3D point : line.PointList) {
                        points.add(new PointZ(point.X, point.Y, point.Z));
                    }
                    shape.setPoints(points);
                    graphics.add(new Graphic(shape, cb));
                }
            } else {
                double[][][] m = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(cdata);
                List<PolyLine3D> streamLines = Contour.tracingStreamline3D(u, v, w, m, x, y, z, density, loopLimit);
                ColorBreak cb;
                double mm;
                for (PolyLine3D line : streamLines) {
                    if (line.PointList.size() < minPoints)
                        continue;

                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    ColorBreakCollection cbs = new ColorBreakCollection();
                    for (Point3D point : line.PointList) {
                        points.add(new PointZ(point.X, point.Y, point.Z));
                        cb = ls.findLegendBreak(point.M);
                        cbs.add(cb);
                    }
                    shape.setPoints(points);
                    graphics.add(new Graphic(shape, cbs));
                }
            }
        } else {
            double[][][] x = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(xdata);
            double[][][] y = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(ydata);
            double[][][] z = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(zdata);

            if (cdata == null) {
                List<PolyLine3D> streamLines = Contour.tracingStreamline3D(u, v, w, null, x, y, z, density, loopLimit);
                ColorBreak cb = ls.getLegendBreak(0);
                for (PolyLine3D line : streamLines) {
                    if (line.PointList.size() < minPoints)
                        continue;

                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (Point3D point : line.PointList) {
                        points.add(new PointZ(point.X, point.Y, point.Z));
                    }
                    shape.setPoints(points);
                    graphics.add(new Graphic(shape, cb));
                }
            } else {
                double[][][] m = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(cdata);
                List<PolyLine3D> streamLines = Contour.tracingStreamline3D(u, v, w, m, x, y, z, density, loopLimit);
                ColorBreak cb;
                double mm;
                for (PolyLine3D line : streamLines) {
                    if (line.PointList.size() < minPoints)
                        continue;

                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    ColorBreakCollection cbs = new ColorBreakCollection();
                    for (Point3D point : line.PointList) {
                        points.add(new PointZ(point.X, point.Y, point.Z));
                        cb = ls.findLegendBreak(point.M);
                        cbs.add(cb);
                    }
                    shape.setPoints(points);
                    graphics.add(new Graphic(shape, cbs));
                }
            }
        }
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create 3D streamlines
     * @param xdata X coordinate array
     * @param ydata Y coordinate array
     * @param zdata Z coordinate array
     * @param udata U wind component array
     * @param vdata V wind component array
     * @param wdata W wind component array
     * @param cdata Value array
     * @param density Streamline density
     * @param ls The legend scheme
     * @param minPoints Minimum point number of streamline
     * @param loopLimit Limit loop number
     * @param startX Start x coordinates
     * @param startY Start y coordinates
     * @param startZ Start z coordinates
     * @return
     */
    public static GraphicCollection3D createStreamlines3D(Array xdata, Array ydata, Array zdata,
                                                          Array udata, Array vdata, Array wdata,
                                                          Array cdata, int density, LegendScheme ls,
                                                          int minPoints, int loopLimit, Array startX,
                                                          Array startY, Array startZ) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        double[][][] u = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(udata);
        double[][][] v = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(vdata);
        double[][][] w = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(wdata);
        double[] sX = (double[]) ArrayUtil.copyToNDJavaArray_Double(startX);
        double[] sY = (double[]) ArrayUtil.copyToNDJavaArray_Double(startY);
        double[] sZ = (double[]) ArrayUtil.copyToNDJavaArray_Double(startZ);
        if (xdata.getRank() == 1) {
            double[] x = (double[]) ArrayUtil.copyToNDJavaArray_Double(xdata);
            double[] y = (double[]) ArrayUtil.copyToNDJavaArray_Double(ydata);
            double[] z = (double[]) ArrayUtil.copyToNDJavaArray_Double(zdata);

            if (cdata == null) {
                List<PolyLine3D> streamLines = Contour.tracingStreamline3D(u, v, w, null, x, y, z, density,
                        sX, sY, sZ, loopLimit);
                ColorBreak cb = ls.getLegendBreak(0);
                for (PolyLine3D line : streamLines) {
                    if (line.PointList.size() < minPoints)
                        continue;

                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (Point3D point : line.PointList) {
                        points.add(new PointZ(point.X, point.Y, point.Z));
                    }
                    shape.setPoints(points);
                    graphics.add(new Graphic(shape, cb));
                }
            } else {
                double[][][] m = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(cdata);
                List<PolyLine3D> streamLines = Contour.tracingStreamline3D(u, v, w, m, x, y, z, density,
                        sX, sY, sZ, loopLimit);
                ColorBreak cb;
                double mm;
                for (PolyLine3D line : streamLines) {
                    if (line.PointList.size() < minPoints)
                        continue;

                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    ColorBreakCollection cbs = new ColorBreakCollection();
                    for (Point3D point : line.PointList) {
                        points.add(new PointZ(point.X, point.Y, point.Z));
                        cb = ls.findLegendBreak(point.M);
                        cbs.add(cb);
                    }
                    shape.setPoints(points);
                    graphics.add(new Graphic(shape, cbs));
                }
            }
        } else {
            double[][][] x = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(xdata);
            double[][][] y = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(ydata);
            double[][][] z = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(zdata);

            if (cdata == null) {
                List<PolyLine3D> streamLines = Contour.tracingStreamline3D(u, v, w, null, x, y, z, density,
                        sX, sY, sZ, loopLimit);
                ColorBreak cb = ls.getLegendBreak(0);
                for (PolyLine3D line : streamLines) {
                    if (line.PointList.size() < minPoints)
                        continue;

                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    for (Point3D point : line.PointList) {
                        points.add(new PointZ(point.X, point.Y, point.Z));
                    }
                    shape.setPoints(points);
                    graphics.add(new Graphic(shape, cb));
                }
            } else {
                double[][][] m = (double[][][]) ArrayUtil.copyToNDJavaArray_Double(cdata);
                List<PolyLine3D> streamLines = Contour.tracingStreamline3D(u, v, w, m, x, y, z, density,
                        sX, sY, sZ, loopLimit);
                ColorBreak cb;
                double mm;
                for (PolyLine3D line : streamLines) {
                    if (line.PointList.size() < minPoints)
                        continue;

                    PolylineZShape shape = new PolylineZShape();
                    List<PointZ> points = new ArrayList<>();
                    ColorBreakCollection cbs = new ColorBreakCollection();
                    for (Point3D point : line.PointList) {
                        points.add(new PointZ(point.X, point.Y, point.Z));
                        cb = ls.findLegendBreak(point.M);
                        cbs.add(cb);
                    }
                    shape.setPoints(points);
                    graphics.add(new Graphic(shape, cbs));
                }
            }
        }
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create stream line
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param udata U/WindDirection data array
     * @param vdata V/WindSpeed data array
     * @param density Streamline density
     * @param slb Streamline break
     * @param isUV Is U/V or not
     * @return GraphicCollection
     */
    public static GraphicCollection createStreamlines(Array xdata, Array ydata, Array udata, Array vdata,
            int density, StreamlineBreak slb, boolean isUV) {
        GraphicCollection gc = new GraphicCollection();
        if (!isUV) {            
            Array[] uvData = MeteoMath.ds2uv(udata, vdata);
            udata = uvData[0];
            vdata = uvData[1];
        }
        
        double[][] u = (double[][])ArrayUtil.copyToNDJavaArray_Double(udata);
        double[][] v = (double[][])ArrayUtil.copyToNDJavaArray_Double(vdata);
        double[] x = (double[]) ArrayUtil.copyToNDJavaArray_Double(xdata);
        double[] y = (double[]) ArrayUtil.copyToNDJavaArray_Double(ydata);
        List<PolyLine> streamlines = Contour.tracingStreamline(u, v,
                x, y, density);
        PolyLine line;
        for (int i = 0; i < streamlines.size() - 1; i++) {
            line = streamlines.get(i);
            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (int j = 0; j < line.PointList.size(); j++) {
                aPoint = new PointD();
                aPoint.X = (line.PointList.get(j)).X;
                aPoint.Y = (line.PointList.get(j)).Y;
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(density);
            gc.add(new Graphic(aPolyline, slb));
        }

        return gc;
    }

    /**
     * Trace streamlines
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param z Z value
     * @param ua U component
     * @param va V component
     * @param data Data array
     * @param density Streamline density
     * @param zDir Z direction: "x", "y" or "z"
     * @param ls Legend scheme
     * @return Streamlines
     */
    public static GraphicCollection3D streamLines(Array xa, Array ya, double z, Array ua,
                                                  Array va, Array data, int density,
                                                  String zDir, LegendScheme ls) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        double[][] u = (double[][]) ArrayUtil.copyToNDJavaArray_Double(ua);
        double[][] v = (double[][]) ArrayUtil.copyToNDJavaArray_Double(va);
        List<PolyLine> streamlines;
        if (xa.getRank() == 1) {
            double[] x = (double[]) ArrayUtil.copyToNDJavaArray_Double(xa);
            double[] y = (double[]) ArrayUtil.copyToNDJavaArray_Double(ya);
            streamlines = Contour.tracingStreamline(u, v,
                    x, y, density);
        } else {
            xa = xa.copyIfView();
            ya = ya.copyIfView();
            double[][] x = (double[][]) ArrayUtil.copyToNDJavaArray_Double(xa);
            double[][] y = (double[][]) ArrayUtil.copyToNDJavaArray_Double(ya);
            streamlines = Contour.tracingStreamline(u, v,
                    x, y, density);
        }

        int ny = u.length;
        int nx = u[0].length;
        ColorBreak cb = ls.getLegendBreak(0);
        if (data == null) {
            for (PolyLine line : streamlines) {
                PolylineZShape shape = new PolylineZShape();
                List<PointZ> points = new ArrayList<>();
                PointZ p;
                if (zDir.equals("x")) {
                    for (int j = 0; j < line.PointList.size(); j++) {
                        p = new PointZ();
                        p.Y = (line.PointList.get(j)).X;
                        p.Z = (line.PointList.get(j)).Y;
                        p.X = z;
                        points.add(p);
                    }
                } else if (zDir.equals("y")) {
                    for (int j = 0; j < line.PointList.size(); j++) {
                        p = new PointZ();
                        p.X = (line.PointList.get(j)).X;
                        p.Z = (line.PointList.get(j)).Y;
                        p.Y = z;
                        points.add(p);
                    }
                } else {
                    for (int j = 0; j < line.PointList.size(); j++) {
                        p = new PointZ();
                        p.X = (line.PointList.get(j)).X;
                        p.Y = (line.PointList.get(j)).Y;
                        p.Z = z;
                        points.add(p);
                    }
                }
                shape.setPoints(points);
                graphics.add(new Graphic(shape, cb));
            }
        } else {
            data = data.copyIfView();
            for (PolyLine line : streamlines) {
                PolylineZShape shape = new PolylineZShape();
                List<PointZ> points = new ArrayList<>();
                PointZ p;
                ColorBreakCollection cbs = new ColorBreakCollection();
                if (zDir.equals("x")) {
                    for (int j = 0; j < line.PointList.size(); j++) {
                        p = new PointZ();
                        p.Y = (line.PointList.get(j)).X;
                        p.Z = (line.PointList.get(j)).Y;
                        p.X = z;
                        int[] idx = ArrayUtil.gridIndex(xa, ya, p.Y, p.Z);
                        if (idx != null) {
                            int yi = idx[0];
                            int xi = idx[1];
                            p.M = data.getDouble(yi * nx + xi);
                        }
                        cb = ls.findLegendBreak(p.M);
                        cbs.add(cb);
                        points.add(p);
                    }
                } else if (zDir.equals("y")) {
                    for (int j = 0; j < line.PointList.size(); j++) {
                        p = new PointZ();
                        p.X = (line.PointList.get(j)).X;
                        p.Z = (line.PointList.get(j)).Y;
                        p.Y = z;
                        int[] idx = ArrayUtil.gridIndex(xa, ya, p.X, p.Z);
                        if (idx != null) {
                            int yi = idx[0];
                            int xi = idx[1];
                            p.M = data.getDouble(yi * nx + xi);
                        }
                        cb = ls.findLegendBreak(p.M);
                        cbs.add(cb);
                        points.add(p);
                    }
                } else {
                    for (int j = 0; j < line.PointList.size(); j++) {
                        p = new PointZ();
                        p.X = (line.PointList.get(j)).X;
                        p.Y = (line.PointList.get(j)).Y;
                        p.Z = z;
                        int[] idx = ArrayUtil.gridIndex(xa, ya, p.X, p.Y);
                        if (idx != null) {
                            int yi = idx[0];
                            int xi = idx[1];
                            p.M = data.getDouble(yi * nx + xi);
                        }
                        cb = ls.findLegendBreak(p.M);
                        cbs.add(cb);
                        points.add(p);
                    }
                }
                shape.setPoints(points);
                graphics.add(new Graphic(shape, cbs));
            }
        }
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Trace streamlines
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param xySlice XY slice value - [x1,y1,x2,y2]
     * @param ua U component
     * @param va V component
     * @param data Data array
     * @param density Streamline density
     * @param ls Legend scheme
     * @return Streamlines
     */
    public static GraphicCollection3D streamLinesXY(Array xa, Array ya, List<Number> xySlice, Array ua,
                                                  Array va, Array data, int density, LegendScheme ls) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        double[][] u = (double[][]) ArrayUtil.copyToNDJavaArray_Double(ua);
        double[][] v = (double[][]) ArrayUtil.copyToNDJavaArray_Double(va);
        List<PolyLine> streamlines;
        if (xa.getRank() == 1) {
            double[] x = (double[]) ArrayUtil.copyToNDJavaArray_Double(xa);
            double[] y = (double[]) ArrayUtil.copyToNDJavaArray_Double(ya);
            streamlines = Contour.tracingStreamline(u, v,
                    x, y, density);
        } else {
            xa = xa.copyIfView();
            ya = ya.copyIfView();
            double[][] x = (double[][]) ArrayUtil.copyToNDJavaArray_Double(xa);
            double[][] y = (double[][]) ArrayUtil.copyToNDJavaArray_Double(ya);
            streamlines = Contour.tracingStreamline(u, v,
                    x, y, density);
        }

        int ny = u.length;
        int nx = u[0].length;
        ColorBreak cb = ls.getLegendBreak(0);
        double x1 = xySlice.get(0).doubleValue();
        double y1 = xySlice.get(1).doubleValue();
        double x2 = xySlice.get(2).doubleValue();
        double y2 = xySlice.get(3).doubleValue();
        if (data == null) {
            for (PolyLine line : streamlines) {
                PolylineZShape shape = new PolylineZShape();
                List<PointZ> points = new ArrayList<>();
                PointZ p;
                for (int j = 0; j < line.PointList.size(); j++) {
                    p = new PointZ();
                    p.X = (line.PointList.get(j)).X;
                    p.Z = (line.PointList.get(j)).Y;
                    p.Y = y1 + (y2 - y1) * (p.X - x1) / (x2 - x1);
                    points.add(p);
                }
                shape.setPoints(points);
                graphics.add(new Graphic(shape, cb));
            }
        } else {
            data = data.copyIfView();
            for (PolyLine line : streamlines) {
                PolylineZShape shape = new PolylineZShape();
                List<PointZ> points = new ArrayList<>();
                PointZ p;
                ColorBreakCollection cbs = new ColorBreakCollection();
                for (int j = 0; j < line.PointList.size(); j++) {
                    p = new PointZ();
                    p.X = (line.PointList.get(j)).X;
                    p.Z = (line.PointList.get(j)).Y;
                    p.Y = y1 + (y2 - y1) * (p.X - x1) / (x2 - x1);
                    int[] idx = ArrayUtil.gridIndex(xa, ya, p.X, p.Z);
                    if (idx != null) {
                        int yi = idx[0];
                        int xi = idx[1];
                        p.M = data.getDouble(yi * nx + xi);
                    }
                    cb = ls.findLegendBreak(p.M);
                    cbs.add(cb);
                    points.add(p);
                }
                shape.setPoints(points);
                graphics.add(new Graphic(shape, cbs));
            }
        }
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Trace streamlines
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate value
     * @param ua U component
     * @param va V component
     * @param data Data array
     * @param density Streamline density
     * @param ls Legend scheme
     * @return Streamlines
     */
    public static GraphicCollection3D streamLines(Array xa, Array ya, Array za, Array ua,
                                                  Array va, Array data, int density,
                                                  LegendScheme ls) {
        GraphicCollection3D graphics = new GraphicCollection3D();
        double[][] u = (double[][]) ArrayUtil.copyToNDJavaArray_Double(ua);
        double[][] v = (double[][]) ArrayUtil.copyToNDJavaArray_Double(va);
        List<PolyLine> streamlines;
        if (xa.getRank() == 1) {
            double[] x = (double[]) ArrayUtil.copyToNDJavaArray_Double(xa);
            double[] y = (double[]) ArrayUtil.copyToNDJavaArray_Double(ya);
            streamlines = Contour.tracingStreamline(u, v,
                    x, y, density);
        } else {
            xa = xa.copyIfView();
            ya = ya.copyIfView();
            double[][] x = (double[][]) ArrayUtil.copyToNDJavaArray_Double(xa);
            double[][] y = (double[][]) ArrayUtil.copyToNDJavaArray_Double(ya);
            streamlines = Contour.tracingStreamline(u, v,
                    x, y, density);
        }

        int ny = u.length;
        int nx = u[0].length;
        ColorBreak cb = ls.getLegendBreak(0);
        if (data == null) {
            for (PolyLine line : streamlines) {
                PolylineZShape shape = new PolylineZShape();
                List<PointZ> points = new ArrayList<>();
                PointZ p;
                for (int j = 0; j < line.PointList.size(); j++) {
                    p = new PointZ();
                    p.X = (line.PointList.get(j)).X;
                    p.Y = (line.PointList.get(j)).Y;
                    int[] idx = ArrayUtil.gridIndex(xa, ya, p.Y, p.Z);
                    if (idx != null) {
                        int yi = idx[0];
                        int xi = idx[1];
                        p.Z = za.getDouble(yi * nx + xi);
                    }
                    points.add(p);
                }
                shape.setPoints(points);
                graphics.add(new Graphic(shape, cb));
            }
        } else {
            data = data.copyIfView();
            for (PolyLine line : streamlines) {
                PolylineZShape shape = new PolylineZShape();
                List<PointZ> points = new ArrayList<>();
                PointZ p;
                ColorBreakCollection cbs = new ColorBreakCollection();
                for (int j = 0; j < line.PointList.size(); j++) {
                    p = new PointZ();
                    p.X = (line.PointList.get(j)).X;
                    p.Y = (line.PointList.get(j)).Y;
                    int[] idx = ArrayUtil.gridIndex(xa, ya, p.X, p.Y);
                    if (idx != null) {
                        int yi = idx[0];
                        int xi = idx[1];
                        p.Z = za.getDouble(yi * nx + xi);
                        p.M = data.getDouble(yi * nx + xi);
                    }
                    cb = ls.findLegendBreak(p.M);
                    cbs.add(cb);
                    points.add(p);
                }
                shape.setPoints(points);
                graphics.add(new Graphic(shape, cbs));
            }
        }
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create streamline slices in 3D axes
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param ua U component array
     * @param va V component array
     * @param wa W component array
     * @param data Data array
     * @param xSlice X slices
     * @param ySlice Y slices
     * @param zSlice Z slices
     * @param density Streamline density
     * @param ls Legend scheme
     * @return Streamline slices graphics
     * @throws InvalidRangeException
     */
    public static List<GraphicCollection3D> streamSlice(Array xa, Array ya, Array za, Array ua,
                                                        Array va, Array wa, Array data, List<Number> xSlice,
                                                        List<Number> ySlice, List<Number> zSlice,
                                                        int density, LegendScheme ls) throws InvalidRangeException {
        List<GraphicCollection3D> sgs = new ArrayList<>();
        double x, y, z;

        //X slice
        for (int i = 0; i < xSlice.size(); i++) {
            x = xSlice.get(i).doubleValue();
            Array aa = xa;
            if (xa.getRank() == 3) {
                int[] shape = xa.getShape();
                aa = xa.section(new int[]{0,0,0}, new int[]{1,1,shape[2]});
            }
            Array xua = ArrayUtil.slice(va, 2, aa, x);
            Array xva = ArrayUtil.slice(wa, 2, aa, x);
            Array r = data == null ? null : ArrayUtil.slice(data, 2, aa, x);
            Array yya = ya.getRank() == 1 ? ya : ArrayUtil.slice(ya, 2, aa, x);
            Array zza = za.getRank() == 1 ? za : ArrayUtil.slice(za, 2, aa, x);
            zza = zza.copyIfView();
            if ((zza.getDouble(1) - zza.getDouble(0)) != (zza.getDouble(2) - zza.getDouble(1))) {
                Array[] xy = ArrayUtil.meshgrid(yya, zza);
                yya = xy[0];
                zza = xy[1];
            }
            GraphicCollection3D graphics = streamLines(yya, zza, x, xua, xva, r, density, "x", ls);
            sgs.add(graphics);
        }

        //Y slice
        for (int i = 0; i < ySlice.size(); i++) {
            y = ySlice.get(i).doubleValue();
            Array aa = ya;
            if (ya.getRank() == 3) {
                int[] shape = ya.getShape();
                aa = ya.section(new int[]{0,0,0}, new int[]{1,shape[1],1});
            }
            Array xua = ArrayUtil.slice(ua, 1, aa, y);
            Array xva = ArrayUtil.slice(wa, 1, aa, y);
            Array r = data == null ? null : ArrayUtil.slice(data, 1, aa, y);
            Array xxa = xa.getRank() == 1 ? xa : ArrayUtil.slice(xa, 1, aa, y);
            Array zza = za.getRank() == 1 ? za : ArrayUtil.slice(za, 1, aa, y);
            zza = zza.copyIfView();
            if ((zza.getDouble(1) - zza.getDouble(0)) != (zza.getDouble(2) - zza.getDouble(1))) {
                Array[] xy = ArrayUtil.meshgrid(xxa, zza);
                xxa = xy[0];
                zza = xy[1];
            }
            GraphicCollection3D graphics = streamLines(xxa, zza, y, xua, xva, r, density, "y", ls);
            sgs.add(graphics);
        }

        //Z slice
        for (int i = 0; i < zSlice.size(); i++) {
            z = zSlice.get(i).doubleValue();
            Array aa = za;
            if (za.getRank() == 3) {
                int[] shape = za.getShape();
                aa = za.section(new int[]{0,0,0}, new int[]{shape[0],1,1});
            }
            Array xua = ArrayUtil.slice(ua, 0, aa, z);
            Array xva = ArrayUtil.slice(va, 0, aa, z);
            Array r = data == null ? null : ArrayUtil.slice(data, 0, aa, z);
            Array xxa = xa.getRank() == 1 ? xa : ArrayUtil.slice(xa, 0, aa, z);
            Array yya = ya.getRank() == 1 ? ya : ArrayUtil.slice(ya, 0, aa, z);
            GraphicCollection3D graphics = streamLines(xxa, yya, z, xua, xva, r, density, "z", ls);
            sgs.add(graphics);
        }

        return sgs;
    }

    /**
     * Create streamline slices in 3D axes
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param ua U component array
     * @param va V component array
     * @param wa W component array
     * @param data Data array
     * @param zSliceIndex Z slice index list
     * @param density Streamline density
     * @param ls Legend scheme
     * @return Streamline slices graphics
     * @throws InvalidRangeException
     */
    public static List<GraphicCollection3D> streamSlice(Array xa, Array ya, Array za, Array ua,
                                                        Array va, Array wa, Array data, List<Integer> zSliceIndex,
                                                        int density, LegendScheme ls) throws InvalidRangeException {
        List<GraphicCollection3D> sgs = new ArrayList<>();
        int zIdx;

        //Z slice
        for (int i = 0; i < zSliceIndex.size(); i++) {
            zIdx = zSliceIndex.get(i);
            Array aa = za;
            Array xua = ArrayUtil.slice(ua, 0, zIdx);
            Array xva = ArrayUtil.slice(va, 0, zIdx);
            Array r = data == null ? null : ArrayUtil.slice(data, 0, zIdx);
            Array xxa = xa.getRank() == 1 ? xa : ArrayUtil.slice(xa, 0, zIdx);
            Array yya = ya.getRank() == 1 ? ya : ArrayUtil.slice(ya, 0, zIdx);
            GraphicCollection3D graphics;
            if (za.getRank() == 1) {
                double z = za.getDouble(zIdx);
                graphics = streamLines(xxa, yya, z, xua, xva, r, density, "z", ls);
            } else {
                Array zza = ArrayUtil.slice(za, 0, zIdx);
                zza = zza.copyIfView();
                graphics = streamLines(xxa, yya, zza, xua, xva, r, density, ls);
            }
            sgs.add(graphics);
        }

        return sgs;
    }

    /**
     * Create streamline slices in 3D axes
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param ua U component array
     * @param va V component array
     * @param wa W component array
     * @param data Data array
     * @param xySlice XY slices - [x1,y1,x2,y2]
     * @param method Interpolation method
     * @param density Streamline density
     * @param ls Legend scheme
     * @return Streamline slices graphics
     * @throws InvalidRangeException
     */
    public static GraphicCollection3D streamSlice(Array xa, Array ya, Array za, Array ua,
                                                        Array va, Array wa, Array data, List<Number> xySlice,
                                                        InterpolationMethod method, int density, LegendScheme ls) throws InvalidRangeException {
        Array[] xyu = InterpUtil.sliceXY(xa, ya, za, ua, xySlice, method);
        Array xua = xyu[0];
        Array[] xyv = InterpUtil.sliceXY(xa, ya, za, va, xySlice, method);
        Array xva = xyv[0];
        Array[] wds = MeteoMath.uv2ds(xua, xva);
        double x1 = xySlice.get(0).doubleValue();
        double y1 = xySlice.get(1).doubleValue();
        double x2 = xySlice.get(2).doubleValue();
        double y2 = xySlice.get(3).doubleValue();
        double angle = Math.atan2(y2 - y1, x2 - x1) * 180 / Math.PI;
        Array wd = ArrayMath.add(wds[0], angle);
        Array[] uv = MeteoMath.ds2uv(wd, wds[1]);
        xua = uv[0];
        Array[] xyw = InterpUtil.sliceXY(xa, ya, za, wa, xySlice, method);
        Array xwa = xyw[0];
        Array r = data == null ? null : InterpUtil.sliceXY(xa, ya, za, data, xySlice, method)[0];
        Array yya = xyu[1];
        Array zza = za.getRank() == 1 ? za : ArrayUtil.slice(za, 2, 0);
        GraphicCollection3D graphics = streamLinesXY(yya, zza, xySlice, xua, xwa, r, density, ls);

        return graphics;
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
     * @param radius Pie radius
     * @param wedgeprops Wedge properties
     * @return GraphicCollection
     */
    public static GraphicCollection[] createPieArcs(Array xdata, List<Color> colors,
            List<String> labels, float startAngle, List<Number> explode, Font labelFont,
            Color labelColor, float labelDis, String autopct, float pctDis, float radius,
            HashMap wedgeprops) {
        GraphicCollection gc = new GraphicCollection();
        GraphicCollection lgc = new GraphicCollection();
        GraphicCollection pgc = new GraphicCollection();
        double sum = ArrayMath.sum(xdata).doubleValue();
        double v;
        float sweepAngle, angle;
        float ex;
        double dx, dy, ldx, ldy;
        String label, pct = null;
        LegendScheme ls = new LegendScheme(ShapeTypes.POLYGON);
        Boolean drawEdge = wedgeprops.get("drawedge") == null ? null : (Boolean) wedgeprops.get("drawedge");
        Color edgeColor = wedgeprops.get("edgecolor") == null ? null : (Color) wedgeprops.get("edgecolor");
        Float lineWidth = wedgeprops.get("linewidth") == null ? null : Float.parseFloat(String.valueOf(wedgeprops.get("linewidth")));
        Float wedgeWidth = wedgeprops.get("width") == null ? null : Float.parseFloat(String.valueOf(wedgeprops.get("width")));
        IndexIterator xIter = xdata.getIndexIterator();
        int i = 0;
        while (xIter.hasNext()){
            v = xIter.getDoubleNext();
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
                dx = radius * ex * Math.cos(angle * Math.PI / 180);
                dy = radius * ex * Math.sin(angle * Math.PI / 180);
            }
            List<PointD> points = new ArrayList<>();
            points.add(new PointD(-radius + dx, -radius + dy));
            points.add(new PointD(-radius + dx, radius + dy));
            points.add(new PointD(radius + dx, radius + dy));
            points.add(new PointD(radius + dx, -radius + dy));
            points.add(new PointD(dx, dy));
            aShape.setPoints(points);
            if (wedgeWidth != null) {
                aShape.setWedgeWidth(wedgeWidth);
            }
            PolygonBreak pgb = new PolygonBreak();
            pgb.setColor(colors.get(i));
            if (drawEdge != null) {
                pgb.setDrawOutline(drawEdge);
            }
            if (edgeColor != null) {
                pgb.setOutlineColor(edgeColor);
            }
            if (lineWidth != null) {
                pgb.setOutlineSize(lineWidth);
            }
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
            if (!label.isEmpty()) {
                ChartText ps = new ChartText();
                ldx = dx + radius * labelDis * Math.cos(angle * Math.PI / 180);
                ldy = dy + radius * labelDis * Math.sin(angle * Math.PI / 180);
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
            }

            //pct text
            if (pct != null) {
                ChartText ps = new ChartText();
                ldx = dx + radius * pctDis * Math.cos(angle * Math.PI / 180);
                ldy = dy + radius * pctDis * Math.sin(angle * Math.PI / 180);
                ps.setPoint(ldx, ldy);
                ps.setText(pct);
                ps.setFont(labelFont);
                ps.setColor(labelColor);
                ps.setXAlign(XAlign.CENTER);
                ps.setYAlign(YAlign.CENTER);
                pgc.add(new Graphic(ps, new ColorBreak()));
            }

            startAngle += sweepAngle;
            i++;
        }
        gc.setSingleLegend(false);
        gc.setLegendScheme(ls);
        gc.getLabelSet().setLabelFont(labelFont);
        gc.getLabelSet().setLabelColor(labelColor);
        dx = radius * 0.1;
        if (labels != null || autopct != null) {
            Extent ext = gc.getExtent().extend(dx, dx);
            gc.setExtent(ext);
        }

        if (pct == null) {
            if (lgc.isEmpty()) {
                return new GraphicCollection[]{gc};
            } else {
                return new GraphicCollection[]{gc, lgc};
            }
        } else {
            if (lgc.isEmpty()) {
                return new GraphicCollection[]{gc, pgc};
            } else {
                return new GraphicCollection[]{gc, lgc, pgc};
            }
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
     * @param showmedians Show medians or not
     * @param boxBreak Box polygon break
     * @param medianBreak Meandian line break
     * @param whiskerBreak Whisker line break
     * @param capBreak Whisker cap line break
     * @param meanBreak Mean point break
     * @param flierBreak Flier point break
     * @return GraphicCollection
     */
    public static GraphicCollection createBox(List<Array> xdata, List<Number> positions, List<Number> widths,
            boolean showcaps, boolean showfliers, boolean showmeans, boolean showmedians, PolygonBreak boxBreak,
            ColorBreak medianBreak, PolylineBreak whiskerBreak, PolylineBreak capBreak,
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
            flierBreak.setStyle(PointStyle.PLUS);
        }
        if (meanBreak == null) {
            meanBreak = new PointBreak();
            ((PointBreak) meanBreak).setStyle(PointStyle.SQUARE);
            ((PointBreak) meanBreak).setColor(Color.red);
            ((PointBreak) meanBreak).setOutlineColor(Color.black);
        }

        for (int i = 0; i < n; i++) {
            Array a = xdata.get(i);
            if (Double.isNaN(ArrayMath.min(a).doubleValue())) {
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
            if (showmedians) {
                if (medianBreak.getBreakType() == BreakTypes.POLYLINE_BREAK) {
                    pList = new ArrayList<>();
                    pList.add(new PointD(v - width * 0.5, median));
                    pList.add(new PointD(v + width * 0.5, median));
                    PolylineShape pls = new PolylineShape();
                    pls.setPoints(pList);
                    gc.add(new Graphic(pls, medianBreak));
                } else {
                    PointShape ps = new PointShape();
                    ps.setPoint(new PointD(v, median));
                    gc.add(new Graphic(ps, medianBreak));
                }
            }

            //Add low whisker line
            double min = Math.max(mino, mind);
            pList = new ArrayList<>();
            pList.add(new PointD(v, q1));
            pList.add(new PointD(v, min));
            PolylineShape pls = new PolylineShape();
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
                if (meanBreak.getBreakType() == BreakTypes.POINT_BREAK) {
                    PointShape ps = new PointShape();
                    ps.setPoint(new PointD(v, mean));
                    gc.add(new Graphic(ps, meanBreak));
                } else {
                    pList = new ArrayList<>();
                    pList.add(new PointD(v - width * 0.5, mean));
                    pList.add(new PointD(v + width * 0.5, mean));
                    pls = new PolylineShape();
                    pls.setPoints(pList);
                    gc.add(new Graphic(pls, meanBreak));
                }
            }
        }
        gc.setSingleLegend(false);

        return gc;
    }

    /**
     * Create horizontal box graphics
     *
     * @param xdata X data array list
     * @param positions Box position list
     * @param widths Box width list
     * @param showcaps Show caps or not
     * @param showfliers Show fliers or not
     * @param showmeans Show means or not
     * @param showmedians Show medians or not
     * @param boxBreak Box polygon break
     * @param medianBreak Meandian line break
     * @param whiskerBreak Whisker line break
     * @param capBreak Whisker cap line break
     * @param meanBreak Mean point break
     * @param flierBreak Flier point break
     * @return GraphicCollection
     */
    public static GraphicCollection createHBox(List<Array> xdata, List<Number> positions, List<Number> widths,
                                              boolean showcaps, boolean showfliers, boolean showmeans, boolean showmedians, PolygonBreak boxBreak,
                                              ColorBreak medianBreak, PolylineBreak whiskerBreak, PolylineBreak capBreak,
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
            flierBreak.setStyle(PointStyle.PLUS);
        }
        if (meanBreak == null) {
            meanBreak = new PointBreak();
            ((PointBreak) meanBreak).setStyle(PointStyle.SQUARE);
            ((PointBreak) meanBreak).setColor(Color.red);
            ((PointBreak) meanBreak).setOutlineColor(Color.black);
        }

        for (int i = 0; i < n; i++) {
            Array a = xdata.get(i);
            if (Double.isNaN(ArrayMath.min(a).doubleValue())) {
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
            pList.add(new PointD(q1, v - width * 0.5));
            pList.add(new PointD(q3, v - width * 0.5));
            pList.add(new PointD(q3, v + width * 0.5));
            pList.add(new PointD(q1, v + width * 0.5));
            pList.add(new PointD(q1, v - width * 0.5));
            PolygonShape pgs = new PolygonShape();
            pgs.setPoints(pList);
            gc.add(new Graphic(pgs, boxBreak));

            //Add meadian line
            if (showmedians) {
                if (medianBreak.getBreakType() == BreakTypes.POLYLINE_BREAK) {
                    pList = new ArrayList<>();
                    pList.add(new PointD(median, v - width * 0.5));
                    pList.add(new PointD(median, v + width * 0.5));
                    PolylineShape pls = new PolylineShape();
                    pls.setPoints(pList);
                    gc.add(new Graphic(pls, medianBreak));
                } else {
                    PointShape ps = new PointShape();
                    ps.setPoint(new PointD(median, v));
                    gc.add(new Graphic(ps, medianBreak));
                }
            }

            //Add low whisker line
            double min = Math.max(mino, mind);
            pList = new ArrayList<>();
            pList.add(new PointD(q1, v));
            pList.add(new PointD(min, v));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            gc.add(new Graphic(pls, whiskerBreak));
            //Add cap
            if (showcaps) {
                pList = new ArrayList<>();
                pList.add(new PointD(min, v - width * 0.25));
                pList.add(new PointD(min, v + width * 0.25));
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
                            ps.setPoint(new PointD(a.getDouble(j), v));
                            gc.add(new Graphic(ps, flierBreak));
                        }
                    }
                }
            }

            //Add high whisker line
            double max = Math.min(maxo, maxd);
            pList = new ArrayList<>();
            pList.add(new PointD(q3, v));
            pList.add(new PointD(max, v));
            pls = new PolylineShape();
            pls.setPoints(pList);
            gc.add(new Graphic(pls, whiskerBreak));
            //Add cap
            if (showcaps) {
                pList = new ArrayList<>();
                pList.add(new PointD(max, v - width * 0.25));
                pList.add(new PointD(max, v + width * 0.25));
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
                            ps.setPoint(new PointD(a.getDouble(j), v));
                            gc.add(new Graphic(ps, flierBreak));
                        }
                    }
                }
            }

            //Add mean line
            if (showmeans) {
                double mean = ArrayMath.mean(a);
                if (meanBreak.getBreakType() == BreakTypes.POINT_BREAK) {
                    PointShape ps = new PointShape();
                    ps.setPoint(new PointD(mean, v));
                    gc.add(new Graphic(ps, meanBreak));
                } else {
                    pList = new ArrayList<>();
                    pList.add(new PointD(mean, v - width * 0.5));
                    pList.add(new PointD(mean, v + width * 0.5));
                    pls = new PolylineShape();
                    pls.setPoints(pList);
                    gc.add(new Graphic(pls, meanBreak));
                }
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

    /**
     * Convert graphics from polar to cartesian coordinate
     *
     * @param graphics Graphics
     */
    public static void polarToCartesian(GraphicCollection graphics, double bottom) {
        for (int m = 0; m < graphics.getNumGraphics(); m++) {
            Graphic graphic = graphics.get(m);
            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                Shape shape = gg.getShape();
                List<PointD> points = new ArrayList<>();
                for (PointD p : shape.getPoints()) {
                    double[] xy = MIMath.polarToCartesian(p.X, p.Y + bottom);
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

    /**
     * Create surface graphic
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param ls Legend scheme
     * @return Surface graphic
     */
    public static MeshGraphic surface(Array xa, Array ya, Array za, LegendScheme ls) {
        return surface(xa, ya, za, za, ls);
    }

    /**
     * Create surface graphic
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param va Data array
     * @param ls Legend scheme
     * @return Surface graphic
     */
    public static MeshGraphic surface(Array xa, Array ya, Array za, Array va, LegendScheme ls) {
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();
        va = va.copyIfView();

        MeshGraphic surfaceGraphic = new MeshGraphic();
        int[] shape = xa.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        int idx;
        float[] vertexPosition = new float[rowNum * colNum * 3];
        float[] vertexValue = new float[rowNum * colNum];
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                idx = i * colNum + j;
                vertexValue[idx] = va.getFloat(idx);
                vertexPosition[idx * 3] = xa.getFloat(idx);
                vertexPosition[idx * 3 + 1] = ya.getFloat(idx);
                vertexPosition[idx * 3 + 2] = za.getFloat(idx);
            }
        }
        surfaceGraphic.setVertexPosition(vertexPosition, rowNum);
        surfaceGraphic.setVertexValue(vertexValue);
        surfaceGraphic.setLegendScheme(ls);
        return surfaceGraphic;
    }

    /**
     * Create surface graphic
     *
     * @param layer         Image layer
     * @param offset        Offset of z axis
     * @param xShift        X shift - to shift the graphics in x direction, normally
     *                      for map in 180 - 360 degree east
     * @param nLon Number of longitude
     * @param nLat Number of latitude
     * @return Graphics
     * @throws IOException
     */
    public static MeshGraphic geoSurface(ImageLayer layer, double offset, double xShift,
                                      int nLon, int nLat) throws IOException {
        Extent extent = layer.getExtent();
        Array lon = ArrayUtil.lineSpace(extent.minX + xShift, extent.maxX + xShift, nLon + 1, true);
        Array lat = ArrayUtil.lineSpace(extent.minY, extent.maxY, nLat + 1, true);
        lat = lat.flip(0).copy();
        Array[] lonlat = ArrayUtil.meshgrid(lon, lat);
        lon = lonlat[0];
        lat = lonlat[1];
        Array alt = ArrayUtil.zeros(lon.getShape(), DataType.FLOAT);
        alt = ArrayMath.add(alt, offset);
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, Color.cyan, 1);
        ((PolygonBreak) ls.getLegendBreak(0)).setDrawOutline(false);
        ((PolygonBreak) ls.getLegendBreak(0)).setOutlineColor(Color.white);

        MeshGraphic graphic = GraphicFactory.surface(lon, lat, alt, ls);
        graphic.setImage(layer.getImage());

        return graphic;
    }

    /**
     * Create surface graphic
     *
     * @param layer         Image layer
     * @param offset        Offset of z axis
     * @param xShift        X shift - to shift the graphics in x direction, normally
     *                      for map in 180 - 360 degree east
     * @param nLon Number of longitude
     * @param nLat Number of latitude
     * @param toProj Destination projection
     * @return Graphics
     * @throws IOException
     */
    public static MeshGraphic geoSurface(ImageLayer layer, double offset, double xShift,
                                         int nLon, int nLat, ProjectionInfo toProj) throws IOException {
        Extent layerExtent = layer.getExtent();
        Extent extent = (Extent) layerExtent.clone();
        double width = extent.getWidth();
        double height = extent.getHeight();
        float cutoff = toProj.getCutoff();
        double cLon = toProj.getCenterLon();
        boolean extentChanged = false;
        switch (toProj.getProjectionName()) {
            case Lambert_Conformal_Conic:
                if (extent.minY < cutoff) {
                    extent.minY = cutoff;
                    extentChanged = true;
                }
                break;
            case North_Polar_Stereographic_Azimuthal:
                if (extent.minY < cutoff) {
                    extent.minY = cutoff;
                    extentChanged = true;
                }
                break;
            case South_Polar_Stereographic_Azimuthal:
                if (extent.maxY > cutoff) {
                    extent.maxY = cutoff;
                    extentChanged = true;
                }
                break;
            case Mercator:
                if (extent.maxY > cutoff) {
                    extent.maxY = cutoff;
                    extentChanged = true;
                }
                if (extent.minY < -cutoff) {
                    extent.minY = -cutoff;
                    extentChanged = true;
                }
                break;
        }

        if (cLon != 0) {
            extent.minX = cLon - 180 + 0.1;
            extent.maxX = cLon + 180 - 0.1;
            extentChanged = true;
        }

        BufferedImage image = layer.getImage();
        if (extentChanged) {
            int x = (int) ((extent.minX - layerExtent.minX) / width * image.getWidth());
            int y = (int) ((layerExtent.maxY - extent.maxY) / height * image.getHeight());
            int w = (int) (extent.getWidth() / layerExtent.getWidth() * image.getWidth());
            int h = (int) (extent.getHeight() / layerExtent.getHeight() * image.getHeight());
            BufferedImage nImage = new BufferedImage(w, h, image.getType());
            if (cLon == 0) {
                int[] rgb = new int[w * h];
                rgb = image.getRGB(x, y, w, h, rgb, 0, w);
                nImage.setRGB(0, 0, w, h, rgb, 0, w);
            }
            else {
                if (cLon > 0) {
                    int w1 = (int) ((layerExtent.maxX - extent.minX) / extent.getWidth() * w);
                    int[] rgb1 = new int[w1 * h];
                    rgb1 = image.getRGB(x, y, w1, h, rgb1, 0, w1);
                    int[] rgb2 = new int[(w - w1) * h];
                    rgb2 = image.getRGB(0, y, w - w1, h, rgb2, 0, w - w1);
                    nImage.setRGB(0, 0, w1, h, rgb1, 0, w1);
                    nImage.setRGB(w1, 0, w - w1, h, rgb2, 0, w - w1);
                } else {
                    int w1 = (int) ((extent.maxX - layerExtent.minX) / extent.getWidth() * w);
                    int[] rgb1 = new int[w1 * h];
                    rgb1 = image.getRGB(x, y, w1, h, rgb1, 0, w1);
                    int[] rgb2 = new int[(w - w1) * h];
                    rgb2 = image.getRGB(x + w1, y, w - w1, h, rgb2, 0, w - w1);
                    nImage.setRGB(w - w1, 0, w1, h, rgb1, 0, w1);
                    nImage.setRGB(0, 0, w - w1, h, rgb2, 0, w - w1);
                }
            }
            image = nImage;
        }

        Array lon = ArrayUtil.lineSpace(extent.minX + xShift, extent.maxX + xShift, nLon + 1, true);
        Array lat = ArrayUtil.lineSpace(extent.minY, extent.maxY, nLat + 1, true);
        lat = lat.flip(0).copy();
        Array[] lonlat = ArrayUtil.meshgrid(lon, lat);
        lon = lonlat[0];
        lat = lonlat[1];
        Array alt = ArrayUtil.zeros(lon.getShape(), DataType.FLOAT);
        alt = ArrayMath.add(alt, offset);
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, Color.cyan, 1);
        ((PolygonBreak) ls.getLegendBreak(0)).setDrawOutline(false);
        ((PolygonBreak) ls.getLegendBreak(0)).setOutlineColor(Color.white);

        MeshGraphic graphic = GraphicFactory.surface(lon, lat, alt, ls);
        graphic.setImage(image);

        return graphic;
    }

    /**
     * Create surface graphic
     *
     * @param layer         Image layer
     * @param offset        Offset of z axis
     * @param xShift        X shift - to shift the graphics in x direction, normally
     *                      for map in 180 - 360 degree east
     * @param nLon Number of longitude
     * @param nLat Number of latitude
     * @param toProj Destination projection
     * @param limits Image extent limitations
     * @return Graphics
     * @throws IOException
     */
    public static MeshGraphic geoSurface(ImageLayer layer, double offset, double xShift,
                                         int nLon, int nLat, ProjectionInfo toProj, List<Number> limits) throws IOException {
        Extent layerExtent = layer.getExtent();
        Extent extent = new Extent(limits.get(0).doubleValue(), limits.get(1).doubleValue(),
                limits.get(2).doubleValue(), limits.get(3).doubleValue());
        double width = layerExtent.getWidth();
        double height = layerExtent.getHeight();

        BufferedImage image = layer.getImage();
        int x = (int) ((extent.minX - layerExtent.minX) / width * image.getWidth());
        int y = (int) ((layerExtent.maxY - extent.maxY) / height * image.getHeight());
        int w = (int)(extent.getWidth() / layerExtent.getWidth() * image.getWidth());
        int h = (int)(extent.getHeight() / layerExtent.getHeight() * image.getHeight());
        BufferedImage nImage = new BufferedImage(w, h, image.getType());
        Graphics2D g2 = nImage.createGraphics();
        g2.drawImage(image.getSubimage(x, y, w, h), 0, 0, null);
        image = nImage;

        Array lon = ArrayUtil.lineSpace(extent.minX + xShift, extent.maxX + xShift, nLon + 1, true);
        Array lat = ArrayUtil.lineSpace(extent.minY, extent.maxY, nLat + 1, true);
        lat = lat.flip(0).copy();
        Array[] lonlat = ArrayUtil.meshgrid(lon, lat);
        lon = lonlat[0];
        lat = lonlat[1];
        Array alt = ArrayUtil.zeros(lon.getShape(), DataType.FLOAT);
        alt = ArrayMath.add(alt, offset);
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, Color.cyan, 1);
        ((PolygonBreak) ls.getLegendBreak(0)).setDrawOutline(false);
        ((PolygonBreak) ls.getLegendBreak(0)).setOutlineColor(Color.white);

        MeshGraphic graphic = GraphicFactory.surface(lon, lat, alt, ls);
        graphic.setImage(image);

        return graphic;
    }

    /**
     * Create slice graphics
     *
     * @param data   Data array - 3D
     * @param xa     X coordinate array - 1D
     * @param ya     Y coordinate array - 1D
     * @param za     Z coordinate array - 1D
     * @param xSlice X slice list
     * @param ySlice Y slice list
     * @param zSlice Z slice list
     * @param ls     Legend scheme
     * @return Surface graphics
     */
    public static List<MeshGraphic> slice(Array data, Array xa, Array ya, Array za, List<Number> xSlice,
                                          List<Number> ySlice, List<Number> zSlice, LegendScheme ls) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        List<MeshGraphic> sgs = new ArrayList<>();

        int dim1, dim2;
        float x, y, z;
        int idx;

        //X slices
        dim1 = (int) za.getSize();
        dim2 = (int) ya.getSize();
        for (int s = 0; s < xSlice.size(); s++) {
            x = xSlice.get(s).floatValue();
            Array r = ArrayUtil.slice(data, 2, xa, x);
            if (r != null) {
                Index index = r.getIndex();
                MeshGraphic graphic = new MeshGraphic();
                float[] vertexPosition = new float[dim1 * dim2 * 3];
                float[] vertexValue = new float[dim1 * dim2];
                for (int i = 0; i < dim1; i++) {
                    z = za.getFloat(i);
                    for (int j = 0; j < dim2; j++) {
                        y = ya.getFloat(j);
                        idx = i * dim2 +j;
                        vertexValue[idx] = r.getFloat(index.set(i, j));
                        idx = idx * 3;
                        vertexPosition[idx] = x;
                        vertexPosition[idx+1] = y;
                        vertexPosition[idx+2] = z;
                    }
                }
                graphic.setVertexPosition(vertexPosition, dim1);
                graphic.setVertexValue(vertexValue);
                graphic.setLegendScheme(ls);
                sgs.add(graphic);
            }
        }

        //Y slices
        dim1 = (int) za.getSize();
        dim2 = (int) xa.getSize();
        for (int s = 0; s < ySlice.size(); s++) {
            y = ySlice.get(s).floatValue();
            Array r = ArrayUtil.slice(data, 1, ya, y);
            if (r != null) {
                Index index = r.getIndex();
                MeshGraphic graphic = new MeshGraphic();
                float[] vertexPosition = new float[dim1 * dim2 * 3];
                float[] vertexValue = new float[dim1 * dim2];
                for (int i = 0; i < dim1; i++) {
                    z = za.getFloat(i);
                    for (int j = 0; j < dim2; j++) {
                        x = xa.getFloat(j);
                        idx = i * dim2 +j;
                        vertexValue[idx] = r.getFloat(index.set(i, j));
                        idx = idx * 3;
                        vertexPosition[idx] = x;
                        vertexPosition[idx+1] = y;
                        vertexPosition[idx+2] = z;
                    }
                }
                graphic.setVertexPosition(vertexPosition, dim1);
                graphic.setVertexValue(vertexValue);
                graphic.setLegendScheme(ls);
                sgs.add(graphic);
            }
        }

        //Z slices
        dim1 = (int) ya.getSize();
        dim2 = (int) xa.getSize();
        for (int s = 0; s < zSlice.size(); s++) {
            z = zSlice.get(s).floatValue();
            Array r = ArrayUtil.slice(data, 0, za, z);
            if (r != null) {
                Index index = r.getIndex();
                MeshGraphic graphic = new MeshGraphic();
                float[] vertexPosition = new float[dim1 * dim2 * 3];
                float[] vertexValue = new float[dim1 * dim2];
                for (int i = 0; i < dim1; i++) {
                    y = ya.getFloat(i);
                    for (int j = 0; j < dim2; j++) {
                        x = xa.getFloat(j);
                        idx = i * dim2 +j;
                        vertexValue[idx] = r.getFloat(index.set(i, j));
                        idx = idx * 3;
                        vertexPosition[idx] = x;
                        vertexPosition[idx+1] = y;
                        vertexPosition[idx+2] = z;
                    }
                }
                graphic.setVertexPosition(vertexPosition, dim1);
                graphic.setVertexValue(vertexValue);
                graphic.setLegendScheme(ls);
                sgs.add(graphic);
            }
        }

        return sgs;
    }

    /**
     * Create slice graphics
     *
     * @param data   Data array - 3D
     * @param xa     X coordinate array - 1D
     * @param ya     Y coordinate array - 1D
     * @param za     Z coordinate array - 1D
     * @param xSlice X slice list
     * @param ySlice Y slice list
     * @param zSlice Z slice list
     * @param transferFunction Transfer function
     * @return Surface graphics
     */
    public static List<MeshGraphic> slice(Array data, Array xa, Array ya, Array za, List<Number> xSlice,
                                          List<Number> ySlice, List<Number> zSlice, TransferFunction transferFunction) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        List<MeshGraphic> sgs = new ArrayList<>();

        int dim1, dim2;
        float x, y, z;
        int idx;

        //X slices
        dim1 = (int) za.getSize();
        dim2 = (int) ya.getSize();
        for (int s = 0; s < xSlice.size(); s++) {
            x = xSlice.get(s).floatValue();
            Array r = ArrayUtil.slice(data, 2, xa, x);
            if (r != null) {
                Index index = r.getIndex();
                MeshGraphic graphic = new MeshGraphic();
                float[] vertexPosition = new float[dim1 * dim2 * 3];
                float[] vertexValue = new float[dim1 * dim2];
                for (int i = 0; i < dim1; i++) {
                    z = za.getFloat(i);
                    for (int j = 0; j < dim2; j++) {
                        y = ya.getFloat(j);
                        idx = i * dim2 +j;
                        vertexValue[idx] = r.getFloat(index.set(i, j));
                        idx = idx * 3;
                        vertexPosition[idx] = x;
                        vertexPosition[idx+1] = y;
                        vertexPosition[idx+2] = z;
                    }
                }
                graphic.setVertexPosition(vertexPosition, dim1);
                graphic.setVertexValue(vertexValue);
                graphic.setTransferFunction(transferFunction);
                sgs.add(graphic);
            }
        }

        //Y slices
        dim1 = (int) za.getSize();
        dim2 = (int) xa.getSize();
        for (int s = 0; s < ySlice.size(); s++) {
            y = ySlice.get(s).floatValue();
            Array r = ArrayUtil.slice(data, 1, ya, y);
            if (r != null) {
                Index index = r.getIndex();
                MeshGraphic graphic = new MeshGraphic();
                float[] vertexPosition = new float[dim1 * dim2 * 3];
                float[] vertexValue = new float[dim1 * dim2];
                for (int i = 0; i < dim1; i++) {
                    z = za.getFloat(i);
                    for (int j = 0; j < dim2; j++) {
                        x = xa.getFloat(j);
                        idx = i * dim2 +j;
                        vertexValue[idx] = r.getFloat(index.set(i, j));
                        idx = idx * 3;
                        vertexPosition[idx] = x;
                        vertexPosition[idx+1] = y;
                        vertexPosition[idx+2] = z;
                    }
                }
                graphic.setVertexPosition(vertexPosition, dim1);
                graphic.setVertexValue(vertexValue);
                graphic.setTransferFunction(transferFunction);
                sgs.add(graphic);
            }
        }

        //Z slices
        dim1 = (int) ya.getSize();
        dim2 = (int) xa.getSize();
        for (int s = 0; s < zSlice.size(); s++) {
            z = zSlice.get(s).floatValue();
            Array r = ArrayUtil.slice(data, 0, za, z);
            if (r != null) {
                Index index = r.getIndex();
                MeshGraphic graphic = new MeshGraphic();
                float[] vertexPosition = new float[dim1 * dim2 * 3];
                float[] vertexValue = new float[dim1 * dim2];
                for (int i = 0; i < dim1; i++) {
                    y = ya.getFloat(i);
                    for (int j = 0; j < dim2; j++) {
                        x = xa.getFloat(j);
                        idx = i * dim2 +j;
                        vertexValue[idx] = r.getFloat(index.set(i, j));
                        idx = idx * 3;
                        vertexPosition[idx] = x;
                        vertexPosition[idx+1] = y;
                        vertexPosition[idx+2] = z;
                    }
                }
                graphic.setVertexPosition(vertexPosition, dim1);
                graphic.setVertexValue(vertexValue);
                graphic.setTransferFunction(transferFunction);
                sgs.add(graphic);
            }
        }

        return sgs;
    }

    /**
     * Create xy slice graphics
     *
     * @param data   Data array - 3D
     * @param xa     X coordinate array - 1D
     * @param ya     Y coordinate array - 1D
     * @param za     Z coordinate array - 1D
     * @param xySlice XY slice list - [x1,y1,x2,y2]
     * @param ls     Legend scheme
     * @param method Interpolation method - nearest or linear
     * @return Surface graphics
     */
    public static MeshGraphic slice(Array data, Array xa, Array ya, Array za, List<Number> xySlice,
                                    LegendScheme ls, InterpolationMethod method) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        Array[] rxy = InterpUtil.sliceXY(xa, ya, za, data, xySlice, method);
        Array r = rxy[0];
        Array x2d = rxy[4];
        Array y2d = rxy[5];
        Array z2d = rxy[6];
        MeshGraphic graphic = new MeshGraphic();
        int[] shape = r.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        int idx;
        float[] vertexPosition = new float[rowNum * colNum * 3];
        float[] vertexValue = new float[rowNum * colNum];
        for (int i = 0; i < vertexValue.length; i++) {
            idx = i * 3;
            vertexPosition[idx] = x2d.getFloat(i);
            vertexPosition[idx+1] = y2d.getFloat(i);
            vertexPosition[idx+2] = z2d.getFloat(i);
            vertexValue[i] = r.getFloat(i);
        }

        graphic.setVertexPosition(vertexPosition, rowNum);
        graphic.setVertexValue(vertexValue);
        graphic.setLegendScheme(ls);

        return graphic;
    }

    /**
     * Create slice graphics
     *
     * @param data   Data array - 3D
     * @param xa     X coordinate array - 1D
     * @param ya     Y coordinate array - 1D
     * @param za     Z coordinate array - 1D
     * @param xSlice X slice list
     * @param ySlice Y slice list
     * @param zSlice Z slice list
     * @param ls     Legend scheme
     * @return Surface graphics
     */
    public static List<MeshGraphic> slice(Array data, Array xa, Array ya, Array za, Array xSlice,
                                          Array ySlice, Array zSlice, LegendScheme ls) throws InvalidRangeException {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();
        xSlice = xSlice.copyIfView();
        ySlice = ySlice.copyIfView();
        zSlice = zSlice.copyIfView();

        List<MeshGraphic> sgs = new ArrayList<>();

        RectNearestInterpolator3D interpolator3D = new RectNearestInterpolator3D(xa, ya, za, data);
        Array r = interpolator3D.interpolate(xSlice, ySlice, zSlice);
        MeshGraphic graphic = new MeshGraphic();
        int[] shape = r.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        int idx;
        float[] vertexPosition = new float[rowNum * colNum * 3];
        float[] vertexValue = new float[rowNum * colNum];
        for (int i = 0; i < vertexValue.length; i++) {
            idx = i * 3;
            vertexPosition[idx] = xSlice.getFloat(i);
            vertexPosition[idx+1] = ySlice.getFloat(i);
            vertexPosition[idx+2] = zSlice.getFloat(i);
            vertexValue[i] = r.getFloat(i);
        }

        graphic.setVertexPosition(vertexPosition, rowNum);
        graphic.setVertexValue(vertexValue);
        graphic.setLegendScheme(ls);
        sgs.add(graphic);

        return sgs;
    }

    /**
     * Create isosurface graphics
     *
     * @param data     3d data array
     * @param x        X coordinates
     * @param y        Y coordinates
     * @param z        Z coordinates
     * @param isoLevel iso level
     * @param pb       Polygon break
     * @return Graphics
     */
    public static TriMeshGraphic isosurface(Array data, Array x, Array y, Array z,
                                            float isoLevel, PolygonBreak pb) {
        x = x.copyIfView();
        y = y.copyIfView();
        z = z.copyIfView();
        data = data.copyIfView();

        List<float[]> vertices = MarchingCubes.marchingCubes(data, x, y, z, isoLevel);

        int nVertex = vertices.size();
        float[] vertexData = new float[nVertex * 3];
        int pos = 0;
        for (int i = 0; i < vertices.size(); i++) {
            System.arraycopy(vertices.get(i), 0, vertexData, pos, 3);
            pos += 3;
        }

        TriMeshGraphic meshGraphic = new TriMeshGraphic();
        meshGraphic.setLegendBreak(pb);
        meshGraphic.setVertexData(vertexData);

        return meshGraphic;
    }

    /**
     * Create isosurface graphics
     *
     * @param data     3d data array
     * @param x        X coordinates
     * @param y        Y coordinates
     * @param z        Z coordinates
     * @param isoLevel iso level
     * @param pb       Polygon break
     * @param nThreads Thread number
     * @return Graphics
     */
    public static TriMeshGraphic isosurface(final Array data, final Array x, final Array y, final Array z,
                                            final float isoLevel, PolygonBreak pb, int nThreads) {
        // TIMER
        ArrayList<Thread> threads = new ArrayList<>();
        final ArrayList<ArrayList<float[]>> results = new ArrayList<>();

        // Thread work distribution
        int nz = (int) z.getSize();
        int remainder = nz % nThreads;
        int segment = nz / nThreads;

        // Z axis offset for vertice position calculation
        int zAxisOffset = 0;

        for (int i = 0; i < nThreads; i++) {
            // Distribute remainder among first (remainder) threads
            int segmentSize = (remainder-- > 0) ? segment + 1 : segment;

            // Padding needs to be added to correctly close the gaps between segments
            final int paddedSegmentSize = (i != nThreads - 1) ? segmentSize + 1 : segmentSize;

            // Finished callback
            final CallbackMC callback = new CallbackMC() {
                @Override
                public void run() {
                    results.add(getVertices());
                }
            };

            // Java...
            final int finalZAxisOffset = zAxisOffset;

            // Start the thread
            Thread t = new Thread() {
                public void run() {
                    MarchingCubes.marchingCubes(data, x, y, z, isoLevel, paddedSegmentSize, finalZAxisOffset, callback);
                }
            };

            threads.add(t);
            t.start();

            // Correct offsets for next iteration
            zAxisOffset += segmentSize;
        }

        // Join the threads
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int nVertex = 0;
        for (List<float[]> vertices : results) {
            nVertex += vertices.size();
        }
        float[] vertexData = new float[nVertex * 3];
        int pos = 0;
        for (List<float[]> vertices : results) {
            for (int i = 0; i < vertices.size(); i++) {
                System.arraycopy(vertices.get(i), 0, vertexData, pos, 3);
                pos += 3;
            }
        }

        TriMeshGraphic meshGraphic = new TriMeshGraphic();
        meshGraphic.setLegendBreak(pb);
        meshGraphic.setVertexData(vertexData);

        return meshGraphic;
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param colorMap ColorMap
     * @param vMin Min value
     * @param vMax Max value
     * @param alphaMin Min alpha
     * @param alphaMax Max alpha
     * @return Particles
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, ColorMap colorMap,
                                           double vMin, double vMax, float alphaMin, float alphaMax) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        VolumeGraphic graphics = new VolumeGraphic(data, colorMap, vMin, vMax);
        graphics.setAlphaMin(alphaMin);
        graphics.setAlphaMax(alphaMax);
        graphics.updateColors();

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);

        return graphics;
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param colorMap ColorMap
     * @param alphaMin Min alpha
     * @param alphaMax Max alpha
     * @return Particles
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, ColorMap colorMap,
                                           Normalize norm, float alphaMin, float alphaMax) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        VolumeGraphic graphics = new VolumeGraphic(data, colorMap, norm);
        graphics.setAlphaMin(alphaMin);
        graphics.setAlphaMax(alphaMax);
        graphics.updateColors();

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);

        return graphics;
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param colorMap ColorMap
     * @param norm Normalize
     * @param opacityNodes Opacity nodes
     * @param opacityLevels Opacity levels
     * @return Volume graphics
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, ColorMap colorMap,
                                           Normalize norm, List<Number> opacityNodes, List<Number> opacityLevels) {
        OpacityTransferFunction opacityTransferFunction = new OpacityTransferFunction(opacityNodes, opacityLevels, norm);
        return volume(data, xa, ya, za, colorMap, norm, opacityTransferFunction);
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param transferFunction Transfer function
     * @return Volume graphics
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za,
                                           TransferFunction transferFunction) {
        return volume(data, xa, ya, za, transferFunction.getColorMap(), transferFunction.getNormalize(),
                transferFunction.getOpacityTransferFunction());
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param colorMap ColorMap
     * @param norm Normalize
     * @param opacityTransferFunction Opacity transfer function
     * @return Volume graphics
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, ColorMap colorMap,
                                           Normalize norm, OpacityTransferFunction opacityTransferFunction) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        VolumeGraphic graphics = new VolumeGraphic(data, colorMap, norm);
        graphics.setOpacityTransferFunction(opacityTransferFunction);
        graphics.updateColors();

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);

        return graphics;
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param ls       LegendScheme
     * @param alphaMin Min alpha
     * @param alphaMax Max alpha
     * @return Particles
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, LegendScheme ls,
                                           float alphaMin, float alphaMax) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        VolumeGraphic graphics = new VolumeGraphic(data, ls);
        graphics.setAlphaMin(alphaMin);
        graphics.setAlphaMax(alphaMax);
        graphics.updateColors();

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);

        return graphics;
    }

}
