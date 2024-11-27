/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;


import org.meteoinfo.data.meteodata.bufr.tables.TableD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Convert a list of data descriptors to a tree of DataDescriptor objects.
 * Expand Table D, process table C operators.
 *
 * @author caron
 * @since Jul 14, 2008
 */
public class DataDescriptorTreeConstructor {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataDescriptorTreeConstructor.class);

    //////////////////////////////////////////////////////////////////////////////////
    private DataDescriptor root;

    public DataDescriptor factory(BufrTableLookup lookup, BufrDataDescriptionSection dds) {
        root = new DataDescriptor();

        // convert ids to DataDescriptor
        List<DataDescriptor> keys = decode(dds.getDataDescriptors(), lookup);

        // deal with f3-60-4
        keys = preflatten(keys);

        // try to find useful struct names
        grabCompoundNames(keys);

        // make replicated keys into subKeys, constituting a tree
        List<DataDescriptor> tree = replicate(keys);

        // flatten the compounds
        root.subKeys = new ArrayList<>();
        flatten(root.subKeys, tree);

        // process the operators
        operate(root.subKeys);

        // count the size
        root.total_nbits = root.countBits();

        return root;
    }

    // convert ids to DataDescriptors, expand table D
    private List<DataDescriptor> decode(List<Short> keyDesc, BufrTableLookup lookup) {
        if (keyDesc == null)
            return null;

        List<DataDescriptor> keys = new ArrayList<>();
        for (short id : keyDesc) {
            DataDescriptor dd = new DataDescriptor(id, lookup);
            keys.add(dd);
            if (dd.f == 3) {
                TableD.Descriptor tdd = lookup.getDescriptorTableD(dd.fxy);
                if (tdd == null || tdd.getSequence() == null) {
                    dd.bad = true;
                } else {
                    dd.name = tdd.getName();
                    dd.subKeys = decode(tdd.getSequence(), lookup);
                }
            }
        }
        return keys;
    }

    // look for replication, move replicated items into subtree
    private List<DataDescriptor> replicate(List<DataDescriptor> keys) {
        List<DataDescriptor> tree = new ArrayList<>();
        Iterator<DataDescriptor> dkIter = keys.iterator();
        while (dkIter.hasNext()) {
            DataDescriptor dk = dkIter.next();
            if (dk.f == 1) {
                dk.subKeys = new ArrayList<>();
                dk.replication = dk.y; // replication count

                if (dk.replication == 0) { // delayed replication
                    root.isVarLength = true; // variable sized data == deferred replication == sequence data

                    // the next one is the replication count size : does not count in field count (x)
                    DataDescriptor replication = dkIter.next();

                    // see https://github.com/Unidata/netcdf-java/issues/1282
                    if (replication.x == 31)
                        dk.replicationCountSize = replication.bitWidth;
                        // Not sure about the following hard codes values and if the previous condition (replication.x == 31) already
                        // captures those cases automatically. Ideally need an expert for BUFR to look over these.
                    else if (replication.y == 0)
                        dk.replicationCountSize = 1; // ??
                    else if (replication.y == 1)
                        dk.replicationCountSize = 8;
                    else if (replication.y == 2)
                        dk.replicationCountSize = 16;
                    else if (replication.y == 11)
                        dk.repetitionCountSize = 8;
                    else if (replication.y == 12)
                        dk.repetitionCountSize = 16;
                    else
                        log.error("Unknown replication type= " + replication);
                }

                // transfer to the subKey list
                for (int j = 0; j < dk.x && dkIter.hasNext(); j++) {
                    dk.subKeys.add(dkIter.next());
                }

                // recurse
                dk.subKeys = replicate(dk.subKeys);

            } else if ((dk.f == 3) && (dk.subKeys != null)) {
                dk.subKeys = replicate(dk.subKeys); // do at all levels
            }

            tree.add(dk);
        }

        return tree;
    }

    /*
     * Use case:
     * 3-62-1 : HEADR
     * 0-4-194 : FORECAST TIME
     * 0-1-205 : STATION NUMBER -- 6 DIGITS
     * 0-1-198 : REPORT IDENTIFIER
     * 0-5-2 : Latitude (coarse accuracy)
     * 0-6-2 : Longitude (coarse accuracy)
     * 0-10-194: GRID-POINT ELEVATION
     * 0-2-196 : CLASS OF PROFILE OUTPUT
     * 3-60-2 :
     * 1-01-000: replication
     * 0-31-1 : Delayed descriptor replication factor
     * 3-62-2 : PROFILE
     * 0-10-4 : Pressure
     * 0-12-1 : Temperature/dry-bulb temperature
     * 0-11-3 : u-component
     *
     * where the 3-62-2 should be replicated.
     * This is from NCEP bufrtab.ETACLS1. Not sure if others use this idiom.
     *
     * Use case 2:
     * not just top level
     * 3-61-37 : TMPSQ1 SYNOPTIC REPORT TEMPERATURE DATA
     * 0-33-193: QMAT
     * 0-12-101: TMDB
     * 0-33-194: QMDD
     * 0-12-103: TMDP
     * 0-2-38 : MSST
     * 0-33-218: QMST
     * 0-22-43 : SST1
     * 3-60-4 : DRP1BIT
     * 1-01-000: replication
     * 0-31-0 : DRF1BIT
     * 3-61-38 : TMPSQ2 SYNOPTIC REPORT WET BULB TEMPERATURE DATA
     * 0-2-39 : MWBT
     * 0-12-102: TMWB
     * 0-13-3 : REHU
     * 3-60-4 : DRP1BIT
     * 1-01-000: replication
     * 0-31-0 : DRF1BIT
     * 3-61-39 : TMPSQ3 SYNOPTIC REPORT MAXIMUM AND MINIMUM TEMPERATURE DATA
     * 0-4-31 : DTH
     * 0-12-111: MXTM
     * 0-4-31 : DTH
     * 0-12-112: MITM
     *
     * I think that a 3-60-4 should just be flattened:
     * 3-61-37 : TMPSQ1 SYNOPTIC REPORT TEMPERATURE DATA
     * 0-33-193: QMAT
     * 0-12-101: TMDB
     * 0-33-194: QMDD
     * 0-12-103: TMDP
     * 0-2-38 : MSST
     * 0-33-218: QMST
     * 0-22-43 : SST1
     * 1-01-000: replication
     * 0-31-0 : DRF1BIT
     * 3-61-38 : TMPSQ2 SYNOPTIC REPORT WET BULB TEMPERATURE DATA
     * 0-2-39 : MWBT
     * 0-12-102: TMWB
     * 0-13-3 : REHU
     * 1-01-000: replication
     * 0-31-0 : DRF1BIT
     * 3-61-39 : TMPSQ3 SYNOPTIC REPORT MAXIMUM AND MINIMUM TEMPERATURE DATA
     * 0-4-31 : DTH
     * 0-12-111: MXTM
     * 0-4-31 : DTH
     * 0-12-112: MITM
     */

    // LOOK this is NCEP specific !!
    static boolean isNcepDRP(DataDescriptor key) {
        return key.f == 3 && key.x == 60;
    }

    private List<DataDescriptor> preflatten(List<DataDescriptor> tree) {
        if (tree == null)
            return null;

        // do we need to flatten, ie have f3604 ??
        boolean flatten = false;
        for (DataDescriptor key : tree) {
            if (isNcepDRP(key))
                flatten = true;
        }

        if (flatten) {
            List<DataDescriptor> result = new ArrayList<>(tree.size());
            for (DataDescriptor key : tree) {
                if (isNcepDRP(key)) {
                    result.addAll(key.subKeys); // remove f3604
                } else {
                    result.add(key); // leave others
                }
            }
            tree = result;
        }

        // recurse
        for (DataDescriptor key : tree) {
            key.subKeys = preflatten(key.subKeys);
        }

        return tree;
    }

    /*
     * try to grab names of compounds (structs)
     * if f=1 is followed by f=3, eg:
     * 0-40-20 : GQisFlagQualDetailed - Quality flag for the system
     * 1-01-010: replication
     * 3-40-2 : (IASI Level 1c band description)
     * 0-25-140: Start channel
     * 0-25-141: End channel
     * 0-25-142: Channel scale factor
     * 1-01-087: replication
     * 3-40-3 : (IASI Level 1c 100 channels)
     * 1-04-100: replication
     * 2-01-136: Operator= change data width
     * 0-5-42 : Channel number
     * 2-01-000: Operator= change data width
     * 0-14-46 : Scaled IASI radiance
     * 0-2-19 : Satellite instruments
     * 0-25-51 : AVHRR channel combination
     * 1-01-007: replication
     * 3-40-4 : (IASI Level 1c AVHRR single scene)
     * 0-5-60 : Y angular position from centre of gravity
     * 0-5-61 : Z angular position from centre of gravity
     * 0-25-85 : Fraction of clear pixels in HIRS FOV
     * ...
     *
     * sequence:
     * 3-60-4 : DRP1BIT
     * 1-01-000: replication
     * 0-31-0 : DRF1BIT
     * 3-61-38 : TMPSQ2 SYNOPTIC REPORT WET BULB TEMPERATURE DATA
     * 0-2-39 : MWBT
     * 0-12-102: TMWB
     * 0-13-3 : REHU
     *
     * which has been preflattened into:
     *
     * 1-01-000: replication
     * 0-31-0 : DRF1BIT
     * 3-61-38 : TMPSQ2 SYNOPTIC REPORT WET BULB TEMPERATURE DATA
     * 0-2-39 : MWBT
     * 0-12-102: TMWB
     * 0-13-3 : REHU
     *
     *
     */
    private void grabCompoundNames(List<DataDescriptor> tree) {

        for (int i = 0; i < tree.size(); i++) {
            DataDescriptor key = tree.get(i);
            if (key.bad)
                continue;

            if ((key.f == 3) && (key.subKeys != null)) {
                grabCompoundNames(key.subKeys);

            } else if (key.f == 1 && key.x == 1 && i < tree.size() - 1) { // replicator with 1 element
                DataDescriptor nextKey = tree.get(i + 1);
                if (nextKey.f == 3) { // the one element is a compound
                    if (nextKey.name != null && !nextKey.name.isEmpty())
                        key.name = nextKey.name;

                } else if (key.y == 0 && i < tree.size() - 2) { // seq has an extra key before the 3
                    DataDescriptor nnKey = tree.get(i + 2);
                    if (nnKey.f == 3)
                        if (nnKey.name != null && !nnKey.name.isEmpty())
                            key.name = nnKey.name;
                }
            }
        }
    }


    // flatten the compounds (type 3); but dont remove bad ones
    private void flatten(List<DataDescriptor> result, List<DataDescriptor> tree) {

        for (DataDescriptor key : tree) {
            if (key.bad) {
                root.isBad = true;
                result.add(key); // add it anyway so we can see it in debug
                continue;
            }

            if ((key.f == 3) && (key.subKeys != null)) {
                flatten(result, key.subKeys);

            } else if (key.f == 1) { // flatten the subtrees
                List<DataDescriptor> subTree = new ArrayList<>();
                flatten(subTree, key.subKeys);
                key.subKeys = subTree;
                result.add(key);

            } else {
                result.add(key);
            }
        }
    }

    private DataDescriptor changeWidth; // 02 01 Y
    private DataDescriptor changeScale; // 02 02 Y
    private DataDescriptor changeRefval; // 02 03 Y
    private DataDescriptor changeWtf; // 02 07 Y
    private DataPresentIndicator dpi; // assume theres only one in effect at a time

    private void operate(List<DataDescriptor> tree) {
        if (tree == null)
            return;
        boolean hasAssFields = false;
        // boolean hasDpiFields = false;
        DataDescriptor.AssociatedField assField = null; // 02 04 Y

        Iterator<DataDescriptor> iter = tree.iterator();
        while (iter.hasNext()) {
            DataDescriptor dd = iter.next();

            if (dd.f == 2) {
                if (dd.x == 1) {
                    changeWidth = (dd.y == 0) ? null : dd;
                    iter.remove();

                } else if (dd.x == 2) {
                    changeScale = (dd.y == 0) ? null : dd;
                    iter.remove();
                    // throw new UnsupportedOperationException("2-2-Y (change scale)");

                } else if (dd.x == 3) {
                    changeRefval = (dd.y == 255) ? null : dd;
                    iter.remove();
                    // throw new UnsupportedOperationException("2-3-Y (change reference values)"); // untested - no examples

                } else if (dd.x == 4) {
                    assField = (dd.y == 0) ? null : new DataDescriptor.AssociatedField(dd.y);
                    iter.remove();
                    hasAssFields = true;

                } else if (dd.x == 5) { // char data - this allows arbitrary string to be inserted
                    dd.type = 1; // String
                    dd.bitWidth = dd.y * 8;
                    dd.name = "Note";

                } else if (dd.x == 6) {
                    // see L3-82 (3.1.6.5)
                    // "Y bits of data are described by the immediately following descriptor". could they speak English?
                    iter.remove();
                    if ((dd.y != 0) && iter.hasNext()) { // fnmoc using 2-6-0 as cancel (apparently)
                        DataDescriptor next = iter.next();
                        next.bitWidth = dd.y; // LOOK should it be dd.bitWidth??
                    }

                } else if (dd.x == 7) {
                    changeWtf = (dd.y == 0) ? null : dd;
                    iter.remove();

                } else if (dd.x == 36) {
                    if (iter.hasNext()) {
                        DataDescriptor dpi_dd = iter.next(); // this should be a replicated data present field
                        dpi = new DataPresentIndicator(tree, dpi_dd);
                        dd.dpi = dpi;
                        dpi_dd.dpi = dpi;
                    }

                } else if ((dd.x == 37) && (dd.y == 255)) { // cancel dpi
                    dpi = null;

                } else if ((dd.x == 24) && (dd.y == 255)) {
                    dd.dpi = dpi;
                }

            } else if (dd.subKeys != null) {
                operate(dd.subKeys);

            } else if (dd.f == 0) {

                if (dd.type != 3) { // numeric or string or enum, not compound
                    if (changeWidth != null)
                        dd.bitWidth += changeWidth.y - 128;
                    if (changeScale != null)
                        dd.scale += changeScale.y - 128;
                    if (changeRefval != null)
                        dd.refVal += changeRefval.y - 128; // LOOK wrong

                    if (changeWtf != null && dd.type == 0) {
                        // see I.2 – BUFR Table C — 4
                        // For Table B elements, which are not CCITT IA5 (character data), code tables, or flag tables:
                        // 1. Add Y to the existing scale factor
                        // 2. Multiply the existing reference value by 10 Y
                        // 3. Calculate ((10 x Y) + 2) ÷ 3, disregard any fractional remainder and add the result to the existing
                        // bit width.
                        // HAHAHAHAHAHAHAHA
                        int y = changeWtf.y;
                        dd.scale += y;
                        dd.refVal *= Math.pow(10, y);
                        int wtf = ((10 * y) + 2) / 3;
                        dd.bitWidth += wtf;
                    }
                }

                if (assField != null) {
                    assField.nfields++;
                    dd.assField = assField;
                    assField.dataFldName = dd.name;
                }
            }
        }

        if (hasAssFields)
            addAssFields(tree);
        // if (hasDpiFields) addDpiFields(tree);
    }

    private void addAssFields(List<DataDescriptor> tree) {
        if (tree == null)
            return;

        int index = 0;
        while (index < tree.size()) {
            DataDescriptor dd = tree.get(index);
            if (dd.assField != null) {
                DataDescriptor.AssociatedField assField = dd.assField;

                if ((dd.f == 0) && (dd.x == 31) && (dd.y == 21)) { // the meaning field
                    dd.name = assField.dataFldName + "_associated_field_significance";
                    dd.assField = null;

                } else {
                    DataDescriptor assDD = dd.makeAssociatedField(assField.nbits);
                    tree.add(index, assDD);
                    index++;
                }
            }

            index++;
        }
    }

    static class DataPresentIndicator {
        DataDescriptor dataPresent; // replication of bit present field
        List<DataDescriptor> linear; // linear list of dds

        DataPresentIndicator(List<DataDescriptor> tree, DataDescriptor dpi_dd) {
            this.dataPresent = dpi_dd;
            linear = new ArrayList<>();
            linearize(tree);
        }

        int getNfields() {
            return dataPresent.replication;
        }

        private void linearize(List<DataDescriptor> tree) {
            for (DataDescriptor dd : tree) {
                if (dd.f == 0) {
                    linear.add(dd);

                } else if (dd.f == 1) {
                    for (int i = 0; i < dd.replication; i++) // whut about defered replication hahahahahah
                        linearize(dd.getSubKeys());
                }
            }
        }
    }

}
