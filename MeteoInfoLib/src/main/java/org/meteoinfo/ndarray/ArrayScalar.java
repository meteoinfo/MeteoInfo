/*
 * Copyright 1998-2014 University Corporation for Atmospheric Research/Unidata
 *
 *   Portions of this software were developed by the Unidata Program at the
 *   University Corporation for Atmospheric Research.
 *
 *   Access and use of this software shall impose the following obligations
 *   and understandings on the user. The user is granted the right, without
 *   any fee or cost, to use, copy, modify, alter, enhance and distribute
 *   this software, and any derivative works thereof, and its supporting
 *   documentation for any purpose whatsoever, provided that this entire
 *   notice appears in all copies of the software, derivative works and
 *   supporting documentation.  Further, UCAR requests that the user credit
 *   UCAR/Unidata in any publications that result from the use of this
 *   software or in any product that includes this software. The names UCAR
 *   and/or Unidata, however, may not be used in any advertising or publicity
 *   to endorse or promote any products or commercial entity unless specific
 *   written permission is obtained from UCAR/Unidata. The user also
 *   understands that UCAR/Unidata is not obligated to provide the user with
 *   any support, consulting, training or assistance of any kind with regard
 *   to the use, operation and performance of this software nor to provide
 *   the user with any updates, revisions, new versions or "bug fixes."
 *
 *   THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *   IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *   INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *   FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *   NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *   WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.meteoinfo.ndarray;

import javax.annotation.concurrent.Immutable;
import java.time.LocalDateTime;

/**
 * Helper class for StructureDataAscii
 */
@Immutable
public class ArrayScalar extends Array {

    private final Object value;

    public ArrayScalar(Object value, boolean isUnsigned) {
        super(DataType.getType(value.getClass(), isUnsigned), new int[] {});
        this.value = value;
    }

    @Override
    public Class getElementType() {
        return value.getClass();
    }

    @Override
    protected Array createView(Index index) {
        return this;
    }

    @Override
    public Object[] getStorage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void copyFrom1DJavaArray(IndexIterator iter, Object javaArray) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double getDouble(Index ima) {
        return getDouble(0);
    }

    @Override
    public void setDouble(Index ima, double value) {
    }

    @Override
    public float getFloat(Index ima) {
        return getFloat(0);
    }

    @Override
    public void setFloat(Index ima, float value) {
    }

    @Override
    public long getLong(Index ima) {
        return getLong(0);
    }

    @Override
    public void setLong(Index ima, long value) {
    }

    @Override
    public int getInt(Index ima) {
        return getInt(0);
    }

    @Override
    public void setInt(Index ima, int value) {
    }

    @Override
    public short getShort(Index ima) {
        return getShort(0);
    }

    @Override
    public void setShort(Index ima, short value) {
    }

    @Override
    public byte getByte(Index ima) {
        return getByte(0);
    }

    @Override
    public void setByte(Index ima, byte value) {
    }

    @Override
    public char getChar(Index ima) {
        return getChar(0);
    }

    @Override
    public void setChar(Index ima, char value) {
    }

    @Override
    public boolean getBoolean(Index ima) {
        return getBoolean(0);
    }

    @Override
    public void setBoolean(Index ima, boolean value) {
    }

    public String getString(Index index) {
        return getString(0);
    }

    public void setString(Index index, String value) {

    }

    public Complex getComplex(Index index) {
        return getComplex(0);
    }

    public void setComplex(Index index, Complex value) {
    }

    public LocalDateTime getDate(Index i) { return getDate(0); }

    public void setDate(Index i, LocalDateTime value) { }

    @Override
    public Object getObject(Index ima) {
        return value;
    }

    @Override
    public void setObject(Index ima, Object value) {
    }

    @Override
    public double getDouble(int elem) {
        return ((Number) value).doubleValue();
    }

    @Override
    public void setDouble(int elem, double val) {
    }

    @Override
    public float getFloat(int elem) {
        return ((Number) value).floatValue();
    }

    @Override
    public void setFloat(int elem, float val) {
    }

    @Override
    public long getLong(int elem) {
        return ((Number) value).longValue();
    }

    @Override
    public void setLong(int elem, long value) {
    }

    @Override
    public int getInt(int elem) {
        return ((Number) value).intValue();
    }

    @Override
    public void setInt(int elem, int value) {
    }

    @Override
    public short getShort(int elem) {
        return ((Number) value).shortValue();
    }

    @Override
    public void setShort(int elem, short value) {
    }

    @Override
    public byte getByte(int elem) {
        return ((Number) value).byteValue();
    }

    @Override
    public void setByte(int elem, byte value) {
    }

    @Override
    public char getChar(int elem) {
        return (Character) value;
    }

    @Override
    public void setChar(int elem, char value) {
    }

    @Override
    public boolean getBoolean(int elem) {
        return (Boolean) value;
    }

    @Override
    public void setBoolean(int elem, boolean value) {
    }

    public String getString(int index) {
        return (String) value;
    }

    public void setString(int index, String value) {

    }

    public Complex getComplex(int index) {
        return (Complex) value;
    }

    public void setComplex(int index, Complex value) {

    }

    public LocalDateTime getDate(int index) {return (LocalDateTime) value; }

    public void setDate(int index, LocalDateTime value) {}

    @Override
    public Object getObject(int elem) {
        return value;
    }

    @Override
    public void setObject(int elem, Object value) {
    }
}
