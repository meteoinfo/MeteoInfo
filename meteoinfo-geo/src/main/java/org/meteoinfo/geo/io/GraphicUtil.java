package org.meteoinfo.geo.io;

import org.meteoinfo.chart.graphic.*;
import org.meteoinfo.geometry.*;
import org.meteoinfo.geo.layer.ImageLayer;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geo.mapdata.ShapeFileType;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.special.*;
import org.meteoinfo.geometry.special.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GraphicUtil {

    /**
     * Create 3D graphics from a VectorLayer.
     *
     * @param layer The layer
     * @param xShift X shift - to shift the graphics in x direction, normally
     * for map in 180 - 360 degree east
     * @return Graphics
     */
    public static GraphicCollection layerToGraphics(VectorLayer layer, double xShift) {
        GraphicCollection graphics = GraphicCollection.factory(layer.getGeometryType());
        LegendScheme ls = layer.getLegendScheme();
        ColorBreak cb;
        if (xShift == 0) {
            for (Geometry shape : layer.getShapes()) {
                if (shape.getLegendIndex() >= 0) {
                    cb = ls.getLegendBreak(shape.getLegendIndex());
                    graphics.add(Graphic.factory(shape, cb));
                }
            }
        } else {
            for (Geometry shape : layer.getShapes()) {
                if (shape.getLegendIndex() >= 0) {
                    for (Coordinate p : shape.getCoordinates()) {
                        p.x += xShift;
                    }
                    shape.updateExtent();
                    cb = ls.getLegendBreak(shape.getLegendIndex());
                    graphics.add(Graphic.factory(shape, cb));
                }
            }
        }
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create 3D graphics from a VectorLayer.
     *
     * @param layer The layer
     * @param offset Offset of z axis.
     * @param xshift X shift - to shift the graphics in x direction, normally
     * for map in 180 - 360 degree east
     * @return Graphics
     */
    public static GraphicCollection layerToGraphics(VectorLayer layer, double offset, double xshift) {
        GraphicCollection graphics = GraphicCollection.factory(layer.getGeometryType());
        graphics.setFixZ(true);
        graphics.setZValue(offset);
        LegendScheme ls = layer.getLegendScheme();
        Coordinate pz;
        ColorBreak cb;
        ShapeFileType shapeFileType = layer.getShapeType();
        switch (shapeFileType) {
            case POINT:
                for (Point shape : (List<Point>) layer.getShapes()) {
                    Point s = new Point();
                    Coordinate pd = shape.getCoordinate();
                    pz = new Coordinate(pd.x + xshift, pd.y, offset);
                    s.setCoordinate(pz);
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    graphics.add(Graphic.factory(s, cb));
                }
                break;
            case POLYLINE:
                for (MultiLineString shape : (List<MultiLineString>) layer.getShapes()) {
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    for (LineString pl : (List<LineString>) shape.getPolylines()) {
                        MultiLineString s = new MultiLineString();
                        List<Coordinate> plist = new ArrayList<>();
                        for (Coordinate pd : pl.getCoordinates()) {
                            pz = new Coordinate(pd.x + xshift, pd.y, pd.z + offset);
                            plist.add(pz);
                        }
                        s.setCoordinates(plist);
                        graphics.add(Graphic.factory(s, cb));
                    }
                }
                break;
            case POLYGON:
                for (MultiPolygon shape : (List<MultiPolygon>) layer.getShapes()) {
                    MultiPolygon s = new MultiPolygon();
                    List<Coordinate> plist = new ArrayList<>();
                    for (Coordinate pd : shape.getCoordinates()) {
                        pz = new Coordinate(pd.x + xshift, pd.y, offset);
                        plist.add(pz);
                    }
                    s.setPartNum(shape.getNumParts());
                    s.setParts(shape.getParts());
                    s.setCoordinates(plist);
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    graphics.add(Graphic.factory(s, cb));
                }
                break;
            case POINT_Z:
            case POLYLINE_Z:
            case POLYGON_Z:
                graphics.setFixZ(false);
                switch (shapeFileType) {
                    case POLYLINE_Z:
                        for (MultiLineString shape : (List<MultiLineString>) layer.getShapes()) {
                            cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                            for (LineString pl : shape.getPolylines()) {
                                MultiLineString s = new MultiLineString();
                                List<Coordinate> plist = new ArrayList<>();
                                for (Coordinate pd : pl.getCoordinates()) {
                                    pz = new Coordinate(pd.x + xshift, pd.y, pd.z + offset, pd.m);
                                    plist.add(pz);
                                }
                                s.setCoordinates(plist);
                                graphics.add(Graphic.factory(s, cb));
                            }
                        }
                        break;
                    case POLYGON_Z:
                        for (MultiPolygon shape : (List<MultiPolygon>) layer.getShapes()) {
                            MultiPolygon s = new MultiPolygon();
                            List<Coordinate> plist = new ArrayList<>();
                            for (Coordinate pd : (List<Coordinate>) shape.getCoordinates()) {
                                pz = new Coordinate(pd.x + xshift, pd.y, pd.z + offset, pd.m);
                                plist.add(pz);
                            }
                            s.setPartNum(shape.getNumParts());
                            s.setParts(shape.getParts());
                            s.setCoordinates(plist);
                            cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                            graphics.add(Graphic.factory(s, cb));
                        }
                        break;
                }
                break;
        }
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create image graphic from ImageLayer
     *
     * @param layer Image layer
     * @param xShift X shift - to shift the graphics in x direction, normally
     * for map in 180 - 360 degree east
     * @param interpolation Interpolation
     * @return Graphics
     */
    public static GraphicCollection layerToGraphics(ImageLayer layer, double xShift,
                                                    String interpolation) {
        GraphicCollection graphics = new GraphicCollection();
        Image ishape = new Image();
        ishape.setImage(layer.getImage());
        Extent extent = layer.getExtent();
        extent = extent.shift(xShift, 0);
        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(extent.minX + xShift, extent.minY, 0));
        coords.add(new Coordinate(extent.maxX + xShift, extent.minY, 0));
        coords.add(new Coordinate(extent.maxX + xShift, extent.maxY, 0));
        coords.add(new Coordinate(extent.minX + xShift, extent.maxY, 0));
        ishape.setExtent(extent);
        ishape.setCoords(coords);
        Graphic gg = Graphic.factory(ishape, new ColorBreak());
        if (interpolation != null) {
            ((Image) gg.getGeometry()).setInterpolation(interpolation);
        }
        graphics.add(gg);

        return graphics;
    }

    /**
     * Create image graphic from ImageLayer
     *
     * @param layer Image layer
     * @param offset Offset of z axis
     * @param xshift X shift - to shift the graphics in x direction, normally
     * for map in 180 - 360 degree east
     * @param interpolation Interpolation
     * @return Graphics
     */
    public static GraphicCollection layerToGraphics(ImageLayer layer, double offset, double xshift,
                                                String interpolation) {
        GraphicCollection graphics = new GraphicCollection();
        graphics.setFixZ(true);
        graphics.setZDir("z");
        graphics.setZValue(offset);
        Image ishape = new Image();
        ishape.setImage(layer.getImage());
        Extent extent = layer.getExtent();
        Extent ex3 = new Extent(extent.minX + xshift, extent.maxX + xshift, extent.minY, extent.maxY, offset, offset);
        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(extent.minX + xshift, extent.minY, offset));
        coords.add(new Coordinate(extent.maxX + xshift, extent.minY, offset));
        coords.add(new Coordinate(extent.maxX + xshift, extent.maxY, offset));
        coords.add(new Coordinate(extent.minX + xshift, extent.maxY, offset));
        ishape.setExtent(ex3);
        ishape.setCoords(coords);
        Graphic gg = Graphic.factory(ishape, new ColorBreak());
        if (interpolation != null) {
            ((Image) gg.getGeometry()).setInterpolation(interpolation);
        }
        graphics.add(gg);

        return graphics;
    }
}
