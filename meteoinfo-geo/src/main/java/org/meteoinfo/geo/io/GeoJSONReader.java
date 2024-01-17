package org.meteoinfo.geo.io;

import org.meteoinfo.common.colors.ColorUtil;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geometry.io.geojson.*;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.LegendType;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.ndarray.DataType;

import java.awt.*;
import java.util.Map;

public class GeoJSONReader {

    /**
     * Create a VectorLayer from GeoJSON feature collection
     * @param features The feature collection
     * @return VectorLayer object
     */
    public static VectorLayer read(FeatureCollection features) {
        Shape shape = GeoJSONUtil.toShape(features.getFeature(0).getGeometry());
        VectorLayer layer = new VectorLayer(shape.getShapeType());
        String fieldName = "title";
        layer.editAddField(fieldName, DataType.STRING);
        LegendScheme ls = new LegendScheme(shape.getShapeType());
        ls.setLegendType(LegendType.UNIQUE_VALUE);
        ls.setFieldName(fieldName);
        for (int i = 0; i < features.getNumFeatures(); i++) {
            Feature feature = features.getFeature(i);
            Map<String, Object> properties = feature.getProperties();
            String titleValue = (String) properties.get("title");
            PolygonBreak cb = new PolygonBreak();
            cb.setStartValue(titleValue);
            cb.setCaption(titleValue);
            Color color = ColorUtil.parseToColor((String) properties.get("fill"));
            float alpha = Float.parseFloat(properties.get("fill-opacity").toString());
            color = ColorUtil.getColor(color, alpha);
            cb.setColor(color);
            color = ColorUtil.parseToColor((String) properties.get("stroke"));
            alpha = Float.parseFloat(properties.get("stroke-opacity").toString());
            color = ColorUtil.getColor(color, alpha);
            cb.setOutlineColor(color);
            float lineWidth = Float.parseFloat(properties.get("stroke-width").toString());
            cb.setOutlineSize(lineWidth);
            ls.addLegendBreak(cb);
            Geometry geometry = feature.getGeometry();
            if (geometry != null) {
                try {
                    int idx = layer.getShapeNum();
                    layer.editInsertShape(GeoJSONUtil.toShape(geometry), idx);
                    layer.editCellValue(fieldName, idx, titleValue);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        layer.setLegendScheme(ls);

        return layer;
    }

    /**
     * Create a VectorLayer from GeoJSON string
     * @param json The GeoJSON string
     * @return VectorLayer object
     */
    public static VectorLayer read(String json) {
        FeatureCollection features = (FeatureCollection) GeoJSONFactory.create(json);

        return read(features);
    }

}
