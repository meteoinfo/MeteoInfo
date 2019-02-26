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
import org.meteoinfo.chart.Chart;
import org.meteoinfo.chart.ChartPanel;
import org.meteoinfo.chart.plot.ChartPlotMethod;
import org.meteoinfo.chart.plot.XY1DPlot;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.XYArrayDataset;
import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.data.meteodata.DrawMeteoData;
import org.meteoinfo.data.meteodata.DrawType2D;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.meteoinfo.legend.PointStyle;
import org.meteoinfo.global.Extent;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layout.MapLayout;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.map.MapView;

/**
 *
 * @author yaqiang
 */
public class MeteoInfoData {
    // <editor-fold desc="Variables">
    private List<MeteoDataInfo> dataInfoList;
    private MeteoDataInfo currentDataInfo;    

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MeteoInfoData() {
        dataInfoList = new ArrayList<MeteoDataInfo>();        
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

    // </editor-fold>    
    // <editor-fold desc="Meteo Data">
    /**
     * Get grid data
     * @param varName Variable name
     * @return Grid data
     */
    public GridData getGridData(String varName){
        return this.currentDataInfo.getGridData(varName);
    }
    // </editor-fold>
    // <editor-fold desc="Other Data">
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
