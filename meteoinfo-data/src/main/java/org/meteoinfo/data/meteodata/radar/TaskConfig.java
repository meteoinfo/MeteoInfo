package org.meteoinfo.data.meteodata.radar;

import org.meteoinfo.common.DataConvert;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class TaskConfig {
    public static int length = 256;
    public String taskName;
    public String taskDescription;
    public int polarizationType;
    public int scanType;
    public int pulseWidth;
    public LocalDateTime scanStartTime;
    public int cutNumber;
    public float horizontalNoise;
    public float verticalNoise;
    public float horizontalCalibration;
    public float verticalCalibration;
    public float horizontalNoiseTemperature;
    public float verticalNoiseTemperature;
    public float ZDRCalibration;
    public float PHIDRCalibration;
    public float LDRCalibration;
    public byte[] reserves;

    /**
     * Constructor
     * @param raf InputStream object
     */
    public TaskConfig(InputStream raf) throws IOException {
        byte[] bytes = new byte[32];
        raf.read(bytes);
        taskName = new String(bytes).trim();
        bytes = new byte[128];
        raf.read(bytes);
        taskDescription = new String(bytes).trim();
        bytes = new byte[4];
        raf.read(bytes);
        polarizationType = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        scanType = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        pulseWidth = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        int seconds = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        LocalDateTime dt = LocalDateTime.of(1970, 1, 1, 0, 0);
        scanStartTime = dt.plusSeconds(seconds);
        raf.read(bytes);
        cutNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        horizontalNoise = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        verticalNoise = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        horizontalCalibration = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        verticalCalibration = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        horizontalNoiseTemperature = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        verticalNoiseTemperature = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        ZDRCalibration = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        PHIDRCalibration = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        LDRCalibration = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        reserves = new byte[40];
        raf.read(reserves);
    }
}
