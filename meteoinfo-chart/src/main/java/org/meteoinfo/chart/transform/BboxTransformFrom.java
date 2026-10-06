package org.meteoinfo.chart.transform;

import org.meteoinfo.common.Extent;

import java.awt.geom.AffineTransform;

// BBox Normalization: Maps [bbox] to normalized coordinates [0, 1] x [0, 1]
public class BboxTransformFrom extends Affine2D {

    private Extent bbox;

    public BboxTransformFrom(Extent bbox) {
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
                affineTransform = new AffineTransform(-1.0 / w, 0.0,
                        0.0, -1.0 / h, 1.0 + x / w, 1.0 + y / h);
            } else {
                affineTransform = new AffineTransform(-1.0 / w, 0.0,
                        0.0, 1.0 / h, 1.0 + x / w, -y / h);
            }
        } else if (yInverted) {
            affineTransform = new AffineTransform(1.0 / w, 0.0,
                    0.0, -1.0 / h, -x / w, 1.0 + y / h);
        } else {
            affineTransform = new AffineTransform(1.0 / w, 0.0,
                    0.0, 1.0 / h, -x / w, -y / h);
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
