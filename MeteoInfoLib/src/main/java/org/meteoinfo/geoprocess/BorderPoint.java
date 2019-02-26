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
package org.meteoinfo.geoprocess;

import org.meteoinfo.global.PointD;

/**
 *
 * @author Yaqiang Wang
 */
public class BorderPoint {
    /// <summary>
        /// Identifer
        /// </summary>
        public int Id;
        /// <summary>
        /// Border index
        /// </summary>
        public int BorderIdx;
        /// <summary>
        /// Border inner index
        /// </summary>
        public int BInnerIdx;
        /// <summary>
        /// Point
        /// </summary>
        public PointD Point;
        /// <summary>
        /// Value
        /// </summary>
        public double Value;
        
        public RectPointTypes rectPointType = RectPointTypes.None;
}
