/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;

/**
 * A class that contains static methods for converting multiple
 * bytes into one float or integer.
 */

public final class BufrNumbers {

  // used to check missing values when value is packed with all 1's
  private static final long[] missing_value = new long[2049];

  static {
    long accum = 0;
    for (int i = 0; i < 65; i++) {
      missing_value[i] = accum;
      accum = accum * 2 + 1;
    }
  }

  public static boolean isMissing(long raw, int bitWidth) {
    return (raw == BufrNumbers.missing_value[bitWidth]);
  }

  static long missingValue(int bitWidth) {
    return BufrNumbers.missing_value[bitWidth];
  }

  /** if missing value is not defined use this value. */
  private static final int UNDEFINED = -9999;

  /** Convert 2 bytes into a signed integer. */
  static int int2(RandomAccessFile raf) throws IOException {
    int a = raf.read();
    int b = raf.read();

    return int2(a, b);
  }

  /** Convert 2 bytes to a signed integer. */
  private static int int2(int a, int b) {
    if ((a == 0xff && b == 0xff)) // all bits set to one
      return UNDEFINED;

    return (1 - ((a & 128) >> 6)) * ((a & 127) << 8 | b);
  }

  /** Read 3 bytes and turn into a signed integer. */
  static int int3(RandomAccessFile raf) throws IOException {
    int a = raf.read();
    int b = raf.read();
    int c = raf.read();

    return int3(a, b, c);
  }

  /** Convert 3 bytes to signed integer. */
  private static int int3(int a, int b, int c) {
    return (1 - ((a & 128) >> 6)) * ((a & 127) << 16 | b << 8 | c);
  }

  /** Convert 4 bytes into a signed integer. */
  public static int int4(RandomAccessFile raf) throws IOException {
    int a = raf.read();
    int b = raf.read();
    int c = raf.read();
    int d = raf.read();

    return int4(a, b, c, d);
  }

  /** Convert 4 bytes into a signed integer. */
  private static int int4(int a, int b, int c, int d) {
    // all bits set to ones
    if (a == 0xff && b == 0xff && c == 0xff && d == 0xff)
      return UNDEFINED;

    return (1 - ((a & 128) >> 6)) * ((a & 127) << 24 | b << 16 | c << 8 | d);
  } // end int4

  /** Convert 2 bytes into an unsigned integer. */
  static int uint2(RandomAccessFile raf) throws IOException {
    int a = raf.read();
    int b = raf.read();

    return uint2(a, b);
  }

  /** Convert 2 bytes to an unsigned integer. */
  private static int uint2(int a, int b) {
    return a << 8 | b;
  }

  /** Read 3 bytes and convert into an unsigned integer. */
  public static int uint3(RandomAccessFile raf) throws IOException {
    int a = raf.read();
    int b = raf.read();
    int c = raf.read();

    return uint3(a, b, c);
  }

  /** Convert 3 bytes into an unsigned int. */
  private static int uint3(int a, int b, int c) {
    return a << 16 | b << 8 | c;
  }

  /** Read 4 bytes and convert into a float value. */
  public static float float4(RandomAccessFile raf) throws IOException {
    int a = raf.read();
    int b = raf.read();
    int c = raf.read();
    int d = raf.read();

    return float4(a, b, c, d);
  }

  /** Convert 4 bytes to a float. */
  private static float float4(int a, int b, int c, int d) {
    int sgn, mant, exp;

    mant = b << 16 | c << 8 | d;
    if (mant == 0)
      return 0.0f;

    sgn = -(((a & 128) >> 6) - 1);
    exp = (a & 127) - 64;

    return (float) (sgn * Math.pow(16.0, exp - 6) * mant);
  }

  /** Read 8 bytes and convert into a signed long. */
  public static long int8(RandomAccessFile raf) throws IOException {
    int a = raf.read();
    int b = raf.read();
    int c = raf.read();
    int d = raf.read();
    int e = raf.read();
    int f = raf.read();
    int g = raf.read();
    int h = raf.read();

    return (1 - ((a & 128) >> 6))
        * ((long) (a & 127) << 56 | (long) b << 48 | (long) c << 40 | (long) d << 32 | e << 24 | f << 16 | g << 8 | h);
  }
}
