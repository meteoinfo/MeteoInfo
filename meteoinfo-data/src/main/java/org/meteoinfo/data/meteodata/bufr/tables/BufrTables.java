/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr.tables;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.meteoinfo.data.meteodata.bufr.Descriptor;
import org.meteoinfo.data.meteodata.bufr.MessageScanner;
import org.meteoinfo.data.meteodata.bufr.TableLookup;
import ucar.nc2.util.TableParser;
import ucar.nc2.wmo.Util;
import ucar.unidata.io.RandomAccessFile;
import ucar.unidata.util.StringUtil2;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads BUFR tables of various forms. Interacts with TableLookup.
 *
 * <pre>
 * Table B:
 *
 * csv----------
 * Class,FXY,enElementName,BUFR_Unit,BUFR_Scale,BUFR_ReferenceValue,BUFR_DataWidth_Bits,CREX_Unit,CREX_Scale,CREX_DataWidth,Status
 * 00,000001,Table A: entry,CCITT IA5,0,0,24,Character,0,3,Operational
 *
 *
 * ecmwf---------
 * 000001 TABLE A:  ENTRY                                                  CCITTIA5                   0            0  24 CHARACTER                 0          3
 * 000001 TABLE A:  ENTRY                                                  CCITTIA5                   0            0  24 CHARACTER                 0         3
 *
 *
 * mel-bufr-----------
 * 0; 7; 190; 1; -1024; 12; M; HEIGHT INCREMENT
 *
 *
 * mel-tabs (tab delimited) ---------------
 * F  X  Y  Scale  RefVal  Width  Units  Element Name
 * 0  0  1  0  0  24  CCITT_IA5  Table A: entry
 * 0  0  2  0  0  256  CCITT_IA5  Table A: data category description, line 1
 *
 *
 * ncep-----------
 * #====================================================================================================
 * # F-XX-YYY |SCALE| REFERENCE   | BIT |      UNIT      | MNEMONIC ;DESC ;  ELEMENT NAME
 * #          |     |   VALUE     |WIDTH|                |          ;CODE ;
 * #====================================================================================================
 * 0-00-001 |   0 |           0 |  24 | CCITT IA5      | TABLAE   ;     ; Table A: entry
 *
 *
 * opera ----------------------------
 * 0;02;181;Supplementary present weather sensor;Flag-Table;0;0;21
 * 0;07;192;Pixel size in Z-direction;Meters;-1;0;16
 * 0;21;036;Radar rainfall intensity;mm*h-1;2;0;16
 *
 *
 * ===============================================================
 * Table D:
 * csv----------
 * SNo,Category,FXY1,enElementName1,FXY2,enElementName2,Status
 * 1,00,300002,,000002,"Table A category, line 1",Operational
 *
 * ecmwf-------------
 * 300002  2 000002
 * 000003
 * 300003  3 000010
 * 000011
 * 000012
 *
 * mel-bufr------------
 * 3   1 192  optional_name
 * 0   1   7
 * 0  25  60
 * 0   1  33
 * 1   1   2
 * 3  61 169
 * 0   5  40
 * -1
 *
 * ncep ------------------
 * #====================================================================================================
 * # F-XX-YYY | MNEMONIC   ;DCOD ; NAME           <-- sequence definition
 * #          | F-XX-YYY > | NAME                 <-- element definition (first thru next-to-last)
 * #          | F-XX-YYY   | NAME                 <-- element definition (last)
 * #====================================================================================================
 *
 * 3-00-002 | TABLACAT   ;     ; Table A category definition
 * | 0-00-002 > | Table A category, line 1
 * | 0-00-003   | Table A category, line 2
 *
 * opera ---------------
 * # Heights of side view
 * 3;13;192;  1;01;000
 * ;  ;   ;  0;31;001
 * ;  ;   ;  0;10;007
 * # 4 bit per pixel radar images (top view)
 * 3;21;192;  1;10;000
 * ...
 *
 * </pre>
 */

public class BufrTables {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BufrTables.class);

    public enum Mode {
        wmoOnly, // wmo entries only found from wmo table
        wmoLocal, // if wmo entries not found in wmo table, look in local table
        localOverride // look in local first, then wmo
    }

    public enum Format {
        ecmwf, mel_bufr, mel_tabs, ncep, ncep_nm, opera, ukmet, wmo_csv, wmo_xml, cypher, embed
    }

    static final String RESOURCE_PATH = "/resources/bufrTables/";
    private static final String canonicalLookup = "resource:" + RESOURCE_PATH + "local/tablelookup.csv";
    private static final int latestVersion = 37;

    private static final boolean showTables = false;
    private static final boolean showReadErrs = true;

    private static List<TableConfig> tables;
    private static final Map<String, TableB> tablesB = new ConcurrentHashMap<>();
    private static final Map<String, TableD> tablesD = new ConcurrentHashMap<>();

    private static List<String> lookups;

    public static synchronized void addLookupFile(String filename) throws FileNotFoundException {
        if (lookups == null)
            lookups = new ArrayList<>();
        File f = new File(filename);
        if (!f.exists())
            throw new FileNotFoundException(filename + " not found");
        lookups.add(filename);
    }

    private static synchronized void readLookupTable() {
        tables = new ArrayList<>();
        if (lookups != null) {
            lookups.add(canonicalLookup);
            for (String fname : lookups)
                readLookupTable(fname);
        } else {
            readLookupTable(canonicalLookup);
        }
    }

    // center,subcenter,master,local,cat,tableB,tableBformat,tableD,tableDformat, mode
    private static void readLookupTable(String filename) {

        try (InputStream ios = openStream(filename)) {
            BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));
            int count = 0;
            while (true) {
                String line = dataIS.readLine();
                if (line == null)
                    break;
                if (line.startsWith("#"))
                    continue;
                count++;

                String[] flds = line.split(",");
                if (flds.length < 8) {
                    if (showReadErrs)
                        System.out.printf("%d BAD line == %s%n", count, line);
                    continue;
                }

                int fldidx = 0;
                try {
                    TableConfig table = new TableConfig();
                    table.name = flds[fldidx++].trim();
                    table.center = Integer.parseInt(flds[fldidx++].trim());
                    table.subcenter = Integer.parseInt(flds[fldidx++].trim());
                    table.master = Integer.parseInt(flds[fldidx++].trim());
                    table.local = Integer.parseInt(flds[fldidx++].trim());
                    table.cat = Integer.parseInt(flds[fldidx++].trim());
                    table.tableBname = flds[fldidx++].trim();
                    table.tableBformat = getFormat(flds[fldidx++].trim(), line);
                    if (table.tableBformat == null)
                        continue;

                    table.tableDname = flds[fldidx++].trim();
                    table.tableDformat = getFormat(flds[fldidx++].trim(), line);

                    if (fldidx < flds.length) {
                        String modes = flds[fldidx++].trim();
                        if (modes.equalsIgnoreCase("wmoLocal"))
                            table.mode = Mode.wmoLocal;
                        else if (modes.equalsIgnoreCase("localWmo"))
                            table.mode = Mode.localOverride;
                    }

                    tables.add(table);
                    if (showTables)
                        System.out.printf("Added table == %s%n", table);

                } catch (Exception e) {
                    if (showReadErrs)
                        System.out.printf("%d %d BAD line == %s (%s)%n", count, fldidx, line, e.getMessage());
                }
            }

        } catch (IOException ioe) {
            String mess = "Need BUFR tables in path; looking for " + filename;
            throw new RuntimeException(mess, ioe);
        }
    }

    private static Format getFormat(String formatS, String line) {
        if (formatS.isEmpty())
            return null;
        try {
            return Format.valueOf(formatS);
        } catch (Exception e) {
            if (showReadErrs)
                log.warn("BAD format = {} line == {}%n", formatS, line);
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static class TableConfig {
        String name;
        int center, subcenter, master, local, cat;
        String tableBname, tableDname;
        Format tableBformat, tableDformat;
        Mode mode = Mode.localOverride;

        boolean matches(int center, int subcenter, int master, int local, int cat) {
            if ((this.center >= 0) && (center >= 0) && (center != this.center))
                return false;
            if ((this.subcenter >= 0) && (subcenter >= 0) && (subcenter != this.subcenter))
                return false;
            if ((this.master >= 0) && (master >= 0) && (master != this.master))
                return false;
            if ((this.local >= 0) && (local >= 0) && (local != this.local))
                return false;
            return (this.cat < 0) || (cat < 0) || (cat == this.cat);
        }

        public String getName() {
            return name;
        }

        public Format getTableBformat() {
            return tableBformat;
        }

        public Format getTableDformat() {
            return tableDformat;
        }

        public Mode getMode() {
            return mode;
        }

        public String getTableDname() {
            return tableDname;
        }

        public String getTableBname() {
            return tableBname;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public static List<TableConfig> getTables() {
        if (tables == null)
            readLookupTable();
        return tables;
    }

    public static TableConfig[] getTableConfigsAsArray() {
        if (tables == null)
            readLookupTable();
        TableConfig[] result = new TableConfig[tables.size()];
        return tables.toArray(result);
    }

    private static TableConfig matchTableConfig(int center, int subcenter, int master, int local, int cat) {
        if (tables == null)
            readLookupTable();

        for (TableConfig tc : tables) {
            if (tc.matches(center, subcenter, master, local, cat))
                return tc;
        }
        return null;
    }

    /*
     * private static TableConfig matchTableConfig(BufrIdentificationSection ids) {
     * if (tables == null) readLookupTable();
     *
     * int center = ids.getCenterId();
     * int subcenter = ids.getSubCenterId();
     * int master = ids.getMasterTableVersion();
     * int local = ids.getLocalTableVersion();
     * int cat = ids.getCategory();
     *
     * return matchTableConfig(center, subcenter, master, local, cat);
     * }
     */

    ///////////////////////

    /*
     * Note Robb has this cleanup in DescriptorTableB
     * desc = desc.replaceFirst( "\\w+\\s+TABLE B ENTRY( - )?", "" );
     * desc = desc.trim();
     * this.description = desc;
     * desc = desc.replaceAll( "\\s*\\(.*\\)", "" );
     * desc = desc.replaceAll( "\\*", "" );
     * desc = desc.replaceAll( "\\s+", "_" );
     * desc = desc.replaceAll( "\\/", "_or_" );
     * desc = desc.replaceFirst( "1-", "One-" );
     * desc = desc.replaceFirst( "2-", "Two-" );
     */

    public static class Tables {
        public TableB b;
        public TableD d;
        public Mode mode;

        Tables() {
        }

        Tables(TableB b, TableD d, Mode mode) {
            this.b = b;
            this.d = d;
            this.mode = (mode == null) ? Mode.wmoOnly : mode;
        }
    }

    public static Tables getLocalTables(int center, int subcenter, int master, int local, int cat) throws IOException {
        TableConfig tc = matchTableConfig(center, subcenter, master, local, cat);
        if (tc == null)
            return null;

        if (tc.tableBformat == Format.ncep_nm) { // LOOK ??
            // see if we already have it
            TableB b = tablesB.get(tc.tableBname);
            TableD d = tablesD.get(tc.tableBname);
            if ((b != null) && (d != null))
                return new Tables(b, d, tc.mode);

            // read it
            b = new TableB(tc.tableBname, tc.tableBname);
            d = new TableD(tc.tableBname, tc.tableBname);
            Tables t = new Tables(b, d, tc.mode);
            try (InputStream ios = openStream(tc.tableBname)) {
                NcepMnemonic.read(ios, t);
            }

            // cache
            tablesB.put(tc.tableBname, t.b); // assume we would get the same table in any thread, so race condition is ok
            tablesD.put(tc.tableBname, t.d); // assume we would get the same table in any thread, so race condition is ok
            return t;
        }

        Tables tables = new Tables();
        tables.b = readTableB(tc.tableBname, tc.tableBformat, false);
        tables.d = readTableD(tc.tableDname, tc.tableDformat, false);
        tables.mode = tc.mode;

        return tables;
    }

    ////////////////////////////////////////////////////

    private static TableB latestWmoB;

    public static synchronized TableB getWmoTableBlatest() {
        if (latestWmoB == null) {
            try {
                latestWmoB = getWmoTableB(latestVersion);
            } catch (IOException ioe) {
                log.error("Cant open latest WMO ", ioe);
                throw new RuntimeException(ioe);
            }
        }
        return latestWmoB;
    }

    /*
     * // private static final String version14 = "wmo.v14";
     * static public TableB getWmoTableBold(int version) throws IOException {
     * String version13 = "wmo.v13.composite";
     * String tableName = (version == 14) ? version14 : version13;
     * TableB tb = tablesB.get(tableName);
     * if (tb != null) return tb;
     *
     * // always read 14 in
     * TableConfig tc14 = matchTableConfig(0, 0, 14, 0, -1);
     * TableB result = readTableB(tc14.tableBname, tc14.tableBformat, false);
     * tablesB.put(version14, result); // hash by standard name
     *
     * // everyone else uses 13 : cant override - do it in local if needed
     * if (version < 14) {
     * TableConfig tc = matchTableConfig(0, 0, 13, 0, -1);
     * TableB b13 = readTableB(tc.tableBname, tc.tableBformat, false);
     * TableB.Composite bb = new TableB.Composite(version13, version13);
     * bb.addTable(b13); // check in 13 first, so it overrides
     * bb.addTable(result); // then in 14
     * result = bb;
     * tablesB.put(version13, result); // hash by standard name
     * }
     *
     * return result;
     * }
     */

    public static TableB getWmoTableB(int masterTableVersion) throws IOException {
        TableConfig tc = matchTableConfig(0, 0, masterTableVersion, 0, -1);
        if (tc != null)
            return readTableB(tc.tableBname, tc.tableBformat, false);
        return null;
    }

    public static TableB readTableB(String location, Format format, boolean force) throws IOException {
        if (!force) {
            TableB tb = tablesB.get(location);
            if (tb != null)
                return tb;
        }
        if (showTables)
            System.out.printf("Read BufrTable B %s format=%s%n", location, format);

        TableB b = new TableB(location, location);
        try (InputStream ios = openStream(location)) {
            switch (format) {
                case cypher:
                    readCypherTableB(ios, b);
                    break;
                case ecmwf:
                    readEcmwfTableB(ios, b);
                    break;
                case embed:
                    b = readEmbeddedTableB(location);
                    break;
                case ncep:
                    readNcepTableB(ios, b);
                    break;
                case ncep_nm:
                    Tables t = new Tables(b, null, null);
                    NcepMnemonic.read(ios, t);
                    break;
                case mel_bufr:
                    readMelbufrTableB(ios, b);
                    break;
                case mel_tabs:
                    readMeltabTableB(ios, b);
                    break;
                case opera:
                    readOperaTableB(ios, b);
                    break;
                case ukmet:
                    readBmetTableB(ios, b);
                    break;
                case wmo_csv:
                    readWmoCsvTableB(ios, b);
                    break;
                case wmo_xml:
                    WmoXmlReader.readWmoXmlTableB(ios, b);
                    break;
            }
        }

        if (b != null)
            tablesB.put(location, b); // assume we would get the same table in any thread, so race condition is ok
        return b;
    }

    private static void readWmoCsvTableB(InputStream ios, TableB b) throws IOException {
        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));
        int count = 0;
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.startsWith("#"))
                continue;
            count++;

            if (count == 1) { // skip first line - its the header
                continue;
            }

            // any commas that are embedded in quotes - replace with blanks for now so split works
            int pos1 = line.indexOf('"');
            if (pos1 >= 0) {
                int pos2 = line.indexOf('"', pos1 + 1);
                StringBuilder sb = new StringBuilder(line);
                for (int i = pos1; i < pos2; i++)
                    if (sb.charAt(i) == ',')
                        sb.setCharAt(i, ' ');
                line = sb.toString();
            }

            String[] flds = line.split(",");
            if (flds.length < 7) {
                if (showReadErrs)
                    System.out.printf("%d BAD split == %s%n", count, line);
                continue;
            }

            int fldidx = 1; // Start at 1 to skip classId
            try {
                int xy = Integer.parseInt(flds[fldidx++].trim());
                String name = StringUtil2.remove(flds[fldidx++], '"');
                String units = StringUtil2.filter(flds[fldidx++], " %+-_/()*"); // alphanumeric plus these chars
                int scale = Integer.parseInt(clean(flds[fldidx++].trim()));
                int refVal = Integer.parseInt(clean(flds[fldidx++].trim()));
                int width = Integer.parseInt(clean(flds[fldidx++].trim()));

                int x = xy / 1000;
                int y = xy % 1000;

                b.addDescriptor((short) x, (short) y, scale, refVal, width, name, units, null);

            } catch (Exception e) {
                if (showReadErrs)
                    System.out.printf("%d %d BAD line == %s%n", count, fldidx, line);
            }
        }
    }

    private static String clean(String s) {
        return StringUtil2.remove(s, ' ');
    }

    private static TableB readEmbeddedTableB(String location) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(location, "r")) {
            MessageScanner scan = new MessageScanner(raf);
            TableLookup lookup = scan.getTableLookup();
            if (lookup != null) {
                return lookup.getLocalTableB();
            }
            return null;
        }
    }

    private static TableD readEmbeddedTableD(String location) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(location, "r")) {
            MessageScanner scan = new MessageScanner(raf);
            TableLookup lookup = scan.getTableLookup();
            if (lookup != null) {
                return lookup.getLocalTableD();
            }
            return null;
        }
    }

    // tables are in mel-bufr format
    private static TableB readMelbufrTableB(InputStream ios, TableB b) throws IOException {

        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));

        // read table B looking for descriptors
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.startsWith("#") || line.isEmpty())
                continue;

            try {
                String[] split = line.split(";");
                short x = Short.parseShort(split[1].trim());
                short y = Short.parseShort(split[2].trim());
                int scale = Integer.parseInt(split[3].trim());
                int refVal = Integer.parseInt(split[4].trim());
                int width = Integer.parseInt(split[5].trim());

                b.addDescriptor(x, y, scale, refVal, width, split[7], split[6], null);
            } catch (Exception e) {
                log.error("Bad table B entry: table=" + b.getName() + " entry=<" + line + ">", e.getMessage());
            }
        }

        return b;
    }

    /*
     * tables are in cypher format : http://www.northern-lighthouse.com/tables/B_d00v13.htm
     * #
     * 002063 Aircraft roll angle
     * Degree
     * 2 -18000 16
     */

    private static TableB readCypherTableB(InputStream ios, TableB b) throws IOException {
        boolean startMode = false;
        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.isEmpty())
                continue;
            if (line.startsWith("<"))
                continue;

            if (line.startsWith("#")) {
                startMode = true;
                continue;
            }

            if (startMode) {
                try {
                    String xys = line.substring(0, 8).trim();
                    int xy = Integer.parseInt(xys);
                    short x = (short) (xy / 1000);
                    short y = (short) (xy % 1000);

                    String name = Util.cleanName(line.substring(8));
                    String units = "";
                    line = dataIS.readLine();
                    if (line != null)
                        units = WmoXmlReader.cleanUnit(line);

                    int scale = 0, refVal = 0, width = 0;
                    line = dataIS.readLine();
                    if (line != null) {
                        line = StringUtil2.remove(line, '*');
                        String[] split = StringUtil2.splitString(line);
                        scale = Integer.parseInt(split[0].trim());
                        refVal = Integer.parseInt(split[1].trim());
                        width = Integer.parseInt(split[2].trim());
                    }

                    b.addDescriptor(x, y, scale, refVal, width, name, units, null);
                    startMode = false;
                } catch (Exception e) {
                    log.error("Bad table B entry: table=" + b.getName() + " entry=<" + line + ">", e.getMessage());
                }
            }
        }

        return b;
    }

    // tables are in mel-bufr format
    // #F X Y Scale RefVal Width Units Element Name
    // 0 0 1 0 0 24 CCITT_IA5 Table A: entry
    private static TableB readMeltabTableB(InputStream ios, TableB b) throws IOException {

        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));

        // read table B looking for descriptors
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.startsWith("#") || line.isEmpty())
                continue;

            try {
                String[] split = line.split("\t");
                short x = Short.parseShort(split[1].trim());
                short y = Short.parseShort(split[2].trim());
                int scale = Integer.parseInt(split[3].trim());
                int refVal = Integer.parseInt(split[4].trim());
                int width = Integer.parseInt(split[5].trim());

                b.addDescriptor(x, y, scale, refVal, width, split[7], split[6], null);
            } catch (Exception e) {
                log.error("Bad table " + b.getName() + " entry=<" + line + ">", e);
            }
        }

        return b;
    }

    // F-XX-YYY |SCALE| REFERENCE | BIT | UNIT | MNEMONIC ;DESC ; ELEMENT NAME
    private static TableB readNcepTableB(InputStream ios, TableB b) throws IOException {

        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));

        dataIS.readLine(); // throw first line away

        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.startsWith("#") || line.isEmpty())
                continue;

            try {
                String[] flds = line.split("[\\|;]");
                if (flds[0].equals("END"))
                    break;

                if (flds.length < 8) {
                    log.error("Bad line in table " + b.getName() + " entry=<" + line + ">");
                    continue;
                }

                String fxys = flds[0];
                int scale = Integer.parseInt(clean(flds[1]));
                int refVal = Integer.parseInt(clean(flds[2]));
                int width = Integer.parseInt(clean(flds[3]));
                String units = StringUtil2.remove(flds[4], '"');
                String name = StringUtil2.remove(flds[5], '"');
                String desc = StringUtil2.remove(flds[7], '"');

                String[] xyflds = fxys.split("-");
                short x = Short.parseShort(clean(xyflds[1]));
                short y = Short.parseShort(clean(xyflds[2]));

                b.addDescriptor(x, y, scale, refVal, width, name, units, desc);

            } catch (Exception e) {
                log.error("Bad table " + b.getName() + " entry=<" + line + ">", e);
            }
        }

        return b;
    }

    /*
     * opera ----------------------------
     * 0;02;181;Supplementary present weather sensor;Flag-Table;0;0;21
     * 0;07;192;Pixel size in Z-direction;Meters;-1;0;16
     * 0;21;036;Radar rainfall intensity;mm*h-1;2;0;16
     */
    private static void readOperaTableB(InputStream ios, TableB b) throws IOException {
        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));
        int count = 0;
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.startsWith("#"))
                continue;
            count++;

            String[] flds = line.split(";");
            if (flds.length < 8) {
                if (showReadErrs)
                    System.out.printf("%d BAD split == %s%n", count, line);
                continue;
            }

            int fldidx = 1; // skip classId
            try {
                int x = Integer.parseInt(flds[fldidx++].trim());
                int y = Integer.parseInt(flds[fldidx++].trim());
                String name = StringUtil2.remove(flds[fldidx++], '"');
                String units = StringUtil2.filter(flds[fldidx++], " %+-_/()*"); // alphanumeric plus these chars
                int scale = Integer.parseInt(clean(flds[fldidx++].trim()));
                int refVal = Integer.parseInt(clean(flds[fldidx++].trim()));
                int width = Integer.parseInt(clean(flds[fldidx++].trim()));

                b.addDescriptor((short) x, (short) y, scale, refVal, width, name, units, null);

            } catch (Exception e) {
                if (showReadErrs)
                    System.out.printf("%d %d BAD line == %s%n", count, fldidx, line);
            }
        }
    }

    /*
     * fxy name units scale ref w units
     * 01234567 72 97 102 119
     * 001015 STATION OR SITE NAME CCITTIA5 0 0 160 CHARACTER 0 20
     * 001041 ABSOLUTE PLATFORM VELOCITY - FIRST COMPONENT (SEE NOTE 6) M/S 5 -1073741824 31 M/S 5 10
     */
    private static TableB readEcmwfTableB(InputStream ios, TableB b) throws IOException {
        List<TableParser.Record> recs = TableParser.readTable(ios, "4i,7i,72,97,102i,114i,119i", 50000);
        for (TableParser.Record record : recs) {
            if (record.nfields() < 7) {
                continue;
            }
            int x = (Integer) record.get(0);
            int y = (Integer) record.get(1);
            String name = (String) record.get(2);
            String units = (String) record.get(3);
            int scale = (Integer) record.get(4);
            int ref = (Integer) record.get(5);
            int width = (Integer) record.get(6);

            b.addDescriptor((short) x, (short) y, scale, ref, width, name, units, null);
        }

        return b;
    }

    private static void readBmetTableB(InputStream ios, TableB b) throws IOException {
        org.jdom2.Document doc;
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setExpandEntities(false);
            doc = builder.build(ios);
        } catch (JDOMException e) {
            throw new IOException(e.getMessage());
        }

        Element root = doc.getRootElement();
        List<Element> featList = root.getChildren("featureCatalogue");
        for (Element featureCat : featList) {
            List<Element> features = featureCat.getChildren("feature");

            for (Element feature : features) {
                String name = feature.getChild("annotation").getChildTextNormalize("documentation");
                int f = Integer.parseInt(feature.getChildText("F"));
                int x = Integer.parseInt(feature.getChildText("X"));
                int y = Integer.parseInt(feature.getChildText("Y"));
                int fxy = (f << 16) + (x << 8) + y;

                Element bufrElem = feature.getChild("BUFR");
                String units = bufrElem.getChildTextNormalize("BUFR_units");
                int scale = 0, reference = 0, width = 0;

                String s = null;
                try {
                    s = bufrElem.getChildTextNormalize("BUFR_scale");
                    scale = Integer.parseInt(clean(s));
                } catch (NumberFormatException e) {
                    log.warn(" key {} name '{}' has bad scale='{}'%n", fxy, name, s);
                }

                try {
                    s = bufrElem.getChildTextNormalize("BUFR_reference");
                    reference = Integer.parseInt(clean(s));
                } catch (NumberFormatException e) {
                    log.warn(" key {} name '{}' has bad reference='{}' %n", fxy, name, s);
                }

                try {
                    s = bufrElem.getChildTextNormalize("BUFR_width");
                    width = Integer.parseInt(clean(s));
                } catch (NumberFormatException e) {
                    log.warn(" key {} name '{}' has bad width='{}' %n", fxy, name, s);
                }

                b.addDescriptor((short) x, (short) y, scale, reference, width, name, units, null);
            }
        }

    }

    ///////////////////////////////////////////////////////

    private static TableD latestWmoD;

    public static synchronized TableD getWmoTableDlatest() {
        if (latestWmoD == null) {
            try {
                latestWmoD = getWmoTableD(latestVersion);
            } catch (IOException ioe) {
                log.error("Cant open latest WMO ", ioe);
                throw new RuntimeException(ioe);
            }
        }
        return latestWmoD;
    }

    public static TableD getWmoTableD(int masterTableVersion) throws IOException {
        TableConfig tc = matchTableConfig(0, 0, masterTableVersion, 0, -1);
        if (tc != null)
            return readTableD(tc.tableDname, tc.tableDformat, false);
        return null;
    }

    public static TableD readTableD(String location, Format format, boolean force) throws IOException {
        if (location == null)
            return null;
        if (location.trim().isEmpty())
            return null;

        if (!force) {
            TableD tb = tablesD.get(location);
            if (tb != null)
                return tb;
        }

        if (showTables)
            System.out.printf("Read BufrTable D %s format=%s%n", location, format);
        TableD d = new TableD(location, location);

        try (InputStream ios = openStream(location)) {
            switch (format) {
                case cypher:
                    readCypherTableD(ios, d);
                    break;
                case embed:
                    d = readEmbeddedTableD(location);
                    break;
                case ncep:
                    readNcepTableD(ios, d);
                    break;
                case ncep_nm:
                    Tables t = new Tables(null, d, null);
                    NcepMnemonic.read(ios, t);
                    break;
                case ecmwf:
                    readEcmwfTableD(ios, d);
                    break;
                case mel_bufr:
                    readMelbufrTableD(ios, d);
                    break;
                case opera:
                    readOperaTableD(ios, d);
                    break;
                case wmo_csv:
                    readWmoCsvTableD(ios, d);
                    break;
                case wmo_xml:
                    WmoXmlReader.readWmoXmlTableD(ios, d);
                    break;
                default:
                    log.warn("Unknown format= {}", format);
                    return null;
            }
        }

        if (d != null)
            tablesD.put(location, d); // assume we would get the same table in any thread, so race condition is ok
        return d;
    }

    /*
     * #
     * <A HREF="#C40"> 40</A>
     * </TT></P>
     * <PRE></PRE>
     * <HR>
     * <A NAME="C00"></A>
     * <H4 ALIGN="CENTER">Class 00</H4>
     * <PRE>
     * #
     * 300002
     * 000002 Table A category, line 1
     * 000003 Table A category, line 2
     * #
     * 300003
     * 000010 F, part descriptor
     * 000011 X, part descriptor
     * 000012 Y, part descriptor
     * #
     * 300004
     * 300003
     * 000013 Element name, line 1
     * 000014 Element name, line 2
     * 000015 Units name
     * 000016 Units scale sign
     * 000017 Units scale
     * 000018 Units reference sign
     * 000019 Units reference value
     * 000020 Element data width
     */
    private static void readCypherTableD(InputStream ios, TableD t) throws IOException {
        TableD.Descriptor currDesc = null;
        boolean startMode = false;

        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.isEmpty())
                continue;
            if (line.startsWith("<"))
                continue;

            if (line.startsWith("#")) {
                startMode = true;
                continue;
            }

            if (startMode) {
                try {
                    String[] flds = StringUtil2.splitString(line);
                    int fxy = Integer.parseInt(flds[0]);
                    int y = fxy % 1000;
                    fxy /= 1000;
                    int x = fxy % 100;
                    int f1 = fxy / 100;
                    if (f1 != 3)
                        log.error("Bad table " + t.getName() + " entry=<" + line + ">");
                    else
                        currDesc = t.addDescriptor((short) x, (short) y, "", new ArrayList<>());
                    startMode = false;
                    continue;

                } catch (Exception e) {
                    log.warn("Bad table " + t.getName() + " line=<" + line + ">", e.getMessage());
                }
            }
            if (currDesc != null) {
                try {
                    String[] flds = StringUtil2.splitString(line);
                    String fxys = cleanNumber(flds[0]);
                    int fxy = Integer.parseInt(fxys);
                    int y1 = fxy % 1000;
                    fxy /= 1000;
                    int x1 = fxy % 100;
                    int f1 = fxy / 100;
                    int fxy1 = (f1 << 14) + (x1 << 8) + y1;
                    currDesc.addFeature((short) fxy1);
                } catch (Exception e) {
                    log.warn("Bad table " + t.getName() + " line=<" + line + ">", e.getMessage());
                }
            } else {
                log.warn("Bad table " + t.getName() + " line=<" + line + ">" + " trying to add feature without descriptor.");
            }
        }
    }

    static String cleanNumber(String s) {
        int pos = s.indexOf("(");
        if (pos > 0)
            return s.substring(0, pos);
        return s;
    }

    /*
     * opera:
     * # Heights of side view
     * 3;13;192; 1;01;000
     * ; ; ; 0;31;001
     * ; ; ; 0;10;007
     * # 4 bit per pixel radar images (top view)
     * 3;21;192; 1;10;000
     * ...
     */
    private static void readOperaTableD(InputStream ios, TableD t) throws IOException {

        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));

        TableD.Descriptor currDesc = null;

        String name = null;
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            line = line.trim();
            if (line.isEmpty())
                continue;
            if (line.startsWith("#")) {
                name = line.substring(2).trim();
                continue;
            }

            try {
                String[] flds = line.split(";");
                if (!flds[0].trim().isEmpty()) {
                    int x = Integer.parseInt(flds[1].trim());
                    int y = Integer.parseInt(flds[2].trim());
                    currDesc = t.addDescriptor((short) x, (short) y, name, new ArrayList<>());
                }

                if (currDesc != null) {
                    int f1 = Integer.parseInt(flds[3].trim());
                    int x1 = Integer.parseInt(flds[4].trim());
                    int y1 = Integer.parseInt(flds[5].trim());
                    int fxy = (f1 << 14) + (x1 << 8) + y1;
                    currDesc.addFeature((short) fxy);
                } else {
                    throw new Exception("Trying to add feature to null descriptor");
                }

            } catch (Exception e) {
                log.error("Bad table " + t.getName() + " entry=<" + line + ">", e);
            }
        }
    }


    private static void readWmoCsvTableD(InputStream ios, TableD tableD) throws IOException {
        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));
        int count = 0;
        int currSeqno = -1;
        TableD.Descriptor currDesc = null;

        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.startsWith("#"))
                continue;
            count++;

            if (count == 1) {
                continue;
            }

            // commas embedded in quotes - replace with blanks for now
            int pos1 = line.indexOf('"');
            if (pos1 >= 0) {
                int pos2 = line.indexOf('"', pos1 + 1);
                StringBuilder sb = new StringBuilder(line);
                for (int i = pos1; i < pos2; i++)
                    if (sb.charAt(i) == ',')
                        sb.setCharAt(i, ' ');
                line = sb.toString();
            }

            String[] flds = line.split(",");
            if (flds.length < 5) {
                if (showReadErrs)
                    System.out.printf("%d INCOMPLETE line == %s%n", count, line);
                continue;
            }

            int fldidx = 2; // skip sno and cat
            try {
                int seq = Integer.parseInt(flds[fldidx++]);
                String seqName = flds[fldidx++];
                String featno = flds[fldidx++].trim();
                if (featno.isEmpty()) {
                    if (showReadErrs)
                        System.out.printf("%d no FXY2 specified; line == %s%n", count, line);
                    continue;
                }

                if (currSeqno != seq) {
                    int y = seq % 1000;
                    int w = seq / 1000;
                    int x = w % 100;
                    seqName = StringUtil2.remove(seqName, '"');
                    currDesc = tableD.addDescriptor((short) x, (short) y, seqName, new ArrayList<>());
                    currSeqno = seq;
                }

                int fno = Integer.parseInt(featno);
                int y = fno % 1000;
                int w = fno / 1000;
                int x = w % 100;
                int f = w / 100;

                int fxy = (f << 14) + (x << 8) + y;
                if (currDesc != null) {
                    currDesc.addFeature((short) fxy);
                } else {
                    log.error("Trying to add feature to null desc!");
                }

            } catch (Exception e) {
                if (showReadErrs)
                    System.out.printf("%d %d BAD line == %s : %s%n", count, fldidx, line, e.getMessage());
            }
        }
    }

    private static final Pattern threeInts = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)"); // get 3 integers from
    // beginning of line
    private static final Pattern negOne = Pattern.compile("^\\s*-1"); // check for -1 sequence terminator

    private static void readMelbufrTableD(InputStream ios, TableD t) throws IOException {

        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));
        int count = 0;

        // read table D to store sequences and their descriptors
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            count++;
            // check for comment lines
            if (line.startsWith("#") || line.isEmpty())
                continue;

            line = line.trim();
            String[] split = line.split("[ \t]+"); // 1 or more whitespace
            if (split.length < 3)
                continue;
            if (split[0].equals("END"))
                break;

            try {
                short seqF = Short.parseShort(split[0]);
                short seqX = Short.parseShort(split[1]);
                short seqY = Short.parseShort(split[2]);
                assert seqF == 3;

                String seqName = "";
                if (split.length > 3) {
                    StringBuilder sb = new StringBuilder(40);
                    for (int i = 3; i < split.length; i++)
                        sb.append(split[i]).append(" ");
                    seqName = sb.toString();
                    seqName = StringUtil2.remove(seqName, "()");
                }

                List<Short> seq = new ArrayList<>();
                // look for descriptors within sequence terminated by -1
                while (true) {
                    line = dataIS.readLine();
                    if (line == null)
                        break;
                    count++;
                    // check for comment lines
                    if (line.startsWith("#") || line.isEmpty())
                        continue;

                    Matcher m = threeInts.matcher(line);
                    // descriptor found
                    if (m.find()) {
                        short f = Short.parseShort(m.group(1));
                        short x = Short.parseShort(m.group(2));
                        short y = Short.parseShort(m.group(3));
                        seq.add(Descriptor.getFxy(f, x, y));

                    } else {
                        m = negOne.matcher(line);
                        if (m.find()) {
                            // store this sequence
                            t.addDescriptor(seqX, seqY, seqName, seq);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("TableD " + t.getName() + " Failed on line " + count + " = " + line + "\n " + e);
                // e.printStackTrace();
            }
        }
    }

    /*
     * 3-00-010 | DELAYREP ; ; Table D sequence definition
     * | 3-00-003 > | Table D descriptor to be defined
     * | 1-01-000 > | Delayed replication of 1 descriptor
     * | 0-31-001 > | Delayed descriptor replication factor
     * | 0-00-030 | Descriptor defining sequence
     *
     * 3-01-001 | WMOBLKST ; ;
     * | 0-01-001 > | WMO block number
     * | 0-01-002 | WMO station number
     *
     */
    private static void readNcepTableD(InputStream ios, TableD t) throws IOException {

        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));

        dataIS.readLine(); // throw first line away

        TableD.Descriptor currDesc = null;

        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            if (line.startsWith("#") || line.trim().isEmpty())
                continue;

            try {
                String[] flds = line.split("[\\|;]");
                if (flds[0].equals("END"))
                    break;

                String fxys = flds[0].trim();

                if (!fxys.isEmpty()) {
                    String[] xyflds = fxys.split("-");
                    short x = Short.parseShort(clean(xyflds[1]));
                    short y = Short.parseShort(clean(xyflds[2]));
                    String seqName = (flds.length > 3) ? flds[3].trim() : "";
                    currDesc = t.addDescriptor(x, y, seqName, new ArrayList<>());
                } else if (currDesc != null) {
                    fxys = StringUtil2.remove(flds[1], '>');
                    String[] xyflds = fxys.split("-");
                    short f = Short.parseShort(clean(xyflds[0]));
                    short x = Short.parseShort(clean(xyflds[1]));
                    short y = Short.parseShort(clean(xyflds[2]));
                    int fxy = (f << 14) + (x << 8) + y;
                    currDesc.addFeature((short) fxy);
                }

            } catch (Exception e) {
                log.error("Bad table " + t.getName() + " entry=<" + line + ">", e);
            }


        }
    }

    /*
     * 300002 2 000002
     * 000003
     * 300003 3 000010
     * 000011
     * 000012
     */
    private static void readEcmwfTableD(InputStream ios, TableD t) throws IOException {

        BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));

        TableD.Descriptor currDesc = null;

        int n = 0;
        while (true) {
            String line = dataIS.readLine();
            if (line == null)
                break;
            line = line.trim();
            if (line.startsWith("#") || line.isEmpty())
                continue;

            try {
                String fxys;
                // Need to do fixed-width parsing since, spaces can disappear (if
                // middle number is triple digits, it can end up right against the
                // first number
                if (line.length() > 6) {
                    String[] flds = {line.substring(0, 6), line.substring(6, 9), line.substring(9)};
                    fxys = flds[0].trim();
                    int fxy = Integer.parseInt(fxys);
                    int y = fxy % 1000;
                    fxy /= 1000;
                    int x = fxy % 100;
                    currDesc = t.addDescriptor((short) x, (short) y, "", new ArrayList<>());
                    n = Integer.parseInt(flds[1].trim());
                    fxys = flds[2].trim();
                } else {
                    fxys = line;
                }

                int fxy = Integer.parseInt(fxys);
                int y = fxy % 1000;
                fxy /= 1000;
                int x = fxy % 100;
                fxy /= 100;
                fxy = (fxy << 14) + (x << 8) + y;
                if (currDesc != null) {
                    currDesc.addFeature((short) fxy);
                }
                n--;

            } catch (Exception e) {
                log.error("Bad table " + t.getName() + " entry=<" + line + ">", e);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    static InputStream openStream(String location) throws IOException {
        InputStream ios;

        if (location.startsWith("resource:")) {
            location = location.substring(9);
            ios = BufrTables.class.getResourceAsStream(location);
            if (ios == null)
                throw new RuntimeException("resource not found=<" + location + ">");
            return ios;
        }

        if (location.startsWith("http:")) {
            URL url = new URL(location);
            ios = url.openStream();
        } else {
            ios = new FileInputStream(location);
        }
        return ios;
    }
}
