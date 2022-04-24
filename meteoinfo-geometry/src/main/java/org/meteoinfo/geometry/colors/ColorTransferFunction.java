package org.meteoinfo.geometry.colors;

import org.meteoinfo.common.colors.ColorMap;

import java.awt.*;

public class ColorTransferFunction {
    private TransferFunction transferFunction;
    private ColorMap colorMap;
    private Normalize normalize;

    /**
     * Constructor
     * @param transferFunction Transfer function
     * @param colorMap Color map
     * @param normalize Normalize
     */
    public ColorTransferFunction(TransferFunction transferFunction, ColorMap colorMap, Normalize normalize) {
        this.transferFunction = transferFunction;
        this.colorMap = colorMap;
        this.normalize = normalize;
    }

    /**
     * Constructor
     * @param transferFunction Transfer function
     * @param colorMap Color map
     */
    public ColorTransferFunction(TransferFunction transferFunction, ColorMap colorMap) {
        this(transferFunction, colorMap, new Normalize());
    }

    /**
     * Constructor
     * @param colorMap Color map
     * @param normalize Normalize
     */
    public ColorTransferFunction(ColorMap colorMap, Normalize normalize) {
        this(new TransferFunction(), colorMap, normalize);
    }

    /**
     * Constructor
     * @param colorMap Color map
     */
    public ColorTransferFunction(ColorMap colorMap) {
        this(colorMap, new Normalize());
    }

    /**
     * Get transfer function
     * @return Transfer function
     */
    public TransferFunction getTransferFunction() {
        return this.transferFunction;
    }

    /**
     * Set transfer function
     * @param value Transfer function
     */
    public void setTransferFunction(TransferFunction value) {
        this.transferFunction = value;
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
        Color c = this.colorMap.getColor(ratio);
        float alpha = this.transferFunction.getOpacity(ratio);

        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) alpha * 255);
    }
}
