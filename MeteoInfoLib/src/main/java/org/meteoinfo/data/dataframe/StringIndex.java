/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.util.List;
import org.meteoinfo.data.ArrayUtil;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class StringIndex extends Index<String> {
    
    /**
     * Construction
     * @param data The index data
     */
    public StringIndex(List data) {
        this.data = data;
        this.updateFormat();
    }  
    
    /**
     * Equal operation
     * @param v The value
     * @return Boolean array
     */
    public Array equal(String v) {
        Array r = ArrayUtil.factory(DataType.BOOLEAN, new int[]{this.size()});
        int i = 0;
        for (String d : this.data) {
            r.setBoolean(i, d.equals(v));
            i += 1;
        }
        
        return r;
    }
}
