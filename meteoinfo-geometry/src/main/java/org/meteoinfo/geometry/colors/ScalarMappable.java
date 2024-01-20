package org.meteoinfo.geometry.colors;

import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.geometry.legend.LegendFactory;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.shape.ShapeTypes;

import java.awt.*;

public class ScalarMappable {
    private ColorMap colorMap;
    private Normalize normalize;

    /**
     * Constructor
     */
    public ScalarMappable() {
        this.normalize = new Normalize();
        this.colorMap = new ColorMap();
    }

    /**
     * Constructor
     * @param colorMap Color map
     * @param normalize Normalize
     */
    public ScalarMappable(ColorMap colorMap, Normalize normalize) {
        this.colorMap = colorMap;
        this.normalize = normalize;
    }

    /**
     * Constructor
     * @param opacityTransferFunction Transfer function
     * @param colorMap Color map
     */
    public ScalarMappable(ColorMap colorMap) {
        this(colorMap, new Normalize());
    }

    /**
     * Get color map
     * @return Color map
     */
    public ColorMap getColorMap() {
        return this.colorMap;
    }

    /**
     * Set color map
     * @param value Color map
     */
    public void setColorMap(ColorMap value) {
        this.colorMap = value;
    }

    /**
     * Get normalize
     * @return Normalize
     */
    public Normalize getNormalize() {
        return this.normalize;
    }

    /**
     * Set normalize
     * @param value Normalize
     */
    public void setNormalize(Normalize value) {
        this.normalize = value;
    }

    /**
     * Get color with a data value
     * @param v The data value
     * @return The color
     */
    public Color getColor(double v) {
        float ratio = this.normalize.apply(v).floatValue();
        return this.colorMap.map(ratio);
    }

    /**
     * Get colors
     * @return Colors
     */
    public Color[] getColors() {
        if (this.normalize instanceof BoundaryNorm) {
            return this.colorMap.getColors(((BoundaryNorm) this.normalize).getNRegions());
        } else {
            return this.colorMap.getColors();
        }
    }

    /**
     * To legend scheme
     * @param min The minimum value
     * @param max The maximum value
     * @param n Legend break number
     * @return Legend scheme
     */
    public LegendScheme toLegendScheme(double min, double max) {
        if (min == max) {
            return LegendFactory.createSingleSymbolLegendScheme(ShapeTypes.IMAGE);
        }

        double[] values = MIMath.getIntervalValues(min, max);
        int n = values.length;
        Color[] colors = new Color[n + 1];
        colors[0] = getColor(min);
        for (int i = 1; i < n + 1; i++) {
            colors[i] = getColor(values[i - 1]);
        }

        LegendScheme ls = LegendFactory.createGraduatedLegendScheme(values, colors, ShapeTypes.IMAGE, min, max);
        ls.setColorMap(colorMap);
        ls.setNormalize(normalize);
        return ls;
    }

    /**
     * To legend scheme
     * @param min The minimum value
     * @param max The maximum value
     * @param n Legend break number
     * @return Legend scheme
     */
    public LegendScheme toLegendScheme(double min, double max, int n) {
        if (min == max) {
            return LegendFactory.createSingleSymbolLegendScheme(ShapeTypes.IMAGE);
        }

        double[] values = MIMath.getIntervalValues(min, max, n);
        Color[] colors = new Color[n + 1];
        colors[0] = getColor(min);
        for (int i = 1; i < n + 1; i++) {
            colors[i] = getColor(values[i - 1]);
        }

        LegendScheme ls = LegendFactory.createGraduatedLegendScheme(values, colors, ShapeTypes.IMAGE, min, max);
        ls.setColorMap(colorMap);
        ls.setNormalize(normalize);
        return ls;
    }
}
