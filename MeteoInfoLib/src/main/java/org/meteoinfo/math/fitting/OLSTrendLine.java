/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.fitting;

import java.util.Arrays;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import ucar.ma2.Array;
import ucar.ma2.Index;

/**
 *
 * @author Yaqiang Wang
 */
public abstract class OLSTrendLine implements TrendLine {
    RealMatrix coef = null; // will hold prediction coefs once we get values
    double rs;

    protected abstract double[] xVector(double x); // create vector of values from x
    protected abstract boolean logY(); // set true to predict log of y (note: y must be positive)

    @Override
    public void setValues(Array y, Array x) {
        if (x.getSize() != y.getSize()) {
            throw new IllegalArgumentException(String.format("The numbers of y and x values must be equal (%d != %d)",y.getSize(),x.getSize()));
        }
        double[][] xData = new double[(int)x.getSize()][]; 
        for (int i = 0; i < x.getSize(); i++) {
            // the implementation determines how to produce a vector of predictors from a single x
            xData[i] = xVector(x.getDouble(i));
        }
        double[] yy = new double[(int)y.getSize()];
        if(logY()) { // in some models we are predicting ln y, so we replace each y with ln y
            for (int i = 0; i < yy.length; i++) {
                if (i < x.getSize())
                    yy[i] = Math.log(y.getDouble(i));
                else
                    yy[i] = y.getDouble(i);
            }
        } else {
            for (int i = 0; i < yy.length; i++) {
                yy[i] = y.getDouble(i);
            }
        }
//        double[] yy = (double[])y.copyTo1DJavaArray();
//        if(logY()) { // in some models we are predicting ln y, so we replace each y with ln y
//            yy = Arrays.copyOf(yy, yy.length); // user might not be finished with the array we were given
//            for (int i = 0; i < x.getSize(); i++) {
//                yy[i] = Math.log(yy[i]);
//            }
//        }
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(true); // let the implementation include a constant in xVector if desired
        ols.newSampleData(yy, xData); // provide the data to the model
        coef = MatrixUtils.createColumnRealMatrix(ols.estimateRegressionParameters()); // get our coefs
        rs = ols.calculateRSquared();
    }

    @Override
    public double predict(double x) {
        double yhat = coef.preMultiply(xVector(x))[0]; // apply coefs to xVector
        if (logY()) yhat = (Math.exp(yhat)); // if we predicted ln y, we still need to get y
        return yhat;
    }
}
