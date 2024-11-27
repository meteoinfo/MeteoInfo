/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr.tables;

/*
 * BufrRead mnemonic.java 1.0 05/09/2008
 *
 * @author Robb Kambic
 *
 * @version 1.0
 */

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import org.meteoinfo.data.meteodata.bufr.Descriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A class that reads a mnemonic table. It doesn't include X < 48 and Y < 192 type of
 * descriptors because they are already stored in the latest WMO tables.
 */

public class NcepMnemonic {

    // | HEADR | 362001 | TABLE D ENTRY - PROFILE COORDINATES | |
    private static final Pattern fields3 = Pattern.compile("^\\|\\s+(.*)\\s+\\|\\s+(.*)\\s+\\|\\s+(.*)\\s*\\|");
    private static final Pattern fields2 = Pattern.compile("^\\|\\s+(.*)\\s+\\|\\s+(.*)\\s+\\|");
    private static final Pattern fields5 =
            Pattern.compile("^\\|\\s+(.*)\\s+\\|\\s+(.*)\\s+\\|\\s+(.*)\\s+\\|\\s+(.*)\\s+\\|\\s+(.*)\\s+\\|");

    /**
     * Pattern to get 3 integers from beginning of line.
     */
    private static final Pattern ints6 = Pattern.compile("^\\d{6}");

    private static final int XlocalCutoff = 48;
    private static final int YlocalCutoff = 192;

    private static final boolean debugTable = false;

    /**
     * Read NCEP mnemonic BUFR tables.
     *
     * @return true on success.
     */
    public static boolean read(InputStream ios, BufrTables.Tables tables) throws IOException {
        if (ios == null)
            return false;

        if (tables.b == null)
            tables.b = new TableB("fake", "fake");
        if (tables.d == null)
            tables.d = new TableD("fake", "fake");

        HashMap<String, String> number = new HashMap<>(); // key = mnemonic value = fxy
        HashMap<String, String> desc = new HashMap<>(); // key = mnemonic value = description
        HashMap<String, String> mnseq = new HashMap<>();

        try {
            BufferedReader dataIS = new BufferedReader(new InputStreamReader(ios, StandardCharsets.UTF_8));

            // read mnemonic table
            Matcher m;
            // read header info and disregard
            while (true) {
                String line = dataIS.readLine();
                if (line == null)
                    throw new RuntimeException("Bad NCEP mnemonic BUFR table ");
                if (line.contains("MNEMONIC"))
                    break;
            }
            // read mnemonic, number, and description
            // | HEADR | 362001 | TABLE D ENTRY - PROFILE COORDINATES |
            while (true) {
                String line = dataIS.readLine();
                if (line == null)
                    break;
                if (line.contains("MNEMONIC"))
                    break;
                if (line.contains("----"))
                    continue;
                if (line.startsWith("*"))
                    continue;
                if (line.startsWith("|       "))
                    continue;
                m = fields3.matcher(line);
                if (m.find()) {
                    String mnu = m.group(1).trim();
                    String fxy = m.group(2).trim();
                    if (fxy.startsWith("3")) {
                        number.put(mnu, fxy);
                        desc.put(mnu, m.group(3).replace("TABLE D ENTRY - ", "").trim());
                    } else if (fxy.startsWith("0")) {
                        number.put(mnu, fxy);
                        desc.put(mnu, m.group(3).replace("TABLE B ENTRY - ", "").trim());
                    } else if (fxy.startsWith("A")) {
                        number.put(mnu, fxy);
                        desc.put(mnu, m.group(3).replace("TABLE A ENTRY - ", "").trim());
                    }
                } else if (debugTable) {
                    System.out.println("bad mnemonic, number, and description: " + line);
                }
            }
            // read in sequences using mnemonics
            // | ETACLS1 | HEADR {PROFILE} SURF FLUX HYDR D10M {SLYR} XTRA |
            while (true) {
                String line = dataIS.readLine();
                if (line == null)
                    break;
                if (line.contains("MNEMONIC"))
                    break;
                if (line.contains("----"))
                    continue;
                if (line.startsWith("|       "))
                    continue;
                if (line.startsWith("*"))
                    continue;
                m = fields2.matcher(line);
                if (m.find()) {
                    String mnu = m.group(1).trim();
                    if (mnseq.containsKey(mnu)) { // concat lines with same mnu
                        String value = mnseq.get(mnu);
                        value = value + " " + m.group(2);
                        mnseq.put(mnu, value);
                    } else {
                        mnseq.put(mnu, m.group(2));
                    }
                } else if (debugTable) {
                    System.out.println("bad sequence mnemonic: " + line);
                }
            }

            // create sequences, replacing mnemonics with numbers
            for (Map.Entry<String, String> ent : mnseq.entrySet()) {
                String seq = ent.getValue();
                seq = seq.replaceAll("\\<", "1-1-0 0-31-0 ");
                seq = seq.replaceAll("\\>", "");
                seq = seq.replaceAll("\\{", "1-1-0 0-31-1 ");
                seq = seq.replaceAll("\\}", "");
                seq = seq.replaceAll("\\(", "1-1-0 0-31-2 ");
                seq = seq.replaceAll("\\)", "");

                StringTokenizer stoke = new StringTokenizer(seq, " ");
                List<Short> list = new ArrayList<>();
                while (stoke.hasMoreTokens()) {
                    String mn = stoke.nextToken();
                    if (mn.charAt(1) == '-') {
                        list.add(Descriptor.getFxy(mn));
                        continue;
                    }
                    // element descriptor needs hyphens
                    m = ints6.matcher(mn);
                    if (m.find()) {
                        String F = mn.substring(0, 1);
                        String X = removeLeading0(mn.substring(1, 3));
                        String Y = removeLeading0(mn.substring(3));
                        list.add(Descriptor.getFxy(F + "-" + X + "-" + Y));
                        continue;
                    }
                    if (mn.startsWith("\"")) {
                        int idx = mn.lastIndexOf('"');
                        String count = mn.substring(idx + 1);
                        list.add(Descriptor.getFxy("1-1-" + count));
                        mn = mn.substring(1, idx);

                    }
                    if (mn.startsWith(".")) {
                        String des = mn.substring(mn.length() - 4);
                        mn = mn.replace(des, "....");

                    }
                    String fxy = number.get(mn);
                    String F = fxy.substring(0, 1);
                    String X = removeLeading0(fxy.substring(1, 3));
                    String Y = removeLeading0(fxy.substring(3));
                    list.add(Descriptor.getFxy(F + "-" + X + "-" + Y));
                }

                String fxy = number.get(ent.getKey());
                String X = removeLeading0(fxy.substring(1, 3));
                String Y = removeLeading0(fxy.substring(3));
                // these are in latest tables
                if (XlocalCutoff > Integer.parseInt(X) && YlocalCutoff > Integer.parseInt(Y))
                    continue;
                // key = F + "-" + X + "-" + Y;

                short seqX = Short.parseShort(X.trim());
                short seqY = Short.parseShort(Y.trim());

                tables.d.addDescriptor(seqX, seqY, ent.getKey(), list);
                // short id = Descriptor.getFxy(key);
                // sequences.put(Short.valueOf(id), tableD);
            }

            // add some static repetition sequences
            // LOOK why?
            List<Short> list = new ArrayList<>();
            // 16 bit delayed repetition
            list.add(Descriptor.getFxy("1-1-0"));
            list.add(Descriptor.getFxy("0-31-2"));
            tables.d.addDescriptor((short) 60, (short) 1, "", list);
            // tableD = new DescriptorTableD("", "3-60-1", list, false);
            // tableD.put( "3-60-1", d);
            // short id = Descriptor.getFxy("3-60-1");
            // sequences.put(Short.valueOf(id), tableD);

            list = new ArrayList<>();
            // 8 bit delayed repetition
            list.add(Descriptor.getFxy("1-1-0"));
            list.add(Descriptor.getFxy("0-31-1"));
            tables.d.addDescriptor((short) 60, (short) 2, "", list);
            // tableD = new DescriptorTableD("", "3-60-2", list, false);
            // tableD.put( "3-60-2", d);
            // id = Descriptor.getFxy("3-60-2");
            // sequences.put(Short.valueOf(id), tableD);

            list = new ArrayList<>();
            // 8 bit delayed repetition
            list.add(Descriptor.getFxy("1-1-0"));
            list.add(Descriptor.getFxy("0-31-1"));
            tables.d.addDescriptor((short) 60, (short) 3, "", list);
            // tableD = new DescriptorTableD("", "3-60-3", list, false);
            // tableD.put( "3-60-3", d);
            // id = Descriptor.getFxy("3-60-3");
            // sequences.put(Short.valueOf(id), tableD);

            list = new ArrayList<>();
            // 1 bit delayed repetition
            list.add(Descriptor.getFxy("1-1-0"));
            list.add(Descriptor.getFxy("0-31-0"));
            tables.d.addDescriptor((short) 60, (short) 4, "", list);
            // tableD = new DescriptorTableD("", "3-60-4", list, false);
            // tableD.put( "3-60-4", d);
            // id = Descriptor.getFxy("3-60-4");
            // sequences.put(Short.valueOf(id), tableD);

            // add in element descriptors
            // MNEMONIC | SCAL | REFERENCE | BIT | UNITS
            // | FTIM | 0 | 0 | 24 | SECONDS |-------------|

            // tableB = new TableB(tablename, tablename);

            while (true) {
                String line = dataIS.readLine();
                if (line == null)
                    break;
                if (line.contains("MNEMONIC"))
                    break;
                if (line.startsWith("|       "))
                    continue;
                if (line.startsWith("*"))
                    continue;
                m = fields5.matcher(line);
                if (m.find()) {
                    if (m.group(1).equals("")) {
                        // do nothing

                    } else if (number.containsKey(m.group(1).trim())) { // add descriptor to tableB
                        String fxy = number.get(m.group(1).trim());
                        String X = fxy.substring(1, 3);
                        String Y = fxy.substring(3);
                        String mnu = m.group(1).trim();
                        String descr = desc.get(mnu);

                        short x = Short.parseShort(X.trim());
                        short y = Short.parseShort(Y.trim());

                        // these are in latest tables so skip LOOK WHY
                        if (XlocalCutoff > x && YlocalCutoff > y)
                            continue;

                        int scale = Integer.parseInt(m.group(2).trim());
                        int refVal = Integer.parseInt(m.group(3).trim());
                        int width = Integer.parseInt(m.group(4).trim());
                        String units = m.group(5).trim();

                        tables.b.addDescriptor(x, y, scale, refVal, width, mnu, units, descr);

                    } else if (debugTable) {
                        System.out.println("bad element descriptors: " + line);
                    }
                }
            }

        } finally {
            ios.close();
        }

        // LOOK why ?
        // default for NCEP
        // 0; 63; 0; 0; 0; 16; Numeric; Byte count
        tables.b.addDescriptor((short) 63, (short) 0, 0, 0, 16, "Byte count", "Numeric", null);

        return true;
    }

    private static String removeLeading0(String number) {
        if (number.length() == 2 && number.startsWith("0")) {
            number = number.substring(1);
        } else if (number.length() == 3 && number.startsWith("00")) {
            number = number.substring(2);
        } else if (number.length() == 3 && number.startsWith("0")) {
            number = number.substring(1);
        }
        return number;
    }
}
