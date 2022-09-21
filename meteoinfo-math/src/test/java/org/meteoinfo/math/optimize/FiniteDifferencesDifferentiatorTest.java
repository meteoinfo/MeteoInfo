package org.meteoinfo.math.optimize;

import org.apache.commons.math4.legacy.analysis.UnivariateFunction;
import org.apache.commons.math4.legacy.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math4.legacy.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math4.legacy.analysis.differentiation.UnivariateDifferentiableFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FiniteDifferencesDifferentiatorTest {

    @Test
    public void testGradients() {
        double[] parameters = new double[]{2.5, 1.3, 0.5};
        double a = parameters[0];
        double b = parameters[1];
        double c = parameters[2];
        double x = 2.0;
        double[] gradients = new double[3];
        gradients[0] = Math.exp(-b * x);
        gradients[1] = -a * x * gradients[0];
        gradients[2] = 1;

        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(5, 0.01);
        double[] gTests = new double[3];
        for (int i = 0; i < 3; i++) {
            UnivariateFunction uf = new MyUnivariateFunction(new ParamFunction(), x, i, parameters);
            UnivariateDifferentiableFunction f = differentiator.differentiate(uf);
            DerivativeStructure y = f.value(new DerivativeStructure(1, 1, 0, parameters[i]));
            Assertions.assertEquals(gradients[i], y.getPartialDerivative(1), 1.0e-8);
            //gTests[i] = y.getPartialDerivative(1);
        }
    }

    private static class ParamFunction {

        public double value(double v, double... parameters) {
            double a = parameters[0];
            double b = parameters[1];
            double c = parameters[2];

            return a * Math.exp(-b * v) + c;
        }
    }

    private static class MyUnivariateFunction implements UnivariateFunction {

        private ParamFunction function;
        private double x;
        private int paraIdx, paraNum;
        private double[] parameters;

        public MyUnivariateFunction(ParamFunction function, double x, int paraIdx, double... parameters) {
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

            return function.value(x, params);
        }
    }
}
