/*
 * Copyright (c) 1998-2020 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import org.jdom2.Element;
import ucar.ma2.*;
import ucar.nc2.*;
import ucar.nc2.constants.DataFormatType;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.util.*;

/**
 * IOSP for BUFR data - version 2, using the preprocessor.
 *
 * @author caron
 * @since 8/8/13
 */
public class BufrIosp2 extends AbstractIOServiceProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BufrIosp2.class);

    public static final String obsRecordName = "obs";
    public static final String fxyAttName = "BUFR:TableB_descriptor";
    public static final String centerId = "BUFR:centerId";

    // debugging
    private static boolean debugIter;

    public static void setDebugFlags(ucar.nc2.util.DebugFlags debugFlag) {
        debugIter = debugFlag.isSet("Bufr/iter");
    }

    //private Structure obsStructure;
    //private Message protoMessage; // prototypical message: all messages in the file must be the same.
    private MessageScanner scanner;
    private List<Message> protoMessages;  // prototypical messages: the messages with different category.
    private List<RootVariable> rootVariables;
    private HashSet<Integer> messHash;
    private boolean isSingle;
    private BufrConfig config;
    private Element iospParam;

    @Override
    public boolean isValidFile(RandomAccessFile raf) throws IOException {
        return MessageScanner.isValidFile(raf);
    }

    @Override
    public boolean isBuilder() {
        return true;
    }

    public void build(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        Group.Builder rootGroup = new Group.Builder().setName("");

        this.build(raf, rootGroup, null);
        System.out.println(rootGroup);
    }

    @Override
    public void build(RandomAccessFile raf, Group.Builder rootGroup, CancelTask cancelTask) throws IOException {
        super.open(raf, rootGroup.getNcfile(), cancelTask);

        scanner = new MessageScanner(raf);
        Message protoMessage = scanner.getFirstDataMessage();
        if (protoMessage == null)
            throw new IOException("No data messages in the file= " + raf.getLocation());
        if (!protoMessage.isTablesComplete())
            throw new IllegalStateException("BUFR file has incomplete tables");

        // get all prototype messages - contains different message category in a Bufr data file
        protoMessages = new ArrayList<>();
        protoMessages.add(protoMessage);
        int category = protoMessage.ids.getCategory();
        while (scanner.hasNext()) {
            Message message = scanner.next();
            if (message.ids.getCategory() != category) {
                protoMessages.add(message);
                category = message.ids.getCategory();
            }
        }

        // just get the fields
        BufrConfig config = BufrConfig.openFromMessage(raf, protoMessage, iospParam);

        // this fills the netcdf object
        if (this.protoMessages.size() == 1) {
            new BufrIospBuilder(protoMessage, config, rootGroup, raf.getLocation());
        } else {
            List<BufrConfig> configs = new ArrayList<>();
            for (Message message : protoMessages) {
                configs.add(BufrConfig.openFromMessage(raf, message, iospParam));
            }
            new BufrIospBuilder(protoMessage, configs, rootGroup, raf.getLocation());
        }

        isSingle = false;
    }

    @Override
    public void buildFinish(NetcdfFile ncfile) {
        // support multiple root variables in one Bufr data file
        this.rootVariables = new ArrayList<>();
        if (this.protoMessages.size() == 1) {
            Structure obsStructure = (Structure) ncfile.findVariable(obsRecordName);
            // The proto DataDescriptor must have a link to the Sequence object to read nested Sequences.
            connectSequences(obsStructure.getVariables(), protoMessages.get(0).getRootDataDescriptor().getSubKeys());
            this.rootVariables.add(new RootVariable(protoMessages.get(0), obsStructure));
        } else {
            for (int i = 0; i < this.protoMessages.size(); i++) {
                Structure variable = (Structure) ncfile.getVariables().get(i);
                Message message = protoMessages.get(i);
                connectSequences(variable.getVariables(), message.getRootDataDescriptor().getSubKeys());
                this.rootVariables.add(new RootVariable(message, variable));
            }
        }
    }

    private void connectSequences(List<Variable> variables, List<DataDescriptor> dataDescriptors) {
        for (Variable v : variables) {
            if (v instanceof Sequence) {
                findDataDescriptor(dataDescriptors, v.getShortName()).ifPresent(dds -> dds.refersTo = (Sequence) v);
            }
            if (v instanceof Structure) { // recurse
                findDataDescriptor(dataDescriptors, v.getShortName())
                        .ifPresent(dds -> connectSequences(((Structure) v).getVariables(), dds.getSubKeys()));
            }
        }
    }

    private Optional<DataDescriptor> findDataDescriptor(List<DataDescriptor> dataDescriptors, String name) {
        Optional<DataDescriptor> ddsOpt = dataDescriptors.stream().filter(d -> name.equals(d.name)).findFirst();
        if (ddsOpt.isPresent()) {
            return ddsOpt;
        } else {
            throw new IllegalStateException("DataDescriptor does not contain " + name);
        }
    }

    @Override
    public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {
        super.open(raf, ncfile, cancelTask);

        scanner = new MessageScanner(raf);
        Message protoMessage = scanner.getFirstDataMessage();
        if (protoMessage == null)
            throw new IOException("No data messages in the file= " + ncfile.getLocation());
        if (!protoMessage.isTablesComplete())
            throw new IllegalStateException("BUFR file has incomplete tables");

        // just get the fields
        BufrConfig config = BufrConfig.openFromMessage(raf, protoMessage, iospParam);

        // this fills the netcdf object
        Construct2 construct = new Construct2(protoMessage, config, ncfile);
        Structure obsStructure = construct.getObsStructure();
        ncfile.finish();
        isSingle = false;
    }

    // for BufrMessageViewer
    public void open(RandomAccessFile raf, NetcdfFile ncfile, Message single) throws IOException {
        this.raf = raf;

        Message protoMessage = single;
        protoMessage.getRootDataDescriptor(); // construct the data descriptors, check for complete tables
        if (!protoMessage.isTablesComplete())
            throw new IllegalStateException("BUFR file has incomplete tables");

        BufrConfig config = BufrConfig.openFromMessage(raf, protoMessage, null);

        // this fills the netcdf object
        Construct2 construct = new Construct2(protoMessage, config, ncfile);
        Structure obsStructure = construct.getObsStructure();
        isSingle = true;

        ncfile.finish();
        this.ncfile = ncfile;
    }

    @Override
    public Object sendIospMessage(Object message) {
        if (message instanceof Element) {
            iospParam = (Element) message;
            iospParam.detach();
            return true;
        }

        return super.sendIospMessage(message);
    }

    /*public BufrConfig getConfig() {
        return config;
    }*/

    public Element getElem() {
        return iospParam;
    }

    private int nelems = -1;

    @Override
    public Array readData(Variable v2, Section section) {
        RootVariable rootVariable = findRootSequence(v2);
        Structure obsStructure = rootVariable.getVariable();
        return new ArraySequence(obsStructure.makeStructureMembers(), new SeqIter(rootVariable), nelems);
    }

    @Override
    public StructureDataIterator getStructureIterator(Structure s, int bufferSize) {
        RootVariable rootVariable = findRootSequence(s);
        return isSingle ? new SeqIterSingle(rootVariable) : new SeqIter(rootVariable);
    }

    private Structure findRootSequence() {
        return (Structure) this.ncfile.findVariable(BufrIosp2.obsRecordName);
    }

    // find root sequence from root variable list
    private RootVariable findRootSequence(Variable var) {
        for (RootVariable rootVariable : this.rootVariables) {
            if (rootVariable.getVariable().getShortName().equals(var.getShortName())) {
                return rootVariable;
            }
        }
        return null;
    }

    // root variable contains prototype message and corresponding variable
    private class RootVariable {
        private Message protoMessage;
        private Structure variable;

        public RootVariable(Message message, Structure variable) {
            this.protoMessage = message;
            this.variable = variable;
        }

        public Message getProtoMessage() {
            return this.protoMessage;
        }

        public Structure getVariable() {
            return this.variable;
        }
    }

    private class SeqIter implements StructureDataIterator {
        StructureDataIterator currIter;
        int recnum;
        // add its own prototype message and observation structure
        Message protoMessage;
        Structure obsStructure;

        SeqIter(Message message, Structure structure) {
            this.protoMessage = message;
            this.obsStructure = structure;
            reset();
        }

        SeqIter(RootVariable rootVariable) {
            this(rootVariable.protoMessage, rootVariable.variable);
        }

        @Override
        public StructureDataIterator reset() {
            recnum = 0;
            currIter = null;
            scanner.reset();
            return this;
        }

        @Override
        public boolean hasNext() throws IOException {
            if (currIter == null) {
                currIter = readNextMessage();
                if (currIter == null) {
                    nelems = recnum;
                    return false;
                }
            }

            if (!currIter.hasNext()) {
                currIter = readNextMessage();
                return hasNext();
            }

            return true;
        }

        @Override
        public StructureData next() throws IOException {
            recnum++;
            return currIter.next();
        }

        private StructureDataIterator readNextMessage() throws IOException {
            if (!scanner.hasNext())
                return null;
            Message m = scanner.next();
            if (m == null) {
                log.warn("BUFR scanner hasNext() true but next() null!");
                return null;
            }
            if (m.containsBufrTable()) // data messages only
                return readNextMessage();

            // mixed messages
            if (!protoMessage.equals(m)) {
                if (messHash == null)
                    messHash = new HashSet<>(20);
                if (!messHash.contains(m.hashCode())) {
                    log.warn("File " + raf.getLocation() + " has different BUFR message types hash=" + protoMessage.hashCode()
                            + "; skipping");
                    messHash.add(m.hashCode());
                }
                return readNextMessage();
            }

            ArrayStructure as = readMessage(m);
            return as.getStructureDataIterator();
        }

        private ArrayStructure readMessage(Message m) throws IOException {
            ArrayStructure as;
            if (m.dds.isCompressed()) {
                MessageCompressedDataReader reader = new MessageCompressedDataReader();
                as = reader.readEntireMessage(obsStructure, protoMessage, m, raf, null);
            } else {
                MessageUncompressedDataReader reader = new MessageUncompressedDataReader();
                as = reader.readEntireMessage(obsStructure, protoMessage, m, raf, null);
            }
            return as;
        }

        @Override
        public int getCurrentRecno() {
            return recnum - 1;
        }

        @Override
        public void close() {
            if (currIter != null)
                currIter.close();
            currIter = null;
            if (debugIter)
                System.out.printf("BUFR read recnum %d%n", recnum);
        }
    }

    private class SeqIterSingle implements StructureDataIterator {
        StructureDataIterator currIter;
        int recnum;
        // add its own prototype message and observation structure
        Message protoMessage;
        Structure obsStructure;

        SeqIterSingle(Message message, Structure structure) {
            protoMessage = message;
            obsStructure = structure;
            reset();
        }

        SeqIterSingle(RootVariable rootVariable) {
            this(rootVariable.protoMessage, rootVariable.variable);
        }

        @Override
        public StructureDataIterator reset() {
            recnum = 0;
            currIter = null;
            return this;
        }

        @Override
        public boolean hasNext() throws IOException {
            if (currIter == null) {
                currIter = readProtoMessage();
                if (currIter == null) {
                    nelems = recnum;
                    return false;
                }
            }

            return currIter.hasNext();
        }

        @Override
        public StructureData next() throws IOException {
            recnum++;
            return currIter.next();
        }

        private StructureDataIterator readProtoMessage() throws IOException {
            Message m = protoMessage;
            ArrayStructure as;
            if (m.dds.isCompressed()) {
                MessageCompressedDataReader reader = new MessageCompressedDataReader();
                as = reader.readEntireMessage(obsStructure, protoMessage, m, raf, null);
            } else {
                MessageUncompressedDataReader reader = new MessageUncompressedDataReader();
                as = reader.readEntireMessage(obsStructure, protoMessage, m, raf, null);
            }

            return as.getStructureDataIterator();
        }

        @Override
        public int getCurrentRecno() {
            return recnum - 1;
        }

        @Override
        public void close() {
            if (currIter != null)
                currIter.close();
            currIter = null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getDetailInfo() {
        Formatter ff = new Formatter();
        ff.format("%s", super.getDetailInfo());
        protoMessages.get(0).dump(ff);
        ff.format("%n");
        config.show(ff);
        return ff.toString();
    }

    @Override
    public String getFileTypeId() {
        return DataFormatType.BUFR.getDescription();
    }

    @Override
    public String getFileTypeDescription() {
        return "WMO Binary Universal Form";
    }

}
