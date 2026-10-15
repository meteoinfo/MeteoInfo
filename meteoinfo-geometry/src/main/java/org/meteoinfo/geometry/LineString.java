/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.geometry;

import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Polyline class
 * 
 * @author Yaqiang Wang
 */
public class LineString extends Geometry implements Cloneable {

    protected List<Coordinate> coordinates;

    /**
     * Constructor
     */
    public LineString() {
        extent = new Extent();
        coordinates = new ArrayList<>();
    }

    /**
     * Constructor
     * @param coordinates Coordinates
     */
    public LineString(List<Coordinate> coordinates) {
        this.setCoordinates(coordinates);
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.LINESTRING;
    }

    @Override
    public Coordinate getCoordinate() {
        return this.coordinates.get(0);
    }

    @Override
    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }

    @Override
    public void updateExtent() {
        this.extent = GeometryUtil.getPointsExtent(this.coordinates);
    }

    @Override
    public int getNumPoints() {
        return this.getCoordinates().size();
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
        this.updateExtent();
    }

    @Override
    public boolean isEmpty() {
        return coordinates.isEmpty();
    }

    /**
     * To geometry method
     *
     * @param factory GeometryFactory
     * @return Geometry
     */
    @Override
    public org.locationtech.jts.geom.Geometry toJTSGeometry(GeometryFactory factory) {
        Coordinate p;
        org.locationtech.jts.geom.Coordinate[] cs = new org.locationtech.jts.geom.Coordinate[this.getNumPoints()];
        for (int i = 0; i < cs.length; i++) {
            p = this.coordinates.get(i);
            cs[i] = new org.locationtech.jts.geom.Coordinate(p.x, p.y, p.z);
        }
        return factory.createLineString(cs);
    }

    /**
     * Set points
     * 
     * @param points point array
     */
    public void setPoints(PointF[] points) {
        List<Coordinate> pointList = new ArrayList<>();
        for (PointF aP : points) {
            pointList.add(new Coordinate(aP.X, aP.Y));
        }
        coordinates = pointList;

        extent = GeometryUtil.getPointsExtent(coordinates);
    }

    /**
     * Get n-th coordinate
     * @param n N-th
     * @return Coordinate
     */
    public Coordinate getCoordinateN(int n) {
        return coordinates.get(n);
    }

    /**
     * Determine if the polyline is closed
     * 
     * @return boolean
     */
    public boolean isClosed() {
        if (isEmpty()) {
            return false;
        }
        return getCoordinateN(0).equals2D(getCoordinateN(getNumPoints() - 1));
    }

    /**
     * Get length
     * 
     * @return length
     */
    public double getLength() {
        double length = 0.0;
        double dx, dy;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            dx = coordinates.get(i + 1).x - coordinates.get(i).x;
            dy = coordinates.get(i + 1).y - coordinates.get(i).y;
            length += Math.sqrt(dx * dx + dy * dy);
        }

        return length;
    }

    /**
     * Get lengths of each segment
     * 
     * @return length array
     */
    public double[] getLengths() {
        double[] lengths = new double[coordinates.size() - 1];
        double dx, dy;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            dx = coordinates.get(i + 1).x - coordinates.get(i).x;
            dy = coordinates.get(i + 1).y - coordinates.get(i).y;
            lengths[i] = Math.sqrt(dx * dx + dy * dy);
        }

        return lengths;
    }

    /**
     * Get position list: x, y, angle
     * @param aLen segment length
     * @return  position list
     */
    public List<double[]> getPositions(double aLen) {
        double length = getLength();
        int n = (int) (length / aLen);
        if (n <= 0) {
            return null;
        }

        List<double[]> pos = new ArrayList<>();
        double x, y, angle;
        double[] lengths = getLengths();
        int idx = 0;
        double sLen = lengths[0];
        for (int i = 0; i < n; i++) {
            double len = aLen * (i + 1);
            for (int j = idx; j < lengths.length; j++) {
                if (sLen > len) {
                    idx = j;
                    break;
                }
                sLen += lengths[j + 1];
            }

            Coordinate aPoint = coordinates.get(idx);
            Coordinate bPoint = coordinates.get(idx + 1);
            x = aPoint.x + (bPoint.x - aPoint.x) * (lengths[idx] - (sLen - len)) / (lengths[idx]);
            y = aPoint.y + (bPoint.y - aPoint.y) * (lengths[idx] - (sLen - len)) / (lengths[idx]);
            double U = bPoint.x - aPoint.x;
            double V = bPoint.y - aPoint.y;
            angle = Math.atan((V) / (U)) * 180 / Math.PI;
            if (U < 0) {
                angle += 180;
            }

            double[] data = new double[]{x, y, angle};
            pos.add(data);
        }

        return pos;
    }

    /**
     * Vertice edited update
     *
     * @param vIdx Vertice index
     * @param newX New X
     * @param newY New Y
     */
    public void moveVertice(int vIdx, double newX, double newY) {
        List<Coordinate> points = getCoordinates();
        if (this.getGeometryType().isPolygon()) {
            int last = points.size() - 1;
            if (vIdx == 0) {
                if (points.get(0).x == points.get(last).x && points.get(0).y == points.get(last).y) {
                    points.get(last).x = newX;
                    points.get(last).y = newY;
                }
            } else if (vIdx == last) {
                if (points.get(0).x == points.get(last).x && points.get(0).y == points.get(last).y) {
                    points.get(0).x = newX;
                    points.get(0).y = newY;
                }
            }
        }

        Coordinate aP = points.get(vIdx);
        aP.x = newX;
        aP.y = newY;
        setCoordinates(points);
    }

    /**
     * Move the shape
     * @param xShift X shift
     * @param yShift Y shift
     */
    public void move(double xShift, double yShift){
        List<Coordinate> points = this.getCoordinates();
        for (Coordinate aPoint : points) {
            aPoint.x += xShift;
            aPoint.y += yShift;
        }

        this.setCoordinates(points);
    }

    /**
     * Reverse points direction
     */
    public LineString reverse() {
        Collections.reverse(coordinates);
        return new LineString(coordinates);
    }
}
