/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.distribution;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;

/**
 *
 * @author wyq
 */
public class DistributionUtil {
    /**
     * Random variates of given type.
     * @param dis Distribution.
     * @param n Size.
     * @return Result array.
     */
    public static Array rvs(RealDistribution dis, int n){
        double[] samples = dis.sample(n);
        Array r = Array.factory(DataType.DOUBLE, new int[]{n}, samples);        
        return r;
    }
    
    /**
     * Probability density function at x
     * @param dis Distribution.
     * @param x X.
     * @return Probability density value.
     */
    public static double pdf(RealDistribution dis, Number x){
        return dis.density(x.doubleValue());
    }
    
    /**
     * Probability density function at x
     * @param dis Distribution.
     * @param x X array.
     * @return Probability density array.
     */
    public static Array pdf(RealDistribution dis, Array x){
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
    public static double cdf(RealDistribution dis, Number x){
        return dis.cumulativeProbability(x.doubleValue());
    }
    
    /**
     * Cumulative distribution function at x.
     * @param dis Distribution.
     * @param x X array.
     * @return Result array.
     */
    public static Array cdf(RealDistribution dis, Array x){
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
    public static double pmf(RealDistribution dis, Number x){
        return dis.probability(x.doubleValue());
    }
    
    /**
     * Probability mass function (PMF) for the distribution at x.
     * @param dis Distribution.
     * @param x X array.
     * @return Result array.
     */
    public static Array pmf(RealDistribution dis, Array x){
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator iter = x.getIndexIterator();
        for (int i = 0; i < r.getSize(); i++){
            r.setDouble(i, dis.probability(iter.getDoubleNext()));
        }
        
        return r;
    }
    
    /**
     * Percent point function (inverse of cdf) at q.
     * @param dis Distribution.
     * @param q Lower tail probability
     * @return PMF value.
     */
    public static double ppf(RealDistribution dis, Number q){
        return dis.inverseCumulativeProbability(q.doubleValue());
    }
    
    /**
     * Percent point function (inverse of cdf) at q
     * @param dis Distribution.
     * @param q Q array.
     * @return Result array.
     */
    public static Array ppf(RealDistribution dis, Array q){
        Array r = Array.factory(DataType.DOUBLE, q.getShape());
        IndexIterator iter = q.getIndexIterator();
        for (int i = 0; i < r.getSize(); i++){
            r.setDouble(i, dis.inverseCumulativeProbability(iter.getDoubleNext()));
        }
        
        return r;
    }
}
