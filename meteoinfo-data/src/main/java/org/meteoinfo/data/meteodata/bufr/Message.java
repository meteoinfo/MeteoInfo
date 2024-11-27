/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import ucar.nc2.time.CalendarDate;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;

/**
 * Encapsolates a complete BUFR message.
 * A message has a DataDescriptor and one or more "datasets" aka "data subsets" aka "observations" aka "obs".
 * Table lookup is done through getLookup().
 */
public class Message {
    private static final Pattern wmoPattern = Pattern.compile(".*([IJ]..... ....) .*");

    public BufrIndicatorSection is;
    public BufrIdentificationSection ids;
    public BufrDataDescriptionSection dds;
    public BufrDataSection dataSection;

    private RandomAccessFile raf;
    private BufrTableLookup lookup;
    private DataDescriptor root;

    private String header; // wmo header
    private long startPos; // starting pos in raf
    private byte[] raw; // raw bytes

    // bit counting
    BitCounterUncompressed[] counterDatasets; // uncompressed: one for each dataset
    int msg_nbits;

    public Message(RandomAccessFile raf, BufrIndicatorSection is, BufrIdentificationSection ids,
                   BufrDataDescriptionSection dds, BufrDataSection dataSection) throws IOException {
        this.raf = raf;
        this.is = is;
        this.ids = ids;
        this.dds = dds;
        this.dataSection = dataSection;
        lookup = BufrTableLookup.factory(this);
    }

    void setTableLookup(TableLookup lookup) {
        this.lookup.setTableLookup(lookup);
    }

    public void close() throws IOException {
        if (raf != null)
            raf.close();
    }

    /**
     * Get number of datasets in this message.
     *
     * @return number of datasets in this message
     */
    public int getNumberDatasets() {
        return dds.getNumberDatasets();
    }

    public CalendarDate getReferenceTime() {
        return ids.getReferenceTime();
    }

    ///////////////////////////////////////////////////////////////////////////

    // the WMO header is in here somewhere when the message comes over the IDD
    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    // where the message starts in the file
    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

    public long getStartPos() {
        return startPos;
    }

    public void setRawBytes(byte[] raw) {
        this.raw = raw;
    }

    public byte[] getRawBytes() {
        return raw;
    }

    public String extractWMO() {
        Matcher matcher = wmoPattern.matcher(header);
        if (!matcher.matches()) {
            return "";
        }
        return matcher.group(1);
    }

    /**
     * Get the byte length of the entire BUFR record.
     *
     * @return length in bytes of BUFR record
     */
    public long getMessageSize() {
        return is.getBufrLength();
    }

    /**
     * Get the root of the DataDescriptor tree.
     *
     * @return root DataDescriptor
     */
    public DataDescriptor getRootDataDescriptor() {
        if (root == null)
            root = new DataDescriptorTreeConstructor().factory(lookup, dds);
        return root;
    }

    public boolean usesLocalTable() throws IOException {
        DataDescriptor root = getRootDataDescriptor();
        return usesLocalTable(root);
    }

    private boolean usesLocalTable(DataDescriptor dds) {
        for (DataDescriptor key : dds.getSubKeys()) {
            if (key.isLocal())
                return true;
            if ((key.getSubKeys() != null) && usesLocalTable(key))
                return true;
        }
        return false;
    }

    /**
     * Check if this message contains a BUFR table
     *
     * @return true if message contains a BUFR table
     */
    public boolean containsBufrTable() {
        for (Short key : dds.getDataDescriptors()) {
            if (ucar.nc2.iosp.bufr.Descriptor.isBufrTable(key))
                return true;
        }
        return false;
    }

    /**
     * Check if all descriptors were found in the tables.
     *
     * @return true if all dds were found.
     */
    public boolean isTablesComplete() {
        DataDescriptor root = getRootDataDescriptor();
        return !root.isBad;
    }

    public BufrTableLookup getLookup() {
        return lookup;
    }

    ////////////////////////////////////////////////////////////////////////
    // bit counting

    public boolean isBitCountOk() {
        getRootDataDescriptor(); // make sure root is calculated
        getTotalBits(); // make sure bits are counted
        // int nbitsGiven = 8 * (dataSection.getDataLength() - 4);
        int nbytesCounted = getCountedDataBytes();
        int nbytesGiven = dataSection.getDataLength();
        return Math.abs(nbytesCounted - nbytesGiven) <= 1; // radiosondes dataLen not even number of bytes
    }

    public int getCountedDataBytes() {
        int msg_nbytes = msg_nbits / 8;
        if (msg_nbits % 8 != 0)
            msg_nbytes++;
        msg_nbytes += 4;
        if (msg_nbytes % 2 != 0)
            msg_nbytes++; // LOOK seems to be violated by some messages
        return msg_nbytes;
    }

    public int getCountedDataBits() {
        return msg_nbits;
    }


    /**
     * Get the offset of this obs from the start of the message data.
     * Use only for non compressed data
     *
     * @param obsOffsetInMessage index of obs in the message
     * @return offset in bits
     * <p/>
     * public int getBitOffset(int obsOffsetInMessage) {
     * if (dds.isCompressed())
     * throw new IllegalArgumentException("cant call BufrMessage.getBitOffset() on compressed message");
     * <p/>
     * if (!root.isVarLength)
     * return root.total_nbits * obsOffsetInMessage;
     * <p/>
     * getTotalBits(); // make sure its been set
     * return nestedTableCounter[obsOffsetInMessage].getStartBit();
     * }
     */

    public BitCounterUncompressed getBitCounterUncompressed(int obsOffsetInMessage) {
        if (dds.isCompressed())
            throw new IllegalArgumentException("cant call BufrMessage.getBitOffset() on compressed message");

        calcTotalBits(null); // make sure its been set
        return counterDatasets[obsOffsetInMessage];
    }

    /**
     * This is the total number of bits taken by the data in the data section of the message.
     * This is the counted number.
     *
     * @return total number of bits
     */
    public int getTotalBits() {
        if (msg_nbits == 0)
            calcTotalBits(null);
        return msg_nbits;
    }

    // sets msg_nbits as side-effect
    public int calcTotalBits(Formatter out) {
        try {
            if (!dds.isCompressed()) {
                MessageUncompressedDataReader reader = new MessageUncompressedDataReader();
                reader.readData(null, this, raf, null, false, out);
            } else {
                MessageCompressedDataReader reader = new MessageCompressedDataReader();
                reader.readData(null, this, raf, null, out);
            }
        } catch (IOException ioe) {
            return 0;
        }
        return msg_nbits;
    }

    ///////////////////////////////////////////////////////////////////

    /**
     * Override hashcode to be consistent with equals.
     *
     * @return the hash code of dds.getDescriptors()
     */
    public int hashCode() {
        int result = 17;
        result += 37 * result + getDDShashcode();
        // result += 37 * result + ids.getCenterId();
        // result += 37 * result + ids.getSubCenter_id();
        result += 37 * result + ids.getCategory();
        result += 37 * result + ids.getSubCategory();
        return result;
    }

    public int getDDShashcode() {
        root = getRootDataDescriptor();
        return root.hashCode2();
    }

    /**
     * BufrMessage is equal if they have the same dds.
     *
     * @param obj other BufrMessage
     * @return true if equals
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Message))
            return false;
        Message o = (Message) obj;
        if (!dds.getDataDescriptors().equals(o.dds.getDataDescriptors()))
            return false;
        if (ids.getCenterId() != o.ids.getCenterId())
            return false;
        // if (ids.getSubCenter_id() != o.ids.getSubCenter_id()) return false;
        if (ids.getCategory() != o.ids.getCategory())
            return false;
        return ids.getSubCategory() == o.ids.getSubCategory();
    }

    ////////////////////////////////////////////////////////////////////
    // perhaps move this into a helper class - started from ucar.bufr.Dump


    public void showMissingFields(Formatter out) throws IOException {
        lookup.showMissingFields(dds.getDataDescriptors(), out);
    }

    public void dump(Formatter out) { // throws IOException {

        int listHash = dds.getDataDescriptors().hashCode();
        out.format(" BUFR edition %d time= %s wmoHeader=%s hash=[0x%x] listHash=[0x%x] (%d) %n", is.getBufrEdition(),
                getReferenceTime(), getHeader(), hashCode(), listHash, listHash);
        out.format("   Category= %s %n", lookup.getCategoryFullName());
        out.format("   Center= %s %n", lookup.getCenterName());
        out.format("   Table= %s %n", lookup.getTableName());
        out.format("    Table B= wmoTable= %s localTable= %s mode=%s%n", lookup.getWmoTableBName(),
                lookup.getLocalTableBName(), lookup.getMode());
        out.format("    Table D= wmoTable= %s localTable= %s%n", lookup.getWmoTableDName(), lookup.getLocalTableDName());

        out.format("  DDS nsubsets=%d type=0x%x isObs=%b isCompressed=%b%n", dds.getNumberDatasets(), dds.getDataType(),
                dds.isObserved(), dds.isCompressed());

        long startPos = is.getStartPos();
        long startData = dataSection.getDataPos();
        out.format("  startPos=%d len=%d endPos=%d dataStart=%d dataLen=%d dataEnd=%d %n", startPos, is.getBufrLength(),
                (startPos + is.getBufrLength()), startData, dataSection.getDataLength(),
                startData + dataSection.getDataLength());

        dumpDesc(out, dds.getDataDescriptors(), lookup, 4);

        out.format("%n  CDM Nested Table=%n");
        DataDescriptor root = new DataDescriptorTreeConstructor().factory(lookup, dds);
        dumpKeys(out, root, 4);

        /*
         * int nbits = m.getTotalBits();
         * int nbytes = (nbits % 8 == 0) ? nbits / 8 : nbits / 8 + 1;
         * out.format("  totalBits = %d (%d bytes) outputBytes= %d isVarLen=%s isCompressed=%s\n\n",
         * nbits, nbytes, root.getByteWidthCDM(), root.isVarLength(), m.dds.isCompressed());
         */
    }

    private void dumpDesc(Formatter out, List<Short> desc, BufrTableLookup table, int indent) {
        if (desc == null)
            return;

        for (Short fxy : desc) {
            for (int i = 0; i < indent; i++)
                out.format(" ");
            Descriptor.show(out, fxy, table);
            out.format("%n");
            int f = (fxy & 0xC000) >> 14;
            if (f == 3) {
                List<Short> sublist = table.getDescriptorListTableD(fxy);
                dumpDesc(out, sublist, table, indent + 2);
            }
        }
    }

    private void dumpKeys(Formatter out, DataDescriptor tree, int indent) {
        for (DataDescriptor key : tree.subKeys) {
            for (int i = 0; i < indent; i++)
                out.format(" ");
            out.format("%s%n", key);
            if (key.getSubKeys() != null)
                dumpKeys(out, key, indent + 2);
        }
    }

    public void dumpHeader(Formatter out) {

        out.format(" BUFR edition %d time= %s wmoHeader=%s %n", is.getBufrEdition(), getReferenceTime(), getHeader());
        out.format("   Category= %d %s %s %n", lookup.getCategory(), lookup.getCategoryName(), lookup.getCategoryNo());
        out.format("   Center= %s %s %n", lookup.getCenterName(), lookup.getCenterNo());
        out.format("   Table= %d.%d local= %d wmoTables= %s,%s localTables= %s,%s %n", ids.getMasterTableId(),
                ids.getMasterTableVersion(), ids.getLocalTableVersion(), lookup.getWmoTableBName(), lookup.getWmoTableDName(),
                lookup.getLocalTableBName(), lookup.getLocalTableDName());

        out.format("  DDS nsubsets=%d type=0x%x isObs=%b isCompressed=%b%n", dds.getNumberDatasets(), dds.getDataType(),
                dds.isObserved(), dds.isCompressed());
    }

    public void dumpHeaderShort(Formatter out) {
        out.format(" %s, Cat= %s, Center= %s (%s), Table= %d.%d.%d %n", getHeader(), lookup.getCategoryName(),
                lookup.getCenterName(), lookup.getCenterNo(), ids.getMasterTableId(), ids.getMasterTableVersion(),
                ids.getLocalTableVersion());
    }

}
