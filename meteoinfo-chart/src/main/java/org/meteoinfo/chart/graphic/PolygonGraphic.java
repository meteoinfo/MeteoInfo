package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.tessellator.TriangleTessellator;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.LinearRing;
import org.meteoinfo.geometry.Polygon;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PolygonGraphic extends Graphic implements PolygonalGraphic {

    private Array xData;
    private Array yData;
    private Array zData;
    private Array cData;
    protected float[] vertexPosition;
    protected float[] vertexColor; 
    protected float[] vertexNormal;
    protected int[] vertexIndices;
    private float[] lineVertex;
    private List<Integer> linePointNumbers;
    protected boolean faceInterp;
    protected boolean edgeInterp;
    protected boolean tessellateDirty = true;

    /**
     * Constructor
     * @param polygon Polygon geometry
     * @param legend Polygon legend break
     */
    public PolygonGraphic(Polygon polygon, PolygonBreak legend) {
        this.geometry = polygon;
        this.legendBreak = legend;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param zData Z data
     * @param polygonBreak Polygon break
     */
    public PolygonGraphic(Array xData, Array yData, Array zData, PolygonBreak polygonBreak) {
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;

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
     * @param polygonBreak Polygon break
     */
    public PolygonGraphic(Array xData, Array yData, PolygonBreak polygonBreak) {
        this(xData, yData, null, polygonBreak);
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
     * @param zData Z data
     * @param cData Color data
     * @param legendScheme Legend scheme
     */
    public PolygonGraphic(Array xData, Array yData, Array zData, Array cData, LegendScheme legendScheme) {
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;
        this.cData = cData;

        updateShapeLegend(legendScheme);
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param cData Color data
     * @param legendScheme Legend scheme
     */
    public PolygonGraphic(Array xData, Array yData, Array cData, LegendScheme legendScheme) {
        this(xData, yData, null, cData, legendScheme);
    }

    protected void updateShape() {
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        double x, y, z;
        if (zData == null) {
            while (xIter.hasNext() && yIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                points.add(new Coordinate(x, y));
            }
        } else {
            IndexIterator zIter = this.zData.getIndexIterator();
            while (xIter.hasNext() && yIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                z = zIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                points.add(new Coordinate(x, y, z));
            }
        }

        if (this.geometry == null) {
            this.geometry = new Polygon();
        }
        this.geometry.setCoordinates(points);
    }

    protected void updateShapeLegend(LegendScheme legendScheme) {
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        IndexIterator cIter = this.cData.getIndexIterator();
        ColorBreakCollection cbc = new ColorBreakCollection();
        ColorBreak cb;
        double x, y, z, c;
        if (zData == null) {
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
        } else {
            IndexIterator zIter = zData.getIndexIterator();
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                z = zIter.getDoubleNext();
                c = cIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                points.add(new Coordinate(x, y, z));
                cb = legendScheme.findLegendBreakAlways(c);
                cbc.add(cb);
            }
        }

        if (this.geometry == null) {
            this.geometry = new Polygon();
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
     * Get z data
     * @return Z data
     */
    public Array getZData() {
        return this.zData;
    }

    /**
     * Set z data
     * @param zData Z data
     */
    public void setZData(Array zData) {
        this.zData = zData;
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
     * Set data
     * @param xData X data
     * @param yData Y data
     * @param zData Z data
     */
    public void setData(Array xData, Array yData, Array zData) {
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;
        updateShape();
    }

    /**
     * Get if tessellate dirty
     * @return Tessellate dirty or not
     */
    @Override
    public boolean isTessellateDirty() {
        return this.tessellateDirty;
    }

    /**
     * Get vertex position
     * @return Vertex position
     */
    @Override
    public float[] getVertexPosition() {
        return vertexPosition;
    }

    @Override
    public void setVertexPosition(float[] vertexPosition) {
        this.vertexPosition = vertexPosition;
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
     * Get vertex color data
     * @return Vertex color data
     */
    @Override
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
    @Override
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
     * Get vertex indices
     * @return Vertex indices
     */
    @Override
    public int[] getVertexIndices() {
        return this.vertexIndices;
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
    @Override
    public int getVertexNumber() {
        return vertexPosition.length / 3;
    }

    /**
     * Get face number
     * @return Face number
     */
    public int getFaceNumber() {
        return this.vertexIndices.length / 3;
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
            triangles.add(getTriangle(vData, i));
        }

        return triangles;
    }

    /**
     * Get all triangles
     * @return All triangles
     */
    public List<Triangle> getTriangles() {
        int n = getFaceNumber();
        List<Triangle> triangles = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            triangles.add(getTriangle(i));
        }

        return triangles;
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

    @Override
    public float[] getLineVertex() {
        return this.lineVertex;
    }

    @Override
    public void setLineVertex(float[] vData) {
        this.lineVertex = vData;
    }

    @Override
    public List<Integer> getLinePointNumbers() {
        return this.linePointNumbers;
    }

    /**
     * Builds deduplicated vertex arrays (position, indices, color) from tessellated triangles.
     * Vertices sharing both spatial position AND color are merged into a single index.
     *
     * @param triangles Per-polygon triangle lists produced by the tessellator.
     * @return Object array: [0]=float[] positions, [1]=int[] indices, [2]=float[] colors.
     */
    public Object[] getVertices(List<Triangle> triangles) {

        // --- Phase 1: Estimate capacity to minimize rehashing and reallocation ---

        int estimatedTriangles = triangles.size();

        // Heuristic: shared edges/corners mean ~1.5 unique vertices per triangle
        // on average. Over-estimating slightly avoids early rehashes.
        int initialCapacity = Math.max(16, (int) (estimatedTriangles * 1.5f));

        // accessOrder=false maintains insertion order (needed for deterministic output).
        // Pre-sized capacity avoids costly rehash during the main insertion loop.
        LinkedHashMap<ColoredVector3f, Integer> map =
                new LinkedHashMap<>(initialCapacity, 0.75f, false);

        // Allocate output buffers based on estimate; will grow if exceeded.
        float[] vertexPosition = new float[initialCapacity * 3];
        float[] vertexColor    = new float[initialCapacity * 4];
        int[]   vertexIndices  = new int[estimatedTriangles * 3];

        int uniqueVertexCount = 0;  // Number of unique vertices written so far
        int indexPtr          = 0;  // Write cursor into vertexIndices array

        // --- Phase 2: Reusable scratch objects to eliminate hot-loop allocations ---
        // Original created new Vector3f + new ColoredVector3f for EVERY triangle corner.
        // For N triangles this meant 6N heap allocations. Reusing temporaries reduces
        // this to only 1 allocation per genuinely NEW unique vertex (for the map key).
        Vector3f      tempVec = new Vector3f();
        ColoredVector3f tempKey = new ColoredVector3f(); // Assumes mutable set() method

        // --- Phase 3: Single-pass deduplication + incremental output population ---
        Color color = this.legendBreak.getColor();
        float[] colors = color.getRGBComponents(null);

        for (Triangle triangle : triangles) {
            for (int j = 0; j < 3; j++) {

                // Populate reusable vector instead of: new Vector3f(triangle.getPoint(j))
                tempVec.set(triangle.getPoint(j));
                tempKey.set(tempVec, color);

                // OPTIMIZATION #1: Single get() + null check replaces:
                //   if (map.containsKey(cv)) { index = map.get(cv); }
                // This halves the hash computation and equals() calls per corner.
                Integer existingIndex = map.get(tempKey);

                if (existingIndex != null) {
                    // Vertex already exists; just record its index
                    vertexIndices[indexPtr++] = existingIndex;
                } else {
                    // --- New unique vertex discovered ---

                    // OPTIMIZATION #4: Grow buffers on demand instead of
                    // pre-allocating worst-case nv*3. Amortized O(1) per vertex.
                    if (uniqueVertexCount >= vertexPosition.length / 3) {
                        int newCap = vertexPosition.length * 2;
                        vertexPosition = Arrays.copyOf(vertexPosition, newCap);
                        vertexColor    = Arrays.copyOf(vertexColor, (newCap / 3) * 4);
                    }

                    // Record index mapping
                    vertexIndices[indexPtr++] = uniqueVertexCount;

                    // IMPORTANT: Must create a NEW instance for the map key because
                    // tempKey/tempVec are overwritten on the next iteration.
                    // This is the ONLY allocation inside the hot loop.
                    map.put(new ColoredVector3f(tempVec, color), uniqueVertexCount);

                    // OPTIMIZATION #3: Write directly to output arrays NOW.
                    // Original deferred this to a secondary Map.entrySet() loop
                    // and maintained a separate List<float[]> colorList.
                    // Incremental writing eliminates both overheads entirely.
                    int posOff = uniqueVertexCount * 3;
                    vertexPosition[posOff]     = tempVec.x;
                    vertexPosition[posOff + 1] = tempVec.y;
                    vertexPosition[posOff + 2] = tempVec.z;

                    int colOff = uniqueVertexCount * 4;
                    System.arraycopy(colors, 0, vertexColor, colOff, 4);

                    uniqueVertexCount++;
                }
            }
        }

        // --- Phase 4: Trim arrays to exact size for GPU upload ---
        // Dynamic growth may leave buffers 0-50% oversized. Trimming ensures the
        // renderer receives tightly packed data with no wasted VRAM bandwidth.
        if (uniqueVertexCount < vertexPosition.length / 3) {
            vertexPosition = Arrays.copyOf(vertexPosition, uniqueVertexCount * 3);
            vertexColor    = Arrays.copyOf(vertexColor, uniqueVertexCount * 4);
        }
        if (indexPtr < vertexIndices.length) {
            vertexIndices = Arrays.copyOf(vertexIndices, indexPtr);
        }

        return new Object[]{vertexPosition, vertexIndices, vertexColor};
    }

    /**
     * Update outline vertex arrayh
     */
    @Override
    public void updateLineVertex() {
        int n = this.getNumPoints();
        lineVertex = new float[n * 3];
        linePointNumbers = new ArrayList<>();
        int idx = 0;
        Polygon p = (Polygon) this.geometry;
        linePointNumbers.add(p.getExteriorRing().getNumPoints());
        for (Coordinate c : p.getExteriorRing().getCoordinates()) {
            lineVertex[idx++] = (float) c.x;
            lineVertex[idx++] = (float) c.y;
            lineVertex[idx++] = (float) c.z;
        }
        for (int j = 0; j < p.getNumInteriorRing(); j++) {
            LinearRing lr = (LinearRing) p.getInteriorRingN(j);
            linePointNumbers.add(lr.getNumPoints());
            for (Coordinate c : lr.getCoordinates()) {
                lineVertex[idx++] = (float) c.x;
                lineVertex[idx++] = (float) c.y;
                lineVertex[idx++] = (float) c.z;
            }
        }
    }

    /**
     * Get is fill polyogns or not
     * @return Fill polygons or not
     */
    @Override
    public boolean isFill() {
        PolygonBreak polygonBreak = (PolygonBreak) this.getLegendBreak();
        return polygonBreak.isDrawFill();
    }

    /**
     * Get is draw outline or not
     * @return Draw outline or not
     */
    @Override
    public boolean isDrawOutline() {
        PolygonBreak polygonBreak = (PolygonBreak) this.getLegendBreak();
        return polygonBreak.isDrawOutline();
    }

    @Override
    public void tessellate(boolean subdivide) {
        if (subdivide) {
            List<Triangle> ts1 = TriangleTessellator.tessellate(this);
            List<Triangle> triangles = new ArrayList<>();
            for (Triangle triangle : ts1) {
                triangles.addAll(TriangleTessellator.subdivideEquilateral(triangle, 3.0f));
            }

            Object[] r = getVertices(triangles);
            this.vertexPosition = (float[]) r[0];
            this.vertexIndices = (int[]) r[1];
            this.vertexColor = (float[]) r[2];
        } else {
            List<Triangle> triangles = TriangleTessellator.tessellate((PolygonGraphic) this);

            Object[] r = getVertices(triangles);
            this.vertexPosition = (float[]) r[0];
            this.vertexIndices = (int[]) r[1];
            this.vertexColor = (float[]) r[2];
        }
        this.tessellateDirty = false;
    }

    /**
     * Update vertex color data
     */
    public void updateVertexColor() {
        int vn = getVertexNumber();
        vertexColor = new float[vn * 4];
        float[] color = this.legendBreak.getColor().getRGBComponents(null);
        for (int i = 0; i < vn; i++) {
            System.arraycopy(color, 0, vertexColor, i * 4, 4);
        }
    }

    @Override
    public void updateExtent() {
        int vn = getVertexNumber();
    }
}
