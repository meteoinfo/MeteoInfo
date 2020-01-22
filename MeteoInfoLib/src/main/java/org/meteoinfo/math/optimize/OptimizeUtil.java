package org.meteoinfo.math.optimize;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.lang.reflect.Method;

public class OptimizeUtil {

    /**
     * Calculate Jacobian matrix.
     * @param func The uni-variate function
     * @param x X values
     * @param nbPoints Number of points for difference calculation
     * @param stepSize Step size for difference calculation
     * @return Jacobian matrix
     */
    public static RealMatrix calJacobianMatrix(UnivariateFunction func, double[] x, int nbPoints,
                                               double stepSize) throws NoSuchMethodException {
        Class cls = func.getClass();
        Method method = cls.getMethod("value");
        int order = method.getParameterCount() - 1;

        // create a differentiator
        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(nbPoints, stepSize);

        // create a new function that computes both the value and the derivatives
        // using DerivativeStructure
        UnivariateDifferentiableFunction diffFunc = differentiator.differentiate(func);

        // now we can compute display the value and its derivatives
        // here we decided to display up to second order derivatives,
        // because we feed completeF with order 2 DerivativeStructure instances
        RealMatrix jacobian = new Array2DRowRealMatrix(x.length, order);
        for (int i = 0; i < x.length; i++) {
            DerivativeStructure xDS = new DerivativeStructure(1, order, 0, x[i]);
            DerivativeStructure yDS = diffFunc.value(xDS);
            for (int j = 0; j < order; j++) {
                jacobian.setEntry(i, j, yDS.getPartialDerivative(j));
            }
        }

        return jacobian;
    }

    /**
     * Get Jacobian function.
     * @param func The uni-variate function
     * @param x X values
     * @param params Parameter number
     * @param nbPoints Number of points for difference calculation
     * @param stepSize Step size for difference calculation
     * @return Jacobian function
     */
    public static MultivariateJacobianFunction getJacobianFunction(ParamUnivariateFunction func, Array x,
        int params, int nbPoints, double stepSize) throws NoSuchMethodException {
//        Class cls = func.getClass();
//        Method method = cls.getMethod("value");
//        int order = method.getParameterCount() - 1;

        // create a differentiator
        FiniteDifferencesDifferentiator differentiator =
                new FiniteDifferencesDifferentiator(nbPoints, stepSize);

        MultivariateJacobianFunction jacobianFunc = new MultivariateJacobianFunction() {
            public Pair<RealVector, RealMatrix> value(final RealVector point) {
                func.setParameters(point.toArray());
                // create a new function that computes both the value and the derivatives
                // using DerivativeStructure
                UnivariateDifferentiableFunction diffFunc = differentiator.differentiate(func);

                int n = (int) x.getSize();
                RealVector value = new ArrayRealVector(n);
                RealMatrix jacobian = new Array2DRowRealMatrix(n, params);
                IndexIterator iter = x.getIndexIterator();
                double v;
                for (int i = 0; i < n; ++i) {
                    v = iter.getDoubleNext();
                    double modelI = func.value(v);
                    value.setEntry(i, modelI);
                    for (int j = 0; j < params; j++) {
                        DerivativeStructure xDS = new DerivativeStructure(params, 1, j, point.getEntry(j));
                        DerivativeStructure yDS = diffFunc.value(xDS);
                        int[] idx = new int[params];
                        idx[j] = 1;
                        jacobian.setEntry(i, j, yDS.getPartialDerivative(idx));
                    }
                }

                return new Pair<RealVector, RealMatrix>(value, jacobian);
            }
        };

        return jacobianFunc;
    }
}
