/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;

/**
 *
 * @author wyq
 */
public class XYArrayDataset extends XYDataset {

    // <editor-fold desc="Variables">

    private final double[][] xValues;
    private final double[][] yValues;
    private final int seriesCount;
    private final int itemCount;
    private String[] seriesKeys;

    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param seriesNum Series number
     * @param itemNum Item number
     */
    public XYArrayDataset(int seriesNum, int itemNum) {
        this.seriesCount = seriesNum;
        this.itemCount = itemNum;
        xValues = new double[seriesNum][itemNum];
        yValues = new double[seriesNum][itemNum];
        seriesKeys = new String[seriesNum];
    }

    /**
     * Constructor
     *
     * @param xdata X station data
     * @param ydata Y station data
     * @param seriesKey Series key
     */
    public XYArrayDataset(StationData xdata, StationData ydata, String seriesKey) {
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
        seriesCount = 1;
        seriesKeys = new String[seriesCount];
        seriesKeys[0] = seriesKey;
        itemCount = vdata.size();
        xValues = new double[seriesCount][itemCount];
        yValues = new double[seriesCount][itemCount];
        for (int i = 0; i < seriesCount; i++) {
            for (int j = 0; j < itemCount; j++) {
                xValues[i][j] = vdata.get(j)[0];
                yValues[i][j] = vdata.get(j)[1];
            }
        }
    }
    
    /**
     * Constructor
     *
     * @param xdata X data
     * @param ydata Y data
     * @param seriesKey Series key
     */
    public XYArrayDataset(List<Number> xdata, List<Number> ydata, String seriesKey) {
        List<Double> nxdata = new ArrayList<>();
        List<Double> nydata = new ArrayList<>();
        for (int i = 0; i < xdata.size(); i++) {
            nxdata.add(Double.parseDouble(xdata.get(i).toString()));
            nydata.add(Double.parseDouble(ydata.get(i).toString()));
        }
        
        List<double[]> vdata = new ArrayList<>();
        double v1, v2;
        for (int i = 0; i < xdata.size(); i++) {
            v1 = nxdata.get(i);            
            v2 = nydata.get(i);
            vdata.add(new double[]{v1, v2});
        }
        seriesCount = 1;
        seriesKeys = new String[seriesCount];
        seriesKeys[0] = seriesKey;
        itemCount = vdata.size();
        xValues = new double[seriesCount][itemCount];
        yValues = new double[seriesCount][itemCount];
        for (int i = 0; i < seriesCount; i++) {
            for (int j = 0; j < itemCount; j++) {
                xValues[i][j] = vdata.get(j)[0];
                yValues[i][j] = vdata.get(j)[1];
            }
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get series count
     * @return Series count
     */
    @Override
    public int getSeriesCount() {
        return this.seriesCount;
    }

    /**
     * Get series key by index
     * @param seriesIdx Series index
     * @return Series key
     */
    @Override
    public String getSeriesKey(int seriesIdx) {
        return seriesKeys[seriesIdx];
    }
    
    /**
     * Set series key by index
     * @param seriesIdx Series index
     * @param seriesKey Series key
     */
    @Override
    public void setSeriesKey(int seriesIdx, String seriesKey){
        this.seriesKeys[seriesIdx] = seriesKey;
    }

    /**
     * Get item count
     * @return Item count
     */
    @Override
    public int getItemCount() {
        return this.itemCount;
    }
    
    /**
     * Get item count
     * @param seriesIdx Series index
     * @return Item count
     */
    @Override
    public int getItemCount(int seriesIdx){
        return this.itemCount;
    }
    
    /**
     * Get x values
     * @param seriesIdx Series index
     * @return X values
     */
    @Override
    public double[] getXValues(int seriesIdx){
        return this.xValues[seriesIdx];
    }
    
    /**
     * Get y values
     * @param seriesIdx Series index
     * @return Y values
     */
    @Override
    public double[] getYValues(int seriesIdx){
        return this.yValues[seriesIdx];
    }

    /**
     * Get x value
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @return X value
     */
    @Override
    public double getX(int seriesIdx, int itemIdx) {
        return xValues[seriesIdx][itemIdx];
    }
    
    /**
     * Set x value
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @param value X value
     */
    @Override
    public void setX(int seriesIdx, int itemIdx, double value){
        xValues[seriesIdx][itemIdx] = value;
    }

    /**
     * Get Y value
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @return Y value
     */
    @Override
    public double getY(int seriesIdx, int itemIdx) {
        return yValues[seriesIdx][itemIdx];
    }
    
    /**
     * Set Y value
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @param value Y value
     */
    @Override
    public void setY(int seriesIdx, int itemIdx, double value){
        yValues[seriesIdx][itemIdx] = value;
    }
    
    /**
     * Get series keys
     * @return Series keys
     */    
    @Override
    public List<String> getSeriesKeys(){
        return Arrays.asList(this.seriesKeys);
    }
    
    /**
     * Set series keys
     * @param value Series keys
     */
    public void setSeriesKeys(String[] value){
        this.seriesKeys = value;
    }
    
    /**
     * Set series keys
     * @param value Series keys
     */
    @Override
    public void setSeriesKeys(List<String> value){
        this.seriesKeys = (String[])value.toArray(new String[value.size()]);
    }
        
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get data extent
     * @return Data extent
     */
    @Override
    public Extent getDataExtent() {
        Extent cET = new Extent();
        double x, y;
        int n = 0;
        for (int i = 0; i < this.seriesCount; i++) {
            for (int j = 0; j < this.itemCount; j++) {                
                x = xValues[i][j];
                y = yValues[i][j];
                if (MIMath.doubleEquals(y, this.getMissingValue()) || MIMath.doubleEquals(x, this.getMissingValue()))
                    continue;
                if (n == 0) {
                    cET.minX = x;
                    cET.maxX = x;
                    cET.minY = y;
                    cET.maxY = y;
                } else {
                    if (cET.minX > x) {
                        cET.minX = x;
                    } else if (cET.maxX < x) {
                        cET.maxX = x;
                    }

                    if (cET.minY > y) {
                        cET.minY = y;
                    } else if (cET.maxY < y) {
                        cET.maxY = y;
                    }
                }
                n ++;
            }
        }

        return cET;
    }
    
    /**
     * Select data points
     * @param extent Selection extent
     * @return Selected data points
     */
    @Override
    public List<int[]> selectPoints(Extent extent){
        List<int[]> selIdxs = new ArrayList<int[]>();
        double x, y;
        for (int i = 0; i < this.seriesCount; i++){
            for (int j = 0; j < this.itemCount; j++){
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
    
    /**
     * Get missing value index list
     * @param seriesIdx Series index
     * @return Missing value index list
     */
    @Override
    public List<Integer> getMissingValueIndex(int seriesIdx){
        List<Integer> mvidx = new ArrayList<Integer>();
        double[] xvs = this.getXValues(seriesIdx);
        double[] yvs = this.getYValues(seriesIdx);
        for (int i = 0; i < this.itemCount; i++){
            if (MIMath.doubleEquals(xvs[i], this.getMissingValue()) || MIMath.doubleEquals(yvs[i], this.getMissingValue()))
                mvidx.add(i);
        }
        
        return mvidx;
    }
    // </editor-fold>               
}
