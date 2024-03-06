package org.meteoinfo.geo.io;

import org.meteoinfo.common.colors.ColorUtil;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.io.geojson.Feature;
import org.meteoinfo.geometry.io.geojson.FeatureCollection;
import org.meteoinfo.geometry.io.geojson.GeoJSONUtil;
import org.meteoinfo.geometry.io.geojson.Geometry;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.Shape;

import java.util.HashMap;
import java.util.Map;

public class GeoJSONWriter {

    /**
     * Write a vector layer to GeoJSON feature collection
     *
     * @param layer The vector layer
     * @return GeoJSON feature collection
     */
    public static FeatureCollection write(VectorLayer layer) {
        Feature[] features = new Feature[layer.getShapeNum()];
        LegendScheme ls = layer.getLegendScheme();
        for (int i = 0; i < layer.getShapeNum(); i++) {
            Shape shape = layer.getShape(i);
            Geometry geometry = GeoJSONUtil.fromShape(shape);
            Map<String, Object> properties = new HashMap<>();
            ColorBreak cb = ls.getLegendBreak(shape.getLegendIndex());
            switch (cb.getBreakType()) {
                case POINT_BREAK:
                    PointBreak pointBreak = (PointBreak) cb;
                    properties.put("marker-size", "medium");
                    properties.put("marker-color", ColorUtil.toHex(pointBreak.getColor()));
                    break;
                case POLYLINE_BREAK:
                    PolylineBreak polylineBreak = (PolylineBreak) cb;
                    properties.put("stroke", ColorUtil.toHex(polylineBreak.getColor()));
                    properties.put("stroke-opacity", polylineBreak.getColor().getAlpha() / 255.f);
                    properties.put("stroke-width", polylineBreak.getWidth());
                    break;
                case POLYGON_BREAK:
                    PolygonBreak polygonBreak = (PolygonBreak) cb;
                    properties.put("fill", ColorUtil.toHex(cb.getColor()));
                    properties.put("fill-opacity", cb.getColor().getAlpha() / 255.f);
                    if (polygonBreak.isDrawOutline()) {
                        properties.put("stroke", ColorUtil.toHex(polygonBreak.getOutlineColor()));
                        properties.put("stroke-opacity", polygonBreak.getOutlineColor().getAlpha() / 255.f);
                        properties.put("stroke-width", polygonBreak.getOutlineSize());
                    } else {
                        properties.put("stroke-opacity", 0);
                    }
                    break;
            }
            properties.put("title", cb.getCaption());
            features[i] = new Feature(geometry, properties);
        }

        return new FeatureCollection(features);
    }

    /**
     * Write a GeoGraphicCollection to GeoJSON feature collection
     *
     * @param graphics The GeoGraphicCollection object
     * @return GeoJSON feature collection
     */
    public static FeatureCollection write(GraphicCollection graphics) {
        Feature[] features = new Feature[graphics.getNumGraphics()];
        for (int i = 0; i < graphics.getNumGraphics(); i++) {
            Graphic graphic = graphics.getGraphicN(i);
            Shape shape = graphic.getShape();
            Geometry geometry = GeoJSONUtil.fromShape(shape);
            Map<String, Object> properties = new HashMap<>();
            ColorBreak cb = graphic.getLegend();
            switch (cb.getBreakType()) {
                case POINT_BREAK:
                    PointBreak pointBreak = (PointBreak) cb;
                    properties.put("marker-size", "medium");
                    properties.put("marker-color", ColorUtil.toHex(pointBreak.getColor()));
                    break;
                case POLYLINE_BREAK:
                    PolylineBreak polylineBreak = (PolylineBreak) cb;
                    properties.put("stroke", ColorUtil.toHex(polylineBreak.getColor()));
                    properties.put("stroke-opacity", polylineBreak.getColor().getAlpha() / 255.f);
                    properties.put("stroke-width", polylineBreak.getWidth());
                    break;
                case POLYGON_BREAK:
                    PolygonBreak polygonBreak = (PolygonBreak) cb;
                    properties.put("fill", ColorUtil.toHex(cb.getColor()));
                    properties.put("fill-opacity", cb.getColor().getAlpha() / 255.f);
                    if (polygonBreak.isDrawOutline()) {
                        properties.put("stroke", ColorUtil.toHex(polygonBreak.getOutlineColor()));
                        properties.put("stroke-opacity", polygonBreak.getOutlineColor().getAlpha() / 255.f);
                        properties.put("stroke-width", polygonBreak.getOutlineSize());
                    } else {
                        properties.put("stroke-opacity", 0);
                    }
                    break;
            }
            properties.put("title", cb.getCaption());
            features[i] = new Feature(geometry, properties);
        }

        return new FeatureCollection(features);
    }

}
