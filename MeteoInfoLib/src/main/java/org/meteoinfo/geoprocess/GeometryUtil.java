/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geoprocess;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.shape.PolygonShape;

/**
 *
 * @author wyq
 */
public class GeometryUtil {
    // <editor-fold desc="Ellipse">
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
        Geometry[] geos = new Geometry[n];
        GeometryFactory factory = new GeometryFactory();
        for (int i = 0; i < n; i++) {
            Coordinate c = new Coordinate(x.getDouble(i), y.getDouble(i));
            geos[i] = factory.createPoint(c);
        }
        Geometry gs = factory.createGeometryCollection(geos);
        Geometry ch = gs.convexHull();
        return new PolygonShape(ch);
    }
    
    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param layer Polygon vector layer
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, VectorLayer layer) {
        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
        return inPolygon(a, x, y, polygons);
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
        for (int i = 0; i < r.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
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
        for (int i = 0; i < x_p.getSize(); i++) {
            points.add(new PointD(x_p.getDouble(i), y_p.getDouble(i)));
        }
        ps.setPoints(points);
        List<PolygonShape> shapes = new ArrayList<>();
        shapes.add(ps);

        return inPolygon(x, y, shapes);
    }
    
    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param layer VectorLayer
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, VectorLayer layer, Number missingValue) {
        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
        return maskout(a, x, y, polygons, missingValue);
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
        for (int i = 0; i < a.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                r.setObject(i, a.getObject(i));
            } else {
                r.setObject(i, Double.NaN);
            }
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
        for (int i = 0; i < a.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                r.setObject(i, Double.NaN);
            } else {
                r.setObject(i, a.getObject(i));
            }
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
        for (int i = 0; i < a.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                rdata.add(a.getObject(i));
                rxdata.add(x.getDouble(i));
                rydata.add(y.getDouble(i));
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
        for (int i = 0; i < a.getSize(); i++) {
            if (!GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                rdata.add(a.getObject(i));
                rxdata.add(x.getDouble(i));
                rydata.add(y.getDouble(i));
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
        if (a.getRank() == 1) {
            for (int i = 0; i < xNum; i++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
                    r.setObject(i, a.getObject(i));
                } else {
                    r.setObject(i, missingValue);
                }
            }
        } else if (a.getRank() == 2) {
            int idx;
            for (int i = 0; i < yNum; i++) {
                for (int j = 0; j < xNum; j++) {
                    idx = i * xNum + j;
                    if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                        r.setObject(idx, a.getObject(idx));
                    } else {
                        r.setObject(idx, missingValue);
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
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, missingValue);
            } else {
                r.setObject(i, a.getObject(i));
            }
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
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, Double.NaN);
            } else {
                r.setObject(i, a.getObject(i));
            }
        }

        return r;
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
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, a.getObject(i));
            } else {
                r.setObject(i, Double.NaN);
            }
        }

        return r;
    }
    // </editor-fold>
}
