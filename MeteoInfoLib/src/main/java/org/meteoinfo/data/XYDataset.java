/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.List;
import org.meteoinfo.global.Extent;

/**
 *
 * @author yaqiang
 */
public abstract class XYDataset extends Dataset{

    private double missingValue = -9999.0;
    
    /**
     * Get missing value
     * @return Missing value
     */
    public double getMissingValue(){
        return this.missingValue;
    }
    
    /**
     * Set missing value
     * @param value Missing value
     */
    public void setMissingValue(double value){
        this.missingValue = value;
    }
    
    /**
     * Get dataset type
     * @return Dataset type
     */
    @Override
    public DatasetType getDatasetType() {
        return DatasetType.XY;
    }
    
    /**
     * Get series count
     * @return Series count
     */
    public abstract int getSeriesCount();
    
    /**
     * Get item count
     * @return Item count
     */
    public abstract int getItemCount();
    
    /**
     * Get item count by series index
     * @param seriesIdx Series index
     * @return Item count
     */
    public abstract int getItemCount(int seriesIdx);
    
    /**
     * Get x value
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @return X value
     */
    public abstract double getX(int seriesIdx, int itemIdx);
    
    /**
     * Get y value
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @return Y value
     */
    public abstract double getY(int seriesIdx, int itemIdx);
    
    /**
     * Set x value
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @param value X value
     */
    public abstract void setX(int seriesIdx, int itemIdx, double value);
    
    /**
     * Set y value
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @param value Y value
     */
    public abstract void setY(int seriesIdx, int itemIdx, double value);        
    
    /**
     * Get x values
     * @param seriesIdx Series index
     * @return X values
     */
    public abstract double[] getXValues(int seriesIdx);
    
    /**
     * Get y values
     * @param seriesIdx Series index
     * @return Y values
     */
    public abstract double[] getYValues(int seriesIdx);
    
    /**
     * Get series key by index
     * @param seriesIdx Series index
     * @return Series key
     */
    public abstract String getSeriesKey(int seriesIdx);
    
    /**
     * Set series key by index
     * @param seriesIdx Series index
     * @param seriesKey  Series key
     */
    public abstract void setSeriesKey(int seriesIdx, String seriesKey);
    
    /**
     * Get series keys
     * @return Series keys
     */
    public abstract List<String> getSeriesKeys();
    
    /**
     * Set series keys
     * @param keys Keys
     */
    public abstract void setSeriesKeys(List<String> keys);
    
    /**
     * Get data extent
     * @return Data extent
     */
    public abstract Extent getDataExtent();
    
    /**
     * Get missing value index list
     * @param seriesIdx Series index
     * @return Missing value index list
     */
    public abstract List<Integer> getMissingValueIndex(int seriesIdx);
    
    /**
     * Select data points
     * @param extent Selection extent
     * @return Selected data points
     */
    public abstract List<int[]> selectPoints(Extent extent);
    
}
