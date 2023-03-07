package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;

import java.util.List;

public class Model extends TriMeshGraphic {

    protected Vector3f angle = new Vector3f();
    protected float scale = 1;

    /**
     * Constructor
     */
    public Model() {

    }

    /**
     * Get angle
     * @return The angle
     */
    public Vector3f getAngle() {
        return angle;
    }

    /**
     * Set angle
     * @param value Then angle
     */
    public void setAngle(Vector3f value) {
        angle = value;
    }

    /**
     * Set angle
     * @param value The angle
     */
    public void setAngle(List<Float> value) {
        angle = new Vector3f(value.get(0), value.get(1), value.get(2));
    }

    /**
     * Get scale
     * @return The scale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Set value
     * @param value The value
     */
    public void setScale(float value) {
        scale = value;
    }

    /**
     * Build triangle mesh graphic
     */
    protected void buildTriMeshGraphic() {

    }
}
