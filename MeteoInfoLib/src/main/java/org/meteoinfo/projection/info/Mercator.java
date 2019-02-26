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
public class Mercator extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coorinate reference system
     */
    public Mercator(CoordinateReferenceSystem crs) {
        this.crs = crs;
        this.cutoff = 85.0511f;
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
        return ProjectionNames.Mercator;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set latitude cutoff
     *
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
        double minLon = cenLon - 180 + epsilon;
        double maxLon = cenLon + 180 - epsilon;
        double minLat = -this.cutoff;
        double maxLat = this.cutoff;
        List<PointD> points = new ArrayList<>();
        points.add(new PointD(minLon, minLat));
        points.add(new PointD(maxLon, minLat));
        points.add(new PointD(maxLon, maxLat));
        points.add(new PointD(minLon, maxLat));
        points.add(new PointD(minLon, minLat));
        PolygonShape ps = new PolygonShape();
        ps.setPoints(points);
        this.boundary = ProjectionUtil.projectPolygonShape(ps, KnownCoordinateSystems.geographic.world.WGS1984, this);
    }
    // </editor-fold>
}
