package org.meteoinfo.geometry;

import java.util.ArrayList;
import java.util.List;

public class GeometryFactory {

    /**
     * Create a Coordinate using the give JTS Coordinate.
     * @param coordinate JTS Coordinate
     * @return the created Coordinate
     */
    public static Coordinate createCoordinate(org.locationtech.jts.geom.Coordinate coordinate) {
        return new Coordinate(coordinate.x, coordinate.y, coordinate.z, coordinate.getM());
    }

    /**
     * Create a Coordinate list using the give JTS Coordinate array.
     * @param coordinate JTS Coordinate array
     * @return the created Coordinate list
     */
    public static List<Coordinate> createCoordinateList(org.locationtech.jts.geom.Coordinate[] coordinates) {
        List<Coordinate> list = new ArrayList();
        for (org.locationtech.jts.geom.Coordinate c : coordinates) {
            list.add(new Coordinate(c.x, c.y, c.z, c.getM()));
        }

        return list;
    }

    /**
     * Creates a Point using the given Coordinate.
     * A null Coordinate creates an empty Geometry.
     *
     * @param coordinate a Coordinate, or null
     * @return the created Point
     */
    public static Point createPoint(Coordinate coordinate) {
        return new Point(coordinate);
    }

    /**
     * Creates a Point using the given JtS Point.
     *
     * @param point a JTS Point
     * @return the created Point
     */
    public static Point createPoint(org.locationtech.jts.geom.Point point) {
        org.locationtech.jts.geom.Coordinate c = point.getCoordinate();
        return new Point(new Coordinate(c.x, c.y, c.getZ()));
    }

    /**
     * Creates a {@link MultiPoint} using the given {@link Point}s.
     * A null or empty array will create an empty MultiPoint.
     *
     * @param point an array of Points (without null elements), or an empty array, or <code>null</code>
     * @return a MultiPoint object
     */
    public static MultiPoint createMultiPoint(Point[] points) {
        return new MultiPoint(points);
    }

    /**
     * Creates a {@link MultiPoint} using the given {@link Point}s.
     * A null or empty array will create an empty MultiPoint.
     *
     * @param multiPoint an JTS MultiPoint
     * @return a MultiPoint object
     */
    public static MultiPoint createMultiPoint(org.locationtech.jts.geom.MultiPoint multiPoint) {
        Point[] points = new Point[multiPoint.getNumGeometries()];
        for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
            points[i] = createPoint((org.locationtech.jts.geom.Point) multiPoint.getGeometryN(i));
        }

        return new MultiPoint(points);
    }

    /**
     * Creates a LineString using the given Coordinates.
     * A null or empty array creates an empty LineString.
     *
     * @param coordinates an list without null elements, or an empty array, or null
     * @return a LineString object
     */
    public static LineString createLineString(List<Coordinate> coordinates) {
        return new LineString(coordinates);
    }

    /**
     * Creates a LineString using the given JTS LineString.
     * A null or empty array creates an empty LineString.
     *
     * @param lineString an JTS LineString
     * @return a LineString object
     */
    public static LineString createLineString(org.locationtech.jts.geom.LineString lineString) {
        return new LineString(createCoordinateList(lineString.getCoordinates()));
    }

    /**
     * Creates a LinearRing using the given JTS LinearRing.
     *
     * @param linearRing an JTS LinearRing
     * @return a LinearRing object
     */
    public static LinearRing createLinearRing(org.locationtech.jts.geom.LinearRing linearRing) {
        return new LinearRing(createCoordinateList(linearRing.getCoordinates()));
    }

    /**
     * Creates a {@link MultiLineString} using the given {@link LineString}s.
     * A null or empty array will create an empty MultiPoint.
     *
     * @param lineStrings an array of LineString
     * @return a MultiLineString object
     */
    public static MultiLineString createMultiLineString(LineString[] lineStrings) {
        return new MultiLineString(lineStrings);
    }

    /**
     * Creates a {@link MultiLineString} using the given JTS MultiLineString.
     *
     * @param multiLineString an JTS MultiLineString
     * @return a MultiLineString object
     */
    public static MultiLineString createMultiLineString(org.locationtech.jts.geom.MultiLineString multiLineString) {
        LineString[] lineStrings = new LineString[multiLineString.getNumGeometries()];
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            lineStrings[i] = createLineString((org.locationtech.jts.geom.LineString) multiLineString.getGeometryN(i));
        }

        return new MultiLineString(lineStrings);
    }

    /**
     * Creates a Polygon using the given LinearRings..
     *
     * @param shell an shell LinearRing
     * @param holes an holes LinearRing list
     * @return a Polygon object
     */
    public static Polygon createPolygon(LinearRing shell, List<LinearRing> holes) {
        return new Polygon(shell, holes);
    }

    /**
     * Creates a Polygon using the given LinearRings..
     *
     * @param shell an shell LinearRing
     * @return a Polygon object
     */
    public static Polygon createPolygon(LinearRing shell) {
        return createPolygon(shell, new ArrayList<LinearRing>());
    }

    /**
     * Creates a Polygon using the given JTS Polygon.
     *
     * @param polygon an JTS Polygon
     * @return a Polygon object
     */
    public static Polygon createPolygon(org.locationtech.jts.geom.Polygon polygon) {
        LinearRing shell = createLinearRing(polygon.getExteriorRing());
        if (polygon.getNumInteriorRing() == 0)
            return createPolygon(shell);
        else {
            List<LinearRing> holes = new ArrayList();
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                holes.add(createLinearRing(polygon.getInteriorRingN(i)));
            }
            return createPolygon(shell, holes);
        }
    }

    /**
     * Creates a {@link MultiPolygon} using the given {@link Polygon}s.
     *
     * @param polygons an array of Polygon
     * @return a MultiPolygon object
     */
    public static MultiPolygon createMultiPolygon(Polygon[] polygons) {
        return new MultiPolygon(polygons);
    }

    /**
     * Creates a {@link MultiPolygon} using the given JTS MultiPolygon
     *
     * @param multiPolygon a JTS MultiPolygon
     * @return a MultiPolygon object
     */
    public static MultiPolygon createMultiPolygon(org.locationtech.jts.geom.MultiPolygon multiPolygon) {
        Polygon[] polygons = new Polygon[multiPolygon.getNumGeometries()];
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            polygons[i] = createPolygon((org.locationtech.jts.geom.Polygon) multiPolygon.getGeometryN(i));
        }

        return new MultiPolygon(polygons);
    }

    /**
     * Create Geometry from JTS Geometry
     *
     * @param geometry The JTS geometry
     * @return created Geometry
     */
    public static Geometry createGeometry(org.locationtech.jts.geom.Geometry geometry){
        switch (geometry.getGeometryType()){
            case org.locationtech.jts.geom.Geometry.TYPENAME_POINT:
                return createPoint((org.locationtech.jts.geom.Point) geometry);
            case org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOINT:
                return createMultiPoint((org.locationtech.jts.geom.MultiPoint) geometry);
            case org.locationtech.jts.geom.Geometry.TYPENAME_LINESTRING:
                return createLineString((org.locationtech.jts.geom.LineString) geometry);
            case org.locationtech.jts.geom.Geometry.TYPENAME_MULTILINESTRING:
                return createMultiLineString((org.locationtech.jts.geom.MultiLineString) geometry);
            case org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON:
                return createPolygon((org.locationtech.jts.geom.Polygon) geometry);
            case org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOLYGON:
                return createMultiPolygon((org.locationtech.jts.geom.MultiPolygon) geometry);
        }
        return null;
    }
}
