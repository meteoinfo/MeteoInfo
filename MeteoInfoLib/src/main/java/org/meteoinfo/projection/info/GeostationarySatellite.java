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
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.proj4j.CoordinateReferenceSystem;
import org.meteoinfo.shape.PolygonShape;

/**
 *
 * @author Yaqiang Wang
 */
public class GeostationarySatellite extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coorinate reference system
     */
    public GeostationarySatellite(CoordinateReferenceSystem crs) {
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
        return ProjectionNames.Geostationary_Satellite;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    void updateBoundary() {
        double epsilon = 1e-10;
        double a = this.crs.getDatum().getEllipsoid().getA();
        double b = this.crs.getDatum().getEllipsoid().getB();
        double h = this.crs.getProjection().getHeightOfOrbit();
        double max_x = h * Math.asin(a / (a + h));
        double max_y = h * Math.asin(b / (a + h));
        double easting = this.crs.getProjection().getFalseEasting();
        double northing = this.crs.getProjection().getFalseNorthing();
        List<PointD> points = ellipse_boundary(max_x, max_y, easting, northing, 201);
        PolygonShape ps = new PolygonShape();
        ps.setPoints(points);
        this.boundary = ps;
    }
    // </editor-fold>
}
