/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr.tables;

import org.meteoinfo.data.meteodata.bufr.DataDescriptor;

import javax.annotation.concurrent.Immutable;
import java.util.*;

/**
 * BUFR Table B - Data descriptors
 *
 * @author caron
 * @since Sep 25, 2008
 */
public class TableB {
    private final String name;
    private final String location;
    private final Map<Short, Descriptor> map;

    public TableB(String name, String location) {
        this.name = name;
        this.location = location;
        map = new HashMap<>();
    }

    public void addDescriptor(short x, short y, int scale, int refVal, int width, String name, String units,
                              String desc) {
        short id = (short) ((x << 8) + y);
        map.put(id, new Descriptor(x, y, scale, refVal, width, name, units, desc));
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Descriptor getDescriptor(short id) {
        return map.get(id);
    }

    public Collection<Descriptor> getDescriptors() {
        return map.values();
    }

    public Collection<Short> getKeys() {
        return map.keySet();
    }

    public void show(Formatter out) {
        List<Short> sortKeys = new ArrayList<>(getKeys());
        Collections.sort(sortKeys);

        out.format("Table B %s %n", name);
        for (Short key : sortKeys) {
            Descriptor dd = getDescriptor(key);
            if (dd != null)
                dd.show(out);
            out.format("%n");
        }
    }

    /**
     * Composite pattern - collection of TableB
     */
    public static class Composite extends TableB {
        List<TableB> list = new ArrayList<>(3);

        public Composite(String name, String location) {
            super(name, location);
        }

        public void addTable(TableB b) {
            list.add(b);
        }

        @Override
        public Descriptor getDescriptor(short id) {
            for (TableB b : list) {
                Descriptor d = b.getDescriptor(id);
                if (d != null)
                    return d;
            }
            return null;
        }

        @Override
        public Collection<Descriptor> getDescriptors() {
            ArrayList<Descriptor> result = new ArrayList<>(3000);
            for (TableB b : list)
                result.addAll(b.getDescriptors());
            return result;
        }

        @Override
        public Collection<Short> getKeys() {
            ArrayList<Short> result = new ArrayList<>(3000);
            for (TableB b : list)
                result.addAll(b.getKeys());
            return result;
        }
    }

    // inner class
    @Immutable
    public class Descriptor implements Comparable<Descriptor> {

        private final short x, y;
        private final int scale;
        private final int refVal;
        private final int dataWidth;
        private final String units;
        private final String name;
        private final String desc;
        private final boolean numeric;
        private boolean localOverride;

        Descriptor(short x, short y, int scale, int refVal, int width, String name, String units, String desc) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.refVal = refVal;
            this.dataWidth = width;
            this.name = name.trim();
            this.units = units.trim().intern();
            this.desc = desc;

            this.numeric = !DataDescriptor.isInternationalAlphabetUnit(units);
        }

        public int getScale() {
            return scale;
        }

        public int getRefVal() {
            return refVal;
        }

        public int getDataWidth() {
            return dataWidth;
        }

        public String getUnits() {
            return units;
        }

        public String getName() {
            return name;
        }

        public String getDesc() { // optional - use as long name
            return desc;
        }

        /**
         * Get fxy as a short
         *
         * @return fxy encoded as a short
         */
        public short getId() {
            return (short) ((x << 8) + y);
        }

        /**
         * Get fxy as a String, eg 0-5-22
         *
         * @return fxy encoded as a String
         */
        public String getFxy() {
            return "0-" + x + "-" + y;
        }

        /**
         * is descriptor numeric or String
         *
         * @return true if numeric
         */
        public boolean isNumeric() {
            return numeric;
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

        public String toString() {
            Formatter out = new Formatter();
            show(out);
            return out.toString();
        }

        public String getSource() {
            return getLocation();
        }

        void show(Formatter out) {
            out.format(" %8s scale=%d refVal=%d width=%d  units=(%s) name=(%s)", getFxy(), scale, refVal, dataWidth, units,
                    name);
        }

        @Override
        public int compareTo(Descriptor o) {
            return getId() - o.getId();
        }
    }


}
