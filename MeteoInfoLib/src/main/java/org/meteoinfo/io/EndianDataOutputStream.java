/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.io;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author yaqiang - come from OpenJump
 */
public class EndianDataOutputStream {

    private final java.io.DataOutputStream outputStream;

    /**
     * Creates new EndianDataOutputStream
     * @param out
     */
    public EndianDataOutputStream(java.io.OutputStream out) {
        outputStream = new DataOutputStream(out);
    }

    /**
     * close stream*
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        outputStream.close();
    }
    
    /**
     * Write bytes
     * @param b bytes
     * @throws IOException 
     */
    public void write(byte[] b) throws IOException{
        outputStream.write(b);
    }

    /**
     * write bytes
     * @param b
     * @param off
     * @param len
     * @throws java.io.IOException
     */
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    /**
     * flush stream*
     * @throws java.io.IOException
     */
    public void flush() throws IOException {
        outputStream.flush();
    }

    /**
     * write a byte in LittleEndian - this is exactly the same as the BigEndian
     * version since there's no endian in a single byte
     * @param b
     * @throws java.io.IOException
     */
    public void writeByteLE(int b) throws IOException {
        outputStream.writeByte(b);
    }

    /**
     * write a byte in BigEndian - this is exactly the same as the LittleEndian
     * version since there's no endian in a single byte
     * @param b
     * @throws java.io.IOException
     */
    public void writeByteBE(int b) throws IOException {
        outputStream.writeByte(b);
    }

    /**
     * write a set of bytes in LittleEndian - this is exactly the same as the
     * BigEndian version since there's no endian in a single byte
     * @param s
     * @throws java.io.IOException
     */
    public void writeBytesLE(String s) throws IOException {
        outputStream.writeBytes(s);
    }

    /**
     * write a set of bytes in BigEndian - this is exactly the same as the
     * LittleEndian version since there's no endian in a single byte
     * @param s
     * @throws java.io.IOException
     */
    public void writeBytesBE(String s) throws IOException {
        outputStream.writeBytes(s);
    }

    /**
     * write a 16bit short in BigEndian
     * @param s
     * @throws java.io.IOException
     */
    public void writeShortBE(int s) throws IOException {
        outputStream.writeShort(s);
    }

    /**
     * write a 16bit short in LittleEndian
     * @param s
     * @throws java.io.IOException
     */
    public void writeShortLE(int s) throws IOException {
        outputStream.writeByte(s);
        outputStream.writeByte(s >> 8);
    }

    /**
     * write a 32bit int in BigEndian
     * @param i
     * @throws java.io.IOException
     */
    public void writeIntBE(int i) throws IOException {
        outputStream.writeInt(i);
    }

    /**
     * write a 32bit int in  LittleEndian
     * @param i
     * @throws java.io.IOException
     */
    public void writeIntLE(int i) throws IOException {
        outputStream.writeByte(i);
        outputStream.writeByte(i >> 8);
        outputStream.writeByte(i >> 16);
        outputStream.writeByte(i >> 24);
    }

    /**
     * write a 64bit long in BigEndian
     * @param l
     * @throws java.io.IOException
     */
    public void writeLongBE(long l) throws IOException {
        outputStream.writeLong(l);
    }

    /**
     * write a 64bit long in LittleEndian
     * @param l
     * @throws java.io.IOException
     */
    public void writeLongLE(long l) throws IOException {
        outputStream.writeByte((byte) (l));
        outputStream.writeByte((byte) (l >> 8));
        outputStream.writeByte((byte) (l >> 16));
        outputStream.writeByte((byte) (l >> 24));
        outputStream.writeByte((byte) (l >> 32));
        outputStream.writeByte((byte) (l >> 40));
        outputStream.writeByte((byte) (l >> 48));
        outputStream.writeByte((byte) (l >> 56));
    }
    
    /**
     * Write a float in big endian
     * @param f Float value
     * @throws IOException 
     */
    public void writeFloatBE(float f) throws IOException {
        outputStream.writeFloat(f);
    }
    
    /**
     * Write a float in little endian
     * @param f Float value
     * @throws IOException 
     */
    public void writeFloatLE(float f) throws IOException {
        this.writeIntLE(Float.floatToIntBits(f));
    }

    /**
     * write a 64bit double in BigEndian
     * @param d
     * @throws java.io.IOException
     */
    public void writeDoubleBE(double d) throws IOException {
        outputStream.writeDouble(d);
    }

    /**
     * write a 64bit double in LittleEndian
     * @param d
     * @throws java.io.IOException
     */
    public void writeDoubleLE(double d) throws IOException {
        this.writeLongLE(Double.doubleToLongBits(d));
    }
}
