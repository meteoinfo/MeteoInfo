package org.meteoinfo.ndarray.io.npy;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public final class NpyIntArray extends AbstractNpyArray<int[]> {

  public NpyIntArray(int[] shape, int[] data, boolean fortranOrder) {
    super(shape, data, fortranOrder);
  }

  public static NpyIntArray vectorOf(int[] data) {
    return new NpyIntArray(new int[] {data.length}, data, false);
  }

  /**
   * Wraps the given data in a 2-dimensional array in row-major order (C order).
   *
   * @param data the data of the array
   * @param rows the number of rows of the array
   * @param cols the number of columns of the array
   * @return a 2d array of the given shape
   */
  public static NpyIntArray rowOrderOf(int[] data, int rows, int cols) {
    return new NpyIntArray(new int[]{rows, cols}, data, false);
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
  public static NpyIntArray columnOrderOf(int[] data, int rows, int cols) {
    return new NpyIntArray(new int[]{rows, cols}, data, true);
  }

  @Override
  public NpyDataType dataType() {
    return NpyDataType.i4;
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
    buffer.putInt(data[i]);
  }

  @Override
  public boolean isIntArray() {
    return true;
  }

  @Override
  public NpyIntArray asIntArray() {
    return this;
  }

  @Override
  public NpyBooleanArray asBooleanArray() {
    boolean[] booleans = new boolean[data.length];
    for (int i = 0; i < data.length; i++) {
      booleans[i] = data[i] != 0;
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
  public NpyCharArray asCharArray() {
    int bufferSize = Math.max(data.length, 10);
    CharBuffer buffer = CharBuffer.allocate(bufferSize);
    for (int i : data) {
      char[] next = Character.toChars(i);

      // because a code point can result in multiple
      // characters, we may need to allocate a larger
      // buffer here
      if (buffer.remaining() < next.length) {
        bufferSize = Math.max(
          bufferSize + next.length,
          bufferSize + (bufferSize >> 1));
        if (bufferSize < 0)
          throw new OutOfMemoryError();
        char[] chars = new char[bufferSize];
        buffer.flip();
        int nextPos = buffer.limit();
        buffer.get(chars, 0, nextPos);
        buffer = CharBuffer.wrap(chars);
        buffer.position(nextPos);
      }

      for (char c : next) {
        buffer.put(c);
      }
    }

    char[] chars;
    if (buffer.remaining() == 0) {
      chars = buffer.array();
    } else {
      buffer.flip();
      chars = new char[buffer.limit()];
      buffer.get(chars, 0, buffer.limit());
    }

    return new NpyCharArray(copyShape(), chars, fortranOrder);
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
      floats[i] = (float) data[i];
    }
    return new NpyFloatArray(copyShape(), floats, fortranOrder);
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
      shorts[i] = (short) data[i];
    }
    return new NpyShortArray(copyShape(), shorts, fortranOrder);
  }

}
  
