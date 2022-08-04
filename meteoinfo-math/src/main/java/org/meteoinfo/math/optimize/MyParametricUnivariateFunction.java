package org.meteoinfo.math.optimize;

import org.apache.commons.math4.legacy.analysis.ParametricUnivariateFunction;
import org.apache.commons.math4.legacy.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math4.legacy.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math4.legacy.analysis.differentiation.UnivariateDifferentiableFunction;

public class MyParametricUnivariateFunction implements ParametricUnivariateFunction {

    private ParamUnivariateFunction function;
    private int nbPoints;
    private double stepSize;

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

        int n = parameters.length;
        double[] gradients = new double[n];
        for (int i = 0; i < n; i++) {
            gradients[i] = Double.POSITIVE_INFINITY;
        }

        try {
            // create a differentiator
            FiniteDifferencesDifferentiator differentiator =
                    new FiniteDifferencesDifferentiator(nbPoints, stepSize);

            // create a new function that computes both the value and the derivatives
            // using DerivativeStructure
            UnivariateDifferentiableFunction diffFunc = differentiator.differentiate(function);

            for (int i = 0; i < n; i++) {
                DerivativeStructure xDS = new DerivativeStructure(n, 1, i, parameters[i]);
                DerivativeStructure yDS = diffFunc.value(xDS);
                int[] idx = new int[n];
                idx[i] = 1;
                gradients[i] = yDS.getPartialDerivative(idx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
