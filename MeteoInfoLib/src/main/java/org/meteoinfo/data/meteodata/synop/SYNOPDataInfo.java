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
package org.meteoinfo.data.meteodata.synop;

import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModel;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.data.meteodata.Attribute;

/**
 *
 * @author yaqiang
 */
public class SYNOPDataInfo extends DataInfo implements IStationDataInfo {

    // <editor-fold desc="Variables">
    private String stFileName;
    private LocalDateTime date;
    private int stationNum;
    private final List<String> varList;
    private List<List<String>> DataList;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public SYNOPDataInfo() {
        String[] items = new String[]{"Visibility", "CloudCover", "WindDirection", "WindSpeed", "Temperature", "DewPoint",
            "Pressure", "Precipitation", "Weather"};
        varList = Arrays.asList(items);
        DataList = new ArrayList<>();
        this.setDataType(MeteoDataType.SYNOP);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get station file name
     *
     * @return Station file name
     */
    public String getStationFileName() {
        return this.stFileName;
    }

    /**
     * Set station file name
     *
     * @param value Station file name
     */
    public void setStationFileName(String value) {
        this.stFileName = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        BufferedReader sr = null;
        try {
            this.setFileName(fileName);
            //Read stations
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(this.stFileName)));
            String aLine;
            String[] dataArray;
            List<String> dataList;
            List<String> stIDList = new ArrayList<>();
            List<String[]> stPosList = new ArrayList<>();
            int i;
            sr.readLine();
            while (true) {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                if (aLine.isEmpty()) {
                    continue;
                }
                dataArray = aLine.split(",");
                stIDList.add(dataArray[1]);
                stPosList.add(new String[]{dataArray[5], dataArray[4]});
            }
            sr.close();

            //Read data
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            List<List<String>> disDataList = new ArrayList<>();
            String reportType = "AAXX", str, stID;
            LocalDateTime toDay = LocalDateTime.now();
            LocalDateTime tt = LocalDateTime.now();
            String windSpeedIndicator = "/";
            int stIdx;
            boolean isSetTime = true;
            while (true) {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }

                aLine = aLine.trim();
                if (aLine.isEmpty()) {
                    continue;
                }

                if (aLine.length() == 3 && MIMath.isNumeric(aLine)) //Skip group number
                {
                    sr.readLine();    //Skip 090000 line                
                    continue;
                }

                //if (aLine.Substring(0, 2) == "SI" || aLine.Substring(0,2) == "SN")    //Skip "SI????" line
                //    continue;
                if (aLine.length() < 4) {
                    continue;
                }

                switch (aLine.substring(0, 4)) {
                    case "AAXX":    //A SYNOP report from a fixed land station is identified by the symbolic letters MiMiMjMj = AAXX
                        reportType = "AAXX";
                        str = aLine.substring(aLine.length() - 5, aLine.length());
                        if (isSetTime) {
                            tt = LocalDateTime.of(toDay.getYear(), toDay.getMonth(), Integer.parseInt(str.substring(0, 2)),
                                    Integer.parseInt(str.substring(2, 4)), 0, 0);
                            this.date = tt;
                            isSetTime = false;
                        }
                        windSpeedIndicator = str.substring(str.length() - 1, str.length());
                        break;
                    case "BBXX":    //A SHIP report from a sea station is identified by the symbolic letters MiMiMjMj = BBXX
                        reportType = "BBXX";
                        break;
                    case "OOXX":    //A SYNOP MOBIL report from a mobile land station is identified by the symbolic letters MiMiMjMj = OOXX
                        reportType = "OOXX";
                        break;
                    default:    //Data line
                        while (!aLine.substring(aLine.length() - 1, aLine.length()).equals("=")) {
                            str = sr.readLine();
                            if (str == null) {
                                break;
                            }
                            aLine = aLine + " " + sr.readLine();
                        }

                        dataArray = aLine.split("\\s+");
                        dataList = new ArrayList<>();
                        for (i = 0; i < dataArray.length; i++) {
                            dataList.add(dataArray[i]);
                        }

                        stID = dataList.get(0);
                        switch (reportType) {
                            case "AAXX":
                                if (dataList.size() > 2) {
                                    stIdx = stIDList.indexOf(stID);
                                    if (stIdx >= 0) {
                                        dataList.add(0, windSpeedIndicator);
                                        dataList.add(0, reportType);
                                        dataList.add(0, stPosList.get(stIdx)[1]);
                                        dataList.add(0, stPosList.get(stIdx)[0]);
                                        disDataList.add(dataList);
                                    }
                                }
                                break;
                            case "BBXX":
                            case "OOXX":
                                if (dataList.size() > 5) {
                                    if (dataList.get(2).contains("/") || dataList.get(3).contains("/")) {
                                        continue;
                                    }

                                    if (!dataList.get(2).substring(0, 2).equals("99")) {
                                        continue;
                                    }

                                    str = dataList.get(1);
                                    windSpeedIndicator = str.substring(str.length() - 1, str.length());

                                    float lat = Float.parseFloat(dataList.get(2).substring(2)) / 10;
                                    float lon = Float.parseFloat(dataList.get(3).substring(1)) / 10;
                                    if (lat > 90 || lon > 180) {
                                        continue;
                                    }

                                    switch (dataList.get(3).substring(0, 1)) {
                                        case "1":    //North east

                                            break;
                                        case "3":    //South east
                                            lat = -lat;
                                            break;
                                        case "5":    //South west
                                            lat = -lat;
                                            lon = -lon;
                                            break;
                                        case "7":    //North west
                                            lon = -lon;
                                            break;
                                    }

                                    dataList.add(0, windSpeedIndicator);
                                    dataList.add(0, reportType);
                                    dataList.add(0, String.valueOf(lat));
                                    dataList.add(0, String.valueOf(lon));
                                    disDataList.add(dataList);
                                }
                                break;
                        }
                        break;

                }
            }
            sr.close();

            stationNum = disDataList.size();
            DataList = disDataList;

            Dimension tdim = new Dimension(DimensionType.T);
            double[] values = new double[1];
            values[0] = JDateUtil.toOADate(date);
            tdim.setValues(values);
            this.setTimeDimension(tdim);
            List<Variable> vars = new ArrayList<>();
            for (String vName : varList) {
                Variable var = new Variable();
                var.setName(vName);
                var.setDimension(tdim);
                var.setStation(true);
                vars.add(var);
            }
            this.setVariables(vars);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SYNOPDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SYNOPDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (sr != null)
                    sr.close();
            } catch (IOException ex) {
                Logger.getLogger(SYNOPDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
        dataInfo += System.getProperty("line.separator") + "Time: " + format.format(this.getTimes().get(0));
        dataInfo += System.getProperty("line.separator") + "Station Number: " + String.valueOf(this.stationNum);
        dataInfo += System.getProperty("line.separator") + "Number of Variables = " + String.valueOf(this.getVariableNum());
        for (int i = 0; i < this.getVariableNum(); i++) {
            dataInfo += System.getProperty("line.separator") + "\t" + this.getVariableNames().get(i);
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

    @Override
    public StationData getStationData(int timeIdx, String varName, int levelIdx) {
        StationData stationData = new StationData();
        List<String> stations = new ArrayList<>();
        int varIdx = this.getVariableIndex(varName);
        String aStid;
        int i;
        float lon, lat;
        List<String> dataList;
        //double[,] DiscreteData = new double[3, DataList.Count];
        List<double[]> discreteData = new ArrayList<>();
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        String windSpeedIndicator = "/";
        String reportType = "AAXX";
        //string precIndicator;

        for (i = 0; i < DataList.size(); i++) {
            dataList = DataList.get(i);
            reportType = dataList.get(2);
            windSpeedIndicator = dataList.get(3);
            aStid = dataList.get(4);
            lon = Float.parseFloat(dataList.get(0));
            lat = Float.parseFloat(dataList.get(1));
            int sIdx = 5;
            switch (reportType) {
                case "BBXX":
                case "OOXX":
                    sIdx = 8;
                    break;
            }

            double[] disData = new double[3];
            disData[0] = lon;
            disData[1] = lat;
            disData[2] = getDataValue(dataList, varIdx, sIdx, windSpeedIndicator);
            stations.add(aStid);
            discreteData.add(disData);

            //Get extent
            if (i == 0) {
                minX = lon;
                maxX = minX;
                minY = lat;
                maxY = minY;
            } else {
                if (minX > lon) {
                    minX = lon;
                } else if (maxX < lon) {
                    maxX = lon;
                }
                if (minY > lat) {
                    minY = lat;
                } else if (maxY < lat) {
                    maxY = lat;
                }
            }
        }
        Extent dataExtent = new Extent();
        dataExtent.minX = minX;
        dataExtent.maxX = maxX;
        dataExtent.minY = minY;
        dataExtent.maxY = maxY;

        stationData.data = new double[discreteData.size()][3];
        for (i = 0; i < discreteData.size(); i++) {
            stationData.data[i][0] = discreteData.get(i)[0];
            stationData.data[i][1] = discreteData.get(i)[1];
            stationData.data[i][2] = discreteData.get(i)[2];
        }
        stationData.dataExtent = dataExtent;
        stationData.stations = stations;

        return stationData;
    }

    private double getDataValue(List<String> dataList, int vIdx, int sIdx, String windSpeedIndicator) {
        double value = this.getMissingValue();
        String str;
        int i;
        switch (vIdx) {
            case 0:    //Visibility
                str = dataList.get(sIdx);
                if (str.length() != 5) {
                    break;
                }

                str = str.substring(3);
                if (str.contains("/")) {
                    break;
                }

                value = Integer.parseInt(str);
                if (value <= 50) {
                    value = value / 10;
                } else if (value >= 56 && value <= 80) {
                    value = value - 50;
                } else if (value >= 81 && value <= 89) {
                    value = 30 + (value - 80) * 5;
                } else if (value >= 90 && value <= 99) {
                    switch ((int) value) {
                        case 90:
                            value = 0.04;
                            break;
                        case 91:
                            value = 0.05;
                            break;
                        case 92:
                            value = 0.2;
                            break;
                        case 93:
                            value = 0.5;
                            break;
                        case 94:
                            value = 1;
                            break;
                        case 95:
                            value = 2;
                            break;
                        case 96:
                            value = 4;
                            break;
                        case 97:
                            value = 10;
                            break;
                        case 98:
                            value = 20;
                            break;
                        case 99:
                            value = 50;
                            break;
                    }
                }
                break;
            case 1:   //Cloud cover
                str = dataList.get(sIdx + 1);
                if (str.length() != 5) {
                    break;
                }

                str = str.substring(0, 1);
                if (str.equals("/")) {
                    break;
                }

                value = Integer.parseInt(str);
                break;
            case 2:    //Wind direction
                str = dataList.get(sIdx + 1);
                if (str.length() != 5) {
                    break;
                }

                str = str.substring(1, 3);
                if (str.equals("//")) {
                    break;
                }

                value = Integer.parseInt(str) * 10;
                if (value > 360) {
                    value = 0;
                }
                break;
            case 3:    //Wind speed
                if (windSpeedIndicator.equals("/")) {
                    break;
                }

                str = dataList.get(sIdx + 1);
                if (str.length() != 5) {
                    break;
                }

                str = str.substring(3);
                if (str.contains("/")) {
                    break;
                }

                if (str.equals("99")) {
                    str = dataList.get(sIdx + 2).substring(2);
                    if (str.contains("/")) {
                        break;
                    }

                    value = Integer.parseInt(str);
                } else {
                    value = Integer.parseInt(str);
                }

                if (windSpeedIndicator.equals("3") || windSpeedIndicator.equals("4")) {
                    value = value * 0.51444;    //Convert KT to MPS
                }
                break;
            case 4:    //Temperature
                str = "";
                for (i = sIdx + 2; i < dataList.size(); i++) {
                    if (dataList.get(i).length() == 5 && dataList.get(i).substring(0, 1).equals("1")) {
                        str = dataList.get(i);
                        break;
                    }
                }
                if (!str.isEmpty()) {
                    if (str.contains("/")) {
                        break;
                    }

                    String sign = str.substring(1, 2);
                    value = Double.parseDouble(str.substring(2)) / 10;
                    if (sign.equals("1")) {
                        value = -value;
                    }
                }
                break;
            case 5:    //Dew point
                str = "";
                for (i = sIdx + 2; i < dataList.size(); i++) {
                    if (dataList.get(i).length() == 5 && dataList.get(i).substring(0, 1).equals("2")) {
                        str = dataList.get(i);
                        break;
                    }
                }
                if (!str.isEmpty()) {
                    if (str.contains("/")) {
                        break;
                    }

                    String sign = str.substring(1, 2);
                    if (sign.equals("9")) //Relative humidity
                    {
                        break;
                    }

                    value = Double.parseDouble(str.substring(2)) / 10;
                    if (sign.equals("1")) {
                        value = -value;
                    }
                }
                break;
            case 6:    //Pressure
                str = "";
                for (i = sIdx + 2; i < dataList.size(); i++) {
                    if (dataList.get(i).length() == 5 && dataList.get(i).substring(0, 1).equals("3")) {
                        str = dataList.get(i);
                        break;
                    }
                }
                if (!str.isEmpty()) {
                    if (str.contains("/")) {
                        break;
                    }

                    if (!MIMath.isNumeric(str.substring(1))) {
                        break;
                    }

                    value = Double.parseDouble(str.substring(1)) / 10;
                    value = value / 10;
                    if (value < 500) {
                        value += 1000;
                    }
                }
                break;
            case 7:    //Precipitation
                str = "";
                for (i = sIdx + 2; i < dataList.size(); i++) {
                    if (dataList.get(i).length() == 5 && dataList.get(i).substring(0, 1).equals("6")) {
                        str = dataList.get(i);
                        break;
                    }
                }
                if (!str.isEmpty()) {
                    if (str.contains("/")) {
                        break;
                    }

                    value = Double.parseDouble(str.substring(1, 4));
                    if (value >= 990) {
                        value = value - 990;
                    }
                }
                break;
            case 8:    //Weather
                str = "";
                for (i = sIdx + 2; i < dataList.size(); i++) {
                    if (dataList.get(i).length() == 5 && dataList.get(i).substring(0, 1).equals("7")) {
                        str = dataList.get(i);
                        break;
                    }
                }
                if (!str.isEmpty()) {
                    if (str.substring(1, 3).contains("/")) {
                        break;
                    }

                    value = Integer.parseInt(str.substring(1, 3));
                }
                break;
        }

        return value;
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        return null;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        StationModelData smData = new StationModelData();
        String aStid;
        int i;
        float lon, lat;
        List<String> dataList;
        List<StationModel> smList = new ArrayList<>();
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        String windSpeedIndicator = "/";
        String reportType = "AAXX";

        for (i = 0; i < DataList.size(); i++) {
            dataList = DataList.get(i);
            reportType = dataList.get(2);
            windSpeedIndicator = dataList.get(3);
            aStid = dataList.get(4);
            lon = Float.parseFloat(dataList.get(0));
            lat = Float.parseFloat(dataList.get(1));
            int sIdx = 5;
            switch (reportType) {
                case "BBXX":
                case "OOXX":
                    sIdx = 8;
                    break;
            }
            //Initialize data
            StationModel sm = new StationModel();
            sm.setLongitude(lon);
            sm.setLatitude(lat);
            sm.setWindDirection(getDataValue(dataList, 2, sIdx, windSpeedIndicator));    //Wind direction
            sm.setWindSpeed(getDataValue(dataList, 3, sIdx, windSpeedIndicator));    //Wind speed
            sm.setVisibility(getDataValue(dataList, 0, sIdx, windSpeedIndicator));    //Visibility
            sm.setWeather(getDataValue(dataList, 8, sIdx, windSpeedIndicator));    //Weather
            sm.setCloudCover(getDataValue(dataList, 1, sIdx, windSpeedIndicator));    //Cloud cover
            sm.setTemperature(getDataValue(dataList, 4, sIdx, windSpeedIndicator));    //Temperature
            sm.setDewPoint(getDataValue(dataList, 5, sIdx, windSpeedIndicator));    //Dew point 

            smList.add(sm);

            //Get extent
            if (i == 0) {
                minX = lon;
                maxX = minX;
                minY = lat;
                maxY = minY;
            } else {
                if (minX > lon) {
                    minX = lon;
                } else if (maxX < lon) {
                    maxX = lon;
                }
                if (minY > lat) {
                    minY = lat;
                } else if (maxY < lat) {
                    maxY = lat;
                }
            }
        }
        Extent dataExtent = new Extent();
        dataExtent.minX = minX;
        dataExtent.maxX = maxX;
        dataExtent.minY = minY;
        dataExtent.maxY = maxY;

        smData.setData(smList);
        smData.setDataExtent(dataExtent);
        smData.setMissingValue(this.getMissingValue());

        return smData;
    }
    // </editor-fold>
}
