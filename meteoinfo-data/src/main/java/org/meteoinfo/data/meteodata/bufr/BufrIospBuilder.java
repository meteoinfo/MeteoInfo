package org.meteoinfo.data.meteodata.bufr;

import org.slf4j.Logger;
import ucar.ma2.DataType;
import ucar.nc2.*;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.CDM;
import ucar.nc2.constants.CF;
import ucar.nc2.constants._Coordinate;
import org.meteoinfo.data.meteodata.bufr.tables.CodeFlagTables;

import java.util.*;

/**
 * Construction of the Netcdf objects using builders.
 */
class BufrIospBuilder {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(BufrIospBuilder.class);
    private static final boolean warnUnits = false;

    private final Group.Builder rootGroup;
    private Sequence.Builder recordStructure;
    private final Formatter coordinates = new Formatter();

    private int tempNo = 1; // fishy

    BufrIospBuilder(Message proto, BufrConfig bufrConfig, Group.Builder root, String location) {
        this.rootGroup = root;
        this.recordStructure = Sequence.builder().setName(BufrIosp2.obsRecordName);
        this.rootGroup.addVariable(recordStructure);

        // global Attributes
        AttributeContainerMutable atts = root.getAttributeContainer();
        atts.addAttribute(CDM.HISTORY, "Read using CDM BufrIosp2");
        if (bufrConfig.getFeatureType() != null) {
            atts.addAttribute(CF.FEATURE_TYPE, bufrConfig.getFeatureType().toString());
        }
        atts.addAttribute("location", location);

        atts.addAttribute("BUFR:categoryName", proto.getLookup().getCategoryName());
        atts.addAttribute("BUFR:subCategoryName", proto.getLookup().getSubCategoryName());
        atts.addAttribute("BUFR:centerName", proto.getLookup().getCenterName());
        atts.addAttribute("BUFR:category", proto.ids.getCategory());
        atts.addAttribute("BUFR:subCategory", proto.ids.getSubCategory());
        atts.addAttribute("BUFR:localSubCategory", proto.ids.getLocalSubCategory());
        atts.addAttribute(BufrIosp2.centerId, proto.ids.getCenterId());
        atts.addAttribute("BUFR:subCenter", proto.ids.getSubCenterId());
        atts.addAttribute("BUFR:table", proto.ids.getMasterTableId());
        atts.addAttribute("BUFR:tableVersion", proto.ids.getMasterTableVersion());
        atts.addAttribute("BUFR:localTableVersion", proto.ids.getLocalTableVersion());
        atts.addAttribute("Conventions", "BUFR/CDM");
        atts.addAttribute("BUFR:edition", proto.is.getBufrEdition());


        String header = proto.getHeader();
        if (header != null && !header.isEmpty()) {
            atts.addAttribute("WMO Header", header);
        }

        makeObsRecord(bufrConfig);
        String coordS = coordinates.toString();
        if (!coordS.isEmpty()) {
            recordStructure.addAttribute(new Attribute("coordinates", coordS));
        }
    }

    BufrIospBuilder(Message proto, List<BufrConfig> bufrConfigs, Group.Builder root, String location) {
        this.rootGroup = root;

        // global Attributes
        AttributeContainerMutable atts = root.getAttributeContainer();
        atts.addAttribute(CDM.HISTORY, "Read using CDM BufrIosp2");
        atts.addAttribute("location", location);

        atts.addAttribute("BUFR:categoryName", proto.getLookup().getCategoryName());
        atts.addAttribute("BUFR:subCategoryName", proto.getLookup().getSubCategoryName());
        atts.addAttribute("BUFR:centerName", proto.getLookup().getCenterName());
        atts.addAttribute(BufrIosp2.centerId, proto.ids.getCenterId());
        atts.addAttribute("BUFR:subCenter", proto.ids.getSubCenterId());
        atts.addAttribute("BUFR:table", proto.ids.getMasterTableId());
        atts.addAttribute("BUFR:tableVersion", proto.ids.getMasterTableVersion());
        atts.addAttribute("BUFR:localTableVersion", proto.ids.getLocalTableVersion());
        atts.addAttribute("Conventions", "BUFR/CDM");
        atts.addAttribute("BUFR:edition", proto.is.getBufrEdition());

        String header = proto.getHeader();
        if (header != null && !header.isEmpty()) {
            atts.addAttribute("WMO Header", header);
        }

        for (BufrConfig bufrConfig : bufrConfigs) {
            String varName = proto.getLookup().getCategoryName(bufrConfig.getMessage().ids.getCategory());
            Sequence.Builder rs = Sequence.builder().setName(varName);
            this.rootGroup.addVariable(rs);
            makeObsRecord(bufrConfig, rs);
            String coordS = coordinates.toString();
            if (!coordS.isEmpty()) {
                rs.addAttribute(new Attribute("coordinates", coordS));
            }
        }
    }

    Sequence.Builder getObsStructure() {
        return recordStructure;
    }

    private void makeObsRecord(BufrConfig bufrConfig) {
        BufrConfig.FieldConverter root = bufrConfig.getRootConverter();
        for (BufrConfig.FieldConverter fld : root.flds) {
            DataDescriptor dkey = fld.dds;
            if (!dkey.isOkForVariable()) {
                continue;
            }

            if (dkey.replication == 0) {
                addSequence(rootGroup, recordStructure, fld);

            } else if (dkey.replication > 1) {

                List<BufrConfig.FieldConverter> subFlds = fld.flds;
                List<DataDescriptor> subKeys = dkey.subKeys;
                if (subKeys.size() == 1) { // only one member
                    DataDescriptor subDds = dkey.subKeys.get(0);
                    BufrConfig.FieldConverter subFld = subFlds.get(0);
                    if (subDds.dpi != null) {
                        addDpiStructure(recordStructure, fld, subFld);

                    } else if (subDds.replication == 1) { // one member not a replication
                        Variable.Builder v = addVariable(rootGroup, recordStructure, subFld, dkey.replication);
                        v.setSPobject(fld); // set the replicating field as SPI object

                    } else { // one member is a replication (two replications in a row)
                        addStructure(rootGroup, recordStructure, fld, dkey.replication);
                    }
                } else if (subKeys.size() > 1) {
                    addStructure(rootGroup, recordStructure, fld, dkey.replication);
                }

            } else { // replication == 1
                addVariable(rootGroup, recordStructure, fld, dkey.replication);
            }
        }
    }

    private void makeObsRecord(BufrConfig bufrConfig, Sequence.Builder rs) {
        BufrConfig.FieldConverter root = bufrConfig.getRootConverter();
        for (BufrConfig.FieldConverter fld : root.flds) {
            DataDescriptor dkey = fld.dds;
            if (!dkey.isOkForVariable()) {
                continue;
            }

            if (dkey.replication == 0) {
                addSequence(rootGroup, rs, fld);

            } else if (dkey.replication > 1) {

                List<BufrConfig.FieldConverter> subFlds = fld.flds;
                List<DataDescriptor> subKeys = dkey.subKeys;
                if (subKeys.size() == 1) { // only one member
                    DataDescriptor subDds = dkey.subKeys.get(0);
                    BufrConfig.FieldConverter subFld = subFlds.get(0);
                    if (subDds.dpi != null) {
                        addDpiStructure(rs, fld, subFld);

                    } else if (subDds.replication == 1) { // one member not a replication
                        Variable.Builder v = addVariable(rootGroup, rs, subFld, dkey.replication);
                        v.setSPobject(fld); // set the replicating field as SPI object

                    } else { // one member is a replication (two replications in a row)
                        addStructure(rootGroup, rs, fld, dkey.replication);
                    }
                } else if (subKeys.size() > 1) {
                    addStructure(rootGroup, rs, fld, dkey.replication);
                }

            } else { // replication == 1
                addVariable(rootGroup, rs, fld, dkey.replication);
            }
        }
    }

    private void addStructure(Group.Builder group, Structure.Builder parent, BufrConfig.FieldConverter fld, int count) {
        DataDescriptor dkey = fld.dds;
        String uname = findUniqueName(parent, fld.getName(), "struct");
        dkey.name = uname; // name may need to be changed for uniqueness

        Structure.Builder struct = Structure.builder().setName(uname);
        struct.setDimensionsAnonymous(new int[]{count}); // anon vector
        for (BufrConfig.FieldConverter subKey : fld.flds) {
            addMember(group, struct, subKey);
        }

        parent.addMemberVariable(struct);
        struct.setSPobject(fld);
    }

    private void addSequence(Group.Builder group, Structure.Builder parent, BufrConfig.FieldConverter fld) {
        DataDescriptor dkey = fld.dds;
        String uname = findUniqueName(parent, fld.getName(), "seq");
        dkey.name = uname; // name may need to be changed for uniqueness

        Sequence.Builder seq = Sequence.builder().setName(uname);
        for (BufrConfig.FieldConverter subKey : fld.flds) {
            addMember(group, seq, subKey);
        }

        parent.addMemberVariable(seq);
        seq.setSPobject(fld);
    }

    private void addMember(Group.Builder group, Structure.Builder parent, BufrConfig.FieldConverter fld) {
        DataDescriptor dkey = fld.dds;

        if (dkey.replication == 0) {
            addSequence(group, parent, fld);
        } else if (dkey.replication > 1) {
            List<DataDescriptor> subKeys = dkey.subKeys;
            if (subKeys.size() == 1) {
                BufrConfig.FieldConverter subFld = fld.flds.get(0);
                Variable.Builder v = addVariable(group, parent, subFld, dkey.replication);
                v.setSPobject(fld); // set the replicating field as SPI object

            } else {
                addStructure(group, parent, fld, dkey.replication);
            }

        } else {
            addVariable(group, parent, fld, dkey.replication);
        }
    }

    private void addDpiStructure(Structure.Builder parent, BufrConfig.FieldConverter parentFld,
                                 BufrConfig.FieldConverter dpiField) {
        DataDescriptor dpiKey = dpiField.dds;
        String uname = findUniqueName(parent, dpiField.getName(), "struct");
        dpiKey.name = uname; // name may need to be changed for uniqueness

        Structure.Builder struct = Structure.builder().setName(uname);
        parent.addMemberVariable(struct);
        int n = parentFld.dds.replication;
        struct.setDimensionsAnonymous(new int[]{n}); // anon vector

        Variable.Builder v = Variable.builder().setName("name");
        v.setDataType(DataType.STRING); // scalar
        struct.addMemberVariable(v);

        v = Variable.builder().setName("data");
        v.setDataType(DataType.FLOAT); // scalar
        struct.addMemberVariable(v);

        struct.setSPobject(dpiField); // ??
    }

    private void addDpiSequence(Structure.Builder parent, BufrConfig.FieldConverter fld) {
        Structure.Builder struct = Structure.builder().setName("statistics");
        struct.setDimensionsAnonymous(new int[]{fld.dds.replication}); // scalar

        Variable.Builder v = Variable.builder().setName("name");
        v.setDataType(DataType.STRING); // scalar
        struct.addMemberVariable(v);

        v = Variable.builder().setName("data");
        v.setDataType(DataType.FLOAT); // scalar
        struct.addMemberVariable(v);

        parent.addMemberVariable(struct);
    }

    private Variable.Builder addVariable(Group.Builder group, Structure.Builder struct, BufrConfig.FieldConverter fld,
                                         int count) {
        DataDescriptor dkey = fld.dds;
        String uname = findGloballyUniqueName(fld.getName(), "unknown");
        dkey.name = uname; // name may need to be changed for uniqueness

        Variable.Builder v = Variable.builder().setName(uname);
        if (count > 1) {
            v.setDimensionsAnonymous(new int[]{count}); // anon vector
        }

        if (fld.getDesc() != null) {
            v.addAttribute(new Attribute(CDM.LONG_NAME, fld.getDesc()));
        }

        if (fld.getUnits() == null) {
            if (warnUnits) {
                log.warn("dataDesc.units == null for " + uname);
            }
        } else {
            String units = fld.getUnits();
            if (ucar.nc2.iosp.bufr.DataDescriptor.isCodeTableUnit(units)) {
                v.addAttribute(new Attribute(CDM.UNITS, "CodeTable " + fld.dds.getFxyName()));
            } else if (ucar.nc2.iosp.bufr.DataDescriptor.isFlagTableUnit(units)) {
                v.addAttribute(new Attribute(CDM.UNITS, "FlagTable " + fld.dds.getFxyName()));
            } else if (!ucar.nc2.iosp.bufr.DataDescriptor.isInternationalAlphabetUnit(units) && !units.startsWith("Numeric")) {
                v.addAttribute(new Attribute(CDM.UNITS, units));
            }
        }

        DataDescriptor dataDesc = fld.dds;
        if (dataDesc.type == 1) {
            v.setDataType(DataType.CHAR);
            int size = dataDesc.bitWidth / 8;
            v.setDimensionsAnonymous(new int[]{size});

        } else if ((dataDesc.type == 2) && CodeFlagTables.hasTable(dataDesc.fxy)) { // enum
            int nbits = dataDesc.bitWidth;
            int nbytes = (nbits % 8 == 0) ? nbits / 8 : nbits / 8 + 1;

            CodeFlagTables ct = CodeFlagTables.getTable(dataDesc.fxy);
            if (nbytes == 1) {
                v.setDataType(DataType.ENUM1);
            } else if (nbytes == 2) {
                v.setDataType(DataType.ENUM2);
            } else if (nbytes == 4) {
                v.setDataType(DataType.ENUM4);
            }

            // v.removeAttribute(CDM.UNITS);
            v.addAttribute(new Attribute("BUFR:CodeTable", ct.getName() + " (" + dataDesc.getFxyName() + ")"));

            EnumTypedef type = group.findOrAddEnumTypedef(ct.getName(), ct.getMap());
            v.setEnumTypeName(type.getShortName());

        } else {
            int nbits = dataDesc.bitWidth;
            // use of unsigned seems fishy, since only time it uses high bit is for missing
            // not necessarily true, just when they "add one bit" to deal with missing case
            if (nbits < 9) {
                v.setDataType(DataType.BYTE);
                if (nbits == 8) {
                    v.addAttribute(new Attribute(CDM.UNSIGNED, "true"));
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (short) BufrNumbers.missingValue(nbits)));
                } else {
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (byte) BufrNumbers.missingValue(nbits)));
                }

            } else if (nbits < 17) {
                v.setDataType(DataType.SHORT);
                if (nbits == 16) {
                    v.addAttribute(new Attribute(CDM.UNSIGNED, "true"));
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (int) BufrNumbers.missingValue(nbits)));
                } else {
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (short) BufrNumbers.missingValue(nbits)));
                }

            } else if (nbits < 33) {
                v.setDataType(DataType.INT);
                if (nbits == 32) {
                    v.addAttribute(new Attribute(CDM.UNSIGNED, "true"));
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (int) BufrNumbers.missingValue(nbits)));
                } else {
                    v.addAttribute(new Attribute(CDM.MISSING_VALUE, (int) BufrNumbers.missingValue(nbits)));
                }

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
            if (scale10 != 0) {
                v.addAttribute(new Attribute(CDM.SCALE_FACTOR, (float) scale));
            }
            if (dataDesc.refVal != 0) {
                v.addAttribute(new Attribute(CDM.ADD_OFFSET, (float) scale * dataDesc.refVal));
            }

        }

        annotate(v, fld);
        v.addAttribute(new Attribute(BufrIosp2.fxyAttName, dataDesc.getFxyName()));
        v.addAttribute(new Attribute("BUFR:bitWidth", dataDesc.bitWidth));
        struct.addMemberVariable(v);

        v.setSPobject(fld);
        return v;
    }

    private String findUniqueName(Structure.Builder<?> struct, String want, String def) {
        if (want == null) {
            return def + tempNo++;
        }

        String vwant = NetcdfFiles.makeValidCdmObjectName(want);
        Optional<Variable.Builder<?>> oldV = struct.findMemberVariable(vwant);
        if (!oldV.isPresent()) {
            return vwant;
        }

        int seq = 2;
        while (true) {
            String wantSeq = vwant + "-" + seq;
            oldV = struct.findMemberVariable(wantSeq);
            if (!oldV.isPresent()) {
                return wantSeq;
            }
            seq++;
        }
    }

    // force globally unique variable names, even when they are in different Structures.
    // this allows us to promote structure members without worrying about name collisions
    private Map<String, Integer> names = new HashMap<>(100);

    private String findGloballyUniqueName(String want, String def) {
        if (want == null) {
            return def + tempNo++;
        }

        String vwant = NetcdfFiles.makeValidCdmObjectName(want);
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


    private void annotate(Variable.Builder v, BufrConfig.FieldConverter fld) {
        if (fld.type == null) {
            return;
        }

        switch (fld.type) {
            case lat:
                v.addAttribute(new Attribute(CDM.UNITS, CDM.LAT_UNITS));
                v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
                coordinates.format("%s ", v.shortName);
                break;

            case lon:
                v.addAttribute(new Attribute(CDM.UNITS, CDM.LON_UNITS));
                v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
                coordinates.format("%s ", v.shortName);
                break;

            case height:
            case heightOfStation:
            case heightAboveStation:
                v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Height.toString()));
                coordinates.format("%s ", v.shortName);
                break;

            case stationId:
                v.addAttribute(new Attribute(CF.STANDARD_NAME, CF.STATION_ID));
                break;

            case wmoId:
                v.addAttribute(new Attribute(CF.STANDARD_NAME, CF.STATION_WMOID));
                break;
        }
    }
}

