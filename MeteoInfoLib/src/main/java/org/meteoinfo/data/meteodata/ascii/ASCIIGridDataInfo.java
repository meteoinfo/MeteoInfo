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
package org.meteoinfo.data.meteodata.ascii;

import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.MIMath;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.global.util.BigDecimalUtil;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;

/**
 *
 * @author yaqiang
 */
public class ASCIIGridDataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private DataType dataType;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ASCIIGridDataInfo() {
        this.setDataType(MeteoDataType.ASCII_Grid);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        try {
            this.setFileName(fileName);

            BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
            double xllCorner, yllCorner, cellSize, nodata_value;
            int ncols, nrows, i;
            String aLine;
            String[] dataArray;

            aLine = sr.readLine();
            for (i = 1; i <= 5; i++) {
                aLine = aLine + " " + sr.readLine();
            }
            dataArray = aLine.split("\\s+");
            ncols = Integer.parseInt(dataArray[1]);
            nrows = Integer.parseInt(dataArray[3]);
            xllCorner = Double.parseDouble(dataArray[5]);
            yllCorner = Double.parseDouble(dataArray[7]);
            cellSize = Double.parseDouble(dataArray[9]);
            nodata_value = Double.parseDouble(dataArray[11]);

            aLine = sr.readLine();
            aLine = aLine.trim();
            if (aLine.length() > 7 && aLine.substring(0, 7).equalsIgnoreCase("version")){
                aLine = sr.readLine();
            }
            dataArray = aLine.split("\\s+");
            boolean isInt = true;
            for (String dd : dataArray) {
                if (dd.contains(".")) {
                    isInt = false;
                    break;
                }
            }

            this.setMissingValue(nodata_value);
            double[] X = new double[ncols];
            X[0] = xllCorner;
            for (i = 1; i < ncols; i++) {
                X[i] = BigDecimalUtil.add(X[i - 1], cellSize);
            }
            if (X[ncols - 1] + cellSize - X[0] == 360) {
                this.setGlobal(true);
            }

            double[] Y = new double[nrows];
            Y[0] = yllCorner;
            for (i = 1; i < nrows; i++) {
                Y[i] = BigDecimalUtil.add(Y[i-1], cellSize);
            }

            this.addAttribute(new Attribute("data_format", "ASCII grid data"));

            Dimension xDim = new Dimension(DimensionType.X);
            xDim.setShortName("X");
            xDim.setValues(X);
            this.setXDimension(xDim);
            this.addDimension(xDim);
            Dimension yDim = new Dimension(DimensionType.Y);
            yDim.setShortName("Y");
            yDim.setValues(Y);
            this.setYDimension(yDim);
            this.addDimension(yDim);

            List<Variable> variables = new ArrayList<>();
            Variable aVar = new Variable();
            aVar.setName("var");
            aVar.addDimension(yDim);
            aVar.addDimension(xDim);
            aVar.setFillValue(nodata_value);
            if (isInt) {
                this.dataType = DataType.INT;
            } else {
                this.dataType = DataType.FLOAT;
            }
            this.dataType = DataType.FLOAT;
            aVar.setDataType(dataType);
            aVar.addAttribute(new Attribute("fill_value", this.getMissingValue()));
            variables.add(aVar);
            this.setVariables(variables);

            sr.close();
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(this.dataType, section.getShape());
            int rangeIdx = 0;
            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);
            //IndexIterator ii = dataArray.getIndexIterator();
            readXY(yRange, xRange, dataArray);

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void readXY(Range yRange, Range xRange, Array data) {
        try {
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            String[] dataArray;
            int i, j;
            String aLine;

            for (i = 0; i < 6; i++) {
                sr.readLine();
            }

            List<String> dataList = new ArrayList<>();
            int row = 0;
            int nrow;
            int drow = 0;
            int idx, ii;
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                dataArray = aLine.trim().split("\\s+");
                dataList.addAll(Arrays.asList(dataArray));
                if (row == 0) {
                    if (!MIMath.isNumeric(dataList.get(0))) {
                        aLine = sr.readLine();
                        dataArray = aLine.trim().split("\\s+");
                        dataList = Arrays.asList(dataArray);
                    }
                }
                for (i = 0; i < 100; i++) {
                    if (dataList.size() < xNum) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.trim().split("\\s+");
                        dataList.addAll(Arrays.asList(dataArray));
                    } else {
                        break;
                    }
                }
                nrow = yNum - row - 1;
                if (nrow >= yRange.first() && nrow <= yRange.last()) {
                    if ((nrow - yRange.first()) % yRange.stride() == 0) {
                        idx = (yRange.length() - drow - 1) * xRange.length();
                        for (i = xRange.first(); i <= xRange.last(); i += xRange.stride()) {
                            if (this.dataType == DataType.INT) {
                                data.setObject(idx, Integer.parseInt(dataList.get(i)));
                            } else {
                                data.setObject(idx, Float.parseFloat(dataList.get(i)));
                            }
                            idx += 1;
                        }
                        drow += 1;
                    }
                }
                if (dataList.size() > xNum) {
                    dataList = dataList.subList(xNum, dataList.size() - 1);
                } else {
                    dataList = new ArrayList<>();
                }
                row += 1;
            } while (aLine != null);

            sr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readXY_bak1(Range yRange, Range xRange, IndexIterator ii) {
        try {
            int xNum = this.getXDimension().getLength();
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            String[] dataArray;
            int i, j;
            String aLine;

            for (i = 0; i < 6; i++) {
                sr.readLine();
            }

            List<String> dataList = new ArrayList<>();
            int row = 0;
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                dataArray = aLine.trim().split("\\s+");
                dataList.addAll(Arrays.asList(dataArray));
                if (row == 0) {
                    if (!MIMath.isNumeric(dataList.get(0))) {
                        aLine = sr.readLine();
                        dataArray = aLine.trim().split("\\s+");
                        dataList = Arrays.asList(dataArray);
                    }
                }
                for (i = 0; i < 100; i++) {
                    if (dataList.size() < xNum) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.trim().split("\\s+");
                        dataList.addAll(Arrays.asList(dataArray));
                    } else {
                        break;
                    }
                }
                if (row >= yRange.first() && row <= yRange.last()) {
                    if ((row - yRange.first()) % yRange.stride() == 0) {
                        for (i = xRange.first(); i <= xRange.last(); i += xRange.stride()) {
                            if (this.dataType == DataType.INT) {
                                ii.setObjectNext(Integer.parseInt(dataList.get(i)));
                            } else {
                                ii.setObjectNext(Float.parseFloat(dataList.get(i)));
                            }
                        }
                    }
                }
                if (dataList.size() > xNum) {
                    dataList = dataList.subList(xNum, dataList.size() - 1);
                } else {
                    dataList = new ArrayList<>();
                }
                row += 1;
            } while (aLine != null);

            sr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readXY_bak(Range yRange, Range xRange, IndexIterator ii) {
        try {
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            float[][] theData = new float[yNum][xNum];
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            String[] dataArray;
            int i, j;
            String aLine;

            for (i = 0; i < 6; i++) {
                sr.readLine();
            }

            List<String> dataList = new ArrayList<>();
            int col = 0;
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                dataArray = aLine.trim().split("\\s+");
                dataList.addAll(Arrays.asList(dataArray));
                if (col == 0) {
                    if (!MIMath.isNumeric(dataList.get(0))) {
                        aLine = sr.readLine();
                        dataArray = aLine.trim().split("\\s+");
                        dataList = Arrays.asList(dataArray);
                    }
                }
                for (i = 0; i < 100; i++) {
                    if (dataList.size() < xNum) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.trim().split("\\s+");
                        dataList.addAll(Arrays.asList(dataArray));
                    } else {
                        break;
                    }
                }
                for (i = 0; i < xNum; i++) {
                    theData[col][i] = Float.parseFloat(dataList.get(i));
                }
                if (dataList.size() > xNum) {
                    dataList = dataList.subList(xNum, dataList.size() - 1);
                } else {
                    dataList = new ArrayList<>();
                }
                col += 1;
            } while (aLine != null);

            sr.close();

            float[] data = new float[yNum * xNum];
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    data[i * xNum + j] = theData[yNum - 1 - i][j];
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
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        GridArray ga = new GridArray();
        ga.data = this.read(varName);
        ga.xArray = this.getXDimension().getValues();
        ga.yArray = this.getYDimension().getValues();
        ga.missingValue = this.getMissingValue();

        return ga;
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        try {
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            double[][] theData = new double[yNum][xNum];
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            String[] dataArray;
            int i, j;
            String aLine;

            for (i = 0; i < 6; i++) {
                sr.readLine();
            }

            List<String> dataList = new ArrayList<>();
            int col = 0;
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                dataArray = aLine.trim().split("\\s+");
                dataList.addAll(Arrays.asList(dataArray));
                if (col == 0) {
                    if (!MIMath.isNumeric(dataList.get(0))) {
                        aLine = sr.readLine();
                        dataArray = aLine.trim().split("\\s+");
                        dataList = Arrays.asList(dataArray);
                    }
                }
                for (i = 0; i < 100; i++) {
                    if (dataList.size() < xNum) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.trim().split("\\s+");
                        dataList.addAll(Arrays.asList(dataArray));
                    } else {
                        break;
                    }
                }
                for (i = 0; i < xNum; i++) {
                    theData[col][i] = Double.parseDouble(dataList.get(i));
                }
                if (dataList.size() > xNum) {
                    dataList = dataList.subList(xNum, dataList.size() - 1);
                } else {
                    dataList = new ArrayList<>();
                }
                col += 1;
            } while (aLine != null);

            sr.close();

            double[][] newGridData = new double[yNum][xNum];
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    newGridData[i][j] = theData[yNum - 1 - i][j];
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = newGridData;
            aGridData.xArray = this.getXDimension().getValues();
            aGridData.yArray = this.getYDimension().getValues();
            aGridData.missingValue = this.getMissingValue();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, int varIdx, int lonIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
