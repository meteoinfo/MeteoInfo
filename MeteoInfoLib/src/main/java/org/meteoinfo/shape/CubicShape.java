package org.meteoinfo.shape;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geoprocess.GeometryUtil;

import java.util.ArrayList;
import java.util.List;

public class CubicShape extends Shape{

    private List<PointZ> points;
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
    public CubicShape() {
        super();
        this.points = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            this.points.add(new PointZ());
        }
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    /**
     * Constructor
     */
    public CubicShape(List<PointZ> points) {
        super();
        this.points = points;
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
    }

    @Override
    public ShapeTypes getShapeType() {
        return ShapeTypes.CUBIC;
    }

    @Override
    public Geometry toGeometry(GeometryFactory factory) {
        return null;
    }

    /**
     * Get vertex points
     * @return Vertex points
     */
    public List<PointZ> getPoints() {
        return this.points;
    }

    /**
     * Set vertex points
     * @param value Vertex points
     */
    public void setPoints(List<? extends PointD> value) {
        this.points = (List<PointZ>)value;
        this.setExtent(GeometryUtil.getPointsExtent(this.points));
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
