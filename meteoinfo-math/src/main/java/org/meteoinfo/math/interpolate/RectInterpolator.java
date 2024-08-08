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
     * Factory method
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param va Value array - 2D or more than 2D
     * @param method Interpolation method
     * @return RectInterpolator
     */
    public static RectInterpolator factory(Array xa, Array ya, Array va,
                                             InterpolationMethod method) {
        if (method == InterpolationMethod.NEAREST) {
            return new RectNearestInterpolator(xa, ya, va);
        } else {
            return new RectLinearInterpolator(xa, ya, va);
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
