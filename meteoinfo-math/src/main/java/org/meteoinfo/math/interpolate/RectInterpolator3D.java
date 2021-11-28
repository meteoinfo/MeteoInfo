package org.meteoinfo.math.interpolate;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.Index3D;

import java.util.Arrays;

public abstract class RectInterpolator3D {
    protected Array xa;
    protected Array ya;
    protected Array za;
    protected Array va;

    /**
     * Constructor
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param za Z coordinate array - 1D
     * @param va Value array - 3D or more than 3D
     */
    public RectInterpolator3D(Array xa, Array ya, Array za, Array va) {
        this.xa = xa.copyIfView();
        this.ya = ya.copyIfView();
        this.za = za.copyIfView();
        this.va = va;
    }

    /**
     * Factory method
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param za Z coordinate array - 1D
     * @param va Value array - 3D or more than 3D
     * @param method Interpolation method
     * @return RectInterpolator3D
     */
    public static RectInterpolator3D factory(Array xa, Array ya, Array za, Array va,
                                             InterpolationMethod method) {
        if (method == InterpolationMethod.NEAREST) {
            return new RectNearestInterpolator3D(xa, ya, za, va);
        } else {
            return new RectLinearInterpolator3D(xa, ya, za, va);
        }
    }

    /**
     * Get value index in a dimension array
     *
     * @param dim Dimension array
     * @param v The value
     * @return value index
     */
    public static int getDimIndex(Array dim, Number v) {
        dim = dim.copyIfView();
        switch (dim.getDataType()) {
            case BYTE:
                return Arrays.binarySearch((byte[]) dim.getStorage(), v.byteValue());
            case INT:
                return Arrays.binarySearch((int[]) dim.getStorage(), v.intValue());
            case SHORT:
                return Arrays.binarySearch((short[]) dim.getStorage(), v.shortValue());
            case LONG:
                return Arrays.binarySearch((long[]) dim.getStorage(), v.longValue());
            case FLOAT:
                return Arrays.binarySearch((float[]) dim.getStorage(), v.floatValue());
            case DOUBLE:
                return Arrays.binarySearch((double[]) dim.getStorage(), v.doubleValue());
        }

        int n = (int) dim.getSize();
        if (v.doubleValue() < dim.getDouble(0) || v.doubleValue() > dim.getDouble(n - 1)) {
            return -1;
        }

        int idx = n - 1;
        for (int i = 1; i < n; i++) {
            if (v.doubleValue() < dim.getDouble(i)) {
                idx = i - 1;
                break;
            }
        }
        return idx;
    }

    /**
     * Get grid array x/y value index
     * @param xdim X coordinate array
     * @param ydim Y coordinate array
     * @param zdim Z coordinate array
     * @param x X value
     * @param y Y value
     * @param z Z value
     * @return X/Y/Z index
     */
    public static int[] gridIndex(Array xdim, Array ydim, Array zdim, double x, double y,
                                  double z) {
        if (xdim.getRank() == 1) {
            int xn = (int) xdim.getSize();
            int yn = (int) ydim.getSize();
            int zn = (int) zdim.getSize();
            int xIdx = getDimIndex(xdim, x);
            if (xIdx == -1 || xIdx == -(xn + 1)) {
                return null;
            } else if (xIdx < 0) {
                xIdx = -xIdx - 2;
            }

            int yIdx = getDimIndex(ydim, y);
            if (yIdx == -1 || yIdx == -(yn + 1)) {
                return null;
            } else if (yIdx < 0) {
                yIdx = -yIdx - 2;
            }

            int zIdx = getDimIndex(zdim, z);
            if (zIdx == -1 || zIdx == -(zn + 1)) {
                return null;
            } else if (zIdx < 0) {
                zIdx = -zIdx - 2;
            }

            if (xIdx == xn - 1) {
                xIdx = xn - 2;
            }
            if (yIdx == yn - 1) {
                yIdx = yn - 2;
            }
            if (zIdx == zn - 1) {
                zIdx = zn - 2;
            }

            return new int[]{zIdx, yIdx, xIdx};
        } else {
            int xIdx = -1, yIdx = -1, zIdx = -1;
            int[] shape = xdim.getShape();
            int zn = shape[0];
            int yn = shape[1];
            int xn = shape[2];
            Index index = new Index3D(shape);
            double x1, x2, y1, y2, z1, z2;
            for (int k = 0; k < zn - 1; k++) {
                for (int i = 0; i < yn - 1; i++) {
                    for (int j = 0; j < xn - 1; j++) {
                        index = index.set(k, i, j);
                        z1 = zdim.getDouble(index);
                        index = index.set(k + 1, i, j);
                        z2 = zdim.getDouble(index);
                        if (z >= z1 && z < z2) {
                            index = index.set(k, i, j);
                            y1 = ydim.getDouble(index);
                            index = index.set(k, i + 1, j);
                            y2 = ydim.getDouble(index);
                            if (y >= y1 && y < y2) {
                                index = index.set(k, i, j);
                                x1 = xdim.getDouble(index);
                                index = index.set(k, i, j + 1);
                                x2 = xdim.getDouble(index);
                                if (x >= x1 && x < x2) {
                                    zIdx = k;
                                    yIdx = i;
                                    xIdx = j;
                                }
                            }
                        }
                    }
                }
            }

            if (zIdx >=0 && yIdx >= 0 && xIdx >= 0)
                return new int[]{zIdx, yIdx, xIdx};
            else
                return null;
        }
    }

    public abstract double interpolate(double x, double y, double z);

    /**
     * Interpolate
     * @param newX Interpolated x array
     * @param newY Interpolated y array
     * @param newZ Interpolated z array
     * @return Interpolated result
     */
    public Array interpolate(Array newX, Array newY, Array newZ) {
        newX = newX.copyIfView();
        newY = newY.copyIfView();
        newZ = newZ.copyIfView();

        double x, y, z, v;
        Array r = Array.factory(DataType.DOUBLE, newX.getShape());

        for (int k = 0; k < r.getSize(); k++) {
            z = newZ.getDouble(k);
            y = newY.getDouble(k);
            x = newX.getDouble(k);
            v = interpolate(x, y, z);
            r.setDouble(k, v);
        }

        return r;
    }
}
