package org.meteoinfo.chart.transform;

import org.meteoinfo.common.PointZ;

// Logarithmic Transform
public class LogTransform extends Transform {

    private final double base;
    private final boolean doX, doY;

    public LogTransform(double base, boolean doX, boolean doY) {
        this.base = base; this.doX = doX; this.doY = doY;
    }

    @Override
    public PointZ transform(PointZ p) {
        double x = p.X, y = p.Y;
        if (doX && x > 0) x = Math.log(x) / Math.log(base);
        if (doY && y > 0) y = Math.log(y) / Math.log(base);
        clearInvalid();
        return new PointZ(x, y);
    }

    @Override
    public Transform inverted() {
        return new ExpTransform(base, doX, doY);
    }

    @Override
    public boolean isAffine() {
        return false;
    }

}
