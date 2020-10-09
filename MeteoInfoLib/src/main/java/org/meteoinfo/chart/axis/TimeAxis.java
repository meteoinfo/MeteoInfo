/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.axis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.global.util.JDateUtil;

/**
 *
 * @author wyq
 */
public class TimeAxis extends Axis implements Cloneable {

    private String timeFormat;
    private TimeUnit timeUnit;
    private boolean varFormat;

    /**
     * Constructor
     *
     * @param label Axis label
     * @param xAxis If is x axis
     */
    public TimeAxis(String label, boolean xAxis) {
        super(label, xAxis);

        this.timeFormat = "yyyy-MM-dd";
        this.timeUnit = TimeUnit.DAY;
        this.varFormat = true;
    }
    
    /**
     * Constructor
     * @param axis Axis
     */
    public TimeAxis(Axis axis) {
        this(axis.getLabel().getText(), axis.isXAxis());
        this.autoTick = axis.autoTick;
        this.drawLabel = axis.drawLabel;
        this.drawTickLabel = axis.drawTickLabel;
        this.drawTickLine = axis.drawTickLine;
        this.insideTick = axis.insideTick;
        this.inverse = axis.inverse;
        this.setLabelColor(axis.getLabelColor());
        this.lineWidth = axis.lineWidth;
        this.lineStyle = axis.lineStyle;
        this.location = axis.location;
        this.maxValue = axis.maxValue;
        this.minValue = axis.minValue;
        this.minorTickNum = axis.minorTickNum;
        this.minorTickVisible = axis.minorTickVisible;
        //this.setShift(axis.getShift());
        this.tickColor = axis.tickColor;
        this.tickDeltaValue = axis.tickDeltaValue;
        this.tickLabelColor = axis.tickLabelColor;
        this.tickLabelFont = axis.tickLabelFont;
        this.tickLength = axis.tickLength;
        this.visible = axis.visible;
        this.positionType = axis.positionType;
        this.position = axis.position;
    }

    /**
     * Get time format
     *
     * @return Time format
     */
    public String getTimeFormat() {
        return this.timeFormat;
    }

    /**
     * Set time format
     *
     * @param value
     */
    public void setTimeFormat(String value) {
        this.timeFormat = value;
        if (value.contains("s")) {
            this.timeUnit = TimeUnit.SECOND;
        } else if (value.contains("m")) {
            this.timeUnit = TimeUnit.MINUTE;
        } else if (value.contains("H")) {
            this.timeUnit = TimeUnit.HOUR;
        } else if (value.contains("d")) {
            this.timeUnit = TimeUnit.DAY;
        } else if (value.contains("M")) {
            this.timeUnit = TimeUnit.MONTH;
        } else {
            this.timeUnit = TimeUnit.YEAR;
        }
        this.varFormat = false;
    }

    /**
     * Get time unit
     *
     * @return Time unit
     */
    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    /**
     * Set time unit
     *
     * @param value Time unit
     */
    public void setTimeUnit(TimeUnit value) {
        this.timeUnit = value;
    }
    
    /**
     * If variable time format
     * @return Boolean
     */
    public boolean isVarFormat(){
        return this.varFormat;
    }
    
    /**
     * Set if variable format
     * @param value Boolean
     */
    public void setVarFormat(boolean value){
        this.varFormat = value;
    }

    /**
     * Get tick labels
     *
     */
    @Override
    public void updateTickLabels() {
        //this.updateTimeTickValues();
        List<ChartText> tls = new ArrayList<>();
        String lab;
        DateTimeFormatter format = DateTimeFormatter.ofPattern(this.timeFormat);
        LocalDateTime date;
        double[] tvs = this.getTickValues();
        if (tvs != null){
            for (double value : this.getTickValues()) {
                date = JDateUtil.fromOADate(value);
                lab = format.format(date);
                tls.add(new ChartText(lab));
            }
        }

        this.setTickLabels(tls);
    }

    /**
     * Update time tick values
     */
    @Override
    public void updateTickValues() {
        if (this.varFormat)
            updateTimeTickValues_var();
        else {
            if (this.timeUnit != null)
                updateTimeTickValues();
        }
    }

    /**
     * Update time tick values
     */
    private void updateTimeTickValues() {
        LocalDateTime sdate = JDateUtil.fromOADate(this.getMinValue());
        LocalDateTime edate = JDateUtil.fromOADate(this.getMaxValue());
        LocalDateTime ssdate = LocalDateTime.of(sdate.getYear(), sdate.getMonthValue(), sdate.getDayOfMonth(),
                sdate.getHour(), sdate.getMinute(), sdate.getSecond());

        List<LocalDateTime> dates = new ArrayList<>();
        switch (this.timeUnit) {
            case YEAR:
                sdate = LocalDateTime.of(sdate.getYear(), 1, 1, 0, 0, 0);
                if (!sdate.isBefore(ssdate)) {
                    dates.add(sdate);
                }
                while (!sdate.isAfter(edate)) {
                    sdate = sdate.withYear(sdate.getYear() + 1);
                    dates.add(sdate);
                }
                break;
            case MONTH:
                sdate = LocalDateTime.of(sdate.getYear(), sdate.getMonthValue(), 1, 0, 0, 0);
                if (!sdate.isBefore(ssdate)) {
                    dates.add(sdate);
                }
                while (!sdate.isAfter(edate)) {
                    sdate = sdate.plusMonths(1);
                    if (!sdate.isBefore(ssdate)) {
                        dates.add(sdate);
                    }
                }
                break;
            case DAY:
                sdate = LocalDateTime.of(sdate.getYear(), sdate.getMonthValue(), sdate.getDayOfMonth(),
                        0, 0, 0);
                if (!sdate.isBefore(ssdate)) {
                    dates.add(sdate);
                }
                while (!sdate.isAfter(edate)) {
                    sdate = sdate.plusDays(1);
                    if (sdate.isBefore(edate)) {
                        dates.add(sdate);
                    }
                }
                break;
            case HOUR:
                sdate = LocalDateTime.of(sdate.getYear(), sdate.getMonthValue(), sdate.getDayOfMonth(),
                        sdate.getHour(), 0, 0);
                if (!sdate.isBefore(ssdate)) {
                    dates.add(sdate);
                }
                while (!sdate.isAfter(edate)) {
                    sdate = sdate.plusHours(1);
                    if (sdate.isBefore(edate)) {
                        dates.add(sdate);
                    }
                }
                break;
            case MINUTE:
                sdate = ssdate.withSecond(0);
                if (!sdate.isBefore(ssdate)) {
                    dates.add(sdate);
                }
                while (!sdate.isAfter(edate)) {
                    sdate = sdate.plusMinutes(1);
                    if (sdate.isBefore(edate)) {
                        dates.add(sdate);
                    }
                }
                break;
            case SECOND:
                if (!sdate.isBefore(ssdate)) {
                    dates.add(sdate);
                }
                while (!sdate.isAfter(edate)) {
                    sdate = sdate.plusSeconds(1);
                    if (sdate.isBefore(edate)) {
                        dates.add(sdate);
                    }
                }
                break;
        }

        double[] tvs = new double[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            tvs[i] = JDateUtil.toOADate(dates.get(i));
        }
        this.setTickValues(tvs);
    }

    /**
     * Update time tick values
     */
    private void updateTimeTickValues_var() {
        LocalDateTime sdate = JDateUtil.fromOADate(this.getMinValue());
        LocalDateTime edate = JDateUtil.fromOADate(this.getMaxValue());
        LocalDateTime ssdate = sdate;

        List<LocalDateTime> dates = new ArrayList<>();
        sdate = ssdate.plusYears(5);
        if (sdate.isBefore(edate)) {
            this.timeFormat = "yyyy";
            this.timeUnit = TimeUnit.YEAR;
            sdate = ssdate.withMonth(1);
            sdate = ssdate.withDayOfMonth(1);
            sdate = ssdate.withHour(0);
            sdate = ssdate.withMinute(0);
            sdate = ssdate.withSecond(0);
            if (!sdate.isBefore(ssdate)) {
                dates.add(sdate);
            }
            while (!sdate.isAfter(edate)) {
                sdate = sdate.plusYears(1);
                if (sdate.isBefore(edate))
                    dates.add(sdate);
            }
        } else {
            sdate = ssdate.plusMonths(5);
            if (sdate.isBefore(edate)) {
                this.timeFormat = "M";
                this.timeUnit = TimeUnit.MONTH;
                sdate = ssdate.withDayOfMonth(1);
                sdate = ssdate.withHour(0);
                sdate = ssdate.withMinute(0);
                sdate = ssdate.withSecond(0);
                if (!sdate.isBefore(ssdate)) {
                    dates.add(sdate);
                }
                while (!sdate.isAfter(edate)) {
                    sdate = sdate.plusMonths(1);
                    if (sdate.isBefore(edate))
                        dates.add(sdate);
                }
            } else {
                sdate = ssdate.plusDays(5);
                if (sdate.isBefore(edate)) {
                    this.timeFormat = "d";
                    this.timeUnit = TimeUnit.DAY;
                    sdate = ssdate.withHour(0);
                    sdate = ssdate.withMinute(0);
                    sdate = ssdate.withSecond(0);
                    if (!sdate.isBefore(ssdate)) {
                        dates.add(sdate);
                    }
                    while (!sdate.isAfter(edate)) {
                        sdate = sdate.plusDays(1);
                        if (sdate.isBefore(edate))
                            dates.add(sdate);
                    }
                } else {
                    sdate = ssdate.plusHours(5);
                    if (sdate.isBefore(edate)) {
                        this.timeFormat = "H";
                        this.timeUnit = TimeUnit.HOUR;
                        sdate = ssdate.withMinute(0);
                        sdate = ssdate.withSecond(0);
                        if (!sdate.isBefore(ssdate)) {
                            dates.add(sdate);
                        }
                        while (!sdate.isAfter(edate)) {
                            sdate = sdate.plusHours(1);
                            if (sdate.isBefore(edate))
                                dates.add(sdate);
                        }
                    } else {
                        sdate = ssdate.plusMinutes(5);
                        if (sdate.isBefore(edate)) {
                            this.timeFormat = "HH:mm";
                            this.timeUnit = TimeUnit.MINUTE;
                            sdate = ssdate.withSecond(0);
                            if (!sdate.isBefore(ssdate)) {
                                dates.add(sdate);
                            }
                            while (!sdate.isAfter(edate)) {
                                sdate = sdate.plusMinutes(1);
                                if (sdate.isBefore(edate))
                                    dates.add(sdate);
                            }
                        } else {
                            this.timeFormat = "HH:mm:ss";
                            this.timeUnit = TimeUnit.SECOND;
                            if (!sdate.isBefore(ssdate)) {
                                dates.add(sdate);
                            }
                            while (!sdate.isAfter(edate)) {
                                sdate = sdate.plusSeconds(1);
                                if (sdate.isBefore(edate))
                                    dates.add(sdate);
                            }
                        }
                    }
                }
            }
        }

        double[] tvs = new double[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            tvs[i] = JDateUtil.toOADate(dates.get(i));
        }
        this.setTickValues(tvs);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (TimeAxis) super.clone();
    }
}
