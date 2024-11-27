/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import ucar.unidata.io.RandomAccessFile;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents Section 3 of a BUFR message.
 * 
 * @author caron
 * @since May 10, 2008
 */
@Immutable
public class BufrDataDescriptionSection {

  /**
   * Offset to start of BufrDataDescriptionSection.
   */
  private final long offset;

  /**
   * Number of data sets.
   */
  private final int ndatasets;

  /**
   * data type (observed or compressed).
   */
  private final int datatype;

  /**
   * List of data set descriptors.
   */
  private final List<Short> descriptors = new ArrayList<>();

  /**
   * Constructs a BufrDataDescriptionSection object by reading section 3 from a BUFR file.
   *
   * @param raf RandomAccessFile, position must be on a BUFR section 3
   * @throws IOException on read error
   */
  public BufrDataDescriptionSection(RandomAccessFile raf) throws IOException {
    offset = raf.getFilePointer();
    int length = BufrNumbers.uint3(raf);
    long EOS = offset + length;

    // reserved byte
    raf.read();

    // octets 5-6 number of datasets
    ndatasets = BufrNumbers.uint2(raf);

    // octet 7 data type bit 2 is for compressed data 192 or 64,
    // non-compressed data is 0 or 128
    datatype = raf.read();

    // get descriptors
    int ndesc = (length - 7) / 2;
    for (int i = 0; i < ndesc; i++) {
      int ch1 = raf.read();
      int ch2 = raf.read();
      short fxy = (short) ((ch1 << 8) + (ch2));
      descriptors.add(fxy);
    }

    // reset for any offset discrepancies
    raf.seek(EOS);
  }

  /**
   * Offset to the beginning of BufrDataDescriptionSection.
   *
   * @return offset in bytes of BUFR record
   */
  public final long getOffset() {
    return offset;
  }

  /**
   * Number of data sets in this record.
   *
   * @return datasets
   */
  public final int getNumberDatasets() {
    return ndatasets;
  }

  /**
   * Data type (compressed or non-compressed).
   *
   * @return datatype
   */
  public final int getDataType() {
    return datatype;
  }

  /**
   * Observation data
   *
   * @return true if observation data
   */
  public boolean isObserved() {
    return (datatype & 0x80) != 0;
  }

  /**
   * Is data compressed?
   *
   * @return true if data is compressed
   */
  public boolean isCompressed() {
    return (datatype & 0x40) != 0;
  }

  /**
   * get list of data descriptors as Shorts
   *
   * @return descriptors as List<Short>
   */
  public final List<Short> getDataDescriptors() {
    return descriptors;
  }

  /**
   * get list of data descriptors as Strings
   *
   * @return descriptors as List<String>
   */
  public final List<String> getDescriptors() {
    List<String> desc = new ArrayList<>();
    for (short fxy : descriptors)
      desc.add(Descriptor.makeString(fxy));
    return desc;
  }
}
