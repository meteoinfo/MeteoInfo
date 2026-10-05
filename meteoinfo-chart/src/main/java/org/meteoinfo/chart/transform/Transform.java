package org.meteoinfo.chart.transform;

import org.meteoinfo.common.PointZ;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;

public abstract class Transform extends TransformNode {

    public boolean isValid() {
        return true;
    };

    // Forward transformation
    public abstract PointZ transform(PointZ p);

    public Array[] transform(Array xa, Array ya) {
        xa = xa.copyIfView();
        ya = ya.copyIfView();
        Array xr = Array.factory(DataType.DOUBLE, xa.getShape());
        Array yr = Array.factory(DataType.DOUBLE, ya.getShape());
        PointZ p;
        for (int i = 0; i < xa.getSize(); i++) {
            p = new PointZ(xa.getDouble(i), ya.getDouble(i));
            p = transform(p);
            xr.setDouble(i, p.X);
            yr.setDouble(i, p.Y);
        }

        return new Array[]{xr, yr};
    }

    // Get inverted transformation (optional implementation)
    public Transform inverted() {
        throw new UnsupportedOperationException("Inverse not implemented");
    };

    public Graphic transform(Graphic graphic) {
        throw new UnsupportedOperationException("Transform graphic not implemented");
    };

    /**
     * Check whether this transform can be fully represented by affine matrix.
     * @return true if purely affine; false for non‑affine transform.
     */
    public abstract boolean isAffine();

    /**
     * Transform composition: this + other.
     * Execution order: apply {@code this} first, then apply {@code other}.
     * Mathematically: other( this(p) )
     * Equivalent to matplotlib "t1 + t2" syntax.
     * @param other subsequent transform
     * @return composite transform object
     */
    public CompositeTransform plus(Transform other) {
        return new CompositeTransform(this, other);
    }
}
