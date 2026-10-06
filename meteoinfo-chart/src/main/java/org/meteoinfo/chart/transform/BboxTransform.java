package org.meteoinfo.chart.transform;

import org.meteoinfo.geometry.Extent;

import java.awt.geom.AffineTransform;

public class BboxTransform extends Affine2D {

    private Extent inBbox, outBbox;

    public BboxTransform(Extent inBbox, Extent outBbox) {
        this.inBbox = inBbox;
        this.outBbox = outBbox;
        update();
    }

    private void update() {
        this.affineTransform = new AffineTransform();
        double xScale = outBbox.getWidth() / inBbox.getWidth();
        double yScale = outBbox.getHeight() / inBbox.getHeight();
        affineTransform.translate(-inBbox.minX, -inBbox.minY);
        affineTransform.scale(xScale, yScale);
        affineTransform.translate(outBbox.minX, outBbox.minY);
        this.invalidate();
    }

    public void setInBbox(Extent inBbox) {
        this.inBbox = inBbox;
        update();
    }

    public void setOutBbox(Extent outBbox) {
        this.outBbox = outBbox;
        update();
    }

    public void setInOutBbox(Extent inBbox, Extent outBbox) {
        this.inBbox = inBbox;
        this.outBbox = outBbox;
        update();
    }
}
