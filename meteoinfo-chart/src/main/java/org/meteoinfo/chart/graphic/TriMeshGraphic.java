package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendManage;
import org.meteoinfo.geometry.colors.TransferFunction;
import org.meteoinfo.geometry.graphic.GraphicCollection3D;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TriMeshGraphic extends Graphic {

    protected Logger logger = LoggerFactory.getLogger("TriMeshGraphic");
    protected float[] vertexPosition;
    protected float[] vertexValue;
    protected float[] vertexColor;
    protected float[] vertexNormal;
    protected int[] vertexIndices;
    //private LinkedHashMap<Integer, List<Integer>> triangleMap;
    protected boolean faceInterp;
    protected boolean edgeInterp;
    protected boolean mesh;
    protected boolean normalLoaded = false;
    protected Extent extent;
    protected LegendScheme legendScheme;
    protected boolean singleLegend = true;
    protected ColorBreak legendBreak;

    /**
     * Constructor
     */
    public TriMeshGraphic() {
        super();

        faceInterp = true;
        edgeInterp = false;
        mesh = false;
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
     * Set vertex position
     * @param value Vertex position
     */
    public void setVertexPosition(float[] value) {
        vertexPosition = value;
        updateExtent();
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
     * Get vertex indices
     * @return Vertex indices
     */
    public int[] getVertexIndices() {
        return this.vertexIndices;
    }

    /**
     * Set vertex indices
     * @param value Vertex indices
     */
    public void setVertexIndices(int[] value) {
        this.vertexIndices = value;
    }

    /**
     * Get vertex color data
     * @return Vertex color data
     */
    public float[] getVertexColor() {
        return vertexColor;
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
     * Get if is mesh
     * @return Boolean
     */
    public boolean isMesh() {
        return this.mesh;
    }

    /**
     * Set if is mesh
     * @param value Boolean
     */
    public void setMesh(boolean value) {
        this.mesh = value;
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
     * Set triangles
     * @param vertexData The triangle vertex array
     */
    public void setTriangles(float[] vertexes) {
        LinkedHashMap<Vector3f, Integer> map = new LinkedHashMap<Vector3f, Integer>();
        int n = vertexes.length / 3;
        this.vertexIndices = new int[n];
        Vector3f vector3f;
        int idx = 0, vertexIdx = 0, triangleIdx = 0, index, ii;
        List<Integer> idxList = new ArrayList<>();
        for (int i = 0; i < n / 3; i++) {
            for (int j = 0; j < 3; j++) {
                ii = i * 9 + j * 3;
                vector3f = new Vector3f(vertexes[ii], vertexes[ii + 1], vertexes[ii + 2]);
                if (map.containsKey(vector3f)) {
                    index = map.get(vector3f);
                    vertexIndices[vertexIdx] = index;
                } else {
                    vertexIndices[vertexIdx] = idx;
                    map.put(vector3f, idx++);
                }

                vertexIdx += 1;
            }
            triangleIdx += 1;
        }

        this.vertexPosition = new float[map.size() * 3];
        idx = 0;
        for (Map.Entry<Vector3f, Integer> entry : map.entrySet()) {
            vector3f = entry.getKey();
            vertexPosition[idx++] = vector3f.x;
            vertexPosition[idx++] = vector3f.y;
            vertexPosition[idx++] = vector3f.z;
        }

        updateExtent();
    }

    /**
     * Set triangles
     * @param vertexData The triangle vertex array
     */
    public void setTriangles(List<Triangle3D> triangles) {
        LinkedHashMap<Vector3f, Integer> map = new LinkedHashMap<Vector3f, Integer>();
        int n = triangles.size();
        this.vertexIndices = new int[n];
        Vector3f vector3f;
        int idx = 0, vertexIdx = 0, triangleIdx = 0, index, ii;
        List<Integer> idxList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Triangle3D triangle = triangles.get(i);
            for (int j = 0; j < 3; j++) {
                vector3f = new Vector3f(triangle.getPoint(j));
                if (map.containsKey(vector3f)) {
                    index = map.get(vector3f);
                    vertexIndices[vertexIdx] = index;
                } else {
                    vertexIndices[vertexIdx] = idx;
                    map.put(vector3f, idx++);
                }

                vertexIdx += 1;
            }
            triangleIdx += 1;
        }

        this.vertexPosition = new float[map.size() * 3];
        idx = 0;
        for (Map.Entry<Vector3f, Integer> entry : map.entrySet()) {
            vector3f = entry.getKey();
            vertexPosition[idx++] = vector3f.x;
            vertexPosition[idx++] = vector3f.y;
            vertexPosition[idx++] = vector3f.z;
        }

        updateExtent();
    }

    /**
     * Set triangles
     * @param vertexData The triangle vertex
     * @param faceIndices The triangle face indices
     */
    public void setTriangles(Array vertexes, Array faceIndices) {
        vertexes = vertexes.copyIfView();
        faceIndices = faceIndices.copyIfView();

        this.vertexIndices = (int[]) faceIndices.getStorage();

        this.vertexPosition = (float[]) vertexes.getStorage();

        updateExtent();
    }

    /**
     * Set triangles
     * @param faceIndices The triangle face indices
     * @param x X coordinate array
     * @param y Y coordinate array
     * @param z Z coordinate array
     */
    public void setTriangles(Array faceIndices, Array x, Array y, Array z) {
        logger.info("Start set triangles...");

        x = x.copyIfView();
        y = y.copyIfView();
        z = z.copyIfView();
        faceIndices = faceIndices.copyIfView();

        this.vertexIndices = (int[]) faceIndices.getStorage();

        int n = x.getShape()[0];
        this.vertexPosition = new float[n * 3];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            vertexPosition[idx] = x.getFloat(i);
            vertexPosition[idx + 1] = y.getFloat(i);
            vertexPosition[idx + 2] = z.getFloat(i);
            idx += 3;
        }

        updateExtent();

        logger.info("Set triangles finished!");
    }

    /**
     * Set triangles
     * @param faceIndices The triangle face indices
     * @param x X coordinate array
     * @param y Y coordinate array
     * @param z Z coordinate array
     */
    public void setTriangles(Array faceIndices, Array x, Array y, Array z, Array normal) {
        logger.info("Start set triangles...");

        x = x.copyIfView();
        y = y.copyIfView();
        z = z.copyIfView();
        normal = normal.copyIfView();
        faceIndices = faceIndices.copyIfView();

        int vertexIdx = 0;
        int nFace = faceIndices.getShape()[0];
        this.vertexIndices = (int[]) faceIndices.getStorage();
        /*for (int i = 0; i < nFace; i++) {
            for (int j = 0; j < 3; j++) {
                vertexIndices[vertexIdx] = faceIndices.getInt(i * 3 + j) - 1;
                vertexIdx += 1;
            }
        }*/

        logger.info("Set vertex position and normal...");
        int n = x.getShape()[0];
        this.vertexPosition = new float[n * 3];
        this.vertexNormal = (float[]) normal.getStorage();
        int idx = 0;
        float xx, yy, zz;
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE, minY = minX, maxY = maxX,
                minZ = minX, maxZ = maxX;
        for (int i = 0; i < n; i++) {
            xx = x.getFloat(i);
            yy = y.getFloat(i);
            zz = z.getFloat(i);
            vertexPosition[idx] = xx;
            vertexPosition[idx + 1] = yy;
            vertexPosition[idx + 2] = zz;
            idx += 3;
            if (minX > xx)
                minX = xx;
            if (maxX < xx)
                maxX = xx;
            if (minY > yy)
                minY = yy;
            if (maxY < yy)
                maxY = yy;
            if (minZ > zz)
                minZ = zz;
            if (maxZ < zz)
                maxZ = zz;
        }
        this.extent = new Extent3D(minX, maxX, minY, maxY, minZ, maxZ);

        this.normalLoaded = true;

        //updateExtent();

        logger.info("Set triangles finished!");
    }

    /**
     * Set triangles
     * @param vertexData The triangle vertex array
     * @param cData Color data array
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     */
    public void setTriangles(float[] vertexes, Array cData, Array xa, Array ya, Array za) {
        LinkedHashMap<Vector3f, Integer> map = new LinkedHashMap<Vector3f, Integer>();
        int n = vertexes.length / 3;
        this.vertexIndices = new int[n];
        Vector3f vector3f;
        int idx = 0, vertexIdx = 0, triangleIdx = 0, index, ii;
        List<Integer> idxList = new ArrayList<>();
        for (int i = 0; i < n / 3; i++) {
            for (int j = 0; j < 3; j++) {
                ii = i * 9 + j * 3;
                vector3f = new Vector3f(vertexes[ii], vertexes[ii + 1], vertexes[ii + 2]);
                if (map.containsKey(vector3f)) {
                    index = map.get(vector3f);
                    vertexIndices[vertexIdx] = index;
                } else {
                    vertexIndices[vertexIdx] = idx;
                    map.put(vector3f, idx++);
                }

                vertexIdx += 1;
            }
            triangleIdx += 1;
        }

        this.vertexPosition = new float[map.size() * 3];
        this.vertexValue = new float[map.size()];
        Index cIndex = cData.getIndex();
        idx = 0;
        int xi, yi, zi;
        for (Map.Entry<Vector3f, Integer> entry : map.entrySet()) {
            vector3f = entry.getKey();
            vertexPosition[idx * 3] = vector3f.x;
            vertexPosition[idx * 3 + 1] = vector3f.y;
            vertexPosition[idx * 3 + 2] = vector3f.z;
            xi = ArrayUtil.searchSorted(xa, vector3f.x, true);
            yi = ArrayUtil.searchSorted(ya, vector3f.y, true);
            zi = ArrayUtil.searchSorted(za, vector3f.z, true);
            cIndex.set(zi, yi, xi);
            vertexValue[idx] = cData.getFloat(cIndex);
            idx += 1;
        }

        updateExtent();
    }

    /**
     * Set triangles
     * @param vertexData The triangle vertex array
     * @param cData Color data array
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     */
    public void setVertexValue(Array cData, Array xa, Array ya, Array za) {
        int n = getVertexNumber();
        this.vertexValue = new float[n];
        Index cIndex = cData.getIndex();
        int xi, yi, zi;
        float x, y, z;
        for (int i = 0; i < n; i++) {
            x = vertexPosition[i * 3];
            y = vertexPosition[i * 3 + 1];
            z = vertexPosition[i * 3 + 2];
            xi = ArrayUtil.searchSorted(xa, x, true);
            yi = ArrayUtil.searchSorted(ya, y, true);
            zi = ArrayUtil.searchSorted(za, z, true);
            cIndex.set(zi, yi, xi);
            vertexValue[i] = cData.getFloat(cIndex);
        }
    }

    /**
     * Get extent
     *
     * @return The extent
     */
    @Override
    public Extent getExtent() {
        return extent;
    }

    /**
     * Set extent
     *
     * @param value Extent
     */
    @Override
    public void setExtent(Extent value) {
        this.extent = value;
    }

    /**
     * Get legend scheme
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        return this.legendScheme;
    }

    /**
     * Set legend scheme
     * @param ls Legend scheme
     */
    public void setLegendScheme(LegendScheme ls) {
        this.legendScheme = ls;
        updateVertexColor();
    }

    /**
     * Get is single legend or not
     * @return Boolean
     */
    public boolean isSingleLegend() {
        return this.singleLegend;
    }

    /**
     * Set single legend or not
     * @param value Boolean
     */
    public void setSingleLegend(boolean value) {
        this.singleLegend = value;
    }

    /**
     * Get legend break
     *
     * @return Legend break
     */
    public ColorBreak getLegendBreak() {
        return this.legendBreak;
    }

    /**
     * Set legend break
     *
     * @param value Legend break
     */
    public void setLegendBreak(ColorBreak value) {
        this.legendBreak = value;
    }

    /**
     * Set transfer function
     * @param transferFunction Transfer function
     */
    public void setTransferFunction(TransferFunction transferFunction) {
        if (vertexValue != null) {
            vertexColor = new float[getVertexNumber() * 4];
            float[] color;
            for (int i = 0; i < vertexValue.length; i++) {
                color = transferFunction.getColor(vertexValue[i]).getRGBComponents(null);
                System.arraycopy(color, 0, vertexColor, i * 4, 4);
            }
        }

        LegendScheme ls = LegendManage.createLegendScheme(transferFunction);
        this.legendScheme = ls;
        this.setSingleLegend(false);
    }

    /**
     * Update vertex color data
     */
    public void updateVertexColor() {
        if (legendScheme != null) {
            int n = getVertexNumber();
            vertexColor = new float[n * 4];
            if (vertexValue != null) {
                float[] color;
                for (int i = 0; i < n; i++) {
                    color = legendScheme.findLegendBreak(vertexValue[i]).getColor().getRGBComponents(null);
                    System.arraycopy(color, 0, vertexColor, i * 4, 4);
                }
            } else {
                float[] color = legendScheme.getLegendBreak(0).getColor().getRGBComponents(null);
                for (int i = 0; i < n; i++) {
                    System.arraycopy(color, 0, vertexColor, i * 4, 4);
                }
            }
        }
    }

    /**
     * Get vertex number
     * @return Vertex number
     */
    public int getVertexNumber() {
        return vertexPosition.length / 3;
    }

    /**
     * Get triangle number
     * @return Triangle number
     */
    public int getTriangleNumber() {
        return this.vertexIndices.length / 3;
    }

    /**
     * Get color
     * @return The color
     */
    public Color getColor() {
        return this.legendScheme.getLegendBreak(0).getColor();
    }

    /**
     * Set color
     * @param color The color
     */
    public void setColor(Color color) {
        this.legendScheme.getLegendBreak(0).setColor(color);
        updateVertexColor();
    }

    public void updateExtent() {
        float x, y, z;
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE, minY = minX, maxY = maxX,
                minZ = minX, maxZ = maxX;
        for (int i = 0; i < vertexPosition.length; i+=3) {
            x = vertexPosition[i];
            y = vertexPosition[i + 1];
            z = vertexPosition[i + 2];
            if (minX > x)
                minX = x;
            if (maxX < x)
                maxX = x;
            if (minY > y)
                minY = y;
            if (maxY < y)
                maxY = y;
            if (minZ > z)
                minZ = z;
            if (maxZ < z)
                maxZ = z;
        }

        this.extent = new Extent3D(minX, maxX, minY, maxY, minZ, maxZ);
    }

    /**
     * Get a triangle
     * @param vData Vertex array
     * @param idx Triangle index
     * @return The triangle
     */
    public Triangle3D getTriangle(float[] vData, int idx) {
        Vector3f a = getVertex(vData, vertexIndices[idx * 3]);
        Vector3f b = getVertex(vData, vertexIndices[idx * 3 + 1]);
        Vector3f c = getVertex(vData, vertexIndices[idx * 3 + 2]);

        return new Triangle3D(a, b, c);
    }

    /**
     * Get a triangle
     * @param idx Triangle index
     * @return The triangle
     */
    public Triangle3D getTriangle(int idx) {
        return getTriangle(vertexPosition, idx);
    }

    /**
     * Get all triangles
     * @param vData Vertex array
     * @return All triangles
     */
    public List<Triangle3D> getTriangles(float[] vData) {
        int n = getTriangleNumber();
        List<Triangle3D> triangles = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            triangles.add(getTriangle(i));
        }

        return triangles;
    }

    /**
     * Get all triangles
     * @return All triangles
     */
    public List<Triangle3D> getTriangles() {
        return getTriangles(this.vertexPosition);
    }

    /**
     * Given the vertex coordinates of a shape this function calculates the
     * normal vector coordinates.
     *//*
    public void calculateNormalVectors_bak(float[] vData) {
        if (this.normalLoaded)
            return;

        List<Triangle3D> triangles = getTriangles(vData);
        int nVertex = getVertexNumber();
        vertexNormal = new float[vData.length];
        Vector3f vertex, normal;
        List<Integer> indexes;
        Triangle3D triangle;
        for (int i = 0; i < nVertex; i++) {
            vertex = getVertex(i);
            indexes = triangleMap.get(i);
            normal = new Vector3f();
            for (int idx : indexes) {
                triangle = triangles.get(idx);
                normal.add(triangle.getNormal(vertex));
            }
            normal.normalize();
            normal.negate();
            vertexNormal[i * 3] = normal.x;
            vertexNormal[i * 3 + 1] = normal.y;
            vertexNormal[i * 3 + 2] = normal.z;
        }
    }*/

    /**
     * Given the vertex coordinates of a shape this function calculates the
     * normal vector coordinates.
     */
    public void calculateNormalVectors(float[] vData) {
        if (this.vertexNormal != null)
            return;

        List<Triangle3D> triangles = getTriangles(vData);
        int nVertex = getVertexNumber();
        vertexNormal = new float[vData.length];
        Vector3f vertex, normal;
        List<Integer> indexes;
        HashMap<Vector3f, Vector3f> normalMap = new LinkedHashMap<>();
        for (Triangle3D triangle : triangles) {
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
