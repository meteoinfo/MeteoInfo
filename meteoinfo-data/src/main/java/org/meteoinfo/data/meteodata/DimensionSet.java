/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata;

import org.meteoinfo.data.DataRange;

/**
 *
 * @author wyq
 */
public class DimensionSet {

    // <editor-fold desc="Variables">

    public DataRange xRange;
    public DataRange yRange;
    public DataRange zRange;
    public DataRange tRange;

    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public DimensionSet() {
        this.xRange = new DataRange();
        this.yRange = new DataRange();
        this.zRange = new DataRange();
        this.tRange = new DataRange();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get plot dimension
     *
     * @return Plot dimension
     */
    public PlotDimension getPlotDimension() {
        if (this.zRange.isFixed() && this.tRange.isFixed() && this.xRange.isFixed() && this.yRange.isFixed()) {
            return null;
        }

        PlotDimension pd = null;
        if (this.tRange.isFixed()) {
            if (this.zRange.isFixed()) {
                if (this.xRange.isFixed()) {
                    pd = PlotDimension.Lat;
                } else if (this.yRange.isFixed()) {
                    pd = PlotDimension.Lon;
                } else {
                    pd = PlotDimension.Lat_Lon;
                }
            } else {
                if (this.xRange.isFixed()) {
                    if (this.yRange.isFixed()) {
                        pd = PlotDimension.Level;
                    } else {
                        pd = PlotDimension.Level_Lat;
                    }
                } else {
                    pd = PlotDimension.Level_Lon;
                }
            }
        } else {
            if (this.zRange.isFixed()) {
                if (this.yRange.isFixed()) {
                    if (this.xRange.isFixed()) {
                        pd = PlotDimension.Time;
                    } else {
                        pd = PlotDimension.Time_Lon;
                    }
                } else {
                    pd = PlotDimension.Time_Lat;
                }
            } else {
                pd = PlotDimension.Level_Time;
            }
        }

        return pd;
    }
    // </editor-fold>
}
