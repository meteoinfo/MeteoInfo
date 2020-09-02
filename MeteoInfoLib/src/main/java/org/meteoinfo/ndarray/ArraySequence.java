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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;

/**
 * ArraySequence is the way to contain the data for a Sequence, using a StructureDataIterator.
 * A Sequence is a one-dimensional Structure with indeterminate length.
 * The only data access is through getStructureIterator().
 * So an ArraySequence is really a wrapper around a StructureDataIterator, adapting it to an Array.
 *
 * @author caron
 * @since Feb 27, 2008
 */
public class ArraySequence extends ArrayStructureBak {
  protected StructureDataIterator iter; // never use private

  protected ArraySequence(StructureMembers sm, int[] shape) {
    super(sm, shape);
  }

  /**
   * Constructor
   *
   * @param members the members
   * @param iter    the iterator
   * @param nelems  iterator count, may be missing (<0)
   */
  public ArraySequence(StructureMembers members, StructureDataIterator iter, int nelems) {
    super(members, new int[]{0});
    this.iter = iter;
    this.nelems = nelems;
  }

  /**
   * n
   *
   * @return StructureDataIterator.class
   */
  @Override
  public Class getElementType() {
    return StructureDataIterator.class;
  }

  @Override
  public StructureDataIterator getStructureDataIterator() { // throws java.io.IOException {
    iter = iter.reset();
    return iter;
  }

  public int getStructureDataCount() {
    return nelems;
  }

  @Override
  public long getSizeBytes() {
    return nelems * members.getStructureSize(); // LOOK we may not know the count ???
  }

  @Override
  protected StructureData makeStructureData(ArrayStructureBak as, int index) {
    throw new UnsupportedOperationException("Cannot subset a Sequence");
  }

  @Override
  public Array extractMemberArray(StructureMembers.Member proxym) throws IOException {
    if (proxym.getDataArray() != null)
      return proxym.getDataArray();

    DataType dataType = proxym.getDataType();
    boolean isScalar = (proxym.getSize() == 1) || (dataType == DataType.SEQUENCE);

    // combine the shapes
    int[] mshape = proxym.getShape();
    int rrank = 1 + mshape.length;
    int[] rshape = new int[rrank];
    rshape[0] = nelems;
    System.arraycopy(mshape, 0, rshape, 1, mshape.length);

    if (nelems < 0)
      return extractMemberArrayFromIteration(proxym, rshape);

    // create an empty array to hold the result
    Array result;
    if (dataType == DataType.STRUCTURE) {
      StructureMembers membersw = new StructureMembers(proxym.getStructureMembers()); // no data arrays get propagated
      result = new ArrayStructureW(membersw, rshape);
    } else {
      result = Array.factory(dataType.getPrimitiveClassType(), rshape);
    }

    StructureDataIterator sdataIter = getStructureDataIterator();
    IndexIterator resultIter = result.getIndexIterator();
    try {
      while (sdataIter.hasNext()) {
        StructureData sdata = sdataIter.next();
        StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());

        if (isScalar) {
          if (dataType == DataType.DOUBLE)
            resultIter.setDoubleNext(sdata.getScalarDouble(realm));

          else if (dataType == DataType.FLOAT)
            resultIter.setFloatNext(sdata.getScalarFloat(realm));

          else if ((dataType == DataType.BYTE) || (dataType == DataType.ENUM1))
            resultIter.setByteNext(sdata.getScalarByte(realm));

          else if ((dataType == DataType.SHORT) || (dataType == DataType.ENUM2))
            resultIter.setShortNext(sdata.getScalarShort(realm));

          else if ((dataType == DataType.INT) || (dataType == DataType.ENUM4))
            resultIter.setIntNext(sdata.getScalarInt(realm));

          else if (dataType == DataType.LONG)
            resultIter.setLongNext(sdata.getScalarLong(realm));

          else if (dataType == DataType.CHAR)
            resultIter.setCharNext(sdata.getScalarChar(realm));

          else if (dataType == DataType.STRING)
            resultIter.setObjectNext(sdata.getScalarString(realm));

          else if (dataType == DataType.STRUCTURE)
            resultIter.setObjectNext(sdata.getScalarStructure(realm));

          else if (dataType == DataType.SEQUENCE)
            resultIter.setObjectNext(sdata.getArraySequence(realm));

        } else {
          if (dataType == DataType.DOUBLE) {
            double[] data = sdata.getJavaArrayDouble(realm);
            for (double aData : data) resultIter.setDoubleNext(aData);

          } else if (dataType == DataType.FLOAT) {
            float[] data = sdata.getJavaArrayFloat(realm);
            for (float aData : data) resultIter.setFloatNext(aData);

          } else if ((dataType == DataType.BYTE) || (dataType == DataType.ENUM1)) {
            byte[] data = sdata.getJavaArrayByte(realm);
            for (byte aData : data) resultIter.setByteNext(aData);

          } else if ((dataType == DataType.SHORT) || (dataType == DataType.ENUM2)) {
            short[] data = sdata.getJavaArrayShort(realm);
            for (short aData : data) resultIter.setShortNext(aData);

          } else if ((dataType == DataType.INT) || (dataType == DataType.ENUM4)) {
            int[] data = sdata.getJavaArrayInt(realm);
            for (int aData : data) resultIter.setIntNext(aData);

          } else if (dataType == DataType.LONG) {
            long[] data = sdata.getJavaArrayLong(realm);
            for (long aData : data) resultIter.setLongNext(aData);

          } else if (dataType == DataType.CHAR) {
            char[] data = sdata.getJavaArrayChar(realm);
            for (char aData : data) resultIter.setCharNext(aData);

          } else if (dataType == DataType.STRING) {
            String[] data = sdata.getJavaArrayString(realm);
            for (String aData : data) resultIter.setObjectNext(aData);

          } else if (dataType == DataType.STRUCTURE) {
            ArrayStructureBak as = sdata.getArrayStructure(realm);
            StructureDataIterator innerIter = as.getStructureDataIterator();
            while (innerIter.hasNext())
              resultIter.setObjectNext(innerIter.next());
          }

          // LOOK SEQUENCE, OPAQUE ??
        }
      }
    } finally {
      sdataIter.finish();
    }


    return result;
  }

  // when we dont know how many in the iteration

  private Array extractMemberArrayFromIteration(StructureMembers.Member proxym, int[] rshape) throws IOException {
    DataType dataType = proxym.getDataType();
    StructureDataIterator sdataIter = getStructureDataIterator();
    Object dataArray = null;
    int count = 0;
    int initial = 1000;

    try {
      if (dataType == DataType.DOUBLE) {
        ArrayList<Double> result = new ArrayList<Double>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          double[] data = sdata.getJavaArrayDouble(realm);
          for (double aData : data) result.add(aData);
          count++;
        }
        double[] da = new double[result.size()];
        int i = 0;
        for (Double d : result) da[i++] = d;
        dataArray = da;

      } else if (dataType == DataType.FLOAT) {
        ArrayList<Float> result = new ArrayList<Float>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          float[] data = sdata.getJavaArrayFloat(realm);
          for (float aData : data) result.add(aData);
          count++;
        }
        float[] da = new float[result.size()];
        int i = 0;
        for (Float d : result) da[i++] = d;
        dataArray = da;

      } else if ((dataType == DataType.BYTE) || (dataType == DataType.ENUM1)) {
        ArrayList<Byte> result = new ArrayList<Byte>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          byte[] data = sdata.getJavaArrayByte(realm);
          for (byte aData : data) result.add(aData);
          count++;
        }
        byte[] da = new byte[result.size()];
        int i = 0;
        for (Byte d : result) da[i++] = d;
        dataArray = da;

      } else if ((dataType == DataType.SHORT) || (dataType == DataType.ENUM2)) {
        ArrayList<Short> result = new ArrayList<Short>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          short[] data = sdata.getJavaArrayShort(realm);
          for (short aData : data) result.add(aData);
          count++;
        }
        short[] da = new short[result.size()];
        int i = 0;
        for (Short d : result) da[i++] = d;
        dataArray = da;

      } else if ((dataType == DataType.INT) || (dataType == DataType.ENUM4)) {
        ArrayList<Integer> result = new ArrayList<Integer>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          int[] data = sdata.getJavaArrayInt(realm);
          for (int aData : data) result.add(aData);
          count++;
        }
        int[] da = new int[result.size()];
        int i = 0;
        for (Integer d : result) da[i++] = d;
        dataArray = da;

      } else if (dataType == DataType.LONG) {
        ArrayList<Long> result = new ArrayList<Long>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          long[] data = sdata.getJavaArrayLong(realm);
          for (long aData : data) result.add(aData);
          count++;
        }
        long[] da = new long[result.size()];
        int i = 0;
        for (Long d : result) da[i++] = d;
        dataArray = da;

      } else if (dataType == DataType.CHAR) {
        ArrayList<Character> result = new ArrayList<Character>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          char[] data = sdata.getJavaArrayChar(realm);
          for (char aData : data) result.add(aData);
          count++;
        }
        char[] da = new char[result.size()];
        int i = 0;
        for (Character d : result) da[i++] = d;
        dataArray = da;

      } else if (dataType == DataType.STRING) {
        ArrayList<String> result = new ArrayList<String>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          String[] data = sdata.getJavaArrayString(realm);
          result.addAll(Arrays.asList(data));
          count++;
        }
        String[] da = new String[result.size()];
        int i = 0;
        for (String d : result) da[i++] = d;
        dataArray = da;

      } else if (dataType == DataType.STRUCTURE) {
        ArrayList<StructureData> result = new ArrayList<StructureData>(initial);
        while (sdataIter.hasNext()) {
          StructureData sdata = sdataIter.next();
          StructureMembers.Member realm = sdata.getStructureMembers().findMember(proxym.getName());
          ArrayStructureBak as = sdata.getArrayStructure(realm);
          StructureDataIterator innerIter = as.getStructureDataIterator();
          while (innerIter.hasNext())
            result.add(innerIter.next());
          count++;
        }
        StructureData[] da = new StructureData[result.size()];
        rshape[0] = count;
        StructureMembers membersw = new StructureMembers(proxym.getStructureMembers()); // no data arrays get propagated
        return new ArrayStructureW(membersw, rshape, da);
      }
    } finally {
      sdataIter.finish();
    }
    // create an array to hold the result
    rshape[0] = count;
    return Array.factory(dataType.getPrimitiveClassType(), rshape, dataArray);
  }

  @Override
  public String toString() {
    return "seq n=" + Integer.toString(nelems);
  }

  @Override
  public void showInternal(Formatter f, Indent indent) {
    super.showInternal(f, indent);
    f.format("%sStructureDataIterator Class=%s hash=0x%x%n", indent, iter.getClass().getName(), iter.hashCode());
    if (iter instanceof ArrayStructureBak.ArrayStructureIterator) {
      ArrayStructureBak.ArrayStructureIterator ii = (ArrayStructureBak.ArrayStructureIterator) iter;
      ii.getArrayStructure().showInternal(f, indent.incr());
      indent.decr();
    }
  }

}
