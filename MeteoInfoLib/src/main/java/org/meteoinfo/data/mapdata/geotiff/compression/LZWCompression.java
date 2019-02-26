/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.geotiff.compression;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.nio.ByteOrder;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

/**
 * LZW Compression
 *
 * @author osbornb
 */
public class LZWCompression implements CompressionDecoder, CompressionEncoder {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(LZWCompression.class
            .getName());

    /**
     * Clear code
     */
    private static final int CLEAR_CODE = 256;

    /**
     * End of information code
     */
    private static final int EOI_CODE = 257;

    /**
     * Min bits
     */
    private static final int MIN_BITS = 9;

    /**
     * Table entries
     */
    private Map<Integer, Integer[]> table = new HashMap<>();

    /**
     * Current max table code
     */
    private int maxCode;

    /**
     * Current byte length
     */
    private int byteLength;

    /**
     * Current byte compression position
     */
    private int position;

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decode(byte[] bytes, ByteOrder byteOrder) {

        // Create the byte reader and decoded stream to write to
        ByteReader reader = new ByteReader(bytes, byteOrder);
        ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();

        // Initialize the table, starting position, and old code
        initializeTable();
        position = 0;
        int oldCode = 0;

        // Read codes until end of input
        int code = getNextCode(reader);
        while (code != EOI_CODE) {

            // If a clear code
            if (code == CLEAR_CODE) {

                // Reset the table
                initializeTable();

                // Read past clear codes
                code = getNextCode(reader);
                while (code == CLEAR_CODE) {
                    code = getNextCode(reader);
                }
                if (code == EOI_CODE) {
                    break;
                }
                if (code > CLEAR_CODE) {
                    throw new TiffException("Corrupted code at scan line: "
                            + code);
                }

                // Write the code value
                Integer[] value = table.get(code);
                writeValue(decodedStream, value);
                oldCode = code;

            } else {

                // If already in the table
                Integer[] value = table.get(code);
                if (value != null) {

                    // Write the code value
                    writeValue(decodedStream, value);

                    // Create new value and add to table
                    Integer[] newValue = concat(table.get(oldCode),
                            table.get(code)[0]);
                    addToTable(newValue);
                    oldCode = code;

                } else {

                    // Create and write new value from old value
                    Integer[] oldValue = table.get(oldCode);
                    Integer[] newValue = concat(oldValue, oldValue[0]);
                    writeValue(decodedStream, newValue);

                    // Write value to the table
                    addToTable(code, newValue);
                    oldCode = code;
                }
            }

            // Get the next code
            code = getNextCode(reader);
        }

        byte[] decoded = decodedStream.toByteArray();

        return decoded;
    }

    /**
     * Initialize the table and byte length
     */
    private void initializeTable() {
        table.clear();
        for (int i = 0; i <= 257; i++) {
            table.put(i, new Integer[]{i});
        }
        maxCode = 257;
        byteLength = MIN_BITS;
    }

    /**
     * Check the byte length and increase if needed
     */
    private void checkByteLength() {
        if (maxCode >= Math.pow(2, byteLength) - 2) {
            byteLength++;
        }
    }

    /**
     * Add the value to the table
     *
     * @param value value
     */
    private void addToTable(Integer[] value) {
        addToTable(maxCode + 1, value);
    }

    /**
     * Add the code and value to the table
     *
     * @param code code
     * @param value value
     */
    private void addToTable(int code, Integer[] value) {
        table.put(code, value);
        maxCode = Math.max(maxCode, code);
        checkByteLength();
    }

    /**
     * Concatenate the two values
     *
     * @param first first value
     * @param second second value
     * @return concatenated value
     */
    private Integer[] concat(Integer[] first, Integer second) {
        return concat(first, new Integer[]{second});
    }

    /**
     * Concatenate the two values
     *
     * @param first first value
     * @param second second value
     * @return concatenated value
     */
    private Integer[] concat(Integer[] first, Integer[] second) {
        Integer[] combined = new Integer[first.length + second.length];
        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

    /**
     * Write the value to the decoded stream
     *
     * @param decodedStream decoded byte stream
     * @param value value
     */
    private void writeValue(ByteArrayOutputStream decodedStream, Integer[] value) {
        for (int i = 0; i < value.length; i++) {
            decodedStream.write(value[i]);
        }
    }

    /**
     * Get the next code
     *
     * @param reader byte reader
     * @return code
     */
    private int getNextCode(ByteReader reader) {
        int nextByte = getByte(reader);
        position += byteLength;
        return nextByte;
    }

    /**
     * Get the next byte
     *
     * @param reader byte reader
     * @return byte
     */
    private int getByte(ByteReader reader) {

        int d = position % 8;
        int a = (int) Math.floor(position / 8.0);
        int de = 8 - d;
        int ef = (position + byteLength) - ((a + 1) * 8);
        int fg = 8 * (a + 2) - (position + byteLength);
        int dg = (a + 2) * 8 - position;
        fg = Math.max(0, fg);
        if (a >= reader.byteLength()) {
            logger.log(Level.WARNING,
                    "End of data reached without an end of input code");
            return EOI_CODE;
        }
        int chunk1 = ((int) reader.readUnsignedByte(a))
                & ((int) (Math.pow(2, 8 - d) - 1));
        chunk1 = chunk1 << (byteLength - de);
        int chunks = chunk1;
        if (a + 1 < reader.byteLength()) {
            int chunk2 = reader.readUnsignedByte(a + 1) >>> fg;
            chunk2 = chunk2 << Math.max(0, byteLength - dg);
            chunks += chunk2;
        }
        if (ef > 8 && a + 2 < reader.byteLength()) {
            int hi = (a + 3) * 8 - (position + byteLength);
            int chunk3 = reader.readUnsignedByte(a + 2) >>> hi;
            chunks += chunk3;
        }
        return chunks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rowEncoding() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encode(byte[] bytes, ByteOrder byteOrder) {
        throw new TiffException("LZW encoder is not yet implemented");
    }

}
