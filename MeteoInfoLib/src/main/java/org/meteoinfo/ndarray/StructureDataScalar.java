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
 * A StructureData with scalar data.
 *
 * @author caron
 * @since Jan 26, 2009
 */
public class StructureDataScalar extends StructureDataW {

  public StructureDataScalar(String name) {
    super(new StructureMembers(name));
  }

  public void addMember(String name, String desc, String units, DataType dtype, boolean isUnsigned, Number val) {
    StructureMembers.Member m = members.addMember(name, desc, units, dtype,  new int[0]);
    Array data = null;
    switch (dtype) {
      case BYTE:
        data = new ArrayByte.D0();
        data.setByte(0, val.byteValue());
        break;
      case SHORT:
        data = new ArrayShort.D0();
        data.setShort(0, val.shortValue());
        break;
      case INT:
        data = new ArrayInt.D0();
        data.setInt(0, val.intValue());
        break;
      case LONG:
        data = new ArrayLong.D0();
        data.setDouble(0, val.longValue());
        break;
      case FLOAT:
        data = new ArrayFloat.D0();
        data.setFloat(0, val.floatValue());
        break;
      case DOUBLE:
        data = new ArrayDouble.D0();
        data.setDouble(0, val.doubleValue());
        break;
    }
    setMemberData(m, data);
  }

  public void addMemberString(String name, String desc, String units, String val, int max_len) {
    StructureMembers.Member m = members.addMember(name, desc, units, DataType.CHAR, new int[] { max_len});
    Array data = ArrayChar.makeFromString(val, max_len);
    setMemberData(m, data);
  }


/*  public void addMember(String name, String desc, String units, double val) {
    StructureMembers.Member m = members.addMember(name, desc, units, DataType.DOUBLE,  new int[0]);
    ArrayDouble.D0 data = new ArrayDouble.D0();
    data.set(val);
    setMemberData(m, data);
  }

  public void addMember(String name, String desc, String units, float val) {
    StructureMembers.Member m = members.addMember(name, desc, units, DataType.FLOAT,  new int[0]);
    ArrayFloat.D0 data = new ArrayFloat.D0();
    data.set(val);
    setMemberData(m, data);
  }

  public void addMember(String name, String desc, String units, short val) {
    StructureMembers.Member m = members.addMember(name, desc, units, DataType.SHORT,  new int[0]);
    ArrayShort.D0 data = new ArrayShort.D0();
    data.set(val);
    setMemberData(m, data);
  }

  public void addMember(String name, String desc, String units, int val) {
    StructureMembers.Member m = members.addMember(name, desc, units, DataType.INT,  new int[0]);
    ArrayInt.D0 data = new ArrayInt.D0();
    data.set(val);
    setMemberData(m, data);
  }

  /* public void addMember(String name, String desc, String units, long val) {
    StructureMembers.Member m = members.addMember(name, desc, units, DataType.LONG,  new int[0]);
    ArrayLong.D0 data = new ArrayLong.D0();
    data.set(val);
    setMemberData(m, data);
  }  */
}
