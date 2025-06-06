package org.meteoinfo.image.ndimage;

import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ListIndexComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Correlate1D {
    Array weights;
    ExtendMode mode;
    int axis = -1;
    double cValue = 0.0;

    /**
     * Constructor
     *
     * @param weights Weights
     * @param mode Extend mode
     */
    public Correlate1D(Array weights, ExtendMode mode) {
        this.weights = weights.copyIfView();
        this.mode = mode;
    }

    /**
     * Constructor
     *
     * @param weights Weights
     */
    public Correlate1D(Array weights) {
        this(weights, ExtendMode.REFLECT);
    }

    /***
     * Constructor
     */
    public Correlate1D() {
        this(Array.factory(DataType.INT, new int[]{1,1,1}), ExtendMode.REFLECT);
    }

    /**
     * Get weights
     * @return Weights
     */
    public Array getWeights() {
        return this.weights;
    }

    /**
     * Set weights
     * @param value Weights
     */
    public void setWeights(Array value) {
        this.weights = value.copyIfView();
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
     * Get axis
     * @return Axis
     */
    public int getAxis() {
        return this.axis;
    }

    /**
     * Set axis
     * @param value Axis
     */
    public void setAxis(int value) {
        this.axis = value;
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

    /**
     * Calculate a 1-D correlation along the given axis.
     * The lines of the array along the given axis are correlated with the given weights.
     *
     * @param a The input array
     * @return Correlation result
     */
    public Array correlate(Array a) throws InvalidRangeException {
        int size = (int) weights.getSize();
        int origin = size / 2;
        int n = (int) a.getSize();
        Array r = Array.factory(a.getDataType(), a.getShape());
        double v;
        int idx;
        if (a.getRank() == 1) {
            ArrayList<Double> dList = new ArrayList<>();
            IndexIterator iter = a.getIndexIterator();
            while (iter.hasNext()) {
                dList.add(iter.getDoubleNext());
            }
            ArrayList<Double> rList = correlate(dList);
            for (int i = 0; i < r.getSize(); i++) {
                r.setDouble(i, rList.get(i));
            }
        } else {
            Index index = a.getIndex();
            int[] shape = a.getShape();
            if (axis == -1) {
                axis = n - 1;
            }
            int nn = shape[axis];
            Index indexr = r.getIndex();
            int[] current;
            List<Range> ranges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (i == axis) {
                    ranges.add(new Range(0, 0, 1));
                } else {
                    ranges.add(new Range(0, shape[i] - 1, 1));
                }
            }
            IndexIterator rii = r.sectionNoReduce(ranges).getIndexIterator();
            while (rii.hasNext()) {
                rii.next();
                current = rii.getCurrentCounter();
                ranges = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    if (j == axis) {
                        ranges.add(new Range(0, shape[j] - 1, 1));
                    } else {
                        ranges.add(new Range(current[j], current[j], 1));
                    }
                }
                ArrayList<Double> stlist = new ArrayList();
                IndexIterator ii = a.getRangeIterator(ranges);
                while (ii.hasNext()) {
                    v = ii.getDoubleNext();
                    stlist.add(v);
                }
                ArrayList<Double> rList = correlate(stlist);
                for (int j = 0; j < nn; j++) {
                    indexr.set(current);
                    r.setObject(indexr, rList.get(j));
                    current[axis] = current[axis] + 1;
                }
            }
        }

        return r;
    }

    private ArrayList<Double> correlate(ArrayList<Double> a) {
        int size = (int) weights.getSize();
        int origin = size / 2;
        int n = a.size();
        ArrayList<Double> r = new ArrayList<>();
        double v;
        int idx;
        for (int i = 0; i < n; i++) {
            v = 0;
            for (int j = 0; j < size; j++) {
                idx = i - origin + j;
                v += getValue(a, idx, origin) * weights.getDouble(j);
            }
            r.add(v);
        }

        return r;
    }

    private double getValue(List<Double> dList, int idx, int origin) {
        int n = dList.size();
        switch (this.mode) {
            case REFLECT:
                if (idx < 0) {
                    idx = -idx - 1;
                } else if (idx > n - 1) {
                    idx = n - (idx - (n - 1));
                }
                return dList.get(idx);
            case CONSTANT:
                if (idx < 0 || idx > n - 1) {
                    return this.cValue;
                } else {
                    return dList.get(idx);
                }
            case NEAREST:
                if (idx < 0) {
                    idx = 0;
                } else if (idx > n - 1) {
                    idx = n - 1;
                }
                return dList.get(idx);
            case MIRROR:
                if (idx < 0) {
                    idx = -idx;
                } else if (idx > n - 1) {
                    idx = n - 1 - (idx - (n - 1));
                }
                return dList.get(idx);
            case WRAP:
                if (idx < 0) {
                    idx = idx + origin;
                } else if (idx > n - 1) {
                    idx = idx - origin;
                }
                return dList.get(idx);
        }

        return Double.NaN;
    }

}
