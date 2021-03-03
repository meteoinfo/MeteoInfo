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

import java.util.*;

/**
 * A Collection of members contained in a StructureData.
 *
 * @author caron
 */

public class StructureMembers {
  private String name;
  private Map<String, Member> memberHash;
  private List<Member> members;
  private int structureSize = -1;

  public StructureMembers(String name) {
    this.name = name;
    members = new ArrayList<>();
  }

  public StructureMembers(StructureMembers from) {
    this.name = from.name;
    members = new ArrayList<>(from.getMembers().size());
    for (Member m : from.members) {
      Member nm = new Member(m); // make copy - without the data info
      addMember( nm);
      if (m.members != null) // recurse
        nm.members = new StructureMembers(m.members);
    }
  }

  /**
   * Get the name.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Add a member.
   *
   * @param m member to add
   */
  public void addMember(Member m) {
    members.add(m);
    if (memberHash != null)
      memberHash.put(m.getName(), m);
  }

  /**
   * Add a member at the given position.
   *
   * @param m member to add
   */
  public void addMember(int pos, Member m) {
    members.add(pos, m);
    if (memberHash != null)
      memberHash.put(m.getName(), m);
  }

  public Member addMember(String name, String desc, String units, DataType dtype, int[] shape) {
    Member m = new Member(name, desc, units, dtype, shape);
    addMember(m);
    return m;
  }

  /**
   * Remove the given member
   * @param m member
   * @return position that it used to occupy, or -1 if not found
   */
  public int hideMember(Member m) {
    if (m == null) return -1;
    int index = members.indexOf(m);
    members.remove(m);
    if (memberHash != null) memberHash.remove(m.getName());
    return index;
  }

  /**
   * Get the total size of the Structure in bytes.
   *
   * @return the total size of the Structure in bytes.
   */
  public int getStructureSize() {
    if (structureSize < 0)
      calcStructureSize();
    return structureSize;
  }

  private void calcStructureSize() {
    structureSize = 0;
    for (Member member : members) {
      structureSize += member.getSizeBytes();
      // System.out.println(member.getName()+" size="+member.getTotalSize());
    }
  }

  /**
   * Set the total size of the Structure in bytes.
   *
   * @param structureSize set to this value
   */
  public void setStructureSize(int structureSize) {
    this.structureSize = structureSize;
  }

  /**
   * Get the list of Member objects.
   *
   * @return the list of Member objects.
   */
  public List<Member> getMembers() {
    return members;
  }


  /**
   * Get the names of the members.
   *
   * @return List of type String.
   */
  public List<String> getMemberNames() {
    List<String> memberNames = new ArrayList<>();
    for (Member m : members) {
      memberNames.add(m.getName());
    }
    return memberNames;
  }

  /**
   * Get the index-th member
   *
   * @param index of member
   * @return Member
   */
  public Member getMember(int index) {
    return members.get(index);
  }

  /**
   * Find the member by its name.
   *
   * @param memberName find by this name
   * @return Member matching the name, or null if not found
   */
  public Member findMember(String memberName) {
    if (memberName == null) return null;
    
    if (memberHash == null) { // delay making the hash table until needed
      int initial_capacity = (int) (members.size() / .75) + 1;
      memberHash = new HashMap<>(initial_capacity);
      for (Member m : members)
        memberHash.put(m.getName(), m);
    }
    return memberHash.get(memberName);
  }

  /**
   * A member of a StructureData.
   */
  public class Member { 
    private String name, desc, units;
    private DataType dtype;
    private int size = 1;
    private int[] shape;
    private StructureMembers members; // only if member is type Structure
    private boolean isVariableLength = false;

    // optional, use depends on ArrayStructure subclass
    private Array dataArray;
    private Object dataObject;
    private int dataParam;

    public Member(String name, String desc, String units, DataType dtype, int[] shape) {
      this.name = name;
      this.desc = desc;
      this.units = units;
      this.dtype = dtype;
      setShape(shape);
    }

    public Member(Member from) {
      this.name = from.name;
      this.desc = from.desc;
      this.units = from.units;
      this.dtype = from.dtype;
      this.members = from.members;
      setShape(from.shape);
    }

    /*
     * If member is type Structure, you must set its constituent members
     * @param members set to this value
     */
    public void setStructureMembers(StructureMembers members) {
      this.members = members;
    }

    public StructureMembers getStructureMembers() {
      return members;
    }

    public void setShape(int[] shape) {
      this.shape = shape;
      this.size = (int) Index.computeSize(shape);
      this.isVariableLength = (shape.length > 0 && shape[shape.length - 1] < 0);
    }

    /**
     * Get the short name.
     *
     * @return the short name.
     */
    public String getName() {
      return name;
    }

    public String getFullName() {
      return StructureMembers.this.getName() + "." +  name;
    }

    /**
     * Get the units string, if any.
     *
     * @return the units string, or null if none.
     */
    public String getUnitsString() {
      return units;
    }

    /**
     * Get the description, if any.
     *
     * @return the description, or null if none.
     */
    public String getDescription() {
      return desc;
    }

    /**
     * Get the DataType.
     *
     * @return the DataType.
     */
    public DataType getDataType() {
      return dtype;
    }

    /**
     * Get the array shape. This does not have to match the VariableSimpleIF.
     *
     * @return the array shape.
     */
    public int[] getShape() {
      return shape;
    }

    /**
     * Get the total number of elements.
     * This does not have to match the VariableSimpleIF.
     *
     * @return the total number of elements.
     */
    public int getSize() {
      return size;
    }

    public boolean isVariableLength() {
      return isVariableLength;
    }

    /**
     * Get the total size in bytes. This does not have to match the VariableSimpleIF.
     *
     * Note that this will not be correct when containing a member of type Sequence, or String, since those
     * are variable length. In that case
     *
     * @return total size in bytes
     */
    public int getSizeBytes() {
      if (getDataType() == DataType.SEQUENCE)
        return getDataType().getSize();
      else if (getDataType() == DataType.STRING)
        return getDataType().getSize();
      else if (getDataType() == DataType.STRUCTURE)
        return size * members.getStructureSize();
      else if(this.isVariableLength())
          return 0; // do not know
      else
        return size * getDataType().getSize();
    }

    /**
     * Is this a scalar (size == 1).
     * This does not have to match the VariableSimpleIF.
     *
     * @return if this is a scalar
     */
    public boolean isScalar() {
      return size == 1;
    }

    ////////////////////////////////////////////////
    // these should not really be public

    /**
     * Get the data parameter value, for use behind the scenes.
     *
     * @return data parameter value
     */
    public int getDataParam() {
      return dataParam;
    }

    /**
     * Set the data parameter value, for use behind the scenes.
     *
     * @param dataParam set to this value
     */
    public void setDataParam(int dataParam) {
      this.dataParam = dataParam;
    }

    /**
     * Get the data array, if any. Used for implementation, DO NOT USE DIRECTLY!
     * @return  data object, may be null
     */
    public Array getDataArray() {
      return dataArray;
    }

    /**
     * Set the data array. Used for implementation, DO NOT USE DIRECTLY!
     * @param data set to this Array. must not be a logical view
     */
    public void setDataArray(Array data) {
      this.dataArray = data;
    }

    /**
     * Get an opaque data object, for use behind the scenes. May be null
     * @return data object, may be null
     */
    public Object getDataObject() {
      return dataObject;
    }

    /**
     * Set an opaque data object, for use behind the scenes.
     * @param o set to this value
     */
    public void setDataObject(Object o) {
      this.dataObject = o;
    }

    public void setVariableInfo(String vname, String desc, String unitString, DataType dtype) {
      if (!this.name.equals(vname) && (memberHash != null)) {
        memberHash.remove(name);
        memberHash.put(vname, this);
      }
      name = vname;

      if (dtype != null)
        this.dtype = dtype;
      if (unitString != null)
        this.units = unitString;
      if (desc != null)
        this.desc = desc;
    }

    public void showInternal(Formatter f, Indent indent) {
      f.format("%sname='%s' desc='%s' units='%s' dtype=%s size=%d dataObject=%s dataParam=%d", indent, name, desc, units, dtype, size, dataObject, dataParam);
      if (members != null) {
        indent.incr();
        f.format("%n%sNested members %s%n", indent, members.getName());
        for (Member m : members.getMembers())
          m.showInternal(f, indent);
        indent.decr();
      }
      f.format("%n");
    }

    public String toString() { 
      return name;
    }
  }

}
