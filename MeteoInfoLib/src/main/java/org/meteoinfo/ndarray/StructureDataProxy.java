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
 * Proxy for another StructureData.
 * Does nothing, is intended to be overridden.
 *
 * @author caron
 * @since 8/20/13
 */
public class StructureDataProxy extends StructureData {
  protected StructureData org;

  protected StructureDataProxy( StructureData org) {
    super(org.getStructureMembers());
    this.org = org;
  }

  public StructureDataProxy( StructureMembers members, StructureData org) {
    super(members);
    this.org = org;
  }

  public StructureData getOriginalStructureData() {
    return org;
  }

  public Array getArray(StructureMembers.Member m) {
    return org.getArray(m.getName());
  }

  public float convertScalarFloat(StructureMembers.Member m) {
    return org.convertScalarFloat(m.getName());
  }

  public double convertScalarDouble(StructureMembers.Member m) {
    return org.convertScalarDouble(m.getName());
  }

  public int convertScalarInt(StructureMembers.Member m) {
    return org.convertScalarInt(m.getName());
  }

  public long convertScalarLong(StructureMembers.Member m) {
    return org.convertScalarLong(m.getName());
  }

  public double getScalarDouble(StructureMembers.Member m) {
    return org.getScalarDouble(m.getName());
  }

  public double[] getJavaArrayDouble(StructureMembers.Member m) {
    return org.getJavaArrayDouble(m.getName());
  }

  public float getScalarFloat(StructureMembers.Member m) {
    return org.getScalarFloat(m.getName());
  }

  public float[] getJavaArrayFloat(StructureMembers.Member m) {
    return org.getJavaArrayFloat(m.getName());
  }

  public byte getScalarByte(StructureMembers.Member m) {
    return org.getScalarByte(m.getName());
  }

  public byte[] getJavaArrayByte(StructureMembers.Member m) {
    return org.getJavaArrayByte(m.getName());
  }

  public int getScalarInt(StructureMembers.Member m) {
    return org.getScalarInt(m.getName());
  }

  public int[] getJavaArrayInt(StructureMembers.Member m) {
    return org.getJavaArrayInt(m.getName());
  }

  public short getScalarShort(StructureMembers.Member m) {
    return org.getScalarShort(m.getName());
  }

  public short[] getJavaArrayShort(StructureMembers.Member m) {
    return org.getJavaArrayShort(m.getName());
  }

  public long getScalarLong(StructureMembers.Member m) {
    return org.getScalarLong(m.getName());
  }

  public long[] getJavaArrayLong(StructureMembers.Member m) {
    return org.getJavaArrayLong(m.getName());
  }

  public char getScalarChar(StructureMembers.Member m) {
    return org.getScalarChar(m.getName());
  }

  public char[] getJavaArrayChar(StructureMembers.Member m) {
    return org.getJavaArrayChar(m.getName());
  }

  public String getScalarString(StructureMembers.Member m) {
    return org.getScalarString(m.getName());
  }

  public String[] getJavaArrayString(StructureMembers.Member m) {
    return org.getJavaArrayString(m.getName());
  }

  public StructureData getScalarStructure(StructureMembers.Member m) {
    return org.getScalarStructure(m.getName());
  }

  public ArrayStructureBak getArrayStructure(StructureMembers.Member m) {
    return org.getArrayStructure(m.getName());
  }

  public ArraySequence getArraySequence(StructureMembers.Member m) {
    return org.getArraySequence(m.getName());
  }

  public Object getScalarObject( StructureMembers.Member m) {
    return org.getScalarObject(m.getName());
  }
}
