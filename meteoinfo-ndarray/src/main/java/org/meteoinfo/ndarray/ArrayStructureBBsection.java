/*
 * Copyright 1998-2015 University Corporation for Atmospheric Research/Unidata
 *
 *   Portions of this software were developed by the Unidata Program at the
 *   University Corporation for Atmospheric Research.
 *
 *   Access and use of this software shall impose the following obligations
 *   and understandings on the user. The user is granted the right, without
 *   any fee or cost, to use, copy, modify, alter, enhance and distribute
 *   this software, and any derivative works thereof, and its supporting
 *   documentation for any purpose whatsoever, provided that this entire
 *   notice appears in all copies of the software, derivative works and
 *   supporting documentation.  Further, UCAR requests that the user credit
 *   UCAR/Unidata in any publications that result from the use of this
 *   software or in any product that includes this software. The names UCAR
 *   and/or Unidata, however, may not be used in any advertising or publicity
 *   to endorse or promote any products or commercial entity unless specific
 *   written permission is obtained from UCAR/Unidata. The user also
 *   understands that UCAR/Unidata is not obligated to provide the user with
 *   any support, consulting, training or assistance of any kind with regard
 *   to the use, operation and performance of this software nor to provide
 *   the user with any updates, revisions, new versions or "bug fixes."
 *
 *   THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *   IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *   INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *   FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *   NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *   WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.meteoinfo.ndarray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Describe
 *
 * @author caron
 * @since 5/31/2015
 */
public class ArrayStructureBBsection extends ArrayStructureBB {
  protected int[] orgRecnum;

  /**
   * Make a section of an ArrayStructureBB
   *
   * @param org the original
   * @param section of the whole
   * @return original, if section is empty or the same saze as the original, else a section of the original
   */
  static public ArrayStructureBB factory(ArrayStructureBB org, Section section) {
    if (section == null || section.computeSize() == org.getSize())
      return org;
    return new ArrayStructureBBsection(org.getStructureMembers(), org.getShape(), org.getByteBuffer(), section);
  }

  private ArrayStructureBBsection(StructureMembers members, int[] shape, ByteBuffer bbuffer, Section section) {
    super(members, shape, bbuffer, 0);
    int n = (int) section.computeSize();
    Section.Iterator iter = section.getIterator(shape);
    orgRecnum = new int[n];
    int count = 0;
    while (iter.hasNext()) {
      orgRecnum[count++] = iter.next(null);
    }
  }


  @Override
  protected int calcOffsetSetOrder(int recnum, StructureMembers.Member m) {
    if (null != m.getDataObject())
      bbuffer.order( (ByteOrder) m.getDataObject());
    return bb_offset + orgRecnum[recnum] * getStructureSize() + m.getDataParam();
  }
}
