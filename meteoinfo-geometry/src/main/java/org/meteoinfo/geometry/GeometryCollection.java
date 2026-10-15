package org.meteoinfo.geometry;

import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Models a collection of {@link Geometry}s of
 * arbitrary type and dimension.
 */
public class GeometryCollection extends Geometry {

    protected Geometry[] geometries;

    public GeometryCollection() {
        geometries = new Geometry[0];
    }

    public GeometryCollection(Geometry[] geometries) {
        if (geometries == null) {
            geometries = new Geometry[]{};
        }
        if (hasNullElements(geometries)) {
            throw new IllegalArgumentException("geometries must not contain null elements");
        }
        this.geometries = geometries;
        updateExtent();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.GEOMETRY_COLLECTION;
    }

    @Override
    public Coordinate getCoordinate() {
        for (int i = 0; i < geometries.length; i++) {
            if (! geometries[i].isEmpty()) {
                return geometries[i].getCoordinate();
            }
        }
        return null;
    }

    @Override
    public List<Coordinate> getCoordinates() {
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < geometries.length; i++) {
            List<Coordinate> childCoordinates = geometries[i].getCoordinates();
            for (int j = 0; j < childCoordinates.size(); j++) {
                coordinates.add(childCoordinates.get(j));
            }
        }
        return coordinates;
    }

    @Override
    public org.locationtech.jts.geom.Geometry toJTSGeometry(GeometryFactory factory) {
        return null;
    }

    public Geometry[] getGeometries() {
        return geometries;
    }

    @Override
    public int getNumGeometries() {
        return geometries.length;
    }

    @Override
    public Geometry getGeometryN(int n) {
        return geometries[n];
    }

    @Override
    public int getNumPoints() {
        int numPoints = 0;
        for (int i = 0; i < geometries.length; i++) {
            numPoints += ((Geometry) geometries[i]).getNumPoints();
        }
        return numPoints;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < geometries.length; i++) {
            if (!geometries[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Geometry reverse() {
        return null;
    }

    /**
     * Update extent
     */
    @Override
    public void updateExtent() {
        for (int i = 0; i < geometries.length; i++) {
            if (i == 0) {
                extent = geometries[i].getExtent();
            } else {
                extent.expandToInclude(geometries[i].getExtent());
            }
        }
    }
}
