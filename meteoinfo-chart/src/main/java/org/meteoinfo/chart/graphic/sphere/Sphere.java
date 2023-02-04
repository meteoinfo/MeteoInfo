package org.meteoinfo.chart.graphic.sphere;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.meteoinfo.chart.graphic.cylinder.Cylinder;

import java.util.ArrayList;
import java.util.List;

public class Sphere {
    private float radius;
    private int sectorCount;         // longitude, # of slices
    private int stackCount;          // latitude, # of stacks
    private boolean smooth;
    private List<Vector3f> vertices;
    private List<Vector3f> normals;
    private List<Integer> indices;
    private List<Integer> lineIndices;
    private List<Vector2f> texCoords;
    private List<Vector3f> interleavedVertices;

    /**
     * Constructor
     * @param radius The sphere radius
     * @param sectorCount The sector count
     * @param stackCount The stack count
     * @param smooth Whether smooth
     */
    public Sphere(float radius, int sectorCount, int stackCount, boolean smooth) {
        this.radius = radius;
        this.sectorCount = sectorCount;
        this.stackCount = stackCount;
        this.smooth = smooth;

        this.updateVertices();
    }

    /**
     * Constructor
     * @param radius The sphere radius
     * @param sectorCount The sector count
     * @param stackCount The stack count
     */
    public Sphere(float radius, int sectorCount, int stackCount) {
        this(radius, sectorCount, stackCount, true);
    }

    /**
     * Update vertices
     */
    public void updateVertices() {
        if (smooth) {
            buildVerticesSmooth();
        } else {
            buildVerticesFlat();
        }
    }

    /**
     * Get radius
     * @return Radius
     */
    public float getRadius() {
        return this.radius;
    }

    /**
     * Set radius
     * @param value Radius
     */
    public void setRadius(float value) {
        if (this.radius != value) {
            this.radius = value;
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

    void clearArrays() {
        this.vertices = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.lineIndices = new ArrayList<>();
        this.texCoords = new ArrayList<>();
    }

    class Vertex {
        public float x, y, z, s, t;
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

    void buildVerticesSmooth() {

        // clear memory of prev arrays
        clearArrays();

        float x, y, z, xy;                              // vertex position
        float nx, ny, nz, lengthInv = 1.0f / radius;    // normal
        float s, t;                                     // texCoord

        float sectorStep = 2 * (float) Math.PI / sectorCount;
        float stackStep = (float) Math.PI / stackCount;
        float sectorAngle, stackAngle;

        for(int i = 0; i <= stackCount; ++i)
        {
            stackAngle = (float) Math.PI / 2 - i * stackStep;        // starting from pi/2 to -pi/2
            xy = radius * (float) Math.cos(stackAngle);             // r * cos(u)
            z = radius * (float) Math.sin(stackAngle);              // r * sin(u)

            // add (sectorCount+1) vertices per stack
            // the first and last vertices have same position and normal, but different tex coords
            for(int j = 0; j <= sectorCount; ++j)
            {
                sectorAngle = j * sectorStep;           // starting from 0 to 2pi

                // vertex position
                x = xy * (float) Math.cos(sectorAngle);             // r * cos(u) * cos(v)
                y = xy * (float) Math.sin(sectorAngle);             // r * cos(u) * sin(v)
                addVertex(x, y, z);

                // normalized vertex normal
                nx = x * lengthInv;
                ny = y * lengthInv;
                nz = z * lengthInv;
                addNormal(nx, ny, nz);

                // vertex tex coord between [0, 1]
                s = (float)j / sectorCount;
                t = (float)i / stackCount;
                addTexCoord(s, t);
            }
        }

        // indices
        //  k1--k1+1
        //  |  / |
        //  | /  |
        //  k2--k2+1
        int k1, k2;
        for(int i = 0; i < stackCount; ++i)
        {
            k1 = i * (sectorCount + 1);     // beginning of current stack
            k2 = k1 + sectorCount + 1;      // beginning of next stack

            for(int j = 0; j < sectorCount; ++j, ++k1, ++k2)
            {
                // 2 triangles per sector excluding 1st and last stacks
                if(i != 0)
                {
                    addIndices(k1, k2, k1+1);   // k1---k2---k1+1
                }

                if(i != (stackCount-1))
                {
                    addIndices(k1+1, k2, k2+1); // k1+1---k2---k2+1
                }

                // vertical lines for all stacks
                lineIndices.add(k1);
                lineIndices.add(k2);
                if(i != 0)  // horizontal lines except 1st stack
                {
                    lineIndices.add(k1);
                    lineIndices.add(k1 + 1);
                }
            }
        }

        // generate interleaved vertex array as well
        buildInterleavedVertices();
    }

    void buildVerticesFlat() {
        // tmp vertex definition (x,y,z,s,t)
        List<Sphere.Vertex> tmpVertices = new ArrayList<>();

        float sectorStep = 2 * (float) Math.PI / sectorCount;
        float stackStep = (float) Math.PI / stackCount;
        float sectorAngle, stackAngle;

        // compute all vertices first, each vertex contains (x,y,z,s,t) except normal
        for(int i = 0; i <= stackCount; ++i)
        {
            stackAngle = (float) Math.PI / 2 - i * stackStep;        // starting from pi/2 to -pi/2
            float xy = radius * (float) Math.cos(stackAngle);       // r * cos(u)
            float z = radius * (float) Math.sin(stackAngle);        // r * sin(u)

            // add (sectorCount+1) vertices per stack
            // the first and last vertices have same position and normal, but different tex coords
            for(int j = 0; j <= sectorCount; ++j)
            {
                sectorAngle = j * sectorStep;           // starting from 0 to 2pi

                Vertex vertex = new Vertex();
                vertex.x = xy * (float) Math.cos(sectorAngle);      // x = r * cos(u) * cos(v)
                vertex.y = xy * (float) Math.sin(sectorAngle);      // y = r * cos(u) * sin(v)
                vertex.z = z;                           // z = r * sin(u)
                vertex.s = (float)j/sectorCount;        // s
                vertex.t = (float)i/stackCount;         // t
                tmpVertices.add(vertex);
            }
        }

        // clear memory of prev arrays
        clearArrays();

        Vertex v1, v2, v3, v4;                          // 4 vertex positions and tex coords
        float[] n;                           // 1 face normal

        int i, j, k, vi1, vi2;
        int index = 0;                                  // index for vertex
        for(i = 0; i < stackCount; ++i)
        {
            vi1 = i * (sectorCount + 1);                // index of tmpVertices
            vi2 = (i + 1) * (sectorCount + 1);

            for(j = 0; j < sectorCount; ++j, ++vi1, ++vi2)
            {
                // get 4 vertices per sector
                //  v1--v3
                //  |    |
                //  v2--v4
                v1 = tmpVertices.get(vi1);
                v2 = tmpVertices.get(vi2);
                v3 = tmpVertices.get(vi1 + 1);
                v4 = tmpVertices.get(vi2 + 1);

                // if 1st stack and last stack, store only 1 triangle per sector
                // otherwise, store 2 triangles (quad) per sector
                if(i == 0) // a triangle for first stack ==========================
                {
                    // put a triangle
                    addVertex(v1.x, v1.y, v1.z);
                    addVertex(v2.x, v2.y, v2.z);
                    addVertex(v4.x, v4.y, v4.z);

                    // put tex coords of triangle
                    addTexCoord(v1.s, v1.t);
                    addTexCoord(v2.s, v2.t);
                    addTexCoord(v4.s, v4.t);

                    // put normal
                    n = computeFaceNormal(v1.x,v1.y,v1.z, v2.x,v2.y,v2.z, v4.x,v4.y,v4.z);
                    for(k = 0; k < 3; ++k)  // same normals for 3 vertices
                    {
                        addNormal(n[0], n[1], n[2]);
                    }

                    // put indices of 1 triangle
                    addIndices(index, index+1, index+2);

                    // indices for line (first stack requires only vertical line)
                    lineIndices.add(index);
                    lineIndices.add(index+1);

                    index += 3;     // for next
                }
                else if(i == (stackCount-1)) // a triangle for last stack =========
                {
                    // put a triangle
                    addVertex(v1.x, v1.y, v1.z);
                    addVertex(v2.x, v2.y, v2.z);
                    addVertex(v3.x, v3.y, v3.z);

                    // put tex coords of triangle
                    addTexCoord(v1.s, v1.t);
                    addTexCoord(v2.s, v2.t);
                    addTexCoord(v3.s, v3.t);

                    // put normal
                    n = computeFaceNormal(v1.x,v1.y,v1.z, v2.x,v2.y,v2.z, v3.x,v3.y,v3.z);
                    for(k = 0; k < 3; ++k)  // same normals for 3 vertices
                    {
                        addNormal(n[0], n[1], n[2]);
                    }

                    // put indices of 1 triangle
                    addIndices(index, index+1, index+2);

                    // indices for lines (last stack requires both vert/hori lines)
                    lineIndices.add(index);
                    lineIndices.add(index+1);
                    lineIndices.add(index);
                    lineIndices.add(index+2);

                    index += 3;     // for next
                }
                else // 2 triangles for others ====================================
                {
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
                    n = computeFaceNormal(v1.x,v1.y,v1.z, v2.x,v2.y,v2.z, v3.x,v3.y,v3.z);
                    for(k = 0; k < 4; ++k)  // same normals for 4 vertices
                    {
                        addNormal(n[0], n[1], n[2]);
                    }

                    // put indices of quad (2 triangles)
                    addIndices(index, index+1, index+2);
                    addIndices(index+2, index+1, index+3);

                    // indices for lines
                    lineIndices.add(index);
                    lineIndices.add(index+1);
                    lineIndices.add(index);
                    lineIndices.add(index+2);

                    index += 4;     // for next
                }
            }
        }

        // generate interleaved vertex array as well
        buildInterleavedVertices();
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
