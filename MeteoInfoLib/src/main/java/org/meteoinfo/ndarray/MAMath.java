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

import org.meteoinfo.util.Misc;

/**
 * Element by element algebra on Arrays
 *
 * @author caron
 * @see Index
 */
public class MAMath {

  /**
   * Add elements of two arrays together, allocating the result array.
   * The result type and the operation type are taken from the type of a.
   *
   * @param a add values from here
   * @param b add values from here
   * @return result = a + b
   * @throws IllegalArgumentException      a and b are not conformable
   * @throws UnsupportedOperationException dont support this data type yet
   */
  public static Array add(Array a, Array b) throws IllegalArgumentException {

    Array result = Array.factory(a.getElementType(), a.getShape());

    if (a.getElementType() == double.class) {
      addDouble(result, a, b);
    } else
      throw new UnsupportedOperationException();

    return result;
  }

  /**
   * Add elements of two arrays together as doubles, place sum in the result array.
   * The values from the arrays a and b are converted to double (if needed),
   * and the sum is converted to the type of result (if needed).
   *
   * @param result result array
   * @param a operand
   * @param b operand
   * @throws IllegalArgumentException a,b,and result are not conformable
   */
  public static void addDouble(Array result, Array a, Array b)
      throws IllegalArgumentException {

    if (!conformable(result, a) || !conformable(a, b))
      throw new IllegalArgumentException();

    IndexIterator iterR = result.getIndexIterator();
    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterB = b.getIndexIterator();

    while (iterA.hasNext())
      iterR.setDoubleNext(iterA.getDoubleNext() + iterB.getDoubleNext());
  }

  /**
   * Check that two arrays are conformable.
   *
   * @param a operand
   * @param b operand
   * @return true if conformable
   */
  public static boolean conformable(Array a, Array b) {
    return conformable(a.getShape(), b.getShape());
  }

  /**
   * Check that two array shapes are conformable.
   * The shapes must match exactly, except that dimensions of length 1 are ignored.
   *
   * @param shapeA shape of array 1
   * @param shapeB shape of array 2
   * @return true if conformable
   */
  public static boolean conformable(int[] shapeA, int[] shapeB) {
    if (reducedRank(shapeA) != reducedRank(shapeB))
      return false;

    int rankB = shapeB.length;

    int dimB = 0;
    for (int aShapeA : shapeA) {
      //System.out.println(dimA + " "+ dimB);

      //skip length 1 dimensions
      if (aShapeA == 1)
        continue;
      while (dimB < rankB)
        if (shapeB[dimB] == 1) dimB++;
        else break;

      // test same shape (NB dimB cant be > rankB due to first test)
      if (aShapeA != shapeB[dimB])
        return false;
      dimB++;
    }

    return true;
  }

  /**
   * Convert unsigned data to signed data of a wider type.
   *
   * @param unsigned must be of type byte, short or int
   * @return converted data of type short, int, or long
   */
  public static Array convertUnsigned( Array unsigned) {
    if (unsigned.getElementType().equals(byte.class)) {
      Array result = Array.factory(DataType.SHORT, unsigned.getShape());
      IndexIterator ii = result.getIndexIterator();
      unsigned.resetLocalIterator();
      while (unsigned.hasNext())
        ii.setShortNext( DataType.unsignedByteToShort(unsigned.nextByte()));
      return result;

    } else if (unsigned.getElementType().equals(short.class)) {
      Array result = Array.factory(DataType.INT, unsigned.getShape());
      IndexIterator ii = result.getIndexIterator();
      unsigned.resetLocalIterator();
      while (unsigned.hasNext())
        ii.setIntNext( DataType.unsignedShortToInt(unsigned.nextShort()));
      return result;

    } else if (unsigned.getElementType().equals(int.class)) {
      Array result = Array.factory(DataType.LONG, unsigned.getShape());
      IndexIterator ii = result.getIndexIterator();
      unsigned.resetLocalIterator();
      while (unsigned.hasNext())
        ii.setLongNext( DataType.unsignedIntToLong(unsigned.nextInt()));
      return result;
    }

    throw new IllegalArgumentException("Cant convertUnsigned type= "+unsigned.getElementType());
  }

  /**
   * Convert original array to desired type
   *
   * @param org original array
   * @param wantType desired type
   * @return converted data of desired type, or original array if it is already
   */
  public static Array convert( Array org, DataType wantType) {
    if (org == null) return null;
    Class wantClass = wantType.getPrimitiveClassType();
    if (org.getElementType().equals(wantClass))
      return org;

    Array result = Array.factory(wantType, org.getShape());
    copy(wantType, org.getIndexIterator(), result.getIndexIterator());
    return result;
  }

  /**
   * Copy using iterators. Will copy until !from.hasNext().
   *
   * @param dataType use this operation type (eg DataType.DOUBLE uses getDoubleNext())
   * @param from     copy from here
   * @param to       copy to here
   * @throws IllegalArgumentException      a and b are not conformable
   * @throws UnsupportedOperationException dont support this data type
   */
  public static void copy(DataType dataType, IndexIterator from, IndexIterator to) throws IllegalArgumentException {
    if (dataType == DataType.DOUBLE) {
      while (from.hasNext())
        to.setDoubleNext(from.getDoubleNext());
    } else if (dataType == DataType.FLOAT) {
      while (from.hasNext())
        to.setFloatNext(from.getFloatNext());
    } else if (dataType == DataType.LONG) {
      while (from.hasNext())
        to.setLongNext(from.getLongNext());
    } else if ((dataType == DataType.INT) || (dataType == DataType.ENUM4)) {
      while (from.hasNext())
        to.setIntNext(from.getIntNext());
    } else if ((dataType == DataType.SHORT) || (dataType == DataType.ENUM2)) {
      while (from.hasNext())
        to.setShortNext(from.getShortNext());
    } else if (dataType == DataType.CHAR) {
      while (from.hasNext())
        to.setCharNext(from.getCharNext());
    } else if ((dataType == DataType.BYTE) || (dataType == DataType.ENUM1)) {
      while (from.hasNext())
        to.setByteNext(from.getByteNext());
    } else if (dataType == DataType.BOOLEAN) {
      while (from.hasNext())
        to.setBooleanNext(from.getBooleanNext());
    } else {
      while (from.hasNext())
        to.setObjectNext(from.getObjectNext());
    }
  }

  /**
   * Copy array a to array result, the result array will be in canonical order
   * The operation type is taken from the type of a.
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException      a and b are not conformable
   * @throws UnsupportedOperationException dont support this data type yet
   */
  public static void copy(Array result, Array a) throws IllegalArgumentException {
    Class classType = a.getElementType();
    if (classType == double.class) {
      copyDouble(result, a);
    } else if (classType == float.class) {
      copyFloat(result, a);
    } else if (classType == long.class) {
      copyLong(result, a);
    } else if (classType == int.class) {
      copyInt(result, a);
    } else if (classType == short.class) {
      copyShort(result, a);
    } else if (classType == char.class) {
      copyChar(result, a);
    } else if (classType == byte.class) {
      copyByte(result, a);
    } else if (classType == boolean.class) {
      copyBoolean(result, a);
    } else
      copyObject(result, a);
  }

  /**
   * copy array a to array result as doubles
   * The values from the arrays a are converted to double (if needed),
   * and then converted to the type of result (if needed).
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyDouble(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setDoubleNext(iterA.getDoubleNext());
  }

  /**
   * copy array a to array result as floats
   * The values from the arrays a are converted to float (if needed),
   * and then converted to the type of result (if needed).
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyFloat(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setFloatNext(iterA.getFloatNext());
  }

  /**
   * copy array a to array result as longs
   * The values from the array a are converted to long (if needed),
   * and then converted to the type of result (if needed).
   * @param result copy to here
   * @param a copy from here
   *
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyLong(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setLongNext(iterA.getLongNext());
  }

  /**
   * copy array a to array result as integers
   * The values from the arrays a are converted to integer (if needed),
   * and then converted to the type of result (if needed).
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyInt(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setIntNext(iterA.getIntNext());
  }

  /**
   * copy array a to array result as shorts
   * The values from the array a are converted to short (if needed),
   * and then converted to the type of result (if needed).
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyShort(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setShortNext(iterA.getShortNext());
  }

  /**
   * copy array a to array result as char
   * The values from the array a are converted to char (if needed),
   * and then converted to the type of result (if needed).
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyChar(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setCharNext(iterA.getCharNext());
  }


  /**
   * copy array a to array result as bytes
   * The values from the array a are converted to byte (if needed),
   * and then converted to the type of result (if needed).
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyByte(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setByteNext(iterA.getByteNext());
  }

  /**
   * copy array a to array result as bytes
   * The array a and result must be type boolean
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyBoolean(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setBooleanNext(iterA.getBooleanNext());
  }

  /**
   * copy array a to array result as an Object
   * The array a and result must be type object
   *
   * @param result copy to here
   * @param a copy from here
   *
   * @throws IllegalArgumentException a and result are not conformable
   */
  public static void copyObject(Array result, Array a) throws IllegalArgumentException {
    if (!conformable(a, result))
      throw new IllegalArgumentException("copy arrays are not conformable");

    IndexIterator iterA = a.getIndexIterator();
    IndexIterator iterR = result.getIndexIterator();
    while (iterA.hasNext())
      iterR.setObjectNext(iterA.getObjectNext());
  }

  /**
   * Calculate the reduced rank of this shape, by subtracting dimensions with length 1
   * @param shape shape of the array
   * @return rank without dimensions of length 1
   */
  public static int reducedRank(int[] shape) {
    int rank = 0;
    for (int aShape : shape) {
      if (aShape > 1)
        rank++;
    }
    return rank;
  }

  public static double getMinimum(Array a) {
    IndexIterator iter = a.getIndexIterator();
    double min = Double.MAX_VALUE;
    while (iter.hasNext()) {
      double val = iter.getDoubleNext();
      if (Double.isNaN(val)) continue;
      if (val < min)
        min = val;
    }
    return min;
  }

  public static double getMaximum(Array a) {
    IndexIterator iter = a.getIndexIterator();
    double max = -Double.MAX_VALUE;
    while (iter.hasNext()) {
      double val = iter.getDoubleNext();
      if (Double.isNaN(val)) continue;
      if (val > max)
        max = val;
    }
    return max;
  }

  /**
   * Find min and max value in this array, getting values as doubles. Skip Double.NaN.
   *
   * @param a the array.
   * @return MinMax
   */
  public static MAMath.MinMax getMinMax(Array a) {
    IndexIterator iter = a.getIndexIterator();
    double max = -Double.MAX_VALUE;
    double min = Double.MAX_VALUE;
    while (iter.hasNext()) {
      double val = iter.getDoubleNext();
      if (Double.isNaN(val)) continue;
      if (val > max)
        max = val;
      if (val < min)
        min = val;
    }
    return new MinMax(min, max);
  }

  public static MAMath.MinMax getMinMaxSkipMissingData(Array a, IsMissingEvaluator eval) {
    if (!eval.hasMissing())
      return MAMath.getMinMax(a);

    IndexIterator iter = a.getIndexIterator();
    double max = -Double.MAX_VALUE;
    double min = Double.MAX_VALUE;
    while (iter.hasNext()) {
      double val = iter.getDoubleNext();
      if (eval.isMissing(val))
        continue;
      if (val > max)
        max = val;
      if (val < min)
        min = val;
    }
    return new MAMath.MinMax(min, max);
  }


  public static double getMinimumSkipMissingData(Array a, double missingValue) {
    IndexIterator iter = a.getIndexIterator();
    double min = Double.MAX_VALUE;
    while (iter.hasNext()) {
      double val = iter.getDoubleNext();
      if ((val != missingValue) && (val < min))
        min = val;
    }
    return min;
  }

  public static double getMaximumSkipMissingData(Array a, double missingValue) {
    IndexIterator iter = a.getIndexIterator();
    double max = -Double.MAX_VALUE;
    while (iter.hasNext()) {
      double val = iter.getDoubleNext();
      if ((val != missingValue) && (val > max))
        max = val;
    }
    return max;
  }

  public static MAMath.MinMax getMinMaxSkipMissingData(Array a, double missingValue) {
    IndexIterator iter = a.getIndexIterator();
    double max = -Double.MAX_VALUE;
    double min = Double.MAX_VALUE;
    while (iter.hasNext()) {
      double val = iter.getDoubleNext();
      if (val == missingValue)
        continue;
      if (val > max)
        max = val;
      if (val < min)
        min = val;
    }
    return new MinMax(min, max);
  }


  /**
   * Set all the elements of this array to the given double value.
   * The value is converted to the element type of the array, if needed.
   *
   * @param result change this Array
   * @param val set all elements to this value
   */
  public static void setDouble(Array result, double val) {
    IndexIterator iter = result.getIndexIterator();
    while (iter.hasNext()) {
      iter.setDoubleNext(val);
    }
  }

  /**
   * sum all of the elements of array a as doubles.
   * The values from the array a are converted to double (if needed).
   * @param a read values from this Array
   * @return sum of elements
   */
  public static double sumDouble(Array a) {
    double sum = 0;
    IndexIterator iterA = a.getIndexIterator();
    while (iterA.hasNext()) {
      sum += iterA.getDoubleNext();
    }
    return sum;
  }

  /**
   * sum all of the elements of array a as doubles.
   * The values from the array a are converted to double (if needed).
   * @param a read values from this Array
   * @param missingValue skip values equal to this, or which are NaNs
   * @return sum of elements
   */
  public static double sumDoubleSkipMissingData(Array a, double missingValue) {
    double sum = 0;
    IndexIterator iterA = a.getIndexIterator();
    while (iterA.hasNext()) {
      double val = iterA.getDoubleNext();
      if ((val == missingValue) || Double.isNaN(val))
        continue;
      sum += val;
    }
    return sum;
  }

  /**
   * Holds a minimum and maximum value.
   */
  public static class MinMax {
    public double min, max;

    public MinMax(double min, double max) {
      this.min = min;
      this.max = max;
    }

    @Override
    public String toString() {
      return "MinMax{" +
              "min=" + min +
              ", max=" + max +
              '}';
    }
  }

  /**
   * Calculate the scale/offset for an array of numbers.
   * <pre>
   * If signed:
   *   then
   *     max value unpacked = 2^(n-1) - 1 packed
   *     min value unpacked = -(2^(n-1) - 1) packed
   *   note that -2^(n-1) is unused, and a good place to map missing values
   *   by solving 2 eq in 2 unknowns, we get:
   *     scale = (max - min) / (2^n - 2)
   *     offset = (max + min) / 2
   * If unsigned then
   *     max value unpacked = 2^n - 1 packed
   *     min value unpacked = 0 packed
   *   and:
   *     scale = (max - min) / (2^n - 1)
   *     offset = min
   *   One could modify this to allow a holder for missing values.
   * </pre>
   * @param a array to convert (not changed)
   * @param missingValue skip these
   * @param nbits map into this many bits
   * @param isUnsigned use signed or unsigned packed values
   * @return ScaleOffset, calculated as above.
   */
  public static MAMath.ScaleOffset calcScaleOffsetSkipMissingData(Array a, double missingValue, int nbits, boolean isUnsigned) {
    MAMath.MinMax minmax = getMinMaxSkipMissingData(a, missingValue);

    if (isUnsigned) {
      long size = (1L << nbits) - 1;
      double offset = minmax.min;
      double scale =(minmax.max - minmax.min) / size;
      return new ScaleOffset(scale, offset);

    } else {
      long size = (1L << nbits) - 2;
      double offset = (minmax.max + minmax.min) / 2;
      double scale =(minmax.max - minmax.min) / size;
      return new ScaleOffset(scale, offset);
    }
  }

  public static Array convert2packed(Array unpacked, double missingValue, int nbits, boolean isUnsigned, DataType packedType) {
    MAMath.ScaleOffset scaleOffset = calcScaleOffsetSkipMissingData(unpacked, missingValue, nbits, isUnsigned);
    Array result = Array.factory(packedType, unpacked.getShape());
    IndexIterator riter = result.getIndexIterator();
    while (unpacked.hasNext()) {
      double uv = unpacked.nextDouble();
      double pv = (uv - scaleOffset.offset) / scaleOffset.scale;
      riter.setDoubleNext( pv);
    }
    return result;
  }

  public static Array convert2Unpacked(Array packed, ScaleOffset scaleOffset) {
    //boolean isUnsigned = packed.isUnsigned();
    Array result = Array.factory(DataType.DOUBLE, packed.getShape());
    IndexIterator riter = result.getIndexIterator();
    while (packed.hasNext())  {
      riter.setDoubleNext( packed.nextDouble() * scaleOffset.scale + scaleOffset.offset);
    }
    return result;
  }

  /**
   * Holds a scale and offset.
   */
  public static class ScaleOffset {
    public double scale, offset;

    public ScaleOffset(double scale, double offset) {
      this.scale = scale;
      this.offset = offset;
    }
  }

  /**
   * Returns true if the specified arrays have the same size, signedness, and <b>approximately</b> equal corresponding
   * elements. {@code float} elements must be within {@code 1.0e-5} of each other and {@code double} elements must be
   * within {@code 1.0e-8} of each other.
   * <p>
   *
   * @param data1  one array to be tested for equality.
   * @param data2  the other array to be tested for equality.
   * @return true if the specified arrays have the same size, signedness, and approximately equal corresponding elems.
   */
  public static boolean fuzzyEquals(Array data1, Array data2) {
    if (data1 == data2) {  // Covers case when both are null.
      return true;
    } else if (data1 == null || data2 == null) {
      return false;
    }

    if (data1.getSize() != data2.getSize()) return false;
    if (data1.isUnsigned() != data2.isUnsigned()) return false;
    DataType dt = data1.getDataType();

    IndexIterator iter1 = data1.getIndexIterator();
    IndexIterator iter2 = data2.getIndexIterator();

    if (dt == DataType.DOUBLE) {
      while (iter1.hasNext() && iter2.hasNext()) {
        double v1 = iter1.getDoubleNext();
        double v2 = iter2.getDoubleNext();
        if (!Double.isNaN(v1) || !Double.isNaN(v2))
          if (!Misc.closeEnough(v1, v2, 1.0e-8))
            return false;
      }
    } else if (dt == DataType.FLOAT) {
      while (iter1.hasNext() && iter2.hasNext()) {
        float v1 = iter1.getFloatNext();
        float v2 = iter2.getFloatNext();
        if (!Float.isNaN(v1) || !Float.isNaN(v2))
          if (!Misc.closeEnough(v1, v2, 1.0e-5))
            return false;
      }
    } else if (dt.getPrimitiveClassType() == int.class) {
      while (iter1.hasNext() && iter2.hasNext()) {
        int v1 = iter1.getIntNext();
        int v2 = iter2.getIntNext();
        if (v1 != v2) return false;
      }
    } else if (dt.getPrimitiveClassType() == byte.class) {
      while (iter1.hasNext() && iter2.hasNext()) {
        short v1 = iter1.getShortNext();
        short v2 = iter2.getShortNext();
        if (v1 != v2) return false;
      }
    } else if (dt.getPrimitiveClassType() == short.class) {
      while (iter1.hasNext() && iter2.hasNext()) {
        byte v1 = iter1.getByteNext();
        byte v2 = iter2.getByteNext();
        if (v1 != v2) return false;
      }
    } else if (dt.getPrimitiveClassType() == long.class) {
      while (iter1.hasNext() && iter2.hasNext()) {
        long v1 = iter1.getLongNext();
        long v2 = iter2.getLongNext();
        if (v1 != v2) return false;
      }
    }

    return true;
  }

  public static boolean isEqual(Array data1, Array data2) {
    if (data1.getSize() != data2.getSize()) return false;
    DataType dt = DataType.getType(data1.getElementType());

    IndexIterator iter1 = data1.getIndexIterator();
    IndexIterator iter2 = data2.getIndexIterator();

    if (dt == DataType.DOUBLE) {
      while (iter1.hasNext() && iter2.hasNext()) {
        double v1 = iter1.getDoubleNext();
        double v2 = iter2.getDoubleNext();
        if (!Double.isNaN(v1) || !Double.isNaN(v2))
          if (!Misc.closeEnough(v1, v2, 1.0e-8))
            return false;
      }
    } else if (dt == DataType.FLOAT) {
      while (iter1.hasNext() && iter2.hasNext()) {
        float v1 = iter1.getFloatNext();
        float v2 = iter2.getFloatNext();
        if (!Float.isNaN(v1) || !Float.isNaN(v2))
          if (!Misc.closeEnough(v1, v2, 1.0e-5))
            return false;
      }
    } else if (dt == DataType.INT) {
      while (iter1.hasNext() && iter2.hasNext()) {
        int v1 = iter1.getIntNext();
        int v2 = iter2.getIntNext();
        if (v1 != v2) return false;
      }
    } else if (dt == DataType.SHORT) {
      while (iter1.hasNext() && iter2.hasNext()) {
        short v1 = iter1.getShortNext();
        short v2 = iter2.getShortNext();
        if (v1 != v2) return false;
      }
    } else if (dt == DataType.BYTE) {
      while (iter1.hasNext() && iter2.hasNext()) {
        byte v1 = iter1.getByteNext();
        byte v2 = iter2.getByteNext();
        if (v1 != v2) return false;
      }
    } else if (dt == DataType.LONG) {
      while (iter1.hasNext() && iter2.hasNext()) {
        long v1 = iter1.getLongNext();
        long v2 = iter2.getLongNext();
        if (v1 != v2) return false;
      }
    }

    return true;
  }
}
