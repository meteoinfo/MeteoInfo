/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.awx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.Extent;
import org.meteoinfo.layer.WorldFilePara;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
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
public class AWXDataInfo extends DataInfo implements IGridDataInfo, IStationDataInfo {

    // <editor-fold desc="Variables">
    private String _dataFileName;
    private int _orderOfInt;
    private int _lenHeadP1;
    private int _lenHeadP2;
    private int _lenFillingData;
    private int _lenRecord;
    private int _numHeadRecord;
    private int _numDataRecord;
    private int _productType;
    private int _zipModel;
    private String _illumination;
    private int _qualityMark;
    ////First level head record - Part 2
    //private string _satelliteName;
    //private int _factorGridField;
    private int _byteGridData;
    //private int _refMarkGridData;
    //private int _scaleGridData;
    //private int _codeTimeFrame;
    private int _baseData = 0;
    private int _scaleFactor = 1;
    private int _startYear;
    private int _startMonth;
    private int _startDay;
    private int _startHour;
    private int _startMinute;
    private int _endYear;
    private int _endMonth;
    private int _endDay;
    private int _endHour;
    private int _endMinute;
    private double _ulLatitude;
    private double _ulLongitude;
    private double _lrLatitude;
    private double _lrLongitude;
    private int _unitGrid;
    private int _spaceLatGrid;
    private int _spaceLonGrid;
    private int _numLatGrid;
    private int _numLonGrid;

    private double _width;
    private double _height;
    private double _lonCenter;
    private double _latCenter;
    private double _xDelt;
    private double _yDelt;
    private double _xLB;
    private double _yLB;

    //private int _ifLandMask;
    //private int _valueLandMask;
    //private int _ifCloudMask;
    //private int _valueCloudMask;
    //private int _ifWaterMask;
    //private int _valueWaterMask;
    //private int _ifIceMask;
    //private int _valueIceMask;
    //private int _ifQCDone;
    //private int _upperLimitQC;
    //private int _lowerQC;
    //private int _standby;
    //private int _fillFlags;
    ////Second level head record
    //private string _fileIdSAT2004;
    //private string _formatVersion;
    //private string _producer;
    //private string _satelliteFlat;
    //private string _instrument;
    //private string _procVersion;
    //private string _reserved;
    //private string _copyRight;
    //private string _lenExtendedFD;
    //private int _sfillFlags;
    //Public
    /// <summary>
    /// start observation time
    /// </summary>
    public Date STime;
    /// <summary>
    /// end observation time
    /// </summary>
    public Date ETime;
    /// <summary>
    /// Image bytes
    /// </summary>
    public byte[] ImageBytes;
    /// <summary>
    /// World file parameter
    /// </summary>
    public WorldFilePara WorldFileP;
    /// <summary>
    /// Variable list
    /// </summary>
    public List<String> VarList;
    /// <summary>
    /// Field list
    /// </summary>
    public List<String> FieldList;

    ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public AWXDataInfo() {
        this.setDataType(MeteoDataType.AWX);
        WorldFileP = new WorldFilePara();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get x number
     *
     * @return X number
     */
    public int getXNum() {
        int xNum = 0;
        switch (_productType) {
            case 1:
                xNum = (int) _width;
                break;
            case 3:
                xNum = _numLonGrid;
                break;
        }
        return xNum;
    }

    /**
     * Get y number
     *
     * @return Y number
     */
    public int getYNum() {
        int yNum = 0;
        switch (_productType) {
            case 1:
                yNum = (int) _height;
                break;
            case 3:
                yNum = _numLatGrid;
                break;
        }
        return yNum;
    }

    /**
     * Get production type ＝1：静止气象卫星图象产品 ＝2：极轨气象卫星图象产品 ＝3：格点场定量产品 ＝4：离散场定量产品
     * ＝5：图形和分析产品
     *
     * @return Production type
     */
    public int getProductType() {
        return this._productType;
    }

    /**
     * Set production type
     *
     * @param value Production type
     */
    public void setProuectType(int value) {
        this._productType = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        this.setFileName(fileName);
        try {
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            //Read first level head record
            //Part 1
            byte[] bytes = new byte[12];
            br.read(bytes);
            this._dataFileName = new String(bytes).trim();
            bytes = new byte[2];
            br.read(bytes);
            _orderOfInt = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            _lenHeadP1 = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            _lenHeadP2 = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            _lenFillingData = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            _lenRecord = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            _numHeadRecord = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            _numDataRecord = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            _productType = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            _zipModel = DataConvert.bytes2Int(bytes, byteOrder);
            bytes = new byte[8];
            br.read(bytes);
            _illumination = new String(bytes).trim();
            br.read(bytes);
            _qualityMark = DataConvert.bytes2Int(bytes, byteOrder);

            //Part 2
            br.seek(0);
            bytes = new byte[_lenHeadP1 + _lenHeadP2];
            br.read(bytes);
            byte[] tbytes = new byte[2];

            if (_productType == 3) {
                tbytes = Arrays.copyOfRange(bytes, 50, 52);
                _byteGridData = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, 52, 54);
                _baseData = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, 54, 56);
                _scaleFactor = DataConvert.bytes2Int(tbytes, byteOrder);
            }

            int yearIdx = 58;
            switch (_productType) {
                case 1:
                    yearIdx = 48;
                    break;
                case 2:

                    break;
                case 3:
                    yearIdx = 58;
                    break;
                case 4:
                    yearIdx = 54;
                    break;
            }

            //Get start time
            tbytes = Arrays.copyOfRange(bytes, yearIdx, yearIdx + 2);
            _startYear = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, yearIdx + 2, yearIdx + 4);
            _startMonth = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, yearIdx + 4, yearIdx + 6);
            _startDay = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, yearIdx + 6, yearIdx + 8);
            _startHour = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, yearIdx + 8, yearIdx + 10);
            _startMinute = DataConvert.bytes2Int(tbytes, byteOrder);
            Calendar cal = Calendar.getInstance();
            cal.set(_startYear, _startMonth, _startDay, _startHour, _startMinute, 0);
            STime = cal.getTime();

            if (_productType == 3 || _productType == 4) //Get end time
            {
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 10, yearIdx + 12);
                _endYear = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 12, yearIdx + 14);
                _endMonth = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 14, yearIdx + 16);
                _endDay = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 16, yearIdx + 18);
                _endHour = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 18, yearIdx + 20);
                _endMinute = DataConvert.bytes2Int(tbytes, byteOrder);
                if (_endYear > 0) {
                    cal.set(_endYear, _endMonth, _endDay, _endHour, _endMinute, 0);
                    ETime = cal.getTime();
                }
            }

            if (_productType == 3) //Get grid parameters
            {
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 20, yearIdx + 22);
                _ulLatitude = DataConvert.bytes2Int(tbytes, byteOrder);
                _ulLatitude = _ulLatitude / 100;
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 22, yearIdx + 24);
                _ulLongitude = DataConvert.bytes2Int(tbytes, byteOrder);
                _ulLongitude = _ulLongitude / 100;
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 24, yearIdx + 26);
                _lrLatitude = DataConvert.bytes2Int(tbytes, byteOrder);
                _lrLatitude = _lrLatitude / 100;
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 26, yearIdx + 28);
                _lrLongitude = DataConvert.bytes2Int(tbytes, byteOrder);
                _lrLongitude = _lrLongitude / 100;
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 28, yearIdx + 30);
                _unitGrid = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 30, yearIdx + 32);
                _spaceLonGrid = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 32, yearIdx + 34);
                _spaceLatGrid = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 34, yearIdx + 36);
                _numLonGrid = DataConvert.bytes2Int(tbytes, byteOrder);
                tbytes = Arrays.copyOfRange(bytes, yearIdx + 36, yearIdx + 38);
                _numLatGrid = DataConvert.bytes2Int(tbytes, byteOrder);

                calCoordinate_3();
            }

            if (_productType == 1) {
                getProjection(bytes);
            }

            br.close();

            //Set variable list
            VarList = new ArrayList<>();
            List<Variable> variables = new ArrayList<>();
            Variable var;
            switch (_productType) {
                case 1:
                case 2:
                case 3:
                    VarList.add("var");
                    var = new Variable();
                    var.setName("var");
                    var.setDimension(this.getYDimension());
                    var.setDimension(this.getXDimension());
                    variables.add(var);
                    break;
                case 4:
                    VarList.add("Latitude");
                    VarList.add("Longitude");
                    VarList.add("Pressure");
                    VarList.add("WindDirection");
                    VarList.add("WindSpeed");
                    VarList.add("Temperature");
                    VarList.add("Slope");
                    VarList.add("Correlation");
                    VarList.add("MiddleRow");
                    VarList.add("MiddleCol");
                    VarList.add("FirstRow");
                    VarList.add("FirstCol");
                    VarList.add("LastRow");
                    VarList.add("LastCol");
                    VarList.add("BrightTemp");
                    Dimension stdim = new Dimension(DimensionType.Other);
                    double[] values = new double[this._numDataRecord];
                    stdim.setValues(values);
                    this.addDimension(stdim);
                    for (String vName : VarList) {
                        var = new Variable();
                        var.setName(vName);
                        var.setStation(true);
                        var.setDimension(stdim);
                        variables.add(var);
                    }

                    FieldList = new ArrayList<>();
                    //FieldList.addAll(Arrays.asList(new String[]{"Stid", "Longitude", "Latitude"}));
                    FieldList.add("Stid");
                    FieldList.addAll(VarList);
                    break;
            }
            this.setVariables(variables);
        } catch (IOException ex) {
            Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getProjection(byte[] bytes) {
        byte[] tbytes = new byte[2];
        if (_productType == 1) //Get grid/projection parameters
        {
            tbytes = Arrays.copyOfRange(bytes, 58, 60);
            int channel = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 60, 62);
            int projType = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 62, 64);
            _width = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 64, 66);
            _height = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 66, 68);
            int ulLineNum = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 68, 70);
            int ulPixelNum = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 70, 72);
            int ratio = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 72, 74);
            _ulLatitude = DataConvert.bytes2Int(tbytes, byteOrder);
            _ulLatitude = _ulLatitude / 100;
            tbytes = Arrays.copyOfRange(bytes, 74, 76);
            _lrLatitude = DataConvert.bytes2Int(tbytes, byteOrder);
            _lrLatitude = _lrLatitude / 100;
            tbytes = Arrays.copyOfRange(bytes, 76, 78);
            _ulLongitude = DataConvert.bytes2Int(tbytes, byteOrder);
            _ulLongitude = _ulLongitude / 100;
            tbytes = Arrays.copyOfRange(bytes, 78, 80);
            _lrLongitude = DataConvert.bytes2Int(tbytes, byteOrder);
            _lrLongitude = _lrLongitude / 100;
            tbytes = Arrays.copyOfRange(bytes, 80, 82);
            _latCenter = DataConvert.bytes2Int(tbytes, byteOrder);
            _latCenter = _latCenter / 100;
            tbytes = Arrays.copyOfRange(bytes, 82, 84);
            _lonCenter = DataConvert.bytes2Int(tbytes, byteOrder);
            _lonCenter = _lonCenter / 100;
            tbytes = Arrays.copyOfRange(bytes, 84, 86);
            float lat1 = DataConvert.bytes2Int(tbytes, byteOrder);
            lat1 = lat1 / 100;
            tbytes = Arrays.copyOfRange(bytes, 86, 88);
            float lat2 = DataConvert.bytes2Int(tbytes, byteOrder);
            lat2 = lat2 / 100;
            tbytes = Arrays.copyOfRange(bytes, 88, 90);
            _xDelt = DataConvert.bytes2Int(tbytes, byteOrder);
            _xDelt = _xDelt / 100;
            tbytes = Arrays.copyOfRange(bytes, 90, 92);
            _yDelt = DataConvert.bytes2Int(tbytes, byteOrder);
            _yDelt = _yDelt / 100;
            tbytes = Arrays.copyOfRange(bytes, 92, 94);
            int hasGeoGrid = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 94, 96);
            int geoGridValue = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 96, 98);
            int lenPallate = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 98, 100);
            int lenVef = DataConvert.bytes2Int(tbytes, byteOrder);
            tbytes = Arrays.copyOfRange(bytes, 100, 102);
            int lenGeo = DataConvert.bytes2Int(tbytes, byteOrder);

            String projStr = this.getProjectionInfo().toProj4String();
            switch (projType) {
                case 0:    //未投影（卫星投影）

                    break;
                case 1:    //兰勃托投影
                    projStr = "+proj=lcc"
                            + "+lon_0=" + String.valueOf(_lonCenter)
                            + "+lat_0=" + String.valueOf(_latCenter)
                            + "+lat_1=" + String.valueOf(lat1)
                            + "+lat_2=" + String.valueOf(lat2);
                    break;
                case 2:    //麦卡托投影
                    projStr = "+proj=merc"
                            + "+lon_0=" + String.valueOf(_lonCenter)
                            + "+lat_ts=" + String.valueOf(lat1);
                    break;
                case 3:    //极射投影
                    projStr = "+proj=stere"
                            + "+lat_0=" + String.valueOf(_latCenter)
                            + "+lon_0=" + String.valueOf(_lonCenter);
                    double k0 = ProjectionInfo.calScaleFactorFromStandardParallel(lat1);
                    projStr += "+k=" + String.valueOf(k0);
                    break;
                case 4:    //等经纬度投影

                    break;
                case 5:    //等面积投影

                    break;
            }

            if (!projStr.equals(this.getProjectionInfo().toProj4String())) {
                this.setProjectionInfo(ProjectionInfo.factory(projStr));
            }

            calCoordinate_1(projType);
        }
    }

    private void calCoordinate_1(int projType) {
        int xNum = (int) _width;
        int yNum = (int) _height;
        switch (projType) {
            case 4:
                _xDelt = (_lrLongitude - _ulLongitude) / xNum;
                _yDelt = (_ulLatitude - _lrLatitude) / yNum;
                _xLB = _ulLongitude + _xDelt;
                _yLB = _lrLatitude + _yDelt;
                break;
            default:
                ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
                double X_Center,
                 Y_Center;
                double[][] points = new double[1][];
                points[0] = new double[]{_lonCenter, _latCenter};
                Reproject.reprojectPoints(points, fromProj, this.getProjectionInfo(), 0, 1);
                X_Center = points[0][0];
                Y_Center = points[0][1];

                _xDelt = _xDelt * 1000;
                _yDelt = _yDelt * 1000;
                _xLB = X_Center - (_xDelt * _width / 2);
                _yLB = Y_Center - (_yDelt * _height / 2);
                break;
        }

        double[] x = new double[xNum];
        double[] y = new double[yNum];
        int i;
        for (i = 0; i < xNum; i++) {
            x[i] = _xLB + _xDelt * i;
        }

        for (i = 0; i < yNum; i++) {
            y[i] = _yLB + _yDelt * i;
        }

        Dimension xdim = new Dimension(DimensionType.X);
        xdim.setShortName("X");
        xdim.setValues(x);
        this.setXDimension(xdim);
        Dimension ydim = new Dimension(DimensionType.Y);
        ydim.setShortName("Y");
        ydim.setValues(y);
        this.setYDimension(ydim);
    }

    private void calCoordinate_3() {
        double width = _lrLongitude - _ulLongitude;
        double height = _ulLatitude - _lrLatitude;

        WorldFileP.xUL = _ulLongitude;
        WorldFileP.yUL = _ulLatitude;
        WorldFileP.xScale = width / _numLonGrid;
        WorldFileP.yScale = -height / _numLatGrid;

        double[] x = new double[_numLonGrid];
        double[] y = new double[_numLatGrid];
        double xDelt = (_lrLongitude - _ulLongitude) / _numLonGrid;
        double yDelt = (_ulLatitude - _lrLatitude) / _numLatGrid;
        int i;
        for (i = 0; i < _numLonGrid; i++) {
            x[i] = _ulLongitude + xDelt * i;
        }

        for (i = 0; i < _numLatGrid; i++) {
            y[i] = _lrLatitude + yDelt * i;
        }
        Dimension xdim = new Dimension(DimensionType.X);
        xdim.setShortName("X");
        xdim.setValues(x);
        this.setXDimension(xdim);
        Dimension ydim = new Dimension(DimensionType.Y);
        ydim.setShortName("Y");
        ydim.setValues(y);
        this.setYDimension(ydim);
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

    @Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "Data Type: AWX";
        if (this._productType != 4) {
            Dimension xdim = this.getXDimension();
            Dimension ydim = this.getYDimension();
            dataInfo += System.getProperty("line.separator") + "XNum = " + String.valueOf(xdim.getLength())
                    + "  YNum = " + String.valueOf(ydim.getLength());
            dataInfo += System.getProperty("line.separator") + "XMin = " + String.valueOf(xdim.getValues()[0])
                    + "  YMin = " + String.valueOf(ydim.getValues()[0]);
            dataInfo += System.getProperty("line.separator") + "XSize = " + String.valueOf(xdim.getValues()[1] - xdim.getValues()[0])
                    + "  YSize = " + String.valueOf(ydim.getValues()[1] - ydim.getValues()[0]);
        }
        dataInfo += System.getProperty("line.separator") + "Product Type = " + String.valueOf(this._productType);
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //dataInfo += System.getProperty("line.separator") + "Start Time = " + format.format(STime);
        //dataInfo += System.getProperty("line.separator") + "End Time = " + format.format(ETime);
        dataInfo += System.getProperty("line.separator") + "Number of Variables = " + String.valueOf(this.getVariableNum());
        for (int i = 0; i < this.getVariableNum(); i++) {
            dataInfo += System.getProperty("line.separator") + "\t" + this.getVariableNames().get(i);
        }

        return dataInfo;
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

    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        try {                    
            if (this._productType == 4){
                Array dataArray = this.read_4(varName);
                dataArray = dataArray.section(origin, size, stride);
                return dataArray;
            }
            
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            int rangeIdx = 0;
            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);
            IndexIterator ii = dataArray.getIndexIterator();
            switch (_productType) {
                case 1:
                    try {
                        this.readXY_1(yRange, xRange, ii);
                    } catch (IOException ex) {
                        Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case 3:
                    try {
                        this.readXY_3(yRange, xRange, ii);
                    } catch (IOException ex) {
                        Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case 4:

                    break;
                default:
                    return null;
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void readXY_1(Range yRange, Range xRange, IndexIterator ii) throws IOException {
        byte[] imageBytes = getIamgeData();

        //Get grid data
        int i, j;
        int xNum = (int) _width;
        int yNum = (int) _height;

        float[] data = new float[yNum * xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                data[(yNum - i - 1) * xNum + j] = DataConvert.byte2Int(imageBytes[i * xNum + j]);
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
    }

    private void readXY_3(Range yRange, Range xRange, IndexIterator ii) throws FileNotFoundException, IOException {
        RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
        //Read byte data 
        br.seek(_lenRecord * _numHeadRecord);
        int length = (int) (br.length() - br.getFilePointer());
        byte[] imageBytes = new byte[length];
        br.read(imageBytes);
        br.close();

        int i, j;
        int bi = 0;
        int value = 0;
        byte[] vbytes = new byte[_byteGridData];
        float[] data = new float[_numLatGrid * _numLonGrid];
        int idx;
        for (i = 0; i < _numLatGrid; i++) {
            for (j = 0; j < _numLonGrid; j++) {
                vbytes = Arrays.copyOfRange(imageBytes, bi, bi + _byteGridData);
                if (_byteGridData == 1) {
                    value = DataConvert.byte2Int(vbytes[0]);
                } else if (_byteGridData == 2) {
                    value = DataConvert.bytes2Int(vbytes, byteOrder);
                } else if (_byteGridData == 4) {
                    value = DataConvert.bytes2Int(vbytes, byteOrder);
                }
                idx = (_numLatGrid - i - 1) * _numLonGrid + j;
                data[idx] = (float) (value + _baseData) / _scaleFactor;
                bi += _byteGridData;
            }
        }

        for (int y = yRange.first(); y <= yRange.last();
                y += yRange.stride()) {
            for (int x = xRange.first(); x <= xRange.last();
                    x += xRange.stride()) {
                int index = y * _numLonGrid + x;
                ii.setFloatNext(data[index]);
            }
        }
    }

    private Array read_4(String varName) {
        try {
            Array a = Array.factory(DataType.FLOAT, new int[]{this._numDataRecord});
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            float t;
            br.seek(_lenRecord * _numHeadRecord);
            long bP = br.getFilePointer();
            byte[] bytes = new byte[2];
            int varIdx = this.VarList.indexOf(varName);
            for (int i = 0; i < _numDataRecord; i++) {
                br.seek(bP);
                if (br.getFilePointer() + _lenRecord > br.length()) {
                    break;
                }
                if (varIdx <= 4) {
                    br.skipBytes(2 * varIdx);
                } else {
                    br.skipBytes(2 * varIdx + 2);
                }
                br.read(bytes);
                t = DataConvert.bytes2Int(bytes, byteOrder);
                switch (varIdx) {
                    case 0:
                    case 1:
                    case 5:
                    case 14:
                        t = t / 100;
                        break;
                    case 6:
                    case 7:
                        t = t / 1000;
                        break;
                }
                a.setFloat(i, t);

                bP = bP + _lenRecord;
            }            
            br.close();

            return a;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        GridData gData = null;
        switch (_productType) {
            case 1: {
                try {
                    gData = getGridData_1();
                } catch (IOException ex) {
                    Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case 3: {
                try {
                    gData = getGridData_3();
                } catch (IOException ex) {
                    Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }

        return gData;
    }

    private GridData getGridData_3() throws FileNotFoundException, IOException {
        RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
        //Read byte data 
        br.seek(_lenRecord * _numHeadRecord);
        int length = (int) (br.length() - br.getFilePointer());
        byte[] imageBytes = new byte[length];
        br.read(imageBytes);
        br.close();

        //Get grid data
        int i, j;
        GridData gridData = new GridData();
        double[] x = new double[_numLonGrid];
        double[] y = new double[_numLatGrid];
        double xDelt = (_lrLongitude - _ulLongitude) / _numLonGrid;
        double yDelt = (_ulLatitude - _lrLatitude) / _numLatGrid;
        for (i = 0; i < _numLonGrid; i++) {
            x[i] = _ulLongitude + xDelt * i;
        }

        for (i = 0; i < _numLatGrid; i++) {
            y[i] = _lrLatitude + yDelt * i;
        }
        gridData.xArray = x;
        gridData.yArray = y;

        double[][] gData = new double[_numLatGrid][_numLonGrid];
        int bi = 0;
        int value = 0;
        byte[] vbytes = new byte[_byteGridData];

        for (i = 0; i < _numLatGrid; i++) {
            for (j = 0; j < _numLonGrid; j++) {
                vbytes = Arrays.copyOfRange(imageBytes, bi, bi + _byteGridData);
                if (_byteGridData == 1) {
                    value = DataConvert.byte2Int(vbytes[0]);
                } else if (_byteGridData == 2) {
                    value = DataConvert.bytes2Int(vbytes, byteOrder);
                } else if (_byteGridData == 4) {
                    value = DataConvert.bytes2Int(vbytes, byteOrder);
                }
                gData[_numLatGrid - i - 1][j] = (double) (value + _baseData) / _scaleFactor;
                bi += _byteGridData;
            }
        }
        gridData.data = gData;

        return gridData;
    }

    private GridData getGridData_1() throws IOException {
        byte[] imageBytes = getIamgeData();

        //Get grid data
        int i, j;
        GridData gridData = new GridData();
        int xNum = (int) _width;
        int yNum = (int) _height;
        double[] x = new double[xNum];
        double[] y = new double[yNum];
        for (i = 0; i < xNum; i++) {
            x[i] = _xLB + _xDelt * i;
        }

        for (i = 0; i < yNum; i++) {
            y[i] = _yLB + _yDelt * i;
        }
        gridData.xArray = x;
        gridData.yArray = y;

        double[][] gData = new double[yNum][xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                gData[yNum - i - 1][j] = DataConvert.byte2Int(imageBytes[i * xNum + j]);
            }
        }
        gridData.data = gData;

        return gridData;
    }

    /**
     * Get image data
     *
     * @return Image data
     * @throws java.io.FileNotFoundException
     */
    public byte[] getIamgeData() throws FileNotFoundException, IOException {
        RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");

        byte[] bytes = new byte[_lenHeadP1 + _lenHeadP2];
        br.read(bytes);
        byte[] tbytes = new byte[2];
        tbytes = Arrays.copyOfRange(bytes, 96, 98);
        int lenPallate = DataConvert.bytes2Int(tbytes, byteOrder);
        tbytes = Arrays.copyOfRange(bytes, 98, 100);
        int lenVef = DataConvert.bytes2Int(tbytes, byteOrder);
        tbytes = Arrays.copyOfRange(bytes, 100, 102);
        int lenGeo = DataConvert.bytes2Int(tbytes, byteOrder);

        //Read byte data 
        br.seek(_lenRecord * _numHeadRecord);
        int length = (int) (br.length() - br.getFilePointer());
        byte[] imageBytes = new byte[length];
        br.read(imageBytes);

        br.close();

        return imageBytes;
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

    @Override
    public StationData getStationData(int timeIdx, int varIdx, int levelIdx) {
        try {
            StationData stationData = new StationData();
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            float lon, lat, t;
            List<String> stations = new ArrayList<>();
            List<double[]> disDataList = new ArrayList<>();
            float minX, maxX, minY, maxY;
            minX = 0;
            maxX = 0;
            minY = 0;
            maxY = 0;
            br.seek(_lenRecord * _numHeadRecord);
            long bP = br.getFilePointer();
            byte[] bytes = new byte[2];
            for (int i = 0; i < _numDataRecord; i++) {
                br.seek(bP);
                if (br.getFilePointer() + _lenRecord > br.length()) {
                    break;
                }

                stations.add(String.valueOf(i));
                br.read(bytes);
                lat = (float) DataConvert.bytes2Int(bytes, byteOrder) / 100;
                br.read(bytes);
                lon = (float) DataConvert.bytes2Int(bytes, byteOrder) / 100;
                switch (varIdx) {
                    case 0:
                        t = lat;
                        break;
                    case 1:
                        t = lon;
                        break;
                    default:
                        int idx = varIdx - 2;
                        if (idx <= 2) {
                            br.skipBytes(2 * idx);
                        } else {
                            br.skipBytes(2 * idx + 2);
                        }
                        br.read(bytes);
                        t = DataConvert.bytes2Int(bytes, byteOrder);
                        switch (idx) {
                            case 3:
                            case 12:
                                t = t / 100;
                                break;
                            case 4:
                            case 5:
                                t = t / 1000;
                                break;
                        }
                        break;
                }
                disDataList.add(new double[]{lon, lat, t});

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

                bP = bP + _lenRecord;
            }
            Extent dataExtent = new Extent();
            dataExtent.minX = minX;
            dataExtent.maxX = maxX;
            dataExtent.minY = minY;
            dataExtent.maxY = maxY;
            br.close();

            double[][] discreteData = new double[disDataList.size()][3];
            for (int i = 0; i < disDataList.size(); i++) {
                discreteData[i][0] = disDataList.get(i)[0];
                discreteData[i][1] = disDataList.get(i)[1];
                discreteData[i][2] = disDataList.get(i)[2];
            }
            stationData.data = discreteData;
            stationData.dataExtent = dataExtent;
            stationData.stations = stations;
            return stationData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private List<List<String>> getStationInfoDataList() throws FileNotFoundException, IOException {
        List<List<String>> stInfoData = new ArrayList<>();
        RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");

        float lon, lat, t;
        List<String> stData;
        br.seek(_lenRecord * 2);
        long bP = br.getFilePointer();
        byte[] bytes = new byte[2];
        for (int i = 0; i < _numDataRecord; i++) {
            br.seek(bP);
            if (br.getFilePointer() + _lenRecord > br.length()) {
                break;
            }

            stData = new ArrayList<>();
            stData.add(String.valueOf(i));
            br.read(bytes);
            lat = (float) DataConvert.bytes2Int(bytes, byteOrder) / 100;
            br.read(bytes);
            lon = (float) DataConvert.bytes2Int(bytes, byteOrder) / 100;
            stData.add(String.valueOf(lon));
            stData.add(String.valueOf(lat));

            for (int j = 0; j < VarList.size(); j++) {
                br.read(bytes);
                t = DataConvert.bytes2Int(bytes, byteOrder);
                switch (j) {
                    case 2:
                        br.skipBytes(2);
                        break;
                    case 3:
                    case 12:
                        t = t / 100;
                        break;
                    case 4:
                    case 5:
                        t = t / 1000;
                        break;
                }
                stData.add(String.valueOf(t));
            }

            stInfoData.add(stData);

            bP = bP + _lenRecord;
        }

        br.close();

        return stInfoData;
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        try {
            StationInfoData stInfoData = new StationInfoData();
            stInfoData.setDataList(getStationInfoDataList());
            stInfoData.setFields(this.FieldList);
            stInfoData.setVariables(this.VarList);

            return stInfoData;
        } catch (IOException ex) {
            Logger.getLogger(AWXDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
