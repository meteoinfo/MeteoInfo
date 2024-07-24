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
import java.time.LocalDateTime;

/**
 * Concrete implementation of Array specialized for doubles. Data storage is
 * with 1D java array of doubles.
 *
 * @see Array
 * @author caron
 */
public class ArrayDouble extends Array {

    /**
     * package private. use Array.factory()
     */
    static ArrayDouble factory(Index index) {
        return ArrayDouble.factory(index, null);
    }

    /* Create new ArrayDouble with given indexImpl and backing store.
   * Should be private.
   * @param index use this Index
   * @param stor. use this storage. if null, allocate.
   * @return. new ArrayDouble.D<rank> or ArrayDouble object.
     */
    static ArrayDouble factory(Index index, double[] storage) {
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
                return new ArrayDouble(index, storage);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    protected double[] storageD;

    /**
     * Create a new Array of type double and the given shape. dimensions.length
     * determines the rank of the new Array.
     *
     * @param shape the shape of the Array.
     */
    public ArrayDouble(int[] shape) {
        super(DataType.DOUBLE, shape);
        storageD = new double[(int) indexCalc.getSize()];
    }

    /**
     * create new Array with given indexImpl and the same backing store
     */
    protected Array createView(Index index) {
        return ArrayDouble.factory(index, storageD);
    }

    /**
     * Create a new Array using the given IndexArray and backing store. used for
     * sections, and factory. Trusted package private.
     *
     * @param ima use this IndexArray as the index
     * @param data use this as the backing store. if null, allocate
     */
    ArrayDouble(Index ima, double[] data) {
        super(DataType.DOUBLE, ima);

        if (data != null) {
            storageD = data;
        } else {
            storageD = new double[(int) indexCalc.getSize()];
        }
    }

    /* Get underlying primitive array storage. CAUTION! You may invalidate your warrentee! */
    public Object getStorage() {
        return storageD;
    }

    // copy from javaArray to storage using the iterator: used by factory( Object);
    protected void copyFrom1DJavaArray(IndexIterator iter, Object javaArray) {
        double[] ja = (double[]) javaArray;
        for (double aJa : ja) {
            iter.setDoubleNext(aJa);
        }
    }

    // copy to javaArray from storage using the iterator: used by copyToNDJavaArray;
    protected void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
        double[] ja = (double[]) javaArray;
        for (int i = 0; i < ja.length; i++) {
            ja[i] = iter.getDoubleNext();
        }
    }

    public ByteBuffer getDataAsByteBuffer() {
        ByteBuffer bb = ByteBuffer.allocate((int) (8 * getSize()));
        DoubleBuffer ib = bb.asDoubleBuffer();
        ib.put((double[]) get1DJavaArray(double.class)); // make sure its in canonical order
        return bb;
    }

    /**
     * Return the element class type
     */
    public Class getElementType() {
        return double.class;
    }

    /**
     * get the value at the specified index.
     */
    public double get(Index i) {
        return storageD[i.currentElement()];
    }

    /**
     * set the value at the specified index.
     */
    public void set(Index i, double value) {
        storageD[i.currentElement()] = value;
    }

    public double getDouble(Index i) {
        return storageD[i.currentElement()];
    }

    public void setDouble(Index i, double value) {
        storageD[i.currentElement()] = value;
    }

    public float getFloat(Index i) {
        return (float) storageD[i.currentElement()];
    }

    public void setFloat(Index i, float value) {
        storageD[i.currentElement()] = (double) value;
    }

    public long getLong(Index i) {
        return (long) storageD[i.currentElement()];
    }

    public void setLong(Index i, long value) {
        storageD[i.currentElement()] = (double) value;
    }

    public int getInt(Index i) {
        return (int) storageD[i.currentElement()];
    }

    public void setInt(Index i, int value) {
        storageD[i.currentElement()] = (double) value;
    }

    public short getShort(Index i) {
        return (short) storageD[i.currentElement()];
    }

    public void setShort(Index i, short value) {
        storageD[i.currentElement()] = (double) value;
    }

    public byte getByte(Index i) {
        return (byte) storageD[i.currentElement()];
    }

    public void setByte(Index i, byte value) {
        storageD[i.currentElement()] = (double) value;
    }

    public char getChar(Index i) {
        return (char) storageD[i.currentElement()];
    }

    public void setChar(Index i, char value) {
        storageD[i.currentElement()] = (double) value;
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public boolean getBoolean(Index i) {
        return storageD[i.currentElement()] != 0;
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setBoolean(Index i, boolean value) {
        storageD[i.currentElement()] = value ? 1 : 0;
    }
    
    /**
     * not legal, throw ForbiddenConversionException
     */
    public String getString(Index i) {
        return String.format("%.8f", storageD[i.currentElement()]);
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setString(Index i, String value) {
        storageD[i.currentElement()] = Double.parseDouble(value);
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

    public LocalDateTime getDate(Index i) {
        return JDateUtil.fromOADate(storageD[i.currentElement()]);
    }

    public void setDate(Index i, LocalDateTime value) {
        storageD[i.currentElement()] = JDateUtil.toOADate(value);
    }

    public Object getObject(Index i) {
        return storageD[i.currentElement()];
    }

    public void setObject(Index i, Object value) {
        if (value instanceof Boolean) {
            storageD[i.currentElement()] = ((Boolean) value) ? 1 : 0;
        } else {
            storageD[i.currentElement()] = ((Number) value).doubleValue();
        }
    }

    // trusted, assumes that individual dimension lengths have been checked
    public double getDouble(int index) {
        return storageD[index];
    }

    public void setDouble(int index, double value) {
        storageD[index] = value;
    }

    public float getFloat(int index) {
        return (float) storageD[index];
    }

    public void setFloat(int index, float value) {
        storageD[index] = (double) value;
    }

    public long getLong(int index) {
        return (long) storageD[index];
    }

    public void setLong(int index, long value) {
        storageD[index] = (double) value;
    }

    public int getInt(int index) {
        return (int) storageD[index];
    }

    public void setInt(int index, int value) {
        storageD[index] = (double) value;
    }

    public short getShort(int index) {
        return (short) storageD[index];
    }

    public void setShort(int index, short value) {
        storageD[index] = (double) value;
    }

    public byte getByte(int index) {
        return (byte) storageD[index];
    }

    public void setByte(int index, byte value) {
        storageD[index] = (double) value;
    }

    public char getChar(int index) {
        return (char) storageD[index];
    }

    public void setChar(int index, char value) {
        storageD[index] = (double) value;
    }

    public boolean getBoolean(int index) {
        return storageD[index] != 0;
    }

    public void setBoolean(int index, boolean value) {
        storageD[index] = value ? 1 : 0;
    }

    public String getString(int index) {
        return String.format("%.8f", storageD[index]);
    }

    public void setString(int index, String value) {
        storageD[index] = Double.parseDouble(value);
    }

    public Complex getComplex(int index) {
        return new Complex(getDouble(index), 0);
    }

    public void setComplex(int index, Complex value) {
        throw new ForbiddenConversionException();
    }

    public LocalDateTime getDate(int index) {
        return JDateUtil.fromOADate(storageD[index]);
    }

    public void setDate(int index, LocalDateTime value) {
        storageD[index] = JDateUtil.toOADate(value);
    }

    public Object getObject(int index) {
        return getDouble(index);
    }

    public void setObject(int index, Object value) {
        if (value instanceof Boolean) {
            storageD[index] = ((Boolean) value) ? 1 : 0;
        } else {
            storageD[index] = ((Number) value).doubleValue();
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 0.
     */
    public static class D0 extends ArrayDouble {

        private Index0D ix;

        /**
         * Constructor.
         */
        public D0() {
            super(new int[]{});
            ix = (Index0D) indexCalc;
        }

        private D0(Index i, double[] store) {
            super(i, store);
            ix = (Index0D) indexCalc;
        }

        /**
         * get the value.
         */
        public double get() {
            return storageD[ix.currentElement()];
        }

        /**
         * set the value.
         */
        public void set(double value) {
            storageD[ix.currentElement()] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 1.
     */
    public static class D1 extends ArrayDouble {

        private Index1D ix;

        /**
         * Constructor for array of shape {len0}.
         */
        public D1(int len0) {
            super(new int[]{len0});
            ix = (Index1D) indexCalc;
        }

        private D1(Index i, double[] store) {
            super(i, store);
            ix = (Index1D) indexCalc;
        }

        /**
         * get the value.
         */
        public double get(int i) {
            return storageD[ix.setDirect(i)];
        }

        /**
         * set the value.
         */
        public void set(int i, double value) {
            storageD[ix.setDirect(i)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 2.
     */
    public static class D2 extends ArrayDouble {

        private Index2D ix;

        /**
         * Constructor for array of shape {len0,len1}.
         */
        public D2(int len0, int len1) {
            super(new int[]{len0, len1});
            ix = (Index2D) indexCalc;
        }

        private D2(Index i, double[] store) {
            super(i, store);
            ix = (Index2D) indexCalc;
        }

        /**
         * get the value.
         */
        public double get(int i, int j) {
            return storageD[ix.setDirect(i, j)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, double value) {
            storageD[ix.setDirect(i, j)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 3.
     */
    public static class D3 extends ArrayDouble {

        private Index3D ix;

        /**
         * Constructor for array of shape {len0,len1,len2}.
         */
        public D3(int len0, int len1, int len2) {
            super(new int[]{len0, len1, len2});
            ix = (Index3D) indexCalc;
        }

        private D3(Index i, double[] store) {
            super(i, store);
            ix = (Index3D) indexCalc;
        }

        /**
         * get the value.
         */
        public double get(int i, int j, int k) {
            return storageD[ix.setDirect(i, j, k)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, double value) {
            storageD[ix.setDirect(i, j, k)] = value;
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

            public double getNext() {
                return storageD[++currElement];
            }

            public void setNext(double val) {
                storageD[++currElement] = val;
            }
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 4.
     */
    public static class D4 extends ArrayDouble {

        private Index4D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3}.
         */
        public D4(int len0, int len1, int len2, int len3) {
            super(new int[]{len0, len1, len2, len3});
            ix = (Index4D) indexCalc;
        }

        private D4(Index i, double[] store) {
            super(i, store);
            ix = (Index4D) indexCalc;
        }

        /**
         * get the value.
         */
        public double get(int i, int j, int k, int l) {
            return storageD[ix.setDirect(i, j, k, l)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, double value) {
            storageD[ix.setDirect(i, j, k, l)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 5.
     */
    public static class D5 extends ArrayDouble {

        private Index5D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4}.
         */
        public D5(int len0, int len1, int len2, int len3, int len4) {
            super(new int[]{len0, len1, len2, len3, len4});
            ix = (Index5D) indexCalc;
        }

        private D5(Index i, double[] store) {
            super(i, store);
            ix = (Index5D) indexCalc;
        }

        /**
         * get the value.
         */
        public double get(int i, int j, int k, int l, int m) {
            return storageD[ix.setDirect(i, j, k, l, m)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, double value) {
            storageD[ix.setDirect(i, j, k, l, m)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 6.
     */
    public static class D6 extends ArrayDouble {

        private Index6D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4,len5,}.
         */
        public D6(int len0, int len1, int len2, int len3, int len4, int len5) {
            super(new int[]{len0, len1, len2, len3, len4, len5});
            ix = (Index6D) indexCalc;
        }

        private D6(Index i, double[] store) {
            super(i, store);
            ix = (Index6D) indexCalc;
        }

        /**
         * get the value.
         */
        public double get(int i, int j, int k, int l, int m, int n) {
            return storageD[ix.setDirect(i, j, k, l, m, n)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, int n, double value) {
            storageD[ix.setDirect(i, j, k, l, m, n)] = value;
        }
    }

    /**
     * Concrete implementation of Array specialized for doubles, rank 7.
     */
    public static class D7 extends ArrayDouble {

        private Index7D ix;

        /**
         * Constructor for array of shape {len0,len1,len2,len3,len4,len5,len6}.
         */
        public D7(int len0, int len1, int len2, int len3, int len4, int len5, int len6) {
            super(new int[]{len0, len1, len2, len3, len4, len5, len6});
            ix = (Index7D) indexCalc;
        }

        private D7(Index i, double[] store) {
            super(i, store);
            ix = (Index7D) indexCalc;
        }

        /**
         * get the value.
         */
        public double get(int i, int j, int k, int l, int m, int n, int o) {
            return storageD[ix.setDirect(i, j, k, l, m, n, o)];
        }

        /**
         * set the value.
         */
        public void set(int i, int j, int k, int l, int m, int n, int o, double value) {
            storageD[ix.setDirect(i, j, k, l, m, n, o)] = value;
        }
    }

}
