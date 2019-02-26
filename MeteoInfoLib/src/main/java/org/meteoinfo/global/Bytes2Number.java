/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.global;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author yaqiang
 */
public class Bytes2Number {

    /**
     * Convert four ints into a signed integer.
     *
     * @param a Highest int
     * @param b Higher middle int
     * @param c Lower middle int
     * @param d Lowest int
     * @return Integer value
     */
    public static int int4(int a, int b, int c, int d) {
        if ((a == 255) && (b == 255) && (c == 255) && (d == 255)) {
            return -9999;
        }

        return (1 - ((a & 0x80) >> 6)) * ((a & 0x7F) << 24 | b << 16 | c << 8 | d);
    }

    /**
     * Read signed integer of 4 bytes from binary reader
     *
     * @param raf RandomAccessFile
     * @return Signed integer
     * @throws IOException
     */
    public static int int4(RandomAccessFile raf) throws IOException {
        int a = raf.read();
        int b = raf.read();
        int c = raf.read();
        int d = raf.read();

        return int4(a, b, c, d);
    }
}
