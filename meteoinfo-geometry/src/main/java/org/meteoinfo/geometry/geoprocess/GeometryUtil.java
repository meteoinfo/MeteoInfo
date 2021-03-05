/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geometry.geoprocess;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.ndarray.*;

/**
 *
 * @author wyq
 */
public class GeometryUtil {

    /**
     * Get extent from point list
     *
     * @param PList point list
     * @return extent
     */
    public static Extent getPointsExtent(List<? extends PointD> PList) {
        if (PList.get(0) instanceof PointZ){
            Extent3D cET = new Extent3D();
            for (int i = 0; i < PList.size(); i++) {
                PointZ aP = (PointZ)PList.get(i);
                if (i == 0) {
                    cET.minX = aP.X;
                    cET.maxX = aP.X;
                    cET.minY = aP.Y;
                    cET.maxY = aP.Y;
                    cET.minZ = aP.Z;
                    cET.maxZ = aP.Z;
                } else {
                    if (cET.minX > aP.X) {
                        cET.minX = aP.X;
                    } else if (cET.maxX < aP.X) {
                        cET.maxX = aP.X;
                    }

                    if (cET.minY > aP.Y) {
                        cET.minY = aP.Y;
                    } else if (cET.maxY < aP.Y) {
                        cET.maxY = aP.Y;
                    }

                    if (cET.minZ > aP.Z) {
                        cET.minZ = aP.Z;
                    } else if (cET.maxZ < aP.Z) {
                        cET.maxZ = aP.Z;
                    }
                }
            }

            return cET;
        } else {
            Extent cET = new Extent();
            for (int i = 0; i < PList.size(); i++) {
                PointD aP = PList.get(i);
                if (i == 0) {
                    cET.minX = aP.X;
                    cET.maxX = aP.X;
                    cET.minY = aP.Y;
                    cET.maxY = aP.Y;
                } else {
                    if (cET.minX > aP.X) {
                        cET.minX = aP.X;
                    } else if (cET.maxX < aP.X) {
                        cET.maxX = aP.X;
                    }

                    if (cET.minY > aP.Y) {
                        cET.minY = aP.Y;
                    } else if (cET.maxY < aP.Y) {
                        cET.maxY = aP.Y;
                    }
                }
            }

            return cET;
        }
    }

    /**
     * Get extent of the shapes
     *
     * @param shapes
     * @return Extent
     */
    public static Extent getExtent(List<? extends Shape> shapes) {
        Extent extent = (Extent) shapes.get(0).getExtent().clone();
        double minx = extent.minX;
        double maxx = extent.maxX;
        double miny = extent.minY;
        double maxy = extent.maxY;
        Extent ext;
        for (int i = 1; i < shapes.size(); i++) {
            ext = shapes.get(i).getExtent();
            if (minx > ext.minX) {
                minx = ext.minX;
            }
            if (maxx < ext.maxX) {
                maxx = ext.maxX;
            }
            if (miny > ext.minY) {
                miny = ext.minY;
            }
            if (maxy < ext.maxY) {
                maxy = ext.maxY;
            }
        }

        extent.minX = minx;
        extent.maxX = maxx;
        extent.minY = miny;
        extent.maxY = maxy;

        return extent;
    }

    /**
     * Get extent of the points
     *
     * @param points
     * @return Extent
     */
    public static Extent3D getExtent(PointZ[] points) {
        PointZ p = points[0];
        double minx = p.X;
        double maxx = p.X;
        double miny = p.Y;
        double maxy = p.Y;
        double minz = p.Z;
        double maxz = p.Z;
        for (int i = 1; i < points.length; i++) {
            if (minx > p.X) {
                minx = p.M;
            }
            if (maxx < p.X) {
                maxx = p.M;
            }
            if (miny > p.Y) {
                miny = p.Y;
            }
            if (maxy < p.Y) {
                maxy = p.Y;
            }
            if (minz > p.Z) {
                minz = p.Z;
            }
            if (maxz < p.Z) {
                maxz = p.Z;
            }
        }

        Extent3D extent = new Extent3D();
        extent.minX = minx;
        extent.maxX = maxx;
        extent.minY = miny;
        extent.maxY = maxy;
        extent.minZ = minz;
        extent.maxZ = maxz;

        return extent;
    }

    /**
     * Get ellipse coordinate
     * @param x0 Center x
     * @param y0 Center y
     * @param a Major axis
     * @param b Minor axis
     * @param angle Angle
     * @return Coordinate on the ellipse
     */
    public static PointD getEllipseXY(double x0, double y0, double a, double b, double angle) {
        double rangle = Math.toRadians(angle);
        double x = (a * b) / Math.sqrt(b * b + a * a * Math.tan(rangle) * Math.tan(rangle));
        if (angle > 90 && angle < 270){
            x = -x;
        }
        double y = Math.tan(rangle) * x;
        if (angle > 0 && angle < 180) {
            y = -Math.abs(y);
        }
        
        return new PointD(x + x0, y + y0);
    }
    
    /**
     * Get ellipse coordinates
     * @param x0 Center x
     * @param y0 Center y
     * @param a Major axis
     * @param b Minor axis
     * @param deltaAngle Delta angle
     * @return Coordinate on the ellipse
     */
    public static List<PointD> getEllipseCoordinates(double x0, double y0, double a, double b, double deltaAngle) {
        List<PointD> points = new ArrayList<>();
        for (double angle = 0; angle <= 360; angle += deltaAngle){
            points.add(getEllipseXY(x0, y0, a, b, angle));
        }
        
        return points;
    }
    
    /**
     * Get ellipse coordinates
     * @param x0 Center x
     * @param y0 Center y
     * @param a Major axis
     * @param b Minor axis
     * @return Coordinate on the ellipse
     */
    public static List<PointD> getEllipseCoordinates(double x0, double y0, double a, double b) {
        List<PointD> points = new ArrayList<>();
        double deltaAngle = 1;
        for (double angle = 0; angle <= 360; angle += deltaAngle){
            points.add(getEllipseXY(x0, y0, a, b, angle));
        }
        
        return points;
    }
    
    /**
     * Computes the smallest convex <code>Polygon</code> that contains all the
     * points
     *
     * @param x X array
     * @param y Y array
     * @return PolygonShape
     */
    public static PolygonShape convexHull(Array x, Array y) {
        int n = (int) x.getSize();
        List<Geometry> geos = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        double xx, yy;
        while(xIter.hasNext()) {
            xx = xIter.getDoubleNext();
            yy = yIter.getDoubleNext();
            if (!Double.isNaN(xx) && !Double.isNaN(yy)) {
                Coordinate c = new Coordinate(xx, yy);
                geos.add(factory.createPoint(c));
            }
        }
        Geometry gs = factory.createGeometryCollection(geos.toArray(new Geometry[geos.size()]));
        Geometry ch = gs.convexHull();
        return new PolygonShape(ch);
    }
    
//    /**
//     * In polygon function
//     *
//     * @param a Array a
//     * @param x X dimension values
//     * @param y Y dimension values
//     * @param layer Polygon vector layer
//     * @return Result array with cell values of 1 inside polygons and -1 outside
//     * polygons
//     */
//    public static Array inPolygon(Array a, List<Number> x, List<Number> y, VectorLayer layer) {
//        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
//        return inPolygon(a, x, y, polygons);
//    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param ps Polygon shape
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, PolygonShape ps) {
        List<PolygonShape> polygons = new ArrayList<>();
        polygons.add(ps);
        return inPolygon(a, x, y, polygons);
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons) {
        if (a.getRank() == 2) {
            int xNum = x.size();
            int yNum = y.size();

            Array r = Array.factory(DataType.INT, a.getShape());
            for (int i = 0; i < yNum; i++) {
                for (int j = 0; j < xNum; j++) {
                    if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                        r.setInt(i * xNum + j, 1);
                    } else {
                        r.setInt(i * xNum + j, -1);
                    }
                }
            }

            return r;
        } else if (a.getRank() == 1) {
            int n = x.size();
            Array r = Array.factory(DataType.INT, a.getShape());
            for (int i = 0; i < n; i++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
                    r.setInt(i, 1);
                } else {
                    r.setInt(i, -1);
                }
            }

            return r;
        }

        return null;
    }

    /**
     * In polygon function
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param polygons PolygonShape list
     * @return Result boolean array
     */
    public static Array inPolygon(Array x, Array y, List<PolygonShape> polygons) {
        Array r = Array.factory(DataType.BOOLEAN, x.getShape());
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        IndexIterator rIter = r.getIndexIterator();
        while (rIter.hasNext()){
            if (GeoComputation.pointInPolygons(polygons, new PointD(xIter.getDoubleNext(),
                    yIter.getDoubleNext()))) {
                rIter.setBooleanNext(true);
            } else {
                rIter.setBooleanNext(false);
            }
        }

        return r;
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param x_p X coordinate of the polygon
     * @param y_p Y coordinate of the polygon
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, List<Number> x_p, List<Number> y_p) {
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x_p.size(); i++) {
            points.add(new PointD(x_p.get(i).doubleValue(), y_p.get(i).doubleValue()));
        }
        ps.setPoints(points);
        List<PolygonShape> shapes = new ArrayList<>();
        shapes.add(ps);

        return inPolygon(a, x, y, shapes);
    }

    /**
     * In polygon function
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param x_p X coordinate of the polygon
     * @param y_p Y coordinate of the polygon
     * @return Result boolean array
     */
    public static Array inPolygon(Array x, Array y, Array x_p, Array y_p) {
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        IndexIterator xIter = x_p.getIndexIterator();
        IndexIterator yIter = y_p.getIndexIterator();
        while (xIter.hasNext()) {
            points.add(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
        }
        ps.setPoints(points);
        List<PolygonShape> shapes = new ArrayList<>();
        shapes.add(ps);

        return inPolygon(x, y, shapes);
    }
    
//    /**
//     * Maskout function
//     *
//     * @param a Array a
//     * @param x X dimension values
//     * @param y Y dimension values
//     * @param layer VectorLayer
//     * @param missingValue Missing value
//     * @return Result array with cell values of missing outside polygons
//     */
//    public static Array maskout(Array a, List<Number> x, List<Number> y, VectorLayer layer, Number missingValue) {
//        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
//        return maskout(a, x, y, polygons, missingValue);
//    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygon Polygon shape
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, PolygonShape polygon, Number missingValue) {
        List<PolygonShape> polygons = new ArrayList<>();
        polygons.add(polygon);
        return maskout(a, x, y, polygons, missingValue);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskout
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, Array x, Array y, List<PolygonShape> polygons) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        IndexIterator aIter = a.getIndexIterator();
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        int i = 0;
        while (aIter.hasNext()){
            if (GeoComputation.pointInPolygons(polygons, new PointD(xIter.getDoubleNext(),
                    yIter.getDoubleNext()))) {
                r.setObject(i, aIter.getObjectNext());
            } else {
                r.setObject(i, Double.NaN);
                aIter.next();
            }
            i++;
        }
        return r;
    }

    /**
     * Maskin function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskin
     * @return Result array with cell values of missing inside polygons
     */
    public static Array maskin(Array a, Array x, Array y, List<PolygonShape> polygons) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        IndexIterator aIter = a.getIndexIterator();
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        int i = 0;
        while(aIter.hasNext()) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(xIter.getDoubleNext(),
                    yIter.getDoubleNext()))) {
                r.setObject(i, Double.NaN);
                aIter.next();
            } else {
                r.setObject(i, aIter.getObjectNext());
            }
            i++;
        }
        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskout
     * @return Result arrays removing cells outside polygons
     */
    public static Array[] maskout_Remove(Array a, Array x, Array y, List<PolygonShape> polygons) {
        List<Object> rdata = new ArrayList<>();
        List<Double> rxdata = new ArrayList<>();
        List<Double> rydata = new ArrayList<>();
        IndexIterator aIter = a.getIndexIterator();
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        double va, vx, vy;
        while(aIter.hasNext()) {
            va = aIter.getDoubleNext();
            vx = xIter.getDoubleNext();
            vy = yIter.getDoubleNext();
            if (GeoComputation.pointInPolygons(polygons, new PointD(vx, vy))) {
                rdata.add(va);
                rxdata.add(vx);
                rydata.add(vy);
            }
        }

        int n = rdata.size();
        int[] shape = new int[1];
        shape[0] = n;
        Array r = Array.factory(a.getDataType(), shape);
        Array rx = Array.factory(x.getDataType(), shape);
        Array ry = Array.factory(y.getDataType(), shape);
        for (int i = 0; i < n; i++) {
            r.setObject(i, rdata.get(i));
            rx.setDouble(i, rxdata.get(i));
            ry.setDouble(i, rydata.get(i));
        }

        return new Array[]{r, rx, ry};
    }

    /**
     * Maskin function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskin
     * @return Result arrays removing cells inside polygons
     */
    public static Array[] maskin_Remove(Array a, Array x, Array y, List<PolygonShape> polygons) {
        List<Object> rdata = new ArrayList<>();
        List<Double> rxdata = new ArrayList<>();
        List<Double> rydata = new ArrayList<>();
        IndexIterator aIter = a.getIndexIterator();
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        double va, vx, vy;
        while(aIter.hasNext()) {
            va = aIter.getDoubleNext();
            vx = xIter.getDoubleNext();
            vy = yIter.getDoubleNext();
            if (!GeoComputation.pointInPolygons(polygons, new PointD(vx, vy))) {
                rdata.add(va);
                rxdata.add(vx);
                rydata.add(vy);
            }
        }

        int n = rdata.size();
        int[] shape = new int[1];
        shape[0] = n;
        Array r = Array.factory(a.getDataType(), shape);
        Array rx = Array.factory(x.getDataType(), shape);
        Array ry = Array.factory(y.getDataType(), shape);
        for (int i = 0; i < n; i++) {
            r.setObject(i, rdata.get(i));
            rx.setDouble(i, rxdata.get(i));
            ry.setDouble(i, rydata.get(i));
        }

        return new Array[]{r, rx, ry};
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons) {
        return maskout(a, x, y, polygons, Double.NaN);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons, Number missingValue) {
        int xNum = x.size();
        int yNum = y.size();

        Array r = Array.factory(a.getDataType(), a.getShape());
        IndexIterator iter = a.getIndexIterator();
        if (a.getRank() == 1) {
            int i = 0;
            while (iter.hasNext()) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
                    r.setObject(i, iter.getObjectNext());
                } else {
                    r.setObject(i, missingValue);
                    iter.next();
                }
                i++;
            }
        } else if (a.getRank() == 2) {
            int idx;
            for (int i = 0; i < yNum; i++) {
                for (int j = 0; j < xNum; j++) {
                    idx = i * xNum + j;
                    if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                        r.setObject(idx, iter.getObjectNext());
                    } else {
                        r.setObject(idx, missingValue);
                        iter.next();
                    }
                }
            }
        }

        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param m Array mask
     * @param missingValue Missing value
     * @return Result array
     */
    public static Array maskout(Array a, Array m, Number missingValue) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        IndexIterator aIter = a.getIndexIterator();
        IndexIterator mIter = m.getIndexIterator();
        double va, vm;
        int i = 0;
        while (aIter.hasNext()){
            va = aIter.getDoubleNext();
            vm = mIter.getDoubleNext();
            if (vm < 0) {
                r.setObject(i, missingValue);
            } else {
                r.setObject(i, va);
            }
            i++;
        }

        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param m Array mask
     * @return Result array
     */
    public static Array maskout(Array a, Array m) {
        return maskout(a, m, Double.NaN);
    }

    /**
     * Maskin function
     *
     * @param a Array a
     * @param m Array mask
     * @return Result array
     */
    public static Array maskin(Array a, Array m) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        IndexIterator aIter = a.getIndexIterator();
        IndexIterator mIter = m.getIndexIterator();
        double va, vm;
        int i = 0;
        while(aIter.hasNext()) {
            va = aIter.getDoubleNext();
            vm = mIter.getDoubleNext();
            if (vm < 0) {
                r.setObject(i, va);
            } else {
                r.setObject(i, Double.NaN);
            }
            i++;
        }

        return r;
    }
    
    /**
     * Check if a polygon is convex
     * @param points Outline point of the polygon
     * @return Is convex or not
     */
    public static boolean isConvex(List<? extends PointD> points) {
        if (points.size() <= 5)
            return true;
        
        PointD p0, p1, p2;
        int sign = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            p0 = points.get(i == 0 ? points.size() - 2 : i - 1);
            p1 = points.get(i);
            p2 = points.get(i + 1);
            double dx1 = p1.X - p0.X;
            double dy1 = p1.Y - p0.Y;
            double dx2 = p2.X - p1.X;
            double dy2 = p2.Y - p2.Y;
            double z = dx1 * dy2 - dy1 * dx2;
            int s = z >= 0.0 ? 1 : -1;
            if(sign == 0) {
                sign = s; 
            } else if(sign != s) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if a polygon is convex
     * @param polygon The polygon
     * @return Is convex or not
     */
    public static boolean isConvex(Polygon polygon) {
        return isConvex(polygon.getOutLine());
    }

    /**
     * Get polygon shape coordinate arrays
     * @param pgs The polygon shape
     * @return Coordinate arrays
     */
    public static Array[] getCoordinates(PolygonShape pgs) {
        int n = pgs.getPointNum() + pgs.getPartNum() - 1;
        int[] shape = new int[]{n};
        Array xArray = Array.factory(DataType.DOUBLE, shape);
        Array yArray = Array.factory(DataType.DOUBLE, shape);
        Array zArray = null;
        boolean isZ = pgs instanceof PolygonZShape ? true : false;
        if (isZ) {
            zArray = Array.factory(DataType.DOUBLE, shape);
        }
        int i = 0;
        for (Polygon polygon : pgs.getPolygons()) {
            for (List points : polygon.getRings()) {
                if (isZ) {
                    for (PointZ p : (List<PointZ>) points) {
                        xArray.setDouble(i, p.X);
                        yArray.setDouble(i, p.Y);
                        zArray.setDouble(i, p.Z);
                        i += 1;
                    }
                    if (i < n) {
                        xArray.setDouble(i, Double.NaN);
                        yArray.setDouble(i, Double.NaN);
                        zArray.setDouble(i, Double.NaN);
                    }
                } else {
                    for (PointD p : (List<PointD>) points) {
                        xArray.setDouble(i, p.X);
                        yArray.setDouble(i, p.Y);
                        i += 1;
                    }
                    if (i < n) {
                        xArray.setDouble(i, Double.NaN);
                        yArray.setDouble(i, Double.NaN);
                    }
                }
                i += 1;
            }
        }

        if (isZ) {
            return new Array[]{xArray, yArray, zArray};
        } else {
            return new Array[]{xArray, yArray};
        }
    }

    /**
     * Get polyline shape coordinate arrays
     * @param pls The polygon shape
     * @return Coordinate arrays
     */
    public static Array[] getCoordinates(PolylineShape pls) {
        int n = pls.getPointNum() + pls.getPartNum() - 1;
        int[] shape = new int[]{n};
        Array xArray = Array.factory(DataType.DOUBLE, shape);
        Array yArray = Array.factory(DataType.DOUBLE, shape);
        Array zArray = null;
        boolean isZ = pls instanceof PolylineZShape ? true : false;
        if (isZ) {
            zArray = Array.factory(DataType.DOUBLE, shape);
        }
        int i = 0;
        for (Polyline polyline : pls.getPolylines()) {
            if (isZ) {
                for (PointZ p : (List<PointZ>) polyline.getPointList()) {
                    xArray.setDouble(i, p.X);
                    yArray.setDouble(i, p.Y);
                    zArray.setDouble(i, p.Z);
                    i += 1;
                }
                if (i < n) {
                    xArray.setDouble(i, Double.NaN);
                    yArray.setDouble(i, Double.NaN);
                    zArray.setDouble(i, Double.NaN);
                }
            } else {
                for (PointD p : (List<PointD>) polyline.getPointList()) {
                    xArray.setDouble(i, p.X);
                    yArray.setDouble(i, p.Y);
                    i += 1;
                }
                if (i < n) {
                    xArray.setDouble(i, Double.NaN);
                    yArray.setDouble(i, Double.NaN);
                }
            }
            i += 1;
        }

        if (isZ) {
            return new Array[]{xArray, yArray, zArray};
        } else {
            return new Array[]{xArray, yArray};
        }
    }

//    /**
//     * Get coordinate arrays of a vector layer
//     * @param layer The vector layer
//     * @return Coordinate arrays
//     */
//    public static Array[] getCoordinates(VectorLayer layer) {
//        boolean isZ = layer.getShapeType().isZ();
//        if (layer.getShapeType().isPoint()) {
//            int n = layer.getShapeNum();
//            Array xArray = Array.factory(DataType.DOUBLE, new int[]{n});
//            Array yArray = Array.factory(DataType.DOUBLE, new int[]{n});
//            Array zArray = null;
//            if (isZ) {
//                zArray = Array.factory(DataType.DOUBLE, new int[]{n});
//                int i = 0;
//                PointZ p;
//                for (PointShape shape : (List<PointShape>) layer.getShapes()) {
//                    p = (PointZ) shape.getPoint();
//                    xArray.setDouble(i, p.X);
//                    yArray.setDouble(i, p.Y);
//                    zArray.setDouble(i, p.Z);
//                }
//                return new Array[]{xArray, yArray, zArray};
//            } else {
//                int i = 0;
//                PointD p;
//                for (PointShape shape : (List<PointShape>) layer.getShapes()) {
//                    p = (PointD) shape.getPoint();
//                    xArray.setDouble(i, p.X);
//                    yArray.setDouble(i, p.Y);
//                }
//                return new Array[]{xArray, yArray};
//            }
//        } else {
//            int n = 0;
//            for (Shape shape : layer.getShapes()) {
//                n += shape.getPointNum() + shape.getPartNum();
//            }
//            n -= 1;
//            Array xArray = Array.factory(DataType.DOUBLE, new int[]{n});
//            Array yArray = Array.factory(DataType.DOUBLE, new int[]{n});
//            Array zArray = null;
//            if (isZ) {
//                zArray = Array.factory(DataType.DOUBLE, new int[]{n});
//            }
//            int i = 0;
//            int[] origin = new int[1];
//            int[] shape = new int[1];
//            if (layer.getShapeType().isLine()) {
//                for (PolylineShape pls : (List<PolylineShape>) layer.getShapes()) {
//                    Array[] arrays = getCoordinates(pls);
//                    origin[0] = i;
//                    shape[0] = (int) arrays[0].getSize();
//                    try {
//                        ArrayMath.setSection(xArray, origin, shape, arrays[0]);
//                        ArrayMath.setSection(yArray, origin, shape, arrays[1]);
//                        if (isZ)
//                            ArrayMath.setSection(zArray, origin, shape, arrays[2]);
//                        i += shape[0];
//                        if (i < n) {
//                            xArray.setDouble(i, Double.NaN);
//                            yArray.setDouble(i, Double.NaN);
//                            if (isZ)
//                                zArray.setDouble(i, Double.NaN);
//                        }
//                        i += 1;
//                    } catch (InvalidRangeException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                for (PolygonShape pgs : (List<PolygonShape>) layer.getShapes()) {
//                    Array[] arrays = getCoordinates(pgs);
//                    origin[0] = i;
//                    shape[0] = (int) arrays[0].getSize();
//                    try {
//                        ArrayMath.setSection(xArray, origin, shape, arrays[0]);
//                        ArrayMath.setSection(yArray, origin, shape, arrays[1]);
//                        if (isZ)
//                            ArrayMath.setSection(zArray, origin, shape, arrays[2]);
//                        i += shape[0];
//                        if (i < n) {
//                            xArray.setDouble(i, Double.NaN);
//                            yArray.setDouble(i, Double.NaN);
//                            if (isZ)
//                                zArray.setDouble(i, Double.NaN);
//                        }
//                        i += 1;
//                    } catch (InvalidRangeException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            if (isZ) {
//                return new Array[]{xArray, yArray, zArray};
//            } else {
//                return new Array[]{xArray, yArray};
//            }
//        }
//    }

}
