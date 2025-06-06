package org.meteoinfo.image.ndimage;

import org.meteoinfo.ndarray.*;

import java.util.ArrayList;
import java.util.List;

public class UniformFilter {
    int size = 3;
    ExtendMode mode;
    double cValue = 0.0;

    /**
     * Constructor
     * @param size Window size
     * @param mode Extend mode
     */
    public UniformFilter(int size, ExtendMode mode) {
        this.size = size;
        this.mode = mode;
    }

    /**
     * Constructor
     * @param size Window size
     */
    public UniformFilter(int size) {
        this(size, ExtendMode.REFLECT);
    }

    /**
     * Get extend mode
     * @return Extend mode
     */
    public ExtendMode getMode() {
        return this.mode;
    }

    /**
     * Set extend mode
     * @param value Extend mode
     */
    public void setMode(ExtendMode value) {
        this.mode = value;
    }

    /**
     * Get constant value
     * @return Constant value
     */
    public double getCValue() {
        return this.cValue;
    }

    /**
     * Set constant value
     * @param value Constant value
     */
    public void setCValue(double value) {
        this.cValue = value;
    }

    private double[] getWeights() {
        double[] weights = new double[size];
        double sum = 0;
        for (int i = 0; i < size; i++)
        {
            double g = 1.0;
            sum += g;
            weights[i] = g;
        }
        //Normalized
        for (int i = 0; i < size; i++) {
            weights[i] /= sum;
        }

        return weights;
    }

    /**
     * Filter with Gaussian kernel
     * @param data Input data
     * @return Filtered data
     * @throws InvalidRangeException
     */
    public Array filter(Array data) throws InvalidRangeException {
        double[] weights = getWeights();
        Array aWeights = Array.factory(DataType.DOUBLE, new int[]{weights.length}, weights);
        Correlate1D correlate1D = new Correlate1D(aWeights, mode);

        int ndim = data.getRank();
        int[] shape = data.getShape();
        Array r = Array.factory(data.getDataType(), shape);
        Index rindex = r.getIndex();
        int[] rcurrent = new int[ndim];
        int idx;
        for (int axis = 0; axis < ndim; axis++) {
            int[] nshape = new int[ndim - 1];
            for (int i = 0; i < ndim; i++) {
                if (i < axis)
                    nshape[i] = shape[i];
                else if (i > axis)
                    nshape[i - 1] = shape[i];
            }
            Index index = Index.factory(nshape);
            int[] current;
            for (int i = 0; i < index.getSize(); i++) {
                current = index.getCurrentCounter();
                List<Range> ranges = new ArrayList<>();
                for (int j = 0; j < ndim; j++) {
                    if (j == axis) {
                        ranges.add(new Range(0, shape[j] - 1, 1));
                        rcurrent[j] = 0;
                    } else {
                        idx = j;
                        if (idx > axis) {
                            idx -= 1;
                        }
                        ranges.add(new Range(current[idx], current[idx], 1));
                        rcurrent[j] = current[idx];
                    }
                }
                Array temp = data.section(ranges);
                temp = correlate1D.correlate(temp);
                for (int j = 0; j < shape[axis]; j++) {
                    rcurrent[axis] = j;
                    rindex.set(rcurrent);
                    r.setDouble(rindex, temp.getDouble(j));
                }
                index.incr();
            }
        }

        return r;
    }
}
