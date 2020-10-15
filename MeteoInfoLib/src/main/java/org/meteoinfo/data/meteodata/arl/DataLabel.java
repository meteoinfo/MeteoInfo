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
package org.meteoinfo.data.meteodata.arl;

import java.time.LocalDateTime;

/**
 *
 * @author yaqiang
 */
public class DataLabel {
    // <editor-fold desc="Variables">
    private int _year;
    private int _month;
    private int _day;
    private int _hour;
    private int _forecast;
    private int _level;
    private int _grid; 
    private String _varName;
    private int _exponent;
    private double _precision;
    private double _value;
    private LocalDateTime _time = LocalDateTime.now();
    
    //header record does not support grids of more than 999 
    //therefore in those situations the grid number is
    //converted to character to represent the 1000s digit
    //e.g. @(64)=<1000, A(65)=1000, B(66)=2000, etc
    public String IGC = null;
    public boolean XGPT = false;
    
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     * consisting of 50 ASCII bytes of time, variable, and level information for record
     */
    public DataLabel() {
    }

    /// <summary>
    /// Constructor
    /// </summary>
    /// <param name="time">Time</param>
    public DataLabel(LocalDateTime time) {
        _time = time;
        _year = time.getYear();
        _month = (short) time.getMonthValue();
        _day = (short) time.getDayOfMonth();
        _hour = (short) time.getHour();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get year
     * @return Year
     */
    public int getYear(){
        return _year;
    }
    
    /**
     * Set year
     * @param value Year
     */
    public void setYear(int value){
        _year = value;
    }
    
    /**
     * Get month
     * @return Month
     */
    public int getMonth(){
        return _month;
    }
    
    /**
     * Set month
     * @param value Month
     */
    public void setMonth(int value){
        _month = value;
    }
    
    /**
     * Get day
     * @return Day 
     */
    public int getDay(){
        return _day;
    }
    
    /**
     * Set day
     * @param value Day
     */
    public void setDay(int value){
        _day = value;
    }
    
    /**
     * Get hour
     * @return Hour
     */
    public int getHour(){
        return _hour;
    }
    
    /**
     * Set hour
     * @param value Hour
     */
    public void setHour(int value){
        _hour = value;
    }
    
    /**
     * Get forecast
     * @return Forecast
     */
    public int getForecast(){
        return _forecast;
    }
    
    /**
     * Set forecast
     * @param value Forecast
     */
    public void setForecast(int value){
        _forecast = value;
    }
    
    /**
     * Get level
     * @return Level
     */
    public int getLevel(){
        return _level;
    }
    
    /**
     * Set level
     * @param value Level 
     */
    public void setLevel(int value){
        _level = value;
    }
    
    /**
     * Get grid
     * @return Grid
     */
    public int getGrid(){
        return _grid;
    }
    
    /**
     * Set grid
     * @param value Grid
     */
    public void setGrid(int value){
        _grid = value;
    }
    
    /**
     * Get variable name
     * @return Variable name
     */
    public String getVarName(){
        return _varName;
    }
    
    /**
     * Set variable name
     * @param value Variable name
     */
    public void setVarName(String value){
        _varName = value;
    }
    
    /**
     * Get exponent
     * @return exponent
     */
    public int getExponent(){
        return _exponent;
    }
    
    /**
     * Set exponent
     * @param value Exponent
     */
    public void setExponent(int value){
        _exponent = value;
    }
    
    /**
     * Get precision
     * @return Precision
     */
    public double getPrecision(){
        return _precision;
    }
    
    /**
     * Set precision
     * @param value Precision
     */
    public void setPrecision(double value){
        _precision = value;
    }
    
    /**
     * Get value
     * @return Value
     */
    public double getValue(){
        return _value;
    }
    
    /**
     * Set value
     * @param value Value
     */
    public void setValue(double value){
        _value = value;
    }
    
    /**
     * Get time
     *
     * @return Time
     */
    public LocalDateTime getTime() {
        return _time;
    }

    /**
     * Set time
     *
     * @param value Time
     */
    public void setTime(LocalDateTime value) {
        _time = value;
        _year = value.getYear();
        _month = (short) value.getMonthValue();
        _day = (short) value.getDayOfMonth();
        _hour = (short) value.getHour();
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get time value
     * @return Time value
     */
    public LocalDateTime getTimeValue(){
        return LocalDateTime.of(_year, _month, _day, _hour, 0);
    }
    // </editor-fold>
}
