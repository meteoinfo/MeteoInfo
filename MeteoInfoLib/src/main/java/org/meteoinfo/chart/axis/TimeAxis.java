/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.axis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.global.util.DateUtil;

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
        this.setAutoTick(axis.isAutoTick());
        this.setDrawLabel(axis.isDrawLabel());
        this.setDrawTickLabel(axis.isDrawTickLabel());
        this.setDrawTickLine(axis.isDrawTickLine());
        this.setInsideTick(axis.isInsideTick());
        this.setInverse(axis.isInverse());
        this.setLabelColor(axis.getLabelColor());
        this.setLineWidth(axis.getLineWidth());
        this.setLineStyle(axis.getLineStyle());
        //this.setLineStroke(axis.getLineStroke());
        this.setLocation(axis.getLocation());
        this.setMaxValue(axis.getMaxValue());
        this.setMinValue(axis.getMinValue());
        this.setMinorTickNum(axis.getMinorTickNum());
        this.setMinorTickVisible(axis.isMinorTickVisible());
        this.setShift(axis.getShift());
        this.setTickColor(axis.getTickColor());
        this.setTickDeltaValue(axis.getTickDeltaValue());
        this.setTickLabelColor(axis.getTickLabelColor());
        this.setTickLabelFont(axis.getTickLabelFont());
        this.setTickLength(axis.getTickLength());
        this.setVisible(axis.isVisible());
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
        SimpleDateFormat format = new SimpleDateFormat(this.timeFormat);
        Date date;
        double[] tvs = this.getTickValues();
        if (tvs != null){
            for (double value : this.getTickValues()) {
                date = DateUtil.fromOADate(value);
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
        Date sdate = DateUtil.fromOADate(this.getMinValue());
        Date edate = DateUtil.fromOADate(this.getMaxValue());
        Calendar scal = Calendar.getInstance();
        Calendar ecal = Calendar.getInstance();
        scal.setTime(sdate);
        ecal.setTime(edate);
        Calendar sscal = Calendar.getInstance();
        sscal.setTime(sdate);

        List<Date> dates = new ArrayList<>();
        switch (this.timeUnit) {
            case YEAR:
                scal.set(Calendar.MONTH, 0);
                scal.set(Calendar.DAY_OF_MONTH, 1);
                scal.set(Calendar.HOUR_OF_DAY, 0);
                scal.set(Calendar.MINUTE, 0);
                scal.set(Calendar.SECOND, 0);
                if (!scal.before(sscal)) {
                    dates.add(scal.getTime());
                }
                while (!scal.after(ecal)) {
                    scal.set(Calendar.YEAR, scal.get(Calendar.YEAR) + 1);
                    dates.add(scal.getTime());
                }
                break;
            case MONTH:
                scal.set(Calendar.DAY_OF_MONTH, 1);
                scal.set(Calendar.HOUR_OF_DAY, 0);
                scal.set(Calendar.MINUTE, 0);
                scal.set(Calendar.SECOND, 0);
                if (!scal.before(sscal)) {
                    dates.add(scal.getTime());
                }
                while (!scal.after(ecal)) {
                    scal.add(Calendar.MONTH, 1);
                    if (!scal.before(sscal)) {
                        dates.add(scal.getTime());
                    }
                }
                break;
            case DAY:
                scal.set(Calendar.HOUR_OF_DAY, 0);
                scal.set(Calendar.MINUTE, 0);
                scal.set(Calendar.SECOND, 0);
                if (!scal.before(sscal)) {
                    dates.add(scal.getTime());
                }
                while (!scal.after(ecal)) {
                    scal.add(Calendar.DAY_OF_MONTH, 1);
                    if (scal.before(ecal)) {
                        dates.add(scal.getTime());
                    }
                }
                break;
            case HOUR:
                scal.set(Calendar.MINUTE, 0);
                scal.set(Calendar.SECOND, 0);
                if (!scal.before(sscal)) {
                    dates.add(scal.getTime());
                }
                while (!scal.after(ecal)) {
                    scal.add(Calendar.HOUR_OF_DAY, 1);
                    if (scal.before(ecal)) {
                        dates.add(scal.getTime());
                    }
                }
                break;
            case MINUTE:
                scal.set(Calendar.SECOND, 0);
                if (!scal.before(sscal)) {
                    dates.add(scal.getTime());
                }
                while (!scal.after(ecal)) {
                    scal.add(Calendar.MINUTE, 1);
                    if (scal.before(ecal)) {
                        dates.add(scal.getTime());
                    }
                }
                break;
            case SECOND:
                if (!scal.before(sscal)) {
                    dates.add(scal.getTime());
                }
                while (!scal.after(ecal)) {
                    scal.add(Calendar.SECOND, 1);
                    if (scal.before(ecal)) {
                        dates.add(scal.getTime());
                    }
                }
                break;
        }

        double[] tvs = new double[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            tvs[i] = DateUtil.toOADate(dates.get(i));
        }
        this.setTickValues(tvs);
    }

    /**
     * Update time tick values
     */
    private void updateTimeTickValues_var() {
        Date sdate = DateUtil.fromOADate(this.getMinValue());
        Date edate = DateUtil.fromOADate(this.getMaxValue());
        Calendar scal = Calendar.getInstance();
        Calendar ecal = Calendar.getInstance();
        scal.setTime(sdate);
        ecal.setTime(edate);
        Calendar sscal = Calendar.getInstance();
        sscal.setTime(sdate);

        List<Date> dates = new ArrayList<>();
        scal.add(Calendar.YEAR, 5);
        if (scal.before(ecal)) {
            this.timeFormat = "yyyy";
            this.timeUnit = TimeUnit.YEAR;
            scal.setTime(sdate);
            scal.set(Calendar.MONTH, 0);
            scal.set(Calendar.DAY_OF_MONTH, 1);
            scal.set(Calendar.HOUR_OF_DAY, 0);
            scal.set(Calendar.MINUTE, 0);
            scal.set(Calendar.SECOND, 0);
            if (!scal.before(sscal)) {
                dates.add(scal.getTime());
            }
            while (!scal.after(ecal)) {
                scal.set(Calendar.YEAR, scal.get(Calendar.YEAR) + 1);
                dates.add(scal.getTime());
            }
        } else {
            scal.setTime(sdate);
            scal.add(Calendar.MONTH, 5);
            if (scal.before(ecal)) {
                scal.setTime(sdate);
                this.timeFormat = "M";
                this.timeUnit = TimeUnit.MONTH;
                scal.set(Calendar.DAY_OF_MONTH, 1);
                scal.set(Calendar.HOUR_OF_DAY, 0);
                scal.set(Calendar.MINUTE, 0);
                scal.set(Calendar.SECOND, 0);
                if (!scal.before(sscal)) {
                    dates.add(scal.getTime());
                }
                while (!scal.after(ecal)) {
                    scal.add(Calendar.MONTH, 1);
                    if (scal.before(ecal))
                        dates.add(scal.getTime());
                }
            } else {
                scal.setTime(sdate);
                scal.add(Calendar.DAY_OF_MONTH, 5);
                if (scal.before(ecal)) {
                    scal.setTime(sdate);
                    this.timeFormat = "d";
                    this.timeUnit = TimeUnit.DAY;
                    scal.set(Calendar.HOUR_OF_DAY, 0);
                    scal.set(Calendar.MINUTE, 0);
                    scal.set(Calendar.SECOND, 0);
                    if (!scal.before(sscal)) {
                        dates.add(scal.getTime());
                    }
                    while (!scal.after(ecal)) {
                        scal.add(Calendar.DAY_OF_MONTH, 1);
                        if (scal.before(ecal))
                            dates.add(scal.getTime());
                    }
                } else {
                    scal.setTime(sdate);
                    scal.add(Calendar.HOUR_OF_DAY, 5);
                    if (scal.before(ecal)) {
                        scal.setTime(sdate);
                        this.timeFormat = "H";
                        this.timeUnit = TimeUnit.HOUR;
                        scal.set(Calendar.MINUTE, 0);
                        scal.set(Calendar.SECOND, 0);
                        if (!scal.before(sscal)) {
                            dates.add(scal.getTime());
                        }
                        while (!scal.after(ecal)) {
                            scal.add(Calendar.HOUR_OF_DAY, 1);
                            if (scal.before(ecal))
                                dates.add(scal.getTime());
                        }
                    } else {
                        scal.setTime(sdate);
                        scal.add(Calendar.MINUTE, 5);
                        if (scal.before(ecal)) {
                            scal.setTime(sdate);
                            this.timeFormat = "HH:mm";
                            this.timeUnit = TimeUnit.MINUTE;
                            scal.set(Calendar.SECOND, 0);
                            if (!scal.before(sscal)) {
                                dates.add(scal.getTime());
                            }
                            while (!scal.after(ecal)) {
                                scal.add(Calendar.MINUTE, 1);
                                if (scal.before(ecal))
                                    dates.add(scal.getTime());
                            }
                        } else {
                            scal.setTime(sdate);
                            this.timeFormat = "HH:mm:ss";
                            this.timeUnit = TimeUnit.SECOND;
                            if (!scal.before(sscal)) {
                                dates.add(scal.getTime());
                            }
                            while (!scal.after(ecal)) {
                                scal.add(Calendar.SECOND, 1);
                                if (scal.before(ecal))
                                    dates.add(scal.getTime());
                            }
                        }
                    }
                }
            }
        }

        double[] tvs = new double[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            tvs[i] = DateUtil.toOADate(dates.get(i));
        }
        this.setTickValues(tvs);
    }

    /**
     * Update time labels
     */
    public void updateTimeLabels_back() {
        Date sdate = DateUtil.fromOADate(this.getMinValue());
        Date edate = DateUtil.fromOADate(this.getMaxValue());
        Calendar scal = Calendar.getInstance();
        Calendar ecal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        scal.setTime(sdate);
        ecal.setTime(edate);
        Calendar sscal = Calendar.getInstance();
        sscal.setTime(sdate);
        boolean sameYear = false;
        boolean sameMonth = false;
        boolean sameDay = false;
        boolean sameHour = false;
        if (scal.get(Calendar.YEAR) == ecal.get(Calendar.YEAR)) {
            sameYear = true;
        }
        if (scal.get(Calendar.MONTH) == ecal.get(Calendar.MONTH)) {
            sameMonth = true;
        }
        if (scal.get(Calendar.DAY_OF_YEAR) == ecal.get(Calendar.DAY_OF_YEAR)) {
            sameDay = true;
        }
        if (scal.get(Calendar.HOUR_OF_DAY) == ecal.get(Calendar.HOUR_OF_DAY)) {
            sameHour = true;
        }

        List<Date> dates = new ArrayList<>();
        if (sameYear) {
            if (sameMonth) {
                if (sameDay) {
                    if (sameHour) {
                        cal.setTime(scal.getTime());
                        cal.add(Calendar.MINUTE, 5);
                        if (cal.before(ecal)) {
                            this.timeFormat = "HH:mm";
                            scal.set(Calendar.MINUTE, scal.get(Calendar.MINUTE) + 1);
                            scal.set(Calendar.SECOND, 0);
                            dates.add(scal.getTime());
                            while (scal.before(ecal)) {
                                scal.set(Calendar.MINUTE, scal.get(Calendar.MINUTE) + 1);
                                dates.add(scal.getTime());
                            }
                        } else {
                            this.timeFormat = "HH:mm:ss";
                            scal.set(Calendar.MINUTE, 0);
                            scal.set(Calendar.SECOND, 0);
                            if (scal.after(sscal)) {
                                dates.add(scal.getTime());
                            }
                            while (scal.before(ecal)) {
                                scal.set(Calendar.SECOND, scal.get(Calendar.SECOND) + 10);
                                if (scal.after(sscal)) {
                                    dates.add(scal.getTime());
                                }
                            }
                        }
                    } else {
                        this.timeFormat = "HH:mm";
                        scal.set(Calendar.HOUR_OF_DAY, scal.get(Calendar.HOUR_OF_DAY) + 1);
                        scal.set(Calendar.MINUTE, 0);
                        scal.set(Calendar.SECOND, 0);
                        dates.add(scal.getTime());
                        while (scal.before(ecal)) {
                            scal.set(Calendar.HOUR_OF_DAY, scal.get(Calendar.HOUR_OF_DAY) + 1);
                            dates.add(scal.getTime());
                        }
                    }
                } else {
                    cal.setTime(scal.getTime());
                    cal.add(Calendar.DAY_OF_MONTH, 5);
                    if (cal.before(ecal)) {
                        this.timeFormat = "MM/dd";
                        scal.set(Calendar.DAY_OF_MONTH, scal.get(Calendar.DAY_OF_MONTH) + 1);
                        scal.set(Calendar.HOUR_OF_DAY, 0);
                        scal.set(Calendar.MINUTE, 0);
                        scal.set(Calendar.SECOND, 0);
                        dates.add(scal.getTime());
                        while (scal.before(ecal)) {
                            scal.set(Calendar.DAY_OF_MONTH, scal.get(Calendar.DAY_OF_MONTH) + 1);
                            dates.add(scal.getTime());
                        }
                    } else {
                        this.timeFormat = "HH:mm";
                        scal.set(Calendar.HOUR_OF_DAY, 0);
                        scal.set(Calendar.MINUTE, 0);
                        scal.set(Calendar.SECOND, 0);
                        if (scal.after(sscal)) {
                            dates.add(scal.getTime());
                        }
                        while (scal.before(ecal)) {
                            scal.set(Calendar.HOUR_OF_DAY, scal.get(Calendar.HOUR_OF_DAY) + 6);
                            if (scal.after(sscal)) {
                                dates.add(scal.getTime());
                            }
                        }
                    }
                }
            } else {
                this.timeFormat = "MM";
                scal.set(Calendar.MONTH, scal.get(Calendar.MONTH) + 1);
                scal.set(Calendar.DAY_OF_MONTH, 1);
                scal.set(Calendar.HOUR_OF_DAY, 0);
                scal.set(Calendar.MINUTE, 0);
                scal.set(Calendar.SECOND, 0);
                dates.add(scal.getTime());
                while (scal.before(ecal)) {
                    scal.set(Calendar.MONTH, scal.get(Calendar.MONTH) + 1);
                    dates.add(scal.getTime());
                }
            }
        } else {
            this.timeFormat = "yyyy";
            scal.set(Calendar.YEAR, scal.get(Calendar.YEAR) + 1);
            scal.set(Calendar.MONTH, 0);
            scal.set(Calendar.DAY_OF_MONTH, 1);
            scal.set(Calendar.HOUR_OF_DAY, 0);
            scal.set(Calendar.MINUTE, 0);
            scal.set(Calendar.SECOND, 0);
            dates.add(scal.getTime());
            while (scal.before(ecal)) {
                scal.set(Calendar.YEAR, scal.get(Calendar.YEAR) + 1);
                dates.add(scal.getTime());
            }
        }

        double[] tvs = new double[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            tvs[i] = DateUtil.toOADate(dates.get(i));
        }
        this.setTickValues(tvs);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (TimeAxis) super.clone();
    }
}
