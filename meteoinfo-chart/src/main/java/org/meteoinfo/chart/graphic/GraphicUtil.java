package org.meteoinfo.chart.graphic;

import org.meteoinfo.common.Extent;

import java.util.ArrayList;
import java.util.List;

public class GraphicUtil {

    /**
     * Convert MeteoInfo MeshGraphic (quadrilateral mesh) to TriMeshGraphic (triangle mesh)
     * Each quad face will be split into two triangle faces.
     * @param meshGraphic Source quadrilateral mesh graphic
     * @param chooseShortestDiag If true, select the shorter diagonal to split quad, avoid poorly shaped triangles
     * @return Converted TriMeshGraphic instance
     */
    public static TriMeshGraphic meshToTriMesh(QuadMeshGraphic meshGraphic, boolean chooseShortestDiag) {
        // ========== Read source mesh data ==========
        float[] vertices = meshGraphic.getVertexPosition();       // Vertex array: interleaved xyzxyz... float[3 * vertexCount]
        float[] normals = meshGraphic.getVertexNormal();         // Vertex normal array, nullable
        float[] texCoords = meshGraphic.getVertexTexture();     // Texture coordinate array, nullable
        float[] colorData = meshGraphic.getVertexColor();       // Vertex color array, nullable
        int[] quads = meshGraphic.getVertexIndices();           // Quad index array: every 4 integers form one quad [a,b,c,d]
        int quadCount = quads.length / 4;

        List<Integer> triIndices = new ArrayList<>();       // Output triangle indices: every 3 integers form one triangle

        // ========== Iterate all quads and split ==========
        for (int qIdx = 0; qIdx < quadCount; qIdx++) {
            int base = qIdx * 4;
            int a = quads[base];
            int b = quads[base + 1];
            int c = quads[base + 2];
            int d = quads[base + 3];

            if (chooseShortestDiag) {
                // Extract xyz coordinates of four vertices
                float[] va = getVertexXYZ(vertices, a);
                float[] vb = getVertexXYZ(vertices, b);
                float[] vc = getVertexXYZ(vertices, c);
                float[] vd = getVertexXYZ(vertices, d);
                double diagAC = distance(va, vc);
                double diagBD = distance(vb, vd);

                if (diagAC <= diagBD) {
                    // Split along diagonal a-c: triangle1(a,b,c), triangle2(a,c,d)
                    triIndices.add(a); triIndices.add(b); triIndices.add(c);
                    triIndices.add(a); triIndices.add(c); triIndices.add(d);
                } else {
                    // Split along diagonal b-d: triangle1(b,c,d), triangle2(b,d,a)
                    triIndices.add(b); triIndices.add(c); triIndices.add(d);
                    triIndices.add(b); triIndices.add(d); triIndices.add(a);
                }
            } else {
                // Fixed split along a-c, faster for regular structured grids
                triIndices.add(a); triIndices.add(b); triIndices.add(c);
                triIndices.add(a); triIndices.add(c); triIndices.add(d);
            }
        }

        // ========== Convert list to primitive arrays ==========
        int[] triFaces = triIndices.stream().mapToInt(Integer::intValue).toArray();

        // ========== Construct TriMeshGraphic ==========
        TriMeshGraphic triMesh = new TriMeshGraphic();
        triMesh.setVertexIndices(triFaces);
        triMesh.setVertexNormal(normals);
        triMesh.setVertexPosition(vertices);
        triMesh.setVertexColor(colorData);
        triMesh.updateLegendScheme(meshGraphic.legendScheme);

        return triMesh;
    }

    /**
     * Helper method: get x,y,z of target vertex from interleaved vertex array
     * @param vertices Interleaved vertex array xyzxyz...
     * @param vid Vertex index
     * @return float array [x, y, z]
     */
    public static float[] getVertexXYZ(float[] vertices, int vid) {
        int pos = vid * 3;
        return new float[]{vertices[pos], vertices[pos + 1], vertices[pos + 2]};
    }

    /**
     * Helper method: calculate Euclidean distance between two 3D points
     * @param p1 Point1 [x,y,z]
     * @param p2 Point2 [x,y,z]
     * @return Euclidean distance
     */
    public static double distance(float[] p1, float[] p2) {
        double dx = p1[0] - p2[0];
        double dy = p1[1] - p2[1];
        double dz = p1[2] - p2[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Get extent from vertex position array
     * @param vertexPosition Vertex position array
     * @return
     */
    public static Extent getExtent(float[] vertexPosition) {
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

        return new Extent(minX, maxX, minY, maxY, minZ, maxZ);
    }
}
