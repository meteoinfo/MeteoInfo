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
package org.meteoinfo.geometry.shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.GeometryUtil;

/**
 * Poyline shape class
 *
 * @author Yaqiang Wang
 */
public class PolylineShape extends Shape implements Cloneable {
    // <editor-fold desc="Variables">

    private List<Polyline> _polylines;
    /**
     * Part number
     */
    private int _numParts;
    /**
     * Part array
     */
    public int[] parts;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PolylineShape() {
        points = new ArrayList<>();
        _numParts = 1;
        parts = new int[1];
        parts[0] = 0;
        _polylines = new ArrayList<>();
    }

    /**
     * Constructor
     * @param points Point list
     */
    public PolylineShape(List<Coordinate> points) {
        this();
        this.setPoints(points);
    }

    /**
     * Constructor
     *
     * @param geometry Geometry
     */
    public PolylineShape(Geometry geometry) {
        this();
        org.locationtech.jts.geom.Coordinate[] cs = geometry.getCoordinates();
        List<Coordinate> pts = new ArrayList();
        for (org.locationtech.jts.geom.Coordinate c : cs) {
            pts.add(new Coordinate(c.x, c.y, c.z));
        }
        switch (geometry.getGeometryType()) {
            case "MultiLineString":
                this.points = pts;
                List<Coordinate> pp;
                int n = geometry.getNumGeometries();
                _numParts = n;
                List<Integer> partlist = new ArrayList<>();
                int idx = 0;
                for (int i = 0; i < n; i++) {
                    LineString poly = (LineString) geometry.getGeometryN(i);
                    partlist.add(idx);
                    Polyline polyline = new Polyline();
                    pp = new ArrayList<>();
                    for (int j = idx; j < idx + poly.getNumPoints(); j++) {
                        pp.add(points.get(j));
                    }
                    polyline.setPointList(pp);
                    idx += poly.getNumPoints();
                    this._polylines.add(polyline);
                }
                parts = new int[n];
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = partlist.get(i);
                }
                this.setExtent(GeometryUtil.getPointsExtent(pts));
                break;
            default:
                this.setPoints(pts);
                break;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    @Override
    public ShapeTypes getShapeType() {
        return ShapeTypes.POLYLINE;
    }

    /**
     * Get is multi line or not
     * @return Multi line or not
     */
    public boolean isMultiLine() {
        return this._numParts > 1;
    }

    /**
     * To geometry method
     *
     * @param factory GeometryFactory
     * @return Geometry
     */
    @Override
    public Geometry toGeometry(GeometryFactory factory) {
        Coordinate p;
        if (this.getPartNum() == 1) {
            org.locationtech.jts.geom.Coordinate[] cs = new org.locationtech.jts.geom.Coordinate[this.getPointNum()];
            for (int i = 0; i < cs.length; i++) {
                p = this.points.get(i);
                cs[i] = new org.locationtech.jts.geom.Coordinate(p.x, p.y, p.z);
            }
            return factory.createLineString(cs);
        } else {
            LineString[] lss = new LineString[this._polylines.size()];
            for (int j = 0; j < lss.length; j++) {
                Polyline line = this._polylines.get(j);
                org.locationtech.jts.geom.Coordinate[] cs = new org.locationtech.jts.geom.Coordinate[line.getPointList().size()];
                for (int i = 0; i < cs.length; i++) {
                    p = line.getPointList().get(i);
                    cs[i] = new org.locationtech.jts.geom.Coordinate(p.x, p.y, p.z);
                }
                lss[j] = factory.createLineString(cs);
            }
            MultiLineString mls = factory.createMultiLineString(lss);
            return mls;
        }
    }

    /**
     * Set points
     *
     * @param points point list
     */
    @Override
    public void setPoints(List<Coordinate> points) {
        this.points = points;
        this.updateExtent();
        updatePolyLines();
    }

    /**
     * Get part number
     *
     * @return Part number
     */
    public int getPartNum() {
        return this._numParts;
    }

    /**
     * Set part number
     *
     * @param value Part number
     */
    public void setPartNum(int value) {
        this._numParts = value;
    }

    /**
     * Get point number
     *
     * @return Point number
     */
    public int getPointNum() {
        return this.points.size();
    }

    /**
     * Get polylines
     *
     * @return polyline list
     */
    public List<Polyline> getPolylines() {
        return _polylines;
    }

    public void setPolylines(List<Polyline> polylines) {
        if (!polylines.isEmpty()){
            _polylines = polylines;
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
        for (Polyline aPL : _polylines) {
            for (int i = 0; i < aPL.getPointList().size() - 1; i++) {
                dx = aPL.getPointList().get(i + 1).x - aPL.getPointList().get(i).x;
                dy = aPL.getPointList().get(i + 1).y - aPL.getPointList().get(i).y;
                length += Math.sqrt(dx * dx + dy * dy);
            }
        }

        return length;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    private void updatePolyLines() {
        List<Polyline> polylines = new ArrayList<>();
        if (_numParts == 1) {
            Polyline aPolyLine = new Polyline();
            aPolyLine.setPointList(points);
            polylines.add(aPolyLine);
        } else {
            Coordinate[] Pointps;
            Polyline aPolyLine;
            int numPoints = this.getPointNum();
            for (int p = 0; p < _numParts; p++) {
                if (p == _numParts - 1) {
                    Pointps = new Coordinate[numPoints - parts[p]];
                    for (int pp = parts[p]; pp < numPoints; pp++) {
                        Pointps[pp - parts[p]] = points.get(pp);
                    }
                } else {
                    Pointps = new Coordinate[parts[p + 1] - parts[p]];
                    for (int pp = parts[p]; pp < parts[p + 1]; pp++) {
                        Pointps[pp - parts[p]] = points.get(pp);
                    }
                }

                aPolyLine = new Polyline();
                aPolyLine.setPointList(Arrays.asList(Pointps));
                polylines.add(aPolyLine);
            }
        }

        _polylines = polylines;
    }

    private void updatePartsPoints() {
        _numParts = 0;
        List<Coordinate> points = new ArrayList<>();
        List<Integer> partList = new ArrayList<>();
        for (int i = 0; i < _polylines.size(); i++) {
            _numParts += 1;
            partList.add(points.size());
            points.addAll(_polylines.get(i).getPointList());
        }
        this.points = points;
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
        if (_numParts == 1) {
            return 0;
        } else {
            for (int p = 1; p < _numParts; p++) {
                if (vIdx < parts[p]) {
                    return p - 1;
                }
            }
            return _numParts - 1;
        }
    }

    /**
     * Add a vertice
     *
     * @param vIdx Vertice index
     * @param vertice The vertice
     */
    @Override
    public void addVertice(int vIdx, Coordinate vertice) {
        int partIdx = getPartIndex(vIdx);
        if (partIdx < _numParts - 1) {
            parts[partIdx + 1] += 1;
        }

       points.add(vIdx, vertice);
        this.updateExtent();
        updatePolyLines();
    }

    /**
     * Remove a vertice
     *
     * @param vIdx Vertice index
     */
    @Override
    public void removeVertice(int vIdx) {
        int partIdx = getPartIndex(vIdx);
        if (partIdx < _numParts - 1) {
            parts[partIdx + 1] -= 1;
        }

        points.remove(vIdx);
        this.updateExtent();
        updatePolyLines();
    }

    /**
     * Reverse points direction
     */
    @Override
    public void reverse() {
        Collections.reverse(points);
    }

    /**
     * Get Z Array
     *
     * @return Z array
     */
    public double[] getZArray() {
        double[] zArray = new double[this.getPoints().size()];
        for (int i = 0; i < this.getPoints().size(); i++) {
            zArray[i] = (this.getPoints().get(i)).z;
        }

        return zArray;
    }

    /**
     * Get Z Array
     *
     * @return Z value array
     */
    public double[] getMArray() {
        double[] mArray = new double[this.getPoints().size()];
        for (int i = 0; i < this.getPoints().size(); i++) {
            mArray[i] = (this.getPoints().get(i)).m;
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
        PolylineShape aPLS = new PolylineShape();
        aPLS.setValue(this.getValue());
        aPLS.setExtent(this.getExtent());
        aPLS._numParts = _numParts;
        aPLS.parts = (int[]) parts.clone();
        List<Coordinate> points = new ArrayList<>();
        for (Coordinate point : this.points) {
            points.add((Coordinate) point.clone());
        }
        aPLS.setPoints(points);
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
        PolylineShape aPLS = new PolylineShape();
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
    public void cloneValue(Shape other) {
        PolylineShape o = (PolylineShape) other;
        this.setValue(o.getValue());
        this.setExtent(o.getExtent());
        this._numParts = o._numParts;
        this.parts = (int[]) o.parts.clone();
        List<Coordinate> points = new ArrayList<>();
        for (Coordinate point : o.points) {
            points.add((Coordinate) point.clone());
        }
        this.setPoints(points);
        this.setVisible(o.isVisible());
        this.setSelected(o.isSelected());
        this.setLegendIndex(o.getLegendIndex());
    }

    /**
     * Check if the polyline shape is closed or not
     * @return Boolean
     */
    public boolean isClosed() {
        return MIMath.doubleEquals(points.get(0).x, points.get(points.size() - 1).x)
                && MIMath.doubleEquals(points.get(0).y, points.get(points.size() - 1).y);
    }

    // </editor-fold>
}
