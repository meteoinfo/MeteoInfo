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
package org.meteoinfo.data.meteodata.grads;

import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.io.EndianDataOutputStream;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.arl.ARLDataInfo;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.Range;
import org.meteoinfo.ndarray.Section;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.global.util.JDateUtil;

/**
 *
 * @author Yaqiang Wang
 */
public class GrADSDataInfo extends DataInfo implements IGridDataInfo, IStationDataInfo {
    // <editor-fold desc="Variables">

    /// <summary>
    /// Descriptor
    /// </summary>
    public String DESCRIPTOR;
    /// <summary>
    /// Data file name
    /// </summary>
    public String DSET;
    /// <summary>
    /// Is Lat/Lon
    /// </summary>
    public boolean isLatLon;
    /// <summary>
    /// Projection info
    /// </summary>
    //public ProjectionInfo ProjInfo;
    /// <summary>
    /// If rotate vector
    /// </summary>
    public boolean EarthWind;
    /// <summary>
    /// Data type
    /// </summary>
    public String DTYPE;
    /// <summary>
    /// Options
    /// </summary>
    public Options OPTIONS;
    /// <summary>
    /// Title
    /// </summary>
    public String TITLE;
    /// <summary>
    /// Projection set
    /// </summary>
    public PDEFS PDEF;
    /// <summary>
    /// X set
    /// </summary>
    public XDEFS XDEF = new XDEFS();
    /// <summary>
    /// Y set
    /// </summary>
    public YDEFS YDEF = new YDEFS();
    /// <summary>
    /// Level set
    /// </summary>
    public ZDEFS ZDEF = new ZDEFS();
    /// <summary>
    /// Time set
    /// </summary>
    public TDEFS TDEF = new TDEFS();
    private List<String> ensNames = new ArrayList<>();
    /// <summary>
    /// Variable set
    /// </summary>
    public VARDEFS VARDEF = new VARDEFS();
    /// <summary>
    /// A header record of length bytes that precedes the data
    /// </summary>
    public int FILEHEADER;
    /// <summary>
    /// A header record of length bytes preceding each time block of binary data
    /// </summary>
    public int THEADER;
    /// <summary>
    /// A header record of length bytes preceding each horizontal grid (XY block) of binary data
    /// </summary>
    public int XYHEADER;
    /// <summary>
    /// Is global
    /// </summary>
    public boolean isGlobal;
    /// <summary>
    /// Record length in bytes for x/y varying grid
    /// </summary>
    public int RecordLen;
    /// <summary>
    /// Record length per time
    /// </summary>
    public long RecLenPerTime;
    /// <summary>
    /// X coordinate
    /// </summary>
    public double[] X;
    /// <summary>
    /// Y coordinate
    /// </summary>
    public double[] Y;
    /// <summary>
    /// X coordinate number
    /// </summary>
    public int XNum;
    /// <summary>
    /// Y coordinate number
    /// </summary>
    public int YNum;
    private DataOutputStream _bw = null;
    private ByteOrder _byteOrder = ByteOrder.LITTLE_ENDIAN;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GrADSDataInfo() {
        DTYPE = "Gridded";
        TITLE = "null";
        this.DESCRIPTOR = "null";
        OPTIONS = new Options();
        FILEHEADER = 0;
        THEADER = 0;
        XYHEADER = 0;
        isGlobal = false;
        isLatLon = true;
        PDEF = new PDEFS();
        //ProjInfo = KnownCoordinateSystems.geographic.world.WGS1984;
        EarthWind = true;
        this.setDataType(MeteoDataType.GrADS_Grid);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get variable name list
     *
     * @return Variable names
     */
    public List<String> getVarNames() {
        List<String> varList = new ArrayList<>();
        for (Variable aVar : VARDEF.getVars()) {
            varList.add(aVar.getName());
        }

        return varList;
    }

    /**
     * Get variable list they have upper levels
     *
     * @return Upper variables
     */
    public List<Variable> getUpperVariables() {
        List<Variable> uVarList = new ArrayList<>();
        for (Variable aVar : VARDEF.getVars()) {
            if (aVar.getLevelNum() > 1) {
                uVarList.add(aVar);
            }
        }

        return uVarList;
    }

    /**
     * Get variable name list they have upper levels
     *
     * @return Upper variable names
     */
    public List<String> getUpperVariableNames() {
        List<String> uVarList = new ArrayList<>();
        for (Variable aVar : VARDEF.getVars()) {
            if (aVar.getLevelNum() > 1) {
                uVarList.add(aVar.getName());
            }
        }

        return uVarList;
    }

    /**
     * Get time list
     *
     * @return Times
     */
    @Override
    public List<LocalDateTime> getTimes() {
        return TDEF.times;
    }

    /**
     * Get if is big endian
     *
     * @return Boolean
     */
    public boolean isBigEndian() {
        return _byteOrder == ByteOrder.BIG_ENDIAN;
    }

    /**
     * Set if is big endian
     *
     * @param value Boolean
     */
    public void setBigEndian(boolean value) {
        if (value) {
            _byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
            _byteOrder = ByteOrder.LITTLE_ENDIAN;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Read and write data">

    public static boolean canOpen(String fileName) {
        try {
            BufferedReader sr = new BufferedReader(new FileReader(new File(fileName)));
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Read GrADS data info
     *
     * @param aFile The control file path
     */
    @Override
    public void readDataInfo(String aFile) {
        String eStr = "";
        try {
            readDataInfo(aFile, eStr);
            if (OPTIONS.big_endian || OPTIONS.byteswapped) {
                _byteOrder = ByteOrder.BIG_ENDIAN;
            }
            this.setTimes(TDEF.times);
            //this.setVariables(VARDEF.getVars());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Read GrADS control file
     *
     * @param aFile The control file
     * @param errorStr Error string
     * @return If read corrected
     */
    private boolean readDataInfo(String aFile, String errorStr) throws FileNotFoundException, IOException {
        this.setFileName(aFile);
        BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
        String aLine = "";
        String[] dataArray;
        int i;
        boolean isEnd = false;

        //Set dufault value
        DESCRIPTOR = aFile;
        boolean isReadLine = true;
        Dimension zDim;
        Dimension eDim = null;
        this.addAttribute(new Attribute("data_format", "GrADS binary"));
        do {
            if (isReadLine) {
                aLine = sr.readLine().trim();
                if (aLine.isEmpty()) {
                    continue;
                }
            }
            isReadLine = true;
            dataArray = aLine.split("\\s+");
            String hStr = dataArray[0].toUpperCase();
            switch (hStr) {
                case "DSET":
                    DSET = dataArray[1];
                    boolean isNotPath = false;
                    if (!DSET.contains("/") && !DSET.contains("\\")) {
                        isNotPath = true;
                    }
                    File theFile = new File(aFile);
                    String aDir = theFile.getParent();
                    if (isNotPath) {
                        if (DSET.substring(0, 1).equals("^")) {
                            DSET = aDir + File.separator + DSET.substring(1);
                        } else {
                            DSET = aDir + File.separator + DSET;
                        }

                        if (!new File(DSET).isFile()) {
                            DSET = dataArray[1];
                            DSET = theFile.getParent() + "/"
                                    + DSET.substring(1, DSET.length());
                        }
                    }
                    if (!new File(DSET).isFile()) {
                        if (DSET.substring(0, 2).equals("./") || DSET.substring(0, 2).equals(".\\")) {
                            DSET = aDir + File.separator + DSET.substring(2);
                        } else {
                            errorStr = "The data file is not exist!" + System.getProperty("line.separator") + DSET;
                            System.out.println(errorStr);
                        }
                        //goto ERROR;
                    }
                    break;
                case "DTYPE":
                    DTYPE = dataArray[1];
                    if (!DTYPE.toUpperCase().equals("GRIDDED") && !DTYPE.toUpperCase().equals("STATION")) {
                        errorStr = "The data type is not supported at present!" + System.getProperty("line.separator")
                                + DTYPE;
                        //goto ERROR;
                    }
                    if (DTYPE.toUpperCase().equals("STATION")) {
                        this.setDataType(MeteoDataType.GrADS_Station);
                    }
                    Attribute attr = new Attribute("data_type", this.DTYPE);
                    this.addAttribute(attr);
                    break;
                case "OPTIONS":
                    for (i = 1; i < dataArray.length; i++) {
                        String oStr = dataArray[i].toLowerCase();
                        switch (oStr) {
                            case "big_endian":
                                OPTIONS.big_endian = true;
                                break;
                            case "byteswapped":
                                OPTIONS.byteswapped = true;
                                break;
                            case "365_day_calendar":
                                OPTIONS.calendar_365_day = true;
                                break;
                            case "cray_32bit_ieee":
                                OPTIONS.cray_32bit_ieee = true;
                                break;
                            case "little_endian":
                                OPTIONS.little_endian = true;
                                break;
                            case "pascals":
                                OPTIONS.pascals = true;
                                break;
                            case "sequential":
                                OPTIONS.sequential = true;
                                break;
                            case "template":
                                OPTIONS.template = true;
                                break;
                            case "yrev":
                                OPTIONS.yrev = true;
                                break;
                            case "zrev":
                                OPTIONS.zrev = true;
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case "UNDEF":
                    this.setMissingValue(Double.parseDouble(dataArray[1]));
                    attr = new Attribute("fill_value", this.getMissingValue());
                    this.addAttribute(attr);
                    break;
                case "TITLE":
                    TITLE = aLine.substring(5, aLine.length()).trim();
                    attr = new Attribute("title", this.TITLE);
                    this.addAttribute(attr);
                    break;
                case "FILEHEADER":
                    FILEHEADER = Integer.parseInt(dataArray[1]);
                    break;
                case "THEADER":
                    THEADER = Integer.parseInt(dataArray[1]);
                    break;
                case "XYHEADER":
                    XYHEADER = Integer.parseInt(dataArray[1]);
                    break;
                case "PDEF":
                    PDEF.PDEF_Type = dataArray[3].toUpperCase();
                    String ProjStr;
                    ProjectionInfo theProj;
                    String pStr = PDEF.PDEF_Type;
                    switch (pStr) {
                        case "LCC":
                        case "LCCR": {
                            PDEF_LCC aPLCC = new PDEF_LCC();
                            aPLCC.isize = Integer.parseInt(dataArray[1]);
                            aPLCC.jsize = Integer.parseInt(dataArray[2]);
                            aPLCC.latref = Float.parseFloat(dataArray[4]);
                            aPLCC.lonref = Float.parseFloat(dataArray[5]);
                            aPLCC.iref = Float.parseFloat(dataArray[6]);
                            aPLCC.jref = Float.parseFloat(dataArray[7]);
                            aPLCC.Struelat = Float.parseFloat(dataArray[8]);
                            aPLCC.Ntruelat = Float.parseFloat(dataArray[9]);
                            aPLCC.slon = Float.parseFloat(dataArray[10]);
                            aPLCC.dx = Float.parseFloat(dataArray[11]);
                            aPLCC.dy = Float.parseFloat(dataArray[12]);
                            PDEF.PDEF_Content = aPLCC;
                            isLatLon = false;
                            ProjStr = "+proj=lcc"
                                    + " +lat_1=" + String.valueOf(aPLCC.Struelat)
                                    + " +lat_2=" + String.valueOf(aPLCC.Ntruelat)
                                    + " +lat_0=" + String.valueOf(aPLCC.latref)
                                    + " +lon_0=" + String.valueOf(aPLCC.slon);
                            theProj = ProjectionInfo.factory(ProjStr);
                            this.setProjectionInfo(theProj);
                            if (PDEF.PDEF_Type.equals("LCCR")) {
                                EarthWind = false;
                            }       //Set X Y
                            XNum = aPLCC.isize;
                            YNum = aPLCC.jsize;
                            X = new double[aPLCC.isize];
                            Y = new double[aPLCC.jsize];
                            getProjectedXY(theProj, aPLCC.dx, aPLCC.dy, aPLCC.iref, aPLCC.jref, aPLCC.lonref,
                                    aPLCC.latref, X, Y);
                            Dimension xdim = new Dimension(DimensionType.X);
                            xdim.setShortName("X");
                            xdim.setValues(X);
                            this.setXDimension(xdim);
                            this.addDimension(xdim);
                            Dimension ydim = new Dimension(DimensionType.Y);
                            ydim.setShortName("Y");
                            ydim.setValues(Y);
                            this.setYDimension(ydim);
                            this.addDimension(ydim);
                            break;
                        }
                        case "NPS":
                        case "SPS": {
                            int iSize = Integer.parseInt(dataArray[1]);
                            int jSize = Integer.parseInt(dataArray[2]);
                            float iPole = Float.parseFloat(dataArray[3]);
                            float jPole = Float.parseFloat(dataArray[4]);
                            float lonRef = Float.parseFloat(dataArray[5]);
                            float dx = Float.parseFloat(dataArray[6]) * 1000;
                            float dy = dx;
                            String lat0 = "90";
                            if (PDEF.PDEF_Type.equals("SPS")) {
                                lat0 = "-90";
                            }
                            isLatLon = false;
                            ProjStr = "+proj=stere +lon_0=" + String.valueOf(lonRef)
                                    + " +lat_0=" + lat0;
                            this.setProjectionInfo(ProjectionInfo.factory(ProjStr));
                            //Set X Y
                            XNum = iSize;
                            YNum = jSize;
                            X = new double[iSize];
                            Y = new double[jSize];
                            getProjectedXY_NPS(dx, dy, iPole, jPole, X, Y);
                            Dimension xdim = new Dimension(DimensionType.X);
                            xdim.setShortName("X");
                            xdim.setValues(X);
                            this.setXDimension(xdim);
                            this.addDimension(xdim);
                            Dimension ydim = new Dimension(DimensionType.Y);
                            ydim.setShortName("Y");
                            ydim.setValues(Y);
                            this.setYDimension(ydim);
                            this.addDimension(ydim);
                            break;
                        }
                        default:
                            errorStr = "The PDEF type is not supported at present!" + System.getProperty("line.separator")
                                    + "Please send your data to the author to improve MeteoInfo!";
                            //goto ERROR;
                            break;
                    }
                    break;
                case "XDEF":
                    if (this.getProjectionInfo().isLonLat()) {
                        XDEF.XNum = Integer.parseInt(dataArray[1]);
                        XDEF.X = new double[XDEF.XNum];
                        XDEF.Type = dataArray[2];
                        List<Double> values = new ArrayList<>();
                        if (XDEF.Type.toUpperCase().equals("LINEAR")) {
                            XDEF.XMin = Float.parseFloat(dataArray[3]);
                            XDEF.XDelt = Float.parseFloat(dataArray[4]);
                        } else {
                            if (dataArray.length < XDEF.XNum + 3) {
                                while (true) {
                                    aLine = aLine + " " + sr.readLine().trim();
                                    if (aLine.isEmpty()) {
                                        continue;
                                    }
                                    dataArray = aLine.split("\\s+");
                                    if (dataArray.length >= XDEF.XNum + 3) {
                                        break;
                                    }
                                }
                            }
                            if (dataArray.length > XDEF.XNum + 3) {
                                errorStr = "XDEF is wrong! Please check the ctl file!";
                                //goto ERROR;
                            }
                            XDEF.XMin = Float.parseFloat(dataArray[3]);
                            float xmax = Float.parseFloat(dataArray[dataArray.length - 1]);
                            XDEF.XDelt = (xmax - XDEF.XMin) / (XDEF.XNum - 1);
                        }
                        double delta = BigDecimalUtil.toDouble(XDEF.XDelt);
                        double min = BigDecimalUtil.toDouble(XDEF.XMin);
                        for (i = 0; i < XDEF.XNum; i++) {
                            XDEF.X[i] = BigDecimalUtil.add(min, BigDecimalUtil.mul(i, delta));
                            values.add(XDEF.X[i]);
                        }
                        if (XDEF.XMin == 0 && XDEF.X[XDEF.XNum - 1]
                                + XDEF.XDelt == 360) {
                            isGlobal = true;
                        }
                        Dimension xDim = new Dimension(DimensionType.X);
                        xDim.setShortName("X");
                        xDim.setValues(values);
                        this.setXDimension(xDim);
                        this.addDimension(xDim);
                    }
                    break;
                case "YDEF":
                    if (this.getProjectionInfo().isLonLat()) {
                        YDEF.YNum = Integer.parseInt(dataArray[1]);
                        YDEF.Y = new double[YDEF.YNum];
                        YDEF.Type = dataArray[2];
                        List<Double> values = new ArrayList<>();
                        if (YDEF.Type.toUpperCase().equals("LINEAR")) {
                            YDEF.YMin = Float.parseFloat(dataArray[3]);
                            YDEF.YDelt = Float.parseFloat(dataArray[4]);
                        } else {
                            if (dataArray.length < YDEF.YNum + 3) {
                                while (true) {
                                    aLine = aLine + " " + sr.readLine().trim();
                                    if (aLine.isEmpty()) {
                                        continue;
                                    }
                                    dataArray = aLine.split("\\s+");
                                    if (dataArray.length >= YDEF.YNum + 3) {
                                        break;
                                    }
                                }
                            }
                            if (dataArray.length > YDEF.YNum + 3) {
                                errorStr = "YDEF is wrong! Please check the ctl file!";
                                //goto ERROR;
                            }
                            YDEF.YMin = Float.parseFloat(dataArray[3]);
                            float ymax = Float.parseFloat(dataArray[dataArray.length - 1]);
                            YDEF.YDelt = (ymax - YDEF.YMin) / (YDEF.YNum - 1);
                        }
                        double delta = BigDecimalUtil.toDouble(YDEF.YDelt);
                        double min = BigDecimalUtil.toDouble(YDEF.YMin);
                        for (i = 0; i < YDEF.YNum; i++) {
                            YDEF.Y[i] = BigDecimalUtil.add(min, BigDecimalUtil.mul(i, delta));
                            values.add(YDEF.Y[i]);
                        }
                        Dimension yDim = new Dimension(DimensionType.Y);
                        yDim.setShortName("Y");
                        yDim.setValues(values);
                        this.setYDimension(yDim);
                        this.addDimension(yDim);
                    }
                    break;
                case "ZDEF":
                    ZDEF.ZNum = Integer.parseInt(dataArray[1]);
                    ZDEF.Type = dataArray[2];
                    ZDEF.ZLevels = new float[ZDEF.ZNum];
                    List<Double> values = new ArrayList<>();
                    if (ZDEF.Type.toUpperCase().equals("LINEAR")) {
                        ZDEF.SLevel = Float.parseFloat(dataArray[3]);
                        ZDEF.ZDelt = Float.parseFloat(dataArray[4]);
                        for (i = 0; i < ZDEF.ZNum; i++) {
                            ZDEF.ZLevels[i] = ZDEF.SLevel + i * ZDEF.ZDelt;
                            values.add(Double.valueOf(ZDEF.ZLevels[i]));
                        }
                    } else if (dataArray.length < ZDEF.ZNum + 3) {
                        while (true) {
                            String line = sr.readLine().trim();
                            if (line.isEmpty()) {
                                continue;
                            }
                            dataArray = line.split("\\s+");
                            if (this.isKeyWord(dataArray[0])) {
                                dataArray = aLine.split("\\s+");
//                    if (dataArray.length > ZDEF.ZNum + 3) {
//                        errorStr = "ZDEF is wrong! Please check the ctl file!";
//                        //goto ERROR;
//                    }
//                    for (i = 0; i < ZDEF.ZNum; i++) {
//                        ZDEF.ZLevels[i] = Float.parseFloat(dataArray[3 + i]);
//                        values.add(Double.parseDouble(dataArray[3 + i]));
//                    }
                                ZDEF.ZNum = dataArray.length - 3;
                                ZDEF.ZLevels = new float[ZDEF.ZNum];
                                for (i = 0; i < ZDEF.ZNum; i++) {
                                    ZDEF.ZLevels[i] = Float.parseFloat(dataArray[3 + i]);
                                    values.add(Double.parseDouble(dataArray[3 + i]));
                                }
                                aLine = line;
                                isReadLine = false;
                                break;
                            }

                            aLine = aLine + " " + line;
//                            dataArray = aLine.split("\\s+");
//                            if (dataArray.length >= ZDEF.ZNum + 3) {
//                                break;
//                            }
                        }
                    } else {
                        ZDEF.ZNum = dataArray.length - 3;
                        ZDEF.ZLevels = new float[ZDEF.ZNum];
                        for (i = 0; i < ZDEF.ZNum; i++) {
                            ZDEF.ZLevels[i] = Float.parseFloat(dataArray[3 + i]);
                            values.add(Double.parseDouble(dataArray[3 + i]));
                        }
                    }
                    zDim = new Dimension(DimensionType.Z);
                    zDim.setShortName("Z");
                    zDim.setValues(values);
                    this.setZDimension(zDim);
                    this.addDimension(zDim);
                    break;
                case "TDEF":
                    int tnum = Integer.parseInt(dataArray[1]);
                    TDEF.Type = dataArray[2];
                    if (TDEF.Type.toUpperCase().equals("LINEAR")) {
                        String dStr = dataArray[3];
                        dStr = dStr.toUpperCase();
                        i = dStr.indexOf("Z");
                        switch (i) {
                            case -1:
                                if (Character.isDigit(dStr.charAt(0))) {
                                    dStr = "00:00Z" + dStr;
                                } else {
                                    dStr = "00:00Z01" + dStr;
                                }
                                break;
                            case 1:
                                dStr = "0" + dStr.substring(0, 1) + ":00" + dStr.substring(1);
                                break;
                            case 2:
                                dStr = dStr.substring(0, 2) + ":00" + dStr.substring(2);
                                break;
                            default:
                                break;
                        }
                        if (!(Character.isDigit(dStr.charAt(dStr.length() - 3)))) {
                            int aY = Integer.parseInt(dStr.substring(dStr.length() - 2));
                            if (aY > 50) {
                                aY = 1900 + aY;
                            } else {
                                aY = 2000 + aY;
                            }
                            dStr = dStr.substring(0, dStr.length() - 2) + String.valueOf(aY);
                        }
                        if (dStr.length() == 14) {
                            StringBuilder strb = new StringBuilder(dStr);
                            strb.insert(6, "0");
                            dStr = strb.toString();
                        }
                        String mn = dStr.substring(8, 11);
                        String Nmn = mn.substring(0, 1).toUpperCase() + mn.substring(1, 3).toLowerCase();
                        dStr = dStr.replace(mn, Nmn);
                        dStr = dStr.replace("Z", " ");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm ddMMMyyyy", Locale.ENGLISH);
                        TDEF.STime = LocalDateTime.parse(dStr, formatter);

                        //Read time interval
                        TDEF.TDelt = dataArray[4];
                        char[] tChar = dataArray[4].toCharArray();
                        int aPos = 0;    //Position between number and string
                        for (i = 0; i < tChar.length; i++) {
                            if (!Character.isDigit(tChar[i])) {
                                aPos = i;
                                break;
                            }
                        }
                        if (aPos == 0) {
                            errorStr = "TDEF is wrong! Please check the ctl file!";
                            //goto ERROR;
                        }
                        int iNum = Integer.parseInt(TDEF.TDelt.substring(0, aPos));
                        TDEF.DeltaValue = iNum;
                        String tStr = TDEF.TDelt.substring(aPos).toLowerCase();
                        TDEF.unit = tStr;
                        LocalDateTime sTime = TDEF.STime;
                        switch (tStr) {
                            case "mn":
                                for (i = 0; i < tnum; i++) {
                                    TDEF.times.add(sTime);
                                    sTime = sTime.plusMinutes(iNum);
                                }
                                break;
                            case "hr":
                                for (i = 0; i < tnum; i++) {
                                    TDEF.times.add(sTime);
                                    sTime = sTime.plusHours(iNum);
                                }
                                break;
                            case "dy":
                                for (i = 0; i < tnum; i++) {
                                    TDEF.times.add(sTime);
                                    sTime = sTime.plusDays(iNum);
                                }
                                break;
                            case "mo":
                            case "mon":
                                for (i = 0; i < tnum; i++) {
                                    TDEF.times.add(sTime);
                                    sTime = sTime.plusMonths(iNum);
                                }
                                break;
                            case "yr":
                                for (i = 0; i < tnum; i++) {
                                    TDEF.times.add(sTime);
                                    sTime = sTime.plusYears(iNum);

                                }
                                break;
                            default:
                                break;
                        }
                        values = new ArrayList<>();
                        for (LocalDateTime t : TDEF.times) {
                            values.add(JDateUtil.toOADate(t));
                        }
                        Dimension tDim = new Dimension(DimensionType.T);
                        tDim.setShortName("T");
                        tDim.setValues(values);
                        this.setTimeDimension(tDim);
                        this.addDimension(tDim);
                    } else {
                        if (dataArray.length < tnum + 3) {
                            while (true) {
                                aLine = aLine + " " + sr.readLine().trim();
                                if (aLine.isEmpty()) {
                                    continue;
                                }
                                dataArray = aLine.split("\\s+");
                                if (dataArray.length >= tnum + 3) {
                                    break;
                                }
                            }
                        }
                        if (dataArray.length > tnum + 3) {
                            errorStr = "TDEF is wrong! Please check the ctl file!";
                            //goto ERROR;
                        }
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm ddMMMyyyy", Locale.ENGLISH);
                        values = new ArrayList<>();
                        for (i = 0; i < tnum; i++) {
                            String dStr = dataArray[3 + i];
                            dStr = dStr.replace("Z", " ");
                            LocalDateTime t = LocalDateTime.parse(dStr, formatter);
                            TDEF.times.add(t);
                            values.add(JDateUtil.toOADate(t));
                        }
                        Dimension tDim = new Dimension(DimensionType.T);
                        tDim.setShortName("T");
                        tDim.setValues(values);
                        this.setTimeDimension(tDim);
                        this.addDimension(tDim);
                    }
                    break;
                case "EDEF":
                    int eNum = Integer.parseInt(dataArray[1]);
                    if (dataArray.length < eNum + 3) {
                        while (true) {
                            aLine = aLine + " " + sr.readLine().trim();
                            if (aLine.isEmpty()) {
                                continue;
                            }
                            dataArray = aLine.split("\\s+");
                            if (dataArray.length >= eNum + 3) {
                                break;
                            }
                        }
                    }
                    if (dataArray.length > eNum + 3) {
                        errorStr = "EDEF is wrong! Please check the ctl file!";
                    }
                    for (i = 0; i < eNum; i++){
                        this.ensNames.add(dataArray[3 + i]);
                    }
                    eDim = new Dimension(DimensionType.E);
                    eDim.setLength(eNum);
                    this.addDimension(eDim);
                    Variable evar = new Variable();
                    evar.setName("ensemble");
                    evar.setDataType(DataType.STRING);
                    evar.addAttribute("standard_name", "ensemble");
                    evar.addDimension(eDim);
                    this.addVariable(evar);
                    break;
                case "VARS":
                    int vNum = Integer.parseInt(dataArray[1]);
                    for (i = 0; i < vNum; i++) {
                        aLine = sr.readLine().trim();
                        if (aLine.isEmpty()) {
                            i -= 1;
                            continue;
                        }
                        dataArray = aLine.split("\\s+");
                        Variable aVar = new Variable();
                        aVar.setName(dataArray[0]);
                        aVar.setDataType(DataType.FLOAT);
                        int lNum = Integer.parseInt(dataArray[1]);
                        //aVar.setLevelNum(Integer.parseInt(dataArray[1]));
                        aVar.setUnits(dataArray[2]);
                        //attr = new Attribute("units", dataArray[2]);
                        //aVar.addAttribute(attr);
                        if (dataArray.length > 3) {
                            aVar.setDescription(dataArray[3]);
                            attr = new Attribute("description", dataArray[3]);
                            aVar.addAttribute(attr);
                        }
                        if (eDim != null){
                            aVar.addDimension(eDim);
                        }
                        aVar.setDimension(this.getTimeDimension());
                        if (lNum > 1) {
                            boolean isNew = true;
                            for (Dimension dim : this.getDimensions()) {
                                if (dim.getDimType() == DimensionType.Z) {
                                    if (lNum == dim.getLength()) {
                                        aVar.setDimension(dim);
                                        isNew = false;
                                        break;
                                    }
                                }
                            }
                            if (isNew) {
                                List<Double> levs = new ArrayList<>();
                                for (int j = 0; j < lNum; j++) {
                                    if (ZDEF.ZNum > j) {
                                        aVar.addLevel(ZDEF.ZLevels[j]);
                                        levs.add((double) ZDEF.ZLevels[j]);
                                    }
                                }
                                Dimension dim = new Dimension(DimensionType.Z);
                                dim.setShortName("Z_" + String.valueOf(lNum));
                                dim.setValues(levs);
                                aVar.setDimension(dim);
                                this.addDimension(dim);
                            }
                        }
                        aVar.setDimension(this.getYDimension());
                        aVar.setDimension(this.getXDimension());
                        if (this.getDataType() == MeteoDataType.GrADS_Station) {
                            aVar.setStation(true);
                        }
                        aVar.setFillValue(this.getMissingValue());

                        VARDEF.addVar(aVar);
                        this.addVariable(aVar);
                    }
                    break;
                case "ENDVARS":
                    isEnd = true;
                    break;
                default:
                    break;
            }

            if (isEnd) {
                break;
            }

        } while (aLine != null);

        sr.close();

        //Set X/Y coordinate
        if (isLatLon) {
            X = XDEF.X;
            Y = YDEF.Y;
            XNum = XDEF.XNum;
            YNum = YDEF.YNum;
        }

        //Calculate record length
        RecordLen = XNum * YNum * 4;
        if (OPTIONS.sequential) {
            RecordLen += 8;
        }

        //Calculate data length of each time
        RecLenPerTime = 0;
        int lNum;
        for (i = 0; i < VARDEF.getVNum(); i++) {
            lNum = VARDEF.getVars().get(i).getLevelNum();
            if (lNum == 0) {
                lNum = 1;
            }
            RecLenPerTime += lNum * RecordLen;
        }

        return true;
    }

    private boolean isKeyWord(String str) {
        List<String> keyWords = new ArrayList<>();
        keyWords.add("DSET");
        keyWords.add("CHSUB");
        keyWords.add("DTYPE");
        keyWords.add("INDEX");
        keyWords.add("STNMAP");
        keyWords.add("TITLE");
        keyWords.add("UNDEF");
        keyWords.add("UNPACK");
        keyWords.add("FILEHEADER");
        keyWords.add("XYHEADER");
        keyWords.add("TRAILERBYTES");
        keyWords.add("XVAR");
        keyWords.add("YVAR");
        keyWords.add("ZVAR");
        keyWords.add("STID");
        keyWords.add("TVAR");
        keyWords.add("TOFFVAR");
        keyWords.add("CACHESIZE");
        keyWords.add("OPTIONS");
        keyWords.add("PDEF");
        keyWords.add("XDEF");
        keyWords.add("YDEF");
        keyWords.add("ZDEF");
        keyWords.add("TDEF");
        keyWords.add("EDEF");
        keyWords.add("VECTORPAIRS");
        keyWords.add("VARS");
        keyWords.add("ENDVARS");
        keyWords.add("ATTRIBUTE METADATA");
        keyWords.add("COMMENTS");

        return keyWords.contains(str.toUpperCase());
    }

    private void getProjectedXY(ProjectionInfo projInfo, float size,
            float sync_XP, float sync_YP, float sync_Lon, float sync_Lat,
            double[] X, double[] Y) {
        //Get sync X/Y
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        double sync_X, sync_Y;
        double[][] points = new double[1][];
        points[0] = new double[]{sync_Lon, sync_Lat};
        Reproject.reprojectPoints(points, fromProj, projInfo, 0, 1);
        sync_X = points[0][0];
        sync_Y = points[0][1];

        //Get integer sync X/Y            
        int i_XP, i_YP;
        double i_X, i_Y;
        i_XP = (int) sync_XP;
        if (sync_XP == i_XP) {
            i_X = sync_X;
        } else {
            i_X = sync_X - (sync_XP - i_XP) * size;
        }
        i_YP = (int) sync_YP;
        if (sync_YP == i_YP) {
            i_Y = sync_Y;
        } else {
            i_Y = sync_Y - (sync_YP - i_YP) * size;
        }

        //Get left bottom X/Y
        int nx, ny;
        nx = X.length;
        ny = Y.length;
        double xlb, ylb;
        xlb = i_X - (i_XP - 1) * size;
        ylb = i_Y - (i_YP - 1) * size;

        //Get X Y with orient 0
        int i;
        for (i = 0; i < nx; i++) {
            X[i] = xlb + i * size;
        }
        for (i = 0; i < ny; i++) {
            Y[i] = ylb + i * size;
        }
    }

    private void getProjectedXY(ProjectionInfo projInfo, float XSize, float YSize,
            float sync_XP, float sync_YP, float sync_Lon, float sync_Lat,
            double[] X, double[] Y) {
        //Get sync X/Y
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        double sync_X, sync_Y;
        double[][] points = new double[1][];
        points[0] = new double[]{sync_Lon, sync_Lat};
        Reproject.reprojectPoints(points, fromProj, projInfo, 0, 1);
        sync_X = points[0][0];
        sync_Y = points[0][1];

        //Get integer sync X/Y            
        int i_XP, i_YP;
        double i_X, i_Y;
        i_XP = (int) sync_XP;
        if (sync_XP == i_XP) {
            i_X = sync_X;
        } else {
            i_X = sync_X - (sync_XP - i_XP) * XSize;
        }
        i_YP = (int) sync_YP;
        if (sync_YP == i_YP) {
            i_Y = sync_Y;
        } else {
            i_Y = sync_Y - (sync_YP - i_YP) * YSize;
        }

        //Get left bottom X/Y
        int nx, ny;
        nx = X.length;
        ny = Y.length;
        double xlb, ylb;
        xlb = i_X - (i_XP - 1) * XSize;
        ylb = i_Y - (i_YP - 1) * YSize;

        //Get X Y with orient 0
        int i;
        for (i = 0; i < nx; i++) {
            X[i] = xlb + i * XSize;
        }
        for (i = 0; i < ny; i++) {
            Y[i] = ylb + i * YSize;
        }
    }

    private void getProjectedXY_NPS(float XSize, float YSize,
            float sync_XP, float sync_YP,
            double[] X, double[] Y) {
        //Get sync X/Y
        double sync_X = 0, sync_Y = 0;

        //Get integer sync X/Y            
        int i_XP, i_YP;
        double i_X, i_Y;
        i_XP = (int) sync_XP;
        if (sync_XP == i_XP) {
            i_X = sync_X;
        } else {
            i_X = sync_X - (sync_XP - i_XP) * XSize;
        }
        i_YP = (int) sync_YP;
        if (sync_YP == i_YP) {
            i_Y = sync_Y;
        } else {
            i_Y = sync_Y - (sync_YP - i_YP) * YSize;
        }

        //Get left bottom X/Y
        int nx, ny;
        nx = X.length;
        ny = Y.length;
        double xlb, ylb;
        xlb = i_X - (i_XP - 1) * XSize;
        ylb = i_Y - (i_YP - 1) * YSize;

        //Get X Y with orient 0
        int i;
        for (i = 0; i < nx; i++) {
            X[i] = xlb + i * XSize;
        }
        for (i = 0; i < ny; i++) {
            Y[i] = ylb + i * YSize;
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

//    /**
//     * Generate data info text
//     *
//     * @return Data info text
//     */
//    @Override
//    public String generateInfoText() {
//        String dataInfo;
//
//        dataInfo = "Title: " + TITLE;
//        dataInfo += System.getProperty("line.separator") + "Descriptor: " + DESCRIPTOR;
//        dataInfo += System.getProperty("line.separator") + "Binary: " + DSET;
//        dataInfo += System.getProperty("line.separator") + "Type = " + DTYPE;
//        if (DTYPE.toUpperCase().equals("STATION")) {
//            dataInfo += System.getProperty("line.separator") + "Tsize = " + String.valueOf(TDEF.getTimeNum());
//        } else {
//            Dimension xdim = this.getXDimension();
//            dataInfo += System.getProperty("line.separator") + "X Dimension: Xmin = " + String.valueOf(xdim.getMinValue())
//                    + "; Xmax = " + String.valueOf(xdim.getMaxValue()) + "; Xsize = "
//                    + String.valueOf(xdim.getLength()) + "; Xdelta = " + String.valueOf(xdim.getDeltaValue());
//            Dimension ydim = this.getYDimension();
//            dataInfo += System.getProperty("line.separator") + "Y Dimension: Ymin = " + String.valueOf(ydim.getMinValue())
//                    + "; Ymax = " + String.valueOf(ydim.getMaxValue()) + "; Ysize = "
//                    + String.valueOf(ydim.getLength()) + "; Ydelta = " + String.valueOf(ydim.getDeltaValue());
//            dataInfo += System.getProperty("line.separator") + "Zsize = " + String.valueOf(ZDEF.ZNum)
//                    + "  Tsize = " + String.valueOf(TDEF.getTimeNum());
//        }
//        dataInfo += System.getProperty("line.separator") + "Number of Variables = " + String.valueOf(VARDEF.getVNum());
//        for (Variable v : VARDEF.getVars()) {
//            dataInfo += System.getProperty("line.separator") + v.getName() + " " + String.valueOf(v.getLevelNum()) + " "
//                    + v.getUnits() + " " + v.getDescription();
//        }
//
//        return dataInfo;
//    }
    private Object[] getFilePath_Template(int timeIdx) {
        String filePath;
        File file = new File(DSET);
        String path = file.getParent();
        String fn = file.getName();
        LocalDateTime time = this.getTimes().get(timeIdx);
        DateTimeFormatter format;
        String tStr = "year";
        if (fn.contains("%y4")) {
            format = DateTimeFormatter.ofPattern("yyyy");
            fn = fn.replace("%y4", format.format(time));
        }
        if (fn.contains("%y2")) {
            format = DateTimeFormatter.ofPattern("yy");
            fn = fn.replace("%y2", format.format(time));
        }
        if (fn.contains("%m1")) {
            format = DateTimeFormatter.ofPattern("M");
            fn = fn.replace("%m1", format.format(time));
            tStr = "month";
        }
        if (fn.contains("%m2")) {
            format = DateTimeFormatter.ofPattern("MM");
            fn = fn.replace("%m2", format.format(time));
            tStr = "month";
        }
        if (fn.contains("%mc")) {
            format = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
            fn = fn.replace("%mc", format.format(time));
            tStr = "month";
        }
        if (fn.contains("%d1")) {
            format = DateTimeFormatter.ofPattern("d");
            fn = fn.replace("%d1", format.format(time));
            tStr = "day";
        }
        if (fn.contains("%d2")) {
            format = DateTimeFormatter.ofPattern("dd");
            fn = fn.replace("%d2", format.format(time));
            tStr = "day";
        }
        if (fn.contains("%h1")) {
            format = DateTimeFormatter.ofPattern("H");
            fn = fn.replace("%h1", format.format(time));
            tStr = "hour";
        }
        if (fn.contains("%h2")) {
            format = DateTimeFormatter.ofPattern("HH");
            fn = fn.replace("%h2", format.format(time));
            tStr = "hour";
        }
        if (fn.contains("%n2")) {
            format = DateTimeFormatter.ofPattern("mm");
            fn = fn.replace("%n2", format.format(time));
            tStr = "minute";
        }

        filePath = path + File.separator + fn;

        int tIdx = 0;
        if (tStr.equalsIgnoreCase("year")) {
            switch (TDEF.unit) {
                case "mn":
                    tIdx = ((time.getDayOfYear() - 1) * 24 * 60 + time.getMinute()) / TDEF.DeltaValue;
                    break;
                case "hr":
                    tIdx = ((time.getDayOfYear() - 1) * 24 + time.getHour()) / TDEF.DeltaValue;
                    break;
                case "dy":
                    tIdx = time.getDayOfYear() - 1;
                    break;
                case "mo":
                case "mon":
                    tIdx = time.getMonthValue() - 1;
                    break;
                default:
                    break;
            }
        } else if (tStr.equalsIgnoreCase("month")) {
            switch (TDEF.unit) {
                case "mn":
                    tIdx = ((time.getDayOfMonth() - 1) * 24 * 60 + time.getMinute()) / TDEF.DeltaValue;
                    break;
                case "hr":
                    tIdx = ((time.getDayOfMonth() - 1) * 24 + time.getHour()) / TDEF.DeltaValue;
                    break;
                case "dy":
                    tIdx = time.getDayOfMonth() - 1;
                    break;
                default:
                    break;
            }
        } else if (tStr.equalsIgnoreCase("day")) {
            if (TDEF.unit.equals("mn")) {
                tIdx = ((time.getHour() - 1) * 60 + time.getMinute()) / TDEF.DeltaValue;
            } else if (TDEF.unit.equals("hr")) {
                tIdx = time.getHour() - 1;
            }
        }

        return new Object[]{filePath, tIdx};
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
            if (varName.equals("ensemble")){
                Array dataArray = Array.factory(DataType.STRING, section.getShape());
                IndexIterator ii = dataArray.getIndexIterator();
                Range eRange = section.getRange(0);
                this.readEnsemble(eRange, ii);
                return dataArray;
            }
            
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            int rangeIdx = 0;
            Dimension eDim = var.getDimension(DimensionType.E);
            Range eRange = eDim != null ? section.getRange(rangeIdx++)
                    : new Range(0, 0);
            
            Range timeRange = section.getRank() > 2 ? section.getRange(rangeIdx++)
                    : new Range(0, 0);

            Range levRange = var.getLevelNum() > 0 ? section.getRange(rangeIdx++)
                    : new Range(0, 0);

            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);

            IndexIterator ii = dataArray.getIndexIterator();

            for (int eIdx = eRange.first(); eIdx <= eRange.last(); eIdx += eRange.stride()) {
                for (int timeIdx = timeRange.first(); timeIdx <= timeRange.last();
                        timeIdx += timeRange.stride()) {
                    int levelIdx = levRange.first();

                    for (; levelIdx <= levRange.last();
                            levelIdx += levRange.stride()) {
                        readXY(varName, eIdx, timeIdx, levelIdx, yRange, xRange, ii);
                    }
                }
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private void readEnsemble(Range eRange, IndexIterator ii) {
        for (int i = eRange.first(); i <= eRange.last(); i += eRange.stride()){
            ii.setObjectNext(this.ensNames.get(i));
        }
    }

    private void readXY(String varName, int timeIdx, int levelIdx, Range yRange, Range xRange, IndexIterator ii) {
        try {
            int varIdx = this.getVariableNames().indexOf(varName);
            int xNum, yNum;
            xNum = XNum;
            yNum = YNum;
            float[] data = new float[yNum * xNum];

            String filePath = DSET;
            int tIdx = timeIdx;
            if (OPTIONS.template) {
                Object[] result = getFilePath_Template(timeIdx);
                filePath = (String) result[0];
                tIdx = (int) result[1];
            }
            RandomAccessFile br = new RandomAccessFile(filePath, "r");
            int i, lNum;
            byte[] aBytes;

            br.seek(FILEHEADER);
            br.seek(br.getFilePointer() + tIdx * RecLenPerTime);
            for (i = 0; i < varIdx; i++) {
                lNum = VARDEF.getVars().get(i).getLevelNum();
                if (lNum == 0) {
                    lNum = 1;
                }
                br.seek(br.getFilePointer() + lNum * RecordLen);
            }
            br.seek(br.getFilePointer() + levelIdx * RecordLen);

            if (OPTIONS.sequential) {
                br.seek(br.getFilePointer() + 4);
            }

            //Read X/Y data
            byte[] byteData = new byte[xNum * yNum * 4];
            br.read(byteData);
            int start = 0;
            for (i = 0; i < yNum * xNum; i++) {
                aBytes = new byte[4];
                System.arraycopy(byteData, start, aBytes, 0, 4);
                start += 4;
                data[i] = DataConvert.bytes2Float(aBytes, _byteOrder);
            }

            br.close();
            for (int y = yRange.first(); y <= yRange.last();
                    y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last();
                        x += xRange.stride()) {
                    int index = y * xNum + x;
                    ii.setFloatNext(data[index]);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void readXY(String varName, int eIdx, int timeIdx, int levelIdx, Range yRange, Range xRange, IndexIterator ii) {
        try {
            int varIdx = this.getVariableNames().indexOf(varName);
            int xNum, yNum;
            xNum = XNum;
            yNum = YNum;
            float[] data = new float[yNum * xNum];

            String filePath = DSET;
            int tIdx = timeIdx;
            if (OPTIONS.template) {
                Object[] result = getFilePath_Template(timeIdx);
                filePath = (String) result[0];
                tIdx = (int) result[1];
            }
            RandomAccessFile br = new RandomAccessFile(filePath, "r");
            int i, lNum;
            byte[] aBytes;

            br.seek(FILEHEADER);
            br.seek(br.getFilePointer() + eIdx * this.getTimeNum() * RecLenPerTime);
            br.seek(br.getFilePointer() + tIdx * RecLenPerTime);
            for (i = 0; i < varIdx; i++) {
                lNum = VARDEF.getVars().get(i).getLevelNum();
                if (lNum == 0) {
                    lNum = 1;
                }
                br.seek(br.getFilePointer() + lNum * RecordLen);
            }
            br.seek(br.getFilePointer() + levelIdx * RecordLen);

            if (OPTIONS.sequential) {
                br.seek(br.getFilePointer() + 4);
            }

            //Read X/Y data
            byte[] byteData = new byte[xNum * yNum * 4];
            br.read(byteData);
            int start = 0;
            for (i = 0; i < yNum * xNum; i++) {
                aBytes = new byte[4];
                System.arraycopy(byteData, start, aBytes, 0, 4);
                start += 4;
                data[i] = DataConvert.bytes2Float(aBytes, _byteOrder);
            }

            br.close();
            for (int y = yRange.first(); y <= yRange.last();
                    y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last();
                        x += xRange.stride()) {
                    int index = y * xNum + x;
                    ii.setFloatNext(data[index]);
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

    /**
     * Read GrADS grid data - lon/lat
     *
     * @param timeIdx Time index
     * @param varIdx Variable index
     * @param levelIdx Level index
     * @return Grid data
     */
    @Override
    public GridData getGridData_LonLat(int timeIdx, String varName, int levelIdx) {
        GridData gridData = new GridData();
        try {
            int varIdx = this.getVariableIndex(varName);
            gridData.data = readGrADSData_Grid_LonLat(timeIdx, varIdx, levelIdx);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        gridData.xArray = X;
        gridData.yArray = Y;
        gridData.missingValue = this.getMissingValue();

        if (OPTIONS.yrev) {
            gridData.yReverse();
        }

        return gridData;
    }

    /**
     * Read GrADS grid data - lon/lat
     *
     * @param timeIdx Time index
     * @param varIdx Variable index
     * @param levelIdx Level index
     * @return Grid data array
     */
    private double[][] readGrADSData_Grid_LonLat(int timeIdx, int varIdx, int levelIdx) throws FileNotFoundException, IOException {
        int xNum, yNum;
        xNum = XNum;
        yNum = YNum;
        double[][] gridData = new double[yNum][xNum];

        String filePath = DSET;
        int tIdx = timeIdx;
        if (OPTIONS.template) {
            Object[] result = getFilePath_Template(timeIdx);
            filePath = (String) result[0];
            tIdx = (int) result[1];
            if (tIdx < 0) {
                tIdx = 0;
            }
            if (tIdx >= this.getTimeNum()) {
                tIdx = this.getTimeNum() - 1;
            }
        }
        RandomAccessFile br = new RandomAccessFile(filePath, "r");
        int i, j, lNum;
        byte[] aBytes;

        br.seek(FILEHEADER);
        br.seek(br.getFilePointer() + tIdx * RecLenPerTime);
        for (i = 0; i < varIdx; i++) {
            lNum = VARDEF.getVars().get(i).getLevelNum();
            if (lNum == 0) {
                lNum = 1;
            }
            br.seek(br.getFilePointer() + lNum * RecordLen);
        }
        br.seek(br.getFilePointer() + levelIdx * RecordLen);

        if (OPTIONS.sequential) {
            br.seek(br.getFilePointer() + 4);
        }

        //Read X/Y data
        byte[] byteData = new byte[xNum * yNum * 4];
        br.read(byteData);
        int start = 0;
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                aBytes = new byte[4];
                System.arraycopy(byteData, start, aBytes, 0, 4);
                start += 4;
                gridData[i][j] = DataConvert.bytes2Float(aBytes, _byteOrder);
            }
        }

        br.close();

        return gridData;
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, String varName, int levelIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            int xNum, yNum;
            xNum = YNum;
            yNum = TDEF.getTimeNum();
            double[][] gridData = new double[yNum][xNum];
            int i, j, lNum, t;
            long aTPosition;

            if (OPTIONS.template) {
                byte[] aBytes = new byte[4];
                for (t = 0; t < TDEF.getTimeNum(); t++) {
                    Object[] result = getFilePath_Template(t);
                    String filePath = (String) result[0];
                    int tIdx = (int) result[1];
                    RandomAccessFile br = new RandomAccessFile(filePath, "r");
                    br.seek(FILEHEADER + tIdx * RecLenPerTime);

                    for (i = 0; i < varIdx; i++) {
                        lNum = VARDEF.getVars().get(i).getLevelNum();
                        if (lNum == 0) {
                            lNum = 1;
                        }
                        br.seek(br.getFilePointer() + lNum * RecordLen);
                    }
                    br.seek(br.getFilePointer() + levelIdx * RecordLen);
                    if (OPTIONS.sequential) {
                        br.seek(br.getFilePointer() + 4);
                    }

                    if (br.getFilePointer() >= br.length()) {
                        System.out.println("Erro");
                    }

                    for (i = 0; i < YNum; i++) {
                        for (j = 0; j < XNum; j++) {
                            br.read(aBytes);
                            if (j == lonIdx) {
                                gridData[t][i] = DataConvert.bytes2Float(aBytes, _byteOrder);
                            }
                        }
                    }
                    br.close();
                }
            } else {
                RandomAccessFile br = new RandomAccessFile(DSET, "r");
                for (t = 0; t < TDEF.getTimeNum(); t++) {
                    br.seek(FILEHEADER + t * RecLenPerTime);
                    aTPosition = br.getFilePointer();

                    for (i = 0; i < varIdx; i++) {
                        lNum = VARDEF.getVars().get(i).getLevelNum();
                        if (lNum == 0) {
                            lNum = 1;
                        }
                        br.seek(br.getFilePointer() + lNum * RecordLen);
                    }
                    br.seek(br.getFilePointer() + levelIdx * RecordLen);

                    if (OPTIONS.sequential) {
                        br.seek(br.getFilePointer() + 4);
                    }

                    if (br.getFilePointer() >= br.length()) {
                        System.out.println("Erro");
                    }

                    byte[] aBytes = new byte[4];
                    for (i = 0; i < YNum; i++) {
                        for (j = 0; j < XNum; j++) {
                            br.read(aBytes);
                            if (j == lonIdx) {
                                gridData[t][i] = DataConvert.bytes2Float(aBytes, _byteOrder);
                            }
                        }
                    }
                    br.seek(aTPosition);
                }

                br.close();
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = Y;
            aGridData.yArray = new double[this.getTimeNum()];
            for (i = 0; i < this.getTimeNum(); i++) {
                aGridData.yArray[i] = JDateUtil.toOADate(this.getTimes().get(i));
            }

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, String varName, int levelIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            int xNum, yNum;
            xNum = XNum;
            yNum = TDEF.getTimeNum();
            double[][] gridData = new double[yNum][xNum];
            int i, j, lNum, t;
            long aTPosition;

            if (OPTIONS.template) {
                byte[] aBytes = new byte[4];
                for (t = 0; t < TDEF.getTimeNum(); t++) {
                    Object[] result = getFilePath_Template(t);
                    String filePath = (String) result[0];
                    int tIdx = (int) result[1];
                    RandomAccessFile br = new RandomAccessFile(filePath, "r");
                    br.seek(FILEHEADER + tIdx * RecLenPerTime);

                    for (i = 0; i < varIdx; i++) {
                        lNum = VARDEF.getVars().get(i).getLevelNum();
                        if (lNum == 0) {
                            lNum = 1;
                        }
                        br.seek(br.getFilePointer() + lNum * RecordLen);
                    }
                    br.seek(br.getFilePointer() + levelIdx * RecordLen);
                    if (OPTIONS.sequential) {
                        br.seek(br.getFilePointer() + 4);
                    }
                    br.seek(br.getFilePointer() + latIdx * xNum * 4);

                    if (br.getFilePointer() >= br.length()) {
                        System.out.println("Erro");
                    }

                    for (j = 0; j < xNum; j++) {
                        br.read(aBytes);
                        gridData[t][j] = DataConvert.bytes2Float(aBytes, _byteOrder);
                    }
                    br.close();
                }
            } else {
                RandomAccessFile br = new RandomAccessFile(DSET, "r");
                for (t = 0; t < TDEF.getTimeNum(); t++) {
                    br.seek(FILEHEADER + t * RecLenPerTime);
                    aTPosition = br.getFilePointer();

                    for (i = 0; i < varIdx; i++) {
                        lNum = VARDEF.getVars().get(i).getLevelNum();
                        if (lNum == 0) {
                            lNum = 1;
                        }
                        br.seek(br.getFilePointer() + lNum * RecordLen);
                    }
                    br.seek(br.getFilePointer() + levelIdx * RecordLen);
                    if (OPTIONS.sequential) {
                        br.seek(br.getFilePointer() + 4);
                    }
                    br.seek(br.getFilePointer() + latIdx * xNum * 4);

                    if (br.getFilePointer() >= br.length()) {
                        System.out.println("Erro");
                    }

                    byte[] aBytes = new byte[4];
                    for (j = 0; j < xNum; j++) {
                        br.read(aBytes);
                        gridData[t][j] = DataConvert.bytes2Float(aBytes, _byteOrder);
                    }
                    br.seek(aTPosition);
                }

                br.close();
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = X;
            aGridData.yArray = new double[this.getTimeNum()];
            for (i = 0; i < this.getTimeNum(); i++) {
                aGridData.yArray[i] = JDateUtil.toOADate(this.getTimes().get(i));
            }

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, String varName, int timeIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            int xNum, yNum;
            xNum = YNum;
            yNum = VARDEF.getVars().get(varIdx).getLevelNum();
            double[][] gridData = new double[yNum][xNum];

            String filePath = DSET;
            int tIdx = timeIdx;
            if (OPTIONS.template) {
                Object[] result = getFilePath_Template(timeIdx);
                filePath = (String) result[0];
                tIdx = (int) result[1];
            }

            RandomAccessFile br = new RandomAccessFile(filePath, "r");
            int i, j, lNum;

            br.seek(FILEHEADER + tIdx * RecLenPerTime);

            for (i = 0; i < varIdx; i++) {
                lNum = VARDEF.getVars().get(i).getLevelNum();
                if (lNum == 0) {
                    lNum = 1;
                }
                br.seek(br.getFilePointer() + lNum * RecordLen);
            }

            if (br.getFilePointer() >= br.length()) {
                System.out.println("Erro");
            }

            for (i = 0; i < yNum; i++) //Levels
            {
                if (OPTIONS.sequential) {
                    br.seek(br.getFilePointer() + 4);
                }

                byte[] aBytes = new byte[4];
                for (j = 0; j < YNum; j++) {
                    br.skipBytes(lonIdx * 4);
                    br.read(aBytes);
                    gridData[i][j] = DataConvert.bytes2Float(aBytes, _byteOrder);
                    br.skipBytes((XNum - lonIdx - 1) * 4);
                }
            }
            br.close();

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = Y;
            double[] levels = new double[VARDEF.getVars().get(varIdx).getLevelNum()];
            for (i = 0; i < levels.length; i++) {
                levels[i] = ZDEF.ZLevels[i];
            }
            aGridData.yArray = levels;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, String varName, int timeIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            int xNum, yNum;
            xNum = XNum;
            yNum = VARDEF.getVars().get(varIdx).getLevelNum();
            double[][] gridData = new double[yNum][xNum];

            String filePath = DSET;
            int tIdx = timeIdx;
            if (OPTIONS.template) {
                Object[] result = getFilePath_Template(timeIdx);
                filePath = (String) result[0];
                tIdx = (int) result[1];
            }

            RandomAccessFile br = new RandomAccessFile(filePath, "r");
            int i, j, lNum;

            br.seek(FILEHEADER + tIdx * RecLenPerTime);

            for (i = 0; i < varIdx; i++) {
                lNum = VARDEF.getVars().get(i).getLevelNum();
                if (lNum == 0) {
                    lNum = 1;
                }
                br.seek(br.getFilePointer() + lNum * RecordLen);
            }

            if (br.getFilePointer() >= br.length()) {
                System.out.println("Erro");
            }

            for (i = 0; i < yNum; i++) //Levels
            {
                if (OPTIONS.sequential) {
                    br.seek(br.getFilePointer() + 4);
                }
                br.seek(br.getFilePointer() + latIdx * xNum * 4);

                byte[] aBytes = new byte[4];
                for (j = 0; j < xNum; j++) {
                    br.read(aBytes);
                    gridData[i][j] = DataConvert.bytes2Float(aBytes, _byteOrder);
                }
                br.seek(br.getFilePointer() + (YNum - latIdx - 1) * xNum * 4);
            }
            br.close();

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = X;
            double[] levels = new double[VARDEF.getVars().get(varIdx).getLevelNum()];
            for (i = 0; i < levels.length; i++) {
                levels[i] = ZDEF.ZLevels[i];
            }
            aGridData.yArray = levels;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, String varName, int lonIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            int xNum, yNum;
            xNum = TDEF.getTimeNum();
            yNum = VARDEF.getVars().get(varIdx).getLevelNum();
            double[][] gridData = new double[yNum][xNum];
            int i, lNum, t;
            long aTPosition;

            if (OPTIONS.template) {
                byte[] aBytes = new byte[4];
                for (t = 0; t < TDEF.getTimeNum(); t++) {
                    Object[] result = getFilePath_Template(t);
                    String filePath = (String) result[0];
                    int tIdx = (int) result[1];
                    RandomAccessFile br = new RandomAccessFile(filePath, "r");
                    br.seek(FILEHEADER + tIdx * RecLenPerTime);

                    for (i = 0; i < varIdx; i++) {
                        lNum = VARDEF.getVars().get(i).getLevelNum();
                        if (lNum == 0) {
                            lNum = 1;
                        }
                        br.seek(br.getFilePointer() + lNum * RecordLen);
                    }

                    if (br.getFilePointer() >= br.length()) {
                        System.out.println("Erro");
                    }

                    for (i = 0; i < yNum; i++) //Levels
                    {
                        if (OPTIONS.sequential) {
                            br.seek(br.getFilePointer() + 4);
                        }
                        br.seek(br.getFilePointer() + latIdx * xNum * 4 + lonIdx * 4);
                        br.read(aBytes);
                        gridData[i][t] = DataConvert.bytes2Float(aBytes, _byteOrder);
                        br.seek(br.getFilePointer() + (XNum - lonIdx - 1) * 4 + (YNum - latIdx - 1) * xNum * 4);
                    }
                    br.close();
                }
            } else {
                RandomAccessFile br = new RandomAccessFile(DSET, "r");
                for (t = 0; t < xNum; t++) {
                    br.seek(FILEHEADER + t * RecLenPerTime);
                    aTPosition = br.getFilePointer();

                    for (i = 0; i < varIdx; i++) {
                        lNum = VARDEF.getVars().get(i).getLevelNum();
                        if (lNum == 0) {
                            lNum = 1;
                        }
                        br.seek(br.getFilePointer() + lNum * RecordLen);
                    }

                    if (br.getFilePointer() >= br.length()) {
                        System.out.println("Erro");
                    }

                    byte[] aBytes = new byte[4];
                    for (i = 0; i < yNum; i++) //Levels
                    {
                        if (OPTIONS.sequential) {
                            br.seek(br.getFilePointer() + 4);
                        }
                        br.seek(br.getFilePointer() + latIdx * xNum * 4 + lonIdx * 4);
                        br.read(aBytes);
                        gridData[i][t] = DataConvert.bytes2Float(aBytes, _byteOrder);
                        br.seek(br.getFilePointer() + (XNum - lonIdx - 1) * 4 + (YNum - latIdx - 1) * xNum * 4);
                    }

                    br.seek(aTPosition);
                }

                br.close();
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = new double[this.getTimeNum()];
            for (i = 0; i < this.getTimeNum(); i++) {
                aGridData.xArray[i] = JDateUtil.toOADate(this.getTimes().get(i));
            }
            double[] levels = new double[VARDEF.getVars().get(varIdx).getLevelNum()];
            for (i = 0; i < levels.length; i++) {
                levels[i] = ZDEF.ZLevels[i];
            }
            aGridData.yArray = levels;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, String varName, int levelIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            int i, lNum, t;
            byte[] aBytes = new byte[4];
            float aValue;

            GridData aGridData = new GridData();
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = new double[TDEF.getTimeNum()];
            aGridData.yArray = new double[1];
            aGridData.yArray[0] = 0;
            aGridData.data = new double[1][TDEF.getTimeNum()];

            if (OPTIONS.template) {
                for (t = 0; t < TDEF.getTimeNum(); t++) {
                    Object[] result = getFilePath_Template(t);
                    String filePath = (String) result[0];
                    int tIdx = (int) result[1];
                    RandomAccessFile br = new RandomAccessFile(filePath, "r");
                    br.seek(FILEHEADER + tIdx * RecLenPerTime);

                    for (i = 0; i < varIdx; i++) {
                        lNum = VARDEF.getVars().get(i).getLevelNum();
                        if (lNum == 0) {
                            lNum = 1;
                        }
                        br.seek(br.getFilePointer() + lNum * RecordLen);
                    }
                    br.seek(br.getFilePointer() + levelIdx * RecordLen);
                    if (OPTIONS.sequential) {
                        br.seek(br.getFilePointer() + 4);
                    }
                    br.seek(br.getFilePointer() + latIdx * XNum * 4);

                    if (br.getFilePointer() >= br.length()) {
                        System.out.println("Erro");
                    }

                    br.seek(br.getFilePointer() + lonIdx * 4);

                    br.read(aBytes);
                    aValue = DataConvert.bytes2Float(aBytes, _byteOrder);
                    aGridData.xArray[t] = JDateUtil.toOADate(TDEF.times.get(t));
                    aGridData.data[0][t] = aValue;

                    br.close();
                }
            } else {
                RandomAccessFile br = new RandomAccessFile(DSET, "r");
                for (t = 0; t < TDEF.getTimeNum(); t++) {
                    br.seek(FILEHEADER + t * RecLenPerTime);
                    for (i = 0; i < varIdx; i++) {
                        lNum = VARDEF.getVars().get(i).getLevelNum();
                        if (lNum == 0) {
                            lNum = 1;
                        }
                        br.seek(br.getFilePointer() + lNum * RecordLen);
                    }
                    br.seek(br.getFilePointer() + levelIdx * RecordLen);
                    if (OPTIONS.sequential) {
                        br.seek(br.getFilePointer() + 4);
                    }
                    br.seek(br.getFilePointer() + latIdx * XNum * 4);

                    if (br.getFilePointer() >= br.length()) {
                        System.out.println("Erro");
                    }

                    br.seek(br.getFilePointer() + lonIdx * 4);

                    br.read(aBytes);
                    aValue = DataConvert.bytes2Float(aBytes, _byteOrder);
                    aGridData.xArray[t] = JDateUtil.toOADate(TDEF.times.get(t));
                    aGridData.data[0][t] = aValue;
                }

                br.close();
            }

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, String varName, int timeIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            String filePath = DSET;
            int tIdx = timeIdx;
            if (OPTIONS.template) {
                Object[] result = getFilePath_Template(timeIdx);
                filePath = (String) result[0];
                tIdx = (int) result[1];
            }

            RandomAccessFile br = new RandomAccessFile(filePath, "r");
            int i, lNum;
            byte[] aBytes = new byte[4];
            float aValue;

            GridData aGridData = new GridData();
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = new double[ZDEF.ZNum];
            aGridData.yArray = new double[1];
            aGridData.yArray[0] = 0;
            aGridData.data = new double[1][ZDEF.ZNum];

            br.seek(FILEHEADER + tIdx * RecLenPerTime);

            for (i = 0; i < varIdx; i++) {
                lNum = VARDEF.getVars().get(i).getLevelNum();
                if (lNum == 0) {
                    lNum = 1;
                }
                br.seek(br.getFilePointer() + lNum * RecordLen);
            }

            long aPosition = br.getFilePointer();

            for (i = 0; i < ZDEF.ZNum; i++) {
                br.seek(aPosition + i * RecordLen);
                if (OPTIONS.sequential) {
                    br.seek(br.getFilePointer() + 4);
                }
                br.seek(br.getFilePointer() + latIdx * XNum * 4 + lonIdx * 4);

                br.read(aBytes);
                aValue = DataConvert.bytes2Float(aBytes, _byteOrder);
                aGridData.xArray[i] = ZDEF.ZLevels[i];
                aGridData.data[0][i] = aValue;
            }

            br.close();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, String varName, int levelIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            String filePath = DSET;
            int tIdx = timeIdx;
            if (OPTIONS.template) {
                Object[] result = getFilePath_Template(timeIdx);
                filePath = (String) result[0];
                tIdx = (int) result[1];
            }
            RandomAccessFile br = new RandomAccessFile(filePath, "r");
            int i, lNum;
            byte[] aBytes = new byte[4];
            float aValue;

            GridData aGridData = new GridData();
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = X;
            aGridData.yArray = new double[1];
            aGridData.yArray[0] = 0;
            aGridData.data = new double[1][X.length];

            br.seek(FILEHEADER + tIdx * RecLenPerTime);
            for (i = 0; i < varIdx; i++) {
                lNum = VARDEF.getVars().get(i).getLevelNum();
                if (lNum == 0) {
                    lNum = 1;
                }
                br.seek(br.getFilePointer() + lNum * RecordLen);
            }
            br.seek(br.getFilePointer() + levelIdx * RecordLen);
            if (OPTIONS.sequential) {
                br.seek(br.getFilePointer() + 4);
            }
            br.seek(br.getFilePointer() + latIdx * XNum * 4);

            for (i = 0; i < XNum; i++) {
                br.read(aBytes);
                aValue = DataConvert.bytes2Float(aBytes, _byteOrder);
                aGridData.data[0][i] = aValue;
            }

            br.close();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, String varName, int levelIdx) {
        try {
            int varIdx = this.getVariableIndex(varName);
            String filePath = DSET;
            int tIdx = timeIdx;
            if (OPTIONS.template) {
                Object[] result = getFilePath_Template(timeIdx);
                filePath = (String) result[0];
                tIdx = (int) result[1];
            }
            RandomAccessFile br = new RandomAccessFile(filePath, "r");
            int i, lNum;
            byte[] aBytes = new byte[4];
            float aValue;

            GridData aGridData = new GridData();
            aGridData.missingValue = this.getMissingValue();
            aGridData.xArray = Y;
            aGridData.yArray = new double[1];
            aGridData.yArray[0] = 0;
            aGridData.data = new double[1][Y.length];

            br.seek(FILEHEADER + tIdx * RecLenPerTime);
            for (i = 0; i < varIdx; i++) {
                lNum = VARDEF.getVars().get(i).getLevelNum();
                if (lNum == 0) {
                    lNum = 1;
                }
                br.seek(br.getFilePointer() + lNum * RecordLen);
            }
            br.seek(br.getFilePointer() + levelIdx * RecordLen);
            if (OPTIONS.sequential) {
                br.seek(br.getFilePointer() + 4);
            }
            long aPosition = br.getFilePointer();

            for (i = 0; i < YNum; i++) {
                br.seek(aPosition + i * XNum * 4 + lonIdx * 4);
                br.read(aBytes);
                aValue = DataConvert.bytes2Float(aBytes, _byteOrder);
                aGridData.data[0][i] = aValue;
            }

            br.close();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Get GrADS station data
     *
     * @param vIdx Variable index
     * @param stID Station identifer
     * @return Grid data
     */
    public GridData getGridData_Station(int vIdx, String stID) {
        try {
            GridData gData = new GridData();
            gData.missingValue = this.getMissingValue();

            RandomAccessFile br = new RandomAccessFile(DSET, "r");
            int i, stNum, tNum;
            STDataHead aSTDH = new STDataHead();
            STLevData aSTLevData = new STLevData();
            STData aSTData = new STData();
            Variable aVar = getUpperVariables().get(vIdx);
            int varNum = VARDEF.getVNum();
            int uVarNum = getUpperVariables().size();
            if (uVarNum > 0) {
                varNum = varNum - uVarNum;
            }
            byte[] aBytes;

            gData.xArray = new double[this.getTimeNum()];
            for (i = 0; i < this.getTimeNum(); i++) {
                gData.xArray[i] = JDateUtil.toOADate(this.getTimes().get(i));
            }

            gData.yArray = new double[aVar.getLevelNum()];
            for (i = 0; i < aVar.getLevelNum(); i++) {
                gData.yArray[i] = i + 1;
            }

            gData.data = new double[aVar.getLevelNum()][this.getTimeNum()];

            stNum = 0;
            tNum = 0;
            do {
                aBytes = getByteArray(br, 8);
                //aSTDH.STID = System.Text.Encoding.Default.GetString(aBytes);
                aSTDH.STID = new String(aBytes, "UTF-8");

                aBytes = getByteArray(br, 4);
                aSTDH.Lat = DataConvert.bytes2Float(aBytes, _byteOrder);

                aBytes = getByteArray(br, 4);
                aSTDH.Lon = DataConvert.bytes2Float(aBytes, _byteOrder);

                aBytes = getByteArray(br, 4);
                aSTDH.T = DataConvert.bytes2Float(aBytes, _byteOrder);

                aBytes = getByteArray(br, 4);
                aSTDH.NLev = DataConvert.bytes2Int(aBytes);

                aBytes = getByteArray(br, 4);
                aSTDH.Flag = DataConvert.bytes2Int(aBytes);

                if (aSTDH.NLev > 0) {
                    stNum += 1;
                    aSTData.STHead = aSTDH;
                    aSTData.dataList = new ArrayList<>();
                    if (aSTDH.Flag == 1) //Has ground level
                    {
                        if (aSTDH.STID.equals(stID)) {
                            aSTLevData.data = new float[varNum];
                            for (i = 0; i < varNum; i++) {
                                aBytes = getByteArray(br, 4);
                                aSTLevData.data[i] = DataConvert.bytes2Float(aBytes, _byteOrder);
                            }
                            aSTLevData.lev = 0;
                            aSTData.dataList.add(aSTLevData);
                        } else {
                            br.skipBytes(varNum * 4);
                        }
                    }
                    if (aSTDH.NLev - aSTDH.Flag > 0) //Has upper level
                    {
                        if (aSTDH.STID.equals(stID)) {
                            for (i = 0; i < aSTDH.NLev - aSTDH.Flag; i++) {
                                br.skipBytes(4 + vIdx * 4);
                                aBytes = getByteArray(br, 4);
                                gData.data[i][tNum] = DataConvert.bytes2Float(aBytes, _byteOrder);
                                br.skipBytes((uVarNum - vIdx - 1) * 4);
                            }
                        } else {
                            br.skipBytes((aSTDH.NLev - aSTDH.Flag) * (uVarNum + 1) * 4);
                        }
                    }
                } else //End of time seriel
                {
                    stNum = 0;
                    if (tNum == getTimes().size() - 1) {
                        break;
                    }
                    tNum += 1;
                    if (br.getFilePointer() + 28 >= br.length()) {
                        break;
                    }
                }
            } while (true);

            br.close();

            return gData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private byte[] getByteArray(RandomAccessFile br, int n) {
        try {
            byte[] bytes = new byte[n];
            br.read(bytes);
//            if (isBigEndian) {
//                Collections.reverse(Arrays.asList(bytes));
//            }

            return bytes;
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Read GrADS station data
     *
     * @param timeIdx Time index
     * @return Station data list
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     */
    public List<STData> readGrADSData_Station(int timeIdx) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        List<STData> stDataList = new ArrayList<>();

        String filePath = DSET;
        int tIdx = timeIdx;
        if (OPTIONS.template) {
            Object[] result = getFilePath_Template(timeIdx);
            filePath = (String) result[0];
            tIdx = (Integer) result[1];
        }

        RandomAccessFile br = new RandomAccessFile(filePath, "r");
        int i, j, tNum;
        STDataHead aSTDH;
        STLevData aSTLevData;
        STData aSTData;
        int varNum = VARDEF.getVNum();
        int uVarNum = this.getUpperVariables().size();
        if (uVarNum > 0) {
            varNum = varNum - uVarNum;
        }
        byte[] aBytes;

        tNum = 0;
        if (OPTIONS.template) {
            timeIdx = 0;
        }
        do {
            aSTDH = new STDataHead();
            aBytes = getByteArray(br, 8);
            aSTDH.STID = new String(aBytes);

            aBytes = getByteArray(br, 4);
            aSTDH.Lat = DataConvert.bytes2Float(aBytes, _byteOrder);

            aBytes = getByteArray(br, 4);
            aSTDH.Lon = DataConvert.bytes2Float(aBytes, _byteOrder);

            aBytes = getByteArray(br, 4);
            aSTDH.T = DataConvert.bytes2Float(aBytes, _byteOrder);

            aBytes = getByteArray(br, 4);
            aSTDH.NLev = DataConvert.bytes2Int(aBytes, _byteOrder);

            aBytes = getByteArray(br, 4);
            aSTDH.Flag = DataConvert.bytes2Int(aBytes, _byteOrder);
            if (aSTDH.NLev > 0) {
                aSTData = new STData();
                aSTData.STHead = aSTDH;
                aSTData.dataList = new ArrayList<>();
                if (aSTDH.Flag == 1) //Has ground level
                {
                    aSTLevData = new STLevData();
                    aSTLevData.data = new float[varNum];
                    for (i = 0; i < varNum; i++) {
                        aBytes = getByteArray(br, 4);
                        aSTLevData.data[i] = DataConvert.bytes2Float(aBytes, _byteOrder);
                    }
                    aSTLevData.lev = 0;
                    aSTData.dataList.add(aSTLevData);
                }
                if (aSTDH.NLev - aSTDH.Flag > 0) //Has upper level
                {
                    for (i = 0; i < aSTDH.NLev - aSTDH.Flag; i++) {
                        aBytes = getByteArray(br, 4);
                        aSTLevData = new STLevData();
                        aSTLevData.lev = DataConvert.bytes2Float(aBytes, _byteOrder);
                        aSTLevData.data = new float[uVarNum];
                        for (j = 0; j < uVarNum; j++) {
                            aBytes = getByteArray(br, 4);
                            aSTLevData.data[j] = DataConvert.bytes2Float(aBytes, _byteOrder);
                        }
                        aSTData.dataList.add(aSTLevData);
                    }
                }

                if (tNum == tIdx) {
                    stDataList.add(aSTData);
                }
            } else //End of time seriel
            {
                if (tNum == tIdx) {
                    break;
                }
                tNum += 1;
                if (br.getFilePointer() + 28 >= br.length()) {
                    break;
                }
            }
        } while (true);

        br.close();

        return stDataList;
    }

    /**
     * Get ground station data
     *
     * @param stDataList Station data list
     * @param varIdx Variable index
     * @return Station data
     */
    public StationData getGroundStationData(List<STData> stDataList, int varIdx) {
        StationData stationData = new StationData();
        double[][] discretedData;
        List<float[]> disDataList = new ArrayList<>();
        STLevData aSTLevData;
        float lon, lat, aValue;
        float minX, maxX, minY, maxY;
        String stid;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        List<String> stations = new ArrayList<>();

        for (STData aSTData : stDataList) {
            if (aSTData.STHead.Flag == 1) {
                stid = aSTData.STHead.STID;
                lon = aSTData.STHead.Lon;
                lat = aSTData.STHead.Lat;
                aSTLevData = (STLevData) aSTData.dataList.get(0);
                aValue = aSTLevData.data[varIdx];
                stations.add(stid);
                disDataList.add(new float[]{lon, lat, aValue});
            }
        }

        discretedData = new double[disDataList.size()][3];
        int i = 0;
        for (float[] disData : disDataList) {
            discretedData[i][0] = disData[0];
            discretedData[i][1] = disData[1];
            discretedData[i][2] = disData[2];
            if (i == 0) {
                minX = disData[0];
                maxX = minX;
                minY = disData[1];
                maxY = minY;
            } else {
                if (minX > disData[0]) {
                    minX = disData[0];
                } else if (maxX < disData[0]) {
                    maxX = disData[0];
                }
                if (minY > disData[1]) {
                    minY = disData[1];
                } else if (maxY < disData[1]) {
                    maxY = disData[1];
                }
            }
            i++;
        }
        Extent dataExtent = new Extent();
        dataExtent.minX = minX;
        dataExtent.maxX = maxX;
        dataExtent.minY = minY;
        dataExtent.maxY = maxY;

        stationData.data = discretedData;
        stationData.dataExtent = dataExtent;
        stationData.stations = stations;
        stationData.missingValue = this.getMissingValue();

        return stationData;
    }

    @Override
    public StationData getStationData(int timeIdx, String varName, int levelIdx) {
        if (levelIdx == 0) {
            try {
                List<STData> stationData = readGrADSData_Station(timeIdx);
                int varIdx = this.getVariableIndex(varName);
                return getGroundStationData(stationData, varIdx);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (IOException ex) {
                Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
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

    // <editor-fold desc="Write data">
    /**
     * Add a time
     *
     * @param time The time
     */
    public void addTime(LocalDateTime time) {
        this.TDEF.times.add(time);
    }

    /**
     * Write GrADS control file
     */
    public void writeGrADSCTLFile() {
        try {
            File file = new File(this.getFileName());
            BufferedWriter sw = new BufferedWriter(new FileWriter(file));
            String aLine;
            int i;

            String fn = this.DSET;
            sw.write("DSET ^" + new File(fn).getName());
            sw.newLine();
            if (!DTYPE.equals("GRIDDED")) {
                sw.write("DTYPE " + DTYPE);
                sw.newLine();
            }
            sw.write("TITLE " + TITLE);
            sw.newLine();
            String line = "OPTIONS";
            if (OPTIONS.sequential) {
                line += " sequential";
            }
            if (OPTIONS.big_endian) {
                line += " big_endian";
            }
            if (line.length() > "OPTIONS".length()) {
                sw.write(line);
                sw.newLine();
            }
            sw.write("UNDEF " + String.valueOf(this.getMissingValue()));
            sw.newLine();

            if (DTYPE.equals("GRIDDED")) {
                aLine = "XDEF " + String.valueOf(XDEF.XNum) + " " + XDEF.Type;
                if (XDEF.Type.toUpperCase().equals("LINEAR")) {
                    aLine = aLine + " " + String.valueOf(XDEF.XMin) + " " + String.valueOf(XDEF.XDelt);
                } else {
                    for (i = 0; i < XDEF.XNum; i++) {
                        aLine = aLine + " " + String.valueOf(XDEF.X[i]);
                    }
                }
                sw.write(aLine);
                sw.newLine();
                aLine = "YDEF " + String.valueOf(YDEF.YNum) + " " + YDEF.Type;
                if (YDEF.Type.toUpperCase().equals("LINEAR")) {
                    aLine = aLine + " " + String.valueOf(YDEF.YMin) + " " + String.valueOf(YDEF.YDelt);
                } else {
                    for (i = 0; i < YDEF.YNum; i++) {
                        aLine = aLine + " " + String.valueOf(YDEF.Y[i]);
                    }
                }
                sw.write(aLine);
                sw.newLine();
                aLine = "ZDEF " + String.valueOf(ZDEF.ZNum) + " " + ZDEF.Type;
                if (ZDEF.Type.toUpperCase().equals("LINEAR")) {
                    aLine = aLine + " " + String.valueOf(ZDEF.SLevel) + " " + String.valueOf(ZDEF.ZDelt);
                } else {
                    for (i = 0; i < ZDEF.ZNum; i++) {
                        aLine = aLine + " " + String.valueOf(ZDEF.ZLevels[i]);
                    }
                }
                sw.write(aLine);
                sw.newLine();
            }

            aLine = "TDEF " + String.valueOf(TDEF.getTimeNum()) + " " + TDEF.Type;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm-ddMMMyyyy", Locale.ENGLISH);
            if (TDEF.Type.toUpperCase().equals("LINEAR")) {
                String tStr = formatter.format(TDEF.STime);
                tStr = tStr.replace("-", "Z");
                aLine = aLine + " " + tStr + " " + TDEF.TDelt;
                sw.write(aLine);
                sw.newLine();
            } else {
                sw.write(aLine);
                sw.newLine();
                for (i = 0; i < TDEF.getTimeNum(); i++) {
                    //aLine = aLine + " " + formatter.format(TDEF.times.get(i));
                    String tStr = formatter.format(TDEF.times.get(i));
                    tStr = tStr.replace("-", "Z");
                    sw.write("  " + tStr);
                    sw.newLine();
                }
            }

            sw.write("VARS " + String.valueOf(VARDEF.getVNum()));
            sw.newLine();
            for (i = 0; i < VARDEF.getVNum(); i++) {
                sw.write("  " + VARDEF.getVars().get(i).getName() + " " + VARDEF.getVars().get(i).getLevelNum() + " 99 "
                        + VARDEF.getVars().get(i).getDescription() + " (" + VARDEF.getVars().get(i).getUnits() + ")");
                sw.newLine();
            }
            sw.write("ENDVARS");
            sw.newLine();

            sw.flush();
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create a GrADS binary data file
     *
     * @param aFile
     * @throws java.io.IOException
     */
    public void createDataFile(String aFile) throws IOException {
        _bw = new DataOutputStream(new FileOutputStream(new File(aFile)));
    }

    /**
     * Close the data file created by prevoid step
     *
     * @throws java.io.IOException
     */
    public void closeDataFile() throws IOException {
        _bw.close();
    }

    /**
     * Write grid data to a GrADS binary data file
     *
     * @param gridData Grid data
     */
    public void writeGridData(GridData gridData) {
        try {
            writeGrADSData_Grid(_bw, gridData.data);
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write grid data to a GrADS binary data file
     *
     * @param gridData Grid data array
     */
    public void writeGridData(double[][] gridData) {
        try {
            writeGrADSData_Grid(_bw, gridData);
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write GrADS grid data
     *
     * @param bw EndianDataOutputStream
     * @param gridData Grid data array
     * @throws java.io.IOException
     */
    public void writeGrADSData_Grid(DataOutputStream bw, double[][] gridData) throws IOException {
        int i, j, k;
        int ynum = gridData.length;
        int xnum = gridData[0].length;

        EndianDataOutputStream ebw = new EndianDataOutputStream(bw);
        if (this.OPTIONS.sequential) {
            if (this._byteOrder == ByteOrder.BIG_ENDIAN) {
                ebw.writeIntBE(xnum * ynum * 4);
            } else {
                ebw.writeIntLE(xnum * ynum * 4);
            }
        }

        byte[] bytes = new byte[ynum * xnum * 4];
        byte[] bs;
        int p = 0;
        for (i = 0; i < ynum; i++) {
            for (j = 0; j < xnum; j++) {
                bs = DataConvert.float2Bytes((float) gridData[i][j], _byteOrder);
                for (k = 0; k < 4; k++) {
                    bytes[p + k] = bs[k];
                }
                p += 4;
            }
        }
        ebw.write(bytes, 0, bytes.length);

        if (this.OPTIONS.sequential) {
            if (this._byteOrder == ByteOrder.BIG_ENDIAN) {
                ebw.writeIntBE(xnum * ynum * 4);
            } else {
                ebw.writeIntLE(xnum * ynum * 4);
            }
        }
    }

    /**
     * Write undefine grid data GrADS data file
     */
    public void writeGridData_Null() {
        writeGrADSData_Grid_Null(_bw);
    }

    /**
     * Write undefine grid data to GrADS file
     *
     * @param bw DataOutputStream
     */
    public void writeGrADSData_Grid_Null(DataOutputStream bw) {
        double[][] gridData = new double[YDEF.YNum][XDEF.XNum];
        for (int i = 0; i < YDEF.YNum; i++) {
            for (int j = 0; j < XDEF.XNum; j++) {
                gridData[i][j] = this.getMissingValue();
            }
        }
        try {
            writeGrADSData_Grid(bw, gridData);
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write GrADS station data
     *
     * @param stInfoData Station info data
     */
    public void writeStationData(StationInfoData stInfoData) {
        try {
            writeStationData(_bw, stInfoData);
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write station info data
     *
     * @param bw DataOutputStream
     * @param stInfoData StationInfoData
     */
    private void writeStationData(DataOutputStream bw, StationInfoData stInfoData) throws IOException {
        int i, j;
        String aStid = "11111";
        float lon, lat, t, value;
        int nLev, flag;
        lon = 0;
        lat = 0;
        t = 0;
        nLev = 1;
        flag = 1;    //Has ground level
        List<String> dataList;
        int varNum = stInfoData.getVariables().size();
        //char[] st = new char[8];
        //byte[] stBytes = new byte[8];
        EndianDataOutputStream ebw = new EndianDataOutputStream(bw);

        for (i = 0; i < stInfoData.getDataList().size(); i++) {
            dataList = stInfoData.getDataList().get(i);
            aStid = dataList.get(0);
            lon = Float.parseFloat(dataList.get(1));
            lat = Float.parseFloat(dataList.get(2));

            //Write head  
            aStid = String.format("%1$-8s", aStid);
            //st = aStid.toCharArray();
            bw.write(aStid.getBytes());
            ebw.writeFloatLE(lat);
            ebw.writeFloatLE(lon);
            ebw.writeFloatLE(t);
            ebw.writeIntLE(nLev);
            ebw.writeIntLE(flag);

            //Write data
            for (j = 0; j < varNum; j++) {
                value = Float.parseFloat(dataList.get(j + 3));
                ebw.writeFloatLE(value);
            }
        }
        nLev = 0;    //End of a time
        //Write time end head
        aStid = String.format("%1$-8s", aStid);
        //st = aStid.toCharArray();
        bw.write(aStid.getBytes());
        ebw.writeFloatLE(lat);
        ebw.writeFloatLE(lon);
        ebw.writeFloatLE(t);
        ebw.writeIntLE(nLev);
        ebw.writeIntLE(flag);
    }

    /**
     * Write station data
     *
     * @param stData Station data
     */
    public void writeStationData(StationData stData) {
        try {
            writeStationData(_bw, stData);
        } catch (IOException ex) {
            Logger.getLogger(GrADSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write station info data
     *
     * @param bw DataOutputStream
     * @param stData StationData
     */
    private void writeStationData(DataOutputStream bw, StationData stData) throws IOException {
        int i;
        String aStid = "11111";
        float lon, lat, t, value;
        int nLev, flag;
        lon = 0;
        lat = 0;
        t = 0;
        nLev = 1;
        flag = 1;    //Has ground level
        EndianDataOutputStream ebw = new EndianDataOutputStream(bw);

        for (i = 0; i < stData.getStNum(); i++) {
            aStid = stData.getStid(i);
            lon = (float) stData.getX(i);
            lat = (float) stData.getY(i);

            //Write head  
            aStid = String.format("%1$-8s", aStid);
            //st = aStid.toCharArray();
            bw.write(aStid.getBytes());
            ebw.writeFloatLE(lat);
            ebw.writeFloatLE(lon);
            ebw.writeFloatLE(t);
            ebw.writeIntLE(nLev);
            ebw.writeIntLE(flag);

            //Write data
            value = (float) stData.getValue(i);
            ebw.writeFloatLE(value);
        }
        nLev = 0;    //End of a time
        //Write time end head
        aStid = String.format("%1$-8s", aStid);
        //st = aStid.toCharArray();
        bw.write(aStid.getBytes());
        ebw.writeFloatLE(lat);
        ebw.writeFloatLE(lon);
        ebw.writeFloatLE(t);
        ebw.writeIntLE(nLev);
        ebw.writeIntLE(flag);
    }
    // </editor-fold>
    // </editor-fold>   
}
