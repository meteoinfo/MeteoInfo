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

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.PointD;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.ProjectionUtil;
import org.meteoinfo.projection.proj4j.CoordinateReferenceSystem;
import org.meteoinfo.shape.PolygonShape;

/**
 *
 * @author Yaqiang Wang
 */
public class LambertConformalConic extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coorinate reference system
     */
    public LambertConformalConic(CoordinateReferenceSystem crs) {
        this.crs = crs;
        this.cutoff = -80.f;
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
        return ProjectionNames.Lambert_Conformal_Conic;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set latitude cutoff
     * @param value Latitude cutoff
     */
    @Override
    public void setCutoff(float value) {
        this.cutoff = value;
        this.updateBoundary();
    }
    
    @Override
    void updateBoundary() {
        double epsilon = 1e-10;
        double cenLon = this.getCenterLon();
        List<PointD> points = new ArrayList<>();
        double lon = cenLon - 180 + epsilon;
        double lat = this.cutoff;
        while (lon < cenLon + 180 - epsilon) {
            points.add(new PointD(lon, lat));
            lon += 1;
        }
        lon = cenLon + 180 - epsilon;
        points.add(new PointD(lon, lat));
        lat += 1;
        while (lat < 90) {
            points.add(new PointD(lon, lat));
            lat += 1;
        }
        points.add(new PointD(lon, 90));
        lon = cenLon - 180 + epsilon;
        while (lat > this.cutoff) {
            points.add(new PointD(lon, lat));
            lat -= 1;
        }

        PolygonShape ps = new PolygonShape();
        ps.setPoints(points);
        this.boundary = ProjectionUtil.projectPolygonShape(ps, KnownCoordinateSystems.geographic.world.WGS1984, this);
    }
    // </editor-fold>
}
