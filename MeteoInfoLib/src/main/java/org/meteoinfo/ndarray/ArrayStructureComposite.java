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

import java.util.List;
import java.util.ArrayList;

/**
 * An ArrayStructure compose of other ArrayStructures.
 * Doesnt work because of read(StructureMembers.Member). this need to be withdrawn.
 *
 *    int total = 0;
    List<ArrayStructure> list = new ArrayList<ArrayStructure> (msgs.size());
    for (Message m : msgs) {
     ArrayStructure oneMess;
     if (!m.dds.isCompressed()) {
       MessageUncompressedDataReader reader = new MessageUncompressedDataReader();
       oneMess = reader.readEntireMessage(s, protoMessage, m, raf, null);
     } else {
       MessageCompressedDataReader reader = new MessageCompressedDataReader();
       oneMess = reader.readEntireMessage(s, protoMessage, m, raf, null);
     }
      list.add(oneMess);
      total += (int) oneMess.getSize();
    }

    return (list.size() == 1) ? list.get(0) : new ArrayStructureComposite(sm, list, total);
         
 *
 * @author caron
 * @since Nov 19, 2009
 */
public class ArrayStructureComposite extends ArrayStructureBak {
  private List<ArrayStructureBak> compose = new ArrayList<>();
  private int[] start;

  public ArrayStructureComposite(StructureMembers members, List<ArrayStructureBak> c, int total) {
    super(members, new int[total]);
    this.compose = c;

    start = new int[total];
    int count = 0;
    int i = 0;
    for (ArrayStructureBak as : compose) {
      start[i++] = count;
      count += (int) as.getSize();
    }
  }


  @Override
  protected StructureData makeStructureData(ArrayStructureBak me, int recno) {
    for (int i=0; i< start.length; i++) {
      if (recno >= start[i]) {
        ArrayStructureBak as = compose.get(i);
        return as.makeStructureData(as, recno - start[i]);
      }
    }
    throw new IllegalArgumentException();
  }
}
