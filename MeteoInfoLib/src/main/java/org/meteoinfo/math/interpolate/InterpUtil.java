/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.interpolate;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.NevilleInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.meteoinfo.data.ArrayUtil;
import org.meteoinfo.math.spatial.KDTree.Euclidean;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class InterpUtil {

    /**
     * Make linear interpolation function - PolynomialSplineFunction
     *
     * @param x X data
     * @param y Y data
     * @return Linear interpolation function
     */
    public static PolynomialSplineFunction linearInterpFunc(Array x, Array y) {
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray(y);
        LinearInterpolator li = new LinearInterpolator();
        PolynomialSplineFunction psf = li.interpolate(xd, yd);

        return psf;
    }

    /**
     * Make interpolation function
     *
     * @param x X data
     * @param y Y data
     * @param kind Specifies the kind of interpolation as a string (‘linear’,
     * 'spline').
     * @return Interpolation function
     */
    public static UnivariateFunction getInterpFunc(Array x, Array y, String kind) {
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray(y);
        UnivariateInterpolator li;
        switch (kind) {
            case "spline":
            case "cubic":
                li = new SplineInterpolator();
                break;
            case "akima":
                li = new AkimaSplineInterpolator();
                break;
            case "divided":
                li = new DividedDifferenceInterpolator();
                break;
            case "loess":
                li = new LoessInterpolator();
                break;
            case "neville":
                li = new NevilleInterpolator();
                break;
            default:
                li = new LinearInterpolator();
                break;
        }
        UnivariateFunction psf = li.interpolate(xd, yd);

        return psf;
    }

    /**
     * Make interpolation function for grid data
     *
     * @param x X data
     * @param y Y data
     * @param z Z data
     * @return Interpolation function
     */
    public static BivariateFunction getBiInterpFunc(Array x, Array y, Array z) {
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray(y);
        double[][] zd = (double[][]) ArrayUtil.copyToNDJavaArray(z);
        BivariateGridInterpolator li = new BicubicInterpolator();
        BivariateFunction func = li.interpolate(xd, yd, zd);

        return func;
    }

    /**
     * Compute the value of the function
     *
     * @param func The function
     * @param x Input data
     * @return Function value
     */
    public static Array evaluate(UnivariateFunction func, Array x) {
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, func.value(x.getDouble(i)));
        }

        return r;
    }

    /**
     * Compute the value of the function
     *
     * @param func The function
     * @param x Input data
     * @return Function value
     */
    public static double evaluate(UnivariateFunction func, Number x) {
        return func.value(x.doubleValue());
    }

    /**
     * Compute the value of the function
     *
     * @param func The function
     * @param x Input x data
     * @param y Input y data
     * @return Function value
     */
    public static Array evaluate(BivariateFunction func, Array x, Array y) {
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, func.value(x.getDouble(i), y.getDouble(i)));
        }

        return r;
    }

    /**
     * Compute the value of the function
     *
     * @param func The function
     * @param x Input x data
     * @param y Input y data
     * @return Function value
     */
    public static double evaluate(BivariateFunction func, Number x, Number y) {
        return func.value(x.doubleValue(), y.doubleValue());
    }

    /**
     * Cressman analysis
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param v_s scatter value array
     * @param X x array
     * @param Y y array
     * @param radList radii list
     * @return result grid data
     */
    public static Array cressman(List<Number> x_s, List<Number> y_s, Array v_s, List<Number> X, List<Number> Y,
            List<Number> radList) {
        int xNum = X.size();
        int yNum = Y.size();
        int pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yNum, xNum});
        int irad = radList.size();
        int i, j;

        //Loop through each stn report and convert stn lat/lon to grid coordinates
        double xMin = X.get(0).doubleValue();
        double xMax;
        double yMin = Y.get(0).doubleValue();
        double yMax;
        double xDelt = X.get(1).doubleValue() - X.get(0).doubleValue();
        double yDelt = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        double x, y;
        double sum;
        double[][] stationData = new double[pNum][5];
        for (i = 0; i < pNum; i++) {
            x = x_s.get(i).doubleValue();
            y = y_s.get(i).doubleValue();
            stationData[i][0] = x;
            stationData[i][1] = y;
            stationData[i][2] = v_s.getDouble(i);
            stationData[i][3] = (x - xMin) / xDelt;
            stationData[i][4] = (y - yMin) / yDelt;
        }

        //Construct K-D tree
        Euclidean<double[]> kdTree = new Euclidean<>(2);
        for (i = 0; i < pNum; i++) {
            if (!Double.isNaN(stationData[i][2]))
                kdTree.addPoint(new double[]{stationData[i][0], stationData[i][1]}, stationData[i]);
        }

        double HITOP = -999900000000000000000.0;
        double HIBOT = 999900000000000000000.0;
        double[][] TOP = new double[yNum][xNum];
        double[][] BOT = new double[yNum][xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                TOP[i][j] = HITOP;
                BOT[i][j] = HIBOT;
            }
        }

        //Initial grid values are average of station reports within the first radius
        double val, sx, sy, sxi, syi;
        double rad;
        int stNum;
        if (radList.size() > 0) {
            rad = radList.get(0).doubleValue();
        } else {
            rad = 4;
        }
        for (i = 0; i < yNum; i++) {
            y = Y.get(i).doubleValue();
            for (j = 0; j < xNum; j++) {
                x = X.get(j).doubleValue();
                stNum = 0;
                sum = 0;
                ArrayList<double[]> neighbours = kdTree.ballSearch(new double[]{x, y}, rad * rad);
                for (double[] station : neighbours) {
                    val = station[2];
                    sum += val;
                    stNum += 1;
                    if (TOP[i][j] < val) {
                        TOP[i][j] = val;
                    }
                    if (BOT[i][j] > val) {
                        BOT[i][j] = val;
                    }
                }
                if (stNum == 0) {
                    r.setDouble(i * xNum + j, Double.NaN);
                } else {
                    r.setDouble(i * xNum + j, sum / stNum);
                }
            }
        }

        //Perform the objective analysis
        for (int p = 0; p < irad; p++) {
            rad = radList.get(p).doubleValue();
            for (i = 0; i < yNum; i++) {
                y = Y.get(i).doubleValue();
                for (j = 0; j < xNum; j++) {
                    if (Double.isNaN(r.getDouble(i * xNum + j))) {
                        continue;
                    }

                    x = X.get(j).doubleValue();
                    sum = 0;
                    double wSum = 0;
                    ArrayList<double[]> neighbours = kdTree.ballSearch(new double[]{x, y}, rad * rad);
                    for (double[] station : neighbours) {
                        val = station[2];
                        sx = station[0];
                        sy = station[1];
                        sxi = station[3];
                        syi = station[4];
                        
                        if (sxi < 0 || sxi >= xNum - 1 || syi < 0 || syi >= yNum - 1) {
                            continue;
                        }
                        
                        double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));

                        int i1 = (int) syi;
                        int j1 = (int) sxi;
                        int i2 = i1 + 1;
                        int j2 = j1 + 1;
                        double a = r.getDouble(i1 * xNum + j1);
                        double b = r.getDouble(i1 * xNum + j2);
                        double c = r.getDouble(i2 * xNum + j1);
                        double d = r.getDouble(i2 * xNum + j2);
                        List<Double> dList = new ArrayList<>();
                        if (!Double.isNaN(a)) {
                            dList.add(a);
                        }
                        if (!Double.isNaN(b)) {
                            dList.add(b);
                        }
                        if (!Double.isNaN(c)) {
                            dList.add(c);
                        }
                        if (Double.isNaN(d)) {
                            dList.add(d);
                        }

                        double calVal;
                        if (dList.isEmpty()) {
                            continue;
                        } else if (dList.size() == 1) {
                            calVal = dList.get(0);
                        } else if (dList.size() <= 3) {
                            double aSum = 0;
                            for (double dd : dList) {
                                aSum += dd;
                            }
                            calVal = aSum / dList.size();
                        } else {
                            double x1val = a + (c - a) * (syi - i1);
                            double x2val = b + (d - b) * (syi - i1);
                            calVal = x1val + (x2val - x1val) * (sxi - j1);
                        }
                        double eVal = val - calVal;
                        double w = (rad * rad - dis * dis) / (rad * rad + dis * dis);
                        sum += eVal * w;
                        wSum += w;
                    }
                    if (wSum >= 0.000001) {
                        double aData = r.getDouble(i * xNum + j) + sum / wSum;
                        r.setDouble(i * xNum + j, Math.max(BOT[i][j], Math.min(TOP[i][j], aData)));
                    }
                }
            }
        }

        //Return
        return r;
    }

    /**
     * Barnes analysis
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param v_s scatter value array
     * @param X x array
     * @param Y y array
     * @param radList radii list
     * @param kappa A falloff parameter that controls the width of the Gaussian
     * function
     * @param gamma The smoothing parameter, is constrained to be between 0.2
     * and 1.0
     * @return result grid data
     */
    public static Array barnes(List<Number> x_s, List<Number> y_s, Array v_s, List<Number> X, List<Number> Y,
            List<Number> radList, double kappa, double gamma) {
        int xNum = X.size();
        int yNum = Y.size();
        int pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yNum, xNum});
        int irad = radList.size();
        int i, j;

        //Loop through each stn report and convert stn lat/lon to grid coordinates
        double xMin = X.get(0).doubleValue();
        double xMax;
        double yMin = Y.get(0).doubleValue();
        double yMax;
        double xDelt = X.get(1).doubleValue() - X.get(0).doubleValue();
        double yDelt = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        double x, y;
        double sum, wSum, w;
        double[][] stationData = new double[pNum][5];
        for (i = 0; i < pNum; i++) {
            x = x_s.get(i).doubleValue();
            y = y_s.get(i).doubleValue();
            stationData[i][0] = x;
            stationData[i][1] = y;
            stationData[i][2] = v_s.getDouble(i);
            stationData[i][3] = (x - xMin) / xDelt;
            stationData[i][4] = (y - yMin) / yDelt;
        }

        //Construct K-D tree
        Euclidean<double[]> kdTree = new Euclidean<>(2);
        for (i = 0; i < pNum; i++) {
            if (!Double.isNaN(stationData[i][2]))
                kdTree.addPoint(new double[]{stationData[i][0], stationData[i][1]}, stationData[i]);
        }

        double HITOP = -999900000000000000000.0;
        double HIBOT = 999900000000000000000.0;
        double[][] TOP = new double[yNum][xNum];
        double[][] BOT = new double[yNum][xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                TOP[i][j] = HITOP;
                BOT[i][j] = HIBOT;
            }
        }

        //Initial grid values are average of station reports within the first radius
        double val, sx, sy, sxi, syi;
        double rad;
        int stNum;
        if (radList.size() > 0) {
            rad = radList.get(0).doubleValue();
        } else {
            rad = 4;
        }
        for (i = 0; i < yNum; i++) {
            y = Y.get(i).doubleValue();
            for (j = 0; j < xNum; j++) {
                x = X.get(j).doubleValue();
                stNum = 0;
                sum = 0;
                wSum = 0;
                ArrayList<double[]> neighbours = kdTree.ballSearch(new double[]{x, y}, rad * rad);
                for (double[] station : neighbours) {
                    val = station[2];
                    sx = station[0];
                    sy = station[1];
                    double dis = Math.pow(sx - x, 2) + Math.pow(sy - y, 2);
                    w = Math.exp(-dis / (4 * kappa));
                    wSum += w;
                    sum += w * val;
                    stNum += 1;
                    if (TOP[i][j] < val) {
                        TOP[i][j] = val;
                    }
                    if (BOT[i][j] > val) {
                        BOT[i][j] = val;
                    }
                }
                if (stNum == 0) {
                    r.setDouble(i * xNum + j, Double.NaN);
                } else {
                    r.setDouble(i * xNum + j, sum / stNum);
                }
            }
        }

        //Perform the objective analysis
        for (int p = 0; p < irad; p++) {
            rad = radList.get(p).doubleValue();
            for (i = 0; i < yNum; i++) {
                y = Y.get(i).doubleValue();
                for (j = 0; j < xNum; j++) {
                    if (Double.isNaN(r.getDouble(i * xNum + j))) {
                        continue;
                    }

                    x = X.get(j).doubleValue();
                    sum = 0;
                    wSum = 0;
                    ArrayList<double[]> neighbours = kdTree.ballSearch(new double[]{x, y}, rad * rad);
                    for (double[] station : neighbours) {
                        val = station[2];
                        sx = station[0];
                        sy = station[1];
                        sxi = station[3];
                        syi = station[4];
                        
                        if (sxi < 0 || sxi >= xNum - 1 || syi < 0 || syi >= yNum - 1) {
                            continue;
                        }
                        
                        double dis = Math.pow(sx - x, 2) + Math.pow(sy - y, 2);
                        int i1 = (int) syi;
                        int j1 = (int) sxi;
                        int i2 = i1 + 1;
                        int j2 = j1 + 1;
                        double a = r.getDouble(i1 * xNum + j1);
                        double b = r.getDouble(i1 * xNum + j2);
                        double c = r.getDouble(i2 * xNum + j1);
                        double d = r.getDouble(i2 * xNum + j2);
                        List<Double> dList = new ArrayList<>();
                        if (!Double.isNaN(a)) {
                            dList.add(a);
                        }
                        if (!Double.isNaN(b)) {
                            dList.add(b);
                        }
                        if (!Double.isNaN(c)) {
                            dList.add(c);
                        }
                        if (Double.isNaN(d)) {
                            dList.add(d);
                        }

                        double calVal;
                        if (dList.isEmpty()) {
                            continue;
                        } else if (dList.size() == 1) {
                            calVal = dList.get(0);
                        } else if (dList.size() <= 3) {
                            double aSum = 0;
                            for (double dd : dList) {
                                aSum += dd;
                            }
                            calVal = aSum / dList.size();
                        } else {
                            double x1val = a + (c - a) * (syi - i1);
                            double x2val = b + (d - b) * (syi - i1);
                            calVal = x1val + (x2val - x1val) * (sxi - j1);
                        }
                        double eVal = val - calVal;
                        w = Math.exp(-dis / (4 * kappa * gamma));
                        sum += eVal * w;
                        wSum += w;
                    }
                    if (wSum >= 0.000001) {
                        double aData = r.getDouble(i * xNum + j) + sum / wSum;
                        r.setDouble(i * xNum + j, Math.max(BOT[i][j], Math.min(TOP[i][j], aData)));
                    }
                }
            }
        }

        //Return
        return r;
    }

    /**
     * Barnes analysis
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param v_s scatter value array
     * @param X x array
     * @param Y y array
     * @param kappa A falloff parameter that controls the width of the Gaussian
     * function
     * @param gamma The smoothing parameter, is constrained to be between 0.2
     * and 1.0
     * @return result grid data
     */
    public static Array barnes(List<Number> x_s, List<Number> y_s, Array v_s, List<Number> X, List<Number> Y,
            double kappa, double gamma) {
        int xNum = X.size();
        int yNum = Y.size();
        int pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yNum, xNum});
        int i, j;

        //Loop through each stn report and convert stn lat/lon to grid coordinates
        double xMin = X.get(0).doubleValue();
        double xMax = X.get(xNum - 1).doubleValue();
        double yMin = Y.get(0).doubleValue();
        double yMax = Y.get(yNum - 1).doubleValue();
        double xDelt = X.get(1).doubleValue() - X.get(0).doubleValue();
        double yDelt = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        double x, y;
        double sum;
        double[][] stationData = new double[pNum][5];
        for (i = 0; i < pNum; i++) {
            x = x_s.get(i).doubleValue();
            y = y_s.get(i).doubleValue();
            stationData[i][0] = x;
            stationData[i][1] = y;
            stationData[i][2] = v_s.getDouble(i);
            stationData[i][3] = (x - xMin) / xDelt;
            stationData[i][4] = (y - yMin) / yDelt;
        }

        //First guess values
        double val, sx, sy, sxi, syi;
        for (i = 0; i < yNum; i++) {
            y = Y.get(i).doubleValue();
            for (j = 0; j < xNum; j++) {
                x = X.get(j).doubleValue();
                sum = 0;
                double wSum = 0;
                double w;
                for (double[] station : stationData) {
                    val = station[2];
                    sx = station[0];
                    sy = station[1];
                    if (Double.isNaN(val) || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                        continue;
                    }

                    double dis = Math.pow(sx - x, 2) + Math.pow(sy - y, 2);
                    w = Math.exp(-dis / (4 * kappa));
                    wSum += w;
                    sum += w * val;
                }
                r.setDouble(i * xNum + j, sum / wSum);
            }
        }

        //Second pass
        for (i = 0; i < yNum; i++) {
            y = Y.get(i).doubleValue();
            for (j = 0; j < xNum; j++) {
                x = X.get(j).doubleValue();
                sum = 0;
                double wSum = 0;
                double w;
                for (double[] station : stationData) {
                    val = station[2];
                    sx = station[0];
                    sy = station[1];
                    sxi = station[3];
                    syi = station[4];
                    if (Double.isNaN(val) || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                        continue;
                    }

                    int i1 = (int) syi;
                    int j1 = (int) sxi;
                    int i2 = i1 + 1;
                    int j2 = j1 + 1;
                    double a = r.getDouble(i1 * xNum + j1);
                    double b = r.getDouble(i1 * xNum + j2);
                    double c = r.getDouble(i2 * xNum + j1);
                    double d = r.getDouble(i2 * xNum + j2);
                    List<Double> dList = new ArrayList<>();
                    if (!Double.isNaN(a)) {
                        dList.add(a);
                    }
                    if (!Double.isNaN(b)) {
                        dList.add(b);
                    }
                    if (!Double.isNaN(c)) {
                        dList.add(c);
                    }
                    if (Double.isNaN(d)) {
                        dList.add(d);
                    }

                    double calVal;
                    if (dList.isEmpty()) {
                        continue;
                    } else if (dList.size() == 1) {
                        calVal = dList.get(0);
                    } else if (dList.size() <= 3) {
                        double aSum = 0;
                        for (double dd : dList) {
                            aSum += dd;
                        }
                        calVal = aSum / dList.size();
                    } else {
                        double x1val = a + (c - a) * (syi - i1);
                        double x2val = b + (d - b) * (syi - i1);
                        calVal = x1val + (x2val - x1val) * (sxi - j1);
                    }
                    double eVal = val - calVal;

                    double dis = Math.pow(sx - x, 2) + Math.pow(sy - y, 2);
                    w = Math.exp(-dis / (4 * kappa * gamma));
                    wSum += w;
                    sum += w * eVal;
                }
                r.setDouble(i * xNum + j, r.getDouble(i * xNum + j) + sum / wSum);
            }
        }

        //Return
        return r;
    }
}
