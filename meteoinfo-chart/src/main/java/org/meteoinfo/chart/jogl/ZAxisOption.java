package org.meteoinfo.chart.jogl;

import org.meteoinfo.common.PointF;

public class ZAxisOption {
    float x;
    float y;
    boolean left;

    /**
     * Constructor
     */
    public ZAxisOption() {
        this.x = 0;
        this.y = 0;
        this.left = true;
    }

    /**
     * Constructor
     * @param x X
     * @param y Y
     * @param left Whether left tick
     */
    public ZAxisOption(float x, float y, boolean left) {
        this.x = x;
        this.y = y;
        this.left = left;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public PointF getLocation() {
        return new PointF(x, y);
    }
}
