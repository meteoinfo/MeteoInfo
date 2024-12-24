package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.chart.AspectType;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.List;

public class Model extends TriMeshGraphic {

    protected Vector3f location = null;
    protected Vector3f rotation = null;
    protected float scale = 1;
    protected Vector3f direction = null;

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

    /**
     * Build triangle mesh graphic
     */
    protected void buildTriMeshGraphic() {

    }

    /**
     * Set triangles
     * @param faceIndices The triangle face indices
     * @param x X coordinate array
     * @param y Y coordinate array
     * @param z Z coordinate array
     */
    public void setTriangles(Array faceIndices, Array x, Array y, Array z) {
        logger.info("Start set triangles...");

        x = x.copyIfView();
        y = y.copyIfView();
        z = z.copyIfView();
        faceIndices = faceIndices.copyIfView();

        this.vertexIndices = (int[]) faceIndices.getStorage();

        float xMin = ArrayMath.min(x).floatValue();
        float xMax = ArrayMath.max(x).floatValue();
        float yMin = ArrayMath.min(y).floatValue();
        float yMax = ArrayMath.max(y).floatValue();
        float zMin = ArrayMath.min(z).floatValue();
        float zMax = ArrayMath.max(z).floatValue();
        float range = Math.max(xMax - xMin, yMax - yMin);
        range = (zMax - zMin) > range ? (zMax - zMin) : range;
        float min = -range / 2;
        float max = range / 2;
        Transform transform = new Transform();
        transform.setExtent(min, max, min, max, min, max);

        int n = x.getShape()[0];
        this.vertexPosition = new float[n * 3];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            vertexPosition[idx] = transform.transform_x(x.getFloat(i));
            vertexPosition[idx + 1] = transform.transform_y(y.getFloat(i));
            vertexPosition[idx + 2] = transform.transform_z(z.getFloat(i));
            idx += 3;
        }

        updateExtent();

        logger.info("Set triangles finished!");
    }

    /**
     * Set triangles
     * @param faceIndices The triangle face indices
     * @param x X coordinate array
     * @param y Y coordinate array
     * @param z Z coordinate array
     * @param normal Normal array
     */
    public void setTriangles(Array faceIndices, Array x, Array y, Array z, Array normal) {
        logger.info("Start set triangles...");

        x = x.copyIfView();
        y = y.copyIfView();
        z = z.copyIfView();
        faceIndices = faceIndices.copyIfView();

        this.vertexIndices = (int[]) faceIndices.getStorage();
        this.vertexNormal = (float[]) normal.getStorage();

        float xMin = ArrayMath.min(x).floatValue();
        float xMax = ArrayMath.max(x).floatValue();
        float yMin = ArrayMath.min(y).floatValue();
        float yMax = ArrayMath.max(y).floatValue();
        float zMin = ArrayMath.min(z).floatValue();
        float zMax = ArrayMath.max(z).floatValue();
        float range = Math.max(xMax - xMin, yMax - yMin);
        range = (zMax - zMin) > range ? (zMax - zMin) : range;
        float min = -range / 2;
        float max = range / 2;
        Transform transform = new Transform();
        transform.setExtent(min, max, min, max, min, max);

        int n = x.getShape()[0];
        this.vertexPosition = new float[n * 3];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            vertexPosition[idx] = transform.transform_x(x.getFloat(i));
            vertexPosition[idx + 1] = transform.transform_y(y.getFloat(i));
            vertexPosition[idx + 2] = transform.transform_z(z.getFloat(i));
            idx += 3;
        }

        this.normalLoaded = true;
        updateExtent();

        logger.info("Set triangles finished!");
    }
}
