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
package org.meteoinfo.data.meteodata.metar;

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
public class METARDataInfo extends DataInfo implements IStationDataInfo {

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
    public METARDataInfo() {
        String[] items = new String[]{"WindDirection", "WindSpeed", "Visibility", "Weather",
            "CloudCover", "Temperature", "DewPoint", "Altimeter"};
        varList = Arrays.asList(items);
        DataList = new ArrayList<>();
        this.setDataType(MeteoDataType.METAR);
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
            List<String> stNameList = new ArrayList<>();
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
                stNameList.add(dataArray[1]);
                stPosList.add(new String[]{dataArray[2], dataArray[3]});
            }
            sr.close();

            //Read METAR data
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            List<List<String>> disDataList = new ArrayList<>();
            List<String> stList = new ArrayList<>();
            int stIdx;
            int stIdx1;
            String stName;
            aLine = sr.readLine();
            if (aLine.isEmpty()) {
                aLine = sr.readLine();
            }
            aLine = aLine.trim();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            LocalDateTime ddate = LocalDateTime.parse(aLine, format);
            ddate = ddate.plusMinutes(29);
            ddate = ddate.minusMinutes(ddate.getMinute());
            this.date = ddate;
            while (true) {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                if (aLine.isEmpty()) {
                    continue;
                }
                aLine = aLine.trim();
                if (aLine.length() == 16) {
                    continue;
                }

                dataArray = aLine.split("\\s+");
                dataList = new ArrayList<>();
                for (i = 0; i < dataArray.length; i++) {
                    dataList.add(dataArray[i]);
                }
                stName = dataList.get(0);
                stIdx = stNameList.indexOf(stName);
                stIdx1 = stList.indexOf(stName);
                if (stIdx >= 0 && stIdx1 < 0) {
                    stList.add(stName);
                    dataList.add(0, stPosList.get(stIdx)[0]);
                    dataList.add(0, stPosList.get(stIdx)[1]);
                    disDataList.add(dataList);
                }
            }
            sr.close();

            this.stationNum = disDataList.size();
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
            Logger.getLogger(METARDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(METARDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (sr != null) {
                    sr.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(METARDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
    public Array read(String varName) {
        Variable var = this.getVariable(varName);
        int n = var.getDimNumber();
        int[] origin = new int[n];
        int[] size = new int[n];
        int[] stride = new int[n];
        for (int i = 0; i < n; i++) {
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
        int varIdx = this.getVariableIndex(varName);
        StationData stationData = new StationData();
        List<String> stations = new ArrayList<>();
        String aStid;
        int i;
        float lon, lat;
        double t;
        t = 0;
        String dataStr;
        List<String> dataList;
        List<double[]> discreteData = new ArrayList<>();
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;

        for (i = 0; i < DataList.size(); i++) {
            dataList = DataList.get(i);
            aStid = dataList.get(2);
            lon = Float.parseFloat(dataList.get(0));
            lat = Float.parseFloat(dataList.get(1));
            double[] disData = new double[3];
            disData[0] = lon;
            disData[1] = lat;
            disData[2] = -9999;
            stations.add(aStid);

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

            int nVIdx = varIdx + 4;
            if (dataList.get(4).toUpperCase().equals("AUTO")) {
                nVIdx += 1;
            }
            if (varIdx >= 1) //Wind speed is in same string with wind direction
            {
                nVIdx -= 1;
            }
            if (varIdx >= 2) //Skip wind direction range
            {
                if (dataList.get(nVIdx - (varIdx - 2)).contains("V")) {
                    nVIdx += 1;
                }
                dataStr = dataList.get(nVIdx - (varIdx - 2));
                //If no visibility data
                if (!MIMath.isNumeric(dataStr)
                        && (dataStr.length()) < 3 || (dataStr.length()) >= 3
                        && !dataStr.substring(dataStr.length() - 2).equals("SM")
                        && !dataStr.substring(dataStr.length() - 3).equals("NDV")) {
                    if (varIdx == 2) {
                        continue;
                    }
                    nVIdx -= 1;
                }
            }
            if (varIdx >= 3) //Skip runway visual range
            {
                if (dataList.size() <= nVIdx) {
                    continue;
                }
                while (true) {
                    if (dataList.get(nVIdx - (varIdx - 3)).substring(0, 1).equals("R")
                            && dataList.get(nVIdx - (varIdx - 3)).contains("/")) {
                        nVIdx += 1;
                    } else {
                        break;
                    }
                }
                //If no weather data
                dataStr = dataList.get(nVIdx - (varIdx - 3));
                if (!dataStr.substring(0, 1).equals("+") && !dataStr.substring(0, 1).equals("-")
                        && !dataStr.substring(0, 2).equals("VC") && dataStr.length() != 2
                        && dataStr.length() != 4) {
                    if (varIdx == 3) {
                        continue;
                    }
                    nVIdx -= 1;
                }
            }
            if (varIdx >= 4) //Skip second weather
            {
                if (dataList.get(nVIdx - (varIdx - 4)).length() == 2) {
                    nVIdx += 1;
                }
            }
            if (varIdx >= 5) //Skip other cloud
            {
                while (true) {
                    dataStr = dataList.get(nVIdx - (varIdx - 5));
                    if ((dataStr.length() == 6
                            && MIMath.isNumeric(dataStr.substring(dataStr.length() - 3)))
                            || (dataStr.length() == 9 && dataStr.substring(dataStr.length() - 3).equals("///"))
                            || dataStr.substring(0, 1).equals("/")) {
                        nVIdx += 1;
                    } else {
                        break;
                    }
                }
            }
            if (varIdx >= 6) //Dew point is in same string with temprature
            {
                nVIdx -= 1;
            }
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            switch (varIdx) {
                case 0:    //WindDirection
                    if (dataStr.length() >= 7) {
                        if (MIMath.isNumeric(dataStr.substring(0, 3))) {
                            t = Double.parseDouble(dataStr.substring(0, 3));
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    break;
                case 1:    //WindSpeed                        
                    if (dataStr.length() >= 7) {
                        if (MIMath.isNumeric(dataStr.substring(3, 5))) {
                            t = Double.parseDouble(dataStr.substring(3, 5));
                            if (dataStr.substring(5, 7).toUpperCase().equals("KT")) {
                                t = t * 0.51444;    //Convert KT to MPS
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    break;
                case 2:    //Visibility                        
                    if (MIMath.isNumeric(dataStr)) //Unit: m
                    {
                        t = Double.parseDouble(dataStr);
                    } else if (dataStr.length() >= 3) {
                        if (dataStr.substring(dataStr.length() - 2).toUpperCase().equals("SM")) {
                            dataStr = dataStr.substring(0, dataStr.length() - 2);
                            if (dataStr.contains("/")) {
                                if (dataStr.substring(0, 1).toUpperCase().equals("M")) {
                                    dataStr = dataStr.substring(1);
                                }
                                t = Integer.parseInt(dataStr.substring(0, dataStr.indexOf("/")))
                                        / Integer.parseInt(dataStr.substring(dataStr.indexOf("/") + 1));
                            } else {
                                t = Double.parseDouble(dataStr);
                            }
                            t = t * 1603.9;    //statute miles to meters
                        } else if (dataStr.substring(dataStr.length() - 3, 3).toUpperCase().equals("NDV")) {
                            dataStr = dataStr.substring(0, dataStr.length() - 3);
                            t = Double.parseDouble(dataStr);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    break;
                case 3:    //Weather
                    int wIdx;
                    switch (dataStr.length()) {
                        case 2:
                            wIdx = getWeatherIndex(dataStr, "", "");
                            if (wIdx >= 0) {
                                t = wIdx;
                            } else {
                                continue;
                            }
                            break;
                        case 3:
                            if (dataStr.substring(0, 1).equals("+") || dataStr.substring(0, 1).equals("-")) {
                                wIdx = getWeatherIndex(dataStr.substring(1, 3), dataStr.substring(0, 1), "");
                                if (wIdx >= 0) {
                                    t = wIdx;
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                            break;
                        case 4:
                            wIdx = getWeatherIndex(dataStr.substring(2, 4), "", dataStr.substring(0, 2));
                            if (wIdx >= 0) {
                                t = wIdx;
                            } else {
                                continue;
                            }
                            break;
                        case 5:
                            if (dataStr.substring(0, 1).equals("+") || dataStr.substring(0, 1).equals("-")) {
                                wIdx = getWeatherIndex(dataStr.substring(3, 5), dataStr.substring(0, 1),
                                        dataStr.substring(1, 3));
                                if (wIdx >= 0) {
                                    t = wIdx;
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                            break;
                        default:
                            continue;
                    }
                    break;
                case 4:    //Cloud
                    int cCover;
                    if (dataStr.length() >= 2) {
                        if (dataStr.substring(0, 2).equals("VV")) {
                            t = 9;
                        } else if (dataStr.equals("CAVOK")) {
                            t = 0;
                        } else if (dataStr.length() >= 3) {
                            dataStr = dataStr.substring(0, 3);
                            cCover = getCloudCover(dataStr);
                            if (cCover >= 0) {
                                t = cCover;
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    break;
                case 5:    //Temeprature
                    if (dataStr.contains("/")) {
                        dataStr = dataStr.substring(0, dataStr.indexOf("/"));
                        if (dataStr.length() == 0) {
                            continue;
                        }
                        if (dataStr.substring(0, 1).equals("M")) {
                            dataStr = dataStr.replace("M", "-");
                        }
                        if (MIMath.isNumeric(dataStr)) {
                            t = Double.parseDouble(dataStr);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    break;
                case 6:    //Dew point
                    if (dataStr.contains("/")) {
                        dataStr = dataStr.substring(dataStr.indexOf("/") + 1);
                        if (dataStr.length() == 0) {
                            continue;
                        }
                        if (dataStr.substring(0, 1).equals("M")) {
                            dataStr = dataStr.replace("M", "-");
                        }
                        if (MIMath.isNumeric(dataStr)) {
                            t = Double.parseDouble(dataStr);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    break;
                case 7:    //Altimeter
                    String altType = dataStr.substring(0, 1);
                    if (dataStr.length() > 1 && (altType.equals("A") || altType.equals("Q"))) {
                        dataStr = dataStr.substring(1);
                        if (MIMath.isNumeric(dataStr)) {
                            t = Double.parseDouble(dataStr);
                            if (altType.equals("A")) {
                                t = t * 33.863 / 100;
                            }
                            if (t < 10) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    break;
            }

            disData[2] = t;
            discreteData.add(disData);
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

    private int getWeatherIndex(String wStr, String intensity, String descriptor) {
        int wIdx = -1;
        switch (wStr.toUpperCase()) {
            //Precipitation
            case "DZ":    //Drizzle
                switch (intensity) {
                    case "+":
                        switch (descriptor) {
                            case "FZ":    //Freezing
                                wIdx = 57;
                                break;
                            default:
                                wIdx = 55;
                                break;
                        }
                        break;
                    case "-":
                        switch (descriptor) {
                            case "FZ":    //Freezing
                                wIdx = 56;
                                break;
                            default:
                                wIdx = 51;
                                break;
                        }
                        break;
                    default:
                        switch (descriptor) {
                            case "FZ":    //Freezing
                                wIdx = 57;
                                break;
                            default:
                                wIdx = 53;
                                break;
                        }
                        break;
                }
                break;
            case "RA":    //Rain
                switch (intensity) {
                    case "+":
                        switch (descriptor) {
                            case "FZ":    //Freezing
                                wIdx = 67;
                                break;
                            case "TS":    //Thunderstorm
                                wIdx = 92;
                                break;
                            case "SH":    //Shower
                                wIdx = 81;
                                break;
                            default:
                                wIdx = 65;
                                break;
                        }
                        break;
                    case "-":
                        switch (descriptor) {
                            case "FZ":    //Freezing
                                wIdx = 66;
                                break;
                            case "TS":    //Thunderstorm
                                wIdx = 91;
                                break;
                            case "SH":    //Shower
                                wIdx = 80;
                                break;
                            default:
                                wIdx = 61;
                                break;
                        }
                        break;
                    default:
                        switch (descriptor) {
                            case "FZ":    //Freezing
                                wIdx = 67;
                                break;
                            case "TS":    //Thunderstorm
                                wIdx = 92;
                                break;
                            case "SH":    //Shower
                                wIdx = 81;
                                break;
                            default:
                                wIdx = 63;
                                break;
                        }
                        break;
                }
                break;
            case "SN":    //Snow
                switch (intensity) {
                    case "+":
                        switch (descriptor) {
                            case "TS":    //Thunderstorm
                                wIdx = 94;
                                break;
                            case "SH":    //Shower
                                wIdx = 86;
                                break;
                            default:
                                wIdx = 75;
                                break;
                        }
                        break;
                    case "-":
                        switch (descriptor) {
                            case "TS":    //Thunderstorm
                                wIdx = 93;
                                break;
                            case "SH":    //Shower
                                wIdx = 85;
                                break;
                            default:
                                wIdx = 71;
                                break;
                        }
                        break;
                    default:
                        switch (descriptor) {
                            case "TS":    //Thunderstorm
                                wIdx = 94;
                                break;
                            case "SH":    //Shower
                                wIdx = 86;
                                break;
                            default:
                                wIdx = 73;
                                break;
                        }
                        break;
                }
                break;
            case "SG":    //Snow grains
                wIdx = 77;
                break;
            case "IC":    //Ice crystals
                wIdx = 76;
                break;
            case "PE":    //Ice pellets
                switch (descriptor) {
                    case "SH":    //Shower
                        if (intensity.equals("-")) {
                            wIdx = 87;
                        } else {
                            wIdx = 88;
                        }
                        break;
                    default:
                        wIdx = 79;
                        break;
                }
                break;
            case "GR":    //Hail
                switch (descriptor) {
                    case "TS":    //Thunderstorm
                        if (intensity.equals("+")) {
                            wIdx = 99;
                        } else {
                            wIdx = 96;
                        }
                        break;
                    default:
                        if (intensity.equals("-")) {
                            wIdx = 89;
                        } else {
                            wIdx = 90;
                        }
                        break;
                }
                break;
            case "GS":    //Small hail / snow pellets
                wIdx = 89;
                break;
            //case "UP":    //Unknow
            //wIdx = 89;
            //break;

            //Obscuration
            case "BR":    //Mist
                wIdx = 10;
                break;
            case "FG":    //Fog
                wIdx = 45;
                break;
            case "FU":    //Smoke
                wIdx = 4;
                break;
            //case "VA":    //Volcanic ash
            //    wIdx = 0;
            //    break;
            case "DU":    //Dust
                wIdx = 6;
                break;
            case "SA":    //Sand
                wIdx = 7;
                break;
            case "HZ":    //Haze
                wIdx = 5;
                break;
            //case "PY":    //Spray
            //    wIdx = 0;
            //    break;

            //Misc
            case "PO":    //Dust whirls
                wIdx = 8;
                break;
            case "SQ":    //Squalls
                wIdx = 39;
                break;
            case "FC":    //funnel cloud/tornado/waterspout
                wIdx = 19;
                break;
            case "SS":    //Dust storm
                switch (intensity) {
                    case "+":
                        wIdx = 34;
                        break;
                    case "-":
                        wIdx = 31;
                        break;
                    default:
                        wIdx = 31;
                        break;
                }
                break;
        }

        return wIdx;
    }

    private int getCloudCover(String cStr) {
        int cCover = - 1;
        switch (cStr) {
            case "CLR":
            case "SKC":
                cCover = 0;
                break;
            case "FEW":
                cCover = 1;
                break;
            case "SCT":
                cCover = 3;
                break;
            case "BKN":
                cCover = 6;
                break;
            case "OVC":
                cCover = 8;
                break;
        }

        return cCover;
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
        double t;
        String dataStr;
        List<String> dataList;
        List<StationModel> smList = new ArrayList<>();
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;

        for (i = 0; i < DataList.size(); i++) {
            dataList = DataList.get(i);
            aStid = dataList.get(2);
            lon = Float.parseFloat(dataList.get(0));
            lat = Float.parseFloat(dataList.get(1));
            if (lon < 0) {
                lon += 360;
            }

            StationModel sm = new StationModel();

            //Initialize data
            sm.setLongitude(lon);
            sm.setLatitude(lat);

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

            //WindDirection
            int nVIdx = 4;    //Wind group
            if (dataList.get(4).toUpperCase().equals("AUTO")) //Skip AUTO
            {
                nVIdx += 1;
            }
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            if (dataStr.length() >= 7) {
                if (MIMath.isNumeric(dataStr.substring(0, 3))) {
                    t = Double.parseDouble(dataStr.substring(0, 3));
                    sm.setWindDirection(t);
                }
            }

            //WindSpeed                        
            if (dataStr.length() >= 7) {
                if (MIMath.isNumeric(dataStr.substring(3, 5))) {
                    t = Double.parseDouble(dataStr.substring(3, 5));
                    if (dataStr.substring(5, 7).toUpperCase().equals("KT")) {
                        t = t * 0.51444;    //Convert KT to MPS
                    }
                    sm.setWindSpeed(t);
                }
            }

            //Visibility         
            nVIdx += 1;
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            //Skip wind direction range
            if (dataStr.contains("V")) {
                nVIdx += 1;
            }
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            if (MIMath.isNumeric(dataStr)) //Unit: m
            {
                t = Double.parseDouble(dataStr);
                sm.setVisibility(t);
            } else if (dataStr.length() >= 3) {
                if (dataStr.substring(dataStr.length() - 2).toUpperCase().equals("SM")) {
                    dataStr = dataStr.substring(0, dataStr.length() - 2);
                    if (dataStr.contains("/")) {
                        if (dataStr.substring(0, 1).toUpperCase().equals("M")) {
                            dataStr = dataStr.substring(1);
                        }
                        t = Integer.parseInt(dataStr.substring(0, dataStr.indexOf("/")))
                                / Integer.parseInt(dataStr.substring(dataStr.indexOf("/") + 1));
                    } else {
                        t = Double.parseDouble(dataStr);
                    }
                    t = t * 1603.9;    //statute miles to meters
                    sm.setVisibility(t);
                } else if (dataStr.substring(dataStr.length() - 3).toUpperCase().equals("NDV")) {
                    dataStr = dataStr.substring(0, dataStr.length() - 3);
                    t = Double.parseDouble(dataStr);
                    sm.setVisibility(t);
                } else {
                    nVIdx -= 1;    //No visibility data
                }
            } else {
                nVIdx -= 1;    //No visibility data
            }

            //Weather
            nVIdx += 1;
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            //Skip runway visual range                
            while (true) {
                if (dataStr.substring(0, 1).equals("R")
                        && dataStr.contains("/")) {
                    nVIdx += 1;
                    if (nVIdx >= dataList.size()) {
                        continue;
                    }
                    dataStr = dataList.get(nVIdx);
                } else {
                    break;
                }
            }
            dataStr = dataList.get(nVIdx);
            int wIdx;
            switch (dataStr.length()) {
                case 2:
                    wIdx = getWeatherIndex(dataStr, "", "");
                    if (wIdx >= 0) {
                        t = wIdx;
                        sm.setWeather(t);
                    } else {
                        nVIdx -= 1;    //No weather data
                    }
                    break;
                case 3:
                    if (dataStr.substring(0, 1).equals("+") || dataStr.substring(0, 1).equals("-")) {
                        wIdx = getWeatherIndex(dataStr.substring(1, 3), dataStr.substring(0, 1), "");
                        if (wIdx >= 0) {
                            t = wIdx;
                            sm.setWeather(t);
                        } else {
                            nVIdx -= 1;    //No weather data
                        }
                    } else {
                        nVIdx -= 1;    //No weather data
                    }
                    break;
                case 4:
                    wIdx = getWeatherIndex(dataStr.substring(2, 4), "", dataStr.substring(0, 2));
                    if (wIdx >= 0) {
                        t = wIdx;
                        sm.setWeather(t);
                    } else {
                        nVIdx -= 1;    //No weather data
                    }
                    break;
                case 5:
                    if (dataStr.substring(0, 1).equals("+") || dataStr.substring(0, 1).equals("-")) {
                        wIdx = getWeatherIndex(dataStr.substring(3, 5), dataStr.substring(0, 1),
                                dataStr.substring(1, 3));
                        if (wIdx >= 0) {
                            t = wIdx;
                            sm.setWeather(t);
                        } else {
                            nVIdx -= 1;    //No weather data
                        }
                    } else {
                        nVIdx -= 1;    //No weather data
                    }
                    break;
                default:
                    nVIdx -= 1;    //No weather data
                    break;
            }

            //Cloud
            nVIdx += 1;
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            //Skip second weather
            if (dataStr.length() == 2) {
                nVIdx += 1;
            }
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            int cCover;
            if (dataStr.length() >= 2) {
                if (dataStr.substring(0, 2).equals("VV")) {
                    t = 9;
                    sm.setCloudCover(t);
                } else if (dataStr.equals("CAVOK")) {
                    t = 0;
                    sm.setCloudCover(t);
                } else if (dataStr.length() >= 3) {
                    dataStr = dataStr.substring(0, 3);
                    cCover = getCloudCover(dataStr);
                    if (cCover >= 0) {
                        t = cCover;
                        sm.setCloudCover(t);
                    }
                }
            }

            //Temperature
            nVIdx += 1;
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            //Skip other cloud
            while (true) {
                if ((dataStr.length() == 6
                        && MIMath.isNumeric(dataStr.substring(dataStr.length() - 3)))
                        || (dataStr.length() == 9 && dataStr.substring(dataStr.length() - 3).equals("///"))
                        || dataStr.substring(0, 1).equals("/")) {
                    nVIdx += 1;
                    if (nVIdx >= dataList.size()) {
                        continue;
                    }
                    dataStr = dataList.get(nVIdx);
                } else {
                    break;
                }
            }
            dataStr = dataList.get(nVIdx);
            if (dataStr.contains("/")) {
                dataStr = dataStr.substring(0, dataStr.indexOf("/"));
                if (dataStr.length() > 0) {
                    if (dataStr.substring(0, 1).equals("M")) {
                        dataStr = dataStr.replace("M", "-");
                    }
                    if (MIMath.isNumeric(dataStr)) {
                        t = Double.parseDouble(dataStr);
                        sm.setTemperature(t);
                    }
                }
            }

            //Dew point
            dataStr = dataList.get(nVIdx);
            if (dataStr.contains("/")) {
                dataStr = dataStr.substring(dataStr.indexOf("/") + 1);
                if (dataStr.length() > 0) {
                    if (dataStr.substring(0, 1).equals("M")) {
                        dataStr = dataStr.replace("M", "-");
                    }
                    if (MIMath.isNumeric(dataStr)) {
                        t = Double.parseDouble(dataStr);
                        sm.setDewPoint(t);
                    }
                }
            }

            //Altimeter
            nVIdx += 1;
            if (nVIdx >= dataList.size()) {
                continue;
            }
            dataStr = dataList.get(nVIdx);
            String altType = dataStr.substring(0, 1);
            if (dataStr.length() > 1 && (altType.equals("A") || altType.equals("Q"))) {
                dataStr = dataStr.substring(1);
                if (MIMath.isNumeric(dataStr)) {
                    t = Double.parseDouble(dataStr);
                    if (altType.equals("A")) {
                        t = t * 33.863 / 100;
                    }
                    if (t > 10) {
                        sm.setPressure(t);
                    }
                }
            }

            smList.add(sm);
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
