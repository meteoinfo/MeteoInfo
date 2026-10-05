package org.meteoinfo.chart.transform;

import org.meteoinfo.common.PointZ;

// Exponential Transform (Inverse of Log)
public class ExpTransform extends Transform {

    private final double base;
    private final boolean doX, doY;

    public ExpTransform(double base, boolean doX, boolean doY) {
        this.base = base; this.doX = doX; this.doY = doY;
    }

    @Override
    public PointZ transform(PointZ p) {
        double x = p.X, y = p.Y;
        if (doX) x = Math.pow(base, x);
        if (doY) y = Math.pow(base, y);
        clearInvalid();
        return new PointZ(x, y);
    }

    @Override
    public boolean isAffine() {
        return false;
    }
}
