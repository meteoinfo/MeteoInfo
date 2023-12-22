package org.meteoinfo.ndarray.io.npy;

@FunctionalInterface
interface ToFloatFunction<T> {

  float applyAsFloat(T value);

}
