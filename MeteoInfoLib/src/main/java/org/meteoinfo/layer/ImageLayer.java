/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.layer;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.Color;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yaqiang Wang
 */
public class ImageLayer extends MapLayer {
    // <editor-fold desc="Variables">

    private BufferedImage _image;
    private WorldFilePara _worldFilePara = new WorldFilePara();
    private String _worldFileName;
    private boolean _isSetTransColor;
    private Color _transparencyColor;
    protected Object interp;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public ImageLayer() {
        super();
        this.setLayerType(LayerTypes.ImageLayer);
        this.setShapeType(ShapeTypes.Image);
        _isSetTransColor = false;
        _transparencyColor = Color.black;
        this.interp = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get image
     *
     * @return The image
     */
    public BufferedImage getImage() {
        return _image;
    }

    /**
     * Set image
     *
     * @param image The image
     */
    public void setImage(BufferedImage image) {
        _image = image;
        _transparencyColor = new Color(image.getRGB(1, 1));
    }

    /**
     * Get world file name of the layer
     *
     * @return World file name
     */
    public String getWorldFileName() {
        return _worldFileName;
    }

    /**
     * Set world file name
     *
     * @param name World file name
     */
    public void setWorldFileName(String name) {
        _worldFileName = name;
    }

    /**
     * Get world file parameters
     *
     * @return World file parameters
     */
    public WorldFilePara getWorldFilePara() {
        return _worldFilePara;
    }

    /**
     * Set world file parameters
     *
     * @param value World file parameters
     */
    public void setWorldFilePara(WorldFilePara value) {
        _worldFilePara = value;
    }

    /**
     * Get if set transparency color
     *
     * @return Boolean
     */
    public boolean isUseTransColor() {
        return _isSetTransColor;
    }

    /**
     * Set if using transparency color
     *
     * @param istrue Boolean
     */
    public void setUseTransColor(boolean istrue) {
        _isSetTransColor = istrue;
        if (istrue) {
            Image image = GlobalUtil.makeColorTransparent(_image, _transparencyColor);
            _image = GlobalUtil.imageToBufferedImage(image);
        }
    }

    /**
     * Get transparency color
     *
     * @return Transparency color
     */
    public Color getTransparencyColor() {
        return _transparencyColor;
    }

    /**
     * Set transparency color
     *
     * @param color The color
     */
    public void setTransparencyColor(Color color) {
        _transparencyColor = color;
        if (_isSetTransColor) {
            Image image = GlobalUtil.makeColorTransparent(_image, _transparencyColor);
            _image = GlobalUtil.imageToBufferedImage(image);
        }
    }

    /**
     * Get X upper-left
     *
     * @return Upper-left x value
     */
    public double getXUL() {
        return _worldFilePara.xUL;
    }

    /**
     * Set upper-left x
     *
     * @param value The value
     * @throws java.io.IOException
     */
    public void setXUL(double value) throws IOException {
        _worldFilePara.xUL = value;
        Extent aExtent = (Extent) this.getExtent().clone();
        aExtent.minX = _worldFilePara.xUL;
        aExtent.maxX = _worldFilePara.xUL + this.getExtent().getWidth();
        this.setExtent(aExtent);
        if (new File(_worldFileName).exists()) {
            writeImageWorldFile(_worldFileName, _worldFilePara);
        }
    }

    /**
     * Get y upper-left
     *
     * @return Upper-left y
     */
    public double getYUL() {
        return _worldFilePara.yUL;
    }

    /**
     * Set upper-left y
     *
     * @param value The value
     * @throws java.io.IOException
     */
    public void setYUL(double value) throws IOException {
        _worldFilePara.yUL = value;
        Extent aExtent = (Extent) this.getExtent().clone();
        aExtent.maxY = _worldFilePara.yUL;
        aExtent.minY = _worldFilePara.yUL - this.getExtent().getHeight();
        this.setExtent(aExtent);
        if (new File(_worldFileName).exists()) {
            writeImageWorldFile(_worldFileName, _worldFilePara);
        }
    }

    /**
     * Get x scale
     *
     * @return The x scale
     */
    public double getXScale() {
        return _worldFilePara.xScale;
    }

    /**
     * Set x scale
     *
     * @param value The value
     * @throws java.io.IOException
     */
    public void setXScale(double value) throws IOException {
        _worldFilePara.xScale = value;
        Extent aExtent = (Extent) this.getExtent();
        double width = _image.getWidth() * _worldFilePara.xScale;
        aExtent.maxX = _worldFilePara.xUL + width;
        this.setExtent(aExtent);
        if (new File(_worldFileName).exists()) {
            writeImageWorldFile(_worldFileName, _worldFilePara);
        }
    }

    /**
     * Get y scale
     *
     * @return The y scale
     */
    public double getYScale() {
        return _worldFilePara.yScale;
    }

    /**
     * Set y scale
     *
     * @param value The y scale value
     * @throws IOException
     */
    public void setYScale(double value) throws IOException {
        _worldFilePara.yScale = value;
        Extent aExtent = (Extent) this.getExtent();
        double height = _image.getHeight() * _worldFilePara.yScale;
        aExtent.minY = _worldFilePara.yUL + height;
        this.setExtent(aExtent);
        if (new File(_worldFileName).exists()) {
            writeImageWorldFile(_worldFileName, _worldFilePara);
        }
    }

    /**
     * Get x rotate(shear)
     *
     * @return X rotate
     */
    public double getXRotate() {
        return _worldFilePara.xRotate;
    }

    /**
     * Set x rotate(shear)
     *
     * @param value Value
     * @throws IOException
     */
    public void setXRotate(double value) throws IOException {
        _worldFilePara.xRotate = value;
        if (new File(_worldFileName).exists()) {
            writeImageWorldFile(_worldFileName, _worldFilePara);
        }
    }

    /**
     * Get y rotate(shear)
     *
     * @return X rotate
     */
    public double getYRotate() {
        return _worldFilePara.yRotate;
    }

    /**
     * Set y rotate(shear)
     *
     * @param value Value
     * @throws IOException
     */
    public void setYRotate(double value) throws IOException {
        _worldFilePara.yRotate = value;
        if (new File(_worldFileName).exists()) {
            writeImageWorldFile(_worldFileName, _worldFilePara);
        }
    }

    /**
     * Get interpolation
     *
     * @return Interpolation
     */
    public Object getInterpolation() {
        return this.interp;
    }

    /**
     * Get interpolation string
     *
     * @return Interpolation string
     */
    public String getInterpolationStr() {
        if (interp == RenderingHints.VALUE_INTERPOLATION_BILINEAR) {
            return "bilinear";
        } else if (interp == RenderingHints.VALUE_INTERPOLATION_BICUBIC) {
            return "bicubic";
        } else {
            return "nearest";
        }
    }

    /**
     * Set interpolation object
     *
     * @param value Interpolation object
     */
    public void setInterpolation(Object value) {
        this.interp = value;
    }

    /**
     * Set interpolation string
     *
     * @param value Interpolation string
     */
    public void setInterpolation(String value) {
        switch (value) {
            case "nearest":
                this.interp = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                break;
            case "bilinear":
                this.interp = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                break;
            case "bicubic":
                this.interp = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                break;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Read image world file
     *
     * @param aIFile Image world file path
     * @throws java.io.FileNotFoundException
     */
    public void readImageWorldFile(String aIFile) throws FileNotFoundException, IOException {
        BufferedReader sr = new BufferedReader(new FileReader(new File(aIFile)));

        _worldFilePara.xScale = Double.parseDouble(sr.readLine());
        _worldFilePara.yRotate = Double.parseDouble(sr.readLine());
        _worldFilePara.xRotate = Double.parseDouble(sr.readLine());
        _worldFilePara.yScale = Double.parseDouble(sr.readLine());
        _worldFilePara.xUL = Double.parseDouble(sr.readLine());
        _worldFilePara.yUL = Double.parseDouble(sr.readLine());
        sr.close();
    }

    /**
     * Write image world file
     *
     * @param aFile File path
     * @param aWFP WorldFilePara
     * @throws java.io.IOException
     */
    public void writeImageWorldFile(String aFile, WorldFilePara aWFP) throws IOException {
        BufferedWriter sw = new BufferedWriter(new FileWriter(new File(aFile)));
        sw.write(String.valueOf(aWFP.xScale));
        sw.newLine();
        sw.write(String.valueOf(aWFP.yRotate));
        sw.newLine();
        sw.write(String.valueOf(aWFP.xRotate));
        sw.newLine();
        sw.write(String.valueOf(aWFP.yScale));
        sw.newLine();
        sw.write(String.valueOf(aWFP.xUL));
        sw.newLine();
        sw.write(String.valueOf(aWFP.yUL));
        sw.close();
    }

    /**
     * Get colors from palette file
     *
     * @param pFile Palette file path
     * @return Colors
     */
    public List<Color> getColorsFromPaletteFile(String pFile) {
        BufferedReader sr;
        try {
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(pFile)));
            sr.readLine();
            String aLine = sr.readLine();
            String[] dataArray;
            List<Color> colors = new ArrayList<>();
            while (aLine != null) {
                if (aLine.isEmpty()) {
                    aLine = sr.readLine();
                    continue;
                }

                aLine = aLine.trim();
                dataArray = aLine.split("\\s+");
                colors.add(new Color(Integer.parseInt(dataArray[3]), Integer.parseInt(dataArray[2]),
                        Integer.parseInt(dataArray[1])));

                aLine = sr.readLine();
            }
            sr.close();

            return colors;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImageLayer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(ImageLayer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Set palette
     *
     * @param colors Colors
     */
    public void setPalette(List<Color> colors) {
        Raster imageData = _image.getData();

        for (int i = 0; i < _image.getWidth(); i++) {
            for (int j = 0; j < _image.getHeight(); j++) {
                _image.setRGB(i, _image.getHeight() - j - 1, colors.get(imageData.getSample(i, j, 0)).getRGB());
            }
        }
    }

    /**
     * Set color palette to a image from a palette file
     *
     * @param aFile File path
     */
    public void setPalette(String aFile) {
        List<Color> colors = getColorsFromPaletteFile(aFile);

        setPalette(colors);
    }
    // </editor-fold>

    // <editor-fold desc="BeanInfo">
    public class ImageLayerBean {

        ImageLayerBean() {
        }

        // <editor-fold desc="Get Set Methods">
        /**
         * Get layer type
         *
         * @return Layer type
         */
        public LayerTypes getLayerType() {
            return ImageLayer.this.getLayerType();
        }

        /**
         * Set layer type
         *
         * @param lt Layer type
         */
        public void setLayerType(LayerTypes lt) {
            ImageLayer.this.setLayerType(lt);
        }

        /**
         * Get layer draw type
         *
         * @return Layer draw type
         */
        public LayerDrawType getLayerDrawType() {
            return ImageLayer.this.getLayerDrawType();
        }

        /**
         * Set layer draw type
         *
         * @param ldt Layer draw type
         */
        public void setLayerDrawType(LayerDrawType ldt) {
            ImageLayer.this.setLayerDrawType(ldt);
        }

        /**
         * Get file name
         *
         * @return File name
         */
        public String getFileName() {
            return ImageLayer.this.getFileName();
        }

        /**
         * Set file name
         *
         * @param fn File name
         */
        public void setFileName(String fn) {
            ImageLayer.this.setFileName(fn);
        }

        /**
         * Get layer handle
         *
         * @return Layer handle
         */
        public int getHandle() {
            return ImageLayer.this.getHandle();
        }

        /**
         * Get layer name
         *
         * @return Layer name
         */
        public String getLayerName() {
            return ImageLayer.this.getLayerName();
        }

        /**
         * Set layer name
         *
         * @param name Layer name
         */
        public void setLayerName(String name) {
            ImageLayer.this.setLayerName(name);
        }

        /**
         * Get if is maskout
         *
         * @return If is maskout
         */
        public boolean isMaskout() {
            return ImageLayer.this.isMaskout();
        }

        /**
         * Set if maskout
         *
         * @param value If maskout
         */
        public void setMaskout(boolean value) {
            ImageLayer.this.setMaskout(value);
        }

        /**
         * Get if is visible
         *
         * @return If is visible
         */
        public boolean isVisible() {
            return ImageLayer.this.isVisible();
        }

        /**
         * Set if is visible
         *
         * @param value If is visible
         */
        public void setVisible(boolean value) {
            ImageLayer.this.setVisible(value);
        }

        /**
         * Get world file name of the layer
         *
         * @return World file name
         */
        public String getWorldFileName() {
            return _worldFileName;
        }

        /**
         * Get if set transparency color
         *
         * @return Boolean
         */
        public boolean isUseTransColor() {
            return _isSetTransColor;
        }

        /**
         * Set if using transparency color
         *
         * @param istrue Boolean
         */
        public void setUseTransColor(boolean istrue) {
            _isSetTransColor = istrue;
            if (istrue) {
                Image image = GlobalUtil.makeColorTransparent(_image, _transparencyColor);
                _image = GlobalUtil.imageToBufferedImage(image);
            }
        }

        /**
         * Get transparency color
         *
         * @return Transparency color
         */
        public Color getTransparencyColor() {
            return _transparencyColor;
        }

        /**
         * Set transparency color
         *
         * @param color The color
         */
        public void setTransparencyColor(Color color) {
            _transparencyColor = color;
            if (_isSetTransColor) {
                Image image = GlobalUtil.makeColorTransparent(_image, _transparencyColor);
                _image = GlobalUtil.imageToBufferedImage(image);
            }
        }

        /**
         * Get transparency percent
         *
         * @return Transparency percent
         */
        public int getTransparency() {
            return ImageLayer.this.getTransparency();
        }

        /**
         * Set transparency percent
         *
         * @param value Transparency percent
         */
        public void setTransparency(int value) {
            ImageLayer.this.setTransparency(value);
        }

        /**
         * Get X upper-left
         *
         * @return Upper-left x value
         */
        public double getXUL() {
            return _worldFilePara.xUL;
        }

        /**
         * Set upper-left x
         *
         * @param value The value
         * @throws java.io.IOException
         */
        public void setXUL(double value) throws IOException {
            _worldFilePara.xUL = value;
            Extent aExtent = (Extent) ImageLayer.this.getExtent().clone();
            aExtent.minX = _worldFilePara.xUL;
            aExtent.maxX = _worldFilePara.xUL + ImageLayer.this.getExtent().getWidth();
            ImageLayer.this.setExtent(aExtent);
            if (new File(_worldFileName).exists()) {
                writeImageWorldFile(_worldFileName, _worldFilePara);
            }
        }

        /**
         * Get y upper-left
         *
         * @return Upper-left y
         */
        public double getYUL() {
            return _worldFilePara.yUL;
        }

        /**
         * Set upper-left y
         *
         * @param value The value
         * @throws java.io.IOException
         */
        public void setYUL(double value) throws IOException {
            _worldFilePara.yUL = value;
            Extent aExtent = (Extent) ImageLayer.this.getExtent().clone();
            aExtent.maxY = _worldFilePara.yUL;
            aExtent.minY = _worldFilePara.yUL - ImageLayer.this.getExtent().getHeight();
            ImageLayer.this.setExtent(aExtent);
            if (new File(_worldFileName).exists()) {
                writeImageWorldFile(_worldFileName, _worldFilePara);
            }
        }

        /**
         * Get x scale
         *
         * @return The x scale
         */
        public double getXScale() {
            return _worldFilePara.xScale;
        }

        /**
         * Set x scale
         *
         * @param value The value
         * @throws java.io.IOException
         */
        public void setXScale(double value) throws IOException {
            _worldFilePara.xScale = value;
            Extent aExtent = (Extent) ImageLayer.this.getExtent();
            double width = _image.getWidth() * _worldFilePara.xScale;
            aExtent.maxX = _worldFilePara.xUL + width;
            ImageLayer.this.setExtent(aExtent);
            if (new File(_worldFileName).exists()) {
                writeImageWorldFile(_worldFileName, _worldFilePara);
            }
        }

        /**
         * Get y scale
         *
         * @return The y scale
         */
        public double getYScale() {
            return _worldFilePara.yScale;
        }

        public void setYScale(double value) throws IOException {
            _worldFilePara.yScale = value;
            Extent aExtent = (Extent) ImageLayer.this.getExtent();
            double height = _image.getHeight() * _worldFilePara.yScale;
            aExtent.minY = _worldFilePara.yUL + height;
            ImageLayer.this.setExtent(aExtent);
            if (new File(_worldFileName).exists()) {
                writeImageWorldFile(_worldFileName, _worldFilePara);
            }
        }

        /**
         * Get x rotate(shear)
         *
         * @return X rotate
         */
        public double getXRotate() {
            return _worldFilePara.xRotate;
        }

        /**
         * Set x rotate(shear)
         *
         * @param value Value
         * @throws IOException
         */
        public void setXRotate(double value) throws IOException {
            _worldFilePara.xRotate = value;
            if (new File(_worldFileName).exists()) {
                writeImageWorldFile(_worldFileName, _worldFilePara);
            }
        }

        /**
         * Get y rotate(shear)
         *
         * @return X rotate
         */
        public double getYRotate() {
            return _worldFilePara.yRotate;
        }

        /**
         * Set y rotate(shear)
         *
         * @param value Value
         * @throws IOException
         */
        public void setYRotate(double value) throws IOException {
            _worldFilePara.yRotate = value;
            if (new File(_worldFileName).exists()) {
                writeImageWorldFile(_worldFileName, _worldFilePara);
            }
        }

        /**
         * Get interpolation
         *
         * @return Interpolation
         */
        public String getInterpolation() {
            if (interp == RenderingHints.VALUE_INTERPOLATION_BILINEAR) {
                return "bilinear";
            } else if (interp == RenderingHints.VALUE_INTERPOLATION_BICUBIC) {
                return "bicubic";
            } else {
                return "nearest";
            }
        }

        /**
         * Set interpolation
         *
         * @param value Interpolation
         */
        public void setInterpolation(String value) {
            switch (value) {
                case "nearest":
                    interp = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                    break;
                case "bilinear":
                    interp = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                    break;
                case "bicubic":
                    interp = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                    break;
            }
        }
        // </editor-fold>
    }

    public static class ImageLayerBeanBeanInfo extends BaseBeanInfo {

        public ImageLayerBeanBeanInfo() {
            super(ImageLayerBean.class);
            addProperty("fileName").setCategory("Read only").setReadOnly().setDisplayName("File name");
            addProperty("layerType").setCategory("Read only").setReadOnly().setDisplayName("Layer type");
            addProperty("layerDrawType").setCategory("Read only").setReadOnly().setDisplayName("Layer draw type");
            addProperty("handle").setCategory("Read only").setReadOnly().setDisplayName("Handle");
            addProperty("layerName").setCategory("Editable").setDisplayName("Layer name");
            addProperty("visible").setCategory("Editable").setDisplayName("Visible");
            addProperty("maskout").setCategory("Editable").setDisplayName("Is maskout");
            addProperty("transparency").setCategory("Editable").setDisplayName("Transparency Percent");
            addProperty("useTransColor").setCategory("Editable").setDisplayName("If use transparency color");
            addProperty("transparencyColor").setCategory("Editable").setDisplayName("Transparency color");
            addProperty("xScale").setCategory("Editable").setDisplayName("X scale");
            addProperty("yScale").setCategory("Editable").setDisplayName("Y scale");
            addProperty("xUL").setCategory("Editable").setDisplayName("X upper left");
            addProperty("yUL").setCategory("Editable").setDisplayName("Y upper left");
            addProperty("xRotate").setCategory("Editable").setDisplayName("X rotate");
            addProperty("yRotate").setCategory("Editable").setDisplayName("Y rotate");
            ExtendedPropertyDescriptor e = addProperty("interpolation");
            e.setCategory("Editable").setPropertyEditorClass(InterpolationEditor.class);
            e.setDisplayName("Interpolation");
        }
    }

    public static class InterpolationEditor extends ComboBoxPropertyEditor {

        public InterpolationEditor() {
            super();
            String[] names = new String[3];
            names[0] = "nearest";
            names[1] = "bilinear";
            names[2] = "bicubic";
            setAvailableValues(names);
        }
    }
    // </editor-fold>
}
