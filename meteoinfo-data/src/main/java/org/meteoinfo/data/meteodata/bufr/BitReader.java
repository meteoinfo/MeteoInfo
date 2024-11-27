/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr;

import ucar.unidata.io.RandomAccessFile;

import java.io.EOFException;
import java.io.IOException;

/**
 * Helper for reading data that has been bit packed.
 *
 * @author caron
 * @since Apr 7, 2008
 */
public class BitReader {

  private static final int BIT_LENGTH = Byte.SIZE;
  private static final int BYTE_BITMASK = 0xFF;
  private static final long LONG_BITMASK = Long.MAX_VALUE;

  private RandomAccessFile raf;
  private long startPos;

  private byte[] data;
  private int dataPos;

  private byte bitBuf;
  private int bitPos; // Current bit position in bitBuf.

  // for testing
  public BitReader(byte[] test) {
    this.data = test;
    this.dataPos = 0;
  }

  /**
   * Constructor
   *
   * @param raf the RandomAccessFile
   * @param startPos points to start of data in data section, in bytes
   * @throws IOException on read error
   */
  public BitReader(RandomAccessFile raf, long startPos) throws IOException {
    this.raf = raf;
    this.startPos = startPos;
    raf.seek(startPos);
  }

  /**
   * Go to the next byte in the stream
   */
  public void incrByte() {
    this.bitPos = 0;
  }

  /**
   * Position file at bitOffset from startPos
   *
   * @param bitOffset bit offset from starting position
   * @throws IOException on io error
   */
  public void setBitOffset(int bitOffset) throws IOException {
    if (bitOffset % 8 == 0) {
      raf.seek(startPos + bitOffset / 8);
      bitPos = 0;
      bitBuf = 0;
    } else {
      raf.seek(startPos + bitOffset / 8);
      bitPos = 8 - (bitOffset % 8);
      bitBuf = (byte) raf.read();
      bitBuf &= 0xff >> (8 - bitPos); // mask off consumed bits
    }
  }

  public long getPos() {
    if (raf != null) {
      return raf.getFilePointer();
    } else {
      return dataPos;
    }
  }

  /**
   * Read the next nb bits and return an Unsigned Long .
   *
   * @param nb the number of bits to convert to int, must be 0 <= nb <= 64.
   * @return result
   * @throws IOException on read error
   */
  public long bits2UInt(int nb) throws IOException {
    assert nb <= 64;
    assert nb >= 0;

    long result = 0;
    int bitsLeft = nb;

    while (bitsLeft > 0) {

      // we ran out of bits - fetch the next byte...
      if (bitPos == 0) {
        bitBuf = nextByte();
        bitPos = BIT_LENGTH;
      }

      // -- retrieve bit from current byte ----------
      // how many bits to read from the current byte
      int size = Math.min(bitsLeft, bitPos);
      // move my part to start
      int myBits = bitBuf >> (bitPos - size);
      // mask-off sign-extending
      myBits &= BYTE_BITMASK;
      // mask-off bits of next value
      myBits &= ~(BYTE_BITMASK << size);

      // -- put bit to result ----------------------
      // where to place myBits inside of result
      int shift = bitsLeft - size;
      assert shift >= 0;

      // put it there
      result |= myBits << shift;

      // -- put bit to result ----------------------
      // update information on what we consumed
      bitsLeft -= size;
      bitPos -= size;
    }

    return result;
  }

  /**
   * Read the next nb bits and return an Signed Long .
   *
   * @param nb the number of bits to convert to int, must be <= 64.
   * @return result
   * @throws IOException on read error
   */
  public long bits2SInt(int nb) throws IOException {

    long result = bits2UInt(nb);

    // check if we're negative
    if (getBit(result, nb)) {
      // it's negative! reset leading bit
      result = setBit(result, nb, false);
      // build 2's-complement
      result = ~result & LONG_BITMASK;
      result = result + 1;
    }

    return result;

  }

  private byte nextByte() throws IOException {
    if (raf != null) {
      int result = raf.read();
      if (result == -1)
        throw new EOFException();
      return (byte) result;
    } else {
      return data[dataPos++];
    }
  }

  public static long setBit(long decimal, int N, boolean value) {
    return value ? decimal | (1 << (N - 1)) : decimal & ~(1 << (N - 1));
  }

  public static boolean getBit(long decimal, int N) {
    int constant = 1 << (N - 1);
    return (decimal & constant) > 0;
  }

}
