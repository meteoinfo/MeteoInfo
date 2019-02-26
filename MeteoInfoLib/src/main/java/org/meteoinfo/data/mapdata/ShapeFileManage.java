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
package org.meteoinfo.data.mapdata;

import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointD;
import org.meteoinfo.io.EndianDataOutputStream;
import org.meteoinfo.layer.LayerDrawType;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.PolylineZShape;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.proj4j.CRSFactory;
import org.meteoinfo.projection.proj4j.CoordinateReferenceSystem;
import org.meteoinfo.shape.PointM;
import org.meteoinfo.shape.PointZShape;
import org.meteoinfo.shape.PolygonMShape;
import org.meteoinfo.shape.PolygonZShape;

/**
 * Shape file read and write
 *
 * @author yaqiang
 */
public class ShapeFileManage {
    
    private final static String ENCODING = "UTF-8";

    /**
     * Load shape file
     *
     * @param shpfilepath Shape file path
     * @return Vector layer
     * @throws IOException
     * @throws java.io.FileNotFoundException
     */
    public static VectorLayer loadShapeFile(String shpfilepath) throws IOException, FileNotFoundException, Exception {
        String cpgfilepath = shpfilepath.replaceFirst(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".cpg");
        File cpgFile = new File(cpgfilepath);
        String encoding = ENCODING;
        if (cpgFile.exists()){
            BufferedReader sr = new BufferedReader(new FileReader(cpgFile));
            String ec = sr.readLine().trim();
            sr.close();
            encoding = ec;
        }
        return loadShapeFile(shpfilepath, encoding);
    }
    
    /**
     * Load shape file
     *
     * @param shpfilepath Shape file path
     * @param encoding Encoding
     * @return Vector layer
     * @throws IOException
     * @throws java.io.FileNotFoundException
     */
    public static VectorLayer loadShapeFile(String shpfilepath, String encoding) throws IOException, FileNotFoundException, Exception {
        //Set file names
        String shxfilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".shx");
        String dbffilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".dbf");
        String projfilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".prj");
        File shpFile = new File(shpfilepath);
        File dbfFile = new File(dbffilepath);
        File shxFile = new File(shxfilepath);
        File prjFile = new File(projfilepath);
        if (!shxFile.exists()) {
            shxfilepath = shxfilepath.replace(".shx", ".SHX");
            shxFile = new File(shxfilepath);
        }
        if (!dbfFile.exists()) {
            dbffilepath = dbffilepath.replace(".dbf", ".DBF");
            dbfFile = new File(dbffilepath);
        }
        if (!prjFile.exists()) {
            projfilepath = projfilepath.replace(".prj", ".PRJ");
            prjFile = new File(projfilepath);
        }

        //Read shx file 
        if ("".equals(shxfilepath)) {
            //  MessageBox.Show("Open shx file error"); 
            return null;
        }

        long BytesSum = shxFile.length();  //Get file byte length   
        int shapeNum = (int) (BytesSum - 100) / 8;  //Get total number of records   
        loadShxFile(shxFile);

        //Open shp file 
        DataInputStream br = new DataInputStream(new BufferedInputStream(new FileInputStream(shpFile)));
        VectorLayer aLayer;
        //byte[] arr = new byte[(int)shpFile.length()];
        byte[] arr = new byte[100];
        br.read(arr);
        ByteBuffer buffer = ByteBuffer.wrap(arr);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        ((Buffer)buffer).position(32);
        //br.skipBytes(32);  //先读出36个字节,紧接着是Box边界合 
        int aShapeType = buffer.getInt();
        ShapeTypes aST = ShapeTypes.valueOf(aShapeType);
        //aLayer = new VectorLayer(aST);
        Extent aExtent = new Extent();
        aExtent.minX = buffer.getDouble();  //读出整个shp图层的边界合 
        aExtent.minY = buffer.getDouble();
        aExtent.maxX = buffer.getDouble();
        aExtent.maxY = buffer.getDouble();

        //br.skipBytes(32);  //   shp中尚未使用的边界盒     
        //buffer.position(buffer.position() + 32);

        //Get Shape Data             
        switch (aST) {
            case Point://single point                                                              
                aLayer = readPointShapes(br, shapeNum);
                break;
            case PointZ:
                aLayer = readPointZShapes(br, shapeNum);
                break;
            case Polyline:    //Polyline layer       
                aLayer = readPolylineShapes(br, shapeNum);
                break;
            case PolylineZ:
                aLayer = readPolylineZShapes(br, shapeNum);
                break;
            case Polygon:    //Polygon layer                                                               
                aLayer = readPolygonShapes(br, shapeNum);
                break;
            case PolygonM:
                aLayer = readPolygonMShapes(br, shapeNum);
                break;
            case PolygonZ:
                aLayer = readPolygonZShapes(br, shapeNum);
                break;
            default:
                System.out.println("The shape type is not supported: " + aST.toString());
                return null;
        }        
        br.close();

        if (aLayer != null) {
            aLayer.setExtent(aExtent);
            
            //Layer property
            aLayer.setLayerDrawType(LayerDrawType.Map);
            aLayer.setFileName(shpfilepath);
            aLayer.setLayerName(shpFile.getName());
            aLayer.setVisible(true);

            //read out the layer attribute information             
            AttributeTable attrTable = loadDbfFile(shpfilepath, encoding);
            aLayer.setAttributeTable(attrTable);

            //Get projection information
            if (prjFile.exists()) {
                aLayer.setProjInfo(loadProjFile(prjFile));
            }
        }

        return aLayer;

    }

    private static void readHeader(DataInputStream br) throws IOException {
        int i;

        int FileCode = swapByteOrder(br.readInt());
        for (i = 0; i < 5; i++) {
            br.readInt();
        }
        int FileLength = swapByteOrder(br.readInt());
        int Version = br.readInt();
        int aShapeType = br.readInt();
        Extent aExtent = new Extent();
        aExtent.minX = br.readDouble();
        aExtent.minY = br.readDouble();
        aExtent.maxX = br.readDouble();
        aExtent.maxY = br.readDouble();
        for (i = 0; i < 4; i++) {
            br.readDouble();
        }
    }

    private static VectorLayer readPointShapes(DataInputStream br, int shapeNum) throws IOException {
        int RecordNum, ContentLength, aShapeType;
        double x, y;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        byte[] bytes = new byte[28 * shapeNum];
        br.read(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        
        for (int i = 0; i < shapeNum; i++) {

            //br.ReadBytes(12); //记录头8个字节和一个int(4个字节)的shapetype 
            buffer.order(ByteOrder.BIG_ENDIAN);
            RecordNum = buffer.getInt();
            ContentLength = buffer.getInt();
            
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            aShapeType = buffer.getInt();

            x = buffer.getDouble();
            y = buffer.getDouble();

            PointShape aP = new PointShape();
            PointD aPoint = new PointD();
            aPoint.X = x;
            aPoint.Y = y;
            aP.setPoint(aPoint);
            aLayer.addShape(aP);
        }

        //Create legend scheme            
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.black, 5));
        return aLayer;
    }
    
    private static VectorLayer readPointZShapes(DataInputStream br, int shapeNum) throws IOException {
        int RecordNum, ContentLength, aShapeType;
        double x, y, z, m;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.PointZ);
        byte[] bytes = new byte[44 * shapeNum];
        br.read(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        
        for (int i = 0; i < shapeNum; i++) {

            //br.ReadBytes(12); //记录头8个字节和一个int(4个字节)的shapetype 
            buffer.order(ByteOrder.BIG_ENDIAN);
            RecordNum = buffer.getInt();
            ContentLength = buffer.getInt();
            
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            aShapeType = buffer.getInt();

            x = buffer.getDouble();
            y = buffer.getDouble();
            z = buffer.getDouble();
            m = buffer.getDouble();

            PointZShape aP = new PointZShape();
            PointZ aPoint = new PointZ();
            aPoint.X = x;
            aPoint.Y = y;
            aPoint.Z = z;
            aPoint.M = m;
            aP.setPoint(aPoint);
            aLayer.addShape(aP);
        }

        //Create legend scheme            
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.black, 5));
        return aLayer;
    }

    private static VectorLayer readPolylineShapes(DataInputStream br, int shapeNum) throws IOException {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Polyline);
        int RecordNum, ContentLength, aShapeType;
        double x, y;
        byte[] bytes;
        ByteBuffer buffer;
        
        //PointD aPoint;
        for (int i = 0; i < shapeNum; i++) {
            bytes = new byte[8];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            //br.skipBytes(12); 
            buffer.order(ByteOrder.BIG_ENDIAN);
            RecordNum = buffer.getInt();
            ContentLength = buffer.getInt();
            
            bytes = new byte[ContentLength * 2];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            aShapeType = buffer.getInt();

            PolylineShape aPL = new PolylineShape();
            Extent extent = new Extent();
            extent.minX = buffer.getDouble();
            extent.minY = buffer.getDouble();
            extent.maxX = buffer.getDouble();
            extent.maxY = buffer.getDouble();
            aPL.setExtent(extent);

            aPL.setPartNum(buffer.getInt());
            int numPoints = buffer.getInt();
            aPL.parts = new int[aPL.getPartNum()];
            List<PointD> points = new ArrayList<>();

            //firstly read out parts begin pos in file 
            for (int j = 0; j < aPL.getPartNum(); j++) {
                aPL.parts[j] = buffer.getInt();
            }

            //read out coordinates 
            for (int j = 0; j < numPoints; j++) {
                x = buffer.getDouble();
                y = buffer.getDouble();
                PointD aPoint = new PointD();
                aPoint.X = x;
                aPoint.Y = y;
                points.add(aPoint);
            }
            aPL.setPoints(points);
            aLayer.addShape(aPL);
        }

        //Create legend scheme            
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.darkGray, 1.0F));

        return aLayer;
    }

    private static VectorLayer readPolylineZShapes(DataInputStream br, int shapeNum) throws IOException {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.PolylineZ);
        int RecordNum, ContentLength, aShapeType;
        double x, y;
        byte[] bytes;
        ByteBuffer buffer;
        
        //PointD aPoint;
        for (int i = 0; i < shapeNum; i++) {
            //br.skipBytes(12);
            bytes = new byte[8];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            //br.skipBytes(12); 
            buffer.order(ByteOrder.BIG_ENDIAN);
            RecordNum = buffer.getInt();
            ContentLength = buffer.getInt();
            
            bytes = new byte[ContentLength * 2];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            aShapeType = buffer.getInt();

            //Read bounding box
            PolylineZShape aPL = new PolylineZShape();
            Extent extent = new Extent();
            extent.minX = buffer.getDouble();
            extent.minY = buffer.getDouble();
            extent.maxX = buffer.getDouble();
            extent.maxY = buffer.getDouble();
            aPL.setExtent(extent);

            aPL.setPartNum(buffer.getInt());
            int numPoints = buffer.getInt();
            aPL.parts = new int[aPL.getPartNum()];
            List<PointD> points = new ArrayList<>();

            //firstly read out parts begin position in file 
            for (int j = 0; j < aPL.getPartNum(); j++) {
                aPL.parts[j] = buffer.getInt();
            }

            //read out coordinates 
            for (int j = 0; j < numPoints; j++) {
                x = buffer.getDouble();
                y = buffer.getDouble();
                PointD aPoint = new PointD();
                aPoint.X = x;
                aPoint.Y = y;
                points.add(aPoint);
            }
            //aPL.Points = points;

            //Read Z
            double zmin = buffer.getDouble();
            double zmax = buffer.getDouble();
            double[] zArray = new double[numPoints];
            for (int j = 0; j < numPoints; j++) {
                zArray[j] = buffer.getDouble();
            }

            //Read measure
            double mmin = buffer.getDouble();
            double mmax = buffer.getDouble();
            double[] mArray = new double[numPoints];
            for (int j = 0; j < numPoints; j++) {
                mArray[j] = buffer.getDouble();
            }

            //Get pointZ list
            List<PointZ> pointZs = new ArrayList<>();
            for (int j = 0; j < numPoints; j++) {
                pointZs.add(new PointZ(points.get(j).X, points.get(j).Y, zArray[j], mArray[j]));
            }

            aPL.setPoints(pointZs);
            aLayer.addShape(aPL);
        }

        //Create legend scheme            
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.darkGray, 1.0F));

        return aLayer;
    }

    private static VectorLayer readPolygonShapes(DataInputStream br, int shapeNum) throws IOException {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Polygon);
        int RecordNum, ContentLength, aShapeType;
        double x, y;
        byte[] bytes;
        ByteBuffer buffer;

        for (int i = 0; i < shapeNum; i++) {            
            //br.skipBytes(12);
            bytes = new byte[8];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            //br.skipBytes(12); 
            buffer.order(ByteOrder.BIG_ENDIAN);
            RecordNum = buffer.getInt();
            ContentLength = buffer.getInt();
            
            bytes = new byte[ContentLength * 2];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            aShapeType = buffer.getInt();

            PolygonShape aSPG = new PolygonShape();
            Extent extent = new Extent();
            extent.minX = buffer.getDouble();
            extent.minY = buffer.getDouble();
            extent.maxX = buffer.getDouble();
            extent.maxY = buffer.getDouble();
            aSPG.setExtent(extent);
            aSPG.setPartNum(buffer.getInt());
            int numPoints = buffer.getInt();
            aSPG.parts = new int[aSPG.getPartNum()];
            List<PointD> points = new ArrayList<>();

            //firstly read out parts begin pos in file 
            for (int j = 0; j < aSPG.getPartNum(); j++) {
                aSPG.parts[j] = buffer.getInt();
            }

            //read out coordinates 
            for (int j = 0; j < numPoints; j++) {
                x = buffer.getDouble();
                y = buffer.getDouble();
                PointD aPoint = new PointD();
                aPoint.X = x;
                aPoint.Y = y;
                points.add(aPoint);
            }
            aSPG.setPoints(points);
            aLayer.addShape(aSPG);
        }

        //Create legend scheme            
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polygon, new Color(255, 251, 195), 1.0F));

        return aLayer;
    }
    
    private static VectorLayer readPolygonMShapes(DataInputStream br, int shapeNum) throws IOException {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.PolygonM);
        int RecordNum, ContentLength, aShapeType;
        double x, y;
        byte[] bytes;
        ByteBuffer buffer;

        for (int i = 0; i < shapeNum; i++) {            
            //br.skipBytes(12);
            bytes = new byte[8];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            //br.skipBytes(12); 
            buffer.order(ByteOrder.BIG_ENDIAN);
            RecordNum = buffer.getInt();
            ContentLength = buffer.getInt();
            
            bytes = new byte[ContentLength * 2];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            aShapeType = buffer.getInt();

            PolygonMShape aSPG = new PolygonMShape();
            Extent extent = new Extent();
            extent.minX = buffer.getDouble();
            extent.minY = buffer.getDouble();
            extent.maxX = buffer.getDouble();
            extent.maxY = buffer.getDouble();
            aSPG.setExtent(extent);
            aSPG.setPartNum(buffer.getInt());
            int numPoints = buffer.getInt();
            aSPG.parts = new int[aSPG.getPartNum()];
            List<PointD> points = new ArrayList<>();

            //firstly read out parts begin pos in file 
            for (int j = 0; j < aSPG.getPartNum(); j++) {
                aSPG.parts[j] = buffer.getInt();
            }

            //read out coordinates 
            for (int j = 0; j < numPoints; j++) {
                x = buffer.getDouble();
                y = buffer.getDouble();
                PointD aPoint = new PointD();
                aPoint.X = x;
                aPoint.Y = y;
                points.add(aPoint);
            }
            
            //Read measure
            double mmin = buffer.getDouble();
            double mmax = buffer.getDouble();
            double[] mArray = new double[numPoints];
            for (int j = 0; j < numPoints; j++) {
                mArray[j] = buffer.getDouble();
            }
            
            //Get pointM list
            List<PointM> pointMs = new ArrayList<>();
            for (int j = 0; j < numPoints; j++) {
                pointMs.add(new PointM(points.get(j).X, points.get(j).Y, mArray[j]));
            }
            
            aSPG.setPoints(pointMs);
            aLayer.addShape(aSPG);
        }

        //Create legend scheme            
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polygon, new Color(255, 251, 195), 1.0F));

        return aLayer;
    }
    
    private static VectorLayer readPolygonZShapes(DataInputStream br, int shapeNum) throws IOException {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.PolygonZ);
        int RecordNum, ContentLength, aShapeType;
        double x, y;
        byte[] bytes;
        ByteBuffer buffer;

        for (int i = 0; i < shapeNum; i++) {            
            //br.skipBytes(12);
            bytes = new byte[8];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            //br.skipBytes(12); 
            buffer.order(ByteOrder.BIG_ENDIAN);
            RecordNum = buffer.getInt();
            ContentLength = buffer.getInt();
            
            bytes = new byte[ContentLength * 2];
            br.read(bytes);
            buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            aShapeType = buffer.getInt();

            PolygonZShape aSPG = new PolygonZShape();
            Extent extent = new Extent();
            extent.minX = buffer.getDouble();
            extent.minY = buffer.getDouble();
            extent.maxX = buffer.getDouble();
            extent.maxY = buffer.getDouble();
            aSPG.setExtent(extent);
            aSPG.setPartNum(buffer.getInt());
            int numPoints = buffer.getInt();
            aSPG.parts = new int[aSPG.getPartNum()];
            List<PointD> points = new ArrayList<>();

            //firstly read out parts begin pos in file 
            for (int j = 0; j < aSPG.getPartNum(); j++) {
                aSPG.parts[j] = buffer.getInt();
            }

            //read out coordinates 
            for (int j = 0; j < numPoints; j++) {
                x = buffer.getDouble();
                y = buffer.getDouble();
                PointD aPoint = new PointD();
                aPoint.X = x;
                aPoint.Y = y;
                points.add(aPoint);
            }
            
            //Read Z
            double zmin = buffer.getDouble();
            double zmax = buffer.getDouble();
            double[] zArray = new double[numPoints];
            for (int j = 0; j < numPoints; j++) {
                zArray[j] = buffer.getDouble();
            }
            
            //Read measure
            double mmin = buffer.getDouble();
            double mmax = buffer.getDouble();
            double[] mArray = new double[numPoints];
            for (int j = 0; j < numPoints; j++) {
                mArray[j] = buffer.getDouble();
            }
            
            //Get pointZ list
            List<PointZ> pointZs = new ArrayList<>();
            for (int j = 0; j < numPoints; j++) {
                pointZs.add(new PointZ(points.get(j).X, points.get(j).Y, zArray[j], mArray[j]));
            }
            
            aSPG.setPoints(pointZs);
            aLayer.addShape(aSPG);
        }

        //Create legend scheme            
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polygon, new Color(255, 251, 195), 1.0F));

        return aLayer;
    }

    private static void loadShxFile(File shxFile) throws FileNotFoundException, IOException {
        DataInputStream bridx = new DataInputStream(new BufferedInputStream(new FileInputStream(shxFile)));
        long BytesSum = shxFile.length();  //Get file byte length   
        int shapeNum = (int) (BytesSum - 100) / 8;  //Get total number of records   
        readHeader(bridx);

        int OffSet = 0, ContentLength = 0;
        for (int i = 0; i < shapeNum; i++) {
            OffSet = swapByteOrder(bridx.readInt());
            ContentLength = swapByteOrder(bridx.readInt());
        }

        bridx.close();
    }
    
//    /**
//     * Load DBF data file
//     * @param shpFileName Shape file name
//     * @return Attribute table
//     * @throws Exception 
//     */
//    public static AttributeTable loadDbfFile(String shpFileName) throws Exception{
//        AttributeTable attrTable = new AttributeTable();
//        attrTable.open(shpFileName);
//        attrTable.fill(attrTable.getNumRecords());
//        
//        return attrTable;
//    }
    
    /**
     * Load DBF data file
     * @param shpFileName Shape file name
     * @param encoding Encoding
     * @return Attribute table
     * @throws Exception 
     */
    public static AttributeTable loadDbfFile(String shpFileName, String encoding) throws Exception{
        AttributeTable attrTable = new AttributeTable();
        attrTable.setEncoding(encoding);
        attrTable.open(shpFileName);
        attrTable.fill(attrTable.getNumRecords());
        
        return attrTable;
    }
    
    /**
     * Load projection file
     * @param projFile Projection file
     * @return Projection infomation
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static ProjectionInfo loadProjFile(File projFile) throws FileNotFoundException, IOException {        
        BufferedReader sr = new BufferedReader(new FileReader(projFile));
        String line;
        StringBuilder buffer = new StringBuilder();
        while ((line = sr.readLine()) != null) {
            buffer.append(line);
        }

        String esriString = buffer.toString();
        sr.close();

        CRSFactory crsFactory = new CRSFactory();        
        CoordinateReferenceSystem crs = crsFactory.createFromEsriString(esriString);
        ProjectionInfo projInfo = ProjectionInfo.factory(crs);
        
        return projInfo;
    }
      
    /**
     * Save shape file
     * @param shpfilepath Shape file path
     * @param aLayer Vector layer
     * @return Boolean
     * @throws java.io.IOException*/
    public static boolean saveShapeFile(String shpfilepath, VectorLayer aLayer) throws IOException {
        String shxfilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".shx");
        String dbffilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".dbf");
        String projFilePath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".prj");        

        switch (aLayer.getShapeType()) {
            case Point:
            case PointZ:
            case Polyline:
            case PolylineZ:
            case Polygon:
                writeShxFile(shxfilepath, aLayer);
                writeShpFile(shpfilepath, aLayer);
                writeDbfFile(dbffilepath, aLayer);
                writeProjFile(projFilePath, aLayer);
                return true;

            default:
                return false;
        }
    }
    
    /**
     * Save shape file
     * @param shpfilepath Shape file path
     * @param aLayer Vector layer
     * @param encoding Encoding
     * @return Boolean
     * @throws java.io.IOException*/
    public static boolean saveShapeFile(String shpfilepath, VectorLayer aLayer, String encoding) throws IOException {
        String shxfilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".shx");
        String dbffilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".dbf");
        String projFilePath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".prj");        

        switch (aLayer.getShapeType()) {
            case Point:
            case PointZ:
            case Polyline:
            case PolylineZ:
            case Polygon:
                writeShxFile(shxfilepath, aLayer);
                writeShpFile(shpfilepath, aLayer);
                writeDbfFile(dbffilepath, aLayer, encoding);
                writeProjFile(projFilePath, aLayer);
                return true;

            default:
                return false;
        }
    }

    private static void writeShpFile(String shpfilepath, VectorLayer aLayer) throws FileNotFoundException, IOException {
        File shpFile = new File(shpfilepath);
        EndianDataOutputStream bw = new EndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(shpFile)));

        //Write header
        int FileLength = getShpFileLength(aLayer);
        writeHeader(bw, aLayer, FileLength);

        //Write records
        int RecordNumber;

        for (int i = 0; i < aLayer.getShapeNum(); i++) {
            Shape aShape = aLayer.getShapes().get(i);
            RecordNumber = i + 1;
            writeRecord(bw, RecordNumber, aShape, aLayer.getShapeType());
        }

        //Close
        bw.close();
    }

    private static int getShpFileLength(VectorLayer aLayer) {
        int fileLength = 50;

        for (int i = 0; i < aLayer.getShapeNum(); i++) {
            Shape aShape = aLayer.getShapes().get(i);
            int cLen = getContentLength(aShape, aLayer.getShapeType());
            fileLength += 4 + cLen;
        }

        return fileLength;
    }

    private static int getContentLength(Shape aShape, ShapeTypes aST) {
        int contentLength = 0;
        switch (aST) {
            case Point:
                contentLength = 2 + 4 * 2;
                break;
            case Polyline:
                PolylineShape aPLS = (PolylineShape) aShape;
                contentLength = 2 + 4 * 4 + 2 + 2 + 2 * aPLS.getPartNum() + 4 * 2 * aPLS.getPointNum();
                break;
            case PolylineZ:
                PolylineZShape aPLZS = (PolylineZShape) aShape;
                contentLength = 2 + 4 * 4 + 2 + 2 + 2 * aPLZS.getPartNum() + 4 * 2 * aPLZS.getPointNum()
                        + 4 + 4 + 4 * aPLZS.getPointNum() + 4 + 4 + 4 * aPLZS.getPointNum();
                break;
            case Polygon:
                PolygonShape aPGS = (PolygonShape) aShape;
                contentLength = 2 + 4 * 4 + 2 + 2 + 2 * aPGS.getPartNum() + 4 * 2 * aPGS.getPointNum();
                break;
        }

        return contentLength;
    }

    private static void writeRecord(EndianDataOutputStream bw, int RecordNumber, Shape aShape, ShapeTypes aST) throws IOException {
        int ContentLength, i;

        ContentLength = getContentLength(aShape, aST);
        bw.writeIntBE(RecordNumber);
        bw.writeIntBE(ContentLength);
        bw.writeIntLE(aST.getValue());
        switch (aST) {
            case Point:
                PointShape aPS = (PointShape) aShape;
                bw.writeDoubleLE(aPS.getPoint().X);
                bw.writeDoubleLE(aPS.getPoint().Y);
                break;
            case Polyline:
                PolylineShape aPLS = (PolylineShape) aShape;
                bw.writeDoubleLE(aPLS.getExtent().minX);
                bw.writeDoubleLE(aPLS.getExtent().minY);
                bw.writeDoubleLE(aPLS.getExtent().maxX);
                bw.writeDoubleLE(aPLS.getExtent().maxY);
                bw.writeIntLE(aPLS.getPartNum());
                bw.writeIntLE(aPLS.getPointNum());
                for (i = 0; i < aPLS.getPartNum(); i++) {
                    bw.writeIntLE(aPLS.parts[i]);
                }
                for (i = 0; i < aPLS.getPointNum(); i++) {
                    bw.writeDoubleLE((aPLS.getPoints().get(i)).X);
                    bw.writeDoubleLE((aPLS.getPoints().get(i)).Y);
                }
                break;
            case PolylineZ:
                PolylineZShape aPLZS = (PolylineZShape) aShape;
                bw.writeDoubleLE(aPLZS.getExtent().minX);
                bw.writeDoubleLE(aPLZS.getExtent().minY);
                bw.writeDoubleLE(aPLZS.getExtent().maxX);
                bw.writeDoubleLE(aPLZS.getExtent().maxY);
                bw.writeIntLE(aPLZS.getPartNum());
                bw.writeIntLE(aPLZS.getPointNum());
                for (i = 0; i < aPLZS.getPartNum(); i++) {
                    bw.writeIntLE(aPLZS.parts[i]);
                }
                for (i = 0; i < aPLZS.getPointNum(); i++) {
                    bw.writeDoubleLE((aPLZS.getPoints().get(i)).X);
                    bw.writeDoubleLE((aPLZS.getPoints().get(i)).Y);
                }
                bw.writeDoubleLE(aPLZS.getZRange()[0]);
                bw.writeDoubleLE(aPLZS.getZRange()[1]);
                for (i = 0; i < aPLZS.getPointNum(); i++) {
                    bw.writeDoubleLE(aPLZS.getZArray()[i]);
                }
                bw.writeDoubleLE(aPLZS.getMRange()[0]);
                bw.writeDoubleLE(aPLZS.getMRange()[1]);
                for (i = 0; i < aPLZS.getPointNum(); i++) {
                    bw.writeDoubleLE(aPLZS.getMArray()[i]);
                }

                break;
            case Polygon:
                PolygonShape aPGS = (PolygonShape) aShape;
                bw.writeDoubleLE(aPGS.getExtent().minX);
                bw.writeDoubleLE(aPGS.getExtent().minY);
                bw.writeDoubleLE(aPGS.getExtent().maxX);
                bw.writeDoubleLE(aPGS.getExtent().maxY);
                bw.writeIntLE(aPGS.getPartNum());
                bw.writeIntLE(aPGS.getPointNum());
                for (i = 0; i < aPGS.getPartNum(); i++) {
                    bw.writeIntLE(aPGS.parts[i]);
                }
                for (i = 0; i < aPGS.getPointNum(); i++) {
                    bw.writeDoubleLE((aPGS.getPoints().get(i)).X);
                    bw.writeDoubleLE((aPGS.getPoints().get(i)).Y);
                }
                break;
        }
    }

    private static void writeHeader(EndianDataOutputStream bw, VectorLayer aLayer, int FileLength) throws IOException {
        int i;
        int FileCode = 9994;
        //FileCode = swapByteOrder(FileCode);
        int Unused = 0;
        //Unused = swapByteOrder(Unused);
        //FileLength = swapByteOrder(FileLength);
        int Version = 1000;
        int aShapeType = aLayer.getShapeType().getValue();

        bw.writeIntBE(FileCode);
        for (i = 0; i < 5; i++) {
            bw.writeIntBE(Unused);
        }
        bw.writeIntBE(FileLength);
        bw.writeIntLE(Version);
        bw.writeIntLE(aShapeType);
        bw.writeDoubleLE(aLayer.getExtent().minX);
        bw.writeDoubleLE(aLayer.getExtent().minY);
        bw.writeDoubleLE(aLayer.getExtent().maxX);
        bw.writeDoubleLE(aLayer.getExtent().maxY);
        for (i = 0; i < 4; i++) {
            bw.writeDoubleLE(0.0);
        }
    }

    private static void writeShxFile(String shxfilepath, VectorLayer aLayer) throws IOException {
        File shxFile = new File(shxfilepath);
        EndianDataOutputStream bw = new EndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(shxFile)));

        //Write header
        int FileLength = aLayer.getShapeNum() * 4 + 50;
        writeHeader(bw, aLayer, FileLength);

        //Write content
        int OffSet, ContentLength;
        OffSet = 50;

        for (int i = 0; i < aLayer.getShapeNum(); i++) {
            Shape aShape = aLayer.getShapes().get(i);
            ContentLength = getContentLength(aShape, aLayer.getShapeType());

            bw.writeIntBE(OffSet);
            bw.writeIntBE(ContentLength);

            OffSet = OffSet + 4 + ContentLength;
        }

        //Close
        bw.close();
    }

    private static void writeDbfFile(String dbffilepath, VectorLayer aLayer) {
        aLayer.getAttributeTable().saveAs(dbffilepath, true);
    }
    
    private static void writeDbfFile(String dbffilepath, VectorLayer aLayer, String encoding) {
        AttributeTable attTable = aLayer.getAttributeTable();
        attTable.setEncoding(encoding);
        attTable.saveAs(dbffilepath, true);
    }

    private static void writeProjFile(String projFilePath, VectorLayer aLayer) {
        BufferedWriter sw = null;
        try {
            String esriString = aLayer.getProjInfo().toEsriString();
            sw = new BufferedWriter(new FileWriter(new File(projFilePath)));
            sw.write(esriString);
            sw.flush();
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(ShapeFileManage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sw.close();
            } catch (IOException ex) {
                Logger.getLogger(ShapeFileManage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Swaps the byte order of an int32
     *
     * @param i Integer
     * @return Byte order swapped int
     */
    private static int swapByteOrder(int i) {
        byte[] buffer = intToBytes(i);
        return ((buffer[3] & 0xff) << 24) | ((buffer[2] & 0xff) << 16)
                | ((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff);
    }

    private static byte[] intToBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }
    
    private static int bytesToInt(byte[] buffer){
        return ((buffer[3] & 0xff) << 24) | ((buffer[2] & 0xff) << 16)
                | ((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff);
    }
}
