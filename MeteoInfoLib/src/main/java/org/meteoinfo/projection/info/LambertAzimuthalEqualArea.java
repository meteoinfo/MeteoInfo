/* Copyright 2012 - Yaqiang Wang,
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
package org.meteoinfo.projection.info;

import java.util.List;
import org.meteoinfo.global.PointD;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.projection.proj4j.CoordinateReferenceSystem;
import org.meteoinfo.shape.PolygonShape;

/**
 *
 * @author Yaqiang Wang
 */
public class LambertAzimuthalEqualArea extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coorinate reference system
     */
    public LambertAzimuthalEqualArea(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get projection name
     *
     * @return Projection name
     */
    @Override
    public ProjectionNames getProjectionName() {
        return ProjectionNames.Lambert_Azimuthal_Equal_Area;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    void updateBoundary() {
        double epsilon = 1e-10;
        double a = this.crs.getDatum().getEllipsoid().getA();
        double cenLat = this.getCenterLat();
        double cenLon = this.getCenterLon();
        double lon = cenLon + 180 - epsilon;
        double sign = Math.signum(cenLat);
        if (sign == 0)
            sign = 1;
        double lat = -cenLat + sign * 0.01;        
        PointD p = Reproject.reprojectPoint(lon, lat, KnownCoordinateSystems.geographic.world.WGS1984, this);
        double x = p.X;
        double max_y = p.Y;
        double easting = this.crs.getProjection().getFalseEasting();
        double northing = this.crs.getProjection().getFalseNorthing();
        List<PointD> points = this.ellipse_boundary(a * 1.9999, max_y - northing, easting, northing, 61);
        PolygonShape ps = new PolygonShape();
        ps.setPoints(points);
        this.boundary = ps;
    }
    // </editor-fold>
}
