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

import org.meteoinfo.util.Indent;

import java.util.Formatter;
import java.util.List;

/**
 * A container for a Structure's data. 
 * Is normally contained within an ArrayStructure, which is an Array of StructureData.
 * This is the abstract supertype for all implementations.
 *
 * <pre>
   for (Iterator iter = sdata.getMembers().iterator(); iter.hasNext(); ) {
      StructureMembers.Member m = (StructureMembers.Member) iter.next();
      Array sdataArray = sdata.getArray(m);
      ...
   }
  </pre>

 * General ways to access data in an StructureData are:
    <pre> Array getArray(Member m) </pre>
    <pre> Array getArray(String memberName) </pre>

 * The following will return an object of type Byte, Char, Double, Float, Int, Long, Short, String, or Structure, depending 
 * upon the member type:
   <pre> Object getScalarObject( Member m) </pre>

 * A number of convenience routines may be able to avoid extra Object creation, and so are recommended for efficiency.
 * These require that you know the data types of the member data, but they are the most efficent:
   <pre>
    getScalarXXX(int recnum, Member m)
    getJavaArrayXXX(int recnum, Member m) </pre>
 * where XXX is Byte, Char, Double, Float, Int, Long, Short, or String. For members that are themselves Structures,
   the equivilent is:
   <pre>
    StructureData getScalarStructure(int recnum, Member m)
    ArrayStructure getArrayStructure(int recnum, Member m) </pre>

 * These will return any compatible type as a double or float, but may have extra overhead when the types dont match:
   <pre>
    convertScalarXXX(int recnum, Member m)
    convertJavaArrayXXX(int recnum, Member m) </pre>
  where XXX is Double or Float

 *
 * @author caron
 * @see ArrayStructure
 */

abstract public class StructureData {

  static public final StructureData EMPTY = new StructureDataScalar("empty");

  /*
   * Copy all the data out of 'from' and into a new StructureData.
   * @param from copy from here
   * @return a new StructureData object.
   *
  public static StructureData copy( StructureData from) {
    return new StructureDataW( from);
  } */

  /////////////////////////////////////////////////////////
  protected StructureMembers members;

  /**
   * Constructor.
   *
   * @param members    StructureData is always contained in a StructureArray.
   */
  protected StructureData(StructureMembers members) {
    this.members = members;
  }

  /**
   * @return name of Structure
   */
  public String getName() {
    return members.getName();
  }

  /**
   * @return StructureMembers object
   */
  public StructureMembers getStructureMembers() {
    return members;
  }

  /**
   * @return List of StructureMembers.Member
   */
  public List<StructureMembers.Member> getMembers() {
    return members.getMembers();
  }

  /**
   * Find a member by its name.
   *
   * @param memberName find member with this name
   * @return StructureMembers.Member matching the name, or null if not found
   */
  public StructureMembers.Member findMember(String memberName) {
    return members.findMember(memberName);
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////


  /**
   * Get member data array of any type as an Array.
   * @param m get data from this StructureMembers.Member.
   * @return Array values.
   */
  abstract public Array getArray(StructureMembers.Member m);

  /**
   * Get  member data array of any type as an Array.
   * For more efficiency, use getScalarXXX(Member) or getJavaArrayXXX(Member) is possible.
   * @param memberName name of member Variable.
   * @return member data array of any type as an Array.
   * @throws IllegalArgumentException if name is not legal member name.
   */
  public Array getArray(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null)
      throw new IllegalArgumentException("illegal member name =" + memberName);
    return getArray(m);
  }

  /**
   * Get member data array of any type as an Object, eg, Float, Double, String etc.
   * @param memberName name of member Variable.
   * @return value as Float, Double, etc..
   */
  public Object getScalarObject(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null)
      throw new IllegalArgumentException("illegal member name =" + memberName);
    return getScalarObject(m);
  }

  /**
   * Get member data array of any type as an Object, eg, Float, Double, String etc.
   * @param m get data from this StructureMembers.Member.
   * @return value as Float, Double, etc..
   */
  public Object getScalarObject( StructureMembers.Member m) {
    DataType dataType = m.getDataType();
    //boolean isScalar = m.isScalar();

    if (dataType == DataType.DOUBLE) {
        return getScalarDouble(m);

    } else if (dataType == DataType.FLOAT) {
      return getScalarFloat(m);

    } else if ((dataType == DataType.BYTE) || (dataType == DataType.ENUM1)) {
      return getScalarByte(m);

    } else if ((dataType == DataType.SHORT) || (dataType == DataType.ENUM2)){
      return getScalarShort(m);

    } else if ((dataType == DataType.INT)|| (dataType == DataType.ENUM4)) {
      return getScalarInt(m);

    } else if (dataType == DataType.LONG) {
      return getScalarLong(m);

    } else if (dataType == DataType.CHAR) {
      return getScalarString( m);

    } else if (dataType == DataType.STRING) {
      return getScalarString( m);

    } else if (dataType == DataType.STRUCTURE) {
      return getScalarStructure( m);

    } else if (dataType == DataType.SEQUENCE) {
      return getArraySequence(m);
    }

    throw new RuntimeException("Dont have implemenation for "+dataType);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////


  /**
   * Get scalar value as a float, with conversion as needed. Underlying type must be convertible to float.
   * @param memberName name of member Variable. Must be convertible to float.
   * @return scalar value as a float
   * @throws ForbiddenConversionException if not convertible to float.
   */
  public float convertScalarFloat(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return convertScalarFloat(m);
  }

  /**
   * Get scalar value as a float, with conversion as needed. Underlying type must be convertible to float.
   * @param m member Variable.
   * @return scalar value as a float
   * @throws ForbiddenConversionException if not convertible to float.
   */
  abstract public float convertScalarFloat(StructureMembers.Member m);

  /**
   * Get scalar value as a double, with conversion as needed. Underlying type must be convertible to double.
   * @param memberName name of member Variable. Must be convertible to double.
   * @return scalar value as a double
   * @throws ForbiddenConversionException if not convertible to double.
   */
  public double convertScalarDouble(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null)
      throw new IllegalArgumentException("illegal member name =" + memberName);
    return convertScalarDouble(m);
  }

  /**
   * Get scalar value as a double, with conversion as needed. Underlying type must be convertible to double.
   * @param m member Variable.
   * @return scalar value as a double
   * @throws ForbiddenConversionException if not convertible to double.
   */
  abstract public double convertScalarDouble(StructureMembers.Member m);

  /**
   * Get scalar value as a int, with conversion as needed. Underlying type must be convertible to int.
   * @param memberName name of member Variable. Must be convertible to double.
   * @return scalar value as a int
   * @throws ForbiddenConversionException if not convertible to int.
   */
  public int convertScalarInt(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return convertScalarInt(m);
  }

  /**
   * Get scalar value as a int, with conversion as needed. Underlying type must be convertible to int.
   * @param m member Variable.
   * @return scalar value as a int
   * @throws ForbiddenConversionException if not convertible to int.
   */
  abstract public int convertScalarInt(StructureMembers.Member m);

  /**
   * Get scalar value as a int, with conversion as needed. Underlying type must be convertible to int.
   * @param memberName name of member Variable. Must be convertible to double.
   * @return scalar value as a int
   * @throws ForbiddenConversionException if not convertible to int.
   */
  public long convertScalarLong(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return convertScalarLong(m);
  }

  /**
   * Get scalar value as a int, with conversion as needed. Underlying type must be convertible to int.
   * @param m member Variable.
   * @return scalar value as a int
   * @throws ForbiddenConversionException if not convertible to int.
   */
  abstract public long convertScalarLong(StructureMembers.Member m);

  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get member data of type double.
   * @param memberName name of member Variable. Must be of type double.
   * @throws IllegalArgumentException if name is not legal member name.
   * @return scalar value as a double
   */
  public double getScalarDouble(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getScalarDouble(m);
  }

  /**
   * Get member data of type double.
   * @param m get data from this StructureMembers.Member. Must be of type double.
   * @return scalar double value
   */
  abstract public double getScalarDouble(StructureMembers.Member m);

  /**
   * Get java double array for a member of type double.
   * @param memberName name of member Variable. Must be of type double.
   * @return 1D java array of doubles
   */
  public double[] getJavaArrayDouble(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getJavaArrayDouble(m);
  }

  /**
   * Get java double array for a member of type double.
   * @param m get data from this StructureMembers.Member. Must be of type double.
   * @return 1D java array of doubles
   */
  abstract public double[] getJavaArrayDouble(StructureMembers.Member m);

  ////////////////

  /**
   * Get member data of type float.
   * @param memberName name of member Variable. Must be of type float.
   * @return scalar float value
   * @throws IllegalArgumentException if name is not legal member name.
   */
  public float getScalarFloat(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getScalarFloat(m);
  }

  /**
   * Get member data of type float.
   * @param m get data from this StructureMembers.Member. Must be of type float.
   * @return scalar double value
   */
  abstract public float getScalarFloat(StructureMembers.Member m);

  /**
   * Get java float array for a member of type float.
   * @param memberName name of member Variable. Must be of type float.
   * @return 1D java array of floats
   */
  public float[] getJavaArrayFloat(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getJavaArrayFloat(m);
  }

  /**
   * Get java float array for a member of type float.
   * @param m get data from this StructureMembers.Member. Must be of type float.
   * @return 1D java array of floats
   */
  abstract public float[] getJavaArrayFloat(StructureMembers.Member m);

  /////

  /**
   * Get member data of type byte.
   * @param memberName name of member Variable. Must be of type byte.
   * @return scalar byte value
   * @throws IllegalArgumentException if name is not legal member name.
   */
  public byte getScalarByte(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getScalarByte(m);
  }

  /**
   * Get member data of type byte.
   * @param m get data from this StructureMembers.Member. Must be of type byte.
   * @return scalar byte value
   */
  abstract public byte getScalarByte(StructureMembers.Member m);

  /**
   * Get java byte array for a member of type byte.
   * @param memberName name of member Variable. Must be of type byte.
   * @return 1D java array of bytes
   */
  public byte[] getJavaArrayByte(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getJavaArrayByte(m);
  }

  /**
   * Get java byte array for a member of type byte.
   * @param m get data from this StructureMembers.Member. Must be of type byte.
   * @return 1D java array of bytes
   */
  abstract public byte[] getJavaArrayByte(StructureMembers.Member m);

  /////
  /**
   * Get member data of type int.
   * @param memberName name of member Variable. Must be of type int.
   * @return scalar int value
   * @throws IllegalArgumentException if name is not legal member name.
   */
  public int getScalarInt(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null)
      throw new IllegalArgumentException("illegal member name =" + memberName);
    return getScalarInt(m);
  }

  /**
   * Get member data of type int.
   * @param m get data from this StructureMembers.Member. Must be of type int.
   * @return scalar int value
   */
  abstract public int getScalarInt(StructureMembers.Member m);

  /**
   * Get java int array for a member of type int.
   * @param memberName name of member Variable. Must be of type int.
   * @return 1D java array of ints
   */
  public int[] getJavaArrayInt(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getJavaArrayInt(m);
  }

  /**
   * Get java int array for a member of type int.
   * @param m get data from this StructureMembers.Member. Must be of type int.
   * @return 1D java array of ints
   */
  abstract public int[] getJavaArrayInt(StructureMembers.Member m);

  /////
  /**
   * Get member data of type short.
   * @param memberName name of member Variable. Must be of type short.
   * @return scalar short value
   * @throws IllegalArgumentException if name is not legal member name.
   */
  public short getScalarShort(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getScalarShort(m);
  }

  /**
   * Get member data of type short.
   * @param m get data from this StructureMembers.Member. Must be of type short.
   * @return scalar short value
   */
  abstract public short getScalarShort(StructureMembers.Member m);

  /**
   * Get java short array for a member of type short.
   * @param memberName name of member Variable. Must be of type short.
   * @return 1D java array of shorts
   */
  public short[] getJavaArrayShort(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getJavaArrayShort(m);
  }

  /**
   * Get java short array for a member of type short.
   * @param m get data from this StructureMembers.Member. Must be of type short.
   * @return 1D java array of shorts
   */
  abstract public short[] getJavaArrayShort(StructureMembers.Member m);

  /////
  /**
   * Get member data of type long.
   * @param memberName name of member Variable. Must be of type long.
  * @return scalar long value
   * @throws IllegalArgumentException if name is not legal member name.
   */
  public long getScalarLong(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getScalarLong(m);
  }

  /**
   * Get member data of type long.
   * @param m get data from this StructureMembers.Member. Must be of type long.
   * @return scalar long value
   */
  abstract public long getScalarLong(StructureMembers.Member m);

  /**
   * Get java long array for a member of type long.
   * @param memberName name of member Variable. Must be of type long.
   * @return 1D java array of longs
   */
  public long[] getJavaArrayLong(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getJavaArrayLong(m);
  }

  /**
   * Get java long array for a member of type long.
   * @param m get data from this StructureMembers.Member. Must be of type long.
   * @return 1D java array of longs
   */
  abstract public long[] getJavaArrayLong(StructureMembers.Member m);

/////
  /**
   * Get member data of type char.
   * @param memberName name of member Variable. Must be of type char.
   * @return scalar char value
   * @throws IllegalArgumentException if name is not legal member name.
   */
  public char getScalarChar(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getScalarChar(m);
  }

  /**
   * Get member data of type char.
   * @param m get data from this StructureMembers.Member. Must be of type char.
   * @return scalar char value
   */
  abstract public char getScalarChar(StructureMembers.Member m);

  /**
   * Get java char array for a member of type char.
   * @param memberName name of member Variable. Must be of type char.
   * @return 1D java array of chars
   */
  public char[] getJavaArrayChar(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getJavaArrayChar(m);
  }

  /**
   * Get java char array for a member of type char.
   * @param m get data from this StructureMembers.Member. Must be of type char.
   * @return 1D java array of chars
   */
  abstract public char[] getJavaArrayChar(StructureMembers.Member m);

  /////

  /**
   * Get String value, from rank 0 String or rank 1 char member array.
   * @param memberName name of member Variable.
   * @return scalar String value
   * @throws IllegalArgumentException if name is not legal member name.
   */
  public String getScalarString(String memberName) {
    StructureMembers.Member m = findMember(memberName);
    if (null == m)
      throw new IllegalArgumentException("Member not found= " + memberName);
    return getScalarString(m);
  }

  /**
   * Get String value, from rank 0 String or rank 1 char member array.
   * @param m get data from this StructureMembers.Member. Must be of type char or String.
   * @return scalar String value
   */
  abstract public String getScalarString(StructureMembers.Member m);

  /**
   * Get java String array for a member of type String.
   * @param memberName name of member Variable. Must be of type char or String.
   * @return 1D java array of String
   */
  public String[] getJavaArrayString(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getJavaArrayString(m);
  }

  /**
   * Get java array of Strings for a member of type char or String.
   * @param m get data from this StructureMembers.Member. Must be of type char or String.
   * @return 1D java array of String
   */
  abstract public String[] getJavaArrayString(StructureMembers.Member m);

  ////

  /**
    * Get member data of type Structure.
    * @param memberName name of member Variable.
    * @return scalar StructureData value
    * @throws IllegalArgumentException if name is not legal member name.
    */
  public StructureData getScalarStructure(String memberName) {
    StructureMembers.Member m = findMember(memberName);
    if (null == m) throw new IllegalArgumentException("Member not found= " + memberName);
    return getScalarStructure(m);
  }

  /**
   * Get member data of type Structure.
   * @param m get data from this StructureMembers.Member. Must be of type Structure.
   * @return StructureData
   */
  abstract public StructureData getScalarStructure(StructureMembers.Member m);

  /**
    * Get member data of type Structure.
    * @param memberName name of member Variable.
    * @return array of StructureData
    * @throws IllegalArgumentException if name is not legal member name.
    */
  public ArrayStructure getArrayStructure(String memberName) {
    StructureMembers.Member m = findMember(memberName);
    if (null == m) throw new IllegalArgumentException("Member not found= " + memberName);
    return getArrayStructure(m);
  }

  /**
   * Get ArrayStructure for a member of type Structure.
   * @param m get data from this StructureMembers.Member. Must be of type Structure.
   * @return ArrayStructure
   */
  abstract public ArrayStructure getArrayStructure(StructureMembers.Member m);

  //////

  /**
   * Get ArraySequence for a member of type Sequence.
   * @param memberName name of member Variable. Must be of type Sequence.
   * @return ArrayStructure
   */
  public ArraySequence getArraySequence(String memberName) {
    StructureMembers.Member m = members.findMember(memberName);
    if (m == null) throw new IllegalArgumentException("illegal member name =" + memberName);
    return getArraySequence(m);
  }

  /**
   * Get ArraySequence for a member of type Sequence.
   * @param m get data from this StructureMembers.Member. Must be of type Sequence.
   * @return ArrayStructure
   */
  abstract public ArraySequence getArraySequence(StructureMembers.Member m);

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // debugging
  public void showInternal(Formatter f, Indent indent) {
    f.format("%sStructureData %s class=%s hash=0x%x%n", indent, members.getName(), this.getClass().getName(), hashCode());
  }

  public void showInternalMembers(Formatter f, Indent indent) {
    f.format("%sStructureData %s class=%s hash=0x%x%n", indent, members.getName(), this.getClass().getName(), hashCode());
    indent.incr();
    for (StructureMembers.Member m : getMembers())
      m.showInternal(f, indent);
    indent.incr();
  }

   public String toString() { 
    return members.toString();
  }

}
