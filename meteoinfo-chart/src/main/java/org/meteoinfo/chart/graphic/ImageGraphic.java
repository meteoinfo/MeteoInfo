package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.shape.ImageShape;
import org.meteoinfo.chart.legend.ColorBreak;
import org.meteoinfo.chart.legend.LegendScheme;

public class ImageGraphic extends Graphic{
    protected LegendScheme legendScheme;

    /**
     * Constructor
     */
    public ImageGraphic() {
        super();
    }

    /**
     * Constructor
     * @param shape The image shape
     * @param ls
     */
    public ImageGraphic(ImageShape shape, LegendScheme ls) {
        super(shape, new ColorBreak());
        this.legendScheme = ls;
    }

    /**
     * Get legend scheme
     *
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        return this.legendScheme;
    }

    /**
     * Set legend scheme
     *
     * @param value Legend scheme
     */
    public void setLegendScheme(LegendScheme value) {
        this.legendScheme = value;
    }
}
