/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.ndarray;

import java.time.LocalDateTime;

/**
 *
 * @author Yaqiang Wang
 */
public class ArrayStructure extends Array {

    private Object arrayObject;
    
    public ArrayStructure(int[] shape) {
        super(shape);
        this.datatype = DataType.STRUCTURE;
    }

    protected ArrayStructure(Index index) {
        super(index);
        this.datatype = DataType.STRUCTURE;
    }

    /**
     * Get array object - NetCDF array object
     *
     * @return Array object
     */
    public Object getArrayObject() {
        return this.arrayObject;
    }

    /**
     * Set array object
     *
     * @param value Array object
     */
    public void setArrayObject(Object value) {
        this.arrayObject = value;
    }

    /**
     * DO NOT USE, throws UnsupportedOperationException
     */
    public Array copy() {
        throw new UnsupportedOperationException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public double getDouble(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setDouble(Index i, double value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public float getFloat(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setFloat(Index i, float value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public long getLong(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setLong(Index i, long value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public int getInt(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setInt(Index i, int value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public short getShort(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setShort(Index i, short value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public byte getByte(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setByte(Index i, byte value) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public boolean getBoolean(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setBoolean(Index i, boolean value) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public String getString(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setString(Index i, String value) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public Complex getComplex(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * not legal, throw ForbiddenConversionException
     */
    public void setComplex(Index i, Complex value) {
        throw new ForbiddenConversionException();
    }

    public LocalDateTime getDate(Index i) { throw new ForbiddenConversionException(); }

    public void setDate(Index i, LocalDateTime value) { throw new ForbiddenConversionException(); }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public char getChar(Index i) {
        throw new ForbiddenConversionException();
    }

    /**
     * DO NOT USE, throw ForbiddenConversionException
     */
    public void setChar(Index i, char value) {
        throw new ForbiddenConversionException();
    }

    // trusted, assumes that individual dimension lengths have been checked
    // package private : mostly for iterators
    public double getDouble(int index) {
        throw new ForbiddenConversionException();
    }

    public void setDouble(int index, double value) {
        throw new ForbiddenConversionException();
    }

    public float getFloat(int index) {
        throw new ForbiddenConversionException();
    }

    public void setFloat(int index, float value) {
        throw new ForbiddenConversionException();
    }

    public long getLong(int index) {
        throw new ForbiddenConversionException();
    }

    public void setLong(int index, long value) {
        throw new ForbiddenConversionException();
    }

    public int getInt(int index) {
        throw new ForbiddenConversionException();
    }

    public void setInt(int index, int value) {
        throw new ForbiddenConversionException();
    }

    public short getShort(int index) {
        throw new ForbiddenConversionException();
    }

    public void setShort(int index, short value) {
        throw new ForbiddenConversionException();
    }

    public byte getByte(int index) {
        throw new ForbiddenConversionException();
    }

    public void setByte(int index, byte value) {
        throw new ForbiddenConversionException();
    }

    public char getChar(int index) {
        throw new ForbiddenConversionException();
    }

    public void setChar(int index, char value) {
        throw new ForbiddenConversionException();
    }

    public boolean getBoolean(int index) {
        throw new ForbiddenConversionException();
    }

    public void setBoolean(int index, boolean value) {
        throw new ForbiddenConversionException();
    }

    public String getString(int index) {
        throw new ForbiddenConversionException();
    }

    public void setString(int index, String value) {
        throw new ForbiddenConversionException();
    }

    public Complex getComplex(int index) {
        throw new ForbiddenConversionException();
    }

    public void setComplex(int index, Complex value) {
        throw new ForbiddenConversionException();
    }

    public LocalDateTime getDate(int index) { throw new ForbiddenConversionException(); }

    public void setDate(int index, LocalDateTime value) { throw new ForbiddenConversionException(); }

    @Override
    public Class getElementType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getStorage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void copyFrom1DJavaArray(IndexIterator arg0, Object arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void copyTo1DJavaArray(IndexIterator arg0, Object arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Array createView(Index arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getObject(Index arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setObject(Index arg0, Object arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getObject(int arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setObject(int arg0, Object arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return this.arrayObject.toString();
    }
}
