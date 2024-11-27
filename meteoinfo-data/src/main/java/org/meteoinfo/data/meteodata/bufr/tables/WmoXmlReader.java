/*
 * Copyright (c) 1998-2020 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr.tables;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import ucar.nc2.wmo.Util;
import ucar.unidata.util.StringUtil2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Read WMO BUFR XML formats
 *
 * @author John
 * @since 8/10/11
 */
public class WmoXmlReader {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WmoXmlReader.class);

    public enum Version {
        BUFR_14_1_0, BUFR_14_2_0, BUFR_15_1_1, BUFR_16_0_0, BUFR_WMO;

        String[] getElemNamesB() {
            if (this == BUFR_14_1_0) {
                return new String[]{"BC_TableB_BUFR14_1_0_CREX_6_1_0", "ElementName_E"};

            } else if (this == BUFR_14_2_0) {
                return new String[]{"Exporting_BCTableB_E", "ElementName"};

            } else if (this == BUFR_15_1_1) {
                return new String[]{"Exp_JointTableB_E", "ElementName_E"};

            } else if (this == BUFR_16_0_0) {
                return new String[]{"Exp_BUFRCREXTableB_E", "ElementName_E"};

            } else if (this == BUFR_WMO) { // from now on this is the element name
                return new String[]{null, "ElementName_en"};

            }
            return null;
        }

        String[] getElemNamesD() {
            if (this == BUFR_14_1_0) {
                return new String[]{"B_TableD_BUFR14_1_0_CREX_6_1_0", "ElementName1_E"};

            } else if (this == BUFR_14_2_0) {
                return new String[]{"Exporting_BUFRTableD_E", "ElementName1"};

            } else if (this == BUFR_15_1_1) {
                return new String[]{"Exp_BUFRTableD_E", "ElementName_E", "ExistingElementName_E"};

            } else if (this == BUFR_16_0_0) {
                return new String[]{"Exp_BUFRTableD_E", "ElementName_E", "ExistingElementName_E"};

            } else if (this == BUFR_WMO) {
                return new String[]{null, "ElementName_en"};

            }
            return null;
        }
    }

    /*
     * 14.1
     * <BC_TableB_BUFR14_1_0_CREX_6_1_0>
     * <SNo>1</SNo>
     * <Class>00</Class>
     * <FXY>000001</FXY>
     * <ElementName_E>Table A: entry</ElementName_E>
     * <ElementName_F>Table A : entr?e</ElementName_F>
     * <ElementName_R>??????? ?: ???????</ElementName_R>
     * <ElementName_S>Tabla A: elemento</ElementName_S>
     * <BUFR_Unit>CCITT IA5</BUFR_Unit>
     * <BUFR_Scale>0</BUFR_Scale>
     * <BUFR_ReferenceValue>0</BUFR_ReferenceValue>
     * <BUFR_DataWidth_Bits>24</BUFR_DataWidth_Bits>
     * <CREX_Unit>Character</CREX_Unit>
     * <CREX_Scale>0</CREX_Scale>
     * <CREX_DataWidth>3</CREX_DataWidth>
     * <Status>Operational</Status>
     * <NotesToTable_E>Notes: (see)#BUFR14_1_0_CREX6_1_0_Notes.doc#BC_Cl000</NotesToTable_E>
     * </BC_TableB_BUFR14_1_0_CREX_6_1_0>
     *
     * 14.2
     * <Exporting_BCTableB_E>
     * <No>2</No>
     * <ClassNo>00</ClassNo>
     * <ClassName>BUFR/CREX table entries</ClassName>
     * <FXY>000002</FXY>
     * <ElementName>Table A: data category description, line 1 </ElementName>
     * <BUFR_Unit>CCITT IA5 </BUFR_Unit>
     * <BUFR_Scale>0</BUFR_Scale>
     * <BUFR_ReferenceValue>0</BUFR_ReferenceValue>
     * <BUFR_DataWidth_Bits>256</BUFR_DataWidth_Bits>
     * <CREX_Unit>Character</CREX_Unit>
     * <CREX_Scale>0</CREX_Scale>
     * <CREX_DataWidth>32</CREX_DataWidth>
     * <Status>Operational</Status>
     * </Exporting_BCTableB_E>
     *
     * 15.1
     * <Exp_JointTableB_E>
     * <No>1</No>
     * <ClassNo>00</ClassNo>
     * <ClassName_E>BUFR/CREX table entries</ClassName_E>
     * <FXY>000001</FXY>
     * <ElementName_E>Table A: entry</ElementName_E>
     * <BUFR_Unit>CCITT IA5</BUFR_Unit>
     * <BUFR_Scale>0</BUFR_Scale>
     * <BUFR_ReferenceValue>0</BUFR_ReferenceValue>
     * <BUFR_DataWidth_Bits>24</BUFR_DataWidth_Bits>
     * <CREX_Unit>Character</CREX_Unit>
     * <CREX_Scale>0</CREX_Scale>
     * <CREX_DataWidth_Char>3</CREX_DataWidth_Char>
     * <Status>Operational</Status>
     * </Exp_JointTableB_E>
     *
     * 16.0
     * <Exp_BUFRCREXTableB_E>
     * <No>681</No>
     * <ClassNo>13</ClassNo>
     * <ClassName_E>Hydrographic and hydrological elements</ClassName_E>
     * <FXY>013060</FXY>
     * <ElementName_E>Total accumulated precipitation</ElementName_E>
     * <BUFR_Unit>kg m-2</BUFR_Unit>
     * <BUFR_Scale>1</BUFR_Scale>
     * <BUFR_ReferenceValue>-1</BUFR_ReferenceValue>
     * <BUFR_DataWidth_Bits>17</BUFR_DataWidth_Bits>
     * <CREX_Unit>kg m-2</CREX_Unit>
     * <CREX_Scale>1</CREX_Scale>
     * <CREX_DataWidth_Char>5</CREX_DataWidth_Char>
     * <Status>Operational</Status>
     * </Exp_BUFRCREXTableB_E>
     *
     * <BUFRCREX_17_0_0_TableB_en>
     * <No>8</No>
     * <ClassNo>00</ClassNo>
     * <ClassName_en>BUFR/CREX table entries</ClassName_en>
     * <FXY>000008</FXY>
     * <ElementName_en>BUFR Local table version number</ElementName_en>
     * <Note_en>(see Note 4)</Note_en>
     * <BUFR_Unit>CCITT IA5</BUFR_Unit>
     * <BUFR_Scale>0</BUFR_Scale>
     * <BUFR_ReferenceValue>0</BUFR_ReferenceValue>
     * <BUFR_DataWidth_Bits>16</BUFR_DataWidth_Bits>
     * <CREX_Unit>Character</CREX_Unit>
     * <CREX_Scale>0</CREX_Scale>
     * <CREX_DataWidth_Char>2</CREX_DataWidth_Char>
     * <Status>Operational</Status>
     * </BUFRCREX_17_0_0_TableB_en>
     *
     * <BUFRCREX_22_0_1_TableB_en>
     * <No>1018</No>
     * <ClassNo>21</ClassNo>
     * <ClassName_en>BUFR/CREX Radar data</ClassName_en>
     * <FXY>021073</FXY>
     * <ElementName_en>Satellite altimeter instrument mode</ElementName_en>
     * <BUFR_Unit>Flag table</BUFR_Unit>
     * <BUFR_Scale>0</BUFR_Scale>
     * <BUFR_ReferenceValue>0</BUFR_ReferenceValue>
     * <BUFR_DataWidth_Bits>9</BUFR_DataWidth_Bits>
     * <CREX_Unit>Flag table</CREX_Unit>
     * <CREX_Scale>0</CREX_Scale>
     * <CREX_DataWidth_Char>3</CREX_DataWidth_Char>
     * <Status>Operational</Status>
     * </BUFRCREX_22_0_1_TableB_en>
     */

    static void readWmoXmlTableB(InputStream ios, TableB b) throws IOException {
        org.jdom2.Document doc;
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setExpandEntities(false);
            doc = builder.build(ios);
        } catch (JDOMException e) {
            throw new IOException(e.getMessage());
        }

        Element root = doc.getRootElement();

        // what elements do we need to parse tableB?
        String[] elems = elementsUsedFromTableB(root);

        List<Element> unrecognizedSequenceTermElements = new ArrayList<>();
        List<Element> featList = root.getChildren();
        for (Element elem : featList) {
            Element ce = null;
            for (int nameTest = 1; nameTest < elems.length; nameTest++) {
                ce = elem.getChild(elems[nameTest]);
                if (ce != null) {
                    break;
                }
            }
            if (ce == null) {
                unrecognizedSequenceTermElements.add(elem);
                continue;
            }

            String name = Util.cleanName(ce.getTextNormalize());
            String units = cleanUnit(elem.getChildTextNormalize("BUFR_Unit"));
            int x = 0, y = 0, scale = 0, reference = 0, width = 0;

            String fxy = null;
            String s = null;
            try {
                fxy = elem.getChildTextNormalize("FXY");
                int xy = Integer.parseInt(cleanNumber(fxy));
                x = xy / 1000;
                y = xy % 1000;

            } catch (NumberFormatException e) {
                log.warn(" key {} name '{}' fails parsing", fxy, name);
            }

            try {
                s = elem.getChildTextNormalize("BUFR_Scale");
                scale = Integer.parseInt(cleanNumber(s));
            } catch (NumberFormatException e) {
                log.warn(" key {} name '{}' has bad scale='{}'", fxy, name, s);
            }

            try {
                s = elem.getChildTextNormalize("BUFR_ReferenceValue");
                reference = Integer.parseInt(cleanNumber(s));
            } catch (NumberFormatException e) {
                log.warn(" key {} name '{}' has bad reference='{}'", fxy, name, s);
            }

            try {
                s = elem.getChildTextNormalize("BUFR_DataWidth_Bits");
                width = Integer.parseInt(cleanNumber(s));
            } catch (NumberFormatException e) {
                log.warn(" key {} name '{}' has bad width='{}'", fxy, name, s);
            }

            b.addDescriptor((short) x, (short) y, scale, reference, width, name, units, null);
        }

        if (log.isDebugEnabled()) {
            logUnrecognizedElements(unrecognizedSequenceTermElements, "B", b.getLocation());
        }

        ios.close();
    }

    static String cleanNumber(String s) {
        return StringUtil2.remove(s, ' ');
    }

    public static String cleanUnit(String unit) {
        String result = StringUtil2.remove(unit, 176);
        return StringUtil2.replace(result, (char) 65533, "2"); // seems to be a superscript 2 in some language
    }

    static String[] elementsUsedFromTableD(Element root) {
        return elementsUsedFromTable(root, "D");
    }

    static String[] elementsUsedFromTableB(Element root) {
        return elementsUsedFromTable(root, "B");
    }

    static String[] elementsUsedFromTable(Element root, String tableType) {
        String[] elems = null;
        // does the table have its own enum value? If so, use it.
        for (Version v : Version.values()) {
            boolean match = root.getAttributes().stream().anyMatch(attr -> attr.getValue().contains(v.toString()));
            if (match) {
                elems = tableType.equals("B") ? v.getElemNamesB() : v.getElemNamesD();
                break;
            }
        }

        // exact table match not found. Try seeing if the table uses
        // the sequence element from a version defined in the Version enum.
        // Note: will stop on the first version that works, as defined by
        // the order of the Version enum. might not be correct.
        if (elems == null) {
            for (Version v : Version.values()) {
                elems = tableType.equals("B") ? v.getElemNamesB() : v.getElemNamesD();
                List<Element> featList = null;
                if ((elems != null) && (elems.length > 0)) {
                    featList = root.getChildren(elems[0]);
                }
                if (featList != null && !featList.isEmpty()) {
                    break;
                }
            }
        }

        return elems;
    }

    static void logUnrecognizedElements(List<Element> unrecognizedSequenceTermElements, String tableType,
                                        String location) {
        // not every sequence entry in the WMO xml table D files is processed. This has caused trouble before.
        // this is a pretty specific, low level debug message to hopefully give a clue to us in the future
        // that if we are having trouble decoding BUFR messages, maybe we're not fully parsing the WMO xml TableD
        // entries, and so the sequence being used might not be the full sequence necessary to decode.
        if (log.isDebugEnabled()) {
            if (unrecognizedSequenceTermElements.size() > 0) {
                StringBuilder msgBuilder = new StringBuilder();
                msgBuilder.append(String.format("%d Unprocessed sequences in WMO table %s %s",
                        unrecognizedSequenceTermElements.size(), tableType, location));
                if (tableType.equals("D")) {
                    String tableDChecker = "bufr/src/test/java/ucar/nc2/iosp/bufr/tables/WmoTableDVariations.java";
                    msgBuilder
                            .append(String.format("This might be ok, but to know for sure, consider running %s", tableDChecker));
                }
                log.debug(msgBuilder.toString());
            }
        }
    }

    /*
     * <B_TableD_BUFR14_1_0_CREX_6_1_0>
     * <SNo>2647</SNo>
     * <Category>10</Category>
     * <FXY1>310013</FXY1>
     * <ElementName1_E>(AVHRR (GAC) report)</ElementName1_E>
     * <FXY2>004005</FXY2>
     * <ElementName2_E>Minute</ElementName2_E>
     * <Remarks_E>Minute</Remarks_E>
     * <Status>Operational</Status>
     * </B_TableD_BUFR14_1_0_CREX_6_1_0>
     *
     * 14.2.0
     * <Exporting_BUFRTableD_E>
     * <No>2901</No>
     * <Category>10</Category>
     * <CategoryOfSequences>Vertical sounding sequences (satellite data)</CategoryOfSequences>
     * <FXY1>310025</FXY1>
     * <ElementName1>(SSMIS Temperature data record)</ElementName1>
     * <FXY2>004006</FXY2>
     * <Status>Operational</Status>
     * </Exporting_BUFRTableD_E>
     *
     * 15.1.1
     * <Exp_BUFRTableD_E>
     * <No>102</No>
     * <Category>01</Category>
     * <CategoryOfSequences_E>Location and identification sequences</CategoryOfSequences_E>
     * <FXY1>301034</FXY1>
     * <Title_E>(Buoy/platform - fixed)</Title_E>
     * <FXY2>001005</FXY2>
     * <ElementName_E>Buoy/platform identifier</ElementName_E>
     * <ExistingElementName_E>Buoy/platform identifier</ExistingElementName_E>
     * <Status>Operational</Status>
     * </Exp_BUFRTableD_E>
     *
     * 16.0.0
     * <Exp_BUFRTableD_E>
     * <No>402</No>
     * <Category>02</Category>
     * <CategoryOfSequences_E>Meteorological sequences common to surface data</CategoryOfSequences_E>
     * <FXY1>302001</FXY1>
     * <FXY2>010051</FXY2>
     * <ElementName_E>Pressure reduced to mean sea level</ElementName_E>
     * <ExistingElementName_E>Pressure reduced to mean sea level</ExistingElementName_E>
     * <Status>Operational</Status>
     * </Exp_BUFRTableD_E>
     *
     * <BUFR_19_1_1_TableD_en>
     * <No>4</No>
     * <Category>00</Category>
     * <CategoryOfSequences_en>BUFR table entries sequences</CategoryOfSequences_en>
     * <FXY1>300003</FXY1>
     * <Title_en>(F, X, Y of descriptor to be added or defined)</Title_en>
     * <FXY2>000011</FXY2>
     * <ElementName_en>X descriptor to be added or defined</ElementName_en>
     * <Status>Operational</Status>
     * </BUFR_19_1_1_TableD_en>
     *
     * <BUFR_22_0_1_TableD_en>
     * <No>5874</No>
     * <Category>15</Category>
     * <CategoryOfSequences_en>Oceanographic report sequences</CategoryOfSequences_en>
     * <FXY1>315004</FXY1>
     * <Title_en>(XBT temperature profile data sequence)</Title_en>
     * <FXY2>025061</FXY2>
     * <ElementName_en>Software identification and version number</ElementName_en>
     * <Status>Operational</Status>
     * </BUFR_22_0_1_TableD_en>
     *
     */
    static void readWmoXmlTableD(InputStream ios, TableD tableD) throws IOException {
        org.jdom2.Document doc;
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setExpandEntities(false);
            doc = builder.build(ios);
        } catch (JDOMException e) {
            throw new IOException(e.getMessage());
        }

        int currSeqno = -1;
        TableD.Descriptor currDesc = null;

        Element root = doc.getRootElement();

        // what elements do we need to parse tableD?
        String[] elems = elementsUsedFromTableD(root);
        List<Element> unrecognizedSequenceTermElements = new ArrayList<>();
        List<Element> featList = root.getChildren();
        for (Element elem : featList) {
            // see if element in table is recognized
            Element ce = null;
            for (int nameTest = 1; nameTest < elems.length; nameTest++) {
                ce = elem.getChild(elems[nameTest]);
                if (ce != null) {
                    break;
                }
            }
            if (ce == null) {
                unrecognizedSequenceTermElements.add(elem);
                continue;
            }

            String seqs = elem.getChildTextNormalize("FXY1");
            int seq = Integer.parseInt(seqs);

            if (currSeqno != seq) {
                int y = seq % 1000;
                int w = seq / 1000;
                int x = w % 100;
                String seqName = Util.cleanName(ce.getTextNormalize());
                currDesc = tableD.addDescriptor((short) x, (short) y, seqName, new ArrayList<>());
                currSeqno = seq;
            }

            String fnos = elem.getChildTextNormalize("FXY2");
            int fno = Integer.parseInt(fnos);
            int y = fno % 1000;
            int w = fno / 1000;
            int x = w % 100;
            int f = w / 100;
            int fxy = (f << 14) + (x << 8) + y;
            currDesc.addFeature((short) fxy);
        }

        if (log.isDebugEnabled()) {
            logUnrecognizedElements(unrecognizedSequenceTermElements, "D", tableD.getLocation());
        }

        ios.close();
    }

}

