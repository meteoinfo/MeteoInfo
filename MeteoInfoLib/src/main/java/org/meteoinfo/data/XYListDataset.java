/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import ucar.ma2.Array;

/**
 *
 * @author wyq
 */
public class XYListDataset extends XYDataset {
    // <editor-fold desc="Variables">
    //private List<double[]> xValues;
    //private List<double[]> yValues;
    //private List<String> seriesKeys;
    private List<XYSeriesData> dataset;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XYListDataset(){
        //xValues = new ArrayList<>();
        //yValues = new ArrayList<>();
        //seriesKeys = new ArrayList<>();
        dataset = new ArrayList<>();
    }
    
    /**
     * Constructor
     *
     * @param seriesNum Series number
     * @param itemNum Item number
     */
    public XYListDataset(int seriesNum, int itemNum) {      
        this();
        
        for (int i = 0; i < seriesNum; i++){
            //xValues.add(new double[itemNum]);
            //yValues.add(new double[itemNum]);
            //seriesKeys.add("");
            dataset.add(new XYSeriesData());
        }
    }
    
    /**
     * Constructor
     * @param xdata X station data
     * @param ydata Y station data
     * @param seriesKey Series key
     */
    public XYListDataset(StationData xdata, StationData ydata, String seriesKey){
        this();
        List<double[]> vdata = new ArrayList<>();
        double v1, v2;
        for (int i = 0; i < xdata.getStNum(); i++) {
            v1 = xdata.getValue(i);
            if (MIMath.doubleEquals(v1, xdata.missingValue)) {
                continue;
            }
            v2 = ydata.getValue(i);
            if (MIMath.doubleEquals(v2, ydata.missingValue)) {
                continue;
            }
            vdata.add(new double[]{v1, v2});
        }
        
        int n = vdata.size();
        double[] xvs = new double[n];
        double[] yvs = new double[n];
        for (int i = 0; i < n; i++){
            xvs[i] = vdata.get(i)[0];
            yvs[i] = vdata.get(i)[1];
        }
        //this.xValues.add(xvs);
        //this.yValues.add(yvs);
        //this.seriesKeys.add(seriesKey);
        XYSeriesData sdata = new XYSeriesData(seriesKey, xvs, yvs);
        this.dataset.add(sdata);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    /**
     * Get series data
     * @param seriesIdx Series index
     * @return Series data
     */
    public XYSeriesData getSeriesData(int seriesIdx){
        return this.dataset.get(seriesIdx);
    }
   
    @Override
    public int getSeriesCount() {
        //return this.xValues.size();
        return this.dataset.size();
    }
    
    @Override
    public String getSeriesKey(int seriesIdx) {
        //return this.seriesKeys.get(seriesIdx);
        return this.dataset.get(seriesIdx).getKey();
    }
    
    /**
     * Set series key by index
     * @param seriesIdx Series index
     * @param seriesKey Series key
     */
    @Override
    public void setSeriesKey(int seriesIdx, String seriesKey){
        //this.seriesKeys.set(seriesIdx, seriesKey);
        this.dataset.get(seriesIdx).setKey(seriesKey);
    }
    
    /**
     * Get series keys
     * @return Series keys
     */
    @Override
    public List<String> getSeriesKeys(){
        //return this.seriesKeys;
        List<String> keys = new ArrayList<>();
        for (XYSeriesData d :this.dataset){
            keys.add(d.getKey());
        }
        return keys;
    }
    
    /**
     * Set series keys
     * @param value Series keys
     */
    @Override
    public void setSeriesKeys(List<String> value){
        //this.seriesKeys = value;
        int i = 0;
        for (XYSeriesData d :this.dataset){
            d.setKey(value.get(i));
            i++;
        }
    }
    
    @Override
    public int getItemCount(){
        int n = this.getItemCount(0);
        if (this.getSeriesCount() > 1){
            for (int i = 1; i < this.getSeriesCount(); i++){
                int nn = this.getItemCount(i);
                if (n < nn)
                    n = nn;
            }
        }
        
        return n;
    }

    @Override
    public int getItemCount(int seriesIdx) {        
        //return this.xValues.get(seriesIdx).length;
        return this.dataset.get(seriesIdx).dataLength();
    }
    
    @Override
    public double[] getXValues(int seriesIdx){
        //return this.xValues.get(seriesIdx);
        return this.dataset.get(seriesIdx).getXdata();
    }
    
    @Override
    public double[] getYValues(int seriesIdx){
        //return this.yValues.get(seriesIdx);
        return this.dataset.get(seriesIdx).getYdata();
    }

    @Override
    public double getX(int seriesIdx, int itemIdx) {
        //return this.xValues.get(seriesIdx)[itemIdx];
        return this.dataset.get(seriesIdx).getXdata()[itemIdx];
    }

    @Override
    public double getY(int seriesIdx, int itemIdx) {
        //return this.yValues.get(seriesIdx)[itemIdx];
        return this.dataset.get(seriesIdx).getYdata()[itemIdx];
    }
    
    @Override
    public void setX(int seriesIdx, int itemIdx, double value){
        //this.xValues.get(seriesIdx)[itemIdx] = value;
        this.dataset.get(seriesIdx).getXdata()[itemIdx] = value;
    }
    
    @Override
    public void setY(int seriesIdx, int itemIdx, double value){
        //this.yValues.get(seriesIdx)[itemIdx] = value;
        this.dataset.get(seriesIdx).getYdata()[itemIdx] = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a series data
     * @param sdata Series data
     */
    public void addSeries(XYSeriesData sdata){
        this.dataset.add(sdata);
    }
    
    /**
     * Add a series data 
     * @param seriesKey Series key
     * @param xvs X value array
     * @param yvs Y value array
     */
    public void addSeries(String seriesKey, double[] xvs, double[] yvs){
        //this.seriesKeys.add(seriesKey);
        //this.xValues.add(xvs);
        //this.yValues.add(yvs);
        XYSeriesData sdata = new XYSeriesData(seriesKey, xvs, yvs);
        this.dataset.add(sdata);
    }
    
    /**
     * Add a series data 
     * @param seriesKey Series key
     * @param xvs X value array
     * @param yvs Y value array
     */
    public void addSeries(String seriesKey, List<Number> xvs, List<Number> yvs){
        double[] nxvs = new double[xvs.size()];
        double[] nyvs = new double[yvs.size()];
        double v;
        for (int i = 0; i < xvs.size(); i++){
            v = xvs.get(i).doubleValue();
            if (Double.isNaN(v))
                nxvs[i] = this.getMissingValue();
            else
                nxvs[i] = xvs.get(i).doubleValue();
        }
        for (int i = 0; i < yvs.size(); i++){
            v = yvs.get(i).doubleValue();
            if (Double.isNaN(v))
                nyvs[i] = this.getMissingValue();
            else
                nyvs[i] = v;
        }
        
        this.addSeries(seriesKey, nxvs, nyvs);
    }
    
   /**
     * Add a series data 
     * @param seriesKey Series key
     * @param xvs X value array
     * @param yvs Y value array
     */
    public void addSeries(String seriesKey, List<Number> xvs, Array yvs){
        int xn = (int)xvs.size();
        int yn = (int)yvs.getSize();
        double[] nxvs = new double[xn];
        double[] nyvs = new double[yn];
        double v;
        for (int i = 0; i < xn; i++)
            nxvs[i] = xvs.get(i).doubleValue();
        for (int i = 0; i < yn; i++) {
            v = yvs.getDouble(i);
            if (Double.isNaN(v))
                nyvs[i] = this.getMissingValue();
            else
                nyvs[i] = v;
        }
        
        this.addSeries(seriesKey, nxvs, nyvs);
    } 
    
    /**
     * Add a series data 
     * @param seriesKey Series key
     * @param xvs X value array
     * @param yvs Y value array
     */
    public void addSeries(String seriesKey, Array xvs, Array yvs){
        int xn = (int)xvs.getSize();
        int yn = (int)yvs.getSize();
        double[] nxvs = new double[xn];
        double[] nyvs = new double[yn];
        double v;
        for (int i = 0; i < xn; i++)
            nxvs[i] = xvs.getDouble(i);
        for (int i = 0; i < yn; i++){
            v = yvs.getDouble(i);
            if (Double.isNaN(v))
                nyvs[i] = this.getMissingValue();
            else
                nyvs[i] = v;
        }
        
        this.addSeries(seriesKey, nxvs, nyvs);
    }
    
    /**
     * Add a series data 
     * @param seriesKey Series key
     * @param xvs X value array
     * @param yvs Y value array
     */
    public void addSeries(String seriesKey, Array xvs, List<Number> yvs){
        int xn = (int)xvs.getSize();
        int yn = yvs.size();
        double[] nxvs = new double[xn];
        double[] nyvs = new double[yn];
        double v;
        for (int i = 0; i < xn; i++)
            nxvs[i] = xvs.getDouble(i);
        for (int i = 0; i < yn; i++){
            v = yvs.get(i).doubleValue();
            if (Double.isNaN(v))
                nyvs[i] = this.getMissingValue();
            else
                nyvs[i] = v;
        }
        
        this.addSeries(seriesKey, nxvs, nyvs);
    }
    
    /**
     * Remove a series data
     * @param seriesIdx Series data
     */
    public void removeSeries(int seriesIdx){
        //this.seriesKeys.remove(seriesIdx);
        //this.xValues.remove(seriesIdx);
        //this.yValues.remove(seriesIdx);
        this.dataset.remove(seriesIdx);
    }
    
    /**
     * Remove a series data
     * @param seriesKey Series key
     */
    public void removeSeries(String seriesKey){
        List<String> keys = this.getSeriesKeys();
        int idx = keys.indexOf(seriesKey);
        if (idx >= 0){
            this.removeSeries(idx);
        }
    }
    
    /**
     * Get data extent
     * @return Data extent
     */
    @Override
    public Extent getDataExtent() {
        Extent cET = new Extent();
        double xmin, xmax, ymin, ymax;
        int n = 0;
        for (int i = 0; i < this.getSeriesCount(); i++) {
            XYSeriesData sdata = this.dataset.get(i);
            for (int j = 0; j < this.getItemCount(i); j++) {                
                xmin = sdata.getX_min(j);
                xmax = sdata.getX_max(j);
                ymin = sdata.getY_min(j);
                ymax = sdata.getY_max(j);
                if (Double.isNaN(sdata.getX(j)) || Double.isNaN(sdata.getY(j)))
                    continue;
                if (MIMath.doubleEquals(sdata.getX(j), this.getMissingValue()) || MIMath.doubleEquals(sdata.getY(j), this.getMissingValue()))
                    continue;
                if (n == 0) {
                    cET.minX = xmin;
                    cET.maxX = xmax;
                    cET.minY = ymin;
                    cET.maxY = ymax;
                } else {
                    if (cET.minX > xmin) {
                        cET.minX = xmin;
                    } else if (cET.maxX < xmax) {
                        cET.maxX = xmax;
                    }

                    if (cET.minY > ymin) {
                        cET.minY = ymin;
                    } else if (cET.maxY < ymax) {
                        cET.maxY = ymax;
                    }
                }
                n ++;
            }
        }

        return cET;
    }
    
    /**
     * Get missing value index list
     * @param seriesIdx Series index
     * @return Missing value index list
     */
    @Override
    public List<Integer> getMissingValueIndex(int seriesIdx){
        List<Integer> mvidx = new ArrayList<>();
        double[] xvs = this.getXValues(seriesIdx);
        double[] yvs = this.getYValues(seriesIdx);
        for (int i = 0; i < yvs.length; i++){
            if (MIMath.doubleEquals(xvs[i], this.getMissingValue()) || MIMath.doubleEquals(yvs[i], this.getMissingValue()))
                mvidx.add(i);
        }
        
        return mvidx;
    }
    
    /**
     * Select data points
     * @param extent Selection extent
     * @return Selected data points
     */
    @Override
    public List<int[]> selectPoints(Extent extent){
        List<int[]> selIdxs = new ArrayList<>();
        double x, y;
        for (int i = 0; i < this.getSeriesCount(); i++){
            for (int j = 0; j < this.getItemCount(i); j++){
                x = this.getX(i, j);
                if (x >= extent.minX && x <= extent.maxX){
                    y = this.getY(i, j);
                    if (y >= extent.minY && y <= extent.maxY){
                        selIdxs.add(new int[]{i, j});
                    }
                }                    
            }
        }
        
        return selIdxs;
    }
    // </editor-fold>               
}
