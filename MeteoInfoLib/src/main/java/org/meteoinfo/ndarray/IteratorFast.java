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

/**
 * A "fast" iterator that can be used when the data is in canonical order.
 *
 * @author caron
 */
  /* the idea is IteratorFast can do the iteration without an Index */
  public class IteratorFast implements IndexIterator {

    private int currElement = -1;
    private final Array maa;
    private long size;

    IteratorFast(long size, Array maa) {
      this.size = size;
      this.maa = maa;
      //System.out.println("IteratorFast");
    }

    public boolean hasNext() {
      return currElement < size-1;
    }

    public boolean hasMore(int howMany) {
      return currElement < size-howMany;
    }

    private Index counter = null; // usually not used
    public String toString() {
      if (counter == null || counter.toString().equals(""))   // not sure about the second condition
        counter = Index.factory(maa.getShape());
      counter.setCurrentCounter( currElement);
      return counter.toString();
    }

    public int[] getCurrentCounter() {
      if (counter == null)   // or counter == "" ?
        counter = Index.factory(maa.getShape());
      counter.setCurrentCounter( currElement);
      return counter.current;
    }

    public double getDoubleCurrent() { return maa.getDouble(currElement); }
    public double getDoubleNext() { return maa.getDouble(++currElement); }
    public void setDoubleCurrent(double val) { maa.setDouble(currElement, val); }
    public void setDoubleNext(double val) { maa.setDouble(++currElement, val); }

    public float getFloatCurrent() { return maa.getFloat(currElement); }
    public float getFloatNext() { return maa.getFloat(++currElement); }
    public void setFloatCurrent(float val) { maa.setFloat(currElement, val); }
    public void setFloatNext(float val) { maa.setFloat(++currElement, val); }

    public long getLongCurrent() { return maa.getLong(currElement); }
    public long getLongNext() { return maa.getLong(++currElement); }
    public void setLongCurrent(long val) { maa.setLong(currElement, val); }
    public void setLongNext(long val) { maa.setLong(++currElement, val); }

    public int getIntCurrent() { return maa.getInt(currElement); }
    public int getIntNext() { return maa.getInt(++currElement); }
    public void setIntCurrent(int val) { maa.setInt(currElement, val); }
    public void setIntNext(int val) { maa.setInt(++currElement, val); }

    public short getShortCurrent() { return maa.getShort(currElement); }
    public short getShortNext() { return maa.getShort(++currElement); }
    public void setShortCurrent(short val) { maa.setShort(currElement, val); }
    public void setShortNext(short val) { maa.setShort(++currElement, val); }

    public byte getByteCurrent() { return maa.getByte(currElement); }
    public byte getByteNext() { return maa.getByte(++currElement); }
    public void setByteCurrent(byte val) { maa.setByte(currElement, val); }
    public void setByteNext(byte val) { maa.setByte(++currElement, val); }

    public char getCharCurrent() { return maa.getChar(currElement); }
    public char getCharNext() { return maa.getChar(++currElement); }
    public void setCharCurrent(char val) { maa.setChar(currElement, val); }
    public void setCharNext(char val) { maa.setChar(++currElement, val); }

    public boolean getBooleanCurrent() { return maa.getBoolean(currElement); }
    public boolean getBooleanNext() { return maa.getBoolean(++currElement); }
    public void setBooleanCurrent(boolean val) { maa.setBoolean(currElement, val); }
    public void setBooleanNext(boolean val) { maa.setBoolean(++currElement, val); }
    
    public String getStringCurrent() { return maa.getString(currElement); }
    public String getStringNext() { return maa.getString(++currElement); }
    public void setStringCurrent(String val) { maa.setString(currElement, val); }
    public void setStringNext(String val) { maa.setString(++currElement, val); }
    
    public Complex getComplexCurrent() { return maa.getComplex(currElement); }
    public Complex getComplexNext() { return maa.getComplex(++currElement); }
    public void setComplexCurrent(Complex val) { maa.setComplex(currElement, val); }
    public void setComplexNext(Complex val) { maa.setComplex(++currElement, val); }

    public Object getObjectCurrent() { return maa.getObject(currElement); }
    public Object getObjectNext() { return maa.getObject(++currElement); }
    public void setObjectCurrent(Object val) { maa.setObject(currElement, val); }
    public void setObjectNext(Object val) { maa.setObject(++currElement, val); }

    public Object next() { return maa.getObject(++currElement); }
  }