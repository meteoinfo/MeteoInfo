package org.meteoinfo.math.special;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;

public class SpecialUtil {

    /**
     * Returns n!. Shorthand for n Factorial, the product of the numbers 1,...,n.
     * @param n Input value. If n < 0, the return value is 0.
     * @return Factorial value
     */
    public static long factorial(int n) {
        return n >= 0 ? CombinatoricsUtils.factorial(n) : 0;
    }

    /**
     * Returns n!. Shorthand for n Factorial, the product of the numbers 1,...,n.
     * @param x Input value array. If n < 0, the return value is 0.
     * @return Factorial value array
     */
    public static Array factorial(Array x) {
        Array r = Array.factory(DataType.LONG, x.getShape());
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator rIter = r.getIndexIterator();
        while(xIter.hasNext()) {
            rIter.setLongNext(factorial(xIter.getIntNext()));
        }

        return r;
    }

    /**
     * Gamma function
     * @param x Value
     * @return Value of gamma function
     */
    public static double gamma(double x) {
        return Gamma.gamma(x);
    }

    /**
     * Gamma function
     * @param x Value array
     * @return Value array of gamma function
     */
    public static Array gamma(Array x) {
        x = x.copyIfView();
        Array y = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        while(xIter.hasNext()) {
            yIter.setDoubleNext(Gamma.gamma(xIter.getDoubleNext()));
        }

        return y;
    }

    /**
     * The natural logarithm of the Gamma function
     * @param x Value
     * @return Value of logarithm of the gamma function
     */
    public static double logGamma(double x) {
        return Gamma.logGamma(x);
    }

    /**
     * The natural logarithm of the Gamma function
     * @param x Value array
     * @return Value array of logarithm of the gamma function
     */
    public static Array logGamma(Array x) {
        x = x.copyIfView();
        Array y = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator yIter = y.getIndexIterator();
        while(xIter.hasNext()) {
            yIter.setDoubleNext(Gamma.logGamma(xIter.getDoubleNext()));
        }

        return y;
    }

    /**
     * Returns the error function of complex argument.
     * It is defined as 2/sqrt(pi)*integral(exp(-t**2), t=0..z).
     * @param x Value
     * @return Error function
     */
    public static double erf(double x) {
        return Erf.erf(x);
    }

    /**
     * Returns the error function of complex argument.
     * It is defined as 2/sqrt(pi)*integral(exp(-t**2), t=0..z).
     * @param x Value array
     * @return Error function
     */
    public static Array erf(Array x) {
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator rIter = r.getIndexIterator();
        while(xIter.hasNext()) {
            rIter.setDoubleNext(Erf.erf(xIter.getDoubleNext()));
        }

        return r;
    }

    /**
     * Complementary error function, 1 - erf(x)
     * @param x Value
     * @return Complementary error function
     */
    public static double erfc(double x) {
        return Erf.erfc(x);
    }

    /**
     * Complementary error function, 1 - erf(x)
     * @param x Value array
     * @return Complementary error function
     */
    public static Array erfc(Array x) {
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        IndexIterator xIter = x.getIndexIterator();
        IndexIterator rIter = r.getIndexIterator();
        while(xIter.hasNext()) {
            rIter.setDoubleNext(Erf.erfc(xIter.getDoubleNext()));
        }

        return r;
    }
}
