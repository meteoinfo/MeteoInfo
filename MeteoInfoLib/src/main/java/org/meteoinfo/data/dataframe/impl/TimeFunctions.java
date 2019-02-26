/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe.impl;

import java.util.Locale;
import org.joda.time.DateTime;

/**
 *
 * @author Yaqiang Wang
 */
public class TimeFunctions {
    
    public static TimeFunction factory(String tStr){
        switch(tStr.toLowerCase()) {
            case "month_of_year":
                return new TimeFunctions.MonthOfYear();
            case "season_of_year":
                return new TimeFunctions.SeasonOfYear();
            case "day_of_week":
                return new TimeFunctions.DayOfWeek();
            case "hour_of_day":
                return new TimeFunctions.HourOfDay();
        }
        return null;
    }
    
    public static class MonthOfYear<I, O> implements TimeFunction<DateTime, String> {
        @Override
        public String apply(DateTime value) {
            return value.toString("MMM", Locale.ENGLISH);
        }
    }
    
    public static class SeasonOfYear<I, O> implements TimeFunction<DateTime, String> {
        @Override
        public String apply(DateTime value) {
            int month = value.getMonthOfYear();
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
    
    public static class DayOfWeek<I, O> implements TimeFunction<DateTime, String> {
        @Override
        public String apply(DateTime value) {
            return value.toString("EEEE", Locale.ENGLISH);
        }
    }
    
    public static class HourOfDay<I, O> implements TimeFunction<DateTime, String> {
        @Override
        public String apply(DateTime value) {
            return value.toString("HH", Locale.ENGLISH);
        }
    }
}
