/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.fitting;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.data.ArrayMath;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class FittingUtil {
    /**
     * Power fitting
     * @param x X array
     * @param y Y array
     * @return Fitting parameters and trend line object
     */
    public static Object[] powerFit(Array x, Array y){
        PowerTrendLine t = new PowerTrendLine();
        t.setValues(y, x);
        double y_mean = ArrayMath.mean(y);
        double py;
        double sum1 = 0.0, sum2 = 0.0;
        for (int i = 0; i < y.getSize(); i++){
            py = t.predict(x.getDouble(i));
            sum1 += Math.pow(y.getDouble(i) - py, 2);
            sum2 += Math.pow(y.getDouble(i) - y_mean, 2);
        }
        double r = 1 - sum1 / sum2;
        return new Object[]{Math.exp(t.coef.getEntry(0, 0)), t.coef.getEntry(1, 0), r, t};
    }
    
    /**
     * Exponent fitting
     * @param x X array
     * @param y Y array
     * @return Fitting parameters and trend line object
     */
    public static Object[] expFit(Array x, Array y){
        ExpTrendLine t = new ExpTrendLine();
        t.setValues(y, x);
        double y_mean = ArrayMath.mean(y);
        double py;
        double sum1 = 0.0, sum2 = 0.0;
        for (int i = 0; i < y.getSize(); i++){
            py = t.predict(x.getDouble(i));
            sum1 += Math.pow(y.getDouble(i) - py, 2);
            sum2 += Math.pow(y.getDouble(i) - y_mean, 2);
        }
        double r = 1 - sum1 / sum2;
        return new Object[]{Math.exp(t.coef.getEntry(0, 0)), t.coef.getEntry(1, 0), r, t};
    }
    
    /**
     * Polynomail fitting
     * @param x X array
     * @param y Y array
     * @param degree Degree
     * @return Fitting parameters and trend line object
     */
    public static Object[] polyFit(Array x, Array y, int degree){
        PolyTrendLine t = new PolyTrendLine(degree);
        t.setValues(y, x);
        double y_mean = ArrayMath.mean(y);
        double py;
        double sum1 = 0.0, sum2 = 0.0;
        for (int i = 0; i < y.getSize(); i++){
            py = t.predict(x.getDouble(i));
            sum1 += Math.pow(y.getDouble(i) - py, 2);
            sum2 += Math.pow(y.getDouble(i) - y_mean, 2);
        }
        double r = 1 - sum1 / sum2;
        List<Double> para = new ArrayList<>();
        int n = t.coef.getRowDimension();
        for (int i = 0; i < n; i++){
            para.add(t.coef.getEntry(n - i - 1, 0));
        }
        return new Object[]{para, r, t};
    }
    
    /**
     * Predict a value
     * @param x X value
     * @param tl The trend line object
     * @return Predicted value
     */
    public static double predict(double x, OLSTrendLine tl){
        return tl.predict(x);
    }

    /**
     * Predict a value
     * @param x X value
     * @param tl The trend line object
     * @return Predicted value
     */
    public static Array predict(Array x, OLSTrendLine tl){
        Array y = Array.factory(DataType.DOUBLE, x.getShape());
        for (int i = 0; i < y.getSize(); i++){
            y.setDouble(i, tl.predict(x.getDouble(i)));
        }
        return y;
    }
}
