package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.MultiPolygon;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PolygonGraphic extends Graphic {

    private Array xData;
    private Array yData;
    private Array cData;
    private LegendScheme legendScheme;
    protected float[] vertexPosition;
    protected float[] vertexValue;
    protected float[] vertexColor;
    protected float[] vertexNormal;
    protected boolean faceInterp;
    protected boolean edgeInterp;

    /**
     * Constructor
     * @param shape Polygon shape
     * @param legend Polygon legend break
     */
    public PolygonGraphic(MultiPolygon shape, PolygonBreak legend) {
        this.geometry = shape;
        this.legendBreak = legend;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param polygonBreak Polygon break
     */
    public PolygonGraphic(Array xData, Array yData, PolygonBreak polygonBreak) {
        this.xData = xData;
        this.yData = yData;

        updateShape();
        if (polygonBreak == null) {
            polygonBreak = new PolygonBreak();
        }
        this.legendBreak = polygonBreak;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     */
    public PolygonGraphic(Array xData, Array yData) {
        this(xData, yData, new PolygonBreak());
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param cData Color data
     * @param legendScheme Legend scheme
     */
    public PolygonGraphic(Array xData, Array yData, Array cData, LegendScheme legendScheme) {
        this.xData = xData;
        this.yData = yData;
        this.cData = cData;

        updateShapeLegend(legendScheme);
    }

    protected void updateShape() {
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        double x, y;
        while (xIter.hasNext() && yIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            points.add(new Coordinate(x, y));
        }
        if (this.geometry == null) {
            this.geometry = new MultiPolygon();
        }
        this.geometry.setCoordinates(points);
    }

    protected void updateShapeLegend(LegendScheme legendScheme) {
        this.legendScheme = legendScheme;
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        IndexIterator cIter = this.cData.getIndexIterator();
        ColorBreakCollection cbc = new ColorBreakCollection();
        ColorBreak cb;
        double x, y, c;
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            c = cIter.getDoubleNext();
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            points.add(new Coordinate(x, y));
            cb = legendScheme.findLegendBreakAlways(c);
            cbc.add(cb);
        }
        if (this.geometry == null) {
            this.geometry = new MultiPolygon();
        }
        if (points.size() >= 2)
            this.geometry.setCoordinates(points);

        this.legendBreak = cbc;
    }

    /**
     * Get x data
     * @return X data
     */
    public Array getXData() {
        return this.xData;
    }

    /**
     * Set x data
     * @param xData X data
     */
    public void setXData(Array xData) {
        this.xData = xData;
        updateShape();
    }

    /**
     * Get y data
     * @return Y data
     */
    public Array getYData() {
        return this.yData;
    }

    /**
     * Set y data
     * @param yData Y data
     */
    public void setYData(Array yData) {
        this.yData = yData;
        updateShape();
    }

    /**
     * Set data
     * @param xData X data
     * @param yData Y data
     */
    public void setData(Array xData, Array yData) {
        this.xData = xData;
        this.yData = yData;
        updateShape();
    }

    /**
     * Get vertex position
     * @return Vertex position
     */
    public float[] getVertexPosition() {
        return vertexPosition;
    }

    /**
     * Get vertex position
     * @param transform The transform
     * @return Vertex position
     */
    public float[] getVertexPosition(Transform transform) {
        int n = vertexPosition.length;
        float[] vData = new float[n];
        for (int i = 0; i < n; i+=3) {
            vData[i] = transform.transform_x(vertexPosition[i]);
            vData[i + 1] = transform.transform_y(vertexPosition[i + 1]);
            vData[i + 2] = transform.transform_z(vertexPosition[i + 2]);
        }

        return vData;
    }

    /**
     * Get vertex values
     * @return Vertex values
     */
    public float[] getVertexValue() {
        return this.vertexValue;
    }

    /**
     * Set vertex values
     * @param value Vertex values
     */
    public void setVertexValue(float[] value) {
        this.vertexValue = value;
    }

    /**
     * Get vertex color data
     * @return Vertex color data
     */
    public float[] getVertexColor() {
        return vertexColor;
    }

    /**
     * Set vertex color data
     * @param value Vertex color data
     */
    public void setVertexColor(float[] value) {
        this.vertexColor = value;
    }

    /**
     * Get vertex normal
     * @return Vertex normal
     */
    public float[] getVertexNormal() {
        return vertexNormal;
    }

    /**
     * Set vertex normal
     * @param value Vertex normal
     */
    public void setVertexNormal(float[] value) {
        this.vertexNormal = value;
    }

    /**
     * Get whether using interpolated coloring for each face
     * @return Boolean
     */
    public boolean isFaceInterp() {
        return this.faceInterp;
    }

    /**
     * Set whether using interpolated coloring for each face
     * @param value Boolean
     */
    public void setFaceInterp(boolean value) {
        this.faceInterp = value;
    }

    /**
     * Get whether using interpolated coloring for each edge
     * @return Boolean
     */
    public boolean isEdgeInterp() {
        return this.edgeInterp;
    }

    /**
     * Set whether using interpolated coloring for each edge
     * @param value Boolean
     */
    public void setEdgeInterp(boolean value) {
        this.edgeInterp = value;
    }

    /**
     * Get vertex number
     * @return Vertex number
     */
    public int getVertexNumber() {
        return vertexPosition.length / 3;
    }

    /**
     * Get face number
     * @return Face number
     */
    public int getFaceNumber() {
        return getVertexNumber() / 3;
    }

    /**
     * Get vertex
     * @param vData Vertex array
     * @param idx Vertex index
     * @return Vertex
     */
    public Vector3f getVertex(float[] vData, int idx) {
        return new Vector3f(vData[idx * 3], vData[idx * 3 + 1], vData[idx * 3 + 2]);
    }

    /**
     * Get vertex
     * @param idx Vertex index
     * @return Vertex
     */
    public Vector3f getVertex(int idx) {
        return getVertex(vertexPosition, idx);
    }

    /**
     * Get a triangle
     * @param vData Vertex array
     * @param idx Triangle index
     * @return The triangle
     */
    public Triangle getTriangle(float[] vData, int idx) {
        Vector3f a = getVertex(vData, idx * 9);
        Vector3f b = getVertex(vData, idx * 9 + 3);
        Vector3f c = getVertex(vData, idx * 9 + 6);

        return new Triangle(a, b, c);
    }

    /**
     * Get a triangle
     * @param idx Triangle index
     * @return The triangle
     */
    public Triangle getTriangle(int idx) {
        return getTriangle(vertexPosition, idx);
    }

    /**
     * Get all triangles
     * @param vData Vertex array
     * @return All triangles
     */
    public List<Triangle> getTriangles(float[] vData) {
        int n = getFaceNumber();
        List<Triangle> triangles = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            triangles.add(getTriangle(i));
        }

        return triangles;
    }

    /**
     * Get all triangles
     * @return All triangles
     */
    public List<Triangle> getTriangles() {
        return getTriangles(this.vertexPosition);
    }

    /**
     * Given the vertex coordinates of a shape this function calculates the
     * normal vector coordinates.
     */
    public void calculateNormalVectors(float[] vData) {
        if (this.vertexNormal != null)
            return;

        List<Triangle> triangles = getTriangles(vData);
        int nVertex = getVertexNumber();
        vertexNormal = new float[vData.length];
        Vector3f vertex, normal;
        List<Integer> indexes;
        HashMap<Vector3f, Vector3f> normalMap = new LinkedHashMap<>();
        for (Triangle triangle : triangles) {
            List<Vector3f> points = triangle.getPoints();
            List<Vector3f> normals = triangle.getNormals();
            for (int i = 0; i < 3; i++) {
                vertex = points.get(i);
                normal = normals.get(i);
                if (normalMap.containsKey(vertex)) {
                    normalMap.put(vertex, normalMap.get(vertex).add(normal));
                } else {
                    normalMap.put(vertex, normal);
                }
            }
        }
        for (int i = 0; i < nVertex; i++) {
            vertex = getVertex(i);
            normal = normalMap.get(vertex);
            normal.normalize();
            normal.negate();
            vertexNormal[i * 3] = normal.x;
            vertexNormal[i * 3 + 1] = normal.y;
            vertexNormal[i * 3 + 2] = normal.z;
        }
    }
}
