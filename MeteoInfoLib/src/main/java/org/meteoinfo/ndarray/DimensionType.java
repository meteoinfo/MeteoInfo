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
package org.meteoinfo.ndarray;

/**
 *
 * @author Yaqiang Wang
 */
public enum DimensionType {
    /// <summary>
        /// X dimension
        /// </summary>
        X,
        /// <summary>
        /// Y dimension
        /// </summary>
        Y,
        /// <summary>
        /// Z/Level dimension
        /// </summary>
        Z,
        /// <summary>
        /// Time dimension
        /// </summary>
        T,
        E,    //Ensemble dimension
        /// <summary>
        /// Xtrack dimension - for HDF EOS swath data
        /// </summary>
        Xtrack,
        /// <summary>
        /// Other dimension
        /// </summary>
        Other
}
