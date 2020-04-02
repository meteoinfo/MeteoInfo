package org.meteoinfo.global.util;

import java.time.*;
import java.time.chrono.ChronoPeriod;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class JDateUtil {

    /**
     * Convert LocalDate to Date object.
     *
     * @param localDate The LocalDate object.
     * @return Date object.
     */
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert LocalDateTime to Date object.
     *
     * @param localDateTime The LocalDateTime object.
     * @return Date object.
     */
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert Date to LocalDate object.
     *
     * @param date The Date object.
     * @return LocalDate object.
     */
    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Convert Date to LocalDateTime object.
     *
     * @param date The Date object.
     * @return LocalDateTime object.
     */
    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Convert milli seconds to LocalDateTime object.
     *
     * @param ms The milli seconds.
     * @return LocalDateTime object.
     */
    public static LocalDateTime asLocalDateTime(long ms) {
        return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Convert OA date to date
     *
     * @param oaDate OA date
     * @return Date
     */
    public static LocalDateTime fromOADate(double oaDate) {
        long t = (long) BigDecimalUtil.mul(oaDate, 1000000);
        Instant instant = Instant.ofEpochMilli(t);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Convert LocalDateTime to milli seconds
     * @param ldt Local date time
     * @return Milli seconds
     */
    public static long asMilliSeconds(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Convert LocalDateTime to OA date
     *
     * @param ldt Local date time
     * @return OA date
     */
    public static double toOADate(LocalDateTime ldt) {
        double oaDate = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        oaDate = BigDecimalUtil.div(oaDate, 1000000);

        return oaDate;
    }

    /**
     * Get days of a month
     *
     * @param year The year
     * @param month The month
     * @return The days in the month
     */
    public static int getDaysOfMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return ym.lengthOfMonth();
    }

    /**
     * Get days of a year
     *
     * @param year The year
     * @return The days in the year
     */
    public static int getDaysOfYear(int year) {
        YearMonth ym = YearMonth.of(year, 1);
        return ym.lengthOfYear();
    }

    /**
     * Get time values - Time delta values of base date
     *
     * @param times Time list
     * @param baseDate Base date
     * @param tDelta Time delta type - days/hours/...
     * @return The time delta values
     */
    public static List<Integer> getTimeDeltaValues(List<LocalDateTime> times, LocalDateTime baseDate, String tDelta) {
        List<Integer> values = new ArrayList<>();
        int value;
        for (int i = 0; i < times.size(); i++) {
            if (tDelta.equalsIgnoreCase("hours")) {
                value = (int) Duration.between(baseDate, times.get(i)).toHours();
                values.add(value);
            } else if (tDelta.equalsIgnoreCase("days")) {
                value = (int) Period.between(baseDate.toLocalDate(), times.get(i).toLocalDate()).getDays();
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
    public static int getTimeDeltaValue(LocalDateTime t, LocalDateTime baseDate, String tDelta) {
        int value = 0;
        if (tDelta.equalsIgnoreCase("hours")) {
            value = (int) Duration.between(baseDate, t).toHours();
        } else if (tDelta.equalsIgnoreCase("days")) {
            value = (int) Period.between(baseDate.toLocalDate(), t.toLocalDate()).getDays();
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
    public static int getDays(LocalDateTime t, LocalDateTime baseDate) {
        return Period.between(baseDate.toLocalDate(), t.toLocalDate()).getDays();
    }

    /**
     * Get hours difference between two dates
     *
     * @param t The time
     * @param baseDate Base date
     * @return The time delta value
     */
    public static int getHours(LocalDateTime t, LocalDateTime baseDate) {
        return (int) Duration.between(baseDate, t).toHours();
    }

    /**
     * Date equals
     *
     * @param a Date a
     * @param b Date b
     * @return If equals
     */
    public static boolean equals(LocalDateTime a, LocalDateTime b) {
        return a.equals(b);
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
        LocalDate ldt = LocalDate.of(year, month, day);
        int doy = ldt.getDayOfYear();
        return doy;
    }

    /**
     * Convert day of year to date
     *
     * @param year Year
     * @param doy Day of year
     * @return The date
     */
    public static LocalDate doy2date(int year, int doy) {
        return LocalDate.ofYearDay(year, doy);
    }

    /**
     * Get period type from string
     *
     * @param p Period type string
     * @return PeriodType
     */
    public static ChronoUnit getPeriodType(String p) {
        ChronoUnit pt = ChronoUnit.DAYS;
        switch (p) {
            case "H":
                pt = ChronoUnit.HOURS;
                break;
            case "M":
                pt = ChronoUnit.MINUTES;
                break;
            case "S":
                pt = ChronoUnit.SECONDS;
                break;
            case "m":
                pt = ChronoUnit.MONTHS;
                break;
            case "Y":
                pt = ChronoUnit.YEARS;
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
    public static TemporalAmount getPeriod(String pStr) {
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

        TemporalAmount pe;
        switch (p) {
            case "H":
                pe = Duration.ofHours(n);
                break;
            case "T":
            case "Min":
                pe = Duration.ofMinutes(n);
                break;
            case "S":
                pe = Duration.ofSeconds(n);
                break;
            case "D":
                pe = Period.ofDays(n);
                break;
            case "W":
                pe = Period.ofWeeks(n);
                break;
            case "M":
                pe = Period.ofMonths(n);
                break;
            case "Y":
                pe = Period.ofYears(n);
                break;
            default:
                pe = Duration.ofSeconds(n);
                break;
        }

        return pe;
    }

    /**
     * Get Chrono unit
     * @param ta The Temporal amount
     * @return Chrono unit
     */
    public static ChronoUnit getChronoUnit(TemporalAmount ta) {
        ChronoUnit cu = ChronoUnit.HOURS;
        if (ta instanceof Period) {
            Period period = (Period)ta;
            if (period.getYears() > 0)
                cu = ChronoUnit.YEARS;
            else if (period.getMonths() > 0)
                cu = ChronoUnit.MONTHS;
            else
                cu = ChronoUnit.DAYS;
        } else {
            Duration duration = (Duration)ta;
            if (duration.toHours() > 0)
                cu = ChronoUnit.HOURS;
            else if (duration.toMinutes() > 0)
                cu = ChronoUnit.MINUTES;
            else if (duration.getSeconds() > 0)
                cu = ChronoUnit.SECONDS;
            else if (duration.toMillis() > 0)
                cu = ChronoUnit.MILLIS;
            else
                cu = ChronoUnit.MICROS;
        }

        return cu;
    }

    /**
     * Get date format string
     *
     * @param p Period
     * @return Date format string
     */
    public static String getDateFormat(TemporalAmount p) {
        String df = "yyyy-MM-dd";
        ChronoUnit cp = getChronoUnit(p);
        switch (cp) {
            case HOURS:
                df = "yyyy-MM-dd HH";
                break;
            case MINUTES:
                df = "yyyy-MM-dd HH:mm";
                break;
            case SECONDS:
                df = "yyyy-MM-dd HH:mm:ss";
                break;
        }

        return df;
    }

    /**
     * Get date time from string
     *
     * @param dts Date time string
     * @return DateTime
     */
    public static LocalDateTime getDateTime(String dts) {
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

        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    /**
     * Get date time from string
     *
     * @param dts Date time string
     * @return DateTime
     */
    public static LocalDateTime getDateTime_(String dts) {
        DateTimeFormatter dtf;
        if (dts.contains(":")) {
            dtf = TypeUtils.getDateTimeFormatter(dts);
        } else {
            dtf = TypeUtils.getDateFormatter(dts);
        }
        LocalDateTime dt = LocalDateTime.parse(dts, dtf);
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
    public static List<LocalDateTime> getDateTimes(LocalDateTime start, LocalDateTime end, TemporalAmount p) {
        List<LocalDateTime> dts = new ArrayList<>();
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
    public static List<LocalDateTime> getDateTimes(LocalDateTime start, int tNum, TemporalAmount p) {
        List<LocalDateTime> dts = new ArrayList<>();
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
    public static List<LocalDateTime> getDateTimes(int tNum, LocalDateTime end, TemporalAmount p) {
        List<LocalDateTime> dts = new ArrayList<>();
        for (int i = 0; i < tNum; i++) {
            dts.add(end);
            end = end.minus(p);
        }

        return dts;
    }

    /**
     * Parse string to LocalDateTime
     * @param dtStr The string
     * @param formatter DateTimeFormatter
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dtStr, DateTimeFormatter formatter) {
        TemporalAccessor ta = formatter.parse(dtStr);
        if (ta.isSupported(ChronoField.HOUR_OF_DAY))
            return LocalDateTime.from(ta);
        else
            return (LocalDate.from(ta)).atStartOfDay();
    }
}
