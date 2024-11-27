/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.CDM;
import ucar.nc2.constants.CF;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.ft.point.bufr.BufrCdmIndexProto;
import ucar.nc2.ft.point.bufr.StandardFields;
import org.meteoinfo.data.meteodata.bufr.tables.CodeFlagTables;

import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BufrIosp2 delegates the construction of the Netcdf objects to Construct2.
 *
 * @author caron
 * @since 8/8/13
 */

class Construct2 {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Construct2.class);
    private static final boolean warnUnits = false;

    private NetcdfFile ncfile;

    private Sequence recordStructure;
    private int centerId;
    private Formatter coordinates = new Formatter();

    Construct2(Message proto, BufrConfig bufrConfig, NetcdfFile nc) throws IOException {
        this.ncfile = nc;

        // dkeyRoot = dds.getDescriptorRoot();
        // int nbits = dds.getTotalBits();
        // int inputBytes = (nbits % 8 == 0) ? nbits / 8 : nbits / 8 + 1;
        // int outputBytes = dds.getTotalBytes();

        // the category
        // int cat = proto.ids.getCategory();
        // int subcat = proto.ids.getSubCategory();

        // global Attributes
        ncfile.addAttribute(null, new Attribute(CDM.HISTORY, "Read using CDM BufrIosp2"));
        if (bufrConfig.getFeatureType() != null)
            ncfile.addAttribute(null, CF.FEATURE_TYPE, bufrConfig.getFeatureType().toString());
        ncfile.addAttribute(null, "location", nc.getLocation());

        ncfile.addAttribute(null, "BUFR:categoryName", proto.getLookup().getCategoryName());
        ncfile.addAttribute(null, "BUFR:subCategoryName", proto.getLookup().getSubCategoryName());
        ncfile.addAttribute(null, "BUFR:centerName", proto.getLookup().getCenterName());
        ncfile.addAttribute(null, new Attribute("BUFR:category", proto.ids.getCategory()));
        ncfile.addAttribute(null, new Attribute("BUFR:subCategory", proto.ids.getSubCategory()));
        ncfile.addAttribute(null, new Attribute("BUFR:localSubCategory", proto.ids.getLocalSubCategory()));
        ncfile.addAttribute(null, new Attribute(BufrIosp2.centerId, proto.ids.getCenterId()));
        ncfile.addAttribute(null, new Attribute("BUFR:subCenter", proto.ids.getSubCenterId()));
        // ncfile.addAttribute(null, "BUFR:tableName", proto.ids.getMasterTableFilename()));
        ncfile.addAttribute(null, new Attribute("BUFR:table", proto.ids.getMasterTableId()));
        ncfile.addAttribute(null, new Attribute("BUFR:tableVersion", proto.ids.getMasterTableVersion()));
        ncfile.addAttribute(null, new Attribute("BUFR:localTableVersion", proto.ids.getLocalTableVersion()));
        ncfile.addAttribute(null, "Conventions", "BUFR/CDM");
        ncfile.addAttribute(null, new Attribute("BUFR:edition", proto.is.getBufrEdition()));

        centerId = proto.ids.getCenterId();

        String header = proto.getHeader();
        if (header != null && !header.isEmpty())
            ncfile.addAttribute(null, new Attribute("WMO Header", header));

        makeObsRecord(bufrConfig);
        String coordS = coordinates.toString();
        if (!coordS.isEmpty())
            recordStructure.addAttribute(new Attribute("coordinates", coordS));

        ncfile.finish();
    }

    Sequence getObsStructure() {
        return recordStructure;
    }

    private void makeObsRecord(BufrConfig bufrConfig) {
        recordStructure = new Sequence(ncfile, null, null, BufrIosp2.obsRecordName);
        ncfile.addVariable(null, recordStructure);

        BufrConfig.FieldConverter root = bufrConfig.getRootConverter();
        for (BufrConfig.FieldConverter fld : root.flds) {
            DataDescriptor dkey = fld.dds;
            if (!dkey.isOkForVariable())
                continue;

            if (dkey.replication == 0) {
                addSequence(recordStructure, fld);

            } else if (dkey.replication > 1) {

                List<BufrConfig.FieldConverter> subFlds = fld.flds;
                List<DataDescriptor> subKeys = dkey.subKeys;
                if (subKeys.size() == 1) { // only one member
                    DataDescriptor subDds = dkey.subKeys.get(0);
                    BufrConfig.FieldConverter subFld = subFlds.get(0);
                    if (subDds.dpi != null) {
                        addDpiStructure(recordStructure, fld, subFld);

                    } else if (subDds.replication == 1) { // one member not a replication
                        Variable v = addVariable(recordStructure, subFld, dkey.replication);
                        v.setSPobject(fld); // set the replicating field as SPI object

                    } else { // one member is a replication (two replications in a row)
                        addStructure(recordStructure, fld, dkey.replication);
                    }
                } else if (subKeys.size() > 1) {
                    addStructure(recordStructure, fld, dkey.replication);
                }

            } else { // replication == 1
                addVariable(recordStructure, fld, dkey.replication);
            }
        }
    }

    private void addStructure(Structure parent, BufrConfig.FieldConverter fld, int count) {
        DataDescriptor dkey = fld.dds;
        String uname = findUniqueName(parent, fld.getName(), "struct");
        dkey.name = uname; // name may need to be changed for uniqueness

        // String structName = dataDesc.name != null ? dataDesc.name : "struct" + structNum++;
        Structure struct = new Structure(ncfile, null, parent, uname);
        try {
            struct.setDimensionsAnonymous(new int[]{count}); // anon vector
        } catch (InvalidRangeException e) {
            log.error("illegal count= " + count + " for " + fld);
        }

        for (BufrConfig.FieldConverter subKey : fld.flds)
            addMember(struct, subKey);

        parent.addMemberVariable(struct);
        struct.setSPobject(fld);
    }

    private void addSequence(Structure parent, BufrConfig.FieldConverter fld) {

        DataDescriptor dkey = fld.dds;
        String uname = findUniqueName(parent, fld.getName(), "seq");
        dkey.name = uname; // name may need to be changed for uniqueness

        // String seqName = ftype == (FeatureType.STATION_PROFILE) ? "profile" : "seq";
        // String seqName = dataDesc.name != null ? dataDesc.name : "seq" + seqNum++;

        Sequence seq = new Sequence(ncfile, null, parent, uname);
        seq.setDimensions(""); // scalar

        for (BufrConfig.FieldConverter subKey : fld.flds)
            addMember(seq, subKey);

        parent.addMemberVariable(seq);
        seq.setSPobject(fld);

        dkey.refersTo = seq;
    }

    private void addMember(Structure parent, BufrConfig.FieldConverter fld) {
        DataDescriptor dkey = fld.dds;

        if (dkey.replication == 0)
            addSequence(parent, fld);

        else if (dkey.replication > 1) {
            List<DataDescriptor> subKeys = dkey.subKeys;
            if (subKeys.size() == 1) {
                BufrConfig.FieldConverter subFld = fld.flds.get(0);
                Variable v = addVariable(parent, subFld, dkey.replication);
                v.setSPobject(fld); // set the replicating field as SPI object

            } else {
                addStructure(parent, fld, dkey.replication);
            }

        } else {
            addVariable(parent, fld, dkey.replication);
        }
    }

    private void addDpiStructure(Structure parent, BufrConfig.FieldConverter parentFld,
                                 BufrConfig.FieldConverter dpiField) {
        DataDescriptor dpiKey = dpiField.dds;
        String uname = findUniqueName(parent, dpiField.getName(), "struct");
        dpiKey.name = uname; // name may need to be changed for uniqueness

        // String structName = findUnique(parent, dpiField.name);
        Structure struct = new Structure(ncfile, null, parent, uname);
        int n = parentFld.dds.replication;
        try {
            struct.setDimensionsAnonymous(new int[]{n}); // anon vector
        } catch (InvalidRangeException e) {
            log.error("illegal count= " + 1 + " for " + dpiField);
        }

        Variable v = new Variable(ncfile, null, struct, "name");
        v.setDataType(DataType.STRING); // scalar
        v.setDimensions(""); // scalar
        struct.addMemberVariable(v);

        v = new Variable(ncfile, null, struct, "data");
        v.setDataType(DataType.FLOAT); // scalar
        v.setDimensions(""); // scalar
        struct.addMemberVariable(v);

        parent.addMemberVariable(struct);
        struct.setSPobject(dpiField); // ??

        // add some fake dkeys corresponding to above
        // DataDescriptor nameDD = new DataDescriptor();
    }

    private void addDpiSequence(Structure parent, BufrConfig.FieldConverter fld) {
        Structure struct = new Structure(ncfile, null, parent, "statistics");
        try {
            struct.setDimensionsAnonymous(new int[]{fld.dds.replication}); // scalar
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        Variable v = new Variable(ncfile, null, struct, "name");
        v.setDataType(DataType.STRING); // scalar
        v.setDimensions(""); // scalar
        struct.addMemberVariable(v);

        v = new Variable(ncfile, null, struct, "data");
        v.setDataType(DataType.FLOAT); // scalar
        v.setDimensions(""); // scalar
        struct.addMemberVariable(v);

        parent.addMemberVariable(struct);
    }

    private Variable addVariable(Structure struct, BufrConfig.FieldConverter fld, int count) {
        DataDescriptor dkey = fld.dds;
        String uname = findGloballyUniqueName(fld.getName(), "unknown");
        dkey.name = uname; // name may need to be changed for uniqueness

        Variable v = new Variable(ncfile, null, struct, uname);
        try {
            if (count > 1)
                v.setDimensionsAnonymous(new int[]{count}); // anon vector
            else
                v.setDimensions(""); // scalar
        } catch (InvalidRangeException e) {
            log.error("illegal count= " + count + " for " + fld);
        }

        if (fld.getDesc() != null)
            v.addAttribute(new Attribute(CDM.LONG_NAME, fld.getDesc()));

        if (fld.getUnits() == null) {
            if (warnUnits)
                log.warn("dataDesc.units == null for " + uname);
        } else {
            String units = fld.getUnits();
            if (DataDescriptor.isCodeTableUnit(units)) {
                v.addAttribute(new Attribute(CDM.UNITS, "CodeTable " + fld.dds.getFxyName()));
            } else if (DataDescriptor.isFlagTableUnit(units)) {
                v.addAttribute(new Attribute(CDM.UNITS, "FlagTable " + fld.dds.getFxyName()));
            } else if (!DataDescriptor.isInternationalAlphabetUnit(units) && !units.startsWith("Numeric")) {
                v.addAttribute(new Attribute(CDM.UNITS, units));
            }
        }

        DataDescriptor dataDesc = fld.dds;
        if (dataDesc.type == 1) {
            v.setDataType(DataType.CHAR);
            int size = dataDesc.bitWidth / 8;
            try {
                v.setDimensionsAnonymous(new int[]{size});
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }

        } else if ((dataDesc.type == 2) && CodeFlagTables.hasTable(dataDesc.fxy)) { // enum
            int nbits = dataDesc.bitWidth;
            int nbytes = (nbits % 8 == 0) ? nbits / 8 : nbits / 8 + 1;

            CodeFlagTables ct = CodeFlagTables.getTable(dataDesc.fxy);
            if (nbytes == 1)
                v.setDataType(DataType.ENUM1);
            else if (nbytes == 2)
                v.setDataType(DataType.ENUM2);
            else if (nbytes == 4)
                v.setDataType(DataType.ENUM4);

            // v.removeAttribute(CDM.UNITS);
            v.addAttribute(new Attribute("BUFR:CodeTable", ct.getName() + " (" + dataDesc.getFxyName() + ")"));

            Group g = struct.getParentGroupOrRoot();
            if (g == null)
                log.warn("Struct parent group is null.");
            EnumTypedef enumTypedef = g.findEnumeration(ct.getName());
            if (enumTypedef == null) {
                enumTypedef = new EnumTypedef(ct.getName(), ct.getMap());
                g.addEnumeration(enumTypedef);
            }
            v.setEnumTypedef(enumTypedef);

        } else {
            int nbits = dataDesc.bitWidth;
            // use of unsigned seems fishy, since only time it uses high bit is for missing
            // not necessarily true, just when they "add one bit" to deal with missing case
            if (nbits < 9) {
                v.setDataType(DataType.BYTE);
                if (nbits == 8) {
                    v.addAttribute(new Attribute(CDM.UNSIGNED, "true"));
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (short) BufrNumbers.missingValue(nbits)));
                } else
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (byte) BufrNumbers.missingValue(nbits)));

            } else if (nbits < 17) {
                v.setDataType(DataType.SHORT);
                if (nbits == 16) {
                    v.addAttribute(new Attribute(CDM.UNSIGNED, "true"));
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (int) BufrNumbers.missingValue(nbits)));
                } else
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (short) BufrNumbers.missingValue(nbits)));

            } else if (nbits < 33) {
                v.setDataType(DataType.INT);
                if (nbits == 32) {
                    v.addAttribute(new Attribute(CDM.UNSIGNED, "true"));
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (int) BufrNumbers.missingValue(nbits)));
                } else
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (int) BufrNumbers.missingValue(nbits)));

            } else {
                v.setDataType(DataType.LONG);
                v.addAttribute(new Attribute(CDM.MISSING_VALUE, BufrNumbers.missingValue(nbits)));
            }

            // value = scale_factor * packed + add_offset
            // bpacked = (value * 10^scale - refVal)
            // (bpacked + refVal) / 10^scale = value
            // value = bpacked * 10^-scale + refVal * 10^-scale
            // scale_factor = 10^-scale
            // add_ofset = refVal * 10^-scale
            int scale10 = dataDesc.scale;
            double scale = (scale10 == 0) ? 1.0 : Math.pow(10.0, -scale10);
            if (scale10 != 0)
                v.addAttribute(new Attribute(CDM.SCALE_FACTOR, (float) scale));
            if (dataDesc.refVal != 0)
                v.addAttribute(new Attribute(CDM.ADD_OFFSET, (float) scale * dataDesc.refVal));

        }

        annotate(v, fld);
        v.addAttribute(new Attribute(BufrIosp2.fxyAttName, dataDesc.getFxyName()));
        v.addAttribute(new Attribute("BUFR:bitWidth", dataDesc.bitWidth));
        struct.addMemberVariable(v);

        v.setSPobject(fld);
        return v;
    }


    private int tempNo = 1;

    private String findUniqueName(Structure struct, String want, String def) {
        if (want == null)
            return def + tempNo++;

        String vwant = NetcdfFile.makeValidCdmObjectName(want);
        Variable oldV = struct.findVariable(vwant);
        if (oldV == null)
            return vwant;

        int seq = 2;
        while (true) {
            String wantSeq = vwant + "-" + seq;
            oldV = struct.findVariable(wantSeq);
            if (oldV == null)
                return wantSeq;
            seq++;
        }
    }

    // force globally unique variable names, even when they are in different Structures.
    // this allows us to promote structure members without worrying about name collisions
    private Map<String, Integer> names = new HashMap<>(100);

    private String findGloballyUniqueName(String want, String def) {
        if (want == null)
            return def + tempNo++;

        String vwant = NetcdfFile.makeValidCdmObjectName(want);
        Integer have = names.get(vwant);
        if (have == null) {
            names.put(vwant, 1);
            return vwant;
        } else {
            have = have + 1;
            String wantSeq = vwant + "-" + have;
            names.put(vwant, have);
            return wantSeq;
        }
    }


    private void annotate(Variable v, BufrConfig.FieldConverter fld) {
        if (fld.type == null)
            return;

        switch (fld.type) {
            case lat:
                v.addAttribute(new Attribute(CDM.UNITS, CDM.LAT_UNITS));
                v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
                coordinates.format("%s ", v.getShortName());
                break;

            case lon:
                v.addAttribute(new Attribute(CDM.UNITS, CDM.LON_UNITS));
                v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
                coordinates.format("%s ", v.getShortName());
                break;

            case height:
            case heightOfStation:
            case heightAboveStation:
                v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Height.toString()));
                coordinates.format("%s ", v.getShortName());
                break;

            case stationId:
                v.addAttribute(new Attribute(CF.STANDARD_NAME, CF.STATION_ID));
                break;

            case wmoId:
                v.addAttribute(new Attribute(CF.STANDARD_NAME, CF.STATION_WMOID));
                break;
        }

    }

    private void annotateObs(Sequence recordStructure) {
        StandardFields.StandardFieldsFromStructure extract =
                new StandardFields.StandardFieldsFromStructure(centerId, recordStructure);

        try (Formatter f = new Formatter()) {
            String name = extract.getFieldName(BufrCdmIndexProto.FldType.lat);
            if (name != null)
                f.format("%s ", name);
            name = extract.getFieldName(BufrCdmIndexProto.FldType.lon);
            if (name != null)
                f.format("%s ", name);
            name = extract.getFieldName(BufrCdmIndexProto.FldType.height);
            if (name != null)
                f.format("%s ", name);
            name = extract.getFieldName(BufrCdmIndexProto.FldType.heightAboveStation);
            if (name != null)
                f.format("%s ", name);

            recordStructure.addAttribute(new Attribute("coordinates", f.toString()));
        }
    }


}
