package org.meteoinfo.math.optimize;

import org.apache.commons.math4.legacy.analysis.ParametricUnivariateFunction;
import org.apache.commons.math4.legacy.analysis.UnivariateFunction;
import org.apache.commons.math4.legacy.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math4.legacy.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math4.legacy.analysis.differentiation.UnivariateDifferentiableFunction;

public class MyParametricUnivariateFunction implements ParametricUnivariateFunction {

    private ParamUnivariateFunction function;
    private int nbPoints;
    private double stepSize;

    /** A number close to zero, between machine epsilon and its square root. */
    double EPSILON = 1E-8;
    boolean useDerivativeStructure;

    /**
     * Constructor
     * @param function ParamUnivariateFunction
     * @param nbPoints Number of points for difference calculation
     * @param stepSize Step size for difference calculation
     */
    public MyParametricUnivariateFunction(ParamUnivariateFunction function, int nbPoints, double stepSize) {
        this(function, nbPoints, stepSize, true);
    }

    /**
     * Constructor
     * @param function ParamUnivariateFunction
     * @param nbPoints Number of points for difference calculation
     * @param stepSize Step size for difference calculation
     */
    public MyParametricUnivariateFunction(ParamUnivariateFunction function, int nbPoints, double stepSize,
                                          boolean useDerivativeStructure) {
        this.function = function;
        this.nbPoints = nbPoints;
        this.stepSize = stepSize;
        this.useDerivativeStructure = useDerivativeStructure;
    }

    /**
     * Get whether using DerivativeStructure
     * @return Whether using DerivativeStructure
     */
    public boolean isUseDerivativeStructure() {
        return this.useDerivativeStructure;
    }

    /**
     * Set whether using DerivativeStructure
     * @param value Whether using DerivativeStructure
     */
    public void setUseDerivativeStructure(boolean value) {
        this.useDerivativeStructure = value;
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
        if (this.useDerivativeStructure) {
            return gradientDerivativeStructure(x, parameters);
        } else {
            return gradientSimple(x, parameters);
        }
    }

    public double[] gradientSimple(double x, double ... parameters) {
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
    }

    public double[] gradientDerivativeStructure(double x, double ... parameters) {
        function.setParameters(parameters);
        double fx = function.value(x);

        int n = parameters.length;
        double[] gradients = new double[n];
        for (int i = 0; i < n; i++) {
            gradients[i] = Double.POSITIVE_INFINITY;
        }

        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(5, 0.01);
        for (int i = 0; i < n; i++) {
            UnivariateFunction uf = new MyUnivariateFunction(function, x, i, parameters);
            UnivariateDifferentiableFunction f = differentiator.differentiate(uf);
            DerivativeStructure y = f.value(new DerivativeStructure(1, 1, 0, parameters[i]));
            gradients[i] = y.getPartialDerivative(1);
        }
        function.setParameters(parameters);

        return gradients;
    }

    private class MyUnivariateFunction implements UnivariateFunction {

        private ParamUnivariateFunction function;
        private double x;
        private int paraIdx, paraNum;
        private double[] parameters;

        public MyUnivariateFunction(ParamUnivariateFunction function, double x, int paraIdx, double... parameters) {
            this.function = function;
            this.x = x;
            this.paraIdx = paraIdx;
            this.parameters = parameters;
            this.paraNum = parameters.length;
        }

        @Override
        public double value(double v) {
            double[] params = new double[this.paraNum];
            System.arraycopy(this.parameters, 0, params, 0, paraNum);
            params[paraIdx] = v;
            function.setParameters(params);

            return function.value(x);
        }
    }
}
