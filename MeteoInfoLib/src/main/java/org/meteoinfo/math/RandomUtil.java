/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math;

import java.util.List;
import java.util.Random;
import org.apache.commons.math3.random.RandomDataGenerator;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class RandomUtil {
    public static long seed = 1;
    public static boolean useSeed = false;
    
    /**
     * Get random value
     *
     * @return Random value
     */
    public static double rand() {
        Random r = new Random();
        if (useSeed)
            r.setSeed(seed);
        return r.nextDouble();
    }

    /**
     * Get random array - one dimension
     *
     * @param n Array length
     * @return Result array
     */
    public static Array rand(int n) {
        Array r = Array.factory(DataType.DOUBLE, new int[]{n});
        Random rd = new Random();
        if (useSeed)
            rd.setSeed(seed);
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, rd.nextDouble());
        }

        return r;
    }

    /**
     * Get random array
     *
     * @param shape Shape
     * @return Array Result array
     */
    public static Array rand(List<Integer> shape) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(DataType.DOUBLE, ashape);
        Random rd = new Random();
        if (useSeed)
            rd.setSeed(seed);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, rd.nextDouble());
        }

        return a;
    }
    
    /**
     * Get random value
     *
     * @return Random value
     */
    public static double randn() {
        Random r = new Random();
        if (useSeed)
            r.setSeed(seed);
        return r.nextGaussian();
    }

    /**
     * Get random array - one dimension
     *
     * @param n Array length
     * @return Result array
     */
    public static Array randn(int n) {
        Array r = Array.factory(DataType.DOUBLE, new int[]{n});
        Random rd = new Random();
        if (useSeed)
            rd.setSeed(seed);
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, rd.nextGaussian());
        }

        return r;
    }

    /**
     * Get random array
     *
     * @param shape Shape
     * @return Array Result array
     */
    public static Array randn(List<Integer> shape) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(DataType.DOUBLE, ashape);
        Random rd = new Random();
        if (useSeed)
            rd.setSeed(seed);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, rd.nextGaussian());
        }

        return a;
    }
    
    /**
     * Get random int value
     *
     * @param bound Highest value
     * @return Random int value
     */
    public static int randint(int bound) {
        Random r = new Random();
        if (useSeed)
            r.setSeed(seed);
        return r.nextInt(bound);
    }
    
    /**
     * Get random integer array
     *
     * @param bound Highest value
     * @param n Array length
     * @return Array Result array
     */
    public static Array randint(int bound, int n) {
        Array a = Array.factory(DataType.INT, new int[]{n});
        Random rd = new Random();
        if (useSeed)
            rd.setSeed(seed);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, rd.nextInt(bound));
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
    public static Array randint(int bound, List<Integer> shape) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(DataType.INT, ashape);
        Random rd = new Random();
        if (useSeed)
            rd.setSeed(seed);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, rd.nextInt(bound));
        }

        return a;
    }
    
    /**
     * Get random data from a Poisson distribution
     * @param mean Poisson mean
     * @return Random value
     */
    public static double poisson(double mean){
        RandomDataGenerator rdg = new RandomDataGenerator();
        if (useSeed)
            rdg.reSeed(seed);
        return rdg.nextPoisson(mean);
    }
    
    /**
     * Get random data from a Poisson distribution
     *
     * @param mean Poisson mean
     * @param n Array length
     * @return Array Result array
     */
    public static Array poisson(double mean, int n) {
        Array a = Array.factory(DataType.INT, new int[]{n});
        RandomDataGenerator rd = new RandomDataGenerator();
        if (useSeed)
            rd.reSeed(seed);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, rd.nextPoisson(mean));
        }

        return a;
    }
    
    /**
     * Get random data from a Poisson distribution
     *
     * @param mean Poisson mean
     * @param shape Shape
     * @return Array Result array
     */
    public static Array poisson(double mean, List<Integer> shape) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(DataType.INT, ashape);
        RandomDataGenerator rd = new RandomDataGenerator();
        if (useSeed)
            rd.reSeed(seed);
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, rd.nextPoisson(mean));
        }

        return a;
    }
        
}
