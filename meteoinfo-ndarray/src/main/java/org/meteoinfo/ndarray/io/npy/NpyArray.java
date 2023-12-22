package org.meteoinfo.ndarray.io.npy;

import java.nio.ByteBuffer;

public interface NpyArray<T> {

  T data();

  int[] shape();

  /**
   * Returns {@code true} when this array is stored in column-major order
   * (Fortran order).
   */
  boolean hasColumnOrder();

  /**
   * Returns {@code true} when this array is stored in row-major order (C order).
   */
  default boolean hasRowOrder() {
    return !hasColumnOrder();
  }

  NpyDataType dataType();

  /**
   * Get the element by index.
   *
   * @param i Index
   *
   * @return The element
   */
  Object getElement(int i);

  /**
   * Writes the element {@code i} of this array to the given buffer.
   *
   * @param i      the 0-based position of the element in this array that should
   *               be written to the buffer; must have a value between {@code 0}
   *               inclusively and {@link #size()} exclusively.
   * @param buffer the byte buffer to which the element should be written
   */
  void writeElementTo(int i, ByteBuffer buffer);

  /**
   * Returns the size of this array. That is the number of elements of this
   * array.
   *
   * @return the number of elements of this array
   */
  int size();

  default boolean isBigIntegerArray() {
    return false;
  }

  default boolean isBooleanArray() {
    return false;
  }

  NpyBooleanArray asBooleanArray();

  /**
   * Returns true if this array is an instance of {@link NpyByteArray}.
   */
  default boolean isByteArray() {
    return false;
  }

  /**
   * Converts this array into an instance of {@link NpyByteArray}. If this
   * array is already such an instance it is directly returned without copying.
   * Otherwise the values of this array are casted into a new
   * {@link NpyByteArray}. Note that such casting can result in data loss.
   *
   * @return this array as an instance of {@link NpyByteArray}
   */
  NpyByteArray asByteArray();

  default boolean isDoubleArray() {
    return false;
  }

  /**
   * Convert this array into a double array. If this array is already a double
   * array it is directly returned without making a copy of it.
   *
   * @return this array if it is a double array, otherwise a converted array
   */
  NpyDoubleArray asDoubleArray();

  default boolean isFloatArray() {
    return false;
  }

  NpyFloatArray asFloatArray();

  default boolean isIntArray() {
    return false;
  }

  NpyIntArray asIntArray();

  default boolean isLongArray() {
    return false;
  }

  NpyLongArray asLongArray();

  default boolean isShortArray() {
    return false;
  }

  NpyShortArray asShortArray();

  default boolean isCharArray() {
    return false;
  }

  default NpyCharArray asCharArray() {
    return asIntArray().asCharArray();
  }

}
