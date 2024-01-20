package org.meteoinfo.geometry.colors;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.List;

/**
 * Generate a colormap index based on discrete intervals
 */
public class BoundaryNorm extends Normalize {
    private Array boundaries;
    private int size;
    private int nRegions;    //Number of colors needed
    private int offset;
    private ExtendType extendType;

    /**
     * Constructor
     * @param boundaries Boundaries
     * @param extendType Extend type
     */
    public BoundaryNorm(Array boundaries, ExtendType extendType) {
        super();
        boundaries = boundaries.copyIfView();
        this.setMinValue(boundaries.getDouble(0));
        this.setMaxValue(boundaries.getDouble((int)boundaries.getSize() - 1));

        this.boundaries = boundaries;
        this.size = (int) this.boundaries.getSize();
        this.extendType = extendType;
        this.nRegions = this.size - 1;
        this.offset = 0;
        switch (this.extendType) {
            case MIN:
                this.nRegions += 1;
                this.offset = 1;
                break;
            case MAX:
                this.nRegions += 1;
                break;
            case BOTH:
                this.nRegions += 2;
                this.offset = 1;
                break;
        }
    }

    /**
     * Get boundaries
     * @return Boundaries
     */
    public Array getBoundaries() {
        return this.boundaries;
    }

    /**
     * Get extend type
     * @return Extend type
     */
    public ExtendType getExtendType() {
        return this.extendType;
    }

    /**
     * Get boundaries size
     * @return Boundaries size
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Get offset
     * @return The offset
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Get number of color regions
     * @return Number of color regions
     */
    public int getNRegions() {
        return this.nRegions;
    }

    @Override
    public Number apply(Number v) {
        int idx = ArrayUtil.searchSorted(this.boundaries, v, true) - 1 + this.offset;

        return (float) idx / this.nRegions;
    }

    @Override
    public Array apply(Array a) {
        Array r = ArrayUtil.searchSorted(this.boundaries, a, false);
        r = ArrayMath.add(r, this.offset - 1);
        r = ArrayMath.div(r, (float) this.nRegions);

        return r;
    }

    @Override
    public double inverse(double v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array inverse(Array a) {
        throw new UnsupportedOperationException();
    }
}
