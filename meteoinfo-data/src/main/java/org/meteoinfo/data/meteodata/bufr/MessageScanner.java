/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import ucar.unidata.io.KMPMatch;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Sequentially scans a BUFR file, extracts the messages.
 *
 * @author caron
 * @since May 9, 2008
 */
public class MessageScanner {
    // static public final int MAX_MESSAGE_SIZE = 500 * 1000; // GTS allows up to 500 Kb messages (ref?)
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageScanner.class);

    private static final KMPMatch matcher = new KMPMatch("BUFR".getBytes(StandardCharsets.UTF_8));

    /**
     * is this a valid BUFR file.
     *
     * @param raf check this file
     * @return true if its a BUFR file
     * @throws IOException on read error
     */
    public static boolean isValidFile(RandomAccessFile raf) throws IOException {
        raf.seek(0);
        if (!raf.searchForward(matcher, 40 * 1000))
            return false; // must find "BUFR" in first 40k
        raf.skipBytes(4);
        BufrIndicatorSection is = new BufrIndicatorSection(raf);
        if (is.getBufrEdition() > 4)
            return false;
        // if(is.getBufrLength() > MAX_MESSAGE_SIZE) return false;
        return !(is.getBufrLength() > raf.length());
    }

    /////////////////////////////////

    private RandomAccessFile raf;
    private boolean useEmbeddedTables;

    private int countMsgs;
    private int countObs;
    private byte[] header;
    private long startPos;
    private long lastPos;
    private boolean debug;

    private EmbeddedTable embedTable;

    public MessageScanner(RandomAccessFile raf) throws IOException {
        this(raf, 0, true);
    }

    public MessageScanner(RandomAccessFile raf, long startPos, boolean useEmbeddedTables) throws IOException {
        startPos = (startPos < 30) ? 0 : startPos - 30; // look for the header
        this.raf = raf;
        lastPos = startPos;
        this.useEmbeddedTables = useEmbeddedTables;
        raf.seek(startPos);
        raf.order(RandomAccessFile.BIG_ENDIAN);
    }

    public Message getFirstDataMessage() throws IOException {
        while (hasNext()) {
            Message m = next();
            if (m == null)
                continue;
            if (m.containsBufrTable())
                continue; // not data
            if (m.getNumberDatasets() == 0)
                continue; // empty
            return m;
        }
        return null;
    }

    public void reset() {
        lastPos = 0;
    }

    public boolean hasNext() throws IOException {
        if (lastPos >= raf.length())
            return false;
        raf.seek(lastPos);
        boolean more = raf.searchForward(matcher, -1); // will scan to end for another BUFR header
        if (more) {
            long stop = raf.getFilePointer();
            int sizeHeader = (int) (stop - lastPos);
            if (sizeHeader > 30)
                sizeHeader = 30;
            header = new byte[sizeHeader];
            startPos = stop - sizeHeader;
            raf.seek(startPos);
            int nRead = raf.read(header);
            if (nRead != header.length) {
                log.warn("Unable to read full BUFR header. Got " + nRead + " but expected " + header.length);
                return false;
            }
        }
        if (debug && countMsgs % 100 == 0)
            System.out.printf("%d ", countMsgs);
        return more;
    }

    public Message next() {

        try {
            long start = raf.getFilePointer();
            raf.seek(start + 4);

            BufrIndicatorSection is = new BufrIndicatorSection(raf);
            BufrIdentificationSection ids = new BufrIdentificationSection(raf, is);
            BufrDataDescriptionSection dds = new BufrDataDescriptionSection(raf);

            long dataPos = raf.getFilePointer();
            int dataLength = BufrNumbers.uint3(raf);
            BufrDataSection dataSection = new BufrDataSection(dataPos, dataLength);
            lastPos = dataPos + dataLength + 4; // position to the end message plus 1
            // nbytes += lastPos - startPos;

            /*
             * length consistency checks
             * if (is.getBufrLength() > MAX_MESSAGE_SIZE) {
             * log.warn("Illegal length - BUFR message at pos "+start+" header= "+cleanup(header)+" size= "+is.getBufrLength()
             * );
             * return null;
             * }
             */

            if (is.getBufrEdition() > 4) {
                log.warn("Illegal edition - BUFR message at pos " + start + " header= " + cleanup(header));
                return null;
            }

            if (is.getBufrEdition() < 2) {
                log.warn("Edition " + is.getBufrEdition() + " is not supported - BUFR message at pos " + start + " header= "
                        + cleanup(header));
                return null;
            }

            // check that end section is correct
            long ending = dataPos + dataLength;
            raf.seek(dataPos + dataLength);
            for (int i = 0; i < 3; i++) {
                if (raf.read() != 55) {
                    log.warn("Missing End of BUFR message at pos= {} header= {} file= {}", ending, cleanup(header),
                            raf.getLocation());
                    return null;
                }
            }
            // allow off by one : may happen when dataLength rounded to even bytes
            if (raf.read() != 55) {
                raf.seek(dataPos + dataLength - 1); // see if byte before is a '7'
                if (raf.read() != 55) {
                    log.warn("Missing End of BUFR message at pos= {} header= {} edition={} file= {}", ending, cleanup(header),
                            is.getBufrEdition(), raf.getLocation());
                    return null;
                } else {
                    log.info("End of BUFR message off-by-one at pos= {} header= {} edition={} file= {}", ending, cleanup(header),
                            is.getBufrEdition(), raf.getLocation());
                    lastPos--;
                }
            }

            Message m = new Message(raf, is, ids, dds, dataSection);
            m.setHeader(cleanup(header));
            m.setStartPos(start);

            if (useEmbeddedTables && m.containsBufrTable()) {
                if (embedTable == null)
                    embedTable = new EmbeddedTable(m, raf);
                embedTable.addTable(m);
            } else if (embedTable != null) {
                m.setTableLookup(embedTable.getTableLookup());
            }

            countMsgs++;
            countObs += dds.getNumberDatasets();
            raf.seek(start + is.getBufrLength());
            return m;

        } catch (IOException ioe) {
            log.error("Error reading message at " + lastPos, ioe);
            lastPos = raf.getFilePointer(); // dont do an infinite loop
            return null;
        }
    }

    public TableLookup getTableLookup() throws IOException {
        while (hasNext()) {
            next();
        }
        return (embedTable != null) ? embedTable.getTableLookup() : null;
    }

    public byte[] getMessageBytesFromLast(ucar.nc2.iosp.bufr.Message m) throws IOException {
        long startPos = m.getStartPos();
        int length = (int) (lastPos - startPos);
        byte[] result = new byte[length];

        raf.seek(startPos);
        raf.readFully(result);
        return result;
    }

    public byte[] getMessageBytes(Message m) throws IOException {
        long startPos = m.getStartPos();
        int length = m.is.getBufrLength();
        byte[] result = new byte[length];

        raf.seek(startPos);
        raf.readFully(result);
        return result;
    }

    public int getTotalObs() {
        return countObs;
    }

    public int getTotalMessages() {
        return countMsgs;
    }

    // the WMO header is in here somewhere when the message comes over the IDD
    private static String cleanup(byte[] h) {
        byte[] bb = new byte[h.length];
        int count = 0;
        for (byte b : h) {
            if (b >= 32 && b < 127)
                bb[count++] = b;
        }
        return new String(bb, 0, count, StandardCharsets.UTF_8);
    }

    public long writeCurrentMessage(WritableByteChannel out) throws IOException {
        long nbytes = lastPos - startPos;
        return raf.readToByteChannel(out, startPos, nbytes);
    }

}
