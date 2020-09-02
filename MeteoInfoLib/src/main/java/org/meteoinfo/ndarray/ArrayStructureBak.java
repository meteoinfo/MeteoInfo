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
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.List;
import java.nio.ByteBuffer;

/**
 * Superclass for implementations of Array of StructureData.
 * <p/>
 * The general way to access data in an ArrayStructure is to use
 * <pre> StructureData getStructureData(Index index).</pre>
 * <p/>
 * For 1D arrays (or by calculating your own recnum for nD arrays), you can also use:
 * <pre> StructureData getStructureData(int recnum).</pre>
 * <p/>
 * Once you have a StructureData object, you can access data in a general way by using:
 * <pre> Array StructureData.getArray(Member m) </pre>
 * <p/>
 * When dealing with large arrays of Structures, there can be significant overhead in using the generic interfaces.
 * A number of convenience routines may be able to avoid extra Object creation, and so are recommended for efficiency.
 * The following may avoid the overhead of creating the StructureData object:
 * <pre> Array getArray(int recno, StructureMembers.Member m) </pre>
 * <p/>
 * The following can be convenient for accessing all the data in the ArrayStructure for one member, but its efficiency
 * depends on the implementation:
 * <pre> Array getMemberArray(StructureMembers.Member m) </pre>
 * <p/>
 * These require that you know the data types of the member data, but they are the most efficent:
 * <pre>
 * getScalarXXX(int recnum, Member m)
 * getJavaArrayXXX(int recnum, Member m) </pre>
 * where XXX is Byte, Char, Double, Float, Int, Long, Short, or String. For members that are themselves Structures,
 * the equivilent is:
 * <pre>
 * StructureData getScalarStructure(int recnum, Member m)
 * ArrayStructure getArrayStructure(int recnum, Member m) </pre>
 * <p/>
 * These will return any compatible type as a double or float, but will have extra overhead when the types dont match:
 * <pre>
 * convertScalarXXX(int recnum, Member m)
 * convertJavaArrayXXX(int recnum, Member m) </pre>
 * where XXX is Double or Float
 *
 * @author caron
 * @see Array
 * @see StructureData
 */
public abstract class ArrayStructureBak extends Array {

  /* Implementation notes
     ArrayStructure contains the default implementation of storing the data in individual member arrays.
     ArrayStructureMA uses all of these.
     ArrayStructureW uses some of these.
     ArrayStructureBB override all such methods.
   */

    protected StructureMembers members;
    protected int nelems;
    protected StructureData[] sdata;

    /**
     * Create a new Array of type StructureData and the given members and shape.
     * dimensions.length determines the rank of the new Array.
     *
     * @param members a description of the structure members
     * @param shape   the shape of the Array.
     */
    protected ArrayStructureBak(StructureMembers members, int[] shape) {
        super(DataType.STRUCTURE, shape);
        this.members = members;
        this.nelems = (int) indexCalc.getSize();
    }

    // for subclasses to create views
    protected ArrayStructureBak(StructureMembers members, Index ima) {
        super(DataType.STRUCTURE, ima);
        this.members = members;
        this.nelems = (int) indexCalc.getSize();
    }

    // copy from javaArray to storage using the iterator: used by factory( Object);
    protected void copyFrom1DJavaArray(IndexIterator iter, Object javaArray) {
        Object[] ja = (Object[]) javaArray;
        for (Object aJa : ja)
            iter.setObjectNext(aJa);
    }

    // copy to javaArray from storage using the iterator: used by copyToNDJavaArray;
    protected void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
        Object[] ja = (Object[]) javaArray;
        for (int i = 0; i < ja.length; i++)
            ja[i] = iter.getObjectNext();
    }

    public Class getElementType() {
        return StructureData.class;
    }

    /**
     * Get the StructureMembers object.
     *
     * @return the StructureMembers object.
     */
    public StructureMembers getStructureMembers() {
        return members;
    }

    /**
     * Get a list of structure members.
     *
     * @return the structure members.
     */
    public List<StructureMembers.Member> getMembers() {
        return members.getMembers();
    }

    /**
     * Get a list structure member names.
     *
     * @return the structure members.
     */
    public List<String> getStructureMemberNames() {
        return members.getMemberNames();
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

    @Override
    public long getSizeBytes() {
        return indexCalc.getSize() * members.getStructureSize();
    }

    /**
     * Get the index-th StructureData of this ArrayStructure.
     *
     * @param i which one to get, specified by an Index.
     * @return object of type StructureData.
     */
    public Object getObject(Index i) {
        return getObject(i.currentElement());
    }

    /**
     * Set one of the StructureData of this ArrayStructure.
     *
     * @param i     which one to set, specified by an Index.
     * @param value must be type StructureData.
     */
    public void setObject(Index i, Object value) {
        setObject(i.currentElement(), value);
    }

    /**
     * Get the index-th StructureData of this ArrayStructure.
     *
     * @param index which one to get, specified by an integer.
     * @return object of type StructureData.
     */
    public Object getObject(int index) {
        return getStructureData(index);
    }

    /**
     * Set the index-th StructureData of this ArrayStructure.
     *
     * @param index which one to set.
     * @param value must be type StructureData.
     */
    public void setObject(int index, Object value) {
        if (sdata == null)
            sdata = new StructureData[nelems];
        sdata[index] = (StructureData) value;
    }

    /**
     * Get the index-th StructureData of this ArrayStructure.
     *
     * @param i which one to get, specified by an Index.
     * @return object of type StructureData.
     */
    public StructureData getStructureData(Index i) {
        return getStructureData(i.currentElement());
    }

    /**
     * Get the index-th StructureData of this ArrayStructure.
     *
     * @param index which one to get, specified by an integer.
     * @return object of type StructureData.
     */
    public StructureData getStructureData(int index) {
        if (sdata == null)
            sdata = new StructureData[nelems];
        if (index >= sdata.length)
            throw new IllegalArgumentException(index + " > " + sdata.length);
        if (sdata[index] == null)
            sdata[index] = makeStructureData(this, index);
        return sdata[index];
    }

    public Object[] getStorage() {
        // this fills the sdata array
        for (int i = 0; i < nelems; i++)
            getStructureData(i);
        return sdata;
    }

    abstract protected StructureData makeStructureData(ArrayStructureBak as, int recno);

    /**
     * Get the size of each StructureData object in bytes.
     *
     * @return the size of each StructureData object in bytes.
     */
    public int getStructureSize() {
        return members.getStructureSize();
    }

    public StructureDataIterator getStructureDataIterator() { // throws java.io.IOException {
        return new ArrayStructureIterator();
    }

    public class ArrayStructureIterator implements StructureDataIterator {
        private int count = 0;
        private int size = (int) getSize();

        @Override
        public boolean hasNext() throws IOException {
            return count < size;
        }

        @Override
        public StructureData next() throws IOException {
            return getStructureData(count++);
        }

        @Override
        public void setBufferSize(int bytes) {
        }

        @Override
        public StructureDataIterator reset() {
            count = 0;
            return this;
        }

        @Override
        public int getCurrentRecno() {
            return count - 1;
        }

        @Override
        public void finish() {
        }

        // debugging
        public ArrayStructureBak getArrayStructure() {
            return ArrayStructureBak.this;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Get member data of any type for a specific record as an Array.
     * This may avoid the overhead of creating the StructureData object, but is equivilent to
     * getStructure(recno).getArray( Member m).
     *
     * @param recno get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m     get data from this StructureMembers.Member.
     * @return Array values.
     */
    public Array getArray(int recno, StructureMembers.Member m) {
        DataType dataType = m.getDataType();

        if (dataType == DataType.DOUBLE) {
            double[] pa = getJavaArrayDouble(recno, m);
            return Array.factory(double.class, m.getShape(), pa);

        } else if (dataType == DataType.FLOAT) {
            float[] pa = getJavaArrayFloat(recno, m);
            return Array.factory(float.class, m.getShape(), pa);

        } else if ((dataType == DataType.BYTE) || (dataType == DataType.ENUM1)) {
            byte[] pa = getJavaArrayByte(recno, m);
            return Array.factory(byte.class, m.getShape(), pa);

        } else if ((dataType == DataType.SHORT) || (dataType == DataType.ENUM2)) {
            short[] pa = getJavaArrayShort(recno, m);
            return Array.factory(short.class, m.getShape(), pa);

        } else if ((dataType == DataType.INT) || (dataType == DataType.ENUM4)) {
            int[] pa = getJavaArrayInt(recno, m);
            return Array.factory(int.class, m.getShape(), pa);

        } else if (dataType == DataType.LONG) {
            long[] pa = getJavaArrayLong(recno, m);
            return Array.factory(long.class, m.getShape(), pa);

        } else if (dataType == DataType.CHAR) {
            char[] pa = getJavaArrayChar(recno, m);
            return Array.factory(char.class, m.getShape(), pa);

        } else if (dataType == DataType.STRING) {
            String[] pa = getJavaArrayString(recno, m);
            return Array.factory(String.class, m.getShape(), pa);

        } else if (dataType == DataType.STRUCTURE) {
            return getArrayStructure(recno, m);

        } else if (dataType == DataType.SEQUENCE) {
            return getArraySequence(recno, m);

        } else if (dataType == DataType.OPAQUE) {
            return getArrayObject(recno, m);
        }

        throw new RuntimeException("Dont have implemenation for " + dataType);
    }

    /**
     * Set data for one member, over all structures.
     * This is used by VariableDS to do scale/offset.
     *
     * @param m           set data for this StructureMembers.Member.
     * @param memberArray Array values.
     */
    public void setMemberArray(StructureMembers.Member m, Array memberArray) {
        m.setDataArray(memberArray);
        if (memberArray instanceof ArrayStructureBak) {  // LOOK
            ArrayStructureBak as = (ArrayStructureBak) memberArray;
            m.setStructureMembers(as.getStructureMembers());
        }
    }

    /**
     * Extract data for one member, over all structures.
     *
     * @param m get data from this StructureMembers.Member.
     * @return Array values.
     * @throws java.io.IOException on read error (only happens for Sequences, otherwise data is already read)
     */
    public Array extractMemberArray(StructureMembers.Member m) throws IOException {
        if (m.getDataArray() != null)
            return m.getDataArray();
        DataType dataType = m.getDataType();

    /* special handling for sequences
    if (dataType == DataType.SEQUENCE) {
      List<StructureData> sdataList = new ArrayList<StructureData>();
      for (int recno=0; recno<getSize(); recno++) {
        ArraySequence2 seq = getArraySequence(recno, m);
        StructureDataIterator iter = seq.getStructureDataIterator();
        while (iter.hasNext())
          sdataList.add( iter.next());
      }
      ArraySequence2 seq = getArraySequence(0, m);
      int size = sdataList.size();
      StructureData[] sdataArray = sdataList.toArray( new StructureData[size]);
      return new ArrayStructureW( seq.getStructureMembers(), new int[] {size}, sdataArray);
   } */

        // combine the shapes
        int[] mshape = m.getShape();
        int rrank = rank + mshape.length;
        int[] rshape = new int[rrank];
        System.arraycopy(getShape(), 0, rshape, 0, rank);
        System.arraycopy(mshape, 0, rshape, rank, mshape.length);

        // create an empty array to hold the result
        Array result;
        if (dataType == DataType.STRUCTURE) {
            StructureMembers membersw = new StructureMembers(m.getStructureMembers()); // no data arrays get propagated
            result = new ArrayStructureW(membersw, rshape);

        } else if (dataType == DataType.OPAQUE) {
            result = new ArrayObject(dataType, ByteBuffer.class, false, rshape);

        } else {
            result = Array.factory(dataType.getPrimitiveClassType(), rshape);
        }

        IndexIterator resultIter = result.getIndexIterator();
        if (dataType == DataType.DOUBLE) {
            for (int recno = 0; recno < getSize(); recno++)
                copyDoubles(recno, m, resultIter);

        } else if (dataType == DataType.FLOAT) {
            for (int recno = 0; recno < getSize(); recno++)
                copyFloats(recno, m, resultIter);

        } else if ((dataType == DataType.BYTE) || (dataType == DataType.ENUM1)) {
            for (int recno = 0; recno < getSize(); recno++)
                copyBytes(recno, m, resultIter);

        } else if ((dataType == DataType.SHORT) || (dataType == DataType.ENUM2)) {
            for (int recno = 0; recno < getSize(); recno++)
                copyShorts(recno, m, resultIter);

        } else if ((dataType == DataType.INT) || (dataType == DataType.ENUM4)) {
            for (int recno = 0; recno < getSize(); recno++)
                copyInts(recno, m, resultIter);

        } else if (dataType == DataType.LONG) {
            for (int recno = 0; recno < getSize(); recno++)
                copyLongs(recno, m, resultIter);

        } else if (dataType == DataType.CHAR) {
            for (int recno = 0; recno < getSize(); recno++)
                copyChars(recno, m, resultIter);

        } else if ((dataType == DataType.STRING) || (dataType == DataType.OPAQUE)) {
            for (int recno = 0; recno < getSize(); recno++)
                copyObjects(recno, m, resultIter);

        } else if (dataType == DataType.STRUCTURE) {
            for (int recno = 0; recno < getSize(); recno++)
                copyStructures(recno, m, resultIter);

        } else if (dataType == DataType.SEQUENCE) {
            for (int recno = 0; recno < getSize(); recno++)
                copySequences(recno, m, resultIter);

        }

        return result;
    }

    protected void copyChars(int recnum, StructureMembers.Member m, IndexIterator result) {
        IndexIterator dataIter = getArray(recnum, m).getIndexIterator();
        while (dataIter.hasNext())
            result.setCharNext(dataIter.getCharNext());
    }

    protected void copyDoubles(int recnum, StructureMembers.Member m, IndexIterator result) {
        IndexIterator dataIter = getArray(recnum, m).getIndexIterator();
        while (dataIter.hasNext())
            result.setDoubleNext(dataIter.getDoubleNext());
    }

    protected void copyFloats(int recnum, StructureMembers.Member m, IndexIterator result) {
        IndexIterator dataIter = getArray(recnum, m).getIndexIterator();
        while (dataIter.hasNext())
            result.setFloatNext(dataIter.getFloatNext());
    }

    protected void copyBytes(int recnum, StructureMembers.Member m, IndexIterator result) {
        IndexIterator dataIter = getArray(recnum, m).getIndexIterator();
        while (dataIter.hasNext())
            result.setByteNext(dataIter.getByteNext());
    }

    protected void copyShorts(int recnum, StructureMembers.Member m, IndexIterator result) {
        IndexIterator dataIter = getArray(recnum, m).getIndexIterator();
        while (dataIter.hasNext())
            result.setShortNext(dataIter.getShortNext());
    }

    protected void copyInts(int recnum, StructureMembers.Member m, IndexIterator result) {
        IndexIterator dataIter = getArray(recnum, m).getIndexIterator();
        while (dataIter.hasNext())
            result.setIntNext(dataIter.getIntNext());
    }

    protected void copyLongs(int recnum, StructureMembers.Member m, IndexIterator result) {
        IndexIterator dataIter = getArray(recnum, m).getIndexIterator();
        while (dataIter.hasNext())
            result.setLongNext(dataIter.getLongNext());
    }

    protected void copyObjects(int recnum, StructureMembers.Member m, IndexIterator result) {
        IndexIterator dataIter = getArray(recnum, m).getIndexIterator();
        while (dataIter.hasNext())
            result.setObjectNext(dataIter.getObjectNext());
    }

    // from the recnum-th structure, copy the member data into result.
    // member data is itself a structure, and may be an array of structures.
    protected void copyStructures(int recnum, StructureMembers.Member m, IndexIterator result) {
        Array data = getArray(recnum, m);
        IndexIterator dataIter = data.getIndexIterator();
        while (dataIter.hasNext())
            result.setObjectNext(dataIter.getObjectNext());
    }

    protected void copySequences(int recnum, StructureMembers.Member m, IndexIterator result) {
        // there can only be one sequence, not an array; copy to an ArrayObject
        Array data = getArray(recnum, m);
        result.setObjectNext(data);
    }

    /**
     * Get member data array of any type as an Object, eg, Float, Double, String, StructureData etc.
     *
     * @param recno get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m     get data from this StructureMembers.Member.
     * @return value as Float, Double, etc..
     */
    public Object getScalarObject(int recno, StructureMembers.Member m) {
        DataType dataType = m.getDataType();

        if (dataType == DataType.DOUBLE) {
            return getScalarDouble(recno, m);

        } else if (dataType == DataType.FLOAT) {
            return getScalarFloat(recno, m);

        } else if ((dataType == DataType.BYTE) || (dataType == DataType.ENUM1)) {
            return getScalarByte(recno, m);

        } else if ((dataType == DataType.SHORT) || (dataType == DataType.ENUM2)) {
            return getScalarShort(recno, m);

        } else if ((dataType == DataType.INT) || (dataType == DataType.ENUM4)) {
            return getScalarInt(recno, m);

        } else if (dataType == DataType.LONG) {
            return getScalarLong(recno, m);

        } else if (dataType == DataType.CHAR) {
            return getScalarString(recno, m);

        } else if (dataType == DataType.STRING) {
            return getScalarString(recno, m);

        } else if (dataType == DataType.STRUCTURE) {
            return getScalarStructure(recno, m);

        } else if (dataType == DataType.OPAQUE) {
            ArrayObject data = (ArrayObject) m.getDataArray();
            return data.getObject(recno * m.getSize()); // LOOK ??
        }

        throw new RuntimeException("Dont have implementation for " + dataType);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get scalar value as a float, with conversion as needed. Underlying type must be convertible to float.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      member Variable.
     * @return scalar float value
     * @throws ForbiddenConversionException if not convertible to float.
     */
    public float convertScalarFloat(int recnum, StructureMembers.Member m) {
        if (m.getDataType() == DataType.FLOAT) return getScalarFloat(recnum, m);
        if (m.getDataType() == DataType.DOUBLE) return (float) getScalarDouble(recnum, m);
        Object o = getScalarObject(recnum, m);
        if (o instanceof Number) return ((Number) o).floatValue();
        throw new ForbiddenConversionException("Type is " + m.getDataType() + ", not convertible to float");
    }

    /**
     * Get scalar value as a double, with conversion as needed. Underlying type must be convertible to double.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      member Variable.
     * @return scalar double value
     * @throws ForbiddenConversionException if not convertible to double.
     */
    public double convertScalarDouble(int recnum, StructureMembers.Member m) {
        if (m.getDataType() == DataType.DOUBLE) return getScalarDouble(recnum, m);
        if (m.getDataType() == DataType.FLOAT) return (double) getScalarFloat(recnum, m);
        Object o = getScalarObject(recnum, m);
        if (o instanceof Number) return ((Number) o).doubleValue();
        throw new ForbiddenConversionException("Type is " + m.getDataType() + ", not convertible to double");
    }

    /**
     * Get scalar value as an int, with conversion as needed. Underlying type must be convertible to int.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      member Variable.
     * @return scalar double value
     * @throws ForbiddenConversionException if not convertible to double.
     */
    public int convertScalarInt(int recnum, StructureMembers.Member m) {
        if (m.getDataType() == DataType.INT) return getScalarInt(recnum, m);
        if (m.getDataType() == DataType.SHORT) return (int) getScalarShort(recnum, m);
        if (m.getDataType() == DataType.BYTE) return (int) getScalarByte(recnum, m);
        if (m.getDataType() == DataType.LONG) return (int) getScalarLong(recnum, m);
        Object o = getScalarObject(recnum, m);
        if (o instanceof Number) return ((Number) o).intValue();
        throw new ForbiddenConversionException("Type is " + m.getDataType() + ", not convertible to int");
    }

    public long convertScalarLong(int recnum, StructureMembers.Member m) {
        if (m.getDataType() == DataType.LONG) return getScalarLong(recnum, m);
        if (m.getDataType() == DataType.INT) return (long) getScalarInt(recnum, m);
        if (m.getDataType() == DataType.SHORT) return (long) getScalarShort(recnum, m);
        if (m.getDataType() == DataType.BYTE) return (long) getScalarByte(recnum, m);
        Object o = getScalarObject(recnum, m);
        if (o instanceof Number) return ((Number) o).longValue();
        throw new ForbiddenConversionException("Type is " + m.getDataType() + ", not convertible to int");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get scalar member data of type double.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type double.
     * @return scalar double value
     */
    public double getScalarDouble(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.DOUBLE)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be double");
        Array data = m.getDataArray();
        return data.getDouble(recnum * m.getSize()); // gets first one in the array
    }

    /**
     * Get member data of type double as a 1D array. The member data may be any rank.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type double.
     * @return double[]
     */
    public double[] getJavaArrayDouble(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.DOUBLE)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be double");
        int count = m.getSize();
        Array data = m.getDataArray();
        double[] pa = new double[count];
        for (int i = 0; i < count; i++)
            pa[i] = data.getDouble(recnum * count + i);
        return pa;
    }

    /**
     * Get scalar member data of type float.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type float.
     * @return scalar double value
     */
    public float getScalarFloat(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.FLOAT)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be float");
        Array data = m.getDataArray();
        return data.getFloat(recnum * m.getSize()); // gets first one in the array
    }

    /**
     * Get member data of type float as a 1D array.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type float.
     * @return float[]
     */
    public float[] getJavaArrayFloat(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.FLOAT)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be float");
        int count = m.getSize();
        Array data = m.getDataArray();
        float[] pa = new float[count];
        for (int i = 0; i < count; i++)
            pa[i] = data.getFloat(recnum * count + i);
        return pa;
    }

    /**
     * Get scalar member data of type byte.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type byte.
     * @return scalar double value
     */
    public byte getScalarByte(int recnum, StructureMembers.Member m) {
        if ((m.getDataType() != DataType.BYTE) && (m.getDataType() != DataType.ENUM1))
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be byte");
        Array data = m.getDataArray();
        return data.getByte(recnum * m.getSize()); // gets first one in the array
    }

    /**
     * Get member data of type byte as a 1D array.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type byte.
     * @return byte[]
     */
    public byte[] getJavaArrayByte(int recnum, StructureMembers.Member m) {
        if ((m.getDataType() != DataType.BYTE) && (m.getDataType() != DataType.ENUM1))
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be byte");
        int count = m.getSize();
        Array data = m.getDataArray();
        byte[] pa = new byte[count];
        for (int i = 0; i < count; i++)
            pa[i] = data.getByte(recnum * count + i);
        return pa;
    }

    /**
     * Get scalar member data of type short.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type short.
     * @return scalar double value
     */
    public short getScalarShort(int recnum, StructureMembers.Member m) {
        if ((m.getDataType() != DataType.SHORT) && (m.getDataType() != DataType.ENUM2))
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be short");
        Array data = m.getDataArray();
        return data.getShort(recnum * m.getSize()); // gets first one in the array
    }

    /**
     * Get member data of type short as a 1D array.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type float.
     * @return short[]
     */
    public short[] getJavaArrayShort(int recnum, StructureMembers.Member m) {
        if ((m.getDataType() != DataType.SHORT) && (m.getDataType() != DataType.ENUM2))
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be short");
        int count = m.getSize();
        Array data = m.getDataArray();
        short[] pa = new short[count];
        for (int i = 0; i < count; i++)
            pa[i] = data.getShort(recnum * count + i);
        return pa;
    }

    /**
     * Get scalar member data of type int.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type int.
     * @return scalar double value
     */
    public int getScalarInt(int recnum, StructureMembers.Member m) {
        if ((m.getDataType() != DataType.INT) && (m.getDataType() != DataType.ENUM4))
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be int");
        Array data = m.getDataArray();
        return data.getInt(recnum * m.getSize()); // gets first one in the array
    }

    /**
     * Get member data of type int as a 1D array.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type int.
     * @return int[]
     */
    public int[] getJavaArrayInt(int recnum, StructureMembers.Member m) {
        if ((m.getDataType() != DataType.INT) && (m.getDataType() != DataType.ENUM4))
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be int");
        int count = m.getSize();
        Array data = m.getDataArray();
        int[] pa = new int[count];
        for (int i = 0; i < count; i++)
            pa[i] = data.getInt(recnum * count + i);
        return pa;
    }

    /**
     * Get scalar member data of type long.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type long.
     * @return scalar double value
     */
    public long getScalarLong(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.LONG)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be long");
        Array data = m.getDataArray();
        return data.getLong(recnum * m.getSize()); // gets first one in the array
    }

    /**
     * Get member data of type long as a 1D array.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type long.
     * @return long[]
     */
    public long[] getJavaArrayLong(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.LONG)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be long");
        int count = m.getSize();
        Array data = m.getDataArray();
        long[] pa = new long[count];
        for (int i = 0; i < count; i++)
            pa[i] = data.getLong(recnum * count + i);
        return pa;
    }

    /**
     * Get scalar member data of type char.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type char.
     * @return scalar double value
     */
    public char getScalarChar(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.CHAR)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be char");
        Array data = m.getDataArray();
        return data.getChar(recnum * m.getSize()); // gets first one in the array
    }

    /**
     * Get member data of type char as a 1D array.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type char.
     * @return char[]
     */
    public char[] getJavaArrayChar(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.CHAR)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be char");
        int count = m.getSize();
        Array data = m.getDataArray();
        char[] pa = new char[count];
        for (int i = 0; i < count; i++)
            pa[i] = data.getChar(recnum * count + i);
        return pa;
    }

    /**
     * Get member data of type String or char.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type String or char.
     * @return scalar String value
     */
    public String getScalarString(int recnum, StructureMembers.Member m) {
        if (m.getDataType() == DataType.CHAR) {
            ArrayChar data = (ArrayChar) m.getDataArray();
            return data.getString(recnum);
        }

        if (m.getDataType() == DataType.STRING) {
            Array data = m.getDataArray();
            return (String) data.getObject(recnum);
        }

        throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be String or char");
    }

    /**
     * Get member data of type String as a 1D array.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type String.
     * @return String[]
     */
    public String[] getJavaArrayString(int recnum, StructureMembers.Member m) {
        if (m.getDataType() == DataType.STRING) {
            int n = m.getSize();
            String[] result = new String[n];
            Array data = m.getDataArray();
            for (int i = 0; i < n; i++)
                result[i] = (String) data.getObject(recnum * n + i);
            return result;
        }

        if (m.getDataType() == DataType.CHAR) {
            int strlen = indexCalc.getShape(rank - 1);
            int n = m.getSize() / strlen;
            String[] result = new String[n];
            ArrayChar data = (ArrayChar) m.getDataArray();
            for (int i = 0; i < n; i++)
                result[i] = data.getString((recnum * n + i) * strlen);
            return result;
        }

        throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be String or char");
    }

    /**
     * Get member data of type Structure.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type Structure.
     * @return scalar StructureData
     */
    public StructureData getScalarStructure(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.STRUCTURE)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Structure");

        ArrayStructureBak data = (ArrayStructureBak) m.getDataArray();
        return data.getStructureData(recnum * m.getSize());  // gets first in the array
    }

    /**
     * Get member data of type array of Structure.
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type Structure.
     * @return nested ArrayStructure.
     */
    public ArrayStructureBak getArrayStructure(int recnum, StructureMembers.Member m) {
        if ((m.getDataType() != DataType.STRUCTURE) && (m.getDataType() != DataType.SEQUENCE))
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Structure or Sequence");

        if (m.getDataType() == DataType.SEQUENCE)
            return getArraySequence(recnum, m);

        ArrayStructureBak array = (ArrayStructureBak) m.getDataArray();

        int count = m.getSize();
        StructureData[] this_sdata = new StructureData[count];
        for (int i = 0; i < count; i++)
            this_sdata[i] = array.getStructureData(recnum * count + i);

        // make a copy of the members, but remove the data arrays, since the structureData must be used instead
        StructureMembers membersw = new StructureMembers(array.getStructureMembers());
        return new ArrayStructureW(membersw, m.getShape(), this_sdata);
    }

    /**
     * Get member data of type ArraySequence
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type Structure.
     * @return nested ArrayStructure.
     */
    public ArraySequence getArraySequence(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.SEQUENCE)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Sequence");

        // should store sequences as ArrayObject of ArraySequence objects
        ArrayObject array = (ArrayObject) m.getDataArray();
        return (ArraySequence) array.getObject(recnum);
    }

    /**
     * Get member data of type ArrayObject
     *
     * @param recnum get data from the recnum-th StructureData of the ArrayStructure. Must be less than getSize();
     * @param m      get data from this StructureMembers.Member. Must be of type Structure.
     * @return ArrayObject.
     */
    public ArrayObject getArrayObject(int recnum, StructureMembers.Member m) {
        if (m.getDataType() != DataType.OPAQUE)
            throw new IllegalArgumentException("Type is " + m.getDataType() + ", must be Sequence");

        ArrayObject array = (ArrayObject) m.getDataArray();
        return (ArrayObject) array.getObject(recnum);     // LOOK ??
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void showInternal(Formatter f, Indent indent) {
        f.format("%sArrayStructure %s size=%d class=%s hash=0x%x%n", indent, members.getName(), getSize(), this.getClass().getName(), hashCode());
    }

    public void showInternalMembers(Formatter f, Indent indent) {
        f.format("%sArrayStructure %s class=%s hash=0x%x%n", indent, members.getName(), this.getClass().getName(), hashCode());
        indent.incr();
        for (StructureMembers.Member m : getMembers())
            m.showInternal(f, indent);
        indent.incr();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Array createView(Index index) {
        //    Section viewSection = index.getSection(); / LOOK if we could do this, we could make this work

        throw new UnsupportedOperationException();
    }

    @Override
    public Array sectionNoReduce(List<Range> ranges) throws InvalidRangeException {
        Section viewSection = new Section(ranges);
        ArrayStructureW result = new ArrayStructureW(this.members, viewSection.getShape());
        int count = 0;
        Section.Iterator iter = viewSection.getIterator(getShape());
        while (iter.hasNext()) {
            int recno = iter.next(null);
            StructureData sd = getStructureData(recno);
            result.setStructureData(sd, count++);
        }
        return result;
    }

    /**
     * DO NOT USE, throws UnsupportedOperationException
     */
    public Array copy() {
        throw new UnsupportedOperationException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public double getDouble(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setDouble(Index i, double value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public float getFloat(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setFloat(Index i, float value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public long getLong(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setLong(Index i, long value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public int getInt(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setInt(Index i, int value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public short getShort(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setShort(Index i, short value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public byte getByte(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setByte(Index i, byte value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public boolean getBoolean(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setBoolean(Index i, boolean value) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public String getString(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setString(Index i, String value) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public Complex getComplex(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setComplex(Index i, Complex value) {
        throw new ForbiddenConversionException();
    }

    public LocalDateTime getDate(Index i) {
        throw new ForbiddenConversionException();
    }

    public void setDate(Index i, LocalDateTime value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public char getChar(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setChar(Index i, char value) {
        throw new ForbiddenConversionException();
    }

    // trusted, assumes that individual dimension lengths have been checked
    // package private : mostly for iterators
    public double getDouble(int index) {
        throw new ForbiddenConversionException();
    }

    public void setDouble(int index, double value) {
        throw new ForbiddenConversionException();
    }

    public float getFloat(int index) {
        throw new ForbiddenConversionException();
    }

    public void setFloat(int index, float value) {
        throw new ForbiddenConversionException();
    }

    public long getLong(int index) {
        throw new ForbiddenConversionException();
    }

    public void setLong(int index, long value) {
        throw new ForbiddenConversionException();
    }

    public int getInt(int index) {
        throw new ForbiddenConversionException();
    }

    public void setInt(int index, int value) {
        throw new ForbiddenConversionException();
    }

    public short getShort(int index) {
        throw new ForbiddenConversionException();
    }

    public void setShort(int index, short value) {
        throw new ForbiddenConversionException();
    }

    public byte getByte(int index) {
        throw new ForbiddenConversionException();
    }

    public void setByte(int index, byte value) {
        throw new ForbiddenConversionException();
    }

    public char getChar(int index) {
        throw new ForbiddenConversionException();
    }

    public void setChar(int index, char value) {
        throw new ForbiddenConversionException();
    }

    public boolean getBoolean(int index) {
        throw new ForbiddenConversionException();
    }

    public void setBoolean(int index, boolean value) {
        throw new ForbiddenConversionException();
    }

    public String getString(int index) {
        throw new ForbiddenConversionException();
    }

    public void setString(int index, String value) {
        throw new ForbiddenConversionException();
    }

    public Complex getComplex(int index) {
        throw new ForbiddenConversionException();
    }

    public void setComplex(int index, Complex value) {
        throw new ForbiddenConversionException();
    }

    public LocalDateTime getDate(int index) { throw new ForbiddenConversionException(); }

    public void setDate(int index, LocalDateTime value) { throw new ForbiddenConversionException(); }

}
