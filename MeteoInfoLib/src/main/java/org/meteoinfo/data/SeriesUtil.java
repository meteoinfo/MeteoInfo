/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ucar.ma2.Array;

/**
 *
 * @author Yaqiang Wang
 */
public class SeriesUtil {
    
    /**
     * Get indices
     * @param index Index array
     * @param labels Labels
     * @return Indices
     */
    public static Object[] getIndices(List<Object> index, Array labels) {
        return getIndices(index, ArrayMath.asList(labels));
    }
    
    /**
     * Get indices
     * @param index Index array
     * @param labels Labels
     * @return Indices
     */
    public static Object[] getIndices(Array index, Array labels) {
        return getIndices(ArrayMath.asList(index), ArrayMath.asList(labels));
    }
    
    /**
     * Get indices
     * @param index Index array
     * @param labels Labels
     * @return Indices
     */
    public static Object[] getIndices(List<Object> index, List<Object> labels) {
        List<Integer> r = new ArrayList<>();
        List<Object> rIndex = new ArrayList<>();
        List<Integer> rData = new ArrayList<>();
        List<Object> rrIndex = new ArrayList<>();
        Object[] rr;
        List<Integer> r1;
        List<Object> rIndex1;
        for (Object l : labels){
            rr = getIndices(index, l);
            r1 = (ArrayList<Integer>)rr[0];
            rIndex1 = (ArrayList<Object>)rr[1];
            if (r1.isEmpty()){
                rData.add(0);
                rrIndex.add(l);
            } else {
                r.addAll(r1);
                rIndex.addAll(rIndex1);
                for (Iterator<Integer> it = r1.iterator(); it.hasNext();) {
                    it.next();
                    rData.add(1);
                    rrIndex.add(l);
                }
            }
        }
        
        return new Object[]{r, rIndex, rData, rrIndex};
    }
    
    /**
     * Get indices
     * @param index Index array
     * @param label Label
     * @return Indices
     */
    public static Object[] getIndices(List<Object> index, Object label) {
        List<Integer> r = new ArrayList<>();
        List<Object> rIndex = new ArrayList<>();
        for (int i = 0; i < index.size(); i++){
            if (index.get(i).equals(label)){
                r.add(i);
                rIndex.add(index.get(i));
            }
        }
        
        return new Object[]{r, rIndex};
    }
    
    /**
     * Sub list by index
     * @param list The list
     * @param index The index
     * @return Result list
     */
    public static List subList(List list, List<Integer> index){
        List r = new ArrayList<>();
        for (int i : index){
            r.add(list.get(i));
        }
        
        return r;
    }
    
//    /**
//     * Fill key list
//     * @param data Valid data array
//     * @param rrdata Result data flags
//     * @return Result data array with same length as key list
//     */
//    public static Array fillKeyList(Array data, List<Integer> rrdata){
//        Array kdata = Array.factory(data.getDataType(), new int[]{rrdata.size()});
//        Object nanObj = null;
//        switch (data.getDataType()){
//            case FLOAT:
//                nanObj = Float.NaN;
//                break;
//            case DOUBLE:
//                nanObj = Double.NaN;
//                break;
//        } 
//        int idx = 0;
//        int i = 0;
//        for (int f : rrdata){
//            if (f == 0)
//                kdata.setObject(i, nanObj);
//            else {
//                kdata.setObject(i, data.getObject(idx));
//                idx += 1;
//            }
//            i += 1;
//        }
//        
//        return kdata;
//    }
    
    /**
     * Fill key list
     * @param data Valid data array
     * @param rrdata Result data flags
     * @return Result data array with same length as key list
     */
    public static Array fillKeyList(Array data, List<Integer> rrdata){
        Array kdata = Array.factory(data.getDataType(), new int[]{rrdata.size()}); 
        int idx = 0;
        int i = 0;
        for (int f : rrdata){
            if (f == 0)
                kdata.setObject(i, Double.NaN);
            else {
                kdata.setObject(i, data.getObject(idx));
                idx += 1;
            }
            i += 1;
        }
        
        return kdata;
    }

}
