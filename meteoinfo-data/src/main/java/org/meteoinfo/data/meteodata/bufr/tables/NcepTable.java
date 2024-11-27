/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr.tables;

import ucar.unidata.util.StringUtil2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Ncep local table overrides
 *
 * @author caron
 * @since 8/22/13
 */
public class NcepTable {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NcepTable.class);

    private static void readNcepTable(String location) throws IOException {
        try (InputStream ios = BufrTables.openStream(location)) {
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
                if (flds.length < 3) {
                    log.warn("{} BAD split == {}", count, line);
                    continue;
                }

                int fldidx = 0;
                try {
                    int cat = Integer.parseInt(flds[fldidx++].trim());
                    int subcat = Integer.parseInt(flds[fldidx++].trim());
                    String desc = StringUtil2.remove(flds[fldidx++], '"');
                    entries.add(new TableEntry(cat, subcat, desc));
                } catch (Exception e) {
                    log.warn("{} {} BAD line == {}", count, fldidx, line);
                }
            }
        }
    }

    private static List<TableEntry> entries;

    private static class TableEntry {
        public int cat, subcat;
        public String value;

        public TableEntry(int cat, int subcat, String value) {
            this.cat = cat;
            this.subcat = subcat;
            this.value = value.trim();
        }
    }

    private static void init() {
        entries = new ArrayList<>(100);
        String location = "resource:/resources/bufrTables/local/ncep/DataSubCategories.csv";
        try {
            readNcepTable(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getDataSubcategory(int cat, int subcat) {
        if (entries == null)
            init();

        for (TableEntry p : entries) {
            if ((p.cat == cat) && (p.subcat == subcat))
                return p.value;
        }
        return null;
    }


}
