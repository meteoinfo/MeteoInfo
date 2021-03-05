/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.table.util;

import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.util.GlobalUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class TableUtil {

    /**
     * Get format list
     *
     * @param formatSpec Input format specific string
     * @return Format list
     */
    public static List<String> getFormats(String formatSpec) {
        if (formatSpec == null) {
            return null;
        }

        List<String> formats = new ArrayList<>();
        String[] colFormats = formatSpec.split("%");

        for (String colFormat : colFormats) {
            if (colFormat.isEmpty()) {
                continue;
            }

            if (colFormat.substring(0, 1).equals("{")) {    //Date
                int eidx = colFormat.indexOf("}");
                colFormat = colFormat.substring(1, eidx);
            } else {
                colFormat = "%" + colFormat;
            }
            formats.add(colFormat);
        }

        return formats;
    }

    /**
     * To data type - MeteoInfo
     *
     * @param dt Data type string
     * @return Data type
     */
    public static DataType toDataTypes(String dt) {
        if (dt.contains("%")) {
            dt = dt.split("%")[1];
        }
        switch (dt.toLowerCase()) {
            case "c":
            case "s":
            case "string":
                return DataType.STRING;
            case "i":
            case "int":
                return DataType.INT;
            case "f":
            case "float":
                return DataType.FLOAT;
            case "d":
            case "double":
                return DataType.DOUBLE;
            default:
                if (dt.substring(0, 1).equals("{")) {    //Date
                    return DataType.DATE;
                } else {
                    return DataType.STRING;
                }
        }
    }

    /**
     * Get date format string
     *
     * @param dt Format string
     * @return Date format string
     */
    public static String getDateFormat(String dt) {
        int sidx = dt.indexOf("{");
        int eidx = dt.indexOf("}");
        String formatStr = dt.substring(sidx + 1, eidx);
        return formatStr;
    }

    /**
     * Average month by month
     *
     * @param data Data array list
     * @param colNames Column names
     * @param time Time list
     * @return Result data table
     * @throws Exception
     */
    public static DataTable ave_Month(List<Array> data, List<String> colNames, List<LocalDateTime> time) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("YearMonth", DataType.STRING);
        for (String col : colNames) {
            rTable.addColumn(col, DataType.DOUBLE);
        }

        List<String> yms = getYearMonths(time);
        LocalDateTime ldt;
        double v;
        for (String ym : yms) {
            int year = Integer.parseInt(ym.substring(0, 4));
            int month = Integer.parseInt(ym.substring(4));
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, ym);
            int col = 0;
            for (Array a : data) {
                List<Double> values = new ArrayList<>();
                for (int i = 0; i < time.size(); i++) {
                    ldt = time.get(i);
                    if (ldt.getYear() == year) {
                        if (ldt.getMonthValue() == month) {
                            v = a.getDouble(i);
                            if (!Double.isNaN(v)) {
                                values.add(v);
                            }
                        }
                    }
                }
                nRow.setValue(colNames.get(col), MIMath.mean(values));
                col += 1;
            }
        }

        return rTable;
    }

    /**
     * Get year months
     *
     * @param time Date list
     * @return Year month list
     */
    public static List<String> getYearMonths(List<LocalDateTime> time) {
        List<String> yms = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMM");
        String ym;
        for (LocalDateTime t : time) {
            ym = format.format(t);
            if (!yms.contains(ym)) {
                yms.add(ym);
            }
        }

        return yms;
    }

}
