/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.PointD;
import ucar.ma2.Array;

/**
 *
 * @author wyq
 */
public class ShapeUtil {

    /**
     * Create point shapes
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @return Point shapes
     */
    public static List<PointShape> createPointShapes(List<Number> x, List<Number> y) {
        double xx, yy;
        PointShape ps;
        List<PointShape> shapes = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            ps = new PointShape();
            xx = x.get(i).doubleValue();
            yy = y.get(i).doubleValue();
            ps.setPoint(new PointD(xx, yy));
            shapes.add(ps);
        }
        return shapes;
    }

    /**
     * Create point shapes
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @return Point shapes
     */
    public static List<PointShape> createPointShapes(Array x, Array y) {
        double xx, yy;
        PointShape ps;
        List<PointShape> shapes = new ArrayList<>();
        for (int i = 0; i < x.getSize(); i++) {
            ps = new PointShape();
            xx = x.getDouble(i);
            yy = y.getDouble(i);
            ps.setPoint(new PointD(xx, yy));
            shapes.add(ps);
        }
        return shapes;
    }
    
    /**
     * Create PointZ shapes
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param z Z coordinates
     * @param m M coordinates
     * @return PointZ shapes
     */
    public static List<PointZShape> createPointShapes(Array x, Array y, Array z, Array m) {
        double xx, yy;
        PointZShape ps;
        List<PointZShape> shapes = new ArrayList<>();
        for (int i = 0; i < x.getSize(); i++) {
            ps = new PointZShape();
            xx = x.getDouble(i);
            yy = y.getDouble(i);
            
            ps.setPoint(new PointZ(xx, yy, z.getDouble(i), m.getDouble(i)));
            shapes.add(ps);
        }
        return shapes;
    }

    /**
     * Create polyline shapes
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @return Polyline shapes
     */
    public static List<PolylineShape> createPolylineShapes(List<Number> x, List<Number> y) {
        double xx, yy;
        List<PointD> points = new ArrayList<>();
        PolylineShape pls;
        List<PolylineShape> shapes = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            xx = x.get(i).doubleValue();
            yy = y.get(i).doubleValue();
            if (Double.isNaN(xx)) {
                if (points.size() >= 2) {
                    pls = new PolylineShape();
                    pls.setPoints(points);
                    shapes.add(pls);
                }
                points = new ArrayList<>();
            } else {
                points.add(new PointD(xx, yy));
            }
        }
        if (points.size() >= 2) {
            pls = new PolylineShape();
            pls.setPoints(points);
            shapes.add(pls);
        }

        return shapes;
    }
    
    /**
     * Create polyline shapes
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @return Polyline shapes
     */
    public static List<PolylineShape> createPolylineShapes(Array x, Array y) {
        double xx, yy;
        List<PointD> points = new ArrayList<>();
        PolylineShape pls;
        List<PolylineShape> shapes = new ArrayList<>();
        for (int i = 0; i < x.getSize(); i++) {
            xx = x.getDouble(i);
            yy = y.getDouble(i);
            if (Double.isNaN(xx)) {
                if (points.size() >= 2) {
                    pls = new PolylineShape();
                    pls.setPoints(points);
                    shapes.add(pls);
                }
                points = new ArrayList<>();
            } else {
                points.add(new PointD(xx, yy));
            }
        }
        if (points.size() >= 2) {
            pls = new PolylineShape();
            pls.setPoints(points);
            shapes.add(pls);
        }

        return shapes;
    }
    
    /**
     * Create polylineZ shapes
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param z Z coordinates
     * @param m M coordinates
     * @return PolylineZ shapes
     */
    public static List<PolylineZShape> createPolylineShapes(Array x, Array y, Array z, Array m) {
        double xx, yy;
        List<PointD> points = new ArrayList<>();
        PolylineZShape pls;
        List<PolylineZShape> shapes = new ArrayList<>();
        for (int i = 0; i < x.getSize(); i++) {
            xx = x.getDouble(i);
            yy = y.getDouble(i);
            if (Double.isNaN(xx)) {
                if (points.size() >= 2) {
                    pls = new PolylineZShape();
                    pls.setPoints(points);
                    shapes.add(pls);
                }
                points = new ArrayList<>();
            } else {
                points.add(new PointZ(xx, yy, z.getDouble(i), m.getDouble(i)));
            }
        }
        if (points.size() >= 2) {
            pls = new PolylineZShape();
            pls.setPoints(points);
            shapes.add(pls);
        }

        return shapes;
    }
    
    /**
     * Create polygon shapes
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @return Polygon shapes
     */
    public static List<PolygonShape> createPolygonShapes(List<Number> x, List<Number> y) {
        double xx, yy;
        List<PointD> points = new ArrayList<>();
        PolygonShape pls;
        List<PolygonShape> shapes = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            xx = x.get(i).doubleValue();
            yy = y.get(i).doubleValue();
            if (Double.isNaN(xx)) {
                if (points.size() > 2) {
                    pls = new PolygonShape();
                    pls.setPoints(points);
                    shapes.add(pls);
                }
                points = new ArrayList<>();
            } else {
                points.add(new PointD(xx, yy));
            }
        }
        if (points.size() > 2) {
            pls = new PolygonShape();
            pls.setPoints(points);
            shapes.add(pls);
        }

        return shapes;
    }
    
    /**
     * Create polygon shapes
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @return Polygon shapes
     */
    public static List<PolygonShape> createPolygonShapes(Array x, Array y) {
        double xx, yy;
        List<PointD> points = new ArrayList<>();
        PolygonShape pls;
        List<PolygonShape> shapes = new ArrayList<>();
        for (int i = 0; i < x.getSize(); i++) {
            xx = x.getDouble(i);
            yy = y.getDouble(i);
            if (Double.isNaN(xx)) {
                if (points.size() > 2) {
                    pls = new PolygonShape();
                    pls.setPoints(points);
                    shapes.add(pls);
                }
                points = new ArrayList<>();
            } else {
                points.add(new PointD(xx, yy));
            }
        }
        if (points.size() > 2) {
            pls = new PolygonShape();
            pls.setPoints(points);
            shapes.add(pls);
        }

        return shapes;
    }
    
    /**
     * Create polygon shape
     *
     * @param x_p X coordinate list
     * @param y_p Y coordinate list
     * @return Polygon shape
     */
    public static PolygonShape createPolygonShape(List<Number> x_p, List<Number> y_p) {
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x_p.size(); i++) {
            points.add(new PointD(x_p.get(i).doubleValue(), y_p.get(i).doubleValue()));
        }
        if (!points.get(points.size() - 1).equals(points.get(0))) {
            points.add((PointD) points.get(0).clone());
        }
        ps.setPoints(points);

        return ps;
    }

    /**
     * Create polygon shape
     *
     * @param xy X/Y coordinates
     * @return Polygon shape
     */
    public static PolygonShape createPolygonShape(List<List<Number>> xy) {
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (List<Number> xy1 : xy) {
            points.add(new PointD(xy1.get(0).doubleValue(), xy1.get(1).doubleValue()));
        }
        if (!points.get(points.size() - 1).equals(points.get(0))) {
            points.add((PointD) points.get(0).clone());
        }
        ps.setPoints(points);

        return ps;
    }
    
    /**
     * Add a circle
     *
     * @param x Center x
     * @param y Center y
     * @param radius
     * @return Graphic
     */
    public static CircleShape createCircleShape(float x, float y, float radius) {
        List<PointD> points = new ArrayList<>();
        points.add(new PointD(x - radius, y));
        points.add(new PointD(x, y -  radius));
        points.add(new PointD(x + radius, y));
        points.add(new PointD(x, y + radius));
        
        CircleShape aPGS = new CircleShape();
        aPGS.setPoints(points);
        
        return aPGS;
    }
}
