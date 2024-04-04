package org.meteoinfo.chart.geo;

import org.meteoinfo.chart.geo.MapGridLine;
import org.meteoinfo.geometry.graphic.GraphicCollection3D;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.PolylineZShape;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapGridLine3D extends MapGridLine {

    /**
     * Constructor
     */
    public MapGridLine3D() {
        super();
        this.extent = new Extent3D(-100, 100, -100, 100, 0, 100);
    }

    /**
     * Constructor
     * @param projInfo Projection
     * @param extent Extent
     */
    public MapGridLine3D(ProjectionInfo projInfo, Extent3D extent) {
        super(true);
        this.projInfo = projInfo;
        this.setExtent(extent);
    }

    protected void updateLongitudeLines() {
        this.longitudeLines = new GraphicCollection3D();
        ((GraphicCollection3D) this.longitudeLines).setUsingLight(false);
        double latMin = this.lonLatExtent.minY;
        double latMax = this.lonLatExtent.maxY;
        double delta = this.lonLatExtent.getHeight() / (this.nPoints - 1);
        double z = ((Extent3D)this.extent).minZ;
        for (double lon : this.longitudeLocations) {
            List<PointZ> points = new ArrayList<>();
            double lat = latMin;
            while (lat <= latMax) {
                points.add(new PointZ(lon, lat, z));
                lat += delta;
            }
            PolylineZShape line = new PolylineZShape();
            line.setPoints(points);
            Graphic graphic = new Graphic(line, this.lineBreak);
            graphic = ProjectionUtil.projectClipGraphic(graphic, ProjectionInfo.LONG_LAT, projInfo);
            graphic.getShape().setValue(lon);
            this.longitudeLines.add(graphic);
        }
    }

    protected void updateLatitudeLines() {
        this.latitudeLines = new GraphicCollection3D();
        ((GraphicCollection3D) this.latitudeLines).setUsingLight(false);
        double lonMin = this.lonLatExtent.minX;
        double lonMax = this.lonLatExtent.maxX;
        if (lonMin < - 170) {
            lonMin = -180;
        }
        if (lonMax > 170) {
            lonMax = 180;
        }
        double delta = (lonMax - lonMin) / (this.nPoints - 1);
        double z = ((Extent3D)this.extent).minZ;
        for (double lat : this.latitudeLocations) {
            List<PointZ> points = new ArrayList<>();
            double lon = lonMin;
            while (lon <= lonMax) {
                points.add(new PointZ(lon, lat, z));
                lon += delta;
            }
            PolylineZShape line = new PolylineZShape();
            line.setPoints(points);
            Graphic graphic = new Graphic(line, this.lineBreak);
            graphic = ProjectionUtil.projectClipGraphic(graphic, ProjectionInfo.LONG_LAT, projInfo);
            if (graphic.getShape().getPartNum() > 1) {
                points = (List<PointZ>) ((PolylineZShape) graphic.getShape()).getPolylines().get(0).getPointList();
                List<PointZ> points1 = (List<PointZ>) ((PolylineZShape) graphic.getShape()).getPolylines().
                        get(1).getPointList();
                Collections.reverse(points1);
                points.addAll(points1);
                line = new PolylineZShape();
                line.setPoints(points);
                graphic = new Graphic(line, this.lineBreak);
            }
            graphic.getShape().setValue(lat);
            this.latitudeLines.add(graphic);
        }
    }
}
