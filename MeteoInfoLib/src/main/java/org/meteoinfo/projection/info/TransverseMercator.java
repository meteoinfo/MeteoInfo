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
public class TransverseMercator extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coorinate reference system
     */
    public TransverseMercator(CoordinateReferenceSystem crs) {
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
        return ProjectionNames.Transverse_Mercator;
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
        double x0 = -2e7;
        double x1 = 2e7;
        double y0 = -1e7;
        double y1 = 1e7;
        List<PointD> points = new ArrayList<>();
        points.add(new PointD(x0, y0));
        points.add(new PointD(x1, y0));
        points.add(new PointD(x1, y1));
        points.add(new PointD(x0, y1));
        points.add(new PointD(x0, y0));
        PolygonShape ps = new PolygonShape();
        ps.setPoints(points);
        this.boundary = ps;
    }
    // </editor-fold>
}
