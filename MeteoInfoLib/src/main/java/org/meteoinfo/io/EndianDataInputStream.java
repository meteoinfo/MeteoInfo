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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author yaqiang - come from OpenJump
 */
public class EndianDataInputStream {
    private final java.io.DataInputStream inputStream;
    private final byte[] workSpace = new byte[8]; //chars are 16 bits, so we always quash the 1st 8 bits

    /** Creates new EndianDataInputStream
     * @param in */
    public EndianDataInputStream(java.io.InputStream in) {
        inputStream = new DataInputStream(new BufferedInputStream(in));
    }

    /** close the strea*/
    public void close() throws IOException {
        inputStream.close();
    }

    /** read a byte in BigEndian - the same as LE because its only 1 byt*/
    public byte readByteBE() throws IOException {
        return inputStream.readByte();
    }

    /** read a byte in LittleEndian - the same as BE because its only 1 byt*/
    public byte readByteLE() throws IOException {
        return inputStream.readByte();
    }
    
     /** read a byte in LittleEndian - the same as BE because its only 1 byte*/
    public void readByteLEnum(byte[] b) throws IOException {
        inputStream.readFully(b);
    }
    

    /** read a byte in BigEndian - the same as LE because its only 1 byte.  returns int as per java.io.DataStream*/
    public int readUnsignedByteBE() throws IOException {
        return inputStream.readUnsignedByte();
    }

    /** read a byte in LittleEndian - the same as BE because its only 1 byte.  returns int as per java.io.DataStream*/
    public int readUnsignedByteLE() throws IOException {
        return inputStream.readUnsignedByte();
    }

    /** read a 16bit short in BE*/
    public short readShortBE() throws IOException {
        return inputStream.readShort();
    }

    /** read a 16bit short in LE*/
    public short readShortLE() throws IOException {
        inputStream.readFully(workSpace, 0, 2);

        return (short) (((workSpace[1] & 0xff) << 8) | (workSpace[0] & 0xff));
    }

    /** read a 32bit int in BE*/
    public int readIntBE() throws IOException {
        return inputStream.readInt();
    }

    /** read a 32bit int in LE*/
    public int readIntLE() throws IOException {
        inputStream.readFully(workSpace, 0, 4);

        return ((workSpace[3] & 0xff) << 24) | ((workSpace[2] & 0xff) << 16) |
        ((workSpace[1] & 0xff) << 8) | (workSpace[0] & 0xff);
    }

    /** read a 64bit long  in BE*/
    public long readLongBE() throws IOException {
        return inputStream.readLong();
    }

    /** read a 64bit long  in LE*/
    public long readLongLE() throws IOException {
        inputStream.readFully(workSpace, 0, 8);

        return ((long) (workSpace[7] & 0xff) << 56) |
        ((long) (workSpace[6] & 0xff) << 48) |
        ((long) (workSpace[5] & 0xff) << 40) |
        ((long) (workSpace[4] & 0xff) << 32) |
        ((long) (workSpace[3] & 0xff) << 24) |
        ((long) (workSpace[2] & 0xff) << 16) |
        ((long) (workSpace[1] & 0xff) << 8) | ((long) (workSpace[0] & 0xff));
    }

    /** read a 64bit double  in BE*/
    public double readDoubleBE() throws IOException {
        return inputStream.readDouble();
    }

    /** read a 64bit double  in LE*/
    public double readDoubleLE() throws IOException {
        long l;

        inputStream.readFully(workSpace, 0, 8);
        l = ((long) (workSpace[7] & 0xff) << 56) |
            ((long) (workSpace[6] & 0xff) << 48) |
            ((long) (workSpace[5] & 0xff) << 40) |
            ((long) (workSpace[4] & 0xff) << 32) |
            ((long) (workSpace[3] & 0xff) << 24) |
            ((long) (workSpace[2] & 0xff) << 16) |
            ((long) (workSpace[1] & 0xff) << 8) |
            ((long) (workSpace[0] & 0xff));

        return Double.longBitsToDouble(l);
    }

    /** skip ahead in the stream
     * @param num number of bytes to read ahead
     */
    public int skipBytes(int num) throws IOException {
        return inputStream.skipBytes(num);
    }
}
