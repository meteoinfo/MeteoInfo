package org.meteoinfo.chart.ui;

public class OpacityControlPoint extends ControlPoint{
    private float opacity;

    /**
     * Constructor
     *
     * @param value The value
     */
    public OpacityControlPoint(float value) {
        this(value, value);
    }

    /**
     * Constructor
     *
     * @param value The value
     * @param opacity The opacity
     */
    public OpacityControlPoint(float value, float opacity) {
        super(value);
        this.opacity = opacity;
    }

    /**
     * Set opacity
     * @return Opacity
     */
    public float getOpacity() {
        return this.opacity;
    }

    /**
     * Set opacity
     * @param value Opacity
     */
    public void setOpacity(float value) {
        if (value < 0)
            this.opacity = 0;
        else if (value > 1)
            this.opacity = 1;
        else
            this.opacity = value;
    }
}
