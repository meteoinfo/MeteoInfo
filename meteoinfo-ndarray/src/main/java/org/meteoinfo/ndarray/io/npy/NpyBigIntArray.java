package org.meteoinfo.ndarray.io.npy;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public final class NpyBigIntArray extends AbstractNpyArray<BigInteger[]> {

  public NpyBigIntArray(int[] shape, BigInteger[] data, boolean fortranOrder) {
    super(shape, data, fortranOrder);
  }

  public static NpyBigIntArray vectorOf(BigInteger[] data) {
    return new NpyBigIntArray(new int[] {data.length}, data, false);
  }

  /**
   * Wraps the given data in a 2-dimensional array in row-major order (C order).
   *
   * @param data the data of the array
   * @param rows the number of rows of the array
   * @param cols the number of columns of the array
   * @return a 2d array of the given shape
   */
  public static NpyBigIntArray rowOrderOf(BigInteger[] data, int rows, int cols) {
    return new NpyBigIntArray(new int[]{rows, cols}, data, false);
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
  public static NpyBigIntArray columnOrderOf(BigInteger[] data, int rows, int cols) {
    return new NpyBigIntArray(new int[]{rows, cols}, data, true);
  }

  @Override
  public NpyDataType dataType() {
    return NpyDataType.u8;
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
    BigInteger value = data[i];
    if (value == null) {
      buffer.putLong(0);
    } else {
      buffer.putLong(value.longValueExact());
    }
  }

  @Override
  public boolean isBigIntegerArray() {
    return true;
  }

  @Override
  public NpyBooleanArray asBooleanArray() {
    boolean[] booleans = new boolean[data.length];
    for (int i = 0; i < data.length; i++) {
      BigInteger val = data[i];
      if (val != null) {
        booleans[i] = val.longValueExact() != 0;
      }
    }
    return new NpyBooleanArray(copyShape(), booleans, fortranOrder);
  }

  @Override
  public NpyByteArray asByteArray() {
    byte[] bytes = new byte[data.length];
    for (int i = 0; i < data.length; i++) {
      BigInteger val = data[i];
      if (val != null) {
        bytes[i] = (byte) val.intValueExact();
      }
    }
    return new NpyByteArray(copyShape(), bytes, fortranOrder);
  }

  @Override
  public NpyDoubleArray asDoubleArray() {
    double[] doubles = new double[data.length];
    for (int i = 0; i < data.length; i++) {
      BigInteger val = data[i];
      if (val != null) {
        doubles[i] = val.doubleValue();
      }
    }
    return new NpyDoubleArray(copyShape(), doubles, fortranOrder);
  }

  @Override
  public NpyFloatArray asFloatArray() {
    float[] floats = new float[data.length];
    for (int i = 0; i < data.length; i++) {
      BigInteger val = data[i];
      if (val != null) {
        floats[i] = (float) val.doubleValue();
      }
    }
    return new NpyFloatArray(copyShape(), floats, fortranOrder);
  }

  @Override
  public NpyIntArray asIntArray() {
    int[] ints = new int[data.length];
    for (int i = 0; i < data.length; i++) {
      BigInteger val = data[i];
      if (val != null) {
        ints[i] = val.intValueExact();
      }
    }
    return new NpyIntArray(copyShape(), ints, fortranOrder);
  }

  @Override
  public NpyLongArray asLongArray() {
    long[] longs = new long[data.length];
    for (int i = 0; i < data.length; i++) {
      BigInteger val = data[i];
      if (val != null) {
        longs[i] = val.longValueExact();
      }
    }
    return new NpyLongArray(copyShape(), longs, fortranOrder);
  }

  @Override
  public NpyShortArray asShortArray() {
    short[] shorts = new short[data.length];
    for (int i = 0; i < data.length; i++) {
      BigInteger val = data[i];
      if (val != null) {
        shorts[i] = (short) val.intValueExact();
      }
    }
    return new NpyShortArray(copyShape(), shorts, fortranOrder);
  }
}
