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

import org.apache.commons.lang3.math.NumberUtils;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.util.JDateUtil;
import org.meteoinfo.common.util.NumberUtil;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.dimarray.DimArray;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.dimarray.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.Range;
import org.meteoinfo.ndarray.Section;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.ndarray.math.ArrayMath;

/**
 *
 * @author yaqiang
 */
public class MICAPS4DataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private double[] _xArray;
    private double[] _yArray;
    private int _headLineNum;
    private boolean _yReverse = false;
    private float _preHours;
    private int _level;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MICAPS4DataInfo() {
        this.setDataType(MeteoDataType.MICAPS_4);
        this.setMissingValue(9999.0);
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
        try {
            BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "gbk"));
            String aLine;
            String[] dataArray;
            int i, n;
            List<String> dataList = new ArrayList<>();

            this.setFileName(fileName);
            aLine = sr.readLine().trim();
            _description = aLine;
            _headLineNum = 1;
            //aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            for (i = 0; i < dataArray.length; i++) {
                if (!dataArray[i].isEmpty()) {
                    dataList.add(dataArray[i]);
                }
            }
            if (dataList.size() < 17) {
                for (n = 0; n <= 10; n++) {
                    if (dataList.size() < 17) {
                        aLine = sr.readLine().trim();
                        dataArray = aLine.split("\\s+");
                        for (i = 0; i < dataArray.length; i++) {
                            if (!dataArray[i].isEmpty()) {
                                dataList.add(dataArray[i]);
                            }
                        }
                        _headLineNum += 1;
                    } else {
                        break;
                    }
                }
            }
            sr.close();

            int idx = 3;
            int year = Integer.parseInt(dataList.get(idx));
            if (year < 100) {
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
            }
            LocalDateTime time = LocalDateTime.of(year, Integer.parseInt(dataList.get(idx + 1)),
                    Integer.parseInt(dataList.get(idx + 2)),
                    Integer.parseInt(dataList.get(idx + 3)), 0, 0);
            String preHourStr = dataList.get(idx + 4);
            if (NumberUtil.isInteger(preHourStr)) {
                _preHours = Integer.parseInt(preHourStr);
                time = time.plusHours((int) _preHours);
            } else {
                _preHours = Float.parseFloat(preHourStr);
                time = time.plusMinutes((int) (_preHours * 60));
            }
            
            _level = Integer.parseInt(dataList.get(idx + 5));
            float XDelt = Float.parseFloat(dataList.get(idx + 6));
            float YDelt = Float.parseFloat(dataList.get(idx + 7));
            float XMin = Float.parseFloat(dataList.get(idx + 8));
            float XMax = Float.parseFloat(dataList.get(idx + 9));
            float YMin = Float.parseFloat(dataList.get(idx + 10));
            float YMax = Float.parseFloat(dataList.get(idx + 11));
            int XNum = Integer.parseInt(dataList.get(idx + 12));
            int YNum = Integer.parseInt(dataList.get(idx + 13));
            /*float contourDelt = Float.parseFloat(dataList.get(idx + 14));
            float contourSValue = Float.parseFloat(dataList.get(idx + 15));
            float contourEValue = Float.parseFloat(dataList.get(idx + 16));
            float smoothCo = Float.parseFloat(dataList.get(idx + 17));
            float boldValue = Float.parseFloat(dataList.get(idx + 18));*/
            boolean isLonLat = true;
            if (dataList.size() >= 20) {
                if (dataList.get(idx + 16).equals("-1") || dataList.get(idx + 16).equals("-2") || dataList.get(idx + 16).equals("-3")) {
                    isLonLat = false;
                }
            }
            _xArray = new double[XNum];
            for (i = 0; i < XNum; i++) {
                _xArray[i] = XMin + i * XDelt;
            }
            _yArray = new double[YNum];

            _yReverse = false;
            if (YDelt < 0) {
                _yReverse = true;
                YDelt = -YDelt;
            }
            if (YMin > YMax) {
                float temp = YMin;
                YMin = YMax;
                YMax = temp;
            }
            for (i = 0; i < YNum; i++) {
                _yArray[i] = YMin + i * YDelt;
            }

            this.addAttribute(new Attribute("data_format", "MICAPS 4"));
            Dimension tdim = new Dimension(DimensionType.T);
            double[] values = new double[1];
            values[0] = JDateUtil.toOADate(time);
            tdim.setValues(values);
            this.setTimeDimension(tdim);
            this.addDimension(tdim);
            Dimension zdim = new Dimension(DimensionType.Z);
            zdim.setValues(new double[]{_level});
            this.addDimension(zdim);
            Dimension xdim = new Dimension(DimensionType.X);
            xdim.setValues(_xArray);
            this.setXDimension(xdim);            
            Dimension ydim = new Dimension(DimensionType.Y);
            ydim.setValues(_yArray);
            this.setYDimension(ydim);
            this.addDimension(ydim);
            this.addDimension(xdim);

            List<Variable> variables = new ArrayList<>();
            Variable var = new Variable();
            var.setName("var");
            var.setDataType(DataType.FLOAT);
            var.setDimension(tdim);
            var.setDimension(zdim);
            var.setDimension(ydim);
            var.setDimension(xdim);
            variables.add(var);
            this.setVariables(variables);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS4DataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
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
        try {
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            int rangeIdx = 2;
            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);
            IndexIterator ii = dataArray.getIndexIterator();
            readXY(yRange, xRange, ii);

            if (dataArray.getDataType().isNumeric()) {
                ArrayMath.missingToNaN(dataArray, this.missingValue);
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(MICAPS4DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private void readXY(Range yRange, Range xRange, IndexIterator ii) {
        try {
            BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(this.getFileName()), "gbk"));
            int i, j;
            for (i = 0; i < _headLineNum; i++) {
                sr.readLine();
            }

            List<String> dataList = new ArrayList<>();
            String[] dataArray;
            int col = 0;
            String aLine;
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            float[][] theData = new float[yNum][xNum];
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    while (dataList.size() >= xNum) {
                        for (i = 0; i < xNum; i++) {
                            //theData[col][i] = Float.parseFloat(dataList.get(i));
                            theData[col][i] = format.parse(dataList.get(i)).floatValue();
                        }
                        dataList = dataList.subList(xNum, dataList.size());
                        col += 1;
                    }
                    break;
                }
                aLine = aLine.trim();
                if (aLine.isEmpty())
                    continue;
                dataArray = aLine.split("\\s+");
                dataList.addAll(Arrays.asList(dataArray));
                if (col == 0) {
                    if (!MIMath.isNumeric(dataList.get(0))) {
                        aLine = sr.readLine().trim();
                        dataArray = aLine.split("\\s+");
                        dataList.clear();
                        dataList.addAll(Arrays.asList(dataArray));
                    }
                }
                for (i = 0; i < 1000; i++) {
                    if (dataList.size() < xNum) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        aLine = aLine.trim();
                        dataArray = aLine.split("\\s+");
                        dataList.addAll(Arrays.asList(dataArray));
                    } else {
                        break;
                    }
                }
                for (i = 0; i < xNum; i++) {
                    //theData[col][i] = Float.parseFloat(dataList.get(i));
                    theData[col][i] = format.parse(dataList.get(i)).floatValue();
                }
                if (dataList.size() > xNum) {
                    dataList = dataList.subList(xNum, dataList.size());
                } else {
                    dataList = new ArrayList<>();
                }
                col += 1;
            } while (aLine != null);

            sr.close();
            
            float[] data = new float[yNum * xNum];
            if (this._yReverse){
                for (i = 0; i < yNum; i++) {
                    for (j = 0; j < xNum; j++) {
                        data[i * xNum + j] = theData[yNum - 1 - i][j];
                    }
                }
            } else {
                for (i = 0; i < yNum; i++) {
                    for (j = 0; j < xNum; j++) {
                        data[i * xNum + j] = theData[i][j];
                    }
                }
            }

            for (int y = yRange.first(); y <= yRange.last();
                    y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last();
                        x += xRange.stride()) {
                    int index = y * xNum + x;
                    ii.setFloatNext(data[index]);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS4DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS4DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get grid data
     *
     * @param varName Variable name
     * @return Grid data
     */
    @Override
    public GridArray getGridArray(String varName) {
        return null;    
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, String varName, int levelIdx) {
        try {
            BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(this.getFileName()), "gbk"));
            int i, j;
            for (i = 0; i < _headLineNum; i++) {
                sr.readLine();
            }

            List<String> dataList = new ArrayList<>();
            String[] dataArray;
            int col = 0;
            String aLine;
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            double[][] theData = new double[yNum][xNum];
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    while (dataList.size() >= xNum) {
                        for (i = 0; i < xNum; i++) {
                            //theData[col][i] = Float.parseFloat(dataList.get(i));
                            theData[col][i] = format.parse(dataList.get(i)).doubleValue();
                        }
                        dataList = dataList.subList(xNum, dataList.size());
                        col += 1;
                    }
                    break;
                }
                aLine = aLine.trim();
                if (aLine.isEmpty())
                    continue;
                dataArray = aLine.split("\\s+");
                dataList.addAll(Arrays.asList(dataArray));
                if (col == 0) {
                    if (!MIMath.isNumeric(dataList.get(0))) {
                        aLine = sr.readLine().trim();
                        dataArray = aLine.split("\\s+");
                        dataList.clear();
                        dataList.addAll(Arrays.asList(dataArray));
                    }
                }
                for (i = 0; i < 1000; i++) {
                    if (dataList.size() < xNum) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        aLine = aLine.trim();
                        if (aLine.isEmpty())
                            continue;
                        dataArray = aLine.split("\\s+");
                        dataList.addAll(Arrays.asList(dataArray));
                    } else {
                        break;
                    }
                }
                for (i = 0; i < xNum; i++) {
                    //theData[col][i] = Double.parseDouble(dataList.get(i));
                    theData[col][i] = format.parse(dataList.get(i)).doubleValue();
                }
                if (dataList.size() > xNum) {
                    dataList = new ArrayList(dataList.subList(xNum, dataList.size()));
                } else {
                    dataList = new ArrayList<>();
                }
                col += 1;
            } while (aLine != null);

            sr.close();

            double[][] newGridData = new double[yNum][xNum];
            if (!_yReverse) {
                newGridData = theData;
            } else {
                for (i = 0; i < yNum; i++) {
                    for (j = 0; j < xNum; j++) {
                        newGridData[i][j] = theData[yNum - 1 - i][j];
                    }
                }
            }

            return new GridData(newGridData, this.getXDimension().getValues(),
                    this.getYDimension().getValues(), this.missingValue);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(MICAPS4DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, String varName, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, String varName, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, String varName, int lonIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, String varName, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
