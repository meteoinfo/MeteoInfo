package org.meteoinfo.geometry.colors;

import org.meteoinfo.common.colors.ColorMap;
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
}
