package org.meteoinfo.data.meteodata.radar;

import org.meteoinfo.common.DataConvert;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

public class RadialHeader {
    public static int length = 64;
    public int radialState;
    public int spotBlank;
    public int sequenceNumber;
    public int radialNumber;
    public int elevationNumber;
    public float azimuth;
    public float elevation;
    public int seconds;
    public int microSeconds;
    public int lengthOfData;
    public int momentNumber;
    public byte[] reserved;
    public short horizontalEstimatedNoise;
    public short verticalEstimatedNoise;
    public byte[] reserved2;

    /**
     * Constructor
     * @param raf RandomAccessFile object
     */
    public RadialHeader(RandomAccessFile raf) throws IOException {
        byte[] bytes = new byte[4];
        raf.read(bytes);
        radialState = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        spotBlank = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        sequenceNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        radialNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        elevationNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        azimuth = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        elevation = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        seconds = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        microSeconds = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        lengthOfData = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        momentNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved = new byte[2];
        raf.read(reserved);
        bytes = new byte[2];
        raf.read(bytes);
        horizontalEstimatedNoise = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        verticalEstimatedNoise = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved2 = new byte[14];
        raf.read(reserved2);
    }

    /**
     * Constructor
     * @param raf InputStream object
     */
    public RadialHeader(InputStream raf) throws IOException {
        byte[] bytes = new byte[4];
        raf.read(bytes);
        radialState = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        spotBlank = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        sequenceNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        radialNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        elevationNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        azimuth = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        elevation = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        seconds = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        microSeconds = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        lengthOfData = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        momentNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved = new byte[2];
        raf.read(reserved);
        bytes = new byte[2];
        raf.read(bytes);
        horizontalEstimatedNoise = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        verticalEstimatedNoise = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved2 = new byte[14];
        raf.read(reserved2);
    }

    /**
     * Constructor
     * @param inBytes Input bytes
     */
    public RadialHeader(byte[] inBytes) throws IOException {
        int idx = 0;
        byte[] bytes = new byte[4];
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        radialState = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        spotBlank = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        sequenceNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        radialNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        elevationNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        azimuth = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        elevation = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        seconds = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        microSeconds = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        lengthOfData = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, bytes, 0, 4); idx += 4;
        momentNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved = new byte[2];
        System.arraycopy(inBytes, idx, reserved, 0, 2); idx += 2;
        bytes = new byte[2];
        System.arraycopy(inBytes, idx, bytes, 0, 2); idx += 2;
        horizontalEstimatedNoise = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        System.arraycopy(inBytes, idx, reserved, 0, 2); idx += 2;
        verticalEstimatedNoise = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved2 = new byte[14];
        System.arraycopy(inBytes, idx, reserved2, 0, 14);
    }
}
