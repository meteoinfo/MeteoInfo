/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geometry;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.common.*;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.geometry.shape.Polygon;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wyq
 */
public class GeometryUtil {

    /**
     * Get extent from PointF array
     *
     * @param PList PointF array
     * @return The extent
     */
    public static Extent getPointFsExtent(PointF[] PList) {
        Extent cET = new Extent();
        for (int i = 0; i < PList.length; i++) {
            PointF aP = (PointF) PList[i];
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

    /**
     * Get extent from PointF list
     *
     * @param PList PointF list
     * @return The extent
     */
    public static Extent getPointFsExtent(List<PointF> PList) {
        Extent cET = new Extent();
        for (int i = 0; i < PList.size(); i++) {
            PointF aP = (PointF) PList.get(i);
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

    /**
     * Get extent from point list
     *
     * @param PList point list
     * @return extent
     */
    public static Extent getPointsExtent(List<Coordinate> PList) {
        Extent cET = new Extent();
        for (int i = 0; i < PList.size(); i++) {
            Coordinate aP = (Coordinate)PList.get(i);
            if (i == 0) {
                cET.minX = aP.x;
                cET.maxX = aP.x;
                cET.minY = aP.y;
                cET.maxY = aP.y;
                cET.minZ = aP.z;
                cET.maxZ = aP.z;
            } else {
                if (cET.minX > aP.x) {
                    cET.minX = aP.x;
                } else if (cET.maxX < aP.x) {
                    cET.maxX = aP.x;
                }

                if (cET.minY > aP.y) {
                    cET.minY = aP.y;
                } else if (cET.maxY < aP.y) {
                    cET.maxY = aP.y;
                }

                if (cET.minZ > aP.z) {
                    cET.minZ = aP.z;
                } else if (cET.maxZ < aP.z) {
                    cET.maxZ = aP.z;
                }
            }
        }

        return cET;
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
    public static Extent getExtent(Coordinate[] points) {
        Coordinate p = points[0];
        double minx = p.x;
        double maxx = p.x;
        double miny = p.y;
        double maxy = p.y;
        double minz = p.z;
        double maxz = p.z;
        for (int i = 1; i < points.length; i++) {
            p = points[i];
            if (minx > p.x) {
                minx = p.x;
            }
            if (maxx < p.x) {
                maxx = p.x;
            }
            if (miny > p.y) {
                miny = p.y;
            }
            if (maxy < p.y) {
                maxy = p.y;
            }
            if (minz > p.z) {
                minz = p.z;
            }
            if (maxz < p.z) {
                maxz = p.z;
            }
        }

        Extent extent = new Extent();
        extent.minX = minx;
        extent.maxX = maxx;
        extent.minY = miny;
        extent.maxY = maxy;
        extent.minZ = minz;
        extent.maxZ = maxz;

        return extent;
    }

    /**
     * Get extent of the points
     *
     * @param points
     * @return Extent
     */
    public static Extent getExtent(PointF[] points) {
        PointF p = points[0];
        double minx = p.X;
        double maxx = p.X;
        double miny = p.Y;
        double maxy = p.Y;
        for (int i = 1; i < points.length; i++) {
            p = points[i];
            if (minx > p.X) {
                minx = p.X;
            }
            if (maxx < p.X) {
                maxx = p.X;
            }
            if (miny > p.Y) {
                miny = p.Y;
            }
            if (maxy < p.Y) {
                maxy = p.Y;
            }
        }

        Extent extent = new Extent();
        extent.minX = minx;
        extent.maxX = maxx;
        extent.minY = miny;
        extent.maxY = maxy;

        return extent;
    }

    /**
     * Shift extent with longitude
     *
     * @param aET Input extent
     * @param lonShift Longitude shift
     * @return Output extent
     */
    public static Extent shiftExtentLon(Extent aET, double lonShift) {
        Extent cET = new Extent();
        cET.minX = aET.minX + lonShift;
        cET.maxX = aET.maxX + lonShift;
        cET.minY = aET.minY;
        cET.maxY = aET.maxY;

        return cET;
    }

    /**
     * Get maximum extent from two extent
     *
     * @param aET Extent a
     * @param bET Extent b
     * @return Maximum extent
     */
    public static Extent getLagerExtent(Extent aET, Extent bET) {
        Extent cET = new Extent();
        if (aET.isNaN()) {
            return bET;
        } else if (bET.isNaN()) {
            return aET;
        }

        cET.minX = Math.min(aET.minX, bET.minX);
        cET.minY = Math.min(aET.minY, bET.minY);
        cET.maxX = Math.max(aET.maxX, bET.maxX);
        cET.maxY = Math.max(aET.maxY, bET.maxY);
        cET.minZ = Math.min(aET.minZ, bET.minZ);
        cET.maxZ = Math.max(aET.maxZ, bET.maxZ);

        return cET;
    }

    /**
     * Get Minimum extent from two extent
     *
     * @param aET Extent a
     * @param bET Extent b
     * @return Minimum extent
     */
    public static Extent getSmallerExtent(Extent aET, Extent bET) {
        Extent cET = new Extent();
        cET.minX = Math.max(aET.minX, bET.minX);
        cET.minY = Math.max(aET.minY, bET.minY);
        cET.maxX = Math.min(aET.maxX, bET.maxX);
        cET.maxY = Math.min(aET.maxY, bET.maxY);

        return cET;
    }

    /**
     * Determine if two extent cross each other
     *
     * @param aET Extent
     * @param bET Extent
     * @return Boolean
     */
    public static Boolean isExtentCross(Extent aET, Extent bET) {
        if (aET.maxX < bET.minX || aET.maxY < bET.minY || bET.maxX < aET.minX || bET.maxY < aET.minY) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Determine if a point is in an Extent
     *
     * @param aP The point
     * @param aET The extent
     * @return Boolean
     */
    public static boolean pointInExtent(PointF aP, Extent aET) {
        if (aP.X >= aET.minX && aP.X <= aET.maxX && aP.Y >= aET.minY && aP.Y <= aET.maxY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if a point is in an Extent
     *
     * @param aP The point
     * @param aET The extent
     * @return Boolean
     */
    public static boolean pointInExtent(Coordinate aP, Extent aET) {
        return aET.contains(aP);
    }

    /**
     * Determine if a point is in a rectangel
     *
     * @param aP The point
     * @param aRect The rectangel
     * @return Boolean
     */
    public static boolean pointInRectangle(PointF aP, Rectangle aRect) {
        if (aP.X > aRect.x && aP.X < aRect.x + aRect.width && aP.Y > aRect.y && aP.Y < aRect.y + aRect.height) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if a point is in a rectangel
     *
     * @param aP The point
     * @param aRect The rectangel
     * @return Boolean
     */
    public static boolean pointInRectangle(Coordinate aP, Rectangle aRect) {
        if (aP.x > aRect.x && aP.x < aRect.x + aRect.width && aP.y > aRect.y && aP.y < aRect.y + aRect.height) {
            return true;
        } else {
            return false;
        }
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
    public static Coordinate getEllipseXY(double x0, double y0, double a, double b, double angle) {
        double rangle = Math.toRadians(angle);
        double x = (a * b) / Math.sqrt(b * b + a * a * Math.tan(rangle) * Math.tan(rangle));
        if (angle > 90 && angle < 270){
            x = -x;
        }
        double y = Math.tan(rangle) * x;
        if (angle > 0 && angle < 180) {
            y = -Math.abs(y);
        }
        
        return new Coordinate(x + x0, y + y0);
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
    public static List<Coordinate> getEllipseCoordinates(double x0, double y0, double a, double b, double deltaAngle) {
        List<Coordinate> points = new ArrayList<>();
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
    public static List<Coordinate> getEllipseCoordinates(double x0, double y0, double a, double b) {
        List<Coordinate> points = new ArrayList<>();
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
                org.locationtech.jts.geom.Coordinate c = new org.locationtech.jts.geom.Coordinate(xx, yy);
                geos.add(factory.createPoint(c));
            }
        }
        Geometry gs = factory.createGeometryCollection(geos.toArray(new Geometry[geos.size()]));
        Geometry ch = gs.convexHull();
        return new PolygonShape(ch);
    }

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
                    if (GeoComputation.pointInPolygons(polygons, new Coordinate(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
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
                if (GeoComputation.pointInPolygons(polygons, new Coordinate(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
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
            if (GeoComputation.pointInPolygons(polygons, new Coordinate(xIter.getDoubleNext(),
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
        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < x_p.size(); i++) {
            points.add(new Coordinate(x_p.get(i).doubleValue(), y_p.get(i).doubleValue()));
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
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = x_p.getIndexIterator();
        IndexIterator yIter = y_p.getIndexIterator();
        while (xIter.hasNext()) {
            points.add(new Coordinate(xIter.getDoubleNext(), yIter.getDoubleNext()));
        }
        ps.setPoints(points);
        List<PolygonShape> shapes = new ArrayList<>();
        shapes.add(ps);

        return inPolygon(x, y, shapes);
    }

    /**
     * Find polygon index for each point
     * @param x X coordinate of the points
     * @param y Y coordinate of the points
     * @param polygons The polygons
     * @return Polygon index for each point
     */
    public static Array polygonIndex(Array x, Array y, List<PolygonShape> polygons) {
        Array r = Array.factory(DataType.INT, x.getShape());
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        IndexIterator rIter = r.getIndexIterator();
        int idx;
        while (rIter.hasNext()){
            idx = GeoComputation.polygonIndex(polygons, new Coordinate(xIter.getDoubleNext(),
                    yIter.getDoubleNext()));
            rIter.setIntNext(idx);
        }

        return r;
    }

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
            if (GeoComputation.pointInPolygons(polygons, new Coordinate(xIter.getDoubleNext(),
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
            if (GeoComputation.pointInPolygons(polygons, new Coordinate(xIter.getDoubleNext(),
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
            if (GeoComputation.pointInPolygons(polygons, new Coordinate(vx, vy))) {
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
            if (!GeoComputation.pointInPolygons(polygons, new Coordinate(vx, vy))) {
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
                if (GeoComputation.pointInPolygons(polygons, new Coordinate(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
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
                    if (GeoComputation.pointInPolygons(polygons, new Coordinate(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
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
    public static boolean isConvex(List<Coordinate> points) {
        if (points.size() <= 5)
            return true;
        
        Coordinate p0, p1, p2;
        int sign = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            p0 = points.get(i == 0 ? points.size() - 2 : i - 1);
            p1 = points.get(i);
            p2 = points.get(i + 1);
            double dx1 = p1.x - p0.x;
            double dy1 = p1.y - p0.y;
            double dx2 = p2.x - p1.x;
            double dy2 = p2.y - p2.y;
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
        Array zArray = Array.factory(DataType.DOUBLE, shape);
        int i = 0;
        for (Polygon polygon : pgs.getPolygons()) {
            for (List<Coordinate> points : polygon.getRings()) {
                for (Coordinate p : (List<Coordinate>) points) {
                    xArray.setDouble(i, p.x);
                    yArray.setDouble(i, p.y);
                    zArray.setDouble(i, p.z);
                    i += 1;
                }
                if (i < n) {
                    xArray.setDouble(i, Double.NaN);
                    yArray.setDouble(i, Double.NaN);
                    zArray.setDouble(i, Double.NaN);
                }
                i += 1;
            }
        }

        return new Array[]{xArray, yArray, zArray};
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
        Array zArray = Array.factory(DataType.DOUBLE, shape);
        int i = 0;
        for (Polyline polyline : pls.getPolylines()) {
            for (Coordinate p : (List<Coordinate>) polyline.getPointList()) {
                xArray.setDouble(i, p.x);
                yArray.setDouble(i, p.y);
                zArray.setDouble(i, p.z);
                i += 1;
            }
            if (i < n) {
                xArray.setDouble(i, Double.NaN);
                yArray.setDouble(i, Double.NaN);
                zArray.setDouble(i, Double.NaN);
            }
            i += 1;
        }

        return new Array[]{xArray, yArray, zArray};
    }

}
