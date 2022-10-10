package org.meteoinfo.data.meteodata.radar;

import org.meteoinfo.common.DataConvert;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

public class MomentHeader {
    public static int length = 32;
    public int dataType;
    public int scale;
    public int offset;
    public short binLength;
    public short flags;
    public int dataLength;
    public byte[] reserved;

    /**
     * Constructor
     * @param raf RandomAccessFile object
     */
    public MomentHeader(RandomAccessFile raf) throws IOException {
        byte[] bytes = new byte[4];
        raf.read(bytes);
        dataType = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        scale = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        offset = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        bytes = new byte[2];
        raf.read(bytes);
        binLength = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        flags = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        bytes = new byte[4];
        raf.read(bytes);
        dataLength = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved = new byte[12];
        raf.read(reserved);
    }

    /**
     * Constructor
     * @param raf InputStream object
     */
    public MomentHeader(InputStream raf) throws IOException {
        byte[] bytes = new byte[4];
        raf.read(bytes);
        dataType = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        scale = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        offset = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        bytes = new byte[2];
        raf.read(bytes);
        binLength = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        flags = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        bytes = new byte[4];
        raf.read(bytes);
        dataLength = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved = new byte[12];
        raf.read(reserved);
    }
}
