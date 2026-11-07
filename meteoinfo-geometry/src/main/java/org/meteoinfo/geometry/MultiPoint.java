package org.meteoinfo.geometry;

public class MultiPoint extends GeometryCollection {

    public MultiPoint() {

    }

    public MultiPoint(Point[] shapes) {
        super(shapes);
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_POINT;
    }

    @Override
    public GeometryCollection clone() {
        Point[] points = new Point[this.geometries.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = (Point) this.geometries[i].clone();
        }
        return new MultiPoint(points);
    }
}
