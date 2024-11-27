/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr.point;

import org.jdom2.Element;
import thredds.client.catalog.Catalog;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.Structure;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.SequenceDS;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.ft.*;
import ucar.nc2.ft.point.*;
import ucar.nc2.ft.point.bufr.BufrCdmIndexProto;
import ucar.nc2.iosp.IOServiceProvider;
import org.meteoinfo.data.meteodata.bufr.BufrIosp2;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.time.CalendarDateUnit;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.Indent;
import ucar.unidata.geoloc.EarthLocation;
import ucar.unidata.geoloc.LatLonRect;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Use BufrConfig to make BUFR files into PointFeatureDataset
 *
 * @author caron
 * @since 8/14/13
 */
public class BufrFeatureDatasetFactory implements FeatureDatasetFactory {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BufrFeatureDatasetFactory.class);
    private static CalendarDateUnit bufrDateUnits = CalendarDateUnit.of(null, "msecs since 1970-01-01T00:00:00");
    private static String bufrAltUnits = "m"; // LOOK fake

    @Override
    public Object isMine(FeatureType wantFeatureType, NetcdfDataset ncd, Formatter errlog) {
        IOServiceProvider iosp = ncd.getIosp();
        return (iosp instanceof BufrIosp2) ? true : null;
    }

    @Override
    public FeatureType[] getFeatureTypes() {
        return new FeatureType[]{FeatureType.ANY_POINT};
    }

    @Override
    public FeatureDataset open(FeatureType ftype, NetcdfDataset ncd, Object analysis, CancelTask task, Formatter errlog)
            throws IOException {

        // must have an index file
        File indexFile = BufrCdmIndex.calcIndexFile(ncd.getLocation());
        if (indexFile == null)
            return null;

        BufrCdmIndex index = BufrCdmIndex.readIndex(indexFile.getPath());
        return new BufrStationDataset(ncd, index);
    }

    private void show(Element parent, Indent indent, Formatter f) {
        if (parent == null)
            return;
        for (Element child : parent.getChildren("fld", Catalog.ncmlNS)) {
            String idx = child.getAttributeValue("idx");
            String fxy = child.getAttributeValue("fxy");
            String name = child.getAttributeValue("name");
            String action = child.getAttributeValue("action");
            f.format("%sidx='%s' fxy='%s' name='%s' action='%s'%n", indent, idx, fxy, name, action);
            indent.incr();
            show(child, indent, f);
            indent.decr();
        }
    }

    private void processSeq(Structure struct, Element parent) {
        if (parent == null || struct == null)
            return;
        List<Variable> vars = struct.getVariables();
        for (Element child : parent.getChildren("fld", Catalog.ncmlNS)) {
            String idxS = child.getAttributeValue("idx");
            int idx = Integer.parseInt(idxS);
            if (idx < 0 || idx >= vars.size()) {
                log.error("Bad index = {}", child);
                continue;
            }
            Variable want = vars.get(idx);
            struct.removeMemberVariable(want);
        }
    }

    private static class BufrStationDataset extends PointDatasetImpl {
        private Munge munger;
        private BufrCdmIndex index;
        private SequenceDS obs;

        private BufrStationDataset(NetcdfDataset ncfile, BufrCdmIndex index) {
            super(ncfile, FeatureType.STATION);
            this.index = index;

            // create the list of data variables
            munger = new Munge();
            obs = (SequenceDS) ncfile.findVariable(BufrIosp2.obsRecordName);
            this.dataVariables = munger.makeDataVariables(index, obs);

            BufrStationCollection bufrCollection = new BufrStationCollection(ncfile.getLocation());
            setPointFeatureCollection(bufrCollection);

            CalendarDateRange dateRange = CalendarDateRange.of(CalendarDate.of(index.start), CalendarDate.of(index.end));
            setDateRange(dateRange);
        }

        @Override
        public FeatureType getFeatureType() {
            return FeatureType.STATION;
        }

        @Override
        public void getDetailInfo(Formatter sf) {
            super.getDetailInfo(sf);
            index.showIndex(sf);
        }

        private class BufrStationCollection extends StationTimeSeriesCollectionImpl {
            StandardFields.StandardFieldsFromStructure extract;

            private BufrStationCollection(String name) {
                super(name, null, null);

                // need the center id to match the standard fields
                Attribute centerAtt = netcdfDataset.findGlobalAttribute(BufrIosp2.centerId);
                int center = (centerAtt == null) ? 0 : centerAtt.getNumericValue().intValue();
                this.extract = new StandardFields.StandardFieldsFromStructure(center, obs);

                try {
                    this.timeUnit = bufrDateUnits;
                } catch (Exception e) {
                    e.printStackTrace(); // cant happen
                }

                this.altUnits = "m"; // LOOK fake units
            }

            @Override
            protected StationHelper createStationHelper() {
                StationHelper stationHelper = new StationHelper();
                for (BufrCdmIndexProto.Station s : index.stations)
                    stationHelper.addStation(new BufrStation(s));

                return stationHelper;
            }

            private class BufrStation extends StationTimeSeriesFeatureImpl {
                private BufrStation(BufrCdmIndexProto.Station proto) {
                    super(proto.getId(), proto.getDesc(), proto.getWmoId(), proto.getLat(), proto.getLon(), proto.getAlt(),
                            bufrDateUnits, bufrAltUnits, proto.getCount(), StructureData.EMPTY);
                }

                @Override
                public PointFeatureIterator getPointFeatureIterator() throws IOException {
                    return new BufrStationIterator(obs.getStructureIterator(), null);
                }

                @Nonnull
                @Override
                public StructureData getFeatureData() {
                    return StructureData.EMPTY;
                }

                // iterates over the records for this station
                public class BufrStationIterator extends PointIteratorFromStructureData {
                    public BufrStationIterator(StructureDataIterator structIter, Filter filter) {
                        super(structIter, filter);
                    }

                    @Override
                    protected PointFeature makeFeature(int recnum, StructureData sdata) throws IOException {
                        extract.extract(sdata);
                        String stationId = extract.getStationId();
                        if (!stationId.equals(s.getName()))
                            return null;
                        CalendarDate date = extract.makeCalendarDate();
                        return new BufrStationPoint(s, date.getMillis(), 0, munger.munge(sdata)); // LOOK obsTime, nomTime
                    }
                }

                public class BufrStationPoint extends PointFeatureImpl implements StationFeatureHas {
                    StructureData sdata;

                    public BufrStationPoint(EarthLocation location, double obsTime, double nomTime, StructureData sdata) {
                        super(BufrStation.this, location, obsTime, nomTime, bufrDateUnits);
                        this.sdata = sdata;
                    }

                    @Nonnull
                    @Override
                    public StructureData getDataAll() {
                        return sdata;
                    }

                    @Nonnull
                    @Override
                    public StructureData getFeatureData() {
                        return sdata;
                    }

                    @Override
                    public StationFeature getStationFeature() {
                        return BufrStation.this;
                    }
                }
            }

            // flatten into a PointFeatureCollection
            // if empty, may return null
            @Override
            public PointFeatureCollection flatten(LatLonRect boundingBox, CalendarDateRange dateRange) throws IOException {
                return new BufrPointFeatureCollection(boundingBox, dateRange);
            }

            private class BufrPointFeatureCollection extends PointCollectionImpl {
                StationHelper stationsWanted;
                PointFeatureIterator.Filter filter;

                BufrPointFeatureCollection(LatLonRect boundingBox, CalendarDateRange dateRange) throws IOException {
                    super("BufrPointFeatureCollection", bufrDateUnits, bufrAltUnits);
                    setBoundingBox(boundingBox);
                    if (dateRange != null) {
                        getInfo();
                        info.setCalendarDateRange(dateRange);
                    }
                    createStationHelper();
                    stationsWanted = getStationHelper().subset(boundingBox);
                    if (dateRange != null)
                        filter = new PointIteratorFiltered.SpaceAndTimeFilter(null, dateRange);
                }

                @Override
                public PointFeatureIterator getPointFeatureIterator() throws IOException {
                    return new BufrRecordIterator(obs.getStructureIterator(), filter);
                }

                // iterates once over all the records
                public class BufrRecordIterator extends PointIteratorFromStructureData {
                    int countHere;

                    public BufrRecordIterator(StructureDataIterator structIter, Filter filter) {
                        super(structIter, filter);
                    }

                    @Override
                    protected PointFeature makeFeature(int recnum, StructureData sdata) throws IOException {
                        extract.extract(sdata);
                        String stationId = extract.getStationId();
                        StationFeature want = stationsWanted.getStation(stationId);
                        if (want == null)
                            return null;
                        CalendarDate date = extract.makeCalendarDate();
                        countHere++;
                        return new BufrPoint(want, date.getMillis(), 0, munger.munge(sdata));
                    }

                    @Override
                    public void close() {
                        log.debug(String.format("BufrRecordIterator passed %d features super claims %d%n", countHere,
                                getInfo().nfeatures));
                        super.close();
                    }

                }

                public class BufrPoint extends PointFeatureImpl implements StationPointFeature {
                    StructureData sdata;

                    public BufrPoint(StationFeature want, double obsTime, double nomTime, StructureData sdata) {
                        super(BufrPointFeatureCollection.this, want, obsTime, nomTime, bufrDateUnits);
                        this.sdata = sdata;
                    }

                    @Nonnull
                    @Override
                    public StructureData getDataAll() {
                        return sdata;
                    }

                    @Nonnull
                    @Override
                    public StructureData getFeatureData() {
                        return sdata;
                    }

                    @Override
                    public StationFeature getStation() {
                        return (StationFeature) location;
                    }
                }
            }

        }
    }

    private static class Action {
        BufrCdmIndexProto.FldAction what;

        private Action(BufrCdmIndexProto.FldAction what) {
            this.what = what;
        }
    }

    private static class Munge {
        String sdataName;
        boolean needed;
        protected Map<String, Action> actions = new HashMap<>(32);
        protected Map<String, StructureData> missingData = new HashMap<>(32);
        protected Map<String, VariableDS> vars = new HashMap<>(32);

        List<VariableSimpleIF> makeDataVariables(BufrCdmIndex index, Structure obs) {
            this.sdataName = obs.getShortName() + "Munged";

            List<Variable> members = obs.getVariables();
            List<VariableSimpleIF> result = new ArrayList<>(members.size());

            List<BufrCdmIndexProto.Field> flds = index.root.getFldsList();
            int count = 0;
            for (Variable v : members) {
                BufrCdmIndexProto.Field fld = flds.get(count++);
                if (fld.getAction() != null && fld.getAction() != BufrCdmIndexProto.FldAction.none) {
                    needed = true;
                    Action act = new Action(fld.getAction());
                    actions.put(v.getShortName(), act);

                    if (fld.getAction() == BufrCdmIndexProto.FldAction.remove) {
                        continue; // skip

                    } else if (fld.getAction() == BufrCdmIndexProto.FldAction.asMissing) {
                        // promote the children
                        Structure s = (Structure) v;
                        for (Variable child : s.getVariables()) {
                            result.add(child);
                            vars.put(child.getShortName(), (VariableDS) child); // track ones we may have to create missing values for
                        }
                        continue;
                    }
                }

                if (v.getDataType() == DataType.SEQUENCE)
                    continue;
                result.add(v);
            }
            return result;
        }

        StructureData munge(StructureData org) throws IOException {
            return needed ? new StructureDataMunged2(org) : org;
        }

        // LOOK needs to be ported to immutable StructureDataComposite
        private class StructureDataMunged2 extends StructureDataComposite {

            StructureDataMunged2(StructureData sdata) throws IOException {
                add(sdata);
                for (StructureMembers.Member m : sdata.getMembers()) {
                    Action act = actions.get(m.getName());
                    if (act == null) {
                        // do nothing

                    } else if (act.what == BufrCdmIndexProto.FldAction.remove) {
                        this.members.hideMember(m);

                    } else if (act.what == BufrCdmIndexProto.FldAction.asMissing) { // 0 or 1
                        int pos = this.members.hideMember(m);
                        ArraySequence seq = sdata.getArraySequence(m);
                        StructureDataIterator iter = seq.getStructureDataIterator();
                        if (iter.hasNext()) {
                            add(pos, iter.next());
                        } else {
                            // missing data
                            add(pos, makeMissing(m, seq));
                        }
                    }
                }
            }
        }

        StructureData makeMissing(StructureMembers.Member seqm, ArraySequence seq) {
            StructureData result = missingData.get(seqm.getName());
            if (result != null)
                return result;

            StructureMembers sm = seq.getStructureMembers().toBuilder(false).build();
            StructureDataW resultW = new StructureDataW(sm);
            for (StructureMembers.Member m : sm.getMembers()) {
                VariableDS var = vars.get(m.getName());
                Array missingData = var.getMissingDataArray(m.getShape());
                resultW.setMemberData(m, missingData);
            }

            missingData.put(seqm.getName(), resultW);
            return resultW;
        }

    } // Munge


}
