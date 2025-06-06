package org.meteoinfo.image.ndimage;

import java.util.List;

public enum ExtendMode {
    REFLECT,
    CONSTANT,
    NEAREST,
    MIRROR,
    WRAP;

    /**
     * Get value
     * @param dList The data list
     * @param idx The index
     * @param origin Origin
     * @param mode Extend mode
     * @return The value
     */
    public double getValue(List<Double> dList, int idx, int origin, double cValue) {
        int n = dList.size();
        switch (this) {
            case REFLECT:
                if (idx < 0) {
                    idx = -idx - 1;
                } else if (idx > n - 1) {
                    idx = n - (idx - (n - 1));
                }
                return dList.get(idx);
            case CONSTANT:
                if (idx < 0 || idx > n - 1) {
                    return cValue;
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
