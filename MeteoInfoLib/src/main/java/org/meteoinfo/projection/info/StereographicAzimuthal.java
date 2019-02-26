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
public class StereographicAzimuthal extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coorinate reference system
     */
    public StereographicAzimuthal(CoordinateReferenceSystem crs) {
        this.crs = crs;
        this.cutoff = 0.f;
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
        if (this.isNorthPolar()) {
            return ProjectionNames.North_Polar_Stereographic_Azimuthal;
        } else if (this.isSouthPolar()) {
            return ProjectionNames.South_Polar_Stereographic_Azimuthal;
        } else {
            return ProjectionNames.Stereographic_Azimuthal;
        }
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Check is north polar or not
     *
     * @return Boolean
     */
    public boolean isNorthPolar() {
        return this.crs.getProjection().getProjectionLatitudeDegrees() == 90;
    }

    /**
     * Check is south polar or not
     *
     * @return Boolean
     */
    public boolean isSouthPolar() {
        return this.crs.getProjection().getProjectionLatitudeDegrees() == -90;
    }

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
        List<PointD> points = new ArrayList<>();
        double lon = -180;
        double lat = this.cutoff;
        while (lon <= 180) {
            points.add(new PointD(lon, lat));
            lon += 1;
        }
        PolygonShape ps = new PolygonShape();
        ps.setPoints(points);
        this.boundary = ProjectionUtil.projectPolygonShape(ps, KnownCoordinateSystems.geographic.world.WGS1984, this);
    }
    // </editor-fold>
}
