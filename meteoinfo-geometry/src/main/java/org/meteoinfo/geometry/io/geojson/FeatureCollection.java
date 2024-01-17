package org.meteoinfo.geometry.io.geojson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"type", "features"})
public class FeatureCollection extends GeoJSON {
    private final Feature[] features;

    @JsonCreator
    public FeatureCollection(@JsonProperty("features") Feature[] features) {
        super();
        this.features = features;
    }

    /**
     * Get features
     * @return Features
     */
    public Feature[] getFeatures() {
        return features;
    }

    /**
     * Get number of features
     * @return Number of features
     */
    public int getNumFeatures() {
        return features.length;
    }

    /**
     * Get a feature by index
     * @param index The index
     * @return The feature
     */
    public Feature getFeature(int index) {
        return features[index];
    }
}
