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

import org.meteoinfo.geometry.geoprocess.GeoComputation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.meteoinfo.common.DataConvert;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Polygon class
 *
 * @author Yaqiang Wang
 */
public class Polygon extends Geometry {
    // <editor-fold desc="Variables">

    protected LinearRing shell = null;
    protected List<LinearRing> holes;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Polygon() {
        holes = new ArrayList<>();
    }

    /**
     * Constructor
     * @param shell Shell
     * @param holes Holes
     */
    public Polygon(LinearRing shell, List<LinearRing> holes) {
        this.shell = shell;
        this.holes = holes;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYGON;
    }

    @Override
    public Coordinate getCoordinate() {
        return shell.getCoordinate();
    }

    @Override
    public List<Coordinate> getCoordinates() {
        if (isEmpty()) {
            return new ArrayList<Coordinate>();
        }
        List<Coordinate> coordinates = new ArrayList<>();
        List<Coordinate> shellCoordinates = shell.getCoordinates();
        for (int x = 0; x < shellCoordinates.size(); x++) {
            coordinates.add(shellCoordinates.get(x));
        }
        for (int i = 0; i < holes.size(); i++) {
            List<Coordinate> childCoordinates = holes.get(i).getCoordinates();
            for (int j = 0; j < childCoordinates.size(); j++) {
                coordinates.add(childCoordinates.get(j));
            }
        }
        return coordinates;
    }

    @Override
    public void updateExtent() {
        this.extent = shell.extent;
        for (LinearRing hole : holes) {
            extent.expandToInclude(hole.extent);
        }
    }

    @Override
    public int getNumPoints() {
        int numPoints = shell.getNumPoints();
        for (int i = 0; i < holes.size(); i++) {
            numPoints += holes.get(i).getNumPoints();
        }
        return numPoints;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get outLine
     *
     * @return outLine point list
     */
    public List<Coordinate> getOutLine() {
        return shell.getCoordinates();
    }

    /**
     * Set outLine point list
     *
     * @param outLine outLine point list
     */
    public void setOutLine(List<Coordinate> outLine) {
        shell = new LinearRing(outLine);
        extent = GeometryUtil.getPointsExtent(outLine);
    }

    /**
     * Get hole lines
     *
     * @return hole lines
     */
    public List<List<Coordinate>> getHoleLines() {
        List<List<Coordinate>> holeLines = new ArrayList<>();
        for (LinearRing ring : holes) {
            holeLines.add(ring.getCoordinates());
        }
        return holeLines;
    }
    
    /**
     * Get a hole line
     * @param idx Index
     * @return A hole line
     */
    public List<Coordinate> getHoleLine(int idx) {
        return this.holes.get(idx).getCoordinates();
    }

    /**
     * Set hole lines
     *
     * @param holeLines hole lines list
     */
    public void setHoleLines(List<List<Coordinate>> holeLines) {
        this.holes = new ArrayList<>();
        for (List<Coordinate> coors : holeLines) {
            this.holes.add(new LinearRing(coors));
        }
    }
    
    /**
     * Set a hole line
     * @param idx Index
     * @param holeLine The hole line
     */
    public void setHoleLine(int idx, List<Coordinate> holeLine){
        if (GeoComputation.isClockwise(holeLine)) {
            Collections.reverse(holeLine);
        }
        holes.set(idx, new LinearRing(holeLine));
    }

    /**
     * Get exterior ring (shell)
     * @return Exterior ring
     */
    public LinearRing getExteriorRing() {
        return shell;
    }

    /**
     * Set coordinates
     * @param coordinates Coordinates
     */
    public void setCoordinates(List<Coordinate> coordinates) {
        shell = new LinearRing(coordinates);
    }

    @Override
    public boolean isEmpty() {
        return shell == null || shell.isEmpty();
    }

    @Override
    public Geometry reverse() {
        LinearRing shell = (LinearRing) getExteriorRing().reverse();
        List<LinearRing> holes = new ArrayList<>();
        for (int i = 0; i < getNumInteriorRing(); i++) {
            holes.add ((LinearRing) getInteriorRingN(i).reverse());
        }

        return new Polygon(shell, holes);
    }

    /**
     * Get rings
     *
     * @return Rings
     */
    public List<List<Coordinate>> getRings() {
        List<List<Coordinate>> rings = new ArrayList<>();
        rings.add(shell.getCoordinates());
        if (hasHoles()) {
            rings.addAll(getHoleLines());
        }

        return rings;
    }

    /**
     * Determine if the polygon has holes
     *
     * @return boolean
     */
    public boolean hasHoles() {
        return (holes.size() > 0);
    }
    
    /**
     * Get hole line number
     * @return Hole line number
     */
    public int getNumInteriorRing(){
        return this.holes.size();
    }

    public LinearRing getInteriorRingN(int n) {
        return holes.get(n);
    }

    /**
     * Get ring number - outline number + holeline number
     *
     * @return ring number
     */
    public int getRingNumber() {
        return holes.size() + 1;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a hole line
     *
     * @param points point list
     */
    public void addHole(List<Coordinate> points) {
        if (GeoComputation.isClockwise(points)) {
            Collections.reverse(points);
        }
        holes.add(new LinearRing(points));
    }
    
    /**
     * Remove a hole line
     * @param holeIdx Hole index
     */
    public void removeHole(int holeIdx){
        this.holes.remove(holeIdx);
    }

    /**
     * To geometry
     *
     * @param factory GeometryFactory
     * @return Geometry
     */
    public org.locationtech.jts.geom.Geometry toJTSGeometry(GeometryFactory factory) {
        Coordinate p;
        org.locationtech.jts.geom.Coordinate[] cs = new org.locationtech.jts.geom.Coordinate[shell.getNumPoints()];
        for (int i = 0; i < cs.length; i++) {
            p = shell.getCoordinateN(i);
            cs[i] = new org.locationtech.jts.geom.Coordinate(p.x, p.y);
        }
        if (!cs[0].equals(cs[cs.length - 1])){
            cs = (org.locationtech.jts.geom.Coordinate[])DataConvert.resizeArray(cs, cs.length + 1);
            cs[cs.length - 1] = (org.locationtech.jts.geom.Coordinate) cs[0].clone();
        }
        org.locationtech.jts.geom.LinearRing shell = factory.createLinearRing(cs);
        org.locationtech.jts.geom.LinearRing[] holes = new org.locationtech.jts.geom.LinearRing[this.holes.size()];
        for (int j = 0; j < holes.length; j++) {
            List<Coordinate> hole = this.holes.get(j).getCoordinates();
            cs = new org.locationtech.jts.geom.Coordinate[hole.size()];
            for (int i = 0; i < hole.size(); i++) {
                p = hole.get(i);
                cs[i] = new org.locationtech.jts.geom.Coordinate(p.x, p.y);
            }      
            if (!cs[0].equals(cs[cs.length - 1])){
                cs = (org.locationtech.jts.geom.Coordinate[])DataConvert.resizeArray(cs, cs.length + 1);
                cs[cs.length - 1] = (org.locationtech.jts.geom.Coordinate) cs[0].clone();
            }
            holes[j] = factory.createLinearRing(cs);
        }
        return factory.createPolygon(shell, holes);
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
