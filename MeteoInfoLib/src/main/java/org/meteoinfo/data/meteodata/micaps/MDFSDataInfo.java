package org.meteoinfo.data.meteodata.micaps;

import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.*;
import org.meteoinfo.data.meteodata.awx.AWXDataInfo;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.ndarray.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MDFSDataInfo extends DataInfo implements IGridDataInfo, IStationDataInfo {
    // <editor-fold desc="Variables">
    int type;
    String modelName;
    String element;
    String description;
    float level;
    private int numLon, numLat;
    private byte[] dataBytes;
    private boolean yReverse = false;
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
                    br.read(bytes);
                    int timeZone = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
                    br.read(bytes);

                    br.skipBytes(100);


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
                    br.skipBytes(100);

                    switch (this.type) {
                        case 4:
                            this.dataBytes = new byte[numLat * numLon * 4];
                            br.read(dataBytes);
                            break;
                        case 11:
                            this.dataBytes = new byte[numLat * numLon * 8];
                            break;
                    }

                    LocalDateTime dt = LocalDateTime.of(year, month, day, hour, 0);
                    Dimension tDim = new Dimension(DimensionType.T);
                    tDim.addValue(JDateUtil.toOADate(dt));
                    this.setTimeDimension(tDim);
                    this.addDimension(tDim);
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

                    Variable var = new Variable();
                    var.setName(this.element);
                    var.setDataType(DataType.FLOAT);
                    var.setDimension(tDim);
                    var.setDimension(yDim);
                    var.setDimension(xDim);
                    this.addVariable(var);
                    break;
            }

            br.close();
        } catch (IOException ex) {
            Logger.getLogger(MDFSDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        try {
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            int rangeIdx = 1;
            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);
            IndexIterator ii = dataArray.getIndexIterator();
            int xNum = this.numLon;
            int index;
            byte[] bytes = new byte[4];
            for (int y = yRange.first(); y <= yRange.last(); y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last(); x += xRange.stride()) {
                    if (this.yReverse)
                        index = (numLat - y - 1) * xNum + x;
                    else
                        index = y * xNum + x;
                    System.arraycopy(dataBytes, index * 4, bytes, 0, 4);
                    ii.setFloatNext(DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN));
                }
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(MICAPS4DataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        return null;
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        return null;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        return null;
    }
    // </editor-fold>
}
