package org.meteoinfo.math.special;

import org.apache.commons.numbers.gamma.Erf;
import org.apache.commons.numbers.gamma.Gamma;
import org.apache.commons.numbers.gamma.LogGamma;
import org.apache.commons.numbers.combinatorics.Factorial;
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
        return n >= 0 ? Factorial.value(n) : 0;
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
        return Gamma.value(x);
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
            yIter.setDoubleNext(Gamma.value(xIter.getDoubleNext()));
        }

        return y;
    }

    /**
     * The natural logarithm of the Gamma function
     * @param x Value
     * @return Value of logarithm of the gamma function
     */
    public static double logGamma(double x) {
        return LogGamma.value(x);
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
            yIter.setDoubleNext(LogGamma.value(xIter.getDoubleNext()));
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
        return Erf.value(x);
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
            rIter.setDoubleNext(Erf.value(xIter.getDoubleNext()));
        }

        return r;
    }

    /**
     * Complementary error function, 1 - erf(x)
     * @param x Value
     * @return Complementary error function
     */
    public static double erfc(double x) {
        return Erf.value(x);
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
            rIter.setDoubleNext(Erf.value(xIter.getDoubleNext()));
        }

        return r;
    }
}
