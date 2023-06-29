package org.meteoinfo.data.meteodata.radar;

import org.meteoinfo.common.DataConvert;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

public class SiteConfig {
    public static int length = 128;
    public String siteCode;
    public String siteName;
    public float latitude;
    public float longitude;
    public int antennaHeight;
    public int groundHeight;
    public float frequency;
    public float beamWidthHori;
    public float beamWidthVert;
    public int RADVersion;
    public short radarType;
    public short antennaGain;
    public short transmittingFeederLoss;
    public short receivingFeederLoss;
    public short otherLoss;
    public byte[] reserved;

    /**
     * Constructor
     * @param raf RandomAccessFile object
     */
    public SiteConfig(RandomAccessFile raf) throws IOException {
        byte[] bytes = new byte[8];
        raf.read(bytes);
        siteCode = new String(bytes).trim();
        bytes = new byte[32];
        raf.read(bytes);
        siteName = new String(bytes).trim();
        bytes = new byte[4];
        raf.read(bytes);
        latitude = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        longitude = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        antennaHeight = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        groundHeight = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        frequency = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        beamWidthHori = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        beamWidthVert = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        RADVersion = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        bytes = new byte[2];
        raf.read(bytes);
        radarType = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        antennaGain = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        transmittingFeederLoss = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        receivingFeederLoss = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        otherLoss = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved = new byte[46];
        raf.read(reserved);
    }

    /**
     * Constructor
     * @param raf InputStream object
     */
    public SiteConfig(InputStream raf) throws IOException {
        byte[] bytes = new byte[8];
        raf.read(bytes);
        siteCode = new String(bytes).trim();
        bytes = new byte[32];
        raf.read(bytes);
        siteName = new String(bytes).trim();
        bytes = new byte[4];
        raf.read(bytes);
        latitude = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        longitude = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        antennaHeight = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        groundHeight = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        frequency = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        beamWidthHori = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        beamWidthVert = DataConvert.bytes2Float(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        RADVersion = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
        bytes = new byte[2];
        raf.read(bytes);
        radarType = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        antennaGain = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        transmittingFeederLoss = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        receivingFeederLoss = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        raf.read(bytes);
        otherLoss = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
        reserved = new byte[46];
        raf.read(reserved);
    }

    /**
     * Get radar type string
     * @return Radar type string
     */
    public String getRadarType() {
        switch (radarType) {
            case 1:
                return "SA";
            case 2:
                return "SB";
            case 3:
                return "SC";
            case 4:
                return "SAD";
            case 5:
                return "SBD";
            case 6:
                return "SCD";
            case 33:
                return "CA";
            case 34:
                return "CB";
            case 35:
                return "CC";
            case 36:
                return "CCJ";
            case 37:
                return "CD";
            case 38:
                return "CAD";
            case 39:
                return "CBD";
            case 40:
                return "CCD";
            case 41:
                return "CCJD";
            case 42:
                return "CDD";
            case 65:
                return "XA";
            case 66:
                return "XAD";
            default:
                return "UNDEFINE";
        }
    }
}
