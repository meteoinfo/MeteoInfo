/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import ucar.nc2.util.Indent;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Counts the size of nested tables, for uncompressed messages.
 * <p>
 * A top-level BitCounterUncompressed counts bits for one row = obs = dataset.
 * obs = new BitCounterUncompressed(root, 1, 0);
 *
 * @author caron
 * @since May 10, 2008
 */
public class BitCounterUncompressed implements BitCounter {
    private final DataDescriptor parent; // represents the table - fields/cols are the subKeys of dkey
    private final int nrows; // number of rows in this table
    private final int replicationCountSize; // number of bits taken up by the count variable (non-zero only for sequences)

    private Map<DataDescriptor, Integer> bitPosition;
    private Map<DataDescriptor, BitCounterUncompressed[]> subCounters; // nested tables; null for regular fields
    private int[] startBit; // from start of data section, for each row
    private int countBits; // total nbits in this table
    private int bitOffset; // count bits

    private static boolean debug;

    /**
     * This counts the size of an array of Structures or Sequences, ie Structure(n)
     *
     * @param parent               is a structure or a sequence - so has subKeys
     * @param nrows                numbers of rows in the table, equals 1 for top level
     * @param replicationCountSize number of bits taken up by the count variable (non-zero only for sequences)
     */
    BitCounterUncompressed(DataDescriptor parent, int nrows, int replicationCountSize) {
        this.parent = parent;
        this.nrows = nrows;
        this.replicationCountSize = replicationCountSize;
    }

    // not used yet
    public void setBitOffset(DataDescriptor dkey) {
        if (bitPosition == null)
            bitPosition = new HashMap<>(2 * parent.getSubKeys().size());
        bitPosition.put(dkey, bitOffset);
        bitOffset += dkey.getBitWidth();
    }

    public int getOffset(DataDescriptor dkey) {
        return bitPosition.get(dkey);
    }


    /**
     * Track nested Tables.
     *
     * @param subKey               subKey is a structure or a sequence - so itself has subKeys
     * @param n                    numbers of rows in the nested table
     * @param row                  which row in the parent Table this belongs to
     * @param replicationCountSize number of bits taken up by the count (non-zero for sequences)
     * @return nested ReplicationCounter
     */
    BitCounterUncompressed makeNested(DataDescriptor subKey, int n, int row, int replicationCountSize) {
        if (subCounters == null)
            subCounters = new HashMap<>(5); // assumes DataDescriptor.equals is ==

        // one for each row in this table
        BitCounterUncompressed[] subCounter = subCounters.computeIfAbsent(subKey, k -> new BitCounterUncompressed[nrows]);

        BitCounterUncompressed rc = new BitCounterUncompressed(subKey, n, replicationCountSize);
        subCounter[row] = rc;

        return rc;
    }

    public BitCounterUncompressed[] getNested(DataDescriptor subKey) {
        return (subCounters == null) ? null : subCounters.get(subKey);
    }

    // total bits of this table and all subtables
    int countBits(int startBit) {
        countBits = replicationCountSize;
        this.startBit = new int[nrows];

        for (int i = 0; i < nrows; i++) {
            this.startBit[i] = startBit + countBits;
            if (debug)
                System.out.println(" BitCounterUncompressed row " + i + " startBit=" + this.startBit[i]);

            for (DataDescriptor nd : parent.subKeys) {
                BitCounterUncompressed[] bitCounter = (subCounters == null) ? null : subCounters.get(nd);
                if (bitCounter == null) // a regular field
                    countBits += nd.getBitWidth();
                else {
                    if (debug)
                        System.out.println(" ---------> nested " + nd.getFxyName() + " starts at =" + (startBit + countBits));
                    countBits += bitCounter[i].countBits(startBit + countBits);
                    if (debug)
                        System.out.println(" <--------- nested " + nd.getFxyName() + " ends at =" + (startBit + countBits));
                }
            }
        }
        return countBits;
    }

    public int getCountBits() {
        return countBits;
    }

    public int getNumberRows() {
        return nrows;
    }

    public int getStartBit(int row) {
        if (row >= startBit.length)
            throw new IllegalStateException();
        return startBit[row];
    }

    public void toString(Formatter f, Indent indent) {
        f.format("%s dds=%s, ", indent, parent.getFxyName());
        f.format("nrows=%d%n", nrows);
        if (subCounters == null)
            return;

        indent.incr();
        int count = 0;

        // Map<DataDescriptor, BitCounterUncompressed[]> subCounters; // nested tables; null for regular fields
        for (BitCounterUncompressed[] bcus : subCounters.values()) {
            if (bcus == null)
                f.format("%s%d: null", indent, count);
            else {
                for (BitCounterUncompressed bcu : bcus)
                    bcu.toString(f, indent);
            }
            count++;
        }
        indent.decr();
    }
}
