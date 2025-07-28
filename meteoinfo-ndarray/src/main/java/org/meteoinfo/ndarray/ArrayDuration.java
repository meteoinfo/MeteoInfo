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

import org.meteoinfo.common.util.JDateUtil;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Concrete implementation of Array specialized for time duration. Data storage is
 * with 1D java array of Duration.
 *
 * @see Array
 * @author Yaqiang
 */
public class ArrayDuration extends Array {

    /**
     * package private. use Array.factory()
     */
    static ArrayDuration factory(Index index) {
        return ArrayDuration.factory(index, null);
    }

    /* Create new ArrayDuration with given indexImpl and backing store.
    * Should be private.
    * @param index use this Index
    * @param stor. use this storage. if null, allocate.
    * @return. new ArrayDuration.D<rank> or ArrayDuration object.
     */
    static ArrayDuration factory(Index index, Duration[] storage) {
        switch (index.getRank()) {
            case 0:
                return new D0(index, storage);
            case 1:
                return new D1(index, storage);
            case 2:
                return new D2(index, storage);
            case 3:
                return new D3(index, storage);
            case 4:
                return new D4(index, storage);
            case 5:
                return new D5(index, storage);
            case 6:
                return new D6(index, storage);
            case 7:
                return new D7(index, storage);
            default:
                return new ArrayDuration(index, storage);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    protected Duration[] storage;

    /**
     * Create a new Array of type double and the given shape. dimensions.length
     * determines the rank of the new Array.
     *
     * @param shape the shape of the Array.
     */
    public ArrayDuration(int[] shape) {
        super(DataType.DATE, shape);
        storage = new Duration[(int) indexCalc.getSize()];
    }

    /**
     * create new Array with given indexImpl and the same backing store
     * @return New Array view
     */
    @Override
    protected Array createView(Index index) {
        return ArrayDuration.factory(index, storage);
    }

    /**
     * Create a new Array using the given IndexArray and backing store. used for
     * sections, and factory. Trusted package private.
     *
     * @param ima use this IndexArray as the index
     * @param data use this as the backing store. if null, allocate
     */
    ArrayDuration(Index ima, Duration[] data) {
        super(DataType.DATE, ima);

        if (data != null) {
            storage = data;
        } else {
            storage = new Duration[(int) indexCalc.getSize()];
        }
    }

    /* Get underlying primitive array storage. CAUTION! You may invalidate your warrentee! */
    @Override
    public Object[] getStorage() {
        return storage;
    }

    // copy from javaArray to storage using the iterator: used by factory( Object);
    @Override
    protected void copyFrom1DJavaArray(IndexIterator iter, Object javaArray) {
        LocalDateTime[] ja = (LocalDateTime[]) javaArray;
        for (LocalDateTime aJa : ja) {
            iter.setDateNext(aJa);
        }
    }

    // copy to javaArray from storage using the iterator: used by copyToNDJavaArray;
    @Override
    protected void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
        LocalDateTime[] ja = (LocalDateTime[]) javaArray;
        for (int i = 0; i < ja.length; i++) {
            ja[i] = iter.getDateNext();
        }
    }

    @Override
    public ByteBuffer getDataAsByteBuffer() {
        ByteBuffer bb = ByteBuffer.allocate((int) (8 * getSize()));
        DoubleBuffer ib = bb.asDoubleBuffer();
        ib.put((double[]) get1DJavaArray(double.class)); // make sure its in canonical order
        return bb;
    }

    /**
     * Return the element class type
     * @return Element class type
     */
    @Override
    public Class getElementType() {
        return LocalDateTime.class;
    }

    /**
     * get the value at the specified index.
     * @param i
     * @return 
     */
    public Duration get(Index i) {
        return storage[i.currentElement()];
    }

    /**
     * set the value at the specified index.
     * @param i
     * @param value
     */
    public void set(Index i, Duration value) {
        storage[i.currentElement()] = value;
    }

    public double getDouble(Index i) {
        return storage[i.currentElement()].getSeconds();
    }

    public void setDouble(Index i, double value) {
        storage[i.currentElement()] = Duration.ofSeconds(Math.round(value));
    }

    public float getFloat(Index i) {
        return storage[i.currentElement()].getSeconds();
    }

    public void setFloat(Index i, float value) {
        storage[i.currentElement()] = Duration.ofSeconds(Math.round(value));
    }

    public long getLong(Index i) {
        return storage[i.currentElement()].getSeconds();
    }

    public void setLong(Index i, long value) {
        storage[i.currentElement()] = Duration.ofSeconds(value);
    }

    public int getInt(Index i) {
        return (int) storage[i.currentElement()].getSeconds();
    }

    public void setInt(Index i, int value) {
        storage[i.currentElement()] = Duration.ofSeconds(value);
    }

    public short getShort(Index i) {
        throw new ForbiddenConversionException();
    }

    public void setShort(Index i, short value) {
        throw new ForbiddenConversionException();
    }

    public byte getByte(Index i) {
        throw new ForbiddenConversionException();
    }

    public void setByte(Index i, byte value) {
        throw new ForbiddenConversionException();
    }

    public char getChar(Index i) {
        throw new ForbiddenConversionException();
    }

    public void setChar(Index i, char value) {
        throw new ForbiddenConversionException();
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
        return this.storage[i.currentElement()].toString();
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
    public Complex getComplex(Index i) { throw new ForbiddenConversionException(); }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setComplex(Index i, Complex value) { throw new ForbiddenConversionException(); }

    public LocalDateTime getDate(Index i) { throw new ForbiddenConversionException(); }

    public void setDate(Index i, LocalDateTime value) { throw new ForbiddenConversionException(); }

    public Duration getDuration(Index i) {
        return storage[i.currentElement()];
    }

    public void setDuration(Index i, Duration value) {
        storage[i.currentElement()] = value;
    }

    public Object getObject(Index i) {
        return storage[i.currentElement()];
    }

    public void setObject(Index i, Object value) {
        storage[i.currentElement()] = (Duration) value;
    }

    // trusted, assumes that individual dimension lengths have been checked
    public double getDouble(int index) {
        return storage[index].getSeconds();
    }

    public void setDouble(int index, double value) {
        storage[index] = Duration.ofSeconds(Math.round(value));
    }

    public float getFloat(int index) {
        return storage[index].getSeconds();
    }

    public void setFloat(int index, float value) {
        storage[index] = Duration.ofSeconds(Math.round(value));
    }

    public long getLong(int index) {
        return storage[index].getSeconds();
    }

    public void setLong(int index, long value) {
        storage[index] = Duration.ofSeconds(value);
    }

    public int getInt(int index) {
        return (int) storage[index].getSeconds();
    }

    public void setInt(int index, int value) {
        storage[index] = Duration.ofSeconds(value);
    }

    public short getShort(int index) {
        throw new ForbiddenConversionException();
    }

    public void setShort(int index, short value) {
        throw new ForbiddenConversionException();
    }

    public byte getByte(int index) {
        throw new ForbiddenConversionException();
    }

    public void setByte(int index, byte value) {
        throw new ForbiddenConversionException();
    }

    public char getChar(int index) {
        throw new ForbiddenConversionException();
    }

    public void setChar(int index, char value) {
        throw new ForbiddenConversionException();
    }

    public boolean getBoolean(int index) {
        throw new ForbiddenConversionException();
    }

    public void setBoolean(int index, boolean value) {
        throw new ForbiddenConversionException();
    }
    
    public String getString(int index) {
        return this.storage[index].toString();
    }

    public void setString(int index, String value) {
        throw new ForbiddenConversionException();
    }
    
    public Complex getComplex(int index) {
        throw new ForbiddenConversionException();
    }

    public void setComplex(int index, Complex value) { throw new ForbiddenConversionException(); }

    public LocalDateTime getDate(int index) { throw new ForbiddenConversionException(); }

    public void setDate(int index, LocalDateTime value) { throw new ForbiddenConversionException(); }

    public Duration getDuration(int index) {
        return storage[index];
    }

    public void setDuration(int index, Duration value) {
        storage[index] = value;
    }

    public Object getObject(int index) {
        return getDate(index);
    }

    public void setObject(int index, Object value) {
        storage[index] = (Duration) value;
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 0.
     */
    public static class D0 extends ArrayDuration {

        private Index0D ix;

        /**
         * Constructor.
         */
        public D0() {
            super(new int[]{});
            ix = (Index0D) indexCalc;
        }

        private D0(Index i, Duration[] store) {
            super(i, store);
            ix = (Index0D) indexCalc;
        }

        /**
         * get the value.
         * @return 
         */
        public Duration get() {
            return storage[ix.currentElement()];
        }

        /**
         * set the value.
         * @param value
         */
        public void set(Duration value) {
            storage[ix.currentElement()] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 1.
     */
    public static class D1 extends ArrayDuration {

        private Index1D ix;

        /**
         * Constructor for array of shape {len0}.
         * @param len0
         */
        public D1(int len0) {
            super(new int[]{len0});
            ix = (Index1D) indexCalc;
        }

        private D1(Index i, Duration[] store) {
            super(i, store);
            ix = (Index1D) indexCalc;
        }

        /**
         * get the value.
         * @param i
         * @return 
         */
        public Duration get(int i) {
            return storage[ix.setDirect(i)];
        }

        /**
         * set the value.
         * @param i
         * @param value
         */
        public void set(int i, Duration value) {
            storage[ix.setDirect(i)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 2.
     */
    public static class D2 extends ArrayDuration {

        private Index2D ix;

        /**
         * Constructor for array of shape {len0,len1}.
         */
        public D2(int len0, int len1) {
            super(new int[]{len0, len1});
            ix = (Index2D) indexCalc;
        }

        private D2(Index i, Duration[] store) {
            super(i, store);
            ix = (Index2D) indexCalc;
        }

        /**
         * get the value.
         */
        public Duration get(int i, int j) {
            return storage[ix.setDirect(i, j)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, Duration value) {
            storage[ix.setDirect(i, j)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 3.
     */
    public static class D3 extends ArrayDuration {

        private Index3D ix;

        /**
         * Constructor for array of shape {len0,len1,len2}.
         */
        public D3(int len0, int len1, int len2) {
            super(new int[]{len0, len1, len2});
            ix = (Index3D) indexCalc;
        }

        private D3(Index i, Duration[] store) {
            super(i, store);
            ix = (Index3D) indexCalc;
        }

        /**
         * get the value.
         */
        public Duration get(int i, int j, int k) {
            return storage[ix.setDirect(i, j, k)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, Duration value) {
            storage[ix.setDirect(i, j, k)] = value;
        }

        public IF getIF() {
            return new IF();
        }

        public class IF {

            private int currElement = -1;
            private int size = (int) ix.getSize();

            public boolean hasNext(int howMany) {
                return currElement < size - howMany;
            }

            public Duration getNext() {
                return storage[++currElement];
            }

            public void setNext(Duration val) {
                storage[++currElement] = val;
            }
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 4.
     */
    public static class D4 extends ArrayDuration {

        private Index4D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3}.
         */
        public D4(int len0, int len1, int len2, int len3) {
            super(new int[]{len0, len1, len2, len3});
            ix = (Index4D) indexCalc;
        }

        private D4(Index i, Duration[] store) {
            super(i, store);
            ix = (Index4D) indexCalc;
        }

        /**
         * get the value.
         */
        public Duration get(int i, int j, int k, int l) {
            return storage[ix.setDirect(i, j, k, l)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, Duration value) {
            storage[ix.setDirect(i, j, k, l)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 5.
     */
    public static class D5 extends ArrayDuration {

        private Index5D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4}.
         */
        public D5(int len0, int len1, int len2, int len3, int len4) {
            super(new int[]{len0, len1, len2, len3, len4});
            ix = (Index5D) indexCalc;
        }

        private D5(Index i, Duration[] store) {
            super(i, store);
            ix = (Index5D) indexCalc;
        }

        /**
         * get the value.
         */
        public Duration get(int i, int j, int k, int l, int m) {
            return storage[ix.setDirect(i, j, k, l, m)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, Duration value) {
            storage[ix.setDirect(i, j, k, l, m)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 6.
     */
    public static class D6 extends ArrayDuration {

        private Index6D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4,len5,}.
         */
        public D6(int len0, int len1, int len2, int len3, int len4, int len5) {
            super(new int[]{len0, len1, len2, len3, len4, len5});
            ix = (Index6D) indexCalc;
        }

        private D6(Index i, Duration[] store) {
            super(i, store);
            ix = (Index6D) indexCalc;
        }

        /**
         * get the value.
         */
        public Duration get(int i, int j, int k, int l, int m, int n) {
            return storage[ix.setDirect(i, j, k, l, m, n)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, int n, Duration value) {
            storage[ix.setDirect(i, j, k, l, m, n)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 7.
     */
    public static class D7 extends ArrayDuration {

        private Index7D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4,len5,len6}.
         */
        public D7(int len0, int len1, int len2, int len3, int len4, int len5, int len6) {
            super(new int[]{len0, len1, len2, len3, len4, len5, len6});
            ix = (Index7D) indexCalc;
        }

        private D7(Index i, Duration[] store) {
            super(i, store);
            ix = (Index7D) indexCalc;
        }

        /**
         * get the value.
         */
        public Duration get(int i, int j, int k, int l, int m, int n, int o) {
            return storage[ix.setDirect(i, j, k, l, m, n, o)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, int n, int o, Duration value) {
            storage[ix.setDirect(i, j, k, l, m, n, o)] = value;
        }
    }

}
