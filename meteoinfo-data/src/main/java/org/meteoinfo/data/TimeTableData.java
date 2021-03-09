/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;

import org.meteoinfo.common.util.GlobalUtil;
import org.meteoinfo.data.analysis.Statistics;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.ndarray.DataType;

/**
 *
 * @author wyq
 */
public class TimeTableData extends TableData {

    // <editor-fold desc="Variables">
    //private int timeColIdx = 0;
    private String timeColName;
    private List<LocalDateTime> times;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public TimeTableData() {
        super();
        DataColumn col = new DataColumn("Time", DataType.DATE);
        this.addColumn(col);
        this.times = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param dataTable Data table
     * @param timeColName Time column name
     */
    public TimeTableData(DataTable dataTable, String timeColName) {
        super(dataTable);
        this.timeColName = timeColName;
        this.times = this.getColumnData(timeColName).getData();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get time column name
     *
     * @return Time column name
     */
    @Override
    public String getTimeColName() {
        return this.timeColName;
    }

    /**
     * Set time column name
     *
     * @param value Time column name
     */
    public void setTimeColName(String value) {
        this.timeColName = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Read data table from ASCII file
     *
     * @param fileName File name
     * @param timeColIdx Time column index
     * @param formatStr Time format string
     * @param dataColumns Data columns
     * @throws FileNotFoundException
     */
    public void readASCIIFile(String fileName, int timeColIdx, String formatStr, List<DataColumn> dataColumns) throws FileNotFoundException, IOException, Exception {
        //DataTable dTable = new DataTable();
        this.addColumn("Time", DataType.DATE);

        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String title = sr.readLine().trim();
        //Determine separator
        String separator = GlobalUtil.getDelimiter(title);
        String[] titleArray = GlobalUtil.split(title, separator);
        if (titleArray.length < 2) {
            JOptionPane.showMessageDialog(null, "File Format Error!");
            sr.close();
        } else {
            //Get fields
            for (DataColumn col : dataColumns) {
                this.addColumn(col);
            }
            DateTimeFormatter format = DateTimeFormatter.ofPattern(formatStr);
            List<Integer> dataIdxs = new ArrayList<>();
            String fieldName;
            for (int i = 0; i < titleArray.length; i++) {
                fieldName = titleArray[i];
                if (i == timeColIdx) {
                    this.getColumns().get(0).setColumnName(fieldName);
                    continue;
                }
                for (DataColumn col : dataColumns) {
                    if (col.getDataType() != DataType.DATE) {
                        if (fieldName.equals(col.getColumnName())) {
                            dataIdxs.add(i);
                        }
                    }
                }
            }

            String[] dataArray;
            int rn = 0;
            String line = sr.readLine();
            while (line != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                dataArray = GlobalUtil.split(line, separator);
                this.addRow();
                this.setValue(rn, 0, format.parse(dataArray[timeColIdx]));
                int cn = 1;
                for (int idx : dataIdxs) {
                    this.setValue(rn, cn, dataArray[idx]);
                    cn++;
                }

                rn += 1;
                line = sr.readLine();
            }

            //dataTable = dTable;
            sr.close();
        }
    }

    /**
     * Read data table from ASCII file
     *
     * @param fileName File name
     * @param timeColIdx Time column index
     * @param formatStr Time format string
     * @throws FileNotFoundException
     */
    public void readASCIIFile(String fileName, int timeColIdx, String formatStr) throws FileNotFoundException, IOException, Exception {
        //DataTable dTable = new DataTable();
        this.addColumn("Time", DataType.DATE);

        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String title = sr.readLine().trim();
        //Determine separator
        String separator = GlobalUtil.getDelimiter(title);
        String[] titleArray = GlobalUtil.split(title, separator);
        if (titleArray.length < 2) {
            JOptionPane.showMessageDialog(null, "File Format Error!");
            sr.close();
        } else {
            //Get fields
            DateTimeFormatter format = DateTimeFormatter.ofPattern(formatStr);
            List<Integer> dataIdxs = new ArrayList<>();
            String fieldName;
            for (int i = 0; i < titleArray.length; i++) {
                fieldName = titleArray[i];
                if (i == timeColIdx) {
                    this.getColumns().get(0).setColumnName(fieldName);
                } else {
                    this.addColumn(fieldName, DataType.STRING);
                    dataIdxs.add(i);
                }
            }

            String[] dataArray;
            int rn = 0;
            String line = sr.readLine();
            while (line != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                dataArray = GlobalUtil.split(line, separator);
                this.addRow();
                this.setValue(rn, 0, format.parse(dataArray[timeColIdx]));
                int cn = 1;
                for (int idx : dataIdxs) {
                    if (dataArray.length > idx) {
                        this.setValue(rn, cn, dataArray[idx]);
                    } else {
                        this.setValue(rn, cn, "");
                    }
                    cn++;
                }

                rn += 1;
                line = sr.readLine();
            }

            //dataTable = dTable;
            sr.close();
        }
    }
    
    /**
     * Get time index
     * @param t Time
     * @return Index
     */
    public int getTimeIndex_Ex(LocalDateTime t){
        return this.times.indexOf(t);
    }
    
    /**
     * Get time index
     * @param t Time
     * @return Index
     */
    public int getTimeIndex(LocalDateTime t){
        if (t.isBefore(times.get(0)))
            return 0;
        
        if (t.isAfter(times.get(times.size() - 1)))
            return times.size() - 1;
        
        int idx = -1;
        for (int i = 0; i < this.times.size(); i++){
            if (! t.isAfter(times.get(i))) {
                idx = i;
                break;
            }
        }
        
        return idx;
    }
    
    /**
     * Get time index list
     * @param ts Times
     * @return Index list
     */
    public List<Integer> getTimeIndex(List<LocalDateTime> ts){
        List<Integer> ii = new ArrayList<>();
        int i;
        for (LocalDateTime t : ts){
            i = this.times.indexOf(t);
            if (i >= 0)
                ii.add(i);
        }
        
        return ii;
    }
    
    /**
     * Get time index
     * @param st Start time
     * @param et End time
     * @param step Step
     * @return Time index
     */
    public List<Integer> getTimeIndex(LocalDateTime st, LocalDateTime et, int step){
        int sidx = getTimeIndex(st);
        int eidx = getTimeIndex(et);
        List<Integer> ii = new ArrayList<>();
        for (int i = sidx; i < eidx; i+=step){
            ii.add(i);
        }
        
        return ii;
    }

    /**
     * Get years
     *
     * @return Year list
     */
    public List<Integer> getYears() {
        List<Integer> years = new ArrayList<>();
        LocalDateTime ldt;
        int year;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            year = ldt.getYear();
            if (!years.contains(year)) {
                years.add(year);
            }
        }

        return years;
    }

    /**
     * Get year months
     *
     * @return Year month list
     */
    public List<String> getYearMonths() {
        List<String> yms = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMM");
        String ym;
        LocalDateTime date;
        for (DataRow row : this.getRows()) {
            date = (LocalDateTime) row.getValue(this.timeColName);
            if (date == null) {
                continue;
            }
            ym = format.format(date);
            if (!yms.contains(ym)) {
                yms.add(ym);
            }
        }

        return yms;
    }

    /**
     * Get days
     *
     * @return Date list
     */
    public List<LocalDateTime> getDates_Day() {
        List<String> days = new ArrayList<>();
        List<LocalDateTime> dates = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String day;
        LocalDateTime date;
        for (DataRow row : this.getRows()) {
            date = (LocalDateTime) row.getValue(this.timeColName);
            if (date == null) {
                continue;
            }
            day = format.format(date);
            if (!days.contains(day)) {
                days.add(day);
                dates.add(date);
            }
        }

        return dates;
    }
    
    /**
     * Get date hours
     *
     * @return Date list
     */
    public List<LocalDateTime> getDates_Hour() {
        List<String> hours = new ArrayList<>();
        List<LocalDateTime> dates = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String hour;
        LocalDateTime date;
        for (DataRow row : this.getRows()) {
            date = (LocalDateTime) row.getValue(this.timeColName);
            if (date == null) {
                continue;
            }
            hour = format.format(date);
            if (!hours.contains(hour)) {
                hours.add(hour);
                dates.add(date);
            }
        }

        return dates;
    }

    /**
     * Get data row list by year
     *
     * @param year The year
     * @return Data row list
     */
    public List<DataRow> getDataByYear(int year) {
        List<DataRow> rows = new ArrayList<>();
        LocalDateTime ldt;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            if (ldt.getYear() == year) {
                rows.add(row);
            }
        }

        return rows;
    }

    /**
     * Get data row list by year
     *
     * @param season The season
     * @return Data row list
     */
    public List<DataRow> getDataBySeason(String season) {
        List<Integer> months = this.getMonthsBySeason(season);
        List<DataRow> rows = new ArrayList<>();
        LocalDateTime ldt;
        int month;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            month = ldt.getMonthValue();
            if (months.contains(month)) {
                rows.add(row);
            }
        }

        return rows;
    }

    private List<Integer> getMonthsBySeason(String season) {
        List<Integer> months = new ArrayList<>();
        if (season.equalsIgnoreCase("spring")) {
            months.add(3);
            months.add(4);
            months.add(5);
        } else if (season.equalsIgnoreCase("summer")) {
            months.add(6);
            months.add(7);
            months.add(8);
        } else if (season.equalsIgnoreCase("autumn")) {
            months.add(9);
            months.add(10);
            months.add(11);
        } else if (season.equalsIgnoreCase("winter")) {
            months.add(12);
            months.add(1);
            months.add(2);
        }

        return months;
    }

    /**
     * Get data row list by year and month
     *
     * @param yearMonth The year and month
     * @return Data row list
     */
    public List<DataRow> getDataByYearMonth(String yearMonth) {
        int year = Integer.parseInt(yearMonth.substring(0, 4));
        int month = Integer.parseInt(yearMonth.substring(4));
        return this.getDataByYearMonth(year, month);
    }

    /**
     * Get data row list by year and month
     *
     * @param year The year
     * @param month The month
     * @return Data row list
     */
    public List<DataRow> getDataByYearMonth(int year, int month) {
        List<DataRow> rows = new ArrayList<>();
        LocalDateTime ldt;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            if (ldt.getYear() == year) {
                if (ldt.getMonthValue() == month) {
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    /**
     * Get data row list by date
     *
     * @param date Date string
     * @param drs Data rows
     * @return Data row list
     */
    public List<DataRow> getDataByDate(LocalDateTime date, List<DataRow> drs) {
        List<DataRow> rows = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime bdate;
        for (DataRow row : drs) {
            bdate = (LocalDateTime) row.getValue(this.timeColName);
            if (format.format(bdate).equals(format.format(date))) {
                rows.add(row);
            }
        }

        return rows;
    }
    
    /**
     * Get data row list by date - hour
     *
     * @param date Date string
     * @param drs Data rows
     * @return Data row list
     */
    public List<DataRow> getDataByDate_Hour(LocalDateTime date, List<DataRow> drs) {
        List<DataRow> rows = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHH");
        LocalDateTime bdate;
        for (DataRow row : drs) {
            bdate = (LocalDateTime) row.getValue(this.timeColName);
            if (format.format(bdate).equals(format.format(date))) {
                rows.add(row);
            }
        }        

        return rows;
    }

    /**
     * Get data row list by date
     *
     * @param year The year
     * @param month The month
     * @param day The day
     * @return Data row list
     */
    public List<DataRow> getDataByDate(int year, int month, int day) {
        List<DataRow> rows = new ArrayList<>();
        LocalDateTime ldt;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            if (ldt.getYear() == year) {
                if (ldt.getMonthValue() == month) {
                    if (ldt.getDayOfMonth() == day) {
                        rows.add(row);
                    }
                }
            }
        }

        return rows;
    }
    
    /**
     * Get data row list by date
     *
     * @param year The year
     * @param month The month
     * @param day The day
     * @param hour The hour
     * @return Data row list
     */
    public List<DataRow> getDataByDate(int year, int month, int day, int hour) {
        List<DataRow> rows = new ArrayList<>();
        LocalDateTime ldt;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            if (ldt.getYear() == year) {
                if (ldt.getMonthValue() == month) {
                    if (ldt.getDayOfMonth() == day) {
                        if (ldt.getHour() == hour)
                            rows.add(row);
                    }
                }
            }
        }

        return rows;
    }

    /**
     * Get data row list by month
     *
     * @param month The month
     * @return Data row list
     */
    public List<DataRow> getDataByMonth(int month) {
        List<DataRow> rows = new ArrayList<>();
        LocalDateTime ldt;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            if (ldt.getMonthValue() == month) {
                rows.add(row);
            }
        }

        return rows;
    }

    /**
     * Get data row list by day of week
     *
     * @param dow Day of week
     * @return Data row list
     */
    public List<DataRow> getDataByDayOfWeek(int dow) {
        dow = dow + 1;
        if (dow == 8) {
            dow = 1;
        }

        List<DataRow> rows = new ArrayList<>();
        LocalDateTime ldt;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            if (ldt.getDayOfWeek().getValue() == dow) {
                rows.add(row);
            }
        }

        return rows;
    }

    /**
     * Get data row list by hour
     *
     * @param hour The hour
     * @return Result data row list
     */
    public List<DataRow> getDataByHour(int hour) {
        List<DataRow> rows = new ArrayList<>();
        LocalDateTime ldt;
        for (DataRow row : this.getRows()) {
            ldt = (LocalDateTime) row.getValue(this.timeColName);
            if (ldt.getHour() == hour) {
                rows.add(row);
            }
        }

        return rows;
    }

    /**
     * Average year by year
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_Year(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Year", DataType.INT);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<Integer> years = this.getYears();
        for (int year : years) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, year);
            List<DataRow> rows = this.getDataByYear(year);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }

        return rTable;
    }

    /**
     * Average year
     *
     * @param cols The data columns
     * @param year The year
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_Year(List<DataColumn> cols, int year) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Year", DataType.INT);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        DataRow nRow = rTable.addRow();
        nRow.setValue(0, year);
        List<DataRow> rows = this.getDataByYear(year);
        for (DataColumn col : cols) {
            List<Double> values = this.getValidColumnValues(rows, col);
            nRow.setValue(col.getColumnName(), Statistics.mean(values));
        }

        return rTable;
    }

    /**
     * Average year by year
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_Year(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Year", DataType.INT);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<Integer> years = this.getYears();
        for (int year : years) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, year);
            List<DataRow> rows = this.getDataByYear(year);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.sum(values));
            }
        }

        return rTable;
    }

    /**
     * Average month by year
     *
     * @param cols The data columns
     * @param month The month
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_YearMonth(List<DataColumn> cols, int month) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Year", DataType.INT);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<Integer> years = this.getYears();
        for (int year : years) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, year);
            List<DataRow> rows = this.getDataByYearMonth(year, month);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }

        return rTable;
    }

    /**
     * Sum month by year
     *
     * @param cols The data columns
     * @param month The month
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_YearMonth(List<DataColumn> cols, int month) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Year", DataType.INT);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<Integer> years = this.getYears();
        for (int year : years) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, year);
            List<DataRow> rows = this.getDataByYearMonth(year, month);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.sum(values));
            }
        }

        return rTable;
    }

    /**
     * Average month by month
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_Month(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("YearMonth", DataType.STRING);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<String> yms = this.getYearMonths();
        for (String ym : yms) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, ym);
            List<DataRow> rows = this.getDataByYearMonth(ym);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }

        return rTable;
    }
    
    /**
     * Summary month by month
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_Month(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("YearMonth", DataType.STRING);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<String> yms = this.getYearMonths();
        for (String ym : yms) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, ym);
            List<DataRow> rows = this.getDataByYearMonth(ym);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.sum(values));
            }
        }

        return rTable;
    }

    /**
     * Average daily
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_Day(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn(new DataColumn("Date", DataType.DATE, "yyyyMMdd"));
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<LocalDateTime> days = this.getDates_Day();
        List<DataRow> drs = new ArrayList<>(this.getRows());
        for (LocalDateTime day : days) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, day);
            List<DataRow> rows = this.getDataByDate(day, drs);
            drs.removeAll(rows);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }

        return rTable;
    }
    
    /**
     * Summary daily
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_Day(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn(new DataColumn("Date", DataType.DATE, "yyyyMMdd"));
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<LocalDateTime> days = this.getDates_Day();
        List<DataRow> drs = new ArrayList<>(this.getRows());
        for (LocalDateTime day : days) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, day);
            List<DataRow> rows = this.getDataByDate(day, drs);
            drs.removeAll(rows);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.sum(values));
            }
        }

        return rTable;
    }
    
    /**
     * Average Hourly
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_Hour(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn(new DataColumn("Date", DataType.DATE, "yyyyMMddHH"));
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<LocalDateTime> hours = this.getDates_Hour();
        List<DataRow> drs = new ArrayList<>(this.getRows());
        for (LocalDateTime hour : hours) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, hour);
            List<DataRow> rows = this.getDataByDate_Hour(hour, drs);
            drs.removeAll(rows);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }

        return rTable;
    }
    
    /**
     * Summary Hourly
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_Hour(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn(new DataColumn("Date", DataType.DATE, "yyyyMMddHH"));
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<LocalDateTime> hours = this.getDates_Hour();
        List<DataRow> drs = new ArrayList<>(this.getRows());
        for (LocalDateTime hour : hours) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, hour);
            List<DataRow> rows = this.getDataByDate_Hour(hour, drs);
            drs.removeAll(rows);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.sum(values));
            }
        }

        return rTable;
    }

    /**
     * Average monthly
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_MonthOfYear(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Month", DataType.STRING);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<String> monthNames = Arrays.asList(new String[]{"Jan", "Feb", "Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        List<Integer> months = new ArrayList<>();
        int i;
        for (i = 1; i < 13; i++) {
            months.add(i);
        }

        i = 0;
        for (int month : months) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, monthNames.get(i));
            List<DataRow> rows = this.getDataByMonth(month);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
            i++;
        }

        return rTable;
    }
    
    /**
     * Summary month of year
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_MonthOfYear(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Month", DataType.STRING);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<String> monthNames = Arrays.asList(new String[]{"Jan", "Feb", "Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        List<Integer> months = new ArrayList<>();
        int i;
        for (i = 1; i < 13; i++) {
            months.add(i);
        }

        i = 0;
        for (int month : months) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, monthNames.get(i));
            List<DataRow> rows = this.getDataByMonth(month);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.sum(values));
            }
            i++;
        }

        return rTable;
    }

    /**
     * Average seasonal
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_SeasonOfYear(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Season", DataType.STRING);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<String> seasons = Arrays.asList(new String[]{"Spring", "Summer", "Autumn", "Winter"});
        for (String season : seasons) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, season);
            List<DataRow> rows = this.getDataBySeason(season);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }

        return rTable;
    }
    
    /**
     * Summary seasonal
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_SeasonOfYear(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Season", DataType.STRING);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<String> seasons = Arrays.asList(new String[]{"Spring", "Summer", "Autumn", "Winter"});
        for (String season : seasons) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, season);
            List<DataRow> rows = this.getDataBySeason(season);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }

        return rTable;
    }

    /**
     * Average by day of week
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_DayOfWeek(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Day", DataType.STRING);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.STRING);
        }

        List<String> dowNames = Arrays.asList(new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday"});
        List<Integer> dows = new ArrayList<>();
        dows.add(7);
        int i;
        for (i = 1; i < 7; i++) {
            dows.add(i);
        }

        i = 0;
        for (int dow : dows) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, dowNames.get(i));
            List<DataRow> rows = this.getDataByDayOfWeek(dow);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
            i++;
        }

        return rTable;
    }
    
    /**
     * Summary by day of week
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_DayOfWeek(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Day", DataType.STRING);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<String> dowNames = Arrays.asList(new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday"});
        List<Integer> dows = new ArrayList<>();
        dows.add(7);
        int i;
        for (i = 1; i < 7; i++) {
            dows.add(i);
        }

        i = 0;
        for (int dow : dows) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, dowNames.get(i));
            List<DataRow> rows = this.getDataByDayOfWeek(dow);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.sum(values));
            }
            i++;
        }

        return rTable;
    }

    /**
     * Average by hour of day
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable ave_HourOfDay(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Hour", DataType.INT);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<Integer> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }

        for (int hour : hours) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, hour);
            List<DataRow> rows = this.getDataByHour(hour);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }

        return rTable;
    }
    
    /**
     * Summary by hour of day
     *
     * @param cols The data columns
     * @return Result data table
     * @throws Exception
     */
    public DataTable sum_HourOfDay(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Hour", DataType.INT);
        for (DataColumn col : cols) {
            rTable.addColumn(col.getColumnName(), DataType.DOUBLE);
        }

        List<Integer> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }

        for (int hour : hours) {
            DataRow nRow = rTable.addRow();
            nRow.setValue(0, hour);
            List<DataRow> rows = this.getDataByHour(hour);
            for (DataColumn col : cols) {
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.sum(values));
            }
        }

        return rTable;
    }
    
    /**
     * Get date list - String
     * @param stdate Start date
     * @param enddate End date
     * @param tdtype Calendar type
     * @param timeDelt Time delta value
     * @return Date list
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException 
     */
    public static List<LocalDateTime> getDateList(LocalDateTime stdate, LocalDateTime enddate, String tdtype, int timeDelt)
            throws FileNotFoundException, IOException, ParseException {
        List<LocalDateTime> dates = new ArrayList<>();

        while (stdate.isBefore(enddate)) {
            dates.add(stdate);
            switch (tdtype.toUpperCase()) {
                case "YEAR":
                    stdate = stdate.plusYears(timeDelt);
                    break;
                case "MONTH":
                    stdate = stdate.plusMonths(timeDelt);
                    break;
                case "DAY":
                    stdate = stdate.plusDays(timeDelt);
                    break;                
                case "HOUR":
                    stdate = stdate.plusHours(timeDelt);
                    break;
                case "MINUTE":
                    stdate = stdate.plusMinutes(timeDelt);
                    break;
                default:
                    stdate = stdate.plusSeconds(timeDelt);
                    break;
            }            
        }
        dates.add(enddate);
        return dates;
    }
    
    /**
     * Time order for data
     * @param stdate Start date
     * @param enddate End date
     * @param tdtype Calendar type
     * @param timeDelt Time delta
     * @return Ordered data
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ParseException 
     */
    public TimeTableData timeOrder(LocalDateTime stdate, LocalDateTime enddate, String tdtype, int timeDelt) throws IOException, FileNotFoundException, ParseException, Exception{
        List<LocalDateTime> dateList = getDateList(stdate, enddate, tdtype, timeDelt);
        int lineNum = this.getRowCount();
        int colNum = this.getColumnCount();
        DataTable outData = new DataTable();
        for (DataColumn col : this.getDataColumns()){
            outData.addColumn((DataColumn)col.clone());
        }
        for (int i = 0; i < dateList.size(); i++){
            outData.addRow();
            //outData.setValue(i, timeColName, date);
            i += 1;
        }
        
        int idx;
        LocalDateTime date;
        for (int i = 0; i < lineNum; i++) {
            date = (LocalDateTime)this.getValue(i, timeColName);
            idx = dateList.indexOf(date);
            if (idx >= 0) {                
                for (int j = 0; j < colNum; j++){
                    outData.setValue(idx, j, this.getValue(i, j));
                }
            }
        }
        
        TimeTableData r = new TimeTableData(outData, this.timeColName);
        return r;
    }

    // </editor-fold>
}
