package org.meteoinfo.ndarray.io.npy.dict;

interface PyValue {

  default boolean isNone() {
    return false;
  }

  default PyNone asNone() {
    return (PyNone) this;
  }

  default boolean isError() {
    return false;
  }

  default PyError asError() {
    return (PyError) this;
  }

  default boolean isDict() {
    return false;
  }

  default PyDict asDict() {
    return (PyDict) this;
  }

  default boolean isString() {
    return false;
  }

  default PyString asString() {
    return (PyString) this;
  }

  default boolean isInt() {
    return false;
  }

  default PyInt asInt() {
    return (PyInt) this;
  }

  default boolean isIdentifier() {
    return false;
  }

  default PyIdentifier asIdentifier() {
    return (PyIdentifier) this;
  }

  default boolean isTuple() {
    return false;
  }

  default PyTuple asTuple() {
    return (PyTuple) this;
  }
}
