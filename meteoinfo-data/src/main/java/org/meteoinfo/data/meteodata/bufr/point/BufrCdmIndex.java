/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr.point;

import org.meteoinfo.data.meteodata.bufr.BufrConfig;
import ucar.nc2.ft.point.bufr.BufrCdmIndexProto;
import ucar.nc2.stream.NcStream;
import ucar.nc2.time.CalendarDate;
import ucar.unidata.io.RandomAccessFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manage cdm index (ncx) for Bufr files.
 * Covers BufrCdmIndexProto
 * Never completed for operational use, could redo as needed
 *
 * @author caron
 * @since 8/14/13
 */
public class BufrCdmIndex {
  private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BufrCdmIndex.class);

  public static final String MAGIC_START = "BufrCdmIndex";
  public static final String NCX_IDX = ".ncx";
  private static final int version = 1;

  public static File calcIndexFile(String bufrFilename) {
    File bufrFile = new File(bufrFilename);
    String name = bufrFile.getName();
    File result = new File(bufrFile.getParent(), name + BufrCdmIndex.NCX_IDX);
    if (result.exists())
      return result;

    int pos = name.indexOf('.');
    if (pos > 0) {
      name = name.substring(0, pos);
      result = new File(bufrFile.getParent(), name + BufrCdmIndex.NCX_IDX);
      if (result.exists())
        return result;
    }

    return null;
  }

  public static boolean writeIndex(String bufrFilename, BufrConfig config, File idxFile) throws IOException {
    return new BufrCdmIndex().writeIndex2(bufrFilename, config, idxFile);
  }

  public static BufrCdmIndex readIndex(String indexFilename) throws IOException {
    BufrCdmIndex index = new BufrCdmIndex();
    try (RandomAccessFile raf = RandomAccessFile.acquire(indexFilename)) {
      index.readIndex(raf);
    }
    return index;
  }

  /////////////////////////////////////////////////////////////////////////////////

  /*
   * MAGIC_START
   * version
   * sizeIndex
   * BufrCdmIndexProto (sizeIndex bytes)
   */
  private boolean writeIndex2(String bufrFilename, BufrConfig config, File indexFile) throws IOException {
    if (indexFile.exists()) {
      if (!indexFile.delete())
        log.warn(" BufrCdmIndex cant delete index file {}", indexFile.getPath());
    }
    log.debug(" createIndex for {}", indexFile.getPath());

    try (RandomAccessFile raf = new RandomAccessFile(indexFile.getPath(), "rw")) {
      raf.order(RandomAccessFile.BIG_ENDIAN);
      //// header message
      raf.write(MAGIC_START.getBytes(StandardCharsets.UTF_8));
      raf.writeInt(version);

      // build it
      BufrCdmIndexProto.BufrIndex.Builder indexBuilder = BufrCdmIndexProto.BufrIndex.newBuilder();
      indexBuilder.setFilename(bufrFilename);
      root = buildField(config.getRootConverter());
      indexBuilder.setRoot(root);
      indexBuilder.setStart(config.getStart());
      indexBuilder.setEnd(config.getEnd());
      indexBuilder.setNobs(config.getNobs());

      Map<String, BufrConfig.BufrStation> smaps = config.getStationMap();
      if (smaps != null) {
        List<BufrConfig.BufrStation> stations = new ArrayList<>(smaps.values());
        Collections.sort(stations);
        for (BufrConfig.BufrStation s : stations) {
          indexBuilder.addStations(buildStation(s));
        }
      }

      // write it
      BufrCdmIndexProto.BufrIndex index = indexBuilder.build();
      byte[] b = index.toByteArray();
      NcStream.writeVInt(raf, b.length); // message size
      raf.write(b); // message - all in one gulp

      log.debug("  file size = {} bytes", raf.length());
      return true;
    }
  }

  public static boolean writeIndex(BufrCdmIndex index, BufrField root, File indexFile) throws IOException {
    if (indexFile.exists()) {
      if (!indexFile.delete())
        log.warn(" BufrCdmIndex cant delete index file {}", indexFile.getPath());
    }
    log.debug(" createIndex for {}", indexFile.getPath());

    try (RandomAccessFile raf = new RandomAccessFile(indexFile.getPath(), "rw")) {
      raf.order(RandomAccessFile.BIG_ENDIAN);
      //// header message
      raf.write(MAGIC_START.getBytes(StandardCharsets.UTF_8));
      raf.writeInt(version);

      // build it
      BufrCdmIndexProto.BufrIndex.Builder indexBuilder = BufrCdmIndexProto.BufrIndex.newBuilder();
      indexBuilder.setFilename(index.bufrFilename);
      BufrCdmIndexProto.Field rootf = buildField(root);
      indexBuilder.setRoot(rootf);
      indexBuilder.setStart(index.start);
      indexBuilder.setEnd(index.end);
      indexBuilder.setNobs(index.nobs);

      if (index.stations != null) {
        for (BufrCdmIndexProto.Station s : index.stations) {
          indexBuilder.addStations(s);
        }
      }

      // write it
      BufrCdmIndexProto.BufrIndex indexOut = indexBuilder.build();
      byte[] b = indexOut.toByteArray();
      NcStream.writeVInt(raf, b.length); // message size
      raf.write(b); // message - all in one gulp
      log.debug("  write BufrCdmIndexProto= {} bytes", b.length);

      log.debug("  file size = {} bytes", raf.length());
      return true;
    }
  }

  private BufrCdmIndexProto.Station buildStation(BufrConfig.BufrStation s) {
    BufrCdmIndexProto.Station.Builder builder = BufrCdmIndexProto.Station.newBuilder();

    builder.setId(s.getName());
    builder.setCount(s.count);
    if (s.getWmoId() != null)
      builder.setWmoId(s.getWmoId());
    if (s.getDescription() != null)
      builder.setDesc(s.getDescription());
    builder.setLat(s.getLatitude());
    builder.setLon(s.getLongitude());
    builder.setAlt(s.getAltitude());

    return builder.build();
  }

  private static BufrCdmIndexProto.Field buildField(BufrField fld) {
    BufrCdmIndexProto.Field.Builder fldBuilder = BufrCdmIndexProto.Field.newBuilder();

    fldBuilder.setFxy(fld.getFxy());
    fldBuilder.setScale(fld.getScale());
    fldBuilder.setReference(fld.getReference());
    fldBuilder.setBitWidth(fld.getBitWidth());

    if (fld.getName() != null)
      fldBuilder.setName(fld.getName());

    if (fld.getDesc() != null)
      fldBuilder.setDesc(fld.getDesc());

    if (fld.getUnits() != null)
      fldBuilder.setUnits(fld.getUnits());

    if (fld.getChildren() != null) {
      for (BufrField child : fld.getChildren())
        fldBuilder.addFlds(buildField(child));
    }

    if (fld.getAction() != null && fld.getAction() != BufrCdmIndexProto.FldAction.none)
      fldBuilder.setAction(fld.getAction());

    if (fld.getType() != null)
      fldBuilder.setType(fld.getType());

    if (fld.isSeq()) {
      fldBuilder.setMin(fld.getMin());
      fldBuilder.setMax(fld.getMax());
    }

    return fldBuilder.build();
  }

  //////////////////////////////////////////////////////////////////

  public String idxFilename;
  public String bufrFilename;
  public BufrCdmIndexProto.Field root;
  public List<BufrCdmIndexProto.Station> stations;
  public long start, end;
  public long nobs;

  protected boolean readIndex(RandomAccessFile raf) {
    this.idxFilename = raf.getLocation();

    try {
      raf.order(RandomAccessFile.BIG_ENDIAN);
      raf.seek(0);

      //// header message
      if (!NcStream.readAndTest(raf, MAGIC_START.getBytes(StandardCharsets.UTF_8))) {
        log.error("BufrCdmIndex {}: invalid index", raf.getLocation());
        return false;
      }

      int indexVersion = raf.readInt();
      boolean versionOk = (indexVersion == version);
      if (!versionOk) {
        log.warn("BufrCdmIndex {}: index found version={}, want version= {}", raf.getLocation(), indexVersion, version);
        return false;
      }

      int size = NcStream.readVInt(raf);
      if ((size < 0) || (size > 100 * 1000 * 1000)) {
        log.warn("BufrCdmIndex {}: invalid or empty index ", raf.getLocation());
        return false;
      }

      byte[] m = new byte[size];
      raf.readFully(m);

      BufrCdmIndexProto.BufrIndex proto = BufrCdmIndexProto.BufrIndex.parseFrom(m);
      bufrFilename = proto.getFilename();
      root = proto.getRoot();
      stations = proto.getStationsList();
      start = proto.getStart();
      end = proto.getEnd();
      nobs = proto.getNobs();

      // showProtoRoot(root);

    } catch (Throwable t) {
      log.error("Error reading index " + raf.getLocation(), t);
      return false;
    }

    return true;
  }

  public void showIndex(Formatter f) {
    f.format("BufrCdmIndex %n");
    f.format("  idxFilename=%s%n", idxFilename);
    f.format("  bufrFilename=%s%n", bufrFilename);
    f.format("  dates=[%s,%s]%n", CalendarDate.of(start), CalendarDate.of(end));
    f.format("  nobs=%s%n", nobs);
    if (stations != null) {
      f.format("  # stations=%d%n", stations.size());
      int count = 0;
      for (BufrCdmIndexProto.Station s : stations)
        count += s.getCount();
      f.format("  # stations obs=%d%n", count);
    }

  }

}
