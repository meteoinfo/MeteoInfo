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
package org.meteoinfo.table;

import org.meteoinfo.ndarray.DataType;

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
     public Field(String fName, DataType type, int fLen, int fNumDec){
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
     public Field(String fName, DataType type) {
         this.setColumnName(fName);
         this.setDataType(type);
         switch (type) {
             case STRING:
                 fieldLen = 255;
                 break;
             case DATE:
                 fieldLen = 8;
                 break;
             case FLOAT:
                 fieldLen = 18;
                 fieldNumDec = 6;
                 break;
             case DOUBLE:
                 fieldLen = 18;
                 fieldNumDec = 9;
                 break;
             case INT:
                 fieldLen = 11;
                 break;
             case BOOLEAN:
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
                 this.setDataType(DataType.DATE);
                 break;
             case 'L':
                 this.setDataType(DataType.BOOLEAN);
                 break;
             case 'F':
                 this.setDataType(DataType.FLOAT);
                 break;
             case 'N':
                 if (fNumDec == 0 && fLen <= 11){
                     this.setDataType(DataType.INT);
                 }
                 else
                     this.setDataType(DataType.DOUBLE);
                 break;
             default:
                 this.setDataType(DataType.STRING);
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
             case BOOLEAN:
                 return 'L';
             case DATE:
                 return 'D';
             case FLOAT:
                 return 'F';
             case DOUBLE:
             case INT:
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
         return this.getDataType().isNumeric();
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
             case FLOAT:
                 //_decimalCount = (byte)40;  // Singles  -3.402823E+38 to 3.402823E+38
                 //_length = (byte)40;
                 this.fieldLen = 18;
                 this.fieldNumDec = 6;
                 break;
             case DOUBLE:
                 //_decimalCount = (byte)255; // Doubles -1.79769313486232E+308 to 1.79769313486232E+308
                 //_length = (byte)255;
                 this.fieldLen = 18;
                 this.fieldNumDec = 9;
                 break;
             case INT:
                 this.fieldNumDec = 0;
                 this.fieldLen = 11;
                 break;
             case STRING:
                 this.fieldNumDec = 0;
                 this.fieldLen = 255;
                 break;
             case DATE:
                 this.fieldNumDec = 0;
                 this.fieldLen = 8;
                 break;
         }
     }
     // </editor-fold>
 }
