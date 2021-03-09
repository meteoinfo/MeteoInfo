/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.dataframe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.meteoinfo.common.DataConvert;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class DateTimeColumn extends Column {

    private DateTimeFormatter dateTimeFormatter;

    /**
     * Constructor
     */
    public DateTimeColumn() {
        this("Column");
    }

    /**
     * Constructor
     *
     * @param name Name
     */
    public DateTimeColumn(String name) {
        this(name, "yyyy-MM-dd");
    }

    /**
     * Constructor
     *
     * @param name Name
     * @param format Format
     */
    public DateTimeColumn(String name, String format) {
        this.name = name;
        this.dataType = DataType.OBJECT;
        this.format = format;
        this.formatLen = Math.max(this.name.length(), this.format.length());
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(this.format);
    }

    @Override
    public void setFormat(String value) {
        this.format = value;
        this.formatLen = Math.max(this.name.length(), this.format.length());
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(this.format);
    }

    @Override
    public void setName(String value) {
        this.formatLen = Math.max(this.name.length(), this.format.length());
    }

    @Override
    public String toString(Object o) {
        return toString((LocalDateTime) o);
    }

    /**
     * Convert DateTime object to string
     *
     * @param t DateTime object
     * @return String
     */
    public String toString(LocalDateTime t) {
        return t.format(this.dateTimeFormatter);
    }

    /**
     * Update format
     *
     * @param data Data array
     */
    @Override
    public void updateFormat(Array data) {
        int n = Math.min(10, (int) data.getSize());
        String ff = "yyyyMMddHHmmSS";
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(this.format);
        String str;
        int idx = 8;
        for (int i = 0; i < n; i++) {
            str = ((LocalDateTime) data.getObject(i)).format(this.dateTimeFormatter);
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
}
