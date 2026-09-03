package org.meteoinfo.chart.transform;

import org.meteoinfo.common.PointD;

// Exponential Transform (Inverse of Log)
public class ExpTransform extends Transform {

    private final double base;
    private final boolean doX, doY;

    public ExpTransform(double base, boolean doX, boolean doY) {
        this.base = base; this.doX = doX; this.doY = doY;
    }

    @Override
    public PointD transform(PointD p) {
        double x = p.X, y = p.Y;
        if (doX) x = Math.pow(base, x);
        if (doY) y = Math.pow(base, y);
        clearInvalid();
        return new PointD(x, y);
    }

    @Override
    public boolean isAffine() {
        return false;
    }
}
