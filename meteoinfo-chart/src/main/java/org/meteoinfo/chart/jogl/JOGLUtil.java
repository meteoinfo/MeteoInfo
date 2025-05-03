/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import org.joml.Vector3f;
import org.meteoinfo.chart.GLChart;
import org.meteoinfo.chart.GLChartPanel;
import org.meteoinfo.chart.graphic.*;
import org.meteoinfo.chart.jogl.mc.CallbackMC;
import org.meteoinfo.chart.jogl.mc.MarchingCubes;
import org.meteoinfo.geometry.colors.OpacityTransferFunction;
import org.meteoinfo.chart.shape.TextureShape;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.colors.ColorMap;
import org.meteoinfo.geometry.colors.Normalize;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.shape.ImageShape;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.image.ImageUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;
import org.w3c.dom.Element;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yaqiang
 */
public class JOGLUtil {

    public static void diff3(final float[] a, final float[] b, final float[] c) {
        c[0] = a[0] - b[0];
        c[1] = a[1] - b[1];
        c[2] = a[2] - b[2];
    }

    public static void crossprod(final float[] v1, final float[] v2, final float[] prod) {
        final float[] p = new float[3];         /* in case prod == v1 or v2 */

        p[0] = v1[1] * v2[2] - v2[1] * v1[2];
        p[1] = v1[2] * v2[0] - v2[2] * v1[0];
        p[2] = v1[0] * v2[1] - v2[0] * v1[1];
        prod[0] = p[0];
        prod[1] = p[1];
        prod[2] = p[2];
    }

    public static void normalize(final float[] v) {
        float d;

        d = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (d == 0.0) {
            v[0] = d = 1.0f;
        }
        d = 1 / d;
        v[0] *= d;
        v[1] *= d;
        v[2] *= d;
    }

    public static float[] normalize(final float[] n1, final float[] n2, final float[] n3) {
        final float[] q0 = new float[3];
        final float[] q1 = new float[3];

        diff3(n1, n2, q0);
        diff3(n2, n3, q1);
        crossprod(q0, q1, q1);
        normalize(q1);

        return q1;
    }

    private static void recordItem(final GL2 gl, final float[] n1, final float[] n2, final float[] n3, final int shadeType) {
        final float[] q0 = new float[3];
        final float[] q1 = new float[3];

        diff3(n1, n2, q0);
        diff3(n2, n3, q1);
        crossprod(q0, q1, q1);
        normalize(q1);

        gl.glBegin(shadeType);
        gl.glNormal3fv(q1, 0);
        gl.glVertex3fv(n1, 0);
        gl.glVertex3fv(n2, 0);
        gl.glVertex3fv(n3, 0);
        gl.glEnd();
    }

    private static void subdivide(final GL2 gl, final float[] v0, final float[] v1, final float[] v2, final int shadeType) {
        int depth;
        final float[] w0 = new float[3];
        final float[] w1 = new float[3];
        final float[] w2 = new float[3];
        float l;
        int i, j, k, n;

        depth = 1;
        for (i = 0; i < depth; i++) {
            for (j = 0; i + j < depth; j++) {
                k = depth - i - j;
                for (n = 0; n < 3; n++) {
                    w0[n] = (i * v0[n] + j * v1[n] + k * v2[n]) / depth;
                    w1[n] = ((i + 1) * v0[n] + j * v1[n] + (k - 1) * v2[n])
                            / depth;
                    w2[n] = (i * v0[n] + (j + 1) * v1[n] + (k - 1) * v2[n])
                            / depth;
                }
                l = (float) Math.sqrt(w0[0] * w0[0] + w0[1] * w0[1] + w0[2] * w0[2]);
                w0[0] /= l;
                w0[1] /= l;
                w0[2] /= l;
                l = (float) Math.sqrt(w1[0] * w1[0] + w1[1] * w1[1] + w1[2] * w1[2]);
                w1[0] /= l;
                w1[1] /= l;
                w1[2] /= l;
                l = (float) Math.sqrt(w2[0] * w2[0] + w2[1] * w2[1] + w2[2] * w2[2]);
                w2[0] /= l;
                w2[1] /= l;
                w2[2] /= l;
                recordItem(gl, w1, w0, w2, shadeType);
            }
        }
    }

    public static void drawTriangle(final GL2 gl, final float[] x0, final float[] x1,
                                    final float[] x2) {
        //subdivide(gl, x0, x1, x2, GL2.GL_TRIANGLES);
        recordItem(gl, x0, x1, x2, GL2.GL_TRIANGLES);
    }

    /**
     * Get RGBA components from a legend break
     *
     * @param cb Legend break
     * @return RGBA float array
     */
    public static float[] getRGBA(ColorBreak cb) {
        return cb.getColor().getRGBComponents(null);
    }

    /**
     * Create particle graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param ls       LegendScheme
     * @param alphaMin Min alpha
     * @param alphaMax Max alpha
     * @param density  Point density
     * @return Particles
     */
    public static GraphicCollection particles(Array data, Array xa, Array ya, Array za, LegendScheme ls,
                                              float alphaMin, float alphaMax, int density) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        ParticleGraphics graphics = new ParticleGraphics();
        ParticleGraphics.Particle particle;
        Random random = new Random();
        float dx = xa.getFloat(1) - xa.getFloat(0);
        float dy = ya.getFloat(1) - ya.getFloat(0);
        float dz = za.getFloat(1) - za.getFloat(0);
        int n = ls.getBreakNum();
        float[] alphas = new float[n];
        float dd = (alphaMax - alphaMin) / (n - 1);
        for (int i = 0; i < n; i++) {
            alphas[i] = alphaMin + i * dd;
        }
        double v;
        ColorBreak cb;
        float[] rgba;
        int level;
        double vMin = ls.getMinValue();
        double vMax = ls.getMaxValue();
        Index index = data.getIndex();
        if (za.getRank() == 1) {
            for (int i = 0; i < za.getSize(); i++) {
                for (int j = 0; j < ya.getSize(); j++) {
                    for (int k = 0; k < xa.getSize(); k++) {
                        index.set(i, j, k);
                        v = data.getDouble(index);
                        if (Double.isNaN(v)) {
                            continue;
                        }
                        if (v < vMin || v > vMax) {
                            continue;
                        }
                        level = ls.legendBreakIndex(v);
                        if (level >= 0) {
                            cb = ls.getLegendBreak(level);
                            rgba = cb.getColor().getRGBComponents(null);
                            rgba[3] = alphas[level];
                            for (int l = 0; l <= level * density; l++) {
                                particle = new ParticleGraphics.Particle();
                                particle.x = xa.getFloat(k) + (random.nextFloat() - 0.5f) * dx * 2;
                                particle.y = ya.getFloat(j) + (random.nextFloat() - 0.5f) * dy * 2;
                                particle.z = za.getFloat(i) + (random.nextFloat() - 0.5f) * dz * 2;
                                particle.rgba = rgba;
                                graphics.addParticle(level, particle);
                            }
                        }
                    }
                }
            }
        } else {
            int zn = za.getShape()[0];
            Index zIndex = za.getIndex();
            float z;
            for (int i = 0; i < zn; i++) {
                for (int j = 0; j < ya.getSize(); j++) {
                    for (int k = 0; k < xa.getSize(); k++) {
                        index.set(i, j, k);
                        v = data.getDouble(index);
                        if (Double.isNaN(v)) {
                            continue;
                        }
                        if (v < vMin || v > vMax) {
                            continue;
                        }
                        level = ls.legendBreakIndex(v);
                        zIndex.set(i, j, k);
                        z = za.getFloat(zIndex);
                        if (i == 0)
                            zIndex.set(1, j, k);
                        else
                            zIndex.set(i - 1, j, k);
                        dz = Math.abs(z - za.getFloat(zIndex));
                        if (level >= 0) {
                            cb = ls.getLegendBreak(level);
                            rgba = cb.getColor().getRGBComponents(null);
                            rgba[3] = alphas[level];
                            for (int l = 0; l <= level * density; l++) {
                                particle = new ParticleGraphics.Particle();
                                particle.x = xa.getFloat(k) + (random.nextFloat() - 0.5f) * dx * 2;
                                particle.y = ya.getFloat(j) + (random.nextFloat() - 0.5f) * dy * 2;
                                particle.z = z + (random.nextFloat() - 0.5f) * dz * 2;
                                particle.rgba = rgba;
                                graphics.addParticle(level, particle);
                            }
                        }
                    }
                }
            }
        }

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param colorMap ColorMap
     * @param vMin Min value
     * @param vMax Max value
     * @param alphaMin Min alpha
     * @param alphaMax Max alpha
     * @return Particles
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, ColorMap colorMap,
                                           double vMin, double vMax, float alphaMin, float alphaMax) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        VolumeGraphic graphics = new VolumeGraphic(data, colorMap, vMin, vMax);
        graphics.setAlphaMin(alphaMin);
        graphics.setAlphaMax(alphaMax);
        graphics.updateColors();

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);

        return graphics;
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param colorMap ColorMap
     * @param alphaMin Min alpha
     * @param alphaMax Max alpha
     * @return Particles
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, ColorMap colorMap,
                                           Normalize norm, float alphaMin, float alphaMax) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        VolumeGraphic graphics = new VolumeGraphic(data, colorMap, norm);
        graphics.setAlphaMin(alphaMin);
        graphics.setAlphaMax(alphaMax);
        graphics.updateColors();

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);

        return graphics;
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param colorMap ColorMap
     * @param norm Normalize
     * @param opacityNodes Opacity nodes
     * @param opacityLevels Opacity levels
     * @return Volume graphics
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, ColorMap colorMap,
                                           Normalize norm, List<Number> opacityNodes, List<Number> opacityLevels) {
        OpacityTransferFunction transferFunction = new OpacityTransferFunction(opacityNodes, opacityLevels, norm);
        return volume(data, xa, ya, za, colorMap, norm, transferFunction);
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param colorMap ColorMap
     * @param norm Normalize
     * @param transferFunction Transfer function
     * @return Volume graphics
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, ColorMap colorMap,
                                           Normalize norm, OpacityTransferFunction transferFunction) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        VolumeGraphic graphics = new VolumeGraphic(data, colorMap, norm);
        graphics.setOpacityTransferFunction(transferFunction);
        graphics.updateColors();

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);

        return graphics;
    }

    /**
     * Create volume graphics
     *
     * @param data     3d data array
     * @param xa       X coordinates
     * @param ya       Y coordinates
     * @param za       Z coordinates
     * @param ls       LegendScheme
     * @param alphaMin Min alpha
     * @param alphaMax Max alpha
     * @return Particles
     */
    public static GraphicCollection volume(Array data, Array xa, Array ya, Array za, LegendScheme ls,
                                           float alphaMin, float alphaMax) {
        data = data.copyIfView();
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        za = za.copyIfView();

        VolumeGraphic graphics = new VolumeGraphic(data, ls);
        graphics.setAlphaMin(alphaMin);
        graphics.setAlphaMax(alphaMax);
        graphics.updateColors();

        Extent3D extent3D = new Extent3D();
        extent3D.minX = xa.getDouble(0);
        extent3D.maxX = xa.getDouble((int) xa.getSize() - 1);
        extent3D.minY = ya.getDouble(0);
        extent3D.maxY = ya.getDouble((int) ya.getSize() - 1);
        extent3D.minZ = za.getDouble(0);
        extent3D.maxZ = za.getDouble((int) za.getSize() - 1);
        graphics.setExtent(extent3D);

        return graphics;
    }

    /**
     * Paint view image
     *
     * @param chart The GLChart
     * @param width Image width
     * @param height Image height
     * @return View image
     */
    public static BufferedImage paintViewImage(GLChart chart, int width, int height) {
        final GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setHardwareAccelerated(true);
        caps.setDoubleBuffered(false);
        caps.setAlphaBits(8);
        caps.setRedBits(8);
        caps.setBlueBits(8);
        caps.setGreenBits(8);
        caps.setOnscreen(false);
        caps.setPBuffer(true);
        GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
        GLOffscreenAutoDrawable drawable = factory.createOffscreenAutoDrawable(null, caps, null,
                width, height);
        drawable.addGLEventListener(chart);
        chart.setDoScreenShot(true);
        chart.setAlwaysUpdateBuffers(true);
        drawable.display();

        BufferedImage image = chart.getScreenImage();
        drawable.destroy();
        chart.setAlwaysUpdateBuffers(true);

        return image;
    }

    /**
     * Paint view image
     *
     * @param chart The GLChart
     * @param width    Image width
     * @param height   Image height
     * @param dpi      Image dpi
     * @return View image
     */
    public static BufferedImage paintViewImage(GLChart chart, int width, int height, int dpi) {
        double scaleFactor = dpi / 72.0;
        width = (int) (width * scaleFactor);
        height = (int) (height * scaleFactor);
        chart.setDpiScale((float) scaleFactor);
        BufferedImage image = paintViewImage(chart, width, height);
        chart.setDpiScale(1);
        return image;
    }

    /**
     * Save image file
     *
     * @param fn     File path
     * @param width  Image width
     * @param height Image height
     * @throws InterruptedException
     */
    public static void saveImage(GLChart plot3DGL, String fn, int width, int height) throws InterruptedException {
        BufferedImage image = paintViewImage(plot3DGL, width, height);
        if (image != null) {
            //String extension = fn.substring(fn.lastIndexOf('.') + 1);
            try {
                //ImageIO.write(image, extension, new File(fn));
                ImageUtil.imageSave(image, fn);
            } catch (IOException ex) {
                Logger.getLogger(GLChartPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Save image file
     *
     * @param plot3DGL Plot3DGL
     * @param fn       File path
     * @param width    Image width
     * @param height   Image height
     * @param dpi      Image dpi
     * @throws InterruptedException
     */
    public static void saveImage(GLChart plot3DGL, String fn, int width, int height, int dpi) throws InterruptedException, IOException {
        BufferedImage image = paintViewImage(plot3DGL, width, height, dpi);

        if (image != null) {
            ImageUtil.imageSave(image, fn, dpi);
        }
    }

    /**
     * Save image file
     *
     * @param plot3DGL Plot3DGL
     * @param fn       File path
     * @param width    Image width
     * @param height   Image height
     * @param dpi      Image dpi
     * @throws InterruptedException
     */
    public static void saveImage_bak(GLChart plot3DGL, String fn, int width, int height, int dpi) throws InterruptedException, IOException {
        String formatName = fn.substring(fn.lastIndexOf('.') + 1);
        if (formatName.equals("jpg")) {
            formatName = "jpeg";
            saveImage_Jpeg(plot3DGL, fn, width, height, dpi);
            return;
        }

        BufferedImage image = paintViewImage(plot3DGL, width, height, dpi);

        if (image != null) {
            try {
                File output = new File(fn);
                output.delete();
                for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext(); ) {
                    ImageWriter writer = iw.next();
                    ImageWriteParam writeParam = writer.getDefaultWriteParam();
                    ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
                    IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
                    if (metadata == null) {
                        metadata = writer.getDefaultImageMetadata(typeSpecifier, null);
                    }
                    if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                        continue;
                    }

                    ImageUtil.setDPI(metadata, dpi);

                    final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
                    try {
                        writer.setOutput(stream);
                        writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
                    } finally {
                        stream.close();
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveImage_Jpeg(GLChart plot3DGL, String file, int width, int height, int dpi) {
        BufferedImage bufferedImage = paintViewImage(plot3DGL, width, height, dpi);

        if (bufferedImage != null) {
            try {
                // Image writer
                ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(new File(file));
                imageWriter.setOutput(ios);

                // Compression
                JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
                jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
                jpegParams.setCompressionQuality(0.85f);

                // Metadata (dpi)
                IIOMetadata data = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(bufferedImage), jpegParams);
                Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
                Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
                jfif.setAttribute("Xdensity", Integer.toString(dpi));
                jfif.setAttribute("Ydensity", Integer.toString(dpi));
                jfif.setAttribute("resUnits", "1"); // density is dots per inch
                data.setFromTree("javax_imageio_jpeg_image_1.0", tree);

                // Write and clean up
                imageWriter.write(null, new IIOImage(bufferedImage, null, data), jpegParams);
                ios.close();
                imageWriter.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Convert Vector3f to float array
     * @param v The Vector3f
     * @return Float array
     */
    public static float[] toArray(Vector3f v) {
        return new float[]{v.x, v.y, v.z};
    }
}
