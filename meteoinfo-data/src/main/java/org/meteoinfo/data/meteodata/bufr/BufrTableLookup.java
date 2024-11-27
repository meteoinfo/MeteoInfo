/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr;

import org.meteoinfo.data.meteodata.bufr.tables.*;
import ucar.nc2.wmo.CommonCodeTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * Look up info in BUFR tables.
 * Allows local center overrides for BUFR tables
 *
 * @author caron
 * @since 8/22/13
 */
public class BufrTableLookup {

    public static BufrTableLookup factory(Message m) throws IOException {
        return new BufrTableLookup(m.is.getBufrEdition(), m.ids.getCenterId(), m.ids.getSubCenterId(),
                m.ids.getMasterTableId(), m.ids.getMasterTableVersion(), m.ids.getLocalTableVersion(), m.ids.getCategory(),
                m.ids.getSubCategory(), m.ids.getLocalSubCategory());
    }

    /*
     * static public BufrTableLookup factory(int bufrEdition, int center, int subCenter, int masterId, int masterVersion,
     * int localVersion,
     * int category, int subCategory, int localSubCategory) {
     * return new BufrTableLookup(bufrEdition, center, subCenter, masterId, masterVersion, localVersion, category,
     * subCategory, localSubCategory);
     * }
     */

    //////////////////////////////////////////////////////////

    private int center, subCenter, masterId, masterVersion, localVersion, bufrEdition, category, subCategory,
            localSubCategory;

    private BufrTableLookup(int bufrEdition, int center, int subCenter, int masterId, int masterVersion, int localVersion,
                            int category, int subCategory, int localSubCategory) throws IOException {
        this.bufrEdition = bufrEdition;
        this.center = center;
        this.subCenter = subCenter;
        this.masterId = masterId;
        this.masterVersion = masterVersion;
        this.localVersion = localVersion;
        this.category = category;
        this.subCategory = subCategory;
        this.localSubCategory = localSubCategory;

        tlookup = new TableLookup(center, subCenter, masterVersion, localVersion, category);
    }

    public int getBufrEdition() {
        return bufrEdition;
    }

    public int getCenter() {
        return center;
    }

    public int getSubCenter() {
        return subCenter;
    }

    public int getMasterTableId() {
        return masterId;
    }

    public int getMasterTableVersion() {
        return masterVersion;
    }

    public int getLocalTableVersion() {
        return localVersion;
    }

    public int getCategory() {
        return category;
    }

    public int getSubCategory() {
        return subCategory;
    }

    public int getLocalSubCategory() {
        return localSubCategory;
    }

    public String getCenterName() {
        String name = CommonCodeTable.getCenterNameBufr(getCenter(), getBufrEdition());
        String subname = CommonCodeTable.getSubCenterName(getCenter(), getSubCenter());
        if (subname != null)
            name = name + " / " + subname;
        return getCenter() + "." + getSubCenter() + " (" + name + ")";
    }

    public String getCenterNo() {
        return getCenter() + "." + getSubCenter();
    }

    public String getTableName() {
        return getMasterTableId() + "." + getMasterTableVersion() + "." + getLocalTableVersion();
    }

    public String getCategoryFullName() { // throws IOException {
        String catName = getCategoryName();
        String subcatName = getSubCategoryName();

        if (subcatName != null)
            return getCategoryNo() + "=" + catName + " / " + subcatName;
        else
            return getCategoryNo() + "=" + catName;
    }

    public String getSubCategoryName() { // throws IOException {
        String subcatName = null;
        if (center == 7)
            subcatName = NcepTable.getDataSubcategory(getCategory(), getSubCategory());
        if (subcatName == null)
            subcatName = CommonCodeTable.getDataSubcategoy(getCategory(), getSubCategory());
        return subcatName;
    }

    public String getCategoryName() {
        return TableA.getDataCategoryName(getCategory());
    }

    public String getCategoryName(int cat) {
        return TableA.getDataCategoryName(cat);
    }

    public String getCategoryNo() {
        String result = getCategory() + "." + getSubCategory();
        if (getLocalSubCategory() >= 0)
            result += "." + getLocalSubCategory();
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private TableLookup tlookup;

    private void init() {
    }

    public void setTableLookup(TableLookup tlookup) {
        this.tlookup = tlookup;
    }

    public TableA.Descriptor getDescriptorTableA(int code) {
        return tlookup.getDescriptorTableA(code);
    }

    public TableB.Descriptor getDescriptorTableB(short fxy) {
        return tlookup.getDescriptorTableB(fxy);
    }

    public TableD.Descriptor getDescriptorTableD(short fxy) {
        return tlookup.getDescriptorTableD(fxy);
    }

    public String getWmoTableBName() {
        return tlookup.getWmoTableBName();
    }

    public String getLocalTableBName() {
        return tlookup.getLocalTableBName();
    }

    public String getLocalTableDName() {
        return tlookup.getLocalTableDName();
    }

    public String getWmoTableDName() {
        return tlookup.getWmoTableDName();
    }

    public BufrTables.Mode getMode() {
        return tlookup.getMode();
    }

    public void showMissingFields(List<Short> ddsList, Formatter out) {
        for (short fxy : ddsList) {
            int f = (fxy & 0xC000) >> 14;
            if (f == 3) {
                List<Short> sublist = getDescriptorListTableD(fxy);
                if (sublist == null)
                    out.format("%s, ", ucar.nc2.iosp.bufr.Descriptor.makeString(fxy));
                else
                    showMissingFields(sublist, out);

            } else if (f == 0) { // skip the 2- operators for now
                TableB.Descriptor b = getDescriptorTableB(fxy);
                if (b == null)
                    out.format("%s, ", ucar.nc2.iosp.bufr.Descriptor.makeString(fxy));
            }
        }
    }

    public List<String> getDescriptorListTableD(String fxy) {
        short id = ucar.nc2.iosp.bufr.Descriptor.getFxy(fxy);
        List<Short> seq = getDescriptorListTableD(id);
        if (seq == null)
            return null;
        List<String> result = new ArrayList<>(seq.size());
        for (Short s : seq)
            result.add(Descriptor.makeString(s));
        return result;
    }

    public List<Short> getDescriptorListTableD(short id) {
        TableD.Descriptor d = getDescriptorTableD(id);
        if (d != null)
            return d.getSequence();
        return null;
    }
}
