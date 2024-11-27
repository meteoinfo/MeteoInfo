/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import org.meteoinfo.data.meteodata.bufr.tables.TableB;
import org.meteoinfo.data.meteodata.bufr.tables.TableC;
import ucar.nc2.Sequence;

import java.util.List;
import java.util.Objects;

/**
 * Essentially a TableB entry, modified by any relevent TableC operators.
 * TableD has been expanded.
 * Replication gets made into nested DataDescriptors, which we map to Structures (fixed replication) or
 * Sequences (deferred replication).
 * Most of the processing is done by DataDescriptorTreeConstructor.convert().
 * Here we encapsulate the final result, ready to map to the CDM.
 *
 * @author caron
 * @since Apr 5, 2008
 */
public class DataDescriptor {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataDescriptor.class);

    ////////////////////////////////

    // from the TableB.Descriptor
    short fxy;
    int f, x, y;
    String name;
    private String units, desc, source;
    private boolean localOverride;
    boolean bad; // no descriptor found

    // may get modified by TableC operators
    int scale;
    int refVal;
    int bitWidth;
    int type; // 0 = isNumeric, 1 = isString, 2 = isEnum, 3 = compound;

    // replication info
    List<DataDescriptor> subKeys;
    int replication = 1; // number of replications, essentially dk.y when sk.f == 1
    int replicationCountSize; // for delayed replication : size of count in bits
    int repetitionCountSize; // for delayed repetition

    AssociatedField assField; // associated field == 02 04 Y, Y number of extra bits
    Sequence refersTo; // needed for nested sequence objects
    DataDescriptorTreeConstructor.DataPresentIndicator dpi;

    DataDescriptor() {
    }

    public DataDescriptor(short fxy, BufrTableLookup lookup) {
        this.fxy = fxy;
        this.f = (fxy & 0xC000) >> 14;
        this.x = (fxy & 0x3F00) >> 8;
        this.y = fxy & 0xFF;

        TableB.Descriptor db;
        if (f == 0) {
            db = lookup.getDescriptorTableB(fxy);
            if (db != null)
                setDescriptor(db);
            else {
                bad = true;
                this.name = "*NOT FOUND";
            }
        } else if (f == 1) // replication
            this.type = 3; // compound

        else if (f == 2) {
            this.name = TableC.getOperatorName(x);
        }
    }

    /**
     * Test if unit string indicates that the data are 7-bit coded characters following
     * the International Reference Alphabet (formally known as the International Alphabet
     * No.5 (IA5)) Recommendation/International Standard from the International Telegraph
     * and Telephone Consultative Committee (CCITT)
     * <p>
     * https://www.itu.int/rec/T-REC-T.50/en
     *
     * @param unitString unit
     * @return If true, treat the data as 7-bit coded International Reference Alphabet Characters
     */
    public static boolean isInternationalAlphabetUnit(String unitString) {
        String testUnitString = unitString.toLowerCase();
        return testUnitString.startsWith("ccitt");
    }

    /**
     * Test if the unit string indicates that we are dealing with data associated with a code table
     *
     * @param unitString unit
     * @return If true, the unit indicates we are working with data associated with a code table
     */
    public static boolean isCodeTableUnit(String unitString) {
        String testUnitString = unitString.toLowerCase();
        return testUnitString.equalsIgnoreCase("Code Table") || testUnitString.equalsIgnoreCase("Code_Table")
                || testUnitString.startsWith("codetable");
    }

    /**
     * Test if the unit string indicates that we are dealing with data associated with a flag table
     *
     * @param unitString unit
     * @return If true, the unit indicates we are working with data associated with a flag table
     */
    public static boolean isFlagTableUnit(String unitString) {
        String testUnitString = unitString.toLowerCase();
        return testUnitString.equalsIgnoreCase("Flag Table") || testUnitString.equalsIgnoreCase("Flag_Table")
                || testUnitString.startsWith("flagtable");
    }

    private void setDescriptor(TableB.Descriptor d) {
        this.name = d.getName().trim();
        this.units = d.getUnits().trim();
        this.desc = d.getDesc();
        this.refVal = d.getRefVal();
        this.scale = d.getScale();
        this.bitWidth = d.getDataWidth();
        this.localOverride = d.getLocalOverride();
        this.source = d.getSource();

        if (isInternationalAlphabetUnit(units)) {
            this.type = 1; // String
        }

        // LOOK what about flag table ??
        if (isCodeTableUnit(units)) {
            this.type = 2; // enum
        }
    }

    /*
     * for dpi fields
     * DataDescriptor makeStatField(String statType) {
     * DataDescriptor statDD = new DataDescriptor();
     * statDD.name = name + "_" + statType;
     * statDD.units = units;
     * statDD.refVal = 0;
     *
     * return statDD;
     * }
     */

    // for associated fields
    DataDescriptor makeAssociatedField(int bitWidth) {
        DataDescriptor assDD = new DataDescriptor();
        assDD.name = name + "_associated_field";
        assDD.units = "";
        assDD.refVal = 0;
        assDD.scale = 0;
        assDD.bitWidth = bitWidth;
        assDD.type = 0;

        assDD.f = 0;
        assDD.x = 31;
        assDD.y = 22;
        assDD.fxy = (short) ((f << 14) + (x << 8) + (y));

        return assDD;
    }

    static class AssociatedField {
        int nbits;
        int nfields;
        String dataFldName;

        AssociatedField(int nbits) {
            this.nbits = nbits;
        }
    }

    public List<DataDescriptor> getSubKeys() {
        return subKeys;
    }

    boolean isOkForVariable() {
        return (f == 0) || (f == 1) || ((f == 2) && (x == 5) || ((f == 2) && (x == 24) && (y == 255)));
    }

    public boolean isLocal() {
        if ((f == 0) || (f == 3)) {
            return (x >= 48) || (y >= 192);
        }
        return false;
    }

    public boolean isLocalOverride() {
        return localOverride;
    }

    public String getFxyName() {
        return Descriptor.makeString(f, x, y);
    }


    public short getFxy() {
        return fxy;
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public int getType() {
        return type;
    }

    public int getScale() {
        return scale;
    }

    public int getRefVal() {
        return refVal;
    }

    public String getUnits() {
        return units;
    }

    public String getDesc() {
        return desc;
    }

    public float convert(long raw) {
        if (ucar.nc2.iosp.bufr.BufrNumbers.isMissing(raw, bitWidth))
            return Float.NaN;

        // bpacked = (value * 10^scale - refVal)
        // value = (bpacked + refVal) / 10^scale
        float fscale = (float) Math.pow(10.0, -scale); // LOOK precompute ??
        float fval = (raw + refVal);
        return fscale * fval;
    }

    public static float convert(long raw, int scale, int refVal, int bitWidth) {
        if (BufrNumbers.isMissing(raw, bitWidth))
            return Float.NaN;

        // bpacked = (value * 10^scale - refVal)
        // value = (bpacked + refVal) / 10^scale
        float fscale = (float) Math.pow(10.0, -scale); // LOOK precompute ??
        float fval = (raw + refVal);
        return fscale * fval;
    }

    /**
     * Transfer info from the "proto message" to another message with the exact same structure.
     *
     * @param fromList transfer from here
     * @param toList   to here
     */
    static void transferInfo(List<DataDescriptor> fromList, List<DataDescriptor> toList) { // get info from proto
        // message
        if (fromList.size() != toList.size())
            throw new IllegalArgumentException("list sizes dont match " + fromList.size() + " != " + toList.size());

        for (int i = 0; i < fromList.size(); i++) {
            DataDescriptor from = fromList.get(i);
            DataDescriptor to = toList.get(i);
            to.refersTo = from.refersTo;
            to.name = from.name;

            if (from.getSubKeys() != null)
                transferInfo(from.getSubKeys(), to.getSubKeys());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private int total_nbytesCDM;

    /**
     * count the bits used by the data in this dd and its children
     * only accurate for not compressed, and not variable length
     *
     * @return bits used by the data in the file
     */
    int countBits() {
        int total_nbits = 0;
        total_nbytesCDM = 0;

        for (DataDescriptor dd : subKeys) {
            if (dd.subKeys != null) {
                total_nbits += dd.countBits();
                total_nbytesCDM += dd.total_nbytesCDM;

            } else if (dd.f == 0) {
                total_nbits += dd.bitWidth;
                total_nbytesCDM += dd.getByteWidthCDM();
            }
        }

        // replication
        if (replication > 1) {
            total_nbits *= replication;
            total_nbytesCDM *= replication;
        }

        return total_nbits;
    }

    public int getBitWidth() {
        return bitWidth;
    }

    /**
     * Get the number of bytes the CDM datatype will take
     *
     * @return the number of bytes the CDM datatype will take
     */
    int getByteWidthCDM() {
        if (type == 1) // string
            return bitWidth / 8;

        if (type == 3) // compound
            return total_nbytesCDM;

        // numeric or enum
        if (bitWidth < 9)
            return 1;
        if (bitWidth < 17)
            return 2;
        if (bitWidth < 33)
            return 4;
        return 8;
    }

    public String toString() {
        String id = getFxyName();
        StringBuilder sbuff = new StringBuilder();
        if (f == 0) {
            sbuff.append(getFxyName()).append(": ");
            sbuff.append(name).append(" units=").append(units);
            if (type == 0) {
                sbuff.append(" scale=").append(scale).append(" refVal=").append(refVal);
                sbuff.append(" nbits=").append(bitWidth);
            } else if (type == 1) {
                sbuff.append(" nchars=").append(bitWidth / 8);
            } else {
                sbuff.append(" enum nbits=").append(bitWidth);
            }

        } else if (f == 1) {
            sbuff.append(id).append(": ").append("Replication");
            if (replication != 1)
                sbuff.append(" count=").append(replication);
            if (replicationCountSize != 0)
                sbuff.append(" replicationCountSize=").append(replicationCountSize);
            if (repetitionCountSize != 0)
                sbuff.append(" repetitionCountSize=").append(repetitionCountSize);
            if (name != null)
                sbuff.append(": " + name);

        } else if (f == 2) {
            String desc = TableC.getOperatorName(x);
            if (desc == null)
                desc = "Operator";
            sbuff.append(id).append(": ").append(desc);

        } else
            sbuff.append(id).append(": ").append(name);

        return sbuff.toString();
    }

    /////////////////////////////////
    // stuff for the root
    boolean isVarLength;
    boolean isBad;
    int total_nbits;

    public int getTotalBits() {
        return total_nbits;
    }

    public boolean isVarLength() {
        return isVarLength;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LOOK need different hashCode, reader assumes using object id
    public boolean equals2(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataDescriptor that = (DataDescriptor) o;

        if (fxy != that.fxy)
            return false;
        if (replication != that.replication)
            return false;
        if (type != that.type)
            return false;
        return Objects.equals(subKeys, that.subKeys);

    }

    public int hashCode2() {
        int result = (int) fxy;
        result = 31 * result + type;
        result = 31 * result + getListHash();
        result = 31 * result + replication;
        return result;
    }

    // has to use hashCode2, so cant use list.hashCode()
    private int getListHash() {
        if (subKeys == null)
            return 0;
        int result = 1;
        for (DataDescriptor e : subKeys)
            result = 31 * result + (e == null ? 0 : e.hashCode2());
        return result;
    }

}
