package org.meteoinfo.chart.graphic;

import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.legend.ColorBreak;

import java.util.Arrays;

public class SurfaceGraphic extends GraphicCollection3D {
    private float[] vertexData;
    private float[] values;
    private float[] colorData;
    private float[] vertexNormal;

    /**
     * Constructor
     */
    public SurfaceGraphic() {
        super();
    }

    /**
     * Get vertex data
     * @return Vertex data
     */
    public float[] getVertexData() {
        return vertexData;
    }

    /**
     * Get vertex data
     * @param transform The transform
     * @return Vertex data
     */
    public float[] getVertexData(Transform transform) {
        int n = vertexData.length;
        float[] vData = new float[n];
        for (int i = 0; i < n; i+=3) {
            vData[i] = transform.transform_x(vertexData[i]);
            vData[i + 1] = transform.transform_y(vertexData[i + 1]);
            vData[i + 2] = transform.transform_z(vertexData[i + 2]);
        }

        return vData;
    }

    /**
     * Set vertex data
     * @param value Vertex data
     */
    public void setVertexData(float[] value) {
        vertexData = value;
        updateExtent();
        //calculateNormalVectors(vertexData);
    }

    /**
     * Get vertex values
     * @return Vertex values
     */
    public float[] getValues() {
        return this.values;
    }

    /**
     * Set vertex values
     * @param value Vertex values
     */
    public void setValues(float[] value) {
        this.values = value;
    }

    /**
     * Color data
     * @return Color data
     */
    public float[] getColorData() {
        return colorData;
    }

    /**
     * Get vertex normal
     * @return Vertex normal
     */
    public float[] getVertexNormal() {
        return vertexNormal;
    }

    /**
     * Get vertex number
     * @return Vertex number
     */
    public int getVertexNumber() {
        return vertexData.length / 3;
    }

    /**
     * Update extent
     */
    public void updateExtent() {
        float x, y, z;
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE, minY = minX, maxY = maxX,
                minZ = minX, maxZ = maxX;
        int idx = 0;
        for (int i = 0; i < vertexData.length; i+=3) {
            x = vertexData[i];
            y = vertexData[i + 1];
            z = vertexData[i + 2];
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

        this._extent = new Extent3D(minX, maxX, minY, maxX, minZ, maxZ);
    }

    /**
     * Update color data
     */
    public void updateColorData() {
        if (values != null && legendScheme != null) {
            colorData = new float[getVertexNumber() * 4];
            float[] color;
            for (int i = 0; i < colorData.length; i+=4) {
                color = legendScheme.findLegendBreak(values[i]).getColor().getColorComponents(null);
                System.arraycopy(color, 0, colorData, i, 4);
            }
        }
    }
}
