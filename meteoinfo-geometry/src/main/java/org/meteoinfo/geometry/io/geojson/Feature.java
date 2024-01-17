package org.meteoinfo.geometry.io.geojson;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonPropertyOrder({"type", "id", "geometry", "properties"})
public class Feature extends GeoJSON {
    @JsonInclude(Include.NON_EMPTY)
    private final Object id;
    private final Geometry geometry;
    private final Map<String, Object> properties;

    public Feature(
            @JsonProperty("geometry") Geometry geometry,
            @JsonProperty("properties") Map<String,Object> properties) {
        this(null, geometry, properties);
    }

    @JsonCreator
    public Feature(
            @JsonProperty("id") Object id,
            @JsonProperty("geometry") Geometry geometry,
            @JsonProperty("properties") Map<String,Object> properties) {
        super();
        this.id = id;
        this.geometry = geometry;
        this.properties = properties;
    }

    public Object getId() {
        return id;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
