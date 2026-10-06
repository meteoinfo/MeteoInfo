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
package org.meteoinfo.geometry.geoprocess;

import org.meteoinfo.common.*;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.geometry.GeometryUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * GeoComputation class
 *
 * @author Yaqiang Wang
 */
public class GeoComputation {

    private static final double EARTH_RADIUS = 6371.393;

    // <editor-fold desc="General">        
    /**
     * Determine if a point array is clockwise
     *
     * @param pointList point list
     * @return boolean
     */
    public static boolean isClockwise(List<Coordinate> pointList) {
        int i;
        Coordinate aPoint;
        double yMax = 0;
        int yMaxIdx = 0;
        for (i = 0; i < pointList.size() - 1; i++) {
            aPoint = pointList.get(i);
            if (i == 0) {
                yMax = aPoint.y;
                yMaxIdx = 0;
            } else {
                if (yMax < aPoint.y) {
                    yMax = aPoint.y;
                    yMaxIdx = i;
                }
            }
        }
        Coordinate p1, p2, p3;
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
        return (p3.x - p1.x) * (p2.y - p1.y) - (p2.x - p1.x) * (p3.y - p1.y) > 0;

    }

    /**
     * Determine if a point array is clockwise
     *
     * @param points point array
     * @return boolean
     */
    public static boolean isClockwise(Coordinate[] points) {
        List<Coordinate> pointList = Arrays.asList(points);
        return isClockwise(pointList);
    }

    /**
     * Determine if a point is in a polygon
     *
     * @param poly Polygon border points
     * @param aPoint The point
     * @return If the point is in the polygon
     */
    public static boolean pointInPolygon(List<Coordinate> poly, Coordinate aPoint) {
        double xNew, yNew, xOld, yOld;
        double x1, y1, x2, y2;
        int i;
        boolean inside = false;
        int nPoints = poly.size();

        if (nPoints < 3) {
            return false;
        }

        xOld = (poly.get(nPoints - 1)).x;
        yOld = (poly.get(nPoints - 1)).y;
        for (i = 0; i < nPoints; i++) {
            xNew = (poly.get(i)).x;
            yNew = (poly.get(i)).y;
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
            if ((xNew < aPoint.x) == (aPoint.x <= xOld)
                    && (aPoint.y - y1) * (x2 - x1) < (y2 - y1) * (aPoint.x - x1)) {
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
    public static boolean pointInPolygon(PolygonShape aPolygon, Coordinate aPoint) {
        if (!aPolygon.getExtent().contains(aPoint)) {
            return false;
        }

        switch (aPolygon.getShapeType()) {
            case CIRCLE:
                return ((CircleShape) aPolygon).contains(aPoint);
            case ELLIPSE:
                return ((EllipseShape) aPolygon).contains(aPoint);
            case ARC:
                return ((ArcShape) aPolygon).contains(aPoint);
        }

        boolean isIn = false;
        for (int i = 0; i < aPolygon.getPolygons().size(); i++) {
            Polygon aPRing = aPolygon.getPolygons().get(i);
            isIn = pointInPolygon(aPRing.getOutLine(), aPoint);
            if (isIn) {
                if (aPRing.hasHole()) {
                    for (List<Coordinate> aLine : aPRing.getHoleLines()) {
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
        return pointInPolygon(aPolygon, new Coordinate(x, y));
    }

    /**
     * Determine if a point is in a polygon
     *
     * @param aPolygon The polygon
     * @param aPoint The point
     * @return Boolean
     */
    public static boolean pointInPolygon(Polygon aPolygon, Coordinate aPoint) {
        if (!aPolygon.getExtent().contains(aPoint)) {
            return false;
        }

        if (aPolygon.hasHole()) {
            boolean isIn = pointInPolygon(aPolygon.getOutLine(), aPoint);
            if (isIn) {
                for (List<Coordinate> aLine : aPolygon.getHoleLines()) {
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
    public static boolean pointInPolygons(List<PolygonShape> polygons, Coordinate aPoint) {
        boolean isIn = false;
        Extent ext = GeometryUtil.getExtent(polygons);
        if (ext.contains(aPoint)) {
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
     * Get polygon index from polygons by a point
     * @param polygons The polygons
     * @param point The point
     * @return Polygon index - the point is inside the polygon
     */
    public static int polygonIndex(List<PolygonShape> polygons, Coordinate point) {
        int idx = -1;
        Extent ext = GeometryUtil.getExtent(polygons);
        if (ext.contains(point)) {
            PolygonShape polygonShape;
            for (int i = 0; i < polygons.size(); i++) {
                polygonShape = polygons.get(i);
                if (pointInPolygon(polygonShape, point)) {
                    return i;
                }
            }
        }

        return idx;
    }

//    /**
//     * Determine if a point loacted in a polygon layer
//     *
//     * @param aLayer The polygon layer
//     * @param aPoint The point
//     * @param onlySel If check only selected shapes
//     * @return Inside or outside
//     */
//    public static boolean pointInPolygonLayer(VectorLayer aLayer, PointZ aPoint, boolean onlySel) {
//        if (!MIMath.pointInExtent(aPoint, aLayer.getExtent())) {
//            return false;
//        }
//
//        List<PolygonShape> polygons = new ArrayList<>();
//        if (onlySel) {
//            for (Shape aShape : aLayer.getShapes()) {
//                if (aShape.isSelected()) {
//                    polygons.add((PolygonShape) aShape);
//                }
//            }
//        } else {
//            for (Shape aShape : aLayer.getShapes()) {
//                polygons.add((PolygonShape) aShape);
//            }
//        }
//
//        return pointInPolygons(polygons, aPoint);
//    }

    /**
     * Calculate the distance between point and a line segment
     *
     * @param point The point
     * @param pt1 End point of the line segment
     * @param pt2 End point of the line segment
     * @return Distance
     */
    public static double dis_PointToLine(Coordinate point, Coordinate pt1, Coordinate pt2) {
        double dis;
        if (MIMath.doubleEquals(pt2.x, pt1.x)) {
            dis = Math.abs(point.x - pt1.x);
        } else if (MIMath.doubleEquals(pt2.y, pt1.y)) {
            dis = Math.abs(point.y - pt1.y);
        } else {
            double k = (pt2.y - pt1.y) / (pt2.x - pt1.x);
            double x = (k * k * pt1.x + k * (point.y - pt1.y) + point.x) / (k * k + 1);
            double y = k * (x - pt1.x) + pt1.y;
            //double dis = Math.sqrt((point.Y - y) * (point.Y - y) + (point.X - x) * (point.X - x));
            dis = distance(point, new Coordinate(x, y));
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
    public static double distance(Coordinate pt1, Coordinate pt2) {
        return Math.sqrt((pt2.y - pt1.y) * (pt2.y - pt1.y) + (pt2.x - pt1.x) * (pt2.x - pt1.x));
    }

    /**
     * Select polyline shape by a point
     *
     * @param sp The point
     * @param aPLS The polyline shape
     * @param buffer Buffer
     * @return Is the polyline shape selected
     */
    public static Object selectPolylineShape(Coordinate sp, PolylineShape aPLS, double buffer) {
        Extent aExtent = new Extent();
        aExtent.minX = sp.x - buffer;
        aExtent.maxX = sp.x + buffer;
        aExtent.minY = sp.y - buffer;
        aExtent.maxY = sp.y + buffer;
        double dis;
        if (GeometryUtil.isExtentCross(aExtent, aPLS.getExtent())) {
            for (int j = 0; j < aPLS.getPointNum(); j++) {
                Coordinate aPoint = aPLS.getPoints().get(j);
                if (aExtent.contains(aPoint)) {
                    return GeoComputation.distance(sp, aPoint);
                }
                if (j < aPLS.getPointNum() - 1) {
                    Coordinate bPoint = aPLS.getPoints().get(j + 1);
                    if (Math.abs(sp.y - aPoint.y) <= Math.abs(bPoint.y - aPoint.y)
                            || Math.abs(sp.x - aPoint.x) <= Math.abs(bPoint.x - aPoint.x)) {
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
    public static Object selectPolyline(Coordinate sp, List<Coordinate> points, double buffer) {
        Extent aExtent = new Extent();
        aExtent.minX = sp.x - buffer;
        aExtent.maxX = sp.x + buffer;
        aExtent.minY = sp.y - buffer;
        aExtent.maxY = sp.y + buffer;
        Extent bExtent = GeometryUtil.getPointsExtent(points);
        double dis;
        if (GeometryUtil.isExtentCross(aExtent, bExtent)) {
            for (int j = 0; j < points.size(); j++) {
                Coordinate aPoint = points.get(j);
                if (aExtent.contains(aPoint)) {
                    return GeoComputation.distance(sp, aPoint);
                }
                if (j < points.size() - 1) {
                    Coordinate bPoint = points.get(j + 1);
                    if (Math.abs(sp.y - aPoint.y) <= Math.abs(bPoint.y - aPoint.y)
                            || Math.abs(sp.x - aPoint.x) <= Math.abs(bPoint.x - aPoint.x)) {
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
     * Calculate area of grid cells.
     * @param xOrig X origin
     * @param xCell X cell spacing
     * @param xNum X number
     * @param yOrig Y origin
     * @param yCell Y cell spacing
     * @param yNum Y number
     * @param isLonLat Is lonlat projection or not
     * @param allCell Calculate each grid or not
     * @return Grid area array
     */
    public static Array getGridArea(double xOrig, double xCell, int xNum, double yOrig,
                                    double yCell, int yNum, boolean isLonLat, boolean allCell) {
        return getGridArea(xOrig, xCell, xNum, yOrig, yCell, yNum, isLonLat, allCell, EARTH_RADIUS * 1000);
    }

    /**
     * Calculate area of grid cells.
     * @param xOrig X origin
     * @param xCell X cell spacing
     * @param xNum X number
     * @param yOrig Y origin
     * @param yCell Y cell spacing
     * @param yNum Y number
     * @param isLonLat Is lonlat projection or not
     * @param allCell Calculate each grid or not
     * @param earthRadius Earth radius
     * @return Grid area array
     */
    public static Array getGridArea(double xOrig, double xCell, int xNum, double yOrig,
             double yCell, int yNum, boolean isLonLat, boolean allCell, double earthRadius) {
        Array r = Array.factory(DataType.DOUBLE, new int[]{yNum, xNum});
        double[] xx = new double[5];
        double[] yy = new double[5];
        double dx = xCell * 0.5;
        double dy = yCell * 0.5;
        double x, y, a;
        if (allCell) {
            for (int i = 0; i < yNum; i++) {
                y = yOrig + i * yCell;
                for (int j = 0; j < xNum; j++) {
                    x = xOrig + j * xCell;
                    xx = new double[]{x - dx, x + dx, x + dx, x - dx, x - dx};
                    yy = new double[]{y - dy, y - dy, y + dy, y + dy, y - dy};
                    a = getArea(xx, yy, isLonLat, earthRadius);
                    r.setDouble(i * xNum + j, a);
                }
            }
        } else {
            x = xOrig;
            xx = new double[]{x - dx, x + dx, x + dx, x - dx, x - dx};
            for (int i = 0; i < yNum; i++) {
                y = yOrig + i * yCell;
                yy = new double[]{y - dy, y - dy, y + dy, y + dy, y - dy};
                a = getArea(xx, yy, isLonLat, earthRadius);
                for (int j = 0; j < xNum; j++)
                    r.setDouble(i * xNum + j, a);
            }
        }
        return r;
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
        double[] xx = new double[x.size()];
        double[] yy = new double[x.size()];
        for (int i = 0; i < x.size(); i++) {
            xx[i] = x.get(i).doubleValue();
            yy[i] = y.get(i).doubleValue();
        }

        return getArea(xx, yy, isLonLat);
    }

    /**
     * Get polygon area on earth surface
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param isLonLat if is lon/lat
     * @return area
     */
    public static double getArea(double[] x, double[] y, boolean isLonLat) {
        return getArea(x, y, isLonLat, EARTH_RADIUS * 1000);
    }

    /**
     * Get polygon area on earth surface
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param isLonLat if is lon/lat
     * @param earthRadius Earth radius
     * @return area
     */
    public static double getArea(double[] x, double[] y, boolean isLonLat, double earthRadius) {

        int n = x.length;
        if (n > 2) {
            double mtotalArea = 0;

            if (isLonLat) {
                double[] lon = new double[x.length];
                double[] lat = new double[y.length];
                for (int i = 0; i < x.length; i++) {
                    lon[i] = Math.toRadians(x[i]);
                    lat[i] = Math.toRadians(y[i]);
                }
                return sphericalPolygonArea(lat, lon, earthRadius);
            } else {
                int i, j;
                double p1x, p1y;
                double p2x, p2y;
                for (i = n - 1, j = 0; j < n; i = j, j++) {

                    p1x = x[i];
                    p1y = y[i];

                    p2x = x[j];
                    p2y = y[j];

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
     * @param isLonLat if is lon/lat
     * @return area
     */
    public static double getArea(List<Coordinate> points, boolean isLonLat) {

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

                    p1x = points.get(i).x;
                    p1y = points.get(i).y;

                    p2x = points.get(j).x;
                    p2y = points.get(j).y;

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
    public static double getArea(List<Coordinate> points) {
        return getArea(points, false);
    }

    /**
     * Get polygon area on earth surface
     *
     * @param points point list
     * @return area
     */
    public static double calArea(List<Coordinate> points) {
        if (points.size() < 3) {
            return 0.0;
        }

        double sum = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            double bx = points.get(i).x;
            double by = points.get(i).y;
            double cx = points.get(i + 1).x;
            double cy = points.get(i + 1).y;
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
    public static double sphericalPolygonArea(List<Coordinate> points) {
        return sphericalPolygonArea(points, EARTH_RADIUS * 1000);
    }

    /**
     * Compute the Area of a Spherical Polygon
     *
     * @param points lon/lat point list
     * @param r spherical radius
     * @return area
     */
    public static double sphericalPolygonArea(List<Coordinate> points, double r) {
        double[] lat = new double[points.size()];
        double[] lon = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            lon[i] = rad(points.get(i).x);
            lat[i] = rad(points.get(i).y);
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
    public static double getDistance(List<Coordinate> points, boolean isLonLat) {
        double tdis = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            double ax = points.get(i).x;
            double ay = points.get(i).y;
            double bx = points.get(i + 1).x;
            double by = points.get(i + 1).y;
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
     * Clip a shape
     *
     * @param aShape The shape
     * @param clipObj Clipping object
     * @return Clipped shape
     */
    public static Shape clipShape(Shape aShape, Object clipObj) {
        switch (aShape.getShapeType()) {
            case POINT:
            case WEATHER_SYMBOL:
            case WIND_ARROW:
            case WIND_BARB:
            case STATION_MODEL:
                return clipPointShape((PointShape) aShape, clipObj);
            case POLYLINE:
                return clipPolylineShape((PolylineShape) aShape, clipObj);
            case POLYGON:
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
        if (polylines.size() == 0) {
            return null;
        }

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
        List<Coordinate> aPList = inPolyLine.getPointList();

        if (!isExtentCross(inPolyLine.getExtent(), clipObj)) {
            return newPolylines;
        }

        int i, j;

        if (clipObj instanceof List) {
            if (!isClockwise((List<Coordinate>) clipObj)) {
                Collections.reverse((List<Coordinate>) clipObj);
            }
        } else if (clipObj.getClass() == ClipLine.class) {
            if (((ClipLine) clipObj).isExtentInside(inPolyLine.getExtent())) {
                newPolylines.add(inPolyLine);
                return newPolylines;
            }
        }

        //Judge if all points of the polyline are in the cut polygon - outline   
        List<List<Coordinate>> newLines = new ArrayList<>();
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
                    List<Coordinate> bPList = new ArrayList<>();
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
        List<Coordinate> clipPList = getClipPointList(clipObj);
        for (Coordinate aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = aP;
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting                     
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = pointInClipObj(clipObj, aPList.get(0));
            Coordinate q1, q2, p1, p2, IPoint = new Coordinate();
            Line lineA, lineB;
            List<Coordinate> newPlist = new ArrayList<>();
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

    public static List<Polygon> clipPolygon(Polygon inPolygon, Object clipObj) {
        List<Polygon> newPolygons = new ArrayList<>();
        List<Polyline> newPolylines = new ArrayList<>();
        List<Coordinate> aPList = (List<Coordinate>) inPolygon.getOutLine();

        if (!isExtentCross(inPolygon.getExtent(), clipObj)) {
            return newPolygons;
        }

        int i, j;

        if (clipObj instanceof List) {
            if (!isClockwise((List<Coordinate>) clipObj)) {
                Collections.reverse((List<Coordinate>) clipObj);
            }
        } else if (clipObj.getClass() == ClipLine.class) {
            if (((ClipLine) clipObj).isExtentInside(inPolygon.getExtent())) {
                newPolygons.add(inPolygon);
                return newPolygons;
            }
        }

        //Judge if all points of the polyline are in the cut polygon - outline   
        List<List<Coordinate>> newLines = new ArrayList<>();
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
                List<Coordinate> bPList = new ArrayList<>();
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
        List<List<Coordinate>> holeLines = new ArrayList<>();
        if (inPolygon.hasHole()) {
            for (int h = 0; h < inPolygon.getHoleLines().size(); h++) {
                List<Coordinate> holePList = inPolygon.getHoleLines().get(h);
                Extent plExtent = GeometryUtil.getPointsExtent(holePList);
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
                        List<Coordinate> bPList = new ArrayList<>();
                        bPList.addAll(holePList.subList(notInIdx, holePList.size()));
                        bPList.addAll(holePList.subList(1, notInIdx));

                        bPList.add(bPList.get(0));
                        newLines.add(bPList);
                    } else //the hole is inside the cut polygon
                    {
                        holeLines.add(inPolygon.getHoleLines().get(h));
                    }
                } else {
                    newLines.add(holePList);
                }
            }
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP = new BorderPoint();
        List<Coordinate> clipPList = getClipPointList(clipObj);
        for (Coordinate aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = (Coordinate) aP.clone();
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting                     
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = false;
            Coordinate q1, q2, p1, p2, IPoint = new Coordinate();
            Line lineA, lineB;
            List<Coordinate> newPlist = new ArrayList<>();
            Polyline bLine;
            p1 = (Coordinate) aPList.get(0).clone();
            int inIdx = -1, outIdx = -1;
            boolean newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = (Coordinate) aPList.get(i).clone();
                if (pointInClipObj(clipObj, p2)) {
                    if (!isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        //q1 = borderList[borderList.Count - 1].Point;
                        q1 = (Coordinate) borderList.get(0).Point.clone();
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = (Coordinate) borderList.get(j).Point.clone();
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = (Coordinate) IPoint.clone();
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
                        q1 = (Coordinate) borderList.get(0).Point.clone();
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = (Coordinate) borderList.get(j).Point.clone();
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
                                aBP.Point = (Coordinate) IPoint.clone();
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

                    try {
                        Polygon aPolygon = inPolygon.getClass().getDeclaredConstructor().newInstance();
                        aPolygon.setOutLine(new ArrayList<>(clipPList));
                        //aPolygon.setHoleLines(new ArrayList<List<PointZ>>());

                        newPolygons.add(aPolygon);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        List<Coordinate> aPList = inPolygon.getOutLine();

        if (!isExtentCross(inPolygon.getExtent(), extent)) {
            return newPolygons;
        }

        int i, j;
        //Judge if all points of the polyline are in the cut polygon - outline   
        List<List<Coordinate>> newLines = new ArrayList<>();
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
                List<Coordinate> bPList = new ArrayList<>();
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
        List<List<Coordinate>> holeLines = new ArrayList<>();
        if (inPolygon.hasHole()) {
            for (int h = 0; h < inPolygon.getHoleLines().size(); h++) {
                List<Coordinate> holePList = inPolygon.getHoleLines().get(h);
                Extent plExtent = GeometryUtil.getPointsExtent(holePList);
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
                        List<Coordinate> bPList = new ArrayList<>();
                        bPList.addAll(holePList.subList(notInIdx, holePList.size()));
                        bPList.addAll(holePList.subList(1, notInIdx));

                        bPList.add(bPList.get(0));
                        newLines.add(bPList);
                    } else //the hole is inside the cut polygon
                    {
                        holeLines.add(inPolygon.getHoleLines().get(h));
                    }
                } else {
                    newLines.add(holePList);
                }
            }
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP = new BorderPoint();
        List<Coordinate> clipPList = getClipPointList(extent);
        for (i = 0; i < clipPList.size(); i++) {
            aBP = new BorderPoint();
            aBP.Point = (Coordinate) clipPList.get(i).clone();
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
            Coordinate q1, q2, p1, p2, IPoint = new Coordinate();
            Line lineA, lineB;
            List<Coordinate> newPlist = new ArrayList<>();
            Polyline bLine;
            p1 = (Coordinate) aPList.get(0).clone();
            int inIdx = -1, outIdx = -1;
            boolean newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = (Coordinate) aPList.get(i).clone();
                if (pointInClipObj(extent, p2)) {
                    if (!isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        //q1 = borderList[borderList.Count - 1].Point;
                        q1 = (Coordinate) borderList.get(0).Point.clone();
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = (Coordinate) borderList.get(j).Point.clone();
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = (Coordinate) IPoint.clone();
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
                        q1 = (Coordinate) borderList.get(0).Point.clone();
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = (Coordinate) borderList.get(j).Point.clone();
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
                                aBP.Point = (Coordinate) IPoint.clone();
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
                                newPlist.add((Coordinate) cBP.Point.clone());
                                switch (cBP.rectPointType) {
                                    case Left:
                                        for (int k = 0; k < borderList.size(); k++) {
                                            if (borderList.get(k).rectPointType == RectPointTypes.LeftBottom) {
                                                for (int m = k; m < borderList.size(); m++) {
                                                    if (cBP.Point.y <= borderList.get(m).Point.y) {
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
                                                    if (cBP.Point.x <= borderList.get(m).Point.x) {
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
                                                    if (borderList.get(m).Point.y <= cBP.Point.y) {
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
                                                    if (borderList.get(m).Point.x <= cBP.Point.x) {
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
                //aPolygon.setHoleLines(new ArrayList<List<PointZ>>());

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
        Coordinate aPoint;
        Polygon aPolygon;
        int i, j;

        aLineList = new ArrayList<>(LineList);

        //---- Tracing border polygon
        List<Coordinate> aPList = new ArrayList<>();
        List<Coordinate> newPList;
        BorderPoint bP;
        int[] timesArray = new int[borderList.size() - 1];
        for (i = 0; i < timesArray.length; i++) {
            timesArray[i] = 0;
        }

        int pIdx, pNum;
        //List<BorderPoint> lineBorderList = new ArrayList<BorderPoint>();

        pNum = borderList.size() - 1;
        Coordinate bPoint, b1Point;
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

                bPoint = (Coordinate) borderList.get(pIdx).Point.clone();
                if (borderList.get(pIdx).Id > -1) {
                    bPoint.x = (bPoint.x + b1Point.x) / 2;
                    bPoint.y = (bPoint.y + b1Point.y) / 2;
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

                            newPList = (List<Coordinate>) new ArrayList<>(aLine.getPointList());
                            aPoint = newPList.get(0);

                            if (!(MIMath.doubleEquals(bP.Point.x, aPoint.x) && MIMath.doubleEquals(bP.Point.y, aPoint.y))) {
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
                                try {
                                    aPolygon = inPolygon.getClass().getDeclaredConstructor().newInstance();
                                    aPolygon.setOutLine(new ArrayList<>(aPList));
                                    //aPolygon.setHoleLines(new ArrayList<List<PointZ>>());
                                    aPolygonList.add(aPolygon);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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

                bPoint = (Coordinate) borderList.get(pIdx).Point.clone();
                if (borderList.get(pIdx).Id > -1) {
                    bPoint.x = (bPoint.x + b1Point.x) / 2;
                    bPoint.y = (bPoint.y + b1Point.y) / 2;
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

                            newPList = new ArrayList<>(aLine.getPointList());
                            aPoint = newPList.get(0);

                            if (!(MIMath.doubleEquals(bP.Point.x, aPoint.x) && MIMath.doubleEquals(bP.Point.y, aPoint.y))) {
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
                                try {
                                    aPolygon = inPolygon.getClass().getDeclaredConstructor().newInstance();
                                    Collections.reverse(aPList);
                                    aPolygon.setOutLine(new ArrayList<>(aPList));
                                    //aPolygon.setHoleLines(new ArrayList<List<PointZ>>());
                                    aPolygonList.add(aPolygon);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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

    private static void addHoles_Ring(List<Polygon> polygonList, List<List<Coordinate>> holeList) {
        int i, j;
        for (i = 0; i < holeList.size(); i++) {
            List<Coordinate> holePs = holeList.get(i);
            Extent aExtent = GeometryUtil.getPointsExtent(holePs);
            for (j = 0; j < polygonList.size(); j++) {
                Polygon aPolygon = polygonList.get(j);
                if (aPolygon.getExtent().include(aExtent)) {
                    boolean isHole = true;
                    for (Coordinate aP : holePs) {
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

    protected static boolean isExtentCross(Extent aExtent, Object clipObj) {
        if (clipObj instanceof List) {
            Extent bExtent = GeometryUtil.getPointsExtent((List<Coordinate>) clipObj);
            return GeometryUtil.isExtentCross(aExtent, bExtent);
        }
        if (clipObj.getClass() == ClipLine.class) {
            return ((ClipLine) clipObj).isExtentCross(aExtent);
        }
        if (clipObj instanceof Extent) {
            return GeometryUtil.isExtentCross(aExtent, (Extent) clipObj);
        }

        return false;
    }

    public static boolean pointInClipObj(Object clipObj, Coordinate aPoint) {
        if (clipObj instanceof List) {
            return pointInPolygon((List<Coordinate>) clipObj, aPoint);
        }
        if (clipObj.getClass() == ClipLine.class) {
            return ((ClipLine) clipObj).isInside(aPoint);
        }
        if (clipObj instanceof Extent) {
            return ((Extent) clipObj).contains(aPoint);
        }

        return false;
    }

    public static List<Coordinate> getClipPointList(Object clipObj) {
        List<Coordinate> clipPList = new ArrayList<>();
        if (clipObj instanceof List) {
            clipPList = (List<Coordinate>) clipObj;
        }
        if (clipObj.getClass() == ClipLine.class) {
            ClipLine clipLine = (ClipLine) clipObj;
            if (clipLine.isLongitude()) {
                for (int i = -100; i <= 100; i++) {
                    clipPList.add(new Coordinate(clipLine.getValue(), i));
                }
            } else {
                for (int i = -370; i <= 370; i++) {
                    clipPList.add(new Coordinate(i, clipLine.getValue()));
                }
            }
        }
        if (clipObj instanceof Extent) {
            Extent aExtent = (Extent) clipObj;
            clipPList.add(new Coordinate(aExtent.minX, aExtent.minY));
            clipPList.add(new Coordinate(aExtent.minX, aExtent.maxY));
            clipPList.add(new Coordinate(aExtent.maxX, aExtent.maxY));
            clipPList.add(new Coordinate(aExtent.maxX, aExtent.minY));
            clipPList.add((Coordinate) clipPList.get(0).clone());
        }

        return clipPList;
    }

    protected static List<Coordinate> getClipPointList(Object clipObj, boolean z) {
        List<Coordinate> clipPList = new ArrayList<>();
        if (clipObj instanceof List) {
            clipPList = (List<Coordinate>) clipObj;
        }
        if (clipObj.getClass() == ClipLine.class) {
            ClipLine clipLine = (ClipLine) clipObj;
            if (z) {
                if (clipLine.isLongitude()) {
                    for (int i = -100; i <= 100; i++) {
                        clipPList.add(new Coordinate(clipLine.getValue(), i, 0));
                    }
                } else {
                    for (int i = -370; i <= 370; i++) {
                        clipPList.add(new Coordinate(i, clipLine.getValue(), 0));
                    }
                }
            } else {
                if (clipLine.isLongitude()) {
                    for (int i = -100; i <= 100; i++) {
                        clipPList.add(new Coordinate(clipLine.getValue(), i));
                    }
                } else {
                    for (int i = -370; i <= 370; i++) {
                        clipPList.add(new Coordinate(i, clipLine.getValue()));
                    }
                }
            }
        }
        if (clipObj instanceof Extent) {
            Extent aExtent = (Extent) clipObj;
            if (z) {
                clipPList.add(new Coordinate(aExtent.minX, aExtent.minY, 0));
                clipPList.add(new Coordinate(aExtent.minX, aExtent.maxY, 0));
                clipPList.add(new Coordinate(aExtent.maxX, aExtent.maxY, 0));
                clipPList.add(new Coordinate(aExtent.maxX, aExtent.minY, 0));
                clipPList.add((Coordinate) clipPList.get(0).clone());
            } else {
                clipPList.add(new Coordinate(aExtent.minX, aExtent.minY));
                clipPList.add(new Coordinate(aExtent.minX, aExtent.maxY));
                clipPList.add(new Coordinate(aExtent.maxX, aExtent.maxY));
                clipPList.add(new Coordinate(aExtent.maxX, aExtent.minY));
                clipPList.add((Coordinate) clipPList.get(0).clone());
            }
        }

        return clipPList;
    }

    private static boolean isLineSegmentCross_old(Line lineA, Line lineB) {
        Extent boundA, boundB;
        List<Coordinate> PListA = new ArrayList<>(), PListB = new ArrayList<>();
        PListA.add(lineA.P1);
        PListA.add(lineA.P2);
        PListB.add(lineB.P1);
        PListB.add(lineB.P2);
        boundA = GeometryUtil.getPointsExtent(PListA);
        boundB = GeometryUtil.getPointsExtent(PListB);

        if (!GeometryUtil.isExtentCross(boundA, boundB)) {
            return false;
        } else {
            double XP1 = (lineB.P1.x - lineA.P1.x) * (lineA.P2.y - lineA.P1.y)
                    - (lineA.P2.x - lineA.P1.x) * (lineB.P1.y - lineA.P1.y);
            double XP2 = (lineB.P2.x - lineA.P1.x) * (lineA.P2.y - lineA.P1.y)
                    - (lineA.P2.x - lineA.P1.x) * (lineB.P2.y - lineA.P1.y);
            if (XP1 * XP2 > 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static boolean isLineSegmentCross(Line lineA, Line lineB) {
        Extent boundA, boundB;
        List<Coordinate> PListA = new ArrayList<>(), PListB = new ArrayList<>();
        PListA.add(lineA.P1);
        PListA.add(lineA.P2);
        PListB.add(lineB.P1);
        PListB.add(lineB.P2);
        boundA = new Extent(Math.min(lineA.P1.x, lineA.P2.x), Math.max(lineA.P1.x, lineA.P2.x),
            Math.min(lineA.P1.y, lineA.P2.y), Math.max(lineA.P1.y, lineA.P2.y));
        boundB = new Extent(Math.min(lineB.P1.x, lineB.P2.x), Math.max(lineB.P1.x, lineB.P2.x),
                Math.min(lineB.P1.y, lineB.P2.y), Math.max(lineB.P1.y, lineB.P2.y));

        if (!GeometryUtil.isExtentCross(boundA, boundB)) {
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
     * 
     * if the sign of the cross product is negative. The vectors make a "left
     * turn" if the sign of the cross product is positive. The vectors are
     * colinear (on the same line) if the cross product is zero.
     *
     * @param p1 Point 1
     * @param p2 Point 2
     * @param p3 Piont 3
     * @return Cross product of the two vectors
     */
    public static double crossProduct(Coordinate p1, Coordinate p2, Coordinate p3) {
        return (p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x) * (p2.y - p1.y);
    }

    /**
     * Finds the point product of the 2 vectors created by the 3 vertices.
     *
     * @param p1 Point 1
     * @param p2 Point 2
     * @param p3 Piont 3
     * @return Cross product of the two vectors
     */
    public static double pointProduct(Coordinate p1, Coordinate p2, Coordinate p3) {
        return (p2.x - p1.x) * (p3.x - p1.x) + (p2.y - p1.y) * (p3.y - p1.y);
    }

    private static boolean lineIntersectRect(Line line, Extent extent) {
        boolean result = false;
        result |= checkRectLineH(line.P1, line.P2, extent.minY, extent.minX, extent.maxX);
        result |= checkRectLineH(line.P1, line.P2, extent.maxY, extent.minX, extent.maxX);
        result |= checkRectLineV(line.P1, line.P2, extent.minX, extent.minY, extent.maxY);
        result |= checkRectLineV(line.P1, line.P2, extent.maxX, extent.minY, extent.maxY);
        return result;
    }

    private static boolean checkRectLineH(Coordinate start, Coordinate end, double y0, double x1, double x2) {
        if ((y0 < start.y) && (y0 < end.y)) {
            return false;
        }

        if ((y0 > start.y) && (y0 > end.y)) {
            return false;
        }

        if (start.y == end.y) {
            if (y0 == start.y) {
                if ((start.x < x1) && (end.x < x1)) {
                    return false;
                }

                if ((start.x > x2) && (end.x > x2)) {
                    return false;
                }

                return true;
            } else {
                return false;
            }
        }

        double x = (end.x - start.x) * (y0 - start.y) / (end.y - start.y) + start.x;
        return ((x >= x1) && (x <= x2));
    }

    private static boolean checkRectLineV(Coordinate start, Coordinate end, double x0, double y1, double y2) {
        if ((x0 < start.x) && (x0 < end.x)) {
            return false;
        }
        if ((x0 > start.x) && (x0 > end.x)) {
            return false;
        }
        if (start.x == end.x) {
            if (x0 == start.x) {
                if ((start.y < y1) && (end.y < y1)) {
                    return false;
                }
                if ((start.y > y2) && (end.y > y2)) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        double y = (end.y - start.y) * (x0 - start.x) / (end.x - start.x) + start.y;
        return ((y >= y1) && (y <= y2));
    }

    private static List<BorderPoint> getCrossPoints(Line line, Extent extent) {
        List<BorderPoint> crossPoints = new ArrayList<>();

        if (line.P1.x <= extent.minX && line.P2.x <= extent.minX) {
            return crossPoints;
        }
        if (line.P1.x >= extent.maxX && line.P2.x >= extent.maxX) {
            return crossPoints;
        }
        if (line.P1.y <= extent.minY && line.P2.y <= extent.minY) {
            return crossPoints;
        }
        if (line.P1.y >= extent.maxY && line.P2.y >= extent.maxY) {
            return crossPoints;
        }

        //Left border
        if (Math.abs(line.P1.x - line.P2.x) - Math.abs(line.P1.x - extent.minX) > 0) {
            double y = (line.P2.y - line.P1.y) * (extent.minX - line.P1.x) / (line.P2.x - line.P1.x) + line.P1.y;
            if ((y >= extent.minY) && (y <= extent.maxY)) {
                BorderPoint bp = new BorderPoint();
                bp.rectPointType = RectPointTypes.Left;
                bp.Point = new Coordinate(extent.minX, y);
                crossPoints.add(bp);
            }
        }

        //Top border
        if (Math.abs(line.P1.y - line.P2.y) - Math.abs(line.P1.y - extent.maxY) > 0) {
            double x = (extent.maxY - line.P1.y) * (line.P2.x - line.P1.x) / (line.P2.y - line.P1.y) + line.P1.x;
            if ((x >= extent.minX) && (x <= extent.maxX)) {
                BorderPoint bp = new BorderPoint();
                bp.rectPointType = RectPointTypes.Top;
                bp.Point = new Coordinate(x, extent.maxY);
                crossPoints.add(bp);
            }
        }

        //Right border
        if (Math.abs(line.P1.x - line.P2.x) - Math.abs(line.P1.x - extent.maxX) > 0) {
            double y = (line.P2.y - line.P1.y) * (extent.maxX - line.P1.x) / (line.P2.x - line.P1.x) + line.P1.y;
            if ((y >= extent.minY) && (y <= extent.maxY)) {
                BorderPoint bp = new BorderPoint();
                bp.rectPointType = RectPointTypes.Right;
                bp.Point = new Coordinate(extent.maxX, y);
                crossPoints.add(bp);
            }
        }

        //Bottom border
        if (Math.abs(line.P1.y - line.P2.y) - Math.abs(line.P1.y - extent.minY) > 0) {
            double x = (extent.minY - line.P1.y) * (line.P2.x - line.P1.x) / (line.P2.y - line.P1.y) + line.P1.x;
            if ((x >= extent.minX) && (x <= extent.maxX)) {
                BorderPoint bp = new BorderPoint();
                bp.rectPointType = RectPointTypes.Bottom;
                bp.Point = new Coordinate(x, extent.minY);
                crossPoints.add(bp);
            }
        }

        return crossPoints;
    }

    public static Coordinate getCrossPoint(Line lineA, Line lineB) {
        Coordinate IPoint = new Coordinate();
        Coordinate p1, p2, q1, q2;
        double tempLeft, tempRight;

        double XP1 = (lineB.P1.x - lineA.P1.x) * (lineA.P2.y - lineA.P1.y)
                - (lineA.P2.x - lineA.P1.x) * (lineB.P1.y - lineA.P1.y);
        double XP2 = (lineB.P2.x - lineA.P1.x) * (lineA.P2.y - lineA.P1.y)
                - (lineA.P2.x - lineA.P1.x) * (lineB.P2.y - lineA.P1.y);
        if (XP1 == 0) {
            IPoint = lineB.P1;
        } else if (XP2 == 0) {
            IPoint = lineB.P2;
        } else {
            p1 = lineA.P1;
            p2 = lineA.P2;
            q1 = lineB.P1;
            q2 = lineB.P2;

            tempLeft = (q2.x - q1.x) * (p1.y - p2.y) - (p2.x - p1.x) * (q1.y - q2.y);
            tempRight = (p1.y - q1.y) * (p2.x - p1.x) * (q2.x - q1.x) + q1.x * (q2.y - q1.y) * (p2.x - p1.x) - p1.x * (p2.y - p1.y) * (q2.x - q1.x);
            IPoint.x = tempRight / tempLeft;

            tempLeft = (p1.x - p2.x) * (q2.y - q1.y) - (p2.y - p1.y) * (q1.x - q2.x);
            tempRight = p2.y * (p1.x - p2.x) * (q2.y - q1.y) + (q2.x - p2.x) * (q2.y - q1.y) * (p1.y - p2.y) - q2.y * (q1.x - q2.x) * (p2.y - p1.y);
            IPoint.y = tempRight / tempLeft;
        }

        if (lineA.P1 instanceof Coordinate) {
            return new Coordinate(IPoint.x, IPoint.y, ((Coordinate) lineA.P1).z, ((Coordinate) lineA.P1).m);
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

    // </editor-fold>

    // <editor-fold desc="Interpolation">
    /**
     * Interpolate with surface method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @return grid data
     */
    public static Array interpolation_Surface(Array x_s, Array y_s, Array a, Array X, Array Y) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        int rowNum, colNum, xn, yn;
        int[] shape = x_s.getShape();
        colNum = shape[1];
        rowNum = shape[0];
        xn = (int) X.getSize();
        yn = (int) Y.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yn, xn});
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, Double.NaN);
        }

        double x, y;
        double v, xll, xtl, xtr, xlr, yll, ytl, ytr, ylr;
        double dX = X.getDouble(1) - X.getDouble(0);
        double dY = Y.getDouble(1) - Y.getDouble(0);
        int minxi, maxxi, minyi, maxyi;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                v = a.getDouble(i * colNum + j);
                if (Double.isNaN(v)) {
                    continue;
                }
                xll = x_s.getDouble(i * colNum + j);
                xtl = x_s.getDouble((i + 1) * colNum + j);
                xtr = x_s.getDouble((i + 1) * colNum + j + 1);
                xlr = x_s.getDouble(i * colNum + j + 1);
                yll = y_s.getDouble(i * colNum + j);
                ytl = y_s.getDouble((i + 1) * colNum + j);
                ytr = y_s.getDouble((i + 1) * colNum + j + 1);
                ylr = y_s.getDouble(i * colNum + j + 1);
                if (Double.isNaN(xll) || Double.isNaN(xtl) || Double.isNaN(xtr) || Double.isNaN(xlr)
                        || Double.isNaN(yll) || Double.isNaN(ytl) || Double.isNaN(ytr) || Double.isNaN(ylr)) {
                    continue;
                }
                PolygonShape ps = new PolygonShape();
                List<Coordinate> points = new ArrayList<>();
                points.add(new Coordinate(xll, yll));
                points.add(new Coordinate(xtl, ytl));
                points.add(new Coordinate(xtr, ytr));
                points.add(new Coordinate(xlr, ylr));
                points.add((Coordinate) points.get(0).clone());
                ps.setPoints(points);
                minxi = (int) ((ps.getExtent().minX - X.getDouble(0)) / dX);
                maxxi = (int) ((ps.getExtent().maxX - X.getDouble(0)) / dX);
                minyi = (int) ((ps.getExtent().minY - Y.getDouble(0)) / dY);
                maxyi = (int) ((ps.getExtent().maxY - Y.getDouble(0)) / dY);
                maxxi += 1;
                maxyi += 1;
                if (maxxi < 0 || minxi >= xn) {
                    continue;
                }
                if (maxyi < 0 || minyi >= yn) {
                    continue;
                }
                if (minxi < 0) {
                    minxi = 0;
                }
                if (maxxi >= xn) {
                    maxxi = xn - 1;
                }
                if (maxyi >= yn) {
                    maxyi = yn - 1;
                }
                for (int m = minyi; m <= maxyi; m++) {
                    y = Y.getDouble(m);
                    for (int n = minxi; n <= maxxi; n++) {
                        x = X.getDouble(n);
                        if (GeoComputation.pointInPolygon(ps, x, y)) {
                            r.setDouble(m * xn + n, v);
                        }
                    }
                }
            }
        }

        return r;
    }
    // </editor-fold>
}
