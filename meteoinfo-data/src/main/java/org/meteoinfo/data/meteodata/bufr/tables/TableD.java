/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr.tables;

import java.util.*;

/**
 * BUFR Table D - Data sequences
 *
 * @author caron
 * @since Sep 25, 2008
 */
public class TableD {
    private String name;
    private String location;
    private Map<Short, Descriptor> map;

    public TableD(String name, String location) {
        this.name = name;
        this.location = location;
        map = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Descriptor addDescriptor(short x, short y, String name, List<Short> seq) {
        short id = (short) ((3 << 14) + (x << 8) + y);
        Descriptor d = new Descriptor(x, y, name, seq);
        map.put(id, d);
        return d;
    }

    public Descriptor getDescriptor(short id) {
        return map.get(id);
    }

    public Collection<Descriptor> getDescriptors() {
        return map.values();
    }

    public void show(Formatter out) {
        Collection<Short> keys = map.keySet();
        List<Short> sortKeys = new ArrayList<>(keys);
        Collections.sort(sortKeys);

        out.format("Table D %s %n", name);
        for (Short key : sortKeys) {
            Descriptor dd = map.get(key);
            dd.show(out, true);
        }
    }

    public static class Descriptor implements Comparable<Descriptor> {
        private short x, y;
        private String name;
        private List<Short> seq;
        private boolean localOverride;

        Descriptor(short x, short y, String name, List<Short> seq) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.seq = seq;
        }

        public List<Short> getSequence() {
            return seq;
        }

        public void addFeature(short f) {
            seq.add(f);
        }

        public String getName() {
            return name;
        }

        /**
         * Get fxy as a short
         *
         * @return fxy encoded as a short
         */
        public short getId() {
            return (short) ((3 << 14) + (x << 8) + y);
        }

        /**
         * Get fxy as a String, eg 3-4-22
         *
         * @return fxy encoded as a String
         */
        public String getFxy() {
            return "3-" + x + "-" + y;
        }

        public String toString() {
            return getFxy() + " " + getName();
        }

        public void show(Formatter out, boolean oneline) {
            out.format(" %8s: name=(%s) seq=", getFxy(), name);
            if (oneline) {
                for (short s : seq)
                    out.format(" %s,", ucar.nc2.iosp.bufr.Descriptor.makeString(s));
                out.format("%n");
            } else {
                for (short s : seq)
                    out.format("    %s%n", ucar.nc2.iosp.bufr.Descriptor.makeString(s));
            }
        }

        @Override
        public int compareTo(Descriptor o) {
            return getId() - o.getId();
        }


        public boolean isLocal() {
            return ((x >= 48) || (y >= 192));
        }

        public void setLocalOverride(boolean isOverride) {
            this.localOverride = isOverride;
        }

        public boolean getLocalOverride() {
            return localOverride;
        }

    }
}
