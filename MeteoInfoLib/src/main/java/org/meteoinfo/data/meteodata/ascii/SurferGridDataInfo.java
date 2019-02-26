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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.meteodata.MeteoDataType;
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
public class SurferGridDataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public SurferGridDataInfo() {
        this.setDataType(MeteoDataType.Sufer_Grid);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        try {
            this.setFileName(fileName);

            BufferedReader sr = new BufferedReader(new FileReader(new File(fileName)));
            double xmin, ymin, xmax, ymax, zmin, zmax;
            int xnum, ynum, i;
            String aLine;
            String[] dataArray;

            aLine = sr.readLine().trim();
            for (i = 1; i <= 4; i++) {
                aLine = aLine + " " + sr.readLine().trim();
            }

            dataArray = aLine.split("\\s+");
            xnum = Integer.parseInt(dataArray[1]);
            ynum = Integer.parseInt(dataArray[2]);
            xmin = Double.parseDouble(dataArray[3]);
            xmax = Double.parseDouble(dataArray[4]);
            ymin = Double.parseDouble(dataArray[5]);
            ymax = Double.parseDouble(dataArray[6]);
            zmin = Double.parseDouble(dataArray[7]);
            zmax = Double.parseDouble(dataArray[8]);

            double xdelt = (xmax - xmin) / (xnum - 1);
            double ydelt = (ymax - ymin) / (ynum - 1);
            double[] X = new double[xnum];
            for (i = 0; i < xnum; i++) {
                X[i] = xmin + i * xdelt;
            }
            if (X[xnum - 1] + xdelt - X[0] == 360) {
                this.setGlobal(true);
            }

            double[] Y = new double[ynum];
            for (i = 0; i < ynum; i++) {
                Y[i] = ymin + i * ydelt;
            }

            Dimension xDim = new Dimension(DimensionType.X);
            xDim.setValues(X);
            this.setXDimension(xDim);
            Dimension yDim = new Dimension(DimensionType.Y);
            yDim.setValues(Y);
            this.setYDimension(yDim);

            List<Variable> variables = new ArrayList<>();
            Variable aVar = new Variable();
            aVar.setName("var");            
            aVar.addDimension(yDim);
            aVar.addDimension(xDim);
            variables.add(aVar);
            this.setVariables(variables);

            sr.close();
        } catch (IOException ex) {
            Logger.getLogger(SurferGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        dataInfo = "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "Data Type: Sufer ASCII Grid";
        Dimension xdim = this.getXDimension();
        Dimension ydim = this.getYDimension();
        dataInfo += System.getProperty("line.separator") + "XNum = " + String.valueOf(xdim.getLength())
                + "  YNum = " + String.valueOf(ydim.getLength());
        dataInfo += System.getProperty("line.separator") + "XMin = " + String.valueOf(xdim.getValues()[0])
                + "  YMin = " + String.valueOf(ydim.getValues()[0]);
        dataInfo += System.getProperty("line.separator") + "XSize = " + String.valueOf(xdim.getValues()[1] - xdim.getValues()[0])
                + "  YSize = " + String.valueOf(ydim.getValues()[1] - ydim.getValues()[0]);
        dataInfo += System.getProperty("line.separator") + "UNDEF = " + String.valueOf(this.getMissingValue());

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
            int rangeIdx = 0;
            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);
            IndexIterator ii = dataArray.getIndexIterator();
            readXY(yRange, xRange, ii);

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(SurferGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void readXY(Range yRange, Range xRange, IndexIterator ii) {
        try {
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            float[] data = new float[yNum * xNum];
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            String[] dataArray;
            int i, j;
            String aLine;

            for (i = 0; i < 5; i++) {
                sr.readLine();
            }

            int idx = 0;
            aLine = sr.readLine();
            while (aLine != null) {
                dataArray = aLine.trim().split("\\s+");
                for (String dstr : dataArray) {
                    data[idx] = Float.parseFloat(dstr);
                    idx += 1;
                }
                aLine = sr.readLine();
            }
            sr.close();

            for (int y = yRange.first(); y <= yRange.last();
                    y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last();
                        x += xRange.stride()) {
                    int index = y * xNum + x;
                    ii.setFloatNext(data[index]);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SurferGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SurferGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        try {
            int xNum = this.getXDimension().getLength();
            int yNum = this.getYDimension().getLength();
            double[][] theData = new double[yNum][xNum];
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            String[] dataArray;
            int i, j;
            String aLine;

            for (i = 0; i < 5; i++) {
                sr.readLine();
            }

            int ii, jj;
            int d = 0;
            aLine = sr.readLine();
            while (aLine != null) {
                if (aLine.isEmpty()){
                    aLine = sr.readLine();
                    continue;
                }
                dataArray = aLine.trim().split("\\s+");
                for (String dstr : dataArray) {
                    ii = d / xNum;
                    jj = d % xNum;
                    if (ii >= yNum) {
                        d += 1;
                        break;
                    }
                    if (jj >= xNum) {
                        d += 1;
                        break;
                    }
                    theData[ii][jj] = Double.parseDouble(dstr);
                    d += 1;
                }
                aLine = sr.readLine();
            }

            sr.close();

            GridData aGridData = new GridData();
            aGridData.data = theData;
            aGridData.xArray = this.getXDimension().getValues();
            aGridData.yArray = this.getYDimension().getValues();
            aGridData.missingValue = this.getMissingValue();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(SurferGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
