/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.script;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.WindowConstants;
import org.meteoinfo.chart.ChartPanel;
import org.meteoinfo.chart.plot.XY1DPlot;
import org.meteoinfo.legend.PointStyle;
import org.meteoinfo.global.Extent;
import org.meteoinfo.legend.LineStyles;

/**
 *
 * @author yaqiang
 */
public class MeteoInfoPlot {

    // <editor-fold desc="Variables">
    private boolean batchMode;
    private ChartPanel charPanel;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MeteoInfoPlot() {
        this.batchMode = true;        
        this.charPanel = new ChartPanel(null);        
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    
    // <editor-fold desc="Plot">
//    /**
//     * Plot
//     *
//     * @param yValues Y values
//     */
//    public void plot(List<Number> yValues) {
//        List<Number> xValues = new ArrayList<Number>();
//        for (int i = 0; i < yValues.size(); i++) {
//            xValues.add(i);
//        }
//
//        this.plot(xValues, yValues);
//    }

//    /**
//     * Plot
//     *
//     * @param xValues X values
//     * @param yValues Y values
//     */
//    public void plot(List<Number> xValues, List<Number> yValues) {
//        this.plot(xValues, yValues, "");
//    }

//    /**
//     * Plot
//     *
//     * @param xValues X values
//     * @param yValues Y values
//     * @param style Plot style
//     */
//    public void plot(List<Number> xValues, List<Number> yValues, String style) {
//        if (xValues.size() != yValues.size()) {
//            System.out.println("The size of x and y values are not same!");
//            return;
//        }
//
//        XYArrayDataset dataset = new XYArrayDataset(xValues, yValues, "S_1");
//        XY1DPlot plot = new XY1DPlot(dataset);
//
//        if (!style.isEmpty()) {
//            Color color = this.getColor(style);
//            PointStyle ps = this.getPointStyle(style);
//            LineStyles ls = this.getLineStyle(style);
//            if (ps != null) {
//                if (ls == null){
//                    plot.setChartPlotMethod(ChartPlotMethod.POINT);
//                    PointBreak pb = plot.getPointBreak(0);
//                    pb.setSize(8);
//                    pb.setStyle(this.getPointStyle(style));
//                    if (color != null) {
//                        pb.setColor(color);
//                    }
//                } else {
//                    plot.setChartPlotMethod(ChartPlotMethod.LINE_POINT);
//                    PolylineBreak plb = plot.getPolylineBreak(0);
//                    plb.setStyle(ls);
//                    plb.setDrawSymbol(true);
//                    plb.setSymbolStyle(ps);
//                    plb.setSymbolInterval(this.getSymbolInterval(xValues.size()));
//                    if (color != null) {
//                        plb.setColor(color);
//                        plb.setSymbolColor(color);
//                    }
//                }
//            } else {                                
//                plot.setChartPlotMethod(ChartPlotMethod.LINE);
//                PolylineBreak plb = plot.getPolylineBreak(0);
//                if (color != null) {
//                    plb.setColor(color);
//                }
//                if (ls != null){
//                    plb.setStyle(ls);
//                }
//            }
//        }
//
//        Chart chart = new Chart(plot);
//        chart.setAntiAlias(true);
//        this.charPanel.setChart(chart);
//        this.charPanel.paintGraphics();
//    }    
    
    private LineStyles getLineStyle(String style){
        LineStyles ls = null;
        if (style.contains("--")){
            ls = LineStyles.DASH;
        } else if (style.contains(":")){
            ls = LineStyles.DOT;
        } else if (style.contains("-.")) {
            ls = LineStyles.DASHDOT;
        } else if (style.contains("-")) {
            ls = LineStyles.SOLID;
        }
        
        return ls;
    }

    private PointStyle getPointStyle(String style) {
        PointStyle ps = null;
        if (style.contains("o")) {
            ps = PointStyle.Circle;
        } else if (style.contains("D")){
            ps = PointStyle.Diamond;
        } else if (style.contains("+")){
            ps = PointStyle.Plus;
        } else if (style.contains("s")){
            ps = PointStyle.Square;
        } else if (style.contains("*")){
            ps = PointStyle.StarLines;
        } else if (style.contains("^")){
            ps = PointStyle.UpTriangle;
        } else if (style.contains("x")){
            ps = PointStyle.XCross;
        }

        return ps;
    }

    private Color getColor(String style) {
        if (style.contains("r")) {
            return Color.red;
        } else if (style.contains("k")) {
            return Color.black;
        } else if (style.contains("b")) {
            return Color.blue;
        } else if (style.contains("g")) {
            return Color.green;
        } else if (style.contains("g")) {
            return Color.white;
        } else {
            return null;
        }
    }
    
    private int getSymbolInterval(int n){
        int i;
        int v = 20;
        if (n < v)
            i = 1;
        else {
            i = n / v;
        }
            
        return i;
    }
    
    /**
     * Set axis limits
     * @param limits Limits
     */
    public void axis(List<Number> limits){
        if (limits.size() == 4){
            double xmin = Double.parseDouble(limits.get(0).toString());
            double xmax = Double.parseDouble(limits.get(1).toString());
            double ymin = Double.parseDouble(limits.get(2).toString());
            double ymax = Double.parseDouble(limits.get(3).toString());
            XY1DPlot plot = (XY1DPlot)this.charPanel.getChart().getPlot();
            plot.setDrawExtent(new Extent(xmin, xmax, ymin, ymax));
            this.charPanel.paintGraphics();
        }
    }
    
    /**
     * Set y axis label
     * @param label Y axis label
     */
    public void ylabel(String label){
        XY1DPlot plot = (XY1DPlot)this.charPanel.getChart().getPlot();
        plot.getYAxis().setLabel(label);
        plot.getYAxis().setDrawLabel(true);
        this.charPanel.paintGraphics();
    }
    
    /**
     * Set y axis label
     * @param label Y axis label
     */
    public void xlabel(String label){
        XY1DPlot plot = (XY1DPlot)this.charPanel.getChart().getPlot();
        plot.getXAxis().setLabel(label);
        plot.getXAxis().setDrawLabel(true);
        this.charPanel.paintGraphics();
    }

    // </editor-fold>
    // <editor-fold desc="Form">
    /**
     * Show figure form
     */
    public void show() {        
        ChartForm form = new ChartForm(this.charPanel);
        form.setSize(600, 500);
        form.setLocationRelativeTo(null);
        form.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        form.setVisible(true);
        this.charPanel.paintGraphics();
    }    
    // </editor-fold>
    // <editor-fold desc="Data">
    /**
     * Get line space data list
     * @param min Minimum
     * @param max Maximum
     * @param n Number
     * @return Data list
     */
    public List<Double> linespace(double min, double max, int n){
        List<Double> values = new ArrayList<Double>();
        double delta = (max - min) / (n - 1);
        for (int i = 0; i < n; i++){
            values.add(min + delta * i);
        }
        
        return values;
    }
    
    /**
     * Get sine values
     * @param values Input values
     * @return Sine values
     */
    public List<Double> sin(List<Double> values){
        List<Double> rvalues = new ArrayList<Double>();
        for (Double v : values){
            rvalues.add(Math.sin(v));
        }
        
        return rvalues;
    }
    // </editor-fold>
    // </editor-fold>
}
