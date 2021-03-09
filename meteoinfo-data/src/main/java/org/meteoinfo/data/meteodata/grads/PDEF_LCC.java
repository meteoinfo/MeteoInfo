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
 *
 * @author Yaqiang Wang
 */
public class PDEF_LCC {
    /// <summary>
    /// Projection type
    /// </summary>

    public String PType;
    /// <summary>
    /// The size of the native grid in the x direction
    /// </summary>
    public int isize;
    /// <summary>
    /// The size of the native grid in the y direction
    /// </summary>
    public int jsize;
    /// <summary>
    /// reference latitude
    /// </summary>
    public float latref;
    /// <summary>
    /// reference longitude (in degrees, E is positive, W is negative)
    /// </summary>
    public float lonref;
    /// <summary>
    /// i of ref point
    /// </summary>
    public float iref;
    /// <summary>
    /// j of ref point 
    /// </summary>
    public float jref;
    /// <summary>
    /// S true lat
    /// </summary>
    public float Struelat;
    /// <summary>
    /// N true lat
    /// </summary>
    public float Ntruelat;
    /// <summary>
    /// Standard longitude
    /// </summary>
    public float slon;
    /// <summary>
    /// grid X increment in meters
    /// </summary>
    public float dx;
    /// <summary>
    /// grid y increment in meters
    /// </summary>
    public float dy;
}
