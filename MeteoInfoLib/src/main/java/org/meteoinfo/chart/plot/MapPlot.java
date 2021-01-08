/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.meteoinfo.chart.ChartNorthArrow;
import org.meteoinfo.chart.ChartPanel;
import org.meteoinfo.chart.ChartScaleBar;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.Location;
import org.meteoinfo.chart.axis.LonLatAxis;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.data.mapdata.webmap.IWebMapPanel;
import org.meteoinfo.data.mapdata.webmap.TileLoadListener;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.Direction;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.PointF;
import org.meteoinfo.layer.LayerCollection;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.legend.LabelBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.map.GridLabel;
import org.meteoinfo.map.MapView;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.ProjectionUtil;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.shape.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author wyq
 */
public class MapPlot extends AbstractPlot2D implements IWebMapPanel {

    // <editor-fold desc="Variables">
    private MapFrame mapFrame;
    private MapView mapView;
    private boolean antialias;
    private MapLayer selectedLayer;
    private final TileLoadListener tileLoadListener = new TileLoadListener(this);
    private ChartPanel parent;
    private float[] lonLim;
    private float[] latLim;
    private Graphic boundary;
    private ChartScaleBar scaleBar;
    private ChartNorthArrow northArrow;
    private boolean degreeSpace = false;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MapPlot() {
        super();
        this.antialias = false;
        this.setAutoAspect(false);
        try {
            this.setXAxis(new LonLatAxis("Longitude", true));
            this.setYAxis(new LonLatAxis("Latitude", false));
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(MapPlot.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.getAxis(Location.TOP).setDrawTickLabel(false);
        this.getAxis(Location.RIGHT).setDrawTickLabel(false);
        this.setDrawNeatLine(true);
        this.getGridLine().setTop(true);
    }

    /**
     * Constructor
     *
     * @param mapView MapView
     */
    public MapPlot(MapView mapView) {
        this();
        this.setMapView(mapView, true);
        this.mapFrame = new MapFrame();
        this.mapFrame.setMapView(mapView);
    }

    /**
     * Constructor
     *
     * @param mapFrame MapFrame
     */
    public MapPlot(MapFrame mapFrame) {
        this();
        this.mapFrame = mapFrame;
        this.setMapView(mapFrame.getMapView(), true);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * ChartPanel parent
     *
     * @param value ChartPanel
     */
    public void setParent(ChartPanel value) {
        this.parent = value;
    }

    @Override
    public Dataset getDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDataset(Dataset dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Get map view
     *
     * @return Map view
     */
    public MapView getMapView() {
        return this.mapView;
    }

    /**
     * Set map view
     *
     * @param value Map view
     * @param isGeoMap If is geo map
     */
    public void setMapView(MapView value, boolean isGeoMap) {
        this.mapView = value;
        this.mapView.setGeoMap(isGeoMap);
        this.mapView.setMultiGlobalDraw(isGeoMap);
        Extent extent = this.getAutoExtent();
        this.setDrawExtent(extent);
        PolygonShape bvs = this.mapView.getProjection().getProjInfo().getBoundary();
        if (bvs != null) {
            this.setBoundary(bvs);
        }
    }

    @Override
    public PlotType getPlotType() {
        return PlotType.XY2D;
    }

    /**
     * Get if is antialias
     *
     * @return Boolean
     */
    public boolean isAntialias() {
        return this.antialias;
    }

    /**
     * Set if is antialias
     *
     * @param value Boolean
     */
    public void setAntialias(boolean value) {
        this.antialias = value;
    }

    /**
     * Get background color
     *
     * @return Background color
     */
    @Override
    public Color getBackground() {
        return this.mapView.getBackground();
    }

    /**
     * Set background color
     *
     * @param value Background color
     */
    @Override
    public void setBackground(Color value) {
        this.mapView.setBackground(value);
    }

    /**
     * Get map frame
     *
     * @return Map frame
     */
    public MapFrame getMapFrame() {
        return this.mapFrame;
    }

    /**
     * Set map frame
     *
     * @param value Map frame
     */
    public void setMapFrame(MapFrame value) {
        this.mapFrame = value;
        this.setMapView(mapFrame.getMapView(), true);
    }

    /**
     * Get projection info
     *
     * @return Projection info
     */
    public ProjectionInfo getProjInfo() {
        return this.getMapView().getProjection().getProjInfo();
    }
    
    /**
     * Set projection info
     * @param proj Projection info
     */
    public void setProjInfo(ProjectionInfo proj) {
        this.getMapView().getProjection().setProjInfo(proj);
        if (proj.getBoundary() != null) {
            this.setBoundary(proj.getBoundary());
        }
    }

    /**
     * Is lon/lat map or not
     *
     * @return Boolean
     */
    public boolean isLonLatMap() {
        return this.getMapView().getProjection().isLonLatMap();
    }

    /**
     * Get selected layer
     *
     * @return Selected layer
     */
    public MapLayer getSelectedLayer() {
        if (this.selectedLayer != null) {
            return this.selectedLayer;
        } else if (this.mapView.getLastAddedLayer() != null) {
            return this.mapView.getLastAddedLayer();
        } else {
            return null;
        }
    }

    /**
     * Set selected layer
     *
     * @param value Selected layer
     */
    public void setSelectedLayer(MapLayer value) {
        this.selectedLayer = value;
    }

    /**
     * Get longitude limitations
     *
     * @return Longitude limitations
     */
    public float[] getLonLim() {
        return this.lonLim;
    }

    /**
     * Set longitude limitations
     *
     * @param value Longitude limitations
     */
    public void setLonLim(float[] value) {
        this.lonLim = value;
    }

    /**
     * Set longitude limitations
     *
     * @param lon1 Minimum longitude
     * @param lon2 Maximum longitude
     */
    public void setLonLim(float lon1, float lon2) {
        this.lonLim = new float[]{lon1, lon2};
    }

    /**
     * Get latitude limitations
     *
     * @return latitude limitations
     */
    public float[] getLatLim() {
        return this.latLim;
    }

    /**
     * Set latitude limitations
     *
     * @param value latitude limitations
     */
    public void setLatLim(float[] value) {
        this.latLim = value;
    }

    /**
     * Set latitude limitations
     *
     * @param lat1 Minimum latitude
     * @param lat2 Maximum latitude
     */
    public void setLatLim(float lat1, float lat2) {
        this.latLim = new float[]{lat1, lat2};
    }

    /**
     * Get map boundary
     *
     * @return Map boundary
     */
    public Graphic getBoundary() {
        return this.boundary;
    }

    /**
     * Set map boundary
     *
     * @param value Map boundary
     */
    public void setBoundary(Graphic value) {
        this.boundary = value;
    }

    /**
     * Set map boundary
     *
     * @param value Map boundary
     */
    public void setBoundary(PolygonShape value) {
        PolygonBreak pb = new PolygonBreak();
        pb.setOutlineSize(1.5f);
        pb.setDrawFill(false);
        this.boundary = new Graphic(value, pb);
    }
    
    /**
     * Set boundary property
     * @param pb Boundary property
     */
    public void setBoundaryProp(PolygonBreak pb) {
        if (this.boundary != null) {
            this.boundary = new Graphic(this.boundary.getShape(), pb);
        }
    }
    
    /**
     * Get scale bar
     * @return Scale bar
     */
    public ChartScaleBar getScaleBar() {
        return this.scaleBar;
    }
    
    /**
     * Set scale bar
     * @param value Scale bar
     */
    public void setScaleBar(ChartScaleBar value) {
        this.scaleBar = value;
    }
    
    /**
     * Get north arrow
     * @return North arrow
     */
    public ChartNorthArrow getNorthArrow() {
        return this.northArrow;
    }
    
    /**
     * Set north arrow
     * @param value North arrow
     */
    public void setNorthArrow(ChartNorthArrow value) {
        this.northArrow = value;
    }

    /**
     * Get if using space between degree and E/W/S/N
     * @return Boolean
     */
    public boolean isDegreeSpace() {
        return this.degreeSpace;
    }

    /**
     * Set if using space between degree and E/W/S/N
     * @param value Boolean
     */
    public void setDegreeSpace(boolean value) {
        this.degreeSpace = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Check if has web map layer
     *
     * @return Boolean
     */
    public boolean hasWebMapLayer() {
        return this.mapView.hasWebMapLayer();
    }

    /**
     * Get web map zoom
     *
     * @return Web map zoom
     */
    @Override
    public int getWebMapZoom() {
        return this.mapView.getWebMapZoom();
    }

    @Override
    public void reDraw() {
        if (this.parent != null) {
            this.parent.paintGraphics();
        }
    }
    
    /**
     * Draw plot
     *
     * @param g Graphics2D
     * @param area Drawing area
     */
    @Override
    public void draw(Graphics2D g, Rectangle2D area) {
        super.draw(g, area);
        if (this.scaleBar != null) {
            float x = (float) (area.getWidth() * this.scaleBar.getX());
            float y = (float) (area.getHeight() * (1 - this.scaleBar.getY()));
            this.scaleBar.draw(g, x, y);
        }
        
        if (this.northArrow != null) {
            float x = (float) (area.getWidth() * this.northArrow.getX());
            float y = (float) (area.getHeight() * (1 - this.northArrow.getY()));
            this.northArrow.draw(g, x, y);
        }
    }

    @Override
    void drawGraph(Graphics2D g, Rectangle2D area) {
        this.mapView.setAntiAlias(this.antialias);        
        this.mapView.setViewExtent((Extent) this.getDrawExtent().clone());
        if (this.boundary != null) {
            PolygonBreak pb = (PolygonBreak)this.boundary.getLegend().clone();
            if (pb.isDrawFill()) {
                pb.setDrawOutline(false);
                this.mapView.drawGraphic(g, new Graphic(this.boundary.getShape(), pb), area.getBounds());
                //pb.setDrawOutline(true);
            }
        }
        this.mapView.paintGraphics(g, area, this.tileLoadListener);
        if (this.boundary != null) {
            PolygonBreak pb = (PolygonBreak)this.boundary.getLegend().clone();
            pb.setDrawFill(false);
            this.mapView.drawGraphic(g, new Graphic(this.boundary.getShape(), pb), area.getBounds());
            //pb.setDrawFill(true);
        }
    }

    /**
     * Get auto extent
     *
     * @return Auto extent
     */
    @Override
    public Extent getAutoExtent() {
        return this.mapView.getLayersWholeExtent();
    }

    @Override
    public void setAutoExtent() {

    }

    @Override
    public void updateLegendScheme() {

    }

    /**
     * Add a graphic
     *
     * @param graphic The graphic
     */
    public void addGraphic(Graphic graphic) {
        this.getMapView().addGraphic(graphic);
    }

    /**
     * Add graphics
     * @param graphics The graphics
     */
    public void addGraphics(GraphicCollection graphics) {
        for (int i = 0; i < graphics.getNumGraphics(); i++) {
            this.getMapView().addGraphic(graphics.getGraphicN(i));
        }
    }

    /**
     * Add a graphic
     *
     * @param graphic The graphic
     * @param proj The graphic projection
     * @return Added graphic
     */
    public Graphic addGraphic(Graphic graphic, ProjectionInfo proj) {
        ProjectionInfo toProj = this.getMapView().getProjection().getProjInfo();
        if (proj.equals(toProj)) {
            this.getMapView().addGraphic(graphic);
            return graphic;
        } else {
            Graphic nGraphic = ProjectionUtil.projectGraphic(graphic, proj, toProj);
            this.getMapView().addGraphic(nGraphic);
            return nGraphic;
        }
    }

    /**
     * Add graphics
     *
     * @param graphics The graphics
     * @param proj The graphic projection
     * @return Added graphics
     */
    public GraphicCollection addGraphics(GraphicCollection graphics, ProjectionInfo proj) {

        ProjectionInfo toProj = this.getMapView().getProjection().getProjInfo();
        if (proj.equals(toProj)) {
            for (int i = 0; i < graphics.getNumGraphics(); i++)
                this.getMapView().addGraphic(graphics.getGraphicN(i));
            return graphics;
        } else {
            GraphicCollection nGraphics = new GraphicCollection();
            for (int i = 0; i < graphics.getNumGraphics(); i++) {
                Graphic nGraphic = ProjectionUtil.projectGraphic(graphics.getGraphicN(i), proj, toProj);
                nGraphics.add(nGraphic);
                this.getMapView().addGraphic(nGraphic);
            }
            return nGraphics;
        }
    }

    /**
     * Add a layer
     *
     * @param layer The layer
     */
    public void addLayer(MapLayer layer) {
        this.mapView.addLayer(layer);
        this.setDrawExtent(layer.getExtent());
    }

    /**
     * Add a layer
     *
     * @param idx Index
     * @param layer Layer
     */
    public void addLayer(int idx, MapLayer layer) {
        this.mapView.addLayer(idx, layer);
        this.setDrawExtent(layer.getExtent());
    }

    /**
     * Remove last added layer
     */
    public void removeLastLayer() {
        this.mapView.removeLayer(this.mapView.getLastAddedLayer());
    }

    /**
     * Set all axis visible or not
     *
     * @param value Boolean
     */
    @Override
    public void setAxisOn(boolean value) {
        super.setAxisOn(value);
        this.mapFrame.setDrawGridTickLine(value);
        this.mapFrame.setDrawGridLabel(value);
    }

    /**
     * Get full extent
     *
     * @return Full extent
     */
    public Extent getFullExtent() {
        Extent ext = this.mapView.getExtent();
        if (this.boundary != null) {
            ext = ext.union(this.boundary.getExtent().extend(0.01));
        }

        return ext;
    }

    /**
     * Set longitude/latitude extent
     *
     * @param extent Extent
     */
    public void setLonLatExtent(Extent extent) {
        if (this.getMapView().getProjection().isLonLatMap()) {
            super.setDrawExtent(extent);
        } else {
            this.getMapView().zoomToExtentLonLatEx(extent);
            super.setDrawExtent1(this.getMapView().getViewExtent());
            this.setAxisExtent(extent);
        }
    }

    /**
     * Set axis extent
     *
     * @param extent Extent
     */
    public void setAxisExtent(Extent extent) {
        this.getAxis(Location.BOTTOM).setMinMaxValue(extent.minX, extent.maxX);
        this.getAxis(Location.TOP).setMinMaxValue(extent.minX, extent.maxX);
        this.getAxis(Location.LEFT).setMinMaxValue(extent.minY, extent.maxY);
        this.getAxis(Location.RIGHT).setMinMaxValue(extent.minY, extent.maxY);
    }

    @Override
    public void addText(ChartText text) {
        addText(text, true);
    }

    public void addText(ChartText text, boolean isLonLat) {
        if (isLonLat) {
            if (this.getMapView().getProjection().isLonLatMap()) {
                super.addText(text);
            } else {
                PointShape ps = new PointShape();
                PointD lonlatp = new PointD(text.getX(), text.getY());
                PointD xyp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                        this.getMapView().getProjection().getProjInfo());
                ps.setPoint(xyp);
                LabelBreak lb = new LabelBreak();
                lb.setText(text.getText());
                lb.setFont(text.getFont());
                lb.setColor(text.getColor());
                Graphic aGraphic = new Graphic(ps, lb);
                this.getMapView().addGraphic(aGraphic);
            }
        } else {
            super.addText(text);
        }
    }

    /**
     * Add point graphic
     *
     * @param lat Latitude
     * @param lon Lontitude
     * @param pb Point break
     */
    public void addPoint(double lat, double lon, PointBreak pb) {
        PointShape ps = new PointShape();
        PointD lonlatp = new PointD(lon, lat);
        if (this.getMapView().getProjection().isLonLatMap()) {
            ps.setPoint(lonlatp);
        } else {
            PointD xyp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                    this.getMapView().getProjection().getProjInfo());
            ps.setPoint(xyp);
        }
        Graphic aGraphic = new Graphic(ps, pb);
        this.getMapView().addGraphic(aGraphic);
    }

    /**
     * Add point graphic
     *
     * @param lat Latitude
     * @param lon Lontitude
     * @param pb Point break
     * @return Graphic
     */
    public Graphic addPoint(List<Number> lat, List<Number> lon, PointBreak pb) {
        double x, y;
        PointShape ps;
        PointD lonlatp, xyp;
        for (int i = 0; i < lat.size(); i++) {
            ps = new PointShape();
            x = lon.get(i).doubleValue();
            y = lat.get(i).doubleValue();
            lonlatp = new PointD(x, y);
            if (this.getMapView().getProjection().isLonLatMap()) {
                ps.setPoint(lonlatp);
            } else {
                xyp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                        this.getMapView().getProjection().getProjInfo());
                ps.setPoint(xyp);
            }
            Graphic aGraphic = new Graphic(ps, pb);
            this.getMapView().addGraphic(aGraphic);
            return aGraphic;
        }
        return null;
    }

    /**
     * Add polyline
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param plb PolylineBreak
     * @return Graphic
     */
    public Graphic addPolyline(List<Number> lat, List<Number> lon, PolylineBreak plb) {
        double x, y;
        PolylineShape pls;
        PointD lonlatp;
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < lat.size(); i++) {
            x = lon.get(i).doubleValue();
            y = lat.get(i).doubleValue();
            if (Double.isNaN(x)) {
                if (points.size() >= 2) {
                    pls = new PolylineShape();
                    pls.setPoints(points);
                    Graphic aGraphic = new Graphic(pls, plb);
                    this.getMapView().addGraphic(aGraphic);
                }
                points = new ArrayList<>();
            } else {
                lonlatp = new PointD(x, y);
                if (!this.getMapView().getProjection().isLonLatMap()) {
                    lonlatp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                            this.getMapView().getProjection().getProjInfo());
                }
                points.add(lonlatp);
            }
        }
        if (points.size() >= 2) {
            pls = new PolylineShape();
            pls.setPoints(points);
            Graphic aGraphic = new Graphic(pls, plb);
            this.getMapView().addGraphic(aGraphic);
            return aGraphic;
        }
        return null;
    }

    /**
     * Add polyline
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param plb PolylineBreak
     * @param iscurve Is curve line or not
     * @return Graphic
     */
    public Graphic addPolyline(List<Number> lat, List<Number> lon, PolylineBreak plb, boolean iscurve) {
        double x, y;
        PolylineShape pls;
        PointD lonlatp;
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < lat.size(); i++) {
            x = lon.get(i).doubleValue();
            y = lat.get(i).doubleValue();
            if (Double.isNaN(x)) {
                if (points.size() >= 2) {
                    if (iscurve) {
                        pls = new CurveLineShape();
                    } else {
                        pls = new PolylineShape();
                    }
                    pls.setPoints(points);
                    Graphic aGraphic = new Graphic(pls, plb);
                    this.getMapView().addGraphic(aGraphic);
                }
                points = new ArrayList<>();
            } else {
                lonlatp = new PointD(x, y);
                if (!this.getMapView().getProjection().isLonLatMap()) {
                    lonlatp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                            this.getMapView().getProjection().getProjInfo());
                }
                points.add(lonlatp);
            }
        }
        if (points.size() >= 2) {
            if (iscurve) {
                pls = new CurveLineShape();
            } else {
                pls = new PolylineShape();
            }
            pls.setPoints(points);
            Graphic aGraphic = new Graphic(pls, plb);
            this.getMapView().addGraphic(aGraphic);
            return aGraphic;
        }
        return null;
    }

    /**
     * Add polygon
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param pgb PolygonBreak
     * @return Graphic
     */
    public Graphic addPolygon(List<Number> lat, List<Number> lon, PolygonBreak pgb) {
        double x, y;
        PolygonShape pgs;
        PointD lonlatp;
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < lat.size(); i++) {
            x = lon.get(i).doubleValue();
            y = lat.get(i).doubleValue();
            if (Double.isNaN(x)) {
                if (points.size() > 2) {
                    pgs = new PolygonShape();
                    pgs.setPoints(points);
                    Graphic aGraphic = new Graphic(pgs, pgb);
                    this.getMapView().addGraphic(aGraphic);
                }
                points = new ArrayList<>();
            } else {
                lonlatp = new PointD(x, y);
                if (!this.getMapView().getProjection().isLonLatMap()) {
                    lonlatp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                            this.getMapView().getProjection().getProjInfo());
                }
                points.add(lonlatp);
            }
        }
        if (points.size() > 2) {
            pgs = new PolygonShape();
            pgs.setPoints(points);
            Graphic aGraphic = new Graphic(pgs, pgb);
            this.getMapView().addGraphic(aGraphic);
            return aGraphic;
        }
        return null;
    }

    /**
     * Add a circle
     *
     * @param x Center x
     * @param y Center y
     * @param radius
     * @param pgb PolygonBreak
     * @return Graphic
     */
    public Graphic addCircle(float x, float y, float radius, PolygonBreak pgb) {
        CircleShape aPGS = ShapeUtil.createCircleShape(x, y, radius);
        Graphic graphic = new Graphic(aPGS, pgb);
        this.mapView.addGraphic(graphic);

        return graphic;
    }

//    /**
//     * Add a layer
//     * @param idx Index
//     * @param layer Layer
//     */
//    public void addLayer(int idx, MapLayer layer){
//        this.mapFrame.addLayer(idx, layer);
//    }
//    
//    /**
//     * Add a layer
//     * @param layer Layer 
//     */
//    public void addLayer(MapLayer layer){
//        this.mapFrame.addLayer(layer);
//    }
//    
//    /**
//     * Set extent
//     *
//     * @param extent Extent
//     */
//    public void setExtent(Extent extent) {
//        this.mapFrame.getMapView().setViewExtent(extent);
//    }
    /**
     * Get position area
     *
     * @param area Whole area
     * @return Graphic area
     */
    @Override
    public Rectangle2D getPositionArea(Rectangle2D area) {
        Rectangle2D plotArea = super.getPositionArea(area);
        if (!this.isAutoAspect()) {
            MapView mv = this.mapFrame.getMapView();
            mv.setViewExtent((Extent) this.getDrawExtent().clone());
            Extent extent = mv.getViewExtent();
            double width = extent.getWidth();
            double height = extent.getHeight();
            double scaleFactor = mv.getXYScaleFactor();
            if (width / height / scaleFactor > plotArea.getWidth() / plotArea.getHeight()) {
                double h = plotArea.getWidth() * height * scaleFactor / width;
                double delta = plotArea.getHeight() - h;
                plotArea.setRect(plotArea.getX(), plotArea.getY() + delta / 2, plotArea.getWidth(), h);
            } else {
                double w = width * plotArea.getHeight() / height / scaleFactor;
                double delta = plotArea.getWidth() - w;
                plotArea.setRect(plotArea.getX() + delta / 2, plotArea.getY(), w, plotArea.getHeight());
            }
        }
        return plotArea;
    }

//    @Override
//    public void drawGraph(Graphics2D g, Rectangle2D area) {
//        MapView mapView = this.mapFrame.getMapView();
//        mapView.setViewExtent(this.getDrawExtent());
//        Extent extent = mapView.getViewExtent();
//        double width = extent.getWidth();
//        double height = extent.getHeight();
//        double scaleFactor = mapView.getXYScaleFactor();
//        if (width / height / scaleFactor > area.getWidth() / area.getHeight()){
//            double h = area.getWidth() * height * scaleFactor / width;
//            double delta = area.getHeight() - h;
//            area.setRect(area.getX(), area.getY() + delta / 2, area.getWidth(), h);
//        } else {
//            double w = width * area.getHeight() / height / scaleFactor;
//            double delta = area.getWidth() - w;
//            area.setRect(area.getX() + delta / 2, area.getY(), w, area.getHeight());
//        }
//        mapView.paintGraphics(g, area);
//    }
//    /**
//     * Set draw extent
//     *
//     * @param extent Extent
//     */
//    @Override
//    public void setDrawExtent(Extent extent) {
//        if (this.isLonLatMap()){
//            super.updateDrawExtent();
//        } else {            
//            ((ProjLonLatAxis)this.getAxis(Location.BOTTOM)).setX_Y(extent.minY);
//            ((ProjLonLatAxis)this.getAxis(Location.TOP)).setX_Y(extent.maxY);
//            ((ProjLonLatAxis)this.getAxis(Location.LEFT)).setX_Y(extent.minX);
//            ((ProjLonLatAxis)this.getAxis(Location.RIGHT)).setX_Y(extent.maxX);
//            super.setDrawExtent(extent);
//        }
//    }
//    
//    /**
//     * Update draw extent
//     */
//    @Override
//    public void updateDrawExtent(){
//        if (this.isLonLatMap()){
//            super.updateDrawExtent();
//        } else {
//            Extent extent = this.getDrawExtent();
//            ((ProjLonLatAxis)this.getAxis(Location.BOTTOM)).setX_Y(extent.minY);
//            ((ProjLonLatAxis)this.getAxis(Location.TOP)).setX_Y(extent.maxY);
//            ((ProjLonLatAxis)this.getAxis(Location.LEFT)).setX_Y(extent.minX);
//            ((ProjLonLatAxis)this.getAxis(Location.RIGHT)).setX_Y(extent.maxX);
//            super.updateDrawExtent();
//        }
//    }
    @Override
    void drawAxis(Graphics2D g, Rectangle2D area) {
        if (this.mapFrame.getMapView().getProjection().isLonLatMap()) {
            super.drawAxis(g, area);
            return;
        }

        //Draw lon/lat grid labels
        if (this.mapFrame.isDrawGridLabel()) {
            final float shift = 5.0F;
            List<Extent> extentList = new ArrayList<>();
            Extent maxExtent = new Extent();
            Extent aExtent;
            Dimension aSF;
            g.setColor(this.mapFrame.getGridLineColor());
            g.setStroke(new BasicStroke(this.mapFrame.getGridLineSize()));
            String drawStr;
            PointF sP = new PointF(0, 0);
            PointF eP = new PointF(0, 0);
            Font font = this.getXAxis().getTickLabelFont();
            //Font font = new Font(this.mapFrame.getGridFont().getFontName(), this.mapFrame.getGridFont().getStyle(), (int) (this.mapFrame.getGridFont().getSize()));
            g.setFont(font);
            float labX, labY;
            int len = mapFrame.getTickLineLength();
            int space = len + this.mapFrame.getGridLabelShift();
            if (mapFrame.isInsideTickLine()) {
                space = mapFrame.getGridLabelShift();
            }

            Object[] objs;
            float xShift, yShift;
            XAlign xAlign;
            YAlign yAlign;
            for (int i = 0; i < mapFrame.getMapView().getGridLabels().size(); i++) {
                GridLabel aGL = mapFrame.getMapView().getGridLabels().get(i);
                switch (mapFrame.getGridLabelPosition()) {
                    case LEFT_BOTTOM:
                        switch (aGL.getLabDirection()) {
                            case East:
                            case North:
                                continue;
                        }
                        break;
                    case LEFT_UP:
                        switch (aGL.getLabDirection()) {
                            case East:
                            case South:
                                continue;
                        }
                        break;
                    case RIGHT_BOTTOM:
                        switch (aGL.getLabDirection()) {
                            case Weast:
                            case North:
                                continue;
                        }
                        break;
                    case RIGHT_UP:
                        switch (aGL.getLabDirection()) {
                            case Weast:
                            case South:
                                continue;
                        }
                        break;
                }

                labX = (float) aGL.getLabPoint().X;
                labY = (float) aGL.getLabPoint().Y;
                labX = labX + (float) area.getX();
                labY = labY + (float) area.getY();
                sP.X = labX;
                sP.Y = labY;

                drawStr = aGL.getLabString();
                //if (this.drawDegreeSymbol) {
                if (drawStr.endsWith("E") || drawStr.endsWith("W") || drawStr.endsWith("N") || drawStr.endsWith("S")) {
                    if (this.degreeSpace) {
                        drawStr = drawStr.substring(0, drawStr.length() - 1) + String.valueOf((char) 186) +
                                " " + drawStr.substring(drawStr.length() - 1);
                    } else {
                        drawStr = drawStr.substring(0, drawStr.length() - 1) + String.valueOf((char) 186) +
                                drawStr.substring(drawStr.length() - 1);
                    }
                } else {
                    drawStr = drawStr + String.valueOf((char) 186);
                }
                //}
                aSF = Draw.getStringDimension(drawStr, g);
                boolean ifDraw = true;
                aExtent = new Extent();
                aExtent.minX = labX;
                aExtent.maxX = labX + aSF.width;
                aExtent.minY = labY - aSF.height;
                aExtent.maxY = labY;

                //Judge extent                                        
                if (extentList.isEmpty()) {
                    maxExtent = (Extent) aExtent.clone();
                    extentList.add((Extent) aExtent.clone());
                } else if (!MIMath.isExtentCross(aExtent, maxExtent)) {
                    extentList.add((Extent) aExtent.clone());
                    maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                } else {
                    for (int j = 0; j < extentList.size(); j++) {
                        if (MIMath.isExtentCross(aExtent, extentList.get(j))) {
                            ifDraw = false;
                            break;
                        }
                    }
                    if (ifDraw) {
                        extentList.add(aExtent);
                        maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                    }
                }
                if (ifDraw) {
                    if (aGL.isBorder()) {
                        switch (aGL.getLabDirection()) {
                            case South:
                                labX = labX - aSF.width / 2;
                                labY = labY + aSF.height * 3 / 4 + space;
                                eP.X = sP.X;
                                if (mapFrame.isInsideTickLine()) {
                                    eP.Y = sP.Y - len;
                                } else {
                                    eP.Y = sP.Y + len;
                                }
                                break;
                            case Weast:
                                labX = labX - aSF.width - space;
                                labY = labY + aSF.height / 3;
                                eP.Y = sP.Y;
                                if (mapFrame.isInsideTickLine()) {
                                    eP.X = sP.X + len;
                                } else {
                                    eP.X = sP.X - len;
                                }
                                break;
                            case North:
                                labX = labX - aSF.width / 2;
                                //labY = labY - aSF.height / 3 - space;
                                labY = labY - space;
                                eP.X = sP.X;
                                if (mapFrame.isInsideTickLine()) {
                                    eP.Y = sP.Y + len;
                                } else {
                                    eP.Y = sP.Y - len;
                                }
                                break;
                            case East:
                                labX = labX + space;
                                labY = labY + aSF.height / 3;
                                eP.Y = sP.Y;
                                if (mapFrame.isInsideTickLine()) {
                                    eP.X = sP.X - len;
                                } else {
                                    eP.X = sP.X + len;
                                }
                                break;
                        }
                        g.setColor(mapFrame.getGridLineColor());
                        g.draw(new Line2D.Float(sP.X, sP.Y, eP.X, eP.Y));
                        g.setColor(this.getXAxis().getTickLabelColor());
                        g.drawString(drawStr, labX, labY);
                    } else {
                        g.setColor(this.getXAxis().getTickLabelColor());
                        objs = this.getProjInfo().checkGridLabel(aGL, shift);
                        xShift = (float)objs[0];
                        yShift = (float)objs[1];
                        xAlign = (XAlign)objs[2];
                        yAlign = (YAlign)objs[3];
                        Draw.drawString(g, labX+xShift, labY+yShift, drawStr, xAlign, yAlign, false);
                    }
                }
            }
        }
    }

    @Override
    int getXAxisHeight(Graphics2D g) {
        if (this.isLonLatMap()) {
            return super.getXAxisHeight(g);
        }

        int space = 4;
        if (this.mapFrame.isDrawGridLabel()) {
            int height = space;
            height += mapFrame.getTickLineLength() + mapFrame.getGridLabelShift();
            FontMetrics m = g.getFontMetrics(mapFrame.getGridFont());
            height += m.getHeight();
            return height;
        }

        return 0;
    }

    @Override
    int getYAxisWidth(Graphics2D g) {
        if (this.isLonLatMap()) {
            return super.getYAxisWidth(g);
        }

        int space = 4;
        if (this.mapFrame.isDrawGridLabel()) {
            int width = space;
            width += mapFrame.getTickLineLength() + mapFrame.getGridLabelShift();
            FontMetrics m = g.getFontMetrics(mapFrame.getGridFont());
            List<GridLabel> labels = mapFrame.getMapView().getGridLabels();
            int labWidth = 0, w;
            for (int i = 0; i < labels.size(); i++) {
                w = m.stringWidth(labels.get(i).getLabString());
                if (w > labWidth) {
                    labWidth = w;
                }
            }
            width += labWidth;
            return width;
        }

        return 0;
    }

    /**
     * Get layer number
     *
     * @return Layer number
     */
    public int getLayerNum() {
        return this.mapView.getLayerNum();
    }

    /**
     * Get layers
     *
     * @return Layers
     */
    public LayerCollection getLayers() {
        return this.mapView.getLayers();
    }

    /**
     * Get layer by index
     *
     * @param i The layer index
     * @return The layer
     */
    public MapLayer getLayer(int i) {
        return this.mapView.getLayers().get(i);
    }

    /**
     * Get layer by name
     *
     * @param name The layer name
     * @return The layer
     */
    public MapLayer getLayer(String name) {
        return this.mapView.getLayer(name);
    }

    /**
     * Get legend scheme
     *
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        MapLayer layer = this.mapView.getLastAddedLayer();
        return layer == null ? null : layer.getLegendScheme();
    }

    /**
     * Load MeteoInfo project file
     *
     * @param fn MeteoInfo project file name
     * @param mfidx Map frame index
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public void loadMIProjectFile(String fn, int mfidx) throws SAXException, IOException, ParserConfigurationException {
        File file = new File(fn);
        String userDir = System.getProperty("user.dir");
        System.setProperty("user.dir", file.getParent());
        String pPath = file.getParent();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(fn));

        Element root = doc.getDocumentElement();

        Element mapFrames = (Element) root.getElementsByTagName("MapFrames").item(0);
        if (mapFrames == null) {
            this.mapFrame.importProjectXML(pPath, root);
        } else {
            NodeList mfNodes = mapFrames.getElementsByTagName("MapFrame");
            Node mfNode = mfNodes.item(mfidx);
            this.mapFrame.importProjectXML(pPath, (Element) mfNode);
        }
        this.setDrawExtent(this.mapView.getViewExtent());
        this.setExtent(this.mapView.getViewExtent());
        System.setProperty("user.dir", userDir);
    }

    /**
     * Load MeteoInfo project file
     *
     * @param fn MeteoInfo project file name
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public void loadMIProjectFile(String fn) throws SAXException, IOException, ParserConfigurationException {
        this.loadMIProjectFile(fn, 0);
    }
    // </editor-fold>
}
