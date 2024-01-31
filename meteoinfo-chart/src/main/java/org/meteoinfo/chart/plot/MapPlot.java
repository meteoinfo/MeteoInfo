/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import org.meteoinfo.chart.*;
import org.meteoinfo.chart.axis.LonLatAxis;
import org.meteoinfo.common.*;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.data.mapdata.webmap.IWebMapPanel;
import org.meteoinfo.data.mapdata.webmap.TileLoadListener;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wyq
 */
public class MapPlot extends Plot2D implements IWebMapPanel {

    // <editor-fold desc="Variables">
    private ProjectionInfo projInfo;
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
        this(ProjectionInfo.LONG_LAT);
    }

    /**
     * Constructor
     * @param projInfo Projection info
     */
    public MapPlot(ProjectionInfo projInfo) {
        super();

        this.projInfo = projInfo;
        this.aspectType = AspectType.EQUAL;
        try {
            this.setXAxis(new LonLatAxis("Longitude", true));
            this.setYAxis(new LonLatAxis("Latitude", false));
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(MapPlot.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.getAxis(Location.TOP).setDrawTickLine(false);
        this.getAxis(Location.TOP).setDrawTickLabel(false);
        this.getAxis(Location.RIGHT).setDrawTickLine(false);
        this.getAxis(Location.RIGHT).setDrawTickLabel(false);
        this.setDrawNeatLine(true);
        this.getGridLine().setTop(true);

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
    public PlotType getPlotType() {
        return PlotType.XY2D;
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
     * Get x scale
     * @return X scale
     */
    public float getXScale() {
        return 1.f;
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
        return false;
    }

    /**
     * Get web map zoom
     *
     * @return Web map zoom
     */
    @Override
    public int getWebMapZoom() {
        return 1;
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
    public void updateLegendScheme() {

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

    // </editor-fold>
}
