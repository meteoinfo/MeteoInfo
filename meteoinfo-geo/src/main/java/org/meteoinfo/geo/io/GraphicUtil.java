package org.meteoinfo.geo.io;

import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geo.layer.ImageLayer;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.chart.graphic.GraphicCollection3D;
import org.meteoinfo.geo.mapdata.ShapeFileType;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.shape.*;

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
        GraphicCollection graphics = new GraphicCollection();
        LegendScheme ls = layer.getLegendScheme();
        ColorBreak cb;
        if (xShift == 0) {
            for (Shape shape : layer.getShapes()) {
                if (shape.getLegendIndex() >= 0) {
                    cb = ls.getLegendBreak(shape.getLegendIndex());
                    graphics.add(new Graphic(shape, cb));
                }
            }
        } else {
            for (Shape shape : layer.getShapes()) {
                if (shape.getLegendIndex() >= 0) {
                    for (Coordinate p : shape.getPoints()) {
                        p.x += xShift;
                    }
                    shape.updateExtent();
                    cb = ls.getLegendBreak(shape.getLegendIndex());
                    graphics.add(new Graphic(shape, cb));
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
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZValue(offset);
        ShapeFileType shapeType = layer.getShapeType();
        LegendScheme ls = layer.getLegendScheme();
        Coordinate pz;
        ColorBreak cb;
        switch (shapeType) {
            case POINT:
                for (PointShape shape : (List<PointShape>) layer.getShapes()) {
                    PointShape s = new PointShape();
                    Coordinate pd = shape.getPoint();
                    pz = new Coordinate(pd.x + xshift, pd.y, offset);
                    s.setPoint(pz);
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    graphics.add(new Graphic(s, cb));
                }
                break;
            case POLYLINE:
                for (PolylineShape shape : (List<PolylineShape>) layer.getShapes()) {
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    for (Polyline pl : (List<Polyline>) shape.getPolylines()) {
                        PolylineShape s = new PolylineShape();
                        List<Coordinate> plist = new ArrayList<>();
                        for (Coordinate pd : pl.getPointList()) {
                            pz = new Coordinate(pd.x + xshift, pd.y, pd.z + offset);
                            plist.add(pz);
                        }
                        s.setPoints(plist);
                        graphics.add(new Graphic(s, cb));
                    }
                }
                break;
            case POLYGON:
                for (PolygonShape shape : (List<PolygonShape>) layer.getShapes()) {
                    PolygonShape s = new PolygonShape();
                    List<Coordinate> plist = new ArrayList<>();
                    for (Coordinate pd : shape.getPoints()) {
                        pz = new Coordinate(pd.x + xshift, pd.y, offset);
                        plist.add(pz);
                    }
                    s.setPartNum(shape.getPartNum());
                    s.setParts(shape.getParts());
                    s.setPoints(plist);
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    graphics.add(new Graphic(s, cb));
                }
                break;
            case POINT_Z:
            case POLYLINE_Z:
            case POLYGON_Z:
                graphics.setFixZ(false);
                switch (shapeType) {
                    case POLYLINE_Z:
                        for (PolylineShape shape : (List<PolylineShape>) layer.getShapes()) {
                            cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                            for (Polyline pl : shape.getPolylines()) {
                                PolylineShape s = new PolylineShape();
                                List<Coordinate> plist = new ArrayList<>();
                                for (Coordinate pd : (List<Coordinate>) pl.getPointList()) {
                                    pz = new Coordinate(pd.x + xshift, pd.y, pd.z + offset, pd.m);
                                    plist.add(pz);
                                }
                                s.setPoints(plist);
                                graphics.add(new Graphic(s, cb));
                            }
                        }
                        break;
                    case POLYGON_Z:
                        for (PolygonShape shape : (List<PolygonShape>) layer.getShapes()) {
                            PolygonShape s = new PolygonShape();
                            List<Coordinate> plist = new ArrayList<>();
                            for (Coordinate pd : (List<Coordinate>) shape.getPoints()) {
                                pz = new Coordinate(pd.x + xshift, pd.y, pd.z + offset, pd.m);
                                plist.add(pz);
                            }
                            s.setPartNum(shape.getPartNum());
                            s.setParts(shape.getParts());
                            s.setPoints(plist);
                            cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                            graphics.add(new Graphic(s, cb));
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
        ImageShape ishape = new ImageShape();
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
        Graphic gg = new Graphic(ishape, new ColorBreak());
        if (interpolation != null) {
            ((ImageShape) gg.getShape()).setInterpolation(interpolation);
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
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZDir("z");
        graphics.setZValue(offset);
        ImageShape ishape = new ImageShape();
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
        Graphic gg = new Graphic(ishape, new ColorBreak());
        if (interpolation != null) {
            ((ImageShape) gg.getShape()).setInterpolation(interpolation);
        }
        graphics.add(gg);

        return graphics;
    }
}
