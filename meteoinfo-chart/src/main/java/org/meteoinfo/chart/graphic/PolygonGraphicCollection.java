package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.tessellator.TessPolygon;
import org.meteoinfo.chart.jogl.tessellator.TriangleTessellator;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.LinearRing;
import org.meteoinfo.geometry.MultiPolygon;
import org.meteoinfo.geometry.Polygon;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PolygonGraphicCollection extends GraphicCollection implements PolygonalGraphic {

    private Array cData;
    private List<Array> data;
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
     */
    public PolygonGraphicCollection() {
        this(new ArrayList<PolygonGraphic>());
    }

    /**
     * Constructor
     * @param graphics Graphics
     */
    public PolygonGraphicCollection(List<PolygonGraphic> graphics) {
        super();
        this.graphics = graphics;
    }

    /**
     * Constructor
     * @param data Data list
     * @param lineBreak Polyline break
     */
    public PolygonGraphicCollection(List<Array> data, PolygonBreak polygonBreak) {
        super();

        updateGraphics(data, polygonBreak);
    }

    /**
     * Constructor
     * @param data Data list
     * @param polygonBreaks Polygon break list
     */
    public PolygonGraphicCollection(List<Array> data, List<PolygonBreak> polygonBreaks) {
        super();

        updateGraphics(data, polygonBreaks);
    }

    /**
     * Constructor
     * @param data Data list
     * @param cdata Color data
     * @param ls Legend scheme
     */
    public PolygonGraphicCollection(List<Array> data, Array cData, LegendScheme ls) {
        this.legendScheme = ls;
        this.setSingleLegend(false);
        if (cData.getSize() == data.size()) {
            List<PolygonBreak> polygonBreaks = new ArrayList<>();
            IndexIterator iterC = cData.getIndexIterator();
            while (iterC.hasNext()) {
                polygonBreaks.add((PolygonBreak) ls.findLegendBreak(iterC.getDoubleNext()));
            }
            updateGraphics(data, polygonBreaks);
        } else {
            updateGraphics(data, cData, ls);
        }
    }

    /**
     * Constructor
     * @param data Data list
     * @param cdata Color data
     * @param ls Legend scheme
     */
    public PolygonGraphicCollection(List<Array> data, List<Array> cData, LegendScheme ls) {
        this.legendScheme = ls;
        this.setSingleLegend(false);
        updateGraphics(data, cData, ls);
    }

    protected void updateGraphics(List<Array> data, PolygonBreak polygonBreak) {
        this.data = data;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        try {
            for (Array array : data) {
                int[] arrShape = array.getShape();
                int nPoint = arrShape[0];
                int nDim = arrShape[1];
                origin = new int[]{0, 0};
                shape = new int[]{nPoint, 1};
                Array x = array.section(origin, shape);
                origin = new int[]{0, 1};
                Array y = array.section(origin, shape);
                Array z = null;
                if (nDim == 3) {
                    origin = new int[]{0, 2};
                    z = array.section(origin, shape);
                }
                this.add(new PolygonGraphic(x, y, z, polygonBreak));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, List<PolygonBreak> breaks) {
        this.singleLegend = false;
        this.data = data;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        try {
            int i = 0;
            for (Array array : data) {
                int[] arrShape = array.getShape();
                int nPoint = arrShape[0];
                int nDim = arrShape[1];
                origin = new int[]{0, 0};
                shape = new int[]{nPoint, 1};
                Array x = array.section(origin, shape);
                origin = new int[]{0, 1};
                Array y = array.section(origin, shape);
                Array z = null;
                if (nDim == 3) {
                    origin = new int[]{0, 2};
                    z = array.section(origin, shape);
                }
                if (i >= breaks.size()) {
                    i = 0;
                }
                this.add(new PolygonGraphic(x, y, z, breaks.get(i)));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, List<Array> cData, LegendScheme ls) {
        this.singleLegend = false;
        this.data = data;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        try {
            int i = 0;
            for (Array array : data) {
                int[] arrShape = array.getShape();
                int nPoint = arrShape[0];
                int nDim = arrShape[1];
                origin = new int[]{0, 0};
                shape = new int[]{nPoint, 1};
                Array x = array.section(origin, shape);
                origin = new int[]{0, 1};
                Array y = array.section(origin, shape);
                Array z = null;
                if (nDim == 3) {
                    origin = new int[]{0, 2};
                    z = array.section(origin, shape);
                }
                this.add(new PolygonGraphic(x, y, z, cData.get(i), ls));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, Array cData, LegendScheme ls) {
        this.singleLegend = false;
        this.data = data;
        this.cData = cData;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        try {
            int i = 0;
            for (Array array : data) {
                int[] arrShape = array.getShape();
                int nPoint = arrShape[0];
                int nDim = arrShape[1];
                origin = new int[]{0, 0};
                shape = new int[]{nPoint, 1};
                Array x = array.section(origin, shape);
                origin = new int[]{0, 1};
                Array y = array.section(origin, shape);
                Array z = null;
                if (nDim == 3) {
                    origin = new int[]{0, 2};
                    z = array.section(origin, shape);
                }
                origin = new int[]{0, i};
                Array c = cData.section(origin, shape);
                this.add(new PolygonGraphic(x, y, z, c, ls));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Return has color data array or not
     *
     * @return Has color data array of not
     */
    public boolean hasColorData() {
        return this.cData != null;
    }

    /**
     * Get segments data
     * @return Segments data
     */
    public List<Array> getData() {
        return this.data;
    }

    /**
     * Set segments data
     * @param value Segments data
     */
    public void setData(List<Array> value) {
        if (this.cData != null) {
            updateGraphics(value, this.cData, this.legendScheme);
        } else {
            List<PolygonBreak> breaks = getLegendBreaks();
            updateGraphics(value, breaks);
        }
    }

    /**
     * Set segments data
     * @param value Segments data
     */
    public void setData(List<Array> value, Array cData) {
        updateGraphics(value, cData, this.legendScheme);
    }

    /**
     * Get legend breaks
     * @return Legend breaks
     */
    public List<PolygonBreak> getLegendBreaks() {
        List<PolygonBreak> breaks = new ArrayList<>();
        for (PolygonGraphic graphic : (List<PolygonGraphic>) this.getGraphics()) {
            breaks.add((PolygonBreak) graphic.legendBreak);
        }
        return breaks;
    }

    /**
     * Get is fill polyogns or not
     * @return Fill polygons or not
     */
    public boolean isFill() {
        PolygonBreak polygonBreak = (PolygonBreak) this.getLegendBreak();
        return polygonBreak.isDrawFill();
    }

    /**
     * Get is draw outline or not
     * @return Draw outline or not
     */
    public boolean isDrawOutline() {
        PolygonBreak polygonBreak = (PolygonBreak) this.getLegendBreak();
        return polygonBreak.isDrawOutline();
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

    @Override
    public float[] getVertexPosition() {
        return this.vertexPosition;
    }

    public void setVertexPosition(float[] vertexPosition) {
        this.vertexPosition = vertexPosition;
    }

    @Override
    public int[] getVertexIndices() {
        return this.vertexIndices;
    }

    @Override
    public float[] getVertexNormal() {
        return this.vertexNormal;
    }

    @Override
    public float[] getVertexColor() {
        return this.vertexColor;
    }

    /**
     * Get outline vertext array
     * @return Outline vertex array
     */
    public float[] getLineVertex() {
        return this.lineVertex;
    }

    public void setLineVertex(float[] lineVertex) {
        this.lineVertex = lineVertex;
    }

    /**
     * Get line point number list
     * @return Line point number list
     */
    public List<Integer> getLinePointNumbers() {
        return this.linePointNumbers;
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
     * Builds deduplicated vertex arrays (position, indices, color) from tessellated triangles.
     * Vertices sharing both spatial position AND color are merged into a single index.
     *
     * @param triangles Per-polygon triangle lists produced by the tessellator.
     * @return Object array: [0]=float[] positions, [1]=int[] indices, [2]=float[] colors.
     */
    public Object[] getVertices(List<List<Triangle>> triangles) {

        // --- Phase 1: Estimate capacity to minimize rehashing and reallocation ---

        int estimatedTriangles = 0;
        for (List<Triangle> list : triangles) {
            estimatedTriangles += list.size();
        }

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
        for (int polyIdx = 0; polyIdx < triangles.size(); polyIdx++) {
            List<Triangle> list = triangles.get(polyIdx);
            Color color = this.getGraphicN(polyIdx).legendBreak.getColor();
            float[] colors = color.getRGBComponents(null);

            for (Triangle triangle : list) {
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
    public void updateLineVertex() {
        int n = this.getNumPoints();
        lineVertex = new float[n * 3];
        linePointNumbers = new ArrayList<>();
        int idx = 0;
        for (int i = 0; i < this.getNumGraphics(); i++) {
            PolygonGraphic pg = (PolygonGraphic) this.getGraphicN(i);
            MultiPolygon mp = (MultiPolygon) pg.getGeometry();
            for (org.meteoinfo.geometry.Polygon p : (Polygon[]) mp.getGeometries()) {
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
        }
    }

    @Override
    public void tessellate() {
        tessellate(false);
    }

    public void tessellate(boolean subdivide) {
        if (subdivide) {
            List<List<Triangle>> triangles = new ArrayList<>();
            for (int i = 0; i < this.getNumGraphics(); i++) {
                List<Triangle> ts1 = TriangleTessellator.tessellate((PolygonGraphic) this.getGraphicN(i));
                List<Triangle> ts2 = new ArrayList<>();
                for (Triangle triangle : ts1) {
                    ts2.addAll(TriangleTessellator.subdivideEquilateral(triangle, 3.0f));
                }
                triangles.add(ts2);
            }

            Object[] r = getVertices(triangles);
            this.vertexPosition = (float[]) r[0];
            this.vertexIndices = (int[]) r[1];
            this.vertexColor = (float[]) r[2];
        } else {
            List<List<Triangle>> triangles = new ArrayList<>();
            for (int i = 0; i < this.getNumGraphics(); i++) {
                triangles.add(TriangleTessellator.tessellate((PolygonGraphic) this.getGraphicN(i)));
            }

            Object[] r = getVertices(triangles);
            this.vertexPosition = (float[]) r[0];
            this.vertexIndices = (int[]) r[1];
            this.vertexColor = (float[]) r[2];
        }
        this.tessellateDirty = false;
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
        if (!this.tessellateDirty && this.vertexNormal != null) {
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
}
