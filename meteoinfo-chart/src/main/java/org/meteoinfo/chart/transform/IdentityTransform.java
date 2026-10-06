package org.meteoinfo.chart.transform;

import org.meteoinfo.geometry.Coordinate;

import java.awt.geom.AffineTransform;

public class IdentityTransform extends Affine2D {

    public IdentityTransform() {
        this.affineTransform = new AffineTransform();
    }

    @Override
    public Coordinate transform(Coordinate p) {
        return p;
    }
}
