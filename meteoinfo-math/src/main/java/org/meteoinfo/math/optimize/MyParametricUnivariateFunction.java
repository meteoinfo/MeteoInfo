package org.meteoinfo.math.optimize;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;

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
        return function.value(v);
    }

    @Override
    public double[] gradient(double v, double... parameters) {
        function.setParameters(parameters);

        // create a differentiator
        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(nbPoints, stepSize);

        // create a new function that computes both the value and the derivatives
        // using DerivativeStructure
        UnivariateDifferentiableFunction diffFunc = differentiator.differentiate(function);

        double y = function.value(v);
        int n = parameters.length;
        double[] gradients = new double[n];
        for (int i = 0; i < n; i++) {
            DerivativeStructure xDS = new DerivativeStructure(n, 1, i, parameters[i]);
            DerivativeStructure yDS = diffFunc.value(xDS);
            int[] idx = new int[n];
            idx[i] = 1;
            gradients[i] = yDS.getPartialDerivative(idx);
        }

        return gradients;
    }
}
