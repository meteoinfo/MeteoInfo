/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr;

import org.meteoinfo.data.meteodata.bufr.tables.TableA;
import ucar.ma2.*;
import ucar.nc2.*;
import org.meteoinfo.data.meteodata.bufr.tables.TableB;
import org.meteoinfo.data.meteodata.bufr.tables.TableD;
import org.meteoinfo.data.meteodata.bufr.tables.WmoXmlReader;
import ucar.nc2.wmo.Util;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BUFR allows you to encode a BUFR table in BUFR.
 * if table is embedded, all entries must be from it
 * LOOK: may be NCEP specific ?
 *
 * @author John
 * @since 8/11/11
 */
public class EmbeddedTable {
    private static final boolean showB = false;
    private static final boolean showD = false;

    private final RandomAccessFile raf;
    private final BufrIdentificationSection ids;

    private List<Message> messages = new ArrayList<>();
    private boolean tableRead;
    private TableA a;
    private TableB b;
    private TableD d;
    private Structure seq1, seq2, seq3, seq4;
    private TableLookup tlookup;

    EmbeddedTable(Message m, RandomAccessFile raf) {
        this.raf = raf;
        this.ids = m.ids;
        a = new TableA("embed", raf.getLocation());
        b = new TableB("embed", raf.getLocation());
        d = new TableD("embed", raf.getLocation());
    }

    public void addTable(Message m) {
        messages.add(m);
    }

    private void read2() throws IOException {
        Message proto = messages.get(0);

        // make root sub key data descriptors name as null, so the following construct
        // will have seq2 and seq3 variables
        DataDescriptor root = proto.getRootDataDescriptor();
        for (DataDescriptor ds : root.subKeys) {
            ds.name = null;
        }

        BufrConfig config = BufrConfig.openFromMessage(raf, proto, null);
        Construct2 construct = new Construct2(proto, config, new NetcdfFileSubclass());

        Sequence obs = construct.getObsStructure();
        seq1 = (Structure) obs.findVariable("seq1");
        seq2 = (Structure) obs.findVariable("seq2");
        seq3 = (Structure) obs.findVariable("seq3");
        seq4 = (Structure) seq3.findVariable("seq4");

        // read all the messages
        ArrayStructure data;
        for (Message m : messages) {
            if (!m.dds.isCompressed()) {
                MessageUncompressedDataReader reader = new MessageUncompressedDataReader();
                data = reader.readEntireMessage(obs, proto, m, raf, null);
            } else {
                MessageCompressedDataReader reader = new MessageCompressedDataReader();
                data = reader.readEntireMessage(obs, proto, m, raf, null);
            }
            while (data.hasNext()) {
                StructureData sdata = (StructureData) data.next();
                add(sdata);
            }
        }
    }

    private void add(StructureData data) throws IOException {
        for (StructureMembers.Member m : data.getMembers()) {
            if (showB)
                System.out.printf("%s%n", m);
            if (m.getDataType() == DataType.SEQUENCE) {
                if (m.getName().equals("seq1")) {
                    ArraySequence seq = data.getArraySequence(m);
                    StructureDataIterator iter = seq.getStructureDataIterator();
                    while (iter.hasNext())
                        addTableEntryA(iter.next());
                } else if (m.getName().equals("seq2")) {
                    ArraySequence seq = data.getArraySequence(m);
                    StructureDataIterator iter = seq.getStructureDataIterator();
                    while (iter.hasNext())
                        addTableEntryB(iter.next());
                } else if (m.getName().equals("seq3")) {
                    ArraySequence seq = data.getArraySequence(m);
                    StructureDataIterator iter = seq.getStructureDataIterator();
                    while (iter.hasNext())
                        addTableEntryD(iter.next());
                }
            }
        }
    }

    private void addTableEntryA(StructureData sdata) {
        int scale = 0, refVal = 0, width = 0;
        String entry = "", line1 = "", line2 = "";
        List<StructureMembers.Member> members = sdata.getMembers();
        List<Variable> vars = seq1.getVariables();
        for (int i = 0; i < vars.size(); i++) {
            Variable v = vars.get(i);
            StructureMembers.Member m = members.get(i);
            String data = sdata.getScalarString(m);
            Attribute att = v.attributes().findAttribute(BufrIosp2.fxyAttName);
            switch (att.getStringValue()) {
                case "0-0-1":
                    entry = sdata.getScalarString(m);
                    System.out.println(entry);
                    break;
                case "0-0-2":
                    line1 = sdata.getScalarString(m);
                    System.out.println(line1);
                    break;
                case "0-0-3":
                    line2 = sdata.getScalarString(m);
                    System.out.println(line2);
                    break;
            }
        }

        int code = Integer.parseInt(entry);

        // split name and description from appended line 1 and 2
        String desc = (line1 + line2).trim();
        String name = "";
        int pos = desc.indexOf(' ');
        if (pos > 0) {
            name = desc.substring(0, pos);
        }

        TableA.Descriptor d = a.addDescriptor(code, desc);
        d.setName(name);
    }

    private void addTableEntryB(StructureData sdata) {
        String name = "", units = "", signScale = null, signRef = null;
        int scale = 0, refVal = 0, width = 0;
        short x1 = 0, y1 = 0;
        List<StructureMembers.Member> members = sdata.getMembers();
        List<Variable> vars = seq2.getVariables();
        for (int i = 0; i < vars.size(); i++) {
            Variable v = vars.get(i);
            StructureMembers.Member m = members.get(i);
            String data = sdata.getScalarString(m);
            if (showB)
                System.out.printf("%s == %s%n", v, data);

            Attribute att = v.attributes().findAttribute(BufrIosp2.fxyAttName);
            switch (att.getStringValue()) {
                case "0-0-10":
                    sdata.getScalarString(m);
                    break;
                case "0-0-11":
                    String x = sdata.getScalarString(m);
                    x1 = Short.parseShort(x.trim());
                    break;
                case "0-0-12":
                    String y = sdata.getScalarString(m);
                    y1 = Short.parseShort(y.trim());
                    break;
                case "0-0-13":
                    name = sdata.getScalarString(m);
                    break;
                case "0-0-14":
                    name += sdata.getScalarString(m); // append both lines

                    break;
                case "0-0-15":
                    units = sdata.getScalarString(m);
                    units = WmoXmlReader.cleanUnit(units.trim());
                    break;
                case "0-0-16":
                    signScale = sdata.getScalarString(m).trim();
                    break;
                case "0-0-17":
                    String scaleS = sdata.getScalarString(m);
                    scale = Integer.parseInt(scaleS.trim());
                    break;
                case "0-0-18":
                    signRef = sdata.getScalarString(m).trim();
                    break;
                case "0-0-19":
                    String refS = sdata.getScalarString(m);
                    refVal = Integer.parseInt(refS.trim());
                    break;
                case "0-0-20":
                    String widthS = sdata.getScalarString(m);
                    width = Integer.parseInt(widthS.trim());
                    break;
            }
        }
        if (showB)
            System.out.printf("%n");

        // split name and description from appended line 1 and 2
        String desc = null;
        name = name.trim();
        int pos = name.indexOf(' ');
        if (pos > 0) {
            desc = Util.cleanName(name.substring(pos + 1));
            name = name.substring(0, pos);
            name = Util.cleanName(name);
        }

        if ("-".equals(signScale))
            scale = -1 * scale;
        if ("-".equals(signRef))
            refVal = -1 * refVal;

        b.addDescriptor(x1, y1, scale, refVal, width, name, units, desc);
    }

    private void addTableEntryD(StructureData sdata) throws IOException {
        String name = null;
        short x1 = 0, y1 = 0;
        List<Short> dds = null;

        List<StructureMembers.Member> members = sdata.getMembers();
        List<Variable> vars = seq3.getVariables();
        for (int i = 0; i < vars.size(); i++) {
            Variable v = vars.get(i);
            StructureMembers.Member m = members.get(i);
            if (m.getName().equals("seq4")) {
                dds = getDescriptors(sdata.getArraySequence(m));
                continue;
            }

            Attribute att = v.attributes().findAttribute(BufrIosp2.fxyAttName);
            if (att != null) {
                if (showD)
                    System.out.printf("%s == %s%n", v, sdata.getScalarString(m));
                switch (att.getStringValue()) {
                    case "0-0-10":
                        sdata.getScalarString(m);
                        break;
                    case "0-0-11":
                        String x = sdata.getScalarString(m);
                        x1 = Short.parseShort(x.trim());
                        break;
                    case "0-0-12":
                        String y = sdata.getScalarString(m);
                        y1 = Short.parseShort(y.trim());
                        break;
                    case "2-5-64":
                        name = sdata.getScalarString(m);
                        break;
                }
            }
        }
        if (showD)
            System.out.printf("%n");

        name = Util.cleanName(name);

        d.addDescriptor(x1, y1, name, dds);
    }

    private List<Short> getDescriptors(ArraySequence seqdata) throws IOException {
        List<Short> list = new ArrayList<>();
        String fxyS = null;
        List<Variable> vars = seq4.getVariables();

        StructureDataIterator iter = seqdata.getStructureDataIterator();
        while (iter.hasNext()) {
            StructureData sdata = iter.next();

            List<StructureMembers.Member> members = sdata.getMembers();
            for (int i = 0; i < vars.size(); i++) {
                Variable v = vars.get(i);
                StructureMembers.Member m = members.get(i);
                String data = sdata.getScalarString(m);
                if (showD)
                    System.out.printf("%s == %s%n", v, data);

                Attribute att = v.attributes().findAttribute(BufrIosp2.fxyAttName);
                if (att != null && att.getStringValue().equals("0-0-30"))
                    fxyS = sdata.getScalarString(m);
            }
            if (showD)
                System.out.printf("%n");

            if (fxyS != null) {
                short id = Descriptor.getFxy2(fxyS);
                list.add(id);
            }
        }
        return list;
    }

    TableLookup getTableLookup() throws IOException {
        if (!tableRead) {
            read2();
            tableRead = true;
            tlookup = new TableLookup(ids, a, b, d);
        }
        return tlookup;
    }

}
