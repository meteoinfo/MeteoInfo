package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.PolygonBreak;

import java.util.*;

public interface PolygonalGraphic {

    ColorBreak getLegendBreak();

    boolean isFaceInterp();

    void updateExtent();

    void setExtent(Extent value);

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
     * Set vertex position
     * @param vertexPosition Vertex position
     */
    void setVertexPosition(float[] vertexPosition);

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
    default void tessellate() {
        tessellate(false);
    };

    /**
     * Polygon graphic tesselate to create vertext related float arrays
     * @param subdivide Subdivide or not
     */
    void tessellate(boolean subdivide);

    /**
     * Given the vertex coordinates of a shape this function calculates the
     * normal vector coordinates.
     */
    void calculateNormalVectors(float[] vData);

    /**
     * Get vertex of polygonal lines data
     * @return Vertex data
     */
    float[] getLineVertex();

    /**
     * Set line vertex
     * @param vData Line vertex
     */
    void setLineVertex(float[] vData);

    /**
     * Get line point number list
     * @return Line point number list
     */
    List<Integer> getLinePointNumbers();

    /**
     * Update line vertex
     */
    void updateLineVertex();

    /**
     * Get is fill polyogns or not
     * @return Fill polygons or not
     */
    boolean isFill();

    /**
     * Get is draw outline or not
     * @return Draw outline or not
     */
    boolean isDrawOutline();
}
