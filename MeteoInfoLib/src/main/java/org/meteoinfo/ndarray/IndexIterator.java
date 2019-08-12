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
 * Iteration through each element of an Array in "canonical order".
 * The user obtains this by calling getIndexIterator() on an Array.
 * <p/>
 * Canonical order for A[i][j][k] has k varying fastest, then j, then i.<br>
 * <p> Example: Replace array with its square:
 * <br>
 * <pre>
 * IndexIterator iter = A.getIndexIterator();
 * while (iter.hasNext()) {
 * double val = iter.getDoubleNext();
 * iter.setDoubleCurrent( val * val);
 * }
 * </pre>
 * <p/>
 * Note that canonical order may not be physical order.
 *
 * @author caron
 */

public interface IndexIterator {

  /**
   * Return true if there are more elements in the iteration.
   * @return true if there are more elements in the iteration.
   */
  public boolean hasNext();

  /**
   * Get next value as a double
   * @return next value as a double
   */
  public double getDoubleNext();

  /**
   * Set next value with a double
   * @param val the next value as a double
   */
  public void setDoubleNext(double val);

  /**
   * Get current value as a double
   * @return current value as a double
   */
  public double getDoubleCurrent();

  /**
   * Set current value with a double
   * @param val the current value as a double
   */
  public void setDoubleCurrent(double val);

  /**
   * Get next value as a float
   * @return next value as a float
   */
  public float getFloatNext();

  /**
   * Set next value with a float
   * @param val the  next value as a float
   */
  public void setFloatNext(float val);

  /**
   * Get current value as a float
   * @return current value as a float
   */
  public float getFloatCurrent();

  /**
   * Set current value with a float
   * @param val the  current value as a float
   */
  public void setFloatCurrent(float val);

  /**
   * Get next value as a long
   * @return next value as a long
   */
  public long getLongNext();

  /**
   * Set next value with a long
   * @param val the  next value as a long
   */
  public void setLongNext(long val);

  /**
   * Get current value as a long
   * @return current value as a long
   */
  public long getLongCurrent();

  /**
   * Set current value with a long
   * @param val the  current value as a long
   */
  public void setLongCurrent(long val);

  /**
   * Get next value as a int
   * @return next value as a int
   */
  public int getIntNext();

  /**
   * Set next value with a int
   * @param val the  next value as a int
   */
  public void setIntNext(int val);

  /**
   * Get current value as a int
   * @return current value as a int
   */
  public int getIntCurrent();

  /**
   * Set current value with a int
   * @param val the  current value as a int
   */
  public void setIntCurrent(int val);

  /**
   * Get next value as a short
   * @return next value as a short
   */
  public short getShortNext();

  /**
   * Set next value with a short
   * @param val the  next value as a short
   */
  public void setShortNext(short val);

  /**
   * Get current value as a short
   * @return current value as a short
   */
  public short getShortCurrent();

  /**
   * Set current value with a short
   * @param val the  current value as a short
   */
  public void setShortCurrent(short val);

  /**
   * Get next value as a byte
   * @return next value as a byte
   */
  public byte getByteNext();

  /**
   * Set next value with a byte
   * @param val the  next value as a byte
   */
  public void setByteNext(byte val);

  /**
   * Get current value as a byte
   * @return current value as a byte
   */
  public byte getByteCurrent();

  /**
   * Set current value with a byte
   * @param val the  current value as a byte
   */
  public void setByteCurrent(byte val);

  /**
   * Get next value as a char
   * @return next value as a char
   */
  public char getCharNext();

  /**
   * Set next value with a char
   * @param val the  next value as a char
   */
  public void setCharNext(char val);

  /**
   * Get current value as a char
   * @return current value as a char
   */
  public char getCharCurrent();

  /**
   * Set current value with a char
   * @param val the  current value as a char
   */
  public void setCharCurrent(char val);
  
  /**
   * Get next value as a string
   * @return next value as a string
   */
  public String getStringNext();

  /**
   * Set next value with a string
   * @param val the  next value as a string
   */
  public void setStringNext(String val);

  /**
   * Get current value as a string
   * @return current value as a string
   */
  public String getStringCurrent();

  /**
   * Set current value with a string
   * @param val the  current value as a string
   */
  public void setStringCurrent(String val);

  /**
   * Get next value as a boolean
   * @return next value as a boolean
   */
  public boolean getBooleanNext();

  /**
   * Set next value with a boolean
   * @param val the  next value as a boolean
   */
  public void setBooleanNext(boolean val);

  /**
   * Get current value as a boolean
   * @return current value as a boolean
   */
  public boolean getBooleanCurrent();

  /**
   * Set current value with a boolean
   * @param val the  current value as a boolean
   */
  public void setBooleanCurrent(boolean val);
  
  /**
   * Get next value as a complex
   * @return next value as a complex
   */
  public Complex getComplexNext();

  /**
   * Set next value with a complex
   * @param val the  next value as a complex
   */
  public void setComplexNext(Complex val);
  
  /**
   * Get current value as a complex
   * @return Current value as a complex
   */
  public Complex getComplexCurrent();
  
  /**
   * Set current value with a complex
   * @param val The current value as a complex
   */
  public void setComplexCurrent(Complex val);

  /**
   * Get next value as an Object
   * @return next value as an Object
   */
  public Object getObjectNext();

  /**
   * Set next value with a Object
   * @param val the  next value as a Object
   */
  public void setObjectNext(Object val);

  /**
   * Get current value as a Object
   * @return current value as a Object
   */
  public Object getObjectCurrent();

  /**
   * Set current value with a Object
   * @param val the current value as a Object
   */
  public void setObjectCurrent(Object val);

  /**
   * Get next value as an Object
   * @return next value as an Object
   */
  public Object next();

  /**
   * Get the current counter, use for debugging
   * @return the current counter, use for debugging
   */
  public int[] getCurrentCounter();
}
