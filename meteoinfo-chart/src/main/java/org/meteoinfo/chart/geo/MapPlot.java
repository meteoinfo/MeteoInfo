/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.geo;

import org.meteoinfo.chart.*;
import org.meteoinfo.chart.axis.Axis;
import org.meteoinfo.chart.axis.LonLatAxis;
import org.meteoinfo.chart.axis.ProjLonLatAxis;
import org.meteoinfo.chart.geo.MapGridLine;
import org.meteoinfo.chart.graphic.WebMapImage;
import org.meteoinfo.chart.plot.Plot2D;
import org.meteoinfo.chart.plot.PlotType;
import org.meteoinfo.common.*;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.data.mapdata.webmap.GeoPosition;
import org.meteoinfo.data.mapdata.webmap.GeoUtil;
import org.meteoinfo.data.mapdata.webmap.IWebMapPanel;
import org.meteoinfo.data.mapdata.webmap.TileLoadListener;
import org.meteoinfo.geo.drawing.Draw;
import org.meteoinfo.geo.graphic.GeoGraphicCollection;
import org.meteoinfo.geo.layer.WebMapLayer;
import org.meteoinfo.geo.util.GeoProjectionUtil;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.*;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionUtil;
import org.meteoinfo.projection.Reproject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wyq
 */
public class MapPlot extends Plot2D implements IWebMapPanel {

    // <editor-fold desc="Variables">
    private ProjectionInfo projInfo;
    private boolean antialias;
    protected TileLoadListener tileLoadListener = new TileLoadListener(this);
    private IChartPanel parent;
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
        this(KnownCoordinateSystems.geographic.world.WGS1984);
    }

    /**
     * Constructor
     * @param projInfo The projection info
     */
    public MapPlot(ProjectionInfo projInfo) {
        super();

        this.projInfo = projInfo;
        this.antialias = false;
        this.aspectType = AspectType.EQUAL;
        this.gridLine = new MapGridLine(projInfo);
        this.gridLine.setTop(true);
        if (this.projInfo.isLonLat()) {
            this.setXAxis(new LonLatAxis("Longitude", true));
            this.setYAxis(new LonLatAxis("Latitude", false));
        } else {
            this.setXAxis(new ProjLonLatAxis("Longitude", true, (MapGridLine) this.gridLine));
            this.setYAxis(new ProjLonLatAxis("Latitude", false, (MapGridLine) this.gridLine));
        }
        this.getAxis(Location.TOP).setVisible(false);
        this.getAxis(Location.RIGHT).setVisible(false);
        this.setDrawNeatLine(true);

        Extent extent = this.getAutoExtent();
        this.setDrawExtent(extent);
        PolygonShape bvs = this.projInfo.getBoundary();
        if (bvs != null) {
            this.setBoundary(bvs);
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get tile load listener
     *
     * @return Tile load listener
     */
    public TileLoadListener getTileLoadListener() {
        return this.tileLoadListener;
    }

    /**
     * ChartPanel parent
     *
     * @param value ChartPanel
     */
    public void setParent(IChartPanel value) {
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
     * Get projection info
     *
     * @return Projection info
     */
    public ProjectionInfo getProjInfo() {
        return this.projInfo;
    }
    
    /**
     * Set projection info
     * @param proj Projection info
     */
    public void setProjInfo(ProjectionInfo proj) {
        this.projInfo = proj;
        ((MapGridLine) this.gridLine).setProjInfo(proj);
        if (proj.isLonLat()) {
            if (this.getXAxis() instanceof ProjLonLatAxis) {
                for (Location loc : this.axis.keySet()) {
                    Axis axis = this.axis.get(loc);
                    axis = new LonLatAxis(axis);
                    this.axis.put(loc, axis);
                }
            }
        } else {
            if (!(this.getXAxis() instanceof ProjLonLatAxis)) {
                for (Location loc : this.axis.keySet()) {
                    Axis axis = this.axis.get(loc);
                    axis = new ProjLonLatAxis((LonLatAxis)axis, (MapGridLine) this.gridLine);
                    this.axis.put(loc, axis);
                }
            }
        }

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
        return this.projInfo.isLonLat();
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
     * Check if the plot has web map layer
     *
     * @return Boolean
     */
    public boolean hasWebMapLayer() {
        for (Graphic g : this.graphics.getGraphics()) {
            if (g instanceof WebMapImage) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get web map image
     * @return Web map image
     */
    public WebMapImage getWebMapImage() {
        for (Graphic g : this.graphics.getGraphics()) {
            if (g instanceof WebMapImage) {
                return (WebMapImage) g;
            }
        }
        return null;
    }

    /**
     * Get web map zoom
     *
     * @return Web map zoom
     */
    @Override
    public int getWebMapZoom() {
        WebMapImage webMapImage = getWebMapImage();

        return webMapImage == null ? 0 : webMapImage.getZoom();
    }

    /**
     * Get geographic center with longitude/latitude
     *
     * @return Geographic center
     */
    public PointD getGeoCenter() {
        PointD viewCenter = this.getViewCenter();
        return Reproject.reprojectPoint(viewCenter, this.projInfo,
                KnownCoordinateSystems.geographic.world.WGS1984);
    }

    /**
     * Get view center point
     *
     * @return The view center point
     */
    public PointD getViewCenter() {
        return this.drawExtent.getCenterPoint();
    }

    /**
     * Set view center point
     *
     * @param center The view center point
     */
    public void setViewCenter(PointD center) {
        PointD oldCenter = this.getViewCenter();
        double dx = center.X - oldCenter.X;
        double dy = center.Y - oldCenter.Y;
        Extent extent = this.drawExtent.shift(dx, dy);
        this.drawExtent = extent;
    }

    @Override
    public void reDraw() {
        if (this.parent != null) {
            this.parent.paintGraphics();
        }
    }

    /**
     * Re draw function
     * @param graphics2D Graphics2D object
     * @param width Width
     * @param height Height
     */
    @Override
    public void reDraw(Graphics2D graphics2D, int width, int height) {
        if (this.parent != null) {
            this.parent.paintGraphics(graphics2D, width, height);
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
    protected void drawGraph(Graphics2D g, Rectangle2D area) {
        //fill boundary polygon
        java.awt.Shape oldRegion = g.getClip();
        if (this.clip) {
            g.setClip(area);
        }
        AffineTransform oldMatrix = g.getTransform();
        g.translate(area.getX(), area.getY());

        if (this.boundary != null) {
            PolygonBreak pb = (PolygonBreak)this.boundary.getLegend().clone();
            if (pb.isDrawFill()) {
                pb.setDrawOutline(false);
                this.drawGraphic(g, this.boundary, pb, area);
            }
        }

        g.setTransform(oldMatrix);

        //Plot graphics
        g.translate(area.getX(), area.getY());

        plotGraphics(g, area);

        //Draw boundary line
        if (this.boundary != null) {
            PolygonBreak pb = (PolygonBreak)this.boundary.getLegend().clone();
            pb.setDrawFill(false);
            this.drawGraphic(g, this.boundary, pb, area);
        }

        g.setTransform(oldMatrix);
        if (this.clip) {
            g.setClip(oldRegion);
        }
    }

    @Override
    protected void plotGraphics(Graphics2D g, Rectangle2D area) {
        int barIdx = 0;
        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            if (graphic instanceof WebMapImage) {
                this.drawWebMapImage(g, (WebMapImage) graphic, area);
                continue;
            }

            ColorBreak cb = graphic.getLegend();
            ShapeTypes shapeType = graphic.getGraphicN(0).getShape().getShapeType();
            switch(shapeType){
                case BAR:
                    this.drawBars(g, (GraphicCollection) graphic, barIdx, area);
                    barIdx += 1;
                    continue;
                case STATION_MODEL:
                    this.drawStationModel(g, (GraphicCollection) graphic, area);
                    continue;
            }

            if (graphic.getExtent().intersects(this.drawExtent)) {
                drawGraphics(g, graphic, area);
            }

            if (this.isLonLatMap() && graphic instanceof GeoGraphicCollection) {
                if (this.drawExtent.maxX > 180) {
                    drawGraphics(g, ((GeoGraphicCollection) graphic).xShiftCopy(360), area);
                }
            }
        }
    }

    void drawStationModel(Graphics2D g, GraphicCollection graphics, Rectangle2D area) {
        PointF pointF = new PointF();
        LegendScheme ls = graphics.getLegendScheme();
        List<Extent> extentList = new ArrayList<>();
        Extent maxExtent = new Extent();
        Extent aExtent;
        PointBreak pointBreak = (PointBreak) ls.getLegendBreak(0);
        for (Graphic graphic : graphics.getGraphics()) {
            StationModelShape shape = (StationModelShape) graphic.getShape();
            PointD p = shape.getPoint();
            if (p.X < drawExtent.minX || p.X > drawExtent.maxX
                    || p.Y < drawExtent.minY || p.Y > drawExtent.maxY) {
                continue;
            }

            if (pointBreak.isDrawShape()) {
                double[] screenXY;
                screenXY = projToScreen(p.X, p.Y, area);
                pointF.X = (float) screenXY[0];
                pointF.Y = (float) screenXY[1];
                boolean isDraw = true;
                if (graphics.isAvoidCollision()) {
                    float aSize = pointBreak.getSize();
                    aExtent = new Extent();
                    aExtent.minX = pointF.X - aSize;
                    aExtent.maxX = pointF.X + aSize;
                    aExtent.minY = pointF.Y - aSize;
                    aExtent.maxY = pointF.Y + aSize;
                    if (extentList.isEmpty()) {
                        maxExtent = (Extent) aExtent.clone();
                        extentList.add(aExtent);
                    } else if (!MIMath.isExtentCross(aExtent, maxExtent)) {
                        extentList.add(aExtent);
                        maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                    } else {
                        for (Extent extent : extentList) {
                            if (MIMath.isExtentCross(aExtent, extent)) {
                                isDraw = false;
                                break;
                            }
                        }
                        if (isDraw) {
                            extentList.add(aExtent);
                            maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                        }
                    }
                }

                if (isDraw) {
                    Draw.drawStationModel(pointBreak.getColor(), pointBreak.getOutlineColor(), pointF, shape,
                            g, pointBreak.getSize(), pointBreak.getSize() / 8 * 3);
                }
            }
        }
    }

    private double getWebMapScale(WebMapImage graphic, int zoom, double width, double height) {
        Point2D center = graphic.getCenter();
        double minx = center.getX() - width / 2.;
        double miny = center.getY() - height / 2.;
        double maxx = center.getX() + width / 2.;
        double maxy = center.getY() + height / 2.;
        GeoPosition pos1 = GeoUtil.getPosition(new Point2D.Double(minx, miny), zoom, graphic.getTileFactory().getInfo());
        GeoPosition pos2 = GeoUtil.getPosition(new Point2D.Double(maxx, maxy), zoom, graphic.getTileFactory().getInfo());
        PointD p1 = Reproject.reprojectPoint(new PointD(pos1.getLongitude(), pos1.getLatitude()),
                KnownCoordinateSystems.geographic.world.WGS1984, this.projInfo);
        PointD p2 = Reproject.reprojectPoint(new PointD(pos2.getLongitude(), pos2.getLatitude()),
                KnownCoordinateSystems.geographic.world.WGS1984, this.projInfo);
        if (pos2.getLongitude() - pos1.getLongitude() < 360.0 && pos2.getLongitude() <= 180) {
            double xlen = Math.abs(p2.X - p1.X);
            return (double) width / xlen;
        } else {
            double ylen = Math.abs(p2.Y - p1.Y);
            return (double) height / ylen;
        }
    }

    private void setScale(double scale, double width, double height) {
        this.xScale = scale;
        this.yScale = scale;
        PointD center = (PointD)this.drawExtent.getCenterPoint().clone();
        double xlen = width / scale * 0.5;
        double ylen = height / scale * 0.5;
        this.drawExtent.minX = center.X - xlen;
        this.drawExtent.maxX = center.X + xlen;
        this.drawExtent.minY = center.Y - ylen;
        this.drawExtent.maxY = center.Y + ylen;
    }

    void drawWebMapImage(Graphics2D g, WebMapImage graphic, Rectangle2D area) {
        PointD geoCenter = this.getGeoCenter();
        graphic.setAddressLocation(new GeoPosition(geoCenter.Y, geoCenter.X));
        double webMapScale = graphic.getWebMapScale();
        if (!MIMath.doubleEquals(this.xScale, webMapScale)) {
            int minZoom = graphic.getTileFactory().getInfo().getMinimumZoomLevel();
            int maxZoom = graphic.getTileFactory().getInfo().getMaximumZoomLevel();
            int newZoom = minZoom;
            double scale = webMapScale;
            double width = area.getWidth();
            double height = area.getHeight();
            for (int i = maxZoom; i >= minZoom; i--) {
                graphic.setZoom(i);
                scale = getWebMapScale(graphic, i, width, height);
                if (xScale < scale) {
                    newZoom = i;
                    if (xScale < webMapScale) {
                        if (i < maxZoom) {
                            newZoom = i + 1;
                            scale = getWebMapScale(graphic, newZoom, width, height);
                        }
                    }
                    break;
                }
            }
            this.setScale(scale, width, height);
            graphic.setWebMapScale(scale);
            graphic.setZoom(newZoom);
        }

        graphic.draw(g, area, this.tileLoadListener);
    }

    /**
     * Add a graphic
     *
     * @param graphic The graphic
     * @param proj The graphic projection
     * @return Added graphic
     */
    public Graphic addGraphic(Graphic graphic, ProjectionInfo proj) {
        ProjectionInfo toProj = this.getProjInfo();
        if (proj.equals(toProj)) {
            this.addGraphic(graphic);
            return graphic;
        } else {
            Graphic nGraphic = GeoProjectionUtil.projectClipGraphic(graphic, proj, toProj);
            this.addGraphic(nGraphic);
            return nGraphic;
        }
    }

    /**
     * Add a graphic
     *
     * @param index The graphic index
     * @param graphic The graphic
     * @param proj The graphic projection
     * @return Added graphic
     */
    public Graphic addGraphic(int index, Graphic graphic, ProjectionInfo proj) {
        ProjectionInfo toProj = this.getProjInfo();
        if (proj.equals(toProj)) {
            this.addGraphic(index, graphic);
            return graphic;
        } else {
            Graphic nGraphic = GeoProjectionUtil.projectClipGraphic(graphic, proj, toProj);
            this.addGraphic(index, nGraphic);
            return nGraphic;
        }
    }

    @Override
    public void addText(ChartText text) {
        addText(text, true);
    }

    public void addText(ChartText text, boolean isLonLat) {
        if (isLonLat) {
            if (!this.projInfo.isLonLat()) {
                PointD xyp = Reproject.reprojectPoint(text.getX(), text.getY(), KnownCoordinateSystems.geographic.world.WGS1984,
                        this.projInfo);
                text.setX(xyp.X);
                text.setY(xyp.Y);
            }
        }
        super.addText(text);
    }

    /**
     * Get full extent
     *
     * @return Full extent
     */
    public Extent getFullExtent() {
        Extent ext = this.getExtent();
        if (this.boundary != null) {
            ext = ext.union(this.boundary.getExtent().extend(0.01));
        }

        return ext;
    }

    /**
     * Zoom to exactly lon/lat extent
     *
     * @param aExtent The extent
     */
    public void zoomToExtentLonLatEx(Extent aExtent) {
        if (!this.projInfo.isLonLat()) {
            aExtent = ProjectionUtil.getProjectionExtent(ProjectionInfo.LONG_LAT, this.projInfo, aExtent, 10);
        }

        this.setDrawExtent(aExtent);
    }

    /**
     * Set longitude/latitude extent
     *
     * @param extent Extent
     */
    public void setLonLatExtent(Extent extent) {
        if (this.projInfo.isLonLat()) {
            super.setDrawExtent(extent);
        } else {
            this.zoomToExtentLonLatEx(extent);
            super.setDrawExtent1(this.getDrawExtent());
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

    /**
     * Set draw extent
     *
     * @param extent Extent
     */
    @Override
    public void setDrawExtent(Extent extent) {
        super.setDrawExtent(extent);

        if (!this.isLonLatMap()) {
            ((MapGridLine) this.gridLine).setExtent(extent);
        }
    }

    /**
     * Add point graphic
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param pb Point break
     */
    public void addPoint(double lat, double lon, PointBreak pb) {
        PointShape ps = new PointShape();
        PointD lonlatp = new PointD(lon, lat);
        if (this.isLonLatMap()) {
            ps.setPoint(lonlatp);
        } else {
            PointD xyp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                    this.getProjInfo());
            ps.setPoint(xyp);
        }
        Graphic aGraphic = new Graphic(ps, pb);
        this.addGraphic(aGraphic);
    }

    /**
     * Add point graphic
     *
     * @param lat Latitude
     * @param lon Longitude
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
            if (this.isLonLatMap()) {
                ps.setPoint(lonlatp);
            } else {
                xyp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                        this.getProjInfo());
                ps.setPoint(xyp);
            }
            Graphic aGraphic = new Graphic(ps, pb);
            this.addGraphic(aGraphic);
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
                    this.addGraphic(aGraphic);
                }
                points = new ArrayList<>();
            } else {
                lonlatp = new PointD(x, y);
                if (!this.isLonLatMap()) {
                    lonlatp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                            this.projInfo);
                }
                points.add(lonlatp);
            }
        }
        if (points.size() >= 2) {
            pls = new PolylineShape();
            pls.setPoints(points);
            Graphic aGraphic = new Graphic(pls, plb);
            this.addGraphic(aGraphic);
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
                    this.addGraphic(aGraphic);
                }
                points = new ArrayList<>();
            } else {
                lonlatp = new PointD(x, y);
                if (!this.projInfo.isLonLat()) {
                    lonlatp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                            this.projInfo);
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
            this.addGraphic(aGraphic);
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
                    this.addGraphic(aGraphic);
                }
                points = new ArrayList<>();
            } else {
                lonlatp = new PointD(x, y);
                if (!this.projInfo.isLonLat()) {
                    lonlatp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984,
                            this.projInfo);
                }
                points.add(lonlatp);
            }
        }
        if (points.size() > 2) {
            pgs = new PolygonShape();
            pgs.setPoints(points);
            Graphic aGraphic = new Graphic(pgs, pgb);
            this.addGraphic(aGraphic);
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
        this.addGraphic(graphic);

        return graphic;
    }

    @Override
    protected void drawGridLine(Graphics2D g, Rectangle2D area) {
        if (this.projInfo.isLonLat()) {
            super.drawGridLine(g, area);
        } else {
            if (this.antiAlias) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            } else {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
            }

            AffineTransform oldMatrix = g.getTransform();
            java.awt.Shape oldRegion = g.getClip();
            if (this.clip) {
                g.setClip(area);
            }
            g.translate(area.getX(), area.getY());

            MapGridLine mapGridLine = (MapGridLine) gridLine;
            //Longitude
            if (mapGridLine.isDrawXLine()) {
                if (mapGridLine.getLongitudeLines() != null) {
                    this.drawGraphics(g, mapGridLine.getLongitudeLines(), area);
                }
            }
            //Latitude
            if (mapGridLine.isDrawYLine()) {
                if (mapGridLine.getLatitudeLines() != null) {
                    this.drawGraphics(g, mapGridLine.getLatitudeLines(), area);
                }
            }

            g.setTransform(oldMatrix);
            if (this.clip) {
                g.setClip(oldRegion);
            }

            //Draw lon/lat grid labels
            if (mapGridLine.isLabelVisible()) {
                double xMin = area.getX();
                double xMax = area.getX() + area.getWidth();
                double yMin = area.getY();
                double yMax = area.getY() + area.getHeight();

                final float shift = 5.0F;
                List<Extent> extentList = new ArrayList<>();
                Extent maxExtent = new Extent();
                Extent aExtent;
                Dimension aSF;
                g.setColor(mapGridLine.getColor());
                g.setStroke(new BasicStroke(mapGridLine.getSize()));
                String drawStr;
                PointF sP = new PointF(0, 0);
                PointF eP = new PointF(0, 0);
                Axis axis = this.getXAxis();
                Font font = mapGridLine.labelFont;
                g.setFont(font);
                double labX, labY;
                float len = axis.getTickLength();
                int space = axis.getTickSpace();

                Object[] objs;
                float xShift, yShift;
                XAlign xAlign = XAlign.CENTER;
                YAlign yAlign = YAlign.CENTER;
                double[] xy;
                for (int i = 0; i < mapGridLine.getGridLabels().size(); i++) {
                    GridLabel aGL = mapGridLine.getGridLabels().get(i);
                    switch (mapGridLine.getLabelPosition()) {
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

                    labX = aGL.getCoord().X;
                    labY = aGL.getCoord().Y;
                    xy = projToScreen(labX, labY, area);
                    labX = xy[0] + xMin;
                    labY = xy[1] + yMin;
                    sP.X = (float) labX;
                    sP.Y = (float) labY;
                    if (aGL.isBorder()) {
                        switch (aGL.getLabDirection()) {
                            case South:
                                sP.Y = (float) area.getMaxY();
                                break;
                        }
                    }

                    drawStr = aGL.getLabString();
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
                                    eP.X = sP.X;
                                    if (axis.isInsideTick()) {
                                        eP.Y = sP.Y - len;
                                        labY = sP.Y + space;
                                    } else {
                                        eP.Y = sP.Y + len;
                                        labY = eP.Y + space;
                                    }
                                    xAlign = XAlign.CENTER;
                                    yAlign = YAlign.TOP;
                                    break;
                                case Weast:
                                    eP.Y = sP.Y;
                                    if (axis.isInsideTick()) {
                                        eP.X = sP.X + len;
                                        labX = sP.X - space;
                                    } else {
                                        eP.X = sP.X - len;
                                        labX = eP.X - space;
                                    }
                                    xAlign = XAlign.RIGHT;
                                    yAlign = YAlign.CENTER;
                                    break;
                                case North:
                                    eP.X = sP.X;
                                    if (axis.isInsideTick()) {
                                        eP.Y = sP.Y + len;
                                        labY = sP.Y - space;
                                    } else {
                                        eP.Y = sP.Y - len;
                                        labY = eP.Y - space;
                                    }
                                    xAlign = XAlign.CENTER;
                                    yAlign = YAlign.BOTTOM;
                                    break;
                                case East:
                                    eP.Y = sP.Y;
                                    if (axis.isInsideTick()) {
                                        eP.X = sP.X - len;
                                        labX = sP.X + space;
                                    } else {
                                        eP.X = sP.X + len;
                                        labX = eP.X + space;
                                    }
                                    xAlign = XAlign.LEFT;
                                    yAlign = YAlign.CENTER;
                                    break;
                            }
                            g.setColor(axis.getLineColor());
                            g.setStroke(new BasicStroke(axis.getLineWidth()));
                            g.draw(new Line2D.Float(sP.X, sP.Y, eP.X, eP.Y));
                            g.setColor(this.getXAxis().getTickLabelColor());
                            //g.drawString(drawStr, labX, labY);
                            Draw.drawString(g, labX, labY, drawStr, xAlign, yAlign, false);
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
    }

    /**
     * Load MeteoInfo project file
     *
     * @param fn MeteoInfo project file name
     * @param mfidx Map frame index
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
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
        /*if (mapFrames == null) {
            this.mapFrame.importProjectXML(pPath, root);
        } else {
            NodeList mfNodes = mapFrames.getElementsByTagName("MapFrame");
            Node mfNode = mfNodes.item(mfidx);
            this.mapFrame.importProjectXML(pPath, (Element) mfNode);
        }
        this.setDrawExtent(this.mapView.getViewExtent());
        this.setExtent(this.mapView.getViewExtent());*/
        System.setProperty("user.dir", userDir);
    }

    /**
     * Load MeteoInfo project file
     *
     * @param fn MeteoInfo project file name
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public void loadMIProjectFile(String fn) throws SAXException, IOException, ParserConfigurationException {
        this.loadMIProjectFile(fn, 0);
    }
    // </editor-fold>
}
