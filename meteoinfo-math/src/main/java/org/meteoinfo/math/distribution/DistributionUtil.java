/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.distribution;

import org.apache.commons.math4.legacy.distribution.MultivariateNormalDistribution;
import org.apache.commons.math4.legacy.distribution.MultivariateRealDistribution;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.statistics.distribution.NormalDistribution;
import org.apache.commons.statistics.distribution.ContinuousDistribution;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author wyq
 */
public class DistributionUtil {

    /**
     * Create MultivariateNormalDistribution object
     * @param means Vector of means
     * @param covariances Matrix of covariances
     * @return MultivariateNormalDistribution object
     */
    public static MultivariateNormalDistribution mvNormDist(Array means, Array covariances) {
        double[] m = (double[]) ArrayUtil.copyToNDJavaArray_Double(means);
        double[][] cov = (double[][]) ArrayUtil.copyToNDJavaArray_Double(covariances);

        return new MultivariateNormalDistribution(m, cov);
    }

    /**
     * Random variates of given type.
     * @param dis Distribution.
     * @param n Size.
     * @return Result array.
     */
    public static Array rvs(ContinuousDistribution dis, int n){
        ContinuousDistribution.Sampler sampler = dis.createSampler(RandomSource.MT.create());
        double[] samples = new double[n];
        for (int i = 0; i < n; i++) {
            samples[i] = sampler.sample();
        }
        Array r = Array.factory(DataType.DOUBLE, new int[]{n}, samples);        
        return r;
    }

    /**
     * Random variates of given type.
     * @param dis Distribution.
     * @param size Size.
     * @return Result array.
     */
    public static Array rvs(ContinuousDistribution dis, List<Integer> size){
        ContinuousDistribution.Sampler sampler = dis.createSampler(RandomSource.MT.create());
        int n = 1;
        for (int s : size) {
            n = n * s;
        }
        double[] samples = new double[n];
        for (int i = 0; i < n; i++) {
            samples[i] = sampler.sample();
        }
        int[] shape = size.stream().mapToInt(Integer::intValue).toArray();
        Array r = Array.factory(DataType.DOUBLE, shape, samples);
        return r;
    }

    /**
     * Random variates of given type.
     * @param dis Distribution.
     * @param n Size.
     * @return Result array.
     */
    public static Array rvs(MultivariateRealDistribution dis, int n) {
        MultivariateRealDistribution.Sampler sampler = dis.createSampler(RandomSource.MT.create());
        int dim = dis.getDimension();
        double[][] samples = new double[n][dim];
        for (int i = 0; i < n; i++) {
            samples[i] = sampler.sample();
        }
        double[] s = Arrays.stream(samples).flatMapToDouble(x -> Arrays.stream(x)).toArray();
        Array r = Array.factory(DataType.DOUBLE, new int[]{n, dim}, s);
        return r;
    }

    /**
     * Random variates of given type.
     * @param dis Distribution.
     * @param n Size.
     * @return Result array.
     */
    public static Array rvs(MultivariateNormalDistribution dis, int n) {
        MultivariateNormalDistribution.Sampler sampler = dis.createSampler(RandomSource.MT.create());
        int dim = dis.getDimension();
        double[][] samples = new double[n][dim];
        for (int i = 0; i < n; i++) {
            samples[i] = sampler.sample();
        }
        double[] s = Arrays.stream(samples).flatMapToDouble(x -> Arrays.stream(x)).toArray();
        Array r = Array.factory(DataType.DOUBLE, new int[]{n, dim}, s);
        return r;
    }

    /**
     * Random variates of given type.
     * @param dis Distribution.
     * @param n Size.
     * @return Result array.
     */
    public static Array rvs(MultivariateNormalDistribution dis, Array size) {
        MultivariateNormalDistribution.Sampler sampler = dis.createSampler(RandomSource.MT.create());
        int dim = dis.getDimension();
        int n = (int) size.getSize();
        double[][] samples = new double[n][dim];
        for (int i = 0; i < n; i++) {
            samples[i] = sampler.sample();
        }
        double[] s = Arrays.stream(samples).flatMapToDouble(x -> Arrays.stream(x)).toArray();
        Array r = Array.factory(DataType.DOUBLE, new int[]{n, dim}, s);
        return r;
    }
    
    /**
     * Probability density function at x
     * @param dis Distribution.
     * @param x X.
     * @return Probability density value.
     */
    public static double pdf(ContinuousDistribution dis, Number x){
        return dis.density(x.doubleValue());
    }
    
    /**
     * Probability density function at x
     * @param dis Distribution.
     * @param x X array.
     * @return Probability density array.
     */
    public static Array pdf(ContinuousDistribution dis, Array x){
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator iter = x.getIndexIterator();
        for (int i = 0; i < r.getSize(); i++){
            r.setDouble(i, dis.density(iter.getDoubleNext()));
        }
        
        return r;
    }
    
    /**
     * Natural logarithm probability density function at x
     * @param dis Distribution.
     * @param x X.
     * @return Log probability density value.
     */
    public static double logpdf(NormalDistribution dis, Number x){
        return dis.logDensity(x.doubleValue());
    }
    
    /**
     * Natural logarithm probability density function at x
     * @param dis Distribution.
     * @param x X array.
     * @return Result array.
     */
    public static Array logpdf(NormalDistribution dis, Array x){
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator iter = x.getIndexIterator();
        for (int i = 0; i < r.getSize(); i++){
            r.setDouble(i, dis.logDensity(iter.getDoubleNext()));
        }
        
        return r;
    }
    
    /**
     * Cumulative distribution function at x
     * @param dis Distribution.
     * @param x X.
     * @return Cumulative distribution value.
     */
    public static double cdf(ContinuousDistribution dis, Number x){
        return dis.cumulativeProbability(x.doubleValue());
    }
    
    /**
     * Cumulative distribution function at x.
     * @param dis Distribution.
     * @param x X array.
     * @return Result array.
     */
    public static Array cdf(ContinuousDistribution dis, Array x){
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator iter = x.getIndexIterator();
        for (int i = 0; i < r.getSize(); i++){
            r.setDouble(i, dis.cumulativeProbability(iter.getDoubleNext()));
        }
        
        return r;
    }
    
    /**
     * Probability mass function (PMF) for the distribution at x.
     * @param dis Distribution.
     * @param x X.
     * @return PMF value.
     */
    public static double pmf(ContinuousDistribution dis, Number x){
        return dis.probability(x.doubleValue(), x.doubleValue());
    }
    
    /**
     * Probability mass function (PMF) for the distribution at x.
     * @param dis Distribution.
     * @param x X array.
     * @return Result array.
     */
    public static Array pmf(ContinuousDistribution dis, Array x){
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator iter = x.getIndexIterator();
        double v;
        for (int i = 0; i < r.getSize(); i++){
            v = iter.getDoubleNext();
            r.setDouble(i, dis.probability(v, v));
        }
        
        return r;
    }
    
    /**
     * Percent point function (inverse of cdf) at q.
     * @param dis Distribution.
     * @param q Lower tail probability
     * @return PMF value.
     */
    public static double ppf(ContinuousDistribution dis, Number q){
        return dis.inverseCumulativeProbability(q.doubleValue());
    }
    
    /**
     * Percent point function (inverse of cdf) at q
     * @param dis Distribution.
     * @param q Q array.
     * @return Result array.
     */
    public static Array ppf(ContinuousDistribution dis, Array q){
        Array r = Array.factory(DataType.DOUBLE, q.getShape());
        IndexIterator iter = q.getIndexIterator();
        for (int i = 0; i < r.getSize(); i++){
            r.setDouble(i, dis.inverseCumulativeProbability(iter.getDoubleNext()));
        }
        
        return r;
    }
}
