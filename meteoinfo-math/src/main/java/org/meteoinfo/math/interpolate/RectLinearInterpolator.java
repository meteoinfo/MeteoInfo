package org.meteoinfo.math.interpolate;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;

import java.util.ArrayList;
import java.util.List;

public class RectLinearInterpolator extends RectInterpolator{
    /**
     * Constructor
     *
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param va Value array - 2D or more than 2D
     */
    public RectLinearInterpolator(Array xa, Array ya, Array va) {
        super(xa, ya, va);
    }

    @Override
    double cellValue(Index dindex, double x, double y) {
        double iValue = Double.NaN;
        int[] xyIdx = gridIndex(xa, ya, x, y);
        if (xyIdx == null) {
            return iValue;
        }

        int i1 = xyIdx[0];
        int j1 = xyIdx[1];
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        Index index = va.getIndex();
        int n = index.getRank();
        for (int i = 0; i < n - 2; i++) {
            index.setDim(i, dindex.getCurrentCounter()[i]);
        }
        index.setDim(n - 2, i1);
        index.setDim(n - 1, j1);
        double a = va.getDouble(index);
        index.setDim(n - 1, j2);
        double b = va.getDouble(index);
        index.setDim(n - 2, i2);
        index.setDim(n - 1, j1);
        double c = va.getDouble(index);
        index.setDim(n - 2, i2);
        index.setDim(n - 1, j2);
        double d = va.getDouble(index);
        List<Double> dList = new ArrayList<>();
        if (!Double.isNaN(a)) {
            dList.add(a);
        }
        if (!Double.isNaN(b)) {
            dList.add(b);
        }
        if (!Double.isNaN(c)) {
            dList.add(c);
        }
        if (!Double.isNaN(d)) {
            dList.add(d);
        }

        if (dList.isEmpty()) {
            return iValue;
        } else if (dList.size() == 1) {
            iValue = dList.get(0);
        } else if (dList.size() <= 3) {
            double aSum = 0;
            for (double dd : dList) {
                aSum += dd;
            }
            iValue = aSum / dList.size();
        } else {
            double dx = xa.getDouble(j1 + 1) - xa.getDouble(j1);
            double dy = ya.getDouble(i1 + 1) - ya.getDouble(i1);
            double x1val = a + (c - a) * (y - ya.getDouble(i1)) / dy;
            double x2val = b + (d - b) * (y - ya.getDouble(i1)) / dy;
            iValue = x1val + (x2val - x1val) * (x - xa.getDouble(j1)) / dx;
        }

        return iValue;
    }
}
