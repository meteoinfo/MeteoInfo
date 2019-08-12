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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Concrete implementation of Array specialized for bytes. Data storage is with
 * 1D java array of bytes.
 * <p/>
 * issues: what should we do if a conversion loses accuracy? nothing ? Exception
 * ?
 *
 * @author caron
 * @see Array
 */
public class ArrayByte extends Array {

    // package private. use Array.factory() */
    static ArrayByte factory(Index index) {
        return ArrayByte.factory(index, null);
    }

    /* create new ArrayByte with given Index and backing store.
   * Should be private.
   * @param index use this Index
   * @param storage use this storage. if null, allocate.
   * @return new ArrayByte.D<rank> or ArrayByte object.
     */
    static ArrayByte factory(Index index, byte[] storage) {
        switch (index.getRank()) {
            case 0:
                return new ArrayByte.D0(index, storage);
            case 1:
                return new ArrayByte.D1(index, storage);
            case 2:
                return new ArrayByte.D2(index, storage);
            case 3:
                return new ArrayByte.D3(index, storage);
            case 4:
                return new ArrayByte.D4(index, storage);
            case 5:
                return new ArrayByte.D5(index, storage);
            case 6:
                return new ArrayByte.D6(index, storage);
            case 7:
                return new ArrayByte.D7(index, storage);
            default:
                return new ArrayByte(index, storage);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    protected byte[] storage;

    /**
     * Create a new Array of type byte and the given shape. dimensions.length
     * determines the rank of the new Array.
     *
     * @param dimensions the shape of the Array.
     */
    public ArrayByte(int[] dimensions) {
        super(dimensions);
        storage = new byte[(int) indexCalc.getSize()];
    }

    /**
     * Create a new Array using the given IndexArray and backing store. used for
     * sections. Trusted package private.
     *
     * @param ima use this IndexArray as the index
     * @param data use this as the backing store
     */
    ArrayByte(Index ima, byte[] data) {
        super(ima);
        /* replace by something better
    if (ima.getSize() != data.length)
      throw new IllegalArgumentException("bad data length");  */
        if (data != null) {
            storage = data;
        } else {
            storage = new byte[(int) ima.getSize()];
        }
    }

    protected Array createView(Index index) {
        Array result = ArrayByte.factory(index, storage);
        result.setUnsigned(isUnsigned());
        return result;
    }

    public Object getStorage() {
        return storage;
    }

    // copy from javaArray to storage using the iterator: used by factory( Object);
    protected void copyFrom1DJavaArray(IndexIterator iter, Object javaArray) {
        byte[] ja = (byte[]) javaArray;
        for (byte aJa : ja) {
            iter.setByteNext(aJa);
        }
    }

    // copy to javaArray from storage using the iterator: used by copyToNDJavaArray;
    protected void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
        byte[] ja = (byte[]) javaArray;
        for (int i = 0; i < ja.length; i++) {
            ja[i] = iter.getByteNext();
        }
    }

    @Override
    public ByteBuffer getDataAsByteBuffer() {
        return getDataAsByteBuffer(null);
    }

    public ByteBuffer getDataAsByteBuffer(ByteOrder order) {// order irrelevant here
        return ByteBuffer.wrap((byte[]) get1DJavaArray(byte.class));
    }

    /**
     * Return the element class type
     */
    public Class getElementType() {
        return byte.class;
    }

    /**
     * get the value at the specified index.
     *
     * @param i index
     * @return byte value
     */
    public byte get(Index i) {
        return storage[i.currentElement()];
    }

    /**
     * set the value at the sepcified index.
     *
     * @param i index
     * @param value set to this value
     */
    public void set(Index i, byte value) {
        storage[i.currentElement()] = value;
    }

    public double getDouble(Index i) {
        byte val = storage[i.currentElement()];
        return (double) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setDouble(Index i, double value) {
        storage[i.currentElement()] = (byte) value;
    }

    public float getFloat(Index i) {
        byte val = storage[i.currentElement()];
        return (float) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setFloat(Index i, float value) {
        storage[i.currentElement()] = (byte) value;
    }

    public long getLong(Index i) {
        byte val = storage[i.currentElement()];
        return (long) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setLong(Index i, long value) {
        storage[i.currentElement()] = (byte) value;
    }

    public int getInt(Index i) {
        byte val = storage[i.currentElement()];
        return (int) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setInt(Index i, int value) {
        storage[i.currentElement()] = (byte) value;
    }

    public short getShort(Index i) {
        byte val = storage[i.currentElement()];
        return (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setShort(Index i, short value) {
        storage[i.currentElement()] = (byte) value;
    }

    public byte getByte(Index i) {
        return storage[i.currentElement()];
    }

    public void setByte(Index i, byte value) {
        storage[i.currentElement()] = value;
    }

    public char getChar(Index i) {
        byte val = storage[i.currentElement()];
        return (char) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setChar(Index i, char value) {
        storage[i.currentElement()] = (byte) value;
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public boolean getBoolean(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setBoolean(Index i, boolean value) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public String getString(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setString(Index i, String value) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public Complex getComplex(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setComplex(Index i, Complex value) {
        throw new ForbiddenConversionException();
    }

    public Object getObject(Index i) {
        return storage[i.currentElement()];
    }

    public void setObject(Index i, Object value) {
        storage[i.currentElement()] = ((Number) value).byteValue();
    }

    // package private : mostly for iterators
    public double getDouble(int index) {
        byte val = storage[index];
        return (double) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setDouble(int index, double value) {
        storage[index] = (byte) value;
    }

    public float getFloat(int index) {
        byte val = storage[index];
        return (float) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setFloat(int index, float value) {
        storage[index] = (byte) value;
    }

    public long getLong(int index) {
        byte val = storage[index];
        return (long) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setLong(int index, long value) {
        storage[index] = (byte) value;
    }

    public int getInt(int index) {
        byte val = storage[index];
        return (int) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setInt(int index, int value) {
        storage[index] = (byte) value;
    }

    public short getShort(int index) {
        byte val = storage[index];
        return (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setShort(int index, short value) {
        storage[index] = (byte) value;
    }

    public byte getByte(int index) {
        return storage[index];
    }

    public void setByte(int index, byte value) {
        storage[index] = value;
    }

    public char getChar(int index) {
        byte val = storage[index];
        return (char) (unsigned ? DataType.unsignedByteToShort(val) : val);
    }

    public void setChar(int index, char value) {
        storage[index] = (byte) value;
    }

    public boolean getBoolean(int index) {
        throw new ForbiddenConversionException();
    }

    public void setBoolean(int index, boolean value) {
        throw new ForbiddenConversionException();
    }

    public String getString(int index) {
        throw new ForbiddenConversionException();
    }

    public void setString(int index, String value) {
        throw new ForbiddenConversionException();
    }

    public Complex getComplex(int index) {
        throw new ForbiddenConversionException();
    }

    public void setComplex(int index, Complex value) {
        throw new ForbiddenConversionException();
    }

    public Object getObject(int index) {
        return getByte(index);
    }

    public void setObject(int index, Object value) {
        storage[index] = ((Number) value).byteValue();
    }

    /**
     * Concrete implementation of Array specialized for byte, rank 0.
     */
    public static class D0 extends ArrayByte {

        private Index0D ix;

        public D0() {
            super(new int[]{});
            ix = (Index0D) indexCalc;
        }

        private D0(Index i, byte[] store) {
            super(i, store);
            ix = (Index0D) indexCalc;
        }

        public byte get() {
            return storage[ix.currentElement()];
        }

        public void set(byte value) {
            storage[ix.currentElement()] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for byte, rank 1.
     */
    public static class D1 extends ArrayByte {

        private Index1D ix;

        public D1(int len0) {
            super(new int[]{len0});
            ix = (Index1D) indexCalc;
        }

        private D1(Index i, byte[] store) {
            super(i, store);
            ix = (Index1D) indexCalc;
        }

        public byte get(int i) {
            return storage[ix.setDirect(i)];
        }

        public void set(int i, byte value) {
            storage[ix.setDirect(i)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for byte, rank 2.
     */
    public static class D2 extends ArrayByte {

        private Index2D ix;

        public D2(int len0, int len1) {
            super(new int[]{len0, len1});
            ix = (Index2D) indexCalc;
        }

        private D2(Index i, byte[] store) {
            super(i, store);
            ix = (Index2D) indexCalc;
        }

        public byte get(int i, int j) {
            return storage[ix.setDirect(i, j)];
        }

        public void set(int i, int j, byte value) {
            storage[ix.setDirect(i, j)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for byte, rank 3.
     */
    public static class D3 extends ArrayByte {

        private Index3D ix;

        public D3(int len0, int len1, int len2) {
            super(new int[]{len0, len1, len2});
            ix = (Index3D) indexCalc;
        }

        private D3(Index i, byte[] store) {
            super(i, store);
            ix = (Index3D) indexCalc;
        }

        public byte get(int i, int j, int k) {
            return storage[ix.setDirect(i, j, k)];
        }

        public void set(int i, int j, int k, byte value) {
            storage[ix.setDirect(i, j, k)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for byte, rank 4.
     */
    public static class D4 extends ArrayByte {

        private Index4D ix;

        public D4(int len0, int len1, int len2, int len3) {
            super(new int[]{len0, len1, len2, len3});
            ix = (Index4D) indexCalc;
        }

        private D4(Index i, byte[] store) {
            super(i, store);
            ix = (Index4D) indexCalc;
        }

        public byte get(int i, int j, int k, int l) {
            return storage[ix.setDirect(i, j, k, l)];
        }

        public void set(int i, int j, int k, int l, byte value) {
            storage[ix.setDirect(i, j, k, l)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for byte, rank 5.
     */
    public static class D5 extends ArrayByte {

        private Index5D ix;

        public D5(int len0, int len1, int len2, int len3, int len4) {
            super(new int[]{len0, len1, len2, len3, len4});
            ix = (Index5D) indexCalc;
        }

        private D5(Index i, byte[] store) {
            super(i, store);
            ix = (Index5D) indexCalc;
        }

        public byte get(int i, int j, int k, int l, int m) {
            return storage[ix.setDirect(i, j, k, l, m)];
        }

        public void set(int i, int j, int k, int l, int m, byte value) {
            storage[ix.setDirect(i, j, k, l, m)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for byte, rank 6.
     */
    public static class D6 extends ArrayByte {

        private Index6D ix;

        public D6(int len0, int len1, int len2, int len3, int len4, int len5) {
            super(new int[]{len0, len1, len2, len3, len4, len5});
            ix = (Index6D) indexCalc;
        }

        private D6(Index i, byte[] store) {
            super(i, store);
            ix = (Index6D) indexCalc;
        }

        public byte get(int i, int j, int k, int l, int m, int n) {
            return storage[ix.setDirect(i, j, k, l, m, n)];
        }

        public void set(int i, int j, int k, int l, int m, int n, byte value) {
            storage[ix.setDirect(i, j, k, l, m, n)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for byte, rank 7.
     */
    public static class D7 extends ArrayByte {

        protected Index7D ix;

        public D7(int len0, int len1, int len2, int len3, int len4, int len5, int len6) {
            super(new int[]{len0, len1, len2, len3, len4, len5, len6});
            ix = (Index7D) indexCalc;
        }

        private D7(Index i, byte[] store) {
            super(i, store);
            ix = (Index7D) indexCalc;
        }

        public byte get(int i, int j, int k, int l, int m, int n, int o) {
            return storage[ix.setDirect(i, j, k, l, m, n, o)];
        }

        public void set(int i, int j, int k, int l, int m, int n, int o, byte value) {
            storage[ix.setDirect(i, j, k, l, m, n, o)] = value;
        }
    }

}
