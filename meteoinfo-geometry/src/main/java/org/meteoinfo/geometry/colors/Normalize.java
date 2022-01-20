package org.meteoinfo.geometry.colors;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.math.ArrayMath;

/**
 * A class which, when called, linearly normalizes data into the [0.0, 1.0] interval.
 */
public class Normalize {
    protected Double minValue;
    protected Double maxValue;
    protected boolean clip;

    /**
     * Constructor
     */
    public Normalize() {
        this.minValue = null;
        this.maxValue = null;
        this.clip = false;
    }

    /**
     * Constructor
     * @param minValue Minimum value
     * @param maxValue Maximum value
     */
    public Normalize(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.clip = false;
    }

    /**
     * Constructor
     * @param minValue Minimum value
     * @param maxValue Maximum value
     * @param clip Clip
     */
    public Normalize(double minValue, double maxValue, boolean clip) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.clip = clip;
    }

    /**
     * Get minimum value
     * @return Minimum value
     */
    public double getMinValue() {
        return this.minValue;
    }

    /**
     * Set minimum value
     * @param value Minimum value
     */
    public void setMinValue(double value) {
        this.minValue = value;
    }

    /**
     * Get maximum value
     * @return Maximum value
     */
    public double getMaxValue() {
        return this.maxValue;
    }

    /**
     * Set maximum value
     * @param value Maximum value
     */
    public void setMaxValue(double value) {
        this.maxValue = value;
    }

    /**
     * Get is clip or not
     * @return Clip or not
     */
    public boolean isClip() {
        return this.clip;
    }

    /**
     * Set clip or not
     * @param value Clip or not
     */
    public void setClip(boolean value) {
        this.clip = value;
    }

    /**
     * Set minimum and maximum values by data array
     * @param a The data array
     */
    public void autoScale(Array a) {
        this.minValue = ArrayMath.min(a).doubleValue();
        this.maxValue = ArrayMath.max(a).doubleValue();
    }

    /**
     * Set minimum and maximum values by data array if minValue or maxValue is not set
     * @param a The data array
     */
    public void autoScaleNull(Array a) {
        if (this.minValue == null)
            this.minValue = ArrayMath.min(a).doubleValue();
        if (this.maxValue == null)
            this.maxValue = ArrayMath.max(a).doubleValue();
    }

    /**
     * Check whether minimum and maximum values are set
     * @return Boolean
     */
    public boolean isScaled() {
        return this.minValue != null && this.maxValue != null;
    }

    /**
     * Normalize a value
     * @param v The value
     * @return Normalized value
     */
    public Number apply(double v) {
        double range = maxValue - minValue;
        v = (v - minValue) / range;
        if (clip) {
            if (v < 0)
                v = 0;
            else if (v > 1)
                v = 1;
        }
        return v;
    }

    /**
     * Normalize the data array
     * @param a The data array
     * @return Normalized data array
     */
    public Array apply(Array a) {
        this.autoScaleNull(a);

        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        double v, range = maxValue - minValue;
        if (clip) {
            while (iterA.hasNext()) {
                v = iterA.getDoubleNext();
                v = (v - minValue) / range;
                if (v < 0)
                    v = 0;
                else if (v > 1)
                    v = 1;
                iterR.setDoubleNext(v);
            }
        } else {
            while (iterA.hasNext()) {
                v = iterA.getDoubleNext();
                v = (v - minValue) / range;
                iterR.setDoubleNext(v);
            }
        }

        return r;
    }

    /**
     * Inverse data value
     * @param v The data value
     * @return Inverse data value
     */
    public double inverse(double v) {
        return minValue + v * (maxValue - minValue);
    }

    /**
     * Inverse data array
     * @param a The data array
     * @return Inverse data array
     */
    public Array inverse(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        double v, range = maxValue - minValue;
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
            v = minValue + v * range;
            iterR.setDoubleNext(v);
        }
        return r;
    }
}
