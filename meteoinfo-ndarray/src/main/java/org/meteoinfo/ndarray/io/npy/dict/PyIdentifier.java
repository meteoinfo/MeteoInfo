package org.meteoinfo.ndarray.io.npy.dict;

final class PyIdentifier implements PyValue {

  private final String value;

  PyIdentifier(String value) {
    this.value = value;
  }

  @Override
  public boolean isIdentifier() {
    return true;
  }

  public String value() {
    return value;
  }
}
