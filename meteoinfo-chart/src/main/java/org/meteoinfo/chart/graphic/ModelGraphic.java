package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayMath;

import java.util.List;

public class ModelGraphic extends Graphic {

    protected MeshGraphic meshGraphic;
    protected Vector3f location;
    protected Vector3f rotation;
    protected float scale;
    protected Vector3f direction;

    /**
     * Constructor
     * @param meshGraphic The mesh graphic
     */
    public ModelGraphic(MeshGraphic meshGraphic) {
        this.location = null;
        this.rotation = null;
        this.direction = null;
        this.scale = 1.0f;
        this.setMeshGraphic(meshGraphic);
    }

    /**
     * Get mesh graphic
     * @return Mesh graphic
     */
    public MeshGraphic getMeshGraphic() {
        return this.meshGraphic;
    }

    /**
     * Set mesh graphic
     * @param meshGraphic Mesh graphic
     */
    public void setMeshGraphic(MeshGraphic meshGraphic) {
        this.meshGraphic = meshGraphic;
        Extent3D extent = (Extent3D) this.meshGraphic.getExtent();

        float range = (float) Math.max(extent.getWidth(), extent.getHeight());
        float zRange = (float) extent.getZLength();
        range = zRange > range ? zRange : range;
        float min = -range / 2;
        float max = range / 2;
        Transform transform = new Transform();
        transform.setExtent(min, max, min, max, min, max);

        this.meshGraphic.vertexPosition = meshGraphic.getVertexPosition(transform);
        this.meshGraphic.updateExtent();
    }

    @Override
    public Extent getExtent() {
        return meshGraphic.getExtent();
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
     * Set location
     * @param x X
     * @param y Y
     * @param z Z
     */
    public void setLocation(float x, float y, float z) {
        location = new Vector3f(x, y, z);
    }

    /**
     * Get rotation
     * @return The rotation
     */
    public Vector3f getRotation() {
        return rotation;
    }

    /**
     * Set rotation
     * @param value Then rotation
     */
    public void setRotation(Vector3f value) {
        rotation = value;
    }

    /**
     * Set rotation
     * @param value The rotation
     */
    public void setRotation(List<Number> value) {
        rotation = new Vector3f(value.get(0).floatValue(), value.get(1).floatValue(),
                value.get(2).floatValue());
    }

    /**
     * Get direction
     * @return direction
     */
    public Vector3f getDirection() {
        return direction;
    }

    /**
     * Set direction
     * @param value direction
     */
    public void setDirection(Vector3f value) {
        direction = value;
    }

    /**
     * Set direction
     * @param value direction
     */
    public void setDirection(List<Number> value) {
        direction = new Vector3f(value.get(0).floatValue(), value.get(1).floatValue(), value.get(2).floatValue());
    }

    /**
     * Set direction
     * @param start Start location
     * @param end End location
     */
    public void setDirection(Vector3f start, Vector3f end) {
        direction = end.sub(start);
    }

    /**
     * Set look at
     * @param start Start location
     * @param end End location
     */
    public void setLookAt(List<Number> start, List<Number> end) {
        Vector3f sv = new Vector3f(start.get(0).floatValue(), start.get(1).floatValue(), start.get(2).floatValue());
        Vector3f ev = new Vector3f(end.get(0).floatValue(), end.get(1).floatValue(), end.get(2).floatValue());
        direction = ev.sub(sv);
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
     * Get radians rotation
     * @return Radians
     */
    public Vector3f getRotationRadians() {
        return new Vector3f((float) Math.toRadians(rotation.x), (float) Math.toRadians(rotation.y),
                (float) Math.toRadians(rotation.z));
    }

}
