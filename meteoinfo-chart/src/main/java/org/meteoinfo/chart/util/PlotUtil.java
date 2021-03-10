/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.util;

import org.meteoinfo.chart.Chart;
import org.meteoinfo.chart.ChartPanel;
import org.meteoinfo.chart.plot.ChartPlotMethod;
import org.meteoinfo.chart.plot.XY1DPlot;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.XYArrayDataset;
import org.meteoinfo.data.XYDataset;
import org.meteoinfo.geometry.legend.PointBreak;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class PlotUtil {

    /**
     * Get XYDataset from two StationData
     *
     * @param xdata X station data
     * @param ydata Y station data
     * @param seriesKey Series key
     * @return XYDataset XYDataset
     */
    public static XYDataset getXYDataset(StationData xdata, StationData ydata, String seriesKey) {
        List<Number> xvs = new ArrayList<>();
        List<Number> yvs = new ArrayList<>();
        double x, y;
        int n = xdata.getStNum();
        for (int i = 0; i < n; i++) {
            x = xdata.data[i][2];
            if (MIMath.doubleEquals(x, xdata.missingValue)) {
                continue;
            }
            y = ydata.data[i][2];
            if (MIMath.doubleEquals(y, ydata.missingValue)) {
                continue;
            }
            xvs.add(x);
            yvs.add(y);
        }

        return new XYArrayDataset(xvs, yvs, seriesKey);
    }

    /**
     * Create scatter plot
     *
     * @param title Title
     * @param xAxisLabel X axis label
     * @param yAxisLabel Y axis label
     * @param dataset XYDataset
     * @return JFreeChart
     */
    public static Chart createScatterPlot(String title, String xAxisLabel, String yAxisLabel, XYDataset dataset) {
        XY1DPlot plot = new XY1DPlot(dataset);
        plot.setTitle(title);
        plot.setChartPlotMethod(ChartPlotMethod.POINT);
        PointBreak pb = new PointBreak();
        plot.setLegendBreak(0, pb);
        plot.getXAxis().setLabel(xAxisLabel);
        plot.getYAxis().setLabel(yAxisLabel);
        Chart chart = new Chart(plot, null);

        return chart;
    }

    /**
     * Save chart as PNG image file
     *
     * @param fileName The file name
     * @param chart The chart
     * @param width Width
     * @param height Heigth
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    public static void exportToPicture(String fileName, Chart chart, int width, int height)
            throws FileNotFoundException, InterruptedException {
        ChartPanel cp = new ChartPanel(chart);
        cp.setSize(width, height);
        cp.paintGraphics();
        cp.saveImage(fileName);
    }
}
