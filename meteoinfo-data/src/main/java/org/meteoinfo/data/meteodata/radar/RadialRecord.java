package org.meteoinfo.data.meteodata.radar;

import org.meteoinfo.common.DataConvert;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.dimarray.DimensionType;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class RadialRecord {
    public String product;
    private int binLength;
    private DataType dataType;
    public int scale;
    public int offset;
    public List<Float> fixedElevation = new ArrayList<>();
    public List<List<Float>> elevation = new ArrayList<>();
    public List<List<Float>> azimuth = new ArrayList<>();
    public List<Integer> azimuthMinIndex = new ArrayList<>();
    public List<Array> distance = new ArrayList<>();
    public List<Integer> disResolution = new ArrayList<>();
    private final List<List<Array>> data = new ArrayList<>();

    /**
     * Constructor
     * @param product Product name
     */
    public RadialRecord(String product) {
        this.product = product;
    }

    /**
     * Set bin length and update DataType
     * @param value Bin length
     */
    public void setBinLength(int value) {
        this.binLength = value;
        this.dataType = this.binLength == 1 ? DataType.UBYTE : DataType.USHORT;
        //this.dataType = DataType.FLOAT;
    }

    /**
     * Get data type
     * @return Data type
     */
    public DataType getDataType() {
        return this.dataType;
    }

    /**
     * Add an azimuth value
     * @param a Azimuth value
     */
    public void addAzimuth(float a) {
        int n = getScanNumber();
        List<Float> azi = this.azimuth.get(n - 1);
        azi.add(a);
        if (this.azimuthMinIndex.get(n - 1) == 0) {
            if (azi.size() > 1) {
                if (a < azi.get(azi.size() - 2)) {
                    this.azimuthMinIndex.set(n - 1, azi.size() - 1);
                }
            }
        }
    }

    /**
     * Is velocity group or not
     * @return Velocity group or not
     */
    public boolean isVelocityGroup() {
        return this.product.equals("V") || this.product.equals("W");
    }

    /**
     * Get scan number
     * @return Scan number
     */
    public int getScanNumber() {
        return elevation.size();
    }

    /**
     * Get radial number of a scan
     * @param scanIdx The scan index
     * @return Radial number
     */
    public int getRadialNumber(int scanIdx) {
        return elevation.get(scanIdx).size();
    }

    /**
     * Get radial number of a scan
     * @param scanIdx The scan index
     * @return Radial number
     */
    public int getGateNumber(int scanIdx) {
        return (int) distance.get(scanIdx).getSize();
    }

    /**
     * Add new scan data list
     */
    public void newScanData() {
        this.data.add(new ArrayList<>());
    }

    /**
     * Add a data bytes
     * @param bytes Data bytes
     */
    public void addDataBytes(byte[] bytes) {
        if (this.data.isEmpty()) {
            this.data.add(new ArrayList<>());
        }
        Array array;
        if (this.dataType == DataType.UBYTE) {
            array = Array.factory(this.dataType, new int[]{bytes.length}, bytes);
        } else {
            int n = bytes.length / 2;
            array = Array.factory(this.dataType, new int[]{n});
            for (int i = 0; i < n; i++) {
                short v = DataConvert.bytes2Short(new byte[]{bytes[i*2], bytes[i*2+1]}, ByteOrder.LITTLE_ENDIAN);
                array.setShort(i, v);
            }
        }
        this.data.get(this.data.size() - 1).add(array);
    }

    /**
     * Add a data bytes
     * @param bytes Data bytes
     * @param offset Offset
     * @param scale Scale
     */
    public void addDataBytes(byte[] bytes, int offset, int scale) {
        if (this.data.isEmpty()) {
            this.data.add(new ArrayList<>());
        }
        Array array;
        float v;
        if (this.binLength == 1) {
            array = Array.factory(this.dataType, new int[]{bytes.length});
            for (int i = 0; i < bytes.length; i++) {
                v = (float) DataType.unsignedByteToShort(bytes[i]);
                v = (v - offset) / scale;
                array.setFloat(i, v);
            }
        } else {
            int n = bytes.length / 2;
            array = Array.factory(this.dataType, new int[]{n});
            for (int i = 0; i < n; i++) {
                short s = DataConvert.bytes2Short(new byte[]{bytes[i*2], bytes[i*2+1]}, ByteOrder.LITTLE_ENDIAN);
                v = (float) DataType.unsignedShortToInt(s);
                v = (v - offset) / scale;
                array.setFloat(i, v);
            }
        }
        this.data.get(this.data.size() - 1).add(array);
    }

    /**
     * Get data array
     * @param scanIdx The scan index
     * @return Data array
     */
    public List<Array> getDataArray(int scanIdx) {
        return this.data.get(scanIdx);
    }

    /**
     * Get maximum radials number
     * @return Maximum radials number
     */
    public int getMaxRadials() {
        int maxRadials = 0;
        for (List a : this.azimuth) {
            if (maxRadials < a.size()) {
                maxRadials = a.size();
            }
        }

        return maxRadials;
    }

    /**
     * Get minimum radials number
     * @return Minimum radials number
     */
    public int getMinRadials() {
        int minRadials = Integer.MAX_VALUE;
        for (List a : this.azimuth) {
            if (minRadials > a.size()) {
                minRadials = a.size();
            }
        }

        return minRadials;
    }

    /**
     * Get XYZ data array
     * @param scanIdx The scan index
     * @return XYZ data array
     */
    public Array getXYZ(int scanIdx) {
        List<Float> azi = this.azimuth.get(scanIdx);
        Array dis = this.distance.get(scanIdx);
        List<Float> ele = this.elevation.get(scanIdx);
        int nz = 3;
        int ny = azi.size();
        int nx = (int) dis.getSize();
        Array r = Array.factory(DataType.FLOAT, new int[]{nz, ny, nx});
        Index index = r.getIndex();
        float a, e, x, y, z;
        double[] xyz;
        for (int i = 0; i < ny; i++) {
            a = (float) Math.toRadians(azi.get(i));
            e = (float) Math.toRadians(ele.get(i));
            for (int j = 0; j < nx; j++) {
                xyz = Transform.antennaToCartesian(dis.getFloat(j), a, e);
                index.set(0, i, j);
                r.setFloat(index, (float) xyz[0]);
                index.set(1, i, j);
                r.setFloat(index, (float) xyz[1]);
                index.set(2, i, j);
                r.setFloat(index, (float) xyz[2]);
            }
        }

        return r;
    }

    /**
     * Make variable
     * @param dataInfo The data info
     * @param dimensions Dimensions
     */
    public void makeVariable(CMARadarBaseDataInfo dataInfo, Dimension[] dimensions) {
        Variable variable = new Variable();
        variable.setName(this.product);
        variable.setDataType(this.dataType);
        for (Dimension dimension : dimensions) {
            variable.addDimension(dimension);
        }
        variable.addAttribute(new Attribute("scale_factor", this.scale));
        variable.addAttribute(new Attribute("add_offset", this.offset));
        dataInfo.addVariable(variable);
    }

    /**
     * Make variables
     * @param dataInfo The data info
     * @param xyzDim xyz dimension
     */
    public void makeVariables(CMARadarBaseDataInfo dataInfo, Dimension xyzDim) {
        for (int i = 0; i < getScanNumber(); i++) {
            String suffix = "_s" + String.valueOf(i + 1);
            Dimension radialDim = new Dimension(DimensionType.Y);
            radialDim.setShortName("azimuth_" + this.product + suffix);
            radialDim.setUnit("degree");
            radialDim.setDimValue(ArrayUtil.array_list(this.azimuth.get(i), DataType.FLOAT));
            dataInfo.addDimension(radialDim);
            Dimension disDim = new Dimension(DimensionType.X);
            disDim.setShortName("distance_" + this.product + suffix);
            disDim.setUnit("meter");
            disDim.setDimValue(this.distance.get(i));
            dataInfo.addDimension(disDim);
            Dimension eleDim = new Dimension(DimensionType.OTHER);
            eleDim.setShortName("elevation_" + this.product + suffix);
            eleDim.setUnit("degree");
            eleDim.setDimValue(ArrayUtil.array_list(this.elevation.get(i), DataType.FLOAT));
            dataInfo.addDimension(eleDim);

            Variable variable = new Variable();
            variable.setName(this.product + suffix);
            variable.setDataType(this.dataType);
            variable.addDimension(radialDim);
            variable.addDimension(disDim);
            variable.addAttribute(new Attribute("scale_factor", this.scale));
            variable.addAttribute(new Attribute("add_offset", this.offset));
            dataInfo.addVariable(variable);

            variable = new Variable();
            variable.setName("xyz_" + this.product + suffix);
            variable.setDataType(DataType.FLOAT);
            variable.addDimension(xyzDim);
            variable.addDimension(radialDim);
            variable.addDimension(disDim);
            variable.addAttribute(new Attribute("long_name", "x, y, z coordinates"));
            variable.addAttribute(new Attribute("units", "meters"));
            dataInfo.addVariable(variable);
        }
    }

    /**
     * Get azimuth value index
     * @param ei Scan index
     * @param a Azimuth value
     * @return Azimuth value index
     */
    public int getAzimuthIndex(int ei, float a) {
        List<Float> azs = this.azimuth.get(ei);
        int n = azs.size();
        int sIdx = this.azimuthMinIndex.get(ei);
        int eIdx = sIdx - 1;
        if (eIdx < 0) {
            eIdx = n - 1;
        }

        int i1 = -1, i2 = -1;
        if (a < azs.get(sIdx) || a > azs.get(eIdx)) {
            i1 = eIdx;
            i2 = sIdx;
        } else {
            for (int i = sIdx + 1; i < n; i++) {
                if (a == azs.get(i)) {
                    return i;
                } else if (a < azs.get(i)) {
                    i1 = i - 1;
                    i2 = i;
                    break;
                }
            }
            if (i1 < 0) {
                for (int i = 0; i <= eIdx; i++) {
                    if (a == azs.get(i)) {
                        return i;
                    } else if (a < azs.get(i)) {
                        i1 = i - 1;
                        i2 = i;
                        if (i1 < 0) {
                            i1 = n - 1;
                        }
                        break;
                    }
                }
            }
        }

        if (azs.get(i2) - a < a - azs.get(i1)) {
            return i2;
        } else {
            return i1;
        }
    }

    /**
     * Get value by elevation, azimuth and distance
     * @param ei Elevation index
     * @param a Azimuth value
     * @param r Distance value
     * @return Data value
     */
    public float getValue(int ei, float a, float r) {
        List<Array> sData = this.data.get(ei);
        int aziIdx = getAzimuthIndex(ei, a);
        int disRes = this.disResolution.get(ei);
        int disIdx = (int) (r / disRes);
        Array rData = this.data.get(ei).get(aziIdx);
        float v;
        if (disIdx < rData.getSize()) {
            v = rData.getFloat(disIdx);
            v = (v - this.offset) / this.scale;
        } else {
            v = Float.NaN;
        }

        return v;
    }
}
