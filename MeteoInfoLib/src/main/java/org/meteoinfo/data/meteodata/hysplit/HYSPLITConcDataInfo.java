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

import org.meteoinfo.data.meteodata.ascii.ASCIIGridDataInfo;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.arl.ARLDataInfo;
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
public class HYSPLITConcDataInfo extends DataInfo implements IGridDataInfo {
    // <editor-fold desc="Variables">

    private int _pack_flag;
    private int _loc_num;
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public HYSPLITConcDataInfo(){
        this.setDataType(MeteoDataType.HYSPLIT_Conc);
    }
    
    /**
     * Constructor
     * @param bigendian Big endian or not
     */
    public HYSPLITConcDataInfo(boolean bigendian){
        this();
        if (bigendian)
            this.byteOrder = ByteOrder.BIG_ENDIAN;
        else
            this.byteOrder = ByteOrder.LITTLE_ENDIAN;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Set if is big endian
     *
     * @param value Boolean
     */
    public void setBigEndian(boolean value) {
        if (value) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        }
    }
    
    @Override
    public void readDataInfo(String fileName) {
        try {
            this.setFileName(fileName);

            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            int i, j, hBytes;
            byte[] aBytes;

            //Record #1
            br.skipBytes(4);
            aBytes = new byte[4];
            br.read(aBytes);
            String Ident = new String(aBytes);
            byte[] bytes = new byte[28];
            br.read(bytes);
            int start = 0;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            //int year = br.readInt();
            int year = DataConvert.bytes2Int(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            //int month = br.readInt();
            int month = DataConvert.bytes2Int(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            //int day = br.readInt();
            int day = DataConvert.bytes2Int(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            //int hour = br.readInt();
            int hour = DataConvert.bytes2Int(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            //int forecast_hour = br.readInt();
            int forecast_hour = DataConvert.bytes2Int(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            //_loc_num = br.readInt();
            _loc_num = DataConvert.bytes2Int(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            //_pack_flag = br.readInt();
            _pack_flag = DataConvert.bytes2Int(aBytes, byteOrder);

            //Record #2
            Object[][] locArray = new Object[8][_loc_num];
            bytes = new byte[_loc_num * (8 + 32)];
            br.read(bytes);
            start = 0;
            for (i = 0; i < _loc_num; i++) {
                //br.skipBytes(8);
                start += 8;
                for (j = 0; j < 4; j++) {                    
                    //locArray[j][i] = br.readInt();
                    System.arraycopy(bytes, start, aBytes, 0, 4);
                    locArray[j][i] = DataConvert.bytes2Int(aBytes, byteOrder);
                    start += 4;
                }
                for (j = 4; j < 7; j++) {
                    //locArray[j][i] = br.readFloat();
                    System.arraycopy(bytes, start, aBytes, 0, 4);
                    locArray[j][i] = DataConvert.bytes2Float(aBytes, byteOrder);
                    start += 4;
                }
                //locArray[7][i] = br.readInt();
                System.arraycopy(bytes, start, aBytes, 0, 4);
                locArray[7][i] = DataConvert.bytes2Int(aBytes, byteOrder);
                start += 4;
            }

            //Record #3
            String fName = new File(fileName).getName().toLowerCase();
            if (fName.contains("gemzint")) {
                br.skipBytes(4);   //For vertical concentration file gemzint
            } else {
                br.skipBytes(8);
            }
            //int lat_point_num = br.readInt();
            //int lon_point_num = br.readInt();
            //float lat_delta = br.readFloat();
            //float lon_delta = br.readFloat();
            //float lat_LF = br.readFloat();
            //float lon_LF = br.readFloat();
            bytes = new byte[24];
            br.read(bytes);
            start = 0;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            int lat_point_num = DataConvert.bytes2Int(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            int lon_point_num = DataConvert.bytes2Int(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            float lat_delta = DataConvert.bytes2Float(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            float lon_delta = DataConvert.bytes2Float(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            float lat_LF = DataConvert.bytes2Float(aBytes, byteOrder);
            start += 4;
            System.arraycopy(bytes, start, aBytes, 0, 4);
            float lon_LF = DataConvert.bytes2Float(aBytes, byteOrder);

            double[] X = new double[lon_point_num];
            double[] Y = new double[lat_point_num];
            double lonMin = BigDecimalUtil.toDouble(lon_LF);
            double lonDelta = BigDecimalUtil.toDouble(lon_delta);
            for (i = 0; i < lon_point_num; i++) {
                //X[i] = lon_LF + i * lon_delta;
                X[i] = BigDecimalUtil.add(lonMin, BigDecimalUtil.mul(i, lonDelta));
            }
            if (X[0] == 0 && BigDecimalUtil.add(X[X.length - 1], lon_delta) == 360) {
                this.setGlobal(true);
            }
            double latMin = BigDecimalUtil.toDouble(lat_LF);
            double latDelta = BigDecimalUtil.toDouble(lat_delta);
            for (i = 0; i < lat_point_num; i++) {
                //Y[i] = lat_LF + i * lat_delta;
                Y[i] = BigDecimalUtil.add(latMin, BigDecimalUtil.mul(i, latDelta));
            }
            
            this.addAttribute(new Attribute("data_format", "HYSPLIT Concentration"));
            Dimension xDim = new Dimension(DimensionType.X);
            xDim.setShortName("lon");
            xDim.setValues(X);
            this.setXDimension(xDim);
            this.addDimension(xDim);            
            Dimension yDim = new Dimension(DimensionType.Y);
            yDim.setShortName("lat");
            yDim.setValues(Y);
            this.setYDimension(yDim);
            this.addDimension(yDim);
            List<Variable> variables = new ArrayList<>();            

            //Record #4
            br.skipBytes(8);            
            //int level_num = br.readInt();
            br.read(aBytes);
            int level_num = DataConvert.bytes2Int(aBytes, byteOrder);
            double[] heights = new double[level_num];
            bytes = new byte[level_num * 4];
            br.read(bytes);
            start = 0;
            for (i = 0; i < level_num; i++) {
                //heights[i] = br.readInt();
                System.arraycopy(bytes, start, aBytes, 0, 4);
                heights[i] = DataConvert.bytes2Int(aBytes, byteOrder);
                start += 4;
            }
            Dimension zDim = new Dimension(DimensionType.Z);
            zDim.setShortName("level");
            zDim.setValues(heights);
            this.setZDimension(zDim);
            this.addDimension(zDim);

            //Record #5
            br.skipBytes(8);
            //int pollutant_num = br.readInt();
            br.read(aBytes);
            int pollutant_num = DataConvert.bytes2Int(aBytes, byteOrder);     
            String vName;
            for (i = 0; i < pollutant_num; i++) {
                br.read(aBytes);
                vName = new String(aBytes);
                vName = vName.trim();
                Variable var = new Variable();
                var.setName(vName);
                var.setDataType(DataType.FLOAT);
                var.addAttribute("long_name", vName);
                variables.add(var);
            }
            this.setVariables(variables);

            hBytes = 36 + lon_point_num * 40 + 32 + 12 + level_num * 4 + 12
                    + pollutant_num * 4;
            int hByte_num = hBytes;

            //Record Data
            int k, tNum;
            tNum = 0;
            int[] sampleTimes = new int[6];
            String dStr;
            LocalDateTime aDateTime;
            List<LocalDateTime> sample_start = new ArrayList<>();
            List<LocalDateTime> sample_stop = new ArrayList<>();
            do {
                //Record #6
                br.skipBytes(8);
                bytes = new byte[24];
                br.read(bytes);
                start = 0;
                for (i = 0; i < 6; i++) {
                    //sampleTimes[i] = br.readInt();
                    System.arraycopy(bytes, start, aBytes, 0, 4);
                    sampleTimes[i] = DataConvert.bytes2Int(aBytes, byteOrder);
                    start += 4;
                }
                year = sampleTimes[0];
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
                aDateTime = LocalDateTime.of(year, sampleTimes[1], sampleTimes[2], sampleTimes[3], 0, 0);
                sample_start.add(aDateTime);

                //Record #7
                br.skipBytes(8);
                bytes = new byte[24];
                br.read(bytes);
                start = 0;
                for (i = 0; i < 6; i++) {
                    //sampleTimes[i] = br.readInt();
                    System.arraycopy(bytes, start, aBytes, 0, 4);
                    sampleTimes[i] = DataConvert.bytes2Int(aBytes, byteOrder);
                    start += 4;
                }
                year = sampleTimes[0];
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
                aDateTime = LocalDateTime.of(year, sampleTimes[1], sampleTimes[2], sampleTimes[3], 0, 0);
                sample_stop.add(aDateTime);

                //Record 8;
                int aLevel, aN, IP, JP;
                String aType;
                for (i = 0; i < pollutant_num; i++) {
                    for (j = 0; j < level_num; j++) {
                        if (_pack_flag == 1) {
                            br.skipBytes(8);
                            br.read(aBytes);
                            aType = new String(aBytes);
                            //aLevel = br.readInt();
                            br.read(aBytes);
                            aLevel = DataConvert.bytes2Int(aBytes, byteOrder);
                            //aN = br.readInt();
                            br.read(aBytes);
                            aN = DataConvert.bytes2Int(aBytes, byteOrder);
//                            for (k = 0; k < aN; k++) {
//                                if (br.getFilePointer() + 8 > br.length()) {
//                                    break;
//                                }
//                                //IP = br.readShort();
//                                //JP = br.readShort();
//                                //br.skipBytes(4);
//                                br.skipBytes(8);
//                            }
                            br.skipBytes(aN * 8);
                        } else {
                            br.skipBytes(8);
                            br.read(aBytes);
                            aType = new String(aBytes);
                            //aLevel = br.readInt();
                            br.read(aBytes);
                            aLevel = DataConvert.bytes2Int(aBytes, byteOrder);
//                            for (JP = 0; JP < lat_point_num; JP++) {
//                                for (IP = 0; IP < lon_point_num; IP++) {
//                                    br.skipBytes(4);
//                                }
//                            }
                            br.skipBytes(lat_point_num * lon_point_num * 4);
                        }
                    }
                }

                tNum += 1;

                if (br.getFilePointer() + 10 > br.length()) {
                    break;
                }
            } while (true);

            List<Double> values = new ArrayList<>();
            for (LocalDateTime t : sample_start) {
                values.add(JDateUtil.toOADate(t));
            }
            Dimension tDim = new Dimension(DimensionType.T);
            tDim.setShortName("time");
            tDim.setValues(values);
            this.setTimeDimension(tDim);
            this.addDimension(tDim);

            for (Variable v : variables) {                                
                v.setDimension(tDim);
                v.setDimension(zDim);
                v.setDimension(yDim);
                v.setDimension(xDim);
            }
            this.setVariables(variables);

            br.close();
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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

//    @Override
//    public String generateInfoText() {
//        String dataInfo;
//        dataInfo = "File Name: " + this.getFileName();
//        dataInfo += System.getProperty("line.separator") + "Pack Flag = " + String.valueOf(_pack_flag);
//        dataInfo += System.getProperty("line.separator") + "Xsize = " + String.valueOf(this.getXDimension().getLength())
//                + "  Ysize = " + String.valueOf(this.getYDimension().getLength()) + "  Zsize = " + String.valueOf(this.getZDimension().getLength())
//                + "  Tsize = " + String.valueOf(this.getTimeDimension().getLength());
//        dataInfo += System.getProperty("line.separator") + "Number of Variables = " + String.valueOf(this.getVariableNum());
//        for (String v : this.getVariableNames()) {
//            dataInfo += System.getProperty("line.separator") + v;
//        }
//
//        return dataInfo;
//    }
    
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
            Variable var = this.getVariable(varName);
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.DOUBLE, section.getShape());
            int rangeIdx = 0;
            Range timeRange = section.getRank() > 2 ? section
                    .getRange(rangeIdx++)
                    : new Range(0, 0);

            Range levRange = var.getLevelNum() > 0 ? section
                    .getRange(rangeIdx++)
                    : new Range(0, 0);

            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);

            IndexIterator ii = dataArray.getIndexIterator();

            for (int timeIdx = timeRange.first(); timeIdx <= timeRange.last();
                    timeIdx += timeRange.stride()) {
                int levelIdx = levRange.first();

                for (; levelIdx <= levRange.last();
                        levelIdx += levRange.stride()) {
                    readXY(varName, timeIdx, levelIdx, yRange, xRange, ii);
                }
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(HYSPLITConcDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private void readXY(String varName, int timeIdx, int levelIdx, Range yRange, Range xRange, IndexIterator ii) {
        try {
            int varIdx = this.getVariableNames().indexOf(varName);
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            int i, j, nBytes;
            byte[] aBytes = new byte[4];
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            double[][] dataArray = new double[xNum][yNum];
            double[] data = new double[yNum * xNum];

            //Record #1            
            br.skipBytes(36);

            //Record #2
            nBytes = (8 * 4 + 8) * _loc_num;
            br.skipBytes(nBytes);

            //Record #3
            String fName = new File(this.getFileName()).getName().toLowerCase();
            if (fName.contains("gemzint")) {
                br.skipBytes(28);   //For vertical concentration file gemzint
            } else {
                br.skipBytes(32);
            }

            //Record #4
            nBytes = 12 + this.getZDimension().getLength() * 4;
            br.skipBytes(nBytes);

            //Record #5
            nBytes = 12 + this.getVariableNum() * 4;
            br.skipBytes(nBytes);

            //Record Data
            int t, k;
            int aLevel, aN, IP, JP;
            String aType;
            double aConc;
            byte[] bytes;
            byte[] sbytes = new byte[2];
            int start = 0;
            for (t = 0; t < this.getTimeNum(); t++) {
                br.skipBytes(64);

                for (i = 0; i < this.getVariableNum(); i++) {
                    for (j = 0; j < this.getZDimension().getLength(); j++) {
                        if (t == timeIdx && i == varIdx && j == levelIdx) {
                            if (br.getFilePointer() + 28 > br.length()) {
                                break;
                            }
                            if (_pack_flag == 1) {
                                br.skipBytes(8);
                                br.read(aBytes);
                                aType = new String(aBytes);
                                //aLevel = br.readInt();
                                br.read(aBytes);
                                aLevel = DataConvert.bytes2Int(aBytes, byteOrder);
                                //aN = br.readInt();
                                br.read(aBytes);
                                aN = DataConvert.bytes2Int(aBytes, byteOrder);
                                bytes = new byte[aN * 8];
                                br.read(bytes);
                                start = 0;
                                for (k = 0; k < aN; k++) {
//                                    if (br.getFilePointer() + 8 > br.length()) {
//                                        break;
//                                    }
                                    //IP = br.readShort();
                                    //JP = br.readShort();
                                    //aConc = br.readFloat();
                                    System.arraycopy(bytes, start, sbytes, 0, 2);
                                    IP = DataConvert.bytes2Short(sbytes, byteOrder) - 1;
                                    start += 2;
                                    System.arraycopy(bytes, start, sbytes, 0, 2);                                    
                                    JP = DataConvert.bytes2Short(sbytes, byteOrder) - 1;
                                    start += 2;
                                    System.arraycopy(bytes, start, aBytes, 0, 4);
                                    aConc = DataConvert.bytes2Float(aBytes, byteOrder);
                                    start += 4;
                                    if (IP >= 0 && IP < xNum && JP >= 0 && JP < yNum) {
                                        dataArray[IP][JP] = aConc;
                                    }
                                }
                            } else {
                                br.skipBytes(8);
                                br.read(aBytes);
                                aType = new String(aBytes);
                                //aLevel = br.readInt();
                                br.read(aBytes);
                                aLevel = DataConvert.bytes2Int(aBytes, byteOrder);
                                bytes = new byte[yNum * xNum * 4];
                                br.read(bytes);
                                start = 0;
                                for (JP = 0; JP < yNum; JP++) {
                                    for (IP = 0; IP < xNum; IP++) {
                                        //aConc = br.readFloat();
                                        System.arraycopy(bytes, start, aBytes, 0, 4);
                                        aConc = DataConvert.bytes2Float(aBytes, byteOrder);
                                        start += 4;
                                        dataArray[IP][JP] = aConc;
                                    }
                                }
                            }
                        } else {
                            if (br.getFilePointer() + 28 > br.length()) {
                                break;
                            }
                            if (_pack_flag == 1) {
                                br.skipBytes(8);
                                br.read(aBytes);
                                aType = new String(aBytes);
                                //aLevel = br.readInt();
                                br.read(aBytes);
                                aLevel = DataConvert.bytes2Int(aBytes, byteOrder);
                                //aN = br.readInt();
                                br.read(aBytes);
                                aN = DataConvert.bytes2Int(aBytes, byteOrder);
//                                for (k = 0; k < aN; k++) {
//                                    if (br.getFilePointer() + 8 > br.length()) {
//                                        break;
//                                    }
//                                    IP = br.readShort();
//                                    JP = br.readShort();
//                                    br.skipBytes(4);
//                                }
                                br.skipBytes(aN * 8);
                            } else {
                                br.skipBytes(8);
                                br.read(aBytes);
                                aType = new String(aBytes);
                                //aLevel = br.readInt();
                                br.read(aBytes);
                                aLevel = DataConvert.bytes2Int(aBytes, byteOrder);
//                                for (JP = 0; JP < yNum; JP++) {
//                                    for (IP = 0; IP < xNum; IP++) {
//                                        br.skipBytes(4);
//                                    }
//                                }
                                br.skipBytes(yNum * xNum * 4);
                            }
                        }
                    }
                }

                if (br.getFilePointer() + 10 > br.length()) {
                    break;
                }
            }

            br.close();

            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    data[i * xNum + j] = dataArray[j][i];
                }
            }
            
            for (int y = yRange.first(); y <= yRange.last();
                    y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last();
                        x += xRange.stride()) {
                    int index = y * xNum + x;
                    ii.setDoubleNext(data[index]);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
            int i, j, nBytes;
            byte[] aBytes = new byte[4];
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            double[][] dataArray = new double[xNum][yNum];
            double[][] newDataArray = new double[yNum][xNum];
            int varIdx = this.getVariableIndex(varName);

            //Record #1            
            br.skipBytes(36);

            //Record #2
            nBytes = (8 * 4 + 8) * _loc_num;
            br.skipBytes(nBytes);

            //Record #3
            String fName = new File(this.getFileName()).getName().toLowerCase();
            if (fName.contains("gemzint")) {
                br.skipBytes(28);   //For vertical concentration file gemzint
            } else {
                br.skipBytes(32);
            }

            //Record #4
            nBytes = 12 + this.getZDimension().getLength() * 4;
            br.skipBytes(nBytes);

            //Record #5
            nBytes = 12 + this.getVariableNum() * 4;
            br.skipBytes(nBytes);

            //Record Data
            int t, k;
            int aLevel, aN, IP, JP;
            String aType;
            double aConc;
            for (t = 0; t < this.getTimeNum(); t++) {
                br.skipBytes(64);

                for (i = 0; i < this.getVariableNum(); i++) {
                    for (j = 0; j < this.getZDimension().getLength(); j++) {
                        if (t == timeIdx && i == varIdx && j == levelIdx) {
                            if (br.getFilePointer() + 28 > br.length()) {
                                break;
                            }
                            if (_pack_flag == 1) {
                                br.skipBytes(8);
                                br.read(aBytes);
                                aType = new String(aBytes);
                                aLevel = br.readInt();
                                aN = br.readInt();
                                for (k = 0; k < aN; k++) {
                                    if (br.getFilePointer() + 8 > br.length()) {
                                        break;
                                    }
                                    IP = br.readShort() - 1;
                                    JP = br.readShort() - 1;
                                    aConc = br.readFloat();
                                    if (IP >= 0 && IP < xNum && JP >= 0 && JP < yNum) {
                                        dataArray[IP][JP] = aConc;
                                    }
                                }
                            } else {
                                br.skipBytes(8);
                                br.read(aBytes);
                                aType = new String(aBytes);
                                aLevel = br.readInt();
                                for (JP = 0; JP < yNum; JP++) {
                                    for (IP = 0; IP < xNum; IP++) {
                                        aConc = br.readFloat();
                                        dataArray[IP][JP] = aConc;
                                    }
                                }
                            }
                        } else {
                            if (br.getFilePointer() + 28 > br.length()) {
                                break;
                            }
                            if (_pack_flag == 1) {
                                br.skipBytes(8);
                                br.read(aBytes);
                                aType = new String(aBytes);
                                aLevel = br.readInt();
                                aN = br.readInt();
                                for (k = 0; k < aN; k++) {
                                    if (br.getFilePointer() + 8 > br.length()) {
                                        break;
                                    }
                                    IP = br.readShort();
                                    JP = br.readShort();
                                    br.skipBytes(4);
                                }
                            } else {
                                br.skipBytes(8);
                                br.read(aBytes);
                                aType = new String(aBytes);
                                aLevel = br.readInt();
                                for (JP = 0; JP < yNum; JP++) {
                                    for (IP = 0; IP < xNum; IP++) {
                                        br.skipBytes(4);
                                    }
                                }
                            }
                        }
                    }
                }

                if (br.getFilePointer() + 10 > br.length()) {
                    break;
                }
            }

            br.close();

            double[] newX = this.getXDimension().getValues();
            for (i = 0; i < xNum; i++) {
                for (j = 0; j < yNum; j++) {
                    newDataArray[j][i] = dataArray[i][j];
                }
            }

            GridData gridData = new GridData();
            gridData.data = newDataArray;
            gridData.xArray = newX;
            gridData.yArray = this.getYDimension().getValues();
            gridData.missingValue = this.getMissingValue();

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
