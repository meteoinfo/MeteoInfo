package org.meteoinfo.ndarray.io.npy;

import java.math.BigInteger;

class OrderSwitch2d<T extends NpyArray<?>> {

  private final T array;
  private final int rows;
  private final int cols;

  private OrderSwitch2d(T array) {
    this.array = array;
    this.rows = Array2d.rowCountOf(array);
    this.cols = Array2d.columnCountOf(array);
  }

  static <T extends NpyArray<?>> T of(T array) {
    return new OrderSwitch2d<T>(array).apply();
  }

  @SuppressWarnings("unchecked")
  private T apply() {
    if (array.isBigIntegerArray())
      return (T) switchBigInts((NpyBigIntArray) array);
    if (array.isBooleanArray())
      return (T) switchBooleans(array.asBooleanArray());
    if (array.isByteArray())
      return (T) switchBytes(array.asByteArray());
    if (array.isCharArray())
      return (T) switchChars(array.asCharArray());
    if (array.isDoubleArray())
      return (T) switchDoubles(array.asDoubleArray());
    if (array.isFloatArray())
      return (T) switchFloats(array.asFloatArray());
    if (array.isIntArray())
      return (T) switchInts(array.asIntArray());
    if (array.isLongArray())
      return (T) switchLongs(array.asLongArray());
    if (array.isShortArray())
      return (T) switchShorts(array.asShortArray());

    throw new IllegalArgumentException("unsupported array type: " + array);
  }

  private NpyBigIntArray switchBigInts(NpyBigIntArray a) {
    BigInteger[] data = a.data;
    BigInteger[] newData = new BigInteger[data.length];
    iter((pos, newPos) -> {
      BigInteger value = data[pos];
      if (value == null)
        return;
      newData[newPos] = value; // BigInts are immutable; so this is fine
    });
    return a.hasColumnOrder()
      ? NpyBigIntArray.rowOrderOf(newData, rows, cols)
      : NpyBigIntArray.columnOrderOf(newData, rows, cols);
  }

  private NpyBooleanArray switchBooleans(NpyBooleanArray a) {
    boolean[] data = a.data;
    boolean[] newData = new boolean[data.length];
    iter((pos, newPos) -> {
      boolean value = data[pos];
      if (!value)
        return;
      newData[newPos] = true;
    });
    return a.hasColumnOrder()
      ? NpyBooleanArray.rowOrderOf(newData, rows, cols)
      : NpyBooleanArray.columnOrderOf(newData, rows, cols);
  }

  private NpyByteArray switchBytes(NpyByteArray a) {
    byte[] data = a.data;
    byte[] newData = new byte[data.length];
    iter((pos, newPos) -> {
      byte value = data[pos];
      if (value == 0)
        return;
      newData[newPos] = value;
    });
    return a.hasColumnOrder()
      ? NpyByteArray.rowOrderOf(newData, rows, cols)
      : NpyByteArray.columnOrderOf(newData, rows, cols);
  }

  private NpyCharArray switchChars(NpyCharArray a) {
    char[] data = a.data;
    char[] newData = new char[data.length];
    iter((pos, newPos) -> {
      char value = data[pos];
      if (value == 0)
        return;
      newData[newPos] = value;
    });
    return a.hasColumnOrder()
      ? new NpyCharArray(new int[]{rows, cols}, newData, false)
      : new NpyCharArray(new int[]{rows, cols}, newData, true);
  }

  private NpyDoubleArray switchDoubles(NpyDoubleArray a) {
    double[] data = a.data;
    double[] newData = new double[data.length];
    iter((pos, newPos) -> {
      double value = data[pos];
      if (value == 0)
        return;
      newData[newPos] = value;
    });
    return a.hasColumnOrder()
      ? NpyDoubleArray.rowOrderOf(newData, rows, cols)
      : NpyDoubleArray.columnOrderOf(newData, rows, cols);
  }

  private NpyFloatArray switchFloats(NpyFloatArray a) {
    float[] data = a.data;
    float[] newData = new float[data.length];
    iter((pos, newPos) -> {
      float value = data[pos];
      if (value == 0)
        return;
      newData[newPos] = value;
    });
    return a.hasColumnOrder()
      ? NpyFloatArray.rowOrderOf(newData, rows, cols)
      : NpyFloatArray.columnOrderOf(newData, rows, cols);
  }

  private NpyIntArray switchInts(NpyIntArray a) {
    int[] data = a.data;
    int[] newData = new int[data.length];
    iter((pos, newPos) -> {
      int value = data[pos];
      if (value == 0)
        return;
      newData[newPos] = value;
    });
    return a.hasColumnOrder()
      ? NpyIntArray.rowOrderOf(newData, rows, cols)
      : NpyIntArray.columnOrderOf(newData, rows, cols);
  }

  private NpyLongArray switchLongs(NpyLongArray a) {
    long[] data = a.data;
    long[] newData = new long[data.length];
    iter((pos, newPos) -> {
      long value = data[pos];
      if (value == 0)
        return;
      newData[newPos] = value;
    });
    return a.hasColumnOrder()
      ? NpyLongArray.rowOrderOf(newData, rows, cols)
      : NpyLongArray.columnOrderOf(newData, rows, cols);
  }

  private NpyShortArray switchShorts(NpyShortArray a) {
    short[] data = a.data;
    short[] newData = new short[data.length];
    iter((pos, newPos) -> {
      short value = data[pos];
      if (value == 0)
        return;
      newData[newPos] = value;
    });
    return a.hasColumnOrder()
      ? NpyShortArray.rowOrderOf(newData, rows, cols)
      : NpyShortArray.columnOrderOf(newData, rows, cols);
  }

  private void iter(IndexFn fn) {
    int pos = 0;
    if (array.hasColumnOrder()) {
      for (int col = 0; col < cols; col++) {
        for (int row = 0; row < rows; row++) {
          int newPos = row * cols + col;
          fn.accept(pos, newPos);
          pos++;
        }
      }
    } else {
      for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
          int newPos = col * rows + row;
          fn.accept(pos, newPos);
          pos++;
        }
      }
    }
  }

  @FunctionalInterface
  interface IndexFn {
    void accept(int pos, int newPos);
  }

}
