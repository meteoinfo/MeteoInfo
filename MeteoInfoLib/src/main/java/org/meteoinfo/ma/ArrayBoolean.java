/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.ma;

import ucar.ma2.Index;

/**
 *
 * @author Yaqiang Wang
 */
public class ArrayBoolean extends ucar.ma2.ArrayBoolean {
    
    public ArrayBoolean(int[] ints) {
        super(ints);
    }
    
    public ArrayBoolean(ucar.ma2.Array a) {        
        super(a.getShape());
        this.storage = (boolean[])a.getStorage();
    }
    
    @Override
    public int getInt(Index i) {
        return storage[i.currentElement()] ? 1 : 0;
    }
    
    @Override
    public short getShort(Index i) {
        return storage[i.currentElement()] ? (short)1 : 0;
    }
    
    @Override
    public long getLong(Index i) {
        return storage[i.currentElement()] ? 1 : 0;
    }
    
    @Override
    public float getFloat(Index i) {
        return storage[i.currentElement()] ? 1 : 0;
    }
    
    @Override
    public double getDouble(Index i) {
        return storage[i.currentElement()] ? 1 : 0;
    }
    
    @Override
    public int getInt(int i) {
        return storage[i] ? 1 : 0;
    }
    
    @Override
    public short getShort(int i) {
        return storage[i] ? (short)1 : 0;
    }
    
    @Override
    public long getLong(int i) {
        return storage[i] ? 1 : 0;
    }
    
    @Override
    public float getFloat(int i) {
        return storage[i] ? 1 : 0;
    }
    
    @Override
    public double getDouble(int i) {
        return storage[i] ? 1 : 0;
    }
    
    @Override
    public void setInt(Index i, int value){
        storage[i.currentElement()] = (value != 0);
    }
    
    @Override
    public void setInt(int i, int value){
        storage[i] = (value != 0);
    }
    
    @Override
    public void setObject(Index i, Object value) {
        boolean v;
        if (value instanceof Boolean)
            v = (Boolean) value;
        else
            v = ((double)value != 0);
        storage[i.currentElement()] = v; 
    }
    
    @Override
    public void setObject(int i, Object value) {
        boolean v;
        if (value instanceof Boolean)
            v = (Boolean) value;
        else
            v = ((Integer)value != 0);
        storage[i] = v; 
    }
    
}
