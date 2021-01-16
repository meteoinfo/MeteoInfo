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
import org.apache.commons.math3.analysis.interpolation.*;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.PointD;
import org.meteoinfo.math.ArrayUtil;
import org.meteoinfo.math.spatial.KDTree;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.shape.PolygonShape;

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
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray_Double(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray_Double(y);
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
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray_Double(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray_Double(y);
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
     * @param kind Specifies the kind of interpolation as a string.
     * @return Interpolation function
     */
    public static BivariateFunction getBiInterpFunc(Array x, Array y, Array z, String kind) {
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray_Double(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray_Double(y);
        double[][] zd = (double[][]) ArrayUtil.copyToNDJavaArray_Double(z);
        BivariateGridInterpolator li;
        switch (kind) {
            case "spline":
                li = new PiecewiseBicubicSplineInterpolator();
                break;
            default:
                li = new BicubicInterpolator();
                break;
        }
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
        IndexIterator xIter = x.getIndexIterator();
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, func.value(xIter.getDoubleNext()));
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
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, func.value(xIter.getDoubleNext(), yIter.getDoubleNext()));
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
        v_s = v_s.copyIfView();

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
        KDTree.Euclidean<double[]> kdTree = new KDTree.Euclidean<>(2);
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
        v_s = v_s.copyIfView();

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
        KDTree.Euclidean<double[]> kdTree = new KDTree.Euclidean<>(2);
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
        v_s = v_s.copyIfView();

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

    /**
     * Interpolate with inside method - The grid cell value is the sum value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param centerPoint If the grid point is center or border
     * @return grid data
     */
    public static Array interpolation_Inside_Sum(Array x_s, Array y_s, Array a, Array X, Array Y,
                                                  boolean centerPoint) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        int rowNum, colNum, pNum;
        colNum = (int) X.getSize();
        rowNum = (int) Y.getSize();
        pNum = (int) x_s.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.getDouble(1) - X.getDouble(0);
        double dY = Y.getDouble(1) - Y.getDouble(0);
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v, sx, sy, ex, ey;
        if (centerPoint) {
            sx = X.getDouble(0) - dX * 0.5;
            sy = Y.getDouble(0) - dY * 0.5;
            ex = X.getDouble(colNum - 1) + dX * 0.5;
            ey = Y.getDouble(rowNum - 1) + dY * 0.5;
        } else {
            sx = X.getDouble(0);
            sy = Y.getDouble(0);
            ex = X.getDouble(colNum - 1);
            ey = Y.getDouble(rowNum - 1);
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, 0.0);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (Double.isNaN(v)) {
                continue;
            }

            x = x_s.getDouble(p);
            y = y_s.getDouble(p);
            if (x < sx || x > ex) {
                continue;
            }
            if (y < sy || y > ey) {
                continue;
            }

            int j = (int) ((x - sx) / dX);
            int i = (int) ((y - sy) / dY);
            if (i >= rowNum)
                i = rowNum - 1;
            if (j >= colNum)
                j = colNum - 1;
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) + v);
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0) {
                    r.setDouble(i * colNum + j, Double.NaN);
                }
            }
        }

        return r;
    }


    /**
     * Interpolate with inside method - The grid cell value is the average value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param centerPoint If the grid point is center or border
     * @return grid data
     */
    public static Array interpolation_Inside_Mean(Array x_s, Array y_s, Array a, Array X, Array Y,
                                                  boolean centerPoint) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        int rowNum, colNum, pNum;
        colNum = (int) X.getSize();
        rowNum = (int) Y.getSize();
        pNum = (int) x_s.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.getDouble(1) - X.getDouble(0);
        double dY = Y.getDouble(1) - Y.getDouble(0);
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v, sx, sy, ex, ey;
        if (centerPoint) {
            sx = X.getDouble(0) - dX * 0.5;
            sy = Y.getDouble(0) - dY * 0.5;
            ex = X.getDouble(colNum - 1) + dX * 0.5;
            ey = Y.getDouble(rowNum - 1) + dY * 0.5;
        } else {
            sx = X.getDouble(0);
            sy = Y.getDouble(0);
            ex = X.getDouble(colNum - 1);
            ey = Y.getDouble(rowNum - 1);
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, 0.0);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (Double.isNaN(v)) {
                continue;
            }

            x = x_s.getDouble(p);
            y = y_s.getDouble(p);
            if (x < sx || x > ex) {
                continue;
            }
            if (y < sy || y > ey) {
                continue;
            }

            int j = (int) ((x - sx) / dX);
            int i = (int) ((y - sy) / dY);
            if (i >= rowNum)
                i = rowNum - 1;
            if (j >= colNum)
                j = colNum - 1;
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) + v);
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0) {
                    r.setDouble(i * colNum + j, Double.NaN);
                } else {
                    r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) / pNums[i][j]);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with inside method - The grid cell value is the maximum value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param centerPoint points locate at center or border of grid
     * @return grid data
     */
    public static Array interpolation_Inside_Max(List<Number> x_s, List<Number> y_s, Array a,
                                                 List<Number> X, List<Number> Y, boolean centerPoint) {
        a = a.copyIfView();

        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v, sx, sy, ex, ey;
        double min = Double.NEGATIVE_INFINITY;
        if (centerPoint) {
            sx = X.get(0).doubleValue() - dX * 0.5;
            sy = Y.get(0).doubleValue() - dY * 0.5;
            ex = X.get(colNum - 1).doubleValue() + dX * 0.5;
            ey = Y.get(rowNum - 1).doubleValue() + dY * 0.5;
        } else {
            sx = X.get(0).doubleValue();
            sy = Y.get(0).doubleValue();
            ex = X.get(colNum - 1).doubleValue();
            ey = Y.get(rowNum - 1).doubleValue();
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, min);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (Double.isNaN(v)) {
                continue;
            }

            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < sx || x > ex) {
                continue;
            }
            if (y < sy || y > ey) {
                continue;
            }

            int j = (int) ((x - sx) / dX);
            int i = (int) ((y - sy) / dY);
            if (i >= rowNum)
                i = rowNum - 1;
            if (j >= colNum)
                j = colNum - 1;
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, Math.max(r.getDouble(i * colNum + j), v));
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0 || Double.isInfinite(r.getDouble(i * colNum + j))) {
                    r.setDouble(i * colNum + j, Double.NaN);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with inside method - The grid cell value is the minimum value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param centerPoint points locate at center or border of grid
     * @return grid data
     */
    public static Array interpolation_Inside_Min(List<Number> x_s, List<Number> y_s, Array a,
                                                 List<Number> X, List<Number> Y, boolean centerPoint) {
        a = a.copyIfView();

        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v, sx, sy, ex, ey;
        double max = Double.MAX_VALUE;
        if (centerPoint) {
            sx = X.get(0).doubleValue() - dX * 0.5;
            sy = Y.get(0).doubleValue() - dY * 0.5;
            ex = X.get(colNum - 1).doubleValue() + dX * 0.5;
            ey = Y.get(rowNum - 1).doubleValue() + dY * 0.5;
        } else {
            sx = X.get(0).doubleValue();
            sy = Y.get(0).doubleValue();
            ex = X.get(colNum - 1).doubleValue();
            ey = Y.get(rowNum - 1).doubleValue();
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, max);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (Double.isNaN(v)) {
                continue;
            }

            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < sx || x > ex) {
                continue;
            }
            if (y < sy || y > ey) {
                continue;
            }

            int j = (int) ((x - sx) / dX);
            int i = (int) ((y - sy) / dY);
            if (i >= rowNum)
                i = rowNum - 1;
            if (j >= colNum)
                j = colNum - 1;
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, Math.min(r.getDouble(i * colNum + j), v));
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0 || r.getDouble(i * colNum + j) == Double.MAX_VALUE) {
                    r.setDouble(i * colNum + j, Double.NaN);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with inside method - The grid cell value is the count number
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param X x coordinate
     * @param Y y coordinate
     * @param pointDensity If return point density value
     * @param centerPoint points locate at center or border of grid
     * @return grid data
     */
    public static Object interpolation_Inside_Count(List<Number> x_s, List<Number> y_s,
                                                    List<Number> X, List<Number> Y, boolean pointDensity, boolean centerPoint) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.INT, new int[]{rowNum, colNum});
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        int[][] pNums = new int[rowNum][colNum];
        double x, y, sx, sy, ex, ey;
        if (centerPoint) {
            sx = X.get(0).doubleValue() - dX * 0.5;
            sy = Y.get(0).doubleValue() - dY * 0.5;
            ex = X.get(colNum - 1).doubleValue() + dX * 0.5;
            ey = Y.get(rowNum - 1).doubleValue() + dY * 0.5;
        } else {
            sx = X.get(0).doubleValue();
            sy = Y.get(0).doubleValue();
            ex = X.get(colNum - 1).doubleValue();
            ey = Y.get(rowNum - 1).doubleValue();
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                //r.setInt(i * colNum + j, 0);
            }
        }

        for (int p = 0; p < pNum; p++) {
            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < sx || x > ex) {
                continue;
            }
            if (y < sy || y > ey) {
                continue;
            }

            int j = (int) ((x - sx) / dX);
            int i = (int) ((y - sy) / dY);
            if (i >= rowNum)
                i = rowNum - 1;
            if (j >= colNum)
                j = colNum - 1;
            pNums[i][j] += 1;
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                r.setInt(i * colNum + j, pNums[i][j]);
            }
        }

        if (pointDensity) {
            Array pds = Array.factory(DataType.INT, new int[]{pNum});
            for (int p = 0; p < pNum; p++) {
                x = x_s.get(p).doubleValue();
                y = y_s.get(p).doubleValue();
                if (x < sx || x > ex) {
                    continue;
                }
                if (y < sy || y > ey) {
                    continue;
                }

                int j = (int) ((x - sx) / dX);
                int i = (int) ((y - sy) / dY);
                if (i >= rowNum)
                    i = rowNum - 1;
                if (j >= colNum)
                    j = colNum - 1;
                pds.setInt(p, pNums[i][j]);
            }
            return new Array[]{r, pds};
        }

        return r;
    }

    /**
     * Interpolate with nearest method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param radius Radius
     * @return grid data
     */
    public static Array interpolation_Nearest(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y,
                                              double radius) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double gx, gy;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        IndexIterator iter = a.getIndexIterator();
        double v;
        for (int i = 0; i < pNum; i++) {
            v = iter.getDoubleNext();
//            if (!Double.isNaN(v)) {
//                kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
//            }
            kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
        }

        //Loop
        if (radius == Double.POSITIVE_INFINITY) {
            for (int i = 0; i < rowNum; i++) {
                gy = Y.get(i).doubleValue();
                for (int j = 0; j < colNum; j++) {
                    gx = X.get(j).doubleValue();
                    KDTree.SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy}, 1).get(0);
                    rdata.setDouble(i * colNum + j, ((double) r.payload));
                }
            }
        } else {
            for (int i = 0; i < rowNum; i++) {
                gy = Y.get(i).doubleValue();
                for (int j = 0; j < colNum; j++) {
                    gx = X.get(j).doubleValue();
                    KDTree.SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy}, 1).get(0);
                    if (Math.sqrt(r.distance) <= radius) {
                        rdata.setDouble(i * colNum + j, ((double) r.payload));
                    } else {
                        rdata.setDouble(i * colNum + j, Double.NaN);
                    }
                }
            }
        }

        return rdata;
    }

    /**
     * Interpolate with nearest method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param radius Radius
     * @return grid data
     */
    public static Array interpolation_Nearest(Array x_s, Array y_s, Array a, Array X, Array Y,
                                              double radius) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        int rowNum, colNum, pNum;
        colNum = (int)X.getSize();
        rowNum = (int)Y.getSize();
        pNum = (int)x_s.getSize();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double gx, gy;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        double v;
        for (int i = 0; i < pNum; i++) {
            v = a.getDouble(i);
//            if (!Double.isNaN(v)) {
//                kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
//            }
            kdTree.addPoint(new double[]{x_s.getDouble(i), y_s.getDouble(i)}, v);
        }

        //Loop
        if (radius == Double.POSITIVE_INFINITY) {
            for (int i = 0; i < rowNum; i++) {
                gy = Y.getDouble(i);
                for (int j = 0; j < colNum; j++) {
                    gx = X.getDouble(j);
                    KDTree.SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy}, 1).get(0);
                    rdata.setDouble(i * colNum + j, ((double) r.payload));
                }
            }
        } else {
            for (int i = 0; i < rowNum; i++) {
                gy = Y.getDouble(i);
                for (int j = 0; j < colNum; j++) {
                    gx = X.getDouble(j);
                    KDTree.SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy}, 1).get(0);
                    if (Math.sqrt(r.distance) <= radius) {
                        rdata.setDouble(i * colNum + j, ((double) r.payload));
                    } else {
                        rdata.setDouble(i * colNum + j, Double.NaN);
                    }
                }
            }
        }

        return rdata;
    }

    /**
     * Interpolate with nearest method
     *
     * @param x_s Scatter X coordinate
     * @param y_s Scatter Y coordinate
     * @param z_s Scatter Z coordinate
     * @param a scatter value array
     * @param X Grid x coordinate
     * @param Y Grid y coordinate
     * @param Z Grid z coordinate
     * @param radius Radius
     * @return Grid data array
     */
    public static Array interpolation_Nearest(Array x_s, Array y_s, Array z_s, Array a, Array X, Array Y,
                                              Array Z, double radius) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        z_s = z_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();
        Z = Z.copyIfView();

        int xNum, yNum, zNum, pNum;
        xNum = (int)X.getSize();
        yNum = (int)Y.getSize();
        zNum = (int)Z.getSize();
        pNum = (int)x_s.getSize();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{zNum, yNum, xNum});
        double gx, gy, gz;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(3);
        double v;
        for (int i = 0; i < pNum; i++) {
            v = a.getDouble(i);
            kdTree.addPoint(new double[]{x_s.getDouble(i), y_s.getDouble(i), z_s.getDouble(i)}, v);
        }

        //Loop
        if (radius == Double.POSITIVE_INFINITY) {
            int ii = 0;
            for (int k = 0; k < zNum; k++) {
                gz = Z.getDouble(k);
                for (int i = 0; i < yNum; i++) {
                    gy = Y.getDouble(i);
                    for (int j = 0; j < xNum; j++) {
                        gx = X.getDouble(j);
                        KDTree.SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy, gz}, 1).get(0);
                        rdata.setDouble(ii, ((double) r.payload));
                        ii += 1;
                    }
                }
            }
        } else {
            int ii = 0;
            for (int k = 0; k < zNum; k++) {
                gz = Z.getDouble(k);
                for (int i = 0; i < yNum; i++) {
                    gy = Y.getDouble(i);
                    for (int j = 0; j < xNum; j++) {
                        gx = X.getDouble(j);
                        KDTree.SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy, gz}, 1).get(0);
                        if (Math.sqrt(r.distance) <= radius) {
                            rdata.setDouble(ii, ((double) r.payload));
                        } else {
                            rdata.setDouble(ii, Double.NaN);
                        }
                        ii += 1;
                    }
                }
            }
        }

        return rdata;
    }

    /**
     * Interpolate with surface method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @return grid data
     */
    public static Array interpolation_Surface(Array x_s, Array y_s, Array a, Array X, Array Y) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        int rowNum, colNum, xn, yn;
        int[] shape = x_s.getShape();
        colNum = shape[1];
        rowNum = shape[0];
        xn = (int) X.getSize();
        yn = (int) Y.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yn, xn});
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, Double.NaN);
        }

        double x, y;
        double v, xll, xtl, xtr, xlr, yll, ytl, ytr, ylr;
        double dX = X.getDouble(1) - X.getDouble(0);
        double dY = Y.getDouble(1) - Y.getDouble(0);
        int minxi, maxxi, minyi, maxyi;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                v = a.getDouble(i * colNum + j);
                if (Double.isNaN(v)) {
                    continue;
                }
                xll = x_s.getDouble(i * colNum + j);
                xtl = x_s.getDouble((i + 1) * colNum + j);
                xtr = x_s.getDouble((i + 1) * colNum + j + 1);
                xlr = x_s.getDouble(i * colNum + j + 1);
                yll = y_s.getDouble(i * colNum + j);
                ytl = y_s.getDouble((i + 1) * colNum + j);
                ytr = y_s.getDouble((i + 1) * colNum + j + 1);
                ylr = y_s.getDouble(i * colNum + j + 1);
                if (Double.isNaN(xll) || Double.isNaN(xtl) || Double.isNaN(xtr) || Double.isNaN(xlr)
                        || Double.isNaN(yll) || Double.isNaN(ytl) || Double.isNaN(ytr) || Double.isNaN(ylr)) {
                    continue;
                }
                PolygonShape ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(xll, yll));
                points.add(new PointD(xtl, ytl));
                points.add(new PointD(xtr, ytr));
                points.add(new PointD(xlr, ylr));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                minxi = (int) ((ps.getExtent().minX - X.getDouble(0)) / dX);
                maxxi = (int) ((ps.getExtent().maxX - X.getDouble(0)) / dX);
                minyi = (int) ((ps.getExtent().minY - Y.getDouble(0)) / dY);
                maxyi = (int) ((ps.getExtent().maxY - Y.getDouble(0)) / dY);
                maxxi += 1;
                maxyi += 1;
                if (maxxi < 0 || minxi >= xn) {
                    continue;
                }
                if (maxyi < 0 || minyi >= yn) {
                    continue;
                }
                if (minxi < 0) {
                    minxi = 0;
                }
                if (maxxi >= xn) {
                    maxxi = xn - 1;
                }
                if (maxyi >= yn) {
                    maxyi = yn - 1;
                }
                for (int m = minyi; m <= maxyi; m++) {
                    y = Y.getDouble(m);
                    for (int n = minxi; n <= maxxi; n++) {
                        x = X.getDouble(n);
                        if (GeoComputation.pointInPolygon(ps, x, y)) {
                            r.setDouble(m * xn + n, v);
                        }
                    }
                }
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW radius method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param neededPointNum needed at least point number
     * @param radius search radius
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Radius(List<Number> x_s, List<Number> y_s, Array a,
                                                 List<Number> X, List<Number> Y, int neededPointNum, double radius) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        int i, j;
        double w, gx, gy, v;
        boolean match;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        IndexIterator iter = a.getIndexIterator();
        for (i = 0; i < pNum; i++) {
            v = iter.getDoubleNext();
            if (!Double.isNaN(v)) {
                kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
            }
        }

        //---- Do interpolation
        for (i = 0; i < rowNum; i++) {
            gy = Y.get(i).doubleValue();
            for (j = 0; j < colNum; j++) {
                gx = X.get(j).doubleValue();
                List<KDTree.SearchResult<Double>> srs = kdTree.ballSearch_distance(new double[]{gx, gy}, radius * radius);
                if (srs == null || srs.size() < neededPointNum) {
                    r.setDouble(i * colNum + j, Double.NaN);
                } else {
                    double v_sum = 0.0;
                    double weight_sum = 0.0;
                    match = false;
                    for (KDTree.SearchResult sr : srs) {
                        v = (double) sr.payload;
                        if (sr.distance == 0) {
                            r.setDouble(i * colNum + j, v);
                            match = true;
                            break;
                        } else {
                            w = 1. / sr.distance;
                            weight_sum += w;
                            v_sum += v * w;
                        }
                    }
                    if (!match) {
                        r.setDouble(i * colNum + j, v_sum / weight_sum);
                    }
                }
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW radius method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param neededPointNum needed at least point number
     * @param radius search radius
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Radius(Array x_s, Array y_s, Array a,
                                                 Array X, Array Y, int neededPointNum, double radius) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        int rowNum, colNum, pNum;
        colNum = (int)X.getSize();
        rowNum = (int)Y.getSize();
        pNum = (int)x_s.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        int i, j;
        double w, gx, gy, v;
        boolean match;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        for (i = 0; i < pNum; i++) {
            v = a.getDouble(i);
            if (!Double.isNaN(v)) {
                kdTree.addPoint(new double[]{x_s.getDouble(i), y_s.getDouble(i)}, v);
            }
        }

        //---- Do interpolation
        for (i = 0; i < rowNum; i++) {
            gy = Y.getDouble(i);
            for (j = 0; j < colNum; j++) {
                gx = X.getDouble(j);
                List<KDTree.SearchResult<Double>> srs = kdTree.ballSearch_distance(new double[]{gx, gy}, radius * radius);
                if (srs == null || srs.size() < neededPointNum) {
                    r.setDouble(i * colNum + j, Double.NaN);
                } else {
                    double v_sum = 0.0;
                    double weight_sum = 0.0;
                    match = false;
                    for (KDTree.SearchResult sr : srs) {
                        v = (double) sr.payload;
                        if (sr.distance == 0) {
                            r.setDouble(i * colNum + j, v);
                            match = true;
                            break;
                        } else {
                            w = 1. / sr.distance;
                            weight_sum += w;
                            v_sum += v * w;
                        }
                    }
                    if (!match) {
                        r.setDouble(i * colNum + j, v_sum / weight_sum);
                    }
                }
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW radius method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param z_s Scatter Z coordinate
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param Z Grid Z coordinate
     * @param neededPointNum needed at least point number
     * @param radius search radius
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Radius(Array x_s, Array y_s, Array z_s, Array a,
                                                 Array X, Array Y, Array Z, int neededPointNum, double radius) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        z_s = z_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();
        Z = Z.copyIfView();

        int xNum, yNum, zNum, pNum;
        xNum = (int)X.getSize();
        yNum = (int)Y.getSize();
        zNum = (int)Z.getSize();
        pNum = (int)x_s.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{zNum, yNum, xNum});
        int i, j;
        double w, gx, gy, gz, v;
        boolean match;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        for (i = 0; i < pNum; i++) {
            v = a.getDouble(i);
            if (!Double.isNaN(v)) {
                kdTree.addPoint(new double[]{x_s.getDouble(i), y_s.getDouble(i), z_s.getDouble(i)}, v);
            }
        }

        //---- Do interpolation
        int ii = 0;
        for (int k = 0; k < zNum; k++) {
            gz = Z.getDouble(k);
            for (i = 0; i < yNum; i++) {
                gy = Y.getDouble(i);
                for (j = 0; j < xNum; j++) {
                    gx = X.getDouble(j);
                    List<KDTree.SearchResult<Double>> srs = kdTree.ballSearch_distance(new double[]{gx, gy, gz}, radius * radius);
                    if (srs == null || srs.size() < neededPointNum) {
                        r.setDouble(ii, Double.NaN);
                    } else {
                        double v_sum = 0.0;
                        double weight_sum = 0.0;
                        match = false;
                        for (KDTree.SearchResult sr : srs) {
                            v = (double) sr.payload;
                            if (sr.distance == 0) {
                                r.setDouble(ii, v);
                                match = true;
                                break;
                            } else {
                                w = 1. / sr.distance;
                                weight_sum += w;
                                v_sum += v * w;
                            }
                        }
                        if (!match) {
                            r.setDouble(ii, v_sum / weight_sum);
                        }
                    }
                    ii += 1;
                }
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW neighbor method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param points Number of points used for interpolation
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Neighbor(List<Number> x_s, List<Number> y_s, Array a,
                                                   List<Number> X, List<Number> Y, Integer points) {
        int colNum = X.size();
        int rowNum = Y.size();
        int pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        int i, j;
        double w, v, gx, gy;
        boolean match;

        //Construct K-D tree
        int n = 0;
        IndexIterator iter = a.getIndexIterator();
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        for (i = 0; i < pNum; i++) {
            v = iter.getDoubleNext();
            if (!Double.isNaN(v)) {
                kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
                n += 1;
            }
        }
        if (points == null) {
            points = n;
        }

        //---- Do interpolation with IDW method
        for (i = 0; i < rowNum; i++) {
            gy = Y.get(i).doubleValue();
            for (j = 0; j < colNum; j++) {
                gx = X.get(j).doubleValue();
                List<KDTree.SearchResult<Double>> srs = kdTree.nearestNeighbours(new double[]{gx, gy}, points);
                double v_sum = 0.0;
                double weight_sum = 0.0;
                match = false;
                for (KDTree.SearchResult sr : srs) {
                    v = (double) sr.payload;
                    if (sr.distance == 0) {
                        r.setDouble(i * colNum + j, v);
                        match = true;
                        break;
                    } else {
                        w = 1. / sr.distance;
                        weight_sum += w;
                        v_sum += v * w;
                    }
                }
                if (!match) {
                    r.setDouble(i * colNum + j, v_sum / weight_sum);
                }
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW neighbor method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param points Number of points used for interpolation
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Neighbor(Array x_s, Array y_s, Array a,
                                                   Array X, Array Y, Integer points) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();
        int colNum = (int)X.getSize();
        int rowNum = (int)Y.getSize();
        int pNum = (int)x_s.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        int i, j;
        double w, v, gx, gy;
        boolean match;

        //Construct K-D tree
        int n = 0;
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        for (i = 0; i < pNum; i++) {
            v = a.getDouble(i);
            if (!Double.isNaN(v)) {
                kdTree.addPoint(new double[]{x_s.getDouble(i), y_s.getDouble(i)}, v);
                n += 1;
            }
        }
        if (points == null) {
            points = n;
        }

        //---- Do interpolation with IDW method
        for (i = 0; i < rowNum; i++) {
            gy = Y.getDouble(i);
            for (j = 0; j < colNum; j++) {
                gx = X.getDouble(j);
                List<KDTree.SearchResult<Double>> srs = kdTree.nearestNeighbours(new double[]{gx, gy}, points);
                double v_sum = 0.0;
                double weight_sum = 0.0;
                match = false;
                for (KDTree.SearchResult sr : srs) {
                    v = (double) sr.payload;
                    if (sr.distance == 0) {
                        r.setDouble(i * colNum + j, v);
                        match = true;
                        break;
                    } else {
                        w = 1. / sr.distance;
                        weight_sum += w;
                        v_sum += v * w;
                    }
                }
                if (!match) {
                    r.setDouble(i * colNum + j, v_sum / weight_sum);
                }
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW neighbor method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param z_s Scatter Z coordinate
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param Z Grid Z coordinate
     * @param points Number of points used for interpolation
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Neighbor(Array x_s, Array y_s, Array z_s, Array a,
                                                   Array X, Array Y, Array Z, Integer points) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        z_s = z_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();
        Z = Z.copyIfView();
        int xNum = (int)X.getSize();
        int yNum = (int)Y.getSize();
        int zNum = (int)Z.getSize();
        int pNum = (int)x_s.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{zNum, yNum, xNum});
        int i, j;
        double w, v, gx, gy, gz;
        boolean match;

        //Construct K-D tree
        int n = 0;
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(3);
        for (i = 0; i < pNum; i++) {
            v = a.getDouble(i);
            if (!Double.isNaN(v)) {
                kdTree.addPoint(new double[]{x_s.getDouble(i), y_s.getDouble(i), z_s.getDouble(i)}, v);
                n += 1;
            }
        }
        if (points == null) {
            points = n;
        }

        //---- Do interpolation with IDW method
        int ii = 0;
        for (int k = 0; k < zNum; k++) {
            gz = Z.getDouble(k);
            for (i = 0; i < yNum; i++) {
                gy = Y.getDouble(i);
                for (j = 0; j < xNum; j++) {
                    gx = X.getDouble(j);
                    List<KDTree.SearchResult<Double>> srs = kdTree.nearestNeighbours(new double[]{gx, gy, gz}, points);
                    double v_sum = 0.0;
                    double weight_sum = 0.0;
                    match = false;
                    for (KDTree.SearchResult sr : srs) {
                        v = (double) sr.payload;
                        if (sr.distance == 0) {
                            r.setDouble(ii, v);
                            match = true;
                            break;
                        } else {
                            w = 1. / sr.distance;
                            weight_sum += w;
                            v_sum += v * w;
                        }
                    }
                    if (!match) {
                        r.setDouble(ii, v_sum / weight_sum);
                    }
                    ii += 1;
                }
            }
        }

        return r;
    }
}
