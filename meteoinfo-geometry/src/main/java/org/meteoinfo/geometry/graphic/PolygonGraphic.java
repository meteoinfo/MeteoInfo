package org.meteoinfo.geometry.graphic;

import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.shape.PolygonShape;

public class PolygonGraphic extends Graphic {

    /**
     * Constructor
     * @param shape Polygon shape
     * @param legend Polygon legend break
     */
    public PolygonGraphic(PolygonShape shape, PolygonBreak legend) {
        this.shape = shape;
        this.legend = legend;
    }
}
