package org.meteoinfo.ndarray.io.npy;

import java.nio.ByteBuffer;

public final class NpyShortArray extends AbstractNpyArray<short[]> {

  public NpyShortArray(int[] shape, short[] data, boolean fortranOrder) {
    super(shape, data, fortranOrder);
  }

  public static NpyShortArray vectorOf(short[] data) {
    return new NpyShortArray(new int[] {data.length}, data, false);
  }

  /**
   * Wraps the given data in a 2-dimensional array in row-major order (C order).
   *
   * @param data the data of the array
   * @param rows the number of rows of the array
   * @param cols the number of columns of the array
   * @return a 2d array of the given shape
   */
  public static NpyShortArray rowOrderOf(short[] data, int rows, int cols) {
    return new NpyShortArray(new int[]{rows, cols}, data, false);
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
  public static NpyShortArray columnOrderOf(short[] data, int rows, int cols) {
    return new NpyShortArray(new int[]{rows, cols}, data, true);
  }

  @Override
  public NpyDataType dataType() {
    return NpyDataType.i2;
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
    buffer.putShort(data[i]);
  }

  @Override
  public boolean isShortArray() {
    return true;
  }

  @Override
  public NpyShortArray asShortArray() {
    return this;
  }

  @Override
  public NpyBooleanArray asBooleanArray() {
    boolean[] booleans = new boolean[data.length];
    for (int i = 0; i < data.length; i++) {
      booleans[i] = i != 0;
    }
    return new NpyBooleanArray(copyShape(), booleans, fortranOrder);
  }

  @Override
  public NpyByteArray asByteArray() {
    byte[] bytes = new byte[data.length];
    for (int i = 0; i < data.length; i++) {
      bytes[i] = (byte) data[i];
    }
    return new NpyByteArray(copyShape(), bytes, fortranOrder);
  }

  @Override
  public NpyDoubleArray asDoubleArray() {
    double[] doubles = new double[data.length];
    for (int i = 0; i < data.length; i++) {
      doubles[i] = data[i];
    }
    return new NpyDoubleArray(copyShape(), doubles, fortranOrder);
  }

  @Override
  public NpyFloatArray asFloatArray() {
    float[] floats = new float[data.length];
    for (int i = 0; i < data.length; i++) {
      floats[i] = data[i];
    }
    return new NpyFloatArray(copyShape(), floats, fortranOrder);
  }

  @Override
  public NpyIntArray asIntArray() {
    int[] ints = new int[data.length];
    for (int i = 0; i < data.length; i++) {
      ints[i] = data[i];
    }
    return new NpyIntArray(copyShape(), ints, fortranOrder);
  }

  @Override
  public NpyLongArray asLongArray() {
    long[] longs = new long[data.length];
    for (int i = 0; i < data.length; i++) {
      longs[i] = data[i];
    }
    return new NpyLongArray(copyShape(), longs, fortranOrder);
  }
}
  
