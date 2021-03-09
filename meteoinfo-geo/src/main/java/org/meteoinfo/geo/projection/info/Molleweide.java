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
package org.meteoinfo.geo.projection.info;

import java.util.ArrayList;
import java.util.List;

import org.meteoinfo.common.Direction;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geo.mapview.GridLabel;
import org.meteoinfo.geo.projection.KnownCoordinateSystems;
import org.meteoinfo.geo.projection.ProjectionNames;
import org.meteoinfo.geo.projection.ProjectionUtil;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.meteoinfo.geometry.shape.PolygonShape;

/**
 *
 * @author Yaqiang Wang
 */
public class Molleweide extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coorinate reference system
     */
    public Molleweide(CoordinateReferenceSystem crs) {
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
        return ProjectionNames.Molleweide;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    void updateBoundary() {
        double epsilon = 1e-10;
        double cenLon = this.getCenterLon();
        double minLon = cenLon - 180 + epsilon;
        double maxLon = cenLon + 180 - epsilon;
        double minLat = -90;
        double maxLat = 90;
        List<PointD> points = new ArrayList<>();
        double lon = minLon;
        double lat = minLat;        
        lon = maxLon;
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

    @Override
    public Object[] checkGridLabel(GridLabel gl, float shift) {
        float angle = gl.getAngle();
        double v = gl.getValue();
        float xShift = 0.f;
        float yShift = 0.f;
        XAlign xAlign = XAlign.CENTER;
        YAlign yAlign = YAlign.CENTER;
        if (v == 0) {
            if (angle == 90) {
                xShift = shift;
                xAlign = XAlign.LEFT;
            } else if (angle == 270) {
                xShift = -shift;
                xAlign = XAlign.RIGHT;
            } else if (angle < 90) {
                xShift = shift;
                xAlign = XAlign.LEFT;
                yAlign = YAlign.BOTTOM;
            } else if (angle > 90 && angle <= 180) {
                xShift = shift;
                xAlign = XAlign.LEFT;
                yAlign = YAlign.TOP;
            } else if (angle > 180 && angle < 270) {
                xShift = -shift;
                xAlign = XAlign.RIGHT;
                yAlign = YAlign.TOP;
            } else if (angle > 270 && angle <= 360) {
                xShift = -shift;
                xAlign = XAlign.RIGHT;
                yAlign = YAlign.BOTTOM;
            }
        } else if (v > 0) {
            if (gl.getLabDirection() == Direction.East) {
                xShift = shift;
                xAlign = XAlign.LEFT;
                yAlign = YAlign.BOTTOM;
            } else {
                xShift = -shift;
                xAlign = XAlign.RIGHT;
                yAlign = YAlign.BOTTOM;
            }
        } else {
            if (gl.getLabDirection() == Direction.East) {
                xShift = shift;
                xAlign = XAlign.LEFT;
                yAlign = YAlign.TOP;
            } else {
                xShift = -shift;
                xAlign = XAlign.RIGHT;
                yAlign = YAlign.TOP;
            }
        }

        return new Object[]{xShift, yShift, xAlign, yAlign};
    }
    // </editor-fold>
}
