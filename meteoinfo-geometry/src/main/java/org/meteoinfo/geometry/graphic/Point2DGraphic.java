package org.meteoinfo.geometry.graphic;

import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.shape.PointShape;

public class Point2DGraphic extends Graphic {

    double x, y;

    /**
     * Constructor
     * @param pointShape Point shape
     * @param pointBreak Point break
     */
    public Point2DGraphic(PointShape pointShape, PointBreak pointBreak) {
        this.shape = pointShape;
        this.legend = pointBreak;
        this.x = pointShape.getPoint().X;
        this.y = pointShape.getPoint().Y;
    }

    /**
     * Get x
     * @return X
     */
    public double getX() {
        return this.x;
    }

    /**
     * Set x
     * @param value X
     */
    public void setX(double value) {
        this.x = value;
        ((PointShape) this.shape).getPoint().X = value;
    }

    /**
     * Get y
     * @return Y
     */
    public double getY() {
        return this.y;
    }

    /**
     * Set y
     * @param value Y
     */
    public void setY(double value) {
        this.y = value;
        ((PointShape) this.shape).getPoint().Y = value;
    }
}
