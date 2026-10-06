package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.legend.LegendManage;
import org.meteoinfo.geometry.colors.TransferFunction;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.chart.transform.GeoTransform;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class MeshGraphic extends GraphicCollection3D {
    protected float[] vertexPosition;
    protected float[] vertexValue;
    protected float[] vertexColor;
    protected float[] vertexNormal;
    protected float[] vertexTexture;
    protected int[] vertexIndices;
    protected boolean faceInterp;
    protected boolean edgeInterp;
    protected BufferedImage image;
    protected boolean mesh;
    protected boolean model;

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
     * Set vertex normal
     * @param value Vertex normal
     */
    public void setVertexNormal(float[] value) {
        this.vertexNormal = value;
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
    public abstract int getFaceNumber();


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

        this.extent = new Extent(minX, maxX, minY, maxY, minZ, maxZ);
    }

    @Override
    public void setLegendScheme(LegendScheme ls) {
        super.setLegendScheme(ls);
        updateVertexColor();
    }

    /**
     * Only update legend scheme
     * @param ls Legend scheme
     */
    public void updateLegendScheme(LegendScheme ls) {
        this.legendScheme = ls;
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
    public abstract void updateVertexTexture();

    /**
     * Calculate vertex normal vectors
     * @param vData Vertex position
     */
    public abstract void calculateNormalVectors(float[] vData);

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
