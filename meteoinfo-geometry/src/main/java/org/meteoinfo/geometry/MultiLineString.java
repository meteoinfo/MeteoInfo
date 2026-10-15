/* Copyright 2012 Yaqiang Wang,
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.common.MIMath;

/**
 * Poyline shape class
 *
 * @author Yaqiang Wang
 */
public class MultiLineString extends GeometryCollection implements Cloneable {
    // <editor-fold desc="Variables">
    /**
     * Part number
     */
    private int numParts;
    /**
     * Part array
     */
    public int[] parts;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MultiLineString() {
        numParts = 1;
        parts = new int[1];
        parts[0] = 0;
    }

    public MultiLineString(LineString[] lineStrings) {
        super(lineStrings);
        numParts = 1;
        parts = new int[1];
        parts[0] = 0;
    }

    /**
     * Constructor
     * @param points Point list
     */
    public MultiLineString(List<Coordinate> points) {
        this();
        this.setCoordinates(points);
    }

    /**
     * Constructor
     *
     * @param geometry Geometry
     */
    public MultiLineString(org.locationtech.jts.geom.Geometry geometry) {
        this();
        org.locationtech.jts.geom.Coordinate[] cs = geometry.getCoordinates();
        List<Coordinate> pts = new ArrayList();
        for (org.locationtech.jts.geom.Coordinate c : cs) {
            pts.add(new Coordinate(c.x, c.y, c.z));
        }
        switch (geometry.getGeometryType()) {
            case "MultiLineString":
                List<Coordinate> pp;
                int n = geometry.getNumGeometries();
                numParts = n;
                List<Integer> partlist = new ArrayList<>();
                int idx = 0;
                this.geometries = new LineString[n];
                for (int i = 0; i < n; i++) {
                    org.locationtech.jts.geom.LineString poly = (org.locationtech.jts.geom.LineString) geometry.getGeometryN(i);
                    partlist.add(idx);
                    LineString polyline = new LineString();
                    pp = new ArrayList<>();
                    for (int j = idx; j < idx + poly.getNumPoints(); j++) {
                        pp.add(pts.get(j));
                    }
                    polyline.setCoordinates(pp);
                    idx += poly.getNumPoints();
                    this.geometries[i] = polyline;
                }
                parts = new int[n];
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = partlist.get(i);
                }
                this.setExtent(GeometryUtil.getPointsExtent(pts));
                break;
            default:
                this.setCoordinates(pts);
                break;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_LINESTRING;
    }

    /**
     * Get is multi line or not
     * @return Multi line or not
     */
    public boolean isMultiLine() {
        return this.numParts > 1;
    }

    public boolean isClosed() {
        if (isEmpty()) {
            return false;
        }
        for (int i = 0; i < geometries.length; i++) {
            if (!((LineString) geometries[i]).isClosed()) {
                return false;
            }
        }
        return true;
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
        if (this.getNumParts() == 1) {
            org.locationtech.jts.geom.Coordinate[] cs = new org.locationtech.jts.geom.Coordinate[this.getNumPoints()];
            LineString line = (LineString) geometries[0];
            for (int i = 0; i < cs.length; i++) {
                p = line.getCoordinateN(i);
                cs[i] = new org.locationtech.jts.geom.Coordinate(p.x, p.y, p.z);
            }
            return factory.createLineString(cs);
        } else {
            org.locationtech.jts.geom.LineString[] lss = new org.locationtech.jts.geom.LineString[this.getNumGeometries()];
            for (int j = 0; j < lss.length; j++) {
                LineString line = (LineString) this.geometries[j];
                org.locationtech.jts.geom.Coordinate[] cs = new org.locationtech.jts.geom.Coordinate[line.getCoordinates().size()];
                for (int i = 0; i < cs.length; i++) {
                    p = line.getCoordinateN(i);
                    cs[i] = new org.locationtech.jts.geom.Coordinate(p.x, p.y, p.z);
                }
                lss[j] = factory.createLineString(cs);
            }
            org.locationtech.jts.geom.MultiLineString mls = factory.createMultiLineString(lss);
            return mls;
        }
    }

    /**
     * Set points
     *
     * @param coordinates point list
     */
    public void setCoordinates(List<Coordinate> coordinates) {
        updatePolyLines(coordinates);
        this.updateExtent();
    }

    /**
     * Get part number
     *
     * @return Part number
     */
    public int getNumParts() {
        return this.numParts;
    }

    /**
     * Set part number
     *
     * @param value Part number
     */
    public void setPartNum(int value) {
        this.numParts = value;
    }

    /**
     * Get polylines
     *
     * @return polyline list
     */
    public List<LineString> getPolylines() {
        List<LineString> ls = new ArrayList<>();
        for (Geometry geom : this.geometries) {
            ls.add((LineString) geom);
        }
        return ls;
    }

    public void setPolylines(List<LineString> polylines) {
        if (!polylines.isEmpty()){
            geometries = polylines.toArray(new LineString[0]);
            updatePartsPoints();
        }
    }

    /**
     * Get length
     *
     * @return length
     */
    public double getLength() {
        double length = 0.0;
        double dx, dy;
        for (LineString aPL : (LineString[]) geometries) {
            for (int i = 0; i < aPL.getCoordinates().size() - 1; i++) {
                dx = aPL.getCoordinates().get(i + 1).x - aPL.getCoordinates().get(i).x;
                dy = aPL.getCoordinates().get(i + 1).y - aPL.getCoordinates().get(i).y;
                length += Math.sqrt(dx * dx + dy * dy);
            }
        }

        return length;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    private void updatePolyLines(List<Coordinate> coordinates) {
        List<LineString> polylines = new ArrayList<>();
        if (numParts == 1) {
            LineString aPolyLine = new LineString();
            aPolyLine.setCoordinates(coordinates);
            polylines.add(aPolyLine);
        } else {
            Coordinate[] Pointps;
            LineString aPolyLine;
            int numPoints = this.getNumPoints();
            for (int p = 0; p < numParts; p++) {
                if (p == numParts - 1) {
                    Pointps = new Coordinate[numPoints - parts[p]];
                    for (int pp = parts[p]; pp < numPoints; pp++) {
                        Pointps[pp - parts[p]] = coordinates.get(pp);
                    }
                } else {
                    Pointps = new Coordinate[parts[p + 1] - parts[p]];
                    for (int pp = parts[p]; pp < parts[p + 1]; pp++) {
                        Pointps[pp - parts[p]] = coordinates.get(pp);
                    }
                }

                aPolyLine = new LineString();
                aPolyLine.setCoordinates(Arrays.asList(Pointps));
                polylines.add(aPolyLine);
            }
        }

        geometries = polylines.toArray(new LineString[0]);
    }

    private void updatePartsPoints() {
        numParts = 0;
        List<Coordinate> points = new ArrayList<>();
        List<Integer> partList = new ArrayList<>();
        for (int i = 0; i < geometries.length; i++) {
            numParts += 1;
            partList.add(points.size());
            points.addAll(geometries[i].getCoordinates());
        }

        parts = new int[partList.size()];
        for (int i = 0; i < partList.size(); i++) {
            parts[i] = partList.get(i);
        }
        this.setExtent(GeometryUtil.getPointsExtent(points));
    }

    /**
     * Get part index
     *
     * @param vIdx The vertice index
     * @return Part index
     */
    public int getPartIndex(int vIdx) {
        if (numParts == 1) {
            return 0;
        } else {
            for (int p = 1; p < numParts; p++) {
                if (vIdx < parts[p]) {
                    return p - 1;
                }
            }
            return numParts - 1;
        }
    }

    /**
     * Add a vertice
     *
     * @param vIdx Vertice index
     * @param vertice The vertice
     */
    public void addVertice(int vIdx, Coordinate vertice) {
        int partIdx = getPartIndex(vIdx);
        if (partIdx < numParts - 1) {
            parts[partIdx + 1] += 1;
        }

        List<Coordinate> coordinates = this.getCoordinates();
        coordinates.add(vIdx, vertice);
        updatePolyLines(coordinates);
        this.updateExtent();
    }

    /**
     * Remove a vertice
     *
     * @param vIdx Vertice index
     */
    public void removeVertice(int vIdx) {
        int partIdx = getPartIndex(vIdx);
        if (partIdx < numParts - 1) {
            parts[partIdx + 1] -= 1;
        }

        List<Coordinate> coordinates = this.getCoordinates();
        coordinates.remove(vIdx);
        updatePolyLines(coordinates);
        this.updateExtent();
    }

    /**
     * Get Z Array
     *
     * @return Z array
     */
    public double[] getZArray() {
        double[] zArray = new double[this.getCoordinates().size()];
        for (int i = 0; i < this.getCoordinates().size(); i++) {
            zArray[i] = (this.getCoordinates().get(i)).z;
        }

        return zArray;
    }

    /**
     * Get Z Array
     *
     * @return Z value array
     */
    public double[] getMArray() {
        double[] mArray = new double[this.getCoordinates().size()];
        for (int i = 0; i < this.getCoordinates().size(); i++) {
            mArray[i] = (this.getCoordinates().get(i)).m;
        }

        return mArray;
    }

    /**
     * Get Z range - min, max
     *
     * @return Z min, max
     */
    public double[] getZRange() {
        return MIMath.arrayMinMax(getZArray());
    }

    /**
     * Get M range - min, max
     *
     * @return M min, max
     */
    public double[] getMRange() {
        return MIMath.arrayMinMax(getMArray());
    }

    /**
     * Clone
     *
     * @return PolylineShape
     */
    @Override
    public Object clone() {
        MultiLineString aPLS = new MultiLineString();
        aPLS.setValue(this.getValue());
        aPLS.setExtent(this.getExtent());
        aPLS.numParts = numParts;
        aPLS.parts = (int[]) parts.clone();
        LineString[] ls = new LineString[getNumGeometries()];
        for (int i = 0; i < getNumGeometries(); i++) {
            ls[i] = (LineString) geometries[i].clone();
        }
        aPLS.geometries = ls;
        aPLS.setVisible(this.isVisible());
        aPLS.setSelected(this.isSelected());
        aPLS.setLegendIndex(this.getLegendIndex());

        return aPLS;
    }

    /**
     * Value clone
     *
     * @return PolylineShape
     */
    public Object valueClone() {
        MultiLineString aPLS = new MultiLineString();
        aPLS.setValue(this.getValue());
        aPLS.setVisible(this.isVisible());
        aPLS.setSelected(this.isSelected());
        aPLS.setLegendIndex(this.getLegendIndex());

        return aPLS;
    }

    /**
     * Clone value
     *
     * @param other Other polyline shape
     */
    @Override
    public void cloneValue(Geometry other) {
        MultiLineString o = (MultiLineString) other;
        this.setValue(o.getValue());
        this.setExtent(o.getExtent());
        this.numParts = o.numParts;
        this.parts = (int[]) o.parts.clone();
        LineString[] ls = new LineString[getNumGeometries()];
        for (int i = 0; i < getNumGeometries(); i++) {
            ls[i] = (LineString) geometries[i].clone();
        }
        this.geometries = ls;
        this.setVisible(o.isVisible());
        this.setSelected(o.isSelected());
        this.setLegendIndex(o.getLegendIndex());
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

    // </editor-fold>
}
