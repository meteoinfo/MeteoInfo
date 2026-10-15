package org.meteoinfo.chart.geo;

import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.MultiLineString;
import org.meteoinfo.projection.ProjectionInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapGridLine3D extends MapGridLine {

    /**
     * Constructor
     */
    public MapGridLine3D() {
        super();
        this.extent = new Extent(-100, 100, -100, 100, 0, 100);
    }

    /**
     * Constructor
     * @param projInfo Projection
     * @param extent Extent
     */
    public MapGridLine3D(ProjectionInfo projInfo, Extent extent) {
        super(true);
        this.projInfo = projInfo;
        this.setExtent(extent);
    }

    protected void updateLongitudeLines() {
        this.longitudeLines = new GraphicCollection();
        ((GraphicCollection) this.longitudeLines).setUsingLight(false);
        double latMin = this.lonLatExtent.minY;
        double latMax = this.lonLatExtent.maxY;
        double delta = this.lonLatExtent.getHeight() / (this.nPoints - 1);
        double z = this.extent.minZ;
        for (double lon : this.longitudeLocations) {
            List<Coordinate> points = new ArrayList<>();
            double lat = latMin;
            while (lat <= latMax) {
                points.add(new Coordinate(lon, lat, z));
                lat += delta;
            }
            MultiLineString line = new MultiLineString();
            line.setCoordinates(points);
            Graphic graphic = new Graphic(line, this.lineBreak);
            graphic = ProjectUtil.projectClipGraphic(graphic, ProjectionInfo.LONG_LAT, projInfo);
            graphic.getGeometry().setValue(lon);
            this.longitudeLines.add(graphic);
        }
    }

    protected void updateLatitudeLines() {
        this.latitudeLines = new GraphicCollection();
        ((GraphicCollection) this.latitudeLines).setUsingLight(false);
        double lonMin = this.lonLatExtent.minX;
        double lonMax = this.lonLatExtent.maxX;
        if (lonMin < - 170) {
            lonMin = -180;
        }
        if (lonMax > 170) {
            lonMax = 180;
        }
        double delta = (lonMax - lonMin) / (this.nPoints - 1);
        double z = this.extent.minZ;
        for (double lat : this.latitudeLocations) {
            List<Coordinate> points = new ArrayList<>();
            double lon = lonMin;
            while (lon <= lonMax) {
                points.add(new Coordinate(lon, lat, z));
                lon += delta;
            }
            MultiLineString line = new MultiLineString();
            line.setCoordinates(points);
            Graphic graphic = new Graphic(line, this.lineBreak);
            graphic = ProjectUtil.projectClipGraphic(graphic, ProjectionInfo.LONG_LAT, projInfo);
            if (graphic == null) {
                continue;
            }

            if (graphic.getGeometry().getNumParts() > 1) {
                points = (List<Coordinate>) ((MultiLineString) graphic.getGeometry()).getPolylines().get(0).getCoordinates();
                List<Coordinate> points1 = (List<Coordinate>) ((MultiLineString) graphic.getGeometry()).getPolylines().
                        get(1).getCoordinates();
                Collections.reverse(points1);
                points.addAll(points1);
                line = new MultiLineString();
                line.setCoordinates(points);
                graphic = new Graphic(line, this.lineBreak);
            }
            graphic.getGeometry().setValue(lat);
            this.latitudeLines.add(graphic);
        }
    }
}
