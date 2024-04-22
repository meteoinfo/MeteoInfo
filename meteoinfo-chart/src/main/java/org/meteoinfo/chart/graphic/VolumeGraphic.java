package org.meteoinfo.chart.graphic;

import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.common.Extent;
import org.meteoinfo.geometry.colors.OpacityTransferFunction;
import org.meteoinfo.chart.render.jogl.RayCastingType;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.geometry.legend.LegendManage;
import org.meteoinfo.geometry.colors.Normalize;
import org.meteoinfo.geometry.colors.TransferFunction;
import org.meteoinfo.geometry.graphic.GraphicCollection3D;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.ndarray.Array;

import java.awt.*;
import java.util.List;

import static org.joml.Math.clamp;

public class VolumeGraphic extends GraphicCollection3D {
    final int width;
    final int height;
    final int depth;
    private Array data;
    final byte[] byteData;
    private byte[] normals;
    final float[] scale = new float[]{1, 1, 1};
    private byte[] colors;
    private byte[] originalColors;
    private TransferFunction transferFunction = new TransferFunction();
    private float[] opacityLevels = new float[]{0, 1};
    private float[] opacityNodes = new float[]{0f, 1f};
    private float[] colorRange = new float[]{0f, 1f};
    private float[] aabbMin = new float[]{-1, -1, -1};
    private float[] aabbMax = new float[]{1, 1, 1};
    private RayCastingType rayCastingType = RayCastingType.MAX_VALUE;
    private float brightness = 1.0f;

    boolean hasChanges = true;

    /**
     * Constructor
     * @param data Byte data
     * @param width 3D texture width
     * @param height 3D texture height
     * @param depth 3D texture depth
     * @param colors Colors byte array
     */
    public VolumeGraphic(byte[] data, int width, int height, int depth, byte[] colors) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.byteData = data;
        //this.buffer = Buffers.newDirectByteBuffer(this.data);
        this.colors = colors;

        Extent3D extent = new Extent3D();
        extent.maxX = width;
        extent.maxY = height;
        extent.maxZ = depth;
        this.setExtent(extent);
    }

    /**
     * Constructor
     * @param value Value array - 3D
     * @param colorMap Color map
     * @param vMin Minimum value
     * @param vMax Maximum value
     */
    public VolumeGraphic(Array value, ColorMap colorMap, double vMin, double vMax) {
        this.transferFunction.setColorMap(colorMap);

        value = value.copyIfView();
        this.data = value;
        int[] shape = value.getShape();
        this.depth = shape[0];
        this.height = shape[1];
        this.width = shape[2];
        this.byteData = new byte[width * height * depth];
        double range = vMax - vMin;
        for (int i = 0; i < value.getSize(); i++) {
            if (Double.isNaN(value.getDouble(i))) {
                byteData[i] = 0;
            } else {
                byteData[i] = (byte) ((int) ((value.getDouble(i) - vMin) / range * 255));
            }
        }
        //buffer = Buffers.newDirectByteBuffer(data);

        Color[] oColors = colorMap.getColors();
        int n = oColors.length;
        originalColors = new byte[n * 3];
        for (int i = 0; i < n; i++) {
            int color = oColors[i].getRGB();
            originalColors[i * 3 + 0] = (byte) ((color >> 16) & 0xff);
            originalColors[i * 3 + 1] = (byte) ((color >> 8) & 0xff);
            originalColors[i * 3 + 2] = (byte) ((color) & 0xff);
        }

        double[] values = MIMath.getIntervalValues(vMin, vMax, n - 1);
        LegendScheme ls = LegendManage.createGraduatedLegendScheme(values, oColors, ShapeTypes.POLYGON, vMin, vMax);
        this.setLegendScheme(ls);
        this.setSingleLegend(false);
    }

    /**
     * Constructor
     * @param value Value array - 3D
     * @param colorMap Color map
     * @param norm Normalize
     */
    public VolumeGraphic(Array value, ColorMap colorMap, Normalize norm) {
        value = value.copyIfView();
        this.data = value;
        int[] shape = value.getShape();
        this.depth = shape[0];
        this.height = shape[1];
        this.width = shape[2];
        this.byteData = new byte[width * height * depth];
        for (int i = 0; i < value.getSize(); i++) {
            if (Double.isNaN(value.getDouble(i))) {
                byteData[i] = 0;
            } else {
                byteData[i] = (byte) ((int) (norm.apply(value.getDouble(i)).floatValue() * 255));
            }
        }
        //buffer = Buffers.newDirectByteBuffer(data);

        setColorMap(colorMap, norm);
    }

    /**
     * Constructor
     * @param value Value array - 3D
     * @param ls LegendScheme
     */
    public VolumeGraphic(Array value, LegendScheme ls) {
        value = value.copyIfView();
        this.data = value;
        int[] shape = value.getShape();
        this.depth = shape[0];
        this.height = shape[1];
        this.width = shape[2];
        this.byteData = new byte[width * height * depth];
        List<Color> oColors = ls.getColors();
        int n = oColors.size();
        for (int i = 0; i < value.getSize(); i++) {
            if (Double.isNaN(value.getDouble(i))) {
                byteData[i] = 0;
            } else {
                byteData[i] = (byte) ((int) (ls.legendBreakIndex(value.getDouble(i)) * 255.0 / n));
            }
        }
        //buffer = Buffers.newDirectByteBuffer(data);

        originalColors = new byte[n * 3];
        for (int i = 0; i < n; i++) {
            int color = oColors.get(i).getRGB();
            originalColors[i * 3 + 0] = (byte) ((color >> 16) & 0xff);
            originalColors[i * 3 + 1] = (byte) ((color >> 8) & 0xff);
            originalColors[i * 3 + 2] = (byte) ((color) & 0xff);
        }

        this.setLegendScheme(ls);
        this.setSingleLegend(false);
    }

    @Override
    public void setExtent(Extent value) {
        super.setExtent(value);

        Extent3D extent = (Extent3D) this.getExtent();
        float xMin = (float) extent.minX;
        float xMax = (float) extent.maxX;
        float yMin = (float) extent.minY;
        float yMax = (float) extent.maxY;
        float zMin = (float) extent.minZ;
        float zMax = (float) extent.maxZ;
        float[] p0 = new float[]{xMin, yMin, zMin};
        float[] p1 = new float[]{xMax, yMin, zMin};
        float[] p2 = new float[]{xMax, yMax, zMin};
        float[] p3 = new float[]{xMin, yMax, zMin};
        float[] p4 = new float[]{xMin, yMin, zMax};
        float[] p5 = new float[]{xMax, yMin, zMax};
        float[] p6 = new float[]{xMax, yMax, zMax};
        float[] p7 = new float[]{xMin, yMax, zMax};
        this.aabbMin = p0;
        this.aabbMax = p6;
        this.vertexBufferData = new float[] {
                p0[0], p0[1], p0[2], // triangle 1 : begin
                p1[0], p1[1], p1[2],
                p4[0], p4[1], p4[2], // triangle 1 : end
                p4[0], p4[1], p4[2], // triangle 2 : begin
                p5[0], p5[1], p5[2],
                p1[0], p1[1], p1[2], // triangle 2 : end
                p1[0], p1[1], p1[2],
                p5[0], p5[1], p5[2],
                p2[0], p2[1], p2[2], //3
                p5[0], p5[1], p5[2],
                p2[0], p2[1], p2[2],
                p6[0], p6[1], p6[2], //4
                p2[0], p2[1], p2[2],
                p6[0], p6[1], p6[2],
                p3[0], p3[1], p3[2], //5
                p6[0], p6[1], p6[2],
                p3[0], p3[1], p3[2],
                p7[0], p7[1], p7[2], //6
                p3[0], p3[1], p3[2],
                p7[0], p7[1], p7[2],
                p0[0], p0[1], p0[2], //7
                p7[0], p7[1], p7[2],
                p0[0], p0[1], p0[2],
                p4[0], p4[1], p4[2], //8
                p0[0], p0[1], p0[2],
                p1[0], p1[1], p1[2],
                p3[0], p3[1], p3[2], //9
                p1[0], p1[1], p1[2],
                p3[0], p3[1], p3[2],
                p2[0], p2[1], p2[2], //10
                p4[0], p4[1], p4[2],
                p5[0], p5[1], p5[2],
                p7[0], p7[1], p7[2], //11
                p5[0], p5[1], p5[2],
                p7[0], p7[1], p7[2],
                p6[0], p6[1], p6[2], //12
        };
    }

    /**
     * Set color map
     * @param colorMap Color map
     */
    public void setColorMap(ColorMap colorMap) {
        setColorMap(colorMap, this.transferFunction.getNormalize());
    }

    /**
     * Set color map
     * @param colorMap Color map
     * @param norm Normalize
     */
    public void setColorMap(ColorMap colorMap, Normalize norm) {
        this.transferFunction.setColorMap(colorMap);
        this.transferFunction.setNormalize(norm);

        Color[] oColors = colorMap.getColors();
        int n = oColors.length;
        originalColors = new byte[n * 3];
        for (int i = 0; i < n; i++) {
            int color = oColors[i].getRGB();
            originalColors[i * 3 + 0] = (byte) ((color >> 16) & 0xff);
            originalColors[i * 3 + 1] = (byte) ((color >> 8) & 0xff);
            originalColors[i * 3 + 2] = (byte) ((color) & 0xff);
        }

        double[] values = MIMath.getIntervalValues(norm.getMinValue(), norm.getMaxValue(), n - 1);
        LegendScheme ls = LegendManage.createGraduatedLegendScheme(values, oColors, ShapeTypes.POLYGON,
                norm.getMinValue(), norm.getMaxValue());
        ls.setColorMap(colorMap);
        ls.setNormalize(norm);
        this.setLegendScheme(ls);
        this.setSingleLegend(false);
    }

    public void updateColors() {
        final float cRange = colorRange[1] - colorRange[0];
        final float min = opacityLevels[0] * opacityLevels[0];
        final float max = opacityLevels[1] * opacityLevels[1];
        final float opacityNodeRange = opacityNodes[1] - opacityNodes[0];
        int n = this.originalColors.length / 3;
        colors = new byte[n * 4];
        for (int i = 0; i < n; i++) {
            float px = ((float) i) / n;
            float a;
            a = this.transferFunction.getOpacityTransferFunction().getOpacity(px);
            /*if (px <= opacityNodes[0]) {
                a = opacityNodes[0];
            } else if (px > opacityNodes[1]) {
                a = opacityNodes[1];
            } else {
                final float ratio = (px - opacityNodes[0]) / opacityNodeRange;
                a = (min * (1 - ratio) + max * ratio);
            }*/
            int colorI = 0;
            if (px > colorRange[1] * 255) {
                colorI = 255;
            } else if (px > colorRange[0]) {
                colorI = clamp(0, 255, Math.round(((((float) i) / 255) - colorRange[0]) * (1f / cRange) * 255f));
            }
            float r = ((float) Byte.toUnsignedInt(originalColors[colorI * 3 + 0])) / 255;
            float g = ((float) Byte.toUnsignedInt(originalColors[colorI * 3 + 1])) / 255;
            float b = ((float) Byte.toUnsignedInt(originalColors[colorI * 3 + 2])) / 255;

            r = r * r * a;
            g = g * g * a;
            b = b * b * a;

            colors[i * 4 + 0] = (byte) Math.round(r * 255);
            colors[i * 4 + 1] = (byte) Math.round(g * 255);
            colors[i * 4 + 2] = (byte) Math.round(b * 255);
            colors[i * 4 + 3] = (byte) Math.round(a * 255);

            Color color = this.legendScheme.getLegendBreak(i).getColor();
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 255));
            this.legendScheme.getLegendBreak(i).setColor(color);
        }
    }

    /**
     * Get data array
     * @return Data array
     */
    public Array getData() {
        return this.data;
    }

    /**
     * Get width
     * @return Width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get height
     * @return Height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get depth
     * @return Depth
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Get data array
     * @return Data array
     */
    public byte[] getByteData() {
        return this.byteData;
    }

    /**
     * Get normals
     * @return Normals
     */
    public byte[] getNormals() {
        if (this.normals == null) {
            this.calculateNormals();
        }
        return this.normals;
    }

    /**
     * Get colors
     * @return Colors
     */
    public byte[] getColors() {
        return this.colors;
    }

    /**
     * Set minimum alpha value
     * @param value Minimum alpha value
     */
    public void setAlphaMin(float value) {
        this.opacityNodes[0] = value;
    }

    /**
     * Set maximum alpha value
     * @param value Maximum alpha value
     */
    public void setAlphaMax(float value) {
        this.opacityNodes[1] = value;
    }

    /**
     * Set opacity transfer function
     * @param value Opacity transfer function
     */
    public void setOpacityTransferFunction(OpacityTransferFunction value) {
        this.transferFunction.setOpacityTransferFunction(value);
    }

    /**
     * Set opacity transfer function
     * @param opacityNodes Opacity nodes
     * @param opacityLevels Opacity levels
     */
    public void setOpacityTransferFunction(List<Number> opacityNodes, List<Number> opacityLevels) {
        this.transferFunction.setOpacityTransferFunction(new OpacityTransferFunction(opacityNodes, opacityLevels));
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

        ColorMap colorMap = this.transferFunction.getColorMap();
        Color[] oColors = colorMap.getColors();
        int n = oColors.length;
        originalColors = new byte[n * 3];
        for (int i = 0; i < n; i++) {
            int color = oColors[i].getRGB();
            originalColors[i * 3 + 0] = (byte) ((color >> 16) & 0xff);
            originalColors[i * 3 + 1] = (byte) ((color >> 8) & 0xff);
            originalColors[i * 3 + 2] = (byte) ((color) & 0xff);
        }

        Normalize norm = this.transferFunction.getNormalize();
        double[] values = MIMath.getIntervalValues(norm.getMinValue(), norm.getMaxValue(), n - 1);
        LegendScheme ls = LegendManage.createGraduatedLegendScheme(values, oColors, ShapeTypes.POLYGON,
                norm.getMinValue(), norm.getMaxValue());
        ls.setColorMap(colorMap);
        ls.setNormalize(norm);
        this.setLegendScheme(ls);
        this.setSingleLegend(false);
    }

    /**
     * Get scale
     * @return Scale
     */
    public float[] getScale() {
        return this.scale;
    }

    /**
     * Get scale by index
     * @param i Index
     * @return Scale
     */
    public float getScale(int i) {
        return this.scale[i];
    }

    public int getColorNum() {
        return colors.length / 4;
    }

    public float[] getAabbMin() {
        return this.aabbMin;
    }

    public float[] getAabbMax() {
        return this.aabbMax;
    }

    /**
     * Get ray casting type
     * @return Ray casting type
     */
    public RayCastingType getRayCastingType() {
        return this.rayCastingType;
    }

    /**
     * Set ray casting type
     * @param value Ray casting type
     */
    public void setRayCastingType(RayCastingType value) {
        this.rayCastingType = value;
    }

    /**
     * Get brightness
     * @return Brightness
     */
    public float getBrightness() {
        return this.brightness;
    }

    /**
     * Set brightness
     * @param value Brightness
     */
    public void setBrightness(float value) {
        this.brightness = value;
    }

    /**
     * Set ray casting type
     * @param value Ray casting type
     */
    public void setRayCastingType(String value) {
        this.rayCastingType = RayCastingType.valueOf(value.toUpperCase());
    }

    private float[] vertexBufferData = new float[]{
            -1.0f, -1.0f, -1.0f, // triangle 1 : begin
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f, // triangle 1 : end
            1.0f, 1.0f, -1.0f, // triangle 1 : begin
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f, // triangle 1 : end
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f
    };

    /**
     * Get vertex buffer data
     * @return Vertex buffer data
     */
    public float[] getVertexBufferData() {
        return this.vertexBufferData;
    }

    public float[] getVertexBufferData(Transform transform) {
        Extent3D extent = (Extent3D) this.getExtent();
        float xMin = (float) extent.minX;
        float xMax = (float) extent.maxX;
        float yMin = (float) extent.minY;
        float yMax = (float) extent.maxY;
        float zMin = (float) extent.minZ;
        float zMax = (float) extent.maxZ;
        float[] p0 = transform.transformArray(xMin, yMin, zMin);
        float[] p1 = transform.transformArray(xMax, yMin, zMin);
        float[] p2 = transform.transformArray(xMax, yMax, zMin);
        float[] p3 = transform.transformArray(xMin, yMax, zMin);
        float[] p4 = transform.transformArray(xMin, yMin, zMax);
        float[] p5 = transform.transformArray(xMax, yMin, zMax);
        float[] p6 = transform.transformArray(xMax, yMax, zMax);
        float[] p7 = transform.transformArray(xMin, yMax, zMax);
        this.aabbMin = p0;
        this.aabbMax = p6;
        return new float[] {
                p0[0], p0[1], p0[2], // triangle 1 : begin
                p1[0], p1[1], p1[2],
                p4[0], p4[1], p4[2], // triangle 1 : end
                p4[0], p4[1], p4[2], // triangle 2 : begin
                p5[0], p5[1], p5[2],
                p1[0], p1[1], p1[2], // triangle 2 : end
                p1[0], p1[1], p1[2],
                p5[0], p5[1], p5[2],
                p2[0], p2[1], p2[2], //3
                p5[0], p5[1], p5[2],
                p2[0], p2[1], p2[2],
                p6[0], p6[1], p6[2], //4
                p2[0], p2[1], p2[2],
                p6[0], p6[1], p6[2],
                p3[0], p3[1], p3[2], //5
                p6[0], p6[1], p6[2],
                p3[0], p3[1], p3[2],
                p7[0], p7[1], p7[2], //6
                p3[0], p3[1], p3[2],
                p7[0], p7[1], p7[2],
                p0[0], p0[1], p0[2], //7
                p7[0], p7[1], p7[2],
                p0[0], p0[1], p0[2],
                p4[0], p4[1], p4[2], //8
                p0[0], p0[1], p0[2],
                p1[0], p1[1], p1[2],
                p3[0], p3[1], p3[2], //9
                p1[0], p1[1], p1[2],
                p3[0], p3[1], p3[2],
                p2[0], p2[1], p2[2], //10
                p4[0], p4[1], p4[2],
                p5[0], p5[1], p5[2],
                p7[0], p7[1], p7[2], //11
                p5[0], p5[1], p5[2],
                p7[0], p7[1], p7[2],
                p6[0], p6[1], p6[2], //12
        };
    }

    /**
     * Get vertex number
     * @return Vertex number
     */
    public int getVertexNumber() {
        return this.vertexBufferData.length / 3;
    }

    /**
     * Calculate normals
     */
    public void calculateNormals() {
        this.normals = new byte[this.byteData.length * 3];
        int xn, yn, zn, i1, i2;
        int n = this.byteData.length;
        for (int i = 0; i < n; i++) {
            i1 = i - 1;
            i2 = i + 1;
            if (i1 < 0 || i2 >= n)
                xn = 0;
            else
                xn = byteData[i1] - byteData[i2];
            normals[i * 3] = (byte) (xn + 128);

            i1 = i - width;
            i2 = i + width;
            if (i1 < 0 || i2 >= n)
                yn = 0;
            else
                yn = byteData[i1] - byteData[i2];
            normals[i * 3 + 1] = (byte) (yn + 128);

            i1 = i - (width * height);
            i2 = i + (width * height);
            if (i1 < 0 || i2 >= n)
                zn = 0;
            else
                zn = byteData[i1] - byteData[i2];
            normals[i * 3 + 2] = (byte) (zn + 128);
        }
    }
}
