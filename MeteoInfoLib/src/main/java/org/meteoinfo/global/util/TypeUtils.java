/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.global.util;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * Ported from Tablesaw
 */
public class TypeUtils {
    // These Strings will convert to true booleans
    public static final List<String> TRUE_STRINGS =
            Arrays.asList("T", "t", "Y", "y", "TRUE", "true", "True", "1");
    // A more restricted set of 'true' strings that is used for column type detection
    public static final List<String> TRUE_STRINGS_FOR_DETECTION =
            Arrays.asList("T", "t", "Y", "y", "TRUE", "true", "True");
    // These Strings will convert to false booleans
    public static final List<String> FALSE_STRINGS =
            Arrays.asList("F", "f", "N", "n", "FALSE", "false", "False", "0");
    // A more restricted set of 'false' strings that is used for column type detection
    public static final List<String> FALSE_STRINGS_FOR_DETECTION =
            Arrays.asList("F", "f", "N", "n", "FALSE", "false", "False");

    // Formats that we accept in parsing dates from strings
    // TODO: Add more types, especially dates with month names spelled-out fully.
    private static final DateTimeFormatter dtf1 = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter dtf2 = DateTimeFormat.forPattern("MM/dd/yyyy");
    private static final DateTimeFormatter dtf3 = DateTimeFormat.forPattern("MM-dd-yyyy");
    private static final DateTimeFormatter dtf4 = DateTimeFormat.forPattern("MM.dd.yyyy");
    private static final DateTimeFormatter dtf5 = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dtf6 = DateTimeFormat.forPattern("yyyy/MM/dd");
    private static final DateTimeFormatter dtf7 = DateTimeFormat.forPattern("dd/MMM/yyyy");
    private static final DateTimeFormatter dtf8 = DateTimeFormat.forPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter dtf9 = DateTimeFormat.forPattern("M/d/yyyy");
    private static final DateTimeFormatter dtf10 = DateTimeFormat.forPattern("M/d/yy");
    private static final DateTimeFormatter dtf11 = DateTimeFormat.forPattern("MMM/dd/yyyy");
    private static final DateTimeFormatter dtf12 = DateTimeFormat.forPattern("MMM-dd-yyyy");
    private static final DateTimeFormatter dtf13 = DateTimeFormat.forPattern("MMM/dd/yy");
    private static final DateTimeFormatter dtf14 = DateTimeFormat.forPattern("MMM-dd-yy");
    private static final DateTimeFormatter dtf15 = DateTimeFormat.forPattern("MMM/dd/yyyy");
    private static final DateTimeFormatter dtf16 = DateTimeFormat.forPattern("MMM/d/yyyy");
    private static final DateTimeFormatter dtf17 = DateTimeFormat.forPattern("MMM-dd-yy");
    private static final DateTimeFormatter dtf18 = DateTimeFormat.forPattern("MMM dd, yyyy");
    private static final DateTimeFormatter dtf19 = DateTimeFormat.forPattern("MMM d, yyyy");
    // A formatter that handles all the date formats defined above
    public static final DateTimeFormatter DATE_FORMATTER =
            new DateTimeFormatterBuilder()
                    .append(dtf1)
                    .append(dtf2)
                    .append(dtf3)
                    .append(dtf4)
                    .append(dtf5)
                    .append(dtf6)
                    .append(dtf7)
                    .append(dtf8)
                    .append(dtf9)
                    .append(dtf10)
                    .append(dtf11)
                    .append(dtf12)
                    .append(dtf13)
                    .append(dtf14)
                    .append(dtf15)
                    .append(dtf16)
                    .append(dtf17)
                    .append(dtf18)
                    .append(dtf19)
                    .toFormatter();
    private static final DateTimeFormatter dtTimef0 =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");     // 2014-07-09 13:03:44
    private static final DateTimeFormatter dtTimef2 =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S");   // 2014-07-09 13:03:44.7 (as above, but without leading 0 in millis
    private static final DateTimeFormatter dtTimef4 =
            DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm");       // 09-Jul-2014 13:03
    private static final DateTimeFormatter dtTimef5 = ISODateTimeFormat.dateTime();
    private static final DateTimeFormatter dtTimef6;                // ISO, with millis appended
    private static final DateTimeFormatter dtTimef7 =               //  7/9/14 9:04
            DateTimeFormat.forPattern("M/d/yy H:mm");
    private static final DateTimeFormatter dtTimef8 =
            DateTimeFormat.forPattern("M/d/yyyy h:mm:ss a");      //  7/9/2014 9:04:55 PM

    static {
        dtTimef6 = new DateTimeFormatterBuilder()
                .append(ISODateTimeFormat.dateTime())
                .appendLiteral('.')
                .appendPattern("SSS")
                .toFormatter();
    }

    // A formatter that handles date time formats defined above
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            new DateTimeFormatterBuilder()
                    .append(dtTimef7)
                    .append(dtTimef8)
                    .append(dtTimef2)
                    .append(dtTimef4)
                    .append(dtTimef0)
                    .append(dtTimef5)
                    .append(dtTimef6)
                    .toFormatter();
    private static final DateTimeFormatter timef1 = DateTimeFormat.forPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter timef2 = DateTimeFormat.forPattern("hh:mm:ss a");
    private static final DateTimeFormatter timef3 = DateTimeFormat.forPattern("h:mm:ss a");
    private static final DateTimeFormatter timef4 = ISODateTimeFormat.dateTime();
    private static final DateTimeFormatter timef5 = DateTimeFormat.forPattern("hh:mm a");
    private static final DateTimeFormatter timef6 = DateTimeFormat.forPattern("h:mm a");
    // A formatter that handles time formats defined above used for type detection.
    // It is more conservative than the converter
    public static final DateTimeFormatter TIME_DETECTION_FORMATTER =
            new DateTimeFormatterBuilder()
                    .append(timef5)                    
                    .append(timef2)
                    .append(timef3)
                    .append(timef1)
                    .append(timef4)
                    .append(timef6)
                    //  .append(timef7)
                    .toFormatter();
    private static final DateTimeFormatter timef7 = DateTimeFormat.forPattern("HHmm");
    // A formatter that handles time formats defined above
    public static final DateTimeFormatter TIME_FORMATTER =
            new DateTimeFormatterBuilder()
                    .append(timef5)
                    .append(timef2)
                    .append(timef3)
                    .append(timef1)
                    .append(timef4)
                    .append(timef6)
                    .append(timef7)
                    .toFormatter();
    /**
     * Strings representing missing values in, for example, a CSV file that is being imported
     */
    private static final String missingInd1 = "NaN";
    private static final String missingInd2 = "*";
    private static final String missingInd3 = "NA";
    private static final String missingInd4 = "null";
    public static final ImmutableList<String> MISSING_INDICATORS = ImmutableList.of(
            missingInd1,
            missingInd2,
            missingInd3,
            missingInd4
    );
    /**
     * List of formatters for use in code that selects the correct one for a given Date string
     */
    private static ImmutableList<DateTimeFormatter> dateFormatters = ImmutableList.of(
            dtf1,
            dtf2,
            dtf3,
            dtf4,
            dtf5,
            dtf6,
            dtf7,
            dtf8,
            dtf9,
            dtf10,
            dtf11,
            dtf12,
            dtf13,
            dtf14,
            dtf15,
            dtf16,
            dtf17,
            dtf18,
            dtf19
    );
    /**
     * List of formatters for use in code that selects the correct one for a given DateTime string
     */
    private static ImmutableList<DateTimeFormatter> dateTimeFormatters = ImmutableList.of(
            dtTimef0,
            dtTimef2,
            dtTimef4,
            dtTimef5,
            dtTimef6,
            dtTimef7,
            dtTimef8
    );
    /**
     * List of formatters for use in code that selects the correct one for a given Time string
     */
    private static ImmutableList<DateTimeFormatter> timeFormatters = ImmutableList.of(
            timef1,
            timef2,
            timef3,
            timef4,
            timef5,
            timef6
    );

    /**
     * Private constructor to prevent instantiation
     */
    private TypeUtils() {
    }

    /**
     * Returns the first DateTimeFormatter to parse the string, which represents a DATE
     * <p>
     * It's intended to be called at the start of a large formatting job so that it picks the write format and is not
     * called again. This is an optimization, because the older version, which will try multiple formatters was too
     * slow for large data sets.
     */
    public static DateTimeFormatter getDateFormatter(String dateValue) {

        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                formatter.parseDateTime(dateValue);
                return formatter;
            } catch (DateTimeParseException e) {
                // ignore;
            }
        }
        return DATE_FORMATTER;
    }

    /**
     * Returns the first DateTimeFormatter to parse the string, which represents a DATE_TIME
     * <p>
     * It's intended to be called at the start of a large formatting job so that it picks the write format and is not
     * called again. This is an optimization, because the older version, which will try multiple formatters was too
     * slow for large data sets.
     */
    public static DateTimeFormatter getDateTimeFormatter(String dateTimeValue) {
        for (DateTimeFormatter formatter : dateTimeFormatters) {
            if (canParse(formatter, dateTimeValue)) {
                return formatter;
            }
        }
        if (canParse(DATE_FORMATTER, dateTimeValue)) {
            return DATE_FORMATTER;
        }
        if (canParse(DATE_TIME_FORMATTER, dateTimeValue)) {
            return DATE_TIME_FORMATTER;
        }
        throw new IllegalArgumentException("Could not find datetime parser for " + dateTimeValue);
    }

    private static boolean canParse(DateTimeFormatter formatter, String dateTimeValue) {
        try {
            formatter.parseDateTime(dateTimeValue);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns the first DateTimeFormatter to parse the string, which represents a TIME
     * <p>
     * It's intended to be called at the start of a large formatting job so that it picks the write format and is not
     * called again. This is an optimization, because the older version, which will try multiple formatters was too
     * slow for large data sets.
     */
    public static DateTimeFormatter getTimeFormatter(String timeValue) {
        for (DateTimeFormatter formatter : timeFormatters) {
            try {
                formatter.parseDateTime(timeValue);
                return formatter;
            } catch (DateTimeParseException e) {
                // ignore;
            }
        }
        return DATE_FORMATTER;
    }

}
