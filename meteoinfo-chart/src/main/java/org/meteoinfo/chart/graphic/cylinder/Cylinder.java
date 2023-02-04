package org.meteoinfo.chart.graphic.cylinder;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Cylinder {
    private float baseRadius;
    private float topRadius;
    private float height;
    private int sectorCount;
    private int stackCount;
    private boolean smooth;
    private int baseIndex;
    private int topIndex;
    private List<Vector3f> unitCircleVertices;
    private List<Vector3f> vertices;
    private List<Vector3f> normals;
    private List<Integer> indices;
    private List<Integer> lineIndices;
    private List<Vector2f> texCoords;
    private List<Vector3f> interleavedVertices;
    private int interleavedStride;

    /**
     * Constructor
     *
     * @param baseRadius The base radius
     * @param topRadius The top radius
     * @param height The height
     * @param sectorCount The sector count
     * @param stackCount The stack count
     * @param smooth Whether smooth
     */
    public Cylinder(float baseRadius, float topRadius, float height, int sectorCount, int stackCount,
                    boolean smooth) {
        this.baseRadius = baseRadius;
        this.topRadius = topRadius;
        this.height = height;
        this.sectorCount = sectorCount;
        this.stackCount = stackCount;
        this.smooth = smooth;

        updateVertices();
    }

    /**
     * Update vertices
     */
    public void updateVertices() {
        // generate unit circle vertices first
        buildUnitCircleVertices();

        if (smooth) {
            buildVerticesSmooth();
        } else {
            buildVerticesFlat();
        }
    }

    /**
     * Get base radius
     * @return Base radius
     */
    public float getBaseRadius() {
        return baseRadius;
    }

    /**
     * Set base radius
     * @param value Base radius
     */
    public void setBaseRadius(float value) {
        if (this.baseRadius != value) {
            this.baseRadius = value;
            updateVertices();
        }
    }

    /**
     * Get top radius
     * @return Top radius
     */
    public float getTopRadius() {
        return topRadius;
    }

    /**
     * Set top radius
     * @param value Top radius
     */
    public void setTopRadius(float value) {
        if (this.topRadius != value) {
            this.topRadius = value;
            updateVertices();
        }
    }

    /**
     * Get height
     * @return Height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set height
     * @param value Height
     */
    public void setHeight(float value) {
        if (this.height != value) {
            this.height = value;
            updateVertices();
        }
    }

    /**
     * Get sector count
     * @return Sector count
     */
    public int getSectorCount() {
        return sectorCount;
    }

    /**
     * Set sector count
     * @param value Sector count
     */
    public void setSectorCount(int value) {
        if (this.sectorCount != value) {
            this.sectorCount = value;
            updateVertices();
        }
    }

    /**
     * Get stack count
     * @return Stack count
     */
    public int getStackCount() {
        return stackCount;
    }

    /**
     * Set stack count
     * @param value stack count
     */
    public void setStackCount(int value) {
        if (this.stackCount != value) {
            this.stackCount = value;
            updateVertices();
        }
    }

    /**
     * Get smooth
     * @return Smooth
     */
    public boolean isSmooth() {
        return smooth;
    }

    /**
     * Set smooth
     * @param value Smooth
     */
    public void setSmooth(boolean value) {
        if (this.smooth != value) {
            this.smooth = value;
            updateVertices();
        }
    }

    /**
     * Get vertices
     * @return Vertices
     */
    public List<Vector3f> getVertices() {
        return vertices;
    }

    /**
     * Get normals
     * @return Normals
     */
    public List<Vector3f> getNormals() {
        return normals;
    }

    /**
     * Get texture coordinates
     * @return Texture coordinates
     */
    public List<Vector2f> getTexCoords() {
        return texCoords;
    }

    /**
     * Get indices
     * @return Indices
     */
    public List<Integer> getIndices() {
        return indices;
    }

    /**
     * Get interleaved vertices
     * @return Interleaved vertices
     */
    public List<Vector3f> getInterleavedVertices() {
        return interleavedVertices;
    }

    /**
     * Get interleaved stride
     * @return Interleaved stride
     */
    public int getInterleavedStride() {
        return interleavedStride;
    }

    /**
     * generate interleaved vertices: V/N/T
     * stride must be 32 bytes
     */
    void buildInterleavedVertices()
    {
        interleavedVertices = new ArrayList<>();

        int i, j;
        int count = vertices.size();
        for(i = 0; i < count; i++)
        {
            interleavedVertices.add(vertices.get(i));

            interleavedVertices.add(normals.get(i));

            //interleavedVertices.add(texCoords.get(j));
            //interleavedVertices.add(texCoords.get(j+1));
        }
    }

    /**
     * generate 3D vertices of a unit circle on XY place
     */
    void buildUnitCircleVertices() {
        float sectorStep = (float) (2 * Math.PI / sectorCount);
        float sectorAngle;  // radian

        this.unitCircleVertices = new ArrayList<>();
        float x, y, z = 0.f;
        for(int i = 0; i <= sectorCount; ++i) {
            sectorAngle = i * sectorStep;
            x = (float) Math.cos(sectorAngle);
            y = (float) Math.sin(sectorAngle);
            unitCircleVertices.add(new Vector3f(x, y, z));
        }
    }

    class Vertex {
        public float x, y, z, s, t;
    }

    void clearArrays() {
        this.vertices = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.lineIndices = new ArrayList<>();
        this.texCoords = new ArrayList<>();
    }

    /**
     * Add single vertex to array
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    void addVertex(float x, float y, float z) {
        vertices.add(new Vector3f(x, y, z));
    }

    /**
     * Add single normal to array
     * @param nx Normal x
     * @param ny Normal y
     * @param nz Normal z
     */
    void addNormal(float nx, float ny, float nz)
    {
        normals.add(new Vector3f(nx, ny, nz));
    }

    void addNormal(Vector3f n) {
        normals.add(n);
    }

    /**
     * Add single texture coord to array
     * @param s
     * @param t
     */
    void addTexCoord(float s, float t)
    {
        texCoords.add(new Vector2f(s, t));
    }

    /**
     * Add 3 indices to array
     * @param i1 Index 1
     * @param i2 Index 2
     * @param i3 Index 3
     */
    void addIndices(int i1, int i2, int i3) {
        indices.add(i1);
        indices.add(i2);
        indices.add(i3);
    }

    /**
     * build vertices of cylinder with smooth shading
     * where v: sector angle (0 <= v <= 360)
     */
    void buildVerticesSmooth()
    {
        // clear memory of prev arrays
        clearArrays();

        float x, y, z;                                  // vertex position
        //float s, t;                                     // texCoord
        float radius;                                   // radius for each stack

        // get normals for cylinder sides
        List<Vector3f> sideNormals = getSideNormals();

        // put vertices of side cylinder to array by scaling unit circle
        for(int i = 0; i <= stackCount; ++i)
        {
            z = -(height * 0.5f) + (float)i / stackCount * height;      // vertex position z
            radius = baseRadius + (float)i / stackCount * (topRadius - baseRadius);     // lerp
            float t = 1.0f - (float)i / stackCount;   // top-to-bottom

            for(int j = 0; j <= sectorCount; ++j)
            {
                Vector3f v = unitCircleVertices.get(j);
                addVertex(v.x * radius, v.y * radius, z);   // position
                Vector3f s = sideNormals.get(j);
                addNormal(sideNormals.get(j)); // normal
                addTexCoord((float)j / sectorCount, t); // tex coord
            }
        }

        // remember where the base.top vertices start
        int baseVertexIndex = vertices.size();

        // put vertices of base of cylinder
        z = -height * 0.5f;
        addVertex(0, 0, z);
        addNormal(0, 0, -1);
        addTexCoord(0.5f, 0.5f);
        for(int i = 0; i < sectorCount; ++i)
        {
            Vector3f v = unitCircleVertices.get(i);
            addVertex(v.x * baseRadius, v.y * baseRadius, z);
            addNormal(0, 0, -1);
            addTexCoord(-v.x * 0.5f + 0.5f, -v.y * 0.5f + 0.5f);    // flip horizontal
        }

        // remember where the base vertices start
        int topVertexIndex = vertices.size();

        // put vertices of top of cylinder
        z = height * 0.5f;
        addVertex(0, 0, z);
        addNormal(0, 0, 1);
        addTexCoord(0.5f, 0.5f);
        for(int i = 0; i < sectorCount; ++i)
        {
            Vector3f v = unitCircleVertices.get(i);
            addVertex(v.x * topRadius, v.y * topRadius, z);
            addNormal(0, 0, 1);
            addTexCoord(v.x * 0.5f + 0.5f, -v.y * 0.5f + 0.5f);
        }

        // put indices for sides
        int k1, k2;
        for(int i = 0; i < stackCount; ++i)
        {
            k1 = i * (sectorCount + 1);     // beginning of current stack
            k2 = k1 + sectorCount + 1;      // beginning of next stack

            for(int j = 0; j < sectorCount; ++j, ++k1, ++k2)
            {
                // 2 triangles per sector
                addIndices(k1, k1 + 1, k2);
                addIndices(k2, k1 + 1, k2 + 1);

                // vertical lines for all stacks
                lineIndices.add(k1);
                lineIndices.add(k2);
                // horizontal lines
                lineIndices.add(k2);
                lineIndices.add(k2 + 1);
                if(i == 0) {
                    lineIndices.add(k1);
                    lineIndices.add(k1 + 1);
                }
            }
        }

        // remember where the base indices start
        baseIndex = indices.size();

        // put indices for base
        for(int i = 0, k = baseVertexIndex + 1; i < sectorCount; ++i, ++k) {
            if(i < (sectorCount - 1))
                addIndices(baseVertexIndex, k + 1, k);
            else    // last triangle
                addIndices(baseVertexIndex, baseVertexIndex + 1, k);
        }

        // remember where the base indices start
        topIndex = indices.size();

        for(int i = 0, k = topVertexIndex + 1; i < sectorCount; ++i, ++k) {
            if(i < (sectorCount - 1))
                addIndices(topVertexIndex, k, k + 1);
            else
                addIndices(topVertexIndex, k, topVertexIndex + 1);
        }

        // generate interleaved vertex array as well
        buildInterleavedVertices();
    }

    /**
     *  generate vertices with flat shading
     *  each triangle is independent (no shared vertices)
     */
    void buildVerticesFlat()
    {
        // tmp vertex definition (x,y,z,s,t)
        List<Vertex> tmpVertices = new ArrayList<>();

        int i, j, k;    // indices
        float x, y, z, s, t, radius;

        // put tmp vertices of cylinder side to array by scaling unit circle
        //NOTE: start and end vertex positions are same, but texcoords are different
        //      so, add additional vertex at the end point
        for(i = 0; i <= stackCount; ++i) {
            z = -(height * 0.5f) + (float)i / stackCount * height;      // vertex position z
            radius = baseRadius + (float)i / stackCount * (topRadius - baseRadius);     // lerp
            t = 1.0f - (float)i / stackCount;   // top-to-bottom

            for(j = 0; j <= sectorCount; ++j) {
                Vector3f v = unitCircleVertices.get(j);
                s = (float)j / sectorCount;

                Vertex vertex = new Vertex();
                vertex.x = v.x * radius;
                vertex.y = v.y * radius;
                vertex.z = z;
                vertex.s = s;
                vertex.t = t;
                tmpVertices.add(vertex);
            }
        }

        // clear memory of prev arrays
        clearArrays();

        Vertex v1, v2, v3, v4;      // 4 vertex positions v1, v2, v3, v4
        float[] n;       // 1 face normal
        int vi1, vi2;               // indices
        int index = 0;

        // v2-v4 <== stack at i+1
        // | \ |
        // v1-v3 <== stack at i
        for(i = 0; i < stackCount; ++i)
        {
            vi1 = i * (sectorCount + 1);            // index of tmpVertices
            vi2 = (i + 1) * (sectorCount + 1);

            for(j = 0; j < sectorCount; ++j, ++vi1, ++vi2)
            {
                v1 = tmpVertices.get(vi1);
                v2 = tmpVertices.get(vi2);
                v3 = tmpVertices.get(vi1 + 1);
                v4 = tmpVertices.get(vi2 + 1);

                // compute a face normal of v1-v3-v2
                n = computeFaceNormal(v1.x, v1.y, v1.z, v3.x, v3.y, v3.z, v2.x, v2.y, v2.z);

                // put quad vertices: v1-v2-v3-v4
                addVertex(v1.x, v1.y, v1.z);
                addVertex(v2.x, v2.y, v2.z);
                addVertex(v3.x, v3.y, v3.z);
                addVertex(v4.x, v4.y, v4.z);

                // put tex coords of quad
                addTexCoord(v1.s, v1.t);
                addTexCoord(v2.s, v2.t);
                addTexCoord(v3.s, v3.t);
                addTexCoord(v4.s, v4.t);

                // put normal
                for(k = 0; k < 4; ++k)  // same normals for all 4 vertices
                {
                    addNormal(n[0], n[1], n[2]);
                }

                // put indices of a quad
                addIndices(index,   index+2, index+1);    // v1-v3-v2
                addIndices(index+1, index+2, index+3);    // v2-v3-v4

                // vertical line per quad: v1-v2
                lineIndices.add(index);
                lineIndices.add(index+1);
                // horizontal line per quad: v2-v4
                lineIndices.add(index+1);
                lineIndices.add(index+3);
                if (i == 0) {
                    lineIndices.add(index);
                    lineIndices.add(index+2);
                }

                index += 4;     // for next
            }
        }

        // remember where the base index starts
        baseIndex = indices.size();
        int baseVertexIndex = vertices.size() / 3;

        // put vertices of base of cylinder
        z = -height * 0.5f;
        addVertex(0, 0, z);
        addNormal(0, 0, -1);
        addTexCoord(0.5f, 0.5f);
        for(i = 0; i < sectorCount; ++i)
        {
            Vector3f v = unitCircleVertices.get(i);
            addVertex(v.x * baseRadius, v.y * baseRadius, z);
            addNormal(0, 0, -1);
            addTexCoord(-v.x * 0.5f + 0.5f, -v.y * 0.5f + 0.5f); // flip horizontal
        }

        // put indices for base
        for(i = 0, k = baseVertexIndex + 1; i < sectorCount; ++i, ++k)
        {
            if(i < sectorCount - 1)
                addIndices(baseVertexIndex, k + 1, k);
            else
                addIndices(baseVertexIndex, baseVertexIndex + 1, k);
        }

        // remember where the top index starts
        topIndex = indices.size();
        int topVertexIndex = vertices.size() / 3;

        // put vertices of top of cylinder
        z = height * 0.5f;
        addVertex(0, 0, z);
        addNormal(0, 0, 1);
        addTexCoord(0.5f, 0.5f);
        for(i = 0; i < sectorCount; ++i)
        {
            Vector3f v = unitCircleVertices.get(i);
            addVertex(v.x * topRadius, v.y * topRadius, z);
            addNormal(0, 0, 1);
            addTexCoord(v.x * 0.5f + 0.5f, -v.y * 0.5f + 0.5f);
        }

        for(i = 0, k = topVertexIndex + 1; i < sectorCount; ++i, ++k)
        {
            if(i < sectorCount - 1)
                addIndices(topVertexIndex, k, k + 1);
            else
                addIndices(topVertexIndex, k, topVertexIndex + 1);
        }

        // generate interleaved vertex array as well
        buildInterleavedVertices();
    }

    ///////////////////////////////////////////////////////////////////////////////
    // generate shared normal vectors of the side of cylinder
    ///////////////////////////////////////////////////////////////////////////////
    List<Vector3f> getSideNormals()
    {
        float sectorStep = (float) (2 * Math.PI / sectorCount);
        float sectorAngle;  // radian

        // compute the normal vector at 0 degree first
        // tanA = (baseRadius-topRadius) / height
        float zAngle = (float) Math.atan2(baseRadius - topRadius, height);
        float x0 = (float) Math.cos(zAngle);     // nx
        float y0 = 0;               // ny
        float z0 = (float) Math.sin(zAngle);     // nz

        // rotate (x0,y0,z0) per sector angle
        List<Vector3f> normals = new ArrayList<>();
        for(int i = 0; i <= sectorCount; ++i)
        {
            sectorAngle = i * sectorStep;
            normals.add(new Vector3f((float) (Math.cos(sectorAngle)*x0 - Math.sin(sectorAngle)*y0),
                    (float) (Math.sin(sectorAngle)*x0 + Math.cos(sectorAngle)*y0), z0));
        }

        return normals;
    }

    ///////////////////////////////////////////////////////////////////////////////
    // return face normal of a triangle v1-v2-v3
    // if a triangle has no surface (normal length = 0), then return a zero vector
    ///////////////////////////////////////////////////////////////////////////////
    float[] computeFaceNormal(float x1, float y1, float z1,  // v1
                                                   float x2, float y2, float z2,  // v2
                                                   float x3, float y3, float z3)  // v3
    {
        float EPSILON = 0.000001f;

        float[] normal = new float[]{0.f, 0.f, 0.f};     // default return value (0,0,0)
        float nx, ny, nz;

        // find 2 edge vectors: v1-v2, v1-v3
        float ex1 = x2 - x1;
        float ey1 = y2 - y1;
        float ez1 = z2 - z1;
        float ex2 = x3 - x1;
        float ey2 = y3 - y1;
        float ez2 = z3 - z1;

        // cross product: e1 x e2
        nx = ey1 * ez2 - ez1 * ey2;
        ny = ez1 * ex2 - ex1 * ez2;
        nz = ex1 * ey2 - ey1 * ex2;

        // normalize only if the length is > 0
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if(length > EPSILON) {
            // normalize
            float lengthInv = 1.0f / length;
            normal[0] = nx * lengthInv;
            normal[1] = ny * lengthInv;
            normal[2] = nz * lengthInv;
        }

        return normal;
    }
}
