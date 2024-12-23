package org.meteoinfo.data.meteodata.radar;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.meteoinfo.common.DataConvert;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PARadarDataInfo extends BaseRadarDataInfo implements IRadarDataInfo {

    private GenericHeader genericHeader;
    private SiteConfig siteConfig;
    private TaskConfig taskConfig;
    private List<BeamConfig> beamConfigs;
    private List<CutConfig> cutConfigs;
    private List<RadialHeader> radialHeaders;

    /**
     * Constructor
     */
    public PARadarDataInfo() {
        this.meteoDataType = MeteoDataType.RADAR;
    }

    /**
     * Get radar data type
     * @return Radar data type
     */
    @Override
    public RadarDataType getRadarDataType() {
        return RadarDataType.PA;
    }

    /**
     * Get generic header
     * @return Generic header
     */
    public GenericHeader getGenericHeader() {
        return this.genericHeader;
    }

    /**
     * Get site config
     * @return Site config
     */
    public SiteConfig getSiteConfig() {
        return this.siteConfig;
    }

    /**
     * Get task config
     * @return Task config
     */
    public TaskConfig getTaskConfig() {
        return this.taskConfig;
    }

    /**
     * Get cut config list
     * @return Cut config list
     */
    public List<CutConfig> getCutConfigs() {
        return this.cutConfigs;
    }

    /**
     * Get radial header list
     * @return Radial header list
     */
    public List<RadialHeader> getRadialHeaders() {
        return this.radialHeaders;
    }

    @Override
    public boolean isValidFile(RandomAccessFile raf) {
        try {
            raf.seek(0);
            byte[] bytes = new byte[4];
            raf.read(bytes);
            int magic = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            if (magic == 1297371986) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Check the data file format
     * @param fileName Data file name
     * @return Boolean
     */
    public static boolean canOpen(String fileName) {
        try {
            byte[] bytes = new byte[4];
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
            int magic = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            if (magic == 1297371986) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    void readDataInfo(InputStream raf) {
        try {
            genericHeader = new GenericHeader(raf);
            siteConfig = new SiteConfig(raf);
            this.antennaHeight = siteConfig.antennaHeight;

            //Add global attributes
            this.addAttribute(new Attribute("StationCode", siteConfig.siteCode));
            this.addAttribute(new Attribute("StationName", siteConfig.siteName));
            this.addAttribute(new Attribute("StationLatitude", siteConfig.latitude));
            this.addAttribute(new Attribute("StationLongitude", siteConfig.longitude));
            this.addAttribute(new Attribute("AntennaHeight", siteConfig.antennaHeight));
            this.addAttribute(new Attribute("GroundHeight", siteConfig.groundHeight));
            this.addAttribute(new Attribute("RadarType", siteConfig.getRadarType()));
            this.addAttribute(new Attribute("featureType", "RADIAL"));
            this.addAttribute(new Attribute("DataType", "Radial"));
            this.addAttribute(new Attribute("RadarDataType", "PA"));

            //Read task configuration
            taskConfig = new TaskConfig(raf);
            this.addAttribute(new Attribute("TaskName", taskConfig.taskName));
            this.addAttribute(new Attribute("TaskDescription", taskConfig.taskDescription));

            //Read scan beam configuration
            beamConfigs = new ArrayList<>();
            BeamConfig beamConfig;
            for (int i = 0; i < taskConfig.beamNumber; i++) {
                beamConfig = new BeamConfig(raf);
                beamConfigs.add(beamConfig);
                if (i == 0) {
                    this.beamWidthVert = beamConfig.txBeamWidthV;
                }
            }

            //Read cut configuration
            cutConfigs = new ArrayList<>();
            CutConfig cutConfig;
            for (int i = 0; i < taskConfig.cutNumber; i++) {
                cutConfig = new CutConfig(raf);
                cutConfigs.add(cutConfig);
                if (i == 0) {
                    this.logResolution = cutConfig.logResolution;
                    this.dopplerResolution = cutConfig.dopplerResolution;
                }
            }

            //Read radial data
            radialHeaders = new ArrayList<>();
            byte[] rhBytes = new byte[RadialHeader.length];
            while (raf.read(rhBytes) != -1) {
                RadialHeader radialHeader = new RadialHeader(rhBytes);
                int scanIdx = radialHeader.elevationNumber - 1;
                for (int i = 0; i < radialHeader.momentNumber; i++) {
                    MomentHeader momentHeader = new MomentHeader(raf);
                    String product = this.productMap.get(momentHeader.dataType);
                    RadialRecord record;
                    if (this.recordMap.containsKey(product)) {
                        record = this.recordMap.get(product);
                    } else {
                        record = new RadialRecord(product);
                        record.setBinLength(momentHeader.binLength);
                        record.scale = 1.f / momentHeader.scale;
                        record.offset = -momentHeader.offset / (float) momentHeader.scale;
                        this.recordMap.put(product, record);
                    }
                    if (radialHeader.radialNumber == 1) {
                        record.fixedElevation.add(cutConfigs.get(scanIdx).elevation);
                        record.elevation.add(new ArrayList<>());
                        record.azimuth.add(new ArrayList<>());
                        record.azimuthMinIndex.add(0);
                        if (isVelocityGroup(record)) {
                            record.disResolution.add(cutConfigs.get(scanIdx).dopplerResolution);
                            record.distance.add(ArrayUtil.arrayRange1(0, momentHeader.dataLength / momentHeader.binLength,
                                    cutConfigs.get(scanIdx).dopplerResolution));
                        } else {
                            record.disResolution.add(cutConfigs.get(scanIdx).logResolution);
                            record.distance.add(ArrayUtil.arrayRange1(0, momentHeader.dataLength / momentHeader.binLength,
                                    cutConfigs.get(scanIdx).logResolution));
                        }
                        record.newScanData();
                    }
                    record.elevation.get(scanIdx).add(radialHeader.elevation);
                    record.addAzimuth(scanIdx, radialHeader.azimuth);
                    byte[] bytes = new byte[momentHeader.dataLength];
                    raf.read(bytes);
                    record.addDataBytes(scanIdx, bytes);
                }
                radialHeaders.add(radialHeader);
            }
            raf.close();

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

    /**
     * Get scan elevations
     * @return Scan elevations
     */
    public List<Float> getElevations() {
        List<Float> elevations = new ArrayList<>();
        for (CutConfig cutConfig : this.cutConfigs) {
            if (!elevations.contains(cutConfig.elevation))
                elevations.add(cutConfig.elevation);
        }

        return elevations;
    }

    @Override
    public List<Attribute> getGlobalAttributes() {
        return this.attributes;
    }

    /**
     * Site configure inner class
     */
    static class SiteConfig {
        public static int length = 128;
        public String siteCode;
        public String siteName;
        public float latitude;
        public float longitude;
        public int antennaHeight;
        public int groundHeight;
        public float frequency;
        public int antennaType;
        public int TRNumber;
        public int RADVersion;
        public short radarType;
        public byte[] reserved;

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
            antennaType = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            raf.read(bytes);
            TRNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            raf.read(bytes);
            RADVersion = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            bytes = new byte[2];
            raf.read(bytes);
            radarType = DataConvert.bytes2Short(bytes, ByteOrder.LITTLE_ENDIAN);
            reserved = new byte[54];
            raf.read(reserved);
        }

        /**
         * Get radar type string
         * @return Radar type string
         */
        public String getRadarType() {
            switch (radarType) {
                case 7:
                    return "SPAR";
                case 8:
                    return "SPARD";
                case 43:
                    return "CPAR";
                case 44:
                    return "CPARD";
                case 69:
                    return "XPAR";
                case 70:
                    return "XPARD";
                default:
                    return "UNDEFINE";
            }
        }
    }

    /**
     * Task configure inner class
     */
    static class TaskConfig {
        public static int length = 256;
        public String taskName;
        public String taskDescription;
        public int polarizationType;
        public int scanType;
        public int beamNumber;
        public int cutNumber;
        public int rayOrder;
        public LocalDateTime scanStartTime;
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
            taskDescription = new String(bytes, "GB2312").trim();
            bytes = new byte[4];
            raf.read(bytes);
            polarizationType = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            raf.read(bytes);
            scanType = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            raf.read(bytes);
            beamNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            raf.read(bytes);
            cutNumber = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            raf.read(bytes);
            rayOrder = DataConvert.bytes2Int(bytes, ByteOrder.LITTLE_ENDIAN);
            bytes = new byte[8];
            raf.read(bytes);
            long seconds = DataConvert.bytes2Long(bytes, ByteOrder.LITTLE_ENDIAN);
            LocalDateTime dt = LocalDateTime.of(1970, 1, 1, 0, 0);
            scanStartTime = dt.plusSeconds(seconds);
            reserves = new byte[68];
            raf.read(reserves);
        }
    }

    /**
     * Scan beam configure inner class
     */
    static class BeamConfig {
        public static int length = 640;
        public int beamIndex;
        public int beamType;
        public int subPulseNumber;
        public float txBeamDirection;
        public float txBeamWidthH;
        public float txBeamWidthV;
        public float txBeamGain;
        public byte[] reserves1;    //100 bytes

        public int subPulseStrategy;
        public int subPulseModulation;
        public float subPulseFrequency;
        public float subPulseBandWidth;
        public int subPulseWidth;
        public byte[] reserves2;    //492 bytes

        /**
         * Constructor
         * @param inputStream The input steam
         */
        public BeamConfig(InputStream inputStream) throws IOException {
            byte[] inBytes = new byte[BeamConfig.length];
            inputStream.read(inBytes);
            ByteBuffer byteBuffer = ByteBuffer.wrap(inBytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            beamIndex = byteBuffer.getInt();
            beamType = byteBuffer.getInt();
            subPulseNumber = byteBuffer.getInt();
            txBeamDirection = byteBuffer.getFloat();
            txBeamWidthH = byteBuffer.getFloat();
            txBeamWidthV = byteBuffer.getFloat();
            txBeamGain = byteBuffer.getFloat();
            byteBuffer.position(byteBuffer.position() + 100);

            subPulseStrategy = byteBuffer.getInt();
            subPulseModulation = byteBuffer.getInt();
            subPulseFrequency = byteBuffer.getFloat();
            subPulseBandWidth = byteBuffer.getFloat();
            subPulseWidth = byteBuffer.getInt();
        }
    }

    /**
     * Cut configuration inner class
     */
    static class CutConfig {
        public static int length = 256;
        public short cutIndex;
        public short txBeamIndex;
        public float elevation;
        public float txBeamGain;
        public float rxBeamWidthH;
        public float rxBeamWidthV;
        public float rxBeamGain;
        public int processMode;    // 1-PPP 2-FFT
        public int waveForm;
        public float N1_PRF_1;
        public float N1_PRF_2;
        public float N2_PRF_1;
        public float N2_PRF_2;
        public int unfoldMode;
        public float azimuth;
        public float startAngle;
        public float endAngle;
        public float angleResolution;
        public float scanSpeed;
        public float logResolution;
        public float dopplerResolution;
        public int maximumRange;
        public int maximumRange2;
        public int startRange;
        public int sample_1;
        public int sample_2;
        public int phaseMode;
        public float atmosphericLoss;
        public float nyquistSpeed;
        public long momentsMask;
        public long momentsSizeMask;
        public int miscFilterMask;
        public float SQIThreshold;
        public float SIGThreshold;
        public float CSRThreshold;
        public float LOGThreshold;
        public float CPAThreshold;
        public float PMIThreshold;
        public float DPLOGThreshold;
        public byte[] thresholdsReserved;    // 4 bytes
        public int dBTMask;
        public int dBZMask;
        public int velocity;
        public int spectrumWidthMask;
        public int ZDRMask;
        public byte[] maskReserved;    // 12 bytes
        public int scanSync;
        public int direction;
        public short groundClutterClassifierType;
        public short groundClutterFilterType;
        public short groundClutterFilterNotchWidth;
        public short groundClutterFilterWindow;
        public byte[] reserved;    // 44 bytes

        /**
         * Constructor
         * @param inputStream The input stream
         */
        public CutConfig(InputStream inputStream) throws IOException {
            byte[] bytes = new byte[CutConfig.length];
            inputStream.read(bytes);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            cutIndex = byteBuffer.getShort();
            txBeamIndex = byteBuffer.getShort();
            elevation = byteBuffer.getFloat();
            txBeamGain = byteBuffer.getFloat();
            rxBeamWidthH = byteBuffer.getFloat();
            rxBeamWidthV = byteBuffer.getFloat();
            rxBeamGain = byteBuffer.getFloat();
            processMode = byteBuffer.getInt();
            waveForm = byteBuffer.getInt();
            N1_PRF_1 = byteBuffer.getFloat();
            N1_PRF_2 = byteBuffer.getFloat();
            N2_PRF_1 = byteBuffer.getFloat();
            N2_PRF_2 = byteBuffer.getFloat();
            unfoldMode = byteBuffer.getInt();
            azimuth = byteBuffer.getFloat();
            startAngle = byteBuffer.getFloat();
            endAngle = byteBuffer.getFloat();
            angleResolution = byteBuffer.getFloat();
            scanSpeed = byteBuffer.getFloat();
            logResolution = byteBuffer.getFloat();
            dopplerResolution = byteBuffer.getFloat();
            maximumRange = byteBuffer.getInt();
            maximumRange2 = byteBuffer.getInt();
            startRange = byteBuffer.getInt();
            sample_1 = byteBuffer.getInt();
            sample_2 = byteBuffer.getInt();
            phaseMode = byteBuffer.getInt();
            atmosphericLoss = byteBuffer.getFloat();
            nyquistSpeed = byteBuffer.getFloat();
            momentsMask = byteBuffer.getLong();
            momentsSizeMask = byteBuffer.getLong();
            miscFilterMask = byteBuffer.getInt();
            SQIThreshold = byteBuffer.getFloat();
            SIGThreshold = byteBuffer.getFloat();
            CSRThreshold = byteBuffer.getFloat();
            LOGThreshold = byteBuffer.getFloat();
            CPAThreshold = byteBuffer.getFloat();
            PMIThreshold = byteBuffer.getFloat();
            DPLOGThreshold = byteBuffer.getFloat();
            byteBuffer.getInt();

            dBTMask = byteBuffer.getInt();
            dBZMask = byteBuffer.getInt();
            velocity = byteBuffer.getInt();
            spectrumWidthMask = byteBuffer.getInt();
            ZDRMask = byteBuffer.getInt();
            byteBuffer.position(byteBuffer.position() + 12);

            scanSync = byteBuffer.getInt();
            direction = byteBuffer.getInt();
            groundClutterClassifierType = byteBuffer.getShort();
            groundClutterFilterType = byteBuffer.getShort();
            groundClutterFilterNotchWidth = byteBuffer.getShort();
            groundClutterFilterWindow = byteBuffer.getShort();
        }
    }

    /**
     * Radial header inner class
     */
    static class RadialHeader {
        public static int length = 128;
        public int radialState;
        public int spotBlank;
        public int sequenceNumber;
        public int radialNumber;
        public int elevationNumber;
        public float azimuth;
        public float elevation;
        public long seconds;
        public int microSeconds;
        public int lengthOfData;
        public int momentNumber;
        public short scanBeamIndex;
        public short horizontalEstimatedNoise;
        public short verticalEstimatedNoise;
        public int PRFFLAG;
        public byte[] reserved04;    //70 bytes

        /**
         * Constructor
         * @param inputStream The input stream
         */
        public RadialHeader(byte[] bytes) {
            ByteBuffer bf = ByteBuffer.wrap(bytes);
            bf.order(ByteOrder.LITTLE_ENDIAN);
            radialState = bf.getInt();
            spotBlank = bf.getInt();
            sequenceNumber = bf.getInt();
            radialNumber = bf.getInt();
            elevationNumber = bf.getInt();
            azimuth = bf.getFloat();
            elevation = bf.getFloat();
            seconds = bf.getLong();
            microSeconds = bf.getInt();
            lengthOfData = bf.getInt();
            momentNumber = bf.getInt();
            scanBeamIndex = bf.getShort();
            horizontalEstimatedNoise = bf.getShort();
            verticalEstimatedNoise = bf.getShort();
            PRFFLAG = bf.getInt();
        }
    }
}
