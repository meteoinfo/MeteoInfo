
package org.meteoinfo.ndarray.io.npy;

import java.nio.ByteBuffer;

public final class NpyBooleanArray extends AbstractNpyArray<boolean[]> {

  public NpyBooleanArray(int[] shape, boolean[] data, boolean fortranOrder) {
    super(shape, data, fortranOrder);
  }

  public static NpyBooleanArray vectorOf(boolean[] data) {
    return new NpyBooleanArray(new int[] {data.length}, data, false);
  }

  /**
   * Wraps the given data in a 2-dimensional array in row-major order (C order).
   *
   * @param data the data of the array
   * @param rows the number of rows of the array
   * @param cols the number of columns of the array
   * @return a 2d array of the given shape
   */
  public static NpyBooleanArray rowOrderOf(boolean[] data, int rows, int cols) {
    return new NpyBooleanArray(new int[]{rows, cols}, data, false);
  }

  /**
   * Wraps the given data in a 2-dimensional array in column-major order (
   * Fortran order).
   *
   * @param data the data of the array
   * @param rows the number of rows of the array
   * @param cols the number of columns of the array
   * @return a 2d array of the given shape
   */
  public static NpyBooleanArray columnOrderOf(boolean[] data, int rows, int cols) {
    return new NpyBooleanArray(new int[]{rows, cols}, data, true);
  }

  @Override
  public NpyDataType dataType() {
    return NpyDataType.bool;
  }

  @Override
  public int size() {
    return data.length;
  }

  @Override
  public Object getElement(int i) {
    return data[i];
  }

  @Override
  public void writeElementTo(int i, ByteBuffer buffer) {
    byte b = data[i] ? (byte) 1 : (byte) 0;
    buffer.put(b);
  }

  @Override
  public boolean isBooleanArray() {
    return true;
  }

  @Override
  public NpyBooleanArray asBooleanArray() {
    return this;
  }

  @Override
  public NpyByteArray asByteArray() {
    byte[] bytes = new byte[data.length];
    for (int i = 0; i < data.length; i++) {
      if (data[i]) {
        bytes[i] = 1;
      }
    }
    return new NpyByteArray(copyShape(), bytes, fortranOrder);
  }

  @Override
  public NpyDoubleArray asDoubleArray() {
    double[] doubles = new double[data.length];
    for (int i = 0; i < data.length; i++) {
      if (data[i]) {
        doubles[i] = 1d;
      }
    }
    return new NpyDoubleArray(copyShape(), doubles, fortranOrder);
  }

  @Override
  public NpyFloatArray asFloatArray() {
    float[] floats = new float[data.length];
    for (int i = 0; i < data.length; i++) {
      if (data[i]) {
        floats[i] = 1f;
      }
    }
    return new NpyFloatArray(copyShape(), floats, fortranOrder);
  }

  @Override
  public NpyIntArray asIntArray() {
    int[] ints = new int[data.length];
    for (int i = 0; i < data.length; i++) {
      if (data[i]) {
        ints[i] = 1;
      }
    }
    return new NpyIntArray(copyShape(), ints, fortranOrder);
  }

  @Override
  public NpyLongArray asLongArray() {
    long[] longs = new long[data.length];
    for (int i = 0; i < data.length; i++) {
      if (data[i]) {
        longs[i] = 1L;
      }
    }
    return new NpyLongArray(copyShape(), longs, fortranOrder);
  }

  @Override
  public NpyShortArray asShortArray() {
    short[] shorts = new short[data.length];
    for (int i = 0; i < data.length; i++) {
      if (data[i]) {
        shorts[i] = 1;
      }
    }
    return new NpyShortArray(copyShape(), shorts, fortranOrder);
  }
}
  
