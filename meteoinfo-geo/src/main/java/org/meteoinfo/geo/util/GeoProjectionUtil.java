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
package org.meteoinfo.geo.util;

import org.meteoinfo.common.Extent;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointD;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.geo.layer.RasterLayer;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.geoprocess.GeometryUtil;
import org.meteoinfo.math.interpolate.InterpUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.ProjectionUtil;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.table.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yaqiang Wang
 */
public class GeoProjectionUtil {

    /**
     * Project grid data
     *
     * @param data Data array
     * @param xx X array
     * @param yy Y array
     * @param fromProj From projection
     * @param toProj To projection
     * @return Porjected grid data
     * @throws InvalidRangeException
     */
    public static Object[] reprojectGrid(Array data, Array xx, Array yy, ProjectionInfo fromProj,
                                         ProjectionInfo toProj) throws InvalidRangeException {
        //Get destination projection extent
        Extent aExtent;
        int xnum = (int)xx.getSize();
        int ynum = (int)yy.getSize();
        aExtent = ProjectionUtil.getProjectionExtent(fromProj, toProj, xx, yy);

        double xDelt = (aExtent.maxX - aExtent.minX) / (xnum - 1);
        double yDelt = (aExtent.maxY - aExtent.minY) / (ynum - 1);
        int i;
        Array rx = Array.factory(DataType.DOUBLE, new int[]{xnum});
        Array ry = Array.factory(DataType.DOUBLE, new int[]{ynum});
        for (i = 0; i < xnum; i++) {
            rx.setDouble(i, aExtent.minX + i * xDelt);
        }
        for (i = 0; i < ynum; i++) {
            ry.setDouble(i, aExtent.minY + i * yDelt);
        }

        //Projection data
        Array[] gxy = ArrayUtil.meshgrid(xx, yy);
        Array[] pxy = Reproject.reproject(gxy[0], gxy[1], fromProj, toProj);
        Array px = pxy[0];
        Array py = pxy[1];

        //Interpolation data
        Array r = InterpUtil.interpolation_Nearest(px, py, data, rx, ry, Double.POSITIVE_INFINITY);

        //Convexhull maskout
        PolygonShape polyshape = org.meteoinfo.geometry.geoprocess.GeometryUtil.convexHull(px, py);
        Array[] rxy = ArrayUtil.meshgrid(rx, ry);
        List<PolygonShape> pss = new ArrayList<>();
        pss.add(polyshape);
        r = org.meteoinfo.geometry.geoprocess.GeometryUtil.maskout(r, rxy[0], rxy[1], pss);

        return new Object[]{r, rx, ry};
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
                    aPS = ProjectionUtil.projectPointShape(aPS, fromProj, toProj);
                    if (aPS != null) {
                        shapePoints.add(aPS);
                        newPoints.add(aPS.getPoint());

                        DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
                        try {
                            aTable.addRow(aDR);
                        } catch (Exception ex) {
                            Logger.getLogger(GeoProjectionUtil.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                oLayer.setShapes(new ArrayList<>(shapePoints));
                oLayer.setExtent(GeometryUtil.getPointsExtent(newPoints));

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
                        aPLS = ProjectionUtil.projectPolylineShape(aPLS, fromProj, toProj);
                        if (aPLS != null) {
                            newPolylines.add(aPLS);

                            DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
                            try {
                                aTable.addRow(aDR);
                            } catch (Exception ex) {
                                Logger.getLogger(GeoProjectionUtil.class.getName()).log(Level.SEVERE, null, ex);
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
                        aPGS = ProjectionUtil.projectPolygonShape(aPGS, fromProj, toProj);
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
                oLayer.setLabelPoints(ProjectionUtil.projectGraphics(oLayer.getLabelPoints(), fromProj, toProj));
            } else {
                oLayer.setLabelPoints(new ArrayList<>(oLayer.getLabelPoints()));
            }
        }
    }

    /**
     * Project grid data
     *
     * @param gridArray GridArray
     * @param fromProj From projection
     * @param toProj To projection
     * @return Porjected grid data
     * @throws InvalidRangeException
     */
    public static GridArray project(GridArray gridArray, ProjectionInfo fromProj, ProjectionInfo toProj) throws InvalidRangeException {
        Array xx = ArrayUtil.array(gridArray.xArray);
        Array yy = ArrayUtil.array(gridArray.yArray);
        Object[] r = GeoProjectionUtil.reprojectGrid(gridArray.getData(),  xx, yy, fromProj, toProj);
        GridArray rdata = new GridArray((Array) r[0], (Array) r[1], (Array) r[2], gridArray.missingValue);
        rdata.projInfo = toProj;

        return rdata;
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

            oLayer.setGridData(project(oLayer.getGridData(), oLayer.getProjInfo(), toProj));
            oLayer.updateImage(oLayer.getLegendScheme());
        } catch (InvalidRangeException ex) {
            Logger.getLogger(GeoProjectionUtil.class.getName()).log(Level.SEVERE, null, ex);
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
                        ((WindArrow) aPS).angle = ProjectionUtil.projectAngle(((WindArrow) aPS).angle, toP, fromP, fromProj, toProj);
                        break;
                    case Barb:
                        ((WindBarb) aPS).angle = ProjectionUtil.projectAngle(((WindBarb) aPS).angle, toP, fromP, fromProj, toProj);
                        break;
                    case StationModel:
                        ((StationModelShape) aPS).windBarb.angle = ProjectionUtil.projectAngle(((StationModelShape) aPS).windBarb.angle, toP, fromP, fromProj, toProj);
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
        aLayer.setExtent(GeometryUtil.getPointsExtent(newPoints));
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
                            ((WindArrow) aPS).angle = ProjectionUtil.projectAngle(((WindArrow) aPS).angle, fromP, toP, fromProj, toProj);
                            break;
                        case Barb:
                            ((WindBarb) aPS).angle = ProjectionUtil.projectAngle(((WindBarb) aPS).angle, fromP, toP, fromProj, toProj);
                            break;
                        case StationModel:
                            ((StationModelShape) aPS).windBarb.angle = ProjectionUtil.projectAngle(((StationModelShape) aPS).windBarb.angle, fromP, toP, fromProj, toProj);
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
        oLayer.setExtent(GeometryUtil.getPointsExtent(newPoints));
        oLayer.getAttributeTable().setTable(aTable);

        if (oLayer.getLabelPoints().size() > 0) {
            oLayer.setLabelPoints(ProjectionUtil.projectGraphics(oLayer.getLabelPoints(), fromProj, toProj));
        }
    }

}
