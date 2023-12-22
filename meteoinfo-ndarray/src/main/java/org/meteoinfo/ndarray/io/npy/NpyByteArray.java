package org.meteoinfo.ndarray.io.npy;

import java.nio.ByteBuffer;

public final class NpyByteArray extends AbstractNpyArray<byte[]> {

  public NpyByteArray(int[] shape, byte[] data, boolean fortranOrder) {
    super(shape, data, fortranOrder);
  }

  public static NpyByteArray vectorOf(byte[] data) {
    return new NpyByteArray(new int[] {data.length}, data, false);
  }

  /**
   * Wraps the given data in a 2-dimensional array in row-major order (C order).
   *
   * @param data the data of the array
   * @param rows the number of rows of the array
   * @param cols the number of columns of the array
   * @return a 2d array of the given shape
   */
  public static NpyByteArray rowOrderOf(byte[] data, int rows, int cols) {
    return new NpyByteArray(new int[]{rows, cols}, data, false);
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
  public static NpyByteArray columnOrderOf(byte[] data, int rows, int cols) {
    return new NpyByteArray(new int[]{rows, cols}, data, true);
  }

  @Override
  public NpyDataType dataType() {
    return NpyDataType.i1;
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
    buffer.put(data[i]);
  }

  @Override
  public boolean isByteArray() {
    return true;
  }

  @Override
  public NpyByteArray asByteArray() {
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

  @Override
  public NpyShortArray asShortArray() {
    short[] shorts = new short[data.length];
    for (int i = 0; i < data.length; i++) {
      shorts[i] = data[i];
    }
    return new NpyShortArray(copyShape(), shorts, fortranOrder);
  }
}
  
