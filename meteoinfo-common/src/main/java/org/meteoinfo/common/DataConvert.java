/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.common;

import org.meteoinfo.common.util.JDateUtil;
import org.meteoinfo.common.util.TypeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @author Yaqiang Wang
 */
public class DataConvert {

    /**
     * Byte array convert to float
     *
     * @param b Byte array
     * @param byteOrder Byte order
     * @return Float value
     */
    public static float bytes2Float(byte[] b, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.wrap(b);
        buf.order(byteOrder);
        return buf.getFloat();
    }

    /**
     * Byte array convert to double
     *
     * @param b Byte array
     * @param byteOrder Byte order
     * @return Double value
     */
    public static double bytes2Double(byte[] b, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.wrap(b);
        buf.order(byteOrder);
        return buf.getDouble();
    }

    /**
     * Byte array convert to integer
     *
     * @param bytes Byte array
     * @param byteOrder Byte order
     * @return Integer value
     */
    public static int bytes2Int(byte[] bytes, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(byteOrder);
        if (bytes.length == 4) {
            return buf.getInt();
        } else {
            return buf.getShort();
        }
    }

    /**
     * Byte array convert to short integer
     *
     * @param bytes Byte array
     * @param byteOrder Byte order
     * @return Short integer value
     */
    public static short bytes2Short(byte[] bytes, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(byteOrder);
        return buf.getShort();
    }

    /**
     * Byte array convert to short integer
     *
     * @param bytes Byte array
     * @return Short integer value
     */
    public static short bytes2Short(byte[] bytes) {
        short sRet = 0;
        sRet += (bytes[0] & 0xFF) << 8;
        sRet += bytes[1] & 0xFF;
        return sRet;
    }

    /**
     * Byte array convert to unsigned short integer
     *
     * @param bytes Byte array
     * @return Unsigned short integer value
     */
    public static int bytes2UShort(byte[] bytes) {
        return (int)(
                (int)(bytes[1] & 0xff) << 8  |
                        (int)(bytes[0] & 0xff)
        );
    }

    /**
     * Byte array convert to unsigned short integer
     *
     * @param bytes Byte array
     * @param byteOrder Byte order
     * @return Unsigned short integer value
     */
    public static int bytes2UShort(byte[] bytes, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return bytes2UShort(new byte[]{bytes[1], bytes[0]});
        } else {
            return bytes2UShort(bytes);
        }
    }

    /**
     * Byte array convert to long integer
     *
     * @param bytes Byte array
     * @param byteOrder Byte order
     * @return Long integer value
     */
    public static Long bytes2Long(byte[] bytes, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(byteOrder);
        return buf.getLong();
    }

    /**
     * Byte array convert to integer
     *
     * @param bytes byte array
     * @return Integer value
     */
    public static int bytes2Int(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }

    /**
     * Byte array (2 bytes) convert to integer
     *
     * @param bytes byte array
     * @return Integer value
     */
    public static int bytes2Int2(byte[] bytes) {
        return bytes[1] & 0xFF |
                (bytes[0] & 0xFF) << 8;
    }

    /**
     * Byte array (3 bytes) convert to integer
     *
     * @param bytes Byte array
     * @return Integer value
     */
    public static int bytes2Int3(byte[] bytes) {
        int val = 0;
        for (int bb = 0; bb < 3; bb++) {
            val <<= 8;
            val |= (int) bytes[bb] & 0xFF;
        }
        return val;
    }

    /**
     * Convert byte to int - byte in Java is signed
     *
     * @param b Input byte
     * @return Output integer
     */
    public static int byte2Int(byte b) {
        return b >= 0 ? (int) b : (int) (b + 256);
    }

    /**
     * Convert LittleEndian to BigEndian
     *
     * @param bytes Input LittleEndian byte array
     * @return Output BigEndian byte array
     */
    public static byte[] littleToBig(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException();
        }
        byte[] temp = new byte[bytes.length];
        for (int i = bytes.length - 1; i >= 0; i--) {
            temp[i] = bytes[bytes.length - 1 - i];
        }
        return temp;
    }

    /**
     * Convert BigEndian to LittleEndian
     *
     * @param bytes Input BigEndian byte array
     * @return Output LittleEndian byte array
     */
    public static byte[] bigToLittle(byte[] bytes) {
        return littleToBig(bytes);
    }

    /**
     * Convert int to byte array.
     *
     * @param i Int value
     * @return Byte array
     */
    public static byte[] toBytes(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (i >> 24 & 0xff);
        bytes[1] = (byte) (i >> 16 & 0xff);
        bytes[2] = (byte) (i >> 8 & 0xff);
        bytes[3] = (byte) (i & 0xff);
        return bytes;
    }

    /**
     * Convert int to 3 byte array.
     *
     * @param i Int value
     * @return Byte array
     */
    public static byte[] toUint3Int(int i) {
        byte[] ints = new byte[3];
        ints[0] = (byte) (i >> 16 & 0xff);
        ints[1] = (byte) (i >> 8 & 0xff);
        ints[2] = (byte) (i & 0xff);
        return ints;
    }

    /**
     * Convert int to 2 byte array.
     *
     * @param i Int value
     * @return Byte array
     */
    public static byte[] toUint2Int(int i) {
        byte[] ints = new byte[2];
        ints[0] = (byte) (i >> 8 & 0xff);
        ints[1] = (byte) (i & 0xff);
        return ints;
    }

    /**
     * Convert int to N byte array.
     *
     * @param i Int value
     * @param n bit number
     * @return Byte array
     */
    public static byte[] toUintNInt(int i, int n) {
        byte[] ints = new byte[2];
        ints[0] = (byte) (i >> 8 & 0xff);
        ints[1] = (byte) (i & 0xff);
        return ints;
    }

    /**
     * Convert int to byte array - LittleEndian
     *
     * @param i Int value
     * @return Byte array
     */
    public static byte[] toLittleBytes(int i) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (i >> 24 & 0xff);
        bytes[2] = (byte) (i >> 16 & 0xff);
        bytes[1] = (byte) (i >> 8 & 0xff);
        bytes[0] = (byte) (i & 0xff);
        return bytes;
    }

    /**
     * Convert float to byte array
     *
     * @param f Float value
     * @return Byte array
     */
    public static byte[] toBytes(float f) {
        return toBytes(Float.floatToIntBits(f));
    }

    /**
     * Convert float to byte array
     *
     * @param f Float value
     * @param byteOrder ByteOrder
     * @return Byte array
     */
    public static byte[] float2Bytes(float f, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(byteOrder);
        buf.putFloat(f);
        return buf.array();
    }

    /**
     * Convert float to byte array
     *
     * @param f Float array
     * @return Byte array
     */
    public static byte[] toLittleBytes(float f) {
        return toLittleBytes(Float.floatToIntBits(f));
    }

    /**
     * Resize array
     *
     * @param oldArray Old array
     * @param newSize New size
     * @return Resized array
     */
    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(
                elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) {
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        }
        return newArray;
    }

    /**
     * Resize double 2d array
     *
     * @param oldArray Old array
     * @param newSize New size
     * @return Resized array
     */
    public static double[][] resizeArray2D(double[][] oldArray, int newSize) {
        int ynum = oldArray.length;
        int xnum = oldArray[0].length;
        double[][] newArray = new double[newSize][xnum];
        for (int j = 0; j < ynum; j++) {
            for (int i = 0; i < xnum; i++) {
                newArray[j][i] = oldArray[j][i];
            }
        }
        return newArray;
    }

    /**
     * Double to string
     *
     * @param v The double value
     * @return Result string
     */
    public static String doubleToString(double v) {
        BigDecimal a = new BigDecimal(Double.toString(v));
        a = a.setScale(12, BigDecimal.ROUND_HALF_UP);
        return a.stripTrailingZeros().toPlainString();
    }

    /**
     * Remove tail zero
     *
     * @param s The string
     * @return Result string
     */
    public static String removeTailingZeros(String s) {
        if (s.equals("0.0")) {
            s = "0";
        }
        if (s.length() <= 1) {
            return s;
        }
        if (s.substring(s.length() - 2).equals(".0")) {
            return new BigDecimal(s).stripTrailingZeros().toPlainString();
        } else {
            return s;
        }
    }
    
    /**
     * Remove last zero chars
     * @param str String
     * @return Result string
     */
    public static String removeTail0(String str){  
        // Return string if the last char is not 0  
        if(!str.substring(str.length() -1).equals("0")){  
            return str;  
        }else{  
             // Or remove last char and recursion  
            return removeTail0(str.substring(0, str.length() -1 ));  
        }  
    }  

    // Returns a byte array of at least length 1.
// The most significant bit in the result is guaranteed not to be a 1
// (since BitSet does not support sign extension).
// The byte-ordering of the result is big-endian which means the most significant bit is in element 0.
// The bit at index 0 of the bit set is assumed to be the least significant bit.
    public static byte[] toByte_bak1(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    public static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[i / 8] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    public static byte[] toByteArray_bak(BitSet bitSet) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bitSet.size());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(bitSet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return baos.toByteArray();
    }

    /**
     * Get date format string
     * @param format Format string
     * @return Date format string
     */
    public static String getDateFormat(String format) {
        int eidx = format.indexOf("}");
        String formatStr = format.substring(1, eidx);
        return formatStr;
    }

    /**
     * Check a string is double or not
     *
     * @param s The string
     * @return Boolean
     */
    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check a string is float or not
     *
     * @param s The string
     * @return Boolean
     */
    public static boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check a string is integer or not
     *
     * @param s The string
     * @return Boolean
     */
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check a string is boolean or not
     *
     * @param s The string
     * @return Boolean
     */
    public static boolean isBoolean(String s) {
        return TypeUtils.TRUE_STRINGS_FOR_DETECTION.contains(s) || TypeUtils.FALSE_STRINGS_FOR_DETECTION.contains(s);
    }

    /**
     * Check a string is local date or not
     *
     * @param s The string
     * @param dateTimeFormatter DateTimeFormatter
     * @return
     */
    public static boolean isLocalDate(String s, DateTimeFormatter dateTimeFormatter) {
        try {
            if (dateTimeFormatter == null) {
                LocalDate.parse(s, TypeUtils.DATE_FORMATTER);
                return true;
            } else {
                LocalDate.parse(s, dateTimeFormatter);
                return true;
            }
        } catch (Exception e) {
            // it's all part of the plan
            return false;
        }
    }

    /**
     * Check a string is local time or not
     *
     * @param s The string
     * @param formatter DateTimeFormatter
     * @return
     */
    public static boolean isLocalTime(String s, DateTimeFormatter formatter) {
        try {
            if (formatter == null) {
                LocalTime.parse(s, TypeUtils.TIME_DETECTION_FORMATTER);
                return true;
            } else {
                LocalDate.parse(s, formatter);
                return true;
            }
        } catch (Exception e) {
            // it's all part of the plan
            return false;
        }
    }

    /**
     * Check a string is local date time or not
     *
     * @param s The string
     * @param formatter DateTimeFormatter
     * @return
     */
    public static boolean isLocalDateTime(String s, DateTimeFormatter formatter) {
        try {
            if (formatter == null) {
                LocalDateTime.parse(s, TypeUtils.DATE_TIME_FORMATTER);
                return true;
            } else {
                LocalDate.parse(s, formatter);
                return true;
            }
        } catch (Exception e) {
            // it's all part of the plan
            return false;
        }
    }

}
