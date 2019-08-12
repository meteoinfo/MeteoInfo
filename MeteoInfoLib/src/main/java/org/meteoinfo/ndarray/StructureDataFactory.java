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

/**
 * Convenience routines for constructing one-off StructureData objects
 *
 * @author caron
 * @since Jan 19, 2009
 */
public class StructureDataFactory {

  /* static public StructureData make(String name, String value) {
    StructureMembers members = new StructureMembers("");
    StructureMembers.Member m = members.addMember(name, null, null, DataType.STRING, new int[]{1});
    StructureDataW sw = new StructureDataW(members);
    Array dataArray = Array.factory(DataType.STRING, new int[]{1});
    dataArray.setObject(dataArray.getIndex(), value);
    sw.setMemberData(m, dataArray);
    return sw;
  } */

  static public StructureData make(String name, Object value) {
    StructureMembers members = new StructureMembers("");
    DataType dtype = DataType.getType(value.getClass());
    StructureMembers.Member m = members.addMember(name, null, null, dtype, new int[]{1});
    StructureDataW sw = new StructureDataW(members);
    Array dataArray = Array.factory(dtype, new int[]{1});
    dataArray.setObject(dataArray.getIndex(), value);
    sw.setMemberData(m, dataArray);
    return sw;
  }

  static public StructureData make(StructureData s1, StructureData s2) {
    return make(new StructureData[]{s1, s2});
  }

  static public StructureData make(StructureData[] sdatas) {
    if (sdatas.length == 1) return sdatas[0];

    // look for sole
    int count = 0;
    StructureData result = null;
    for (StructureData sdata : sdatas) {
      if (sdata != null) {
        count++;
        result = sdata;
      }
    }
    if (count == 1) return result;

    // combine
    StructureDataComposite result2 = new StructureDataComposite();
    for (StructureData sdata : sdatas) {
      if (sdata != null)
        result2.add(sdata);
    }
    return result2;
  }


}
