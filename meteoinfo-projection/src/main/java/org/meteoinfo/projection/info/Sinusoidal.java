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

import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.shape.PolygonShape;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.ProjectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class Sinusoidal extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coordinate reference system
     */
    public Sinusoidal(CoordinateReferenceSystem crs) {
        super(crs);
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
        return ProjectionNames.Sinusoidal;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get valid parameters
     * @return Valid parameters
     */
    @Override
    public List<String> getValidParas() {
        List<String> paras = new ArrayList<>();
        paras.add("lon_0");
        return paras;
    }
    
    @Override
    public void updateBoundary() {
        double epsilon = 1e-10;
        double cenLon = this.getCenterLon();
        double minLon = cenLon - 180 + epsilon;
        double maxLon = cenLon + 180 - epsilon;
        double minLat = -90;
        double maxLat = 90;
        List<PointD> points = new ArrayList<>();
        double lon = maxLon;
        double lat = minLat;
        while (lat < maxLat) {
            points.add(new PointD(lon, lat));
            lat += 1;
        }
        lat = maxLat;    
        lon = minLon;
        while (lat > minLat) {
            points.add(new PointD(lon, lat));
            lat -= 1;
        }
        lat = minLat;
        points.add(new PointD(lon, lat));
        PolygonShape ps = new PolygonShape();
        ps.setPoints(points);
        this.boundary = ProjectionUtil.projectPolygonShape(ps, KnownCoordinateSystems.geographic.world.WGS1984, this);
    }
    // </editor-fold>
}
