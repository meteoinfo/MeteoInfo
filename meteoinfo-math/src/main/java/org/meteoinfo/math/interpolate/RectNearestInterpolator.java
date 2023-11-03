package org.meteoinfo.math.interpolate;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;

public class RectNearestInterpolator extends RectInterpolator{
    /**
     * Constructor
     *
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param va Value array - 2D or more than 2D
     */
    public RectNearestInterpolator(Array xa, Array ya, Array va) {
        super(xa, ya, va);
    }

    @Override
    double cellValue(Index dindex, double x, double y) {
        int[] xyIdx = gridIndex(xa, ya, x, y);
        if (xyIdx == null) {
            return Double.NaN;
        }

        int i1 = xyIdx[0];
        int j1 = xyIdx[1];
        int i2 = i1 + 1;
        int j2 = j1 + 1;

        double x1 = xa.getDouble(j1);
        double x2 = xa.getDouble(j2);
        double y1 = ya.getDouble(i1);
        double y2 = ya.getDouble(i2);
        int ii = (y - y1) < (y2 - y) ? i1 : i2;
        int jj = (x - x1) < (x2 - x) ? j1 : j2;


        Index index = va.getIndex();
        int n = index.getRank();
        for (int i = 0; i < n - 2; i++) {
            index.setDim(i, dindex.getCurrentCounter()[i]);
        }
        index.setDim(n - 2, ii);
        index.setDim(n - 1, jj);
        double v = va.getDouble(index);

        return v;
    }

    @Override
    double interpolate(double x, double y) {
        int[] xyIdx = gridIndex(xa, ya, x, y);
        if (xyIdx == null) {
            return Double.NaN;
        }

        int i1 = xyIdx[0];
        int j1 = xyIdx[1];
        int i2 = i1 + 1;
        int j2 = j1 + 1;

        double x1 = xa.getDouble(j1);
        double x2 = xa.getDouble(j2);
        double y1 = ya.getDouble(i1);
        double y2 = ya.getDouble(i2);
        int ii = (y - y1) < (y2 - y) ? i1 : i2;
        int jj = (x - x1) < (x2 - x) ? j1 : j2;


        Index index = va.getIndex();
        index.setDim(0, ii);
        index.setDim(1, jj);

        return va.getDouble(index);
    }
}
