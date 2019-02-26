/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.DateUtil;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class DateTimeIndex extends Index<DateTime> {    
    // <editor-fold desc="Variables">
    ReadablePeriod period;
    ReadablePeriod resamplePeriod;
    //DateTimeFormatter dtFormatter;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public DateTimeIndex(){
        this.format = "yyyy-MM-dd";
        this.dataType = DataType.OBJECT;
    }
    
    /**
     * Constructor
     * @param data Data
     */
    public DateTimeIndex(Array data){
        this(ArrayMath.asList(data));
    }
    
    /**
     * Constructor
     * @param data Data list
     */
    public DateTimeIndex(List data){
        this(data, "Index");
    }
    
    /**
     * Constructor
     * @param data Data list
     * @param name Name
     */
    public DateTimeIndex(List data, String name){
        this();
        this.name = name;
        if (data.get(0) instanceof Date) {
            this.data = new ArrayList<>();
            for (Date d : (List<Date>)data) {
                this.data.add(new DateTime(d));
            }
        } else {
            this.data = data;
        }
        this.updateFormat();
    }
    
    /**
     * Constructor
     * @param start Start time
     * @param end End time
     * @param freq Frequent
     */
    public DateTimeIndex(String start, String end, String freq) {
        this();
        DateTime sdt = DateUtil.getDateTime(start);
        DateTime edt = DateUtil.getDateTime(end);
        period = DateUtil.getPeriod(freq);
        this.setFormat(DateUtil.getDateFormat(period));
        this.data = DateUtil.getDateTimes(sdt, edt, period);
    }
    
    /**
     * Constructor
     * @param start Start time
     * @param tNum Date time number
     * @param freq Frequent
     */
    public DateTimeIndex(String start, int tNum, String freq) {
        this();
        DateTime sdt = DateUtil.getDateTime(start);
        period = DateUtil.getPeriod(freq);
        this.setFormat(DateUtil.getDateFormat(period));
        this.data = DateUtil.getDateTimes(sdt, tNum, period);
    }

    /**
     * Constructor
     * @param tNum Time number
     * @param end End time
     * @param freq Frequent
     */
    public DateTimeIndex(int tNum, String end, String freq) {
        this();
        DateTime edt = DateUtil.getDateTime(end);
        period = DateUtil.getPeriod(freq);
        this.setFormat(DateUtil.getDateFormat(period));
        this.data = DateUtil.getDateTimes(tNum, edt, period);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    public ReadablePeriod getPeriod(){
        return this.period;
    }
    
    /**
     * Set period
     * @param value Period
     */
    public void setPeriod(ReadablePeriod value) {
        this.period = value;
        this.setFormat(DateUtil.getDateFormat(value));
    }
    
    /**
     * Get resample period
     * @return Resample period
     */
    public ReadablePeriod getResamplePeriod(){
        return this.resamplePeriod == null ? this.period : this.resamplePeriod;
    }
    
    /**
     * Set resample period
     * @param value Resample period
     */
    public void setResamplPeriod(ReadablePeriod value){
        this.resamplePeriod = value;
    }
    
    /**
     * Set string format
     * @param value String format
     */
    @Override
    public void setFormat(String value){
        super.setFormat(value);
        //this.dtFormatter = DateTimeFormat.forPattern(format);
    }
    
    /**
     * Get Name format
     * @return 
     */
    @Override
    public String getNameFormat() {
        String str = ((DateTime)this.data.get(0)).toString(this.format);
        return "%" + String.valueOf(str.length()) + "s";
    }
    
//    /**
//     * Get date time formatter
//     * @return Date time formatter
//     */
//    public DateTimeFormatter getDateTimeFormatter(){
//        return this.dtFormatter;
//    }
//    
//    /**
//     * Set date time formatter
//     * @param value Date time formatter
//     */
//    public void setDateTimeFormatter(DateTimeFormatter value){
//        this.dtFormatter = value;
//    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Index of
     * @param d DateTime
     * @return Index
     */
    public int indexOf(DateTime d){
        return this.data.indexOf(d);
    }
    
    private DateTime toDateTime(Object d){
        DateTime dt = null;
        if (d instanceof DateTime) {
            dt = (DateTime)d;
        } else if (d instanceof Date) {
            dt = new DateTime(d);
        } else if (d instanceof java.sql.Timestamp) {
            dt = new DateTime(d);
        } else if (d instanceof String) {
            dt = DateUtil.getDateTime((String)d);
        }
        
        return dt;
    }
    
    /**
     * Index of
     * @param d Date
     * @return Index
     */
    public int indexOf(Date d){
        DateTime dt = new DateTime(d);
        return this.data.indexOf(dt);
    }
    
    /**
     * Index of
     * @param d Date string
     * @return Index
     */
    public int indexOf(String d){
        DateTime dt = DateUtil.getDateTime(d);
        return this.data.indexOf(dt);
    }
    
    /**
     * Index of
     * @param ds Date list
     * @return Index list
     */
    @Override
    public List<Integer> indexOf(List ds){
        List<Integer> r = new ArrayList<>();
        if (ds.get(0) instanceof Date){
            for (Object d : ds){
                r.add(indexOf((Date)d));
            }
        } else if (ds.get(0) instanceof DateTime) {
            for (Object d : ds){
                r.add(indexOf((DateTime)d));
            }
        } else if (ds.get(0) instanceof String) {
            for (Object d : ds){
                r.add(indexOf((String)d));
            }
        }
        
        return r;
    }
    
    /**
     * Get indices
     * @param labels Labels
     * @return Indices
     */
    @Override
    public Object[] getIndices(List<Object> labels) {
        if (labels.get(0) instanceof DateTime){
            return super.getIndices(labels);
        } else {
            List<Object> dts = new ArrayList<>();
            for (Object label : labels){
                dts.add(toDateTime(label));
            }
            return super.getIndices(dts);
        }        
    }
    
    /**
     * Get indices
     * @param label Label
     * @return Indices
     */
    @Override
    public Object[] getIndices(Object label) {
        if (label instanceof DateTime){
            return super.getIndices(label);
        } else {
            DateTime dt = toDateTime(label);
            return super.getIndices(dt);
        }        
    }
    
    /**
     * Sub index
     * @return Index
     */
    @Override
    public DateTimeIndex subIndex(){
        DateTimeIndex r = new DateTimeIndex(this.data);
        r.format = this.format;
        //r.setDateTimeFormatter(dtFormatter);
        return r;
    }
    
    /**
     * Sub index
     * @param idx Index list
     * @return Index
     */
    @Override
    public DateTimeIndex subIndex(List<Integer> idx){
        DateTimeIndex r = new DateTimeIndex();
        for (int i : idx)
            r.add(this.data.get(i));
        //r.setDateTimeFormatter(dtFormatter);
        r.format = this.format;
        return r;
    }
    
    /**
     * Sub index
     * @param start Start index
     * @param end End index
     * @param step Step
     * @return Index
     */
    @Override
    public DateTimeIndex subIndex(int start, int end, int step) {
        List rv = new ArrayList<>();
        for (int i = start; i < end; i+=step){
            rv.add(this.data.get(i));
        }
        DateTimeIndex r = new DateTimeIndex(rv);
        //r.setDateTimeFormatter(dtFormatter);
        r.format = this.format;
        return r;
    }
    
    /**
     * Get date data
     * @return Date data
     */
    public List<Date> getDateValues(){
        List<Date> vs = new ArrayList<>();
        for (DateTime dt : (List<DateTime>)this.data){
            vs.add(dt.toDate());
        }
        return vs;
    }
    
    /**
     * Update format
     */
    @Override
    public void updateFormat(){
        int n = Math.min(10, data.size());
        String ff = "yyyyMMddHHmmSS";
        String str;
        int idx = 8;
        for (int i = 0; i < n; i++) {
            str = ((DateTime) data.get(i)).toString(ff);
            str = DataConvert.removeTail0(str);
            idx = Math.max(idx, str.length());
        }
        switch (idx) {
            case 8:
                ff = "yyyy-MM-dd";
                break;
            case 9:
            case 10:
                ff = "yyyy-MM-dd HH";
                break;
            case 11:
            case 12:
                ff = "yyyy-MM-dd HH:mm";
                break;
            case 13:
            case 14:
                ff = "yyyy-MM-dd HH:mm:SS";
                break;
        }
        this.setFormat(ff);
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("DateTimeIndex([");
        for (int i = 0; i < this.size(); i++){
            sb.append("'");
            sb.append(toString(i));
            sb.append("'");
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
    @Override
    public String toString(int idx) {
        return ((DateTime)this.data.get(idx)).toString(this.format);
    }
    
    /**
     * Convert i_th index to string
     * @param idx Index i
     * @param format Format string
     * @return String
     */
    @Override
    public String toString(int idx, String format) {
        return ((DateTime)this.data.get(idx)).toString(format);
    }
    
    @Override
    public Object clone() {
        List ndata = new ArrayList<>(this.data);
        DateTimeIndex r = new DateTimeIndex(ndata, this.name);
        r.format = this.format;
        r.period = this.period;
        r.resamplePeriod = this.resamplePeriod;
        
        return r;
    }
    // </editor-fold>
}
