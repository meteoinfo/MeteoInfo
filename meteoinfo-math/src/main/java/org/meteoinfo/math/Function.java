package org.meteoinfo.math;

import java.io.Serializable;

public interface Function extends Serializable {
    /**
     * Computes the value of the function at x.
     *
     * @param x a real number.
     * @return the function value.
     */
    double f(double x);

    /**
     * Computes the value of the function at x.
     * It delegates the computation to f().
     * This is simply for Scala convenience.
     *
     * @param x a real number.
     * @return the function value.
     */
    default double apply(double x) {
        return f(x);
    }
}
