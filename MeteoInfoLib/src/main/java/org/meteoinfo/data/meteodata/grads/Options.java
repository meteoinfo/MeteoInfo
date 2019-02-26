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

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class Options {
    // <editor-fold desc="Variables">
    /// <summary>
    /// (GrADS version 2.0) (For DTYPE grib2 only) Indicates that pressure values that appear in 
    /// the descriptor file (in the ZDEF entry and in the GRIB2 codes in the variable declarations) 
    /// are given in units of Pascals. 
    /// </summary>

    public boolean pascals;
    /// <summary>
    /// Indicates that the Y dimension (latitude) in the data file has been written in the reverse 
    /// order from what GrADS assumes. 
    /// </summary>
    public boolean yrev;
    /// <summary>
    /// Indicates that the Z dimension (pressure) in the data file has been written from top to bottom,
    /// rather than from bottom to top as GrADS assumes. 
    /// </summary>
    public boolean zrev;
    /// <summary>
    /// Indicates that a template for multiple data files is in use.
    /// </summary>
    public boolean template;
    /// <summary>
    /// Indicates that the file was written in sequential unformatted I/O. 
    /// This keyword may be used with either station or gridded data. 
    /// If your gridded data is written in sequential format, 
    /// then each record must be an X-Y varying grid. 
    /// </summary>
    public boolean sequential;
    /// <summary>
    /// Indicates the data file was created with perpetual 365-day years,
    /// with no leap years. This is used for some types of model ouput.
    /// </summary>
    public boolean calendar_365_day;
    /// <summary>
    /// Indicates the binary data file is in reverse byte order from the normal byte order of your machine.
    /// </summary>
    public boolean byteswapped;
    /// <summary>
    /// Indicates the data file contains 32-bit IEEE floats created on a big endian platform (e.g., sun, sgi)
    /// </summary>
    public boolean big_endian;
    /// <summary>
    /// Indicates the data file contains 32-bit IEEE floats created on a little endian platform (e.g., iX86, and dec)
    /// </summary>
    public boolean little_endian;
    /// <summary>
    /// Indicates the data file contains 32-bit IEEE floats created on a cray.
    /// </summary>
    public boolean cray_32bit_ieee;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Options() {
        pascals = false;
        yrev = false;
        zrev = false;
        template = false;
        sequential = false;
        calendar_365_day = false;
        byteswapped = false;
        big_endian = false;
        little_endian = true;
        cray_32bit_ieee = false;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
