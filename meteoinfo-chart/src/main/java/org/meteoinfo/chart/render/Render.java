package org.meteoinfo.chart.render;

import org.meteoinfo.geometry.graphic.GraphicCollection;

public abstract class Render {
    protected GraphicCollection graphics;

    /**
     * Constructor
     * @param graphics Graphics
     */
    public Render(GraphicCollection graphics) {
        this.graphics = graphics;
    }

    /**
     * Draw graphic
     */
    public abstract void draw();
}
