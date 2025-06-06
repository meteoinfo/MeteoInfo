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
}
