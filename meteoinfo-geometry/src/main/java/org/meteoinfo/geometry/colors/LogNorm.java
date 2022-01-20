package org.meteoinfo.geometry.colors;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

/**
 * Normalize a given value to the 0-1 range on a log scale
 */
public class LogNorm extends Normalize {
    private double min;
    private double max;

    /**
     * Constructor
     * @param minValue Minimum value
     * @param maxValue Maximum value
     */
    public LogNorm(double minValue, double maxValue) {
        this(minValue, maxValue, false);
    }

    /**
     * Constructor
     * @param minValue Minimum value
     * @param maxValue Maximum value
     * @param clip Clip
     */
    public LogNorm(double minValue, double maxValue, boolean clip) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.clip = clip;
        this.min = Math.log10(minValue);
        this.max = Math.log10(maxValue);
    }

    @Override
    public Number apply(double v) {
        double range = max - min;
        v = Math.log10(v);
        v = (v - min) / range;
        if (clip) {
            if (v < 0)
                v = 0;
            else if (v > 1)
                v = 1;
        }
        return v;
    }

    @Override
    public Array apply(Array a) {
        this.autoScaleNull(a);

        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        double v, range = max - min;
        if (clip) {
            while (iterA.hasNext()) {
                v = Math.log10(iterA.getDoubleNext());
                v = (v - min) / range;
                if (v < 0)
                    v = 0;
                else if (v > 1)
                    v = 1;
                iterR.setDoubleNext(v);
            }
        } else {
            while (iterA.hasNext()) {
                v = iterA.getDoubleNext();
                v = (v - min) / range;
                iterR.setDoubleNext(v);
            }
        }

        return r;
    }

    @Override
    public double inverse(double v) {
        return Math.pow(10, min + v * (max - min));
    }

    @Override
    public Array inverse(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        double v, range = max - min;
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
            v = Math.pow(10, min + v * range);
            iterR.setDoubleNext(v);
        }
        return r;
    }

}
