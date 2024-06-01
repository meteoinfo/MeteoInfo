package org.meteoinfo.data.meteodata.radar;

import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.meteoinfo.common.DataConvert;
import org.meteoinfo.data.meteodata.DataInfo;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

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
     * Get InputStream from file name.
     *
     * @param fileName The file name
     * @return The InputStream
     * @throws IOException
     */
    public static InputStream getInputStream(String fileName) throws IOException {
        String fileExtent = FilenameUtils.getExtension(fileName).toLowerCase();
        switch (fileExtent) {
            case "bz2":
                return new BZip2CompressorInputStream(Files.newInputStream(Paths.get(fileName)));
            case "gz":
                return new GzipCompressorInputStream(new FileInputStream(fileName));
            default:
                return new BufferedInputStream(Files.newInputStream(Paths.get(fileName)));
        }
    }

    /**
     * Get radar data type
     * @param fileName Data file name
     * @return Radar data type
     */
    public static RadarDataType getRadarDataType(String fileName) {
        try {
            InputStream inputStream = getInputStream(fileName);
            byte[] bytes = new byte[136];
            inputStream.read(bytes);
            inputStream.close();

            byte[] magicBytes = Arrays.copyOf(bytes, 4);
            int magic = DataConvert.bytes2Int(magicBytes, ByteOrder.LITTLE_ENDIAN);
            if (magic == 1297371986) {
                byte[] inBytes = Arrays.copyOf(bytes, GenericHeader.length);
                GenericHeader genericHeader = new GenericHeader(inBytes);
                switch (genericHeader.genericType) {
                    case 1:
                        return RadarDataType.STANDARD;
                    case 16:
                        return RadarDataType.PA;
                    default:
                        return null;
                }
            }

            String radarT = new String(bytes);
            if (radarT.contains("CINRAD/SC") || radarT.contains("CINRAD/CD")) {
                return RadarDataType.SC;
            } else if (radarT.contains("CINRADC")) {
                return RadarDataType.CC;
            } else if (!radarT.contains("CINRADC") && radarT.contains("CINRAD/CC")) {
                return RadarDataType.CC20;
            }

            magicBytes = Arrays.copyOf(bytes, 128);
            SABRadarDataInfo.RadialHeader radialHeader = new SABRadarDataInfo.RadialHeader(magicBytes);
            if (radialHeader.messageType != 1) {
                return null;
            }
            if (radialHeader.mSecond > 86400000) {
                return null;
            }
            LocalDateTime dateTime = radialHeader.getDateTime();
            if (dateTime.getYear() >= 1990 && dateTime.getYear() <= LocalDateTime.now().getYear()) {
                return RadarDataType.SAB;
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
                    return new StandardRadarDataInfo();
                case PA:
                    return new PARadarDataInfo();
                case SAB:
                    return new SABRadarDataInfo();
                case CC:
                    return new CCRadarDataInfo();
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
