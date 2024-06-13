package org.meteoinfo.geo.io;

import org.meteoinfo.common.Extent;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geo.layer.ImageLayer;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.graphic.GraphicCollection3D;
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
                    for (PointD p : shape.getPoints()) {
                        p.X += xShift;
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
     * @param xShift X shift - to shift the graphics in x direction, normally
     * for map in 180 - 360 degree east
     * @return Graphics
     */
    public static GraphicCollection layerToGraphics_back(VectorLayer layer, double xShift) {
        GraphicCollection graphics = new GraphicCollection();
        ShapeTypes shapeType = layer.getShapeType();
        LegendScheme ls = layer.getLegendScheme();
        ColorBreak cb;
        switch (shapeType) {
            case POINT:
                for (PointShape shape : (List<PointShape>) layer.getShapes()) {
                    if (shape.getLegendIndex() >= 0) {
                        shape.getPoint().X += xShift;
                        cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                        graphics.add(new Graphic(shape, cb));
                    }
                }
                break;
            case POLYLINE:
                for (PolylineShape shape : (List<PolylineShape>) layer.getShapes()) {
                    if (shape.getLegendIndex() >= 0) {
                        cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                        for (Polyline pl : (List<Polyline>) shape.getPolylines()) {
                            PolylineShape s = new PolylineShape();
                            List<PointD> plist = new ArrayList<>();
                            for (PointD p : pl.getPointList()) {
                                p.X += xShift;
                                plist.add(p);
                            }
                            s.setPoints(plist);
                            graphics.add(new Graphic(s, cb));
                        }
                    }
                }
                break;
            case POLYGON:
                for (PolygonShape shape : (List<PolygonShape>) layer.getShapes()) {
                    if (shape.getLegendIndex() >= 0) {
                        PolygonShape s = new PolygonShape();
                        List<PointD> plist = new ArrayList<>();
                        for (PointD p : shape.getPoints()) {
                            p.X += xShift;
                            plist.add(p);
                        }
                        s.setPartNum(shape.getPartNum());
                        s.setParts(shape.getParts());
                        s.setPoints(plist);
                        cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                        graphics.add(new Graphic(s, cb));
                    }
                }
                break;
            case POINT_Z:
            case POLYLINE_Z:
            case POLYGON_Z:
                graphics = new GraphicCollection3D();
                ((GraphicCollection3D) graphics).setFixZ(false);
                switch (shapeType) {
                    case POINT_Z:
                        for (PointZShape shape : (List<PointZShape>) layer.getShapes()) {
                            if (shape.getLegendIndex() >= 0) {
                                ((PointZ) shape.getPoint()).X += xShift;
                                cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                                graphics.add(new Graphic(shape, cb));
                            }
                        }
                        break;
                    case POLYLINE_Z:
                        for (PolylineZShape shape : (List<PolylineZShape>) layer.getShapes()) {
                            if (shape.getLegendIndex() >= 0) {
                                cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                                for (PointZ p : (List<PointZ>) shape.getPoints()) {
                                    p.X += xShift;
                                }
                                graphics.add(new Graphic(shape, cb));
                                /*for (PolylineZ pl : (List<PolylineZ>) shape.getPolylines()) {
                                    PolylineZShape s = new PolylineZShape();
                                    List<PointZ> plist = new ArrayList<>();
                                    for (PointZ p : (List<PointZ>) pl.getPointList()) {
                                        p.X += xShift;
                                        plist.add(p);
                                    }
                                    s.setPoints(plist);
                                    graphics.add(new Graphic(s, cb));
                                }*/
                            }
                        }
                        break;
                    case POLYGON_Z:
                        for (PolygonZShape shape : (List<PolygonZShape>) layer.getShapes()) {
                            if (shape.getLegendIndex() >= 0) {
                                PolygonZShape s = new PolygonZShape();
                                List<PointZ> plist = new ArrayList<>();
                                for (PointZ p : (List<PointZ>) shape.getPoints()) {
                                    p.X += xShift;
                                    plist.add(p);
                                }
                                s.setPartNum(shape.getPartNum());
                                s.setParts(shape.getParts());
                                s.setPoints(plist);
                                cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                                graphics.add(new Graphic(s, cb));
                            }
                        }
                        break;
                }
                break;
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
        ShapeTypes shapeType = layer.getShapeType();
        LegendScheme ls = layer.getLegendScheme();
        PointZ pz;
        ColorBreak cb;
        switch (shapeType) {
            case POINT:
                for (PointShape shape : (List<PointShape>) layer.getShapes()) {
                    PointZShape s = new PointZShape();
                    PointD pd = shape.getPoint();
                    pz = new PointZ(pd.X + xshift, pd.Y, offset);
                    s.setPoint(pz);
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    graphics.add(new Graphic(s, cb));
                }
                break;
            case POLYLINE:
                for (PolylineShape shape : (List<PolylineShape>) layer.getShapes()) {
                    cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                    for (Polyline pl : (List<Polyline>) shape.getPolylines()) {
                        PolylineZShape s = new PolylineZShape();
                        List<PointZ> plist = new ArrayList<>();
                        for (PointD pd : pl.getPointList()) {
                            pz = new PointZ(pd.X + xshift, pd.Y, offset);
                            plist.add(pz);
                        }
                        s.setPoints(plist);
                        graphics.add(new Graphic(s, cb));
                    }
                }
                break;
            case POLYGON:
                for (PolygonShape shape : (List<PolygonShape>) layer.getShapes()) {
                    PolygonZShape s = new PolygonZShape();
                    List<PointZ> plist = new ArrayList<>();
                    for (PointD pd : shape.getPoints()) {
                        pz = new PointZ(pd.X + xshift, pd.Y, offset);
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
                    case POINT_Z:
                        for (PointZShape shape : (List<PointZShape>) layer.getShapes()) {
                            PointZShape s = new PointZShape();
                            PointZ pd = (PointZ) shape.getPoint();
                            pz = new PointZ(pd.X + xshift, pd.Y, pd.Z + offset, pd.M);
                            s.setPoint(pz);
                            cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                            graphics.add(new Graphic(s, cb));
                        }
                        break;
                    case POLYLINE_Z:
                        for (PolylineZShape shape : (List<PolylineZShape>) layer.getShapes()) {
                            cb = ls.getLegendBreaks().get(shape.getLegendIndex());
                            for (PolylineZ pl : (List<PolylineZ>) shape.getPolylines()) {
                                PolylineZShape s = new PolylineZShape();
                                List<PointZ> plist = new ArrayList<>();
                                for (PointZ pd : (List<PointZ>) pl.getPointList()) {
                                    pz = new PointZ(pd.X + xshift, pd.Y, pd.Z + offset, pd.M);
                                    plist.add(pz);
                                }
                                s.setPoints(plist);
                                graphics.add(new Graphic(s, cb));
                            }
                        }
                        break;
                    case POLYGON_Z:
                        for (PolygonZShape shape : (List<PolygonZShape>) layer.getShapes()) {
                            PolygonZShape s = new PolygonZShape();
                            List<PointZ> plist = new ArrayList<>();
                            for (PointZ pd : (List<PointZ>) shape.getPoints()) {
                                pz = new PointZ(pd.X + xshift, pd.Y, pd.Z + offset, pd.M);
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
        List<PointZ> coords = new ArrayList<>();
        coords.add(new PointZ(extent.minX + xShift, extent.minY, 0));
        coords.add(new PointZ(extent.maxX + xShift, extent.minY, 0));
        coords.add(new PointZ(extent.maxX + xShift, extent.maxY, 0));
        coords.add(new PointZ(extent.minX + xShift, extent.maxY, 0));
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
        Extent3D ex3 = new Extent3D(extent.minX + xshift, extent.maxX + xshift, extent.minY, extent.maxY, offset, offset);
        List<PointZ> coords = new ArrayList<>();
        coords.add(new PointZ(extent.minX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.maxY, offset));
        coords.add(new PointZ(extent.minX + xshift, extent.maxY, offset));
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
