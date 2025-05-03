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
package org.meteoinfo.geo.mapdata;

import org.meteoinfo.common.DataConvert;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.PointD;
import org.meteoinfo.common.io.IOUtil;
import org.meteoinfo.common.util.GlobalUtil;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.mapdata.MapDataType;
import org.meteoinfo.data.mapdata.geotiff.GeoTiff;
import org.meteoinfo.data.meteodata.ascii.ASCIIGridDataInfo;
import org.meteoinfo.data.meteodata.ascii.SurferGridDataInfo;
import org.meteoinfo.data.meteodata.bandraster.BILDataInfo;
import org.meteoinfo.geo.layer.*;
import org.meteoinfo.geo.legend.LegendManage;
import org.meteoinfo.geo.meteodata.DrawMeteoData;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.LegendType;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.geometry.geoprocess.GeometryUtil;
import org.meteoinfo.image.ImageUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.table.Field;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yaqiang
 */
public class MapDataManage {

    /**
     * Can open or not as a map layer
     *
     * @param fileName File name
     * @return MapDataType
     * @throws FileNotFoundException
     */
    public static MapDataType canOpen(String fileName) throws FileNotFoundException, IOException {
        MapDataType mdt = null;
        if (new File(fileName).isFile()) {
            String ext = GlobalUtil.getFileExtension(fileName);
            switch (ext.toLowerCase()) {
                case "shp":
                    mdt = MapDataType.SHAPE;
                    break;
                case "wmp":
                    mdt = MapDataType.WMP;
                    break;
                case "bmp":
                case "gif":
                case "jpg":
                case "png":
                    mdt = MapDataType.IMAGE;
                    break;
                case "tif":
                    if (GeoTiff.isGeoTiff(fileName))
                        mdt = MapDataType.GEO_TIFF;
                    else
                        mdt = MapDataType.IMAGE;
                    break;
                case "bil":
                case "bip":
                case "bsq":
                    mdt = MapDataType.BIL;
                    break;
                default:
                    RandomAccessFile br = new RandomAccessFile(fileName, "r");
                    byte[] bytes = new byte[100];
                    br.read(bytes);
                    String line = new String(bytes).trim().toUpperCase();
                    br.close();
                    if (line.contains("NCOLS")) {
                        mdt = MapDataType.ESRI_ASCII_GRID;
                    } else if (line.contains("DSAA")) {
                        mdt = MapDataType.SURFER_ASCII_GRID;
                    } else {
                        mdt = MapDataType.GRADS;
                    }
                    break;
            }
        }

        return mdt;
    }

    /**
     * Load a layer from a file
     *
     * @param aFile The file path
     * @return The layer
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static MapLayer loadLayer(String aFile) throws IOException, FileNotFoundException, Exception {
        MapDataType mdt = canOpen(aFile);
        if (mdt == null) {
            return null;
        }

        MapLayer aLayer = null;
        switch (mdt) {
            case SHAPE:
                aLayer = readMapFile_ShapeFile(aFile);
                break;
            case WMP:
                aLayer = readMapFile_WMP(aFile);
                break;
            case IMAGE:
                aLayer = readImageFile(aFile);
                break;
            case GEO_TIFF:
                aLayer = readGeoTiffFile(aFile);
                break;
            case BIL:
                aLayer = readBILFile(aFile);
                break;
            case ESRI_ASCII_GRID:
                aLayer = readESRI_ASCII_GRID(aFile);
                break;
            case SURFER_ASCII_GRID:
                aLayer = readSurfer_ASCII_GRID(aFile);
                break;
            case GRADS:
                aLayer = readMapFile_GrADS(aFile);
                break;
        }

        if (aLayer != null) {
            switch (mdt) {
                case BIL:
                case ESRI_ASCII_GRID:
                case SURFER_ASCII_GRID:
                    String projFn = aFile.substring(0, aFile.length() - 4) + ".prj";
                    File pFile = new File(projFn);
                    if (pFile.isFile()) {
                        ProjectionInfo projInfo = ShapeFileManage.loadProjFile(pFile);
                        aLayer.setProjInfo(projInfo);
                    }
                    break;
            }
        }

        return aLayer;
    }

    /**
     * Load a layer from a file with a certain projection
     *
     * @param aFile The file name
     * @param projInfo The projection
     * @return The layer
     * @throws IOException
     * @throws FileNotFoundException
     * @throws Exception
     */
    public static MapLayer loadLayer(String aFile, ProjectionInfo projInfo) throws IOException, FileNotFoundException, Exception {
        MapLayer layer = loadLayer(aFile);
        layer.setProjInfo(projInfo);

        return layer;
    }

    /**
     * Read shape file as map
     *
     * @param fn File name
     * @return Vector layer
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static VectorLayer readMapFile_ShapeFile(String fn) throws IOException, FileNotFoundException, Exception {
        String cpgfilepath = fn.replaceFirst(fn.substring(fn.lastIndexOf(".")), ".cpg");
        File cpgFile = new File(cpgfilepath);
        String encoding;
        if (cpgFile.exists()){
            BufferedReader sr = new BufferedReader(new FileReader(cpgFile));
            String ec = sr.readLine().trim();
            sr.close();
            encoding = ec;
        } else {
            encoding = IOUtil.encodingDetectShp(fn);
            if (encoding.equals("ISO8859_1")) {
                encoding = "UTF-8";
            }
        }

        return readMapFile_ShapeFile(fn, encoding);
    }

    /**
     * Read shape file as map
     *
     * @param aFile File name
     * @param encoding Encoding
     * @return Vector layer
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static VectorLayer readMapFile_ShapeFile(String aFile, String encoding) throws IOException, FileNotFoundException, Exception {
        VectorLayer aLayer = ShapeFileManage.loadShapeFile(aFile, encoding);

        return aLayer;
    }

    /**
     * Read GrADS map file
     *
     * @param aFile The file path
     * @return The layer
     * @throws FileNotFoundException
     */
    public static VectorLayer readMapFile_GrADS(String aFile) throws FileNotFoundException, IOException, Exception {
        DataInputStream br = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(aFile))));
        int i, lineNum;
        byte b;
        short N, lType;
        double lon, lat;
        byte[] bytes;

        PointD aPoint;
        List<PointD> pList = new ArrayList<>();

        VectorLayer aLayer = new VectorLayer(ShapeTypes.POLYLINE);
        String columnName = "Value";
        Field aDC = new Field(columnName, DataType.INT);
        aLayer.editAddField(aDC);

        lineNum = 0;
        do {
            b = br.readByte();    // 1-data, 2-skip
            if ("2".equals(Byte.toString(b))) {
                br.skipBytes(18);
                continue;
            }
            b = br.readByte();    // Line type: country, river ...
            lType = (short) DataConvert.byte2Int(b);
            b = br.readByte();   // Point number
            N = (short) DataConvert.byte2Int(b);
            for (i = 0; i < N; i++) {
                bytes = new byte[3];
                br.read(bytes);    //Longitude
                int val = 0;
                for (int bb = 0; bb < 3; bb++) {
                    val <<= 8;
                    val |= (int) bytes[bb] & 0xFF;
                }
                lon = val / 10000.0;

                bytes = new byte[3];
                br.read(bytes);    //Latitude
                val = 0;
                for (int bb = 0; bb < 3; bb++) {
                    val <<= 8;
                    val |= (int) bytes[bb] & 0xFF;
                }
                lat = val / 10000.0 - 90.0;

                aPoint = new PointD();
                aPoint.X = lon;
                aPoint.Y = lat;
                pList.add(aPoint);
            }
            if (pList.size() > 1) {
                PolylineShape aPolyline = new PolylineShape();
                aPolyline.setValue(lineNum);
                aPolyline.setPoints(pList);
                aPolyline.setExtent(GeometryUtil.getPointsExtent(pList));
                aPolyline.setPartNum(1);
                aPolyline.parts = new int[1];
                aPolyline.parts[0] = 0;

                int shapeNum = aLayer.getShapeNum();
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue(columnName, shapeNum, lineNum);
                }

                lineNum++;
            }
            pList = new ArrayList<>();

        } while (br.available() > 0);

        br.close();

        aLayer.setLayerName(new File(aFile).getName());
        aLayer.setFileName(aFile);
        aLayer.setLayerDrawType(LayerDrawType.MAP);
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYLINE, Color.darkGray, 1.0F));
        aLayer.setVisible(true);

        return aLayer;
    }

    /**
     * Read image file
     *
     * @param aFile File path
     * @return Image layer
     * @throws IOException
     */
    public static ImageLayer readImageFile(String aFile) throws IOException {
        String oEx = aFile.substring(aFile.lastIndexOf("."));
        String last = oEx.substring(oEx.length() - 1);
        String sEx = oEx.substring(0, oEx.length() - 2) + last;
        sEx = sEx + "w";
        String wFile = aFile.replace(oEx, sEx);
        BufferedImage aImage = ImageUtil.imageLoad(aFile);
        ImageLayer aImageLayer = new ImageLayer();
        aImageLayer.setFileName(aFile);
        aImageLayer.setWorldFileName(wFile);
        aImageLayer.setImage(aImage);
        aImageLayer.setLayerName(new File(aFile).getName());
        aImageLayer.setVisible(true);
        if (new File(wFile).exists()) {
            aImageLayer.readImageWorldFile(wFile);
        } else {
            WorldFilePara aWFP = new WorldFilePara();
            aWFP.xUL = 0;
            aWFP.yUL = 90;
            aWFP.xScale = 0.05;
            aWFP.yScale = -0.05;
            aWFP.xRotate = 0;
            aWFP.yRotate = 0;
            aImageLayer.setWorldFilePara(aWFP);
            aImageLayer.writeImageWorldFile(wFile, aImageLayer.getWorldFilePara());
        }

        double XBR, YBR;
        XBR = aImageLayer.getImage().getWidth() * aImageLayer.getWorldFilePara().xScale + aImageLayer.getWorldFilePara().xUL;
        YBR = aImageLayer.getImage().getHeight() * aImageLayer.getWorldFilePara().yScale + aImageLayer.getWorldFilePara().yUL;
        Extent aExtent = new Extent();
        aExtent.minX = aImageLayer.getWorldFilePara().xUL;
        aExtent.minY = YBR;
        aExtent.maxX = XBR;
        aExtent.maxY = aImageLayer.getWorldFilePara().yUL;
        aImageLayer.setExtent(aExtent);
        aImageLayer.setLayerDrawType(LayerDrawType.IMAGE);
        aImageLayer.setMaskout(true);

        return aImageLayer;
    }

    /**
     * Create a raster layer from geotiff file
     *
     * @param fileName File path
     * @return Raster layer
     */
    public static RasterLayer readGeoTiffFile(String fileName) {
        try {
            GeoTiff geoTiff = new GeoTiff(fileName);
            geoTiff.read();
            GridArray gData = geoTiff.getGridArray();
//            LegendScheme aLS = LegendManage.createLegendSchemeFromGridData(gData, LegendType.GraduatedColor,
//                    ShapeTypes.Image);
            RasterLayer aLayer = DrawMeteoData.createRasterLayer(gData, new File(fileName).getName());
            aLayer.setProjInfo(gData.projInfo);
            aLayer.setFileName(fileName);

            return aLayer;
        } catch (IOException ex) {
            Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Read BIL file and create a raster layer
     *
     * @param fileName BIL file name
     * @return Raster layer
     */
    public static RasterLayer readBILFile(String fileName) {
        BILDataInfo dataInfo = new BILDataInfo();
        dataInfo.readDataInfo(fileName);
        //GridData gData = dataInfo.getGridData_LonLat(0, 0, 0);
        Array a = dataInfo.read(dataInfo.getVariableNames().get(0));
        GridArray data = new GridArray();
        data.setData(a);
        data.xArray = dataInfo.getXDimension().getValues();
        data.yArray = dataInfo.getYDimension().getValues();
        data.missingValue = dataInfo.getMissingValue();
        LegendScheme aLS = LegendManage.createLegendSchemeFromGridData(data, LegendType.GRADUATED_COLOR,
                ShapeTypes.IMAGE);
        RasterLayer aLayer = DrawMeteoData.createRasterLayer(data, new File(fileName).getName(), aLS);
        aLayer.setFileName(fileName);

        return aLayer;
    }

    /**
     * Read ESRI ASCII grid file and create a raster layer
     *
     * @param fileName File name
     * @return Raster layer
     */
    public static RasterLayer readESRI_ASCII_GRID(String fileName) {
        ASCIIGridDataInfo dataInfo = new ASCIIGridDataInfo();
        dataInfo.readDataInfo(fileName);
        GridArray gData = dataInfo.getGridArray("var");
        RasterLayer aLayer = DrawMeteoData.createRasterLayer(gData, new File(fileName).getName());
        aLayer.setProjInfo(KnownCoordinateSystems.geographic.world.WGS1984);
        aLayer.setFileName(fileName);

        return aLayer;
    }

    /**
     * Read surfer ASCII grid file and create a raster layer
     *
     * @param fileName File name
     * @return Raster layer
     */
    public static RasterLayer readSurfer_ASCII_GRID(String fileName) {
        SurferGridDataInfo dataInfo = new SurferGridDataInfo();
        dataInfo.readDataInfo(fileName);
        GridData gData = dataInfo.getGridData_LonLat(0, "var", 0);
        LegendScheme aLS = LegendManage.createLegendSchemeFromGridData(gData, LegendType.GRADUATED_COLOR,
                ShapeTypes.IMAGE);
        RasterLayer aLayer = DrawMeteoData.createRasterLayer(gData, new File(fileName).getName(), aLS);
        aLayer.setFileName(fileName);

        return aLayer;
    }

    /**
     * Read WMP file
     *
     * @param fileName The file name
     * @return Created vector layer
     * @throws IOException
     */
    public static VectorLayer readMapFile_WMP(String fileName) throws IOException, Exception {
        BufferedReader sr = null;
        try {
            File file = new File(fileName);
            sr = new BufferedReader(new FileReader(file));
            String aLine;
            String shapeType;
            String[] dataArray;
            int shapeNum;
            int i, j, pNum;
            List<PointD> pList = new ArrayList<>();
            PointD aPoint;
            boolean IsTrue;
            String columnName = "Value";
            VectorLayer aLayer = new VectorLayer(ShapeTypes.POINT);
            //Read shape type
            shapeType = sr.readLine().trim().toLowerCase();
            //Read shape number
            shapeNum = Integer.parseInt(sr.readLine());
            switch (shapeType) {
                case "point":
                    aLayer = new VectorLayer(ShapeTypes.POINT);
                    aLayer.editAddField(columnName, DataType.INT);
                    for (i = 0; i < shapeNum; i++) {
                        aLine = sr.readLine();
                        dataArray = aLine.split(",");
                        aPoint = new PointD();
                        aPoint.X = Double.parseDouble(dataArray[0]);
                        aPoint.Y = Double.parseDouble(dataArray[1]);
                        pList.add(aPoint);
                        PointShape aPS = new PointShape();
                        aPS.setValue(i);
                        aPS.setPoint(aPoint);

                        int sNum = aLayer.getShapeNum();
                        if (aLayer.editInsertShape(aPS, sNum)) {
                            aLayer.editCellValue(columnName, sNum, i);
                        }
                    }
                    aLayer.setLayerName(file.getName());
                    aLayer.setFileName(fileName);
                    aLayer.setLayerDrawType(LayerDrawType.MAP);
                    aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, Color.black, 5));
                    aLayer.setVisible(true);
                    IsTrue = true;
                    break;
                case "polyline":
                    aLayer = new VectorLayer(ShapeTypes.POLYLINE);
                    aLayer.editAddField(columnName, DataType.INT);
                    for (i = 0; i < shapeNum; i++) {
                        pNum = Integer.parseInt(sr.readLine());
                        pList = new ArrayList<>();
                        for (j = 0; j < pNum; j++) {
                            aLine = sr.readLine();
                            dataArray = aLine.split(",");
                            aPoint = new PointD();
                            aPoint.X = Double.parseDouble(dataArray[0]);
                            aPoint.Y = Double.parseDouble(dataArray[1]);
                            pList.add(aPoint);
                        }
                        PolylineShape aPLS = new PolylineShape();
                        aPLS.setValue(i);
                        aPLS.setExtent(GeometryUtil.getPointsExtent(pList));
                        aPLS.setPoints(pList);

                        int sNum = aLayer.getShapeNum();
                        if (aLayer.editInsertShape(aPLS, sNum)) {
                            aLayer.editCellValue(columnName, sNum, i);
                        }
                    }
                    aLayer.setLayerName(file.getName());
                    aLayer.setFileName(fileName);
                    aLayer.setLayerDrawType(LayerDrawType.MAP);
                    aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYLINE, Color.darkGray, 1.0F));
                    aLayer.setVisible(true);
                    IsTrue = true;
                    break;
                case "polygon":
                    aLayer = new VectorLayer(ShapeTypes.POLYGON);
                    aLayer.editAddField(columnName, DataType.INT);
                    //ArrayList polygons = new ArrayList();
                    for (i = 0; i < shapeNum; i++) {
                        pNum = Integer.parseInt(sr.readLine());
                        pList = new ArrayList<>();
                        for (j = 0; j < pNum; j++) {
                            aLine = sr.readLine();
                            dataArray = aLine.split(",");
                            aPoint = new PointD();
                            aPoint.X = Double.parseDouble(dataArray[0]);
                            aPoint.Y = Double.parseDouble(dataArray[1]);
                            pList.add(aPoint);
                        }
                        PolygonShape aPGS = new PolygonShape();
                        aPGS.lowValue = i;
                        aPGS.highValue = i;
                        aPGS.setExtent(GeometryUtil.getPointsExtent(pList));
                        aPGS.setPoints(pList);

                        int sNum = aLayer.getShapeNum();
                        if (aLayer.editInsertShape(aPGS, sNum)) {
                            aLayer.editCellValue(columnName, sNum, i);
                        }
                    }
                    aLayer.setLayerName(file.getName());
                    aLayer.setFileName(fileName);
                    aLayer.setLayerDrawType(LayerDrawType.MAP);
                    aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, new Color(255, 251, 195), 1.0F));
                    aLayer.setVisible(true);
                    IsTrue = true;
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Shape type is invalid!" + System.getProperty("line.separator")
                            + shapeType);
                    IsTrue = false;
                    break;
            }
            sr.close();
            if (IsTrue) {
                return aLayer;
            } else {
                return null;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (sr != null) {
                    sr.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Write WMP file
     *
     * @param fileName The file name
     * @param shapes Shapes
     */
    public static void writeMapFile_WMP(String fileName, List<Shape> shapes) {
        BufferedWriter sw = null;
        try {
            File file = new File(fileName);
            sw = new BufferedWriter(new FileWriter(file));
            int shpNum = shapes.size();
            int i;
            switch (shapes.get(0).getShapeType()) {
                case POINT:
                    sw.write("Point");
                    sw.newLine();
                    sw.write(String.valueOf(shpNum));
                    sw.newLine();
                    PointShape aPS;
                    for (i = 0; i < shpNum; i++) {
                        aPS = (PointShape) shapes.get(i);
                        if (aPS.isSelected()) {
                            sw.write(String.valueOf(aPS.getPoint().X) + "," + String.valueOf(aPS.getPoint().Y));
                            sw.newLine();
                        }
                    }
                    break;
                case POLYLINE:
                case POLYLINE_Z:
                    sw.write("Polyline");
                    sw.newLine();
                    int shapeNum = 0;
                    PolylineShape aPLS;
                    for (i = 0; i < shpNum; i++) {
                        aPLS = (PolylineShape) shapes.get(i);
                        shapeNum += aPLS.getPartNum();
                    }
                    sw.write(String.valueOf(shapeNum));
                    sw.newLine();

                    shapeNum = 0;
                    for (i = 0; i < shpNum; i++) {
                        aPLS = (PolylineShape) shapes.get(i);
                        PointD[] Pointps;
                        for (int p = 0; p < aPLS.getPartNum(); p++) {
                            if (p == aPLS.getPartNum() - 1) {
                                Pointps = new PointD[aPLS.getPointNum() - aPLS.parts[p]];
                                for (int pp = aPLS.parts[p]; pp < aPLS.getPointNum(); pp++) {
                                    Pointps[pp - aPLS.parts[p]] = (PointD) aPLS.getPoints().get(pp);
                                }
                            } else {
                                Pointps = new PointD[aPLS.parts[p + 1] - aPLS.parts[p]];
                                for (int pp = aPLS.parts[p]; pp < aPLS.parts[p + 1]; pp++) {
                                    Pointps[pp - aPLS.parts[p]] = (PointD) aPLS.getPoints().get(pp);
                                }
                            }
                            sw.write(String.valueOf(Pointps.length));
                            sw.newLine();
                            for (PointD aPoint : Pointps) {
                                sw.write(String.valueOf(aPoint.X) + "," + String.valueOf(aPoint.Y));
                                sw.newLine();
                            }
                            shapeNum += 1;
                        }
                    }
                    break;
                case POLYGON:
                    sw.write("Polygon");
                    sw.newLine();
                    shapeNum = 0;
                    PolygonShape aPGS;
                    for (i = 0; i < shpNum; i++) {
                        aPGS = (PolygonShape) shapes.get(i);
                        shapeNum += aPGS.getPartNum();
                    }
                    sw.write(String.valueOf(shapeNum));
                    sw.newLine();

                    shapeNum = 0;
                    for (i = 0; i < shpNum; i++) {
                        aPGS = (PolygonShape) shapes.get(i);

                        PointD[] Pointps;
                        for (int p = 0; p < aPGS.getPartNum(); p++) {
                            if (p == aPGS.getPartNum() - 1) {
                                Pointps = new PointD[aPGS.getPointNum() - aPGS.parts[p]];
                                for (int pp = aPGS.parts[p]; pp < aPGS.getPointNum(); pp++) {
                                    Pointps[pp - aPGS.parts[p]] = (PointD) aPGS.getPoints().get(pp);
                                }
                            } else {
                                Pointps = new PointD[aPGS.parts[p + 1] - aPGS.parts[p]];
                                for (int pp = aPGS.parts[p]; pp < aPGS.parts[p + 1]; pp++) {
                                    Pointps[pp - aPGS.parts[p]] = (PointD) aPGS.getPoints().get(pp);
                                }
                            }
                            sw.write(String.valueOf(Pointps.length));
                            sw.newLine();
                            for (PointD aPoint : Pointps) {
                                sw.write(String.valueOf(aPoint.X) + "," + String.valueOf(aPoint.Y));
                                sw.newLine();
                            }
                            shapeNum += 1;
                        }
                    }
                    break;
            }
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Write projection file
     *
     * @param projFilePath Projection file path
     * @param projInfo Projection info
     */
    public static void writeProjFile(String projFilePath, ProjectionInfo projInfo) {
        BufferedWriter sw = null;
        try {
            String esriString = projInfo.toEsriString();
            sw = new BufferedWriter(new FileWriter(new File(projFilePath)));
            sw.write(esriString);
            sw.flush();
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(ShapeFileManage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ShapeFileManage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
