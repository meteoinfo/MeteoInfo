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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SCRadarDataInfo extends BaseRadarDataInfo implements IRadarDataInfo {

    @Override
    public boolean isValidFile(java.io.RandomAccessFile raf) {
        return false;
    }

    void setScaleOffset(RadialRecord record, float maxV) {
        switch (record.product) {
            case "dBZ":
            case "dBT":
                record.scale = 0.5f;
                record.offset = -32.f;
                break;
            case "V":
                record.scale = maxV / 127.5f;
                record.offset = -128 * maxV / 127.5f;
                break;
            case "W":
                record.scale = maxV / 256.f;
                record.offset = 0;
                break;
        }
    }

    @Override
    void readDataInfo(InputStream is) {
        try {
            byte[] bytes = new byte[SCRadarDataInfo.RadarHeader.length];
            is.read(bytes);
            RadarHeader radarHeader = new RadarHeader(bytes);
            List<String> products = new ArrayList<>(Arrays.asList("dBZ", "V", "dBT", "W"));
            float maxV = radarHeader.layerParams.get(0).getMaxV();
            for (String product : products) {
                RadialRecord record = new RadialRecord(product);
                record.setBinLength(1);
                setScaleOffset(record, maxV);
                this.recordMap.put(product, record);
            }
            int gateNum = 998;
            byte[] rhBytes = new byte[RadialHeader.length];
            for (int iSweep = 0; iSweep < radarHeader.nSweeps; iSweep++) {
                LayerParam layerParam = radarHeader.layerParams.get(iSweep);
                maxV = layerParam.maxV / 100.f;
                for (int iRadial = 0; iRadial < layerParam.recordNumber; iRadial++) {
                    is.read(rhBytes);
                    RadialHeader radialHeader = new RadialHeader(rhBytes);
                    bytes = new byte[gateNum * 4];
                    is.read(bytes);
                    int i = 0;
                    for (String product : products) {
                        RadialRecord record = this.recordMap.get(product);
                        if (iRadial == 0) {
                            record.fixedElevation.add(layerParam.getSweepAngle());
                            record.elevation.add(new ArrayList<>());
                            record.azimuth.add(new ArrayList<>());
                            record.azimuthMinIndex.add(0);
                            record.disResolution.add(layerParam.binWidth / 10.f);
                            record.distance.add(ArrayUtil.arrayRange1(0,
                                    gateNum, layerParam.binWidth / 10));
                            record.newScanData();
                        }
                        record.elevation.get(record.elevation.size() - 1).add(radialHeader.getElevation());
                        record.addAzimuth(radialHeader.getAzimuth());
                        byte[] data = new byte[gateNum];
                        for (int j = 0; j < 998; j++) {
                            data[j] = bytes[4 * j + i];
                        }
                        record.addDataBytes(data);
                        i += 1;
                    }
                }
            }

            is.close();

            this.addAttribute(new Attribute("Country", radarHeader.country));
            this.addAttribute(new Attribute("Province", radarHeader.province));
            this.addAttribute(new Attribute("StationName", radarHeader.station));
            this.addAttribute(new Attribute("StationLongitude", radarHeader.getLongitude()));
            this.addAttribute(new Attribute("StationLatitude", radarHeader.getLatitude()));
            this.addAttribute(new Attribute("AntennaHeight", radarHeader.getHeight()));
            this.addAttribute(new Attribute("featureType", "RADIAL"));
            this.addAttribute(new Attribute("DataType", "Radial"));
            this.addAttribute(new Attribute("RadarDataType", "SC"));

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RadarDataType getRadarDataType() {
        return RadarDataType.SC;
    }

    static class LayerParam {
        public byte ambiguousP;
        public short aRotate;
        public short prf1;
        public short prf2;
        public short sPulseW;
        public short maxV;
        public short maxL;
        public short binWidth;
        public short binNumber;
        public short recordNumber;
        public short swAngles;

        /**
         * Constructor
         * @param byteBuffer Byte buffer
         */
        public LayerParam(ByteBuffer byteBuffer) {
            ambiguousP = byteBuffer.get();
            aRotate = byteBuffer.getShort();
            prf1 = byteBuffer.getShort();
            prf2 = byteBuffer.getShort();
            sPulseW = byteBuffer.getShort();
            maxV = byteBuffer.getShort();
            maxL = byteBuffer.getShort();
            binWidth = byteBuffer.getShort();
            binNumber = byteBuffer.getShort();
            recordNumber = byteBuffer.getShort();
            swAngles = byteBuffer.getShort();
        }

        /**
         * Get max value
         * @return Max value
         */
        public float getMaxV() {
            return this.maxV / 100.f;
        }

        /**
         * Get bin width
         * @return Bin width
         */
        public int getBinWidth() {
            return (int) binWidth / 10;
        }

        /**
         * Get sweep angle
         * @return Sweep angle
         */
        public float getSweepAngle() {
            return swAngles / 100.f;
        }
    }

    static class RadarHeader {
        public static int length = 1024;
        public String country;
        public String province;
        public String station;
        public String stationID;
        public String radarType;
        public String longitudeStr;
        public String latitudeStr;
        public int longitude;
        public int latitude;
        public int height;
        public short maxAngle;
        public short opAngle;
        public short mangFreq;

        public int antennaG;
        public short beamH;
        public short beamL;
        public byte polarizations;
        public byte sideLobe;
        public int power;
        public int waveLength;
        public short logA;
        public short lineA;
        public short AGCP;
        public byte clutterT;
        public byte velocityP;
        public byte filderP;
        public byte noiseT;
        public byte SQIT;
        public byte intensityC;
        public byte intensityR;

        public byte sType;
        public short sYear;
        public byte sMonth;
        public byte sDay;
        public byte sHour;
        public byte sMinute;
        public byte sSecond;
        public byte timeP;
        public int sMillisecond;
        public byte calibration;
        public byte intensityI;
        public byte velocityP1;

        public List<LayerParam> layerParams;

        public short RHIA;
        public short RHIL;
        public short RHIH;
        public short eYear;
        public byte eMonth;
        public byte eDay;
        public byte eHour;
        public byte eMinute;
        public byte eSecond;
        public byte eTenth;

        public int nSweeps;

        /**
         * Constructor
         * @param inBytes Input bytes
         */
        public RadarHeader(byte[] inBytes) throws UnsupportedEncodingException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(inBytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byte[] bytes = new byte[30];
            byteBuffer.get(bytes);
            String charsetName = "GB2312";

            //Site information
            country = new String(bytes, charsetName);
            bytes = new byte[20];
            byteBuffer.get(bytes);
            province = new String(bytes, charsetName);
            bytes = new byte[40];
            byteBuffer.get(bytes);
            station = new String(bytes, charsetName);
            bytes = new byte[10];
            byteBuffer.get(bytes);
            stationID = new String(bytes, charsetName);
            bytes = new byte[20];
            byteBuffer.get(bytes);
            radarType = new String(bytes, charsetName);
            bytes = new byte[16];
            byteBuffer.get(bytes);
            longitudeStr = new String(bytes, charsetName);
            bytes = new byte[16];
            byteBuffer.get(bytes);
            latitudeStr = new String(bytes, charsetName);
            longitude = byteBuffer.getInt();
            latitude = byteBuffer.getInt();
            height = byteBuffer.getInt();
            maxAngle = byteBuffer.getShort();
            opAngle = byteBuffer.getShort();
            mangFreq = byteBuffer.getShort();

            //Radar performance parameters
            antennaG = byteBuffer.getInt();
            beamH = byteBuffer.getShort();
            beamL = byteBuffer.getShort();
            polarizations = byteBuffer.get();
            sideLobe = byteBuffer.get();
            power = byteBuffer.getInt();
            waveLength = byteBuffer.getInt();
            logA = byteBuffer.getShort();
            lineA = byteBuffer.getShort();
            AGCP = byteBuffer.getShort();
            clutterT = byteBuffer.get();
            velocityP = byteBuffer.get();
            filderP = byteBuffer.get();
            noiseT = byteBuffer.get();
            SQIT = byteBuffer.get();
            intensityC = byteBuffer.get();
            intensityR = byteBuffer.get();

            //Radar observation parameters 1
            sType = byteBuffer.get();
            nSweeps = sType - 100;
            sYear = byteBuffer.getShort();
            sMonth = byteBuffer.get();
            sDay = byteBuffer.get();
            sHour = byteBuffer.get();
            sMinute = byteBuffer.get();
            sSecond = byteBuffer.get();
            timeP = byteBuffer.get();
            sMillisecond = byteBuffer.getInt();
            calibration = byteBuffer.get();
            intensityI = byteBuffer.get();
            velocityP1 = byteBuffer.get();

            //Layer parameters
            this.layerParams = new ArrayList<>();
            for (int i = 0; i < nSweeps; i++) {
                layerParams.add(new LayerParam(byteBuffer));
            }

            //Radar observation parameters 2
            RHIA = byteBuffer.getShort();
            RHIL = byteBuffer.getShort();
            RHIH = byteBuffer.getShort();
            eYear = byteBuffer.getShort();
            eMonth = byteBuffer.get();
            eDay = byteBuffer.get();
            eHour = byteBuffer.get();
            eMinute = byteBuffer.get();
            eSecond = byteBuffer.get();
            eTenth = byteBuffer.get();
        }

        /**
         * Get longitude
         * @return Longitude
         */
        public float getLongitude() {
            return this.longitude / 100.f;
        }

        /**
         * Get latitude
         * @return Latitude
         */
        public float getLatitude() {
            return this.latitude / 100.f;
        }

        /**
         * Get height
         * @return Height
         */
        public float getHeight() {
            return this.height / 1000.f;
        }

    }

    static class RadialHeader {
        public static int length = 8;
        public short startAzimuth;
        public short startElevation;
        public short endAzimuth;
        public short endElevation;

        /**
         * Constructor
         * @param is InputStream
         */
        public RadialHeader(byte[] inBytes) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(inBytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            startAzimuth = byteBuffer.getShort();
            startElevation = byteBuffer.getShort();
            endAzimuth = byteBuffer.getShort();
            endElevation = byteBuffer.getShort();
        }

        /**
         * Get azimuth
         * @return Azimuth
         */
        public float getAzimuth() {
            return startAzimuth * 360.f / 65536;
        }

        /**
         * Get elevation
         * @return Elevation
         */
        public float getElevation() {
            return startElevation * 120.f / 65536;
        }
    }
}
