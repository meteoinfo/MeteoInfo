/*
 * Copyright 1998-2014 University Corporation for Atmospheric Research/Unidata
 *
 *   Portions of this software were developed by the Unidata Program at the
 *   University Corporation for Atmospheric Research.
 *
 *   Access and use of this software shall impose the following obligations
 *   and understandings on the user. The user is granted the right, without
 *   any fee or cost, to use, copy, modify, alter, enhance and distribute
 *   this software, and any derivative works thereof, and its supporting
 *   documentation for any purpose whatsoever, provided that this entire
 *   notice appears in all copies of the software, derivative works and
 *   supporting documentation.  Further, UCAR requests that the user credit
 *   UCAR/Unidata in any publications that result from the use of this
 *   software or in any product that includes this software. The names UCAR
 *   and/or Unidata, however, may not be used in any advertising or publicity
 *   to endorse or promote any products or commercial entity unless specific
 *   written permission is obtained from UCAR/Unidata. The user also
 *   understands that UCAR/Unidata is not obligated to provide the user with
 *   any support, consulting, training or assistance of any kind with regard
 *   to the use, operation and performance of this software nor to provide
 *   the user with any updates, revisions, new versions or "bug fixes."
 *
 *   THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *   IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *   INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *   FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *   NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *   WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.meteoinfo.ndarray;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

/**
 * Type-safe enumeration of data types.
 *
 * @author john caron
 */
public enum DataType {

    BOOLEAN("boolean", 1, boolean.class, false, 0),
    BYTE("byte", 1, byte.class, false, 1),
    CHAR("char", 1, char.class, false, 18),
    SHORT("short", 2, short.class, false, 3),
    INT("int", 4, int.class, false, 7),
    LONG("long", 8, long.class, false, 9),
    FLOAT("float", 4, float.class, false, 11),
    DOUBLE("double", 8, double.class, false, 12),
    COMPLEX("complex", 1, Complex.class, false, 15),
    DATE("date", 1, LocalDateTime.class, false, 21),
    
    // object types
    SEQUENCE("Sequence", 4, StructureDataIterator.class, false, 22), // 32-bit index
    STRING("String", 4, String.class, false, 19), // 32-bit index
    STRUCTURE("Structure", 1, StructureData.class, false, 23), // size meaningless

    ENUM1("enum1", 1, byte.class, false, 24), // byte
    ENUM2("enum2", 2, short.class, false, 25), // short
    ENUM4("enum4", 4, int.class, false, 26), // int

    OPAQUE("opaque", 1, ByteBuffer.class, false, 27), // byte blobs

    OBJECT("object", 1, Object.class, false, 30), // added for use with Array
    
    UBYTE("ubyte", 1, byte.class, true, 2),
    USHORT("ushort", 2, short.class, true, 4),
    UINT("uint", 4, int.class, true, 8),
    ULONG("ulong", 8, long.class, true, 10);
      
    /**
    * A property of {@link #isIntegral() integral} data types that determines whether they can represent both
    * positive and negative numbers (signed), or only non-negative numbers (unsigned).
    */
    public enum Signedness {
        /** The data type can represent both positive and negative numbers. */
        SIGNED,
        /** The data type can represent only non-negative numbers. */
        UNSIGNED
    }

    private final String niceName;
    private final int size;
    private final Class primitiveClass;
    private final Signedness signedness;
    private final int number;

    DataType(String s, int size, Class primitiveClass, boolean isUnsigned, int number) {
        this(s, size, primitiveClass, isUnsigned ? Signedness.UNSIGNED : Signedness.SIGNED, number);
    }

    DataType(String s, int size, Class primitiveClass, Signedness signedness, int number) {
        this.niceName = s;
        this.size = size;
        this.primitiveClass = primitiveClass;
        this.signedness = signedness;
        this.number = number;
    }

    /**
     * The DataType name, eg "byte", "float", "String".
     *
     * @return The DataType name
     */
    @Override
    public String toString() {
        return niceName;
    }

    /**
     * Size in bytes of one element of this data type. Strings dont know, so
     * return 0. Structures return 1.
     *
     * @return Size in bytes of one element of this data type.
     */
    public int getSize() {
        return size;
    }

    /*
  * The Object class type: Character, Byte, Float, Double, Short, Integer, Boolean, Long, String, StructureData.
  * @deprecated use getPrimitiveClassType()
  * @return the primitive class type
     */
    public Class getClassType() {
        return getPrimitiveClassType();
    }

    /**
     * The primitive class type: char, byte, float, double, short, int, long,
     * boolean, String, StructureData, StructureDataIterator, ByteBuffer.
     *
     * @return the primitive class type
     */
    public Class getPrimitiveClassType() {
        return primitiveClass;
    }
    
    /**
    * Returns the {@link Signedness signedness} of this data type.
    * For non-{@link #isIntegral() integral} data types, it is guaranteed to be {@link Signedness#SIGNED}.
    *
    * @return the signedness of this data type.
    */
    public Signedness getSignedness() {
        return signedness;
    }

    /**
     * Returns {@code true} if the data type is {@link Signedness#UNSIGNED unsigned}.
     * For non-{@link #isIntegral() integral} data types, it is guaranteed to be {@code false}.
     *
     * @return {@code true} if the data type is unsigned.
     */
    public boolean isUnsigned() {
        return signedness == Signedness.UNSIGNED;
    }

    /**
     * Returns A unique number for each of the different built-in types.
     * These are roughly ordered from least-to-most precision.
     *
     * @return
     */
    public int getNumber() {
        return number;
    }

    /**
     * Is String or Char
     *
     * @return true if String or Char
     */
    public boolean isString() {
        return (this == DataType.STRING) || (this == DataType.CHAR);
    }
    
    /**
     * Is Boolean
     * @return true if Boolean
     */
    public boolean isBoolean() {
        return this == DataType.BOOLEAN;
    }

    /**
     * Is Byte, Float, Double, Int, Short, or Long
     *
     * @return true if numeric
     */
    public boolean isNumeric() {
        return (this == DataType.BYTE) || (this == DataType.FLOAT) || (this == DataType.DOUBLE) || (this == DataType.INT)
                || (this == DataType.SHORT) || (this == DataType.LONG) || (this == DataType.COMPLEX);
    }

    /**
     * Is Byte, Int, Short, or Long
     *
     * @return true if integral
     */
    public boolean isIntegral() {
        return (this == DataType.BYTE) || (this == DataType.INT) || (this == DataType.SHORT) || (this == DataType.LONG);
    }

    /**
     * Is Float or Double
     *
     * @return true if floating point type
     */
    public boolean isFloatingPoint() {
        return (this == DataType.FLOAT) || (this == DataType.DOUBLE);
    }

    /**
     * Is this an enumeration types?
     *
     * @return true if ENUM1, 2, or 4
     */
    public boolean isEnum() {
        return (this == DataType.ENUM1) || (this == DataType.ENUM2) || (this == DataType.ENUM4);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Find the DataType that matches this name.
     *
     * @param name find DataType with this name.
     * @return DataType or null if no match.
     */
    public static DataType getType(String name) {
        if (name == null) {
            return null;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) { // lame!
            return null;
        }
    }

    /**
     * Find the DataType that matches this class.
     *
     * @param c primitive or object class, eg float.class or Float.class
     * @return DataType or null if no match.
     */
    public static DataType getType(Class c) {
        if ((c == float.class) || (c == Float.class)) {
            return DataType.FLOAT;
        }
        if ((c == double.class) || (c == Double.class)) {
            return DataType.DOUBLE;
        }
        if ((c == short.class) || (c == Short.class)) {
            return DataType.SHORT;
        }
        if ((c == int.class) || (c == Integer.class)) {
            return DataType.INT;
        }
        if ((c == byte.class) || (c == Byte.class)) {
            return DataType.BYTE;
        }
        if ((c == char.class) || (c == Character.class)) {
            return DataType.CHAR;
        }
        if ((c == boolean.class) || (c == Boolean.class)) {
            return DataType.BOOLEAN;
        }
        if ((c == long.class) || (c == Long.class)) {
            return DataType.LONG;
        }
        if (c == String.class) {
            return DataType.STRING;
        }
        if (c == Complex.class) {
            return DataType.COMPLEX;
        }
        if (c == LocalDateTime.class) {
            return DataType.DATE;
        }
        if (c == StructureData.class) {
            return DataType.STRUCTURE;
        }
        if (c == StructureDataIterator.class) {
            return DataType.SEQUENCE;
        }
        if (c == ByteBuffer.class) {
            return DataType.OPAQUE;
        }
        return null;
    }

    /**
     * Find the DataType that matches this class.
     *
     * @param c primitive or object class, eg float.class or Float.class
     * @return DataType or null if no match.
     */
    public static DataType getType(Class c, boolean isUnsigned) {
        if ((c == float.class) || (c == Float.class))
            return DataType.FLOAT;
        if ((c == double.class) || (c == Double.class))
            return DataType.DOUBLE;
        if ((c == short.class) || (c == Short.class))
            return isUnsigned ? DataType.USHORT : DataType.SHORT;
        if ((c == int.class) || (c == Integer.class))
            return isUnsigned ? DataType.UINT : DataType.INT;
        if ((c == byte.class) || (c == Byte.class))
            return isUnsigned ? DataType.UBYTE : DataType.BYTE;
        if ((c == char.class) || (c == Character.class))
            return DataType.CHAR;
        if ((c == boolean.class) || (c == Boolean.class))
            return DataType.BOOLEAN;
        if ((c == long.class) || (c == Long.class))
            return isUnsigned ? DataType.ULONG : DataType.LONG;
        if (c == String.class)
            return DataType.STRING;
        if (c == StructureData.class)
            return DataType.STRUCTURE;
        if (c == StructureDataIterator.class)
            return DataType.SEQUENCE;
        if (c == ByteBuffer.class)
            return DataType.OPAQUE;
        return DataType.OBJECT;
    }

    /**
     * convert an unsigned long to a String
     *
     * @param li unsigned int
     * @return equivalent long value
     */
    static public String unsignedLongToString(long li) {
        if (li >= 0) {
            return Long.toString(li);
        }

        // else do the hard part - see http://technologicaloddity.com/2010/09/22/biginteger-as-unsigned-long-in-java/
        byte[] val = new byte[8];
        for (int i = 0; i < 8; i++) {
            val[7 - i] = (byte) ((li) & 0xFF);
            li = li >>> 8;
        }

        BigInteger biggy = new BigInteger(1, val);
        return biggy.toString();
    }

    /**
     * widen an unsigned int to a long
     *
     * @param i unsigned int
     * @return equivalent long value
     */
    static public long unsignedIntToLong(int i) {
        return (i < 0) ? (long) i + 4294967296L : (long) i;
    }

    /**
     * widen an unsigned short to an int
     *
     * @param s unsigned short
     * @return equivalent int value
     */
    static public int unsignedShortToInt(short s) {
        return (s & 0xffff);
    }

    /**
     * widen an unsigned byte to a short
     *
     * @param b unsigned byte
     * @return equivalent short value
     */
    static public short unsignedByteToShort(byte b) {
        return (short) (b & 0xff);
    }
    //      return (short)((b<0)? (short)b + 256 : (short)b);

    public static void main(String[] args) {
        for (int i = 0; i < 260; i++) {
            byte b = (byte) i;
            System.out.printf("%4d = %4d%n", b, unsignedByteToShort(b));
        }
    }

}
