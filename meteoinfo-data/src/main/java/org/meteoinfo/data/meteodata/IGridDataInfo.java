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

import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;

 /**
  * Template
  *
  * @author Yaqiang Wang
  */
 public interface IGridDataInfo {

     // <editor-fold desc="Methods">

     /**
      * Get grid array
      *
      * @param varName Variable name
      * @return Grid array
      */
     public abstract GridArray getGridArray(String varName);

     /**
      * Read grid data - lon/lat
      *
      * @param timeIdx Time index
      * @param varName Variable name
      * @param levelIdx Level index
      * @return Grid data
      */
     public abstract GridData getGridData_LonLat(int timeIdx, String varName, int levelIdx);

     /**
      * Read grid data - time/lat
      *
      * @param lonIdx Longitude index
      * @param varName Variable name
      * @param levelIdx Level index
      * @return Grid data
      */
     public abstract GridData getGridData_TimeLat(int lonIdx, String varName, int levelIdx);

     /**
      * Read grid data - time/lon
      *
      * @param latIdx Latitude index
      * @param varName Variable name
      * @param levelIdx Level index
      * @return Grid data
      */
     public abstract GridData getGridData_TimeLon(int latIdx, String varName, int levelIdx);

     /**
      * Read grid data - level/lat
      *
      * @param lonIdx Longitude index
      * @param varName Variable name
      * @param timeIdx Time index
      * @return Grid data
      */
     public abstract GridData getGridData_LevelLat(int lonIdx, String varName, int timeIdx);

     /**
      * Read grid data - Level/lon
      *
      * @param latIdx Latitude index
      * @param varName Variable name
      * @param timeIdx Time index
      * @return Grid data
      */
     public abstract GridData getGridData_LevelLon(int latIdx, String varName, int timeIdx);

     /**
      * Read grid data - Level/time
      *
      * @param latIdx Latitude index
      * @param varName Variable name
      * @param lonIdx Longitude index
      * @return Grid data
      */
     public abstract GridData getGridData_LevelTime(int latIdx, String varName, int lonIdx);

     /**
      * Read grid data - time
      *
      * @param lonIdx Longitude index
      * @param latIdx Latitude index
      * @param varName Variable name
      * @param levelIdx Level index
      * @return Grid data
      */
     public abstract GridData getGridData_Time(int lonIdx, int latIdx, String varName, int levelIdx);

     /**
      * Read grid data - level
      *
      * @param lonIdx Longitude index
      * @param latIdx Latitude index
      * @param varName Variable name
      * @param timeIdx Time index
      * @return Grid data
      */
     public abstract GridData getGridData_Level(int lonIdx, int latIdx, String varName, int timeIdx);

     /**
      * Read grid data - longitude
      *
      * @param timeIdx Time index
      * @param latIdx Latitude index
      * @param varName Variable name
      * @param levelIdx Level index
      * @return Grid data
      */
     public abstract GridData getGridData_Lon(int timeIdx, int latIdx, String varName, int levelIdx);

     /**
      * Read grid data - latitude
      *
      * @param timeIdx Time index
      * @param lonIdx Longitude index
      * @param varName Variable name
      * @param levelIdx Level index
      * @return Grid data
      */
     public abstract GridData getGridData_Lat(int timeIdx, int lonIdx, String varName, int levelIdx);
     // </editor-fold>
 }
