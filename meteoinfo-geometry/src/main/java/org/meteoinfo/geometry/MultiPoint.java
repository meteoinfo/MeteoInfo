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
}
