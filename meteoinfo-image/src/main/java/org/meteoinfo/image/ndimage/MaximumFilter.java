package org.meteoinfo.image.ndimage;

import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;

import java.util.ArrayList;
import java.util.List;

public class MaximumFilter {
    int size = 3;
    ExtendMode mode;
    double cValue = 0.0;

    /**
     * Constructor
     * @param size Window size
     * @param mode Extend model
     */
    public MaximumFilter(int size, ExtendMode mode) {
        this.size = size;
        this.mode = mode;
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

    private ArrayList<Double> maximum(ArrayList<Double> a) {
        int origin = size / 2;
        int n = a.size();
        ArrayList<Double> r = new ArrayList<>();
        double v;
        int idx;
        for (int i = 0; i < n; i++) {
            v = -Double.MAX_VALUE;
            for (int j = 0; j < size; j++) {
                idx = i - origin + j;
                v = Math.max(mode.getValue(a, idx, origin, cValue), v);
            }
            r.add(v);
        }

        return r;
    }

    private Array maximum(Array a, int axis) throws InvalidRangeException {
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
            ArrayList<Double> rList = maximum(dList);
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
                ArrayList<Double> rList = maximum(stlist);
                for (int j = 0; j < nn; j++) {
                    indexr.set(current);
                    r.setObject(indexr, rList.get(j));
                    current[axis] = current[axis] + 1;
                }
            }
        }

        return r;
    }

    /**
     * Maximum filter
     * @param data Input data
     * @return Filtered data
     * @throws InvalidRangeException
     */
    public Array filter(Array data) throws InvalidRangeException {
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
                temp = maximum(temp, axis);
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
