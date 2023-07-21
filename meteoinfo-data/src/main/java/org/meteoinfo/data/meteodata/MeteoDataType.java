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

        GRADS_GRID,
        GRADS_STATION,
        MICAPS_1,
        MICAPS_2,
        MICAPS_3,
        MICAPS_4,
        MICAPS_7,
        MICAPS_11,
        MICAPS_13,
        MICAPS_120,
        MICAPS_131,
        MICAPS_MDFS,
        HYSPLIT_CONC,
        HYSPLIT_PARTICLE,
        HYSPLIT_TRAJ,
        ARL_GRID,
        NETCDF,
        HDF,
        ASCII_GRID,
        SURFER_GRID,
        SYNOP,
        METAR,
        ISH,
        LON_LAT_STATION,
        GRIB1,
        GRIB2,
        AWX,
        HRIT,
        MM5,
        MM5IM,
        GEOTIFF,
        BIL,
        RADAR,
        NULL;
        
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
                case GRADS_GRID:
                case GRADS_STATION:
                    return true;
                default:
                    return false;
            }
        }
}
