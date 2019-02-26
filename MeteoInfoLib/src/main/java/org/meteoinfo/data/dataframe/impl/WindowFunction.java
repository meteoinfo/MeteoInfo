/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe.impl;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;
import org.joda.time.Years;

/**
 *
 * @author Yaqiang Wang
 */
public class WindowFunction implements Function<DateTime, Object>{
    
    ReadablePeriod period;
    
    /**
     * Constructor
     * @param period Period
     */
    public WindowFunction(ReadablePeriod period){
        this.period = period;
    }
    
    /**
     * Get period
     * @return Period
     */
    public ReadablePeriod getPeriod(){
        return this.period;
    }

    @Override
    public Object apply(DateTime value) {
        DateTime ndt = new DateTime();
        if (period instanceof Seconds) {
            ndt = new DateTime(value.getYear(), value.getMonthOfYear(), value.getDayOfMonth(),
                value.getHourOfDay(), value.getMinuteOfHour(), 0);            
        } else if (period instanceof Minutes) {
            ndt = new DateTime(value.getYear(), value.getMonthOfYear(), value.getDayOfMonth(),
                value.getHourOfDay(), 0, 0); 
        } else if (period instanceof Hours) {
            ndt = new DateTime(value.getYear(), value.getMonthOfYear(), value.getDayOfMonth(),
                0, 0, 0);            
        } else if (period instanceof Days) {
            ndt = new DateTime(value.getYear(), value.getMonthOfYear(), 1,
                0, 0, 0);            
        } else if (period instanceof Months) {
            ndt = new DateTime(value.getYear(), 1, 1,
                0, 0, 0);            
        } else if (period instanceof Years) {
            int n = ((Years)period).getYears();
            ndt = new DateTime(value.getYear() - n, 1, 1,
                0, 0, 0);            
        }
        
        while (ndt.isBefore(value)){
            ndt = ndt.plus(period);
        }
        if (ndt.isAfter(value))
            ndt = ndt.minus(period);
        
        return ndt;
    }
    
}
