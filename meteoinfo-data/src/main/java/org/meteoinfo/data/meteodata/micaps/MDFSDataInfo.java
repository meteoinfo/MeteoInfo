package org.meteoinfo.data.meteodata.micaps;

import org.meteoinfo.common.DataConvert;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.util.JDateUtil;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;
import org.meteoinfo.dataframe.Column;
import org.meteoinfo.dataframe.ColumnIndex;
import org.meteoinfo.dataframe.DataFrame;
import org.meteoinfo.dataframe.Index;
import org.meteoinfo.data.meteodata.*;
import org.meteoinfo.ndarray.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MDFSDataInfo extends DataInfo implements IGridDataInfo, IStationDataInfo {
    // <editor-fold desc="Variables">
    private int type;
    private String modelName;
    private String element;
    private String description;
    float level;
    private int numLon, numLat, numStation;
    private boolean yReverse = false;
    private Map varMap;
    private Map dataTypeMap;
    private List<String> variableNames = new ArrayList<>();
    private DataFrame dataFrame;
    // </editor-fold>

    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MDFSDataInfo() {
        this.setDataType(MeteoDataType.MICAPS_MDFS);
    }
    // </editor-fold>

    // <editor-fold desc="Get Set Methods">

    /**
     * Get data type
     * @return Data type
     */
    public int getType() {
        return this.type;
    }
    // </editor-fold>

    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        this.setFileName(fileName);
        try {
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            byte[] bytes = new byte[4];
            br.read(bytes);
            String desc = new String(bytes).trim();
            bytes = new byte[2];
            br.read(bytes);
            type = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
            this.addAttribute(new Attribute("data_format", "MICAPS MDFS"));
            switch (type) {
                case 1:
                case 2:
                    bytes = new byte[100];
                    br.read(bytes);
                    description = new String(bytes, "GBK").trim();
                    bytes = new byte[4];
                    br.read(bytes);
                    level = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    bytes = new byte[50];
                    br.read(bytes);
                    String levDesc = new String(bytes, "GBK").trim();
                    bytes = new byte[4];
                    br.read(bytes);
                    int year = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    int month = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    int day = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    int hour = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    int minute = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    int second = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    int timeZone = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);

                    br.skipBytes(100);

                    br.read(bytes);
                    this.numStation = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    bytes = new byte[2];
                    br.read(bytes);
                    int varNum = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);

                    LocalDateTime dt = LocalDateTime.of(year, month, day, hour, 0);
                    Dimension tDim = new Dimension(DimensionType.T);
                    tDim.setName("time");
                    tDim.addValue(JDateUtil.toOADate(dt));
                    this.setTimeDimension(tDim);
                    this.addDimension(tDim);
                    Dimension zDim = new Dimension(DimensionType.Z);
                    zDim.setName("level");
                    zDim.setValues(new float[]{level});
                    this.setZDimension(zDim);
                    this.addDimension(zDim);
                    Dimension stDim = new Dimension(DimensionType.Other);
                    stDim.setName("station");
                    float[] values = new float[numStation];
                    for (int i = 0; i < numStation; i++) {
                        values[i] = i;
                    }
                    stDim.setValues(values);
                    this.addDimension(stDim);

                    int varId;
                    int dataTypeId;
                    this.initVarMap();
                    this.initDataTypeMap();
                    String varName;
                    Variable var = new Variable();
                    varName = "Stid";
                    var.setName(varName);
                    var.setStation(false);
                    var.setDataType(DataType.STRING);
                    var.setDimension(tDim);
                    var.setDimension(zDim);
                    var.setDimension(stDim);
                    var.addAttribute("name", varName);
                    this.addVariable(var);
                    this.variableNames.add(varName);
                    var = new Variable();
                    varName = "Longitude";
                    var.setName(varName);
                    var.setStation(true);
                    var.setDataType(DataType.FLOAT);
                    var.setDimension(tDim);
                    var.setDimension(zDim);
                    var.setDimension(stDim);
                    var.addAttribute("name", varName);
                    this.addVariable(var);
                    this.variableNames.add(varName);
                    var = new Variable();
                    varName = "Latitude";
                    var.setName(varName);
                    var.setStation(true);
                    var.setDataType(DataType.FLOAT);
                    var.setDimension(tDim);
                    var.setDimension(zDim);
                    var.setDimension(stDim);
                    var.addAttribute("name", varName);
                    this.variableNames.add(varName);
                    this.addVariable(var);
                    for (int i = 0; i < varNum; i++) {
                        br.read(bytes);
                        varId = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
                        br.read(bytes);
                        dataTypeId = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
                        var = new Variable();
                        varName = this.getVariableName(varId);
                        var.setName(varName);
                        var.setStation(true);
                        var.setDataType((DataType) this.dataTypeMap.get(dataTypeId));
                        var.setDimension(tDim);
                        var.setDimension(zDim);
                        var.setDimension(stDim);
                        var.addAttribute("name", varName);
                        this.addVariable(var);
                        this.variableNames.add(varName);
                    }

                    this.dataFrame = this.readDataFrame(br);

                    break;
                default:
                    bytes = new byte[20];
                    br.read(bytes);
                    modelName = new String(bytes, "GBK").trim();
                    bytes = new byte[50];
                    br.read(bytes);
                    element = new String(bytes, "GBK").trim();
                    bytes = new byte[30];
                    br.read(bytes);
                    description = new String(bytes, "GBK").trim();
                    bytes = new byte[4];
                    br.read(bytes);
                    level = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    year = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    month = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    day = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    hour = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    timeZone = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    int period = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float sLon = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float eLon = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float deltaLon = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    numLon = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float sLat = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float eLat = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float deltaLat = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    numLat = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float isolineStartValue = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float isolineEndValue = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);
                    float isolineSpace = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.skipBytes(2);
                    int nMembers = br.readByte();
                    nMembers = nMembers == 0 ? 1 : nMembers;
                    br.skipBytes(97);

                    dt = LocalDateTime.of(year, month, day, hour, 0);
                    tDim = new Dimension(DimensionType.T);
                    tDim.setName("time");
                    tDim.addValue(JDateUtil.toOADate(dt));
                    this.setTimeDimension(tDim);
                    this.addDimension(tDim);
                    zDim = new Dimension(DimensionType.Z);
                    zDim.setName("level");
                    zDim.setValues(new float[]{level});
                    this.setZDimension(zDim);
                    this.addDimension(zDim);
                    Dimension yDim = new Dimension(DimensionType.Y);
                    if (deltaLat < 0) {
                        this.yReverse = true;
                        float temp = eLat;
                        eLat = sLat;
                        sLat = temp;
                        deltaLat = -deltaLat;
                    }
                    double[] Y = new double[numLat];
                    for (int i = 0; i < numLat; i++) {
                        Y[i] = sLat + i * deltaLat;
                    }
                    yDim.setValues(Y);
                    this.addDimension(yDim);
                    this.setYDimension(yDim);
                    Dimension xDim = new Dimension(DimensionType.X);
                    double[] X = new double[numLon];
                    for (int i = 0; i < numLon; i++) {
                        X[i] = sLon + i * deltaLon;
                    }
                    xDim.setValues(X);
                    this.addDimension(xDim);
                    this.setXDimension(xDim);

                    switch (this.type) {
                        case 4:
                            if (nMembers == 1) {
                                var = new Variable();
                                var.setName(this.element);
                                var.setDataType(DataType.FLOAT);
                                var.setDimension(tDim);
                                var.setDimension(zDim);
                                var.setDimension(yDim);
                                var.setDimension(xDim);
                                this.addVariable(var);
                                this.variableNames.add(this.element);
                            } else {
                                for (int i = 0; i < nMembers; i++) {
                                    String vName = this.element + "_" + String.valueOf(i + 1);
                                    var = new Variable();
                                    var.setName(vName);
                                    var.setDataType(DataType.FLOAT);
                                    var.setDimension(tDim);
                                    var.setDimension(zDim);
                                    var.setDimension(yDim);
                                    var.setDimension(xDim);
                                    this.addVariable(var);
                                    this.variableNames.add(vName);
                                }
                            }
                            break;
                        case 11:
                            var = new Variable();
                            var.setName("WindSpeed");
                            var.setDataType(DataType.FLOAT);
                            var.setDimension(tDim);
                            var.setDimension(zDim);
                            var.setDimension(yDim);
                            var.setDimension(xDim);
                            this.addVariable(var);
                            this.variableNames.add("WindSpeed");
                            var = new Variable();
                            var.setName("WindDirection");
                            var.setDataType(DataType.FLOAT);
                            var.setDimension(tDim);
                            var.setDimension(zDim);
                            var.setDimension(yDim);
                            var.setDimension(xDim);
                            this.addVariable(var);
                            this.variableNames.add("WindDirection");
                            break;
                    }
                    break;
            }

            br.close();
        } catch (IOException ex) {
            Logger.getLogger(MDFSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Object getValue(byte[] bytes, DataType dataType) {
        switch (dataType) {
            case BYTE:
                return bytes[0];
            case SHORT:
                return DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
            case INT:
                return DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            case LONG:
                return DataConvert.bytes2Long(bytes, ByteOrder.LITTLE_ENDIAN);
            case FLOAT:
                return DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
            case DOUBLE:
                return DataConvert.bytes2Double(bytes, ByteOrder.LITTLE_ENDIAN);
            default:
                return new String(bytes);
        }
    }

    private DataFrame readDataFrame(RandomAccessFile br) throws IOException {
        List<Array> data = new ArrayList<>();
        ColumnIndex columns = new ColumnIndex();
        List<Integer> varSizes = new ArrayList<>();
        for (Variable var : this.variables) {
            varSizes.add(this.getDataTypeSize(var.getDataType()));
            columns.add(new Column(var.getName(), var.getDataType()));
            data.add(Array.factory(var.getDataType(), new int[]{numStation}));
        }

        List<String> idxList = new ArrayList<>();
        byte[] bytes;
        int stId, numVar, varId, idx;
        float lon, lat;
        String varName;
        for (int i = 0; i < numStation; i++) {
            bytes = new byte[4];
            br.read(bytes);
            stId = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            idxList.add(String.valueOf(stId));
            data.get(0).setString(i, String.valueOf(stId));
            br.read(bytes);
            lon = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
            data.get(1).setFloat(i, lon);
            br.read(bytes);
            lat = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
            data.get(2).setFloat(i, lat);
            bytes = new byte[2];
            br.read(bytes);
            numVar = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
            for (int j = 0; j < numVar; j++) {
                bytes = new byte[2];
                br.read(bytes);
                varId = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
                if (varId % 2 == 0 && varId > 21) {
                    br.readByte();
                } else {
                    varName = this.getVariableName(varId);
                    idx = this.variableNames.indexOf(varName);
                    bytes = new byte[varSizes.get(idx)];
                    br.read(bytes);
                    data.get(idx).setObject(i, this.getValue(bytes, columns.get(idx).getDataType()));
                }
            }
        }

        Index index = Index.factory(idxList);
        DataFrame df = new DataFrame(data, index, columns);
        return df;
    }

    /**
     * Read data frame
     * @return Data frame
     */
    public DataFrame readDataFrame() {
        return this.dataFrame;
    }

    @Override
    public Array read(String varName) {
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

    private byte[] readDataBytes(String varName) {
        int length = numLat * numLon * 4;
        byte[] bytes = new byte[length];
        try {
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            switch (this.type) {
                case 11:
                    br.skipBytes(278);
                    if (varName.equals("WindDirection")) {
                        br.skipBytes(length);
                    }
                    br.read(bytes);
                    break;
                case 4:
                    int idx = this.variableNames.indexOf(varName);
                    if (idx > 0) {
                        br.skipBytes(idx * (278 + length));
                    }
                    br.skipBytes(278);
                    br.read(bytes);
                    break;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }

    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        try {
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            IndexIterator ii = dataArray.getIndexIterator();
            int rangeIdx = 2;
            switch (this.type) {
                case 1:
                case 2:
                    Range stRange = section.getRange(rangeIdx);
                    Array array = this.dataFrame.getColumnData(varName);
                    for (int i = stRange.first(); i <= stRange.last(); i += stRange.stride()) {
                        ii.setObjectNext(array.getObject(i));
                    }
                    break;
                case 4:
                case 11:
                    byte[] dataBytes = this.readDataBytes(varName);
                    Range yRange = section.getRange(rangeIdx++);
                    Range xRange = section.getRange(rangeIdx);
                    int xNum = this.numLon;
                    int index;
                    byte[] bytes = new byte[4];
                    float v;
                    for (int y = yRange.first(); y <= yRange.last(); y += yRange.stride()) {
                        for (int x = xRange.first(); x <= xRange.last(); x += xRange.stride()) {
                            if (this.yReverse)
                                index = (numLat - y - 1) * xNum + x;
                            else
                                index = y * xNum + x;
                            System.arraycopy(dataBytes, index * 4, bytes, 0, 4);
                            v = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
                            ii.setFloatNext(v);
                        }
                    }
                    break;
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(MDFSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<Attribute> getGlobalAttributes() {
        return null;
    }

    @Override
    public GridArray getGridArray(String varName) {
        return null;
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, String varName, int levelIdx) {
        byte[] dataBytes = this.readDataBytes(varName);
        double[][] data = new double[numLat][numLon];
        int index;
        byte[] bytes = new byte[4];
        for (int i = 0; i < numLat; i++) {
            for (int j = 0; j < numLon; j++) {
                if (this.yReverse)
                    index = (numLat - i - 1) * numLon + j;
                else
                    index = i * numLon + j;
                System.arraycopy(dataBytes, index * 4, bytes, 0, 4);
                data[i][j] = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
            }
        }

        GridData gridData = new GridData();
        gridData.data = data;
        gridData.xArray = this.getXDimension().getValues();
        gridData.yArray = this.getYDimension().getValues();
        gridData.missingValue = this.getMissingValue();

        return gridData;
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, String varName, int lonIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public StationData getStationData(int timeIdx, String varName, int levelIdx) {
        double[][] data = new double[this.numStation][3];
        double minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        List<String> stations = new ArrayList<>();
        double lon, lat, v;
        try {
            Array array = this.dataFrame.getColumnData(varName);
            Array stArray = this.dataFrame.getColumnData("Stid");
            Array lonArray = this.dataFrame.getColumnData("Longitude");
            Array latArray = this.dataFrame.getColumnData("Latitude");
            for (int i = 0; i < numStation; i++) {
                stations.add(stArray.getString(i));
                lon = lonArray.getDouble(i);
                lat = latArray.getDouble(i);
                v = array.getDouble(i);
                data[i][0] = lon;
                data[i][1] = lat;
                data[i][2] = v;

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
            stData.data = data;
            stData.stations = stations;
            stData.dataExtent = dataExtent;
            stData.missingValue = this.getMissingValue();

            return stData;

        } catch (InvalidRangeException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        StationInfoData stInfoData = new StationInfoData();
        stInfoData.setDataFrame(this.dataFrame);
        stInfoData.setStations(this.dataFrame.getIndex().getValues());

        return stInfoData;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        return null;
    }

    private String getVariableName(int varId) {
        if (this.varMap.containsKey(varId)) {
            return (String)varMap.get(varId);
        } else {
            return "Undefine";
        }
    }

    private int getDataTypeSize(DataType dataType) {
        switch (dataType) {
            case STRING:
                return 1;
            default:
                return dataType.getSize();
        }
    }

    private void initDataTypeMap() {
        dataTypeMap = new HashMap();
        dataTypeMap.put(1, DataType.BYTE);
        dataTypeMap.put(2, DataType.SHORT);
        dataTypeMap.put(3, DataType.INT);
        dataTypeMap.put(4, DataType.LONG);
        dataTypeMap.put(5, DataType.FLOAT);
        dataTypeMap.put(6, DataType.DOUBLE);
        dataTypeMap.put(7, DataType.STRING);
    }

    private void initVarMap() {
        this.varMap = new HashMap();
        //Geo info
        varMap.put(1, "Longitude");
        varMap.put(2, "Latitude");
        varMap.put(3, "Altitude");
        varMap.put(4, "Grade");
        varMap.put(5, "StationType");
        varMap.put(6, "PressHeight");
        varMap.put(7, "HumidityHeight");
        varMap.put(8, "HumidityHeightWater");
        varMap.put(9, "WindHeight");
        varMap.put(10, "WindHeightPlat");
        varMap.put(11, "WindHeightWater");
        varMap.put(12, "MoveDirection");
        varMap.put(13, "MoveSpeed");
        varMap.put(14, "SeaSaltDepth");
        varMap.put(15, "WaveHeightAlt");
        varMap.put(16, "BuoyPos");
        varMap.put(17, "WaterDepth");
        varMap.put(18, "UnderWaterDepth");
        varMap.put(19, "BoatSeaHeight");
        varMap.put(20, "Azimuth");
        varMap.put(21, "StationName");
        //Wind
        varMap.put(201, "WindDirection");
        varMap.put(203, "WindSpeed");
        varMap.put(205, "WD_1Min");
        varMap.put(207, "WS_1Min");
        varMap.put(209, "WD_2Min");
        varMap.put(211, "WS_2Min");
        varMap.put(213, "WD_10Min");
        varMap.put(215, "WS_10Min");
        varMap.put(217, "WD_Max");
        varMap.put(219, "WS_Max");
        varMap.put(221, "WD_Inst");
        varMap.put(223, "WS_Inst");
        varMap.put(225, "WD_Extra");
        varMap.put(227, "WS_Extra");
        varMap.put(229, "WD_6H");
        varMap.put(231, "WS_6H");
        varMap.put(233, "WD_12H");
        varMap.put(235, "WS_12H");
        varMap.put(237, "WindForce");
        //Pressure
        varMap.put(401, "Pressure_SeaLevel");
        varMap.put(403, "Pressure_Var_3H");
        varMap.put(405, "Pressure_Var_24H");
        varMap.put(407, "Pressure_Station");
        varMap.put(409, "Pressure_Max");
        varMap.put(411, "Pressure_Min");
        varMap.put(413, "Pressure");
        varMap.put(415, "Pressure_DayAve");
        varMap.put(417, "Pressure_SeaLevel_DayAve");
        varMap.put(419, "Height");
        varMap.put(421, "HGT");
        //Temperature
        varMap.put(601, "Temperature");
        varMap.put(603, "Temperature_Max");
        varMap.put(605, "Temperature_Min");
        varMap.put(607, "Temperature_Var_24H");
        varMap.put(609, "Temperature_Max_24H");
        varMap.put(611, "Temperature_Min_24H");
        varMap.put(613, "Temperature_DayAve");
        //Humidity
        varMap.put(801, "DewPoint");
        varMap.put(803, "T_Td_Diff");
        varMap.put(805, "RH");
        varMap.put(807, "RH_Min");
        varMap.put(809, "RH_DayAve");
        varMap.put(811, "Vapor_Pressure");
        varMap.put(813, "Vapor_Pressure_DayAve");
        //Precipitation
        varMap.put(1001, "Precipitation");
        varMap.put(1003, "Precipitation_1H");
        varMap.put(1005, "Precipitation_3H");
        varMap.put(1007, "Precipitation_6H");
        varMap.put(1009, "Precipitation_12H");
        varMap.put(1011, "Precipitation_24H");
        varMap.put(1013, "Precipitation_DaySum");
        varMap.put(1015, "Precipitation_20-08");
        varMap.put(1017, "Precipitation_08-20");
        varMap.put(1019, "Precipitation_20-20");
        varMap.put(1021, "Precipitation_08-08");
        varMap.put(1023, "Evaporation");
        varMap.put(1025, "Evaporation_Large");
        varMap.put(1027, "Precipitate");
        //Visibility
        varMap.put(1201, "Visibility_1Min");
        varMap.put(1203, "Visibility_10Min");
        varMap.put(1205, "Visibility_Minimum");
        varMap.put(1207, "Visibility");
        //Cloud
        varMap.put(1401, "Cloud_Total");
        varMap.put(1403, "Cloud_Low");
        varMap.put(1405, "Cloud_Base_Height");
        varMap.put(1407, "Cloudiness_Low");
        varMap.put(1409, "Cloudiness_Middle");
        varMap.put(1411, "Cloudiness_High");
        varMap.put(1413, "Cloud_Total_DayAve");
        varMap.put(1415, "Cloud_Low_DayAve");
        varMap.put(1417, "Cloud_Cover");
        varMap.put(1419, "Cloud_Type");
        //Weather
        varMap.put(1601, "Weather_Now");
        varMap.put(1603, "Weather_Past1");
        varMap.put(1605, "Weather_Past2");
        //Important weather
        varMap.put(1801, "Tornado_Type");
        varMap.put(1803, "Tornado_Pos");
        varMap.put(1805, "Hail_Diameter_Max");
        varMap.put(1807, "Thunderstorm");
        varMap.put(1809, "Lighting_Intensity");
    }
    // </editor-fold>
}
