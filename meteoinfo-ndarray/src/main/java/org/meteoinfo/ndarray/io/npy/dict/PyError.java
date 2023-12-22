package org.meteoinfo.ndarray.io.npy.dict;

final class PyError implements PyValue {

  private final String message;

  PyError(String message) {
    this.message = message;
  }

  @Override
  public boolean isError() {
    return true;
  }

  static PyError of(String message) {
    return new PyError(message);
  }

  String message() {
    return message;
  }

}
