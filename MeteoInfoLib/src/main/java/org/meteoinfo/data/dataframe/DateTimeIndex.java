/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

import org.meteoinfo.common.util.JDateUtil;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.math.ArrayMath;

/**
 *
 * @author Yaqiang Wang
 */
public class DateTimeIndex extends Index<LocalDateTime> {
    // <editor-fold desc="Variables">
    TemporalAmount period;
    TemporalAmount resamplePeriod;
    DateTimeFormatter dtFormatter;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public DateTimeIndex(){
        this.format = "yyyy-MM-dd";
        this.dtFormatter = DateTimeFormatter.ofPattern(this.format);
        this.dataType = DataType.DATE;
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
                this.data.add(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
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
        LocalDateTime sdt = JDateUtil.getDateTime(start);
        LocalDateTime edt = JDateUtil.getDateTime(end);
        period = JDateUtil.getPeriod(freq);
        this.setFormat(JDateUtil.getDateFormat(period));
        this.data = JDateUtil.getDateTimes(sdt, edt, period);
    }
    
    /**
     * Constructor
     * @param start Start time
     * @param tNum Date time number
     * @param freq Frequent
     */
    public DateTimeIndex(String start, int tNum, String freq) {
        this();
        LocalDateTime sdt = JDateUtil.getDateTime(start);
        period = JDateUtil.getPeriod(freq);
        this.setFormat(JDateUtil.getDateFormat(period));
        this.data = JDateUtil.getDateTimes(sdt, tNum, period);
    }

    /**
     * Constructor
     * @param tNum Time number
     * @param end End time
     * @param freq Frequent
     */
    public DateTimeIndex(int tNum, String end, String freq) {
        this();
        LocalDateTime edt = JDateUtil.getDateTime(end);
        period = JDateUtil.getPeriod(freq);
        this.setFormat(JDateUtil.getDateFormat(period));
        this.data = JDateUtil.getDateTimes(tNum, edt, period);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    public TemporalAmount getPeriod(){
        return this.period;
    }
    
    /**
     * Set period
     * @param value Period
     */
    public void setPeriod(TemporalAmount value) {
        this.period = value;
        this.setFormat(JDateUtil.getDateFormat(value));
    }
    
    /**
     * Get resample period
     * @return Resample period
     */
    public TemporalAmount getResamplePeriod(){
        return this.resamplePeriod == null ? this.period : this.resamplePeriod;
    }
    
    /**
     * Set resample period
     * @param value Resample period
     */
    public void setResamplPeriod(TemporalAmount value){
        this.resamplePeriod = value;
    }
    
    /**
     * Set string format
     * @param value String format
     */
    @Override
    public void setFormat(String value){
        super.setFormat(value);
        this.dtFormatter = DateTimeFormatter.ofPattern(format);
    }
    
    /**
     * Get Name format
     * @return 
     */
    @Override
    public String getNameFormat() {
        String str = this.dtFormatter.format(this.data.get(0));
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
    public int indexOf(LocalDateTime d){
        return this.data.indexOf(d);
    }
    
    private LocalDateTime toDateTime(Object d){
        LocalDateTime dt = null;
        if (d instanceof LocalDateTime) {
            dt = (LocalDateTime)d;
        } else if (d instanceof Date) {
            dt = ((Date)d).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else if (d instanceof java.sql.Timestamp) {
            dt = ((java.sql.Timestamp)d).toLocalDateTime();
        } else if (d instanceof String) {
            dt = JDateUtil.getDateTime((String)d);
        }
        
        return dt;
    }
    
    /**
     * Index of
     * @param d Date
     * @return Index
     */
    public int indexOf(Date d){
        LocalDateTime dt = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return this.data.indexOf(dt);
    }
    
    /**
     * Index of
     * @param d Date string
     * @return Index
     */
    public int indexOf(String d){
        LocalDateTime dt = JDateUtil.getDateTime(d);
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
        } else if (ds.get(0) instanceof LocalDateTime) {
            for (Object d : ds){
                r.add(indexOf((LocalDateTime)d));
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
        if (labels.get(0) instanceof LocalDateTime){
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
        if (label instanceof LocalDateTime){
            return super.getIndices(label);
        } else {
            LocalDateTime dt = toDateTime(label);
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
        r.setFormat(this.format);
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
        r.setFormat(this.format);
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
        r.setFormat(this.format);
        return r;
    }

    /**
     * Get year index
     * @return Year index
     */
    public Index getYear() {
        List<Integer> years = new ArrayList<>();
        for (LocalDateTime dt : this.data) {
            years.add(dt.getYear());
        }
        return Index.factory(years);
    }
    
    /**
     * Get month index
     * @return Month index
     */
    public Index getMonth() {
        List<Integer> months = new ArrayList<>();
        for (LocalDateTime dt : this.data) {
            months.add(dt.getMonthValue());
        }
        return Index.factory(months);
    }
    
    /**
     * Get day index
     * @return Day index
     */
    public Index getDay() {
        List<Integer> days = new ArrayList<>();
        for (LocalDateTime dt : this.data) {
            days.add(dt.getDayOfMonth());
        }
        return Index.factory(days);
    }
    
    /**
     * Get hour index
     * @return HOur index
     */
    public Index getHour() {
        List<Integer> hours = new ArrayList<>();
        for (LocalDateTime dt : this.data) {
            hours.add(dt.getHour());
        }
        return Index.factory(hours);
    }
    
    /**
     * Get minute index
     * @return Minute index
     */
    public Index getMinute() {
        List<Integer> minutes = new ArrayList<>();
        for (LocalDateTime dt : this.data) {
            minutes.add(dt.getMinute());
        }
        return Index.factory(minutes);
    }
    
    /**
     * Get second index
     * @return Second index
     */
    public Index getSecond() {
        List<Integer> seconds = new ArrayList<>();
        for (LocalDateTime dt : this.data) {
            seconds.add(dt.getSecond());
        }
        return Index.factory(seconds);
    }
    
    /**
     * Update format
     */
    @Override
    public void updateFormat(){
        int n = Math.min(10, data.size());
        String ff = "yyyyMMddHHmmSS";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(ff);
        String str;
        int idx = 8;
        for (int i = 0; i < n; i++) {
            str = dtf.format(this.data.get(i));
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
        return this.dtFormatter.format(this.data.get(idx));
    }
    
    /**
     * Convert i_th index to string
     * @param idx Index i
     * @param format Format string
     * @return String
     */
    @Override
    public String toString(int idx, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(this.data.get(idx));
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
