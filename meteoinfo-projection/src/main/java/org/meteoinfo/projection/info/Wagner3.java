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
import org.meteoinfo.common.*;
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
public class Wagner3 extends ProjectionInfo {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Construction
     *
     * @param crs Coordinate reference system
     */
    public Wagner3(CoordinateReferenceSystem crs) {
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
        return ProjectionNames.Wagner3;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public void updateBoundary() {
        double epsilon = 1e-10;
        double cenLon = this.getCenterLon();
        double minLon = cenLon - 180 + epsilon;
        double maxLon = cenLon + 180 - epsilon;
        double minLat = -90;
        double maxLat = 90;
        List<PointD> points = new ArrayList<>();
        double lon = minLon;
        double lat = minLat;
        while (lon < maxLon) {
            points.add(new PointD(lon, lat));
            lon += 1;
        }
        lon = maxLon;
        while (lat < maxLat) {
            points.add(new PointD(lon, lat));
            lat += 1;
        }
        lat = maxLat;
        while (lon > minLon) {
            points.add(new PointD(lon, lat));
            lon -= 1;
        }
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
        if (gl.isLongitude()) {
            if (gl.getLabDirection() == Direction.South) {
                yShift = shift;
                yAlign = YAlign.TOP;
            } else {
                yShift = -shift;
                yAlign = YAlign.BOTTOM;
            }
        } else {
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
        }

        return new Object[]{xShift, yShift, xAlign, yAlign};
    }
    // </editor-fold>
}
