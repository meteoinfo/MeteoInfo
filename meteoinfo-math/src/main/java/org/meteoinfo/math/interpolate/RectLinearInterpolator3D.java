package org.meteoinfo.math.interpolate;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RectLinearInterpolator3D extends RectInterpolator3D {
    /**
     * Constructor
     *
     * @param xa X coordinate array - 1D
     * @param ya Y coordinate array - 1D
     * @param za Z coordinate array - 1D
     * @param va Value array - 3D or more than 3D
     */
    public RectLinearInterpolator3D(Array xa, Array ya, Array za, Array va) {
        super(xa, ya, za, va);
    }

    @Override
    public double interpolate(double x, double y, double z) {
        List<Array> points = Arrays.asList(this.za, this.ya, this.xa);
        Array xi = Array.factory(DataType.DOUBLE, new int[]{3});
        xi.setDouble(0, z);
        xi.setDouble(1, y);
        xi.setDouble(2, x);

        return ArrayUtil.interpn_s(points, this.va, xi);
    }
}
