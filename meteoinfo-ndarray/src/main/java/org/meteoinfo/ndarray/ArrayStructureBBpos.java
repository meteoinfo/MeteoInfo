/*
 * Copyright 1998-2014 University Corporation for Atmospheric Research/Unidata
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
 * Variation of ArrayStructureBB, where the offsets of the records into the ByteBuffer are uneven and must be
 * passed in by the user.
 *
 * @author caron
 */
public class ArrayStructureBBpos extends ArrayStructureBB {
  protected int[] positions;

  /**
   * Construct an ArrayStructureBB with the given ByteBuffer.
   *
   * @param members the list of structure members.
   * @param shape the shape of the structure array
   * @param bbuffer the data is stored in this ByteBuffer. bbuffer.order must already be set.
   * @param positions offset from the start of the ByteBufffer to each record. must have length = with shape.getSize()
   */
  public ArrayStructureBBpos(StructureMembers members, int[] shape, ByteBuffer bbuffer, int[] positions) {
    super(members, shape, bbuffer, 0);
    this.positions = positions;
  }


  @Override
  protected int calcOffsetSetOrder(int recnum, StructureMembers.Member m) {
    if (null != m.getDataObject())
      bbuffer.order( (ByteOrder) m.getDataObject());
    return positions[recnum] + m.getDataParam();
  }
}