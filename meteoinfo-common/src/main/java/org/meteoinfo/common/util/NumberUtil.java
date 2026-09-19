package org.meteoinfo.common.util;

public class NumberUtil {

    /**
     * Check if the string is an integer
     *
     * @param str The string
     * @return Is an integer or not
     */
    public static boolean isInteger(String str) {
        return str.matches("-?\\d+");
    }


    /**
     * Check if the string is a decimal
     *
     * @param str The string
     * @return Is a decimal or not
     */
    public static boolean isDecimal(String str) {
        return str.matches("-?\\d*\\.\\d+");
    }

    /**
     * Check if the two double value are equal
     *
     * @param x1 Double value 1
     * @param x2 Double value 2
     * @param tolerance Tolerance
     * @return Is equal or not
     */
    public static boolean equalsWithTolerance(double x1, double x2, double tolerance) {
        return Math.abs(x1 - x2) <= tolerance;
    }
}
