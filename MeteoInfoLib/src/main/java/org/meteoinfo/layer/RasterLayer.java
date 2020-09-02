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
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.global.GenericFileFilter;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.ndarray.Index;

/**
 *
 * @author yaqiang
 */
public class RasterLayer extends ImageLayer {
    // <editor-fold desc="Variables">

    //private LegendScheme _legendScheme;
    private GridArray _gridData;
    private GridArray _originGridData = null;
    private boolean _isProjected = false;
    private List<Color> _colors;
    //private InterpolationMode _interpMode = InterpolationMode.NearestNeighbor;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public RasterLayer() {
        this.setLayerType(LayerTypes.RasterLayer);
        this.setShapeType(ShapeTypes.Image);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Set legend scheme
     *
     * @param ls Legend scheme
     */
    @Override
    public void setLegendScheme(LegendScheme ls) {
        super.setLegendScheme(ls);
        if (ls == null) {
            updateImage(ls);
        } else if (ls.getBreakNum() < 200) {
            updateImage(ls);
        } else {
            setPaletteByLegend();
        }
    }

    /**
     * Get grid data
     *
     * @return Grid data
     */
    public GridArray getGridData() {
        return _gridData;
    }

    /**
     * Set grid data
     *
     * @param gdata Grid data
     */
    public void setGridData(GridArray gdata) {
        _gridData = gdata;
        updateGridData();
    }

    /**
     * Get if is projected
     *
     * @return Boolean
     */
    public boolean isProjected() {
        return _isProjected;
    }

    /**
     * Set if is projected
     *
     * @param istrue Boolean
     */
    public void setProjected(boolean istrue) {
        _isProjected = istrue;
    }

//        public InterpolationMode InterpMode
//        {
//            get { return _interpMode; }
//            set 
//            { 
//                _interpMode = value;
//                if (_interpMode == InterpolationMode.Invalid)
//                    _interpMode = InterpolationMode.NearestNeighbor;
//            }
//        }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get cell value by a point
     *
     * @param iIdx I index
     * @param jIdx J index
     * @return Cell value
     */
    public double getCellValue(int iIdx, int jIdx) {
        return _gridData.getDoubleValue(iIdx, jIdx);
    }

    /**
     * Update image by legend scheme
     *
     * @param als The legend scheme
     */
    public void updateImage(LegendScheme als) {
        BufferedImage image;
        if (_gridData.getData().getRank() <= 2) {
            image = getImageFromGridData(_gridData, als);
        } else {
            image = getRGBImage(_gridData);
            super.setLegendScheme(null);
        }
        this.setImage(image);
    }

    /**
     * Update image by legend scheme
     */
    public void updateImage() {
        BufferedImage image = getImageFromGridData(_gridData, this.getLegendScheme());
        this.setImage(image);
    }

    private BufferedImage getRGBImage(GridArray gdata) {
        int width, height, r, g, b;
        width = gdata.getXNum();
        height = gdata.getYNum();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Index index = gdata.getData().getIndex();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                r = gdata.getData().getInt(index);
                index.incr();
                g = gdata.getData().getInt(index);
                index.incr();
                b = gdata.getData().getInt(index);
                index.incr();
                image.setRGB(j, height - i - 1, new Color(r, g, b).getRGB());
            }
        }

        return image;
    }

    private BufferedImage getImageFromGridData(GridArray gdata, LegendScheme als) {
        int width, height, breakNum;
        width = gdata.getXNum();
        height = gdata.getYNum();
        breakNum = als.getBreakNum();
        double[] breakValue = new double[breakNum];
        Color[] breakColor = new Color[breakNum];
        Color undefColor = new Color(255, 255, 255, 0);
        Color defaultColor = als.getLegendBreaks().get(breakNum - 1).getColor();
        Color color;
        for (int i = 0; i < breakNum; i++) {
            breakValue[i] = Double.parseDouble(als.getLegendBreaks().get(i).getEndValue().toString());
            color = als.getLegendBreaks().get(i).getColor();
            breakColor[i] = color;
            if (als.getLegendBreaks().get(i).isNoData()) {
                undefColor = color;
            } else {
                defaultColor = color;
            }
        }
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //oneValue = gdata.data[i][j];
                oneValue = gdata.getDoubleValue(i, j);
                if (Double.isNaN(oneValue) || MIMath.doubleEquals(oneValue, gdata.missingValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = als.findLegendBreak(oneValue).getColor();
                }
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        return aImage;
    }

    private BufferedImage getImageFromGridData(GridArray gdata, List<Color> colors) {
        int width, height;
        width = gdata.getXNum();
        height = gdata.getYNum();
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int oneValue;
        Color oneColor;
        int n = colors.size();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                oneValue = gdata.getValue(i, j).intValue();
                oneColor = colors.get(oneValue);
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        return aImage;
    }

    /**
     * Set color palette to a image from a palette file
     *
     * @param aFile File path
     */
    @Override
    public void setPalette(String aFile) {
        List<Color> colors = this.getColorsFromPaletteFile(aFile);
        BufferedImage image = this.getImageFromGridData(_gridData, colors);
        this.setImage(image);

        LegendScheme ls = new LegendScheme(ShapeTypes.Image);
        ls.importFromPaletteFile_Unique(aFile);
        this.setLegendScheme(ls);
    }

    /**
     * Set color palette by legend scheme
     */
    public void setPaletteByLegend() {
        _colors = this.getLegendScheme().getColors();
        BufferedImage image = this.getImageFromGridData(_gridData, _colors);
        this.setImage(image);
    }

    /**
     * Update grid data
     */
    public void updateGridData() {
        WorldFilePara aWFP = new WorldFilePara();

        //aWFP.xUL = _gridData.xArray[0];
        //aWFP.yUL = _gridData.yArray[_gridData.getYNum() - 1];
        aWFP.xUL = _gridData.xArray[0] - _gridData.getXDelt() / 2;
        aWFP.yUL = _gridData.yArray[_gridData.getYNum() - 1] + _gridData.getYDelt() / 2;
        aWFP.xScale = _gridData.getXDelt();
        aWFP.yScale = -_gridData.getYDelt();

        aWFP.xRotate = 0;
        aWFP.yRotate = 0;

        this.setWorldFilePara(aWFP);

        updateExtent();
    }

    private void updateExtent() {
        double XBR, YBR;
        XBR = _gridData.getXNum() * this.getWorldFilePara().xScale + this.getWorldFilePara().xUL;
        YBR = _gridData.getYNum() * this.getWorldFilePara().yScale + this.getWorldFilePara().yUL;
        Extent aExtent = new Extent();
        aExtent.minX = this.getWorldFilePara().xUL;
        aExtent.minY = YBR;
        aExtent.maxX = XBR;
        aExtent.maxY = this.getWorldFilePara().yUL;
        this.setExtent(aExtent);
    }

    /**
     * Update origin data
     */
    public void updateOriginData() {
        _originGridData = (GridArray) _gridData.clone();
        _isProjected = true;
    }

    /**
     * Get origin data
     */
    public void getOriginData() {
        _gridData = (GridArray) _originGridData.clone();
    }

    /**
     * Save layer as a shape file
     */
    @Override
    public void saveFile() {
        File aFile = new File(this.getFileName());
        if (aFile.exists()) {
            saveFile(aFile.getAbsolutePath());
        } else {
            JFileChooser aDlg = new JFileChooser();
            String curDir = System.getProperty("user.dir");
            aDlg.setCurrentDirectory(new File(curDir));
            String[] fileExts = {"bil"};
            GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "BIL File (*.bil)");
            aDlg.addChoosableFileFilter(pFileFilter);
            aDlg.setFileFilter(pFileFilter);
            fileExts = new String[]{"grd"};
            pFileFilter = new GenericFileFilter(fileExts, "Surfer ASCII Grid File (*.grd)");
            aDlg.addChoosableFileFilter(pFileFilter);
            fileExts = new String[]{"asc"};
            pFileFilter = new GenericFileFilter(fileExts, "ESRI ASCII Grid File (*.asc)");
            aDlg.addChoosableFileFilter(pFileFilter);
            aDlg.setAcceptAllFileFilterUsed(false);
            if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(null)) {
                aFile = aDlg.getSelectedFile();
                System.setProperty("user.dir", aFile.getParent());
                String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
                String fileName = aFile.getAbsolutePath();
                if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                    fileName = fileName + "." + extent;
                }
                saveFile(fileName);
            }
        }
    }

    /**
     * Save layer as a file
     *
     * @param fileName File name
     */
    @Override
    public void saveFile(String fileName) {        
        this.saveFile(fileName, this.getProjInfo());
    }
    
    /**
     * Save layer as a file
     *
     * @param fileName File name
     * @param projInfo Projection information
     */
    public void saveFile(String fileName, ProjectionInfo projInfo) {        
        File aFile = new File(fileName);
        if (aFile.exists()) {
            int n = JOptionPane.showConfirmDialog(null, "Overwirte the existing file?", "Overwrite confirm", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return;
            }
        }
        try {
            this.setFileName(fileName);
            String ext = GlobalUtil.getFileExtension(fileName);
            switch (ext) {
                case "bil":
                    this._gridData.saveAsBILFile(fileName);
                    break;
                case "grd":
                    this._gridData.saveAsSurferASCIIFile(fileName);
                    break;
                case "asc":
                    this._gridData.saveAsESRIASCIIFile(fileName);
                    break;
                default:
                    return;
            }
            if (!this.getProjInfo().isLonLat()) {
                String projFn = fileName.substring(0, fileName.length() - 3) + "prj";
                MapDataManage.writeProjFile(projFn, projInfo);
            }
        } catch (IOException ex) {
            Logger.getLogger(RasterLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // </editor-fold>
    // <editor-fold desc="BeanInfo">
    public class RasterLayerBean {

        RasterLayerBean() {
        }

        // <editor-fold desc="Get Set Methods">
        /**
         * Get layer type
         *
         * @return Layer type
         */
        public LayerTypes getLayerType() {
            return RasterLayer.this.getLayerType();
        }

        /**
         * Set layer type
         *
         * @param lt Layer type
         */
        public void setLayerType(LayerTypes lt) {
            RasterLayer.this.setLayerType(lt);
        }

        /**
         * Get layer draw type
         *
         * @return Layer draw type
         */
        public LayerDrawType getLayerDrawType() {
            return RasterLayer.this.getLayerDrawType();
        }

        /**
         * Set layer draw type
         *
         * @param ldt Layer draw type
         */
        public void setLayerDrawType(LayerDrawType ldt) {
            RasterLayer.this.setLayerDrawType(ldt);
        }

        /**
         * Get file name
         *
         * @return File name
         */
        public String getFileName() {
            return RasterLayer.this.getFileName();
        }

        /**
         * Set file name
         *
         * @param fn File name
         */
        public void setFileName(String fn) {
            RasterLayer.this.setFileName(fn);
        }

        /**
         * Get layer handle
         *
         * @return Layer handle
         */
        public int getHandle() {
            return RasterLayer.this.getHandle();
        }

        /**
         * Get layer name
         *
         * @return Layer name
         */
        public String getLayerName() {
            return RasterLayer.this.getLayerName();
        }

        /**
         * Set layer name
         *
         * @param name Layer name
         */
        public void setLayerName(String name) {
            RasterLayer.this.setLayerName(name);
        }

        /**
         * Get if is maskout
         *
         * @return If is maskout
         */
        public boolean isMaskout() {
            return RasterLayer.this.isMaskout();
        }

        /**
         * Set if maskout
         *
         * @param value If maskout
         */
        public void setMaskout(boolean value) {
            RasterLayer.this.setMaskout(value);
        }

        /**
         * Get if is visible
         *
         * @return If is visible
         */
        public boolean isVisible() {
            return RasterLayer.this.isVisible();
        }

        /**
         * Set if is visible
         *
         * @param value If is visible
         */
        public void setVisible(boolean value) {
            RasterLayer.this.setVisible(value);
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

    public static class RasterLayerBeanBeanInfo extends BaseBeanInfo {

        public RasterLayerBeanBeanInfo() {
            super(RasterLayer.RasterLayerBean.class);
            addProperty("fileName").setCategory("Read only").setReadOnly().setDisplayName("File name");
            addProperty("layerType").setCategory("Read only").setReadOnly().setDisplayName("Layer type");
            addProperty("layerDrawType").setCategory("Read only").setReadOnly().setDisplayName("Layer draw type");
            addProperty("handle").setCategory("Read only").setReadOnly().setDisplayName("Handle");
            addProperty("layerName").setCategory("Editable").setDisplayName("Layer name");
            addProperty("visible").setCategory("Editable").setDisplayName("Visible");
            addProperty("maskout").setCategory("Editable").setDisplayName("Is maskout");
            ExtendedPropertyDescriptor e = addProperty("interpolation");
            e.setCategory("Editable").setPropertyEditorClass(InterpolationEditor.class);
            e.setDisplayName("Interpolation");
        }
    }
    // </editor-fold>
}
