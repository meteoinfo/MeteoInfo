package org.meteoinfo.data.meteodata.radar;

import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseRadarDataInfo extends DataInfo {

    protected final Map<Integer, String> productMap = Stream.of(new Object[][]{{1,"dBT"}, {2,"dBZ"},
            {3,"V"}, {4,"W"}, {5,"SQI"}, {6,"CPA"}, {7,"ZDR"}, {8,"LDR"}, {9,"CC"}, {10,"PhiDP"},
            {11,"KDP"}, {12,"CP"}, {13,"Flag"}, {14,"HCL"}, {15,"CF"}, {16,"SNRH"}, {17,"SNRV"},
            {18,"Flag"}, {19,"Flag"}, {20,"Flag"}, {21,"Flag"}, {22,"Flag"}, {23,"Flag"},
            {24,"Flag"}, {25,"Flag"}, {26,"Flag"}, {27,"Flag"}, {28,"Flag"}, {29,"Flag"},
            {30,"Flag"}, {31,"Flag"}, {32,"Zc"}, {33,"Vc"}, {34,"Wc"}, {35,"ZDRc"}, {0,"Flag"}
    }).collect(Collectors.toMap(data -> (Integer) data[0], data -> (String) data[1]));
    protected final Map<String, RadialRecord> recordMap = new HashMap<>();
    protected final List<String> velocityGroup = new ArrayList<>(Arrays.asList("V", "W"));
    protected Dimension radialDim, scanDim, gateRDim, gateVDim;
    protected float antennaHeight = 0;
    protected float beamWidthVert = 1.f;
    protected float logResolution = 1000;
    protected float dopplerResolution = 1000;

    /**
     * Get record map
     * @return Record map
     */
    public Map<String, RadialRecord> getRecordMap() {
        return this.recordMap;
    }

    /**
     * Is a radial record is in velocity group or not
     * @param record The radial record
     * @return Velocity group or not
     */
    public boolean isVelocityGroup(RadialRecord record) {
        return velocityGroup.contains(record.product);
    }

    /**
     * Is a product is in velocity group or not
     * @param product The product name
     * @return Velocity group or not
     */
    public boolean isVelocityGroup(String product) {
        return velocityGroup.contains(product);
    }

    @Override
    public void readDataInfo(String fileName) {
        this.fileName = fileName;
        try {
            InputStream inputStream = RadarDataUtil.getInputStream(fileName);
            readDataInfo(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read data info
     * @param is The InputStream
     */
    abstract void readDataInfo(InputStream is);

    protected void makeRefVariables(RadialRecord refRadialRecord) {
        Dimension[] dimensions = new Dimension[]{scanDim, radialDim, gateRDim};
        for (RadialRecord radialRecord : this.recordMap.values()) {
            if (!radialRecord.isVelocityGroup())
                radialRecord.makeVariable(this, dimensions);
        }

        //coordinate variables
        Variable elevation = new Variable();
        elevation.setName("elevationR");
        elevation.setDataType(DataType.FLOAT);
        elevation.addDimension(scanDim);
        elevation.addDimension(radialDim);
        elevation.addAttribute(new Attribute("units", "degree"));
        elevation.addAttribute(new Attribute("long_name", "elevation angle in degrees"));
        this.addVariable(elevation);

        Variable azimuth = new Variable();
        azimuth.setName("azimuthR");
        azimuth.setDataType(DataType.FLOAT);
        azimuth.addDimension(scanDim);
        azimuth.addDimension(radialDim);
        azimuth.addAttribute(new Attribute("units", "degree"));
        azimuth.addAttribute(new Attribute("long_name", "azimuth angle in degrees"));
        this.addVariable(azimuth);

        Variable distance = new Variable();
        distance.setName("distanceR");
        distance.setDataType(DataType.FLOAT);
        distance.addDimension(gateRDim);
        distance.addAttribute(new Attribute("units", "m"));
        distance.addAttribute(new Attribute("long_name", "radial distance to start of gate"));
        this.addVariable(distance);

        Variable nRadials = new Variable();
        nRadials.setName("numRadialsR");
        nRadials.setDataType(DataType.INT);
        nRadials.addDimension(scanDim);
        nRadials.addAttribute(new Attribute("long_name", "number of valid radials in this scan"));
        this.addVariable(nRadials);

        Variable nGates = new Variable();
        nGates.setName("numGatesR");
        nGates.setDataType(DataType.INT);
        nGates.addDimension(scanDim);
        nGates.addAttribute(new Attribute("long_name", "number of valid gates in this scan"));
        this.addVariable(nGates);

        int nScan = scanDim.getLength();
        int nRadial = radialDim.getLength();
        int nGate = gateRDim.getLength();
        Array elevData = Array.factory(DataType.FLOAT, new int[]{nScan, nRadial});
        Array aziData = Array.factory(DataType.FLOAT, new int[]{nScan, nRadial});
        Array nRData = Array.factory(DataType.INT, new int[]{nScan});
        Array nGData = Array.factory(DataType.INT, new int[]{nScan});
        Index elevIndex = elevData.getIndex();
        Index aziIndex = aziData.getIndex();
        for (int i = 0; i < nScan; i++) {
            List<Float> elevList = refRadialRecord.elevation.get(i);
            List<Float> aziList = refRadialRecord.azimuth.get(i);
            nRData.setInt(i, aziList.size());
            nGData.setInt(i, (int) refRadialRecord.distance.get(i).getSize());
            for (int j = 0; j < nRadial; j++) {
                if (j < elevList.size()) {
                    elevData.setFloat(elevIndex.set(i, j), elevList.get(j));
                    aziData.setFloat(aziIndex.set(i, j), aziList.get(j));
                } else {
                    elevData.setFloat(elevIndex.set(i, j), Float.NaN);
                    aziData.setFloat(aziIndex.set(i, j), Float.NaN);
                }
            }
        }
        Array disData = refRadialRecord.distance.get(0);

        elevation.setCachedData(elevData);
        azimuth.setCachedData(aziData);
        distance.setCachedData(disData);
        nRadials.setCachedData(nRData);
        nGates.setCachedData(nGData);
    }

    protected void makeVelVariables(RadialRecord velRadialRecord) {
        Dimension[] dimensions = new Dimension[]{scanDim, radialDim, gateVDim};
        for (RadialRecord radialRecord : this.recordMap.values()) {
            if (radialRecord.isVelocityGroup())
                radialRecord.makeVariable(this, dimensions);
        }

        //coordinate variables
        Variable elevation = new Variable();
        elevation.setName("elevationV");
        elevation.setDataType(DataType.FLOAT);
        elevation.addDimension(scanDim);
        elevation.addDimension(radialDim);
        elevation.addAttribute(new Attribute("units", "degree"));
        elevation.addAttribute(new Attribute("long_name", "elevation angle in degrees"));
        this.addVariable(elevation);

        Variable azimuth = new Variable();
        azimuth.setName("azimuthV");
        azimuth.setDataType(DataType.FLOAT);
        azimuth.addDimension(scanDim);
        azimuth.addDimension(radialDim);
        azimuth.addAttribute(new Attribute("units", "degree"));
        azimuth.addAttribute(new Attribute("long_name", "azimuth angle in degrees"));
        this.addVariable(azimuth);

        Variable distance = new Variable();
        distance.setName("distanceV");
        distance.setDataType(DataType.FLOAT);
        distance.addDimension(gateVDim);
        distance.addAttribute(new Attribute("units", "m"));
        distance.addAttribute(new Attribute("long_name", "radial distance to start of gate"));
        this.addVariable(distance);

        Variable nRadials = new Variable();
        nRadials.setName("numRadialsR");
        nRadials.setDataType(DataType.INT);
        nRadials.addDimension(scanDim);
        nRadials.addAttribute(new Attribute("long_name", "number of valid radials in this scan"));
        this.addVariable(nRadials);

        Variable nGates = new Variable();
        nGates.setName("numGatesR");
        nGates.setDataType(DataType.INT);
        nGates.addDimension(scanDim);
        nGates.addAttribute(new Attribute("long_name", "number of valid gates in this scan"));
        this.addVariable(nGates);

        int nScan = scanDim.getLength();
        int nRadial = radialDim.getLength();
        int nGate = gateVDim.getLength();
        Array elevData = Array.factory(DataType.FLOAT, new int[]{nScan, nRadial});
        Array aziData = Array.factory(DataType.FLOAT, new int[]{nScan, nRadial});
        Array nRData = Array.factory(DataType.INT, new int[]{nScan});
        Array nGData = Array.factory(DataType.INT, new int[]{nScan});
        Index elevIndex = elevData.getIndex();
        Index aziIndex = aziData.getIndex();
        for (int i = 0; i < nScan; i++) {
            List<Float> elevList = velRadialRecord.elevation.get(i);
            List<Float> aziList = velRadialRecord.azimuth.get(i);
            nRData.setInt(i, aziList.size());
            nGData.setInt(i, (int) velRadialRecord.distance.get(i).getSize());
            for (int j = 0; j < nRadial; j++) {
                if (j < elevList.size()) {
                    elevData.setFloat(elevIndex.set(i, j), elevList.get(j));
                    aziData.setFloat(aziIndex.set(i, j), aziList.get(j));
                } else {
                    elevData.setFloat(elevIndex.set(i, j), Float.NaN);
                    aziData.setFloat(aziIndex.set(i, j), Float.NaN);
                }
            }
        }
        Array disData = velRadialRecord.distance.get(0);

        elevation.setCachedData(elevData);
        azimuth.setCachedData(aziData);
        distance.setCachedData(disData);
        nRadials.setCachedData(nRData);
        nGates.setCachedData(nGData);
    }

    /**
     * Get product names
     * @return product names
     */
    public List<String> getProducts() {
        List<String> products = new ArrayList<>();
        for (String product : this.recordMap.keySet()) {
            products.add(product);
        }

        return products;
    }

    /**
     * Get scan elevations
     * @return Scan elevations
     */
    public List<Float> getElevations(String product) {
        RadialRecord radialRecord = this.recordMap.get(product);

        return radialRecord.fixedElevation;
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
            Variable variable = this.getVariable(varName);
            if (variable.hasCachedData()) {
                return variable.getCachedData().section(origin, size, stride).copy();
            }

            Section section = new Section(origin, size, stride);
            RadialRecord record = this.recordMap.get(varName);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            Range zRange = section.getRange(0);
            Range yRange = section.getRange(1);
            Range xRange = section.getRange(2);
            IndexIterator iter = dataArray.getIndexIterator();
            for (int s = zRange.first(); s <= zRange.last(); s += zRange.stride()) {
                List<Array> arrays = record.getDataArray(s);
                for (int i = yRange.first(); i <= yRange.last(); i += yRange.stride()) {
                    if (i < arrays.size()) {
                        Array array = arrays.get(i);
                        for (int j = xRange.first(); j <= xRange.last(); j += xRange.stride()) {
                            if (j < array.getSize())
                                iter.setFloatNext(array.getFloat(j));
                            else
                                iter.setFloatNext(Float.NaN);
                        }
                    } else {
                        for (int j = xRange.first(); j <= xRange.last(); j += xRange.stride()) {
                            iter.setFloatNext(Float.NaN);
                        }
                    }
                }
            }

            Attribute mvAttr = variable.findAttribute("missing_value");
            if (mvAttr != null) {
                Number missingValue = mvAttr.getNumericValue();
                ArrayMath.missingToNaN(dataArray, missingValue);
            }

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
                dataArray = ArrayMath.add(ArrayMath.mul(dataArray, scale_factor), add_offset);
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
            h = antennaHeight;
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
            h = antennaHeight;
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
            h = antennaHeight;
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
        float halfBeamWidth = beamWidthVert / 2;
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
            h = antennaHeight;
        }

        int nz = (int) za.getSize();
        int[] shape2D = xa.getShape();
        int[] shape3D = new int[]{nz, shape2D[0], shape2D[1]};
        Array data = Array.factory(DataType.FLOAT, shape3D);
        IndexIterator iterData = data.getIndexIterator();
        IndexIterator iterZ = za.getIndexIterator();
        float halfBeamWidth = beamWidthVert / 2;
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
        float halfBeamWidth = this.beamWidthVert / 2;
        float binRes = isVelocityGroup(varName) ? this.dopplerResolution : this.logResolution;
        float height = this.antennaHeight;
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
