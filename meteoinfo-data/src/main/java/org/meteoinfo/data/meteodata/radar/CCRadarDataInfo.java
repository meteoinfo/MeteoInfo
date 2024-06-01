package org.meteoinfo.data.meteodata.radar;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CCRadarDataInfo extends BaseRadarDataInfo implements IRadarDataInfo {

    private List<CutConfig> cutConfigs;
    private int radarHeaderSize = 1024;
    private int perRadialSize = 3000;
    private int messageHeaderSize = 28;

    @Override
    public boolean isValidFile(java.io.RandomAccessFile raf) {
        return false;
    }

    void setScaleOffset(RadialRecord record) {
        switch (record.product) {
            case "dBZ":
            case "V":
            case "W":
                record.scale = 0.1f;
                record.offset = 0.f;
                break;
        }
    }

    @Override
    void readDataInfo(InputStream is) {
        try {
            byte[] headerBytes = new byte[this.radarHeaderSize];
            is.read(headerBytes);
            byte[] bytes = Arrays.copyOf(headerBytes, RadarHeader.length);
            RadarHeader radarHeader = new RadarHeader(bytes);

            int sweepN = radarHeader.getSweepNumber();
            cutConfigs = new ArrayList<>();
            int idx = RadarHeader.length;
            for (int i = 0; i < sweepN; i++) {
                bytes = Arrays.copyOfRange(headerBytes, idx, idx + CutConfig.length);
                idx += CutConfig.length;
                CutConfig cutConfig = new CutConfig(bytes);
                cutConfigs.add(cutConfig);
                if (i == 0) {
                    this.logResolution = cutConfig.usBindWidth;
                    this.dopplerResolution = cutConfig.usBindWidth;
                }
            }

            idx = 878;
            bytes = Arrays.copyOfRange(headerBytes, idx, idx + RadarHeader2.length);
            RadarHeader2 radarHeader2 = new RadarHeader2(bytes);

            List<String> products = new ArrayList<>(Arrays.asList("dBZ", "V", "W"));
            for (String product : products) {
                RadialRecord record = new RadialRecord(product);
                record.setRadarDataType(RadarDataType.CC);
                record.setBinLength(2);
                record.setDataType(DataType.SHORT);
                setScaleOffset(record);
                this.recordMap.put(product, record);
            }

            for (int i = 0; i < sweepN; i++) {
                CutConfig cutConfig = cutConfigs.get(i);
                int radialN = cutConfig.usBinNumber;
                float azimuth = 0;
                float aDelta = 360.f / cutConfig.usRecordNumber;
                for (int j = 0; j < cutConfig.usRecordNumber; j++) {
                    bytes = new byte[this.perRadialSize];
                    is.read(bytes);
                    idx = 0;
                    for (String product : products) {
                        RadialRecord record = this.recordMap.get(product);
                        if (j == 0) {
                            record.fixedElevation.add(cutConfig.getAngle());
                            record.elevation.add(new ArrayList<>());
                            record.azimuth.add(new ArrayList<>());
                            record.azimuthMinIndex.add(0);
                            record.disResolution.add((float) cutConfig.usBindWidth);
                            record.distance.add(ArrayUtil.arrayRange1(300,
                                    radialN, cutConfig.usBindWidth));
                            record.newScanData();
                        }
                        record.elevation.get(record.elevation.size() - 1).add(cutConfig.getAngle());
                        record.addAzimuth(azimuth);
                        byte[] data = Arrays.copyOfRange(bytes, idx, idx + 2 * radialN);
                        idx += 2 * radialN;
                        Array dataArray = Array.factory(record.getDataType(), new int[]{radialN});
                        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        for (int k = 0; k < radialN; k++) {
                            dataArray.setShort(k, byteBuffer.getShort());
                        }
                        record.addDataArray(dataArray);
                    }
                    azimuth += aDelta;
                }
            }
            is.close();

            this.addAttribute(new Attribute("Country", radarHeader.cCountry));
            this.addAttribute(new Attribute("Province", radarHeader.cProvince));
            this.addAttribute(new Attribute("StationName", radarHeader.cStation));
            this.addAttribute(new Attribute("StationCode", radarHeader.cStationNumber));
            this.addAttribute(new Attribute("StationLongitude", radarHeader.getLongitude()));
            this.addAttribute(new Attribute("StationLatitude", radarHeader.getLatitude()));
            this.addAttribute(new Attribute("AntennaHeight", radarHeader.getHeight()));
            this.addAttribute(new Attribute("featureType", "RADIAL"));
            this.addAttribute(new Attribute("DataType", "Radial"));
            this.addAttribute(new Attribute("RadarDataType", "CC"));

            //Add dimensions and variables
            RadialRecord refRadialRecord = this.recordMap.get("dBZ");
            radialDim = new Dimension();
            radialDim.setName("radial");
            radialDim.setLength(refRadialRecord.getMaxRadials());
            this.addDimension(radialDim);
            scanDim = new Dimension();
            scanDim.setName("scan");
            scanDim.setLength(refRadialRecord.getScanNumber());
            this.addDimension(scanDim);
            gateRDim = new Dimension();
            gateRDim.setName("gateR");
            gateRDim.setLength(refRadialRecord.getGateNumber(0));
            this.addDimension(gateRDim);
            makeRefVariables(refRadialRecord);

            RadialRecord velRadialRecord = this.recordMap.get("V");
            gateVDim = new Dimension();
            gateVDim.setName("gateV");
            gateVDim.setLength(velRadialRecord.getGateNumber(0));
            this.addDimension(gateVDim);
            makeVelVariables(velRadialRecord);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RadarDataType getRadarDataType() {
        return RadarDataType.CC;
    }

    /**
     * Radar header inner class
     */
    static class RadarHeader {
        public static int length = 218;
        public String cFileType;    //16 bytes CINRADC
        public String cCountry;    //30 bytes, country name
        public String cProvince;    //20 bytes, province name
        public String cStation;    //40 bytes, station name
        public String cStationNumber;    //10 bytes, station ID
        public String cRadarType;    //20 bytes, radar type
        public String cLongitude;    //16 bytes, longitude string
        public String cLatitude;    //16 bytes, latitude string
        public int lLongitudeValue;    //longitude
        public int lLatitudeValue;    //latitude
        public int lHeight;    //height
        public short sMaxAngle;
        public short sOptAngle;
        public short ucSYear1;
        public short ucSYear2;
        public short ucSMonth;
        public short ucSDay;
        public short ucSHour;
        public short ucSMinute;
        public short ucSSecond;
        public short ucTimeFrom;
        public short ucEYear1;
        public short ucEYear2;
        public short ucEMonth;
        public short ucEDay;
        public short ucEHour;
        public short ucEMinute;
        public short ucESecond;
        public short ucScanMode;
        public int ulSmilliSecond;
        public short usRHIA;
        public short sRHIL;
        public short sRHIH;
        public int usEchoType;
        public int usProdCode;
        public short ucCalibration;
        public byte[] remain1;    //3 bytes

        /**
         * Constructor
         * @param bytes The byte array
         * @throws IOException
         */
        public RadarHeader(byte[] bytes) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            bytes = new byte[16];
            byteBuffer.get(bytes);
            cFileType = new String(bytes);
            bytes = new byte[30];
            byteBuffer.get(bytes);
            cCountry = new String(bytes, "GB2312");
            bytes = new byte[20];
            byteBuffer.get(bytes);
            cProvince = new String(bytes, "GB2312");
            bytes = new byte[40];
            byteBuffer.get(bytes);
            cStation = new String(bytes, "GB2312");
            bytes = new byte[10];
            byteBuffer.get(bytes);
            cStationNumber = new String(bytes, "GB2312");
            bytes = new byte[20];
            byteBuffer.get(bytes);
            cRadarType = new String(bytes, "GB2312");
            bytes = new byte[16];
            byteBuffer.get(bytes);
            cLongitude = new String(bytes, "GB2312");
            bytes = new byte[16];
            byteBuffer.get(bytes);
            cLatitude = new String(bytes, "GB2312");
            lLongitudeValue = byteBuffer.getInt();
            lLatitudeValue = byteBuffer.getInt();
            lHeight = byteBuffer.getInt();
            sMaxAngle = byteBuffer.getShort();
            sOptAngle = byteBuffer.getShort();
            ucSYear1 = DataType.unsignedByteToShort(byteBuffer.get());
            ucSYear2 = DataType.unsignedByteToShort(byteBuffer.get());
            ucSMonth = DataType.unsignedByteToShort(byteBuffer.get());
            ucSDay = DataType.unsignedByteToShort(byteBuffer.get());
            ucSHour = DataType.unsignedByteToShort(byteBuffer.get());
            ucSMinute = DataType.unsignedByteToShort(byteBuffer.get());
            ucSSecond = DataType.unsignedByteToShort(byteBuffer.get());
            ucTimeFrom = DataType.unsignedByteToShort(byteBuffer.get());
            ucEYear1 = DataType.unsignedByteToShort(byteBuffer.get());
            ucEYear2 = DataType.unsignedByteToShort(byteBuffer.get());
            ucEMonth = DataType.unsignedByteToShort(byteBuffer.get());
            ucEDay = DataType.unsignedByteToShort(byteBuffer.get());
            ucEHour = DataType.unsignedByteToShort(byteBuffer.get());
            ucEMinute = DataType.unsignedByteToShort(byteBuffer.get());
            ucESecond = DataType.unsignedByteToShort(byteBuffer.get());
            ucScanMode = DataType.unsignedByteToShort(byteBuffer.get());
            if (ucScanMode < 100 && ucScanMode != 10) {
                throw new IOException("Error reading CINRAD CC data: Unsupported product: RHI/FFT");
            }

            ulSmilliSecond = byteBuffer.getInt();
            usRHIA = byteBuffer.getShort();
            sRHIL = byteBuffer.getShort();
            sRHIH = byteBuffer.getShort();
            usEchoType = DataType.unsignedShortToInt(byteBuffer.getShort());
            if (usEchoType != 0x408a) // only support vppi at this moment
                throw new IOException("Error reading CINRAD CC data: Unsupported level 2 data");

            usProdCode = DataType.unsignedShortToInt(byteBuffer.getShort());
            if (usProdCode != 0x8003) // only support vppi at this moment
                throw new IOException("Error reading CINRAD CC data: Unsupported product: RHI/FFT");

            ucCalibration = DataType.unsignedByteToShort(byteBuffer.get());
        }

        /**
         * Get longitude
         * @return Longitude
         */
        public float getLongitude() {
            return lLongitudeValue / 3600000.f;
        }

        /**
         * Get latitude
         * @return Latitude
         */
        public float getLatitude() {
            return lLatitudeValue / 3600000.f;
        }

        /**
         * Get height
         * @return Height
         */
        public float getHeight() {
            return lHeight / 1000.f;
        }

        /**
         * Get start time
         * @return Start time
         */
        public LocalDateTime getStartTime() {
            int sYear = ucSYear1 * 100 + ucSYear2;
            return LocalDateTime.of(sYear, ucSMonth, ucSDay, ucSHour, ucSMinute, ucSSecond);
        }

        /**
         * Get end time
         * @return end time
         */
        public LocalDateTime getEndTime() {
            int eYear = ucEYear1 * 100 + ucEYear2;
            return LocalDateTime.of(eYear, ucEMonth, ucEDay, ucEHour, ucEMinute, ucESecond);
        }

        /**
         * Get sweep number
         * @return Sweep number
         */
        public int getSweepNumber() {
            int sweepN = 0;
            if (ucScanMode == 10) {
                sweepN = 1;
            } else if (ucScanMode >= 100) {
                sweepN = ucScanMode - 100;
            }

            return sweepN;
        }
    }

    /**
     * Cut configure inner class
     */
    static class CutConfig {
        public static int length = 22;
        public int usMaxV;
        public int usMaxL;
        public int usBindWidth;
        public int usBinNumber;
        public int usRecordNumber;
        public int usArotate;
        public int usPrf1;
        public int usPrf2;
        public int usSpulseW;
        public short usAngle;
        public short cSweepStatus;
        public short cAmbiguousp;

        /**
         * Constructor
         * @param bytes The byte array
         */
        public CutConfig(byte[] bytes) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            usMaxV = DataType.unsignedShortToInt(byteBuffer.getShort());
            usMaxL = DataType.unsignedShortToInt(byteBuffer.getShort());
            usBindWidth = DataType.unsignedShortToInt(byteBuffer.getShort());
            usBinNumber = DataType.unsignedShortToInt(byteBuffer.getShort());
            usRecordNumber = DataType.unsignedShortToInt(byteBuffer.getShort());
            usArotate = DataType.unsignedShortToInt(byteBuffer.getShort());
            usPrf1 = DataType.unsignedShortToInt(byteBuffer.getShort());
            usPrf2 = DataType.unsignedShortToInt(byteBuffer.getShort());
            usSpulseW = DataType.unsignedShortToInt(byteBuffer.getShort());
            usAngle = byteBuffer.getShort();
            cSweepStatus = DataType.unsignedByteToShort(byteBuffer.get());
            cAmbiguousp = DataType.unsignedByteToShort(byteBuffer.get());
        }

        /**
         * Get angle
         * @return Angle
         */
        public float getAngle() {
            return usAngle / 100.f;
        }
    }

    /**
     * Radar header 2 inner class
     */
    static class RadarHeader2 {
        public static int length = 146;
        public byte[] remain2;    //2 bytes
        public int lAntennaG;
        public int lPower;
        public int lWavelength;

        public int usBeamH;
        public int usBeamL;
        public int usPolarization;
        public int usLogA;
        public int usLineA;
        public int usAGCP;
        public int usFreqMode;
        public int usFreqRepeat;
        public int usPPPPulse;
        public int usFFTPoint;
        public int usProcessType;

        public short ucClutterT;
        public short cSidelobe;
        public short ucVelocityT;
        public short ucFilderP;
        public short ucNoiseT;
        public short ucSQIT;
        public short ucIntensityC;
        public short ucIntensityR;
        public short ucCalNoise;
        public short ucCalPower;
        public short ucCalPulseWidth;
        public short ucCalWorkFreq;
        public short ucCalLog;

        public byte[] remain3;    //92 bytes
        public int liDataOffset;
        public byte[] remain4;     //1 byte

        /**
         * Constructor
         * @param bytes The byte array
         */
        public RadarHeader2(byte[] bytes) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.position(2);
            lAntennaG = byteBuffer.getInt();
            lPower = byteBuffer.getInt();
            lWavelength = byteBuffer.getInt();

            usBeamH = DataType.unsignedShortToInt(byteBuffer.getShort());
            usBeamL = DataType.unsignedShortToInt(byteBuffer.getShort());
            usPolarization = DataType.unsignedShortToInt(byteBuffer.getShort());
            usLogA = DataType.unsignedShortToInt(byteBuffer.getShort());
            usLineA = DataType.unsignedShortToInt(byteBuffer.getShort());
            usAGCP = DataType.unsignedShortToInt(byteBuffer.getShort());
            usFreqMode = DataType.unsignedShortToInt(byteBuffer.getShort());
            usFreqRepeat = DataType.unsignedShortToInt(byteBuffer.getShort());
        }
    }
}
