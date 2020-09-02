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
import java.nio.FloatBuffer;
import java.time.LocalDateTime;

/**
 * Concrete implementation of Array specialized for floats. Data storage is with
 * 1D java array of floats.
 *
 * issues: what should we do if a conversion loses accuracy? nothing ? Exception
 * ?
 *
 * @see Array
 * @author caron
 */
public class ArrayFloat extends Array {

    /**
     * package private. use Array.factory()
     */
    static ArrayFloat factory(Index index) {
        return ArrayFloat.factory(index, null);
    }

    /* create new ArrayFloat with given indexImpl and backing store.
   * Should be private.
   * @param index use this Index
   * @param stor. use this storage. if null, allocate.
   * @return. new ArrayFloat.D<rank> or ArrayFloat object.
     */
    static ArrayFloat factory(Index index, float[] storage) {
        switch (index.getRank()) {
            case 0:
                return new ArrayFloat.D0(index, storage);
            case 1:
                return new ArrayFloat.D1(index, storage);
            case 2:
                return new ArrayFloat.D2(index, storage);
            case 3:
                return new ArrayFloat.D3(index, storage);
            case 4:
                return new ArrayFloat.D4(index, storage);
            case 5:
                return new ArrayFloat.D5(index, storage);
            case 6:
                return new ArrayFloat.D6(index, storage);
            case 7:
                return new ArrayFloat.D7(index, storage);
            default:
                return new ArrayFloat(index, storage);
        }
    }

    //////////////////////////////////////////////////////
    protected float[] storage;

    /**
     * Create a new Array of type float and the given shape. dimensions.length
     * determines the rank of the new Array.
     *
     * @param dimensions the shape of the Array.
     */
    public ArrayFloat(int[] dimensions) {
        super(DataType.FLOAT, dimensions);
        storage = new float[(int) indexCalc.getSize()];
    }

    /**
     * Create a new Array using the given IndexArray and backing store. used for
     * sections. Trusted package private.
     *
     * @param ima use this IndexArray as the index
     * @param data use this as the backing store
     */
    ArrayFloat(Index ima, float[] data) {
        super(DataType.FLOAT, ima);
        /* replace by something better
    if (ima.getSize() != data.length)
      throw new IllegalArgumentException("bad data length");  */
        if (data != null) {
            storage = data;
        } else {
            storage = new float[(int) ima.getSize()];
        }
    }

    /**
     * create new Array with given indexImpl and same backing store
     */
    protected Array createView(Index index) {
        return ArrayFloat.factory(index, storage);
    }

    /* Get underlying primitive array storage. CAUTION! You may invalidate your warrentee! */
    public Object getStorage() {
        return storage;
    }

    // copy from javaArray to storage using the iterator: used by factory( Object);
    protected void copyFrom1DJavaArray(IndexIterator iter, Object javaArray) {
        float[] ja = (float[]) javaArray;
        for (float aJa : ja) {
            iter.setFloatNext(aJa);
        }
    }

    // copy to javaArray from storage using the iterator: used by copyToNDJavaArray;
    protected void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
        float[] ja = (float[]) javaArray;
        for (int i = 0; i < ja.length; i++) {
            ja[i] = iter.getFloatNext();
        }
    }

    public ByteBuffer getDataAsByteBuffer() {
        ByteBuffer bb = ByteBuffer.allocate((int) (4 * getSize()));
        FloatBuffer ib = bb.asFloatBuffer();
        ib.put((float[]) get1DJavaArray(float.class)); // make sure its in canonical order
        return bb;
    }

    /**
     * Return the element class type
     */
    public Class getElementType() {
        return float.class;
    }

    /**
     * get the value at the specified index.
     */
    public float get(Index i) {
        return storage[i.currentElement()];
    }

    /**
     * set the value at the sepcified index.
     */
    public void set(Index i, float value) {
        storage[i.currentElement()] = value;
    }

    public double getDouble(Index i) {
        return (double) storage[i.currentElement()];
    }

    public void setDouble(Index i, double value) {
        storage[i.currentElement()] = (float) value;
    }

    public float getFloat(Index i) {
        return storage[i.currentElement()];
    }

    public void setFloat(Index i, float value) {
        storage[i.currentElement()] = value;
    }

    public long getLong(Index i) {
        return (long) storage[i.currentElement()];
    }

    public void setLong(Index i, long value) {
        storage[i.currentElement()] = (float) value;
    }

    public int getInt(Index i) {
        return (int) storage[i.currentElement()];
    }

    public void setInt(Index i, int value) {
        storage[i.currentElement()] = (float) value;
    }

    public short getShort(Index i) {
        return (short) storage[i.currentElement()];
    }

    public void setShort(Index i, short value) {
        storage[i.currentElement()] = (float) value;
    }

    public byte getByte(Index i) {
        return (byte) storage[i.currentElement()];
    }

    public void setByte(Index i, byte value) {
        storage[i.currentElement()] = (float) value;
    }

    public char getChar(Index i) {
        return (char) storage[i.currentElement()];
    }

    public void setChar(Index i, char value) {
        storage[i.currentElement()] = (float) value;
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
        return String.valueOf(this.storage[i.currentElement()]);
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setString(Index i, String value) {
        this.storage[i.currentElement()] = Float.parseFloat(value);
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
        return storage[i.currentElement()];
    }

    public void setObject(Index i, Object value) {
        storage[i.currentElement()] = ((Number) value).floatValue();
    }

    // package private : mostly for iterators
    public double getDouble(int index) {
        return (double) storage[index];
    }

    public void setDouble(int index, double value) {
        storage[index] = (float) value;
    }

    public float getFloat(int index) {
        return storage[index];
    }

    public void setFloat(int index, float value) {
        storage[index] = value;
    }

    public long getLong(int index) {
        return (long) storage[index];
    }

    public void setLong(int index, long value) {
        storage[index] = (float) value;
    }

    public int getInt(int index) {
        return (int) storage[index];
    }

    public void setInt(int index, int value) {
        storage[index] = (float) value;
    }

    public short getShort(int index) {
        return (short) storage[index];
    }

    public void setShort(int index, short value) {
        storage[index] = (float) value;
    }

    public byte getByte(int index) {
        return (byte) storage[index];
    }

    public void setByte(int index, byte value) {
        storage[index] = (float) value;
    }

    public char getChar(int index) {
        return (char) storage[index];
    }

    public void setChar(int index, char value) {
        storage[index] = (float) value;
    }

    public boolean getBoolean(int index) {
        throw new ForbiddenConversionException();
    }

    public void setBoolean(int index, boolean value) {
        throw new ForbiddenConversionException();
    }
    
    public String getString(int index) {
        return String.valueOf(this.storage[index]);
    }

    public void setString(int index, String value) {
        this.storage[index] = Float.parseFloat(value);
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
        return getFloat(index);
    }

    public void setObject(int index, Object value) {
        storage[index] = ((Number) value).floatValue();
    }

    /**
     * Concrete implementation of Array specialized for floats, rank 0.
     */
    public static class D0 extends ArrayFloat {

        private Index0D ix;

        /**
         * Constructor.
         */
        public D0() {
            super(new int[]{});
            ix = (Index0D) indexCalc;
        }

        private D0(Index i, float[] store) {
            super(i, store);
            ix = (Index0D) indexCalc;
        }

        /**
         * get the value.
         */
        public float get() {
            return storage[ix.currentElement()];
        }

        /**
         * set the value.
         */
        public void set(float value) {
            storage[ix.currentElement()] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for floats, rank 1.
     */
    public static class D1 extends ArrayFloat {

        private Index1D ix;

        /**
         * Constructor for array of shape {len0}.
         */
        public D1(int len0) {
            super(new int[]{len0});
            ix = (Index1D) indexCalc;
        }

        private D1(Index i, float[] store) {
            super(i, store);
            ix = (Index1D) indexCalc;
        }

        /**
         * get the value.
         */
        public float get(int i) {
            return storage[ix.setDirect(i)];
        }

        /**
         * set the value.
         */
        public void set(int i, float value) {
            storage[ix.setDirect(i)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for floats, rank 2.
     */
    public static class D2 extends ArrayFloat {

        private Index2D ix;

        /**
         * Constructor for array of shape {len0,len1}.
         */
        public D2(int len0, int len1) {
            super(new int[]{len0, len1});
            ix = (Index2D) indexCalc;
        }

        private D2(Index i, float[] store) {
            super(i, store);
            ix = (Index2D) indexCalc;
        }

        /**
         * get the value.
         */
        public float get(int i, int j) {
            return storage[ix.setDirect(i, j)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, float value) {
            storage[ix.setDirect(i, j)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for floats, rank 3.
     */
    public static class D3 extends ArrayFloat {

        private Index3D ix;

        /**
         * Constructor for array of shape {len0,len1,len2}.
         */
        public D3(int len0, int len1, int len2) {
            super(new int[]{len0, len1, len2});
            ix = (Index3D) indexCalc;
        }

        private D3(Index i, float[] store) {
            super(i, store);
            ix = (Index3D) indexCalc;
        }

        /**
         * get the value.
         */
        public float get(int i, int j, int k) {
            return storage[ix.setDirect(i, j, k)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, float value) {
            storage[ix.setDirect(i, j, k)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for floats, rank 4.
     */
    public static class D4 extends ArrayFloat {

        private Index4D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3}.
         */
        public D4(int len0, int len1, int len2, int len3) {
            super(new int[]{len0, len1, len2, len3});
            ix = (Index4D) indexCalc;
        }

        private D4(Index i, float[] store) {
            super(i, store);
            ix = (Index4D) indexCalc;
        }

        /**
         * get the value.
         */
        public float get(int i, int j, int k, int l) {
            return storage[ix.setDirect(i, j, k, l)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, float value) {
            storage[ix.setDirect(i, j, k, l)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for floats, rank 5.
     */
    public static class D5 extends ArrayFloat {

        private Index5D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4}.
         */
        public D5(int len0, int len1, int len2, int len3, int len4) {
            super(new int[]{len0, len1, len2, len3, len4});
            ix = (Index5D) indexCalc;
        }

        private D5(Index i, float[] store) {
            super(i, store);
            ix = (Index5D) indexCalc;
        }

        /**
         * get the value.
         */
        public float get(int i, int j, int k, int l, int m) {
            return storage[ix.setDirect(i, j, k, l, m)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, float value) {
            storage[ix.setDirect(i, j, k, l, m)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for floats, rank 6.
     */
    public static class D6 extends ArrayFloat {

        private Index6D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4,len5,}.
         */
        public D6(int len0, int len1, int len2, int len3, int len4, int len5) {
            super(new int[]{len0, len1, len2, len3, len4, len5});
            ix = (Index6D) indexCalc;
        }

        private D6(Index i, float[] store) {
            super(i, store);
            ix = (Index6D) indexCalc;
        }

        /**
         * get the value.
         */
        public float get(int i, int j, int k, int l, int m, int n) {
            return storage[ix.setDirect(i, j, k, l, m, n)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, int n, float value) {
            storage[ix.setDirect(i, j, k, l, m, n)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for floats, rank 7.
     */
    public static class D7 extends ArrayFloat {

        private Index7D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4,len5,len6}.
         */
        public D7(int len0, int len1, int len2, int len3, int len4, int len5, int len6) {
            super(new int[]{len0, len1, len2, len3, len4, len5, len6});
            ix = (Index7D) indexCalc;
        }

        private D7(Index i, float[] store) {
            super(i, store);
            ix = (Index7D) indexCalc;
        }

        /**
         * get the value.
         */
        public float get(int i, int j, int k, int l, int m, int n, int o) {
            return storage[ix.setDirect(i, j, k, l, m, n, o)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, int n, int o, float value) {
            storage[ix.setDirect(i, j, k, l, m, n, o)] = value;
        }
    }

}
