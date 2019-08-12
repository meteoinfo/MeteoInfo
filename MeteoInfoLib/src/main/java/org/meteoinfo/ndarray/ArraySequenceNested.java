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
 * Handles nested sequences: a 1D array of variable length 1D arrays of StructureData.
 * Uses same technique as ArrayStructureMA for the inner fields; data storage is in member arrays.
 * Used only by opendap internals.
 *
 * Example use:
 * <pre>
    ArraySequence aseq = new ArraySequence( members, outerLength);
    for (int seq=0; seq < outerLength; seq++) {
      aseq.setSequenceLength(seq, seqLength);
    }
    aseq.finish();
 </pre>
 *
 * @author caron
 */
public class ArraySequenceNested extends ArrayStructure {
  private int[] sequenceLen;
  private int[] sequenceOffset;
  private int total = 0;

  /**
   * This is used for inner sequences, ie variable length structures nested inside of another structure.
   * @param members the members of the STructure
   * @param nseq the number of sequences, ie the length of the outer structure.
   */
  public ArraySequenceNested(StructureMembers members, int nseq) {
    super(members, new int[] {nseq});
    sequenceLen = new int[nseq];
  }

  // not sure how this is used
  protected StructureData makeStructureData( ArrayStructure as, int index) {
    return new StructureDataA( as, index);
  }

  public StructureData getStructureData(int index) {
    return new StructureDataA( this, index);
  }

  /**
   * Set the length of one of the sequences.
   * @param outerIndex which sequence?
   * @param len what is its length?
   */
  public void setSequenceLength( int outerIndex, int len) {
    sequenceLen[outerIndex] = len;
  }

  /**
   * Get the length of the ith sequence.
   * @param outerIndex which sequence?
   * @return its length
   */
  public int getSequenceLength( int outerIndex) {
    return sequenceLen[outerIndex];
  }

  /**
   * Get the the starting index of the ith sequence.
   * @param outerIndex which sequence?
   * @return its starting index
   */
  public int getSequenceOffset( int outerIndex) {
    return sequenceOffset[outerIndex];
  }

  /**
   * Call this when you have set all the sequence lengths.
   */
  public void finish() {
    sequenceOffset = new int[nelems];

    total = 0;
    for (int i=0; i<nelems; i++) {
      sequenceOffset[i] = total;
      total += sequenceLen[i];
    }

    sdata = new StructureData[nelems];
    for (int i=0; i<nelems; i++)
      sdata[i] = new StructureDataA( this, sequenceOffset[i]);

    // make the member arrays
    for (StructureMembers.Member m : members.getMembers()) {
      int[] mShape = m.getShape();
      int[] shape = new int[mShape.length + 1];
      shape[0] = total;
      System.arraycopy(mShape, 0, shape, 1, mShape.length);

      // LOOK not doing nested structures
      Array data = Array.factory(m.getDataType(), shape);
      m.setDataArray(data);
    }
  }

  /**
   * @return the total number of Structures over all the nested sequences.
   */
  public int getTotalNumberOfStructures() { return total; }

  /**
   * Flatten the Structures into a 1D array of Structures of length getTotalNumberOfStructures().
   * @return Array of Structures
   */
  public ArrayStructure flatten() {
    ArrayStructureW aw = new ArrayStructureW( getStructureMembers(), new int[] {total});
    for (int i=0; i<total; i++) {
      StructureData sdata = new StructureDataA( this, i);
      aw.setStructureData(sdata, i);
    }
    return aw;
  }

  public double getScalarDouble(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.DOUBLE) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be double");
    Array data = m.getDataArray();
    return data.getDouble( recnum * m.getSize()); // gets first one in the array
  }

  public double[] getJavaArrayDouble(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.DOUBLE) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be double");
    int count = m.getSize();
    Array data = m.getDataArray();
    double[] pa = new double[count];
    for (int i=0; i<count; i++)
      pa[i] = data.getDouble( recnum * count + i);
    return pa;
  }

  public float getScalarFloat(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.FLOAT) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be float");
    Array data = m.getDataArray();
    return data.getFloat( recnum * m.getSize()); // gets first one in the array
  }

  public float[] getJavaArrayFloat(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.FLOAT) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be float");
    int count = m.getSize();
    Array data = m.getDataArray();
    float[] pa = new float[count];
    for (int i=0; i<count; i++)
      pa[i] = data.getFloat( recnum * count + i);
    return pa;
  }

  public byte getScalarByte(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.BYTE) && (m.getDataType() != DataType.ENUM1)) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be byte");
    Array data = m.getDataArray();
    return data.getByte( recnum * m.getSize()); // gets first one in the array
  }

  public byte[] getJavaArrayByte(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.BYTE) && (m.getDataType() != DataType.ENUM1)) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be byte");
    int count = m.getSize();
    Array data = m.getDataArray();
    byte[] pa = new byte[count];
    for (int i=0; i<count; i++)
      pa[i] = data.getByte( recnum * count + i);
    return pa;
  }

  public short getScalarShort(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.SHORT) && (m.getDataType() != DataType.ENUM2)) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be short");
    Array data = m.getDataArray();
    return data.getShort( recnum * m.getSize()); // gets first one in the array
  }

  public short[] getJavaArrayShort(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.SHORT) && (m.getDataType() != DataType.ENUM2)) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be short");
    int count = m.getSize();
    Array data = m.getDataArray();
    short[] pa = new short[count];
    for (int i=0; i<count; i++)
      pa[i] = data.getShort( recnum * count + i);
    return pa;
  }

  public int getScalarInt(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.INT) && (m.getDataType() != DataType.ENUM4)) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be int");
    Array data = m.getDataArray();
    return data.getInt( recnum * m.getSize()); // gets first one in the array
  }

  public int[] getJavaArrayInt(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.INT) && (m.getDataType() != DataType.ENUM4)) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be int");
    int count = m.getSize();
    Array data = m.getDataArray();
    int[] pa = new int[count];
    for (int i=0; i<count; i++)
      pa[i] = data.getInt( recnum * count + i);
    return pa;
  }

  public long getScalarLong(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.LONG) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be long");
    Array data = m.getDataArray();
    return data.getLong( recnum * m.getSize()); // gets first one in the array
  }

  public long[] getJavaArrayLong(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.LONG) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be long");
    int count = m.getSize();
    Array data = m.getDataArray();
    long[] pa = new long[count];
    for (int i=0; i<count; i++)
      pa[i] = data.getLong( recnum * count + i);
    return pa;
  }

  public char getScalarChar(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.CHAR) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be char");
    Array data = m.getDataArray();
    return data.getChar( recnum * m.getSize()); // gets first one in the array
  }

  public char[] getJavaArrayChar(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.CHAR) throw new IllegalArgumentException("Type is "+m.getDataType()+", must be char");
    int count = m.getSize();
    Array data = m.getDataArray();
    char[] pa = new char[count];
    for (int i=0; i<count; i++)
      pa[i] = data.getChar( recnum * count + i);
    return pa;
  }

  public String getScalarString(int recnum, StructureMembers.Member m) {
    if (m.getDataType() == DataType.CHAR) {
      ArrayChar data = (ArrayChar) m.getDataArray();
      return data.getString( recnum);
    }

    if (m.getDataType() == DataType.STRING) {
      ArrayObject data = (ArrayObject) m.getDataArray();
      return (String) data.getObject( recnum);
    }

    throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be String or char");
  }

  public String[] getJavaArrayString(int recnum, StructureMembers.Member m) {
    int n = m.getSize();
    String[] result = new String[n];

    if (m.getDataType() == DataType.CHAR) {

      ArrayChar data = (ArrayChar) m.getDataArray();
      for (int i=0; i<n; i++)
        result[i] = data.getString( recnum * n + i);
      return result;

    } else if (m.getDataType() == DataType.STRING) {

      Array data = m.getDataArray();
      for (int i=0; i<n; i++)
        result[i] = (String) data.getObject( recnum * n + i);
      return result;
    }

    throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be String or char");
  }

  public StructureData getScalarStructure(int recnum, StructureMembers.Member m) {
    if (m.getDataType() == DataType.STRUCTURE) {
      ArrayStructure data = (ArrayStructure) m.getDataArray();
      return data.getStructureData( recnum * m.getSize());  // gets first in the array
    }

    throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Structure");
  }

  public ArrayStructure getArrayStructure(int recnum, StructureMembers.Member m) {
    if (m.getDataType() == DataType.STRUCTURE) {
      ArrayStructure data = (ArrayStructure) m.getDataArray();
      // we need to subset this array structure to deal with just the subset for this recno
      // use "brute force" for now, see if we can finesse later
      int count = m.getSize();
      StructureData[] sdata = new StructureData[count];
      for (int i=0; i<count; i++)
        sdata[i] = data.getStructureData( recnum * count + i);

      return new ArrayStructureW( data.getStructureMembers(), m.getShape(), sdata);
    }

    throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Structure");
  }

}