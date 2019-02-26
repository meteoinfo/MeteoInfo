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
package org.meteoinfo.projection;

/**
 *
 * @author Yaqiang Wang
 */
public enum ProjectionNames {
    LongLat("longlat"),
    Lambert_Conformal_Conic("lcc"),
    Lambert_Equal_Area_Conic("leac"),
    Lambert_Azimuthal_Equal_Area("laea"),
    Albers_Equal_Area("aea"),
    Stereographic_Azimuthal("stere"),
    North_Polar_Stereographic_Azimuthal("stere"),
    South_Polar_Stereographic_Azimuthal("stere"),
    Mercator("merc"),
    Robinson("robin"),
    Molleweide("moll"),
    Orthographic_Azimuthal("ortho"),
    Geostationary_Satellite("geos"),       
    Oblique_Stereographic_Alternative("sterea"),
    Transverse_Mercator("tmerc"),
    Sinusoidal("sinu"),
    Cylindrical_Equal_Area("cea"),
    Hammer_Eckert("hammer"),
    Wagner3("wag3"),
    Undefine(null);
    
    private final String proj4Name;
    
    /**
     * Constructor
     * @param proj4Name Proj4 name
     */
    private ProjectionNames(String proj4Name) {
        this.proj4Name = proj4Name;
    }
    
    /**
     * Get proj4 name
     * @return Porj4 name
     */
    public String getProj4Name() {
        return this.proj4Name;
    }
    
    /**
     * Get projection name
     * @param proj4Name Proj4 name
     * @return Projection name
     */
    public static ProjectionNames getName(String proj4Name) {
        for (ProjectionNames name : ProjectionNames.values()) {
            if (name.proj4Name.equals(proj4Name)) {
                return name;
            }
        }
        return null;
    }
}
