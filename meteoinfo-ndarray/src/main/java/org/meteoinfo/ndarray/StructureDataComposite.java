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

import java.util.HashMap;
import java.util.Map;

/**
 * A composite of other StructureData.
 * If multiple members of same name exist, the first one added is used
 *
 * @author caron
 * @since Jan 21, 2009
 */
public class StructureDataComposite extends StructureData {
  protected Map<StructureMembers.Member, StructureData> proxy = new HashMap<>(32);

  public StructureDataComposite() {
    super(new StructureMembers(""));
  }

  public void add(StructureData sdata) {
    for (StructureMembers.Member m : sdata.getMembers()) {
      if (this.members.findMember(m.getName()) == null) {
        this.members.addMember(m);
        proxy.put(m, sdata);
      }
    }
  }

  public void add(int pos, StructureData sdata) {
    for (StructureMembers.Member m : sdata.getMembers()) {
      if (this.members.findMember(m.getName()) == null) {
        this.members.addMember(pos++, m);
        proxy.put(m, sdata);
      }
    }
  }

  public Array getArray(StructureMembers.Member m) {
    StructureData sdata = proxy.get(m);
    return sdata.getArray(m.getName());
  }

  public float convertScalarFloat(StructureMembers.Member m) {
    return proxy.get(m).convertScalarFloat(m.getName());
  }

  public double convertScalarDouble(StructureMembers.Member m) {
    return proxy.get(m).convertScalarDouble(m.getName());
  }

  public int convertScalarInt(StructureMembers.Member m) {
    return proxy.get(m).convertScalarInt(m.getName());
  }

  public long convertScalarLong(StructureMembers.Member m) {
    return proxy.get(m).convertScalarLong(m.getName());
  }

  public double getScalarDouble(StructureMembers.Member m) {
    return proxy.get(m).getScalarDouble(m.getName());
  }

  public double[] getJavaArrayDouble(StructureMembers.Member m) {
    return proxy.get(m).getJavaArrayDouble(m.getName());
  }

  public float getScalarFloat(StructureMembers.Member m) {
    return proxy.get(m).getScalarFloat(m.getName());
  }

  public float[] getJavaArrayFloat(StructureMembers.Member m) {
    return proxy.get(m).getJavaArrayFloat(m.getName());
  }

  public byte getScalarByte(StructureMembers.Member m) {
    return proxy.get(m).getScalarByte(m.getName());
  }

  public byte[] getJavaArrayByte(StructureMembers.Member m) {
    return proxy.get(m).getJavaArrayByte(m.getName());
  }

  public int getScalarInt(StructureMembers.Member m) {
    return proxy.get(m).getScalarInt(m.getName());
  }

  public int[] getJavaArrayInt(StructureMembers.Member m) {
    return proxy.get(m).getJavaArrayInt(m.getName());
  }

  public short getScalarShort(StructureMembers.Member m) {
    return proxy.get(m).getScalarShort(m.getName());
  }

  public short[] getJavaArrayShort(StructureMembers.Member m) {
    return proxy.get(m).getJavaArrayShort(m.getName());
  }

  public long getScalarLong(StructureMembers.Member m) {
    return proxy.get(m).getScalarLong(m.getName());
  }

  public long[] getJavaArrayLong(StructureMembers.Member m) {
    return proxy.get(m).getJavaArrayLong(m.getName());
  }

  public char getScalarChar(StructureMembers.Member m) {
    return proxy.get(m).getScalarChar(m.getName());
  }

  public char[] getJavaArrayChar(StructureMembers.Member m) {
    return proxy.get(m).getJavaArrayChar(m.getName());
  }

  public String getScalarString(StructureMembers.Member m) {
    return proxy.get(m).getScalarString(m.getName());
  }

  public String[] getJavaArrayString(StructureMembers.Member m) {
    return proxy.get(m).getJavaArrayString(m.getName());
  }

  public StructureData getScalarStructure(StructureMembers.Member m) {
    return proxy.get(m).getScalarStructure(m.getName());
  }

  public ArrayStructureBak getArrayStructure(StructureMembers.Member m) {
    return proxy.get(m).getArrayStructure(m.getName());
  }

  public ArraySequence getArraySequence(StructureMembers.Member m) {
    return proxy.get(m).getArraySequence(m.getName());
  }

  public Object getScalarObject( StructureMembers.Member m) {
    return proxy.get(m).getScalarObject(m.getName());
  }  

}
