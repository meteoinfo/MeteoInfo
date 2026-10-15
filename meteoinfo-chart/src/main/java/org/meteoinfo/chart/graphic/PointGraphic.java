package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.Point;

public class PointGraphic extends Graphic {

    private Coordinate coordinate;

    /**
     * Constructor
     * @param pointShape Point shape
     * @param pointBreak Point break
     */
    public PointGraphic(Point pointShape, PointBreak pointBreak) {
        this.geometry = pointShape;
        this.legendBreak = pointBreak;
        this.coordinate = pointShape.getCoordinate();
    }

    /**
     * Get x
     * @return X
     */
    public double getX() {
        return this.coordinate.x;
    }

    /**
     * Set x
     * @param value X
     */
    public void setX(double value) {
        this.coordinate.x = value;
    }

    /**
     * Get y
     * @return Y
     */
    public double getY() {
        return this.coordinate.y;
    }

    /**
     * Set y
     * @param value Y
     */
    public void setY(double value) {
        this.coordinate.y = value;
    }

    /**
     * Get z
     * @return Z
     */
    public double getZ() {
        return this.coordinate.z;
    }

    /**
     * Set z
     * @param value Z
     */
    public void setZ(double value) {
        this.coordinate.z = value;
    }
}
