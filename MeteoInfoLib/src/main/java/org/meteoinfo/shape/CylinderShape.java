package org.meteoinfo.shape;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;

import java.util.ArrayList;
import java.util.List;

public class CylinderShape extends Shape {

    private List<PointZ> points;
    double radius = 1.;

    /**
     * Constructor
     */
    public CylinderShape() {
        super();
        this.points = new ArrayList<>();
        this.points.add(new PointZ());
        this.points.add(new PointZ());
        this.setExtent(MIMath.getPointsExtent(this.points));
    }

    /**
     * Constructor
     * @param points Points
     * @param radius Radius
     */
    public CylinderShape(List<PointZ> points, double radius) {
        super();
        this.points = points;
        this.radius = radius;
        this.setExtent(MIMath.getPointsExtent(this.points));
    }

    /**
     * Constructor
     * @param p1 Point 1
     * @param p2 Point 2
     * @param radius Radius
     */
    public CylinderShape(PointZ p1, PointZ p2, double radius) {
        super();
        this.points = new ArrayList<>();
        this.points.add(p1);
        this.points.add(p2);
        this.radius = radius;
        this.setExtent(MIMath.getPointsExtent(this.points));
    }

    @Override
    public ShapeTypes getShapeType() {
        return ShapeTypes.CYLINDER;
    }

    @Override
    public Geometry toGeometry(GeometryFactory factory) {
        return null;
    }

    /**
     * Get vertex points
     * @return Vertex points
     */
    public List<PointZ> getPoints() {
        return this.points;
    }

    /**
     * Get a vertex point
     * @param i The index
     * @return A vertex point
     */
    public PointZ getPoint(int i) {
        return this.points.get(i);
    }

    /**
     * Set vertex points
     * @param value Vertex points
     */
    public void setPoints(List<? extends PointD> value) {
        this.points = (List<PointZ>)value;
        this.setExtent(MIMath.getPointsExtent(this.points));
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
        return this.points.get(1).Z - this.points.get(0).Z;
    }
}
