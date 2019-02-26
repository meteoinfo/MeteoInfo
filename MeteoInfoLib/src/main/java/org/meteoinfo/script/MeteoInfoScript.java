/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.script;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.WindowConstants;
import org.meteoinfo.chart.ChartPanel;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.plot.XY1DPlot;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.data.meteodata.DimensionSet;
import org.meteoinfo.data.meteodata.DrawMeteoData;
import org.meteoinfo.data.meteodata.DrawType2D;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.meteoinfo.data.meteodata.PlotDimension;
import org.meteoinfo.legend.PointStyle;
import org.meteoinfo.global.Extent;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layout.MapLayout;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.map.MapView;

/**
 *
 * @author yaqiang
 */
public class MeteoInfoScript {

    // <editor-fold desc="Variables">
    private boolean batchMode;
    private List<MeteoDataInfo> dataInfoList;
    private MeteoDataInfo currentDataInfo;
    private MapLayout mapLayout;
    private ChartPanel chartPanel;
    private DrawType2D drawType2D;
    private PlotDimension plotDimension;
    private String startUpPath;
    private boolean isMap;
    private DimensionSet dimensionSet;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param path Start up path
     */
    public MeteoInfoScript(String path) {
        this.startUpPath = path;
        this.batchMode = true;
        this.mapLayout = new MapLayout();        
        this.chartPanel = new ChartPanel(null);
        dataInfoList = new ArrayList<MeteoDataInfo>();
        drawType2D = DrawType2D.Contour;
        this.plotDimension = PlotDimension.Lat_Lon;
        this.isMap = true;
        
        //Add default map layer
        //String fn = path + File.separator + "map" + File.separator + "country1.shp";
        String fn = "D:/Temp/map/country1.shp";
        if (new File(fn).exists()){
            try {
                MapLayer layer = MapDataManage.loadLayer(fn);
                PolygonBreak pgb = (PolygonBreak)layer.getLegendScheme().getLegendBreaks().get(0);
                pgb.setDrawFill(false);                
                MapFrame mapFrame = mapLayout.getActiveMapFrame();
                MapView mapView = mapFrame.getMapView();
                mapView.setLockViewUpdate(true);
                mapFrame.addLayer(layer);
                mapFrame.setGridXDelt(60);
                mapFrame.setGridYDelt(30);
                mapLayout.getActiveLayoutMap().zoomToExtentLonLatEx(mapView.getLayersWholeExtent());
                mapView.setLockViewUpdate(false);
            } catch (Exception ex) {
                Logger.getLogger(MeteoInfoScript.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="MeteoDataInfo">
    /**
     * Add a meteo data info
     *
     * @param aDataInfo The meteo data info
     */
    public void addMeteoData(MeteoDataInfo aDataInfo) {
        dataInfoList.add(aDataInfo);
        currentDataInfo = aDataInfo;        
    }

    /**
     * Open GrADS data file
     *
     * @param fileName GrADS data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openGrADSData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openGrADSData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open ARL data file
     *
     * @param fileName ARL data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openARLData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openARLData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open ASCII grid data file
     *
     * @param fileName ASCII grid data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openASCIIGridData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openASCIIGridData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open Surfer grid data file
     *
     * @param fileName Surfer grid data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openSurferGridData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openSurferGridData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open HYSPLIT concentration data file
     *
     * @param fileName HYSPLIT concentration data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openHYSPLITConcData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openHYSPLITConcData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open HYSPLIT particle data file
     *
     * @param fileName HYSPLIT particle data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openHYSPITPartData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openHYSPLITPartData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open HYSPLIT trajectory data file
     *
     * @param fileName HYSPLIT trajectory data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openHYSPLITTrajData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openHYSPLITTrajData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

//    /**
//     * Open HYSPLIT trajectory data files
//     *
//     * @param fileNames HYSPLIT trajectory data file names
//     * @return MeteoDataInfo
//     */
//    public MeteoDataInfo openHYSPLITTrajData(String[] fileNames) {
//        MeteoDataInfo aDataInfo = new MeteoDataInfo();
//        aDataInfo.openHYSPLITTrajData(fileNames);
//        addMeteoData(aDataInfo);
//
//        return aDataInfo;
//    }

    /**
     * Open NetCDF, GRIB, HDF... data file
     *
     * @param fileName NetCDF, GRIB, HDF... data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openNetCDFData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openNetCDFData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open Lon/Lat station data file
     *
     * @param fileName Lon/Lat station data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openLonLatData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openLonLatData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open MICAPS data file
     *
     * @param fileName MICAPS data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openMICAPSData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openMICAPSData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open MM5 output data file
     *
     * @param fileName MM5 output data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openMM5Data(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openMM5Data(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open MM5 intermedia data file
     *
     * @param fileName MM5 intermedia data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openMM5IMData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openMM5IMData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }
    
    /**
     * Set plot dimension
     * @param pdStr Plot dimension
     */
    public void setPlotDimension(String pdStr){
        PlotDimension pd = null;
        if (pdStr.equalsIgnoreCase("lat_lon"))
            pd = PlotDimension.Lat_Lon;
        
        if (pd != null)
            this.currentDataInfo.setDimensionSet(pd);
    }

    // </editor-fold>
    // <editor-fold desc="Common">
    public void title(String title){
        if (this.isMap){
            this.mapLayout.addText(title, 280, 20, 16);
            this.mapLayout.paintGraphics();
        } else {
            this.chartPanel.getChart().setTitle(new ChartText(title));
            this.chartPanel.paintGraphics();
        }
    }
    // </editor-fold>
    // <editor-fold desc="Display">        
    /**
     * Dislay
     *
     * @param varName Variable name
     */
    public void display(String varName) {
        if (!this.isMap)
            this.map();
        
        if (currentDataInfo.isGridData()) {
            GridData gdata = this.currentDataInfo.getGridData(varName);
            MapLayer layer = DrawMeteoData.createContourLayer(gdata, varName, varName);
            if (mapLayout == null) {
                mapLayout = new MapLayout();
            }

            MapFrame mapFrame = mapLayout.getActiveMapFrame();
            MapView mapView = mapFrame.getMapView();
            mapView.setLockViewUpdate(true);
            mapFrame.addLayer(layer);
            mapLayout.getActiveLayoutMap().zoomToExtentLonLatEx(mapView.getMeteoLayersExtent());
            mapView.setLockViewUpdate(false);
            mapLayout.paintGraphics();
        }
    }

    // </editor-fold>
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
//
//    /**
//     * Plot
//     *
//     * @param xValues X values
//     * @param yValues Y values
//     */
//    public void plot(List<Number> xValues, List<Number> yValues) {
//        this.plot(xValues, yValues, "");
//    }
//
//    /**
//     * Plot
//     *
//     * @param xValues X values
//     * @param yValues Y values
//     * @param style Plot style
//     */
//    public void plot(List<Number> xValues, List<Number> yValues, String style) {
//        if (this.isMap)
//            this.figure();
//            
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
//                    pb.setStyle(ps);
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
//        this.chartPanel.setChart(chart);
//        this.chartPanel.paintGraphics();
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
        } else if (style.contains("w")) {
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
            XY1DPlot plot = (XY1DPlot)this.chartPanel.getChart().getPlot();
            plot.setDrawExtent(new Extent(xmin, xmax, ymin, ymax));
            this.chartPanel.paintGraphics();
        }
    }
    
    /**
     * Set y axis label
     * @param label Y axis label
     */
    public void ylabel(String label){
        XY1DPlot plot = (XY1DPlot)this.chartPanel.getChart().getPlot();
        plot.getYAxis().setLabel(label);
        plot.getYAxis().setDrawLabel(true);
        this.chartPanel.paintGraphics();
    }
    
    /**
     * Set y axis label
     * @param label Y axis label
     */
    public void xlabel(String label){
        XY1DPlot plot = (XY1DPlot)this.chartPanel.getChart().getPlot();
        plot.getXAxis().setLabel(label);
        plot.getXAxis().setDrawLabel(true);
        this.chartPanel.paintGraphics();
    }

    // </editor-fold>
    // <editor-fold desc="Form">
    /**
     * Show figure form
     */
    public void showfigure() {        
        ChartForm form = new ChartForm(this.chartPanel);
        form.setSize(600, 500);
        form.setLocationRelativeTo(null);
        form.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        form.setVisible(true);
        this.chartPanel.paintGraphics();
    }
    
    /**
     * Show map form
     */
    public void show(){
        if (this.isMap)
            this.showmap();
        else
            this.showfigure();
    }
    
    /**
     * Show map or figure form
     * @param i I
     */
    public void show(int i){
        if (i == 0){
            this.showmap();
        } else 
            this.showfigure();
    }

    /**
     * Switch to map mode
     */
    public void map(){
        this.isMap = true;
        System.out.println("Switch to map mode");
    }
    
    /**
     * Switch to figure mode
     */
    public void figure() {
        this.isMap = false;
        System.out.println("Switch to figure mode");
    }

    /**
     * Create and show map figure form
     */
    public void showmap() {
        MapForm frame = new MapForm(this.mapLayout);
        frame.setSize(750, 540);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
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
