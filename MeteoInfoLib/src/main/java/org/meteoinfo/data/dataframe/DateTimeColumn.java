/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import org.joda.time.DateTime;
import org.meteoinfo.global.DataConvert;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class DateTimeColumn extends Column {

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
    }

    @Override
    public void setFormat(String value) {
        this.format = value;
        this.formatLen = Math.max(this.name.length(), this.format.length());
    }

    @Override
    public void setName(String value) {
        this.formatLen = Math.max(this.name.length(), this.format.length());
    }

    @Override
    public String toString(Object o) {
        return toString((DateTime) o);
    }

    /**
     * Convert DateTime object to string
     *
     * @param t DateTime object
     * @return String
     */
    public String toString(DateTime t) {
        return t.toString(format);
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
        String str;
        int idx = 8;
        for (int i = 0; i < n; i++) {
            str = ((DateTime) data.getObject(i)).toString(ff);
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
