package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendManage;
import org.meteoinfo.geometry.colors.TransferFunction;
import org.meteoinfo.geometry.graphic.GraphicCollection3D;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.projection.GeoTransform;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MeshGraphic extends GraphicCollection3D {
    private float[] vertexPosition;
    private float[] vertexValue;
    private float[] vertexColor;
    private float[] vertexNormal;
    private float[] vertexTexture;
    private int[] vertexIndices;
    private int rows;
    private int columns;
    private boolean faceInterp;
    private boolean edgeInterp;
    private BufferedImage image;
    private boolean mesh;

    /**
     * Constructor
     */
    public MeshGraphic() {
        super();

        faceInterp = false;
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
     * Get whether using texture
     * @return Whether using texture
     */
    public boolean isUsingTexture() {
        return this.image != null;
    }

    /**
     * Get vertex texture
     * @return Vertex texture
     */
    public float[] getVertexTexture() {
        return this.vertexTexture;
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
     * Get image
     * @return The image
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Set image
     * @param value The image
     */
    public void setImage(BufferedImage value) {
        this.image = value;
        updateVertexTexture();
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
        return (rows - 1) * (columns - 1);
    }

    /**
     * Update extent
     */
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

        this.extent = new Extent3D(minX, maxX, minY, maxY, minZ, maxZ);
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

    @Override
    public void setLegendScheme(LegendScheme ls) {
        super.setLegendScheme(ls);
        updateVertexColor();
    }

    /**
     * Update vertex color data
     */
    public void updateVertexColor() {
        if (vertexValue != null && legendScheme != null) {
            vertexColor = new float[getVertexNumber() * 4];
            float[] color;
            for (int i = 0; i < vertexValue.length; i++) {
                if (Float.isNaN(vertexValue[i])) {
                    color = legendScheme.getLegendBreak(0).getColor().getRGBComponents(null);
                } else {
                    color = legendScheme.findLegendBreakAlways(vertexValue[i]).getColor().getRGBComponents(null);
                }
                System.arraycopy(color, 0, vertexColor, i * 4, 4);
            }
        }
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
     * Update vertex texture data
     */
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

    public Color getColor() {
        return Color.red;
    }

    /**
     * Transform the graphic
     */
    @Override
    public void doTransform() {
        if (this.transform != null && this.transform.isValid()) {
            if (this.transform instanceof GeoTransform) {
                GeoTransform geoTransform = (GeoTransform) this.transform;
                GraphicProjectionUtil.projectClipGraphic(this, geoTransform.getSourceProj(),
                        geoTransform.getTargetProj());
            }
        }
    }
}
