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
package org.meteoinfo.data.meteodata.grads;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class TDEFS {
    // <editor-fold desc="Variables">
    /// <summary>
    /// Type - linear or ...
    /// </summary>

    public String Type;
    /// <summary>
    /// Time number
    /// </summary>
    //public int TNum;
    /// <summary>
    /// Start time
    /// </summary>
    public Date STime;
    /// <summary>
    /// Time delt
    /// </summary>
    public String TDelt;
    public int DeltaValue;
    public String unit;
    /// <summary>
    /// Time array
    /// </summary>
    public List<Date> times = new ArrayList<>();
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get time number
     * @return Time number
     */
    public int getTimeNum(){
        return this.times.size();
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
