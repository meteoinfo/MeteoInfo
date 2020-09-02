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

import org.meteoinfo.data.StationData;
import org.meteoinfo.data.dataframe.Column;
import org.meteoinfo.data.dataframe.ColumnIndex;
import org.meteoinfo.data.dataframe.DataFrame;
import org.meteoinfo.data.dataframe.Index;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.Extent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class HYSPLITPartDataInfo extends DataInfo implements IStationDataInfo {

    // <editor-fold desc="Variables">
    private List<List<Integer>> _parameters = new ArrayList<List<Integer>>();
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public HYSPLITPartDataInfo(){
        this.setDataType(MeteoDataType.HYSPLIT_Particle);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        try {
            this.setFileName(fileName);
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            int year, month, day, hour;
            List<LocalDateTime> times = new ArrayList<>();
            _parameters = new ArrayList<List<Integer>>();
            List<Variable> variables = new ArrayList<>();

            this.addAttribute(new Attribute("data_format", "HYSPLIT Particles"));

            int i = 0;
            String[] varNames = new String[]{"lat", "lon", "height"};
            while (br.getFilePointer() < br.length() - 28) {
                //Read head
                int pos = (int) br.getFilePointer();
                br.skipBytes(4);
                int particleNum = br.readInt();
                int pollutantNum = br.readInt();
                year = br.readInt();
                month = br.readInt();
                day = br.readInt();
                hour = br.readInt();                
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
                times.add(LocalDateTime.of(year, month, day, hour, 0, 0));
                List<Integer> data = new ArrayList<Integer>();
                data.add(particleNum);
                data.add(pollutantNum);
                data.add(pos);
                _parameters.add(data);

                Dimension dim = new Dimension();
                dim.setName(String.format("pnum_t%d", i));
                dim.setValues(new float[particleNum]);
                this.addDimension(dim);

                for (String varName : varNames) {
                    Variable var = new Variable();
                    var.setStation(true);
                    var.setName(String.format("%s_t%d", varName, i));
                    var.setDimension(dim);
                    var.setDataType(DataType.FLOAT);
                    var.addAttribute(new Attribute("time_index", i));
                    if (varName == "lon")
                        var.addAttribute("long_name", "longitude");
                    else if (varName == "lat")
                        var.addAttribute("long_name", "latitude");
                    else
                        var.addAttribute("long_name", "height");
                    variables.add(var);
                }

                //Skip data
                int len = (8 + pollutantNum * 4 + 60) * particleNum + 4;
                br.skipBytes(len);

                i ++;
            }

            br.close();

            List<Double> values = new ArrayList<Double>();
            for (LocalDateTime t : times) {
                values.add(JDateUtil.toOADate(t));
            }
            Dimension tDim = new Dimension(DimensionType.T);
            tDim.setValues(values);
            this.setTimeDimension(tDim);
            this.addDimension(tDim);

            this.setVariables(variables);
            
        } catch (IOException ex) {
            Logger.getLogger(HYSPLITPartDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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

    /*@Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        List<LocalDateTime> times = this.getTimes();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
        for (int i = 0; i < this.getTimeNum(); i++) {
            dataInfo += System.getProperty("line.separator") + "Time: " + format.format(times.get(i));
            dataInfo += System.getProperty("line.separator") + "\tParticle Number: " + _parameters.get(i).get(0);
            dataInfo += System.getProperty("line.separator") + "\tPollutant Number: " + _parameters.get(i).get(1);
        }

        return dataInfo;
    }*/
    
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
        try {
            Variable var = this.getVariable(varName);
            int timeIdx = (int)var.findAttribute("time_index").getNumericValue();
            int particleNum = _parameters.get(timeIdx).get(0);
            int pollutantNum = _parameters.get(timeIdx).get(1);
            int pos = _parameters.get(timeIdx).get(2);
            Array r = Array.factory(var.getDataType(), new int[]{particleNum});

            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            int i, j;
            float lon, lat, alt;

            br.seek(pos);
            br.skipBytes(28);
            for (i = 0; i < particleNum; i++) {
                br.skipBytes(8);
                for (j = 0; j < pollutantNum; j++) {
                    br.skipBytes(4);
                }
                br.skipBytes(8);
                lat = br.readFloat();
                lon = br.readFloat();
                alt = br.readFloat();

                if (varName.startsWith("lon"))
                    r.setFloat(i, lon);
                else if (varName.startsWith("lat"))
                    r.setFloat(i, lat);
                else
                    r.setFloat(i, alt);

                br.skipBytes(40);
            }

            return r;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Read data frame
     *
     * @param timeIdx Time index
     * @return Data frame
     */
    public DataFrame readDataFrame(int timeIdx) {
        try {
            int particleNum = _parameters.get(timeIdx).get(0);
            int pollutantNum = _parameters.get(timeIdx).get(1);
            int pos = _parameters.get(timeIdx).get(2);
            List<Array> data = new ArrayList<>();
            ColumnIndex columns = new ColumnIndex();
            for (Variable var : this.variables) {
                if ((int)var.findAttribute("time_index").getNumericValue() == timeIdx) {
                    columns.add(new Column(var.getName(), var.getDataType()));
                    data.add(Array.factory(var.getDataType(), new int[]{particleNum}));
                }
            }

            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            int i, j;
            float lon, lat, alt;

            br.seek(pos);
            br.skipBytes(28);
            for (i = 0; i < particleNum; i++) {
                br.skipBytes(8);
                for (j = 0; j < pollutantNum; j++) {
                    br.skipBytes(4);
                }
                br.skipBytes(8);
                lat = br.readFloat();
                lon = br.readFloat();
                alt = br.readFloat();

                data.get(0).setFloat(i, lat);
                data.get(1).setFloat(i, lon);
                data.get(2).setFloat(i, alt);

                br.skipBytes(40);
            }

            Index index = Index.factory(particleNum);
            DataFrame df = new DataFrame(data, index, columns);
            return df;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public StationData getStationData(int timeIdx, String varName, int levelIdx) {
        try {
            StationData stationData = new StationData();
            List<String> stations = new ArrayList<String>();
            int particleNum = _parameters.get(timeIdx).get(0);
            int pollutantNum = _parameters.get(timeIdx).get(1);
            int pos = _parameters.get(timeIdx).get(2);
            double[][] discreteData = new double[particleNum][3];

            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] aBytes;
            int i, j;
            float lon, lat, alt;
            float minX, maxX, minY, maxY;
            minX = 0;
            maxX = 0;
            minY = 0;
            maxY = 0;

            br.seek(pos);
            br.skipBytes(28);
            for (i = 0; i < particleNum; i++) {
                br.skipBytes(8);
                for (j = 0; j < pollutantNum; j++) {
                    br.skipBytes(4);
                }
                br.skipBytes(8);
                lat = br.readFloat();
                lon = br.readFloat();
                alt = br.readFloat();

                discreteData[i][0] = lon;
                discreteData[i][1] = lat;
                discreteData[i][2] = alt;
                stations.add("P" + String.valueOf(i + 1));

                br.skipBytes(40);

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

            br.close();

            stationData.data = discreteData;
            stationData.dataExtent = dataExtent;
            stationData.stations = stations;

            return stationData;
        } catch (IOException ex) {
            Logger.getLogger(HYSPLITPartDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
