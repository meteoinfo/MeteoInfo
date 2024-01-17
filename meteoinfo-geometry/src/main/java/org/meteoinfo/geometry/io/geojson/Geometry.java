package org.meteoinfo.geometry.io.geojson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes( {
        @JsonSubTypes.Type(value=Point.class, name="Point"  ),
        @JsonSubTypes.Type(value=LineString.class, name="LineString"  ),
        @JsonSubTypes.Type(value=Polygon.class, name="Polygon"  ),
        @JsonSubTypes.Type(value=MultiPoint.class, name="MultiPoint"  ),
        @JsonSubTypes.Type(value=MultiLineString.class, name="MultiLineString"  ),
        @JsonSubTypes.Type(value=MultiPolygon.class, name="MultiPolygon"  ),
        @JsonSubTypes.Type(value=Feature.class, name="Feature"  ),
        @JsonSubTypes.Type(value=FeatureCollection.class, name="FeatureCollection"  ),
        @JsonSubTypes.Type(value=GeometryCollection.class, name="GeometryCollection"  )
} )

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"type", "coordinates", "bbox"})
public abstract class Geometry extends GeoJSON {
    @JsonCreator
    public Geometry() {
        super();
    }
}
