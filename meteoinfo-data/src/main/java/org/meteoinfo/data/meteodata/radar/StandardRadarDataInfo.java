package org.meteoinfo.data.meteodata.radar;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.meteoinfo.common.DataConvert;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.meteodata.*;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class StandardRadarDataInfo extends BaseRadarDataInfo implements IRadarDataInfo {

    private GenericHeader genericHeader;
    private SiteConfig siteConfig;
    private TaskConfig taskConfig;
    private List<CutConfig> cutConfigs;
    private List<RadialHeader> radialHeaders;

    /**
     * Constructor
     */
    public StandardRadarDataInfo() {
        this.meteoDataType = MeteoDataType.RADAR;
    }

    /**
     * Get radar data type
     * @return Radar data type
     */
    @Override
    public RadarDataType getRadarDataType() {
        return RadarDataType.STANDARD;
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
            this.beamWidthVert = siteConfig.beamWidthVert;

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
            this.addAttribute(new Attribute("RadarDataType", "CMA Standard"));

            //Read task configuration
            taskConfig = new TaskConfig(raf);
            this.addAttribute(new Attribute("TaskName", taskConfig.taskName));
            this.addAttribute(new Attribute("TaskDescription", taskConfig.taskDescription));

            //Read radial data
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
            radialHeaders = new ArrayList<>();
            byte[] rhBytes = new byte[RadialHeader.length];
            while (raf.read(rhBytes) != -1) {
                RadialHeader radialHeader = new RadialHeader(rhBytes);
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
                        record.fixedElevation.add(cutConfigs.get(radialHeader.elevationNumber - 1).elevation);
                        record.elevation.add(new ArrayList<>());
                        record.azimuth.add(new ArrayList<>());
                        record.azimuthMinIndex.add(0);
                        if (isVelocityGroup(record)) {
                            record.disResolution.add((float) cutConfigs.get(radialHeader.elevationNumber - 1).dopplerResolution);
                            record.distance.add(ArrayUtil.arrayRange1(0, momentHeader.dataLength / momentHeader.binLength,
                                    cutConfigs.get(radialHeader.elevationNumber - 1).dopplerResolution));
                        } else {
                            record.disResolution.add((float) cutConfigs.get(radialHeader.elevationNumber - 1).logResolution);
                            record.distance.add(ArrayUtil.arrayRange1(0, momentHeader.dataLength / momentHeader.binLength,
                                    cutConfigs.get(radialHeader.elevationNumber - 1).logResolution));
                        }
                        record.newScanData();
                    }
                    record.elevation.get(record.elevation.size() - 1).add(radialHeader.elevation);
                    record.addAzimuth(radialHeader.azimuth);
                    byte[] bytes = new byte[momentHeader.dataLength];
                    raf.read(bytes);
                    record.addDataBytes(bytes);
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

            /*Dimension xyzDim = new Dimension(DimensionType.OTHER);
            xyzDim.setShortName("xyz");
            xyzDim.setDimValue(Array.factory(DataType.INT, new int[]{3}, new int[]{1,2,3}));
            this.addDimension(xyzDim);
            for (String product : this.recordMap.keySet()) {
                this.recordMap.get(product).makeVariables(this, xyzDim);
            }*/
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

}
