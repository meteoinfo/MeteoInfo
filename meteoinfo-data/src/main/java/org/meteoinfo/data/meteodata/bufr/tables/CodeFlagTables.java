/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr.tables;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.meteoinfo.data.meteodata.bufr.Descriptor;
import ucar.nc2.wmo.CommonCodeTable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Read BUFR Code / Flag tables. */
public class CodeFlagTables {
  private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodeFlagTables.class);
  private static final String CodeFlagFilename = "wmo/BUFRCREX_37_0_0_CodeFlag_en.xml";
  static Map<Short, CodeFlagTables> tableMap;

  public static CodeFlagTables getTable(short id) {
    if (tableMap == null)
      init();

    if (id == 263)
      return useCC(id, 5); // 0-1-7
    if (id == 526)
      return useCC(id, 7); // 0-2-14
    if (id == 531)
      return useCC(id, 8); // 0-2-19
    if (id == 5699)
      return useCC(id, 3); // 0-22-67
    if (id == 5700)
      return useCC(id, 4); // 0-22-68

    return tableMap.get(id);
  }

  private static CodeFlagTables useCC(short fxy, int cc) {
    CodeFlagTables cft = tableMap.get(fxy);
    if (cft == null) {
      CommonCodeTable cct = CommonCodeTable.getTable(cc);
      cft = new CodeFlagTables(fxy, cct.getTableName(), cct.getMap());
      tableMap.put(fxy, cft);
    }
    return cft;
  }

  public static boolean hasTable(short id) {
    if (tableMap == null)
      init();
    CodeFlagTables result = tableMap.get(id);
    return result != null;
  }

  private static void init() {
    tableMap = new HashMap<>(300);
    init(tableMap);
  }

  public static Map<Short, CodeFlagTables> getTables() {
    if (tableMap == null)
      init();
    return tableMap;
  }

  /*
   * <Exp_CodeFlagTables_E>
   * <No>837</No>
   * <FXY>002119</FXY>
   * <ElementName_E>Instrument operations</ElementName_E>
   * <CodeFigure>0</CodeFigure>
   * <EntryName_E>Intermediate frequency calibration mode (IF CAL)</EntryName_E>
   * <Status>Operational</Status>
   * </Exp_CodeFlagTables_E>
   * 
   * <BUFRCREX_19_1_1_CodeFlag_en>
   * <No>2905</No>
   * <FXY>020042</FXY>
   * <ElementName_en>Airframe icing present</ElementName_en>
   * <CodeFigure>2</CodeFigure>
   * <EntryName_en>Reserved</EntryName_en>
   * <Status>Operational</Status>
   * </BUFRCREX_19_1_1_CodeFlag_en>
   * 
   * <BUFRCREX_22_0_1_CodeFlag_en>
   * <No>3183</No>
   * <FXY>020063</FXY>
   * <ElementName_en>Special phenomena</ElementName_en>
   * <CodeFigure>31</CodeFigure>
   * <EntryName_en>Slight coloration of clouds at sunrise associated with a tropical disturbance</EntryName_en>
   * <Status>Operational</Status>
   * </BUFRCREX_22_0_1_CodeFlag_en>
   * 
   */
  static void init(Map<Short, CodeFlagTables> table) {
    String filename = BufrTables.RESOURCE_PATH + CodeFlagFilename;
    try (InputStream is = CodeFlagTables.class.getResourceAsStream(filename)) {
      SAXBuilder builder = new SAXBuilder();
      builder.setExpandEntities(false);
      org.jdom2.Document tdoc = builder.build(is);
      Element root = tdoc.getRootElement();

      List<Element> elems = root.getChildren();
      for (Element elem : elems) {
        String fxyS = elem.getChildText("FXY");
        String desc = elem.getChildText("ElementName_en");

        short fxy = Descriptor.getFxy2(fxyS);
        CodeFlagTables ct = table.get(fxy);
        if (ct == null) {
          ct = new CodeFlagTables(fxy, desc);
          table.put(fxy, ct);
        }

        String line = elem.getChildText("No");
        String codeS = elem.getChildText("CodeFigure");
        String value = elem.getChildText("EntryName_en");

        if ((codeS == null) || (value == null))
          continue;
        if (value.toLowerCase().startsWith("reserved"))
          continue;
        if (value.toLowerCase().startsWith("not used"))
          continue;

        int code;
        if (codeS.toLowerCase().contains("all")) {
          code = -1;
        } else
          try {
            code = Integer.parseInt(codeS);
          } catch (NumberFormatException e) {
            log.debug("NumberFormatException on line " + line + " in " + codeS);
            continue;
          }
        ct.addValue((short) code, value);
      }

    } catch (IOException | JDOMException e) {
      log.error("Can't read BUFR code table " + filename, e);
    }
  }

  ////////////////////////////////////////////////
  // TODO Make Immutable
  private short fxy;
  private String name;
  private Map<Integer, String> map; // needs to be integer for EnumTypedef

  CodeFlagTables(short fxy, String name) {
    this.fxy = fxy;
    this.name = (name == null) ? fxy() : name; // StringUtil2.replace(name, ' ', "_") + "("+fxy()+")";
    map = new HashMap<>(20);
  }

  private CodeFlagTables(short fxy, String name, Map<Integer, String> map) {
    this.fxy = fxy;
    this.name = (name == null) ? fxy() : name;
    this.map = map;
  }

  public String getName() {
    return name;
  }

  public Map<Integer, String> getMap() {
    return map;
  }

  void addValue(int value, String text) {
    map.put(value, text);
  }

  public short getId() {
    return fxy;
  }

  public String fxy() {
    int f = fxy >> 14;
    int x = (fxy & 0xff00) >> 8;
    int y = (fxy & 0xff);

    return f + "-" + x + "-" + y;
  }

  public String toString() {
    return name;
  }

}
