package org.meteoinfo.geometry.colors;

import org.meteoinfo.geometry.colors.Normalize;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OpacityTransferFunction {
    private float[] opacityLevels;
    private float[] opacityNodes;

    /**
     * Constructor
     */
    public OpacityTransferFunction() {
        opacityLevels = new float[]{0.0f, 1.0f};
        opacityNodes = new float[]{0.0f, 1.0f};
    }

    /**
     * Constructor
     * @param opacityNodes Opacity nodes
     * @param opacityLevels Opacity levels
     */
    public OpacityTransferFunction(List<Number> opacityNodes, List<Number> opacityLevels) {
        this.opacityNodes = new float[opacityNodes.size()];
        this.opacityLevels = new float[opacityLevels.size()];
        for (int i = 0; i < this.opacityNodes.length; i++) {
            this.opacityNodes[i] = opacityNodes.get(i).floatValue();
            this.opacityLevels[i] = opacityLevels.get(i).floatValue();
        }
    }

    /**
     * Constructor
     * @param opacityNodes Opacity nodes
     * @param opacityLevels Opacity levels
     */
    public OpacityTransferFunction(List<Number> opacityNodes, List<Number> opacityLevels, Normalize normalize) {
        int n = opacityLevels.size();
        this.opacityNodes = new float[n];
        this.opacityLevels = new float[n];
        if (opacityNodes == null) {
            for (int i = 0; i < n; i++) {
                this.opacityNodes[i] = (float) i / (n - 1);
                this.opacityLevels[i] = opacityLevels.get(i).floatValue();
            }
        } else {
            for (int i = 0; i < n; i++) {
                this.opacityNodes[i] = normalize.apply(opacityNodes.get(i).doubleValue()).floatValue();
                this.opacityLevels[i] = opacityLevels.get(i).floatValue();
            }
        }
    }

    /**
     * Get opacity nodes
     * @return Opacity nodes
     */
    public float[] getOpacityNodes() {
        return opacityNodes;
    }

    /**
     * Set opacity nodes
     * @param opacityNodes Opacity nodes
     */
    public void setOpacityNodes(float[] opacityNodes) {
        this.opacityNodes = opacityNodes;
    }

    /**
     * Get opacity levels
     * @return Opacity levels
     */
    public float[] getOpacityLevels() {
        return opacityLevels;
    }

    /**
     * Set opacity levels
     * @param opacityLevels Opacity levels
     */
    public void setOpacityLevels(float[] opacityLevels) {
        this.opacityLevels = opacityLevels;
    }

    public int getNodeIndex(float node) {
        int idx = -1;
        for (int i = 0; i < opacityNodes.length; i++) {
            if (node < opacityNodes[i]) {
                idx = i;
                break;
            }
        }
        if (idx == -1)
            idx = opacityNodes.length;

        return idx;
    }

    /**
     * Get opacity node
     * @param i Index
     * @return Opacity node
     */
    public float getNode(int i) {
        return opacityNodes[i];
    }

    /**
     * Get opacity level
     * @param i Index
     * @return Opacity level
     */
    public float getLevel(int i) {
        return opacityLevels[i];
    }

    /**
     * Get opacity levels size
     * @return Opacity levels size
     */
    public int size() {
        return this.opacityLevels.length;
    }

    /**
     * Get opacity
     * @param nodeValue The node value
     * @return Opacity
     */
    public float getOpacity(float nodeValue) {
        int idx = getNodeIndex(nodeValue);
        int n = size();
        if (idx == 0) {
            return opacityLevels[idx];
        } else if (idx == n) {
            return opacityLevels[n - 1];
        } else {
            float node1 = opacityNodes[idx - 1];
            float node2 = opacityNodes[idx];
            float level1 = opacityLevels[idx - 1];
            float level2 = opacityLevels[idx];
            float level = level1 + (nodeValue - node1) / (node2 - node1) * (level2 - level1);
            return level;
        }
    }

    /**
     * Get colors
     * @param colors Origin colors
     * @return New colors
     */
    public byte[] getColors(Color[] colors) {
        int n = colors.length;
        byte[] nColors = new byte[n * 4];
        float node, a;
        Color color;
        int r, g, b;
        for (int i = 0; i < n; i++) {
            node = (float) i / (float) (n - 1);
            a = getOpacity(node);
            color = colors[i];
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
            nColors[i * 4 + 0] = (byte) r;
            nColors[i * 4 + 1] = (byte) g;
            nColors[i * 4 + 2] = (byte) b;
            nColors[i * 4 + 3] = (byte) Math.round(a * 255);
        }

        return nColors;
    }

    /**
     * Get colors
     * @param colors Origin colors
     * @return New colors
     */
    public byte[] getColors(byte[] colors) {
        int n = colors.length / 3;
        byte[] nColors = new byte[n * 4];
        float node, a;

        float r, g, b;
        for (int i = 0; i < n; i++) {
            node = (float) i / (float) n;
            a = getOpacity(node);

            r = ((float) Byte.toUnsignedInt(colors[i * 3 + 0])) / 255;
            g = ((float) Byte.toUnsignedInt(colors[i * 3 + 1])) / 255;
            b = ((float) Byte.toUnsignedInt(colors[i * 3 + 2])) / 255;

            r = r * r * a;
            g = g * g * a;
            b = b * b * a;

            nColors[i * 4 + 0] = (byte) Math.round(r * 255);
            nColors[i * 4 + 1] = (byte) Math.round(g * 255);
            nColors[i * 4 + 2] = (byte) Math.round(b * 255);
            nColors[i * 4 + 3] = (byte) Math.round(a * 255);
        }

        return nColors;
    }
}
