package org.meteoinfo.math.interpolate;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;

public class RectNearestInterpolator3D extends RectInterpolator3D{
    /**
     * Constructor
     *
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param za Z coordinate array - 1D
     * @param va Value array - 3D or more than 3D
     */
    public RectNearestInterpolator3D(Array xa, Array ya, Array za, Array va) {
        super(xa, ya, za, va);
    }

    @Override
    public double interpolate(double x, double y, double z) {
        int[] xyzIdx = gridIndex(xa, ya, za, x, y, z);
        if (xyzIdx == null) {
            return Double.NaN;
        }

        int k1 = xyzIdx[0];
        int i1 = xyzIdx[1];
        int j1 = xyzIdx[2];
        int k2 = k1 + 1;
        int i2 = i1 + 1;
        int j2 = j1 + 1;

        double x1 = xa.getDouble(j1);
        double x2 = xa.getDouble(j2);
        double y1 = ya.getDouble(i1);
        double y2 = ya.getDouble(i2);
        double z1 = za.getDouble(k1);
        double z2 = za.getDouble(k2);
        int kk = (z - z1) < (z2 - z) ? k1 : k2;
        int ii = (y - y1) < (y2 - y) ? i1 : i2;
        int jj = (x - x1) < (x2 - x) ? j1 : j2;

        Index index = va.getIndex();
        index.setDim(0, kk);
        index.setDim(1, ii);
        index.setDim(2, jj);

        return va.getDouble(index);
    }
}
