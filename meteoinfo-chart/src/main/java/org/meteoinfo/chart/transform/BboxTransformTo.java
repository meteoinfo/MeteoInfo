package org.meteoinfo.chart.transform;

import org.meteoinfo.common.Extent;

import java.awt.geom.AffineTransform;

// BBox Mapping: Maps normalized coordinates [0, 1] x [0, 1] to [bbox]
// Note: If bbox.y0 > bbox.y1, this automatically handles Y-axis flipping
// (adapting to screen coordinate system where Y increases downwards)
public class BboxTransformTo extends Affine2D {

    private Extent bbox;

    public BboxTransformTo(Extent bbox) {
        this.bbox = bbox;
        update();
    }

    private void update() {
        update(false, false);
    }

    private void update(boolean xInverted,  boolean yInverted) {
        double x = bbox.minX;
        double y = bbox.minY;
        double w = bbox.getWidth();
        double h = bbox.getHeight();
        if (xInverted) {
            if (yInverted) {
                affineTransform = new AffineTransform(-w, 0.0,
                        0.0, -h, x + w, y + h);
            } else {
                affineTransform = new AffineTransform(-w, 0.0,
                        0.0, h, x + w, y);
            }
        } else if (yInverted) {
            affineTransform = new AffineTransform(w, 0.0,
                    0.0, -h, x, y + h);
        } else {
            affineTransform = new AffineTransform( w, 0.0,
                    0.0, h, x, y);
        }
        invalidate();
    }

    public Extent getBbox() {
        return this.bbox;
    }

    public void setBbox(Extent bbox) {
        this.bbox = bbox;
        update(false, false);
    }

    public void setBbox(Extent bbox, boolean xInverted, boolean yInverted) {
        this.bbox = bbox;
        update(xInverted, yInverted);
    }
}
