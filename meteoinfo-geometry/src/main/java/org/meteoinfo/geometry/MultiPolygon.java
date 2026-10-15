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

import org.checkerframework.checker.units.qual.C;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointF;
import org.meteoinfo.geometry.geoprocess.GeoComputation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;

/**
 * PolygonShape class
 *
 * @author Yaqiang Wang
 */
public class MultiPolygon extends GeometryCollection implements Cloneable {
    // <editor-fold desc="Variables">
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
    public MultiPolygon() {
        _numParts = 1;
        parts = new int[1];
        parts[0] = 0;
    }

    public MultiPolygon(Polygon[] polygons) {
        super(polygons);
        _numParts = polygons.length;
    }
        
    /**
     * Constructor
     *
     * @param geometry Geometry
     */
    public MultiPolygon(org.locationtech.jts.geom.Geometry geometry) {
        this();
        org.locationtech.jts.geom.Coordinate[] cs = geometry.getCoordinates();
        List<Coordinate> points = new ArrayList();
        for (org.locationtech.jts.geom.Coordinate c : cs) {
            points.add(new Coordinate(c.x, c.y, c.z));
        }

        List<Coordinate> pp;
        switch (geometry.getGeometryType()) {
            case "MultiPolygon":
                int n = geometry.getNumGeometries();                
                _numParts = 0;
                List<Integer> partlist = new ArrayList<>();
                this.geometries = new Geometry[n];
                int idx = 0;
                for (int i = 0; i < n; i++) {
                    org.locationtech.jts.geom.Polygon poly = (org.locationtech.jts.geom.Polygon) geometry.getGeometryN(i);
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
                    geometries[i] = polygon;
                }
                parts = new int[partlist.size()];
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = partlist.get(i);
                }
                break;
            default:
                org.locationtech.jts.geom.Polygon poly = (org.locationtech.jts.geom.Polygon) geometry;
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
                this.geometries = new Polygon[1];
                geometries[0] = polygon;
                break;
        }   
        this.updateExtent();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_POLYGON;
    }

    /**
     * Get is multi polygon or not
     * @return Multi polygon or not
     */
    public boolean isMultiPolygon() {
        return this._numParts > 1;
    }

    /**
     * To geometry method
     *
     * @param factory GeometryFactory
     * @return Geometry
     */
    @Override
    public org.locationtech.jts.geom.Geometry toJTSGeometry(GeometryFactory factory) {
        if (this.geometries.length == 1) {
            return this.geometries[0].toJTSGeometry(factory);
        } else {
            org.locationtech.jts.geom.Polygon[] polygons = new org.locationtech.jts.geom.Polygon[this.getNumGeometries()];
            for (int j = 0; j < polygons.length; j++) {
                polygons[j] = (org.locationtech.jts.geom.Polygon) this.getGeometryN(j).toJTSGeometry(factory);
            }
            org.locationtech.jts.geom.MultiPolygon mls = factory.createMultiPolygon(polygons);
            return mls;
        }
    }

    /**
     * Set points
     *
     * @param coordinates point list
     */
    public void setCoordinates(List<Coordinate> coordinates) {
        updatePolygons(coordinates);
        this.updateExtent();
    }

    /**
     * Set points and keep the polygons
     * @param points The points
     */
    public void setPoints_keep(List<Coordinate> points){
        updatePolygons_keep(points);
        this.updateExtent();
    }
    
    /**
     * Set points and keep the polygons
     * @param points
     */
    public void setPoints_keep(PointF[] points){
        List<Coordinate> ps = new ArrayList<>();
        for (int i = 0; i < points.length; i++){
            ps.add(new Coordinate(points[i].X, points[i].Y));
        }
        setPoints_keep(ps);
    }
    
    /**
     *
     * @param points
     */
    public void setPoints(PointF[] points){
        List<Coordinate> ps = new ArrayList<>();
        for (PointF point : points) {
            ps.add(new Coordinate(point.X, point.Y));
        }
        setCoordinates(ps);
    }

    /**
     * Get part number
     *
     * @return Part number
     */
    public int getNumParts() {
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
     * Get polygons
     *
     * @return polygon list
     */
    public List<Polygon> getPolygons() {
        List<Polygon> ls = new ArrayList<>();
        for (Geometry geom : this.geometries) {
            ls.add((Polygon) geom);
        }
        return ls;
    }

    /**
     * Get a polygon
     * @param index The polygon index
     * @return The polygon
     */
    public Polygon getPolygon(int index) {
        return (Polygon) geometries[index];
    }

    /**
     * Set polygons
     *
     * @param polygons polygon list
     */
    public void setPolygons(List<Polygon> polygons) {
        geometries = polygons.toArray(new Polygon[0]);
        updatePartsPoints();
    }

    /**
     * Get Z Array
     *
     * @return Z value array
     */
    public double[] getZArray() {
        double[] zArray = new double[this.getCoordinates().size()];
        for (int i = 0; i < this.getCoordinates().size(); i++) {
            zArray[i] = ((Coordinate)this.getCoordinates().get(i)).z;
        }

        return zArray;
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
     * Get M Array
     *
     * @return M value array
     */
    public double[] getMArray() {
        double[] mArray = new double[this.getCoordinates().size()];
        for (int i = 0; i < this.getCoordinates().size(); i++) {
            mArray[i] = ((Coordinate)this.getCoordinates().get(i)).m;
        }

        return mArray;
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
     * Get area
     *
     * @return area
     */
    public double getArea() {
        double area = 0.0;
        for (Polygon aPG : (Polygon[]) geometries) {
            area += GeoComputation.getArea(aPG.getOutLine());
            for (List<Coordinate> hole : aPG.getHoleLines()) {
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
        for (Polygon aPG : (Polygon[]) geometries) {
            area += GeoComputation.sphericalPolygonArea(aPG.getOutLine());
            for (List<Coordinate> hole : aPG.getHoleLines()) {
                area -= GeoComputation.sphericalPolygonArea(hole);
            }
        }
        
        return area;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    protected void updatePolygons_keep(List<Coordinate> coordinates) {
        if (_numParts == 1) {
            ((Polygon) geometries[0]).setOutLine(coordinates);
        } else {
            int sidx = 0, eidx = 0;
            for (Polygon poly : (Polygon[]) geometries) {
                eidx = sidx + poly.getOutLine().size();
                poly.setOutLine(coordinates.subList(sidx, eidx));
                sidx = eidx;
                if (poly.hasHoles()){
                    for (int i = 0; i < poly.getNumInteriorRing(); i++){
                        eidx = sidx + poly.getHoleLine(i).size();
                        poly.setHoleLine(i, coordinates.subList(sidx, eidx));
                        sidx = eidx;
                    }
                }
            }            
        }
    }
    
    protected void updatePolygons(List<Coordinate> coordinates) {
        if (_numParts == 1) {
            Polygon aPolygon = new Polygon();
            aPolygon.setOutLine(coordinates);
            geometries = new Polygon[]{aPolygon};
        } else {
            Coordinate[] Pointps;
            Polygon aPolygon = null;
            int numPoints = isEmpty() ? coordinates.size() : this.getNumPoints();
            List<Polygon> polys = new ArrayList<>();
            for (int p = 0; p < _numParts; p++) {
                if (p == _numParts - 1) {
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
                
                if (GeoComputation.isClockwise(Pointps)) {
                    if (p > 0) {
                        polys.add(aPolygon);
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
            polys.add(aPolygon);
            geometries = polys.toArray(new Polygon[0]);
        }
    }
    
    private void updatePartsPoints() {
        _numParts = 0;
        int pn = 0;
        List<Integer> partList = new ArrayList<>();
        for (int i = 0; i < geometries.length; i++) {
            Polygon polygon = (Polygon) geometries[i];
            _numParts += polygon.getRingNumber();
            partList.add(pn);
            pn += polygon.getExteriorRing().getNumPoints();
            for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                partList.add(pn);
                pn += polygon.getInteriorRingN(j).getNumPoints();
            }
        }
        parts = new int[partList.size()];
        for (int i = 0; i < partList.size(); i++) {
            parts[i] = partList.get(i);
        }

        this.updateExtent();
    }
    
    /**
     * Add a hole line
     * @param points Hole points
     * @return Hole index
     */
    public int addHole(List<Coordinate> points){
        return addHole(points, 0);
    }

    /**
     * Add a hole line
     *
     * @param points point list
     * @param polygonIdx polygon index
     * @return Hole index
     */
    public int addHole(List<Coordinate> points, int polygonIdx) {
        Polygon aPolygon = (Polygon) geometries[polygonIdx];
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
        Polygon poly = (Polygon) geometries[polygonIdx];
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
    public void addVertice(int vIdx, Coordinate vertice) {
        int partIdx = getPartIndex(vIdx);
        if (partIdx < _numParts - 1) {
            parts[partIdx + 1] += 1;
        }

        List<Coordinate> coordinates = getCoordinates();
        coordinates.add(vIdx, vertice);
        this.updatePolygons(coordinates);
        this.updateExtent();
    }

    /**
     * Remove a vertice
     *
     * @param vIdx Vertice index
     */
    public void removeVertice(int vIdx) {
        int partIdx = getPartIndex(vIdx);
        if (partIdx < _numParts - 1) {
            parts[partIdx + 1] -= 1;
        }

        List<Coordinate> coordinates = getCoordinates();
        coordinates.remove(vIdx);
        this.updatePolygons(coordinates);
        this.updateExtent();
    }

    /**
     * Clone
     *
     * @return PolygonShape
     */
    @Override
    public Object clone() {
        MultiPolygon aPGS = new MultiPolygon();
        aPGS.setExtent(this.getExtent());
        aPGS.highValue = highValue;
        aPGS.lowValue = lowValue;
        aPGS._numParts = _numParts;
        aPGS.parts = (int[]) parts.clone();
        aPGS.geometries = (Polygon[]) geometries.clone();
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
    public MultiPolygon valueClone() {
        MultiPolygon aPGS = new MultiPolygon();
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
    public void cloneValue(Geometry other){
        MultiPolygon o = (MultiPolygon)other;
        this.setExtent(o.getExtent());
        this.highValue = o.highValue;
        this.lowValue = o.lowValue;
        this._numParts = o._numParts;
        this.parts = (int[]) o.parts.clone();
        this.geometries = (Polygon[]) o.geometries.clone();
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
    public Geometry difference(Geometry b){
        if (this.contains(b)){
            MultiPolygon r = (MultiPolygon)this.clone();
            r.addHole(b.getCoordinates(), 0);
            return r;
        } else {
            return super.difference(b);
        }
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
