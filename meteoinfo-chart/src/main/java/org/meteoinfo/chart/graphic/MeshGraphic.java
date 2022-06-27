package org.meteoinfo.chart.graphic;

import org.meteoinfo.chart.jogl.JOGLUtil;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.legend.ColorBreak;

import java.awt.*;

public class MeshGraphic extends GraphicCollection3D {
    private float[] vertexData;
    private float[] vertexNormal;

    /**
     * Constructor
     */
    public MeshGraphic() {
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
     * Get triangle number
     * @return Triangle number
     */
    public int getTriangleNumber() {
        return getVertexNumber() / 3;
    }

    /**
     * Get color
     * @return The color
     */
    public Color getColor() {
        return this.legendBreak.getColor();
    }

    /**
     * Set color
     * @param color The color
     */
    public void setColor(Color color) {
        this.legendBreak.setColor(color);
    }

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

        this._extent = new Extent3D(minX, maxX, minY, maxY, minZ, maxZ);
    }

    /**
     * Given the vertex coordinates of a shape this function calculates the
     * normal vector coordinates.
     */
    public void calculateNormalVectors(float[] vData) {
        // Temporary storage for the normal vector coordinates
        // there's one temporary array for each dimension.
        int n = vData.length / 3;
        float[] nx = new float[n];
        float[] ny = new float[n];
        float[] nz = new float[n];

        // Temporary storage for the vertex coordinates
        // there's one temporary array for each dimension.
        float[] x = new float[n];
        float[] y = new float[n];
        float[] z = new float[n];

        // Load the coordinate values into their respective arrays.
        int counter = 0;
        for (int i = 0; i < n; i++) {
            x[i] = vData[counter];
            counter++;
            y[i] = vData[counter];
            counter++;
            z[i] = vData[counter];
            counter++;
        }

        // Compute normals for each vertex.
        for (int i = 0; i < n; i += 3) {
            float[] normal = JOGLUtil.normalize(new float[]{x[i], y[i], z[i]}, new float[]{x[i+1], y[i+1], z[i+1]},
                    new float[]{x[i+2], y[i+2], z[i+2]});

            nx[i] += normal[0];
            ny[i] += normal[1];
            nz[i] += normal[2];

            nx[i + 1] += normal[0];
            ny[i + 1] += normal[1];
            nz[i + 1] += normal[2];

            nx[i + 2] += normal[0];
            ny[i + 2] += normal[1];
            nz[i + 2] += normal[2];
        }

        // Copy the data for the normal vectors from the temporary arrays into
        // the permanent normalArray vector.
        vertexNormal = new float[n * 3];
        counter = 0;
        for (int i = 0; i < n; i++) {
            vertexNormal[counter] = nx[i];
            counter++;
            vertexNormal[counter] = ny[i];
            counter++;
            vertexNormal[counter] = nz[i];
            counter++;
        }
    }
}
