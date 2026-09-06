package org.meteoinfo.geometry.shape;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShapeCollection extends Shape {

    protected Shape[] shapes;

    public ShapeCollection(Shape[] shapes) {
        this.shapes = shapes;
    }

    @Override
    public ShapeTypes getShapeType() {
        return ShapeTypes.SHAPE_COLLECTION;
    }

    @Override
    public Geometry toGeometry(GeometryFactory factory) {
        return null;
    }

    public Shape[] getShapes() {
        return shapes;
    }

    public int getNumShapes() {
        return shapes.length;
    }

    public Shape getShapeN(int n) {
        return shapes[n];
    }
}
