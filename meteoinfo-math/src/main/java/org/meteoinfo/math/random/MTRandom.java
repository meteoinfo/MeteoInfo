package org.meteoinfo.math.random;

import org.apache.commons.math3.random.MersenneTwister;
import org.meteoinfo.ndarray.*;

import java.util.ArrayList;
import java.util.List;

public class MTRandom extends MersenneTwister {

    /**
     * Constructor
     */
    public MTRandom() {
        super();
    }

    /**
     * Constructor
     * @param seed Seed
     */
    public MTRandom(int seed) {
        super(seed);
    }

    /**
     * Get random array - one dimension
     *
     * @param n Array length
     * @return Result array
     */
    public Array rand(int n) {
        Array r = Array.factory(DataType.DOUBLE, new int[]{n});
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, this.nextDouble());
        }

        return r;
    }

    /**
     * Get random array
     *
     * @param shape Shape
     * @return Array Result array
     */
    public Array rand(List<Integer> shape) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(DataType.DOUBLE, ashape);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, this.nextDouble());
        }

        return a;
    }

    /**
     * Get random value
     *
     * @return Random value
     */
    public double randn() {
        return this.nextGaussian();
    }

    /**
     * Get random array - one dimension
     *
     * @param n Array length
     * @return Result array
     */
    public Array randn(int n) {
        Array r = Array.factory(DataType.DOUBLE, new int[]{n});
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, this.nextGaussian());
        }

        return r;
    }

    /**
     * Get random array
     *
     * @param shape Shape
     * @return Array Result array
     */
    public Array randn(List<Integer> shape) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(DataType.DOUBLE, ashape);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, this.nextGaussian());
        }

        return a;
    }

    /**
     * Get random int value
     *
     * @param bound Highest value
     * @return Random int value
     */
    public int randint(int bound) {
        return this.nextInt(bound);
    }

    /**
     * Get random integer array
     *
     * @param bound Highest value
     * @param n Array length
     * @return Array Result array
     */
    public Array randint(int bound, int n) {
        Array a = Array.factory(DataType.INT, new int[]{n});
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, this.nextInt(bound));
        }

        return a;
    }

    /**
     * Get random integer array
     *
     * @param bound Highest value
     * @param shape Shape
     * @return Array Result array
     */
    public Array randint(int bound, List<Integer> shape) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(DataType.INT, ashape);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, this.nextInt(bound));
        }

        return a;
    }

    /**
     * Fisher-Yates algorithm with O(n) time complexity
     * Permutes the given array
     *
     * @param x array to be shuffled
     * @param axis The axis which x is shuffled along
     */
    public void shuffle(Array x, int axis) throws InvalidRangeException {
        x = x.copyIfView();
        int nDim = x.getRank();
        int ii;
        if (nDim == 1) {
            Object tmp;
            for (int i = (int) x.getSize() - 1; i > 0; i--) {
                ii = this.nextInt(i);
                if (ii != i) {
                    // swap
                    tmp = x.getObject(ii);
                    x.setObject(ii, x.getObject(i));
                    x.setObject(i, tmp);
                }
            }
        } else {
            int n = x.getShape()[axis];
            Array tmp1, tmp2;
            int[] shape = x.getShape();
            shape[axis] = 1;
            List<Range> ranges = new ArrayList<>();
            for (int i = 0; i < nDim; i++) {
                ranges.add(new Range(0, shape[i] - 1, 1));
            }
            Index index = x.getIndex();
            for (int i = n - 1; i > 0; i--) {
                ii = this.nextInt(i);
                if (ii != i) {
                    // swap
                    ranges.set(axis, new Range(ii, ii, 1));
                    tmp1 = x.section(ranges).copy();
                    Index iIndex1 = index.section(ranges);
                    ranges.set(axis, new Range(i, i, 1));
                    tmp2 = x.section(ranges);
                    Index iIndex2 = index.section(ranges);
                    IndexIterator iter1 = tmp1.getIndexIterator();
                    IndexIterator iter2 = tmp2.getIndexIterator();
                    while (iter2.hasNext()) {
                        x.setObject(iIndex1, iter2.getObjectNext());
                        iIndex1.incr();
                    }
                    while (iter1.hasNext()) {
                        x.setObject(iIndex2, iter1.getObjectNext());
                        iIndex2.incr();
                    }
                }
            }
        }
    }
}
