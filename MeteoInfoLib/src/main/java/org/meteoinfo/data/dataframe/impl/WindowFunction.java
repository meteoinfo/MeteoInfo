/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe.impl;

import org.meteoinfo.global.util.JDateUtil;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

/**
 *
 * @author Yaqiang Wang
 */
public class WindowFunction implements Function<LocalDateTime, Object>{
    
    TemporalAmount period;
    
    /**
     * Constructor
     * @param period Period
     */
    public WindowFunction(TemporalAmount period){
        this.period = period;
    }
    
    /**
     * Get period
     * @return Period
     */
    public TemporalAmount getPeriod(){
        return this.period;
    }

    @Override
    public Object apply(LocalDateTime value) {
        LocalDateTime ndt = LocalDateTime.now();
        ChronoUnit cu = JDateUtil.getChronoUnit(this.period);
        switch (cu) {
            case SECONDS:
                ndt = LocalDateTime.of(value.getYear(), value.getMonthValue(), value.getDayOfMonth(),
                        value.getHour(), value.getMinute(), 0);
                break;
            case MINUTES:
                ndt = LocalDateTime.of(value.getYear(), value.getMonthValue(), value.getDayOfMonth(),
                        value.getHour(), 0, 0);
                break;
            case HOURS:
                ndt = LocalDateTime.of(value.getYear(), value.getMonthValue(), value.getDayOfMonth(),
                        0, 0, 0);
                break;
            case DAYS:
                ndt = LocalDateTime.of(value.getYear(), value.getMonthValue(), 1,
                        0, 0, 0);
                break;
            case MONTHS:
                ndt = LocalDateTime.of(value.getYear(), 1, 1, 0, 0, 0);
                break;
            case YEARS:
                int n = ((Period)period).getYears();
                ndt = LocalDateTime.of(value.getYear() - n, 1, 1, 0, 0, 0);
        }
        
        while (ndt.isBefore(value)){
            ndt = ndt.plus(period);
        }
        if (ndt.isAfter(value))
            ndt = ndt.minus(period);
        
        return ndt;
    }
    
}
