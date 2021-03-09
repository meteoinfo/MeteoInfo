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
package org.meteoinfo.data.meteodata.arl;

/**
 *
 * @author yaqiang
 */
public class DataHead {
    /// <summary>
        /// MODEL - Dada source
        /// </summary>
        public String MODEL;
        /// <summary>
        /// Forecast hour (>99 the header forecast hr = 99)
        /// </summary>
        public int ICX;
        /// <summary>
        /// Minutes associated with data time
        /// </summary>
        public short MN;
        /// <summary>
        /// Standard conformal projections are drawn around a central (polar) point. 
        /// A Normal Projection is one where this point is either the North Pole (latitude = +90.°) 
        /// or the South Pole (latitude = -90.°). 
        /// </summary>
        public float POLE_LAT;
        /// <summary>
        /// Polar longitude
        /// </summary>
        public float POLE_LON;
        /// <summary>
        /// Reference latitude is the angle at which the cone of a Lambert Conformal projection is tangent to the Earth
        /// </summary>
        public float REF_LAT;
        /// <summary>
        /// Reference longitude is the longitude furthest from the cut
        /// </summary>
        public float REF_LON;
        /// <summary>
        /// At reference longitude and latitude point,
        /// the scale (gridsize in km.) and orientation (degrees between local North and the y-axis) 
        /// have the specified values
        /// </summary>
        public float SIZE;
        /// <summary>
        /// Orientation
        /// </summary>
        public float ORIENT;
        /// <summary>
        /// TANG_LAT: Stereographic Projections are commonly drawn on a plane tangent to the North or South Pole,
        /// and Mercator projections on a cylinder parallel to the axis between the two poles
        /// </summary>
        public float TANG_LAT;
        /// <summary>
        /// SYNC_XP
        /// </summary>
        public float SYNC_XP;
        /// <summary>
        /// SYNC_YP
        /// </summary>
        public float SYNC_YP;
        /// <summary>
        /// SYNC_LAT: To align (synchronize) the grid coordinates with the map, 
        /// we specify the latitude and longitude coordinates of a specific grid point, 
        /// which could be the origin or could be the midpoint of the grid, 
        /// or any other point in the grid
        /// </summary>
        public float SYNC_LAT;
        /// <summary>
        /// SYNU_LON
        /// </summary>
        public float SYNC_LON;
        /// <summary>
        /// DUMMY
        /// </summary>
        public float DUMMY;
        /// <summary>
        /// X number
        /// </summary>
        public int NX;
        /// <summary>
        /// Y number
        /// </summary>
        public int NY;
        /// <summary>
        /// Z number - levels
        /// </summary>
        public int NZ;
        /// <summary>
        /// Vertical coordinate system flag
        //1-sigma (fraction)
        //2-pressure (mb)
        //3-terrain (fraction)
        //4-hybrid (mb: offset.fraction)
        /// </summary>
        public short K_FLAG;
        /// <summary>
        /// Length in bytes of the index record, excluding the first 50 bytes
        /// </summary>
        public int LENH;
}
