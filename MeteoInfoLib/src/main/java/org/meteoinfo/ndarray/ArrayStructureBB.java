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

import org.meteoinfo.ndarry.constants.CDM;
import org.meteoinfo.util.Indent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Concrete implementation of ArrayStructure, data storage is in a ByteBuffer, which is converted to member data on the fly.
 * In order to use this, the records must have the same size, and the member offset must be the same for each record.
 * Use StructureMembers.setStructureSize() to set the record size.
 * Use StructureMembers.Member.setDataParam() to set the offset of the member from the start of each record.
 * The member data will then be located in the BB at offset = recnum * getStructureSize() + member.getDataParam().
 * This defers object creation for efficiency. Use getArray<type>() and getScalar<type>() data accessors if possible.
 * <pre>
     Structure pdata = (Structure) ncfile.findVariable( name);
     StructureMembers members = pdata.makeStructureMembers();
     members.findMember("value").setDataParam(0); // these are the offsets into the record
     members.findMember("x_start").setDataParam(2);
     members.findMember("y_start").setDataParam(4);
     members.findMember("direction").setDataParam(6);
     members.findMember("speed").setDataParam(8);
     int recsize = pos[1] - pos[0]; // each record  must be all the same size
     members.setStructureSize( recsize);
     ArrayStructureBB asbb = new ArrayStructureBB( members, new int[] { size}, bos, pos[0]);
 * </pre>
 * For String members, you must store the Strings in the stringHeap. An integer index into the heap is used in the ByteBuffer.
 * @author caron
 * @see Array
 */
public class ArrayStructureBB extends ArrayStructureBak {

  /**
   * Set the offsets, based on m.getSizeBytes().
   * Also sets members.setStructureSize().
   * @param members set offsets for these members
   * @return the total size
   */
  public static int setOffsets(StructureMembers members) {
    int offset = 0;
    for (StructureMembers.Member m : members.getMembers()) {
      m.setDataParam(offset);
      offset += m.getSizeBytes();

      // set inner offsets (starts again at 0)
      if (m.getStructureMembers() != null)
        setOffsets(m.getStructureMembers());
    }
    members.setStructureSize(offset);
    return offset;
  }

  public static int showOffsets(StructureMembers members, Indent indent, Formatter f) {
    int offset = 0;
    for (StructureMembers.Member m : members.getMembers()) {
      f.format("%s%s offset=%d (%d %s = %d bytes)%n", indent, m.getName(), m.getDataParam(), m.getSize(), m.getDataType(), m.getSizeBytes());

      if (m.getStructureMembers() != null) {
        indent.incr();
        StructureMembers nested = m.getStructureMembers();
        f.format("%n%s%s == %d bytes%n", indent, nested.getName(), nested.getStructureSize());
        showOffsets(nested, indent, f);
        indent.decr();
      }
    }
    return offset;
  }

  /////////////////////////////////////////////////////

  protected ByteBuffer bbuffer;
  protected int bb_offset = 0;

  /**
   * Create a new Array of type StructureData and the given members and shape.
   * Generally, you extract the byte array and fill it: <pre>
     byte [] result = (byte []) structureArray.getStorage(); </pre>
   *
   * @param members a description of the structure members
   * @param shape   the shape of the Array.
   */
  public ArrayStructureBB(StructureMembers members, int[] shape) {
    super(members, shape);
    this.bbuffer = ByteBuffer.allocate(nelems * getStructureSize());
    bbuffer.order(ByteOrder.BIG_ENDIAN);
  }

  /**
   * Construct an ArrayStructureBB with the given ByteBuffer.
   *
   * @param members the list of structure members.
   * @param shape   the shape of the structure array
   * @param bbuffer the data is stored in this ByteBuffer. bbuffer.order must already be set.
   * @param offset  offset from the start of the ByteBufffer to the first record.
   */
  public ArrayStructureBB(StructureMembers members, int[] shape, ByteBuffer bbuffer, int offset) {
    super(members, shape);
    this.bbuffer = bbuffer;
    this.bb_offset = offset;
  }

  @Override
  protected StructureData makeStructureData(ArrayStructureBak as, int index) {
    return new StructureDataA(as, index);
  }

  /**
   * Return backing storage as a ByteBuffer
   * @return backing storage as a ByteBuffer
   */
  public ByteBuffer getByteBuffer() {
    return bbuffer;
  }

  @Override
  public double getScalarDouble(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.DOUBLE) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be double");
    if (m.getDataArray() != null) return super.getScalarDouble(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    return bbuffer.getDouble(offset);
  }

  @Override
  public double[] getJavaArrayDouble(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.DOUBLE) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be double");
    if (m.getDataArray() != null) return super.getJavaArrayDouble(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    double[] pa = new double[count];
    for (int i = 0; i < count; i++)
      pa[i] = bbuffer.getDouble(offset + i * 8);
    return pa;
  }

  @Override
  protected void copyDoubles(int recnum, StructureMembers.Member m, IndexIterator result) {
    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    for (int i = 0; i < count; i++)
      result.setDoubleNext( bbuffer.getDouble(offset + i * 8));
  }

  @Override
  public float getScalarFloat(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.FLOAT) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be float");
    if (m.getDataArray() != null) return super.getScalarFloat(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    return bbuffer.getFloat(offset);
  }

  @Override
  public Array getArray(int recnum, StructureMembers.Member m) {
    if (m.isVariableLength()) {
      int offset = calcOffsetSetOrder(recnum, m);
      int heapIndex = bbuffer.getInt(offset);
      return (Array) heap.get( heapIndex);
    }

    return super.getArray(recnum, m);
  }

  @Override
  public float[] getJavaArrayFloat(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.FLOAT) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be float");
    if (m.getDataArray() != null) return super.getJavaArrayFloat(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    float[] pa = new float[count];
    for (int i = 0; i < count; i++)
      pa[i] = bbuffer.getFloat(offset + i * 4);
    return pa;
  }

  @Override
  protected void copyFloats(int recnum, StructureMembers.Member m, IndexIterator result) {
    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    for (int i = 0; i < count; i++)
      result.setFloatNext( bbuffer.getFloat(offset + i * 4));
  }

  @Override
  public byte getScalarByte(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.BYTE) && (m.getDataType() != DataType.ENUM1)) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be byte");
    if (m.getDataArray() != null) return super.getScalarByte(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    return bbuffer.get(offset);
  }

  @Override
  public byte[] getJavaArrayByte(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.BYTE)  && (m.getDataType() != DataType.ENUM1)) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be byte");
    if (m.getDataArray() != null) return super.getJavaArrayByte(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    byte[] pa = new byte[count];
    for (int i = 0; i < count; i++)
      pa[i] = bbuffer.get(offset + i);
    return pa;
  }

  @Override
  protected void copyBytes(int recnum, StructureMembers.Member m, IndexIterator result) {
    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    for (int i = 0; i < count; i++)
      result.setByteNext( bbuffer.get(offset + i));
  }

  @Override
  public short getScalarShort(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.SHORT) && (m.getDataType() != DataType.ENUM2)) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be short");
    if (m.getDataArray() != null) return super.getScalarShort(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    return bbuffer.getShort(offset);
  }

  @Override
  public short[] getJavaArrayShort(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.SHORT)  && (m.getDataType() != DataType.ENUM2)) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be short");
    if (m.getDataArray() != null) return super.getJavaArrayShort(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    short[] pa = new short[count];
    for (int i = 0; i < count; i++)
      pa[i] = bbuffer.getShort(offset + i * 2);
    return pa;
  }

  @Override
  protected void copyShorts(int recnum, StructureMembers.Member m, IndexIterator result) {
    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    for (int i = 0; i < count; i++)
      result.setShortNext(  bbuffer.getShort(offset + i * 2));
  }

  @Override
  public int getScalarInt(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.INT) && (m.getDataType() != DataType.ENUM4)) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be int");
    if (m.getDataArray() != null) return super.getScalarInt(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    return bbuffer.getInt(offset);
  }

  @Override
  public int[] getJavaArrayInt(int recnum, StructureMembers.Member m) {
    if ((m.getDataType() != DataType.INT) && (m.getDataType() != DataType.ENUM4)) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be int");
    if (m.getDataArray() != null) return super.getJavaArrayInt(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    int[] pa = new int[count];
    for (int i = 0; i < count; i++)
      pa[i] = bbuffer.getInt(offset + i * 4);
    return pa;
  }

  @Override
  protected void copyInts(int recnum, StructureMembers.Member m, IndexIterator result) {
    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    for (int i = 0; i < count; i++)
      result.setIntNext(  bbuffer.getInt(offset + i * 4));
  }

  @Override
  public long getScalarLong(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.LONG) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be long");
    if (m.getDataArray() != null) return super.getScalarLong(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    return bbuffer.getLong(offset);
  }

  @Override
  public long[] getJavaArrayLong(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.LONG) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be long");
    if (m.getDataArray() != null) return super.getJavaArrayLong(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    long[] pa = new long[count];
    for (int i = 0; i < count; i++)
      pa[i] = bbuffer.getLong(offset + i * 8);
    return pa;
  }

  @Override
  protected void copyLongs(int recnum, StructureMembers.Member m, IndexIterator result) {
    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    for (int i = 0; i < count; i++)
      result.setLongNext(  bbuffer.getLong(offset + i * 8));
  }

  @Override
  public char getScalarChar(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.CHAR) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be char");
    if (m.getDataArray() != null) return super.getScalarChar(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    return (char) bbuffer.get(offset);
  }

  @Override
  public char[] getJavaArrayChar(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.CHAR) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be char");
    if (m.getDataArray() != null) return super.getJavaArrayChar(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    char[] pa = new char[count];
    for (int i = 0; i < count; i++)
      pa[i] = (char) bbuffer.get(offset + i);
    return pa;
  }

  @Override
  protected void copyChars(int recnum, StructureMembers.Member m, IndexIterator result) {
    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    for (int i = 0; i < count; i++)
      result.setCharNext(  (char) bbuffer.get(offset + i));
  }

  @Override
  public String getScalarString(int recnum, StructureMembers.Member m) {
    if (m.getDataArray() != null) return super.getScalarString(recnum, m);

    // strings are stored on the "heap", and the index to the heap is kept in the bbuffer
    if (m.getDataType() == DataType.STRING) {
      int offset = calcOffsetSetOrder(recnum, m);
      int index = bbuffer.getInt(offset);
      Object data = heap.get(index);
      if (data instanceof String) return (String) data;
      return ((String[]) data)[0];
    }

    if (m.getDataType() == DataType.CHAR) {
      int offset = calcOffsetSetOrder(recnum, m);
      int count = m.getSize();
      byte[] pa = new byte[count];
      int i;
      for (i = 0; i < count; i++) {
        pa[i] = bbuffer.get(offset + i);
        if (0 == pa[i]) break;
      }
      return new String(pa, 0, i, CDM.utf8Charset);
    }

    throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be String or char");
  }

  @Override
  public String[] getJavaArrayString(int recnum, StructureMembers.Member m) {
    if (m.getDataArray() != null) return super.getJavaArrayString(recnum, m);

    // strings are stored on the "heap", and the index to the heap is kept in the bbuffer
    if (m.getDataType() == DataType.STRING) {
      int offset = calcOffsetSetOrder(recnum, m);
      int heapIndex = bbuffer.getInt(offset);
      Object ho = heap.get( heapIndex);
      if (ho instanceof String[])
        return (String[]) ho;
      else if (ho instanceof String) {
        String[] result = new String[1];
        result[0] = (String) ho;
        return result;
      } else {
        throw new IllegalArgumentException("Expected a String, but found an object of type  " + ho.getClass().getName() + ", on heap for "+
          " member "+m.getName());
      }
    }

    if (m.getDataType() == DataType.CHAR) {
      int[] shape = m.getShape();
      int rank = shape.length;
      if (rank < 2) {
        String[] result = new String[1];
        result[0] = getScalarString(recnum, m);
        return result;
      }

      int strlen = shape[rank - 1];
      int n = m.getSize() / strlen;
      int offset = calcOffsetSetOrder(recnum, m);
      String[] result = new String[n];
      for (int i = 0; i < n; i++) {
        byte[] bytes = new byte[strlen];
        for (int j = 0; j < bytes.length; j++)
          bytes[j] = bbuffer.get(offset + i * strlen + j);
        result[i] = new String(bytes, CDM.utf8Charset);
      }
      return result;
    }

    throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be char");
  }

  @Override
  protected void copyObjects(int recnum, StructureMembers.Member m, IndexIterator result) {
    int offset = calcOffsetSetOrder(recnum, m);
    int count = m.getSize();
    int index = bbuffer.getInt(offset);
    String[] data = (String[]) heap.get(index);

    for (int i = 0; i < count; i++)
      result.setObjectNext(  data[i]);
  }

  // LOOK not tested ??
  @Override
  public StructureData getScalarStructure(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.STRUCTURE) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Structure");
    if (m.getDataArray() != null) return super.getScalarStructure(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    ArrayStructureBB subset = new ArrayStructureBB(m.getStructureMembers(), new int[]{1}, this.bbuffer, offset);

    return new StructureDataA(subset, 0);
  }

  @Override
  public ArrayStructureBak getArrayStructure(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.STRUCTURE) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Structure");
    if (m.getDataArray() != null) return super.getArrayStructure(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    ArrayStructureBB result = new ArrayStructureBB(m.getStructureMembers(), m.getShape(), this.bbuffer, offset);
    result.heap = this.heap; // share the heap
    return result;
  }

  @Override
  public ArraySequence getArraySequence(int recnum, StructureMembers.Member m) {
    if (m.getDataType() != DataType.SEQUENCE) throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Sequence");
    //if (m.getDataArray() != null) return super.getArrayStructure(recnum, m);

    int offset = calcOffsetSetOrder(recnum, m);
    int index = bbuffer.getInt(offset);
    if (heap == null) {
      System.out.println("ArrayStructureBB null heap");
      return null;
    }
    Object ho = heap.get(index);
    if (ho instanceof ArraySequence)
      return (ArraySequence) heap.get(index);
    return null; // LOOK
  }

  /* from the recnum-th structure, copy the member data into result. 
  // member data is itself a structure, and may be an array of structures.
  @Override
  protected void copyStructures(int recnum, StructureMembers.Member m, IndexIterator result) {
    ArrayStructure data = getArrayStructure( recnum, m);
    Array data = getArray(recnum, m);
    IndexIterator dataIter = data.getIndexIterator();
    while (dataIter.hasNext())
      result.setObjectNext( dataIter.getObjectNext());

    int count = m.getSize();
    for (int i = 0; i < count; i++)
      result.setObjectNext(  makeStructureData(this, recnum));
  } */

  protected int calcOffsetSetOrder(int recnum, StructureMembers.Member m) {
    if (null != m.getDataObject())
      bbuffer.order( (ByteOrder) m.getDataObject());
    return bb_offset + recnum * getStructureSize() + m.getDataParam();
  }

  /*  int index = asbb.addObjectToHeap(s);
      bb.order( ByteOrder.nativeOrder()); // the string index is always written in "native order"
      bb.putInt(destPos + i * 4, index); // overwrite with the index into the StringHeap
  */
  private List<Object> heap;
  public int addObjectToHeap(Object s) {
    if (null == heap) heap = new ArrayList<>();
    heap.add(s);
    return heap.size() - 1;
  }

  public void addObjectToHeap(int recnum, StructureMembers.Member m, Object s) {
    if (null == heap) heap = new ArrayList<>();
    heap.add(s);
    int index = heap.size() - 1;
    // was  setInt(calcOffsetSetOrder(recnum, m), index) jc 12/4/2012
    bbuffer.putInt(calcOffsetSetOrder(recnum, m), index);
  }

  /**
   * DO NOT MODIFY
   * @return heap
   */
  public List<Object> getHeap() {
    return heap;
  }

  @Override
  public void showInternal(Formatter f, Indent indent) {
    super.showInternal(f, indent);

    f.format("%sByteBuffer = %s (hash=0x%x)%n", indent,  bbuffer, bbuffer.hashCode());
    if (null != heap) {
      f.format("%s  Heap Objects%n", indent);
      for (int i=0; i<heap.size(); i++) {
        Object o =  heap.get(i);
        f.format("%s   %d class=%s hash=0x%x = %s%n", indent, i, o.getClass().getName(), o.hashCode(), o);
        if (o instanceof ArrayStructureBak) {
          ((ArrayStructureBak) o).showInternal(f, indent.incr());
          indent.decr();
        }
      }
      f.format("%n");
    }

  }

  ////////////////////////////////////////////////////////////////////////
  // debugging
  public static void main(String argv[]) {
    byte[] ba = new byte[20];
    for (int i = 0; i < ba.length; ++i)
      ba[i] = (byte) i;

    ByteBuffer bbw = ByteBuffer.wrap(ba, 5, 15);
    bbw.get(0);
    System.out.println(" bbw(0)=" + bbw.get(0) + " i would expect = 5");

    bbw.position(5);
    System.out.println(" bbw(0)=" + bbw.get(0) + " i would expect = 4");
  }

}