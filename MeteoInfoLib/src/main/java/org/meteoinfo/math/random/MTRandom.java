package org.meteoinfo.math.random;

import org.apache.commons.math3.random.MersenneTwister;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;

import java.util.List;
import java.util.Random;

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
     */
    public void shuffle(Array x) {
        for (int i = (int)x.getSize() - 1; i > 0; i--) {
            int index = this.nextInt(i);
            // swap
            Object tmp = x.getObject(index);
            x.setObject(index, x.getObject(i));
            x.setObject(i, tmp);
        }
    }
}
