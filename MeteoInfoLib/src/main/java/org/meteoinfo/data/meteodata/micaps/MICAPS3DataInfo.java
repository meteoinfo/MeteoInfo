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
import org.meteoinfo.global.Extent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.ArrayString;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.data.meteodata.Attribute;

/**
 *
 * @author yaqiang
 */
public class MICAPS3DataInfo extends DataInfo implements IStationDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private List<String> _varList = new ArrayList<>();
    private List<String> _fieldList = new ArrayList<>();
    private List<List<String>> _dataList = new ArrayList<>();
    private int stNum;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MICAPS3DataInfo() {
        this.setMissingValue(9999.0);
        this.setDataType(MeteoDataType.MICAPS_3);
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
            int i;
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "gbk"));
            String[] dataArray;
            List<String> dataList = new ArrayList<>();

            this.addAttribute(new Attribute("data_format", "MICAPS 3"));
            //Read file head
            String aLine = sr.readLine().trim();
            _description = aLine;
            //Read all lines
            aLine = sr.readLine().trim();
            String bLine;
            while ((bLine = sr.readLine()) != null) {
                aLine = aLine + " " + bLine.trim();
            }
            sr.close();
            dataArray = aLine.split("\\s+");
            dataList.clear();
            for (i = 0; i < dataArray.length; i++) {
                if (!dataArray[i].isEmpty()) {
                    dataList.add(dataArray[i]);
                }
            }

            int year = Integer.parseInt(dataList.get(0));
            if (year < 100) {
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
            }
            LocalDateTime time = LocalDateTime.of(year, Integer.parseInt(dataList.get(1)), Integer.parseInt(dataList.get(2)),
                    Integer.parseInt(dataList.get(3)), 0, 0);
            int level = Integer.parseInt(dataList.get(4));
            int contourNum = Integer.parseInt(dataList.get(5));
            List<Float> contours = new ArrayList<>();
            for (i = 0; i < contourNum; i++) {
                contours.add(Float.parseFloat(dataList.get(6 + i)));
            }
            int idx = 6 + contourNum + 2;
            int pNum = Integer.parseInt(dataList.get(idx));
            idx += pNum * 2 + 1;
            int varNum = Integer.parseInt(dataList.get(idx));
            idx += 1;
            stNum = Integer.parseInt(dataList.get(idx));
            idx += 1;
            for (i = 0; i < varNum; i++) {
                _varList.add("Var" + String.valueOf(i + 1));
            }
            _fieldList.add("Stid");
            _fieldList.add("Longitude");
            _fieldList.add("Latitude");
            _fieldList.add("Altitude");
            _fieldList.addAll(_varList);
            while (idx + 3 + varNum < dataList.size()) {
                List<String> aData = new ArrayList<>();
                for (int j = 0; j < 4 + varNum; j++) {
                    aData.add(dataList.get(idx));
                    idx += 1;
                }
                _dataList.add(aData);
            }

            stNum = _dataList.size();
            Dimension stdim = new Dimension(DimensionType.Other);
            stdim.setShortName("station");
            double[] values = new double[stNum];
            for (i = 0; i < stNum; i++){
                values[i] = i;
            }
            stdim.setValues(values);
            this.addDimension(stdim);
            Dimension tdim = new Dimension(DimensionType.T);
            values = new double[1];
            values[0] = JDateUtil.toOADate(time);
            tdim.setValues(values);
            this.setTimeDimension(tdim);
            Dimension zdim = new Dimension(DimensionType.Z);
            zdim.setValues(new double[]{level});
            this.setZDimension(zdim);
            List<Variable> variables = new ArrayList<>();
            for (String vName : this._fieldList) {
                Variable var = new Variable();
                var.setName(vName);
                if (vName.equals("Stid"))
                    var.setDataType(DataType.STRING);
                var.setStation(true);
                //var.setDimension(tdim);
                //var.setDimension(zdim);
                var.setDimension(stdim);
                var.setFillValue(this.getMissingValue());
                variables.add(var);
            }
            this.setVariables(variables);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS3DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MICAPS3DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS3DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(MICAPS3DataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        if (varIdx < 0) {
            return null;
        }
        int[] shape = new int[1];
        shape[0] = this.stNum;
        Array r;
        DataType dt = DataType.FLOAT;
        if (varName.equals("Stid")){
            dt = DataType.STRING;
            r = new ArrayString(shape);            
        } else {
            r = Array.factory(dt, shape);
        }
        List<String> dataList;
        for (int i = 0; i < _dataList.size(); i++) {
            dataList = _dataList.get(i);
            switch (dt) {
                case STRING:
                    r.setObject(i, dataList.get(varIdx));
                    break;
                default:
                    r.setFloat(i, Float.parseFloat(dataList.get(varIdx)));
                    break;
            }
        }
        
        return r;
    }

    @Override
    public StationData getStationData(int timeIdx, String varName, int levelIdx) {
        int varIdx = this.getVariableIndex(varName);
        String stName;
        int i;
        double lon, lat;
        double t;
        t = 0;

        List<String> dataList;
        double[][] discreteData = new double[_dataList.size()][3];
        double minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        List<String> stations = new ArrayList<>();

        for (i = 0; i < _dataList.size(); i++) {
            dataList = _dataList.get(i);
            stName = dataList.get(0);
            lon = Double.parseDouble(dataList.get(1));
            lat = Double.parseDouble(dataList.get(2));
            t = Double.parseDouble(dataList.get(varIdx));

            stations.add(stName);
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
        stData.dataExtent = dataExtent;
        stData.missingValue = this.getMissingValue();
        stData.stations = stations;

        return stData;
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        StationInfoData stInfoData = new StationInfoData();
        stInfoData.setDataList(_dataList);
        stInfoData.setFields(_fieldList);
        stInfoData.setVariables(_varList);

        return stInfoData;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
