/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.stats;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math4.legacy.linear.Array2DRowRealMatrix;
import org.apache.commons.math4.legacy.linear.RealMatrix;
import org.apache.commons.math4.legacy.stat.StatUtils;
import org.apache.commons.math4.legacy.stat.correlation.Covariance;
import org.apache.commons.math4.legacy.stat.correlation.KendallsCorrelation;
import org.apache.commons.math4.legacy.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math4.legacy.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math4.legacy.stat.inference.InferenceTestUtils;
import org.apache.commons.math4.legacy.stat.inference.TTest;
import org.apache.commons.math4.legacy.stat.regression.OLSMultipleLinearRegression;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.math.special.Erf;

/**
 *
 * @author Yaqiang Wang
 */
public class StatsUtil {

    /**
     * Computes covariance of two arrays.
     *
     * @param x X data
     * @param y Y data
     * @param bias If true, returned value will be bias-corrected
     * @return The covariance
     */
    public static double covariance(Array x, Array y, boolean bias){
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray_Double(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray_Double(y);
        double r = new Covariance().covariance(xd, yd, bias);
        return r;
    }
    
    /**
     * Computes covariances for pairs of arrays or columns of a matrix.
     *
     * @param x X data
     * @param y Y data
     * @param bias If true, returned value will be bias-corrected
     * @return The covariance matrix
     */
    public static Array cov(Array x, Array y, boolean bias) {
        x = x.copyIfView();
        y = y.copyIfView();

        int m = x.getShape()[0];
        int n = 1;
        if (x.getRank() == 2)
            n = x.getShape()[1];
        double[][] aa = new double[m][n * 2];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n * 2; j++) {
                if (j < n) {
                    aa[i][j] = x.getDouble(i * n + j);
                } else {
                    aa[i][j] = y.getDouble(i * n + j - n);
                }
            }
        }
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        Covariance cov = new Covariance(matrix, bias);
        RealMatrix mcov = cov.getCovarianceMatrix();
        m = mcov.getColumnDimension();
        n = mcov.getRowDimension();
        Array r = Array.factory(DataType.DOUBLE, new int[]{m, n});
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                r.setDouble(i * n + j, mcov.getEntry(i, j));
            }
        }

        return r;
    }

    /**
     * Computes covariances for columns of a matrix.
     * @param a Matrix data
     * @param bias If true, returned value will be bias-corrected
     * @return Covariant matrix or value
     */
    public static Object cov(Array a, boolean bias) {
        if (a.getRank() == 1) {
            double[] ad = (double[]) ArrayUtil.copyToNDJavaArray_Double(a);
            Covariance cov = new Covariance();
            return cov.covariance(ad, ad);
        } else {
            double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray_Double(a);
            RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
            Covariance cov = new Covariance(matrix, bias);
            RealMatrix mcov = cov.getCovarianceMatrix();
            int m = mcov.getColumnDimension();
            int n = mcov.getRowDimension();
            Array r = Array.factory(DataType.DOUBLE, new int[]{m, n});
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    r.setDouble(i * n + j, mcov.getEntry(i, j));
                }
            }

            return r;
        }
    }

    /**
     * Calculates Kendall's tau, a correlation measure for ordinal data.
     *
     * @param x X data
     * @param y Y data
     * @return Kendall's tau correlation.
     */
    public static double[] kendalltau(Array x, Array y) {
        x = x.copyIfView();
        y = y.copyIfView();

        int is = 0, n2 = 0, n1 = 0, n = (int) x.getSize();
        double aa, a2, a1;
        for (int j = 0; j < n - 1; j++) {
            for (int k = j + 1; k < n; k++) {
                a1 = x.getDouble(j) - x.getDouble(k);
                a2 = y.getDouble(j) - y.getDouble(k);
                aa = a1 * a2;
                if (aa != 0.0) {
                    ++n1;
                    ++n2;
                    if (aa > 0)
                        ++is;
                    else
                        --is;

                } else {
                    if (a1 != 0.0) ++n1;
                    if (a2 != 0.0) ++n2;
                }
            }
        }

        double tau = is / (Math.sqrt(n1) * Math.sqrt(n2));

        // Kendall test is non-parametric as it does not rely on any
        // assumptions on the distributions of X or Y or the distribution
        // of (X,Y).

        // Under the null hypothesis of independence of X and Y, the sampling
        // distribution of tau has an expected value of zero. The precise
        // distribution cannot be characterized in terms of common distributions,
        // but may be calculated exactly for small samples. For larger samples,
        // it is common to use an approximation to the normal distribution,
        // with mean zero and variance sqrt(2(2n+5)/9n(n-1)).
        double var = (4.0 * n + 10.0) / (9.0 * n * (n - 1.0));
        double z = tau / Math.sqrt(var);
        double pvalue = Erf.erfcc(Math.abs(z) / 1.4142136);

        return new double[]{tau, pvalue};
    }

    /**
     * Calculates Kendall's tau, a correlation measure for ordinal data.
     *
     * @param x X data
     * @param y Y data
     * @return Kendall's tau correlation.
     */
    public static double kendalltau_bak(Array x, Array y) {
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray_Double(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray_Double(y);
        KendallsCorrelation kc = new KendallsCorrelation();
        double r = kc.correlation(xd, yd);
        return r;
    }
    
    /**
     * Calculates a Pearson correlation coefficient.
     *
     * @param x X data
     * @param y Y data
     * @return Pearson correlation and p-value.
     */
    public static double[] pearsonr(Array x, Array y) {
        x = x.copyIfView();
        y = y.copyIfView();

        if (ArrayMath.containsNaN(x) || ArrayMath.containsNaN(y)) {
            Array[] xy = ArrayMath.removeNaN(x, y);
            if (xy == null) {
                return new double[]{Double.NaN, Double.NaN};
            }
            
            x = xy[0];
            y = xy[1];
        }
        
        if (MAMath.isEqual(x, y)) {
            return new double[]{1, 0};
        }
        
        int m = (int)x.getSize();
        int n = 1;
        double[][] aa = new double[m][n * 2];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n * 2; j++) {
                if (j < n) {
                    aa[i][j] = x.getDouble(i * n + j);
                } else {
                    aa[i][j] = y.getDouble(i * n + j - n);
                }
            }
        }
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        PearsonsCorrelation pc = new PearsonsCorrelation(matrix);
        double r = pc.getCorrelationMatrix().getEntry(0, 1);
        double pvalue = pc.getCorrelationPValues().getEntry(0, 1);
        return new double[]{r, pvalue};
    }
    
    /**
     * Calculates a Pearson correlation coefficient.
     *
     * @param x X data
     * @param y Y data
     * @param axis Special axis for calculation
     * @return Pearson correlation and p-value.
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static Array[] pearsonr(Array x, Array y, int axis) throws InvalidRangeException {
        int[] dataShape = x.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        Array pv = Array.factory(DataType.DOUBLE, shape);
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            Array xx = ArrayMath.section(x, ranges);
            Array yy = ArrayMath.section(y, ranges);
            double[] rp = pearsonr(xx, yy);
            r.setDouble(i, rp[0]);
            pv.setDouble(i, rp[1]);
            indexr.incr();
        }
        
        return new Array[]{r, pv};
    }
    
    /**
     * Computes Spearman's rank correlation for pairs of arrays or columns of a matrix.
     *
     * @param x X data
     * @param y Y data
     * @return Spearman's rank correlation
     */
    public static double[] spearmanr(Array x, Array y) {
        x = x.copyIfView();
        y = y.copyIfView();

        int m = x.getShape()[0];
        int n = 1;
        if (x.getRank() == 2)
            n = x.getShape()[1];
        double[][] aa = new double[m][n * 2];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n * 2; j++) {
                if (j < n) {
                    aa[i][j] = x.getDouble(i * n + j);
                } else {
                    aa[i][j] = y.getDouble(i * n + j - n);
                }
            }
        }
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        SpearmansCorrelation cov = new SpearmansCorrelation(matrix);
        /*RealMatrix mcov = cov.getCorrelationMatrix();
        m = mcov.getColumnDimension();
        n = mcov.getRowDimension();
        Array r = Array.factory(DataType.DOUBLE, new int[]{m, n});
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                r.setDouble(i * n + j, mcov.getEntry(i, j));
            }
        }*/
        double r = cov.getCorrelationMatrix().getEntry(0, 1);
        double pValue =cov.getRankCorrelation().getCorrelationPValues().getEntry(0, 1);

        return new double[]{r, pValue};
    }

    /**
     * Computes Spearman's rank correlation for columns of a matrix.
     * @param a Matrix data
     * @return Spearman's rank correlation
     */
    public static Object spearmanr(Array a) {
        if (a.getRank() == 1) {
            double[] ad = (double[]) ArrayUtil.copyToNDJavaArray_Double(a);
            Covariance cov = new Covariance();
            return cov.covariance(ad, ad);
        } else {
            double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray_Double(a);
            RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
            SpearmansCorrelation cov = new SpearmansCorrelation(matrix);
            RealMatrix mcov = cov.getCorrelationMatrix();
            int m = mcov.getColumnDimension();
            int n = mcov.getRowDimension();
            Array r = Array.factory(DataType.DOUBLE, new int[]{m, n});
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    r.setDouble(i * n + j, mcov.getEntry(i, j));
                }
            }

            return r;
        }
    }
    
    /**
     * Implements ordinary least squares (OLS) to estimate the parameters of a 
     * multiple linear regression model.
     * @param y Y sample data - one dimension array
     * @param x X sample data - two dimension array
     * @return Estimated regression parameters and residuals
     */
    public static Object[] multipleLineRegress_OLS(Array y, Array x) {
        return multipleLineRegress_OLS(y, x, false);
    }
    
    /**
     * Implements ordinary least squares (OLS) to estimate the parameters of a 
     * multiple linear regression model.
     * @param y Y sample data - one dimension array
     * @param x X sample data - two dimension array
     * @param noIntercept No intercept
     * @return Estimated regression parameters and residuals
     */
    public static Object[] multipleLineRegress_OLS(Array y, Array x, boolean noIntercept) {
        OLSMultipleLinearRegression regression = new MIOLSMultipleLinearRegression();
        regression.setNoIntercept(noIntercept);
        y = y.reduce();
        double[] yy = (double[])ArrayUtil.copyToNDJavaArray_Double(y);
        double[][] xx = (double[][])ArrayUtil.copyToNDJavaArray_Double(x);
        regression.newSampleData(yy, xx);
        double[] para = regression.estimateRegressionParameters();
        double[] residuals = regression.estimateResiduals();
        int k = para.length;
        int n = residuals.length;
        Array aPara = Array.factory(DataType.DOUBLE, new int[]{k});
        Array aResiduals = Array.factory(DataType.DOUBLE, new int[]{n});
        for (int i = 0; i < k; i++){
            aPara.setDouble(i, para[i]);
        }
        for (int i = 0; i < k; i++){
            aResiduals.setDouble(i, residuals[i]);
        }
        double r2 = regression.calculateRSquared();
        
        return new Object[]{aPara, aResiduals, r2};
    }
    
    /**
     * Returns an estimate of the pth percentile of the values in the array.
     * @param a Input array
     * @param p The percentile value to compute
     * @return The pth percentile
     */
    public static double percentile(Array a, double p){
        double[] v = (double[])a.get1DJavaArray(Double.class);
        double r = StatUtils.percentile(v, p);
        return r;
    }
    
    /**
     * Returns an estimate of the pth percentile of the values in the array along an axis.
     * @param a Input array
     * @param p The percentile value to compute
     * @param axis The axis
     * @return The pth percentile
     * @throws InvalidRangeException 
     */
    public static Array percentile(Array a, double p, int axis) throws InvalidRangeException{
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            Array aa = ArrayMath.section(a, ranges);
            double[] v = (double[])aa.get1DJavaArray(Double.class);
            double q = StatUtils.percentile(v, p);
            r.setDouble(i, q);
            indexr.incr();
        }

        return r;
    }
    
    /**
     * One sample t test
     * @param a Input data
     * @param mu Expected value in null hypothesis
     * @return t_statistic and p_value
     */
    public static double[] tTestOneSample(Array a, double mu){
        double[] ad = (double[]) a.get1DJavaArray(double.class);
        double s = InferenceTestUtils.t(mu, ad);
        double p = InferenceTestUtils.tTest(mu, ad);
        
        return new double[]{s, p};
    }

    /**
     * One sample t test
     *
     * @param a Sample a
     * @param mu Expected value in null hypothesis
     * @param axis Axis
     * @return t_statistic and p_value
     */
    public static Array[] tTestOneSample(Array a, double mu, int axis) throws InvalidRangeException {
        int[] aShape = a.getShape();
        int[] shape = new int[aShape.length - 1];
        int idx;
        for (int i = 0; i < aShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = aShape[i];
        }
        Array tStatistic = Array.factory(DataType.DOUBLE, shape);
        Array pValue = Array.factory(DataType.DOUBLE, shape);
        Index indexr = tStatistic.getIndex();
        int[] current;
        for (int i = 0; i < tStatistic.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> aRanges = new ArrayList<>();
            for (int j = 0; j < aShape.length; j++) {
                if (j == axis) {
                    aRanges.add(new Range(0, aShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    aRanges.add(new Range(current[idx], current[idx], 1));
                }
            }
            Array xx = ArrayMath.section(a, aRanges);
            double[] rp = tTestOneSample(xx, mu);
            tStatistic.setDouble(i, rp[0]);
            pValue.setDouble(i, rp[1]);
            indexr.incr();
        }

        return new Array[]{tStatistic, pValue};
    }

    /**
     * One sample t test
     *
     * @param a Sample a
     * @param mu Expected value in null hypothesis
     * @param axis Axis
     * @return t_statistic and p_value
     */
    public static Array[] tTestOneSample(Array a, Array mu, int axis) throws InvalidRangeException {
        mu = mu.copyIfView();
        int[] aShape = a.getShape();
        int[] shape = new int[aShape.length - 1];
        int idx;
        for (int i = 0; i < aShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = aShape[i];
        }
        Array tStatistic = Array.factory(DataType.DOUBLE, shape);
        Array pValue = Array.factory(DataType.DOUBLE, shape);
        Index indexr = tStatistic.getIndex();
        int[] current;
        for (int i = 0; i < tStatistic.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> aRanges = new ArrayList<>();
            for (int j = 0; j < aShape.length; j++) {
                if (j == axis) {
                    aRanges.add(new Range(0, aShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    aRanges.add(new Range(current[idx], current[idx], 1));
                }
            }
            Array xx = ArrayMath.section(a, aRanges);
            double[] rp = tTestOneSample(xx, mu.getDouble(i));
            tStatistic.setDouble(i, rp[0]);
            pValue.setDouble(i, rp[1]);
            indexr.incr();
        }

        return new Array[]{tStatistic, pValue};
    }
    
    /**
     * unpaired, two-sided, two-sample t-test.
     * 
     * @param a Sample a
     * @param b Sample b
     * @return t_statistic and p_value
     */
    public static double[] tTest(Array a, Array b) {
        double[] ad = (double[]) a.get1DJavaArray(double.class);
        double[] bd = (double[]) b.get1DJavaArray(double.class);
        double s = InferenceTestUtils.t(ad, bd);
        double p = InferenceTestUtils.tTest(ad, bd);
        
        return new double[]{s, p};
    }

    /**
     * unpaired, two-sided, two-sample t-test.
     *
     * @param a Sample a
     * @param b Sample b
     * @param equalVariance Equal variance or not
     * @return t_statistic and p_value
     */
    public static double[] tTest(Array a, Array b, boolean equalVariance) {
        double[] ad = (double[]) a.get1DJavaArray(double.class);
        double[] bd = (double[]) b.get1DJavaArray(double.class);
        TTest tTest = new TTest();
        double tStatistic, pValue;
        if (equalVariance) {
            tStatistic = tTest.homoscedasticT(ad, bd);
            pValue = tTest.homoscedasticTTest(ad, bd);
        } else {
            // （Welch's t-test）
            tStatistic = tTest.t(ad, bd);
            pValue = tTest.tTest(ad, bd);
        }

        return new double[]{tStatistic, pValue};
    }

    /**
     * unpaired, two-sided, two-sample t-test.
     *
     * @param a Sample a
     * @param b Sample b
     * @param axis Axis
     * @param equalVariance Equal variance or not
     * @return t_statistic and p_value
     */
    public static Array[] tTest(Array a, Array b, int axis, boolean equalVariance) throws InvalidRangeException {
        int[] aShape = a.getShape();
        int[] bShape = b.getShape();
        int[] shape = new int[aShape.length - 1];
        int idx;
        for (int i = 0; i < aShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = aShape[i];
        }
        Array tStatistic = Array.factory(DataType.DOUBLE, shape);
        Array pValue = Array.factory(DataType.DOUBLE, shape);
        Index indexr = tStatistic.getIndex();
        int[] current;
        for (int i = 0; i < tStatistic.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> aRanges = new ArrayList<>();
            List<Range> bRanges = new ArrayList<>();
            for (int j = 0; j < aShape.length; j++) {
                if (j == axis) {
                    aRanges.add(new Range(0, aShape[j] - 1, 1));
                    bRanges.add(new Range(0, bShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    aRanges.add(new Range(current[idx], current[idx], 1));
                    bRanges.add(new Range(current[idx], current[idx], 1));
                }
            }
            Array xx = ArrayMath.section(a, aRanges);
            Array yy = ArrayMath.section(b, bRanges);
            double[] rp = tTest(xx, yy, equalVariance);
            tStatistic.setDouble(i, rp[0]);
            pValue.setDouble(i, rp[1]);
            indexr.incr();
        }

        return new Array[]{tStatistic, pValue};
    }
    
    /**
     * Paired test evaluating the null hypothesis that the mean difference 
     * between corresponding (paired) elements of the double[] arrays sample1 
     * and sample2 is zero.
     * 
     * @param a Sample a
     * @param b Sample b
     * @return t_statistic and p_value
     */
    public static double[] pairedTTest(Array a, Array b) {
        double[] ad = (double[]) a.get1DJavaArray(double.class);
        double[] bd = (double[]) b.get1DJavaArray(double.class);
        double s = InferenceTestUtils.pairedT(ad, bd);
        double p = InferenceTestUtils.pairedTTest(ad, bd);
        
        return new double[]{s, p};
    }

    /**
     * Paired test evaluating the null hypothesis that the mean difference
     * between corresponding (paired) elements of the double[] arrays sample1
     * and sample2 is zero.
     *
     * @param a Sample a
     * @param b Sample b
     * @param axis Axis
     * @return t_statistic and p_value
     */
    public static Array[] pairedTTest(Array a, Array b, int axis) throws InvalidRangeException {
        int[] aShape = a.getShape();
        int[] bShape = b.getShape();
        int[] shape = new int[aShape.length - 1];
        int idx;
        for (int i = 0; i < aShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = aShape[i];
        }
        Array tStatistic = Array.factory(DataType.DOUBLE, shape);
        Array pValue = Array.factory(DataType.DOUBLE, shape);
        Index indexr = tStatistic.getIndex();
        int[] current;
        for (int i = 0; i < tStatistic.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> aRanges = new ArrayList<>();
            List<Range> bRanges = new ArrayList<>();
            for (int j = 0; j < aShape.length; j++) {
                if (j == axis) {
                    aRanges.add(new Range(0, aShape[j] - 1, 1));
                    bRanges.add(new Range(0, bShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    aRanges.add(new Range(current[idx], current[idx], 1));
                    bRanges.add(new Range(current[idx], current[idx], 1));
                }
            }
            Array xx = ArrayMath.section(a, aRanges);
            Array yy = ArrayMath.section(b, bRanges);
            double[] rp = pairedTTest(xx, yy);
            tStatistic.setDouble(i, rp[0]);
            pValue.setDouble(i, rp[1]);
            indexr.incr();
        }

        return new Array[]{tStatistic, pValue};
    }
    
    /**
     * Chi-square test
     * 
     * @param e Expected.
     * @param o Observed.
     * @return Chi-square_statistic and p_value
     */
    public static double[] chiSquareTest(Array e, Array o) {
        double[] ed = (double[]) e.get1DJavaArray(double.class);
        long[] od = (long[]) o.get1DJavaArray(double.class);
        double s = InferenceTestUtils.chiSquare(ed, od);
        double p = InferenceTestUtils.chiSquareTest(ed, od);
        
        return new double[]{s, p};
    }
    
    /**
     * Chi-square test of independence
     * 
     * @param o Observed.
     * @return Chi-square_statistic and p_value
     */
    public static double[] chiSquareTest(Array o) {
        long[][] od = (long[][]) ArrayUtil.copyToNDJavaArray_Long(o);
        double s = InferenceTestUtils.chiSquare(od);
        double p = InferenceTestUtils.chiSquareTest(od);
        
        return new double[]{s, p};
    }

    /**
     * Calculate Jenks breaks
     * @param a The input data array
     * @param nClass Number of classes
     * @return Jenks breaks value array
     */
    public static Array jenksBreaks(Array a, int nClass) {
        Jenks jenks = new Jenks();
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            jenks.addValue(iterA.getDoubleNext());
        }

        Jenks.Breaks breaks = jenks.computeBreaks(nClass);
        Array r = breaks.getClassValues();

        return r;
    }

    /**
     * Calculate Jenks breaks
     * @param a The input data array
     * @param nClass Number of classes
     * @return Jenks breaks value array and gvf value
     */
    public static Object[] jenksBreaksGvf(Array a, int nClass) {
        Jenks jenks = new Jenks();
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            jenks.addValue(iterA.getDoubleNext());
        }

        Jenks.Breaks breaks = jenks.computeBreaks(nClass);
        Array r = breaks.getClassValues();

        return new Object[]{r, breaks.gvf()};
    }
}
