package org.meteoinfo.geometry.special;

import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Geometry;
import org.meteoinfo.geometry.GeometryType;
import org.meteoinfo.geometry.GeometryUtil;

import java.util.ArrayList;
import java.util.List;

public class Cylinder extends Geometry {

    private List<Coordinate> points;
    double radius = 1.;

    /**
     * Constructor
     */
    public Cylinder() {
        super();
        this.points = new ArrayList<>();
        this.points.add(new Coordinate());
        this.points.add(new Coordinate());
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    /**
     * Constructor
     * @param points Points
     * @param radius Radius
     */
    public Cylinder(List<Coordinate> points, double radius) {
        super();
        this.points = points;
        this.radius = radius;
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    /**
     * Constructor
     * @param p1 Point 1
     * @param p2 Point 2
     * @param radius Radius
     */
    public Cylinder(Coordinate p1, Coordinate p2, double radius) {
        super();
        this.points = new ArrayList<>();
        this.points.add(p1);
        this.points.add(p2);
        this.radius = radius;
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.CYLINDER;
    }

    @Override
    public Coordinate getCoordinate() {
        return points.get(0);
    }

    @Override
    public org.locationtech.jts.geom.Geometry toJTSGeometry(GeometryFactory factory) {
        return null;
    }

    /**
     * Get vertex points
     * @return Vertex points
     */
    public List<Coordinate> getCoordinates() {
        return this.points;
    }

    @Override
    public void updateExtent() {
        this.extent = GeometryUtil.getPointsExtent(this.points);
    }

    @Override
    public int getNumPoints() {
        return this.points.size();
    }

    /**
     * Get a vertex point
     * @param i The index
     * @return A vertex point
     */
    public Coordinate getPoint(int i) {
        return this.points.get(i);
    }

    /**
     * Set vertex points
     * @param value Vertex points
     */
    public void setCoordinates(List<Coordinate> value) {
        this.points = value;
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Geometry reverse() {
        return null;
    }

    /**
     * Get radius
     * @return Radius
     */
    public double getRadius() {
        return this.radius;
    }

    /**
     * Set radius
     * @param value Radius
     */
    public void setRadius(double value) {
        this.radius = value;
    }

    /**
     * Get diameter
     * @return Diameter
     */
    public double getDiameter() {
        return  this.radius * 2;
    }

    /**
     * Get cylinder height
     * @return Cylinder height
     */
    public double getHeight() {
        return this.points.get(1).z - this.points.get(0).z;
    }
}
