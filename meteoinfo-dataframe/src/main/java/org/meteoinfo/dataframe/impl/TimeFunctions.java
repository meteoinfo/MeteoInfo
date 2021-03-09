/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.dataframe.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 *
 * @author Yaqiang Wang
 */
public class TimeFunctions {
    
    public static TimeFunction factory(String tStr){
        switch(tStr.toLowerCase()) {
            case "month_of_year":
                return new MonthOfYear();
            case "season_of_year":
                return new SeasonOfYear();
            case "day_of_week":
                return new DayOfWeek();
            case "hour_of_day":
                return new HourOfDay();
        }
        return null;
    }
    
    public static class MonthOfYear<I, O> implements TimeFunction<LocalDateTime, String> {
        @Override
        public String apply(LocalDateTime value) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM").withLocale(Locale.ENGLISH);
            return dtf.format(value);
        }
    }
    
    public static class SeasonOfYear<I, O> implements TimeFunction<LocalDateTime, String> {
        @Override
        public String apply(LocalDateTime value) {
            int month = value.getMonthValue();
            String season;
            switch (month) {
                case 1:
                case 2:
                case 12:
                    season = "Winter";
                    break;
                case 3:
                case 4:
                case 5:
                    season = "Spring";
                    break;
                case 6:
                case 7:
                case 8:
                    season = "Summer";
                    break;
                case 9:
                case 10:
                case 11:
                    season = "Autumn";
                    break;
                default:
                    season = "Null";
                    break;
            }
            return season;
        }
    }
    
    public static class DayOfWeek<I, O> implements TimeFunction<LocalDateTime, String> {
        @Override
        public String apply(LocalDateTime value) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE").withLocale(Locale.ENGLISH);
            return dtf.format(value);
        }
    }
    
    public static class HourOfDay<I, O> implements TimeFunction<LocalDateTime, String> {
        @Override
        public String apply(LocalDateTime value) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH").withLocale(Locale.ENGLISH);
            return dtf.format(value);
        }
    }
}
