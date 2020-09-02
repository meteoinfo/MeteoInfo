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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.meteoinfo.global.MIMath;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
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
public class MICAPS13DataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private LocalDateTime _time;
    private int _xNum;
    private int _yNum;
    private double _lon_LB;
    private double _lat_LB;
    private double _lon_Center;
    private double _lat_Center;
    private int _projOption;
    private double _zoomFactor;
    private int _imageType;
    private String _tableName;
    private byte[] _imageBytes;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MICAPS13DataInfo(){
        this.setDataType(MeteoDataType.MICAPS_13);
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
            byte[] bytes = new byte[128];
            br.read(bytes);
            String header = new String(bytes, "GBK").trim();
            String[] dataArray = header.split("\\s+");
            this._description = dataArray[2];
            int year = Integer.parseInt(dataArray[3]);
            if (year < 100) {
                if (year > 50) {
                    year = 1900 + year;
                } else {
                    year = 2000 + year;
                }
            }
            LocalDateTime tt = LocalDateTime.of(year, Integer.parseInt(dataArray[4]), Integer.parseInt(dataArray[5]),
                    Integer.parseInt(dataArray[6]), 0, 0);
            _time = tt;
            _xNum = Integer.parseInt(dataArray[7]);
            _yNum = Integer.parseInt(dataArray[8]);
            _lon_LB = Double.parseDouble(dataArray[9]);
            _lat_LB = Double.parseDouble(dataArray[10]);
            _projOption = Integer.parseInt(dataArray[11]);
            _zoomFactor = Double.parseDouble(dataArray[12]);
            _imageType = Integer.parseInt(dataArray[13]);
            _tableName = dataArray[14];
            if (MIMath.isNumeric(dataArray[15]) && MIMath.isNumeric(dataArray[16])) {
                _lon_Center = Double.parseDouble(dataArray[15]);
                _lat_Center = Double.parseDouble(dataArray[16]);
                if (_lon_Center > 180) {
                    _lon_Center = _lon_Center / 100;
                }
                if (_lat_Center > 90) {
                    _lat_Center = _lat_Center / 100;
                }
            } else {
                _lon_Center = 110.0;
                _lat_Center = 30.0;
            }

            //Read image data    
            int length = (int) br.length() - 128;
            _imageBytes = new byte[length];
            br.read(_imageBytes);

            //Get projection and coordinate
            this.setProjectionInfo(this.getProjectionInfo(_lon_Center, _lat_Center, _projOption));
            Object[] coords = this.calCoordinate(_lon_LB, _lat_LB, _lon_Center, _lat_Center, _xNum, _yNum);
            br.close();

            Dimension tdim = new Dimension(DimensionType.T);
            tdim.addValue(JDateUtil.toOADate(_time));
            this.setTimeDimension(tdim);
            this.addDimension(tdim);
            Dimension ydim = new Dimension(DimensionType.Y);
            ydim.setValues((double[]) coords[1]);
            this.addDimension(ydim);
            this.setYDimension(ydim);
            Dimension xdim = new Dimension(DimensionType.X);
            xdim.setValues((double[]) coords[0]);
            this.addDimension(xdim);
            this.setXDimension(xdim);

            Variable var = new Variable();
            var.setName("var");
            var.setDimension(tdim);
            var.setDimension(ydim);
            var.setDimension(xdim);
            this.addVariable(var);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS13DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS13DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ProjectionInfo getProjectionInfo(double lon_Center, double lat_Center, int projOption) {
        String ProjStr = "+proj=lcc"
                + "+lat_1=" + String.valueOf(lat_Center)
                + "+lat_2=60"
                + "+lat_0=0"
                + "+lon_0=" + String.valueOf(lon_Center)
                + "+x_0=0"
                + "+y_0=0";
        switch (projOption) {
            case 1:
                ProjStr = "+proj=lcc"
                        + "+lat_1=" + String.valueOf(lat_Center)
                        + "+lat_2=60"
                        + "+lat_0=0"
                        + "+lon_0=" + String.valueOf(lon_Center)
                        + "+x_0=0"
                        + "+y_0=0";
                break;
            case 2:
                ProjStr = "+proj=merc"
                        + "+lon_0=" + String.valueOf(lon_Center);
                break;
            case 3:
                ProjStr = "+proj=stere"
                        + "+lat_0=90"
                        + "+lon_0=" + String.valueOf(lon_Center);
                break;
            case 4:
                ProjStr = "+proj=stere"
                        + "+lat_0=-90"
                        + "+lon_0=" + String.valueOf(lon_Center);
                break;
        }

        return ProjectionInfo.factory(ProjStr);
    }

    private Object[] calCoordinate(double lon_LB, double lat_LB, double lon_Center, double lat_Center,
            int xNum, int yNum) {
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        double X_LB, Y_LB;
        double[][] points = new double[1][];
        points[0] = new double[]{lon_LB, lat_LB};
        Reproject.reprojectPoints(points, fromProj, this.getProjectionInfo(), 0, 1);
        X_LB = points[0][0];
        Y_LB = points[0][1];

        double X_Center, Y_Center;
        points = new double[1][];
        points[0] = new double[]{lon_Center, lat_Center};
        Reproject.reprojectPoints(points, fromProj, this.getProjectionInfo(), 0, 1);
        X_Center = points[0][0];
        Y_Center = points[0][1];

        double[] X = new double[xNum];
        double[] Y = new double[yNum];
        double xMax = X_Center + (X_Center - X_LB);
        double yMax = Y_Center + (Y_Center - Y_LB);
        double width = xMax - X_LB;
        double height = yMax - Y_LB;
        double xDelt = width / (xNum - 1);
        double yDelt = height / (yNum - 1);
        int i;
        for (i = 0; i < xNum; i++) {
            X[i] = X_LB + i * xDelt;
        }
        for (i = 0; i < yNum; i++) {
            Y[i] = Y_LB + i * yDelt;
        }

        return new Object[]{X, Y};
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
        String dataInfo = "";
        dataInfo += "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "Description: " + _description;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dataInfo += System.getProperty("line.separator") + "Time: " + format.format(_time);
        dataInfo += System.getProperty("line.separator") + "X number: " + String.valueOf(_xNum);
        dataInfo += System.getProperty("line.separator") + "Y number: " + String.valueOf(_yNum);
        dataInfo += System.getProperty("line.separator") + "Left-Bottom longitude: " + String.valueOf(_lon_LB);
        dataInfo += System.getProperty("line.separator") + "Left-Bottom latitude: " + String.valueOf(_lat_LB);
        dataInfo += System.getProperty("line.separator") + "Center longitude: " + String.valueOf(_lon_Center);
        dataInfo += System.getProperty("line.separator") + "Center latitude: " + String.valueOf(_lat_Center);
        dataInfo += System.getProperty("line.separator") + "Projection: " + getProjectionString(_projOption);
        dataInfo += System.getProperty("line.separator") + "Zoom factor: " + String.valueOf(_zoomFactor);
        dataInfo += System.getProperty("line.separator") + "Image type: " + getImageType(_imageType);
        dataInfo += System.getProperty("line.separator") + "Table name: " + _tableName;
        dataInfo += System.getProperty("line.separator") + super.generateInfoText();

        return dataInfo;
    }

    private String getProjectionString(int proj) {
        String projStr = "Lon/Lat";
        switch (proj) {
            case 1:
                projStr = "Lambert";
                break;
            case 2:
                projStr = "Mecator";
                break;
            case 3:
                projStr = "NorthPolar";
                break;
            case 4:
                projStr = "SourthPolar";
                break;
        }

        return projStr;
    }

    private String getImageType(int iType) {
        String imageType = "1—红外云图 2—雷达拼图 3—地形图 4—可见光云图 5—水汽图";
        switch (iType) {
            case 1:
                imageType = "红外云图";
                break;
            case 2:
                imageType = "雷达拼图";
                break;
            case 3:
                imageType = "地形图";
                break;
            case 4:
                imageType = "可见光云图";
                break;
            case 5:
                imageType = "水汽图";
                break;
        }

        return imageType;
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
            Array dataArray = Array.factory(DataType.INT, section.getShape());
            int rangeIdx = 1;
            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);
            IndexIterator ii = dataArray.getIndexIterator();
            int xNum = this._xNum;
            int index;
            for (int y = yRange.first(); y <= yRange.last();
                    y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last();
                        x += xRange.stride()) {
                    index = y * xNum + x;
                    ii.setIntNext(DataConvert.byte2Int(_imageBytes[index]));
                }
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(MICAPS4DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
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
        GridData gridData = new GridData();
        double[][] gData = new double[_yNum][_xNum];
        for (int i = 0; i < _yNum; i++) {
            for (int j = 0; j < _xNum; j++) {
                gData[i][j] = DataConvert.byte2Int(_imageBytes[i * _xNum + j]);
            }
        }
        gridData.data = gData;
        gridData.xArray = this.getXDimension().getValues();
        gridData.yArray = this.getYDimension().getValues();

        return gridData;
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
