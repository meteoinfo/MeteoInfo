/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import java.text.DecimalFormat;
import org.joda.time.DateTime;
import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.global.DataConvert;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class Column {
    // <editor-fold desc="Variables">
    protected String name;
    protected DataType dataType;
    protected String format;
    protected int formatLen;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Column(){
        this("Column", DataType.OBJECT);
    }
    
    /**
     * Constructor
     * @param name Name
     */
    public Column(String name){
        this(name, DataType.OBJECT);
    }
    
    /**
     * Constructor
     * @param name Name
     * @param dataType Data type
     */
    public Column(String name, DataType dataType) {
        this.name = name;
        this.dataType = dataType;
        this.updateFormat();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
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
    
    /**
     * Get data type
     * @return Data type
     */
    public DataType getDataType(){
        return this.dataType;
    }
    
    /**
     * Set data type
     * @param value Data type
     */
    public void setDataType(DataType value) {
        this.dataType = value;
    }
    
    /**
     * Get format
     * @return Format
     */
    public String getFormat(){
        return this.format;
    }
    
    /**
     * Get Name format
     * @return 
     */
    public String getNameFormat() {
        return "%" + String.valueOf(this.formatLen) + "s";
    }
    
    /**
     * Set format
     * @param value Format 
     */
    public void setFormat(String value){
        this.format = value;
    }
    
    /**
     * Get format length
     * @return Format length
     */
    public int getFormatLen(){
        return this.formatLen;
    }
    
    /**
     * Set format length
     * @param value Format length
     */
    public void setFormatLen(int value) {
        this.formatLen = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Factory method
     * @param name Name
     * @param dtype Data type
     * @return Column
     */
    public static Column factory(String name, DataType dtype){
        return new Column(name, dtype);
    }
    
    /**
     * Factory method
     * @param name Name
     * @param array Data array
     * @return Column
     */
    public static Column factory(String name, Array array){
        DataType dtype = array.getDataType();
        if (dtype == DataType.OBJECT && (array.getObject(0) instanceof DateTime)){
            DateTimeColumn col = new DateTimeColumn(name);
            col.updateFormat(array);
            return col;
        }
        return new Column(name, dtype);
    }
    
    /**
     * Update format
     */
    public void updateFormat(){
        this.format = null;
        switch (this.dataType){
            case FLOAT:
            case DOUBLE:
                this.format = "%f";                
                break;
        }
        this.formatLen = this.name.length();
    }
    
    /**
     * Update format
     * @param data Data array
     */
    public void updateFormat(Array data) {
        this.formatLen = this.name.length();
        switch(this.dataType) {
            case DOUBLE:
            case FLOAT:
                double dmax = ArrayMath.max(data);
                DecimalFormat df = new DecimalFormat("0.0");
                df.setMaximumFractionDigits(6);
                int nf = 1, ci, nn;
                String str;
                for (int i = 0; i < data.getSize(); i++){
                    str = df.format(data.getDouble(i));
                    ci = str.indexOf(".");
                    nn = str.length() - ci - 1;
                    if (nf < nn) {
                        nf = nn;
                        if (nf == 6)
                            break;
                    }
                }
                String smax = df.format(dmax);              
                ci = smax.indexOf(".");
                int len = ci + nf + 2;
                formatLen = Math.max(formatLen, len);
                this.format = "%" + String.valueOf(formatLen) + "." + String.valueOf(nf) + "f";
                break;
            case INT:
                int imax = (int)ArrayMath.max(data);
                smax = Integer.toString(imax);
                formatLen = Math.max(formatLen, smax.length());
                this.format = "%" + String.valueOf(formatLen) + "d";
                break;
            default:                
                String v;
                for (int i = 0; i < data.getSize(); i++){
                    if (data.getObject(i) == null)
                        v = "null";
                    else
                        v = data.getObject(i).toString();
                    if (formatLen < v.length())
                        formatLen = v.length();
                }
                this.format = "%" + String.valueOf(formatLen) + "s";
                break;
        }
    }        
    
    /**
     * Convert input data to current data type
     *
     * @param value Object value
     * @return Result object
     */
    public Object convertTo(Object value) {
        return DataConvert.convertTo(value, this.dataType, this.format);
    }
    
    /**
     * Convert input data to current data type
     * @param s Input string
     * @return Result object
     */
    public Object convertStringTo(String s) {
        return DataConvert.convertStringTo(s, dataType, format);
    }
    
    @Override
    public String toString(){
        return this.name;
    }
    
    /**
     * Convert an object (same datatype with this column) to string
     * @param o
     * @return String
     */
    public String toString(Object o){
        if (format == null)
            return o.toString();
        else
            return String.format(format, o);
    }
    
    /**
     *
     * @return Column
     */
    @Override
    public Object clone() {
        Column col = new Column(this.name, this.dataType);
        col.setFormat(this.format);
        col.setFormatLen(this.formatLen);
        return col;
    }
    // </editor-fold>
}
