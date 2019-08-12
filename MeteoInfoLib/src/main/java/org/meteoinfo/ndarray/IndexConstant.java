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

import java.util.List;

/**
 * An Index into an Array that always returns 0. It can have any shape, so it allows you to create a
 * constant Array of any shape.
 *  
 * @author caron
 */
 // LOOK : need to override section, etc !!
public class IndexConstant extends Index {

  protected IndexConstant(int rank) {
    super( rank);
  }

  protected IndexConstant( int[] shape) {
    super( shape);
  }

  protected IndexConstant( int[] shape, int[] stride) {
    super( shape, stride);
  }

  /** always return 0 */
  public int currentElement() {
    return 0;
  }

  @Override
  Index flip(int index) {
    return this;
  }

  @Override
  Index sectionNoReduce(List<Range> ranges) throws InvalidRangeException {
    Section curr = new Section(shape);
    Section want = curr.compose(new Section(ranges));
    return new IndexConstant( want.getShape());
  }

  @Override
  Index section(List<Range> ranges) throws InvalidRangeException {
    Section curr = new Section(shape);
    Section want = curr.compose(new Section(ranges)).reduce();
    return new IndexConstant( want.getShape());
  }

  @Override
  Index reduce() {
    Section curr = new Section(shape);
    Section want = curr.reduce();
    return new IndexConstant( want.getShape());
  }

  @Override
  Index reduce(int dim) {
    if ((dim < 0) || (dim >= rank))
      throw new IllegalArgumentException("illegal reduce dim " + dim);
    if (shape[dim] != 1)
      throw new IllegalArgumentException("illegal reduce dim " + dim + " : length != 1");

    Section curr = new Section(shape);
    Section want = curr.removeRange(dim);
    return new IndexConstant( want.getShape());
  }

  @Override
  Index transpose(int index1, int index2) {
    return this;
  }

  @Override          
  Index permute(int[] dims) {
    return this;
  }

  // This method is only used in Array.get1DJavaArray(Class), where it's used to decide whether a copy is necessary.
  // In the case of a constant array, we DO need a copy because the size of our backing storage (length=1) is likely
  // not the size of the Array.
  // Fixes bug in https://github.com/Unidata/thredds/issues/581
  @Override
  public boolean isFastIterator() {
    return false;
  }

  ///////////////////////

  IndexIterator getIndexIterator(Array maa) {
    return new IteratorConstant(size, maa);
  }

  private static class IteratorConstant implements IndexIterator {

    private int currElement = -1;
    private final Array maa;
    private long size;

    IteratorConstant(long size, Array maa) {
      this.size = size;
      this.maa = maa;
    }

    public boolean hasNext() {
      return currElement < size-1;
    }

    public boolean hasMore(int howMany) {
      return currElement < size-howMany;
    }

    private Index counter = null; // usually not used
    public String toString() {
      if (counter == null)
        counter = new Index(maa.getShape());
      counter.setCurrentCounter( currElement);
      return counter.toString();
    }
    public int[] getCurrentCounter() {
      if (counter == null)
        counter = new Index(maa.getShape());
      counter.setCurrentCounter( currElement);
      return counter.current;
    }
    
    public double getDoubleCurrent() { return maa.getDouble(0); }
    public double getDoubleNext() { currElement++; return maa.getDouble(0); }
    public void setDoubleCurrent(double val) { maa.setDouble(0, val); }
    public void setDoubleNext(double val) { currElement++; maa.setDouble(0, val); }

    public float getFloatCurrent() { return maa.getFloat(currElement); }
    public float getFloatNext() { currElement++; return maa.getFloat(0); }
    public void setFloatCurrent(float val) { maa.setFloat(currElement, val); }
    public void setFloatNext(float val) { currElement++; maa.setFloat(0, val); }

    public long getLongCurrent() { return maa.getLong(currElement); }
    public long getLongNext() { currElement++; return maa.getLong(0); }
    public void setLongCurrent(long val) { maa.setLong(currElement, val); }
    public void setLongNext(long val) { currElement++; maa.setLong(0, val); }

    public int getIntCurrent() { return maa.getInt(currElement); }
    public int getIntNext() { currElement++; return maa.getInt(0); }
    public void setIntCurrent(int val) { maa.setInt(currElement, val); }
    public void setIntNext(int val) { currElement++; maa.setInt(0, val); }

    public short getShortCurrent() { return maa.getShort(currElement); }
    public short getShortNext() { currElement++; return maa.getShort(0); }
    public void setShortCurrent(short val) { maa.setShort(currElement, val); }
    public void setShortNext(short val) { currElement++; maa.setShort(0, val); }

    public byte getByteCurrent() { return maa.getByte(currElement); }
    public byte getByteNext() { currElement++; return maa.getByte(0); }
    public void setByteCurrent(byte val) { maa.setByte(currElement, val); }
    public void setByteNext(byte val) { currElement++; maa.setByte(0, val); }

    public char getCharCurrent() { return maa.getChar(currElement); }
    public char getCharNext() { currElement++; return maa.getChar(0); }
    public void setCharCurrent(char val) { maa.setChar(currElement, val); }
    public void setCharNext(char val) { currElement++; maa.setChar(0, val); }

    public boolean getBooleanCurrent() { return maa.getBoolean(currElement); }
    public boolean getBooleanNext() { currElement++; return maa.getBoolean(0); }
    public void setBooleanCurrent(boolean val) { maa.setBoolean(currElement, val); }
    public void setBooleanNext(boolean val) {currElement++;  maa.setBoolean(0, val); }
    
    public String getStringCurrent() { return maa.getString(currElement); }
    public String getStringNext() { currElement++; return maa.getString(0); }
    public void setStringCurrent(String val) { maa.setString(currElement, val); }
    public void setStringNext(String val) {currElement++;  maa.setString(0, val); }
    
    public Complex getComplexCurrent() { return maa.getComplex(currElement); }
    public Complex getComplexNext() { currElement++; return maa.getComplex(0); }
    public void setComplexCurrent(Complex val) { maa.setComplex(currElement, val); }
    public void setComplexNext(Complex val) {currElement++;  maa.setComplex(0, val); }

    public Object getObjectCurrent() { return maa.getObject(currElement); }
    public Object getObjectNext() { currElement++; return maa.getObject(0); }
    public void setObjectCurrent(Object val) { maa.setObject(currElement, val); }
    public void setObjectNext(Object val) { currElement++; maa.setObject(0, val); }

    public Object next() { currElement++; return maa.getObject(0); }
  }
}
