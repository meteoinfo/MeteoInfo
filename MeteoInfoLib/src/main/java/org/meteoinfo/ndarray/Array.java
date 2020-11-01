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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.nio.*;

/**
 * Superclass for implementations of multidimensional arrays. An Array has a
 * <b>classType</b> which gives the Class of its elements, and a <b>shape</b>
 * which describes the number of elements in each index. The <b>rank</b> is the
 * number of indices. A <b>scalar</b> Array has rank = 0. An Array may have
 * arbitrary rank. The Array <b>size</b> is the total number of elements, which
 * must be less than 2^31 (about 2x10^9).
 * <p/>
 * Actual data storage is done with Java 1D arrays and stride index
 * calculations. This makes our Arrays rectangular, i.e. no "ragged arrays"
 * where different elements can have different lengths as in Java
 * multidimensional arrays, which are arrays of arrays.
 * <p/>
 * Each primitive Java type (boolean, byte, char, short, int, long, float,
 * double) has a corresponding concrete implementation, e.g. ArrayBoolean,
 * ArrayDouble. Reference types are all implemented using the ArrayObject class,
 * with the exceptions of the reference types that correspond to the primitive
 * types, eg Double.class is mapped to double.class.
 * <p/>
 * For efficiency, each Array type implementation has concrete subclasses for
 * ranks 0-7, eg ArrayDouble.D0 is a double array of rank 0, ArrayDouble.D1 is a
 * double array of rank 1, etc. These type and rank specific classes are
 * convenient to work with when you know the type and rank of the Array. Ranks
 * greater than 7 are handled by the type-specific superclass e.g. ArrayDouble.
 * The Array class itself is used for fully general handling of any type and
 * rank array. Use the Array.factory() methods to create Arrays in a general
 * way.
 * <p/>
 * The stride index calculations allow <b>logical views</b> to be efficiently
 * implemented, eg subset, transpose, slice, etc. These views use the same data
 * storage as the original Array they are derived from. The index stride
 * calculations are equally efficient for any composition of logical views.
 * <p/>
 * The type, shape and backing storage of an Array are immutable. The data
 * itself is read or written using an Index or an IndexIterator, which stores
 * any needed state information for efficient traversal. This makes use of
 * Arrays thread-safe (as long as you dont share the Index or IndexIterator)
 * except for the possibility of non-atomic read/write on long/doubles. If this
 * is the case, you should probably synchronize your calls. Presumably 64-bit
 * CPUs will make those operations atomic also.
 *
 * @author caron
 * @see Index
 * @see IndexIterator
 */
public abstract class Array {

    /**
     * Generate new Array with given type and shape and zeroed storage.
     *
     * @param dataType instance of DataType.
     * @param shape shape of the array.
     * @return new Array<type> or Array<type>.D<rank> if 0 <= rank <= 7.
     */
    static public Array factory(DataType dataType, int[] shape) {
        return factory(dataType, Index.factory(shape), null);
    }

    /**
     * Generate new Array with given type and shape and zeroed storage.
     *
     * @param classType element Class type, eg double.class.
     * @param shape shape of the array.
     * @return new Array<type> or Array<type>.D<rank> if 0 <= rank <= 7.
     */
    static public Array factory(Class classType, int[] shape) {
        Index index = Index.factory(shape);
        return factory(classType, index);
    }

    /* generate new Array with given type and shape and zeroed storage */
    static private Array factory(Class classType, Index index) {
        if ((classType == double.class) || (classType == Double.class)) {
            return ArrayDouble.factory(index);
        } else if ((classType == float.class) || (classType == Float.class)) {
            return ArrayFloat.factory(index);
        } else if ((classType == long.class) || (classType == Long.class)) {
            return ArrayLong.factory(index, false);
        } else if ((classType == int.class) || (classType == Integer.class)) {
            return ArrayInt.factory(index, false);
        } else if ((classType == short.class) || (classType == Short.class)) {
            return ArrayShort.factory(index, false);
        } else if ((classType == byte.class) || (classType == Byte.class)) {
            return ArrayByte.factory(index, false);
        } else if ((classType == char.class) || (classType == Character.class)) {
            return ArrayChar.factory(index);
        } else if ((classType == boolean.class) || (classType == Boolean.class)) {
            return ArrayBoolean.factory(index);
        } else if (classType == String.class) {
            return ArrayString.factory(index);
        } else if (classType == Complex.class) {
            return ArrayComplex.factory(index);
        } else if (classType == LocalDateTime.class) {
            return ArrayDate.factory(index);
        } else {
            return ArrayObject.factory(DataType.OBJECT, classType, false, index);
        }
    }

    /**
     * /** Generate new Array with given type, shape, storage.
     *
     * @param dataType DataType, eg DataType.DOUBLE.
     * @param shape shape of the array.
     * @param storage primitive array of correct type
     * @return new Array<type> or Array<type>.D<rank> if 0 <= rank <= 7.
     */
    static public Array factory(DataType dataType, int[] shape, Object storage) {
        return factory(dataType, Index.factory(shape), storage);
    }

    /* generate new Array with given type, index and storage */
    public static Array factory(DataType dtype, Index index, Object storage) {
        switch (dtype) {
            case DOUBLE:
                return ArrayDouble.factory(index, (double[]) storage);
            case FLOAT:
                return ArrayFloat.factory(index, (float[]) storage);
            case CHAR:
                return ArrayChar.factory(index, (char[]) storage);
            case BOOLEAN:
                return ArrayBoolean.factory(index, (boolean[]) storage);
            case ENUM4:
            case UINT:
            case INT:
                return ArrayInt.factory(index, dtype.isUnsigned(), (int[]) storage);
            case ENUM2:
            case USHORT:
            case SHORT:
                return ArrayShort.factory(index, dtype.isUnsigned(), (short[]) storage);
            case ENUM1:
            case UBYTE:
            case BYTE:
                return ArrayByte.factory(index, dtype.isUnsigned(), (byte[]) storage);
            case ULONG:
            case LONG:
                return ArrayLong.factory(index, dtype.isUnsigned(), (long[]) storage);
            case STRING:
                return ArrayString.factory(index, (Object[]) storage);
            case COMPLEX:
                return ArrayComplex.factory(index, (Complex[]) storage);
            case DATE:
                return ArrayDate.factory(index, (LocalDateTime[]) storage);
            case STRUCTURE:
                return ArrayObject.factory(dtype, StructureData.class, false, index, (Object[]) storage);
            case SEQUENCE:
                return ArrayObject.factory(dtype, StructureDataIterator.class, false, index, (Object[]) storage);
            case OPAQUE:
                return ArrayObject.factory(dtype, ByteBuffer.class, false, index, (Object[]) storage);
        }

        throw new RuntimeException("Cant use this method for datatype " + dtype);

        // used for VLEN ??
        // default:
        // return ArrayObject.factory(DataType.OBJECT, Object.class, index, (Object[]) storage); // LOOK dont know the
        // object class
        // }
    }

    /**
     * Generate new Array with given type, shape, storage. This should be
     * package private, but is exposed for efficiency. Normally use factory(
     * Class classType, int [] shape) instead. storage must be 1D array of type
     * classType. storage.length must equal product of shapes storage data needs
     * to be in canonical order
     *
     * @param classType element class type, eg double.class. Corresponding
     * Object types like Double.class are mapped to double.class. Any reference
     * types use ArrayObject.
     * @param shape array shape
     * @param storage 1D java array of type classType, except object types like
     * Double.class are mapped to their corresponding primitive type, eg
     * double.class.
     * @return Array of given type, shape and storage
     * @throws IllegalArgumentException storage.length != product of shapes
     * @throws ClassCastException wrong storage type
     */
    static public Array factory(Class classType, int[] shape, Object storage) {
        Index indexCalc = Index.factory(shape);
        return factory(classType, indexCalc, storage);
    }

    /**
     * Create new array with given type, index, storage.
     *
     * @param classType element class type, eg double.class.
     * @param indexCalc array index.
     * @param storage 1D java array of type classType.
     * @return Array of give type, index and storage.
     */
    static public Array factory(Class classType, Index indexCalc, Object storage) {
        if ((classType == double.class) || (classType == Double.class)) {
            return ArrayDouble.factory(indexCalc, (double[]) storage);
        } else if ((classType == float.class) || (classType == Float.class)) {
            return ArrayFloat.factory(indexCalc, (float[]) storage);
        } else if ((classType == long.class) || (classType == Long.class)) {
            return ArrayLong.factory(indexCalc, false, (long[]) storage);
        } else if ((classType == int.class) || (classType == Integer.class)) {
            return ArrayInt.factory(indexCalc, false, (int[]) storage);
        } else if ((classType == short.class) || (classType == Short.class)) {
            return ArrayShort.factory(indexCalc, false, (short[]) storage);
        } else if ((classType == byte.class) || (classType == Byte.class)) {
            return ArrayByte.factory(indexCalc, false, (byte[]) storage);
        } else if ((classType == char.class) || (classType == Character.class)) {
            return ArrayChar.factory(indexCalc, (char[]) storage);
        } else if ((classType == boolean.class) || (classType == Boolean.class)) {
            return ArrayBoolean.factory(indexCalc, (boolean[]) storage);
        } else if (classType == String.class) {
            return ArrayString.factory(indexCalc, (Object[]) storage);
        } else if (classType == Complex.class) {
            return ArrayComplex.factory(indexCalc, (Complex[]) storage);
        } else if (classType == LocalDateTime.class) {
            return ArrayDate.factory(indexCalc, (LocalDateTime[]) storage);
        } else {
            return ArrayObject.factory(classType, indexCalc, (Object[]) storage);
        }
    }

    /**
     * Generate new Array with given type and shape and an Index that always
     * return 0.
     *
     * @param classType element Class type, eg double.class.
     * @param shape shape of the array.
     * @param storage primitive array of correct type of length 1
     * @return new Array<type> or Array<type>.D<rank> if 0 <= rank <= 7.
     */
    static public Array factoryConstant(Class classType, int[] shape, Object storage) {
        Index index = new IndexConstant(shape);

        if ((classType == double.class) || (classType == Double.class)) {
            return new ArrayDouble(index, (double[]) storage);
        } else if ((classType == float.class) || (classType == Float.class)) {
            return new ArrayFloat(index, (float[]) storage);
        } else if ((classType == long.class) || (classType == Long.class)) {
            return new ArrayLong(index, false, (long[]) storage);
        } else if ((classType == int.class) || (classType == Integer.class)) {
            return new ArrayInt(index, false, (int[]) storage);
        } else if ((classType == short.class) || (classType == Short.class)) {
            return new ArrayShort(index, false, (short[]) storage);
        } else if ((classType == byte.class) || (classType == Byte.class)) {
            return new ArrayByte(index, false, (byte[]) storage);
        } else if ((classType == char.class) || (classType == Character.class)) {
            return new ArrayChar(index, (char[]) storage);
        } else if ((classType == boolean.class) || (classType == Boolean.class)) {
            return new ArrayBoolean(index, (boolean[]) storage);
        } else if (classType == String.class) {
            return ArrayString.factory(index, (Object[]) storage);
        } else if (classType == Complex.class) {
            return ArrayComplex.factory(index, (Complex[]) storage);
        } else if (classType == LocalDateTime.class) {
            return ArrayDate.factory(index, (LocalDateTime[]) storage);
        } else {
            return new ArrayObject(DataType.OBJECT, classType, false, index, (Object[]) storage);
        }
    }

    /**
     * Generate a new Array from a java array of any rank and type. This makes a
     * COPY of the data values of javaArray. LOOK: not sure this works for
     * reference types.
     *
     * @param javaArray scalar Object or a java array of any rank and type
     * @return Array of the appropriate rank and type, with the data copied from
     * javaArray.
     */
    static public Array factory(Object javaArray) {
        // get the rank and type
        int rank_ = 0;
        Class componentType = javaArray.getClass();
        while (componentType.isArray()) {
            rank_++;
            componentType = componentType.getComponentType();
        }
        /* if( rank_ == 0)
      throw new IllegalArgumentException("Array.factory: not an array");
    if( !componentType.isPrimitive())
      throw new UnsupportedOperationException("Array.factory: not a primitive array"); */

        // get the shape
        int count = 0;
        int[] shape = new int[rank_];
        Object jArray = javaArray;
        Class cType = jArray.getClass();
        while (cType.isArray()) {
            shape[count++] = java.lang.reflect.Array.getLength(jArray);
            jArray = java.lang.reflect.Array.get(jArray, 0);
            cType = jArray.getClass();
        }

        // create the Array
        Array aa = factory(componentType, shape);

        // copy the original array
        IndexIterator aaIter = aa.getIndexIterator();
        reflectArrayCopyIn(javaArray, aa, aaIter);

        return aa;
    }

    static private void reflectArrayCopyIn(Object jArray, Array aa, IndexIterator aaIter) {
        Class cType = jArray.getClass().getComponentType();
        if (cType.isPrimitive()) {
            aa.copyFrom1DJavaArray(aaIter, jArray);  // subclass does type-specific copy
        } else {
            for (int i = 0; i < java.lang.reflect.Array.getLength(jArray); i++) // recurse
            {
                reflectArrayCopyIn(java.lang.reflect.Array.get(jArray, i), aa, aaIter);
            }
        }
    }

    static private void reflectArrayCopyOut(Object jArray, Array aa, IndexIterator aaIter) {
        Class cType = jArray.getClass().getComponentType();
        //if (cType.isPrimitive()) { // Rob Weingruber <weingrub@rap.ucar.edu> May 11, 2011
        if (!cType.isArray()) {
            aa.copyTo1DJavaArray(aaIter, jArray);  // subclass does type-specific copy
        } else {
            for (int i = 0; i < java.lang.reflect.Array.getLength(jArray); i++) // recurse
            {
                reflectArrayCopyOut(java.lang.reflect.Array.get(jArray, i), aa, aaIter);
            }
        }
    }

    /**
     * Cover for System.arraycopy(). Works with the underlying data arrays.
     * ArraySrc and ArrayDst must be the same primitive type. Exposed for
     * efficiency; use at your own risk.
     *
     * @param arraySrc copy from here : if not in canonical order, an extra copy
     * will be done
     * @param srcPos starting at
     * @param arrayDst copy to here : must be in canonical order
     * @param dstPos starting at
     * @param len number of elements to copy
     */
    static public void arraycopy(Array arraySrc, int srcPos, Array arrayDst, int dstPos, int len) {
        // deal with special case
        if (arraySrc.isConstant()) {
            double d = arraySrc.getDouble(0);
            for (int i = dstPos; i < dstPos + len; i++) {
                arrayDst.setDouble(i, d);
            }
            return;
        }

        Object src = arraySrc.get1DJavaArray(arraySrc.getElementType()); // ensure canonical order
        Object dst = arrayDst.getStorage();
        System.arraycopy(src, srcPos, dst, dstPos, len);
    }

    /**
     * Make a 1D array from a start and inccr.
     *
     * @param dtype data type of result. must be convertible to double.
     * @param npts number of points
     * @param start starting values
     * @param incr increment
     * @return 1D array
     */
    static public Array makeArray(DataType dtype, int npts, double start, double incr) {
        Array result = Array.factory(dtype.getPrimitiveClassType(), new int[]{npts});
        IndexIterator dataI = result.getIndexIterator();
        for (int i = 0; i < npts; i++) {
            double val = start + i * incr;
            dataI.setDoubleNext(val);
        }
        return result;
    }

    /**
     * Make an 1D array from a list of strings.
     *
     * @param dtype data type of the array.
     * @param isUnsigned may be unsigned (byte, short, int, long)
     * @param stringValues list of strings.
     * @return resulting 1D array.
     * @throws NumberFormatException if string values not parseable to specified
     * data type
     */
    static public Array makeArray(DataType dtype, boolean isUnsigned, List<String> stringValues) throws NumberFormatException {
        Array result = Array.factory(dtype.getPrimitiveClassType(), new int[]{stringValues.size()});
        IndexIterator dataI = result.getIndexIterator();

        for (String s : stringValues) {
            if (dtype == DataType.STRING) {
                dataI.setObjectNext(s);

            } else if (dtype == DataType.LONG) {
                if (isUnsigned) {
                    BigInteger biggy = new BigInteger(s);
                    long convert = biggy.longValue(); // > 63 bits will become "negetive".
                    System.out.printf("%s == %d%n", biggy, convert);
                    dataI.setLongNext(biggy.longValue());

                } else {
                    long val = Long.parseLong(s);
                    dataI.setLongNext(val);
                }

            } else {
                double val = Double.parseDouble(s);
                dataI.setDoubleNext(val);
            }
        }
        return result;
    }

    /**
     * Make an 1D array from a list of strings.
     *
     * @param dtype data type of the array. Assumed unsigned
     * @param stringValues list of strings.
     * @return resulting 1D array.
     * @throws NumberFormatException if string values not parseable to specified
     * data type
     */
    static public Array makeArray(DataType dtype, List<String> stringValues) throws NumberFormatException {
        return makeArray(dtype, false, stringValues);
    }

    /**
     * Make an 1D array from an array of strings.
     *
     * @param dtype data type of the array. Assumed unsigned
     * @param stringValues list of strings.
     * @return resulting 1D array.
     * @throws NumberFormatException if string values not parseable to specified
     * data type
     */
    static public Array makeArray(DataType dtype, String[] stringValues) throws NumberFormatException {
        return makeArray(dtype, Arrays.asList(stringValues));
    }

    /**
     * Add extra dimension with len = 1.
     *
     * @param org original array
     * @return rank1 array of rank + 1
     */
    static public Array makeArrayRankPlusOne(Array org) {
        int[] shape = new int[org.getRank() + 1];
        System.arraycopy(org.getShape(), 0, shape, 1, org.getRank());
        shape[0] = 1;
        return factory(org.getDataType(), shape, org.getStorage());
    }

    /////////////////////////////////////////////////////
    protected final Index indexCalc;
    protected final int rank;
    private IndexIterator ii; // local iterator
    protected DataType dataType;

    // for subclasses only
    protected Array(DataType dataType, int[] shape) {
        this.dataType = dataType;
        this.rank = shape.length;
        this.indexCalc = Index.factory(shape);
    }

    protected Array(DataType dataType, Index index) {
        this.dataType = dataType;
        this.rank = index.getRank();
        this.indexCalc = index;
    }

    /**
     * Compute the datatype sort
     */
    DataType computesort() {
        if (this instanceof ArrayBoolean) {
            return DataType.BOOLEAN;
        }
        if (this instanceof ArrayByte) {
            return DataType.BYTE;
        }
        if (this instanceof ArrayChar) {
            return DataType.CHAR;
        }
        if (this instanceof ArrayShort) {
            return DataType.SHORT;
        }
        if (this instanceof ArrayInt) {
            return DataType.INT;
        }
        if (this instanceof ArrayLong) {
            return DataType.LONG;
        }
        if (this instanceof ArrayFloat) {
            return DataType.FLOAT;
        }
        if (this instanceof ArrayDouble) {
            return DataType.DOUBLE;
        }
        if (this instanceof ArrayString) {
            return DataType.STRING;
        }
        if (this instanceof ArrayComplex) {
            return DataType.COMPLEX;
        }
        if (this instanceof ArrayDate) {
            return DataType.DATE;
        }
        if (this instanceof ArrayObject) {
            return DataType.OBJECT;
        }
        if (this instanceof ArraySequence) {
            return DataType.SEQUENCE;
        }
        if (this instanceof ArrayStructure) {
            return DataType.STRUCTURE;
        }
        return null;
    }

    /**
     * Return the computed datatype for this array
     *
     * @return the data type
     */
    public DataType getDataType() {
        return this.dataType;
    }

    /**
     * Get an Index object used for indexed access of this Array.
     *
     * @return an Index for this Array
     * @see Index
     */
    public Index getIndex() {
        return (Index) indexCalc.clone();
    }

    /**
     * Get an Index object used for indexed access of this Array.
     *
     * @return an Index for this Array
     */
    public Index getIndexPrivate() {
        return indexCalc;
    }

    /**
     * Get an index iterator for traversing the array in canonical order.
     *
     * @return an IndexIterator for this Array
     * @see IndexIterator
     */
    public IndexIterator getIndexIterator() {
        return indexCalc.getIndexIterator(this);
    }

    /**
     * Get the number of dimensions of the array.
     *
     * @return number of dimensions of the array
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the shape: length of array in each dimension.
     *
     * @return array whose length is the rank of this Array and whose elements
     * represent the length of each of its indices.
     */
    public int[] getShape() {
        return indexCalc.getShape();
    }

    /**
     * Get the total number of elements in the array.
     *
     * @return total number of elements in the array
     */
    public long getSize() {
        return indexCalc.getSize();
    }

    /**
     * Get the total number of bytes in the array.
     *
     * @return total number of bytes in the array
     */
    public long getSizeBytes() {
        DataType dtype = DataType.getType(getElementType());
        return indexCalc.getSize() * dtype.getSize();
    }

    /**
     * Get an index iterator for traversing a section of the array in canonical
     * order. This is equivalent to Array.section(ranges).getIterator();
     *
     * @param ranges list of Ranges that specify the array subset. Must be same
     * rank as original Array. A particular Range: 1) may be a subset, or 2) may
     * be null, meaning use entire Range.
     * @return an IndexIterator over the named range.
     * @throws InvalidRangeException if ranges is invalid
     */
    public IndexIterator getRangeIterator(List<Range> ranges) throws InvalidRangeException {
        return section(ranges).getIndexIterator();
    }

    /**
     * Get an index iterator for traversing the array in arbitrary order. Use
     * this if you dont care what order the elements are returned, eg if you are
     * summing an Array. To get an iteration in order, use getIndexIterator(),
     * which returns a fast iterator if possible.
     *
     * @return an IndexIterator for traversing the array in arbitrary order.
     * @deprecated use getIndexIterator
     */
    public IndexIterator getIndexIteratorFast() {
        return indexCalc.getIndexIteratorFast(this);
    }

    /**
     * Get the element class type of this Array
     *
     * @return the class of the element
     */
    public abstract Class getElementType();

    /**
     * Get underlying primitive array storage. Exposed for efficiency, use at
     * your own risk.
     *
     * @return underlying primitive array storage
     */
    public abstract Object getStorage();

    // So it turns out that non-public, non-protected abstract
    // methods cannot be overridden in classes in other packages.
    // If the methods are declared protected, however, they
    // can be overridden; just one of those Java things.
    // 
    // So, modify the following methods to be protected:
    //     copyFrom1DJavaArray, copyTo1DJavaArray, and createView.
    // 
    // This has consequences all over ucar.ma2.
    // used to create Array from java array
    abstract protected void copyFrom1DJavaArray(IndexIterator iter, Object javaArray);

    abstract protected void copyTo1DJavaArray(IndexIterator iter, Object javaArray);

    /**
     * create new Array with given Index and the same backing store
     *
     * @param index use this Index
     * @return a view of the Array using the given Index
     */
    abstract protected Array createView(Index index);

    /**
     * Create a new Array as a subsection of this Array, with rank reduction. No
     * data is moved, so the new Array references the same backing store as the
     * original.
     *
     * @param ranges list of Ranges that specify the array subset. Must be same
     * rank as original Array. A particular Range: 1) may be a subset, or 2) may
     * be null, meaning use entire Range. If Range[dim].length == 1, then the
     * rank of the resulting Array is reduced at that dimension.
     * @return the new Array
     * @throws InvalidRangeException if ranges is invalid
     */
    public Array section(List<Range> ranges) throws InvalidRangeException {
        return createView(indexCalc.section(ranges));
    }

    /**
     * Create a new Array as a subsection of this Array, with rank reduction. No
     * data is moved, so the new Array references the same backing store as the
     * original.
     * <p/>
     *
     * @param origin int array specifying the starting index. Must be same rank
     * as original Array.
     * @param shape int array specifying the extents in each dimension. This
     * becomes the shape of the returned Array. Must be same rank as original
     * Array. If shape[dim] == 1, then the rank of the resulting Array is
     * reduced at that dimension.
     * @return the new Array
     * @throws InvalidRangeException if ranges is invalid
     */
    public Array section(int[] origin, int[] shape) throws InvalidRangeException {
        return section(origin, shape, null);
    }

    /**
     * Create a new Array as a subsection of this Array, with rank reduction. No
     * data is moved, so the new Array references the same backing store as the
     * original.
     * <p/>
     *
     * @param origin int array specifying the starting index. Must be same rank
     * as original Array.
     * @param shape int array specifying the extents in each dimension. This
     * becomes the shape of the returned Array. Must be same rank as original
     * Array. If shape[dim] == 1, then the rank of the resulting Array is
     * reduced at that dimension.
     * @param stride int array specifying the strides in each dimension. If
     * null, assume all ones.
     * @return the new Array
     * @throws InvalidRangeException if ranges is invalid
     */
    public Array section(int[] origin, int[] shape, int[] stride) throws InvalidRangeException {
        List<Range> ranges = new ArrayList<>(origin.length);
        if (stride == null) {
            stride = new int[origin.length];
            for (int i = 0; i < stride.length; i++) {
                stride[i] = 1;
            }
        }
        for (int i = 0; i < origin.length; i++) {
            ranges.add(new Range(origin[i], origin[i] + stride[i] * shape[i] - 1, stride[i]));
        }
        return createView(indexCalc.section(ranges));
    }

    /**
     * Create a new Array as a subsection of this Array, without rank reduction.
     * No data is moved, so the new Array references the same backing store as
     * the original. Vlen is transferred over unchanged.
     *
     * @param ranges list of Ranges that specify the array subset. Must be same
     * rank as original Array. A particular Range: 1) may be a subset, or 2) may
     * be null, meaning use entire Range.
     * @return the new Array
     * @throws InvalidRangeException if ranges is invalid
     */
    public Array sectionNoReduce(List<Range> ranges) throws InvalidRangeException {
        return createView(indexCalc.sectionNoReduce(ranges));
    }

    /**
     * Create a new Array as a subsection of this Array, without rank reduction.
     * No data is moved, so the new Array references the same backing store as
     * the original.
     *
     * @param origin int array specifying the starting index. Must be same rank
     * as original Array.
     * @param shape int array specifying the extents in each dimension. This
     * becomes the shape of the returned Array. Must be same rank as original
     * Array.
     * @param stride int array specifying the strides in each dimension. If
     * null, assume all ones.
     * @return the new Array
     * @throws InvalidRangeException if ranges is invalid
     */
    public Array sectionNoReduce(int[] origin, int[] shape, int[] stride) throws InvalidRangeException {
        List<Range> ranges = new ArrayList<>(origin.length);
        if (stride == null) {
            stride = new int[origin.length];
            for (int i = 0; i < stride.length; i++) {
                stride[i] = 1;
            }
        }
        for (int i = 0; i < origin.length; i++) {
            if (shape[i] < 0) // VLEN
            {
                ranges.add(Range.VLEN);
            } else {
                ranges.add(new Range(origin[i], origin[i] + stride[i] * shape[i] - 1, stride[i]));
            }
        }
        return createView(indexCalc.sectionNoReduce(ranges));
    }

    /**
     * Create a new Array using same backing store as this Array, by fixing the
     * specified dimension at the specified index value. This reduces rank by 1.
     *
     * @param dim which dimension to fix
     * @param value at what index value
     * @return a new Array
     */
    public Array slice(int dim, int value) {
        int[] origin = new int[rank];
        int[] shape = getShape();
        origin[dim] = value;
        shape[dim] = 1;
        try {
            return sectionNoReduce(origin, shape, null).reduce(dim);  // preserve other dim 1
        } catch (InvalidRangeException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create a copy of this Array, copying the data so that physical order is
     * the same as logical order
     *
     * @return the new Array
     */
    public Array copy() {
        Array newA = factory(getDataType(), getShape());
        MAMath.copy(newA, this);
        return newA;
    }

    /**
     * Get if the this Array is view - the physical order is not same as
     * logical order
     *
     * @return Is view or not
     */
    public boolean isView() {
        return !this.getIndexPrivate().isFastIterator();
    }

    /**
     * Create a copy of this Array if it's physical order is
     * not the same as logical order
     *
     * @return the new Array
     */
    public Array copyIfView() {
        if (this.isView()) {
            return this.copy();
        } else {
            return this;
        }
    }

    /**
     * This gets the equivalent java array of the wanted type, in correct order.
     * It avoids copying if possible.
     *
     * @param wantType returned object will be an array of this type. This must
     * be convertible to it.
     * @return java array of type want
     */
    public Object get1DJavaArray(Class wantType) {
        if (wantType == getElementType()) {
            if (indexCalc.isFastIterator()) {
                return getStorage(); // already in order
            } else {
                return copyTo1DJavaArray(); // gotta copy
            }
        }

        // gotta convert to new type
        Array newA = factory(wantType, getShape());
        MAMath.copy(newA, this);
        return newA.getStorage();
    }

    /**
     * This gets the data as a ByteBuffer, in correct order. It avoids copying
     * if possible. Only for numeric types (byte, short, int, long, double,
     * float)
     *
     * @return equivilent data in a ByteBuffer
     */
    public ByteBuffer getDataAsByteBuffer() {
        throw new UnsupportedOperationException();
    }

    public ByteBuffer getDataAsByteBuffer(ByteOrder order) {
        throw new UnsupportedOperationException();
    }

    public ByteBuffer getDataAsByteBuffer(int capacity, ByteOrder order) {
        ByteBuffer bb = ByteBuffer.allocate(capacity);
        if (order != null) {
            bb.order(order);
        }
        return bb;
    }

    /**
     * Create an Array from a ByteBuffer
     *
     * @param dtype type of data
     * @param shape shape of data; if null, then use int[]{bb.limit()}
     * @param bb data is in here
     * @return equivilent Array
     */
    public static Array factory(DataType dtype, int[] shape, ByteBuffer bb) {
        int size;
        Array result;

        switch (dtype) {
            case ENUM1:
            case BYTE:
                if (shape == null) {
                    shape = new int[]{bb.limit()};
                }
                return factory(dtype, shape, bb.array());

            case CHAR:
                size = bb.limit();
                if (shape == null) {
                    shape = new int[]{size};
                }
                result = factory(dtype, shape);
                for (int i = 0; i < size; i++) {
                    result.setChar(i, (char) bb.get(i));
                }
                return result;

            case ENUM2:
            case SHORT:
                ShortBuffer sb = bb.asShortBuffer();
                size = sb.limit();
                if (shape == null) {
                    shape = new int[]{size};
                }
                result = factory(dtype, shape);
                for (int i = 0; i < size; i++) {
                    result.setShort(i, sb.get(i));
                }
                return result;

            case ENUM4:
            case INT:
                IntBuffer ib = bb.asIntBuffer();
                size = ib.limit();
                if (shape == null) {
                    shape = new int[]{size};
                }
                result = factory(dtype, shape);
                for (int i = 0; i < size; i++) {
                    result.setInt(i, ib.get(i));
                }
                return result;

            case LONG:
                LongBuffer lb = bb.asLongBuffer();
                size = lb.limit();
                if (shape == null) {
                    shape = new int[]{size};
                }
                result = factory(dtype, shape);
                for (int i = 0; i < size; i++) {
                    result.setLong(i, lb.get(i));
                }
                return result;

            case FLOAT:
                FloatBuffer ffb = bb.asFloatBuffer();
                size = ffb.limit();
                if (shape == null) {
                    shape = new int[]{size};
                }
                result = factory(dtype, shape);
                for (int i = 0; i < size; i++) {
                    result.setFloat(i, ffb.get(i));
                }
                return result;

            case DOUBLE:
                DoubleBuffer db = bb.asDoubleBuffer();
                size = db.limit();
                if (shape == null) {
                    shape = new int[]{size};
                }
                result = factory(dtype, shape);
                for (int i = 0; i < size; i++) {
                    result.setDouble(i, db.get(i));
                }
                return result;
        }

        throw new UnsupportedOperationException("" + dtype);
    }

    /**
     * Copy this array to a 1D Java primitive array of type getElementType(),
     * with the physical order of the result the same as logical order.
     *
     * @return a Java 1D array of type getElementType().
     */
    public Object copyTo1DJavaArray() {
        Array newA = copy();
        return newA.getStorage();
    }

    /**
     * Copy this array to a n-Dimensional Java primitive array of type
     * getElementType() and rank getRank(). Makes a copy of the data.
     *
     * @return a Java ND array of type getElementType().
     */
    public Object copyToNDJavaArray() {
        Object javaArray;
        try {
            javaArray = java.lang.reflect.Array.newInstance(getElementType(), getShape());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        // copy data
        IndexIterator iter = getIndexIterator();
        reflectArrayCopyOut(javaArray, this, iter);

        return javaArray;
    }

    /**
     * Create a new Array using same backing store as this Array, by flipping
     * the index so that it runs from shape[index]-1 to 0.
     *
     * @param dim dimension to flip
     * @return the new Array
     */
    public Array flip(int dim) {
        return createView(indexCalc.flip(dim));
    }

    /**
     * Create a new Array using same backing store as this Array, by transposing
     * two of the indices.
     *
     * @param dim1 transpose these two indices
     * @param dim2 transpose these two indices
     * @return the new Array
     */
    public Array transpose(int dim1, int dim2) {
        return createView(indexCalc.transpose(dim1, dim2));
    }

    /**
     * Create a new Array using same backing store as this Array, by permuting
     * the indices.
     *
     * @param dims the old index dims[k] becomes the new kth index.
     * @return the new Array
     * @throws IllegalArgumentException: wrong rank or dim[k] not valid
     */
    public Array permute(int[] dims) {
        return createView(indexCalc.permute(dims));
    }

    /**
     * Create a new Array by copying this Array to a new one with given shape
     *
     * @param shape the new shape
     * @return the new Array
     * @throws IllegalArgumentException new shape is not conformable
     */
    public Array reshape(int[] shape) {
        Array result = factory(this.getDataType(), shape);
        if (result.getSize() != getSize()) {
            throw new IllegalArgumentException("reshape arrays must have same total size");
        }

        Array.arraycopy(this, 0, result, 0, (int) getSize());
        return result;
    }

    /**
     * Reshape this array without copying data
     *
     * @param shape the new shape
     * @return the new Array, using same backing object
     * @throws IllegalArgumentException new shape is not conformable
     */
    public Array reshapeNoCopy(int[] shape) {
        Array result = factory(this.getDataType(), shape, getStorage());
        if (result.getSize() != getSize()) {
            throw new IllegalArgumentException("reshape arrays must have same total size");
        }
        return result;
    }
    
    /**
     * Reshape this array - the new shape total size may be different.
     * 
     * @param shape the new shape
     * @return the new Array
     */
    public Array reshapeVLen(int[] shape) {
        Array result = factory(this.getDataType(), shape);
        long len = Math.min(this.getSize(), result.getSize());
        Array.arraycopy(this, 0, result, 0, (int) len);
        return result;
    }

    /**
     * Create a new Array using same backing store as this Array, by eliminating
     * any dimensions with length one.
     *
     * @return the new Array, or the same array if no reduction was done
     */
    public Array reduce() {
        Index ri = indexCalc.reduce();
        if (ri == indexCalc) {
            return this;
        }
        return createView(ri);
    }

    /**
     * Create a new Array using same backing store as this Array, by eliminating
     * the specified dimension.
     *
     * @param dim dimension to eliminate: must be of length one, else
     * IllegalArgumentException
     * @return the new Array
     */
    public Array reduce(int dim) {
        return createView(indexCalc.reduce(dim));
    }

    //////////////////////////////////////////////////////////////
    /**
     * Find whether the underlying data should be interpreted as unsigned. Only
     * affects byte, short, and int. When true, conversions to wider types are
     * handled correctly.
     *
     * @return true if the data is unsigned integer type.
     */
    public boolean isUnsigned() {
        return this.dataType.isUnsigned();
    }

    /**
     * If this is a constant array
     *
     * @return If this is a constant array
     */
    public boolean isConstant() {
        return indexCalc instanceof IndexConstant;
    }

    /*
   * Set the name of one of the indices.
   * @param dim which index?
   * @param indexName name of index
   *
  public void setIndexName( int dim, String indexName) {
    indexCalc.setIndexName( dim, indexName);
  }

  /*
   * Get the name of one of the indices.
   * @param dim which index?
   * @return name of index, or null if none.
   *
  public String getIndexName( int dim) {
    return indexCalc.getIndexName( dim);
  } */
    ///////////////////////////////////////////////////
    /* these are the type-specific element accessors */
    ///////////////////////////////////////////////////
    /**
     * Get the array element at the current element of ima, as a double.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to double if necessary.
     */
    public abstract double getDouble(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     */
    public abstract void setDouble(Index ima, double value);

    /**
     * Get the array element at the current element of ima, as a float.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to float if necessary.
     */
    public abstract float getFloat(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     */
    public abstract void setFloat(Index ima, float value);

    /**
     * Get the array element at the current element of ima, as a long.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to long if necessary.
     */
    public abstract long getLong(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     */
    public abstract void setLong(Index ima, long value);

    /**
     * Get the array element at the current element of ima, as a int.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to int if necessary.
     */
    public abstract int getInt(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     */
    public abstract void setInt(Index ima, int value);

    /**
     * Get the array element at the current element of ima, as a short.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to short if necessary.
     */
    public abstract short getShort(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     */
    public abstract void setShort(Index ima, short value);

    /**
     * Get the array element at the current element of ima, as a byte.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to float if necessary.
     */
    public abstract byte getByte(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     */
    public abstract void setByte(Index ima, byte value);

    /**
     * Get the array element at the current element of ima, as a char.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to char if necessary.
     */
    public abstract char getChar(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     */
    public abstract void setChar(Index ima, char value);

    /**
     * Get the array element at the current element of ima, as a boolean.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to boolean if necessary.
     * @throws ForbiddenConversionException if underlying array not boolean
     */
    public abstract boolean getBoolean(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     * @throws ForbiddenConversionException if underlying array not boolean
     */
    public abstract void setBoolean(Index ima, boolean value);
    
    /**
     * Get the array element at the current element of ima, as a string.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to string if necessary.
     * @throws ForbiddenConversionException if underlying array not string
     */
    public abstract String getString(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     * @throws ForbiddenConversionException if underlying array not string
     */
    public abstract void setString(Index ima, String value);
    
    /**
     * Get the array element at the current element of ima, as a complex.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to boolean if necessary.
     * @throws ForbiddenConversionException if underlying array not complex
     */
    public abstract Complex getComplex(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     * @throws ForbiddenConversionException if underlying array not complex
     */
    public abstract void setComplex(Index ima, Complex value);

    /**
     * Get the array element at the current element of ima, as a LocalDateTime.
     *
     * @param ima Index with current element set
     * @return value at <code>index</code> cast to boolean if necessary.
     * @throws ForbiddenConversionException if underlying array not LocalDateTime
     */
    public abstract LocalDateTime getDate(Index ima);

    /**
     * Set the array element at the current element of ima.
     *
     * @param ima Index with current element set
     * @param value the new value; cast to underlying data type if necessary.
     * @throws ForbiddenConversionException if underlying array not LocalDateTime
     */
    public abstract void setDate(Index ima, LocalDateTime value);

    /**
     * Get the array element at index as an Object. The returned value is
     * wrapped in an object, eg Double for double
     *
     * @param ima element Index
     * @return Object value at <code>index</code>
     * @throws ArrayIndexOutOfBoundsException if index incorrect rank or out of
     * bounds
     */
    public abstract Object getObject(Index ima);

    /**
     * Set the array element at index to the specified value. the value must be
     * passed wrapped in the appropriate Object (eg Double for double)
     *
     * @param ima Index with current element set
     * @param value the new value.
     * @throws ArrayIndexOutOfBoundsException if index incorrect rank or out of
     * bounds
     * @throws ClassCastException if Object is incorrect type
     */
    abstract public void setObject(Index ima, Object value);

    //// these are for optimized access with no need to check index values
    //// elem is the index into the backing data
    abstract public double getDouble(int elem);

    abstract public void setDouble(int elem, double val);

    abstract public float getFloat(int elem);

    abstract public void setFloat(int elem, float val);

    abstract public long getLong(int elem);

    abstract public void setLong(int elem, long value);

    abstract public int getInt(int elem);

    abstract public void setInt(int elem, int value);

    abstract public short getShort(int elem);

    abstract public void setShort(int elem, short value);

    abstract public byte getByte(int elem);

    abstract public void setByte(int elem, byte value);

    abstract public char getChar(int elem);

    abstract public void setChar(int elem, char value);

    abstract public boolean getBoolean(int elem);

    abstract public void setBoolean(int elem, boolean value);
    
    abstract public String getString(int elem);
    
    abstract public void setString(int elem, String value);
    
    abstract public Complex getComplex(int elem);
    
    abstract public void setComplex(int elem, Complex value);

    abstract public LocalDateTime getDate(int elem);

    abstract public void setDate(int elem, LocalDateTime value);

    abstract public Object getObject(int elem);

    abstract public void setObject(int elem, Object value);

    @Override
    public String toString() {
        StringBuilder sbuff = new StringBuilder();
        IndexIterator ii = getIndexIterator();
        while (ii.hasNext()) {
            Object data = ii.getObjectNext();
            sbuff.append(data);
            sbuff.append(" ");
        }
        return sbuff.toString();
    }

    /**
     * Create a string representation of the shape of this Array.
     *
     * @return string representation of the shape
     */
    public String shapeToString() {
        int[] shape = getShape();
        if (shape.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i < shape.length; i++) {
            int s = shape[i];
            if (i > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Check if more elements in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     * You cannot call any of the array.nextXXX() methods without calling
     * hasNext() first. If you are not sure of the state of the iterator, you
     * must reset it before use. Example:
     * <pre>
     * arr.resetLocalIterator();
     * while (arr.hasNext()) {
     * double val = mdata.nextDouble();
     * ..
     * }
     * <.pre>
     *
     * @return true if there are more elements in the iteration
     */
    public boolean hasNext() {
        if (null == ii) {
            ii = getIndexIterator();
        }
        return ii.hasNext();
    }

    /**
     * Return the next object in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as an Object, same as IndexIterator.getObjectNext().
     */
    public Object next() {
        return ii.getObjectNext();
    }

    /**
     * Return the next double in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as a double, same as IndexIterator.getDoubleNext().
     */
    public double nextDouble() {
        return ii.getDoubleNext();
    }

    /**
     * Return the next float in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as a float, same as IndexIterator.getFloatNext().
     */
    public float nextFloat() {
        return ii.getFloatNext();
    }

    /**
     * Return the next byte in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as a byte, same as IndexIterator.getByteNext().
     */
    public byte nextByte() {
        return ii.getByteNext();
    }

    /**
     * Return the next short in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as a short, same as IndexIterator.getShortNext().
     */
    public short nextShort() {
        return ii.getShortNext();
    }

    /**
     * Return the next int in the local iterator. Uses the local iterator, which
     * is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as a int, same as IndexIterator.getIntNext().
     */
    public int nextInt() {
        return ii.getIntNext();
    }

    /**
     * Return the next long in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as a long, same as IndexIterator.getLongNext().
     */
    public long nextLong() {
        return ii.getLongNext();
    }

    /**
     * Return the next char in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as a char, same as IndexIterator.getCharNext().
     */
    public char nextChar() {
        return ii.getCharNext();
    }

    /**
     * Return the next boolean in the local iterator. Uses the local iterator,
     * which is not thread-safe. Use getIndexIterator if you need thread-safety.
     *
     * @return next element as a boolean, same as
     * IndexIterator.getBooleanNext().
     */
    public boolean nextBoolean() {
        return ii.getBooleanNext();
    }

    /**
     * Reset the local iterator. Uses the local iterator, which is not
     * thread-safe. Use getIndexIterator if you need thread-safety.
     */
    public void resetLocalIterator() {
        ii = null;
    }
}
