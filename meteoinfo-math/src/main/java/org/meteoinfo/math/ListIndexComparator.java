/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math;

import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class ListIndexComparator implements Comparator<Integer> {
    private final List list;

    public ListIndexComparator(List list)
    {
        this.list = list;
    }

    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            indexes[i] = i; // Autoboxing
        }
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
         // Autounbox from Integer to int to use as array indexes
        if (this.list.get(0) instanceof String){
            return ((String)list.get(index1)).compareTo((String)list.get(index2));
        } else if (this.list.get(0) instanceof Integer) {
            return ((Integer)list.get(index1)).
                    compareTo((Integer)list.get(index2));
        } else if (this.list.get(0) instanceof Float) {
            return ((Float)list.get(index1)).
                    compareTo((Float)list.get(index2));
        } else if (this.list.get(0) instanceof Double) {
            return ((Double)list.get(index1)).
                    compareTo((Double)list.get(index2));
        } else {
            return -1;
        }
    }
}
