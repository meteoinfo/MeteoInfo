/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;

/**
 *
 * @author wyq
 */
public class SeriesLegend {
    // <editor-fold desc="Variables">
    private ChartPlotMethod plotMethod;
    private List<ColorBreak> legendBreaks;
    private Color errorColor;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public SeriesLegend(){
        this.plotMethod = ChartPlotMethod.LINE;
        this.legendBreaks = new ArrayList<>();
        this.errorColor = Color.black;
    }
    
    /**
     * Constructor
     * @param cb ColorBreak
     */
    public SeriesLegend(ColorBreak cb){
        this();
        this.legendBreaks.add(cb);
    }
    
    /**
     * Constructor
     * @param n Break number
     */
    public SeriesLegend(int n){
        this();
        for (int i = 0; i < n; i++)
            this.legendBreaks.add(new PolylineBreak());
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get plot method
     * @return Plot method
     */
    public ChartPlotMethod getPlotMethod(){
        return this.plotMethod;
    }
    
    /**
     * Set plot method
     * @param value Plot method
     */
    public void setPlotMethod(ChartPlotMethod value){
        this.plotMethod = value;
    }
    
    /**
     * Get if the legend is PointBreak
     * @return Boolean
     */
    public boolean isPoint(){
        return this.legendBreaks.get(0) instanceof PointBreak;
    }
    
    /**
     * Get if the legend is PolylineBreak
     * @return Boolean
     */
    public boolean isLine(){
        return this.legendBreaks.get(0) instanceof PolylineBreak;
    }
    
    /**
     * Get if the legend is PolygonBreak
     * @return Boolean
     */
    public boolean isPolygon(){
        return this.legendBreaks.get(0) instanceof PolygonBreak;
    }
    
    /**
     * Get if if mutiple legend breaks
     * @return Boolean
     */
    public boolean isMutiple(){
        return this.legendBreaks.size() > 1;
    }
    
    /**
     * Get a legend break
     * @return Legend break
     */
    public ColorBreak getLegendBreak(){
        return this.legendBreaks.get(0);
    }
    
    /**
     * Set legend break
     * @param cb Legend break
     */
    public void setLegendBreak(ColorBreak cb){
        this.legendBreaks.clear();
        this.legendBreaks.add(cb);
    }
    
    /**
     * Get a legend break
     * @param idx Index
     * @return Legend break
     */
    public ColorBreak getLegendBreak(int idx){
        if (idx >= this.legendBreaks.size())
            idx = 0;
        return this.legendBreaks.get(idx);
    }
    
    /**
     * Set legend break
     * @param idx Index
     * @param cb Legend break
     */
    public void setLegendBreak(int idx, ColorBreak cb){
        this.legendBreaks.set(idx, cb);
    }
    
    /**
     * Get error color
     * @return Error color
     */
    public Color getErrorColor(){
        return this.errorColor;
    }
    
    /**
     * Set error color
     * @param value Error color
     */
    public void setErrorColor(Color value){
        this.errorColor = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a legend break
     * @param cb Legend break
     */
    public void addLegendBreak(ColorBreak cb){
        this.legendBreaks.add(cb);
    }
    
    /**
     * Get legend break number
     * @return Legend break number
     */
    public int getBreakNum(){
        return this.legendBreaks.size();
    }
    // </editor-fold>
}
