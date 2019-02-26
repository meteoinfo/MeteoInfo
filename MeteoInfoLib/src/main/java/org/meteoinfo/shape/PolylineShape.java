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
package org.meteoinfo.shape;

import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.meteoinfo.jts.geom.Coordinate;
import org.meteoinfo.jts.geom.Geometry;
import org.meteoinfo.jts.geom.GeometryFactory;
import org.meteoinfo.jts.geom.LineString;
import org.meteoinfo.jts.geom.MultiLineString;

/**
 * Poyline shape class
 *
 * @author Yaqiang Wang
 */
public class PolylineShape extends Shape implements Cloneable {
    // <editor-fold desc="Variables">

    private List<? extends PointD> _points;
    private List<? extends Polyline> _polylines;
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
        _points = new ArrayList<>();
        _numParts = 1;
        parts = new int[1];
        parts[0] = 0;
        _polylines = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param geometry Geometry
     */
    public PolylineShape(Geometry geometry) {
        this();
        Coordinate[] cs = geometry.getCoordinates();
        List<PointD> points = new ArrayList();
        for (Coordinate c : cs) {
            points.add(new PointD(c.x, c.y));
        }
        switch (geometry.getGeometryType()) {
            case "MultiLineString":
                this._points = points;
                List<PointD> pp;
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
                    ((List<Polyline>) this._polylines).add(polyline);
                }
                parts = new int[n];
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = partlist.get(i);
                }
                this.setExtent(MIMath.getPointsExtent(_points));
                break;
            default:
                this.setPoints(points);
                break;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    @Override
    public ShapeTypes getShapeType() {
        return ShapeTypes.Polyline;
    }

    /**
     * To geometry method
     *
     * @param factory GeometryFactory
     * @return Geometry
     */
    @Override
    public Geometry toGeometry(GeometryFactory factory) {
        PointD p;
        if (this.getPartNum() == 1) {
            Coordinate[] cs = new Coordinate[this.getPointNum()];
            for (int i = 0; i < cs.length; i++) {
                p = this._points.get(i);
                cs[i] = new Coordinate(p.X, p.Y);
            }
            return factory.createLineString(cs);
        } else {
            LineString[] lss = new LineString[this._polylines.size()];
            for (int j = 0; j < lss.length; j++) {
                Polyline line = this._polylines.get(j);
                Coordinate[] cs = new Coordinate[line.getPointList().size()];
                for (int i = 0; i < cs.length; i++) {
                    p = line.getPointList().get(i);
                    cs[i] = new Coordinate(p.X, p.Y);
                }
                lss[j] = factory.createLineString(cs);
            }
            MultiLineString mls = factory.createMultiLineString(lss);
            return mls;
        }
    }

    ;

    /**
     * Get points
     *
     * @return point list
     */
    @Override
    public List<? extends PointD> getPoints() {
        return _points;
    }

    /**
     * Set points
     *
     * @param points point list
     */
    @Override
    public void setPoints(List<? extends PointD> points) {
        _points = points;
        this.setExtent(MIMath.getPointsExtent(_points));
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
        return this._points.size();
    }

    /**
     * Get polylines
     *
     * @return polyline list
     */
    public List<? extends Polyline> getPolylines() {
        return _polylines;
    }

    public void setPolylines(List<? extends Polyline> polylines) {
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
                dx = aPL.getPointList().get(i + 1).X - aPL.getPointList().get(i).X;
                dy = aPL.getPointList().get(i + 1).Y - aPL.getPointList().get(i).Y;
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
            aPolyLine.setPointList(_points);
            polylines.add(aPolyLine);
        } else {
            PointD[] Pointps;
            Polyline aPolyLine;
            int numPoints = this.getPointNum();
            for (int p = 0; p < _numParts; p++) {
                if (p == _numParts - 1) {
                    Pointps = new PointD[numPoints - parts[p]];
                    for (int pp = parts[p]; pp < numPoints; pp++) {
                        Pointps[pp - parts[p]] = _points.get(pp);
                    }
                } else {
                    Pointps = new PointD[parts[p + 1] - parts[p]];
                    for (int pp = parts[p]; pp < parts[p + 1]; pp++) {
                        Pointps[pp - parts[p]] = _points.get(pp);
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
        List<PointD> points = new ArrayList<>();
        List<Integer> partList = new ArrayList<>();
        for (int i = 0; i < _polylines.size(); i++) {
            _numParts += 1;
            partList.add(points.size());
            points.addAll(_polylines.get(i).getPointList());
        }
        _points = points;
        parts = new int[partList.size()];
        for (int i = 0; i < partList.size(); i++) {
            parts[i] = partList.get(i);
        }
        this.setExtent(MIMath.getPointsExtent(_points));
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
    public void addVertice(int vIdx, PointD vertice) {
        int partIdx = getPartIndex(vIdx);
        if (partIdx < _numParts - 1) {
            parts[partIdx + 1] += 1;
        }

        ((List<PointD>) _points).add(vIdx, vertice);
        this.setExtent(MIMath.getPointsExtent(_points));
        updatePolyLines();
    }

    /**
     * Remove a vertice
     *
     * @param vIdx Vertice index
     */
    @Override
    public void removeVerice(int vIdx) {
        int partIdx = getPartIndex(vIdx);
        if (partIdx < _numParts - 1) {
            parts[partIdx + 1] -= 1;
        }

        ((List<PointD>) _points).remove(vIdx);
        this.setExtent(MIMath.getPointsExtent(_points));
        updatePolyLines();
    }

    /**
     * Reverse points direction
     */
    @Override
    public void reverse() {
        Collections.reverse(_points);
    }

    //@Override
    public Object clone_back() {
        PolylineShape o = (PolylineShape) super.clone();
        List<PointD> points = new ArrayList<>();
        for (PointD point : (List<PointD>) _points) {
            points.add((PointD) point.clone());
        }
        o.setPoints(points);

        return o;
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
        List<PointD> points = new ArrayList<>();
        for (PointD point : (List<PointD>) _points) {
            points.add((PointD) point.clone());
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
        List<PointD> points = new ArrayList<>();
        for (PointD point : (List<PointD>) o._points) {
            points.add((PointD) point.clone());
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
        return MIMath.doubleEquals(_points.get(0).X, _points.get(_points.size() - 1).X)
                && MIMath.doubleEquals(_points.get(0).Y, _points.get(_points.size() - 1).Y);
    }

    // </editor-fold>
}
