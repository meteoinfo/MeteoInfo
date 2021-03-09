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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meteoinfo.common.PointD;
import org.meteoinfo.common.util.JDateUtil;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.LegendType;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.data.meteodata.hysplit.TrajectoryInfo;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.geometry.shape.PointShape;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.table.Field;

 /**
 *
 * @author yaqiang
 */
public class MICAPS7DataInfo extends DataInfo {

    // <editor-fold desc="Variables">
    public List<String> FileNames;
    /// <summary>
    /// Number of meteorological files
    /// </summary>
    public List<Integer> MeteoFileNums;
    /// <summary>
    /// Number of trajectories
    /// </summary>
    public int TrajeoryNumber;
    /// <summary>
    /// Number of trajectories
    /// </summary>
    public List<Integer> TrajeoryNums;
    /// <summary>
    /// Trajectory direction - foreward or backward
    /// </summary>
    public List<String> TrajDirections;
    /// <summary>
    /// Vertical motion
    /// </summary>
    public List<String> VerticalMotions;
    /// <summary>
    /// Information list of trajectories
    /// </summary>
    public List<List<TrajectoryInfo>> TrajInfos;
    /// <summary>
    /// Number of variables
    /// </summary>
    public List<Integer> VarNums;
    /// <summary>
    /// Variable name list
    /// </summary>
    public List<List<String>> VarNames;
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
        FileNames = new ArrayList<>();
        MeteoFileNums = new ArrayList<>();
        TrajeoryNums = new ArrayList<>();
        TrajDirections = new ArrayList<>();
        VerticalMotions = new ArrayList<>();
        TrajInfos = new ArrayList<>();
        VarNums = new ArrayList<>();
        VarNames = new ArrayList<>();
        TrajeoryNumber = 0;
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
            Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readDataInfo(String[] trajFiles) throws IOException {
        this.setFileName(trajFiles[0]);
        String aLine;
        String[] dataArray;
        int t;

        initVariables();
        List<Double> times = new ArrayList<>();

        for (t = 0; t < trajFiles.length; t++) {
            String aFile = trajFiles[t];
            FileNames.add(aFile);

            BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));

            TrajectoryInfo aTrajInfo = new TrajectoryInfo();
            List<TrajectoryInfo> trajInfoList = new ArrayList<>();
            sr.readLine();
            aLine = sr.readLine();
            int trajIdx = -1;
            int trajNum = 0;
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
            TrajeoryNums.add(trajNum);
            TrajeoryNumber += TrajeoryNums.get(t);
            TrajInfos.add(trajInfoList);

            Dimension tdim = new Dimension(DimensionType.T);
            tdim.setValues(times);
            this.setTimeDimension(tdim);

            sr.close();

            Variable var = new Variable();
            var.setName("Traj");
            var.setStation(true);
            var.setDimension(tdim);
            List<Variable> variables = new ArrayList<>();
            variables.add(var);
            this.setVariables(variables);

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
        for (int t = 0; t < FileNames.size(); t++) {
            dataInfo += "File Name: " + FileNames.get(t);
            dataInfo += System.getProperty("line.separator") + "Typhoon number = " + String.valueOf(TrajeoryNums.get(t));
            dataInfo += System.getProperty("line.separator") + System.getProperty("line.separator") + "Typhoons:";
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
            for (TrajectoryInfo aTrajInfo : TrajInfos.get(t)) {
                dataInfo += System.getProperty("line.separator") + "  " + aTrajInfo.trajName + " "
                        + aTrajInfo.trajID + " " + aTrajInfo.trajCenter + " " + format.format(aTrajInfo.startTime)
                        + "  " + String.valueOf(aTrajInfo.startLat) + "  " + String.valueOf(aTrajInfo.startLon)
                        + "  " + String.valueOf(aTrajInfo.startHeight);
            }

            if (t < FileNames.size() - 1) {
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
        Variable var = this.getVariable(varName);
        int n = var.getDimNumber();
        int[] origin = new int[n];
        int[] size = new int[n];
        int[] stride = new int[n];
        for (int i = 0; i < n; i++){
            origin[i] = 0;
            size[i] = var.getDimLength(i);
            stride[i] = 1;
        }
        
        Array r = read(varName, origin, size, stride);
        
        return r;
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

    /**
     * Get a trajectory points data
     *
     * @param aTrajIdx The trajectory index
     * @return A trajectory points data
     */
    public List<List<Object>> getATrajData(int aTrajIdx) {
        List<List<Object>> trajPointsData = new ArrayList<>();

        boolean ifExit = false;
        for (int t = 0; t < FileNames.size(); t++) {
            BufferedReader sr = null;
            try {
                String aFile = FileNames.get(t);
                sr = new BufferedReader(new FileReader(new File(aFile)));
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
                if (ifExit) {
                    break;
                }
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
        }

        return trajPointsData;
    }
    // </editor-fold>
}
