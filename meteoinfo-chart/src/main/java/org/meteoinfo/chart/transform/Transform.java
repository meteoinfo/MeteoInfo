package org.meteoinfo.chart.transform;

import org.meteoinfo.common.PointD;
import org.meteoinfo.chart.graphic.Graphic;

public abstract class Transform extends TransformNode {

    public boolean isValid() {
        return true;
    };

    // Forward transformation
    public abstract PointD transform(PointD p);

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
