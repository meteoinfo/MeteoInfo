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
package org.meteoinfo.geo.meteodata;

import org.apache.commons.lang3.ArrayUtils;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointD;
import org.meteoinfo.data.*;
import org.meteoinfo.data.meteodata.*;
import org.meteoinfo.dataframe.DataFrame;
import org.meteoinfo.geo.drawing.ContourDraw;
import org.meteoinfo.geo.drawing.Draw;
import org.meteoinfo.geo.layer.*;
import org.meteoinfo.geo.legend.LegendManage;
import org.meteoinfo.geometry.geoprocess.GeometryUtil;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.math.meteo.MeteoMath;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.table.Field;
import wcontour.Contour;
import wcontour.global.PolyLine;
import wcontour.global.Polygon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class DrawMeteoData {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Create a polyline layer
     *
     * @param data XYListDataset
     * @param ls Legend scheme
     * @param layerName Layer name
     * @param fieldName Field name
     * @return Polyline layer
     */
    public static VectorLayer createPolylineLayer(XYListDataset data, LegendScheme ls,
                                                  String layerName, String fieldName) {
        VectorLayer layer = new VectorLayer(ShapeTypes.POLYLINE);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        layer.editAddField(aDC);
        for (int i = 0; i < data.getSeriesCount(); i++) {
            double[] xd = data.getXValues(i);
            double[] yd = data.getYValues(i);
            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (int j = 0; j < xd.length; j++) {
                aPoint = new PointD();
                aPoint.X = xd[j];
                aPoint.Y = yd[j];
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(i);
            aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));

            int shapeNum = layer.getShapeNum();
            try {
                if (layer.editInsertShape(aPolyline, shapeNum)) {
                    layer.editCellValue(fieldName, shapeNum, i);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        layer.setLayerName(layerName);
        ls.setFieldName(fieldName);
        layer.setLegendScheme(ls);

        return layer;
    }

    /**
     * Create a polyline layer
     *
     * @param xdata X array list
     * @param ydata Y array list
     * @param ls Legend scheme
     * @param layerName Layer name
     * @param fieldName Field name
     * @return Polyline layer
     */
    public static VectorLayer createPolylineLayer(List<Array> xdata, List<Array> ydata, LegendScheme ls,
                                                  String layerName, String fieldName) {
        VectorLayer layer = new VectorLayer(ShapeTypes.POLYLINE);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        layer.editAddField(aDC);
        for (int i = 0; i < xdata.size(); i++) {
            Array xd = xdata.get(i);
            Array yd = ydata.get(i);
            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            IndexIterator xIter = xd.getIndexIterator();
            IndexIterator yIter = yd.getIndexIterator();
            while (xIter.hasNext()){
                aPoint = new PointD();
                aPoint.X = xIter.getDoubleNext();
                aPoint.Y = yIter.getDoubleNext();
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(i);
            aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));

            int shapeNum = layer.getShapeNum();
            try {
                if (layer.editInsertShape(aPolyline, shapeNum)) {
                    layer.editCellValue(fieldName, shapeNum, i);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        layer.setLayerName(layerName);
        ls.setFieldName(fieldName);
        layer.setLegendScheme(ls);

        return layer;
    }

    /**
     * Create a polyline layer
     *
     * @param data XYListDataset
     * @param ls Legend scheme
     * @param layerName Layer name
     * @param fieldName Field name
     * @param westLon West border longitude - split polyline if the points cross
     * it
     * @param eastLon East border longitude - split polyline if the points cross
     * it
     * @return Polyline layer
     */
    public static VectorLayer createPolylineLayer(XYListDataset data, LegendScheme ls,
                                                  String layerName, String fieldName, double westLon, double eastLon) {
        VectorLayer layer = new VectorLayer(ShapeTypes.POLYLINE);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        layer.editAddField(aDC);
        for (int i = 0; i < data.getSeriesCount(); i++) {
            double[] xd = data.getXValues(i);
            double[] yd = data.getYValues(i);
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            List<List<PointD>> ppList = new ArrayList<>();
            double preLon = 0;
            for (int j = 0; j < xd.length; j++) {
                aPoint = new PointD();
                aPoint.X = xd[j];
                aPoint.Y = yd[j];
                if (j == 0) {
                    preLon = xd[j];
                    pList.add(aPoint);
                } else {
                    if (Math.abs(aPoint.X - preLon) > 350) {
                        if (aPoint.X > preLon) {
                            pList.add(new PointD(westLon, aPoint.Y));
                        } else {
                            pList.add(new PointD(eastLon, aPoint.Y));
                        }
                        if (pList.size() > 1) {
                            ppList.add(new ArrayList<>(pList));
                        }
                        pList.clear();
                        pList.add(aPoint);
                    } else {
                        pList.add(aPoint);
                    }
                    preLon = xd[j];
                }
            }
            if (pList.size() > 1) {
                ppList.add(pList);
            }
            for (List<PointD> ps : ppList) {
                PolylineShape aPolyline = new PolylineShape();
                aPolyline.setPoints(ps);
                aPolyline.setValue(i);
                aPolyline.setExtent(GeometryUtil.getPointsExtent(ps));

                int shapeNum = layer.getShapeNum();
                try {
                    if (layer.editInsertShape(aPolyline, shapeNum)) {
                        layer.editCellValue(fieldName, shapeNum, i);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        layer.setLayerName(layerName);
        ls.setFieldName(fieldName);
        layer.setLegendScheme(ls);

        return layer;
    }

    /**
     * Create a polyline layer
     *
     * @param xdata X array list
     * @param ydata Y array list
     * @param ls Legend scheme
     * @param layerName Layer name
     * @param fieldName Field name
     * @param westLon West border longitude - split polyline if the points cross
     * it
     * @param eastLon East border longitude - split polyline if the points cross
     * it
     * @return Polyline layer
     */
    public static VectorLayer createPolylineLayer(List<Array> xdata, List<Array> ydata, LegendScheme ls,
                                                  String layerName, String fieldName, double westLon, double eastLon) {
        VectorLayer layer = new VectorLayer(ShapeTypes.POLYLINE);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        layer.editAddField(aDC);
        for (int i = 0; i < xdata.size(); i++) {
            Array xd = xdata.get(i);
            Array yd = ydata.get(i);
            PointD aPoint;
            List<PointD> pList;
            List<List<PointD>> ppList = new ArrayList<>();
            double preLon;
            IndexIterator xIter = xd.getIndexIterator();
            IndexIterator yIter = yd.getIndexIterator();
            if (xd.getRank() == 1) {
                pList = new ArrayList<>();
                preLon = 0;
                int j = 0;
                while (xIter.hasNext()){
                    aPoint = new PointD();
                    aPoint.X = xIter.getDoubleNext();
                    aPoint.Y = yIter.getDoubleNext();
                    if (j == 0) {
                        preLon = aPoint.X;
                        pList.add(aPoint);
                    } else {
                        if (Double.isNaN(aPoint.X)) {
                            if (pList.size() > 1) {
                                ppList.add(new ArrayList<>(pList));
                            }
                            pList.clear();
                        } else if (Math.abs(aPoint.X - preLon) > 350) {
                            if (aPoint.X > preLon) {
                                pList.add(new PointD(westLon, aPoint.Y));
                            } else {
                                pList.add(new PointD(eastLon, aPoint.Y));
                            }
                            if (pList.size() > 1) {
                                ppList.add(new ArrayList<>(pList));
                            }
                            pList.clear();
                            pList.add(aPoint);
                        } else {
                            pList.add(aPoint);
                        }
                        preLon = aPoint.X;
                    }
                    j++;
                }
                if (pList.size() > 1) {
                    ppList.add(pList);
                }
            } else {    //Two dimensions
                int[] shape = xd.getShape();
                int ny = shape[0];
                int nx = shape[1];
                for (int k = 0; k < ny; k++) {
                    pList = new ArrayList<>();
                    preLon = 0;
                    for (int j = 0; j < nx; j++) {
                        aPoint = new PointD();
                        aPoint.X = xIter.getDoubleNext();
                        aPoint.Y = yIter.getDoubleNext();
                        //aPoint.X = xd.getDouble(k * nx + j);
                        //aPoint.Y = yd.getDouble(k * nx + j);
                        if (j == 0) {
                            preLon = aPoint.X;
                            pList.add(aPoint);
                        } else {
                            if (Double.isNaN(aPoint.X)) {
                                if (pList.size() > 1) {
                                    ppList.add(new ArrayList<>(pList));
                                }
                                pList.clear();
                            } else if (Math.abs(aPoint.X - preLon) > 350) {
                                if (aPoint.X > preLon) {
                                    pList.add(new PointD(westLon, aPoint.Y));
                                } else {
                                    pList.add(new PointD(eastLon, aPoint.Y));
                                }
                                if (pList.size() > 1) {
                                    ppList.add(new ArrayList<>(pList));
                                }
                                pList.clear();
                                pList.add(aPoint);
                            } else {
                                pList.add(aPoint);
                            }
                            preLon = aPoint.X;
                        }
                    }
                    if (pList.size() > 1) {
                        ppList.add(pList);
                    }
                }
            }
            int k = 0;
            for (List<PointD> ps : ppList) {
                PolylineShape aPolyline = new PolylineShape();
                aPolyline.setPoints(ps);
                aPolyline.setValue(k);
                aPolyline.setExtent(GeometryUtil.getPointsExtent(ps));

                int shapeNum = layer.getShapeNum();
                try {
                    if (layer.editInsertShape(aPolyline, shapeNum)) {
                        layer.editCellValue(fieldName, shapeNum, k);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
                k += 1;
            }
        }

        layer.setLayerName(layerName);
        ls.setFieldName(fieldName);
        layer.setLegendScheme(ls);

        return layer;
    }

    /**
     * Create a polyline z layer
     *
     * @param xdata X array
     * @param ydata Y array
     * @param zdata Z array
     * @param ls Legend scheme
     * @param layerName Layer name
     * @param fieldName Field name
     * @param westLon West border longitude - split polyline if the points cross
     * it
     * @param eastLon East border longitude - split polyline if the points cross
     * it
     * @return PolylineZ layer
     */
    public static VectorLayer createPolylineLayer(Array xdata, Array ydata, Array zdata, LegendScheme ls,
                                                  String layerName, String fieldName, double westLon, double eastLon) {
        VectorLayer layer = new VectorLayer(ShapeTypes.POLYLINE_Z);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        layer.editAddField(aDC);

        PointZ aPoint;
        List<PointZ> pList;
        List<List<PointZ>> ppList = new ArrayList<>();
        double preLon;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        if (xdata.getRank() == 1) {
            pList = new ArrayList<>();
            preLon = 0;
            int j = 0;
            while (xIter.hasNext()){
                aPoint = new PointZ();
                aPoint.X = xIter.getDoubleNext();
                aPoint.Y = yIter.getDoubleNext();
                aPoint.Z = zIter.getDoubleNext();
                if (j == 0) {
                    preLon = aPoint.X;
                    pList.add(aPoint);
                } else {
                    if (Double.isNaN(aPoint.X)) {
                        if (pList.size() > 1) {
                            ppList.add(new ArrayList<>(pList));
                        }
                        pList.clear();
                    } else if (Math.abs(aPoint.X - preLon) > 350) {
                        if (aPoint.X > preLon) {
                            pList.add(new PointZ(westLon, aPoint.Y, aPoint.Z));
                        } else {
                            pList.add(new PointZ(eastLon, aPoint.Y, aPoint.Z));
                        }
                        if (pList.size() > 1) {
                            ppList.add(new ArrayList<>(pList));
                        }
                        pList.clear();
                        pList.add(aPoint);
                    } else {
                        pList.add(aPoint);
                    }
                    preLon = aPoint.X;
                }
                j++;
            }

            if (pList.size() > 1) {
                ppList.add(pList);
            }
        } else {    //Two dimensions
            int[] shape = xdata.getShape();
            int ny = shape[0];
            int nx = shape[1];
            for (int i = 0; i < ny; i++) {
                pList = new ArrayList<>();
                preLon = 0;
                for (int j = 0; j < nx; j++) {
                    aPoint = new PointZ();
                    aPoint.X = xIter.getDoubleNext();
                    aPoint.Y = yIter.getDoubleNext();
                    aPoint.Z = zIter.getDoubleNext();
                    //aPoint.X = xdata.getDouble(i * nx + j);
                    //aPoint.Y = ydata.getDouble(i * nx + j);
                    //aPoint.Z = zdata.getDouble(i * nx + j);
                    if (j == 0) {
                        preLon = aPoint.X;
                        pList.add(aPoint);
                    } else {
                        if (Double.isNaN(aPoint.X)) {
                            if (pList.size() > 1) {
                                ppList.add(new ArrayList<>(pList));
                            }
                            pList.clear();
                        } else if (Math.abs(aPoint.X - preLon) > 350) {
                            if (aPoint.X > preLon) {
                                pList.add(new PointZ(westLon, aPoint.Y, aPoint.Z));
                            } else {
                                pList.add(new PointZ(eastLon, aPoint.Y, aPoint.Z));
                            }
                            if (pList.size() > 1) {
                                ppList.add(new ArrayList<>(pList));
                            }
                            pList.clear();
                            pList.add(aPoint);
                        } else {
                            pList.add(aPoint);
                        }
                        preLon = aPoint.X;
                    }
                }

                if (pList.size() > 1) {
                    ppList.add(pList);
                }
            }
        }

        for (List<PointZ> ps : ppList) {
            PolylineZShape aPolyline = new PolylineZShape();
            aPolyline.setPoints(ps);
            aPolyline.setValue(0);
            aPolyline.setExtent(GeometryUtil.getPointsExtent(ps));

            int shapeNum = layer.getShapeNum();
            try {
                if (layer.editInsertShape(aPolyline, shapeNum)) {
                    layer.editCellValue(fieldName, shapeNum, 0);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        layer.setLayerName(layerName);
        layer.setLegendScheme(ls);

        return layer;
    }

    /**
     * Create a polyline z layer
     *
     * @param xdata X array
     * @param ydata Y array
     * @param zdata Z array
     * @param ls Legend scheme
     * @param layerName Layer name
     * @param fieldName Field name
     * @return PolylineZ layer
     */
    public static VectorLayer createPolylineLayer(Array xdata, Array ydata, Array zdata, LegendScheme ls,
                                                  String layerName, String fieldName) {
        VectorLayer layer = new VectorLayer(ShapeTypes.POLYLINE_Z);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        layer.editAddField(aDC);

        PointZ aPoint;
        List<PointZ> pList;
        List<List<PointZ>> ppList = new ArrayList<>();
        double preLon;
        IndexIterator xIter = xdata.getIndexIterator();
        IndexIterator yIter = ydata.getIndexIterator();
        IndexIterator zIter = zdata.getIndexIterator();
        if (xdata.getRank() == 1) {
            pList = new ArrayList<>();
            preLon = 0;
            int j = 0;
            while (xIter.hasNext()){
                aPoint = new PointZ();
                aPoint.X = xIter.getDoubleNext();
                aPoint.Y = yIter.getDoubleNext();
                aPoint.Z = zIter.getDoubleNext();
                if (j == 0) {
                    preLon = aPoint.X;
                    pList.add(aPoint);
                } else {
                    if (Double.isNaN(aPoint.X)) {
                        if (pList.size() > 1) {
                            ppList.add(new ArrayList<>(pList));
                        }
                        pList.clear();
                    } else {
                        pList.add(aPoint);
                    }
                    preLon = aPoint.X;
                }
                j++;
            }

            if (pList.size() > 1) {
                ppList.add(pList);
            }
        } else {    //Two dimensions
            int[] shape = xdata.getShape();
            int ny = shape[0];
            int nx = shape[1];
            for (int i = 0; i < ny; i++) {
                pList = new ArrayList<>();
                preLon = 0;
                for (int j = 0; j < nx; j++) {
                    aPoint = new PointZ();
                    aPoint.X = xIter.getDoubleNext();
                    aPoint.Y = yIter.getDoubleNext();
                    aPoint.Z = zIter.getDoubleNext();
                    //aPoint.X = xdata.getDouble(i * nx + j);
                    //aPoint.Y = ydata.getDouble(i * nx + j);
                    //aPoint.Z = zdata.getDouble(i * nx + j);
                    if (j == 0) {
                        preLon = aPoint.X;
                        pList.add(aPoint);
                    } else {
                        if (Double.isNaN(aPoint.X)) {
                            if (pList.size() > 1) {
                                ppList.add(new ArrayList<>(pList));
                            }
                            pList.clear();
                        } else {
                            pList.add(aPoint);
                        }
                        preLon = aPoint.X;
                    }
                }

                if (pList.size() > 1) {
                    ppList.add(pList);
                }
            }
        }

        for (List<PointZ> ps : ppList) {
            PolylineZShape aPolyline = new PolylineZShape();
            aPolyline.setPoints(ps);
            aPolyline.setValue(0);
            aPolyline.setExtent(GeometryUtil.getPointsExtent(ps));

            int shapeNum = layer.getShapeNum();
            try {
                if (layer.editInsertShape(aPolyline, shapeNum)) {
                    layer.editCellValue(fieldName, shapeNum, 0);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        layer.setLayerName(layerName);
        layer.setLegendScheme(ls);

        return layer;
    }

    /**
     * Create contour layer
     *
     * @param gridData Grid data
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createContourLayer(GridData gridData, String lName, String fieldName) {
        LegendScheme ls = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.UNIQUE_VALUE, ShapeTypes.POLYLINE);

        return createContourLayer(gridData, ls, lName, fieldName, true);
    }

    /**
     * Create contour layer
     *
     * @param gridData Grid data
     * @param lName Layer name
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createContourLayer(GridData gridData, String lName, String fieldName, boolean isSmooth) {
        LegendScheme ls = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.UNIQUE_VALUE, ShapeTypes.POLYLINE);

        return createContourLayer(gridData, ls, lName, fieldName, isSmooth);
    }

    /**
     * Create contour layer
     *
     * @param data Grid data array
     * @param x X array
     * @param y Y array
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createContourLayer(Array data, Array x, Array y, LegendScheme aLS, String lName, String fieldName, boolean isSmooth) {
        GridData gridData = new GridData(data, x, y);
        return createContourLayer(gridData, aLS, lName, fieldName, isSmooth);
    }

    /**
     * Create contour layer
     *
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createContourLayer(GridData gridData, LegendScheme aLS, String lName, String fieldName, boolean isSmooth) {
        LegendScheme ls = aLS.convertTo(ShapeTypes.POLYLINE);
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
            ContourLines = wcontour.Contour.smoothLines(ContourLines);
        }

        PolyLine aLine;
        double aValue;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYLINE);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        aLayer.editAddField(aDC);

        for (int i = 0; i < ContourLines.size(); i++) {
            aLine = ContourLines.get(i);
            aValue = aLine.Value;

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
            aPolyline.setValue(aValue);
            aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue(fieldName, shapeNum, aValue);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        ls.setFieldName(fieldName);
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.CONTOUR);

        aLayer.getLabelSet().setDrawLabels(true);
        aLayer.getLabelSet().setDrawShadow(true);
        aLayer.getLabelSet().setShadowColor(Color.white);
        aLayer.getLabelSet().setYOffset(3);
        aLayer.getLabelSet().setFieldName(fieldName);
        aLayer.getLabelSet().setColorByLegend(true);
        aLayer.getLabelSet().setDynamicContourLabel(true);
        //aLayer.getLabelSet().setAutoDecimal(false);
        int decimaln = MIMath.getDecimalNum(cValues[0]);
        if (cValues.length > 1) {
            int decimaln2 = MIMath.getDecimalNum(cValues[1] - cValues[0]);
            decimaln = Math.max(decimaln, decimaln2);
        }
        aLayer.getLabelSet().setDecimalDigits(decimaln);
        //aLayer.addLabels();

        return aLayer;
    }

    /**
     * Create contour layer
     *
     * @param data Data
     * @param xArray X array
     * @param aLS Legend scheme
     * @param yArray Y array
     * @param lName Layer name
     * @param missingValue Missing value
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createContourLayer(double[][] data, double[] xArray, double[] yArray, double missingValue,
                                                 LegendScheme aLS, String lName, String fieldName, boolean isSmooth) {
        LegendScheme ls = aLS.convertTo(ShapeTypes.POLYLINE);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[data.length][data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(data,
                cValues, xArray, yArray, missingValue, S1);
        List<PolyLine> ContourLines = (List<PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = wcontour.Contour.smoothLines(ContourLines);
        }

        double aValue;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYLINE);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        aLayer.editAddField(aDC);

        for (PolyLine aLine : ContourLines) {
            aValue = aLine.Value;
            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (wcontour.global.PointD p : aLine.PointList) {
                aPoint = new PointD();
                aPoint.X = p.X;
                aPoint.Y = p.Y;
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(aValue);
            aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));
            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue(fieldName, shapeNum, aValue);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        ls.setFieldName(fieldName);
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.CONTOUR);

        aLayer.getLabelSet().setDrawLabels(true);
        aLayer.getLabelSet().setDrawShadow(true);
        aLayer.getLabelSet().setShadowColor(Color.white);
        aLayer.getLabelSet().setYOffset(3);
        aLayer.getLabelSet().setFieldName(fieldName);
        aLayer.getLabelSet().setColorByLegend(true);
        aLayer.getLabelSet().setDynamicContourLabel(true);
        //aLayer.addLabels();

        return aLayer;
    }

    /**
     * Create shaded layer
     *
     * @param gridData Grid data
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createShadedLayer(GridData gridData, String lName, String fieldName) {
        LegendScheme ls = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.GRADUATED_COLOR, ShapeTypes.POLYGON);

        return createShadedLayer(gridData, ls, lName, fieldName, true);
    }

    /**
     * Create shaded layer
     *
     * @param gridData Grid data
     * @param lName Layer name
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createShadedLayer(GridData gridData, String lName, String fieldName, boolean isSmooth) {
        LegendScheme ls = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.GRADUATED_COLOR, ShapeTypes.POLYGON);

        return createShadedLayer(gridData, ls, lName, fieldName, isSmooth);
    }

    /**
     * Create shaded layer
     *
     * @param data Grid data array
     * @param x X array
     * @param y Y array
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createShadedLayer(Array data, Array x, Array y, LegendScheme aLS, String lName, String fieldName, boolean isSmooth) {
        GridData gridData = new GridData(data, x, y);
        return createShadedLayer(gridData, aLS, lName, fieldName, isSmooth);
    }

    /**
     * Create shaded layer
     *
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createShadedLayer(GridData gridData, LegendScheme aLS, String lName, String fieldName, boolean isSmooth) {
        List<PolyLine> ContourLines;
        List<Polygon> ContourPolygons;

        LegendScheme ls = aLS.convertTo(ShapeTypes.POLYGON);
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
        ContourLines = (List<PolyLine>) cbs[0];
        List<wcontour.global.Border> borders = (List<wcontour.global.Border>) cbs[1];

        if (isSmooth) {
            ContourLines = wcontour.Contour.smoothLines(ContourLines);
        }
        ContourPolygons = ContourDraw.tracingPolygons(gridData.getData(), ContourLines, borders, cValues);

        //wContour.Global.Polygon aPolygon;
        //Color aColor;
        double aValue;
        int valueIdx;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYGON);
        Field aDC = new Field(fieldName + "_Low", DataType.DOUBLE);
        aLayer.editAddField(aDC);
        aDC = new Field(fieldName + "_High", DataType.DOUBLE);
        aLayer.editAddField(aDC);

        for (Polygon aPolygon : ContourPolygons) {
            //aPolygon = ContourPolygon;
            aValue = aPolygon.LowValue;
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (wcontour.global.PointD pointList : aPolygon.OutLine.PointList) {
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
            aPolygonShape.lowValue = aValue;
            if (aPolygon.HasHoles()) {
                for (PolyLine holeLine : aPolygon.HoleLines) {
                    if (holeLine.PointList.size() < 3) {
                        continue;
                    }
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
            valueIdx = Arrays.binarySearch(cValues, aValue);
            if (valueIdx < 0) {
                valueIdx = -valueIdx - 1;
            }
            //valueIdx = Arrays.asList(cValues).indexOf(aValue);
            if (valueIdx >= cValues.length - 1) {
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
            if (!aPolygon.IsHighCenter && aPolygon.HighValue == aPolygon.LowValue) {
                aPolygonShape.highValue = aValue;
                if (valueIdx == 0) {
                    aPolygonShape.lowValue = minData;
                } else {
                    aPolygonShape.lowValue = cValues[valueIdx - 1];
                }
            }
            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolygonShape, shapeNum)) {
                    aLayer.editCellValue(fieldName + "_Low", shapeNum, aPolygonShape.lowValue);
                    aLayer.editCellValue(fieldName + "_High", shapeNum, aPolygonShape.highValue);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        ls.setFieldName(fieldName + "_Low");
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.SHADED);
//        for (org.meteoinfo.legend.ColorBreak cb : aLayer.getLegendScheme().getLegendBreaks()){
//            System.out.println(cb.getColor().getAlpha());
//        }

        return aLayer;
    }

    /**
     * Create grid fill layer
     *
     * @param gridData Grid data
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createGridFillLayer(GridData gridData, String lName, String fieldName) {
        LegendScheme ls = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.GRADUATED_COLOR, ShapeTypes.POLYGON);

        return createGridFillLayer(gridData, ls, lName, fieldName);
    }

    /**
     * Create grid fill layer
     *
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createGridFillLayer(GridData gridData, LegendScheme aLS, String lName, String fieldName) {
        //generate grid points
        int i, j;
        PointD aPoint;

        List<PointD> PList;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYGON);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        aLayer.editAddField(aDC);

        double XDelt = gridData.getXDelta();
        double YDelt = gridData.getYDelta();
        double[] xArray = gridData.getXArray();
        double[] yArray = gridData.getYArray();
        for (i = 0; i < gridData.getYNum(); i++) {
            for (j = 0; j < gridData.getXNum(); j++) {
                PList = new ArrayList<>();
                aPoint = new PointD();
                aPoint.X = xArray[j] - XDelt / 2;
                aPoint.Y = yArray[i] - YDelt / 2;
                PList.add(aPoint);
                aPoint = new PointD();
                aPoint.X = xArray[j] - XDelt / 2;
                aPoint.Y = yArray[i] + YDelt / 2;
                PList.add(aPoint);
                aPoint = new PointD();
                aPoint.X = xArray[j] + XDelt / 2;
                aPoint.Y = yArray[i] + YDelt / 2;
                PList.add(aPoint);
                aPoint = new PointD();
                aPoint.X = xArray[j] + XDelt / 2;
                aPoint.Y = yArray[i] - YDelt / 2;
                PList.add(aPoint);
                PList.add(PList.get(0));

                PolygonShape aPGS = new PolygonShape();
                aPGS.lowValue = gridData.getDoubleValue(i, j);
                aPGS.highValue = aPGS.lowValue;
                aPGS.setPoints(PList);

                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(aPGS, shapeNum)) {
                        aLayer.editCellValue(fieldName, shapeNum, gridData.getDoubleValue(i, j));
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        aLayer.setLayerName(lName);
        aLS.setFieldName(fieldName);
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POLYGON));
        aLayer.setLayerDrawType(LayerDrawType.GRID_FILL);

        return aLayer;
    }

    /**
     * Create grid fill layer
     *
     * @param x_s X array
     * @param y_s Y array
     * @param a Data array
     * @param ls Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createGridFillLayer(Array x_s, Array y_s, Array a, LegendScheme ls, String lName, String fieldName) {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYGON);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        aLayer.editAddField(aDC);

        int colNum = (int) x_s.getSize();
        int rowNum = (int) y_s.getSize();
        double x, x1 = 0, x2, y, y1 = 0, y2, xd, yd, v;
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
                ps.lowValue = v;
                ps.highValue = v;
                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(ps, shapeNum)) {
                        aLayer.editCellValue(fieldName, shapeNum, v);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
                x1 = x2;
            }
            y1 = y2;
        }

        aLayer.setLayerName(lName);
        ls.setFieldName(fieldName);
        aLayer.setLegendScheme(ls.convertTo(ShapeTypes.POLYGON));
        aLayer.setLayerDrawType(LayerDrawType.GRID_FILL);

        return aLayer;
    }

    /**
     * Create grid point layer
     *
     * @param gridData Grid data
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createGridPointLayer(GridData gridData, String lName, String fieldName) {
        LegendScheme ls = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.GRADUATED_COLOR, ShapeTypes.POINT);

        return createGridPointLayer(gridData, ls, lName, fieldName);
    }

    /**
     * Create grid point layer
     *
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createGridPointLayer(GridData gridData, LegendScheme aLS, String lName, String fieldName) {
        //generate grid points
        int i, j;
        PointD aPoint;

        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        aLayer.editAddField(aDC);

        double[] xArray = gridData.getXArray();
        double[] yArray = gridData.getYArray();
        for (i = 0; i < gridData.getYNum(); i++) {
            for (j = 0; j < gridData.getXNum(); j++) {
                aPoint = new PointD();
                aPoint.X = xArray[j];
                aPoint.Y = yArray[i];
                PointShape aPointShape = new PointShape();
                aPointShape.setPoint(aPoint);
                aPointShape.setValue(gridData.getDoubleValue(i, j));

                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                        aLayer.editCellValue(fieldName, shapeNum, gridData.getDoubleValue(i, j));
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        aLayer.setLayerName(lName);
        aLS.setFieldName(fieldName);
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        aLayer.setLayerDrawType(LayerDrawType.GRID_POINT);

        return aLayer;
    }

    /**
     * Create grid wind vector layer from U/V or direction/speed grid data
     *
     * @param uData U or wind direction grid data
     * @param vData V or wind speed grid data
     * @param lName Layer name
     * @param isUV if is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridVectorLayer(GridData uData, GridData vData,
                                                    String lName, boolean isUV) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.blue, 10);
        ls.asArrow();
        return createGridVectorLayer(uData, vData, uData, ls, false, lName, isUV);
    }

    /**
     * Create grid wind vector layer from U/V or direction/speed grid data
     *
     * @param uData U or wind direction grid data
     * @param vData V or wind speed grid data
     * @param ls Legend scheme
     * @param lName Layer name
     * @param isUV if is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridVectorLayer(GridData uData, GridData vData,
                                                    LegendScheme ls, String lName, boolean isUV) {
        ls.asArrow();
        return createGridVectorLayer(uData, vData, uData, ls, false, lName, isUV);
    }

    /**
     * Create grid wind vector layer from U/V or direction/speed grid data
     *
     * @param uData U or wind direction grid data
     * @param vData V or wind speed grid data
     * @param gridData The grid data
     * @param lName Layer name
     * @param isUV if is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridVectorLayer(GridData uData, GridData vData, GridData gridData,
                                                    String lName, boolean isUV) {
        LegendScheme ls = LegendManage.createLegendSchemeFromGridData(gridData,
                LegendType.GRADUATED_COLOR, ShapeTypes.POINT);
        PointBreak aPB;
        for (int i = 0; i < ls.getBreakNum(); i++) {
            aPB = (PointBreak) ls.getLegendBreaks().get(i);
            aPB.setSize(10);
            ls.setLegendBreak(i, new ArrowBreak(aPB));
        }

        return createGridVectorLayer(uData, vData, gridData, ls, true, lName, isUV);
    }

    /**
     * Create grid wind vector layer from U/V or direction/speed grid data
     *
     * @param uData U or wind direction grid data
     * @param vData V or wind speed grid data
     * @param gridData The grid data
     * @param ls Legend scheme
     * @param lName Layer name
     * @param isUV if is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridVectorLayer(GridData uData, GridData vData, GridData gridData,
                                                    LegendScheme ls, String lName, boolean isUV) {
        ls.asArrow();
        return createGridVectorLayer(uData, vData, gridData, ls, true, lName, isUV);
    }

    /**
     * Create grid wind vector layer from U/V or direction/speed grid data
     *
     * @param uData U or wind direction grid data
     * @param vData V or wind speed grid data
     * @param gridData The grid data
     * @param ls Legend scheme
     * @param ifColor If draw color wind
     * @param lName Layer name
     * @param isUV if is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridVectorLayer(GridData uData, GridData vData, GridData gridData,
                                                    LegendScheme ls, boolean ifColor, String lName, boolean isUV) {
        GridData windDirData;
        GridData windSpeedData;
        if (isUV) {
            GridData[] uv = DataMath.getDSFromUV(uData, vData);
            windDirData = uv[0];
            windSpeedData = uv[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i, j;
        double windDir, windSpeed;
        float size = 6;
        PointD aPoint;
        int XNum = uData.getXNum();
        int YNum = uData.getYNum();

        String columnName = lName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        //Add data column
        if (isUV) {
            aLayer.editAddField("U", DataType.FLOAT);
            aLayer.editAddField("V", DataType.FLOAT);
        }
        aLayer.editAddField("WindDirection", DataType.FLOAT);
        aLayer.editAddField("WindSpeed", DataType.FLOAT);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifColor && ifAdd) {
            aLayer.editAddField(columnName, DataType.FLOAT);
        }

        double[] xArray = uData.getXArray();
        double[] yArray = uData.getYArray();
        for (i = 0; i < YNum; i++) {
            for (j = 0; j < XNum; j++) {
                windDir = windDirData.getDoubleValue(i, j);
                windSpeed = windSpeedData.getDoubleValue(i, j);
                if (!MIMath.doubleEquals(windDir, windDirData.getDoubleMissingValue())) {
                    if (!MIMath.doubleEquals(windSpeed, windSpeedData.getDoubleMissingValue())) {
                        aPoint = new PointD();
                        aPoint.X = xArray[j];
                        aPoint.Y = yArray[i];
                        WindArrow aArraw = new WindArrow();
                        aArraw.angle = windDir;
                        aArraw.length = (float) windSpeed;
                        aArraw.size = size;
                        aArraw.setPoint(aPoint);

                        if (ifColor) {
                            aArraw.setValue(gridData.getDoubleValue(i, j));
                        }

                        int shapeNum = aLayer.getShapeNum();
                        try {
                            if (aLayer.editInsertShape(aArraw, shapeNum)) {
                                if (isUV) {
                                    aLayer.editCellValue("U", shapeNum, uData.getDoubleValue(i, j));
                                    aLayer.editCellValue("V", shapeNum, vData.getDoubleValue(i, j));
                                }
                                aLayer.editCellValue("WindDirection", shapeNum, aArraw.angle);
                                aLayer.editCellValue("WindSpeed", shapeNum, aArraw.length);
                                if (ifColor && ifAdd) {
                                    aLayer.editCellValue(columnName, shapeNum, gridData.getDoubleValue(i, j));
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
        }

        aLayer.setLayerName(lName);
        if (ifColor && ifAdd) {
            ls.setFieldName(columnName);
        } else {
            ls.setFieldName("WindSpeed");
        }
        LegendScheme nls = ls.convertTo(ShapeTypes.POINT);
        nls.asArrow();
        if (ls.getShapeType() != nls.getShapeType()) {
            PointBreak aPB;
            for (i = 0; i < nls.getBreakNum(); i++) {
                aPB = (PointBreak) nls.getLegendBreaks().get(i);
                aPB.setSize(10);
            }
        }
        aLayer.setLegendScheme(nls);
        aLayer.setLayerDrawType(LayerDrawType.VECTOR);

        return aLayer;
    }

    /**
     * Create grid barb layer from U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridBarbLayer(GridData uData, GridData vData,
                                                  String lName, boolean isUV) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.blue, 10);
        ls.asArrow();
        return createGridBarbLayer(uData, vData, uData, ls, false, lName, isUV);
    }

    /**
     * Create grid barb layer from U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param ls Legend schemer
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridBarbLayer(GridData uData, GridData vData,
                                                  LegendScheme ls, String lName, boolean isUV) {
        ls.asArrow();
        return createGridBarbLayer(uData, vData, uData, ls, false, lName, isUV);
    }

    /**
     * Create grid barb layer from U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param gridData Grid data
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridBarbLayer(GridData uData, GridData vData, GridData gridData,
                                                  String lName, boolean isUV) {
        LegendScheme ls = LegendManage.createLegendSchemeFromGridData(gridData,
                LegendType.GRADUATED_COLOR, ShapeTypes.POINT);
        PointBreak aPB;
        for (int i = 0; i < ls.getBreakNum(); i++) {
            aPB = (PointBreak) ls.getLegendBreaks().get(i);
            aPB.setSize(10);
        }

        return createGridBarbLayer(uData, vData, gridData, ls, true, lName, isUV);
    }

    /**
     * Create grid barb layer from U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param gridData Grid data
     * @param aLS Legend schemer
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridBarbLayer(GridData uData, GridData vData, GridData gridData,
                                                  LegendScheme aLS, String lName, boolean isUV) {
        return createGridBarbLayer(uData, vData, gridData, aLS, true, lName, isUV);
    }

    /**
     * Create grid barb layer from U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param ifColor If is color
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridBarbLayer(GridData uData, GridData vData, GridData gridData,
                                                  LegendScheme aLS, boolean ifColor, String lName, boolean isUV) {
        GridData windDirData;
        GridData windSpeedData;
        if (isUV) {
            GridData[] wwData = DataMath.getDSFromUV(uData, vData);
            windDirData = wwData[0];
            windSpeedData = wwData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i, j;
        WindBarb aWB;
        double windDir, windSpeed;
        PointD aPoint;
        int XNum = windDirData.getXNum();
        int YNum = windDirData.getYNum();
        String columnName = lName.split("_")[0];

        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        //Add data column
        if (isUV) {
            aLayer.editAddField("U", DataType.FLOAT);
            aLayer.editAddField("V", DataType.FLOAT);
        }
        aLayer.editAddField("WindDirection", DataType.FLOAT);
        aLayer.editAddField("WindSpeed", DataType.FLOAT);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifColor && ifAdd) {
            aLayer.editAddField(columnName, DataType.FLOAT);
        }

        double[] xArray = windDirData.getXArray();
        double[] yArray = windDirData.getYArray();
        for (i = 0; i < YNum; i++) {
            for (j = 0; j < XNum; j++) {
                windDir = windDirData.getDoubleValue(i, j);
                windSpeed = windSpeedData.getDoubleValue(i, j);
                if (!MIMath.doubleEquals(windDir, windDirData.getDoubleMissingValue())) {
                    if (!MIMath.doubleEquals(windSpeed, windSpeedData.getDoubleMissingValue())) {
                        aPoint = new PointD();
                        aPoint.X = xArray[j];
                        aPoint.Y = yArray[i];
                        aWB = Draw.calWindBarb((float) windDir, (float) windSpeed, 0, 10, aPoint);
                        if (ifColor) {
                            aWB.setValue(gridData.getDoubleValue(i, j));
                        }

                        int shapeNum = aLayer.getShapeNum();
                        try {
                            if (aLayer.editInsertShape(aWB, shapeNum)) {
                                if (isUV) {
                                    aLayer.editCellValue("U", shapeNum, uData.getDoubleValue(i, j));
                                    aLayer.editCellValue("V", shapeNum, vData.getDoubleValue(i, j));
                                }
                                aLayer.editCellValue("WindDirection", shapeNum, aWB.angle);
                                aLayer.editCellValue("WindSpeed", shapeNum, aWB.windSpeed);
                                if (ifColor && ifAdd) {
                                    aLayer.editCellValue(columnName, shapeNum, gridData.getDoubleValue(i, j));
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
        }

        aLayer.setLayerName(lName);
        if (ifColor && ifAdd) {
            aLS.setFieldName(columnName);
        } else {
            aLS.setFieldName("WindSpeed");
        }
        LegendScheme ls = aLS.convertTo(ShapeTypes.POINT);
        if (aLS.getShapeType() != ls.getShapeType()) {
            PointBreak aPB;
            for (i = 0; i < ls.getBreakNum(); i++) {
                aPB = (PointBreak) ls.getLegendBreaks().get(i);
                aPB.setSize(10);
            }
        }
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.BARB);

        return aLayer;
    }

    /**
     * Create streamline layer by U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createStreamlineLayer(GridData uData, GridData vData,
                                                    String lName, boolean isUV) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYLINE, Color.blue, 1);
        return createStreamlineLayer(uData, vData, 4, ls, lName, isUV);
    }

    /**
     * Create streamline layer by U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param density Density
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createStreamlineLayer(GridData uData, GridData vData, int density,
                                                    String lName, boolean isUV) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYLINE, Color.blue, 1);
        return createStreamlineLayer(uData, vData, density, ls, lName, isUV);
    }

    /**
     * Create streamline layer by U/V or wind direction/speed grid data
     *
     * @param udata U data array
     * @param vdata V data array
     * @param xdata X array
     * @param ydata Y array
     * @param density Density
     * @param ls Legend scheme
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createStreamlineLayer(Array udata, Array vdata, Array xdata, Array ydata, int density, LegendScheme ls,
                                                    String lName, boolean isUV) {
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

        PolyLine aLine;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYLINE_Z);
        aLayer.editAddField("ID", DataType.INT);

        for (int i = 0; i < streamlines.size() - 1; i++) {
            aLine = streamlines.get(i);

            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (int j = 0; j < aLine.PointList.size(); j++) {
                aPoint = new PointD();
                aPoint.X = (aLine.PointList.get(j)).X;
                aPoint.Y = (aLine.PointList.get(j)).Y;
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(density);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue("ID", shapeNum, shapeNum + 1);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        ls.setFieldName("ID");
        if (ls.getShapeType() != ShapeTypes.POLYLINE)
            ls = ls.convertTo(ShapeTypes.POLYLINE);
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.STREAMLINE);

        return aLayer;
    }

    /**
     * Create streamline layer by U/V or wind direction/speed grid data
     *
     * @param udata U data array
     * @param vdata V data array
     * @param xdata X array
     * @param ydata Y array
     * @param cdata Color data array
     * @param density Density
     * @param ls Legend scheme
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createStreamlineLayer(Array udata, Array vdata, Array xdata, Array ydata, Array cdata,
                                                    int density, LegendScheme ls, String lName, boolean isUV) {
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
        int ny = u.length;
        int nx = u[0].length;

        PolyLine aLine;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYLINE_Z);
        aLayer.editAddField("ID", DataType.INT);

        for (int i = 0; i < streamlines.size() - 1; i++) {
            aLine = streamlines.get(i);

            PolylineZShape aPolyline = new PolylineZShape();
            PointZ p;
            List<PointD> pList = new ArrayList<>();
            double c = 0;
            for (int j = 0; j < aLine.PointList.size(); j++) {
                p = new PointZ();
                p.X = (aLine.PointList.get(j)).X;
                p.Y = (aLine.PointList.get(j)).Y;
                int[] idx = ArrayUtil.gridIndex(xdata, ydata, p.X, p.Y);
                if (idx != null) {
                    int yi = idx[0];
                    int xi = idx[1];
                    c = cdata.getDouble(yi * nx + xi);
                }
                p.Z = c;
                pList.add(p);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(density);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue("ID", shapeNum, shapeNum + 1);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        ls.setFieldName("Geometry_Z");
        if (ls.getShapeType() != ShapeTypes.POLYLINE)
            ls = ls.convertTo(ShapeTypes.POLYLINE);
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.STREAMLINE);

        return aLayer;
    }

    /**
     * Create streamline layer by U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param density Density
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createStreamlineLayer(GridData uData, GridData vData, int density, LegendScheme aLS,
                                                    String lName, boolean isUV) {
        uData.asAscending();
        vData.asAscending();
        GridData uGridData;
        GridData vGridData;
        if (isUV) {
            uGridData = uData;
            vGridData = vData;
        } else {
            GridData[] uvData = DataMath.getUVFromDS(uData, vData);
            uGridData = uvData[0];
            vGridData = uvData[1];
        }

        List<PolyLine> streamlines = wcontour.Contour.tracingStreamline(uGridData.getData(), vGridData.getData(),
                uGridData.getXArray(), vGridData.getYArray(), uGridData.getDoubleMissingValue(), density);

        PolyLine aLine;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYLINE);
        aLayer.editAddField("ID", DataType.INT);

        for (int i = 0; i < streamlines.size() - 1; i++) {
            aLine = streamlines.get(i);

            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (int j = 0; j < aLine.PointList.size(); j++) {
                aPoint = new PointD();
                aPoint.X = (aLine.PointList.get(j)).X;
                aPoint.Y = (aLine.PointList.get(j)).Y;
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(density);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue("ID", shapeNum, shapeNum + 1);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        aLS.setFieldName("ID");
        if (aLS.getShapeType() != ShapeTypes.POLYLINE)
            aLS = aLS.convertTo(ShapeTypes.POLYLINE);
        aLayer.setLegendScheme(aLS);
        aLayer.setLayerDrawType(LayerDrawType.STREAMLINE);

        return aLayer;
    }

    /**
     * Create image layer
     *
     * @param x X array
     * @param y Y array
     * @param graphic Image graphic
     * @param layerName Layer name
     * @return Image layer
     */
    public static ImageLayer createImageLayer(Array x, Array y, Graphic graphic, String layerName) {
        BufferedImage image = ((ImageShape) graphic.getShape()).getImage();
        return createImageLayer(x, y, image, layerName);
    }

    /**
     * Create image layer
     *
     * @param x X array
     * @param y Y array
     * @param image Image
     * @param layerName Layer name
     * @return Image layer
     */
    public static ImageLayer createImageLayer(Array x, Array y, BufferedImage image, String layerName) {
        x = x.copyIfView();
        y = y.copyIfView();

        ImageLayer aImageLayer = new ImageLayer();
        aImageLayer.setImage(image);
        aImageLayer.setLayerName(layerName);
        aImageLayer.setVisible(true);

        WorldFilePara aWFP = new WorldFilePara();
        double xdelta = x.getDouble(1) - x.getDouble(0);
        double ydelta = y.getDouble(1) - y.getDouble(0);
        aWFP.xUL = x.getDouble(0) - xdelta / 2;
        aWFP.yUL = y.getDouble(y.getShape()[0] - 1) + ydelta / 2;
        aWFP.xScale = xdelta;
        aWFP.yScale = -ydelta;
        aWFP.xRotate = 0;
        aWFP.yRotate = 0;
        aImageLayer.setWorldFilePara(aWFP);

        double XBR, YBR;
        XBR = aImageLayer.getImage().getWidth() * aImageLayer.getWorldFilePara().xScale + aImageLayer.getWorldFilePara().xUL;
        YBR = aImageLayer.getImage().getHeight() * aImageLayer.getWorldFilePara().yScale + aImageLayer.getWorldFilePara().yUL;
        Extent aExtent = new Extent();
        aExtent.minX = aImageLayer.getWorldFilePara().xUL;
        aExtent.minY = YBR;
        aExtent.maxX = XBR;
        aExtent.maxY = aImageLayer.getWorldFilePara().yUL;
        aImageLayer.setExtent(aExtent);
        aImageLayer.setLayerDrawType(LayerDrawType.IMAGE);
        aImageLayer.setMaskout(true);

        return aImageLayer;
    }

    /**
     * Create raster layer
     *
     * @param gridData Grid data
     * @param lName Layer name
     * @return Raster layer
     */
    public static RasterLayer createRasterLayer(GridData gridData, String lName) {
        boolean isUnique = gridData.testUniqueValues();
        LegendScheme ls;
        if (isUnique) {
            List<Number> values = gridData.getUniqueValues();
            ls = LegendManage.createUniqValueLegendScheme(values, ShapeTypes.POLYGON);
        } else {
            ls = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.GRADUATED_COLOR, ShapeTypes.POLYGON);
        }

        return createRasterLayer(gridData, lName, ls);
    }

    /**
     * Create raster layer
     *
     * @param gridData Grid data
     * @param lName Layer name
     * @return Raster layer
     */
    public static RasterLayer createRasterLayer(GridArray gridData, String lName) {
        boolean isUnique = gridData.testUniqueValues();
        LegendScheme ls;
        if (isUnique) {
            List<Number> values = gridData.getUniqueValues();
            ls = LegendManage.createUniqValueLegendScheme(values, ShapeTypes.POLYGON);
        } else {
            ls = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.GRADUATED_COLOR, ShapeTypes.POLYGON);
        }

        return createRasterLayer(gridData, lName, ls);
    }

    /**
     * Create raster layer
     *
     * @param gridData Grid data
     * @param LName Layer name
     * @param aLS Legend scheme
     * @return Raster layer
     */
    public static RasterLayer createRasterLayer(GridArray gridData, String LName, LegendScheme aLS) {
        RasterLayer aRLayer = new RasterLayer();
        aRLayer.setGridData(gridData);
        aRLayer.setShapeType(ShapeTypes.IMAGE);
        aRLayer.setLegendScheme(aLS.convertTo(ShapeTypes.IMAGE));
        aRLayer.setLayerName(LName);
        aRLayer.setVisible(true);
        aRLayer.setLayerDrawType(LayerDrawType.RASTER);
        aRLayer.setMaskout(true);

        return aRLayer;
    }

    /**
     * Create raster layer
     *
     * @param gridData Grid data
     * @param LName Layer name
     * @param aLS Legend scheme
     * @return Raster layer
     */
    public static RasterLayer createRasterLayer(GridData gridData, String LName, LegendScheme aLS) {
        RasterLayer aRLayer = new RasterLayer();
        aRLayer.setGridData(gridData.toGridArray());
        aRLayer.setShapeType(ShapeTypes.IMAGE);
        aRLayer.setLegendScheme(aLS.convertTo(ShapeTypes.IMAGE));
        aRLayer.setLayerName(LName);
        aRLayer.setVisible(true);
        aRLayer.setLayerDrawType(LayerDrawType.RASTER);
        aRLayer.setMaskout(true);

        return aRLayer;
    }

    /**
     * Create raster layer
     *
     * @param gridData Grid data
     * @param LName Layer name
     * @param paletteFile Palette file name
     * @return Raster layer
     */
    public static RasterLayer createRasterLayer(GridData gridData, String LName, String paletteFile) {
        RasterLayer aRLayer = new RasterLayer();
        aRLayer.setGridData(gridData.toGridArray());
        aRLayer.setPalette(paletteFile);
        aRLayer.setLayerName(LName);
        aRLayer.setVisible(true);
        aRLayer.setLayerDrawType(LayerDrawType.RASTER);
        aRLayer.setMaskout(true);

        return aRLayer;
    }

    /**
     * Create station point layer
     *
     * @param stationData Station data
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createSTPointLayer(StationData stationData, String lName, String fieldName) {
        LegendScheme ls = LegendManage.createLegendSchemeFromStationData(stationData, LegendType.GRADUATED_COLOR, ShapeTypes.POINT);

        return createSTPointLayer(stationData, ls, lName, fieldName);
    }

    /**
     * Create station point layer
     *
     * @param data Station data array
     * @param x X array
     * @param y Y array
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createSTPointLayer(Array data, Array x, Array y, LegendScheme aLS, String lName, String fieldName) {
        int i;
        PointD aPoint;

        if (data.getRank() == 2 && x.getRank() == 1) {
            Array[] xy = ArrayUtil.meshgrid(x, y);
            x = xy[0];
            y = xy[1];
        }

        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        aLayer.editAddField(fieldName, DataType.DOUBLE);

        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        IndexIterator iter = data.getIndexIterator();
        double v;
        while(iter.hasNext()) {
            aPoint = new PointD();
            aPoint.X = xIter.getDoubleNext();
            aPoint.Y = yIter.getDoubleNext();
            v = iter.getDoubleNext();
            if (Double.isNaN(aPoint.X) || Double.isNaN(aPoint.Y) || Double.isNaN(v)) {
                continue;
            }
            PointShape aPointShape = new PointShape();
            aPointShape.setPoint(aPoint);
            aPointShape.setValue(v);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                    aLayer.editCellValue(fieldName, shapeNum, v);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        aLS.setFieldName(fieldName);
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        aLayer.setLayerDrawType(LayerDrawType.STATION_POINT);

        return aLayer;
    }

    /**
     * Create station point layer
     *
     * @param stationData Station data
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createSTPointLayer(StationData stationData, LegendScheme aLS, String lName, String fieldName) {
        int i;
        PointD aPoint;

        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        aLayer.editAddField("Station", DataType.STRING);
        aLayer.editAddField(fieldName, DataType.DOUBLE);

        for (i = 0; i < stationData.data.length; i++) {
            aPoint = new PointD();
            aPoint.X = stationData.data[i][0];
            aPoint.Y = stationData.data[i][1];
            if (Double.isNaN(aPoint.X)) {
                continue;
            }
            PointShape aPointShape = new PointShape();
            aPointShape.setPoint(aPoint);
            aPointShape.setValue(stationData.data[i][2]);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                    aLayer.editCellValue("Station", shapeNum, stationData.stations.get(i));
                    aLayer.editCellValue(fieldName, shapeNum, stationData.data[i][2]);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        aLS.setFieldName(fieldName);
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        //aLayer.setAvoidCollision(true);
        aLayer.setLayerDrawType(LayerDrawType.STATION_POINT);

        return aLayer;
    }

    /**
     * Create station point layer
     *
     * @param data Station data array
     * @param x X array
     * @param y Y array
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createSTPointLayer_Unique(Array data, Array x, Array y, LegendScheme aLS, String lName, String fieldName) {
        int i;
        PointD aPoint;

        if (data.getRank() == 2 && x.getRank() == 1) {
            Array[] xy = ArrayUtil.meshgrid(x, y);
            x = xy[0];
            y = xy[1];
        }

        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        aLayer.editAddField("ID", DataType.INT);
        aLayer.editAddField(fieldName, DataType.DOUBLE);

        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        IndexIterator iter = data.getIndexIterator();
        double v;
        while(iter.hasNext()) {
            aPoint = new PointD();
            aPoint.X = xIter.getDoubleNext();
            aPoint.Y = yIter.getDoubleNext();
            v = iter.getDoubleNext();
            if (Double.isNaN(aPoint.X) || Double.isNaN(aPoint.Y)) {
                continue;
            }
            PointShape aPointShape = new PointShape();
            aPointShape.setPoint(aPoint);
            aPointShape.setValue(v);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                    aLayer.editCellValue("ID", shapeNum, shapeNum);
                    aLayer.editCellValue(fieldName, shapeNum, v);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        aLS.setFieldName("ID");
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        aLayer.setLayerDrawType(LayerDrawType.STATION_POINT);

        return aLayer;
    }

    /**
     * Create station point layer
     *
     * @param stationData Station data
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createSTPointLayer_Unique(StationData stationData, LegendScheme aLS, String lName, String fieldName) {
        int i;
        PointD aPoint;

        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        aLayer.editAddField("ID", DataType.INT);
        aLayer.editAddField("Stid", DataType.STRING);
        aLayer.editAddField(fieldName, DataType.DOUBLE);

        for (i = 0; i < stationData.data.length; i++) {
            aPoint = new PointD();
            aPoint.X = stationData.data[i][0];
            aPoint.Y = stationData.data[i][1];
            if (Double.isNaN(aPoint.X)) {
                continue;
            }
            PointShape aPointShape = new PointShape();
            aPointShape.setPoint(aPoint);
            aPointShape.setValue(stationData.data[i][2]);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                    aLayer.editCellValue("ID", shapeNum, i);
                    aLayer.editCellValue("Stid", shapeNum, stationData.stations.get(i));
                    aLayer.editCellValue(fieldName, shapeNum, stationData.data[i][2]);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        aLS.setFieldName("ID");
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        //aLayer.setAvoidCollision(true);
        aLayer.setLayerDrawType(LayerDrawType.STATION_POINT);

        return aLayer;
    }

    /**
     * Create station info layer
     *
     * @param stInfoData Station info data
     * @param layerName Layer name
     * @return Station info layer
     */
    public static VectorLayer createSTInfoLayer(StationInfoData stInfoData, String layerName) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.red, 8);

        return createSTInfoLayer(stInfoData, ls, layerName);
    }

    /**
     * Create station info layer
     *
     * @param stInfoData Station info data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @return Station info layer
     */
    public static VectorLayer createSTInfoLayer(StationInfoData stInfoData, LegendScheme aLS, String layerName) {
        int i, j;
        PointD aPoint;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);

        DataFrame dataFrame = stInfoData.getDataFrame();
        if (dataFrame == null) {
            for (i = 0; i < stInfoData.getFields().size(); i++) {
                String fieldName = stInfoData.getFields().get(i);
                DataType dtype = DataType.STRING;
                if (stInfoData.getVariables().contains(stInfoData.getFields().get(i))) {
                    dtype = DataType.DOUBLE;
                }
                aLayer.editAddField(fieldName, dtype);
            }

            double v;
            for (i = 0; i < stInfoData.getDataList().size(); i++) {
                List<String> dataList = stInfoData.getDataList().get(i);
                aPoint = new PointD();
                aPoint.X = Double.parseDouble(dataList.get(1));
                aPoint.Y = Double.parseDouble(dataList.get(2));
                PointShape aPointShape = new PointShape();
                aPointShape.setPoint(aPoint);

                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                        for (j = 0; j < stInfoData.getFields().size(); j++) {
                            if (stInfoData.getVariables().contains(stInfoData.getFields().get(j))) {
                                if (dataList.size() <= j) {
                                    v = 9999.0;
                                } else {
                                    if (dataList.get(j).isEmpty()) {
                                        v = 9999.0;
                                    } else {
                                        try {
                                            v = Double.parseDouble(dataList.get(j));
                                        } catch (Exception e) {
                                            v = 9999.0;
                                        }
                                    }
                                }
                                aLayer.editCellValue(stInfoData.getFields().get(j), shapeNum, v);
                            } else {
                                aLayer.editCellValue(stInfoData.getFields().get(j), shapeNum, dataList.get(j));
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            for (i = 0; i < dataFrame.size(); i++) {
                String fieldName = dataFrame.getColumnNames().get(i);
                DataType dataType = dataFrame.getColumnDataTypes().get(i);
                aLayer.editAddField(fieldName, dataType);
            }

            double v;
            for (i = 0; i < dataFrame.length(); i++) {
                aPoint = new PointD();
                aPoint.X = (float)dataFrame.getValue(i, "Longitude");
                aPoint.Y = (float)dataFrame.getValue(i, "Latitude");
                PointShape aPointShape = new PointShape();
                aPointShape.setPoint(aPoint);

                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                        for (j = 0; j < dataFrame.size(); j++) {
                            aLayer.editCellValue(j, shapeNum, dataFrame.getValue(i, j));
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        //aLayer.setAvoidCollision(true);
        aLayer.setLayerDrawType(LayerDrawType.STATION_POINT);

        return aLayer;
    }

    /**
     * Create station vector layer
     *
     * @param uData U station data
     * @param vData V station data
     * @param stData Station data
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station vector layer
     */
    public static VectorLayer createSTVectorLayer(StationData uData, StationData vData, StationData stData,
                                                  String layerName, boolean isUV) {
        LegendScheme ls = LegendManage.createLegendSchemeFromStationData(stData, LegendType.GRADUATED_COLOR, ShapeTypes.POINT);
        for (int i = 0; i < ls.getLegendBreaks().size(); i++) {
            PointBreak aPB = (PointBreak) ls.getLegendBreaks().get(i);
            aPB.setSize(10);
        }

        return createSTVectorLayer(uData, vData, stData, ls, layerName, isUV);
    }

    /**
     * Create station vector layer
     *
     * @param uData U station data
     * @param vData V station data
     * @param stData Station data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station vector layer
     */
    public static VectorLayer createSTVectorLayer(StationData uData, StationData vData, StationData stData,
                                                  LegendScheme aLS, String layerName, boolean isUV) {
        aLS.asArrow();
        StationData windDirData;
        StationData windSpeedData;
        if (isUV) {
            StationData[] dsData = DataMath.getDSFromUV(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        float windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WIND_ARROW);
        //Add data column
        if (isUV) {
            aLayer.editAddField("U", DataType.FLOAT);
            aLayer.editAddField("V", DataType.FLOAT);
        }
        aLayer.editAddField("WindDirection", DataType.FLOAT);
        aLayer.editAddField("WindSpeed", DataType.FLOAT);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifAdd) {
            aLayer.editAddField(columnName, DataType.FLOAT);
        }

        for (i = 0; i < windDirData.getStNum(); i++) {
            windDir = (float) windDirData.data[i][2];
            windSpeed = (float) windSpeedData.data[i][2];
            if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                    aPoint = new PointD();
                    aPoint.X = windDirData.data[i][0];
                    aPoint.Y = windDirData.data[i][1];

                    WindArrow aArraw = new WindArrow();
                    aArraw.angle = windDir;
                    aArraw.length = windSpeed;
                    aArraw.size = 6;
                    aArraw.setPoint(aPoint);
                    aArraw.setValue(stData.data[i][2]);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aArraw, shapeNum)) {
                            if (isUV) {
                                aLayer.editCellValue("U", shapeNum, uData.data[i][2]);
                                aLayer.editCellValue("V", shapeNum, vData.data[i][2]);
                            }
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                            if (ifAdd) {
                                aLayer.editCellValue(columnName, shapeNum, stData.data[i][2]);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        aLayer.setLayerDrawType(LayerDrawType.VECTOR);

        return aLayer;
    }

    /**
     * Create vector layer
     *
     * @param xData X array data
     * @param yData Y array data
     * @param uData U array data
     * @param vData V array data
     * @param stData Array data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createVectorLayer(Array xData, Array yData, Array uData, Array vData, Array stData,
                                                LegendScheme aLS, String layerName, boolean isUV) {
        Array windDirData;
        Array windSpeedData;
        if (isUV) {
            Array[] dsData = MeteoMath.uv2ds(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        double windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WIND_ARROW);
        //Add data column
        if (isUV) {
            aLayer.editAddField("U", DataType.FLOAT);
            aLayer.editAddField("V", DataType.FLOAT);
        }
        aLayer.editAddField("WindDirection", DataType.FLOAT);
        aLayer.editAddField("WindSpeed", DataType.FLOAT);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifAdd) {
            aLayer.editAddField(columnName, DataType.FLOAT);
        }

        IndexIterator xIter = xData.getIndexIterator();
        IndexIterator yIter = yData.getIndexIterator();
        IndexIterator wdIter = windDirData.getIndexIterator();
        IndexIterator wsIter = windSpeedData.getIndexIterator();
        IndexIterator cIter = stData == null ? null : stData.getIndexIterator();
        IndexIterator uIter = uData.getIndexIterator();
        IndexIterator vIter = vData.getIndexIterator();
        double v = 0.;
        while (xIter.hasNext()){
            windDir = wdIter.getFloatNext();
            windSpeed = wsIter.getFloatNext();
            if (!Double.isNaN(windDir) && !Double.isNaN(windSpeed)) {
                aPoint = new PointD();
                aPoint.X = xIter.getDoubleNext();
                aPoint.Y = yIter.getDoubleNext();

                WindArrow aArraw = new WindArrow();
                aArraw.angle = windDir;
                aArraw.length = (float) windSpeed;
                aArraw.size = 6;
                aArraw.setPoint(aPoint);
                if (stData != null) {
                    v = cIter.getDoubleNext();
                    aArraw.setValue(v);
                }

                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(aArraw, shapeNum)) {
                        if (isUV) {
                            aLayer.editCellValue("U", shapeNum, uIter.getDoubleNext());
                            aLayer.editCellValue("V", shapeNum, vIter.getDoubleNext());
                        }
                        aLayer.editCellValue("WindDirection", shapeNum, windDir);
                        aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                        if (ifAdd) {
                            if (stData != null) {
                                aLayer.editCellValue(columnName, shapeNum, v);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                xIter.next();
                yIter.next();
                uIter.next();
                vIter.next();
                if (stData != null) {
                    cIter.next();
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        aLayer.setLayerDrawType(LayerDrawType.VECTOR);

        return aLayer;
    }

    /**
     * Create barb layer
     *
     * @param xData X array data
     * @param yData Y array data
     * @param uData U array data
     * @param vData V array data
     * @param stData Array data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Barb layer
     */
    public static VectorLayer createBarbLayer(Array xData, Array yData, Array uData, Array vData, Array stData,
                                              LegendScheme aLS, String layerName, boolean isUV) {
        Array windDirData;
        Array windSpeedData;
        if (isUV) {
            Array[] dsData = MeteoMath.uv2ds(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        double windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WIND_ARROW);
        //Add data column
        if (isUV) {
            aLayer.editAddField("U", DataType.FLOAT);
            aLayer.editAddField("V", DataType.FLOAT);
        }
        aLayer.editAddField("WindDirection", DataType.FLOAT);
        aLayer.editAddField("WindSpeed", DataType.FLOAT);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifAdd) {
            aLayer.editAddField(columnName, DataType.FLOAT);
        }

        WindBarb aWB;
        IndexIterator xIter = xData.getIndexIterator();
        IndexIterator yIter = yData.getIndexIterator();
        IndexIterator wdIter = windDirData.getIndexIterator();
        IndexIterator wsIter = windSpeedData.getIndexIterator();
        IndexIterator cIter = stData == null ? null : stData.getIndexIterator();
        IndexIterator uIter = uData.getIndexIterator();
        IndexIterator vIter = vData.getIndexIterator();
        double v = 0.;
        while (xIter.hasNext()){
            windDir = wdIter.getFloatNext();
            windSpeed = wsIter.getFloatNext();
            if (!Double.isNaN(windDir) && !Double.isNaN(windSpeed)) {
                aPoint = new PointD();
                aPoint.X = xIter.getDoubleNext();
                aPoint.Y = yIter.getDoubleNext();
                aWB = Draw.calWindBarb((float) windDir, (float) windSpeed, 0, 10, aPoint);
                if (stData != null) {
                    v = cIter.getDoubleNext();
                    aWB.setValue(v);
                }

                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(aWB, shapeNum)) {
                        if (isUV) {
                            aLayer.editCellValue("U", shapeNum, uIter.getDoubleNext());
                            aLayer.editCellValue("V", shapeNum, vIter.getDoubleNext());
                        }
                        aLayer.editCellValue("WindDirection", shapeNum, aWB.angle);
                        aLayer.editCellValue("WindSpeed", shapeNum, aWB.windSpeed);
                        if (ifAdd) {
                            if (stData != null) {
                                aLayer.editCellValue(columnName, shapeNum, v);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                xIter.next();
                yIter.next();
                uIter.next();
                vIter.next();
                if (cIter != null)
                    cIter.next();
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS.convertTo(ShapeTypes.POINT));
        aLayer.setLayerDrawType(LayerDrawType.BARB);

        return aLayer;
    }

    /**
     * Create station vector layer
     *
     * @param uData U station data
     * @param vData V station data
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createSTVectorLayer(StationData uData, StationData vData,
                                                  String layerName, boolean isUV) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.blue, 10);

        return createSTVectorLayer(uData, vData, ls, layerName, isUV);
    }

    /**
     * Create station vector layer
     *
     * @param uData U station data
     * @param vData V station data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createSTVectorLayer(StationData uData, StationData vData,
                                                  LegendScheme aLS, String layerName, boolean isUV) {
        aLS.asArrow();
        StationData windDirData;
        StationData windSpeedData;
        if (isUV) {
            StationData[] dsData = DataMath.getDSFromUV(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        float windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WIND_ARROW);
        //Add data column
        if (isUV) {
            aLayer.editAddField("U", DataType.FLOAT);
            aLayer.editAddField("V", DataType.FLOAT);
        }
        aLayer.editAddField("WindDirection", DataType.FLOAT);
        aLayer.editAddField("WindSpeed", DataType.FLOAT);

        for (i = 0; i < windDirData.getStNum(); i++) {
            windDir = (float) windDirData.data[i][2];
            windSpeed = (float) windSpeedData.data[i][2];
            if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                    aPoint = new PointD();
                    aPoint.X = windDirData.data[i][0];
                    aPoint.Y = windDirData.data[i][1];

                    WindArrow aArraw = new WindArrow();
                    aArraw.angle = windDir;
                    aArraw.length = windSpeed;
                    aArraw.size = 6;
                    aArraw.setPoint(aPoint);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aArraw, shapeNum)) {
                            if (isUV) {
                                aLayer.editCellValue("U", shapeNum, uData.data[i][2]);
                                aLayer.editCellValue("V", shapeNum, vData.data[i][2]);
                            }
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        LegendScheme ls = aLS.convertTo(ShapeTypes.POINT);
        if (aLS.getShapeType() != ls.getShapeType()) {
            PointBreak aPB;
            for (i = 0; i < ls.getBreakNum(); i++) {
                aPB = (PointBreak) ls.getLegendBreaks().get(i);
                aPB.setSize(10);
            }
        }
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.VECTOR);

        return aLayer;
    }

    /**
     * Create station barb layer from U/V or direction/speed station data
     *
     * @param uData U station data
     * @param vData V station data
     * @param stData Station data
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station barb layer
     */
    public static VectorLayer createSTBarbLayer(StationData uData, StationData vData, StationData stData,
                                                String layerName, boolean isUV) {
        LegendScheme ls = LegendManage.createLegendSchemeFromStationData(stData, LegendType.GRADUATED_COLOR, ShapeTypes.POINT);
        for (int i = 0; i < ls.getLegendBreaks().size(); i++) {
            PointBreak aPB = (PointBreak) ls.getLegendBreaks().get(i);
            aPB.setSize(10);
        }

        return createSTBarbLayer(uData, vData, stData, ls, layerName, isUV);
    }

    /**
     * Create station barb layer from U/V or direction/speed station data
     *
     * @param uData U station data
     * @param vData V station data
     * @param stData Station data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station barb layer
     */
    public static VectorLayer createSTBarbLayer(StationData uData, StationData vData, StationData stData,
                                                LegendScheme aLS, String layerName, boolean isUV) {
        StationData windDirData;
        StationData windSpeedData;
        if (isUV) {
            StationData[] dsData = DataMath.getDSFromUV(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        float windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WIND_BARB);
        //Add data column
        if (isUV) {
            aLayer.editAddField("U", DataType.FLOAT);
            aLayer.editAddField("V", DataType.FLOAT);
        }
        aLayer.editAddField("WindDirection", DataType.FLOAT);
        aLayer.editAddField("WindSpeed", DataType.FLOAT);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifAdd) {
            aLayer.editAddField(columnName, DataType.FLOAT);
        }

        for (i = 0; i < windDirData.getStNum(); i++) {
            windDir = (float) windDirData.data[i][2];
            windSpeed = (float) windSpeedData.data[i][2];
            if (windSpeed == 0) {
                continue;
            }

            if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                    aPoint = new PointD();
                    aPoint.X = windDirData.data[i][0];
                    aPoint.Y = windDirData.data[i][1];
                    WindBarb aWB = Draw.calWindBarb(windDir, windSpeed, 0, 10, aPoint);
                    aWB.setValue(stData.data[i][2]);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aWB, shapeNum)) {
                            if (isUV) {
                                aLayer.editCellValue("U", shapeNum, uData.data[i][2]);
                                aLayer.editCellValue("V", shapeNum, vData.data[i][2]);
                            }
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                            if (ifAdd) {
                                aLayer.editCellValue(columnName, shapeNum, stData.data[i][2]);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        LegendScheme ls = aLS.convertTo(ShapeTypes.POINT);
        if (aLS.getShapeType() != ls.getShapeType()) {
            PointBreak aPB;
            for (i = 0; i < ls.getBreakNum(); i++) {
                aPB = (PointBreak) ls.getLegendBreaks().get(i);
                aPB.setSize(10);
            }
        }
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.BARB);

        return aLayer;
    }

    /**
     * Create station barb layer from U/V or direction/speed station data
     *
     * @param uData U station data
     * @param vData V station data
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station barb layer
     */
    public static VectorLayer createSTBarbLayer(StationData uData, StationData vData,
                                                String layerName, boolean isUV) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.blue, 10);

        return createSTBarbLayer(uData, vData, ls, layerName, isUV);
    }

    /**
     * Create station barb layer from U/V or direction/speed station data
     *
     * @param uData U station data
     * @param vData V station data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station barb layer
     */
    public static VectorLayer createSTBarbLayer(StationData uData, StationData vData,
                                                LegendScheme aLS, String layerName, boolean isUV) {
        StationData windDirData;
        StationData windSpeedData;
        if (isUV) {
            StationData[] dsData = DataMath.getDSFromUV(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        float windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WIND_BARB);
        //Add data column
        if (isUV) {
            aLayer.editAddField("U", DataType.FLOAT);
            aLayer.editAddField("V", DataType.FLOAT);
        }
        aLayer.editAddField("WindDirection", DataType.FLOAT);
        aLayer.editAddField("WindSpeed", DataType.FLOAT);

        for (i = 0; i < windDirData.getStNum(); i++) {
            windDir = (float) windDirData.data[i][2];
            windSpeed = (float) windSpeedData.data[i][2];
            if (windSpeed == 0) {
                continue;
            }

            if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                    aPoint = new PointD();
                    aPoint.X = windDirData.data[i][0];
                    aPoint.Y = windDirData.data[i][1];
                    WindBarb aWB = Draw.calWindBarb(windDir, windSpeed, 0, 10, aPoint);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aWB, shapeNum)) {
                            if (isUV) {
                                aLayer.editCellValue("U", shapeNum, uData.data[i][2]);
                                aLayer.editCellValue("V", shapeNum, vData.data[i][2]);
                            }
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        LegendScheme ls = aLS.convertTo(ShapeTypes.POINT);
        if (aLS.getShapeType() != ls.getShapeType()) {
            PointBreak aPB;
            for (i = 0; i < ls.getBreakNum(); i++) {
                aPB = (PointBreak) ls.getLegendBreaks().get(i);
                aPB.setSize(10);
            }
        }
        aLayer.setLegendScheme(ls);
        aLayer.setLayerDrawType(LayerDrawType.BARB);

        return aLayer;
    }

    /**
     * Create station model layer
     *
     * @param stationModelData Station model data
     * @param layerName Layer name
     * @return Station model layer
     */
    public static VectorLayer createStationModelLayer(StationModelData stationModelData,
                                                      String layerName) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.blue, 12);

        return createStationModelLayer(stationModelData, layerName, true);
    }

    /**
     * Create station model layer
     *
     * @param stationModelData Station model data
     * @param layerName Layer name
     * @param isSurface If is surface
     * @return Station model layer
     */
    public static VectorLayer createStationModelLayer(StationModelData stationModelData,
                                                      String layerName, boolean isSurface) {
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.blue, 12);

        return createStationModelLayer(stationModelData, ls, layerName, isSurface);
    }

    /**
     * Create station model layer
     *
     * @param stationModelData Station model data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @return Station model layer
     */
    public static VectorLayer createStationModelLayer(StationModelData stationModelData,
                                                      LegendScheme aLS, String layerName) {
        return createStationModelLayer(stationModelData, aLS, layerName, true);
    }

    /**
     * Create station model layer
     *
     * @param stationModelData Station model data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isSurface If is surface
     * @return Station model layer
     */
    public static VectorLayer createStationModelLayer(StationModelData stationModelData,
                                                      LegendScheme aLS, String layerName, boolean isSurface) {
        int i;
        StationModelShape aSM;
        float windDir, windSpeed;
        int weather, cCover, temp, dewPoint, pressure;
        PointD aPoint;

        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        aLayer.editAddField(new Field("WindDirection", DataType.FLOAT));
        aLayer.editAddField(new Field("WindSpeed", DataType.FLOAT));
        aLayer.editAddField(new Field("Weather", DataType.INT));
        aLayer.editAddField(new Field("Temperature", DataType.INT));
        aLayer.editAddField(new Field("DewPoint", DataType.INT));
        aLayer.editAddField(new Field("Pressure", DataType.INT));
        aLayer.editAddField(new Field("CloudCoverage", DataType.INT));

        for (i = 0; i < stationModelData.getDataNum(); i++) {
            StationModel sm = stationModelData.getData().get(i);
            windDir = (float) sm.getWindDirection();
            windSpeed = (float) sm.getWindSpeed();
            if (!(MIMath.doubleEquals(windDir, stationModelData.getMissingValue()))) {
                if (!(MIMath.doubleEquals(windSpeed, stationModelData.getMissingValue()))) {
                    aPoint = new PointD();
                    aPoint.X = (float) sm.getLongitude();
                    aPoint.Y = (float) sm.getLatitude();
                    weather = (int) sm.getWeather();
                    cCover = (int) sm.getCloudCover();
                    temp = (int) sm.getTemperature();
                    dewPoint = (int) sm.getDewPoint();
                    pressure = (int) sm.getPressure();
                    if (isSurface) {
                        if (!(MIMath.doubleEquals(sm.getPressure(), stationModelData.getMissingValue()))) {
                            //pressure = (int)((stationModelData[9, i] - 1000) * 10);
                            String pStr = String.valueOf((int) (sm.getPressure() * 10));
                            if (pStr.length() < 3) {
                                pressure = (int) stationModelData.getMissingValue();
                            } else {
                                pStr = pStr.substring(pStr.length() - 3);
                                pressure = Integer.parseInt(pStr);
                            }
                        }
                    }

                    aSM = Draw.calStationModel(windDir, windSpeed, 0, 12, aPoint, weather,
                            temp, dewPoint, pressure, cCover);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aSM, shapeNum)) {
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                            aLayer.editCellValue("Weather", shapeNum, weather);
                            aLayer.editCellValue("Temperature", shapeNum, temp);
                            aLayer.editCellValue("DewPoint", shapeNum, dewPoint);
                            aLayer.editCellValue("Pressure", shapeNum, pressure);
                            aLayer.editCellValue("CloudCoverage", shapeNum, cCover);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName("");
        aLayer.setLegendScheme(aLS);
        aLayer.setAvoidCollision(true);
        aLayer.setLayerDrawType(LayerDrawType.STATION_MODEL);

        return aLayer;
    }

    /**
     * Create station weather symbol layer
     *
     * @param weatherData Weather station data
     * @param layerName Layer name
     * @return Weather symbol layer
     */
    public static VectorLayer createWeatherSymbolLayer(StationData weatherData, String layerName) {
        return createWeatherSymbolLayer(weatherData, "All Weather", layerName);
    }

    /**
     * Create station weather symbol layer
     *
     * @param weatherData Weather station data
     * @param WeatherType Weatehr type
     * @param layerName Layer name
     * @return Weather symbol layer
     */
    public static VectorLayer createWeatherSymbolLayer(StationData weatherData, String WeatherType,
                                                       String layerName) {
        List<Integer> wList = getWeatherTypes(WeatherType);

        return createWeatherSymbolLayer(weatherData, wList, layerName);
    }

    /**
     * Create weather symbol station layer
     *
     * @param weatherData Weather station data
     * @param wList Weather list
     * @param layerName Layer name
     * @return VectorLayer
     */
    public static VectorLayer createWeatherSymbolLayer(StationData weatherData, List<Integer> wList,
                                                       String layerName) {
        LegendScheme aLS = createWeatherLegendScheme(wList, 20, Color.blue);
        return createWeatherSymbolLayer(weatherData, wList, aLS, layerName);
    }

    /**
     * Create weather symbol station layer
     *
     * @param weatherData Weather station data
     * @param wList Weather list
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @return Weather symbol layer
     */
    public static VectorLayer createWeatherSymbolLayer(StationData weatherData, List<Integer> wList,
                                                       LegendScheme aLS, String layerName) {
        int i;
        int weather;
        PointD aPoint;

        String columnName = "Weather";
        VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
        aLayer.editAddField(columnName, DataType.DOUBLE);

        for (i = 0; i < weatherData.getStNum(); i++) {
            weather = (int) weatherData.data[i][2];
            if (!(MIMath.doubleEquals(weather, weatherData.missingValue))) {
                if (wList.contains(weather)) {
                    aPoint = new PointD();
                    aPoint.X = weatherData.data[i][0];
                    aPoint.Y = weatherData.data[i][1];
                    PointShape aPS = new PointShape();
                    aPS.setPoint(aPoint);
                    aPS.setValue(weather);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aPS, shapeNum)) {
                            aLayer.editCellValue(columnName, shapeNum, weather);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS);
        aLayer.setLayerDrawType(LayerDrawType.STATION_POINT);

        return aLayer;
    }

    /**
     * Create weather legend scheme
     *
     * @param wList Weather inex list
     * @param size Size
     * @param color Color
     * @return Weather legend scheme
     */
    public static LegendScheme createWeatherLegendScheme(List<Integer> wList, int size, Color color) {
        LegendScheme aLS = new LegendScheme(ShapeTypes.POINT);
        aLS.setLegendType(LegendType.UNIQUE_VALUE);
        for (int w : wList) {
            PointBreak aPB = new PointBreak();
            aPB.setMarkerType(MarkerType.CHARACTER);
            aPB.setSize(size);
            aPB.setColor(color);
            aPB.setFontName("Weather");
            aPB.setStartValue(w);
            aPB.setEndValue(w);
            int charIdx = w + 28;
            if (w == 99) {
                charIdx = w + 97;
            }
            aPB.setCharIndex(charIdx);
            aPB.setCaption(String.valueOf(w));

            aLS.getLegendBreaks().add(aPB);
        }

        return aLS;
    }

    /**
     * Get weather list
     *
     * @param weatherType Weather type
     * @return Weather list
     */
    public static List<Integer> getWeatherTypes(String weatherType) {
        List<Integer> weatherList = new ArrayList<>();
        int i;
        int[] weathers;
        switch (weatherType.toLowerCase()) {
            case "all weather":
            case "all":
            default:
                weathers = new int[96];
                for (i = 4; i < 100; i++) {
                    weathers[i - 4] = i;
                }
                break;
            case "sds":
            case "dust":
                weathers = new int[]{6, 7, 8, 9, 30, 31, 32, 33, 34, 35};
                break;
            case "sds, haze":
                weathers = new int[]{5, 6, 7, 8, 9, 30, 31, 32, 33, 34, 35};
                break;
            case "smoke, haze, mist":
                weathers = new int[]{4, 5, 10};
                break;
            case "smoke":
                weathers = new int[]{4};
                break;
            case "haze":
                weathers = new int[]{5};
                break;
            case "mist":
                weathers = new int[]{10};
                break;
            case "Fog":
                weathers = new int[10];
                for (i = 40; i < 50; i++) {
                    weathers[i - 40] = i;
                }
                break;
        }

        for (int w : weathers) {
            weatherList.add(w);
        }

        return weatherList;
    }

    /**
     * Create could amount legend scheme
     *
     * @param size Size
     * @param color Color
     * @return Cloud amount legend scheme
     */
    public static LegendScheme createCloudLegendScheme(int size, Color color) {
        LegendScheme aLS = new LegendScheme(ShapeTypes.POINT);
        aLS.setLegendType(LegendType.UNIQUE_VALUE);
        int[] clouds = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        for (int w : clouds) {
            PointBreak aPB = new PointBreak();
            aPB.setMarkerType(MarkerType.CHARACTER);
            aPB.setSize(size);
            aPB.setColor(color);
            aPB.setFontName("Weather");
            aPB.setStartValue(w);
            aPB.setEndValue(w);
            int charIdx = w + 197;
            aPB.setCharIndex(charIdx);
            aPB.setCaption(String.valueOf(w));

            aLS.getLegendBreaks().add(aPB);
        }

        return aLS;
    }

    /**
     * Create mesh polygon layer
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param ls Legend scheme
     * @param lonlim Longitude limitation - to avoid the polygon cross -180/180
     * @return Mesh polygon layer
     */
    public static VectorLayer meshLayer(Array x_s, Array y_s, Array a, LegendScheme ls, double lonlim) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();

        VectorLayer layer = new VectorLayer(ShapeTypes.POLYGON);
        String fieldName = "Data";
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        layer.editAddField(aDC);

        int[] shape = x_s.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        double x1, x2, x3, x4;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                x1 = x_s.getDouble(i * colNum + j);
                x2 = x_s.getDouble(i * colNum + j + 1);
                x3 = x_s.getDouble((i + 1) * colNum + j);
                x4 = x_s.getDouble((i + 1) * colNum + j + 1);
                if (lonlim > 0) {
                    if (Math.abs(x2 - x4) > lonlim || Math.abs(x1 - x4) > lonlim
                            || Math.abs(x3 - x4) > lonlim || Math.abs(x1 - x2) > lonlim
                            || Math.abs(x2 - x3) > lonlim) {
                        continue;
                    }
                }

                PolygonShape ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(x1, y_s.getDouble(i * colNum + j)));
                points.add(new PointD(x3, y_s.getDouble((i + 1) * colNum + j)));
                points.add(new PointD(x4, y_s.getDouble((i + 1) * colNum + j + 1)));
                points.add(new PointD(x2, y_s.getDouble(i * colNum + j + 1)));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                ps.lowValue = a.getDouble(i * colNum + j);
                ps.highValue = ps.lowValue;
                int shapeNum = layer.getShapeNum();
                try {
                    if (layer.editInsertShape(ps, shapeNum)) {
                        layer.editCellValue(fieldName, shapeNum, ps.lowValue);
                    }
                } catch (Exception ex) {

                }
            }
        }
        layer.setLayerName("Mesh_Layer");
        ls.setFieldName(fieldName);
        layer.setLegendScheme(ls.convertTo(ShapeTypes.POLYGON));

        return layer;
    }

    /**
     * Create mesh polygon layer
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param ls Legend scheme
     * @return Mesh polygon layer
     */
    public static VectorLayer meshLayer(Array x_s, Array y_s, Array a, LegendScheme ls) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();

        VectorLayer layer = new VectorLayer(ShapeTypes.POLYGON);
        String fieldName = "Data";
        Field aDC = new Field(fieldName, DataType.DOUBLE);
        layer.editAddField(aDC);

        int[] shape = x_s.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        double x1, x2, x3, x4;
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
                ps.lowValue = a.getDouble(i * colNum + j);
                ps.highValue = ps.lowValue;
                int shapeNum = layer.getShapeNum();
                try {
                    if (layer.editInsertShape(ps, shapeNum)) {
                        layer.editCellValue(fieldName, shapeNum, ps.lowValue);
                    }
                } catch (Exception ex) {

                }
            }
        }
        layer.setLayerName("Mesh_Layer");
        ls.setFieldName(fieldName);
        layer.setLegendScheme(ls.convertTo(ShapeTypes.POLYGON));

        return layer;
    }

    /**
     * Create trajectory line layer
     * @param trajDataInfo ITrajDataInfo
     * @return Trajectory line layer
     */
    public static VectorLayer createTrajLineLayer(ITrajDataInfo trajDataInfo) {
        List<DataTable> dataTables = trajDataInfo.getDataTables();
        List<TrajectoryInfo> trajInfoList = trajDataInfo.getTrajInfoList();
        String xVarName = trajDataInfo.getXVarName();
        String yVarName = trajDataInfo.getYVarName();
        String zVarName = trajDataInfo.getZVarName();
        ShapeTypes shapeType = zVarName == null ? ShapeTypes.POLYLINE : ShapeTypes.POLYLINE_Z;

        VectorLayer layer = new VectorLayer(shapeType);
        layer.editAddField(new Field("TrajIndex", DataType.INT));
        layer.editAddField(new Field("TrajName", DataType.STRING));
        layer.editAddField(new Field("TrajID", DataType.STRING));
        layer.editAddField(new Field("TrajCenter", DataType.STRING));
        layer.editAddField(new Field("StartDate", DataType.DATE));
        layer.editAddField(new Field("StartLon", DataType.DOUBLE));
        layer.editAddField(new Field("StartLat", DataType.DOUBLE));
        layer.editAddField(new Field("StartHeight", DataType.DOUBLE));

        int trajIdx = 0;
        if (shapeType == ShapeTypes.POLYLINE) {
            double x, y;
            for (DataTable dataTable : dataTables) {
                TrajectoryInfo trajInfo = trajInfoList.get(trajIdx);
                List<PointD> points = new ArrayList<>();
                for (int i = 0; i < dataTable.getRowCount(); i++) {
                    x = Double.parseDouble(dataTable.getValue(i, xVarName).toString());
                    y = Double.parseDouble(dataTable.getValue(i, yVarName).toString());
                    points.add(new PointD(x, y));
                }
                PolylineShape polylineShape = new PolylineShape();
                polylineShape.setPoints(points);
                int shapeNum = layer.getShapeNum();
                try {
                    if (layer.editInsertShape(polylineShape, shapeNum)) {
                        layer.editCellValue("TrajIndex", shapeNum, trajIdx + 1);
                        layer.editCellValue("TrajName", shapeNum, trajInfo.trajName);
                        layer.editCellValue("TrajID", shapeNum, trajInfo.trajID);
                        layer.editCellValue("TrajCenter", shapeNum, trajInfo.trajCenter);
                        layer.editCellValue("StartDate", shapeNum, trajInfo.startTime);
                        layer.editCellValue("StartLat", shapeNum, trajInfo.startLat);
                        layer.editCellValue("StartLon", shapeNum, trajInfo.startLon);
                        layer.editCellValue("StartHeight", shapeNum, trajInfo.startHeight);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                trajIdx += 1;
            }
        } else {
            double x, y, z;
            for (DataTable dataTable : dataTables) {
                TrajectoryInfo trajInfo = trajInfoList.get(trajIdx);
                List<PointZ> points = new ArrayList<>();
                for (int i = 0; i < dataTable.getRowCount(); i++) {
                    x = Double.parseDouble(dataTable.getValue(i, xVarName).toString());
                    y = Double.parseDouble(dataTable.getValue(i, yVarName).toString());
                    z = Double.parseDouble(dataTable.getValue(i, zVarName).toString());
                    points.add(new PointZ(x, y, z));
                }
                PolylineZShape polylineShape = new PolylineZShape();
                polylineShape.setPoints(points);
                int shapeNum = layer.getShapeNum();
                try {
                    if (layer.editInsertShape(polylineShape, shapeNum)) {
                        layer.editCellValue("TrajIndex", shapeNum, trajIdx + 1);
                        layer.editCellValue("TrajName", shapeNum, trajInfo.trajName);
                        layer.editCellValue("TrajID", shapeNum, trajInfo.trajID);
                        layer.editCellValue("TrajCenter", shapeNum, trajInfo.trajCenter);
                        layer.editCellValue("StartDate", shapeNum, trajInfo.startTime);
                        layer.editCellValue("StartLat", shapeNum, trajInfo.startLat);
                        layer.editCellValue("StartLon", shapeNum, trajInfo.startLon);
                        layer.editCellValue("StartHeight", shapeNum, trajInfo.startHeight);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                trajIdx += 1;
            }
        }

        layer.setLayerName("Trajectory_Lines");
        layer.setLayerDrawType(LayerDrawType.TRAJECTORY_LINE);
        layer.setVisible(true);
        layer.updateLegendScheme(LegendType.UNIQUE_VALUE, "TrajID");

        return layer;
    }

    /**
     * Create trajectory line layer
     * @param trajDataInfo ITrajDataInfo
     * @param varName Variation name
     * @return Trajectory line layer
     */
    public static VectorLayer createTrajLineLayer(ITrajDataInfo trajDataInfo, String varName) {
        List<DataTable> dataTables = trajDataInfo.getDataTables();
        List<TrajectoryInfo> trajInfoList = trajDataInfo.getTrajInfoList();
        String xVarName = trajDataInfo.getXVarName();
        String yVarName = trajDataInfo.getYVarName();
        String zVarName = trajDataInfo.getZVarName();

        VectorLayer layer = new VectorLayer(ShapeTypes.POLYLINE_Z);
        layer.editAddField(new Field("TrajIndex", DataType.INT));
        layer.editAddField(new Field("TrajName", DataType.STRING));
        layer.editAddField(new Field("TrajID", DataType.STRING));
        layer.editAddField(new Field("TrajCenter", DataType.STRING));
        layer.editAddField(new Field("StartDate", DataType.DATE));
        layer.editAddField(new Field("StartLon", DataType.DOUBLE));
        layer.editAddField(new Field("StartLat", DataType.DOUBLE));
        layer.editAddField(new Field("StartHeight", DataType.DOUBLE));

        int trajIdx = 0;
        double x, y, z, m;
        for (DataTable dataTable : dataTables) {
            TrajectoryInfo trajInfo = trajInfoList.get(trajIdx);
            List<PointZ> points = new ArrayList<>();
            for (int i = 0; i < dataTable.getRowCount(); i++) {
                x = Double.parseDouble(dataTable.getValue(i, xVarName).toString());
                y = Double.parseDouble(dataTable.getValue(i, yVarName).toString());
                if (zVarName == null)
                    z = 0;
                else
                    z = Double.parseDouble(dataTable.getValue(i, zVarName).toString());
                m = Double.parseDouble(dataTable.getValue(i, varName).toString());
                points.add(new PointZ(x, y, z, m));
            }
            PolylineZShape polylineShape = new PolylineZShape();
            polylineShape.setPoints(points);
            int shapeNum = layer.getShapeNum();
            try {
                if (layer.editInsertShape(polylineShape, shapeNum)) {
                    layer.editCellValue("TrajIndex", shapeNum, trajIdx + 1);
                    layer.editCellValue("TrajName", shapeNum, trajInfo.trajName);
                    layer.editCellValue("TrajID", shapeNum, trajInfo.trajID);
                    layer.editCellValue("TrajCenter", shapeNum, trajInfo.trajCenter);
                    layer.editCellValue("StartDate", shapeNum, trajInfo.startTime);
                    layer.editCellValue("StartLat", shapeNum, trajInfo.startLat);
                    layer.editCellValue("StartLon", shapeNum, trajInfo.startLon);
                    layer.editCellValue("StartHeight", shapeNum, trajInfo.startHeight);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            trajIdx += 1;
        }

        layer.setLayerName("Trajectory_Lines");
        layer.setLayerDrawType(LayerDrawType.TRAJECTORY_LINE);
        layer.setVisible(true);
        layer.updateLegendScheme(LegendType.UNIQUE_VALUE, "TrajID");

        return layer;
    }

    public static VectorLayer createTrajPointLayer(ITrajDataInfo trajDataInfo) {
        List<DataTable> dataTables = trajDataInfo.getDataTables();
        List<Variable> variables = trajDataInfo.getVariables();
        String xVarName = trajDataInfo.getXVarName();
        String yVarName = trajDataInfo.getYVarName();
        String zVarName = trajDataInfo.getZVarName();

        VectorLayer layer = new VectorLayer(ShapeTypes.POINT_Z);
        layer.editAddField(new Field("TrajID", DataType.INT));
        for (Variable variable : variables) {
            layer.editAddField(new Field(variable.getName(), variable.getDataType()));
        }

        int trajIdx = 0;
        double x, y, z;
        for (DataTable dataTable : dataTables) {
            for (int i = 0; i < dataTable.getRowCount(); i++) {
                x = Double.parseDouble(dataTable.getValue(i, xVarName).toString());
                y = Double.parseDouble(dataTable.getValue(i, yVarName).toString());
                if (zVarName == null)
                    z = 0;
                else
                    z = Double.parseDouble(dataTable.getValue(i, zVarName).toString());
                PointZ point = new PointZ(x, y, z);
                PointZShape pointZShape = new PointZShape();
                pointZShape.setPoint(point);
                int shapeNum = layer.getShapeNum();
                try {
                    if (layer.editInsertShape(pointZShape, shapeNum)) {
                        layer.editCellValue("TrajID", shapeNum, trajIdx + 1);
                        for (Variable variable : variables) {
                            layer.editCellValue(variable.getName(), shapeNum, dataTable.getValue(i, variable.getName()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            trajIdx += 1;
        }

        layer.setLayerName("Trajectory_Points");
        layer.setLayerDrawType(LayerDrawType.TRAJECTORY_POINT);
        layer.setVisible(true);
        LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.red, 5.0F);
        aLS.setFieldName("TrajID");
        layer.setLegendScheme(aLS);

        return layer;
    }

    /**
     * Create trajectory start point layer
     * @param trajDataInfo ITrajDataInfo
     * @return Trajectory start point layer
     */
    public static VectorLayer createTrajStartPointLayer(ITrajDataInfo trajDataInfo) {
        List<DataTable> dataTables = trajDataInfo.getDataTables();
        List<TrajectoryInfo> trajInfoList = trajDataInfo.getTrajInfoList();
        String xVarName = trajDataInfo.getXVarName();
        String yVarName = trajDataInfo.getYVarName();
        String zVarName = trajDataInfo.getZVarName();

        VectorLayer layer = new VectorLayer(ShapeTypes.POINT_Z);
        layer.editAddField(new Field("TrajIndex", DataType.INT));
        layer.editAddField(new Field("TrajName", DataType.STRING));
        layer.editAddField(new Field("TrajID", DataType.STRING));
        layer.editAddField(new Field("TrajCenter", DataType.STRING));
        layer.editAddField(new Field("StartDate", DataType.DATE));
        layer.editAddField(new Field("StartLon", DataType.DOUBLE));
        layer.editAddField(new Field("StartLat", DataType.DOUBLE));
        layer.editAddField(new Field("StartHeight", DataType.DOUBLE));

        int trajIdx = 0;
        double x, y, z;
        for (DataTable dataTable : dataTables) {
            TrajectoryInfo trajInfo = trajInfoList.get(trajIdx);
            List<PointZ> points = new ArrayList<>();
            x = Double.parseDouble(dataTable.getValue(0, xVarName).toString());
            y = Double.parseDouble(dataTable.getValue(0, yVarName).toString());
            if (zVarName == null)
                z = 0;
            else
                z = Double.parseDouble(dataTable.getValue(0, zVarName).toString());
            PointZShape pointZShape = new PointZShape();
            pointZShape.setPoint(new PointZ(x, y, z));
            int shapeNum = layer.getShapeNum();
            try {
                if (layer.editInsertShape(pointZShape, shapeNum)) {
                    layer.editCellValue("TrajIndex", shapeNum, trajIdx + 1);
                    layer.editCellValue("TrajName", shapeNum, trajInfo.trajName);
                    layer.editCellValue("TrajID", shapeNum, trajInfo.trajID);
                    layer.editCellValue("TrajCenter", shapeNum, trajInfo.trajCenter);
                    layer.editCellValue("StartDate", shapeNum, trajInfo.startTime);
                    layer.editCellValue("StartLat", shapeNum, trajInfo.startLat);
                    layer.editCellValue("StartLon", shapeNum, trajInfo.startLon);
                    layer.editCellValue("StartHeight", shapeNum, trajInfo.startHeight);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            trajIdx += 1;
        }

        layer.setLayerName("Trajectory_Start_Points");
        layer.setLayerDrawType(LayerDrawType.TRAJECTORY_POINT);
        layer.setVisible(true);
        LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.black, 8.0F);
        aLS.setFieldName("TrajID");
        layer.setLegendScheme(aLS);

        return layer;
    }
    // </editor-fold>
}
