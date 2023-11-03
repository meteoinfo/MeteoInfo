package org.meteoinfo.math.interpolate;

import org.checkerframework.checker.units.qual.A;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.Index2D;

import java.util.Arrays;

public abstract class RectInterpolator {
    protected Array xa;
    protected Array ya;
    protected Array va;

    /**
     * Constructor
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param va Value array - 2D or more than 2D
     */
    public RectInterpolator(Array xa, Array ya, Array va) {
        this.xa = xa.copyIfView();
        this.ya = ya.copyIfView();
        this.va = va;
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
     * @param x X value
     * @param y Y value
     * @return X/Y index
     */
    public static int[] gridIndex(Array xdim, Array ydim, double x, double y) {
        if (xdim.getRank() == 1) {
            int xn = (int) xdim.getSize();
            int yn = (int) ydim.getSize();
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

            if (xIdx == xn - 1) {
                xIdx = xn - 2;
            }
            if (yIdx == yn - 1) {
                yIdx = yn - 2;
            }

            return new int[]{yIdx, xIdx};
        } else {
            int xIdx = -1, yIdx = -1;
            int[] shape = xdim.getShape();
            int yn = shape[0];
            int xn = shape[1];
            Index index = new Index2D(shape);
            double x1, x2, y1, y2;
            for (int i = 0; i < yn - 1; i++) {
                for (int j = 0; j < xn - 1; j++) {
                    index = index.set(i, j);
                    y1 = ydim.getDouble(index);
                    index = index.set(i+1, j);
                    y2 = ydim.getDouble(index);
                    if (y >= y1 && y < y2) {
                        index = index.set(i, j);
                        x1 = xdim.getDouble(index);
                        index = index.set(i, j+1);
                        x2 = xdim.getDouble(index);
                        if (x >= x1 && x < x2) {
                            yIdx = i;
                            xIdx = j;
                        }
                    }
                }
            }

            if (yIdx >= 0 && xIdx >= 0)
                return new int[]{yIdx, xIdx};
            else
                return null;
        }
    }

    abstract double cellValue(Index index, double x, double y);

    abstract double interpolate(double x, double y);

    /**
     * Interpolate
     * @param newX Interpolated x array
     * @param newY Interpolated y array
     * @return Interpolated result
     */
    public Array interpolate(Array newX, Array newY) {
        newX = newX.copyIfView();
        newY = newY.copyIfView();
        if (this.va.getRank() == 2 && (newY.getSize() == newX.getSize())) {
            double x, y, v;
            Array r = Array.factory(DataType.DOUBLE, newX.getShape());

            for (int k = 0; k < r.getSize(); k++) {
                y = newY.getDouble(k);
                x = newX.getDouble(k);
                v = interpolate(x, y);
                r.setDouble(k, v);
            }

            return r;
        } else {
            int xn = (int) newX.getSize();
            int yn = (int) newY.getSize();
            int[] shape = this.va.getShape();
            int n = shape.length;
            shape[n - 1] = xn;
            shape[n - 2] = yn;
            double x, y, v;
            Array r = Array.factory(DataType.DOUBLE, shape);

            Index index = r.getIndex();
            int[] counter;
            int yi, xi;
            for (int k = 0; k < r.getSize(); k++) {
                counter = index.getCurrentCounter();
                yi = counter[n - 2];
                xi = counter[n - 1];
                y = newY.getDouble(yi);
                x = newX.getDouble(xi);
                v = cellValue(index, x, y);
                r.setDouble(index, v);
                index.incr();
            }

            return r;
        }
    }
}
