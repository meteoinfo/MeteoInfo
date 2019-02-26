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

/**
 *
 * @author Yaqiang Wang
 */
public enum MeteoDataType {
    /// <summary>
        /// GrADS grid
        /// </summary>
        GrADS_Grid,
        /// <summary>
        /// GrADS station
        /// </summary>
        GrADS_Station,
        /// <summary>
        /// MICAPS 1
        /// </summary>
        MICAPS_1,
        /// <summary>
        /// MICAPS 2
        /// </summary>
        MICAPS_2,
        /// <summary>
        /// MICAPS 3
        /// </summary>
        MICAPS_3,
        /// <summary>
        /// MICAPS 4
        /// </summary>
        MICAPS_4,
        /// <summary>
        /// MICAPS 7
        /// </summary>
        MICAPS_7,
        /// <summary>
        /// MICAPS 11
        /// </summary>
        MICAPS_11,
        /// <summary>
        /// MICAPS 13
        /// </summary>
        MICAPS_13,
        MICAPS_120,
        MICAPS_131,
        /// <summary>
        /// HYSPLIT concentration
        /// </summary>
        HYSPLIT_Conc,
        /// <summary>
        /// HYSPLIT particle
        /// </summary>
        HYSPLIT_Particle,
        /// <summary>
        /// HYSPLIT trajectory
        /// </summary>
        HYSPLIT_Traj,
        /// <summary>
        /// ARL grid
        /// </summary>
        ARL_Grid,
        /// <summary>
        /// NetCDF
        /// </summary>
        NetCDF,
        /// <summary>
        /// HDF
        /// </summary>
        HDF,
        /// <summary>
        /// ASCII grid
        /// </summary>
        ASCII_Grid,
        /// <summary>
        /// Sufer grid
        /// </summary>
        Sufer_Grid,
        /// <summary>
        /// SYNOP
        /// </summary>
        SYNOP,
        /// <summary>
        /// METAR
        /// </summary>
        METAR,
        /// <summary>
        /// NOAA ISH dataset
        /// </summary>
        ISH,
        /// <summary>
        /// Lon/Lat stations
        /// </summary>
        LonLatStation,
        /// <summary>
        /// GRIB edition 1
        /// </summary>
        GRIB1,
        /// <summary>
        /// GRIB edition 2
        /// </summary>
        GRIB2,
        /// <summary>
        /// AWX - FY satellite data format
        /// </summary>
        AWX,
        /// <summary>
        /// HRIT/LRIT satellite data format
        /// </summary>
        HRIT,
        MM5,
        MM5IM,
        GEOTIFF,
        BIL;
        
        /**
         * If is MICAPS data
         * @return Is or not MICAPS data
         */
        public boolean isMICAPS(){
            switch (this){
                case MICAPS_1:
                case MICAPS_2:
                case MICAPS_3:
                case MICAPS_4:
                case MICAPS_7:
                case MICAPS_11:
                case MICAPS_13:
                case MICAPS_120:
                    return true;
                default:
                    return false;
            }
        }
        
        /**
         * If is GrADS data
         * @return Is or not GrADS data
         */
        public boolean isGrADS() {
            switch(this){
                case GrADS_Grid:
                case GrADS_Station:
                    return true;
                default:
                    return false;
            }
        }
}
