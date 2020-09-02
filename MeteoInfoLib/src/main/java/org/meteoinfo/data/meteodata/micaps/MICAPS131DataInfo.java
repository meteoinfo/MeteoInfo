/* Copyright 2018 Yaqiang Wang,
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.Range;
import org.meteoinfo.ndarray.Section;
import org.meteoinfo.data.meteodata.Attribute;

/**
 *
 * @author yaqiang
 */
public class MICAPS131DataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private String zonName;
    private String dataName;
    private String flag;
    private String version;
    private LocalDateTime time;
    private int xNum;
    private int yNum;
    private int zNum;
    private int radarCount;
    private float startLon;
    private float startLat;
    private float _lon_Center;
    private float _lat_Center;
    private float lonDelta;
    private float latDelta;
    private float[] heights;
    private String[] stationNames;
    private float[] stLons;
    private float[] stLats;
    private float[] stAlts;
    private int dataByteNum;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MICAPS131DataInfo() {
        this.setDataType(MeteoDataType.MICAPS_131);
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
            byte[] bytes = new byte[1024];
            br.read(bytes);
            int sidx, len;
            sidx = 0;
            len = 12;
            byte[] bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            zonName = new String(bb, "GBK").trim();
            this.addAttribute(new Attribute("Zon_Name", zonName));
            sidx += len;
            len = 38;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            dataName = new String(bb, "GBK").trim();
            this.addAttribute(new Attribute("Data_Name", dataName));
            sidx += len;
            len = 8;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            flag = new String(bb, "GBK").trim();
            this.addAttribute(new Attribute("Flag", flag));
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            version = new String(bb, "GBK").trim();
            this.addAttribute(new Attribute("Version", version));
            this._description = this.zonName + "; " + this.dataName + "; " + this.flag + "; " + this.version;
            sidx += len;
            len = 2;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            int year = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            int month = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            int day = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            int hour = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            int minute = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            time = LocalDateTime.of(year, month, day, hour, minute, 0);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            int interval = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            xNum = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            yNum = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            zNum = DataConvert.bytes2Short(bb, ByteOrder.LITTLE_ENDIAN);
            if (zNum > 1) {
                this.dataByteNum = 1;
            } else {
                if (br.length() - 1024 == xNum * yNum)
                    this.dataByteNum = 1;
                else
                    this.dataByteNum = 2;
            }
            sidx += len;
            len = 4;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            radarCount = DataConvert.bytes2Int(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            startLon = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            startLat = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            this._lon_Center = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            this._lat_Center = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            this.lonDelta = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
            sidx += len;
            bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
            this.latDelta = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
            this.heights = new float[40];
            for (int i = 0; i < 40; i++) {
                sidx += len;
                bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
                this.heights[i] = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
            }
            sidx += len;
            len = 16;
            this.stationNames = new String[20];
            for (int i = 0; i < 20; i++) {
                bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
                this.stationNames[i] = new String(bb, "GBK").trim();
                sidx += len;
            }
            len = 4;
            this.stLons = new float[20];
            for (int i = 0; i < 20; i++) {
                bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
                this.stLons[i] = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
                sidx += len;
            }
            this.stLats = new float[20];
            for (int i = 0; i < 20; i++) {
                bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
                this.stLats[i] = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
                sidx += len;
            }
            this.stAlts = new float[20];
            for (int i = 0; i < 20; i++) {
                bb = Arrays.copyOfRange(bytes, sidx, sidx + len);
                this.stAlts[i] = DataConvert.bytes2Float(bb, ByteOrder.LITTLE_ENDIAN);
                sidx += len;
            }
            br.close();

            Dimension tdim = new Dimension(DimensionType.T);
            tdim.addValue(JDateUtil.toOADate(time));
            this.setTimeDimension(tdim);
            this.addDimension(tdim);
            Dimension zdim = new Dimension(DimensionType.Z);
            double[] zValues = new double[zNum];
            for (int i = 0; i < zNum; i++) {
                zValues[i] = this.heights[i];
            }
            zdim.setValues(zValues);
            this.setZDimension(zdim);
            this.addDimension(zdim);
            Dimension ydim = new Dimension(DimensionType.Y);
            double[] yValues = new double[yNum];
            for (int i = 0; i < yNum; i++) {
                yValues[i] = BigDecimalUtil.sub(startLat, BigDecimalUtil.mul((yNum - i - 1), latDelta));
            }
            ydim.setValues(yValues);
            this.addDimension(ydim);
            this.setYDimension(ydim);
            Dimension xdim = new Dimension(DimensionType.X);
            double[] xValues = new double[xNum];
            for (int i = 0; i < xNum; i++) {
                xValues[i] = BigDecimalUtil.add(startLon, BigDecimalUtil.mul(i, lonDelta));
            }
            xdim.setValues(xValues);
            this.addDimension(xdim);
            this.setXDimension(xdim);

            Variable var = new Variable();
            var.setName("var");
            var.setDimension(tdim);
            var.setDimension(zdim);
            var.setDimension(ydim);
            var.setDimension(xdim);
            var.addAttribute("data_name", dataName);
            this.addVariable(var);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS131DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS131DataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            Variable var = this.getVariable(varName);
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            int rangeIdx = 1;

            Range levRange = var.getLevelNum() > 0 ? section
                    .getRange(rangeIdx++)
                    : new Range(0, 0);

            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);

            IndexIterator ii = dataArray.getIndexIterator();

            for (int levelIdx = levRange.first(); levelIdx <= levRange.last();
                    levelIdx += levRange.stride()) {
                readXY(levelIdx, yRange, xRange, ii);
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(MICAPS131DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void readXY(int levelIdx, Range yRange, Range xRange, IndexIterator ii) {
        try {
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            int n = yNum * xNum * dataByteNum;
            byte[] bytes = new byte[n];
            br.skipBytes(1024);
            br.skipBytes(levelIdx * n);
            br.read(bytes);
            br.close();

            int index;
            if (dataByteNum == 1) {
                for (int y = yRange.first(); y <= yRange.last();
                        y += yRange.stride()) {
                    for (int x = xRange.first(); x <= xRange.last();
                            x += xRange.stride()) {
                        index = (yNum - y - 1) * xNum + x;             
                        ii.setFloatNext(DataConvert.byte2Int(bytes[index]));
                    }
                }
            } else {
                for (int y = yRange.first(); y <= yRange.last();
                        y += yRange.stride()) {
                    for (int x = xRange.first(); x <= xRange.last();
                            x += xRange.stride()) {
                        index = ((yNum - y - 1) * xNum + x) * 2;             
                        ii.setFloatNext(DataConvert.bytes2Short(Arrays.copyOfRange(bytes, index, index + 2), ByteOrder.LITTLE_ENDIAN));
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS131DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS131DataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            int n = yNum * xNum * dataByteNum;
            byte[] bytes = new byte[n];
            br.skipBytes(1024);
            br.skipBytes(levelIdx * n);
            br.read(bytes);
            br.close();

            GridData gridData = new GridData();
            double[][] gData = new double[yNum][xNum];
            if (dataByteNum == 1) {
                for (int i = 0; i < yNum; i++) {
                    for (int j = 0; j < xNum; j++) {
                        gData[i][j] = DataConvert.byte2Int(bytes[(yNum - i - 1) * xNum + j]);
                    }
                }
            } else {
                int index;
                for (int i = 0; i < yNum; i++) {
                    for (int j = 0; j < xNum; j++) {
                        index = ((yNum - i - 1) * xNum + j) * 2;             
                        gData[i][j] = DataConvert.bytes2Short(Arrays.copyOfRange(bytes, index, index + 2), ByteOrder.LITTLE_ENDIAN);
                    }
                }
            }
            gridData.data = gData;
            gridData.xArray = this.getXDimension().getValues();
            gridData.yArray = this.getYDimension().getValues();

            return gridData;
        } catch (IOException e) {
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
