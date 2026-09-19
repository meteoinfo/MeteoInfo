package org.meteoinfo.common.util;

public class MathUtil {
    /**
     * Computes the length of the vector (x,y).
     * This is the length of the hypotenuse of
     * a right triangle with sides of length x and y.
     *
     * This function is faster than the standard {@link Math.hypot} function.
     *
     * @param x the x ordinate
     * @param y the y ordinate
     * @return the length of vector (x,y)
     */
    public static double hypot(double x, double y) {
        return Math.sqrt(x * x +  y * y);
    }
}
