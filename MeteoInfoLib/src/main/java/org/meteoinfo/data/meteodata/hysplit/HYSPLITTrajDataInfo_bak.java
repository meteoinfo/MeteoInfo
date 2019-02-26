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
package org.meteoinfo.data.meteodata.hysplit;

import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.TrajDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.data.DataTypes;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.XYListDataset;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.legend.PointStyle;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.DateUtil;
import org.meteoinfo.layer.LayerDrawType;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PointZShape;
import org.meteoinfo.shape.PolylineZShape;
import org.meteoinfo.shape.ShapeTypes;
import ucar.ma2.Array;
import ucar.nc2.Attribute;

/**
 *
 * @author yaqiang
 */
public class HYSPLITTrajDataInfo_bak extends DataInfo implements TrajDataInfo {
    // <editor-fold desc="Variables">
/// <summary>
    /// File name
    /// </summary>

    public List<String> fileNames;
    /// <summary>
    /// Number of meteorological files
    /// </summary>
    public List<Integer> meteoFileNums;
    /// <summary>
    /// Number of trajectories
    /// </summary>
    public int trajeoryNumber;
    /// <summary>
    /// Number of trajectories
    /// </summary>
    public List<Integer> trajeoryNums;
    /// <summary>
    /// Trajectory direction - foreward or backward
    /// </summary>
    public List<String> trajDirections;
    /// <summary>
    /// Vertical motion
    /// </summary>
    public List<String> verticalMotions;
    /// <summary>
    /// Information list of trajectories
    /// </summary>
    public List<List<TrajectoryInfo>> trajInfos;
    /// <summary>
    /// Number of variables
    /// </summary>
    public List<Integer> varNums;
    /// <summary>
    /// Variable name list
    /// </summary>
    public List<List<String>> varNames;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public HYSPLITTrajDataInfo_bak() {
        this.setDataType(MeteoDataType.HYSPLIT_Traj);
        initVariables();
    }

    private void initVariables() {
        fileNames = new ArrayList<>();
        meteoFileNums = new ArrayList<>();
        trajeoryNums = new ArrayList<>();
        trajDirections = new ArrayList<>();
        verticalMotions = new ArrayList<>();
        trajInfos = new ArrayList<>();
        varNums = new ArrayList<>();
        varNames = new ArrayList<>();
        trajeoryNumber = 0;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        String[] trajFiles = new String[1];
        trajFiles[0] = fileName;
        try {
            readDataInfo(trajFiles);
        } catch (IOException ex) {
            Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Read data info for multi trajectory files
     *
     * @param trajFiles Trajectory files
     * @throws IOException
     */
    public void readDataInfo(String[] trajFiles) throws IOException {
        this.setFileName(trajFiles[0]);
        String aLine;
        String[] dataArray;
        int i, t;

        initVariables();
        List<Double> times = new ArrayList<>();

        for (t = 0; t < trajFiles.length; t++) {
            String aFile = trajFiles[t];
            fileNames.add(aFile);

            BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));

            //Record #1
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            meteoFileNums.add(Integer.parseInt(dataArray[0]));

            //Record #2
            for (i = 0; i < meteoFileNums.get(t); i++) {
                sr.readLine();
            }

            //Record #3
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            trajeoryNums.add(Integer.parseInt(dataArray[0]));
            trajeoryNumber += trajeoryNums.get(t);
            trajDirections.add(dataArray[1]);
            verticalMotions.add(dataArray[2]);

            //Record #4  
            TrajectoryInfo aTrajInfo;
            List<TrajectoryInfo> trajInfoList = new ArrayList<>();
            for (i = 0; i < trajeoryNums.get(t); i++) {
                aLine = sr.readLine().trim();
                dataArray = aLine.split("\\s+");
                int y = Integer.parseInt(dataArray[0]);
                if (y < 100) {
                    if (y > 50) {
                        y = 1900 + y;
                    } else {
                        y = 2000 + y;
                    }
                }
                Calendar cal = new GregorianCalendar(y, Integer.parseInt(dataArray[1]) - 1,
                        Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);

                if (times.isEmpty()) {
                    times.add(DateUtil.toOADate(cal.getTime()));
                }
                aTrajInfo = new TrajectoryInfo();
                aTrajInfo.startTime = cal.getTime();
                aTrajInfo.startLat = Float.parseFloat(dataArray[4]);
                aTrajInfo.startLon = Float.parseFloat(dataArray[5]);
                aTrajInfo.startHeight = Float.parseFloat(dataArray[6]);
                trajInfoList.add(aTrajInfo);
            }
            trajInfos.add(trajInfoList);
            Dimension tdim = new Dimension(DimensionType.T);
            tdim.setValues(times);

            //Record #5
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            int nvar = Integer.parseInt(dataArray[0]);
            if (nvar > dataArray.length - 1){
                nvar = dataArray.length - 1;
            }
            varNums.add(nvar);
            List<String> varNameList = new ArrayList<>();
            for (i = 0; i < varNums.get(t); i++) {
                varNameList.add(dataArray[i + 1]);
            }
            varNames.add(varNameList);

            Variable var = new Variable();
            var.setName("Traj");
            var.setStation(true);
            var.setDimension(tdim);
            List<Variable> variables = new ArrayList<>();
            variables.add(var);
            this.setVariables(variables);

            sr.close();
        }
    }
    
    /**
     * Get global attributes
     * @return Global attributes
     */
    @Override
    public List<Attribute> getGlobalAttributes(){
        return new ArrayList<>();
    }

    @Override
    public String generateInfoText() {
        String dataInfo = "";
        for (int t = 0; t < fileNames.size(); t++) {
            int i;
            dataInfo += "File Name: " + fileNames.get(t);
            dataInfo += System.getProperty("line.separator") + "Trajectory number = " + String.valueOf(trajeoryNums.get(t));
            dataInfo += System.getProperty("line.separator") + "Trajectory direction = " + trajDirections.get(t);
            dataInfo += System.getProperty("line.separator") + "Vertical motion =" + verticalMotions.get(t);
            dataInfo += System.getProperty("line.separator") + "Number of diagnostic output variables = "
                    + String.valueOf(varNums.get(t));
            dataInfo += System.getProperty("line.separator") + "Variables:";
            for (i = 0; i < varNums.get(t); i++) {
                dataInfo += " " + varNames.get(t).get(i);
            }
            dataInfo += System.getProperty("line.separator") + System.getProperty("line.separator") + "Trajectories:";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00");
            for (TrajectoryInfo aTrajInfo : trajInfos.get(t)) {
                dataInfo += System.getProperty("line.separator") + "  " + format.format(aTrajInfo.startTime)
                        + "  " + String.valueOf(aTrajInfo.startLat) + "  " + String.valueOf(aTrajInfo.startLon)
                        + "  " + String.valueOf(aTrajInfo.startHeight);
            }

            if (t < fileNames.size() - 1) {
                dataInfo += System.getProperty("line.separator") + System.getProperty("line.separator")
                        + "******************************" + System.getProperty("line.separator");
            }
        }

        return dataInfo;
    }
    
    /**
     * Read array data of a variable
     * 
     * @param varName Variable name
     * @return Array data
     */
    @Override
    public Array read(String varName){
        return null;
    }
    
    /**
     * Read array data of the variable
     *
     * @param varName Variable name
     * @param origin The origin array
     * @param size The size array
     * @param stride The stride array
     * @return Array data
     */
    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        return null;
    }

    @Override
    public VectorLayer createTrajLineLayer() {
        return createTrajLineLayer(false);
    }
    
    public VectorLayer createTrajLineLayer(boolean zPres) {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.PolylineZ);
        aLayer.editAddField("ID", DataTypes.Integer);
        aLayer.editAddField("Date", DataTypes.Date);
        aLayer.editAddField("Year", DataTypes.Integer);
        aLayer.editAddField("Month", DataTypes.Integer);
        aLayer.editAddField("Day", DataTypes.Integer);
        aLayer.editAddField("Hour", DataTypes.Integer);
        aLayer.editAddField("Height", DataTypes.Float);

        Calendar cal = Calendar.getInstance();
        int TrajNum = 0;
        for (int t = 0; t < fileNames.size(); t++) {
            try {
                String aFile = fileNames.get(t);
                BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                int i;

                //Record #1
                sr.readLine();

                //Record #2
                for (i = 0; i < meteoFileNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #3
                sr.readLine();

                //Record #4             
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #5
                sr.readLine();

                //Record #6
                int TrajIdx;
                List<PointZ> pList;
                List<List<PointZ>> PointList = new ArrayList<>();
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    pList = new ArrayList<>();
                    PointList.add(pList);
                }
                PointZ aPoint;
                //ArrayList polylines = new ArrayList();
                int dn = 12 + this.varNums.get(t);
                while (true) {
                    aLine = sr.readLine();
                    if (aLine == null) {
                        break;
                    }
                    if (aLine.isEmpty()) {
                        continue;
                    }
                    aLine = aLine.trim();
                    dataArray = aLine.split("\\s+");
                    if (dataArray.length < dn){
                        sr.readLine();
                        //aLine = sr.readLine().trim();
                        //tempArray = aLine.split("\\s+");
                    }
                    TrajIdx = Integer.parseInt(dataArray[0]) - 1;
                    aPoint = new PointZ();
                    aPoint.X = Double.parseDouble(dataArray[10]);
                    aPoint.Y = Double.parseDouble(dataArray[9]);
                    if (dataArray.length >= 13){
                        if (zPres){
                            aPoint.M = Double.parseDouble(dataArray[11]);
                            aPoint.Z = Double.parseDouble(dataArray[12]);
                        } else {
                            aPoint.M = Double.parseDouble(dataArray[12]);
                            aPoint.Z = Double.parseDouble(dataArray[11]);
                        }
                    } else {
                        aPoint.Z = Double.parseDouble(dataArray[11]);
                    }

                    if (PointList.get(TrajIdx).size() > 1) {
                        PointZ oldPoint = PointList.get(TrajIdx).get(PointList.get(TrajIdx).size() - 1);
                        if (Math.abs(aPoint.X - oldPoint.X) > 100) {
                            if (aPoint.X > oldPoint.X) {
                                aPoint.X -= 360;
                            } else {
                                aPoint.X += 360;
                            }
                        }
                    }
                    PointList.get(TrajIdx).add(aPoint);
                }

                //SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    PolylineZShape aPolyline = new PolylineZShape();
                    TrajNum += 1;
                    aPolyline.setValue(TrajNum);
                    aPolyline.setPoints(PointList.get(i));
                    aPolyline.setExtent(MIMath.getPointsExtent(aPolyline.getPoints()));

                    int shapeNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                        cal.setTime(trajInfos.get(t).get(i).startTime);
                        aLayer.editCellValue("ID", shapeNum, TrajNum);
                        aLayer.editCellValue("Date", shapeNum, cal.getTime());
                        aLayer.editCellValue("Year", shapeNum, cal.get(Calendar.YEAR));
                        aLayer.editCellValue("Month", shapeNum, cal.get(Calendar.MONTH) + 1);
                        aLayer.editCellValue("Day", shapeNum, cal.get(Calendar.DAY_OF_MONTH));
                        aLayer.editCellValue("Hour", shapeNum, cal.get(Calendar.HOUR_OF_DAY));
                        aLayer.editCellValue("Height", shapeNum, trajInfos.get(t).get(i).startHeight);
                        //aLayer.editCellValue("StartLat", shapeNum, trajInfos.get(t).get(i).startLat);
                        //aLayer.editCellValue("StartLon", shapeNum, trajInfos.get(t).get(i).startLon);
                    }
                }

                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName("Trajectory_Lines");
        aLayer.setLayerDrawType(LayerDrawType.TrajLine);
        //aLayer.LegendScheme = m_Legend.CreateSingleSymbolLegendScheme(Shape.ShapeType.Polyline, Color.Blue, 1.0F, 1, aDataInfo.TrajeoryNum);            
        aLayer.setVisible(true);
        //LegendScheme aLS = LegendManage.createUniqValueLegendScheme(aLayer, 1, trajeoryNumber);
        aLayer.updateLegendScheme(LegendType.UniqueValue, "ID");
        LegendScheme ls = aLayer.getLegendScheme();
        int i = 0;
        for (ColorBreak cb : ls.getLegendBreaks()) {
            PolylineBreak plb = (PolylineBreak) cb;
            plb.setDrawSymbol(true);
            plb.setSymbolFillColor(plb.getSymbolColor());
            plb.setSymbolInterval(6);
            plb.setWidth(2);
            if (i == PointStyle.values().length) {
                i = 0;
            }
            plb.setSymbolStyle(PointStyle.values()[i]);
            i += 1;
        }
        //aLS.setFieldName("TrajID");
        //aLayer.setLegendScheme(aLS);

        return aLayer;
    }

    @Override
    public VectorLayer createTrajPointLayer() {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        aLayer.editAddField(new Field("TrajID", DataTypes.Integer));
        aLayer.editAddField(new Field("Date", DataTypes.String));
        aLayer.editAddField(new Field("Lon", DataTypes.Double));
        aLayer.editAddField(new Field("Lat", DataTypes.Double));
        aLayer.editAddField(new Field("Altitude", DataTypes.Double));
        aLayer.editAddField(new Field("Pressure", DataTypes.Double));
        boolean isMultiVar = false;
        if (varNums.get(0) > 1) {
            isMultiVar = true;
            for (int v = 1; v < varNums.get(0); v++) {
                aLayer.editAddField(new Field(varNames.get(0).get(v), DataTypes.Double));
            }
        }

        int TrajNum = 0;
        for (int t = 0; t < fileNames.size(); t++) {
            try {
                String aFile = fileNames.get(t);
                BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray, tempArray;
                int i;

                //Record #1
                sr.readLine();

                //Record #2
                for (i = 0; i < meteoFileNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #3
                sr.readLine();

                //Record #4             
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #5
                sr.readLine();

                //Record #6
                int TrajIdx;
                List<List<Object>> pList;
                List<List<List<Object>>> PointList = new ArrayList<>();
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    pList = new ArrayList<>();
                    PointList.add(pList);
                }
                PointZ aPoint;
                double Height, Press;
                int dn = 12 + this.varNums.get(t);
                while (true) {
                    aLine = sr.readLine();
                    if (aLine == null) {
                        break;
                    }
                    if (aLine.isEmpty()) {
                        continue;
                    }
                    aLine = aLine.trim();
                    dataArray = aLine.split("\\s+");
                    if (dataArray.length < dn){
                        aLine = sr.readLine().trim();
                        tempArray = aLine.split("\\s+");
                        dataArray = (String[])DataConvert.resizeArray(dataArray, dn);
                        for (i = 0; i < tempArray.length; i++){
                            dataArray[dn - tempArray.length + i] = tempArray[i];
                        }
                    }
                    List<Object> dList = new ArrayList<>();
                    TrajIdx = Integer.parseInt(dataArray[0]) - 1;
                    int y = Integer.parseInt(dataArray[2]);
                    if (y < 100) {
                        if (y > 50) {
                            y = 1900 + y;
                        } else {
                            y = 2000 + y;
                        }
                    }
                    Calendar cal = new GregorianCalendar(y, Integer.parseInt(dataArray[3]) - 1,
                            Integer.parseInt(dataArray[4]), Integer.parseInt(dataArray[5]), 0, 0);
                    aPoint = new PointZ();
                    aPoint.X = Double.parseDouble(dataArray[10]);
                    aPoint.Y = Double.parseDouble(dataArray[9]);
                    Height = Double.parseDouble(dataArray[11]);
                    Press = Double.parseDouble(dataArray[12]);
                    aPoint.Z = Height;
                    aPoint.M = Press;
                    dList.add(aPoint);
                    dList.add(cal.getTime());
                    dList.add(Height);
                    dList.add(Press);
                    if (isMultiVar) {
                        for (i = 13; i < dataArray.length; i++) {
                            dList.add(Double.parseDouble(dataArray[i]));
                        }
                    }
                    PointList.get(TrajIdx).add(dList);
                }

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    TrajNum += 1;
                    for (int j = 0; j < PointList.get(i).size(); j++) {
                        PointZShape aPS = new PointZShape();
                        aPS.setValue(TrajNum);
                        aPS.setPoint((PointD) PointList.get(i).get(j).get(0));
                        int shapeNum = aLayer.getShapeNum();
                        if (aLayer.editInsertShape(aPS, shapeNum)) {
                            aLayer.editCellValue("TrajID", shapeNum, TrajNum);
                            aLayer.editCellValue("Date", shapeNum, format.format((Date) PointList.get(i).get(j).get(1)));
                            aLayer.editCellValue("Lat", shapeNum, aPS.getPoint().Y);
                            aLayer.editCellValue("Lon", shapeNum, aPS.getPoint().X);
                            aLayer.editCellValue("Altitude", shapeNum, PointList.get(i).get(j).get(2));
                            aLayer.editCellValue("Pressure", shapeNum, PointList.get(i).get(j).get(3));
                            if (isMultiVar) {
                                for (int v = 1; v < varNums.get(0); v++) {
                                    aLayer.editCellValue(varNames.get(0).get(v), shapeNum, PointList.get(i).get(j).get(3 + v));
                                }
                            }
                        }
                    }
                }

                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName("Trajectory_Points");
        aLayer.setLayerDrawType(LayerDrawType.TrajLine);
        //aLayer.LegendScheme = m_Legend.CreateSingleSymbolLegendScheme(Shape.ShapeType.Polyline, Color.Blue, 1.0F, 1, aDataInfo.TrajeoryNum);            
        aLayer.setVisible(true);
        LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.red, 5.0F);
        aLS.setFieldName("TrajID");
        aLayer.setLegendScheme(aLS);

        return aLayer;
    }

    @Override
    public VectorLayer createTrajStartPointLayer() {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.PointZ);
        aLayer.editAddField(new Field("TrajID", DataTypes.Integer));
        aLayer.editAddField(new Field("StartDate", DataTypes.String));
        aLayer.editAddField(new Field("StartLon", DataTypes.Double));
        aLayer.editAddField(new Field("StartLat", DataTypes.Double));
        aLayer.editAddField(new Field("StartHeight", DataTypes.Double));

        int TrajNum = 0;
        for (int t = 0; t < fileNames.size(); t++) {
            try {
                String aFile = fileNames.get(t);
                BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                int i;

                //Record #1
                sr.readLine();

                //Record #2
                for (i = 0; i < meteoFileNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #3
                sr.readLine();

                //Record #4             
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #5
                sr.readLine();

                //Record #6
                int TrajIdx;
                List<PointZ> PointList = new ArrayList<>();
                PointZ aPoint = new PointZ();
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    PointList.add(aPoint);
                }

                int dn = 12 + this.varNums.get(t);
                //ArrayList polylines = new ArrayList();
                while (true) {
                    aLine = sr.readLine();
                    if (aLine == null) {
                        break;
                    }
                    if (aLine.isEmpty()) {
                        continue;
                    }
                    aLine = aLine.trim();
                    dataArray = aLine.split("\\s+");
                    if (dataArray.length < dn){
                        sr.readLine();
                    }
                    if (Float.parseFloat(dataArray[8]) == 0) {
                        TrajIdx = Integer.parseInt(dataArray[0]) - 1;
                        aPoint = new PointZ();
                        aPoint.X = Double.parseDouble(dataArray[10]);
                        aPoint.Y = Double.parseDouble(dataArray[9]);
                        aPoint.Z = Double.parseDouble(dataArray[11]);
                        aPoint.M = Double.parseDouble(dataArray[12]);
                        PointList.set(TrajIdx, aPoint);
                    }
                }

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    PointZShape aPS = new PointZShape();
                    TrajNum += 1;
                    aPS.setValue(TrajNum);
                    aPS.setPoint(PointList.get(i));

                    int shapeNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPS, shapeNum)) {
                        aLayer.editCellValue("TrajID", shapeNum, TrajNum);
                        aLayer.editCellValue("StartDate", shapeNum, format.format(trajInfos.get(t).get(i).startTime));
                        aLayer.editCellValue("StartLat", shapeNum, trajInfos.get(t).get(i).startLat);
                        aLayer.editCellValue("StartLon", shapeNum, trajInfos.get(t).get(i).startLon);
                        aLayer.editCellValue("StartHeight", shapeNum, trajInfos.get(t).get(i).startHeight);
                    }
                }

                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName("Trajectory_Start_Points");
        aLayer.setLayerDrawType(LayerDrawType.TrajPoint);
        aLayer.setVisible(true);
        LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.black, 8.0F);
        aLS.setFieldName("TrajID");
        aLayer.setLegendScheme(aLS);

        return aLayer;
    }

    /**
     * Get XYDataset
     *
     * @param varIndex Variable index
     * @return XYDataset
     */
    public XYListDataset getXYDataset(int varIndex) {
        XYListDataset dataset = new XYListDataset();
        Calendar cal = Calendar.getInstance();
        int trajNum = 1;
        for (int t = 0; t < fileNames.size(); t++) {
            try {
                String aFile = fileNames.get(t);
                BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray, tempArray;
                int i;

                //Record #1
                sr.readLine();

                //Record #2
                for (i = 0; i < meteoFileNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #3
                sr.readLine();

                //Record #4             
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #5
                sr.readLine();

                //Record #6
                int TrajIdx;
                List<PointD> pList;
                List<List<PointD>> PointList = new ArrayList<>();
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    pList = new ArrayList<>();
                    PointList.add(pList);
                }
                PointD aPoint;
                //ArrayList polylines = new ArrayList();
                int dn = 12 + this.varNums.get(t);
                while (true) {
                    aLine = sr.readLine();
                    if (aLine == null) {
                        break;
                    }
                    if (aLine.isEmpty()) {
                        continue;
                    }
                    aLine = aLine.trim();
                    dataArray = aLine.split("\\s+");
                    if (dataArray.length < dn){
                        aLine = sr.readLine().trim();
                        tempArray = aLine.split("\\s+");
                        dataArray = (String[])DataConvert.resizeArray(dataArray, dn);
                        for (i = 0; i < tempArray.length; i++){
                            dataArray[dn - tempArray.length + i] = tempArray[i];
                        }
                    }
                    TrajIdx = Integer.parseInt(dataArray[0]) - 1;
                    int y = Integer.parseInt(dataArray[2]);
                    if (y < 100) {
                        if (y > 50) {
                            y = 1900 + y;
                        } else {
                            y = 2000 + y;
                        }
                    }
                    cal.set(y, Integer.parseInt(dataArray[3]) - 1,
                            Integer.parseInt(dataArray[4]), Integer.parseInt(dataArray[5]), 0, 0);

                    aPoint = new PointD();
                    aPoint.X = DateUtil.toOADate(cal.getTime());
                    aPoint.Y = Double.parseDouble(dataArray[varIndex]);
                    PointList.get(TrajIdx).add(aPoint);
                }

                //SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    int n = PointList.get(i).size();
                    double[] xvs = new double[n];
                    double[] yvs = new double[n];
                    for (int j = 0; j < n; j++) {
                        xvs[j] = PointList.get(i).get(j).X;
                        yvs[j] = PointList.get(i).get(j).Y;
                    }
                    dataset.addSeries("Traj_" + String.valueOf(trajNum), xvs, yvs);
                    trajNum += 1;
                }

                sr.close();
            } catch (IOException | NumberFormatException ex) {
                Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return dataset;
    }
    
    /**
     * Get XYDataset - X dimension is hours from start point
     *
     * @param varIndex Variable index
     * @return XYDataset
     */
    public XYListDataset getXYDataset_HourX(int varIndex) {
        XYListDataset dataset = new XYListDataset();
        Calendar cal = Calendar.getInstance();
        int trajNum = 1;
        for (int t = 0; t < fileNames.size(); t++) {
            try {
                String aFile = fileNames.get(t);
                BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray, tempArray;
                int i;

                //Record #1
                sr.readLine();

                //Record #2
                for (i = 0; i < meteoFileNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #3
                sr.readLine();

                //Record #4             
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #5
                sr.readLine();

                //Record #6
                int TrajIdx;
                List<PointD> pList;
                List<List<PointD>> PointList = new ArrayList<>();
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    pList = new ArrayList<>();
                    PointList.add(pList);
                }
                PointD aPoint;
                int dn = 12 + this.varNums.get(t);
                while (true) {
                    aLine = sr.readLine();
                    if (aLine == null) {
                        break;
                    }
                    if (aLine.isEmpty()) {
                        continue;
                    }
                    aLine = aLine.trim();
                    dataArray = aLine.split("\\s+");
                    if (dataArray.length < dn){
                        aLine = sr.readLine().trim();
                        tempArray = aLine.split("\\s+");
                        dataArray = (String[])DataConvert.resizeArray(dataArray, dn);
                        for (i = 0; i < tempArray.length; i++){
                            dataArray[dn - tempArray.length + i] = tempArray[i];
                        }
                    }
                    TrajIdx = Integer.parseInt(dataArray[0]) - 1;
                    int y = Integer.parseInt(dataArray[2]);
                    if (y < 100) {
                        if (y > 50) {
                            y = 1900 + y;
                        } else {
                            y = 2000 + y;
                        }
                    }
                    cal.set(y, Integer.parseInt(dataArray[3]) - 1,
                            Integer.parseInt(dataArray[4]), Integer.parseInt(dataArray[5]), 0, 0);

                    aPoint = new PointD();
                    aPoint.X = DateUtil.toOADate(cal.getTime());
                    aPoint.Y = Double.parseDouble(dataArray[varIndex]);
                    PointList.get(TrajIdx).add(aPoint);
                }

                //SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < trajeoryNums.get(t); i++) {
                    int n = PointList.get(i).size();
                    double[] xvs = new double[n];
                    double[] yvs = new double[n];
                    Date cdate, sdate = new Date();
                    for (int j = 0; j < n; j++) {
                        cdate = DateUtil.fromOADate(PointList.get(i).get(j).X);
                        if (j == 0) {
                            sdate = cdate;
                            xvs[j] = 0;
                        } else {
                            xvs[j] = DateUtil.getHours(cdate, sdate);
                        }
                        yvs[j] = PointList.get(i).get(j).Y;
                    }
                    dataset.addSeries("Traj_" + String.valueOf(trajNum), xvs, yvs);
                    trajNum += 1;
                }

                sr.close();
            } catch (IOException | NumberFormatException ex) {
                Logger.getLogger(HYSPLITTrajDataInfo_bak.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return dataset;
    }
    
    // </editor-fold>
}
