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

import org.meteoinfo.ndarray.util.Indent;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

/**
 * A StructureData implementation that has its data self-contained.
 * This is often the easiest to construct, but not very efficient for large arrays of Structures.
 * You should call setMemberData() for each member.
 *
 * @author caron
 */

public class StructureDataW extends StructureData {
  protected final Map<StructureMembers.Member, Array> memberData;

  /**
   * Constructor.
   *
   * @param members    StructureData is always contained in a StructureArray.
   */
  public StructureDataW(StructureMembers members) {
    super(members);
    memberData = new HashMap<>(2*members.getMembers().size());
  }

  public StructureDataW(StructureMembers members, int size) {
    super(members);
    memberData = new HashMap<>(2*size);
  }

  /* Copy constructor.
   *  This makes a local copy of all the data in the from StrucureData.
   * @param from copy from here
   *
  public StructureDataW (StructureData from) {
    this(from.getStructureMembers());
    List<StructureMembers.Member> members = getMembers();
    for (StructureMembers.Member m : members) {
      Array data = from.getArray(m);
      setMemberData(m, data.copy());  // LOOK wont work for StructureData
    }
  } */

  public void setMemberData(StructureMembers.Member m, Array data) {
    if (data == null)
      throw new IllegalArgumentException("data cant be null");

    memberData.put(m, data);
  }

  public void setMemberData(String memberName, Array data) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null)
      throw new IllegalArgumentException("illegal member name =" + memberName);
    setMemberData(m, data);
  }

  /**
   * Get member data array of any type as an Array.
   * @param m get data from this StructureMembers.Member.
   * @return Array values.
   */
  public Array getArray(StructureMembers.Member m) {
    if (m == null) throw new IllegalArgumentException("member is null");
    return memberData.get(m);
  }

  public float convertScalarFloat(StructureMembers.Member m) {
    return getScalarFloat(m);
  }

  public double convertScalarDouble(StructureMembers.Member m) {
    return getScalarDouble(m);
  }

  public int convertScalarInt(StructureMembers.Member m) {
    return getScalarInt(m);
  }

  public long convertScalarLong(StructureMembers.Member m) {
    return getScalarLong(m);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get member data of type double.
   * @param m get data from this StructureMembers.Member. Must be of type double.
   * @return scalar double value
   */
  public double getScalarDouble(StructureMembers.Member m) {
    Array data = getArray(m);
    return data.getDouble(Index.scalarIndexImmutable);
  }


  /**
   * Get java double array for a member of type double.
   * @param m get data from this StructureMembers.Member. Must be of type double.
   * @return 1D java array of doubles
   */
  public double[] getJavaArrayDouble(StructureMembers.Member m) {
    Array data = getArray(m);
    return (double []) data.getStorage();
  }

  ////////////////

  /**
   * Get member data of type float.
   * @param m get data from this StructureMembers.Member. Must be of type float.
   * @return scalar double value
   */
  public float getScalarFloat(StructureMembers.Member m) {
    Array data = getArray(m);
    return data.getFloat(Index.scalarIndexImmutable);
  }

  /**
   * Get java float array for a member of type float.
   * @param m get data from this StructureMembers.Member. Must be of type float.
   * @return 1D java array of floats
   */
  public float[] getJavaArrayFloat(StructureMembers.Member m) {
    Array data = getArray(m);
    return (float []) data.getStorage();
  }

  /////

  /**
   * Get member data of type byte.
   * @param m get data from this StructureMembers.Member. Must be of type byte.
   * @return scalar byte value
   */
  public byte getScalarByte(StructureMembers.Member m) {
    Array data = getArray(m);
    return data.getByte(Index.scalarIndexImmutable);
  }

  /**
   * Get java byte array for a member of type byte.
   * @param m get data from this StructureMembers.Member. Must be of type byte.
   * @return 1D java array of bytes
   */
  public byte[] getJavaArrayByte(StructureMembers.Member m) {
    Array data = getArray(m);
    return (byte []) data.getStorage();
  }

  /////

  /**
   * Get member data of type int.
   * @param m get data from this StructureMembers.Member. Must be of type int.
   * @return scalar int value
   */
  public int getScalarInt(StructureMembers.Member m) {
    Array data = getArray(m);
    return data.getInt(Index.scalarIndexImmutable);
  }

  /**
   * Get java int array for a member of type int.
   * @param m get data from this StructureMembers.Member. Must be of type int.
   * @return 1D java array of ints
   */
  public int[] getJavaArrayInt(StructureMembers.Member m) {
    Array data = getArray(m);
    return (int []) data.getStorage();
  }

  /////

  /**
   * Get member data of type short.
   * @param m get data from this StructureMembers.Member. Must be of type short.
   * @return scalar short value
   */
  public short getScalarShort(StructureMembers.Member m) {
    Array data = getArray(m);
    return data.getShort(Index.scalarIndexImmutable);
  }

  /**
   * Get java short array for a member of type short.
   * @param m get data from this StructureMembers.Member. Must be of type short.
   * @return 1D java array of shorts
   */
  public short[] getJavaArrayShort(StructureMembers.Member m) {
    Array data = getArray(m);
    return (short []) data.getStorage();
  }


  /////

  /**
   * Get member data of type long.
   * @param m get data from this StructureMembers.Member. Must be of type long.
   * @return scalar long value
   */
  public long getScalarLong(StructureMembers.Member m) {
    Array data = getArray(m);
    return data.getLong(Index.scalarIndexImmutable);
  }

  /**
   * Get java long array for a member of type long.
   * @param m get data from this StructureMembers.Member. Must be of type long.
   * @return 1D java array of longs
   */
  public long[] getJavaArrayLong(StructureMembers.Member m) {
    Array data = getArray(m);
    return (long []) data.getStorage();
  }


/////

  /**
   * Get member data of type char.
   * @param m get data from this StructureMembers.Member. Must be of type char.
   * @return scalar char value
   */
  public char getScalarChar(StructureMembers.Member m) {
    Array data = getArray(m);
    return data.getChar(Index.scalarIndexImmutable);
  }

  /**
   * Get java char array for a member of type char.
   * @param m get data from this StructureMembers.Member. Must be of type char.
   * @return 1D java array of chars
   */
  public char[] getJavaArrayChar(StructureMembers.Member m) {
    Array data = getArray(m);
    return (char []) data.getStorage();
  }

  /**
   * Get String value, from rank 0 String or rank 1 char member array.
   * @param m get data from this StructureMembers.Member. Must be of type char or String.
   */
  public String getScalarString(StructureMembers.Member m) {
    if (m.getDataType() == DataType.STRING) {
      Array data = getArray(m);
      if (data == null)
        data = getArray(m);
      return (String) data.getObject(0);
    } else {
      char[] ba = getJavaArrayChar(m);
      int count = 0;
      while (count < ba.length) {
        if (0 == ba[count]) break;
        count++;
      }
      return new String(ba, 0, count);
    }
  }

  public String[] getJavaArrayString(StructureMembers.Member m) {
    if (m.getDataType() == DataType.STRING) {
      Array data = getArray(m);
      int n = m.getSize();
      String[] result = new String[n];
      for (int i = 0; i < result.length; i++)
        result[i] = (String) data.getObject(i);
     return result;

    } else if (m.getDataType() == DataType.CHAR) {
      ArrayChar data = (ArrayChar) getArray(m);
      ArrayChar.StringIterator iter = data.getStringIterator();
      String[] result = new String[ iter.getNumElems()];
      int count = 0;
      while (iter.hasNext())
        result[count++] =  iter.next();
      return result;
    }

    throw new IllegalArgumentException("getJavaArrayString: not String DataType :"+m.getDataType());
  }

  /**
   * Get member data of type Structure.
   * @param m get data from this StructureMembers.Member. Must be of type Structure.
   * @return StructureData
   */
  public StructureData getScalarStructure(StructureMembers.Member m) {
    ArrayStructureBak data = (ArrayStructureBak) getArray(m);
    return data.getStructureData(0);
  }

  /**
   * Get member data of type Structure.
   * @param m get data from this StructureMembers.Member. Must be of type Structure.
   * @return StructureData
   */
  @Override
  public ArrayStructureBak getArrayStructure(StructureMembers.Member m) {
    return (ArrayStructureBak) getArray(m);
  }

  public ArraySequence getArraySequence(StructureMembers.Member m) {
    return (ArraySequence) getArray(m);
  }

  @Override
  public void showInternal(Formatter f, Indent indent) {
    super.showInternal(f, indent);
    for (StructureMembers.Member m : memberData.keySet()) {
      Array data = memberData.get(m);
      f.format("%s %s = %s%n", indent, m, data);
    }

  }

}
