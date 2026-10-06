/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.micaps;

import org.meteoinfo.geometry.Extent;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.dimarray.DimensionType;
import org.meteoinfo.data.meteodata.*;
import org.meteoinfo.dataframe.Column;
import org.meteoinfo.dataframe.ColumnIndex;
import org.meteoinfo.dataframe.DataFrame;
import org.meteoinfo.dataframe.Index;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.math.ArrayMath;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yaqiang Wang
 */
public class MICAPS5DataInfo extends DataInfo implements IStationDataInfo {
    // <editor-fold desc="Variables">
    private String description;
    private List<String> varList = new ArrayList<>();
    private List<String> fieldList = new ArrayList<>();
    private List<List<String>> dataList = new ArrayList<>();
    private List<List<String>> stationList = new ArrayList<>();
    private int stNum;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public MICAPS5DataInfo(){
        varList = Arrays.asList(new String[]{"Stid","Pressure","Height","Temperature","DewPoint",
                "WindDirection","WindSpeed"});
        fieldList.addAll(Arrays.asList(new String[]{"Stid", "Longitude", "Latitude", "Altitude"}));
        this.setMissingValue(9999.0);
        this.setDataType(MeteoDataType.MICAPS_5);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public boolean isValidFile(RandomAccessFile raf) {
        return false;
    }

    @Override
    public void readDataInfo(String fileName) {
        BufferedReader sr = null;
        try {

            this.setFileName(fileName);
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "gbk"));
            String[] dataArray;
            List<String> dataLine = new ArrayList<>();

            //Read file head
            String line = sr.readLine().trim();
            description = line;
            line = sr.readLine().trim();
            dataArray = line.split("\\s+");
            dataLine.clear();
            for (int i = 0; i < dataArray.length; i++) {
                dataLine.add(dataArray[i]);
            }
            if (dataLine.size() < 5){
                line = sr.readLine().trim();
                dataArray = line.split("\\s+");
                for (int i = 0; i < dataArray.length; i++) {
                    dataLine.add(dataArray[i]);
                }
            }

            int year = Integer.parseInt(dataLine.get(0));
            if (year < 100) {
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
            }
            LocalDateTime time = LocalDateTime.of(year, Integer.parseInt(dataLine.get(1)),
                    Integer.parseInt(dataLine.get(2)), Integer.parseInt(dataLine.get(3)),
                    0, 0);
            stNum = Integer.parseInt(dataLine.get(4));
            
            //Read data
            int len, N = 0;
            String stId;
            for (int i = 0; i < stNum; i++) {
                line = sr.readLine();
                if (line == null) {
                    stNum = i;
                    break;
                }

                line = line.trim();
                dataArray = line.split("\\s+");
                stId = dataArray[0];
                dataLine = new ArrayList<>();
                for (int j = 0; j < dataArray.length; j++) {
                    dataLine.add(dataArray[j]);
                }
                this.stationList.add(dataLine);
                len = Integer.parseInt(dataArray[4]);
                int ln = len / 6;
                for (int j = 0; j < ln; j++) {
                    line = sr.readLine();
                    if (line == null) {
                        break;
                    }

                    line = line.trim();
                    dataArray = line.split("\\s+");
                    dataLine = new ArrayList<>();
                    dataLine.add(stId);
                    for (int k = 0; k < dataArray.length; k++) {
                        dataLine.add(dataArray[k]);
                    }
                    this.dataList.add(dataLine);
                    N += 1;
                }
            }

            this.addAttribute(new Attribute("data_format", "MICAPS 5"));
            this.addAttribute(new Attribute("time", time));

            Dimension tdim = new Dimension(DimensionType.T);
            tdim.setValue(time);
            this.setTimeDimension(tdim);

            Dimension stDim = new Dimension(DimensionType.OTHER);
            stDim.setShortName("station");
            double[] values = new double[stNum];
            for (int i = 0; i < stNum; i++){
                values[i] = i;
            }
            stDim.setValues(values);
            this.addDimension(stDim);

            Dimension dataDim = new Dimension(DimensionType.OTHER);
            dataDim.setShortName("data");
            double[] values2 = new double[N];
            for (int i = 0; i < N; i++){
                values2[i] = i;
            }
            dataDim.setValues(values2);
            this.addDimension(dataDim);

            List<Variable> variables = new ArrayList<>();
            for (String vName : this.fieldList) {
                Variable var = new Variable();
                var.setName(vName);
                var.setStation(true);
                var.setDimension(stDim);
                variables.add(var);
            }

            for (String vName : this.varList) {
                if (vName.equals("Stid")) {
                    continue;
                }

                Variable var = new Variable();
                var.setName(vName);
                var.setStation(true);
                var.setDimension(dataDim);
                variables.add(var);
            }

            this.setVariables(variables);

            //Add coordinate variables
            Variable variable;
            for (Dimension dim : this.dimensions) {
                variable = new Variable(dim.getName());
                variable.setDimVar(true);
                variable.setCachedData(dim.getDimValue());
                variable.addDimension(dim);
                this.addCoordinate(variable);
            }
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
        dataInfo = "Description: " + description;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
        dataInfo += System.getProperty("line.separator") + "Time: " + format.format(this.getTimes().getDate(0));
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
    public Array realRead(String varName){
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
        
        Array r = realRead(varName, origin, size, stride);
        
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
    public Array realRead(String varName, int[] origin, int[] size, int[] stride) {
        int varIdx = this.fieldList.indexOf(varName);
        if (varIdx >= 0) {
            DataType dt = DataType.FLOAT;
            switch (varName) {
                case "Stid":
                    dt = DataType.STRING;
                    break;
            }
            int[] shape = new int[1];
            shape[0] = this.stNum;
            Array r = Array.factory(dt, shape);
            float v;
            List<String> dataLine;
            for (int i = 0; i < this.stNum; i++) {
                dataLine = this.stationList.get(i);
                switch (dt) {
                    case STRING:
                        r.setObject(i, dataLine.get(varIdx));
                        break;
                    case FLOAT:
                        v = Float.parseFloat(dataLine.get(varIdx));
                        r.setFloat(i, v);
                        break;
                }
            }

            if (r.getDataType().isNumeric()) {
                ArrayMath.missingToNaN(r, this.missingValue);
            }

            return r;
        } else {
            varIdx = this.varList.indexOf(varName);
            if (varIdx >= 0) {
                varIdx += 1;
                DataType dt = DataType.FLOAT;
                int N = this.dataList.size();
                Array r = Array.factory(dt, new int[]{N});
                float v;
                List<String> dataLine;
                for (int i = 0; i < this.stNum; i++) {
                    dataLine = this.dataList.get(i);
                    v = Float.parseFloat(dataLine.get(varIdx));
                    r.setFloat(i, v);
                }

                if (r.getDataType().isNumeric()) {
                    ArrayMath.missingToNaN(r, this.missingValue);
                }

                return r;
            }
        }

        return null;
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
        for (String vName : this.varList) {
            switch (vName) {
                case "Stid":
                    continue;
                default:
                    dtype = DataType.FLOAT;
                    break;
            }
            columns.add(new Column(vName, dtype));
            data.add(Array.factory(dtype, new int[]{dataList.size()}));
        }
        List<String> idxList = new ArrayList<>();
        Array dd;
        for (int i = 0; i < dataList.size(); i++) {
            List<String> dataLine = dataList.get(i);
            idxList.add(dataLine.get(0));
            for (int j = 0; j < data.size(); j++) {
                dd = (Array) data.get(j);
                switch (dd.getDataType()) {
                    case FLOAT:
                        try {
                            dd.setFloat(i, Float.parseFloat(dataLine.get(j + 1)));
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

    @Override
    public StationData getStationData(int timeIdx, String varName, int levelIdx) {
        int varIdx = this.getVariableIndex(varName);
        String stName;
        int i;
        double lon, lat;
        double t;
        t = 0;

        List<String> dataList;
        double[][] discreteData = new double[this.dataList.size()][3];
        double minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        List<String> stations = new ArrayList<>();

        //Get real variable index
        //varIdx = _fieldList.indexOf(_varList.get(varIdx));

        for (i = 0; i < this.dataList.size(); i++) {
            dataList = this.dataList.get(i);
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
        stInfoData.setDataList(dataList);
        stInfoData.setFields(fieldList);
        stInfoData.setVariables(varList);

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

        for (i = 0; i < this.dataList.size(); i++) {
            dataList = this.dataList.get(i);
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
