package org.meteoinfo.chart.graphic;

import com.jogamp.common.nio.Buffers;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.geo.legend.LegendManage;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayMath;

import java.awt.*;
import java.nio.Buffer;
import java.util.List;

import static org.joml.Math.clamp;

public class VolumeGraphics extends GraphicCollection3D {
    public static Buffer buffer = null;
    final int width;
    final int height;
    final int depth;
    final byte[] data;
    final float[] scale = new float[]{1, 1, 1};
    public static byte[] colors;
    private byte[] originalColors;
    public static float[] opacityLevels = new float[]{0, 1};
    public static float[] opacityNodes = new float[]{0f, 1f};
    public static float[] colorRange = new float[]{0f, 1f};
    private float[] aabbMin = new float[]{-1, -1, -1};
    private float[] aabbMax = new float[]{1, 1, 1};

    boolean hasChanges = true;

    /**
     * Constructor
     * @param data Byte data
     * @param width 3D texture width
     * @param height 3D texture height
     * @param depth 3D texture depth
     * @param colors Colors byte array
     */
    public VolumeGraphics(byte[] data, int width, int height, int depth, byte[] colors) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.data = data;
        this.buffer = Buffers.newDirectByteBuffer(this.data);
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
     */
    public VolumeGraphics(Array value, ColorMap colorMap) {
        value = value.copyIfView();
        int[] shape = value.getShape();
        this.depth = shape[0];
        this.height = shape[1];
        this.width = shape[2];
        this.data = new byte[width * height * depth];
        double vMax = ArrayMath.max(value).doubleValue();
        double vMin = ArrayMath.min(value).doubleValue();
        double range = vMax - vMin;
        for (int i = 0; i < value.getSize(); i++) {
            data[i] = (byte) ((int) ((value.getDouble(i) - vMin) / range * 255));
        }
        buffer = Buffers.newDirectByteBuffer(data);

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
     * @param ls LegendScheme
     */
    public VolumeGraphics(Array value, LegendScheme ls) {
        value = value.copyIfView();
        int[] shape = value.getShape();
        this.depth = shape[0];
        this.height = shape[1];
        this.width = shape[2];
        this.data = new byte[width * height * depth];
        List<Color> oColors = ls.getColors();
        int n = oColors.size();
        for (int i = 0; i < value.getSize(); i++) {
            data[i] = (byte)((int)(ls.legendBreakIndex(value.getDouble(i)) * 255.0 / n));
        }
        buffer = Buffers.newDirectByteBuffer(data);

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
            if (px <= opacityNodes[0]) {
                a = opacityNodes[0];
            } else if (px > opacityNodes[1]) {
                a = opacityNodes[1];
            } else {
                final float ratio = (px - opacityNodes[0]) / opacityNodeRange;
                a = (min * (1 - ratio) + max * ratio);
            }
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
        }
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
    public byte[] getData() {
        return this.data;
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

    final float[] vertexBufferData = new float[]{
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
        float[] p0 = transform.transform(xMin, yMin, zMin);
        float[] p1 = transform.transform(xMax, yMin, zMin);
        float[] p2 = transform.transform(xMax, yMax, zMin);
        float[] p3 = transform.transform(xMin, yMax, zMin);
        float[] p4 = transform.transform(xMin, yMin, zMax);
        float[] p5 = transform.transform(xMax, yMin, zMax);
        float[] p6 = transform.transform(xMax, yMax, zMax);
        float[] p7 = transform.transform(xMin, yMax, zMax);
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
}
