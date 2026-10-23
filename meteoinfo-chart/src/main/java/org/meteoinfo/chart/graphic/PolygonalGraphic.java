package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;

import java.util.*;

public interface PolygonalGraphic {

    /**
     * Get vertex number
     * @return Vertex number
     */
    int getVertexNumber();

    /**
     * Get vertex position
     * @return Vertex position
     */
    float[] getVertexPosition();

    /**
     * Get vertex indices
     * @return Vertex indices
     */
    int[] getVertexIndices();

    /**
     * Get vertex normal
     * @return Vertex normal
     */
    float[] getVertexNormal();

    /**
     * Get vertex color data
     * @return Vertex color data
     */
    float[] getVertexColor();

    /**
     * Get if tessellate dirty
     * @return Tessellate dirty or not
     */
    boolean isTessellateDirty();

    /**
     * Polygon graphic tesselate to create vertext related float arrays
     */
    void tessellate();

    /**
     * Given the vertex coordinates of a shape this function calculates the
     * normal vector coordinates.
     */
    void calculateNormalVectors(float[] vData);
}
