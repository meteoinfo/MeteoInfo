package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;

import java.util.List;

public class Model extends TriMeshGraphic {

    protected Vector3f location = new Vector3f();
    protected Vector3f angle = new Vector3f();
    protected float scale = 1;

    /**
     * Constructor
     */
    public Model() {

    }

    /**
     * Get location
     * @return Location
     */
    public Vector3f getLocation() {
        return location;
    }

    /**
     * Set location
     * @param value Location
     */
    public void setLocation(Vector3f value) {
        location = value;
    }

    /**
     * Set location
     * @param value Location
     */
    public void setLocation(List<Number> value) {
        location = new Vector3f(value.get(0).floatValue(), value.get(1).floatValue(),
                value.get(2).floatValue());
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
    public void setAngle(List<Number> value) {
        angle = new Vector3f(value.get(0).floatValue(), value.get(1).floatValue(),
                value.get(2).floatValue());
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
     * Get radians angle
     * @return Radians
     */
    public Vector3f getRadians() {
        return new Vector3f((float) Math.toRadians(angle.x), (float) Math.toRadians(angle.y),
                (float) Math.toRadians(angle.z));
    }

    /**
     * Build triangle mesh graphic
     */
    protected void buildTriMeshGraphic() {

    }
}
