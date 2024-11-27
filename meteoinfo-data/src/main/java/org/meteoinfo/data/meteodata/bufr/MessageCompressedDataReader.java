/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr;

import ucar.ma2.*;
import ucar.nc2.Sequence;
import ucar.nc2.Structure;
import ucar.nc2.iosp.BitReader;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

/**
 * Reads through the data of a message.
 * Can count bits / transfer all or some data to an Array.
 *
 * @author caron
 * @since Nov 15, 2009
 */

/*
 * Within one message there are n obs (datasets) and s fields in each dataset.
 * For compressed datasets, storage order is data(fld, obs) (obs varying fastest) :
 *
 * Ro1, NBINC1, I11, I12, . . . I1n
 * Ro2, NBINC2, I21, I22, . . . I2n
 * ...
 * Ros, NBINCs, Is1, Is2, . . . Isn
 *
 * where Ro1, Ro2, . . . Ros are local reference values (number of bits as Table B) for field i.
 * NBINC1 . . . NBINCs contain, as 6-bit quantities, the number of bits occupied by the increments that follow.
 * If NBINC1 = 0, all values of element I are equal to Ro1; in such cases, the increments shall be omitted.
 * For character data, NBINC shall contain the number of octets occupied by the character element.
 * However, if the character data in all subsets are identical NBINC=0.
 * Iij is the increment for the ith field and the jth obs.
 *
 * A replicated field (structure) takes a group of fields and replicates them.
 * Let C be the entire compressed block for the ith field, as above.
 *
 * Ci = Roi, NBINCi, Ii1, Ii2, . . . Iin
 *
 * data:
 *
 * C1, (C2, C3)*r, ... Cs
 *
 * where r is set in the data descriptor, and is the same for all datasets.
 *
 * A delayed replicated field (sequence) takes a group of fields and replicates them, with the number of replications
 * in the data :
 *
 * C1, dr, 6bits, (C2, C3)*dr, ... Cs
 *
 * where the width (nbits) of dr is set in the data descriptor. This dr must be the same for each dataset in the
 * message.
 * For some reason there is an extra 6 bits after the dr. My guess its a programming mistake that is now needed.
 * There is no description of this case in the spec or the guide.
 *
 *
 * --------------------------
 *
 * We use an ArrayStructureMA to hold the data, and fill it sequentially as we scan the message.
 * Each field is held in an Array stored in the member.getDataArray().
 * An iterator is stored in member.getDataObject() which keeps track of where we are.
 * For fixed length nested Structures, we need fld(dataset, inner) but we have fld(inner, dataset) se we transpose the
 * dimensions
 * before we set the iterator.
 * For Sequences, inner.length is the same for all datasets in the message. However, it may vary across messages.
 * However, we
 * only iterate over the inner sequence, never across all messages. So the implementation can be specific to the
 * meassage.
 *
 */

public class MessageCompressedDataReader {

    /**
     * Read all datasets from a single message
     *
     * @param s     outer variables
     * @param proto prototype message, has been processed
     * @param m     read this message
     * @param raf   from this file
     * @param f     output bit count debugging info (may be null)
     * @return ArrayStructure with all the data from the message in it.
     * @throws IOException on read error
     */
    public ArrayStructure readEntireMessage(Structure s, Message proto, Message m, RandomAccessFile raf, Formatter f)
            throws IOException {
        // transfer info (refersTo, name) from the proto message
        DataDescriptor.transferInfo(proto.getRootDataDescriptor().getSubKeys(), m.getRootDataDescriptor().getSubKeys());

        // allocate ArrayStructureMA for outer structure
        int n = m.getNumberDatasets();
        ArrayStructureMA ama = ArrayStructureMA.factoryMA(s, new int[]{n});
        setIterators(ama);

        // map dkey to Member recursively
        HashMap<DataDescriptor, StructureMembers.Member> map = new HashMap<>(100);
        associateMessage2Members(ama.getStructureMembers(), m.getRootDataDescriptor(), map);

        readData(m, raf, f, new Request(ama, map, null));

        return ama;
    }

    /**
     * Read some or all datasets from a single message
     *
     * @param ama place data into here in order (may be null). iterators must be already set.
     * @param m   read this message
     * @param raf from this file
     * @param r   which datasets, relative to this message. null == all.
     * @param f   output bit count debugging info (may be null)
     * @throws IOException on read error
     */
    public void readData(ArrayStructureMA ama, Message m, RandomAccessFile raf, Range r, Formatter f) throws IOException {
        // map dkey to Member recursively
        HashMap<DataDescriptor, StructureMembers.Member> map = null;
        if (ama != null) {
            map = new HashMap<>(2 * ama.getMembers().size());
            associateMessage2Members(ama.getStructureMembers(), m.getRootDataDescriptor(), map);
        }

        readData(m, raf, f, new Request(ama, map, r));
    }

    // manage the request
    private static class Request {
        ArrayStructureMA ama; // data goes here, may be null
        HashMap<DataDescriptor, StructureMembers.Member> map; // map of DataDescriptor to members of ama, may be null
        Range r; // requested range
        DpiTracker dpiTracker; // may be null
        int outerRow; // if inner process needs to know what row its on

        Request(ArrayStructureMA ama, HashMap<DataDescriptor, StructureMembers.Member> map, Range r) {
            this.ama = ama;
            this.map = map;
            this.r = r;
        }

        boolean wantRow(int row) {
            if (ama == null)
                return false;
            if (r == null)
                return true;
            return r.contains(row);
        }
    }

    // An iterator is stored in member.getDataObject() which keeps track of where we are.
    // For fixed length nested Structures, we need fld(dataset, inner1, inner2, ...) but we have fld(inner1, inner2, ... ,
    // dataset)
    // so we permute the dimensions
    // before we set the iterator.
    public static void setIterators(ArrayStructureMA ama) {
        StructureMembers sms = ama.getStructureMembers();
        for (StructureMembers.Member sm : sms.getMembers()) {
            Array data = sm.getDataArray();
            if (data instanceof ArrayStructureMA) {
                setIterators((ArrayStructureMA) data);

            } else {
                int[] shape = data.getShape();
                if ((shape.length > 1) && (sm.getDataType() != DataType.CHAR)) {
                    Array datap;
                    if (shape.length == 2)
                        datap = data.transpose(0, 1);
                    else {
                        int[] pdims = new int[shape.length]; // (0,1,2,3...) -> (1,2,3...,0)
                        for (int i = 0; i < shape.length - 1; i++)
                            pdims[i] = i + 1;
                        datap = data.permute(pdims);
                    }
                    sm.setDataObject(datap.getIndexIterator());
                } else {
                    sm.setDataObject(data.getIndexIterator());
                }
            }
        }
    }

    private void associateMessage2Members(StructureMembers members, DataDescriptor parent,
                                          HashMap<DataDescriptor, StructureMembers.Member> map) {
        for (DataDescriptor dkey : parent.getSubKeys()) {
            if (dkey.name == null) {
                if (dkey.getSubKeys() != null)
                    associateMessage2Members(members, dkey, map);
                continue;
            }
            StructureMembers.Member m = members.findMember(dkey.name);
            if (m != null) {
                map.put(dkey, m);

                if (m.getDataType() == DataType.STRUCTURE) {
                    ArrayStructure nested = (ArrayStructure) m.getDataArray();
                    if (dkey.getSubKeys() != null)
                        associateMessage2Members(nested.getStructureMembers(), dkey, map);
                }

            } else {
                if (dkey.getSubKeys() != null)
                    associateMessage2Members(members, dkey, map);
            }
        }
    }

    // read / count the bits in a compressed message
    private int readData(Message m, RandomAccessFile raf, Formatter f, Request req) throws IOException {

        BitReader reader = new BitReader(raf, m.dataSection.getDataPos() + 4);
        DataDescriptor root = m.getRootDataDescriptor();
        if (root.isBad)
            return 0;

        DebugOut out = (f == null) ? null : new DebugOut(f);
        BitCounterCompressed[] counterFlds = new BitCounterCompressed[root.subKeys.size()]; // one for each field LOOK why
        // not m.counterFlds ?
        readData(out, reader, counterFlds, root, 0, m.getNumberDatasets(), req);

        m.msg_nbits = 0;
        for (BitCounterCompressed counter : counterFlds)
            if (counter != null)
                m.msg_nbits += counter.getTotalBits();
        return m.msg_nbits;
    }

    /**
     * @param out         debug info; may be null
     * @param reader      raf wrapper for bit reading
     * @param fldCounters one for each field
     * @param parent      parent.subkeys() holds the fields
     * @param bitOffset   bit offset from beginning of data
     * @param ndatasets   number of compressed datasets
     * @param req         for writing into the ArrayStructure;
     * @return bitOffset
     * @throws IOException on read error
     */
    private int readData(DebugOut out, BitReader reader, BitCounterCompressed[] fldCounters, DataDescriptor parent,
                         int bitOffset, int ndatasets, Request req) throws IOException {

        List<DataDescriptor> flds = parent.getSubKeys();
        for (int fldidx = 0; fldidx < flds.size(); fldidx++) {
            DataDescriptor dkey = flds.get(fldidx);
            if (!dkey.isOkForVariable()) { // dds with no data to read

                // the dpi nightmare
                if ((dkey.f == 2) && (dkey.x == 36)) {
                    req.dpiTracker = new DpiTracker(dkey.dpi, dkey.dpi.getNfields());
                }

                if (out != null)
                    out.f.format("%s %d %s (%s) %n", out.indent(), out.fldno++, dkey.name, dkey.getFxyName());
                continue;
            }

            BitCounterCompressed counter = new BitCounterCompressed(dkey, ndatasets, bitOffset);
            fldCounters[fldidx] = counter;

            // sequence
            if (dkey.replication == 0) {
                reader.setBitOffset(bitOffset);
                int count = (int) reader.bits2UInt(dkey.replicationCountSize);
                bitOffset += dkey.replicationCountSize;

                reader.bits2UInt(6);
                if (null != out)
                    out.f.format("%s--sequence %s bitOffset=%d replication=%s %n", out.indent(), dkey.getFxyName(), bitOffset,
                            count);
                bitOffset += 6; // LOOK seems to be an extra 6 bits.

                counter.addNestedCounters(count);

                // make an ArrayObject of ArraySequence, place it into the data array
                bitOffset = makeArraySequenceCompressed(out, reader, counter, dkey, bitOffset, ndatasets, count, req);
                // if (null != out) out.f.format("--back %s %d %n", dkey.getFxyName(), bitOffset);
                continue;
            }

            // structure
            if (dkey.type == 3) {
                if (null != out)
                    out.f.format("%s--structure %s bitOffset=%d replication=%s %n", out.indent(), dkey.getFxyName(), bitOffset,
                            dkey.replication);

                // p 11 of "standard", doesnt really describe the case of replication AND compression
                counter.addNestedCounters(dkey.replication);
                for (int i = 0; i < dkey.replication; i++) {
                    BitCounterCompressed[] nested = counter.getNestedCounters(i);
                    req.outerRow = i;
                    if (null != out) {
                        out.f.format("%n");
                        out.indent.incr();
                        bitOffset = readData(out, reader, nested, dkey, bitOffset, ndatasets, req);
                        out.indent.decr();
                    } else {
                        bitOffset = readData(null, reader, nested, dkey, bitOffset, ndatasets, req);
                    }
                }
                // if (null != out) out.f.format("--back %s %d %n", dkey.getFxyName(), bitOffset);

                continue;
            }

            // all other fields

            StructureMembers.Member member;
            IndexIterator iter = null;
            ArrayStructure dataDpi = null; // if iter is missing - for the dpi case
            if (req.map != null) {
                member = req.map.get(dkey);
                iter = (IndexIterator) member.getDataObject();
                if (iter == null) {
                    dataDpi = (ArrayStructure) member.getDataArray();
                }
            }

            reader.setBitOffset(bitOffset); // ?? needed ??

            // char data special case
            if (dkey.type == 1) {
                int nc = dkey.bitWidth / 8;
                byte[] minValue = new byte[nc];
                for (int i = 0; i < nc; i++)
                    minValue[i] = (byte) reader.bits2UInt(8);
                int dataWidth = (int) reader.bits2UInt(6); // incremental data width in bytes
                counter.setDataWidth(8 * dataWidth);
                int totalWidth = dkey.bitWidth + 6 + 8 * dataWidth * ndatasets; // total width in bits for this compressed set
                // of values
                bitOffset += totalWidth; // bitOffset now points to the next field

                if (null != out)
                    out.f.format("%s read %d %s (%s) bitWidth=%d defValue=%s dataWidth=%d n=%d bitOffset=%d %n", out.indent(),
                            out.fldno++, dkey.name, dkey.getFxyName(), dkey.bitWidth, new String(minValue, StandardCharsets.UTF_8),
                            dataWidth, ndatasets, bitOffset);

                if (iter != null) {
                    for (int dataset = 0; dataset < ndatasets; dataset++) {
                        if (dataWidth == 0) { // use the min value
                            if (req.wantRow(dataset))
                                for (int i = 0; i < nc; i++)
                                    iter.setCharNext((char) minValue[i]); // ??

                        } else { // read the incremental value
                            int nt = Math.min(nc, dataWidth);
                            byte[] incValue = new byte[nc];
                            for (int i = 0; i < nt; i++)
                                incValue[i] = (byte) reader.bits2UInt(8);
                            for (int i = nt; i < nc; i++) // can dataWidth < n ?
                                incValue[i] = 0;

                            if (req.wantRow(dataset))
                                for (int i = 0; i < nc; i++) {
                                    int cval = incValue[i];
                                    if (cval < 32 || cval > 126)
                                        cval = 0; // printable ascii KLUDGE!
                                    iter.setCharNext((char) cval); // ??
                                }
                            if (out != null)
                                out.f.format(" %s,", new String(incValue, StandardCharsets.UTF_8));
                        }
                    }
                }
                if (out != null)
                    out.f.format("%n");
                continue;
            }

            // numeric fields
            int useBitWidth = dkey.bitWidth;

            // a dpi Field needs to be substituted
            boolean isDpi = ((dkey.f == 0) && (dkey.x == 31) && (dkey.y == 31));
            boolean isDpiField = false;
            if ((dkey.f == 2) && (dkey.x == 24) && (dkey.y == 255)) {
                isDpiField = true;
                DataDescriptor dpiDD = req.dpiTracker.getDpiDD(req.outerRow);
                useBitWidth = dpiDD.bitWidth;
            }

            long dataMin = reader.bits2UInt(useBitWidth);
            int dataWidth = (int) reader.bits2UInt(6); // increment data width - always in 6 bits, so max is 2^6 = 64
            if (dataWidth > useBitWidth && (null != out))
                out.f.format(" BAD WIDTH ");
            if (dkey.type == 1)
                dataWidth *= 8; // char data count is in bytes
            counter.setDataWidth(dataWidth);

            int totalWidth = useBitWidth + 6 + dataWidth * ndatasets; // total width in bits for this compressed set of values
            bitOffset += totalWidth; // bitOffset now points to the next field

            if (null != out)
                out.f.format("%s read %d, %s (%s) bitWidth=%d dataMin=%d (%f) dataWidth=%d n=%d bitOffset=%d %n", out.indent(),
                        out.fldno++, dkey.name, dkey.getFxyName(), useBitWidth, dataMin, dkey.convert(dataMin), dataWidth,
                        ndatasets, bitOffset);

            // numeric fields

            // if dataWidth == 0, just use min value, otherwise read the compressed value here
            for (int dataset = 0; dataset < ndatasets; dataset++) {
                long value = dataMin;

                if (dataWidth > 0) {
                    long cv = reader.bits2UInt(dataWidth);
                    if (BufrNumbers.isMissing(cv, dataWidth))
                        value = BufrNumbers.missingValue(useBitWidth); // set to missing value
                    else // add to minimum
                        value += cv;
                }

                // workaround for malformed messages
                if (dataWidth > useBitWidth) {
                    long missingVal = BufrNumbers.missingValue(useBitWidth);
                    if ((value & missingVal) != value) // overflow
                        value = missingVal; // replace with missing value
                }

                if (req.wantRow(dataset)) {
                    if (isDpiField) {
                        if (dataDpi != null) {
                            DataDescriptor dpiDD = req.dpiTracker.getDpiDD(req.outerRow);
                            StructureMembers sms = dataDpi.getStructureMembers();
                            StructureMembers.Member m0 = sms.getMember(0);
                            IndexIterator iter2 = (IndexIterator) m0.getDataObject();
                            iter2.setObjectNext(dpiDD.getName());

                            StructureMembers.Member m1 = sms.getMember(1);
                            iter2 = (IndexIterator) m1.getDataObject();
                            iter2.setFloatNext(dpiDD.convert(value));
                        }
                    } else if (iter != null) {
                        iter.setLongNext(value);
                    }
                }
                // since dpi must be the same for all datasets, just keep the first one
                if (isDpi && (dataset == 0))
                    req.dpiTracker.setDpiValue(req.outerRow, value); // keep track of dpi values in the tracker - perhaps not
                // expose

                if ((out != null) && (dataWidth > 0))
                    out.f.format(" %d (%f)", value, dkey.convert(value));
            }
            if (out != null)
                out.f.format("%n");
        }

        return bitOffset;
    }

    // read in the data into an ArrayStructureMA, holding an ArrayObject() of ArraySequence
    private int makeArraySequenceCompressed(DebugOut out, BitReader reader, BitCounterCompressed bitCounterNested,
                                            DataDescriptor seqdd, int bitOffset, int ndatasets, int count, Request req) throws IOException {

        // construct ArrayStructureMA and associated map
        ArrayStructureMA ama = null;
        StructureMembers members = null;
        HashMap<DataDescriptor, StructureMembers.Member> nmap = null;
        if (req.map != null) {
            Sequence seq = (Sequence) seqdd.refersTo;
            int[] shape = {ndatasets, count}; // seems unlikely this can handle recursion
            ama = ArrayStructureMA.factoryMA(seq, shape);
            setIterators(ama);

            members = ama.getStructureMembers();
            nmap = new HashMap<>(2 * members.getMembers().size());
            associateMessage2Members(members, seqdd, nmap);
        }
        Request nreq = new Request(ama, nmap, req.r);

        // iterate over the number of replications, reading ndataset compressed values at each iteration
        if (out != null)
            out.indent.incr();
        for (int i = 0; i < count; i++) {
            BitCounterCompressed[] nested = bitCounterNested.getNestedCounters(i);
            nreq.outerRow = i;
            bitOffset = readData(out, reader, nested, seqdd, bitOffset, ndatasets, nreq);
        }
        if (out != null)
            out.indent.decr();

        // add ArraySequence to the ArrayObject in the outer structure
        if (req.map != null) {
            StructureMembers.Member m = req.map.get(seqdd);
            ArrayObject arrObj = (ArrayObject) m.getDataArray();

            // we need to break ama into separate sequences, one for each dataset
            int start = 0;
            for (int i = 0; i < ndatasets; i++) {
                ArraySequence arrSeq = new ArraySequence(members, new SequenceIterator(start, count, ama), count);
                arrObj.setObject(i, arrSeq);
                start += count;
            }
        }

        return bitOffset;
    }

    private static class DpiTracker {
        DataDescriptorTreeConstructor.DataPresentIndicator dpi;
        boolean[] isPresent;
        List<DataDescriptor> dpiDD;

        DpiTracker(DataDescriptorTreeConstructor.DataPresentIndicator dpi, int nPresentFlags) {
            this.dpi = dpi;
            isPresent = new boolean[nPresentFlags];
        }

        void setDpiValue(int fldidx, long value) {
            isPresent[fldidx] = (value == 0); // present if the value is zero
        }

        DataDescriptor getDpiDD(int fldPresentIndex) {
            if (dpiDD == null) {
                dpiDD = new ArrayList<>();
                for (int i = 0; i < isPresent.length; i++) {
                    if (isPresent[i])
                        dpiDD.add(dpi.linear.get(i));
                }
            }
            return dpiDD.get(fldPresentIndex);
        }

        boolean isDpiDDs(DataDescriptor dkey) {
            return (dkey.f == 2) && (dkey.x == 24) && (dkey.y == 255);
        }

        boolean isDpiField(DataDescriptor dkey) {
            return (dkey.f == 2) && (dkey.x == 24) && (dkey.y == 255);
        }

    }
}
