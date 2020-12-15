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

import org.meteoinfo.math.ArrayUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.projection.info.ProjectionInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.RasterLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.map.ProjectionSet;
import org.meteoinfo.shape.CircleShape;
import org.meteoinfo.shape.CurveLineShape;
import org.meteoinfo.shape.CurvePolygonShape;
import org.meteoinfo.shape.EllipseShape;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.Polygon;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.Polyline;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.StationModelShape;
import org.meteoinfo.shape.WindArrow;
import org.meteoinfo.shape.WindBarb;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.ndarray.InvalidRangeException;

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

    /**
     * Get projected extent
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param X X coordinate
     * @param Y Y coordinate
     * @return Extent
     */
    public static Extent getProjectionExtent_bak2(ProjectionInfo fromProj, ProjectionInfo toProj, double[] X, double[] Y) {
        double x, y, minX = Double.NaN, minY = Double.NaN, maxX = Double.NaN, maxY = Double.NaN;
        int i, j;
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

                    if (Double.isNaN(minX)) {
                        minX = x;
                        minY = y;
                        maxX = x;
                        maxY = y;
                    } else {
                        if (x < minX) {
                            minX = x;
                        } else if (x > maxX) {
                            maxX = x;
                        }
                        if (y < minY) {
                            minY = y;
                        } else if (y > maxY) {
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
     * @param X X coordinate
     * @param Y Y coordinate
     * @return Extent
     */
    public static Extent getProjectionExtent_bak(ProjectionInfo fromProj, ProjectionInfo toProj, double[] X, double[] Y) {
        double x, y, minX = Double.NaN, minY = Double.NaN, maxX = Double.NaN, maxY = Double.NaN;
        int i;
        for (i = 0; i < Y.length; i++) {
            switch (toProj.getProjectionName()) {
                case Lambert_Conformal_Conic:
                    if (Y[i] < -80) {
                        continue;
                    }
                    break;
                case North_Polar_Stereographic_Azimuthal:
                    if (Y[i] < 0) {
                        continue;
                    }
                    break;
                case South_Polar_Stereographic_Azimuthal:
                    if (Y[i] > 0) {
                        continue;
                    }
                    break;
            }
            double[][] points = new double[1][];
            points[0] = new double[]{X[0], Y[i]};
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
            } catch (Exception e) {
                continue;
            }

            points[0] = new double[]{X[X.length - 1], Y[i]};
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                x = points[0][0];
                y = points[0][1];
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }

                if (Double.isNaN(maxX)) {
                    maxY = y;
                    maxX = x;
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

        int yIdx = 0;
        int eyIdx = Y.length - 1;
        switch (toProj.getProjectionName()) {
            case Lambert_Conformal_Conic:
                for (i = 0; i < Y.length; i++) {
                    if (Y[i] >= -80) {
                        yIdx = i;
                        break;
                    }
                }
                break;
            case North_Polar_Stereographic_Azimuthal:
                for (i = 0; i < Y.length; i++) {
                    if (Y[i] >= 0) {
                        yIdx = i;
                        break;
                    }
                }
                break;
            case South_Polar_Stereographic_Azimuthal:
                for (i = 0; i < Y.length; i++) {
                    if (Y[i] > 0) {
                        eyIdx = i - 1;
                        break;
                    }
                }
                break;
        }
        if (eyIdx < 0) {
            eyIdx = 0;
        }

        for (i = 0; i < X.length; i++) {
            double[][] points = new double[1][];
            points[0] = new double[]{X[i], Y[yIdx]};
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
            } catch (Exception e) {
                continue;
            }

            points[0] = new double[]{X[i], Y[eyIdx]};
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                x = points[0][0];
                y = points[0][1];
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
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

        Extent aExtent = new Extent();
        aExtent.minX = minX;
        aExtent.maxX = maxX;
        aExtent.minY = minY;
        aExtent.maxY = maxY;

        return aExtent;
    }

    /**
     * Project vector layer
     *
     * @param oLayer The layer
     * @param toProj To projection info
     */
    public static void projectLayer(VectorLayer oLayer, ProjectionInfo toProj) {
        double refLon = toProj.getCoordinateReferenceSystem().getProjection().getProjectionLongitudeDegrees();
        refLon += 180;
        if (refLon > 180) {
            refLon = refLon - 360;
        } else if (refLon < -180) {
            refLon = refLon + 360;
        }
        projectLayer(oLayer, toProj, refLon, true);
    }

    /**
     * Project vector layer
     *
     * @param oLayer The layer
     * @param toProj To projection info
     * @param projectLabels If projectLabels
     */
    public static void projectLayer(VectorLayer oLayer, ProjectionInfo toProj, boolean projectLabels) {
        double refLon = toProj.getCoordinateReferenceSystem().getProjection().getProjectionLongitudeDegrees();
        refLon += 180;
        if (refLon > 180) {
            refLon = refLon - 360;
        } else if (refLon < -180) {
            refLon = refLon + 360;
        }
        projectLayer(oLayer, toProj, refLon, projectLabels);
    }

    /**
     * Project vector layer
     *
     * @param oLayer The layer
     * @param toProj To projection info
     * @param refCutLon Reference clip longitude
     */
    public static void projectLayer(VectorLayer oLayer, ProjectionInfo toProj, double refCutLon) {
        projectLayer(oLayer, toProj, refCutLon, true);
    }

    /**
     * Project vector layer
     *
     * @param oLayer The layer
     * @param toProj To projection info
     * @param refCutLon Reference clip longitude
     * @param projectLabels If project labels
     */
    public static void projectLayer(VectorLayer oLayer, ProjectionInfo toProj, double refCutLon, boolean projectLabels) {
        ProjectionInfo fromProj = oLayer.getOriginProjInfo();
        if (fromProj.equals(toProj)) {
            if (oLayer.isProjected()) {
                oLayer.getOriginData();
            }

            return;
        }

        if (oLayer.isProjected()) {
            oLayer.getOriginData();
        } else {
            oLayer.updateOriginData();
        }

        double refLon = refCutLon;
        if (oLayer.getExtent().maxX > 180 && oLayer.getExtent().minX > refLon) {
            refLon += 360;
        }

        //coordinate transform process
        int i, s;
        ArrayList newPoints = new ArrayList();
        Extent lExtent = new Extent();

        DataTable aTable = new DataTable();
        for (DataColumn aDC : oLayer.getAttributeTable().getTable().getColumns()) {
            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
            aTable.getColumns().add(bDC);
        }

        //aLayer.AttributeTable.Table.Rows.Clear();
        float cutoff = toProj.getCutoff();
        switch (oLayer.getShapeType()) {
            case Point:
            case PointM:
            case PointZ:
            case WeatherSymbol:
            case WindArraw:
            case WindBarb:
            case StationModel:
                List<Shape> shapePoints = new ArrayList<>();
                newPoints.clear();                
                for (s = 0; s < oLayer.getShapeNum(); s++) {
                    PointShape aPS = (PointShape) oLayer.getShapes().get(s);
                    if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
                        switch (toProj.getProjectionName()) {
                            case Lambert_Conformal_Conic:
                                if (aPS.getPoint().Y < cutoff) {
                                    continue;
                                }
                                break;
                            case North_Polar_Stereographic_Azimuthal:
                                if (aPS.getPoint().Y < cutoff) {
                                    continue;
                                }
                                break;
                            case South_Polar_Stereographic_Azimuthal:
                                if (aPS.getPoint().Y > cutoff) {
                                    continue;
                                }
                                break;
                            case Mercator:
                                if (aPS.getPoint().Y > cutoff || aPS.getPoint().Y < -cutoff) {
                                    continue;
                                }
                                break;
                        }
                    }
                    aPS = projectPointShape(aPS, fromProj, toProj);
                    if (aPS != null) {
                        shapePoints.add(aPS);
                        newPoints.add(aPS.getPoint());

                        DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
                        try {
                            aTable.addRow(aDR);
                        } catch (Exception ex) {
                            Logger.getLogger(ProjectionSet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                oLayer.setShapes(new ArrayList<>(shapePoints));
                oLayer.setExtent(MIMath.getPointsExtent(newPoints));

                break;
            case Polyline:
            case PolylineM:
            case PolylineZ:
                List<Shape> newPolylines = new ArrayList<>();
                for (s = 0; s < oLayer.getShapeNum(); s++) {
                    PolylineShape aPLS = (PolylineShape) oLayer.getShapes().get(s);
                    List<PolylineShape> plsList = new ArrayList<>();
                    if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
                        switch (toProj.getProjectionName()) {
                            case Lambert_Conformal_Conic:
                                if (aPLS.getExtent().minY < cutoff) {
                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, cutoff, true);
                                }
                                break;
                            case North_Polar_Stereographic_Azimuthal:
                                if (aPLS.getExtent().minY < cutoff) {
                                    //continue;
                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, cutoff, true);
                                }
                                break;
                            case South_Polar_Stereographic_Azimuthal:
                                if (aPLS.getExtent().maxY > cutoff) {
                                    //continue;
                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, cutoff, false);
                                }
                                break;
                            case Mercator:
                                if (aPLS.getExtent().maxY > cutoff) {
                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, cutoff, false);
                                }
                                if (aPLS.getExtent().minY < -cutoff) {
                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, -cutoff, true);
                                }
                                break;
                        }
                        if (aPLS == null) {
                            continue;
                        }

//                        aPLS = GeoComputation.clipPolylineShape_Lon(aPLS, refLon);
//                        if (aPLS == null) {
//                            continue;
//                        }
                        if (aPLS.getExtent().minX <= refLon && aPLS.getExtent().maxX >= refLon) {
                            switch (toProj.getProjectionName()) {
                                case North_Polar_Stereographic_Azimuthal:
                                case South_Polar_Stereographic_Azimuthal:
                                    plsList.add(aPLS);
                                    break;
                                default:
                                    plsList.add(GeoComputation.clipPolylineShape_Lon(aPLS, refLon));
                                    break;
                            }
                        } else {
                            plsList.add(aPLS);
                        }
                    } else {
                        plsList.add(aPLS);
                    }
                    for (i = 0; i < plsList.size(); i++) {
                        aPLS = plsList.get(i);
                        aPLS = projectPolylineShape(aPLS, fromProj, toProj);
                        if (aPLS != null) {
                            newPolylines.add(aPLS);

                            DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
                            try {
                                aTable.addRow(aDR);
                            } catch (Exception ex) {
                                Logger.getLogger(ProjectionSet.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            if (s == 0 && i == 0) {
                                lExtent = (Extent) aPLS.getExtent().clone();
                            } else {
                                lExtent = MIMath.getLagerExtent(lExtent, aPLS.getExtent());
                            }
                        }
                    }
                }
                oLayer.setShapes(new ArrayList<>(newPolylines));
                newPolylines.clear();
                oLayer.setExtent(lExtent);
                break;
            case Polygon:
            case PolygonM:
            case PolygonZ:
                List<Shape> newPolygons = new ArrayList<>();
                for (s = 0; s < oLayer.getShapeNum(); s++) {
                    DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
                    PolygonShape aPGS = (PolygonShape) oLayer.getShapes().get(s);
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
                            continue;
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
                    for (i = 0; i < pgsList.size(); i++) {
                        aPGS = pgsList.get(i);
                        aPGS = projectPolygonShape(aPGS, fromProj, toProj);
                        if (aPGS != null) {
                            newPolygons.add(aPGS);

                            aTable.getRows().add(aDR);

                            if (s == 0) {
                                lExtent = (Extent) aPGS.getExtent().clone();
                            } else {
                                lExtent = MIMath.getLagerExtent(lExtent, aPGS.getExtent());
                            }
                        }
                    }
                }
                oLayer.setShapes(new ArrayList<>(newPolygons));
                newPolygons.clear();
                oLayer.setExtent(lExtent);
                break;
        }
        oLayer.getAttributeTable().setTable(aTable);
        oLayer.setProjInfo(toProj);

        if (oLayer.getLabelPoints().size() > 0) {
            if (projectLabels) {
                oLayer.setLabelPoints(projectGraphics(oLayer.getLabelPoints(), fromProj, toProj));
            } else {
                oLayer.setLabelPoints(new ArrayList<>(oLayer.getLabelPoints()));
            }
        }
    }

    /**
     * Project raster layer
     *
     * @param oLayer The layer
     * @param toProj To projection
     */
    public static void projectLayer(RasterLayer oLayer, ProjectionInfo toProj) {
        try {
            if (oLayer.getProjInfo().toProj4String().equals(toProj.toProj4String())) {
                if (oLayer.isProjected()) {
                    oLayer.getOriginData();
                    oLayer.updateGridData();
                    if (oLayer.getLegendScheme().getBreakNum() < 50) {
                        oLayer.updateImage();
                    } else {
                        oLayer.setPaletteByLegend();
                    }
                }
                return;
            }

            if (!oLayer.isProjected()) {
                oLayer.updateOriginData();
            } else {
                oLayer.getOriginData();
            }

            oLayer.setGridData(oLayer.getGridData().project(oLayer.getProjInfo(), toProj));
            oLayer.updateImage(oLayer.getLegendScheme());
        } catch (InvalidRangeException ex) {
            Logger.getLogger(ProjectionSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Project layer angle
     *
     * @param oLayer The layer
     * @param fromProj From projection
     * @param toProj To projection
     * @return VectorLayer
     */
    public static VectorLayer projectLayerAngle(VectorLayer oLayer, ProjectionInfo fromProj, ProjectionInfo toProj) {
        //coordinate transform process            
        ArrayList newPoints = new ArrayList();

        VectorLayer aLayer = (VectorLayer) oLayer.clone();

        //aLayer.AttributeTable.Table = oLayer.AttributeTable.Table.Clone();
        DataTable aTable = new DataTable();
        for (DataColumn aDC : oLayer.getAttributeTable().getTable().getColumns()) {
            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
            aTable.getColumns().add(bDC);
        }

        int s;
        List<Shape> vectors = new ArrayList<>();
        newPoints.clear();
        for (s = 0; s < aLayer.getShapeNum(); s++) {
            PointShape aPS = (PointShape) aLayer.getShapes().get(s);
            if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
                switch (toProj.getProjectionName()) {
                    case Lambert_Conformal_Conic:
                    case North_Polar_Stereographic_Azimuthal:
                        if (aPS.getPoint().X < -89) {
                            continue;
                        }
                        break;
                    case South_Polar_Stereographic_Azimuthal:
                        if (aPS.getPoint().Y > 89) {
                            continue;
                        }
                        break;
                }
            }
            double[] fromP = new double[]{aPS.getPoint().X, aPS.getPoint().Y};
            double[] toP;
            double[][] points = new double[1][];
            points[0] = (double[]) fromP.clone();
            try {
                //Reproject point back to fromProj
                Reproject.reprojectPoints(points, toProj, fromProj, 0, points.length);
                toP = points[0];
                switch (aLayer.getLayerDrawType()) {
                    case Vector:
                        ((WindArrow) aPS).angle = projectAngle(((WindArrow) aPS).angle, toP, fromP, fromProj, toProj);
                        break;
                    case Barb:
                        ((WindBarb) aPS).angle = projectAngle(((WindBarb) aPS).angle, toP, fromP, fromProj, toProj);
                        break;
                    case StationModel:
                        ((StationModelShape) aPS).windBarb.angle = projectAngle(((StationModelShape) aPS).windBarb.angle, toP, fromP, fromProj, toProj);
                        break;
                }
                newPoints.add(aPS.getPoint());
                vectors.add(aPS);

                DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
                aTable.getRows().add(aDR);
            } catch (Exception e) {
            }
        }
        aLayer.setShapes(new ArrayList<>(vectors));
        aLayer.setExtent(MIMath.getPointsExtent(newPoints));
        aLayer.getAttributeTable().setTable(aTable);

        return aLayer;
    }
    
    /**
     * Project wind layer
     *
     * @param oLayer Origin layer
     * @param toProj To projection
     * @param IfReprojectAngle If reproject wind angle
     */
    public static void projectWindLayer(VectorLayer oLayer, ProjectionInfo toProj, boolean IfReprojectAngle) {
        ProjectionInfo fromProj = oLayer.getProjInfo();
        if (fromProj.toProj4String().equals(toProj.toProj4String())) {
            if (oLayer.isProjected()) {
                oLayer.getOriginData();
            }

            return;
        }

        if (oLayer.isProjected()) {
            oLayer.getOriginData();
        } else {
            oLayer.updateOriginData();
        }

        //Set reference longitude
        double refLon = toProj.getRefCutLon();
        if (oLayer.getExtent().maxX > 180 && oLayer.getExtent().minX > refLon) {
            refLon += 360;
        }

        //coordinate transform process
        int s;
        //PointD wPoint = new PointD();
        PointD aPoint;
        List<PointD> newPoints = new ArrayList<>();
        //Extent lExtent = new Extent();

        DataTable aTable = new DataTable();
        for (DataColumn aDC : oLayer.getAttributeTable().getTable().getColumns()) {
            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
            aTable.getColumns().add(bDC);
        }

        List<Shape> shapes = new ArrayList<>();
        newPoints.clear();
        for (s = 0; s < oLayer.getShapeNum(); s++) {
            PointShape aPS = (PointShape) oLayer.getShapes().get(s);
            if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
                switch (toProj.getProjectionName()) {
                    case Lambert_Conformal_Conic:
                    case North_Polar_Stereographic_Azimuthal:
                        if (aPS.getPoint().Y < -89) {
                            continue;
                        }
                        break;
                    case South_Polar_Stereographic_Azimuthal:
                        if (aPS.getPoint().Y > 89) {
                            continue;
                        }
                        break;
                }
            }
            double[] fromP = new double[]{aPS.getPoint().X, aPS.getPoint().Y};
            double[] toP;
            double[][] points = new double[1][];
            points[0] = (double[]) fromP.clone();
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
                toP = points[0];
                aPoint = new PointD();
                aPoint.X = (float) toP[0];
                aPoint.Y = (float) toP[1];
                aPS.setPoint(aPoint);
                if (IfReprojectAngle) {
                    switch (oLayer.getLayerDrawType()) {
                        case Vector:
                            ((WindArrow) aPS).angle = projectAngle(((WindArrow) aPS).angle, fromP, toP, fromProj, toProj);
                            break;
                        case Barb:
                            ((WindBarb) aPS).angle = projectAngle(((WindBarb) aPS).angle, fromP, toP, fromProj, toProj);
                            break;
                        case StationModel:
                            ((StationModelShape) aPS).windBarb.angle = projectAngle(((StationModelShape) aPS).windBarb.angle, fromP, toP, fromProj, toProj);
                            break;
                    }
                }
                newPoints.add(aPoint);
                shapes.add(aPS);

                DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
                aTable.getRows().add(aDR);
            } catch (Exception e) {
            }
        }
        oLayer.setShapes(new ArrayList<>(shapes));
        oLayer.setExtent(MIMath.getPointsExtent(newPoints));
        oLayer.getAttributeTable().setTable(aTable);

        if (oLayer.getLabelPoints().size() > 0) {
            oLayer.setLabelPoints(projectGraphics(oLayer.getLabelPoints(), fromProj, toProj));
        }
    }

    private static PointShape projectPointShape(PointShape aPS, ProjectionInfo fromProj, ProjectionInfo toProj) {
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
                    case WindBarb:
                        ((WindBarb) newPS).angle = projectAngle(((WindBarb) newPS).angle, fromP, toP, fromProj, toProj);
                        break;
                    case WindArraw:
                        ((WindArrow) newPS).angle = projectAngle(((WindArrow) newPS).angle, fromP, toP, fromProj, toProj);
                        break;
                    case StationModel:
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

    private static PolylineShape projectPolylineShape(PolylineShape aPLS, ProjectionInfo fromProj, ProjectionInfo toProj) {
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

    private static GraphicCollection projectGraphics(GraphicCollection aGCollection, ProjectionInfo fromProj, ProjectionInfo toProj) {
        GraphicCollection newGCollection = new GraphicCollection();
        for (Graphic aGraphic : aGCollection.getGraphics()) {
            aGraphic.setShape(projectShape(aGraphic.getShape(), fromProj, toProj));
            if (aGraphic.getShape() != null) {
                newGCollection.add(aGraphic);
            }
        }

        return newGCollection;
    }

    private static List<Graphic> projectGraphics(List<Graphic> graphics, ProjectionInfo fromProj, ProjectionInfo toProj) {
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
            case Point:
            case PointM:
                newShape = projectPointShape((PointShape) aShape, fromProj, toProj);
                break;
            case Polyline:
            case PolylineM:
                newShape = projectPolylineShape((PolylineShape) aShape, fromProj, toProj);
                break;
            case CurveLine:
                newShape = projectCurvelineShape((CurveLineShape) aShape, fromProj, toProj);
                break;
            case Polygon:
            case PolygonM:
            case Rectangle:
                newShape = projectPolygonShape((PolygonShape) aShape, fromProj, toProj);
                break;
            case CurvePolygon:
                newShape = projectCurvePolygonShape((CurvePolygonShape) aShape, fromProj, toProj);
                break;
            case Circle:
                newShape = projectCircleShape((CircleShape) aShape, fromProj, toProj);
                break;
            case Ellipse:
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
