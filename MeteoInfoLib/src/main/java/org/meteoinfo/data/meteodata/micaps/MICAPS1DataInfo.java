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
import org.meteoinfo.math.ArrayMath;
import org.meteoinfo.data.dataframe.Column;
import org.meteoinfo.data.dataframe.ColumnIndex;
import org.meteoinfo.data.dataframe.DataFrame;
import org.meteoinfo.data.dataframe.Index;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.data.meteodata.Attribute;

/**
 *
 * @author yaqiang
 */
public class MICAPS1DataInfo extends DataInfo implements IStationDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private boolean _isAutoStation = false;
    private List<String> _varList = new ArrayList<>();
    private int _stNum;
    private boolean _hasAllCols = false;
    private int _varNum;
    //private final List<List<String>> _dataList = new ArrayList<>();
    private final List<String> _fieldList = new ArrayList<>();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MICAPS1DataInfo() {
        String[] items = new String[]{"Altitude", "Grade", "CloudCover", "WindDirection", "WindSpeed", "Pressure",
            "PressVar3h", "WeatherPast1", "WeatherPast2", "Precipitation6h", "LowCloudShape",
            "LowCloudAmount", "LowCloudHeight", "DewPoint", "Visibility", "WeatherNow",
            "Temperature", "MiddleCloudShape", "HighCloudShape"};
        _varList = Arrays.asList(items);
        _fieldList.addAll(Arrays.asList(new String[]{"Stid", "Longitude", "Latitude"}));
        _fieldList.addAll(_varList);
        this.setMissingValue(9999.0);
        this.setDataType(MeteoDataType.MICAPS_1);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        BufferedReader sr = null;
        try {
            this.setFileName(fileName);
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "gbk"));
            String aLine;
            String[] dataArray;
            List<String> aList;
            int n, dataNum;

            this.addAttribute(new Attribute("data_format", "MICAPS 1"));
            //Read file head            
            aLine = sr.readLine().trim();
            _description = aLine;
            if (aLine.contains("自动")) {
                _isAutoStation = true;
            }
            aLine = sr.readLine().trim();
            if (aLine.isEmpty()) {
                aLine = sr.readLine();
            }
            dataArray = aLine.split("\\s+");
            int year = Integer.parseInt(dataArray[0]);
            if (year < 100) {
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
            }
            LocalDateTime time = LocalDateTime.of(year, Integer.parseInt(dataArray[1]),
                    Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);
            Dimension tdim = new Dimension(DimensionType.T);
            double[] values = new double[1];
            values[0] = JDateUtil.toOADate(time);
            tdim.setValues(values);
            this.setTimeDimension(tdim);

            if (dataArray.length >= 5)
                _stNum = Integer.parseInt(dataArray[4]);
            else {
                aLine = sr.readLine().trim();
                if (aLine.isEmpty()) {
                    aLine = sr.readLine();
                }
                dataArray = aLine.split("\\s+");
                if (dataArray.length == 1) {
                    _stNum = Integer.parseInt(dataArray[0]);
                }
            }
            Dimension stdim = new Dimension(DimensionType.Other);
            stdim.setShortName("station");
            values = new double[_stNum];
            for (int i = 0; i < _stNum; i++) {
                values[i] = i;
            }
            stdim.setValues(values);
            this.addDimension(stdim);
            List<Variable> variables = new ArrayList<>();
            for (String vName : _fieldList) {
                Variable var = new Variable();
                var.setName(vName);
                DataType dt = DataType.FLOAT;
                switch (vName) {
                    case "Stid":
                        dt = DataType.STRING;
                        break;
                    case "Grade":
                    case "CloudCover":
                    case "WeatherPast1":
                    case "WeatherPast2":
                    case "WeatherNow":
                    case "MiddleCloudShape":
                    case "HighCloudShape":
                        dt = DataType.INT;
                        break;
                }
                var.setDataType(dt);
                var.setStation(true);
                //var.setDimension(tdim);
                var.setDimension(stdim);
                var.setFillValue(this.getMissingValue());
                variables.add(var);
            }
            this.setVariables(variables);

            //Read data
            dataNum = 0;
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                aLine = aLine.trim();
                if (aLine.isEmpty()) {
                    continue;
                }

                dataArray = aLine.split("\\s+");
                aList = new ArrayList<>();
                aList.addAll(Arrays.asList(dataArray));
                for (n = 0; n <= 10; n++) {
                    if (aList.size() < 24) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.split("\\s+");
                        for (String str : dataArray) {
                            if (!str.isEmpty()) {
                                aList.add(str);
                            }
                        }
                    } else {
                        break;
                    }
                }

                if (aList.size() < 24) {
                    break;
                } else {
                    for (n = 0; n < 10; n++) {
                        aList.remove(aList.size() - 1);
                        if (aList.size() == 22) {
                            break;
                        }
                    }
                }

                if (dataNum == 0) {
                    _hasAllCols = dataArray.length == 26;
                }

                dataNum++;
                if (dataNum == 1) {
                    _varNum = aList.size();
                    break;
                }
                //_dataList.add(aList);
            } while (aLine != null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (sr != null) {
                    sr.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private List<List<String>> readData() {
        BufferedReader sr = null;
        try {
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(this.getFileName()), "gbk"));
            String aLine;
            String[] dataArray;
            List<String> aList;
            int n;
            List<List<String>> dataList = new ArrayList<>();

            //Read file head            
            sr.readLine();
            aLine = sr.readLine().trim();
            if (aLine.isEmpty()) {
                sr.readLine();
            }

            //Read data
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                aLine = aLine.trim();
                if (aLine.isEmpty()) {
                    continue;
                }

                dataArray = aLine.split("\\s+");
                aList = new ArrayList<>();
                aList.addAll(Arrays.asList(dataArray));
                for (n = 0; n <= 10; n++) {
                    if (aList.size() < 24) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.split("\\s+");
                        for (String str : dataArray) {
                            if (!str.isEmpty()) {
                                aList.add(str);
                            }
                        }
                    } else {
                        break;
                    }
                }

                if (aList.size() < 24) {
                    break;
                } else {
                    for (n = 0; n < 10; n++) {
                        aList.remove(aList.size() - 1);
                        if (aList.size() == 22) {
                            break;
                        }
                    }
                }
                dataList.add(aList);
            } while (aLine != null);
            return dataList;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (sr != null) {
                    sr.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        String dataInfo;
        dataInfo = "Description: " + _description;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
        dataInfo += System.getProperty("line.separator") + "Time: " + format.format(this.getTimes().get(0));
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
        int varIdx = this._fieldList.indexOf(varName);
        if (varIdx < 0) {
            return null;
        }

        DataType dt = DataType.FLOAT;
        switch (varName) {
            case "Stid":
                dt = DataType.STRING;
                break;
            case "Grade":
            case "CloudCover":
            case "WeatherPast1":
            case "WeatherPast2":
            case "WeatherNow":
            case "MiddleCloudShape":
            case "HighCloudShape":
                dt = DataType.INT;
                break;
        }
        int[] shape = new int[1];
        shape[0] = this._stNum;
        Array r = Array.factory(dt, shape);
        int i;
        float v;
        List<String> dataList;
        List<List<String>> allDataList = this.readData();

        for (i = 0; i < allDataList.size(); i++) {
            dataList = allDataList.get(i);
            switch (dt) {
                case STRING:
                    r.setObject(i, dataList.get(varIdx));
                    break;
                case INT:
                    int vi = Integer.parseInt(dataList.get(varIdx));
                    r.setInt(i, vi);
                    break;
                case FLOAT:
                    try {
                        v = Float.parseFloat(dataList.get(varIdx));
                        if (varIdx == 8) //Pressure
                        {
                            if (!MIMath.doubleEquals(v, this.getMissingValue())) {
                                if (v > 800) {
                                    v = v / 10 + 900;
                                } else {
                                    v = v / 10 + 1000;
                                }
                            }
                        }
                        r.setFloat(i, v);
                    } catch (Exception e) {
                        r.setFloat(i, Float.NaN);
                    }
                    break;
            }
        }
        try {
            r = r.section(origin, shape, stride);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return r;
    }

    /**
     * Read data frame
     *
     * @return Data frame
     */
    public DataFrame readDataFrame() {
        List<List<String>> allDataList = this.readData();
        List<Array> data = new ArrayList<>();
        ColumnIndex columns = new ColumnIndex();
        DataType dtype;
        for (String vName : this._fieldList) {
            switch (vName) {
                case "Stid":
                    continue;
                case "Grade":
                case "CloudCover":
                case "WeatherPast1":
                case "WeatherPast2":
                case "WeatherNow":
                case "MiddleCloudShape":
                case "HighCloudShape":
                    dtype = DataType.INT;
                    break;
                default:
                    dtype = DataType.FLOAT;
                    break;
            }
            columns.add(new Column(vName, dtype));
            data.add(Array.factory(dtype, new int[]{allDataList.size()}));
        }
        List<String> idxList = new ArrayList<>();
        Array dd;
        float v;
        for (int i = 0; i < allDataList.size(); i++) {
            List<String> dataList = allDataList.get(i);
            idxList.add(dataList.get(0));
            for (int j = 0; j < data.size(); j++) {
                dd = (Array) data.get(j);
                switch (dd.getDataType()) {
                    case INT:
                        dd.setInt(i, Integer.parseInt(dataList.get(j + 1)));
                        break;
                    case FLOAT:
                        try {
                            v = Float.parseFloat(dataList.get(j + 1));
                            if (j + 1 == 8) //Pressure
                            {
                                if (!MIMath.doubleEquals(v, this.getMissingValue())) {
                                    if (v > 800) {
                                        v = v / 10 + 900;
                                    } else {
                                        v = v / 10 + 1000;
                                    }
                                }
                            }
                            dd.setFloat(i, v);
                        } catch (Exception e) {
                            dd.setFloat(i, Float.NaN);
                        }
                        break;
                }
            }
        }
        
        for (Array a : data){
            ArrayMath.missingToNaN(a, 9999);
        }

        Index index = Index.factory(idxList);
        DataFrame df = new DataFrame(data, index, columns);
        return df;
    }

    /**
     * Read data table
     *
     * @return Data table
     */
    public DataTable readTable() {
        List<List<String>> allDataList = this.readData();
        DataTable dTable = new DataTable();
        DataType dtype;
        for (String vName : this._fieldList) {
            switch (vName) {
                case "Stid":
                    dtype = DataType.STRING;
                    break;
                case "Grade":
                case "CloudCover":
                case "WeatherPast1":
                case "WeatherPast2":
                case "WeatherNow":
                case "MiddleCloudShape":
                case "HighCloudShape":
                    dtype = DataType.INT;
                    break;
                default:
                    dtype = DataType.FLOAT;
                    break;
            }
            try {
                dTable.addColumn(vName, dtype);
            } catch (Exception ex) {
                Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int nCol = dTable.getColumnCount();
        for (int i = 0; i < allDataList.size(); i++) {
            List<String> dataList = allDataList.get(i);
            try {
                dTable.addRow();
                for (int j = 0; j < nCol; j++) {
                    dTable.setValue(i, j, dataList.get(j));
                }
            } catch (Exception ex) {
                Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return dTable;
    }

    @Override
    public StationData getStationData(int timeIdx, String varName, int levelIdx) {
        int varIdx = this.getVariableIndex(varName);

        List<List<String>> allDataList = this.readData();
        String aStid;
        int i;
        float lon, lat, t;
        List<String> dataList;
        double[][] discreteData = new double[allDataList.size()][3];
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        List<String> stations = new ArrayList<>();

        for (i = 0; i < allDataList.size(); i++) {
            dataList = allDataList.get(i);
            aStid = dataList.get(0);
            lon = Float.parseFloat(dataList.get(1));
            lat = Float.parseFloat(dataList.get(2));
            try {
                t = Float.parseFloat(dataList.get(varIdx));

                if (varIdx == 8) //Pressure
                {
                    if (!MIMath.doubleEquals(t, this.getMissingValue())) {
                        if (t > 800) {
                            t = t / 10 + 900;
                        } else {
                            t = t / 10 + 1000;
                        }
                    }
                }
            } catch (Exception e) {
                t = Float.NaN;
            }

            stations.add(aStid);
            discreteData[i][0] = lon;
            discreteData[i][1] = lat;
            discreteData[i][2] = t;

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

        StationData stData = new StationData();
        stData.data = discreteData;
        stData.stations = stations;
        stData.dataExtent = dataExtent;
        stData.missingValue = this.getMissingValue();

        return stData;
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        StationInfoData stInfoData = new StationInfoData();
        List<List<String>> allDataList = this.readData();
        stInfoData.setDataList(allDataList);
        stInfoData.setFields(_fieldList);
        stInfoData.setVariables(_varList);

        List<String> stations = new ArrayList<>();
        int stNum = allDataList.size();
        for (int i = 0; i < stNum; i++) {
            stations.add(allDataList.get(i).get(0));
        }
        stInfoData.setStations(stations);

        return stInfoData;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        StationModelData smData = new StationModelData();
        int i;
        float lon, lat;
        String aStid;
        List<String> dataList;
        List<StationModel> smList = new ArrayList<>();
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;

        List<List<String>> allDataList = this.readData();
        for (i = 0; i < allDataList.size(); i++) {
            dataList = allDataList.get(i);
            aStid = dataList.get(0);
            lon = Float.parseFloat(dataList.get(1));
            lat = Float.parseFloat(dataList.get(2));

            StationModel sm = new StationModel();
            sm.setStationIdentifer(aStid);
            sm.setLongitude(lon);
            sm.setLatitude(lat);
            sm.setWindDirection(Double.parseDouble(dataList.get(6)));    //Wind direction
            sm.setWindSpeed(Double.parseDouble(dataList.get(7)));    //Wind speed
            sm.setVisibility(Double.parseDouble(dataList.get(17)));    //Visibility
            sm.setWeather(Double.parseDouble(dataList.get(18)));    //Weather
            sm.setCloudCover(Double.parseDouble(dataList.get(5)));    //Cloud cover
            sm.setTemperature(Double.parseDouble(dataList.get(19)));    //Temperature
            sm.setDewPoint(Double.parseDouble(dataList.get(16)));    //Dew point
            //Pressure
            double press = Double.parseDouble(dataList.get(8));
            if (MIMath.doubleEquals(press, this.getMissingValue())) {
                sm.setPressure(press);
            } else if (press > 800) {
                sm.setPressure(press / 10 + 900);
            } else {
                sm.setPressure(press / 10 + 1000);
            }
            smList.add(sm);

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
