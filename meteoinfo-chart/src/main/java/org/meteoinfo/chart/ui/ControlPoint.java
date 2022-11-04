package org.meteoinfo.chart.ui;

import java.awt.geom.Point2D;

public class ControlPoint {
    private float ratio;    // 0.f - 1.0f
    private float minRatio;
    private float maxRatio;
    private boolean selected = false;
    private Point2D.Float location = new Point2D.Float();

    /**
     * Constructor
     * @param value The ratio value
     */
    public ControlPoint(float value) {
        this.minRatio = 0.f;
        this.maxRatio = 1.f;
        setRatio(value);
    }

    /**
     * Get ratio value
     * @return The ratio value
     */
    public float getRatio() {
        return this.ratio;
    }

    /**
     * Set ratio value
     * @param value The ratio value
     */
    public void setRatio(float value) {
        if (value < this.minRatio)
            this.ratio = this.minRatio;
        else if (value > this.maxRatio)
            this.ratio = this.maxRatio;
        else
            this.ratio = value;
    }

    /**
     * Get minimum ratio
     * @return Minimum ratio
     */
    public float getMinRatio() {
        return this.minRatio;
    }

    /**
     * Set minimum ratio
     * @param value Minimum ratio
     */
    public void setMinRatio(float value) {
        this.minRatio = value;
    }

    /**
     * Get maximum ratio
     * @return Maximum ratio
     */
    public float getMaxRatio() {
        return this.maxRatio;
    }

    /**
     * Set maximum ratio
     * @param value Maximum ratio
     */
    public void setMaxRatio(float value) {
        this.maxRatio = value;
    }

    /**
     * Get whether selected
     * @return Whether selected
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * Set whether selected
     * @param value Whether selected
     */
    public void setSelected(boolean value) {
        this.selected = value;
    }

    /**
     * Get point location
     * @return Point location
     */
    public Point2D.Float getLocation() {
        return this.location;
    }

    /**
     * Set point location
     * @param location Point location
     */
    public void setLocation(Point2D.Float location) {
        this.location = location;
    }

    /**
     * Set point location
     * @param x X
     * @param y Y
     */
    public void setLocation(float x, float y) {
        this.location = new Point2D.Float(x, y);
    }

    /**
     * Set value at the ratio
     * @param min The minimum value
     * @param max The maximum value
     * @return The value at the ratio
     */
    public double getValue(double min, double max) {
        return min + (max - min) * this.ratio;
    }

    /**
     * Check whether a coordinate in this point extent
     * @param x X coordinates
     * @param y Y coordinates
     * @param size The extent size
     * @return Whether inside
     */
    public boolean isInPointExtent(float x, float y, float size) {
        float hSize = size / 2;
        if (x < location.x - hSize)
            return false;

        if (x > location.x + hSize)
            return false;

        if (y < location.y - hSize)
            return false;

        if (y > location.y + hSize)
            return false;

        return true;
    }
}
