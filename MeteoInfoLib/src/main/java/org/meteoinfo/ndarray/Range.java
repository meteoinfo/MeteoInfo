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

import net.jcip.annotations.Immutable;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Represents a set of integers, used as an index for arrays.
 * It should be considered as a subset of the interval of integers [0, length-1] inclusive.
 * For example Range(1:11:3) represents the set of integers {1,4,7,10}
 * Immutable.
 * <p/>
 * Ranges are monotonically increasing.
 * Elements must be nonnegative.
 * EMPTY is the empty Range.
 * VLEN is for variable length dimensions.
 * <p> Note last is inclusive, so standard iteration is
 * <pre>
 * for (int i=range.first(); i<=range.last(); i+= range.stride()) {
 *   ...
 * }
 * or use:
 * Range.Iterator iter = timeRange.getIterator();
 * while (iter.hasNext()) {
 *   int index = iter.next();
 *   ...
 * }
 * </pre>
 *
 * @author caron
 */

@Immutable
public final class Range {
  public static final Range EMPTY = new Range();
  public static final Range ONE = new Range(1);
  public static final Range VLEN = new Range(-1);

  private final int n; // number of elements
  private final int first; // first value in range
  private final int stride; // stride, must be >= 1
  private final String name; // optional name

  /**
   * Used for EMPTY
   */
  private Range() {
    this.n = 0;
    this.first = 0;
    this.stride = 1;
    this.name = null;
  }

  /**
   * Create a range with unit stride.
   *
   * @param first first value in range
   * @param last  last value in range, inclusive
   * @throws InvalidRangeException elements must be nonnegative, 0 <= first <= last
   */
  public Range(int first, int last) throws InvalidRangeException {
    this(null, first, last, 1);
  }

  /**
   * Create a range starting at zero, with unit stride.
   * @param length number of elements in the Rnage
   */
  public Range(int length) {
    this.name = null;
    this.first = 0;
    this.stride = 1;
    this.n = length;
    assert (this.n != 0);
  }

  /**
   * Create a named range with unit stride.
   *
   * @param name  name of Range
   * @param first first value in range
   * @param last  last value in range, inclusive
   * @throws InvalidRangeException elements must be nonnegative, 0 <= first <= last
   */
  public Range(String name, int first, int last) throws InvalidRangeException {
    this(name, first, last, 1);
  }


  /**
   * Create a range with a specified stride.
   *
   * @param first  first value in range
   * @param last   last value in range, inclusive
   * @param stride stride between consecutive elements, must be > 0
   * @throws InvalidRangeException elements must be nonnegative: 0 <= first <= last, stride > 0
   */
  public Range(int first, int last, int stride) throws InvalidRangeException {
    this(null, first, last, stride);
  }

  /**
   * Create a named range with a specified stride.
   *
   * @param name   name of Range
   * @param first  first value in range
   * @param last   last value in range, inclusive
   * @param stride stride between consecutive elements, must be > 0
   * @throws InvalidRangeException elements must be nonnegative: 0 <= first <= last, stride > 0
   */
  public Range(String name, int first, int last, int stride) throws InvalidRangeException {
    if (first < 0)
      throw new InvalidRangeException("first ("+first+") must be >= 0");
    if (last < first)
      throw new InvalidRangeException("last ("+last+") must be >= first ("+first+")");
    if (stride < 1)
      throw new InvalidRangeException("stride ("+stride+") must be > 0");
    this.name = name;
    this.first = first;
    this.stride = stride;
    this.n = Math.max(1 + (last - first) / stride, 1);
    assert this.n != 0;
  }

  /**
   * Copy Constructor
   *
   * @param r copy from here
   */
  public Range(Range r) {
    first = r.first();
    n = r.length();
    stride = r.stride();
    name = r.getName();
    assert this.n != 0;
  }

  /**
   * Copy Constructor with name
   *
   * @param name result name
   * @param r    copy from here
   */
  public Range(String name, Range r) {
    this.name = name;
    first = r.first();
    n = r.length();
    stride = r.stride();
    assert this.n != 0;
  }


  /**
   * Create a new Range by composing a Range that is reletive to this Range.
   * Revised 2013/04/19 by Dennis Heimbigner to handle edge cases.
   * See the commentary associated with the netcdf-c file dceconstraints.h,
   * function dceslicecompose().
   *
   * @param r range reletive to base
   * @return combined Range, may be EMPTY
   * @throws InvalidRangeException elements must be nonnegative, 0 <= first <= last
   */
  public Range compose(Range r) throws InvalidRangeException {
    if ((length() == 0) || (r.length() == 0))
      return EMPTY;
    if (this == VLEN || r == VLEN)
      return VLEN;
if(false) {// Original version
    // Note that this version assumes that range r is
    // correct with respect to this.
    int first = element(r.first());
    int stride = stride() * r.stride();
    int last = element(r.last());
    return new Range(name, first, last, stride);
} else {//new version: handles versions all values of r.
    int sr_stride = stride() * r.stride();
    int sr_first  = element(r.first()); // MAP(this,i) == element(i)
    int lastx = element(r.last());
    int sr_last = (last() < lastx ? last() : lastx); //min(last(),lastx)
    //unused int sr_length = (sr_last + 1) - sr_first;
    return new Range(name, sr_first, sr_last, sr_stride);
}
  }

  /**
   * Create a new Range by compacting this Range by removing the stride.
   * first = first/stride, last=last/stride, stride=1.
   *
   * @return compacted Range
   * @throws InvalidRangeException elements must be nonnegative, 0 <= first <= last
   */
  public Range compact() throws InvalidRangeException {
    if (stride() == 1) return this;
    int first = first() / stride();
    int last = first + length() - 1;
    return new Range(name, first, last, 1);
  }

  /**
   * Create a new Range shifting this range by a constant factor.
   *
   * @param origin subtract this from first, last
   * @return shiften range
   * @throws InvalidRangeException elements must be nonnegative, 0 <= first <= last
   */
  public Range shiftOrigin(int origin) throws InvalidRangeException {
    if (this == VLEN)
      return VLEN;

    int first = first() - origin;
    int stride = stride();
    int last = last() - origin;
    return new Range(name, first, last, stride);
  }

  /**
   * Create a new Range by intersecting with a Range using same interval as this Range.
   * NOTE: we dont yet support intersection when both Ranges have strides
   *
   * @param r range to intersect
   * @return intersected Range, may be EMPTY
   * @throws InvalidRangeException elements must be nonnegative
   */
  public Range intersect(Range r) throws InvalidRangeException {
    if ((length() == 0) || (r.length() == 0))
      return EMPTY;
    if (this == VLEN || r == VLEN)
      return VLEN;

    int last = Math.min(this.last(), r.last());
    int stride = stride() * r.stride();

    int useFirst;
    if (stride == 1) {
      useFirst = Math.max(this.first(), r.first());

    } else if (stride() == 1) { // then r has a stride

      if (r.first() >= first())
        useFirst = r.first();
      else {
        int incr = (first() - r.first()) / stride;
        useFirst = r.first() + incr * stride;
        if (useFirst < first()) useFirst += stride;
      }

    } else if (r.stride() == 1) { // then this has a stride

      if (first() >= r.first())
        useFirst = first();
      else {
        int incr = (r.first() - first()) / stride;
        useFirst = first() + incr * stride;
        if (useFirst < r.first()) useFirst += stride;
      }

    } else {
      throw new UnsupportedOperationException("Intersection when both ranges have a stride");
    }

    if (useFirst > last)
      return EMPTY;
    return new Range(name, useFirst, last, stride);
  }

  /**
   * Determine if a given Range intersects this one.
   * NOTE: we dont yet support intersection when both Ranges have strides
   *
   * @param r range to intersect
   * @return true if they intersect
   * @throws UnsupportedOperationException if both Ranges have strides
   */
  public boolean intersects(Range r) {
    if ((length() == 0) || (r.length() == 0))
      return false;
    if (this == VLEN || r == VLEN)
      return true;

    int last = Math.min(this.last(), r.last());
    int stride = stride() * r.stride();

    int useFirst;
    if (stride == 1) {
      useFirst = Math.max(this.first(), r.first());

    } else if (stride() == 1) { // then r has a stride

      if (r.first() >= first())
        useFirst = r.first();
      else {
        int incr = (first() - r.first()) / stride;
        useFirst = r.first() + incr * stride;
        if (useFirst < first()) useFirst += stride;
      }

    } else if (r.stride() == 1) { // then this has a stride

      if (first() >= r.first())
        useFirst = first();
      else {
        int incr = (r.first() - first()) / stride;
        useFirst = first() + incr * stride;
        if (useFirst < r.first()) useFirst += stride;
      }

    } else {
      throw new UnsupportedOperationException("Intersection when both ranges have a stride");
    }

    return (useFirst <= last);
  }


  /**
   * If this range is completely past the wanted range
   * @param want desired range
   * @return true if  first() > want.last()
   */
  public boolean past(Range want) {
    return (first() > want.last());
  }

  /**
   * Create a new Range by making the union with a Range using same interval as this Range.
   * NOTE: no strides
   *
   * @param r range to add
   * @return intersected Range, may be EMPTY
   * @throws InvalidRangeException elements must be nonnegative
   */
  public Range union(Range r) throws InvalidRangeException {
    if (length() == 0)
      return r;
    if (this == VLEN || r == VLEN)
      return VLEN;

    if (r.length() == 0)
      return this;

    int first = Math.min(this.first(), r.first());
    int last = Math.max(this.last(), r.last());
    return new Range(name, first, last);
  }

  /**
   * Get the number of elements in the range.
   * @return the number of elements in the range.
   */
  public int length() {
    return n;
  }

  /**
   * Get ith element
   *
   * @param i index of the element
   * @return the i-th element of a range.
   * @throws InvalidRangeException i must be: 0 <= i < length
   */
  public int element(int i) throws InvalidRangeException {
    if (i < 0)
      throw new InvalidRangeException("i must be >= 0");
    if (i >= n)
      throw new InvalidRangeException("i must be < length");

    return first + i * stride;
  }

  // inverse of element
  /**
   * Get the index for this element: inverse of element
   * @param elem the element of the range
   * @return index
   * @throws InvalidRangeException if illegal elem
   */
  public int index(int elem) throws InvalidRangeException {
    if (elem < first)
      throw new InvalidRangeException("elem must be >= first");
    int result = (elem - first) / stride;
    if (result > n)
      throw new InvalidRangeException("elem must be <= first = n * stride");
    return result;
  }

  /**
   * Is the ith element contained in this Range?
   *
   * @param i index in the original Range
   * @return true if the ith element would be returned by the Range iterator
   */
  public boolean contains(int i) {
    if (i < first())
      return false;
    if (i > last())
      return false;
    if (stride == 1) return true;
    return (i - first) % stride == 0;
  }

  /**
   * Get ith element; skip checking, for speed.
   *
   * @param i index of the element
   * @return the i-th element of a range, no check
   */
  private int elementNC(int i) {
    return first + i * stride;
  }

  /**
   * @return first in range
   */
  public int first() {
    return first;
  }

  /**
   * @return last in range, inclusive
   */
  public int last() {
    return first + (n - 1) * stride;
  }

  /**
   * @return stride, must be >= 1
   */
  public int stride() {
    return stride;
  }


  /**
   * Get name
   *
   * @return name, or null if none
   */
  public String getName() {
    return name;
  }

  /**
   * Iterate over Range index
   * Usage: <pre>
   * Iterator iter = range.getIterator();
   * while (iter.hasNext()) {
   *   int index = iter.next();
   *   doSomething(index);
   * }
   * </pre>
   *
   * @return Iterator over element indices
   */
  public Iterator getIterator() {
    return new Iterator();
  }

  public class Iterator {
    private int current = 0;

    public boolean hasNext() {
      return current < n;
    }

    public int next() {
      return elementNC(current++);
    }
  }

  /**
   * Find the first element in a strided array after some index start.
   * Return the smallest element k in the Range, such that <ul>
   * <li>k >= first
   * <li>k >= start
   * <li>k <= last
   * <li>k = first + i * stride for some integer i.
   * </ul>
   *
   * @param start starting index
   * @return first in interval, else -1 if there is no such element.
   */
  public int getFirstInInterval(int start) {
    if (start > last()) return -1;
    if (start <= first) return first;
    if (stride == 1) return start;
    int offset = start - first;
    int i = offset/stride;
    i = (offset % stride == 0) ? i : i+1; // round up
    return first + i * stride;
  }

  public String toString()
  {
    if(this.n == 0)
        return "EMPTY";
    else if(this.n < 0)
        return "VLEN";
    else
      return first + ":" + last() + (stride > 1 ? ":" + stride : "");
  }

  /**
   * Range elements with same first, last, stride are equal.
   */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Range)) return false;   // this catches nulls
    Range or = (Range) o;

    if ((n == 0) && (or.n == 0)) // empty ranges are equal
      return true;

    return (or.first == first) && (or.n == n) && (or.stride == stride);
  }

  /**
   * Override Object.hashCode() to implement equals.
   */
  public int hashCode() {
    int result = first();
    result = 37 * result + last();
    result = 37 * result + stride();
    return result;
  }


  //////////////////////////////////////////////////////////////////////////
  // deprecated
  /**
   * @return Minimum index, inclusive.
   * @deprecated use first()
   */
  public int min() {
    if (n > 0) {
      if (stride > 0)
        return first;
      else
        return first + (n - 1) * stride;
    } else {
      return first;
    }
  }

  /**
   * @return Maximum index, inclusive.
   * @deprecated use last()
   */
  public int max() {
    if (n > 0) {
      if (stride > 0)
        return first + (n - 1) * stride;
      else
        return first;
    } else {
      if (stride > 0)
        return first - 1;
      else
        return first + 1;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // deprecated - use Section

  /**
   * Convert shape array to List of Ranges. Assume 0 origin for all.
   *
   * @deprecated use Section(int[] shape)
   */
  public static List factory(int[] shape) {
    ArrayList result = new ArrayList();
    for (int i = 0; i < shape.length; i++) {
      try {
        result.add(new Range(0, Math.max(shape[i] - 1, -1)));
      } catch (InvalidRangeException e) {
        return null;
      }
    }
    return result;
  }

  /**
   * Check rangeList has no nulls, set from shape array.
   *
   * @deprecated use Section.setDefaults(int[] shape)
   */
  public static List setDefaults(List rangeList, int[] shape) {
    try {
      // entire rangeList is null
      if (rangeList == null) {
        rangeList = new ArrayList();
        for (int i = 0; i < shape.length; i++) {
          rangeList.add(new Range(0, shape[i]));
        }
        return rangeList;
      }

      // check that any individual range is null
      for (int i = 0; i < shape.length; i++) {
        Range r = (Range) rangeList.get(i);
        if (r == null) {
          rangeList.set(i, new Range(0, shape[i] - 1));
        }
      }
      return rangeList;
    }
    catch (InvalidRangeException ex) {
      return null; // could happen if shape[i] is negetive
    }
  }

  /**
   * Convert shape, origin array to List of Ranges.
   *
   * @deprecated use Section(int[] origin, int[] shape)
   */
  public static List factory(int[] origin, int[] shape) throws InvalidRangeException {
    ArrayList result = new ArrayList();
    for (int i = 0; i < shape.length; i++) {
      try {
        result.add(new Range(origin[i], origin[i] + shape[i] - 1));
      } catch (Exception e) {
        throw new InvalidRangeException(e.getMessage());
      }
    }
    return result;
  }

  /**
   * Convert List of Ranges to shape array using the range.length.
   *
   * @deprecated use Section.getShape()
   */
  public static int[] getShape(List ranges) {
    if (ranges == null) return null;
    int[] result = new int[ranges.size()];
    for (int i = 0; i < ranges.size(); i++) {
      result[i] = ((Range) ranges.get(i)).length();
    }
    return result;
  }

  /**
   * @deprecated use Section.toString()
   */
  public static String toString(List ranges) {
    if (ranges == null) return "";
    StringBuilder sbuff = new StringBuilder();
    for (int i = 0; i < ranges.size(); i++) {
      if (i > 0) sbuff.append(",");
      sbuff.append(((Range) ranges.get(i)).length());
    }
    return sbuff.toString();
  }

  /**
   * /** Compute total number of elements represented by the section.
   *
   * @param section List of Range objects
   * @return total number of elements
   * @deprecated use Section.computeSize()
   */
  static public long computeSize(List section) {
    int[] shape = getShape(section);
    return Index.computeSize(shape);
  }

  /**
   * Append a new Range(0,size-1) to the list
   *
   * @param ranges list of Range
   * @param size   add this Range
   * @return same list
   * @throws InvalidRangeException if size < 1
   * @deprecated use Section.appendRange(int size)
   */
  public static List appendShape(List ranges, int size) throws InvalidRangeException {
    ranges.add(new Range(0, size - 1));
    return ranges;
  }

  /**
   * Convert List of Ranges to origin array using the range.first.
   *
   * @deprecated use Section.getOrigin()
   */
  public static int[] getOrigin(List ranges) {
    if (ranges == null) return null;
    int[] result = new int[ranges.size()];
    for (int i = 0; i < ranges.size(); i++) {
      result[i] = ((Range) ranges.get(i)).first();
    }
    return result;
  }

  /**
   * Convert List of Ranges to array of Ranges.  *
   *
   * @deprecated use Section.getRanges()
   */
  public static Range[] toArray(List ranges) {
    if (ranges == null) return null;
    return (Range[]) ranges.toArray(new Range[ranges.size()]);
  }

  /**
   * Convert array of Ranges to List of Ranges.
   *
   * @deprecated use Section.getRanges()
   */
  public static List toList(Range[] ranges) {
    if (ranges == null) return null;
    return java.util.Arrays.asList(ranges);
  }

  /**
   * Convert List of Ranges to String Spec.
   * Inverse of parseSpec
   *
   * @deprecated use Section.toString()
   */
  public static String makeSectionSpec(List ranges) {
    StringBuilder sbuff = new StringBuilder();
    for (int i = 0; i < ranges.size(); i++) {
      Range r = (Range) ranges.get(i);
      if (i > 0) sbuff.append(",");
      sbuff.append(r.toString());
    }
    return sbuff.toString();
  }


  /**
   * Parse an index section String specification, return equivilent list of ucar.ma2.Range objects.
   * The sectionSpec string uses fortran90 array section syntax, namely:
   * <pre>
   *   sectionSpec := dims
   *   dims := dim | dim, dims
   *   dim := ':' | slice | start ':' end | start ':' end ':' stride
   *   slice := INTEGER
   *   start := INTEGER
   *   stride := INTEGER
   *   end := INTEGER
   * <p/>
   * where nonterminals are in lower case, terminals are in upper case, literals are in single quotes.
   * <p/>
   * Meaning of index selector :
   *  ':' = all
   *  slice = hold index to that value
   *  start:end = all indices from start to end inclusive
   *  start:end:stride = all indices from start to end inclusive with given stride
   * <p/>
   * </pre>
   *
   * @param sectionSpec the token to parse, eg "(1:20,:,3,10:20:2)", parenthesis optional
   * @return return List of ucar.ma2.Range objects corresponding to the index selection. A null
   *         Range means "all" (i.e.":") indices in that dimension.
   * @throws IllegalArgumentException when sectionSpec is misformed
   * @deprecated use new Section(String sectionSpec)
   */
  public static List parseSpec(String sectionSpec) throws InvalidRangeException {

    ArrayList result = new ArrayList();
    Range section;

    StringTokenizer stoke = new StringTokenizer(sectionSpec, "(),");
    while (stoke.hasMoreTokens()) {
      String s = stoke.nextToken().trim();
      if (s.equals(":"))
        section = null; // all

      else if (s.indexOf(':') < 0) { // just a number : slice
        try {
          int index = Integer.parseInt(s);
          section = new Range(index, index);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException(" illegal selector: " + s + " part of <" + sectionSpec + ">");
        }

      } else {  // gotta be "start : end" or "start : end : stride"
        StringTokenizer stoke2 = new StringTokenizer(s, ":");
        String s1 = stoke2.nextToken();
        String s2 = stoke2.nextToken();
        String s3 = stoke2.hasMoreTokens() ? stoke2.nextToken() : null;
        try {
          int index1 = Integer.parseInt(s1);
          int index2 = Integer.parseInt(s2);
          int stride = (s3 != null) ? Integer.parseInt(s3) : 1;
          section = new Range(index1, index2, stride);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException(" illegal selector: " + s + " part of <" + sectionSpec + ">");
        }
      }

      result.add(section);
    }

    return result;
  }

  /**
   * Check ranges are valid
   *
   * @param section
   * @param shape
   * @return error message, or null if all ok
   * @deprecated use Section.checkInRange(int shape[])
   */
  public static String checkInRange(List section, int shape[]) {
    if (section.size() != shape.length)
      return "Number of ranges in section must be =" + shape.length;
    for (int i = 0; i < section.size(); i++) {
      Range r = (Range) section.get(i);
      if (r == null) continue;
      if (r.last() >= shape[i])
        return "Illegal range for dimension " + i + ": requested " + r.last() + " >= max " + shape[i];
    }

    return null;
  }
}
