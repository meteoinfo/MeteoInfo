package org.meteoinfo.ndarray.io.npy;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NpyUtil {

    private NpyUtil() {
    }

    static short u1ToShort(byte b) {
        return (short) (b & (short) 0xff);
    }

    static short u1ToShort(ByteBuffer buffer) {
        return (short) (buffer.get() & (short) 0xff);
    }

    static int u2ToInt(byte[] bytes, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, 2)
                .order(order);
        return u2ToInt(buffer);
    }

    static int u2ToInt(ByteBuffer buffer) {
        short s = buffer.getShort();
        return s & 0xffff;
    }

    static long u4ToLong(byte[] bytes, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, 4)
                .order(order);
        return u4ToLong(buffer);
    }

    static long u4ToLong(ByteBuffer buffer) {
        int i = buffer.getInt();
        return i & 0xffffffffL;
    }

    static BigInteger u8ToBigInteger(ByteBuffer buffer) {
        long i = buffer.getLong();
        if (i >= 0L)
            return BigInteger.valueOf(i);
        BigInteger upper = BigInteger.valueOf(Integer.toUnsignedLong((int) (i >>> 32)));
        BigInteger lower = BigInteger.valueOf(Integer.toUnsignedLong((int) i));
        return upper.shiftLeft(32).add(lower);
    }

    static float f2ToFloat(ByteBuffer buffer) {
        return toFloat(buffer.getShort() & 0xffff);
    }

    /**
     * Converts a 16 bit floating point number to a 32 bit floating point number.
     * The 16 bits are stored in the given integer parameter, the higher 16 bits
     * are ignored. This function was directly taken from here:
     * https://stackoverflow.com/a/6162687.
     */
    private static float toFloat(int hbits) {
        int mant = hbits & 0x03ff;           // 10 bits mantissa
        int exp = hbits & 0x7c00;            // 5 bits exponent
        if (exp == 0x7c00)                   // NaN/Inf
            exp = 0x3fc00;                     // -> NaN/Inf
        else if (exp != 0)                   // normalized value
        {
            exp += 0x1c000;                   // exp - 15 + 127
            if (mant == 0 && exp > 0x1c400)   // smooth transition
                return Float.intBitsToFloat((hbits & 0x8000) << 16
                        | exp << 13 | 0x3ff);
        } else if (mant != 0)                  // && exp==0 -> subnormal
        {
            exp = 0x1c400;                    // make it normal
            do {
                mant <<= 1;                     // mantissa * 2
                exp -= 0x400;                   // decrease exp by 1
            } while ((mant & 0x400) == 0);    // while not normal
            mant &= 0x3ff;                    // discard subnormal bit
        }                                     // else +/-0 -> +/-0
        return Float.intBitsToFloat(          // combine all parts
                (hbits & 0x8000) << 16          // sign  << ( 31 - 15 )
                        | (exp | mant) << 13);          // value << ( 23 - 10 )
    }

    /**
     * Convert Npy data type to MeteoInfo data type
     *
     * @param npyDataType Npy data type
     * @return MeteoInfo data type
     */
    public static DataType toMIDataType(NpyDataType npyDataType) {
        switch (npyDataType) {
            case i1:
                return DataType.BYTE;
            case i2:
                return DataType.SHORT;
            case i4:
                return DataType.INT;
            case i8:
                return DataType.LONG;
            case f2:
            case f4:
                return DataType.FLOAT;
            case f8:
                return DataType.DOUBLE;
            case u1:
                return DataType.UBYTE;
            case u2:
                return DataType.USHORT;
            case u4:
                return DataType.UINT;
            case u8:
                return DataType.ULONG;
            case bool:
                return DataType.BOOLEAN;
            case S:
            case U:
                return DataType.STRING;
            default:
                return DataType.OBJECT;
        }
    }

    /**
     * Convert MeteoInfo data type to npy data type
     *
     * @param dataType MeteoInfo data type
     * @return Npy data type
     */
    public static NpyDataType toNpyDataType(DataType dataType) {
        switch (dataType) {
            case BYTE:
                return NpyDataType.i1;
            case SHORT:
                return NpyDataType.i2;
            case INT:
                return NpyDataType.i4;
            case LONG:
                return NpyDataType.i8;
            case FLOAT:
                return NpyDataType.f4;
            case DOUBLE:
                return NpyDataType.f8;
            case UBYTE:
                return NpyDataType.u1;
            case USHORT:
                return NpyDataType.u2;
            case UINT:
                return NpyDataType.u4;
            case ULONG:
                return NpyDataType.u8;
            case BOOLEAN:
                return NpyDataType.bool;
            case STRING:
                return NpyDataType.S;
            default:
                throw new NpyFormatException("Unsupported data type: " + dataType);
        }
    }

    /**
     * Convert Npy array to MeteoInfo array
     *
     * @param npyArray Npy array
     * @return MeteoInfo array
     */
    public static Array toMIArray(NpyArray npyArray) {
        DataType dataType = toMIDataType(npyArray.dataType());
        int[] shape = npyArray.shape();
        Array array = Array.factory(dataType, shape);
        for (int i = 0; i < array.getSize(); i++) {
            array.setObject(i, npyArray.getElement(i));
        }

        return array;
    }
}
