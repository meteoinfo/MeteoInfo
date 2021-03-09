/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.mapdata.geotiff;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class IFDEntry implements Comparable{
    // <editor-fold desc="Variables">

    protected Tag tag;
    protected FieldType type;
    protected int count;
    protected int[] value;
    protected double[] valueD;
    protected String valueS;
    protected List<GeoKey> geokeys = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param tag Tag
     * @param type Field type
     */
    IFDEntry(Tag tag, FieldType type) {
        this.tag = tag;
        this.type = type;
        this.count = 1;
    }

    /**
     * Constructor
     *
     * @param tag Tag
     * @param type Field type
     * @param count Count
     */
    IFDEntry(Tag tag, FieldType type, int count) {
        this.tag = tag;
        this.type = type;
        this.count = count;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Set value
     *
     * @param v Value
     * @return IFDEntry
     */
    public IFDEntry setValue(int v) {
        this.value = new int[1];
        this.value[0] = v;
        return this;
    }

    /**
     * Set value
     *
     * @param n Value 1
     * @param d Value 2
     * @return IFDEntry
     */
    public IFDEntry setValue(int n, int d) {
        this.value = new int[2];
        this.value[0] = n;
        this.value[1] = d;
        return this;
    }

    /**
     * Set value
     *
     * @param n Value 1
     * @param d Value 2
     * @param f Value 3
     * @return IFDEntry
     */
    public IFDEntry setValue(int n, int d, int f) {
        this.value = new int[3];
        this.value[0] = n;
        this.value[1] = d;
        this.value[2] = f;
        return this;
    }

    /**
     * Set value
     *
     * @param v Value array
     * @return IFDEntry
     */
    public IFDEntry setValue(int[] v) {
        this.count = v.length;
        this.value = ((int[]) v.clone());
        return this;
    }

    /**
     * Set value
     *
     * @param v Value
     * @return IFDEntry
     */
    public IFDEntry setValue(double v) {
        this.count = 1;
        this.valueD = new double[1];
        this.valueD[0] = v;
        return this;
    }

    /**
     * Set vlaue
     *
     * @param v Value array
     * @return IDFEntry
     */
    public IFDEntry setValue(double[] v) {
        this.count = v.length;
        this.valueD = ((double[]) v.clone());
        return this;
    }

    /**
     * Set value
     *
     * @param v Value
     * @return IFDEntry
     */
    public IFDEntry setValue(String v) {
        this.count = v.length();
        this.valueS = v;
        return this;
    }

    /**
     * Add GeoKey
     *
     * @param geokey GeoKey
     */
    public void addGeoKey(GeoKey geokey) {
        if (this.geokeys == null) {
            this.geokeys = new ArrayList<GeoKey>();
        }
        this.geokeys.add(geokey);
    }
    
    /**
     * Find a GeoKey
     * @param tag The GeoKey tag
     * @return Found GeoKey
     */
    public GeoKey findGeoKey(GeoKey.Tag tag){
        for (GeoKey geoKey : this.geokeys){
            if (geoKey.tag == tag)
                return geoKey;
        }
        
        return null;
    }

    /**
     * Compare to
     *
     * @param o Object
     * @return Int
     */
    @Override
    public int compareTo(Object o) {
        return this.tag.compareTo(((IFDEntry) o).tag);
    }

    /**
     * To string
     *
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(new StringBuilder().append(" tag = ").append(this.tag).toString());
        sbuf.append(new StringBuilder().append(" type = ").append(this.type).toString());
        sbuf.append(new StringBuilder().append(" count = ").append(this.count).toString());
        sbuf.append(" values = ");

        if (this.type == FieldType.ASCII) {
            sbuf.append(this.valueS);
        } else if (this.type == FieldType.RATIONAL) {
            for (int i = 0; i < 2; i += 2) {
                if (i > 1) {
                    sbuf.append(", ");
                }
                sbuf.append(new StringBuilder().append(this.value[i]).append("/").append(this.value[(i + 1)]).toString());
            }
        } else if ((this.type == FieldType.DOUBLE) || (this.type == FieldType.FLOAT)) {
            for (int i = 0; i < this.count; i++) {
                sbuf.append(new StringBuilder().append(this.valueD[i]).append(" ").toString());
            }
        } else {
            int n = Math.min(this.count, 30);
            for (int i = 0; i < n; i++) {
                sbuf.append(new StringBuilder().append(this.value[i]).append(" ").toString());
            }
        }
        if (this.geokeys != null) {
            sbuf.append("\n");
            for (int i = 0; i < this.geokeys.size(); i++) {
                GeoKey elem = (GeoKey) this.geokeys.get(i);
                sbuf.append(new StringBuilder().append("        ").append(elem).append("\n").toString());
            }
        }

        return sbuf.toString();
    }
    // </editor-fold>
}
