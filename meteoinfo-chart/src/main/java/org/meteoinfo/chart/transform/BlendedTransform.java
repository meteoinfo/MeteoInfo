package org.meteoinfo.chart.transform;

import org.meteoinfo.common.PointD;

// Blended Transform: Uses different transforms for X and Y axes
// This is the key to implementing semi-log or log-log plots
public class BlendedTransform extends Transform {
    private final Transform xTransform, yTransform;

    public BlendedTransform(Transform xTransform, Transform yTransform) {
        this.xTransform = xTransform; this.yTransform = yTransform;
    }

    @Override
    public PointD transform(PointD p) {
        PointD px = xTransform.transform(p);
        PointD py = yTransform.transform(p);
        clearInvalid();
        return new PointD(px.X, py.Y);
    }

    @Override
    public boolean isAffine() {
        return false;
    }
}
