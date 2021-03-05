/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import org.meteoinfo.data.XYSeriesData;

/**
 *
 * @author Yaqiang Wang
 */
public class PlotSeries {
    // <editor-fold desc="Variables">
    private XYSeriesData data;
    private SeriesLegend legend;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construtor
     * @param data Series data
     * @param legend Series legend
     */
    public PlotSeries(XYSeriesData data, SeriesLegend legend){
        this.data = data;
        this.legend = legend;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get series data
     * @return Series data
     */
    public XYSeriesData getData(){
        return this.data;
    }
    
    /**
     * Set series data
     * @param value Series data
     */
    public void setData(XYSeriesData value){
        this.data = value;
    }
    
    /**
     * Get series legend
     * @return Series legend
     */
    public SeriesLegend getLegend(){
        return this.legend;
    }
    
    /**
     * Set series legend
     * @param value Series legend
     */
    public void setLegend(SeriesLegend value){
        this.legend = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
