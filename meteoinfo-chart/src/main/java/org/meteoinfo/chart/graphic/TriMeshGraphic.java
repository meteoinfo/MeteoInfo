package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TriMeshGraphic extends MeshGraphic {

    protected Logger logger = LoggerFactory.getLogger("TriMeshGraphic");
    protected boolean normalLoaded = false;

    /**
     * Constructor
     */
    public TriMeshGraphic() {
        super();

        faceInterp = true;
        edgeInterp = false;
        mesh = false;
        normalLoaded = false;
        singleLegend = true;
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
    public void setTriangles(List<Triangle> triangles) {
        LinkedHashMap<Vector3f, Integer> map = new LinkedHashMap<Vector3f, Integer>();
        int n = triangles.size();
        this.vertexIndices = new int[n * 3];
        Vector3f vector3f;
        int idx = 0, vertexIdx = 0, index;
        List<Integer> idxList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Triangle triangle = triangles.get(i);
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
     * @param normal Normal array
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
        this.extent = new Extent(minX, maxX, minY, maxY, minZ, maxZ);

        this.normalLoaded = true;

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

    @Override
    public void updateVertexTexture() {

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

    @Override
    public int getFaceNumber() {
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

    /**
     * Get a triangle
     * @param vData Vertex array
     * @param idx Triangle index
     * @return The triangle
     */
    public Triangle getTriangle(float[] vData, int idx) {
        Vector3f a = getVertex(vData, vertexIndices[idx * 3]);
        Vector3f b = getVertex(vData, vertexIndices[idx * 3 + 1]);
        Vector3f c = getVertex(vData, vertexIndices[idx * 3 + 2]);

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
            triangles.add(getTriangle(vData, i));
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

    @Override
    public void calculateNormalVectors(float[] vData) {
        if (this.vertexNormal != null) {
            return;
        }

        int nVertex = getVertexNumber();
        vertexNormal = new float[vData.length];

        // Index-based accumulator: accum[i][0..2] holds the summed normal for vertex i.
        // Replaces the broken HashMap<Vector3f, Vector3f> approach. Using vertex indices
        // as keys guarantees correct aggregation regardless of object identity.
        float[][] accum = new float[nVertex][3];

        int faceCount = getFaceNumber();

        // Reusable scratch vectors to avoid allocating ~3*nFace Vector3f objects
        // inside the hot loop. These are overwritten each iteration.
        Vector3f edgeAB = new Vector3f();
        Vector3f edgeAC = new Vector3f();
        Vector3f faceN = new Vector3f();

        for (int f = 0; f < faceCount; f++) {
            // Read triangle vertex indices from the index buffer
            int ia = vertexIndices[f * 3];
            int ib = vertexIndices[f * 3 + 1];
            int ic = vertexIndices[f * 3 + 2];

            // Sample vertex positions directly from the flat array.
            // No getVertex() calls → zero intermediate Vector3f allocation.
            float ax = vData[ia * 3],     ay = vData[ia * 3 + 1], az = vData[ia * 3 + 2];
            float bx = vData[ib * 3],     by = vData[ib * 3 + 1], bz = vData[ib * 3 + 2];
            float cx = vData[ic * 3],     cy = vData[ic * 3 + 1], cz = vData[ic * 3 + 2];

            // Compute edge vectors originating from vertex A
            edgeAB.set(bx - ax, by - ay, bz - az);
            edgeAC.set(cx - ax, cy - ay, cz - az);

            // Cross product: (B-A) × (C-A). Direction follows right-hand rule
            // assuming counter-clockwise winding order.
            edgeAB.cross(edgeAC, faceN);

            // Normalize BEFORE accumulation. Unnormalized cross product has magnitude
            // proportional to triangle area; without this step, larger triangles would
            // disproportionately bias the averaged vertex normal.
            float lenSq = faceN.lengthSquared();
            if (lenSq > 1e-12f) {
                faceN.normalize();
            } else {
                // Degenerate triangle (collinear or coincident vertices).
                // Skip entirely rather than injecting a zero/NaN vector into the sum.
                continue;
            }

            // Accumulate the unit face normal into all three vertex slots.
            // Each shared vertex receives one contribution per adjacent face.
            for (int idx : new int[]{ia, ib, ic}) {
                accum[idx][0] += faceN.x;
                accum[idx][1] += faceN.y;
                accum[idx][2] += faceN.z;
            }
        }

        // Final pass: normalize each accumulated vertex normal to unit length
        // and write into the output vertexNormal array.
        for (int i = 0; i < nVertex; i++) {
            float nx = accum[i][0];
            float ny = accum[i][1];
            float nz = accum[i][2];
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

            if (len > 1e-8f) {
                // Standard case: normalize the weighted average to unit length
                vertexNormal[i * 3]     = nx / len;
                vertexNormal[i * 3 + 1] = ny / len;
                vertexNormal[i * 3 + 2] = nz / len;
            } else {
                // Isolated vertex or all adjacent faces were degenerate.
                // Assign a safe upward default to prevent NaN propagation
                // through the lighting pipeline.
                vertexNormal[i * 3]     = 0f;
                vertexNormal[i * 3 + 1] = 1f;
                vertexNormal[i * 3 + 2] = 0f;
            }
        }
    }

    /**
     * Given the vertex coordinates of a shape this function calculates the
     * normal vector coordinates.
     */
    public void calculateNormalVectors_bak(float[] vData) {
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
