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
            if (GeoJSONUtil.getShapeType(feature) != shape.getShapeType()) {
                continue;
            }

            ColorBreak cb = GeoJSONUtil.getLegendBreak(feature);
            ls.addLegendBreak(cb);
            Geometry geometry = feature.getGeometry();
            if (geometry != null) {
                try {
                    int idx = layer.getShapeNum();
                    layer.editInsertShape(GeoJSONUtil.toShape(geometry), idx);
                    layer.editCellValue(fieldName, idx, cb.getCaption());
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
