/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.micaps;

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
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModel;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.Extent;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.data.meteodata.Attribute;

/**
 *
 * @author Yaqiang Wang
 */
public class MICAPS2DataInfo extends DataInfo implements IStationDataInfo{
    // <editor-fold desc="Variables">
    private String _description;
    private List<String> _varList = new ArrayList<>();
    private List<String> _fieldList = new ArrayList<>();
    private List<List<String>> _dataList = new ArrayList<>();
    private int stNum;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    
    public MICAPS2DataInfo(){
        String[] items = new String[]{"Height","Temperature","DepDewPoint","WindDirection","WindSpeed"};
        _varList = Arrays.asList(items);
        _fieldList.addAll(Arrays.asList(new String[]{"Stid", "Longitude", "Latitude", "Altitude", "Grade"}));
        _fieldList.addAll(_varList);
        this.setMissingValue(9999.0);
        this.setDataType(MeteoDataType.MICAPS_2);
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

            //Read file head
            String aLine = sr.readLine().trim();
            _description = aLine;
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            dataList.clear();
            for (i = 0; i < dataArray.length; i++) {
                dataList.add(dataArray[i]);
            }
            if (dataList.size() < 6){
                aLine = sr.readLine().trim();
                dataArray = aLine.split("\\s+");
                for (i = 0; i < dataArray.length; i++) {
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
            stNum = Integer.parseInt(dataList.get(5));
            
            //Read data
            while((aLine = sr.readLine()) != null){
                aLine = aLine.trim();
                dataArray = aLine.split("\\s+");
                dataList = new ArrayList<>();
                for (i = 0; i < dataArray.length; i++) {
                    dataList.add(dataArray[i]);
                }
                if (dataList.size() < 10){
                    aLine = sr.readLine().trim();
                    dataArray = aLine.split("\\s+");
                    for (i = 0; i < dataArray.length; i++) {
                        dataList.add(dataArray[i]);
                    }
                }
                _dataList.add(dataList);
            }
            
            Dimension tdim = new Dimension(DimensionType.T);
            double[] values = new double[1];
            values[0] = JDateUtil.toOADate(time);
            tdim.setValues(values);
            this.setTimeDimension(tdim);
            Dimension zdim = new Dimension(DimensionType.Z);
            zdim.setValues(new double[]{level});
            this.setZDimension(zdim);
            Dimension stdim = new Dimension(DimensionType.Other);
            stdim.setShortName("station");
            values = new double[stNum];
            for (i = 0; i < stNum; i++){
                values[i] = i;
            }
            stdim.setValues(values);
            this.addDimension(stdim);
            List<Variable> variables = new ArrayList<>();
            for (String vName : this._fieldList) {
                Variable var = new Variable();
                var.setName(vName);
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

        DataType dt = DataType.FLOAT;
        switch (varName) {
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
        //List<List<String>> allDataList = this.readData();

        for (i = 0; i < this.stNum; i++) {
            dataList = this._dataList.get(i);
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

        //Get real variable index
        //varIdx = _fieldList.indexOf(_varList.get(varIdx));

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

        for (i = 0; i < _dataList.size(); i++) {
            dataList = _dataList.get(i);
            aStid = dataList.get(0);
            lon = Float.parseFloat(dataList.get(1));
            lat = Float.parseFloat(dataList.get(2));

            StationModel sm = new StationModel();
            sm.setStationIdentifer(aStid);
            sm.setLongitude(lon);
            sm.setLatitude(lat);
            sm.setWindDirection(Double.parseDouble(dataList.get(8)));    //Wind direction
            sm.setWindSpeed(Double.parseDouble(dataList.get(9)));    //Wind speed            
            sm.setCloudCover(1);    //Cloud cover
            sm.setTemperature(Double.parseDouble(dataList.get(6)));    //Temperature
            double ddp = Double.parseDouble(dataList.get(7));
            sm.setDewPoint(sm.getTemperature() - ddp);    //Dew point
            sm.setPressure(Double.parseDouble(dataList.get(5)));
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
