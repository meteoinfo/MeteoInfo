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
import java.nio.IntBuffer;
import java.time.LocalDateTime;

/**
 * Concrete implementation of Array specialized for ints. Data storage is with
 * 1D java array of ints.
 * <p/>
 * issues: what should we do if a conversion loses accuracy? nothing ? Exception
 * ?
 *
 * @author caron
 * @see Array
 */
public class ArrayInt extends Array {

    // package private. use Array.factory()
    static ArrayInt factory(Index index, boolean isUnsigned) {
        return ArrayInt.factory(index, isUnsigned, null);
    }

    /* create new ArrayInt with given indexImpl and backing store.
   * Should be private.
   * @param index use this Index
   * @param isUnsigned Unsigned or not
   * @param storage use this storage. if null, allocate.
   * @return. new ArrayInt.D<rank> or ArrayInt object.
     */
    static ArrayInt factory(Index index, boolean isUnsigned, int[] storage) {
        switch (index.getRank()) {
            case 0:
                return new D0(index, isUnsigned, storage);
            case 1:
                return new D1(index, isUnsigned, storage);
            case 2:
                return new D2(index, isUnsigned, storage);
            case 3:
                return new D3(index, isUnsigned, storage);
            case 4:
                return new D4(index, isUnsigned, storage);
            case 5:
                return new D5(index, isUnsigned, storage);
            case 6:
                return new D6(index, isUnsigned, storage);
            case 7:
                return new D7(index, isUnsigned, storage);
            default:
                return new ArrayInt(index, isUnsigned, storage);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    protected int[] storage;

    /**
     * Create a new Array of type int and the given shape. dimensions.length
     * determines the rank of the new Array.
     *
     * @param dimensions the shape of the Array.
     */
    public ArrayInt(int[] dimensions, boolean isUnsigned) {
        super(isUnsigned ? DataType.UINT : DataType.INT, dimensions);
        storage = new int[(int) indexCalc.getSize()];
    }

    /**
     * Create a new Array using the given IndexArray and backing store. used for
     * sections. Trusted package private.
     *
     * @param ima use this IndexArray as the index
     * @param isUnsigned Unsigned or not
     * @param data use this as the backing store
     */
    ArrayInt(Index ima, boolean isUnsigned, int[] data) {
        super(isUnsigned ? DataType.UINT : DataType.INT, ima);
        /*
         * replace by something better
         * if (ima.getSize() != data.length)
         * throw new IllegalArgumentException("bad data length");
         */
        if (data != null)
            storage = data;
        else
            storage = new int[(int) ima.getSize()];
    }

    /**
     * create new Array with given indexImpl and same backing store
     */
    protected Array createView(Index index) {
        return ArrayInt.factory(index, isUnsigned(), storage);
    }

    /* Get underlying primitive array storage. CAUTION! You may invalidate your warrentee! */
    public Object getStorage() {
        return storage;
    }

    // copy from javaArray to storage using the iterator: used by factory( Object);
    protected void copyFrom1DJavaArray(IndexIterator iter, Object javaArray) {
        int[] ja = (int[]) javaArray;
        for (int aJa : ja) {
            iter.setIntNext(aJa);
        }
    }

    // copy to javaArray from storage using the iterator: used by copyToNDJavaArray;
    protected void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
        int[] ja = (int[]) javaArray;
        for (int i = 0; i < ja.length; i++) {
            ja[i] = iter.getIntNext();
        }
    }

    public ByteBuffer getDataAsByteBuffer() {
        return getDataAsByteBuffer(null);
    }

    public ByteBuffer getDataAsByteBuffer(ByteOrder order) {
        ByteBuffer bb = super.getDataAsByteBuffer((int) (4 * getSize()), order);
        IntBuffer ib = bb.asIntBuffer();
        ib.put((int[]) get1DJavaArray(int.class)); // make sure its in canonical order
        return bb;
    }

    /**
     * Return the element class type
     */
    public Class getElementType() {
        return int.class;
    }

    /**
     * Get the value at the specified index.
     *
     * @param i the index
     * @return the value at the specified index.
     */
    public int get(Index i) {
        return storage[i.currentElement()];
    }

    /**
     * Set the value at the specified index.
     *
     * @param i the index
     * @param value set to this value
     */
    public void set(Index i, int value) {
        storage[i.currentElement()] = value;
    }

    public double getDouble(Index i) {
        int val = storage[i.currentElement()];
        return (double) (isUnsigned() ? DataType.unsignedIntToLong(val) : val);
    }

    public void setDouble(Index i, double value) {
        storage[i.currentElement()] = (int) value;
    }

    public float getFloat(Index i) {
        int val = storage[i.currentElement()];
        return (float) (isUnsigned() ? DataType.unsignedIntToLong(val) : val);
    }

    public void setFloat(Index i, float value) {
        storage[i.currentElement()] = (int) value;
    }

    public long getLong(Index i) {
        int val = storage[i.currentElement()];
        return (isUnsigned() ? DataType.unsignedIntToLong(val) : val);
    }

    public void setLong(Index i, long value) {
        storage[i.currentElement()] = (int) value;
    }

    public int getInt(Index i) {
        return storage[i.currentElement()];
    }

    public void setInt(Index i, int value) {
        storage[i.currentElement()] = value;
    }

    public short getShort(Index i) {
        return (short) storage[i.currentElement()];
    }

    public void setShort(Index i, short value) {
        storage[i.currentElement()] = (int) value;
    }

    public byte getByte(Index i) {
        return (byte) storage[i.currentElement()];
    }

    public void setByte(Index i, byte value) {
        storage[i.currentElement()] = (int) value;
    }

    public char getChar(Index i) {
        return (char) storage[i.currentElement()];
    }

    public void setChar(Index i, char value) {
        storage[i.currentElement()] = (int) value;
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
        int v = this.storage[i.currentElement()];
        return isUnsigned() ? String.valueOf(DataType.unsignedIntToLong(v)) :
                String.valueOf(v);
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setString(Index i, String value) {
        this.storage[i.currentElement()] = Integer.parseInt(value);
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public Complex getComplex(Index i) {
        return new Complex(getDouble(i), 0);
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setComplex(Index i, Complex value) {
        throw new ForbiddenConversionException();
    }

    public LocalDateTime getDate(Index i) { throw new ForbiddenConversionException(); }

    public void setDate(Index i, LocalDateTime value) { throw new ForbiddenConversionException(); }

    public Object getObject(Index i) {
        int val = storage[i.currentElement()];
        return (int) (isUnsigned() ? DataType.unsignedIntToLong(val) : val);
    }

    public void setObject(Index i, Object value) {
        storage[i.currentElement()] = ((Number) value).intValue();
    }

    // package private : mostly for iterators
    public double getDouble(int index) {
        int val = storage[index];
        return (double) (isUnsigned() ? DataType.unsignedIntToLong(val) : val);
    }

    public void setDouble(int index, double value) {
        storage[index] = (int) value;
    }

    public float getFloat(int index) {
        int val = storage[index];
        return (float) (isUnsigned() ? DataType.unsignedIntToLong(val) : val);
    }

    public void setFloat(int index, float value) {
        storage[index] = (int) value;
    }

    public long getLong(int index) {
        int val = storage[index];
        return (isUnsigned() ? DataType.unsignedIntToLong(val) : val);
    }

    public void setLong(int index, long value) {
        storage[index] = (int) value;
    }

    public int getInt(int index) {
        return storage[index];
    }

    public void setInt(int index, int value) {
        storage[index] = value;
    }

    public short getShort(int index) {
        return (short) storage[index];
    }

    public void setShort(int index, short value) {
        storage[index] = (int) value;
    }

    public byte getByte(int index) {
        return (byte) storage[index];
    }

    public void setByte(int index, byte value) {
        storage[index] = (int) value;
    }

    public char getChar(int index) {
        return (char) storage[index];
    }

    public void setChar(int index, char value) {
        storage[index] = (int) value;
    }

    public boolean getBoolean(int index) {
        return storage[index] != 0;
    }

    public void setBoolean(int index, boolean value) {
        storage[index] = value ? 1 : 0;
    }

    public String getString(int index) {
        int v = this.storage[index];
        return isUnsigned() ? String.valueOf(DataType.unsignedIntToLong(v)) :
                String.valueOf(v);
    }

    public void setString(int index, String value) {
        this.storage[index] = Integer.parseInt(value);
    }

    public Complex getComplex(int index) {
        return new Complex(getDouble(index), 0);
    }

    public void setComplex(int index, Complex value) {
        throw new ForbiddenConversionException();
    }

    public LocalDateTime getDate(int index) { throw new ForbiddenConversionException(); }

    public void setDate(int index, LocalDateTime value) { throw new ForbiddenConversionException(); }

    public Object getObject(int index) {
        int val = storage[index];
        return (int) (isUnsigned() ? DataType.unsignedIntToLong(val) : val);
    }

    public void setObject(int index, Object value) {
        storage[index] = ((Number) value).intValue();
    }

    /**
     * Concrete implementation of Array specialized for ints, rank 0.
     */
    public static class D0 extends ArrayInt {

        private Index0D ix;

        public D0(boolean isUnsigned) {
            super(new int[]{}, isUnsigned);
            ix = (Index0D) indexCalc;
        }

        private D0(Index i, boolean isUnsigned, int[] store) {
            super(i, isUnsigned, store);
            ix = (Index0D) indexCalc;
        }

        public int get() {
            return storage[ix.currentElement()];
        }

        public void set(int value) {
            storage[ix.currentElement()] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for ints, rank 1.
     */
    public static class D1 extends ArrayInt {

        private Index1D ix;

        public D1(int len0, boolean isUnsigned) {
            super(new int[]{len0}, isUnsigned);
            ix = (Index1D) indexCalc;
        }

        private D1(Index i, boolean isUnsigned, int[] store) {
            super(i, isUnsigned, store);
            ix = (Index1D) indexCalc;
        }

        public int get(int i) {
            return storage[ix.setDirect(i)];
        }

        public void set(int i, int value) {
            storage[ix.setDirect(i)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for ints, rank 2.
     */
    public static class D2 extends ArrayInt {

        private Index2D ix;

        public D2(int len0, int len1, boolean isUnsigned) {
            super(new int[]{len0, len1}, isUnsigned);
            ix = (Index2D) indexCalc;
        }

        private D2(Index i, boolean isUnsigned, int[] store) {
            super(i, isUnsigned, store);
            ix = (Index2D) indexCalc;
        }

        public int get(int i, int j) {
            return storage[ix.setDirect(i, j)];
        }

        public void set(int i, int j, int value) {
            storage[ix.setDirect(i, j)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for ints, rank 3.
     */
    public static class D3 extends ArrayInt {

        private Index3D ix;

        public D3(int len0, int len1, int len2, boolean isUnsigned) {
            super(new int[]{len0, len1, len2}, isUnsigned);
            ix = (Index3D) indexCalc;
        }

        private D3(Index i, boolean isUnsigned, int[] store) {
            super(i, isUnsigned, store);
            ix = (Index3D) indexCalc;
        }

        public int get(int i, int j, int k) {
            return storage[ix.setDirect(i, j, k)];
        }

        public void set(int i, int j, int k, int value) {
            storage[ix.setDirect(i, j, k)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for ints, rank 4.
     */
    public static class D4 extends ArrayInt {

        private Index4D ix;

        public D4(int len0, int len1, int len2, int len3, boolean isUnsigned) {
            super(new int[]{len0, len1, len2, len3}, isUnsigned);
            ix = (Index4D) indexCalc;
        }

        private D4(Index i, boolean isUnsigned, int[] store) {
            super(i, isUnsigned, store);
            ix = (Index4D) indexCalc;
        }

        public int get(int i, int j, int k, int l) {
            return storage[ix.setDirect(i, j, k, l)];
        }

        public void set(int i, int j, int k, int l, int value) {
            storage[ix.setDirect(i, j, k, l)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for ints, rank 5.
     */
    public static class D5 extends ArrayInt {

        private Index5D ix;

        public D5(int len0, int len1, int len2, int len3, int len4, boolean isUnsigned) {
            super(new int[]{len0, len1, len2, len3, len4}, isUnsigned);
            ix = (Index5D) indexCalc;
        }

        private D5(Index i, boolean isUnsigned, int[] store) {
            super(i, isUnsigned, store);
            ix = (Index5D) indexCalc;
        }

        public int get(int i, int j, int k, int l, int m) {
            return storage[ix.setDirect(i, j, k, l, m)];
        }

        public void set(int i, int j, int k, int l, int m, int value) {
            storage[ix.setDirect(i, j, k, l, m)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for ints, rank 6.
     */
    public static class D6 extends ArrayInt {

        private Index6D ix;

        public D6(int len0, int len1, int len2, int len3, int len4, int len5, boolean isUnsigned) {
            super(new int[]{len0, len1, len2, len3, len4, len5}, isUnsigned);
            ix = (Index6D) indexCalc;
        }

        private D6(Index i, boolean isUnsigned, int[] store) {
            super(i, isUnsigned, store);
            ix = (Index6D) indexCalc;
        }

        public int get(int i, int j, int k, int l, int m, int n) {
            return storage[ix.setDirect(i, j, k, l, m, n)];
        }

        public void set(int i, int j, int k, int l, int m, int n, int value) {
            storage[ix.setDirect(i, j, k, l, m, n)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for ints, rank 7.
     */
    public static class D7 extends ArrayInt {

        private Index7D ix;

        public D7(int len0, int len1, int len2, int len3, int len4, int len5, int len6, boolean isUnsigned) {
            super(new int[]{len0, len1, len2, len3, len4, len5, len6}, isUnsigned);
            ix = (Index7D) indexCalc;
        }

        private D7(Index i, boolean isUnsigned, int[] store) {
            super(i, isUnsigned, store);
            ix = (Index7D) indexCalc;
        }

        public int get(int i, int j, int k, int l, int m, int n, int o) {
            return storage[ix.setDirect(i, j, k, l, m, n, o)];
        }

        public void set(int i, int j, int k, int l, int m, int n, int o, int value) {
            storage[ix.setDirect(i, j, k, l, m, n, o)] = value;
        }
    }

}
