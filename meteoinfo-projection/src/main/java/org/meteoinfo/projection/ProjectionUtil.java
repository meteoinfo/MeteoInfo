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
import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

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
                    if (i == minYI) {
                        if (y < minY) {
                            minY = y;
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

                    if (Double.isNaN(maxY)) {
                        maxY = y;
                        maxYI = i;
                    }
                    if (i == maxYI) {
                        if (y > maxY) {
                            maxY = y;
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

                    if (Double.isNaN(minX)) {
                        minX = x;
                        minXI = j;
                    }
                    if (j == minXI) {
                        if (x < minX) {
                            minX = x;
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

                    if (Double.isNaN(maxX)) {
                        maxX = x;
                        maxXI = j;
                    }
                    if (j == maxXI) {
                        if (x > maxX) {
                            maxX = x;
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
     * @param toProj To porjection
     * @return Projected polygon shape
     */
    public static PolygonShape projectPolygonShape(PolygonShape aPGS, ProjectionInfo fromProj, ProjectionInfo toProj) {
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

    /**
     * Project graphic
     *
     * @param graphic The graphic
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected graphic
     */
    public static Graphic projectGraphic(Graphic graphic, ProjectionInfo fromProj, ProjectionInfo toProj) {
        Shape shape = projectShape(graphic.getShape(), fromProj, toProj);
        return new Graphic(shape, graphic.getLegend());
    }

    public static GraphicCollection projectGraphics(GraphicCollection aGCollection, ProjectionInfo fromProj, ProjectionInfo toProj) {
        GraphicCollection newGCollection = new GraphicCollection();
        for (Graphic aGraphic : aGCollection.getGraphics()) {
            aGraphic.setShape(projectShape(aGraphic.getShape(), fromProj, toProj));
            if (aGraphic.getShape() != null) {
                newGCollection.add(aGraphic);
            }
        }

        return newGCollection;
    }

    public static List<Graphic> projectGraphics(List<Graphic> graphics, ProjectionInfo fromProj, ProjectionInfo toProj) {
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
