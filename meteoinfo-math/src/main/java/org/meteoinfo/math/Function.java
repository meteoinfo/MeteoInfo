package org.meteoinfo.math;

import java.io.Serializable;

public interface Function extends Serializable {
    /**
     * Evaluates the function at x.
     *
     * @param x
     *
     * @return double f(x)
     */
    public double evaluate(double x);
}
