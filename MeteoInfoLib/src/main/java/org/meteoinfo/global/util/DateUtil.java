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
package org.meteoinfo.global.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author yaqiang
 */
public class DateUtil {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Add days to a date
     *
     * @param sDate Start date
     * @param days Days
     * @return Added date
     */
    public static Date addDays(Date sDate, float days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(sDate);
        int intDays = (int) days;
        cal.add(Calendar.DAY_OF_YEAR, intDays);
        int hours = (int) ((days - intDays) * 24);
        cal.add(Calendar.HOUR, hours);

        return cal.getTime();
    }

    /**
     * Get days of a month
     *
     * @param year The year
     * @param month The month
     * @return The days in the month
     */
    public static int getDaysInMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get time values - Time delta values of base date
     *
     * @param times Time list
     * @param baseDate Base date
     * @param tDelta Time delta type - days/hours/...
     * @return The time delta values
     */
    public static List<Integer> getTimeDeltaValues(List<Date> times, Date baseDate, String tDelta) {
        List<Integer> values = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        long sl = cal.getTimeInMillis();
        long el, delta;
        int value;
        for (int i = 0; i < times.size(); i++) {
            cal.setTime(times.get(i));
            el = cal.getTimeInMillis();
            delta = el - sl;
            if (tDelta.equalsIgnoreCase("hours")) {
                value = (int) (delta / (60 * 60 * 1000));
                values.add(value);
            } else if (tDelta.equalsIgnoreCase("days")) {
                value = (int) (delta / (24 * 60 * 60 * 1000));
                values.add(value);
            }
        }

        return values;
    }

    /**
     * Get time value - Time delta value of base date
     *
     * @param t The time
     * @param baseDate Base date
     * @param tDelta Time delta type - days/hours/...
     * @return The time delta value
     */
    public static int getTimeDeltaValue(Date t, Date baseDate, String tDelta) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        long sl = cal.getTimeInMillis();
        long el, delta;
        int value = 0;
        cal.setTime(t);
        el = cal.getTimeInMillis();
        delta = el - sl;
        if (tDelta.equalsIgnoreCase("hours")) {
            value = (int) (delta / (60 * 60 * 1000));
        } else if (tDelta.equalsIgnoreCase("days")) {
            value = (int) (delta / (24 * 60 * 60 * 1000));
        }

        return value;
    }

    /**
     * Get days difference between two dates
     *
     * @param t The time
     * @param baseDate Base date
     * @return The time delta value
     */
    public static int getDays(Date t, Date baseDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        long sl = cal.getTimeInMillis();
        long el, delta;
        cal.setTime(t);
        el = cal.getTimeInMillis();
        delta = el - sl;
        int value = (int) (delta / (24 * 60 * 60 * 1000));

        return value;
    }

    /**
     * Get hours difference between two dates
     *
     * @param t The time
     * @param baseDate Base date
     * @return The time delta value
     */
    public static int getHours(Date t, Date baseDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        long sl = cal.getTimeInMillis();
        long el, delta;
        cal.setTime(t);
        el = cal.getTimeInMillis();
        delta = el - sl;
        int value = (int) (delta / (60 * 60 * 1000));

        return value;
    }

    /**
     * Convert OA date to date
     *
     * @param oaDate OA date
     * @return Date
     */
    public static Date fromOADate(double oaDate) {
        Date date = new Date();
        //long t = (long)((oaDate - 25569) * 24 * 3600 * 1000);
        //long t = (long) (oaDate * 1000000);
        long t = (long) BigDecimalUtil.mul(oaDate, 1000000);
        date.setTime(t);
        return date;
    }

    /**
     * Convert date to OA date
     *
     * @param date Date
     * @return OA date
     */
    public static double toOADate(Date date) {
        double oaDate = date.getTime();
        //oaDate = oaDate / (24 * 3600 * 1000) + 25569;
        //oaDate = oaDate / 1000000;
        oaDate = BigDecimalUtil.div(oaDate, 1000000);

        return oaDate;
    }

    /**
     * Date equals
     *
     * @param a Date a
     * @param b Date b
     * @return If equals
     */
    public static boolean equals(Date a, Date b) {
        if (a.getTime() == b.getTime()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get day of year
     *
     * @param year Year
     * @param month Month
     * @param day Day
     * @return Day of year
     */
    public static int dayOfYear(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        int doy = cal.get(Calendar.DAY_OF_YEAR);
        return doy;
    }

    /**
     * Convert day of year to date
     *
     * @param year Year
     * @param doy Day of year
     * @return The date
     */
    public static Date doy2date(int year, int doy) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, doy);
        return cal.getTime();
    }

    /**
     * Get period type from string
     *
     * @param p Period type string
     * @return PeriodType
     */
    public static PeriodType getPeriodType(String p) {
        PeriodType pt = PeriodType.days();
        switch (p) {
            case "H":
                pt = PeriodType.hours();
                break;
            case "M":
                pt = PeriodType.minutes();
                break;
            case "S":
                pt = PeriodType.seconds();
                break;
            case "m":
                pt = PeriodType.months();
                break;
            case "Y":
                pt = PeriodType.years();
                break;
        }

        return pt;
    }

    /**
     * Get period from string
     *
     * @param pStr Period string
     * @return Period
     */
    public static ReadablePeriod getPeriod(String pStr) {
        String p;
        int n = 1;
        int idx = 0;
        for (int i = 0; i < pStr.length(); i++) {
            if (Character.isLetter(pStr.charAt(i))){
                break;
            }
            idx += 1;
        }
        if (idx == 0) {
            p = pStr;
        } else {
            p = pStr.substring(idx);
            n = Integer.parseInt(pStr.substring(0, idx));
        }

        ReadablePeriod pe;
        switch (p) {
            case "H":
                pe = Hours.hours(n);
                break;
            case "T":
            case "Min":
                pe = Minutes.minutes(n);
                break;
            case "S":
                pe = Seconds.seconds(n);
                break;
            case "D":
                pe = Days.days(n);
                break;
            case "W":
                pe = Weeks.weeks(n);
                break;
            case "M":
                pe = Months.months(n);
                break;
            case "Y":
                pe = Years.years(n);
                break;
            default:
                pe = new Period();
                break;
        }

        return pe;
    }

    /**
     * Get date format string
     *
     * @param p Period
     * @return Date format string
     */
    public static String getDateFormat(ReadablePeriod p) {
        String df = "yyyy-MM-dd";
        if (p instanceof Hours) {
            df = "yyyy-MM-dd HH";
        } else if (p instanceof Minutes) {
            df = "yyyy-MM-dd HH:mm";
        } else if (p instanceof Seconds) {
            df = "yyyy-MM-dd HH:mm:ss";
        }

        return df;
    }

    /**
     * Get date time from string
     *
     * @param dts Date time string
     * @return DateTime
     */
    public static DateTime getDateTime(String dts) {
        int year, month, day;
        String dateStr = dts;
        String timeStr = null;
        if (dts.contains(":")) {
            String[] v = dts.split("\\s+");
            dateStr = v[0].trim();
            timeStr = v[1].trim();
        }
        if (dateStr.contains("/")) {
            String[] ymd = dateStr.split("/");
            month = Integer.parseInt(ymd[0]);
            day = Integer.parseInt(ymd[1]);
            year = Integer.parseInt(ymd[2]);
        } else if (dateStr.contains("-")) {
            String[] ymd = dateStr.split("-");
            month = Integer.parseInt(ymd[1]);
            day = Integer.parseInt(ymd[2]);
            year = Integer.parseInt(ymd[0]);
        } else {
            year = Integer.parseInt(dateStr.substring(0, 4));
            month = Integer.parseInt(dateStr.substring(4, 6));
            day = Integer.parseInt(dateStr.substring(6));
        }
        int hour = 0, minute = 0, second = 0;        
        if (timeStr != null) {            
            String[] hms = timeStr.split(":");
            hour = Integer.parseInt(hms[0]);
            minute = Integer.parseInt(hms[1]);
            second = hms.length == 3 ? Integer.parseInt(hms[2]) : 0;            
        }

        return new DateTime(year, month, day, hour, minute, second);
    }

    /**
     * Get date time from string
     *
     * @param dts Date time string
     * @return DateTime
     */
    public static DateTime getDateTime_(String dts) {
        DateTimeFormatter dtf;
        if (dts.contains(":")) {
            dtf = TypeUtils.getDateTimeFormatter(dts);
        } else {
            dtf = TypeUtils.getDateFormatter(dts);
        }
        DateTime dt = dtf.parseDateTime(dts);
        return dt;
    }

    /**
     * Get date time list
     *
     * @param start Start date time
     * @param end End date time
     * @param p Peroid
     * @return Date time list
     */
    public static List<DateTime> getDateTimes(DateTime start, DateTime end, ReadablePeriod p) {
        List<DateTime> dts = new ArrayList<>();
        while (!start.isAfter(end)) {
            dts.add(start);
            start = start.plus(p);
        }

        return dts;
    }

    /**
     * Get date time list
     *
     * @param start Start date time
     * @param tNum Date time number
     * @param p Peroid
     * @return Date time list
     */
    public static List<DateTime> getDateTimes(DateTime start, int tNum, ReadablePeriod p) {
        List<DateTime> dts = new ArrayList<>();
        for (int i = 0; i < tNum; i++) {
            dts.add(start);
            start = start.plus(p);
        }

        return dts;
    }

    /**
     * Get date time list
     *
     * @param end End date time
     * @param tNum Date time number
     * @param p Peroid
     * @return Date time list
     */
    public static List<DateTime> getDateTimes(int tNum, DateTime end, ReadablePeriod p) {
        List<DateTime> dts = new ArrayList<>();
        for (int i = 0; i < tNum; i++) {
            dts.add(end);
            end = end.minus(p);
        }

        return dts;
    }
    // </editor-fold>
}
