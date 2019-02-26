/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.geotiff.compression;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Read through a byte array
 *
 * @author osbornb
 */
public class ByteReader {

    /**
     * Next byte index to read
     */
    private int nextByte = 0;

    /**
     * Bytes to read
     */
    private final byte[] bytes;

    /**
     * Byte order
     */
    private ByteOrder byteOrder = null;

    /**
     * Constructor
     *
     * @param bytes bytes
     */
    public ByteReader(byte[] bytes) {
        this(bytes, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Constructor
     *
     * @param bytes bytes
     * @param byteOrder byte order
     */
    public ByteReader(byte[] bytes, ByteOrder byteOrder) {
        this.bytes = bytes;
        this.byteOrder = byteOrder;
    }

    /**
     * Get the next byte to be read
     *
     * @return next byte to be read
     */
    public int getNextByte() {
        return nextByte;
    }

    /**
     * Set the next byte to be read
     *
     * @param nextByte next byte
     */
    public void setNextByte(int nextByte) {
        this.nextByte = nextByte;
    }

    /**
     * Get the byte order
     *
     * @return byte order
     */
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * Set the byte order
     *
     * @param byteOrder byte order
     */
    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    /**
     * Check if there is at least one more byte left to read
     *
     * @return true more bytes left to read
     */
    public boolean hasByte() {
        return hasBytes(1);
    }

    /**
     * Check if there is at least one more byte left to read
     *
     * @param offset byte offset
     * @return true more bytes left to read
     */
    public boolean hasByte(int offset) {
        return hasBytes(offset, 1);
    }

    /**
     * Check if there are the provided number of bytes left to read
     *
     * @param count number of bytes
     * @return true if has at least the number of bytes left
     */
    public boolean hasBytes(int count) {
        return hasBytes(nextByte, count);
    }

    /**
     * Check if there are the provided number of bytes left to read
     *
     * @param offset byte offset
     * @param count number of bytes
     * @return true if has at least the number of bytes left
     */
    public boolean hasBytes(int offset, int count) {
        return offset + count <= bytes.length;
    }

    /**
     * Read a String from the provided number of bytes
     *
     * @param num number of bytes
     * @return String
     * @throws UnsupportedEncodingException
     */
    public String readString(int num) throws UnsupportedEncodingException {
        String value = readString(nextByte, num);
        nextByte += num;
        return value;
    }

    /**
     * Read a String from the provided number of bytes
     *
     * @param offset byte offset
     * @param num number of bytes
     * @return String
     * @throws UnsupportedEncodingException
     */
    public String readString(int offset, int num)
            throws UnsupportedEncodingException {
        verifyRemainingBytes(offset, num);
        String value = null;
        if (num != 1 || bytes[offset] != 0) {
            value = new String(bytes, offset, num, StandardCharsets.US_ASCII);
        }
        return value;
    }

    /**
     * Read a byte
     *
     * @return byte
     */
    public byte readByte() {
        byte value = readByte(nextByte);
        nextByte++;
        return value;
    }

    /**
     * Read a byte
     *
     * @param offset byte offset
     * @return byte
     */
    public byte readByte(int offset) {
        verifyRemainingBytes(offset, 1);
        byte value = bytes[offset];
        return value;
    }

    /**
     * Read an unsigned byte
     *
     * @return unsigned byte as short
     */
    public short readUnsignedByte() {
        short value = readUnsignedByte(nextByte);
        nextByte++;
        return value;
    }

    /**
     * Read an unsigned byte
     *
     * @param offset byte offset
     * @return unsigned byte as short
     */
    public short readUnsignedByte(int offset) {
        return ((short) (readByte(offset) & 0xff));
    }

    /**
     * Read a number of bytes
     *
     * @param num number of bytes
     * @return bytes
     */
    public byte[] readBytes(int num) {
        byte[] readBytes = readBytes(nextByte, num);
        nextByte += num;
        return readBytes;
    }

    /**
     * Read a number of bytes
     *
     * @param offset byte offset
     * @param num number of bytes
     * @return bytes
     */
    public byte[] readBytes(int offset, int num) {
        verifyRemainingBytes(offset, num);
        byte[] readBytes = Arrays.copyOfRange(bytes, offset, offset + num);
        return readBytes;
    }

    /**
     * Read a short
     *
     * @return short
     */
    public short readShort() {
        short value = readShort(nextByte);
        nextByte += 2;
        return value;
    }

    /**
     * Read a short
     *
     * @param offset byte offset
     * @return short
     */
    public short readShort(int offset) {
        verifyRemainingBytes(offset, 2);
        short value = ByteBuffer.wrap(bytes, offset, 2).order(byteOrder)
                .getShort();
        return value;
    }

    /**
     * Read an unsigned short
     *
     * @return unsigned short as int
     */
    public int readUnsignedShort() {
        int value = readUnsignedShort(nextByte);
        nextByte += 2;
        return value;
    }

    /**
     * Read an unsigned short
     *
     * @param offset byte offset
     * @return unsigned short as int
     */
    public int readUnsignedShort(int offset) {
        return (readShort(offset) & 0xffff);
    }

    /**
     * Read an integer
     *
     * @return integer
     */
    public int readInt() {
        int value = readInt(nextByte);
        nextByte += 4;
        return value;
    }

    /**
     * Read an integer
     *
     * @param offset byte offset
     * @return integer
     */
    public int readInt(int offset) {
        verifyRemainingBytes(offset, 4);
        int value = ByteBuffer.wrap(bytes, offset, 4).order(byteOrder).getInt();
        return value;
    }

    /**
     * Read an unsigned int
     *
     * @return unsigned int as long
     */
    public long readUnsignedInt() {
        long value = readUnsignedInt(nextByte);
        nextByte += 4;
        return value;
    }

    /**
     * Read an unsigned int
     *
     * @param offset byte offset
     * @return unsigned int as long
     */
    public long readUnsignedInt(int offset) {
        return ((long) readInt(offset) & 0xffffffffL);
    }

    /**
     * Read a float
     *
     * @return float
     */
    public float readFloat() {
        float value = readFloat(nextByte);
        nextByte += 4;
        return value;
    }

    /**
     * Read a float
     *
     * @param offset byte offset
     * @return float
     */
    public float readFloat(int offset) {
        verifyRemainingBytes(offset, 4);
        float value = ByteBuffer.wrap(bytes, offset, 4).order(byteOrder)
                .getFloat();
        return value;
    }

    /**
     * Read a double
     *
     * @return double
     */
    public double readDouble() {
        double value = readDouble(nextByte);
        nextByte += 8;
        return value;
    }

    /**
     * Read a double
     *
     * @param offset byte offset
     * @return double
     */
    public double readDouble(int offset) {
        verifyRemainingBytes(offset, 8);
        double value = ByteBuffer.wrap(bytes, offset, 8).order(byteOrder)
                .getDouble();
        return value;
    }

    /**
     * Get the byte length
     *
     * @return byte length
     */
    public int byteLength() {
        return bytes.length;
    }

    /**
     * Verify with the remaining bytes that there are enough remaining to read
     * the provided amount
     *
     * @param offset byte offset
     * @param bytesToRead number of bytes to read
     */
    private void verifyRemainingBytes(int offset, int bytesToRead) {
        if (offset + bytesToRead > bytes.length) {
            throw new IllegalStateException(
                    "No more remaining bytes to read. Total Bytes: "
                    + bytes.length + ", Byte offset: " + offset
                    + ", Attempted to read: " + bytesToRead);
        }
    }

}
