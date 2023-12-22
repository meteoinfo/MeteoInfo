package org.meteoinfo.ndarray.io.npy.dict;

final class PyInt implements PyValue {

  private final long value;

  PyInt(long value) {
    this.value = value;
  }

  @Override
  public boolean isInt() {
    return true;
  }

  public long value() {
    return value;
  }
}
