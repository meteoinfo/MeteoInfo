/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr.tables;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.InputStream;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read standard WMO Table A (data categories).
 */
public class TableA {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TableA.class);
    private static final String TABLEA_FILENAME = "wmo/BUFR_37_0_0_TableA_en.xml";
    private static Map<Integer, Descriptor> tableA;
    private final String name;
    private final String location;

    /*
     * <BUFR_19_1_1_TableA_en>
     * <No>27</No>
     * <CodeFigure>28</CodeFigure>
     * <Meaning_en>Precision orbit (satellite)</Meaning_en>
     * <Status>Operational</Status>
     * </BUFR_19_1_1_TableA_en>
     *
     * <Exp_BUFRTableA_E>
     * <No>4</No>
     * <CodeFigure>3</CodeFigure>
     * <Meaning_E>Vertical soundings (satellite)</Meaning_E>
     * <Status>Operational</Status>
     * </Exp_BUFRTableA_E>
     */
    private static void init() {
        String filename = BufrTables.RESOURCE_PATH + TABLEA_FILENAME;
        try (InputStream is = CodeFlagTables.class.getResourceAsStream(filename)) {

            HashMap<Integer, Descriptor> map = new HashMap<>(100);
            SAXBuilder builder = new SAXBuilder();
            builder.setExpandEntities(false);
            org.jdom2.Document tdoc = builder.build(is);
            Element root = tdoc.getRootElement();

            List<Element> elems = root.getChildren();
            for (Element elem : elems) {
                String line = elem.getChildText("No");
                String codeS = elem.getChildText("CodeFigure");
                String desc = elem.getChildText("Meaning_en");

                try {
                    int code = Integer.parseInt(codeS);
                    Descriptor descriptor = new Descriptor(code, desc);
                    map.put(code, descriptor);
                } catch (NumberFormatException e) {
                    log.debug("NumberFormatException on line " + line + " in " + codeS);
                }

            }
            tableA = map;

        } catch (Exception e) {
            log.error("Can't read BUFR code table " + filename, e);
        }
    }

    public TableA(String name, String location) {
        this.name = name;
        this.location = location;
        tableA = new HashMap<>();
    }

    public Descriptor getDescriptor(int code) {
        if (tableA == null)
            init();
        return tableA.get(code);
    }

    /**
     * data category description, from table A
     *
     * @param cat data category
     * @return category description, or null if not found
     */
    public static String getDataCategory(int cat) {
        if (tableA == null)
            init();
        Descriptor descriptor = tableA.get(cat);
        return descriptor != null ? descriptor.getDescription() : "Unknown category=" + cat;
    }

    /**
     * data category name, from table A
     *
     * @param cat data category
     * @return category name, or null if not found
     */
    public static String getDataCategoryName(int cat) {
        if (tableA == null)
            init();
        Descriptor descriptor = tableA.get(cat);
        return descriptor != null ? descriptor.getName() : "obs_" + cat;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public TableA.Descriptor addDescriptor(int code, String description) {
        TableA.Descriptor d = new TableA.Descriptor(code, description);
        tableA.put(code, d);
        return d;
    }

    public static class Descriptor implements Comparable<Descriptor> {
        private int code;
        private String name;
        private String description;
        private boolean localOverride;

        Descriptor(int code, String description) {
            this.code = code;
            this.description = description;
            this.name = "obs_" + String.valueOf(code);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return this.description;
        }

        /**
         * Get code
         *
         * @return Code
         */
        public int getCode() {
            return this.code;
        }

        public String toString() {
            return String.valueOf(code) + " " + getName() + " " +
                    this.description;
        }

        @Override
        public int compareTo(Descriptor o) {
            return code - o.getCode();
        }

        public boolean isLocal() {
            return ((code >= 102) && (code <= 239));
        }

        public void setLocalOverride(boolean isOverride) {
            this.localOverride = isOverride;
        }

        public boolean getLocalOverride() {
            return localOverride;
        }

    }

}
