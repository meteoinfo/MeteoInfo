package org.meteoinfo.chart.transform;

import org.meteoinfo.common.PointZ;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class IdentityTransform extends Affine2D {

    public IdentityTransform() {
        this.affineTransform = new AffineTransform();
    }

    @Override
    public PointZ transform(PointZ p) {
        return p;
    }
}
