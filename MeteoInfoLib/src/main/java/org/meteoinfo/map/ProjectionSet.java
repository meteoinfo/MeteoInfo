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
package org.meteoinfo.map;

import org.meteoinfo.global.event.IProjectionChangedListener;
import org.meteoinfo.global.event.ProjectionChangedEvent;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.RasterLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.meteoinfo.projection.ProjectionUtil;
import org.meteoinfo.projection.proj4j.CRSFactory;

/**
 *
 * @author Yaqiang Wang
 */
public class ProjectionSet {
    // <editor-fold desc="Variables">

    private final EventListenerList _listeners = new EventListenerList();
    CRSFactory _crsFactory = new CRSFactory();
    private ProjectionInfo _projInfo;
    //private String _projStr;
    //private double _refLon;
    //private double _refCutLon;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public ProjectionSet() {
        _projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
        //_projStr = _projInfo.getParameterString();
        //_refLon = 0;
    }
    // </editor-fold>

    // <editor-fold desc="Events">
    public void addProjectionChangedListener(IProjectionChangedListener listener) {
        this._listeners.add(IProjectionChangedListener.class, listener);
    }

    public void removeViewExtentChangedListener(IProjectionChangedListener listener) {
        this._listeners.remove(IProjectionChangedListener.class, listener);
    }

    public void fireProjectionChangedEvent() {
        fireProjectionChangedEvent(new ProjectionChangedEvent(this));
    }

    private void fireProjectionChangedEvent(ProjectionChangedEvent event) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IProjectionChangedListener.class) {
                ((IProjectionChangedListener) listeners[i + 1]).projectionChangedEvent(event);
            }
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get if is Lon/Lat projection
     *
     * @return Boolean
     */
    public boolean isLonLatMap() {
        return "longlat".equals(_projInfo.getCoordinateReferenceSystem().getProjection().toString().toLowerCase());
    }

    /**
     * Get projection info
     *
     * @return Projection Info
     */
    public ProjectionInfo getProjInfo() {
        return _projInfo;
    }

    /**
     * Set projection info
     *
     * @param projInfo The Projection info
     */
    public void setProjInfo(ProjectionInfo projInfo) {
        _projInfo = projInfo;
    }

    /**
     * Get Porj4 string
     *
     * @return Proj4 string
     */
    public String getProjStr() {
        return _projInfo.toProj4String();
    }

    /**
     * Set proj4 string
     *
     * @param projStr Porj4 string
     */
    public void setProjStr(String projStr) {
        _projInfo = ProjectionInfo.factory(projStr);
    }

//    /**
//     * Get reference longitude
//     *
//     * @return Reference longitue
//     */
//    public double getRefLon() {
//        return _refLon;
//    }
//
//    /**
//     * Set reference longitude
//     *
//     * @param lon Reference longitude
//     */
//    public void setRefLon(double lon) {
//        _refLon = lon;
//    }

//    /**
//     * Get reference cut longitude
//     *
//     * @return Reference cut longitude
//     */
//    public double getRefCutLon() {
//        return _refCutLon;
//    }
//
//    /**
//     * Set reference cut longitude
//     *
//     * @param lon
//     */
//    public void setRefCutLon(double lon) {
//        _refCutLon = lon;
//    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get projected extent from lon/lat
     *
     * @param sExtent Lon/lat extent
     * @return Projected extent
     */
    public Extent getProjectedExtentFromLonLat(Extent sExtent) {
        Extent aExtent = new Extent();
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        ProjectionInfo toProj = _projInfo;

        //Get the border of longitude and latitude            
        double[][] points = new double[4][];
        points[0] = new double[]{sExtent.minX, sExtent.minY};
        points[1] = new double[]{sExtent.minX, sExtent.maxY};
        points[2] = new double[]{sExtent.maxX, sExtent.maxY};
        points[3] = new double[]{sExtent.maxX, sExtent.minY};
        Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);

        //Get lon lat extent
        aExtent.minX = Math.min(points[0][0], points[1][0]);
        aExtent.minY = Math.min(points[0][1], points[3][1]);
        aExtent.maxX = Math.max(points[2][0], points[3][0]);
        aExtent.maxY = Math.max(points[1][1], points[2][1]);

        return aExtent;
    }

    /**
     * Project layers
     *
     * @param aMapView The Map view
     * @param toProj To projection info
     */
    public void projectLayers(MapView aMapView, ProjectionInfo toProj) {
        projectLayers(aMapView, toProj, true);
    }

    /**
     * Project layers
     *
     * @param aMapView The map view
     * @param toProj To projection
     * @param isUpdateView If repaint mapview
     */
    public void projectLayers(MapView aMapView, ProjectionInfo toProj, boolean isUpdateView) {
        if (aMapView.getProjection().getProjInfo().toProj4String().equals(toProj.toProj4String())) {
            return;
        }

        aMapView.setLockViewUpdate(true);

        ProjectionInfo fromProj = aMapView.getProjection().getProjInfo();

        aMapView.getProjection().setProjInfo(toProj);
        double refLon = toProj.getRefCutLon();
        //aMapView.getProjection().setRefCutLon(refLon);

        for (int i = 0; i < aMapView.getLayers().size(); i++) {
            switch (aMapView.getLayers().get(i).getLayerType()) {
                case VectorLayer:
                    VectorLayer oLayer = (VectorLayer) aMapView.getLayers().get(i);
                    //projectLayer(oLayer, toProj);
                    ProjectionUtil.projectLayer(oLayer, toProj);
                    break;
                case RasterLayer:
                    RasterLayer oRLayer = (RasterLayer) aMapView.getLayers().get(i);
                    ProjectionUtil.projectLayer(oRLayer, toProj);                    
                    break;
            }
        }

        //Project graphics
        if (aMapView.getGraphicCollection().size() > 0) {
            GraphicCollection newGCollection = projectGraphics(aMapView.getGraphicCollection(), fromProj, toProj);
            aMapView.setGraphicCollection(newGCollection);
        }

        aMapView.setExtent(aMapView.getLayersWholeExtent());
        Extent aExten = aMapView.getExtent();
        aMapView.setLonLatLayer(aMapView.generateLonLatLayer());
        ProjectionUtil.projectLayer(aMapView.getLonLatLayer(), toProj);
        //aMapView.setLonLatProjLayer(aMapView.getLonLatLayer());
        for (int i = 0; i < aMapView.getLonLatLayer().getShapeNum(); i++) {
            PolylineShape aPLS = (PolylineShape) aMapView.getLonLatLayer().getShapes().get(i);
            if (aPLS.getPolylines().size() == 2) {
                PointD aP = aPLS.getPolylines().get(0).getPointList().get(aPLS.getPolylines().get(0).getPointList().size() - 1);
                PointD bP = aPLS.getPolylines().get(1).getPointList().get(aPLS.getPolylines().get(1).getPointList().size() - 1);
                boolean isJoin = false;
                if (refLon == 0) {
                    if (Math.abs(aP.X) < 0.1 && Math.abs(bP.X) < 0.1 && MIMath.doubleEquals(aP.Y, bP.Y)) {
                        isJoin = true;
                    }
                } else if (MIMath.doubleEquals(aP.X, bP.X) && MIMath.doubleEquals(aP.Y, bP.Y)) {
                    isJoin = true;
                }

                if (isJoin) {
                    List<Polyline> polyLines = new ArrayList<>();
                    Polyline aPL = new Polyline();
                    List<PointD> pList = (List<PointD>) new ArrayList<>(aPLS.getPolylines().get(1).getPointList());
                    List<PointD> bPList = (List<PointD>) new ArrayList<>(aPLS.getPolylines().get(0).getPointList());
                    Collections.reverse(bPList);
                    pList.addAll(bPList);
                    aPL.setPointList(pList);
                    polyLines.add(aPL);
                    aPLS.setPolylines(polyLines);
                    //aMapView.getLonLatProjLayer().getShapes().get(i) = aPLS;
                }
            }
        }

        if (isUpdateView) {
            aMapView.setLockViewUpdate(false);
        }
        aMapView.zoomToExtent(aExten);

        this.fireProjectionChangedEvent();
    }

//    /**
//     * Project raster layer
//     *
//     * @param oLayer The layer
//     * @param toProj To projection
//     */
//    public void projectLayer(RasterLayer oLayer, ProjectionInfo toProj) {
//        try {
//            //            if (toProj.getProjectionName() == ProjectionNames.Robinson) {
////                return;
////            }
//
//            if (oLayer.getProjInfo().toProj4String().equals(toProj.toProj4String())) {
//                if (oLayer.isProjected()) {
//                    oLayer.getOriginData();
//                    oLayer.updateGridData();
//                    if (oLayer.getLegendScheme().getBreakNum() < 50) {
//                        oLayer.updateImage();
//                    } else {
//                        oLayer.setPaletteByLegend();
//                    }
//                }
//                return;
//            }
//
//            if (!oLayer.isProjected()) {
//                oLayer.updateOriginData();
//            } else {
//                oLayer.getOriginData();
//            }
//
//            oLayer.setGridData(oLayer.getGridData().project(oLayer.getProjInfo(), toProj));
//            oLayer.updateImage(oLayer.getLegendScheme());
////            if (oLayer.getLegendScheme().getBreakNum() < 50) {
////                oLayer.updateImage(oLayer.getLegendScheme());
////            } else {
////                oLayer.setPaletteByLegend();
////            }
//        } catch (InvalidRangeException ex) {
//            Logger.getLogger(ProjectionSet.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    /**
//     * Project vector layer
//     *
//     * @param oLayer The layer
//     * @param toProj To projection info
//     */
//    public void projectLayer(VectorLayer oLayer, ProjectionInfo toProj) {
//        projectLayer(oLayer, toProj, true);
//    }
//
//    /**
//     * Project vector layer
//     *
//     * @param oLayer The layer
//     * @param toProj To projection info
//     * @param projectLabels If project labels
//     */
//    public void projectLayer(VectorLayer oLayer, ProjectionInfo toProj, boolean projectLabels) {
//        ProjectionInfo fromProj = oLayer.getProjInfo();
//        if (fromProj.equals(toProj)) {
//            if (oLayer.isProjected()) {
//                oLayer.getOriginData();
//            }
//
//            return;
//        }
//
//        if (oLayer.isProjected()) {
//            oLayer.getOriginData();
//        } else {
//            oLayer.updateOriginData();
//        }
//
//        double refLon = toProj.getRefCutLon();
//        if (oLayer.getExtent().maxX > 180 && oLayer.getExtent().minX > refLon) {
//            refLon += 360;
//        }
//
//        //coordinate transform process
//        int i, s;
//        ArrayList newPoints = new ArrayList();
//        Extent lExtent = new Extent();
//
//        DataTable aTable = new DataTable();
//        for (DataColumn aDC : oLayer.getAttributeTable().getTable().getColumns()) {
//            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
//            aTable.getColumns().add(bDC);
//        }
//
//        //aLayer.AttributeTable.Table.Rows.Clear();
//        switch (oLayer.getShapeType()) {
//            case Point:
//            case PointM:
//            case PointZ:
//            case WeatherSymbol:
//            case WindArraw:
//            case WindBarb:
//            case StationModel:
//                List<Shape> shapePoints = new ArrayList<>();
//                newPoints.clear();
//                for (s = 0; s < oLayer.getShapeNum(); s++) {
//                    PointShape aPS = (PointShape) oLayer.getShapes().get(s);
//                    if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
//                        switch (toProj.getProjectionName()) {
//                            case Lambert_Conformal_Conic:
//                                if (aPS.getPoint().Y < -80) {
//                                    continue;
//                                }
//                                break;
//                            case North_Polar_Stereographic_Azimuthal:
//                                if (aPS.getPoint().Y < 0) {
//                                    continue;
//                                }
//                                break;
//                            case South_Polar_Stereographic_Azimuthal:
//                                if (aPS.getPoint().Y > 0) {
//                                    continue;
//                                }
//                                break;
//                            case Mercator:
//                                if (aPS.getPoint().Y > 85.0511 || aPS.getPoint().Y < -85.0511) {
//                                    continue;
//                                }
//                                break;
//                        }
//                    }
//                    aPS = projectPointShape(aPS, fromProj, toProj);
//                    if (aPS != null) {
//                        shapePoints.add(aPS);
//                        newPoints.add(aPS.getPoint());
//
//                        DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
//                        try {
//                            aTable.addRow(aDR);
//                        } catch (Exception ex) {
//                            Logger.getLogger(ProjectionSet.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//                oLayer.setShapes(new ArrayList<>(shapePoints));
//                oLayer.setExtent(MIMath.getPointsExtent(newPoints));
//
//                break;
//            case Polyline:
//            case PolylineM:
//            case PolylineZ:
//                List<Shape> newPolylines = new ArrayList<>();
//                for (s = 0; s < oLayer.getShapeNum(); s++) {
//                    PolylineShape aPLS = (PolylineShape) oLayer.getShapes().get(s);
//                    List<PolylineShape> plsList = new ArrayList<>();
//                    if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
//                        switch (toProj.getProjectionName()) {
//                            case Lambert_Conformal_Conic:
//                                if (aPLS.getExtent().minY < -80) {
//                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, -80, true);
//                                }
//                                break;
//                            case North_Polar_Stereographic_Azimuthal:
//                                if (aPLS.getExtent().minY < 0) {
//                                    //continue;
//                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, 0, true);
//                                }
//                                break;
//                            case South_Polar_Stereographic_Azimuthal:
//                                if (aPLS.getExtent().maxY > 0) {
//                                    //continue;
//                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, 0, false);
//                                }
//                                break;
//                            case Mercator:
//                                if (aPLS.getExtent().maxY > 85.0511) {
//                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, 85.0511, false);
//                                }
//                                if (aPLS.getExtent().minY < -85.0511) {
//                                    aPLS = GeoComputation.clipPolylineShape_Lat(aPLS, -85.0511, true);
//                                }
//                                break;
//                        }
//                        if (aPLS == null) {
//                            continue;
//                        }
//
////                        aPLS = GeoComputation.clipPolylineShape_Lon(aPLS, refLon);
////                        if (aPLS == null) {
////                            continue;
////                        }
//                        if (aPLS.getExtent().minX < refLon && aPLS.getExtent().maxX > refLon) {
//                            plsList.add(GeoComputation.clipPolylineShape_Lon(aPLS, refLon));
//                        } else {
//                            plsList.add(aPLS);
//                        }
//                    } else {
//                        plsList.add(aPLS);
//                    }
//                    for (i = 0; i < plsList.size(); i++) {
//                        aPLS = plsList.get(i);
//                        aPLS = projectPolylineShape(aPLS, fromProj, toProj);
//                        if (aPLS != null) {
//                            newPolylines.add(aPLS);
//
//                            DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
//                            try {
//                                aTable.addRow(aDR);
//                            } catch (Exception ex) {
//                                Logger.getLogger(ProjectionSet.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//
//                            if (s == 0 && i == 0) {
//                                lExtent = (Extent) aPLS.getExtent().clone();
//                            } else {
//                                lExtent = MIMath.getLagerExtent(lExtent, aPLS.getExtent());
//                            }
//                        }
//                    }
//                }
//                oLayer.setShapes(new ArrayList<>(newPolylines));
//                newPolylines.clear();
//                oLayer.setExtent(lExtent);
//                break;
//            case Polygon:
//            case PolygonM:
//                List<Shape> newPolygons = new ArrayList<>();
//                for (s = 0; s < oLayer.getShapeNum(); s++) {
//                    DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
//                    PolygonShape aPGS = (PolygonShape) oLayer.getShapes().get(s);
//                    List<PolygonShape> pgsList = new ArrayList<>();
//                    if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
//                        switch (toProj.getProjectionName()) {
//                            case Lambert_Conformal_Conic:
//                                if (aPGS.getExtent().minY < -80) {
//                                    aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, -80, true);
//                                }
//                                break;
//                            case North_Polar_Stereographic_Azimuthal:
//                                if (aPGS.getExtent().minY < 0) {
//                                    //continue;
//                                    aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, 0, true);
//                                }
//                                break;
//                            case South_Polar_Stereographic_Azimuthal:
//                                if (aPGS.getExtent().maxY > 0) {
//                                    //continue;
//                                    aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, 0, false);
//                                }
//                                break;
//                            case Mercator:
//                                if (aPGS.getExtent().maxY > 85.0511) {
//                                    aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, 85.0511, false);
//                                }
//                                if (aPGS.getExtent().minY < -85.0511) {
//                                    aPGS = GeoComputation.clipPolygonShape_Lat(aPGS, -85.0511, true);
//                                }
//                                break;
//                        }
//                        if (aPGS == null) {
//                            continue;
//                        }
//
//                        if (aPGS.getExtent().minX <= refLon && aPGS.getExtent().maxX >= refLon) {
//                            pgsList.add(GeoComputation.clipPolygonShape_Lon(aPGS, refLon));
//                        } else {
//                            pgsList.add(aPGS);
//                        }
//                    } else {
//                        pgsList.add(aPGS);
//                    }
//                    for (i = 0; i < pgsList.size(); i++) {
//                        aPGS = pgsList.get(i);
//                        aPGS = projectPolygonShape(aPGS, fromProj, toProj);
//                        if (aPGS != null) {
//                            newPolygons.add(aPGS);
//
//                            aTable.getRows().add(aDR);
//
//                            if (s == 0) {
//                                lExtent = (Extent) aPGS.getExtent().clone();
//                            } else {
//                                lExtent = MIMath.getLagerExtent(lExtent, aPGS.getExtent());
//                            }
//                        }
//                    }
//                }
//                oLayer.setShapes(new ArrayList<>(newPolygons));
//                newPolygons.clear();
//                oLayer.setExtent(lExtent);
//                break;
//        }
//        oLayer.getAttributeTable().setTable(aTable);
//
//        if (oLayer.getLabelPoints().size() > 0) {
//            if (projectLabels) {
//                oLayer.setLabelPoints(projectGraphics(oLayer.getLabelPoints(), fromProj, toProj));
//            } else {
//                oLayer.setLabelPoints(new ArrayList<>(oLayer.getLabelPoints()));
//            }
//        }
//    }
//
//    /**
//     * Project layer angle
//     *
//     * @param oLayer The layer
//     * @param fromProj From projection
//     * @param toProj To projection
//     * @return VectorLayer
//     */
//    public VectorLayer projectLayerAngle(VectorLayer oLayer, ProjectionInfo fromProj, ProjectionInfo toProj) {
//        //coordinate transform process            
//        ArrayList newPoints = new ArrayList();
//
//        VectorLayer aLayer = (VectorLayer) oLayer.clone();
//
//        //aLayer.AttributeTable.Table = oLayer.AttributeTable.Table.Clone();
//        DataTable aTable = new DataTable();
//        for (DataColumn aDC : oLayer.getAttributeTable().getTable().getColumns()) {
//            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
//            aTable.getColumns().add(bDC);
//        }
//
//        int s;
//        List<Shape> vectors = new ArrayList<>();
//        newPoints.clear();
//        for (s = 0; s < aLayer.getShapeNum(); s++) {
//            PointShape aPS = (PointShape) aLayer.getShapes().get(s);
//            if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
//                switch (toProj.getProjectionName()) {
//                    case Lambert_Conformal_Conic:
//                    case North_Polar_Stereographic_Azimuthal:
//                        if (aPS.getPoint().X < -89) {
//                            continue;
//                        }
//                        break;
//                    case South_Polar_Stereographic_Azimuthal:
//                        if (aPS.getPoint().Y > 89) {
//                            continue;
//                        }
//                        break;
//                }
//            }
//            double[] fromP = new double[]{aPS.getPoint().X, aPS.getPoint().Y};
//            double[] toP;
//            double[][] points = new double[1][];
//            points[0] = (double[]) fromP.clone();
//            try {
//                //Reproject point back to fromProj
//                Reproject.reprojectPoints(points, toProj, fromProj, 0, points.length);
//                toP = points[0];
//                switch (aLayer.getLayerDrawType()) {
//                    case Vector:
//                        ((WindArrow) aPS).angle = projectAngle(((WindArrow) aPS).angle, toP, fromP, fromProj, toProj);
//                        break;
//                    case Barb:
//                        ((WindBarb) aPS).angle = projectAngle(((WindBarb) aPS).angle, toP, fromP, fromProj, toProj);
//                        break;
//                    case StationModel:
//                        ((StationModelShape) aPS).windBarb.angle = projectAngle(((StationModelShape) aPS).windBarb.angle, toP, fromP, fromProj, toProj);
//                        break;
//                }
//                newPoints.add(aPS.getPoint());
//                vectors.add(aPS);
//
//                DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
//                aTable.getRows().add(aDR);
//            } catch (Exception e) {
//            }
//        }
//        aLayer.setShapes(new ArrayList<>(vectors));
//        aLayer.setExtent(MIMath.getPointsExtent(newPoints));
//        aLayer.getAttributeTable().setTable(aTable);
//
//        //if (aLayer.LabelSetV.DrawLabels)
//        //{
//        //    aLayer.AddLabels();
//        //}
//        return aLayer;
//    }

//    /**
//     * Project wind layer
//     *
//     * @param oLayer Origin layer
//     * @param toProj To projection
//     * @param IfReprojectAngle If reproject wind angle
//     */
//    public void projectWindLayer(VectorLayer oLayer, ProjectionInfo toProj, boolean IfReprojectAngle) {
//        ProjectionInfo fromProj = oLayer.getProjInfo();
//        if (fromProj.toProj4String().equals(toProj.toProj4String())) {
//            if (oLayer.isProjected()) {
//                oLayer.getOriginData();
//            }
//
//            return;
//        }
//
//        if (oLayer.isProjected()) {
//            oLayer.getOriginData();
//        } else {
//            oLayer.updateOriginData();
//        }
//
//        //Set reference longitude
//        double refLon = toProj.getRefCutLon();
//        if (oLayer.getExtent().maxX > 180 && oLayer.getExtent().minX > refLon) {
//            refLon += 360;
//        }
//
//        //coordinate transform process
//        int s;
//        //PointD wPoint = new PointD();
//        PointD aPoint;
//        List<PointD> newPoints = new ArrayList<>();
//        //Extent lExtent = new Extent();
//
//        DataTable aTable = new DataTable();
//        for (DataColumn aDC : oLayer.getAttributeTable().getTable().getColumns()) {
//            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
//            aTable.getColumns().add(bDC);
//        }
//
//        List<Shape> shapes = new ArrayList<>();
//        newPoints.clear();
//        for (s = 0; s < oLayer.getShapeNum(); s++) {
//            PointShape aPS = (PointShape) oLayer.getShapes().get(s);
//            if (fromProj.getProjectionName() == ProjectionNames.LongLat) {
//                switch (toProj.getProjectionName()) {
//                    case Lambert_Conformal_Conic:
//                    case North_Polar_Stereographic_Azimuthal:
//                        if (aPS.getPoint().Y < -89) {
//                            continue;
//                        }
//                        break;
//                    case South_Polar_Stereographic_Azimuthal:
//                        if (aPS.getPoint().Y > 89) {
//                            continue;
//                        }
//                        break;
//                }
//            }
//            double[] fromP = new double[]{aPS.getPoint().X, aPS.getPoint().Y};
//            double[] toP;
//            double[][] points = new double[1][];
//            points[0] = (double[]) fromP.clone();
//            try {
//                Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
//                toP = points[0];
//                aPoint = new PointD();
//                aPoint.X = (float) toP[0];
//                aPoint.Y = (float) toP[1];
//                aPS.setPoint(aPoint);
//                if (IfReprojectAngle) {
//                    switch (oLayer.getLayerDrawType()) {
//                        case Vector:
//                            ((WindArrow) aPS).angle = projectAngle(((WindArrow) aPS).angle, fromP, toP, fromProj, toProj);
//                            break;
//                        case Barb:
//                            ((WindBarb) aPS).angle = projectAngle(((WindBarb) aPS).angle, fromP, toP, fromProj, toProj);
//                            break;
//                        case StationModel:
//                            ((StationModelShape) aPS).windBarb.angle = projectAngle(((StationModelShape) aPS).windBarb.angle, fromP, toP, fromProj, toProj);
//                            break;
//                    }
//                }
//                newPoints.add(aPoint);
//                shapes.add(aPS);
//
//                DataRow aDR = oLayer.getAttributeTable().getTable().getRows().get(s);
//                aTable.getRows().add(aDR);
//            } catch (Exception e) {
//            }
//        }
//        oLayer.setShapes(new ArrayList<>(shapes));
//        oLayer.setExtent(MIMath.getPointsExtent(newPoints));
//        oLayer.getAttributeTable().setTable(aTable);
//
//        if (oLayer.getLabelPoints().size() > 0) {
//            oLayer.setLabelPoints(projectGraphics(oLayer.getLabelPoints(), fromProj, toProj));
//        }
//    }

    private PointShape projectPointShape(PointShape aPS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        PointShape newPS = (PointShape) aPS.clone();
        double[][] points = new double[1][];
        points[0] = new double[]{newPS.getPoint().X, newPS.getPoint().Y};
        double[] fromP = new double[]{newPS.getPoint().X, newPS.getPoint().Y};
        try {
            Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
            if (!Double.isNaN(points[0][0]) && !Double.isNaN(points[0][1])) {
                double[] toP = points[0];
                newPS.setPoint(new PointD(points[0][0], points[0][1]));
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

    private PolylineShape projectPolylineShape(PolylineShape aPLS, ProjectionInfo fromProj, ProjectionInfo toProj) {
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
                    //break;                    
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

    private CurveLineShape projectCurvelineShape(CurveLineShape aPLS, ProjectionInfo fromProj, ProjectionInfo toProj) {
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

    /**
     * Project polygon shape
     *
     * @param aPGS A polygon shape
     * @param fromProj From projection
     * @param toProj To porjection
     * @return Projected polygon shape
     */
    public PolygonShape projectPolygonShape(PolygonShape aPGS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < aPGS.getPolygons().size(); i++) {
            Polygon aPG = aPGS.getPolygons().get(i);
            Polygon bPG = null;
            for (int r = 0; r < aPG.getRingNumber(); r++) {
                List<PointD> pList = (List<PointD>)aPG.getRings().get(r);
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
                } else if (newPoints.size() > 2) {
                    if (bPG != null)
                        bPG.addHole(newPoints);
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

    private CurvePolygonShape projectCurvePolygonShape(CurvePolygonShape aPGS, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < aPGS.getPolygons().size(); i++) {
            Polygon aPG = aPGS.getPolygons().get(i);
            Polygon bPG = null;
            for (int r = 0; r < aPG.getRingNumber(); r++) {
                List<PointD> pList = (List<PointD>)aPG.getRings().get(r);
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
                } else if (newPoints.size() > 2) {
                    if (bPG != null)
                        bPG.addHole(newPoints);
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

    private CircleShape projectCircleShape(CircleShape aCS, ProjectionInfo fromProj, ProjectionInfo toProj) {
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

    private EllipseShape projectEllipseShape(EllipseShape aES, ProjectionInfo fromProj, ProjectionInfo toProj) {
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
    public double projectAngle(double oAngle, double[] fromP1, double[] toP1, ProjectionInfo fromProj, ProjectionInfo toProj) {
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

    private GraphicCollection projectGraphics(GraphicCollection aGCollection, ProjectionInfo fromProj, ProjectionInfo toProj) {
        GraphicCollection newGCollection = new GraphicCollection();
        for (Graphic aGraphic : aGCollection.getGraphics()) {
            aGraphic.setShape(projectShape(aGraphic.getShape(), fromProj, toProj));
            if (aGraphic.getShape() != null) {
                newGCollection.add(aGraphic);
            }
        }

        return newGCollection;
    }

    private List<Graphic> projectGraphics(List<Graphic> graphics, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<Graphic> newGraphics = new ArrayList<>();
        for (Graphic aGraphic : graphics) {
            Shape aShape = projectShape(aGraphic.getShape(), fromProj, toProj);
            if (aShape != null) {
                newGraphics.add(new Graphic(aShape, aGraphic.getLegend()));
            }
        }

        return newGraphics;
    }

    private Shape projectShape(Shape aShape, ProjectionInfo fromProj, ProjectionInfo toProj) {
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
    // </editor-fold>
}
