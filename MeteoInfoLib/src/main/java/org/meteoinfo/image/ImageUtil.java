/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.meteoinfo.math.ArrayMath;
import org.meteoinfo.ndarray.*;

/**
 *
 * @author Yaqiang Wang
 */
public class ImageUtil {
    
    private final static double INCH_2_CM = 2.54;
    
    /**
     * Read RGB array data from image file
     * @param fileName Image file name
     * @return RGB array data
     * @throws java.io.IOException
     * @throws ImageReadException
     */
    public static Array imageRead(String fileName) throws IOException, ImageReadException{
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        BufferedImage image;
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")){
            image = ImageIO.read(new File(fileName));
        } else {
            image = Imaging.getBufferedImage(new File(fileName));
        }
        return imageRead(image);
    }
    
    /**
     * Read RGB array data from image
     * @param image Image
     * @return RGB array data
     */
    public static Array imageRead(BufferedImage image){
        int xn = image.getWidth();
        int yn = image.getHeight();
        Array r = Array.factory(DataType.INT, new int[]{yn, xn, 3});
        Index index = r.getIndex();
        int rgb;
        Color color;
        for (int i = 0; i < yn; i++){
            for (int j = 0; j < xn; j++){
                rgb = image.getRGB(j, yn - i - 1);
                color = new Color(rgb);
                r.setInt(index.set(i, j, 0), color.getRed());
                r.setInt(index.set(i, j, 1), color.getGreen());
                r.setInt(index.set(i, j, 2), color.getBlue());
            }
        }
        return r;
    }
    
    /**
     * Load image from image file
     * @param fileName Image file name
     * @return Image
     * @throws java.io.IOException
     * @throws ImageReadException
     */
    public static BufferedImage imageLoad(String fileName) throws IOException, ImageReadException{
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        BufferedImage image;
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")){
            image = ImageIO.read(new File(fileName));
        } else {
            image = Imaging.getBufferedImage(new File(fileName));
        }
        return image;
    }
    
    /**
     * Create image from RGB(A) data array
     * @param data RGB(A) data array
     * @return Image
     */
    public static BufferedImage createImage(Array data) {
        int width, height;
        width = data.getShape()[1];
        height = data.getShape()[0];
        Color undefColor = Color.white;
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Color color;
        Index index = data.getIndex();
        boolean isAlpha = data.getShape()[2] == 4;
        if (data.getDataType() == DataType.FLOAT || data.getDataType() == DataType.DOUBLE){
            float r, g, b;
            if (isAlpha) {
                float a;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = data.getFloat(index.set(i, j, 0));
                        g = data.getFloat(index.set(i, j, 1));
                        b = data.getFloat(index.set(i, j, 2));
                        a = data.getFloat(index.set(i, j, 3));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = data.getFloat(index.set(i, j, 0));
                        g = data.getFloat(index.set(i, j, 1));
                        b = data.getFloat(index.set(i, j, 2));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        } else {
            int r, g, b;
            if (isAlpha) {
                int a;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = data.getInt(index.set(i, j, 0));
                        g = data.getInt(index.set(i, j, 1));
                        b = data.getInt(index.set(i, j, 2));
                        a = data.getInt(index.set(i, j, 3));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = data.getInt(index.set(i, j, 0));
                        g = data.getInt(index.set(i, j, 1));
                        b = data.getInt(index.set(i, j, 2));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        }

        return aImage;
    }
    
    /**
     * Save image into a file
     * @param data RGB(A) data array
     * @param fileName Output image file name
     * @throws IOException 
     * @throws ImageWriteException 
     */
    public static void imageSave(Array data, String fileName) throws IOException, ImageWriteException{
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        BufferedImage image = createImage(data);  
        ImageFormat format = getImageFormat(extension);
        if (format == ImageFormats.JPEG){
            ImageIO.write(image, extension, new File(fileName));
        } else {
            Imaging.writeImage(image, new File(fileName), format, null);       
        }
    }
    
    /**
     * Save image into a file
     * @param image Image
     * @param fileName Output image file name
     * @throws IOException 
     * @throws ImageWriteException 
     */
    public static void imageSave(BufferedImage image, String fileName) throws IOException, ImageWriteException{
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1); 
        ImageFormat format = getImageFormat(extension);
        if (format == ImageFormats.JPEG){
            ImageIO.write(image, extension, new File(fileName));
        } else {
            Imaging.writeImage(image, new File(fileName), format, null);       
        }
    }
    
    private static ImageFormat getImageFormat(String ext){
        ImageFormat format = ImageFormats.PNG;
        switch(ext.toLowerCase()){
            case "gif":
                format = ImageFormats.GIF;
                break;
            case "jpeg":
            case "jpg":
                format = ImageFormats.JPEG;
                break;
            case "bmp":
                format = ImageFormats.BMP;
                break;
            case "tif":
            case "tiff":
                format = ImageFormats.TIFF;
                break;
        }
        return format;
    }
    
    /**
     * Count none-zero points with window size
     * @param data Input data
     * @param size Window size
     * @return Count array
     */
    public static Array count(Array data, int size){
        int ny = data.getShape()[0];
        int nx = data.getShape()[1];
        int skip = size / 2;
        int ii, jj, n;
        Array r = Array.factory(DataType.INT, data.getShape());
        for (int i = 0; i < ny; i++){
            if (i < skip || i >= ny - skip){
                for (int j = 0; j < nx; j++){
                    r.setInt(i * nx + j, 0);
                }
            } else {
                for (int j = 0; j < nx; j++){
                    if (j < skip || j >=  nx - skip){
                        r.setInt(i * nx + j, 0);
                    } else {
                        n = 0;
                        for (ii = i - skip; ii <= i + skip; ii++){
                            for (jj = j - skip; jj <= j + skip; jj++){
                                if (data.getDouble(ii * nx + jj) > 0){
                                    n += 1;
                                }
                            }
                        }
                        r.setInt(i * nx + j, n);
                    }
                }
            }
        }
        
        return r;
    }
    
    /**
     * Calculate mean value with window size
     * @param data Input data
     * @param size Window size
     * @param positive Only calculate the positive value or not.
     * @return Mean array
     */
    public static Array mean(Array data, int size, boolean positive){
        int ny = data.getShape()[0];
        int nx = data.getShape()[1];
        int skip = size / 2;
        double sum;
        int ii, jj, n;
        Array r = Array.factory(data.getDataType(), data.getShape());
        for (int i = 0; i < ny; i++){
            if (i < skip || i >= ny - skip){
                for (int j = 0; j < nx; j++){
                    r.setObject(i * nx + j, 0);
                }
            } else {
                for (int j = 0; j < nx; j++){
                    if (j < skip || j >=  nx - skip){
                        r.setObject(i * nx + j, 0);
                    } else {
                        n = 0;
                        sum = 0;
                        if (positive){
                            for (ii = i - skip; ii <= i + skip; ii++){
                                for (jj = j - skip; jj <= j + skip; jj++){
                                    if (data.getDouble(ii * nx + jj) > 0){
                                        sum += data.getDouble(ii * nx + jj);
                                        n += 1;
                                    }
                                }
                            }
                        } else {
                            for (ii = i - skip; ii <= i + skip; ii++){
                                for (jj = j - skip; jj <= j + skip; jj++){
                                    if (!Double.isNaN(data.getDouble(ii * nx + jj))){
                                        sum += data.getDouble(ii * nx + jj);
                                        n += 1;
                                    }
                                }
                            }
                        }
                        if (n > 0)
                            r.setObject(i * nx + j, sum / n);
                        else
                            r.setObject(i * nx + j, 0);
                    }
                }
            }
        }
        
        return r;
    }

    /**
     * Calculate a multi-dimensional minimum filter.
     * @param data Input data
     * @param size Window size
     * @return Minimum filter array
     */
    public static Array minimumFilter(Array data, int size) throws InvalidRangeException {
        int[] shape = data.getShape();
        int half = size / 2;
        double min;
        int n = data.getRank();
        Array r = Array.factory(data.getDataType(), shape);
        IndexIterator iter = data.getIndexIterator();
        IndexIterator riter = r.getIndexIterator();
        int[] counter;
        List<Range> ranges;
        int si, ei;
        Array temp;
        while(iter.hasNext()) {
            iter.next();
            counter = iter.getCurrentCounter();
            ranges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                si = (counter[i] - half) >= 0 ? counter[i] - half : 0;
                ei = (counter[i] + half < shape[i]) ? counter[i] + half : shape[i] - 1;
                ranges.add(new Range(si, ei));
            }
            temp = data.section(ranges);
            min = ArrayMath.min(temp).doubleValue();
            riter.setDoubleNext(min);
        }

        return r;
    }

    /**
     * Calculate a multi-dimensional maximum filter.
     * @param data Input data
     * @param size Window size
     * @return Maximum filter array
     */
    public static Array maximumFilter(Array data, int size) throws InvalidRangeException {
        int[] shape = data.getShape();
        int half = size / 2;
        double max;
        int n = data.getRank();
        Array r = Array.factory(data.getDataType(), shape);
        IndexIterator iter = data.getIndexIterator();
        IndexIterator riter = r.getIndexIterator();
        int[] counter;
        List<Range> ranges;
        int si, ei;
        Array temp;
        while(iter.hasNext()) {
            iter.next();
            counter = iter.getCurrentCounter();
            ranges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                si = (counter[i] - half) >= 0 ? counter[i] - half : 0;
                ei = (counter[i] + half < shape[i]) ? counter[i] + half : shape[i] - 1;
                ranges.add(new Range(si, ei));
            }
            temp = data.section(ranges);
            max = ArrayMath.max(temp).doubleValue();
            riter.setDoubleNext(max);
        }

        return r;
    }

    private static Array corrlated1D(Array a, double[] weights) {
        int size = weights.length;
        int origin = size / 2;
        int n = (int)a.getSize();
        Array r = Array.factory(a.getDataType(), a.getShape());
        double v;
        Index1D index = (Index1D)a.getIndex();
        int idx;
        for (int i = 0; i < r.getSize(); i++) {
            v = 0;
            for (int j = 0; j < size; j++) {
                idx = i - origin + j;
                if (idx < 0)
                    idx = -idx;
                else if (idx > n - 1)
                    idx = n - 1 - (idx - (n - 1));
                index.set(idx);
                v += a.getDouble(index) * weights[j];
            }
            r.setDouble(i, v);
        }

        return r;
    }

    /**
     * Calculate a multi-dimensional gaussian filter.
     * @param data Input data
     * @param size Window size
     * @param sigma Sigma
     * @return Gaussian filter array
     */
    public static Array gaussianFilter(Array data, int size, double sigma) throws InvalidRangeException {
        //Create 1D template
        double[] weights = new double[size];
        double sum = 0;
        int origin = size / 2;
        for (int i = 0; i < size; i++)
        {
            //The first constant does not need to be calculate, which will be eliminated finally
            double g = Math.exp(-(i - origin) * (i - origin) / (2 * sigma * sigma));
            sum += g;
            weights[i] = g;
        }
        //Normalized
        for (int i = 0; i < size; i++)
            weights[i] /= sum;

        //Filter
        int ndim = data.getRank();
        int[] shape = data.getShape();
        Array r = Array.factory(data.getDataType(), shape);
        Index rindex = r.getIndex();
        int[] rcurrent = new int[ndim];
        int idx;
        for (int axis = 0; axis < ndim; axis++) {
            int[] nshape = new int[ndim - 1];
            for (int i = 0; i < ndim; i++) {
                if (i < axis)
                    nshape[i] = shape[i];
                else if (i > axis)
                    nshape[i - 1] = shape[i];
            }
            Index index = Index.factory(nshape);
            int[] current;
            for (int i = 0; i < index.getSize(); i++) {
                current = index.getCurrentCounter();
                List<Range> ranges = new ArrayList<>();
                for (int j = 0; j < ndim; j++) {
                    if (j == axis) {
                        ranges.add(new Range(0, shape[j] - 1, 1));
                        rcurrent[j] = 0;
                    } else {
                        idx = j;
                        if (idx > axis) {
                            idx -= 1;
                        }
                        ranges.add(new Range(current[idx], current[idx], 1));
                        rcurrent[j] = current[idx];
                    }
                }
                Array temp = data.section(ranges);
                temp = corrlated1D(temp, weights);
                for (int j = 0; j < shape[axis]; j++) {
                    rcurrent[axis] = j;
                    rindex.set(rcurrent);
                    r.setDouble(rindex, temp.getDouble(j));
                }
                index.incr();
            }
        }

        return r;
    }
    
    /**
     * Create gif animator file from image files
     *
     * @param inImageFiles Input image files
     * @param outGifFile Output gif file
     * @param delay Delay time in milliseconds between each frame
     * @param repeat Repeat times, 0 means unlimite repeat
     */
    public static void createGifAnimator(List<String> inImageFiles, String outGifFile, int delay, int repeat) {
        try {
            AnimatedGifEncoder e = new AnimatedGifEncoder();
            e.setRepeat(0);
            e.setDelay(delay);
            e.start(outGifFile);
            for (String infn : inImageFiles){
                e.addFrame(ImageIO.read(new File(infn)));
            }
            e.finish();
        } catch (Exception e) {
            System.out.println("Create gif animator failed:");
            e.printStackTrace();
        }
    }
    
    /**
     * Create gif animator file from image files
     *
     * @param inImageFiles Input image files
     * @param outGifFile Output gif file
     * @param delay Delay time in milliseconds between each frame
     */
    public static void createGifAnimator(List<String> inImageFiles, String outGifFile, int delay) {
        createGifAnimator(inImageFiles, outGifFile, delay, 0);
    }
    
    /**
     * Create gif animator file from image files
     * @param infiles Input image files
     * @param outfile Output gif file
     * @param delay Delay time in milliseconds between each frame
     */
    public static void createGifAnimator(File[] infiles, File outfile, int delay) {
        try {
            AnimatedGifEncoder e = new AnimatedGifEncoder();
            e.setRepeat(0);
            e.setDelay(delay);
            e.start(outfile.getCanonicalPath());
            for (File inf : infiles){
                e.addFrame(ImageIO.read(inf));
            }
            e.finish();
        } catch (Exception e) {
            System.out.println("Create gif animator failed:");
            e.printStackTrace();
        }
    }
    
    /**
     * Set DPI
     * @param metadata IIOMetadata
     * @param dpi DPI
     * @throws IIOInvalidTreeException 
     */
    public static void setDPI(IIOMetadata metadata, float dpi) throws IIOInvalidTreeException {

        // for PMG, it's dots per millimeter
        double dotsPerMilli = 1.0 * dpi / 10 / INCH_2_CM;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

}
