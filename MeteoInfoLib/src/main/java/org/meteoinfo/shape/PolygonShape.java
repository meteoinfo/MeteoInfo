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

import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.meteoinfo.global.PointF;
import org.meteoinfo.jts.geom.Coordinate;
import org.meteoinfo.jts.geom.Geometry;
import org.meteoinfo.jts.geom.GeometryFactory;
import org.meteoinfo.jts.geom.MultiPolygon;

/**
 * PolygonShape class
 *
 * @author Yaqiang Wang
 */
public class PolygonShape extends Shape implements Cloneable {
    // <editor-fold desc="Variables">

    protected List<? extends PointD> _points;
    protected List<? extends Polygon> _polygons;
    /**
     * Start value
     */
    public double lowValue;
    /**
     * End value
     */
    public double highValue;
    /**
     * Part number
     */
    protected int _numParts;
    /**
     * Part array
     */
    public int[] parts;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PolygonShape() {
        _points = new ArrayList<>();
        _numParts = 1;
        parts = new int[1];
        parts[0] = 0;
        _polygons = new ArrayList<>();
    }
        
    /**
     * Constructor
     *
     * @param geometry Geometry
     */
    public PolygonShape(Geometry geometry) {
        this();
        Coordinate[] cs = geometry.getCoordinates();
        List<PointD> points = new ArrayList();
        for (Coordinate c : cs) {
            points.add(new PointD(c.x, c.y));
        }
        this._points = points;
        List<PointD> pp;
        switch (geometry.getGeometryType()) {
            case "MultiPolygon":
                int n = geometry.getNumGeometries();                
                _numParts = 0;
                List<Integer> partlist = new ArrayList<>();
                int idx = 0;
                for (int i = 0; i < n; i++) {
                    org.meteoinfo.jts.geom.Polygon poly = (org.meteoinfo.jts.geom.Polygon) geometry.getGeometryN(i);
                    _numParts += poly.getNumInteriorRing() + 1;                    
                    partlist.add(idx);                    
                    Polygon polygon = new Polygon();
                    pp = new ArrayList<>();
                    for (int j = idx; j < idx + poly.getExteriorRing().getNumPoints(); j++) {
                        pp.add(points.get(j));
                    }
                    polygon.setOutLine(pp);
                    idx += poly.getExteriorRing().getNumPoints();
                    for (int j = 0; j < poly.getNumInteriorRing(); j++) {
                        partlist.add(idx);
                        pp = new ArrayList<>();
                        for (int k = idx; k < idx + poly.getInteriorRingN(j).getNumPoints(); k++) {
                            pp.add(points.get(k));
                        }
                        polygon.addHole(pp);
                        idx += poly.getInteriorRingN(j).getNumPoints();
                    }
                    ((List<Polygon>)this._polygons).add(polygon);
                }
                parts = new int[partlist.size()];
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = partlist.get(i);
                }
                break;
            default:
                org.meteoinfo.jts.geom.Polygon poly = (org.meteoinfo.jts.geom.Polygon) geometry;
                _numParts = poly.getNumInteriorRing() + 1;                
                parts = new int[_numParts];                
                parts[0] = 0;
                Polygon polygon = new Polygon();
                pp = new ArrayList<>();
                for (int j = 0; j < poly.getExteriorRing().getNumPoints(); j++) {
                    pp.add(points.get(j));
                }
                polygon.setOutLine(pp);
                idx = poly.getExteriorRing().getNumPoints();
                for (int j = 0; j < poly.getNumInteriorRing(); j++) {
                    parts[j + 1] = idx;
                    pp = new ArrayList<>();
                    for (int k = idx; k < idx + poly.getInteriorRingN(j).getNumPoints(); k++) {
                        pp.add(points.get(k));
                    }
                    polygon.addHole(pp);
                    idx += poly.getInteriorRingN(j).getNumPoints();
                }
                ((List<Polygon>)this._polygons).add(polygon);
                break;
        }   
        this.setExtent(MIMath.getPointsExtent(_points));
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    @Override
    public ShapeTypes getShapeType() {
        return ShapeTypes.Polygon;
    }

    /**
     * To geometry method
     *
     * @param factory GeometryFactory
     * @return Geometry
     */
    @Override
    public Geometry toGeometry(GeometryFactory factory) {
        if (this._polygons.size() == 1) {
            return this._polygons.get(0).toGeometry(factory);
        } else {
            org.meteoinfo.jts.geom.Polygon[] polygons = new org.meteoinfo.jts.geom.Polygon[this._polygons.size()];
            for (int j = 0; j < polygons.length; j++) {
                polygons[j] = (org.meteoinfo.jts.geom.Polygon) this._polygons.get(j).toGeometry(factory);
            }
            MultiPolygon mls = factory.createMultiPolygon(polygons);
            return mls;
        }
    }

    /**
     * Get points
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
        updatePolygons();
    }
    
    /**
     * Set points and keep the polygons
     * @param points
     */
    public void setPoints_keep(PointF[] points){
        List<PointD> ps = new ArrayList<>();
        for (int i = 0; i < points.length; i++){
            ps.add(new PointD(points[i].X, points[i].Y));
        }
        _points = ps;
        this.setExtent(MIMath.getPointsExtent(_points));
        updatePolygons_keep();
    }
    
    /**
     *
     * @param points
     */
    public void setPoints(PointF[] points){
        List<PointD> ps = new ArrayList<>();
        for (PointF point : points) {
            ps.add(new PointD(point.X, point.Y));
        }
        setPoints(ps);
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
     * Get parts
     * @return Parts
     */
    public int[] getParts(){
        return this.parts;
    }
    
    /**
     * Set parts
     * @param value Parts 
     */
    public void setParts(int[] value){
        this.parts = value;
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
     * Get polygons
     *
     * @return polygon list
     */
    public List<? extends Polygon> getPolygons() {
        return _polygons;
    }

    /**
     * Set polygons
     *
     * @param polygons polygon list
     */
    public void setPolygons(List<Polygon> polygons) {
        _polygons = polygons;
        updatePartsPoints();
    }

    /**
     * Get area
     *
     * @return area
     */
    public double getArea() {
        double area = 0.0;
        for (Polygon aPG : _polygons) {
            area += GeoComputation.getArea(aPG.getOutLine());
            for (List<? extends PointD> hole : aPG.getHoleLines()) {
                area -= GeoComputation.getArea(hole);
            }
        }
        
        return area;
    }

    /**
     * Get spherical area
     *
     * @return spherical area
     */
    public double getSphericalArea() {
        double area = 0.0;
        for (Polygon aPG : _polygons) {
            area += GeoComputation.sphericalPolygonArea(aPG.getOutLine());
            for (List<? extends PointD> hole : aPG.getHoleLines()) {
                area -= GeoComputation.sphericalPolygonArea(hole);
            }
        }
        
        return area;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    protected void updatePolygons_keep() {
        if (_numParts == 1) {
            _polygons.get(0).setOutLine(_points);
        } else {
            int sidx = 0, eidx = 0;
            for (Polygon poly : _polygons){
                eidx = sidx + poly.getOutLine().size();
                poly.setOutLine(_points.subList(sidx, eidx));
                sidx = eidx;
                if (poly.hasHole()){
                    for (int i = 0; i < poly.getHoleLineNumber(); i++){
                        eidx = sidx + poly.getHoleLine(i).size();
                        poly.setHoleLine(i, _points.subList(sidx, eidx));
                        sidx = eidx;
                    }
                }
            }            
        }
    }
    
    protected void updatePolygons() {
        _polygons = new ArrayList<>();
        if (_numParts == 1) {
            Polygon aPolygon = new Polygon();
            aPolygon.setOutLine(_points);
            ((List<Polygon>)_polygons).add(aPolygon);
        } else {
            PointD[] Pointps;
            Polygon aPolygon = null;
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
                
                if (GeoComputation.isClockwise(Pointps)) {
                    if (p > 0) {
                        ((List<Polygon>)_polygons).add(aPolygon);
                    }
                    
                    aPolygon = new Polygon();
                    aPolygon.setOutLine(Arrays.asList(Pointps));
                } else if (aPolygon == null) {
                    MIMath.arrayReverse(Pointps);
                    aPolygon = new Polygon();
                    aPolygon.setOutLine(Arrays.asList(Pointps));
                } else {
                    aPolygon.addHole(Arrays.asList(Pointps));
                }
            }
            ((List<Polygon>)_polygons).add(aPolygon);
        }
    }
    
    private void updatePartsPoints() {
        _numParts = 0;
        _points = new ArrayList<>();
        List<Integer> partList = new ArrayList<>();
        for (int i = 0; i < _polygons.size(); i++) {
            _numParts += _polygons.get(i).getRingNumber();
            for (int j = 0; j < _polygons.get(i).getRingNumber(); j++) {
                partList.add(_points.size());
                ((List<PointD>)_points).addAll(_polygons.get(i).getRings().get(j));                
            }
        }
        parts = new int[partList.size()];
        for (int i = 0; i < partList.size(); i++) {
            parts[i] = partList.get(i);
        }
        if (_points.size() > 0)
            this.setExtent(MIMath.getPointsExtent(_points));
    }
    
    /**
     * Add a hole line
     * @param points Hole points
     * @return Hole index
     */
    public int addHole(List<? extends PointD> points){
        return addHole(points, 0);
    }

    /**
     * Add a hole line
     *
     * @param points point list
     * @param polygonIdx polygon index
     * @return Hole index
     */
    public int addHole(List<? extends PointD> points, int polygonIdx) {
        Polygon aPolygon = _polygons.get(polygonIdx);
        aPolygon.addHole(points);
        
        updatePartsPoints();
        return aPolygon.getHoleLines().size() - 1;
    }
    
    /**
     * Remove a hole
     * @param polygonIdx Polygon index
     * @param holeIdx Hole index
     */
    public void removeHole(int polygonIdx, int holeIdx){
        Polygon poly = _polygons.get(polygonIdx);
        poly.removeHole(holeIdx);
        
        updatePartsPoints();
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
        this.updatePolygons();
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
        this.updatePolygons();
    }
    
    /**
     * Reverse points direction
     */
    @Override
    public void reverse(){
        Collections.reverse(_points);
    }
    
    //@Override
    public Object clone_back() {
        return (PolygonShape) super.clone();
    }

    /**
     * Clone
     *
     * @return PolygonShape
     */
    @Override
    public Object clone() {
        PolygonShape aPGS = new PolygonShape();
        aPGS.setExtent(this.getExtent());
        aPGS.highValue = highValue;
        aPGS.lowValue = lowValue;
        aPGS._numParts = _numParts;
        aPGS.parts = (int[]) parts.clone();
        List<PointD> points = new ArrayList<>();
        for (PointD p : _points){
            points.add((PointD)p.clone());
        }
        aPGS.setPoints(points);
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());
        aPGS.setLegendIndex(this.getLegendIndex());
        
        return aPGS;
    }

    /**
     * Clone polygon shape with values
     *
     * @return new polygon shape
     */
    public PolygonShape valueClone() {
        PolygonShape aPGS = new PolygonShape();
        aPGS.highValue = highValue;
        aPGS.lowValue = lowValue;
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());
        aPGS.setLegendIndex(this.getLegendIndex());
        
        return aPGS;
    }
    
    /**
     * Constructor
     * @param other Other polygon shape
     */
    @Override
    public void cloneValue(Shape other){
        PolygonShape o = (PolygonShape)other;
        this.setExtent(o.getExtent());
        this.highValue = o.highValue;
        this.lowValue = o.lowValue;
        this._numParts = o._numParts;
        this.parts = (int[]) o.parts.clone();
        List<PointD> points = new ArrayList<>();
        for (PointD p : o._points){
            points.add((PointD)p.clone());
        }
        this.setPoints(points);
        this.setVisible(o.isVisible());
        this.setSelected(o.isSelected());
        this.setLegendIndex(o.getLegendIndex());
    }
    
    /**
     * Get difference shape
     * @param b Other shape
     * @return Difference shape
     */
    @Override
    public Shape difference(Shape b){
        if (this.contains(b)){
            PolygonShape r = (PolygonShape)this.clone();
            r.addHole(b.getPoints(), 0);
            return r;
        } else {
            return super.difference(b);
        }
    }

    // </editor-fold>
}
