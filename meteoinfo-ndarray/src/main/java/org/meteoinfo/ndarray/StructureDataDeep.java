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

import org.meteoinfo.ndarray.util.DataConvert;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Make a deep copy of an existing StructureData, so that all the data is contained in this object
 *
 * @author caron
 * @since 6/19/2014
 */
public class StructureDataDeep extends StructureDataA {

  /**
   * Make deep copy from sdata to another StructureData object whose data is self contained
   * @param sdata    original sdata
   * @param members  the StructureData members. a reference is kept to this object
   * @return StructureData with all data self contained
   */
  static public StructureDataDeep copy(StructureData sdata, StructureMembers members) {
    ArrayStructureBB abb = copyToArrayBB(sdata, members, ByteOrder.BIG_ENDIAN);
    return new StructureDataDeep(abb);
  }

  /**
   * Make deep copy from an ArrayStructure to a ArrayStructureBB whose data is contained in a ByteBuffer
   * @param as    original ArrayStructure
   * @param bo    what byte order to use ? (null for any)
   * @param canonical  packing must be canonical
   * @return ArrayStructureBB with all data self contained
   */
  static public ArrayStructureBB copyToArrayBB(ArrayStructureBak as, ByteOrder bo, boolean canonical) throws IOException {
    if (!canonical && as.getClass().equals(ArrayStructureBB.class)) { // no subclasses, LOOK detect already canonical later
      ArrayStructureBB abb = (ArrayStructureBB) as;
      ByteBuffer bb = abb.getByteBuffer();
      if (bo == null || bo.equals(bb.order()))
        return abb;
    }

    StructureMembers smo = as.getStructureMembers();
    StructureMembers sm = new StructureMembers(smo);
    ArrayStructureBB abb = new ArrayStructureBB(sm, as.getShape());
    ArrayStructureBB.setOffsets(sm);  // this makes the packing canonical
    if (bo != null) {
      ByteBuffer bb = abb.getByteBuffer();
      bb.order(bo);
    }

    StructureDataIterator iter = as.getStructureDataIterator();
    try {
      while (iter.hasNext())
        copyToArrayBB(iter.next(), abb);
    } finally {
      iter.finish();
    }
    return abb;
  }

   /**
   * Make deep copy from a StructureData to a ArrayStructureBB whose data is contained in a ByteBuffer.
   * @param sdata  original ArrayStructure.
   * @return ArrayStructureBB with all data self contained
   */
  static public ArrayStructureBB copyToArrayBB(StructureData sdata) {
    return copyToArrayBB(sdata, new StructureMembers(sdata.getStructureMembers()), ByteOrder.BIG_ENDIAN);
  }

  /**
   * Make deep copy from a StructureData to a ArrayStructureBB whose data is contained in a ByteBuffer
   * @param sdata    original ArrayStructure
   * @param sm       the StructureData members. a reference is kept to this object
   * @param bo       Byte Order of the ByteBuffer
   * @return ArrayStructureBB with all data self contained
   */
  static public ArrayStructureBB copyToArrayBB(StructureData sdata, StructureMembers sm, ByteOrder bo) {
    int size = sm.getStructureSize();
    ByteBuffer bb = ByteBuffer.allocate(size); // default is big endian
    bb.order(bo);
    ArrayStructureBB abb = new ArrayStructureBB(sm, new int[]{1}, bb, 0);
    ArrayStructureBB.setOffsets(sm);
    copyToArrayBB(sdata, abb);
    return abb;
  }

  /**
   * Make deep copy from a StructureData into the given ArrayStructureBB
   * @param sdata    original data from here
   * @param abb      copy data into this ArrayStructureBB, starting from wherever the ByteBuffer current position is
   * @return number of bytes copied
   */
  static public int copyToArrayBB(StructureData sdata, ArrayStructureBB abb) {
    //StructureMembers sm = sdata.getStructureMembers();
    ByteBuffer bb = abb.getByteBuffer();
    int start = bb.limit();

    for (StructureMembers.Member wantMember : abb.getMembers()) {
      StructureMembers.Member m = sdata.findMember(wantMember.getName());
      assert m != null;
      assert m.getDataType() == wantMember.getDataType();

      DataType dtype = m.getDataType();
      //System.out.printf("do %s (%s) = %d%n", m.getName(), m.getDataType(), bb.position());
      if (m.isScalar()) {
        switch (dtype) {
          case STRING:
            bb.putInt(abb.addObjectToHeap(sdata.getScalarString(m)));
            break;
          case FLOAT:
            bb.putFloat(sdata.getScalarFloat(m));
            break;
          case DOUBLE:
            bb.putDouble(sdata.getScalarDouble(m));
            break;
          case INT:
          case ENUM4:
            bb.putInt(sdata.getScalarInt(m));
            break;
          case SHORT:
          case ENUM2:
            bb.putShort(sdata.getScalarShort(m));
            break;
          case BYTE:
          case ENUM1:
            bb.put(sdata.getScalarByte(m));
            break;
          case CHAR:
            bb.put((byte) sdata.getScalarChar(m));
            break;
          case LONG:
            bb.putLong(sdata.getScalarLong(m));
            break;
          case STRUCTURE:
            StructureData sd  = sdata.getScalarStructure(m);
            ArrayStructureBB out_abb = new ArrayStructureBB(sd.getStructureMembers(),
                    new int[]{1}, bb, 0);
            copyToArrayBB(sd, out_abb);
            break;
          default:
            throw new IllegalStateException("scalar " + dtype.toString());
            /* case BOOLEAN:
           break;
         case SEQUENCE:
           break;
         case OPAQUE:
           break; */
        }
      } else {
        int n = m.getSize();
        switch (dtype) {
          case STRING:
            String[] ss = sdata.getJavaArrayString(m);
            bb.putInt(abb.addObjectToHeap(ss)); // stored as String[] on the heap
            break;
          case FLOAT:
            float[] fdata = sdata.getJavaArrayFloat(m);
            for (int i = 0; i < n; i++)
              bb.putFloat(fdata[i]);
            break;
          case DOUBLE:
            double[] ddata = sdata.getJavaArrayDouble(m);
            for (int i = 0; i < n; i++)
              bb.putDouble(ddata[i]);
            break;
          case INT:
          case ENUM4:
            int[] idata = sdata.getJavaArrayInt(m);
            for (int i = 0; i < n; i++)
              bb.putInt(idata[i]);
            break;
          case SHORT:
          case ENUM2:
            short[] shdata = sdata.getJavaArrayShort(m);
            for (int i = 0; i < n; i++)
              bb.putShort(shdata[i]);
            break;
          case BYTE:
          case ENUM1:
            byte[] bdata = sdata.getJavaArrayByte(m);
            for (int i = 0; i < n; i++)
              bb.put(bdata[i]);
            break;
          case CHAR:
            char[] cdata = sdata.getJavaArrayChar(m);
            bb.put(DataConvert.convertCharToByte(cdata));
            break;
          case LONG:
            long[] ldata = sdata.getJavaArrayLong(m);
            for (int i = 0; i < n; i++)
              bb.putLong(ldata[i]);
            break;
          default:
            throw new IllegalStateException("array " + dtype.toString());
            /* case BOOLEAN:
          break;
         case OPAQUE:
          break;
        case STRUCTURE:
          break; // */
          case SEQUENCE:
            break; // skip
        }
      }
    }
    return bb.limit() - start;
  }

  //private ArrayStructureBB abb;
  private StructureDataDeep(ArrayStructureBB abb) {
    super(abb, 0);
    //this.abb = abb;
  }

  // public ByteBuffer getByteBuffer() { return abb.getByteBuffer(); }

}
