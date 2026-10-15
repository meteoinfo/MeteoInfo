package org.meteoinfo.geometry.special;

import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Geometry;
import org.meteoinfo.geometry.GeometryType;
import org.meteoinfo.geometry.GeometryUtil;

import java.util.ArrayList;
import java.util.List;

public class Cubic extends Geometry {

    private List<Coordinate> points;
    private int[][] index = new int[][]{
        {0, 2, 3, 1},
        {0, 4, 6, 2},
        {0, 1, 5, 4},
        {4, 5, 7, 6},
        {1, 3, 7, 5},
        {2, 6, 7, 3}};
    private int[][] lineIndex = new int[][]{
            {0, 1},
            {0, 2},
            {0, 4},
            {1, 3},
            {1, 5},
            {2, 3},
            {2, 6},
            {3, 7},
            {4, 5},
            {4, 6},
            {5, 7},
            {6, 7}};

    /**
     * Constructor
     */
    public Cubic() {
        super();
        this.points = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            this.points.add(new Coordinate());
        }
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    /**
     * Constructor
     */
    public Cubic(List<Coordinate> points) {
        super();
        this.points = points;
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.CUBIC;
    }

    @Override
    public Coordinate getCoordinate() {
        return null;
    }

    @Override
    public org.locationtech.jts.geom.Geometry toJTSGeometry(GeometryFactory factory) {
        return null;
    }

    /**
     * Get vertex points
     * @return Vertex points
     */
    public List<Coordinate> getCoordinates() {
        return this.points;
    }

    @Override
    public void updateExtent() {
        this.extent = GeometryUtil.getPointsExtent(this.points);
    }

    @Override
    public int getNumPoints() {
        return this.points.size();
    }

    /**
     * Set vertex points
     * @param value Vertex points
     */
    public void setCoordinates(List<Coordinate> value) {
        this.points = (List<Coordinate>)value;
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Geometry reverse() {
        return null;
    }

    /**
     * Get quads point index
     * @return Quads point index
     */
    public int[][] getIndex() {
        return this.index;
    }

    /**
     * Get a quads point index
     * @param i i th quads
     * @return A quads point index
     */
    public int[] getIndex(int i) {
        return this.index[i];
    }

    /**
     * Set quads point index
     * @param value Quads point index
     */
    public void setIndex(int[][] value) {
        this.index = value;
    }

    /**
     * Get lines point index
     * @return Lines point index
     */
    public int[][] getLineIndex() {
        return this.lineIndex;
    }

    /**
     * Get a line point index
     * @param i i th line
     * @return A line point index
     */
    public int[] getLineIndex(int i) {
        return this.lineIndex[i];
    }
}
