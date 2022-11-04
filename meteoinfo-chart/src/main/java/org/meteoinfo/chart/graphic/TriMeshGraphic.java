package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.locationtech.jts.triangulate.tri.Tri;
import org.meteoinfo.chart.jogl.JOGLUtil;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TriMeshGraphic extends GraphicCollection3D {
    private float[] vertexPosition;
    private float[] vertexValue;
    private float[] vertexColor;
    private float[] vertexNormal;
    private int[] vertexIndices;
    private List<List<Integer>> triangleMap;
    private boolean faceInterp;
    private boolean edgeInterp;
    private boolean mesh;

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
        //calculateNormalVectors(vertexPosition);
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
        triangleMap = new ArrayList<>();
        List<Integer> idxList = new ArrayList<>();
        for (int i = 0; i < n / 3; i++) {
            for (int j = 0; j < 3; j++) {
                ii = i * 9 + j * 3;
                vector3f = new Vector3f(vertexes[ii], vertexes[ii + 1], vertexes[ii + 2]);
                if (map.containsKey(vector3f)) {
                    index = map.get(vector3f);
                    vertexIndices[vertexIdx] = index;
                    idxList = triangleMap.get(index);
                    idxList.add(triangleIdx);
                    triangleMap.set(index, idxList);
                } else {
                    vertexIndices[vertexIdx] = idx;
                    idxList = new ArrayList<>();
                    idxList.add(triangleIdx);
                    triangleMap.add(idxList);
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
        triangleMap = new ArrayList<>();
        List<Integer> idxList = new ArrayList<>();
        for (int i = 0; i < n / 3; i++) {
            for (int j = 0; j < 3; j++) {
                ii = i * 9 + j * 3;
                vector3f = new Vector3f(vertexes[ii], vertexes[ii + 1], vertexes[ii + 2]);
                if (map.containsKey(vector3f)) {
                    index = map.get(vector3f);
                    vertexIndices[vertexIdx] = index;
                    idxList = triangleMap.get(index);
                    idxList.add(triangleIdx);
                    triangleMap.set(index, idxList);
                } else {
                    vertexIndices[vertexIdx] = idx;
                    idxList = new ArrayList<>();
                    idxList.add(triangleIdx);
                    triangleMap.add(idxList);
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
            xi = ArrayUtil.searchSorted(xa, vector3f.x);
            yi = ArrayUtil.searchSorted(ya, vector3f.y);
            zi = ArrayUtil.searchSorted(za, vector3f.z);
            cIndex.set(zi, yi, xi);
            vertexValue[idx] = cData.getFloat(cIndex);
            idx += 1;
        }

        updateExtent();
    }

    @Override
    public void setLegendScheme(LegendScheme ls) {
        super.setLegendScheme(ls);
        updateVertexColor();
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
        int idx = 0;
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

        this._extent = new Extent3D(minX, maxX, minY, maxY, minZ, maxZ);
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
     */
    public void calculateNormalVectors(float[] vData) {
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
    }

    /**
     * Given the vertex coordinates of a shape this function calculates the
     * normal vector coordinates.
     */
    public void calculateNormalVectors_bak(float[] vData) {
        // Temporary storage for the normal vector coordinates
        // there's one temporary array for each dimension.
        int n = vData.length / 3;
        float[] nx = new float[n];
        float[] ny = new float[n];
        float[] nz = new float[n];

        // Temporary storage for the vertex coordinates
        // there's one temporary array for each dimension.
        float[] x = new float[n];
        float[] y = new float[n];
        float[] z = new float[n];

        // Load the coordinate values into their respective arrays.
        int counter = 0;
        for (int i = 0; i < n; i++) {
            x[i] = vData[counter];
            counter++;
            y[i] = vData[counter];
            counter++;
            z[i] = vData[counter];
            counter++;
        }

        // Compute normals for each vertex.
        for (int i = 0; i < n; i += 3) {
            float[] normal = JOGLUtil.normalize(new float[]{x[i], y[i], z[i]}, new float[]{x[i+1], y[i+1], z[i+1]},
                    new float[]{x[i+2], y[i+2], z[i+2]});

            nx[i] += normal[0];
            ny[i] += normal[1];
            nz[i] += normal[2];

            nx[i + 1] += normal[0];
            ny[i + 1] += normal[1];
            nz[i + 1] += normal[2];

            nx[i + 2] += normal[0];
            ny[i + 2] += normal[1];
            nz[i + 2] += normal[2];
        }

        // Copy the data for the normal vectors from the temporary arrays into
        // the permanent normalArray vector.
        vertexNormal = new float[n * 3];
        counter = 0;
        for (int i = 0; i < n; i++) {
            vertexNormal[counter] = nx[i];
            counter++;
            vertexNormal[counter] = ny[i];
            counter++;
            vertexNormal[counter] = nz[i];
            counter++;
        }
    }
}
