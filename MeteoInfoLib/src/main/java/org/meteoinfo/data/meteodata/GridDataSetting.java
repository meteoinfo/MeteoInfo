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

import org.meteoinfo.global.Extent;

/**
 *
 * @author yaqiang
 */
public class GridDataSetting {
    /// <summary>
    /// Data extent
    /// </summary>

    public Extent dataExtent = new Extent();
    /// <summary>
    /// X number
    /// </summary>
    public int xNum;
    /// <summary>
    /// Y number
    /// </summary>
    public int yNum;
}
