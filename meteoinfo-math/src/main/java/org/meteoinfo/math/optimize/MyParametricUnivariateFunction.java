package org.meteoinfo.math.optimize;

import org.apache.commons.math4.legacy.analysis.ParametricUnivariateFunction;
import org.apache.commons.math4.legacy.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math4.legacy.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math4.legacy.analysis.differentiation.UnivariateDifferentiableFunction;

public class MyParametricUnivariateFunction implements ParametricUnivariateFunction {

    private ParamUnivariateFunction function;
    private int nbPoints;
    private double stepSize;

    /** A number close to zero, between machine epsilon and its square root. */
    double EPSILON = 1E-8;

    /**
     * Constructor
     * @param function ParamUnivariateFunction
     * @param nbPoints Number of points for difference calculation
     * @param stepSize Step size for difference calculation
     */
    public MyParametricUnivariateFunction(ParamUnivariateFunction function, int nbPoints, double stepSize) {
        this.function = function;
        this.nbPoints = nbPoints;
        this.stepSize = stepSize;
    }

    @Override
    public double value(double v, double... parameters) {
        function.setParameters(parameters);
        double y = Double.POSITIVE_INFINITY;
        try {
            y = function.value(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return y;
    }

    @Override
    public double[] gradient(double x, double ... parameters) {
        function.setParameters(parameters);
        double fx = function.value(x);

        int n = parameters.length;
        double[] gradients = new double[n];
        for (int i = 0; i < n; i++) {
            gradients[i] = Double.POSITIVE_INFINITY;
        }

        double[] xh = new double[n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(parameters, 0, xh, 0, n);
            double xi = parameters[i];
            double h = EPSILON * Math.abs(xi);
            if (h == 0.0) {
                h = EPSILON;
            }
            xh[i] = xi + h; // trick to reduce finite-precision error.
            h = xh[i] - xi;

            function.setParameters(xh);
            double fh = function.value(x);
            xh[i] = xi;
            gradients[i] = (fh - fx) / h;
        }
        function.setParameters(parameters);

        return gradients;

        /*final double a = parameters[0];
        final double b = parameters[1];
        final double c = parameters[2];
        final double[] grad = new double[3];
        grad[0] = Math.exp(-b * x);
        grad[1] = -a * x * grad[0];
        grad[2] = 1;
        return grad;*/
    }
}
