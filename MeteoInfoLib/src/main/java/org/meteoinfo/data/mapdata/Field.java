 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.mapdata;

import org.meteoinfo.table.DataColumn;
import org.meteoinfo.data.DataTypes;

/**
 * The field in attribute table of shape file
 *
 * @author yaqiang
 */
public class Field extends DataColumn {
    // <editor-fold desc="Variables">

    //private String fieldName;
    //private char fieldType;
    private int fieldLen;
    private int fieldNumDec = 0;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     * @param fName Field name
     * @param type Data type
     * @param fLen Field length
     * @param fNumDec Field decimal number
     */
    public Field(String fName, DataTypes type, int fLen, int fNumDec){
        this.setColumnName(fName);
        this.setDataType(type);
        this.fieldLen = fLen;
        this.fieldNumDec = fNumDec;
    }
    
    /**
     * Constructor
     * @param fName Field name
     * @param type Field data type
     */
    public Field(String fName, DataTypes type) {
        this.setColumnName(fName);
        this.setDataType(type);
        switch (type) {
            case String:
                fieldLen = 255;
                break;
            case Date:
                fieldLen = 8;
                break;
            case Float:
                fieldLen = 18;
                fieldNumDec = 6;
                break;
            case Double:
                fieldLen = 18;
                fieldNumDec = 9;
                break;
            case Decimal:
                fieldLen = 18;
                fieldNumDec = 9;
                break;
            case Integer:
                fieldLen = 11;
                break;
            case Boolean:
                fieldLen = 1;
                break;
        }
    }

    /**
     * Constructor
     *
     * @param fName Field name
     * @param fType Field type
     * @param fLen Field length
     * @param fNumDec Field decimal number
     */
    public Field(String fName, char fType, int fLen, int fNumDec) {
        this.setColumnName(fName);
        //this.fieldType = fType;
        this.fieldLen = fLen;
        this.fieldNumDec = fNumDec;
        switch (fType) {
            case 'D':   //Date
            case 'T':
                this.setDataType(DataTypes.Date);
                break;
            case 'L':
                this.setDataType(DataTypes.Boolean);
                break;
            case 'F':
                this.setDataType(DataTypes.Float);
                break;
            case 'N':
                if (fNumDec == 0 && fLen <= 11){
                    this.setDataType(DataTypes.Integer);
                }
                else
                    this.setDataType(DataTypes.Double);
                break;
            default:
                this.setDataType(DataTypes.String);
                break;
        }
    }

    /**
     * Constructor
     *
     * @param inColumn In data column
     */
    public Field(DataColumn inColumn) {
        this(inColumn.getColumnName(), inColumn.getDataType());
        this.setup_decimalCount();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get field length
     *
     * @return Field length
     */
    public int getLength() {
        return this.fieldLen;
    }

    /**
     * Set field length
     *
     * @param len Field length
     */
    public void setLength(int len) {
        this.fieldLen = len;
    }

    /**
     * Get field decimal count
     *
     * @return Field decimal count
     */
    public int getDecimalCount() {
        return this.fieldNumDec;
    }

    /**
     * Set field decimal count
     *
     * @param value Field decimal count
     */
    public void setDecimalCount(int value) {
        this.fieldNumDec = value;
    }

    /**
     * This is the single character dBase code. Only some of these are supported
     * with ESRI. C - Character (Chars, Strings, objects - as ToString(), and
     * structs - as ) D - Date (DateTime) T - Time (DateTime) N - Number (Short,
     * Integer, Long, Float, Double, byte) L - Logic (True-False, Yes-No) F -
     * Float B - Double
     *
     * @return Type character
     */
    public char getTypeCharacter() {
        switch (this.getDataType()) {
            case Boolean:
                return 'L';
            case Date:
                return 'D';
            case Float:
                return 'F';
            case Double:
            case Decimal:
            case Integer:
                return 'N';
            default:
                return 'C';
        }
    }
    
    /**
     * If the field is numeric
     * @return Boolean
     */
    public boolean isNumeric(){
        switch (this.getDataType()){
            case Integer:
            case Float:
            case Double:
            case Decimal:
                return true;
            default:
                return false;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Methods">   

    /**
     * Internal method that decides an appropriate decimal count, given a data
     * column
     */
    private void setup_decimalCount() {
        // Going this way, we want a large enough decimal count to hold any of the possible numeric values.
        // We will try to make the length large enough to hold any values, but some doubles simply will be 
        // too large to be stored in this format, so we will throw exceptions if that happens later.

        // These sizes represent the "maximized" length and decimal counts that will be shrunk in order
        // to fit the data before saving.
        switch (this.getDataType()) {
            case Float:
                //_decimalCount = (byte)40;  // Singles  -3.402823E+38 to 3.402823E+38
                //_length = (byte)40;
                this.fieldLen = 18;
                this.fieldNumDec = 6;
                break;
            case Double:
                //_decimalCount = (byte)255; // Doubles -1.79769313486232E+308 to 1.79769313486232E+308
                //_length = (byte)255;
                this.fieldLen = 18;
                this.fieldNumDec = 9;
                break;
            case Decimal:
                this.fieldNumDec = 9; // Decimals -79228162514264337593543950335 to 79228162514264337593543950335
                this.fieldLen = 18;
                break;
            case Integer:
                this.fieldNumDec = 0;
                this.fieldLen = 11;
                break;
            case String:
                this.fieldNumDec = 0;
                this.fieldLen = 255;
                break;
            case Date:
                this.fieldNumDec = 0;
                this.fieldLen = 8;
                break;
        }
    }
    // </editor-fold>
}
