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

import java.util.*;

/**
 * A section of multidimensional array indices.
 * Represented as List<Range>.
 * Immutable if makeImmutable() was called.
 *
 * @author caron
 */

public class Section {
  private List<Range> list;
  private boolean immutable = false;
  private boolean isvariablelength = false;

  /**
   * Create Section from a shape array, assumes 0 origin.
   *
   * @param shape array of lengths for each Range. 0 = EMPTY, < 0 = VLEN
   */
  public Section(int[] shape) {
    list = new ArrayList<>();
    for (int aShape : shape) {
      if (aShape > 0)
        list.add(new Range(aShape));
      else if (aShape == 0)
        list.add(Range.EMPTY);
      else {
        list.add(Range.VLEN);
        isvariablelength = true;
      }
    }
  }

  /**
   * Create Section from a shape and origin arrays.
   *
   * @param origin array of start for each Range
   * @param shape  array of lengths for each Range
   * @throws InvalidRangeException if origin < 0, or shape < 1.
   */
  public Section(int[] origin, int[] shape) throws InvalidRangeException {
    list = new ArrayList<>();
    for (int i = 0; i < shape.length; i++) {
      if (shape[i] > 0)
        list.add(new Range(origin[i], origin[i] + shape[i] - 1));
      else if (shape[i] == 0)
        list.add(Range.EMPTY);
      else {
        list.add(Range.VLEN);
        isvariablelength = true;
      }
    }
  }

  /**
   * Create Section from a shape, origin, and stride arrays.
   *
   * @param origin array of start for each Range
   * @param size   array of lengths for each Range (last = origin + size -1)
   * @param stride stride between consecutive elements, must be > 0
   * @throws InvalidRangeException if origin < 0, or shape < 1.
   */
  public Section(int[] origin, int[] size, int[] stride) throws InvalidRangeException {
    list = new ArrayList<>();
    for (int i = 0; i < size.length; i++) {
      if (size[i] > 0)
        list.add(new Range(origin[i], origin[i] + size[i] - 1, stride[i]));
      else if (size[i] == 0)
        list.add(Range.EMPTY);
      else {
        list.add(Range.VLEN);
        isvariablelength = true;
      }
    }
  }

  /**
   * Create Section from a List<Range>.
   *
   * @param from the list of Range
   */
  public Section(List<Range> from) {
    list = new ArrayList<>(from);
    for (Range aFrom : from) {
      if (aFrom == Range.VLEN)
        isvariablelength = true;
    }
  }

  /**
   * Create Section from a variable length list of Ranges
   *
   * @param ranges the list
   */
  public Section(Range... ranges) {
    list = new ArrayList<>();
    for (Range r : ranges) {
      list.add(r);
      if (r == Range.VLEN)
        isvariablelength = true;
    }
  }

  /**
   * Copy Constructor.
   * Returned copy is mutable
   *
   * @param from the Section to copy
   */
  public Section(Section from) {
    list = new ArrayList<>(from.getRanges());
  }

  /**
   * Create Section from a List<Range>.
   *
   * @param from  the list of Range
   * @param shape use this as default shape if any of the ranges are null.
   * @throws InvalidRangeException if shape and range list done match
   */
  public Section(List<Range> from, int[] shape) throws InvalidRangeException {
    list = new ArrayList<>(from);
    setDefaults(shape);
  }

  /**
   * Return a Section guaranteed to be non null, with no null Ranges, and within the bounds set by shape.
   * A section with no nulls is called "filled".
   * If s is already filled, return it, otherwise return a new Section, filled from the shape.
   *
   * @param s     the original Section, may be null or not filled
   * @param shape use this as default shape if any of the ranges are null.
   * @return a filled Section
   * @throws InvalidRangeException if shape and s and shape rank dont match, or if s has invalid range compared to shape
   */
  static public Section fill(Section s, int[] shape) throws InvalidRangeException {
    // want all
    if (s == null)
      return new Section(shape);

    String errs = s.checkInRange(shape);
    if (errs != null)
      throw new InvalidRangeException(errs);

    // if s is already filled, use it
    boolean ok = true;
    for (int i = 0; i < shape.length; i++)
      ok &= (s.getRange(i) != null);
    if (ok) return s;

    // fill in any nulls
    return new Section(s.getRanges(), shape);
  }

  /* public Section subsection(int startElement, int endElement) throws InvalidRangeException {
    int[] shape = getShape();
    Index index = Index.factory( shape);
    index.setCurrentCounter(startElement);
    int[] startShape = index.getCurrentCounter();
    index.setCurrentCounter(endElement);
    int[] endShape = index.getCurrentCounter();
    Section result = new Section();
    for (int i=0; i<getRank();i++)
      result.appendRange(startShape[i], endShape[i] - 1);
    return result;
  } */

  /**
   * Parse an index section String specification, return equivilent Section.
   * A null Range means "all" (i.e.":") indices in that dimension.
   * <p/>
   * The sectionSpec string uses fortran90 array section syntax, namely:
   * <pre>
   *   sectionSpec := dims
   *   dims := dim | dim, dims
   *   dim := ':' | slice | start ':' end | start ':' end ':' stride
   *   slice := INTEGER
   *   start := INTEGER
   *   stride := INTEGER
   *   end := INTEGER
   *
   * where nonterminals are in lower case, terminals are in upper case, literals are in single quotes.
   *
   * Meaning of index selector :
   *  ':' = all
   *  slice = hold index to that value
   *  start:end = all indices from start to end inclusive
   *  start:end:stride = all indices from start to end inclusive with given stride
   *
   * </pre>
   *
   * @param sectionSpec the token to parse, eg "(1:20,:,3,10:20:2)", parenthesis optional
   * @throws InvalidRangeException    when the Range is illegal
   * @throws IllegalArgumentException when sectionSpec is misformed
   */
  public Section(String sectionSpec) throws InvalidRangeException {

    list = new ArrayList<>();
    Range range;

    StringTokenizer stoke = new StringTokenizer(sectionSpec, "(),");
    while (stoke.hasMoreTokens()) {
      String s = stoke.nextToken().trim();
      if (s.equals(":"))
        range = null; // all

      else if (s.indexOf(':') < 0) { // just a number : slice
        try {
          int index = Integer.parseInt(s);
          range = new Range(index, index);
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
          range = new Range(index1, index2, stride);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException(" illegal selector: " + s + " part of <" + sectionSpec + ">");
        }
      }
      list.add(range);
    }

  }

  /**
   * Create a new Section by compacting each Range.
   * first = first/stride, last=last/stride, stride=1.
   *
   * @return compacted Section
   * @throws InvalidRangeException elements must be nonnegative, 0 <= first <= last
   */
  public Section compact() throws InvalidRangeException {
    List<Range> results = new ArrayList<>(getRank());
    for (Range r : list) {
      results.add(r.compact());
    }
    return new Section(results);
  }


  /**
   * Create a new Section by compacting each Range.
   * first = first/stride, last=last/stride, stride=1.
   *
   * @return compacted Section
   * @throws InvalidRangeException elements must be nonnegative, 0 <= first <= last
   */
  public Section removeVlen() throws InvalidRangeException {
    boolean need = false;
    for (Range r : list) {
      if (r == Range.VLEN) need = true;
    }
    if (!need) return this;

    List<Range> results = new ArrayList<>(getRank());
    for (Range r : list) {
      if (r != Range.VLEN) results.add(r);
    }
    return new Section(results);
  }


  /**
   * Create a new Section by composing with a Section that is reletive to this Section.
   *
   * @param want Section reletive to this one. If null, return this. If individual ranges are null, use corresponding Range in this.
   * @return new Section, composed
   * @throws InvalidRangeException if want.getRank() not equal to this.getRank(), or invalid component Range
   */
  public Section compose(Section want) throws InvalidRangeException {
    // all nulls
    if (want == null) return this; // LOOK maybe a copy ??

    if (want.getRank() != getRank())
      throw new InvalidRangeException("Invalid Section rank");

    // check individual nulls
    List<Range> results = new ArrayList<>(getRank());
    for (int j = 0; j < list.size(); j++) {
      Range base = list.get(j);
      Range r = want.getRange(j);

      if (r == null)
        results.add(base);
      else
        results.add(base.compose(r));
    }

    return new Section(results);
  }

  /**
   * Create a new Section by intersection with another Section
   *
   * @param other Section other section
   * @return new Section, composed
   * @throws InvalidRangeException if want.getRank() not equal to this.getRank(), or invalid component Range
   */
  public Section intersect(Section other) throws InvalidRangeException {
    if (!compatibleRank(other))
      throw new InvalidRangeException("Invalid Section rank");

    // check individual nulls
    List<Range> results = new ArrayList<>(getRank());
    for (int j = 0; j < list.size(); j++) {
      Range base = list.get(j);
      Range r = other.getRange(j);
      results.add(base.intersect(r));
    }

    return new Section(results);
  }

  public int offset(Section intersect) throws InvalidRangeException {
    if (!compatibleRank(intersect))
      throw new InvalidRangeException("Invalid Section rank");

    int result = 0;
    int stride = 1;
    for (int j = list.size() - 1; j >= 0; j--) {
      Range base = list.get(j);
      Range r = intersect.getRange(j);
      int offset = base.index(r.first());
      result += offset * stride;
      stride *= base.length();
    }

    return result;
  }

  /**
   * Create a new Section by union with another Section
   *
   * @param other Section other section
   * @return new Section, union of the two
   * @throws InvalidRangeException if want.getRank() not equal to this.getRank(), or invalid component Range
   */
  public Section union(Section other) throws InvalidRangeException {
    if (other.getRank() != getRank())
      throw new InvalidRangeException("Invalid Section rank");

    List<Range> results = new ArrayList<>(getRank());
    for (int j = 0; j < list.size(); j++) {
      Range base = list.get(j);
      Range r = other.getRange(j);
      results.add(base.union(r));
    }

    return new Section(results);
  }

  /**
   * Create a new Section by shifting each range by newOrigin.first()
   * The result is then a reletive offset from the newOrigin.
   *
   * @param newOrigin this becomes the origin of the result
   * @return new Section, shifted
   * @throws InvalidRangeException if want.getRank() not equal to this.getRank()
   */
  public Section shiftOrigin(Section newOrigin) throws InvalidRangeException {
    if (newOrigin.getRank() != getRank())
      throw new InvalidRangeException("Invalid Section rank");

    // check individual nulls
    List<Range> results = new ArrayList<>(getRank());
    for (int j = 0; j < list.size(); j++) {
      Range base = list.get(j);
      Range r = newOrigin.getRange(j);
      results.add(base.shiftOrigin(r.first()));
    }

    return new Section(results);
  }

  /**
   * See if this Section intersects with another Section. ignores strides
   *
   * @param other another section
   * @return true if intersection is non-empty
   * @throws InvalidRangeException if want.getRank() not equal to this.getRank(),
   *                               ignoring vlen
   */
  public boolean intersects(Section other) throws InvalidRangeException {
    if (!compatibleRank(other))
      throw new InvalidRangeException("Invalid Section rank");

    for (int j = 0; j < list.size(); j++) {
      Range base = list.get(j);
      Range r = other.getRange(j);
      if (base == Range.VLEN || r == Range.VLEN)
        continue;
      if (!base.intersects(r))
        return false;
    }

    return true;
  }


  /**
   * See if this Section contains another Section. ignores strides
   * Must have same rank and last >= other.last.
   *
   * @param other another section
   * @return true if its a subset
   */
  public boolean contains(Section other) {
    if (other.getRank() != getRank())
      return false;

    for (int j = 0; j < list.size(); j++) {
      Range base = list.get(j);
      Range r = other.getRange(j);
      if (base.first() > r.first())
        return false;
      if (base.last() < r.last())
        return false;
    }

    return true;
  }


  /**
   * Convert List of Ranges to String Spec.
   * Inverse of new Section(String sectionSpec)
   *
   * @return index section String specification
   */
  public String toString() {
    StringBuilder sbuff = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      Range r = list.get(i);
      if (i > 0) sbuff.append(",");
      if (r == null)
        sbuff.append(":");
      else
        sbuff.append(r.toString());
    }
    return sbuff.toString();
  }

  /**
   * No-arg Constructor
   */
  public Section() {
    list = new ArrayList<>();
  }

  // these make it mutable

  /**
   * Append a null Range to the Section - meaning "all"
   *
   * @return this
   */
  public Section appendRange() {
    if (immutable) throw new IllegalStateException("Cant modify");
    list.add(null);
    return this;
  }

  /**
   * Append a Range to the Section
   *
   * @return this
   */
  public Section appendRange(Range r) {
    if (immutable) throw new IllegalStateException("Cant modify");
    list.add(r);
    return this;
  }

  /**
   * Append a new Range(0,size-1) to the Section
   *
   * @param size add this Range
   * @return this
   * @throws InvalidRangeException if size < 1
   */
  public Section appendRange(int size) throws InvalidRangeException {
    if (immutable) throw new IllegalStateException("Cant modify");
    if (size > 0)
      list.add(new Range(size));
    else if (size == 0)
      list.add(Range.EMPTY);
    else
      list.add(Range.VLEN);
    return this;
  }

  /**
   * Append a new Range(first, last) to the Section
   *
   * @param first starting index
   * @param last  last index, inclusive
   * @return this
   * @throws InvalidRangeException if last < first
   */
  public Section appendRange(int first, int last) throws InvalidRangeException {
    if (immutable) throw new IllegalStateException("Cant modify");
    if (last < 0)
      list.add(Range.VLEN);
    else
      list.add(new Range(first, last));
    return this;
  }

  /**
   * Append a new Range(first,last,stride) to the Section
   *
   * @param first  starting index
   * @param last   last index, inclusive
   * @param stride stride
   * @return this
   * @throws InvalidRangeException if last < first
   */
  public Section appendRange(int first, int last, int stride) throws InvalidRangeException {
    if (immutable) throw new IllegalStateException("Cant modify");
    list.add(new Range(first, last, stride));
    return this;
  }

  /**
   * Append a new Range(name,first,last,stride) to the Section
   *
   * @param name   name of Range
   * @param first  starting index
   * @param last   last index, inclusive
   * @param stride stride
   * @return this
   * @throws InvalidRangeException if last < first
   */
  public Section appendRange(String name, int first, int last, int stride) throws InvalidRangeException {
    if (immutable) throw new IllegalStateException("Cant modify");
    list.add(new Range(name, first, last, stride));
    return this;
  }

  /**
   * Insert a range at the specified index in the list.
   *
   * @param index insert here in the list, existing ranges at or after this index get shifted by one
   * @param r     insert this Range
   * @return this
   * @throws IndexOutOfBoundsException if bad index
   */
  public Section insertRange(int index, Range r) {
    if (immutable) throw new IllegalStateException("Cant modify");
    list.add(index, r);
    return this;
  }

  /**
   * Remove a range at the specified index in the list.
   *
   * @param index remove here in the list, existing ranges after this index get shifted by one
   * @return this
   * @throws IndexOutOfBoundsException if bad index
   */
  public Section removeRange(int index) {
    if (immutable) throw new IllegalStateException("Cant modify");
    list.remove(index);
    return this;
  }

  /**
   * Set the range at the specified index in the list, previous Range is discarded
   *
   * @param index list index, must be in interval [0,size).
   * @param r     insert this Range
   * @return this
   * @throws IndexOutOfBoundsException if bad index
   */
  public Section setRange(int index, Range r) {
    if (immutable) throw new IllegalStateException("Cant modify");
    list.set(index, r);
    return this;
  }

  /**
   * Replace a range at the specified index in the list.
   *
   * @param index replace here in the list.
   * @param r     use this Range
   * @return this
   * @throws IndexOutOfBoundsException if bad index
   */
  public Section replaceRange(int index, Range r) {
    if (immutable) throw new IllegalStateException("Cant modify");
    list.set(index, r);
    return this;
  }

  /**
   * Remove any ranges of length 1
   *
   * @return new Section if any Range had length 1, else this
   */
  public Section reduce() {
    if (immutable) throw new IllegalStateException("Cant modify");
    boolean needed = false;
    for (Range r : list) {
      if (r.length() == 1) needed = true;
    }
    if (!needed) return this;

    List<Range> newList = new ArrayList<>(list.size());
    for (Range r : list) {
      if (r.length() > 1)
        newList.add(r);
    }
    return new Section(newList);
  }

  public Section subSection(int from, int endExclusive) {
    return new Section(list.subList(from, endExclusive));
  }

  /**
   * If any of the ranges are null, which means "all", set the Range from the
   * corresponding length in shape[].
   *
   * @param shape default length for each Range; must have matching rank.
   * @throws InvalidRangeException if rank is wrong
   */
  public void setDefaults(int[] shape) throws InvalidRangeException {
    if (immutable) throw new IllegalStateException("Cant modify");
    if (shape.length != list.size())
      throw new InvalidRangeException(" shape[] must have same rank as Section");

    // check that any individual Range is null
    for (int i = 0; i < shape.length; i++) {
      Range r = list.get(i);
      if (r == null) {
        if (shape[i] > 0)
          list.set(i, new Range(shape[i]));
        else if (shape[i] == 0)
          list.set(i, Range.EMPTY);
        else {
          list.set(i, Range.VLEN);
          isvariablelength = true;
        }
      }
    }
  }

  /**
   * Makes the object immutable, so can be safely shared
   *
   * @return this Section
   */
  public Section makeImmutable() {
    if (immutable) return this;
    immutable = true;
    list = Collections.unmodifiableList(list);
    return this;
  }

  // end mutable methods

  public boolean isImmutable() {
    return immutable;
  }

  public boolean isVariableLength() {
    return isvariablelength;
  }

  public boolean isStrided() {
    for (Range r : list) {
      if (r != null && r.stride() != 1) return false;
    }
    return true;
  }

  /**
   * Get shape array using the Range.length() values.
   *
   * @return int[] shape
   */
  public int[] getShape() {
    int[] result = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      result[i] = list.get(i).length();
    }
    return result;
  }

  /**
   * Get origin array using the Range.first() values.
   *
   * @return int[] origin
   */
  public int[] getOrigin() {
    int[] result = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      result[i] = list.get(i).first();
    }
    return result;
  }

  /**
   * Get stride array using the Range.stride() values.
   *
   * @return int[] origin
   */
  public int[] getStride() {
    int[] result = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      result[i] = list.get(i).stride();
    }
    return result;
  }

  /**
   * Get origin of the ith Range
   *
   * @param i index of Range
   * @return origin of ith Range
   */
  public int getOrigin(int i) {
    return list.get(i).first();
  }

  /**
   * Get length of the ith Range
   *
   * @param i index of Range
   * @return length of ith Range
   */
  public int getShape(int i) {
    return list.get(i).length();
  }

  /**
   * Get stride of the ith Range
   *
   * @param i index of Range
   * @return stride of ith Range
   */
  public int getStride(int i) {
    return list.get(i).stride();
  }

  /**
   * Get rank - number of Ranges.
   *
   * @return rank
   */
  public int getRank() {
    return list.size();
  }

  /**
   * Get rank excluding vlens.
   *
   * @return rank
   */
  public int getVlenRank() {
    return list.size() - (isvariablelength ? 1 : 0);
  }

  /**
   * Compare this section with another upto the vlen in either
   */
  public boolean compatibleRank(Section other) {
    return (getRank() == other.getRank());
      /*if((isVariableLength() && other.isVariableLength())
          || (!isVariableLength() && !other.isVariableLength()))
          return getRank() == other.getRank();
      else if(isVariableLength() && !other.isVariableLength())
          return getVlenRank() == other.getRank();
      else // !isVariableLength() && other.isVariableLength()
        return getRank() == other.getVlenRank(); */
  }

  /**
   * Compute total number of elements represented by the section.
   * Any VLEN Ranges are effectively skipped.
   *
   * @return total number of elements
   */
  public long computeSize() {
    long product = 1;
    for (Range r : list) {
      if (r != Range.VLEN)
        product *= r.length();
    }
    return product;
  }

  /**
   * Get the list of Ranges.
   *
   * @return the List<Range>
   */
  public List<Range> getRanges() {
    return list;
  }

  /**
   * Get the ith Range
   *
   * @param i index into the list of Ranges
   * @return ith Range
   */
  public Range getRange(int i) {
    return list.get(i);
  }

  /**
   * Find a Range by its name.
   *
   * @param rangeName find this Range
   * @return named Range or null
   */
  public Range find(String rangeName) {
    for (Range r : list) {
      if (rangeName.equals(r.getName())) return r;
    }
    return null;
  }

  public Section addRangeNames(List<String> rangeNames) throws InvalidRangeException {
    if (rangeNames.size() != getRank())
      throw new InvalidRangeException("Invalid number of Range Names");

    int count = 0;
    Section result = new Section();
    for (Range r : getRanges()) {
      Range nr = new Range(rangeNames.get(count++), r);
      result.appendRange(nr);
    }
    return result;
  }

  /**
   * Check if this Section is legal for the given shape.
   * [Note: modified by dmh to address the case of unlimited
   * where the size is zero]
   *
   * @param shape range must fit within this shape, rank must match.
   * @return error message if illegal, null if all ok
   */
  public String checkInRange(int shape[]) {
    if (list.size() != shape.length)
      return "Number of ranges in section (" + list.size() + ") must be = " + shape.length;

    for (int i = 0; i < list.size(); i++) {
      Range r = list.get(i);
      if (r == null) continue;
      if (r == Range.VLEN) continue;
      if (r.last() >= shape[i])
        return "Illegal Range for dimension " + i + ": last requested " + r.last() + " > max " + (shape[i] - 1);
    }

    return null;
  }

  /**
   * Is this section equivilent to the given shape.
   * All non-null ranges must have origin 0 and length = shape[i]
   *
   * @param shape the given shape.
   * @return true if equivilent
   * @throws InvalidRangeException if setion rank doesnt match shape length
   */
  public boolean equivalent(int[] shape) throws InvalidRangeException {
    if (getRank() != shape.length)
      throw new InvalidRangeException("Invalid Section rank");

    for (int i = 0; i < list.size(); i++) {
      Range r = list.get(i);
      if (r == null) continue;
      if (r.first() != 0) return false;
      if (r.length() != shape[i]) return false;
    }
    return true;
  }

  /**
   * Sections with equals Ranges are equal.
   */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Section)) return false;
    Section os = (Section) o;

    if (getRank() != os.getRank()) return false;
    for (int i = 0; i < getRank(); i++) {
      Range r = getRange(i);
      Range or = os.getRange(i);
      if ((r == null) && (or != null)) return false;
      if ((or == null) && (r != null)) return false;
      if (r == null) continue;  // then or is also null
      if (!r.equals(or)) return false;
    }
    return true;
  }

  /**
   * Override Object.hashCode() to agree with equals.
   */
  public int hashCode() {
    int result = 17;
    for (Range r : list)
      if (r != null) result += 37 * result + r.hashCode();
    return result;
  }


  /**
   * Iterate over a section, returning the index in an equivalent 1D array of shape[], and optionally the corresponding index[n]
   * So this is a section in a (possibly) larger array described by shape[].
   * The index is in the "source" array.
   *
   * @param shape total array shape
   * @return iterator over this section
   */
  public Iterator getIterator(int[] shape) {
    return new Iterator(shape);
  }

  public class Iterator {
    private int[] odo = new int[getRank()];
    private int[] stride = new int[getRank()];
    private long done, total;

    Iterator(int[] shape) {
      int ss = 1;
      for (int i = getRank() - 1; i >= 0; i--) {
        odo[i] = getRange(i).first();
        stride[i] = ss;
        ss *= shape[i];
      }
      done = 0;
      total = Index.computeSize(getShape()); // total in the section
    }

    /**
     * Return true if there are more elements
     *
     * @return true if there are more elements
     */
    public boolean hasNext() {
      return done < total;
    }

    /**
     * Get the position in the equivalant 1D array of shape[]
     *
     * @param index if not null, return the current nD index
     * @return the current position in a 1D array
     */
    public int next(int[] index) {
      int next = currentElement();
      if (index != null)
        System.arraycopy(odo, 0, index, 0, odo.length);

      done++;
      if (done < total) incr(); // increment for next call
      return next;
    }

    /**
     * Get the current index in the result array, ie in this section
     *
     * @return result index
     */
    public int getResultIndex() {
      return (int) done - 1;
    }

    private void incr() {
      int digit = getRank() - 1;
      while (digit >= 0) {
        Range r = getRange(digit);
        odo[digit] += r.stride();
        if (odo[digit] <= r.last())
          break;  // normal exit
        odo[digit] = r.first(); // else, carry
        digit--;
        assert digit >= 0; // catch screw-ups
      }
    }

    private int currentElement() {
      int value = 0;
      for (int ii = 0; ii < getRank(); ii++)
        value += odo[ii] * stride[ii];
      return value;
    }
  }

}
