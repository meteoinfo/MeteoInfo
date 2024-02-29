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

import org.meteoinfo.common.Extent;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yaqiang Wang
 */
public class ProjectionUtil {

    /**
     * Get global extent of a projection
     *
     * @param toProj To projection
     * @return Extent
     */
    public static Extent getProjectionGlobalExtent(ProjectionInfo toProj) {
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        double x, y, minX = Double.NaN, minY = Double.NaN, maxX = Double.NaN, maxY = Double.NaN;
        int si = -90;
        int ei = 90;
        switch (toProj.getProjectionName()) {
            case Lambert_Conformal_Conic:
                si = -80;
                break;
            case North_Polar_Stereographic_Azimuthal:
                si = 0;
                break;
            case South_Polar_Stereographic_Azimuthal:
                ei = 0;
                break;
        }
        for (int i = si; i <= ei; i++) {
            y = i;
            for (int j = -180; j <= 180; j++) {
                x = i;
                double[][] points = new double[1][];
                points[0] = new double[]{x, y};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }
                    if (Double.isNaN(minX)) {
                        minX = x;
                        minY = y;
                    } else {
                        if (x < minX) {
                            minX = x;
                        }
                        if (y < minY) {
                            minY = y;
                        }
                    }
                    if (Double.isNaN(maxX)) {
                        maxX = x;
                        maxY = y;
                    } else {
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        Extent aExtent = new Extent();
        aExtent.minX = minX;
        aExtent.maxX = maxX;
        aExtent.minY = minY;
        aExtent.maxY = maxY;
        return aExtent;
    }

    /**
     * Get projected extent
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param x X coordinate
     * @param y Y coordinate
     * @return Extent
     */
    public static Extent getProjectionExtent(ProjectionInfo fromProj, ProjectionInfo toProj, List<Number> x, List<Number> y) {
        double[] X = new double[x.size()];
        double[] Y = new double[y.size()];
        for (int i = 0; i < X.length; i++) {
            X[i] = x.get(i).doubleValue();
        }
        for (int i = 0; i < Y.length; i++) {
            Y[i] = y.get(i).doubleValue();
        }
        return getProjectionExtent(fromProj, toProj, X, Y);
    }

    /**
     * Get projected extent
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param x X coordinate
     * @param y Y coordinate
     * @return Extent
     */
    public static Extent getProjectionExtent(ProjectionInfo fromProj, ProjectionInfo toProj,
                                             Array x, Array y) {
        x = x.copyIfView();
        y = y.copyIfView();
        double[] X = (double[])ArrayUtil.copyToNDJavaArray_Double(x);
        double[] Y = (double[])ArrayUtil.copyToNDJavaArray_Double(y);
        return getProjectionExtent(fromProj, toProj, X, Y);
    }

    /**
     * Get projected extent
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param extent Origin extent
     * @param n X/Y number
     * @return Extent
     */
    public static Extent getProjectionExtent(ProjectionInfo fromProj, ProjectionInfo toProj,
                                             Extent extent, int n) {
        double[] X = new double[n];
        double[] Y = new double[n];
        double dx = extent.getWidth() / (n - 1);
        double dy = extent.getHeight() / (n - 1);
        for (int i = 0; i < n; i++) {
            X[i] = extent.minX + i * dx;
            Y[i] = extent.minY + i * dy;
        }
        return getProjectionExtent(fromProj, toProj, X, Y);
    }

    /**
     * Get projected extent
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param X X coordinate
     * @param Y Y coordinate
     * @return Extent
     */
    public static Extent getProjectionExtent(ProjectionInfo fromProj, ProjectionInfo toProj, double[] X, double[] Y) {
        double x, y, minX = Double.NaN, minY = Double.NaN, maxX = Double.NaN, maxY = Double.NaN;
        int i, j;
        int minXI = X.length, minYI = Y.length, maxXI = -1, maxYI = -1;
        for (i = 0; i < Y.length; i++) {
            for (j = 0; j < X.length; j++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(minY)) {
                        minY = y;
                        minYI = i;
                    }
                    if (Double.isNaN(minX)) {
                        minX = x;
                    }
                    if (Double.isNaN(maxX)) {
                        maxX = x;
                    }
                    if (Double.isNaN(maxY)) {
                        maxY = y;
                    }
                    if (i == minYI) {
                        if (y < minY) {
                            minY = y;
                        }
                        if (x < minX) {
                            minX = x;
                        }
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                    } else if (i > minYI) {
                        break;
                    }
                } catch (Exception e) {
                }
            }
        }

        for (i = Y.length - 1; i >= 0; i--) {
            for (j = 0; j < X.length; j++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(maxY) | y > maxY) {
                        maxY = y;
                        maxYI = i;
                    }
                    if (Double.isNaN(maxX)) {
                        maxX = x;
                    }
                    if (Double.isNaN(minX)) {
                        minX = x;
                    }
                    if (Double.isNaN(minY)) {
                        minY = y;
                    }
                    if (i == maxYI) {
                        if (y > maxY) {
                            maxY = y;
                        }
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (x < minX) {
                            minX = x;
                        }
                        if (y < minY) {
                            minY = y;
                        }
                    } else if (i < maxYI) {
                        break;
                    }
                } catch (Exception e) {
                }
            }
        }

        for (j = 0; j < X.length; j++) {
            for (i = 0; i < Y.length; i++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(minX) | x < minX) {
                        minX = x;
                        minXI = j;
                    }
                    if (Double.isNaN(minY)) {
                        minY = y;
                    }
                    if (Double.isNaN(maxX)) {
                        maxX = x;
                    }
                    if (Double.isNaN(maxY)) {
                        maxY = y;
                    }
                    if (j == minXI) {
                        if (x < minX) {
                            minX = x;
                        }
                        if (y < minY) {
                            minY = y;
                        }
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                    } else if (j > minXI) {
                        break;
                    }
                } catch (Exception e) {
                }
            }
        }

        for (j = X.length - 1; j >= 0; j--) {
            for (i = 0; i < Y.length; i++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(maxX) | x > maxX) {
                        maxX = x;
                        maxXI = j;
                    }
                    if (Double.isNaN(maxY)) {
                        maxY = y;
                    }
                    if (j == maxXI) {
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                    } else if (j < maxXI) {
                        break;
                    }
                } catch (Exception e) {
                }
            }
        }

        if (Double.isNaN(minX)) {
            return null;
        }
        if (Double.isNaN(minY)) {
            return null;
        }
        if (Double.isNaN(maxX)) {
            return null;
        }
        if (Double.isNaN(maxY)) {
            return null;
        }

        if (toProj.isLonLat()) {
            if (maxX < minX && maxX < 0) {
                maxX += 360;
            }
        }
        Extent aExtent = new Extent();
        aExtent.minX = minX;
        aExtent.maxX = maxX;
        aExtent.minY = minY;
        aExtent.maxY = maxY;

        return aExtent;
    }

    public static PointShape projectPointShape(PointShape aPS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        PointShape newPS = (PointShape) aPS.clone();
        double[][] points = new double[1][];
        PointD oP = newPS.getPoint();
        points[0] = new double[]{oP.X, oP.Y};
        double[] fromP = new double[]{oP.X, oP.Y};
        try {
            Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
            if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                double[] toP = points[0];
                PointD rp = (PointD) oP.clone();
                rp.X = points[0][0];
                rp.Y = points[0][1];
                newPS.setPoint(rp);
                switch (aPS.getShapeType()) {
                    case WIND_BARB:
                        ((WindBarb) newPS).angle = projectAngle(((WindBarb) newPS).angle, fromP, toP, fromProj, toProj);
                        break;
                    case WIND_ARROW:
                        ((WindArrow) newPS).angle = projectAngle(((WindArrow) newPS).angle, fromP, toP, fromProj, toProj);
                        break;
                    case STATION_MODEL:
                        ((StationModelShape) newPS).windBarb.angle = projectAngle(((StationModelShape) newPS).windBarb.angle, fromP, toP, fromProj, toProj);
                        break;
                }
                return newPS;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static PolylineShape projectPolylineShape(PolylineShape aPLS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<Polyline> polyLines = new ArrayList<>();
        for (int i = 0; i < aPLS.getPolylines().size(); i++) {
            List<PointD> newPoints = new ArrayList<>();
            Polyline aPL = aPLS.getPolylines().get(i);
            Polyline bPL;
            double x;
            for (int j = 0; j < aPL.getPointList().size(); j++) {
                double[][] points = new double[1][];
                PointD wPoint = aPL.getPointList().get(j);
                x = wPoint.X;
                if (fromProj.isLonLat()) {
                    if (x > 180) {
                        x -= 360;
                    } else if (x < -180) {
                        x += 360;
                    }
                }
                points[0] = new double[]{x, wPoint.Y};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
                    if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                        //wPoint = new PointD();
                        wPoint.X = points[0][0];
                        wPoint.Y = points[0][1];
                        newPoints.add(wPoint);
                    }
                } catch (Exception e) {
                    break;
                }
            }

            if (newPoints.size() > 1) {
                bPL = new Polyline();
                bPL.setPointList(newPoints);
                polyLines.add(bPL);
            }
        }

        if (polyLines.size() > 0) {
            aPLS.setPolylines(polyLines);

            return aPLS;
        } else {
            return null;
        }
    }

    /**
     * Project angle
     *
     * @param oAngle The angle
     * @param fromP1 From point
     * @param toP1 To point
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected angle
     */
    public static double projectAngle(double oAngle, double[] fromP1, double[] toP1, ProjectionInfo fromProj, ProjectionInfo toProj) {
        double pAngle = oAngle;
        double[] fromP2;
        double[] toP2;
        double[][] points = new double[1][];

        if (fromP1[1] == 90) {
            fromP2 = new double[]{fromP1[0], fromP1[1] - 10};
            points[0] = (double[]) fromP2.clone();
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
                toP2 = points[0];
                double x, y;
                x = toP2[0] - toP1[0];
                y = toP2[1] - toP1[1];
                double aLen = Math.sqrt(x * x + y * y);
                double angle = Math.asin(x / aLen) * 180 / Math.PI;
                if (x < 0 && y < 0) {
                    angle = 180.0 - angle;
                } else if (x > 0 && y < 0) {
                    angle = 180.0 - angle;
                } else if (x < 0 && y > 0) {
                    angle = 360.0 + angle;
                }
                if (aLen == 0) {
                    System.out.print("Error");
                }
                pAngle = oAngle + (angle - 180);
                if (pAngle > 360) {
                    pAngle = pAngle - 360;
                } else if (pAngle < 0) {
                    pAngle = pAngle + 360;
                }
            } catch (Exception e) {
            }
        } else {
            fromP2 = new double[]{fromP1[0] + 10, fromP1[1]};
            points[0] = (double[]) fromP2.clone();
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
                toP2 = points[0];

                double x, y;
                x = toP2[0] - toP1[0];
                y = toP2[1] - toP1[1];
                double aLen = Math.sqrt(x * x + y * y);
                if (aLen == 0) {
                    return pAngle;
                }

                double angle = Math.asin(x / aLen) * 180 / Math.PI;
                if (Double.isNaN(angle)) {
                    return pAngle;
                }

                if (x < 0 && y < 0) {
                    angle = 180.0 - angle;
                } else if (x > 0 && y < 0) {
                    angle = 180.0 - angle;
                } else if (x < 0 && y > 0) {
                    angle = 360.0 + angle;
                }

                pAngle = oAngle + (angle - 90);
                if (pAngle > 360) {
                    pAngle = pAngle - 360;
                } else if (pAngle < 0) {
                    pAngle = pAngle + 360;
                }
            } catch (Exception e) {
            }
        }

        return pAngle;
    }

    /**
     * Project polygon shape
     *
     * @param aPGS A polygon shape
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected polygon shape
     */
    public static PolygonShape projectPolygonShape(PolygonShape aPGS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        if (aPGS instanceof PolygonZShape) {
            List<PolygonZ> polygons = new ArrayList<>();
            for (int i = 0; i < aPGS.getPolygons().size(); i++) {
                PolygonZ aPG = (PolygonZ) aPGS.getPolygons().get(i);
                PolygonZ bPG = null;
                for (int r = 0; r < aPG.getRingNumber(); r++) {
                    List<PointZ> pList = (List<PointZ>) aPG.getRings().get(r);
                    List<PointZ> newPoints = new ArrayList<>();
                    for (int j = 0; j < pList.size(); j++) {
                        double[][] points = new double[1][];
                        PointZ wPoint = pList.get(j);
                        points[0] = new double[]{wPoint.X, wPoint.Y};
                        try {
                            Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
                            if (!Double.isNaN(points[0][0]) || !Double.isInfinite(points[0][0]) ||
                                    !Double.isNaN(points[0][1]) || !Double.isInfinite(points[0][1])) {
                                PointZ nPoint = new PointZ();
                                nPoint.X = points[0][0];
                                nPoint.Y = points[0][1];
                                nPoint.M = wPoint.M;
                                nPoint.Z = wPoint.Z;
                                newPoints.add(nPoint);
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }

                    if (r == 0) {
                        if (newPoints.size() > 2) {
                            bPG = new PolygonZ();
                            bPG.setOutLine(newPoints);
                        } else {
                            break;
                        }
                    } else {
                        if (newPoints.size() > 2) {
                            bPG.addHole(newPoints);
                        }
                    }
                }

                if (bPG != null) {
                    polygons.add(bPG);
                }
            }

            if (polygons.size() > 0) {
                ((PolygonZShape) aPGS).setPolygons(polygons);

                return aPGS;
            } else {
                return null;
            }
        } else {
            List<Polygon> polygons = new ArrayList<>();
            for (int i = 0; i < aPGS.getPolygons().size(); i++) {
                Polygon aPG = aPGS.getPolygons().get(i);
                Polygon bPG = null;
                for (int r = 0; r < aPG.getRingNumber(); r++) {
                    List<PointD> pList = (List<PointD>) aPG.getRings().get(r);
                    List<PointD> newPoints = new ArrayList<>();
                    for (int j = 0; j < pList.size(); j++) {
                        double[][] points = new double[1][];
                        PointD wPoint = pList.get(j);
                        points[0] = new double[]{wPoint.X, wPoint.Y};
                        try {
                            Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
                            if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                                wPoint = new PointD();
                                wPoint.X = points[0][0];
                                wPoint.Y = points[0][1];
                                newPoints.add(wPoint);
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }

                    if (r == 0) {
                        if (newPoints.size() > 2) {
                            bPG = new Polygon();
                            bPG.setOutLine(newPoints);
                        } else {
                            break;
                        }
                    } else {
                        if (newPoints.size() > 2) {
                            bPG.addHole(newPoints);
                        }
                    }
                }

                if (bPG != null) {
                    polygons.add(bPG);
                }
            }

            if (polygons.size() > 0) {
                aPGS.setPolygons(polygons);

                return aPGS;
            } else {
                return null;
            }
        }
    }

    /**
     * Project point shape - clip the point shape when necessary
     *
     * @param pointShape A point shape
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected point shape
     */
    public static PointShape projectClipPointShape(PointShape pointShape, ProjectionInfo fromProj, ProjectionInfo toProj) {
        double refLon = toProj.getCoordinateReferenceSystem().getProjection().getProjectionLongitudeDegrees();
        refLon += 180;
        if (refLon > 180) {
            refLon = refLon - 360;
        } else if (refLon < -180) {
            refLon = refLon + 360;
        }
        float cutoff = toProj.getCutoff();

        if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
            switch (toProj.getProjectionName()) {
                case Lambert_Conformal_Conic:
                    if (pointShape.getPoint().Y < cutoff) {
                        return null;
                    }
                    break;
                case North_Polar_Stereographic_Azimuthal:
                    if (pointShape.getPoint().Y < cutoff) {
                        return null;
                    }
                    break;
                case South_Polar_Stereographic_Azimuthal:
                    if (pointShape.getPoint().Y > cutoff) {
                        return null;
                    }
                    break;
                case Mercator:
                    if (pointShape.getPoint().Y > cutoff || pointShape.getPoint().Y < -cutoff) {
                        return null;
                    }
                    break;
            }
        }
        pointShape = ProjectionUtil.projectPointShape(pointShape, fromProj, toProj);

        return pointShape;
    }

    /**
     * Project polyline shape - clip the polyline shape when necessary
     *
     * @param lineShape A polyline shape
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected polyline shape
     */
    public static List<PolylineShape> projectClipPolylineShape(PolylineShape lineShape, ProjectionInfo fromProj, ProjectionInfo toProj) {
        double refLon = toProj.getCoordinateReferenceSystem().getProjection().getProjectionLongitudeDegrees();
        refLon += 180;
        if (refLon > 180) {
            refLon = refLon - 360;
        } else if (refLon < -180) {
            refLon = refLon + 360;
        }
        float cutoff = toProj.getCutoff();

        List<PolylineShape> lineShapes = new ArrayList<>();
        if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
            switch (toProj.getProjectionName()) {
                case Lambert_Conformal_Conic:
                    if (lineShape.getExtent().minY < cutoff) {
                        lineShape = GeoComputation.clipPolylineShape_Lat(lineShape, cutoff, true);
                    }
                    break;
                case North_Polar_Stereographic_Azimuthal:
                    if (lineShape.getExtent().minY < cutoff) {
                        lineShape = GeoComputation.clipPolylineShape_Lat(lineShape, cutoff, true);
                    }
                    break;
                case South_Polar_Stereographic_Azimuthal:
                    if (lineShape.getExtent().maxY > cutoff) {
                        lineShape = GeoComputation.clipPolylineShape_Lat(lineShape, cutoff, false);
                    }
                    break;
                case Mercator:
                    if (lineShape.getExtent().maxY > cutoff) {
                        lineShape = GeoComputation.clipPolylineShape_Lat(lineShape, cutoff, false);
                    }
                    if (lineShape.getExtent().minY < -cutoff) {
                        lineShape = GeoComputation.clipPolylineShape_Lat(lineShape, -cutoff, true);
                    }
                    break;
            }
            if (lineShape == null) {
                return null;
            }

            if (lineShape.getExtent().minX <= refLon && lineShape.getExtent().maxX >= refLon) {
                switch (toProj.getProjectionName()) {
                    case North_Polar_Stereographic_Azimuthal:
                    case South_Polar_Stereographic_Azimuthal:
                        lineShapes.add(lineShape);
                        break;
                    default:
                        lineShapes.add(GeoComputation.clipPolylineShape_Lon(lineShape, refLon));
                        break;
                }
            } else {
                lineShapes.add(lineShape);
            }
        } else {
            lineShapes.add(lineShape);
        }

        List<PolylineShape> newPolylines = new ArrayList<>();
        for (int i = 0; i < lineShapes.size(); i++) {
            lineShape = lineShapes.get(i);
            lineShape = ProjectionUtil.projectPolylineShape(lineShape, fromProj, toProj);
            if (lineShape != null) {
                newPolylines.add(lineShape);
            }
        }

        return newPolylines;
    }

    /**
     * Project polygon shape - clip the polygon shape when necessary
     *
     * @param aPGS A polygon shape
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected polygon shape
     */
    public static List<PolygonShape> projectClipPolygonShape(PolygonShape aPGS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        double refLon = toProj.getCoordinateReferenceSystem().getProjection().getProjectionLongitudeDegrees();
        refLon += 180;
        if (refLon > 180) {
            refLon = refLon - 360;
        } else if (refLon < -180) {
            refLon = refLon + 360;
        }
        float cutoff = toProj.getCutoff();

        List<PolygonShape> pgsList = new ArrayList<>();
        if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
            switch (toProj.getProjectionName()) {
                case Lambert_Conformal_Conic:
                    if (aPGS.getExtent().minY < cutoff) {
                        aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, cutoff, true);
                    }
                    break;
                case North_Polar_Stereographic_Azimuthal:
                    if (aPGS.getExtent().minY < cutoff) {
                        //continue;
                        aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, cutoff, true);
                    }
                    break;
                case South_Polar_Stereographic_Azimuthal:
                    if (aPGS.getExtent().maxY > cutoff) {
                        //continue;
                        aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, cutoff, false);
                    }
                    break;
                case Mercator:
                    if (aPGS.getExtent().maxY > cutoff) {
                        aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, cutoff, false);
                    }
                    if (aPGS.getExtent().minY < -cutoff) {
                        aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, -cutoff, true);
                    }
                    break;
            }
            if (aPGS == null) {
                return null;
            }

            if (aPGS.getExtent().minX <= refLon && aPGS.getExtent().maxX >= refLon) {
                switch (toProj.getProjectionName()) {
                    case North_Polar_Stereographic_Azimuthal:
                    case South_Polar_Stereographic_Azimuthal:
                        pgsList.add(aPGS);
                        break;
                    default:
                        pgsList.add(GeoComputation.clipPolygonShape_Lon(aPGS, refLon));
                        break;
                }
            } else {
                pgsList.add(aPGS);
            }
        } else {
            pgsList.add(aPGS);
        }

        List<PolygonShape> newPolygons = new ArrayList<>();
        for (int i = 0; i < pgsList.size(); i++) {
            aPGS = pgsList.get(i);
            aPGS = projectPolygonShape(aPGS, fromProj, toProj);
            if (aPGS != null) {
                newPolygons.add(aPGS);
            }
        }

        return newPolygons;
    }

    /**
     * Project graphic
     *
     * @param graphic The graphic
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected graphic
     */
    public static Graphic projectGraphic(Graphic graphic, ProjectionInfo fromProj, ProjectionInfo toProj) {
        if (graphic instanceof GraphicCollection) {
            GraphicCollection newGCollection = new GraphicCollection();
            for (Graphic aGraphic : ((GraphicCollection) graphic).getGraphics()) {
                aGraphic.setShape(projectShape(aGraphic.getShape(), fromProj, toProj));
                if (aGraphic.getShape() != null) {
                    newGCollection.add(aGraphic);
                }
            }

            return newGCollection;
        } else {
            Shape shape = projectShape(graphic.getShape(), fromProj, toProj);
            return new Graphic(shape, graphic.getLegend());
        }
    }

    /**
     * Project graphic
     *
     * @param graphic The graphic
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected graphic
     */
    public static Graphic projectClipGraphic(Graphic graphic, ProjectionInfo fromProj, ProjectionInfo toProj) {
        if (graphic instanceof GraphicCollection) {
            try {
                GraphicCollection newGCollection = new GraphicCollection();
                for (Graphic aGraphic : ((GraphicCollection) graphic).getGraphics()) {
                    List<? extends Shape> shapes = projectClipShape(aGraphic.getShape(), fromProj, toProj);
                    if (shapes != null && shapes.size() > 0) {
                        aGraphic.setShape(shapes.get(0));
                        newGCollection.add(aGraphic);
                    }
                }
                newGCollection.setLegendScheme(((GraphicCollection) graphic).getLegendScheme());
                newGCollection.setSingleLegend(((GraphicCollection) graphic).isSingleLegend());

                return newGCollection;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            List<? extends Shape> shapes = projectClipShape(graphic.getShape(), fromProj, toProj);
            if (shapes != null) {
                return new Graphic(shapes.get(0), graphic.getLegend());
            } else {
                return null;
            }
        }
    }

    /*public static GraphicCollection projectGraphic(GraphicCollection aGCollection, ProjectionInfo fromProj, ProjectionInfo toProj) {
        GraphicCollection newGCollection = new GraphicCollection();
        for (Graphic aGraphic : aGCollection.getGraphics()) {
            aGraphic.setShape(projectShape(aGraphic.getShape(), fromProj, toProj));
            if (aGraphic.getShape() != null) {
                newGCollection.add(aGraphic);
            }
        }

        return newGCollection;
    }*/

    public static List<Graphic> projectGraphic(List<Graphic> graphics, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<Graphic> newGraphics = new ArrayList<>();
        for (Graphic aGraphic : graphics) {
            Shape aShape = projectShape(aGraphic.getShape(), fromProj, toProj);
            if (aShape != null) {
                newGraphics.add(new Graphic(aShape, aGraphic.getLegend()));
            }
        }

        return newGraphics;
    }

    private static Shape projectShape(Shape aShape, ProjectionInfo fromProj, ProjectionInfo toProj) {
        Shape newShape;
        switch (aShape.getShapeType()) {
            case POINT:
            case POINT_M:
                newShape = projectPointShape((PointShape) aShape, fromProj, toProj);
                break;
            case POLYLINE:
            case POLYLINE_M:
                newShape = projectPolylineShape((PolylineShape) aShape, fromProj, toProj);
                break;
            case CURVE_LINE:
                newShape = projectCurvelineShape((CurveLineShape) aShape, fromProj, toProj);
                break;
            case POLYGON:
            case POLYGON_M:
            case POLYGON_Z:
            case RECTANGLE:
                newShape = projectPolygonShape((PolygonShape) aShape, fromProj, toProj);
                break;
            case CURVE_POLYGON:
                newShape = projectCurvePolygonShape((CurvePolygonShape) aShape, fromProj, toProj);
                break;
            case CIRCLE:
                newShape = projectCircleShape((CircleShape) aShape, fromProj, toProj);
                break;
            case ELLIPSE:
                newShape = projectEllipseShape((EllipseShape) aShape, fromProj, toProj);
                break;
            default:
                newShape = null;
                break;
        }

        return newShape;
    }

    private static List<? extends Shape> projectClipShape(Shape shape, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<? extends Shape> shapes = null;
        switch (shape.getShapeType()) {
            case POINT:
            case POINT_M:
            case POINT_Z:
            case WIND_ARROW:
            case WIND_BARB:
            case STATION_MODEL:
                PointShape pointShape = projectClipPointShape((PointShape) shape, fromProj, toProj);
                if (pointShape != null) {
                    List<PointShape> pointShapes = new ArrayList<PointShape>();
                    pointShapes.add(pointShape);
                    shapes = pointShapes;
                }
                break;
            case POLYLINE:
            case POLYLINE_M:
            case POLYLINE_Z:
                shapes = projectClipPolylineShape((PolylineShape) shape, fromProj, toProj);
                break;
            case CURVE_LINE:
                //shapes = projectCurvelineShape((CurveLineShape) shape, fromProj, toProj);
                break;
            case POLYGON:
            case POLYGON_M:
            case POLYGON_Z:
            case RECTANGLE:
                shapes = projectClipPolygonShape((PolygonShape) shape, fromProj, toProj);
                break;
            case CURVE_POLYGON:
                //shapes = projectCurvePolygonShape((CurvePolygonShape) shape, fromProj, toProj);
                break;
            case CIRCLE:
                //shapes = projectCircleShape((CircleShape) shape, fromProj, toProj);
                break;
            case ELLIPSE:
                //shapes = projectEllipseShape((EllipseShape) shape, fromProj, toProj);
                break;
        }

        return shapes;
    }

    private static CurveLineShape projectCurvelineShape(CurveLineShape aPLS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<Polyline> polyLines = new ArrayList<>();
        for (int i = 0; i < aPLS.getPolylines().size(); i++) {
            List<PointD> newPoints = new ArrayList<>();
            Polyline aPL = aPLS.getPolylines().get(i);
            Polyline bPL;
            for (int j = 0; j < aPL.getPointList().size(); j++) {
                double[][] points = new double[1][];
                PointD wPoint = aPL.getPointList().get(j);
                points[0] = new double[]{wPoint.X, wPoint.Y};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
                    if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                        wPoint = new PointD();
                        wPoint.X = points[0][0];
                        wPoint.Y = points[0][1];
                        newPoints.add(wPoint);
                    }
                } catch (Exception e) {
                    break;
                }
            }

            if (newPoints.size() > 1) {
                bPL = new Polyline();
                bPL.setPointList(newPoints);
                polyLines.add(bPL);
            }
        }

        if (polyLines.size() > 0) {
            aPLS.setPolylines(polyLines);

            return aPLS;
        } else {
            return null;
        }
    }

    private static CurvePolygonShape projectCurvePolygonShape(CurvePolygonShape aPGS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < aPGS.getPolygons().size(); i++) {
            Polygon aPG = aPGS.getPolygons().get(i);
            Polygon bPG = null;
            for (int r = 0; r < aPG.getRingNumber(); r++) {
                List<PointD> pList = (List<PointD>) aPG.getRings().get(r);
                List<PointD> newPoints = new ArrayList<>();
                for (int j = 0; j < pList.size(); j++) {
                    double[][] points = new double[1][];
                    PointD wPoint = pList.get(j);
                    points[0] = new double[]{wPoint.X, wPoint.Y};
                    try {
                        Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
                        if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                            wPoint = new PointD();
                            wPoint.X = points[0][0];
                            wPoint.Y = points[0][1];
                            newPoints.add(wPoint);
                        }
                    } catch (Exception e) {
                        break;
                    }
                }

                if (r == 0) {
                    if (newPoints.size() > 2) {
                        bPG = new Polygon();
                        bPG.setOutLine(newPoints);
                    } else {
                        break;
                    }
                } else {
                    if (newPoints.size() > 2) {
                        bPG.addHole(newPoints);
                    }
                }
            }

            if (bPG != null) {
                polygons.add(bPG);
            }
        }

        if (polygons.size() > 0) {
            aPGS.setPolygons(polygons);

            return aPGS;
        } else {
            return null;
        }
    }

    private static CircleShape projectCircleShape(CircleShape aCS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        double radius = Math.abs(aCS.getPoints().get(1).X - aCS.getPoints().get(0).X);
        double[][] points = new double[1][];
        PointD centerPoint = new PointD(aCS.getPoints().get(0).X + radius, aCS.getPoints().get(0).Y);
        points[0] = new double[]{centerPoint.X, centerPoint.Y};
        try {
            Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
            if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                centerPoint.X = points[0][0];
                centerPoint.Y = points[0][1];
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        points = new double[1][];
        PointD leftPoint = aCS.getPoints().get(0);
        points[0] = new double[]{leftPoint.X, leftPoint.Y};
        try {
            Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
            if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                leftPoint.X = points[0][0];
                leftPoint.Y = points[0][1];
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        radius = Math.abs(centerPoint.X - leftPoint.X);
        List<PointD> newPoints = new ArrayList<>();
        newPoints.add(new PointD(centerPoint.X - radius, centerPoint.Y));
        newPoints.add(new PointD(centerPoint.X, centerPoint.Y - radius));
        newPoints.add(new PointD(centerPoint.X + radius, centerPoint.Y));
        newPoints.add(new PointD(centerPoint.X, centerPoint.Y + radius));
        CircleShape newCS = new CircleShape();
        newCS.setPoints(newPoints);

        return newCS;
    }

    private static EllipseShape projectEllipseShape(EllipseShape aES, ProjectionInfo fromProj, ProjectionInfo toProj) {
        double xRadius = Math.abs(aES.getPoints().get(2).X - aES.getPoints().get(0).X) / 2;
        double yRadius = Math.abs(aES.getPoints().get(2).Y - aES.getPoints().get(0).Y) / 2;
        double[][] points = new double[1][];
        PointD centerPoint = new PointD(aES.getExtent().minX + xRadius, aES.getExtent().minY + yRadius);
        points[0] = new double[]{centerPoint.X, centerPoint.Y};
        try {
            Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
            if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                centerPoint.X = points[0][0];
                centerPoint.Y = points[0][1];
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        points = new double[1][];
        PointD lbPoint = new PointD(aES.getExtent().minX, aES.getExtent().minY);
        points[0] = new double[]{lbPoint.X, lbPoint.Y};
        try {
            Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
            if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                lbPoint.X = points[0][0];
                lbPoint.Y = points[0][1];
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        xRadius = Math.abs(centerPoint.X - lbPoint.X);
        yRadius = Math.abs(centerPoint.Y - lbPoint.Y);
        List<PointD> newPoints = new ArrayList<>();
        newPoints.add(new PointD(centerPoint.X - xRadius, centerPoint.Y - yRadius));
        newPoints.add(new PointD(centerPoint.X - xRadius, centerPoint.Y + yRadius));
        newPoints.add(new PointD(centerPoint.X + xRadius, centerPoint.Y + yRadius));
        newPoints.add(new PointD(centerPoint.X + xRadius, centerPoint.Y - yRadius));
        EllipseShape newES = new EllipseShape();
        newES.setPoints(newPoints);

        return newES;
    }

}
