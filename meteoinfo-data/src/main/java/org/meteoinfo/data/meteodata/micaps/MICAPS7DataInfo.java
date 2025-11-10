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
 package org.meteoinfo.data.meteodata.micaps;

 import java.io.*;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;

 import org.meteoinfo.common.PointD;
 import org.meteoinfo.common.util.JDateUtil;
 import org.meteoinfo.data.dimarray.Dimension;
 import org.meteoinfo.data.dimarray.DimensionType;
 import org.meteoinfo.data.meteodata.*;
 import org.meteoinfo.data.meteodata.hysplit.HYSPLITTrajDataInfo;
 import org.meteoinfo.data.meteodata.TrajectoryInfo;
 import org.meteoinfo.ndarray.*;
 import org.meteoinfo.table.ColumnData;
 import org.meteoinfo.table.DataColumn;
 import org.meteoinfo.table.DataTable;

 /**
  * @author yaqiang
  */
 public class MICAPS7DataInfo extends DataInfo implements ITrajDataInfo {

     // <editor-fold desc="Variables">
     // Number of trajectories
     private int trajNum;
     // Information list of trajectories
     private List<TrajectoryInfo> trajInfoList;
     private String[] varNames;
     private int pointNum;
     private List<DataTable> dataTables;
     private String[] inVarNames;
     // </editor-fold>
     // <editor-fold desc="Constructor">

     /**
      * Constructor
      */
     public MICAPS7DataInfo() {
         this.setDataType(MeteoDataType.MICAPS_7);
         initVariables();
     }

     private void initVariables() {
         varNames = new String[]{"time", "run_hour", "lon", "lat", "wind_speed", "pressure", "radius_7",
                 "radius_10", "move_dir", "move_speed"};
         inVarNames = new String[]{"time", "run_hour", "lon", "lat"};
     }
     // </editor-fold>
     // <editor-fold desc="Get Set Methods">

     @Override
     public String getXVarName() {
         return "lon";
     }

     @Override
     public String getYVarName() {
         return "lat";
     }

     @Override
     public String getZVarName() {
         return null;
     }

     @Override
     public String getTVarName() {
         return "time";
     }

     @Override
     public List<TrajectoryInfo> getTrajInfoList() {
         return this.trajInfoList;
     }

     /**
      * Get data table list
      *
      * @return Data table list
      */
     @Override
     public List<DataTable> getDataTables() {
         return this.dataTables;
     }
     // </editor-fold>
     // <editor-fold desc="Methods">
     @Override
     public boolean isValidFile(RandomAccessFile raf) {
         return false;
     }

     @Override
     public void readDataInfo(String fileName) {
         this.setFileName(fileName);
         String aLine;
         String[] dataArray;
         int t;

         initVariables();
         List<Double> times = new ArrayList<>();
         this.pointNum = 0;
         int pn;

         BufferedReader sr = null;
         try {
             sr = new BufferedReader(new FileReader(fileName));

             this.trajInfoList = new ArrayList<>();
             TrajectoryInfo aTrajInfo = new TrajectoryInfo();
             sr.readLine();
             aLine = sr.readLine();
             int trajIdx = -1;
             this.trajNum = 0;
             while (aLine != null) {
                 if (aLine.trim().isEmpty()) {
                     aLine = sr.readLine();
                     continue;
                 }

                 dataArray = aLine.split("\\s+");
                 if (dataArray.length == 4) {
                     aTrajInfo = new TrajectoryInfo();
                     aTrajInfo.trajName = dataArray[0];
                     aTrajInfo.trajID = dataArray[1];
                     aTrajInfo.trajCenter = dataArray[2];
                     pn = Integer.parseInt(dataArray[3]);
                     if (this.pointNum < pn)
                         this.pointNum = pn;
                     trajIdx = -1;
                     trajNum += 1;
                 } else if (dataArray.length == 13) {
                     trajIdx += 1;
                     if (trajIdx == 0) {
                         int year = Integer.parseInt(dataArray[0]);
                         if (year < 100) {
                             if (year < 50) {
                                 year = 2000 + year;
                             } else {
                                 year = 1900 + year;
                             }
                         }
                         LocalDateTime tt = LocalDateTime.of(year, Integer.parseInt(dataArray[1]),
                                 Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);
                         if (times.isEmpty()) {
                             times.add(JDateUtil.toOADate(tt));
                         }

                         aTrajInfo.startTime = tt;
                         aTrajInfo.startLat = Float.parseFloat(dataArray[6]);
                         aTrajInfo.startLon = Float.parseFloat(dataArray[5]);
                         trajInfoList.add(aTrajInfo);
                     }
                 }
                 aLine = sr.readLine();
             }
             sr.close();

             //Dimensions
             Dimension trajDim = new Dimension(DimensionType.OTHER);
             trajDim.setName("trajectory");
             trajDim.setLength(trajNum);
             this.addDimension(trajDim);
             Dimension obsDim = new Dimension(DimensionType.OTHER);
             obsDim.setName("obs");
             obsDim.setLength(this.pointNum);
             this.addDimension(obsDim);

             //Variables
             for (String vName : this.varNames) {
                 Variable var = new Variable();
                 var.setName(vName);
                 switch (vName) {
                     case "time":
                         var.setDataType(DataType.DATE);
                         break;
                     case "run_hour":
                         var.setDataType(DataType.INT);
                         break;
                     default:
                         var.setDataType(DataType.FLOAT);
                         break;
                 }
                 var.addDimension(trajDim);
                 var.addDimension(obsDim);
                 var.addAttribute("long_name", vName);
                 if (!Arrays.asList(this.inVarNames).contains(vName))
                    var.setStation(true);
                 this.addVariable(var);
             }

             //Read tables
             this.dataTables = this.readTable();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             try {
                 if (sr != null) {
                     sr.close();
                 }
             } catch (IOException ex) {
                 Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }

     /**
      * Get global attributes
      *
      * @return Global attributes
      */
     @Override
     public List<Attribute> getGlobalAttributes() {
         return new ArrayList<>();
     }

     @Override
     public String generateInfoText() {
         String dataInfo = "";
         dataInfo += "File Name: " + this.fileName;
         dataInfo += System.getProperty("line.separator") + "Typhoon number = " + String.valueOf(this.trajNum);
         dataInfo += System.getProperty("line.separator") + System.getProperty("line.separator") + "Typhoons:";
         DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
         for (TrajectoryInfo aTrajInfo : this.trajInfoList) {
             dataInfo += System.getProperty("line.separator") + "  " + aTrajInfo.trajName + " "
                     + aTrajInfo.trajID + " " + aTrajInfo.trajCenter + " " + format.format(aTrajInfo.startTime)
                     + "  " + String.valueOf(aTrajInfo.startLat) + "  " + String.valueOf(aTrajInfo.startLon)
                     + "  " + String.valueOf(aTrajInfo.startHeight);
         }

         dataInfo += System.getProperty("line.separator") + super.generateInfoText();

         return dataInfo;
     }

     /**
      * Read array data of a variable
      *
      * @param varName Variable name
      * @return Array data
      */
     @Override
     public Array realRead(String varName) {
         int[] origin = new int[]{0, 0};
         int[] size = new int[]{this.trajNum, this.pointNum};
         int[] stride = new int[]{1, 1};

         Array r = realRead(varName, origin, size, stride);

         return r;
     }

     /**
      * Read array data of the variable
      *
      * @param varName Variable name
      * @param origin  The origin array
      * @param size    The size array
      * @param stride  The stride array
      * @return Array data
      */
     @Override
     public Array realRead(String varName, int[] origin, int[] size, int[] stride) {
         try {
             DataColumn col = this.dataTables.get(0).findColumn(varName);
             DataType dtype = col.getDataType();
             switch (col.getDataType()){
                 case DATE:
                     dtype = DataType.DOUBLE;
                     break;
             }
             Section section = new Section(origin, size, stride);
             Array array = Array.factory(dtype, section.getShape());
             Range trajRange = section.getRange(0);
             Range obsRange = section.getRange(1);
             Index index = array.getIndex();
             for (int trajIdx = trajRange.first(); trajIdx <= trajRange.last(); trajIdx += trajRange.stride()){
                 DataTable dTable = this.dataTables.get(trajIdx);
                 ColumnData colData = dTable.getColumnData(varName);
                 for (int obsIdx = obsRange.first(); obsIdx <= obsRange.last(); obsIdx += obsRange.stride()){
                     if (colData.size() > obsIdx)
                         if (col.getDataType() == DataType.DATE) {
                             array.setObject(index, JDateUtil.toOADate((LocalDateTime) colData.getValue(obsIdx)));
                         } else {
                             array.setObject(index, colData.getValue(obsIdx));
                         }
                     else
                         array.setObject(index, Double.NaN);
                     index.incr();
                 }
             }

             return array;
         } catch (InvalidRangeException ex) {
             Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }

     /**
      * Read trajectories as data table list.
      *
      * @return Data table list
      * @throws Exception
      */
     public List<DataTable> readTable() throws Exception {
         List<DataTable> tables = new ArrayList<>();
         for (int i = 0; i < this.trajNum; i++) {
             DataTable table = new DataTable();
             for (Variable variable : this.variables) {
                 table.addColumn(variable.getName(), variable.getDataType());
             }
             tables.add(table);
         }

         try {
             BufferedReader sr = new BufferedReader(new FileReader(this.getFileName()));
             String[] dataArray;
             float v;
             int trajId = -1;
             sr.readLine();
             String line = sr.readLine();
             while (line != null) {
                 line = line.trim();
                 if (line.isEmpty()) {
                     line = sr.readLine();
                     continue;
                 }
                 dataArray = line.split("\\s+");
                 switch (dataArray.length) {
                     case 4:
                         trajId += 1;
                         break;
                     case 13:
                         DataTable dataTable = tables.get(trajId);
                         dataTable.addRow();
                         int rowIdx = dataTable.getRowCount() - 1;
                         int year = Integer.parseInt(dataArray[0]);
                         if (year < 50)
                             year = 2000 + year;
                         else
                             year = 1900 + year;
                         LocalDateTime tt = LocalDateTime.of(year, Integer.parseInt(dataArray[1]),
                                 Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);
                         int runHour = Integer.parseInt(dataArray[4]);
                         tt = tt.plusHours(runHour);
                         int i = 3;
                         for (String vName : this.varNames) {
                             switch (vName) {
                                 case "time":
                                     dataTable.setValue(rowIdx, vName, tt);
                                     break;
                                 case "run_hour":
                                     dataTable.setValue(rowIdx, vName, runHour);
                                     break;
                                 default:
                                     v = Float.parseFloat(dataArray[i]);
                                     if (v == 9999)
                                         v = Float.NaN;
                                     dataTable.setValue(rowIdx, vName, v);
                                     break;
                             }
                             i += 1;
                         }
                         break;
                 }
                 line = sr.readLine();
             }
             sr.close();
         } catch (IOException ex) {
             Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
             Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
         }

         return tables;
     }

     /**
      * Get a trajectory points data
      *
      * @param aTrajIdx The trajectory index
      * @return A trajectory points data
      */
     public List<List<Object>> getATrajData(int aTrajIdx) {
         List<List<Object>> trajPointsData = new ArrayList<>();

         boolean ifExit = false;
         BufferedReader sr = null;
         try {
             sr = new BufferedReader(new FileReader(this.fileName));
             String aLine;
             String[] dataArray;
             //
             int TrajIdx = -1;
             PointD aPoint;
             sr.readLine();
             aLine = sr.readLine();
             while (aLine != null) {
                 if (aLine.trim().isEmpty()) {
                     aLine = sr.readLine();
                     continue;
                 }
                 dataArray = aLine.split("\\s+");
                 switch (dataArray.length) {
                     case 4:
                         TrajIdx += 1;
                         if (TrajIdx > aTrajIdx) {
                             ifExit = true;
                         }

                         break;
                     case 13:
                         if (TrajIdx == aTrajIdx) {
                             List<Object> dList = new ArrayList<>();
                             LocalDateTime tt = LocalDateTime.of(Integer.parseInt(dataArray[0]), Integer.parseInt(dataArray[1]),
                                     Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);
                             aPoint = new PointD();
                             aPoint.X = Double.parseDouble(dataArray[5]);
                             aPoint.Y = Double.parseDouble(dataArray[6]);
                             dList.add(aPoint);
                             dList.add(tt);
                             dList.add(Double.parseDouble(dataArray[7]));

                             trajPointsData.add(dList);
                         }
                         break;
                 }
                 if (ifExit) {
                     break;
                 }

                 aLine = sr.readLine();
             }
             sr.close();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 sr.close();
             } catch (IOException ex) {
                 Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
             }
         }

         return trajPointsData;
     }
     // </editor-fold>
 }
