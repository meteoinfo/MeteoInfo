package org.meteoinfo.chart.transform;

import org.meteoinfo.geometry.Coordinate;

// Blended Transform: Uses different transforms for X and Y axes
// This is the key to implementing semi-log or log-log plots
public class BlendedTransform extends Transform {
    private final Transform xTransform, yTransform;

    public BlendedTransform(Transform xTransform, Transform yTransform) {
        this.xTransform = xTransform; this.yTransform = yTransform;
    }

    @Override
    public Coordinate transform(Coordinate p) {
        Coordinate px = xTransform.transform(p);
        Coordinate py = yTransform.transform(p);
        clearInvalid();
        return new Coordinate(px.x, py.y);
    }

    @Override
    public boolean isAffine() {
        return false;
    }
}
