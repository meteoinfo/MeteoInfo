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

import java.time.LocalDateTime;

/**
 *
 * @author yaqiang
 */
public class TrajectoryInfo {
    /// <summary>
        /// Trajectory name
        /// </summary>
        public String trajName;
        /// <summary>
        /// Trajectory identifer
        /// </summary>
        public String trajID;
        /// <summary>
        /// Trajectory center
        /// </summary>
        public String trajCenter;
        /// <summary>
        /// Start time
        /// </summary>
        public LocalDateTime startTime;
        /// <summary>
        /// Start latitude
        /// </summary>
        public float startLat;
        /// <summary>
        /// Start longitude
        /// </summary>
        public float startLon;
        /// <summary>
        /// Start height
        /// </summary>
        public float startHeight;    
}
