/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class IntIndex extends Index<Integer>{
    
    /**
     * Construction
     * @param data The index data
     */
    public IntIndex(List data) {
        this.data = data;
        this.updateFormat();
    }        
    
    /**
     * Constructor
     * @param size Index size
     */
    public IntIndex(int size) {
        this.data = new ArrayList<>();
        for (Integer i = 0; i < size; i++){
            data.add(i);
        }
        this.updateFormat();
    }
    
    /**
     * Equal operation
     * @param v The value
     * @return Boolean array
     */
    public Array equal(Number v) {
        int vi = v.intValue();
        Array r = Array.factory(DataType.BOOLEAN, new int[]{this.size()});
        int i = 0;
        for (int d : this.data) {
            r.setBoolean(i, d == vi);
            i += 1;
        }
        
        return r;
    }
}
