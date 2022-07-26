package org.meteoinfo.geometry.colors;

import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.geometry.legend.LegendFactory;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.shape.ShapeTypes;

import java.awt.*;

public class TransferFunction {
    private OpacityTransferFunction opacityTransferFunction;
    private ColorMap colorMap;
    private Normalize normalize;

    /**
     * Constructor
     */
    public TransferFunction() {
        this.opacityTransferFunction = new OpacityTransferFunction();
        this.normalize = new Normalize();
        this.colorMap = new ColorMap();
    }

    /**
     * Constructor
     * @param opacityTransferFunction Transfer function
     * @param colorMap Color map
     * @param normalize Normalize
     */
    public TransferFunction(OpacityTransferFunction opacityTransferFunction, ColorMap colorMap, Normalize normalize) {
        this.opacityTransferFunction = opacityTransferFunction;
        this.colorMap = colorMap;
        this.normalize = normalize;
    }

    /**
     * Constructor
     * @param opacityTransferFunction Transfer function
     * @param colorMap Color map
     */
    public TransferFunction(OpacityTransferFunction opacityTransferFunction, ColorMap colorMap) {
        this(opacityTransferFunction, colorMap, new Normalize());
    }

    /**
     * Constructor
     * @param colorMap Color map
     * @param normalize Normalize
     */
    public TransferFunction(ColorMap colorMap, Normalize normalize) {
        this(new OpacityTransferFunction(), colorMap, normalize);
    }

    /**
     * Constructor
     * @param colorMap Color map
     */
    public TransferFunction(ColorMap colorMap) {
        this(colorMap, new Normalize());
    }

    /**
     * Get opacity transfer function
     * @return Opacity transfer function
     */
    public OpacityTransferFunction getOpacityTransferFunction() {
        return this.opacityTransferFunction;
    }

    /**
     * Set opacity transfer function
     * @param value Opacity transfer function
     */
    public void setOpacityTransferFunction(OpacityTransferFunction value) {
        this.opacityTransferFunction = value;
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
        Color c = this.colorMap.map(ratio);
        float alpha = this.opacityTransferFunction.getOpacity(ratio);

        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255));
    }

    /**
     * To legend scheme
     * @param min The minimum value
     * @param max The maximum value
     * @param n Legend break number
     * @return Legend scheme
     */
    public LegendScheme toLegendScheme(double min, double max) {
        double[] values = MIMath.getIntervalValues(min, max);
        int n = values.length;
        Color[] colors = new Color[n + 1];
        colors[0] = getColor(min);
        for (int i = 1; i < n; i++) {
            colors[i] = getColor(values[i - 1]);
        }

        return LegendFactory.createGraduatedLegendScheme(values, colors, ShapeTypes.IMAGE, min, max);
    }

    /**
     * To legend scheme
     * @param min The minimum value
     * @param max The maximum value
     * @param n Legend break number
     * @return Legend scheme
     */
    public LegendScheme toLegendScheme(double min, double max, int n) {
        double[] values = MIMath.getIntervalValues(min, max, n);
        Color[] colors = new Color[n + 1];
        colors[0] = getColor(min);
        for (int i = 1; i < n; i++) {
            colors[i] = getColor(values[i - 1]);
        }

        return LegendFactory.createGraduatedLegendScheme(values, colors, ShapeTypes.IMAGE, min, max);
    }
}
