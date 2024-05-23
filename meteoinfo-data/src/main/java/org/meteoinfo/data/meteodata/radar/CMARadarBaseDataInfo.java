package org.meteoinfo.data.meteodata.radar;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.meteoinfo.common.DataConvert;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.dimarray.DimensionType;
import org.meteoinfo.data.meteodata.*;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.awt.image.ImagingOpException;
import java.io.*;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CMARadarBaseDataInfo extends BaseRadarDataInfo implements IRadarDataInfo {

    private GenericHeader genericHeader;
    private SiteConfig siteConfig;
    private TaskConfig taskConfig;
    private List<CutConfig> cutConfigs;
    private List<RadialHeader> radialHeaders;

    /**
     * Constructor
     */
    public CMARadarBaseDataInfo() {
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
    public void readDataInfo(String fileName) {
        this.fileName = fileName;
        if (fileName.endsWith(".bz2")) {
            try {
                BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(Files.newInputStream(Paths.get(fileName)));
                readDataInfo(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                //RandomAccessFile raf = new RandomAccessFile(fileName, "r");
                //readDataInfo(raf);
                BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(Paths.get(fileName)));
                readDataInfo(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

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
                            record.disResolution.add(cutConfigs.get(radialHeader.elevationNumber - 1).dopplerResolution);
                            record.distance.add(ArrayUtil.arrayRange1(0, momentHeader.dataLength / momentHeader.binLength,
                                    cutConfigs.get(radialHeader.elevationNumber - 1).dopplerResolution));
                        } else {
                            record.disResolution.add(cutConfigs.get(radialHeader.elevationNumber - 1).logResolution);
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

    @Override
    public List<Attribute> getGlobalAttributes() {
        return this.attributes;
    }

    /**
     * Read grid ppi data
     * @param varName Variable name
     * @param scanIdx Scan index
     * @param xa X coordinates array
     * @param ya Y coordinates array
     * @param h Radar height
     * @return Grid ppi data
     */
    public Array readGridData(String varName, int scanIdx, Array xa, Array ya, Float h) {
        RadialRecord record = this.recordMap.get(varName);
        if (h == null) {
            h = (float) siteConfig.antennaHeight;
        }
        Array[] rr = Transform.cartesianToAntennaElevation(xa, ya, record.fixedElevation.get(scanIdx), h);
        Array azimuth = rr[0];
        Array ranges = rr[1];
        Array data = Array.factory(DataType.FLOAT, xa.getShape());
        IndexIterator iterA = azimuth.getIndexIterator();
        IndexIterator iterR = ranges.getIndexIterator();
        IndexIterator iterData = data.getIndexIterator();
        float v;
        while (iterData.hasNext()) {
            v = record.interpolateValue(scanIdx, iterA.getFloatNext(), iterR.getFloatNext());
            iterData.setFloatNext(v);
        }

        return data;
    }

    /**
     * Read CR data
     * @param varName Variable name
     * @param xa X coordinates array - 2D
     * @param ya Y coordinates array - 2D
     * @param h Radar height
     * @return CR data
     */
    public Array getCRData(String varName, Array xa, Array ya, Float h) {
        RadialRecord record = this.recordMap.get(varName);
        int nScan = record.getScanNumber();
        if (h == null) {
            h = (float) siteConfig.antennaHeight;
        }

        int[] shape = xa.getShape();
        int ny = shape[0];
        int nx = shape[1];
        Array data = Array.factory(DataType.FLOAT, shape);
        Index2D index2D = (Index2D) data.getIndex();
        float v;
        for (int s = 0; s < nScan; s++) {
            Array[] rr = Transform.cartesianToAntennaElevation(xa, ya, record.fixedElevation.get(s), h);
            Array azimuth = rr[0];
            Array ranges = rr[1];
            IndexIterator iterA = azimuth.getIndexIterator();
            IndexIterator iterR = ranges.getIndexIterator();
            if (s == 0) {
                for (int i = 0; i < ny; i++) {
                    for (int j = 0; j < nx; j++) {
                        v = record.interpolateValue(s, iterA.getFloatNext(), iterR.getFloatNext());
                        data.setFloat(index2D.set(i, j), v);
                    }
                }
            } else {
                float v1;
                for (int i = 0; i < ny; i++) {
                    for (int j = 0; j < nx; j++) {
                        v = record.interpolateValue(s, iterA.getFloatNext(), iterR.getFloatNext());
                        index2D.set(i, j);
                        v1 = data.getFloat(index2D);
                        if (Float.isNaN(v1) || (v > v1))
                            data.setFloat(index2D, v);
                    }
                }
            }
        }

        return data;
    }

    /**
     * Read CAPPI data
     * @param varName Variable name
     * @param xa X coordinates array
     * @param ya Y coordinates array
     * @param z Z coordinates value
     * @param h Radar height
     * @return Grid ppi data
     */
    public Array getCAPPIData(String varName, Array xa, Array ya, float z, Float h) {
        RadialRecord record = this.recordMap.get(varName);
        if (h == null) {
            h = (float) siteConfig.antennaHeight;
        }
        Array[] rr = Transform.cartesianToAntenna(xa, ya, z, h);
        Array azimuth = rr[0];
        Array ranges = rr[1];
        Array elevation = rr[2];
        Array data = Array.factory(DataType.FLOAT, xa.getShape());
        IndexIterator iterA = azimuth.getIndexIterator();
        IndexIterator iterR = ranges.getIndexIterator();
        IndexIterator iterE = elevation.getIndexIterator();
        IndexIterator iterData = data.getIndexIterator();
        float v;
        float halfBeamWidth = this.siteConfig.beamWidthVert / 2;
        while (iterData.hasNext()) {
            v = record.interpolateValue(iterE.getFloatNext(), iterA.getFloatNext(),
                    iterR.getFloatNext(), halfBeamWidth);
            iterData.setFloatNext(v);
        }

        return data;
    }

    /**
     * Read grid 3d data
     * @param varName Variable name
     * @param xa X coordinates array
     * @param ya Y coordinates array
     * @param z Z coordinates array
     * @param h Radar height
     * @return Grid ppi data
     */
    public Array getGrid3DData(String varName, Array xa, Array ya, Array za, Float h) {
        RadialRecord record = this.recordMap.get(varName);
        if (h == null) {
            h = (float) siteConfig.antennaHeight;
        }

        int nz = (int) za.getSize();
        int[] shape2D = xa.getShape();
        int[] shape3D = new int[]{nz, shape2D[0], shape2D[1]};
        Array data = Array.factory(DataType.FLOAT, shape3D);
        IndexIterator iterData = data.getIndexIterator();
        IndexIterator iterZ = za.getIndexIterator();
        float halfBeamWidth = this.siteConfig.beamWidthVert / 2;
        while(iterZ.hasNext()) {
            float z = iterZ.getFloatNext();
            Array[] rr = Transform.cartesianToAntenna(xa, ya, z, h);
            Array azimuth = rr[0];
            Array ranges = rr[1];
            Array elevation = rr[2];
            IndexIterator iterA = azimuth.getIndexIterator();
            IndexIterator iterR = ranges.getIndexIterator();
            IndexIterator iterE = elevation.getIndexIterator();
            float v;
            while (iterA.hasNext()) {
                v = record.interpolateValue(iterE.getFloatNext(), iterA.getFloatNext(),
                        iterR.getFloatNext(), halfBeamWidth);
                iterData.setFloatNext(v);
            }
        }

        return data;
    }

    /**
     * Get VCS data
     * @param varName Variable name
     * @param startX Start x, km
     * @param startY Start y, km
     * @param endX End x, km
     * @param endY End y, km
     * @return VCS data
     */
    public Array[] getVCSData(String varName, float startX, float startY, float endX, float endY) {
        RadialRecord record = this.recordMap.get(varName);
        int nScan = record.getScanNumber();
        float halfBeamWidth = this.siteConfig.beamWidthVert / 2;
        float binRes = this.cutConfigs.get(0).logResolution;
        float height = this.siteConfig.antennaHeight;
        float startEndDistance = (float) Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        int nPoints = (int) (startEndDistance * 1000 / binRes + 1);
        Array xa = ArrayUtil.lineSpace(startX, endX, nPoints);
        Array ya = ArrayUtil.lineSpace(startY, endY, nPoints);
        Array aa = Transform.xyToAzimuth(xa, ya);
        int[] shape = new int[]{nScan, 2, nPoints};
        Array data = Array.factory(DataType.FLOAT, shape);
        Array meshXY = Array.factory(DataType.FLOAT, shape);
        Array meshZ = Array.factory(DataType.FLOAT, shape);
        Index dataIndex = data.getIndex();
        Index meshXYIndex = meshXY.getIndex();
        Index meshZIndex = meshZ.getIndex();
        float x, y, z1, z2, dis, azi, v, ele;
        for (int i = 0; i < nScan; i++) {
            ele = record.fixedElevation.get(i);
            for (int j = 0; j < nPoints; j++) {
                x = xa.getFloat(j);
                y = ya.getFloat(j);
                dis = (float) Math.sqrt(x * x + y * y);
                azi = aa.getFloat(j);
                v = record.getValue(i, azi, dis * 1000);
                z1 = Transform.toCartesianZ(dis * 1000, (float) Math.toRadians(ele -
                        halfBeamWidth), height) / 1000.f;
                z2 = Transform.toCartesianZ(dis * 1000, (float) Math.toRadians(ele +
                        halfBeamWidth), height) / 1000.f;
                for (int k = 0; k < 2; k++) {
                    data.setFloat(dataIndex.set(i, k, j), v);
                    meshXY.setFloat(meshXYIndex.set(i, k, j), dis);
                }
                meshZ.setFloat(meshZIndex.set(i, 0, j), z1);
                meshZ.setFloat(meshZIndex.set(i, 1, j), z2);
            }
        }

        return new Array[]{data, meshXY, meshZ};
    }
}
