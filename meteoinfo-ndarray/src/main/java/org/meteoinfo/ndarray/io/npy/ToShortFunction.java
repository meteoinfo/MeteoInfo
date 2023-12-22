package org.meteoinfo.ndarray.io.npy;

@FunctionalInterface
interface ToShortFunction<T> {

  short applyAsShort(T value);

}
