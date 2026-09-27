package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.legend.LegendManage;
import org.meteoinfo.geometry.colors.TransferFunction;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.chart.transform.GeoTransform;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class QuadMeshGraphic extends MeshGraphic {
    private int rows;
    private int columns;

    /**
     * Constructor
     */
    public QuadMeshGraphic() {
        super();
    }

    /**
     * Set vertex position
     * @param value Vertex position
     * @param rows Row number
     */
    public void setVertexPosition(float[] value, int rows) {
        this.setVertexPosition(value);

        this.setRows(rows);
        updateVertexIndices();
    }

    /**
     * Get row number
     * @return Row number
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Set row number
     * @param value Row number
     */
    public void setRows(int value) {
        this.rows = value;
        this.columns = this.getVertexNumber() / value;
        calculateNormalVectors(vertexPosition);
    }

    /**
     * Get column number
     * @return Column number
     */
    public int getColumns() {
        return this.columns;
    }

    /**
     * Set column number
     * @param value Column number
     */
    public void setColumns(int value) {
        this.columns = value;
        this.rows = this.getVertexNumber() / value;
        calculateNormalVectors(vertexPosition);
    }

    /**
     * Get face number
     * @return Face number
     */
    @Override
    public int getFaceNumber() {
        return (rows - 1) * (columns - 1);
    }

    /**
     * Update vertex indices
     */
    public void updateVertexIndices() {
        int n = (rows - 1) * (columns - 1) * 4;
        vertexIndices = new int[n];
        int idx, vIdx;
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < columns - 1; j++) {
                vIdx = i * columns + j;
                idx = (i * (columns - 1) + j) * 4;
                vertexIndices[idx] = vIdx;
                vertexIndices[idx + 1] = vIdx + 1;
                vertexIndices[idx + 2] = vIdx + 1 + columns;
                vertexIndices[idx + 3] = vIdx + columns;
            }
        }
    }

    /**
     * Update vertex texture data
     */
    @Override
    public void updateVertexTexture() {
        vertexTexture = new float[getVertexNumber() * 2];
        int idx;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                idx = (i * columns + j) * 2;
                vertexTexture[idx] = (float) j / (columns - 1);
                vertexTexture[idx + 1] = (float) i / (rows - 1);
            }
        }
    }

    /**
     * Get vertex
     * @param vData Vertex array
     * @param row Row index
     * @param col Column index
     * @return Vertex
     */
    public Vector3f getVertex(float[] vData, int row, int col) {
        int idx = (row * this.columns + col) * 3;
        return new Vector3f(vData[idx], vData[idx + 1], vData[idx + 2]);
    }

    /**
     * Get vertex
     * @param row Row index
     * @param col Column index
     * @return Vertex
     */
    public Vector3f getVertex(int row, int col) {
        return getVertex(this.vertexPosition, row, col);
    }

    /**
     * Calculate vertex normal vectors
     * @param vData Vertex position
     */
    public void calculateNormalVectors(float[] vData) {
        int n = this.getVertexNumber();
        this.vertexNormal = new float[n * 3];
        Vector3f v, left, right, up, down;
        Vector3f normal, nLeftUp, nLeftDown, nRightUp, nRightDown;
        int idx;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                idx = (i * columns + j) * 3;
                v = new Vector3f(vData[idx], vData[idx + 1], vData[idx + 2]);
                left = j > 0 ? getVertex(vData, i, j - 1) : null;
                right = j < columns - 1 ? getVertex(vData, i, j + 1) : null;
                down = i > 0 ? getVertex(vData, i - 1, j) : null;
                up = i < rows - 1 ? getVertex(vData, i + 1, j) : null;
                nLeftUp = (left == null || up == null) ? new Vector3f() :
                        left.sub(v, new Vector3f()).cross(up.sub(v, new Vector3f()));
                nLeftDown = (left == null || down == null) ? new Vector3f() :
                        down.sub(v, new Vector3f()).cross(left.sub(v, new Vector3f()));
                nRightUp = (right == null || up == null) ? new Vector3f() :
                        up.sub(v, new Vector3f()).cross(right.sub(v, new Vector3f()));
                nRightDown = (right == null || down == null) ? new Vector3f() :
                        right.sub(v, new Vector3f()).cross(down.sub(v, new Vector3f()));
                normal = nLeftUp.add(nLeftDown).add(nRightUp).add(nRightDown).normalize();
                normal.negate();

                vertexNormal[idx] = normal.x;
                vertexNormal[idx + 1] = normal.y;
                vertexNormal[idx + 2] = normal.z;
            }
        }
    }

    /**
     * Convert to tri mesh graphic
     * @return Tri mesh graphic
     */
    public TriMeshGraphic toTriMeshGraphic() {
        return toTriMeshGraphic(true);
    }

    /**
     * Convert to tri mesh graphic
     * @param chooseShortestDiag If true, select the shorter diagonal to split quad, avoid poorly shaped triangles
     * @return Tri mesh graphic
     */
    public TriMeshGraphic toTriMeshGraphic(boolean chooseShortestDiag) {
        // ========== Read source mesh data ==========
        int[] quads = this.getVertexIndices();           // Quad index array: every 4 integers form one quad [a,b,c,d]
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
                float[] va = GraphicUtil.getVertexXYZ(vertexPosition, a);
                float[] vb = GraphicUtil.getVertexXYZ(vertexPosition, b);
                float[] vc = GraphicUtil.getVertexXYZ(vertexPosition, c);
                float[] vd = GraphicUtil.getVertexXYZ(vertexPosition, d);
                double diagAC = GraphicUtil.distance(va, vc);
                double diagBD = GraphicUtil.distance(vb, vd);

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
        triMesh.vertexIndices = triFaces;
        triMesh.vertexNormal = this.vertexNormal;
        triMesh.vertexPosition = this.vertexPosition;
        triMesh.vertexColor = this.vertexColor;
        triMesh.vertexTexture = this.vertexTexture;
        triMesh.image = this.image;
        triMesh.legendScheme = this.legendScheme;
        triMesh.extent = this.extent;

        return triMesh;
    }
}
