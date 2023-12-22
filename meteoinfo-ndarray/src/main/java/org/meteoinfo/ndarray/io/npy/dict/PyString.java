package org.meteoinfo.ndarray.io.npy.dict;

final class PyString implements PyValue {

  private final String value;

  PyString(String value) {
    this.value = value;
  }

  @Override
  public boolean isString() {
    return true;
  }

  public String value() {
    return value;
  }
}
