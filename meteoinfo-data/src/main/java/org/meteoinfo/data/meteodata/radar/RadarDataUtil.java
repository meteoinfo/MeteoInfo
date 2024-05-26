package org.meteoinfo.data.meteodata.radar;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.meteoinfo.common.DataConvert;
import org.meteoinfo.data.meteodata.DataInfo;

import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class RadarDataUtil {

    /**
     * Get radar data type
     * @param raf RandomAccessFile object
     * @return Radar data type
     */
    public static RadarDataType getRadarDataType(RandomAccessFile raf) {
        try {
            raf.seek(0);
            byte[] bytes = new byte[136];
            raf.read(bytes);
            byte[] magicBytes = Arrays.copyOf(bytes, 4);
            int magic = DataConvert.bytes2Int(magicBytes, ByteOrder.LITTLE_ENDIAN);
            if (magic == 1297371986) {
                return RadarDataType.STANDARD;
            }

            magicBytes = Arrays.copyOfRange(bytes, 14, 16);
            if (Arrays.equals(magicBytes, new byte[]{1, 0})) {
                return RadarDataType.SAB;
            }

            magicBytes = Arrays.copyOfRange(bytes, 8, 12);
            if (Arrays.equals(magicBytes, new byte[]{16, 0, 0, 0})) {
                return RadarDataType.PA;
            }

            String radarT = new String(bytes);
            if (radarT.contains("CINRAD/SC") || radarT.contains("CINRAD/CD")) {
                return RadarDataType.SC;
            } else if (radarT.contains("CINRADC")) {
                return RadarDataType.CC;
            } else if (!radarT.contains("CINRADC") && radarT.contains("CINRAD/CC")) {
                return RadarDataType.CC20;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    /**
     * Get radar data type
     * @param fileName Data file name
     * @return Radar data type
     */
    public static RadarDataType getRadarDataType(String fileName) {
        try {
            byte[] bytes = new byte[136];
            if (fileName.endsWith("bz2")) {
                BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(Files.newInputStream(Paths.get(fileName)));
                inputStream.read(bytes);
                inputStream.close();
            } else {
                RandomAccessFile raf = new RandomAccessFile(fileName, "r");
                raf.seek(0);
                raf.read(bytes);
                raf.close();
            }
            byte[] magicBytes = Arrays.copyOf(bytes, 4);
            int magic = DataConvert.bytes2Int(magicBytes, ByteOrder.LITTLE_ENDIAN);
            if (magic == 1297371986) {
                return RadarDataType.STANDARD;
            }

            magicBytes = Arrays.copyOfRange(bytes, 14, 16);
            if (Arrays.equals(magicBytes, new byte[]{1, 0})) {
                return RadarDataType.SAB;
            }

            magicBytes = Arrays.copyOfRange(bytes, 8, 12);
            if (Arrays.equals(magicBytes, new byte[]{16, 0, 0, 0})) {
                return RadarDataType.PA;
            }

            String radarT = new String(bytes);
            if (radarT.contains("CINRAD/SC") || radarT.contains("CINRAD/CD")) {
                return RadarDataType.SC;
            } else if (radarT.contains("CINRADC")) {
                return RadarDataType.CC;
            } else if (!radarT.contains("CINRADC") && radarT.contains("CINRAD/CC")) {
                return RadarDataType.CC20;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    /**
     * Get radar data info
     * @param radarDataType Radar data type
     * @return The DataInfo object
     */
    public static DataInfo getDataInfo(RadarDataType radarDataType) {
        if (radarDataType == null) {
            return null;
        } else {
            switch (radarDataType) {
                case STANDARD:
                    return new CMARadarBaseDataInfo();
                case SAB:
                    return new SABRadarDataInfo();
                /*case CC:
                    return new CCRadarDataInfo();*/
                case SC:
                    return new SCRadarDataInfo();
                default:
                    return null;
            }
        }
    }

    /**
     * Get radar data info
     * @param raf RandomAccessFile object
     * @return The DataInfo object
     */
    public static DataInfo getDataInfo(RandomAccessFile raf) {
        RadarDataType radarDataType = getRadarDataType(raf);
        return getDataInfo(radarDataType);
    }

    /**
     * Get radar data info
     * @param fileName Data file name
     * @return The DataInfo object
     */
    public static DataInfo getDataInfo(String fileName) {
        RadarDataType radarDataType = getRadarDataType(fileName);
        return getDataInfo(radarDataType);
    }
}
