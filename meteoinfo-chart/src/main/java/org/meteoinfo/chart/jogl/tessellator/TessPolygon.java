package org.meteoinfo.chart.jogl.tessellator;

import org.meteoinfo.chart.graphic.Triangle;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Polygon;

import java.util.ArrayList;
import java.util.List;

public class TessPolygon extends Polygon {
    protected List<Triangle> triangles;

    /**
     * Constructor
     */
    public TessPolygon() {
        this.triangles = new ArrayList<>();
    }

    /**
     * Constructor
     * @param polygon Input Polygon
     */
    public TessPolygon(Polygon polygon) {
        this.setOutLine(polygon.getOutLine());
        this.setHoleLines(polygon.getHoleLines());
        this.setExtent(polygon.getExtent());

        this.triangles = TriangleTessellator.tessellate(polygon);
    }

    /**
     * Get triangles
     * @return Triangle list
     */
    public List<Triangle> getTriangles() {
        return this.triangles;
    }

    /**
     * Subdivide triangles internal
     * @param depth Subdivision depth; each level produces 4 child triangles (4^depth total)
     */
    public void subdivide(int depth) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle triangle : this.triangles) {
            result.addAll(TriangleTessellator.subdivide(triangle, depth));
        }
        this.triangles = result;
    }

    /**
     * Subdivide triangles internal
     * @param targetEdgeLen Maximum allowed edge length; subdivision stops when
     *                      all edges are shorter than this threshold
     */
    public void subdivideEquilateral(float targetEdgeLen) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle triangle : this.triangles) {
            result.addAll(TriangleTessellator.subdivideEquilateral(triangle, targetEdgeLen));
        }
        this.triangles = result;
    }
}
