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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Indexes for Multidimensional arrays. An Index refers to a particular element
 * of an array.
 * <p/>
 * This is a generalization of index as int[]. Its main function is to do the
 * index arithmetic to translate an n-dim index into a 1-dim index. The user
 * obtains this by calling getIndex() on an Array. The set() and seti() routines
 * are convenience routines for 1-7 dim arrays.
 *
 * @author caron
 * @see Array
 */
public class Index implements Cloneable {

    public static final Index0D scalarIndexImmutable = new Index0D(); // immutable, so can be shared

    /**
     * Generate a subclass of Index optimized for this array's rank
     *
     * @param shape use this shape
     * @return a subclass of Index optimized for this array's rank
     */
    static public Index factory(int[] shape) {
        int rank = shape.length;
        switch (rank) {
            case 0:
                return new Index0D();
            case 1:
                return new Index1D(shape);
            case 2:
                return new Index2D(shape);
            case 3:
                return new Index3D(shape);
            case 4:
                return new Index4D(shape);
            case 5:
                return new Index5D(shape);
            case 6:
                return new Index6D(shape);
            case 7:
                return new Index7D(shape);
            default:
                return new Index(shape);
        }
    }

    private static Index factory(int rank) {
        switch (rank) {
            case 0:
                return new Index0D();
            case 1:
                return new Index1D();
            case 2:
                return new Index2D();
            case 3:
                return new Index3D();
            case 4:
                return new Index4D();
            case 5:
                return new Index5D();
            case 6:
                return new Index6D();
            case 7:
                return new Index7D();
            default:
                return new Index(rank);
        }
    }

    /**
     * Compute total number of elements in the array. Stop at vlen
     *
     * @param shape length of array in each dimension.
     * @return total number of elements in the array.
     */
    static public long computeSize(int[] shape) {
        long product = 1;
        for (int aShape : shape) {
            if (aShape < 0) {
                break; // stop at vlen
            }
            product *= aShape;
        }
        return product;
    }

    /**
     * Compute standard strides based on array's shape. Ignore vlen
     *
     * @param shape length of array in each dimension.
     * @param stride put result here
     * @return standard strides based on array's shape.
     */
    static private long computeStrides(int[] shape, int[] stride) {
        long product = 1;
        for (int ii = shape.length - 1; ii >= 0; ii--) {
            final int thisDim = shape[ii];
            if (thisDim < 0) {
                continue; // ignore vlen
            }
            stride[ii] = (int) product;
            product *= thisDim;
        }
        return product;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    protected int[] shape;
    protected int[] stride;
    protected int rank;

    protected long size; // total number of elements
    protected int offset; // element = offset + stride[0]*current[0] + ...
    private boolean fastIterator = true; // use fast iterator if in canonical order

    protected int[] current; // current element's index, used only for the general case

    protected boolean hasvlen = false;

    /**
     * General case Index - use when you want to manipulate current elements
     * yourself
     *
     * @param rank rank of the Index
     */
    protected Index(int rank) {
        this.rank = rank;
        shape = new int[rank];
        current = new int[rank];
        stride = new int[rank];
        hasvlen = false;
    }

    /**
     * Constructor for subclasses only.
     *
     * @param _shape describes an index section: slowest varying comes first
     * (row major)
     */
    protected Index(int[] _shape) {
        this.shape = new int[_shape.length];  // optimization over clone
        System.arraycopy(_shape, 0, this.shape, 0, _shape.length);

        rank = shape.length;
        current = new int[rank];
        stride = new int[rank];
        size = computeStrides(shape, stride);
        offset = 0;
        hasvlen = (shape.length > 0 && shape[shape.length - 1] < 0);
    }

    /**
     * Constructor that lets you set the strides yourself. This is used as a
     * counter, not a description of an index section.
     *
     * @param _shape Index shape
     * @param _stride Index stride
     */
    public Index(int[] _shape, int[] _stride) {
        this.shape = new int[_shape.length];  // optimization over clone
        System.arraycopy(_shape, 0, this.shape, 0, _shape.length);

        this.stride = new int[_stride.length];  // optimization over clone
        System.arraycopy(_stride, 0, this.stride, 0, _stride.length);

        rank = shape.length;
        current = new int[rank];
        size = computeSize(shape);
        offset = 0;
        hasvlen = (shape.length > 0 && shape[shape.length - 1] < 0);
    }

    /**
     * subclass specialization/optimization calculations
     */
    protected void precalc() {
    }

    /**
     * Create a new Index based on current one, except flip the index so that it
     * runs from shape[index]-1 to 0. Leave rightmost vlen alone.
     *
     * @param index dimension to flip
     * @return new index with flipped dimension
     */
    Index flip(int index) {
        if ((index < 0) || (index >= rank)) {
            throw new IllegalArgumentException();
        }

        Index i = (Index) this.clone();
        if (shape[index] >= 0) {// !vlen case
            i.offset += stride[index] * (shape[index] - 1);
            i.stride[index] = -stride[index];
        }
        i.fastIterator = false;
        i.precalc(); // any subclass-specific optimizations
        return i;
    }

    /**
     * create a new Index based on a subsection of this one, with rank reduction
     * if dimension length == 1.
     *
     * @param ranges array of Ranges that specify the array subset. Must be same
     * rank as original Array. A particular Range: 1) may be a subset; 2) may be
     * null, meaning use entire Range.
     * @return new Index, with same or smaller rank as original.
     * @throws InvalidRangeException if ranges don't match current shape
     */
    public Index section(List<Range> ranges) throws InvalidRangeException {

        // check ranges are valid
        if (ranges.size() != rank) {
            throw new InvalidRangeException("Bad ranges [] length");
        }
        for (int ii = 0; ii < rank; ii++) {
            Range r = ranges.get(ii);
            if (r == null) {
                continue;
            }
            if (r == Range.VLEN) {
                continue;
            }
            if ((r.first() < 0) || (r.first() >= shape[ii])) {
                throw new InvalidRangeException("Bad range starting value at index " + ii + " == " + r.first());
            }
            if ((r.last() < 0) || (r.last() >= shape[ii])) {
                throw new InvalidRangeException("Bad range ending value at index " + ii + " == " + r.last());
            }
        }

        int reducedRank = rank;
        for (Range r : ranges) {
            if ((r != null) && (r.length() == 1)) {
                reducedRank--;
            }
        }
        Index newindex = Index.factory(reducedRank);
        newindex.offset = offset;

        // calc shape, size, and index transformations
        // calc strides into original (backing) store
        int newDim = 0;
        for (int ii = 0; ii < rank; ii++) {
            Range r = ranges.get(ii);
            if (r == null) {          // null range means use the whole original dimension
                newindex.shape[newDim] = shape[ii];
                newindex.stride[newDim] = stride[ii];
                //if (name != null) newindex.name[newDim] = name[ii];
                newDim++;
            } else if (r.length() != 1) {
                newindex.shape[newDim] = r.length();
                newindex.stride[newDim] = stride[ii] * r.stride();
                newindex.offset += stride[ii] * r.first();
                //if (name != null) newindex.name[newDim] = name[ii];
                newDim++;
            } else {
                newindex.offset += stride[ii] * r.first();   // constant due to rank reduction
            }
        }
        newindex.size = computeSize(newindex.shape);
        newindex.fastIterator = fastIterator && (newindex.size == size); // if equal, then its not a real subset, so can still use fastIterator
        newindex.precalc(); // any subclass-specific optimizations
        return newindex;
    }

    /**
     * create a new Index based on a subsection of this one, without rank
     * reduction.
     *
     * @param ranges list of Ranges that specify the array subset. Must be same
     * rank as original Array. A particular Range: 1) may be a subset; 2) may be
     * null, meaning use entire Range.
     * @return new Index, with same rank as original.
     * @throws InvalidRangeException if ranges dont match current shape
     */
    Index sectionNoReduce(List<Range> ranges) throws InvalidRangeException {

        // check ranges are valid
        if (ranges.size() != rank) {
            throw new InvalidRangeException("Bad ranges [] length");
        }
        for (int ii = 0; ii < rank; ii++) {
            Range r = ranges.get(ii);
            if (r == null) {
                continue;
            }
            if (r == Range.VLEN) {
                continue;
            }
            if ((r.first() < 0) || (r.first() >= shape[ii])) {
                throw new InvalidRangeException("Bad range starting value at index " + ii + " == " + r.first());
            }
            if ((r.last() < 0) || (r.last() >= shape[ii])) {
                throw new InvalidRangeException("Bad range ending value at index " + ii + " == " + r.last());
            }
        }

        // allocate
        Index newindex = Index.factory(rank);
        newindex.offset = offset;

        // calc shape, size, and index transformations
        // calc strides into original (backing) store
        for (int ii = 0; ii < rank; ii++) {
            Range r = ranges.get(ii);
            if (r == null) {          // null range means use the whole original dimension
                newindex.shape[ii] = shape[ii];
                newindex.stride[ii] = stride[ii];
            } else {
                newindex.shape[ii] = r.length();
                newindex.stride[ii] = stride[ii] * r.stride();
                newindex.offset += stride[ii] * r.first();
            }
            //if (name != null) newindex.name[ii] = name[ii];
        }
        newindex.size = computeSize(newindex.shape);
        newindex.fastIterator = fastIterator && (newindex.size == size); // if equal, then its not a real subset, so can still use fastIterator
        newindex.precalc(); // any subclass-specific optimizations
        return newindex;
    }

    /**
     * Reshape a new index
     * @param shape New shape
     * @return New index
     */
    Index reshape(int[] shape) {
        boolean canReshape = true;
        for (int s : this.stride) {
            if (s != 1) {
                canReshape = false;
            }
        }
        if (!canReshape) {
            return null;
        }

        Index newIndex = Index.factory(shape);
        newIndex.offset = this.offset;
        newIndex.size = this.size;
        newIndex.fastIterator = this.fastIterator;
        newIndex.precalc(); // any subclass-specific optimizations
        return newIndex;
    }

    /**
     * Create a new Index based on current one by eliminating any dimensions
     * with length one.
     *
     * @return the new Index
     */
    Index reduce() {
        Index c = this;
        for (int ii = 0; ii < rank; ii++) {
            if (shape[ii] == 1) {  // do this on the first one you find
                Index newc = c.reduce(ii);
                return newc.reduce();  // any more to do?
            }
        }
        return c;
    }

    /**
     * Create a new Index based on current one by eliminating the specified
     * dimension;
     *
     * @param dim: dimension to eliminate: must be of length one, else
     * IllegalArgumentException
     * @return the new Index
     */
    Index reduce(int dim) {
        if ((dim < 0) || (dim >= rank)) {
            throw new IllegalArgumentException("illegal reduce dim " + dim);
        }
        if (shape[dim] != 1) {
            throw new IllegalArgumentException("illegal reduce dim " + dim + " : length != 1");
        }

        Index newindex = Index.factory(rank - 1);
        newindex.offset = offset;
        int count = 0;
        for (int ii = 0; ii < rank; ii++) {
            if (ii != dim) {
                newindex.shape[count] = shape[ii];
                newindex.stride[count] = stride[ii];
                //if (name != null) newindex.name[count] = name[ii];

                count++;
            }
        }
        newindex.size = computeSize(newindex.shape);
        newindex.fastIterator = fastIterator;
        newindex.precalc();         // any subclass-specific optimizations
        return newindex;
    }

    /**
     * create a new Index based on current one, except transpose two of the
     * indices.
     *
     * @param index1 transpose these two indices
     * @param index2 transpose these two indices
     * @return new Index with transposed indices
     */
    Index transpose(int index1, int index2) {
        if ((index1 < 0) || (index1 >= rank)) {
            throw new IllegalArgumentException();
        }
        if ((index2 < 0) || (index2 >= rank)) {
            throw new IllegalArgumentException();
        }

        Index newIndex = (Index) this.clone();
        newIndex.stride[index1] = stride[index2];
        newIndex.stride[index2] = stride[index1];
        newIndex.shape[index1] = shape[index2];
        newIndex.shape[index2] = shape[index1];
        /* if (name != null) {
      newIndex.name[index1] = name[index2];
      newIndex.name[index2] = name[index1];
    } */

        newIndex.fastIterator = false;
        newIndex.precalc(); // any subclass-specific optimizations
        return newIndex;
    }

    /**
     * create a new Index based on a permutation of the current indices; vlen
     * fails.
     *
     * @param dims: the old index dim[k] becomes the new kth index.
     * @return new Index with permuted indices
     */
    Index permute(int[] dims) {
        if (dims.length != shape.length) {
            throw new IllegalArgumentException();
        }
        for (int dim : dims) {
            if ((dim < 0) || (dim >= rank)) {
                throw new IllegalArgumentException();
            }
        }

        boolean isPermuted = false;
        Index newIndex = (Index) this.clone();
        for (int i = 0; i < dims.length; i++) {
            newIndex.stride[i] = stride[dims[i]];
            newIndex.shape[i] = shape[dims[i]];
            //if (name != null) newIndex.name[i] = name[dims[i]];
            if (i != dims[i]) {
                isPermuted = true;
            }
        }

        newIndex.fastIterator = fastIterator && !isPermuted; // useful optimization
        newIndex.precalc(); // any subclass-specific optimizations
        return newIndex;
    }

    /**
     * Get the number of dimensions in the array.
     *
     * @return the number of dimensions in the array.
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the shape: length of array in each dimension.
     *
     * @return the shape
     */
    public int[] getShape() {
        int[] result = new int[shape.length];  // optimization over clone
        System.arraycopy(shape, 0, result, 0, shape.length);
        return result;
    }

    /**
     * Get the length of the ith dimension.
     *
     * @return the ith dimension length
     * @param index which dimension. must be in [0, getRank())
     */
    public int getShape(int index) {
        return shape[index];
    }

    /**
     * Get an index iterator for traversing the array in canonical order.
     *
     * @param maa the array to iterate through
     * @return an index iterator for traversing the array in canonical order.
     * @see IndexIterator
     */
    IndexIterator getIndexIterator(Array maa) {
        if (fastIterator) {
            return new IteratorFast(size, maa);
        } else {
            return new IteratorImpl(maa);
        }
    }

    IteratorFast getIndexIteratorFast(Array maa) {
        return new IteratorFast(size, maa);
    }

    public boolean isFastIterator() {
        return fastIterator;
    }

    /**
     * Get the total number of elements in the array.
     *
     * @return the total number of elements in the array.
     */
    public long getSize() {
        return size;
    }

    /**
     * Get the current element's index into the 1D backing array. VLEN stops
     * processing.
     *
     * @return the current element's index into the 1D backing array.
     */
    public int currentElement() {
        int value = offset;                 // NB: dont have to check each index again
        for (int ii = 0; ii < rank; ii++) { // general rank
            if (shape[ii] < 0) {
                break;//vlen
            }
            value += current[ii] * stride[ii];
        }
        return value;
    }

    /**
     * Get the current counter.
     *
     * @return copy of the current counter.
     */
    public int[] getCurrentCounter() {
        return current.clone();
    }

    // only use from FastIterator, where the indices are not permuted
    /**
     * Set the current counter from the 1D "current element" currElement =
     * offset + stride[0]*current[0] + ...
     *
     * @param currElement set to this value
     */
    public void setCurrentCounter(int currElement) {
        currElement -= offset;
        for (int ii = 0; ii < rank; ii++) { // general rank
            if (shape[ii] < 0) {
                current[ii] = -1;
                break;
            }
            current[ii] = currElement / stride[ii];
            currElement -= current[ii] * stride[ii];
        }
        set(current); // transfer to subclass fields
    }

    /**
     * Set the current index from the 1D "current index" currIndex =
     * stride[0]*current[0] + ...
     *
     * @param currIndex set to this value
     */
    public void setCurrentIndex(int currIndex) {
        int[] cc = new int[rank];
        int len;
        for (int ii = 0; ii < rank; ii++) { // general rank
            len = 1;
            for (int i = ii + 1; i < rank; i++) {
                len *= shape[i];
            }
            cc[ii] = currIndex / len;
            currIndex -= len * cc[ii];
            if (currIndex == 0)
                break;
        }
        set(cc); // transfer to subclass fields
    }

    /**
     * Increment the current element by 1. Used by IndexIterator. General rank,
     * with subclass specialization. Vlen skipped.
     *
     * @return currentElement()
     */
    public int incr() {
        int digit = rank - 1;
        while (digit >= 0) {
            if (shape[digit] < 0) {
                current[digit] = -1;
                continue;
            } // do not increment vlen
            current[digit]++;
            if (current[digit] < shape[digit]) {
                break;                        // normal exit
            }
            current[digit] = 0;               // else, carry
            digit--;
        }
        return currentElement();
    }

    /**
     * Set the current element's index. General-rank case.
     *
     * @param index set current value to these values
     * @return this, so you can use A.get(i.set(i))
     * @throws ArrayIndexOutOfBoundsException if index.length != rank.
     */
    public Index set(int[] index) {
        if (index.length != rank) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (rank == 0) {
            return this;
        }
        int prefixrank = (hasvlen ? rank : rank - 1);
        System.arraycopy(index, 0, current, 0, prefixrank);
        if (hasvlen) {
            current[prefixrank] = -1;
        }
        return this;
    }

    /**
     * set current element at dimension dim to v
     *
     * @param dim set this dimension
     * @param value to this value
     */
    public void setDim(int dim, int value) {
        if (value < 0 || value >= shape[dim]) // check index here
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (shape[dim] >= 0) //!vlen
        {
            current[dim] = value;
        }
    }

    /**
     * set current element at dimension 0 to v
     *
     * @param v set 0th dimension index to this value
     * @return this, so you can use A.get(i.set(i))
     */
    public Index set0(int v) {
        setDim(0, v);
        return this;
    }

    /**
     * set current element at dimension 1 to v
     *
     * @param v set dimension 1 index to this value
     * @return this, so you can use A.get(i.set(i))
     */
    public Index set1(int v) {
        setDim(1, v);
        return this;
    }

    /**
     * set current element at dimension 2 to v
     *
     * @param v set dimension 2 index to this value
     * @return this, so you can use A.get(i.set(i))
     */
    public Index set2(int v) {
        setDim(2, v);
        return this;
    }

    /**
     * set current element at dimension 3 to v
     *
     * @param v set dimension 3 index to this value
     * @return this, so you can use A.get(i.set(i))
     */
    public Index set3(int v) {
        setDim(3, v);
        return this;
    }

    /**
     * set current element at dimension 4 to v
     *
     * @param v set dimension 4 index to this value
     * @return this, so you can use A.get(i.set(i))
     */
    public Index set4(int v) {
        setDim(4, v);
        return this;
    }

    /**
     * set current element at dimension 5 to v
     *
     * @param v set dimension 5 index to this value
     * @return this, so you can use A.get(i.set(i))
     */
    public Index set5(int v) {
        setDim(5, v);
        return this;
    }

    /**
     * set current element at dimension 6 to v
     *
     * @param v set dimension 6 index to this value
     * @return this, so you can use A.get(i.set(i))
     */
    public Index set6(int v) {
        setDim(6, v);
        return this;
    }

    /**
     * set current element at dimension 0 to v0
     *
     * @param v0 set dimension 0 index to this value
     * @return this, so you can use A.get(i.set(i))
     */
    public Index set(int v0) {
        setDim(0, v0);
        return this;
    }

    /**
     * set current element at dimension 0,1 to v0,v1
     *
     * @param v0 set dimension 0 index to this value
     * @param v1 set dimension 1 index to this value
     * @return this, so you can use A.get(i.set(i,j))
     */
    public Index set(int v0, int v1) {
        setDim(0, v0);
        setDim(1, v1);
        return this;
    }

    /**
     * set current element at dimension 0,1,2 to v0,v1,v2
     *
     * @param v0 set dimension 0 index to this value
     * @param v1 set dimension 1 index to this value
     * @param v2 set dimension 2 index to this value
     * @return this, so you can use A.get(i.set(i,j,k))
     */
    public Index set(int v0, int v1, int v2) {
        setDim(0, v0);
        setDim(1, v1);
        setDim(2, v2);
        return this;
    }

    /**
     * set current element at dimension 0,1,2,3 to v0,v1,v2,v3
     *
     * @param v0 set dimension 0 index to this value
     * @param v1 set dimension 1 index to this value
     * @param v2 set dimension 2 index to this value
     * @param v3 set dimension 3 index to this value
     * @return this, so you can use A.get(i.set(i,j,k,l))
     */
    public Index set(int v0, int v1, int v2, int v3) {
        setDim(0, v0);
        setDim(1, v1);
        setDim(2, v2);
        setDim(3, v3);
        return this;
    }

    /**
     * set current element at dimension 0,1,2,3,4 to v0,v1,v2,v3,v4
     *
     * @param v0 set dimension 0 index to this value
     * @param v1 set dimension 1 index to this value
     * @param v2 set dimension 2 index to this value
     * @param v3 set dimension 3 index to this value
     * @param v4 set dimension 4 index to this value
     * @return this, so you can use A.get(i.set(i,j,k,l,m))
     */
    public Index set(int v0, int v1, int v2, int v3, int v4) {
        setDim(0, v0);
        setDim(1, v1);
        setDim(2, v2);
        setDim(3, v3);
        setDim(4, v4);
        return this;
    }

    /**
     * set current element at dimension 0,1,2,3,4,5 to v0,v1,v2,v3,v4,v5
     *
     * @param v0 set dimension 0 index to this value
     * @param v1 set dimension 1 index to this value
     * @param v2 set dimension 2 index to this value
     * @param v3 set dimension 3 index to this value
     * @param v4 set dimension 4 index to this value
     * @param v5 set dimension 5 index to this value
     * @return this, so you can use A.get(i.set(i,j,k,l,m,n))
     */
    public Index set(int v0, int v1, int v2, int v3, int v4, int v5) {
        setDim(0, v0);
        setDim(1, v1);
        setDim(2, v2);
        setDim(3, v3);
        setDim(4, v4);
        setDim(5, v5);
        return this;
    }

    /**
     * set current element at dimension 0,1,2,3,4,5,6 to v0,v1,v2,v3,v4,v5,v6
     *
     * @param v0 set dimension 0 index to this value
     * @param v1 set dimension 1 index to this value
     * @param v2 set dimension 2 index to this value
     * @param v3 set dimension 3 index to this value
     * @param v4 set dimension 4 index to this value
     * @param v5 set dimension 5 index to this value
     * @param v6 set dimension 6 index to this value
     * @return this, so you can use A.get(i.set(i,j,k,l,m,n,p))
     */
    public Index set(int v0, int v1, int v2, int v3, int v4, int v5, int v6) {
        setDim(0, v0);
        setDim(1, v1);
        setDim(2, v2);
        setDim(3, v3);
        setDim(4, v4);
        setDim(5, v5);
        setDim(6, v6);
        return this;
    }

    /**
     * String representation
     *
     * @return String representation
     */
    public String toStringDebug() {
        StringBuilder sbuff = new StringBuilder(100);
        sbuff.setLength(0);

        sbuff.append(" shape= ");
        for (int ii = 0; ii < rank; ii++) {
            sbuff.append(shape[ii]);
            sbuff.append(" ");
        }

        sbuff.append(" stride= ");
        for (int ii = 0; ii < rank; ii++) {
            sbuff.append(stride[ii]);
            sbuff.append(" ");
        }

        /* if (name != null) {
      sbuff.append(" names= ");
      for (int ii = 0; ii < rank; ii++) {
        sbuff.append(name[ii]);
        sbuff.append(" ");
      }
    } */
        sbuff.append(" offset= ").append(offset);
        sbuff.append(" rank= ").append(rank);
        sbuff.append(" size= ").append(size);

        sbuff.append(" current= ");
        for (int ii = 0; ii < rank; ii++) {
            sbuff.append(current[ii]);
            sbuff.append(" ");
        }

        return sbuff.toString();
    }

    public String toString() {
        StringBuilder sbuff = new StringBuilder(100);
        sbuff.setLength(0);
        for (int ii = 0; ii < rank; ii++) {
            if (ii > 0) {
                sbuff.append(",");
            }
            sbuff.append(current[ii]);
        }
        return sbuff.toString();
    }

    public Object clone() {
        Index i;
        try {
            i = (Index) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
        i.stride = stride.clone();
        i.shape = shape.clone();
        i.current = new int[rank];  // want zeros

        // if (name != null) i.name = name.clone();
        return i;
    }

    //////////////////////////////////////////////////////////////
    /*
   * Set the name of one of the indices.
   *
   * @param dim       which index?
   * @param indexName name of index
   *
  public void setIndexName(int dim, String indexName) {
    if (name == null) name = new String[rank];
    name[dim] = indexName;
  }

  /*
   * Get the name of one of the indices.
   *
   * @param dim which index?
   * @return name of index, or null if none.
   *
  public String getIndexName(int dim) {
    if (name == null) return null;
    return name[dim];
  } */
    private class IteratorImpl implements IndexIterator {

        private int count = 0;
        private int currElement = 0;
        private Index counter;
        private Array maa;

        private IteratorImpl(Array maa) {
            this.maa = maa;
            counter = (Index) Index.this.clone();  // could be subtype of Index
            if (rank > 0) {
                counter.current[rank - 1] = -1;                  // avoid "if first" on every incr.
            }
            counter.precalc();
            //System.out.println("IteratorSlow");
        }

        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public String toString() {
            return counter.toString();
        }

        @Override
        public int[] getCurrentCounter() {
            return counter.getCurrentCounter();
        }

        @Override
        public Object next() {
            count++;
            currElement = counter.incr();
            return maa.getObject(currElement);
        }

        @Override
        public double getDoubleCurrent() {
            return maa.getDouble(currElement);
        }

        @Override
        public double getDoubleNext() {
            count++;
            currElement = counter.incr();
            return maa.getDouble(currElement);
        }

        @Override
        public void setDoubleCurrent(double val) {
            maa.setDouble(currElement, val);
        }

        @Override
        public void setDoubleNext(double val) {
            count++;
            currElement = counter.incr();
            maa.setDouble(currElement, val);
        }

        @Override
        public float getFloatCurrent() {
            return maa.getFloat(currElement);
        }

        @Override
        public float getFloatNext() {
            count++;
            currElement = counter.incr();
            return maa.getFloat(currElement);
        }

        @Override
        public void setFloatCurrent(float val) {
            maa.setFloat(currElement, val);
        }

        @Override
        public void setFloatNext(float val) {
            count++;
            currElement = counter.incr();
            maa.setFloat(currElement, val);
        }

        @Override
        public long getLongCurrent() {
            return maa.getLong(currElement);
        }

        @Override
        public long getLongNext() {
            count++;
            currElement = counter.incr();
            return maa.getLong(currElement);
        }

        @Override
        public void setLongCurrent(long val) {
            maa.setLong(currElement, val);
        }

        @Override
        public void setLongNext(long val) {
            count++;
            currElement = counter.incr();
            maa.setLong(currElement, val);
        }

        @Override
        public int getIntCurrent() {
            return maa.getInt(currElement);
        }

        @Override
        public int getIntNext() {
            count++;
            currElement = counter.incr();
            return maa.getInt(currElement);
        }

        @Override
        public void setIntCurrent(int val) {
            maa.setInt(currElement, val);
        }

        @Override
        public void setIntNext(int val) {
            count++;
            currElement = counter.incr();
            maa.setInt(currElement, val);
        }

        @Override
        public short getShortCurrent() {
            return maa.getShort(currElement);
        }

        @Override
        public short getShortNext() {
            count++;
            currElement = counter.incr();
            return maa.getShort(currElement);
        }

        @Override
        public void setShortCurrent(short val) {
            maa.setShort(currElement, val);
        }

        @Override
        public void setShortNext(short val) {
            count++;
            currElement = counter.incr();
            maa.setShort(currElement, val);
        }

        @Override
        public byte getByteCurrent() {
            return maa.getByte(currElement);
        }

        @Override
        public byte getByteNext() {
            count++;
            currElement = counter.incr();
            return maa.getByte(currElement);
        }

        @Override
        public void setByteCurrent(byte val) {
            maa.setByte(currElement, val);
        }

        @Override
        public void setByteNext(byte val) {
            count++;
            currElement = counter.incr();
            maa.setByte(currElement, val);
        }

        @Override
        public char getCharCurrent() {
            return maa.getChar(currElement);
        }

        @Override
        public char getCharNext() {
            count++;
            currElement = counter.incr();
            return maa.getChar(currElement);
        }

        @Override
        public void setCharCurrent(char val) {
            maa.setChar(currElement, val);
        }

        @Override
        public void setCharNext(char val) {
            count++;
            currElement = counter.incr();
            maa.setChar(currElement, val);
        }

        @Override
        public boolean getBooleanCurrent() {
            return maa.getBoolean(currElement);
        }

        @Override
        public boolean getBooleanNext() {
            count++;
            currElement = counter.incr();
            return maa.getBoolean(currElement);
        }

        @Override
        public void setBooleanCurrent(boolean val) {
            maa.setBoolean(currElement, val);
        }

        @Override
        public void setBooleanNext(boolean val) {
            count++;
            currElement = counter.incr();
            maa.setBoolean(currElement, val);
        }
        
        @Override
        public String getStringCurrent() {
            return maa.getString(currElement);
        }

        @Override
        public String getStringNext() {
            count++;
            currElement = counter.incr();
            return maa.getString(currElement);
        }

        @Override
        public void setStringCurrent(String val) {
            maa.setString(currElement, val);
        }

        @Override
        public void setStringNext(String val) {
            count++;
            currElement = counter.incr();
            maa.setString(currElement, val);
        }
        
        @Override
        public Complex getComplexCurrent() {
            return maa.getComplex(currElement);
        }

        @Override
        public Complex getComplexNext() {
            count++;
            currElement = counter.incr();
            return maa.getComplex(currElement);
        }

        @Override
        public void setComplexCurrent(Complex val) {
            maa.setComplex(currElement, val);
        }

        @Override
        public void setComplexNext(Complex val) {
            count++;
            currElement = counter.incr();
            maa.setComplex(currElement, val);
        }

        @Override
        public LocalDateTime getDateCurrent() {
            return maa.getDate(currElement);
        }

        @Override
        public LocalDateTime getDateNext() {
            count++;
            currElement = counter.incr();
            return maa.getDate(currElement);
        }

        @Override
        public void setDateCurrent(LocalDateTime val) {
            maa.setDate(currElement, val);
        }

        @Override
        public void setDateNext(LocalDateTime val) {
            count++;
            currElement = counter.incr();
            maa.setDate(currElement, val);
        }

        @Override
        public Object getObjectCurrent() {
            return maa.getObject(currElement);
        }

        @Override
        public Object getObjectNext() {
            count++;
            currElement = counter.incr();
            return maa.getObject(currElement);
        }

        @Override
        public void setObjectCurrent(Object val) {
            maa.setObject(currElement, val);
        }

        @Override
        public void setObjectNext(Object val) {
            count++;
            currElement = counter.incr();
            maa.setObject(currElement, val);
        }
    }

}
