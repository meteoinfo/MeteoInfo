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
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.meteoinfo.global.Extent;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.data.meteodata.Attribute;

/**
 *
 * @author yaqiang
 */
public class MICAPS120DataInfo extends DataInfo implements IStationDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private List<String> _varList = new ArrayList<>();
    private final List<String> _fieldList = new ArrayList<>();
    private final List<List<String>> _dataList = new ArrayList<>();
    private int stNum;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MICAPS120DataInfo() {
        this.setMissingValue(9999.0);
        this.setDataType(MeteoDataType.MICAPS_120);
        String[] items = new String[]{"AQI", "Grade", "PM2.5", "PM10", "CO", "NO2", "O3", "O3_8h", "SO2"};
        _varList = Arrays.asList(items);
        _fieldList.addAll(Arrays.asList(new String[]{"Stid", "Longitude", "Latitude"}));
        _fieldList.addAll(_varList);
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
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

            //Read file head
            String aLine = sr.readLine().trim();
            _description = aLine;
            String dateStr = this._description.split("\\s+")[2];
            dateStr = dateStr.substring(dateStr.length() - 10);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHH");
            LocalDateTime time = LocalDateTime.parse(dateStr, format);

            //Set dimension and variables
            Dimension tdim = new Dimension(DimensionType.T);
            double[] values = new double[1];
            values[0] = JDateUtil.toOADate(time);
            tdim.setValues(values);
            this.setTimeDimension(tdim);
            
            String[] dataArray;
            String line;
            List<String> aList;
            stNum = 0;
            while((line = sr.readLine()) != null){
                line = line.trim();
                dataArray = line.split("\\s+");
                if (dataArray.length < 12){
                    continue;
                }
                aList = new ArrayList<>();
                aList.add(dataArray[0]);
                aList.add(dataArray[2]);
                aList.add(dataArray[1]);
                for (int i = 3; i < dataArray.length; i++){
                    aList.add(dataArray[i]);
                }               
                _dataList.add(aList);
                stNum += 1;
            }
            
            Dimension stdim = new Dimension(DimensionType.Other);
            stdim.setShortName("station");
            values = new double[stNum];
            for (int i = 0; i < stNum; i++){
                values[i] = i;
            }
            stdim.setValues(values);
            this.addDimension(stdim);
            List<Variable> variables = new ArrayList<>();
            for (String vName : this._fieldList) {
                Variable var = new Variable();
                var.setName(vName);
                switch(vName){
                    case "Stid":
                        var.setDataType(DataType.STRING);
                        break;
                }
                var.setStation(true);
                var.setDimension(stdim);
                var.setFillValue(this.getMissingValue());
                variables.add(var);
            }
            this.setVariables(variables);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (sr != null)
                    sr.close();
            } catch (IOException ex) {
                Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        int varIdx = this._fieldList.indexOf(varName);
        if (varIdx < 0)
            return null;
        
        DataType dt = DataType.FLOAT;
        switch (varName){
            case "Stid":
                dt = DataType.STRING;
                break;
            case "Grade":
                dt = DataType.INT;
                break;
        }
        int[] shape = new int[1];
        shape[0] = this.stNum;
        Array r = Array.factory(dt, shape);
        int i;
        float v;       
        List<String> dataList;

        for (i = 0; i < _dataList.size(); i++) {
            dataList = _dataList.get(i);
            if (varIdx < dataList.size()){
                switch (dt) {
                    case STRING:
                        r.setObject(i, dataList.get(varIdx));
                        break;
                    case INT:
                        int vi = Integer.parseInt(dataList.get(varIdx));
                        r.setInt(i, vi);
                        break;
                    case FLOAT:
                        v = Float.parseFloat(dataList.get(varIdx));
                        r.setFloat(i, v);
                        break;
                }
            } else {
                switch (dt) {
                    case STRING:
                        r.setObject(i, "Null");
                        break;
                    case INT:
                        r.setInt(i, Integer.MIN_VALUE);
                        break;
                    case FLOAT:
                        r.setFloat(i, Float.NaN);
                        break;
                }
            }
        }
        
        return r;
    }
    
    /**
     * Read data frame
     *
     * @return Data frame
     */
    public DataFrame readDataFrame() {
        List<Array> data = new ArrayList<>();
        ColumnIndex columns = new ColumnIndex();
        DataType dtype;
        for (String vName : this._fieldList) {
            switch (vName) {
                case "Stid":
                    continue;
                case "AQI":
                case "Grade":
                    dtype = DataType.INT;
                    break;
                default:
                    dtype = DataType.FLOAT;
                    break;
            }
            columns.add(new Column(vName, dtype));
            data.add(Array.factory(dtype, new int[]{this._dataList.size()}));
        }
        List<String> idxList = new ArrayList<>();
        Array dd;
        float v;
        for (int i = 0; i < this._dataList.size(); i++) {
            List<String> dataList = this._dataList.get(i);
            idxList.add(dataList.get(0));
            for (int j = 0; j < data.size(); j++) {
                dd = (Array) data.get(j);
                switch (dd.getDataType()) {
                    case INT:
                        dd.setObject(i, Integer.parseInt(dataList.get(j + 1)));
                        break;
                    case FLOAT:
                        v = Float.parseFloat(dataList.get(j + 1));
                        dd.setObject(i, v);
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

    @Override
    public StationData getStationData(int timeIdx, String varName, int levelIdx) {
        int varIdx = this.getVariableIndex(varName);
        String aStid;
        int i;
        float lon, lat, t;
        List<String> dataList;
        List<double[]> disData = new ArrayList<>();
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        List<String> stations = new ArrayList<>();

        for (i = 0; i < _dataList.size(); i++) {
            dataList = _dataList.get(i);
            if (varIdx >= dataList.size())
                continue;
            
            aStid = dataList.get(0);
            lon = Float.parseFloat(dataList.get(1));
            lat = Float.parseFloat(dataList.get(2));
            t = Float.parseFloat(dataList.get(varIdx));
            stations.add(aStid);
            disData.add(new double[]{lon, lat, t});
            //discreteData[i][0] = lon;
            //discreteData[i][1] = lat;
            //discreteData[i][2] = t;

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
        double[][] discreteData = new double[disData.size()][3];
        for (i = 0; i < disData.size(); i++){
            discreteData[i][0] = disData.get(i)[0];
            discreteData[i][1] = disData.get(i)[1];
            discreteData[i][2] = disData.get(i)[2];
        }
        stData.data = discreteData;
        stData.stations = stations;
        stData.dataExtent = dataExtent;
        stData.missingValue = this.getMissingValue();

        return stData;
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        StationInfoData stInfoData = new StationInfoData();
        stInfoData.setDataList(_dataList);
        stInfoData.setFields(_fieldList);
        stInfoData.setVariables(_varList);

        List<String> stations = new ArrayList<>();
        stNum = _dataList.size();
        for (int i = 0; i < stNum; i++) {
            stations.add(_dataList.get(i).get(0));
        }
        stInfoData.setStations(stations);

        return stInfoData;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
