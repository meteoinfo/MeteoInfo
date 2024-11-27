/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;


import java.util.Formatter;

/**
 * Count size of compressed fields
 *
 * @author caron
 * @since Jul 4, 2008
 */
public class BitCounterCompressed implements BitCounter {

  private final DataDescriptor dkey; // the field to count
  private final int nrows; // number of (obs) in the compression
  private final int bitOffset; // starting position of the compressed data, relative to start of data section
  private int dataWidth; // bitWidth of incremental values
  private BitCounterCompressed[][] nested; // used if the dkey is a structure = nested[innerRows][dkey.subkeys.size]

  /**
   * This counts the size of an array of Structures or Sequences, ie Structure(n)
   *
   * @param dkey is a structure or a sequence - so has subKeys
   * @param n numbers of rows in the table
   * @param bitOffset number of bits taken up by the count variable (non-zero only for sequences)
   */
  public BitCounterCompressed(DataDescriptor dkey, int n, int bitOffset) {
    this.dkey = dkey;
    this.nrows = n;
    this.bitOffset = bitOffset;
  }

  void setDataWidth(int dataWidth) {
    this.dataWidth = dataWidth;
  }

  public int getStartingBitPos() {
    return bitOffset;
  }

  public int getBitPos(int msgOffset) {
    return bitOffset + dkey.bitWidth + 6 + dataWidth * msgOffset;
  }

  public int getTotalBits() {
    if (nested == null)
      return dkey.bitWidth + 6 + dataWidth * nrows;
    else {
      int totalBits = 0;
      for (BitCounterCompressed[] counters : nested) {
        if (counters == null)
          continue;
        for (BitCounterCompressed counter : counters)
          if (counter != null)
            totalBits += counter.getTotalBits();
      }
      if (dkey.replicationCountSize > 0)
        totalBits += dkey.replicationCountSize + 6; // 6 boit count, 6 bit extra
      return totalBits;
    }
  }

  public BitCounterCompressed[] getNestedCounters(int innerIndex) {
    return nested[innerIndex];
  }

  public void addNestedCounters(int innerDimensionSize) {
    nested = new BitCounterCompressed[innerDimensionSize][dkey.getSubKeys().size()];
  }

  /**
   * Number of nested fields
   * 
   * @return 1 if no nested fields, otherwise count of nested fields
   */
  public int ncounters() {
    if (nested == null)
      return 1;
    else {
      int ncounters = 0;
      for (BitCounterCompressed[] counters : nested) {
        if (counters == null)
          continue;
        for (BitCounterCompressed counter : counters)
          if (counter != null)
            ncounters += counter.ncounters();
      }
      return ncounters;
    }
  }

  public void show(Formatter out, int indent) {
    for (int i = 0; i < indent; i++)
      out.format(" ");
    out.format("%8d %8d %4d %s %n", getTotalBits(), bitOffset, dataWidth, dkey.name);
    if (nested != null) {
      for (BitCounterCompressed[] counters : nested) {
        if (counters == null)
          continue;
        for (BitCounterCompressed counter : counters)
          if (counter != null)
            counter.show(out, indent + 2);
      }
    }
  }

  @Override
  public int getNumberRows() {
    return nrows;
  }
}

