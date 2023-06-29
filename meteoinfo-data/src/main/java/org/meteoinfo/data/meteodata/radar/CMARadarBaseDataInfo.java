package org.meteoinfo.data.meteodata.radar;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.meteoinfo.common.DataConvert;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.dimarray.DimensionType;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.awt.image.ImagingOpException;
import java.io.*;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CMARadarBaseDataInfo extends DataInfo implements IGridDataInfo {

    private GenericHeader genericHeader;
    private SiteConfig siteConfig;
    private TaskConfig taskConfig;
    private List<CutConfig> cutConfigs;
    private Map<Integer, String> productMap = Stream.of(new Object[][]{{1,"dBT"}, {2,"dBZ"},
            {3,"V"}, {4,"W"}, {5,"SQI"}, {6,"CPA"}, {7,"ZDR"}, {8,"LDR"}, {9,"CC"}, {10,"PhiDP"},
            {11,"KDP"}, {12,"CP"}, {13,"Flag"}, {14,"HCL"}, {15,"CF"}, {16,"SNRH"}, {17,"SNRV"},
            {18,"Flag"}, {19,"Flag"}, {20,"Flag"}, {21,"Flag"}, {22,"Flag"}, {23,"Flag"},
            {24,"Flag"}, {25,"Flag"}, {26,"Flag"}, {27,"Flag"}, {28,"Flag"}, {29,"Flag"},
            {30,"Flag"}, {31,"Flag"}, {32,"Zc"}, {33,"Vc"}, {34,"Wc"}, {35,"ZDRc"}, {0,"Flag"}
        }).collect(Collectors.toMap(data -> (Integer) data[0], data -> (String) data[1]));
    private Map<String, RadialRecord> recordMap = new HashMap<>();

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
     * Get record map
     * @return Record map
     */
    public Map<String, RadialRecord> getRecordMap() {
        return this.recordMap;
    }

    @Override
    public GridArray getGridArray(String varName) {
        return null;
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, String varName, int lonIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, String varName, int levelIdx) {
        return null;
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

    public boolean canOpen(String fileName) {
        try {
            byte[] bytes = new byte[4];
            if (fileName.endsWith("bz2")) {
                BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(new FileInputStream(fileName));
                inputStream.read(bytes);
            } else {
                RandomAccessFile raf = new RandomAccessFile(fileName, "r");
                raf.seek(0);
                raf.read(bytes);
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
                BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(new FileInputStream(fileName));
                readDataInfo(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                RandomAccessFile raf = new RandomAccessFile(fileName, "r");
                readDataInfo(raf);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void readDataInfo(RandomAccessFile raf) {
        try {
            genericHeader = new GenericHeader(raf);
            siteConfig = new SiteConfig(raf);

            //Add global attributes
            this.addAttribute(new Attribute("StationCode", siteConfig.siteCode));
            this.addAttribute(new Attribute("StationName", siteConfig.siteName));
            this.addAttribute(new Attribute("StationLatitude", siteConfig.latitude));
            this.addAttribute(new Attribute("StationLongitude", siteConfig.longitude));
            this.addAttribute(new Attribute("AntennaHeight", siteConfig.antennaHeight));
            this.addAttribute(new Attribute("GroundHeight", siteConfig.groundHeight));

            //Read radial data
            taskConfig = new TaskConfig(raf);
            cutConfigs = new ArrayList<>();
            for (int i = 0; i < taskConfig.cutNumber; i++) {
                cutConfigs.add(new CutConfig(raf));
            }
            List<RadialHeader> radialHeaders = new ArrayList<>();
            while (raf.length() - raf.getFilePointer() > RadialHeader.length) {
                RadialHeader radialHeader = new RadialHeader(raf);
                for (int i = 0; i < radialHeader.momentNumber; i++) {
                    MomentHeader momentHeader = new MomentHeader(raf);
                    String product = this.productMap.get(momentHeader.dataType);
                    RadialRecord record;
                    if (this.recordMap.containsKey(product)) {
                        record = this.recordMap.get(product);
                    } else {
                        record = new RadialRecord(product);
                        record.setBinLength(momentHeader.binLength);
                        record.scale = momentHeader.scale;
                        record.offset = momentHeader.offset;
                        this.recordMap.put(product, record);
                    }
                    if (radialHeader.radialNumber == 1) {
                        record.elevation.add(new ArrayList<>());
                        record.azimuth.add(new ArrayList<>());
                        record.distance.add(ArrayUtil.arrayRange1(0, momentHeader.dataLength / momentHeader.binLength,
                                cutConfigs.get(0).logResolution));
                        record.newScanData();
                    }
                    record.elevation.get(record.elevation.size() - 1).add(radialHeader.elevation);
                    record.azimuth.get(record.azimuth.size() - 1).add(radialHeader.azimuth);
                    byte[] bytes = new byte[momentHeader.dataLength];
                    raf.read(bytes);
                    record.addDataBytes(bytes);
                }
                radialHeaders.add(radialHeader);
            }
            raf.close();

            //Add dimensions and variables
            Dimension xyzDim = new Dimension(DimensionType.OTHER);
            xyzDim.setShortName("xyz");
            xyzDim.setDimValue(Array.factory(DataType.INT, new int[]{3}, new int[]{1,2,3}));
            this.addDimension(xyzDim);
            for (String product : this.recordMap.keySet()) {
                this.recordMap.get(product).makeVariables(this, xyzDim);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void readDataInfo(InputStream raf) {
        try {
            genericHeader = new GenericHeader(raf);
            siteConfig = new SiteConfig(raf);

            //Add global attributes
            this.addAttribute(new Attribute("StationCode", siteConfig.siteCode));
            this.addAttribute(new Attribute("StationName", siteConfig.siteName));
            this.addAttribute(new Attribute("StationLatitude", siteConfig.latitude));
            this.addAttribute(new Attribute("StationLongitude", siteConfig.longitude));
            this.addAttribute(new Attribute("AntennaHeight", siteConfig.antennaHeight));
            this.addAttribute(new Attribute("GroundHeight", siteConfig.groundHeight));
            this.addAttribute(new Attribute("RadarType", siteConfig.getRadarType()));

            //Read radial data
            taskConfig = new TaskConfig(raf);
            cutConfigs = new ArrayList<>();
            for (int i = 0; i < taskConfig.cutNumber; i++) {
                cutConfigs.add(new CutConfig(raf));
            }
            List<RadialHeader> radialHeaders = new ArrayList<>();
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
                        record.scale = momentHeader.scale;;
                        record.offset = momentHeader.offset;
                        this.recordMap.put(product, record);
                    }
                    if (radialHeader.radialNumber == 1) {
                        record.elevation.add(new ArrayList<>());
                        record.azimuth.add(new ArrayList<>());
                        record.distance.add(ArrayUtil.arrayRange1(0, momentHeader.dataLength / momentHeader.binLength,
                                cutConfigs.get(0).logResolution));
                        record.newScanData();
                    }
                    record.elevation.get(record.elevation.size() - 1).add(radialHeader.elevation);
                    record.azimuth.get(record.azimuth.size() - 1).add(radialHeader.azimuth);
                    byte[] bytes = new byte[momentHeader.dataLength];
                    raf.read(bytes);
                    record.addDataBytes(bytes);
                }
                radialHeaders.add(radialHeader);
            }
            raf.close();

            //Add dimensions and variables
            Dimension xyzDim = new Dimension(DimensionType.OTHER);
            xyzDim.setShortName("xyz");
            xyzDim.setDimValue(Array.factory(DataType.INT, new int[]{3}, new int[]{1,2,3}));
            this.addDimension(xyzDim);
            for (String product : this.recordMap.keySet()) {
                this.recordMap.get(product).makeVariables(this, xyzDim);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Array read(String varName) {
        Variable var = this.getVariable(varName);
        int n = var.getDimNumber();
        int[] origin = new int[n];
        int[] size = new int[n];
        int[] stride = new int[n];
        for (int i = 0; i < n; i++) {
            origin[i] = 0;
            size[i] = var.getDimLength(i);
            stride[i] = 1;
        }

        Array r = read(varName, origin, size, stride);

        return r;
    }

    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        try {
            int idx = varName.lastIndexOf("_s");
            int scanIdx = Integer.parseInt(varName.substring(idx + 2)) - 1;
            String product = varName.substring(0, idx);
            boolean isXYZ = product.startsWith("xyz_");
            if (isXYZ) {
                product = product.substring(4);
            }
            RadialRecord record = this.recordMap.get(product);

            Array dataArray;
            if (isXYZ) {
                dataArray = record.getXYZ(scanIdx).section(origin, size, stride).copy();
            } else {
                List<Array> arrays = record.getDataArray(scanIdx);
                Section section = new Section(origin, size, stride);
                dataArray = Array.factory(record.getDataType(), section.getShape());
                Range yRange = section.getRange(0);
                Range xRange = section.getRange(1);
                IndexIterator iter = dataArray.getIndexIterator();
                for (int i = yRange.first(); i <= yRange.last(); i += yRange.stride()) {
                    Array array = arrays.get(i);
                    for (int j = xRange.first(); j <= xRange.last(); j += xRange.stride()) {
                        iter.setObjectNext(array.getObject(j));
                    }
                }

                Variable variable = this.getVariable(varName);
                Attribute aoAttr = variable.findAttribute("add_offset");
                Attribute sfAttr = variable.findAttribute("scale_factor");
                if (aoAttr != null || sfAttr != null) {
                    Number add_offset = 0.f;
                    Number scale_factor = 1.f;
                    if (aoAttr != null) {
                        switch (aoAttr.getDataType()) {
                            case DOUBLE:
                                add_offset = aoAttr.getValues().getDouble(0);
                                break;
                            case FLOAT:
                            case INT:
                                add_offset = aoAttr.getValues().getFloat(0);
                                break;
                        }
                    }
                    if (sfAttr != null) {
                        switch (sfAttr.getDataType()) {
                            case DOUBLE:
                                scale_factor = sfAttr.getValues().getDouble(0);
                                break;
                            case FLOAT:
                            case INT:
                                scale_factor = sfAttr.getValues().getFloat(0);
                                break;
                        }
                    }
                    dataArray = ArrayMath.div(ArrayMath.sub(dataArray, add_offset), scale_factor);
                }
            }

            return dataArray;
        } catch (InvalidRangeException e) {
            return null;
        }
    }

    @Override
    public List<Attribute> getGlobalAttributes() {
        return this.attributes;
    }
}
