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
    public List<List<Float>> elevation = new ArrayList<>();
    public List<List<Float>> azimuth = new ArrayList<>();
    public List<Array> distance = new ArrayList<>();
    private List<List<Array>> data = new ArrayList<>();

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
    }

    /**
     * Get data type
     * @return Data type
     */
    public DataType getDataType() {
        return this.dataType;
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
     * Get data array
     * @param scanIdx The scan index
     * @return Data array
     */
    public List<Array> getDataArray(int scanIdx) {
        return this.data.get(scanIdx);
    }

    /**
     * Convert antenna coordinate to cartesian coordinate
     * @param r Distances to the center of the radar gates (bins) in meters
     * @param a Azimuth angle of the radar in radians
     * @param e Elevation angle of the radar in radians
     * @return Cartesian coordinate in meters from the radar
     */
    public double[] antennaToCartesian(float r, float a, float e) {
        double R = 6371.0 * 1000.0 * 4.0 / 3.0;     // effective radius of earth in meters.

        double z = Math.pow(r * r + R * R + 2.0 * r * R * Math.sin(e), 0.5) - R;
        double s = R * Math.asin(r * Math.cos(e) / (R + z));  // arc length in m.
        double x = s * Math.sin(a);
        double y = s * Math.cos(a);

        return new double[]{x, y, z};
    }

    /**
     * Convert antenna coordinate to cartesian coordinate
     * @param r Distances to the center of the radar gates (bins) in meters
     * @param a Azimuth angle of the radar in radians
     * @param e Elevation angle of the radar in radians
     * @param h Altitude of the instrument, above sea level, units:m
     * @return Cartesian coordinate in meters from the radar
     */
    public double[] antennaToCartesian(float r, float a, float e, float h) {
        double R = 6371.0 * 1000.0 * 4.0 / 3.0;     // effective radius of earth in meters.

        double z = Math.pow(Math.pow(r * Math.cos(e), 2) + Math.pow(R + h + r * Math.sin(e), 2), 0.5) - R;
        double s = R * Math.asin(r * Math.cos(e) / (R + z));  // arc length in m.
        double x = s * Math.sin(a);
        double y = s * Math.cos(a);

        return new double[]{x, y, z};
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
                xyz = antennaToCartesian(dis.getFloat(j), a, e);
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
}
