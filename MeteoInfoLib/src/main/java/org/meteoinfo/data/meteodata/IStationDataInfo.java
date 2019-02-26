 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.meteodata;

import org.meteoinfo.data.StationData;

/**
 * Template
 * 
 * @author Yaqiang Wang
 */
public interface IStationDataInfo {
    
    // <editor-fold desc="Methods">
        
    /**
     * Read station data
     *
     * @param timeIdx Time index
     * @param varIdx Variable index
     * @param levelIdx Level index
     * @return Station data
     */
    public abstract StationData getStationData(int timeIdx, int varIdx, int levelIdx);
    
    /**
     * Read station info data
     *
     * @param timeIdx Time index    
     * @param levelIdx Level index
     * @return Station info data
     */
    public abstract StationInfoData getStationInfoData(int timeIdx, int levelIdx);
    
    /**
     * Read station model data
     *
     * @param timeIdx Time index    
     * @param levelIdx Level index
     * @return Station model data data
     */
    public abstract StationModelData getStationModelData(int timeIdx, int levelIdx);
       
    // </editor-fold>
}
