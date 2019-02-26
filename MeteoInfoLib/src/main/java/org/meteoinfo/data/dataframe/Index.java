/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.joda.time.DateTime;
import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.global.MIMath;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 * @param <V> Index data type
 */
public class Index<V> implements Iterable<V>{
    // <editor-fold desc="Variables">
    protected List<V> data;
    protected String format = "%4s";
    protected String name = "Index";
    protected DataType dataType = DataType.STRING;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Index(){
        data = new ArrayList<>();
    }
    
    /**
     * Constructor
     * @param array Index array
     * @param name Index name
     */
    public Index(Array array, String name) {
        this();
        data = (List<V>)ArrayMath.asList(array);
        this.name = name;
        this.updateFormat();
    }
    
    /**
     * Constructor
     * @param array Index array
     */
    public Index(Array array) {
        this();
        data = (List<V>)ArrayMath.asList(array);
        this.updateFormat();
    }
    
    /**
     * Constructor
     * @param size Index size
     */
    public Index(int size) {
        this();
        for (Integer i = 0; i < size; i++){
            data.add((V)i);
        }
        this.updateFormat();
    }
    
    /**
     * Constructor
     * @param data Index data
     * @param name Index name
     */
    public Index(List data, String name){
        this();
        this.data = data;
        this.name = name;
        this.updateFormat();
    }
    
    /**
     * Constructor
     * @param data Index data
     */
    public Index(List data){
        this();
        this.data = data;
        this.updateFormat();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get data
     * @return Values
     */
    public List<V> getValues(){
        return this.data;
    }
    
    /**
     * Get data
     * @return Data
     */
    public List<V> getData(){
        return this.data;
    }
    
    /**
     * Set data
     * @param value Data
     */
    public void setData(List<V> value){
        this.data = value;
        this.updateFormat();
    }
    
    @Override
    public Iterator iterator() {
        return this.data.iterator();
    }
    
    /**
     * Get data size
     * @return Index size
     */
    public int size(){
        return data.size();
    }
    
    /**
     * Get string format
     * @return String format
     */
    public String getFormat(){
        return this.format;
    }
    
    /**
     * Get Name format
     * @return 
     */
    public String getNameFormat() {
        return format.substring(0, format.length() - 1) + "s";
    }
    
    /**
     * Set string format
     * @param value String format
     */
    public void setFormat(String value){
        this.format = value;
    }
    
    /**
     * Get name
     * @return Name
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Set name
     * @param value Name
     */
    public void setName(String value){
        this.name = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Factory method to create a new index object
     * @param data Values
     * @return Index object
     */
    public static Index factory(List data) {
        if (data.get(0) instanceof DateTime) {
            return new DateTimeIndex(data);
        } else {
            return new Index(data);
        }
    }
    
    /**
     * Factory method to create a new index object
     * @param data Values
     * @return Index object
     */
    public static Index factory(Array data) {
        if (data.getObject(0) instanceof DateTime) {
            return new DateTimeIndex(data);
        } else {
            return new Index(data);
        }
    }
    
    /**
     * Update format
     */
    public void updateFormat(){
        if (data.get(0) instanceof Integer){
            int max = MIMath.getMinMaxInt(data)[1];
            int len = String.valueOf(max).length();
            this.format = "%" + String.valueOf(len) + "s";
            this.dataType = DataType.INT;
        } else if (data.get(0) instanceof String) {    //String
            int len = 0;
            for (String s : (List<String>)this.data){
                if (len < s.length())
                    len = s.length();
            }
            this.format = "%" + String.valueOf(len) + "s";
            this.dataType = DataType.STRING;
        }
    }
    
    /**
     * Add a value
     * @param v Value
     */
    public void add(V v) {
        this.data.add(v);
    }
    
    /**
     * Add a value
     * @param i Index
     * @param v Value
     */
    public void add(int i, V v){
        this.data.add(i, v);
    }
    
    /**
     * Append another index
     * @param idx Index
     * @return Appended index
     */
    public Index append(Index idx) {
        Index index = new Index(new ArrayList<>(this.data));
        index.setFormat(this.format);
        index.setName(this.name);
        index.data.addAll(idx.data);
        return index;
    }
    
    /**
     * Get a value
     * @param i Index
     * @return Value
     */
    public V get(int i) {
        return this.data.get(i);
    }
    
    /**
     * Set a value
     * @param i Index
     * @param value Value
     */
    public void set(int i, V value) {
        this.data.set(i, value);
    }
    
    /**
     * Get indices
     * @param names Names
     * @return Indices
     */
    public Integer[] indices(final Object[] names) {
        return indices(Arrays.asList(names));
    }

    /**
     * Get indices
     * @param names Names
     * @return Indices
     */
    public Integer[] indices(final List<Object> names) {
        final int size = names.size();
        final Integer[] indices = new Integer[size];
        for (int i = 0; i < size; i++) {
            indices[i] = indexOf(names.get(i));
        }
        return indices;
    }
    
    /**
     * Index of
     * @param v Value
     * @return Index
     */
    public int indexOf(Object v) {
        return this.data.indexOf(v);
    }
    
    /**
     * Index of
     * @param vs Value list
     * @return Index list
     */
    public List<Integer> indexOf(List<V> vs) {
        List<Integer> r = new ArrayList<>();
        for (V v : vs)
            r.add(indexOf(v));
        
        return r;
    }
    
    /**
     * Contains function
     * @param v Value
     * @return Boolean
     */
    public boolean contains(V v) {
        return this.data.contains(v);
    }
    
    /**
     * Sub index
     * @return Index
     */
    public Index subIndex(){
        Index r = new Index(this.data);
        r.setFormat(format);
        return r;
    }
    
    /**
     * Sub index
     * @param idx Index list
     * @return Index
     */
    public Index subIndex(List<Integer> idx){
        Index r = new Index();
        for (int i : idx)
            r.add(this.data.get(i));
        r.setFormat(format);
        return r;
    }
    
    /**
     * Sub index
     * @param start Start index
     * @param end End index
     * @param step Step
     * @return Index
     */
    public Index subIndex(int start, int end, int step) {
        List rv = new ArrayList<>();
        for (int i = start; i < end; i+=step){
            rv.add(this.data.get(i));
        }
        Index r = new Index(rv);
        r.setFormat(format);
        return r;
    }
    
    /**
     * Get indices
     * @param labels Labels
     * @return Indices
     */
    public Object[] getIndices(Array labels) {
        return getIndices(ArrayMath.asList(labels));
    }
    
    /**
     * Get indices
     * @param labels Labels
     * @return Indices
     */
    public Object[] getIndices(List<Object> labels) {
        List<Integer> r = new ArrayList<>();
        List<Object> rIndex = new ArrayList<>();
        List<Integer> rData = new ArrayList<>();
        List<Object> rrIndex = new ArrayList<>();
        Object[] rr;
        List<Integer> r1;
        List<Object> rIndex1;
        for (Object l : labels){
            rr = getIndices(l);
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
     * @param label Label
     * @return Indices
     */
    public Object[] getIndices(Object label) {
        List<Integer> r = new ArrayList<>();
        List<Object> rIndex = new ArrayList<>();
        int idx = data.indexOf(label);
        if (idx >= 0) {
            r.add(idx);
            rIndex.add(label);
        } else {
            r.add(-1);
            rIndex.add(label);
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
    public Array fillKeyList(Array data, List<Integer> rrdata){
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
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Index([");
        for (int i = 0; i < this.size(); i++){
            sb.append(this.toString_Index(i));
            if (i < 100) {
                if (i < this.size() - 1)
                    sb.append(", ");
                else 
                    break;
            } else {                
                sb.append(", ...");
                break;
            }
        }
        sb.append("])");
        
        return sb.toString();
    }
    
    /**
     * Convert i_th index to string
     * @param idx Index i
     * @return String
     */
    public String toString_Index(int idx) {
        String s = String.valueOf(this.data.get(idx));
        switch (this.dataType) {
            case STRING:
                s = "'" + s + "'";
                break;
        }
        return s;
    }
    
    /**
     * Convert i_th index to string
     * @param idx Index i
     * @return String
     */
    public String toString(int idx) {
        return String.format(this.format, this.data.get(idx));
    }
    
    /**
     * Convert i_th index to string
     * @param idx Index i
     * @param format Format string
     * @return String
     */
    public String toString(int idx, String format) {
        return String.format(format, this.data.get(idx));
    }
    
    @Override
    public Object clone() {
        List ndata = new ArrayList<>(this.data);
        Index r = new Index(ndata, this.name);
        r.format = this.format;
        return r;
    }
    // </editor-fold>    
}
