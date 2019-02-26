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
package org.meteoinfo.geoprocess;

import org.meteoinfo.global.Direction;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.map.GridLabel;
import org.meteoinfo.shape.Line;
import org.meteoinfo.shape.Polygon;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.Polyline;
import org.meteoinfo.shape.PolylineShape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.shape.CircleShape;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.Shape;

/**
 * GeoComputation class
 *
 * @author Yaqiang Wang
 */
public class GeoComputation {

    private static final double EARTH_RADIUS = 6378.137;

    // <editor-fold desc="General">        
    /**
     * Determine if a point array is clockwise
     *
     * @param pointList point list
     * @return boolean
     */
    public static boolean isClockwise(List<? extends PointD> pointList) {
        int i;
        PointD aPoint;
        double yMax = 0;
        int yMaxIdx = 0;
        for (i = 0; i < pointList.size() - 1; i++) {
            aPoint = pointList.get(i);
            if (i == 0) {
                yMax = aPoint.Y;
                yMaxIdx = 0;
            } else {
                if (yMax < aPoint.Y) {
                    yMax = aPoint.Y;
                    yMaxIdx = i;
                }
            }
        }
        PointD p1, p2, p3;
        int p1Idx, p2Idx, p3Idx;
        p1Idx = yMaxIdx - 1;
        p2Idx = yMaxIdx;
        p3Idx = yMaxIdx + 1;
        if (yMaxIdx == 0) {
            p1Idx = pointList.size() - 2;
        }

        p1 = pointList.get(p1Idx);
        p2 = pointList.get(p2Idx);
        p3 = pointList.get(p3Idx);
        return (p3.X - p1.X) * (p2.Y - p1.Y) - (p2.X - p1.X) * (p3.Y - p1.Y) > 0;

    }

    /**
     * Determine if a point array is clockwise
     *
     * @param points point array
     * @return boolean
     */
    public static boolean isClockwise(PointD[] points) {
        List<PointD> pointList = Arrays.asList(points);
        return isClockwise(pointList);
    }

    /**
     * Determine if a point is in a polygon
     *
     * @param poly Polygon border points
     * @param aPoint The point
     * @return If the point is in the polygon
     */
    public static boolean pointInPolygon(List<? extends PointD> poly, PointD aPoint) {
        double xNew, yNew, xOld, yOld;
        double x1, y1, x2, y2;
        int i;
        boolean inside = false;
        int nPoints = poly.size();

        if (nPoints < 3) {
            return false;
        }

        xOld = (poly.get(nPoints - 1)).X;
        yOld = (poly.get(nPoints - 1)).Y;
        for (i = 0; i < nPoints; i++) {
            xNew = (poly.get(i)).X;
            yNew = (poly.get(i)).Y;
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                y1 = yOld;
                y2 = yNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                y1 = yNew;
                y2 = yOld;
            }

            //---- edge "open" at left end
            if ((xNew < aPoint.X) == (aPoint.X <= xOld)
                    && (aPoint.Y - y1) * (x2 - x1) < (y2 - y1) * (aPoint.X - x1)) {
                inside = !inside;
            }

            xOld = xNew;
            yOld = yNew;
        }

        return inside;
    }

    /**
     * Determine if a point is in a polygon
     *
     * @param aPolygon The polygon
     * @param aPoint The point
     * @return Boolean
     */
    public static boolean pointInPolygon(PolygonShape aPolygon, PointD aPoint) {
        if (!MIMath.pointInExtent(aPoint, aPolygon.getExtent())) {
            return false;
        }

        if (aPolygon instanceof CircleShape) {
            return ((CircleShape) aPolygon).contains(aPoint);
        }

        boolean isIn = false;
        for (int i = 0; i < aPolygon.getPolygons().size(); i++) {
            Polygon aPRing = aPolygon.getPolygons().get(i);
            isIn = pointInPolygon(aPRing.getOutLine(), aPoint);
            if (isIn) {
                if (aPRing.hasHole()) {
                    for (List<? extends PointD> aLine : aPRing.getHoleLines()) {
                        if (pointInPolygon(aLine, aPoint)) {
                            isIn = false;
                            break;
                        }
                    }
                }
            }

            if (isIn) {
                return isIn;
            }
        }

        return isIn;
    }

    /**
     * Determine if a point is in a polygon
     *
     * @param aPolygon The polygon
     * @param x X
     * @param y Y
     * @return Boolean
     */
    public static boolean pointInPolygon(PolygonShape aPolygon, double x, double y) {
        return pointInPolygon(aPolygon, new PointD(x, y));
    }

    /**
     * Determine if a point is in a polygon
     *
     * @param aPolygon The polygon
     * @param aPoint The point
     * @return Boolean
     */
    public static boolean pointInPolygon(Polygon aPolygon, PointD aPoint) {
        if (!MIMath.pointInExtent(aPoint, aPolygon.getExtent())) {
            return false;
        }

        if (aPolygon.hasHole()) {
            boolean isIn = pointInPolygon(aPolygon.getOutLine(), aPoint);
            if (isIn) {
                for (List<? extends PointD> aLine : aPolygon.getHoleLines()) {
                    if (pointInPolygon(aLine, aPoint)) {
                        isIn = false;
                        break;
                    }
                }
            }

            return isIn;
        } else {
            return pointInPolygon(aPolygon.getOutLine(), aPoint);
        }
    }

    /**
     * Determine if a point located in polygons
     *
     * @param polygons The polygons
     * @param aPoint The point
     * @return Boolean
     */
    public static boolean pointInPolygons(List<PolygonShape> polygons, PointD aPoint) {
        boolean isIn = false;
        Extent ext = MIMath.getExtent(polygons);
        if (MIMath.pointInExtent(aPoint, ext)) {
            for (PolygonShape aPGS : polygons) {
                if (pointInPolygon(aPGS, aPoint)) {
                    isIn = true;
                    break;
                }
            }
        }

        return isIn;
    }

    /**
     * Determine if a point loacted in a polygon layer
     *
     * @param aLayer The polygon layer
     * @param aPoint The point
     * @param onlySel If check only selected shapes
     * @return Inside or outside
     */
    public static boolean pointInPolygonLayer(VectorLayer aLayer, PointD aPoint, boolean onlySel) {
        if (!MIMath.pointInExtent(aPoint, aLayer.getExtent())) {
            return false;
        }

        List<PolygonShape> polygons = new ArrayList<>();
        if (onlySel) {
            for (Shape aShape : aLayer.getShapes()) {
                if (aShape.isSelected()) {
                    polygons.add((PolygonShape) aShape);
                }
            }
        } else {
            for (Shape aShape : aLayer.getShapes()) {
                polygons.add((PolygonShape) aShape);
            }
        }

        return pointInPolygons(polygons, aPoint);
    }

    /**
     * Calculate the distance between point and a line segment
     *
     * @param point The point
     * @param pt1 End point of the line segment
     * @param pt2 End point of the line segment
     * @return Distance
     */
    public static double dis_PointToLine(PointD point, PointD pt1, PointD pt2) {
        double dis;
        if (MIMath.doubleEquals(pt2.X, pt1.X)) {
            dis = Math.abs(point.X - pt1.X);
        } else if (MIMath.doubleEquals(pt2.Y, pt1.Y)) {
            dis = Math.abs(point.Y - pt1.Y);
        } else {
            double k = (pt2.Y - pt1.Y) / (pt2.X - pt1.X);
            double x = (k * k * pt1.X + k * (point.Y - pt1.Y) + point.X) / (k * k + 1);
            double y = k * (x - pt1.X) + pt1.Y;
            //double dis = Math.sqrt((point.Y - y) * (point.Y - y) + (point.X - x) * (point.X - x));
            dis = distance(point, new PointD(x, y));
        }
        return dis;
    }

    /**
     * Get distance between two points
     *
     * @param pt1 Point one
     * @param pt2 Point two
     * @return Distance
     */
    public static double distance(PointD pt1, PointD pt2) {
        return Math.sqrt((pt2.Y - pt1.Y) * (pt2.Y - pt1.Y) + (pt2.X - pt1.X) * (pt2.X - pt1.X));
    }

    /**
     * Select polyline shape by a point
     *
     * @param sp The point
     * @param aPLS The polyline shape
     * @param buffer Buffer
     * @return Is the polyline shape selected
     */
    public static Object selectPolylineShape(PointD sp, PolylineShape aPLS, double buffer) {
        Extent aExtent = new Extent();
        aExtent.minX = sp.X - buffer;
        aExtent.maxX = sp.X + buffer;
        aExtent.minY = sp.Y - buffer;
        aExtent.maxY = sp.Y + buffer;
        double dis;
        if (MIMath.isExtentCross(aExtent, aPLS.getExtent())) {
            for (int j = 0; j < aPLS.getPointNum(); j++) {
                PointD aPoint = aPLS.getPoints().get(j);
                if (MIMath.pointInExtent(aPoint, aExtent)) {
                    return GeoComputation.distance(sp, aPoint);
                }
                if (j < aPLS.getPointNum() - 1) {
                    PointD bPoint = aPLS.getPoints().get(j + 1);
                    if (Math.abs(sp.Y - aPoint.Y) <= Math.abs(bPoint.Y - aPoint.Y)
                            || Math.abs(sp.X - aPoint.X) <= Math.abs(bPoint.X - aPoint.X)) {
                        dis = GeoComputation.dis_PointToLine(sp, aPoint, bPoint);
                        if (dis < aExtent.getWidth()) {
                            return dis;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Select polyline shape by a point
     *
     * @param sp The point
     * @param points The point list
     * @param buffer Buffer
     * @return Is the polyline shape selected
     */
    public static Object selectPolyline(PointD sp, List<PointD> points, double buffer) {
        Extent aExtent = new Extent();
        aExtent.minX = sp.X - buffer;
        aExtent.maxX = sp.X + buffer;
        aExtent.minY = sp.Y - buffer;
        aExtent.maxY = sp.Y + buffer;
        Extent bExtent = MIMath.getPointsExtent(points);
        double dis;
        if (MIMath.isExtentCross(aExtent, bExtent)) {
            for (int j = 0; j < points.size(); j++) {
                PointD aPoint = points.get(j);
                if (MIMath.pointInExtent(aPoint, aExtent)) {
                    return GeoComputation.distance(sp, aPoint);
                }
                if (j < points.size() - 1) {
                    PointD bPoint = points.get(j + 1);
                    if (Math.abs(sp.Y - aPoint.Y) <= Math.abs(bPoint.Y - aPoint.Y)
                            || Math.abs(sp.X - aPoint.X) <= Math.abs(bPoint.X - aPoint.X)) {
                        dis = GeoComputation.dis_PointToLine(sp, aPoint, bPoint);
                        if (dis < aExtent.getWidth()) {
                            return new Object[]{j + 1, dis};
                        }
                    }
                }
            }
        }

        return null;
    }

    // </editor-fold>
    // <editor-fold desc="Earth">
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * Get polygon area
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param isLonLat If is on earth surface (lon/lat)
     * @return Area
     */
    public static double getArea(List<Number> x, List<Number> y, boolean isLonLat) {
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            points.add(new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()));
        }

        return getArea(points, isLonLat);
    }

    /**
     * Get polygon area on earth surface
     *
     * @param points point list
     * @param isLonLat if is lon/lat
     * @return area
     */
    public static double getArea(List<? extends PointD> points, boolean isLonLat) {

        int Count = points.size();
        if (Count > 2) {
            double mtotalArea = 0;

            if (isLonLat) {
                return sphericalPolygonArea(points);
            } else {
                int i, j;
                double p1x, p1y;
                double p2x, p2y;
                for (i = Count - 1, j = 0; j < Count; i = j, j++) {

                    p1x = points.get(i).X;
                    p1y = points.get(i).Y;

                    p2x = points.get(j).X;
                    p2y = points.get(j).Y;

                    mtotalArea += p1x * p2y - p2x * p1y;
                }
                mtotalArea /= 2.0;

                if (mtotalArea < 0) {
                    mtotalArea = -mtotalArea;
                }

                return mtotalArea;
            }
        }
        return 0;
    }

    /**
     * Get polygon area on earth surface
     *
     * @param points point list
     * @return area
     */
    public static double getArea(List<? extends PointD> points) {
        return getArea(points, false);
    }

    /**
     * Get polygon area on earth surface
     *
     * @param points point list
     * @return area
     */
    public static double calArea(List<PointD> points) {
        if (points.size() < 3) {
            return 0.0;
        }

        double sum = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            double bx = points.get(i).X;
            double by = points.get(i).Y;
            double cx = points.get(i + 1).X;
            double cy = points.get(i + 1).Y;
            sum += (bx + cx) * (cy - by);
        }
        return -sum / 2.0;
    }

    /**
     * Compute the Area of a Spherical Polygon
     *
     * @param points lon/lat point list
     * @return area
     */
    public static double sphericalPolygonArea(List<? extends PointD> points) {
        return sphericalPolygonArea(points, EARTH_RADIUS * 1000);
    }

    /**
     * Compute the Area of a Spherical Polygon
     *
     * @param points lon/lat point list
     * @param r spherical radius
     * @return area
     */
    public static double sphericalPolygonArea(List<? extends PointD> points, double r) {
        double[] lat = new double[points.size()];
        double[] lon = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            lon[i] = rad(points.get(i).X);
            lat[i] = rad(points.get(i).Y);
        }

        return sphericalPolygonArea(lat, lon, r);
    }

    /**
     * Haversine function : hav(x) = (1-cos(x))/2
     *
     * @param x
     * @return Returns the value of Haversine function
     */
    public static double haversine(double x) {
        return (1.0 - Math.cos(x)) / 2.0;
    }

    /**
     * Compute the Area of a Spherical Polygon
     *
     * @param lat the latitudes of all vertices(in radian)
     * @param lon the longitudes of all vertices(in radian)
     * @param r spherical radius
     * @return Returns the area of a spherical polygon
     */
    public static double sphericalPolygonArea(double[] lat, double[] lon, double r) {
        double lam1, lam2 = 0, beta1, beta2 = 0, cosB1, cosB2 = 0;
        double hav;
        double sum = 0;

        for (int j = 0; j < lat.length; j++) {
            //int k = j + 1;
            if (j == 0) {
                lam1 = lon[j];
                beta1 = lat[j];
                lam2 = lon[j + 1];
                beta2 = lat[j + 1];
                cosB1 = Math.cos(beta1);
                cosB2 = Math.cos(beta2);
            } else {
                int k = (j + 1) % lat.length;
                lam1 = lam2;
                beta1 = beta2;
                lam2 = lon[k];
                beta2 = lat[k];
                cosB1 = cosB2;
                cosB2 = Math.cos(beta2);
            }
            if (lam1 != lam2) {
                hav = haversine(beta2 - beta1)
                        + cosB1 * cosB2 * haversine(lam2 - lam1);
                double a = 2 * Math.asin(Math.sqrt(hav));
                double b = Math.PI / 2 - beta2;
                double c = Math.PI / 2 - beta1;
                double s = 0.5 * (a + b + c);
                double t = Math.tan(s / 2) * Math.tan((s - a) / 2)
                        * Math.tan((s - b) / 2) * Math.tan((s - c) / 2);

                double excess = Math.abs(4 * Math.atan(Math.sqrt(
                        Math.abs(t))));

                if (lam2 < lam1) {
                    excess = -excess;
                }

                sum += excess;
            }
        }
        return Math.abs(sum) * r * r;
    }

    /**
     * Get distance
     *
     * @param points Point list
     * @param isLonLat If is lon/lat
     * @return Distance
     */
    public static double getDistance(List<? extends PointD> points, boolean isLonLat) {
        double tdis = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            double ax = points.get(i).X;
            double ay = points.get(i).Y;
            double bx = points.get(i + 1).X;
            double by = points.get(i + 1).Y;
            double dx = Math.abs(bx - ax);
            double dy = Math.abs(by - ay);
            double dist;
            if (isLonLat) {
                double y = (by + ay) / 2;
                double factor = Math.cos(y * Math.PI / 180);
                dx *= factor;
                dist = Math.sqrt(dx * dx + dy * dy);
                dist = dist * 111319.5;
            } else {
                dist = Math.sqrt(dx * dx + dy * dy);
            }

            tdis += dist;
        }

        return tdis;
    }

    /**
     * Get distance
     *
     * @param xx X coordinates
     * @param yy Y coordinates
     * @param isLonLat If is lon/lat
     * @return Distance
     */
    public static double getDistance(List<Number> xx, List<Number> yy, boolean isLonLat) {
        double tdis = 0.0;
        for (int i = 0; i < xx.size() - 1; i++) {
            double ax = xx.get(i).doubleValue();
            double ay = yy.get(i).doubleValue();
            double bx = xx.get(i + 1).doubleValue();
            double by = yy.get(i + 1).doubleValue();
            double dx = Math.abs(bx - ax);
            double dy = Math.abs(by - ay);
            double dist;
            if (isLonLat) {
                double y = (by + ay) / 2;
                double factor = Math.cos(y * Math.PI / 180);
                dx *= factor;
                dist = Math.sqrt(dx * dx + dy * dy);
                dist = dist * 111319.5;
            } else {
                dist = Math.sqrt(dx * dx + dy * dy);
            }

            tdis += dist;
        }

        return tdis;
    }
    // </editor-fold>

    // <editor-fold desc="Cipping">
    /**
     * Clip a vector layer by a polygon shape
     *
     * @param subjectLayer Subject vector layer
     * @param clipObject Cipping object
     * @return Result clipped shapes
     */
    public static List<Shape> clipLayer(VectorLayer subjectLayer, Object clipObject) {
        List<Shape> clippedShapes = new ArrayList<>();
        for (int i = 0; i < subjectLayer.getShapeNum(); i++) {
            Shape bShape = subjectLayer.getShapes().get(i);
            Shape clippedShape = clipShape(bShape, clipObject);
            if (clippedShape != null) {
                clippedShapes.add(clippedShape);
            }
        }

        return clippedShapes;
    }

//    /**
//     * Clip a vector layer by polygon shape list
//     *
//     * @param subjectLayer Subject vector layer
//     * @param polygonShapes Cipping polygon shape list
//     * @return Result clipped layer
//     */
//    public static VectorLayer clipLayer(VectorLayer subjectLayer, List<PolygonShape> polygonShapes) {
//        VectorLayer newLayer = (VectorLayer) subjectLayer.cloneValue();
//        DataTable aTable = new DataTable();
//        for (DataColumn aDC : subjectLayer.getAttributeTable().getTable().getColumns()) {
//            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
//            aTable.getColumns().add(bDC);
//        }
//
//        newLayer.setShapes(new ArrayList<Shape>());
//        for (PolygonShape polygonShape : polygonShapes) {
//            for (int i = 0; i < subjectLayer.getShapeNum(); i++) {
//                Shape bShape = subjectLayer.getShapes().get(i);
//                DataRow aDR = subjectLayer.getAttributeTable().getTable().getRows().get(i);
//                for (Polygon aPolygon : polygonShape.getPolygons()) {
//                    Shape clippedShape = clipShape(bShape, aPolygon.getOutLine());
//                    if (clippedShape != null) {
//                        newLayer.addShape(clippedShape);
//                        try {
//                            aTable.addRow((DataRow) aDR.clone());
//                        } catch (Exception ex) {
//                            Logger.getLogger(GeoComputation.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            }
//        }
//        newLayer.getAttributeTable().setTable(aTable);
//        
//        return newLayer;
//    }
    /**
     * Clip a vector layer by polygon shape list
     *
     * @param subjectLayer Subject vector layer
     * @param clipObjects Cipping object list
     * @return Result clipped layer
     */
    public static VectorLayer clipLayer(VectorLayer subjectLayer, List<Object> clipObjects) {
        VectorLayer newLayer = (VectorLayer) subjectLayer.cloneValue();
        DataTable aTable = new DataTable();
        for (DataColumn aDC : subjectLayer.getAttributeTable().getTable().getColumns()) {
            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
            aTable.getColumns().add(bDC);
        }

        newLayer.setShapes(new ArrayList<Shape>());
        for (Object clipObject : clipObjects) {
            for (int i = 0; i < subjectLayer.getShapeNum(); i++) {
                Shape bShape = subjectLayer.getShapes().get(i);
                DataRow aDR = subjectLayer.getAttributeTable().getTable().getRows().get(i);
                Shape clippedShape = clipShape(bShape, clipObject);
                if (clippedShape != null) {
                    newLayer.addShape(clippedShape);
                    try {
                        aTable.addRow((DataRow) aDR.clone());
                    } catch (Exception ex) {
                        Logger.getLogger(GeoComputation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        newLayer.getAttributeTable().setTable(aTable);

        return newLayer;
    }

    /**
     * Clip a shape
     *
     * @param aShape The shape
     * @param clipObj Clipping object
     * @return Clipped shape
     */
    public static Shape clipShape(Shape aShape, Object clipObj) {
        switch (aShape.getShapeType()) {
            case Point:
            case WeatherSymbol:
            case WindArraw:
            case WindBarb:
            case StationModel:
                return clipPointShape((PointShape) aShape, clipObj);
            case Polyline:
                return clipPolylineShape((PolylineShape) aShape, clipObj);
            case Polygon:
            case PolygonM:
                return clipPolygonShape((PolygonShape) aShape, clipObj);
            default:
                return null;
        }
    }

    /**
     * Clip point shape with a clipping object
     *
     * @param aPS The point shape
     * @param clipObj Clipping object
     * @return Clipped point shape
     */
    public static PointShape clipPointShape(PointShape aPS, Object clipObj) {
        if (pointInClipObj(clipObj, aPS.getPoint())) {
            return aPS;
        } else {
            return null;
        }
    }

    /**
     * Clip polyline shape with a clipping object
     *
     * @param aPLS The polyline shape
     * @param clipObj Clipping object
     * @return Clipped polyline shape
     */
    public static PolylineShape clipPolylineShape(PolylineShape aPLS, Object clipObj) {
        List<Polyline> polyLines = clipPolylines(aPLS.getPolylines(), clipObj);
        if (polyLines.isEmpty()) {
            return null;
        } else {
            PolylineShape bPLS = (PolylineShape) aPLS.valueClone();
            bPLS.setPolylines(polyLines);

            return bPLS;
        }
    }

    /**
     * Clip polyline shape with a longitude
     *
     * @param aPLS Polyline shape
     * @param lon Longitude
     * @return Clipped polyline shape
     */
    public static PolylineShape clipPolylineShape_Lon(PolylineShape aPLS, double lon) {
        List<Polyline> polylines = new ArrayList<>();
        ClipLine clipLine = new ClipLine();
        clipLine.setLongitude(true);
        clipLine.setValue(lon - 0.0001);
        clipLine.setLeftOrTop(true);
        polylines.addAll(clipPolylines(aPLS.getPolylines(), clipLine));

        clipLine.setValue(lon + 0.0001);
        clipLine.setLeftOrTop(false);
        polylines.addAll(clipPolylines(aPLS.getPolylines(), clipLine));

        PolylineShape bPLS = (PolylineShape) aPLS.valueClone();
        bPLS.setPolylines(polylines);

        return bPLS;
    }

    /**
     * Clip polyline shape with a longitude
     *
     * @param aPLS Polyline shape
     * @param lat The latitude
     * @return Clipped polyline shape
     */
    public static PolylineShape clipPolylineShape_Lat(PolylineShape aPLS, double lat) {
        List<Polyline> polylines = new ArrayList<>();
        ClipLine clipLine = new ClipLine();
        clipLine.setLongitude(false);
        clipLine.setValue(lat + 0.0001);
        clipLine.setLeftOrTop(true);
        polylines.addAll(clipPolylines(aPLS.getPolylines(), clipLine));

        clipLine.setValue(lat - 0.0001);
        clipLine.setLeftOrTop(false);
        polylines.addAll(clipPolylines(aPLS.getPolylines(), clipLine));

        PolylineShape bPLS = (PolylineShape) aPLS.valueClone();
        bPLS.setPolylines(polylines);

        return bPLS;
    }

    /**
     * Clip polyline shape with a longitude
     *
     * @param aPLS Polyline shape
     * @param lat Latitude
     * @param isTop If is top
     * @return Clipped polyline shape
     */
    public static PolylineShape clipPolylineShape_Lat(PolylineShape aPLS, double lat, boolean isTop) {
        List<Polyline> polylines = new ArrayList<>();
        ClipLine clipLine = new ClipLine();
        clipLine.setLongitude(false);
        clipLine.setValue(lat);
        clipLine.setLeftOrTop(isTop);
        polylines.addAll(clipPolylines(aPLS.getPolylines(), clipLine));

        PolylineShape bPLS = (PolylineShape) aPLS.valueClone();
        bPLS.setPolylines(polylines);

        return bPLS;
    }

    /**
     * Clip polylines with a clipping object
     *
     * @param polyLines Polyline list
     * @param clipObj Clipping object
     * @return Clipped polylines
     */
    private static List<Polyline> clipPolylines(List<? extends Polyline> polyLines, Object clipObj) {
        List<Polyline> newPolyLines = new ArrayList<>();
        for (Polyline aPolyLine : polyLines) {
            newPolyLines.addAll(clipPolyline(aPolyLine, clipObj));
        }

        return newPolyLines;
    }

    private static List<? extends Polyline> clipPolyline(Polyline inPolyLine, Object clipObj) {
        List<Polyline> newPolylines = new ArrayList<>();
        List<PointD> aPList = (List<PointD>) inPolyLine.getPointList();

        if (!isExtentCross(inPolyLine.getExtent(), clipObj)) {
            return newPolylines;
        }

        int i, j;

        if (clipObj instanceof List) {
            if (!isClockwise((List<PointD>) clipObj)) {
                Collections.reverse((List<PointD>) clipObj);
            }
        } else if (clipObj.getClass() == ClipLine.class) {
            if (((ClipLine) clipObj).isExtentInside(inPolyLine.getExtent())) {
                newPolylines.add(inPolyLine);
                return newPolylines;
            }
        }

        //Judge if all points of the polyline are in the cut polygon - outline   
        List<List<PointD>> newLines = new ArrayList<>();
        if (pointInClipObj(clipObj, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!pointInClipObj(clipObj, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            if (!isAllIn) //Put start point outside of the cut polygon
            {
                if (inPolyLine.isClosed()) {
                    List<PointD> bPList = new ArrayList<>();
                    bPList.addAll(aPList.subList(notInIdx, aPList.size()));
                    bPList.addAll(aPList.subList(0, notInIdx));

                    bPList.add(bPList.get(0));
                    newLines.add(bPList);
                } else {
                    Collections.reverse(aPList);
                    newLines.add(aPList);
                }
            } else //the input polygon is inside the cut polygon
            {
                newPolylines.add(inPolyLine);
                return newPolylines;
            }
        } else {
            newLines.add(aPList);
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP = new BorderPoint();
        List<PointD> clipPList = getClipPointList(clipObj);
        for (PointD aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = aP;
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting                     
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = pointInClipObj(clipObj, aPList.get(0));
            PointD q1, q2, p1, p2, IPoint = new PointD();
            Line lineA, lineB;
            List<PointD> newPlist = new ArrayList<>();
            Polyline bLine;
            p1 = aPList.get(0);
            int inIdx = -1, outIdx = -1;
            boolean newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = aPList.get(i);
                if (pointInClipObj(clipObj, p2)) {
                    if (!isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = borderList.get(0).Point;
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = IPoint;
                                borderList.add(j, aBP);
                                inIdx = j;
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);
                    }
                    newPlist.add(aPList.get(i));
                    isInPolygon = true;
                } else {
                    if (isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = borderList.get(0).Point;
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                if (!newLine) {
                                    if (inIdx - outIdx >= 1 && inIdx - outIdx <= 10) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(outIdx, aBP);
                                        }
                                    } else if (inIdx - outIdx <= -1 && inIdx - outIdx >= -10) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(outIdx + 1, aBP);
                                        }
                                    } else if (inIdx == outIdx) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(inIdx + 1, aBP);
                                        }
                                    }
                                }
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = IPoint;
                                borderList.add(j, aBP);
                                outIdx = j;
                                a1 = inIdx;

                                newLine = false;
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);

                        bLine = new Polyline();
                        bLine.setPointList(new ArrayList<>(newPlist));
                        newPolylines.add(bLine);

                        isInPolygon = false;
                        newPlist = new ArrayList<>();
                    }
                }
                p1 = p2;
            }

            if (isInPolygon && newPlist.size() > 1) {
                bLine = new Polyline();
                bLine.setPointList(new ArrayList<>(newPlist));
                newPolylines.add(bLine);
            }
        }

        return newPolylines;
    }

    /**
     * Clip polygon shape with a clipping object
     *
     * @param aPGS Polygon shape
     * @param clipObj Clipping object
     * @return Clipped polygon shape
     */
    public static PolygonShape clipPolygonShape(PolygonShape aPGS, Object clipObj) {
        List<Polygon> polygons = clipPolygons(aPGS.getPolygons(), clipObj);
        if (polygons.isEmpty()) {
            return null;
        } else {
            PolygonShape bPGS = aPGS.valueClone();
            bPGS.setPolygons(polygons);

            return bPGS;
        }
    }

    /**
     * Clip polygon shape with a longitude
     *
     * @param aPGS Polygon shape
     * @param lon Longitude
     * @return Clipped polygon shape
     */
    public static PolygonShape clipPolygonShape_Lon(PolygonShape aPGS, double lon) {
        List<Polygon> polygons = new ArrayList<>();
        ClipLine clipLine = new ClipLine();
        clipLine.setLongitude(true);
        clipLine.setValue(lon - 0.0001);
        clipLine.setLeftOrTop(true);
        polygons.addAll(clipPolygons(aPGS.getPolygons(), clipLine));

        clipLine.setValue(lon + 0.0001);
        clipLine.setLeftOrTop(false);
        polygons.addAll(clipPolygons(aPGS.getPolygons(), clipLine));

        PolygonShape bPGS = aPGS.valueClone();
        bPGS.setPolygons(polygons);

        return bPGS;
    }

    /**
     * Clip polygon shape with a latitude
     *
     * @param aPGS Polygon shape
     * @param lat Latitude
     * @return Clipped polygon shape
     */
    public static PolygonShape clipPolygonShape_Lat(PolygonShape aPGS, double lat) {
        List<Polygon> polygons = new ArrayList<>();
        ClipLine clipLine = new ClipLine();
        clipLine.setLongitude(false);
        clipLine.setValue(lat + 0.0001);
        clipLine.setLeftOrTop(true);
        polygons.addAll(clipPolygons(aPGS.getPolygons(), clipLine));

        clipLine.setValue(lat - 0.0001);
        clipLine.setLeftOrTop(false);
        polygons.addAll(clipPolygons(aPGS.getPolygons(), clipLine));

        PolygonShape bPGS = aPGS.valueClone();
        bPGS.setPolygons(polygons);

        return bPGS;
    }

    /**
     * Clip polygon shape with a latitude
     *
     * @param aPGS Polygon shape
     * @param lat Latitude
     * @param isTop If is top
     * @return Clipped polygon shape
     */
    public static PolygonShape clipPolygonShape_Lat(PolygonShape aPGS, double lat, boolean isTop) {
        List<Polygon> polygons = new ArrayList<>();
        ClipLine clipLine = new ClipLine();
        clipLine.setLongitude(false);
        clipLine.setValue(lat);
        clipLine.setLeftOrTop(isTop);
        polygons.addAll(clipPolygons(aPGS.getPolygons(), clipLine));

        PolygonShape bPGS = aPGS.valueClone();
        bPGS.setPolygons(polygons);

        return bPGS;
    }

    /**
     * Clip polygons with a clipping object
     *
     * @param polygons Polygon list
     * @param clipObj Clipping object
     * @return Clipped polygons
     */
    private static List<Polygon> clipPolygons(List<? extends Polygon> polygons, Object clipObj) {
        List<Polygon> newPolygons = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            Polygon aPolygon = polygons.get(i);
            if (clipObj instanceof Extent) {
                newPolygons.addAll(clipPolygon_Extent(aPolygon, (Extent) clipObj));
            } else {
                newPolygons.addAll(clipPolygon(aPolygon, clipObj));
            }
        }

        return newPolygons;
    }

    private static List<Polygon> clipPolygon(Polygon inPolygon, Object clipObj) {
        List<Polygon> newPolygons = new ArrayList<>();
        List<Polyline> newPolylines = new ArrayList<>();
        List<PointD> aPList = (List<PointD>) inPolygon.getOutLine();

        if (!isExtentCross(inPolygon.getExtent(), clipObj)) {
            return newPolygons;
        }

        int i, j;

        if (clipObj instanceof List) {
            if (!isClockwise((List<PointD>) clipObj)) {
                Collections.reverse((List<PointD>) clipObj);
            }
        } else if (clipObj.getClass() == ClipLine.class) {
            if (((ClipLine) clipObj).isExtentInside(inPolygon.getExtent())) {
                newPolygons.add(inPolygon);
                return newPolygons;
            }
        }

        //Judge if all points of the polyline are in the cut polygon - outline   
        List<List<PointD>> newLines = new ArrayList<>();
        if (pointInClipObj(clipObj, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!pointInClipObj(clipObj, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            if (!isAllIn) //Put start point outside of the cut polygon
            {
                List<PointD> bPList = new ArrayList<>();
                bPList.addAll(aPList.subList(notInIdx, aPList.size()));
                bPList.addAll(aPList.subList(1, notInIdx));

                bPList.add(bPList.get(0));
                newLines.add(bPList);
            } else //the input polygon is inside the cut polygon
            {
                newPolygons.add(inPolygon);
                return newPolygons;
            }
        } else {
            newLines.add(aPList);
        }

        //Holes
        List<List<PointD>> holeLines = new ArrayList<>();
        if (inPolygon.hasHole()) {
            for (int h = 0; h < inPolygon.getHoleLines().size(); h++) {
                List<PointD> holePList = (List<PointD>) inPolygon.getHoleLines().get(h);
                Extent plExtent = MIMath.getPointsExtent(holePList);
                if (!isExtentCross(plExtent, clipObj)) {
                    continue;
                }

                if (pointInClipObj(clipObj, holePList.get(0))) {
                    boolean isAllIn = true;
                    int notInIdx = 0;
                    for (i = 0; i < holePList.size(); i++) {
                        if (!pointInClipObj(clipObj, holePList.get(i))) {
                            notInIdx = i;
                            isAllIn = false;
                            break;
                        }
                    }
                    if (!isAllIn) //Put start point outside of the cut polygon
                    {
                        List<PointD> bPList = new ArrayList<>();
                        bPList.addAll(holePList.subList(notInIdx, holePList.size()));
                        bPList.addAll(holePList.subList(1, notInIdx));

                        bPList.add(bPList.get(0));
                        newLines.add(bPList);
                    } else //the hole is inside the cut polygon
                    {
                        holeLines.add((List<PointD>) inPolygon.getHoleLines().get(h));
                    }
                } else {
                    newLines.add(holePList);
                }
            }
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP = new BorderPoint();
        List<PointD> clipPList = getClipPointList(clipObj);
        for (PointD aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = (PointD) aP.clone();
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting                     
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = false;
            PointD q1, q2, p1, p2, IPoint = new PointD();
            Line lineA, lineB;
            List<PointD> newPlist = new ArrayList<>();
            Polyline bLine;
            p1 = (PointD) aPList.get(0).clone();
            int inIdx = -1, outIdx = -1;
            boolean newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = (PointD) aPList.get(i).clone();
                if (pointInClipObj(clipObj, p2)) {
                    if (!isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        //q1 = borderList[borderList.Count - 1].Point;
                        q1 = (PointD) borderList.get(0).Point.clone();
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = (PointD) borderList.get(j).Point.clone();
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = (PointD) IPoint.clone();
                                borderList.add(j, aBP);
                                inIdx = j;
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);
                    }
                    newPlist.add(aPList.get(i));
                    isInPolygon = true;
                } else {
                    if (isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = (PointD) borderList.get(0).Point.clone();
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = (PointD) borderList.get(j).Point.clone();
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                if (!newLine) {
                                    if (inIdx - outIdx >= 1 && inIdx - outIdx <= 10) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(outIdx, aBP);
                                        }
                                    } else if (inIdx - outIdx <= -1 && inIdx - outIdx >= -10) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(outIdx + 1, aBP);
                                        }
                                    } else if (inIdx == outIdx) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(inIdx + 1, aBP);
                                        }
                                    }
                                }
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = (PointD) IPoint.clone();
                                borderList.add(j, aBP);
                                outIdx = j;
                                a1 = inIdx;

                                //newLine = false;
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);

                        bLine = new Polyline();
                        bLine.setPointList(new ArrayList<>(newPlist));
                        newPolylines.add(bLine);

                        isInPolygon = false;
                        newPlist = new ArrayList<>();
                    }
                }
                p1 = p2;
            }
        }

        if (newPolylines.size() > 0) {
            if (aBP.Id >= newPolylines.size()) {
                return newPolygons;
            }

            //Tracing polygons
            newPolygons = tracingClipPolygons(inPolygon, newPolylines, borderList);
        } else {
            if (clipObj.getClass() != ClipLine.class) {
                if (pointInPolygon(aPList, clipPList.get(0))) {
                    if (!isClockwise(clipPList)) {
                        Collections.reverse(clipPList);
                    }

                    Polygon aPolygon = new Polygon();
                    aPolygon.setOutLine(new ArrayList<>(clipPList));
                    //aPolygon.setHoleLines(new ArrayList<List<PointD>>());

                    newPolygons.add(aPolygon);
                }
            }
        }

        if (holeLines.size() > 0) {
            addHoles_Ring(newPolygons, holeLines);
        }

        return newPolygons;
    }

    private static List<Polygon> clipPolygon_Extent(Polygon inPolygon, Extent extent) {
        List<Polygon> newPolygons = new ArrayList<>();
        List<Polyline> newPolylines = new ArrayList<>();
        List<PointD> aPList = (List<PointD>) inPolygon.getOutLine();

        if (!isExtentCross(inPolygon.getExtent(), extent)) {
            return newPolygons;
        }

        int i, j;
        //Judge if all points of the polyline are in the cut polygon - outline   
        List<List<PointD>> newLines = new ArrayList<>();
        if (pointInClipObj(extent, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!pointInClipObj(extent, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            if (!isAllIn) //Put start point outside of the cut polygon
            {
                List<PointD> bPList = new ArrayList<>();
                bPList.addAll(aPList.subList(notInIdx, aPList.size()));
                bPList.addAll(aPList.subList(1, notInIdx));

                bPList.add(bPList.get(0));
                newLines.add(bPList);
            } else //the input polygon is inside the cut polygon
            {
                newPolygons.add(inPolygon);
                return newPolygons;
            }
        } else {
            newLines.add(aPList);
        }

        //Holes
        List<List<PointD>> holeLines = new ArrayList<>();
        if (inPolygon.hasHole()) {
            for (int h = 0; h < inPolygon.getHoleLines().size(); h++) {
                List<PointD> holePList = (List<PointD>) inPolygon.getHoleLines().get(h);
                Extent plExtent = MIMath.getPointsExtent(holePList);
                if (!isExtentCross(plExtent, extent)) {
                    continue;
                }

                if (pointInClipObj(extent, holePList.get(0))) {
                    boolean isAllIn = true;
                    int notInIdx = 0;
                    for (i = 0; i < holePList.size(); i++) {
                        if (!pointInClipObj(extent, holePList.get(i))) {
                            notInIdx = i;
                            isAllIn = false;
                            break;
                        }
                    }
                    if (!isAllIn) //Put start point outside of the cut polygon
                    {
                        List<PointD> bPList = new ArrayList<>();
                        bPList.addAll(holePList.subList(notInIdx, holePList.size()));
                        bPList.addAll(holePList.subList(1, notInIdx));

                        bPList.add(bPList.get(0));
                        newLines.add(bPList);
                    } else //the hole is inside the cut polygon
                    {
                        holeLines.add((List<PointD>) inPolygon.getHoleLines().get(h));
                    }
                } else {
                    newLines.add(holePList);
                }
            }
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP = new BorderPoint();
        List<PointD> clipPList = getClipPointList(extent);
        for (i = 0; i < clipPList.size(); i++) {
            aBP = new BorderPoint();
            aBP.Point = (PointD) clipPList.get(i).clone();
            aBP.Id = -1;
            switch (i) {
                case 0:
                    aBP.rectPointType = RectPointTypes.LeftBottom;
                    break;
                case 1:
                    aBP.rectPointType = RectPointTypes.LeftTop;
                    break;
                case 2:
                    aBP.rectPointType = RectPointTypes.RightTop;
                    break;
                case 3:
                    aBP.rectPointType = RectPointTypes.RightBottom;
                    break;
                case 4:
                    aBP.rectPointType = RectPointTypes.LeftBottom;
                    break;
            }
            borderList.add(aBP);
        }

        //Cutting                     
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = false;
            PointD q1, q2, p1, p2, IPoint = new PointD();
            Line lineA, lineB;
            List<PointD> newPlist = new ArrayList<>();
            Polyline bLine;
            p1 = (PointD) aPList.get(0).clone();
            int inIdx = -1, outIdx = -1;
            boolean newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = (PointD) aPList.get(i).clone();
                if (pointInClipObj(extent, p2)) {
                    if (!isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        //q1 = borderList[borderList.Count - 1].Point;
                        q1 = (PointD) borderList.get(0).Point.clone();
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = (PointD) borderList.get(j).Point.clone();
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = (PointD) IPoint.clone();
                                borderList.add(j, aBP);
                                inIdx = j;
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);
                    }
                    newPlist.add(aPList.get(i));
                    isInPolygon = true;
                } else {
                    if (isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = (PointD) borderList.get(0).Point.clone();
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = (PointD) borderList.get(j).Point.clone();
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                if (!newLine) {
                                    if (inIdx - outIdx >= 1 && inIdx - outIdx <= 10) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(outIdx, aBP);
                                        }
                                    } else if (inIdx - outIdx <= -1 && inIdx - outIdx >= -10) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(outIdx + 1, aBP);
                                        }
                                    } else if (inIdx == outIdx) {
                                        if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                            borderList.remove(inIdx);
                                            borderList.add(inIdx + 1, aBP);
                                        }
                                    }
                                }
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = (PointD) IPoint.clone();
                                borderList.add(j, aBP);
                                outIdx = j;
                                a1 = inIdx;

                                //newLine = false;
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);

                        bLine = new Polyline();
                        bLine.setPointList(new ArrayList<>(newPlist));
                        newPolylines.add(bLine);

                        isInPolygon = false;
                        newPlist = new ArrayList<>();
                    } else {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        List<BorderPoint> clippedBPs = getCrossPoints(lineA, extent);
                        boolean isOK = clippedBPs.size() > 1;
                        if (clippedBPs.size() == 2) {
                            if (clippedBPs.get(0).rectPointType == clippedBPs.get(1).rectPointType) {
                                isOK = false;
                            }
                        }
                        if (isOK) {
                            newPlist = new ArrayList<>();
                            for (j = 0; j < clippedBPs.size(); j++) {
                                BorderPoint cBP = clippedBPs.get(j);
                                cBP.Id = newPolylines.size();
                                newPlist.add((PointD) cBP.Point.clone());
                                switch (cBP.rectPointType) {
                                    case Left:
                                        for (int k = 0; k < borderList.size(); k++) {
                                            if (borderList.get(k).rectPointType == RectPointTypes.LeftBottom) {
                                                for (int m = k; m < borderList.size(); m++) {
                                                    if (cBP.Point.Y <= borderList.get(m).Point.Y) {
                                                        borderList.add(m, cBP);
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case Top:
                                        for (int k = 0; k < borderList.size(); k++) {
                                            if (borderList.get(k).rectPointType == RectPointTypes.LeftTop) {
                                                for (int m = k; m < borderList.size(); m++) {
                                                    if (cBP.Point.X <= borderList.get(m).Point.X) {
                                                        borderList.add(m, cBP);
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case Right:
                                        for (int k = 0; k < borderList.size(); k++) {
                                            if (borderList.get(k).rectPointType == RectPointTypes.RightTop) {
                                                for (int m = k; m < borderList.size(); m++) {
                                                    if (borderList.get(m).Point.Y <= cBP.Point.Y) {
                                                        borderList.add(m, cBP);
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case Bottom:
                                        for (int k = 0; k < borderList.size(); k++) {
                                            if (borderList.get(k).rectPointType == RectPointTypes.RightBottom) {
                                                for (int m = k; m < borderList.size(); m++) {
                                                    if (borderList.get(m).Point.X <= cBP.Point.X) {
                                                        borderList.add(m, cBP);
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                }
                            }
                            bLine = new Polyline();
                            bLine.setPointList(new ArrayList<>(newPlist));
                            newPolylines.add(bLine);

                            isInPolygon = false;
                            newPlist = new ArrayList<>();
                        }
                    }
                }
                p1 = p2;
            }
        }

        if (newPolylines.size() > 0) {
            if (aBP.Id >= newPolylines.size()) {
                return newPolygons;
            }

            //Tracing polygons
            newPolygons = tracingClipPolygons(inPolygon, newPolylines, borderList);
        } else {
            if (pointInClipObj(aPList, clipPList.get(0))) {
                if (!isClockwise(clipPList)) {
                    Collections.reverse(clipPList);
                }

                Polygon aPolygon = new Polygon();
                aPolygon.setOutLine(new ArrayList<>(clipPList));
                //aPolygon.setHoleLines(new ArrayList<List<PointD>>());

                newPolygons.add(aPolygon);
            }
        }

        if (holeLines.size() > 0) {
            addHoles_Ring(newPolygons, holeLines);
        }

        return newPolygons;
    }

    private static List<Polygon> tracingClipPolygons(Polygon inPolygon, List<Polyline> LineList, List<BorderPoint> borderList) {
        if (LineList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Polygon> aPolygonList = new ArrayList<>(), newPolygonlist;
        List<Polyline> aLineList;
        Polyline aLine;
        PointD aPoint;
        Polygon aPolygon;
        int i, j;

        aLineList = new ArrayList<>(LineList);

        //---- Tracing border polygon
        List<PointD> aPList = new ArrayList<>();
        List<PointD> newPList;
        BorderPoint bP;
        int[] timesArray = new int[borderList.size() - 1];
        for (i = 0; i < timesArray.length; i++) {
            timesArray[i] = 0;
        }

        int pIdx, pNum;
        //List<BorderPoint> lineBorderList = new ArrayList<BorderPoint>();

        pNum = borderList.size() - 1;
        PointD bPoint, b1Point;
        for (i = 0; i < pNum; i++) {
            if ((borderList.get(i)).Id == -1) {
                continue;
            }

            pIdx = i;
            aPList.clear();
            //lineBorderList.add(borderList.get(i));
            //bP = borderList.get(pIdx);
            b1Point = borderList.get(pIdx).Point;

            //---- Clockwise traceing
            if (timesArray[pIdx] < 1) {
                aPList.add((borderList.get(pIdx)).Point);
                pIdx += 1;
                if (pIdx == pNum) {
                    pIdx = 0;
                }

                bPoint = (PointD) borderList.get(pIdx).Point.clone();
                if (borderList.get(pIdx).Id > -1) {
                    bPoint.X = (bPoint.X + b1Point.X) / 2;
                    bPoint.Y = (bPoint.Y + b1Point.Y) / 2;
                }
                if (pointInPolygon(inPolygon, bPoint)) {
                    while (true) {
                        bP = borderList.get(pIdx);
                        if (bP.Id == -1) //---- Not endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            aPList.add(bP.Point);
                            timesArray[pIdx] += +1;
                        } else //---- endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            timesArray[pIdx] += +1;
                            aLine = aLineList.get(bP.Id);

                            newPList = (List<PointD>) new ArrayList<>(aLine.getPointList());
                            aPoint = newPList.get(0);

                            if (!(MIMath.doubleEquals(bP.Point.X, aPoint.X) && MIMath.doubleEquals(bP.Point.Y, aPoint.Y))) {
                                Collections.reverse(newPList);
                            }

                            aPList.addAll(newPList);
                            for (j = 0; j < borderList.size() - 1; j++) {
                                if (j != pIdx) {
                                    if ((borderList.get(j)).Id == bP.Id) {
                                        pIdx = j;
                                        timesArray[pIdx] += +1;
                                        break;
                                    }
                                }
                            }
                        }

                        if (pIdx == i) {
                            if (aPList.size() > 0) {
                                aPolygon = new Polygon();
                                aPolygon.setOutLine(new ArrayList<>(aPList));
                                //aPolygon.setHoleLines(new ArrayList<List<PointD>>());
                                aPolygonList.add(aPolygon);
                            }
                            break;
                        }
                        pIdx += 1;
                        if (pIdx == pNum) {
                            pIdx = 0;
                        }
                    }
                }
            }

            //---- Anticlockwise traceing
            pIdx = i;
            if (timesArray[pIdx] < 1) {
                aPList.clear();
                aPList.add((borderList.get(pIdx)).Point);
                pIdx += -1;
                if (pIdx == -1) {
                    pIdx = pNum - 1;
                }

                bPoint = (PointD) borderList.get(pIdx).Point.clone();
                if (borderList.get(pIdx).Id > -1) {
                    bPoint.X = (bPoint.X + b1Point.X) / 2;
                    bPoint.Y = (bPoint.Y + b1Point.Y) / 2;
                }
                if (pointInPolygon(inPolygon, bPoint)) {
                    while (true) {
                        bP = borderList.get(pIdx);
                        if (bP.Id == -1) //---- Not endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            aPList.add(bP.Point);
                            timesArray[pIdx] += +1;
                        } else //---- endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            timesArray[pIdx] += +1;
                            aLine = aLineList.get(bP.Id);

                            newPList = (List<PointD>) new ArrayList<>(aLine.getPointList());
                            aPoint = newPList.get(0);

                            if (!(MIMath.doubleEquals(bP.Point.X, aPoint.X) && MIMath.doubleEquals(bP.Point.Y, aPoint.Y))) {
                                Collections.reverse(newPList);
                            }

                            aPList.addAll(newPList);
                            for (j = 0; j < borderList.size() - 1; j++) {
                                if (j != pIdx) {
                                    if ((borderList.get(j)).Id == bP.Id) {
                                        pIdx = j;
                                        timesArray[pIdx] += +1;
                                        break;
                                    }
                                }
                            }
                        }

                        if (pIdx == i) {
                            if (aPList.size() > 0) {
                                aPolygon = new Polygon();
                                Collections.reverse(aPList);
                                aPolygon.setOutLine(new ArrayList<>(aPList));
                                //aPolygon.setHoleLines(new ArrayList<List<PointD>>());
                                aPolygonList.add(aPolygon);
                            }
                            break;
                        }
                        pIdx += -1;
                        if (pIdx == -1) {
                            pIdx = pNum - 1;
                        }

                    }
                }
            }
        }

        newPolygonlist = new ArrayList<>(aPolygonList);

        return newPolygonlist;
    }

    private static void addHoles_Ring(List<Polygon> polygonList, List<List<PointD>> holeList) {
        int i, j;
        for (i = 0; i < holeList.size(); i++) {
            List<PointD> holePs = holeList.get(i);
            Extent aExtent = MIMath.getPointsExtent(holePs);
            for (j = 0; j < polygonList.size(); j++) {
                Polygon aPolygon = polygonList.get(j);
                if (aPolygon.getExtent().include(aExtent)) {
                    boolean isHole = true;
                    for (PointD aP : holePs) {
                        if (!pointInPolygon(aPolygon.getOutLine(), aP)) {
                            isHole = false;
                            break;
                        }
                    }
                    if (isHole) {
                        aPolygon.addHole(holePs);
                        //polygonList.set(j, aPolygon);
                        break;
                    }
                }
            }
        }
    }

    private static boolean isExtentCross(Extent aExtent, Object clipObj) {
        if (clipObj instanceof List) {
            Extent bExtent = MIMath.getPointsExtent((List<PointD>) clipObj);
            return MIMath.isExtentCross(aExtent, bExtent);
        }
        if (clipObj.getClass() == ClipLine.class) {
            return ((ClipLine) clipObj).isExtentCross(aExtent);
        }
        if (clipObj instanceof Extent) {
            return MIMath.isExtentCross(aExtent, (Extent) clipObj);
        }
//        if (clipObj.getClass() == Extent.class) {
//            return MIMath.isExtentCross(aExtent, (Extent) clipObj);
//        }

        return false;
    }

    private static boolean pointInClipObj(Object clipObj, PointD aPoint) {
        if (clipObj instanceof List) {
            return pointInPolygon((List<PointD>) clipObj, aPoint);
        }
        if (clipObj.getClass() == ClipLine.class) {
            return ((ClipLine) clipObj).isInside(aPoint);
        }
        if (clipObj instanceof Extent) {
            return MIMath.pointInExtent(aPoint, (Extent) clipObj);
        }

        return false;
    }

    private static List<PointD> getClipPointList(Object clipObj) {
        List<PointD> clipPList = new ArrayList<>();
        if (clipObj instanceof List) {
            clipPList = (List<PointD>) clipObj;
        }
        if (clipObj.getClass() == ClipLine.class) {
            ClipLine clipLine = (ClipLine) clipObj;
            if (clipLine.isLongitude()) {
                for (int i = -100; i <= 100; i++) {
                    clipPList.add(new PointD(clipLine.getValue(), i));
                }
            } else {
                for (int i = -370; i <= 370; i++) {
                    clipPList.add(new PointD(i, clipLine.getValue()));
                }
            }
        }
        if (clipObj instanceof Extent) {
            Extent aExtent = (Extent) clipObj;
            clipPList.add(new PointD(aExtent.minX, aExtent.minY));
            clipPList.add(new PointD(aExtent.minX, aExtent.maxY));
            clipPList.add(new PointD(aExtent.maxX, aExtent.maxY));
            clipPList.add(new PointD(aExtent.maxX, aExtent.minY));
            clipPList.add((PointD) clipPList.get(0).clone());
        }

        return clipPList;
    }

    private static boolean isLineSegmentCross_old(Line lineA, Line lineB) {
        Extent boundA, boundB;
        List<PointD> PListA = new ArrayList<>(), PListB = new ArrayList<>();
        PListA.add(lineA.P1);
        PListA.add(lineA.P2);
        PListB.add(lineB.P1);
        PListB.add(lineB.P2);
        boundA = MIMath.getPointsExtent(PListA);
        boundB = MIMath.getPointsExtent(PListB);

        if (!MIMath.isExtentCross(boundA, boundB)) {
            return false;
        } else {
            double XP1 = (lineB.P1.X - lineA.P1.X) * (lineA.P2.Y - lineA.P1.Y)
                    - (lineA.P2.X - lineA.P1.X) * (lineB.P1.Y - lineA.P1.Y);
            double XP2 = (lineB.P2.X - lineA.P1.X) * (lineA.P2.Y - lineA.P1.Y)
                    - (lineA.P2.X - lineA.P1.X) * (lineB.P2.Y - lineA.P1.Y);
            if (XP1 * XP2 > 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    private static boolean isLineSegmentCross(Line lineA, Line lineB) {
        Extent boundA, boundB;
        List<PointD> PListA = new ArrayList<>(), PListB = new ArrayList<>();
        PListA.add(lineA.P1);
        PListA.add(lineA.P2);
        PListB.add(lineB.P1);
        PListB.add(lineB.P2);
        boundA = MIMath.getPointsExtent(PListA);
        boundB = MIMath.getPointsExtent(PListB);

        if (!MIMath.isExtentCross(boundA, boundB)) {
            return false;
        } else {
            double d1 = crossProduct(lineA.P1, lineA.P2, lineB.P1);
            double d2 = crossProduct(lineA.P1, lineA.P2, lineB.P2);
            double d3 = crossProduct(lineB.P1, lineB.P2, lineA.P1);
            double d4 = crossProduct(lineB.P1, lineB.P2, lineA.P2);
            if ((d1 * d2 < 0) && (d3 * d4 < 0)) {
                return true;
            } else if (d1 == 0 && pointProduct(lineB.P1, lineA.P1, lineA.P2) <= 0) {
                return true;
            } else if (d2 == 0 && pointProduct(lineB.P2, lineA.P1, lineA.P2) <= 0) {
                return true;
            } else if (d3 == 0 && pointProduct(lineA.P1, lineB.P1, lineB.P2) <= 0) {
                return true;
            } else if (d4 == 0 && pointProduct(lineA.P2, lineB.P1, lineB.P2) <= 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Finds the cross product of the 2 vectors created by the 3 vertices.
     * Vector 1 = v1 -> v2, Vector 2 = v2 -> v3 The vectors make a "right turn"
     * if the sign of the cross product is negative. The vectors make a "left
     * turn" if the sign of the cross product is positive. The vectors are
     * colinear (on the same line) if the cross product is zero.
     *
     * @param p1 Point 1
     * @param p2 Point 2
     * @param p3 Piont 3
     * @return Cross product of the two vectors
     */
    public static double crossProduct(PointD p1, PointD p2, PointD p3) {
        return (p2.X - p1.X) * (p3.Y - p1.Y) - (p3.X - p1.X) * (p2.Y - p1.Y);
    }

    /**
     * Finds the point product of the 2 vectors created by the 3 vertices.
     *
     * @param p1 Point 1
     * @param p2 Point 2
     * @param p3 Piont 3
     * @return Cross product of the two vectors
     */
    public static double pointProduct(PointD p1, PointD p2, PointD p3) {
        return (p2.X - p1.X) * (p3.X - p1.X) + (p2.Y - p1.Y) * (p3.Y - p1.Y);
    }

    private static boolean lineIntersectRect(Line line, Extent extent) {
        boolean result = false;
        result |= checkRectLineH(line.P1, line.P2, extent.minY, extent.minX, extent.maxX);
        result |= checkRectLineH(line.P1, line.P2, extent.maxY, extent.minX, extent.maxX);
        result |= checkRectLineV(line.P1, line.P2, extent.minX, extent.minY, extent.maxY);
        result |= checkRectLineV(line.P1, line.P2, extent.maxX, extent.minY, extent.maxY);
        return result;
    }

    private static boolean checkRectLineH(PointD start, PointD end, double y0, double x1, double x2) {
        if ((y0 < start.Y) && (y0 < end.Y)) {
            return false;
        }

        if ((y0 > start.Y) && (y0 > end.Y)) {
            return false;
        }

        if (start.Y == end.Y) {
            if (y0 == start.Y) {
                if ((start.X < x1) && (end.X < x1)) {
                    return false;
                }

                if ((start.X > x2) && (end.X > x2)) {
                    return false;
                }

                return true;
            } else {
                return false;
            }
        }

        double x = (end.X - start.X) * (y0 - start.Y) / (end.Y - start.Y) + start.X;
        return ((x >= x1) && (x <= x2));
    }

    private static boolean checkRectLineV(PointD start, PointD end, double x0, double y1, double y2) {
        if ((x0 < start.X) && (x0 < end.X)) {
            return false;
        }
        if ((x0 > start.X) && (x0 > end.X)) {
            return false;
        }
        if (start.X == end.X) {
            if (x0 == start.X) {
                if ((start.Y < y1) && (end.Y < y1)) {
                    return false;
                }
                if ((start.Y > y2) && (end.Y > y2)) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        double y = (end.Y - start.Y) * (x0 - start.X) / (end.X - start.X) + start.Y;
        return ((y >= y1) && (y <= y2));
    }

    private static List<BorderPoint> getCrossPoints(Line line, Extent extent) {
        List<BorderPoint> crossPoints = new ArrayList<>();

        if (line.P1.X <= extent.minX && line.P2.X <= extent.minX) {
            return crossPoints;
        }
        if (line.P1.X >= extent.maxX && line.P2.X >= extent.maxX) {
            return crossPoints;
        }
        if (line.P1.Y <= extent.minY && line.P2.Y <= extent.minY) {
            return crossPoints;
        }
        if (line.P1.Y >= extent.maxY && line.P2.Y >= extent.maxY) {
            return crossPoints;
        }

        //Left border
        if (Math.abs(line.P1.X - line.P2.X) - Math.abs(line.P1.X - extent.minX) > 0) {
            double y = (line.P2.Y - line.P1.Y) * (extent.minX - line.P1.X) / (line.P2.X - line.P1.X) + line.P1.Y;
            if ((y >= extent.minY) && (y <= extent.maxY)) {
                BorderPoint bp = new BorderPoint();
                bp.rectPointType = RectPointTypes.Left;
                bp.Point = new PointD(extent.minX, y);
                crossPoints.add(bp);
            }
        }

        //Top border
        if (Math.abs(line.P1.Y - line.P2.Y) - Math.abs(line.P1.Y - extent.maxY) > 0) {
            double x = (extent.maxY - line.P1.Y) * (line.P2.X - line.P1.X) / (line.P2.Y - line.P1.Y) + line.P1.X;
            if ((x >= extent.minX) && (x <= extent.maxX)) {
                BorderPoint bp = new BorderPoint();
                bp.rectPointType = RectPointTypes.Top;
                bp.Point = new PointD(x, extent.maxY);
                crossPoints.add(bp);
            }
        }

        //Right border
        if (Math.abs(line.P1.X - line.P2.X) - Math.abs(line.P1.X - extent.maxX) > 0) {
            double y = (line.P2.Y - line.P1.Y) * (extent.maxX - line.P1.X) / (line.P2.X - line.P1.X) + line.P1.Y;
            if ((y >= extent.minY) && (y <= extent.maxY)) {
                BorderPoint bp = new BorderPoint();
                bp.rectPointType = RectPointTypes.Right;
                bp.Point = new PointD(extent.maxX, y);
                crossPoints.add(bp);
            }
        }

        //Bottom border
        if (Math.abs(line.P1.Y - line.P2.Y) - Math.abs(line.P1.Y - extent.minY) > 0) {
            double x = (extent.minY - line.P1.Y) * (line.P2.X - line.P1.X) / (line.P2.Y - line.P1.Y) + line.P1.X;
            if ((x >= extent.minX) && (x <= extent.maxX)) {
                BorderPoint bp = new BorderPoint();
                bp.rectPointType = RectPointTypes.Bottom;
                bp.Point = new PointD(x, extent.minY);
                crossPoints.add(bp);
            }
        }

        return crossPoints;
    }

    private static PointD getCrossPoint(Line lineA, Line lineB) {
        PointD IPoint = new PointD();
        PointD p1, p2, q1, q2;
        double tempLeft, tempRight;

        double XP1 = (lineB.P1.X - lineA.P1.X) * (lineA.P2.Y - lineA.P1.Y)
                - (lineA.P2.X - lineA.P1.X) * (lineB.P1.Y - lineA.P1.Y);
        double XP2 = (lineB.P2.X - lineA.P1.X) * (lineA.P2.Y - lineA.P1.Y)
                - (lineA.P2.X - lineA.P1.X) * (lineB.P2.Y - lineA.P1.Y);
        if (XP1 == 0) {
            IPoint = lineB.P1;
        } else if (XP2 == 0) {
            IPoint = lineB.P2;
        } else {
            p1 = lineA.P1;
            p2 = lineA.P2;
            q1 = lineB.P1;
            q2 = lineB.P2;

            tempLeft = (q2.X - q1.X) * (p1.Y - p2.Y) - (p2.X - p1.X) * (q1.Y - q2.Y);
            tempRight = (p1.Y - q1.Y) * (p2.X - p1.X) * (q2.X - q1.X) + q1.X * (q2.Y - q1.Y) * (p2.X - p1.X) - p1.X * (p2.Y - p1.Y) * (q2.X - q1.X);
            IPoint.X = tempRight / tempLeft;

            tempLeft = (p1.X - p2.X) * (q2.Y - q1.Y) - (p2.Y - p1.Y) * (q1.X - q2.X);
            tempRight = p2.Y * (p1.X - p2.X) * (q2.Y - q1.Y) + (q2.X - p2.X) * (q2.Y - q1.Y) * (p1.Y - p2.Y) - q2.Y * (q1.X - q2.X) * (p2.Y - p1.Y);
            IPoint.Y = tempRight / tempLeft;
        }

        if (lineA.P1 instanceof PointZ) {
            return new PointZ(IPoint.X, IPoint.Y, ((PointZ) lineA.P1).Z, ((PointZ) lineA.P1).M);
        }
        return IPoint;
    }

    private static boolean twoPointsInside(int a1, int a2, int b1, int b2) {
        if (a2 < a1) {
            int c = a1;
            a1 = a2;
            a2 = c;
        }

        if (b1 >= a1 && b1 <= a2) {
            if (b2 >= a1 && b2 <= a2) {
                return true;
            } else {
                return false;
            }
        } else {
            if (!(b2 >= a1 && b2 <= a2)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Get grid labels of a polyline
     *
     * @param inPolyLine Polyline
     * @param clipExtent Clipping object
     * @param isVertical If is vertical
     * @return Clip points
     */
    public static List<GridLabel> getGridLabels(Polyline inPolyLine, Extent clipExtent, boolean isVertical) {
        List<GridLabel> gridLabels = new ArrayList<>();
        List<PointD> aPList = (List<PointD>) inPolyLine.getPointList();

        if (!isExtentCross(inPolyLine.getExtent(), clipExtent)) {
            return gridLabels;
        }

        int i, j;
        //Judge if all points of the polyline are in the cut polygon - outline   
        List<List<PointD>> newLines = new ArrayList<>();
        PointD p1, p2;
        boolean isReversed = false;
        if (pointInClipObj(clipExtent, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!pointInClipObj(clipExtent, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            if (!isAllIn) //Put start point outside of the cut polygon
            {
                if (inPolyLine.isClosed()) {
                    List<PointD> bPList = new ArrayList<>();
                    bPList.addAll(aPList.subList(notInIdx, aPList.size() - 1));
                    bPList.addAll(aPList.subList(1, notInIdx));
                    bPList.add(bPList.get(0));
                    newLines.add(bPList);
                } else {
                    Collections.reverse(aPList);
                    newLines.add(aPList);
                    isReversed = true;
                }
            } else {    //the input polygon is inside the cut polygon
                p1 = aPList.get(0);
                if (aPList.size() == 2)
                    p2 = aPList.get(1);
                else
                    p2 = aPList.get(2);
                GridLabel aGL = new GridLabel();
                aGL.setLongitude(isVertical);
                aGL.setBorder(false);
                aGL.setCoord(p1);
                if (isVertical) {
                    aGL.setLabDirection(Direction.South);
                } else {
                    aGL.setLabDirection(Direction.Weast);
                }
                aGL.setAnge((float)ArrayMath.uv2ds(p2.X - p1.X, p2.Y - p1.Y)[0]);
                gridLabels.add(aGL);

                p1 = aPList.get(aPList.size() - 1);
                if (aPList.size() == 2)
                    p2 = aPList.get(aPList.size() - 2);
                else
                    p2 = aPList.get(aPList.size() - 3);
                aGL = new GridLabel();
                aGL.setLongitude(isVertical);
                aGL.setBorder(false);
                aGL.setCoord(p1);
                if (isVertical) {
                    aGL.setLabDirection(Direction.North);
                } else {
                    aGL.setLabDirection(Direction.East);
                }
                aGL.setAnge((float)ArrayMath.uv2ds(p2.X - p1.X, p2.Y - p1.Y)[0]);
                gridLabels.add(aGL);

                return gridLabels;
            }
        } else {
            newLines.add(aPList);
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP;
        List<PointD> clipPList = getClipPointList(clipExtent);
        for (PointD aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = aP;
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting                     
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = pointInClipObj(clipExtent, aPList.get(0));
            PointD q1, q2, IPoint = new PointD();
            Line lineA, lineB;
            List<PointD> newPlist = new ArrayList<>();
            //Polyline bLine = new Polyline();
            p1 = aPList.get(0);
            int inIdx = -1, outIdx = -1;
            //bool newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = aPList.get(i);
                if (pointInClipObj(clipExtent, p2)) {
                    if (!isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = borderList.get(0).Point;
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                inIdx = j;
                                break;
                            }
                            q1 = q2;
                        }
                        GridLabel aGL = new GridLabel();
                        aGL.setLongitude(isVertical);
                        aGL.setBorder(true);
                        aGL.setCoord(IPoint);
                        if (MIMath.doubleEquals(q1.X, borderList.get(j).Point.X)) {
                            if (MIMath.doubleEquals(q1.X, clipExtent.minX)) {
                                aGL.setLabDirection(Direction.Weast);
                            } else {
                                aGL.setLabDirection(Direction.East);
                            }
                        } else {
                            if (MIMath.doubleEquals(q1.Y, clipExtent.minY)) {
                                aGL.setLabDirection(Direction.South);
                            } else {
                                aGL.setLabDirection(Direction.North);
                            }
                        }

                        if (isVertical) {
                            if (aGL.getLabDirection() == Direction.South || aGL.getLabDirection() == Direction.North) {
                                gridLabels.add(aGL);
                            }
                        } else {
                            if (aGL.getLabDirection() == Direction.East || aGL.getLabDirection() == Direction.Weast) {
                                gridLabels.add(aGL);
                            }
                        }

                    }
                    newPlist.add(aPList.get(i));
                    isInPolygon = true;
                } else {
                    if (isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = borderList.get(0).Point;
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                outIdx = j;
                                a1 = inIdx;
                                break;
                            }
                            q1 = q2;
                        }
                        GridLabel aGL = new GridLabel();
                        aGL.setBorder(true);
                        aGL.setLongitude(isVertical);
                        aGL.setCoord(IPoint);
                        if (MIMath.doubleEquals(q1.X, borderList.get(j).Point.X)) {
                            if (MIMath.doubleEquals(q1.X, clipExtent.minX)) {
                                aGL.setLabDirection(Direction.Weast);
                            } else {
                                aGL.setLabDirection(Direction.East);
                            }
                        } else {
                            if (MIMath.doubleEquals(q1.Y, clipExtent.minY)) {
                                aGL.setLabDirection(Direction.South);
                            } else {
                                aGL.setLabDirection(Direction.North);
                            }
                        }

                        if (isVertical) {
                            if (aGL.getLabDirection() == Direction.South || aGL.getLabDirection() == Direction.North) {
                                gridLabels.add(aGL);
                            }
                        } else {
                            if (aGL.getLabDirection() == Direction.East || aGL.getLabDirection() == Direction.Weast) {
                                gridLabels.add(aGL);
                            }
                        }

                        isInPolygon = false;
                        newPlist = new ArrayList<>();
                    }
                }
                p1 = p2;
            }

            if (isInPolygon && newPlist.size() > 1) {
                GridLabel aGL = new GridLabel();
                aGL.setLongitude(isVertical);
                aGL.setBorder(false);
                aGL.setCoord(newPlist.get(newPlist.size() - 1));
                if (isVertical) {
                    if (isReversed) {
                        aGL.setLabDirection(Direction.South);
                    } else {
                        aGL.setLabDirection(Direction.North);
                    }
                } else {
                    if (isReversed) {
                        aGL.setLabDirection(Direction.Weast);
                    } else {
                        aGL.setLabDirection(Direction.East);
                    }
                }

                gridLabels.add(aGL);
            }
        }

        return gridLabels;
    }

    /**
     * Get grid labels of a straight line
     *
     * @param inPolyLine Polyline
     * @param clipExtent Clipping object
     * @param isVertical If is vertical
     * @return Clip points
     */
    public static List<GridLabel> getGridLabels_StraightLine(Polyline inPolyLine, Extent clipExtent, boolean isVertical) {
        List<GridLabel> gridLabels = new ArrayList<>();
        //List<PointD> aPList = (List<PointD>) inPolyLine.getPointList();

        PointD aPoint = inPolyLine.getPointList().get(0);
        if (isVertical) {
            if (aPoint.X < clipExtent.minX || aPoint.X > clipExtent.maxX) {
                return gridLabels;
            }

            GridLabel aGL = new GridLabel();
            aGL.setLabDirection(Direction.South);
            aGL.setCoord(new PointD(aPoint.X, clipExtent.minY));
            gridLabels.add(aGL);
        } else {
            if (aPoint.Y < clipExtent.minY || aPoint.Y > clipExtent.maxY) {
                return gridLabels;
            }

            GridLabel aGL = new GridLabel();
            aGL.setLabDirection(Direction.Weast);
            aGL.setCoord(new PointD(clipExtent.minX, aPoint.Y));
            gridLabels.add(aGL);
        }

        aPoint = inPolyLine.getPointList().get(inPolyLine.getPointList().size() - 1);
        if (isVertical) {
            if (aPoint.X < clipExtent.minX || aPoint.X > clipExtent.maxX) {
                return gridLabels;
            }

            GridLabel aGL = new GridLabel();
            aGL.setLabDirection(Direction.North);
            aGL.setCoord(new PointD(aPoint.X, clipExtent.maxY));
            gridLabels.add(aGL);
        } else {
            if (aPoint.Y < clipExtent.minY || aPoint.Y > clipExtent.maxY) {
                return gridLabels;
            }

            GridLabel aGL = new GridLabel();
            aGL.setLabDirection(Direction.East);
            aGL.setCoord(new PointD(clipExtent.maxX, aPoint.Y));
            gridLabels.add(aGL);
        }

        return gridLabels;
    }
    // </editor-fold>
}
