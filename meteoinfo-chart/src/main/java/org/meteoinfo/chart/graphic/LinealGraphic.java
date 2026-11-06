package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.legend.ColorBreak;

import java.util.List;

public interface LinealGraphic {

    ColorBreak getLegendBreak();

    /**
     * Get vertex position
     * @return Vertex position
     */
    float[] getVertexPosition();

    /**
     * Get vertex color data
     * @return Vertex color data
     */
    float[] getVertexColor();

    /**
     * Get line point number list
     * @return Line point number list
     */
    List<Integer> getLinePointNumbers();

    /**
     * Update vertex position and color arrays
     */
    void updateVertex();
}
