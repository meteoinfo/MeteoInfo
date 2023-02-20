package org.meteoinfo.chart.graphic;

public class Model {
    protected TriMeshGraphic triMeshGraphic;

    /**
     * Constructor
     */
    public Model() {

    }

    /**
     * Constructor
     *
     * @param triMeshGraphic Triangle mesh graphic
     */
    public Model(TriMeshGraphic triMeshGraphic) {
        this.triMeshGraphic = triMeshGraphic;
    }

    /**
     * Get triangle mesh graphic
     * @return Triangle mesh graphic
     */
    public TriMeshGraphic getTriMeshGraphic() {
        return this.triMeshGraphic;
    }

    /**
     * Set triangle mesh graphic
     * @param value Triangle mesh graphic
     */
    public void setTriMeshGraphic(TriMeshGraphic value) {
        this.triMeshGraphic = value;
    }

    /**
     * Build triangle mesh graphic
     */
    protected void buildTriMeshGraphic() {

    }
}
