/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.bufr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.global.DataConvert;
import ucar.nc2.iosp.bufr.BufrTableLookup;
import ucar.nc2.iosp.bufr.DataDescriptor;
import ucar.nc2.iosp.bufr.Descriptor;
import ucar.nc2.iosp.bufr.Message;
import ucar.nc2.iosp.bufr.MessageScanner;

/**
 *
 * @author Yaqiang Wang
 */
public class BufrDataInfo {

    // <editor-fold desc="Variables">
    private RandomAccessFile bw = null;
    private long indicatorPos = 0;
    private long dataPos = 0;
    private int edition = 3;
    private BufrTableLookup lookup;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Read first message
     *
     * @param fileName Bufr File Name
     * @return First message
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Message readFirstMessage(String fileName) throws FileNotFoundException, IOException {
        ucar.unidata.io.RandomAccessFile br = new ucar.unidata.io.RandomAccessFile(fileName, "r");
        MessageScanner ms = new MessageScanner(br);
        Message m = ms.getFirstDataMessage();
        br.close();
        return m;
    }

    /**
     * Create Bufr binary data file
     *
     * @param fileName File name
     */
    public void createDataFile(String fileName) {
        try {
            bw = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BufrDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Close the data file created by previos step
     */
    public void closeDataFile() {
        try {
            bw.close();
            bw = null;
        } catch (IOException ex) {
            Logger.getLogger(BufrDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write indicator section
     *
     * @param bufrLength The total length of the message
     * @param edition Bufr edition
     * @return Indicator section length
     * @throws IOException
     */
    public int writeIndicatorSection(int bufrLength, int edition) throws IOException {
        this.indicatorPos = bw.getFilePointer();
        bw.writeBytes("BUFR");
        byte[] ints = DataConvert.toUint3Int(bufrLength);
        bw.write(ints);
        bw.write(edition);
        this.edition = edition;
        
        return 8;
    }
    
    /**
     * Write indicator section
     *
     * @param bufrLength The total length of the message
     * @param edition Bufr edition
     * @throws IOException
     */
    public void reWriteIndicatorSection(int bufrLength, int edition) throws IOException {
        bw.seek(this.indicatorPos);
        bw.writeBytes("BUFR");
        byte[] ints = DataConvert.toUint3Int(bufrLength);
        bw.write(ints);
        bw.write(edition);
        this.edition = edition;
        bw.seek(bw.length());
    }

    /**
     * Write identification section
     *
     * @param len Section length
     * @param master_table Master table
     * @param subcenter_id Subcenter id
     * @param center_id Center id
     * @param update_sequence Update sequency
     * @param optional Optional
     * @param category Category
     * @param sub_category Sub category
     * @param master_table_version Master table version
     * @param local_table_version Local table version
     * @param year Year
     * @param month Month
     * @param day Day
     * @param hour Hour
     * @param minute Minute
     * @return Section length
     * @throws IOException
     */
    public int writeIdentificationSection(int len, int master_table, int subcenter_id, int center_id,
            int update_sequence, int optional, int category, int sub_category, int master_table_version,
            int local_table_version, int year, int month, int day, int hour, int minute) throws IOException {
        byte[] ints = DataConvert.toUint3Int(len);
        bw.write(ints);
        bw.write(master_table);
        bw.write(subcenter_id);
        bw.write(center_id);
        bw.write(update_sequence);
        bw.write(optional);
        bw.write(category);
        bw.write(sub_category);
        bw.write(master_table_version);
        bw.write(local_table_version);
        bw.write(year);
        bw.write(month);
        bw.write(day);
        bw.write(hour);
        bw.write(minute);
        bw.write(0);
        //this.lookup = new BufrTableLookup(this.edition, center_id, subcenter_id, master_table,
        //master_table_version, local_table_version, category, sub_category, 0);
        
        return len;
    }

    /**
     * Write data description section
     *
     * @param ndatasets Number of datasets
     * @param datatype Data type
     * @param descriptors Data descriptors
     * @return Section length
     * @throws IOException
     */
    public int writeDataDescriptionSection(int ndatasets, int datatype,
            List<String> descriptors) throws IOException {
        int len = 7 + descriptors.size() * 2;
        byte[] ints = DataConvert.toUint3Int(len);
        bw.write(ints);
        bw.write(0);
        ints = DataConvert.toUint2Int(ndatasets);
        bw.write(ints);
        bw.write(datatype);
        for (String des : descriptors) {
            short fxy = Descriptor.getFxy(des);
            bw.writeShort(fxy);
        }
        
        return len;
    }

    /**
     * Write data section head
     *
     * @param len Length
     * @return Data section head length
     * @throws IOException
     */
    public int writeDataSectionHead(int len) throws IOException {
        this.dataPos = bw.getFilePointer();
        byte[] ints = DataConvert.toUint3Int(len);
        bw.write(ints);
        bw.write(0);
        
        return 4;
    }
    
    /**
     * Write data section head
     *
     * @param len Length
     * @throws IOException
     */
    public void reWriteDataSectionHead(int len) throws IOException {
        bw.seek(this.dataPos);
        byte[] ints = DataConvert.toUint3Int(len);
        bw.write(ints);
        bw.write(0);
        bw.seek(bw.length());
    }

    /**
     * Write a int value
     *
     * @param value Value
     * @param nbits bit number
     * @return Data length
     * @throws IOException
     */
    public int write(int value, int nbits) throws IOException {
        BitSet bits = new BitSet(nbits);
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        byte[] bytes = bits.toByteArray();
        int n = nbits / 8;
        if (bytes.length < n) {
            byte[] nbytes = new byte[n];
            for (int i = 0; i < n; i++) {
                if (i < n - bytes.length) {
                    nbytes[i] = (byte) 0;
                } else {
                    nbytes[i] = bytes[i - (n - bytes.length)];
                }
            }
            bw.write(nbytes);
            return nbytes.length;
        } else {
            bw.write(bytes);
            return bytes.length;
        }
    }

    /**
     * Write a int value
     *
     * @param value Value
     * @param des Data descriptor string
     * @return Data length
     * @throws IOException
     */
    public int write(String des, int value) throws IOException {
        DataDescriptor dds = new DataDescriptor(Descriptor.getFxy(des), null);
        int len = dds.getBitWidth();
        return write(value, len);
    }

    /**
     * Write int values
     *
     * @param values Values
     * @param nbits bit numbers
     * @return Data length
     * @throws IOException
     */
    public int write(List<Integer> values, List<Integer> nbits) throws IOException {
        int tnb = 0;
        for (int n : nbits) {
            tnb += n;
        }
        BitSet bits = new BitSet(tnb);
        int sidx = 0;
        for (int k = 0; k < values.size(); k++) {
            int value = values.get(k);
            int nb = nbits.get(k);
            BitSet temp = new BitSet(nb);
            int index = 0;
            while (value != 0L) {
                if (value % 2L != 0) {
                    temp.set(index);
                }
                ++index;
                value = value >>> 1;
            }
            for (int i = 0; i < index; i++){
                if (temp.get(i)){
                    bits.set(sidx + nb - i - 1);
                }
            }
            sidx += nb;
        }
        byte[] bytes = toByteArray(bits);
        bw.write(bytes);
        return bytes.length;
    }

    public byte[] toByteArray(BitSet bs) {
        if (bs.size() == 0) {
            return new byte[0];
        }

        // Find highest bit
        int hiBit = -1;
        for (int i = 0; i < bs.size(); i++) {
            if (bs.get(i)) {
                hiBit = i;
            }
        }

        int n = (hiBit + 8) / 8;
        byte[] bytes = new byte[n];
        if (n == 0) {
            return bytes;
        }

        Arrays.fill(bytes, (byte) 0);
        for (int i = 0; i < n * 8; i++) {
            if (bs.get(i)) {
                setBit(i, bytes);
            }
        }

        return bytes;
    }

    protected static int BIT_MASK[]
            = {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};

    protected static void setBit(int bit, byte[] bytes) {
        int size = bytes == null ? 0 : bytes.length * 8;

        if (bit >= size) {
            throw new ArrayIndexOutOfBoundsException("Byte array too small");
        }

        bytes[bit / 8] |= BIT_MASK[bit % 8];
    }

    /**
     * Write end section
     *
     * @return End section length
     * @throws IOException
     */
    public int writeEndSection() throws IOException {
        bw.writeBytes("7777");
        return 4;
    }
    // </editor-fold>
}
